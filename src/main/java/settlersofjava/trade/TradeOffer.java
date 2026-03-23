package settlersofjava.trade;

import settlersofjava.resources.ResourceType;

/**
 * Value object representing a trade: offering X of one resource for Y of another.
 */
public class TradeOffer {

    private final ResourceType offering;
    private final int offerAmount;
    private final ResourceType wanting;
    private final int wantAmount;

    public TradeOffer(ResourceType offering, int offerAmount,
                      ResourceType wanting,  int wantAmount) {
        this.offering    = offering;
        this.offerAmount = offerAmount;
        this.wanting     = wanting;
        this.wantAmount  = wantAmount;
    }

    public ResourceType getOffering()   { return offering; }
    public int getOfferAmount()         { return offerAmount; }
    public ResourceType getWanting()    { return wanting; }
    public int getWantAmount()          { return wantAmount; }

    @Override
    public String toString() {
        return offerAmount + "x" + offering + " → " + wantAmount + "x" + wanting;
    }
}

