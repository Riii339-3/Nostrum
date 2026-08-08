package io.github.riiimc.nostrum.content.recipes.alchemy

import com.mojang.datafixers.util.Function3
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.fluids.FluidStack
import java.util.function.Function


class AlchemyRecipeSerializer : RecipeSerializer<AlchemyRecipe> {

    companion object {
        val CODEC: MapCodec<AlchemyRecipe> =
            RecordCodecBuilder.mapCodec { inst ->
                inst.group(
                    BlockState.CODEC
                        .fieldOf("state")
                        .forGetter(AlchemyRecipe::inputState),

                    Ingredient.CODEC.listOf()
                        .fieldOf("ingredient")
                        .forGetter(AlchemyRecipe::inputItems),

                    FluidStack.CODEC
                        .fieldOf("fluid")
                        .forGetter(AlchemyRecipe::inputFluid),

                    ItemStack.CODEC
                        .fieldOf("result")
                        .forGetter(AlchemyRecipe::result),

                    ).apply(inst, ::AlchemyRecipe)
            }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AlchemyRecipe> =
            StreamCodec.composite(
                ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY),
                AlchemyRecipe::inputState,

                Ingredient.CONTENTS_STREAM_CODEC.apply(
                    ByteBufCodecs.list()
                ),
                AlchemyRecipe::inputItems,

                FluidStack.STREAM_CODEC,
                AlchemyRecipe::inputFluid,

                ItemStack.STREAM_CODEC,
                AlchemyRecipe::result,

                ::AlchemyRecipe
            )
    }

    override fun codec(): MapCodec<AlchemyRecipe> = CODEC

    override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, AlchemyRecipe> =
        STREAM_CODEC
}


