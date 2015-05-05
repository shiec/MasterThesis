package no.ntnu.item.smash.sim.model;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.HouseholdStatistics;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.WMPlan;
import no.ntnu.item.smash.sim.structure.WMSchedule;
import no.ntnu.item.smash.sim.structure.WashingMachine;
import no.ntnu.item.smash.sim.structure.Room;

public class WashingMachineModel extends DeviceModel {
	
	/* Variables */
	private WashingMachine machine;
	private double[] plan;
	private String[] schedule;
	
	// simulation
	private boolean run = false;
	private boolean machineOpen; // 5 min resolution
	private HashMap<String, Double[]> processTimeStat;
	private HashMap<String, Double[]> processWattStat;
	private Double[] timeChain = {};
	private Double[] accumulatedTime = {};
	private Double[] wattChain = {};
	private double[] extendedPlan = new double[288];
	double startTime;
	private int dayOfWeek;

	public WashingMachineModel(Device device, Room room, WMPlan plan) {
		super(device, room);
		this.machine = (WashingMachine)device;
		this.plan = plan.getWeeklyPlan();
		//this.schedule = new String[]{"synthetics_40_1400","","","","wool_40_1000","",""};  // For Testing Case4;Case5
		this.schedule = new String[]{"wool_40_1000","","","","synthetics_40_1400","",""}; // For Testing Case6
		//this.schedule = new String[]{"","","","synthetics_40_1400","","","wool_40_1000"}; // For Testing Case1;Case2;Case3
		//this.schedule = WMSchedule.createProgramSchedule(Arrays.copyOf(this.plan, this.plan.length)); //The original design
		processTimeStat = machine.getProcessTimeStat();
		processWattStat = machine.getProcessWattStat();
	}

	public double[] getPlan() {
		return plan;
	}
	
	public double[] getExtendedPlan() {
		return extendedPlan;
	}
	
	public String[] getSchedule() {
		return schedule;
	}
	
	public synchronized void updateSchedule(double[] plan) {
		System.out.println("[POLICY] Schedule changed for " + room.getName() + " " +currentDay);
		extendedPlan = plan;
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
		int day = 0;
		int dayTime = 24*60*60;
		double beginSecond = 0.0;
		double machineOnTime = 0.0;
		double totalTime = 0.0;
		
		model.synchTimeToCSS(second / 3600, currentDay, currentMonth,
				currentYear);
		stat.writeYearHeadingToFiles(currentYear);
		Calendar calendar = Calendar.getInstance();
		calendar.set(currentYear, currentMonth-1, currentDay);
		dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		extendedPlan = createHourlyPlan(dayOfWeek);
		
		while (run) {		
			if (isControlPeriod(second) && DSO.getContractType() == 0) {
				machineOpen = false;
			}else if (extendedPlan[(second / 300) % 288] > 0.0){
				for(int i = 0; i < extendedPlan.length; i++){
					if(extendedPlan[i] == 1) {
						startTime = (double)i / (double)12;
						break;
					}
				}
				//startTime = plan[dayOfWeek];
				beginSecond = day*dayTime + startTime*3600;
				if(second >= beginSecond){
					machineOpen = true;
				}
			} else{
				machineOpen = false;
			}

			// when the machine is open, it gets the time needed for one wash
			if (machineOpen) {
				machineOnTime++;
				totalTime++;
			}
			
			if (second > 0 && second % 300 == 299) {
			    int index = second / 300;
			    if (accumulatedTime.length>0) {
					if(totalTime <= accumulatedTime[0]*60 && totalTime > 0){
						hourlyEnergy[index % 288] = (wattChain[0] * ((double) machineOnTime / (double) 3600)) * 0.001;
						hourlyPeakLoad[index % 288] = (machineOnTime>300*0.2?wattChain[0]:0);
					}
					for(int i = 1; i < timeChain.length; i++) {
						if(totalTime <= accumulatedTime[i]*60 && totalTime > accumulatedTime[i-1]*60) {
							if((totalTime - machineOnTime) < accumulatedTime[i-1]*60){
								hourlyEnergy[index % 288] = (wattChain[i-1] * ((double) (accumulatedTime[i-1]*60 - (totalTime - machineOnTime)) / (double) 3600)) * 0.001
															+ (wattChain[i] * ((double) (totalTime - accumulatedTime[i-1]*60) / (double) 3600)) * 0.001;
								//System.out.println((index % 288)+": "+hourlyEnergy[index % 288]);
								hourlyPeakLoad[index % 288] = (machineOnTime>300*0.2?Math.max(wattChain[i-1], wattChain[i]):0);
							} else {
								hourlyEnergy[index % 288] = (wattChain[i] * ((double) machineOnTime / (double) 3600)) * 0.001;
								//System.out.println((index % 288)+": "+hourlyEnergy[index % 288]);
								hourlyPeakLoad[index % 288] = (machineOnTime>300*0.2?wattChain[i]:0);
							}
						}
					}
			    }
			    //if(hourlyEnergy[index%288]>0)System.out.println(index % 288 +"SS"+hourlyEnergy[index%288]);
				hourlyPrice[index % 288] = DSO.getActualElectricityPrice(second, currentDay,currentMonth, currentYear);
				hourlyCost[index % 288] = hourlyPrice[index % 288] * hourlyEnergy[index % 288];
				machineOnTime = 0;
			}
			
			// if we reach 1 hour
			if (second > 0 && second % 3600 == 3599) { 
				if (second+1 < simTime && second % (24 * 3600) != (24 * 3600 - 1)) {
					model.synchTimeToCSS((second+1) / 3600, currentDay,
							currentMonth, currentYear);
				}				

				// if we reach 24 hours
				if (second % (24 * 3600) == (24 * 3600 - 1)) {
					totalTime = 0;
					day++;
					// store stat data
					stat.addEnergyData(hourlyEnergy);
					stat.addNewPlanData(extendedPlan);
					stat.addScheduleData(extendedPlan);
					stat.addPeakLoadData(hourlyPeakLoad);
					dayOfWeek++;
					dayOfWeek %= 7;
					extendedPlan = createHourlyPlan(dayOfWeek);
					
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
					
					hourlyEnergy = new double[288];
					
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
						//schedule = WMSchedule.createProgramSchedule(Arrays.copyOf(plan, plan.length));  //the original design
						//schedule = new String[]{"","","","synthetics_40_1400","","","wool_40_1000"};   // For Testing Case1;Case2;Case3
						schedule = new String[]{"wool_40_1000","","","","synthetics_40_1400","",""};	 // For Testing Case6
						//schedule = new String[]{"synthetics_40_1400","","","","wool_40_1000","",""};   //For Testing Case4;Case5
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
	
	public double[] createHourlyPlan(int day) {
		double[] newPlan = new double[288];
		int i;
		if(plan[day] != 0) {
			timeChain = processTimeStat.get(schedule[day]);
			wattChain = processWattStat.get(schedule[day]);
			accumulatedTime = new Double[timeChain.length];
			accumulatedTime[0] = timeChain[0];
			for(int j = 1; j < timeChain.length; j++){
				accumulatedTime[j] = timeChain[j] + accumulatedTime[j-1];
			}
			
			for(i = (int)Math.round(plan[day]*12); i < Math.ceil(plan[day]*12) + accumulatedTime[accumulatedTime.length-1]/5; i++){
				newPlan[i] = 1.0;
			}
		}
		else{
			timeChain = new Double[]{};
			wattChain = new Double[]{};
			accumulatedTime = new Double[]{};
			
		}
		return newPlan;
	}
	
}
