package settlersofjava;

import io.cucumber.java.en.*;
import settlersofjava.engine.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(expected, game.getCurrentPhase());
    }

    @Then("the current player index is {int}")
    public void the_current_player_index_is(int expected) {
        assertEquals(expected, game.getTurnManager().getCurrentPlayerIndex());
    }

    @When("the turn ends")
    public void the_turn_ends() {
        game.getTurnManager().endTurn();
    }

    @When("the phase is advanced from SETUP to ROLL")
    public void the_phase_is_advanced_from_setup() {
        game.getTurnManager().advancePhase();
    }

    @When("the phase is advanced")
    public void the_phase_is_advanced() {
        game.getTurnManager().advancePhase();
    }
}

