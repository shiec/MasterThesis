/*
 * Native XML Equivalent Transformation Software Development Kit (NxET)
 * Copyright (C) 2004-2005, Telematics Architecture for Play-based Adaptable System,
 * (TAPAS), Department of Telematics, 
 * Norwegian University of Science and Technology (NTNU),
 * O.S.Bragstads Plass 2, N7491, Trondheim, Norway
 *
 * This file is a part of NxET.
 *
 * NxET is a free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * NxET is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 */
package no.ntnu.item.smash.css.nxet.builtin;

import java.io.PrintStream;

import net.sf.xet.nxet.builtin.Builtin;
import net.sf.xet.nxet.builtin.ExecutionResult;
import net.sf.xet.nxet.builtin.NotExecutableException;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.Specialization;
import net.sf.xet.nxet.core.World;
import no.ntnu.item.smash.css.comm.TimeSynchronizer;

public class CalculateEnergy extends Builtin {

    public final String ATTR_TIME = "time";
    public final String ATTR_CHAIN_TIME = "timeChain";
    public final String ATTR_CHAIN_POWER = "powerChain";
    public final String ATTR_RESULT = "result";
    
    /**
     * A construct to create a builtin object from a builtin atom
     * and an output redirector.
     * 
     * @param builtinAtom The builtin atom
     * @param world The world
     * @param place The place the built-in is executed
     * @param printStream The output redirector
     */
    public CalculateEnergy(Node builtinAtom, World world, Integer place, PrintStream out) {
        super(builtinAtom, world, place, out);
        this.setBuiltinType(Builtin.BT_B);
    }

    /**
     * A construct to create a builtin object from a builtin atom
     * 
     * @param builtinAtom The builtin atom
     * @param world The world that this built-in was being executed.
     * @param place The place the built-in is executed
     */
    public CalculateEnergy(Node builtinAtom, World world, Integer place) {
        super(builtinAtom, world, place);
        this.setBuiltinType(Builtin.BT_B);
    }

    /**
     * Check if the structure of the builtin atom matches
     * this builtin function. A built-in function usually
     * requires some parameters. This function checks if
     * those required parameters exist in the built-in atom
     * or not<br>
     * <br>
     * This built-in function requires one parameter:
     * - parameter result<br>
     * 
     * @return Returns true if matched
     * @see net.sf.xet.nxet.builtin.Builtin#isMatched()
     */
    public Boolean isMatched() {
        
        return new Boolean(builtinAtom.hasAttributeByLocalName(ATTR_TIME) &&builtinAtom.hasAttributeByLocalName(ATTR_CHAIN_TIME)
         		&& builtinAtom.hasAttributeByLocalName(ATTR_CHAIN_POWER) && builtinAtom.hasAttributeByLocalName(ATTR_RESULT));
    
    }

    /**
     * After the structure of the built-in has been verified,
     * The built-in manager verifies also if the built-in
     * can be executed. A built-in can be executed when 
     * it does not have neccessary variable left uninstantiated.
     * In some built-in the number of child nodes are also
     * checked.<br>
     * <br>
     * The following parameter must be left
     * uninstantiated.<br>
     * - parameter result<br>
     * 
     * @return Returns true if the built-in is executable
     * @see net.sf.xet.nxet.builtin.Builtin#isExecutable()
     */
    public Boolean isExecutable() {
        return new Boolean(builtinAtom.attributeByLocalName(ATTR_RESULT).isVariable());
    }

    /**
     * Execute the builtin function. There might be some
     * variables instantiated during the execution. 
     * These variables will be stored in a generated 
     * specialization object, which will be included 
     * in the returned ExecutionResult object.<br>
     * <br>
     * The result value is replaced with the current time
     * 
     * @return Returns an execution result object.
     */
    public ExecutionResult execute() throws NotExecutableException {
        Specialization spec = new Specialization();
        String time = builtinAtom.attributeByLocalName(ATTR_TIME).getNodeValue();
        String timeChain = builtinAtom.attributeByLocalName(ATTR_CHAIN_TIME).getNodeValue();
        String powerChain = builtinAtom.attributeByLocalName(ATTR_CHAIN_POWER).getNodeValue();
        String resultVariable = builtinAtom.attributeByLocalName(ATTR_RESULT).getNodeValue();
        
        timeChain = timeChain.substring(1, timeChain.length()-1);
        powerChain = powerChain.substring(1, powerChain.length()-1);
        
        String[] timeArray = timeChain.split(", ");
        String[] powerArray = powerChain.split(", ");
        
        
        double period = Math.round(Double.parseDouble(time));
        //System.out.println("$$$$$$$$$$$$$$$$"+period);
        int processCount = timeArray.length;
        double sum = 0.0;
        for(int i = processCount-1; i >= 0; i--) {
        	double subTime = Double.parseDouble(timeArray[i]);
        	double subPower = Double.parseDouble(powerArray[i]);
        	if(period >= subTime) {
        		sum += subPower*subTime;
        		period -= subTime;
        	} else {
        		sum += period*subPower;
        		break;
        	}
        }
        sum /= 60*1000;
        spec.addSpecialization(resultVariable, ""+ sum);
        ExecutionResult execResult = new ExecutionResult(true, spec);
        execResult.setResultDescription(" [" + resultVariable + " <- " + 
										sum + " = CalculateEnergy()]");
	    return execResult;
    }

}
