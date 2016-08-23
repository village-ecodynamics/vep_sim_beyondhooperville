package com.mesaverde.groups;

import uchicago.src.sim.engine.SimInit;

/** Run this class to get a simulation with no GUI elements.  You can provide a parameter file
 *  similar to that found in v8.pf that will allow you to set the amount of runs, as well as some
 *  other parameters that are adjustable in the Repast panel.
 * 
 */
public class BeyondHooperBatchAgentModel extends BeyondHooperAgentModelSwarm {
	public BeyondHooperBatchAgentModel() {
		super();
        this.isGui = false;
	}
	
    /**
     * The batch module's default variable settings have to be defined.
     * Note that all the other default settings are defined in Model.setup().
     * Also note that all settings here can be overridden by the parameter file.
     */
    public final void setup() {  
        // Specify the parameters to be manipulated by RePast's parameter
        // mechanism (i.e. set from the parameter file).
		params = new String[] {
				  "File_ID", "allAgents",
				  "experimentDuration", 
				  "group_size", "s", "beta", "mu"
			  };
		
        // Initializing the original model first
        super.setup();
    }
    
	/** Now it's time to build the model objects. We use various parameters
	 * inside ourselves to choose how to create things.
	 * 
	 */
    public void buildModel() {
    	super.buildModel();
    	if (getController().isBatch()) {
    		printBatchHeader();
    	}
    }

	public void printBatchHeader() {

		System.out.printf("v8 is running in Batch mode for %d timesteps.\n",
				getExperimentDuration());

	}
	
	public static void main(String[] args) {
		BeyondHooperBatchAgentModel agentModelSwarm = new BeyondHooperBatchAgentModel();
        SimInit init = new SimInit();
        
        // DC: Allows the use of a parameter file if one is specified.
        if (args.length > 0)        	
            init.loadModel(agentModelSwarm, args[0], true);
        else
        	init.loadModel(agentModelSwarm, "params.txt", true);  // DC: now we go to a parameter file if there are no args on command line invocation
        init.setExitOnExit(true);      
	}
}