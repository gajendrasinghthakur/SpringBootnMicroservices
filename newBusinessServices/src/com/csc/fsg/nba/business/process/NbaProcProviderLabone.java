package com.csc.fsg.nba.business.process;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.provideradapter.AxaEibProviderAdapter;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapter;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapterFacade;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;

/**
 * <code>NbaProcProviderEmsi</code> handles communications between
 * nbAccelerator and LabOne. It extends the NbaProcProviderCommunications class,
 * which drives the process, and supplies LabOne specific functionality.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>ACORD 121/1122 General Requirement Request Migration</td></tr>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 2</td><td>Provider Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaProcProviderLabone extends NbaProcProviderCommunications {
	/**
	 * NbaProcProviderLabone constructor comment.
	 */

	public NbaProcProviderLabone() {
		super();
	}
	
	// NBLXA-2072 New Method
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// Initialization
		if (!initialize(user, work)) {
			return statusProcessFailed();
		}
		// Begin NBLXA-2072
		if ((work.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER) && determineLNRCSuspension()) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "SUSPENDED", "SUSPENDED"));
			return getResult();
		}
		// End NBLXA-2072
		NbaProviderAdapterFacade adapter = new NbaProviderAdapterFacade(work, user);// ACN014
		setProvider(adapter.getProvider());

		boolean routeMVR = false;
		if (work.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_MVRPT && work.getNbaLob().getReqVendor() != null 
				&& PROVIDER_LEXISNEXIS.equalsIgnoreCase(work.getNbaLob().getReqVendor())) {
			NbaTXLife txLife = getNbaTxLife();
			String reqUniqueID = work.getNbaLob().getReqUniqueID();
			if (!NbaUtils.isBlankOrNull(reqUniqueID) && !NbaUtils.isBlankOrNull(txLife) ) {
				RequirementInfo reqInfoMVR = txLife.getRequirementInfo(reqUniqueID);
				String partyId = reqInfoMVR.getAppliesToPartyID();
				if (!NbaUtils.isBlankOrNull(partyId)) {
					List reqInfos = txLife.getRequirementInfoList(partyId, NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER);
					if (isOutstandingRiskClassifierPresent(reqInfos)) {
						routeMVR = true;
					}
				}
			}
		}
		
		if (routeMVR && NbaUtils.isBlankOrNull(work.getNbaLob().getSuspensionCount())) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "SUCCESSFUL", getPassStatus()));
			changeStatus(getResult().getStatus());
			work.getNbaLob().setSuspensionCount(NbaConstants.LOB_SUSPENSION_COUNT_01);
			work.setUpdate();
			int suspendDays = getSuspendDays();
			if (suspendDays <= 0) {
				doUpdateWorkItem();
			} else {
				suspendTransaction(suspendDays);
			}
		} else {
			super.executeProcess(user, work);
		}

		return getResult();
	}

	/**
	 * NBLXA-2072 New Method
	 * returns true if there is any ourstanding RiskClassifier Requirement present on the case
	 * 
	 * @param work
	 * @return
	 */
	public boolean isOutstandingRiskClassifierPresent(List requirementInfoList) {
		boolean present = false;

		if (!requirementInfoList.isEmpty()) {
			Iterator reqInfoIterator = requirementInfoList.iterator();
			while (reqInfoIterator.hasNext()) {
				RequirementInfo currentReqInfo = (RequirementInfo) reqInfoIterator.next();
				if (currentReqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_ORDER
						|| currentReqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_SUBMITTED) {
					present = true;
					break;
				}
			}
		}

		return present;
	}

	
	/**
	 * Answers the result of evaluating the response from the FTP process.
	 * 
	 * @param response
	 *            the response from the sendMessageToProvider method
	 * @return <code>true</code> if the result is not null or empty;
	 *         otherwise, <code>false</code> is returned
	 * @throws NbaBaseException
	 */
	public boolean evaluateResponse(String response) throws NbaBaseException {
		//AXAL3.7.31 - rewrote method to handle transaction errors
	    boolean success = false;
	    if (response != null && response.trim().length() > 0) {
	        NbaTXLife life;
	        try {
	            life = new NbaTXLife(response);
	        } catch (Exception e) {
	            throw new NbaBaseException(NbaBaseException.INVALID_RESPONSE, e);
	        }
	        if (isTransactionError(life)) {
	            handleProviderWebServiceFailure(life);
	        } else {
	            success = true;
	        }
	    }
	    return success;
	}

	/**
	 * For LabOne processing, this will sets the URL (path) from the
	 * NbaConfiguration file.
	 * 
	 * @throws NbaBaseException
	 */
	public void initializeTarget() throws NbaBaseException {
		setTarget(getProvider().getUrl());
	}

	//AXAL3.7.31 New Method
	public Object doProviderSpecificProcessing(Object data)
			throws NbaBaseException {
		List alist = new ArrayList();
		alist.add(removeNameSpace((String) data));
		alist.add(getProvider().getName());
		alist.add(NbaUtils.XSL_REQUIREMENT_REQUEST);
		AxaEibProviderAdapter eibAdapter = new AxaEibProviderAdapter();
		Map map = eibAdapter.convertXmlToProviderFormat(alist);
		String outPutXml = (String) map.get(NbaProviderAdapter.TRANSACTION);
		// Moved namespace definitions to the XSL transform
		return outPutXml;
	}
	
	// NBLXA-2072 New Method
	protected boolean determineLNRCSuspension() throws NbaBaseException {
		String reqUniqueID = work.getNbaLob().getReqUniqueID();
		if (!NbaUtils.isBlankOrNull(reqUniqueID)) {
			RequirementInfo lnrcReqInfo = getNbaTxLife().getRequirementInfo(reqUniqueID);
			String partyId = lnrcReqInfo.getAppliesToPartyID();
			if (!NbaUtils.isBlankOrNull(partyId)) {
				List<RequirementInfo> reqInfos = getNbaTxLife().getRequirementInfoList(partyId, NbaOliConstants.OLI_REQCODE_MVRPT);
				for (RequirementInfo aReqInfo : reqInfos) {
					if (NbaUtils.isRequirementOutstanding(aReqInfo.getReqStatus())) {
						NbaSearchVO searchCritVO = new NbaSearchVO();
						searchCritVO.setResultClassName("NbaSearchResultVO");
						searchCritVO.setWorkType(A_WT_REQUIREMENT);
						searchCritVO.setQueue("N2ORDERD");
						NbaLob searchCritLOB = new NbaLob();
						searchCritLOB.setReqUniqueID(aReqInfo.getRequirementInfoUniqueID());
						searchCritVO.setNbaLob(searchCritLOB);
						NbaSearchVO searchResultVO = lookupWork(getUser(), searchCritVO);
						List searchResults = searchResultVO.getSearchResults();
						if (searchResults == null || searchResults.size() <= 0) {
							NbaSuspendVO suspendVO = new NbaSuspendVO();
							suspendVO.setTransactionID(getWork().getID());
							Calendar aCalendar = new GregorianCalendar();
							aCalendar.setTime(new Date());
							aCalendar.add(Calendar.HOUR, 1); // not thru VP/MS or configuration, in rush for Feb'19 release
							suspendVO.setActivationDate(aCalendar.getTime());
							addComment("Suspended for outstanding MVR not in AP Ordered.");
							updateWork(getUser(), getWork());
							suspendWork(getUser(), suspendVO);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
}
