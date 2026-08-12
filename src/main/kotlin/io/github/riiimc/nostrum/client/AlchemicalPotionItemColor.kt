package io.github.riiimc.nostrum.client

import net.minecraft.client.color.item.ItemColor
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.alchemy.PotionContents

class AlchemicalPotionItemColor: ItemColor {
    override fun getColor(
        stack: ItemStack,
        tintIndex: Int
    ): Int {

        // tintされる部分以外
        if (tintIndex != 0) {
            return 0xFFFFFFFF.toInt()
        }

        val contents = stack.getOrDefault(
            DataComponents.POTION_CONTENTS,
            PotionContents.EMPTY
        )

        var r = 0
        var g = 0
        var b = 0
        var count = 0

        contents.forEachEffect { effect ->

            val color = effect.effect.value().color

            r += (color shr 16) and 0xFF
            g += (color shr 8) and 0xFF
            b += color and 0xFF

            count++
        }

        if (count == 0) {
            return 0xFFFFFFFF.toInt()
        }

        r /= count
        g /= count
        b /= count

        return (0xFF shl 24) or
                (r shl 16) or
                (g shl 8) or
                b
    }

}