package com.mesaverde.village;
import com.mesaverde.model.*;

public class EligibleRecord {
    public int HHTag;
    public Agent ptrHH;
    public int NumberOfEligibleBachelors;
    public Individual individual;
    public int cellx;
    public int celly;
    public Cell ptrCell;
    public int Remarriage;	// 0 = no, 1=yes

    public AgentModelSwarm mySwarm;
	
	public int getAge() {
		return individual.getAge();
	}
	
	public int getGender() {
		return individual.getGender();
	}

	public int getTag() {
		return individual.getTag();
	}
}
