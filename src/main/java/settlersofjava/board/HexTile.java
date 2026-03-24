package settlersofjava.board;

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


    public abstract boolean isActivatedBy(int diceRoll);

    public abstract boolean isBlocked();
}

