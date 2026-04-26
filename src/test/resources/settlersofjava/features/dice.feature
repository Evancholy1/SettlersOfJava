Feature: Dice rolling and resource distribution

  Scenario: Settlement owner receives 1 resource when the matching tile is rolled
    Given a board with a settlement on a known resource tile
    When the matching token number is rolled
    Then the settlement owner has 1 of the expected resource

  Scenario: City owner receives 2 resources when the matching tile is rolled
    Given a board with a city on a known resource tile
    When the matching token number is rolled
    Then the city owner has 2 of the expected resource

  Scenario: No resources are distributed when no tile matches the roll
    Given a board with a settlement on a known resource tile
    When a number that matches no tile token is rolled
    Then the settlement owner has 0 of the expected resource

  Scenario: Only the player on the activated tile receives resources
    Given a board with two players each on different resource tiles
    When the first player's tile token is rolled
    Then only the first player receives a resource
    And the second player receives nothing

  Scenario: Player with less than 8 cards does not discard on a 7
    Given a player has 7 total resource cards
    When a 7 is rolled and the discard rule is applied
    Then the player should have 7 total resource cards left

  Scenario: Player with exactly 8 cards discards half on a 7
    Given a player has 8 total resource cards
    When a 7 is rolled and the discard rule is applied
    Then the player should have 4 total resource cards left

  Scenario: Player with an odd number of cards (9) discards half rounded down on a 7
    Given a player has 9 total resource cards
    When a 7 is rolled and the discard rule is applied
    Then the player should have 5 total resource cards left