package com.mesaverde.exchange;

import com.mesaverde.village.Agent;
import com.mesaverde.village.Village;

public class DonationExchangeNetwork extends ExchangeNetwork {
	public DonationExchangeNetwork(Agent agent) {
		super(agent);
	}

	public int donateMaize(int maize_togiveaway) {
		// id recepientAgent[Village.PHIL_N];
		Agent coopAgent = null;

		int maize_donated = 0;

		int whotopick;
		int whotopickID;
		double distance;

		int phil_n_count = 0;
		int phil_done = 0;
		int valid_donation_rule = 0;

		do {
			whotopick = Village.uniformIntRand(1, 2 + agent.getChildHHCount()
					+ agent.getRelativeHHCount()) - 1;

			if (whotopick == -1) {
				if (Village.COOP_DEBUG) {
					System.out.printf(
							"Agent %d can't find an agent to donate to.\n",
							agent.getTag());
				}
				whotopickID = -1;
			} else if (whotopick == 0) // ParentA
			{
				whotopickID = agent.getParentHHTagA();
			} else if (whotopick == 1) // ParentB
			{
				whotopickID = agent.getParentHHTagB();
			} else if (whotopick > 1 && whotopick < 2 + agent.getChildHHCount()) // Child
			{
				whotopickID = agent.getChildHHTag()[whotopick - 2];
			} else // ask a relative
			{
				whotopickID = agent.getRelativeHHTag()[whotopick - 2
				                                  - agent.getChildHHCount()];
			}

			if (whotopickID != -1) {
				coopAgent = agent.searchAgentList(whotopickID);

				if (coopAgent == null) {
                    phil_n_count++;
					continue;
                }

				// Check distance: cooperate only if within Village.PHIL_RADIUS
				distance = Math.sqrt(Math.pow(
						(agent.getX() - coopAgent.getX()), 2)
						+ Math.pow((agent.getY() - coopAgent.getY()), 2));

				if ((coopAgent.getCoopState() & Village.DONATE_TO_MAIZE()) != 0) {
					valid_donation_rule = 1;
				} else {
					valid_donation_rule = 0;
				}

				if (Village.COOP_DEBUG) {
					System.out
					.printf(
							"Agent %d found agent %d to cooperate and is %.4f pixels away.\n",
							agent.getTag(), whotopickID, distance);
				}
			} else {
				coopAgent = null;
				distance = 9999;
				if (Village.COOP_DEBUG) {
					System.out.printf(
							"Agent %d can't find an agent to cooperate\n",
							agent.getTag());
				}
			}

			if (coopAgent != null && distance <= Village.PHIL_RADIUS
					&& valid_donation_rule != 0) {
				maize_donated = coopAgent.importFood(maize_togiveaway);
				phil_done = 1;

				agent.setDonater(agent.getDonater() + 1);
				agent.setMaize_given(agent.getMaize_given() + maize_donated);

				if(Village.OUTPUT_TRADE){
					if (maize_donated > 0){
						agent.getMySwarm().xmlwriter.recordTrade(agent.getTag(), agent.getX(), agent.getY(), "donation", "donated", "maize", coopAgent.getTag(), maize_donated);
					}
				}

				
				if (Village.COOP_DEBUG) {
					System.out
					.printf(
							"Agent %d now has %d Kg of free maize available, it donated %d.\n",
							agent.getTag(), maize_togiveaway,
							maize_donated);
				}
			}
			++phil_n_count;
		} while (phil_n_count < Village.PHIL_N && phil_done != 1);

		if(Village.OUTPUT_TRADE){
			if(maize_donated > 0){	
				agent.getMySwarm().xmlwriter.flushToDisk("Trades", agent.getMySwarm().getTime());
			}
		}
		return maize_donated;
	}

	public int donateProtein(int protein_togiveaway) {
		Agent coopAgent = null;

		int protein_donated = 0;

		int whotopick;
		int whotopickID;
		double distance;

		int phil_n_count = 0;
		int phil_done = 0;
		int valid_donation_rule = 0;

		do {
			whotopick = Village.uniformIntRand(1, 2 + agent.getChildHHCount()
					+ agent.getRelativeHHCount()) - 1;

			if (whotopick == -1) {
				if (Village.COOP_DEBUG) {
					System.out.printf(
							"Agent %d can't find an agent to donate to.\n",
							agent.getTag());
				}
				whotopickID = -1;
			} else if (whotopick == 0) // ParentA
			{
				whotopickID = agent.getParentHHTagA();
			} else if (whotopick == 1) // ParentB
			{
				whotopickID = agent.getParentHHTagB();
			} else if (whotopick > 1 && whotopick < 2 + agent.getChildHHCount()) // Child
			{
				whotopickID = agent.getChildHHTag()[whotopick - 2];
			} else // ask a relative
			{
				whotopickID = agent.getRelativeHHTag()[whotopick - 2
				                                  - agent.getChildHHCount()];
			}

			if (whotopickID != -1) {
				coopAgent = agent.searchAgentList(whotopickID);
				if (coopAgent == null || coopAgent.equals(agent)) {
                    phil_n_count++; // to make sure this doesn't happen forever
					continue;
				}

				// Check distance: cooperate only if within Village.PHIL_RADIUS
				distance = Math.sqrt(Math.pow(
						(agent.getX() - coopAgent.getX()), 2)
						+ Math.pow((agent.getY() - coopAgent.getY()), 2));

				if ((coopAgent.getProteinCoopState() & Village.DONATE_TO_PRO()) != 0) {
					valid_donation_rule = 1;
				} else {
					valid_donation_rule = 0;
				}

				if (Village.COOP_DEBUG) {
					System.out
					.printf(
							"Agent %d found agent %d to cooperate and is %.4f pixels away.\n",
							agent.getTag(), whotopickID, distance);
				}
			} else {
				coopAgent = null;
				distance = 9999;
				if (Village.COOP_DEBUG) {
					System.out.printf(
							"Agent %d can't find an agent to cooperate\n",
							agent.getTag());
				}
			}

			if (coopAgent != null && distance <= Village.PHIL_RADIUS
					&& valid_donation_rule != 0) {
				protein_donated = coopAgent
				.importFoodProtein(protein_togiveaway);
				phil_done = 1;

				agent.setPro_donater(agent.getPro_donater() + 1);
				agent.setProtein_given(agent.getProtein_given() + protein_donated);

				if(Village.OUTPUT_TRADE){
					if (protein_donated > 0){
						agent.getMySwarm().xmlwriter.recordTrade(agent.getTag(), agent.getX(), agent.getY(), "donation", "donated", "meat", coopAgent.getTag(), protein_donated);
					}
				}

				
				if (Village.COOP_DEBUG) {
					System.out
					.printf(
							"Agent %d now has %d Kg of free protein available, it donated %d.\n",
							agent.getTag(), protein_togiveaway,
							protein_donated);
				}
			}
			++phil_n_count;
		} while (phil_n_count < Village.PHIL_N && phil_done != 1);

		if(Village.OUTPUT_TRADE){
			if(protein_donated > 0){	
				agent.getMySwarm().xmlwriter.flushToDisk("Trades", agent.getMySwarm().getTime());
			}
		}
		
		return protein_donated;
	}

}
