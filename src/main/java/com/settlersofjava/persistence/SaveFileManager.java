package com.settlersofcava.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The ONLY class that reads/writes to the filesystem.
 * Keeps all I/O concerns isolated from game logic.
 */
public class SaveFileManager {

    private static final String DEFAULT_SAVE_PATH = "settlers_save.json";

    public void save(String json) throws IOException {
        Files.writeString(Path.of(DEFAULT_SAVE_PATH), json);
    }

    public String load() throws IOException {
        return Files.readString(Path.of(DEFAULT_SAVE_PATH));
    }

    public boolean saveExists() {
        return Files.exists(Path.of(DEFAULT_SAVE_PATH));
    }
}

