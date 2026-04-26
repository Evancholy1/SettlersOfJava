package settlersofjava.ui;

import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import settlersofjava.player.Player;
import settlersofjava.resources.ResourceType;

import java.util.function.Consumer;

/**
 * PATTERN: Observer (via JavaFX ObservableMap listener)
 * Displays a single resource card for one resource type.
 * The card hides itself entirely when the player's count reaches 0,
 * and reappears (with updated count) when they gain that resource.
 */
public class ResourceCardView extends VBox {

    public ResourceCardView(Player player, ResourceType type) {
        this(player, type, null);
    }

    public ResourceCardView(Player player, ResourceType type, Consumer<ResourceType> onClick) {
        setAlignment(Pos.CENTER);
        setSpacing(4);
        setPadding(new Insets(6));

        Rectangle card = new Rectangle(54, 76);
        card.setFill(resourceColor(type));
        card.setArcWidth(10);
        card.setArcHeight(10);
        card.setStroke(Color.BLACK);
        card.setStrokeWidth(1.5);

        Label nameLabel = new Label(type.name());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 10));

        Label countLabel = new Label();
        countLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        getChildren().addAll(card, nameLabel, countLabel);

        // OBSERVER: listen only to changes for this specific resource type
        Runnable refresh = () -> {
            int count = player.getResource(type);
            countLabel.setText("x" + count);
            boolean visible = count > 0;
            setVisible(visible);
            setManaged(visible);
            if (onClick != null) {
                boolean tradeable = count >= 1;
                card.setStroke(tradeable ? Color.GOLD : Color.BLACK);
                card.setStrokeWidth(tradeable ? 2.5 : 1.5);
                setCursor(tradeable ? Cursor.HAND : Cursor.DEFAULT);
            }
        };

        player.resourcesProperty().addListener(
            (MapChangeListener<ResourceType, Integer>) change -> {
                if (change.getKey() == type) refresh.run();
            }
        );

        if (onClick != null) {
            setOnMouseClicked(ev -> {
                if (player.getResource(type) >= 1) onClick.accept(type);
            });
            setOnMouseEntered(ev -> {
                if (player.getResource(type) >= 1)
                    card.setStyle("-fx-effect: dropshadow(gaussian, gold, 8, 0.6, 0, 0);");
            });
            setOnMouseExited(ev -> card.setStyle(""));
        }

        refresh.run(); // set initial state immediately
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
