package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.cards.VictoryPointCard;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerColor;
import static org.junit.jupiter.api.Assertions.*;

public class LargestArmyStepDefs {

    private Player player1;
    private Player player2;

    // Mirrored engine state for testing independently of GameController
    private int largestArmySize = 2;
    private Player largestArmyOwner = null;

    private void simulateCheckLargestArmy(Player p) {
        if (p.getArmySize() > largestArmySize) {
            largestArmySize = p.getArmySize();
            if (largestArmyOwner != p) {
                if (largestArmyOwner != null) {
                    largestArmyOwner.setHasLargestArmy(false);
                }
                largestArmyOwner = p;
                p.setHasLargestArmy(true);
            }
        }
    }

    // ── Scenario: Calculating Total Victory Points ────────────────────────────

    @Given("a player named {string} has {int} public victory point")
    public void a_player_has_public_vp(String name, int vps) {
        player1 = new Player(name, PlayerColor.RED);
        player1.addVictoryPoints(vps);
    }

    @Given("the player has a hidden Victory Point card")
    public void player_has_hidden_vp() {
        player1.addDevCard(new VictoryPointCard());
    }

    @Then("the player should have {int} total victory points")
    public void player_has_total_vps(int expected) {
        assertEquals(expected, player1.getTotalVictoryPoints());
    }

    // ── Scenario: Claiming the Largest Army ───────────────────────────────────

    @Given("a player named {string} has played {int} Knight cards")
    public void player_played_knights(String name, int knights) {
        player1 = new Player(name, PlayerColor.RED);
        for (int i = 0; i < knights; i++) {
            player1.incrementArmySize();
        }
    }

    @When("the player plays another Knight card")
    public void player_plays_another_knight() {
        player1.incrementArmySize();
        simulateCheckLargestArmy(player1);
    }

    @Then("the player should receive the Largest Army")
    public void player_receives_largest_army() {
        assertTrue(player1.hasLargestArmy(), "Player should have the Largest Army property set to true.");
        assertEquals(player1, largestArmyOwner, "Player should be recognized as the army owner.");
    }

    // ── Scenario: Stealing the Largest Army ───────────────────────────────────

    @Given("{string} holds the Largest Army with {int} Knight cards")
    public void holds_largest_army(String name, int knights) {
        player1 = new Player(name, PlayerColor.RED);
        for (int i = 0; i < knights; i++) {
            player1.incrementArmySize();
        }

        // Reset the tracker and give it to player 1
        largestArmySize = 2;
        simulateCheckLargestArmy(player1);
    }

    @Given("{string} has played {int} Knight cards")
    public void has_played_knights(String name, int knights) {
        player2 = new Player(name, PlayerColor.BLUE);
        for (int i = 0; i < knights; i++) {
            player2.incrementArmySize();
        }
    }

    @When("{string} plays another Knight card")
    public void p2_plays_another_knight(String name) {
        // Find whichever player matches the string name and increment their army
        Player activePlayer = (player1.getName().equals(name)) ? player1 : player2;
        activePlayer.incrementArmySize();
        simulateCheckLargestArmy(activePlayer);
    }

    @Then("{string} should receive the Largest Army")
    public void p2_receives_largest_army(String name) {
        Player expectedOwner = (player1.getName().equals(name)) ? player1 : player2;
        assertTrue(expectedOwner.hasLargestArmy());
    }

    @Then("{string} should lose the Largest Army")
    public void p1_loses_largest_army(String name) {
        Player expectedLoser = (player1.getName().equals(name)) ? player1 : player2;
        assertFalse(expectedLoser.hasLargestArmy());
    }
}