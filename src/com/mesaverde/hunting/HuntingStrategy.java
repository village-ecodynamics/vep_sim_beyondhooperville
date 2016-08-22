package com.mesaverde.hunting;

import com.mesaverde.village.Agent;
import com.mesaverde.village.Village;

public abstract class HuntingStrategy {

    protected Agent agent;
    /*
    protected Cell[] protein_cells = new Cell[Village.MAX_CELLS_HUNTED];
    protected int[] protein_cell_deers  = new int[Village.MAX_CELLS_HUNTED];
    protected int[] protein_cell_turkeys  = new int[Village.MAX_CELLS_HUNTED];
    protected int[] protein_cell_rabbits = new int[Village.MAX_CELLS_HUNTED];
    protected int[] protein_cell_hares = new int[Village.MAX_CELLS_HUNTED];*/
    protected int C_hunt = 0; //caloric cost in hours, since it's Cal/caloric increment per hour work for men (Village.WORK_CAL_MAN)

    /** Sets up connections between the hunting strategy and the agent doing the hunting, Agents should always pass itself */
    protected HuntingStrategy(Agent agent) {
        this.agent = agent;
    }

    /** Perform the hunt.
     * @param protein_need
     * @param hunt_radius
     * @return the amount of hunting calories expended
     */
    public abstract int execute(long protein_need, int hunt_radius);

	 /** DC: returns an array of int containing the updated values for a, b, c, and hunts*/
	public int[] searchNeighborhoodHuntDX(int a, int b, int c, int img, int hunts) {
        int k, r, dx, dy, ddx, ddy;
        dx = a;
        dy = b;
        ddx = dx;
        ddy = dy;
        k = 0;
        r = 0;
        int firsttime;
        int Deerfirsttime;
        int PHunted;
        PHunted = hunts;
        int HuntedCells = 0;

        firsttime = 1;
        Deerfirsttime = 1;

        while (r <= 50) {
            if (Deerfirsttime != 0) {
                if (HuntedCells < PHunted) {
                    HuntedCells++;
                    //  System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                } else {
                    k = agent.evalDProteinCellX(ddx, ddy, img);
                    HuntedCells++;
                    //  System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                    if (k > 0) // 1. kill 2. random relocation
                    {
                        a = ddx;
                        b = ddy;
                        c = 0;
                        hunts = HuntedCells;
                        agent.setHsearchradius(r);
                        return new int[]{a, b, c, hunts};
                    }
                }
                Deerfirsttime = 0;
            } else {
                for (ddy = dy - r; ddy < dy + (r + 1); ddy++) {
                    if (ddy == dy - r || ddy == dy + r) {
                        for (ddx = dx - r; ddx < dx + (r + 1); ddx++) {
                            if (HuntedCells < PHunted) {
                                HuntedCells++;
                                //	      System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                            } else {
                                k = agent.evalDProteinCellX(ddx, ddy, img);
                                HuntedCells++;
                                //     System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                                if (k > 0) // 1. kill 2. random relocation
                                {
                                    a = ddx;
                                    b = ddy;
                                    c = 0;
                                    hunts = HuntedCells;
                                    agent.setHsearchradius(r);
                                    return new int[]{a, b, c, hunts};
                                }
                            }
                        }
                    } else {
                        ddx = dx - r;
                        if (HuntedCells < PHunted) {
                            HuntedCells++;
                            //	  System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                        } else {
                            k = agent.evalDProteinCellX(ddx, ddy, img);
                            HuntedCells++;
                            //	  System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                            if (k > 0) // 1. kill 2. random relocation
                            {
                                a = ddx;
                                b = ddy;
                                c = 0;
                                hunts = HuntedCells;
                                agent.setHsearchradius(r);
                                return new int[]{a, b, c, hunts};
                            }
                        }

                        ddx = dx + r;
                        if (HuntedCells < PHunted) {
                            HuntedCells++;
                            //  System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                        } else {
                            k = agent.evalDProteinCellX(ddx, ddy, img);
                            HuntedCells++;
                            //  System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                            if (k > 0) // 1. kill 2. random relocation
                            {
                                a = ddx;
                                b = ddy;
                                c = 0;
                                hunts = HuntedCells;
                                agent.setHsearchradius(r);
                                return new int[]{a, b, c, hunts};
                            }
                        }
                    }
                }
            }

            if ((r - Village.DEER_DISTANCE) >= 0) {
                if (firsttime != 0) {
                    if (HuntedCells < PHunted) {
                        HuntedCells++;
                        //	  System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                    } else {

                        k = agent.evalLProteinCellX(ddx, ddy, img);
                        HuntedCells++;
                        if ((k) > 0) // 1. kill 2. random relocation
                        {
                            a = ddx;
                            b = ddy;
                            c = 1;
                            hunts = HuntedCells;
                            agent.setHsearchradius(r);
                            return new int[]{a, b, c, hunts};
                        }
                    }
                    firsttime = 0;
                } else {
                    for (ddy = dy - (r - Village.DEER_DISTANCE); ddy < dy + (r - Village.DEER_DISTANCE + 1); ddy++) {
                        if (ddy == dy - (r - Village.DEER_DISTANCE) || ddy == dy + (r - Village.DEER_DISTANCE)) {
                            for (ddx = dx - (r - Village.DEER_DISTANCE); ddx < dx + (r - Village.DEER_DISTANCE + 1); ddx++) {
                                if (HuntedCells < PHunted) {
                                    HuntedCells++;
                                    //	      System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                                } else {
                                    k = agent.evalLProteinCellX(ddx, ddy, img);
                                    HuntedCells++;
                                    if ((k) > 0) // 1. kill 2. random relocation
                                    {
                                        a = ddx;
                                        b = ddy;
                                        c = 1;
                                        hunts = HuntedCells;
                                        agent.setHsearchradius(r);
                                        return new int[]{a, b, c, hunts};
                                    }
                                }
                            }
                        } else {
                            ddx = dx - (r - Village.DEER_DISTANCE);

                            if (HuntedCells < PHunted) {
                                HuntedCells++;
                                //      System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                            } else {
                                k = agent.evalLProteinCellX(ddx, ddy, img);
                                HuntedCells++;
                                if ((k) > 0) // 1. kill 2. random relocation
                                {
                                    a = ddx;
                                    b = ddy;
                                    c = 1;
                                    hunts = HuntedCells;
                                    agent.setHsearchradius(r);
                                    return new int[]{a, b, c, hunts};
                                }
                            }

                            ddx = dx + (r - Village.DEER_DISTANCE);
                            if (HuntedCells < PHunted) {
                                HuntedCells++;
                                //      System.out.printf("searched in x = %d, y = %d # = %d\n", ddx, ddy, HuntedCells);
                            } else {
                                k = agent.evalLProteinCellX(ddx, ddy, img);
                                HuntedCells++;
                                if ((k) > 0) // 1. kill 2. random relocation
                                {
                                    a = ddx;
                                    b = ddy;
                                    c = 1;
                                    hunts = HuntedCells;
                                    agent.setHsearchradius(r);
                                    return new int[]{a, b, c, hunts};
                                }
                            }
                        }
                    }
                }
            }

            r++;
        }

        a = ddx;
        b = ddy;
        agent.setHsearchradius(51);

        return new int[]{a, b, c, hunts};
    }


    public abstract int searchHuntatX(int dx, int dy);

    public int getC_hunt() {
        return C_hunt;
    }
}
