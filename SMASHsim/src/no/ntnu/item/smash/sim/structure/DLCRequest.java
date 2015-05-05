package no.ntnu.item.smash.sim.structure;

public class DLCRequest extends PowerReductionRequest {

	private Device entity;
	private int startHour;
	private int endHour;
	private int cycleDuration; // min
	private int controlDuration; // min
	
	public DLCRequest() {
		
	}
	
	public DLCRequest(Device entity, int startTime, int endTime, int cycleDuration, int controlDuration) {
		this.entity = entity;
		this.startHour = startTime;
		this.endHour = endTime;
		this.cycleDuration = cycleDuration;
		this.controlDuration = controlDuration;
	}
	
	public Device getEntity() {
		return entity;
	}
	public void setEntity(Device entity) {
		this.entity = entity;
	}
	public int getStartHour() {
		return startHour;
	}
	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}
	public int getEndHour() {
		return endHour;
	}
	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}
	public int getCycleDuration() {
		return cycleDuration;
	}
	public void setCycleDuration(int cycleDuration) {
		this.cycleDuration = cycleDuration;
	}
	public int getControlDuration() {
		return controlDuration;
	}
	public void setControlDuration(int controlDuration) {
		this.controlDuration = controlDuration;
	}
	
	
}
