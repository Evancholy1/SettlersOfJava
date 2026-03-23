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
       return playerList.get(currentPlayerIndex);
    }

    public void advancePhase() {
        phase = switch (phase) {
            case SETUP    -> GamePhase.ROLL;
            case ROLL     -> GamePhase.TRADE;
            case TRADE    -> GamePhase.BUILD;
            case BUILD    -> GamePhase.END_TURN;
            case END_TURN -> GamePhase.ROLL;
            case GAME_OVER -> GamePhase.GAME_OVER;
        };
    }

    public void endTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size();
        phase = GamePhase.ROLL;
    }
}

