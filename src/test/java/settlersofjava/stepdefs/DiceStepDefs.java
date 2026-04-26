package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.board.*;
import settlersofjava.buildings.City;
import settlersofjava.buildings.Settlement;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerColor;
import settlersofjava.resources.ResourceType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiceStepDefs {

    private BoardState board;
    private Player     player1;
    private Player     player2;
    private ResourceType expectedResource;
    private int        tokenNumber;

    // ── Board setup ───────────────────────────────────────────────────────────

    @Given("a board with a settlement on a known resource tile")
    public void board_with_settlement() {
        board   = buildBoard();
        player1 = new Player("Alice", PlayerColor.RED);
        placeOn(player1, false);
    }

    @Given("a board with a city on a known resource tile")
    public void board_with_city() {
        board   = buildBoard();
        player1 = new Player("Alice", PlayerColor.RED);
        placeOn(player1, true);
    }

    @Given("a board with two players each on different resource tiles")
    public void board_with_two_players() {
        board   = buildBoard();
        player1 = new Player("Alice", PlayerColor.RED);
        player2 = new Player("Bob",   PlayerColor.BLUE);

        // Place player1 on the first available resource tile
        placeOn(player1, false);

        // Place player2 on a vertex that touches NO tile with player1's token number
        for (Vertex v : board.getVertices()) {
            if (v.isOccupied()) continue;
            boolean touchesTokenTile = board.getTilesFor(v).stream()
                    .anyMatch(t -> t instanceof ResourceTile rt2
                               && rt2.getNumberToken() == tokenNumber);
            if (!touchesTokenTile) {
                v.placeBuilding(new Settlement(player2));
                return;
            }
        }
        fail("Could not find a vertex isolated from player 1's token tile");
    }

    // ── Roll actions ──────────────────────────────────────────────────────────

    @When("the matching token number is rolled")
    public void matching_token_rolled() {
        distribute(tokenNumber);
    }

    @When("a number that matches no tile token is rolled")
    public void non_matching_number_rolled() {
        // 1 and 7 are never valid Catan tokens; 1 will activate nothing
        distribute(1);
    }

    @When("the first player's tile token is rolled")
    public void first_player_tile_token_rolled() {
        distribute(tokenNumber);
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    @Then("the settlement owner has {int} of the expected resource")
    public void settlement_owner_has_n(int expected) {
        assertEquals(expected, player1.getResource(expectedResource));
    }

    @Then("the city owner has {int} of the expected resource")
    public void city_owner_has_n(int expected) {
        assertEquals(expected, player1.getResource(expectedResource));
    }

    @Then("only the first player receives a resource")
    public void only_first_player_receives() {
        assertTrue(player1.getResource(expectedResource) > 0,
            "Player 1 should have received a resource");
    }

    @Then("the second player receives nothing")
    public void second_player_receives_nothing() {
        int total = 0;
        for (ResourceType t : ResourceType.values()) total += player2.getResource(t);
        assertEquals(0, total, "Player 2 should have received nothing");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BoardState buildBoard() {
        return new BoardBuilder()
                .withShuffledTilesAndNumberTokens()
                .withVerticesAndEdges()
                .build();
    }

    /** Places a settlement (or city) for the given player on the first available
     *  vertex of the first ResourceTile found, recording the tile's token and resource. */
    private void placeOn(Player player, boolean city) {
        for (HexTile tile : board.getTiles()) {
            if (!(tile instanceof ResourceTile rt)) continue;
            List<Vertex> verts = board.getVerticesFor(tile.getCoordinate());
            for (Vertex v : verts) {
                if (v.isOccupied()) continue;
                if (city) {
                    v.placeBuilding(new City(player));
                } else {
                    v.placeBuilding(new Settlement(player));
                }
                expectedResource = rt.getTerrainType().toResourceType();
                tokenNumber      = rt.getNumberToken();
                return;
            }
        }
        fail("No unoccupied vertex on a ResourceTile found");
    }

    /** Mirrors GameController.distributeResources — tested independently here. */
    private void distribute(int diceTotal) {
        for (HexTile tile : board.getActiveTilesFor(diceTotal)) {
            if (!(tile instanceof ResourceTile rt)) continue;
            ResourceType res = rt.getTerrainType().toResourceType();
            if (res == null) continue;
            for (Vertex v : board.getVerticesFor(tile.getCoordinate())) {
                if (!v.isOccupied()) continue;
                int amount = v.getBuilding() instanceof City ? 2 : 1;
                v.getBuilding().getOwner().addResource(res, amount);
            }
        }
    }

    // ── Discard Rule (7 Rolled) ───────────────────────────────────────────────

    @Given("a player has {int} total resource cards")
    public void a_player_has_total_resource_cards(int totalCards) {
        player1 = new Player("Alice", PlayerColor.RED);
        // give them all WOOD for an easy test pool
        player1.addResource(ResourceType.WOOD, totalCards);
    }

    @When("a 7 is rolled and the discard rule is applied")
    public void a_7_is_rolled_and_discard_applied() {
        // mirrors the discard logic in GameController.handleDiceRolled
        int totalResources = 0;
        for (ResourceType type : ResourceType.values()) {
            totalResources += player1.getResource(type);
        }

        if (totalResources >= 8) {
            int toDiscard = totalResources / 2;
            java.util.List<ResourceType> pool = new java.util.ArrayList<>();
            for (ResourceType type : ResourceType.values()) {
                for (int i = 0; i < player1.getResource(type); i++) {
                    pool.add(type);
                }
            }

            java.util.Random rand = new java.util.Random();
            for (int i = 0; i < toDiscard; i++) {
                int index = rand.nextInt(pool.size());
                ResourceType typeToDiscard = pool.remove(index);
                player1.removeResource(typeToDiscard, 1);
            }
        }
    }

    @Then("the player should have {int} total resource cards left")
    public void the_player_should_have_total_resource_cards_left(int expectedRemaining) {
        int actualRemaining = 0;
        for (ResourceType type : ResourceType.values()) {
            actualRemaining += player1.getResource(type);
        }
        assertEquals(expectedRemaining, actualRemaining,
                "Player should have exactly " + expectedRemaining + " cards remaining.");
    }
}
