package no.ntnu.item.smash.sim.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

import no.ntnu.item.smash.css.core.NxETReasoner;
import no.ntnu.item.smash.sim.comm.ExternalEventObject;
import no.ntnu.item.smash.sim.comm.RemoteRequestObject;
import no.ntnu.item.smash.sim.comm.RemoteSchedulingRequestObject;
import no.ntnu.item.smash.sim.comm.RemoteULCResponse;
import no.ntnu.item.smash.sim.comm.RequestObject;
import no.ntnu.item.smash.sim.comm.ResponseObject;
import no.ntnu.item.smash.sim.data.EnergyUsageStatistics;
import no.ntnu.item.smash.sim.data.HouseholdStatistics;
import no.ntnu.item.smash.sim.data.RoomStatistics;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.externalsp.WeatherService;
import no.ntnu.item.smash.sim.model.DeviceModel;
import no.ntnu.item.smash.sim.model.ElectricWaterHeaterModel;
import no.ntnu.item.smash.sim.model.LightModel;
import no.ntnu.item.smash.sim.model.WashingMachineModel;
import no.ntnu.item.smash.sim.structure.DLCRequest;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.Light;
import no.ntnu.item.smash.sim.structure.PowerReductionRequest;
import no.ntnu.item.smash.sim.structure.Room;
import no.ntnu.item.smash.sim.structure.Schedule;
import no.ntnu.item.smash.sim.structure.ULCRequest;
import no.ntnu.item.smash.sim.structure.WashingMachine;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SimulationModel {

	private SimEnvironment environment;
	private RoomStatistics roomStat;
	private HouseholdStatistics monitor;
	//private RoomIlluminanceStat illuminanceStat;
	private int numSimModel = 0;
	private int hourDone = 0;
	private int monthDone = 0;
	protected int runningHour;
	protected int runningDay;
	protected int runningMonth;
	protected int runningYear;
	private boolean cssSuspended = false;
	private boolean genPowerReductionRequest = false;
	private boolean controlReqSent = false;

	public SimulationModel() {

	}

	public SimulationModel(SimEnvironment environment) {
		this.environment = environment;
	}

	public SimEnvironment getEnvironment() {
		return environment;
	}

	public void setEnvironment(SimEnvironment environment) {
		this.environment = environment;
	}

	public boolean getGeneratePowerReductionRequest() {
		return genPowerReductionRequest;
	}

	public void setGeneratePowerReductionRequest(boolean gen) {
		genPowerReductionRequest = gen;
	}

	public void startSimulation(int simTime, int currentDay, int currentMonth,
			int currentYear, String resultDirectory) {
		monitor = new HouseholdStatistics(resultDirectory);
		//illuminanceStat = new RoomIlluminanceStat();

		roomStat = new RoomStatistics(resultDirectory);
		roomStat.setStartDay(currentDay);

		for(int i=0; i<environment.getRooms().size(); i++) {
			roomStat.addRoom(environment.getRooms().get(i));
		}
		
		updateCurrentYear(currentYear);

		// start the server
		startServer();

		// start all DeviceModels
		HashMap<Device, DeviceModel> modelMap = environment.getDeviceModelMap();
		numSimModel = modelMap.size();
		hourDone = numSimModel;
		monthDone = numSimModel;

		Set<Device> keys = modelMap.keySet();
		for (Device key : keys) {
			DeviceModel dm = modelMap.get(key);

			dm.setSimParent(this);
			dm.setCurrentDay(currentDay);
			dm.setCurrentMonth(currentMonth);
			dm.setCurrentYear(currentYear);
			dm.setStatistics(resultDirectory, dm.getDevice().getName());
			
			dm.startSim(simTime);
		}
	}
	
	public void updateCurrentYear(int year) {
		if (runningYear != year) {
			runningYear = year;
			monitor.writeYearHeadingToFiles(year);
			roomStat.writeYearHeadingToFiles(year);
		}
	}
	
	public void informMonthReached(int month) {
		if (--monthDone == 0) {
			monitor.writeDataToFiles(month);
			roomStat.writeDataToFiles(month);
			monthDone = numSimModel;
		}
	}

	public synchronized void informSimDone(int day, int month, int year) {
		cssSuspended = true;

		System.out.println("Compiling result data.");
		
		if (--numSimModel == 0) {
			monitor.writeDataToFiles(month);
			roomStat.writeDataToFiles(month);
			
			System.out.println("Simulation ended (" + day + "/" + month + "/" + year + ")");
			System.out.println("Estimated reduced cost: " + NxETReasoner.totalCost);
			System.exit(0);
		}
	}	
	
	public HouseholdStatistics getEnergyMonitor() {
		return monitor;
	}
	
	public RoomStatistics getRoomStat() {
		return roomStat;
	}
	
	public void setCSSSuspended(boolean suspended) {
		cssSuspended = suspended;
	}

	public synchronized void synchTimeToCSS(int hour, int day, int month, int year) {
		if(--hourDone>0) {
			synchronized(this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
			
			return;
		}
		
		DSO.setCurrentDay(day);
		hourDone = numSimModel;
		
		if (hour%24 == 0 && !controlReqSent && genPowerReductionRequest) {
			controlReqSent = true;
			PowerReductionRequest req = DSO.checkPowerReductionRequest(day, month, year);

			if (DSO.getContractType() == 0 && req!=null) {
				DLCRequest dlc = (DLCRequest) req;
				environment.getDeviceModelMap().get(dlc.getEntity())
						.setPowerReductionRequest(req, 0);
			} else if (DSO.getContractType() == 1 && req!=null){
				sendControlRequestToCSS(req);
			}
		} else if (hour%24 == 23) {
			controlReqSent = false;
		}

		if (cssSuspended) {
			notifyAll();
			return;
		}
			
		runningHour = hour;
		runningDay = day;
		runningMonth = month;
		runningYear = year;

		try {
			URL url = new URL(environment.getCSS());

			RequestObject ro = new RequestObject();
			ro.setHour(hour);
			ro.setDay(day);
			ro.setMonth(month);
			ro.setYear(year);

			ObjectMapper mapper = new ObjectMapper();
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setReadTimeout(1);
			mapper.writeValue(connection.getOutputStream(), ro);
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
			// e.printStackTrace();
		}
		
		synchronized(this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
	}

	private void sendControlRequestToCSS(PowerReductionRequest req) {
		ExternalEventObject eo = new ExternalEventObject();
		eo.setSource("DSO");
		eo.setEventTime(((ULCRequest) req).getStartHour()%24-1<0?0:((ULCRequest) req).getStartHour()%24-1);
		eo.addData("id", ((ULCRequest) req).getId());
		eo.addData("startTime", ((ULCRequest) req).getStartHour()%24);
		eo.addData("endTime", ((ULCRequest) req).getEndHour()%24+1);
		eo.addData("powerToReduce", ((ULCRequest) req).getKW());
		eo.addData("timeToReduce",
				((((ULCRequest) req).getEndHour()%24+1) - ((ULCRequest) req)
						.getStartHour()%24) * 60);
		eo.addData("freedom", "limited");
		System.out.println("DSO requests power reduction at hour=" + (eo.getEventTime()+1) + " (request sent at " + eo.getEventTime() + ")");
		try {
			URL url = new URL(environment.getCSS() + "/external-event");

			ObjectMapper mapper = new ObjectMapper();
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setReadTimeout(1);
			mapper.writeValue(connection.getOutputStream(), eo);
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
			// e.printStackTrace();
		}
	}

	private void startServer() {
		InetSocketAddress addr = new InetSocketAddress(8080);
		HttpServer server;
		try {
			server = HttpServer.create(addr, 0);
			ServerHandler handler = new ServerHandler();
			handler.setParent(this);
			server.createContext("/smashsim", handler);
			RequestHandler rhandler = new RequestHandler();
			rhandler.setParent(this);
			server.createContext("/dataendpoint", rhandler);
			ULCResponseHandler uhandler = new ULCResponseHandler();
			uhandler.setParent(this);
			server.createContext("/ulcresponse", uhandler);
			SynchHandler shandler = new SynchHandler();
			shandler.setParent(this);
			server.createContext("/done", shandler);
			server.setExecutor(Executors.newCachedThreadPool());
			server.start();
			System.out.println("SMASHsim server is listening on port 8080");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class ServerHandler implements HttpHandler {
		SimulationModel model;

		@Override
		public void handle(HttpExchange ex) throws IOException {
			// request comes in JSON
			ObjectMapper map = new ObjectMapper();
			RemoteSchedulingRequestObject reqObj = map.readValue(
					ex.getRequestBody(), RemoteSchedulingRequestObject.class);

			String entity = reqObj.getEntity();
			int newValue = reqObj.getValue();
			//System.out.println(newValue);
			int duration = reqObj.getDuration();
			int startHour = reqObj.getStartHour();
			int startMin = reqObj.getStartMin();

			Room room = model.getEnvironment().getRoom(entity);
			//System.out.println(entity + " " + newValue);
			if(reqObj.getDeviceType()==Device.DEVTYPE_HEATER) {
				double[] sch = room.getSchedule().getSchedule();
				int begin = startHour * 12 + startMin/5;
				int slot = duration/5 + (duration%5==0?0:1);
				for(int i=0; i<slot; i++) {
					sch[begin+i] = newValue;
				}
				
				for (Device dev : room.getDevicesByType(reqObj.getDeviceType())) {
					DeviceModel devModel = model.getEnvironment()
							.getDeviceModelMap().get(dev);
					devModel.updateSchedule(room.getSchedule().getSchedule());
				}
			} else if(reqObj.getDeviceType()==Device.DEVTYPE_EWATERHEATER) {
				for (Device dev : room.getDevicesByType(reqObj.getDeviceType())) {					
					DeviceModel devModel = model.getEnvironment()
							.getDeviceModelMap().get(dev);
					double[] sch = ((ElectricWaterHeaterModel)devModel).getSchedule();
					int begin = startHour * 12 + startMin/5;
					for(int i=0; i<duration/5; i++) {
						sch[begin+i] = newValue;
					}
					devModel.updateSchedule(sch);
				}
			} else if(reqObj.getDeviceType()==Device.DEVTYPE_LIGHT) {
				String lightName = reqObj.getDeviceName();
				for (Device dev : room.getDevicesByType(reqObj.getDeviceType())) {
					if (dev.getName().equals(lightName)) {
						DeviceModel devModel = model.getEnvironment()
								.getDeviceModelMap().get(dev);
						double[] sch = ((LightModel)devModel).getSchedule();
						int begin = startHour * 12 + startMin/5;
						for(int i=0; i<duration/5; i++) {
							sch[begin+i] = newValue;
						}
						devModel.updateSchedule(sch);
					}
				}
			} else if(reqObj.getDeviceType()==Device.DEVTYPE_WASHINGMACHINE) {
				String machineName = reqObj.getDeviceName();
				for (Device dev : room.getDevicesByType(reqObj.getDeviceType())) {
					if (dev.getName().equals(machineName)) {
						if(duration != 0){
							DeviceModel devModel = model.getEnvironment()
									.getDeviceModelMap().get(dev);
							double[] plan = ((WashingMachineModel)devModel).getExtendedPlan();
							int begin = startHour * 12 + startMin/5;
							int period = duration/5;
							if (duration%5 != 0) period ++;
							for(int i=0; i<period; i++) {
								plan[begin+i] = newValue;
							}
							devModel.updateSchedule(plan);
						}
					}
				}
			}
		}

		private void setParent(SimulationModel model) {
			this.model = model;
		}
	}
	
	static class SynchHandler implements HttpHandler {
		SimulationModel model;

		@Override
		public void handle(HttpExchange ex) throws IOException {
			synchronized(model) { 
				model.notifyAll();
			}
		}

		private void setParent(SimulationModel model) {
			this.model = model;
		}
	}

	static class ULCResponseHandler implements HttpHandler {

		SimulationModel model;

		@Override
		public void handle(HttpExchange ex) throws IOException {
			// requests comes in JSON
			ObjectMapper map = new ObjectMapper();
			RemoteULCResponse response = map.readValue(ex.getRequestBody(),
					RemoteULCResponse.class);
			String id = response.getId();
			int code = response.getCode();
			switch(code) {
			case RemoteULCResponse.OK:
				System.out.println("ULC request ID#" + id + " fulfilled");
				break;
			case RemoteULCResponse.REJECT:
				System.out.println("ULC request ID#" + id + " rejected");
				double penalty = (Double)response.getData().get("penalty");
				double[] added = new double[24];
				added[((ULCRequest)DSO.getControlRequest(id)).getStartHour()] = penalty;
				model.getEnergyMonitor().addCostData(added);
				
				break;
			case RemoteULCResponse.PARTIAL:
				System.out.println("ULC request ID#" + id + " partially fulfilled");
				break;
			}
		}

		private void setParent(SimulationModel model) {
			this.model = model;
		}

	}

	static class RequestHandler implements HttpHandler {
		SimulationModel model;

		@Override
		public void handle(HttpExchange ex) throws IOException { 
			// requests comes in JSON
			ObjectMapper map = new ObjectMapper();
			RemoteRequestObject reqObj = map.readValue(ex.getRequestBody(),
					RemoteRequestObject.class);

			String provider = reqObj.getProvider();
			String operation = reqObj.getGetOperation();
			HashMap<String, Object> parameters = reqObj.getParameters();
			
			ResponseObject resObj = new ResponseObject();

			if (provider.equals("DSO")) {
				if (operation.equals("getEstESPPriceAtHour")) {
					int time = Integer.parseInt((String) parameters.get("time"));
					resObj.addReturnVal("price", DSO.getEstESPPrice(time*3600, model.runningDay, model.runningMonth, model.runningYear));
				} else if (operation.equals("getEstDSOPriceAtHour")) {
					int time = Integer.parseInt((String) parameters.get("time"));
					resObj.addReturnVal("price", DSO.getEstDSOPrice(time*3600, model.runningDay, model.runningMonth, model.runningYear));
				} else if (operation.equals("getActESPPriceAtHour")) {
					int time = Integer.parseInt((String) parameters.get("time"));
					resObj.addReturnVal("price", DSO.getActualESPPrice(time*3600, model.runningDay, model.runningMonth, model.runningYear));
				} else if (operation.equals("getActDSOPriceAtHour")) {
					int time = Integer.parseInt((String) parameters.get("time"));
					resObj.addReturnVal("price", DSO.getActualDSOPrice(time*3600, model.runningDay, model.runningMonth, model.runningYear));
				} else if (operation.equals("getEstElectricityPrice")) {
					int time = Integer.parseInt((String) parameters.get("time"));
					int day = Integer.parseInt((String) parameters.get("day"));
					int month = Integer.parseInt((String) parameters.get("month"));
					int year = Integer.parseInt((String) parameters.get("year"));
					resObj.addReturnVal("price", DSO.getEstElectricityPrice(time, day, month, year));
				} else if (operation.equals("getEstDSOPrice")) {
					int time = Integer.parseInt((String) parameters.get("time"));
					int day = Integer.parseInt((String) parameters.get("day"));
					int month = Integer.parseInt((String) parameters.get("month"));
					int year = Integer.parseInt((String) parameters.get("year"));
					resObj.addReturnVal("price", DSO.getEstDSOPrice(time, day, month, year));
				} else if (operation.equals("getEstESPPrice")) {
					int time = Integer.parseInt((String) parameters.get("time"));
					int day = Integer.parseInt((String) parameters.get("day"));
					int month = Integer.parseInt((String) parameters.get("month"));
					int year = Integer.parseInt((String) parameters.get("year"));
					resObj.addReturnVal("price", DSO.getEstESPPrice(time, day, month, year));
				} else if (operation.equals("getActElectricityPrice")) {
					int time = Integer.parseInt((String) parameters.get("time"));
					int day = Integer.parseInt((String) parameters.get("day"));
					int month = Integer.parseInt((String) parameters.get("month"));
					int year = Integer.parseInt((String) parameters.get("year"));
					resObj.addReturnVal("price", DSO.getActualElectricityPrice(time, day, month, year));
				} else if (operation.equals("getActESPPrice")) {
					int time = Integer.parseInt((String) parameters.get("time"));
					int day = Integer.parseInt((String) parameters.get("day"));
					int month = Integer.parseInt((String) parameters.get("month"));
					int year = Integer.parseInt((String) parameters.get("year"));
					resObj.addReturnVal("price", DSO.getActualESPPrice(time, day, month, year));
				} else if (operation.equals("getActDSOPrice")) {
					int time = Integer.parseInt((String) parameters.get("time"));
					int day = Integer.parseInt((String) parameters.get("day"));
					int month = Integer.parseInt((String) parameters.get("month"));
					int year = Integer.parseInt((String) parameters.get("year"));
					resObj.addReturnVal("price", DSO.getActualDSOPrice(time, day, month, year));
				} else if (operation.equals("getEstAvgElectricityPrice")) {
					resObj.addReturnVal("price", DSO.getEstAvgElectricityPriceForDay(model.runningDay, model.runningMonth, model.runningYear));
				} else if (operation.equals("getEstAvgESPPrice")) {
					resObj.addReturnVal("price", DSO.getEstAvgESPPriceForDay(model.runningDay, model.runningMonth, model.runningYear));
				} else if (operation.equals("getActAvgElectricityPrice")) {
					resObj.addReturnVal("price", DSO.getActAvgElectricityPriceForDay(model.runningDay));
				} else if (operation.equals("getEstESPPrices")) {
					resObj.addReturnVal("prices", DSO.getEstESPPricesForDay(model.runningDay, model.runningMonth, model.runningYear));
				} else if (operation.equals("getEstDSOPrices")) {
					resObj.addReturnVal("prices", DSO.getEstDSOPricesForDay(model.runningDay, model.runningMonth, model.runningYear));
				} else if (operation.equals("getEstElectricityPrices")) {
					resObj.addReturnVal("prices", DSO.getEstElectricityPrices(model.runningDay, model.runningMonth, model.runningYear));
				} else if (operation.equals("getActDSOPrices")) {
					resObj.addReturnVal("prices", DSO.getActDSOPricesForDay(model.runningDay, model.runningMonth, model.runningYear));
				} else if (operation.equals("getActESPPrices")) {
					resObj.addReturnVal("prices", DSO.getActESPPricesForDay(model.runningDay, model.runningMonth, model.runningYear));
				} else if (operation.equals("getLowestPrice")) {
					int targetTime = Integer.parseInt((String) parameters.get("targetHour"));
					int latestTime = Integer.parseInt((String) parameters.get("latestTime"));
					double min1Price = DSO.getActualElectricityPrice((targetTime-1)*3600, model.runningDay, model.runningMonth, model.runningYear);
					double targetPrice = DSO.getActualElectricityPrice(targetTime*3600, model.runningDay, model.runningMonth, model.runningYear);
					double priceTemp = min1Price;
					int timeTemp = targetTime -1;
					int newHour = timeTemp;

					double[] prices = new double[latestTime-targetTime+1];
					prices[0] = min1Price;
					prices[1] = targetPrice;
					for(int i = 2; i <= latestTime-targetTime; i++) {
						 prices[i] = DSO.getEstElectricityPrice((targetTime + i -1)*3600, model.runningDay, model.runningMonth, model.runningYear);
					}
					for(int j = 1; j < prices.length; j++) {
						if(prices[j] < priceTemp) {
							priceTemp = prices[j];
							newHour = timeTemp + j;
						}
					}
					resObj.addReturnVal("time",newHour);
					resObj.addReturnVal("price",priceTemp);
				}
			} else if (provider.equals("WEATHER")) { 
				if (operation.equals("getEstTemp")) {
					int hour = Integer.parseInt((String)parameters.get("time"));
					resObj.addReturnVal("temp",
							WeatherService.getForecastTempAtHour(hour));
				} else if (operation.equals("getActualTemp")) { 
					int hour = Integer.parseInt((String)parameters.get("time"));
					int day = Integer.parseInt((String)parameters.get("day"));
					int month = Integer.parseInt((String)parameters.get("month"));
					int year = Integer.parseInt((String)parameters.get("year"));
					resObj.addReturnVal("temp", WeatherService
							.getActualTemp(hour * 3600, day, month, year));
					
				} else if (operation.equals("getAvgTemp")) {
					resObj.addReturnVal("temp",
							WeatherService.getForecastedAvgTemp());
				}
			} else if (provider.equals("SIMENV")) {
				if (operation.equals("getCurrentRTemp")) {
					String room = (String) parameters.get("room");
					resObj.addReturnVal("temp",
							model.getEnvironment().getRoom(room)
									.getCurrentTemp());
				} else if (operation.equals("getPlannedTempAtTime")) {
					String room = (String) parameters.get("room");
					String hour = (String) parameters.get("time");
					resObj.addReturnVal("plannedTemp", model
							.getEnvironment().getRoom(room)
							.getPlannedSchedule().getHourlyPlan()[Integer.parseInt(hour)%24]);
				} else if (operation.equals("getScheduledTemps")) {
					String room = (String) parameters.get("room");
					double[] schedule = model.getEnvironment().getRoom(room)
							.getSchedule().getSchedule();
					resObj.addReturnVal("scheduledTemps", schedule);
				} else if (operation.equals("getScheduledTempAtHour")) {
					String room = (String) parameters.get("room");
					int hour = Integer
							.parseInt((String) parameters.get("hour"));

					hour = hour * 12;
					
					if (hour < 0)
						hour = 288 + hour;
					else
						hour = hour % 288;

					resObj.addReturnVal("scheduledTemp",
							model.getEnvironment().getRoom(room).getSchedule()
									.getSchedule()[hour]);
				} else if (operation.equals("getExtendedSchedule")) {
					String r = (String) parameters.get("room");
					Room room = model.getEnvironment().getRoom(r);
					int extendedHours = Integer.parseInt((String) parameters
							.get("extendedHours"));
					double[] exschedule = new double[288 + (2 * 12 * extendedHours)];
					double[] schedule = Arrays.copyOf(room.getSchedule().getSchedule(), 288);

					// if there's -9999 in the schedule, it means that heating is off
					// in which case the system has to calculate the expected temperature
					int day = model.runningDay;
					int month = model.runningMonth;
					int year = model.runningYear;
					
					int j = 0;
					for (int i = extendedHours; i > 0; i--) {
						double expectedTemp = schedule[288 - (extendedHours*12)];
						if(expectedTemp == -9999) {
							expectedTemp = schedule[276 - (extendedHours*12)] - EMUtility.estimatedTempDrop(WeatherService.getActualTemp((24 - i)*3600, day-1, month, year), 
									schedule[276 - (extendedHours*12)], room.getWidth(), room.getLength(), room.getHeight(), 3600);
							
							for(int y=0; y<12; y++)
								schedule[288 - (extendedHours*12) + y] = expectedTemp;
						}
						
						for(int x=0; x<12; x++)
							exschedule[j*12 + x] = expectedTemp;
						j++;
					}
					
					for (int i = 0; i < 24; i++) { 
						double expectedTemp = schedule[i*12];
						if(expectedTemp == -9999) {
							expectedTemp = schedule[i==0?23*12:(i-1)*12] - EMUtility.estimatedTempDrop(WeatherService.getActualTemp(i*3600, day, month, year), schedule[i==0?23*12:(i-1)*12], room.getWidth()
									, room.getLength(), room.getHeight(), 3600);
							
							for(int y=0; y<12; y++)
								schedule[i*12 + y] = expectedTemp;
						}
						
						for(int x=0; x<12; x++)
							exschedule[(i + extendedHours)*12 + x] = expectedTemp; 
					}

					for (int i = 24 + extendedHours; i < 2*extendedHours + 24; i++) {
						double expectedTemp = schedule[(i - extendedHours)*12 % 288]; 
						if(expectedTemp == -9999) {
							expectedTemp = schedule[(i==24+extendedHours?23*12:(i - extendedHours)*12) % 288 - 12] - EMUtility.estimatedTempDrop(
									WeatherService.getActualTemp((i - extendedHours)*12 % 288, day+1, month, year), schedule[(i==24+extendedHours?23*12:(i - extendedHours)*12) % 288 - 12], room.getWidth()
									, room.getLength(), room.getHeight(), 3600);
							
							for(int y=0; y<12; y++)
								schedule[(i - extendedHours)*12 % 288 + y] = expectedTemp;
						}
						
						for(int x=0; x<12; x++)
							exschedule[i*12 + x] = expectedTemp;
					}
					
					resObj.addReturnVal("extendedSchedule", exschedule);
				} else if (operation.equals("getEstDailyEnergyConsumption")) {
					double otemp = Double.parseDouble((String) parameters
							.get("otemp"));
					int comfort = Integer.parseInt((String) parameters
							.get("comfort"));

					resObj.addReturnVal("kwh", HouseholdStatistics
							.getEstEnergyConsumption(comfort, (int) otemp));
				} else if (operation.equals("estimatedEnergyToMaintain")) {
					int otemp = (int)Double.parseDouble((String) parameters.get("otemp")); 
					int temp = (int)Double.parseDouble((String) parameters.get("temp"));
					String entityID = (String) parameters.get("entity");
					
					resObj.addReturnVal("energy", EnergyUsageStatistics.getEstEnergyToMaintain(otemp, temp, entityID));
				} else if (operation.equals("estimateHighESPPriceCeiling")) {
					int day = Integer.parseInt((String) parameters.get("day"));
					int month = Integer.parseInt((String) parameters.get("month"));
					int year = Integer.parseInt((String) parameters.get("year"));
					
					// get estimated average ESP price
					double estAvg = DSO.getEstAvgESPPriceForDay(day, month, year);
					
					// get the estimated max price for that day
					double estMax = -10;
					for (int i=0; i<24; i++) {
						if(DSO.getEstESPPrice(i*3600, day, month, year) > estMax) {
							estMax = DSO.getEstESPPrice(i*3600, day, month, year);
						}
					}
					
					// estimate ceiling
					double temp = (estMax - estAvg)/3;
					
					resObj.addReturnVal("price", estMax - temp);
				} else if (operation.equals("estimateHighElectricityPriceCeiling")) {
					int day = Integer.parseInt((String) parameters.get("day"));
					int month = Integer.parseInt((String) parameters.get("month"));
					int year = Integer.parseInt((String) parameters.get("year"));
					
					// get estimated average Electricity price
					double estAvg = DSO.getEstAvgElectricityPriceForDay(day, month, year);
					
					// get the estimated max price for that day
					double estMax = -10;
					for (int i=0; i<24; i++) {
						if(DSO.getEstElectricityPrice(i*3600, day, month, year) > estMax) {
							estMax = DSO.getEstElectricityPrice(i*3600, day, month, year);
						}
					}
					
					// estimate ceiling
					double temp = (estMax - estAvg)/2;
					//System.out.println(estMax-temp);
					resObj.addReturnVal("price", estMax - temp);
				} else if (operation.equals("getPriceCeiling")) {
					int time = Integer.parseInt((String) parameters.get("time"));
					int day = model.runningDay;
					int month = model.runningMonth;
					int year = model.runningYear;
					double avg = 0;
					double estMax = -10;
					for(int i = 0; i <= time; i++) {
						avg += DSO.getActualElectricityPrice(i*3600, day, month, year);
						if(DSO.getActualElectricityPrice(i*3600, day, month, year) > estMax) {
							estMax = DSO.getActualElectricityPrice(i*3600, day, month, year);
						}
					}
					for(int j = time+1; j < 24;j++) {
						avg += DSO.getEstElectricityPrice(j*3600, day, month, year);
						if(DSO.getEstElectricityPrice(j*3600, day, month, year) > estMax) {
							estMax = DSO.getEstElectricityPrice(j*3600, day, month, year);
						}
					}
					avg = avg/24;
					double temp = (estMax - avg)/2;
					resObj.addReturnVal("priceCeiling", estMax - temp);
				} else if (operation.equals("getNewStartTime")) {
					double totalEnergy = Double.parseDouble((String) parameters.get("totalEnergy"));
					double sub1 = Double.parseDouble((String) parameters.get("subEnergy1"));
					double sub2 = Double.parseDouble((String) parameters.get("subEnergy2"));
					double preCost = Double.parseDouble((String) parameters.get("originalCost"));
					int latest = Integer.parseInt((String) parameters.get("latestTime"));
					double totalTime = Double.parseDouble((String) parameters.get("totalTime"));
					
					double left1 = totalEnergy - sub1;
					double left2 = totalEnergy - sub2;
					int hour = model.runningHour;
					hour %= 24;
					int day = model.runningDay;
					int month = model.runningMonth;
					int year = model.runningYear;
					
					int period = latest - hour;
					double min1Price = DSO.getActualElectricityPrice(hour*3600, day, month, year);
					double targetPrice = DSO.getActualElectricityPrice((hour+1)*3600, day, month, year);
					double[] priceArray = new double[period];
					priceArray[0] = min1Price; 
					priceArray[1] = targetPrice;
					for(int i = 2; i < period; i++) {
						priceArray[i] = DSO.getEstElectricityPrice((hour+i)*3600, day, month, year);
					}
					int newHour = hour + 1;
					double newTime = -10;
					double costTemp = preCost;
					String continuous = "none";
					double[] costArray1 = new double[period-1], costArray2 = new double[period - 1];
					for(int j = 0; j < period - 1; j++) {
						costArray1[j] = left1*priceArray[j] + sub1*priceArray[j+1];
						costArray2[j] = left2*priceArray[j] + sub2*priceArray[j+1];
						if(costArray1[j] < costTemp) {
							newHour = hour + j;
							newTime = 0.0;
							costTemp = costArray1[j];
							continuous = "true";
						}
						if(costArray2[j] < costTemp) {
							newHour = hour + j;
							newTime = 120 - totalTime;
							costTemp = costArray2[j];
							continuous = "true";
						}
					}
					
					double[] costArray3 = new double[period-2], costArray4 = new double[period - 2];
					for(int k = 0; k < period - 2; k++) {
						costArray3[k] = left1*priceArray[k] + sub1*priceArray[k+2];
						costArray4[k] = left2*priceArray[k] + sub2*priceArray[k+2];
						if(costArray3[k] < costTemp) {
							newHour = hour + k;
							newTime = 0.0;
							costTemp = costArray3[k];
							continuous = "false";
						}
						if(costArray4[k] < costTemp) {
							newHour = hour + k;
							newTime = 120 - totalTime;
							costTemp = costArray2[k];
							continuous = "false";
						}
					}
					
					resObj.addReturnVal("hour", newHour);
					resObj.addReturnVal("time", newTime);
					resObj.addReturnVal("cost", costTemp);
					resObj.addReturnVal("continuous", continuous);
					
				} else if (operation.equals("getRoomStatistics")) {
					String r = (String) parameters.get("room"); 
					Room room = model.getEnvironment().getRoom(r); 
					ArrayList<Device> list = room.getDevicesByType(2);
					resObj.addReturnVal("number",list.size());
					double lumen = 0; 
					for(int i = 0; i < list.size(); i++) {
						lumen += ((Light)list.get(i)).getDesignLumen();
					}
					resObj.addReturnVal("totalLumen", lumen);
					double[] plan = room.getLightPlan().getHourlyPlan();
					double[] schedule = Schedule.createHighResolutionSchedule(Arrays.copyOf(plan, plan.length));
					resObj.addReturnVal("lightSchedule",schedule);
				} else if(operation.equals("getDailySchedule")) {
					String r = (String) parameters.get("room");
					String w = (String) parameters.get("machine");
					String d = (String) parameters.get("dayOfWeek");
					int day = Integer.parseInt(d);
					Room room = model.getEnvironment().getRoom(r); 
					ArrayList<Device> list = room.getDevicesByType(3); 
					double[] plan = new double[7];
					String[] schedule = new String[7];
					Double[] times = new Double[]{};
					Double[] powers = new Double[]{};
					double[] timeChain = new double[]{};
					double[] powerChain = new double[]{};
					WashingMachine machine = null;
					for(Device dev:list){
						if(w.equals(dev.getName())) {
							machine = (WashingMachine)dev;
							WashingMachineModel wmModel = (WashingMachineModel)model.getEnvironment().getDeviceModelMap().get(dev);
							schedule = wmModel.getSchedule();
							plan = wmModel.getPlan();
							break;
						}
					}
					if(plan[day]!=0){
						
						times = machine.getProcessTimeStat().get(schedule[day]);
						powers = machine.getProcessWattStat().get(schedule[day]);
						timeChain = new double[times.length];
						powerChain = new double[powers.length];
						for(int i = 0; i < times.length; i ++) {
							timeChain[i] = times[i].doubleValue();
							powerChain[i] = powers[i].doubleValue();
						}
						//powerChain = new double[]{36.0,1500.0,80.0,80.0,90.0,140.0};
					}
					
					resObj.addReturnVal("schedule", schedule[day]);
					resObj.addReturnVal("plan", plan[day]);
					resObj.addReturnVal("timeChain", timeChain);
					resObj.addReturnVal("powerChain", powerChain);
					resObj.addReturnVal("processNum", times.length);
				} 
			}

			String response = map.writeValueAsString(resObj);

			ex.sendResponseHeaders(200, response.length());
			OutputStream os = ex.getResponseBody();
			os.write(response.getBytes());

			os.close();
		}

		private void setParent(SimulationModel model) {
			this.model = model;
		}
	}

}
