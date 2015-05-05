package no.ntnu.item.smash.css.comm;

public class TimeSynchronizer {

	public static int hour = -1;
	public static int day;
	public static int month;
	public static int year;
	
	public static int time() {
		return hour;
	}
	
	public static int day() {
		return day;
	}
	
	public static int month() {
		return month;
	}
	
	public static int year() {
		return year;
	}
	
	public static void setTime(int time, int day, int month, int year) {
		TimeSynchronizer.hour = time;
		TimeSynchronizer.day = day;
		TimeSynchronizer.month = month;
		TimeSynchronizer.year = year;
	}
	
}
