package io.github.riiimc.nostrum.content.components

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.riiimc.nostrum.content.upgrade.AlchemicalEvent
import io.github.riiimc.nostrum.content.upgrade.AttributeData
import io.netty.buffer.ByteBuf
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item


data class AlchemicalUpgradeComponent(
    val id: ResourceLocation
) {
    companion object {
        val CODEC: Codec<AlchemicalUpgradeComponent> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    ResourceLocation.CODEC
                        .fieldOf("id")
                        .forGetter(AlchemicalUpgradeComponent::id)
                ).apply(
                    instance,
                    ::AlchemicalUpgradeComponent
                )
            }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AlchemicalUpgradeComponent> =
            StreamCodec.composite(
                ResourceLocation.STREAM_CODEC,
                AlchemicalUpgradeComponent::id,

                ::AlchemicalUpgradeComponent
            )
    }
}