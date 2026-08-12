package io.github.riiimc.nostrum.content.upgrade

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.Event
import java.util.Optional
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent

data class AlchemicalEvent(
    val eventClass: Class<out Event>,
    val conditions: List<String>,
    val actions: List<String>
) {
    companion object {
        private fun resolveEventClass(
            name: String
        ): Class<out Event> {
            val clazz = Class.forName(
                name,
                true,
                Thread.currentThread().contextClassLoader
            )

            if (!Event::class.java.isAssignableFrom(clazz)) {
                throw IllegalArgumentException(
                    "$name is not a subclass of Event"
                )
            }

            @Suppress("UNCHECKED_CAST")
            return clazz as Class<out Event>
        }

        private val EVENT_CODEC: Codec<Class<out Event>> =
            Codec.STRING.comapFlatMap(
                { name ->
                    try {
                        DataResult.success(
                            resolveEventClass(name)
                        )
                    } catch (e: Exception) {
                        DataResult.error {
                            "Unknown Event class: $name (${e.message})"
                        }
                    }
                },
                Class<out Event>::getName
            )
        val CODEC: Codec<AlchemicalEvent> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    EVENT_CODEC
                        .fieldOf("event")
                        .forGetter(AlchemicalEvent::eventClass),

                    Codec.STRING
                        .listOf()
                        .optionalFieldOf("conditions", emptyList())
                        .forGetter(AlchemicalEvent::conditions),

                    Codec.STRING
                        .listOf()
                        .fieldOf("actions")
                        .forGetter(AlchemicalEvent::actions)
                ).apply(
                    instance,
                    ::AlchemicalEvent
                )
            }


    }
}