package io.github.riiimc.nostrum.compat.ageratum.items

import dev.anvilcraft.resource.ageratum.Ageratum
import io.github.riiimc.nostrum.Nostrum
import io.github.riiimc.nostrum.Nostrum.Companion.rl
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class GuideBookItem(properties: Properties): Item(properties) {
    companion object {
        val DOC_LOCATION = rl("index")
    }
    override fun use(
        level: Level,
        player: Player,
        hand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)

        if (!level.isClientSide && player is ServerPlayer) {
            Ageratum.openGuide(player, DOC_LOCATION)
        }

        return InteractionResultHolder.sidedSuccess(
            stack,
            level.isClientSide
        )
    }

    override fun appendHoverText(
        stack: ItemStack,
        tooltip: TooltipContext,
        components: MutableList<Component?>,
        flag: TooltipFlag
    ) {
        components.add(Component.translatable("item.nostrum.guide_book.tooltip").withStyle(ChatFormatting.DARK_AQUA))
        return super.appendHoverText(stack, tooltip, components, flag)
    }

}