package no.ntnu.item.smash.css.comm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.Executors;

import no.ntnu.item.smash.css.core.SystemContext;
import no.ntnu.item.smash.css.structure.Trigger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class CSSEndPoint {

	private SystemContext context;
	
	public CSSEndPoint(SystemContext context) {
		this.context = context;
		startServer();
	}

	private void startServer() {
		InetSocketAddress addr = new InetSocketAddress(8888);
		HttpServer server;
		try {
			server = HttpServer.create(addr, 0);
			RequestHandler handler = new RequestHandler();
			ExternalEventHandler ehandler = new ExternalEventHandler();
			ehandler.setContext(context);
			server.createContext("/", handler);
			server.createContext("/external-event", ehandler);
			server.setExecutor(Executors.newCachedThreadPool());
			server.start();
			System.out.println("CSS server is listening on port 8888");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class RequestHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange ex) throws IOException { 
			// request comes in JSON
			ObjectMapper map = new ObjectMapper();
			RemoteRequestObject reqObj = map.readValue(
					ex.getRequestBody(), RemoteRequestObject.class);
			
			int hour = reqObj.getHour();
			int day = reqObj.getDay();
			int month = reqObj.getMonth();
			int year = reqObj.getYear();
			TimeSynchronizer.setTime(hour, day, month, year);
		}
	}

	static class ExternalEventHandler implements HttpHandler {
		
		private SystemContext context;
		
		@Override
		public void handle(HttpExchange ex) throws IOException { 
			// request comes in JSON
			ObjectMapper map = new ObjectMapper();
			RemoteExternalEventObject eventObj = map.readValue(
					ex.getRequestBody(), RemoteExternalEventObject.class);
			
			String source = eventObj.getSource();
			int eventTime = eventObj.getEventTime();
			HashMap<String,Object> data = eventObj.getData();
			data.put("mdata-"+Trigger.TYPE_EXTERNAL_EVENT, source);
			
			// create an ExternalEvent
			context.getMon().getExternalEventMonitor().addEvent(data, eventTime);
		}
		
		public void setContext(SystemContext c) {
			context = c;
		}
	}
	
}
