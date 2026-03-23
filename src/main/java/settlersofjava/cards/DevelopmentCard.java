package settlersofjava.cards;

import settlersofjava.player.Player;

/**
 * PATTERN: Polymorphism
 * Abstract base for all development cards.
 * Subclasses define their specific play() effect.
 */
public abstract class DevelopmentCard {

    private boolean played;

    protected DevelopmentCard() {
        this.played = false;
    }

    public boolean isPlayed() { return played; }

    /**
     * Executes this card's effect for the given player.
     * Sets played = true. Cannot be played twice.
     */
    public final void play(Player player) {
        if (played) {
            throw new IllegalStateException(getCardName() + " has already been played.");
        }
        applyEffect(player);
        played = true;
    }

    protected abstract void applyEffect(Player player);

    public abstract String getCardName();

    @Override
    public String toString() { return getCardName(); }
}

