package settlersofjava.player;

import javafx.beans.property.SimpleBooleanProperty;
import settlersofjava.resources.ResourceBundle;
import settlersofjava.resources.ResourceType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import java.util.Map;
import settlersofjava.cards.DevelopmentCard;
/**
 * PATTERN: Observer (via JavaFX Properties)
 * A player's state is exposed via JavaFX observable properties so that
 * UI labels automatically update when resources or VPs change — no manual refresh needed.
 *
 * PATTERN: Dependency Injection — Player is constructed externally and injected
 * into GameController via PlayerList.
 */
public class Player {

    private final String name;
    private final PlayerColor color;

    // OBSERVER: UI binds to these changes update automatically
    private final ObservableMap<ResourceType, Integer> resources;
    private final IntegerProperty victoryPoints;
    private final IntegerProperty armySize;
    private final javafx.collections.ObservableList<DevelopmentCard> devCards;
    private final javafx.beans.property.BooleanProperty hasLargestArmy;

    public Player(String name, PlayerColor color) {
        this.name = name;
        this.color = color;
        this.resources = FXCollections.observableHashMap();
        this.victoryPoints = new SimpleIntegerProperty(0);
        this.armySize = new SimpleIntegerProperty(0);
        this.devCards = FXCollections.observableArrayList();
        this.hasLargestArmy = new SimpleBooleanProperty(false);

        // Initialize all resource counts to 0
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, 0);
        }
    }

    // ── Resource operations ───────────────────────────────────────────────────

    public int getResource(ResourceType type) {
        return resources.getOrDefault(type, 0);
    }

    public void addResource(ResourceType type, int amount) {
        resources.merge(type, amount, Integer::sum);
    }

    public void removeResource(ResourceType type, int amount) {
        int current = getResource(type);
        if (current < amount) {
            throw new IllegalStateException(name + " does not have " + amount + " " + type);
        }
        resources.put(type, current - amount);
    }

    public boolean canAfford(ResourceBundle cost) {
        for (Map.Entry<ResourceType, Integer> entry : cost.toMap().entrySet()) {
            if (getResource(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    // ── Observable property accessors (for JavaFX binding) ───────────────────

    public ObservableMap<ResourceType, Integer> resourcesProperty() { return resources; }

    public IntegerProperty victoryPointsProperty() { return victoryPoints; }
    public int getVictoryPoints()                  { return victoryPoints.get(); }
    public void addVictoryPoints(int delta) {
        victoryPoints.set(victoryPoints.get() + delta);
    }

    public IntegerProperty armySizeProperty()      { return armySize; }
    public int getArmySize()                       { return armySize.get(); }
    public void incrementArmySize()                { armySize.set(armySize.get() + 1); }

    public javafx.beans.property.BooleanProperty hasLargestArmyProperty() { return hasLargestArmy; }
    public boolean hasLargestArmy() { return hasLargestArmy.get(); }
    public void setHasLargestArmy(boolean value) { hasLargestArmy.set(value); }

    // ── Basic getters ─────────────────────────────────────────────────────────

    public String getName()       { return name; }
    public PlayerColor getColor() { return color; }

    @Override
    public String toString() { return name + " (" + color + ")"; }

    // ── Development Card Management  ───────────────────────────────────────────

    public javafx.collections.ObservableList<DevelopmentCard> getDevCards() { return devCards; }

    public void addDevCard(DevelopmentCard card) { devCards.add(card); }

    public void removeDevCard(DevelopmentCard card) { devCards.remove(card); }

    public void unlockDevCards() {
        for (DevelopmentCard card : devCards) card.unlock();
    }

    public int getTotalVictoryPoints() {
        int total = getVictoryPoints(); // Public VPs from Settlements and Cities

        // Add hidden Victory Point cards
        for (settlersofjava.cards.DevelopmentCard card : getDevCards()) {
            if (card instanceof settlersofjava.cards.VictoryPointCard) {
                total++;
            }
        }

        // Add +2 if they hold the Largest Army
        if (hasLargestArmy()) {
            total += 2;
        }

        return total;
    }
}

