package settlersofjava.buildings;

import settlersofjava.player.Player;
import settlersofjava.resources.ResourceBundle;
import settlersofjava.resources.ResourceType;
import java.util.Map;

/**
 * A settlement: costs 1W + 1B + 1Wo + 1G, worth 1 VP.
 * Produces 1 resource per adjacent activated tile.
 * Can be upgraded to a City.
 */
public class Settlement extends Buildable {

    public static final ResourceBundle COST = new ResourceBundle(Map.of(
        ResourceType.WOOD,  1,
        ResourceType.BRICK, 1,
        ResourceType.WOOL,  1,
        ResourceType.GRAIN, 1
    ));

    public Settlement(Player owner) {
        super(owner);
    }

    @Override
    public ResourceBundle getCost() { return COST; }

    @Override
    public int getVictoryPoints() { return 1; }

    @Override
    public String getDisplayName() { return "Settlement"; }
}

