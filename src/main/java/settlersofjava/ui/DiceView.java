package settlersofjava.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Displays the most recent dice roll result.
 * TODO: add animation on roll.
 */
public class DiceView extends HBox {

    private final Label die1Label = new Label("-");
    private final Label die2Label = new Label("-");

    public DiceView() {
        getChildren().addAll(die1Label, new Label(" + "), die2Label);
    }

    public void showRoll(int die1, int die2) {
        die1Label.setText(String.valueOf(die1));
        die2Label.setText(String.valueOf(die2));
    }
}

