package settlersofjava.ui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import settlersofjava.player.Player;
import settlersofjava.resources.ResourceType;

import java.util.function.Consumer;

/**
 * Bottom HUD panel. Shows the active player's color icon + name on the left,
 * and their resource hand (cards) on the right.
 *
 * PATTERN: Observer — resource cards bind to the player's ObservableMap so
 * counts update automatically; switchToPlayer() rebuilds the view on turn change.
 */
public class PlayerDashboard extends HBox {

    private final Circle  iconCircle;
    private final Text    iconInitial;
    private final Label   nameLabel;
    private final HBox    cardsBox;
    private final HBox    devCardsBox;
    private java.util.function.Consumer<settlersofjava.cards.DevelopmentCard> onDevCardPlay;
    private final Label vpLabel;

    private Consumer<ResourceType> onResourceCardClick;

    public PlayerDashboard() {
        setSpacing(16);
        setPadding(new Insets(10, 16, 10, 16));
        setAlignment(Pos.CENTER_LEFT);

        // ── Player icon ───────────────────────────────────────────────────────
        iconCircle = new Circle(22);
        iconCircle.setFill(Color.LIGHTGRAY);
        iconCircle.setStroke(Color.BLACK);
        iconCircle.setStrokeWidth(2);

        iconInitial = new Text("?");
        iconInitial.setFont(Font.font("System", FontWeight.BOLD, 18));
        iconInitial.setFill(Color.WHITE);

        StackPane icon = new StackPane(iconCircle, iconInitial);

        nameLabel = new Label("—");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        vpLabel = new Label("Total VPs: 0");
        vpLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        vpLabel.setTextFill(Color.web("#B8860B")); // Gold/Yellow color for VPs

        VBox playerInfo = new VBox(6, vpLabel, icon, nameLabel);
        playerInfo.setAlignment(Pos.CENTER);

        // ── Resource cards ────────────────────────────────────────────────────
        cardsBox = new HBox(8);
        cardsBox.setAlignment(Pos.CENTER_LEFT);

        // ── Dev cards ─────────────────────────────────────────────────────────
        devCardsBox = new HBox(8);
        devCardsBox.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(playerInfo, new Separator(Orientation.VERTICAL), cardsBox, new Separator(Orientation.VERTICAL), devCardsBox);
    }

    public void setOnDevCardPlay(java.util.function.Consumer<settlersofjava.cards.DevelopmentCard> handler) {
        this.onDevCardPlay = handler;
    }

    /**
     * Called by GameController whenever the active player changes.
     * Updates the icon color + initial, and rebuilds the card row bound
     * to the new player's resource map.
     */
    public void setOnResourceCardClick(Consumer<ResourceType> handler) {
        this.onResourceCardClick = handler;
    }

    public void switchToPlayer(Player player) {
        iconCircle.setFill(BoardView.playerColor(player.getColor()));
        iconInitial.setText(String.valueOf(player.getName().charAt(0)).toUpperCase());
        nameLabel.setText(player.getName());
        vpLabel.setText("Total VPs: " + player.getTotalVictoryPoints());

        cardsBox.getChildren().clear();
        for (ResourceType type : ResourceType.values()) {
            cardsBox.getChildren().add(new ResourceCardView(player, type, onResourceCardClick));
        }

        devCardsBox.getChildren().clear();
        for (settlersofjava.cards.DevelopmentCard card : player.getDevCards()) {
            devCardsBox.getChildren().add(new DevCardView(card, () -> {
                if (onDevCardPlay != null) onDevCardPlay.accept(card);
            }));
        }
    }
}
