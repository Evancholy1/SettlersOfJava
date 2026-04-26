package settlersofjava.board;

import settlersofjava.player.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Computes the length of a player's longest connected road network using
 * DFS with backtracking on the board graph.
 *
 * Rules:
 *  - Roads must be continuously connected (share a vertex).
 *  - Each edge may only be used once per path (edges visited, not vertices).
 *  - An opponent's settlement or city at a vertex breaks the chain through that vertex.
 *  - Loops count: a 5-edge loop has length 5.
 *
 * Extracted into its own class so it can be tested independently of GameController.
 */
public class LongestRoadCalculator {

    private LongestRoadCalculator() {}

    /**
     * Returns the length of the longest road for {@code player} on the given board graph.
     *
     * @param player   the player whose roads to measure
     * @param vertices all board vertices (used as DFS starting points)
     * @param edges    all board edges (used to find adjacency)
     */
    public static int compute(Player player, List<Vertex> vertices, List<Edge> edges) {
        Set<Edge> visited = new HashSet<>();
        int max = 0;
        for (Vertex v : vertices) {
            visited.clear();
            max = Math.max(max, dfs(v, player, edges, visited));
        }
        return max;
    }

    /**
     * DFS from {@code v}: returns the longest path reachable from {@code v}
     * through unvisited roads owned by {@code player}.
     */
    private static int dfs(Vertex v, Player player, List<Edge> edges, Set<Edge> visited) {
        int best = 0;
        for (Edge e : edges) {
            if (!e.connectsVertex(v)) continue;
            if (visited.contains(e)) continue;
            if (!e.hasRoad() || e.getRoad().getOwner() != player) continue;

            Vertex next = e.getVertexA().equals(v) ? e.getVertexB() : e.getVertexA();

            // Opponent building at the next vertex breaks the chain
            if (next.isOccupied() && next.getBuilding().getOwner() != player) continue;

            visited.add(e);
            best = Math.max(best, 1 + dfs(next, player, edges, visited));
            visited.remove(e);
        }
        return best;
    }
}
