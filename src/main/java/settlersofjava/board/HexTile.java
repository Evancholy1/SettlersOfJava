package settlersofjava.board;

import com.settlersofjava.board.HexCoordinate;
import com.settlersofjava.board.Terrain

/**
 * PATTERN: Factory (created by TileFactory), Polymorphism
 * Abstract base for all hex tiles on the board.
 * Subclasses: ResourceTile, DesertTile
 */

import


public abstract class HexTile {

    protected final settlersofjava.board.HexCoordinate coordinate;
    protected final TerrainType terrainType;

    protected HexTile(HexCoordinate coordinate, TerrainType terrainType) {
        this.coordinate = coordinate;
        this.terrainType = terrainType;
    }

    public settlersofjava.board.HexCoordinate getCoordinate() { return coordinate; }
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

