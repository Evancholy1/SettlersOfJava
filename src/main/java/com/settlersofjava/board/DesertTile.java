package com.settlersofcava.board;

/**
 * The desert tile. Produces no resources. Starts with the Robber.
 */
public class DesertTile extends HexTile {

    private boolean robberPresent;

    public DesertTile(HexCoordinate coordinate) {
        super(coordinate, TerrainType.DESERT);
        this.robberPresent = true; // Robber starts here
    }

    @Override
    public boolean isActivatedBy(int diceRoll) { return false; }

    @Override
    public boolean isBlocked() { return robberPresent; }

    public void setRobberPresent(boolean present) { this.robberPresent = present; }
}

