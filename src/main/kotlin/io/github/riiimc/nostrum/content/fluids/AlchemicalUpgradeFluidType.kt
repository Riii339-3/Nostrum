package io.github.riiimc.nostrum.content.fluids

import io.github.riiimc.nostrum.Nostrum.Companion.rl
import io.github.riiimc.nostrum.NostrumRegistries
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.FluidType


class AlchemicalUpgradeFluidType(properties: FluidType.Properties): FluidType(properties) {
    override fun getRarity(): Rarity {
        return Rarity.EPIC
    }

    override fun isVaporizedOnPlacement(level: Level, pos: BlockPos, stack: FluidStack): Boolean {
        return true
    }

    override fun getDescription(stack: FluidStack): Component {
        val component = stack.get(NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT) ?: return super.getDescription(stack)
        return Component.translatable("fluid_type.nostrum.alchemical_upgrade_type.upgrade",
            Component.translatable("nostrum.tooltip.alchemical_upgrade.upgrade.${component.id.namespace}.${component.id.path}")).withStyle {style -> style.withFont(rl("alchemical"))}
    }
}