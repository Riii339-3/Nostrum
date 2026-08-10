package io.github.riiimc.nostrum.content.items

import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.content.components.AlchemicalPotionForm
import io.github.riiimc.nostrum.content.entities.ThrownAlchemicalPotion
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.AreaEffectCloud
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.ThrowablePotionItem
import net.minecraft.world.item.UseAnim
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.AABB
import java.util.function.Consumer

class AlchemicalPotionItem(
    properties: Properties
) : ThrowablePotionItem(properties) {

    override fun use(
        level: Level,
        player: Player,
        hand: InteractionHand
    ): InteractionResultHolder<ItemStack> {

        val stack = player.getItemInHand(hand)

        val content = stack.get(
            NostrumRegistries.ALCHEMICAL_POTION_CONTENT
        ) ?: return InteractionResultHolder.pass(stack)

        when (content.form) {

            // =========================
            // 飲むタイプ
            // =========================
            AlchemicalPotionForm.DRINK,
            AlchemicalPotionForm.AEROSOL,
            AlchemicalPotionForm.SPRAY -> {

                player.startUsingItem(hand)

                return InteractionResultHolder.consume(stack)
            }

            // =========================
            // 投げるタイプ
            // =========================
            AlchemicalPotionForm.SPLASH,
            AlchemicalPotionForm.LINGERING -> {

                if (!level.isClientSide) {

                    val sound = when (content.form) {
                        AlchemicalPotionForm.SPLASH ->
                            SoundEvents.SPLASH_POTION_THROW

                        AlchemicalPotionForm.LINGERING ->
                            SoundEvents.LINGERING_POTION_THROW

                        else -> return InteractionResultHolder.pass(stack)
                    }

                    level.playSound(
                        null,
                        player.x,
                        player.y,
                        player.z,
                        sound,
                        SoundSource.PLAYERS,
                        0.5f,
                        0.4f / (
                                level.random.nextFloat() * 0.4f + 0.8f
                                )
                    )

                    return throwPotion(
                        level,
                        player,
                        hand
                    )
                }

                return InteractionResultHolder.sidedSuccess(
                    stack,
                    true
                )
            }

            else -> {
                return InteractionResultHolder.pass(stack)
            }
        }
    }

    // =========================================================
    // 使用完了
    // =========================================================

    override fun finishUsingItem(
        stack: ItemStack,
        level: Level,
        entity: LivingEntity
    ): ItemStack {

        val content = stack.get(
            NostrumRegistries.ALCHEMICAL_POTION_CONTENT
        ) ?: return super.finishUsingItem(
            stack,
            level,
            entity
        )

        when (content.form) {

            // =================================================
            // DRINK
            // =================================================

            AlchemicalPotionForm.DRINK -> {

                if (!level.isClientSide) {

                    val potionContents = stack.getOrDefault(
                        DataComponents.POTION_CONTENTS,
                        PotionContents.EMPTY
                    )

                    potionContents.forEachEffect { effect ->

                        val effectInstance =
                            MobEffectInstance(effect)

                        val mobEffect =
                            effectInstance.effect.value()

                        if (mobEffect.isInstantenous) {

                            mobEffect.applyInstantenousEffect(
                                entity,
                                entity,
                                entity,
                                effectInstance.amplifier,
                                1.0
                            )

                        } else {

                            entity.addEffect(
                                effectInstance
                            )
                        }
                    }

                    if (entity is ServerPlayer) {

                        CriteriaTriggers.CONSUME_ITEM.trigger(
                            entity,
                            stack
                        )

                        entity.awardStat(
                            Stats.ITEM_USED.get(this)
                        )
                    }

                    stack.consume(
                        1,
                        entity
                    )

                    entity.gameEvent(
                        GameEvent.DRINK
                    )
                }

                if (stack.isEmpty) {
                    return ItemStack(Items.GLASS_BOTTLE)
                }

                return stack
            }

            // =================================================
            // AEROSOL
            // =================================================

            AlchemicalPotionForm.AEROSOL -> {

                if (!level.isClientSide) {

                    val contents = stack.getOrDefault(
                        DataComponents.POTION_CONTENTS,
                        PotionContents.EMPTY
                    )

                    val cloud = AreaEffectCloud(
                        level,
                        entity.x,
                        entity.y,
                        entity.z
                    )

                    cloud.radius = 3.0f
                    cloud.duration = 300
                    cloud.waitTime = 0

                    cloud.radiusOnUse = -0.5f
                    cloud.radiusPerTick = -0.005f

                    contents.forEachEffect { effect ->

                        cloud.addEffect(
                            MobEffectInstance(effect)
                        )
                    }

                    level.addFreshEntity(
                        cloud
                    )

                    if (entity is ServerPlayer) {

                        CriteriaTriggers.CONSUME_ITEM.trigger(
                            entity,
                            stack
                        )

                        entity.awardStat(
                            Stats.ITEM_USED.get(this)
                        )
                    }

                    entity.gameEvent(
                        GameEvent.DRINK
                    )

                    stack.consume(
                        1,
                        entity
                    )
                }

                if (stack.isEmpty) {
                    return ItemStack(Items.GLASS_BOTTLE)
                }

                return stack
            }

            // =================================================
            // SPRAY
            // =================================================

            AlchemicalPotionForm.SPRAY -> {

                // アイテム消費
                if (!level.isClientSide) {

                    if (entity is ServerPlayer) {

                        CriteriaTriggers.CONSUME_ITEM.trigger(
                            entity,
                            stack
                        )

                        entity.awardStat(
                            Stats.ITEM_USED.get(this)
                        )
                    }

                    stack.consume(
                        1,
                        entity
                    )

                    entity.gameEvent(
                        GameEvent.DRINK
                    )
                }

                if (stack.isEmpty) {
                    return ItemStack(Items.GLASS_BOTTLE)
                }

                return stack
            }

            else -> {
                return super.finishUsingItem(
                    stack,
                    level,
                    entity
                )
            }
        }
    }

    // =========================================================
    // 使用時間
    // =========================================================

    override fun getUseDuration(
        stack: ItemStack,
        entity: LivingEntity
    ): Int {
        return 32
    }

    // =========================================================
    // アニメーション
    // =========================================================

    override fun getUseAnimation(
        stack: ItemStack
    ): UseAnim {

        val content = stack.get(
            NostrumRegistries.ALCHEMICAL_POTION_CONTENT
        ) ?: return super.getUseAnimation(stack)

        return when (content.form) {

            AlchemicalPotionForm.DRINK ->
                UseAnim.DRINK

            AlchemicalPotionForm.AEROSOL ->
                UseAnim.DRINK

            AlchemicalPotionForm.SPRAY ->
                UseAnim.NONE

            else ->
                UseAnim.NONE
        }
    }

    // =========================================================
    // 使用中
    // =========================================================

    override fun onUseTick(
        level: Level,
        livingEntity: LivingEntity,
        stack: ItemStack,
        remainingUseDuration: Int
    ) {

        val content = stack.get(
            NostrumRegistries.ALCHEMICAL_POTION_CONTENT
        ) ?: return

        when (content.form) {


            // =================================================
            // SPRAY
            // =================================================

            AlchemicalPotionForm.SPRAY -> {

                val look =
                    livingEntity.lookAngle

                // -------------------------
                // CLIENT
                // -------------------------

                if (level.isClientSide) {

                    val distance = 1.5

                    val pos =
                        livingEntity.eyePosition.add(
                            look.scale(distance)
                        )

                    repeat(3) {

                        val spread = 0.2

                        val x =
                            pos.x +
                                    (
                                            level.random.nextDouble()
                                                    - 0.5
                                            ) * spread

                        val y =
                            pos.y +
                                    (
                                            level.random.nextDouble()
                                                    - 0.5
                                            ) * spread

                        val z =
                            pos.z +
                                    (
                                            level.random.nextDouble()
                                                    - 0.5
                                            ) * spread

                        level.addParticle(
                            ParticleTypes.CLOUD,
                            x,
                            y,
                            z,
                            look.x * 0.08,
                            look.y * 0.08,
                            look.z * 0.08
                        )
                    }

                    return
                }

                // -------------------------
                // SERVER
                // -------------------------

                if (remainingUseDuration % 5 != 0) {
                    return
                }

                val distance = 3.0

                val center =
                    livingEntity.eyePosition.add(
                        look.scale(distance)
                    )

                val box = AABB(
                    center.x - 1.0,
                    center.y - 1.0,
                    center.z - 1.0,
                    center.x + 1.0,
                    center.y + 1.0,
                    center.z + 1.0
                )

                val targets =
                    level.getEntitiesOfClass(
                        LivingEntity::class.java,
                        box
                    ) { target ->

                        target !== livingEntity &&
                                target.isAlive
                    }

                val potionContents =
                    stack.getOrDefault(
                        DataComponents.POTION_CONTENTS,
                        PotionContents.EMPTY
                    )

                for (target in targets) {

                    val toTarget =
                        target.eyePosition
                            .subtract(
                                livingEntity.eyePosition
                            )

                    if (toTarget.lengthSqr() == 0.0) {
                        continue
                    }

                    val direction =
                        toTarget.normalize()

                    val dot =
                        direction.dot(look)

                    // 前方だけ
                    if (dot < 0.3) {
                        continue
                    }

                    potionContents.forEachEffect { effect ->

                        target.addEffect(
                            MobEffectInstance(effect)
                        )
                    }
                }
            }

            else -> {
                // DRINKなどは何もしない
            }
        }
    }

    // =========================================================
    // アイテム名
    // =========================================================

    override fun getName(
        stack: ItemStack
    ): Component {

        val content = stack.get(
            NostrumRegistries.ALCHEMICAL_POTION_CONTENT
        ) ?: return super.getName(stack)

        return Component.translatable(
            "item.nostrum.alchemical_potion",
            Component.translatable(
                "item.nostrum.alchemical_potion.form.${
                    content.form.name.lowercase()
                }"
            )
        )
    }

    override fun useOn(
        context: UseOnContext
    ): InteractionResult {
        return InteractionResult.PASS
    }

    override fun releaseUsing(
        stack: ItemStack,
        level: Level,
        entity: LivingEntity,
        timeCharged: Int
    ) {
        val content = stack.get(
            NostrumRegistries.ALCHEMICAL_POTION_CONTENT
        ) ?: return

        if (content.form != AlchemicalPotionForm.SPRAY) {
            return
        }

        if (!level.isClientSide) {
            if (entity is ServerPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger(
                    entity,
                    stack
                )

                entity.awardStat(
                    Stats.ITEM_USED.get(this)
                )
            }

            stack.consume(
                1,
                entity
            )

            entity.gameEvent(
                GameEvent.DRINK
            )
        }
    }
    // =========================================================
    // 投擲
    // =========================================================

    private fun throwPotion(
        level: Level,
        player: Player,
        hand: InteractionHand
    ): InteractionResultHolder<ItemStack> {

        val stack =
            player.getItemInHand(hand)

        val projectile =
            ThrownAlchemicalPotion(
                level,
                player,
                stack.copy()
            )

        projectile.shootFromRotation(
            player,
            player.xRot,
            player.yRot,
            0.0f,
            0.5f,
            1.0f
        )

        level.addFreshEntity(
            projectile
        )

        if (!player.isCreative) {
            stack.shrink(1)
        }

        return InteractionResultHolder.sidedSuccess(
            stack,
            false
        )
    }
}