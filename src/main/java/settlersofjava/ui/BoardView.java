package settlersofjava.ui;

import settlersofjava.board.BoardState;
import javafx.scene.layout.Pane;

/**
 * Renders the hex board on a JavaFX Pane.
 * Observes BoardState changes and redraws affected tiles/vertices/edges.
 */
public class BoardView extends Pane {

    private final BoardState boardState;

    public BoardView(BoardState boardState) {
        this.boardState = boardState;
        // TODO: draw initial hex grid from boardState.getTiles()
    }

    public void refresh() {
        // TODO: redraw roads, settlements, cities after a change
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

