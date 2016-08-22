package com.mesaverde.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import com.mesaverde.groups.BeyondGroup;

public class BeyondMergeFight {
	private BeyondMerge merge; 
	private BeyondFight fight;
	private BeyondHooperMaster hooper2;

	public BeyondMergeFight(BeyondHooperMaster myHooper) {
		this.setHooper2(myHooper);
		this.setMerge(new BeyondMerge(this));
		this.setFight(new BeyondFight(this));
	}

	/*
	 * I consider "attemptMerge(group1, group2)" as the function an outside
	 * class would call upon when necessary. This makes use of other functions
	 * in the class as required.
	 * 
	 * group2 is assumed to be group1's strongest frustration, which is
	 * calculated prior to calling this class (this could be updated to have
	 * this class use a function to search for the worst frustration instead).
	 * 
	 * Fight.java may use a similar outline, but must also use
	 * "Lanchester's Laws" to decide outcome of fight. Merge and Fight could be
	 * combined in the same class.
	 * 
	 * Previously, I suggested that a new class extending HooperAgent.java would
	 * use this class, but the functions here may be best carried out with
	 * groups instead of agents. I believe "HooperGame.java" is the class that
	 * has lists of groups that be updated, a new function may need to be added
	 * there to call upon this function as desired.
	 */

	public int findAlliance(BeyondGroup a, BeyondGroup b) {
		// Alliances here are taken in the sense of Gavrilets 2008
		// An alliance is a temporary joining for mutual benefit
		// This code will only be called if we are interested in these dynamics
		int allianceSize = 0;
		ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
		cloneList.addAll(a.getAlliances());
		Collections.shuffle(cloneList);

		if (!cloneList.isEmpty()
				&& a.getFighters().size() < b.getFighters()
				.size()) {

			// BeyondGroup askedAlliance = cloneList.get(0);
			Iterator<BeyondGroup> iter = cloneList.iterator();
			Object askedAlliance = iter.next();
			// askedAlliance chooses whether or not to join
			// if true add male members to group a
			// if false nothing
			Random rand = new Random();
			double compareAllianceFactor = rand.nextFloat(); // a random number
			// between 0.0
			// and 0.999999
			// (subject to
			// change)


			if (compareAllianceFactor > 0.5) {
				allianceSize = 0; // askedAlliance doesn't accept merge offer,
				// so we set their size to 0 for purposes of
				// later addition
			} else {
				// return true; askedAlliance accepts merge offer, we calculate
				// the # of fighting males and add it to fighters below
				allianceSize = ((BeyondGroup) askedAlliance).getFighters()
						.size();

			}
			//			System.out.println("New Size of alliance: " + allianceSize);
		}
		return allianceSize;

	}
	

	
	public boolean attemptMergeOrFight(BeyondGroup a, BeyondGroup b) {
		// Check size of each.
		// If a is 0, do nothing.
		// if b is 0, merger will happen automatically.
		if(a.getNetFighters().size()<=0)
			return false;

		HashSet<BeyondGroup> aNeighborhood = a.getNeighborhood();

		if (aNeighborhood.contains(b))
			return false;

		// This checks to see if B is willing and able to be a subordinate
		if (offerMergeFromAToB(a,b)==Boolean.TRUE && b.getDomGroup()==null) {
			// If merge is accepted, create a dom/sub relationship
			this.getMerge().mergeGroups(a, b);
			return true;
		} else if (considerFightFromAToB(a, b)==Boolean.TRUE) {
			this.getFight().fightGroups(a, b);
			return true;
		} else {
			return false;
		}
	}


	public Boolean offerMergeFromAToB(BeyondGroup a, BeyondGroup b) {
		// Test whether a would beat b. If aWin=true, accept the merger (you don't want to lose!)
		// If aWin=false, reject the merger (you'll take your chances).
		return(this.getFight().fightTest(a,b));
	}

	public Boolean considerFightFromAToB(BeyondGroup a, BeyondGroup b) {
		// Test whether a would beat b. If aWin=true, engage in a battle;
		// If aWin=false, walk away.
		return(this.getFight().fightTest(a,b));
	}

	public BeyondMerge getMerge() { //Bocinsky
		return merge;
	}

	public void setMerge(BeyondMerge merge) { //Bocinsky
		this.merge = merge;
	}

	public BeyondFight getFight() {
		return fight;
	}

	public void setFight(BeyondFight fight) {
		this.fight = fight;
	}

	public BeyondHooperMaster getHooper2() {
		return hooper2;
	}

	public void setHooper2(BeyondHooperMaster hooper2) {
		this.hooper2 = hooper2;
	}

}
