package no.ntnu.item.smash.css.comm;

import java.util.HashMap;

public class RemoteExternalEventObject {

	private String source;
	private HashMap<String,Object> data = new HashMap<String,Object>();
	private int eventTime;
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public HashMap<String, Object> getData() {
		return data;
	}
	
	public void setData(HashMap<String, Object> data) {
		this.data = data;
	}
	
	public void addData(String key, Object value) {
		data.put(key, value);
	}
	
	public int getEventTime() {
		return eventTime;
	}
	
	public void setEventTime(int eventTime) {
		this.eventTime = eventTime;
	}
}
