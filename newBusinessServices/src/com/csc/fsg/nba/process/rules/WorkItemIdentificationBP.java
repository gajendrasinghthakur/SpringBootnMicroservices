package com.csc.fsg.nba.process.rules;

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
 * *******************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Result;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.dataobject.nba.auxiliary.UwReqTypesReceived;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * Class Description.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA136</td><td>Version 6</td><td>In Tray and Search Rewrite</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-17</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 17</td></tr>
 * <tr><td>NBA227</td><td>Version 8</td><td>Selection List of Images to Display</td></tr>
 * <tr><td>NBA229</td><td>Version 8</td><td>nbA Work List and Search Results Enhancement</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class WorkItemIdentificationBP extends NewBusinessAccelBP {
	protected NbaLogger logger = null;

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			if (input instanceof NbaDst) {
				result.addResult(getWorkItemIdentification((NbaDst) input));
			} else if (input instanceof NbaSearchResultVO) {
				result.addResult(getWorkItemIdentification((NbaSearchResultVO) input));
			//begin NBA208-17
			} else if (input instanceof NbaDst[]) {
				result.addResult(getWorkItemIdentification((NbaDst[]) input));
			} else if (input instanceof NbaSearchResultVO[]) {
				result.addResult(getWorkItemIdentification((NbaSearchResultVO[]) input));
			} else if (input instanceof List) { // NBA229
				result.addResult(getWorkItemIdentification((List) input)); // NBA229
			} // NBA229
			//end NBA208-17
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}

	protected String getWorkItemIdentification(NbaDst nbaDst) throws NbaDataAccessException {
		return getWorkItemIdentification(nbaDst.getNbaLob(), nbaDst.getStatus(), nbaDst.getID()); //APSL5055
	}

	protected String getWorkItemIdentification(NbaSearchResultVO searchResult) throws NbaDataAccessException {
		return getWorkItemIdentification(searchResult.getNbaLob(), searchResult.getStatus(), getWorkItemID(searchResult)); //APSL5055
	}

	protected String getWorkItemIdentification(NbaLob lob, String status, String workItemID) throws NbaDataAccessException { // APSL5055
		StringBuffer strBuf = new StringBuffer();
		if ((lob != null) && (status != null)) {
			String longDescription = lob.getRouteReason();
			if (longDescription != null) {
				strBuf.append(longDescription);
			} else {
				NbaTableAccessor nta = new NbaTableAccessor();
				strBuf.append(nta.getStatusTranslationString(lob.getWorkType(), status));
			}
			try {
				String translatedLobValue = processWorkItemIdentificationModel(lob, workItemID);  //NBA213, APSL5055
				if (null != translatedLobValue && translatedLobValue.trim().length() > 0) {
					strBuf.append(translatedLobValue);
				}
			} catch (NbaBaseException e) {
				//The exception should only be logged				
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Unable to get lob translation string " + e);
				}
			}
		}
		return strBuf.toString();
	}

	/**
	 * 
	 * @param nbaDst
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA208-17 new method
	protected Map getWorkItemIdentification(NbaDst nbaDst[]) throws NbaBaseException {
	    Map translationMap = new HashMap();
		NbaVpmsAdaptor vpmsProxy = null; 
		try {
		    NbaTableAccessor nta = new NbaTableAccessor();
			vpmsProxy = new NbaVpmsAdaptor(NbaVpmsAdaptor.WORKITEMIDENTIFICATION); 
		    for (int i=0;i<nbaDst.length;i++) {
				StringBuffer strBuf = new StringBuffer();
				NbaLob lob = nbaDst[i].getNbaLob();
				String status = nbaDst[i].getStatus();
				if ((lob != null) && (status != null)) {
					String longDescription = lob.getRouteReason();
					if (longDescription != null) {
						strBuf.append(longDescription);
					} else {
						strBuf.append(nta.getStatusTranslationString(lob.getWorkType(), status));
					}
					try {
						String translatedLobValue = processWorkItemIdentificationModel(lob, vpmsProxy, nbaDst[i].getID());//APSL5055
						if (null != translatedLobValue && translatedLobValue.trim().length() > 0) {
							strBuf.append(translatedLobValue);
						}
					} catch (NbaBaseException e) {
						//The exception should only be logged				
						if (getLogger().isDebugEnabled()) {
							getLogger().logDebug("Unable to get lob translation string " + e);
						}
					}
				}
				translationMap.put(nbaDst[i].getID(),strBuf.toString());
		    }
		} finally {
			try {
				if (vpmsProxy != null) {					
					vpmsProxy.remove();
					vpmsProxy = null;
				}
			} catch (RemoteException e) {
				throw new NbaBaseException("Remote Exception occured while processing VP/MS request", e);
			} catch (NbaVpmsException e) {
			    throw new NbaBaseException("Remote Exception occured while processing VP/MS request", e);            
			}
		}
		return translationMap;
	}
	/**
	 * 
	 * @param searchResult
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA208-17 New Method
	protected Map getWorkItemIdentification(NbaSearchResultVO searchResult[]) throws NbaBaseException {
	    Map translationMap = new HashMap();
		NbaVpmsAdaptor vpmsProxy = null; 
		try {
		    NbaTableAccessor nta = new NbaTableAccessor();
			vpmsProxy = new NbaVpmsAdaptor(NbaVpmsAdaptor.WORKITEMIDENTIFICATION); 
		    for (int i=0;i<searchResult.length;i++) {
				StringBuffer strBuf = new StringBuffer();
				NbaLob lob = searchResult[i].getNbaLob();
				String status = searchResult[i].getStatus();
				if ((lob != null) && (status != null)) {
					String longDescription = lob.getRouteReason();
					if (longDescription != null) {
						strBuf.append(longDescription);
					} else {
						strBuf.append(nta.getStatusTranslationString(lob.getWorkType(), status));
					}
					try {
						String translatedLobValue = processWorkItemIdentificationModel(lob, vpmsProxy, getWorkItemID(searchResult[i]));// APSL5055
						if (null != translatedLobValue && translatedLobValue.trim().length() > 0) {
							strBuf.append(translatedLobValue);
						}
					} catch (NbaBaseException e) {
						//The exception should only be logged				
						if (getLogger().isDebugEnabled()) {
							getLogger().logDebug("Unable to get lob translation string " + e);
						}
					}
				}
				translationMap.put(searchResult[i].getWorkItemID(),strBuf.toString());
		    }
		} finally {
			try {
				if (vpmsProxy != null) {					
					vpmsProxy.remove();
					vpmsProxy = null;
				}
			} catch (RemoteException e) {
				throw new NbaBaseException("Remote Exception occured while processing VP/MS request", e);
			} catch (NbaVpmsException e) {
			    throw new NbaBaseException("Remote Exception occured while processing VP/MS request", e);            
			}
		}
		return translationMap;
	}

	/**
	 * Calls the WorkItemIdentification VP/MS model and returns the concatenated string 
	 * with all the translated lob values.
	 * @param  lobData nba Lob data
	 * @return concatenated string with all the translated lob values
	 * @throws NbaBaseException
	 */
	public String processWorkItemIdentificationModel(NbaLob lobData, String workItemID) throws NbaBaseException {
		//begin NBA208-17		
		NbaVpmsAdaptor vpmsProxy = null;		
		try {
			vpmsProxy = new NbaVpmsAdaptor(NbaVpmsAdaptor.WORKITEMIDENTIFICATION);
			return processWorkItemIdentificationModel(lobData,vpmsProxy, workItemID); // APSL5055 
		} finally {
			try {
				if (vpmsProxy != null) {					
					vpmsProxy.remove();
					vpmsProxy = null;
				}
			} catch (RemoteException e) {
			    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		//end NBA208-17
	}
	/**
	 * Calls the WorkItemIdentification VP/MS model and returns the concatenated string 
	 * with all the translated lob values.
	 * @param  lobData nba Lob data
	 * @param  vpmsProxy For WorkItemIdentification model
	 * @return concatenated string with all the translated lob values
	 * @throws NbaBaseException
	 */
	//NBA208-17 New Method
	public String processWorkItemIdentificationModel(NbaLob lobData, NbaVpmsAdaptor vpmsProxy, String workItemID) throws NbaBaseException {
		StringBuffer resultString = new StringBuffer();
		NbaOinkDataAccess data = new NbaOinkDataAccess();
		data.setLobSource(lobData);
		try {
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_TOOLTIPSTRING); 
			vpmsProxy.setOinkSurrogate(data);			
			// Begin NBA229
			Map deOinkMap = new HashMap();
			if (NbaConstants.A_WT_APPLICATION.equals(lobData.getWorkType()) 
					|| (NbaConstants.A_WT_AGGREGATE_CONTRACT.equals(lobData.getWorkType()) && NbaConstants.A_QUEUE_AGGREGATE_CONTRACT_UW.equals(lobData.getAggrReference()))) {//NBLXA-1326 - Defect-NBLXA-1438
				//begin NBA331.1, APSL5055
				int seq = 0;
				Result result = callService(ServiceCatalog.UW_REQTYPES_RECEIVED_DISASSEMBLER, workItemID);
				if (!result.hasErrors()) {
					result = invoke(ServiceCatalog.RETRIEVE_UW_REQTYPES_RECEIVED, result.getData());
					if (!result.hasErrors()) {
						UwReqTypesReceived reqTypesRcvd = (UwReqTypesReceived) result.getFirst();
						if (reqTypesRcvd != null) {
							for (String reqType : reqTypesRcvd.getListOfReqTypes()) {
								if (seq == 0) {
									deOinkMap.put(NbaVpmsAdaptor.A_REQ_TYPE_LOB, reqType);
								} else {
									StringBuilder sb = new StringBuilder();
									sb.append(NbaVpmsAdaptor.A_REQ_TYPE_LOB).append("[").append(seq).append("]");
									deOinkMap.put(sb.toString(), reqType);
								}
								seq++;
							}
						}
					}
				}
                deOinkMap.put(NbaVpmsAdaptor.A_REQ_COUNT, String.valueOf(seq));
//				int seq = 1;
//				while (lobData.getReqTypeAt(seq) != 0) {
//					if (seq == 1) {
//						deOinkMap.put(NbaVpmsAdaptor.A_REQ_TYPE_LOB, String.valueOf(lobData.getReqTypeAt(seq)));
//					} else {
//						deOinkMap.put(NbaVpmsAdaptor.A_REQ_TYPE_LOB + "[" + (seq - 1) + "]", String.valueOf(lobData.getReqTypeAt(seq)));
//					}
//					seq++;
//				}
//				deOinkMap.put(NbaVpmsAdaptor.A_REQ_COUNT, String.valueOf(seq - 1));
              //end NBA331.1, APSL5055
			} else if(NbaConstants.A_WT_AGGREGATE_CONTRACT.equals(lobData.getWorkType())) {//NBLXA-1326 - Defect-NBLXA-1438
				deOinkMap.put(NbaVpmsAdaptor.A_REQ_COUNT, String.valueOf(0));
			}
			vpmsProxy.setSkipAttributesMap(deOinkMap); 
			// End NBA229
			VpmsComputeResult result = vpmsProxy.getResults();
			if (null != result && result.getReturnCode() == 0) {
				String[] tokens = result.getResult().trim().split(NbaVpmsConstants.VPMS_DELIMITER[1]);				
				for(int i=0;i<tokens.length;i++){
					resultString.append(getTranslatedValue(tokens[i], lobData, true));	//NBA227			
				}
			}
			return resultString.toString();
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} 
	}
	/**
	 * Parses the result string returned from the VPMS model for one lob and returns the  
	 * concatenated string containing the prefix text and the translated value.	 
	 * @param  aToken: the delimited string returned from vpms mode for one lob value
	 * @param  lobData Nba Lob data.
	 * @return concatenated string containing prefix text and translated value for one lob
	 * @throws NbaBaseException
	 */	
	//NBA213 New Method
//	NBA213 New Method
	//NBA227 Signature of the method changed
	public String getTranslatedValue(String aToken, NbaLob lobData, boolean prefix) throws NbaBaseException {
		StringBuffer resultString = new StringBuffer();
		String prefixText = null;
		String lobValue = null;
		String translationTable = null;
		String translatedValue = null;
		if(null != aToken ){
			String[] bToken = aToken.split(NbaVpmsConstants.VPMS_DELIMITER[0]);
			if(bToken !=null){
				if(bToken.length > 0){
					prefixText = bToken[0];
				}
				if(bToken.length > 1){
					lobValue = bToken[1];
				}
				if(bToken.length > 2){
					translationTable = bToken[2];
				}
			}
		}
		if (null != lobValue && lobValue.trim().length() > 0) {
			if (null != translationTable && translationTable.trim().length() > 0) {
				NbaTableAccessor tableAccessor = new NbaTableAccessor();
				HashMap keys = tableAccessor.setupTableMap(lobData);
				if (keys.get(NbaTableAccessConstants.C_SYSTEM_ID) != null
					&& ((String) keys.get(NbaTableAccessConstants.C_SYSTEM_ID)).equals(NbaTableAccessConstants.WILDCARD)) {
						keys.put(NbaTableAccessConstants.C_SYSTEM_ID, getSystemIdFromDetermineAdminSystemModel(lobData));
					}				
				NbaTableData tableData = tableAccessor.getDataForOlifeValue(keys, translationTable, lobValue);
				if (null != tableData) {
					translatedValue = tableData.text();
					if (null != translatedValue && translatedValue.trim().length() > 0) {
						if (prefix) { //NBA227
							resultString.append(prefixText);
							resultString.append(translatedValue);
							//	begin NBA227
						} else {
							resultString.append(translatedValue);
							resultString.append(prefixText);
						} 
						// end NBA227
						return resultString.toString();
					}
				}
			}
			resultString.append(prefixText);
			resultString.append(lobValue.trim());
		}
		return resultString.toString();
	}
	/**
	 * Gets the backend system value from DetermineAdminSystem VP/MS model.   
	 * @param  lobData nba Lob data
	 * @return backend system id
	 * @throws NbaBaseException  
	 */
	//NBA213 New Method
	public String getSystemIdFromDetermineAdminSystemModel(NbaLob lobData) throws NbaBaseException {
		String backEndSystemID = null;
		NbaOinkDataAccess data = new NbaOinkDataAccess();
		data.setLobSource(lobData);
		NbaVpmsAdaptor vpmsProxy = null;
		try {
            vpmsProxy = new NbaVpmsAdaptor(data, NbaVpmsConstants.DETERMINEADMINSYSTEM);
            vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_ADMINISTRATION_SYSTEM);
            NbaVpmsResultsData result = new NbaVpmsResultsData(vpmsProxy.getResults());
            if (null != result) {
                backEndSystemID = (String) result.resultsData.get(0);
            }
        } catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
        } finally {
			try {
			    if (vpmsProxy != null) {
                    vpmsProxy.remove();
                }
			} catch (RemoteException e) {
			    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		return backEndSystemID;
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	/**
	 * process list of workitem object and gets the translated value for each workitem from the vpms model
	 * @param nbaDst
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA229 New Method
	protected Map getWorkItemIdentification(List list) throws NbaBaseException {
	    Map translationMap = new HashMap();
		NbaVpmsAdaptor vpmsProxy = null; 
		try {
		    NbaTableAccessor nta = new NbaTableAccessor();
			vpmsProxy = new NbaVpmsAdaptor(NbaVpmsAdaptor.WORKITEMIDENTIFICATION); 
			int size = list.size();
			WorkItem work;
			NbaLob lob;
			String longDescription;
			String status;
			String translatedLobValue;
		    for (int i=0;i<size;i++) {
				StringBuffer strBuf = new StringBuffer();
				work = (WorkItem)list.get(i);
				lob = new NbaLob(work.getLobData());
				lob.setWorkType(work.getWorkType());
				lob.setBusinessArea(work.getBusinessArea());
				lob.setStatus(work.getStatus());
				status = work.getStatus();
				if ((lob != null) && (status != null)) {
					longDescription = lob.getRouteReason();
					if (longDescription != null) {
						strBuf.append(longDescription);
					} else {
						strBuf.append(nta.getStatusTranslationString(lob.getWorkType(), status));
					}
					try {
						//QCDUG Begin
						if ("NBAGGCNT".equalsIgnoreCase(work.getWorkType())
								&& !NbaConstants.A_QUEUE_AGGREGATE_CONTRACT_UW.equals(lob.getAggrReference())) { //NBLXA-1326 - Defect-NBLXA-1438
							translatedLobValue = null;    
						} else {   
							translatedLobValue = processWorkItemIdentificationModel(lob, vpmsProxy, work.getItemID()); //NBA331.1, APSL5055
						}
						//QCDUG End
						if (null != translatedLobValue && translatedLobValue.trim().length() > 0) {
							strBuf.append(translatedLobValue);
						}
					} catch (NbaBaseException e) {
						//The exception should only be logged				
						if (getLogger().isErrorEnabled()) {
							getLogger().logError("Unable to get lob translation string " + e);
						}
					}
				}
				translationMap.put(work.getItemID(),strBuf.toString());
		    }
		} finally {
			try {
				if (vpmsProxy != null) {					
					vpmsProxy.remove();
					vpmsProxy = null;
				}
			} catch (RemoteException e) {
				throw new NbaBaseException("Remote Exception occured while processing VP/MS request", e);
			} catch (NbaVpmsException e) {
			    throw new NbaBaseException("Remote Exception occured while processing VP/MS request", e);            
			}
		}
		return translationMap;
	}
	
	
    /**
     * Returns the work item ID from the NbaSearchResultVO.  If it is null, then it
     * will check for a work item provided in the search result.
     * @param searchResult
     * @return
     */
	//NBA331.1, APSL5055 New Method
    protected String getWorkItemID(NbaSearchResultVO searchResult) {
		String workItemID = null;
		if (searchResult.getWorkItemID() != null) {
			workItemID = searchResult.getWorkItemID();
		} else if (searchResult.getWorkItem() != null) {
			workItemID = searchResult.getWorkItem().getItemID();
		}
		return workItemID;
	}
}
