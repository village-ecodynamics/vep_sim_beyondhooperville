package com.mesaverde.exchange;

import com.mesaverde.specialization.resources.Maize;
import com.mesaverde.specialization.resources.Meat;
import com.mesaverde.village.Agent;
import java.util.ArrayList;
import java.util.HashSet;

public class BRNExchangeNetwork extends RBRNExchangeNetwork {
	public BRNExchangeNetwork(Agent agent) {
		super(agent);
	}

	public int exportFood(int maize_requested, int agTag) {
		Agent agentGettingFood = agent.searchAgentList(agTag);
		int result = 0;

		if (agentGettingFood != null)
			result = super.export(Maize.class, maize_requested, agentGettingFood);

		return result;
	}

	public int exportFoodProtein(Agent agent, int protein_requested, int agTag) {
		Agent agentGettingFood = agent.searchAgentList(agTag);
		int result = 0;

		if (agentGettingFood != null)
			result = super.export(Meat.class, protein_requested, agentGettingFood);

		return result;
	}

	public int requestMaize(int amount) {
		return super.request(Maize.class, amount);
	}

	public int requestProtein(int amount) {
		return super.request(Meat.class, amount);
	}

	int requestProteinPayBack(Agent agent, int amount) {
		return agent.exportProtein(amount);
	}
    
   public void setTradePartners(ArrayList<BRNTradePartner> newTradePartners) {
        tradePartners.clear();
        tradePartners.addAll(newTradePartners);
    }

   public void setTradePartner(BRNTradePartner newTradePartner) {
        tradePartners.add(newTradePartner);
   }

   public HashSet<BRNTradePartner> getTradePartners() {
        return tradePartners;
    }
}
