package com.mesaverde.specialization;
import java.util.ArrayList;
import java.util.HashMap;

import com.mesaverde.specialization.tasks.Task;
import com.mesaverde.village.Agent;


/** Calculates the amount of specialization among a population.  Needs a reference to all the agents in the
 * system as well as a list of tasks in the system.
 * @author Denton
 *
 */
public class DOLCalculator {	
	private Double[][] matrix;
	private Double[] rowsums; // the matrix row sums
	Double[] colsums;
	private double hxy = 0;
	private ArrayList<? extends Agent> agentList;
	private Class<? extends Task>[] taskTypes;
	private int size;
	
	public DOLCalculator(ArrayList<? extends Agent> agentList, Class<? extends Task>[] taskTypes) {
		this.agentList = agentList;
		this.taskTypes = taskTypes;
		size = taskTypes.length;
	}
	
	public double calcDivisionOfLabour() {
		matrix = new Double[agentList.size()][taskTypes.length];
		rowsums = new Double[matrix.length];
		
		readMatrix();
		normalizeMatrix();
		double hy = calcHY();
		
		double ixy = calcMutualInformation();
		double shannon = ixy - hy + hxy;
		
		if (shannon == 0)
			return 0;
				
		return ixy / shannon;
	}

	private void readMatrix() {
		int i = 0;
	
		for (Agent a : agentList) {
			SpecializedAgent sa = (SpecializedAgent) a;
			
			Double[] vals = new Double[taskTypes.length];
			HashMap<Task, Double> weights = sa.getAllocationStrategy().getWeights();				
			
			HashMap<Class<? extends Task>, Double> valsByClassType = getValuesByClassType(weights);
			
			for (int j = 0; j < taskTypes.length; j++) {
				Double val = valsByClassType.get(taskTypes[j]);
				
				if (val == null) 
					val = 0.0d;
				
				// we end up with NaN if its the agents first year (so no weights)
				//if (val.isNaN())
				//	val = 0.0d;
				
				vals[j] = val;
			}
			
			normalize(vals);
			matrix[i++] = vals;
		}
	}
	
	/** Make it so that the total of all values is 1, remaining in proportion */
	private void normalize(Double[] vals) {
		double sum = sum(vals);

        if (sum != 1.0d) {
            if (sum > 0) {
                for (int i = 0; i < vals.length; i++) {
                    vals[i] = vals[i] / sum;
                }
            } else {
                // just fill the array with the same values
                for (int i = 0; i < vals.length; i++) {
                    vals[i] = 1.0 / vals.length;
                }
            }
        }
	}

	private HashMap<Class<? extends Task>, Double> getValuesByClassType(
			HashMap<Task, Double> weights) {
		HashMap<Class<? extends Task>, Double> result = new HashMap<Class<? extends Task>, Double>();
		
		for (Task t : weights.keySet()) {
			result.put(t.getClass(), weights.get(t));
		}
		
		return result;
	}
	
	private void normalizeMatrix() {	
		double total = 0;
		
		// get total
		for (Double[] d : matrix)
		{
			Double s = sum(d);
			
			total += s;
		}
		
		// normalize
		int pos = 0;
		for (Double[] d : matrix)
		{
			for (int i = 0; i < d.length; i++) {
				d[i] /= total;
			}
			
			Double result = sum(d);
			
			rowsums[pos++] = result;
		}		
	}
	
	private double calcHY() {
		double hx = 0;
		
		for (int i = 0; i < rowsums.length; i++) {
			Double sum = rowsums[i];

			if (sum != 0)
				hx += sum * log2(sum);
		}
		
		return hx * -1;
	}

	private double sum(Double[] doubles) {
		double total = 0;
		
		for (Double d : doubles) {
			if (d != null)
				total += d;
		}
		
		return total;
	}

	private double calcMutualInformation() {
		Double total = 0d;
		calcColumnSums();
		hxy = 0;
		
		int i = 0;
		for (Double[] row : matrix) {
			int j = 0;
			for (Double d : row) {
				if (d != 0 && rowsums[i] != 0 && colsums[j] != 0) {
					hxy += d * log2(d);
					total += d * log2(d / (rowsums[i] * colsums[j]));
				}
				j++;
			}
			i++;
		}
		
		hxy *= -1;
		return total;
	}
	
	private final double log2(double x) {
		  return Math.log(x)/Math.log(2.0); 
	}

	
	private Double[] calcColumnSums() {
		Double[] results = new Double[size];
		
		for (int i = 0; i < size; i++) {
			double total = 0.0d;
			
			for (Double[] d : matrix) {
				total += d[i];
			}
			results[i] = total;
		}
		
		colsums = results;
		return results;
	}

	public void printMatrix() {
		for (Double[] arr : matrix) {
			for (Double d : arr)
				System.out.print(d + " ");
			System.out.println();
		}
	}
}
