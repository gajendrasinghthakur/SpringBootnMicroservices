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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaDst;

/**
 *  NbaRetrieveDstVO retrieves information from an NbaDst object. A static initializer 
 *  method initializes a Map containing the variable names that may be used, and the 
 *  Method object used to access them. Map entries are present for all methods of the 
 *  NbaDst class whose return type is either boolean or java.lang.String.  The Method object 
 *  for all entries in the Map is the retrieveValueFromDst() of NbaRetrieveDstVO.  This 
 *  Map of variables is returned to the NbaOinkDataAccess when the NbaDst source is 
 *  initialized.
 *
 *  While constructing the variable map for NbaOinkDataAccess, a second map is 
 *  constructed for use by NbaRetrieveDstVO.  This map contains the same variable 
 *  names as the first Map, but the Method objects are the getXXX or isXXX methods 
 *  (all methods that return type boolean or java.lang.String) of the NbaDst object. When 
 *  information is retrieved, the retrieveValueFromDst() method uses reflection to message the NbaDst
 *  with the methods from the second map. This is to allow NbaRetrieveDstVO to store the results 
 *  in the NbaOinkRequest.
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
public class NbaRetrieveDstVO {
	protected com.csc.fsg.nba.vo.NbaDst nbaDst;
	static HashMap variables = new HashMap();
	static HashMap nbaDstMethods = new HashMap();
	private static NbaLogger logger = null;

	static {
		NbaRetrieveDstVO aNbaRetrieveDstVO = new NbaRetrieveDstVO();
		String thisClassName = aNbaRetrieveDstVO.getClass().getName();

		try {
			ArrayList allowedTypes = new ArrayList();
			//these are the allowed types for now
			allowedTypes.add("java.lang.String");
			allowedTypes.add("boolean");
			Class[] lobArgs = { Class.forName("com.csc.fsg.nba.datamanipulation.NbaOinkRequest")};

			Method lobMethod = aNbaRetrieveDstVO.getClass().getDeclaredMethod("retrieveValueFromDst", lobArgs);
			Object[] args = { thisClassName, lobMethod };
			NbaDst aNbaDst = new NbaDst();
			Method[] allMethods = aNbaDst.getClass().getDeclaredMethods();

			for (int i = 0; i < allMethods.length; i++) {
				Method aMethod = allMethods[i];
				String aMethodName = aMethod.getName();
				String returnType = aMethod.getReturnType().getName();
				if (allowedTypes.contains(returnType) && aMethod.getParameterTypes().length == 0) {
					String theVariable = (aMethodName.startsWith("get") ? aMethodName.substring(3) : aMethodName) + "DST";
					variables.put(theVariable.toUpperCase(), args);
					nbaDstMethods.put(theVariable.toUpperCase(), aMethod);
				}
			}
		} catch (Exception e) {
		}
	}
/**
 * NbaRetrieveDstVO constructor comment.
 */
public NbaRetrieveDstVO() {
	super();
}
/**
 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
 * @return the logger implementation
 */
protected static NbaLogger getLogger() {
	if (logger == null) {
		try {
			logger = NbaLogFactory.getLogger(NbaRetrieveDstVO.class.getName());
		} catch (Exception e) {
			NbaBootLogger.log("NbaRetrieveDstVO could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}
	}
	return logger;
}
/**
 * Get the value for nbaLob.
 * @return com.csc.fsg.nba.vo.NbaLob
 */
protected com.csc.fsg.nba.vo.NbaDst getNbaDst() {
	return nbaDst;
}
/**
 * Answer the Map containing the getXXX or isXXX methods for NbaDst
 * @return java.util.HashMap
 */
protected static java.util.HashMap getNbaDstMethods() {
	return nbaDstMethods;
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
public void initializeObjects(NbaDst newDstSource) {
	setNbaLob(newDstSource);
}
/**
 * Obtain the value for the variable identified in the NbaOinkRequest.
 */
public void retrieveValueFromDst(NbaOinkRequest aNbaOinkRequest) {
	aNbaOinkRequest.setValue(new Vector());
	Method aMethod = (Method) nbaDstMethods.get(aNbaOinkRequest.getVariable().toUpperCase());
	String returnType = aMethod.getReturnType().getName();
	Object[] args = new Object[0];
	//Invoke the method on the class instance
	try {
		Object retValue = aMethod.invoke(getNbaDst(), args); // SPR3290
		if (returnType.equals("boolean")) {
			aNbaOinkRequest.addValue(((Boolean) retValue).booleanValue());
		} else if (returnType.equals("java.lang.String")) {
			aNbaOinkRequest.addValue((String) retValue);
		}
	} catch (Exception e) {
		getLogger().logError("Error invoking variable resolution routine:" + aNbaOinkRequest.getVariable());
		getLogger().logError("   Exception:" + e.toString());
	}
}
/**
 * Set the value for nbaDst.
 * @param newNbaLob com.csc.fsg.nba.vo.NbaDst
 */
protected void setNbaLob(com.csc.fsg.nba.vo.NbaDst newNbaDst) {
	nbaDst = newNbaDst;
}
}
