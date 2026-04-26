package settlersofjava.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PATTERN: Factory
 * Builds the standard development card deck and shuffles it.
 * Standard deck: 14 Knights, 5 VPs, 2 Road Building, 2 Year of Plenty, 2 Monopoly = 25 total
 */
public class DevCardFactory {
    public List<DevelopmentCard> createShuffledDeck() {
        List<DevelopmentCard> deck = new ArrayList<>();
        for (int i = 0; i < 14; i++) deck.add(new KnightCard());
        for (int i = 0; i < 5;  i++) deck.add(new VictoryPointCard());
        for (int i = 0; i < 2;  i++) deck.add(new RoadBuildingCard());
        for (int i = 0; i < 2;  i++) deck.add(new MonopolyCard());
        for (int i = 0; i < 2;  i++) deck.add(new YearOfPlentyCard());
        Collections.shuffle(deck);
        return deck;
    }
}

