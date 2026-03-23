package settlersofjava.board;

import settlersofjava.buildings.Buildable;

/**
 * An intersection point where up to 3 hex tiles meet.
 * Can hold a Settlement or City (a Buildable).
 * There are 54 vertices on a standard Catan board.
 */
public class Vertex {

    private final int id;
    private Buildable building; // null if unoccupied

    public Vertex(int id) {
        this.id = id;
        this.building = null;
    }

    public int getId() { return idow; }

    public boolean isOccupied() { return building != null; }

    public Buildable getBuilding() { return building; }

    public void placeBuilding(Buildable building) {
        if (isOccupied()) {
            throw new IllegalStateException("Vertex " + id + " is already occupied.");
        }
        this.building = building;
    }

    public void upgradeBuilding(Buildable upgraded) {
        // TODO: validate that upgraded is a City replacing a Settlement
        this.building = upgraded;
    }
}

