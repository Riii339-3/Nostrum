package io.github.riiimc.nostrum

import io.github.riiimc.nostrum.content.blockentities.AlchemistCauldronBlockEntity
import io.github.riiimc.nostrum.content.blocks.AlchemistCauldronBlock
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipe
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipeSerializer
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier


object NostrumRegistries {
    val ITEMS = DeferredRegister.createItems(Nostrum.MODID)
    val BLOCKS = DeferredRegister.createBlocks(Nostrum.MODID)
    val DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Nostrum.MODID)
    val BE_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Nostrum.MODID)
    val RECIPE_TYPES: DeferredRegister<RecipeType<*>?> =
        DeferredRegister.create<RecipeType<*>?>(Registries.RECIPE_TYPE, Nostrum.MODID)

    val RECIPE_SERIALIZERS: DeferredRegister<RecipeSerializer<*>?> =
        DeferredRegister.create<RecipeSerializer<*>?>(Registries.RECIPE_SERIALIZER, Nostrum.MODID)
    val CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Nostrum.MODID)

    val ALCHEMY_RECIPE_SERIALIZER: Supplier<RecipeSerializer<AlchemyRecipe>> =
        RECIPE_SERIALIZERS.register("alchemy", Supplier { AlchemyRecipeSerializer() })

    val ALCHEMY_RECIPE: Supplier<RecipeType<AlchemyRecipe>> =
        RECIPE_TYPES.register<RecipeType<AlchemyRecipe>>(
            "alchemy",  // We need the qualifying generic here due to generics being generics.
            Supplier {
                RecipeType.simple<AlchemyRecipe>(
                    ResourceLocation.fromNamespaceAndPath(
                        Nostrum.MODID,
                        "alchemy"
                    )
                )
            }
        )
    val ALCHEMIST_CAULDRON_BLOCK = BLOCKS.register("alchemist_cauldron", Supplier { AlchemistCauldronBlock(
        BlockBehaviour.Properties.of())})
    val ALCHEMIST_CAULDRON_BE_TYPE = BE_TYPES.register("alchemist_cauldron", Supplier { BlockEntityType.Builder.of(::AlchemistCauldronBlockEntity, ALCHEMIST_CAULDRON_BLOCK.get()).build(null)})
    val ALCHEMIST_CAULDRON_ITEM = ITEMS.register("alchemist_cauldron", Supplier { BlockItem(ALCHEMIST_CAULDRON_BLOCK.get(), Item.Properties())})

    val ALCHEMIST_WAND = ITEMS.register("alchemist_wand", Supplier { Item(Item.Properties().stacksTo(1).durability(1024))})

    fun registry(bus: IEventBus) {
        ITEMS.register(bus)
        BLOCKS.register(bus)
        CREATIVE_TABS.register(bus)
        BE_TYPES.register(bus)
        RECIPE_TYPES.register(bus)
        RECIPE_SERIALIZERS.register(bus)
    }
}