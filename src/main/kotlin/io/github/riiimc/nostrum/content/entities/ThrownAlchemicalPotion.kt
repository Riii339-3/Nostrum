package io.github.riiimc.nostrum.content.entities

import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.content.components.AlchemicalPotionForm
import net.minecraft.core.component.DataComponents
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.AreaEffectCloud
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

class ThrownAlchemicalPotion(
    type: EntityType<out ThrownAlchemicalPotion>,
    level: Level
) : ThrowableItemProjectile(type, level) {

    constructor(level: Level, owner: LivingEntity, stack: ItemStack) :
            this(NostrumRegistries.THROWN_ALCHEMICAL_POTION.get(), level) {

        setOwner(owner)
        item = stack

        val look = owner.lookAngle

        setPos(
            owner.x + look.x * 0.5,
            owner.eyeY - 0.1 + look.y * 0.5,
            owner.z + look.z * 0.5
        )
    }

    constructor(level: Level, x: Double, y: Double, z: Double, stack: ItemStack) :
            this(NostrumRegistries.THROWN_ALCHEMICAL_POTION.get(), level) {
        setPos(x, y, z)
        item = stack
    }

    override fun getDefaultItem(): Item {
        return NostrumRegistries.ALCHEMICAL_POTION.get()
    }

    override fun onHit(hitResult: HitResult) {
        if (level().isClientSide) return

        val content = item.get(
            NostrumRegistries.ALCHEMICAL_POTION_CONTENT
        ) ?: run {
            discard()
            return
        }

        when (content.form) {
            AlchemicalPotionForm.SPLASH -> {
                applyPotionEffects()
            }

            AlchemicalPotionForm.LINGERING -> {
                createLingeringCloud()
            }

            else -> {

            }
        }

        discard()
    }
    private fun applyPotionEffects() {
        val stack = item

        val contents = stack.getOrDefault(
            DataComponents.POTION_CONTENTS,
            PotionContents.EMPTY
        )

        val center = position()

        for (effect in contents.allEffects) {
            // 範囲内のLivingEntityに適用
            val entities = level().getEntitiesOfClass(
                LivingEntity::class.java,
                boundingBox.inflate(4.0)
            )

            for (entity in entities) {
                entity.addEffect(MobEffectInstance(effect))
            }
        }
    }
    private fun createLingeringCloud() {
        val level = level()

        val cloud = AreaEffectCloud(
            level,
            x,
            y,
            z
        )

        cloud.radius = 3.0f
        cloud.duration = 300 // 15秒
        cloud.waitTime = 0

        cloud.radiusOnUse = -0.5f
        cloud.radiusPerTick = -0.005f

        val contents = item.getOrDefault(
            DataComponents.POTION_CONTENTS,
            PotionContents.EMPTY
        )

        contents.forEachEffect { effect ->
            cloud.addEffect(
                MobEffectInstance(effect)
            )
        }

        level.addFreshEntity(cloud)
    }
}