package com.settlersofcava.trade;

import com.settlersofcava.board.PortType;
import com.settlersofcava.resources.ResourceType;

/**
 * PATTERN: Strategy
 * Port trade rate depends on the port type:
 * - Generic port (3:1): 3 of any resource
 * - Specific port (2:1): 2 of the matching resource, 4 otherwise
 */
public class PortTradeStrategy implements TradeStrategy {

    private final PortType portType;

    public PortTradeStrategy(PortType portType) {
        this.portType = portType;
    }

    @Override
    public int getRate(ResourceType offered) {
        return switch (portType) {
            case GENERIC_3_1 -> 3;
            case WOOD_2_1  -> offered == ResourceType.WOOD  ? 2 : 4;
            case BRICK_2_1 -> offered == ResourceType.BRICK ? 2 : 4;
            case WOOL_2_1  -> offered == ResourceType.WOOL  ? 2 : 4;
            case GRAIN_2_1 -> offered == ResourceType.GRAIN ? 2 : 4;
            case ORE_2_1   -> offered == ResourceType.ORE   ? 2 : 4;
        };
    }
}

