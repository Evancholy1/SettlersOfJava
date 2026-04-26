package settlersofjava.cards;

import settlersofjava.player.Player;
import settlersofjava.resources.ResourceBundle;
import settlersofjava.resources.ResourceType;
import java.util.Map;

/**
 * PATTERN: Polymorphism
 * Abstract base for all development cards.
 * Subclasses define their specific play() effect.
 */
public abstract class DevelopmentCard {

    public static final ResourceBundle COST = new ResourceBundle(Map.of(
            ResourceType.SHEEP, 1,
            ResourceType.WHEAT, 1,
            ResourceType.ORE, 1
    ));

    private boolean played;
    private boolean lockedThisTurn;

    protected DevelopmentCard() {
        this.played = false;
        this.lockedThisTurn = true; // Locked on the turn it is bought
    }

    public boolean isPlayed() { return played; }
    public boolean isLocked() { return lockedThisTurn; }
    public void unlock() { this.lockedThisTurn = false; }

    public final void play(Player player) {
        if (played) throw new IllegalStateException(getCardName() + " has already been played.");
        if (lockedThisTurn) throw new IllegalStateException("Card was bought this turn.");
        applyEffect(player);
        played = true;
    }

    protected abstract void applyEffect(Player player);

    public abstract String getCardName();

    @Override
    public String toString() { return getCardName(); }
}

