Feature: Game phase transitions

  Scenario: Game starts in SETUP phase
    Given a new game is started
    Then the current phase is "SETUP"


  Scenario: Turn advances to next player after end turn
    Given a new game is started
    Then the current player index is 0
    When the turn ends
    Then the current player index is 1

  Scenario: Phase advances from ROLL to TRADE
    Given a new game is started
    When the phase is advanced from SETUP to ROLL
    And the phase is advanced
    Then the current phase is "TRADE"

  Scenario: Phase becomes BUILD after rolling during main game
    Given a new game is started
    When 8 setup turns end
    And the dice are rolled
    Then the current phase is "BUILD"

  Scenario: End turn from BUILD resets phase to ROLL
    Given a new game is started
    When 8 setup turns end
    And the dice are rolled
    And the turn ends
    Then the current phase is "ROLL"

  Scenario: End turn from BUILD advances to next player
    Given a new game is started
    When 8 setup turns end
    And the dice are rolled
    And the turn ends
    Then the current player index is 1
