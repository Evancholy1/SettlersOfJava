package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.cards.*;
import settlersofjava.player.*;
import settlersofjava.resources.*;

import static org.junit.jupiter.api.Assertions.*;

public class DevCardStepDefs {

    private Player player;
    private DevelopmentCard lastDrawnCard;

    @Given("a player named {string} has 1 WHEAT, 1 SHEEP, and 1 ORE")
    public void a_player_has_resources(String name) {
        player = new Player(name, PlayerColor.RED);
        player.addResource(ResourceType.WHEAT, 1);
        player.addResource(ResourceType.SHEEP, 1);
        player.addResource(ResourceType.ORE, 1);
    }

    @When("the player purchases a Development Card")
    public void the_player_purchases_a_development_card() {
        if (player.canAfford(DevelopmentCard.COST)) {
            player.removeResource(ResourceType.WHEAT, 1);
            player.removeResource(ResourceType.SHEEP, 1);
            player.removeResource(ResourceType.ORE, 1);
            lastDrawnCard = new KnightCard(); // Mocking draw
            player.addDevCard(lastDrawnCard);
        }
    }

    @Then("the player's resources are deducted")
    public void resources_are_deducted() {
        assertEquals(0, player.getResource(ResourceType.WHEAT));
        assertEquals(0, player.getResource(ResourceType.SHEEP));
        assertEquals(0, player.getResource(ResourceType.ORE));
    }

    @Then("the player has {int} unplayable Development Card in their hand")
    public void player_has_unplayable_card(int count) {
        assertEquals(count, player.getDevCards().size());
        assertTrue(player.getDevCards().get(0).isLocked());
        assertThrows(IllegalStateException.class, () -> player.getDevCards().get(0).play(player));
    }

    @Given("a player named {string} bought a {string} card on a previous turn")
    public void player_bought_card_previously(String name, String cardType) {
        player = new Player(name, PlayerColor.BLUE);
        lastDrawnCard = new KnightCard();
        player.addDevCard(lastDrawnCard);
        player.unlockDevCards(); // Simulates turn ending
    }

    @When("the player plays the card")
    public void the_player_plays_the_card() {
        lastDrawnCard.play(player);
    }

    @Then("the card is marked as played")
    public void card_is_marked_played() {
        assertTrue(lastDrawnCard.isPlayed());
    }

    @Then("the card remains in the player's hand")
    public void card_remains_in_hand() {
        assertTrue(player.getDevCards().contains(lastDrawnCard));
    }
}