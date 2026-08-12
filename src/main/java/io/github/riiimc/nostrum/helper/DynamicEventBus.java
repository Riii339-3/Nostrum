package io.github.riiimc.nostrum.helper;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;


import java.util.function.Consumer;

public final class DynamicEventBus {

    private DynamicEventBus() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Consumer register(
            Class<? extends Event> eventClass,
            Consumer<Event> consumer
    ) {

        NeoForge.EVENT_BUS.addListener(
                EventPriority.NORMAL,
                true,
                (Class) eventClass,
                (Consumer) consumer
        );

        return consumer;
    }
}