package io.github.riiimc.nostrum.content.items

import io.github.riiimc.nostrum.Nostrum
import io.github.riiimc.nostrum.NostrumRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.material.Fluid

class AlchemicalUpgradeFluidBucketItem(fluid: Fluid, properties: Properties): BucketItem(fluid, properties) {
    override fun getName(stack: ItemStack): MutableComponent {
        if (stack.has(NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT.get())) return super.getName(stack).copy().withStyle { style ->
            style
                .withFont(ResourceLocation.fromNamespaceAndPath("nostrum", "alchemical"))
                .withBold(true)
        }
        return super.getName(stack) as MutableComponent
    }
}