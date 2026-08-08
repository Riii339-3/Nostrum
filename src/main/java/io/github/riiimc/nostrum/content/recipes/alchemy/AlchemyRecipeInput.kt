package io.github.riiimc.nostrum.content.recipes.alchemy

import net.minecraft.core.NonNullList
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.fluids.FluidStack

class AlchemyRecipeInput(val state: BlockState, val stacks: NonNullList<ItemStack>, val fluid: FluidStack): RecipeInput {
    override fun getItem(slot: Int): ItemStack {
        return stacks.getOrElse(slot) {
            throw IllegalArgumentException("No item for index $slot")
        }
    }

    override fun size(): Int {
        return stacks.size
    }

}