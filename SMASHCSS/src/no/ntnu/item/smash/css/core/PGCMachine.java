package no.ntnu.item.smash.css.core;

import java.util.ArrayList;
import java.util.HashMap;

import no.ntnu.item.smash.css.structure.Trigger;

/*
 * Policy/Goal/Constraint EFSM
 * 
 */
public class PGCMachine {

	protected enum State {
		IDLE {
			void transition(PGCMachine machine, Event event, Object data) {
				switch (event) {
				case DEACTIVATE:
					machine.currentState = State.DEACTIVATING;
					break;
				case ACTIVATE:
					machine.currentState = State.ACTIVATING;
					break;
				case REGISTER:
					machine.currentState = State.REGISTERING;
					break;
				case ASSIGNTRIGGER:
					machine.currentState = State.ASSIGNINGTRIGGER;
					break;
				case UPDATE:
					machine.currentState = State.UPDATING;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(PGCMachine machine, Object data) {
			}
		},
		REGISTERING {
			void transition(PGCMachine machine, Event event, Object data) {
				switch (event) {
				case WAIT:
					machine.currentState = State.IDLE;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(PGCMachine machine, Object data) {
				// the data for REGISTERING event contains 1) a file path
				String filePath = (String)data;
				machine.idFileMap.put(machine.nextID, filePath);
				machine.opSuccess = true;
				machine.currentState.transition(machine, Event.WAIT, data);
			}
		},
		DEACTIVATING {
			void transition(PGCMachine machine, Event event, Object data) {
				switch (event) {
				case WAIT:
					machine.currentState = State.IDLE;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(PGCMachine machine, Object data) {
				Integer id = (Integer)data;
				if(!machine.idFileMap.containsKey(id)) {
					machine.opSuccess = false; // invalid ID - the supplied policy does not exist in the system
				} else {			
					if(!machine.inactivePolicies.contains(id)) {
						machine.inactivePolicies.add(id);
					}
					
					machine.activePolicies.remove(id);
					machine.opSuccess = true;
				}
				machine.currentState.transition(machine, Event.WAIT, data);
			}
		},
		ACTIVATING {
			void transition(PGCMachine machine, Event event, Object data) {
				switch (event) {
				case WAIT:
					machine.currentState = State.IDLE;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(PGCMachine machine, Object data) {
				Integer id = (Integer)data;
				if(!machine.idFileMap.containsKey(id)) { 
					machine.opSuccess = false; // invalid ID - the supplied policy does not exist in the system
				} else {			
					if(!machine.activePolicies.contains(id)) {
						machine.activePolicies.add(id);
					}
					
					machine.inactivePolicies.remove(id);
					machine.opSuccess = true;
				}
				
				// the policy to be activated needs to have been assigned a trigger
				int activeTriggerType = machine.idTriggerMap.get(id).getActiveTriggerType();
				
				int dataType = Trigger.TYPE_DEF;
				switch(activeTriggerType) {
				case Trigger.TYPE_PRICE_ESP:
					dataType = MONMachine.SUBSCRIBE_DATA_ESP_PRICE;
					break;
				case Trigger.TYPE_PRICE_DSO:
					dataType = MONMachine.SUBSCRIBE_DATA_DSO_PRICE;
					break;
				case Trigger.TYPE_PRICE:
					dataType = MONMachine.SUBSCRIBE_DATA_PRICE;
					break;
				case Trigger.TYPE_TIME:
					dataType = MONMachine.SUBSCRIBE_DATA_TIME;
					break;
				case Trigger.TYPE_EXTERNAL_EVENT:
					dataType = MONMachine.SUBSCRIBE_DATA_EXTERNALEVENT;
					break;
				default:
					// TODO - error then
					break;
				}
				System.out.println("Subscribes to: " + dataType + " (0=esp prices,1=dso prices,2=prices,3=time,4=external events)");
				machine.context.getMon().subscribe(machine.context.getBsc(), dataType);
				machine.currentState.transition(machine, Event.WAIT, data);
			}
		},
		ASSIGNINGTRIGGER {
			void transition(PGCMachine machine, Event event, Object data) {
				switch (event) {
				case WAIT:
					machine.currentState = State.IDLE;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(PGCMachine machine, Object data) {
				ArrayList<Object> d = (ArrayList<Object>)data;
				machine.idTriggerMap.put((Integer)d.get(0), (Trigger)d.get(1));
				machine.opSuccess = true;
				machine.currentState.transition(machine, Event.WAIT, data);
			}
		},
		UPDATING {
			void transition(PGCMachine machine, Event event, Object data) {
			}

			void doAction(PGCMachine machine, Object data) {
			}
		};

		abstract void transition(PGCMachine machine, Event event, Object data);

		abstract void doAction(PGCMachine machine, Object data);
	}
	
	protected enum Event {
		DEACTIVATE, ACTIVATE, REGISTER, ASSIGNTRIGGER, UPDATE, WAIT
	}
	
	private SystemContext context;
	
	private State initialState = State.IDLE;
	private State currentState = initialState;
	
	private HashMap<Integer, String> idFileMap = new HashMap<Integer, String>();
	private HashMap<Integer, Trigger> idTriggerMap = new HashMap<Integer, Trigger>();
	private HashMap<Integer, String> idGoalMap = new HashMap<Integer, String>();
	private ArrayList<Integer> activePolicies = new ArrayList<Integer>();
	private ArrayList<Integer> inactivePolicies = new ArrayList<Integer>();
	private int nextID = 1;
	private boolean opSuccess = true;
	
	public PGCMachine(SystemContext context) {
		this.context = context;
	}
	
	public int registerPolicy(String filePath, String goal) {
		this.currentState.transition(this, Event.REGISTER, filePath);
		idGoalMap.put(nextID, goal);
		
		return nextID++;
	}
	
	public boolean activatePolicy(int id) {
		this.currentState.transition(this, Event.ACTIVATE, id);
		
		return opSuccess;
	}
	
	public boolean deactivatePolicy(int id) {
		this.currentState.transition(this, Event.DEACTIVATE, id);
		
		return opSuccess;
	}
	
	public boolean registerPolicyTrigger(int id, Trigger trigger) {
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(id);
		data.add(trigger);
		this.currentState.transition(this, Event.ASSIGNTRIGGER, data);
		
		return opSuccess;
	}
	
	/* TODO: All these will be removed once the proper datastores have been implemented */
	public HashMap<Integer, String> getIDFileMap() {
		return idFileMap;
	}
	
	public HashMap<Integer, Trigger> getIDTriggerMap() {
		return idTriggerMap;
	}
	
	public ArrayList<Integer> getActivePolicies() {
		return activePolicies;
	}
	
	public HashMap<Integer, String> getIDGoalMap() {
		return idGoalMap;
	}
	
}
