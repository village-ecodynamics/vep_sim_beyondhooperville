package com.mesaverde.groups;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

//import com.mesaverde.groups.BeyondGroupVisualization;
import com.mesaverde.groups.BeyondHooperAgent;
import com.mesaverde.groups.BeyondHooperAgentModelSwarm;
import com.mesaverde.groups.BeyondGroup;
import com.mesaverde.groups.BeyondGroupFission;

public class BeyondHooperMaster {

	private BeyondHooperAgentModelSwarm mySwarm;
	private BeyondMergeFight mergeFight;
	private BeyondGroupFission fission;
	private HashSet<BeyondGroup> groups;
	private HashSet<BeyondGroup> mergedOrFought;

//	private BeyondGroupVisualization groupVisualization;
	private BeyondHooperOutput hooperOut;
	public boolean tributeHead = true;

	public BeyondHooperMaster(BeyondHooperAgentModelSwarm swarm) {
		setMySwarm(swarm);
		setMergeFight(new BeyondMergeFight(this));
		setFission(new BeyondGroupFission(this));
		this.groups = new HashSet<BeyondGroup>();
		this.mergedOrFought = new HashSet<BeyondGroup>();

		if (this.getMySwarm().isOutput()) {
			hooperOut = new BeyondHooperOutput();
			hooperOut.setHooper2(this);
			hooperOut.init();
			this.setTributeHead(true);
		}
	}

	public void clean(){
		purgeDeadMembers();
		purgeZeroGroups();
		cleanDomSubGroups();
	}

	public void execute() {

		resetGroups();

		clean();

		recalculateTerritories();

		fissionGroups();

		clean();

		setGroupTypes();

		clean();

		if (this.getMySwarm().isMerge_and_fight()) {
			clean();

			mergeAndFight();
		}

		if (this.getMySwarm().isHooper_pg_game()) {
			clean();

			hooperGame();
		}

		if (this.getMySwarm().isRevolt()) {
			clean();
			fissionComplexGroups();
////			System.out.println("Running method fissionComplexGroups, line 78 of Master");
		}

		if (this.getMySwarm().isMerge_and_fight()) {
			clean();

			payDomGroup();
		}


		if (this.getMySwarm().isOutput()) {
			clean();

			outputStats();
		}

		resetGroupFrustrations();

	}

	private void resetGroups() {
		ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
		cloneList.addAll(groups);
		Collections.shuffle(cloneList);
		for (BeyondGroup g : cloneList) {
			g.resetStates();
		}
	}

	private void resetGroupFrustrations() {
		ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
		cloneList.addAll(groups);
		Collections.shuffle(cloneList);
		for (BeyondGroup g : cloneList) {
			g.resetGroupFrustrations();
		}
	}


	private void recalculateTerritories() {
		ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
		cloneList.addAll(groups);
		Collections.shuffle(cloneList);
		for (BeyondGroup g : cloneList) {
			g.updateTerritory();
		}
	}

	private void fissionGroups() {
		ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
		cloneList.addAll(groups);
		for (BeyondGroup g : cloneList) {
			g.checkFission();
		}
	}

	private void setGroupTypes() {
		ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
		cloneList.addAll(groups);
		for (BeyondGroup g : cloneList) {
			g.changeOrganizationType();
		}
	}


	private void outputStats() {
		hooperOut.recordStats();
	}

//	private void outputTerritories() {
//		hooperOut.recordTerritories();
//	}

//	public void initHooperDisplay() {
//		// hooperDisplay = new BeyondHooperViewer("cool");
//		// I ADDED THIS - Andrew
//		groupVisualization = new BeyondGroupVisualization(
//				mySwarm.getWorldXSize(), mySwarm.getWorldYSize());
//		groupVisualization.setHooper2(this);
//
//	}

	private void mergeAndFight() {
		// Clear the list of groups that have merged or fought this year
		this.mergedOrFought.clear();


		ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
		cloneList.addAll(groups);

		for (BeyondGroup focal : cloneList) {
			if (this.mergedOrFought.contains(focal))
				continue;

			clean();

			// Retain only those who aren't dead
			this.mergedOrFought.retainAll(this.groups);

			ArrayList<BeyondGroup> frustrators = focal.getRankedFrustrators(this.groups, this.mergedOrFought);

			if(frustrators==null)
				continue;
			// System.out.println("Groups after purge: " +
			// this.getGroups().size());

			for(BeyondGroup frustrator : frustrators){
				if (this.groups.contains(frustrator)){
					// If a merge or fight is completed...
					if (this.getMergeFight().attemptMergeOrFight(focal, frustrator)) {

						clean();

						this.mergedOrFought.add(focal);
						this.mergedOrFought.add(frustrator);

						// Retain only those who aren't dead
						this.mergedOrFought.retainAll(this.groups);

						break;
					}
				}
			}

		}

		// System.out.println("Groups before purge: " +
		// this.getGroups().size());
		// Overkill, but for good measure...
		clean();
		// System.out.println("Groups after purge: " + this.getGroups().size());
		//		System.out.println();
	}

	private void hooperGame() {
		ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
		cloneList.addAll(groups);
		for (BeyondGroup g : cloneList) {
			g.getPGGame().runGame();
		}

	}

	private void fissionComplexGroups() {

//		System.out.println("Before paying our dominant groups we shuffle and fission");

		ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
		cloneList.addAll(groups);
		Collections.shuffle(cloneList);

		for (BeyondGroup focal : cloneList) {
			this.getFission().fissionComplexGroups(focal); 
		}
	}


	private void payDomGroup() {
		ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
		cloneList.addAll(groups);
		for (BeyondGroup g : cloneList) {
			g.payDomGroup();
		}
	}	

//	public void drawGroups() {
//		recalculateTerritories();
//		groupVisualization.repaint();
//	}


	/**
	 * @return the mySwarm
	 */
	public BeyondHooperAgentModelSwarm getMySwarm() {
		return mySwarm;
	}

	/**
	 * @param mySwarm
	 *            the mySwarm to set
	 */
	public void setMySwarm(BeyondHooperAgentModelSwarm mySwarm) {
		this.mySwarm = mySwarm;
	}

	public BeyondHooperOutput getHooperOut() {
		return hooperOut;
	}

	public void setHooperOut(BeyondHooperOutput hooperOut) {
		this.hooperOut = hooperOut;
	}

	/**
	 * @return the groups
	 */
	public HashSet<BeyondGroup> getGroups() {
		return groups;
	}

	/**
	 * @param groups
	 *            the groups to set
	 */
	public void setGroups(HashSet<BeyondGroup> groups) {
		this.groups = groups;
	}

	public HashSet<BeyondGroup> getMergedOrFought() {
		return mergedOrFought;
	}

	public void setMergedOrFought(HashSet<BeyondGroup> mergedOrFought) {
		this.mergedOrFought = mergedOrFought;
	}

	public BeyondMergeFight getMergeFight() {
		return mergeFight;
	}


	public void setMergeFight(BeyondMergeFight mergeFight) {
		this.mergeFight = mergeFight;
	}

	/**
	 * @param groups
	 *            the groups to set
	 */
	public void purgeZeroGroups() {
		@SuppressWarnings("unchecked")
		HashSet<BeyondGroup> cloneList = (HashSet<BeyondGroup>) groups.clone();

		for (BeyondGroup g : cloneList) {
			if (g.getMembers().isEmpty()) {
				this.getGroups().remove(g);
				g = null;
			}
		}
	}

	/**
	 * @param groups
	 *            the groups to set
	 */
	public void cleanDomSubGroups() {
		for (BeyondGroup g : groups) {
			if(g.getDomGroup() != null && !groups.contains(g.getDomGroup())){
				g.setDomGroup(null);
			}
			g.subGroups.retainAll(groups);
		}
	}

	/**
	 * @param groups
	 *            the groups to set
	 */
	public void purgeDeadMembers() {
		@SuppressWarnings("unchecked")
		HashSet<BeyondGroup> cloneList = (HashSet<BeyondGroup>) groups.clone();

		for (BeyondGroup g : cloneList) {
			g.purgeDeadMembers();
		}
	}

	/**
	 * @param groups
	 *            the groups to set
	 */
	public BeyondGroup isInGroupTerritory(int x, int y) {
		Point point = new Point(x,y);

		for (BeyondGroup g : groups) {
			if (g.getTerritory() == null)
				continue;

			if(g.getTerritory().contains(point))
				return g;
		}
		return null;
	}

	/**
	 * @param groups
	 *            the groups to set
	 */
	public BeyondGroup pathCrossesGroupTerritory(int fromX, int fromY, int toX,
			int toY) {
		Line2D.Double line = new Line2D.Double(fromX, fromY, toX, toY);

		for (BeyondGroup g : groups) {
			if (g.getTerritory() == null)
				continue;
			if (g.getTerritory().intersects(line))
				return g;
		}

		return null;
	}

	/**
	 * @param groups
	 *            the groups to set
	 */
	public BeyondGroup newTerritoryIntersectsGroupTerritory(
			BeyondHooperAgent agent, int toX, int toY) {

		// first, create an imaginary territory
		HashSet<Point> points = new HashSet<Point>(agent.getGroup()
				.getMembers().size());

		for (BeyondHooperAgent mem : agent.getGroup().getMembers()) {
			if (agent == mem)
				continue;
			Point temp = new Point(mem.getX(), mem.getY());
			points.add(temp);
		}

		Point temp = new Point(toX, toY);
		points.add(temp);

		Territory t1 = new Territory(points, true);

		for (BeyondGroup g : groups) {
			if (agent.getGroup() == g)
				continue;

			if (Territory.overlaps(t1, g.getTerritory()))
				return g;
		}
		return null;
	}

	/**
	 * @param groups
	 *            the groups to set
	 */
	public BeyondGroup generateNewGroup() {
		BeyondGroup group = new BeyondGroup();
		group.setMySwarm(this.getMySwarm());
		group.setBirthyear(this.getMySwarm().getWorldTime());
		groups.add(group);
		return group;
	}

	public BeyondGroupFission getFission() {
		return fission;
	}

	public void setFission(BeyondGroupFission fission) {
		this.fission = fission;
	}

	public boolean isTributeHead() {
		return tributeHead;
	}

	public void setTributeHead(boolean tributeHead) {
		this.tributeHead = tributeHead;
	}
}
