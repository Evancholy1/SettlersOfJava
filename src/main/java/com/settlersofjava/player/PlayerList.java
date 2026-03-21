package com.settlersofcava.player;

import java.util.List;

/**
 * PATTERN: Dependency Injection (injected alongside BoardState into GameController)
 * Wraps the list of players and provides game-level queries.
 */
public class PlayerList {

    private final List<Player> players;

    public PlayerList(List<Player> players) {
        if (players == null || players.size() < 2) {
            throw new IllegalArgumentException("A game requires at least 2 players.");
        }
        this.players = List.copyOf(players);
    }

    public Player get(int index)      { return players.get(index); }
    public int size()                  { return players.size(); }
    public List<Player> getAll()       { return players; }

    public Player getWinner() {
        // TODO: return player with >= 10 VPs, or null if no winner yet
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

