package no.ntnu.item.smash.sim.experiments1.washing;

import java.util.ArrayList;
import java.util.Calendar;

import no.ntnu.item.smash.css.structure.TimeTrigger;
import no.ntnu.item.smash.css.structure.Trigger;
import no.ntnu.item.smash.sim.core.SimEnvironment;
import no.ntnu.item.smash.sim.core.SimulationModel;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.model.WashingMachineModel;
import no.ntnu.item.smash.sim.structure.BuildingThermalIntegrity;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.Room3;
import no.ntnu.item.smash.sim.structure.Plan;
import no.ntnu.item.smash.sim.structure.WMPlan;
import no.ntnu.item.smash.sim.structure.WashingMachine;

public class Case5P1_washing {

	public static void main(String[] args) {
		SimEnvironment env = new SimEnvironment();
		
		// Configure devices - 4 real heaters + 2 dummy heaters
		ArrayList<Device> devLiving = new ArrayList<Device>();
		ArrayList<Device> devKitchen = new ArrayList<Device>();
		ArrayList<Device> devBath1 = new ArrayList<Device>();
		ArrayList<Device> devBath2 = new ArrayList<Device>();
		ArrayList<Device> devBed1 = new ArrayList<Device>();
		ArrayList<Device> devBed2 = new ArrayList<Device>();
		
		WashingMachine machine = new WashingMachine();
		machine.setId(21);
		machine.setName("WashingMachine1");
		machine.setType(Device.DEVTYPE_WASHINGMACHINE);
		
		devBath1.add(machine);
		
		// Configure rooms - 6 rooms (kitchen, living room, bath1, bath2, study, bed)
		double[] vKitchen = {17,17,17,17,17,17,17,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22};
		double[] vLiving = {18,18,18,18,18,18,18,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23};
		double[] vBath1 = {26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26};
		double[] vBath2 = {23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23};
		double[] vBed1 = {-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,18,18,18,18,18,18,18,18,18,18,-9999,-9999,-9999,-9999};
		double[] vBed2 = {-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,20,20,20,20,20,20,20,20,20,20,-9999,-9999,-9999,-9999};
		double[] wmSchedule = {17.833,0,0,0,17.833,0,0};
		
		Plan schKitchen = new Plan(vKitchen);
		Plan schLiving = new Plan(vLiving);
		Plan schBath1 = new Plan(vBath1);
		Plan schBath2 = new Plan(vBath2);
		Plan schBed1 = new Plan(vBed1);
		Plan schBed2 = new Plan(vBed2);
		WMPlan schWM = new WMPlan(wmSchedule);
		
		double height = 2.6;
		
		Room3 kitchen = new Room3("Kitchen", null, null, height, 3, 4, 2, 1, 1, 0, devKitchen, schKitchen);		
		Room3 living = new Room3("LivingRoom", null, null, height, 4, 4, 4, 1, 1, 0, devLiving, schLiving);		
		Room3 bath1 = new Room3("Bathroom1", null, null, height, 5, 2.75, 1, 1, 1, 0, devBath1, schBath1);
		Room3 bath2 = new Room3("Bathroom2", null, null, height, 4, 2.5, 1, 1, 1, 0, devBath2, schBath2);
		Room3 bedroom1 = new Room3("Bedroom1", null, null, height, 6, 3, 2, 1, 1, 0, devBed1, schBed1);
		Room3 bedroom2 = new Room3("Bedroom2", null, null, height, 4.5, 3, 2, 1, 1, 0, devBed2, schBed2);
		
		kitchen.areaFloor = 12;
		kitchen.areaDoor = 2;
		kitchen.areaCeiling = 12;
		kitchen.areaWindow = 2;
		kitchen.areaEWall = 3*height + 4*height - 2;
		kitchen.areaIWall = 3*height + 4*height - 2;
		
		living.areaFloor = 16;
		living.areaDoor = 6;
		living.areaCeiling = 16;
		living.areaWindow = 4;
		living.areaEWall = 7*height + 3*height - 4;
		living.areaIWall = 3*height + 1.5*height + 4*height + 4.5*height - 6;
		
		bath1.areaFloor = 13.75;
		bath1.areaDoor = 2;
		bath1.areaCeiling = 13.75;
		bath1.areaWindow = 1;
		bath1.areaEWall = 5*height + 2.5*height - 1;
		bath1.areaIWall = 3*height + 2.5*height + 0.5*height + 2.5*height - 2;
		
		bath2.areaFloor = 10;
		bath2.areaDoor = 4;
		bath2.areaCeiling = 0;
		bath2.areaWindow = 1;
		bath2.areaEWall = 4*height + 2.5*height - 1;
		bath2.areaIWall = 4*height + 2.5*height - 4;
		
		bedroom1.areaFloor = 0;
		bedroom1.areaDoor = 6;
		bedroom1.areaCeiling = 0;
		bedroom1.areaWindow = 2;
		bedroom1.areaEWall = 6*height + 3*height - 2;
		bedroom1.areaIWall = 6*height + 3*height - 6;
		
		bedroom2.areaFloor = 0;
		bedroom2.areaDoor = 2;
		bedroom2.areaCeiling = 0;
		bedroom2.areaWindow = 2;
		bedroom2.areaEWall = 3*height + 4.5*height - 2;
		bedroom2.areaIWall = 3*height + 4.5*height - 2;
		
		env.addRoom(kitchen);
		env.addRoom(living);
		env.addRoom(bath1);
		env.addRoom(bath2);
		env.addRoom(bedroom1);
		env.addRoom(bedroom2);
		
		// Configure house thermal property
		double[] thermalProp = BuildingThermalIntegrity.NORWAY_HEAVYINSULATED;
		env.setThermalProperty(thermalProp);
		
		// Configure device-model map
		env.addDeviceModel(machine, new WashingMachineModel(machine, bath1, schWM));
		
		// Configure DSO
		DSO.setContractType(1); // 0=DLC, 1=ULC
		//String[] dlcDevs = {"heaterLiving, heaterKitchen"};
		//DSO.setDLCControllableDevices(dlcDevs);
		
		// Configure CSS
		String cssNetAddress = "http://localhost:8888";
		env.setCSS(cssNetAddress);
		
		// Configure simulation time
		int simTime = 31*24*60*60; // 1 time step is 1 second
		
		// Prepare data if not ready
		//NordpoolDataFormatter.parseAndWrite(1, 2010, "Trondheim");
		//RawPriceDataFormatter.parseAndWrite(1, 2006, "Orkdal");
		//WaterDemandUtility.generateMonthlyData("wd-120.txt", 1, 2010, 4);
		
		SimulationModel sim = new SimulationModel(env);
		sim.setGeneratePowerReductionRequest(false);
		
		no.ntnu.item.smash.css.core.SystemContext context = new no.ntnu.item.smash.css.core.SystemContext();
		context.setThermalProperties(thermalProp);
		int id = context.getPgc().registerPolicy("C:/Users/shuna/workspace/SMASHCSS/policies_add/pwm_price.xet.xml", "cost");
		//PriceTrigger trigger = new PriceTrigger(Trigger.TYPE_PRICE, Trigger.COND_GREATERTHAN, 1.0);
		TimeTrigger trigger = new TimeTrigger(Trigger.TYPE_TIME, Trigger.COND_EQUALTO, 16);
		context.getPgc().registerPolicyTrigger(id, trigger);
		context.getPgc().activatePolicy(id);
		
		sim.setCSSSuspended(false);
		sim.startSimulation(simTime, 1, 1, 2010, "experiments1/washing/Case5P1_washing");
	}
	
}
