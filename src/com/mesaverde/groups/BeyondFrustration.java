package com.mesaverde.groups;

import java.awt.geom.Point2D;
import java.util.HashSet;

/**
 * @author Bocinsky
 *
 */
public class BeyondFrustration {

	HashSet<Frustration> frustrations;
	boolean hurt;
	
	public BeyondFrustration(){
		this.frustrations = new HashSet<Frustration>();
		this.hurt = false;
	}
		
	public void addFrustration(int x, int y, BeyondGroup f){
		Frustration frust = new Frustration(new Point2D.Double(x,y),f);
		this.getFrustrations().add(frust);
	}

	public HashSet<Frustration> getFrustrations() {
		return frustrations;
	}

	public void setFrustrations(HashSet<Frustration> frustrations) {
		this.frustrations = frustrations;
	}

	public boolean isHurt() {
		return hurt;
	}


	public void setHurt(boolean hurt) {
		this.hurt = hurt;
	}

	public class Frustration{
		Point2D cell;
		BeyondGroup frustrator;

		public Frustration(Point2D c, BeyondGroup f){
			cell = c;
			frustrator = f;
		}

		public Point2D getCell() {
			return cell;
		}

		public void setCell(Point2D cell) {
			this.cell = cell;
		}

		public BeyondGroup getFrustrator() {
			return frustrator;
		}

		public void setFrustrator(BeyondGroup frustrator) {
			this.frustrator = frustrator;
		}
	}
}