package io.github.riiimc.nostrum.content.blocks

import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.content.blockentities.AlchemistCauldronBlockEntity
import io.github.riiimc.nostrum.content.blockentities.PotionData
import io.github.riiimc.nostrum.content.components.AlchemicalPotionContent
import io.github.riiimc.nostrum.content.components.AlchemicalPotionForm
import io.github.riiimc.nostrum.content.recipes.AlchemistCauldronMode
import io.github.riiimc.nostrum.utils.NostrumTags
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
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
import java.util.Optional

class AlchemistCauldronBlock(properties: Properties): Block(properties), EntityBlock {
    override fun newBlockEntity(p0: BlockPos, p1: BlockState): BlockEntity {
        return AlchemistCauldronBlockEntity(p0, p1)
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return super.useWithoutItem(state, level, pos, player, hit)
        if (!player.isShiftKeyDown) return super.useWithoutItem(state, level, pos, player, hit)
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity !is AlchemistCauldronBlockEntity) return super.useWithoutItem(state, level, pos, player, hit)
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) return super.useWithoutItem(state, level, pos, player, hit)

        if (blockEntity.inventory.slots == 0) return super.useWithoutItem(state, level, pos, player, hit)
        player.setItemInHand(InteractionHand.MAIN_HAND, blockEntity.inventory.getStackInSlot(blockEntity.inventory.slots - 1))
        blockEntity.inventory.setStackInSlot(blockEntity.inventory.slots - 1, ItemStack.EMPTY)
        blockEntity.inventory.resize(blockEntity.inventory.slots - 1)
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
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity !is AlchemistCauldronBlockEntity) return super.useItemOn(stack, state, level, pos, player, hand, result)
        when (stack.item) {
            NostrumRegistries.ALCHEMIST_WAND.get() -> {
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


                if (level.isClientSide) {
                    return super.useItemOn(stack, state, level, pos, player, hand, result)
                }
                val slot = when (hand) {
                    InteractionHand.MAIN_HAND -> EquipmentSlot.MAINHAND
                    InteractionHand.OFF_HAND -> EquipmentSlot.OFFHAND
                }

                stack.hurtAndBreak(1, player, slot)

                when (blockEntity.mode) {
                    AlchemistCauldronMode.ALCHEMY -> {
                        val recipe = blockEntity.checkRecipe()

                        if (level.isClientSide) {
                            clientParticle(level, pos, recipe != null)
                            return ItemInteractionResult.SUCCESS
                        }

                        if (recipe == null) {
                            for (i in 0 until blockEntity.inventory.slots) {
                                blockEntity.inventory
                                    .getStackInSlot(i)
                                    .shrink(1)
                            }
                            blockEntity.fluid = FluidStack.EMPTY
                            blockEntity.inventory.compact()
                            blockEntity.setChanged()
                            return ItemInteractionResult.SUCCESS
                        }

                        val input = blockEntity.createRecipeInput()

                        val resultStack = recipe.assemble(
                            input,
                            level.registryAccess()
                        )

                        // 材料を消費
                        for (i in 0 until blockEntity.inventory.slots) {
                            blockEntity.inventory
                                .getStackInSlot(i)
                                .shrink(1)
                        }

                        // 液体を消費
                        blockEntity.fluid.shrink(recipe.inputFluid.amount)

                        if (blockEntity.fluid.isEmpty) {
                            blockEntity.fluid = FluidStack.EMPTY
                        }

                        // 結果をプレイヤーへ
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
                    AlchemistCauldronMode.POTION -> {
                        if (level.isClientSide) {
                            return ItemInteractionResult.SUCCESS
                        }

                        val inv = blockEntity.inventory

                        if (inv.slots !in 3..5 || blockEntity.potionData != null) {
                            potionFatal()
                            return ItemInteractionResult.FAIL
                        }

                        val fluid = blockEntity.fluid
                        val firstItem = inv.getStackInSlot(0)
                        val potionIngredient = inv.getStackInSlot(1)

                        if (!firstItem.`is`(NostrumTags.BREWING_MATERIAL)) {
                            potionFatal()
                            return ItemInteractionResult.FAIL
                        }

                        if (fluid.amount < 1000) {
                            potionFatal()
                            return ItemInteractionResult.FAIL
                        }

                        val server = level.server
                            ?: return ItemInteractionResult.FAIL

                        val potionBrewing = server.potionBrewing()

                        // 仮想的なAWKWARD Potion
                        val awkwardPotion = ItemStack(Items.POTION)
                        awkwardPotion.set(
                            DataComponents.POTION_CONTENTS,
                            PotionContents(Potions.AWKWARD)
                        )

                        // AWKWARD + 材料
                        if (!potionBrewing.hasMix(awkwardPotion, potionIngredient)) {
                            potionFatal()
                            return ItemInteractionResult.FAIL
                        }

                        // 実際の醸造結果
                        val result = potionBrewing.mix(
                            potionIngredient,
                            awkwardPotion
                        )

                        val contents = result.get(DataComponents.POTION_CONTENTS)
                            ?: return ItemInteractionResult.FAIL

                        val potion = contents.potion
                            .orElse(null)
                            ?: return ItemInteractionResult.FAIL

                        val effects = potion.value().effects.toMutableList()
                        var form: AlchemicalPotionForm = AlchemicalPotionForm.DRINK

                        // 強化素材
                        for (i in 2 until inv.slots) {
                            val item = inv.getStackInSlot(i)

                            if (item.isEmpty) continue

                            when {
                                item.`is`(NostrumTags.STRONG_1) -> {
                                    for (index in effects.indices) {
                                        val effect = effects[index]

                                        effects[index] = MobEffectInstance(
                                            effect.effect,
                                            (effect.duration * 0.75).toInt(),
                                            effect.amplifier + 1,
                                            effect.isAmbient,
                                            effect.isVisible,
                                            effect.showIcon()
                                        )
                                    }
                                }

                                item.`is`(NostrumTags.SPLASH) -> {
                                    form = AlchemicalPotionForm.SPLASH
                                }

                                item.`is`(NostrumTags.LINGERING) -> {
                                    form = AlchemicalPotionForm.LINGERING
                                }
                                item.`is`(NostrumTags.AEROSOL) -> {
                                    form = AlchemicalPotionForm.AEROSOL
                                }
                                item.`is`(NostrumTags.SPRAY) -> {
                                    form = AlchemicalPotionForm.SPRAY
                                }

                                else -> {
                                    potionFatal()
                                    return ItemInteractionResult.FAIL
                                }
                            }
                        }

                        // 材料消費
                        for (i in 0 until inv.slots) {
                            inv.getStackInSlot(i).shrink(1)
                        }

                        // 水1000mB消費
                        blockEntity.fluid.shrink(1000)

                        if (blockEntity.fluid.isEmpty) {
                            blockEntity.fluid = FluidStack.EMPTY
                        }

                        // ポーション生成
                        blockEntity.potionData = PotionData(
                            effects,
                            3,
                            form
                        )

                        inv.compact()
                        blockEntity.setChanged()

                        return ItemInteractionResult.SUCCESS
                    }

                    AlchemistCauldronMode.POTION_MIXING -> {
                        var potionData = blockEntity.potionData
                        val potionData2 = blockEntity.potionData2

                        if (potionData == null || potionData2 == null) return ItemInteractionResult.SUCCESS
                        var mixingMaterial = 0
                        for (i in 0 until blockEntity.inventory.slots) {
                            val item = blockEntity.inventory.getStackInSlot(i)
                            if (item.`is`(NostrumTags.MIXING_MATERIAL)) {
                                mixingMaterial++
                                blockEntity.inventory.setStackInSlot(i, ItemStack.EMPTY)
                            }
                            else {
                                blockEntity.inventory.compact()
                                blockEntity.setChanged()
                                potionFatal()
                                return ItemInteractionResult.SUCCESS
                            }
                        }
                        var percent = mixingMaterial * 5
                        if (percent > 100) percent = 100

                        val success: Boolean = Math.random() * 100 < percent.coerceIn(0, 100)
                        if (success) {
                            val newEffects = mutableListOf<MobEffectInstance>()

                            // potionData 側をコピー
                            potionData.effects.forEach { effect ->
                                newEffects.add(
                                    MobEffectInstance(
                                        effect.effect,
                                        effect.duration,
                                        effect.amplifier,
                                        effect.isAmbient,
                                        effect.isVisible,
                                        effect.showIcon()
                                    )
                                )
                            }

                            // potionData2 側を合成
                            potionData2.effects.forEach { effect2 ->
                                val existing = newEffects.find {
                                    it.effect == effect2.effect
                                }

                                if (existing != null) {
                                    // 同じ効果なら時間とレベルを加算
                                    val index = newEffects.indexOf(existing)

                                    newEffects[index] = MobEffectInstance(
                                        existing.effect,
                                        existing.duration + effect2.duration,
                                        existing.amplifier + effect2.amplifier + 1,
                                        existing.isAmbient || effect2.isAmbient,
                                        existing.isVisible || effect2.isVisible,
                                        existing.showIcon() || effect2.showIcon()
                                    )
                                } else {
                                    // 違う効果ならそのまま追加
                                    newEffects.add(
                                        MobEffectInstance(
                                            effect2.effect,
                                            effect2.duration,
                                            effect2.amplifier,
                                            effect2.isAmbient,
                                            effect2.isVisible,
                                            effect2.showIcon()
                                        )
                                    )
                                }
                            }
                            blockEntity.potionData = PotionData(newEffects, 3, potionData.form)
                            blockEntity.potionData2 = null

                            blockEntity.inventory.compact()
                            blockEntity.setChanged()
                            return ItemInteractionResult.SUCCESS
                        }
                        else {
                            potionFatal()
                            blockEntity.inventory.compact()
                            blockEntity.setChanged()
                            return ItemInteractionResult.SUCCESS
                        }
                    }
                }

                blockEntity.inventory.compact()
                blockEntity.setChanged()

                return ItemInteractionResult.SUCCESS
            }
            else -> {
                val handItem = player.getItemInHand(hand)
                if (handItem.isEmpty) return super.useItemOn(stack, state, level, pos, player, hand, result)
                if (handItem.`is`(Items.GLASS_BOTTLE)) {
                    if (level.isClientSide) {
                        return ItemInteractionResult.SUCCESS
                    }
                    val potion = ItemStack(NostrumRegistries.ALCHEMICAL_POTION.get())
                    if (blockEntity.potionData2 != null) {
                        val data = blockEntity.potionData2
                            ?: return ItemInteractionResult.FAIL

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

                        data.remaining--

                        if (data.remaining <= 0) {
                            blockEntity.potionData2 = null
                        }
                    }
                    else if (blockEntity.potionData != null) {
                        val data = blockEntity.potionData
                            ?: return ItemInteractionResult.FAIL

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

                        data.remaining--

                        if (data.remaining <= 0) {
                            blockEntity.potionData = null
                        }

                    }
                    else {
                        return ItemInteractionResult.FAIL
                    }

                    blockEntity.setChanged()
                    player.addItem(potion)
                    handItem.shrink(1)
                    return ItemInteractionResult.SUCCESS
                }
                val handler = handItem.getCapability(
                    Capabilities.FluidHandler.ITEM,
                    null
                )
                if (handItem.`is`(NostrumRegistries.ALCHEMICAL_POTION.get())) {
                    val potions = handItem.get(
                        DataComponents.POTION_CONTENTS
                    ) ?: return ItemInteractionResult.SUCCESS

                    val content = handItem.get(
                        NostrumRegistries.ALCHEMICAL_POTION_CONTENT
                    ) ?: return ItemInteractionResult.SUCCESS

                    val potionData = PotionData(
                        potions.allEffects.toMutableList(),
                        1,
                        content.form
                    )

                    if (blockEntity.potionData == null) {
                        blockEntity.potionData = potionData
                    } else if (blockEntity.potionData!!.remaining in 1..2 && blockEntity.potionData!!.form == potionData.form) {
                        blockEntity.potionData!!.remaining++
                    } else if (blockEntity.potionData2 == null) {
                        blockEntity.potionData2 = potionData
                    } else if (blockEntity.potionData2!!.remaining in 1..2 && blockEntity.potionData2!!.form == potionData.form) {
                        blockEntity.potionData2!!.remaining++
                    }
                    else {
                        return ItemInteractionResult.FAIL
                    }
                    blockEntity.setChanged()
                    player.addItem(ItemStack(Items.GLASS_BOTTLE))
                    player.getItemInHand(hand).shrink(1)
                    return ItemInteractionResult.SUCCESS
                }
                if (handler != null) {

                    val contained = handler.getFluidInTank(0)

                    if (contained.isEmpty) {
                        // ItemStack が空 → BlockEntity から回収
                        val stored = blockEntity.fluid

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

                        val amount = handler.fill(
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

                        val toFill = stored.copy()
                        toFill.amount = amount

                        val filled = handler.fill(
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

                        stored.shrink(filled)

                        if (stored.amount <= 0) {
                            blockEntity.fluid = FluidStack.EMPTY
                        }

                        player.setItemInHand(hand, handler.container)
                        blockEntity.setChanged()

                        return ItemInteractionResult.SUCCESS
                    } else {
                        // ItemStack に Fluid が入っている → BlockEntity へ注入
                        val capacity = 2000
                        val current = blockEntity.fluid

                        if (!current.isEmpty &&
                            !FluidStack.isSameFluidSameComponents(current, contained)
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

                        val remaining = capacity - current.amount

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

                        val drainAmount = minOf(contained.amount, remaining)

                        val drained = handler.drain(
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

                        if (current.isEmpty) {
                            blockEntity.fluid = drained.copy()
                        } else {
                            current.grow(drained.amount)
                        }

                        player.setItemInHand(hand, handler.container)
                        blockEntity.setChanged()

                        return ItemInteractionResult.SUCCESS
                    }
                }
                else {
                    val inserted = handItem.copyWithCount(1)

                    blockEntity.inventory.resize(blockEntity.inventory.slots + 1)
                    blockEntity.inventory.setStackInSlot(
                        blockEntity.inventory.slots - 1,
                        inserted
                    )

                    handItem.shrink(1)

                    blockEntity.setChanged()
                }
                return ItemInteractionResult.SUCCESS
            }

        }

    }

    fun clientParticle(level: Level, pos: BlockPos, result: Boolean) {

        if (result) {

        }
        else {
            level.addParticle(
                ParticleTypes.EXPLOSION,
                pos.x + 0.5,
                pos.y + 1.0,
                pos.z + 0.5,
                0.0,
                0.0,
                0.0
            )}
    }
    companion object {
        private val SHAPE = Shapes.or(
            // 底
            box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0),

            // 下段
            box(0.0, 3.0, 0.0, 3.0, 6.0, 16.0),
            box(13.0, 3.0, 0.0, 16.0, 6.0, 16.0),
            box(3.0, 3.0, 0.0, 13.0, 6.0, 3.0),
            box(3.0, 3.0, 13.0, 13.0, 6.0, 16.0),

            // 上端の厚み
            box(0.0, 6.0, 0.0, 2.0, 15.0, 16.0),
            box(14.0, 6.0, 0.0, 16.0, 15.0, 16.0),
            box(2.0, 6.0, 0.0, 14.0, 15.0, 2.0),
            box(2.0, 6.0, 14.0, 14.0, 15.0, 16.0)
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

    fun potionFatal() {

    }
}