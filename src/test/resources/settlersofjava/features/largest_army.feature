Feature: Largest Army and Total Victory Points
  Players should be able to claim the Largest Army after playing 3 Knight cards.
  The Largest Army grants 2 Victory Points.
  Total Victory Points include settlements, cities, Largest Army, and hidden VP cards.

  Scenario: Calculating Total Victory Points with hidden cards
    Given a player named "Alice" has 1 public victory point
    And the player has a hidden Victory Point card
    Then the player should have 2 total victory points

  Scenario: Claiming the Largest Army
    Given a player named "Alice" has played 2 Knight cards
    When the player plays another Knight card
    Then the player should receive the Largest Army
    And the player should have 2 total victory points

  Scenario: Stealing the Largest Army
    Given "Player 1" holds the Largest Army with 3 Knight cards
    And "Player 2" has played 3 Knight cards
    When "Player 2" plays another Knight card
    Then "Player 2" should receive the Largest Army
    And "Player 1" should lose the Largest Army