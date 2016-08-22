package com.mesaverde.village;

import java.io.*;
import java.util.*;
import com.mesaverde.model.*;

public class Database {
	VillageSpace world; // 1.2
	VillageSpace deerWorld;
	AgentModelSwarm mySwarm;
	int tmp;
	float r1;
	int step;
	float proxfit1;
	float proxfit2;
	float proxfit3;
	float proxfitz;
	int popLevel;
	int worldTime = 600;
	int which_yield;
	int random_yield;
	int data_length;
	int array_length;
	int world_x_size = Village.WORLD_X_SIZE;
	int world_y_size = Village.WORLD_Y_SIZE;
	int[][] data = new int[world_y_size][world_x_size];
	float info;
	int[] soilcheck = new int[153];
	float[] almagre_array = new float[1383]; // JAC 9/25/04 almagre data
	float[] prin_array = new float[1383]; // JAC 9/25/04 prin data

	int wx, wy;
	String data_file;
	String water_file;
	String output_file;
	String pop_file;
	int deer_x_size = (int)(Math.ceil((double)Village.WORLD_X_SIZE / 5));
	int deer_y_size = (int)(Math.ceil((double)Village.WORLD_Y_SIZE / 5));
	String[] wfile = new String[140];
	float[][] deer = new float[deer_y_size][deer_x_size]; // used for deer diffusion
	float[][] deerfood = new float[deer_y_size][deer_x_size]; // used for deer diffusion


	Scanner fp;
	Scanner wp;
	FileWriter Hout;
	FileWriter HHout;
	FileWriter BHout;
	FileWriter CCout;
	FileWriter popout;
	String theMethod;

	// DC: static variables - most of which are taken from methods
	private static int update = 600;
	private static int whichfile = 0;
	private static int water_length = 241;
	private static final int[] output_time = new int[] { Village.P6YEAR,
		Village.P7YEAR, Village.P8YEAR, Village.P9YEAR, Village.P10YEAR,
		Village.P11YEAR, Village.P12YEAR, Village.P13YEAR, Village.P14YEAR,
		Village.P15YEAR, Village.P16YEAR, Village.P17YEAR, Village.P18YEAR,
		Village.P19YEAR };
	private static final String[] output_name = new String[] { "P6", "P7", "P8",
		"P9", "P10", "P11", "P12", "P13", "P14", "P15", "P16", "P17",
		"P18", "P19" };

	public static void resetStatics() {
		update = 600;
		whichfile = 0;
		water_length = 241;
	}

	// initialization of database
	public void setDataFile(String name) {
		if (name.length() > 40) {
			System.err.println("Error: file name too long");
			System.exit(-1);
		}
		data_file = name;

		try {
			fp = new Scanner(new File(data_file));
		} catch (IOException e) {
			System.err.println("Error: can't open " + data_file);
			System.exit(-1);
		}
	}

	public void setWaterFile(String name) {
		if (name.length() > 40) {
			System.err.println("Error: file name too long");
			System.exit(-1);
		}
		water_file = name;

		try {
			wp = new Scanner(new File(water_file));
		} catch (IOException e) {
			System.err.println("Error: can't open " + water_file);
			System.exit(-1);
		}
	}

	public void setMySwarm(AgentModelSwarm s) {
		mySwarm = s;
	}

	public void setSelMethod(String method) {
		theMethod = method;
		// printf("%s\n", theMethod);
	}

	public void setDataFileLength(int num) {
		data_length = num;
	}

	// JC 9/24/04 used when reading in single array files
	public void setArrayFileLength(int num) {
		array_length = num;
	}

	public void setWhichYield(int a) {
		which_yield = a;
	}

	public void setRandomYield(int a) {
		random_yield = a;
	}

	public void setWorld(VillageSpace w) // 1.2
	{
		world = w;
	}

	public void setDeerWorld(VillageSpace w) // 1.2
	{
		deerWorld = w;
	}

	public Database createEnd() {
		if (world == null) {
			System.err
			.println("ERROR: must set world before ending database creation");
			System.exit(-1);
		}
		// check on the size of the world
		wx = world.getSizeX();
		if (wx > world_x_size)
			wx = world_x_size;

		wy = world.getSizeY();
		if (wy > world_y_size)
			wy = world_y_size;

		tmp = 0;
		step = 0;

		return this;
	}

	// database actions
	public void closeDataFile() {
		fp.close();
	}

	public void updateDataGrid() {
		int x, y;
		float i;
		if (data_length < 1) {
			if (which_yield == 0)
				data_length = 400;

			fp.close();
			setDataFile(data_file); // DC: used to reset the scanner - equal to
			// rewind(fp)
		}

		// update yield world
		// data is stored as N-S is columns and W-E is rows
		for (y = 0; y < world_y_size; y++) {
			for (x = 0; x < world_x_size; x++) {
				i = fp.nextFloat();
				data[y][x] = (int) i;
				
			}
		}
		data_length--;
	}

	// init spring amount resources
	// 12/02/04 JAC added to update flow rates for springs
	public void updateXYDataGrid() {
		int i;
		float x, y;
		float a, b, dubba;
		Cell cell;

		a = 0;
		b = 0;
		info = 0;
		String springs = "VEPI_data/spring/spring";

		if (worldTime == 1300)
			return;
		else {
			if (worldTime == update) {
				String wyear = String.valueOf(update);
				springs += wyear;
				springs += ".data";
				setWaterFile(springs);

				for (i = 0; i < water_length; i++) {					
					a = wp.nextFloat();					
					b = wp.nextFloat();					
					info = wp.nextFloat();					

					dubba = info;
					x = a;
					y = b;

					if (x >= world_x_size || y >= world_y_size){
						System.out.print("WARNING: Imported data does not match world dimensions!\n");
						continue;
					}

					cell = (Cell) world.getObjectAt((int) x, (int) y);
					cell.setAvailableWater(dubba);
				}
				wp.close();
				update += 5;
				whichfile++;
				water_length = 58;

			}
		}
	}

	public void setWorldTime(int t) {
		worldTime = t;
	}

	public void setPopLevel(int p) {
		popLevel = p;
	}

	// udated JC 9/24/04

	public void updateCells() {
		// printf("begin updateCells\n");
		int x, y;

		try {
			java.lang.reflect.Method method = Cell.class.getMethod(theMethod, Integer.TYPE);

			for (y = 0; y < wy; y++) {
				for (x = 0; x < wx; x++) {
					Cell cell = (Cell) world.getObjectAt(x, y);
					cell.setWorldTime(worldTime);

					cell.setAlmagre(almagre_array[worldTime - 600]); // tells cells what almagre
					// correction is                

					cell.setPrin(prin_array[(worldTime - 600)]); // tells cells what prin correction
					// is                

					method.invoke(cell, data[y][x]);
					//cell.perform(theMethod, data[y][x]);
					cell.refreshWater();

					// These lines call a method in Cell that evaluates whether the cell includes the given UTMs,
					// then outputs that cell's productivity statistics to garden_cells.data. For Crow Canyon's plots.
//					cell.reportGardens(4136717, 711054);
//					cell.reportGardens(4136479, 710401);
//					cell.reportGardens(4136565, 710664);
//					cell.reportGardens(4136832, 711120);
//					cell.reportGardens(4136810, 710972);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public void updateCWSingle() {
		int j, x, y;
		float i;
		float[] array = new float[array_length];
		for (j = 0; j < array_length; j++) {
			i = fp.nextFloat();
			array[j] = i;
		}

		try {
			float[] f = new float[1];
			java.lang.reflect.Method method = Cell.class.getMethod(theMethod, f.getClass());

			for (y = 0; y < wy; y++) {
				for (x = 0; x < wx; x++) {
					Cell cell = (Cell) world.getObjectAt(x, y);
					method.invoke(cell, array);
					//cell.perform(theMethod, array);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setAlmagreArray() {
		int j;
		float i;
		worldTime = 600;
		for (j = 0; j < array_length; j++) {
			i = fp.nextFloat();
			almagre_array[j] = i;
		}
	}

	public void setPrinArray() {
		int j;
		float i;
		for (j = 0; j < array_length; j++) {
			i = fp.nextFloat();
			prin_array[j] = i;

		}
	}

	public void updateDeerCells() {
		int x, y;
		// int test1, test2;
		for (y = 0; y < wy / 5; y++) {
			for (x = 0; x < ((wx / 5) + 1); x++) {
				// printf("x = %d\n", x);
				DeerCell deercell;
				deercell = (DeerCell) deerWorld.getObjectAt(x, y);
				if (deercell == null)
					System.out.printf("deercell[%d][%d] = nil", x, y);

				deercell.setSoilProd(x, y, world);
				deercell.updateDeer();
				deercell.updateCells(x, y, world);
			}
		}
	}

	public void createDeerMatrix() {

		int x, y;
		for (y = 0; y < wy / 5; y++) {
			for (x = 0; x < ((wx / 5) + 1); x++) {
				DeerCell deercell;
				deercell = (DeerCell) deerWorld.getObjectAt(x, y);
				deer[y][x] = deercell.getDeer();
				deerfood[y][x] = deercell.getDeerProd();
				// printf ("[%d][%d] deer = %f, deerfood= %f\n",
				// x,y,deer[y][x],deerfood[y][x]);
			}
		}
	}

	public void diffuseDeer() {
		if (Village.DIFFUSION) {
			createDeerMatrix();

			// Here is the function that actually does the computation.
			// One should call this at every time step.
			int M = deer_y_size;
			int N = deer_x_size;
			float[][] newdeer = new float[deer_y_size][deer_x_size]; // temporary storage

			float ccapacity; // carrying capacity

			float r = Village.DEER_R; // the intrinsic rate of growth

			float gamma; // computed quantity

			float diffusionterm; // temporary

			float diffcoeff = .1f; // the diffusion coefficient originally set
			// .1

			float cellwidth = 1.0f; // the width of a cell.

			int i, j;

			if (Village.EXPLICIT) {
				gamma = (float) Math.exp(-r);
				for (j = 0; j < N; j++) {
					for (i = 0; i < M; i++) {
						ccapacity = deerfood[i][j] / .55f;
						newdeer[i][j] = ccapacity
						* deer[i][j]
						          / (.001f + (deer[i][j] + gamma
						        		  * (ccapacity - deer[i][j])));
					}
				}
				/* Solve for the new deer vector */
				// printf(" in deer: %f %f\n",newdeer[3][3],deerfood[3][3]);
				pcg(deer[0], newdeer[0]); // DC: May have problems with this,
				// originally was &deer[0][0]

			} else {

				gamma = (float) Math.exp(-r);
				// printf ("gamma = %f",gamma);
				// ccapacity = deerfood/0.55/2.0;
				diffcoeff = diffcoeff / (cellwidth * cellwidth);

				// First handle interior terms. The new values go into newdeer
				// for
				// the moment. We broke this up into three "for" loops in order
				// to
				// avoid extra calls to the mod() function. If that
				// doesn't matter,
				// the code can be shortened.

				for (j = 1; j < N - 1; j++) {
					for (i = 1; i < M - 1; i++) {
						ccapacity = (deerfood[i][j]) / .55f; // capacity is
						// the deerfood
						// in megagrams
						// divided by
						// amount deer
						// eat. deerfood
						// has already
						// been modified
						// in deercell
						// to be in
						// megagrams and
						// to percent
						// deer eat has
						// also been
						// accounted for
						// newdeer[i][j] =
						// ccapacity*deer[i][j]/(.001+(deer[i][j]+gamma*(ccapacity-deer[i][j])));

						diffusionterm = deer[i - 1][j] + deer[i + 1][j]
						                                             + deer[i][j - 1] + deer[i][j + 1] - 4.0f
						                                             * deer[i][j];
						deer[i][j] = deer[i][j] + diffcoeff * diffusionterm
						/ (cellwidth * cellwidth);
					}
				}

				// Next handle the vertical boundaries.

				for (j = 0; j < N; j += N - 1) {
					for (i = 1; i < M - 1; i++) {
						ccapacity = (deerfood[i][j]) / .55f;
						// newdeer[i][j] =
						// ccapacity*deer[i][j]/(.001+(deer[i][j]+gamma*(ccapacity-deer[i][j])));
						diffusionterm = deer[i - 1][j] + deer[i + 1][j]
						                                             + deer[i][mod(j - 1, N)]
						                                                       + deer[i][mod(j - 1, N)] - 4.0f * deer[i][j];
						deer[i][j] = deer[i][j] + diffcoeff * diffusionterm
						/ (cellwidth * cellwidth);
					}
				}

				// Finally handle horizontal boundaries.

				for (j = 0; j < N; j++) {
					for (i = 0; i < M; i += M - 1) {
						ccapacity = (deerfood[i][j]) / .55f;
						// newdeer[i][j] =
						// ccapacity*deer[i][j]/(.001+(deer[i][j]+gamma*(ccapacity-deer[i][j])));
						diffusionterm = deer[mod(i - 1, M)][j]
						                                    + deer[mod(i + 1, M)][j]
						                                                          + deer[i][mod(j - 1, N)]
						                                                                    + deer[i][mod(j + 1, N)] - 4.0f * deer[i][j];
						deer[i][j] = deer[i][j] + diffcoeff * diffusionterm
						/ (cellwidth * cellwidth);
						// printf("%d %d i-1: %d i+1: %d j-1: %d j+1:
						// %d\n",i,j,mod(i-1,M),mod(i+1,M),mod(j-1,N),mod(j+1,N));
					}
				}

				// Now put the results back into the deer array.

				// for(j=0;j<N;j++){
				// for(i=0;i<M;i++){
				// deer[i][j] = newdeer[i][j];
				// }
				// }

			}
		}
		// printf("end of Deer Diffuse\n");
	}

	// This function just computes a mod b, where a is called "num",
	// and b is called "den".
	int mod(int num, int den) {
		if (num >= 0)
			num = num - (num / den) * den;
		else
			num = den + num;
		return num;
	}

	/*
	 * The next function is an implementation of the preconditioned conjugate
	 * gradient method of Concus, Golub, and O'Leary. It solves Ax = b where A
	 * is an MN dimensional square matrix, and x and b are MN dimensional
	 * vectors. The user must supply two functions with preassigned names:
	 * Amult(Av,v) - computes the product Av for a given vector v; Msolve(v,y) -
	 * solves Mv = y for v given y. All other matrix operations are handled in
	 * BLAS. Incidentally, we are using single precision throughout these
	 * calculations because the modelling is so speculative and the numerics so
	 * simple that using float precision would just be wasted time/effort.
	 * 
	 * Note that the rhs b is only used to compute the initial residual, so
	 * calling this as pcg(b,b) should be acceptable.
	 */
	public void pcg(float[] x, float[] b) {
		int M = deer_y_size;
		int N = deer_x_size;
		int i, k;
		int idim = M * N;
		int max_it = 100;
		float[] r = new float[M * N];
		float[] z = new float[M * N];
		float beta;
		float zprod1 = 0.0f;
		float zprod2 = 0.0f;
		float[] p = new float[M * N];
		float[] temporary = new float[M * N];
		float alpha;
		float tol = .1e-4f;
		float check;

		for (i = 0; i < idim; i++) {
			r[i] = b[i];
		}
		for (i = 0; i < idim; i++) {
			p[i] = 0.0f;
		}
		for (i = 0; i < idim; i++) {
			x[i] = 0.0f;
		}

		for (k = 0; k < max_it; k++) {
			check = sdot(r, r);
			if (check < tol) {
				// printf(" convergence in %d iterations\n",k);
				return;
			}
			Msolve(z, r);

			zprod1 = sdot(z, r);
			if (zprod2 != 0.0)
				beta = zprod1 / zprod2;
			else
				beta = 0.0f;
			zprod2 = zprod1;
			saxpy(z, p, beta, p); // computing p(k)

			Amult(temporary, p);
			alpha = zprod2 / sdot(p, temporary);
			saxpy(x, p, alpha, x);
			saxpy(r, temporary, -alpha, r);
		}
	}

	/* BLAS sdot function for this package. */
	float sdot(float[] u, float[] v) {
		int M = deer_y_size;
		int N = deer_x_size;
		int i, idim = M * N;
		float accum = 0.0f;
		for (i = 0; i < idim; i++) {
			accum += u[i] * v[i];
		}
		return accum;
	}

	/*
	 * BLAS saxpy function for this package. The result goes in w It is alright
	 * for w to be the same as u or v in memory
	 */
	public void saxpy(float[] u, float[] v, float a, float[] w) {
		int M = deer_y_size;
		int N = deer_x_size;
		int i, idim = M * N;
		float temporary;
		for (i = 0; i < idim; i++) {
			temporary = a * v[i];
			w[i] = temporary + u[i];
		}
	}

	/*
	 * Function to multiply a vector of dimension M*N by the diffusion matrix.
	 * Remember that the vector v is actually an array representing deer
	 * population of cell of an MxN rectangular grid.
	 */
	public void Amult(float[] Av, float[] v) {
		int M = deer_y_size;
		int N = deer_x_size;
		int i, j;
		int index;
		int ipmod, immod, jpmod, jmmod;
		float diffcoef = .1f; // for implicit - this is the real diffusivity.
		// original = .1

		for (i = 0; i < M; i++) {
			for (j = 0; j < N; j++) {
				index = i * N + j;
				ipmod = mod(i + 1, M);
				immod = mod(i - 1, M);
				jpmod = mod(j + 1, N);
				jmmod = mod(j - 1, N);
				Av[index] = 4.0f * v[index];
				Av[index] += -v[ipmod * N + jpmod] - v[ipmod * N + jmmod];
				Av[index] += -v[immod * N + jpmod] - v[immod * N + jmmod];
				Av[index] = diffcoef * Av[index] + v[index];
			}
		}
	}

	public void Msolve(float[] v, float[] y) {
		int M = deer_y_size;
		int N = deer_x_size;
		int i;
		for (i = 0; i < M * N; i++) {
			v[i] = y[i];
		}
	}

	public void updateRandomCells() {
		int x, y;

		try {
			java.lang.reflect.Method method = Cell.class.getMethod(theMethod, Integer.TYPE);

			for (y = 0; y < wy; y++) {
				for (x = 0; x < wx; x++) {
					Cell cell;
					cell = (Cell) world.getObjectAt(x, y);
					method.invoke(cell, ((int) (data[y][x] * (1.0 + gaussian() / 4.0))));
					//cell.perform(theMethod, ((int) (data[y][x] * (1.0 + gaussian() / 4.0))));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateCellWorld() {
		switch (which_yield) {
		case -1: // init data files is folder data
			updateDataGrid();
			updateCells();
			break;
		case 0:
			updateDataGrid();
			updateCells();
			break;
		case 1:
			updateRandomCells();
			break;
		default:
			break;
			// cases 2,3 should not change with updates
		}
		if (Village.DEBUG)
			System.out.printf("end updateCellWorld\n");
	}

	// yield specific stuff
	public void initYields() {
		int x, y;

		switch (which_yield) {
		case 1:
			for (y = 0; y < wy; y++) {
				for (x = 0; x < wx; x++) {
					data[y][x] = ((int) (random_yield / 2.3677) - 4) / 10;
				}
			}
			break;
		case 2:
			for (y = 0; y < wy; y++) {
				for (x = 0; x < wx; x++) {
					data[y][x] = ((int) (random_yield / 2.3677) - 4) / 10;
				}
			}
			break;
		case 3:
			peakIt();
			break;
		default:
			break;
		}

	}

	public void peakIt() {
		int i, j;

		for (i = 0; i <= wy / 2; i++) {
			for (j = 0; j < wx; j++) {
				data[i][j] = i / 2 + 5;
			}
		}
		for (i = wy - 1; i > wy / 2; i--) {
			for (j = 0; j < wx; j++) {
				data[i][j] = wy / 2 - i / 2 + 5;
			}
		}
		for (j = 0; j < wx / 2; j++) {
			for (i = j; i < wy - j - 1; i++) {
				data[i + 1][j] = data[i][j];
			}
		}
		for (j = wx - 1; j >= wx / 2; j--) {
			for (i = wx - j - 1; i < j - wx + wy; i++) {
				data[i + 1][j] = data[i][j];
			}
		}
	}

	float gaussian() {
		// Box Muller transformation
		float x1, x2, w;

		if (tmp == 0) {
			do {
				x1 = (float) (2.0 * Village.uniformDblRand(0, 1) - 1.0);
				x2 = (float) (2.0 * Village.uniformDblRand(0, 1) - 1.0);
				w = x1 * x1 + x2 * x2;
			} while (w >= 1.0f);

			w = (float) Math.sqrt((-2.0 * Math.log(w)) / w);

			// return two rng mean 0 sd 1
			r1 = x1 * w;
			tmp = 1;
			return (x2 * w);
		} else {
			tmp = 0;
			return (r1);
		}
	}
}
