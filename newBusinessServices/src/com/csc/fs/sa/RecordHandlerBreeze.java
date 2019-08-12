/*
 * This software and/or documentation contains trade secrets and
 * confidential information, which are proprietary to
 * Computer Sciences Corporation.
 * The use, reproduction, distribution, or disclosure of this
 * software and/or documentation, in whole or in part, without the express
 * written permission of Computer Sciences Corporation is prohibited.
 * This software and/or documentation is also an unpublished work protected
 * under the copyright laws of the United States of America and other countries.
 * If this software and/or documentation becomes published, the following
 * notice shall apply:
 *
 * Copyright © 2004 Computer Sciences Corporation. All Rights Reserved.
 */
package com.csc.fs.sa;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import com.csc.fs.DataObject;
import com.csc.fs.ErrorHandler;
import com.csc.fs.Message;
import com.csc.fs.Messages;
import com.csc.fs.Result;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.om.ObjectFactory;
import com.csc.fs.ra.SimpleMappedRecord;
import com.csc.fs.sa.InteractionMapping.Item;
import com.csc.fsg.nba.vo.NbaContractVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransBool;
import com.csc.fsg.nba.vo.NbaTransDate;

/**
 * RecordHandlerBreeze extends RecordHandlerBase to handle nuances in Breeze classes.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA234</td><td>Version 8</td><td> nbA ACORD Transformation Service Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class RecordHandlerBreeze extends RecordHandlerBase implements RecordHandler {
	protected static final String DUMMY = "DUMMY"; //$NON-NLS-1$
	protected static final String TRANSFORM = "com.csc.fs.dataobject.accel.Transform"; //$NON-NLS-1$
	protected static final String JAVA_UTIL_ARRAYLIST = "java.util.ArrayList"; //$NON-NLS-1$
	protected static final String ZERO = "0"; //$NON-NLS-1$
	protected static final String ONE = "1"; //$NON-NLS-1$
	protected static final String HAS = "has"; //$NON-NLS-1$
	protected static final String DOUBLE = "double"; //$NON-NLS-1$
	protected static final String JAVA_UTIL_DATE = "java.util.Date"; //$NON-NLS-1$
	protected static final String NBATIME = "com.csc.fsg.nba.vo.NbaTime"; //$NON-NLS-1$
	protected static final String DOT = "."; //$NON-NLS-1$
	protected static final String SET = "set"; //$NON-NLS-1$
	protected static final String BOOLEAN = "boolean"; //$NON-NLS-1$
	protected static final int NONE = -1;
	protected final static DecimalFormat df = new DecimalFormat("####################.####################"); //$NON-NLS-1$

 	/**
	 * Creates a new DataObject.
	 *
	 * @param className  Class of the DataObject
	 * @return           The new DataObject
	 */
	protected DataObject createDataObject(String className) {
		RecordParams params = getParams();
		DataObject cur = null;
		try {
			Class cls =
				Thread.currentThread().getContextClassLoader().loadClass(
					className);
			cur = ObjectFactory.create(cls, false, getServiceContext());
		} catch (Exception e) {
			throw ErrorHandler.process(
				getClass(),
				new RuntimeException(
					"Failed to create DataObject for class["
						+ className
						+ "] with error ["
						+ e.getMessage()
						+ ']'));
		}
		if (cur != null) {
			if (cur instanceof NbaContractVO) {	//Prevent problems caused by CovOption.className
				((NbaContractVO) cur).setInstanceClassName(className);
			} else {
				cur.setClassName(className);
			}
			if (params.parentChildLevel > 1) {
				cur.setParentIdentifier(params.parentIDs[params.parentChildLevel - 1]);			
				if (LogHandler.Factory.isLogging(LogHandler.LOG_LOW_LEVEL_DEBUG)) {
					int level = params.parentChildLevel - 1;
					LogHandler.Factory.LogLowLevelDebug("Setting parent id of  " + cur.getClass().getName() + " to "
							+ params.parentIDs[params.parentChildLevel - 1] + " from level= " + level, "");
				}
			}
			String objID = cur.getIdentifier();
			if (objID != null) {
				if (params.parentChildLevel >= params.parentIDs.length) {
					String[] t = new String[params.parentIDs.length + 5];
					System.arraycopy(params.parentIDs, 0, t, 0, params.parentIDs.length);
					params.parentIDs = t;
				}
				params.parentIDs[params.parentChildLevel] = objID;	
				if (LogHandler.Factory.isLogging(LogHandler.LOG_LOW_LEVEL_DEBUG)) {
					LogHandler.Factory.LogLowLevelDebug("Setting level " + params.parentChildLevel + " objID=" + objID + " Class="
							+ cur.getClass().getName(), "");
				}
			}
			cur.markOriginal();
		}
		return cur;
	}




    protected DataObject locateDataObject(
            DataObject last,
            Map process,
            List inputs,
            DictionaryItem dict,
            InteractionMapping.Item item) {
		RecordParams params = getParams();
		String className;
		if (last != null) {
			if (last instanceof NbaContractVO) {		//Prevent problems caused by CovOption.className
				className = ((NbaContractVO) last).getInstanceClassName();
			} else {
				className = last.getClassName();
			}
			if (dict.getObjectClassName().equals(className)) {
				if (item.applyCriteria(last, process)) {
					return last;
				}
			}
		}
		DataObject[] objs = new DataObject[inputs.size()];
		inputs.toArray(objs);

		int len = objs.length - 1;
		for (int i = len; i > -1; i--) {	//Start at the end to get the most recent instance to prevent all occurrences from being associated with the first instance 
			DataObject obj = objs[i];			
			if (obj == null || dict == null) {
				continue;
			}
			if (obj instanceof NbaContractVO) {		//Prevent problems caused by CovOption.className
				className = ((NbaContractVO) obj).getInstanceClassName();
			} else {
				className = obj.getClassName();
			}
			if (!dict.getObjectClassName().equals(className)){
				continue;
			}
			if (item.applyCriteria(obj, process)) {
				if (params.processingOutbound && params.parentChildLevel > 1) {
					String parentId = obj.getParentIdentifier();
					if (parentId != null) {
						if (!parentId.equals(params.parentIDs[params.parentChildLevel - 1])) {
							LogHandler.Factory.LogError(this, "Parent not found for " + obj.getClassName());
							continue;
						}
					}
				}
				List items = (List) process.get(item.getName());
				if (items == null) {
					items = new ArrayList();
					process.put(item.getName(), items);
				}
				items.add(obj);
				String oid = obj.getIdentifier();
                if (params.processingOutbound) {
					checkIdCapacity();
					params.parentIDs[params.parentChildLevel] = oid;
					if (LogHandler.Factory.isLogging(LogHandler.LOG_LOW_LEVEL_DEBUG)) {
						LogHandler.Factory.LogLowLevelDebug("Adding level " + params.parentChildLevel + " oid=" + oid + " Class="
								+ obj.getClass().getName(), "");
					}
				}
				return obj;
			}
		}
		return null;
	}
	/**
	 * Constructs the record for the outbound request.
	 *
	 * @param api    Description of Parameter
	 * @param input  Description of Parameter
	 * @return       Description of the Returned Value
	 */
	protected Record outbound(SystemAPI api, List input) {
		InteractionMapping im = getInputMapping(api);
        Record record = createRecord(im, api);
		initializeParentChildData();
		RecordParams params = getParams();
		params.processingOutbound = true;
		params.processed = new HashMap();
		processOutboundInteractionMap(im, params.processed, record, input, api, false, NONE);
		if (LogHandler.Factory.isLogging(LogHandler.LOG_LOW_LEVEL_DEBUG)) {
			logRecord(record, "Outbound record");
		}
        return record;
	}
    /**
     * Constructs a Result object containing the outbound record
     * structure from the input list and definitions provided for
     * a collection of API calls.
     *
     * @param apis   Description of Parameter
     * @param input  Description of Parameter
     * @return       The hierarchy of Records
     */
 	protected boolean processOutboundInteractionMap(InteractionMapping interactionMapping, Map processMap, Record outBoundRecord, List inputData,
			SystemAPI systemAPI, boolean isRepeating, int lastIdx) {
		getParams().parentChildLevel++;
		int currIdx;
		Record subRecord;
		boolean hasData;
		boolean includeThis = false;
		DataObject parentDataObject = null;
		if (lastIdx != NONE) {
			parentDataObject = (DataObject) inputData.get(lastIdx);
		}
		currIdx = getDataObject(parentDataObject, interactionMapping, lastIdx, inputData);
		if (currIdx > NONE || lastIdx == NONE) {
			if (lastIdx == NONE) {
				subRecord = outBoundRecord;
			} else {
				subRecord = createRecord(interactionMapping, systemAPI);
			}
			if (interactionMapping.repeats) {
				int count = 0;
				while (currIdx > NONE) {
					Record subRepeatRecord = createRecord(interactionMapping, systemAPI);
					hasData = populateOutBoundRecord(interactionMapping, processMap, subRepeatRecord, inputData, systemAPI, isRepeating, currIdx);
					if (hasData) {
						includeThis = true;
						if (subRecord instanceof IndexedRecord) {
							((IndexedRecord) subRecord).add(subRepeatRecord);
						} else {
							((MappedRecord) subRecord).put(new Integer(count++), subRepeatRecord);
						}
					}
					currIdx = getDataObject(parentDataObject, interactionMapping, currIdx, inputData);
				}
				if (outBoundRecord instanceof IndexedRecord) {
					((IndexedRecord) outBoundRecord).add(subRecord);
				} else {
					((MappedRecord) outBoundRecord).put(interactionMapping.getName(), subRecord);
				}
			} else {
				if (lastIdx == NONE) {
					subRecord = outBoundRecord;
				} else {
					subRecord = createRecord(interactionMapping, systemAPI);
				}
				includeThis = populateOutBoundRecord(interactionMapping, processMap, subRecord, inputData, systemAPI, isRepeating, currIdx);
				if (includeThis && lastIdx != NONE) {
					if (outBoundRecord instanceof IndexedRecord) {
						((IndexedRecord) outBoundRecord).add(subRecord);
					} else {
						((MappedRecord) outBoundRecord).put(interactionMapping.getName(), subRecord);
					}
				}
			}
		}
		getParams().parentChildLevel--;
		return includeThis;
	}

	/**
	 * @param item                       Description of Parameter
	 * @param last                       Description of Parameter
	 * @param processed                  Description of Parameter
	 * @param results                    Description of Parameter
	 * @param record                     Description of Parameter
	 * @param currentItemIndex           Description of Parameter
	 * @param systemAPI                  Description of Parameter
	 * @param isMappedRecord             Description of Parameter
	 * @return                           Description of the Returned Value
	 * @exception SystemAccessException  Description of Exception
	 */
    protected DataObject setupInboundItem(
            InteractionMapping.Item item,
            DataObject last,
            Map processed,
            List results,
            Record record,
            int currentItemIndex,
            SystemAPI systemAPI,
            boolean isMappedRecord)
             throws SystemAccessException {
		RecordParams params = getParams();

		List inboundRules = item.getInboundRules();
		if (!inboundRules.isEmpty()) {
			for (int i = 0; i < inboundRules.size(); i++) {
				String rule = (String) inboundRules.get(i);
				Result ruleResult = invokeRule(rule, item, record, results, systemAPI);
				if (!ruleResult.hasErrors()) {
					if (params.newRecord) {
						params.newRecord = false;
					}
					if (ruleResult.getReturnData() != null && !ruleResult.getReturnData().isEmpty()) {
						last = (DataObject) ruleResult.getReturnData().get(0);
					}
				}
			}
		} else {
			Object value = null;
			if (isMappedRecord) {
				value = ((MappedRecord) record).get(item.getName());
			}
			else {
				value = ((IndexedRecord) record).get(currentItemIndex);
			}
			if (value != null) {
				switch (item.getType()) {
				case InteractionMapping.Item.INSESSION:
					setValueInSession(systemAPI, item.getFieldName(), value);
					break;
				case InteractionMapping.Item.INPROFILE:
						ErrorHandler.log(
								Constants.ErrorCodes.ATTEMPT_TO_UPDATE_PROFILE_DATA
								 + "["
								 + item.getName()
								 + "]");
					break;
				default:
					// InteractionMapping.Item.INOBJECT
					// InteractionMapping.Item.INKEY
					DataObject cur = null;
					if (params.newRecord) {
						if (item.getObjectName() != null) {
							cur = createDataObject(item.getObjectName());
							params.newRecord = false;
							if (cur != null) {
								List items = new ArrayList();
								items.add(cur);
								processed.put(item.getName(), items);
								results.add(cur);
							}
						}
					}
						else {
							cur =
								locateDataObject(
								last,
								processed,
								results,
								item.getDictionaryItem(),
								item);
						}
					if (item.getRemoveString() != null && !item.getRemoveString().isEmpty()) {
						Iterator strings = item.getRemoveString().iterator();
						String stringValue = value.toString();
						while (strings.hasNext()) {
							String current = (String) strings.next();
							stringValue = stringValue.replaceAll(current, "");
						}
						value = stringValue;
					}
					try {
						if (item.isRepeating() && value instanceof SimpleMappedRecord) {	//values for xxxCC objects 
							List values = createListFromMap(item, (SimpleMappedRecord) value);
							item.setValue(cur, values, systemAPI.getSystemName());
						} else {
							if (value != null) {
								setInboundValue(item, cur, value, systemAPI.getSystemName());
							}
						}
					} catch (NullPointerException e) {
							throw ErrorHandler.process(
								getClass(),
								new RuntimeException(
									"Data type mismatch resulting in nullPointer [for object: "
										+ cur.getClassName() + ", item: " + item.getFieldName() 
										+ ", value :" + value + "]"));						
					} catch (Exception e) {
							throw ErrorHandler.process(
								getClass(),
								new RuntimeException(
									"Failed to set data value [for object: "
										+ cur.getClassName() + ", item: " + item.getFieldName() 
										+ ", value :" + value
										+ "] with error [" + e.getMessage() + ']'));
					}
					last = cur;
					break;
				}
			} else if (params.newRecord) {
				if (item.getObjectName() != null) {
					DataObject cur = createDataObject(item.getObjectName());
					params.newRecord = false;
					if (cur != null) {
						List items = new ArrayList();
						items.add(cur);
						processed.put(item.getName(), items);
						results.add(cur);
						last = cur;
					}
				}
			}
		}
		return last;
	}

	/**
	 * @return
	 */
	protected boolean populateOutBoundRecord(InteractionMapping interactionMapping, Map processMap, Record outBoundRecord, List inputData,
			SystemAPI systemAPI, boolean isRepeating, int lastIdx) {
		InteractionMapping.Item interItem;
		Item[] items = interactionMapping.getItems();
		int count = items.length;
		boolean includeThis = false;
		boolean hasData;
		for (int i = 0; i < count; i++) {
			interItem = items[i];
			if ((interItem.getMappingName() != null) && !(interItem.getMappingName().equals(""))) {
				InteractionMapping subInterMap = InteractionMapping.get(interItem.getMappingName());
				hasData = processOutboundInteractionMap(subInterMap, processMap, outBoundRecord, inputData, systemAPI, isRepeating, lastIdx);
			} else {
				hasData = populateOutBoundField(interItem, outBoundRecord, inputData, systemAPI, lastIdx);
			}
			includeThis = includeThis || hasData;
		}
		return includeThis;
	}
	/**
	 * 
	 */
	protected boolean populateOutBoundField(InteractionMapping.Item interItem, Record outBoundRecord, List inputData, SystemAPI systemAPI, int lastIdx) {
		DictionaryItem dict = interItem.getDictionaryItem(); 
		int beginSize;
		if (outBoundRecord instanceof IndexedRecord) {
			beginSize = ((IndexedRecord) outBoundRecord).size();
		} else {
			beginSize = ((MappedRecord) outBoundRecord).size();
		}
		Object value;
		switch (interItem.getType()) {
		case InteractionMapping.Item.INSESSION:
			addValueFromSession(systemAPI, interItem.getFieldName(), interItem.getName(), outBoundRecord);
			break;
		case InteractionMapping.Item.INPROFILE:
			value = getSessionValue(systemAPI, interItem.getFieldName());
			addValueFromProfile(systemAPI, interItem.getFieldName(), interItem.getName(), outBoundRecord);
			break;
		default:
			List outboundRules = interItem.getOutboundRules();
			if (outboundRules != null && !outboundRules.isEmpty()) {
				for (int i = 0; i < outboundRules.size(); i++) {
					String rule = (String) outboundRules.get(i);
					invokeRule(rule, interItem, outBoundRecord, inputData, systemAPI);
				}
			} else {
				DataObject dataObject = (DataObject) inputData.get(lastIdx);
				if (DUMMY.equalsIgnoreCase(interItem.getName())) {
					value = null;
				} else {
					value = interItem.getValue(dataObject, systemAPI.getSystemName());
					if (value == null) {
						value = interItem.getDefaultValue();
					}
				}
				setFieldInRecord(outBoundRecord, interItem.getName(), getOutboundValue(value, interItem, dataObject, systemAPI));
			}
			break;
		}
		if (outBoundRecord instanceof IndexedRecord) {
			return beginSize != ((IndexedRecord) outBoundRecord).size();
		}
		return beginSize != ((MappedRecord) outBoundRecord).size();
	}
	/**
	 * @param value
	 * @return
	 */
	protected Object getOutboundValue(Object value, InteractionMapping.Item interItem, DataObject dataObject, SystemAPI systemAPI) {
		if (value == null) {
			return value;
		}
		if (TRANSFORM.equals(interItem.className)) {
			return value;
		}
		if (interItem.isRepeating() && JAVA_UTIL_ARRAYLIST.equals(interItem.field.getType().getName())) {
			//convert array values into Map
			List list = (List) value;
			if (list.size() > 0) {
				Record record = createRecord(interItem, systemAPI);
				Record repeatRecord;
				int count = 0;
				for (int i = 0; i < list.size(); i++) {
					Object obj = list.get(i);
					if (obj != null) {
						repeatRecord = createRecord(interItem, systemAPI);
						((MappedRecord) repeatRecord).put(interItem.getName(), obj.toString());
						((MappedRecord) record).put(new Integer(count++), repeatRecord);
					}
				}
				return record;
			}
			return null;
		}
		try {
			String methodName = HAS + interItem.getFieldName().substring(0, 1).toUpperCase() + interItem.getFieldName().substring(1);
			Method hasMethod = interItem.classObj.getMethod(methodName, null);
			Boolean o = (Boolean) hasMethod.invoke(dataObject, EMPTY_ARGS);
			if (!o.booleanValue()) {
				return null;
			}
		} catch (Throwable currentException) {
			String errorMessage = "Unable to obtain has method for field [" + interItem.getFieldName() + "] exceptions [" + currentException + "]";
			LogHandler.Factory.LogDebug(this, errorMessage);
			return value;
		}
		if (BOOLEAN.equals(interItem.field.getType().getName())) {
			if (((Boolean) value).booleanValue())
				return ONE;
			else
				return ZERO;
		}
		if (DOUBLE.equals(interItem.field.getType().getName())) {
			return df.format(value);
		}
		if (JAVA_UTIL_DATE.equals(interItem.field.getType().getName())) {
			return com.tbf.util.DateUtilities.getFormattedDate((Date) value, NbaTransDate.TXLIFE_DATEFORMAT);
		}
		if (NBATIME.equals(interItem.field.getType().getName())) {
			return ((NbaTime) value).formatXMLString(null);
		}
		return value;
	}
    /**
	 * @param item
	 * @param value
	 * @return
	 */
	protected List createListFromMap(InteractionMapping.Item item, SimpleMappedRecord simpleMappedRecord) {
		Set keySet = simpleMappedRecord.keySet();
		Iterator it = keySet.iterator();
		List values = new ArrayList();
		while (it.hasNext()) {
			Object key = it.next();
			Object x = simpleMappedRecord.get(key);
			if (x instanceof SimpleMappedRecord) {
				Object y = ((SimpleMappedRecord) x).get(item.getName());
				if (y != null) {
					Object inBoundValue = getInboundValue(item, y);
					if (inBoundValue != null) {
						values.add(inBoundValue);
					}
				}
			}
		}
		return values;
	}
	/**
	 * @param interItem
	 * @param systemAPI
	 * @return
	 */
	protected Record createRecord(InteractionMapping.Item interItem, SystemAPI systemAPI) {
		Record record = null;
		RecordFactory rf = getRecordFactory(systemAPI);
		try {
			record = rf.createMappedRecord(interItem.getName());
		} catch (ResourceException e) {
			record = new MappedRecordBase(interItem.getName());
		}
		if (record == null) {
			Message msg = Message.create(Messages.SAC_NO_RECORD_CREATED, rf.getClass().getName());
			ErrorHandler.process(getClass(), msg);
		}
		return record;
	}
	/**
	 * @param parentDataObject
	 * @param interactionMapping
	 * @param lastIdx
	 * @param inputData
	 * @return
	 */
	protected int getDataObject(DataObject parentDataObject, InteractionMapping interactionMapping, int lastIdx, List inputData) {
		int count = inputData.size();
		DataObject dataObject;
		String name = interactionMapping.getName();
		String className;
		int dot;
		for (int i = lastIdx + 1; i < count; i++) {
			dataObject = (DataObject) inputData.get(i);
			className = dataObject.getClass().getName();
			dot = className.lastIndexOf(DOT);
			if (dot > 0) {
				className = className.substring(dot + 1);
			}
			if (name.equalsIgnoreCase(className)) {
				if (parentDataObject != null) {
					if (dataObject.getParentIdentifier().equals(parentDataObject.getIdentifier()))
						return i;
				} else {
					return i;
				}
			}
		}
		return NONE;
	}

	/**
	 * @param item
	 * @param value
	 * @return
	 */
	protected Object getInboundValue(Item interItem, Object value) {
		if (value == null) {
			return value;
		}
		if (value instanceof SimpleMappedRecord) {
			Object obj = ((Map) value).get(interItem.getName());
			if (obj != null) {
				if (BOOLEAN.equals(interItem.field.getType().getName())) {
					try {
						return Boolean.valueOf(NbaTransBool.parseBoolean((String) obj));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return obj;
		}
		return value;
	}
	
	/**
	 * Most (if not all) setter methods on Breeze classes accept a String argumen.
	 * First attempt to use Reflection to invoke a setter method with a String argument. If that fails
	 * attempt to invoke a setter method whose argument is the Field type.  
	 * @param cur
	 * @param inBoundValue
	 * @param systemName
	 */
	protected void setInboundValue(InteractionMapping.Item item, DataObject dataObject, Object inBoundValue, String systemName) {
		if (inBoundValue != null) {
			String name = SET + item.attrName.substring(0, 1).toUpperCase() + item.attrName.substring(1);
			try {
				Class cls = Thread.currentThread().getContextClassLoader().loadClass(item.className);
				Method setter = cls.getMethod(name, new Class[] { String.class });
				setter.invoke(dataObject, new Object[] { inBoundValue });
			} catch (Exception e) {
				item.setValue(dataObject, inBoundValue, systemName);		
			}
		}
	}

}