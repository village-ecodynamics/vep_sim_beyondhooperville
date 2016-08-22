package com.mesaverde.exchange;

import java.util.ArrayList;

import com.mesaverde.groups.BeyondGroup;
import com.mesaverde.groups.BeyondHooperAgent;
import com.mesaverde.specialization.resources.Maize;
import com.mesaverde.specialization.resources.Meat;
import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.village.Agent;
import com.mesaverde.village.Utilities;
import com.mesaverde.village.Village;
import java.util.HashSet;

public class RBRNExchangeNetwork extends ExchangeNetwork {
	protected static final int MAX_TRADE_PARTNERS = Village.MAX_TRADER_LIST_SIZE;

	protected int TListUpdateDead; /** how often to update dead links (for speed)*/	

	protected HashSet<BRNTradePartner> tradePartners = new HashSet<BRNTradePartner>(MAX_TRADE_PARTNERS);
    protected HashSet<Agent> bannedList = new HashSet<Agent>();

	protected RBRNExchangeNetwork(Agent agent) {
		super(agent);
	}


	/** We're trying to borrow a resource from our neighbors */

    private void dingForDefaultOnLoan(BRNTradePartner partner) {
        // they didn't give me what they owed (we use > 1 in case they owe us a fraction)
        partner.lowerQuality(100);
        
        partner.incDefaultCount();
        if (partner.isBadDebtor()) {
            bannedList.add(partner.getAgent());
            tradePartners.remove(partner);
        }
    }
    

    /** We're trying to borrow a resource from our neighbours
     * We first ensure that our list of potential partners is as full as can be
     * adding new neighbours if need be, after we have completed requesting,
     * we trim the list back down to ensure that we only have people who we
     * have some trade relationship with.
     */
    public int request(Class<? extends Resource> resourceType, int amountNeeded) {
        fillList(tradePartners);

        int result = oldRequest(resourceType, amountNeeded);

        // now trim the list back down
        for (BRNTradePartner partner : (HashSet<BRNTradePartner>) tradePartners.clone()) {
            if (haveNoTradeRelationshipWith(partner)) 
            	
            {
                tradePartners.remove(partner);
            }
        }

        return result;
    }

    
	/** We're trying to borrow a resource from our neighbours */

	private int oldRequest(Class<? extends Resource> resourceType, int amountNeeded) {
		BRNTradePartner coopAgent = null;

		int resourceNeeded = amountNeeded < 1 ? amountNeeded * -1
				: amountNeeded;
		int amount_retrieved = 0;

		int KeepLooking = 1;
		int whotoask; /** index in Links Array*/
		double pick = 0;
		double coop_rate_value;		
		// TListQuality
		// to filter for
		// out of range

		int valid_request_rule = 0;

		/**limit the resource need by trade rules*/
		if (resourceType == Maize.class)
			resourceNeeded = Math.min(resourceNeeded, Village.MAX_EXCHANGE_AMOUNT);
		else if (resourceType == Meat.class)
			resourceNeeded = Math.min(resourceNeeded, Village.MAX_PROTEIN_EXCHANGE_AMOUNT);

		//ArrayList<BRNTradePartner> tradePartners = findPartnersInTradeRange();				
		if (Village.BRN_COOP_DEBUG) {
			System.out
			.printf(
			"From -RequestTrade (maize): Agent %d is starving (deficit=%d), looking for cooperation via BRN\n",
			agent.getTag(), resourceNeeded);
		}

        ArrayList<BRNTradePartner> tradePartnersList = new ArrayList<BRNTradePartner>(tradePartners);
		while (KeepLooking != 0) {
			int amount_exchanged = 0;
			if (Village.COOP_LEARNING == 1) {
				// changes to ignore negative values
				/**changes to ignore negative values*/

				/** This is where I implement calculating distance and Quality
				 * First we get Quality, which is a variable taking into account
				 * rapport, based on likelihood of paying back debts.
				 * Then we take the distance, measured in cells.  
				 * Then we take 1 minus the distance divided by the maximum BRN radius (40)
				 * because both distance and Quality need to be in the same scale.
				 * Both Quality and Distance are weighted equally, as per Gregory and Sahlins.
				 */
				double sum = 0;
				for (BRNTradePartner p : tradePartnersList) {
					if (p.getQuality() > 0) {
						if(p.getAgent() != null){
							double dist = Utilities.distance(agent, p.getAgent());
							sum += (1 - dist/Village.MAX_COOP_RADIUS_BRN) * p.getQuality();	
						}
					}
				}

				/** if (sum < 0)
				// sum =0; // just in case*/
				pick = Village.uniformDblRand(0.0, sum);

				/** if sum == 0, then we have no one to choose from*/

				if (sum == 0) {
					whotoask = -1;
				} else {
					// Can't forget to factor in travel cost for first agent
					/**I added to factor in travel cost for first agent*/
					whotoask = 0;                    
					double quality = tradePartnersList.get(0).getQuality();

					// go to the first non-negative value
					/**go to the first non-negative value*/
					while (quality < 0 && (whotoask+1) < tradePartnersList.size()) {
						whotoask++;
						quality = tradePartnersList.get(whotoask).getQuality();
					}
					/**factor in distance and quality
					 * 
					 */
					if(tradePartnersList.get(whotoask).getAgent()!=null){
						double dist = Utilities.distance(agent,
								tradePartnersList.get(whotoask).getAgent());
						sum = (1 - dist/Village.MAX_COOP_RADIUS_BRN) * quality;
						// (TK) print out both components of sum to see how this is working


						while (pick > sum && (whotoask+1) < tradePartnersList.size()) {
							whotoask++;
							
							/**this is where I printed the output to know it's working*/
							// printf("%d of %d\n", whotoask,
							 //TListSize);fflush(stdout);

							/**Following is where they calculate the cost for exchanging 
							 * with each exchange partner
							 */
							double val = 0;
							if ((val = tradePartnersList.get(whotoask).getQuality()) > 0 && tradePartnersList.get(whotoask).getAgent()!=null) {
								
								/** add in caloric cost*/
								
								dist = Utilities.distance(agent, 
										tradePartnersList.get(whotoask).getAgent());
								sum += (1 - dist/Village.MAX_COOP_RADIUS_BRN) * val;

								/**this is also where I printed the output to know it's working*/
								// System.out.printf("Well ok then");
								//Stef added
								//sum +=  val;
							}
						}
					}
				}
			} else {
				whotoask = Village.uniformIntRand(1, tradePartnersList.size()) - 1;
			}

			if (whotoask == -1) {
				if (Village.BRN_COOP_DEBUG) {
					System.out
					.printf(
							"Agent %d can't find an agent to cooperate. Agents TListSize = %d\n",
							agent.getTag(), tradePartnersList.size());
				}
			}

			if (whotoask != -1) {
				coopAgent = tradePartnersList.get(whotoask);

				if (coopAgent == null || coopAgent.getAgent() == null) {
					return 0;
				}

				if (resourceType == Maize.class) {
					if ((coopAgent.getAgent().getCoopState() & Village.REQUEST_FROM_MAIZE()) != 0) {
						valid_request_rule = 1;
					} else {
						valid_request_rule = 0;
					}

					exchangeRequests++;

					maizeRequests += resourceNeeded;
				} else if (resourceType == Meat.class) {
					if ((coopAgent.getAgent().getProteinCoopState() & Village
							.REQUEST_FROM_PRO()) != 0) {
						valid_request_rule = 1;
					} else {
						valid_request_rule = 0;
					}

					// only count the request if we found someone
					proteinExchangeRequests++;
					proteinRequests += resourceNeeded;
				}

				if (Village.BRN_COOP_DEBUG) {
					System.out
					.printf(
							"Agent %d found agent %d to cooperate and is %.4f pixels away.\n",
							agent.getTag(), coopAgent.getAgent().getTag(),
							Utilities.distance(agent, coopAgent.getAgent()));
				}
			} else {
				coopAgent = null;
				if (Village.BRN_COOP_DEBUG) {
					System.out.printf(
							"2nd Agent %d can't find an agent to cooperate\n",
							agent.getTag());
				}
			}			

			if (coopAgent != null && coopAgent.getAgent() != null && valid_request_rule != 0) {
				// Check the rate that the other one will actually cooperate
				coop_rate_value = Village.uniformDblRand(0.0, 1.0);
				
                // we're not willing to cooperate if higher
				if (coop_rate_value > agent.getMySwarm().getCoop_rate()
						|| agent.getDefectingAgentFlag() != 0 && (Village.DEFECTOR_TYPE == 2 || Village.DEFECTOR_TYPE == 3)) {
					if (Village.BRN_COOP_DEBUG) {
						System.out
						.printf(
								"Agent's odds to cooperate FAILED rate value: %.3f\n",
								coop_rate_value);
					}
					if (Village.COOP_LEARNING == 1) {
						BRNTradePartner rec = coopAgent;
						rec.lowerQuality(100);

                        // can't have negative quality, so we set them back to default
						if (rec.getQuality() < 0)
							rec.raiseQuality(rec.getQuality() * -1 + BRNTradePartner.DEFAULT_QUALITY);
					}

					return 0;
				} else {
					if (Village.BRN_COOP_DEBUG) {
						System.out
						.printf(
								"Agent's odds to cooperate Village.SUCCEEDED rate value: %.3f\n",
								coop_rate_value);
					}
				}

				// EXCHANGE
				if (resourceType == Maize.class) {
					agent.setMaize_coop_count(agent.getMaize_coop_count() + 1);
					amount_exchanged += coopAgent.getAgent().getBrnNetwork().exportFood(
							resourceNeeded, agent.getTag());
					amount_retrieved += amount_exchanged;
					coopAgent.recordRepayment(Maize.class, amount_exchanged);
					maizeExchanged += amount_exchanged;
				} else if (resourceType == Meat.class) {
					agent.setProtein_coop_count(agent.getProtein_coop_count() + 1);
					amount_exchanged += coopAgent.getAgent().getBrnNetwork().exportFoodProtein(
							coopAgent.getAgent(), resourceNeeded, agent.getTag());
					amount_retrieved += amount_exchanged;
					coopAgent.recordRepayment(Meat.class, amount_exchanged);
					proteinExchanged += amount_exchanged;
				} //can deduct here. calc cost in calories

				/**This is where I have them actually get charged for the distance traveled
				 * */
				
				double travelDistance = Utilities.distance(agent, coopAgent.getAgent());


				/**this is also where I have it printing out exchanges*/
				//System.out.printf("Making trade, decrementing by %d for a distance of %f\n",
					//agent.calcTravelCal((int)travelDistance, 0, 2), 
						//travelDistance);

				if (Village.BRN_COOP_DEBUG) {
					System.out.printf(
							"Agent %d now has %d Kg of maize, it needed %d.\n",
							agent.getTag(), amount_retrieved, resourceNeeded);
				}
				if (Village.COOP_LEARNING == 1) {
					if (amount_exchanged > 0 && resourceNeeded > 0)// checks div.
						// by zero
					{
						coopAgent.raiseQuality(amount_exchanged / resourceNeeded * 100);
						//  positive
						// reinforcement
					}
				}
			} else {
				if (Village.COOP_LEARNING == 1  && coopAgent != null) {
					BRNTradePartner rec = coopAgent;
					rec.lowerQuality(100);

					if (rec.getQuality() < 0)
						rec.raiseQuality(rec.getQuality() * -1);
				}
			}

			if (resourceType == Maize.class) {
				if (amount_retrieved <= resourceNeeded
						|| agent.getMaize_coop_count() >= Village.COOP_ATTEMPTS) {
					KeepLooking = 0;
				}
			} else if (resourceType == Meat.class) {
				if (amount_retrieved <= resourceNeeded
						|| agent.getProtein_coop_count() >= Village.COOP_ATTEMPTS) {
					KeepLooking = 0;
				}
			}
			if(Village.OUTPUT_TRADE){
				if (amount_exchanged > 0){
					if (resourceType == Maize.class) {
						agent.getMySwarm().xmlwriter.recordTrade(agent.getTag(), agent.getX(), agent.getY(), "brn", "recieved", "maize", coopAgent.getTag(), amount_exchanged);
					} else if (resourceType == Meat.class) {
						agent.getMySwarm().xmlwriter.recordTrade(agent.getTag(), agent.getX(), agent.getY(), "brn", "recieved", "meat", coopAgent.getTag(), amount_exchanged);
					}
				}
			}
		}
        tradePartnersList = null; // no longer need it

		if (resourceType == Maize.class) {
			maizeExchanged += amount_retrieved;
			totalMaizeExchanged += amount_retrieved;		

			if (Village.BRN_COOP_DEBUG) {
				System.out.printf("%d\t%d\n", amount_retrieved, resourceNeeded);
			}

			if (amount_retrieved > 0) {
				if (Village.BRN_COOP_DEBUG) {
					System.out.printf("received required amount\n");
				}
				successfulExchanges += 1;
			} else {
				maizeWasted += totalMaizeExchanged;
			}					
		} else if (resourceType == Meat.class) {
			proteinExchanged += amount_retrieved;
			totalProteinExchanged += amount_retrieved;

			if (amount_retrieved > 0) {
				if (Village.COOP_DEBUG) {
					System.out.printf("received required amount\n");
				}
				proteinSuccessfullyExchanged += 1;
			} else {
				proteinWasted += totalProteinExchanged;
			}
		}
		if(Village.OUTPUT_TRADE){
			if(amount_retrieved > 0){	
				agent.getMySwarm().xmlwriter.flushToDisk("Trades", agent.getMySwarm().getTime());
			}
		}
		return amount_retrieved;
	}

	public int getTListSize() {
		return tradePartners.size();
	}

	public void initialize() {
		tradePartners.clear();

		TListUpdateDead = 0;		
	}

	public void saveLinks() {
		if (Village.SAVE_LINKS) {
			/* default String concatenation (+) is still pretty slow in Java.
			 * I'm therefore changing the code to use StringBuilder.
			 * This is much faster.
			 */
			StringBuilder build = new StringBuilder(1000); // 1000 character initial capacity

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

			for (int i = 0; i < Village.MAX_CHILD_LINKS; i++) {
				build.append(" ");
				build.append(agent.getChildHHTag()[i]);				
			}

			for (BRNTradePartner btp : tradePartners) {
				build.append(" ");
				build.append(btp.getTag());
			}

			agent.getMySwarm().log(build.toString(), "BRNlinks", true);
		}
	}

	public void setTListUpdateDead(int i) {
		TListUpdateDead = i;
	}

	public int export(Class<? extends Resource> resourceType, int amountRequested, Agent agentGettingFood) {
		int amtGranted = 0;

		BRNTradePartner partnerRecord = searchTradePartnersFor(agentGettingFood);

		// not currently a trade partner (we have no history), then add them		
		if (partnerRecord == null && tradePartners.size() < MAX_TRADE_PARTNERS) {
			partnerRecord = new BRNTradePartner(agentGettingFood);
			partnerRecord.lowerQuality(100);
			tradePartners.add(partnerRecord);			
		}

		if (partnerRecord != null && !partnerRecord.owesResource(resourceType)) {
			// this is ugly, but until agents just work with resources, it's necessary
			if (resourceType == Maize.class) {
				amtGranted = agent.exportMaize(amountRequested);
                // now give the maize to the receiver
                agentGettingFood.importFood(amtGranted);
				partnerRecord.recordLoan(Maize.class, amtGranted);
			} else if (resourceType == Meat.class) {
				amtGranted = agent.exportProtein(amountRequested);
                // now give the protein to the receiver
                agentGettingFood.importFoodProtein(amtGranted);
				partnerRecord.recordLoan(Meat.class, amtGranted);
			}
		}

		return amtGranted;		
	}

	protected BRNTradePartner searchTradePartnersFor(Agent agent) {
		for (BRNTradePartner p : tradePartners) {
            int tag = agent.getTag();
			if (p.getTag() == tag)
				return p;
		}

		return null;
	}

	// TODO: check if we actually ever call in protein debts
	/** This only works for maize right now, so I'll write it for that */
	public void callInDebts() {
		for (BRNTradePartner partner : (HashSet<BRNTradePartner>) tradePartners.clone()) {
			if (partner.getAgent() == null)
				continue;
			Double balance = partner.getBalance(Maize.class);

			if (balance > 0) { // they owe me
				int payback = partner.getAgent().RequestPayBack(balance.intValue());

				if (balance - payback >= 1) { // they didn't give me what they owed (we use > 1 in case they owe us a fraction)
                    dingForDefaultOnLoan(partner);
				} else { // they paid us back
                    partner.resetDefaultCount();
					partner.raiseQuality(payback * 100 / balance);
					agent.setMaizeStorage(agent.getMaizeStorage()
							+ payback);
					maizePaidBack += payback;
					maizePaybacks++;
					partner.recordRepayment(Maize.class, payback);
				}
			}

            // now do it again for protein call backs
            balance = partner.getBalance(Meat.class);

			if (balance > 0) { // they owe me
				int payback = partner.getAgent().RequestPayBackProtein(balance.intValue());

				if (balance - payback >= 1) { // they didn't give me what they owed (we use > 1 in case they owe us a fraction)
                    dingForDefaultOnLoan(partner);
				} else { // they paid us back
                    partner.resetDefaultCount();
					partner.raiseQuality(payback * 100 / balance);
                    agent.setCurrentProteinStorage(agent.getCurrentProteinStorage() + payback);
					proteinPaidBack += payback;
                    proteinPaybacks++;
					
					partner.recordRepayment(Meat.class, payback);
				}
			}
		}		
	}

	/** Removes dead agents and updates our list of neighbours.
	 * Note that the neighbour list is set only once per year (currently)
	 */
	public void update() {
		updateTradePartners();
	}

	/** Updates our lists of potential trade partners */
	/* DC: Now we're limiting this to only neighbours who are still within trade range */
	protected void updateTradePartners() {

		ArrayList<Agent> neighbours = Utilities.findAgentsInRange(agent);

		// remove dead partners
		ArrayList<BRNTradePartner> aliveList = new ArrayList<BRNTradePartner>();


		for (BRNTradePartner ag : tradePartners) {
			Agent theAgent = ag.getAgent();


			if (theAgent != null && theAgent.isAlive())
				aliveList.add(ag);

            if (theAgent != null && theAgent.isAlive()) {
                 // add people who moved away from us and owes us
                // to our banned list
                 if (!inTradeRange(theAgent)) {
                     if (ag.getBalance(Maize.class) > 0
                             || ag.getBalance(Meat.class) > 0)
                        bannedList.add(theAgent);
                 } else {
                    aliveList.add(ag);
                }
            }
        }



		tradePartners.clear();
		tradePartners.addAll(aliveList);
		aliveList = null;

		// now add new neighbours
	}



    @Override
    public void clearInformation() {
        tradePartners.clear();
        bannedList.clear();
    }

    /** Creates a trade partner object using the given agent.
     *
     * @return null if the agent is in our banned list, returns an object otherwise.
     *
     */
    public BRNTradePartner createTradePartner(Agent agt) {
        BRNTradePartner part = null;

        if (!bannedList.contains(agt))
            part = new BRNTradePartner(agt);

        return part;
    }

    private boolean inTradeRange(Agent theAgent) {
        return Utilities.distance(agent, theAgent) <= Village.MAX_COOP_RADIUS_BRN;
    }

    
	/** Adds neighbours to our trade partners, just to have people to ask. This one is used for Beyond Hooperville.  */
	private void fillList(HashSet<BRNTradePartner> tradePartners) {


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
			
			if(Village.AGENT_TYPE == Village.HOOPER_AGENTS){
				BeyondGroup myGroup = ((BeyondHooperAgent) agent).getGroup();
				BeyondGroup theirGroup = ((BeyondHooperAgent) result.getAgent()).getGroup();
				
				if (myGroup.getAggressors().contains(theirGroup)){
					tradePartners.remove(result);
				}
			}
		}
	}

    /*  Java use of floating point numbers, like most languages, is in general...
     pretty weak, so we use approximation to check for equality. */
    private boolean isAtDefaultQuality(BRNTradePartner partner) {
        return Math.abs(partner.getQuality() - BRNTradePartner.DEFAULT_QUALITY) < (BRNTradePartner.DEFAULT_QUALITY * 0.1);
    }

    /** returns true if we have no debts either way, and we have no old history */
    private boolean haveNoTradeRelationshipWith(BRNTradePartner partner) {
        return (partner.getBalance(Maize.class) == 0
                    && partner.getBalance(Meat.class) == 0
                    && isAtDefaultQuality(partner));
    }
}


