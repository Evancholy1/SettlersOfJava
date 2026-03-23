package settlersofjava.board;


import java.util.*;

import settlersofjava.board.Edge;
import settlersofjava.board.HexCoordinate;
import settlersofjava.board.TileFactory;
import settlersofjava.board.BoardState;

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

    private static final List<TerrainType> STANDARD_TERRAINS = List.of(
            TerrainType.MOUNTAINS, TerrainType.PASTURE,  TerrainType.FOREST,
            TerrainType.FIELDS,    TerrainType.HILLS,    TerrainType.PASTURE,
            TerrainType.HILLS,     TerrainType.FIELDS,   TerrainType.FOREST,
            TerrainType.DESERT,
            TerrainType.FOREST,    TerrainType.MOUNTAINS,TerrainType.FOREST,
            TerrainType.MOUNTAINS, TerrainType.FIELDS,   TerrainType.PASTURE,
            TerrainType.HILLS,     TerrainType.FIELDS,   TerrainType.PASTURE
    );

    private static final List<Integer> STANDARD_TOKENS = List.of(
            10, 2, 9, 12, 6, 4, 10, 9, 11, 3, 8, 8, 3, 4, 5, 5, 6, 11
    );

    private static final List<HexCoordinate> STANDARD_COORDS = List.of(
            new HexCoordinate( 0,  0),
            new HexCoordinate( 1,  0), new HexCoordinate( 0,  1),
            new HexCoordinate(-1,  1), new HexCoordinate(-1,  0),
            new HexCoordinate( 0, -1), new HexCoordinate( 1, -1),
            new HexCoordinate( 2,  0), new HexCoordinate( 1,  1),
            new HexCoordinate( 0,  2), new HexCoordinate(-1,  2),
            new HexCoordinate(-2,  1), new HexCoordinate(-2,  0),
            new HexCoordinate(-1, -1), new HexCoordinate( 0, -2),
            new HexCoordinate( 1, -2), new HexCoordinate( 2, -1),
            new HexCoordinate( 2, -2), new HexCoordinate(-2,  2)
    );

    public BoardBuilder withStandardTiles() {
        int tokenIndex = 0;
        for (int i = 0; i < STANDARD_COORDS.size(); i++) {
            HexCoordinate coord = STANDARD_COORDS.get(i);
            TerrainType terrain = STANDARD_TERRAINS.get(i);
            if (terrain == TerrainType.DESERT) {
                tiles.add(tileFactory.createDesertTile(coord));
            } else {
                tiles.add(tileFactory.createResourceTile(coord, terrain, STANDARD_TOKENS.get(tokenIndex++)));
            }
        }
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
        Map<String, Vertex> vertexMap = new HashMap<>();
        Map<String, Edge> edgeMap = new HashMap<>();
        int[] vertexId = {0};
        int[] edgeId = {0};

        // For each tile, compute its 6 vertices using neighbor tile coordinates
        // A vertex is uniquely identified by the set of 3 tiles that share it
        for (HexTile tile : tiles) {
            int q = tile.getCoordinate().getQ();
            int r = tile.getCoordinate().getR();

            // 6 neighbor directions in axial coordinates
            int[][] dirs = {{1,0},{1,-1},{0,-1},{-1,0},{-1,1},{0,1}};

            List<String> vertexKeys = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                // Each vertex is shared by this tile and its two adjacent neighbors
                int[] d1 = dirs[i];
                int[] d2 = dirs[(i + 1) % 6];
                // Sort the three tile coords to make a canonical key
                int[][] three = {
                        {q, r},
                        {q + d1[0], r + d1[1]},
                        {q + d2[0], r + d2[1]}
                };
                Arrays.sort(three, (a, b) -> a[0] != b[0] ? a[0] - b[0] : a[1] - b[1]);
                String key = three[0][0]+","+three[0][1]+"|"+
                        three[1][0]+","+three[1][1]+"|"+
                        three[2][0]+","+three[2][1];
                vertexMap.computeIfAbsent(key, k -> new Vertex(vertexId[0]++));
                vertexKeys.add(key);
            }

            // Store vertices for this tile
            List<Vertex> tileVerts = new ArrayList<>();
            for (String k : vertexKeys) tileVerts.add(vertexMap.get(k));
            tileToVertices.put(tile.getCoordinate(), tileVerts);

            // Create edges between adjacent vertices
            for (int i = 0; i < 6; i++) {
                String ka = vertexKeys.get(i);
                String kb = vertexKeys.get((i + 1) % 6);
                String edgeKey = ka.compareTo(kb) < 0 ? ka+"||"+kb : kb+"||"+ka;
                edgeMap.computeIfAbsent(edgeKey, k ->
                        new Edge(edgeId[0]++, vertexMap.get(ka), vertexMap.get(kb)));
            }
        }

        vertices.addAll(vertexMap.values());
        edges.addAll(edgeMap.values());
        return this;
    }

    public BoardState build() {
        return new BoardState(tiles, vertices, edges, tileToVertices);
    }
}

