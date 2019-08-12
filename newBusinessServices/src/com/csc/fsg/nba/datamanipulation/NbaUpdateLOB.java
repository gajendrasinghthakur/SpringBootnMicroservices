package com.csc.fsg.nba.datamanipulation; //NBA201

/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group®.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaLob;

/**
 * NbaUpdateLOB stores information to an NbaLob object. A static initializer method 
 * generates a Map containing the variable names that may be used and the Method 
 * object used to access them. Map entries are present for all methods of the 
 * NbaLob class whose method name starts with the string "set".  The Method object 
 * for all entries in the Map is the updateLobValue() of NbaUpdateLOB.  This Map 
 * of variables is returned to the NbaOinkDataAccess when the NbaLob destination 
 * is initialized.  
 *
 * While constructing the variable map for NbaOinkDataAccess, a second map is 
 * constructed for use by NbaUpdateLOB.  This map contains the same variable names 
 * as the first Map, but the Method objects are the setXXX methods of the NbaLob object. 
 * When information is retrieved, the updateLobValue() method uses reflection to 
 * message the NbaLob with the methods from the second map.  This is to allow 
 * NbaUpdateLOB to obtain the values from the NbaOinkRequest.
 * <p>  
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA0021</td><td>Version 2</td><td>Object Interactive Name Keeper</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * </p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2 
 */
public class NbaUpdateLOB {
	protected com.csc.fsg.nba.vo.NbaLob nbaLob;
	static HashMap variables = new HashMap();
	static HashMap nbaLobMethods = new HashMap();
	private static NbaLogger logger = null;
	static {
		NbaUpdateLOB aNbaUpdateLOB = new NbaUpdateLOB();
		String thisClassName = aNbaUpdateLOB.getClass().getName();
		try {
			Class[] lobArgs = { Class.forName("com.csc.fsg.nba.datamanipulation.NbaOinkRequest")};
			Method lobMethod = aNbaUpdateLOB.getClass().getDeclaredMethod("updateLobValue", lobArgs);
			Object[] args = { thisClassName, lobMethod };
			NbaLob aNbaLob = new NbaLob();
			Method[] allMethods = aNbaLob.getClass().getDeclaredMethods();
			for (int i = 0; i < allMethods.length; i++) {
				Method aMethod = allMethods[i];
				String aMethodName = aMethod.getName();
				if (aMethodName.startsWith("set")) {
					String theVariable = aMethodName.substring(3) + "LOB";
					variables.put(theVariable.toUpperCase(), args);
					nbaLobMethods.put(theVariable.toUpperCase(), aMethod);
				}
			}
		} catch (Exception e) {
		}
	}
/**
 * NbaUpdateLOB constructor.
 */
public NbaUpdateLOB() {
	super();
}
/**
 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
 * @return the logger implementation
 */
protected static NbaLogger getLogger() {
	if (logger == null) {
		try {
			logger = NbaLogFactory.getLogger(NbaUpdateLOB.class.getName());
		} catch (Exception e) {
			NbaBootLogger.log("NbaUpdateLOB could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}
	}
	return logger;
}
/**
 * Get the value for nbaLob.
 * @return com.csc.fsg.nba.vo.NbaLob
 */
public com.csc.fsg.nba.vo.NbaLob getNbaLob() {
	return nbaLob;
}
/**
 * Answer the Map containing the getXXX methods for NbaLob
 * @return java.util.HashMap
 */
protected static java.util.HashMap getNbaLobMethods() {
	return nbaLobMethods;
}
/**
 * Answer a Map of the available variables. The keys to the map are the
 * variable names. The values are an array containing the class name string
 * and the Method to be invoked to retrieve the variable. The method is
 * always retrieveLobValue().
 * @return methods
 */
public static Map getVariables() {
	return variables;
}
/**
 * Store the reference to the NbaLob.
 * @param newNbaLob com.csc.fsg.nba.vo.NbaLob
 */
public void initializeObjects(NbaLob newLobSource) {
	setNbaLob(newLobSource);
}
/**
 * Set the value for nbaLob.
 * @param newNbaLob com.csc.fsg.nba.vo.NbaLob
 */
protected void setNbaLob(com.csc.fsg.nba.vo.NbaLob newNbaLob) {
	nbaLob = newNbaLob;
}
/**
 * Update the value for the variable identified in the NbaOinkRequest.
 * @param aNbaOinkRequest
 */
public void updateLobValue(NbaOinkRequest aNbaOinkRequest) {
    Object anObject = aNbaOinkRequest.getValue().get(0);
    if (anObject != null) {
        Method aMethod = (Method) nbaLobMethods.get(aNbaOinkRequest.getVariable().toUpperCase());
        Object[] args = new Object[1];
        args[0] = anObject;
        //Invoke the method on the class instance
        try {
            aMethod.invoke(getNbaLob(), args); // SPR3290
        } catch (Exception e) {
            getLogger().logError("Error invoking variable update routine:" + aNbaOinkRequest.getVariable());
            getLogger().logError("   Exception:" + e.toString());
        }
    }
}
}
