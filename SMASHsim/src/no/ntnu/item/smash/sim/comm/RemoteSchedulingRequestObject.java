package no.ntnu.item.smash.sim.comm;

import no.ntnu.item.smash.css.structure.Entity;


public class RemoteSchedulingRequestObject {

	private String entity;
	private String deviceName;
	private int deviceType;
	private int value;
	private int startHour;
	private int startMin;
	private int duration;
	
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String name) {
		this.deviceName = name;
	}
	public int getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public int getStartHour() {
		return startHour;
	}
	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}
	public int getStartMin() {
		return startMin;
	}
	public void setStartMin(int startMin) {
		this.startMin = startMin;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	
	
	
}
