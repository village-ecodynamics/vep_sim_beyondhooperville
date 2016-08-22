package com.mesaverde.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.math3.util.CombinatoricsUtils;

import com.mesaverde.village.Individual;

//provides a fight method for BeyondHooperville implementing a version of Lanchester's linear law (w=1)
//or Lanchester's square (w=2) law relevant to relatively low-level raiding of one group
//by another. Kills males from numerically superior group; kills households from smaller group; only
//n of males counts in relative group size calculation. May result in changes in group size and territories.

public class BeyondFight {
	private boolean head = true;
	private static Random rand = new Random();
	private BeyondMergeFight mergeFight;
	
	public BeyondFight(BeyondMergeFight mergeFight) {
		this.setMergeFight(mergeFight);
		this.setHead(true);
	}
	
	// A method that calculates the probability that group A will beat group B
	// in a battle, and generates a random outcome based on that probability.
	// THIS DOESN'T ACTUALLY KILL ANYBODY! It is just the test.
	// Currently used in BeyondMergeFightCheck.offerMergeFromAToB(),
	// BeyondMergeFightCheck.considerFightFromAToB(), and
	// BeyondFight.fightGroups().
	public Boolean fightTest(BeyondGroup a, BeyondGroup b){
		// Get the number of fighters available to each group
		int aSize = a.getNetFighters().size();
		int bSize = b.getNetFighters().size();

		if(aSize<1 || bSize<1) return null;
		if(aSize<1) return Boolean.FALSE;
		if(bSize<1) return Boolean.TRUE;
		
		// Get the probability of A beating B, based on relative size
		double probAWin = calculateLanchestersLaw(aSize,bSize,1,1);
		double probBWin = 1-probAWin;

		// This is a logic gate that allows for draws, 
		// but doesn't preference one group over the other.
		// The probability of each group winning is tested
		// against an independently-drawn random number,
		// and then a draw is returned iff both groups pass
		// or both groups fail the probability test. Otherwise,
		// the group that passes wins.
		boolean testA = (rand.nextDouble() <= probAWin);
		boolean testB = (rand.nextDouble() <= probBWin);
		
		if ((testA && testB) || (!testA && !testB)){
			return null;
		}else if (testA){
			return Boolean.TRUE;
		}else{
			return Boolean.FALSE;
		}
	}

	/** A battle ensues!
	 * 
	 * Groups A and B are fighting.
	 * First, a winner is determined probabilistically (based on a mean-field solution to
	 * Lanchester's Linear Law).
	 * 
	 * Then, the number of fighting deaths is calculated as the 
	 * average number of casualties on both sides times a factor "S",
	 * taken as a probability of fatality from a casualty.
	 * 
	 * This is a basic attrition model, with no stopping rule. There
	 * will be a lot of deaths.
	 * 
	 * @param a Group A.
	 * @param b Group B.
	 */
	public void fightGroups(BeyondGroup a, BeyondGroup b) {
		// Does group A win?
		Boolean aWin = fightTest(a,b);
//
//		System.out.println(a);
//		System.out.println(b);
//		System.out.println(aWin);
		
		// Get initial group sizes
		int aInitialSize = a.getNetFighters().size();
		int bInitialSize = b.getNetFighters().size();

		// Kill fighters from both sides, and record the deaths.
		int[] deaths = calculateFightDeathCount(a,b,this.getMergeFight().getHooper2().getMySwarm().getS());

		this.getMergeFight().getHooper2().getMySwarm().setWarDead(this.getMergeFight().getHooper2().getMySwarm().getWarDead() + deaths[0] + deaths[1]);

		killFighters(a,b,deaths[0],deaths[1]);

		// Create dom/sub relationship depending on fight outcome.
		if(aWin==Boolean.TRUE) this.getMergeFight().getMerge().mergeGroups(a,b);
		if(aWin==Boolean.FALSE) this.getMergeFight().getMerge().mergeGroups(b,a);

		if (this.getMergeFight().getHooper2().getMySwarm().isOutput()) {
			int worldtime = this.getMergeFight().getHooper2().getMySwarm().getWorldTime();

			String header = ("Year," + "Offense," + "Defense," +
					"OffenseSize,"+ "DefenseSize," +
					"OffenseDeaths," + "DefenseDeaths," + "aWin" + "\n");

			String data = new String(worldtime + "," + a.getID() + "," + b.getID() + "," + 
					aInitialSize + "," + bInitialSize + "," + 
					deaths[0] + "," + deaths[1] + "," + aWin);

			if(head){
				this.getMergeFight().getHooper2().getHooperOut().recordFightStats(header, data, true);
				this.setHead(false);
			}else{
				this.getMergeFight().getHooper2().getHooperOut().recordFightStats(header, data, false);
			}

		}

	}


	/**
	 * This calculates the number of deaths from a war of attrition,
	 * assuming that the skill of the groups fighting is the same. On
	 * average, then, the number of casualties will be the same from each group.
	 * The number of deaths based on the probability "s" that a casualty will become a
	 * fatality.
	 * 
	 * @param a Group A.
	 * @param b Group B.
	 * @param s The probability of a fatality from a casualty. Must be between [0.0,1.0]
	 * @return A vector of two integers, corresponding to the number of deaths in A and B.
	 */
	public int[] calculateFightDeathCount(BeyondGroup a, BeyondGroup b, double s){
		int aDeaths=0;
		int bDeaths=0;
		int[] deathArray = new int[2];

		// Get the number of fighters available to each group
		int aSize = a.getNetFighters().size();
		int bSize = b.getNetFighters().size();

		// Calculate the number of casualties from each group.
		// In evenly skilled groups, this is the minimum group 
		// size if using a mean-field approach.
		int Q = Math.min(aSize, bSize);

		// Generate separate random vectors of size Q for A and B
		double[] aCasualties = new double[Q];
		for(int i=0; i<Q; i++){
			aCasualties[i] = rand.nextDouble();
		}

		double[] bCasualties = new double[Q];
		for(int i=0; i<Q; i++){
			bCasualties[i] = rand.nextDouble();
		}

		// Count deaths
		for(double aTest : aCasualties){
			if(aTest <= s) aDeaths++;
		}

		for(double bTest : bCasualties){
			if(bTest <= s) bDeaths++;
		}

		deathArray[0] = aDeaths;
		deathArray[1] = bDeaths;

		return(deathArray);

	}

	@SuppressWarnings("unchecked")
	public void killFighters(BeyondGroup a, BeyondGroup b, int aDeaths, int bDeaths){
		// Get the number of fighters available to each group
		ArrayList<Individual> aFighters = a.getNetFighters();
		ArrayList<Individual> bFighters = b.getNetFighters();

		// Kill fighters in each group
		Collections.shuffle(aFighters);
		Collections.shuffle(bFighters);

		ArrayList<Individual> cloneList = (ArrayList<Individual>) aFighters.clone();
		for(int i=0;i<aDeaths;i++){
			aFighters.get(i).die();
			aFighters.set(i,null);
		}

		cloneList.clear();
		cloneList = (ArrayList<Individual>) bFighters.clone();
		for(int i=0;i<bDeaths;i++){
			bFighters.get(i).die();
			bFighters.set(i,null);
		}

		a.updateTerritory();
		b.updateTerritory();

	}

	/**
	 * This function calculates the probability
	 * that group A will beat group B in a fight. It is based on a war of attrition
	 * model, where the probability of winning a fight is a function of the relative
	 * strength of the opposing sides, where strength is the product of numbers and fighting skill.
	 * 
	 * This is also called the Stochastic Lanchester's Linear Law.
	 * The function is taken directly from Kress and Talmor 1999:Eq. 16
	 * 
	 * Kress, M. and Talmor, I. (1999). A new look at the 3:1 rule of 
	 * combat through Markov stochastic Lanchester models.
	 * Journal of the Operational Research Society, 50(7):733–744.
	 * 
	 * alpha_a and alpha_b are Lanchester's "strength" coefficients. 
	 * alpha_a is the strength of A fighters
	 * alpha_b is the strength of B fighters
	 * The odds of an A fighter winning a duel with a B fighter is simply
	 * the ratio of these two numbers. The probability is 1/(1+odds).
	 * 
	 * An alternative method would be to actually simulate duels, which would 
	 * better reflect the stochastic nature of the Markov chain.
	 * 
	 * This function calculates the effective strengths of each group, then returns this probability.
	 */
	public double calculateLanchestersLaw(int aSize, int bSize, double alpha_a, double alpha_b) {		
		// Get the ratio of fighter skill, alpha_ratio
		double alpha_ratio = alpha_b/alpha_a;
		
		// Calculate the battle length coefficient, which scales the probability
		double scalar = Math.pow((1/(alpha_ratio+1)),bSize);
		
		// Calculate summation part of the equation
		double out = 0;
		for(int i=0; i<aSize; i++){
			out += (CombinatoricsUtils.binomialCoefficientDouble(bSize+i-1,bSize-1) * Math.pow(alpha_ratio/(alpha_ratio+1),i));
		}
		
		double aWin = out * scalar;
//		
//		System.out.println("aSize: " + aSize);
//		System.out.println("bSize: " + bSize);
//		System.out.println("aWin: " + aWin + "\n");
		
		return(aWin);
	}

	public BeyondMergeFight getMergeFight() {
		return mergeFight;
	}

	public void setMergeFight(BeyondMergeFight mergeFight) {
		this.mergeFight = mergeFight;
	}

	public boolean isHead() {
		return head;
	}

	public void setHead(boolean head) {
		this.head = head;
	}
}


