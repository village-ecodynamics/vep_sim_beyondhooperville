package com.mesaverde.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import com.mesaverde.specialization.resources.Maize;
import com.mesaverde.village.Utilities;
import com.mesaverde.village.Village;

public class BeyondHooperGame {    

	private BeyondGroup group;

	/** Collection data */
	private double totalLeaderDefectionSanctionCosts;
	private double totalLeaderTaxCollection;    
	private double totalLeaderTaxSanctionCosts;
	private int perCapitaBenefitSize;

	public BeyondHooperGame(BeyondGroup group) {
		this.setGroup(group);
	}

	public void runGame() {
		resetCollectionStats();

		if(this.getGroup().isHierarchical()){
			processHierarchical();
		}else{
			processNonHierarchical();
		} 

		outputHooperPGStats();
	}

	private void resetCollectionStats() {
		setTotalLeaderDefectionSanctionCosts(0);
		setTotalLeaderTaxCollection(0);
		setTotalLeaderTaxSanctionCosts(0);
		setPerCapitaBenefitSize(0);

		ArrayList<BeyondHooperAgent> cloneList = new ArrayList<BeyondHooperAgent>();
		cloneList.addAll(this.getGroup().getMembers());
		shuffleAgents(cloneList);
		for(BeyondHooperAgent a : cloneList) {
			a.setBenefitReceived(0);
			a.setPaidContributionOrTaxes(0);
			a.setPgGameBenefit(0);
			a.setPgGameLoss(0);
		}
	}

	public double calcTotalGroupBenefit(double totalMaizeCollected) {
		int group_size = (int) (totalMaizeCollected / this.getGroup().getMySwarm().getRequiredContribution());

		return calcBenefit(group_size) * group_size;
	}

	private void processNonHierarchical() {

		//3. OK, now ask all to pay their contribution (c in Hooper)

		HashSet<BeyondHooperAgent> groupMembers = this.getGroup().getMembers();

		//            BeyondHooperAgent first = groupMembers.iterator().next();
		HashSet<BeyondHooperAgent> monitors = new HashSet<BeyondHooperAgent>();
		double totalMaizeCollected = 0d;                           

		// we need to know how many monitors we have in the group
		for (BeyondHooperAgent ag : groupMembers) {
			if (ag.getPreference().isMM()) {
				monitors.add(ag);
			}
		}

		for (BeyondHooperAgent ag : groupMembers) {
			// contribution: agents keep track of their own states and won't contribute here if they are RC
			// unless they have been previously punished
			Maize m = ag.payContribution();

			if (m == null && monitors.size() > 0) {
				haveMonitorsPunishDefectionBy(ag, monitors);                                        
			} else {
				if (m != null) {
					totalMaizeCollected += m.getAmount();
				}
			}

			haveMonitorsPayMonitoringCostFor(ag, monitors);                               
		}

		//4. Since this is a public-goods game, now each gets a benefit regardless of whether each contributed
		// The size of the benefit pool (totalMaizeCollected) is a function of how many contributed
		// The amount that each member of the group gets is a function of the size of the pool and the size of the group
		this.perCapitaBenefitSize = (int) (calcTotalGroupBenefit(totalMaizeCollected) / groupMembers.size());            
		for (BeyondHooperAgent ag : groupMembers) {                
			ag.setMaizeStorage(ag.getMaizeStorage() + this.perCapitaBenefitSize);
			ag.setPgGameBenefit(ag.getPgGameBenefit() + this.perCapitaBenefitSize);
		}                   

	}

	/** monitors (MM in Hooper) all pay the total costs of monitoring  */
	private void haveMonitorsPayMonitoringCostFor(BeyondHooperAgent ag, HashSet<BeyondHooperAgent> monitors) {
	
		double sanctionDist = Utilities.distance(ag, ag);
		double sanctionDistCost = sanctionDist * this.getGroup().getMySwarm().getCs_sanction_dist();

		double monitoringCost = this.getGroup().getMySwarm().getKg_day() * this.getGroup().getMySwarm().getCm_monitor_cost() + (sanctionDistCost);

		for (BeyondHooperAgent monitor : monitors) {    	 
			if (monitor != ag) {               
				monitor.setMaizeStorage((int) (monitor.getMaizeStorage() - monitoringCost));
				monitor.setPgGameLoss(monitor.getPgGameLoss() + monitoringCost);
			}
		}	
	}

	private void haveMonitorsPunishDefectionBy(BeyondHooperAgent ag, HashSet<BeyondHooperAgent> monitors) {    	
		double punishmentCost = this.getGroup().getMySwarm().getKg_day() * this.getGroup().getMySwarm().getCs_sanction_cost();

		for (BeyondHooperAgent monitor : monitors) {
			if (monitor != ag) {
				monitor.setMaizeStorage((int) (monitor.getMaizeStorage() - punishmentCost));
				monitor.setPgGameLoss(monitor.getPgGameLoss() + punishmentCost);
				ag.punishDefection();
			}
		}	
	}

	//Process hierarchicals first
	private void processHierarchical() {

		//5. Leader of each group collects contributions, taxes and pays monitoring fee
		//6. Leader of each group punishes defectors and tax-cheats, paying the costs to do so
		//Leaders have 3 kinds of costs: 
		// they must monitor everyone (Hooper's c-sub-m, our cm_MONITOR_COST)
		// they must sanction non-contributors to group benefit (Hooper's c-sub-s, our cs_SANCTION_COST)
		// they must sanction non-taxpayers (Hooper's c-hat-sub-s, our cs_TAX_SANCTION_COST)

		BeyondHooperAgent leader = group.getLeader();
		double totalMaizeCollected = 0d;	
		double totalContributionsCollected = 0;
		double requiredContribution = this.getGroup().getMySwarm().getC_cost() * this.getGroup().getMySwarm().getKg_day();

		// make sure leader doesn't defect from himself, or cheat
		leader.getPreference().setPunishedTaxCheat(true);
		leader.getPreference().setPunishedDefector(true);			

		BeyondGroup leadersGroup = leader.getGroup();

		for (BeyondHooperAgent ag : leadersGroup.getMembers()) {				
			if (ag == leader)
				continue;

			// leaders have to contribute too? I have them doing so automatically below
			// when I add it to the totalContributionsCollected
			Maize m = ag.payContribution();

			if (m == null) {
				totalMaizeCollected = punishDefectionBy(ag, totalMaizeCollected);					
			} else {					
				totalMaizeCollected += m.getAmount();	
				totalContributionsCollected += m.getAmount();
			}

			// monitoring cost
			totalMaizeCollected -= this.getGroup().getMySwarm().getKg_day() * this.getGroup().getMySwarm().getCm_monitor_cost();
		}

		// add maize collected
		leader.setMaizeStorage((int) (leader.getMaizeStorage() + totalMaizeCollected));
		leader.setPgGameBenefit(leader.getPgGameBenefit() + totalMaizeCollected);

		//7. Leader pays group's contribution to game
		// make sure leader adds his own contribution			
		totalContributionsCollected += this.getGroup().getMySwarm().getC_cost() * this.getGroup().getMySwarm().getKg_day();

		double maxCanBePayed = Math.min(totalContributionsCollected, leader.getMaizeStorage());			

		//8. Each individual receives the benefits of the contributions
		HashSet<BeyondHooperAgent> benefitGroup = leadersGroup.getMembers();
		benefitGroup.add(leader);

		int benefitSize = (int) (maxCanBePayed / requiredContribution); // let's see the effective size of the group by payments						

		// leader pays the group's contributions - moved here
		leader.setMaizeStorage((int) (leader.getMaizeStorage() - (benefitSize * requiredContribution)));
		leader.setPgGameLoss(leader.getPgGameLoss() + (benefitSize * requiredContribution));

		this.perCapitaBenefitSize = (int) calcBenefit(benefitSize);

		for (BeyondHooperAgent ag : benefitGroup) {  // get the membership	
			ag.receiveBenefit(this.perCapitaBenefitSize);				
		}

		// collect taxes from the group
		collectTaxes(leader, group, benefitSize);

	}
	

	private double punishDefectionBy(BeyondHooperAgent ag, double maizeCollected) {
		
		ag.punishDefection();
		double sanction = this.getGroup().getMySwarm().getKg_day() * this.getGroup().getMySwarm().getCs_sanction_cost();
		setTotalLeaderDefectionSanctionCosts(getTotalLeaderDefectionSanctionCosts()
				+ sanction);
		return maizeCollected - sanction; 				
	}


	private void collectTaxes(BeyondHooperAgent leader, BeyondGroup group, int benefitSize) {
		double taxEvasionSanctionCosts = 0;
		double taxesCollected = 0;

		for (BeyondHooperAgent ag : group.getMembers()) {				
			if (ag == leader)
				continue;
			// taxes
			Maize m = ag.payTax(benefitSize);
			

			if (m == null) {				
				ag.punishTaxEvasion();	
				taxEvasionSanctionCosts += this.getGroup().getMySwarm().getKg_day() * this.getGroup().getMySwarm().getCs_tax_sanction_cost(); 
			} else {
				setTotalLeaderTaxCollection(getTotalLeaderTaxCollection() + m.getAmount());
				taxesCollected += m.getAmount();
			}
		}

		// deduct costs of sanctioning tax evasion, and add taxes collected	
		leader.setMaizeStorage((int)(leader.getMaizeStorage() - taxEvasionSanctionCosts + taxesCollected));
		leader.setPgGameBenefit(leader.getPgGameBenefit() + taxesCollected);
		leader.setPgGameLoss(leader.getPgGameLoss() + taxEvasionSanctionCosts);
		setTotalLeaderTaxSanctionCosts(getTotalLeaderTaxSanctionCosts() + taxEvasionSanctionCosts);
	}

	private void outputHooperPGStats(){
		if(this.getGroup().getMySwarm().isOutput()){
			int worldtime = this.getGroup().getMySwarm().getWorldTime();

			String header = ("Year," +
					"Group," +
					"isHierarchical," +
					"leaderTaxRate," +
					"totalLeaderDefectionSanctionCosts," +
					"totalLeaderTaxCollection," +
					"totalLeaderTaxSanctionCosts\n");

			String data="";
			if(this.getGroup().isHierarchical()){
				data = (worldtime + "," + 
						group.getID() + "," + 
						this.getGroup().isHierarchical() + "," + 
						this.getGroup().getLeader().getPreference().getTaxRate() + "," + 
						totalLeaderDefectionSanctionCosts + "," + 
						totalLeaderTaxCollection + "," + 
						totalLeaderTaxSanctionCosts);
			}else{
				data = (worldtime + "," + 
						group.getID() + "," + 
						this.getGroup().isHierarchical() + "," + 
						"NA," + 
						"NA," + 
						"NA," + 
						"NA");
			}


			this.getGroup().getMySwarm().getHooper2().getHooperOut().recordHooperPGStats(header, data);
		}
	}

	/** Returns the benefit for a group of a given size. */
	public double calcBenefit(int group_size) {
		return this.getGroup().getMySwarm().getKg_day() * this.getGroup().getMySwarm().getB_benefit() * Math.pow(Math.E, (-(this.getGroup().getMySwarm().getGroup_benefit_growth_rate()) / group_size));
	}

	public double getTotalLeaderDefectionSanctionCosts() {
		return totalLeaderDefectionSanctionCosts;
	}

	public void setTotalLeaderDefectionSanctionCosts(
			double totalLeaderDefectionSanctionCosts) {
		this.totalLeaderDefectionSanctionCosts = totalLeaderDefectionSanctionCosts;
	}

	public double getTotalLeaderTaxCollection() {
		return totalLeaderTaxCollection;
	}

	public void setTotalLeaderTaxCollection(double totalLeaderTaxCollection) {
		this.totalLeaderTaxCollection = totalLeaderTaxCollection;
	}

	/**
	 * @return the totalLeaderTaxSanctionCosts
	 */
	public double getTotalLeaderTaxSanctionCosts() {
		return totalLeaderTaxSanctionCosts;
	}

	/**
	 * @param totalLeaderTaxSanctionCosts the totalLeaderTaxSanctionCosts to set
	 */
	public void setTotalLeaderTaxSanctionCosts(double totalLeaderTaxSanctionCosts) {
		this.totalLeaderTaxSanctionCosts = totalLeaderTaxSanctionCosts;
	}

	/**
	 * @return the group
	 */
	public BeyondGroup getGroup() {
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(BeyondGroup group) {
		this.group = group;
	}

	public int getPerCapitaBenefitSize() {
		return perCapitaBenefitSize;
	}

	public void setPerCapitaBenefitSize(int totalBenefitSize) {
		this.perCapitaBenefitSize = totalBenefitSize;
	}

	/** Shuffles the group list.  Uses a REPAST random seed to ensure repeatability of results */
	protected void shuffleAgents(ArrayList<?> cloneList) {
		Collections.shuffle(cloneList, new java.util.Random(Village.uniformIntRand(0, Integer.MAX_VALUE)));		
	}
}
