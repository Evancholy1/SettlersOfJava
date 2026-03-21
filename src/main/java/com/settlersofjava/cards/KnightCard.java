package com.settlersofcava.cards;

import com.settlersofcava.player.Player;

/**
 * Move the Robber and optionally steal a resource.
 * Increments player's army size (tracked for Largest Army).
 */
public class KnightCard extends DevelopmentCard {

    @Override
    protected void applyEffect(Player player) {
        // TODO: trigger robber move flow, increment player.incrementArmySize()
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getCardName() { return "Knight"; }
}

