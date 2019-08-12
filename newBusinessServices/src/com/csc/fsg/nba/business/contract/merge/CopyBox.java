package com.csc.fsg.nba.business.contract.merge;

/**
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 * 
 */

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.business.transaction.NbaContractChangeUtils;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaContractVO;

/** 
 * 
 * This class provides common functions for copying the value of the fields from one NbaContractVO object to another NbaContractVO object.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.04</td><td>Axa Life Phase 1</td><td>Paid Changes</td></tr>
 * <tr><td>P2AXAL016CV</td><td>Axa Life Phase 2</td><td>Product Val - Life 70 Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class CopyBox {

	private static NbaLogger logger;
	private List skipAttributes = new ArrayList();
	private List overrideAttributes = new ArrayList();
	private Map metaData = new HashMap();
	private static final String GET_METHOD_PREFIX = "get";
	private static final String SET_METHOD_PREFIX = "set";
	private static final String HAS_METHOD_PREFIX = "has";
	private static final String DELETE_METHOD_PREFIX = "delete";//P2AXAL016CV

	public CopyBox() {
		skipAttributes.add("id");
	}
	
	/**
	 * @return Returns the metadata.
	 */
	public Map getMetaData() {
		return metaData;
	}
	
	/**
	 * @param metadata The metadata to set.
	 */
	public void setMetaData(Map metaData) {
		this.metaData = metaData;
	}

	/**Default implementation for apecific processing
	 * @param copyTo
	 * @param copyFrom
	 */
	public void doSpecificProcessing(NbaContractVO copyTo, NbaContractVO copyFrom) {
		//TODO Default Implementation
	}
	
	/**Copy the fields value from copyTo to copyFrom object
	 * @param copyTo
	 * @param copyFrom
	 * @param metaData
	 * @throws NbaBaseException
	 */
	public void copy(NbaContractVO copyTo, NbaContractVO copyFrom, Map metaData) throws NbaBaseException {
		setMetaData(metaData);
		copy(copyTo, copyFrom);
	}

	/**Copy the fields value from copyTo to copyFrom object
	 * @param copyTo
	 * @param copyFrom
	 * @throws NbaBaseException
	 */
	public void copy(NbaContractVO copyTo, NbaContractVO copyFrom) throws NbaBaseException {
		if(copyTo != null && copyFrom != null) {
			Field[] fields = copyTo.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (!NbaUtils.isBlankOrNull(fields[i]) && !isStatic(fields[i]) && !isTransient(fields[i]) && isBasicType(fields[i].getType())
						&& isValidName(fields[i].getName()) && !skipAttributes.contains(fields[i].getName())) {
					try {
						copyField(copyTo, copyFrom, fields[i].getName(), overrideAttributes.contains(fields[i].getName()));//P2AXAL016CV
					} catch (NoSuchMethodException e) {
						throw new NbaBaseException(e);
					}
				}
			}
			copyTo.setActionUpdate(); //P2AXAL016CV
			doSpecificProcessing(copyTo, copyFrom);
		}
	}
	
	/**Copies the value of a field fieldName from copyTo to copyFrom
	 * @param copyTo
	 * @param copyFrom
	 * @param fieldName
	 * @throws NoSuchMethodException
	 * @throws NbaBaseException
	 */
	//P2AXAL016CV Changed method signature
	public static void copyField(NbaContractVO copyTo, NbaContractVO copyFrom, String fieldName, boolean override) throws NoSuchMethodException, NbaBaseException {
		Method getterMethod = copyTo.getClass().getMethod(createMethodName(fieldName, GET_METHOD_PREFIX), null);
		Method hasMethod = copyTo.getClass().getMethod(createMethodName(fieldName, HAS_METHOD_PREFIX), null);
		Method setterMethod = copyTo.getClass().getMethod(createMethodName(fieldName, SET_METHOD_PREFIX),
				new Class[] { getterMethod.getReturnType() });
		Method deleteMethod = copyTo.getClass().getMethod(createMethodName(fieldName, DELETE_METHOD_PREFIX), null);//P2AXAL016CV
		if (((Boolean) (invokeMethod(hasMethod, copyFrom, null))).booleanValue()) {
			Object getterValue = invokeMethod(getterMethod, copyFrom, null);
			if ((getterValue != null) && setterMethod != null) {
				invokeMethod(setterMethod, copyTo, new Object[] { getterValue });
			}
		}else if(override){
			invokeMethod(deleteMethod, copyTo, null);//P2AXAL016CV
		}
	}
		
	/**
	 * Creates the method name from fieldName and methodPrefix
	 * @param class
	 * @return true if class is of basic type
	 */
	public static String createMethodName(String fieldName, String methodPrefix) {
		if(fieldName != null) {
			return (methodPrefix + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)); 
		}
		return null;
	}	

	/**invlokes a method on the obj Object and returns the result of the method as Object
	 * @param method
	 * @param obj
	 * @param args
	 * @return
	 * @throws NbaBaseException
	 */
	public static Object invokeMethod(Method method, Object obj, Object args[]) throws NbaBaseException {
		try {
			return method.invoke(obj, args);
		} catch (IllegalAccessException illegalAccessException) {
			getLogger().logException(
					"Illegal access attempted on object Method :[" + method.getName() + "] object: [" + obj.getClass().getName() + "]",
					illegalAccessException);
			throw new NbaBaseException(illegalAccessException);
		} catch (InvocationTargetException invocTargetExcep) {
			getLogger().logException("InvocationTargetException Method :[" + method.getName() + "] object: [" + obj.getClass().getName() + "]",
					invocTargetExcep);
			throw new NbaBaseException(invocTargetExcep);
		} catch (IllegalArgumentException illegalArgException) {
			getLogger().logException("IllegalArgumentException Method :[" + method.getName() + "] object: [" + obj.getClass().getName() + "]",
					illegalArgException);
			throw new NbaBaseException(illegalArgException);
		}
	}
	
	/**
	 * Determine if a class is of basic type. i.e either primitive type, String or Date.  
	 * @param class
	 * @return true if class is of basic type 
	 */
	public static boolean isBasicType(Class className) {
		if(className.isPrimitive() 
			|| className.getName().equalsIgnoreCase("java.util.Date") 
			|| className.getName().equalsIgnoreCase("java.lang.String")) {
			return true;
		}
		return false;
	}
	
	/**Determine if a field has static modifier
	 * @param field
	 * @return true if the field has a static modifier
	 */
	public static boolean isStatic(Field field) {
		if(field != null && Modifier.isStatic(field.getModifiers())) {
			return true;
		}
		return false;
	}
	
	/**Determine if a field has Transient modifier
	 * @param field
	 * @return true if the field has a transient modifier
	 */
	public static boolean isTransient(Field field) {
		if(field != null && Modifier.isTransient(field.getModifiers())) {
			return true;
		}
		return false;
	}
	
	/**Check the validity of the field name
	 * @param fieldName
	 * @return true if the field name is valid
	 */
	public static boolean isValidName(String fieldName) {
		if(fieldName != null && !fieldName.startsWith("has_") && !fieldName.startsWith("node_")) {
			return true;
		}
		return false;
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
	public static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractChangeUtils.class);
			} catch (Exception e) {
				NbaBootLogger.log("CopyBox could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * @return Returns the skipAttributes.
	 */
	//P2AXAL016CV
	public List getSkipAttributes() {
		return skipAttributes;
	}
	
	/**
	 * @return Returns the overrideAttributes.
	 */
	public List getOverrideAttributes() {
		return overrideAttributes;
	}
}
