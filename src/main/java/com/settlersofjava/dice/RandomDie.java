package com.settlersofcava.dice;

import java.util.Random;

/**
 * PATTERN: Dependency Injection (real implementation; inject Die interface in tests)
 * A standard fair die with a configurable number of faces.
 */
public class RandomDie implements Die {

    private final int faces;
    private final Random random;

    public RandomDie(int faces) {
        this.faces = faces;
        this.random = new Random();
    }

    /** Convenience constructor for a standard 6-sided die. */
    public RandomDie() {
        this(6);
    }

    @Override
    public int roll() {
        return random.nextInt(faces) + 1;
    }
}

