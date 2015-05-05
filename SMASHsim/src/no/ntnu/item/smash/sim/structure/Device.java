package no.ntnu.item.smash.sim.structure;


public abstract class Device extends Entity {

	public static final int DEVTYPE_HEATER = 0;
	public static final int DEVTYPE_EWATERHEATER = 1;
	//lighting system
	public static final int DEVTYPE_LIGHT = 2;
	//washing machine
	public static final int DEVTYPE_WASHINGMACHINE = 3;
	
	public static final int DEVENERGYTYPE_CONSTANT = 0;
	public static final int DEVENERGYTYPE_PROGRAM = 1;
	public static final int DEVENERGYTYPE_VARIABLE = 2;
	
	protected int id;
	protected int type;
	protected int energyConsumptionType;
	protected double watt;	
	protected String name;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getEnergyConsumptionType() {
		return energyConsumptionType;
	}

	public void setEnergyConsumptionType(int energyConsumptionType) {
		this.energyConsumptionType = energyConsumptionType;
	}

	public double getWatt() {
		return watt;
	}

	public void setWatt(double watt) {
		this.watt = watt;
	}
	
}
