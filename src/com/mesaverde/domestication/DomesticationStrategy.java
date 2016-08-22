package com.mesaverde.domestication;

import com.mesaverde.village.Agent;

/**
 * @author bocinsky
 *
 */
public abstract class DomesticationStrategy {

    protected Agent agent;

    protected int C_domestication = 0; //caloric cost in hours, since it's Cal/caloric increment per hour work for men (Village.WORK_CAL_MAN)
    
    /** Sets up connections between the domestication strategy and the agent doing the hunting.
     *  Agents should always pass itself.
     */
    public DomesticationStrategy(Agent agent) {
        this.agent = agent;
    }


    /**
     * @param protein_need
     * @return
     */
    public abstract int execute(long protein_need);
    
    public int getC_domestication() {
        return C_domestication;
    }

}
