package settlersofjava.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import settlersofjava.events.EventBus;
import settlersofjava.events.GameEvent;
import settlersofjava.events.GameEventListener;

/**
 * PATTERN: Observer — registers with EventBus and reacts to LOG_MESSAGE events.
 * Displays a scrollable, color-coded history of game events in the top-right panel.
 */
public class GameLogPanel extends VBox implements GameEventListener {

    private final VBox       entryBox;
    private final ScrollPane scroll;
    private static final int MAX_ENTRIES = 60;

    public GameLogPanel() {
        setStyle("-fx-background-color: #F0EAD6;");
        setPrefWidth(220);
        setMaxHeight(Double.MAX_VALUE);

        // ── Header ─────────────────────────────────────────────────────────────
        Label title = new Label("Game Log");
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setTextFill(Color.web("#3E2C1A"));
        title.setMaxWidth(Double.MAX_VALUE);
        title.setPadding(new Insets(8, 10, 7, 10));
        title.setStyle("-fx-background-color: #E8DFC7;"
                + "-fx-border-color: #C4B89A; -fx-border-width: 0 0 1 0;");

        // ── Entry list ─────────────────────────────────────────────────────────
        entryBox = new VBox(1);
        entryBox.setPadding(new Insets(4, 2, 4, 2));
        entryBox.setStyle("-fx-background-color: #F0EAD6;");

        scroll = new ScrollPane(entryBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: #F0EAD6; -fx-background-color: #F0EAD6;"
                + "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(title, scroll);
        EventBus.getInstance().register(this);
    }

    // ── Observer callback ─────────────────────────────────────────────────────

    @Override
    public void onEvent(GameEvent event, Object payload) {
        if (event == GameEvent.LOG_MESSAGE && payload instanceof LogEntry entry)
            addEntry(entry);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void addEntry(LogEntry entry) {
        TextFlow flow = new TextFlow();
        flow.setPadding(new Insets(3, 6, 3, 6));
        flow.setMaxWidth(Double.MAX_VALUE);
        flow.setStyle("-fx-background-color: transparent;");

        for (LogEntry.Segment seg : entry.segments()) {
            Text t = new Text(seg.text());
            t.setFont(Font.font("System", 11));
            t.setFill(seg.color());
            flow.getChildren().add(t);
        }

        entryBox.getChildren().add(flow);

        if (entryBox.getChildren().size() > MAX_ENTRIES)
            entryBox.getChildren().remove(0);

        scroll.setVvalue(1.0);
    }
}
