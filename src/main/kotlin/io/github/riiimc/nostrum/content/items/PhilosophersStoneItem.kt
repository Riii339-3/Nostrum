package io.github.riiimc.nostrum.content.items

import io.github.riiimc.nostrum.Nostrum.Companion.rl
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

class PhilosophersStoneItem(
    properties: Item.Properties
) : Item(
    properties
        .durability(6)
) {
    @OnlyIn(Dist.CLIENT)
    override fun appendHoverText(
        stack: ItemStack,
        tooltip: TooltipContext,
        component: MutableList<Component>,
        flag: TooltipFlag
    ) {
        component.add(Component.translatable("item.nostrum.philosopher_stone.tooltip1").withStyle {style -> style.withFont(rl("epilogue"))})
        component.add(Component.translatable("item.nostrum.philosopher_stone.tooltip2").withStyle {style -> style.withFont(rl("epilogue"))})
        return super.appendHoverText(stack, tooltip, component, flag)
    }
}