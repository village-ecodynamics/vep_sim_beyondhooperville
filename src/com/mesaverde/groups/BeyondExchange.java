package com.mesaverde.groups;

import java.util.ArrayList;
import java.util.HashSet;

import com.mesaverde.specialization.resources.Maize;
import com.mesaverde.specialization.resources.Meat;
import com.mesaverde.village.Agent;
import com.mesaverde.village.Utilities;
import com.mesaverde.exchange.BRNTradePartner;
import com.mesaverde.exchange.RBRNExchangeNetwork;
import com.mesaverde.groups.BeyondHooperAgent;


//This provides a means to remove those agents that are in groups that have previously attacked us from our trade list.
//First the aggressor groups are tagged as aggressors centrally in the group
//These are tagged as "aggressors" in BeyondMergeFight
//Agents, before exchanging, check the group of their exchange partners
//Then they remove those partners who are in aggressor groups from their exchange lists

public class BeyondExchange extends RBRNExchangeNetwork{

	protected BeyondExchange(BeyondHooperAgent agent) {
		super(agent);
	}
	
    @Override
    public void clearInformation() {
//    	System.out.printf("Info cleared!");
        tradePartners.clear();
        bannedList.clear();
    }


	/** Adds neighbours to our trade partners, just to have people to ask */
	@SuppressWarnings("unused")
	private void fillList(HashSet<BRNTradePartner> tradePartners) {
		// now add new neighbours
		
//		System.out.printf("Why HELLO!");

		ArrayList<Agent> neighbours = Utilities.findAgentsInRange((Agent)agent);
		for (Agent a : neighbours) {

			if (tradePartners.size() == MAX_TRADE_PARTNERS)
				break;

			if (tradePartners.size() == MAX_TRADE_PARTNERS)
				break;

			BRNTradePartner result = searchTradePartnersFor(a);

			if (result == null) {
				result = new BRNTradePartner(a);
				tradePartners.add(result);
			}
			
			BeyondGroup myGroup = ((BeyondHooperAgent) agent).getGroup();
			BeyondGroup theirGroup = ((BeyondHooperAgent) result.getAgent()).getGroup();
			
			if (myGroup.getAggressors().contains(theirGroup)){
				tradePartners.remove(result);
			}
		}
	}
	
	public int requestMaize(int amount) {
//		System.out.printf("Why HELLO THERE!");
		return super.request(Maize.class, amount);
	}
	
	public int requestProtein(int amount) {
		return super.request(Meat.class, amount);
	}

	int requestProteinPayBack(Agent agent, int amount) {
		return agent.exportProtein(amount);
	}
}

