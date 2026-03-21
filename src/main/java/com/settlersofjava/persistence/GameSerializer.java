package com.settlersofcava.persistence;

import com.settlersofcava.engine.CatanGame;

/**
 * PATTERN: (pairs with GameDeserializer for save/load)
 * Converts the current CatanGame state into a JSON string.
 * Uses Gson (add to build.gradle: implementation "com.google.code.gson:gson:2.10.1")
 */
public class GameSerializer {

    public String serialize(CatanGame game) {
        // TODO: use Gson to convert game state to JSON
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

