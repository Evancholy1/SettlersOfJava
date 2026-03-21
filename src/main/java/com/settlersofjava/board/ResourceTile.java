package com.settlersofcava.board;

/**
 * A hex tile that produces resources when its number token is rolled.
 */
public class ResourceTile extends HexTile {

    private final int numberToken; // 2–6, 8–12 (7 is never a token)
    private boolean robberPresent;

    public ResourceTile(HexCoordinate coordinate, TerrainType terrainType, int numberToken) {
        super(coordinate, terrainType);
        this.numberToken = numberToken;
        this.robberPresent = false;
    }

    public int getNumberToken() { return numberToken; }

    @Override
    public boolean isActivatedBy(int diceRoll) {
        return !robberPresent && diceRoll == numberToken;
    }

    @Override
    public boolean isBlocked() { return robberPresent; }

    public void setRobberPresent(boolean present) { this.robberPresent = present; }
}

