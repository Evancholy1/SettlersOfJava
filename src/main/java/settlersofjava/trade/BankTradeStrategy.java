package settlersofjava.trade;

import settlersofjava.resources.ResourceType;

/**
 * PATTERN: Strategy (default implementation)
 * Standard 4:1 bank trade — always costs 4 of any resource.
 */
public class BankTradeStrategy implements TradeStrategy {

    @Override
    public int getRate(ResourceType offered) {
        return 4;
    }
}

