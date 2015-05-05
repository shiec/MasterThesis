package no.ntnu.item.smash.sim.structure;

import no.ntnu.item.smash.sim.model.WashingMachineModel;

public class WMSchedule {

	private double[] weeklyShedule;

	private static final String[] operationList = { "synthetics_40_1400",
			"wool_40_1000" };

	public WMSchedule() {

	}

	public WMSchedule(double[] schedule) {
		weeklyShedule = schedule;
	}

	public double[] getSchedule() {
		return weeklyShedule;
	}

	public void setSchedule(double[] schedule) {
		this.weeklyShedule = schedule;
	}

	/**
	 * Create a schedule with 5 min resolution from a schedule with 1 hour resolution
	 * @param schedule
	 * @return another schedule with 5 min resolution
	 */
	public static String[] createProgramSchedule(double[] plan) {
		String[] newSchedule = new String[plan.length];

		for (int i = 0; i < plan.length; i++) {
			if (plan[i] != 0) {
				int choice = (int) Math.floor(Math.random()
						* operationList.length);
				newSchedule[i] = operationList[choice];  
			} else {
				newSchedule[i] = "";
			}
		}

		return newSchedule;
	}

	public static void printSchedule(String[] schedule) {
		for (int i = 0; i < schedule.length; i++)
			System.out.print(schedule[i] + ",");
		System.out.println();
	}
}
