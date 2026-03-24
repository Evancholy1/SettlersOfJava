package settlersofjava.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import settlersofjava.board.BoardState;
import javafx.scene.layout.Pane;
import settlersofjava.board.HexTile;
import settlersofjava.board.ResourceTile;
import settlersofjava.board.TerrainType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.awt.*;

/**
 * Renders the hex board on a JavaFX Pane.
 * Observes BoardState changes and redraws affected tiles/vertices/edges.
 */
public class BoardView extends Pane {

    private final BoardState boardState;
    private static final double HEX_SIZE = 50.0;
    private static final double BOARD_CENTER_X = 640.0;
    private static final double BOARD_CENTER_Y = 360.0;

    public BoardView(BoardState boardState) {
        this.boardState = boardState;
        drawBoard();
    }

    private Color getColorForTerrain(TerrainType type) {
        return switch (type) {
          case FOREST -> Color.GREEN;
          case PASTURE -> Color.LIME;
          case FIELDS -> Color.YELLOW;
          case HILLS -> Color.ORANGE;
          case MOUNTAINS -> Color.GRAY;
          case DESERT -> Color.DARKGOLDENROD;
        };
    }

    private void drawBoard() {
        getChildren().clear();

        for(HexTile tile : boardState.getTiles()) {
            int q = tile.getCoordinate().getQ();
            int r = tile.getCoordinate().getR();

            // convert (q,r) axial to (x,y) pixel
            double x = HEX_SIZE * Math.sqrt(3) * (q +r / 2.0) + BOARD_CENTER_X;
            double y = HEX_SIZE * 3.0 / 2.0 * r + BOARD_CENTER_Y;

            // hexagon shape
            Polygon hex = new Polygon();
            for (int i = 0; i < 6; i++) {
                double angle_deg = 60 * i - 30;
                double angle_rad = Math.PI / 180 * angle_deg;
                hex.getPoints().addAll(
                        HEX_SIZE * Math.cos(angle_rad),
                        HEX_SIZE * Math.sin(angle_rad)
                );
            }
            hex.setFill(getColorForTerrain(tile.getTerrainType()));
            hex.setStroke(Color.BLACK);
            hex.setStrokeWidth(2.0);

            StackPane tileGroup = new StackPane();
            tileGroup.getChildren().add(hex);

            if(tile instanceof ResourceTile resourceTile) {
                int token = resourceTile.getNumberToken();
                Text numberText = new Text(String.valueOf(token));
                numberText.setFont(Font.font("System", FontWeight.BOLD, 18));
                if(token == 6 || token == 8) {
                    numberText.setFill(Color.RED);
                }
                tileGroup.getChildren().add(numberText);
            }
            tileGroup.setLayoutX(x - HEX_SIZE);
            tileGroup.setLayoutY(y - HEX_SIZE);
            getChildren().add(tileGroup);
        }
    }

    public void refresh() {
        // TODO: redraw roads, settlements, cities after a change
        drawBoard();
    }
}

