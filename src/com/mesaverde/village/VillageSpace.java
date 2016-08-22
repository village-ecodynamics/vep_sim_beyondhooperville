package com.mesaverde.village;

import uchicago.src.sim.space.Object2DGrid;

/**
 * 
 * @author denton
 */
public class VillageSpace extends Object2DGrid {
	private boolean overwriteWarnings;

	public VillageSpace(int width, int height) {
		super(width, height);
	}

	public void putObject(Object c, int x, int y) {
		if (super.getObjectAt(x, y) != null && overwriteWarnings)
			System.err.printf("Object already at (%d, %d)", x, y);
		super.putObjectAt(x, y, c);
	}

	public void setOverwriteWarnings(boolean overwrite) {
		overwriteWarnings = overwrite;
	}
}
