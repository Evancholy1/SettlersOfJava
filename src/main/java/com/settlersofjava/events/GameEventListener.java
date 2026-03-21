package com.settlersofcava.events;

/**
 * PATTERN: Observer
 * Implement this interface to receive game events from the EventBus.
 */
public interface GameEventListener {
    /**
     * Called when a game event occurs.
     * @param event   what happened
     * @param payload contextual data (e.g., the Player who triggered it, dice roll value)
     */
    void onEvent(GameEvent event, Object payload);
}

