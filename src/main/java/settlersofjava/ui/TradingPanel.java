package settlersofjava.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import settlersofjava.player.Player;
import settlersofjava.resources.ResourceType;

/**
 * Inline staging panel that appears above the PlayerDashboard when a player
 * initiates a bank trade.
 *
 * Flow:
 *   1. Player clicks a resource card in their hand  → stageOne() is called.
 *   2. Each additional click on that card adds one more to the staging area.
 *   3. Clicking a staged card returns one resource to the player's hand.
 *   4. Player clicks a receive-pool card to select what they want back.
 *   5. ✓ confirms (adds receive resource to player), ✕ cancels (returns all staged).
 *
 * Resources are temporarily removed from the player's ObservableMap while
 * staged so the dashboard counts update in real time, and restored on cancel.
 */
public class TradingPanel extends HBox {

    private Player       currentPlayer;
    private ResourceType giveType   = null;
    private int          giveCount  = 0;
    private int          tradeRate  = 4;
    private ResourceType receiveType = null;

    private final HBox  giveMiniBox;
    private final HBox  receiveOptionBox;
    private final Button confirmBtn;
    private final Label  giveCountLabel;
    private final Label  bankLabel;

    public TradingPanel() {
        setVisible(false);
        setManaged(false);

        setStyle("-fx-background-color: #263238;");
        setPadding(new Insets(10, 16, 10, 16));
        setSpacing(20);
        setAlignment(Pos.CENTER_LEFT);

        // ── Bank partner label ──────────────────────────────────────────────
        VBox bankSection = new VBox(4);
        bankSection.setAlignment(Pos.CENTER);
        Label bankIcon  = new Label("🏦");
        bankIcon.setFont(Font.font(22));
        bankLabel = new Label("Bank 4:1");
        bankLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        bankLabel.setTextFill(Color.LIGHTGRAY);
        bankSection.getChildren().addAll(bankIcon, bankLabel);

        // ── Give section ────────────────────────────────────────────────────
        VBox giveSection = new VBox(4);
        giveSection.setAlignment(Pos.CENTER_LEFT);

        HBox giveTitleRow = new HBox(6);
        giveTitleRow.setAlignment(Pos.CENTER_LEFT);
        Label giveArrow = new Label("▲");
        giveArrow.setFont(Font.font("System", FontWeight.BOLD, 13));
        giveArrow.setTextFill(Color.FIREBRICK);
        giveCountLabel = new Label("Give 0/4");
        giveCountLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        giveCountLabel.setTextFill(Color.LIGHTGRAY);
        giveTitleRow.getChildren().addAll(giveArrow, giveCountLabel);

        giveMiniBox = new HBox(4);
        giveMiniBox.setAlignment(Pos.CENTER_LEFT);
        giveMiniBox.setMinWidth(160);
        giveSection.getChildren().addAll(giveTitleRow, giveMiniBox);

        // ── Receive section ─────────────────────────────────────────────────
        VBox receiveSection = new VBox(4);
        receiveSection.setAlignment(Pos.CENTER_LEFT);

        HBox receiveTitleRow = new HBox(6);
        receiveTitleRow.setAlignment(Pos.CENTER_LEFT);
        Label receiveArrow = new Label("▼");
        receiveArrow.setFont(Font.font("System", FontWeight.BOLD, 13));
        receiveArrow.setTextFill(Color.SEAGREEN);
        Label receiveLabel = new Label("Receive 1");
        receiveLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        receiveLabel.setTextFill(Color.LIGHTGRAY);
        receiveTitleRow.getChildren().addAll(receiveArrow, receiveLabel);

        receiveOptionBox = new HBox(6);
        receiveOptionBox.setAlignment(Pos.CENTER_LEFT);
        buildReceiveOptions();
        receiveSection.getChildren().addAll(receiveTitleRow, receiveOptionBox);

        // ── Confirm / cancel ────────────────────────────────────────────────
        confirmBtn = new Button("✓");
        confirmBtn.setFont(Font.font("System", FontWeight.BOLD, 18));
        confirmBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-pref-width: 46; -fx-pref-height: 46;");
        confirmBtn.setDisable(true);
        confirmBtn.setOnAction(e -> confirmTrade());

        Button cancelBtn = new Button("✕");
        cancelBtn.setFont(Font.font("System", FontWeight.BOLD, 18));
        cancelBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-pref-width: 46; -fx-pref-height: 46;");
        cancelBtn.setOnAction(e -> cancelTrade());

        VBox buttonBox = new VBox(8, confirmBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox tradeStack = new VBox(8, receiveSection, giveSection);
        tradeStack.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(bankSection, tradeStack, spacer, buttonBox);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Called by GameController when the player clicks a resource card.
     * Switching to a different resource type while staging automatically
     * returns the previously staged cards.
     */
    public void stageOne(ResourceType type, Player player, int rate) {
        if (player.getResource(type) <= 0) return;

        // Switching type: return everything staged so far and adopt new rate
        if (giveType != null && type != giveType) {
            player.addResource(giveType, giveCount);
            giveCount = 0;
            giveType  = null;
        }

        tradeRate = rate;
        bankLabel.setText(rate == 4 ? "Bank 4:1" : "Port " + rate + ":1");

        if (giveCount >= tradeRate) return;

        currentPlayer = player;
        giveType      = type;
        player.removeResource(type, 1);
        giveCount++;

        setVisible(true);
        setManaged(true);
        rebuild();
    }

    /** Return all staged resources and hide the panel (called on turn end or cancel). */
    public void reset() {
        if (currentPlayer != null && giveType != null && giveCount > 0)
            currentPlayer.addResource(giveType, giveCount);
        clearState();
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void unstageOne() {
        if (giveCount <= 0 || currentPlayer == null) return;
        currentPlayer.addResource(giveType, 1);
        giveCount--;
        if (giveCount == 0) {
            giveType = null;
            clearState();
            return;
        }
        rebuild();
    }

    private void confirmTrade() {
        if (receiveType == null || giveCount < tradeRate || currentPlayer == null) return;
        currentPlayer.addResource(receiveType, 1);
        clearState();
    }

    private void cancelTrade() {
        reset();
    }

    private void clearState() {
        giveType      = null;
        giveCount     = 0;
        receiveType   = null;
        currentPlayer = null;
        giveMiniBox.getChildren().clear();
        tradeRate = 4;
        bankLabel.setText("Bank 4:1");
        giveCountLabel.setText("Give 0/4");
        buildReceiveOptions();
        confirmBtn.setDisable(true);
        setVisible(false);
        setManaged(false);
    }

    private void rebuild() {
        // Give cards — click any card to return one to hand
        giveMiniBox.getChildren().clear();
        if (giveType != null) {
            for (int i = 0; i < giveCount; i++) {
                VBox card = miniCard(giveType);
                card.setOnMouseClicked(e -> unstageOne());
                card.setCursor(Cursor.HAND);
                card.setStyle("-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.25), 4, 0, 0, 0);");
                giveMiniBox.getChildren().add(card);
            }
        }
        giveCountLabel.setText("Give " + giveCount + "/" + tradeRate);

        buildReceiveOptions();
        confirmBtn.setDisable(giveCount < tradeRate || receiveType == null);
    }

    private void buildReceiveOptions() {
        receiveOptionBox.getChildren().clear();
        for (ResourceType type : ResourceType.values()) {
            boolean selected   = type == receiveType;
            boolean isGiveType = type == giveType;

            VBox card = miniCard(type);
            if (selected) {
                card.setStyle("-fx-border-color: #fff176; -fx-border-width: 2;"
                        + "-fx-border-radius: 4; -fx-background-radius: 4;"
                        + "-fx-background-color: rgba(255,241,118,0.15);");
            } else if (isGiveType) {
                card.setOpacity(0.35);
            }

            final ResourceType t = type;
            card.setOnMouseClicked(e -> {
                receiveType = (receiveType == t) ? null : t;
                buildReceiveOptions();
                confirmBtn.setDisable(giveCount < tradeRate || receiveType == null);
            });
            card.setCursor(Cursor.HAND);
            receiveOptionBox.getChildren().add(card);
        }
    }

    private VBox miniCard(ResourceType type) {
        Rectangle rect = new Rectangle(36, 50);
        rect.setFill(resourceColor(type));
        rect.setArcWidth(6);
        rect.setArcHeight(6);
        rect.setStroke(Color.web("#ffffff", 0.4));
        rect.setStrokeWidth(1.2);

        Label lbl = new Label(type.name());
        lbl.setFont(Font.font("System", FontWeight.BOLD, 9));
        lbl.setTextFill(Color.WHITE);

        VBox card = new VBox(2, rect, lbl);
        card.setAlignment(Pos.CENTER);
        return card;
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
