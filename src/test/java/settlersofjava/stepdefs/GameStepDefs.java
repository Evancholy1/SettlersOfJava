package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.FixedDie;
import settlersofjava.dice.Die;
import settlersofjava.engine.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameStepDefs {

    private CatanGame game;

    @io.cucumber.java.Before
    public void resetGame() {
        CatanGame.resetInstance();
    }

    @Given("a new game is started")
    public void a_new_game_is_started() {
        game = CatanGame.getInstance("Evan", "Danny");
    }

    @When("the dice are rolled and show {int}")
    public void the_dice_show(int total) {
        Die fixedDie = new FixedDie(total);
        int rollResult = fixedDie.roll();
        assertEquals(total, rollResult);
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

