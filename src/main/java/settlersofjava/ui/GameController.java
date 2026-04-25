package settlersofjava.ui;

import javafx.scene.control.Button;
import settlersofjava.board.*;
import settlersofjava.buildings.City;
import settlersofjava.buildings.Road;
import settlersofjava.buildings.Settlement;
import settlersofjava.dice.Die;
import settlersofjava.dice.RandomDie;
import settlersofjava.engine.GamePhase;
import settlersofjava.engine.TurnManager;
import settlersofjava.events.EventBus;
import settlersofjava.events.GameEvent;
import settlersofjava.events.GameEventListener;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerList;
import settlersofjava.resources.ResourceBundle;
import settlersofjava.resources.ResourceType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * PATTERN: Dependency Injection
 * Root controller wired by SettlersApp. Drives setup, dice rolling, building,
 * and turn management.
 *
 * PATTERN: Observer — registers with EventBus to react to game events.
 */
public class GameController implements GameEventListener {

    private final BoardState  boardState;
    private final PlayerList  playerList;
    private final TurnManager turnManager;
    private final Consumer<String> statusUpdater;

    private final Die die1 = new RandomDie();
    private final Die die2 = new RandomDie();

    private BoardView       boardView;
    private PlayerDashboard playerDashboard;

    // ── Action buttons ────────────────────────────────────────────────────────
    private Button rollButton;
    private Button endTurnButton;
    private Button buildRoadButton;
    private Button buildSettlementButton;
    private Button buildCityButton;

    // ── Build mode ────────────────────────────────────────────────────────────
    private enum BuildMode { NONE, ROAD, SETTLEMENT, CITY }
    private BuildMode buildMode = BuildMode.NONE;

    // ── Setup sub-phase ───────────────────────────────────────────────────────
    private enum SetupSubPhase { PLACE_SETTLEMENT, PLACE_ROAD }
    private SetupSubPhase setupSubPhase = SetupSubPhase.PLACE_SETTLEMENT;
    private Vertex lastPlacedSettlement;

    // ── Constructor ───────────────────────────────────────────────────────────

    public GameController(BoardState boardState,
                          PlayerList playerList,
                          TurnManager turnManager,
                          Consumer<String> statusUpdater) {
        this.boardState    = boardState;
        this.playerList    = playerList;
        this.turnManager   = turnManager;
        this.statusUpdater = statusUpdater;
        EventBus.getInstance().register(this);
    }

    // ── Wiring setters ────────────────────────────────────────────────────────

    public void setBoardView(BoardView view) {
        this.boardView = view;
        view.setOnVertexClick(this::handleVertexClick);
        view.setOnEdgeClick(this::handleEdgeClick);
    }

    public void setPlayerDashboard(PlayerDashboard dashboard) {
        this.playerDashboard = dashboard;
    }

    public void setActionButtons(Button roll, Button endTurn) {
        this.rollButton    = roll;
        this.endTurnButton = endTurn;
        updateActionButtons();
    }

    public void setBuildButtons(Button road, Button settlement, Button city) {
        this.buildRoadButton       = road;
        this.buildSettlementButton = settlement;
        this.buildCityButton       = city;
        updateBuildButtons();
    }

    public void startSetup() {
        setupSubPhase = SetupSubPhase.PLACE_SETTLEMENT;
        updateHighlights();
        updateStatus();
        updateDashboard();
        updateActionButtons();
        updateBuildButtons();
    }

    // ── Public build-mode toggles (called from SettlersApp button actions) ────

    public void toggleBuildRoad()       { toggle(BuildMode.ROAD); }
    public void toggleBuildSettlement() { toggle(BuildMode.SETTLEMENT); }
    public void toggleBuildCity()       { toggle(BuildMode.CITY); }

    private void toggle(BuildMode mode) {
        if (turnManager.getPhase() != GamePhase.BUILD) return;
        buildMode = (buildMode == mode) ? BuildMode.NONE : mode;
        updateHighlights();
        updateBuildButtons();
        updateStatus();
    }

    // ── Roll / End Turn ───────────────────────────────────────────────────────

    public void rollDice() {
        if (turnManager.getPhase() != GamePhase.ROLL) return;
        int v1 = die1.roll();
        int v2 = die2.roll();
        EventBus.getInstance().publish(GameEvent.DICE_ROLLED, new int[]{v1, v2});
    }

    public void endTurn() {
        if (turnManager.getPhase() != GamePhase.BUILD) return;
        buildMode = BuildMode.NONE;
        updateHighlights();
        turnManager.endTurn();
        EventBus.getInstance().publish(GameEvent.TURN_ENDED);
    }

    // ── Click handlers ────────────────────────────────────────────────────────

    private void handleVertexClick(Vertex v) {
        switch (turnManager.getPhase()) {
            case SETUP -> handleSetupVertexClick(v);
            case BUILD -> handleBuildVertexClick(v);
            default    -> {}
        }
    }

    private void handleEdgeClick(Edge e) {
        switch (turnManager.getPhase()) {
            case SETUP -> handleSetupEdgeClick(e);
            case BUILD -> handleBuildEdgeClick(e);
            default    -> {}
        }
    }

    // ── Setup phase ───────────────────────────────────────────────────────────

    private void handleSetupVertexClick(Vertex v) {
        if (setupSubPhase != SetupSubPhase.PLACE_SETTLEMENT) return;
        if (!isValidSetupSettlement(v)) return;

        Player current = turnManager.getCurrentPlayer();
        v.placeBuilding(new Settlement(current));
        current.addVictoryPoints(1);
        lastPlacedSettlement = v;

        if (turnManager.isSetupRound2()) giveStartingResources(v, current);

        setupSubPhase = SetupSubPhase.PLACE_ROAD;
        updateHighlights();
        updateStatus();
    }

    private void handleSetupEdgeClick(Edge e) {
        if (setupSubPhase != SetupSubPhase.PLACE_ROAD) return;
        if (!isValidSetupRoad(e)) return;

        Player current = turnManager.getCurrentPlayer();
        e.placeRoad(new Road(current));

        setupSubPhase = SetupSubPhase.PLACE_SETTLEMENT;
        lastPlacedSettlement = null;
        turnManager.endSetupTurn();
        updateHighlights();
        updateStatus();
        updateDashboard();
        updateActionButtons();
        updateBuildButtons();
    }

    // ── Build phase ───────────────────────────────────────────────────────────

    private void handleBuildVertexClick(Vertex v) {
        Player p = turnManager.getCurrentPlayer();
        switch (buildMode) {
            case SETTLEMENT -> {
                if (!isValidMainSettlement(v)) return;
                spend(Settlement.COST, p);
                v.placeBuilding(new Settlement(p));
                p.addVictoryPoints(1);
                afterBuild();
            }
            case CITY -> {
                if (!isValidCityUpgrade(v)) return;
                spend(City.COST, p);
                v.upgradeBuilding(new City(p));
                p.addVictoryPoints(1); // net +1VP: settlement was 1, city is 2
                afterBuild();
            }
            default -> {}
        }
    }

    private void handleBuildEdgeClick(Edge e) {
        if (buildMode != BuildMode.ROAD) return;
        Player p = turnManager.getCurrentPlayer();
        if (!isValidMainRoad(e)) return;
        spend(Road.COST, p);
        e.placeRoad(new Road(p));
        afterBuild();
    }

    private void afterBuild() {
        if (boardView != null) boardView.refresh();
        updateHighlights();
        updateBuildButtons();
        updateStatus();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    // Setup: any unoccupied vertex with no occupied neighbour within 1 hop
    private boolean isValidSetupSettlement(Vertex v) {
        if (v.isOccupied()) return false;
        for (Vertex adj : boardState.getAdjacentVertices(v)) {
            if (adj.isOccupied()) return false;
        }
        return true;
    }

    // Setup: road adjacent to the just-placed settlement
    private boolean isValidSetupRoad(Edge e) {
        return !e.hasRoad() && e.connectsVertex(lastPlacedSettlement);
    }

    // Main game: road must extend the player's road/building network
    private boolean isValidMainRoad(Edge e) {
        if (e.hasRoad()) return false;
        Player p = turnManager.getCurrentPlayer();
        if (!p.canAfford(Road.COST)) return false;
        for (Vertex endpoint : List.of(e.getVertexA(), e.getVertexB())) {
            if (endpoint.isOccupied() && endpoint.getBuilding().getOwner() == p) return true;
            for (Edge adj : boardState.getEdgesFor(endpoint)) {
                if (adj != e && adj.hasRoad() && adj.getRoad().getOwner() == p) return true;
            }
        }
        return false;
    }

    // Main game: unoccupied, distance rule, AND adjacent to own road
    private boolean isValidMainSettlement(Vertex v) {
        if (v.isOccupied()) return false;
        Player p = turnManager.getCurrentPlayer();
        if (!p.canAfford(Settlement.COST)) return false;
        for (Vertex adj : boardState.getAdjacentVertices(v)) {
            if (adj.isOccupied()) return false;
        }
        for (Edge e : boardState.getEdgesFor(v)) {
            if (e.hasRoad() && e.getRoad().getOwner() == p) return true;
        }
        return false;
    }

    // Must be own settlement (not already a city), and player can afford it
    private boolean isValidCityUpgrade(Vertex v) {
        if (!v.isOccupied()) return false;
        Player p = turnManager.getCurrentPlayer();
        return v.getBuilding().getOwner() == p
            && v.getBuilding() instanceof Settlement
            && p.canAfford(City.COST);
    }

    // ── Resource helpers ──────────────────────────────────────────────────────

    private void giveStartingResources(Vertex v, Player player) {
        for (HexTile tile : boardState.getTilesFor(v)) {
            if (tile instanceof ResourceTile rt) {
                ResourceType res = rt.getTerrainType().toResourceType();
                if (res != null) player.addResource(res, 1);
            }
        }
    }

    private void distributeResources(int diceTotal) {
        for (HexTile tile : boardState.getActiveTilesFor(diceTotal)) {
            if (!(tile instanceof ResourceTile rt)) continue;
            ResourceType res = rt.getTerrainType().toResourceType();
            if (res == null) continue;
            for (Vertex v : boardState.getVerticesFor(tile.getCoordinate())) {
                if (!v.isOccupied()) continue;
                int amount = v.getBuilding() instanceof City ? 2 : 1;
                v.getBuilding().getOwner().addResource(res, amount);
            }
        }
    }

    private void spend(ResourceBundle cost, Player p) {
        cost.toMap().forEach((type, amount) -> p.removeResource(type, amount));
    }

    // ── Highlight logic ───────────────────────────────────────────────────────

    private void updateHighlights() {
        if (boardView == null) return;

        switch (turnManager.getPhase()) {
            case SETUP -> {
                boardView.setHighlightedCityUpgrades(new HashSet<>());
                if (setupSubPhase == SetupSubPhase.PLACE_SETTLEMENT) {
                    Set<Vertex> valid = new HashSet<>();
                    for (Vertex v : boardState.getVertices()) {
                        if (isValidSetupSettlement(v)) valid.add(v);
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
            case BUILD -> {
                switch (buildMode) {
                    case ROAD -> {
                        boardView.setHighlightedCityUpgrades(new HashSet<>());
                        Set<Edge> valid = new HashSet<>();
                        for (Edge e : boardState.getEdges()) {
                            if (isValidMainRoad(e)) valid.add(e);
                        }
                        boardView.setHighlightedEdges(valid);
                        boardView.setHighlightedVertices(new HashSet<>());
                    }
                    case SETTLEMENT -> {
                        boardView.setHighlightedCityUpgrades(new HashSet<>());
                        Set<Vertex> valid = new HashSet<>();
                        for (Vertex v : boardState.getVertices()) {
                            if (isValidMainSettlement(v)) valid.add(v);
                        }
                        boardView.setHighlightedVertices(valid);
                        boardView.setHighlightedEdges(new HashSet<>());
                    }
                    case CITY -> {
                        boardView.setHighlightedVertices(new HashSet<>());
                        boardView.setHighlightedEdges(new HashSet<>());
                        Set<Vertex> valid = new HashSet<>();
                        for (Vertex v : boardState.getVertices()) {
                            if (isValidCityUpgrade(v)) valid.add(v);
                        }
                        boardView.setHighlightedCityUpgrades(valid);
                    }
                    case NONE -> {
                        boardView.setHighlightedCityUpgrades(new HashSet<>());
                        boardView.setHighlightedVertices(new HashSet<>());
                        boardView.setHighlightedEdges(new HashSet<>());
                    }
                }
            }
            default -> {
                boardView.setHighlightedCityUpgrades(new HashSet<>());
                boardView.setHighlightedVertices(new HashSet<>());
                boardView.setHighlightedEdges(new HashSet<>());
            }
        }
    }

    // ── Status + button update helpers ────────────────────────────────────────

    private void updateStatus() {
        if (statusUpdater == null) return;
        Player p = turnManager.getCurrentPlayer();
        String msg = switch (turnManager.getPhase()) {
            case SETUP -> {
                String action = setupSubPhase == SetupSubPhase.PLACE_SETTLEMENT
                        ? "place a settlement (green dots)"
                        : "place a road (yellow lines)";
                yield p.getName() + "'s turn — " + action;
            }
            case ROLL  -> p.getName() + "'s turn — roll the dice!";
            case BUILD -> buildMode == BuildMode.NONE
                    ? p.getName() + "'s turn — build something or end your turn."
                    : p.getName() + "'s turn — click a highlighted "
                        + buildMode.name().toLowerCase() + " location (or click the button again to cancel).";
            default    -> "";
        };
        statusUpdater.accept(msg);
    }

    private void updateDashboard() {
        if (playerDashboard == null) return;
        playerDashboard.switchToPlayer(turnManager.getCurrentPlayer());
    }

    private void updateActionButtons() {
        if (rollButton    != null) rollButton.setDisable(turnManager.getPhase() != GamePhase.ROLL);
        if (endTurnButton != null) endTurnButton.setDisable(turnManager.getPhase() != GamePhase.BUILD);
    }

    private void updateBuildButtons() {
        boolean isBuild = turnManager.getPhase() == GamePhase.BUILD;
        Player  p       = turnManager.getCurrentPlayer();
        setBuildBtn(buildRoadButton,       BuildMode.ROAD,       isBuild && p.canAfford(Road.COST));
        setBuildBtn(buildSettlementButton, BuildMode.SETTLEMENT, isBuild && p.canAfford(Settlement.COST));
        setBuildBtn(buildCityButton,       BuildMode.CITY,       isBuild && p.canAfford(City.COST));
    }

    private void setBuildBtn(Button btn, BuildMode mode, boolean enabled) {
        if (btn == null) return;
        btn.setDisable(!enabled);
        btn.setStyle(buildMode == mode
                ? "-fx-background-color: #90EE90; -fx-font-weight: bold;"
                : "");
    }

    // ── EventBus callbacks ────────────────────────────────────────────────────

    @Override
    public void onEvent(GameEvent event, Object payload) {
        switch (event) {
            case DICE_ROLLED -> handleDiceRolled(payload);
            case TURN_ENDED  -> handleTurnEnded();
            case GAME_OVER   -> handleGameOver(payload);
            default          -> {}
        }
    }

    private void handleDiceRolled(Object payload) {
        int[] roll  = (int[]) payload;
        int   total = roll[0] + roll[1];
        distributeResources(total);
        turnManager.advancePhase(); // ROLL → TRADE (skipped for now)
        turnManager.advancePhase(); // TRADE → BUILD
        statusUpdater.accept(
            turnManager.getCurrentPlayer().getName()
            + " rolled " + total + " — build something or end your turn."
        );
        updateActionButtons();
        updateBuildButtons();
    }

    private void handleTurnEnded() {
        updateDashboard();
        updateStatus();
        updateActionButtons();
        updateBuildButtons();
    }

    private void handleGameOver(Object payload) {
        // TODO: show winner dialog
    }
}
