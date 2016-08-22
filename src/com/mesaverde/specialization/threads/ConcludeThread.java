package com.mesaverde.specialization.threads;

import java.util.concurrent.CountDownLatch;

import com.mesaverde.village.Agent;

public class ConcludeThread extends Thread {
	private Agent agent;
	private CountDownLatch startSignal;
	private CountDownLatch doneSignal;

	public ConcludeThread(Agent agent, CountDownLatch startSignal, CountDownLatch doneSignal) {
		this.agent = agent;
		this.startSignal = startSignal;
	    this.doneSignal = doneSignal;
	}
	
	@Override public void run() {
		try {
			startSignal.await();
			agent.step_procure_conclude();
			doneSignal.countDown();
		} catch (InterruptedException ex) {}
	}
}
