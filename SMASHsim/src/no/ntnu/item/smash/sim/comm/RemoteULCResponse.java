package no.ntnu.item.smash.sim.comm;

import java.util.HashMap;

public class RemoteULCResponse {

	public static final int OK = 1;
	public static final int REJECT = 0;
	public static final int PARTIAL = 2;
	
	private String id;
	private int code;
	private HashMap<String,Object> data = new HashMap<String,Object>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public HashMap<String, Object> getData() {
		return data;
	}
	public void setData(HashMap<String, Object> data) {
		this.data = data;
	}
	
	
	
}
