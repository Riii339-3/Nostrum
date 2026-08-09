package io.github.riiimc.nostrum.content.items

import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.content.blockentities.AlchemistCauldronBlockEntity
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Block

class AlchemistWandItem(properties: Properties): Item(properties) {
    override fun useOn(ctx: UseOnContext): InteractionResult {
        val level = ctx.level
        if (level.isClientSide) return super.useOn(ctx)
        println(
            "=== WAND USE ON === " +
                    "side=${if (ctx.level.isClientSide) "CLIENT" else "SERVER"}, " +
                    "shift=${ctx.player?.isShiftKeyDown}"
        )
        if (!level.getBlockState(ctx.clickedPos).`is`(NostrumRegistries.ALCHEMIST_CAULDRON_BLOCK)) return super.useOn(ctx)
        val player = ctx.player ?: return InteractionResult.PASS
        val blockEntity = level.getBlockEntity(ctx.clickedPos)
        if (blockEntity !is AlchemistCauldronBlockEntity) return super.useOn(ctx)
        if (player.isShiftKeyDown) {
            blockEntity.mode = blockEntity.mode.next()
            blockEntity.setChanged()

            if (!level.isClientSide) {
                level.sendBlockUpdated(
                    ctx.clickedPos,
                    level.getBlockState(ctx.clickedPos),
                    level.getBlockState(ctx.clickedPos),
                    Block.UPDATE_CLIENTS
                )
            }
            player.displayClientMessage(
                Component.translatable(
                    "block.nostrum.alchemist_cauldron.mode.${blockEntity.mode.name.lowercase()}"
                ),
                true
            )
        }
        else {
            return InteractionResult.PASS
        }
        return InteractionResult.SUCCESS
    }
}