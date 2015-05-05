package no.ntnu.item.smash.css.core;

import java.util.HashMap;

import no.ntnu.item.smash.css.comm.DataRequester;
import no.ntnu.item.smash.css.comm.TimeSynchronizer;
import no.ntnu.item.smash.css.structure.Trigger;

public class PriceMonitoringThread extends MonitoringThread{
	
	public PriceMonitoringThread(MONMachine parent) {
		super(parent);
		//setMonitoringInterval(3000);
	}

	@Override
	public void run() {
		int hour = -1;
		
		while(running) {
			if(TimeSynchronizer.time()>hour) {  
				hour = TimeSynchronizer.time();
				
				// get the price from the DSO - this blocks until the next time interval
				HashMap<String,Object> parameters = new HashMap<String,Object>();
				parameters.put("time", ""+(3600*((hour%24)+1)));
				parameters.put("day", ""+TimeSynchronizer.day());
				parameters.put("month", ""+TimeSynchronizer.month());
				parameters.put("year", ""+TimeSynchronizer.year());
				double dsoPrice = (Double)DataRequester.getRemoteData("DSO", "getActDSOPrice", parameters).get("price");
				parameters.put("time", ""+(3600*(hour%24)));
				double espPrice = (Double)DataRequester.getRemoteData("DSO", "getActESPPrice", parameters).get("price");
				
				// send price back to the subscriber
				HashMap<String,Object> dataMap = new HashMap<String,Object>();
				dataMap.put("mdata-"+Trigger.TYPE_PRICE_DSO, dsoPrice);
				dataMap.put("mdata-"+Trigger.TYPE_PRICE_ESP, espPrice);
				dataMap.put("mdata-"+Trigger.TYPE_PRICE, dsoPrice+espPrice);
				parent.reportData(dataMap, new int[]{MONMachine.SUBSCRIBE_DATA_ESP_PRICE, MONMachine.SUBSCRIBE_DATA_DSO_PRICE, MONMachine.SUBSCRIBE_DATA_PRICE});
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
