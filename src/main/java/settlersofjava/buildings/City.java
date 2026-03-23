package settlersofjava.buildings;

import settlersofjava.player.Player;
import settlersofjava.resources.ResourceBundle;
import settlersofjava.resources.ResourceType;
import java.util.Map;

/**
 * A city: costs 3O + 2G, worth 2 VP.
 * Produces 2 resources per adjacent activated tile.
 * Upgrades an existing Settlement — cannot be placed directly.
 */
public class City extends Buildable {

    public static final ResourceBundle COST = new ResourceBundle(Map.of(
        ResourceType.ORE,   3,
        ResourceType.GRAIN, 2
    ));

    public City(Player owner) {
        super(owner);
    }

    @Override
    public ResourceBundle getCost() { return COST; }

    @Override
    public int getVictoryPoints() { return 2; }

    @Override
    public String getDisplayName() { return "City"; }
}

