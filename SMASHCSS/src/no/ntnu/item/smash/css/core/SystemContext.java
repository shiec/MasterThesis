package no.ntnu.item.smash.css.core;

import no.ntnu.item.smash.css.comm.CSSEndPoint;
import no.ntnu.item.smash.css.em.EnergyManagement;

public class SystemContext {

	private BSCMachine bsc;
	private MONMachine mon;
	private PGCMachine pgc;
	private SDCMachine sdc;
	private CSSEndPoint css;
	
	public SystemContext() {
		initMachines();
		css = new CSSEndPoint(this);
	}
	
	private void initMachines() {
		bsc = new BSCMachine(this);
		mon = new MONMachine(this);
		pgc = new PGCMachine(this);
		sdc = new SDCMachine(this);
	}
	
	public BSCMachine getBsc() {
		return bsc;
	}

	public MONMachine getMon() {
		return mon;
	}

	public PGCMachine getPgc() {
		return pgc;
	}

	public SDCMachine getSdc() {
		return sdc;
	}

	public CSSEndPoint getCss() {
		return css;
	}

	public void setCss(CSSEndPoint css) {
		this.css = css;
	}
	
	public void setThermalProperties(double[] thermalProp) {
		EnergyManagement.thermalProp = thermalProp;
	}
	
}
