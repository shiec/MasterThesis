package no.ntnu.item.smash.sim.comm;

import java.util.ArrayList;
import java.util.HashMap;

public class JSONSimResultObject {
	
	private double[] hourlyEnergy = new double[24];
	private double[] hourlyOTemp = new double[24];
	private double[] hourlyPrice = new double[24];
	private double[] hourlyCost = new double[24];
	
	private HashMap<String,ArrayList<double[]>> entityData = new HashMap<String,ArrayList<double[]>>();
	
	public double[] getHourlyEnergy() {
		return hourlyEnergy;
	}
	public void setHourlyEnergy(double[] hourlyEnergy) {
		this.hourlyEnergy = hourlyEnergy;
	}
	public double[] getHourlyOTemp() {
		return hourlyOTemp;
	}
	public void setHourlyOTemp(double[] hourlyOTemp) {
		this.hourlyOTemp = hourlyOTemp;
	}
	public double[] getHourlyPrice() {
		return hourlyPrice;
	}
	public void setHourlyPrice(double[] hourlyPrice) {
		this.hourlyPrice = hourlyPrice;
	}
	public double[] getHourlyCost() {
		return hourlyCost;
	}
	public void setHourlyCost(double[] hourlyCost) {
		this.hourlyCost = hourlyCost;
	}
	public HashMap<String, ArrayList<double[]>> getEntityData() {
		return entityData;
	}
	public void setEntityData(HashMap<String, ArrayList<double[]>> entityData) {
		this.entityData = entityData;
	}
	
}
