package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import com.settlersofcava.engine.*;

public class GameStepDefs {

    private CatanGame game;

    @Given("a new game is started")
    public void a_new_game_is_started() {
        game = CatanGame.getInstance();
    }

    @When("the dice are rolled and show {int}")
    public void the_dice_show(int total) {
        // TODO: inject a fixed Die that returns this total
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Then("the current phase is {string}")
    public void the_current_phase_is(String phaseName) {
        GamePhase expected = GamePhase.valueOf(phaseName.toUpperCase());
        org.junit.jupiter.api.Assertions.assertEquals(expected, game.getCurrentPhase());
    }
}

