package settlersofjava.engine;

import settlersofjava.board.BoardState;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerColor;
import settlersofjava.player.PlayerList;

import java.util.List;

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

    private String player1Name;
    private String player2Name;

    public static void resetInstance() {
        instance = null;
    }

    private CatanGame(String player1Name, String player2Name) {
        this.currentPhase = GamePhase.SETUP;
        this.playerList = new PlayerList(List.of(
                new Player(player1Name, PlayerColor.RED),
                new Player(player2Name, PlayerColor.BLUE)
        ));
        this.turnManager = new TurnManager(playerList);
    }

    public static CatanGame getInstance(String player1Name, String player2Name) {
        if (instance == null) {
            instance = new CatanGame(player1Name, player2Name);
        }
        return instance;
    }

    public BoardState getBoardState()   { return boardState; }
    public PlayerList getPlayerList()   { return playerList; }
    public GamePhase getCurrentPhase()  { return turnManager.getPhase(); }
    public TurnManager getTurnManager() { return turnManager; }
}

