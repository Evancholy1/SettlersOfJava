package com.settlersofcava.ui;

import com.settlersofcava.board.BoardState;
import com.settlersofcava.events.EventBus;
import com.settlersofcava.events.GameEvent;
import com.settlersofcava.events.GameEventListener;
import com.settlersofcava.player.PlayerList;

/**
 * PATTERN: Dependency Injection
 * The root JavaFX controller. BoardState and PlayerList are injected
 * at construction time — this class never reaches into CatanGame directly.
 *
 * PATTERN: Observer — registers with EventBus to react to game events.
 */
public class GameController implements GameEventListener {

    private final BoardState boardState;
    private final PlayerList playerList;

    // JavaFX @FXML sub-views (wired by FXMLLoader or manually)
    private BoardView boardView;
    private PlayerDashboard playerDashboard;

    /** Constructor injection — called from SettlersApp after building board + players. */
    public GameController(BoardState boardState, PlayerList playerList) {
        this.boardState  = boardState;
        this.playerList  = playerList;
        EventBus.getInstance().register(this);
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {
        switch (event) {
            case DICE_ROLLED      -> handleDiceRolled(payload);
            case BUILDING_PLACED  -> handleBuildingPlaced(payload);
            case TURN_ENDED       -> handleTurnEnded();
            case GAME_OVER        -> handleGameOver(payload);
            default               -> {} // other events handled by sub-views
        }
    }

    private void handleDiceRolled(Object payload) {
        // TODO: update DiceView, trigger resource distribution
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleBuildingPlaced(Object payload) {
        // TODO: refresh BoardView
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleTurnEnded() {
        // TODO: swap PlayerDashboard to next player
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleGameOver(Object payload) {
        // TODO: show winner dialog
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

