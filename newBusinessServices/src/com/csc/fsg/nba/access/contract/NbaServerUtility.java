package com.csc.fsg.nba.access.contract;
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
import java.util.HashMap;

import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NewBusinessGetDataStoreRequest;



/**
 * This class defines utility functions for nbA enterprise java beans (EJBs).
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>SPR1906</td><td>Version 4</td><td>General source code clean up</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-26</td><td>Version 7</td><td>Remove synchronized keyword from getLogger() methods</td></tr>
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaServerUtility implements NbaConstants {
	protected static NbaLogger logger = null;	
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	protected static NbaLogger getLogger() { // NBA208-26
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaServerUtility.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaServerUtility could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	//NBA213 deleted code
	//NBA112 moved getExternalAgentSystemData and all data merging methods to NbaAgentNameAddressRetrieve
	/**
	 * Return true if the primary data store is the nbA Pending database
	 * @return boolean
	 */
	//SPR1715 New Method
	public static boolean isDataStoreDB(NbaTXLife nbaTXLife) throws NbaBaseException {
		NbaLob aNbalob = new NbaLob();
		// ACN014 begin
		if( nbaTXLife.isCase()) {
			aNbalob.updateLobFromNbaTxLife(nbaTXLife);
		} else {
			aNbalob.updateLobForTransactionFromNbaTxLife(nbaTXLife);
		}
		// ACN014 end
		//update all other lobs required
		aNbalob.setTypeCase(true); //it should have all LOBs set required to determine source mode
		if (aNbalob.getBackendSystem() == null || aNbalob.getBackendSystem().length() == 0) {
			//NBA231 code deleted
			aNbalob.setBackendSystem(getBackendSystem(aNbalob));//NBA231
		}
		return isDataStoreDB(aNbalob, null);
	}
	
	//NBA231 new method
	public static String getBackendSystem(NbaLob aNbalob) throws NbaBaseException{
		NbaTableAccessor table = new NbaTableAccessor();
		HashMap map = table.createDefaultHashMap(NbaTableAccessConstants.WILDCARD);
		map.put(NbaTableAccessConstants.C_COMPANY_CODE, aNbalob.getCompany());
		map.put(NbaTableAccessConstants.C_COVERAGE_KEY, aNbalob.getPlan());
		NbaPlansData planData = table.getPlanData(map);
		if (planData == null) {
			throw new NbaBaseException("Plan data is missing or invalid");
		}
		return planData.getSystemId();
		
	}
	/**
	 * Return true if the primary data store is the nbA Pending database
	 * @return boolean
	 */
	//SPR1715 New Method
	public static boolean isDataStoreDB(NbaLob nbaLob, NbaUserVO userVO) throws NbaBaseException {
		return STANDALONE.equalsIgnoreCase(getDataStore(nbaLob, userVO)); //NBA077
	}
	/**
	 * Returns the dataStore.
	 * @return int
	 */
	//SPR1715 New Method
	//NBA077 changed return type from int to String. Also changed access type from protected to public
	//NBA213 removed throws NbaBaseException
	public static String getDataStore(NbaLob nbaLob, NbaUserVO userVO) throws NbaBaseException {
		String dataStore = NbaConstants.STANDALONE; //SPR1906 NBA077
		//begin NBA077
		if(nbaLob.getOperatingMode() != null){
			dataStore = nbaLob.getOperatingMode();
		}else{		
		//end NBA077
			//begin NBA213
			NewBusinessGetDataStoreRequest request = new NewBusinessGetDataStoreRequest();
			request.user = userVO;
			request.lob = nbaLob;
			ServiceContext.currentContext();
			AccelResult getDataStoreRes = (AccelResult)ServiceHandler.invoke("GetDataStoreBP", ServiceContext.currentContext(), request);
	        if (!getDataStoreRes.isErrors()) {
	        	Object obj = getDataStoreRes.getFirst();
	        	dataStore = obj.toString();
	        }
			//end NBA213
		}
		return dataStore; 
	}
	//NBA213 deleted code
}
