package no.ntnu.item.smash.css.role;

import java.util.HashMap;

public interface Reasoner {

	/* TODO: make it return PolicyResult later */
	public Object processPolicy(int policyID, String policyFilePath, HashMap<String,Object> data);
	
}
