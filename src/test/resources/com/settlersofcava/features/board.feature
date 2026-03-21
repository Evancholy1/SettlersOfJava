Feature: Standard board construction

  Scenario: A standard Catan board has the correct topology
    Given a standard Catan board is constructed
    Then the board has 19 tiles
    And the board has 54 vertices
    And the board has 72 edges

