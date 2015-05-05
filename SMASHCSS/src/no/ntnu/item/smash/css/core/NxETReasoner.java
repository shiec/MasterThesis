package no.ntnu.item.smash.css.core;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import net.sf.xet.nxet.data.InMemoryObjects;
import net.sf.xet.nxet.executor.Executor;
import no.ntnu.item.smash.css.comm.TimeSynchronizer;
import no.ntnu.item.smash.css.parser.XMLParser;
import no.ntnu.item.smash.css.role.Reasoner;
import no.ntnu.item.smash.css.structure.ActionSet;
import no.ntnu.item.smash.css.structure.ComfortMeasure;
import no.ntnu.item.smash.css.structure.EWHSchedulingAction;
import no.ntnu.item.smash.css.structure.Entity;
import no.ntnu.item.smash.css.structure.LightSchedulingAction;
import no.ntnu.item.smash.css.structure.RoomSchedulingAction;
import no.ntnu.item.smash.css.structure.ULCRejectionAction;
import no.ntnu.item.smash.css.structure.WashingSchedulingAction;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class NxETReasoner implements Reasoner {

	public static double totalCost = 0;
	
	private Executor executor;

	public NxETReasoner() {

	}

	@Override
	public synchronized ActionSet processPolicy(int policyID, String policyFilePath, HashMap<String,Object> data) {
		File policyFile = new File(policyFilePath);

		// get NxET query file
		String queryFile = policyFile.getParent() + "\\q_"
				+ policyFile.getName();

		// get policy (NxET rule) file
		String ruleFile = policyFilePath;

		// get NxET datasource file
		String dataSourceFile = getDataFilePath(policyFilePath);
		ConsoleHandler handler = new ConsoleHandler();
		
		handler.setLevel(Level.OFF);
		executor = new Executor(queryFile, ruleFile, dataSourceFile, null,
				handler, "C:/Users/shuna/workspace/NxET");
		executor.setInputs(data);
		executor.execute(); 
		//System.out.println(executor.answers());
		
		InMemoryObjects.XETMEMORYOBJECT.clear();

//		if(executor.cleanAnswers().length>1) {
//			//System.out.println("Action set processing type 1: Full permutation. Best action set guaranteed.");
//			return chooseActionSet(executor.cleanAnswers(), (String)data.get("goal"));
//		}
//		else {
//			//System.out.println("Action set processing type 2: Limited permutation. Best action set not guaranteed.");
//			return chooseActionSet2(executor.cleanAnswers(), (String)data.get("goal"));
//		}
		
		return constructActionSet(executor.cleanAnswers()[0]);
	}
	
	@SuppressWarnings("unchecked")
	private ActionSet chooseActionSet2(String[] sets, String goal) {
		//System.out.println("Processing action set combinations.");
		
		ActionSet set = new ActionSet();
		
		String costAttr = "smash:cost_cost";
		if(goal!=null && goal.equals("cost")) costAttr = "smash:cost_cost";
		else if(goal!=null && goal.equals("comfort")) costAttr = "smash:cost_comfort";
		
		HashMap<String, HashMap<String,Integer>[]> tempMap = new HashMap<String, HashMap<String,Integer>[]>();
		ArrayList<String> keyList = new ArrayList<String>();
		ArrayList<Double> costList = new ArrayList<Double>();
		
		Document doc = XMLParser.getDOMFromXMLString(sets[0]);
		
		int noSet = Integer.parseInt(((Element)doc.getFirstChild()).getAttribute("smash:setNumber"));
		double totalCost = 0;
		
		NodeList actionSetList = doc.getElementsByTagName("smash:ActionSet");
		for (int i=0; i<actionSetList.getLength(); i++) {
			Element actionSet = (Element)actionSetList.item(i);
			int setNumber = Integer.parseInt(actionSet.getAttribute("smash:set"));
			String key1 = actionSet.getAttribute("smash:key1");
			String key2 = actionSet.getAttribute("smash:key2");
			
			HashMap<String,Integer>[] tempArray = new HashMap[noSet];
			if(tempMap.containsKey(key1)) tempArray = tempMap.get(key1);
			else {
				tempMap.put(key1, tempArray);
				for(int k=0; k<noSet; k++) 
					tempArray[k] = new HashMap<String,Integer>();
			}			
			
			tempArray[setNumber-1].put(key2, new Integer(i));
			if(!keyList.contains(key2)) keyList.add(key2);
			
			NodeList actions = actionSet.getElementsByTagName("smash:Action");
			double setCost = 0;
			for(int j=0; j<actions.getLength(); j++) {
				setCost += Double.parseDouble(((Element)((Element)actions.item(j)).getElementsByTagName("smash:cost").item(0)).getAttribute(costAttr));
			}
			
			costList.add(setCost);
		}
		
		//System.out.println("Number of possible action set combinations (all entities): " + Math.pow(noSet, keyList.size()*tempMap.size()) + " ( power(" + noSet + "," + (keyList.size()*tempMap.size()) + ") )");
		//System.out.println("Number of possible action set combinations (per 1 entity): " + Math.pow(noSet, keyList.size()));
		//System.out.println("Selected action set below:");
		
		Set<String> keys = tempMap.keySet();
		for(String key:keys) { // for each entity
			ArrayList<Integer> scheduledTimes = new ArrayList<Integer>();
			
			for(int s=0; s<keyList.size(); s++) {
				double minCost = 9999;
				int bestAction = 0;
				
				for(int j=0; j<noSet; j++) {
					if(!tempMap.get(key)[j].containsKey(keyList.get(s))) continue;
					
					int action = tempMap.get(key)[j].get(keyList.get(s));
					if(checkDuplicateScheduling(actionSetList.item(action), scheduledTimes)==true) continue;
					
					if(costList.get(action)<minCost) {
						minCost = costList.get(action);
						bestAction = action;
					} 
				}
				
				if(minCost!=9999) {
					totalCost += minCost;
					
					set = constructAndAppendActionSet(actionSetList.item(bestAction), set, scheduledTimes);
					
					DOMImplementationLS domImplLS = (DOMImplementationLS) doc
					    .getImplementation();
					LSSerializer serializer = domImplLS.createLSSerializer();
					serializer.getDomConfig().setParameter("xml-declaration", false);
					//System.out.println(serializer.writeToString(actionSetList.item(bestAction)));
				}
			}
		}
		
		//System.out.println("Final cost: " + totalCost);
		
		if(totalCost>0) {
			//System.out.println("Best action generates cost. No rescheduling done.");
			return new ActionSet();
		}
		
		return set;
	}
	
	private ActionSet chooseActionSet(String[] sets, String goal) {
		ActionSet set = new ActionSet();
		
		String costAttr = "smash:cost_cost";
		if(goal.equals("cost")) costAttr = "smash:cost_cost";
		else if(goal.equals("comfort")) costAttr = "smash:cost_comfort";
		
		if (sets.length > 1) {
			int selected = 0;
			double minCost = 0;
			
			for(int i=0; i<sets.length; i++) {				
				double cost = 0;
				
				String answer = sets[i];
				
				Document doc = XMLParser.getDOMFromXMLString(answer);
				NodeList actionList = doc.getElementsByTagName("smash:Action");
				for (int j = 0; j < actionList.getLength(); j++) {
					Element actionNode = (Element) actionList.item(j);
					cost += Double.parseDouble(((Element)actionNode.getElementsByTagName("smash:cost").item(0)).getAttribute(costAttr));
				}
				
				if(cost<minCost || i==0) {
					minCost = cost;
					selected = i;
				}
			}
			
			//System.out.println("More than one action set. Goal is " + goal + ". Selected action set: #" + (selected+1) + ". Final cost: " + minCost);
			return constructActionSet(sets[selected]);
		} else if (sets.length > 0) {
			return constructActionSet(sets[0]);
		}
		
		return set;
	}

	private boolean checkDuplicateScheduling(Node actionSetNode, ArrayList<Integer> scheduled) {
		NodeList actionList = ((Element)actionSetNode).getElementsByTagName("smash:Action");
		for (int i = 0; i < actionList.getLength(); i++) {
			Element actionNode = (Element) actionList.item(i);
			String actionType = actionNode.getAttribute("smash:type");
			if (actionType.equals("RoomSchedulingAction")) {
				Calendar cal = Calendar.getInstance();
				DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
				Date startTime = null;
				try {
					startTime = df.parse(actionNode
							.getElementsByTagName("smash:start").item(0)
							.getTextContent());
				} catch (DOMException e) {
					e.printStackTrace();
					continue;
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
				cal.setTime(startTime);
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				int hours = Integer.parseInt(actionNode
						.getElementsByTagName("smash:duration")
						.item(0).getTextContent())/60;
				for(int j=hour; j<hour+hours; j++) {
					if(scheduled.contains(new Integer(j))) return true;
				}
			}
		}
		
		return false;
	}
	
	private ActionSet constructAndAppendActionSet(Node actionSetNode, ActionSet set, ArrayList<Integer> scheduledTimes) {
		NodeList actionList = ((Element)actionSetNode).getElementsByTagName("smash:Action");
		for (int i = 0; i < actionList.getLength(); i++) {
			Element actionNode = (Element) actionList.item(i);
			String actionType = actionNode.getAttribute("smash:type");
			if (actionType.equals("RoomSchedulingAction")) {
				DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
				Date startTime = null;
				try {
					startTime = df.parse(actionNode
							.getElementsByTagName("smash:start").item(0)
							.getTextContent());
				} catch (DOMException e) {
					e.printStackTrace();
					continue;
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}

				RoomSchedulingAction ra = new RoomSchedulingAction();
				ra.setComfortMeasure(new ComfortMeasure());
				ra.setRoom(new Entity(actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent(), actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent()));
				ra.setSchedulingTime(startTime);
				ra.setDuration(Integer.parseInt(actionNode
						.getElementsByTagName("smash:duration")
						.item(0).getTextContent()));
				ra.setTargetValue(Math.round(Double.parseDouble(actionNode
						.getElementsByTagName("smash:newTemp").item(0)
						.getTextContent())));
				set.add(ra);
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(startTime);
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				int hours = Integer.parseInt(actionNode
						.getElementsByTagName("smash:duration")
						.item(0).getTextContent())/60;
				for(int j=hour; j<hour+hours; j++) {
					scheduledTimes.add(new Integer(j));
				}
				
				if((Integer)ra.getTargetValue()==-9999) {
					if(hour-1>=0) scheduledTimes.add(new Integer(hour-1));
					if(hour+1<=23) scheduledTimes.add(new Integer(hour+1));
				}
				
			} else if (actionType.equals("ULCRejectionAction")) {
				ULCRejectionAction ua = new ULCRejectionAction();
				ua.setId(actionNode.getElementsByTagName("smash:id").item(0).getTextContent());
				ua.setPenalty(Double.parseDouble(actionNode.getElementsByTagName("smash:penalty").item(0).getTextContent()));
				set.add(ua);
			}
			
		}
		
		return set;
	}
	
	private ActionSet constructActionSet(String answer) {
		ActionSet actions = new ActionSet();

		double costCost = 0, comfortCost = 0;
		
		Document doc = XMLParser.getDOMFromXMLString(answer);
		NodeList actionList = doc.getElementsByTagName("smash:Action");
		for (int i = 0; i < actionList.getLength(); i++) {
			Element actionNode = (Element) actionList.item(i);
			String actionType = actionNode.getAttribute("smash:type");
			if (actionType.equals("RoomSchedulingAction")) {
				DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
				Date startTime = null;
				try {
					startTime = df.parse(actionNode
							.getElementsByTagName("smash:start").item(0)
							.getTextContent());
				} catch (DOMException e) {
					e.printStackTrace();
					continue;
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}

				RoomSchedulingAction ra = new RoomSchedulingAction();
				ra.setComfortMeasure(new ComfortMeasure());
				ra.setRoom(new Entity(actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent(), actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent()));
				ra.setSchedulingTime(startTime);
				ra.setDuration((int)Math.round(Double.parseDouble(actionNode
						.getElementsByTagName("smash:duration")
						.item(0).getTextContent())));
				ra.setTargetValue((int)Math.round(Double.parseDouble(actionNode
						.getElementsByTagName("smash:newTemp").item(0)
						.getTextContent())));
				actions.add(ra);
			} else if (actionType.equals("ULCRejectionAction")) {
				ULCRejectionAction ua = new ULCRejectionAction();
				ua.setId(actionNode.getElementsByTagName("smash:id").item(0).getTextContent());
				ua.setPenalty(Double.parseDouble(actionNode.getElementsByTagName("smash:penalty").item(0).getTextContent()));
				actions.add(ua);
			} else if (actionType.equals("EWHSchedulingAction")) {
				DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
				Date startTime = null;
				try {
					startTime = df.parse(actionNode
							.getElementsByTagName("smash:start").item(0)
							.getTextContent());
				} catch (DOMException e) {
					e.printStackTrace();
					continue;
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
				
				EWHSchedulingAction ewha = new EWHSchedulingAction();
				ewha.setRoom(new Entity(actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent(), actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent()));
				ewha.setSchedulingTime(startTime);
				ewha.setDuration(Integer.parseInt(actionNode
						.getElementsByTagName("smash:duration")
						.item(0).getTextContent()));
				ewha.setTargetValue((int)Math.round(Double.parseDouble(actionNode
						.getElementsByTagName("smash:newTemp").item(0)
						.getTextContent())));
				actions.add(ewha);
			} else if(actionType.equals("LightSchedulingAction")) {
				DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
				Date startTime = null;
				try {
					startTime = df.parse(actionNode
							.getElementsByTagName("smash:start").item(0)
							.getTextContent());
				} catch (DOMException e) {
					e.printStackTrace();
					continue;
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}

				LightSchedulingAction ra = new LightSchedulingAction();
				ra.setComfortMeasure(new ComfortMeasure());
				ra.setRoom(new Entity(actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent(), actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent()));
				ra.setSchedulingTime(startTime);
				ra.setLightName(actionNode
						.getElementsByTagName("smash:Light").item(0)
						.getTextContent());
				ra.setDuration((int)Math.round(Double.parseDouble(actionNode
						.getElementsByTagName("smash:duration")
						.item(0).getTextContent())));
				ra.setTargetValue((int)Math.round(Double.parseDouble(actionNode
						.getElementsByTagName("smash:newStatus").item(0)
						.getTextContent())));
				actions.add(ra);
			} else if(actionType.equals("WashingSchedulingAction")) {
				DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
				Date startTime = null;
				try {
					startTime = df.parse(actionNode
							.getElementsByTagName("smash:start").item(0)
							.getTextContent());
				} catch (DOMException e) {
					e.printStackTrace();
					continue;
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}

				WashingSchedulingAction ra = new WashingSchedulingAction();
				ra.setComfortMeasure(new ComfortMeasure());
				ra.setRoom(new Entity(actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent(), actionNode
						.getElementsByTagName("smash:Entity").item(0)
						.getTextContent()));
				ra.setSchedulingTime(startTime);
				ra.setMachineName(actionNode
						.getElementsByTagName("smash:Machine").item(0)
						.getTextContent());
				ra.setDuration((int)Math.round(Double.parseDouble(actionNode
						.getElementsByTagName("smash:duration")
						.item(0).getTextContent())));
				ra.setTargetValue((int)Math.round(Double.parseDouble(actionNode
						.getElementsByTagName("smash:newStatus").item(0)
						.getTextContent())));
				actions.add(ra);
			}
			
			costCost += Double.parseDouble(((Element)actionNode.getElementsByTagName("smash:cost").item(0)).getAttribute("smash:cost_cost"));
			comfortCost += Double.parseDouble(((Element)actionNode.getElementsByTagName("smash:cost").item(0)).getAttribute("smash:cost_comfort"));
		}

		totalCost += Math.round(costCost*100.00)/100.00;
		
		//System.out.println("Estimated \"cost\" cost = " + Math.round(costCost*100.00)/100.00 + " / \"comfort\" cost = " + Math.round(comfortCost*100.00)/100.00);

		return actions;
	}

	/*
	 * Parse the rule file (with added data source file path) to obtain the
	 * datasource file path
	 */
	private String getDataFilePath(String policyFilePath) {
		File policyFile = new File(policyFilePath);
		Document doc = XMLParser.getDOMFromFile(policyFilePath);

		if (doc != null)
			return policyFile.getParentFile()
					+ "\\"
					+ doc.getElementsByTagName("xet:Data").item(0)
							.getTextContent();

		return null;
	}
}
