package settlersofjava.engine;

import settlersofjava.player.PlayerList;
import settlersofjava.player.Player;

/**
 * Tracks whose turn it is and manages phase transitions within a turn.
 */
public class TurnManager {

    private final PlayerList playerList;
    private int currentPlayerIndex;
    private GamePhase phase;
    private int setupTurnIndex; // 0 .. 2*n-1 for snake order

    public TurnManager(PlayerList playerList) {
        this.playerList = playerList;
        this.currentPlayerIndex = 0;
        this.phase = GamePhase.SETUP;
        this.setupTurnIndex = 0;
    }

    public Player getCurrentPlayer() {
       return playerList.get(currentPlayerIndex);
    }

    /**
     * Advances to the next setup turn in snake order:
     * P0→P1→...→Pn-1→Pn-1→...→P1→P0, then transitions to ROLL.
     */
    public void endSetupTurn() {
        setupTurnIndex++;
        int n = playerList.size();
        if (setupTurnIndex >= 2 * n) {
            phase = GamePhase.ROLL;
            currentPlayerIndex = 0;
            return;
        }
        if (setupTurnIndex < n) {
            currentPlayerIndex = setupTurnIndex;
        } else {
            currentPlayerIndex = 2 * n - 1 - setupTurnIndex;
        }
    }

    /** True during the second round of setup (reverse order), when 2nd settlements give resources. */
    public boolean isSetupRound2() {
        return setupTurnIndex >= playerList.size();
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
        getCurrentPlayer().unlockDevCards();

        currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size();
        phase = GamePhase.ROLL;
    }

    public int getCurrentPlayerIndex() { return currentPlayerIndex; }

    public GamePhase getPhase(){
        return this.phase;
    }
}

