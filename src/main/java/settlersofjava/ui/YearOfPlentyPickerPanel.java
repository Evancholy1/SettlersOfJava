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
import settlersofjava.resources.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Inline panel for Year of Plenty — pick any 2 resources (repeats allowed).
 * Reuses poolCard / miniCard visual style from TradingPanel.
 */
public class YearOfPlentyPickerPanel extends HBox {

    private final List<ResourceType> picks = new ArrayList<>();
    private final HBox pickRow;
    private final Label pickHint;
    private final Button claimBtn;

    private BiConsumer<ResourceType, ResourceType> onClaim;

    public YearOfPlentyPickerPanel() {
        setVisible(false);
        setManaged(false);
        setStyle("-fx-background-color: #263238;");
        setPadding(new Insets(8, 14, 8, 14));
        setSpacing(14);
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Region.USE_PREF_SIZE);

        // ── Pool row ──────────────────────────────────────────────────────────
        HBox poolRow = new HBox(8);
        poolRow.setAlignment(Pos.CENTER_LEFT);
        for (ResourceType type : ResourceType.values()) {
            VBox card = poolCard(type);
            card.setCursor(Cursor.HAND);
            card.setOnMouseClicked(e -> addPick(type));
            poolRow.getChildren().add(card);
        }
        Label poolHdr = new Label("Pick →");
        poolHdr.setFont(Font.font("System", FontWeight.BOLD, 10));
        poolHdr.setTextFill(Color.web("#90A4AE"));
        HBox poolSection = new HBox(8, poolHdr, poolRow);
        poolSection.setAlignment(Pos.CENTER_LEFT);

        // ── Picked row ────────────────────────────────────────────────────────
        Label pickArrow = new Label("▼");
        pickArrow.setFont(Font.font("System", FontWeight.BOLD, 15));
        pickArrow.setTextFill(Color.SEAGREEN);
        pickRow = new HBox(4);
        pickRow.setAlignment(Pos.CENTER_LEFT);
        pickHint = new Label("pick 2 resources");
        pickHint.setFont(Font.font("System", 10));
        pickHint.setTextFill(Color.web("#546E7A"));
        HBox pickedSection = new HBox(6, pickArrow, pickRow, pickHint);
        pickedSection.setAlignment(Pos.CENTER_LEFT);

        VBox mainStack = new VBox(6, poolSection, pickedSection);
        mainStack.setAlignment(Pos.CENTER_LEFT);

        // ── Claim button ──────────────────────────────────────────────────────
        claimBtn = new Button("✔ Claim");
        claimBtn.setFont(Font.font("System", FontWeight.BOLD, 11));
        claimBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white;"
                + " -fx-background-radius: 8; -fx-pref-width: 90; -fx-pref-height: 40;");
        claimBtn.setDisable(true);
        claimBtn.setOnAction(e -> doClaim());

        getChildren().addAll(mainStack, claimBtn);
    }

    public void setOnClaim(BiConsumer<ResourceType, ResourceType> cb) { this.onClaim = cb; }

    public void show() {
        picks.clear();
        refreshPickRow();
        setVisible(true);
        setManaged(true);
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void addPick(ResourceType type) {
        if (picks.size() >= 2) return;
        picks.add(type);
        refreshPickRow();
    }

    private void removePick(int index) {
        if (index >= 0 && index < picks.size()) {
            picks.remove(index);
            refreshPickRow();
        }
    }

    private void doClaim() {
        if (picks.size() != 2) return;
        ResourceType a = picks.get(0), b = picks.get(1);
        hide();
        if (onClaim != null) onClaim.accept(a, b);
    }

    private void refreshPickRow() {
        pickRow.getChildren().clear();
        for (int i = 0; i < picks.size(); i++) {
            VBox card = miniCard(picks.get(i));
            final int idx = i;
            card.setCursor(Cursor.HAND);
            card.setOnMouseClicked(e -> removePick(idx));
            card.setStyle("-fx-effect: dropshadow(gaussian, rgba(80,200,80,0.4), 5, 0, 0, 0);");
            pickRow.getChildren().add(card);
        }
        boolean empty = picks.isEmpty();
        pickHint.setVisible(empty);
        pickHint.setManaged(empty);
        claimBtn.setDisable(picks.size() != 2);
    }

    // ── Card builders (mirrors TradingPanel) ──────────────────────────────────

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
