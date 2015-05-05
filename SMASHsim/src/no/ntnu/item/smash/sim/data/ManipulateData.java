package no.ntnu.item.smash.sim.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class ManipulateData {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BufferedReader brE = null;
		BufferedReader brD = null;
		BufferedWriter brT = null;
		File newFile = new File("F:\\energy2012.txt");
		String[][] esp = new String[31][24];
		String[][] dso = new String[31][24];
		String[][] total = new String[31][24];
		double[] sum = new double[31];
		String lineE, lineD;
		int i =0, j=0;
		try {
			
			brE = new BufferedReader(new FileReader("D:\\Java\\kepler\\workspace\\SMASHsim\\data\\prices\\E012012.txt"));
			brD = new BufferedReader(new FileReader("D:\\Java\\kepler\\workspace\\SMASHsim\\data\\prices\\N012012.txt"));
			brT = new BufferedWriter(new FileWriter(newFile));
			//start to read from the file
			while ((lineE = brE.readLine()) != null) {
				esp[i]=lineE.split(",");
				i++;
			}
			
			while((lineD=brD.readLine()) != null) {
				dso[j]=lineD.split(",");
				j++;
			}
			DecimalFormat format = new DecimalFormat("#0.000");
			double temp = 0.0;
			for(int k = 0; k < esp.length; k++){
				for(int m = 0; m< esp[0].length;m++){
					total[k][m] = format.format(Double.parseDouble(esp[k][m]) + Double.parseDouble(dso[k][m]));
					brT.write(total[k][m]+"   ");
				}
				brT.newLine();
				for(int m = 0; m< esp[0].length;m++){
					temp += Double.parseDouble(total[k][m]);
					//System.out.println(temp);
				}
				sum[k] = temp/24.0;
				temp = 0.0;
				System.out.println(new DecimalFormat("#0.000").format(sum[k]));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (brE != null)brE.close();
				if(brD != null)brD.close();
				brT.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
 
	}

}
