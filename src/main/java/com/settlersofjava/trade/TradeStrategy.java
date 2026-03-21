package com.settlersofcava.trade;

import com.settlersofcava.resources.ResourceType;

/**
 * PATTERN: Strategy
 * Defines the trade rate for a given resource.
 * Implementations: BankTradeStrategy (4:1), PortTradeStrategy (3:1 or 2:1).
 */
public interface TradeStrategy {

    /**
     * Returns how many of the offered resource are needed for 1 of any other resource.
     * @param offered the resource type being offered
     * @return the trade rate (e.g., 4, 3, or 2)
     */
    int getRate(ResourceType offered);
}

