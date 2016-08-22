package com.mesaverde.specialization.threads;

import com.mesaverde.specialization.SpecializedAgent;
import java.util.concurrent.CountDownLatch;

import com.mesaverde.village.Agent;

public class SocialPressureThread extends Thread {
	private SpecializedAgent agent;
	private CountDownLatch startSignal;
	private CountDownLatch doneSignal;

	public SocialPressureThread(Agent agent, CountDownLatch startSignal, CountDownLatch doneSignal) {
		this.agent = (SpecializedAgent) agent;
		this.startSignal = startSignal;
	    this.doneSignal = doneSignal;
	}
	
	@Override public void run() {
		try {
			startSignal.await();
            agent.applySocialPressure();
			doneSignal.countDown();
		} catch (InterruptedException ex) {}
	}
}
