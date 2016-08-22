package com.mesaverde.exchange;

import com.mesaverde.village.Agent;
import com.mesaverde.village.Utilities;
import com.mesaverde.village.Village;

public class GRNExchangeNetwork extends ExchangeNetwork {
	
	public GRNExchangeNetwork(Agent agent) {
		super(agent);
	}

	void filterWhoToAsk(double[] OldWhoToAskProb) {
		int whotoask, whotoaskID;
		Agent coopAgent;
		double distance;

		for (whotoask = 0; whotoask < 2 + agent.getChildHHCount()
		+ Village.MAX_RELATIVE_LINKS; whotoask++) {
			whotoaskID = -1;
			OldWhoToAskProb[whotoask] = agent.getWhoToAskProb()[whotoask]; // copy
			// here

			// get ID
			if (whotoask == 0) // ParentA
			{
				whotoaskID = agent.getParentHHTagA();
			} else if (whotoask == 1) // ParentB
			{
				whotoaskID = agent.getParentHHTagB();
			} else if (whotoask > 1 && whotoask < 2 + agent.getChildHHCount()) // Child
			{
				whotoaskID = agent.getChildHHTag()[whotoask - 2];
			} else // ask a relative
			{
				whotoaskID = agent.getRelativeHHTag()[whotoask - 2
				                                 - agent.getChildHHCount()];
			}

			// find the agent
			if (whotoaskID != -1) {
				coopAgent = agent.searchAgentList(whotoaskID);

				if (coopAgent == null)
					continue;

				// Check distance: cooperate only if within
				// Village.MAX_COOP_RADIUS
				distance = Math.sqrt(Math.pow(
						(agent.getX() - coopAgent.getX()), 2)
						+ Math.pow((agent.getY() - coopAgent.getY()), 2));

				// 0 probability value if too far away
				if (distance > Village.MAX_COOP_RADIUS_GRN) {
					agent.getWhoToAskProb()[whotoask] = 0;
				} else if (agent.getWhoToAskProb()[whotoask] == 0) {
					agent.getWhoToAskProb()[whotoask] = 0.1;
				}
			}
		}

		return;
	}

	public int requestMaize(int maize_deficit) {
		Agent coopAgent = null;

		int maize_needed = maize_deficit < 1 ? maize_deficit * -1
				: maize_deficit; // * -1;
		int maize_retrieved = 0;
		int KeepLooking = 1;
		int whotoask; // index in Links Array
		int whotoaskID; // Agent ID
		double sum, pick = 0;
		double distance = 0;
		int i;
		double coop_rate_value;
		double[] OldWhoToAskProb = new double[Village.MAX_RELATIVE_LINKS
		                                      + Village.MAX_CHILD_LINKS + 2];

		int valid_request_rule = 0;

		agent.setExchange_requests(agent.getExchange_requests() + 1);
		agent.setMaize_requests(agent.getMaize_requests() + maize_deficit);

		filterWhoToAsk(OldWhoToAskProb);
		if (Village.COOP_DEBUG) {
			System.out
			.printf(
					"Agent %d is starving, looking for cooperation. needs %d kg of corn\n",
					agent.getTag(), maize_deficit);
		}
		while (KeepLooking != 0) {
			int amount_exchanged = 0;
			if (Village.COOP_LEARNING == 1) {
				for (sum = 0, i = 0; i < Village.MAX_RELATIVE_LINKS
				+ Village.MAX_CHILD_LINKS + 2; i++) {
					sum += agent.getWhoToAskProb()[i];
				}

				pick = Village.uniformDblRand(0.0, sum);

				sum = agent.getWhoToAskProb()[0];
				whotoask = 0;
				while (pick > sum) {
					whotoask++;
					sum += agent.getWhoToAskProb()[whotoask];
				}

				// if sum == 0, then we have no one to choose from
				if (sum == 0) {
					whotoask = -1;
				}

			} else if (Village.COOP_LEARNING == 2) // enable memory instead of
				// just roulette
			{
				if (agent.getMemoryInteractionCount() == 0) // no memory, do roulette
				{
					for (sum = 0, i = 0; i < Village.MAX_RELATIVE_LINKS
					+ Village.MAX_CHILD_LINKS + 2; i++) {
						sum += agent.getWhoToAskProb()[i];
					}
					pick = Village.uniformDblRand(0.0, sum);
					sum = agent.getWhoToAskProb()[0];
					whotoask = 0;
					while (pick > sum) {
						whotoask++;
						sum += agent.getWhoToAskProb()[whotoask];
					}
				} else // use memory
				{
					whotoask = agent.getMemoryInteraction_Index()[0]; // just ask the
					// first one
					// in memory
					// for now,
					// assuming
					// only one.
				}

			} else {
				whotoask = Village.uniformIntRand(1, 2 + agent.getChildHHCount()
						+ agent.getRelativeHHCount()) - 1;
			}

			if (whotoask == -1) {
				if (Village.COOP_DEBUG) {
					System.out.printf(
							"Agent %d can't find an agent to cooperate\n",
							agent.getTag());
				}
				whotoaskID = -1;
			} else if (whotoask == 0) // ParentA
			{
				whotoaskID = agent.getParentHHTagA();
			} else if (whotoask == 1) // ParentB
			{
				whotoaskID = agent.getParentHHTagB();
			} else if (whotoask > 1 && whotoask < 2 + agent.getChildHHCount()) // Child
			{
				whotoaskID = agent.getChildHHTag()[whotoask - 2];
			} else // ask a relative
			{
				whotoaskID = agent.getRelativeHHTag()[Math.min(whotoask - 2
						- agent.getChildHHCount(), agent.getRelativeHHTag().length - 1)];
			}

			if (whotoaskID != -1) {
				coopAgent = agent.searchAgentList(whotoaskID);

				if (coopAgent == null || coopAgent.equals(agent)) // ensure we're not asking
					// ourselves
				{
					coopAgent = null;
				} else {
					// Check distance: cooperate only if within
					// Village.MAX_COOP_RADIUS
					distance = Math.sqrt(Math.pow((agent.getX() - coopAgent
							.getX()), 2)
							+ Math.pow((agent.getY() - coopAgent.getY()), 2));

					if ((coopAgent.getCoopState() & Village
							.REQUEST_FROM_MAIZE()) != 0) {
						valid_request_rule = 1;
					} else {
						valid_request_rule = 0;
					}

					if (Village.COOP_DEBUG) {
						System.out
						.printf(
								"Agent %d found agent %d to cooperate and is %.4f pixels away.\n",
								agent.getTag(), whotoaskID, distance);
					}
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

			if (coopAgent != null && distance <= Village.MAX_COOP_RADIUS_GRN
					&& valid_request_rule != 0) {

				// Check the rate that the other one will actually cooperate
				coop_rate_value = Village.uniformDblRand(0.0, 1.0);
				if (coop_rate_value > agent.getMySwarm().getCoop_rate()) {
					if (Village.COOP_DEBUG) {
						System.out
						.printf(
								"Agent's odds to cooperate Village.FAILED rate value: %.3f\n",
								coop_rate_value);
					}
					if (Village.COOP_LEARNING == 1) {
						OldWhoToAskProb[whotoask] -= 0.1;
						if (OldWhoToAskProb[whotoask] < 0) {
							OldWhoToAskProb[whotoask] = 0;
						}
					}

					return 0;
				} else {
					if (Village.COOP_DEBUG) {
						System.out
						.printf(
								"Agent's odds to cooperate Village.SUCCEEDED rate value: %.3f\n",
								coop_rate_value);
						
						int val = 0;	
						if ((val = whotoask) > 0 && coopAgent.get(whotoask)!=null) {
							
						}
						
						Object dist = Utilities.distance(agent, coopAgent.get(whotoask));
						sum += (1 - distance/Village.MAX_COOP_RADIUS_GRN) * val;
					}
				}

				// Village.EXCHANGE
				agent.setExchange_count(agent.getExchange_count() + 1);
				agent.setMaize_coop_count(agent.getMaize_coop_count() + 1);
				
				amount_exchanged += coopAgent.exportMaize(maize_needed);
				maize_retrieved += amount_exchanged;
				Utilities.copyArray(OldWhoToAskProb, agent.getWhoToAskProb(),
						Village.MAX_RELATIVE_LINKS + Village.MAX_CHILD_LINKS
						+ 2);
				
				double travelDistance = Utilities.distance(agent, coopAgent);
				
				if (Village.COOP_DEBUG) {
					System.out.printf(
							"Agent %d now has %d Kg of maize, it needed %d.\n",
							agent.getTag(), maize_retrieved, maize_needed);
				}
				if (Village.COOP_LEARNING == 1 || Village.COOP_LEARNING == 2) {
					if (amount_exchanged > 0 && maize_needed > 0)// checks div.by
						// zero
					{
						agent.getWhoToAskProb()[whotoask] += amount_exchanged
						/ maize_needed; // positive reinforcement

						if (Village.COOP_LEARNING == 2) {
							// update memory
							agent.getMemoryInteraction_ID()[0] = whotoaskID;
							agent.getMemoryInteraction_Index()[0] = whotoask;
							agent.getMemoryInteraction_X()[0] = coopAgent.getX();
							agent.getMemoryInteraction_Y()[0] = coopAgent.getY();
							agent.setMemoryInteractionCount(1);
						}
					}
				}
			} else {
				if (whotoask != -1
						&& (Village.COOP_LEARNING == 1 || Village.COOP_LEARNING == 2)) {
					agent.getWhoToAskProb()[whotoask] -= 0.1;
					if (agent.getWhoToAskProb()[whotoask] < 0) {
						agent.getWhoToAskProb()[whotoask] = 0.1; // use 0.1 instead
						// of 0 to allow
						// this to be picked
						// again
					}
					agent.setMemoryInteractionCount(0);
				}

			}

			if (maize_retrieved <= maize_needed
					|| agent.getMaize_coop_count() >= Village.COOP_ATTEMPTS) {
				KeepLooking = 0;
			}
			
			if(Village.OUTPUT_TRADE){
				if (amount_exchanged > 0){
					agent.getMySwarm().xmlwriter.recordTrade(agent.getTag(), agent.getX(), agent.getY(), "grn", "recieved", "maize", coopAgent.getTag(), amount_exchanged);
				}
			}

		}

		agent.setMaize_exchanged(agent.getMaize_exchanged() + maize_retrieved);
		agent.setTotalMaizeExchanged(agent.getTotalMaizeExchanged() + maize_retrieved);

		if (Village.COOP_DEBUG) {
			System.out.printf("%d\t%d\n", maize_retrieved, maize_deficit);
		}
		if (maize_retrieved > 0) {
			if (Village.COOP_DEBUG) {
				System.out.printf("received required amount\n");
			}
			agent.setSuccessful_exchange(agent.getSuccessful_exchange() + 1);
		} else {
			agent.setMaize_wasted(agent.getMaize_wasted() + agent.getTotalMaizeExchanged());
		}

		if(Village.OUTPUT_TRADE){
			if(maize_retrieved > 0){	
				agent.getMySwarm().xmlwriter.flushToDisk("Trades", agent.getMySwarm().getTime());
			}
		}
		return maize_retrieved;

	}

	public int requestProtein(int protein_deficit) {
		Agent coopAgent = null;

		int protein_needed = protein_deficit;
		int protein_retrieved = 0;
		int KeepLooking = 1;
		int whotoask; // index in Links Array
		int whotoaskID; // Agent ID
		double sum, pick = 0;
		double distance = -1;
		int i;
		double coop_rate_value;
		double[] OldWhoToAskProb = new double[Village.MAX_RELATIVE_LINKS
		                                      + Village.MAX_CHILD_LINKS + 2];

		int valid_request_rule = 0;

		filterWhoToAsk(OldWhoToAskProb);
		if (Village.COOP_DEBUG) {
			System.out.printf(
					"Agent %d is starving, looking for cooperation\n", agent
					.getTag());
		}
		while (KeepLooking != 0) {
			int amount_exchanged = 0;
			if (Village.COOP_LEARNING == 1) // old roulette way
			{
				for (sum = 0, i = 0; i < Village.MAX_RELATIVE_LINKS
				+ Village.MAX_CHILD_LINKS + 2; i++) {
					sum += agent.getWhoToAskProb()[i];
				}

				pick = Village.uniformDblRand(0.0, sum);
				
//				System.out.println("pick: " + pick + "\n" + "agent.getProtein_coop_count(): " + agent.getProtein_coop_count() + "\n");
				
				sum = agent.getWhoToAskProb()[0];
				whotoask = 0;
				while (pick > sum) {
					whotoask++;
					sum += agent.getWhoToAskProb()[whotoask];
				}

				if (sum == 0) {
					whotoask = -1;
				}
			} else if (Village.COOP_LEARNING == 2) // enable memory instead of
				// just roulette
			{
				if (agent.getMemoryInteractionCount() == 0) // no memory, do roulette
				{
					for (sum = 0, i = 0; i < Village.MAX_RELATIVE_LINKS
					+ Village.MAX_CHILD_LINKS + 2; i++) {
						sum += agent.getWhoToAskProb()[i];
					}
					pick = Village.uniformDblRand(0.0, sum);
					sum = agent.getWhoToAskProb()[0];
					whotoask = 0;
					while (pick > sum) {
						whotoask++;
						sum += agent.getWhoToAskProb()[whotoask];
					}
				} else // use memory
				{
					whotoask = agent.getMemoryInteraction_Index()[0]; // just ask the
					// first one
					// in memory
					// for now,
					// assuming
					// only one.
				}

			} else {
				whotoask = Village.uniformIntRand(1, 2 + agent.getChildHHCount()
						+ agent.getRelativeHHCount()) - 1;
			}

			if (whotoask == -1) {
				if (Village.COOP_DEBUG) {
					System.out
					.printf(
							"Agent %d can't find an agent to cooperate as whotoask == -1\n",
							agent.getTag());
				}
				whotoaskID = -1;
			} else if (whotoask == 0) // ParentA
			{
				whotoaskID = agent.getParentHHTagA();
			} else if (whotoask == 1) // ParentB
			{
				whotoaskID = agent.getParentHHTagB();
			} else if (whotoask > 1 && whotoask < 2 + agent.getChildHHCount()) // Child
			{
				whotoaskID = agent.getChildHHTag()[whotoask - 2];
			} else // ask a relative
			{
				whotoaskID = agent.getRelativeHHTag()[Math.min(whotoask - 2
						- agent.getChildHHCount(), agent.getRelativeHHTag().length - 1)];
			}

			if (whotoaskID != -1) {
				coopAgent = agent.searchAgentList(whotoaskID);

				if (coopAgent == null || coopAgent.equals(agent)) {
					coopAgent = null;
				} else {
					distance = Math.sqrt(Math.pow((agent.getX() - coopAgent
							.getX()), 2)
							+ Math.pow((agent.getY() - coopAgent.getY()), 2));

					if ((coopAgent.getProteinCoopState() & Village
							.REQUEST_FROM_PRO()) != 0) {
						valid_request_rule = 1;
					} else {
						valid_request_rule = 0;
					}

					proteinExchangeRequests++;
					proteinRequests += Math.min(protein_needed,
							Village.MAX_PROTEIN_EXCHANGE_AMOUNT);

					if (Village.COOP_DEBUG) {
						System.out
						.printf(
								"Agent %d found agent %d to cooperate and is %.4f pixels away.\n",
								agent.getTag(), whotoaskID, distance);
					}
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

			if (coopAgent != null && distance <= Village.MAX_COOP_RADIUS_GRN
					&& valid_request_rule != 0) {

				// Check the rate that the other one will actually cooperate
				coop_rate_value = Village.uniformDblRand(0.0, 1.0);
				if (coop_rate_value > agent.getMySwarm().getCoop_rate()
						|| agent.getDefectingAgentFlag() != 0
						&& (Village.DEFECTOR_TYPE == 1 || Village.DEFECTOR_TYPE == 3)) {
					if (Village.COOP_DEBUG) {
						System.out
						.printf(
								"Agent's odds to cooperate Village.FAILED rate value: %.3f\n",
								coop_rate_value);
					}
					if (Village.COOP_LEARNING == 1) {
						OldWhoToAskProb[whotoask] -= 0.1;
						if (OldWhoToAskProb[whotoask] < 0) {
							OldWhoToAskProb[whotoask] = 0;
						}
					}
					System.out.println("returning zero!");
					return 0;
				} else {
					if (Village.COOP_DEBUG) {
						System.out
						.printf(
								"Agent's odds to cooperate Village.SUCCEEDED rate value: %.3f\n",
								coop_rate_value);
					}
				}

				// Village.EXCHANGE

				agent.setProtein_coop_count(agent.getProtein_coop_count() + 1);
				amount_exchanged += coopAgent.exportProtein(protein_needed);
				protein_retrieved += amount_exchanged;

				Utilities.copyArray(OldWhoToAskProb, agent.getWhoToAskProb(),
						Village.MAX_RELATIVE_LINKS + Village.MAX_CHILD_LINKS
						+ 2);

				if (Village.COOP_DEBUG) {
					System.out
					.printf(
							"Agent %d now has %d g of protein, it needed %d, and had %d.\n",
							agent.getTag(), protein_retrieved,
							protein_needed,
							agent.getCurrentProteinStorage());
				}
				if (Village.COOP_LEARNING == 1 || Village.COOP_LEARNING == 2) {
					if (amount_exchanged > 0 && protein_needed > 0)// checks
						// div. by
						// zero
					{
						agent.getWhoToAskProb()[whotoask] += amount_exchanged
						/ protein_needed; // positive reinforcement

						if (Village.COOP_LEARNING == 2) {
							// update memory
							agent.getMemoryInteraction_ID()[0] = whotoaskID;
							agent.getMemoryInteraction_Index()[0] = whotoask;
							agent.getMemoryInteraction_X()[0] = coopAgent.getX();
							agent.getMemoryInteraction_Y()[0] = coopAgent.getY();
							agent.setMemoryInteractionCount(1);
						}
					}
				}
			} else {
				if (Village.COOP_LEARNING == 1 || Village.COOP_LEARNING == 2) {
					if (whotoask != -1) {
						agent.getWhoToAskProb()[whotoask] -= 0.1;
						if (agent.getWhoToAskProb()[whotoask] < 0) {
							agent.getWhoToAskProb()[whotoask] = 0.1; // use 0.1
							// instead of 0
							// to allow this
							// to be picked
							// again
						}
					}

					// remove from memory
					agent.setMemoryInteractionCount(0);
				}

			}

			if (protein_retrieved <= protein_needed
					|| agent.getProtein_coop_count() >= Village.COOP_ATTEMPTS || whotoask == -1) {
				KeepLooking = 0;
			}
			
			if(Village.OUTPUT_TRADE){
				if (amount_exchanged > 0){
					agent.getMySwarm().xmlwriter.recordTrade(agent.getTag(), agent.getX(), agent.getY(), "grn", "recieved", "meat", coopAgent.getTag(), amount_exchanged);
				}
			}

		}
		
		proteinExchanged += protein_retrieved;
		totalProteinExchanged += protein_retrieved;

		if (protein_retrieved > 0) {
			if (Village.COOP_DEBUG) {
				System.out.printf("received required amount\n");
			}
			proteinSuccessfullyExchanged += 1;
		} else {
			proteinWasted += totalProteinExchanged;
		}

		if(Village.OUTPUT_TRADE){
			if(protein_retrieved > 0){	
				agent.getMySwarm().xmlwriter.flushToDisk("Trades", agent.getMySwarm().getTime());
			}
		}

		return protein_retrieved;
	}

	public void saveLinks() {
		if (Village.SAVE_LINKS) {
			/* default String concatenation (+) is still pretty slow in Java.
			 * I'm therefore changing the code to use StringBuilder.
			 * This is much faster.
			 */
			StringBuilder build = new StringBuilder(1000); // 1000 character initial capacity
			int i;
			
			build.append(agent.getTag());
			build.append(" ");
			build.append(agent.getX());
			build.append(" ");
			build.append(agent.getY());
			build.append(" ");
			build.append(agent.getMaizeStorage());
			build.append(" ");
			build.append(agent.getParentHHTagA());
			build.append(" ");
			build.append(agent.getParentHHTagB());

			for (i = 0; i < Village.MAX_CHILD_LINKS; i++) {
				build.append(" ");
				build.append(agent.getChildHHTag()[i]);				
			}

			for (i = 0; i < Village.MAX_RELATIVE_LINKS; i++) {
				build.append(" ");
				build.append(agent.getRelativeHHTag()[i]);
			}

			agent.getMySwarm().log(build.toString(), "links", true);
		}
	}
}
