package com.mesaverde.hunting;

import com.mesaverde.domestication.DomesticationParameters;
import com.mesaverde.village.Agent;
import com.mesaverde.village.Cell;
import com.mesaverde.village.Village;

/**
 * The base hunting strategy including turkey domestication.<p>
 * 
 * This class emulates the {@link com.mesaverde.hunting.AlternateHuntingStrategy AlternateHuntingStrategy} 
 * written by Jason Cowan, but continuously calculates the agent's return rates from hunting, and 
 * abandons hunting once those return rates drop below 
 * {@link com.mesaverde.domestication.DomesticationParameters.TURKEY_RR TURKEY_RR}.<p>
 * 
 * The {@link #execute() execute} method controls these actions.<p>
 * 
 * The {@link #searchHuntatX() searchHuntatX} method allows for domestication to be accounted 
 * for during household relocation.<p>
 * 
 * Domestication itself is toggled in the {@link com.mesaverde.village.Village Village} class.<p>
 * 
 * @author R. Kyle Bocinsky <bocinsky@wsu.edu>
 * @version $Date: 2010/11/22 $
 * @since 1.0
 */

public class DomesticationHuntingStrategy extends HuntingStrategy {
	public DomesticationHuntingStrategy(Agent agent) {
		super(agent);
	}

	/**
	 * This alternative hunting routine is designed to allow the cost of
	 * hunting to be tracked
	 * and forced on the agents. To do this, hunting has been broken up into
	 * several different
	 * phases. First the agent looks for a cell to hunt in. This is done in
	 * a outwardly
	 * radiating pattern from the cells location. Next the agent attempts to
	 * find animals in the cell.
	 * If animals are found, the agent will then hunt deer in the cell. Once
	 * the known deer are depleted,
	 * the agent will continue to look in new cells for additional deer. If
	 * the agent moves beyond 5 km (a
	 *  variable number from its location) it will go back over hunted cells
	 * and start hunting the hares and
	 * rabbits in them.
	 */
	@SuppressWarnings("unused")
	@Override
	public int execute(long hh_protein_need, int hunt_radius) {

		if (hh_protein_need <= 0 || hunt_radius == 0 || !Village.HUNTING) {
			return 0;
		}

		int i;
		int hunt_cal = 0;
		Cell c;
		int dx, dy, dw;
		int hunts = 0;
		int tot_deer;
		int known_deer;
		int killed_deer;
		double deer_distance = 0.0;
		int tot_hares;
		int known_hares;
		int killed_hares;
		int tot_rabbits;
		int known_rabbits;
		int killed_rabbits;
		int numhunts;
		int cont_hunting = 1;
		int hunters = 0;
		double max_hours = 0.0;
		double max_hunt_cal;
		double hunting_time;
		double deer_fix = 0.0;
		C_hunt = 0;

		double deer_time = 0.0;
		double hare_time = 0.0;
		double rabbit_time = 0.0;

		agent.setDeer_hunted(0); // tracks total animals hunted by agent
		agent.setHare_hunted(0);
		agent.setRabbit_hunted(0);

		hunters = agent.getWorkerSize();

		// now determine how many hours can be spent hunting
		// the max that any agent can work in a year is set at
		// 14 hours per day per individual in household over 7 years of age

		max_hours = hunters * 14 * 365;

		// now subtract last years non hunting hours from this amount
		// to determine time that can be spent hunting
		hunting_time = max_hours - agent.getNonhunt_hrs();

		/**
		 * {@code max_hunt_cal} is the maximum amount of energy an agent can spend hunting before its 
		 * net protein return rate drops below that needed to meet all of its need from turkey.
		 */
		max_hunt_cal = (hh_protein_need * DomesticationParameters.BASE_CAL_TURKEY * agent.getMySwarm().getTurkey_maize_per()) / DomesticationParameters.PROTEIN_PER_TURKEY;

		// Search for potential cell
		if (agent.getCurrentProteinStorage() < hh_protein_need) {
			do {
				dx = 0;
				dy = 0;
				dw = 0;

				int[] res = searchNeighborhoodHuntDX(dx, dy, dw, 0, hunts);
				dx = res[0];
				dy = res[1];
				dw = res[2];
				hunts = res[3];

				if (agent.getHsearchradius() <= hunt_radius) {
					// accuire cell found
					c = (Cell) agent.getWorld().getObjectAt(
							Village.wrapX(dx, agent.getX(), agent.getWorldX()),
							Village.wrapY(dy, agent.getY(), agent.getWorldY()));

					// if dw = 0 then deer found, 1 = lagomorphs found
					if (dw == 0) {

						// Determine number of deer able to be hunted
						tot_deer = (int) c.getAnimalTracker().getAmount(Deer.class);
//						System.out.println("Deer in cell: " + tot_deer);

						double dpercent_found = Village.uniformDblRand(0.3,
								0.75);
						known_deer = (int) (tot_deer * dpercent_found);
						deer_fix = tot_deer * dpercent_found;

						if (deer_fix > .50 && known_deer < 1) {
							known_deer = 1;
						}

						// Charge the agent for one trip back and forth, in calories and man-hours.
						hunt_cal += agent.calcTravelCal(agent.getHsearchradius(), 0, 1.0);
						C_hunt += agent.calcTravelCal(agent.getHsearchradius(), 0, 1.0) / Village.WORK_CAL_MAN;
						deer_time += agent.calcTravelCal(agent.getHsearchradius(), 0, 1.0) / Village.WORK_CAL_MAN;

						if (C_hunt > hunting_time || hunt_cal >= max_hunt_cal) {
							known_deer = 0;
							cont_hunting = 0;
						}

						killed_deer = 0;
						numhunts = 0;

						// Hunt Deer

						if (known_deer != 0) {
							do {
								// dpercent_kill is the difficulty number needed
								// to successfully kill a deer
								// dpercent_chance is the number compared to
								// difficulty
								double dpercent_kill = Village.uniformDblRand(0.3, 0.75);
								double dpercent_chance = Village.uniformDblRand(0.0, 1.0);

								numhunts++;

								if (dpercent_chance > dpercent_kill) {
									// deer killed
									killed_deer++;
									agent.setDeer_hunted(agent.getDeer_hunted()+1);
									known_deer--;

									agent.setCurrentProteinStorage(agent
											.getCurrentProteinStorage() + Village.PROTEIN_PER_DEER);
									if (agent.getCurrentProteinStorage() > hh_protein_need) {
										known_deer = 0;
									}
								}

								if (known_deer == 0) {
									// set new deer pops in deercell and cell
									if (c == null) {
										System.out.printf("c is null!");
									} else {
										c.setHuntedDeer(killed_deer);
										// accrue costs of hunting
										hunt_cal += 5 * numhunts
												* Village.WORK_CAL_MAN;
										C_hunt += 5 * numhunts;

										deer_time += 5 * numhunts;

										if (C_hunt > hunting_time || hunt_cal >= max_hunt_cal) {
											cont_hunting = 0;
										}

										// add costs for retrieval
										double trips = killed_deer * 36
												/ Village.CARRY_CAPACITY;
										hunt_cal += agent.calcTravelCal(
												agent.getHsearchradius(), 0, trips);
										C_hunt += agent.calcTravelCal(
												agent.getHsearchradius(), 0, trips)
												/ Village.WORK_CAL_MAN;

										deer_time += agent.calcTravelCal(
												agent.getHsearchradius(), 0, trips)
												/ Village.WORK_CAL_MAN;
										deer_distance += agent.getHsearchradius();

										if (C_hunt > hunting_time || hunt_cal >= max_hunt_cal) {
											cont_hunting = 0;
										}
									}
								}
							} while (known_deer != 0);
						}
//						System.out.println("Deer time: " + deer_time);

					} else {
						// Hunting lagomorphs is different than deer hunting.
						// Instead of acquiring one animal
						// at a time a single hunt will bring in
						// Village.HLAG_HUNT (= 2 hare) or Village.RLAG_HUNT (=5
						// rabbit, Village.h)
						// animals. This means that a cell
						// will not be hunted if there are less than
						// Village.LAG_HUNT rabbits or hares in the cell.
						// Additionally the cost of acquiring these animals is
						// higher.

						if (hunters >= Village.HUNTERS) {
							// Determine number of hares able to be hunted

							tot_hares = c.getAnimalTracker().getIntAmount(Hare.class);
//							System.out.println("Hares found: " + tot_hares);
							double hpercent_found = Village.uniformDblRand(0.3,
									0.75);
							known_hares = (int) (tot_hares * hpercent_found);

							if (known_hares < Village.HLAG_HUNT / 2) {
								known_hares = 0;
							}


							double travelCalc = agent.calcTravelCal(
									agent.getHsearchradius(), 0, 1.0);
							hunt_cal += travelCalc;
							C_hunt += travelCalc / Village.WORK_CAL_MAN;
							hare_time += travelCalc / Village.WORK_CAL_MAN;

							if (C_hunt > hunting_time || hunt_cal >= max_hunt_cal) {
								known_hares = 0;
								cont_hunting = 0;
							}

							killed_hares = 0;
							numhunts = 0;

							if (known_hares > Village.HLAG_HUNT) {
								do {
									// dpercent_kill is the difficulty number
									// needed to successfully hunt
									// dpercent_chance is the number compared to
									// difficulty
									double dpercent_kill = Village
											.uniformDblRand(0.3, 0.75);
									double dpercent_chance = Village
											.uniformDblRand(0.0, 1.0);

									numhunts++;

									if (dpercent_chance > dpercent_kill) {
										// hares killed
										killed_hares += Village.HLAG_HUNT;
										agent.setHare_hunted(agent.getHare_hunted() + Village.HLAG_HUNT);
										known_hares -= Village.HLAG_HUNT;

										agent
										.setCurrentProteinStorage(agent
												.getCurrentProteinStorage()
												+ (Village.PROTEIN_PER_HARE * Village.HLAG_HUNT));
										if (agent.getCurrentProteinStorage() > hh_protein_need) {
											known_hares = 0;
										}
									}

									if (known_hares < Village.HLAG_HUNT) {
										// set new hare pops cell
										c.getAnimalTracker().huntAnimals(Hare.class, killed_hares);
										// accrue costs of hunting

										hunt_cal += 0.5 * numhunts
												* Village.WORK_CAL_MAN;
										C_hunt += 0.5 * numhunts;

										hare_time += 0.5 * numhunts;

										if (Village.HUNTERS > 1) {
											hunt_cal += 0.5 * numhunts * Village.WORK_CAL_WOM;
											C_hunt += 0.5 * numhunts;
											hare_time += 0.5 * numhunts;

											for (i = 0; i < Village.HUNTERS - 2; i++) {
												hunt_cal += 0.5 * numhunts * Village.WORK_CAL_KID;
												C_hunt += 0.5 * numhunts;
												hare_time += 0.5 * numhunts;
											}
										}

										// add costs for retrieval
										float trips = killed_hares
												* Village.HARE_WEIGHT
												/ Village.CARRY_CAPACITY;
										hunt_cal += agent.calcTravelCal(
												agent.getHsearchradius(), 0, trips);
										C_hunt += agent.calcTravelCal(
												agent.getHsearchradius(), 0, trips)
												/ Village.WORK_CAL_MAN;

										hare_time += agent.calcTravelCal(
												agent.getHsearchradius(), 0, trips)
												/ Village.WORK_CAL_MAN;

										if (C_hunt > hunting_time || hunt_cal >= max_hunt_cal) {
											cont_hunting = 0;
										}
									}
								} while (known_hares > Village.HLAG_HUNT);
							}

							if (agent.getCurrentProteinStorage() < hh_protein_need
									&& C_hunt < hunting_time &&  hunt_cal < max_hunt_cal) {
								// Determine number of rabbits able to be hunted
								tot_rabbits = c.getAnimalTracker().getIntAmount(Rabbit.class);
								double rpercent_found = Village.uniformDblRand(
										0.3, 0.75);
								known_rabbits = (int) (tot_rabbits * rpercent_found);

								if (known_rabbits < Village.RLAG_HUNT / 2) {
									known_rabbits = 0;
								}

								killed_rabbits = 0;
								numhunts = 0;

								if (known_rabbits > Village.RLAG_HUNT) {
									do {
										// rpercent_kill is the difficulty
										// number needed to successfully hunt
										// rpercent_chance is the number
										// compared to difficulty
										double rpercent_kill = Village
												.uniformDblRand(0.3, 0.75);
										double rpercent_chance = Village
												.uniformDblRand(0.0, 1.0);

										numhunts++;

										if (rpercent_chance > rpercent_kill) {
											// rabbits killed
											killed_rabbits += Village.RLAG_HUNT;
											agent.setRabbit_hunted(agent.getRabbit_hunted() + Village.RLAG_HUNT);
											known_rabbits -= Village.RLAG_HUNT;

											agent
											.setCurrentProteinStorage(agent
													.getCurrentProteinStorage()
													+ (Village.PROTEIN_PER_RABBIT * Village.RLAG_HUNT));
											if (agent.getCurrentProteinStorage() > hh_protein_need) {
												known_rabbits = 0;
											}
										}

										if (known_rabbits < Village.RLAG_HUNT) {
											// set new rabbit pops cell
											c.getAnimalTracker().huntAnimals(Rabbit.class, killed_rabbits);
											// accumulate costs of hunting

											hunt_cal += 0.5 * numhunts
													* Village.WORK_CAL_MAN;
											C_hunt += 0.5 * numhunts;

											rabbit_time += 0.5 * numhunts;

											if (Village.HUNTERS > 1) {
												hunt_cal += 0.5 * numhunts
														* Village.WORK_CAL_WOM;
												C_hunt += 0.5 * numhunts;

												rabbit_time += 0.5 * numhunts;

												for (i = 0; i < Village.HUNTERS - 2; i++) {
													hunt_cal += 0.5
															* numhunts
															* Village.WORK_CAL_KID;
													C_hunt += 0.5 * numhunts;

													rabbit_time += 0.5 * numhunts;
												}
											}
										}

										// add costs for retrieval
										float trips = killed_rabbits
												* Village.RABBIT_WEIGHT
												/ Village.CARRY_CAPACITY;
										hunt_cal += agent.calcTravelCal(
												agent.getHsearchradius(), 0, trips);
										C_hunt += agent.calcTravelCal(
												agent.getHsearchradius(), 0, trips)
												/ Village.WORK_CAL_MAN;

										rabbit_time += agent.calcTravelCal(
												agent.getHsearchradius(), 0, trips)
												/ Village.WORK_CAL_MAN;

										if (C_hunt > hunting_time || hunt_cal >= max_hunt_cal) {
											cont_hunting = 0;
										}
									} while (known_rabbits > Village.RLAG_HUNT);
								}
							}
						}
					}

				} else {
					cont_hunting = 0;
				}

				if (agent.getCurrentProteinStorage() > hh_protein_need) {
					cont_hunting = 0;
				}
				if (agent.getHsearchradius() == hunt_radius) {
					// if no meat found within hunt radius km then stop hunting
					// can cause birth penalties
					cont_hunting = 0;
				}
			} while (cont_hunting != 0);
		}

		if (C_hunt > 0) {
			agent.setHunting_return_rate(0);
		}

		if (deer_time > 0) {
			agent.setDeer_return_rate((agent.getDeer_hunted() * Village.PROTEIN_PER_DEER) / (deer_time*Village.WORK_CAL_MAN));
			agent.setDeer_time(deer_time);
			agent.setDeer_distance(deer_distance);
			agent.setHunting_return_rate(agent.getHunting_return_rate() + (agent.getDeer_hunted() * Village.PROTEIN_PER_DEER)/hunt_cal);
		}

		if (hare_time > 0) {
			agent.setHare_return_rate((agent.getHare_hunted() * Village.PROTEIN_PER_HARE) / (hare_time*Village.WORK_CAL_MAN));
			agent.setHare_time(hare_time);
			agent.setHunting_return_rate(agent.getHunting_return_rate() + (agent.getHare_hunted() * Village.PROTEIN_PER_HARE)/hunt_cal);
		}

		if (rabbit_time > 0) {
			agent.setRabbit_return_rate((agent.getRabbit_hunted() * Village.PROTEIN_PER_RABBIT) / (rabbit_time*Village.WORK_CAL_MAN));
			agent.setRabbit_time(rabbit_time);
			agent.setHunting_return_rate(agent.getHunting_return_rate() + (agent.getRabbit_hunted() * Village.PROTEIN_PER_RABBIT)/hunt_cal);
		}

		return hunt_cal;
	}


	@SuppressWarnings("unused")
	@Override
	public int searchHuntatX(int dx, int dy) {

		long hh_protein_need = 0;		// grams needed for hh to survive
		int protein_cal = 0;       //Numbers of Calories used for hunting
		int i_protein_storage;

		//This alternative hunting routine is designed to allow the cost of hunting to be tracked
		//and forced on the agents.  To do this, hunting has been broken up into several different
		//phases.  First the agent looks for a cell to hunt in.  This is done in a outwardly
		//radiating pattern from the cells location.  Next the agent attempts to find animals in the cell.
		//If animals are found, the agent will then hunt deer in the cell. Once the known deer are depleted,
		//the agent will continue to look in new cells for additional deer. If the agent moves beyond 3 km (a
		//variable number) from its location it will go back over hunted cells and start hunting the hares and
		//rabbits in them.
		Cell c;
		int initx, inity;
		int dw;
		int hunts = 0;
		int tot_deer;
		int known_deer;
		int killed_deer;
		int numhunts;
		int cont_hunting = 1;
		int tot_hares;
		int known_hares;
		int killed_hares;
		int tot_rabbits;
		int known_rabbits;
		int killed_rabbits;
		int hunters = 0;
		int i;
		double dpercent_found;
		double dpercent_kill;
		double dpercent_chance;
		double trips;
		double hpercent_found;
		double hpercent_kill;
		double hpercent_chance;
		double rpercent_found;
		double rpercent_kill;
		double rpercent_chance;

		double max_hunt_cal;

		double deer_fix = 0.0;

		// calculate protein need for hh to survive.  Now, agents only search for the amount of protein they acquired from hunting in the last
		// year.  This effectively raises the rank of cells with less access to protein resources, presumably ones with higher maize productivity.
		hh_protein_need = (long)(agent.getMySwarm().getP_need() * agent.getFamilySize() * 365);
		max_hunt_cal = (hh_protein_need * DomesticationParameters.BASE_CAL_TURKEY * agent.getMySwarm().getTurkey_maize_per()) / DomesticationParameters.PROTEIN_PER_TURKEY;

		//        System.out.printf("family size: %d\n", agent.getFamilySize()); 
		//        System.out.printf("max_hunt_cal: %f\n", max_hunt_cal); 


		i_protein_storage = 0;
		agent.setHunt_test_penalty(0);



		if (hh_protein_need <= 0 || agent.getMySwarm().getHunting_radius() == 0 || !Village.HUNTING) {
			return 0;
		}

		initx = dx;
		inity = dy;
		do {
			dx = initx;
			dy = inity;
			dw = 0;

			int res[] = searchNeighborhoodHuntDX(dx, dy, dw, 1, hunts);
			dx = res[0];
			dy = res[1];
			dw = res[2];
			hunts = res[3];

			if (agent.getHsearchradius() <= agent.getMySwarm().getHunting_radius()) {
				//accuire cell found
				c = (Cell) agent.getWorld().getObjectAt(Village.wrapX(dx, agent.getX(), agent.getWorldX()), Village.wrapY(dy, agent.getY(), agent.getWorldY()));

				if (dw == 0) {

					//Determine number of deer able to be hunted
					tot_deer = (int) c.getAnimalTracker().getAmount(Deer.class);

					dpercent_found = Village.uniformDblRand(0.3, 0.75);
					known_deer = (int) (tot_deer * dpercent_found);

					deer_fix = tot_deer * dpercent_found;

					if (deer_fix > .50 && known_deer < 1) {
						known_deer = 1;
					}

					protein_cal += agent.calcTravelCal(agent.getHsearchradius(), 0, 1.0);

					if (protein_cal >= max_hunt_cal) {
						cont_hunting = 0;
						known_deer = 0;
					}

					killed_deer = 0;
					numhunts = 0;

					//Hunt

					if (known_deer != 0) {
						do {
							//dpercent_kill is the difficulty number needed to successfully kill a deer
							//dpercent_chance is the number compared to difficulty
							dpercent_kill = Village.uniformDblRand(0.3, 0.75);
							dpercent_chance = Village.uniformDblRand(0.0, 1.0);

							numhunts++;

							if (dpercent_chance > dpercent_kill) {
								//deer killed
								killed_deer++;
								known_deer--;
								i_protein_storage += Village.PROTEIN_PER_DEER;
								if (i_protein_storage > hh_protein_need) {
									known_deer = 0;
								}
							}

							if (known_deer == 0) {
								//accrue costs of hunting
								protein_cal += 5 * numhunts * Village.WORK_CAL_MAN;

								//add costs for retrieval
								trips = (killed_deer * 36) / Village.CARRY_CAPACITY;
								protein_cal += agent.calcTravelCal(agent.getHsearchradius(), 0, trips);
								if (protein_cal >= max_hunt_cal) {
									cont_hunting = 0;
								}
							}
						} while (known_deer != 0);
					}
				} else {
					//Hunting lagomorphs and turkeys is different than deer hunting.  Instead of accuiring one animal
					//at a time a single hunt will bring in Village.LAG_HUNT (= 10) animals.  This means that a cell
					//will not be hunted if there are less than Village.LAG_HUNT rabbits or hares in the cell.
					//Additionally the cost of accuiring these animals is higher.  Hunting Lagomophs
					//will require Village.LAG_HUNTERS (3) members of the family to be over the age of 7, and costs are
					//accrued for Village.LAG_HUNTERS people in the hunt.

					if (hunters >= Village.HUNTERS) {
						//Determine number of hares able to be hunted

						tot_hares = c.getAnimalTracker().getIntAmount(Hare.class);
						hpercent_found = Village.uniformDblRand(0.3, 0.75);
						known_hares = (int) (tot_hares * hpercent_found);

						if (known_hares < (Village.HLAG_HUNT / 2)) {
							known_hares = 0;
						}

						protein_cal += agent.calcTravelCal(agent.getHsearchradius(), 0, 1.0);
						killed_hares = 0;
						numhunts = 0;
						if (protein_cal >= max_hunt_cal) {
							cont_hunting = 0;
							known_hares = 0;
						}


						if (known_hares > Village.HLAG_HUNT) {
							do {
								//dpercent_kill is the difficulty number needed to successfully hunting
								//dpercent_chance is the number compared to difficulty
								hpercent_kill = Village.uniformDblRand(0.3, 0.75);
								hpercent_chance = Village.uniformDblRand(0.0, 1.0);

								numhunts++;

								if (hpercent_chance > hpercent_kill) {
									//hares killed
									killed_hares += Village.HLAG_HUNT;
									known_hares -= Village.HLAG_HUNT;
									i_protein_storage += (Village.PROTEIN_PER_HARE * Village.HLAG_HUNT);
									if (i_protein_storage > hh_protein_need) {
										known_hares = 0;
									}
								}

								if (known_hares < Village.HLAG_HUNT) {
									//accrue costs of hunting

									protein_cal += 0.5 * numhunts * Village.WORK_CAL_MAN;
									if (Village.HUNTERS > 1) {
										protein_cal += 0.5 * numhunts * Village.WORK_CAL_WOM;
										for (i = 0; i < (Village.HUNTERS - 2); i++) {
											protein_cal += 0.5 * numhunts * Village.WORK_CAL_KID;
										}
									}


									//add costs for retrieval
									trips = (killed_hares * Village.HARE_WEIGHT) / Village.CARRY_CAPACITY;
									protein_cal += agent.calcTravelCal(agent.getHsearchradius(), 0, trips);
									if (protein_cal >= max_hunt_cal) {
										cont_hunting = 0;
									}
								}


							} while (known_hares > Village.HLAG_HUNT);
						}

						//Determine number of rabbits able to be hunted
						tot_rabbits = c.getAnimalTracker().getIntAmount(Rabbit.class);
						rpercent_found = Village.uniformDblRand(0.3, 0.75);
						known_rabbits = (int) (tot_rabbits * rpercent_found);


						if (known_rabbits < (Village.RLAG_HUNT / 2)) {
							known_rabbits = 0;
						} /*else if (known_rabbits < (Village.RLAG_HUNT + 1)) {
    known_rabbits = (Village.RLAG_HUNT + 1);
}*/



						protein_cal += agent.calcTravelCal(agent.getHsearchradius(), 0, 1.0);
						killed_rabbits = 0;
						numhunts = 0;
						if (protein_cal >= max_hunt_cal) {
							cont_hunting = 0;
							known_rabbits = 0;
						}


						if (known_rabbits > Village.RLAG_HUNT) {
							do {
								//rpercent_kill is the difficulty number needed to successfully hunting
								//rpercent_chance is the number compared to difficulty
								rpercent_kill = Village.uniformDblRand(0.3, 0.75);
								rpercent_chance = Village.uniformDblRand(0.0, 1.0);

								numhunts++;

								if (rpercent_chance > rpercent_kill) {
									//rabbits killed
									killed_rabbits += Village.RLAG_HUNT;
									known_rabbits -= Village.RLAG_HUNT;
									i_protein_storage += (Village.PROTEIN_PER_RABBIT * Village.RLAG_HUNT);
									if (i_protein_storage > hh_protein_need) {
										known_rabbits = 0;
									}
								}

								if (known_rabbits < Village.RLAG_HUNT) {
									//accrue costs of hunting

									protein_cal += 0.5 * numhunts * Village.WORK_CAL_MAN;
									if (Village.HUNTERS > 1) {
										protein_cal += 0.5 * numhunts * Village.WORK_CAL_WOM;
										for (i = 0; i < (Village.HUNTERS - 2); i++) {
											protein_cal += 0.5 * numhunts * Village.WORK_CAL_KID;
										}
									}
								}

								//add costs for retrieval
								trips = (killed_rabbits * Village.RABBIT_WEIGHT) / Village.CARRY_CAPACITY;
								protein_cal += agent.calcTravelCal(agent.getHsearchradius(), 0, trips);
								if (protein_cal >= max_hunt_cal) {
									cont_hunting = 0;
								}
							} while (known_rabbits > Village.RLAG_HUNT);
						}
					}
				}
			} else {
				cont_hunting = 0;
			}

			if (i_protein_storage > hh_protein_need) {
				cont_hunting = 0;
			}

		} while (cont_hunting != 0);

		//        System.out.printf("hh_protein_need: %d\n", hh_protein_need);    
		//		System.out.printf("Protein From Hunting: %d\n", i_protein_storage);

		if (hh_protein_need - i_protein_storage > 0) {
			hh_protein_need -= i_protein_storage;

			int numTurkey = 0;

			numTurkey = (int)Math.ceil((double)hh_protein_need/DomesticationParameters.PROTEIN_PER_TURKEY);

			//   		System.out.printf("numTurkey: %d\n", numTurkey);

			agent.setI_turkey_kept(numTurkey);

			//    		System.out.printf("protein from turkey: %d\n", (int)(numTurkey * Parameters.PROTEIN_PER_TURKEY));

			protein_cal += (int)(numTurkey * DomesticationParameters.BASE_CAL_TURKEY * agent.getMySwarm().getTurkey_maize_per());
		}

		return (protein_cal);
	}
}