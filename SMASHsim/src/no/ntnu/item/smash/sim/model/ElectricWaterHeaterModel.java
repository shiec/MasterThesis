package no.ntnu.item.smash.sim.model;

import java.util.Arrays;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.ElectricWaterHeaterStatistics;
import no.ntnu.item.smash.sim.data.HouseholdStatistics;
import no.ntnu.item.smash.sim.data.WaterDemandUtility;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.externalsp.WeatherService;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.ElectricWaterHeater;
import no.ntnu.item.smash.sim.structure.Plan;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.Schedule;

public class ElectricWaterHeaterModel extends DeviceModel {

	// constants
	private static final double waterDensity = 1; // kg/litre
	private static final double cp = 4186; // J/Ckg

	// variables
	private double tankHeight; // m
	private double tankDiameter; // m
	private double tankRadius; // m
	private double tankVolume; // litre
	private double surfaceArea; // m^2
	private double rValue;
	private double g;
	private double c; // J/C
	private double targetTemp;
	private double tankTemp; // C
	private double ambientTemp; // C (room temp)
	private double inputTemp;
	private double rdash; // watt/C
	private double b; // watt/C
	private double[][] hourlyWaterDemand;
	private String wdemandData;
	private double waterDemand; // litre/s
	private double wattage;
	private double[] schedule;
	private double[] plan;

	// simulation
	private boolean run = false;
	private double energyInput = 0; // watt

	// statistics
	private double[] hourlyWaterTemp = new double[12*24];
	private double[] waterDemandStat = new double[12*24];

	public ElectricWaterHeaterModel(Device device, Room room, Plan plan,
			double targetTemp, String wdemandData, ElectricWaterHeater wh) {
		super(device, room);
		this.plan = plan.getHourlyPlan();
		this.schedule = Schedule.createHighResolutionSchedule(Arrays.copyOf(this.plan, this.plan.length));
		this.targetTemp = targetTemp;
		this.wdemandData = wdemandData;
		this.tankTemp = targetTemp;

		configureWaterHeater(wh);
		calculateProperties();
	}

	private void configureWaterHeater(ElectricWaterHeater wh) {
		tankHeight = wh.getTankHeight();
		tankDiameter = wh.getTankDiameter();
		tankVolume = wh.getTankVolume();
		wattage = wh.getWatt();
		rValue = wh.getrValue();
		hourlyWaterDemand = WaterDemandUtility.readWaterDemand(wdemandData, currentDay);
	}

	private void calculateProperties() {
		tankRadius = tankDiameter / 2;
		surfaceArea = (2 * 3.14 * (tankRadius * tankRadius))
				+ (2 * 3.14 * tankRadius * tankHeight);
		g = surfaceArea * rValue;
		c = tankVolume * waterDensity * cp;
	}

	public void setStatistics(String path, String name) {
		stat = new ElectricWaterHeaterStatistics(path, name);
	}
	
	public double[] getSchedule() {
		return schedule;
	}

	public double getTankTemperature() {
		return tankTemp;
	}
	
	@Override
	public void startSim(int simTime) {
		this.simTime = simTime;
		run = true;
		new Thread(this).start();
	}

	@Override
	public void stopSim() {
		run = false;
	}

	@Override
	public void updateSchedule(double[] schedule) {
		this.schedule = schedule;
	}

	public void setTargetTemp(double temp) {
		targetTemp = temp;
	}
	
	@Override
	public void run() { 
		int second = 0;
		int heatingOnTime = 0;
		double wDemand = 0;
		
		model.synchTimeToCSS(second / 3600, currentDay, currentMonth,
				currentYear);
		
//		try {
//			Thread.sleep(50);
//			
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		stat.writeYearHeadingToFiles(currentYear);

		while (run) {
			// the heater's thermostat detects the different between the
			// tank water temperature and target temperature
			double difference = tankTemp - targetTemp;

			if (isControlPeriod(second) && DSO.getContractType()==0) {
				energyInput = 0;
			} else if (difference >= 1.5 || schedule[(second / 300) % 288] == 0) {
				energyInput = 0;
			} else if (difference <= -1.5) {
				energyInput = wattage;
			}

			if(energyInput==wattage) heatingOnTime++;
			
			// the tank water temperature fluctuates according to several factors
			if (second < simTime
					&& second % 3600 < 60 * hourlyWaterDemand[1][(second / 3600) % 24])
				waterDemand = hourlyWaterDemand[0][(second / 3600) % 24]
						/ (60 * hourlyWaterDemand[1][(second / 3600) % 24]);
			else
				waterDemand = 0;
			
			wDemand+=waterDemand;
			
			ambientTemp = room.getCurrentTemp();
			double oTemp = WeatherService.getActualTemp(second, currentDay,
					currentMonth, currentYear);
			inputTemp = oTemp + Math.random();
			b = waterDensity * waterDemand * cp;
			rdash = 1.0 / (b + g);
			
			tankTemp = (tankTemp * Math.exp(-1.0 * (1.0 / (rdash * c))))
					+ ((g * rdash * ambientTemp + b * rdash * inputTemp + energyInput
							* rdash) * (1.0 - Math.exp(-1.0 * (1.0 / (rdash * c)))));
			
			if (second>0 && second % 300 == 299) {
				int atHour = second / 300;

				hourlyEnergy[atHour%288] = (wattage * heatingOnTime / 3600) * 0.001;
				hourlyWaterTemp[atHour%288] = tankTemp;
				waterDemandStat[atHour%288] = wDemand;
				hourlyPrice[atHour%288] = DSO
						.getActualElectricityPrice(second, currentDay, currentMonth, currentYear);
				hourlyCost[atHour%288] = hourlyPrice[atHour%288] * hourlyEnergy[atHour%288];
				hourlyOTemp[atHour%288] = oTemp;
				hourlyPeakLoad[atHour % 288] = (heatingOnTime>300*0.2?device.getWatt():0);
				
				heatingOnTime = 0;
				wDemand = 0;
			}
			
			if (second > 0 && second % 3600 == 3599) { 
				if (second+1 < simTime && second % (24 * 3600) != (24 * 3600 - 1)) {
					model.synchTimeToCSS((second+1) / 3600, currentDay,
							currentMonth, currentYear);
				}	

				// if we reach 24 hours
				if (second % (24 * 3600) == (24 * 3600 - 1)) { 
					// store stat data
					stat.addEnergyData(hourlyEnergy);
					((ElectricWaterHeaterStatistics) stat)
							.addWaterTempData(hourlyWaterTemp);
					((ElectricWaterHeaterStatistics) stat)
					.addWaterDemandData(waterDemandStat);
					stat.addPlanData(plan);
					stat.addScheduleData(schedule);
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
						hourlyWaterDemand = WaterDemandUtility.readWaterDemand(wdemandData, currentDay);
						schedule = Schedule.createHighResolutionSchedule(Arrays.copyOf(plan, plan.length));
						model.synchTimeToCSS((second+1) / 3600, currentDay,
								currentMonth, currentYear);
						setCurrentDate(currentDay, currentMonth, currentYear);
					}					
				}

//				try {
//					Thread.sleep(50);
//				} catch (InterruptedException e) {
//
//				}
			}

			if (++second == simTime) { 
				run = false;
				stat.writeDataToFiles(currentMonth);
				model.informSimDone(currentDay, currentMonth, currentYear);
			}
		}
	}
}
