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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.EndorsementExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
/**
 * NbaProcPrintHold is the class that processes nbAccelerator cases found
 * on the AWD Print Hold queue (NBPRTHLD). It reviews the data associated
 * with a case to determine if the case can be moved to the next queue or if it
 * fails the print hold process.  It invokes the VP/MS Print Hold model
 * to retrieve information about sources and work items to assist in the decision
 * making process.
 * <p>The NbaProcPrintHold class extends the NbaAutomatedProcess class.  
 * Although this class may be instantiated by any module, the NBA polling class 
 * will be the primary creator of objects of this type.
 * When the polling process finds a case on the Print Hold queue, 
 * it will create an object of this instance and call the object's 
 * executeProcess(NbaUserVO, NbaDst) method.  This method will manage the steps 
 * necessary to evaluate the case and determine the next step for a case.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA033</td><td>Version 3</td><td>Companion Case and HTML Indexing Views</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr> 
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * <tr><td>SPR2670</td><td>Version 6</td><td>Correction needed in Companion Case VP/MS model </td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>CR1343968</td><td>AXA Life Phase 2</td><td>Group Workfow and Miscellaneous</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 * @see NbaAutomatedProcess
 */
public class NbaProcPrintHold extends NbaAutomatedProcess {
	/** The vpmsadaptor object, which provides an interface into the VPMS system */	
	private NbaVpmsAdaptor vpmsProxy = null;
	//SPR2380 removed logger
	private NbaDst parentWork = null; // APSL2808
	// APSL2808 Begin
	protected static int MINS = 15;
	private boolean isContractUpdateRequired = false;
	static {
		try {			
			MINS = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
					NbaConfigurationConstants.SI_PRINT_CWA_SUSPEND_MINS));
		} catch (Exception ex) {			
			MINS = 15;
		}
	}
	// APSL2808 End
	
	/**
	 * This constructor calls the superclass constructor which will set
	 * the appropriate statues for the process.
	 */
	public NbaProcPrintHold() {
		super();
	}
	/**
	 * This method drives the print hold process.
	 * <P>After obtaining a reference to the NbaNetServerAccessor EJB
	 * and retrieving the statuses to be used for the process through 
	 * user of the initializeVpmsStatus method, it retrieves the 
	 * Holding Inquiry for the case and updates the case's NbaLob. 
	 * The NbaLob is then used to instantiate the NbaVpmsVO object 
	 * that will be used for calls to the Print Hold VPMS model.
	 * <P>The process verifies that the severity of the holding inquiry
	 * does not exceed limits established for Print Hold.  If it does
	 * the case fails Print Hold and is routed to a fail queue.
	 * <P>Next, it invokes the verifySources method to ensure that all
	 * required sources for the case are present. If not, the case is 
	 * suspended for a specific number of days.
	 * <P>The third check determines if work items present on the case
	 * are in the correct status and/or have correct values.  If not,
	 * the case is again suspended.
	 * After these checks are made and processed, an <code>NbaAutomatedProcessResult</code>
	 * is created and populated with values used by the polling process to
	 * determine the success or failure of the case.  The case is then updated
	 * to move it to the next queue and the result returned to the poller.
	 * @param user the user for whom the work was retrieved, in this case APPRTHLD.
	 * @param work the AWD case to be processed
	 * @return NbaAutomatedProcessResult the results of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		//begin NBA133
		if (!initializeWithoutStatus(user, work)) {
			return getResult();
		}
		//NBLXA-1782 Starts
		boolean printRestrictIndPrevious = true;
		PolicyExtension policyExt = null;
		if (!NbaUtils.isBlankOrNull(getNbaTxLife())) {
			Policy policy = getNbaTxLife().getPolicy();
			policyExt = NbaUtils.getFirstPolicyExtension(policy);
			if (!NbaUtils.isBlankOrNull(policyExt)) {
				printRestrictIndPrevious = policyExt.getPrintRistrictInd();
			}
		}
		//NBLXA-1782 ENDS
		initializeVpmsStatus(); //determine if nba contract print supported and reinitialize statuses		
		//end NBA133
		setParentWork(retrieveCaseAndTransactions(getWork(), user, true)); // APSL2808 //NBLXA-1281 updated lock indicator.
		// NBLXA-188(APSL5318) Legacy Decommissioning
		/*if (NbaUtils.isGIApplication(getNbaTxLife(), work)) {
			Policy policy = getNbaTxLife().getPolicy();
			PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
			if (null != policyExt) {
				String extComp2 = getWork().getNbaLob().getExtractCompAt(2);
				boolean reprintInd = (extComp2 != null && extComp2.trim().length() > 0);
				if (reprintInd || getNbaTxLife().isPaidReIssue()) {
					policyExt.setMDRConsentIND(false);
					policyExt.setPrintTogetherIND(false);
				}
				if (policyExt.getPrintTogetherIND()) {
					ApplicationInfoExtension appInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy()
							.getApplicationInfo());
					boolean isEmployeeOwned = false;
					if (null != appInfoExtension) {
						isEmployeeOwned = (appInfoExtension.getOwnerTypeCode() == 2);
					}
					if (policyExt.getMDRConsentIND() && policyExt.hasGIBatchID()) {
						boolean isContractPrintExtractUpdatedForAll = NbaSystemDataDatabaseAccessor.isContractPrintExtractDateUpdatedForAll(policyExt
								.getGIBatchID());
						if (isContractPrintExtractUpdatedForAll) {
							policyExt.setPdrInd(true);
						} else {
							policyExt.setPdrInd(false);
						}
						isContractUpdateRequired = true;
					} else if (isEmployeeOwned && !policyExt.getMDRConsentIND()) {
						policyExt.setPdrInd(true);
						isContractUpdateRequired = true;
					}
				}
				policyExt.setActionUpdate();
			}
		}*/
		try {
			NbaCompanionCaseRules rules = new NbaCompanionCaseRules(user, getWork());
			if (rules.isSuspendNeeded(NbaVpmsConstants.PRINT_HOLD)) { //SPR2670
				if(rules.isSuspendDurationWithinLimits()){
					addComment("Work Item suspended: Waiting for other companion cases");					
					if (!NbaUtils.isBlankOrNull(policyExt)) {// NBLXA-1782
						policyExt.setPrintRistrictInd(true); // NBLXA-1782
					}// NBLXA-1782
					updateForSuspend(rules.getSuspendVO());	
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
				} else {
					addComment("Suspend limit exceeded.");
					if (!NbaUtils.isBlankOrNull(policyExt)) {// NBLXA-1782
						policyExt.setPrintRistrictInd(true); // NBLXA-1782
					}// NBLXA-1782
					changeStatus(getFailStatus());
					getWork().getNbaLob().deleteAppHoldSuspDate(); //NBLXA-1646 resetting LOB
					doUpdateWorkItem();
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Error", getFailStatus()));
				}
			} else if (isWorkItemTobeHeldForCWA()) { // APSL2808 Begin
				addComment("Work Item suspended: Waiting for CWA to apply");					
				if (!NbaUtils.isBlankOrNull(policyExt)) {// NBLXA-1782
					policyExt.setPrintRistrictInd(true); // NBLXA-1782
				}// NBLXA-1782
				updateForSuspend(getSuspendWorkVO(Calendar.MINUTE, MINS));	
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
			} else if (isWorkItemTobeHeldForInforceUpdate()) { // APSL5128 Begin
				addComment("OutStanding Inforce Update WorkItem present needs resolution");
				if (!NbaUtils.isBlankOrNull(policyExt)) {// NBLXA-1782
					policyExt.setPrintRistrictInd(true); // NBLXA-1782
				}// NBLXA-1782
				updateForSuspend(getSuspendWorkVO(Calendar.DAY_OF_WEEK,
						Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.PRNT_SUSPEND_DAYS))));
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
			} // APSL2808 End 
			else if(isWorkItemTobeHeldForMissingAmndReq()) {//NBLXA-1281 Policy Print Preview changes
				addComment("Work Item suspended: Waiting for Amendment Requirement to be generated");					
				if (!NbaUtils.isBlankOrNull(policyExt)) {// NBLXA-1782
					policyExt.setPrintRistrictInd(true); // NBLXA-1782
				}// NBLXA-1782
				updateForSuspend(getSuspendVoForAmendment(Calendar.MINUTE, MINS));	
				routeAppToReqDet();
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
			} else { 			
				// NBLXA-188(APSL5318) Legacy Decommissioning
				if(isContractUpdateRequired){
					doContractUpdate();
				}// NBLXA-188(APSL5318) Legacy Decommissioning
				changeStatus(getPassStatus());
				if (!NbaUtils.isBlankOrNull(policyExt)) {// NBLXA-1782
					policyExt.setPrintRistrictInd(false); // NBLXA-1782
				}// NBLXA-1782
				//NBLXA-1896 Begins
				if(!NbaUtils.isBlankOrNull(getNbaTxLife()) && getNbaTxLife().getPrimaryHolding().getSystemMessageCount() > 0){
					if(!AxaUtils.isShortageCVpresent(getNbaTxLife().getPrimaryHolding().getSystemMessage())){
						getWork().getNbaLob().setCheckAmount(NbaConstants.TRUE);
					}
				}
				//NBLXA-1896 Ends
				getWork().getNbaLob().setAppHoldSuspDate(null); //reset this LOB
				doUpdateWorkItem();
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
			}
			if(!NbaUtils.isBlankOrNull(policyExt) && printRestrictIndPrevious != policyExt.getPrintRistrictInd()){ // NBLXA-1782
				setContractAccess(UPDATE);   
				policyExt.setActionUpdate();
				doContractUpdate(); // NBLXA-1782
			}	// NBLXA-1782
			return result;
		} catch (Exception e) {
			NbaBaseException nbe = new NbaBaseException("An exception occured during Print Hold process", e);
			throw nbe;
		}
	}
	/**
	 * NBLXA-1281 Policy Print Preview changes
	 * @return boolean is Work Item To be Held For Missing Amnd Req
	 */
	private boolean isWorkItemTobeHeldForMissingAmndReq() {
		Policy policy = null;
		Endorsement endorsement = null;
		EndorsementExtension endorsementExtension = null;
		int endtCount = 0;
		boolean toBeReturned = false;
		boolean amndReqAdded = false;
		RequirementInfo reqInfo = null;
		if (null != nbaTxLife && null != (policy = nbaTxLife.getPolicy())) {
			endtCount = policy.getEndorsementCount();
			for (int index = 0; index < endtCount; index++) {
				endorsement = policy.getEndorsementAt(index);
				endorsementExtension = NbaUtils.getFirstEndorseExtension(endorsement);
				if (NbaUtils.isAmendment(endorsement) && null != endorsementExtension && !endorsementExtension.getRequirementGeneratedInd()) {
					List reqInfoList = nbaTxLife.getRequirementInfoList(endorsement.getAppliesToPartyID(), NbaOliConstants.OLI_REQCODE_AMENDMENT);
					for (int indexR = 0; indexR < reqInfoList.size(); indexR++) {
						reqInfo = (RequirementInfo) reqInfoList.get(indexR);
						if (null != reqInfo && reqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_ADD) {
							amndReqAdded = true;
							break;
						}
					}
					if (!amndReqAdded) {
						toBeReturned = true;
					}
				}
			}
		}
		return toBeReturned;
	}
	/**
	 * NBLXA-1281 Policy Print Preview changes
	 * @throws NbaBaseException
	 */
	private void routeAppToReqDet() throws NbaBaseException {
		if(getValidStatusList(NbaVpmsAdaptor.QUEUE_STATUS_CHECK, NbaVpmsAdaptor.EP_GET_HOLD_STATUSES).contains(getParentWork().getStatus())) {
			//getLogger().logDebug("Contract [" + getParentWork().getWorkItem().getContractNumber() + "] is changing status to REQDET");
			changeStatus(getParentWork(), A_STATUS_FINAL_ISSUED);
			update(getParentWork());
			if(getParentWork().isSuspended()) {
				NbaSuspendVO suspendVO = new NbaSuspendVO();
				suspendVO.setCaseID(getParentWork().getID());
				unsuspendWork(suspendVO);
				unlockWork(getParentWork());
			}
		}
	}
	
//	NBA103 - removed method

	/**
	 * Since the case must be suspended before it can be unlocked, this method is used 
	 * instead of the superclass method to update AWD.
	 * <P>Updates the case in AWD, suspend the case using the supsendVO, and unlock the case.
	 * @param Suspend value object to send required parameters to suspend a case
	 * @throws NbaBaseException
	 */
	public void updateForSuspend(NbaSuspendVO vo) throws NbaBaseException {
		getLogger().logDebug("Starting updateForSuspend");
		vo.setTransactionID(getWork().getID());
		vo.setCaseID(null); //reset the case id if it had any
		updateWork(getUser(), getWork());  //NBA213
		suspendWork(getUser(), vo);  //NBA213
		unlockWork(getUser(), getWork());  //NBA213
	}
	
	
	/**
	 * Reinitializes the vpms status fields after determining nba contacrt print support for contract.
	 * @throws NbaBaseException
	 */
	//NBA133 New Method
	private boolean initializeVpmsStatus() throws NbaBaseException {
		Map deOink = new HashMap();
		/*Begin CR1343968*/
		NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess();	
		if (nbaTxLife != null) {
			oinkDataAccess.setContractSource(nbaTxLife);
		}
		/*End CR1343968*/
		String extComp2 = getWork().getNbaLob().getPrintExtract(); //APSL50555 APSL3563
		deOink.put("A_EXTRACTCOMPLOB2", extComp2); //APSL3563
		deOink.put("A_NbaContractPrint", String.valueOf(NbaUtils.isNbaContractPrintSupported(getWork().getNbaLob()))); 
		statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), nbaTxLife,deOink);	
		return true;
	}
	
	// APSL2808 New method
	protected boolean isWorkItemTobeHeldForCWA() throws NbaBaseException {
		if (!getNbaTxLife().isSIApplication()) {
			return false;
		}
				
		List tempList = getParentWork().getNbaTransactions();
		Iterator itr = tempList.iterator();
		while (itr.hasNext()) {
			NbaTransaction transaction = (NbaTransaction) itr.next();
			if (NbaConstants.A_WT_CWA.equals(transaction.getWorkType()) && END_QUEUE.equals(transaction.getQueue())) { // Only 1 CWA WI for SI
				return false;
			}
		}
		return true;
	}
	
	//APSL5128
	protected boolean isWorkItemTobeHeldForInforceUpdate() throws NbaNetServerDataNotFoundException {
		List transactions = getParentWork().getNbaTransactions();
		for (int i = 0; i < transactions.size(); i++) {
			NbaTransaction nbaTransaction = (NbaTransaction) transactions.get(i);
			if (nbaTransaction.getWorkType().equalsIgnoreCase(NbaConstants.A_ST_INFORCE_UPDATE) && !(nbaTransaction.isInEndQueue())) {
				try { //NBLXA-2620 Begin
					updateLOBForUnsuspend(nbaTransaction);
				} catch (NbaBaseException e) {
					e.printStackTrace();
				}//NBLXA-2620 End
				return true;
			}
		}
		return false;
	}
	/**
	 * Retrieve the Case and Transactions associated
	 * @param  dst workItem / case, for which the sibiling transactions / child transactions need to be retrieved along with the case
	 * @param  user AWD user id  
	 * @param  locked lock indicator
	 * @return NbaDst containing the case and all the transactions
	 * @throws NbaBaseException
	 */
	// APSL2808 New Method	
	protected NbaDst retrieveCaseAndTransactions(NbaDst dst, NbaUserVO user, boolean locked) throws NbaBaseException {
		//create and set parent case retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(dst.getID(), false);
		retOpt.requestCaseAsParent();
		retOpt.requestTransactionAsSibling();
		if (locked) {			
			retOpt.setLockParentCase();			
		}
		//get case from awd
		NbaDst parentCase = retrieveWorkItem(user, retOpt);
		return parentCase;
	}
	
	// APSL2808 New Method
	protected NbaSuspendVO getSuspendWorkVO(int type, int value) {
		GregorianCalendar cal = new GregorianCalendar();
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		cal.setTime(new Date());
		cal.add(type, value);
		suspendVO.setActivationDate(cal.getTime());		
		return suspendVO;
	}
	
	/**
	 * APSL5398- Creating SuspendVO using AWD time. 
	 * @param type
	 * @param value
	 * @return
	 * @throws NbaBaseException 
	 */
	protected NbaSuspendVO getSuspendVoForAmendment(int type, int value) throws NbaBaseException {
		GregorianCalendar cal = new GregorianCalendar();
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		NbaDst nbaDst = WorkflowServiceHelper.getTimeStamp(getUser());
		Date dt = NbaUtils.getDateFromStringInAWDFormat(nbaDst.getTimestamp());
		cal.setTime(dt);
		cal.add(type, value);
		suspendVO.setActivationDate(cal.getTime());
		return suspendVO;
	}
	
	/**
	 * Returns the parent workitem 
	 * @return
	 */
	// APSL2808 New Method
	protected NbaDst getParentWork() {
		return parentWork;
	}

	/**
	 * Sets the parent workitem
	 * @param newWork the parent workitem
	 */
	// APSL2808 New Method
	protected void setParentWork(NbaDst newWork) {
		parentWork = newWork;
	}
	/**
	 * NBLXA-1281 Policy Print Preview changes
	 * @return list of status
	 * @throws NbaVpmsException
	 * @throws NbaVpmsException
	 */
	protected List<String> getValidStatusList(String vpmsModel, String vpmsEntryPoint) throws NbaVpmsException {
		NbaVpmsAdaptor vpmsProxy = null;
		List<String> validStatusList = new ArrayList<String>();
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			vpmsProxy = new NbaVpmsAdaptor(oinkData, vpmsModel);
			vpmsProxy.setVpmsEntryPoint(vpmsEntryPoint);
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult compResult = vpmsProxy.getResults();
			NbaStringTokenizer tokens = new NbaStringTokenizer(compResult.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			String aToken;
			while (tokens.hasMoreTokens()) {
				aToken = tokens.nextToken();
				validStatusList.add(aToken);
			}

		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("Problem with fetching result from VPMS " + NbaVpmsException.VPMS_EXCEPTION, re);
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
	
	//NBLXA-2620
		public void updateLOBForUnsuspend(NbaTransaction transaction) throws NbaBaseException {
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(transaction.getID(), true);
			retOpt.setLockWorkItem();
			NbaDst nbadst = retrieveWorkItem(getUser(), retOpt);
			nbadst.setUpdate();
			nbadst.getNbaLob().setUnsuspendWorkItem(getWork().getID());
			System.out.println("Inforce Update ID == " + transaction.getID() + " printCRDA on PrintHold== " + getWork().getID() );
			updateWork(getUser(), nbadst);
			unlockWork(nbadst);
		}	
	//NBLXA-2620
}

