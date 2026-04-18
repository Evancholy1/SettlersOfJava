package settlersofjava.board;

import settlersofjava.resources.ResourceType;

/**
 * The six terrain types for hex tiles.
 * DESERT produces no resources and starts with the Robber.
 */
public enum TerrainType {
    FOREST,      // produces WOOD
    PASTURE,     // produces SHEEP
    FIELDS,      // produces WHEAT
    HILLS,       // produces BRICK
    MOUNTAINS,   // produces ORE
    DESERT;      // produces nothing

    /** Returns the resource this terrain produces, or null for DESERT. */
    public ResourceType toResourceType() {
        return switch (this) {
            case FOREST    -> ResourceType.WOOD;
            case PASTURE   -> ResourceType.SHEEP;
            case FIELDS    -> ResourceType.WHEAT;
            case HILLS     -> ResourceType.BRICK;
            case MOUNTAINS -> ResourceType.ORE;
            case DESERT    -> null;
        };
    }
}

