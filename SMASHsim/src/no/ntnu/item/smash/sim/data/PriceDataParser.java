package no.ntnu.item.smash.sim.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PriceDataParser {

	public static final int ESP = 0;
	public static final int DSO = 1;
	public static final String PRICE_DATA_PATH = "data/prices/";
	
	public static double[][] readPriceForMonth(int mode, int month, int year) {
		String prefix = "";
		switch(mode) {
		case ESP:
			prefix = "E";
			break;
		case DSO:
			prefix = "N";
			break;
		}
		
		String filePath = PRICE_DATA_PATH + prefix + (month<10?"0"+month:month) + year + ".txt";
		BufferedReader br = null;
		
		double[][] data = new double[Constants.getNumDaysInMonth(month, year)][24];
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(filePath));
 
			int line = 0;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				String[] daily = sCurrentLine.split(",");
				
				for(int i=0; i<daily.length; i++) {
					data[line][i] = Double.parseDouble(daily[i]);
				}
				
				line++;
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return data;
	}
	
}
