package io.github.riiimc.nostrum

import io.github.riiimc.nostrum.Nostrum.Companion.rl
import io.github.riiimc.nostrum.compat.NostrumCompat
import io.github.riiimc.nostrum.compat.ageratum.NostrumAgeratumRegistries
import io.github.riiimc.nostrum.content.blockentities.AlchemistCauldronBlockEntity
import io.github.riiimc.nostrum.content.blocks.AlchemistCauldronBlock
import io.github.riiimc.nostrum.content.components.AlchemicalPotionContent
import io.github.riiimc.nostrum.content.components.AlchemicalPotionForm
import io.github.riiimc.nostrum.content.components.AlchemicalUpgradeComponent
import io.github.riiimc.nostrum.content.entities.ThrownAlchemicalPotion
import io.github.riiimc.nostrum.content.fluids.AlchemicalUpgradeFluidType
import io.github.riiimc.nostrum.content.items.AlchemicalPotionItem
import io.github.riiimc.nostrum.content.items.AlchemicalUpgradeFluidBucketItem
import io.github.riiimc.nostrum.content.items.AlchemistWandItem
import io.github.riiimc.nostrum.content.items.PhilosophersStoneItem
import io.github.riiimc.nostrum.content.recipes.PhilosopherStoneRecipe
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipe
import io.github.riiimc.nostrum.content.recipes.alchemy.AlchemyRecipeSerializer
import io.github.riiimc.nostrum.content.tiers.DirtTier
import io.github.riiimc.nostrum.content.tiers.NostrumRarities
import io.github.riiimc.nostrum.content.upgrade.AlchemicalEvent
import io.github.riiimc.nostrum.content.upgrade.AlchemicalEvents
import net.mcexpanded.fancytabsections.FancyTabSections
import net.mcexpanded.fancytabsections.Section.SectionColored
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.*
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.fluids.BaseFlowingFluid
import net.neoforged.neoforge.fluids.FluidType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
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
    val FLUID_TYPES: DeferredRegister<FluidType> =
        DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, Nostrum.MODID)
    val FLUIDS = DeferredRegister.create(Registries.FLUID, Nostrum.MODID)
    val ALCHEMICAL_UPGRADE_FLUID_TYPE = FLUID_TYPES.register("alchemical_upgrade_type", Supplier { AlchemicalUpgradeFluidType(FluidType.Properties.create().viscosity(100).temperature(100).density(100).lightLevel(7))})
    val ALCHEMICAL_UPGRADE_FLUID = FLUIDS.register("alchemical_upgrade", Supplier { BaseFlowingFluid.Source(ALCHEMICAL_FLUID_PROPERTIES)})
    val ALCHEMICAL_UPGRADE_FLUID_FLOWING = FLUIDS.register("alchemical_upgrade_flowing", Supplier { BaseFlowingFluid.Flowing(ALCHEMICAL_FLUID_PROPERTIES)})

    val ALCHEMICAL_UPGRADE_BUCKET = ITEMS.register("alchemical_upgrade_bucket", Supplier { BucketItem(ALCHEMICAL_UPGRADE_FLUID.get(), Item.Properties().stacksTo(1).rarity(
        Rarity.RARE))})
    val ALCHEMICAL_FLUID_PROPERTIES: BaseFlowingFluid.Properties = BaseFlowingFluid.Properties(
        ALCHEMICAL_UPGRADE_FLUID_TYPE,
        ALCHEMICAL_UPGRADE_FLUID,
        ALCHEMICAL_UPGRADE_FLUID_FLOWING
    )
        .explosionResistance(100f)
        .bucket(ALCHEMICAL_UPGRADE_BUCKET) //                    .block(MANA_BLOCK)
        .levelDecreasePerBlock(1)
        .tickRate(20)

    @JvmStatic
    val ALCHEMICAL_UPGRADE_COMPONENT = DATA_COMPONENTS.register("alchemical_upgrade", Supplier {
        DataComponentType.builder<AlchemicalUpgradeComponent>()
            .persistent(AlchemicalUpgradeComponent.CODEC)
            .networkSynchronized(AlchemicalUpgradeComponent.STREAM_CODEC)
            .build() })

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
    val PHILOSOPHER_STONE_RECIPE: Supplier<RecipeType<PhilosopherStoneRecipe>> =
        RECIPE_TYPES.register<RecipeType<PhilosopherStoneRecipe>>(
            "philosopher_stone",  // We need the qualifying generic here due to generics being generics.
            Supplier {
                RecipeType.simple<PhilosopherStoneRecipe>(
                    ResourceLocation.fromNamespaceAndPath(
                        Nostrum.MODID,
                        "philosopher_stone"
                    )
                )
            }
        )


    val PHILOSOPHER_STONE_RECIPE_SERIALIZER: Supplier<RecipeSerializer<PhilosopherStoneRecipe>> =
        RECIPE_SERIALIZERS.register("philosopher_stone", Supplier { PhilosopherStoneRecipe.Serializer })


    val PHILOSOPHER_STONE = ITEMS.register("philosopher_stone", Supplier {
        PhilosophersStoneItem(
            Item.Properties().durability(6).rarity(NostrumRarities.EPILOGUE_ENUM_PROXY.value)
        )
    })


    val DIRT_SWORD = ITEMS.register("dirt_sword", Supplier {
        object : SwordItem(
            DirtTier,
            Item.Properties()
                .durability(128)
                .attributes(
                    SwordItem.createAttributes(
                        Tiers.IRON,
                        2,
                        -2.4f
                    )
                )
                .component(ALCHEMICAL_UPGRADE_COMPONENT, AlchemicalUpgradeComponent(rl("dirt_critical")))
        ) {
            override fun isValidRepairItem(
                stack: ItemStack,
                repairCandidate: ItemStack
            ): Boolean {
                return repairCandidate.`is`(Items.DIRT)
            }
        }
    })
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
    val NOSTRUM_UPGRADES = CREATIVE_TABS.register(
        "alchemical_upgrades",
        Supplier {
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.alchemical_upgrades"))
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
        FLUID_TYPES.register(bus)
        FLUIDS.register(bus)

        NostrumCompat.addonRegistry(bus)

        val nostrumSection = SectionColored(rl("alchemy"))
            .setTitle(Component.translatable("section.nostrum.alchemy"))
            .setBannerColor(-0xe5e5d2)
            .setTextColor(-0x44559a)
            .setTextShadow(true)
            .setDisplayItem {
                ItemStack(ALCHEMIST_CAULDRON_ITEM.get())
            }

        FancyTabSections.addSection(
            rl("nostrum"),
            nostrumSection
        )

        if (NostrumCompat.isModLoaded("ageratum")) {
            nostrumSection.add(NostrumAgeratumRegistries.GUIDE_BOOK)
        }

        nostrumSection
            .add(ALCHEMIST_CAULDRON_ITEM)
            .add(ALCHEMIST_WAND)

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
                .add(PHILOSOPHER_STONE)
        )


        FancyTabSections.addSection(
            rl("nostrum"),
            SectionColored(rl("tools"))
                .setTitle(Component.translatable("section.nostrum.tools"))
                .setBannerColor(-0xe5e5d2)
                .setTextColor(-0x44559a)
                .setTextShadow(true)
                .add(DIRT_SWORD)
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

        registerUpgradeEvents()
    }

    fun registerUpgradeEvents() {
        AlchemicalEvents.register(
            rl("heal")
        ) { player, amount ->
            player.heal(amount.toFloat())
        }
        AlchemicalEvents.register(
            rl("critical")
        ) { player, amount ->
            // 処理
        }

        AlchemicalEvents.register(
            rl("custom")
        ) { player, amount ->
            // 処理
        }
    }
}