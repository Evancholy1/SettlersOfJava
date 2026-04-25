Feature: Building phase — roads, settlements, and city upgrades

  Scenario: Player places a road adjacent to their settlement
    Given a player has a settlement on the board and road resources
    When the player places a road on an adjacent edge
    Then the edge has a road owned by the player
    And the player has 1 less brick and 1 less wood

  Scenario: Player places a settlement at a vertex adjacent to their road
    Given a player has a road on the board and settlement resources
    When the player places a settlement at a valid vertex
    Then the vertex has a settlement owned by the player
    And the player has 1 less brick, 1 less wood, 1 less wheat, and 1 less sheep

  Scenario: Player upgrades a settlement to a city with enough resources
    Given a player has a settlement on the board and city resources
    When the player upgrades the settlement to a city
    Then the vertex now contains a city owned by the player
    And the player has 3 less ore and 2 less wheat
    And the player gains a victory point from the upgrade

  Scenario: City upgrade is invalid without enough resources
    Given a player has a settlement on the board but not enough city resources
    When the player checks whether the city upgrade is valid
    Then the city upgrade is not valid

  Scenario: City upgrade is invalid on an opponent's settlement
    Given a player's settlement and an opponent's settlement are on the board
    When the player checks whether they can upgrade the opponent's settlement
    Then the city upgrade is not valid
