Feature: Port trading — rates and execution

  # ── PortTradeStrategy: rate table ─────────────────────────────────────────

  Scenario Outline: Generic 3:1 port gives rate 3 for every resource
    Given the port type is GENERIC_3_1
    When the trade rate for <resource> is looked up
    Then the rate is 3
    Examples:
      | resource |
      | WOOD     |
      | BRICK    |
      | SHEEP    |
      | WHEAT    |
      | ORE      |

  Scenario Outline: Specific 2:1 port gives rate 2 only for its own resource
    Given the port type is <port>
    When the trade rate for <resource> is looked up
    Then the rate is <rate>
    Examples:
      | port       | resource | rate |
      | WOOD_2_1   | WOOD     | 2    |
      | WOOD_2_1   | BRICK    | 4    |
      | BRICK_2_1  | BRICK    | 2    |
      | BRICK_2_1  | ORE      | 4    |
      | WOOL_2_1   | SHEEP    | 2    |
      | WOOL_2_1   | WHEAT    | 4    |
      | GRAIN_2_1  | WHEAT    | 2    |
      | GRAIN_2_1  | WOOD     | 4    |
      | ORE_2_1    | ORE      | 2    |
      | ORE_2_1    | SHEEP    | 4    |

  # ── Best-rate selection (mirrors GameController.getBestRate) ──────────────

  Scenario: Player with no port uses bank rate of 4
    Given a rate player has a settlement not at a port
    When the best rate for WOOD is computed for that player
    Then the best rate is 4

  Scenario: Player at a generic 3:1 port gets rate 3 for any resource
    Given a rate player has a settlement at a GENERIC_3_1 port vertex
    When the best rate for BRICK is computed for that player
    Then the best rate is 3

  Scenario: Player at a wood 2:1 port gets rate 2 when trading wood
    Given a rate player has a settlement at a WOOD_2_1 port vertex
    When the best rate for WOOD is computed for that player
    Then the best rate is 2

  Scenario: Player at a wood 2:1 port still pays 4 for non-matching resources
    Given a rate player has a settlement at a WOOD_2_1 port vertex
    When the best rate for ORE is computed for that player
    Then the best rate is 4

  Scenario: Player with both a 3:1 and a 2:1 port gets the minimum rate
    Given a rate player has settlements at both a GENERIC_3_1 and a WOOD_2_1 port vertex
    When the best rate for WOOD is computed for that player
    Then the best rate is 2

  # ── Trade execution ───────────────────────────────────────────────────────

  Scenario: 4:1 bank trade spends 4 give resources and yields 1 receive resource
    Given a trade player starts with 4 WOOD
    When a 4:1 trade converts WOOD to BRICK
    Then the trade player ends with 0 WOOD and 1 BRICK

  Scenario: 3:1 port trade spends 3 give resources and yields 1 receive resource
    Given a trade player starts with 3 SHEEP
    When a 3:1 trade converts SHEEP to ORE
    Then the trade player ends with 0 SHEEP and 1 ORE

  Scenario: 2:1 port trade spends 2 give resources and yields 1 receive resource
    Given a trade player starts with 2 ORE
    When a 2:1 trade converts ORE to WHEAT
    Then the trade player ends with 0 ORE and 1 WHEAT
