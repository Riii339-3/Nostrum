package io.github.riiimc.nostrum.content.upgrade

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

data class AlchemicalUpgrade(
    val fluidAmount: Int,
    val target: TagKey<Item>,
    val attributes: List<AttributeData>,
    val events: List<AlchemicalEvent>
) {
    companion object {
        val CODEC: Codec<AlchemicalUpgrade> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.INT
                        .fieldOf("fluid_amount")
                        .forGetter(AlchemicalUpgrade::fluidAmount),

                    TagKey.codec(Registries.ITEM)
                        .fieldOf("target")
                        .forGetter(AlchemicalUpgrade::target),

                    AttributeData.CODEC
                        .listOf()
                        .optionalFieldOf("attributes", emptyList())
                        .forGetter(AlchemicalUpgrade::attributes),

                    AlchemicalEvent.CODEC
                        .listOf()
                        .optionalFieldOf("events", emptyList())
                        .forGetter(AlchemicalUpgrade::events)
                ).apply(
                    instance,
                    ::AlchemicalUpgrade
                )
            }
    }
}