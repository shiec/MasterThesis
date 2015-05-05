package no.ntnu.item.smash.sim.experiments;

import java.util.ArrayList;

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

public class FinnArve {

	public static void main(String[] args) {
		SimEnvironment env = new SimEnvironment();
		
		// Configure devices - 5 heaters
		ArrayList<Device> devKitchen = new ArrayList<Device>();
		ArrayList<Device> devLiving = new ArrayList<Device>();
		
		Heater heaterKitchen = new Heater();
		Heater heaterLiving = new Heater();
		heaterKitchen.setId(1);
		heaterKitchen.setType(Device.DEVTYPE_HEATER);
		heaterKitchen.setWatt(1500);
		heaterLiving.setId(2);
		heaterLiving.setType(Device.DEVTYPE_HEATER);
		heaterLiving.setWatt(1000);
		
		devKitchen.add(heaterKitchen);
		devLiving.add(heaterLiving);
		
		// Configure rooms - 6 rooms (kitchen, living room, bath1, bath2, study, bed)
		double[] vKitchen = {15,15,15,15,15,15,15,15,15,15,16,16,16,16,16,16,16,16,16,16,16,16,16,16};
		double[] vLiving = {16,16,20,20,20,-9999,-9999,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20};
		
		Plan schKitchen = new Plan(vKitchen);
		Plan schLiving = new Plan(vLiving);
		
		Room3 kitchen = new Room3("Kitchen", null, null, 2.5, 6, 2, 2, 1, 1, 0, devKitchen, schKitchen);		
		Room3 living = new Room3("LivingRoom", null, null, 2.5, 3, 11, 4, 1, 1, 0, devLiving, schLiving);
		
		kitchen.areaFloor = 23.68;
		kitchen.areaDoor = 2.8;
		kitchen.areaCeiling = 23.68;
		kitchen.areaWindow = 3.2;
		kitchen.areaEWall = 22.975;
		kitchen.areaIWall = 29.975;
		
		living.areaFloor = 11.4172;
		living.areaDoor = 1.4;
		living.areaCeiling = 11.4172;
		living.areaWindow = 2.4;
		living.areaEWall = 14.675;
		living.areaIWall = 15.675;
		
		env.addRoom(kitchen);
		env.addRoom(living);
		
		// Configure house thermal property
		double[] thermalProp = BuildingThermalIntegrity.NORWAY_MEDINSULATED;
		env.setThermalProperty(thermalProp);
		
		// Configure device-model map
		env.addDeviceModel(heaterKitchen, new HeaterModel(heaterKitchen, kitchen, thermalProp));
		env.addDeviceModel(heaterLiving, new HeaterModel(heaterLiving, living, thermalProp));
		
		// Configure DSO
		DSO.setContractType(1); // 0=DLC, 1=ULC
		//String[] dlcDevs = {"heaterLiving, heaterKitchen"};
		//DSO.setDLCControllableDevices(dlcDevs);
		
		// Configure CSS
		String cssNetAddress = "http://localhost:8888";
		env.setCSS(cssNetAddress);
		
		// Configure simulation time
		int simTime = 1*24*60*60; // 1 time step is 1 second
		//int simTime = 20;
		
		// Prepare data if not ready
		//NordpoolDataFormatter.parseAndWrite(1, 2010, "Trondheim");
		//RawPriceDataFormatter.parseAndWrite(1, 2006, "Orkdal");
		
		SimulationModel sim = new SimulationModel(env);
		sim.setGeneratePowerReductionRequest(false);
		no.ntnu.item.smash.css.core.Main.main(null);
		sim.setCSSSuspended(true);
		sim.startSimulation(simTime, 1, 1, 2010, "finnarve_results");
	}
	
}
