Feature: Player-to-player trading

  # ── Offer validity ────────────────────────────────────────────────────────

  Scenario: Proposer with enough resources creates a valid offer
    Given Alice starts with 2 WOOD
    When Alice proposes to give 2 WOOD and receive 1 BRICK
    Then the offer is valid

  Scenario: Proposer who cannot afford the offer has an invalid offer
    Given Alice starts with 1 WOOD
    When Alice proposes to give 2 WOOD and receive 1 BRICK
    Then the offer is invalid

  Scenario: Offer with an empty giving side is invalid
    Given Alice starts with 2 WOOD
    When Alice proposes to give nothing and receive 1 BRICK
    Then the offer is invalid

  Scenario: Offer with an empty receiving side is invalid
    Given Alice starts with 2 WOOD
    When Alice proposes to give 2 WOOD and receive nothing
    Then the offer is invalid

  # ── Multi-resource offer validity ─────────────────────────────────────────

  Scenario: Proposer with enough of every offered resource has a valid multi-resource offer
    Given Alice starts with 2 WOOD and 1 ORE
    When Alice proposes to give 2 WOOD and 1 ORE and receive 3 BRICK
    Then the offer is valid

  Scenario: Proposer missing one resource in a multi-resource offer has an invalid offer
    Given Alice starts with 2 WOOD and 0 ORE
    When Alice proposes to give 2 WOOD and 1 ORE and receive 3 BRICK
    Then the offer is invalid

  # ── Respondent eligibility ────────────────────────────────────────────────

  Scenario: Player with enough of the requested resource can accept
    Given Alice proposes to give 1 SHEEP and receive 1 ORE
    And Bob starts with 1 ORE
    Then Bob can afford to accept

  Scenario: Player without enough of the requested resource cannot accept
    Given Alice proposes to give 1 SHEEP and receive 1 ORE
    And Bob starts with 0 ORE
    Then Bob cannot afford to accept

  Scenario: Proposer cannot accept their own offer
    Given Alice starts with 1 SHEEP
    When Alice proposes to give 1 SHEEP and receive 1 ORE
    Then Alice cannot afford to accept

  # ── Response recording ────────────────────────────────────────────────────

  Scenario: Accepting player appears in the acceptors list
    Given Alice proposes to give 1 SHEEP and receive 1 ORE
    And Bob starts with 1 ORE
    When Bob accepts the offer
    Then the acceptors list contains Bob

  Scenario: Declining player does not appear in the acceptors list
    Given Alice proposes to give 1 SHEEP and receive 1 ORE
    And Bob starts with 1 ORE
    When Bob declines the offer
    Then the acceptors list does not contain Bob

  Scenario: Multiple players can accept the same offer
    Given Alice proposes to give 1 SHEEP and receive 1 ORE
    And Bob starts with 1 ORE
    And Carol starts with 1 ORE
    When Bob accepts the offer
    And Carol accepts the offer
    Then the acceptors list contains Bob
    And the acceptors list contains Carol

  # ── Trade execution ───────────────────────────────────────────────────────

  Scenario: Executing a trade swaps resources between proposer and acceptor
    Given Alice starts with 2 WOOD and 0 BRICK
    And Bob starts with 0 WOOD and 1 BRICK
    When Alice proposes to give 2 WOOD and receive 1 BRICK
    And Bob accepts the offer
    And Alice executes the trade with Bob
    Then Alice has 0 WOOD
    And Alice has 1 BRICK
    And Bob has 2 WOOD
    And Bob has 0 BRICK

  Scenario: Proposer selects one acceptor; other acceptors are unaffected
    Given Alice starts with 1 SHEEP and 0 ORE
    And Bob starts with 0 SHEEP and 1 ORE
    And Carol starts with 0 SHEEP and 1 ORE
    When Alice proposes to give 1 SHEEP and receive 1 ORE
    And Bob accepts the offer
    And Carol accepts the offer
    And Alice executes the trade with Bob
    Then Alice has 0 SHEEP
    And Alice has 1 ORE
    And Bob has 1 SHEEP
    And Bob has 0 ORE
    And Carol has 0 SHEEP
    And Carol has 1 ORE

  Scenario: Executing with a player who declined raises an error
    Given Alice starts with 2 WOOD
    And Bob starts with 1 BRICK
    When Alice proposes to give 2 WOOD and receive 1 BRICK
    And Bob declines the offer
    Then executing the trade with Bob raises an error
