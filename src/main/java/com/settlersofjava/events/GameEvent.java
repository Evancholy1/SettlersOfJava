package com.settlersofcava.events;

/**
 * Game-level events published to the EventBus.
 * Observers (e.g., GameController, BoardView) react to these.
 */
public enum GameEvent {
    DICE_ROLLED,
    RESOURCE_PRODUCED,
    ROBBER_MOVED,
    BUILDING_PLACED,
    ROAD_PLACED,
    DEV_CARD_PLAYED,
    TRADE_COMPLETED,
    TURN_ENDED,
    GAME_OVER
}

