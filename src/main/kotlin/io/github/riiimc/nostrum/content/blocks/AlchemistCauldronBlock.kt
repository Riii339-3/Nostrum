package io.github.riiimc.nostrum.content.blocks

import io.github.riiimc.nostrum.Nostrum
import io.github.riiimc.nostrum.NostrumConfig
import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.compat.NostrumCompat
import io.github.riiimc.nostrum.content.blockentities.AlchemistCauldronBlockEntity
import io.github.riiimc.nostrum.content.blockentities.PotionData
import io.github.riiimc.nostrum.content.components.AlchemicalPotionContent
import io.github.riiimc.nostrum.content.components.AlchemicalPotionForm
import io.github.riiimc.nostrum.content.components.AlchemicalUpgradeComponent
import io.github.riiimc.nostrum.content.recipes.AlchemistCauldronMode
import io.github.riiimc.nostrum.content.upgrade.AttributeData
import io.github.riiimc.nostrum.content.upgrade.UpgradeManage
import io.github.riiimc.nostrum.utils.NostrumTags
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import java.util.*

class AlchemistCauldronBlock(
    properties: Properties
) : Block(properties), EntityBlock {

    override fun newBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity {
        return AlchemistCauldronBlockEntity(pos, state)
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {

        if (!player.isShiftKeyDown) {
            return super.useWithoutItem(
                state,
                level,
                pos,
                player,
                hit
            )
        }

        val blockEntity =
            level.getBlockEntity(pos) as? AlchemistCauldronBlockEntity
                ?: return super.useWithoutItem(
                    state,
                    level,
                    pos,
                    player,
                    hit
                )

        if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) {
            return super.useWithoutItem(
                state,
                level,
                pos,
                player,
                hit
            )
        }

        if (blockEntity.inventory.slots <= 0) {
            return super.useWithoutItem(
                state,
                level,
                pos,
                player,
                hit
            )
        }

        /*
         * クライアントでは実際のインベントリ変更をしない。
         * SUCCESSだけ返してサーバー側に処理させる。
         */
        if (level.isClientSide) {
            return InteractionResult.SUCCESS
        }

        val lastSlot = blockEntity.inventory.slots - 1
        val stack = blockEntity.inventory.getStackInSlot(lastSlot)

        if (stack.isEmpty) {
            blockEntity.inventory.compact()

            if (blockEntity.inventory.slots <= 0) {
                blockEntity.setChanged()
                return InteractionResult.SUCCESS
            }
        }

        val actualLastSlot = blockEntity.inventory.slots - 1
        val result = blockEntity.inventory.getStackInSlot(actualLastSlot)

        if (result.isEmpty) {
            return InteractionResult.SUCCESS
        }

        blockEntity.inventory.setStackInSlot(
            actualLastSlot,
            ItemStack.EMPTY
        )

        blockEntity.inventory.resize(
            (blockEntity.inventory.slots - 1).coerceAtLeast(0)
        )

        player.setItemInHand(
            InteractionHand.MAIN_HAND,
            result
        )

        blockEntity.setChanged()

        return InteractionResult.SUCCESS
    }

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        result: BlockHitResult
    ): ItemInteractionResult {

        val blockEntity =
            level.getBlockEntity(pos) as? AlchemistCauldronBlockEntity
                ?: return super.useItemOn(
                    stack,
                    state,
                    level,
                    pos,
                    player,
                    hand,
                    result
                )

        /*
         * -------------------------------------------------------------
         * クライアント側
         * -------------------------------------------------------------
         *
         * 実際の状態変更は絶対にここでは行わない。
         *
         * WandのAlchemyだけはパーティクルを出す。
         */
        if (level.isClientSide) {
            if (stack.`is`(NostrumRegistries.ALCHEMIST_WAND.get())) {

                if (
                    !player.isShiftKeyDown &&
                    blockEntity.mode == AlchemistCauldronMode.ALCHEMY
                ) {
                    val recipe = blockEntity.checkRecipe()

                    clientParticle(
                        level,
                        pos,
                        recipe != null
                    )
                }

                return ItemInteractionResult.SUCCESS
            }

            /*
             * バケツ・ポーション等もサーバー側で処理する。
             */
            return ItemInteractionResult.SUCCESS
        }

        /*
         * =============================================================
         * ALCHEMIST WAND
         * =============================================================
         */
        if (stack.`is`(NostrumRegistries.ALCHEMIST_WAND.get())) {

            if (player.isShiftKeyDown) {
                /*
                blockEntity.mode.next()

                player.displayClientMessage(
                    Component.translatable(
                        "block.nostrum.alchemist_cauldron.mode.${blockEntity.mode.name.lowercase()}"
                    ),
                    true
                )
                */

                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
            }

            val slot = when (hand) {
                InteractionHand.MAIN_HAND -> EquipmentSlot.MAINHAND
                InteractionHand.OFF_HAND -> EquipmentSlot.OFFHAND
            }

            when (blockEntity.mode) {

                /*
                 * =====================================================
                 * ALCHEMY
                 * =====================================================
                 */
                AlchemistCauldronMode.ALCHEMY -> {

                    /*
                     * outputFluidが残っている間は新しい錬金をしない。
                     */
                    if (!blockEntity.outputFluid.isEmpty) {
                        return ItemInteractionResult.SUCCESS
                    }

                    val recipe = blockEntity.checkRecipe()

                    /*
                     * レシピがない場合
                     *
                     * 既存仕様通り材料を1個ずつ消費して
                     * 液体を空にする。
                     */
                    if (recipe == null) {

                        if (blockEntity.inventory.slots > 0) {
                            for (i in 0 until blockEntity.inventory.slots) {
                                val item =
                                    blockEntity.inventory.getStackInSlot(i)

                                if (!item.isEmpty) {
                                    item.shrink(1)
                                }
                            }
                        }

                        blockEntity.fluid = FluidStack.EMPTY
                        blockEntity.inventory.compact()
                        blockEntity.setChanged()

                        /*
                         * 実際に処理したのでWand耐久を消費。
                         */
                        stack.hurtAndBreak(
                            1,
                            player,
                            slot
                        )

                        return ItemInteractionResult.SUCCESS
                    }

                    /*
                     * レシピ入力を生成。
                     */
                    val input = blockEntity.createRecipeInput()

                    /*
                     * 結果を先に計算。
                     */
                    val resultData =
                        recipe.assembleAlchemy(input)

                    val resultStack = resultData.result
                    val resultFluid = resultData.resultFluid

                    /*
                     * 液体量が足りない場合は絶対に消費しない。
                     */
                    if (blockEntity.fluid.amount < recipe.inputFluid.amount) {
                        return ItemInteractionResult.SUCCESS
                    }

                    /*
                     * 材料を消費。
                     */
                    for (i in 0 until blockEntity.inventory.slots) {
                        val item =
                            blockEntity.inventory.getStackInSlot(i)

                        if (!item.isEmpty) {
                            item.shrink(1)
                        }
                    }

                    /*
                     * 液体を消費。
                     */
                    blockEntity.fluid.shrink(
                        recipe.inputFluid.amount
                    )

                    if (blockEntity.fluid.isEmpty) {
                        blockEntity.fluid = FluidStack.EMPTY
                    }

                    /*
                     * 結果アイテム。
                     */
                    if (!resultStack.isEmpty) {
                        val itemEntity = ItemEntity(
                            level,
                            pos.x + 0.5,
                            pos.y + 1.2,
                            pos.z + 0.5,
                            resultStack
                        )

                        itemEntity.setDefaultPickUpDelay()
                        level.addFreshEntity(itemEntity)
                    }

                    /*
                     * 結果Fluid。
                     */
                    if (!resultFluid.isEmpty) {
                        blockEntity.outputFluid =
                            resultFluid.copy()
                    }

                    blockEntity.inventory.compact()
                    blockEntity.setChanged()

                    /*
                     * 成功したのでWand耐久を消費。
                     */
                    stack.hurtAndBreak(
                        1,
                        player,
                        slot
                    )

                }

                /*
                 * =====================================================
                 * POTION
                 * =====================================================
                 */
                AlchemistCauldronMode.POTION -> {

                    val inv = blockEntity.inventory

                    /*
                     * 3～5スロット必要。
                     */
                    if (inv.slots !in 3..NostrumConfig.maxPotionUpgradeAmount + 2) {
                        potionFatal(
                            blockEntity,
                            level
                        )

                        return ItemInteractionResult.FAIL
                    }

                    /*
                     * 既にPotionDataがあるなら生成不可。
                     */
                    if (blockEntity.potionData != null) {
                        potionFatal(
                            blockEntity,
                            level
                        )

                        return ItemInteractionResult.FAIL
                    }

                    /*
                     * 基本材料。
                     */
                    val firstItem =
                        inv.getStackInSlot(0)

                    if (
                        firstItem.isEmpty ||
                        !firstItem.`is`(NostrumTags.BREWING_MATERIAL)
                    ) {
                        potionFatal(
                            blockEntity,
                            level
                        )

                        return ItemInteractionResult.FAIL
                    }

                    /*
                     * Potion ingredient。
                     */
                    val potionIngredient =
                        inv.getStackInSlot(1)

                    if (potionIngredient.isEmpty) {
                        potionFatal(
                            blockEntity,
                            level
                        )

                        return ItemInteractionResult.FAIL
                    }

                    /*
                     * 水量。
                     */
                    if (blockEntity.fluid.amount < 1000) {
                        potionFatal(
                            blockEntity,
                            level
                        )

                        return ItemInteractionResult.FAIL
                    }

                    val server =
                        level.server
                            ?: return ItemInteractionResult.FAIL

                    val potionBrewing =
                        server.potionBrewing()

                    /*
                     * 仮想AWKWARD Potion。
                     */
                    val awkwardPotion =
                        ItemStack(Items.POTION)

                    awkwardPotion.set(
                        DataComponents.POTION_CONTENTS,
                        PotionContents(Potions.AWKWARD)
                    )

                    /*
                     * Brewing recipe存在確認。
                     */
                    if (
                        !potionBrewing.hasMix(
                            awkwardPotion,
                            potionIngredient
                        )
                    ) {
                        potionFatal(
                            blockEntity,
                            level
                        )

                        return ItemInteractionResult.FAIL
                    }

                    /*
                     * 実際の醸造結果。
                     */
                    val brewed =
                        potionBrewing.mix(
                            potionIngredient,
                            awkwardPotion
                        )

                    val contents =
                        brewed.get(
                            DataComponents.POTION_CONTENTS
                        ) ?: return ItemInteractionResult.FAIL

                    val potion =
                        contents.potion.orElse(null)
                            ?: return ItemInteractionResult.FAIL

                    /*
                     * 効果をコピー。
                     */
                    val effects =
                        potion.value().effects.map { effect ->
                            MobEffectInstance(
                                effect.effect,
                                effect.duration,
                                effect.amplifier,
                                effect.isAmbient,
                                effect.isVisible,
                                effect.showIcon()
                            )
                        }.toMutableList()

                    var form =
                        AlchemicalPotionForm.DRINK

                    /*
                     * 強化素材 / 投擲形態などを確認。
                     *
                     * ここではまだ何も消費しない。
                     */
                    for (i in 2 until inv.slots) {

                        val item =
                            inv.getStackInSlot(i)

                        if (item.isEmpty) {
                            continue
                        }

                        when {

                            item.`is`(NostrumTags.STRONG_1) -> {

                                for (index in effects.indices) {

                                    val effect =
                                        effects[index]

                                    effects[index] =
                                        MobEffectInstance(
                                            effect.effect,
                                            (effect.duration * 0.75)
                                                .toInt()
                                                .coerceAtLeast(1),
                                            effect.amplifier + 1,
                                            effect.isAmbient,
                                            effect.isVisible,
                                            effect.showIcon()
                                        )
                                }
                            }

                            item.`is`(NostrumTags.SPLASH) -> {
                                form =
                                    AlchemicalPotionForm.SPLASH
                            }

                            item.`is`(NostrumTags.LINGERING) -> {
                                form =
                                    AlchemicalPotionForm.LINGERING
                            }

                            item.`is`(NostrumTags.AEROSOL) -> {
                                form =
                                    AlchemicalPotionForm.AEROSOL
                            }

                            item.`is`(NostrumTags.SPRAY) -> {
                                form =
                                    AlchemicalPotionForm.SPRAY
                            }

                            else -> {

                                potionFatal(
                                    blockEntity,
                                    level
                                )

                                return ItemInteractionResult.FAIL
                            }
                        }
                    }

                    /*
                     * ここまで来たら生成確定。
                     *
                     * 材料消費。
                     */
                    for (i in 0 until inv.slots) {
                        val item =
                            inv.getStackInSlot(i)

                        if (!item.isEmpty) {
                            item.shrink(1)
                        }
                    }

                    /*
                     * 水1000mB消費。
                     */
                    blockEntity.fluid.shrink(1000)

                    if (blockEntity.fluid.isEmpty) {
                        blockEntity.fluid =
                            FluidStack.EMPTY
                    }

                    /*
                     * PotionData生成。
                     */
                    blockEntity.potionData =
                        PotionData(
                            effects,
                            3,
                            form
                        )

                    inv.compact()
                    blockEntity.setChanged()

                    /*
                     * 成功したのでWand耐久消費。
                     */
                    stack.hurtAndBreak(
                        1,
                        player,
                        slot
                    )

                }

                /*
                 * =====================================================
                 * POTION MIXING
                 * =====================================================
                 */
                AlchemistCauldronMode.POTION_MIXING -> {

                    val potionData =
                        blockEntity.potionData

                    val potionData2 =
                        blockEntity.potionData2

                    /*
                     * 2つのPotionが揃っていなければ何もしない。
                     */
                    if (
                        potionData == null ||
                        potionData2 == null
                    ) {
                        return ItemInteractionResult.SUCCESS
                    }

                    val inv =
                        blockEntity.inventory

                    /*
                     * まず全素材を検証する。
                     *
                     * 以前のコードだと、
                     *
                     * A = MIXING_MATERIAL
                     * B = MIXING_MATERIALではない
                     *
                     * の場合、Aだけ先に消費されてBで失敗する。
                     */
                    var mixingMaterial = 0

                    for (i in 0 until inv.slots) {

                        val item =
                            inv.getStackInSlot(i)

                        if (item.isEmpty) {
                            continue
                        }

                        if (
                            !item.`is`(
                                NostrumTags.MIXING_MATERIAL
                            )
                        ) {
                            potionFatal(
                                blockEntity,
                                level
                            )

                            return ItemInteractionResult.FAIL
                        }

                        mixingMaterial += item.count
                    }

                    /*
                     * MIXING_MATERIALが1個もない場合。
                     */
                    if (mixingMaterial <= 0) {
                        return ItemInteractionResult.SUCCESS
                    }

                    /*
                     * 5% / 1個。
                     */
                    val percent =
                        (mixingMaterial * 5)
                            .coerceAtMost(100)

                    val success =
                        level.random.nextFloat() * 100.0f < percent

                    /*
                     * 成功 / 失敗に関係なく素材はここで消費。
                     */
                    for (i in 0 until inv.slots) {

                        val item =
                            inv.getStackInSlot(i)

                        if (!item.isEmpty) {
                            item.shrink(item.count)
                        }
                    }

                    if (!success) {

                        potionFatal(
                            blockEntity,
                            level
                        )

                        inv.compact()
                        blockEntity.setChanged()

                        /*
                         * 処理したのでWand耐久消費。
                         */
                        stack.hurtAndBreak(
                            1,
                            player,
                            slot
                        )

                        return ItemInteractionResult.SUCCESS
                    }

                    /*
                     * =================================================
                     * Potion合成
                     * =================================================
                     */

                    val newEffects =
                        mutableListOf<MobEffectInstance>()

                    /*
                     * Potion 1をコピー。
                     */
                    potionData.effects.forEach { effect ->

                        newEffects += MobEffectInstance(
                            effect.effect,
                            effect.duration,
                            effect.amplifier,
                            effect.isAmbient,
                            effect.isVisible,
                            effect.showIcon()
                        )
                    }

                    /*
                     * Potion 2を合成。
                     */
                    potionData2.effects.forEach { effect2 ->

                        val index =
                            newEffects.indexOfFirst {
                                it.effect == effect2.effect
                            }

                        if (index >= 0) {

                            val existing =
                                newEffects[index]

                            newEffects[index] =
                                MobEffectInstance(
                                    existing.effect,
                                    existing.duration +
                                            effect2.duration,
                                    existing.amplifier +
                                            effect2.amplifier +
                                            1,
                                    existing.isAmbient ||
                                            effect2.isAmbient,
                                    existing.isVisible ||
                                            effect2.isVisible,
                                    existing.showIcon() ||
                                            effect2.showIcon()
                                )
                            if (newEffects[index].amplifier > NostrumConfig.maxPotionLevel) {
                                newEffects[index] =
                                    MobEffectInstance(
                                        existing.effect,
                                        existing.duration +
                                                effect2.duration,
                                        existing.amplifier +
                                                effect2.amplifier +
                                                NostrumConfig.maxPotionLevel,
                                        existing.isAmbient ||
                                                effect2.isAmbient,
                                        existing.isVisible ||
                                                effect2.isVisible,
                                        existing.showIcon() ||
                                                effect2.showIcon()
                                    )
                            }

                        } else {

                            newEffects +=
                                MobEffectInstance(
                                    effect2.effect,
                                    effect2.duration,
                                    effect2.amplifier,
                                    effect2.isAmbient,
                                    effect2.isVisible,
                                    effect2.showIcon()
                                )
                        }
                    }

                    /*
                     * Potion 1側に合成結果を入れる。
                     */
                    blockEntity.potionData =
                        PotionData(
                            newEffects,
                            3,
                            potionData.form
                        )

                    blockEntity.potionData2 = null

                    inv.compact()
                    blockEntity.setChanged()

                    /*
                     * 成功したのでWand耐久消費。
                     */
                    stack.hurtAndBreak(
                        1,
                        player,
                        slot
                    )

                }

                /*
                 * =====================================================
                 * UPGRADE
                 * =====================================================
                 */
                AlchemistCauldronMode.UPGRADE -> {

                    val inventory =
                        blockEntity.inventory

                    val fluid =
                        blockEntity.fluid

                    /*
                     * Upgrade液体が入っていない。
                     */
                    if (
                        fluid.isEmpty ||
                        !fluid.has(
                            NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT
                        )
                    ) {
                        return ItemInteractionResult.SUCCESS
                    }

                    /*
                     * 対象アイテムは1個だけ。
                     */
                    if (inventory.slots != 1) {
                        return ItemInteractionResult.SUCCESS
                    }

                    val component =
                        fluid.get(
                            NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT
                        ) ?: return ItemInteractionResult.SUCCESS

                    val upgrade =
                        UpgradeManage.instance.get(component.id)
                            ?: return ItemInteractionResult.SUCCESS

                    val targetItem =
                        inventory.getStackInSlot(0)

                    if (targetItem.isEmpty) {
                        return ItemInteractionResult.SUCCESS
                    }

                    /*
                     * =================================================
                     * ★ 既にUpgradeが付いている場合は再適用不可
                     * =================================================
                     *
                     * 同じUpgradeだけでなく、
                     * 別Upgradeであっても1個付いていたら不可。
                     */
                    if (
                        targetItem.has(
                            NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT
                        )
                    ) {
                        return ItemInteractionResult.SUCCESS
                    }

                    /*
                     * 対象アイテム確認。
                     */
                    if (!targetItem.`is`(upgrade.target)) {
                        return ItemInteractionResult.SUCCESS
                    }

                    /*
                     * 液体量確認。
                     */
                    if (fluid.amount < upgrade.fluidAmount) {
                        return ItemInteractionResult.SUCCESS
                    }

                    /*
                     * Upgradeを適用。
                     */
                    applyUpgrade(
                        targetItem,
                        component.id
                    )

                    /*
                     * Upgrade IDを保存。
                     */
                    targetItem.set(
                        NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT,
                        AlchemicalUpgradeComponent(
                            component.id
                        )
                    )

                    inventory.setStackInSlot(
                        0,
                        targetItem
                    )

                    /*
                     * 液体消費。
                     */
                    fluid.shrink(
                        upgrade.fluidAmount
                    )

                    if (fluid.isEmpty) {
                        blockEntity.fluid =
                            FluidStack.EMPTY
                    }

                    blockEntity.setChanged()

                    /*
                     * 成功したのでWand耐久消費。
                     */
                    stack.hurtAndBreak(
                        1,
                        player,
                        slot
                    )

                }
            }

            blockEntity.inventory.compact()
            blockEntity.setChanged()

            return ItemInteractionResult.SUCCESS
        }

        /*
         * =============================================================
         * 通常アイテム
         * =============================================================
         */

        val handItem =
            player.getItemInHand(hand)

        if (handItem.isEmpty) {
            return super.useItemOn(
                stack,
                state,
                level,
                pos,
                player,
                hand,
                result
            )
        }

        /*
         * =============================================================
         * GLASS BOTTLE → Potion
         * =============================================================
         */
        if (handItem.`is`(Items.GLASS_BOTTLE)) {

            val data =
                blockEntity.potionData2
                    ?: blockEntity.potionData
                    ?: return ItemInteractionResult.FAIL

            val potion =
                ItemStack(
                    NostrumRegistries.ALCHEMICAL_POTION.get()
                )

            potion.set(
                DataComponents.POTION_CONTENTS,
                PotionContents(
                    Optional.empty(),
                    Optional.empty(),
                    data.effects
                )
            )

            potion.set(
                NostrumRegistries.ALCHEMICAL_POTION_CONTENT,
                AlchemicalPotionContent(
                    data.form
                )
            )

            /*
             * プレイヤーに渡す。
             *
             * addItemが失敗しても捨てない。
             */
            giveItemToPlayer(
                level,
                player,
                potion
            )

            /*
             * 取り出した分だけ減らす。
             */
            data.remaining--

            if (data.remaining <= 0) {

                if (blockEntity.potionData2 != null) {
                    blockEntity.potionData2 = null
                } else {
                    blockEntity.potionData = null
                }
            }

            handItem.shrink(1)

            blockEntity.setChanged()

            return ItemInteractionResult.SUCCESS
        }

        /*
         * =============================================================
         * Alchemical Potion → Cauldron
         * =============================================================
         */
        if (
            handItem.`is`(
                NostrumRegistries.ALCHEMICAL_POTION.get()
            )
        ) {

            val potions =
                handItem.get(
                    DataComponents.POTION_CONTENTS
                ) ?: return ItemInteractionResult.SUCCESS

            val content =
                handItem.get(
                    NostrumRegistries.ALCHEMICAL_POTION_CONTENT
                ) ?: return ItemInteractionResult.SUCCESS

            val potionData =
                PotionData(
                    potions.allEffects.toMutableList(),
                    1,
                    content.form
                )

            when {

                blockEntity.potionData == null -> {
                    blockEntity.potionData =
                        potionData
                }

                blockEntity.potionData!!.remaining in 1..2 &&
                        blockEntity.potionData!!.form ==
                        potionData.form -> {

                    blockEntity.potionData!!.remaining++
                }

                blockEntity.potionData2 == null -> {
                    blockEntity.potionData2 =
                        potionData
                }

                blockEntity.potionData2!!.remaining in 1..2 &&
                        blockEntity.potionData2!!.form ==
                        potionData.form -> {

                    blockEntity.potionData2!!.remaining++
                }

                else -> {
                    return ItemInteractionResult.FAIL
                }
            }

            /*
             * 空瓶を返す。
             */
            giveItemToPlayer(
                level,
                player,
                ItemStack(Items.GLASS_BOTTLE)
            )

            handItem.shrink(1)

            blockEntity.setChanged()

            return ItemInteractionResult.SUCCESS
        }

        /*
         * =============================================================
         * Fluid Handler
         * =============================================================
         */
        val handler =
            handItem.getCapability(
                Capabilities.FluidHandler.ITEM,
                null
            )

        if (handler != null) {

            val contained =
                handler.getFluidInTank(0)

            /*
             * =========================================================
             * Container → BlockEntity
             * =========================================================
             */
            if (!contained.isEmpty) {

                val current =
                    blockEntity.fluid

                val capacity = 2000

                /*
                 * 別Fluidは混ぜない。
                 *
                 * Componentまで含めて比較するので、
                 * Upgrade付きFluidと通常Fluidも別物として扱う。
                 */
                if (
                    !current.isEmpty &&
                    !FluidStack.isSameFluidSameComponents(
                        current,
                        contained
                    )
                ) {
                    return super.useItemOn(
                        stack,
                        state,
                        level,
                        pos,
                        player,
                        hand,
                        result
                    )
                }

                val remaining =
                    capacity - current.amount

                if (remaining <= 0) {
                    return super.useItemOn(
                        stack,
                        state,
                        level,
                        pos,
                        player,
                        hand,
                        result
                    )
                }

                val drainAmount =
                    minOf(
                        contained.amount,
                        remaining
                    )

                if (drainAmount <= 0) {
                    return super.useItemOn(
                        stack,
                        state,
                        level,
                        pos,
                        player,
                        hand,
                        result
                    )
                }

                /*
                 * 実際にDrain。
                 */
                val drained =
                    handler.drain(
                        drainAmount,
                        IFluidHandler.FluidAction.EXECUTE
                    )

                if (drained.isEmpty) {
                    return super.useItemOn(
                        stack,
                        state,
                        level,
                        pos,
                        player,
                        hand,
                        result
                    )
                }

                /*
                 * Container側のUpgrade ComponentをFluidへコピー。
                 */
                handItem.get(
                    NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT
                )?.let { component ->

                    drained.set(
                        NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT,
                        component
                    )
                }

                /*
                 * BEへ格納。
                 */
                if (current.isEmpty) {
                    blockEntity.fluid =
                        drained.copy()
                } else {
                    current.grow(
                        drained.amount
                    )
                }

                /*
                 * 空になったContainerへ交換。
                 */
                player.setItemInHand(
                    hand,
                    handler.container
                )

                blockEntity.setChanged()

                return ItemInteractionResult.SUCCESS
            }

            /*
             * =========================================================
             * BlockEntity → Container
             * =========================================================
             *
             * outputFluidを優先。
             */
            val useOutput =
                !blockEntity.outputFluid.isEmpty

            val stored =
                if (useOutput) {
                    blockEntity.outputFluid.copy()
                } else {
                    blockEntity.fluid.copy()
                }

            if (stored.isEmpty) {
                return super.useItemOn(
                    stack,
                    state,
                    level,
                    pos,
                    player,
                    hand,
                    result
                )
            }

            /*
             * 容器に入る量をSimulation。
             */
            val amount =
                handler.fill(
                    stored.copy(),
                    IFluidHandler.FluidAction.SIMULATE
                )

            if (amount <= 0) {
                return super.useItemOn(
                    stack,
                    state,
                    level,
                    pos,
                    player,
                    hand,
                    result
                )
            }

            val toFill =
                stored.copy()

            toFill.amount =
                amount

            /*
             * 実際にFill。
             */
            val filled =
                handler.fill(
                    toFill,
                    IFluidHandler.FluidAction.EXECUTE
                )

            if (filled <= 0) {
                return super.useItemOn(
                    stack,
                    state,
                    level,
                    pos,
                    player,
                    hand,
                    result
                )
            }

            /*
             * FluidのComponentをContainerへコピー。
             */
            val container =
                handler.container

            stored.get(
                NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT
            )?.let { component ->

                container.set(
                    NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT,
                    component
                )
            }

            /*
             * BE側からFluidを消費。
             */
            if (useOutput) {

                blockEntity.outputFluid.shrink(
                    filled
                )

                if (blockEntity.outputFluid.isEmpty) {
                    blockEntity.outputFluid =
                        FluidStack.EMPTY
                }

            } else {

                blockEntity.fluid.shrink(
                    filled
                )

                if (blockEntity.fluid.isEmpty) {
                    blockEntity.fluid =
                        FluidStack.EMPTY
                }
            }

            player.setItemInHand(
                hand,
                container
            )

            blockEntity.setChanged()

            return ItemInteractionResult.SUCCESS
        }

        /*
         * =============================================================
         * 通常アイテムを投入
         * =============================================================
         */

        val inserted =
            handItem.copyWithCount(1)

        blockEntity.inventory.resize(
            blockEntity.inventory.slots + 1
        )

        blockEntity.inventory.setStackInSlot(
            blockEntity.inventory.slots - 1,
            inserted
        )

        handItem.shrink(1)

        blockEntity.setChanged()

        return ItemInteractionResult.SUCCESS
    }

    /*
     * =============================================================
     * Client Particle
     * =============================================================
     */
    fun clientParticle(
        level: Level,
        pos: BlockPos,
        result: Boolean
    ) {

        if (result) {
            /*
             * 成功エフェクトをここに追加。
             */
        } else {

            level.addParticle(
                ParticleTypes.EXPLOSION,
                pos.x + 0.5,
                pos.y + 1.0,
                pos.z + 0.5,
                0.0,
                0.0,
                0.0
            )
        }
    }

    /*
     * =============================================================
     * Give Item Safely
     * =============================================================
     */
    private fun giveItemToPlayer(
        level: Level,
        player: Player,
        stack: ItemStack
    ) {
        if (stack.isEmpty) {
            return
        }

        if (!player.addItem(stack)) {

            val entity = ItemEntity(
                level,
                player.x,
                player.y + 0.5,
                player.z,
                stack
            )

            entity.setDefaultPickUpDelay()

            level.addFreshEntity(entity)
        }
    }

    /*
     * =============================================================
     * Apply Upgrade
     * =============================================================
     */
    fun applyUpgrade(
        stack: ItemStack,
        id: ResourceLocation
    ) {

        /*
         * 既にUpgradeが付いているなら絶対に再適用しない。
         */
        if (
            stack.has(
                NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT
            )
        ) {
            return
        }

        val upgrade =
            UpgradeManage.instance.get(id)
                ?: run {
                    Nostrum.LOGGER.warn(
                        "Unknown alchemical upgrade: {}",
                        id
                    )

                    return
                }

        /*
        for (attribute in upgrade.attributes) {

            applyAttribute(
                stack,
                id,
                attribute
            )
        }

         */
    }

    /*
     * =============================================================
     * Apply Attribute
     * =============================================================
     */
    fun applyAttribute(
        stack: ItemStack,
        upgradeId: ResourceLocation,
        data: AttributeData
    ) {

        val attributeKey =
            data.attribute.key
                ?: error("Attribute has no registry key")

        val modifierId =
            ResourceLocation.fromNamespaceAndPath(
                "nostrum",
                "alchemical/${upgradeId.namespace}/${upgradeId.path}/${attributeKey.location().path}"
            )

        val modifier =
            AttributeModifier(
                modifierId,
                data.amount,
                data.operation
            )

        val current =
            stack.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY
            )

        val modifiers =
            current.modifiers()
                .filterNot { entry ->
                    entry.modifier().id == modifierId
                }
                .toMutableList()

        modifiers += ItemAttributeModifiers.Entry(
            data.attribute,
            modifier,
            data.equipmentSlot
        )

        stack.set(
            DataComponents.ATTRIBUTE_MODIFIERS,
            ItemAttributeModifiers(
                modifiers,
                true
            )
        )
    }

    /*
     * =============================================================
     * Potion Fatal
     * =============================================================
     */
    fun potionFatal(
        be: AlchemistCauldronBlockEntity,
        level: Level
    ) {

        be.potionData = null
        be.potionData2 = null

        be.setChanged()
    }

    companion object {

        private val SHAPE =
            Shapes.or(

                /*
                 * 底
                 */
                box(
                    0.0,
                    0.0,
                    0.0,
                    16.0,
                    3.0,
                    16.0
                ),

                /*
                 * 下段
                 */
                box(
                    0.0,
                    3.0,
                    0.0,
                    3.0,
                    6.0,
                    16.0
                ),

                box(
                    13.0,
                    3.0,
                    0.0,
                    16.0,
                    6.0,
                    16.0
                ),

                box(
                    3.0,
                    3.0,
                    0.0,
                    13.0,
                    6.0,
                    3.0
                ),

                box(
                    3.0,
                    3.0,
                    13.0,
                    13.0,
                    6.0,
                    16.0
                ),

                /*
                 * 上端の厚み
                 */
                box(
                    0.0,
                    6.0,
                    0.0,
                    2.0,
                    15.0,
                    16.0
                ),

                box(
                    14.0,
                    6.0,
                    0.0,
                    16.0,
                    15.0,
                    16.0
                ),

                box(
                    2.0,
                    6.0,
                    0.0,
                    14.0,
                    15.0,
                    2.0
                ),

                box(
                    2.0,
                    6.0,
                    14.0,
                    14.0,
                    15.0,
                    16.0
                )
            )
    }

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return SHAPE
    }
}