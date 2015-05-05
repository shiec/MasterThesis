package no.ntnu.item.smash.css.core;

import java.util.ArrayList;
import java.util.HashMap;

import no.ntnu.item.smash.css.role.Subscriber;
import no.ntnu.item.smash.css.structure.SubscriptionList;


/*
 * Monitoring and diagnostic EFSM
 */
public class MONMachine {

	public static final int SUBSCRIBE_DATA_ESP_PRICE = 0;
	public static final int SUBSCRIBE_DATA_DSO_PRICE = 1;
	public static final int SUBSCRIBE_DATA_PRICE = 2;
	public static final int SUBSCRIBE_DATA_TIME = 3;
	public static final int SUBSCRIBE_DATA_EXTERNALEVENT = 4;
	
	protected enum State {
		IDLE {
			void transition(MONMachine machine, Event event, Object data) {
				switch (event) {
				case SUBSCRIBE:
					machine.currentState = State.PROCESSINGSUBSCRIPTION;
					break;
				case NOTIFY:
					machine.currentState = State.NOTIFYING;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(MONMachine machine, Object data) {
			}
		},
		PROCESSINGSUBSCRIPTION {
			void transition(MONMachine machine, Event event, Object data) {
				switch (event) {
				case WAIT:
					machine.currentState = State.IDLE;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(MONMachine machine, Object data) {
				ArrayList<Object> info = (ArrayList<Object>)data;
				int dataType = (Integer)info.get(1);
				
				machine.subscriptionList.addSubscription((Subscriber)info.get(0), dataType);
				if(!machine.subscriptionTypes.contains(dataType)) machine.subscriptionTypes.add(dataType);
				
				switch(dataType) {
				case SUBSCRIBE_DATA_ESP_PRICE:
				case SUBSCRIBE_DATA_DSO_PRICE:
				case SUBSCRIBE_DATA_PRICE:
					machine.priceMonitor.getCurrentState().transition(machine.priceMonitor, MonitoringThread.Event.MONITOR, null);
					break;
				case SUBSCRIBE_DATA_TIME:
					machine.timeMonitor.getCurrentState().transition(machine.timeMonitor, MonitoringThread.Event.MONITOR, null);
					break;
				case SUBSCRIBE_DATA_EXTERNALEVENT:
					machine.externalEventMonitor.getCurrentState().transition(machine.externalEventMonitor, MonitoringThread.Event.MONITOR, null);
					break;
				default:
					break;
				}
				machine.currentState.transition(machine, Event.WAIT, null);
			}
		},
		NOTIFYING {
			void transition(MONMachine machine, Event event, Object data) {
				switch (event) {
				case WAIT:
					machine.currentState = State.IDLE;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}
			
			void doAction(MONMachine machine, Object data) {
				ArrayList<Object> info = (ArrayList<Object>)data;
				HashMap<String,Object> returnData = (HashMap<String,Object>)info.get(0);
				int[] returnDataTypes = (int[])info.get(1);
				
				for(int i=0; i<returnDataTypes.length; i++) {
					ArrayList<Subscriber> subscribers = machine.subscriptionList.getSubscribers(returnDataTypes[i]);
					
					for(Subscriber subscriber:subscribers) {
						subscriber.getNotified(returnData, returnDataTypes[i]);
					}
				}	
				
				machine.currentState.transition(machine, Event.WAIT, null);
			}
		};
	
		abstract void transition(MONMachine machine, Event event, Object data);

		abstract void doAction(MONMachine machine, Object data);
	}
	
	protected enum Event {
		WAIT, SUBSCRIBE, NOTIFY;
	}
	
	private SystemContext context;
	
	private State initialState = State.IDLE;
	private State currentState = initialState;
	
	private SubscriptionList subscriptionList;
	private ArrayList<Integer> subscriptionTypes;
	
	private PriceMonitoringThread priceMonitor;
	private TimeMonitoringThread timeMonitor;
	private ExternalEventMonitoringThread externalEventMonitor;
	
	public MONMachine(SystemContext context) {
		this.context = context;
		subscriptionList = new SubscriptionList();
		subscriptionTypes = new ArrayList<Integer>();
		priceMonitor = new PriceMonitoringThread(this);
		timeMonitor = new TimeMonitoringThread(this);
		externalEventMonitor = new ExternalEventMonitoringThread(this);
	}
	
	public void subscribe(Subscriber subscriber, int dataType) {
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(subscriber);
		data.add(dataType);
		
		currentState.transition(this, Event.SUBSCRIBE, data);
	}
	
	public void reportData(HashMap<String,Object> returnData, int[] dataTypes) { 
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(returnData);
		data.add(dataTypes);
		
		currentState.transition(this, Event.NOTIFY, data);
	}
	
	public PriceMonitoringThread getPriceMonitor() {
		return priceMonitor;
	}
	
	public TimeMonitoringThread getTimeMonitor() {
		return timeMonitor;
	}
	
	public ExternalEventMonitoringThread getExternalEventMonitor() {
		return externalEventMonitor;
	}
	
	public ArrayList<Integer> getSubscriptionTypes() {
		return subscriptionTypes;
	}
}
