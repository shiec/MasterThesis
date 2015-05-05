package no.ntnu.item.smash.sim.structure;

import java.util.HashMap;
import java.util.Random;

public class WashingMachine extends Device{
	private static final HashMap<String,Double[]> processTimeStat = new HashMap<String, Double[]>();
	private static final HashMap<String,Double[]> processWattStat = new HashMap<String, Double[]>();
	
	static {
		//usage: ("[clothType]_[temperature]_[spinSpeed]", [prepare,heatTime,soakTime,rinseTime,spinTime]), time is in minutes
		processTimeStat.put("synthetics_40_1400",new Double[]{3.0,12.0,32.0,23.0,10.0});
		processTimeStat.put("wool_40_1000",new Double[]{3.0,20.0,7.0,7.0,3.0});
	}
	
	//just for testing process
	static {
		processWattStat.put("synthetics_40_1400",new Double[]{39.58,1856.243,31.54,45.593,371.958});
		processWattStat.put("wool_40_1000",new Double[]{11.375,1151.237,23.247,5.762,285.504});
	}
	
//	static {
//		processWattStat.put("synthetics_40_1400",new Double[]{randNum(4,48),randNum(48,2035),randNum(3,170),randNum(3,167),randNum(4,472)});
//		processWattStat.put("wool_40_1000",new Double[]{randNum(10,48),randNum(387,1798),randNum(3,48),randNum(3,29),randNum(2,455)});
//	}
	
	public static double randNum(double min, double max){
		Random rand = new Random();
		double randomNum = rand.nextDouble()*((max - min) + 1) + min;
	    return randomNum;
	}
	
	public HashMap<String, Double[]> getProcessTimeStat(){
		return processTimeStat;
	}
	
	public HashMap<String, Double[]> getProcessWattStat(){
		return processWattStat;
	}
}
