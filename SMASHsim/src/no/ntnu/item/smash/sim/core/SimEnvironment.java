package no.ntnu.item.smash.sim.core;

import java.util.ArrayList;
import java.util.HashMap;

import no.ntnu.item.smash.sim.data.EnergyUsageStatistics;
import no.ntnu.item.smash.sim.model.DeviceModel;
import no.ntnu.item.smash.sim.structure.BuildingThermalIntegrity;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.RoomWall;
import no.ntnu.item.smash.sim.structure.Plan;

public class SimEnvironment {

	private double[] thermalProp = BuildingThermalIntegrity.NORWAY_MININSULATED;
	private ArrayList<Room> rooms = new ArrayList<Room>();
	private HashMap<Device, DeviceModel> deviceModelMap = new HashMap<Device, DeviceModel>();
	private String css;

	public void setThermalProperty(double[] prop) {
		EMUtility.thermalProp = prop;
		EnergyUsageStatistics.thermalProp = prop;
		thermalProp = prop;
	}
	
	public double[] getThermalProperty() {
		return thermalProp;
	}
	
	public ArrayList<Room> getRooms() {
		return rooms;
	}
	
	public Room getRoom(String name) {
		for(Room r:rooms) {
			if(r.getName().equals(name))
				return r;
		}
		
		return null;
	}
	
	public void addRoom(Room room) {
		rooms.add(room);
	}

	public void addRoom(String name, ArrayList<RoomWall> outerWalls, ArrayList<RoomWall> innerWalls,
			double height, double width, double length, int windowNo, double roofPit, double windowW, double windowL,
			ArrayList<Device> devices, Plan schedule) {
		Room room = new Room(name, outerWalls, innerWalls, height, width, length, windowNo, windowW, windowL, roofPit, devices, schedule);
		rooms.add(room);
	}
	
	public HashMap<Device, DeviceModel> getDeviceModelMap() {
		return deviceModelMap;
	}
	
	public void setDeviceModelMap(HashMap<Device, DeviceModel> map) {
		deviceModelMap = map;
	}
	
	public void addDeviceModel(Device device, DeviceModel model) {
		deviceModelMap.put(device, model);
	}

	public String getCSS() {
		return css;
	}
	
	public void setCSS(String cssNetAddress) {
		this.css = cssNetAddress;		
	}
	
}
