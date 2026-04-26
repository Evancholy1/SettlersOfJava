package settlersofjava.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import settlersofjava.board.*;
import settlersofjava.buildings.City;
import settlersofjava.events.EventBus;
import settlersofjava.events.GameEvent;
import settlersofjava.events.GameEventListener;
import settlersofjava.player.PlayerColor;

import java.util.*;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the hex board on a JavaFX Pane using absolute pixel coordinates.
 * Exposes click callbacks for vertices and edges, and accepts highlight sets
 * so GameController can drive valid-placement indicators.
 */
public class BoardView extends Pane implements GameEventListener {

    private final BoardState boardState;
    private static final double HEX_SIZE = 55.0;

    /** vertexId → [screenX, screenY] */
    private final Map<Integer, double[]> vertexPos = new HashMap<>();

    private Consumer<Vertex>  onVertexClick;
    private Consumer<Edge>    onEdgeClick;
    private Consumer<HexTile> onTileClick;

    private Set<Vertex>  highlightedVertices    = new HashSet<>();
    private Set<Vertex>  highlightedCityUpgrades = new HashSet<>();
    private Set<Edge>    highlightedEdges        = new HashSet<>();
    private Set<HexTile> highlightedTiles        = new HashSet<>();

    private final Image     robberImage;
    private final ImageView robberView;

    // Live DropShadow objects on city-upgrade circles — animated in-place, no redraw needed
    private final List<DropShadow> cityGlowEffects = new ArrayList<>();
    private final SimpleDoubleProperty glowRadius  = new SimpleDoubleProperty(6.0);
    private Timeline cityPulse;

    private final Set<HexTile> flashedTiles = new HashSet<>();
    private PauseTransition flashTimer;

    public BoardView(BoardState boardState) {
        this.boardState = boardState;
        robberImage = new Image(
                getClass().getResourceAsStream("/settlersofjava/ui/robber/robber.png"));
        robberView  = new ImageView(robberImage);
        robberView.setFitWidth(38);
        robberView.setFitHeight(38);
        robberView.setPreserveRatio(true);
        robberView.setMouseTransparent(true);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        EventBus.getInstance().register(this);
        // Update existing glow effects in-place — avoids wiping nodes between animation ticks
        glowRadius.addListener((obs, o, n) ->
            cityGlowEffects.forEach(ds -> ds.setRadius(n.doubleValue())));
        widthProperty().addListener((obs, o, n) -> drawBoard());
        heightProperty().addListener((obs, o, n) -> drawBoard());
        drawBoard();
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {
        if (event == GameEvent.ROBBER_MOVED) { animateRobberMove(); return; }
        if (event != GameEvent.DICE_ROLLED) return;
        int[] roll  = (int[]) payload;
        int   total = roll[0] + roll[1];

        // Cancel any previous flash still in progress
        if (flashTimer != null) flashTimer.stop();

        flashedTiles.clear();
        flashedTiles.addAll(boardState.getActiveTilesFor(total));
        drawBoard();

        flashTimer = new PauseTransition(Duration.seconds(1));
        flashTimer.setOnFinished(e -> { flashedTiles.clear(); drawBoard(); });
        flashTimer.play();
    }

    public void setOnVertexClick(Consumer<Vertex>  handler) { this.onVertexClick = handler; }
    public void setOnEdgeClick(Consumer<Edge>     handler) { this.onEdgeClick   = handler; }
    public void setOnTileClick(Consumer<HexTile>  handler) { this.onTileClick   = handler; }

    public void setHighlightedTiles(Set<HexTile> tiles) {
        this.highlightedTiles = tiles;
        drawBoard();
    }

    public void setHighlightedVertices(Set<Vertex> vertices) {
        this.highlightedVertices = vertices;
        drawBoard();
    }

    public void setHighlightedEdges(Set<Edge> edges) {
        this.highlightedEdges = edges;
        drawBoard();
    }

    public void setHighlightedCityUpgrades(Set<Vertex> vertices) {
        this.highlightedCityUpgrades = vertices;
        if (cityPulse != null) cityPulse.stop();
        drawBoard(); // build nodes (and populate cityGlowEffects) once
        if (!vertices.isEmpty()) {
            cityPulse = new Timeline(
                new KeyFrame(Duration.ZERO,       new KeyValue(glowRadius, 6.0)),
                new KeyFrame(Duration.millis(650), new KeyValue(glowRadius, 22.0))
            );
            cityPulse.setAutoReverse(true);
            cityPulse.setCycleCount(Timeline.INDEFINITE);
            cityPulse.play();
        }
    }

    public void refresh() { drawBoard(); }

    // ── Drawing ───────────────────────────────────────────────────────────────

    private void drawBoard() {
        getChildren().clear();
        vertexPos.clear();
        cityGlowEffects.clear();

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
            Color fill = terrainColor(tile.getTerrainType());
            if (flashedTiles.contains(tile)) fill = fill.darker();
            hex.setFill(fill);
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

        // Draw robber placement highlights (semi-transparent overlay on valid tiles)
        for (HexTile tile : highlightedTiles) {
            double[] c = tileCenter(tile);
            Polygon overlay = new Polygon();
            for (int i = 0; i < 6; i++) {
                double angle = Math.toRadians(60 * i - 30);
                overlay.getPoints().addAll(
                    c[0] + HEX_SIZE * Math.cos(angle),
                    c[1] + HEX_SIZE * Math.sin(angle)
                );
            }
            overlay.setFill(Color.rgb(255, 200, 0, 0.35));
            overlay.setStroke(Color.ORANGE);
            overlay.setStrokeWidth(3);
            final HexTile t = tile;
            overlay.setOnMouseClicked(ev -> { if (onTileClick != null) onTileClick.accept(t); });
            getChildren().add(overlay);
        }

        // Robber icon — added last so it's always on top; position set here,
        // animation offset (translateX/Y) is managed by animateRobberMove().
        HexTile robber = boardState.getRobberTile();
        if (robber != null) {
            double[] c = tileCenter(robber);
            robberView.setLayoutX(c[0] - 19);
            robberView.setLayoutY(c[1] - 19);
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

        // Draw vertices (cities, settlements, or clickable highlights)
        for (Vertex v : boardState.getVertices()) {
            double[] pos = vertexPos.get(v.getId());
            if (pos == null) continue;

            if (v.isOccupied()) {
                Color fill = playerColor(v.getBuilding().getOwner().getColor());
                boolean upgradeTarget = highlightedCityUpgrades.contains(v);
                if (v.getBuilding() instanceof City) {
                    drawDiamond(pos, fill, false, null);
                } else if (upgradeTarget) {
                    // Settlement eligible for city upgrade — glow is animated in-place via cityGlowEffects
                    Circle c = new Circle(pos[0], pos[1], 11, fill);
                    c.setStroke(Color.YELLOW);
                    c.setStrokeWidth(3);
                    DropShadow glow = new DropShadow(glowRadius.get(), Color.YELLOW);
                    glow.setSpread(0.45);
                    c.setEffect(glow);
                    cityGlowEffects.add(glow); // Timeline updates this directly, no redraw needed
                    final Vertex fv = v;
                    c.setOnMouseClicked(ev -> { if (onVertexClick != null) onVertexClick.accept(fv); });
                    getChildren().add(c);
                } else {
                    Circle c = new Circle(pos[0], pos[1], 11, fill);
                    c.setStroke(Color.BLACK);
                    c.setStrokeWidth(2);
                    getChildren().add(c);
                }
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

        // Robber always on top — re-added each redraw; translate offset preserved for animation
        if (boardState.getRobberTile() != null) getChildren().add(robberView);
    }

    private void drawDiamond(double[] pos, Color fill, boolean glowing, Vertex v) {
        double r = 13.0;
        Polygon diamond = new Polygon(
            pos[0],     pos[1] - r,
            pos[0] + r, pos[1],
            pos[0],     pos[1] + r,
            pos[0] - r, pos[1]
        );
        diamond.setFill(fill);
        diamond.setStroke(Color.BLACK);
        diamond.setStrokeWidth(2);
        if (glowing) {
            DropShadow glow = new DropShadow(glowRadius.get(), Color.YELLOW);
            glow.setSpread(0.45);
            diamond.setEffect(glow);
            diamond.setStroke(Color.YELLOW);
            final Vertex fv = v;
            diamond.setOnMouseClicked(ev -> { if (onVertexClick != null) onVertexClick.accept(fv); });
        }
        getChildren().add(diamond);
    }

    // ── Robber animation ──────────────────────────────────────────────────────

    private void animateRobberMove() {
        // Capture where the icon visually is right now (layout + any in-flight translate)
        double fromX = robberView.getLayoutX() + robberView.getTranslateX();
        double fromY = robberView.getLayoutY() + robberView.getTranslateY();

        // Redraw moves layoutX/Y to the new tile; translate is reset to zero by drawBoard
        robberView.setTranslateX(0);
        robberView.setTranslateY(0);
        drawBoard(); // sets robberView.layoutX/Y to destination

        // Offset translate so the icon still appears at fromX/Y visually
        robberView.setTranslateX(fromX - robberView.getLayoutX());
        robberView.setTranslateY(fromY - robberView.getLayoutY());

        // Animate translate → (0, 0) so the icon slides to its new layout position
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), robberView);
        tt.setToX(0);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
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
            double cx = getWidth()  > 0 ? getWidth()  / 2.0 : 640.0;
            double cy = getHeight() > 0 ? getHeight() / 2.0 : 370.0;
            double mx = (a[0] + b[0]) / 2.0;
            double my = (a[1] + b[1]) / 2.0;
            double dx = mx - cx;
            double dy = my - cy;
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
        double cx = getWidth()  > 0 ? getWidth()  / 2.0 : 640.0;
        double cy = getHeight() > 0 ? getHeight() / 2.0 : 370.0;
        return new double[]{
            HEX_SIZE * Math.sqrt(3) * (q + r / 2.0) + cx,
            HEX_SIZE * 1.5 * r + cy
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
            case ORANGE -> Color.web("#E65100");
        };
    }
}
