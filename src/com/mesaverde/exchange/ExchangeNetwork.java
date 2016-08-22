package com.mesaverde.exchange;

import com.mesaverde.village.Agent;

public abstract class ExchangeNetwork {
	protected int totalMaizeExchanged = 0;
	protected int successfulExchanges = 0; // maize
	protected int exchangeRequests = 0; // counts not kg
	protected int maizeRequests = 0; // in KG
	protected int maizeExchanged = 0; // in KG
	protected int maizeWasted = 0; // Amount of maize exchanged and the agent
									// dies anyway at the end of the year
	protected int maizePaidBack = 0; // in Kg; amount of maize retrieved through
										// the calling of debts
	protected int maizePaybacks = 0; // counts of debt calls
	protected int proteinSuccessfullyExchanged = 0;
	protected int proteinExchangeRequests = 0;
	protected int proteinRequests = 0;
	protected int proteinExchanged = 0;
	protected int proteinWasted = 0; // Amount of protein exchanged and the
										// agent dies anyway at the end of the
										// year
	protected int proteinPaidBack = 0;
	protected int proteinPaybacks = 0;
	protected int totalProteinExchanged = 0;
	protected Agent agent;

	protected ExchangeNetwork(Agent agent) {
		this.agent = agent;
	}

	public int getExchangeRequests() {
		return exchangeRequests;
	}

	public int getMaizeExchanged() {
		return maizeExchanged;
	}

	public int getMaizePaidBack() {
		return maizePaidBack;
	}

	public int getMaizePaybacks() {
		return maizePaybacks;
	}

	public int getMaizeRequests() {
		return maizeRequests;
	}

	public int getMaizeWasted() {
		return maizeWasted;
	}

	/**
	 * @return the proteinExchanged
	 */
	public int getProteinExchanged() {
		return proteinExchanged;
	}

	/**
	 * @return the proteinExchangeRequests
	 */
	public int getProteinExchangeRequests() {
		return proteinExchangeRequests;
	}

	/**
	 * @return the proteinPaidBack
	 */
	public int getProteinPaidBack() {
		return proteinPaidBack;
	}

	/**
	 * @return the proteinPaybacks
	 */
	public int getProteinPaybacks() {
		return proteinPaybacks;
	}

	/**
	 * @return the proteinRequests
	 */
	public int getProteinRequests() {
		return proteinRequests;
	}

	/**
	 * @return the proteinSuccessfullyExchanged
	 */
	public int getProteinSuccessfullyExchanged() {
		return proteinSuccessfullyExchanged;
	}

	/**
	 * @return the proteinWasted
	 */
	public int getProteinWasted() {
		return proteinWasted;
	}

	public int getSuccessfulExchanges() {
		return successfulExchanges;
	}

	public int getTotalMaizeExchanged() {
		return totalMaizeExchanged;
	}

	public int getTotalProteinExchanged() {
		return totalProteinExchanged;
	}

	/** Resets all the variables that are measured yearly */
	public void reset() {
		totalMaizeExchanged = 0;
		successfulExchanges = 0; // maize
		exchangeRequests = 0; // counts not kg
		maizeRequests = 0; // in KG
		maizeExchanged = 0; // in KG
		maizeWasted = 0; // Amount of maize exchanged and the agent dies anyway
							// at the end of the year
		maizePaidBack = 0; // in Kg; amount of maize retrieved through the
							// calling of debts
		maizePaybacks = 0; // counts of debt calls
		proteinSuccessfullyExchanged = 0;
		proteinExchangeRequests = 0;
		proteinRequests = 0;
		proteinExchanged = 0;
		proteinWasted = 0; // Amount of protein exchanged and the agent dies
							// anyway at the end of the year
		proteinPaidBack = 0;
		proteinPaybacks = 0;
		totalProteinExchanged = 0;
	}

	protected void setTotalMaizeExchanged(int totalMaizeExchanged) {
		this.totalMaizeExchanged = totalMaizeExchanged;
	}

	protected void setTotalProteinExchanged(int totalProteinExchanged) {
		this.totalProteinExchanged = totalProteinExchanged;
	}

    /** Clears any lists that we have stored */
    public void clearInformation() {}
}
