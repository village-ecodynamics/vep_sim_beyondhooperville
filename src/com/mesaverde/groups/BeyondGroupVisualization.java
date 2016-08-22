package com.mesaverde.groups;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class BeyondGroupVisualization {
	BeyondHooperMaster hooper2;
	JFrame mainMap;
	int mapWidth, mapHeight;

	public BeyondGroupVisualization(int x, int y)
	{
		mapWidth = x;
		mapHeight = y;
		System.out.println(mapWidth + " " + mapHeight);
		initializeMap();
	}

	Dimension myPreferredSize;
	float mapScaleX = 3.9f;
	float mapScaleY = 3.9f;


	JPanel p = new JPanel()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g;
			g2.scale(mapScaleX, mapScaleY);

			ArrayList<BeyondGroup> cloneList = new ArrayList<BeyondGroup>();
			cloneList.addAll(hooper2.getGroups());
			for(BeyondGroup group : cloneList) {
				
				if(Constants.TYPE_COLORS){
					
					g2.setColor(group.isHierarchical() ? Color.RED : Color.BLACK);
					
				}else{
					g2.setColor(group.getColor());
				}
				
				if(group.getTerritory().getTerritoryPoly()!=null){
					g2.drawPolygon(group.getTerritory().getTerritoryPoly());
				}
				
				if(Constants.DRAW_AGENTS){
					Ellipse2D.Double circle;
					
					ArrayList<BeyondHooperAgent> agentList = new ArrayList<BeyondHooperAgent>();
					agentList.addAll(group.getMembers());
					
					for(BeyondHooperAgent a : agentList){
						circle = new Ellipse2D.Double(a.getX(),a.getY(),1,1);
						g2.draw(circle);
					}
				}
			}
		}

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(mapWidth,mapHeight);
		}	
	};

	public void initializeMap()
	{
		mainMap = new JFrame();
		mainMap.setLayout(null);

		myPreferredSize = new Dimension((int)(mapWidth*4), (int)(mapHeight*4));

		mainMap.setContentPane(p);
		mainMap.setMinimumSize(myPreferredSize);

		mainMap.setVisible(true);
	}

	public void repaint()
	{
		mainMap.repaint();
	}

	public BeyondHooperMaster getHooper2() {
		return hooper2;
	}

	public void setHooper2(BeyondHooperMaster hooper2) {
		this.hooper2 = hooper2;
	}
}
