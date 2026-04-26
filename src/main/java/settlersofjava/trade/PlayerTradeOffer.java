package settlersofjava.trade;

import settlersofjava.player.Player;
import settlersofjava.resources.ResourceType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A player-to-player trade offer.
 *
 * Lifecycle:
 *   1. Proposer builds an offer (what they give, what they want).
 *   2. Each other player responds ACCEPTED or DECLINED.
 *   3. Proposer calls execute(acceptor) to complete the swap.
 *
 * Resources are not touched until execute() — no staging side-effects.
 */
public class PlayerTradeOffer {

    public enum Response { PENDING, ACCEPTED, DECLINED }

    private final Player                        proposer;
    private final Map<ResourceType, Integer>    offering;   // proposer gives
    private final Map<ResourceType, Integer>    requesting; // proposer wants
    private final Map<Player, Response>         responses = new LinkedHashMap<>();

    public PlayerTradeOffer(Player proposer,
                            Map<ResourceType, Integer> offering,
                            Map<ResourceType, Integer> requesting) {
        this.proposer   = proposer;
        this.offering   = Map.copyOf(offering);
        this.requesting = Map.copyOf(requesting);
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /** True when the proposer currently holds all offered resources and both sides are non-empty. */
    public boolean isValidOffer() {
        if (offering.isEmpty() || requesting.isEmpty()) return false;
        for (var e : offering.entrySet()) {
            if (e.getValue() <= 0 || proposer.getResource(e.getKey()) < e.getValue()) return false;
        }
        for (var e : requesting.entrySet()) {
            if (e.getValue() <= 0) return false;
        }
        return true;
    }

    /** True when the given player currently holds all resources the proposer is requesting. */
    public boolean canAffordToAccept(Player p) {
        if (p == proposer) return false;
        for (var e : requesting.entrySet()) {
            if (p.getResource(e.getKey()) < e.getValue()) return false;
        }
        return true;
    }

    // ── Responses ─────────────────────────────────────────────────────────────

    public void respond(Player p, Response response) {
        if (p == proposer) throw new IllegalArgumentException("Proposer cannot respond to their own offer");
        responses.put(p, response);
    }

    public Response getResponse(Player p) {
        return responses.getOrDefault(p, Response.PENDING);
    }

    public List<Player> getAcceptors() {
        return responses.entrySet().stream()
                .filter(e -> e.getValue() == Response.ACCEPTED)
                .map(Map.Entry::getKey)
                .toList();
    }

    // ── Execution ─────────────────────────────────────────────────────────────

    /**
     * Atomically swaps resources between proposer and acceptor.
     * Throws if the acceptor never responded ACCEPTED.
     */
    public void execute(Player acceptor) {
        if (getResponse(acceptor) != Response.ACCEPTED)
            throw new IllegalStateException(acceptor.getName() + " did not accept this offer");

        offering.forEach((type, amount) -> {
            proposer.removeResource(type, amount);
            acceptor.addResource(type, amount);
        });
        requesting.forEach((type, amount) -> {
            acceptor.removeResource(type, amount);
            proposer.addResource(type, amount);
        });
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Player                     getProposer()   { return proposer; }
    public Map<ResourceType, Integer> getOffering()   { return offering; }
    public Map<ResourceType, Integer> getRequesting() { return requesting; }
}
