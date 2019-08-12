package com.csc.fsg.nba.process.workflow;
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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.result.RetrieveWorkResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemRequest;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.util.GUID;
import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.database.NbaDatabaseUtils;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.configuration.SystemDataTypes;

/** 
 * NbaSystemDataProcessor processes the NbaSystemData for cache, insert, update, delete etc.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA188</td><td>Version 7</td><td>nbA XML Sources to Auxiliary</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-2</td><td>Version 7</td><td>Performance Tuning and Testing</td></tr> 
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>ALS5900</td><td>AXA Life Phase 1</td><td>QC #4999 - 1 records in AWD and 2 in nbA - Defect for issue #208</td></tr>
 * </table>
 * <p>
 */

public class NbaSystemDataProcessor {

	protected static final String TEMP_ID = "TempId";

	public static SystemDataTypes systemDataTypes = null;

	protected static NbaLogger logger = null;

	static {
		try {
			systemDataTypes = NbaConfiguration.getInstance().getSystemDataTypes();
		} catch (NbaBaseException e) {
			getLogger().logException(e);
		}
	}

	/**
	 * This method removes the NbaSystemData from the case and transactions and stores it in a hashmap.
	 * 
	 * @param nbaDst
	 *                The Dst object containing the requirement control source
	 */
	public static Map cacheSystemData(NbaDst nbaDst) throws NbaBaseException {
		Map systemDataMap = new HashMap();
		int tempIdCounter = 1;
		processCaseOrTransaction(nbaDst, systemDataMap, tempIdCounter);
		//if top level work item is a Case, check case's transactions
		if (nbaDst.isCase()) {
			processChildTransactions(nbaDst, systemDataMap, tempIdCounter);
		}
		return systemDataMap;
	}

	/**
	 * Return list of SystemData which was associated with Temp id for new workItem/transaction and remove the tempId from the work item
	 * 
	 * @param obj
	 *                Object
	 * @param systemDataMap
	 *                Map containing SystemData
	 * @return List containing SystemData
	 */
	public static List getSystemDataOfNewWorkitem(Object obj, Map systemDataMap) {
		List systemData = null;
		WorkItemRequest workItemReq = (WorkItemRequest) obj;
		List workItems = workItemReq.getWorkItems();
		if (workItems.size() > 0) {
			WorkItem work = (WorkItem) workItems.get(0);
			String workId = work.getItemID();
			if (workId != null) {
				systemData = (List) systemDataMap.get(workId);
				systemDataMap.remove(workId);
				work.setItemID(null); //set the item Id as null before sending to AWD
			}
		}
		return systemData;
	}

	/**
	 * Update the systemDataMap with AWD id of parent work item
	 * 
	 * @param resultParams
	 *                map containg AWD result
	 * @param systemDataMap
	 *                map containing SystemData
	 * @param systemData
	 *                List of SystemData
	 */
	public static void addSystemDataWithNewId(RetrieveWorkResult retrieveWorkResult, Map systemDataMap, List systemData) { //NBA213
		List workItems = retrieveWorkResult.getWorkItems(); //NBA213
		if (workItems.size() > 0 && systemData != null) { //NBA213
			WorkItem workItem = (WorkItem) workItems.get(0);
			String id = workItem.getItemID();
			systemDataMap.put(id, systemData);//Update HashMap with AWD id for sources //NBA213
		}
	}

	/**
	 * Update the source information in database
	 * 
	 * @param systemDataMap
	 *                Map containing the systemData
	 */
	public static void updateAuxilliaryDatabaseForSystemData(Map systemDataMap) throws NbaBaseException {
		Iterator keys = systemDataMap.keySet().iterator();
		Iterator allSources;
		String parentId = "";
		Connection conn = null;
		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.NBAAUXILIARY);
			conn.setAutoCommit(false);
			while (keys.hasNext()) {
				parentId = (String) keys.next();
				List sources = (List) systemDataMap.get(parentId);
				allSources = sources.iterator();
				while (allSources.hasNext()) {
					NbaSource nbaSource = (NbaSource) allSources.next();
					updateAuxilliaryDatabase(parentId, nbaSource.getSource(), conn);
				}
			}
			NbaDatabaseUtils.commitConnection(conn);
		} catch (Exception ex) {
			getLogger().logException("During update for NBAAUXILIARY", ex);
			NbaDatabaseUtils.rollbackConnection(conn);
			throw new NbaBaseException(ex);
		} finally {
			NbaDatabaseUtils.returnDBconnection(conn, NbaConfigurationConstants.NBAAUXILIARY);
		}
	}

	/**
	 * Insert, delete, update the source
	 * 
	 * @param parentId
	 *                id of parent work item
	 * @param source
	 *                system data
	 */
	//NBA208-32
	private static void updateAuxilliaryDatabase(String parentId, WorkItemSource source, Connection conn) throws NbaBaseException {
		NbaSystemDataDatabaseAccessor nbaSystemDataDBAccessor = new NbaSystemDataDatabaseAccessor();
		if (NbaConstants.YES_VALUE.equalsIgnoreCase(source.getCreate()) || (NbaConstants.YES_VALUE.equalsIgnoreCase(source.getRelate()))) {
			GUID guid = new GUID();
			if(!NbaConstants.A_ST_CONTRACT_PRINT_EXTRACT.equalsIgnoreCase(source.getSourceType())) { //ALII1668
				nbaSystemDataDBAccessor.insert(parentId, guid.getKeyString(), source.getSourceType(), source.getText(), conn);
			}
		} else if (NbaConstants.YES_VALUE.equalsIgnoreCase(source.getUpdate())) {
			//NBA208-32
			nbaSystemDataDBAccessor.update(source.getItemID(), source.getText(), conn);
		} else if (NbaConstants.YES_VALUE.equalsIgnoreCase(source.getBreakRelation())) {
			//NBA208-32
			nbaSystemDataDBAccessor.delete(source.getItemID(), conn);
		}
	}

	/**
	 * Returns arraylist containing all NbaSystemData for the workItemId
	 * 
	 * @param workItemId
	 *                the id to retrieve NbaSystemData
	 * @return list containing all NbaSystemData for the workItemId
	 */
	public static List getSystemData(String workItemId) throws NbaBaseException {
		NbaSystemDataDatabaseAccessor nbaSystemDataDBAccessor = new NbaSystemDataDatabaseAccessor();
		List sources = nbaSystemDataDBAccessor.select(workItemId);
		return (sources == null ? new ArrayList() : sources);
	}

	/**
	 * Returns Map containing all NbaSystemData for the workItemId List
	 * 
	 * @param workItemIdList
	 *                the List of id's to retrieve NbaSystemData
	 * @return Map containing all NbaSystemData for the workItemId List
	 */
	//  NBA208-2 New Method
	public static Map getSystemData(List workItemIdList) throws NbaBaseException {
		NbaSystemDataDatabaseAccessor nbaSystemDataDBAccessor = new NbaSystemDataDatabaseAccessor();
		return nbaSystemDataDBAccessor.select(workItemIdList);
	}

	/**
	 * Caches the NbaSystemData from the Case of Transaction work item
	 * 
	 * @param nbaDst
	 *                the NbaDst object to cache NbaSystemData from
	 * @param systemDataMap
	 *                Map containing the cached NbaSystemData
	 * @param tempIdCounter
	 *                counter for generating temporary id
	 * @throws NbaBaseException
	 */
	public static void processCaseOrTransaction(NbaDst nbaDst, Map systemDataMap, int tempIdCounter) throws NbaBaseException {
		int systemDataCount = getSystemDataTypes().getTypeCount();
		List cacheSourceList = new ArrayList(5);
		NbaSource nbaSrc;
		Iterator nbaSources = nbaDst.getNbaSources().iterator();
		while (nbaSources.hasNext()) {
			nbaSrc = (NbaSource) nbaSources.next();
			for (int i = 0; i < systemDataCount; i++) {
				if ((getSystemDataTypes().getTypeAt(i)).equalsIgnoreCase(nbaSrc.getSourceType())) {
					if (nbaDst.getID() == null) {
						nbaDst.setID(TEMP_ID + tempIdCounter);
						tempIdCounter++;
					}
					if (nbaDst.isCase()) {
						cacheSourceList.add(nbaSrc);
						//NBA208-32
						nbaDst.getNbaCase().getCase().getSourceChildren().remove(nbaSrc.getSource());
					} else {
						cacheSourceList.add(nbaSrc);
						//NBA208-32
						nbaDst.getNbaTransaction().getTransaction().getSourceChildren().remove(nbaSrc.getSource());
					}
					break;
				}
			}
		}
		if (cacheSourceList.size() > 0) {
			systemDataMap.put(nbaDst.getID(), cacheSourceList);
		}
	}

	/**
	 * Caches the NbaSystemData from the transactions of NbaDst
	 * 
	 * @param nbaDst
	 *                the NbaDst object to cache NbaSystemData from
	 * @param systemDataMap
	 *                Map containing the cached NbaSystemData
	 * @param tempIdCounter
	 *                counter for generating temporary id
	 * @throws NbaBaseException
	 */
	public static void processChildTransactions(NbaDst nbaDst, Map systemDataMap, int tempIdCounter) throws NbaBaseException {
		List cacheSourceList = null;
		NbaSource nbaSrc;
		Iterator nbaSources;
		Iterator allTransactions = nbaDst.getTransactions().iterator();
		NbaTransaction nbaTransaction;
		int systemDataCount = getSystemDataTypes().getTypeCount();
		while (allTransactions.hasNext()) {
			//NBA208-32
			nbaTransaction = new NbaTransaction((WorkItem) allTransactions.next());
			nbaSources = nbaTransaction.getNbaSources().iterator();
			cacheSourceList = new ArrayList(5);
			while (nbaSources.hasNext()) {
				nbaSrc = (NbaSource) nbaSources.next();
				for (int i = 0; i < systemDataCount; i++) {
					if ((getSystemDataTypes().getTypeAt(i)).equalsIgnoreCase(nbaSrc.getSourceType())) {
						if (nbaTransaction.getID() == null) {
							nbaTransaction.setID(TEMP_ID + tempIdCounter);
							tempIdCounter++;
						}
						cacheSourceList.add(nbaSrc);
						//NBA208-32
						nbaTransaction.getTransaction().getSourceChildren().remove(nbaSrc.getSource());
						break;
					}
				}
			}
			if (cacheSourceList.size() > 0) {
				systemDataMap.put(nbaTransaction.getID(), cacheSourceList);
			}
		}
	}

	/**
	 * Return <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaSystemDataProcessor.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log(NbaSystemDataProcessor.class.getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * @return Returns the systemDataTypes.
	 */
	public static SystemDataTypes getSystemDataTypes() {
		return systemDataTypes;
	}

	//ALS5900 New Method
	public static void updatePolNumberToAuxilliaryDB(String polNumber, String workItemId) throws NbaBaseException {
		new NbaSystemDataDatabaseAccessor().updatePolNumber(polNumber, workItemId);
	}

	//ALS5900 New Method
	public static String selectPolNumberFromAuxilliaryDB(String workItemId) throws NbaBaseException {
		return new NbaSystemDataDatabaseAccessor().selectPolNumber(workItemId);
	}
	
	// APSL4756 new method
	public static void updateAuxilliaryDatabaseForCheckList(String polNumber, String type, String data, boolean actionInd,boolean completedInd) throws NbaBaseException {

		Connection conn = null;
		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.NBAAUXILIARY);
			conn.setAutoCommit(false);
			updateAuxilliaryDb(polNumber, type, data, conn, actionInd,completedInd);
			NbaDatabaseUtils.commitConnection(conn);
		} catch (Exception ex) {
			getLogger().logException("During update for NBAAUXILIARY", ex);
			NbaDatabaseUtils.rollbackConnection(conn);
			throw new NbaBaseException(ex);
		} finally {
			NbaDatabaseUtils.returnDBconnection(conn, NbaConfigurationConstants.NBAAUXILIARY);
		}
	}
	
	// APSL4756 new method
	private static void updateAuxilliaryDb(String polNumber, String type, String data, Connection conn, boolean actionInd,boolean completedInd) throws NbaBaseException {
		NbaSystemDataDatabaseAccessor nbaSystemDataDBAccessor = new NbaSystemDataDatabaseAccessor();
		if (!actionInd) {
			nbaSystemDataDBAccessor.insert(polNumber, type, data, conn, completedInd);
		} else {
			nbaSystemDataDBAccessor.updateCheckList(polNumber, data,type, conn, completedInd);
		}
	}
	
	// APSL4756 new method
	public static String selectCheckListFromAuxilliaryDB(String polNumber, String type) throws NbaBaseException {
		return new NbaSystemDataDatabaseAccessor().selectCheckList(polNumber, type);
	}
	
	// APSL4756 new method
	public static int selectChkLstCompletedIndFromDB(String polNumber,String checkType) throws NbaBaseException {
		return new NbaSystemDataDatabaseAccessor().selectChkLstCompletedInd(polNumber,checkType);

	}

}
