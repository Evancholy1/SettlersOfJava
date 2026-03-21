package com.settlersofcava.board;

/**
 * The six terrain types for hex tiles.
 * DESERT produces no resources and starts with the Robber.
 */
public enum TerrainType {
    FOREST,      // produces WOOD
    PASTURE,     // produces WOOL
    FIELDS,      // produces GRAIN
    HILLS,       // produces BRICK
    MOUNTAINS,   // produces ORE
    DESERT       // produces nothing
}

