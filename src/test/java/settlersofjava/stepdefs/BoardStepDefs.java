package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import com.settlersofcava.board.*;
import static org.junit.jupiter.api.Assertions.*;

public class BoardStepDefs {

    private BoardState boardState;

    @Given("a standard Catan board is constructed")
    public void a_standard_board_is_constructed() {
        boardState = new BoardBuilder()
            .withStandardTiles()
            .withNumberTokens()
            .withPorts()
            .withVerticesAndEdges()
            .build();
    }

    @Then("the board has {int} tiles")
    public void the_board_has_tiles(int expected) {
        assertEquals(expected, boardState.getTiles().size());
    }

    @Then("the board has {int} vertices")
    public void the_board_has_vertices(int expected) {
        assertEquals(expected, boardState.getVertices().size());
    }

    @Then("the board has {int} edges")
    public void the_board_has_edges(int expected) {
        assertEquals(expected, boardState.getEdges().size());
    }
}

