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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.SystemAccess;
import com.csc.fs.accel.ui.AxaStatusDefinitionLoader;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.database.NbaAutoClosureAccessor;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAutoClosureContract;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.statusDefinitions.Status;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.TentativeDisp;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;

/**
 * NbaProcClosureCheck is the class that processes nbAccelerator cases that needs to 
 * be Automatically closed. It reviews the closureDate and associated with a case to 
 * determine if the case needs to be closed.  It invokes the VP/MS model to check 
 * if the case is in the right Queue and then send the case to the next Queue.
 * <p>The NbaProcClosureCheck class extends the NbaAutomatedProcess class.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA254</td><td>Version 8</td><td>Automatic Closure and Refund of CWA</td></tr>
 * <tr><td>ALPC96</td><td>AXA Life Phase 1</td><td>xPression OutBound Email</td></tr>
 * <tr><td>PERF-APSL308</td><td>AXA Life Phase 1</td><td>Auto Closure Rewrite</td></tr>
 * <tr><td>SR534322 Retrofit</td><td>Discretionary</td><td>Reopen from NTO</td></tr>
 * <tr><td>APSL3016-NBA283(Retrofit)</td><td>Discretionary</td><td>AWD10 Upgrade</td></tr>
 *  <tr><td>SR678531-APSL3210</td><td>Discretionary</td><td>NTO Notification Changes</td></tr>
 *  <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @see NbaAutomatedProcess
 */

public class NbaProcClosureCheck extends NbaAutomatedProcess {
	protected static final String CASE = "C";

	protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";

	private static final String NOT_LOGGED_ON_ERROR = "SYS0111";// APSL3016-NBA283(Retrofit)

	/**
	 * This constructor calls the superclass constructor which will set
	 * the appropriate statues for the process.
	 */
	public NbaProcClosureCheck() {
		super();
	}

	/**
	 * This method drives the closure check process.
	 * <P>After obtaining a list of contracts that needs auto closure
	 * from the Database, the process search that contract in the AWD
	 * and tries to get lock on the workitem. If it is able to get the
	 * lock it check for the correct Queue. The contract needs to be in
	 * either Underwriter Hold queue or Application Hold queue. If the 
	 * case is not in one of these queues at the time nbA determines 
	 * their closure date has been reached, then the Closure Check 
	 * automated process will bypass automatic negative disposition 
	 * processing of the contract until its status is updated to route
	 * it to one of those queues.  If the case resides in one of those 
	 * queues, then the automated process will retrieve the Application 
	 * work item, retrieve a new status for the work item from the 
	 * AutoProcessStatus VP/MS model and change the status to route the 
	 * work item to the Final Disposition automated process.
	 * @param user the user for whom the work was retrieved, in this case APCLSCHK.
	 * @param work the AWD case to be processed
	 * @return NbaAutomatedProcessResult containing the results of the process
	 * @throws NbaBaseException 
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException{
		NbaDst matchingCase = null;
		NbaProcessStatusProvider statusProvider = null;
		ArrayList contracts = new ArrayList();
		ArrayList validStatusList = new ArrayList();
		setUser(user);
		int autoCloseCount = 0; //PERF-APSL308
		int batchSize = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
				NbaConfigurationConstants.AUTO_CLOSURE_BATCH_SIZE));
		boolean continueAutoClosure = false; //PERF-APSL308
		NbaAutoClosureContract autoClosureContract = new NbaAutoClosureContract();
		getLogger().logDebug("Execution of Auto Closure Check Process Started");
		try {
			contracts = NbaAutoClosureAccessor.selectAutoClosureCases();
		} catch (Exception exp) {
			getLogger().logError("Exeception occoured while retrieving list of contracts from Database -" + NbaUtils.getStackTrace(exp)); //PERF-APSL308
		}
		if (contracts.size() > 0) {
			validStatusList = getValidStatusList();

			for (int i = 0; i < contracts.size(); i++) {
				if (i >= (batchSize)) { //If counter reaches batch size
					getLogger().logDebug("Execution of Batch Ends");
					continueAutoClosure = true; //PERF-APSL308
					break;
				}
				autoClosureContract = (NbaAutoClosureContract) contracts.get(i);
				try {
					matchingCase = retrieveMatchingCase(user, autoClosureContract.getWorkItemId());
					setWork(matchingCase);
				} catch (NbaLockedException lockedExp) {
					getLogger().logError("Locking Exeception occurred while retrieving contract -" + autoClosureContract.getContractNumber());
					continue;
				} catch (Exception exp) {
					// begin APSL3016-NBA283(Retrofit)
					if (isNotLoggedOn(exp)) {
						// user has been automatically logged of workflow system because of inactivity.                        
						try {
							logon();
							user.setSessionKey(getUser().getSessionKey());
							setWork(retrieveMatchingCase(user, autoClosureContract.getWorkItemId())); //retry
						} catch (NbaLockedException lockedExp) {
							getLogger().logError("Locking Exeception occurred while retrieving contract -" + autoClosureContract.getContractNumber());
							continue;
						} catch (NbaBaseException exce) {
							getLogger().logFatal(
									"Exception occurred during execution of Auto Closure Check Process" + " : " + NbaUtils.getStackTrace(exce));
							exce.forceFatalExceptionType();
							throw exce;
						}
					} else {
						//end APSL3016-NBA283(Retrofit)
						getLogger().logError("Exeception occurred while retrieving contract -" + autoClosureContract.getContractNumber()); //PERF-APSL308
						getLogger().logException(exp);
						continue;
					}// APSL3016-NBA283(Retrofit)
				}
				if (validStatusList.contains(matchingCase.getStatus())) {
					getLogger().logDebug("Contract :" + autoClosureContract.getContractNumber() + "is getting Auto Closed");
					try {
						//Start SR534322
						//Code Deleted - Performance changes of SR534322 
						getWork().getNbaLob().setAutoClosureStat(matchingCase.getStatus());
						//Begin APSL2735 EIP
						if(getWork().getNbaLob().getCwaTotal() > 0){
							getWork().getNbaLob().setPaymentReview(NbaConstants.PAYMENT_NOTIF_REVIEW_REQUIRED_NOTDONE);
						}
						//End APSL2735 EIP
						//Code Deleted - Performance changes of SR534322 
						getWork().setUpdate();
						String status=getWork().getStatus();
						//End SR534322
						statusProvider = new NbaProcessStatusProvider(user, matchingCase);
						setStatusProvider(statusProvider);
						changeStatus(matchingCase, statusProvider.getPassStatus());
						//Start APSL4763
						try {
							setNbaTxLife(doHoldingInquiry());
						} catch (NbaBaseException exp) {
							getLogger().logException(exp);
							getLogger().logError("Work Item ID:" + autoClosureContract.getWorkItemId());
						}
						if (getNbaTxLife() != null) { // Begin NBLXA-1722
							if (getNbaTxLife().hasSevereValidationErrors() && !NbaUtils.getActivityByTypeCodeAndStatus(getNbaTxLife(),NbaOliConstants.OLI_ACTTYPE_CLOSURE_CHECK,status)) {
								NbaUtils.createUpdateClosureActivity(getNbaTxLife(), user.getUserID(), NbaOliConstants.OLI_ACTTYPE_CLOSURE_CHECK,status);
								addComment("Prevent process code exist on the case");
								createValErrorWorkItem();
								Status passStatus = AxaStatusDefinitionLoader.determinePassStatus("A2CLSCHK", null);
								matchingCase.setStatus(passStatus.getStatusCode());
								matchingCase.getNbaLob().setRouteReason(passStatus.getRoutingReason());
							} // End NBLXA-1722
							else{
							addAutoCommentForClosure(getNbaTxLife());// APSL4763
							createTentativeDisp();  // APSL5335
							}
							setContractAccess(UPDATE); // APSL5335
							doContractUpdate(getNbaTxLife()); // APSL5335
							NbaContractLock.removeLock(getWork(), user); // APSL5335
						}
						//End APSL4763
						doUpdateWorkItem();
						if (getWork().isSuspended()) {
							NbaSuspendVO suspendVO = new NbaSuspendVO();
							suspendVO.setCaseID(matchingCase.getID());
							suspendVO.setSystemName(getWork().getSystemName());      //APSL5055-NBA331
                            suspendVO.setRetrieveWorkItem(false);   //APSL5055-NBA331.1 It is already locked. 
							unsuspendWork(suspendVO);
						}
					} catch (Throwable e) { //PERF-APSL308
						getLogger().logError("Exeception occurred while auto closing contract -" + autoClosureContract.getContractNumber()); //PERF-APSL308
						getLogger().logException(e); //PERF-APSL308
					}
				}
				try {
					unlockWork(matchingCase);
				} catch (NbaBaseException nbe) {
					getLogger().logError("Unable to unlock work for -" + autoClosureContract.getContractNumber()); //PERF-APSL308
				}
				try {
					NbaAutoClosureAccessor.setProcessedDate(autoClosureContract.getContractNumber());//Set the Last processed date to todays date
				} catch (NbaBaseException nbe) {
					nbe.forceFatalExceptionType(); //error stop poller //PERF-APSL308
					throw nbe; //PERF-APSL308
				}
				autoCloseCount++; //PERF-APSL308
			}
		}
		
		getLogger().logWarn("Auto Closure Count: " + autoCloseCount);
		getLogger().logDebug("Execution of Auto Closure Check Process Ends");
		


	   /* Code to send NTO Notification has been moved to new process NbaProcNTONotification
	    * as Part of SR678531-APSL3210 */
		if (continueAutoClosure) {
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "");
		}
		//end PERF-APSL308
		return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", "");
		
		
	}
	/**
	 * This method adds Automated comments on the case. 
	 * Stating the reason as why the case got auto closed
	 * @param txLife
	 *            TxLife object of the existing case.
	 */
	// APSL4763 - New Method
	public void addAutoCommentForClosure(NbaTXLife txLife){
		 if (txLife != null && NbaUtils.isReqOutstanding(getNbaTxLife())) {
				addComment("Autoclosed - due to outstanding requirements");
			} else {
				addComment("Autoclosed - no decision on case ");
			}
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

	/**
	 * @return
	 * @throws NbaVpmsException
	 * @throws NbaVpmsException
	 */
	private ArrayList getValidStatusList() throws NbaVpmsException {
		NbaVpmsAdaptor vpmsProxy = null;
		ArrayList validStatusList = new ArrayList();
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.QUEUE_STATUS_CHECK);
			vpmsProxy.setVpmsEntryPoint(EP_GET_HOLD_STATUSES);
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult compResult = vpmsProxy.getResults();
			NbaStringTokenizer tokens = new NbaStringTokenizer(compResult.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			String aToken;
			while (tokens.hasMoreTokens()) {
				aToken = tokens.nextToken();
				validStatusList.add(aToken);
			}

		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("Closure Check Problem" + NbaVpmsException.VPMS_EXCEPTION, re);
		} catch (NbaBaseException e) {
			getLogger().logDebug(
					"Exeception occurred while getting valid hold statuses from QUEUE STATUS CHECK VPMS Model : " + NbaUtils.getStackTrace(e));
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}
		return validStatusList;
	}

	//Overloading parent method
	public void handleHostResponse(NbaTXLife aTXLifeResponse, boolean addAddComments) {
		if (aTXLifeResponse != null && aTXLifeResponse.isTransactionError()) {//NBA094
			setNbaTxLife(null);
			getLogger().logError("Error retrieving TxLife from database.");
		}
	}

	/**
	 * Logon the user. First logoff the user to clear any session data, then logon.
	 * @throws NbaBaseException
	 */
	//APSL3016-NBA283(Retrofit) New Method    
	protected void logon() throws NbaBaseException {
		SystemAccess sysAccess = (SystemAccess) ServiceLocator.lookup(SystemAccess.SERVICENAME);
		String system = null;
		try {
			system = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_SYSTEM);
		} catch (NbaBaseException exp) {
			getLogger().logException("Exception occured while reading external system from configuration : ", exp);
			throw exp;
		}
		sysAccess.logout(system);
		sysAccess.login(system);
	}

	/**
	 * Determine if the error is a not logged on condition
	 * @param whoops
	 * @return
	 */
	//APSL3016-NBA283(Retrofit) New Method 
	private boolean isNotLoggedOn(Exception whoops) {
		return whoops.getMessage() != null && whoops.getMessage().indexOf(NOT_LOGGED_ON_ERROR) > -1;
	}

	//	Methods Deleted - Performance changes of SR534322 Retrofit 
	
	// APSL5335 :: START Tantative disp will create when case is AutoClose
		private void createTentativeDisp()throws NbaBaseException {
		    TentativeDisp tentativeDisp = new TentativeDisp();
		    ApplicationInfoExtension appInfoExt = NbaUtils.getAppInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
		    List tentativeDispList = appInfoExt.getTentativeDisp();
			tentativeDisp.setDisposition(getDisposition());
			tentativeDisp.setDispLevel(getHighestDispositionLevel(tentativeDispList) + 1);
			tentativeDisp.setDispUndID(getUser().getUserID());
			tentativeDisp.setDispDate(getCurrentDateFromWorkflow(getUser()));
			tentativeDisp.setDispReason("");
			tentativeDisp.setUWRole(getUser().getUserID());
			tentativeDisp.setUWRoleLevel(getUnderwriterLevel(getUser().getUserID(), NbaTableAccessConstants.WILDCARD,
					NbaTableAccessConstants.WILDCARD));
			tentativeDisp.setActionAdd();
			
			appInfoExt.addTentativeDisp(tentativeDisp);
		}

		private long getDisposition() {
			ApplicationInfo appInfo = getNbaTxLife().getPolicy().getApplicationInfo();
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			String closerType = appInfoExt.getClosureType();
			long closerTypeTC = getClsoureTypeFromString(closerType);
			return closerTypeTC;
		}
	private long getClsoureTypeFromString(String closureType){
		long closureTypeTC = -1;
		if(closureType!=null){
			if(INCOMPLETE_STRING.equals(closureType)){//ALS5041
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_INCOMPLETE;
			}else if(NOT_TAKEN_STR.equals(closureType)){
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_NTO;
			}else if(OFFER_EXPIRED_STR.equals(closureType)){
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_OFFEREXPIRED;
			}else if (REG60_DECLINE_STR.equals(closureType) || PRESAL_EDECLINE_STR.equals(closureType)){ //ALS2773
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_WITHDRAW; //ALS2773
			}else if(CANCELLED_STR.equals(closureType)){//ALS5041
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_CANCELLED;//ALS5041
			}else if(REG60_PRESALE_EXPIRED.equals(closureType)){//APSL5125
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_REG60_PRESALE_EXPIRED;//APSL5125
			}
		}
		return closureTypeTC;
	}
	/**
	 * The method returns the highest level of disposition among all the tentative disposition objects
	 * @param tentDispList the list of Tentative disposition objects
	 * @return dispLevel the highest disposition level
	 */
	protected int getHighestDispositionLevel(List tentDispList) {
		int dispLevel = 0;
		int dispSize = tentDispList.size();
		TentativeDisp tentDisp = null;
		for (int i = 0; i < dispSize; i++) {
			tentDisp = (TentativeDisp) tentDispList.get(i);
			if (tentDisp.getDispLevel() > dispLevel) {
				dispLevel = tentDisp.getDispLevel();
			}
		}
		return dispLevel;
	}
	
	// Begin NBLXA-1722
	protected void createValErrorWorkItem() throws NbaBaseException {
		Map deOink = new HashMap();
		deOink.put("A_ErrorSeverity", Long.toString(NbaOliConstants.OLI_MSGSEVERITY_SEVERE));
		deOink.put("A_CreateValidationWI", "true");
		NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), nbaTxLife, deOink);
		NbaDst validationTransaction = retrieveValidationErrWorkItem(workProvider.getWorkType());
		if (validationTransaction == null) {
			getWork().addTransaction(workProvider.getWorkType(), workProvider.getInitialStatus());
		} else {
			changeStatus(validationTransaction, workProvider.getInitialStatus());
			validationTransaction.setUpdate();
			getWork().getWorkItem().getWorkItemChildren().add(validationTransaction.getTransaction());

		}
	}
	
	protected NbaTransaction getValidationTransaction(String workType) throws NbaBaseException {
		List transactions = getWork().getNbaTransactions();
		int count = transactions.size();
		NbaTransaction nbaTransaction = null;
		for (int i = 0; i < count; i++) {
			nbaTransaction = (NbaTransaction) transactions.get(i);
			if (workType.equalsIgnoreCase(nbaTransaction.getWorkType())) {					
				return nbaTransaction;
			}
		}
		return null;
	}
	
	    
	protected NbaDst retrieveValidationErrWorkItem(String workType) {
		NbaSearchVO searchValidationErrorVO;
		NbaDst aWorkItem = null;
		ListIterator results = null;
		try {
			searchValidationErrorVO = searchWI(getUser(), getWork(), workType);
			if (!NbaUtils.isBlankOrNull(searchValidationErrorVO) && searchValidationErrorVO.getSearchResults() != null
					&& !searchValidationErrorVO.getSearchResults().isEmpty()) {
				results = searchValidationErrorVO.getSearchResults().listIterator();
				while (results.hasNext()) {
					try {
						aWorkItem = retrieveWorkItem((NbaSearchResultVO) results.next());
						break;
					} catch (NbaBaseException e) {
						e.printStackTrace();
					}
				}
			}

		} catch (NbaBaseException e) {
			e.printStackTrace();
		}
		return aWorkItem;
	}
			
	
	public NbaDst retrieveWorkItem(NbaSearchResultVO resultVO) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setNbaUserVO(getUser());
		retOpt.setWorkItem(resultVO.getWorkItemID(), true);
		retOpt.setLockWorkItem();
		aWorkItem = retrieveWorkItem(getUser(), retOpt);
		return aWorkItem;
	}
	
	
	
	// Begin NBLXA-1722
	
}