package settlersofjava.cards;

import settlersofjava.player.Player;

/**
 * Grants 1 VP when revealed. Kept secret until the player wins.
 */
public class VictoryPointCard extends DevelopmentCard {

    @Override
    protected void applyEffect(Player player) {
        player.addVictoryPoints(1);
    }

    @Override
    public String getCardName() { return "Victory Point"; }
}

