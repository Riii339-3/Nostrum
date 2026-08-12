package io.github.riiimc.nostrum.content.fluids

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.FluidType


class AlchemicalUpgradeFluidType(properties: FluidType.Properties): FluidType(properties) {
    override fun getRarity(): Rarity {
        return Rarity.EPIC
    }

    public override fun onVaporize(player: Player?, level: Level, pos: BlockPos, stack: FluidStack) {
        super.onVaporize(player, level, pos, stack)
    }

    public override fun isVaporizedOnPlacement(level: Level?, pos: BlockPos?, stack: FluidStack?): Boolean {
        return true
    }

}