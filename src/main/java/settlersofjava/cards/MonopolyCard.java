package settlersofjava.cards;

import settlersofjava.player.Player;

public class MonopolyCard extends DevelopmentCard {

    @Override
    protected void applyEffect(Player player) {
        // Handled directly by GameController
    }

    @Override
    public String getCardName() { return "Monopoly"; }
}
