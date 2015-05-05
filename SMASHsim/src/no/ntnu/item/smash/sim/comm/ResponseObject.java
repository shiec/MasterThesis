package no.ntnu.item.smash.sim.comm;

import java.util.HashMap;

public class ResponseObject {

	private HashMap<String,Object> returnVal = new HashMap<String,Object>();

	public HashMap<String, Object> getReturnVal() {
		return returnVal;
	}

	public void setReturnVal(HashMap<String, Object> returnVal) {
		this.returnVal = returnVal;
	}
	
	public void addReturnVal(String name, Object val) {
		returnVal.put(name, val);
	}
}
