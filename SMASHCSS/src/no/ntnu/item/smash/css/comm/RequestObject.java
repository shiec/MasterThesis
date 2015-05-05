package no.ntnu.item.smash.css.comm;

import java.util.HashMap;

public class RequestObject {

	private String provider;
	private String getOperation;
	private HashMap<String,Object> parameters = new HashMap<String,Object>();;
	
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public String getGetOperation() {
		return getOperation;
	}
	public void setGetOperation(String getOperation) {
		this.getOperation = getOperation;
	}
	public HashMap<String, Object> getParameters() {
		return parameters;
	}
	public void setParameters(HashMap<String, Object> parameters) {
		this.parameters = parameters;
	}
	public void addParameter(String name, Object value) {
		parameters.put(name, value);
	}
	
	
}
