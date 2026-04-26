Feature: Longest Road

  Scenario: 5 connected roads claims Longest Road
    Given Alice has a linear road chain of 5 edges
    Then the longest road length for Alice is 5
    And Alice holds the Longest Road

  Scenario: 4 roads do not reach the minimum of 5
    Given Alice has a linear road chain of 4 edges
    Then the longest road length for Alice is 4
    And Alice does not hold the Longest Road

  Scenario: Forked road - longest continuous path through the fork
    Given Alice has roads forming a fork: a stem of 2 then two branches of 2 and 3
    Then the longest road length for Alice is 5

  Scenario: Loop of 5 roads counts all edges
    Given Alice has 5 roads forming a closed loop
    Then the longest road length for Alice is 5

  Scenario: Opponent settlement in the middle breaks the chain
    Given Alice has a linear road chain of 6 edges
    And Bob has a settlement at vertex 3 in the chain
    Then the longest road length for Alice is 3

  Scenario: Player B with longer road takes Longest Road from Player A
    Given Alice holds the Longest Road with a chain of 5
    And Bob builds a chain of 6 roads
    Then Bob holds the Longest Road
    And Alice does not hold the Longest Road

  Scenario: Longest Road grants 2 victory points
    Given Alice holds the Longest Road with a chain of 5
    Then Alice has 2 total victory points from Longest Road
