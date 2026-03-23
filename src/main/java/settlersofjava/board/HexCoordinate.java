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

    public int getQ() { return q; }
    public int getR() { return r; }
    public int getS() { return -q - r; }

    public HexCoordinate neighbor(int direction) {
        // TODO: return the neighboring coordinate in one of 6 directions
        throw new UnsupportedOperationException("Not yet implemented");
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

