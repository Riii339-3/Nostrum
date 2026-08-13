package io.github.riiimc.nostrum.content.upgrade

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier

data class AttributeData(
    val attribute: Holder<Attribute>,
    val operation: AttributeModifier.Operation,
    val equipmentSlot: EquipmentSlotGroup,
    val amount: Double
) {
    companion object {

        val CODEC: Codec<AttributeData> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    Attribute.CODEC
                        .fieldOf("attribute")
                        .forGetter(AttributeData::attribute),

                    AttributeModifier.Operation.CODEC
                        .fieldOf("operation")
                        .forGetter(AttributeData::operation),

                    EquipmentSlotGroup.CODEC
                        .fieldOf("equipment_slot")
                        .forGetter(AttributeData::equipmentSlot),

                    Codec.DOUBLE
                        .fieldOf("amount")
                        .forGetter(AttributeData::amount)
                ).apply(
                    instance,
                    ::AttributeData
                )
            }

        val STREAM_CODEC:
                StreamCodec<RegistryFriendlyByteBuf, AttributeData> =
            StreamCodec.composite(
                ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE),
                AttributeData::attribute,

                AttributeModifier.Operation.STREAM_CODEC,
                AttributeData::operation,

                EquipmentSlotGroup.STREAM_CODEC,
                AttributeData::equipmentSlot,

                ByteBufCodecs.DOUBLE,
                AttributeData::amount,

                ::AttributeData
            )
    }
}