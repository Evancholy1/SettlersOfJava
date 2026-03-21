package com.settlersofcava.cards;

import com.settlersofcava.player.Player;

/**
 * Allows the player to place 2 roads for free.
 */
public class RoadBuildingCard extends DevelopmentCard {

    @Override
    protected void applyEffect(Player player) {
        // TODO: grant player 2 free road placements this turn
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getCardName() { return "Road Building"; }
}

