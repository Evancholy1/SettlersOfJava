package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import com.settlersofcava.player.*;
import com.settlersofcava.resources.*;
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
}

