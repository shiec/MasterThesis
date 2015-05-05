package no.ntnu.item.smash.sim.model;

import java.util.ArrayList;
import java.util.Arrays;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.HouseholdStatistics;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.externalsp.WeatherService;
import no.ntnu.item.smash.sim.structure.BuildingThermalIntegrity;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.ElectricWaterHeater;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.Room3;
import no.ntnu.item.smash.sim.structure.Schedule;

public class HeaterModel extends DeviceModel {

	/* Constants */
	// Thermal properties
	private double[] thermalProp = BuildingThermalIntegrity.NORWAY_HEAVYINSULATED;

	// Solar radiation (winter)
	private static final double S = 10000 / (24 * 30); // W/m2
	private static final double INTERNAL = 20;
	private static final double FRACT_S_WALL = 0.3;
	private static final double FRACT_S_AIR = 0.7;
	private static final double DENS_AIR = 1.225;
	private static final double CP_AIR = 1005.4; // J
	
	/* Variables */
	// Thermal properties
	private double c_air;
	
	// simulation
	private boolean run = false;
	private boolean thermostatOn = false;
	private double roomTemp = 21;
	private double massTemp = 21;
	private double[] schRTemp; // 5 min resolution

	public HeaterModel(Device device, Room room, double[] thermalProp) {
		super(device, room);
		configureRoom(room);
		this.thermalProp = thermalProp;
	}

	public void configureRoom(Room room) {
		schRTemp = room.getSchedule().getSchedule();
		roomTemp = schRTemp[schRTemp.length-1]!=-9999?schRTemp[schRTemp.length-1]:20;
		massTemp = schRTemp[schRTemp.length-1];
	}

	public synchronized void updateSchedule(double[] schedule) {
		schRTemp = schedule;
		room.setSchedule(new Schedule(schedule));
		
		//System.out.println("[POLICY] Schedule changed for " + room.getName() + ".");
	}

	public void startSim(int simTime) {
		this.simTime = simTime;
		run = true;
		new Thread(this).start();
	}

	public void stopSim() {
		run = false;
	}

	@Override
	public void run() {
		int second = 0;
		int heatingOnTime = 0; 
		if(device.getId()==1)System.out.println("Day: " + currentDay);
		
		model.synchTimeToCSS(second / 3600, currentDay, currentMonth,
				currentYear);
		
		stat.writeYearHeadingToFiles(currentYear);

		while (run) {			
			// the heater's thermostat detects the difference between the
			// room temperature and target temperature		
			double targetTemp = schRTemp[(second / 300) % 288];
			double difference = roomTemp - targetTemp;

			if (isControlPeriod(second) && DSO.getContractType() == 0) {
				thermostatOn = false;
			} else if (difference > 0.3) {
				thermostatOn = false;
			} else if (difference <= -0.1) {
				thermostatOn = true;
			}

			// when the heater is active (thermostatOn is true), it
			// generates heat input
			double heatInput = 0;
			if (thermostatOn) {
				heatInput = device.getWatt();
				heatingOnTime++;
			}

			// get outdoor temperature
			double oTemp = WeatherService.getActualTemp(second, currentDay,
					currentMonth, currentYear);

			// mass temperature
			Room3 r = (Room3)room;
			double c_mass = 40842*r.getWidth()*r.getLength();
			double H_m = 8.3 * (r.areaEWall + r.areaIWall + (r.getWidth()*r.getLength())); // 8.3 is 5.68*1.46 Btu/hrFft2
			massTemp = massTemp + ((1/c_mass) * (FRACT_S_WALL*S*r.areaWindow + H_m*(roomTemp-massTemp)));
			
			double U_a = r.areaWindow*thermalProp[BuildingThermalIntegrity.U_WINDOW_INDEX] + (r.areaDoor/thermalProp[BuildingThermalIntegrity.R_DOOR_INDEX])
					+ (r.areaEWall/thermalProp[BuildingThermalIntegrity.R_WALLS_INDEX]) + (r.areaCeiling/thermalProp[BuildingThermalIntegrity.R_CEILING_INDEX])
					+ (r.areaFloor/thermalProp[BuildingThermalIntegrity.R_FLOORS_INDEX]);
			double ewhHeat = 0;
			ArrayList<Device> ewhs = r.getDevicesByType(Device.DEVTYPE_EWATERHEATER);
			
			for(int i=0; i<ewhs.size(); i++) {
				ElectricWaterHeater ewh = (ElectricWaterHeater)ewhs.get(i);
				double rVal = ewh.getrValue();
				double surfaceArea = (2 * 3.14 * (ewh.getTankDiameter()/2 * ewh.getTankDiameter()/2))
						+ (2 * 3.14 * ewh.getTankDiameter()/2 * ewh.getTankHeight());
				ElectricWaterHeaterModel ewhModel = (ElectricWaterHeaterModel)model.getEnvironment().getDeviceModelMap().get(ewh);
				ewhHeat += (1/rVal) * surfaceArea * (ewhModel.getTankTemperature() - roomTemp);
			}
//			if(second>=60600 && second<=68700 && device.getId()==1)
//				System.out.println(second + " " + targetTemp + " " + roomTemp + " " + massTemp);
			
			c_air = 3 * (DENS_AIR*3*r.getWidth()*r.getLength()*CP_AIR);
			roomTemp = roomTemp + ((1/c_air) * (heatInput + INTERNAL + FRACT_S_AIR*S*r.areaWindow + ewhHeat + H_m*(massTemp-roomTemp) + U_a*(oTemp-roomTemp)));
			if(second>=60600 && second<=68700 && device.getId()==1)
			//System.out.println(second+" : "+roomTemp+"+"+"((1/"+c_air+")*("+heatInput+"+"+INTERNAL+"+"+FRACT_S_AIR*S*r.areaWindow+"+"+0+"+"+H_m+"*("+massTemp+"-"+roomTemp+")+"+U_a+"*("+oTemp+"-"+roomTemp+")))");
			room.setCurrentTemp(roomTemp);

			if (second > 0 && second % 300 == 299) {
				int index = second / 300;
				hourlyEnergy[index % 288] = (device.getWatt() * ((double) heatingOnTime / (double) 3600)) * 0.001;
				hourlyRTemp[index % 288] = roomTemp;
				hourlyOTemp[index % 288] = oTemp;
				hourlyPrice[index % 288] = DSO.getActualElectricityPrice(second, currentDay,currentMonth, currentYear);
				hourlyCost[index % 288] = hourlyPrice[index % 288] * hourlyEnergy[index % 288];
				hourlyPeakLoad[index % 288] = (heatingOnTime>300*0.2?device.getWatt():0);
				
				heatingOnTime = 0;
			}
			
			// if we reach 1 hour
			if (second > 0 && second % 3600 == 3599) { 
				if (second+1 < simTime && second % (24 * 3600) != (24 * 3600 - 1)) {
					model.synchTimeToCSS((second+1) / 3600, currentDay,
							currentMonth, currentYear);
				}				

				// if we reach 24 hours
				if (second % (24 * 3600) == (24 * 3600 - 1)) {
					// store stat data
					stat.addEnergyData(hourlyEnergy);
					stat.addRTempData(hourlyRTemp);
					stat.addPlanData(room.getPlannedSchedule()
							.getHourlyPlan());
					stat.addScheduleData(schRTemp);
					stat.addPeakLoadData(hourlyPeakLoad);
					
					model.getRoomStat().addEnergyDataAtDay(room, currentDay, HouseholdStatistics.aggregateData(hourlyEnergy, 12, false));
					model.getRoomStat().addCostDataAtDay(room, currentDay, HouseholdStatistics.aggregateData(hourlyCost, 12, false));
					model.getRoomStat().addPeakLoadDataDay(room, currentDay, HouseholdStatistics.aggregateData(hourlyPeakLoad, 12, false));

					model.getEnergyMonitor().addEnergyDataAtDay(currentDay,
							HouseholdStatistics.aggregateData(hourlyEnergy, 12, false));
					model.getEnergyMonitor().addCostDataAtDay(currentDay,
							HouseholdStatistics.aggregateData(hourlyCost, 12, false));
					model.getEnergyMonitor().addOTempDataAtDay(currentDay,
							HouseholdStatistics.aggregateData(hourlyOTemp, 12, true));
					model.getEnergyMonitor().addPriceDataAtDay(currentDay,
							HouseholdStatistics.aggregateData(hourlyPrice, 12, true));
					model.getEnergyMonitor().addPeakLoadDataDay(currentDay,
							HouseholdStatistics.aggregateData(hourlyPeakLoad, 12, true));
					
					// if we reach one month
					if (currentDay++ == Constants.getNumDaysInMonth(
							currentMonth, currentYear)) {

						currentDay = 1;
						stat.writeDataToFiles(currentMonth);
						model.informMonthReached(currentMonth);

						// if we reach one year
						if (currentMonth++ == 12) {
							currentMonth = 1;
							currentYear++;
							if (second + 1 < simTime) {
								stat.writeYearHeadingToFiles(currentYear);
								model.updateCurrentYear(currentYear);
							}
						}
					}
					
					if(second+1 < simTime) {
						if(device.getId()==1)System.out.println("Day: " + currentDay);
						double[] plan = Arrays.copyOf(room.getPlannedSchedule().getHourlyPlan(), 24);
						room.setSchedule(new Schedule(Schedule.createHighResolutionSchedule(plan)));
						model.synchTimeToCSS((second+1) / 3600, currentDay,
								currentMonth, currentYear);
						setCurrentDate(currentDay, currentMonth, currentYear);
					}
				}
			}

			if (++second == simTime) {
				run = false;
				stat.writeDataToFiles(currentMonth);
				model.informSimDone(currentDay, currentMonth, currentYear);
			}
		}

	}
}
