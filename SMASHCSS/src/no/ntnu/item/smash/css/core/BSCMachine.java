package no.ntnu.item.smash.css.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.ntnu.item.smash.css.comm.SchedulingRequestObject;
import no.ntnu.item.smash.css.role.Subscriber;
import no.ntnu.item.smash.css.structure.ActionSet;
import no.ntnu.item.smash.css.structure.DecidedAction;
import no.ntnu.item.smash.css.structure.Trigger;

public class BSCMachine extends Observable implements Subscriber, Observer {

	private NxETReasoner rm;
	private SystemContext context;
	
	public BSCMachine(SystemContext context) {
		this.context = context;
		rm = new NxETReasoner();
	}
	
	protected enum State {
		IDLE {
			void transition(BSCMachine machine, Event event, HashMap<String,Object> data) {
				switch (event) {
				case DEACTIVATE:
					machine.currentState = State.SUSPENDED;
					break;
				case STARTPROCESSING:
					machine.currentState = State.PROCESSPOLICY;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(BSCMachine machine, HashMap<String,Object> data) {
			}
		},
		PROCESSPOLICY {
			void transition(BSCMachine machine, Event event, HashMap<String,Object> data) {
				switch (event) {
				case WAIT:
					machine.currentState = State.IDLE;
					break;
				case DEACTIVATE:
					machine.currentState = State.SUSPENDED;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(BSCMachine machine, HashMap<String,Object> data) {
				if(!machine.processingQueue.isEmpty()) {
					int policyToProcess = machine.processingQueue.remove(0);
					String policyFilePath = machine.context.getPgc().getIDFileMap().get(policyToProcess);
					
					ActionSet actions = machine.rm.processPolicy(policyToProcess, policyFilePath, machine.dataQueue.remove(0));
					ArrayList<DecidedAction> actionList = actions.getActions();
					for(DecidedAction action:actionList) {
						action.execute();
					}
				}
				
				machine.update(machine, null);
				//machine.currentState.transition(machine, Event.WAIT, null);
			}
		},
		SUSPENDED {
			void transition(BSCMachine machine, Event event, HashMap<String,Object> data) {
				switch (event) {
				case ACTIVATE:
					machine.currentState = State.IDLE;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(BSCMachine machine, HashMap<String,Object> data) {
			}
		};

		abstract void transition(BSCMachine machine, Event event, HashMap<String,Object> data);

		abstract void doAction(BSCMachine machine, HashMap<String,Object> data);
	}

	protected enum Event {
		DEACTIVATE, ACTIVATE, STARTPROCESSING, WAIT
	}

	private State initialState = State.IDLE;
	private State currentState = initialState;
	
	private ArrayList<Integer> processingQueue = new ArrayList<Integer>();
	private ArrayList<HashMap<String,Object>> dataQueue = new ArrayList<HashMap<String,Object>>();
	
	private int dataReceived = 0;
	
	public State getState() {
		return currentState;
	}

	@Override
	public void subscribe(int dataType) {
				
	}

	/*
	 * Receive the subscribed data
	 */
	@Override
	public synchronized void getNotified(HashMap<String,Object> data, int dataType) { 
		if(data!=null) {
			//System.out.println("BSC receives data: " + data.get("mdata-"+dataType));
			
			// check policies with the required data type
			HashMap<Integer, Trigger> idTriggerMap = context.getPgc().getIDTriggerMap();
			Set<Integer> ids = idTriggerMap.keySet();
			
			for(int id:ids) {
				Trigger trigger = idTriggerMap.get(id);
				if(dataType==trigger.getActiveTriggerType() && trigger.conditionFulfilled(data.get("mdata-"+dataType))) {
					data.put("goal", context.getPgc().getIDGoalMap().get(id));
					// put the policy in the processing queue
					processingQueue.add(id);
					dataQueue.add(data);
				}
			}
		}
		
		dataReceived++;
		if(currentState!=State.PROCESSPOLICY) currentState.transition(this, Event.STARTPROCESSING, null);
	}

	@Override
	public void update(Observable o, Object arg) {
		if(processingQueue.isEmpty()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if(dataReceived == context.getMon().getSubscriptionTypes().size()) {				
				try {
					URL url = new URL("http://localhost:8080/done");
					HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					connection.setDoOutput(true);
					connection.setReadTimeout(1);
					connection.getOutputStream().write(1);
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
				
				dataReceived = 0;
			}
			
			this.currentState.transition(this, Event.WAIT, null);
		} else {
			this.currentState.doAction(this, null);
		}
	}

}
