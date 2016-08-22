package com.mesaverde.specialization.tasks;

import com.mesaverde.specialization.Constraints;
import com.mesaverde.specialization.Season;
import com.mesaverde.specialization.SpecializedAgent;
import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.village.Village;
import java.util.concurrent.locks.Lock;


/** DC: TODO: Note: - One change that I've made is that agents no longer make trips to locations where they can't collect a full load.
 * Normally, they'd make an additional trip even if it would just result in one piece of wood, or one litre of water.
 * Note: Do NOT reuse Task objects.  The state information contained within is updated each time performTask is called.
 * This means it's not simply a reflection of what occured the current year.
 *  The Agent can then use the resources, trade them, or do something else.
 *
 */
public abstract class Task {
	protected SpecializedAgent agent;
	protected int unitCount;
	protected double totalCost;
	protected int amountNeeded;

	protected Task(SpecializedAgent agent) {
		this.agent = agent;
	}

    /** Creating this for special cases where we will not access the agent directly
     * The problem is that the agent will be null if called on later */
    protected Task() {
    }

	public int getUnitCount() {
		return unitCount;
	}

	public double getTotalCost() {
		return totalCost;
	}

	public abstract int performTask(Season season, Constraints cons, boolean onlyNeeds);

	public void reset() {
		unitCount = 0;
		totalCost = 0;
		amountNeeded = 0;
	}

	public int getAmountNeeded() {
		return amountNeeded;
	}

	/** This method is used to track the usage of the resource produced by this Task */
	public void increaseAmountNeeded(int amount) {
		amountNeeded += amount;
	}

	public abstract Class<? extends Resource> getResourceType();

    /** Tries to apply a lock if multi-threading is enabled */
    protected void lock(Lock lock) {
        if (Village.ENABLE_MULTITHREADING)
            lock.lock();
    }

    /** Tries to unlock a lock object if multi-threading is enabled */
    protected void unlock(Lock lock) {
        if (Village.ENABLE_MULTITHREADING) {
            lock.unlock();
        }
    }
	

}
