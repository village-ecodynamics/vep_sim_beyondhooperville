package com.mesaverde.village;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.mesaverde.groups.BeyondHooperAgent;
//import com.mesaverde.groups.LearningHooperAgent;
//import com.mesaverde.specialization.SpecializedAgent;

public class Utilities {
	public static void copyArray(double[] src, double[] dest, int len) {
		int i;
		for (i = 0; i < len; i++) {
			dest[i] = src[i];
		}
	}

	/** Decides what type of agent to use based on settings in Village.java
	 */
	public static Agent createAgent() {
		if (Village.AGENT_TYPE == 0)
			return new Agent();
//		if (Village.AGENT_TYPE == 1)
//			return new SpecializedAgent();
		if (Village.AGENT_TYPE == Village.HOOPER_AGENTS) {
			//            if (Village.ENABLE_LEARNING_FOR_HOOPER)
			//                return new LearningHooperAgent();
			return new BeyondHooperAgent();
		}

		return null;
	}

	/** Calculates the distance between 2 agents.  Works with the Torus world concept */
	public static final double distance(Agent agent, Agent agent2) {
		return distance(agent.getCell(), agent2.getCell());
	}

	/** Calculates the distance between two cells. Works with the Torus world concept. */
	public static final double distance(Cell c1, Cell c2) {
		double diffX = Math.abs(c1.getX() - c2.getX());
		double halfWorldX = Village.WORLD_X_SIZE / 2;
		if (diffX > halfWorldX)
			diffX = halfWorldX * 2 - diffX;

		double diffY = Math.abs(c1.getY() - c2.getY());
		double halfWorldY = Village.WORLD_Y_SIZE / 2;
		if (diffY > halfWorldY)
			diffY = halfWorldY * 2 - diffY;

		double distance = Math.sqrt(Math.pow(diffX, 2)
				+ Math.pow(diffY, 2));

		return distance;
	}

	/** Finds other agents in a certain range of the given agent.
	 * The default distance is Village.MAX_COOP_RADIUS_BRN
	 * @param agent
	 * @return The list of agents (not including agent), that are within range.
	 */
	public static final ArrayList<Agent> findAgentsInRange(Agent agent) {
		return findAgentsInRange(agent, Village.MAX_COOP_RADIUS_BRN);
	}

	/** Finds other agents in a certain range of the given agent.
	 * The default distance is Village.MAX_COOP_RADIUS_BRN
	 * @param agent
	 * @param distance - how far away (inclusive) to find agents
	 *  @return The list of agents (not including agent), that are within range.
	 */
	public static final ArrayList<Agent> findAgentsInRange(Agent agent, double distance) {
		ArrayList<Agent> inRange = new ArrayList<Agent>();

		ArrayList<Agent> agents = agent.getMySwarm().getAgentList();
		for (Agent sa : agents) {
			if (sa != agent && distance(sa, agent) <= distance) {
				inRange.add(sa);
			}
		}

		return inRange;
	}

	public static void unzipFile(String filePath){

		// buffer for read and write data to file
		byte[] buffer = new byte[2048];

		try {
			FileInputStream fInput = new FileInputStream(filePath);
			ZipInputStream zipInput = new ZipInputStream(fInput);

			ZipEntry entry = zipInput.getNextEntry();

			while(entry != null){
				String entryName = entry.getName();
				File file = new File(entryName);           

				System.out.println("Unzip file " + entryName + " to " + file.getAbsolutePath());

				// create the directories of the zip directory
				if(entry.isDirectory()) {
					File newDir = new File(file.getAbsolutePath());
					if(!newDir.exists()) {
						boolean success = newDir.mkdirs();
						if(success == false) {
							System.out.println("Problem creating Folder");
						}
					}
				}

				else {
					FileOutputStream fOutput = new FileOutputStream(file);
					int count = 0;
					while ((count = zipInput.read(buffer)) > 0) {
						// write 'count' bytes to the file output stream
						fOutput.write(buffer, 0, count);
					}
					fOutput.close();
				}
				// close ZipEntry and take the next one
				zipInput.closeEntry();
				entry = zipInput.getNextEntry();
			}

			// close the last ZipEntry
			zipInput.closeEntry();

			zipInput.close();
			fInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File foo = new File("__MACOSX/");
		if(foo.exists()){
			try {
				delete(foo);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	static void delete(File f) throws IOException {
		  if (f.isDirectory()) {
		    for (File c : f.listFiles())
		      delete(c);
		  }
		  if (!f.delete())
		    throw new FileNotFoundException("Failed to delete file: " + f);
		}
	
}
