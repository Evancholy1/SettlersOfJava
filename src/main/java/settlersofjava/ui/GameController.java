package settlersofjava.ui;

import settlersofjava.board.*;
import settlersofjava.buildings.Road;
import settlersofjava.buildings.Settlement;
import settlersofjava.engine.GamePhase;
import settlersofjava.engine.TurnManager;
import settlersofjava.events.EventBus;
import settlersofjava.events.GameEvent;
import settlersofjava.events.GameEventListener;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerList;
import settlersofjava.resources.ResourceType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * PATTERN: Dependency Injection
 * Root controller wired by SettlersApp. Drives the setup phase and will
 * handle main-game events once setup completes.
 *
 * PATTERN: Observer — registers with EventBus to react to game events.
 */
public class GameController implements GameEventListener {

    private final BoardState  boardState;
    private final PlayerList  playerList;
    private final TurnManager turnManager;
    private final Consumer<String> statusUpdater;

    private BoardView boardView;
    private PlayerDashboard playerDashboard;

    private enum SetupSubPhase { PLACE_SETTLEMENT, PLACE_ROAD }
    private SetupSubPhase setupSubPhase = SetupSubPhase.PLACE_SETTLEMENT;
    private Vertex lastPlacedSettlement;

    /** Constructor injection — called from SettlersApp after building board + players. */
    public GameController(BoardState boardState,
                          PlayerList playerList,
                          TurnManager turnManager,
                          Consumer<String> statusUpdater) {
        this.boardState     = boardState;
        this.playerList     = playerList;
        this.turnManager    = turnManager;
        this.statusUpdater  = statusUpdater;
        EventBus.getInstance().register(this);
    }

    /** Call after setting boardView — kicks off the first setup prompt. */
    public void setBoardView(BoardView view) {
        this.boardView = view;
        view.setOnVertexClick(this::handleVertexClick);
        view.setOnEdgeClick(this::handleEdgeClick);
    }

    public void setPlayerDashboard(PlayerDashboard dashboard) {
        this.playerDashboard = dashboard;
    }

    public void startSetup() {
        setupSubPhase = SetupSubPhase.PLACE_SETTLEMENT;
        updateHighlights();
        updateStatus();
        updateDashboard();
    }

    // ── Setup phase ───────────────────────────────────────────────────────────

    private void handleVertexClick(Vertex v) {
        if (turnManager.getPhase() != GamePhase.SETUP) return;
        if (setupSubPhase != SetupSubPhase.PLACE_SETTLEMENT) return;
        if (!isValidSettlementSpot(v)) return;

        Player current = turnManager.getCurrentPlayer();
        v.placeBuilding(new Settlement(current));
        current.addVictoryPoints(1);
        lastPlacedSettlement = v;

        // Second round: give one resource per adjacent non-desert tile
        if (turnManager.isSetupRound2()) {
            giveStartingResources(v, current);
        }

        setupSubPhase = SetupSubPhase.PLACE_ROAD;
        updateHighlights();
        updateStatus();
    }

    private void handleEdgeClick(Edge e) {
        if (turnManager.getPhase() != GamePhase.SETUP) return;
        if (setupSubPhase != SetupSubPhase.PLACE_ROAD) return;
        if (!isValidRoadSpot(e)) return;

        Player current = turnManager.getCurrentPlayer();
        e.placeRoad(new Road(current));

        setupSubPhase = SetupSubPhase.PLACE_SETTLEMENT;
        lastPlacedSettlement = null;
        turnManager.endSetupTurn();
        updateHighlights();
        updateStatus();
        updateDashboard();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private boolean isValidSettlementSpot(Vertex v) {
        if (v.isOccupied()) return false;
        for (Vertex adj : boardState.getAdjacentVertices(v)) {
            if (adj.isOccupied()) return false;
        }
        return true;
    }

    private boolean isValidRoadSpot(Edge e) {
        return !e.hasRoad() && e.connectsVertex(lastPlacedSettlement);
    }

    // ── Resource distribution (2nd round) ─────────────────────────────────────

    private void giveStartingResources(Vertex v, Player player) {
        for (HexTile tile : boardState.getTilesFor(v)) {
            if (tile instanceof ResourceTile rt) {
                ResourceType res = terrainToResource(rt.getTerrainType());
                if (res != null) player.addResource(res, 1);
            }
        }
    }

    private ResourceType terrainToResource(settlersofjava.board.TerrainType t) {
        return switch (t) {
            case FOREST    -> ResourceType.WOOD;
            case PASTURE   -> ResourceType.SHEEP;
            case FIELDS    -> ResourceType.WHEAT;
            case HILLS     -> ResourceType.BRICK;
            case MOUNTAINS -> ResourceType.ORE;
            case DESERT    -> null;
        };
    }

    // ── Highlight + status helpers ────────────────────────────────────────────

    private void updateHighlights() {
        if (boardView == null) return;
        if (turnManager.getPhase() != GamePhase.SETUP) {
            boardView.setHighlightedVertices(new HashSet<>());
            boardView.setHighlightedEdges(new HashSet<>());
            return;
        }

        if (setupSubPhase == SetupSubPhase.PLACE_SETTLEMENT) {
            Set<Vertex> valid = new HashSet<>();
            for (Vertex v : boardState.getVertices()) {
                if (isValidSettlementSpot(v)) valid.add(v);
            }
            boardView.setHighlightedVertices(valid);
            boardView.setHighlightedEdges(new HashSet<>());
        } else {
            Set<Edge> valid = new HashSet<>();
            for (Edge e : boardState.getEdgesFor(lastPlacedSettlement)) {
                if (!e.hasRoad()) valid.add(e);
            }
            boardView.setHighlightedVertices(new HashSet<>());
            boardView.setHighlightedEdges(valid);
        }
    }

    private void updateStatus() {
        if (statusUpdater == null) return;
        if (turnManager.getPhase() != GamePhase.SETUP) {
            statusUpdater.accept("Setup complete! Starting the game...");
            return;
        }
        Player p = turnManager.getCurrentPlayer();
        String action = setupSubPhase == SetupSubPhase.PLACE_SETTLEMENT
                ? "place a settlement (green dots)"
                : "place a road (yellow lines)";
        statusUpdater.accept(p.getName() + "'s turn — " + action);
    }

    private void updateDashboard() {
        if (playerDashboard == null) return;
        playerDashboard.switchToPlayer(turnManager.getCurrentPlayer());
    }

    // ── Main-game events (stubs for later) ────────────────────────────────────

    @Override
    public void onEvent(GameEvent event, Object payload) {
        switch (event) {
            case DICE_ROLLED     -> handleDiceRolled(payload);
            case BUILDING_PLACED -> handleBuildingPlaced(payload);
            case TURN_ENDED      -> handleTurnEnded();
            case GAME_OVER       -> handleGameOver(payload);
            default              -> {}
        }
    }

    private void handleDiceRolled(Object payload) {
        // TODO: update DiceView, trigger resource distribution
    }

    private void handleBuildingPlaced(Object payload) {
        // TODO: refresh BoardView
    }

    private void handleTurnEnded() {
        // TODO: swap PlayerDashboard to next player
    }

    private void handleGameOver(Object payload) {
        // TODO: show winner dialog
    }
}
