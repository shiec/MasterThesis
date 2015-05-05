package no.ntnu.item.smash.css.structure;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import no.ntnu.item.smash.css.comm.SchedulingRequestObject;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RoomSchedulingAction extends DecidedAction {

	private Entity room;
	private ComfortMeasure comfortMeasure;
	private Object targetValue;
	private Date schedulingTime;
	private int duration;
	
	public RoomSchedulingAction() {
		
	}
	
	public RoomSchedulingAction(Entity room, ComfortMeasure measure, Object value, Date time) {
		this.room = room;
		comfortMeasure = measure;
		targetValue = value;
		schedulingTime = time;
	}
	
	public Entity getRoom() {
		return room;
	}

	public void setRoom(Entity room) {
		this.room = room;
	}

	public ComfortMeasure getComfortMeasure() {
		return comfortMeasure;
	}

	public void setComfortMeasure(ComfortMeasure comfortMeasure) {
		this.comfortMeasure = comfortMeasure;
	}

	public Object getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(Object targetValue) {
		this.targetValue = targetValue;
	}

	public Date getSchedulingTime() {
		return schedulingTime;
	}

	public void setSchedulingTime(Date schedulingTime) {
		this.schedulingTime = schedulingTime;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public void execute() {
		// TODO: A SchedulingAction's job is to modify the ActivitySchedule
		
		// TODO: All these will be replaced later
		try {
			URL url = new URL("http://localhost:8080/smashsim");
			
			SchedulingRequestObject ro = new SchedulingRequestObject();
			ro.setEntity(room.getResourceURI());
			ro.setValue((Integer)targetValue);
			ro.setDeviceType(0);
			Calendar cal = Calendar.getInstance();
			cal.setTime(schedulingTime);
			ro.setDuration(duration);
			ro.setStartHour(cal.get(Calendar.HOUR_OF_DAY));
			ro.setStartMin(cal.get(Calendar.MINUTE));
			
			ObjectMapper mapper = new ObjectMapper();
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setDoOutput(true);
			connection.setReadTimeout(1);
			mapper.writeValue(connection.getOutputStream(), ro);
			InputStream is;
			try {
				is = connection.getInputStream();
			} catch (Exception e) { 
				connection.disconnect();
			}		
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
