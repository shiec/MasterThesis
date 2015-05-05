package no.ntnu.item.smash.sim.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class EKlimaDataFormatter {

	public static final String SRC_PATH = "rawdata/weather/";
	public static final String DEST_PATH = "data/weather/";
	
	public static void parseAndWrite(int month, int year, String city) {
		String fileName = "eKlima_" + Constants.getMonthName(month).toUpperCase() + year + "_" + city + ".txt";
		
		File file = new File(DEST_PATH + "W" + (month<10?"0"+month:month) + year + ".txt");
		//if file doesnt exists, then create it
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				file.delete();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(file,true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(SRC_PATH + fileName));
 
			int line = 0;
			String dailyTemp = "";
			
			br.readLine(); // 1st line is only the heading
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				String[] hourly = sCurrentLine.split(";");
				
				if(line==0) {
					dailyTemp = hourly[5];
					
					line++;
				} else if(line<24) {
					dailyTemp = dailyTemp + "," + hourly[5];
					
					line++;
				} else { // line=24, start a new day
					pw.println(dailyTemp);
					
					line = 1;
					dailyTemp = hourly[5];
				}
			}
			
			pw.println(dailyTemp);
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
				pw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
}
