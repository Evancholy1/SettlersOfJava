package settlersofjava.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import settlersofjava.player.Player;
import settlersofjava.resources.ResourceType;

import java.util.function.BiConsumer;

/**
 * 4:1 bank trade modal.  Layout mirrors the reference screenshot:
 *   top row   — resource type buttons (select GIVE)
 *   mid panel — shows the 4 cards being offered (red arrow) and trade partner icon
 *   bot panel — shows the 1 card being received (green arrow), select by clicking top row
 *   right col — Confirm bank, (greyed) player trade, Cancel
 */
public class BankTradeModal {

    private final Stage stage;

    /**
     * @param player     the current player executing the trade
     * @param initial    the resource type clicked to open the modal (pre-selected give)
     * @param owner      main window (for modality)
     * @param onTrade    called with (give, receive) when the player confirms
     */
    public BankTradeModal(Player player,
                          ResourceType initial,
                          Window owner,
                          BiConsumer<ResourceType, ResourceType> onTrade) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Bank Trade");
        stage.setResizable(false);

        // ── State ─────────────────────────────────────────────────────────────
        ResourceType[] giveType    = { initial };
        ResourceType[] receiveType = { null };

        // ── Give card strip (4 mini cards) ────────────────────────────────────
        HBox giveMinis = new HBox(5);
        giveMinis.setAlignment(Pos.CENTER_LEFT);

        // ── Receive card strip (1 mini card, changes with selection) ──────────
        HBox receiveMinis = new HBox(5);
        receiveMinis.setAlignment(Pos.CENTER_LEFT);

        // ── Mid panel (give row) ───────────────────────────────────────────────
        Label giveArrow = new Label("▲");
        giveArrow.setFont(Font.font("System", FontWeight.BOLD, 18));
        giveArrow.setTextFill(Color.FIREBRICK);

        Label giveCount = new Label("×4");
        giveCount.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox giveRow = new HBox(8, giveArrow, giveMinis, giveCount);
        giveRow.setAlignment(Pos.CENTER_LEFT);
        giveRow.setPadding(new Insets(8, 12, 8, 12));
        giveRow.setStyle("-fx-background-color: #f8ece8; -fx-background-radius: 6;");

        // ── Bot panel (receive row) ────────────────────────────────────────────
        Label receiveArrow = new Label("▼");
        receiveArrow.setFont(Font.font("System", FontWeight.BOLD, 18));
        receiveArrow.setTextFill(Color.SEAGREEN);

        Label receiveCount = new Label("×1");
        receiveCount.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label receiveHint = new Label("← select above");
        receiveHint.setFont(Font.font("System", 11));
        receiveHint.setTextFill(Color.GRAY);

        HBox receiveRow = new HBox(8, receiveArrow, receiveMinis, receiveCount, receiveHint);
        receiveRow.setAlignment(Pos.CENTER_LEFT);
        receiveRow.setPadding(new Insets(8, 12, 8, 12));
        receiveRow.setStyle("-fx-background-color: #eaf8ec; -fx-background-radius: 6;");

        // ── Confirm button (right column) ──────────────────────────────────────
        Button confirmBtn = new Button("🏦 Trade");
        confirmBtn.setFont(Font.font("System", FontWeight.BOLD, 13));
        confirmBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8; -fx-pref-width: 100;");
        confirmBtn.setDisable(true);

        Button playerTradeBtn = new Button("👥 Trade");
        playerTradeBtn.setFont(Font.font("System", 13));
        playerTradeBtn.setStyle("-fx-background-color: #bbb; -fx-text-fill: white; -fx-background-radius: 8; -fx-pref-width: 100;");
        playerTradeBtn.setDisable(true); // player-to-player not yet implemented

        Button cancelBtn = new Button("✕");
        cancelBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        cancelBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-background-radius: 8; -fx-pref-width: 100;");
        cancelBtn.setOnAction(e -> stage.close());

        VBox rightCol = new VBox(10, confirmBtn, playerTradeBtn, cancelBtn);
        rightCol.setAlignment(Pos.TOP_CENTER);
        rightCol.setPadding(new Insets(8, 8, 8, 8));

        // ── Top row: resource type buttons ────────────────────────────────────
        HBox typeRow = new HBox(6);
        typeRow.setAlignment(Pos.CENTER_LEFT);
        typeRow.setPadding(new Insets(10, 12, 10, 12));
        typeRow.setStyle("-fx-background-color: #37474f; -fx-background-radius: 0 0 0 0;");

        Label giveSelectLabel = new Label("Give:");
        giveSelectLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        giveSelectLabel.setTextFill(Color.WHITE);
        typeRow.getChildren().add(giveSelectLabel);

        Button[] typeBtns = new Button[ResourceType.values().length];
        int idx = 0;
        for (ResourceType type : ResourceType.values()) {
            Button btn = new Button(shortName(type));
            btn.setFont(Font.font("System", FontWeight.BOLD, 11));
            btn.setStyle(typeButtonStyle(type, type == initial));
            final int i = idx;
            final ResourceType t = type;
            btn.setOnAction(e -> {
                // Update give selection
                giveType[0] = t;
                receiveType[0] = null;
                confirmBtn.setDisable(true);
                for (Button b : typeBtns) {
                    if (b != null) {
                        ResourceType bt = ResourceType.values()[indexOf(typeBtns, b)];
                        b.setStyle(typeButtonStyle(bt, bt == t));
                    }
                }
                rebuildGiveMinis(giveMinis, t);
                rebuildReceiveMinis(receiveMinis, null);
                receiveHint.setVisible(true);
                receiveHint.setManaged(true);
            });
            typeBtns[idx++] = btn;
            typeRow.getChildren().add(btn);
        }

        // Bank icon label at end of type row
        Label bankIcon = new Label("🏦");
        bankIcon.setFont(Font.font(18));
        typeRow.getChildren().add(bankIcon);

        // Build initial give minis
        rebuildGiveMinis(giveMinis, initial);

        // ── Wire receive selection via give-card click ─────────────────────────
        // When user clicks a type button for receiving, we need a separate mechanism.
        // We re-use the type row: first click = set give; but that's not ideal.
        // Instead: add a second row of dimmer buttons for "receive".
        HBox receiveTypeRow = new HBox(6);
        receiveTypeRow.setAlignment(Pos.CENTER_LEFT);
        receiveTypeRow.setPadding(new Insets(6, 12, 6, 12));
        receiveTypeRow.setStyle("-fx-background-color: #546e7a;");

        Label receiveSelectLabel = new Label("Receive:");
        receiveSelectLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        receiveSelectLabel.setTextFill(Color.WHITE);
        receiveTypeRow.getChildren().add(receiveSelectLabel);

        Button[] recvBtns = new Button[ResourceType.values().length];
        int rIdx = 0;
        for (ResourceType type : ResourceType.values()) {
            Button btn = new Button(shortName(type));
            btn.setFont(Font.font("System", FontWeight.BOLD, 11));
            btn.setStyle(recvButtonStyle(type, false, type == giveType[0]));
            final ResourceType t = type;
            btn.setOnAction(e -> {
                if (t == giveType[0]) return; // can't receive same as give
                receiveType[0] = t;
                for (int j = 0; j < recvBtns.length; j++) {
                    if (recvBtns[j] != null) {
                        ResourceType bt = ResourceType.values()[j];
                        recvBtns[j].setStyle(recvButtonStyle(bt, bt == t, bt == giveType[0]));
                    }
                }
                rebuildReceiveMinis(receiveMinis, t);
                receiveHint.setVisible(false);
                receiveHint.setManaged(false);
                boolean canTrade = player.getResource(giveType[0]) >= 4;
                confirmBtn.setDisable(!canTrade);
            });
            recvBtns[rIdx++] = btn;
            receiveTypeRow.getChildren().add(btn);
        }

        // Re-wire give type buttons to also reset receive buttons
        for (int j = 0; j < typeBtns.length; j++) {
            if (typeBtns[j] == null) continue;
            final ResourceType giveT = ResourceType.values()[j];
            typeBtns[j].setOnAction(e -> {
                giveType[0] = giveT;
                receiveType[0] = null;
                confirmBtn.setDisable(true);
                for (int k = 0; k < typeBtns.length; k++) {
                    if (typeBtns[k] != null)
                        typeBtns[k].setStyle(typeButtonStyle(ResourceType.values()[k], ResourceType.values()[k] == giveT));
                }
                for (int k = 0; k < recvBtns.length; k++) {
                    if (recvBtns[k] != null)
                        recvBtns[k].setStyle(recvButtonStyle(ResourceType.values()[k], false, ResourceType.values()[k] == giveT));
                }
                rebuildGiveMinis(giveMinis, giveT);
                rebuildReceiveMinis(receiveMinis, null);
                receiveHint.setVisible(true);
                receiveHint.setManaged(true);
            });
        }

        // ── Confirm wiring ─────────────────────────────────────────────────────
        confirmBtn.setOnAction(e -> {
            if (receiveType[0] == null) return;
            onTrade.accept(giveType[0], receiveType[0]);
            stage.close();
        });

        // ── Layout assembly ────────────────────────────────────────────────────
        VBox midLeft = new VBox(12, giveRow, receiveRow);
        midLeft.setPadding(new Insets(12));
        VBox.setVgrow(midLeft, Priority.ALWAYS);

        HBox body = new HBox(0, midLeft, rightCol);
        HBox.setHgrow(midLeft, Priority.ALWAYS);

        VBox root = new VBox(0, typeRow, receiveTypeRow, body);
        root.setStyle("-fx-background-color: #455a64;");

        Scene scene = new Scene(root, 480, 240);
        stage.setScene(scene);
    }

    public void show() { stage.show(); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void rebuildGiveMinis(HBox box, ResourceType type) {
        box.getChildren().clear();
        for (int i = 0; i < 4; i++) box.getChildren().add(miniCard(type));
    }

    private void rebuildReceiveMinis(HBox box, ResourceType type) {
        box.getChildren().clear();
        if (type != null) box.getChildren().add(miniCard(type));
    }

    private Rectangle miniCard(ResourceType type) {
        Rectangle r = new Rectangle(28, 40);
        r.setFill(resourceColor(type));
        r.setArcWidth(5);
        r.setArcHeight(5);
        r.setStroke(Color.WHITE);
        r.setStrokeWidth(1.2);
        return r;
    }

    private String shortName(ResourceType type) {
        return switch (type) {
            case WOOD  -> "🌲 Wood";
            case BRICK -> "🧱 Brick";
            case SHEEP -> "🐑 Sheep";
            case WHEAT -> "🌾 Wheat";
            case ORE   -> "⛏ Ore";
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

    private String typeButtonStyle(ResourceType type, boolean selected) {
        String bg = toHex(resourceColor(type));
        String border = selected ? "-fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 6;" : "";
        return "-fx-background-color: " + bg + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-opacity: "
                + (selected ? "1.0" : "0.7") + "; " + border;
    }

    private String recvButtonStyle(ResourceType type, boolean selected, boolean isGiveType) {
        String bg = toHex(resourceColor(type));
        double opacity = isGiveType ? 0.3 : (selected ? 1.0 : 0.65);
        String border = selected ? "-fx-border-color: #fff176; -fx-border-width: 2; -fx-border-radius: 6;" : "";
        return "-fx-background-color: " + bg + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-opacity: "
                + opacity + "; " + border;
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    private int indexOf(Button[] arr, Button b) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == b) return i;
        return 0;
    }
}
