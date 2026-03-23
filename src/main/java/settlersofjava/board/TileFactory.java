package settlersofjava.board;

/**
 * PATTERN: Factory
 * Creates HexTile instances based on TerrainType.
 * Ensures the correct subclass (ResourceTile vs DesertTile) is always returned.
 */
public class TileFactory {

    /**
     * Creates a resource-producing tile.
     * @param coordinate  axial grid position
     * @param terrainType must NOT be DESERT
     * @param numberToken the dice number that activates this tile (2–6, 8–12)
     */
    public HexTile createResourceTile(HexCoordinate coordinate, TerrainType terrainType, int numberToken) {
        if (terrainType == TerrainType.DESERT) {
            throw new IllegalArgumentException("Use createDesertTile() for DESERT terrain.");
        }
        return new ResourceTile(coordinate, terrainType, numberToken);
    }

    /**
     * Creates the desert tile (no number token, Robber starts here).
     */
    public HexTile createDesertTile(HexCoordinate coordinate) {
        return new DesertTile(coordinate);
    }
}

