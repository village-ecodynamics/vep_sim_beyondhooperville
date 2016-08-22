package com.mesaverde.groups;

//import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.mesaverde.village.Agent;

public class Territory{	
	protected Polygon territoryPoly = null;
	protected HashSet<Point> territoryPoints = null;

	public Territory(HashSet<BeyondHooperAgent> members){
		this.territoryPoints = new HashSet<Point>();
		this.territoryPoly = new Polygon();

		HashSet<Point> uniqueLocations = new HashSet<Point>();

		for(BeyondHooperAgent a1 : members){
			for(BeyondHooperAgent a2 : members){
				if(a1!=a2 && (a1.getX()!=a2.getX() || a1.getY()!=a2.getY())){
					Point temp = new Point(a1.getX(),a1.getY());
					uniqueLocations.add(temp);
				}
			}
			if(uniqueLocations.size()==0){
				Point temp = new Point(a1.getX(),a1.getY());
				uniqueLocations.add(temp);
			}
		}


		if(uniqueLocations.size() >= 3){
			this.territoryPoly = quickHull(uniqueLocations);
			this.territoryPoints.addAll(Territory.getAllPoints(this.territoryPoly));
		}else{
			this.territoryPoints.addAll(uniqueLocations);
		}

//		System.out.println("Agents in: " + members.size());
//		System.out.println("territoryPoints count: " + territoryPoints.size());
//		System.out.println("territoryPoints: ");
//		for(Point p : territoryPoints){
//			System.out.println(p.getX() + ", " + p.getY());
//		}
//		System.out.println("territoryPoly.npoints: " + this.territoryPoly.npoints);
//		System.out.println("territory area: " + this.area());
//		System.out.println("");
	}

	public Territory(HashSet<Point> points, boolean ispoints){
		this.territoryPoints = new HashSet<Point>();
		this.territoryPoly = new Polygon();

		// Make sure there will be 3 or more unique locations
		HashSet<Point> uniqueLocations = new HashSet<Point>();
		for(Point p1 : points){
			for(Point p2 : points){
				if(p1!=p2 && !p1.equals(p2)){
					uniqueLocations.add(p1);
				}
			}
			if(uniqueLocations.size()==0){
				uniqueLocations.add(p1);
			}
		}

		if(uniqueLocations.size() >= 3){
			this.territoryPoly = quickHull(uniqueLocations);
			this.territoryPoints.addAll(Territory.getAllPoints(this.territoryPoly));
		}else{
			this.territoryPoints.addAll(uniqueLocations);
		}

		//		System.out.println("Points in: " + points.size());
		//		System.out.println("uniqueLocations count: " + uniqueLocations.size());
		//		System.out.println("uniqueLocations: ");
		//		for(Point p : uniqueLocations){
		//			System.out.println(p.getX() + ", " + p.getY());
		//		}
		//		System.out.println("");

	}

	public Polygon quickHull(HashSet<Point> uniqueLocations) {
		ArrayList<Point> convexHull = new ArrayList<Point>();
		if (uniqueLocations.size() < 3){
			return null;
		}
		// find extremals
		Point minPoint = null;
		Point maxPoint = null;

		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		for (Point p : uniqueLocations) {
			if (p.x < minX) {
				minX = p.x;
				minPoint = p;
			} 
			if (p.x > maxX) {
				maxX = p.x;
				maxPoint = p;       
			}
		}
		convexHull.add(minPoint);
		convexHull.add(maxPoint);
		uniqueLocations.remove(minPoint);
		uniqueLocations.remove(maxPoint);

		ArrayList<Point> leftSet = new ArrayList<Point>();
		ArrayList<Point> rightSet = new ArrayList<Point>();

		for (Point p : uniqueLocations) {
			if (pointLocation(minPoint,maxPoint,p) == -1)
				leftSet.add(p);
			else
				rightSet.add(p);
		}
		hullSet(minPoint,maxPoint,rightSet,convexHull);
		hullSet(maxPoint,minPoint,leftSet,convexHull);

		Polygon outPoly = new Polygon();
		for (Point vert : convexHull){
			outPoly.addPoint(vert.x, vert.y);
		}
		return outPoly;
	}

	public void hullSet(Point A, Point B, ArrayList<Point> set, ArrayList<Point> hull) {
		int insertPosition = hull.indexOf(B);
		if (set.size() == 0) return;
		if (set.size() == 1) {
			Point p = set.get(0);
			set.remove(p);
			hull.add(insertPosition,p);
			return;
		}
		int dist = Integer.MIN_VALUE;
		int furthestPoint = -1;
		for (int i = 0; i < set.size(); i++) {
			Point p = set.get(i);
			int distance  = distance(A,B,p);
			if (distance > dist) {
				dist = distance;
				furthestPoint = i;
			}
		}
		Point P = set.get(furthestPoint);
		set.remove(furthestPoint);
		hull.add(insertPosition,P);

		// Determine who's to the left of AP
		ArrayList<Point> leftSetAP = new ArrayList<Point>();
		for (int i = 0; i < set.size(); i++) {
			Point M = set.get(i);
			if (pointLocation(A,P,M)==1) {
				//set.remove(M);
				leftSetAP.add(M);
			}
		}

		// Determine who's to the left of PB
		ArrayList<Point> leftSetPB = new ArrayList<Point>();
		for (int i = 0; i < set.size(); i++) {
			Point M = set.get(i);
			if (pointLocation(P,B,M)==1) {
				//set.remove(M);
				leftSetPB.add(M);
			}
		}
		hullSet(A,P,leftSetAP,hull);
		hullSet(P,B,leftSetPB,hull);
	}

	public int distance(Point A, Point B, Point C) {
		int ABx = B.x-A.x;
		int ABy = B.y-A.y;
		int num = ABx*(A.y-C.y)-ABy*(A.x-C.x);
		if (num < 0) num = -num;
		return num;
	}  

	public int pointLocation(Point A, Point B, Point P) {
		int cp1 = (B.x-A.x)*(P.y-A.y) - (B.y-A.y)*(P.x-A.x);
		return (cp1>0)?1:-1;
	}

	int[] addElement(int[] org, int added) {
		int[] result = Arrays.copyOf(org, org.length +1);
		result[org.length] = added;
		return result;
	}

	boolean intersects(Line2D.Double line) {
		if(this.territoryPoly.npoints==0) return false;
		PathIterator polyIt = this.territoryPoly.getPathIterator(null); //Getting an iterator along the polygon path
		double[] coords = new double[6]; //Double array with length 6 needed by iterator
		double[] firstCoords = new double[2]; //First point (needed for closing polygon path)
		double[] lastCoords = new double[2]; //Previously visited point
		polyIt.currentSegment(firstCoords); //Getting the first coordinate pair
		lastCoords[0] = firstCoords[0]; //Priming the previous coordinate pair
		lastCoords[1] = firstCoords[1];
		polyIt.next();
		while(!polyIt.isDone()) {
			final int type = polyIt.currentSegment(coords);
			switch(type) {
			case PathIterator.SEG_LINETO : {
				final Line2D.Double currentLine = new Line2D.Double(lastCoords[0], lastCoords[1], coords[0], coords[1]);
				if(currentLine.intersectsLine(line))
					return true;
				break;
			}
			case PathIterator.SEG_CLOSE : {
				final Line2D.Double currentLine = new Line2D.Double(coords[0], coords[1], firstCoords[0], firstCoords[1]);
				if(currentLine.intersectsLine(line))
					return true;
				break;
			}
			}
			polyIt.next();
		}
		return false;
	}

	public static boolean overlaps(Territory t1,Territory t2){
		if(t1.getTerritoryPoints().size()==0 || t2.getTerritoryPoints().size()==0) return false;

		for(Point p1 : t1.getTerritoryPoints()){
			for(Point p2 : t2.getTerritoryPoints()){
				if(p1.equals(p2)) return true;
			}
		}

		if(t1.getTerritoryPoly().npoints==0 || t2.getTerritoryPoly().npoints==0) return false;

		PathIterator polyIt = t1.getTerritoryPoly().getPathIterator(null); //Getting an iterator along the polygon path
		double[] coords = new double[6]; //Double array with length 6 needed by iterator
		double[] firstCoords = new double[2]; //First point (needed for closing polygon path)
		double[] lastCoords = new double[2]; //Previously visited point
		polyIt.currentSegment(firstCoords); //Getting the first coordinate pair
		lastCoords[0] = firstCoords[0]; //Priming the previous coordinate pair
		lastCoords[1] = firstCoords[1];
		polyIt.next();
		while(!polyIt.isDone()) {
			final int type = polyIt.currentSegment(coords);
			switch(type) {
			case PathIterator.SEG_LINETO : {
				final Line2D.Double currentLine = new Line2D.Double(lastCoords[0], lastCoords[1], coords[0], coords[1]);

				if(t2.intersects(currentLine))
					return true;
				break;

			}
			case PathIterator.SEG_CLOSE : {
				final Line2D.Double currentLine = new Line2D.Double(coords[0], coords[1], firstCoords[0], firstCoords[1]);
				if(t2.intersects(currentLine))
					return true;
				break;
			}
			}
			polyIt.next();
		}
		return false;
	}

	boolean intersects(Territory territory) {
		return(overlaps(this,territory));
	}

	boolean contains(Point point) {
		for(Point p : this.getTerritoryPoints()){
			if(p.equals(point)) return true;
		}
		if(this.getTerritoryPoly().contains(point)) return true;

		return false;
	}
	
	public static Point2D.Double getMeanLocation(BeyondGroup group){
		double x = 0;
		double y = 0;
		for(Agent ag : group.getMembers()){
			x += ag.getX();
			y += ag.getY();
		}
		x = x/((double)group.getMembers().size());
		y = y/((double)group.getMembers().size());
		
		return new Point2D.Double(x,y);
	}
	
	
	public static double getDistance(BeyondGroup g1, BeyondGroup g2){
		Point2D.Double g1Center = getMeanLocation(g1);
		Point2D.Double g2Center = getMeanLocation(g2);
		return(g1Center.distance(g2Center));
	}

	// return area of polygon
	public double area() { return Math.abs(signedArea()); }

	// return signed area of polygon
	public double signedArea() {
		if(this.territoryPoly.npoints != 0){
			double sum = 0.0;
			for (int i = 1; i < this.territoryPoly.npoints; i++) {
				sum = sum + (this.territoryPoly.xpoints[i-1] * this.territoryPoly.ypoints[i]) - (this.territoryPoly.ypoints[i-1] * this.territoryPoly.xpoints[i]);
			}
			return 0.5 * sum;
		}else{
			return this.territoryPoints.size();
		}

	}

	public static HashSet<Point> getAllPoints(Polygon poly) {
		HashSet<Point> points = new HashSet<Point>();
		for (int i = 0; i < poly.npoints; i++) {
			points.add(new Point(poly.xpoints[i],poly.ypoints[i]));
		}
		return points;
	}

	// are vertices in counterclockwise order?
	// assumes polygon does not intersect itself
	public boolean isCCW() {
		return signedArea() > 0;
	}

	// return the centroid of the polygon
	public Point2D.Double centroid() {

		double cx = 0.0, cy = 0.0;
		if(this.territoryPoly.npoints >= 3){
			//        System.out.println(this.npoints);
			for (int i = 0; i < this.territoryPoly.npoints; i++) {
				cx += this.territoryPoly.xpoints[1];
				cy += this.territoryPoly.ypoints[1];
			}
			cx = cx/((double)this.territoryPoly.npoints);
			cy = cy/((double)this.territoryPoly.npoints);
			return new Point2D.Double(cx, cy);
		}else{
			for (Point point : this.territoryPoints) {
				cx += point.getX();
				cy += point.getY();
			}
			cx = cx/((double)this.territoryPoints.size());
			cy = cy/((double)this.territoryPoints.size());

			return new Point2D.Double(cx, cy);
		}
	}

	public Polygon getTerritoryPoly() {
		return territoryPoly;
	}

	public void setTerritoryPoly(Polygon territoryPoly) {
		this.territoryPoly = territoryPoly;
	}

	public HashSet<Point> getTerritoryPoints() {
		return territoryPoints;
	}

	public void setTerritoryPoints(HashSet<Point> territoryPoints) {
		this.territoryPoints = territoryPoints;
	}

}