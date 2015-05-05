package no.ntnu.item.smash.css.structure;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import no.ntnu.item.smash.css.comm.ULCResponse;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ULCRejectionAction extends DecidedAction {

	private String id;
	private double penalty;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public double getPenalty() {
		return penalty;
	}
	
	public void setPenalty(double penalty) {
		this.penalty = penalty;
	}
	
	@Override
	public void execute() {
		try {
			URL url = new URL("http://localhost:8080/ulcresponse");

			ULCResponse response = new ULCResponse();
			response.setId(id);
			response.setCode(ULCResponse.REJECT);
			HashMap<String, Object> data = new HashMap<String,Object>();
			data.put("penalty", penalty);
			response.setData(data);
			
			ObjectMapper mapper = new ObjectMapper();
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setReadTimeout(1);
			mapper.writeValue(connection.getOutputStream(), response);
			InputStream is;
			try {
				is = connection.getInputStream();
			} catch (Exception e) {
				connection.disconnect();
			}
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
