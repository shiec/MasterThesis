package no.ntnu.item.smash.sim.data;

import java.util.ArrayList;
import java.util.Arrays;

import no.ntnu.item.smash.sim.structure.Light;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.Room;

public class RoomIlluminanceStat {

	//private ArrayList<Room> rooms = new ArrayList<Room>();
	private Room room;
	private double area;
	private int[] lightNums;
	private double[] lumens;
	private double illuminance;
	private double[] hourlyIlluminance = new double[24*12];
	private double[] illumTemp = new double[24*12];
	private int count1 = 0, count2 = 0, count = 0;
	private ArrayList<double[]> monthlyIlluminance = new ArrayList<double[]>();
	private ArrayList<Light> lightList = new ArrayList<Light>();
	
	public RoomIlluminanceStat(Room room) {
		this.room = room;
		area = room.getLength()*room.getWidth();
		ArrayList<Device> deviceList = room.getDevicesByType(2);
		lumens = new double[20];
		lightNums = new int[20];
		for(int i = 0; i < deviceList.size(); i++) {
			this.lightList.add((Light)deviceList.get(i));
			lumens[i] = ((Light)deviceList.get(i)).getDesignLumen();
			lightNums[i] = 0;
		}
	}
	
	public synchronized void addIllumData(int day) { 
		if(day>monthlyIlluminance.size()) 
			monthlyIlluminance.add(Arrays.copyOf(hourlyIlluminance, hourlyIlluminance.length));
		else
			monthlyIlluminance.set(day-1, hourlyIlluminance);
	}
	
	public ArrayList<double[]> getMonthlyIlluminance(){
		return monthlyIlluminance;
	}
	
	public double[] getLumen(){
		return lumens;
	}
	
	public void setLumen(double[] lumens){
		this.lumens = lumens;
	}
	
	public synchronized double getIlluminance(){
		return illuminance;
	}
	
	public synchronized void setIlluminance(int index){
		double totalLumen = 0.0;
		for(int i = 0; i < lightNums.length; i++) {
			if (lightNums[i] == 1) totalLumen += lightList.get(i).getDesignLumen();
		}
		this.illuminance = totalLumen*room.getUtilizationFactor()*room.getLightLossFactor()/area;
		hourlyIlluminance[index] = illuminance;
		//System.out.println(illuminance + "  "+index);
		
	}
	
	public synchronized int[] getLightNums() {
		return lightNums;
	}
	
	public synchronized void addLightNum(int num) {
		lightNums[num-1] = 1;
	}
	
	public synchronized void removeLightNum(int num) {
		lightNums[num-1] = 0;
	}
	
	public double[] getHourlyIlluminance(){
		return hourlyIlluminance;
	}
	
	public ArrayList<Light> getLightList() {
		return lightList;
	}
}
