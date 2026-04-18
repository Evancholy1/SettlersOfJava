Feature: Setup phase

  Scenario: Setup follows snake order for 4 players
    Given a new game is started
    Then the current player index is 0
    When a setup turn ends
    Then the current player index is 1
    When a setup turn ends
    Then the current player index is 2
    When a setup turn ends
    Then the current player index is 3
    When a setup turn ends
    Then the current player index is 3
    When a setup turn ends
    Then the current player index is 2
    When a setup turn ends
    Then the current player index is 1
    When a setup turn ends
    Then the current player index is 0

  Scenario: Round 2 is not active at the start of setup
    Given a new game is started
    Then setup round 2 is not active

  Scenario: Round 2 becomes active after all players place their first settlement
    Given a new game is started
    When 4 setup turns end
    Then setup round 2 is active

  Scenario: Setup phase ends after all players have placed twice
    Given a new game is started
    When 8 setup turns end
    Then the current phase is "ROLL"

  Scenario: Second settlement grants one resource per adjacent non-desert tile
    Given a setup board and player are initialized
    When the player claims setup resources from a resource-adjacent vertex
    Then the player's total resources equal the adjacent non-desert tile count

  Scenario: First settlement grants no resources
    Given a setup board and player are initialized
    When the player claims no resources because it is round 1
    Then the player has 0 total resources
