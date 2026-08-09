package io.github.riiimc.nostrum.content.components

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

data class AlchemicalPotionContent(
    val form: AlchemicalPotionForm,
) {
    companion object {
        val CODEC: Codec<AlchemicalPotionContent> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    AlchemicalPotionForm.CODEC
                        .fieldOf("form")
                        .forGetter(AlchemicalPotionContent::form)
                ).apply(
                    instance,
                    ::AlchemicalPotionContent
                )
            }

        val STREAM_CODEC: StreamCodec<ByteBuf, AlchemicalPotionContent> =
            StreamCodec.composite(
                AlchemicalPotionForm.STREAM_CODEC,
                AlchemicalPotionContent::form,
                ::AlchemicalPotionContent
            )
    }
}