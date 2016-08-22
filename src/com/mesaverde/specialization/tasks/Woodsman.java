package com.mesaverde.specialization.tasks;

import com.mesaverde.specialization.Constraints;
import com.mesaverde.specialization.Season;
import com.mesaverde.specialization.SpecializedAgent;
import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.specialization.resources.Wood;
import com.mesaverde.village.Cell;
import com.mesaverde.village.Village;

public class Woodsman extends Task {
	private int FWsearchradius;

	public Woodsman(SpecializedAgent agent) {
		super(agent);
	}

	private int evalDWinCellX(int dx, int dy, int img) {
		return agent.evalDWinCellX(dx, dy, img);
	}

	/**
	 * DC: returns an array of int containing the updated valus for a, b, and c
	 * Making similar changes to this version as I did in WaterCarrier. This way
	 * agents will ignore locations if there isn't enough wood for one trip
	 */
	private int[] searchNeighborhoodFireDX(int a, int b, int c, int img) {
		int k, r, dx, dy, ddx, ddy;
		dx = a;
		dy = b;
		ddx = dx;
		ddy = dy;
		k = 0;
		r = 0;
		int firsttime;
		int DWfirsttime;

		firsttime = 1;
		DWfirsttime = 1;

		while (r <= 100) {
			if (DWfirsttime != 0) {
				k = evalDWinCellX(ddx, ddy, img);
				if (k > 0) // 1. kill 2. random relocation
				{
					a = ddx;
					b = ddy;
					c = 0;
					Cell cell = agent.getCellAt(ddx, ddy);
					float wood = 0;

					if (c == 0)
						wood = cell.getDWPotential(0);
					else
						wood = cell.getFWPotential(0);

					if (wood > Village.CARRY_CAPACITY) {
						FWsearchradius = r;
						return new int[] { a, b, c };
					}
				}
				DWfirsttime = 0;
			} else {
				for (ddy = dy - r; ddy < dy + r + 1; ddy++) {
					if (ddy == dy - r || ddy == dy + r) {
						for (ddx = dx - r; ddx < dx + r + 1; ddx++) {
							k = evalDWinCellX(ddx, ddy, img);
							if (k > 0) // 1. kill 2. random relocation
							{
								a = ddx;
								b = ddy;
								c = 0;
								Cell cell = agent.getCellAt(ddx, ddy);
								float wood = 0;

								if (c == 0)
									wood = cell.getDWPotential(0);
								else
									wood = cell.getFWPotential(0);

								if (wood > Village.CARRY_CAPACITY) {
									FWsearchradius = r;
									return new int[] { a, b, c };
								}
							}
						}
					} else {
						ddx = dx - r;
						k = evalDWinCellX(ddx, ddy, img);
						if (k > 0) // 1. kill 2. random relocation
						{
							a = ddx;
							b = ddy;
							c = 0;
							Cell cell = agent.getCellAt(ddx, ddy);
							float wood = 0;

							if (c == 0)
								wood = cell.getDWPotential(0);
							else
								wood = cell.getFWPotential(0);

							if (wood > Village.CARRY_CAPACITY) {
								FWsearchradius = r;
								return new int[] { a, b, c };
							}
						}
						ddx = dx + r;
						k = evalDWinCellX(ddx, ddy, img);
						if (k > 0) // 1. kill 2. random relocation
						{
							a = ddx;
							b = ddy;
							c = 0;
							Cell cell = agent.getCellAt(ddx, ddy);
							float wood = 0;

							if (c == 0)
								wood = cell.getDWPotential(0);
							else
								wood = cell.getFWPotential(0);

							if (wood > Village.CARRY_CAPACITY) {
								FWsearchradius = r;
								return new int[] { a, b, c };
							}
						}
					}
				}
			}

			if (r - Village.FW_SEARCH_DISTANCE >= 0) {
				if (firsttime != 0) {
					k = evalFWinCellX(ddx, ddy, img);
					if (k > 0) // 1. kill 2. random relocation
					{
						a = ddx;
						b = ddy;
						c = 1;
						Cell cell = agent.getCellAt(ddx, ddy);
						float wood = 0;

						if (c == 0)
							wood = cell.getDWPotential(0);
						else
							wood = cell.getFWPotential(0);

						if (wood > Village.CARRY_CAPACITY) {
							FWsearchradius = r;
							return new int[] { a, b, c };
						}
					}
					firsttime = 0;
				} else {
					for (ddy = dy - (r - Village.FW_SEARCH_DISTANCE); ddy < dy
							+ r - Village.FW_SEARCH_DISTANCE + 1; ddy++) {
						if (ddy == dy - (r - Village.FW_SEARCH_DISTANCE)
								|| ddy == dy + r - Village.FW_SEARCH_DISTANCE) {
							for (ddx = dx - (r - Village.FW_SEARCH_DISTANCE); ddx < dx
									+ r - Village.FW_SEARCH_DISTANCE + 1; ddx++) {
								k = evalFWinCellX(ddx, ddy, img);
								if (k > 0) // 1. kill 2. random relocation
								{
									a = ddx;
									b = ddy;
									c = 1;
									Cell cell = agent.getCellAt(ddx, ddy);
									float wood = 0;

									if (c == 0)
										wood = cell.getDWPotential(0);
									else
										wood = cell.getFWPotential(0);

									if (wood > Village.CARRY_CAPACITY) {
										FWsearchradius = r;
										return new int[] { a, b, c };
									}
								}
							}
						} else {
							ddx = dx - (r - Village.FW_SEARCH_DISTANCE);
							k = evalFWinCellX(ddx, ddy, img);
							if (k > 0) // 1. kill 2. random relocation
							{
								a = ddx;
								b = ddy;
								c = 1;
								Cell cell = agent.getCellAt(ddx, ddy);
								float wood = 0;

								if (c == 0)
									wood = cell.getDWPotential(0);
								else
									wood = cell.getFWPotential(0);

								if (wood > Village.CARRY_CAPACITY) {
									FWsearchradius = r;
									return new int[] { a, b, c };
								}
							}
							ddx = dx + r - Village.FW_SEARCH_DISTANCE;
							k = evalFWinCellX(ddx, ddy, img);
							if (k > 0) // 1. kill 2. random relocation
							{
								a = ddx;
								b = ddy;
								c = 1;
								Cell cell = agent.getCellAt(ddx, ddy);
								float wood = 0;

								if (c == 0)
									wood = cell.getDWPotential(0);
								else
									wood = cell.getFWPotential(0);

								if (wood > Village.CARRY_CAPACITY) {
									FWsearchradius = r;
									return new int[] { a, b, c };
								}
							}
						}
					}
				}
			}
			r++;
		}

		a = ddx;
		b = ddy;
		FWsearchradius = 100;

		return new int[] { a, b, c };
	}

	private int evalFWinCellX(int ddx, int ddy, int img) {
		return agent.evalFWinCellX(ddx, ddy, img);
	}

	@Override
	public int performTask(Season season, Constraints cons, boolean meetNeeds) {
		reset();
		int bw_cals = 0;

		if (season == Season.SUMMER) {
			boolean continueWorking = true;
			double C_fuel = 0;
			int need = -1;
            agent.FWsearchradius = 0;
            agent.FWHappy = 0;
            agent.FWout = 0;

			do {
				int dx, dy, dw;
				float trips, cellwood;
				Cell c;
				trips = 0;

				dx = dy = dw = 0;

				dx = dy = 0;
				int[] res1 = searchNeighborhoodFireDX(dx, dy, dw, 0); // dw acts
				// as a
				// switch
				// between
				// standing
				// crop
				// and
				// dead
				// wood

				dx = res1[0];
				dy = res1[1];
				dw = res1[2];
				double costPerTrip = agent.calcTravelCal(FWsearchradius, 1, 1); // travel
				// costs
				costPerTrip += Village.SC_COLLECT * Village.WORK_CAL_WOM; // collection
				// costs
				c = agent.getCellAt(dx, dy);

				c.getWoodLock().lock();
				// if dw = 1 then wood taken from standing crop, if
				// dw = 0 wood taken from deadwood
				if (dw == 1)
					cellwood = c.getFWPotential(0);
				else
					cellwood = c.getDWPotential(0);

				int woodTripsAvailable = (int) (cellwood / Village.CARRY_CAPACITY);
				int tripsWeCanMake = (int) (cons.getAvailableCalories() / costPerTrip);

				if (meetNeeds) {
					if (need == -1)
						need = agent.FW_need * agent.getFamilySize(); // FW_need
					// set
					// to
					// Village.WOOD_NEED
					// in
					// -createEnd

					// need to factor in how much we've already gotten
					need -= unitCount;
					int tripsNeeded = (int) Math
							.ceil((need * 1.0 / Village.CARRY_CAPACITY));
					tripsWeCanMake = tripsNeeded;
				}

				if (tripsWeCanMake == 0) {
					continueWorking = false;
				}

				trips = Math.min(tripsWeCanMake, woodTripsAvailable);

				// if there isn't enough wood, then we'll get what we can
				// now, and
				// then search for more
				if (trips > 0) {
					double totalCost1 = costPerTrip * trips;
					int totalWoodTaken = (int) (Village.CARRY_CAPACITY * trips);

					// add up costs
					// trip cost
					bw_cals += totalCost1;
					C_fuel += totalCost1 / Village.WORK_CAL_WOM;

					// decrease the amount of wood left
					cellwood -= totalWoodTaken;

					if (dw == 1)
						c.setFWPotential(cellwood, 0);
					else
						c.setDWPotential(cellwood, 0);

					// update constraint data
					cons.increaseCalories(-totalCost1);
					// we also have to subtract the time it took
					cons.setAvailableTime(cons.getAvailableTime()
							- (totalCost1 / Village.WORK_CAL_WOM));
					// and update the information about how much we got
					unitCount += Village.CARRY_CAPACITY * trips;

					// update how many calories this has cost
					this.totalCost += totalCost1;
				}

				// now search for more wood only if we still have energy to get
				// more
				if (tripsWeCanMake <= woodTripsAvailable) {
					continueWorking = false;
				}

				if (FWsearchradius > 24) {
					agent.FWHappy = 100;
				}

				if (FWsearchradius > 49) {
					agent.FWout = 100;
				}

				agent.fwsr = FWsearchradius;
				agent.setC_fuel(C_fuel);
				c.getWoodLock().unlock();
			} while (continueWorking);
		}

		agent.addResource(new Wood(unitCount, totalCost));

		return bw_cals;
	}

	@Override
	public Class<? extends Resource> getResourceType() {
		return Wood.class;
	}
}
