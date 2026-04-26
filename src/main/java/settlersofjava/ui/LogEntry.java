package settlersofjava.ui;

import javafx.scene.paint.Color;
import java.util.List;

/**
 * Immutable payload for LOG_MESSAGE events.
 * Each entry is a sequence of (text, color) segments rendered as a single line.
 */
public record LogEntry(List<Segment> segments) {

    public record Segment(String text, Color color) {}

    public static LogEntry plain(String text, Color color) {
        return new LogEntry(List.of(new Segment(text, color)));
    }
}
