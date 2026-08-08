package io.github.riiimc.nostrum.utils

import net.minecraft.core.NonNullList
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.ItemStackHandler

class ResizeStackHandler(defaultSize: Int): ItemStackHandler(defaultSize) {
    fun resize(newSize: Int) {
        if (newSize == stacks.size) return

        val oldStacks = stacks.toList()

        stacks = NonNullList.withSize(newSize, ItemStack.EMPTY)

        oldStacks.take(newSize).forEachIndexed { index, stack ->
            stacks[index] = stack
        }

        onContentsChanged(-1)
    }

    fun compact() {
        val nonEmpty = buildList {
            for (i in 0 until slots) {
                val stack = getStackInSlot(i)
                if (!stack.isEmpty) {
                    add(stack)
                }
            }
        }

        resize(nonEmpty.size)

        for (i in nonEmpty.indices) {
            setStackInSlot(i, nonEmpty[i])
        }
    }
}