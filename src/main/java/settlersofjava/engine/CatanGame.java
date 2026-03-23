package settlersofjava.engine;

import settlersofjava.board.BoardState;
import settlersofjava.player.PlayerList;

/**
 * PATTERN: Singleton
 * Central game engine. Owns BoardState, PlayerList, and current GamePhase.
 * All other components access shared game state through this class.
 */
public class CatanGame {

    private static CatanGame instance;

    private BoardState boardState;
    private PlayerList playerList;
    private GamePhase currentPhase;
    private TurnManager turnManager;

    private CatanGame() {
        // TODO: initialize board via BoardBuilder, create PlayerList
    }

    public static CatanGame getInstance() {
        if (instance == null) {
            instance = new CatanGame();
        }
        return instance;
    }

    public BoardState getBoardState()   { return boardState; }
    public PlayerList getPlayerList()   { return playerList; }
    public GamePhase getCurrentPhase()  { return currentPhase; }
    public TurnManager getTurnManager() { return turnManager; }
}

