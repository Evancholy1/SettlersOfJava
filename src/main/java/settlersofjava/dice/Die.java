package settlersofjava.dice;

/**
 * Abstraction over a single die.
 * Inject a fake implementation in tests to control dice outcomes.
 */
public interface Die {
    /** Returns a value between 1 and the die's number of faces. */
    int roll();
}

