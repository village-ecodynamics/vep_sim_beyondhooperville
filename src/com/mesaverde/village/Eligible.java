package com.mesaverde.village;

import java.util.*;

import com.mesaverde.model.*;

public class Eligible {
	private volatile static Vector<EligibleRecord> eligibleList;	
//	protected static int MALES;
//	protected static int FEMALES;
	/*
	 * Add a bachelor to the list
	 */
	synchronized static EligibleRecord addToEligibleListA(int pHHTag, Agent pptrHH,
			int pNOEB, Individual individual, int pcellx, int pcelly,
			Cell pptrCell, int page, int pRemarriage, int pgender, int p_tag) {
		EligibleRecord record = new EligibleRecord();

		record.HHTag = pHHTag;
		record.ptrHH = pptrHH;
		record.NumberOfEligibleBachelors = pNOEB;
		record.individual = individual;
		record.cellx = pcellx;
		record.celly = pcelly;
		record.ptrCell = pptrCell;
		record.Remarriage = pRemarriage; // 0 = no, 1=yes
	//	int MALES = Collections.frequency(eligibleList, "MALE");
	//	int FEMALES = Collections.frequency(eligibleList, "FEMALE");
	//	System.out.println("Number of females" + FEMALES);

		if (Village.DEBUG) {
			System.out.printf("**** ADDED NEW RECORD TO ELIGIBLE LIST\n");
		}
		
		eligibleList.add(record);
		
		return record;
	}

	/* initialize the empty list, called once */
	public static int InitializeEligibleList() {
		eligibleList = new Vector<EligibleRecord>();

		if (Village.DEBUG) {
			System.out
					.print("eligibleList Initialized. It has unlimited capacity .\n");

			// eligibleListLength = 0;
		}
		return eligibleList.size();
	}

	/*
	 * Remove a lucky (or dead) bachelor from the list
	 */
	synchronized static void removeFromEligibleList(EligibleRecord record,
			AgentModelSwarm mySwarm) {
		// check nil
		if (record == null) {
			return;
		}

		eligibleList.remove(record);
	}
	
	/*
	 * Search the eligible list for a mate. (match may not be from same
	 * household, match must be within catchment area) Currently tested with
	 * random function only, may be modified with any search rules // done - now
	 * looks for nearest mate returns the index of the individual match in the
	 * entry
	 */
	private synchronized static EligibleRecord searchEligibleListP(int ptag, int px, int py,
			AgentModelSwarm mySwarm, int myGender) {
		EligibleRecord mateRecord = null;
		EligibleRecord best_mateRecord = null;

		int listsize = eligibleList.size();
		int i;
		double d;
		int min_distance = 99999;

		if (listsize > 0) {
			for (i = 0; i < listsize; i++) {
				mateRecord = eligibleList.get(i);

				// I can't see how we end up with a null element, but we'll try to ignore it
				if (mateRecord == null)
					continue;
				
				if (mateRecord.HHTag != ptag && mateRecord.getGender() != myGender) // not
																				// same
																				// household
																				// ,
																				// not
																				// same
																				// sex
				{
					d = Math.sqrt(Math.pow(mateRecord.cellx - px, 2)
							+ Math.pow(mateRecord.celly - py, 2));
					if (d < min_distance) {
						min_distance = (int) d;
						best_mateRecord = eligibleList.get(i);
					}
				} else // not self
				{
					mateRecord = null;
				}
			}
		}

		return best_mateRecord;

	}
	
	/** Finds a mate and removes it from the list right away.  This prevents other threads from also 
	 * finding the same mate after it's been chosen.
	 * @param ptag - Household tag of searching agent
	 * @param px - x coordinate of searching agent
	 * @param py - y coordinate of searching agent
	 * @param mySwarm - swarm of searching agent
	 * @param myGender - gender of searching agent
	 * @return The mate found, or null if none available
	 */
	synchronized static EligibleRecord findAndRemoveMate(int ptag, int px, int py,
			AgentModelSwarm mySwarm, int myGender) {
		EligibleRecord record = searchEligibleListP(ptag, px, py, mySwarm, myGender);
		
		if (record != null) {
			removeFromEligibleList(record, mySwarm);			
		}
		return record;		
	}
	
	/** Finds a mate and removes it from the list right away.  This prevents other threads from also 
	 * finding the same mate after it's been chosen.  Only removes the original if a mate is found.
	 * @param kidRecord - The record of the Individual looking for a wife
	 * @param ptag - Household tag of searching agent
	 * @param px - x coordinate of searching agent
	 * @param py - y coordinate of searching agent
	 * @param mySwarm - swarm of searching agent
	 * @param myGender - gender of searching agent
	 * @return The mate found, or null if none available
	 */
	synchronized static EligibleRecord findMateAndRemoveBoth(EligibleRecord kidRecord, int ptag, int px, int py,
			AgentModelSwarm mySwarm, int myGender) {
		if (kidRecord == null) return null;
		
		EligibleRecord record = searchEligibleListP(ptag, px, py, mySwarm, myGender);
		
		if (record != null) {
			removeFromEligibleList(record, mySwarm);
			removeFromEligibleList(kidRecord, mySwarm);				
		}
		
		return record;		
	}
//	public static int calculateMales() {
//	System.out.println(eligibleList);
//	int	MALES = Collections.frequency(eligibleList, "MALE");
//	System.out.println("Number of males" + MALES);
//		return MALES;
//	}
//	public static int calculateFemales() {
//	int	FEMALES = Collections.frequency(eligibleList, "0");
//	System.out.println("Number of females" + FEMALES);
//		return FEMALES;
//	}


	public static Vector<EligibleRecord> getEligibleList() {
		return eligibleList;
	}

	public static void setEligibleList(Vector<EligibleRecord> eligibleList) {
		Eligible.eligibleList = eligibleList;
	}
}