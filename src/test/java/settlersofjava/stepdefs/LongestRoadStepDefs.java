package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.board.Edge;
import settlersofjava.board.LongestRoadCalculator;
import settlersofjava.board.Vertex;
import settlersofjava.buildings.Road;
import settlersofjava.buildings.Settlement;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerColor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LongestRoadStepDefs {

    private Player alice;
    private Player bob;

    private List<Vertex> vertices;
    private List<Edge>   edges;

    // Mirrored tracker — mirrors GameController logic
    private Player longestRoadOwner = null;
    private int    longestRoadLength = 4;

    private void simulateCheckLongestRoad(Player p) {
        int length = LongestRoadCalculator.compute(p, vertices, edges);
        if (length >= 5 && length > longestRoadLength) {
            longestRoadLength = length;
            if (longestRoadOwner != p) {
                if (longestRoadOwner != null) longestRoadOwner.setHasLongestRoad(false);
                longestRoadOwner = p;
                p.setHasLongestRoad(true);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Creates n+1 vertices and n edges in a linear chain owned by player. */
    private void buildLinearChain(int n, Player owner) {
        vertices = new ArrayList<>();
        edges    = new ArrayList<>();
        for (int i = 0; i <= n; i++) vertices.add(new Vertex(i));
        for (int i = 0; i < n; i++) {
            Edge e = new Edge(i, vertices.get(i), vertices.get(i + 1));
            e.placeRoad(new Road(owner));
            edges.add(e);
        }
    }

    // ── Step definitions ──────────────────────────────────────────────────────

    @Given("Alice has a linear road chain of {int} edges")
    public void alice_linear_chain(int n) {
        alice = new Player("Alice", PlayerColor.RED);
        buildLinearChain(n, alice);
    }

    @Given("Alice has {int} roads forming a closed loop")
    public void alice_closed_loop(int n) {
        alice = new Player("Alice", PlayerColor.RED);
        vertices = new ArrayList<>();
        edges    = new ArrayList<>();
        for (int i = 0; i < n; i++) vertices.add(new Vertex(i));
        for (int i = 0; i < n; i++) {
            Edge e = new Edge(i, vertices.get(i), vertices.get((i + 1) % n));
            e.placeRoad(new Road(alice));
            edges.add(e);
        }
    }

    @Given("Alice has roads forming a fork: a stem of {int} then two branches of {int} and {int}")
    public void alice_forked_road(int stem, int branch1, int branch2) {
        alice = new Player("Alice", PlayerColor.RED);
        vertices = new ArrayList<>();
        edges    = new ArrayList<>();

        // Build stem: v0-v1-...-v_stem
        for (int i = 0; i <= stem; i++) vertices.add(new Vertex(i));
        for (int i = 0; i < stem; i++) {
            Edge e = new Edge(i, vertices.get(i), vertices.get(i + 1));
            e.placeRoad(new Road(alice));
            edges.add(e);
        }

        // Fork vertex is at index `stem`
        Vertex fork = vertices.get(stem);
        int nextId  = vertices.size();

        // Branch 1: fork → new vertices
        Vertex prev = fork;
        for (int i = 0; i < branch1; i++) {
            Vertex v = new Vertex(nextId++);
            vertices.add(v);
            Edge e = new Edge(edges.size(), prev, v);
            e.placeRoad(new Road(alice));
            edges.add(e);
            prev = v;
        }

        // Branch 2: fork → new vertices
        prev = fork;
        for (int i = 0; i < branch2; i++) {
            Vertex v = new Vertex(nextId++);
            vertices.add(v);
            Edge e = new Edge(edges.size(), prev, v);
            e.placeRoad(new Road(alice));
            edges.add(e);
            prev = v;
        }
    }

    @Given("Bob has a settlement at vertex {int} in the chain")
    public void bob_settlement_at(int idx) {
        bob = new Player("Bob", PlayerColor.BLUE);
        vertices.get(idx).placeBuilding(new Settlement(bob));
    }

    @Given("Alice holds the Longest Road with a chain of {int}")
    public void alice_holds_longest_road(int n) {
        alice = new Player("Alice", PlayerColor.RED);
        buildLinearChain(n, alice);
        simulateCheckLongestRoad(alice);
    }

    @Given("Bob builds a chain of {int} roads")
    public void bob_builds_chain(int n) {
        bob = new Player("Bob", PlayerColor.BLUE);
        // Bob gets a fresh graph; append to shared vertex/edge lists
        int startId = vertices.size();
        for (int i = 0; i <= n; i++) vertices.add(new Vertex(startId + i));
        int startEdgeId = edges.size();
        for (int i = 0; i < n; i++) {
            Edge e = new Edge(startEdgeId + i,
                    vertices.get(startId + i),
                    vertices.get(startId + i + 1));
            e.placeRoad(new Road(bob));
            edges.add(e);
        }
        simulateCheckLongestRoad(bob);
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    @Then("the longest road length for Alice is {int}")
    public void longest_road_length_is(int expected) {
        assertEquals(expected, LongestRoadCalculator.compute(alice, vertices, edges));
    }

    @Then("Alice holds the Longest Road")
    public void alice_holds() {
        simulateCheckLongestRoad(alice);
        assertTrue(alice.hasLongestRoad());
    }

    @Then("Alice does not hold the Longest Road")
    public void alice_does_not_hold() {
        assertFalse(alice.hasLongestRoad());
    }

    @Then("Bob holds the Longest Road")
    public void bob_holds() {
        assertTrue(bob.hasLongestRoad());
    }

    @Then("Alice has {int} total victory points from Longest Road")
    public void alice_vp_from_longest_road(int expected) {
        simulateCheckLongestRoad(alice);
        assertEquals(expected, alice.getTotalVictoryPoints());
    }
}
