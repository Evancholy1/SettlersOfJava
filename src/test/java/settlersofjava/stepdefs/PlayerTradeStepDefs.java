package settlersofjava.stepdefs;

import io.cucumber.java.en.*;
import settlersofjava.player.Player;
import settlersofjava.player.PlayerColor;
import settlersofjava.resources.ResourceType;
import settlersofjava.trade.PlayerTradeOffer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTradeStepDefs {

    private Player           alice;
    private Player           bob;
    private Player           carol;
    private PlayerTradeOffer offer;

    // ── Player setup ("starts with" keeps these distinct from assertion steps) ─

    @Given("Alice starts with {int} {word}")
    public void alice_starts_with(int amount, String resource) {
        if (alice == null) alice = new Player("Alice", PlayerColor.RED);
        alice.addResource(ResourceType.valueOf(resource), amount);
    }

    @Given("Alice starts with {int} {word} and {int} {word}")
    public void alice_starts_with_two(int a1, String r1, int a2, String r2) {
        if (alice == null) alice = new Player("Alice", PlayerColor.RED);
        alice.addResource(ResourceType.valueOf(r1), a1);
        alice.addResource(ResourceType.valueOf(r2), a2);
    }

    @Given("Bob starts with {int} {word}")
    public void bob_starts_with(int amount, String resource) {
        if (bob == null) bob = new Player("Bob", PlayerColor.BLUE);
        bob.addResource(ResourceType.valueOf(resource), amount);
    }

    @Given("Bob starts with {int} {word} and {int} {word}")
    public void bob_starts_with_two(int a1, String r1, int a2, String r2) {
        if (bob == null) bob = new Player("Bob", PlayerColor.BLUE);
        bob.addResource(ResourceType.valueOf(r1), a1);
        bob.addResource(ResourceType.valueOf(r2), a2);
    }

    @Given("Carol starts with {int} {word}")
    public void carol_starts_with(int amount, String resource) {
        if (carol == null) carol = new Player("Carol", PlayerColor.BLACK);
        carol.addResource(ResourceType.valueOf(resource), amount);
    }

    @Given("Carol starts with {int} {word} and {int} {word}")
    public void carol_starts_with_two(int a1, String r1, int a2, String r2) {
        if (carol == null) carol = new Player("Carol", PlayerColor.BLACK);
        carol.addResource(ResourceType.valueOf(r1), a1);
        carol.addResource(ResourceType.valueOf(r2), a2);
    }

    // ── Offer creation ────────────────────────────────────────────────────────

    @Given("Alice proposes to give {int} {word} and receive {int} {word}")
    public void alice_proposes_single(int giveAmt, String giveRes, int wantAmt, String wantRes) {
        if (alice == null) alice = new Player("Alice", PlayerColor.RED);
        offer = new PlayerTradeOffer(alice,
                Map.of(ResourceType.valueOf(giveRes), giveAmt),
                Map.of(ResourceType.valueOf(wantRes), wantAmt));
    }

    @When("Alice proposes to give {int} {word} and {int} {word} and receive {int} {word}")
    public void alice_proposes_multi_give(int g1, String r1, int g2, String r2, int wantAmt, String wantRes) {
        if (alice == null) alice = new Player("Alice", PlayerColor.RED);
        offer = new PlayerTradeOffer(alice,
                Map.of(ResourceType.valueOf(r1), g1, ResourceType.valueOf(r2), g2),
                Map.of(ResourceType.valueOf(wantRes), wantAmt));
    }

    @When("Alice proposes to give nothing and receive {int} {word}")
    public void alice_proposes_empty_give(int wantAmt, String wantRes) {
        if (alice == null) alice = new Player("Alice", PlayerColor.RED);
        offer = new PlayerTradeOffer(alice, Map.of(),
                Map.of(ResourceType.valueOf(wantRes), wantAmt));
    }

    @When("Alice proposes to give {int} {word} and receive nothing")
    public void alice_proposes_empty_receive(int giveAmt, String giveRes) {
        if (alice == null) alice = new Player("Alice", PlayerColor.RED);
        offer = new PlayerTradeOffer(alice,
                Map.of(ResourceType.valueOf(giveRes), giveAmt), Map.of());
    }

    // ── Offer validity ────────────────────────────────────────────────────────

    @Then("the offer is valid")
    public void the_offer_is_valid() {
        assertTrue(offer.isValidOffer(), "Expected offer to be valid");
    }

    @Then("the offer is invalid")
    public void the_offer_is_invalid() {
        assertFalse(offer.isValidOffer(), "Expected offer to be invalid");
    }

    // ── Respondent eligibility ────────────────────────────────────────────────

    @Then("Bob can afford to accept")
    public void bob_can_afford_to_accept() {
        assertTrue(offer.canAffordToAccept(bob));
    }

    @Then("Bob cannot afford to accept")
    public void bob_cannot_afford_to_accept() {
        assertFalse(offer.canAffordToAccept(bob));
    }

    @Then("Alice cannot afford to accept")
    public void alice_cannot_afford_to_accept() {
        assertFalse(offer.canAffordToAccept(alice));
    }

    // ── Responses ─────────────────────────────────────────────────────────────

    @When("Bob accepts the offer")
    public void bob_accepts() {
        offer.respond(bob, PlayerTradeOffer.Response.ACCEPTED);
    }

    @When("Bob declines the offer")
    public void bob_declines() {
        offer.respond(bob, PlayerTradeOffer.Response.DECLINED);
    }

    @When("Carol accepts the offer")
    public void carol_accepts() {
        offer.respond(carol, PlayerTradeOffer.Response.ACCEPTED);
    }

    // ── Acceptors list ────────────────────────────────────────────────────────

    @Then("the acceptors list contains Bob")
    public void acceptors_contains_bob() {
        assertTrue(offer.getAcceptors().contains(bob));
    }

    @Then("the acceptors list does not contain Bob")
    public void acceptors_does_not_contain_bob() {
        assertFalse(offer.getAcceptors().contains(bob));
    }

    @Then("the acceptors list contains Carol")
    public void acceptors_contains_carol() {
        assertTrue(offer.getAcceptors().contains(carol));
    }

    // ── Execution ─────────────────────────────────────────────────────────────

    @When("Alice executes the trade with Bob")
    public void alice_executes_with_bob() {
        offer.execute(bob);
    }

    @Then("Alice has {int} {word}")
    public void alice_has(int expected, String resource) {
        assertEquals(expected, alice.getResource(ResourceType.valueOf(resource)));
    }

    @Then("Bob has {int} {word}")
    public void bob_has(int expected, String resource) {
        assertEquals(expected, bob.getResource(ResourceType.valueOf(resource)));
    }

    @Then("Carol has {int} {word}")
    public void carol_has(int expected, String resource) {
        assertEquals(expected, carol.getResource(ResourceType.valueOf(resource)));
    }

    @Then("executing the trade with Bob raises an error")
    public void executing_with_bob_raises_error() {
        assertThrows(IllegalStateException.class, () -> offer.execute(bob));
    }
}
