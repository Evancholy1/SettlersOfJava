package com.settlersofcava.engine;

import com.settlersofcava.player.PlayerList;
import com.settlersofcava.player.Player;

/**
 * Tracks whose turn it is and manages phase transitions within a turn.
 */
public class TurnManager {

    private final PlayerList playerList;
    private int currentPlayerIndex;
    private GamePhase phase;

    public TurnManager(PlayerList playerList) {
        this.playerList = playerList;
        this.currentPlayerIndex = 0;
        this.phase = GamePhase.SETUP;
    }

    public Player getCurrentPlayer() {
        // TODO: return playerList.get(currentPlayerIndex)
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void advancePhase() {
        // TODO: transition phase state machine
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void endTurn() {
        // TODO: advance to next player, reset to ROLL phase
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

