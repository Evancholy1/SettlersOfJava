package settlersofjava.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import settlersofjava.player.Player;
import settlersofjava.resources.ResourceType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Inline offer-builder panel for player-to-player trading.
 * No resources are moved until the proposer confirms via GameController.execute().
 * The panel tracks intended amounts; validation checks live player inventory.
 */
public class PlayerTradeOfferPanel extends HBox {

    private Player currentPlayer;

    private static final ResourceType[] TYPES = ResourceType.values();
    private final int[]    offeringCounts   = new int[TYPES.length];
    private final int[]    requestingCounts = new int[TYPES.length];
    private final Label[]  offerLabels      = new Label[TYPES.length];
    private final Label[]  reqLabels        = new Label[TYPES.length];
    private final Button[] offerPlus        = new Button[TYPES.length];
    private final Button[] offerMinus       = new Button[TYPES.length];
    private final Button[] reqPlus          = new Button[TYPES.length];
    private final Button[] reqMinus         = new Button[TYPES.length];
    private final Button   proposeBtn;

    private BiConsumer<Map<ResourceType, Integer>, Map<ResourceType, Integer>> onPropose;
    private Runnable onCancel;

    public PlayerTradeOfferPanel() {
        setVisible(false);
        setManaged(false);
        setStyle("-fx-background-color: #263238;");
        setPadding(new Insets(10, 16, 10, 16));
        setSpacing(18);
        setAlignment(Pos.CENTER_LEFT);

        // ── Icon ──────────────────────────────────────────────────────────────
        VBox iconBox = new VBox(4);
        iconBox.setAlignment(Pos.CENTER);
        Label icon = new Label("🤝");
        icon.setFont(Font.font(22));
        Label iconLbl = new Label("Player Trade");
        iconLbl.setFont(Font.font("System", FontWeight.BOLD, 9));
        iconLbl.setTextFill(Color.LIGHTGRAY);
        iconBox.getChildren().addAll(icon, iconLbl);

        // ── Offering section ──────────────────────────────────────────────────
        VBox offerSection = new VBox(4);
        offerSection.setAlignment(Pos.CENTER_LEFT);
        Label offerTitle = new Label("▲ Offering");
        offerTitle.setFont(Font.font("System", FontWeight.BOLD, 11));
        offerTitle.setTextFill(Color.FIREBRICK);
        HBox offerRow = new HBox(10);
        offerRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < TYPES.length; i++) {
            offerRow.getChildren().add(buildColumn(i, true));
        }
        offerSection.getChildren().addAll(offerTitle, offerRow);

        Label arrowLbl = new Label("→");
        arrowLbl.setFont(Font.font("System", FontWeight.BOLD, 20));
        arrowLbl.setTextFill(Color.LIGHTGRAY);

        // ── Requesting section ────────────────────────────────────────────────
        VBox reqSection = new VBox(4);
        reqSection.setAlignment(Pos.CENTER_LEFT);
        Label reqTitle = new Label("▼ Requesting");
        reqTitle.setFont(Font.font("System", FontWeight.BOLD, 11));
        reqTitle.setTextFill(Color.SEAGREEN);
        HBox reqRow = new HBox(10);
        reqRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < TYPES.length; i++) {
            reqRow.getChildren().add(buildColumn(i, false));
        }
        reqSection.getChildren().addAll(reqTitle, reqRow);

        // ── Buttons ───────────────────────────────────────────────────────────
        proposeBtn = new Button("Propose");
        proposeBtn.setFont(Font.font("System", FontWeight.BOLD, 12));
        proposeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-pref-width: 80; -fx-pref-height: 40;");
        proposeBtn.setDisable(true);
        proposeBtn.setOnAction(e -> propose());

        Button cancelBtn = new Button("✕");
        cancelBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        cancelBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-pref-width: 40; -fx-pref-height: 40;");
        cancelBtn.setOnAction(e -> { if (onCancel != null) onCancel.run(); });

        VBox btnBox = new VBox(8, proposeBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(iconBox, offerSection, arrowLbl, reqSection, spacer, btnBox);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setOnPropose(BiConsumer<Map<ResourceType, Integer>, Map<ResourceType, Integer>> cb) {
        this.onPropose = cb;
    }

    public void setOnCancel(Runnable cb) { this.onCancel = cb; }

    public void show(Player player) {
        this.currentPlayer = player;
        for (int i = 0; i < TYPES.length; i++) {
            offeringCounts[i]   = 0;
            requestingCounts[i] = 0;
        }
        refreshAll();
        setVisible(true);
        setManaged(true);
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
        currentPlayer = null;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private VBox buildColumn(int idx, boolean isOffering) {
        Rectangle rect = new Rectangle(30, 40);
        rect.setFill(resourceColor(TYPES[idx]));
        rect.setArcWidth(5);
        rect.setArcHeight(5);
        rect.setStroke(Color.web("#ffffff", 0.4));
        rect.setStrokeWidth(1);

        Label nameLbl = new Label(abbrev(TYPES[idx]));
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 9));
        nameLbl.setTextFill(Color.WHITE);

        Label countLbl = new Label("0");
        countLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        countLbl.setTextFill(Color.WHITE);
        countLbl.setMinWidth(18);
        countLbl.setAlignment(Pos.CENTER);

        Button plus  = spinBtn("+");
        Button minus = spinBtn("−");
        minus.setDisable(true);

        if (isOffering) {
            offerLabels[idx] = countLbl;
            offerPlus[idx]   = plus;
            offerMinus[idx]  = minus;
            plus.setOnAction(e  -> changeOffering(idx, 1));
            minus.setOnAction(e -> changeOffering(idx, -1));
        } else {
            reqLabels[idx] = countLbl;
            reqPlus[idx]   = plus;
            reqMinus[idx]  = minus;
            plus.setOnAction(e  -> changeRequesting(idx, 1));
            minus.setOnAction(e -> changeRequesting(idx, -1));
        }

        HBox spinRow = new HBox(2, minus, countLbl, plus);
        spinRow.setAlignment(Pos.CENTER);

        VBox col = new VBox(2, rect, nameLbl, spinRow);
        col.setAlignment(Pos.CENTER);
        return col;
    }

    private Button spinBtn(String text) {
        Button b = new Button(text);
        b.setFont(Font.font("System", FontWeight.BOLD, 10));
        b.setStyle("-fx-background-color: #546e7a; -fx-text-fill: white;"
                + "-fx-background-radius: 4; -fx-pref-width: 22; -fx-pref-height: 20; -fx-padding: 0;");
        return b;
    }

    private void changeOffering(int idx, int delta) {
        int next = offeringCounts[idx] + delta;
        if (next < 0) return;
        if (delta > 0 && (currentPlayer == null || currentPlayer.getResource(TYPES[idx]) <= offeringCounts[idx])) return;
        offeringCounts[idx] = next;
        refreshAll();
    }

    private void changeRequesting(int idx, int delta) {
        int next = requestingCounts[idx] + delta;
        if (next < 0) return;
        requestingCounts[idx] = next;
        refreshAll();
    }

    private void refreshAll() {
        boolean hasOffer = false, hasReq = false;
        for (int i = 0; i < TYPES.length; i++) {
            if (offerLabels[i]  != null) offerLabels[i].setText(String.valueOf(offeringCounts[i]));
            if (reqLabels[i]    != null) reqLabels[i].setText(String.valueOf(requestingCounts[i]));
            if (offerMinus[i]   != null) offerMinus[i].setDisable(offeringCounts[i] == 0);
            if (reqMinus[i]     != null) reqMinus[i].setDisable(requestingCounts[i] == 0);
            if (offerPlus[i]    != null) offerPlus[i].setDisable(
                    currentPlayer == null || currentPlayer.getResource(TYPES[i]) <= offeringCounts[i]);
            if (offeringCounts[i]   > 0) hasOffer = true;
            if (requestingCounts[i] > 0) hasReq   = true;
        }
        proposeBtn.setDisable(!hasOffer || !hasReq);
    }

    private void propose() {
        if (onPropose == null) return;
        Map<ResourceType, Integer> off = new EnumMap<>(ResourceType.class);
        Map<ResourceType, Integer> req = new EnumMap<>(ResourceType.class);
        for (int i = 0; i < TYPES.length; i++) {
            if (offeringCounts[i]   > 0) off.put(TYPES[i], offeringCounts[i]);
            if (requestingCounts[i] > 0) req.put(TYPES[i], requestingCounts[i]);
        }
        onPropose.accept(off, req);
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
