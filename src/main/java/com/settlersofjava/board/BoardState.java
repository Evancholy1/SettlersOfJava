package com.settlersofcava.board;

import java.util.List;
import java.util.Map;

/**
 * PATTERN: Dependency Injection (injected into GameController)
 * Holds all board topology: tiles, vertices, edges, and their relationships.
 * Constructed exclusively via BoardBuilder.
 */
public class BoardState {

    private final List<HexTile> tiles;
    private final List<Vertex> vertices;
    private final List<Edge> edges;

    // Maps each tile coordinate to the 6 vertices that surround it
    private final Map<HexCoordinate, List<Vertex>> tileToVertices;

    // Package-private: only BoardBuilder should call this
    BoardState(List<HexTile> tiles,
               List<Vertex> vertices,
               List<Edge> edges,
               Map<HexCoordinate, List<Vertex>> tileToVertices) {
        this.tiles = List.copyOf(tiles);
        this.vertices = List.copyOf(vertices);
        this.edges = List.copyOf(edges);
        this.tileToVertices = Map.copyOf(tileToVertices);
    }

    public List<HexTile> getTiles()     { return tiles; }
    public List<Vertex> getVertices()   { return vertices; }
    public List<Edge> getEdges()        { return edges; }

    /**
     * Returns all vertices adjacent to the given tile coordinate.
     * Used during resource distribution after a dice roll.
     */
    public List<Vertex> getVerticesFor(HexCoordinate coord) {
        return tileToVertices.getOrDefault(coord, List.of());
    }

    /**
     * Returns all tiles activated by the given dice roll (excluding robber-blocked tiles).
     */
    public List<HexTile> getActiveTilesFor(int diceRoll) {
        return tiles.stream()
                .filter(t -> t.isActivatedBy(diceRoll))
                .toList();
    }
}

