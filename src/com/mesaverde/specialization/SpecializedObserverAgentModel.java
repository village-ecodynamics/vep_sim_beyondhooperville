package com.mesaverde.specialization;

import uchicago.src.sim.engine.SimInit;

import com.mesaverde.village.Village;

/**
 * Run this class to get a simulation with GUI elements. You can provide a
 * parameter file similar to that found in v8.pf that will allow you to set the
 * amount of runs, as well as some other parameters that are adjustable in the
 * Repast panel. You can then change those parameters in the panel itself.
 * 
 * @author Denton Cockburn
 */
public class SpecializedObserverAgentModel extends SpecializedAgentModelSwarm {
	public static void main(String[] args) {
		Village.AGENT_TYPE = Village.SPECIALIZED_AGENTS; // used specialized agents

        // turn off turkey domestication
        Village.DOMESTICATION = false;

		SpecializedObserverAgentModel agentModelSwarm = new SpecializedObserverAgentModel();		
		
		SimInit init = new SimInit();
		
		// DC: Allows the use of a parameter file if one is specified.
		if (args.length > 0)
			init.loadModel(agentModelSwarm, args[0], false);
		else
			init.loadModel(agentModelSwarm, null, false);			
		init.setExitOnExit(true);		
	}

	public SpecializedObserverAgentModel() {
		Village.GUI_VERSION = Village.ALL; // to enable our graphs
		SpecializedAgentModelSwarm.SHOW_GRAPH = true; // change this to see/hide graphs		
		SpecializedAgentModelSwarm.SHOW_PARENT_GRAPHS = true;		
	}		
}
