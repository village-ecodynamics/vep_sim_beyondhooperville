package com.mesaverde.specialization;

import java.util.ArrayList;
import java.util.HashMap;

import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.specialization.tasks.Task;

/** While monitoring trades, also calculates the amount of production pressure placed on each agent.
 *  How this works:
 *  
 *  1) Incoming request for resource creates upward pressure to increase production (we can call this pressure +P, and is based, using expected value, on the amount being requested)
 *  2) If we do have the resource, this creates downward pressure because we expect to sell it (twice the pressure as incoming, or -2P)
 *  3) If the agent doesn't have anything the trade partner willing to accept, this should offset the pressure (up to 2P)
 *  4) If we get the sale, then pressure should have been a net of 0, as we were able to provide with our current production levels.  (up to P)
 * @author Denton Cockburn
 *
 */
public class SocialTradeManager extends TradeManager {
	protected double influenceRate;
	private static SocialTradeManager instance;

	/** We only need one SocialTradeManager for our application */
	public static SocialTradeManager getInstance() {
		if (instance == null)
			instance = new SocialTradeManager();

		return instance;
	}
	
	@Override
    /* Trade resource between 2 agents; factor in transportation costs */
	protected void trade(SpecializedAgent seller, Resource sellerResource,
			int amountBuyerGets, SpecializedAgent buyer, Resource buyerResource,
			int amountSellerGets, double calorieCost) {

        /*
        if (SpecializedAgent.USE_SOCIAL_INFLUENCE_FOR_SPECIALIZATION)
            seller.updateSocialPressure(getTaskThatProduces(seller, sellerResource), amountBuyerGets);
            */
		super.trade(seller, sellerResource, amountBuyerGets, buyer, buyerResource, amountSellerGets, calorieCost);
        outputBarterLinkData(seller, buyer, amountBuyerGets, amountSellerGets);

	}

    /** Writes the results of the trade to BARTER output files for analysis */
    private void outputBarterLinkData(SpecializedAgent seller, SpecializedAgent buyer, int amountBuyerGets, int amountSellerGets) {
        // output results to file for analysis
        String fileName = "BARTER_EXCHANGE_INFO" + seller.getSwarm().getFileID() + "_";
        String exchangeInfo = seller.getTag() + ", " + buyer.getTag() 
                + ", " + amountBuyerGets;
        seller.getSwarm().log(exchangeInfo + "\n", fileName, true);
        exchangeInfo = buyer.getTag() + ", " + seller.getTag()
                + ", " + amountSellerGets;
        seller.getSwarm().log(exchangeInfo + "\n", fileName, true);
    }

    private Task getTaskThatProduces(SpecializedAgent sa, Resource r) {
        ArrayList<Task> tasks = sa.getAllocationStrategy().getTasks();

        for (Task t : tasks) {
            if (t.getResourceType() == r.getClass())
                return t;
        }

        return null;
    }
	
	@Override
	public void processTrades() {					
		// keep track of requests for this year
		recordRequests();		
		
		// go through and try to fulfill the buyOffers
		for (int j = 0; j < buyOffers.size(); j++) {			
			BuyOffer buy = buyOffers.get(j);

			// there's a chance our buyOffer was filled by someone accepting one of our sell offers
			Resource thingWeAreBuying = buy.getAgent().getResource(buy.resource.getClass());
			
			if (alreadyGotEnoughOfResource(thingWeAreBuying)) { // hey, we got it already	
				// buyOffers.remove(j);
				continue; // just skip us then
			}

			// find possible trade partners
			ArrayList<SellOffer> possible = findPossibleTradePartners(buy);
			buy.possibleTradeOffers = possible;
			sortByPrice(possible, buy.agent);
			
			if (possible.isEmpty())
                continue;
			
			influenceRate = 1.0 / possible.size(); // increase influence rate for these agents					
			
			// go through the sell offers, looking for someone close by, with
			// the resource we are looking for
			for (SellOffer sell : possible) {	
				// Social Influence: We're going to need to refund some of that pressure if the requesting agent doesn't have needed trading resources
				/* Not used
                int amountAgentHasAvailable = amountSellerWillingToTrade(sell);
				
                
                int amountTraded = 0; // the amount traded between the agents
				int amountCanTrade = Math.min(amountAgentHasAvailable, buy.resource.getAmount());  // what we're expecting to trade								
				*/
                
				for (Class<? extends Resource> tradeItem : sell.getAcceptableItemsInOrder()) {
					Resource buyerProductToGive = buy.getProductMatching(tradeItem);
					
					// so we're willing to accept this
					if (buyerProductToGive != null && buyerProductToGive.getAmount() > buyerProductToGive.getSatisfactoryAmount()) {
						// how much is he buying?
						Resource sellerResource = sell.resource;
						Resource buyerResource = buy.resource;

						// limit by amount available (same resource type)
						// how much we want and how much they are willing to trade
						int maxTradeAmount = (int) Math.min(
								buyerResource.getAmount(), amountSellerWillingToTrade(sell));						

						// now how much of the tradeable resource
						// does this work out to?
						Double sellerUnitCost = sellerResource.getUnitCost();						
						double totalCost = sellerUnitCost * maxTradeAmount;
						int amountBuyerWillingToGive = (int) (buyerProductToGive.getAmount() - buyerProductToGive.getMaxStorage());

						// limit the amount the buyer willing to give by the amount the seller willing to take
                        if (SpecializedAgent.ENFORCING_MAXIMUMS) {
                            amountBuyerWillingToGive = Math.min(amountBuyerWillingToGive, amountSellerWillingToTake(sell.agent, buyerProductToGive));
                        }

						if (amountBuyerWillingToGive <= 0) {							
							continue; // go to the next item in the buyer's list													
						}
						// best cost determined by cost to the agent
						// we also factor in resource decay when calculating the price of an item
						// but only when a seller is receiving the item
						Double buyerProductPrice = buyerProductToGive.getUnitCost(); // * (1 - buyerProductToGive.getDecayRate());

						double maxCals = Math.min(totalCost, amountBuyerWillingToGive * buyerProductPrice);																
						int amountSellerGets = (int) (maxCals / buyerProductPrice);
						// limit by amount buyer actually willing to give						
						int amountBuyerGets = (int) (maxCals / sellerUnitCost);

						// complete the trade if we're getting something
						if (amountSellerGets > 0 && amountBuyerGets > 0) {
							buy.resource.decreaseAmount(amountBuyerGets); // reduce								
							
							// our
							// request,
							// as
							// we've
							// now
							// gotten
							// some
							// of
							// it
							trade(sell.agent, sellerResource, amountBuyerGets,
									buy.getAgent(), buyerProductToGive, amountSellerGets,
									amountBuyerGets * sellerUnitCost); 
							
							// Social Influence: update the amount traded
							//amountTraded += amountBuyerGets;

							// record the trade, we only record the trade for the item that was requested
							// this means only buyers' trades count in here.  May change later if needed.
							recordTrade(buy.getAgent(), buy.resource.getClass(), amountBuyerGets);	

						}
					}

					// buyer got everything
					if (buy.resource.getAmount() == 0) {
						buyOffers.remove(buy);
						break;
					}

					// seller isn't willing to trade anymore
					if (!sell.agent.isWillingToTrade(sell.resource)) {
						sellOffers.remove(sell);
						break;
					}
				}
				
				// buyer got everything
				if (buy.resource.getAmount() == 0) {
					buyOffers.remove(buy);
					break;
				}
			}
			
			
		}

		// let agents know of offers that weren't filled
		returnUnfilledTradeOffers();
	}

	private boolean alreadyGotEnoughOfResource(Resource thingWeAreBuying) {
		return (thingWeAreBuying.getAmount() >= thingWeAreBuying.getMaxStorage());
	}

	private int amountSellerWillingToTrade(SellOffer so) {
		return (int)(so.resource.getAmount() - so.resource.getTradeThresholdAmount());
	}
}
