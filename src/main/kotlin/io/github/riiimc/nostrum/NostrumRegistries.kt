package io.github.riiimc.nostrum

import io.github.riiimc.nostrum.Nostrum.Companion.rl
import io.github.riiimc.nostrum.content.blockentities.AlchemistCauldronBlockEntity
import io.github.riiimc.nostrum.content.blocks.AlchemistCauldronBlock
import io.github.riiimc.nostrum.content.components.AlchemicalPotionContent
import io.github.riiimc.nostrum.content.components.AlchemicalPotionForm
import io.github.riiimc.nostrum.content.entities.ThrownAlchemicalPotion
import io.github.riiimc.nostrum.content.items.AlchemicalPotionItem
import io.github.riiimc.nostrum.content.items.AlchemistWandItem
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipe
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipeSerializer
import net.mcexpanded.fancytabsections.FTSExampleMod.FTSExampleModItems
import net.mcexpanded.fancytabsections.FancyTabSections
import net.mcexpanded.fancytabsections.Section.SectionColored
import net.mcexpanded.fancytabsections.creativetab.ConglomerateOfItems.RegistryDependentEntry
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup.RegistryLookup
import net.minecraft.core.HolderSet
import net.minecraft.core.RegistryAccess
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Function
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
    val ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Nostrum.MODID)
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
    val ALCHEMIST_CAULDRON_ITEM = ITEMS.register("alchemist_cauldron", Supplier { BlockItem(ALCHEMIST_CAULDRON_BLOCK.get(), Item.Properties().rarity(
        Rarity.UNCOMMON))})

    val ALCHEMIST_WAND = ITEMS.register("alchemist_wand", Supplier { AlchemistWandItem(Item.Properties().stacksTo(1).durability(1024))})

    val ALCHEMICAL_POTION_CONTENT = DATA_COMPONENTS.register(
        "alchemical_potion_content", Supplier {
        DataComponentType.builder<AlchemicalPotionContent>()
            .persistent(AlchemicalPotionContent.CODEC)
            .networkSynchronized(AlchemicalPotionContent.STREAM_CODEC)
            .build()
    })

    val ALCHEMICAL_POTION = ITEMS.register("alchemical_potion", Supplier {
        AlchemicalPotionItem(Item.Properties().stacksTo(16))
    })

    val THROWN_ALCHEMICAL_POTION =
        ENTITY_TYPES.register("thrown_alchemical_potion", Supplier {
            EntityType.Builder
                .of(
                    ::ThrownAlchemicalPotion,
                    MobCategory.MISC
                )
                .sized(0.25f, 0.25f)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build(
                    rl("thrown_alchemical_potion")
                        .toString()
                )
        })

    val NOSTRUM_TAB = CREATIVE_TABS.register(
        "nostrum",
        Supplier {
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.nostrum"))
                .icon { ItemStack(ALCHEMIST_WAND.get()) }
                .build()
        }
    )
    val NOSTRUM_POTIONS = CREATIVE_TABS.register(
        "alchemical_potions",
        Supplier {
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.alchemical_potions"))
                .icon {
                    ItemStack(ALCHEMICAL_POTION.get()).apply {
                        set(
                            DataComponents.POTION_CONTENTS,
                            PotionContents(Potions.HEALING)
                        )
                    }
                }
                .build()
        }
    )

    val BASIC_ALCHEMICAL_MATERIAL = ITEMS.register("basic_alchemical_material", Supplier {
        Item(Item.Properties().stacksTo(64))
    })

    val INTERMEDIATE_ALCHEMICAL_MATERIAL = ITEMS.register("intermediate_alchemical_material", Supplier {
        Item(Item.Properties().stacksTo(64))
    })

    val ADVANCED_ALCHEMICAL_MATERIAL = ITEMS.register("advanced_alchemical_material", Supplier {
        Item(Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON))
    })

    val SUPERIOR_ALCHEMICAL_MATERIAL = ITEMS.register("superior_alchemical_material", Supplier {
        Item(Item.Properties().stacksTo(64).rarity(Rarity.RARE))
    })

    val ULTIMATE_ALCHEMICAL_MATERIAL = ITEMS.register("ultimate_alchemical_material", Supplier {
        Item(Item.Properties().stacksTo(64).rarity(Rarity.EPIC))
    })

    val ALCHEMICAL_BREWING_MATERIAL = ITEMS.register("alchemical_brewing_material", Supplier {
        Item(Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON))
    })

    val ALCHEMICAL_MIXING_MATERIAL = ITEMS.register("alchemical_mixing_material", Supplier {
        Item(Item.Properties().stacksTo(64).rarity(Rarity.EPIC))
    })




    fun registry(bus: IEventBus) {
        ENTITY_TYPES.register(bus)
        ITEMS.register(bus)
        BLOCKS.register(bus)
        CREATIVE_TABS.register(bus)
        DATA_COMPONENTS.register(bus)
        BE_TYPES.register(bus)
        RECIPE_TYPES.register(bus)
        RECIPE_SERIALIZERS.register(bus)

        FancyTabSections.addSection(
            rl("nostrum"),  //identifier of the section
            SectionColored(rl("alchemy")) //title to display in the "empty row" (banner) of the section
                //by default the title will use the translation key `section.[namespace].[path]`, just as shown here
                .setTitle(Component.translatable("section.nostrum.alchemy")) //background color of the "empty row" - ARGB
                .setBannerColor(-0xe5e5d2) //text color - ARGB
                .setTextColor(-0x44559a) //text shadow
                .setTextShadow(true) //adds an item

                .add(ALCHEMIST_CAULDRON_ITEM) //adds a modded item, using the DeferredItem<Item>
                .add(ALCHEMIST_WAND) //adds an ItemStack
        )

        FancyTabSections.addSection(
            rl("nostrum"),  //identifier of the section
            SectionColored(rl("alchemy_materials")) //title to display in the "empty row" (banner) of the section
                //by default the title will use the translation key `section.[namespace].[path]`, just as shown here
                .setTitle(Component.translatable("section.nostrum.alchemy_materials")) //background color of the "empty row" - ARGB
                .setBannerColor(-0xe5e5d2) //text color - ARGB
                .setTextColor(-0x44559a) //text shadow
                .setTextShadow(true) //adds an item
                .add(BASIC_ALCHEMICAL_MATERIAL)
                .add(INTERMEDIATE_ALCHEMICAL_MATERIAL)
                .add(ADVANCED_ALCHEMICAL_MATERIAL)
                .add(SUPERIOR_ALCHEMICAL_MATERIAL)
                .add(ULTIMATE_ALCHEMICAL_MATERIAL)
                .add(ALCHEMICAL_BREWING_MATERIAL)
                .add(ALCHEMICAL_MIXING_MATERIAL)
        )

        for (form in AlchemicalPotionForm.entries) {
            FancyTabSections.addSection(
                rl("alchemical_potions"),
                SectionColored(rl(form.name.lowercase()))
                    .setTitle(Component.translatable("section.nostrum.alchemical_potions.${form.name.lowercase()}"))
                    .setBannerColor(-0xe5e5d2)
                    .setTextColor(-0x44559a)
                    .setTextShadow(true)
                    .also { section ->
                        BuiltInRegistries.POTION.holders().forEach { holder ->
                            section.add(Supplier {
                                ItemStack(ALCHEMICAL_POTION.get()).apply {
                                    set(
                                        DataComponents.POTION_CONTENTS,
                                        PotionContents(holder)
                                    )
                                }
                                    .apply { set(
                                        ALCHEMICAL_POTION_CONTENT,
                                        AlchemicalPotionContent(form)
                                    ) }
                            })
                        }
                    }
            )
        }


    }
}