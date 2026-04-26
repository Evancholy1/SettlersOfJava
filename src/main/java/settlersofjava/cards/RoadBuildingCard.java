package settlersofjava.cards;

import settlersofjava.player.Player;

/**
 * Allows the player to place 2 roads for free.
 */
public class RoadBuildingCard extends DevelopmentCard {

    @Override
    protected void applyEffect(Player player) {
        // Handled directly by GameController
    }

    @Override
    public String getCardName() { return "Road Building"; }
}

