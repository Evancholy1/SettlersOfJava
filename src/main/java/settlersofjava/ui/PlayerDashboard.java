package settlersofjava.ui;

import settlersofjava.player.Player;
import javafx.scene.layout.VBox;

/**
 * The contextual bottom panel described in the proposal.
 * Swaps its content to show the active player's resources, dev cards, and actions.
 */
public class PlayerDashboard extends VBox {

    private Player activePlayer;

    public void switchToPlayer(Player player) {
        this.activePlayer = player;
        // TODO: rebuild child nodes to reflect player's resources + available actions
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

