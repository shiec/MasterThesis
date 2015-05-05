package no.ntnu.item.smash.sim.data;

import java.util.Calendar;
import java.util.Locale;

public class Constants {
	
	public static int getNumDaysInMonth(int month, int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.YEAR, year);
		
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
	
	public static String getMonthName(int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, month-1);
		
		return cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);
	}
	
	public static int getDayType(int day, int month, int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.YEAR, year);
		
		return cal.get(Calendar.DAY_OF_WEEK);
	}
	
}
