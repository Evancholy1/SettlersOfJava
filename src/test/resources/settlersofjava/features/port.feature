Feature: Port generation

  Scenario: Board has exactly 9 ports
    Given a board with ports is constructed
    Then there are exactly 9 port edges

  Scenario: Port distribution has correct counts
    Given a board with ports is constructed
    Then there are exactly 4 generic 3:1 ports
    And there is exactly 1 wood 2:1 port
    And there is exactly 1 brick 2:1 port
    And there is exactly 1 sheep 2:1 port
    And there is exactly 1 wheat 2:1 port
    And there is exactly 1 ore 2:1 port

  Scenario: All port vertices are coastal
    Given a board with ports is constructed
    Then every port vertex touches fewer than 3 tiles

  Scenario: Each port edge has matching port types on both endpoints
    Given a board with ports is constructed
    Then every port vertex pair on an edge shares the same port type
