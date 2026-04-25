package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.FixedDie;
import settlersofjava.dice.Die;
import settlersofjava.engine.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameStepDefs {

    private CatanGame game;

    @io.cucumber.java.Before
    public void resetGame() {
        CatanGame.resetInstance();
    }

    @Given("a new game is started")
    public void a_new_game_is_started() {
        List<String> players = new ArrayList<>();
        players.add("Evan");
        players.add("Danny");
        players.add("Jeremy");
        players.add("Logan");
        game = CatanGame.getInstance(players);
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

    @When("a setup turn ends")
    public void a_setup_turn_ends() {
        game.getTurnManager().endSetupTurn();
    }

    @When("{int} setup turns end")
    public void n_setup_turns_end(int n) {
        for (int i = 0; i < n; i++) game.getTurnManager().endSetupTurn();
    }

    @Then("setup round 2 is active")
    public void setup_round_2_is_active() {
        assertTrue(game.getTurnManager().isSetupRound2());
    }

    @Then("setup round 2 is not active")
    public void setup_round_2_is_not_active() {
        assertFalse(game.getTurnManager().isSetupRound2());
    }

    @When("the dice are rolled")
    public void the_dice_are_rolled() {
        // Mirrors what GameController.handleDiceRolled does to TurnManager (skip TRADE for now)
        game.getTurnManager().advancePhase(); // ROLL → TRADE
        game.getTurnManager().advancePhase(); // TRADE → BUILD
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

