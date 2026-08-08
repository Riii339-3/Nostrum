package io.github.riiimc.nostrum.content.blockentities

import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.content.recipes.AlchemistCauldronMode
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipe
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipeInput
import io.github.riiimc.nostrum.utils.ResizeStackHandler
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.fluids.FluidStack

class AlchemistCauldronBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(NostrumRegistries.ALCHEMIST_CAULDRON_BE_TYPE.get(), pos, state) {
    var mode = AlchemistCauldronMode.ALCHEMY
    val inventory = ResizeStackHandler(0)
    var fluid = FluidStack.EMPTY
    override fun saveAdditional(tag: CompoundTag, provider: HolderLookup.Provider) {
        super.saveAdditional(tag, provider)
        tag.putString("mode", mode.name)
        tag.put(
            "Inventory",
            inventory.serializeNBT(provider)
        )
        if (!fluid.isEmpty) {
            tag.put(
                "Fluid",
                fluid.save(provider)
            )
        }

    }

    override fun loadAdditional(tag: CompoundTag, provider: HolderLookup.Provider) {
        super.loadAdditional(tag, provider)
        val modeName = tag.getString("mode")
        try {
            mode = AlchemistCauldronMode.valueOf(modeName)
        } catch (e: IllegalArgumentException) {
            mode = AlchemistCauldronMode.ALCHEMY
        }
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(
                provider,
                tag.getCompound("Inventory")
            )
        }
        if (tag.contains("Fluid")) {
            val newFluid = FluidStack.parse(
                provider,
                tag.getCompound("Fluid")
            ).orElse(FluidStack.EMPTY)
            fluid = newFluid ?: FluidStack.EMPTY
        } else {
            fluid = FluidStack.EMPTY
        }
    }

    fun createRecipeInput(): AlchemyRecipeInput {
        val stacks = NonNullList.withSize(
            inventory.slots,
            ItemStack.EMPTY
        )

        for (i in 0 until inventory.slots) {
            stacks[i] = inventory.getStackInSlot(i)
        }

        return AlchemyRecipeInput(
            blockState,
            stacks,
            fluid
        )
    }

    fun checkRecipe(): AlchemyRecipe? {
        val level = level ?: return null

        val input = createRecipeInput()

        println("=== CHECK RECIPE ===")
        println("state = ${input.state}")
        println("stacks = ${input.stacks}")
        println("fluid = ${input.fluid}")

        val recipes = level.recipeManager
            .getAllRecipesFor(
                NostrumRegistries.ALCHEMY_RECIPE.get()
            )

        println("recipes = ${recipes.size}")

        for (holder in recipes) {
            val recipe = holder.value

            println("Recipe:")
            println("  state = ${recipe.inputState}")
            println("  items = ${recipe.inputItems}")
            println("  fluid = ${recipe.inputFluid}")

            println("  matches = ${recipe.matches(input, level)}")
        }

        return level.recipeManager
            .getRecipeFor(
                NostrumRegistries.ALCHEMY_RECIPE.get(),
                input,
                level
            )
            .map { it.value }
            .orElse(null)
    }
    fun useRecipe() {

    }
}