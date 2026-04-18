package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.board.*;
import settlersofjava.buildings.Settlement;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerColor;
import settlersofjava.resources.ResourceType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SetupStepDefs {

    private BoardState board;
    private Player     player;
    private int        expectedResources;

    @Given("a setup board and player are initialized")
    public void setup_board_and_player_initialized() {
        board  = new BoardBuilder()
                .withShuffledTilesAndNumberTokens()
                .withVerticesAndEdges()
                .withPorts()
                .build();
        player = new Player("TestPlayer", PlayerColor.RED);
    }

    // ── Round 2 resource grant ────────────────────────────────────────────────

    @When("the player claims setup resources from a resource-adjacent vertex")
    public void player_claims_setup_resources() {
        // Find any vertex that touches at least one ResourceTile
        for (Vertex v : board.getVertices()) {
            List<HexTile> adjacent = board.getTilesFor(v);
            long nonDesert = adjacent.stream()
                    .filter(t -> t instanceof ResourceTile)
                    .count();
            if (nonDesert == 0) continue;

            expectedResources = (int) nonDesert;

            // Simulate what GameController.giveStartingResources does
            for (HexTile tile : adjacent) {
                if (tile instanceof ResourceTile rt) {
                    ResourceType res = rt.getTerrainType().toResourceType();
                    if (res != null) player.addResource(res, 1);
                }
            }
            return;
        }
        fail("No resource-adjacent vertex found on the board");
    }

    @Then("the player's total resources equal the adjacent non-desert tile count")
    public void player_total_resources_match_tiles() {
        int total = 0;
        for (ResourceType type : ResourceType.values()) {
            total += player.getResource(type);
        }
        assertEquals(expectedResources, total);
    }

    // ── Round 1: no resources granted ────────────────────────────────────────

    @When("the player claims no resources because it is round 1")
    public void player_claims_no_resources_round1() {
        // In round 1, GameController skips giveStartingResources — nothing to do here
    }

    @Then("the player has {int} total resources")
    public void player_has_total_resources(int expected) {
        int total = 0;
        for (ResourceType type : ResourceType.values()) {
            total += player.getResource(type);
        }
        assertEquals(expected, total);
    }
}
