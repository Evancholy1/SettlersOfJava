package settlersofjava.buildings;

import settlersofjava.player.Player;
import settlersofjava.resources.ResourceBundle;
import settlersofjava.resources.ResourceType;
import java.util.Map;

/**
 * A road: costs 1W + 1B, worth 0 VP.
 * Placed on an Edge. Contributes to Longest Road calculation.
 */
public class Road extends Buildable {

    public static final ResourceBundle COST = new ResourceBundle(Map.of(
        ResourceType.WOOD,  1,
        ResourceType.BRICK, 1
    ));

    public Road(Player owner) {
        super(owner);
    }

    @Override
    public ResourceBundle getCost() { return COST; }

    @Override
    public int getVictoryPoints() { return 0; }

    @Override
    public String getDisplayName() { return "Road"; }
}

