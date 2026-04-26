package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.board.*;
import settlersofjava.buildings.Settlement;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerColor;
import settlersofjava.resources.ResourceType;
import settlersofjava.trade.PortTradeStrategy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests port trade rates and execution at the model layer — no JavaFX.
 * Mirrors GameController.getBestRate and the resource mutation done on confirm.
 */
public class TradeStepDefs {

    // ── PortTradeStrategy state ───────────────────────────────────────────────
    private PortType portType;
    private int      computedRate;

    // ── Best-rate selection state ─────────────────────────────────────────────
    private BoardState rateBoard;
    private Player     ratePlayer;
    private int        bestRate;

    // ── Trade execution state ─────────────────────────────────────────────────
    private Player tradePlayer;

    // ── PortTradeStrategy steps ───────────────────────────────────────────────

    @Given("the port type is {word}")
    public void the_port_type_is(String portTypeName) {
        portType = PortType.valueOf(portTypeName);
    }

    @When("the trade rate for {word} is looked up")
    public void the_trade_rate_for_resource_is_looked_up(String resourceName) {
        computedRate = new PortTradeStrategy(portType).getRate(ResourceType.valueOf(resourceName));
    }

    @Then("the rate is {int}")
    public void the_rate_is(int expected) {
        assertEquals(expected, computedRate);
    }

    // ── Best-rate selection steps ─────────────────────────────────────────────

    @Given("a rate player has a settlement not at a port")
    public void rate_player_has_settlement_not_at_port() {
        rateBoard  = buildBoardWithPorts();
        ratePlayer = new Player("Alice", PlayerColor.RED);
        Vertex v = rateBoard.getVertices().stream()
                .filter(vert -> !vert.hasPort() && !vert.isOccupied())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No non-port vertex found"));
        v.placeBuilding(new Settlement(ratePlayer));
    }

    @Given("a rate player has a settlement at a {word} port vertex")
    public void rate_player_has_settlement_at_port_vertex(String portTypeName) {
        rateBoard  = buildBoardWithPorts();
        ratePlayer = new Player("Alice", PlayerColor.RED);
        PortType type = PortType.valueOf(portTypeName);
        Vertex v = rateBoard.getVertices().stream()
                .filter(vert -> vert.getPort() == type && !vert.isOccupied())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No free vertex with port " + portTypeName));
        v.placeBuilding(new Settlement(ratePlayer));
    }

    @Given("a rate player has settlements at both a GENERIC_3_1 and a WOOD_2_1 port vertex")
    public void rate_player_has_settlements_at_both_ports() {
        rateBoard  = buildBoardWithPorts();
        ratePlayer = new Player("Alice", PlayerColor.RED);
        Vertex v1 = rateBoard.getVertices().stream()
                .filter(v -> v.getPort() == PortType.GENERIC_3_1 && !v.isOccupied())
                .findFirst().orElseThrow();
        v1.placeBuilding(new Settlement(ratePlayer));
        Vertex v2 = rateBoard.getVertices().stream()
                .filter(v -> v.getPort() == PortType.WOOD_2_1 && !v.isOccupied())
                .findFirst().orElseThrow();
        v2.placeBuilding(new Settlement(ratePlayer));
    }

    @When("the best rate for {word} is computed for that player")
    public void the_best_rate_for_resource_is_computed(String resourceName) {
        bestRate = getBestRate(ratePlayer, ResourceType.valueOf(resourceName));
    }

    @Then("the best rate is {int}")
    public void the_best_rate_is(int expected) {
        assertEquals(expected, bestRate);
    }

    // ── Trade execution steps ─────────────────────────────────────────────────

    @Given("a trade player starts with {int} {word}")
    public void trade_player_starts_with(int amount, String resourceName) {
        tradePlayer = new Player("Trader", PlayerColor.BLUE);
        tradePlayer.addResource(ResourceType.valueOf(resourceName), amount);
    }

    @When("a {int}:{int} trade converts {word} to {word}")
    public void trade_converts(int giveAmount, int receiveAmount, String giveRes, String receiveRes) {
        tradePlayer.removeResource(ResourceType.valueOf(giveRes), giveAmount);
        tradePlayer.addResource(ResourceType.valueOf(receiveRes), receiveAmount);
    }

    @Then("the trade player ends with {int} {word} and {int} {word}")
    public void trade_player_ends_with(int amount1, String res1, int amount2, String res2) {
        assertEquals(amount1, tradePlayer.getResource(ResourceType.valueOf(res1)));
        assertEquals(amount2, tradePlayer.getResource(ResourceType.valueOf(res2)));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BoardState buildBoardWithPorts() {
        return new BoardBuilder()
                .withShuffledTilesAndNumberTokens()
                .withVerticesAndEdges()
                .withPorts()
                .build();
    }

    /** Mirrors GameController.getBestRate */
    private int getBestRate(Player player, ResourceType type) {
        int best = 4;
        for (Vertex v : rateBoard.getVertices()) {
            if (!v.isOccupied() || v.getBuilding().getOwner() != player || !v.hasPort()) continue;
            int rate = new PortTradeStrategy(v.getPort()).getRate(type);
            if (rate < best) best = rate;
        }
        return best;
    }
}
