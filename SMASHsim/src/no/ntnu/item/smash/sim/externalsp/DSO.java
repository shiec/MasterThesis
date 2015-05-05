package no.ntnu.item.smash.sim.externalsp;

import java.util.HashMap;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.PriceDataParser;
import no.ntnu.item.smash.sim.structure.DLCRequest;
import no.ntnu.item.smash.sim.structure.PowerReductionRequest;
import no.ntnu.item.smash.sim.structure.ULCRequest;

public class DSO {

	private static int CONTRACT_TYPE = 0; // DLC = 0, ULC = 1
	private static String[] DLC_DEVICES;	
	
	private static double[][] ESP_EST_PRICES;
	private static double[][] DSO_EST_PRICES;

	private static double[][] ESP_ACTUAL_PRICES;
	private static double[][] DSO_ACTUAL_PRICES;
	
	private static int currentDay = -1;
	private static int dataMonth = -1;
	private static int dataYear = -1;
	
	private static HashMap<String, PowerReductionRequest> controlRequests = new HashMap<String, PowerReductionRequest>();
	
	public static void setCurrentDay(int day) {
		currentDay = day;
	}

	public static int getCurrentDay() {
		return currentDay;
	}
	
	public static double[] getEstESPPricesForDay(int day, int month, int year) {
		if(month!=dataMonth && year!=dataYear) initMonthlyData(month, year);
		
		double[] toReturn = new double[24];
		
		for(int i=0; i<24; i++) {
			toReturn[i] = ESP_EST_PRICES[day-1][i];
		}
		
		return toReturn;
	}
	
	public static double[] getEstDSOPricesForDay(int day, int month, int year) {
		if(month!=dataMonth && year!=dataYear) initMonthlyData(month, year);
		
		double[] toReturn = new double[24];
		
		for(int i=0; i<24; i++) {
			toReturn[i] = DSO_EST_PRICES[day-1][i];
		}
		
		return toReturn;
	}
	
	public static double[] getActESPPricesForDay(int day, int month, int year) {
		if(month!=dataMonth && year!=dataYear) initMonthlyData(month, year);
		
		double[] toReturn = new double[24];
		
		for(int i=0; i<24; i++) {
			toReturn[i] = ESP_ACTUAL_PRICES[day-1][i];
		}
		
		return toReturn;
	}
	
	public static double[] getActDSOPricesForDay(int day, int month, int year) {
		if(month!=dataMonth && year!=dataYear) initMonthlyData(month, year);
		
		double[] toReturn = new double[24];
		
		for(int i=0; i<24; i++) {
			toReturn[i] = DSO_ACTUAL_PRICES[day-1][i];
		}
		
		return toReturn;
	}
	
	public static double getEstESPPrice(int time, int day, int month, int year) { // time means time
															// step which is in
															// seconds
		int hourOfDay = 0;
		
		if((time/3600)%24==0) hourOfDay = 0;
		else hourOfDay = (time/3600)%24;
		
		if(month==dataMonth && year==dataYear) {
			return ESP_EST_PRICES[day-1][hourOfDay];
		} else {
			initMonthlyData(month, year);
			
			return ESP_EST_PRICES[day-1][hourOfDay];
		}
	}

	public static double getEstDSOPrice(int time, int day, int month, int year) { // time means time
															// step which is in
															// seconds
		int hourOfDay = 0;
		
		if((time/3600)%24==0) hourOfDay = 0;
		else hourOfDay = (time/3600)%24;
		
		if(month==dataMonth && year==dataYear) {
			return DSO_EST_PRICES[day-1][hourOfDay];
		} else {
			initMonthlyData(month, year);
			
			return DSO_EST_PRICES[day-1][hourOfDay];
		}
	}
	
	public static double getActualESPPrice(int time, int day, int month, int year) {
		int hourOfDay = 0;
		
		if((time/3600)%24==0) hourOfDay = 0;
		else hourOfDay = (time/3600)%24;
		
		if(month==dataMonth && year==dataYear) {
			if(ESP_ACTUAL_PRICES[day-1][hourOfDay]!=9999) {
				return ESP_ACTUAL_PRICES[day-1][hourOfDay];
			} else {
				// TODO: not sure what to do
			}
		} else { 
			initMonthlyData(month, year);
		
			return ESP_ACTUAL_PRICES[day-1][hourOfDay];
		}
		
		return 9999;
	}
	
	public static double getActualDSOPrice(int time, int day, int month, int year) {
		int hourOfDay = 0;
		
		if((time/3600)%24==0) hourOfDay = 0;
		else hourOfDay = (time/3600)%24;
		
		if(month==dataMonth && year==dataYear) {
			if(DSO_ACTUAL_PRICES[day-1][hourOfDay]!=9999) {
				return DSO_ACTUAL_PRICES[day-1][hourOfDay];
			} else {
				// TODO: not sure what to do
			}
		} else { 
			initMonthlyData(month, year);
		
			return DSO_ACTUAL_PRICES[day-1][hourOfDay];
		}
		
		return 9999;
	}
	
	public static double getCurrentActualESPPrice(int time) {
		int hourOfDay = (time/3600)%24;
		return DSO_ACTUAL_PRICES[currentDay-1][hourOfDay];
	}
	
	public static double getCurrentActualDSOPrice(int time) {
		int hourOfDay = (time/3600)%24;
		return ESP_ACTUAL_PRICES[currentDay-1][hourOfDay];
	}
	
	public static double[] getEstElectricityPrices(int day, int month, int year) {
		double[] prices = new double[24];
		double[] dso = getEstDSOPricesForDay(day, month, year);
		double[] esp = getEstESPPricesForDay(day, month, year);
		
		for(int i=0; i<prices.length; i++) {
			prices[i] = dso[i] + esp[i];
		}
		
		return prices;
	}
	
	public static double getEstElectricityPrice(int time, int day, int month, int year) {
		return getEstDSOPrice(time, day, month, year) + getEstESPPrice(time, day, month, year);
	}

	public static double getActualElectricityPrice(int time, int day, int month, int year) {
		return getActualESPPrice(time, day, month, year) + getActualDSOPrice(time, day, month, year);
	}
	
	public static double getEstAvgESPPriceForDay(int day, int month, int year) {
		double avg=0;
		
		for(int i=0; i<24; i++) {
			avg += getEstESPPrice(i*3600, day, month, year);
		}
		
		return avg/24;
	}
	
	public static double getEstAvgElectricityPriceForDay(int day, int month, int year) {
		double avg=0;
		
		for(int i=0; i<24; i++) {
			avg += getEstElectricityPrice(i*3600, day, month, year);
		}
		
		return avg/24;
	}
	
	public static double getActAvgElectricityPriceForDay(int day) {
		double avg=0;
		
		for(int i=0; i<24; i++) {
			avg += DSO_ACTUAL_PRICES[day-1][i] + ESP_ACTUAL_PRICES[day-1][i];
		}
		
		return avg/24;
	}
	
	public static void initMonthlyData(int month, int year) {
		
		// ACTUAL
		// DSO
		DSO_ACTUAL_PRICES = PriceDataParser.readPriceForMonth(PriceDataParser.DSO, month, year);
		
		// ESP
		ESP_ACTUAL_PRICES = PriceDataParser.readPriceForMonth(PriceDataParser.ESP, month, year);
		
		// ESTIMATED
		// calculate estimated price based on the actual price (inverse of reality) - est tends to be lower
		
		DSO_EST_PRICES = new double[Constants.getNumDaysInMonth(month, year)][24];
		ESP_EST_PRICES = new double[Constants.getNumDaysInMonth(month, year)][24];
		
		// DSO
		for(int d=0; d<Constants.getNumDaysInMonth(month, year); d++) {
			for(int i=0; i<24; i++) {			
				if(i>0 && i<8)
					DSO_EST_PRICES[d][i] = DSO_ACTUAL_PRICES[d][i];
				else if(i>=8 && i<14)
					DSO_EST_PRICES[d][i] = DSO_ACTUAL_PRICES[d][i];// - (Math.random()<0.6?(Math.random()*0.05):0);
				else if(i>=14 && i<17)
					DSO_EST_PRICES[d][i] = DSO_ACTUAL_PRICES[d][i];
				else if(i>=17 && i<20)
					DSO_EST_PRICES[d][i] = DSO_ACTUAL_PRICES[d][i];// - (Math.random()<0.7?(Math.random()*0.03):0);
				else
					DSO_EST_PRICES[d][i] = DSO_ACTUAL_PRICES[d][i];
			}
		}
		
		// ESP
		for(int d=0; d<Constants.getNumDaysInMonth(month, year); d++) {
			for(int i=0; i<24; i++) {			
				if(i>0 && i<8)
					ESP_EST_PRICES[d][i] = ESP_ACTUAL_PRICES[d][i];
				else if(i>=8 && i<14)
					ESP_EST_PRICES[d][i] = ESP_ACTUAL_PRICES[d][i];// - (Math.random()<0.6?(Math.random()*0.05):0);
				else if(i>=14 && i<17)
					ESP_EST_PRICES[d][i] = ESP_ACTUAL_PRICES[d][i];
				else if(i>=17 && i<20)
					ESP_EST_PRICES[d][i] = ESP_ACTUAL_PRICES[d][i];// - (Math.random()<0.7?(Math.random()*0.03):0);
				else
					ESP_EST_PRICES[d][i] = ESP_ACTUAL_PRICES[d][i];
				
			}
		}
		
		dataMonth = month;
		dataYear = year;
	}

	public static void setContractType(int type) {
		CONTRACT_TYPE = type;
	}
	
	public static int getContractType() {
		return CONTRACT_TYPE;
	}
	
	public static void setDLCControllableDevices(String[] devs) {
		DLC_DEVICES = devs;
	}
	
	public static PowerReductionRequest checkPowerReductionRequest(int day, int month, int year) {
		return controlRequests.get(day + "" + month + "" + year);
	}
	
	/*public static PowerReductionRequest checkPowerReductionRequest(int day, int month, int year) {
		double toCompare = 0;
		int highPriceHour = 0;
		for(int i=0; i<24; i++) {
			if(getEstDSOPrice(i*3600, day, month, year)>toCompare) {
				toCompare = getEstDSOPrice(i*3600, day, month, year);
				highPriceHour = i;
			}
		}
		
		PowerReductionRequest request = null;
		
		// TODO: random whether there will be a request (maybe 20%?)
		
		switch(CONTRACT_TYPE) {
		case 0: //DLC
			String entityID = DLC_DEVICES[(int)Math.round((DLC_DEVICES.length -1)*Math.random())];
			int startHour = highPriceHour;
			//int startHour = 17;
			int endHour = startHour + 4;
			int cycleDuration = 30;
			int controlDuration = 10;
			request = new DLCRequest(entityID, startHour, endHour, cycleDuration, controlDuration);
			break;
		case 1: //ULC
			startHour = highPriceHour;
			endHour = startHour + 1;
			int kW = 1000;
			request = new ULCRequest(startHour, endHour, kW);
			break;
		default:
			break;
		}
		
		request.setId((int)System.currentTimeMillis());
		
		controlRequests.put(request.getId(), request);
		
		return request;
	}*/
	
	public static void addControlRequest(String id, PowerReductionRequest req) {
		req.setId(id);
		controlRequests.put(id, req);
	}
	
	public static PowerReductionRequest getControlRequest(String id) {
		return controlRequests.get(id);
	}
	
}
