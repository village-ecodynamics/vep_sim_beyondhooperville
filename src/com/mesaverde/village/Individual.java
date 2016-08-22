package com.mesaverde.village;

public class Individual {
	public static final int FEMALE = 0, MALE = 1;
	public static final int SINGLE = Village.SINGLE;
	public static final int MARRIED = Village.SINGLE + 1; // just to make sure they never clash

	protected FamilyUnit family;
	protected Agent parentHH;
	protected int age = -1, gender = -1, tag = 0;
	protected int marriageStatus = SINGLE;
	public EligibleRecord eligibleRecord = null;

	private int kinID = -1;

	public Individual(int age, int gender, int tag) {
		this.age = age;
		this.gender = gender;
		this.tag = tag;
	}

	public void setFamily(FamilyUnit fam) {
		family = fam;

		if (fam != null && kinID == -1) {
			Individual mother = fam.getWife();

			if (mother != null)
				kinID = mother.kinID;  // use same ID as mother if we have none already
		}
	}

	public void die(){
		if(this.family != null){
			family.getAgent().setNumDeaths(family.getAgent().getNumDeaths() + 1);
			family.removePerson(this);
			this.setFamily(null);	
		}
	}

	public FamilyUnit getFamily() {
		return family;
	}

	public void setParentHH(Agent hh) {
		this.parentHH = hh;
	}

	public Agent getParentHH() {
		return this.parentHH;
	}

	public EligibleRecord getEligibleRecord() {
		return eligibleRecord;
	}

	public void setMarriageStatus(int status) {
		marriageStatus = status;		
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getGender() {
		return gender;
	}

	public void increaseAge() {
		age++;
	}

	public int getTag() {
		return tag;
	}

	public int getMarriageStatus() {
		return marriageStatus;
	}

	public void setKinID(int id) {
		kinID = id;
	}


}
