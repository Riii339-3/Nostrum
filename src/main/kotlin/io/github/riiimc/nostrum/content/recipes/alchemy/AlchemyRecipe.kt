package io.github.riiimc.nostrum.content.recipes.alchemy

import io.github.riiimc.nostrum.NostrumRegistries
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.fluids.FluidStack


class AlchemyRecipe(val inputState: BlockState, val inputItems: List<Ingredient>, val inputFluid: FluidStack, val result: ItemStack): Recipe<AlchemyRecipeInput> {

    // A list of our ingredients. Does not need to be overridden if you have no ingredients
    // (the default implementation returns an empty list here). It makes sense to cache larger lists in a field.
    override fun getIngredients(): NonNullList<Ingredient?> {
        val list = NonNullList.create<Ingredient?>()
        list.addAll(inputItems)
        return list
    }


    // Grid-based recipes should return whether their recipe can fit in the given dimensions.
    // We don't have a grid, so we just return if any item can be placed in there.
    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        return true
    }

    // Check whether the given input matches this recipe. The first parameter matches the generic.
    // We check our blockstate and our item stack, and only return true if both match.
    override fun matches(
        input: AlchemyRecipeInput,
        level: Level
    ): Boolean {
        if (inputState != input.state) return false

        if (inputItems.size != input.stacks.size) return false

        val remaining = input.stacks.toMutableList()

        for (ingredient in inputItems) {
            val index = remaining.indexOfFirst {
                ingredient.test(it)
            }

            if (index == -1) {
                return false
            }

            remaining.removeAt(index)
        }

        if (input.fluid.amount < inputFluid.amount) {
            return false
        }

        if (!FluidStack.isSameFluidSameComponents(
                this.inputFluid,
                input.fluid
            )
        ) {
            return false
        }

        return input.fluid.amount >= inputFluid.amount
    }


    // Return an UNMODIFIABLE version of your result here. The result of this method is mainly intended
    // for the recipe book, and commonly used by JEI and other recipe viewers as well.
    override fun getResultItem(registries: HolderLookup.Provider): ItemStack {
        return this.result
    }

    // Return the result of the recipe here, based on the given input. The first parameter matches the generic.
    // IMPORTANT: Always call .copy() if you use an existing result! If you don't, things can and will break,
    // as the result exists once per recipe, but the assembled stack is created each time the recipe is crafted.
     override fun assemble(input: AlchemyRecipeInput, registries: HolderLookup.Provider): ItemStack {
        return this.result.copy()
    }

    override fun getType(): RecipeType<*> {
        return NostrumRegistries.ALCHEMY_RECIPE.get()
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return NostrumRegistries.ALCHEMY_RECIPE_SERIALIZER.get()
    }

}