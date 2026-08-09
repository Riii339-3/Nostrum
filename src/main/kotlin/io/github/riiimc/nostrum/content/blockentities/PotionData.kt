package io.github.riiimc.nostrum.content.blockentities

import io.github.riiimc.nostrum.content.components.AlchemicalPotionForm
import net.minecraft.world.effect.MobEffectInstance

data class PotionData(
    val effects: MutableList<MobEffectInstance>,
    var remaining: Int,
    var form: AlchemicalPotionForm
)