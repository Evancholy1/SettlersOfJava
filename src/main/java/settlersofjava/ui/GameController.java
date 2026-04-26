package settlersofjava.ui;

import javafx.scene.control.Button;
import settlersofjava.board.*;
import settlersofjava.buildings.City;
import settlersofjava.buildings.Road;
import settlersofjava.buildings.Settlement;
import settlersofjava.cards.DevelopmentCard;
import settlersofjava.trade.PlayerTradeOffer;
import settlersofjava.trade.PortTradeStrategy;
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

import javafx.collections.MapChangeListener;
import javafx.scene.paint.Color;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
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
    private TradingPanel    tradingPanel;
    private BorderPane gameLayout;
    private boolean isGameOver = false;

    // ── Dev cards ─────────────────────────────────────────────────────────────
    private final List<DevelopmentCard> devCardDeck;
    private boolean devCardPlayedThisTurn = false;

    // ── Largest Army ──────────────────────────────────────────────────────────
    private Player largestArmyOwner = null;
    private int largestArmySize = 2;

    // ── Longest Road ──────────────────────────────────────────────────────────
    private Player longestRoadOwner = null;
    private int longestRoadLength = 4;

    // ── Player trade panels + state ───────────────────────────────────────────
    private PlayerTradeResponsePanel tradeResponsePanel;
    private PlayerTradeSelectPanel   tradeSelectPanel;
    private PlayerTradeOffer         pendingOffer;
    private List<Player>             tradeResponders;
    private int                      tradeResponderIdx;

    // ── Action buttons ────────────────────────────────────────────────────────
    private Button rollButton;
    private Button endTurnButton;
    private Button buildRoadButton;
    private Button buildSettlementButton;
    private Button buildCityButton;
    private Button buildDevCardButton;

    // ── Build mode ────────────────────────────────────────────────────────────
    private enum BuildMode { NONE, ROAD, SETTLEMENT, CITY, ROAD_BUILDING_CARD }
    private BuildMode buildMode = BuildMode.NONE;
    private int freeRoadsLeft = 0;

    // ── Robber mode ───────────────────────────────────────────────────────────
    private boolean robberMode = false;

    // ── Setup sub-phase ───────────────────────────────────────────────────────
    private enum SetupSubPhase { PLACE_SETTLEMENT, PLACE_ROAD }
    private SetupSubPhase setupSubPhase = SetupSubPhase.PLACE_SETTLEMENT;
    private Vertex lastPlacedSettlement;

    // ── Constructor ───────────────────────────────────────────────────────────

    public GameController(BoardState boardState,
                          PlayerList playerList,
                          TurnManager turnManager,
                          Consumer<String> statusUpdater,
                          List<DevelopmentCard> devCardDeck) {
        this.boardState    = boardState;
        this.playerList    = playerList;
        this.turnManager   = turnManager;
        this.statusUpdater = statusUpdater;
        this.devCardDeck   = devCardDeck;
        EventBus.getInstance().register(this);
        // Refresh build buttons whenever any player's resources change
        MapChangeListener<ResourceType, Integer> resourceWatcher =
            change -> updateBuildButtons();
        for (Player p : playerList.getAll()) {
            p.resourcesProperty().addListener(resourceWatcher);
        }
    }

    // ── Wiring setters ────────────────────────────────────────────────────────

    public void setBoardView(BoardView view) {
        this.boardView = view;
        view.setOnVertexClick(this::handleVertexClick);
        view.setOnEdgeClick(this::handleEdgeClick);
        view.setOnTileClick(this::handleTileClick);
    }

    public void setPlayerDashboard(PlayerDashboard dashboard) {
        this.playerDashboard = dashboard;
        dashboard.setOnResourceCardClick(this::openBankTrade);
        dashboard.setOnDevCardPlay(this::playDevCard);
    }

    public void setActionButtons(Button roll, Button endTurn) {
        this.rollButton    = roll;
        this.endTurnButton = endTurn;
        updateActionButtons();
    }

    public void setTradingPanel(TradingPanel panel) {
        this.tradingPanel = panel;
        panel.setOnBankTrade(this::executeBankTrade);
        panel.setOnPropose(this::submitPlayerOffer);
        panel.setOnCancel(() -> {});
    }

    public void setPlayerTradePanels(PlayerTradeResponsePanel response,
                                     PlayerTradeSelectPanel select) {
        this.tradeResponsePanel = response;
        this.tradeSelectPanel   = select;
        response.setOnRespond(this::recordTradeResponse);
        select.setOnExecute(this::executePlayerTrade);
        select.setOnCancel(this::cancelPlayerTrade);
    }

    private void executeBankTrade(Map<ResourceType, Integer> given, ResourceType receiveType) {
        Player current = turnManager.getCurrentPlayer();
        current.addResource(receiveType, 1);
        given.forEach((giveType, count) -> {
            int rate = getBestRate(current, giveType);
            String rateStr = rate == 4 ? "Bank 4:1" : "Port " + rate + ":1";
            log(seg(current.getName(), logColor(current)),
                seg(" traded " + count + " ", Color.web("#444444")),
                seg(capitalize(giveType.name().toLowerCase()), resourceColor(giveType)),
                seg(" for 1 ", Color.web("#444444")),
                seg(capitalize(receiveType.name().toLowerCase()), resourceColor(receiveType)),
                seg(" (" + rateStr + ")", Color.web("#888888")));
        });
        updateDashboard();
    }

    private void submitPlayerOffer(Map<ResourceType, Integer> offering,
                                   Map<ResourceType, Integer> requesting) {
        Player proposer = turnManager.getCurrentPlayer();
        pendingOffer = new PlayerTradeOffer(proposer, offering, requesting);
        tradeResponders   = playerList.getAll().stream().filter(p -> p != proposer).toList();
        tradeResponderIdx = 0;
        advanceTradeResponse();
    }

    private void advanceTradeResponse() {
        if (tradeResponderIdx >= tradeResponders.size()) {
            tradeResponsePanel.hide();
            List<Player> acceptors = pendingOffer.getAcceptors();
            if (acceptors.isEmpty()) {
                log(LogEntry.plain("No one accepted the trade offer.", Color.web("#888888")));
                pendingOffer.getOffering().forEach((t, n) -> pendingOffer.getProposer().addResource(t, n));
                pendingOffer    = null;
                tradeResponders = null;
            } else {
                tradeSelectPanel.show(pendingOffer, acceptors);
            }
            return;
        }
        tradeResponsePanel.show(pendingOffer, tradeResponders.get(tradeResponderIdx));
    }

    private void recordTradeResponse(Player player, PlayerTradeOffer.Response response) {
        pendingOffer.respond(player, response);
        tradeResponderIdx++;
        advanceTradeResponse();
    }

    private void executePlayerTrade(Player acceptor) {
        Player proposer = pendingOffer.getProposer();
        pendingOffer.execute(acceptor, true); // proposer's offering already removed during staging

        // Log offering and requesting sides
        List<LogEntry.Segment> segs = new ArrayList<>();
        segs.add(seg(proposer.getName(), logColor(proposer)));
        segs.add(seg(" traded with ", Color.web("#444444")));
        segs.add(seg(acceptor.getName(), logColor(acceptor)));
        EventBus.getInstance().publish(GameEvent.LOG_MESSAGE, new LogEntry(segs));

        tradeSelectPanel.hide();
        pendingOffer    = null;
        tradeResponders = null;
        updateDashboard();
    }

    private void cancelPlayerTrade() {
        if (pendingOffer != null) {
            pendingOffer.getOffering().forEach((t, n) -> pendingOffer.getProposer().addResource(t, n));
        }
        if (tradeResponsePanel != null) tradeResponsePanel.hide();
        if (tradeSelectPanel   != null) tradeSelectPanel.hide();
        pendingOffer    = null;
        tradeResponders = null;
    }

    public void setBuildButtons(Button road, Button settlement, Button city, Button devCard) {
        this.buildRoadButton       = road;
        this.buildSettlementButton = settlement;
        this.buildCityButton       = city;
        this.buildDevCardButton    = devCard;
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

    public void setGameLayout(BorderPane layout) {
        this.gameLayout = layout;
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
        if (tradingPanel != null) tradingPanel.reset();
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
        log(seg(current.getName(), logColor(current)), seg(" placed a Settlement", Color.web("#444444")));

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
        log(seg(current.getName(), logColor(current)), seg(" placed a Road", Color.web("#444444")));
        checkLongestRoad(current);

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
                log(seg(p.getName(), logColor(p)), seg(" built a Settlement", Color.web("#444444")));
                afterBuild();
                checkWinCondition(p);
            }
            case CITY -> {
                if (!isValidCityUpgrade(v)) return;
                spend(City.COST, p);
                v.upgradeBuilding(new City(p));
                p.addVictoryPoints(1);
                log(seg(p.getName(), logColor(p)), seg(" upgraded to a City", Color.web("#444444")));
                afterBuild();
                checkWinCondition(p);
            }
            default -> {}
        }
    }

    private void handleBuildEdgeClick(Edge e) {
        Player p = turnManager.getCurrentPlayer();

        if (buildMode == BuildMode.ROAD) {
            if (!isValidMainRoad(e)) return;
            spend(Road.COST, p);
            e.placeRoad(new Road(p));
            log(seg(p.getName(), logColor(p)), seg(" built a Road", Color.web("#444444")));
            checkLongestRoad(p);
            afterBuild();

        } else if (buildMode == BuildMode.ROAD_BUILDING_CARD) {
            if (!isValidFreeRoad(e)) return;
            e.placeRoad(new Road(p));
            freeRoadsLeft--;
            log(seg(p.getName(), logColor(p)), seg(" placed a free Road", Color.web("#444444")));
            checkLongestRoad(p);

            if (freeRoadsLeft <= 0) {
                buildMode = BuildMode.NONE;
                statusUpdater.accept(p.getName() + " finished placing free roads. Build something or end your turn.");
            } else {
                statusUpdater.accept(p.getName() + " — place 1 more free road.");
            }
            afterBuild();
        }
    }

    private void afterBuild() {
        if (boardView != null) boardView.refresh();
        updateHighlights();
        updateBuildButtons();
        updateStatus();
        updateDashboard();
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
        if (countRoads(p) >= 15) return false;
        for (Vertex endpoint : List.of(e.getVertexA(), e.getVertexB())) {
            if (endpoint.isOccupied() && endpoint.getBuilding().getOwner() == p) return true;
            for (Edge adj : boardState.getEdgesFor(endpoint)) {
                if (adj != e && adj.hasRoad() && adj.getRoad().getOwner() == p) return true;
            }
        }
        return false;
    }

    // Main game: same as isValidMainRoad but does not check cost
    private boolean isValidFreeRoad(Edge e) {
        if (e.hasRoad()) return false;
        Player p = turnManager.getCurrentPlayer();
        if (countRoads(p) >= 15) return false;
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
        if (countSettlements(p) >= 5) return false;
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
            && p.canAfford(City.COST)
            && countCities(p) < 4;    }

    // ── Piece-count helpers ───────────────────────────────────────────────────

    private long countRoads(Player p) {
        return boardState.getEdges().stream()
                .filter(e -> e.hasRoad() && e.getRoad().getOwner() == p)
                .count();
    }

    private long countSettlements(Player p) {
        return boardState.getVertices().stream()
                .filter(v -> v.isOccupied()
                          && v.getBuilding() instanceof Settlement
                          && v.getBuilding().getOwner() == p)
                .count();
    }

    private long countCities(Player p) {
        return boardState.getVertices().stream()
                .filter(v -> v.isOccupied()
                          && v.getBuilding() instanceof City
                          && v.getBuilding().getOwner() == p)
                .count();
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
        Map<Player, Map<ResourceType, Integer>> gains = new LinkedHashMap<>();
        for (HexTile tile : boardState.getActiveTilesFor(diceTotal)) {
            if (!(tile instanceof ResourceTile rt)) continue;
            ResourceType res = rt.getTerrainType().toResourceType();
            if (res == null) continue;
            for (Vertex v : boardState.getVerticesFor(tile.getCoordinate())) {
                if (!v.isOccupied()) continue;
                int amount = v.getBuilding() instanceof City ? 2 : 1;
                Player owner = v.getBuilding().getOwner();
                owner.addResource(res, amount);
                gains.computeIfAbsent(owner, p -> new LinkedHashMap<>())
                     .merge(res, amount, Integer::sum);
            }
        }
        if (gains.isEmpty()) {
            log(LogEntry.plain("No resources produced.", Color.web("#888888")));
        } else {
            for (var e : gains.entrySet()) {
                Player p = e.getKey();
                List<LogEntry.Segment> segs = new ArrayList<>();
                segs.add(new LogEntry.Segment(p.getName(), logColor(p)));
                segs.add(new LogEntry.Segment(" got ", Color.web("#444444")));
                boolean first = true;
                for (var re : e.getValue().entrySet()) {
                    if (!first) segs.add(new LogEntry.Segment(", ", Color.web("#444444")));
                    segs.add(new LogEntry.Segment(
                        re.getValue() + " " + capitalize(re.getKey().name()), resourceColor(re.getKey())));
                    first = false;
                }
                EventBus.getInstance().publish(GameEvent.LOG_MESSAGE, new LogEntry(segs));
            }
        }
    }

    private void spend(ResourceBundle cost, Player p) {
        cost.toMap().forEach((type, amount) -> p.removeResource(type, amount));
    }

    // ── Highlight logic ───────────────────────────────────────────────────────

    private void updateHighlights() {
        if (boardView == null) return;

        if (robberMode) {
            Set<HexTile> valid = new HashSet<>();
            for (HexTile t : boardState.getTiles())
                if (t != boardState.getRobberTile()) valid.add(t);
            boardView.setHighlightedTiles(valid);
            boardView.setHighlightedCityUpgrades(new HashSet<>());
            boardView.setHighlightedVertices(new HashSet<>());
            boardView.setHighlightedEdges(new HashSet<>());
            return;
        }
        boardView.setHighlightedTiles(new HashSet<>());

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
                    case ROAD_BUILDING_CARD -> {
                        boardView.setHighlightedCityUpgrades(new HashSet<>());
                        boardView.setHighlightedVertices(new HashSet<>());
                        Set<Edge> valid = new HashSet<>();
                        for (Edge e : boardState.getEdges()) {
                            if (isValidFreeRoad(e)) valid.add(e);
                        }
                        boardView.setHighlightedEdges(valid);
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
        if (endTurnButton != null) endTurnButton.setDisable(turnManager.getPhase() != GamePhase.BUILD || robberMode);
    }

    private void updateBuildButtons() {
        boolean isBuild = turnManager.getPhase() == GamePhase.BUILD && !robberMode;
        Player  p       = turnManager.getCurrentPlayer();
        setBuildBtn(buildRoadButton,       BuildMode.ROAD,       isBuild && p.canAfford(Road.COST));
        setBuildBtn(buildSettlementButton, BuildMode.SETTLEMENT, isBuild && p.canAfford(Settlement.COST));
        setBuildBtn(buildCityButton,       BuildMode.CITY,       isBuild && p.canAfford(City.COST));
        if (buildDevCardButton != null) {
            buildDevCardButton.setDisable(!(isBuild && p.canAfford(settlersofjava.cards.DevelopmentCard.COST)));
        }
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
        Player current = turnManager.getCurrentPlayer();
        if (total == 7) {
            log(seg(current.getName(), logColor(current)),
                seg(" rolled ", Color.web("#444444")),
                seg("7", Color.FIREBRICK),
                seg(" — Robber moves!", Color.web("#444444")));

            // players with over 8 cards gets half (rounded down) of their cards randomly discarded
            for (Player p : playerList.getAll()) {
                int totalResources = 0;
                for (ResourceType type : ResourceType.values()) {
                    totalResources += p.getResource(type);
                }

                if (totalResources >= 8) {
                    int toDiscard = totalResources / 2;

                    // pool all resources
                    List<ResourceType> pool = new ArrayList<>();
                    for (ResourceType type : ResourceType.values()) {
                        for (int i = 0; i < p.getResource(type); i++) {
                            pool.add(type);
                        }
                    }
                    Random rand = new Random();
                    for (int i = 0; i < toDiscard; i++) {
                        int index = rand.nextInt(pool.size());
                        ResourceType typeToDiscard = pool.remove(index);
                        p.removeResource(typeToDiscard, 1);
                    }

                    log(seg(p.getName(), logColor(p)),
                            seg(" had " + totalResources + " cards and discarded " + toDiscard + " due to the robber.", Color.web("#444444")));
                }
            }
            robberMode = true;
            turnManager.advancePhase(); // ROLL → TRADE
            turnManager.advancePhase(); // TRADE → BUILD
            updateHighlights();
            updateActionButtons();
            updateBuildButtons();
            statusUpdater.accept(current.getName() + " rolled 7 — move the robber to a new tile!");
        } else {
            log(seg(current.getName(), logColor(current)),
                seg(" rolled ", Color.web("#444444")),
                seg(String.valueOf(total), total == 6 || total == 8 ? Color.web("#B8860B") : Color.web("#222222")));
            distributeResources(total);
            turnManager.advancePhase();
            turnManager.advancePhase();
            statusUpdater.accept(current.getName() + " rolled " + total + " — build something or end your turn.");
            updateActionButtons();
            updateBuildButtons();
        }
    }

    private void handleTileClick(HexTile tile) {
        if (!robberMode) return;
        if (tile == boardState.getRobberTile()) return; // must move to a new tile

        boardState.moveRobber(tile);
        EventBus.getInstance().publish(GameEvent.ROBBER_MOVED, tile);

        Player current = turnManager.getCurrentPlayer();
        log(seg(current.getName(), logColor(current)), seg(" moved the Robber", Color.web("#444444")));

        List<Player> targets = boardState.getVerticesFor(tile.getCoordinate()).stream()
                .filter(Vertex::isOccupied)
                .map(v -> v.getBuilding().getOwner())
                .filter(p -> p != current)
                .distinct()
                .toList();
        if (!targets.isEmpty()) {
            Player victim = targets.get(new Random().nextInt(targets.size()));
            stealRandomResource(victim, current).ifPresent(stolen ->
                log(seg(current.getName(), logColor(current)),
                    seg(" stole 1 ", Color.web("#444444")),
                    seg(capitalize(stolen.name()), resourceColor(stolen)),
                    seg(" from ", Color.web("#444444")),
                    seg(victim.getName(), logColor(victim))));
            updateDashboard();
        }

        robberMode = false;
        updateHighlights();
        updateActionButtons();
        updateBuildButtons();
        updateStatus();
    }

    private void openBankTrade(ResourceType giveType) {
        if (turnManager.getPhase() != GamePhase.BUILD) return;
        Player current = turnManager.getCurrentPlayer();
        if (current.getResource(giveType) < 1) return;
        if (tradingPanel != null) tradingPanel.stageResource(giveType, current, getBestRate(current, giveType));
    }

    private int getBestRate(Player player, ResourceType type) {
        int best = 4;
        for (Vertex v : boardState.getVertices()) {
            if (!v.isOccupied() || v.getBuilding().getOwner() != player || !v.hasPort()) continue;
            int rate = new PortTradeStrategy(v.getPort()).getRate(type);
            if (rate < best) best = rate;
        }
        return best;
    }

    private Optional<ResourceType> stealRandomResource(Player from, Player to) {
        List<ResourceType> pool = new ArrayList<>();
        for (ResourceType type : ResourceType.values())
            for (int i = 0; i < from.getResource(type); i++) pool.add(type);
        if (pool.isEmpty()) return Optional.empty();
        ResourceType stolen = pool.get(new Random().nextInt(pool.size()));
        from.removeResource(stolen, 1);
        to.addResource(stolen, 1);
        return Optional.of(stolen);
    }

    private void handleTurnEnded() {
        devCardPlayedThisTurn = false;
        updateDashboard();
        updateStatus();
        updateActionButtons();
        updateBuildButtons();
    }

    private void handleGameOver(Object payload) {
        if (payload instanceof Player winner) {
            if (gameLayout != null) {
                gameLayout.setCenter(new GameOverPanel(winner, playerList.getAll()));
                gameLayout.setTop(null);
                gameLayout.setBottom(null);
            }

            log(LogEntry.plain("=================================", Color.FIREBRICK));
            log(seg("GAME OVER! ", Color.FIREBRICK),
                    seg(winner.getName() + " wins with " + winner.getTotalVictoryPoints() + " VPs!", logColor(winner)));

            if (statusUpdater != null) {
                statusUpdater.accept("Game Over! " + winner.getName() + " has won the game.");
            }
        }
    }

    public void purchaseDevCard() {
        if (turnManager.getPhase() != GamePhase.BUILD) return;
        Player p = turnManager.getCurrentPlayer();

        if (!p.canAfford(DevelopmentCard.COST)) return;

        if (devCardDeck.isEmpty()) {
            log(LogEntry.plain("The Development Card deck is empty!", Color.FIREBRICK));
            return;
        }

        DevelopmentCard drawn = devCardDeck.removeFirst();

        spend(DevelopmentCard.COST, p);
        p.addDevCard(drawn);

        // NOTE: VP cards are explicitly NOT added to the player's public score here to keep them hidden.

        log(seg(p.getName(), logColor(p)), seg(" bought a Development Card.", Color.web("#444444")));
        updateBuildButtons();
        updateDashboard();

        checkWinCondition(p);
    }

    private void playDevCard(settlersofjava.cards.DevelopmentCard card) {
        if (turnManager.getPhase() != GamePhase.BUILD) {
            log(LogEntry.plain("You must roll the dice before playing a Development Card.", Color.FIREBRICK));
            return;
        }
        if (devCardPlayedThisTurn) {
            log(LogEntry.plain("You can only play one Development Card per turn.", Color.FIREBRICK));
            return;
        }

        Player p = turnManager.getCurrentPlayer();
        card.play(p);
        devCardPlayedThisTurn = true;
        log(seg(p.getName(), logColor(p)), seg(" played ", Color.web("#444444")), seg(card.getCardName(), Color.web("#B8860B")));

        if (card instanceof settlersofjava.cards.KnightCard) {
            p.incrementArmySize();
            checkLargestArmy(p);
            robberMode = true;
            updateHighlights();
            updateActionButtons();
            updateBuildButtons();
            statusUpdater.accept(p.getName() + " played a Knight — move the robber to a new tile!");
        } else if (card instanceof settlersofjava.cards.RoadBuildingCard) {
            p.removeDevCard(card);
            buildMode = BuildMode.ROAD_BUILDING_CARD;
            freeRoadsLeft = 2;
            updateHighlights();
            updateBuildButtons();
            statusUpdater.accept(p.getName() + " played Road Building — place 2 free roads.");
        }

        updateDashboard();
    }

    private void checkLargestArmy(Player p) {
        if (p.getArmySize() > largestArmySize) {
            largestArmySize = p.getArmySize();
            if (largestArmyOwner != p) {
                if (largestArmyOwner != null) {
                    largestArmyOwner.setHasLargestArmy(false);
                    log(seg(largestArmyOwner.getName(), logColor(largestArmyOwner)),
                            seg(" lost the Largest Army.", Color.web("#888888")));
                }
                largestArmyOwner = p;
                p.setHasLargestArmy(true);
                log(seg(p.getName(), logColor(p)),
                        seg(" took the Largest Army! (+2 VP)", Color.web("#B8860B")));
                updateDashboard();
                checkWinCondition(p);
            }
        }
    }

    private void checkLongestRoad(Player p) {
        int length = LongestRoadCalculator.compute(p, boardState.getVertices(), boardState.getEdges());
        if (length >= 5 && length > longestRoadLength) {
            longestRoadLength = length;
            if (longestRoadOwner != p) {
                if (longestRoadOwner != null) {
                    longestRoadOwner.setHasLongestRoad(false);
                    log(seg(longestRoadOwner.getName(), logColor(longestRoadOwner)),
                            seg(" lost the Longest Road.", Color.web("#888888")));
                }
                longestRoadOwner = p;
                p.setHasLongestRoad(true);
                log(seg(p.getName(), logColor(p)),
                        seg(" took the Longest Road! (+2 VP)", Color.web("#B8860B")));
                updateDashboard();
                checkWinCondition(p);
            }
        }
    }

    private void checkWinCondition(Player p) {
        if (isGameOver) return;

        if (p.getTotalVictoryPoints() >= 10) {
            isGameOver = true;
            EventBus.getInstance().publish(GameEvent.GAME_OVER, p);
        }
    }

    // ── Log helpers ───────────────────────────────────────────────────────────

    private void log(LogEntry.Segment... segments) {
        EventBus.getInstance().publish(GameEvent.LOG_MESSAGE, new LogEntry(List.of(segments)));
    }

    private void log(LogEntry entry) {
        EventBus.getInstance().publish(GameEvent.LOG_MESSAGE, entry);
    }

    private LogEntry.Segment seg(String text, Color color) {
        return new LogEntry.Segment(text, color);
    }

    private Color logColor(Player p) {
        return switch (p.getColor()) {
            case RED    -> Color.web("#C62828");
            case BLUE   -> Color.web("#1565C0");
            case BLACK  -> Color.web("#212121");
            case ORANGE -> Color.web("#E65100");
        };
    }

    private Color resourceColor(ResourceType type) {
        return switch (type) {
            case WOOD  -> Color.FORESTGREEN;
            case BRICK -> Color.FIREBRICK;
            case SHEEP -> Color.web("#2E7D32");
            case WHEAT -> Color.web("#B8860B");
            case ORE   -> Color.web("#546E7A");
        };
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : s.charAt(0) + s.substring(1).toLowerCase();
    }
}
