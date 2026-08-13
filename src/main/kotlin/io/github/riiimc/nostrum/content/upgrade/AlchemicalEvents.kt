package io.github.riiimc.nostrum.content.upgrade

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

object AlchemicalEvents {
    private val handlers =
        mutableMapOf<ResourceLocation, (Player, Double) -> Unit>()

    fun register(
        id: ResourceLocation,
        handler: (Player, Double) -> Unit
    ) {
        handlers[id] = handler
    }

}