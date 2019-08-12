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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaLob;

/**
 *  NbaRetrieveLOB retrieves information from an NbaLob object. A static initializer 
 *  method generates a Map containing the variable names that may be used and the 
 *  Method object used to access them. Map entries are present for all methods of the 
 *  NbaLob class whose method name starts with the string "get".  The Method object 
 *  for all entries in the Map is the retrieveLobValue() of  NbaRetrieveLOB.  This 
 *  Map of variables is returned to the NbaOinkDataAccess when the NbaLob source is 
 *  initialized.
 *
 *  While constructing the variable map for NbaOinkDataAccess, a second map is 
 *  constructed for use by NbaRetrieveLOB.  This map contains the same variable 
 *  names as the first Map, but the Method objects are the getXXX methods of the 
 *  NbaLob object. When information is retrieved, the retrieveLobValue() method 
 *  uses reflection to message the NbaLob with the methods from the second map. 
 *  This is to allow NbaRetrieveLOB to store the results in the NbaOinkRequest.
 * <p>  
 * <b>Collaborators:</b><br>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA0021</td><td>Version 2</td><td>Object Interactive Name Keeper</td></tr>
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application Project</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * </p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 * @see com.csc.fsg.nba.datamanipulation.NbaOinkRequest
 * @since New Business Accelerator - Version 2 
 */
public class NbaRetrieveLOB {
	protected com.csc.fsg.nba.vo.NbaLob nbaLob;
	static HashMap variables = new HashMap();
	static HashMap nbaLobMethods = new HashMap();
	private static NbaLogger logger = null;
	static {
		NbaRetrieveLOB aNbaRetrieveLOB = new NbaRetrieveLOB();
		String thisClassName = aNbaRetrieveLOB.getClass().getName();
		try {
			Class[] lobArgs = { Class.forName("com.csc.fsg.nba.datamanipulation.NbaOinkRequest")};
			Method lobMethod = aNbaRetrieveLOB.getClass().getDeclaredMethod("retrieveLobValue", lobArgs);
			Object[] args = { thisClassName, lobMethod };
			NbaLob aNbaLob = new NbaLob();
			Method[] allMethods = aNbaLob.getClass().getDeclaredMethods();
			for (int i = 0; i < allMethods.length; i++) {
				Method aMethod = allMethods[i];
				String aMethodName = aMethod.getName();
				if (aMethodName.startsWith("get")) {
					String theVariable = aMethodName.substring(3) + "LOB";
					variables.put(theVariable.toUpperCase(), args);
					nbaLobMethods.put(theVariable.toUpperCase(), aMethod);
				}
			}
		} catch (Exception e) {
		}
	}
/**
 * NbaRetrieveLOB constructor comment.
 */
public NbaRetrieveLOB() {
	super();
}
/**
 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
 * @return the logger implementation
 */
protected static NbaLogger getLogger() {
	if (logger == null) {
		try {
			logger = NbaLogFactory.getLogger(NbaRetrieveLOB.class.getName());
		} catch (Exception e) {
			NbaBootLogger.log("NbaRetrieveLOB could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}
	}
	return logger;
}
/**
 * Get the value for nbaLob.
 * @return com.csc.fsg.nba.vo.NbaLob
 */
protected com.csc.fsg.nba.vo.NbaLob getNbaLob() {
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
 * This method initializes objects.
 */
public void initializeObjects(NbaLob newLobSource) {
	setNbaLob(newLobSource);
}
/**
 * Obtain the value for the variable identified in the NbaOinkRequest.
 */
public void retrieveLobValue(NbaOinkRequest aNbaOinkRequest) {
	aNbaOinkRequest.setValue(new Vector());
	Method aMethod = (Method) nbaLobMethods.get(aNbaOinkRequest.getVariable().toUpperCase());
	String returnType = aMethod.getReturnType().getName();
	Object[] args = new Object[0];
	//Invoke the method on the class instance
	try {
		Object retValue = aMethod.invoke(getNbaLob(), args); // SPR3290
		if (returnType.equals("boolean")) {
			aNbaOinkRequest.addValue(((Boolean) retValue).booleanValue());
		} else if (returnType.equals("int")) {
			aNbaOinkRequest.addValue(((Integer) retValue).intValue());
		} else if (returnType.equals("double")) {
			aNbaOinkRequest.addValue(((Double) retValue).doubleValue());
		} else if (returnType.equals("java.lang.String")) {
			aNbaOinkRequest.addValue((String) retValue);
		} else if (returnType.equals("java.util.Date")) {
			aNbaOinkRequest.addValue((Date) retValue);
		} else if (returnType.equals("long")) { //NBA187
			aNbaOinkRequest.addValue(((Long) retValue).longValue()); //NBA187
		}
	} catch (Exception e) {
		getLogger().logError("Error invoking variable resolution routine:" + aNbaOinkRequest.getVariable());
		getLogger().logError("   Exception:" + e.toString());
	}
}
/**
 * Set the value for nbaLob.
 * @param newNbaLob com.csc.fsg.nba.vo.NbaLob
 */
protected void setNbaLob(com.csc.fsg.nba.vo.NbaLob newNbaLob) {
	nbaLob = newNbaLob;
}
}
