package com.mesaverde.village;

import java.io.FileWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeerCell {

	VillageSpace world;
	VillageSpace deerWorld; // 1.2

	int x, y; // coordinates in world

	FileWriter out;
	int worldTime;
	float deer;
	float soilprod;
	float TotalsoilProd;
	float usedfood;
	int deer_x_size = (int)(Math.ceil((double)Village.WORLD_X_SIZE / 5));
	int deer_y_size = (int)(Math.ceil((double)Village.WORLD_Y_SIZE / 5));
	static int counter = 18400;
    
    private Lock lock;    

	public DeerCell createEnd() {
		return this;
	}

	public synchronized	float getDeer() {
		return deer;
	}

	public float getDeerProd() {
		return TotalsoilProd;
	}

	public synchronized int huntDeerXY(int inx, int iny, VillageSpace w) {
		int max;
		max = (int) deer;
		int deers_hunted = Village.uniformIntRand(0, max - 1);
		deer = deer - deers_hunted;

		int xx, yy;

		x = inx;
		y = iny;
		world = w;
		for (xx = (5 * x); xx < (5 * x + 5); xx++) {
			for (yy = (5 * y); yy < (5 * y + 5); yy++) {
				Cell cell;
				cell = (Cell) world.getObjectAt(xx, yy);
				cell.setDeer(deer, usedfood);
			}

		}

		return deers_hunted;
	}

	public void setHuntedDeerXY(int inx, int iny, VillageSpace w,
			int killed_deer) {

        int xx, yy;

        synchronized (this) {
            deer = deer - killed_deer;
            
            x = inx;
            y = iny;
            world = w;        

            if (x != 45) {
                for (xx = 5 * x; xx < 5 * x + 5; xx++) {
                    for (yy = 5 * y; yy < 5 * y + 5; yy++) {
                        Cell cell;
                        cell = (Cell) world.getObjectAt(xx, yy);
                        cell.setDeer(deer, usedfood);
                    }

                }
            } else {
                for (xx = 5 * x; xx < 5 * x + 2; xx++) {
                    for (yy = 5 * y; yy < 5 * y + 5; yy++) {
                        Cell cell;
                        cell = (Cell) world.getObjectAt(xx, yy);
                        cell.setDeer(deer, usedfood);
                    }

                }
            }
        }
	}

	public void setInitDeer(float i) {
		deer = i;
	}

	// Takes individual cell values and conbines than into the appropiate supercell
	// In this instance the deercells are reading in the soil productivity numbers
	// from each cell to determine total vegetation productivity for the deercell
	public void setSoilProd(int inx, int iny, VillageSpace w) {
		int xx, yy;
		x = inx;
		y = iny;
		world = w;

		TotalsoilProd = 0;

		if (x != (deer_x_size - 1)) {
			for (xx = (5 * x); xx < (5 * x + 5); xx++) {
				for (yy = (5 * y); yy < (5 * y + 5); yy++) {
					Cell cell;
					cell = (Cell) world.getObjectAt(xx, yy);
					soilprod = cell.getDeerfood();
					TotalsoilProd = TotalsoilProd + soilprod;
					worldTime = cell.getWorldTime();

					//	 soile = [cell getSoilType];
					//	 Veg = [cell getVegPot];
					//	  printf("Cell. x=%d, y=%d, soil = %d\n Total Veg = %f, Deer Percentage = %f, deer food = %d\n", xx,yy,soile, Veg, deer, soilprod);
				}

			}
		} else {
			for (xx = (5 * x); xx < (5 * x + 2); xx++) {
				for (yy = (5 * y); yy < (5 * y + 5); yy++) {
					Cell cell;
					cell = (Cell) world.getObjectAt(xx, yy);
					soilprod = cell.getDeerfood();
					TotalsoilProd = TotalsoilProd + soilprod;
					worldTime = cell.getWorldTime();

					//	 soile = [cell getSoilType];
					//	 Veg = [cell getVegPot];
					//	  printf("Cell. x=%d, y=%d, soil = %d\n Total Veg = %f, Deer Percentage = %f, deer food = %d\n", xx,yy,soile, Veg, deer, soilprod);
				}

			}
		}
	}

	// creation stuff
	// initialization stuff
	public void setWorld(VillageSpace h) { // 1.2
		deerWorld = h;
	}

	public void setXY(int inx, int iny) {
		x = inx;
		y = iny;
		deerWorld.putObject(this, x, y); // add cell to the world 
	}

	public void updateCells(int inx, int iny, VillageSpace w) {
		int xx, yy;

		x = inx;
		y = iny;
		world = w;

		if (x != (deer_x_size - 1)) {
			for (xx = (5 * x); xx < (5 * x + 5); xx++) {
				for (yy = (5 * y); yy < (5 * y + 5); yy++) {
					Cell cell;
					cell = (Cell) world.getObjectAt(xx, yy);
					cell.setDeer(deer, usedfood);
				}

			}
		} else {
			for (xx = (5 * x); xx < (5 * x + 2); xx++) {
				for (yy = (5 * y); yy < (5 * y + 5); yy++) {
					Cell cell;
					cell = (Cell) world.getObjectAt(xx, yy);
					cell.setDeer(deer, usedfood);
				}

			}
		}
	}

	public void updateDeer() {

		float k, r, g;

		//first the total deer food is divided by 2000 since deer only use 1/2 the productivity available to them and we need productivity in Megagrams
		TotalsoilProd = TotalsoilProd / (1000 / (float) Village.DEER_USE);

		// Set up a discrete approximation of the logistic from Gurney and Nisbet 1998 equation 3.65
		k = (float) (TotalsoilProd / Village.DEER_K); //.55 megagrams supports one deer for 1 year

		r = (float) Village.DEER_R; //.4 is the intrinsic rate of increase

		g = (float) Math.exp(-r);

		if (deer != 0)
			deer = (k * deer) / (deer + g * (k - deer));

		//Now we need to determine how much food was actually eaten.
		//This creates a percentage of total food which is sent back to the
		//individual cells so each cell is reduced by the same percentage.
		usedfood = (float) (deer * Village.DEER_K);

		if (deer == 0)
			usedfood = 0;
		else
			usedfood = usedfood / TotalsoilProd;
	}

    /**
     * @return the lock for the DeerCell
     */
    public Lock getLock() {
        if (lock == null && Village.ENABLE_MULTITHREADING)
            lock = new ReentrantLock();
        return lock;
    }
}
