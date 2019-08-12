package com.csc.fsg.nba.assembler.txvalidate;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.csc.fs.ErrorHandler;
import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WebServiceCall;
import com.csc.fs.dataobject.accel.AccelDataObject;
import com.csc.fs.util.GUIDFactory;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.UserAuthRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
import com.tbf.xml.XmlFloatValidator;
import com.tbf.xml.XmlIntegerValidator;
import com.tbf.xml.XmlNamespaceManager;
import com.tbf.xml.XmlObject;
import com.tbf.xml.XmlStringValidator;
import com.tbf.xml.XmlValidator;

/**
 * Transformation service for preparing the <code>TXLife</code> classes for consumption by an accelerator service.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>APSL4508 Websphere 8.5.5 Upgrade</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class TXXMLAssembler extends AccelTransformation {

	protected static NbaLogger logger = null;

	protected static final String USERLOGINNAMEANDUSERPSWD_CAMEL_CASE = "UserLoginNameAndUserPswd"; //$NON-NLS-1$

	protected static final String ORGANIZATION_CLASS = "com.csc.fsg.nba.vo.txlife.Organization"; //$NON-NLS-1$

	protected static final String PSWDORCRYPTPSWD_CLASS = "com.csc.fsg.nba.vo.txlife.PswdOrCryptPswd"; //$NON-NLS-1$

	protected static final String VENDORCODE_CLASS = "com.csc.fsg.nba.vo.txlife.VendorCode"; //$NON-NLS-1$

	protected static final String VENDORAPP_CLASS = "com.csc.fsg.nba.vo.txlife.VendorApp"; //$NON-NLS-1$

	protected static final String GHOST_UPPERCASE = "GHOST"; //$NON-NLS-1$

	protected static final String VALIDATOR = "_VALIDATOR_"; //$NON-NLS-1$

	protected static final String UNCAUGHT_EXCEPTION = "Uncaught exception processing the response: {0}";

	protected static final String INVALID_REQUEST_OBJECT_FOR_DISASSEMBLER = "Invalid Request object for disassembler";

	protected static final String AND = "And"; //$NON-NLS-1$

	protected static Set classesToIgnoreForTXLife = new HashSet(15);

	protected static final String CSC_VENDOR_CODE = "05"; //$NON-NLS-1$

	protected static final String DOT = "."; //$NON-NLS-1$

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	protected static final String EXTENSION = "Extension"; //$NON-NLS-1$ 

	protected static final String GHOST = "Ghost"; //$NON-NLS-1$

	protected static final String JAVA_LANG_STRING = "java.lang.String"; //$NON-NLS-1$

	protected static final String JAVA_UTIL_ARRAYLIST = "java.util.ArrayList"; //$NON-NLS-1$

	protected static final String METHOD_ADDOLIFEEXTENSION = "addOLifEExtension"; //$NON-NLS-1$

	protected static final String METHOD_PREFIX_ADD = "add"; //$NON-NLS-1$

	protected static final String METHOD_PREFIX_GET = "get"; //$NON-NLS-1$

	protected static final String METHOD_PREFIX_SET = "set"; //$NON-NLS-1$

	protected static final String NOT_ACCELDATAOBJECT = "Argument not an instance of AccelDataObject"; //$NON-NLS-1$

	protected static final String OLIFEEXTENSION = "OLifEExtension"; //$NON-NLS-1$

	protected static final String OLIFEEXTENSION_UC = "OLIFEEXTENSION"; //$NON-NLS-1$

	protected static final String OR = "Or"; //$NON-NLS-1$

	protected static final String PARENT = "Parent"; //$NON-NLS-1$

	protected static final String SETEXTENSIONCODE = "setExtensionCode"; //$NON-NLS-1$

	protected static final String SETVENDORCODE = "setVendorCode"; //$NON-NLS-1$

	protected static final String UNDERSCORE = "_"; //$NON-NLS-1$

	protected static final String USERAUTHREQUEST = "USERAUTHREQUEST"; //$NON-NLS-1$

	protected static final String USERAUTHREQUESTANDTXLIFEREQUEST = "USERAUTHREQUESTANDTXLIFEREQUEST"; //$NON-NLS-1$

	protected static final String USERLOGINNAMEANDUSERPSWD = "USERLOGINNAMEANDUSERPSWD"; //$NON-NLS-1$

	protected static final String USERLOGINNAMEANDUSERPSWDORUSERSESSIONKEY = "USERLOGINNAMEANDUSERPSWDORUSERSESSIONKEY"; //$NON-NLS-1$

	protected static final String VENDORAPP = "VENDORAPP"; //$NON-NLS-1$

	protected static final String VERSION_2_8_90 = " 2.8.90"; //$NON-NLS-1$

	static {
		Message.create(234, 101, Message.SEVERE, UNCAUGHT_EXCEPTION);
		classesToIgnoreForTXLife.add(String.class);
		classesToIgnoreForTXLife.add(boolean.class);
		classesToIgnoreForTXLife.add(long.class);
		classesToIgnoreForTXLife.add(int.class);
		classesToIgnoreForTXLife.add(java.util.Date.class);
		classesToIgnoreForTXLife.add(double.class);
		classesToIgnoreForTXLife.add(Vector.class);
		classesToIgnoreForTXLife.add(XmlNamespaceManager.class);
		classesToIgnoreForTXLife.add(XmlValidator.class);
		classesToIgnoreForTXLife.add(XmlObject.class);
		classesToIgnoreForTXLife.add(XmlIntegerValidator.class);
		classesToIgnoreForTXLife.add(XmlStringValidator.class);
		classesToIgnoreForTXLife.add(Hashtable.class);
		classesToIgnoreForTXLife.add(Class.class);
		classesToIgnoreForTXLife.add(XmlFloatValidator.class);
	}

	/**
	 * Assemble the TXLife by inflating a hierarchy of Objects from the List using parent identifiers of the Objects to associate Parents and
	 * children.
	 * 
	 * @param request
	 *            - List containing the a vendor String at [0] and the TXLife at [1]
	 * @return Result containing the vendor String and the flattened TXLife
	 * @see com.csc.fs.accel.AccelTransformation#disassemble(java.lang.Object)
	 */
	public Result assemble(Result request) {
		AccelResult result = new AccelResult();
		try {
			Object accelDataObject = inflate(request.getData());
			NbaTXLife nbaTXLife = new NbaTXLife();
			nbaTXLife.setTXLife((TXLife) accelDataObject);
			result.addResult(nbaTXLife);
		} catch (Exception e) {
			result.addMessage(23401013, new Object[] { e.getMessage() });
		}

		return result;
	}

	/**
	 * Create an OlifeExtension and add it to the parent object and hierarchy.
	 * 
	 * @param hierarchy
	 * @param parentObject
	 * @param parentClass
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected void createOlifeExtension(List hierarchy, AccelDataObject parentObject, Class parentClass, String childFieldName)
			throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

		String parentClassName = parentObject.getClass().getName();
		int idx = parentClassName.lastIndexOf(DOT) + 1;
		Class dataClass = Class.forName(parentClassName.substring(0, idx) + OLIFEEXTENSION);
		Constructor constructor = dataClass.getConstructor(new Class[0]);
		Object extension = constructor.newInstance(new Object[0]);

		// Use Reflection to set the Vendor and Extension codes to avoid referring to the OlifeExtension Class directly
		Class[] parameter = new Class[] { Class.forName(JAVA_LANG_STRING) };
		Method aMethod = dataClass.getDeclaredMethod(SETVENDORCODE, parameter);
		Object[] args = new Object[] { CSC_VENDOR_CODE };
		aMethod.invoke(extension, args);
		idx = childFieldName.lastIndexOf(EXTENSION);
		aMethod = dataClass.getDeclaredMethod(SETEXTENSIONCODE, parameter);
		args = new Object[] { childFieldName.substring(0, idx) + VERSION_2_8_90 };
		aMethod.invoke(extension, args);

		// Add the OlifeExtension to the Parent object

		parameter = new Class[] { dataClass };
		aMethod = parentClass.getMethod(METHOD_ADDOLIFEEXTENSION, parameter);
		args = new Object[] { extension };
		aMethod.invoke(parentObject, args);
		hierarchy.add(extension);
	}

	/**
	 * Create a Wrapper Object for an AND/OR Class and add it to the parent object and hierarchy.
	 * 
	 * @param hierarchy
	 * @param parentObject
	 * @param parentClass
	 * @param aField
	 * @param tempName
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected void createWrapperObject(List hierarchy, AccelDataObject parentObject, Class parentClass, Field aField, String tempName)
			throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		String methodName;
		Method aMethod;
		String classNameForField = aField.getType().getName();
		Class dataClass = Class.forName(classNameForField);
		// Create the obect
		Constructor constructor = dataClass.getConstructor(new Class[0]);
		Object classInstance = constructor.newInstance(new Object[0]);
		methodName = METHOD_PREFIX_SET + tempName;
		Class[] parameter = new Class[] { dataClass };
		aMethod = parentClass.getMethod(methodName, parameter);
		Object[] args = new Object[1];
		args[0] = classInstance;
		aMethod.invoke(parentObject, args);
		hierarchy.add(classInstance);
	}

	/**
	 * Disassemble a TXLife by flattening a hierarchical TXLife object into a List of objects. Disassemble a WebServiceCall by creating a Transform
	 * data object and moving the WebServiceCall.payload to Transform.payload.
	 * 
	 * @param request
	 *            - List containing the a vendor String at [0] and the TXLife at [1]
	 * @return Result containing the vendor String and the flattened TXLife
	 * @see com.csc.fs.accel.AccelTransformation#disassemble(java.lang.Object)
	 */
	public Result disassemble(Object request) throws RuntimeException {
		Result result = new AccelResult();
		if (request instanceof NbaTXLife) {
			List objects = new ArrayList();
		try {
				objects = flatten(((NbaTXLife) request).getTXLife());
			} catch (Exception e) {
				ErrorHandler.process(this.getClass(), e); // Throws RuntimeException
			}
			return Result.Factory.create().addResults(objects);
		}
		if (request instanceof WebServiceCall) {
			String payload = ((WebServiceCall) request).getPayload();
			StringBuffer TXLifeStr = new StringBuffer(payload);
			if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("ProcessXMLValidate Raw xml=" + TXLifeStr.toString());
			}
			// Remove the EIB headers and trailers
			TXLifeStr.delete(0, TXLifeStr.indexOf("<TXLife")+8);
			TXLifeStr.delete(TXLifeStr.toString().indexOf("</TXLife>") + 9, TXLifeStr.length());
			String start = "<TXLife";
			String end = ">";
			String xmlns =" xmlns=\"http://ACORD.org/Standards/Life/2\"";
			TXLifeStr=new StringBuffer(start+xmlns+end+TXLifeStr);
			result.addResult(TXLifeStr);
		    return result;
		}
		ErrorHandler.process(this.getClass(), INVALID_REQUEST_OBJECT_FOR_DISASSEMBLER); // Throws RuntimeException
		return Result.Factory.create();
	}

	/**
	 * Add AccelDataObject objects found in the hierarchy of anObject to returnData, bypassing any objects for which the AccelDataObject is just a
	 * wrapper class (containing no elementary data of its own). This logic uses the xxGhost objects of the parent objects to detect which child
	 * objects are in-memory to prevent database calls from artificially inflating the list of returned objects.
	 * 
	 * @param returnData
	 *            - the List of objects to be returned
	 * @param anObject
	 *            - the current node in the hierarchy
	 * @param previousObjects
	 *            - previously processed objects to prevent "self" references from looping
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	protected final void examineObjectFields(List returnData, AccelDataObject parentObject, AccelDataObject anObject, Set previousObjects)
			throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class anObjectClass = anObject.getClass();
		Field[] fields = anObjectClass.getDeclaredFields(); // get Fields Collection from Class
		if (fields.length > 0) {
			if (!isWrapperOnlyClass(anObject)) { // Do not add wrapper only classes to list
				anObject.setParentIdentifier(parentObject);
				anObject.setIdentifier(GUIDFactory.create().getKeyString());
				returnData.add(anObject);
				previousObjects.add(anObject);
			}
			Field aField;
			Object objValue;
			Object listObjValue;
			for (int i = 0; i < fields.length; i++) { // iterate through each field
				aField = fields[i];
				if (useField(anObject, anObjectClass, aField)) { // Check for potential object
					objValue = getValue(anObject, anObjectClass, aField); // Get the value
					if (objValue != null && !previousObjects.contains(objValue)) {
						if (objValue instanceof AccelDataObject) { // Recurse through it's fields
							flattenNextObject(returnData, parentObject, anObject, previousObjects, objValue);
						} else if (objValue instanceof List) {
							Iterator it = ((List) objValue).iterator();
							while (it.hasNext()) {
								listObjValue = it.next();
								if (listObjValue instanceof AccelDataObject && !previousObjects.contains(listObjValue)) { // Recurse through the
																															// fields in the entry
									flattenNextObject(returnData, parentObject, anObject, previousObjects, listObjValue);
								} else {
									break; // Bypass the list. Contains nothing we're interested in.
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Flatten a hierarchical TXLife object into a List of objects
	 * 
	 * @param aTXLife
	 *            - the TXLife object
	 * @return the List
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	protected final List flatten(Object anObject) throws IllegalArgumentException, SecurityException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		List returnData = new ArrayList();
		if (anObject instanceof AccelDataObject) {
			Set previousObjects = new HashSet();
			examineObjectFields(returnData, null, (AccelDataObject) anObject, previousObjects);
		} else {
			throw new IllegalArgumentException(NOT_ACCELDATAOBJECT);
		}
		return returnData;
	}

	/**
	 * Prevent wrapper only classes from being treated a parents because they are not included in the List.
	 * 
	 * @param returnData
	 * @param parentObject
	 * @param anObject
	 * @param previousObjects
	 * @param objValue
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	protected void flattenNextObject(List returnData, AccelDataObject parentObject, AccelDataObject anObject, Set previousObjects, Object objValue)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (isWrapperOnlyClass(anObject)) {
			examineObjectFields(returnData, parentObject, (AccelDataObject) objValue, previousObjects); // Use the parent of the wrapper only class
		} else {
			examineObjectFields(returnData, anObject, (AccelDataObject) objValue, previousObjects);
		}
	}

	/**
	 * Return the classesToIgnoreForTXLife object
	 * 
	 * @return - classesToIgnoreForTXLife.
	 */
	protected Set getClassesToIgnoreForTXLife() {
		return classesToIgnoreForTXLife;
	}

	/**
	 * Return a String to be used to find the field representing the current (child) object on the parent object. Some child objects are nested more
	 * than one level below the parent within wrapper classes, so special logic is required to navigate to the real parent of the current (child)
	 * object.
	 * 
	 * @param childFieldName
	 *            - the child field name
	 * @param parentObject
	 *            - the parent object.
	 * @return a String to be used to find the field representing the current (child) object on the parent object.
	 */
	protected String getCompareName(String childFieldName, AccelDataObject parentObject) {
		String compareName;
		compareName = childFieldName.toUpperCase();
		if (USERLOGINNAMEANDUSERPSWD.equals(compareName)) {
			if (parentObject instanceof TXLife) {
				compareName = USERAUTHREQUESTANDTXLIFEREQUEST;
			} else if (parentObject instanceof UserAuthRequestAndTXLifeRequest) {
				compareName = USERAUTHREQUEST;
			} else if (parentObject instanceof UserAuthRequest) {
				compareName = USERLOGINNAMEANDUSERPSWDORUSERSESSIONKEY;
			}
		} else if (VENDORAPP.equals(compareName)) {
			if (parentObject instanceof TXLife) {
				compareName = USERAUTHREQUESTANDTXLIFEREQUEST;
			} else if (parentObject instanceof UserAuthRequestAndTXLifeRequest) {
				compareName = USERAUTHREQUEST;
			}
		}
		return compareName;
	}

	/**
	 * Attempt to find the field in the Parent object class. If not found, find a field in an AND/OR or OLifEExtension wrapper Parent Class.
	 * 
	 * @param hierarchy
	 * @param childFieldName
	 * @return Field
	 */
	protected Field getFieldInParent(List hierarchy, String childFieldName) {
		AccelDataObject parentObject = (AccelDataObject) hierarchy.get(hierarchy.size() - 1); // Get last object
		Class parentClass = parentObject.getClass();
		Field aField;
		Field[] fields = parentClass.getDeclaredFields();
		String tempName = childFieldName.substring(0, 1).toLowerCase() + childFieldName.substring(1);
		for (int i = 0; i < fields.length; i++) {
			aField = fields[i];
			if (aField.getName().equals(tempName)) {
				return aField;
			}
		}
		return getfieldInWrapperClass(hierarchy, childFieldName, parentObject, parentClass, fields);
	}

	/**
	 * Find a field in an AND/OR or OLifEExtension wrapper Parent Class. Create the missing wrapper Class (if it doesn't exist) and repeat the process
	 * of locating the field in the wrapper Class.
	 * 
	 * @param hierarchy
	 * @param childFieldName
	 * @param parentObject
	 * @param parentClass
	 * @param fields
	 * @return
	 */
	protected Field getfieldInWrapperClass(List hierarchy, String childFieldName, AccelDataObject parentObject, Class parentClass, Field[] fields) {
		Field aField;
		boolean isExtension = childFieldName.endsWith(EXTENSION);
		boolean isParentOLifEExtension = parentClass.getClass().getName().endsWith(OLIFEEXTENSION);
		String compareName = getCompareName(childFieldName, parentObject);
		String methodName;
		if ("LIFE".equalsIgnoreCase(compareName)) {
			"".toString();
		}
		try {
			if (!isExtension || (isExtension && isParentOLifEExtension)) {
				for (int i = 0; i < fields.length; i++) { // Handle AND/OR clases
					aField = fields[i];
					if (isMatchForName(aField, compareName)) { // If the field name includes the compareName String
						String tempName = aField.getName().substring(0, 1).toUpperCase() + aField.getName().substring(1);
						methodName = METHOD_PREFIX_GET + tempName;
						Method aMethod;
						aMethod = parentClass.getMethod(methodName, null);
						Object obj = aMethod.invoke(parentObject, new Object[0]);
						if (obj != null) { // If object exists
							hierarchy.add(obj);
							return getFieldInParent(hierarchy, childFieldName);
						}
						// Add a new object to the parent
						createWrapperObject(hierarchy, parentObject, parentClass, aField, tempName);
						return getFieldInParent(hierarchy, childFieldName);
					}
				}
			}
			// Create an OLifEExtension parent for the Extension object
			createOlifeExtension(hierarchy, parentObject, parentClass, childFieldName);
			return getFieldInParent(hierarchy, childFieldName);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Determine if a field on the class identifies the object which contains the object represented by the compareName.
	 * 
	 * @param aField
	 *            - the field
	 * @param compareName
	 *            - the name of the object
	 * @return true if the compareName String in included in the aField String and the aField String does not contain "_Validator_" or "Ghost"
	 */
	protected boolean isMatchForName(Field aField, String compareName) {
		String fieldName = aField.getName().toUpperCase();
		return !fieldName.equals(OLIFEEXTENSION_UC) && fieldName.indexOf(compareName) > -1 && fieldName.indexOf(VALIDATOR) < 0
				&& fieldName.indexOf(GHOST_UPPERCASE) < 0;
	}

	/**
	 * Use the Class name of the Object to derive the Field name in the Parent object
	 * 
	 * @param childObject
	 * @return
	 */
	protected String getFieldNameInParent(AccelDataObject childObject) {
		String childClassName = childObject.getClass().getName();
		int start = childClassName.lastIndexOf(DOT);
		childClassName = childClassName.substring(++start);

		return childClassName;
	}

	/**
	 * Create a Map of IDs. The Keys of the Map are the object identifiers. The values in the Map are Lists of objects whose parent ids point to the
	 * objects. Set the Version to 2.8.90 in the OLifE object.
	 * 
	 * @param list
	 * @return
	 */
	protected Map getIDs(List list) {
		Map idMap = new HashMap();
		Iterator it = list.iterator();
		Object obj;
		AccelDataObject accelDataObject;
		String parentIdentifier;
		List childList;
		while (it.hasNext()) {
			obj = it.next();
			if (obj instanceof AccelDataObject) {
				accelDataObject = (AccelDataObject) obj;
				if (obj instanceof TXLife) { // TXLife parent identifier is null
					parentIdentifier = PARENT;
				} else {
					parentIdentifier = accelDataObject.getParentIdentifier();
				}
				childList = (List) idMap.get(parentIdentifier);
				if (childList == null) {
					childList = new ArrayList();
					idMap.put(parentIdentifier, childList);
				}
				childList.add(obj);
			}
			if (obj instanceof OLifE) {
				((OLifE) obj).setVersion(NbaOliConstants.OLIFE_VERSION);
			}
		}
		return idMap;
	}

	/**
	 * Construct a method name result String by concatenating the pre, Field Name and post Strings.
	 * 
	 * @param pre
	 *            - value to be prepended to the result
	 * @param post
	 *            - value to be appended to the result
	 * @param field
	 *            - Field object containing the Field Name
	 * @return the concatenated String
	 */
	protected String getMethodName(String pre, String post, Field field) {
		String fieldName = field.getName();
		StringBuffer buff = new StringBuffer(fieldName.length() + pre.length() + post.length());
		buff.append(pre);
		if (pre.length() == 0) { // Only change the first character to upper case if it is not the first part of the name
			buff.append(fieldName);
		} else {
			buff.append(fieldName.substring(0, 1).toUpperCase()).append(fieldName.substring(1));
		}
		buff.append(post);
		return buff.toString();
	}

	/**
	 * Retrieve the value of a field using Reflection
	 * 
	 * @param anObject
	 *            - the object containing the field
	 * @param anObjectClass
	 *            - the Class of the object
	 * @param field
	 *            - the field
	 * @return the value
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	protected Object getValue(Object anObject, Class anObjectClass, Field field) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName;
		Method aMethod;
		try {
			methodName = getMethodName(METHOD_PREFIX_GET, EMPTY_STRING, field);
			aMethod = anObjectClass.getDeclaredMethod(methodName, null);
		} catch (NoSuchMethodException e) { // Try again without the "get".
			methodName = getMethodName(EMPTY_STRING, EMPTY_STRING, field);
			aMethod = anObjectClass.getDeclaredMethod(methodName, null);
		}
		return aMethod.invoke(anObject, null);
	}

	/**
	 * Inflate a hierarchy of Objects from the List using parent identifiers of the Objects to associate Parents and children
	 * 
	 * @param parentObject
	 * @param idMap
	 */
	protected void inflate(AccelDataObject parentObject, Map idMap, Map clazzes) {
		List childObjects = (List) idMap.get(parentObject.getIdentifier());
		if (childObjects != null) {
			Iterator it = childObjects.iterator();
			AccelDataObject childObject;
			while (it.hasNext()) {
				childObject = (AccelDataObject) it.next();
				String childFieldName = getFieldNameInParent(childObject);
				List hierarchy = new ArrayList();
				hierarchy.add(parentObject);
				Field aField = getFieldInParent(hierarchy, childFieldName);
				if (aField != null) {
					String methodName;
					Object actualParentObj = hierarchy.get(hierarchy.size() - 1);
					Class parentClass = actualParentObj.getClass();
					String classNameForField = aField.getType().getName();
					try {
						if (JAVA_UTIL_ARRAYLIST.equals(classNameForField)) {
							methodName = METHOD_PREFIX_ADD + childFieldName;
						} else {
							methodName = METHOD_PREFIX_SET + childFieldName;
						}
						Class[] parameter = new Class[] { childObject.getClass() };
						Method aMethod = parentClass.getMethod(methodName, parameter);
						Object[] args = new Object[1];
						args[0] = childObject;
						aMethod.invoke(actualParentObj, args);
						inflate(childObject, idMap, clazzes);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Inflate a hierarchy of Objects from the List using parent identifiers of the Objects to associate Parents and children
	 * 
	 * @param list
	 * @return
	 */
	protected AccelDataObject inflate(List list) {
		AccelDataObject parentObject = null;
		Map idMap = getIDs(list);
		Map clazzes = new HashMap();
		List parentList = (List) idMap.get(PARENT);
		if (parentList != null && parentList.size() == 1) {
			parentObject = (AccelDataObject) parentList.get(0);
			inflate(parentObject, idMap, clazzes);
		}
		return parentObject;
	}

	/**
	 * Determine if an ACORD object is potentially in-memory by examining the value returned by invoking the getXxxGhost() method on its parent
	 * object. If the value is null, it is not in memory. Because non-database objects do have getXxxGhost() methods defined on the parent, treat the
	 * absence of a method as a potentially in-memory condition.
	 * 
	 * @param anObject
	 *            - the object containing the field for the ACORD object
	 * @param anObjectClass
	 *            - the Class of the containing object
	 * @param field
	 *            - the field for the ACORD object
	 * @return a boolean indiciting whether the ACORD object is potentially in-memory
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	protected boolean isInMemory(Object anObject, Class anObjectClass, Field field) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		String methodName = getMethodName(METHOD_PREFIX_GET, GHOST, field);
		try {
			Method aMethod = anObjectClass.getDeclaredMethod(methodName, null);
			return (aMethod.invoke(anObject, null) != null);
		} catch (NoSuchMethodException e) {
			return true; // Non-database classes do not have getXxxGhost() methods
		}
	}

	/**
	 * Determine if the object is a wrapper class that contains only references to other compound objects. UserLoginNameAndUserPswd, PswdOrCryptPswd,
	 * VendorCode and VendorApp are not wrapper classes.
	 * 
	 * @param anObject
	 *            - the object
	 * @return boolean
	 */
	protected boolean isWrapperOnlyClass(Object anObject) {
		String className = anObject.getClass().getName();
		return (className.indexOf(OR) > -1 && !className.equals(VENDORAPP_CLASS) && !className.equals(VENDORCODE_CLASS)
				&& !className.equals(PSWDORCRYPTPSWD_CLASS) && !className.equals(ORGANIZATION_CLASS))
				|| className.indexOf(OLIFEEXTENSION) > -1
				|| (className.indexOf(AND) > -1 && className.indexOf(USERLOGINNAMEANDUSERPSWD_CAMEL_CASE) < 0);
	}

	/**
	 * Determine whether a field is a potential in-memory ACORD object. Because some ACORD objects are defined as Object in the parent class, the
	 * field definition alone cannot be used. The concrete class returned by the getXxx() method is the only value that can be trusted. This method
	 * excludes fields which are obviously not potential in-memory ACORD object classes to reduce unnecessary getXxx() method calls and prevent
	 * objects from being retrieved from the database if they are not in-memory. A field is not a potential ACORD object if: - it is defined as final
	 * - or it is identified in a set of classes to ignore - or it represents the Ghost object for the real object - or it is a potential ACORD object
	 * that is not in memory
	 * 
	 * @param aParentObject
	 *            - the object containing the field for the ACORD object
	 * @param aParentObjectClass
	 *            - the Class of the containing object
	 * @param aField
	 *            - the field for the ACORD object
	 * @return a boolean which indicates whether the object is a potential in-memory ACORD object
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected boolean useField(Object aParentObject, Class aParentObjectClass, Field aField) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		return !Modifier.isFinal(aField.getModifiers()) && !getClassesToIgnoreForTXLife().contains(aField.getType())
				&& !aField.getName().endsWith(GHOST) && !aField.getName().endsWith(UNDERSCORE)
				&& isInMemory(aParentObject, aParentObjectClass, aField);
	}
	
	protected String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(TXXMLAssembler.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("TXXMLAssembler could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}	
	
}