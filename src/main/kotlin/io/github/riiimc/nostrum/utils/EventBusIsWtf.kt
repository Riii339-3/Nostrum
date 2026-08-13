package io.github.riiimc.nostrum.utils

import io.github.riiimc.nostrum.Nostrum.Companion.rl
import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.content.components.AlchemicalUpgradeComponent
import net.mcexpanded.fancytabsections.FancyTabSections
import net.mcexpanded.fancytabsections.Section.SectionColored
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent


object EventBusIsWtf {
    @SubscribeEvent
    fun onBuildCreativeModeTabContents(
        event: BuildCreativeModeTabContentsEvent
    ) {
        if (event.tabKey == NostrumRegistries.NOSTRUM_TAB)
            FancyTabSections.addSection(
                rl("nostrum"),
                SectionColored(rl("tools"))
                    .setTitle(Component.translatable("section.nostrum.tools"))
                    .setBannerColor(-0xe5e5d2)
                    .setTextColor(-0x44559a)
                    .setTextShadow(true)
                    .add(
                        ItemStack(NostrumRegistries.DIRT_SWORD.get()).apply {
                            set(
                                NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT,
                                AlchemicalUpgradeComponent(
                                    rl("dirt_critical")
                                )
                            )
                        }
                    )
            )
    }

}