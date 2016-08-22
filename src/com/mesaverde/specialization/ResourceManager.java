package com.mesaverde.specialization;

import java.util.ArrayList;
import java.util.Collections;

import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.village.Village;

/** Currently ignores thresholds such as philanthropic, etc.
 * Currently willing to trade anything above what we need.
 * @author Denton Cockburn
 *  Manages resources for agents, such as shortages, surpluses, and maximum storage
 */
public class ResourceManager {
	private SpecializedAgent owner;
	private ArrayList<Resource> shortages = new ArrayList<Resource>();
	private ArrayList<Resource> surpluses = new ArrayList<Resource>();	
	private ArrayList<ShortageListener> shortageListeners = new ArrayList<ShortageListener>();

	/** This method doesn't really belong here, somewhere more logical would be better.  TODO: review */
	public static TradeManager getTradeManager() {
		// return TradeManager.getInstance();
		return SocialTradeManager.getInstance();
	}
	
	public ResourceManager(SpecializedAgent owner) {
		this.owner = owner;
	}

	public void registerShortage(Resource r) {
		shortages.add(r);
	}

	public void registerSurplus(Resource r) {
		surpluses.add(r);
	}	

	/** Pass on trade requests to the TradeManager */
	@SuppressWarnings("unchecked")
	public void submitTradeRequests() {				
		TradeManager tm = ResourceManager.getTradeManager();		

		// We're willing to trade any of our surpluses in exchange for our shortages	
		ArrayList<Class<? extends Resource>> willingToAcceptClasses = new ArrayList<Class<? extends Resource>>(); // need this for when selling

		// we're willing to accept anything we already have (even if we have none of it right now)
		for (Resource r : owner.getResources()) {
			// we accept everything but what we're selling			
			willingToAcceptClasses.add(r.getClass());
		}		

		// we're willing to trade anything that's not in shortage

		ArrayList<Resource> willingToGive = (ArrayList<Resource>) owner.getResources().clone();

		ArrayList<Resource> newList = new ArrayList<Resource>();
		for (Resource res : willingToGive) {
			if (res.getAmount() > res.getShortageThreshold())
				newList.add(res);
		}
		willingToGive = newList;
		Collections.shuffle(willingToGive, new java.util.Random(Village.uniformIntRand(0, Integer.MAX_VALUE)));  // so we don't always try to give away the same thing each time

		// we submit all shortages anyway so we get back the shortage results
		for (Resource r : shortages) {					
			tm.buy(new TradeManager.BuyOffer(owner, r, willingToGive));			
		}		

		/* agents are willing to accept any resource they already have
		 * in exchange for what they are sellings 
		 */
		if (willingToAcceptClasses.size() > 0) {			
			for (Resource r : surpluses) {
				// make sure we're not accepting the thing we are selling
				ArrayList<Class<? extends Resource>> willTake = (ArrayList<Class<? extends Resource>>) willingToAcceptClasses.clone();
				willTake.remove(r.getClass());
				tm.sell(new TradeManager.SellOffer(owner, r, willTake));
			}
		}

		// we can clear the requests now that we've submitted them
		shortages.clear();
		surpluses.clear();
	}

	// shortages, we let the shortage listeners take care of them
	public void returnFailedBuyRequest(TradeManager.BuyOffer buyOffer) {
		Resource r = buyOffer.getAgent().getResource(buyOffer.getResource().getClass());
		if (r.getAmount() < 0) {
			for (ShortageListener sl : shortageListeners) {
				sl.onShortage(r.makeInstance(0 - r.getAmount(), 0));  // trade to get back to 0 at least
			}	
		}
	}

	public void addShortageListener(ShortageListener shortageListener) {
		shortageListeners .add(shortageListener);	
	}
}
