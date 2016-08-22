package com.mesaverde.village;

class Examplar {
	public double[] strategy = new double[2 + Village.MAX_CHILD_LINKS
			+ Village.MAX_RELATIVE_LINKS]; //WhoToAskProb[2+MAX_CHILD_LINKS+MAX_RELATIVE_LINKS]
	public double fitness; // based on maize storage (could also be multi-objective)
	public int x, y;
}

public class BeliefSpace {
	// Situational
	static Examplar[] E = new Examplar[Village.MAX_EXAMPLAR];
	static Examplar M; // memory just for one

	// Normative
	static double[] strategy_LowerBound = new double[2
			+ Village.MAX_CHILD_LINKS + Village.MAX_RELATIVE_LINKS];
	static double[] strategy_UpperBound = new double[2
			+ Village.MAX_CHILD_LINKS + Village.MAX_RELATIVE_LINKS];

	//static SituationalKn SKn;

	public static int InitializeBeliefSpace() {
		int i, j;

		if (Village.SITUATIONAL) {
			// SITUATIONAL KNOWLEDGE: 
			// Initialize Examplars
			for (i = 0; i < Village.MAX_EXAMPLAR; i++) {
				E[i] = new Examplar();

				for (j = 0; j < 2 + Village.MAX_CHILD_LINKS
						+ Village.MAX_RELATIVE_LINKS; j++) {
					E[i].strategy[j] = 0;
				}
				E[i].fitness = 0;
			}
		}

		if (Village.NORMATIVE) {
			// NORMATIVE KNOWLEDGE:
			// Initialize Ranges
			for (i = 0; i < 2 + Village.MAX_CHILD_LINKS
					+ Village.MAX_RELATIVE_LINKS; i++) {
				strategy_LowerBound[i] = 0;
				strategy_UpperBound[i] = 0;
			}

		}

		if (Village.MEMORY != 0) {

			M = new Examplar();

			for (j = 0; j < 2 + Village.MAX_CHILD_LINKS
					+ Village.MAX_RELATIVE_LINKS; j++) {
				M.strategy[j] = 1; //  note: 1, not 0
			}
			M.fitness = 0;
		}

		// TOPOGRAPHICAL KNOWLEDGE (SPATIAL)
		// 

		return 1;
	}

	static void Influence(Agent Ag) {

		//	[Ag influenceWhoToAskProb_Historical: E.strategy];

		//	printf("Influencing1 : %d", [Ag getTag]);

		int whichKn = -1; // which kn used to influence

		int i;

		if (Village.SITUATIONAL && Village.NORMATIVE)
			whichKn = Village.uniformIntRand(0, 1);
		else if (Village.SITUATIONAL)
			whichKn = 0;
		else if (Village.NORMATIVE)
			whichKn = 1;

		if (Village.MEMORY != 0)
			whichKn = 2;

		switch (whichKn) {
		case 0: // SITUATIONAL

			i = Village.uniformIntRand(0, Village.MAX_EXAMPLAR - 1);
			influenceWhoToAskProb_Situational(Ag, E[i].strategy);
			break;
		case 1: // NORMATIVE

			influenceWhoToAskProb_Normative(Ag, strategy_LowerBound,
					strategy_UpperBound);
			break;
		case 2: // MEMORY

			influenceWhoToAskProb_Memory(Ag, M.strategy);
			break;
		}
	}
	
	public static void influenceWhoToAskProb_Normative(Agent agt, double[] L, double[] U) {
		double[] WhoToAskProb = agt.WhoToAskProb;
		
        int i;
        for (i = 0; i < 2 + Village.MAX_CHILD_LINKS + Village.MAX_RELATIVE_LINKS; i++) {
            if (WhoToAskProb[i] < L[i]) {
                WhoToAskProb[i] = L[i];
            } else if (WhoToAskProb[i] > U[i]) {
                WhoToAskProb[i] = U[i];
            }
        }        
    }
	
	public static void influenceWhoToAskProb_Memory(Agent ag, double[] A) {
		double[] WhoToAskProb = ag.WhoToAskProb;
		
        int i;
        for (i = 0; i < 2 + Village.MAX_CHILD_LINKS + Village.MAX_RELATIVE_LINKS; i++) {
            if (A[i] > WhoToAskProb[i]) {
                WhoToAskProb[i] += 0.1;
            } else if (A[i] < WhoToAskProb[i]) {
                WhoToAskProb[i] -= 0.1;
            }
            if (WhoToAskProb[i] < 0) {
                WhoToAskProb[i] = 0; // check -ve
            }
        }
    }
	
	public static void influenceWhoToAskProb_Situational(Agent ag, double[] A) {
        int i;
        for (i = 0; i < 2 + Village.MAX_CHILD_LINKS + Village.MAX_RELATIVE_LINKS; i++) {
            ag.WhoToAskProb[i] = A[i];
        }
    }

	static void Accept(Agent Ag) {
		int AgMaizeStorage = Ag.getMaizeStorage();
		double[] strategy = new double[2 + Village.MAX_CHILD_LINKS
				+ Village.MAX_RELATIVE_LINKS];

		int i, j, s;
		boolean done = true;

		if (Village.SITUATIONAL) {
			// UPDATE SITUATIONAL

			for (i = 0; i < Village.MAX_EXAMPLAR && done; i++) {
				if (AgMaizeStorage > E[i].fitness) {
					// shift down (insertion sort)
					for (j = Village.MAX_EXAMPLAR - 1; j > i; j--) {
						E[j].fitness = E[j - 1].fitness;
						for (s = 0; s < 2 + Village.MAX_CHILD_LINKS
								+ Village.MAX_RELATIVE_LINKS; s++) {
							E[j].strategy[s] = E[j - 1].strategy[s];
						}
					}

					// strategy[0] = [Ag getWhoToAskProb][0];
					Ag.setWhoToAskProb(strategy);
					E[i].fitness = AgMaizeStorage;
					for (s = 0; s < 2 + Village.MAX_CHILD_LINKS
							+ Village.MAX_RELATIVE_LINKS; s++) {
						E[i].strategy[s] = strategy[s];
					}

					done = !done;
				}
			}

		}

		if (Village.NORMATIVE) {
			// UPDATE NORMATIVE

			// set to first examplar
			for (i = 0; i < 2 + Village.MAX_CHILD_LINKS
					+ Village.MAX_RELATIVE_LINKS; i++) {
				strategy_LowerBound[i] = E[0].strategy[i];
				strategy_UpperBound[i] = E[0].strategy[i];
			}
			// find best min and max
			for (i = 1; i < Village.MAX_EXAMPLAR; i++) {
				for (s = 0; s < 2 + Village.MAX_CHILD_LINKS
						+ Village.MAX_RELATIVE_LINKS; s++) {
					if (E[i].strategy[s] < strategy_LowerBound[s])
						strategy_LowerBound[s] = E[i].strategy[s];

					if (E[i].strategy[s] > strategy_UpperBound[s])
						strategy_UpperBound[s] = E[i].strategy[s];

				}
			}
		}

		if (Village.MEMORY != 0) {
			int worldtime = Ag.getWorldTime();
			Ag.setWhoToAskProb_Memory(M.strategy);

			if (worldtime > 1275) {
				for (i = 0; i < 2 + Village.MAX_CHILD_LINKS
						+ Village.MAX_RELATIVE_LINKS; i++) {
					System.out.printf("%0.3f ", M.strategy[i]);
				}
				System.out.println();
			}
		}
	}
}
