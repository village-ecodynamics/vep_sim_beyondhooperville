package com.mesaverde.village;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Unified data logging, avoids many redundant buffer flushed each sim year.
 * 
 * Examples of usage are in Agent.m in the methods saveLinks and saveLinksBRN.
 */
public class Logger {

	class file {

		String name;
		File fp;
		FileWriter outStream;
		int year;
	}

	public static int MAXLEN = 25; /* The maximum length of filenames */

	public static int MAXFILES = 25; /* Maximum number of log files open at once */

	public static String LOGDIR = "output"; /*
	 * Directory in which all logs are
	 * stored
	 */

	public static String LOGPREFIX = ""; /* Filename prefix for logfiles */
	public static String LOGSUFFIX = ".csv"; /* Filename suffix for logfiles */
	public static final boolean NO = false;

	public static final boolean YES = true;

	// allows MAXFILES open files at any given time. Remember to fail
	// gracefully if user tries to open more.
	file[] openFiles = new file[MAXFILES];

	// Unlike Objective-C, we need to initialize the file objects
	public Logger() {
		for (int i = 0; i < openFiles.length; i++)
			openFiles[i] = new file();
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
			System.err.printf("Logger: closeFile (%s) error: %s\n", record.name, e.getLocalizedMessage());
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
	private void openFile(file record, String filetoopen, boolean usingYr, int yr) {
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
			filename += String.format("%s/%s%s%d%s", LOGDIR, LOGPREFIX,
					filetoopen, yr, LOGSUFFIX);
		} else {
			filename += String.format("%s/%s%s%s", LOGDIR, LOGPREFIX,
					filetoopen, LOGSUFFIX);
		}

		record.fp = new File(filename);

		// DC: ensure that the file exists, if not, then create it
		try {
			record.fp.createNewFile();
			record.outStream = new FileWriter(record.fp, true);
		} catch (IOException e) {
			System.err.printf("Logger: createFile (%s) error: %s\n", filename, e.getLocalizedMessage());
		}

		/*
		 * DC: This does nothing, if the file exists, then the internal part
		 * never occurs if (record.fp.exists()) { if (!record.fp.exists()) { new
		 * File(LOGDIR).mkdir(); //mkdir(LOGDIR, 0755); record.fp = new
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
        /* DC: Synchronizing this method so that we can multi-thread,
         but limit it to only one thread calling this at the same time */
	public synchronized void write(String str, String filename, boolean usingYr, int yr) {

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
			.printf(
					"Whoops!  You are trying to store more than %d filenames.\n",
					MAXFILES);
			System.err
			.printf("Either store fewer files or increase MAXFILES in Logger.h\n");
			System.exit(1);
		}
		if (yr != current.year) {
			// System.out.printf("Logger is closing %s and opening %s\n", current.name, filename);
			closeFile(current);
			openFile(current, filename, usingYr, yr);
		}

		/* after this point, current is an open and fresh file record */

		try {
			current.outStream.write(str + "\n");			
		} catch (Exception e) {
			System.err.printf("Logger: write (%s) error: %s\n", filename, e.getLocalizedMessage());
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
		} catch (Exception e) {
			System.err.printf("Logger: writeFile (%s) error: %s\n", record.name, e.getLocalizedMessage());
		}
	}
}
