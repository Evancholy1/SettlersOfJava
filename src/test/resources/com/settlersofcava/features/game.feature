Feature: Game phase transitions

  Scenario: Game starts in SETUP phase
    Given a new game is started
    Then the current phase is "SETUP"

