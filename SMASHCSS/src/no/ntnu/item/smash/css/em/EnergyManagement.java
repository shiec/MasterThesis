package no.ntnu.item.smash.css.em;

import java.util.Arrays;
import java.util.HashMap;

public class EnergyManagement {

	public static double[] thermalProp = BuildingThermalIntegrity.NORWAY_HEAVYINSULATED;

	public static double functionAdaptor(String name,
			HashMap<String, Object> params) {
		if (name.equals("estimatedHeatTime")) {
			return estimatedHeatTime(parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("deltaT")),
					parseParam(params.get("roomW")),
					parseParam(params.get("roomL")),
					parseParam(params.get("roomH")),
					parseParam(params.get("watt")));
		} else if (name.equals("estimatedEnergy")) {
			return estimatedEnergy(parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("deltaT")),
					parseParam(params.get("roomW")),
					parseParam(params.get("roomL")),
					parseParam(params.get("roomH")),
					parseParam(params.get("watt")));
		} else if (name.equals("estimatedTempDrop")) {
			return estimatedTempDrop(parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("roomW")),
					parseParam(params.get("roomL")),
					parseParam(params.get("roomH")),
					parseParam(params.get("duration")));
		} else if (name.equals("estimatedTempDropTime")) {
			return estimatedTempDropTime(parseParam(params.get("otemp")),
					parseParam(params.get("rtemp")),
					parseParam(params.get("deltaT")),
					parseParam(params.get("roomW")),
					parseParam(params.get("roomL")),
					parseParam(params.get("roomH")));
		} else {
			return 0;
		}
	}

	private static double parseParam(Object param) {
		String toParse = (String) param;
		return Double.parseDouble(toParse);
	}

	public static double estimatedHeatTime(double otemp, double rtemp,
			double deltaT, double roomW, double roomL, double roomH, double watt) {
		// estimate areas of walls, ceiling, and floor
		double areaEWall = (roomW * roomH) + (roomL * roomH);
		double areaIWall = areaEWall;
		double areaCeiling = roomW * roomL;
		double areaFloor = areaCeiling;

		// assume 2 door with area 2m2 and 2 windows with area 2m2
		double uTotal = 2
				* thermalProp[BuildingThermalIntegrity.U_WINDOW_INDEX]
				+ (2 / thermalProp[BuildingThermalIntegrity.R_DOOR_INDEX])
				+ (areaEWall / thermalProp[BuildingThermalIntegrity.R_WALLS_INDEX])
				+ (areaCeiling / thermalProp[BuildingThermalIntegrity.R_CEILING_INDEX])
				+ (areaFloor / thermalProp[BuildingThermalIntegrity.R_FLOORS_INDEX]);
		double transferFromMass = -(8.3 * (areaEWall + areaIWall + areaCeiling) * (Arrays
				.equals(thermalProp,
						BuildingThermalIntegrity.NORWAY_HEAVYINSULATED) ? estimateDeltaT_wall_air(
				otemp, 0) : estimateDeltaT_wall_air(otemp, 1))); // 1.31
		double airMass = 4 * 1005.4 * 1.225 * roomW * roomL * roomH;

		double temp = -(((rtemp + deltaT) * uTotal) - 20 - transferFromMass
				- 20 - watt - (uTotal * otemp))
				/ (20 + transferFromMass + 20 + watt - (rtemp * uTotal) + (uTotal * otemp));

		double estTime = 0;
		if (temp > 0)
			estTime = (-Math.log(temp) * airMass) / uTotal;
		else
			estTime = 99999;
		// System.out.println("(-Math.log(-((("+rtemp+" + "+deltaT+") *"+
		// uTotal+") -"
		// +20+" - "+transferFromMass+" -"+20+" - "+watt+" - ("+uTotal+" * "+otemp+")) /"
		// +
		// "("+20+" + "+transferFromMass+" + "+20+" + "+watt+" - ("+rtemp+" * "+uTotal+") + ("+uTotal+" * "+otemp+"))) * "+airMass+") / "+uTotal);
		//System.out.println("estimatedHeatTime " + rtemp + " " + deltaT + " "
		//+ (estTime*3));

		return estTime*3;
	}

	public static double estimatedEnergy(double otemp, double rtemp,
			double deltaT, double roomW, double roomL, double roomH, double watt) {
		double heatTime = estimatedHeatTime(otemp, rtemp, deltaT, roomW, roomL,
				roomH, watt);

		if (heatTime == 99999)
			return 99999;

		return watt * 0.001 * heatTime / 3600; // kWh
	}

	public static double estimatedTempDrop(double otemp, double rtemp,
			double roomW, double roomL, double roomH, double duration) {
		// estimate areas of walls, ceiling, and floor
		double areaEWall = (roomW * roomH) + (roomL * roomH);
		double areaIWall = areaEWall;
		double areaCeiling = roomW * roomL;
		double areaFloor = areaCeiling;

		// assume 1 door with area 2m2 and 2 windows with area 2m2
		double uTotal = 2
				* thermalProp[BuildingThermalIntegrity.U_WINDOW_INDEX]
				+ (2 / thermalProp[BuildingThermalIntegrity.R_DOOR_INDEX])
				+ (areaEWall / thermalProp[BuildingThermalIntegrity.R_WALLS_INDEX])
				+ (areaCeiling / thermalProp[BuildingThermalIntegrity.R_CEILING_INDEX])
				+ (areaFloor / thermalProp[BuildingThermalIntegrity.R_FLOORS_INDEX]);
		double transferFromMass = (8.3 * (areaEWall + areaIWall + areaCeiling) * (Arrays
				.equals(thermalProp,
						BuildingThermalIntegrity.NORWAY_HEAVYINSULATED) ? estimateDeltaT_wall_air(
				otemp, 0) : estimateDeltaT_wall_air(otemp, 1))); // TODO: have
																	// the
																	// consumer
																	// specify
																	// the
																	// number of
																	// windows
		double airMass = 3 * 1005.4 * 1.225 * roomW * roomL * roomH;

		// (q_int + q_mass + q_solar + U*ot - exp(-(U*t)/C)*(q_int + q_mass +
		// q_solar - Tinit*U + U*ot))/U
		double newTemp = (20 + transferFromMass + 20 + (uTotal * otemp) - (Math
				.exp(-(uTotal * duration) / airMass) * (20 + transferFromMass
				+ 20 - (rtemp * uTotal) + (uTotal * otemp))))
				/ uTotal;

		//System.out.println("estimatedTempDrop " + (rtemp - newTemp)
		//		+ " / otemp " + otemp);

		return rtemp - newTemp; // degree celsius
	}

	public static double estimatedTempDropTime(double otemp, double rtemp,
			double deltaT, double roomW, double roomL, double roomH) {
		rtemp = Math.round(rtemp * 100.0) / 100.0;

		// estimate areas of walls, ceiling, and floor
		double areaEWall = (roomW * roomH) + (roomL * roomH);
		double areaIWall = areaEWall;
		double areaCeiling = roomW * roomL;
		double areaFloor = areaCeiling;

		// assume 1 door with area 2m2 and 2 windows with area 2m2
		double uTotal = Math
				.round((2
						* thermalProp[BuildingThermalIntegrity.U_WINDOW_INDEX]
						+ (2 / thermalProp[BuildingThermalIntegrity.R_DOOR_INDEX])
						+ (areaEWall / thermalProp[BuildingThermalIntegrity.R_WALLS_INDEX])
						+ (areaCeiling / thermalProp[BuildingThermalIntegrity.R_CEILING_INDEX]) + (areaFloor / thermalProp[BuildingThermalIntegrity.R_FLOORS_INDEX])) * 100.0) / 100.0;
		double transferFromMass = (8.3 * (areaEWall + areaIWall + areaCeiling) * (Arrays
				.equals(thermalProp,
						BuildingThermalIntegrity.NORWAY_HEAVYINSULATED) ? estimateDeltaT_wall_air(
				otemp, 0) : estimateDeltaT_wall_air(otemp, 1)));
		double airMass = 3 * 1005.4 * 1.225 * roomW * roomL * roomH;
		// System.out.println(otemp + " " + rtemp + " " + deltaT + " " + roomW +
		// " " + roomL + " " + roomH + " " + uTotal + " " + airMass);

		// (-ln(-((22.9729*U)-q_int-q_mass-q_solar-(U*ot))/(q_int+q_mass+q_solar-(25*U)+(U*ot)))*C)/U
		double estTime = (-Math
				.log(-(((rtemp - deltaT) * uTotal) - 20 - transferFromMass - 20 - (uTotal * otemp))
						/ (20 + transferFromMass + 20 - (rtemp * uTotal) + (uTotal * otemp))) * airMass)
				/ uTotal;
		// System.out.println("estimatedTempDropTime " +
		// (estTime<=0?0:Math.round(estTime*100.0)/100.0));
		return estTime <= 0 ? 0 : Math.round(estTime * 100.0) / 100.0; // second
	}

	private static double estimateDeltaT_wall_air(double otemp,
			int insulationLevel) {
		double start = 0.31;
		double multiply = 0.015;
		double multiply2 = 0.009;
		if (insulationLevel == 1) {
			start = 0.3;
			multiply = 0.02;
		}

		double startOTemp = 2;

		double diffOTemp = startOTemp - otemp;
		double toAdd = (otemp<-14?multiply2:multiply) * diffOTemp;

		return start + toAdd;
	}
}
