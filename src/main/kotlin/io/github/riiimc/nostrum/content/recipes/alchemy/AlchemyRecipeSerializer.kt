package io.github.riiimc.nostrum.content.recipes.alchemy

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
                        .optionalFieldOf("result", ItemStack.EMPTY)
                        .forGetter(AlchemyRecipe::result),

                    FluidStack.CODEC
                        .optionalFieldOf("resultFluid", FluidStack.EMPTY)
                        .forGetter(AlchemyRecipe::resultFluid)

                ).apply(inst, ::AlchemyRecipe)
            }

        private val OPTIONAL_FLUID_STACK:
                StreamCodec<RegistryFriendlyByteBuf, FluidStack> =
            object : StreamCodec<RegistryFriendlyByteBuf, FluidStack> {

                override fun encode(
                    buf: RegistryFriendlyByteBuf,
                    value: FluidStack
                ) {
                    buf.writeBoolean(!value.isEmpty)

                    if (!value.isEmpty) {
                        FluidStack.STREAM_CODEC.encode(buf, value)
                    }
                }

                override fun decode(
                    buf: RegistryFriendlyByteBuf
                ): FluidStack {
                    return if (buf.readBoolean()) {
                        FluidStack.STREAM_CODEC.decode(buf)
                    } else {
                        FluidStack.EMPTY
                    }
                }
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

                OPTIONAL_FLUID_STACK,
                AlchemyRecipe::resultFluid,

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