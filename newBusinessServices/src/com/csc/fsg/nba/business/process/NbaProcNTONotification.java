package com.csc.fsg.nba.business.process;

/*
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
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.csc.fsg.nba.correspondence.NbaCorrespondenceEvents;
import com.csc.fsg.nba.database.NbaAutoClosureAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAutoClosureContract;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * NbaProcNTONotification is the class that processes nbAccelerator cases that needs to 
 * be send notification to agent before auto closure date. It reviews the Notification date  and associated with a case to 
 * determine if the case needs to be send  Notification.  
 * <p>The NbaProcNTONotification class extends the NbaAutomatedProcess class.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SR678531-APSL3210</td><td>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @see NbaAutomatedProcess
 */


public class NbaProcNTONotification extends NbaAutomatedProcess {
	protected static final String CASE = "C";

	protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";
	
	protected static final long Day_diff = 5;

	
	/**
	 * This constructor calls the superclass constructor which will set
	 * the appropriate statues for the process.
	 */
	public NbaProcNTONotification() {
		super();
	}

	/**
	 * This method drives the NTO Notification process.
	 * <P>After obtaining a list of contracts that needs NTO Notification
	 * to be send from the Database, the process search that contract in the AWD
	 * and tries to get lock on the workitem. If it is able to get the
	 * lock it check Pending Requirement . 
	 * @param user the user for whom the work was retrieved, in this case A2NTONOTF.
	 * @param work the AWD case to be processed
	 * @return NbaAutomatedProcessResult containing the results of the process
	 * @throws NbaBaseException 
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		NbaDst matchingCase = null;
		ArrayList contracts = new ArrayList();
		List producerContractList = new ArrayList(); 
		boolean continueNTONotification = false; 
		NbaAutoClosureContract autoClosureContract = new NbaAutoClosureContract();
		setUser(user);
		int batchSize = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
		NbaConfigurationConstants.NTO_NOTIFICSATION_BATCH_SIZE));
		int ntoCount = 0; 
		try {
			getLogger().logDebug("Checking contracts for which Procuder Notification is required.");
			contracts = NbaAutoClosureAccessor.selectProducerNotificationCases();
			
			for (int i = 0; i < contracts.size(); i++) {
				if (i >= (batchSize)) { //If counter reaches batch size
					getLogger().logDebug("Execution of Batch Ends");
					continueNTONotification = true; 
					break;
				}
				autoClosureContract = (NbaAutoClosureContract) contracts.get(i);
				if (producerContractList.contains(autoClosureContract.getContractNumber())) {
					throw new NbaBaseException("Duplicate contract number found!"); 
				}
				producerContractList.add(autoClosureContract.getContractNumber());
				
				try {
					matchingCase = retrieveMatchingCase(user, autoClosureContract.getWorkItemId());
					setWork(matchingCase);
				} catch (NbaBaseException lockedExp) {
					getLogger().logError("Producer Notification: Unable to get lock on : " + autoClosureContract.getContractNumber()); //PERF-APSL308
					continue;
				} catch (Exception exp) {
					getLogger().logError("Exeception occurred while retrieving contract -" + autoClosureContract.getContractNumber()); //PERF-APSL308
					getLogger().logException(exp);
					continue;
				}
				try {
					setNbaTxLife(doHoldingInquiry());
				} catch (NbaBaseException exp) {
					getLogger().logException(exp);
					getLogger().logError("Work Item ID:" + autoClosureContract.getWorkItemId());
				}
				evaluateResponse();
				if (null == getNbaTxLife()) {
					continue;
				}
				List pendReqList = NbaUtils.getPendingRequirementList(getNbaTxLife().getPolicy());
				if (pendReqList.size() > 0 && isContractPrintDone(autoClosureContract.getContractNumber())) {//QC15613/APSL4335
					getLogger().logDebug("Generating Procuder Notification for contract : " + autoClosureContract.getContractNumber());
					NbaCorrespondenceEvents events = new NbaCorrespondenceEvents(user);
					events.validateAwdEvent(matchingCase);
					events.refreshNbaDst(matchingCase);
					events.createWorkItems();
					getLogger().logDebug(
							"Procuder Notification Correspondence letter generated for contract : " + autoClosureContract.getContractNumber());
					
					/* If the isUpdateNeeded method return true then resetting
					   Producer notification date to 5 day earlier to Closure date 
					   else update Notification indictator to  1 */
					
					if (isUpdateNeeded(autoClosureContract)) {
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(autoClosureContract.getClosureDate());
						calendar.add(Calendar.DAY_OF_MONTH, -5);
						autoClosureContract.setProducerNotifyDate(calendar.getTime());
						NbaAutoClosureAccessor.setProducerNotifyDate(autoClosureContract);
					} else {
						NbaAutoClosureAccessor.setProducerNotifyIndicator(autoClosureContract); 
					}
				}
				try {
					unlockWork(matchingCase);
				} catch (NbaBaseException nbe) {
					getLogger().logError("Unable to unlock work for -" + autoClosureContract.getContractNumber()); //PERF-APSL308
				}
				// Start APSL3754
				try {
					NbaAutoClosureAccessor.setNTOProcessedDate(autoClosureContract.getContractNumber()); 
				} catch (NbaBaseException nbe) {
					nbe.forceFatalExceptionType(); 
					throw nbe; 
				}
				// End  APSL3754
				ntoCount++; 
			}
			
		} catch (NbaBaseException nbe) {
			getLogger().logError(
					"Exception occurred while generating Producer Notify Correspondence for contract -" + autoClosureContract.getContractNumber()
							+ " : " + NbaUtils.getStackTrace(nbe));
			nbe.forceFatalExceptionType();
			throw nbe;
		} catch (Throwable e) {
			getLogger().logError(
					"Exception occurred while generating Producer Notify Correspondence for contract -" + autoClosureContract.getContractNumber()
							+ " : " + NbaUtils.getStackTrace(e));
			NbaBaseException nbe = new NbaBaseException(e.getMessage());
			nbe.forceFatalExceptionType();
			throw nbe;
		}
		getLogger().logWarn("NTO Count: " + ntoCount);
		getLogger().logDebug("Execution of NTO Notification Process Ends");
		if (continueNTONotification) {
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "");
		}
		return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", "");
	}

	/**
	 * This method retrieves the matching case by the workitemID.
	 * Each case is retrieved and locked. If unable to retrieve with lock,
	 * this work item will be suspended for a brief period of time due to the auto
	 * suspend flag being set.
	 * 
	 * @param searchResults
	 *            the results of the previous AWD lookup
	 * @throws NbaBaseException
	 *             NbaLockException
	 */
	public NbaDst retrieveMatchingCase(NbaUserVO user, String workItemID) throws NbaBaseException {
		NbaDst aWorkItem = null;
		if (workItemID != null) {
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(workItemID, true);
			//Code Deleted - Performance changes of SR534322 Retrofit 
			retOpt.setLockWorkItem();
			//Code Deleted - Performance changes of SR534322 Retrofit
			aWorkItem = retrieveWorkItem(user, retOpt);
		}
		return aWorkItem;
	}

	
	//Overloading parent method
	public void handleHostResponse(NbaTXLife aTXLifeResponse, boolean addAddComments) {
		if (aTXLifeResponse != null && aTXLifeResponse.isTransactionError()) {//NBA094
			setNbaTxLife(null);
			getLogger().logError("Error retrieving TxLife from database.");
		}
	}

	
	/**
	 * This method calculate the Difference between closure date and current date. 
	 * If the difference is greater then 5 day then it return true.
	 * 
	 * @param NbaAutoClosureContract
	 *            the Object of AutoClosureCOntract.
	 */
	
	private boolean isUpdateNeeded(NbaAutoClosureContract autoClosuerContract) {
		Date currentdate = new Date();
		if (NbaUtils.getDaysDiff(autoClosuerContract.getClosureDate(),currentdate ) > Day_diff) {
			return true;
		}
		return false;

	}
	
	//Begin QC15613/APSL4335
	protected boolean isContractPrintDone(String polNumber) throws NbaBaseException {
		boolean printDone = false;
		NbaSearchVO searchPrintVO = searchWI(NbaConstants.A_WT_CONT_PRINT_EXTRACT, polNumber);
		if (searchPrintVO != null && searchPrintVO.getSearchResults() != null && !searchPrintVO.getSearchResults().isEmpty()) {
			List searchResultList = searchPrintVO.getSearchResults();
			for (int i = 0; i < searchResultList.size(); i++) {
				NbaSearchResultVO searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
				if ((searchResultVo.getQueue().equalsIgnoreCase(END_QUEUE) || searchResultVo.getQueue().equalsIgnoreCase(A_QUEUE_POST_ISSUE))) {
					printDone = true;
					break;
				}
			}
		} 
		return printDone;
	}

	protected NbaSearchVO searchWI(String workType, String policyNumber) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName("NbaSearchResultVO");
		searchVO.setWorkType(workType);
		searchVO.setContractNumber(policyNumber);
		searchVO = lookupWork(getUser(), searchVO);
		return searchVO;
	}
	//End QC15613/APSL4335

}
