package com.mesaverde.specialization;

import uchicago.src.sim.engine.SimInit;

import com.mesaverde.village.Village;

/**
 * Run this class to get a simulation with no GUI elements. You can provide a
 * parameter file similar to that found in v8.pf that will allow you to set the
 * amount of runs, as well as some other parameters that are adjustable in the
 * Repast panel.
 * 
 */
public class SpecializedBatchAgentModel extends SpecializedAgentModelSwarm {
	public static void main(String[] args) {
		Village.AGENT_TYPE = 1; // used specialized agents
		SpecializedBatchAgentModel agentModelSwarm = new SpecializedBatchAgentModel();
		SimInit init = new SimInit();

		// DC: Allows the use of a parameter file if one is specified.
		if (args.length > 0)
			init.loadModel(agentModelSwarm, args[0], true);
		else
			init.loadModel(agentModelSwarm, null, true); // DC: if using
		// parameter file,
		// replace null or v8.pf
		// with the path to
		// the required
		// parameter file
		init.setExitOnExit(true);
	}

	public SpecializedBatchAgentModel() {		
		SpecializedAgentModelSwarm.SHOW_GRAPH = false;
		SpecializedAgentModelSwarm.BATCH = 1;
	}
}