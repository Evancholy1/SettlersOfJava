package settlersofjava.board;


import java.util.*;
import java.util.stream.Collectors;

/**
 * PATTERN: Builder
 * Constructs a BoardState step by step.
 * The board topology (19 tiles, 54 vertices, 72 edges) is too complex
 * for a single constructor. Builder lets us add each layer incrementally.
 *
 * Usage:
 *   BoardState board = new BoardBuilder()
 *       .withShuffledTilesAndNumberTokens()
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
            2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12
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

    public BoardBuilder withShuffledTilesAndNumberTokens() {
        List<TerrainType> shuffledTerrains = new ArrayList<>(STANDARD_TERRAINS);
        Collections.shuffle(shuffledTerrains);
        List<Integer> shuffledTokens = new ArrayList<>(STANDARD_TOKENS);
        Collections.shuffle(shuffledTokens);

        int tokenIndex = 0;
        for (int i = 0; i < STANDARD_COORDS.size(); i++) {
            HexCoordinate coord = STANDARD_COORDS.get(i);
            TerrainType terrain = shuffledTerrains.get(i);
            if (terrain == TerrainType.DESERT) {
                tiles.add(tileFactory.createDesertTile(coord));
            } else {
                tiles.add(tileFactory.createResourceTile(coord, terrain, shuffledTokens.get(tokenIndex++)));
            }
        }
        return this;
    }

    public BoardBuilder withPorts() {
        // Count how many tiles each vertex touches (coastal < 3, interior == 3)
        Map<Integer, Integer> vertexTileCount = new HashMap<>();
        for (List<Vertex> verts : tileToVertices.values()) {
            for (Vertex v : verts) {
                vertexTileCount.merge(v.getId(), 1, Integer::sum);
            }
        }

        // Compute vertex positions in tile-unit coordinates (same math as BoardView, no scale)
        Map<Integer, double[]> vPos = new HashMap<>();
        for (HexTile tile : tiles) {
            int q = tile.getCoordinate().getQ();
            int r = tile.getCoordinate().getR();
            double cx = Math.sqrt(3) * (q + r / 2.0);
            double cy = 1.5 * r;
            List<Vertex> verts = tileToVertices.get(tile.getCoordinate());
            if (verts == null) continue;
            for (int i = 0; i < verts.size(); i++) {
                double angle = Math.toRadians(-60 * i - 30);
                int id = verts.get(i).getId();
                vPos.putIfAbsent(id, new double[]{cx + Math.cos(angle), cy + Math.sin(angle)});
            }
        }

        // Coastal edges: both endpoints touch fewer than 3 tiles
        List<Edge> coastal = edges.stream()
                .filter(e -> vertexTileCount.getOrDefault(e.getVertexA().getId(), 0) < 3
                          && vertexTileCount.getOrDefault(e.getVertexB().getId(), 0) < 3)
                .collect(Collectors.toList());

        // Sort clockwise by midpoint angle from board center (0,0 in tile-unit coords)
        coastal.sort(Comparator.comparingDouble(e -> {
            double[] a = vPos.get(e.getVertexA().getId());
            double[] b = vPos.get(e.getVertexB().getId());
            if (a == null || b == null) return 0.0;
            double mx = (a[0] + b[0]) / 2.0;
            double my = (a[1] + b[1]) / 2.0;
            double angle = Math.atan2(my, mx); // -π to π
            return angle < 0 ? angle + 2 * Math.PI : angle;
        }));

        // 9 ports: 4 generic 3:1 + one of each resource 2:1, shuffled
        List<PortType> portTypes = new ArrayList<>(List.of(
                PortType.GENERIC_3_1, PortType.GENERIC_3_1, PortType.GENERIC_3_1, PortType.GENERIC_3_1,
                PortType.WOOD_2_1, PortType.BRICK_2_1, PortType.WOOL_2_1, PortType.GRAIN_2_1, PortType.ORE_2_1
        ));
        Collections.shuffle(portTypes);

        // Place ports at 9 evenly-spaced positions around the coastal ring
        int total = coastal.size();
        for (int i = 0; i < 9; i++) {
            int idx = ((int) Math.round((double) i * total / 9.0)) % total;
            Edge portEdge = coastal.get(idx);
            PortType type = portTypes.get(i);
            portEdge.getVertexA().setPort(type);
            portEdge.getVertexB().setPort(type);
        }

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
        // Derive the reverse map: vertex -> tiles that touch it (up to 3)
        Map<Vertex, List<HexTile>> vertexToTiles = new HashMap<>();
        for (HexTile tile : tiles) {
            for (Vertex v : tileToVertices.getOrDefault(tile.getCoordinate(), List.of())) {
                vertexToTiles.computeIfAbsent(v, k -> new ArrayList<>()).add(tile);
            }
        }
        return new BoardState(tiles, vertices, edges, tileToVertices, vertexToTiles);
    }
}

