package io.github.riiimc.nostrum.content.blockentities

import net.minecraft.world.effect.MobEffectInstance

data class PotionData(
    val effects: MutableList<MobEffectInstance>,
    var remaining: Int
)