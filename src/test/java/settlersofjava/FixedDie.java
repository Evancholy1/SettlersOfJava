package settlersofjava;

import settlersofjava.dice.Die;

public class FixedDie implements Die {
    private final int fixedValue;

    public FixedDie(int fixedValue) {
        this.fixedValue = fixedValue;
    }

    @Override
    public int roll() {
        return fixedValue;
    }
}
