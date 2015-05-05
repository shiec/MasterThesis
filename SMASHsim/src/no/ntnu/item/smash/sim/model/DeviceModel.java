package no.ntnu.item.smash.sim.model;

import java.util.ArrayList;

import no.ntnu.item.smash.sim.core.SimulationModel;
import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.EntityStatistics;
import no.ntnu.item.smash.sim.structure.DLCRequest;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.PowerReductionRequest;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.ULCRequest;

public abstract class DeviceModel implements Runnable{

	protected SimulationModel model;
	protected Device device;
	protected int currentDay = 1;
	protected int currentMonth = 1;
	protected int currentYear = 2006;
	
	protected Room room;
	protected int simTime = 24 * 60 * 60; // 1 hour in seconds - 1 time step is 1
										  // second
	
	// statistics
	protected EntityStatistics stat;
	
	protected double[] hourlyEnergy = new double[24*12];
	protected double[] hourlyOTemp = new double[24*12];
	protected double[] hourlyPrice = new double[24*12];
	protected double[] hourlyCost = new double[24*12];
	protected double[] hourlyRTemp = new double[24*12];
	protected double[] hourlyPeakLoad = new double[24*12];
	
	protected double[] dailyEnergy;
	protected double[] dailyRTemp;
	
	public abstract void startSim(int simTime);
	public abstract void stopSim();
	
	protected boolean isDLCDevice = false;
	protected ArrayList<int[]> controlPeriods = new ArrayList<int[]>();
	protected PowerReductionRequest currentControlReq;
	
	public DeviceModel(Device device, Room room) {
		this.device = device;
		this.room = room;
	}
	
	public void setSimParent(SimulationModel model) {
		this.model = model;
	}
	
	public abstract void updateSchedule(double[] schedule);
	
	@Override
	public void run() {
		
	}
	
	public void setStatistics(String path, String name) {
		stat = new EntityStatistics(path, name);
	}
	
	public Device getDevice() {
		return device;
	}
	
	public Room getRoom() {
		return room;
	}
	
	public int getCurrentDay() {
		return currentDay;
	}
	
	public void setCurrentDay(int currentDay) {
		this.currentDay = currentDay;
	}
	
	public int getCurrentMonth() {
		return currentMonth;
	}
	
	public void setCurrentMonth(int currentMonth) {
		this.currentMonth = currentMonth;
	}
	
	public void setCurrentDate(int currentDay, int currentMonth, int currentYear) {
		this.currentDay = currentDay;
		this.currentMonth = currentMonth;
		this.currentYear = currentYear;
		int daysInMonth = Constants.getNumDaysInMonth(currentMonth, currentYear);
		dailyEnergy = new double[daysInMonth];
		dailyRTemp = new double[daysInMonth];
	}
	
	public int getCurrentYear() {
		return currentYear;
	}
	
	public void setCurrentYear(int currentYear) {
		this.currentYear = currentYear;
	}
	
	public double[] getEnergyInfo() {
		return hourlyEnergy;
	}
	
	public double[] getPriceInfo() {
		return hourlyPrice;
	}
	
	public double[] getCostInfo() {
		return hourlyCost;
	}
	
	public double[] getOTempInfo() {
		return hourlyOTemp;
	}
	
	public double[] getRTempInfo() {
		return hourlyRTemp;
	}
	
	public double[] getPeakLoadInfo() {
		return hourlyPeakLoad;
	}
	
	public double[] getDailyEnergy() {
		return dailyEnergy;
	}
	
	public double[] getDailyRTemp() {
		return dailyRTemp;
	}
	
	public PowerReductionRequest getCurrentControlRequest() {
		return currentControlReq;
	}
	
	public void setPowerReductionRequest(PowerReductionRequest req, int type) {
		currentControlReq = req;
		isDLCDevice = true;
		
		if(type==0) {
			DLCRequest request = (DLCRequest)req;
			
			int startSecond = request.getStartHour() * 3600;
			int endSecond = request.getEndHour() * 3600;
			int cycleDuration = request.getCycleDuration() * 60;
			int controlDuration = request.getControlDuration() * 60;
			
			for(int i=startSecond; i<=endSecond; i=i+cycleDuration) {
				int[] period = {i, i + controlDuration};
				controlPeriods.add(period);
			}
		} else if(type==1) {
			ULCRequest request = (ULCRequest)req;
			
			int startSecond = request.getStartHour() * 3600;
			int endSecond = request.getEndHour() * 3600;
			
			int[] period = {startSecond, endSecond};
			controlPeriods.add(period);
		}
	}
	
	protected boolean isControlPeriod(int second) { 
		if(!isDLCDevice) return false;
		else {
			for(int[] period:controlPeriods) {
				if(second>=period[0] && second<=period[1]) { 
					return true;
				}
			}
		}
		
		return false;
	}
	
	public double sumAllHours(double[] data) {
		double sum = 0;
		for(double d:data) {
			sum+=d;
		}
			
		return sum;
	}
}
