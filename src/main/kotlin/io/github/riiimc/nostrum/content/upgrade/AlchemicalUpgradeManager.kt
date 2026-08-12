package io.github.riiimc.nostrum.content.upgrade

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import io.github.riiimc.nostrum.Nostrum
import io.github.riiimc.nostrum.content.upgrade.custom.AlchemicalExpression
import io.github.riiimc.nostrum.helper.DynamicEventBus
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.neoforged.bus.api.Event
import net.neoforged.bus.api.EventPriority
import net.neoforged.neoforge.common.NeoForge
import java.util.function.Consumer

class AlchemicalUpgradeManager : SimpleJsonResourceReloadListener(
    GsonBuilder().create(),
    "upgrades"
) {
    private val upgrades =
        mutableMapOf<ResourceLocation, AlchemicalUpgrade>()


    private val listeners = mutableListOf<Any>()

    private fun registerEvent(
        eventClass: Class<out Event>
    ) {
        val listener = DynamicEventBus.register(
            eventClass
        ) { event ->
            handleEvent(event)
        }

        listeners += listener
    }

    private fun unregisterEvents() {
        for (listener in listeners) {
            NeoForge.EVENT_BUS.unregister(listener)
        }

        listeners.clear()
    }

    override fun apply(
        objects: Map<ResourceLocation, JsonElement>,
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        Nostrum.LOGGER.info(
            "=== AlchemicalUpgradeManager.apply() ==="
        )

        Nostrum.LOGGER.info(
            "Found {} upgrade JSON files",
            objects.size
        )

        for ((id, json) in objects) {
            Nostrum.LOGGER.info(
                "Loading upgrade: {}",
                id
            )
        }
        unregisterEvents()

        upgrades.clear()

        for ((id, json) in objects) {
            AlchemicalUpgrade.CODEC
                .parse(JsonOps.INSTANCE, json)
                .resultOrPartial { error ->
                    Nostrum.LOGGER.error(
                        "Failed to load upgrade {}: {}",
                        id,
                        error
                    )
                }
                .ifPresent { upgrade ->
                    upgrades[id] = upgrade
                }
        }

        registerEvents()

        Nostrum.LOGGER.info(
            "Loaded {} alchemical upgrades",
            upgrades.size
        )
    }



    private fun registerEvents() {
        val eventClasses = upgrades.values
            .flatMap { it.events }
            .map { it.eventClass }
            .toSet()

        Nostrum.LOGGER.info(
            "Registering {} event classes",
            eventClasses.size
        )

        for (eventClass in eventClasses) {
            Nostrum.LOGGER.info(
                "Registering dynamic event: {}",
                eventClass.name
            )

            registerEvent(eventClass)
        }
    }
    private fun handleEvent(event: Event) {
        Nostrum.LOGGER.info(
            "Alchemical event fired: {}",
            event.javaClass.name
        )

        for ((upgradeId, upgrade) in upgrades) {
            for (alchemicalEvent in upgrade.events) {
                if (!alchemicalEvent.eventClass.isAssignableFrom(event.javaClass)) {
                    continue
                }

                val variables =
                    AlchemicalExpression.createVariables(event)

                val conditionsPassed =
                    alchemicalEvent.conditions.all { condition ->
                        try {
                            val result =
                                AlchemicalExpression.evaluateCondition(
                                    condition,
                                    variables
                                )

                            Nostrum.LOGGER.info(
                                "Condition [{}] => {}",
                                condition,
                                result
                            )

                            result
                        } catch (e: Exception) {
                            Nostrum.LOGGER.error(
                                "Failed to evaluate condition '{}' for upgrade {}",
                                condition,
                                upgradeId,
                                e
                            )

                            false
                        }
                    }

                if (!conditionsPassed) {
                    continue
                }

                for (action in alchemicalEvent.actions) {
                    try {
                        AlchemicalExpression.execute(
                            action,
                            variables
                        )
                    } catch (e: Exception) {
                        Nostrum.LOGGER.error(
                            "Failed to execute action '{}' for upgrade {}",
                            action,
                            upgradeId,
                            e
                        )
                    }
                }
            }
        }
    }

    fun get(id: ResourceLocation): AlchemicalUpgrade? =
        upgrades[id]
}