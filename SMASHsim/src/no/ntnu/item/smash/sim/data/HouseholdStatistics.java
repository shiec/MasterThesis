package no.ntnu.item.smash.sim.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class HouseholdStatistics {
	
	// Energy consumption HEURISTICS - estimated household energy consumption 
	// with regards to average outdoor temperature and average comfort level
	public static final HashMap<Integer, Double> reducedEnergyConsumptionStat = new HashMap<Integer,Double>();
	public static final HashMap<Integer, Double> normalEnergyConsumptionStat = new HashMap<Integer,Double>();
	
	static {
		normalEnergyConsumptionStat.put(-20, 65.0);
		normalEnergyConsumptionStat.put(-15, 60.0);
		normalEnergyConsumptionStat.put(-10, 55.0);
		normalEnergyConsumptionStat.put(-5, 40.0);
		normalEnergyConsumptionStat.put(0, 35.0);
		normalEnergyConsumptionStat.put(5, 25.0);
		normalEnergyConsumptionStat.put(10, 18.0);
		normalEnergyConsumptionStat.put(15, 12.0);
		normalEnergyConsumptionStat.put(20, 5.0);
		
		reducedEnergyConsumptionStat.put(-20, 62.0);
		reducedEnergyConsumptionStat.put(-15, 56.0);
		reducedEnergyConsumptionStat.put(-10, 47.0);
		reducedEnergyConsumptionStat.put(-5, 35.0);
		reducedEnergyConsumptionStat.put(-0, 32.0);
		reducedEnergyConsumptionStat.put(5, 28.0);
		reducedEnergyConsumptionStat.put(10, 16.0);
		reducedEnergyConsumptionStat.put(15, 11.0);
		reducedEnergyConsumptionStat.put(20, 4.0);
    }
	
	private String resultDirectory = "results";
	private List<double[]> monthlyEnergy = Collections.synchronizedList(new ArrayList<double[]>());
	private List<double[]> monthlyCost = Collections.synchronizedList(new ArrayList<double[]>());
	private List<double[]> monthlyOTemp = Collections.synchronizedList(new ArrayList<double[]>());
	private List<double[]> monthlyPrice = Collections.synchronizedList(new ArrayList<double[]>());
	private List<double[]> monthlyPeakLoad = Collections.synchronizedList(new ArrayList<double[]>());
	
	public HouseholdStatistics(String path) {
		resultDirectory = path;
		
		new File(resultDirectory).mkdirs();
		emptyDirectory();	
		
		InputStream inStream = null;
		OutputStream outStream = null;

    	try{
    		File res = new File("misc/results.html");
    	    File copied = new File(resultDirectory + "/results.html");
    	    
    	    inStream = new FileInputStream(res);
    	    outStream = new FileOutputStream(copied);
 
    	    byte[] buffer = new byte[1024];
 
    	    int length;
    	    while ((length = inStream.read(buffer)) > 0){
     	    	outStream.write(buffer, 0, length);
     	    }
 
    	    inStream.close();
    	    outStream.close(); 
    	}catch(IOException e){
    		e.printStackTrace();
    	}
	}
	
	public synchronized void addEnergyDataAtDay(int day, double[] data) { 
		if(day>monthlyEnergy.size())
			monthlyEnergy.add(Arrays.copyOf(data, data.length));
		else
			monthlyEnergy.set(day-1, mergeData(monthlyEnergy.get(day-1), data));
	}
	
	public synchronized void addCostDataAtDay(int day, double[] data) { 
		if(day>monthlyCost.size())
			monthlyCost.add(Arrays.copyOf(data, data.length));
		else
			monthlyCost.set(day-1, mergeData(monthlyCost.get(day-1), data));
	}
	
	public synchronized void addOTempDataAtDay(int day, double[] data) {
		if(day>monthlyOTemp.size())
			monthlyOTemp.add(Arrays.copyOf(data, data.length));
	}
	
	public synchronized void addPriceDataAtDay(int day, double[] data) {
		if(day>monthlyPrice.size())
			monthlyPrice.add(Arrays.copyOf(data, data.length));
	}
	
	public synchronized void addPeakLoadDataDay(int day, double[] data) {
		if(day>monthlyPeakLoad.size())
			monthlyPeakLoad.add(Arrays.copyOf(data, data.length));
		else
			monthlyPeakLoad.set(day-1, mergeData(monthlyPeakLoad.get(day-1), data));
	}
	
	public synchronized void addCostData(double[] cost) {
		monthlyCost.add(cost);
	}
	
	private void initializeAll() {
		initializeStat(monthlyEnergy);
		initializeStat(monthlyCost);
		initializeStat(monthlyOTemp);
		initializeStat(monthlyPrice);
		initializeStat(monthlyPeakLoad);
	}
	
	private void initializeStat(List<double[]> stat) {
		stat.clear();
	}
	
	public void writeDataToFiles(int month) {
		if(monthlyEnergy.isEmpty()) return;
		
		writeDataToFile(month, "energy", monthlyEnergy);
		writeDataToFile(month, "cost", monthlyCost);
		writeDataToFile(month, "otemp", monthlyOTemp);
		writeDataToFile(month, "price", monthlyPrice);
		writeDataToFile(month, "peakload", monthlyPeakLoad);
		
		initializeAll();
	}	
	
	public void writeDataToFile(int month, String dataType, List<double[]> data) {
		String toWrite = "\n#M-" + Constants.getMonthName(month) + "\n";
		
		// get the content in bytes
		for(double[] d:data) { 
			toWrite += getDailyDataString(d) + "\n";
		}
		
		writeStringToFile(toWrite, resultDirectory + "/Household_" + dataType + ".txt");
	}
	
	private String getDailyDataString(double[] dailyData) {
		DecimalFormat df = (DecimalFormat)
		        NumberFormat.getNumberInstance(new Locale("en", "US"));
		df.applyPattern("0.0000");
		
		String toReturn = "" + (dailyData[0]==-9999?"null":df.format(dailyData[0]));
		
		for(int i=1; i<dailyData.length; i++) {
			toReturn += ", " + (dailyData[i]==-9999?"null":df.format(dailyData[i]));
		}
		
		return toReturn;
	}
	
	public void writeYearHeadingToFiles(int year) {
		writeYearHeadingToFile(year, "energy");
		writeYearHeadingToFile(year, "cost");
		writeYearHeadingToFile(year, "otemp");
		writeYearHeadingToFile(year, "price");
		writeYearHeadingToFile(year, "peakload");
	}
	
	public void writeYearHeadingToFile(int year, String dataType) {
		String toWrite = "#Y-" + year;
		
		writeStringToFile(toWrite, resultDirectory + "/Household_" + dataType + ".txt");
	}
	
	private void writeStringToFile(String text, String file) {
		FileOutputStream fop = null;
		File f;

		try {
			f = new File(file);
			// if file doesnt exists, then create it
			if (!f.exists()) {
				f.createNewFile();
			}
			
			fop = new FileOutputStream(f, true);			

			// get the content in bytes
			fop.write(text.getBytes());
			
			fop.flush();
			fop.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private void emptyDirectory() {
		File folder = new File(resultDirectory);
		File[] files = folder.listFiles();
		for (File f: files) f.delete();
	}
	
	private double[] mergeData(double[] data1, double[] data2) {
		double[] merged = new double[Math.min(data1.length, data2.length)];
		
		for(int i=0; i<Math.min(data1.length, data2.length); i++) {
			merged[i] = data1[i] + data2[i];
		}
		
		return merged;
	}
	
	public static double getEstEnergyConsumption(int comfortLevel, int temperature) {
		double kwh = 0;
		
		HashMap<Integer,Double> refMap = normalEnergyConsumptionStat;
		if(comfortLevel<2) refMap = reducedEnergyConsumptionStat;
		
		int start = (temperature/5)*5; 
		int next = temperature<0?start-5:start+5;
		
		double kwhStart = refMap.get(start);
		double kwhNext = refMap.get(next);
		
		double step = (kwhNext-kwhStart)/5;
		
		kwh = kwhStart + ((temperature%5)*step);
		
		return kwh;
	}
	
	/*
	 * This method creates a new array of 24-hour time series data from
	 * an input array of wider range time series data
	 */
	public static double[] aggregateData(double[] data, int step, boolean avg) {
		double[] newData = new double[data.length/step];
		
		for(int i=0; i<data.length; i=i+step) {
			double total = 0;
			for(int j=i; j<i+step; j++) {
				total += data[j];
			}
			
			if(avg) newData[i/step] = total/step;
			else newData[i/step] = total;
		}
	
		return newData;
	}
	
}
