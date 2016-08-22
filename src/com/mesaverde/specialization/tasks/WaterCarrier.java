package com.mesaverde.specialization.tasks;

import com.mesaverde.specialization.Constraints;
import com.mesaverde.specialization.Season;
import com.mesaverde.specialization.SpecializedAgent;
import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.specialization.resources.Water;
import com.mesaverde.village.Cell;
import com.mesaverde.village.Village;
import com.mesaverde.village.WaterManager;
import java.util.ArrayList;

public class WaterCarrier extends Task {
	// water carrying capacity = total carrying capacity - vessel weight
	// (4kg)(Lightfoot, 1994)

	private int waterCarryCapacity = Village.CARRY_CAPACITY - 4;
	private int waterSearchRadius;

	public WaterCarrier(SpecializedAgent agent) {
		super(agent);
	}

	/**
	 * A specialized version of that found in Agent. This one ignores sites that
	 * don't have enough water for one trip. This one also improves by returning
	 * a Cell instead of simply Cell coordinates.
	 * 
	 * @param a
	 *            - The x location of the Agent
	 * @param b
	 *            - The y location of the Agent
	 */
	public Cell searchNeighborhoodwaterDX(int a, int b) {
		Cell closest = null;
		Cell location = agent.getCellAt(a, b);
		double closestDistance = Double.POSITIVE_INFINITY;

		ArrayList<Cell> waterCells = agent.getSwarm().getWaterManager()
				.getWaterCells();

		for (Cell c : waterCells) {
			double dist = 0;

			if (c.getWaterType() >= Village.H2O_TYPE
					&& ((dist = WaterManager.distance(location, c)) < closestDistance)
					&& c.getWater() >= waterCarryCapacity) {
				closest = c;
				closestDistance = dist;
			}
		}

		if (closest != null)
			waterSearchRadius = (int) WaterManager.distance(closest, agent
					.getCell());

		return closest;
	}

	/**
	 * @param season
	 *            - the current season of the year
	 * @param cons
	 *            - the time and caloric constraints
	 * @param meetNeeds
	 *            - if true, we'll focus on satisfying the agent's water needs,
	 *            ignoring constraints.
	 * @return The amount of calories expended
	 */
	@Override
	public int performTask(Season season, Constraints cons, boolean meetNeeds) {
		reset();
		int W_cals = 0;

		// we only collect water in the summer right now
		// NOTE: even though water is set as the eminent domain of women, I'm
		// ignoring this, so that anyone can get water
		// work is still calculated on a per woman calorie basis though
		if (season == Season.SUMMER) {
			boolean continueWorking = true;

			do {
				int dx, dy;
				Cell c;
				int w_type;
				float l_max;
				dx = dy = 0;

				c = searchNeighborhoodwaterDX(dx, dy);

				if (c == null) {
					continueWorking = false;
					continue;
				}

				c.getWaterLock().lock();
				w_type = c.getWaterType();
				l_max = c.getWater();

				double costPerTrip = agent.calcTravelCal(waterSearchRadius, 1,
						1);
				int waterTripsAvailable = (int) (l_max / waterCarryCapacity);

				int tripsWeCanMake = (int) (cons.getAvailableCalories() / costPerTrip);

				if (meetNeeds) {
					int need = agent.W_need * agent.getFamilySize();

					// need to factor in how much we've already gotten
					need -= unitCount;
					int tripsNeeded = ((need / waterCarryCapacity) + 1);
					tripsWeCanMake = tripsNeeded;
				}

				int trips = Math.min(waterTripsAvailable, tripsWeCanMake);

				// ok, now to deal with when there isn't enough water at a
				// source
				// if we can't make one trip, then we're done
				if (tripsWeCanMake == 0) {
					continueWorking = false;
				}

				// if there isn't enough water, then we'll get what we can
				// now, and
				// then search for more
				if (trips > 0) {

					double totalCost1 = costPerTrip * trips;
					int totalWaterTaken = waterCarryCapacity * trips;

					W_cals += totalCost1;
					agent.increaseC_water(totalCost1 / Village.WORK_CAL_WOM);

					// unfortunately, we can't parallelize agent behaviour
					// right now
					// so it's as if each agent gets all their water one at
					// a time

					// if the water type found is a spring(4) then water
					// taken from
					// it
					// must be reported back to the cell
					if (w_type == 4) {
						c.setWaterUse(totalWaterTaken);
					}
					c.setWater(l_max - totalWaterTaken);

					cons.increaseCalories(-totalCost1);
					// we also have to subtract the time it took
					// calories are all still calculated based on men hours
					cons.setAvailableTime(cons.getAvailableTime()
							- (totalCost1 / Village.WORK_CAL_MAN));
					// and update the information about how much we got
					unitCount += waterCarryCapacity * trips;

					// update how many calories this has cost
					this.totalCost += totalCost1;
				}

				// now search for more water if we still have energy to get
				// more
				if (tripsWeCanMake <= waterTripsAvailable) {
					continueWorking = false;
				}
				c.getWaterLock().unlock();
			} while (continueWorking);
		}

		agent.addResource(new Water(unitCount, totalCost));

		return W_cals;
	}

	@Override
	public Class<? extends Resource> getResourceType() {
		return Water.class;
	}
}
