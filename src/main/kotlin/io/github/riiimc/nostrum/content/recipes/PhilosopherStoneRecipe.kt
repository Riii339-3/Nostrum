package io.github.riiimc.nostrum.content.recipes

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.MapDecoder
import com.mojang.serialization.MapEncoder
import com.mojang.serialization.MapLike
import com.mojang.serialization.RecordBuilder
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.utils.NostrumTags
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.Level
import java.util.stream.Stream

class PhilosopherStoneRecipe(
    category: CraftingBookCategory
) : CustomRecipe(category) {

    override fun matches(
        input: CraftingInput,
        level: Level
    ): Boolean {
        var hasStone = false
        var target: ItemStack? = null

        for (stack in input.items()) {
            if (stack.isEmpty) continue

            if (stack.`is`(NostrumRegistries.PHILOSOPHER_STONE.get())) {
                if (hasStone) return false
                hasStone = true
                continue
            }

            if (stack.`is`(NostrumTags.PHILOSOPHERS_STONE_BLACKLIST)) {
                return false
            }

            if (target != null) {
                return false
            }

            target = stack
        }

        return hasStone && target != null
    }

    override fun assemble(
        input: CraftingInput,
        registries: HolderLookup.Provider
    ): ItemStack {
        for (stack in input.items()) {
            if (
                !stack.isEmpty &&
                !stack.`is`(NostrumRegistries.PHILOSOPHER_STONE.get())
            ) {
                return stack.copyWithCount(2)
            }
        }

        return ItemStack.EMPTY
    }

    override fun getRemainingItems(
        input: CraftingInput
    ): NonNullList<ItemStack> {

        val remaining =
            NonNullList.withSize(
                input.size(),
                ItemStack.EMPTY
            )

        for (i in 0 until input.size()) {
            val stack = input.getItem(i)

            if (
                stack.`is`(
                    NostrumRegistries.PHILOSOPHER_STONE.get()
                )
            ) {
                val copy = stack.copy()

                copy.damageValue++

                if (copy.damageValue < copy.maxDamage) {
                    remaining[i] = copy
                }
            }
        }

        return remaining
    }

    override fun canCraftInDimensions(
        width: Int,
        height: Int
    ): Boolean {
        return width * height >= 2
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return NostrumRegistries
            .PHILOSOPHER_STONE_RECIPE_SERIALIZER
            .get()
    }

    object Serializer : RecipeSerializer<PhilosopherStoneRecipe> {

        private val CODEC: MapCodec<PhilosopherStoneRecipe> =
            MapCodec.unit(CraftingBookCategory.MISC)
                .xmap(
                    ::PhilosopherStoneRecipe,
                    { CraftingBookCategory.MISC }
                )

        private val STREAM_CODEC:
                StreamCodec<RegistryFriendlyByteBuf, PhilosopherStoneRecipe> =
            object : StreamCodec<RegistryFriendlyByteBuf, PhilosopherStoneRecipe> {

                override fun encode(
                    buf: RegistryFriendlyByteBuf,
                    value: PhilosopherStoneRecipe
                ) {
                    // 何も送らない
                }

                override fun decode(
                    buf: RegistryFriendlyByteBuf
                ): PhilosopherStoneRecipe {
                    return PhilosopherStoneRecipe(
                        CraftingBookCategory.MISC
                    )
                }
            }

        override fun codec(): MapCodec<PhilosopherStoneRecipe> {
            return CODEC
        }

        override fun streamCodec():
                StreamCodec<RegistryFriendlyByteBuf, PhilosopherStoneRecipe> {
            return STREAM_CODEC
        }
    }
}