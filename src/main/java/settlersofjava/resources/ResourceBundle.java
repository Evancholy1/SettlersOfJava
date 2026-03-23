package settlersofjava.resources;

import java.util.EnumMap;
import java.util.Map;

/**
 * An immutable snapshot of a set of resources (e.g., a building cost or trade offer).
 * Distinct from a Player's live resource inventory (which uses JavaFX ObservableMap).
 */
public class ResourceBundle {

    private final Map<ResourceType, Integer> amounts;

    public ResourceBundle(Map<ResourceType, Integer> amounts) {
        this.amounts = new EnumMap<>(amounts);
    }

    public int getAmount(ResourceType type) {
        return amounts.getOrDefault(type, 0);
    }

    public Map<ResourceType, Integer> toMap() {
        return Map.copyOf(amounts);
    }

    // Convenience factory
    public static ResourceBundle of(ResourceType type, int amount) {
        return new ResourceBundle(Map.of(type, amount));
    }
}

