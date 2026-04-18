package settlersofjava.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import settlersofjava.board.*;
import settlersofjava.player.PlayerColor;

import java.util.*;
import java.util.function.Consumer;

/**
 * Renders the hex board on a JavaFX Pane using absolute pixel coordinates.
 * Exposes click callbacks for vertices and edges, and accepts highlight sets
 * so GameController can drive valid-placement indicators.
 */
public class BoardView extends Pane {

    private final BoardState boardState;
    private static final double HEX_SIZE      = 55.0;
    private static final double BOARD_CENTER_X = 640.0;
    private static final double BOARD_CENTER_Y = 370.0;

    /** vertexId → [screenX, screenY] */
    private final Map<Integer, double[]> vertexPos = new HashMap<>();

    private Consumer<Vertex> onVertexClick;
    private Consumer<Edge>   onEdgeClick;

    private Set<Vertex> highlightedVertices = new HashSet<>();
    private Set<Edge>   highlightedEdges    = new HashSet<>();

    public BoardView(BoardState boardState) {
        this.boardState = boardState;
        drawBoard();
    }

    public void setOnVertexClick(Consumer<Vertex> handler) { this.onVertexClick = handler; }
    public void setOnEdgeClick(Consumer<Edge> handler)     { this.onEdgeClick   = handler; }

    public void setHighlightedVertices(Set<Vertex> vertices) {
        this.highlightedVertices = vertices;
        drawBoard();
    }

    public void setHighlightedEdges(Set<Edge> edges) {
        this.highlightedEdges = edges;
        drawBoard();
    }

    public void refresh() { drawBoard(); }

    // ── Drawing ───────────────────────────────────────────────────────────────

    private void drawBoard() {
        getChildren().clear();
        vertexPos.clear();

        // First pass: compute vertex pixel positions.
        // Vertex i in withVerticesAndEdges sits between dirs[i] and dirs[(i+1)%6].
        // The polygon draw loop uses +60*i-30 (clockwise), but the vertex numbering
        // in withVerticesAndEdges goes counterclockwise, so the correct formula is
        // -60*i - 30 degrees. Both start at the same corner (i=0 → -30°/upper-right)
        // but wind in opposite directions.
        for (HexTile tile : boardState.getTiles()) {
            double[] center = tileCenter(tile);
            List<Vertex> verts = boardState.getVerticesFor(tile.getCoordinate());
            for (int i = 0; i < 6 && i < verts.size(); i++) {
                double angle = Math.toRadians(-60 * i - 30);
                vertexPos.put(verts.get(i).getId(), new double[]{
                    center[0] + HEX_SIZE * Math.cos(angle),
                    center[1] + HEX_SIZE * Math.sin(angle)
                });
            }
        }

        // Draw hex tiles
        for (HexTile tile : boardState.getTiles()) {
            double[] c = tileCenter(tile);
            Polygon hex = new Polygon();
            for (int i = 0; i < 6; i++) {
                double angle = Math.toRadians(60 * i - 30);
                hex.getPoints().addAll(
                    c[0] + HEX_SIZE * Math.cos(angle),
                    c[1] + HEX_SIZE * Math.sin(angle)
                );
            }
            hex.setFill(terrainColor(tile.getTerrainType()));
            hex.setStroke(Color.BLACK);
            hex.setStrokeWidth(2.0);
            getChildren().add(hex);

            if (tile instanceof ResourceTile rt) {
                int token = rt.getNumberToken();
                Text text = new Text(c[0] - 8, c[1] + 7, String.valueOf(token));
                text.setFont(Font.font("System", FontWeight.BOLD, 18));
                text.setFill(token == 6 || token == 8 ? Color.RED : Color.BLACK);
                getChildren().add(text);
            }
        }

        // Draw ports (behind edges and vertices)
        drawPorts();

        // Draw edges (roads or clickable highlights)
        for (Edge edge : boardState.getEdges()) {
            double[] a = vertexPos.get(edge.getVertexA().getId());
            double[] b = vertexPos.get(edge.getVertexB().getId());
            if (a == null || b == null) continue;

            if (edge.hasRoad()) {
                Line line = new Line(a[0], a[1], b[0], b[1]);
                line.setStroke(playerColor(edge.getRoad().getOwner().getColor()));
                line.setStrokeWidth(7);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
                getChildren().add(line);
            } else if (highlightedEdges.contains(edge)) {
                Line line = new Line(a[0], a[1], b[0], b[1]);
                line.setStroke(Color.YELLOW);
                line.setStrokeWidth(9);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
                line.setOpacity(0.75);
                final Edge e = edge;
                line.setOnMouseClicked(ev -> { if (onEdgeClick != null) onEdgeClick.accept(e); });
                getChildren().add(line);
            }
        }

        // Draw vertices (settlements or clickable highlights)
        for (Vertex v : boardState.getVertices()) {
            double[] pos = vertexPos.get(v.getId());
            if (pos == null) continue;

            if (v.isOccupied()) {
                Color fill = playerColor(v.getBuilding().getOwner().getColor());
                Circle c = new Circle(pos[0], pos[1], 11, fill);
                c.setStroke(Color.BLACK);
                c.setStrokeWidth(2);
                getChildren().add(c);
            } else if (highlightedVertices.contains(v)) {
                Circle c = new Circle(pos[0], pos[1], 11, Color.LIGHTGREEN);
                c.setStroke(Color.DARKGREEN);
                c.setStrokeWidth(2);
                c.setOpacity(0.85);
                final Vertex fv = v;
                c.setOnMouseClicked(ev -> { if (onVertexClick != null) onVertexClick.accept(fv); });
                getChildren().add(c);
            }
        }
    }

    // ── Port rendering ───────────────────────────────────────────────────────

    private void drawPorts() {
        for (Edge edge : boardState.getEdges()) {
            PortType portA = edge.getVertexA().getPort();
            PortType portB = edge.getVertexB().getPort();
            if (portA == null || portA != portB) continue;

            double[] a = vertexPos.get(edge.getVertexA().getId());
            double[] b = vertexPos.get(edge.getVertexB().getId());
            if (a == null || b == null) continue;

            // midpoint of the coastal edge, pushed outward from board center
            double mx = (a[0] + b[0]) / 2.0;
            double my = (a[1] + b[1]) / 2.0;
            double dx = mx - BOARD_CENTER_X;
            double dy = my - BOARD_CENTER_Y;
            double len = Math.sqrt(dx * dx + dy * dy);
            double px = mx + dx / len * 32;
            double py = my + dy / len * 32;

            // dashed dock lines from indicator to each port vertex
            for (double[] pt : new double[][]{a, b}) {
                Line dock = new Line(px, py, pt[0], pt[1]);
                dock.setStroke(Color.rgb(120, 80, 40));
                dock.setStrokeWidth(2.5);
                dock.getStrokeDashArray().addAll(6.0, 3.0);
                getChildren().add(dock);
            }

            // colored indicator circle
            Circle indicator = new Circle(px, py, 18);
            indicator.setFill(portColor(portA));
            indicator.setStroke(Color.BLACK);
            indicator.setStrokeWidth(1.5);
            getChildren().add(indicator);

            // port label centered on the circle
            Label label = new Label(portLabel(portA));
            label.setFont(Font.font("System", FontWeight.BOLD, 9));
            label.setTextFill(Color.WHITE);
            label.setPrefWidth(36);
            label.setAlignment(Pos.CENTER);
            label.setWrapText(true);
            label.setLayoutX(px - 18);
            label.setLayoutY(py - 9);
            getChildren().add(label);
        }
    }

    private Color portColor(PortType type) {
        return switch (type) {
            case GENERIC_3_1 -> Color.STEELBLUE;
            case WOOD_2_1    -> Color.SADDLEBROWN;
            case BRICK_2_1   -> Color.FIREBRICK;
            case WOOL_2_1    -> Color.LIMEGREEN;
            case GRAIN_2_1   -> Color.GOLDENROD;
            case ORE_2_1     -> Color.SLATEGRAY;
        };
    }

    private String portLabel(PortType type) {
        return switch (type) {
            case GENERIC_3_1 -> "3:1";
            case WOOD_2_1    -> "WOOD\n2:1";
            case BRICK_2_1   -> "BRICK\n2:1";
            case WOOL_2_1    -> "SHEEP\n2:1";
            case GRAIN_2_1   -> "WHEAT\n2:1";
            case ORE_2_1     -> "ORE\n2:1";
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double[] tileCenter(HexTile tile) {
        int q = tile.getCoordinate().getQ();
        int r = tile.getCoordinate().getR();
        return new double[]{
            HEX_SIZE * Math.sqrt(3) * (q + r / 2.0) + BOARD_CENTER_X,
            HEX_SIZE * 1.5 * r + BOARD_CENTER_Y
        };
    }

    private Color terrainColor(TerrainType type) {
        return switch (type) {
            case FOREST    -> Color.FORESTGREEN;
            case PASTURE   -> Color.LIMEGREEN;
            case FIELDS    -> Color.GOLD;
            case HILLS     -> Color.SADDLEBROWN;
            case MOUNTAINS -> Color.SLATEGRAY;
            case DESERT    -> Color.DARKGOLDENROD;
        };
    }

    static Color playerColor(PlayerColor c) {
        return switch (c) {
            case RED    -> Color.RED;
            case BLUE   -> Color.DODGERBLUE;
            case BLACK  -> Color.BLACK;
            case ORANGE -> Color.ORANGE;
        };
    }
}
