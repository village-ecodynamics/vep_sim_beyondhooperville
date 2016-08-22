package com.mesaverde.groups;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.mesaverde.village.Logger;
import com.mesaverde.village.Village;

public class BeyondHooperOutput {
	private BeyondHooperMaster hooper2;
	public static String LOGPREFIX = ""; /* Filename prefix for logfiles */
	public static String LOGSUFFIX = ".csv"; /* Filename suffix for logfiles */
	public static int MAXFILES = 25; /* Maximum number of log files open at once */
	public static int MAXLEN = 25; /* The maximum length of filenames */

	private boolean PGGameHead = true;

	class file {
		String name;
		File fp;
		FileWriter outStream;
		int year;
	}

	// allows MAXFILES open files at any given time. Remember to fail
	// gracefully if user tries to open more.
	file[] openFiles = new file[MAXFILES];

	// Unlike Objective-C, we need to initialize the file objects
	public BeyondHooperOutput() {

		// if(new File(Village.OUTPUT_DIR).exists()){
		// System.out.println("LOGFILE exists!");
		// }else{
		// System.out.println("LOGFILE does not exist!");
		// }

		for (int i = 0; i < openFiles.length; i++)
			openFiles[i] = new file();

		this.getHooper2();
		this.setPGGameHead(true);

	}


	public void recordStats() {
		String groupOutput = String.format("group_stats_run_%d", hooper2.getMySwarm().getFileID());
		//		String agentOutput = String.format("agent_stats_run_%d", BeyondHooperMaster.getMySwarm().getFileID());
		String domOutput = String.format("dom_stats_run_%d", hooper2.getMySwarm().getFileID());

		if (hooper2.getMySwarm().getWorldTime() == 600) {
			(new File(Village.OUTPUT_DIR + "/" + groupOutput + ".csv")).delete();
			//			(new File(Village.OUTPUT_DIR + "/" + agentOutput + ".csv")).delete();
			(new File(Village.OUTPUT_DIR + "/" + domOutput + ".csv")).delete();
		}

		ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
		cloneList.addAll(this.hooper2.getGroups());

		int i = 0;
		for (BeyondGroup g : cloneList) {
			if (hooper2.getMySwarm().getWorldTime() == 600 && i == 0) {
				recordGroupStats(g, groupOutput, true);
				recordDomStats(g, domOutput, true);
			} else {
				recordGroupStats(g, groupOutput, false);
				recordDomStats(g, domOutput, false);
			}

			i++;
		}
	}


	public void recordGroupStats(BeyondGroup group, String outfile, boolean head) {

		Point2D.Double centroid = group.getTerritory().centroid();
		Point2D.Double mean = Territory.getMeanLocation(group);

		// used for yearly percent done tracker
		int worldtime = hooper2.getMySwarm().getWorldTime();
		String buffer = "";

		if (worldtime == 600 && head) {
			buffer += new String("Year," + 
					"Group," + 
					"Size," + 
					"Territory_Size," +
					"Lineage," + 
					"Centroid_X," + 
					"Centroid_Y," + 
					"Mean_X," + 
					"Mean_Y," + 
					"isHierarchical," + 
					"isComplex\n");
			this.recordParams();
		}

		buffer += new String(worldtime + "," + group.getID() + ","
				+ group.getSize() + ","
				+ group.getTerritory().area() + ","
				+ group.getLineageID() + ","
				+ centroid.getX() + ","
				+ centroid.getY() + ","
				+ mean.getX() + ","
				+ mean.getY() + ","
				+ Boolean.toString(group.isHierarchical()) + ","
				+ Boolean.toString(group.isComplex()));

		this.write(buffer, outfile, Logger.NO, hooper2.getMySwarm()
				.getWorldTime());
	}

	public void recordDomStats(BeyondGroup group, String outfile, boolean head) {
		// used for yearly percent done tracker
		int worldtime = hooper2.getMySwarm().getWorldTime();
		String buffer = "";
		
		if (worldtime == 600 && head) {
			buffer += new String("Year," + "DomGroup," + "SubGroup");
		}

		if (!group.getSubGroups().isEmpty()){
			Iterator<BeyondGroup> it = group.getSubGroups().iterator();
			BeyondGroup tempGroup = null;
			while(it.hasNext()){
				tempGroup = it.next();
				if(!buffer.isEmpty()) buffer += new String("\n");
				buffer += new String(worldtime + ","
						+ group.getID() + ","
						+ tempGroup.getID());
			}
		}
		
		if(!buffer.isEmpty()){
			this.write(buffer, outfile, Logger.NO, hooper2.getMySwarm()
					.getWorldTime());
		}
	}

	public void recordFightStats(String header, String data, boolean head) {

		String fightOutput = String.format("fight_stats_run_%d",
				hooper2.getMySwarm().getFileID());

		if (head) {
			(new File(Village.OUTPUT_DIR + "/" + fightOutput + ".csv")).delete();
		}

		String buffer = "";

		if (head) {
			buffer += header;
		}

		buffer += data;

		this.write(buffer, fightOutput, Logger.NO, hooper2
				.getMySwarm().getWorldTime());
	}

	public void recordMergeStats(String header, String data, boolean head) {

		String mergeOutput = String.format("merge_stats_run_%d",
				hooper2.getMySwarm().getFileID());

		if (head) {
			(new File(Village.OUTPUT_DIR + "/" + mergeOutput + ".csv")).delete();
		}

		String buffer = "";

		if (head) {
			buffer += header;
		}

		buffer += data;

		this.write(buffer, mergeOutput, Logger.NO, hooper2
				.getMySwarm().getWorldTime());
	}

	public void recordHooperPGStats(String header, String data) {

		String hooperPGOutput = String.format("HooperPG_stats_run_%d",
				hooper2.getMySwarm().getFileID());

		if (PGGameHead) {
			(new File(Village.OUTPUT_DIR + "/" + hooperPGOutput + ".csv")).delete();
		}

		String buffer = "";

		if (PGGameHead) {
			buffer += header;
			this.setPGGameHead(false);
		}

		buffer += data;

		this.write(buffer, hooperPGOutput, Logger.NO, hooper2
				.getMySwarm().getWorldTime());
	}

	public void recordTributeStats(String header, String data, boolean head) {

		String tributeOutput = String.format("tribute_stats_run_%d",
				hooper2.getMySwarm().getFileID());

		if (head) {
			(new File(Village.OUTPUT_DIR + "/" + tributeOutput + ".csv")).delete();
		}

		String buffer = "";

		if (head) {
			buffer += header;
		}

		buffer += data;

		this.write(buffer, tributeOutput, Logger.NO, hooper2
				.getMySwarm().getWorldTime());
	}

	/**
	 * Internal file close function.
	 * 
	 * Just calls fclose. Mostly an abstraction to allow transparent porting.
	 * 
	 * @param record
	 *            file structure to use.
	 */
	private void closeFile(file record) {
		/*
		 * don't bother with other housecleaning since we're not reclaiming
		 * records
		 */
		try {
			record.outStream.close();
			record.name = "";
		} catch (Exception e) {
			System.err.printf("Logger: closeFile (%s) error: %s\n",
					record.name, e.getLocalizedMessage());
		}
	}

	/**
	 * Initialize the file array to known values so the empty check works
	 * properly.
	 * 
	 */
	public void init() {
		int i;
		file current;
		for (i = 0; i < MAXFILES; i++) {
			current = openFiles[i];
			current.name = "";
		}
	}

	public void recordParams(){
		String individual = "output/Hooper_parameters_run_" + hooper2.getMySwarm().getFile_ID() + ".csv";

		try {
			FileWriter out = new FileWriter(new File(individual));
			out.write("FILESUFX," +
					"MERGE_AND_FIGHT," +
					"HOOPER_PG_GAME," +
					"GROUP_SIZE," +
					"S," +
					"BETA," +
					"MU," +
					"ALLIANCES," +
					"PG_OUTCOME_EPIGENETIC_EFFECT," +
					"CHANGE_RATE," +
					"GROUP_BENEFIT_GROWTH_RATE," +
					"leader_STORAGE\n");

			out.write(hooper2.getMySwarm().getFile_ID() + "," +
					hooper2.getMySwarm().isMerge_and_fight() + "," +
					hooper2.getMySwarm().isHooper_pg_game() + "," +
					hooper2.getMySwarm().getGroup_size() + "," +
					hooper2.getMySwarm().getS() + "," +
					hooper2.getMySwarm().getBeta() + "," +
					hooper2.getMySwarm().getMu() + "," +
					hooper2.getMySwarm().isAlliances() + "," +
					hooper2.getMySwarm().isPg_outcome_epigenetic_effect() + "," +
					hooper2.getMySwarm().getChange_rate() + "," +
					hooper2.getMySwarm().getGroup_benefit_growth_rate() + "," +
					hooper2.getMySwarm().getLeader_storage() + "\n");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		String outfile = "output/Hooper_parameters_all.csv";
		File theFile = new File(outfile);
		try {
			if(!theFile.exists()){
				FileWriter out = new FileWriter(theFile);
				out.write("RUN," +
						"GROUP_SIZE," +
						"S," +
						"BETA," +
						"MU\n");
				out.close();
				out = null;
			}
			FileWriter out = new FileWriter(theFile,true);
			out.write(hooper2.getMySwarm().getFile_ID() + "," +
					hooper2.getMySwarm().getGroup_size() + "," +
					hooper2.getMySwarm().getS() + "," +
					hooper2.getMySwarm().getBeta() + "," +
					hooper2.getMySwarm().getMu() + "\n");
			out.close();
			out = null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Internal file open function.
	 * 
	 * Give this function a file structure, a filename, a year and tell it
	 * whether to use the year in the filename. The function will check to see
	 * if the file is already open and whether it needs to expire the currently
	 * open file. All files are closed and reopened at the start of a new year.
	 * This lets us append the year to the end of the filename easily if needed.
	 * 
	 * @param record
	 *            file structure to use.
	 * @param filename
	 *            The name of the file to open
	 * @param usingYr
	 *            Should the output filename include the year?
	 * @param yr
	 *            The current year of the simulation.
	 */
	private void openFile(file record, String filetoopen, boolean usingYr,
			int yr) {
		String filename = "";

		if (filetoopen.length() >= MAXLEN) {
			System.err.printf(
					"Whoops!  The filename (%s) you are trying to write to ",
					filetoopen);
			System.err.printf(
					"is longer than the maximum filename length (%d).\n",
					MAXLEN);
			System.exit(1);
		}

		if (usingYr) {
			filename += String.format("%s/%s%s%d%s", Village.OUTPUT_DIR, LOGPREFIX,
					filetoopen, yr, LOGSUFFIX);
		} else {
			filename += String.format("%s/%s%s%s", Village.OUTPUT_DIR, LOGPREFIX,
					filetoopen, LOGSUFFIX);
		}

		record.fp = new File(filename);

		// DC: ensure that the file exists, if not, then create it
		try {
			record.fp.createNewFile();
			record.outStream = new FileWriter(record.fp, true);
		} catch (IOException e) {
			System.err.printf("Logger: createFile (%s) error: %s\n", filename,
					e.getLocalizedMessage());
		}

		/*
		 * DC: This does nothing, if the file exists, then the internal part
		 * never occurs if (record.fp.exists()) { if (!record.fp.exists()) { new
		 * File(Village.OUTPUT_DIR).mkdir(); //mkdir(Village.OUTPUT_DIR, 0755); record.fp = new
		 * File(filename); } }
		 */
		if (!record.fp.exists()) {
			System.err
			.printf("ERROR: Please create a directory called \"output\" in ");
			System.err
			.printf("the current working directory where the simulation is running ");
			System.err
			.printf("from, make sure you have write permissions to the directory then try again.\n");
			System.exit(1);
		}

		record.name += filetoopen;
		record.year = yr;
	}

	/**
	 * Main logging function. Call this function to log output.
	 * 
	 * Give this function a string, a filename, a year and tell it whether to
	 * use the year in the filename. The string should *not* include the
	 * trailing newline. If you don't need a year, just pass 0 and set
	 * usingYear: NO.
	 * 
	 * Perhaps "filename" is too strong a word. The function is not limited to
	 * flat files. It could just as easily be adapted to write to a database.
	 * This is just the data storage name, such as "links." This function
	 * maintains its own open file handles and indexes by this name. In the
	 * current implementation, a call to this function using "links" as the
	 * filename and 649 as the year would generate a persistent file named
	 * "output/links649.out" and all subsequent calls using those parameters
	 * will write to that same file.
	 * 
	 * @param str
	 *            The string to log. Should not include the trailing newline.
	 * @param filename
	 *            The name of the file to write to
	 * @param usingYr
	 *            Should the output file include the year?
	 * @param yr
	 *            The current year of the simulation.
	 */
	/*
	 * DC: Synchronizing this method so that we can multi-thread, but limit it
	 * to only one thread calling this at the same time
	 */
	public synchronized void write(String str, String filename,
			boolean usingYr, int yr) {

		/*
		 * Don't need to check name length because open will bomb if it's too
		 * long
		 */
		int i;
		file current = null;
		for (i = 0; i < MAXFILES; i++) {
			current = openFiles[i];
			if (current.name.equals(filename)) {
				break;
			}
			/*
			 * if the name field is empty, we've hit the end of the list, open a
			 * new file
			 */
			if (current.name.equals("")) {
				openFile(current, filename, usingYr, yr);

				break;
			}
		}

		/* did we find it or run past the end? */
		if (i >= MAXFILES) {
			System.err
			.printf("Whoops!  You are trying to store more than %d filenames.\n",
					MAXFILES);
			System.err
			.printf("Either store fewer files or increase MAXFILES in Logger.h\n");
			System.exit(1);
		}
		if (yr != current.year) {
			// System.out.printf("Logger is closing %s and opening %s\n",
			// current.name, filename);
			closeFile(current);
			openFile(current, filename, usingYr, yr);
		}

		/* after this point, current is an open and fresh file record */

		try {
			current.outStream.write(str + "\n");
		} catch (Exception e) {
			System.err.printf("Logger: write (%s) error: %s\n", filename,
					e.getLocalizedMessage());
		}
	}

	/**
	 * Internal file write function.
	 * 
	 * Just calls fprintf. Mostly an abstraction to allow transparent porting.
	 * You should call fprintf directly for performance reasons. We'll use this
	 * if port to non flatfile storage.
	 * 
	 * @param record
	 *            file structure to use.
	 * @param str
	 *            String to write to file.
	 */
	@SuppressWarnings("unused")
	private void writeFile(file record, String str) {
		try {
			FileWriter fw = new FileWriter(record.fp);
			fw.write(str + "\n");
			fw.close();
		} catch (Exception e) {
			System.err.printf("Logger: writeFile (%s) error: %s\n",
					record.name, e.getLocalizedMessage());
		}
	}

	public BeyondHooperMaster getHooper2() {
		return hooper2;
	}

	public void setHooper2(BeyondHooperMaster hooper2) {
		this.hooper2 = hooper2;
	}


	public boolean isPGGameHead() {
		return PGGameHead;
	}


	public void setPGGameHead(boolean pGGameHead) {
		PGGameHead = pGGameHead;
	}
}
