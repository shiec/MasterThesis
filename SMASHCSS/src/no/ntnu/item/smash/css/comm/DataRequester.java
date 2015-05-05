package no.ntnu.item.smash.css.comm;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataRequester {

	public static HashMap<String,Object> getRemoteData(String provider, String operation, HashMap<String,Object> parameters) {
		HashMap<String,Object> results = new HashMap<String,Object>();
		
		try {
			URL url = new URL("http://localhost:8080/dataendpoint");
			
			RequestObject ro = new RequestObject();
			ro.setProvider(provider);
			ro.setGetOperation(operation);
			ro.setParameters(parameters);
			
			ObjectMapper mapper = new ObjectMapper();
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setReadTimeout(5000);
			mapper.writeValue(connection.getOutputStream(), ro); 
			InputStream is;
			try {
				is = connection.getInputStream();
				RemoteResponseObject resObj = mapper.readValue(is, RemoteResponseObject.class);
				return resObj.getReturnVal();
			} catch (Exception e) { 
				e.printStackTrace();
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
		
		return results;
	}
	
}
