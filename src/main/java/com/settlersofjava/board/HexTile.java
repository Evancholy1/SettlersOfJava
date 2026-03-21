package com.settlersofcava.board;

/**
 * PATTERN: Factory (created by TileFactory), Polymorphism
 * Abstract base for all hex tiles on the board.
 * Subclasses: ResourceTile, DesertTile
 */
public abstract class HexTile {

    protected final HexCoordinate coordinate;
    protected final TerrainType terrainType;

    protected HexTile(HexCoordinate coordinate, TerrainType terrainType) {
        this.coordinate = coordinate;
        this.terrainType = terrainType;
    }

    public HexCoordinate getCoordinate() { return coordinate; }
    public TerrainType getTerrainType()  { return terrainType; }

    /**
     * Returns true if this tile produces resources when the given number is rolled.
     */
    public abstract boolean isActivatedBy(int diceRoll);

    /**
     * Returns true if the Robber is currently blocking this tile.
     */
    public abstract boolean isBlocked();
}

