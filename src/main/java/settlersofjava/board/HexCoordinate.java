package settlersofjava.board;

import java.util.Objects;

/**
 * Axial (q, r) coordinate for hex grid math.
 * s = -q - r (cube coordinate, derived).
 * See: https://www.redblobgames.com/grids/hexagons/
 */
public class HexCoordinate {

    private final int q;
    private final int r;

    public HexCoordinate(int q, int r) {
        this.q = q;
        this.r = r;
    }

    private static final int[][] DIRECTIONS = {
            {1, 0}, //  0: EAST
            {1, -1}, // 1: NORTH EAST
            {0, -1}, // 2: NORTH WEST
            {-1, 0}, // 3: WEST
            {-1, 1}, // 4: SOUTH WEST
            {0, 1}, //  5: SOUTH EAST
    };

    public int getQ() { return q; }
    public int getR() { return r; }
    public int getS() { return -q - r; }

    public HexCoordinate neighbor(int direction) {
        if(direction < 0 || direction > 5) {
            throw new IllegalArgumentException("Invalid direction: " + direction + ". Direction must be between 0 and 5");
        }
        return new HexCoordinate(q + DIRECTIONS[direction][0], r + DIRECTIONS[direction][1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HexCoordinate h)) return false;
        return q == h.q && r == h.r;
    }

    @Override
    public int hashCode() { return Objects.hash(q, r); }

    @Override
    public String toString() { return "(" + q + ", " + r + ")"; }
}

