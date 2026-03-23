package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.buildings.Settlement;
import settlersofjava.player.*;
import settlersofjava.resources.*;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerStepDefs {

    private Player player;

    @Given("a player named {string}")
    public void a_player_named(String name) {
        player = new Player(name, PlayerColor.RED);
    }

    @When("the player receives {int} {word}")
    public void the_player_receives(int amount, String resourceName) {
        ResourceType type = ResourceType.valueOf(resourceName.toUpperCase());
        player.addResource(type, amount);
    }

    @Then("the player has {int} {word}")
    public void the_player_has(int expected, String resourceName) {
        ResourceType type = ResourceType.valueOf(resourceName.toUpperCase());
        assertEquals(expected, player.getResource(type));
    }

    @Then("the player cannot afford a settlement")
    public void the_player_cannot_afford_a_settlement() {
        assertFalse(player.canAfford(Settlement.COST));
    }

    @Then("the player can afford a settlement")
    public void the_player_can_afford_a_settlement() {
        assertTrue(player.canAfford(Settlement.COST));
    }

    @Then("removing {int} {word} throws an error")
    public void removing_throws_an_error(int amount, String resourceName) {
        ResourceType type = ResourceType.valueOf(resourceName.toUpperCase());
        assertThrows(IllegalStateException.class, () -> player.removeResource(type, amount));
    }


}

