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


  Scenario: Player cannot afford a settlement without enough resources
    Given a player named "Evan"
    Then the player cannot afford a settlement

  Scenario: Player can afford a settlement with exact resources
    Given a player named "Evan"
    When the player receives 1 WOOD
    And the player receives 1 BRICK
    And the player receives 1 WOOL
    And the player receives 1 GRAIN
    Then the player can afford a settlement

  Scenario: Player resources do not go below zero
    Given a player named "Danny"
    When the player receives 1 ORE
    Then removing 2 ORE throws an erro

