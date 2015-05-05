package no.ntnu.item.smash.sim.structure;

import java.util.ArrayList;

public class Room3 extends Room{

	public Room3(String name, ArrayList<RoomWall> outerWalls,
			ArrayList<RoomWall> innerWalls, double height, double width, double length,
			int windowNo, double windowW, double windowL, double roofPit,
			ArrayList<Device> devices, Plan schedule) {
		super(name, outerWalls, innerWalls, height, width, length, windowNo, windowW, windowL, roofPit, devices, schedule);
	}
	
	public Room3(String name, ArrayList<RoomWall> outerWalls,
			ArrayList<RoomWall> innerWalls, double height, double width, double length,
			int windowNo, double windowW, double windowL, double roofPit,
			ArrayList<Device> devices, Plan schedule, double uf, double llf) {
		super(name, outerWalls, innerWalls, height, width, length, windowNo, windowW, windowL, roofPit, devices, schedule, uf, llf);
	}
	
	public double areaDoor;
	public double areaWindow;
	public double areaEWall;
	public double areaIWall;
	public double areaCeiling;
	public double areaFloor;
	
	
}
