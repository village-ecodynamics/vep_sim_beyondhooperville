package com.mesaverde.specialization;

public class Constraints {	
	private double availableTime;
	private double availableCalories;
	private Constraints group;

	public Constraints(double cals, double time) {
		this(cals, time, null);
	}

	/** Create a constraints limited by cals and time.  The parameter group identifies the
	 * 
	 * @param cals - the amount of calories available
	 * @param time - the amount of time available (generally ignored right now)
	 * @param group - the overarching constraint group (like how many calories available in a season).
	 */
	public Constraints(double cals, double time, Constraints group) {
		setAvailableTime(time);
		setAvailableCalories(cals);

		if (SpecializedAgent.ENFORCE_SEASON_CONSTRAINTS)
			this.group = group;
	}

	public Constraints() {	
		this(0, 0);
	}

	/** Don't use this in combination with getAvailableCalories, as the amount
	 * returned by that function isn't necessarily the actual amount.  Use the increaseCalories
	 * method instead.
	 */
	public void setAvailableCalories(double availableCalories) {
		this.availableCalories = availableCalories;
	}

	/** Can be used to increase or decrease the number of calories (use negative number parameter to decrease). 
	 * This also affects the group. */
	public void increaseCalories(double cals) {
		setAvailableCalories(availableCalories + cals);
	}

	/** Ensures that the value returned does not exceed the limit of the group, if there is one */
	public double getAvailableCalories() {
		double cals = availableCalories;

		if (group != null)
			cals = Math.min(cals, group.availableCalories);

		return cals;
	}

	/** Don't use this in combination with getAvailableTime, as the amount
	 * returned by that function isn't necessarily the actual amount.  Use the increaseTime
	 * method instead.
	 */
	public void setAvailableTime(double availableTime) {
		this.availableTime = availableTime;	
	}

	/** Can be used to increase or decrease the time (use negative number parameter to decrease). 
	 * This also affects the group. */
	public void increaseTime(double time) {
		availableTime += time;

		if (group != null)
			group.availableTime += time;
	}

	/** Ensures that the value returned does not exceed the limit of the group, if there is one */
	public double getAvailableTime() {
		double time = availableTime;

		if (group != null)
			time = Math.min(time, group.availableTime);

		return time;
	}	
}
