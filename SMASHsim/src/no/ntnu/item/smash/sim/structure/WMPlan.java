package no.ntnu.item.smash.sim.structure;

public class WMPlan {

	private double[] weeklyPlan;

	public WMPlan() {
		
	}
	
	public WMPlan(double[] plan) {
		weeklyPlan = plan;
	}
	
	public double[] getWeeklyPlan() {
		return weeklyPlan;
	}

	public void setWeeklyPlan(double[] weeklyPlan) {
		this.weeklyPlan = weeklyPlan;
	}
	
	public void setValueAtHour(int day, double value) {
		weeklyPlan[day] = value;
	}
}
