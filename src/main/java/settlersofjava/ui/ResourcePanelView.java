package settlersofjava.ui;

import javafx.collections.MapChangeListener;
import settlersofjava.player.Player;
import settlersofjava.resources.ResourceType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * PATTERN: Observer (via JavaFX property binding)
 * Displays a player's resource counts.
 * Labels are BOUND to the player's ObservableMap — they update automatically,
 * no manual refresh required.
 */
public class ResourcePanelView extends GridPane {

    public ResourcePanelView(Player player) {
        int row = 0;
        for (ResourceType type : ResourceType.values()) {
            Label nameLabel  = new Label(type.name() + ": ");
            Label countLabel = new Label();

            // OBSERVER binding: countLabel text auto-updates when resource map changes
            player.resourcesProperty().addListener((MapChangeListener<ResourceType, Integer>) change ->
                    countLabel.setText(String.valueOf(player.getResource(type)))
            );
            countLabel.setText(String.valueOf(player.getResource(type)));

            add(nameLabel,  0, row);
            add(countLabel, 1, row);
            row++;
        }
    }
}

