package com.mesaverde.specialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.village.Agent;
import com.mesaverde.village.Utilities;
import com.mesaverde.village.Village;

/**
 * Here's how it works: Each SpecializedAgent either offers to buy, or sell resources. They
 * also provide a list of items they are willing to accept (in the case of a
 * sell) or a list of items they are able to provide (in case of buy). This class
 * then matches SpecializedAgents up with others within their neighbourhoods to complete
 * the trade. Trades are currently based on a fair caloric cost basis.
 * 
 * Remember to set the tradeRange variable, as this determines how far agents are allowed to be to trade.
 * 
 * @author Denton Cockburn
 * 
 */
public class TradeManager {
	protected ArrayList<BuyOffer> buyOffers = new ArrayList<BuyOffer>();
	protected ArrayList<SellOffer> sellOffers = new ArrayList<SellOffer>();		
	public static double tradeRange = Village.MAX_COOP_RADIUS_BRN; // the max distance we'll allow trades, going to default to 40 right now (same as BRN)
	protected static TradeManager instance;
	protected static int completedTrades = 0;  // just to see how many trades done this way
	protected static HashMap<Integer, HashMap<Class<? extends Resource>, Double>> requestRecord = new HashMap<Integer, HashMap<Class<? extends Resource>, Double>>();
	protected static HashMap<Integer, HashMap<Class<? extends Resource>, Double>> tradeRecord = new HashMap<Integer, HashMap<Class<? extends Resource>, Double>>();

	protected TradeManager() {}

	/** We only need one TradeManager for our application */
	public static TradeManager getInstance() {
		if (instance == null)
			instance = new TradeManager();

		return instance;
	}

	/** Returns the total number of sales completed through the TradeManager */
	public static int getCompletedTrades() {
		return completedTrades;
	}

	public static class SellOffer {
		protected Resource resource;
		protected ArrayList<Class<? extends Resource>> willingToAccept;
		protected SpecializedAgent agent;		

		public SellOffer(SpecializedAgent agt, Resource r, ArrayList<Class<? extends Resource>> willingToAcceptClasses) {
			this.resource = r;
			this.willingToAccept = willingToAcceptClasses;
			agent = agt;
		}

		/** Returns a copy of the list of acceptable items in the order in which we prefer them.
		 * This order is always in terms of the amount of the item we have.
		 */
		public ArrayList<Class<? extends Resource>> getAcceptableItemsInOrder() {			
			Collections.sort(willingToAccept, new Comparator<Class<? extends Resource>>() {
				@Override
				public int compare(Class<? extends Resource> o1, Class<? extends Resource> o2) {
					Resource r1 = agent.getResource(o1);
					Resource r2 = agent.getResource(o2);					
					
					if (r1 == null && r2 == null )
						return 0;
					
					double r1ratio = r1.getAmount() / r1.getMaxStorage();
					double r2ratio = r2.getAmount() / r2.getMaxStorage();
					
					if (r1ratio < r2ratio)
						return -1;
					
					if (r2ratio > r1ratio)
						return 1;
					
					return 0;
				}
			});
			
			return willingToAccept;
		}				
	}

	public static class BuyOffer {
		protected Resource resource;
		protected ArrayList<Resource> willingToGive;
		protected SpecializedAgent agent;
		protected ArrayList<SellOffer> possibleTradeOffers;		

		public BuyOffer(SpecializedAgent agt, Resource r, ArrayList<Resource> willingToGive) {
			this.resource = r;
			this.willingToGive = willingToGive;	
			agent = agt;
		}

		public Resource getResource() {
			return resource;
		}

		public void setAgent(SpecializedAgent agent) {
			this.agent = agent;
		}

		public SpecializedAgent getAgent() {
			return agent;
		}

		/** Returns the resource this agent is willing to trade that matches the tradeItem.
		 * 
		 * @param tradeItem - the item the seller wishes you to give
		 * @return null if we are not offering to trade the tradeItem
		 */
		public Resource getProductMatching(Class<? extends Resource> tradeItem) {			
			for (Resource r : willingToGive) {
				if (r.getClass() == tradeItem)
					return r;
			}
			
			return null;
		}
	}

	/**
	 * attempt to sell a resource to an available SpecializedAgent. Also provide a list of
	 * items we are willing to accept
	 */
	public synchronized void sell(SellOffer sell) {
		sellOffers.add(sell);

	}

	/**
	 * attempt to buy a resource. Also provide a list of Resources that you are
	 * willing to use as currency
	 */
	public synchronized void buy(BuyOffer buy) {
		buyOffers.add(buy);
	}

	public void processTrades() {					
		// keep track of requests for this year
		recordRequests();		

		// go through and try to fulfill the buyOffers
		for (int j = 0; j < buyOffers.size(); j++) {
			BuyOffer buy = buyOffers.get(j);
			
			if (buy.willingToGive.size() == 0)
				continue; // agent has nothing to give

			// there's a chance our buyOffer was filled by someone accepting one of our sell offers
			Resource thingWeAreBuying = buy.getAgent().getResource(buy.resource.getClass());
			if (thingWeAreBuying.getAmount() >= buy.resource.getAmount()) { // hey, we got it already	
				buy.resource.decreaseAmount(buy.resource.getAmount());  // make it 0
				continue; // just skip us then
			}

			// find possible trade partners
			ArrayList<SellOffer> possible = findPossibleTradePartners(buy);
			sortByPrice(possible, buy.agent);

			// go through the sell offers, looking for someone close by, with
			// the resource we are looking for
			for (SellOffer sell : possible) {	
				for (Resource buyerProductToGive : buy.willingToGive) {
					// so we're willing to accept this
					if (buyerProductToGive.getAmount() > buyerProductToGive.getShortageThreshold()
							&& sell.willingToAccept.contains(buyerProductToGive.getClass())) {
						// how much is he buying?
						Resource sellerResource = sell.resource;
						Resource buyerResource = buy.resource;

						// limit by amount available (same resource type)
						int maxTradeAmount = (int) Math.min(
								buyerResource.getAmount(), sellerResource
								.getAmount() - sellerResource.getTradeThresholdAmount());						

						// now how much of the tradeable resource
						// does this work out to?
						Double sellerUnitCost = sellerResource.getUnitCost();						
						double totalCost = sellerUnitCost * maxTradeAmount;
						int amountBuyerWillingToGive = (buyerProductToGive.getAmount() - buyerProductToGive.getShortageThreshold());

						// limit the amount the buyer willing to give by the amount the seller willing to take

						amountBuyerWillingToGive = Math.min(amountBuyerWillingToGive, amountSellerWillingToTake(sell.agent, buyerProductToGive));

						if (amountBuyerWillingToGive <= 0) {							
							continue; // go to the next item in the buyer's list													
						}
						// best cost determined by market price if available, or
						// just cost to the agent
						Double buyerProductPrice = buyerProductToGive.getUnitCost();

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
									amountBuyerGets * sellerUnitCost); // not actually
							// maxCals,
							// as the
							// number is
							// rounded
							// down

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

	protected int amountSellerWillingToTake(SpecializedAgent agent, Resource buyerProductToGive) {
		Resource r = agent.getResource(buyerProductToGive.getClass());

        // Our only limit is the maximum we can store
		return (int) (SpecializedAgent.REAL_MAX_FACTOR * r.getMaxStorage() - r.getAmount());
	}

	@SuppressWarnings("unchecked")
    /* Sort trade offers by price, including transportation costs */
	protected void sortByPrice(ArrayList<SellOffer> possible, final Agent agent) {
		Collections.sort(possible, new Comparator() {
			@Override
			public int compare(Object arg0, Object arg1) {
				SellOffer one = (SellOffer) arg0;
				SellOffer two = (SellOffer) arg1;

				double unitCostOne = one.resource.getUnitCost();
                unitCostOne += calcUnitTransportCost(one, agent);
				double unitCostTwo = two.resource.getUnitCost();
                unitCostTwo += calcUnitTransportCost(two, agent);
                
				if (unitCostOne < unitCostTwo)
					return -1;
				else if (unitCostOne > unitCostTwo)
					return 1;

				return 0;
			}

		});

	}

    private double calcUnitTransportCost(SellOffer offer, Agent agent) {
        double totalTransport = offer.resource.calculateTransportCost(offer.resource.getAmount(), Utilities.distance(offer.agent, agent));
        double cost = totalTransport / offer.resource.getAmount();

        return cost;
    }

	protected ArrayList<SellOffer> findPossibleTradePartners(BuyOffer buy) {
		ArrayList<SellOffer> possible = new ArrayList<SellOffer>();

		for (SellOffer s : sellOffers)
			if (isPossibleTradePartner(buy, s))
				possible.add(s);
		return possible;
	}

	// record the trade, we only record the trade for the item that was requested
	// this means only buyers' trades count in here.  May change later if needed.
	protected void recordTrade(SpecializedAgent agent,
			Class<? extends Resource> resourceType, int amountBuyerGets) {
		int year = agent.getWorldTime();
		HashMap<Class<? extends Resource>, Double> tradesForYear = tradeRecord.get(year);

		if (tradesForYear == null) {
			tradesForYear = new HashMap<Class<? extends Resource>, Double>();
			tradeRecord.put(year, tradesForYear);
		}

		Double currentVal = tradesForYear.get(resourceType);
		if (currentVal == null)
			currentVal = 0.0;

		currentVal += amountBuyerGets;
		tradesForYear.put(resourceType, currentVal);

	}

	/** Records all the requests that came in */
	protected void recordRequests() {
		if (buyOffers.size() > 0) {
			int year = buyOffers.get(0).getAgent().getWorldTime();

			HashMap<Class<? extends Resource>, Double> requestsForYear = requestRecord.get(year);

			if (requestsForYear == null) {
				requestsForYear = new HashMap<Class<? extends Resource>, Double>();
				requestRecord.put(year, requestsForYear);
			}

			for (BuyOffer bo : buyOffers) {
				Double current = requestsForYear.get(bo.resource.getClass());
				if (current == null)
					current = 0.0;

				current += bo.resource.getAmount();
				requestsForYear.put(bo.resource.getClass(), current);

			}			
		}		
	}

	protected boolean isPossibleTradePartner(BuyOffer buy, SellOffer sell) {
		boolean res = false;

		if (sell.resource.getClass() == buy.resource.getClass() &&
				Utilities.distance(buy.getAgent(), sell.agent) <= tradeRange &&
				sell.agent.isWillingToTrade(sell.resource))
			res = true;

		return res;
	}

    /* Trade resource between 2 agents; factor in transportation costs */
	protected void trade(SpecializedAgent seller, Resource sellerResource,
			int amountBuyerGets, SpecializedAgent buyer, Resource buyerResource,
			int amountSellerGets, double calorieCost) {

		// first change the resource balances		
		sellerResource.decreaseAmount(amountBuyerGets);
		buyerResource.decreaseAmount(amountSellerGets);
		
        double distance = Utilities.distance(seller, buyer);
        double transportCosts = sellerResource.calculateTransportCost(amountBuyerGets, distance);
        transportCosts += buyerResource.calculateTransportCost(amountSellerGets, distance);
        transportCosts /= 2;
        
        // only the buyer pays.  If we wanted both to pay, then we should
        // have divided by 4 above
        //seller.eatMaize((int) transportCosts);
        buyer.eatMaize((int) transportCosts);
        
		seller.addResource(amountSellerGets, buyerResource, calorieCost);
		buyer.addResource(amountBuyerGets, sellerResource, calorieCost + transportCosts);
		completedTrades++;				
	}

	/** returns resources back to agents if no one accepted them */
	protected void returnUnfilledTradeOffers() {
		// this means the agent suffered from a shortage that can't be satisfied
		for (BuyOffer offer : buyOffers) {
			if (offer.resource.getAmount() > 0) {
				offer.getAgent().getResourceManager().returnFailedBuyRequest(offer);								
			}
		}

		// we're done trading, so clear any remainders out
		buyOffers.clear();
		sellOffers.clear();
	}

	// Used to gather statistics from our trading
	public double getRequestSum(int year, Class<? extends Resource> resourceType) {
		HashMap<Class<? extends Resource>, Double> requestsForYear = requestRecord.get(year);
		double val = 0;

		// try to get the result
		if (requestsForYear != null) {
			Double x = requestsForYear.get(resourceType);

			if (x != null)
				val = x;
		}

		return val;
	}

	public double getExchangeSum(int year, Class<? extends Resource> resourceType) {
		HashMap<Class<? extends Resource>, Double> exchangesForYear = tradeRecord.get(year);
		double val = 0;

		// try to get the result
		if (exchangesForYear != null) {
			Double x = exchangesForYear.get(resourceType);

			if (x != null)
				val = x;
		}

		return val;
	}

	public static HashMap<Integer, HashMap<Class<? extends Resource>, Double>> getRequestRecord() {
		return requestRecord;
	}

	public static HashMap<Integer, HashMap<Class<? extends Resource>, Double>> getTradeRecord() {
		return tradeRecord;
	}
}
