package settlersofjava.board;

import settlersofjava.buildings.Road;

/**
 * A border between two adjacent vertices.
 * Can hold a Road owned by a player.
 * There are 72 edges on a standard Catan board.
 */
public class Edge {

    private final int id;
    private final Vertex vertexA;
    private final Vertex vertexB;
    private Road road; // null if no road placed

    public Edge(int id, Vertex vertexA, Vertex vertexB) {
        this.id = id;
        this.vertexA = vertexA;
        this.vertexB = vertexB;
        this.road = null;
    }

    public int getId()         { return id; }
    public Vertex getVertexA() { return vertexA; }
    public Vertex getVertexB() { return vertexB; }

    public boolean hasRoad()   { return road != null; }
    public Road getRoad()      { return road; }

    public void placeRoad(Road road) {
        if (hasRoad()) {
            throw new IllegalStateException("Edge " + id + " already has a road.");
        }
        this.road = road;
    }

    public boolean connectsVertex(Vertex v) {
        return vertexA.equals(v) || vertexB.equals(v);
    }
}

