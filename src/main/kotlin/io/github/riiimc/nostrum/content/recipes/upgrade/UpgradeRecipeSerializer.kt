package io.github.riiimc.nostrum.content.recipes.upgrade

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipe
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.fluids.FluidStack

/*
class UpgradeRecipeSerializer : RecipeSerializer<UpgradeRecipe> {

    companion object {

        val CODEC: MapCodec<UpgradeRecipe> =
            RecordCodecBuilder.mapCodec { inst ->
                inst.group(

                    BlockState.CODEC
                        .fieldOf("state")
                        .forGetter(UpgradeRecipe::inputState),

                    Ingredient.CODEC.listOf()
                        .fieldOf("ingredient")
                        .forGetter(UpgradeRecipe::inputItems),

                    FluidStack.CODEC
                        .fieldOf("fluid")
                        .forGetter(UpgradeRecipe::inputFluid),

                    ItemStack.CODEC
                        .optionalFieldOf("result", ItemStack.EMPTY)
                        .forGetter(UpgradeRecipe::result),

                    FluidStack.CODEC
                        .optionalFieldOf("resultFluid", FluidStack.EMPTY)
                        .forGetter(UpgradeRecipe::resultFluid)

                ).apply(inst, ::UpgradeRecipe)
            }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, UpgradeRecipe> =
            StreamCodec.composite(

                ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY),
                UpgradeRecipe::inputState,

                Ingredient.CONTENTS_STREAM_CODEC.apply(
                    ByteBufCodecs.list()
                ),
                UpgradeRecipe::inputItems,

                FluidStack.STREAM_CODEC,
                UpgradeRecipe::inputFluid,

                ItemStack.STREAM_CODEC,
                UpgradeRecipe::result,

                FluidStack.STREAM_CODEC,
                UpgradeRecipe::resultFluid,

                ::AlchemyRecipe
            )
    }

    override fun codec(): MapCodec<AlchemyRecipe> {
        return CODEC
    }

    override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, AlchemyRecipe> {
        return STREAM_CODEC
    }
}

 */