Feature: Catan Core Mechanics and Architecture

  Scenario: Constructing a standard board
    Given a standard Catan board is constructed
    Then the board has 19 tiles
    And the board has 54 vertices
    And the board has 72 edges

  Scenario: Game Engine Initialization
    Given a new game is started
    Then the current phase is "SETUP"
    And the current player index is 0

  Scenario: Advancing Game Phases
    Given a new game is started
    When the phase is advanced from SETUP to ROLL
    Then the current phase is "ROLL"
    When the phase is advanced
    Then the current phase is "TRADE"

  Scenario: Ending a Turn
    Given a new game is started
    Then the current player index is 0
    When the turn ends
    Then the current player index is 1
    And the current phase is "ROLL"

  Scenario: Rolling the dice with a fixed outcome
    Given a new game is started
    When the dice are rolled and show 8
    # eventually add a Then for resource distribution

  Scenario: Player receives resources successfully
    Given a player named "Evan"
    When the player receives 3 WHEAT
    And the player receives 2 ORE
    Then the player has 3 WHEAT
    And the player has 2 ORE

  Scenario: Player attempts to remove more resources than they have
    Given a player named "Danny"
    When the player receives 1 WOOD
    Then removing 2 WOOD throws an error

  Scenario: Player can afford a settlement
    Given a player named "Alex"
    When the player receives 1 WOOD
    And the player receives 1 BRICK
    And the player receives 1 SHEEP
    And the player receives 1 WHEAT
    Then the player can afford a settlement

  Scenario: Player cannot afford a settlement
    Given a player named "William"
    When the player receives 1 WOOD
    And the player receives 1 BRICK
    Then the player cannot afford a settlement