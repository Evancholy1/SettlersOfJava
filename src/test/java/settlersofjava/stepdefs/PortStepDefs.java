package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.board.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PortStepDefs {

    private BoardState board;

    @Given("a board with ports is constructed")
    public void a_board_with_ports_is_constructed() {
        board = new BoardBuilder()
                .withShuffledTilesAndNumberTokens()
                .withVerticesAndEdges()
                .withPorts()
                .build();
    }

    // ── Port count ────────────────────────────────────────────────────────────

    @Then("there are exactly {int} port edges")
    public void there_are_exactly_n_port_edges(int expected) {
        assertEquals(expected, countPortEdges());
    }

    // ── Port type distribution ────────────────────────────────────────────────

    @Then("there are exactly {int} generic 3:1 ports")
    public void there_are_exactly_n_generic_ports(int expected) {
        assertEquals(expected, countPortEdgesOfType(PortType.GENERIC_3_1));
    }

    @Then("there is exactly {int} wood 2:1 port")
    public void there_is_exactly_n_wood_port(int expected) {
        assertEquals(expected, countPortEdgesOfType(PortType.WOOD_2_1));
    }

    @Then("there is exactly {int} brick 2:1 port")
    public void there_is_exactly_n_brick_port(int expected) {
        assertEquals(expected, countPortEdgesOfType(PortType.BRICK_2_1));
    }

    @Then("there is exactly {int} sheep 2:1 port")
    public void there_is_exactly_n_sheep_port(int expected) {
        assertEquals(expected, countPortEdgesOfType(PortType.WOOL_2_1));
    }

    @Then("there is exactly {int} wheat 2:1 port")
    public void there_is_exactly_n_wheat_port(int expected) {
        assertEquals(expected, countPortEdgesOfType(PortType.GRAIN_2_1));
    }

    @Then("there is exactly {int} ore 2:1 port")
    public void there_is_exactly_n_ore_port(int expected) {
        assertEquals(expected, countPortEdgesOfType(PortType.ORE_2_1));
    }

    // ── Port vertex validity ──────────────────────────────────────────────────

    @Then("every port vertex touches fewer than 3 tiles")
    public void every_port_vertex_is_coastal() {
        for (Vertex v : board.getVertices()) {
            if (v.hasPort()) {
                int tileCount = board.getTilesFor(v).size();
                assertTrue(tileCount < 3,
                    "Port vertex " + v.getId() + " touches " + tileCount + " tiles (expected < 3)");
            }
        }
    }

    @Then("every port vertex pair on an edge shares the same port type")
    public void every_port_edge_has_matching_types() {
        for (Edge edge : board.getEdges()) {
            PortType portA = edge.getVertexA().getPort();
            PortType portB = edge.getVertexB().getPort();
            // If BOTH vertices have ports they must be the same type.
            // One vertex having a port and the other not is normal for
            // edges adjacent to (but not defining) a port.
            if (portA != null && portB != null) {
                assertEquals(portA, portB,
                    "Edge " + edge.getId() + " has mismatched port types: " + portA + " vs " + portB);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private long countPortEdges() {
        return board.getEdges().stream()
                .filter(e -> {
                    PortType a = e.getVertexA().getPort();
                    PortType b = e.getVertexB().getPort();
                    return a != null && a == b;
                })
                .count();
    }

    private long countPortEdgesOfType(PortType type) {
        return board.getEdges().stream()
                .filter(e -> e.getVertexA().getPort() == type
                          && e.getVertexB().getPort() == type)
                .count();
    }
}
