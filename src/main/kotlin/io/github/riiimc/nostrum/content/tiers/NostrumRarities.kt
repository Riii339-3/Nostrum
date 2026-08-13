package io.github.riiimc.nostrum.content.tiers

import io.github.riiimc.nostrum.Nostrum.Companion.rl
import net.minecraft.network.chat.Style
import net.minecraft.world.item.Rarity
import net.neoforged.fml.common.asm.enumextension.EnumProxy
import java.util.function.UnaryOperator


object NostrumRarities {
    @JvmField
    val EPILOGUE_ENUM_PROXY : EnumProxy<Rarity> = EnumProxy<Rarity>(
        Rarity::class.java,
        -1,
        "nostrum:epilogue",
        UnaryOperator { style: Style -> style.withFont(rl("epilogue_move")) } as UnaryOperator<Style?>
    )
}