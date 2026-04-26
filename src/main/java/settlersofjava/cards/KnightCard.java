package settlersofjava.cards;

import settlersofjava.player.Player;

/**
 * Move the Robber and optionally steal a resource.
 * Increments player's army size (tracked for Largest Army).
 */
public class KnightCard extends DevelopmentCard {

    @Override
    protected void applyEffect(Player player) {
        // Handled directly by GameController
    }

    @Override
    public String getCardName() { return "Knight"; }
}

