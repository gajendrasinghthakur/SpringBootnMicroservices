package com.csc.fsg.nba.utility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;

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
 * *******************************************************************************<BR>
 */

/**
 * This class implements reflection utility functions for nbA.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA297</td><td>Version 1201</td><td>Suitability</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */

public class NbaReflectionUtilsImpl implements NbaReflectionUtils{
	
	public Object getValue(Object dataObject, String objName,Class[] parameterTypes, Object[] args ) throws Exception {
		String temp = Character.toUpperCase(objName.charAt(0)) + objName.substring(1);
		String prop = "get" + temp;		
		Method method = getMethod(dataObject, prop, parameterTypes);
		return method.invoke(dataObject, args);
	}	
	
	private Method getMethod(Object dataObject, String objName, Class[] parameterTypes) throws Exception {
		Class cls = dataObject.getClass();
		Method method = null;
		while (true) {
			try {
				if (cls != null)
					method = cls.getDeclaredMethod(objName, parameterTypes);
				break;
			} catch (NoSuchMethodException ex) {
				cls = cls.getSuperclass();
			}
		}
		if(method == null)
			throw new NbaBaseException("Field or attribute is not member of this object");
		method.setAccessible(true);
		return method;
	}
	
	public boolean isValue(Object dataObject, String objName,Class[] parameterTypes, Object[] args ) throws Exception {
		String temp = Character.toUpperCase(objName.charAt(0)) + objName.substring(1);
		String prop = "is" + temp;			
		Method method = getMethod(dataObject, prop, parameterTypes);
		return method != null ? ((Boolean)method.invoke(dataObject, args)).booleanValue() : false;
	}
	
	public Object getValue(Object dataObject, String attribute) throws Exception {
		return getValue( dataObject, attribute, null, null);
	}
	
	public boolean isValue(Object dataObject, String objName ) throws Exception {
		return isValue( dataObject, objName, null, null );
	}	
	
	public boolean hasMethod(Object dataObject, String attribute) {
		Field[] fieldList = dataObject.getClass().getDeclaredFields();
		for (int i = 0; i < fieldList.length; i++) {
			fieldList[i].setAccessible(true);
			if (attribute.equalsIgnoreCase(fieldList[i].getName())) {
				return true;
			}
		}
		return false;
	}
	
	public List getValueFromClass(List fieldsName) throws Exception {
		Class cls = null;
		Field[] fieldList = null;
		String className = "";
		String fieldName = "";
		String fullName = "";
		List valueList = new ArrayList();
		for (int i = 0; i < fieldsName.size(); i++) {
			fullName = String.valueOf(fieldsName.get(i));
			className = fullName.substring(0, fullName.lastIndexOf("."));
			fieldName = fullName.substring(fullName.lastIndexOf(".") + 1, fullName.length());
			if (cls == null || !cls.getName().equalsIgnoreCase(className)) {
				cls = Class.forName(className);
				fieldList = cls.getDeclaredFields();
			}
			for (int j = 0; j < fieldList.length; j++) {
				fieldList[j].setAccessible(true);
				if (fieldName.equalsIgnoreCase(fieldList[j].getName())) {
					valueList.add(fieldList[j].get(null));
				}
			}
		}
		return valueList;
	}
}
