package com.mesaverde.groups;

import com.mesaverde.groups.BeyondFrustration.Frustration;
import com.mesaverde.village.Agent;
import com.mesaverde.village.Individual;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.math3.ml.clustering.*;
import org.apache.commons.math3.stat.StatUtils;



/**
 *
 * @author Bocinsky
 */
public class BeyondGroup{
	private BeyondHooperAgentModelSwarm mySwarm;
	
	protected HashSet<BeyondHooperAgent> members;

	private BeyondHooperAgent leader;

	protected Territory territory;

	protected HashSet<BeyondGroup> aggressors;

	protected HashSet<BeyondGroup> alliances;

	protected BeyondGroup domGroup;

	protected HashSet<BeyondGroup> subGroups;

	protected Color color;

	protected boolean hurt;

	protected int birthyear;

	private BeyondHooperGame PGGame;

	private static Long GROUP_ID = 1l;

	static void resetID() {
		BeyondGroup.GROUP_ID = 1L;
	}

	private Long ID = GROUP_ID++;
	private Long lineageID;

	public BeyondGroup() {
		this.PGGame = new BeyondHooperGame(this);
		this.members = new HashSet<BeyondHooperAgent>();
		this.aggressors = new HashSet<BeyondGroup>();
		this.alliances = new HashSet<BeyondGroup>();
		this.subGroups = new HashSet<BeyondGroup>();
		this.domGroup = null;
		
		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();

		this.color = new Color(r,g,b);
		this.resetStates();
		this.lineageID = this.ID;
	}

	/** Sets the leader of the group.
	 * You must ensure that the agent is already in the group (call setGroup first)
	 * before you try to make them leader.
	 * @param ha
	 */
	public void setLeader(BeyondHooperAgent ha) {
		leader = ha;        
	}


	public BeyondHooperAgent getLeader() {
		return leader;
	}

	public void addMember(BeyondHooperAgent ha) {
		members.add(ha);
		this.updateTerritory();
	}

	public void removeMember(BeyondHooperAgent ha) {
		members.remove(ha);
		this.updateTerritory();
	}


	public int getSize() {
		return members.size();
	}

	/** Returns the list of members */
	public HashSet<BeyondHooperAgent> getMembers() {
		return members;
	}

	/** Chooses the L agent within the group that has the cheapest tax rate.
	 * If there are no possible leaders, then the leader will be set to null.
	 * In that case, it's likely the H group will be disbanded.
	 */
	void chooseLeader() {
		BeyondHooperAgent willLead = null;
		double bestRate = 0d;

		for (BeyondHooperAgent ha : getMembers()) {
			if (ha.getPreference().isL()) {
				if (willLead == null || ha.getPreference().getTaxRate() < bestRate) {
					bestRate = ha.getPreference().getTaxRate();
					willLead = ha;
				}
			}
		}

		// If no leader has been found, force a random agent to be the leader.
		if(willLead==null){
			//			System.out.println(getMembers());
			//			System.out.println();
			for (BeyondHooperAgent ha : getMembers()) {
				willLead = ha;
				break;
			}
		}

		setLeader(willLead);
	}

	// Stef: here we are trying to keep track of which groups have done us wrong. 
	//This bit pulls on BeyondExchange and a method in BeyondMergeFightCheck 
	//where we actually write the array list. Here we just start it.


	public HashSet<Individual> getFighters()
	{
		/* this function is meant to get the number of male
		 * members in the group, used in calculating outcome of 
		 * a fight.*/
		HashSet<Individual> fighters = new HashSet<Individual>();
		for (Agent a : members) {
			//			System.out.println("Family Unit: " + a.getFamilyUnit());
			//			System.out.println("members: " + a.getFamilyUnit().getMembers());
			a.getFamilyUnit().claimNullIndividuals();
			//			a.getFamilyUnit().flushNullIndividuals();

			for (Individual ind : a.getFamilyUnit().getAllIndividuals()) {
				//				System.out.println("Individual: " + ind + ", Family Unit: " + ind.getFamily());

				if(this.getMySwarm().isMen_only_fight() && ind.getGender()!=Individual.MALE)
					continue;

				if (ind.getAge() >= this.getMySwarm().getMin_fight_age() && ind.getAge() <= this.getMySwarm().getMax_fight_age()) 
					fighters.add(ind);

			}
		}
		return fighters;
	}

	// Gets a list of fighters that includes all fighters in a group's dominant and 
	// subordinate groups.
	public ArrayList<Individual> getNetFighters(){
		ArrayList<Individual> fighters = new ArrayList<Individual>();
		fighters.addAll(this.getFighters());

		// Iterate through dom groups
		if(this.getDomGroup()!=null){
			fighters.addAll(this.getDomGroup().getFighters());
		}

		// Iterate through sub groups
		Iterator<BeyondGroup> it = this.getSubGroups().iterator();
		while(it.hasNext()){
			fighters.addAll(it.next().getFighters());
		}

		return fighters;
	}

	public HashSet<Individual> getIndividuals()
	{
		/* this function is meant to get the number of male
		 * members in the group, used in calculating outcome of 
		 * a fight.*/
		HashSet<Individual> individuals = new HashSet<Individual>();
		for (Agent a : members) {
			individuals.addAll(a.getFamilyUnit().getAllIndividuals());

		}
		return individuals;
	}

	public HashSet<Frustration> getGroupFrustrations(){
		this.setHurt(false);
		HashSet<Frustration> frustrations = new HashSet<Frustration>();

		for(BeyondHooperAgent a : members){
			if(!a.getBeyondFrustration().getFrustrations().isEmpty()){
				frustrations.addAll(a.getBeyondFrustration().getFrustrations());
				if(a.getBeyondFrustration().isHurt()){
					this.setHurt(true);
				}
			}
		}

		return frustrations;
	}

	public void resetGroupFrustrations(){
		this.setHurt(false);
		for(BeyondHooperAgent a : members){
			a.getBeyondFrustration().frustrations.clear();
		}
	}

	public ArrayList<BeyondGroup> getRankedFrustrators(HashSet<BeyondGroup> allGroups, HashSet<BeyondGroup> preoccupiedGroups){
		this.setHurt(false);
		HashSet<Frustration> frustrations = this.getGroupFrustrations();

		if(this.isHurt() && frustrations.size()==0){
			System.err.println("PROBLEM! Group hurt without frustrations.");
			System.exit(1);
		}

		if(this.isHurt()){
			HashMap<BeyondGroup,Integer> groupFrustrations = new HashMap<BeyondGroup,Integer>();

			for(Frustration f : frustrations){
				if(groupFrustrations.containsKey(f.getFrustrator())){
					groupFrustrations.put(f.getFrustrator(), groupFrustrations.get(f.getFrustrator())+1);
				}else{
					groupFrustrations.put(f.getFrustrator(),1);
				}
			}

			ArrayList<BeyondGroup> frustrators = new ArrayList<BeyondGroup>(groupFrustrations.size());
			int[] allFrustrations = new int[groupFrustrations.size()];

			int i = 0;
			Iterator<Entry<BeyondGroup, Integer>> it = groupFrustrations.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<BeyondGroup,Integer> pairs = (Map.Entry<BeyondGroup,Integer>)it.next();
				frustrators.add(pairs.getKey());
				allFrustrations[i] = pairs.getValue();
				i++;
			}
			//			System.out.println("Focal: " + this);
			//			System.out.println("Frustrators: " + frustrators);
			int totalFrustration = 0;
			for(int f : allFrustrations){
				totalFrustration += f;
			}
			//			System.out.println("frustrations: " + Arrays.toString(allFrustrations));
			double[] pFrustrations = new double[frustrators.size()];

			for(i=0 ; i < pFrustrations.length ; i++){
				pFrustrations[i] = ((double) allFrustrations[i])/((double) totalFrustration);
			}
			//			System.out.println("pFrustrations: " + Arrays.toString(pFrustrations));
			double[] distances = new double[frustrators.size()];
			for(i=0 ; i < distances.length ; i++){
				distances[i] = Territory.getDistance(this, frustrators.get(i));
			}
			//			System.out.println("distances: " + Arrays.toString(distances));
			double totalDistance = 0;
			for(i=0 ; i < distances.length ; i++){
				totalDistance += 1/distances[i];
			}

			double[] pDistances = new double[distances.length];

			for(i=0 ; i < pDistances.length ; i++){
				pDistances[i] = (1/distances[i])/totalDistance;
			}
			//			System.out.println("totalDistance: " + totalDistance);
			//			System.out.println("pDistances: " + Arrays.toString(pDistances));
			double[] allP = new double[distances.length];
			for(i=0 ; i < allP.length ; i++){
				allP[i] = pDistances[i] * pFrustrations[i];
			}


			//			System.out.println("finalP: " + Arrays.toString(allP));
			ArrayList<Frustrator> frustratorsFinal = new ArrayList<Frustrator>(allP.length);
			for(i=0 ; i < allP.length ; i++){
				frustratorsFinal.add(new Frustrator(frustrators.get(i),(int)(allP[i] * 10000)));
			}

			Collections.shuffle(frustratorsFinal);
			Collections.sort(frustratorsFinal);

			ArrayList<BeyondGroup> outFrustrators = new ArrayList<BeyondGroup>();
			for(Frustrator f : frustratorsFinal){
				outFrustrators.add(f.getGroup());
			}
			//			System.out.println("Ranked frustrators: " + outFrustrators + '\n');
			//			System.out.println("Group " + this + " frustrators: ");
			//			for(Frustrator frustrator : frustrators){
			//				System.out.println(frustrator.group + ": " + frustrator.getFrustrations());
			//			}
			//			System.out.println("\n");

			//			System.out.println("Frustrators before: " + frustrators.size());
			//			System.out.println("Preoccupied: " + preoccupiedGroups.size());
			outFrustrators.removeAll(preoccupiedGroups);

			if (this.getMySwarm().isAlliances()) {
				outFrustrators.removeAll(alliances); }

			return(outFrustrators);
		}
		return null;
	}

	public class Frustrator implements Comparable<Frustrator>{
		BeyondGroup group;
		int frustrations;

		public Frustrator(BeyondGroup group, int frustrations){
			setGroup(group);
			setFrustrations(frustrations);
		}

		public BeyondGroup getGroup() {
			return group;
		}

		public void setGroup(BeyondGroup group) {
			this.group = group;
		}

		public int getFrustrations() {
			return frustrations;
		}

		public void setFrustrations(int frustrations) {
			this.frustrations = frustrations;
		}

		public int compareTo(Frustrator frustrator) {

			int compareQuantity = ((Frustrator) frustrator).getFrustrations(); 

			//ascending order
			//			return this.potentialEnergy - compareQuantity;

			//descending order
			return compareQuantity - this.frustrations;
		}
	}

	//I ADDED THIS: - Andrew
	public void changeOrganizationType()
	{
		/* "Hey Andrew. I've got a quick task for you. 
		 * Would you mind adding a method to the BeyondGroup 
		 * class that polls all of the agents as to whether 
		 * they want to be H or NH, and if there are more H 
		 * members than NH, elects a leader (using the 
		 * -chooseLeader function)? If there are more NH 
		 * agents, leader should be set to null. In case of 
		 * ties, just flip a coin." - Kyle
		 */
		int hVote = 0;
		int nhVote = 0;

		for(BeyondHooperAgent a : members)
		{
			//if preference is H, then add to hVote (agent has hierarchical preference)
			if (a.getPreference().isH() == true)
				hVote++;
			//else, agent has non-hierarchical preference.
			else
				nhVote++;
		}
		//choose leader based on overall organization preference
		if (hVote > nhVote)
			chooseLeader();
		else if (hVote < nhVote)
			setLeader(null);
		else
		{
			Random rand = new Random();
			if (rand.nextFloat() > 0.5f)
				chooseLeader();
			else
				setLeader(null);
		}

	}

	public void resetStates(){
		this.setHurt(false);
		this.setLeader(null);
	}


	/**
	 * This is a new version of tribute payment, which now implements a
	 * "trickle up" tribute dynamic.
	 * 
	 * Tribute in this model consists of two separate tax rates:
	 * beta, which is the direct tax on the benefit from the public goods game, and
	 * mu, which is a "value-added tax", or a tax on the tribute a group received from 
	 * a its subordinates.
	 * 
	 * NOTE: This only applies to the situation where one has a single dominant group, 
	 * as implemented in Kohler, Crabtree, and Bocinsky 2015.
	 * 
	 * Kohler, Crabtree, and Bocinsky 2015 derive an equation for calculating 
	 * the total amount of tribute paid to a dominant, which is a function of the
	 * graph distance to each of the focal group's subordinates.
	 * 
	 * @param beta, the direct tax on the benefit from the public goods game
	 * @param mu, the tax on the tribute received
	 * @return The amount to be paid to one's dominant group.
	 */
	private double calcDomPayment(double beta, double mu) {
		// The amount to be payed
		double payment = 0.0;

		// Start by getting a list of subordinates, 
		// and simultaneously record their out-degree
		HashMap<BeyondGroup,Integer> outNeighbors = new HashMap<BeyondGroup,Integer>();

		// Create temporary lists to keep track of neighbors at each level
		HashSet<BeyondGroup> oldLevelNeighbors = new HashSet<BeyondGroup>();
		HashSet<BeyondGroup> newLevelNeighbors = new HashSet<BeyondGroup>();

		// Create a level counter, and initialize it at 0
		int level = 0;

		// Add the focal group to each list
		outNeighbors.put(this, level);
		oldLevelNeighbors.add(this);
		newLevelNeighbors.add(this);

		// Get the subordinates of the groups at the old level
		while(newLevelNeighbors.size() > 0){
			level++;

			newLevelNeighbors.clear();

			// Get the subordinates of all the old level neighbors
			for(BeyondGroup group : oldLevelNeighbors){
				for(BeyondGroup neigh : group.getLocalNeighbors("out")){
					newLevelNeighbors.add(neigh);
					outNeighbors.put(neigh,level);
				}
			}

			oldLevelNeighbors.clear();
			oldLevelNeighbors.addAll(newLevelNeighbors);
		}

		for (HashMap.Entry<BeyondGroup,Integer> subSet : outNeighbors.entrySet()){
			double subPG = subSet.getKey().getPGGame().getPerCapitaBenefitSize();
			int subSize = subSet.getKey().getSize();

			double subPayAmount = ((double)subPG*subSize) * (double)Math.pow(mu, subSet.getValue());
			payment += subPayAmount;
			//			System.out.printlsn("Subordinate: " + subSet.getKey() + "\n subPG: " + subPG + "\n subSize: " + subSize + "\n level: " + subSet.getValue() + "\n subPayAmount: " + subPayAmount + "\n");
		}

		payment = payment * beta;

		return(payment);
	}

	public void payDomGroup(){
		double payment = this.calcDomPayment(this.getMySwarm().getBeta(), this.getMySwarm().getMu());

		// If you don't have a dom group, no tribute necessary!
		if(this.getDomGroup()==null) return;

		double perCapitaTributeAmount = payment/((double) this.getSize());

		double totalTribute = 0.0;

		for(BeyondHooperAgent ha : this.getMembers()){
			double trib = perCapitaTributeAmount<=ha.getMaizeStorage()?perCapitaTributeAmount:ha.getMaizeStorage();
			ha.setMaizeStorage((int)((double)ha.getMaizeStorage()-trib));
			totalTribute += trib;
		}

		double domPerCapitaTake = totalTribute / ((double) this.getDomGroup().getSize());

		for (BeyondHooperAgent ha : this.getDomGroup().getMembers()) {
			ha.setMaizeStorage((int) (ha.getMaizeStorage() + domPerCapitaTake));
		}

		if(this.getMySwarm().isOutput()){
			//		System.out.println("Recording tribute collection.");
			int worldtime = this.getMySwarm().getWorldTime();

			String header = ("Year," +
					"domGroup," +
					"subGroup," +
					"domGroupSize," +
					"subGroupSize," +
					"perCapitaTributeAmount," +
					"domPerCapitaTake," +
					"subPerCapitaBenefitSize," +
					"totalTribute\n");


			String data = (worldtime + "," + 
					this.getDomGroup().getID() + "," + 
					this.getID() + "," + 
					this.getDomGroup().getSize() + "," + 
					this.getSize() + "," + 
					perCapitaTributeAmount + "," + 
					domPerCapitaTake + "," + 
					this.getPGGame().getPerCapitaBenefitSize() + "," + 
					totalTribute);

			if(this.getMySwarm().getHooper2().isTributeHead()){
				this.getMySwarm().getHooper2().getHooperOut().recordTributeStats(header, data, true);
				this.getMySwarm().getHooper2().setTributeHead(false);
			}else{
				this.getMySwarm().getHooper2().getHooperOut().recordTributeStats(header, data, false);
			}
		}
	}

	public void updateTerritory(){
		this.territory = null;
		this.territory = new Territory(this.getMembers());
	}

	public void purgeDeadMembers(){
		@SuppressWarnings("unchecked")
		HashSet<BeyondHooperAgent> cloneList = (HashSet<BeyondHooperAgent>) members.clone();
		for(BeyondHooperAgent g : cloneList) {
			if(!g.isAlive())
				this.removeMember(g);
		}
	}

	public Territory getTerritory(){
		return this.territory;
	}

	public boolean isHierarchical(){
		if(this.getLeader() == null ){
			return false;
		}else{
			return true;
		}
	}

	public boolean isComplex(){
		if(this.getDomGroup() == null && this.getSubGroups().size() == 0){
			return false;
		}else{
			return true;
		}
	}

	public boolean isHurt() {
		return hurt;
	}

	public void setHurt(boolean hurt) {
		this.hurt = hurt;
	}

	/**
	 * @return the pGGame
	 */
	public BeyondHooperGame getPGGame() {
		return PGGame;
	}

	/**
	 * @param pGGame the pGGame to set
	 */
	public void setPGGame(BeyondHooperGame pGGame) {
		PGGame = pGGame;
	}

	public void checkFission(){
		int maxIterations = this.getSize();
		int i = 0;
		while(this.getSize() > this.getMySwarm().getGroup_size() & i < maxIterations){
			//			System.out.println("Group: " + this + ". Size: " + this.getSize() + ". Removing agents.");
			this.fission();
			//			System.out.println("New group size: " + this.getSize());
			System.out.println();
			i++;
		}
	}


	public void fission(){
		if (this.getMySwarm().isK_means_clustering()) {
		this.fission_kmeans();
		}
		else {
			this.fission_budoff();
		}
	}
	
	public void fission_kmeans(){

		if (this.getTerritory().territoryPoints.size() < 2) {
			return;
		}

		//		 transformer.cluster(this.getTerritory().territoryPoints);
//		System.out.println("Territory Points Size: " + this.getTerritory().territoryPoints.size());
		List<DoublePoint> points = new ArrayList<DoublePoint>(this.getTerritory().territoryPoints.size());
		Iterator<Point> it = this.getTerritory().territoryPoints.iterator();
		while(it.hasNext()) {
			Point thisIt = it.next();
			System.out.println(thisIt.getX());
			System.out.println(thisIt.getY());
			points.add(new DoublePoint(new double[]{thisIt.getX(),thisIt.getY()}));
		}


//		System.out.println("Points: " + points.size());


		KMeansPlusPlusClusterer<DoublePoint> transformer = new KMeansPlusPlusClusterer<DoublePoint>(2);
		List<CentroidCluster<DoublePoint>> clusters = transformer.cluster(points);

		Iterator<DoublePoint> pointList = clusters.get(0).getPoints().iterator();
		HashSet<Point> terrPoints = new HashSet<Point>(clusters.get(0).getPoints().size());
		while(pointList.hasNext()){
			DoublePoint p = pointList.next();
			terrPoints.add(new Point((int)p.getPoint()[0],(int)p.getPoint()[1]));
		}

		Territory terrFinal = new Territory(terrPoints,true);
		//		System.out.println("Cluster 0: " + clusters.get(0));
		//		System.out.println("Cluster 1: " + clusters.get(1));
		//		System.out.println("Cluster 0 members: " + clusters.get(0).getPoints());
		//		System.out.println("Cluster 1 members: " + clusters.get(1).getPoints());
		double[] membership = new double[this.getSize()];
		//		System.out.println("membership before: " + membership);



		Iterator<BeyondHooperAgent> member_it = this.getMembers().iterator();
		int i = 0;
		while(member_it.hasNext()){
			BeyondHooperAgent a = member_it.next();
			if(!terrFinal.contains(new Point(a.getX(),a.getY()))) membership[i] = (double) 1;
			i++;
		}
		//		System.out.println("membership after: " + membership);

		System.out.println("group size: " + this.getSize());

		//		for(i = 0; i < membership.length; i++){
		//			System.out.println("member " + i + ": " + membership[i]);
		//		}


		double mean = StatUtils.mean(membership);
		int leaveGroup = 0;
		if(mean < 0.5){
			leaveGroup = 1;
		}

		//		System.out.println("Mean: " + mean);

		HashSet<BeyondHooperAgent> faragents = new HashSet<BeyondHooperAgent>();
		for (BeyondHooperAgent agent : this.members) {
			if(leaveGroup == 0 & terrFinal.contains(new Point(agent.getX(),agent.getY()))){
				faragents.add(agent);
			}

			if(leaveGroup == 1 & !terrFinal.contains(new Point(agent.getX(),agent.getY()))){
				faragents.add(agent);
			}
		}

		//		System.out.println("agents leaving: " + faragents);




		BeyondGroup newgroup = null;
		@SuppressWarnings("unchecked")
		HashSet<BeyondHooperAgent> cloneList = (HashSet<BeyondHooperAgent>) faragents.clone();
		i = 0;
		for(BeyondHooperAgent g : cloneList) {

			if(i == 0) newgroup = g.getMySwarm().getHooper2().generateNewGroup();
			this.removeMember(g);
			g.setGroup(newgroup);
			newgroup.getNeighborhood();
			i++;


			//			newgroup.getAlliances().add(this);
			//			this.getAlliances().add(newgroup);
		}

		//		System.out.println("Old Group size: " + this.getSize());
		//		System.out.println("New Group size: " + newgroup.getSize());


		newgroup.setLineageID(this.getLineageID());
		newgroup.setDomGroup(this.getDomGroup());
	}



	public void fission_budoff() {

		System.out.println("This group is too big! " + this);
		//		System.out.println("Old size: " + this.getSize());
		Point2D.Double centroid = this.getTerritory().centroid();

		double distance = 0.0;
		BeyondHooperAgent faragent = null;
		for (BeyondHooperAgent agent : this.members) {
			double newDistance = centroid.distance(agent.getX(), agent.getY());
			//			System.out.println("Agent: " + agent + ", x: " + agent.getX() + ", y: " + agent.getY() + ", distance: " + newDistance);
			if(newDistance > distance){
				distance = newDistance;
				faragent = agent;
			}
		}

		HashSet<BeyondHooperAgent> faragents = new HashSet<BeyondHooperAgent>();
		faragents.add(faragent);

		for (BeyondHooperAgent agent : this.members) {
			//			System.out.println("Agent: " + agent);
			//			System.out.println("Faragent: " + faragent);
			if(agent!=faragent && agent.getX()==faragent.getX() && agent.getY()==faragent.getY()){
				faragents.add(agent);
			}
		}

		BeyondGroup newgroup = faragent.getMySwarm().getHooper2().generateNewGroup();
		@SuppressWarnings("unchecked")
		HashSet<BeyondHooperAgent> cloneList = (HashSet<BeyondHooperAgent>) faragents.clone();
		for(BeyondHooperAgent g : cloneList) {
			this.removeMember(g);
			g.setGroup(newgroup);
			newgroup.getNeighborhood();



			//			newgroup.getAlliances().add(this);
			//			this.getAlliances().add(newgroup);
		}

		newgroup.setLineageID(this.getLineageID());

		//		System.out.println("Alliance group: " + this.getSize());
		//				System.out.println("New group: " + newgroup);
		//				System.out.println("New group size: " + newgroup.getSize());
		//				System.out.println();


	}


	/**
	 * The centroid of the group is different than the geometric centroid of the territory.
	 * It is instead the average position of all agents in the group.
	 */
	public Point2D.Double groupCentroid(){
		double xsum = 0.0;
		double ysum = 0.0;

		for (BeyondHooperAgent agent : this.members) {
			xsum += agent.getX();
			ysum += agent.getY();
		}

		double xmean = (double) xsum/this.getSize();
		double ymean = (double) ysum/this.getSize();	

		//		System.out.println("Group Centroid: " + xmean + "," + ymean);

		return new Point2D.Double(xmean,ymean);

	}

	/**
	 * Chance that a group will join in an alliance 
	 * Affects Fight algorithm and Lanchesters Laws outcome 
	 */


	public Long getID() {
		return ID;
	}

	public Long getLineageID() {
		return lineageID;
	}

	public void setLineageID(Long lineageID) {
		this.lineageID = lineageID;
	}

	public Color getColor() {
		return color;
	}

	public int getBirthyear() {
		return birthyear;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public HashSet<BeyondGroup> getAggressors() {
		return aggressors;
	}

	public void setAggressors(HashSet<BeyondGroup> aggressors) {
		this.aggressors = aggressors;
	}

	public HashSet<BeyondGroup> getAlliances() {
		return alliances;
	}

	public void setAlliances(HashSet<BeyondGroup> alliances) {
		this.alliances = alliances;
	} 

	public BeyondGroup getDomGroup(){
		return domGroup;
	}
	public void setDomGroup(BeyondGroup group) {
		this.domGroup = group;
	}

	public HashSet<BeyondGroup> getSubGroups(){
		return subGroups;
	}
	public void setSubGroups(HashSet<BeyondGroup> subGroups) {
		this.subGroups = subGroups;
	}

	/**
	 * Get the graph neighbors of a group.
	 * 
	 * The request can be either directed ('out','in'), which gets subordinate groups
	 * or dominant groups, respectively, or 'all', which gets all groups
	 * 
	 * @param direction, one of either "all", "out", or "in"
	 * @return the directed or undirected neighbors of the group
	 */
	private HashSet<BeyondGroup> getLocalNeighbors(String direction){
		HashSet<BeyondGroup> neighbors = new HashSet<BeyondGroup>();

		if(direction=="all" || direction=="out"){
			neighbors.addAll(this.getSubGroups());
		}

		if((direction=="all" || direction=="in") && this.getDomGroup()!=null){
			neighbors.add(this.getDomGroup());
		}

		return neighbors;
	}


	public HashSet<BeyondGroup> getNeighborhood(){
		HashSet<BeyondGroup> neighbors = new HashSet<BeyondGroup>();
		HashSet<BeyondGroup> testedNeighbors = new HashSet<BeyondGroup>();
		HashSet<BeyondGroup> untestedNeighbors = new HashSet<BeyondGroup>();

		// Start with the focal group
		neighbors.add(this);
		// These are the initial nodes to be tested
		untestedNeighbors.addAll(neighbors);

		// A loop that iteratively searches for neighbors 
		// deeper and deeper into the neighborhood until
		// all neighbors have been located
		//		System.out.println("Getting neighbors for " + this);
		while(untestedNeighbors.size()>0){
			// Get the neighbors of each neighbor
			Iterator<BeyondGroup> it = untestedNeighbors.iterator();
			while(it.hasNext()){
				BeyondGroup neighbor = it.next();
				//				System.out.println(neighbor);
				HashSet<BeyondGroup> neighborNeighbors = neighbor.getLocalNeighbors("all");
				// Take the set difference, removing all neighbors we already know about
				neighborNeighbors.removeAll(testedNeighbors);
				// Add the remainder to the neighbor list
				neighbors.addAll(neighborNeighbors);
			}
			// add all of the now-tested neighbors to the testedNeighbors list
			testedNeighbors.addAll(untestedNeighbors);
			// clear the untestedNeighbors list
			untestedNeighbors.clear();
			// add all of the currently known neighbors
			untestedNeighbors.addAll(neighbors);
			// remove all of the now-tested neighbors
			untestedNeighbors.removeAll(testedNeighbors);
		}
		//		System.out.println("\n");
		return neighbors;
	}

	public void setBirthyear(int birthyear) {
		this.birthyear = birthyear;
	}

	public BeyondHooperAgentModelSwarm getMySwarm() {
		return mySwarm;
	}

	public void setMySwarm(BeyondHooperAgentModelSwarm mySwarm) {
		this.mySwarm = mySwarm;
	}

}
