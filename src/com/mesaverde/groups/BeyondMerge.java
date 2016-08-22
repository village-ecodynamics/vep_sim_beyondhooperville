package com.mesaverde.groups;

public class BeyondMerge {

	private boolean head = true;
	private BeyondMergeFight mergeFight;
	
	public BeyondMerge(BeyondMergeFight mergeFight){
		this.setMergeFight(mergeFight);
		this.setHead(true);
	}

	public void mergeGroups(BeyondGroup a, BeyondGroup b){
		
		//		System.out.println("Group a size: " + a.getSize());
		//		System.out.println("Group b size: " + b.getSize());
		//		System.out.println("Group a empty: " + a.getMembers().isEmpty());
		//		System.out.println("Group b empty: " + b.getMembers().isEmpty());

		if(a.getMembers().isEmpty() || b.getMembers().isEmpty()){
			System.err.println("Group to be merged is empty! Skipping group.");
			System.exit(1);
			return;
		}
		
		if(b.getDomGroup() != null) return;

		b.setDomGroup(a); 
		a.getSubGroups().add(b);
		
		if(this.getMergeFight().getHooper2().getMySwarm().isOutput()){
			int worldtime = this.getMergeFight().getHooper2().getMySwarm().getWorldTime();

			String header = ("Year," +
					"DomGroup," +
					"SubGroup," +
					"DomFighterCount," +
					"SubFighterCount" +
					"\n");


			String data = (
					worldtime + "," + 
							a.getID() + "," + 
							b.getID() + "," + 
							a.getNetFighters().size() + "," + 
							b.getNetFighters().size());

			if(head){
				this.getMergeFight().getHooper2().getHooperOut().recordMergeStats(header, data, true);
				this.setHead(false);
			}else{
				this.getMergeFight().getHooper2().getHooperOut().recordMergeStats(header, data, false);
			}

		}
		return;
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
