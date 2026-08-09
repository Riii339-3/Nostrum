package io.github.riiimc.nostrum.content.components

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

enum class AlchemicalPotionForm {
    DRINK,
    SPLASH,
    LINGERING,
    AEROSOL, // 使用すると周囲に霧として散布。
    SPRAY; //前方に短射程で噴射。

    companion object {
        val CODEC: Codec<AlchemicalPotionForm> =
            Codec.STRING.xmap(
                AlchemicalPotionForm::valueOf,
                AlchemicalPotionForm::name
            )

        val STREAM_CODEC: StreamCodec<ByteBuf, AlchemicalPotionForm> =
            ByteBufCodecs.VAR_INT.map(
                { entries[it] },
                { it.ordinal }
            )
    }

}