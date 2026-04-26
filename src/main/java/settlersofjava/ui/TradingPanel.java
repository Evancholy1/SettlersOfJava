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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Unified trade panel — bank/port and player-to-player in one UI.
 *
 * Layout:
 *   ┌─ top pool row ──────────────────────────────── ┐ │ buttons │
 *   │  [Wd] [Bk] [Sh] [Wh] [Or]  ← click to request │ │ [Bank]  │
 *   ├─ ▼ Receiving ───────────────────────────────── ┤ │[Players]│
 *   │  (receive mini cards — click to remove)         │ │  [✕]   │
 *   ├─ ▲ Offering ────────────────────────────────── ┤ │         │
 *   │  (staged give cards — click to unstage)         │ └─────────┘
 *   └─────────────────────────────────────────────────┘
 *
 * Give side: clicking a hand card calls stageResource() which removes it from
 * the player's ObservableMap (staging). Cleared panel returns them automatically.
 *
 * Receive side: clicking the top pool adds a card; clicking a receive card removes it.
 * No spinners — same click-to-add / click-to-remove mechanic on both sides.
 *
 * On "🤝 Players": staged give resources are NOT returned — GameController must
 * restore them from pendingOffer.getOffering() if the trade is cancelled.
 */
public class TradingPanel extends HBox {

    private static final ResourceType[] TYPES = ResourceType.values();

    private Player currentPlayer;
    private final Map<ResourceType, Integer> givingStaged = new EnumMap<>(ResourceType.class);
    private final List<ResourceType>         receivingList = new ArrayList<>();
    private final Map<ResourceType, Integer> bestRates    = new EnumMap<>(ResourceType.class);

    private final HBox giveCardRow;
    private final HBox receiveCardRow;
    private final Label giveHint;
    private final Label receiveHint;

    private final Button bankBtn;
    private final Button proposeBtn;

    private BiConsumer<Map<ResourceType, Integer>, ResourceType>               onBankTrade;
    private BiConsumer<Map<ResourceType, Integer>, Map<ResourceType, Integer>> onPropose;
    private Runnable onCancel;

    public TradingPanel() {
        setVisible(false);
        setManaged(false);
        setStyle("-fx-background-color: #263238;");
        setPadding(new Insets(8, 14, 8, 14));
        setSpacing(14);
        setAlignment(Pos.CENTER_LEFT);

        // ── Top pool row (infinite receive picker) ────────────────────────────
        HBox poolRow = new HBox(8);
        poolRow.setAlignment(Pos.CENTER_LEFT);
        for (ResourceType type : TYPES) {
            VBox card = poolCard(type);
            card.setOnMouseClicked(e -> addReceive(type));
            card.setCursor(Cursor.HAND);
            poolRow.getChildren().add(card);
        }
        Label poolHdr = new Label("Request →");
        poolHdr.setFont(Font.font("System", FontWeight.BOLD, 10));
        poolHdr.setTextFill(Color.web("#90A4AE"));
        HBox poolSection = new HBox(8, poolHdr, poolRow);
        poolSection.setAlignment(Pos.CENTER_LEFT);

        // ── Receive row (▼ green) ─────────────────────────────────────────────
        Label recArrow = new Label("▼");
        recArrow.setFont(Font.font("System", FontWeight.BOLD, 15));
        recArrow.setTextFill(Color.SEAGREEN);
        receiveCardRow = new HBox(4);
        receiveCardRow.setAlignment(Pos.CENTER_LEFT);
        receiveCardRow.setMinWidth(200);
        receiveHint = new Label("click above to add");
        receiveHint.setFont(Font.font("System", 10));
        receiveHint.setTextFill(Color.web("#546E7A"));
        HBox receiveRow = new HBox(6, recArrow, receiveCardRow, receiveHint);
        receiveRow.setAlignment(Pos.CENTER_LEFT);

        // ── Give row (▲ red) ──────────────────────────────────────────────────
        Label giveArrow = new Label("▲");
        giveArrow.setFont(Font.font("System", FontWeight.BOLD, 15));
        giveArrow.setTextFill(Color.FIREBRICK);
        giveCardRow = new HBox(4);
        giveCardRow.setAlignment(Pos.CENTER_LEFT);
        giveCardRow.setMinWidth(200);
        giveHint = new Label("click cards in your hand");
        giveHint.setFont(Font.font("System", 10));
        giveHint.setTextFill(Color.web("#546E7A"));
        HBox giveRow = new HBox(6, giveArrow, giveCardRow, giveHint);
        giveRow.setAlignment(Pos.CENTER_LEFT);

        // ── Stack: pool / receive / give ──────────────────────────────────────
        VBox mainStack = new VBox(6, poolSection, receiveRow, giveRow);
        mainStack.setAlignment(Pos.CENTER_LEFT);

        // ── Buttons ───────────────────────────────────────────────────────────
        bankBtn = new Button("🏦 Bank");
        bankBtn.setFont(Font.font("System", FontWeight.BOLD, 11));
        bankBtn.setStyle("-fx-background-color: #546E7A; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-pref-width: 100; -fx-pref-height: 32;");
        bankBtn.setDisable(true);
        bankBtn.setOnAction(e -> doBankTrade());

        proposeBtn = new Button("🤝 Players");
        proposeBtn.setFont(Font.font("System", FontWeight.BOLD, 11));
        proposeBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-pref-width: 100; -fx-pref-height: 32;");
        proposeBtn.setDisable(true);
        proposeBtn.setOnAction(e -> doPropose());

        Button cancelBtn = new Button("✕");
        cancelBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        cancelBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-pref-width: 36; -fx-pref-height: 36;");
        cancelBtn.setOnAction(e -> cancelTrade());

        VBox btnBox = new VBox(5, bankBtn, proposeBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER);

        getChildren().addAll(mainStack, btnBox);
        setMaxWidth(Region.USE_PREF_SIZE);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setOnBankTrade(BiConsumer<Map<ResourceType, Integer>, ResourceType> cb) {
        this.onBankTrade = cb;
    }

    public void setOnPropose(BiConsumer<Map<ResourceType, Integer>, Map<ResourceType, Integer>> cb) {
        this.onPropose = cb;
    }

    public void setOnCancel(Runnable cb) {
        this.onCancel = cb;
    }

    public void stageResource(ResourceType type, Player player, int rate) {
        if (player.getResource(type) <= 0) return;
        currentPlayer = player;
        bestRates.put(type, rate);
        player.removeResource(type, 1);
        givingStaged.merge(type, 1, Integer::sum);
        setVisible(true);
        setManaged(true);
        refresh();
    }

    public void reset() {
        if (currentPlayer != null) {
            givingStaged.forEach((t, n) -> currentPlayer.addResource(t, n));
        }
        clearState();
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void addReceive(ResourceType type) {
        receivingList.add(type);
        refresh();
    }

    private void removeReceive(int index) {
        if (index >= 0 && index < receivingList.size()) {
            receivingList.remove(index);
            refresh();
        }
    }

    private void unstageOne(ResourceType type) {
        if (!givingStaged.containsKey(type) || currentPlayer == null) return;
        currentPlayer.addResource(type, 1);
        int remaining = givingStaged.merge(type, -1, Integer::sum);
        if (remaining <= 0) givingStaged.remove(type);
        if (givingStaged.isEmpty() && receivingList.isEmpty()) { clearState(); return; }
        refresh();
    }

    private void doBankTrade() {
        if (!canBankTrade()) return;
        ResourceType receiveType = receivingList.get(0);
        Map<ResourceType, Integer> givingCopy = new EnumMap<>(givingStaged);
        clearState();
        if (onBankTrade != null) onBankTrade.accept(givingCopy, receiveType);
    }

    private void doPropose() {
        if (!canPropose()) return;
        Map<ResourceType, Integer> givingCopy    = new EnumMap<>(givingStaged);
        Map<ResourceType, Integer> requestingMap = new EnumMap<>(ResourceType.class);
        for (ResourceType t : receivingList) requestingMap.merge(t, 1, Integer::sum);
        clearState(); // staged resources consumed — not returned
        if (onPropose != null) onPropose.accept(givingCopy, requestingMap);
    }

    private void cancelTrade() {
        reset();
        if (onCancel != null) onCancel.run();
    }

    private boolean canBankTrade() {
        if (givingStaged.size() != 1) return false;
        Map.Entry<ResourceType, Integer> e = givingStaged.entrySet().iterator().next();
        int rate = bestRates.getOrDefault(e.getKey(), 4);
        if (!e.getValue().equals(rate)) return false;
        if (receivingList.size() != 1) return false;
        return receivingList.get(0) != e.getKey();
    }

    private boolean canPropose() {
        return !givingStaged.isEmpty() && !receivingList.isEmpty();
    }

    private void refresh() {
        // Give cards
        giveCardRow.getChildren().clear();
        List<Map.Entry<ResourceType, Integer>> entries = new ArrayList<>(givingStaged.entrySet());
        for (Map.Entry<ResourceType, Integer> entry : entries) {
            for (int i = 0; i < entry.getValue(); i++) {
                VBox card = miniCard(entry.getKey());
                final ResourceType t = entry.getKey();
                card.setOnMouseClicked(e -> unstageOne(t));
                card.setCursor(Cursor.HAND);
                card.setStyle("-fx-effect: dropshadow(gaussian, rgba(255,80,80,0.4), 5, 0, 0, 0);");
                giveCardRow.getChildren().add(card);
            }
        }
        boolean giveEmpty = givingStaged.isEmpty();
        giveHint.setVisible(giveEmpty);
        giveHint.setManaged(giveEmpty);

        // Receive cards
        receiveCardRow.getChildren().clear();
        for (int i = 0; i < receivingList.size(); i++) {
            VBox card = miniCard(receivingList.get(i));
            final int idx = i;
            card.setOnMouseClicked(e -> removeReceive(idx));
            card.setCursor(Cursor.HAND);
            card.setStyle("-fx-effect: dropshadow(gaussian, rgba(80,200,80,0.4), 5, 0, 0, 0);");
            receiveCardRow.getChildren().add(card);
        }
        boolean recEmpty = receivingList.isEmpty();
        receiveHint.setVisible(recEmpty);
        receiveHint.setManaged(recEmpty);

        // Bank button label
        if (givingStaged.size() == 1) {
            ResourceType gt = givingStaged.keySet().iterator().next();
            int rate        = bestRates.getOrDefault(gt, 4);
            int staged      = givingStaged.get(gt);
            String rateStr  = rate == 4 ? "4:1" : "Port " + rate + ":1";
            bankBtn.setText("🏦 " + rateStr + " (" + staged + "/" + rate + ")");
        } else {
            bankBtn.setText("🏦 Bank");
        }

        bankBtn.setDisable(!canBankTrade());
        proposeBtn.setDisable(!canPropose());
    }

    private void clearState() {
        givingStaged.clear();
        receivingList.clear();
        bestRates.clear();
        currentPlayer = null;
        giveCardRow.getChildren().clear();
        receiveCardRow.getChildren().clear();
        giveHint.setVisible(true);
        giveHint.setManaged(true);
        receiveHint.setVisible(true);
        receiveHint.setManaged(true);
        bankBtn.setText("🏦 Bank");
        bankBtn.setDisable(true);
        proposeBtn.setDisable(true);
        setVisible(false);
        setManaged(false);
    }

    // ── Card builders ─────────────────────────────────────────────────────────

    /** Small card shown in give/receive staging areas. */
    private VBox miniCard(ResourceType type) {
        Rectangle rect = new Rectangle(34, 48);
        rect.setFill(resourceColor(type));
        rect.setArcWidth(6);
        rect.setArcHeight(6);
        rect.setStroke(Color.web("#ffffff", 0.35));
        rect.setStrokeWidth(1.2);
        Label lbl = new Label(abbrev(type));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 9));
        lbl.setTextFill(Color.WHITE);
        VBox card = new VBox(2, rect, lbl);
        card.setAlignment(Pos.CENTER);
        return card;
    }

    /** Slightly larger card for the top infinite pool row. */
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
