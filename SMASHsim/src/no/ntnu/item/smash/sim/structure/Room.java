package no.ntnu.item.smash.sim.structure;

import java.util.ArrayList;

import no.ntnu.item.smash.sim.data.RoomIlluminanceStat;

public class Room extends Entity {

	private ArrayList<RoomWall> outerWalls;
	private ArrayList<RoomWall> innerWalls;
	private double height;
	private double width;
	private double length;
	private int windowNo;
	private double windowW;
	private double windowL;
	private double roofPit;
	private ArrayList<Device> devices;
	private Plan plan;
	private Schedule schedule;
	private double currentTemp;
	private double utilizationFactor;
	private double lightLossFactor;
	private RoomIlluminanceStat illuminanceStat;
	private Plan lightPlan;

	public Room() {

	}

	public Room(String name, ArrayList<RoomWall> outerWalls,
			ArrayList<RoomWall> innerWalls, double height, double width, double length,
			int windowNo, double windowW, double windowL, double roofPit,
			ArrayList<Device> devices, Plan plan) {
		this.name = name;
		this.outerWalls = outerWalls;
		this.innerWalls = innerWalls;
		this.height = height;
		this.width = width;
		this.length = length;
		this.windowNo = windowNo;
		this.windowW = windowW;
		this.windowL = windowL;
		this.roofPit = roofPit;
		this.devices = devices;

		double[] schedule = new double[24];
		System.arraycopy(plan.getHourlyPlan(), 0, schedule, 0, schedule.length);
		this.plan = plan;
		this.schedule = new Schedule(Schedule.createHighResolutionSchedule(schedule));
		this.currentTemp = schedule[0];
	}
	
	public Room(String name, ArrayList<RoomWall> outerWalls,
			ArrayList<RoomWall> innerWalls, double height, double width, double length,
			int windowNo, double windowW, double windowL, double roofPit,
			ArrayList<Device> devices, Plan plan, double uf, double llf) {
		this.name = name;
		this.outerWalls = outerWalls;
		this.innerWalls = innerWalls;
		this.height = height;
		this.width = width;
		this.length = length;
		this.windowNo = windowNo;
		this.windowW = windowW;
		this.windowL = windowL;
		this.roofPit = roofPit;
		this.devices = devices;
		this.utilizationFactor = uf;
		this.lightLossFactor = llf;
		illuminanceStat = new RoomIlluminanceStat(this);

		double[] schedule = new double[24];
		System.arraycopy(plan.getHourlyPlan(), 0, schedule, 0, schedule.length);
		this.plan = plan;
		this.schedule = new Schedule(Schedule.createHighResolutionSchedule(schedule));
		this.currentTemp = schedule[0];
	}

	public ArrayList<RoomWall> getOuterWalls() {
		return outerWalls == null ? new ArrayList<RoomWall>() : outerWalls;
	}

	public ArrayList<RoomWall> getInnerWalls() {
		return innerWalls == null ? new ArrayList<RoomWall>() : innerWalls;
	}

	public void setOuterWalls(ArrayList<RoomWall> outerWalls) {
		this.outerWalls = outerWalls;
	}

	public void setInnerWalls(ArrayList<RoomWall> innerWalls) {
		this.innerWalls = innerWalls;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getWidth() {
		return width;
	}
	
	public void setWidth(double width) {
		this.width = width;
	}
	
	public double getLength() {
		return length;
	}
	
	public void setLength(double length) {
		this.length = length;
	}
	
	public int getWindowNo() {
		return windowNo;
	}

	public void setWindowNo(int windowNo) {
		this.windowNo = windowNo;
	}

	public double getWindowW() {
		return windowW;
	}

	public void setWindowW(double windowW) {
		this.windowW = windowW;
	}

	public double getWindowL() {
		return windowL;
	}

	public void setWindowL(double windowL) {
		this.windowL = windowL;
	}

	public double getRoofPit() {
		return roofPit;
	}

	public void setRoofPit(double roofPit) {
		this.roofPit = roofPit;
	}

	public ArrayList<Device> getDevices() {
		return devices;
	}
	
	public ArrayList<Device> getDevicesByType(int type) {
		ArrayList<Device> dev = new ArrayList<Device>();
		
		for(Device d:devices) {
			if(d.getType() == type) dev.add(d);
		}
		
		return dev;
	}

	public void setDevices(ArrayList<Device> devices) {
		this.devices = devices;
	}

	public void addDevice(Device device) {
		this.devices.add(device);
	}

	public Plan getPlannedSchedule() {
		return plan;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public double getCurrentTemp() {
		return currentTemp;
	}

	public void setCurrentTemp(double value) {
		currentTemp = value;
	}
	
	public double getUtilizationFactor() {
		return utilizationFactor;
	}

	public void setUtilizationFactor(double uf) {
		utilizationFactor = uf;
	}
	
	public double getLightLossFactor() {
		return lightLossFactor;
	}

	public void setLightLossFactor(double llf) {
		lightLossFactor = llf;
	}

	public synchronized RoomIlluminanceStat getIlluminanceStat() {
		return illuminanceStat;
	}
	
	public Plan getLightPlan() {
		return lightPlan;
	}
	
	public void setLightPlan(Plan lightPlan) {
		this.lightPlan = lightPlan;
	}
}
