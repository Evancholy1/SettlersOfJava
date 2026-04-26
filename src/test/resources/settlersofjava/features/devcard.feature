Feature: Development Cards
  Players should be able to buy cards, hold them, and play them (but not on the same turn).

  Scenario: Purchasing a Development Card
    Given a player named "Alice" has 1 WHEAT, 1 SHEEP, and 1 ORE
    When the player purchases a Development Card
    Then the player's resources are deducted
    And the player has 1 unplayable Development Card in their hand

  Scenario: Unlocking and Playing a Knight Card
    Given a player named "Bob" bought a "Knight" card on a previous turn
    When the player plays the card
    Then the card is marked as played
    And the card remains in the player's hand