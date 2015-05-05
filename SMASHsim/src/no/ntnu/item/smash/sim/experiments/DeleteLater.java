package no.ntnu.item.smash.sim.experiments;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DeleteLater {

	/*
	 * Temp class written to parse energy consumption of a result file
	 */
	
	public static void main(String[] args) {		
		DeleteLater.writeResults("experiments/CollectData/LivingRoom_energy.txt");
		DeleteLater.writeResults("experiments/CollectData/Kitchen_energy.txt");
		DeleteLater.writeResults("experiments/CollectData/Bathroom1_energy.txt");
		DeleteLater.writeResults("experiments/CollectData/Bathroom2_energy.txt");
	}
	
	public static void writeResults(String path) {
		System.out.println("File: " + path);
		
		BufferedReader br = null;
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(path));
 
			sCurrentLine = br.readLine();
			sCurrentLine = br.readLine();
			
			int line = 0;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				String[] daily = sCurrentLine.split(", ");
				double print = 0;
				for(int i=0;i<12;i++) {
					print+=Double.parseDouble(daily[i]);
				}
				
				System.out.println((line+1) + ": " + print);
				
				line++;
				if(line==12) break;
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
		
		System.out.println();
	}
	
}
