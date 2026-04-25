package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.board.*;
import settlersofjava.buildings.City;
import settlersofjava.buildings.Road;
import settlersofjava.buildings.Settlement;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerColor;
import settlersofjava.resources.ResourceBundle;
import settlersofjava.resources.ResourceType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the build-phase mechanics (road, settlement, city upgrade) directly at
 * the game-model level — no JavaFX required.  Mirrors the same pattern as
 * DiceStepDefs: copy the logic from GameController and assert outcomes.
 */
public class BuildStepDefs {

    private BoardState board;
    private Player     player;
    private Player     opponent;

    private Vertex targetVertex;
    private Edge   targetEdge;
    private int    vpBefore;
    private boolean upgradeValid;

    // ── Board setup ───────────────────────────────────────────────────────────

    @Given("a player has a settlement on the board and road resources")
    public void player_has_settlement_and_road_resources() {
        board  = buildBoard();
        player = new Player("Alice", PlayerColor.RED);
        targetVertex = firstFreeVertex();
        targetVertex.placeBuilding(new Settlement(player));
        give(player, Road.COST);
        // pick an edge adjacent to that settlement as the target
        targetEdge = board.getEdgesFor(targetVertex).stream()
                .filter(e -> !e.hasRoad())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No free edge adjacent to vertex"));
    }

    @Given("a player has a road on the board and settlement resources")
    public void player_has_road_and_settlement_resources() {
        board  = buildBoard();
        player = new Player("Alice", PlayerColor.RED);
        // place a road on the first free edge, settlement resources are given
        List<Edge> edges = board.getEdges();
        targetEdge = edges.stream().filter(e -> !e.hasRoad()).findFirst()
                .orElseThrow();
        targetEdge.placeRoad(new Road(player));
        // pick either endpoint that is free and satisfies the distance rule
        for (Vertex ep : List.of(targetEdge.getVertexA(), targetEdge.getVertexB())) {
            if (!ep.isOccupied() && noNeighbourOccupied(ep)) {
                targetVertex = ep;
                break;
            }
        }
        assertNotNull(targetVertex, "Could not find a valid settlement vertex");
        give(player, Settlement.COST);
    }

    @Given("a player has a settlement on the board and city resources")
    public void player_has_settlement_and_city_resources() {
        board  = buildBoard();
        player = new Player("Alice", PlayerColor.RED);
        targetVertex = firstFreeVertex();
        targetVertex.placeBuilding(new Settlement(player));
        player.addVictoryPoints(1);
        vpBefore = player.getVictoryPoints();
        give(player, City.COST);
    }

    @Given("a player has a settlement on the board but not enough city resources")
    public void player_has_settlement_but_insufficient_city_resources() {
        board  = buildBoard();
        player = new Player("Alice", PlayerColor.RED);
        targetVertex = firstFreeVertex();
        targetVertex.placeBuilding(new Settlement(player));
        // give only 1 ore — not the 3 required
        player.addResource(ResourceType.ORE, 1);
    }

    @Given("a player's settlement and an opponent's settlement are on the board")
    public void player_and_opponent_settlements_on_board() {
        board    = buildBoard();
        player   = new Player("Alice", PlayerColor.RED);
        opponent = new Player("Bob",   PlayerColor.BLUE);
        List<Vertex> free = board.getVertices().stream()
                .filter(v -> !v.isOccupied()).toList();
        targetVertex = free.get(0);
        targetVertex.placeBuilding(new Settlement(opponent)); // opponent owns it
        give(player, City.COST); // player has money, but it's not their settlement
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    @When("the player places a road on an adjacent edge")
    public void player_places_road() {
        spend(Road.COST, player);
        targetEdge.placeRoad(new Road(player));
    }

    @When("the player places a settlement at a valid vertex")
    public void player_places_settlement() {
        spend(Settlement.COST, player);
        targetVertex.placeBuilding(new Settlement(player));
        player.addVictoryPoints(1);
    }

    @When("the player upgrades the settlement to a city")
    public void player_upgrades_to_city() {
        spend(City.COST, player);
        targetVertex.upgradeBuilding(new City(player));
        player.addVictoryPoints(1);
    }

    @When("the player checks whether the city upgrade is valid")
    public void player_checks_city_upgrade_valid() {
        upgradeValid = isValidCityUpgrade(targetVertex, player);
    }

    @When("the player checks whether they can upgrade the opponent's settlement")
    public void player_checks_city_upgrade_on_opponents_settlement() {
        upgradeValid = isValidCityUpgrade(targetVertex, player);
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    @Then("the edge has a road owned by the player")
    public void edge_has_road() {
        assertTrue(targetEdge.hasRoad());
        assertEquals(player, targetEdge.getRoad().getOwner());
    }

    @Then("the player has {int} less brick and {int} less wood")
    public void player_has_less_brick_and_wood(int brick, int wood) {
        assertEquals(0, player.getResource(ResourceType.BRICK));
        assertEquals(0, player.getResource(ResourceType.WOOD));
    }

    @Then("the vertex has a settlement owned by the player")
    public void vertex_has_settlement() {
        assertTrue(targetVertex.isOccupied());
        assertInstanceOf(Settlement.class, targetVertex.getBuilding());
        assertEquals(player, targetVertex.getBuilding().getOwner());
    }

    @Then("the player has {int} less brick, {int} less wood, {int} less wheat, and {int} less sheep")
    public void player_has_spent_settlement_cost(int brick, int wood, int wheat, int sheep) {
        assertEquals(0, player.getResource(ResourceType.BRICK));
        assertEquals(0, player.getResource(ResourceType.WOOD));
        assertEquals(0, player.getResource(ResourceType.WHEAT));
        assertEquals(0, player.getResource(ResourceType.SHEEP));
    }

    @Then("the vertex now contains a city owned by the player")
    public void vertex_has_city() {
        assertTrue(targetVertex.isOccupied());
        assertInstanceOf(City.class, targetVertex.getBuilding());
        assertEquals(player, targetVertex.getBuilding().getOwner());
    }

    @Then("the player has {int} less ore and {int} less wheat")
    public void player_has_spent_city_cost(int ore, int wheat) {
        assertEquals(0, player.getResource(ResourceType.ORE));
        assertEquals(0, player.getResource(ResourceType.WHEAT));
    }

    @Then("the player gains a victory point from the upgrade")
    public void player_gained_vp() {
        assertEquals(vpBefore + 1, player.getVictoryPoints());
    }

    @Then("the city upgrade is not valid")
    public void city_upgrade_not_valid() {
        assertFalse(upgradeValid);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BoardState buildBoard() {
        return new BoardBuilder()
                .withShuffledTilesAndNumberTokens()
                .withVerticesAndEdges()
                .build();
    }

    private Vertex firstFreeVertex() {
        return board.getVertices().stream()
                .filter(v -> !v.isOccupied())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No free vertex on board"));
    }

    private boolean noNeighbourOccupied(Vertex v) {
        return board.getAdjacentVertices(v).stream().noneMatch(Vertex::isOccupied);
    }

    /** Mirrors GameController.spend */
    private void spend(ResourceBundle cost, Player p) {
        cost.toMap().forEach((type, amount) -> p.removeResource(type, amount));
    }

    /** Mirrors GameController.isValidCityUpgrade */
    private boolean isValidCityUpgrade(Vertex v, Player p) {
        if (!v.isOccupied()) return false;
        return v.getBuilding().getOwner() == p
                && v.getBuilding() instanceof Settlement
                && p.canAfford(City.COST);
    }

    /** Give a player exactly the resources in a cost bundle */
    private void give(Player p, ResourceBundle cost) {
        cost.toMap().forEach((type, amount) -> p.addResource(type, amount));
    }
}
