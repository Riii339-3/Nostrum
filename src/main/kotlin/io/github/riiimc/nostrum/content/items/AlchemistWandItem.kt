package io.github.riiimc.nostrum.content.items

import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.content.blockentities.AlchemistCauldronBlockEntity
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Block

class AlchemistWandItem(
    properties: Properties
) : Item(properties) {

    override fun useOn(ctx: UseOnContext): InteractionResult {
        val level = ctx.level
        val pos = ctx.clickedPos
        val player = ctx.player ?: return InteractionResult.PASS

        if (!level.getBlockState(pos).`is`(NostrumRegistries.ALCHEMIST_CAULDRON_BLOCK)) {
            return InteractionResult.PASS
        }

        if (!player.isShiftKeyDown) {
            return InteractionResult.PASS
        }

        if (level.isClientSide) {
            return InteractionResult.sidedSuccess(true)
        }

        val blockEntity = level.getBlockEntity(pos)
                as? AlchemistCauldronBlockEntity
            ?: return InteractionResult.PASS

        blockEntity.mode = blockEntity.mode.next()
        blockEntity.setChanged()

        val state = level.getBlockState(pos)

        level.sendBlockUpdated(
            pos,
            state,
            state,
            Block.UPDATE_CLIENTS
        )

        player.displayClientMessage(
            Component.translatable(
                "block.nostrum.alchemist_cauldron.mode.${blockEntity.mode.name.lowercase()}"
            ),
            true
        )

        return InteractionResult.sidedSuccess(false)
    }
}