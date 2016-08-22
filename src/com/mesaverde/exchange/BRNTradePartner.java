package com.mesaverde.exchange;

import java.util.HashMap;

import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.village.Agent;
// import com.mesaverde.groups.BeyondGroup;

public class BRNTradePartner {
//	private static final Agent AGGRESSIVE_MEMBER = null;
	/* The number of refusals to repay before we add them to our banned list */
    public static int DEFAULT_COUNT_THRESHOLD = 3;
    public static double DEFAULT_QUALITY = 0.1;
    
    /* The amount of times they can frustrate us */
//    public static int FRUSTRATOR_THRESHOLD = 3;

	//private Agent agentRepresented;	
	private HashMap<Class<? extends Resource>, Double> debts;
	private double quality = DEFAULT_QUALITY; // DC: having quality start at some value, allows all agents to have a chance
    private Agent agentRepresented;
    private int defaultCount = 0;

    /** only want package access. To create an instance, use the createTradePartner
     * method in RBRNExchangeNetwork.
     * @param agent - the agent that should be a trade partner.
     */
	public BRNTradePartner(Agent agent) {
        this.agentRepresented = agent;
	}

    public int getTag() {
        return agentRepresented.getTag();
    }

	public boolean owesResource(Class<? extends Resource> resourceType) {
		return getBalance(resourceType) > 0;
	}

	public void recordLoan(Class<? extends Resource> resourceType, int amtGranted) {
		Double current = getBalance(resourceType);

		getDebts().put(resourceType, current + amtGranted);
	}

	public Agent getAgent() {
		return agentRepresented;
	}

	public void lowerQuality(double amount) {
		quality -= amount;
	}

	public void raiseQuality(double amount) {
		quality += amount;
	}

	public void recordRepayment(Class<? extends Resource> resourceType, int payback) {
		Double current = getBalance(resourceType);

		getDebts().put(resourceType, current - payback);
	}

	public Double getBalance(Class<? extends Resource> resourceType) {
		Double current = getDebts().get(resourceType);

		if (current == null)
			current = 0.0;

		return current;
	}

	public double getQuality() {
        return quality;
	}

    /**
     * @return the debts
     */
    public HashMap<Class<? extends Resource>, Double> getDebts() {
        if (debts == null)
            debts = new HashMap<Class<? extends Resource>, Double>();
        
        return debts;
    }

    void incDefaultCount() {
        defaultCount++;
    }

    /** have we hit or surpassed the default threshold */
    boolean isBadDebtor() {
        return defaultCount >= DEFAULT_COUNT_THRESHOLD;
    }

    void resetDefaultCount() {
        defaultCount = 0;
    }

    public boolean equals(BRNTradePartner other) {
        return agentRepresented == other.getAgent();
    }

}
