package io.github.riiimc.nostrum.content.recipes.alchemy

import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.fluids.FluidStack

data class AlchemyRecipeData(
    val result: ItemStack,
    val resultFluid: FluidStack
) {
}