package com.settlersofcava.events;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Singleton + Observer
 * Central event dispatcher. Any component can publish events;
 * registered listeners are notified automatically.
 *
 * Mirrors the EventBus from Polymorphia.
 */
public class EventBus {

    private static EventBus instance;
    private final List<GameEventListener> listeners = new ArrayList<>();

    private EventBus() {}

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void register(GameEventListener listener) {
        listeners.add(listener);
    }

    public void unregister(GameEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(GameEvent event, Object payload) {
        for (GameEventListener listener : listeners) {
            listener.onEvent(event, payload);
        }
    }

    /** Convenience overload for events with no payload. */
    public void publish(GameEvent event) {
        publish(event, null);
    }
}

