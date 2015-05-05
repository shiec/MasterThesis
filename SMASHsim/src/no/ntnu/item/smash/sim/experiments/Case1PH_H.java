package no.ntnu.item.smash.sim.experiments;

import java.util.ArrayList;

import no.ntnu.item.smash.css.structure.TimeTrigger;
import no.ntnu.item.smash.css.structure.Trigger;
import no.ntnu.item.smash.sim.core.SimEnvironment;
import no.ntnu.item.smash.sim.core.SimulationModel;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.model.ElectricWaterHeaterModel;
import no.ntnu.item.smash.sim.model.HeaterModel;
import no.ntnu.item.smash.sim.structure.BuildingThermalIntegrity;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.ElectricWaterHeater;
import no.ntnu.item.smash.sim.structure.Heater;
import no.ntnu.item.smash.sim.structure.Room3;
import no.ntnu.item.smash.sim.structure.Plan;

public class Case1PH_H {

	public static void main(String[] args) {
		SimEnvironment env = new SimEnvironment();
		
		// Configure devices - 4 real heaters + 2 dummy heaters
		ArrayList<Device> devLiving = new ArrayList<Device>();
		ArrayList<Device> devKitchen = new ArrayList<Device>();
		ArrayList<Device> devBath1 = new ArrayList<Device>();
		ArrayList<Device> devBath2 = new ArrayList<Device>();
		ArrayList<Device> devBed1 = new ArrayList<Device>();
		ArrayList<Device> devBed2 = new ArrayList<Device>();
		
		Heater heaterLiving = new Heater();
		Heater heaterKitchen = new Heater();
		Heater heaterBath1 = new Heater();
		Heater heaterBath2 = new Heater();
		Heater heaterBed1 = new Heater();
		Heater heaterBed2 = new Heater();
		heaterLiving.setId(1);
		heaterLiving.setType(Device.DEVTYPE_HEATER);
		heaterLiving.setWatt(2400);
		heaterKitchen.setId(2);
		heaterKitchen.setType(Device.DEVTYPE_HEATER);
		heaterKitchen.setWatt(1200);
		heaterBath1.setId(3);
		heaterBath1.setType(Device.DEVTYPE_HEATER);
		heaterBath1.setWatt(1500);
		heaterBath2.setId(4);
		heaterBath2.setType(Device.DEVTYPE_HEATER);
		heaterBath2.setWatt(1500);
		ElectricWaterHeater whBath1 = new ElectricWaterHeater();
		whBath1.setId(5);
		whBath1.setType(Device.DEVTYPE_EWATERHEATER);
		whBath1.setName("whBathroom1");
		whBath1.setTankHeight(1.7);
		whBath1.setTankDiameter(0.43);
		whBath1.setTankVolume(145);
		whBath1.setWatt(2000);
		whBath1.setrValue(8);
		heaterBed1.setId(6);
		heaterBed1.setType(Device.DEVTYPE_HEATER);
		heaterBed1.setWatt(1200);
		heaterBed2.setId(7);
		heaterBed2.setType(Device.DEVTYPE_HEATER);
		heaterBed2.setWatt(1200);
		
		devKitchen.add(heaterKitchen);
		devLiving.add(heaterLiving);
		devBath1.add(heaterBath1);
		devBath1.add(whBath1);
		devBath2.add(heaterBath2);
		
		// Configure rooms - 6 rooms (kitchen, living room, bath1, bath2, study, bed)
		double[] vKitchen = {17,17,17,17,17,17,17,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22};
		double[] vLiving = {18,18,18,18,18,18,18,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23};
		double[] vBath1 = {26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26};
		double[] vBath2 = {23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23};
		double[] vBed1 = {-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,18,18,18,18,18,18,18,18,18,18,-9999,-9999,-9999,-9999};
		double[] vBed2 = {-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,20,20,20,20,20,20,20,20,20,20,-9999,-9999,-9999,-9999};
		double[] whSchedule = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
		
		Plan schKitchen = new Plan(vKitchen);
		Plan schLiving = new Plan(vLiving);
		Plan schBath1 = new Plan(vBath1);
		Plan schBath2 = new Plan(vBath2);
		Plan schBed1 = new Plan(vBed1);
		Plan schBed2 = new Plan(vBed2);
		Plan schWH = new Plan(whSchedule);
		
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
		env.addDeviceModel(heaterKitchen, new HeaterModel(heaterKitchen, kitchen, thermalProp));
		env.addDeviceModel(heaterLiving, new HeaterModel(heaterLiving, living, thermalProp));
		env.addDeviceModel(heaterBath1, new HeaterModel(heaterBath1, bath1, thermalProp));
		env.addDeviceModel(heaterBath2, new HeaterModel(heaterBath2, bath2, thermalProp));
		env.addDeviceModel(whBath1, new ElectricWaterHeaterModel(whBath1, bath1, schWH, 60, "wd.txt", whBath1));
		env.addDeviceModel(heaterBed1, new HeaterModel(heaterBed1, bedroom1, thermalProp));
		env.addDeviceModel(heaterBed2, new HeaterModel(heaterBed2, bedroom2, thermalProp));
		
		// Configure DSO
		DSO.setContractType(1); // 0=DLC, 1=ULC
		//String[] dlcDevs = {"heaterLiving, heaterKitchen"};
		//DSO.setDLCControllableDevices(dlcDevs);
		
		// Configure CSS
		String cssNetAddress = "http://localhost:8888";
		env.setCSS(cssNetAddress);
		
		// Configure simulation time
		int simTime = 1*24*60*60; // 1 time step is 1 second
		
		// Prepare data if not ready
		//NordpoolDataFormatter.parseAndWrite(1, 2010, "Trondheim");
		//RawPriceDataFormatter.parseAndWrite(1, 2006, "Orkdal");
		//WaterDemandUtility.generateMonthlyData("wd-120.txt", 1, 2010, 4);
		
		SimulationModel sim = new SimulationModel(env);
		sim.setGeneratePowerReductionRequest(false);
		
		// Register and activate policies in CSS
		no.ntnu.item.smash.css.core.SystemContext context = new no.ntnu.item.smash.css.core.SystemContext();
		context.setThermalProperties(thermalProp);
		int id = context.getPgc().registerPolicy("D:/Java/kepler/workspace/SMASHCSS/policies2/p1_price.xet.xml", "cost");
		TimeTrigger trigger = new TimeTrigger(Trigger.TYPE_TIME, Trigger.COND_EQUALTO, 0);
		
		context.getPgc().registerPolicyTrigger(id, trigger);
		context.getPgc().activatePolicy(id);
		
		sim.setCSSSuspended(false);
		sim.startSimulation(simTime, 6, 1, 2010, "experiments/Case1PH_H");
	}
	
}
