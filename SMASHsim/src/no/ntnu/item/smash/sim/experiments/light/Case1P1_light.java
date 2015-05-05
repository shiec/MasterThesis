package no.ntnu.item.smash.sim.experiments.light;

import java.util.ArrayList;

import no.ntnu.item.smash.css.structure.TimeTrigger;
import no.ntnu.item.smash.css.structure.Trigger;
import no.ntnu.item.smash.sim.core.SimEnvironment;
import no.ntnu.item.smash.sim.core.SimulationModel;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.model.LightModel;
import no.ntnu.item.smash.sim.structure.BuildingThermalIntegrity;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.Light;
import no.ntnu.item.smash.sim.structure.Room3;
import no.ntnu.item.smash.sim.structure.Plan;

public class Case1P1_light {

	public static void main(String[] args) {
		SimEnvironment env = new SimEnvironment();
		
		// Configure devices - 4 real heaters + 2 dummy heaters 
		ArrayList<Device> devLiving = new ArrayList<Device>();
		ArrayList<Device> devKitchen = new ArrayList<Device>();
		ArrayList<Device> devBath1 = new ArrayList<Device>();
		ArrayList<Device> devBath2 = new ArrayList<Device>();
		ArrayList<Device> devBed1 = new ArrayList<Device>();
		ArrayList<Device> devBed2 = new ArrayList<Device>();

		for(int i = 0; i < 4; i++) {
			Light light = new Light();
			light.setDesignLumen(400);
			light.setWatt(30);
			light.setId(i+1);
			light.setName("Light"+(i+1));
			light.setType(Device.DEVTYPE_LIGHT);
			devLiving.add(light);
		}
		
		// Configure rooms - 6 rooms (kitchen, living room, bath1, bath2, study, bed)
		double[] vLiving = {18,18,18,18,18,18,18,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23};
		double[] vKitchen = {17,17,17,17,17,17,17,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22};
		double[] vBath1 = {26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26};
		double[] vBath2 = {23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23};
		double[] vBed1 = {-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,18,18,18,18,18,18,18,18,18,18,-9999,-9999,-9999,-9999};
		double[] vBed2 = {-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,20,20,20,20,20,20,20,20,20,20,-9999,-9999,-9999,-9999};
		
		double[] lightSch1 = {0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,1,0};
		double[] lightSch2 = {0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0};
		double[] lightSch3 = {0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0};
		double[] lightSch4 = {0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0};
		double[] lightSch5 = {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0};
		double[] lightSch6 = {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0};
		
		Plan schLiving = new Plan(vLiving);
		Plan schKitchen = new Plan(vKitchen);
		Plan schBath1 = new Plan(vBath1);
		Plan schBath2 = new Plan(vBath2);
		Plan schBed1 = new Plan(vBed1);
		Plan schBed2 = new Plan(vBed2);
		
		Plan schL1 = new Plan(lightSch1);
		Plan schL2 = new Plan(lightSch2);
		Plan schL3 = new Plan(lightSch3);
		Plan schL4 = new Plan(lightSch4);
		Plan schL5 = new Plan(lightSch5);
		Plan schL6 = new Plan(lightSch6);
		
		double height = 2.6;
		
		Room3 living = new Room3("LivingRoom", null, null, height, 4, 4, 4, 1, 1, 0, devLiving, schLiving, 0.7, 0.8);
		Room3 kitchen = new Room3("Kitchen", null, null, height, 4, 3, 2, 1, 1, 0, devKitchen, schKitchen);		
		Room3 bath1 = new Room3("Bathroom1", null, null, height, 5, 2.75, 1, 1, 1, 0, devBath1, schBath1);
		Room3 bath2 = new Room3("Bathroom2", null, null, height, 4, 2.5, 1, 1, 1, 0, devBath2, schBath2);
		Room3 bedroom1 = new Room3("Bedroom1", null, null, height, 6, 3, 2, 1, 1, 0, devBed1, schBed1);
		Room3 bedroom2 = new Room3("Bedroom2", null, null, height, 4.5, 3, 2, 1, 1, 0, devBed2, schBed2);
		
		living.areaFloor = 16;
		living.areaDoor = 6;
		living.areaCeiling = 16;
		living.areaWindow = 4;
		living.areaEWall = 7*height + 3*height - 4;
		living.areaIWall = 3*height + 1.5*height + 4*height + 4.5*height - 6;
		living.setLightPlan(schL1);
		
		kitchen.areaFloor = 12;
		kitchen.areaDoor = 2;
		kitchen.areaCeiling = 12;
		kitchen.areaWindow = 2;
		kitchen.areaEWall = 3*height + 4*height - 2;
		kitchen.areaIWall = 3*height + 4*height - 2;
		kitchen.setLightPlan(schL2);
		
		bath1.areaFloor = 13.75;
		bath1.areaDoor = 2;
		bath1.areaCeiling = 13.75;
		bath1.areaWindow = 1;
		bath1.areaEWall = 5*height + 2.5*height - 1;
		bath1.areaIWall = 3*height + 2.5*height + 0.5*height + 2.5*height - 2;
		bath1.setLightPlan(schL3);
		
		bath2.areaFloor = 10;
		bath2.areaDoor = 4;
		bath2.areaCeiling = 0;
		bath2.areaWindow = 1;
		bath2.areaEWall = 4*height + 2.5*height - 1;
		bath2.areaIWall = 4*height + 2.5*height - 4;
		bath2.setLightPlan(schL4);
		
		bedroom1.areaFloor = 0;
		bedroom1.areaDoor = 6;
		bedroom1.areaCeiling = 0;
		bedroom1.areaWindow = 2;
		bedroom1.areaEWall = 6*height + 3*height - 2;
		bedroom1.areaIWall = 6*height + 3*height - 6;
		bedroom1.setLightPlan(schL5);
		
		bedroom2.areaFloor = 0;
		bedroom2.areaDoor = 2;
		bedroom2.areaCeiling = 0;
		bedroom2.areaWindow = 2;
		bedroom2.areaEWall = 3*height + 4.5*height - 2;
		bedroom2.areaIWall = 3*height + 4.5*height - 2;
		bedroom2.setLightPlan(schL6);
		
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
		for(int j = 0; j < 4; j++) {
			env.addDeviceModel(devLiving.get(j), new LightModel(devLiving.get(j), living, schL1));
		}
		
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
		
		//configure policy
		no.ntnu.item.smash.css.core.SystemContext context = new no.ntnu.item.smash.css.core.SystemContext();
		context.setThermalProperties(thermalProp);
		int id = context.getPgc().registerPolicy("C:/Users/shuna/workspace/SMASHCSS/policies_add/pls_price2.xet.xml", "cost");
		TimeTrigger trigger = new TimeTrigger(Trigger.TYPE_TIME, Trigger.COND_EQUALTO, 0);
		
		context.getPgc().registerPolicyTrigger(id, trigger);
		context.getPgc().activatePolicy(id);
		
		//no.ntnu.item.smash.css.core.Main.main(null);
		sim.setCSSSuspended(false);
		sim.startSimulation(simTime, 1, 1, 2010, "experiments1/Case1P1_light");
	}
	
}
