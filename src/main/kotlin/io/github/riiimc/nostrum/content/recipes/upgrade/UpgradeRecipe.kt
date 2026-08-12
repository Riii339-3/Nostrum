package io.github.riiimc.nostrum.content.recipes.upgrade

/*
class UpgradeRecipe(
    val inputItems: List<Ingredient>,
    val inputFluid: FluidStack,
    val result: AlchemicalUpgrade
) : Recipe<AlchemyRecipeInput> {

    init {
        require(!result.) {
            "AlchemyRecipe must have at least one output: result or resultFluid"
        }
    }

    override fun getIngredients(): NonNullList<Ingredient?> {
        val list = NonNullList.create<Ingredient?>()
        list.addAll(inputItems)
        return list
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        return true
    }

    override fun matches(
        input: AlchemyRecipeInput,
        level: Level
    ): Boolean {

        if (inputItems.size != input.stacks.size) {
            return false
        }

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
                inputFluid,
                input.fluid
            )
        ) {
            return false
        }

        return true
    }

    override fun getResultItem(
        registries: HolderLookup.Provider
    ): ItemStack {
        return result
    }

    override fun assemble(
        input: AlchemyRecipeInput,
        registries: HolderLookup.Provider
    ): ItemStack {
        return result.copy()
    }

    override fun getType(): RecipeType<*> {
        return NostrumRegistries.ALCHEMY_RECIPE.get()
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return NostrumRegistries.ALCHEMY_RECIPE_SERIALIZER.get()
    }
}

 */