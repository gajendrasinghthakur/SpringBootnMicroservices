package com.csc.fsg.nba.process.rules;

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

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkDefaultFormatter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataStoreModeNotFoundException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NewBusinessGetDataStoreRequest;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * Returns the data store mode for a given work item.  If the mode is already set
 * on the lob (NbaLob.OperatingMode), it is returned.  If the current work item is
 * not a case, a search is done to try and find the related case.  The operating mode
 * on the case is returned if it is set.  If that fails, a call is made to the
 * DetermineDataStore VP/MS model to retrieve the data store mode.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class GetDataStoreBP extends NewBusinessAccelBP implements NbaConstants {
	protected static NbaLogger logger = null;	

	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(GetDataStoreBP.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("GetDataStoreBP could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
        	if (input instanceof NewBusinessGetDataStoreRequest) {
            	NewBusinessGetDataStoreRequest req = (NewBusinessGetDataStoreRequest)input;
            	NbaLob nbaLob = req.lob;
            	NbaUserVO userVO = req.user;
            	
        		String dataStore = STANDALONE;
        		if (nbaLob.getOperatingMode() != null) {
        			dataStore = nbaLob.getOperatingMode();
        		} else {
    				NbaLob vpmsLob = nbaLob;
    				// if not a case work item try to search and find the related case
        			if (!nbaLob.isTypeCase() && userVO != null && nbaLob.getPolicyNumber() != null && nbaLob.getCompany() != null) {
	   					vpmsLob = searchForCaseLob(userVO, nbaLob);
        			}
        			// check the case LOB to see if we can avoid a model call
   					if (vpmsLob != nbaLob && vpmsLob.getOperatingMode() != null) {
   						dataStore = vpmsLob.getOperatingMode();
   					} else {
	        			try {
	        				dataStore = getDataStoreSourceFromVpms(vpmsLob);
	        				if (!(dataStore.equalsIgnoreCase(STANDALONE) || dataStore.equalsIgnoreCase(WRAPPERED))) {
	        					throw new NbaDataStoreModeNotFoundException("Unable to determine Data Store Mode");
	        				}
	        			} catch (NbaBaseException ne) {
	        				throw ne;
	        			} catch (Exception e) {
	        				getLogger().logError("Unable to determine Data Store Mode for " + vpmsLob.getPolicyNumber() + "\n" + e.toString());
	        				throw new NbaBaseException("Unable to determine Data Store Mode", e);
	        			}
   					}
        		}

        		result.addResult(dataStore);
            } else {
            	addErrorMessage(result, "Invalid input provided for 'Get Data Store'");
            }
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

	/**
	 * Performs a search for the case work item and returns the case's lobs. If the
	 * case is not found, the original work lobs are returned.
	 * @param user
	 * @param nbaLob
	 * @return
	 */
	protected NbaLob searchForCaseLob(NbaUserVO user, NbaLob nbaLob) {
		NbaSearchVO searchVO = new NbaSearchVO();
		NbaLob newlob = new NbaLob();
		newlob.setPolicyNumber(nbaLob.getPolicyNumber());
		newlob.setCompany(nbaLob.getCompany());
		searchVO.setWorkType(A_WT_APPLICATION);
		searchVO.setNbaLob(newlob);
		searchVO.setResultClassName("NbaSearchResultVO");
		searchVO.setNbaUserVO(user);
		AccelResult searchRes = (AccelResult) callBusinessService("SearchWorkflowBP", searchVO);
		if (!searchRes.hasErrors()) {
			searchVO = (NbaSearchVO) searchRes.getFirst();
			List searchResult = searchVO.getSearchResults();
			if (searchResult != null && searchResult.size() > 0) {
				NbaSearchResultVO resultVO = (NbaSearchResultVO) searchResult.get(0);
				return resultVO.getNbaLob();
			}
		}
		return nbaLob;
	}

	/**
	 * This method calls a VP/MS model that returns 'S' if datastore mode is stand alone
	 * or 'W' if datastore mode is wrappered.
	 * @param lob the lob fields
	 * @return data store source from VP/MS model
	 */
	protected static String getDataStoreSourceFromVpms(NbaLob lob) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
		    if(getLogger().isDebugEnabled()) { //SPR3290
		        getLogger().logDebug("Starting retrieval of Data Store Mode from VP/MS model");
		    } //SPR3290
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(lob);
			oinkData.getFormatter().setDateSeparator(NbaOinkDefaultFormatter.DATE_SEPARATOR_DASH);
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.DETERMINE_DATA_STORE_MODE);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_DATASTOREMODE);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			if (data.wasSuccessful()) {
				if (data.getResultsData() != null && data.getResultsData().size() > 0) {
				    if(getLogger().isDebugEnabled()) { //SPR3290
				        getLogger().logDebug("Completed retrieval of Data Store Mode from VP/MS model");
				    } //SPR3290
					return (String) data.getResultsData().get(0);
				}
			}
			throw new NbaDataStoreModeNotFoundException("Could not retrieve Data Store Mode from VP/MS model");
		} catch (Throwable th) {
			throw new NbaBaseException("Problem retrieving Data Store Mode from VP/MS", th);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
				//SPR3362 code deleted
			} catch (Throwable th) {
			    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //SPR3362
			}
		}
	}
}
