package settlersofjava.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import settlersofjava.resources.ResourceType;

import java.util.function.Consumer;

/**
 * Inline panel shown when a Monopoly card is played.
 * Reuses the poolCard visual style from TradingPanel.
 */
public class MonopolyPickerPanel extends HBox {

    private Consumer<ResourceType> onPick;

    public MonopolyPickerPanel() {
        setVisible(false);
        setManaged(false);
        setStyle("-fx-background-color: #263238;");
        setPadding(new Insets(8, 14, 8, 14));
        setSpacing(14);
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Region.USE_PREF_SIZE);

        Label prompt = new Label("Monopoly — pick a resource to claim from all players:");
        prompt.setFont(Font.font("System", FontWeight.BOLD, 12));
        prompt.setTextFill(Color.web("#90A4AE"));

        HBox cardRow = new HBox(8);
        cardRow.setAlignment(Pos.CENTER_LEFT);
        for (ResourceType type : ResourceType.values()) {
            VBox card = poolCard(type);
            card.setCursor(Cursor.HAND);
            card.setOnMouseClicked(e -> { if (onPick != null) onPick.accept(type); });
            cardRow.getChildren().add(card);
        }

        getChildren().addAll(prompt, cardRow);
    }

    public void setOnPick(Consumer<ResourceType> cb) { this.onPick = cb; }

    public void show() {
        setVisible(true);
        setManaged(true);
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
    }

    // ── Card builder (mirrors TradingPanel.poolCard) ──────────────────────────

    private VBox poolCard(ResourceType type) {
        Rectangle rect = new Rectangle(38, 52);
        rect.setFill(resourceColor(type));
        rect.setArcWidth(7);
        rect.setArcHeight(7);
        rect.setStroke(Color.web("#ffffff", 0.5));
        rect.setStrokeWidth(1.5);
        Label lbl = new Label(abbrev(type));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 10));
        lbl.setTextFill(Color.WHITE);
        VBox card = new VBox(2, rect, lbl);
        card.setAlignment(Pos.CENTER);
        card.setOnMouseEntered(e ->
            rect.setStyle("-fx-effect: dropshadow(gaussian, white, 6, 0.5, 0, 0);"));
        card.setOnMouseExited(e -> rect.setStyle(""));
        return card;
    }

    private String abbrev(ResourceType t) {
        return switch (t) {
            case WOOD  -> "Wd";
            case BRICK -> "Bk";
            case SHEEP -> "Sh";
            case WHEAT -> "Wh";
            case ORE   -> "Or";
        };
    }

    private Color resourceColor(ResourceType type) {
        return switch (type) {
            case WOOD  -> Color.FORESTGREEN;
            case BRICK -> Color.FIREBRICK;
            case SHEEP -> Color.LIMEGREEN;
            case WHEAT -> Color.GOLDENROD;
            case ORE   -> Color.SLATEGRAY;
        };
    }
}
