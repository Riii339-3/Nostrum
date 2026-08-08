package io.github.riiimc.nostrum.content.blocks

import io.github.riiimc.nostrum.NostrumRegistries
import io.github.riiimc.nostrum.content.blockentities.AlchemistCauldronBlockEntity
import io.github.riiimc.nostrum.content.recipes.AlchemistCauldronMode
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler

class AlchemistCauldronBlock(properties: Properties): Block(properties), EntityBlock {
    override fun newBlockEntity(p0: BlockPos, p1: BlockState): BlockEntity {
        return AlchemistCauldronBlockEntity(p0, p1)
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {
        if (!player.isShiftKeyDown) return super.useWithoutItem(state, level, pos, player, hit)
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity !is AlchemistCauldronBlockEntity) return super.useWithoutItem(state, level, pos, player, hit)
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) return super.useWithoutItem(state, level, pos, player, hit)

        if (blockEntity.inventory.slots == 0) return super.useWithoutItem(state, level, pos, player, hit)
        player.setItemInHand(InteractionHand.MAIN_HAND, blockEntity.inventory.getStackInSlot(blockEntity.inventory.slots - 1))
        blockEntity.inventory.setStackInSlot(blockEntity.inventory.slots - 1, ItemStack.EMPTY)
        blockEntity.inventory.resize(blockEntity.inventory.slots - 1)
        return InteractionResult.SUCCESS
    }

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        result: BlockHitResult
    ): ItemInteractionResult {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity !is AlchemistCauldronBlockEntity) return super.useItemOn(stack, state, level, pos, player, hand, result)
        when (stack.item) {
            NostrumRegistries.ALCHEMIST_WAND.get() -> {
                if (player.isShiftKeyDown) {
                    blockEntity.mode.next()
                    player.displayClientMessage(
                        Component.translatable(
                            "block.nostrum.alchemist_cauldron.mode.${blockEntity.mode.name.lowercase()}"
                        ),
                        true
                    )
                    return ItemInteractionResult.SUCCESS
                }

                val recipe = blockEntity.checkRecipe()

                if (level.isClientSide) {
                    clientParticle(level, pos, recipe != null)
                    return ItemInteractionResult.SUCCESS
                }

                if (recipe == null) {
                    for (i in 0 until blockEntity.inventory.slots) {
                        blockEntity.inventory
                            .getStackInSlot(i)
                            .shrink(1)
                    }
                    blockEntity.fluid = FluidStack.EMPTY
                    blockEntity.inventory.compact()
                    blockEntity.setChanged()
                    return ItemInteractionResult.SUCCESS
                }

                val input = blockEntity.createRecipeInput()

                val resultStack = recipe.assemble(
                    input,
                    level.registryAccess()
                )

                // 材料を消費
                for (i in 0 until blockEntity.inventory.slots) {
                    blockEntity.inventory
                        .getStackInSlot(i)
                        .shrink(1)
                }

                // 液体を消費
                blockEntity.fluid.shrink(recipe.inputFluid.amount)

                if (blockEntity.fluid.isEmpty) {
                    blockEntity.fluid = FluidStack.EMPTY
                }

                // 結果をプレイヤーへ
                val itemEntity = ItemEntity(
                    level,
                    pos.x + 0.5,
                    pos.y + 1.2,
                    pos.z + 0.5,
                    resultStack
                )

                itemEntity.setDefaultPickUpDelay()

                level.addFreshEntity(itemEntity)

                blockEntity.inventory.compact()
                blockEntity.setChanged()

                return ItemInteractionResult.SUCCESS
            }
            else -> {
                val handItem = player.getItemInHand(hand)
                if (handItem.isEmpty) return super.useItemOn(stack, state, level, pos, player, hand, result)
                val handler = handItem.getCapability(
                    Capabilities.FluidHandler.ITEM,
                    null
                )
                if (handler != null) {

                    val contained = handler.getFluidInTank(0)

                    if (contained.isEmpty) {
                        // ItemStack が空 → BlockEntity から回収
                        val stored = blockEntity.fluid

                        if (stored.isEmpty) {
                            return super.useItemOn(
                                stack,
                                state,
                                level,
                                pos,
                                player,
                                hand,
                                result
                            )
                        }

                        val amount = handler.fill(
                            stored.copy(),
                            IFluidHandler.FluidAction.SIMULATE
                        )

                        if (amount <= 0) {
                            return super.useItemOn(
                                stack,
                                state,
                                level,
                                pos,
                                player,
                                hand,
                                result
                            )
                        }

                        val toFill = stored.copy()
                        toFill.amount = amount

                        val filled = handler.fill(
                            toFill,
                            IFluidHandler.FluidAction.EXECUTE
                        )

                        if (filled <= 0) {
                            return super.useItemOn(
                                stack,
                                state,
                                level,
                                pos,
                                player,
                                hand,
                                result
                            )
                        }

                        stored.shrink(filled)

                        if (stored.amount <= 0) {
                            blockEntity.fluid = FluidStack.EMPTY
                        }

                        player.setItemInHand(hand, handler.container)
                        blockEntity.setChanged()

                        return ItemInteractionResult.SUCCESS
                    } else {
                        // ItemStack に Fluid が入っている → BlockEntity へ注入
                        val capacity = 2000
                        val current = blockEntity.fluid

                        if (!current.isEmpty &&
                            !FluidStack.isSameFluidSameComponents(current, contained)
                        ) {
                            return super.useItemOn(
                                stack,
                                state,
                                level,
                                pos,
                                player,
                                hand,
                                result
                            )
                        }

                        val remaining = capacity - current.amount

                        if (remaining <= 0) {
                            return super.useItemOn(
                                stack,
                                state,
                                level,
                                pos,
                                player,
                                hand,
                                result
                            )
                        }

                        val drainAmount = minOf(contained.amount, remaining)

                        val drained = handler.drain(
                            drainAmount,
                            IFluidHandler.FluidAction.EXECUTE
                        )

                        if (drained.isEmpty) {
                            return super.useItemOn(
                                stack,
                                state,
                                level,
                                pos,
                                player,
                                hand,
                                result
                            )
                        }

                        if (current.isEmpty) {
                            blockEntity.fluid = drained.copy()
                        } else {
                            current.grow(drained.amount)
                        }

                        player.setItemInHand(hand, handler.container)
                        blockEntity.setChanged()

                        return ItemInteractionResult.SUCCESS
                    }
                }
                else {
                    val inserted = handItem.copyWithCount(1)

                    blockEntity.inventory.resize(blockEntity.inventory.slots + 1)
                    blockEntity.inventory.setStackInSlot(
                        blockEntity.inventory.slots - 1,
                        inserted
                    )

                    handItem.shrink(1)

                    blockEntity.setChanged()
                }
                return ItemInteractionResult.SUCCESS
            }

        }

    }

    fun clientParticle(level: Level, pos: BlockPos, result: Boolean) {

        if (result) {

        }
        else {
            level.addParticle(
                ParticleTypes.EXPLOSION,
                pos.x + 0.5,
                pos.y + 1.0,
                pos.z + 0.5,
                0.0,
                0.0,
                0.0
            )}
    }
    companion object {
        private val SHAPE = Shapes.or(
            // 底
            Block.box(
                0.0, 0.0, 0.0,
                16.0, 3.0, 16.0
            ),

            // 西側
            Block.box(
                0.0, 0.0, 0.0,
                3.0, 16.0, 16.0
            ),

            // 東側
            Block.box(
                13.0, 0.0, 0.0,
                16.0, 16.0, 16.0
            ),

            // 北側
            Block.box(
                3.0, 0.0, 0.0,
                13.0, 16.0, 3.0
            ),

            // 南側
            Block.box(
                3.0, 0.0, 13.0,
                13.0, 16.0, 16.0
            )
        )
    }

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return SHAPE
    }

}