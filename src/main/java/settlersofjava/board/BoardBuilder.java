package settlersofjava.board;


import java.util.*;

import com.settlersofjava.board.Edge;
import com.settlersofjava.board.HexCoordinate;
import com.settlersofjava.board.TileFactory;
import com.settlersofjava.board.BoardState;

/**
 * PATTERN: Builder
 * Constructs a BoardState step by step.
 * The board topology (19 tiles, 54 vertices, 72 edges) is too complex
 * for a single constructor. Builder lets us add each layer incrementally.
 *
 * Usage:
 *   BoardState board = new BoardBuilder()
 *       .withStandardTiles()
 *       .withNumberTokens()
 *       .withPorts()
 *       .build();
 */
public class BoardBuilder {

    private final List<HexTile> tiles   = new ArrayList<>();
    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Edge> edges      = new ArrayList<>();
    private final Map<HexCoordinate, List<Vertex>> tileToVertices = new HashMap<>();

    private final TileFactory tileFactory = new TileFactory();

    public BoardBuilder withStandardTiles() {
        // TODO: place 19 HexTiles at standard axial coordinates,
        //       using TileFactory to create ResourceTile / DesertTile
        return this;
    }

    public BoardBuilder withNumberTokens() {
        // TODO: assign the standard Catan number token sequence to ResourceTiles
        return this;
    }

    public BoardBuilder withPorts() {
        // TODO: place 9 ports (1 generic 3:1, 5 specific 2:1, 3 generic 3:1)
        //       around the board edge
        return this;
    }

    public BoardBuilder withVerticesAndEdges() {
        // TODO: create 54 Vertex objects and 72 Edge objects,
        //       wire tileToVertices map
        return this;
    }

    public BoardState build() {
        // TODO: validate board is complete before constructing
        return new BoardState(tiles, vertices, edges, tileToVertices);
    }
}

