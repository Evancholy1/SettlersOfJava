package settlersofjava.ui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import settlersofjava.player.Player;
import settlersofjava.resources.ResourceType;
import settlersofjava.trade.PlayerTradeOffer;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Shown to each non-proposer player in sequence.
 * Displays the offer and lets the player accept or decline.
 */
public class PlayerTradeResponsePanel extends HBox {

    private final Circle playerCircle;
    private final Text   playerInitial;
    private final Label  playerNameLabel;
    private final HBox   offerBox;
    private final HBox   reqBox;
    private final Button acceptBtn;

    private Player                                        currentResponder;
    private BiConsumer<Player, PlayerTradeOffer.Response> onRespond;

    public PlayerTradeResponsePanel() {
        setVisible(false);
        setManaged(false);
        setStyle("-fx-background-color: #37474f;");
        setPadding(new Insets(10, 16, 10, 16));
        setSpacing(18);
        setAlignment(Pos.CENTER_LEFT);

        // ── Player icon (matches dashboard style) ─────────────────────────────
        playerCircle = new Circle(20);
        playerCircle.setFill(Color.LIGHTGRAY);
        playerCircle.setStroke(Color.WHITE);
        playerCircle.setStrokeWidth(2);

        playerInitial = new Text("?");
        playerInitial.setFont(Font.font("System", FontWeight.BOLD, 16));
        playerInitial.setFill(Color.WHITE);

        StackPane iconPane = new StackPane(playerCircle, playerInitial);

        playerNameLabel = new Label("—");
        playerNameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        playerNameLabel.setTextFill(Color.WHITE);

        VBox playerBox = new VBox(4, iconPane, playerNameLabel);
        playerBox.setAlignment(Pos.CENTER);

        // ── Offer display ─────────────────────────────────────────────────────
        VBox offerSection = new VBox(3);
        offerSection.setAlignment(Pos.CENTER_LEFT);
        Label offerTitle = new Label("▲ They give you");
        offerTitle.setFont(Font.font("System", FontWeight.BOLD, 10));
        offerTitle.setTextFill(Color.SEAGREEN);
        offerBox = new HBox(6);
        offerBox.setAlignment(Pos.CENTER_LEFT);
        offerSection.getChildren().addAll(offerTitle, offerBox);

        Label forLbl = new Label("for");
        forLbl.setFont(Font.font("System", 11));
        forLbl.setTextFill(Color.LIGHTGRAY);

        VBox reqSection = new VBox(3);
        reqSection.setAlignment(Pos.CENTER_LEFT);
        Label reqTitle = new Label("▼ You give them");
        reqTitle.setFont(Font.font("System", FontWeight.BOLD, 10));
        reqTitle.setTextFill(Color.FIREBRICK);
        reqBox = new HBox(6);
        reqBox.setAlignment(Pos.CENTER_LEFT);
        reqSection.getChildren().addAll(reqTitle, reqBox);

        // ── Buttons ───────────────────────────────────────────────────────────
        acceptBtn = new Button("✓ Accept");
        acceptBtn.setFont(Font.font("System", FontWeight.BOLD, 13));
        acceptBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-pref-width: 90; -fx-pref-height: 40;");
        acceptBtn.setOnAction(e -> respond(PlayerTradeOffer.Response.ACCEPTED));

        Button declineBtn = new Button("✕ Decline");
        declineBtn.setFont(Font.font("System", FontWeight.BOLD, 13));
        declineBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-pref-width: 90; -fx-pref-height: 40;");
        declineBtn.setOnAction(e -> respond(PlayerTradeOffer.Response.DECLINED));

        VBox btnBox = new VBox(8, acceptBtn, declineBtn);
        btnBox.setAlignment(Pos.CENTER);

        Separator divider = new Separator(Orientation.VERTICAL);
        divider.setStyle("-fx-background-color: #90A4AE; -fx-pref-width: 1;");

        getChildren().addAll(playerBox, divider, offerSection, forLbl, reqSection, btnBox);
        setMaxWidth(Region.USE_PREF_SIZE);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setOnRespond(BiConsumer<Player, PlayerTradeOffer.Response> cb) {
        this.onRespond = cb;
    }

    public void show(PlayerTradeOffer offer, Player responder) {
        this.currentResponder = responder;

        playerCircle.setFill(BoardView.playerColor(responder.getColor()));
        playerInitial.setText(String.valueOf(responder.getName().charAt(0)).toUpperCase());
        playerNameLabel.setText(responder.getName());

        rebuildMinis(offerBox, offer.getOffering());
        rebuildMinis(reqBox,   offer.getRequesting());

        acceptBtn.setDisable(!offer.canAffordToAccept(responder));

        setVisible(true);
        setManaged(true);
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
        currentResponder = null;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void respond(PlayerTradeOffer.Response response) {
        if (onRespond != null && currentResponder != null)
            onRespond.accept(currentResponder, response);
    }

    private void rebuildMinis(HBox box, Map<ResourceType, Integer> amounts) {
        box.getChildren().clear();
        for (var e : amounts.entrySet()) {
            if (e.getValue() <= 0) continue;
            box.getChildren().add(miniCard(e.getKey(), e.getValue()));
        }
    }

    private VBox miniCard(ResourceType type, int count) {
        Rectangle rect = new Rectangle(30, 42);
        rect.setFill(resourceColor(type));
        rect.setArcWidth(5);
        rect.setArcHeight(5);
        rect.setStroke(Color.web("#ffffff", 0.4));
        rect.setStrokeWidth(1);

        Label lbl = new Label("×" + count);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.WHITE);

        VBox card = new VBox(2, rect, lbl);
        card.setAlignment(Pos.CENTER);
        return card;
    }

    private Color resourceColor(ResourceType t) {
        return switch (t) {
            case WOOD  -> Color.FORESTGREEN;
            case BRICK -> Color.FIREBRICK;
            case SHEEP -> Color.LIMEGREEN;
            case WHEAT -> Color.GOLDENROD;
            case ORE   -> Color.SLATEGRAY;
        };
    }
}
