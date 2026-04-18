package settlersofjava.board;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    // Reverse map: each vertex to the tiles it touches (up to 3)
    private final Map<Vertex, List<HexTile>> vertexToTiles;

    // Package-private: only BoardBuilder should call this
    BoardState(List<HexTile> tiles,
               List<Vertex> vertices,
               List<Edge> edges,
               Map<HexCoordinate, List<Vertex>> tileToVertices,
               Map<Vertex, List<HexTile>> vertexToTiles) {
        this.tiles = List.copyOf(tiles);
        this.vertices = List.copyOf(vertices);
        this.edges = List.copyOf(edges);
        this.tileToVertices = Map.copyOf(tileToVertices);
        this.vertexToTiles = Map.copyOf(vertexToTiles);
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
     * Returns all tiles touching the given vertex (up to 3).
     * Used for setup-phase resource grants and city/settlement production.
     */
    public List<HexTile> getTilesFor(Vertex v) {
        return vertexToTiles.getOrDefault(v, List.of());
    }

    /**
     * Returns all tiles activated by the given dice roll (excluding robber-blocked tiles).
     */
    public List<HexTile> getActiveTilesFor(int diceRoll) {
        return tiles.stream()
                .filter(t -> t.isActivatedBy(diceRoll))
                .toList();
    }

    /** Returns all vertices directly connected to v via an edge (used for distance rule). */
    public List<Vertex> getAdjacentVertices(Vertex v) {
        return edges.stream()
                .filter(e -> e.connectsVertex(v))
                .flatMap(e -> java.util.stream.Stream.of(e.getVertexA(), e.getVertexB()))
                .filter(other -> other != v)
                .distinct()
                .toList();
    }

    /** Returns all edges that touch the given vertex (used for road placement). */
    public List<Edge> getEdgesFor(Vertex v) {
        return edges.stream()
                .filter(e -> e.connectsVertex(v))
                .toList();
    }

    /** Returns the port at the given vertex, if any. */
    public Optional<PortType> getPortFor(Vertex v) {
        return Optional.ofNullable(v.getPort());
    }
}

