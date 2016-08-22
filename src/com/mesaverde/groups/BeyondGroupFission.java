package com.mesaverde.groups;



//what I need to do; have group identify themself and their subordinates as Group a, have Group b be the dominant group and its dominants. Then calculate a fission

public class BeyondGroupFission {
	private BeyondHooperMaster hooper2;
	
	public BeyondGroupFission(BeyondHooperMaster myHooper) {
		this.setHooper2(myHooper);
	}
	// This method follows the logic developed in BeyondGroupFight and allows groups
	// to fission. Group A is the focal (simple) group. Group B is Group A's dominant group.
	// Group A calculates the probability that, if they leave the hierarchy, group B will
	// beat them in a fight, and generates a random outcome based on that probability.
	// First group A fissions. Then they calculate their fight test from the method BeyondFight.
	// If they will not be successful in their fission, they merge back with group B.


	public void fissionComplexGroups(BeyondGroup a) {

		//		if (a.getDomGroup()==null){
		//			System.out.println("Danger Will Robinson! No dom group!");
		//		}

		if (a.getDomGroup()!=null){
			//first we temporarily fission our groups
			BeyondGroup b = a.getDomGroup();
			a.setDomGroup(null);
			b.getSubGroups().remove(a);

			// Does group A win?
			Boolean aWin = this.getHooper2().getMergeFight().getFight().fightTest(a, b);

			// If Group A doesn't win, they merge back right away
			if(aWin==Boolean.FALSE) {
				a.setDomGroup(b);
				b.getSubGroups().add(a);
//				System.out.println("in BG_Fission, line 33, we didn't win so we're re-merging. Group # " + a );

			}
			if(aWin==Boolean.TRUE) {
//				System.out.println("in BG_Fission, line 37, we DID win so we fissioned! Group # " + a );

			}
		}

	}


	public BeyondHooperMaster getHooper2() {
		return hooper2;
	}


	public void setHooper2(BeyondHooperMaster hooper2) {
		this.hooper2 = hooper2;
	}
}
//end of the fission function. 




