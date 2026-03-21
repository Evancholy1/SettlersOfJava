package com.settlersofcava.buildings;

import com.settlersofcava.player.Player;
import com.settlersofcava.resources.ResourceBundle;

/**
 * PATTERN: Polymorphism (base class for all player-built structures)
 * All buildable structures share: an owner, a cost, and victory points.
 * Road, Settlement, and City extend this class.
 */
public abstract class Buildable {

    protected final Player owner;

    protected Buildable(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() { return owner; }

    /** Resources required to build this structure. */
    public abstract ResourceBundle getCost();

    /** Victory points this structure contributes. */
    public abstract int getVictoryPoints();

    /** Display name used in UI and logging. */
    public abstract String getDisplayName();
}

