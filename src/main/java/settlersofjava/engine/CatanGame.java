package settlersofjava.engine;

import settlersofjava.board.BoardState;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerColor;
import settlersofjava.player.PlayerList;

import java.util.ArrayList;
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

    public static void resetInstance() {
        instance = null;
    }

    private CatanGame(List<String> playerNames) {
        this.currentPhase = GamePhase.SETUP;

        if(playerNames.size() > 4 || playerNames.size() < 2) {
            throw new IllegalArgumentException("Must play with 2-4 players");
        }

        List<Player> players = new ArrayList<>();
        PlayerColor[] colors =  PlayerColor.values();

        for(int i = 0; i < playerNames.size(); i++){
            players.add(new Player(playerNames.get(i), colors[i]));
        }
        this.playerList = new PlayerList(players);
        this.turnManager = new TurnManager(playerList);
    }

    public static CatanGame getInstance(List<String> playerNames) {
        if (instance == null) {
            instance = new CatanGame(playerNames);
        }
        return instance;
    }

    public void setBoardState(BoardState boardState) {this.boardState = boardState;}
    public BoardState getBoardState()   { return boardState; }
    public PlayerList getPlayerList()   { return playerList; }
    public GamePhase getCurrentPhase()  { return turnManager.getPhase(); }
    public TurnManager getTurnManager() { return turnManager; }
}

