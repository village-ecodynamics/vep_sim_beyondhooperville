package com.mesaverde.groups;

import java.util.BitSet;

import com.mesaverde.village.Village;

public class OrganizationPreference implements Cloneable {
    protected BeyondHooperAgentModelSwarm mySwarm;
	protected BitSet data = new BitSet(8);
	/* The value of data in this class: Represents the data in bit form (on, off).
	 * Bit 0: (H, NH)
	 * 1: (ALLC, RC)
	 * 2: (punished defector, unpunished defector)
	 * 3: (MM, nil)
	 * 4: (ALLT, RT)
	 * 5: (punished tax cheat, unpunished tax cheat)
	 * 6: (L, UL)
	 * ...plus a junk bit for possible later use.
	 */	
	
    protected double leadershipTaxRate;
	
	public OrganizationPreference(int seed, BeyondHooperAgentModelSwarm swarm) {
		this.setMySwarm(swarm);
        data = new BitSet(8);

        setBitsFor(data, seed);
		
        // get a tax rate if willing to lead, it will be between the min and max tax rates
		this.setTaxRate(Village.uniformDblRand(this.getMySwarm().getTax_rate_min(), this.getMySwarm().getTax_rate_max()));
        
		setPunishmentFlags();
	}
	
	public OrganizationPreference(BitSet b) {
		this.data = b;
		
		setPunishmentFlags();
	}
	
	private void setPunishmentFlags() {		
		setPunishedDefector(isALLC());
		setPunishedTaxCheat(isALLT());		
	}
	
	public boolean isH() {
		return data.get(0);
	}
	
	public void setH(boolean value) {
		data.set(0,value);
	}
	
	public boolean isALLC() {
		return data.get(1);
	}
	
	public boolean isUnpunishedDefector() {
		return !data.get(2);
	}
	
	public boolean isMM() {
		return data.get(3);
	}
	
	public boolean isALLT() {
		return data.get(4);
	}
	
	public boolean isUnpunishedTaxCheat() {
		return !data.get(5);
	}
	
	public boolean isL() {
		return data.get(6);
	}

	public void setPunishedDefector(boolean b) {
		data.set(2, b);		
	}
	
	public void setPunishedTaxCheat(boolean b) {
		data.set(5, b);		
	}

    public String toString() {
    	String h = isH() ? "H" : "NH";
    	String allc = isALLC() ? "ALLC" : "RC";
    	String defector = isUnpunishedDefector()  ? "unpunishedDefector" : "punishedDefector";
    	String mm = isMM() ? "MM" : "nil";
    	String ally = isALLT() ? "ALLT" : "RT";
    	String taxcheat = isUnpunishedTaxCheat()  ? "unpunishedTaxCheat" : "punishedTaxCheat";
    	String l = isL()  ? "L" : "UL";

    	String type = h + "." + allc + "." + defector + "." + mm + "." + ally + "." + taxcheat + "." + l;
    	
        return type;
    }

    BitSet getBitSet() {
        return data;
    }

    public void setTaxRate(double taxRate) {	
		leadershipTaxRate = taxRate;
	}

    public double getTaxRate() {
		return leadershipTaxRate;
	}

    public BeyondHooperAgentModelSwarm getMySwarm() {
		return mySwarm;
	}

	public void setMySwarm(BeyondHooperAgentModelSwarm mySwarm) {
		this.mySwarm = mySwarm;
	}

	public void setBitsFor(BitSet data, long seed) {
         // set the bits based on the seed
        int pos = 0;

        while (seed > 0 && pos < data.size()) {
            if (seed % 2 == 1L)
                data.set(pos);

            pos++;

            if (seed <= 0)
                break;
            seed = seed >>> 1;
        }
    }
    
	public OrganizationPreference copy() {
		OrganizationPreference copy = new OrganizationPreference((BitSet) data.clone());
        copy.setTaxRate(getTaxRate());
		
		return copy;
	}
}