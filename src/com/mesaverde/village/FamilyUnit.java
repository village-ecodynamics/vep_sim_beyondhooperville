package com.mesaverde.village;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/** Maintains information about individuals within a family (Agent).
 *
 */
public class FamilyUnit {
	private CopyOnWriteArraySet<Individual> members = new CopyOnWriteArraySet<Individual>();
	protected Individual husband, wife;
	protected Agent agent;
	
	public FamilyUnit(Agent agent) {
		this.agent = agent;
	}

    public void addNewChild(int age, int gend) {
		Individual ind = createIndividual(age, gend, Agent.pop_id);
		ind.setFamily(this);
		addPerson(ind);

		Agent.pop_id++;
	}

    protected Individual createIndividual(int age, int gender, int tag) {
    	return new Individual(age, gender, tag);
    }
    
    public void createParents(int wifeAge, int husbandAge, int wifeTag, int husbandTag) {
		// first value in ages array is for wife, second is for husband
		// set to a (=rand int between 17 & 30) in AgentModelSwarm.m -initWorld

		if (husbandTag == 0) {
			husbandTag = Agent.pop_id;
			Agent.pop_id++;
		}

		if (wifeTag == 0) {
			wifeTag = Agent.pop_id;
			Agent.pop_id++;
		}

		Individual wife = createIndividual(wifeAge, Individual.FEMALE, wifeTag);
		Individual husband = createIndividual(husbandAge, Individual.MALE, husbandTag);
		wife.setMarriageStatus(Individual.MARRIED);
		husband.setMarriageStatus(Individual.MARRIED);

		addPerson(wife);
		addPerson(husband);
		setWife(wife);
		setHusband(husband);
	}

	public synchronized void addPerson(Individual ind) {
		if (ind == null) return; // don't accept null
		
//		System.out.println("Adding Individual " + ind + " to Family Unit: " + this);
		
		// make sure they have been removed from their old family if they had one
		if (ind.getFamily() != null) {
            FamilyUnit fam = ind.getFamily();
            
            fam.removePerson(ind);
        }
		ind.setFamily(this);
		
//		System.out.println("Individual " + ind + " Family Unit: " + ind.getFamily() + '\n');
		if(ind.getFamily() != this){
			System.err.println("ERROR mutherfucker: individual's 'family' isn't this family!");
			}
		
		this.members.add(ind);
	}

	public boolean hasHusband() {
		return husband != null;
	}

	public boolean hasWife() {		
		return wife != null;
	}

	public int getWifesAge() {
		if (wife == null)
			return -1;
		
		return wife.getAge();
	}

	public int getHusbandsAge() {
		if (husband == null)
			return -1;
		
		return husband.getAge();
	}
	
	public int getCountAtOrAboveAge(int age) {
		int total = 0;
              
		synchronized (getMembers()) {
	        for (Individual ind : getMembers()) {
	                if (ind.getAge() >= age)
	                        total++;
	        }
		}
		
		return total;
	}

	public int getFamilySize() {
		return getMembers().size();
	}

	/** returns true if a family does not have any individuals above 0 years */
	public boolean isLegitimate() {
		return getCountAtOrAboveAge(1) > 0;
	}

	/* returns an array containing the agents of the individuals in the family.
	 * Changing the values in this array will NOT change the ages of the family members
	 */
	public int[] getAges() {
		int[] ages;
		
		synchronized (getMembers()) {
            ages = new int[getMembers().size()];
            int pos = 0;

            for (Individual ind : getMembers()) {
                    ages[pos++] = ind.getAge();
            }      
		}

            return ages;
	}
	
	public synchronized void removePerson(Individual ind) {
		if (ind == null) return;
		
//		int numMembers = this.members.size();
		
//		System.out.println("Removing individual: " + ind + " from family: " + this.members);
		
		EligibleRecord record = ind.getEligibleRecord();		

		if (record != null) {
			Eligible.removeFromEligibleList(record, agent.getMySwarm());
			ind.eligibleRecord = null;
		}
		
		if (ind == wife || ind == husband) {
			boolean isWife = false;
			
			if (ind == wife) {
				isWife = true;
				wife = null;
			}
			
			if (ind == husband)
				husband = null;
			
			if (ind == wife && husband != null) {
				husband.setMarriageStatus(Village.SINGLE);
			} else if (wife != null) {
				wife.setMarriageStatus(Village.SINGLE);
			}

			if (isWife) {
				Agent.fdeaths++;
				Agent.totalage += ind.getAge();

				Agent.fkids++;
				Agent.totalkids += agent.child_per_female;
			}

			Agent.tot_pop = Agent.tot_pop - 1;
			
			// fecundity control
			if (isWife) // female death, reset counts
			{
				agent.child_per_female = 0;
				agent.last_year_child_born = 0;
			}	
		}
		members.remove(ind);
		
//		this.getMembers().remove(ind);
		ind.setFamily(null);
//		
//		if(this.members.size() != (numMembers-1)){
//			System.err.println("AAAA!!! ");
//		}
//		System.out.println("Final family: " + this.members + '\n');
	}

	int promoteChild() {
		int flag = 0;

		if (wife != null || husband != null)
			return 0;
		
		// we know that there is no wife or husband, so we don't need to check for them anymore
		// what this does is promote one of the children to being the parent
		// I'm going to ensure though that it promotes the eldest child
		
		// first, find the eldest
		Individual eldest = null;
		for (Individual ind : getMembers()) {
			if (eldest == null || ind.getAge() > eldest.getAge()) {
				eldest = ind;
			}
		}
		
		// now promote the eldest if they are old enough
		if (eldest != null && eldest.getAge() >= 7) {
			if (eldest.getGender() == Individual.FEMALE)
				wife = eldest;
			else
				husband = eldest;
			
			eldest.setMarriageStatus(Village.SINGLE);
			flag = 1;
		}
		
		return flag;
	}

	public Individual getWife() {
		return wife;
	}

	public Individual getHusband() {
		return husband;
	}

	public void removeAllMembers() {
		for (Individual ind : new ArrayList<Individual>(getMembers())){
			ind.die();
			ind = null;
		}
		
		wife = null;
		husband = null;
	}

	public ArrayList<Individual> getKids() {
		ArrayList<Individual> kids = new ArrayList<Individual>(getMembers());

                if (wife != null)
                    kids.remove(wife);

                if (husband != null)
                    kids.remove(husband);
		
		return kids;
	}

	public void setWife(Individual wife) {
		this.wife = wife;
		
		if (!getMembers().contains(wife)) {
			addPerson(wife);
		}
		
		wife.setFamily(this);
	}

	public void setHusband(Individual husband) {
		this.husband = husband;
		
		if (!getMembers().contains(husband)) {
			addPerson(husband);
		}
		
		husband.setFamily(this);
	}

	public ArrayList<Individual> getAllIndividuals() {
		return new ArrayList<Individual>(getMembers());
	}

	public int getKidCount() {
		int parentCount = 0;
		
		if (wife != null) parentCount++;
		if (husband != null) parentCount++;
		
		return getMembers().size() - parentCount;
	}

	/** Creates and adds an individual with the given characteristics */
	public synchronized void addPerson(int age, int gender, int tag) {
		Individual ind = createIndividual(age, gender, tag);
		
		addPerson(ind);		
	}

	public Agent getAgent() {
		return agent;
	}

	public CopyOnWriteArraySet<Individual> getMembers() {
		return members;
	}

	public void setMembers(CopyOnWriteArraySet<Individual> members) {
		this.members = members;
	}
	
	public void flushNullIndividuals(){
		for(Individual ind : members){
			if(ind.getFamily()==null) members.remove(ind);
		}
	}

	public void claimNullIndividuals(){
		for(Individual ind : members){
			if(ind.getFamily()==null) ind.setFamily(this);
		}
	}
}
