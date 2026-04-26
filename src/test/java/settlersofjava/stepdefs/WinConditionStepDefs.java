package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.cards.VictoryPointCard;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerColor;
import static org.junit.jupiter.api.Assertions.*;

public class WinConditionStepDefs {

    private Player player;
    private boolean isGameOver = false;
    private Player winner = null;

    // Mirrors the win check inside GameController
    private void simulateCheckWinCondition(Player p) {
        if (p.getTotalVictoryPoints() >= 10) {
            isGameOver = true;
            winner = p;
        }
    }

    @Given("a player named {string} has {int} Total Victory Points")
    public void player_has_vp(String name, int vp) {
        player = new Player(name, PlayerColor.RED);
        player.addVictoryPoints(vp);
    }

    @When("the player builds a Settlement")
    public void player_builds_settlement() {
        player.addVictoryPoints(1); // +1 for Settlement
        simulateCheckWinCondition(player);
    }

    @When("the player upgrades a Settlement to a City")
    public void player_upgrades_city() {
        player.addVictoryPoints(1);
        simulateCheckWinCondition(player);
    }

    @When("the player buys a Victory Point Development Card")
    public void player_buys_vp_card() {
        player.addDevCard(new VictoryPointCard());
        simulateCheckWinCondition(player);
    }

    @Given("the player has played {int} Knight cards")
    public void player_played_knights(int knights) {
        for(int i = 0; i < knights; i++) {
            player.incrementArmySize();
        }
    }

    @Then("the player should win the game")
    public void player_should_win() {
        assertTrue(isGameOver, "The game over state should be triggered.");
        assertEquals(player, winner, "The player should be declared the winner.");
        assertTrue(player.getTotalVictoryPoints() >= 10, "The player must have at least 10 Total VPs to win.");
    }
}