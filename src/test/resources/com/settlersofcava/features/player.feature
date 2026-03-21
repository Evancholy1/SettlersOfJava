Feature: Player resource management

  Scenario: Player receives resources from a dice roll
    Given a player named "Evan"
    When the player receives 2 WOOD
    And the player receives 1 BRICK
    Then the player has 2 WOOD
    And the player has 1 BRICK

  Scenario: Player cannot spend resources they do not have
    Given a player named "Danny"
    Then the player has 0 ORE

