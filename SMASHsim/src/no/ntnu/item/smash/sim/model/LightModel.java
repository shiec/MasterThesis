package no.ntnu.item.smash.sim.model;

import java.util.Arrays;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.HouseholdStatistics;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.Plan;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.Schedule;
import no.ntnu.item.smash.sim.structure.Light;

public class LightModel extends DeviceModel {
	
	/* Variables */
	private Light light;
	private int lightID;
	private double[] plan;
	private double[] schedule;
	
	// simulation
	private boolean run = false;
	private boolean lightOn; // 5 min resolution

	public LightModel(Device device, Room room, Plan plan) {
		super(device, room);
		light = (Light)device;
		this.lightID = light.getId();
		this.plan = plan.getHourlyPlan();
		this.schedule = Schedule.createHighResolutionSchedule(Arrays.copyOf(this.plan, this.plan.length));
		//room.getIlluminanceStat().setLumen(((Light)device).getDesignLumen());
	}

	public double[] getSchedule() {
		return schedule;
	}
	public synchronized void updateSchedule(double[] schedule) {
		this.schedule = schedule;
		
		System.out.println("[POLICY] Schedule changed for " + room.getName() + " light"+lightID +" "+currentDay);
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
		int lightOnTime = 0; 
		boolean addLight = true; 
		if(device.getId()==2)System.out.println("Day: " + currentDay);
		model.synchTimeToCSS(second / 3600, currentDay, currentMonth,
				currentYear);
		
		stat.writeYearHeadingToFiles(currentYear);

		while (run) {			

			if (isControlPeriod(second) && DSO.getContractType() == 0) {
				lightOn = false;
			} else if (schedule[(second / 300) % 288] == 0) {
				lightOn = false;
				if(!addLight){
					room.getIlluminanceStat().removeLightNum(lightID);
					addLight = true;
				}
			} else if (schedule[(second / 300) % 288] > 0){
				lightOn = true;
				if(addLight){
					room.getIlluminanceStat().addLightNum(lightID);
					addLight = false;
				}
			}

			// when the light is on (lightOn is true), it generates input
			if (lightOn) {
				lightOnTime++;
			}

			if (second > 0 && second % 300 == 299) {
				int index = second / 300;
				hourlyEnergy[index % 288] = (light.getWatt() * ((double) lightOnTime / (double) 3600)) * 0.001;
				//System.out.println(hourlyEnergy[index%288]);
				hourlyPrice[index % 288] = DSO.getActualElectricityPrice(second, currentDay,currentMonth, currentYear);
				hourlyCost[index % 288] = hourlyPrice[index % 288] * hourlyEnergy[index % 288];
				hourlyPeakLoad[index % 288] = (lightOnTime>300*0.2?light.getWatt():0);
				//room.getIlluminanceStat().setIlluminance(index%288);
				
				lightOnTime = 0;
			}
			
			// if we reach 1 hour
			if (second > 0 && second % 3600 == 3599) { 
				if (second+1 < simTime && second % (24 * 3600) != (24 * 3600 - 1)) {
					model.synchTimeToCSS((second+1) / 3600, currentDay,
							currentMonth, currentYear);
				}				

				// if we reach 24 hours
				if (second % (24 * 3600) == (24 * 3600 - 1)) {
					//System.out.println("HH"+currentDay);
					// store stat data
					stat.addEnergyData(hourlyEnergy);
					//stat.addRTempData(hourlyRTemp);
					
					stat.addPlanData(plan);
					stat.addScheduleData(schedule);
					stat.addPeakLoadData(hourlyPeakLoad);
					//room.getIlluminanceStat().addIllumData(currentDay);
					
					model.getRoomStat().addEnergyDataAtDay(room, currentDay, HouseholdStatistics.aggregateData(hourlyEnergy, 12, false));
					model.getRoomStat().addCostDataAtDay(room, currentDay, HouseholdStatistics.aggregateData(hourlyCost, 12, false));
					model.getRoomStat().addPeakLoadDataDay(room, currentDay, HouseholdStatistics.aggregateData(hourlyPeakLoad, 12, false));

					model.getEnergyMonitor().addEnergyDataAtDay(currentDay,
							HouseholdStatistics.aggregateData(hourlyEnergy, 12, false));
					model.getEnergyMonitor().addCostDataAtDay(currentDay,
							HouseholdStatistics.aggregateData(hourlyCost, 12, false));
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
						schedule = Schedule.createHighResolutionSchedule(Arrays.copyOf(plan, plan.length));
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
