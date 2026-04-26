package settlersofjava.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerColor;
import settlersofjava.trade.PlayerTradeOffer;

import java.util.List;
import java.util.function.Consumer;

/**
 * Shown to the proposer after all players have responded.
 * Lists players who accepted; proposer clicks one to execute the trade.
 */
public class PlayerTradeSelectPanel extends HBox {

    private final Label  headerLabel;
    private final HBox   acceptorRow;
    private Consumer<Player> onExecute;
    private Runnable         onCancel;

    public PlayerTradeSelectPanel() {
        setVisible(false);
        setManaged(false);
        setStyle("-fx-background-color: #455a64;");
        setPadding(new Insets(10, 16, 10, 16));
        setSpacing(18);
        setAlignment(Pos.CENTER_LEFT);

        headerLabel = new Label("Choose a trade partner:");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        headerLabel.setTextFill(Color.WHITE);

        acceptorRow = new HBox(10);
        acceptorRow.setAlignment(Pos.CENTER_LEFT);

        Button cancelBtn = new Button("✕ Cancel");
        cancelBtn.setFont(Font.font("System", FontWeight.BOLD, 13));
        cancelBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-pref-width: 90; -fx-pref-height: 40;");
        cancelBtn.setOnAction(e -> { if (onCancel != null) onCancel.run(); });

        getChildren().addAll(headerLabel, acceptorRow, cancelBtn);
        setMaxWidth(Region.USE_PREF_SIZE);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setOnExecute(Consumer<Player> cb) { this.onExecute = cb; }
    public void setOnCancel(Runnable cb)           { this.onCancel  = cb; }

    public void show(PlayerTradeOffer offer, List<Player> acceptors) {
        acceptorRow.getChildren().clear();

        if (acceptors.isEmpty()) {
            headerLabel.setText("No one accepted the offer.");
        } else {
            headerLabel.setText("Choose a trade partner:");
            for (Player p : acceptors) {
                Button btn = new Button(p.getName());
                btn.setFont(Font.font("System", FontWeight.BOLD, 13));
                btn.setStyle("-fx-background-color: " + toHex(playerColor(p.getColor()))
                        + "; -fx-text-fill: white; -fx-background-radius: 8;"
                        + " -fx-pref-height: 40; -fx-padding: 0 16 0 16;");
                btn.setOnAction(e -> { if (onExecute != null) onExecute.accept(p); });
                acceptorRow.getChildren().add(btn);
            }
        }

        setVisible(true);
        setManaged(true);
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Color playerColor(PlayerColor c) {
        return switch (c) {
            case RED    -> Color.web("#C62828");
            case BLUE   -> Color.web("#1565C0");
            case BLACK  -> Color.web("#424242");
            case ORANGE -> Color.web("#E65100");
        };
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }
}
