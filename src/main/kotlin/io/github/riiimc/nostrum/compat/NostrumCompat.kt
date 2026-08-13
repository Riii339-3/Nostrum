package io.github.riiimc.nostrum.compat

import io.github.riiimc.nostrum.compat.ageratum.NostrumAgeratumRegistries
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList

object NostrumCompat {
    fun isModLoaded(modId: String): Boolean = ModList.get().isLoaded(modId)
    fun addonRegistry(bus: IEventBus) {
        NostrumAgeratumRegistries.registry(bus)
    }
}