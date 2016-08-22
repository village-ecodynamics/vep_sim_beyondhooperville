package com.mesaverde.specialization.threads;

import java.util.concurrent.CountDownLatch;

import com.mesaverde.specialization.SpecializedAgent;
import com.mesaverde.village.Agent;

public class SpringThread extends Thread {
	private Agent agent;
	private CountDownLatch startSignal;
	private CountDownLatch doneSignal;

	public SpringThread(Agent agent, CountDownLatch startSignal, CountDownLatch doneSignal) {
		this.agent = agent;
		this.startSignal = startSignal;
	    this.doneSignal = doneSignal;
	}
	
	@Override public void run() {
		try {
			startSignal.await();
			agent.step_procure_spring();
			
			if (agent instanceof SpecializedAgent) {
				SpecializedAgent sAgent = ((SpecializedAgent)agent);
				sAgent.getResourceManager().submitTradeRequests();
				sAgent.enforceResourceMaximums();
			}
			doneSignal.countDown();
		} catch (InterruptedException ex) {}
	}
}
