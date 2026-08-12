package io.github.riiimc.nostrum.content.upgrade

import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

interface AlchemicalEventHandler {
    fun execute(
        level: Level,
        player: Player,
        upgrade: AlchemicalUpgrade
    )
}