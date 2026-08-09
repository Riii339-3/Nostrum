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
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import java.util.function.Consumer

class AlchemicalPotionItem(properties: Properties): ThrowablePotionItem(properties) {

    override fun use(
        level: Level,
        player: Player,
        hand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)

        val content = stack.get(
            NostrumRegistries.ALCHEMICAL_POTION_CONTENT
        ) ?: return InteractionResultHolder.pass(stack)

        if (!level.isClientSide) {
            when (content.form) {
                AlchemicalPotionForm.SPLASH -> {
                    level.playSound(
                        null,
                        player.x,
                        player.y,
                        player.z,
                        SoundEvents.SPLASH_POTION_THROW,
                        SoundSource.PLAYERS,
                        0.5f,
                        0.4f / (level.random.nextFloat() * 0.4f + 0.8f)
                    )
                }

                AlchemicalPotionForm.LINGERING -> {
                    level.playSound(
                        null,
                        player.x,
                        player.y,
                        player.z,
                        SoundEvents.LINGERING_POTION_THROW,
                        SoundSource.NEUTRAL,
                        0.5f,
                        0.4f / (level.random.nextFloat() * 0.4f + 0.8f)
                    )
                }
                AlchemicalPotionForm.DRINK -> {
                    player.startUsingItem(hand)

                    return InteractionResultHolder.consume(stack)
                }

                AlchemicalPotionForm.AEROSOL -> {
                    player.startUsingItem(hand)

                    return InteractionResultHolder.consume(stack)
                }

                else -> return InteractionResultHolder.pass(stack)
            }
            if (!player.isCreative) {
                player.getItemInHand(hand).shrink(1)
            }

            return throwPotion(level, player, hand)
        }
        else {
            when (content.form) {
                AlchemicalPotionForm.DRINK -> {
                    player.startUsingItem(hand)

                    return InteractionResultHolder.consume(stack)

                }

                AlchemicalPotionForm.AEROSOL -> {
                    player.startUsingItem(hand)

                    return InteractionResultHolder.consume(stack)

                }

                else -> return InteractionResultHolder.sidedSuccess(
                    stack,
                    true
                )
            }
        }
    }

    override fun finishUsingItem(stack: ItemStack, level: Level, entity: LivingEntity): ItemStack {
        if (!stack.has(NostrumRegistries.ALCHEMICAL_POTION_CONTENT)) return super.finishUsingItem(stack, level, entity)
        if (level.isClientSide) {

        }
        else {
            val content = stack.get(NostrumRegistries.ALCHEMICAL_POTION_CONTENT) ?: return super.finishUsingItem(stack, level, entity)
            when (content.form) {
                AlchemicalPotionForm.DRINK -> {
                    val player = entity as? Player
                    if (player is ServerPlayer) {
                        CriteriaTriggers.CONSUME_ITEM.trigger(player, stack)
                    }

                        val potionContents = stack.getOrDefault<PotionContents?>(
                            DataComponents.POTION_CONTENTS,
                            PotionContents.EMPTY
                        ) as PotionContents
                        potionContents.forEachEffect(Consumer { p_330883_: MobEffectInstance? ->
                            if ((p_330883_!!.effect.value() as MobEffect).isInstantenous) {
                                (p_330883_.effect.value() as MobEffect).applyInstantenousEffect(
                                    player,
                                    player,
                                    entity,
                                    p_330883_.amplifier,
                                    1.0
                                )
                            } else {
                                entity.addEffect(p_330883_)
                            }
                        })


                    if (player != null) {
                        player.awardStat(Stats.ITEM_USED.get(this))
                        stack.consume(1, player)
                    }

                    if (player == null || !player.hasInfiniteMaterials()) {
                        if (stack.isEmpty) {
                            return ItemStack(Items.GLASS_BOTTLE)
                        }

                        player?.getInventory()?.add(ItemStack(Items.GLASS_BOTTLE))
                    }

                    entity.gameEvent(GameEvent.DRINK)
                    return stack
                }
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
                            cloud.addEffect(MobEffectInstance(effect))
                        }

                        level.addFreshEntity(cloud)
                    }

                    // アイテム消費
                    if (entity is ServerPlayer) {
                        entity.awardStat(Stats.ITEM_USED.get(this))
                        CriteriaTriggers.CONSUME_ITEM.trigger(entity, stack)
                    }

                    stack.consume(1, entity)

                    if (stack.isEmpty) {
                        return ItemStack(Items.GLASS_BOTTLE)
                    }
                }
                else -> {
                    return super.finishUsingItem(stack, level, entity)
                }
            }
        }
        return super.finishUsingItem(stack, level, entity)
    }
    override fun getUseDuration(
        stack: ItemStack,
        entity: LivingEntity
    ): Int {
        return 32
    }

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        val content = stack.get(NostrumRegistries.ALCHEMICAL_POTION_CONTENT) ?: return super.getUseAnimation(stack)
        when (content.form) {
            AlchemicalPotionForm.DRINK -> {
                return UseAnim.DRINK
            }

            AlchemicalPotionForm.AEROSOL -> {
                return UseAnim.DRINK
            }
            else -> {

            }
        }
        return UseAnim.DRINK
    }

    /*
    override fun onUseTick(
        level: Level,
        livingEntity: LivingEntity,
        stack: ItemStack,
        remainingUseDuration: Int
    ) {
        val content = stack.get(
            NostrumRegistries.ALCHEMICAL_POTION_CONTENT
        ) ?: return

        if (content.form != AlchemicalPotionForm.AEROSOL) {
            return
        }

        if (level.isClientSide) {
            val look = livingEntity.lookAngle

            val x = livingEntity.x + look.x * 0.5
            val y = livingEntity.eyeY + look.y * 0.5
            val z = livingEntity.z + look.z * 0.5

            level.addParticle(
                ParticleTypes.CLOUD,
                x,
                y,
                z,
                look.x * 0.05,
                look.y * 0.05,
                look.z * 0.05
            )
        }
    }

     */

    override fun getName(stack: ItemStack): Component {
        val content = stack.get(
            NostrumRegistries.ALCHEMICAL_POTION_CONTENT
        ) ?: return super.getName(stack)

        return Component.translatable(
            "item.nostrum.alchemical_potion",
            Component.translatable("item.nostrum.alchemical_potion.form.${content.form.name.lowercase()}")
        )

    }

    private fun throwPotion(
        level: Level,
        player: Player,
        hand: InteractionHand
    ): InteractionResultHolder<ItemStack> {

        val stack = player.getItemInHand(hand)

        val projectile = ThrownAlchemicalPotion(
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

        level.addFreshEntity(projectile)

        level.playSound(
            null,
            player.x,
            player.y,
            player.z,
            SoundEvents.SPLASH_POTION_THROW,
            SoundSource.PLAYERS,
            0.5f,
            0.4f / (level.random.nextFloat() * 0.4f + 0.8f)
        )

        stack.consume(1, player)

        return InteractionResultHolder.sidedSuccess(
            stack,
            level.isClientSide
        )
    }
}