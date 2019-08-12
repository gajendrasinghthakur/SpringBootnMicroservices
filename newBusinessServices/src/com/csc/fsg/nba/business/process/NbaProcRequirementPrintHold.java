package com.csc.fsg.nba.business.process;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fsg.nba.business.process.printpreview.AxaContractPrintProxy;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.SuspendInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * NbaProcRequirementPrintHold is the class that processes nbAccelerator NBPRTTEXT Work Items
 * in NBRQPRHD (Requirement Print hold) queue. It holds this Work in the same queue if there are any 
 * outstanding requirements, with print restrict code as restriction. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA120</td><td>Version 5</td><td>Requirement Print Restrict Code</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3391</td><td>Version 8</td><td>Contract Print (NBPRTEXT) Work Item Errors with Unhandled Exception</td></tr>
 * </table> 
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaProcRequirementPrintHold extends NbaAutomatedProcess {

	protected static final String OUTSTANDING_REQ_MSG = "Outstanding requirement with Restrict Print Code.";
	protected static final String SUSPEND_DURATION_ELPSD_MSG = "Maximum suspend duration elapsed.";
	protected static final String CASE_NOT_PROCESSED_MSG = "Case has not passed the Requirement Determination."; //ALS5573
	private NbaDst lockedCase; //ALS5061
	private NbaDst lockedRequirementTransaction; //NBLXA-2620
	private String printCRDA = null;//NBLXA-2620
	private NbaDst nbaCurrentdst;//NBLXA-2620


	/**
	 * NbaProcRequirementPrintHold constructor comment.
	 */
	public NbaProcRequirementPrintHold() {
		super();
		setContractAccess(UPDATE);//AXAL3.7.40G
	}
	/**
	 * Verifies if there are any outstanding requirements and the work item statisfies the criterion to be suspended
	 * again then suspends the work item depending on the values in the VP / MS model. If the workitem can not be suspended
	 * again, then the workitem is moved to error queue. 
	 * For no outstanding requirements the Work Item is moved to the next stage in the workflow. 
	 * execute the automated process.
	 * @param user the user for whom the process is being executed
	 * @param work print extract Work Item( a DST value object) for which the process is to occur
	 * @return NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		if (!initialize(user, work)) {
			return getResult();
		}
		// get the lob for the print extract work item
		NbaLob lob = getWorkLobs(); //SPR3391

		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Verifying if requirements outstanding with print restrict code for contract " + lob.getPolicyNumber());
		}
		
		printCRDA = work.getID();//NBLXA-2620
		
		//NBLXA-1632 Start
		NbaDst parentCase = retrieveCaseAndTransactions(getWork(), user, true);
		if(NbaUtils.isProductCodeCOIL(nbaTxLife) && !A_STATUS_FINAL_ISSUED.equalsIgnoreCase(parentCase.getStatus())){
			boolean isrestrictCVReqPresent = doCoilPreventProcessing();
			if(!isrestrictCVReqPresent){
				setLockedCase(parentCase);
				isrestrictCVReqPresent = isWorkItemTobeHeld();
			}
			HashMap deOink = new HashMap();
			if(isrestrictCVReqPresent){
				deOink.put("A_AggregateInd", "true");
			}else{
				deOink.put("A_AggregateInd", "false");
			}
			NbaOinkDataAccess oinkData = null;
			NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getDataFromVpms(NbaVpmsAdaptor.AUTO_PROCESS_STATUS,
					NbaVpmsAdaptor.EP_WORKITEM_STATUSES, oinkData, deOink, null));
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", statusProvider.getPassStatus()));
			changeStatus(statusProvider.getPassStatus());
			if (getWork().isLocked(getUser().getUserID())) {
				doUpdateWorkItem();
			}
			//NBLXA-1632 End
		}else{
			//Begin AXAL3.7.40G
			NbaAutomatedProcessResult preventProcessingResult = doPreventProcessing();
			if (preventProcessingResult != null) {
				return preventProcessingResult;
			}
			//End AXAL3.7.40G
			//NbaDst parentCase = retrieveCaseAndTransactions(getWork(), user, true);//ALS5061/ALS5573
			setLockedCase(parentCase); //ALS5061/ALS5573
			// verify if this workitem needs to be held back as there are some requirements outstanding with print restrict restriction code.
			// SPR3391 code deleted
			// isWorkItemToBeHeld == true means the work item needs to be held
			if(isWorkItemTobeHeld()) {//SPR3391
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Requirements outstanding for " + lob.getPolicyNumber());
				}
				// check if work item can be suspended further
				if (!isAllowableSuspendDaysOver()) { //SPR3391
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("WorkItem Suspend days not over for" + lob.getPolicyNumber());
					}
					// suspend the work item
					suspendWorkItem(OUTSTANDING_REQ_MSG, getSuspendActivationDate(lob)); //SPR3391
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "Suspended"));
				} else {
					setSuspendDaysElapsed();
				}
				}else{ //SPR3391
				// if (isWorkItemToBeHeld = false)workitem is ready to go to next queue, change the status to pass.
				// move to work item to next queue as per workflow
				//SPR3391 code deleted
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("No outstanding requirements with print restrict code for " + lob.getPolicyNumber());
				}
				//createPrintPreview(); // APSL4419,APSL5100
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
				changeStatus(getResult().getStatus());
				doUpdateWorkItem();
			}
			//Begin ALS5061
			if (getLockedCase() != null) {
				NbaContractLock.removeLock(getUser());
				unlockWork(getLockedCase());
			}
			//End ALS5061
		}
		
		return getResult();
	}

	protected void setSuspendDaysElapsed() throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(
				"WorkItem Suspend days over, sending print extract work item to error queue " + getWork().getNbaLob().getPolicyNumber());
		}
		// workitem has been suspended for the configured number of times
		// work item cannot be suspended further, Modify the status to failed
		// so move the case to error queue with the PRINTERRD status.
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, SUSPEND_DURATION_ELPSD_MSG, getFailStatus()));//APSL4226
		// resetting the APHL LOB
		getWork().getNbaLob().deleteAppHoldSuspDate();
		addComment(SUSPEND_DURATION_ELPSD_MSG, getUser().getUserID());
		changeStatus(getResult().getStatus());
		doUpdateWorkItem();
	}

	/**
	 * Call Requirement Print Hold VP/MS model at given entry point 
	 * @param lobData Lob data which from which OINK auto populated the input vpms values 
	 * @param deOinkMap map containing overrided or non lob data
	 * @param entryPoint Entry point which needs to be called
	 * @return String result string from VP / MS model execution
	 * @throws NbaBaseException
	 */
	//	SPR3391 changed method signature
	public String processRequirementPrintHoldVpms(NbaLob lobData, Map deOinkMap, NbaOinkRequest oinkRequest, String entryPoint) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess(lobData);
			data.setContractSource(getNbaTxLife()); //SPR3391
			vpmsProxy = new NbaVpmsAdaptor(data, NbaVpmsAdaptor.REQUIREMENT_PRINT_HOLD);
			//begin SPR3391
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			if (deOinkMap != null) {
				vpmsProxy.setSkipAttributesMap(deOinkMap);
			}
			//begin NBA130
			if (oinkRequest != null) {
				vpmsProxy.setANbaOinkRequest(oinkRequest);
			}
			//end NBA130
			//end SPR3391
			// get the string out of XML returned by VP / MS Model and parse it to create the object structure
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			// check if the vpms call was successfull
			if (vpmsResultsData.wasSuccessful()) {
				// got the xml result back
				String xmlString = (String) vpmsResultsData.getResultsData().get(0);
				// parsing the xml result
				NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
				VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
				ArrayList strAttrs = vpmsModelResult.getStandardAttr();
				if (strAttrs.size() > 0) {
					StandardAttr strAttrObj = (StandardAttr) strAttrs.get(0);
					// return the result value
					return strAttrObj.getAttrValue();
				}
			}
			return null;
		} catch (RemoteException re) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", re);
		} finally {
			if (vpmsProxy != null) {
				try {
				    //begin SPR3362
					vpmsProxy.remove();					
					//end SPR3362
				} catch (RemoteException re) {
				    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //SPR3362
				}
			}
		}
	}

	/**
	 * Answers if the print extract work item needs to be held, if there are any out standing requirements
	 * with print restrict restriction code.
	 * @return true if workItem needs to be held back in the queue
	 */
	//SPR3391 change method signature
	protected boolean isWorkItemTobeHeld() throws NbaBaseException {
		boolean isWorkItemToBeHeld = false;
		List requirements = getAllRequirements();  //NBA213, SPR3391/ ALS5573
		Iterator itr = requirements.iterator();
		//SPR3391 code deleted
		while (itr.hasNext()) {
			NbaTransaction tempTrans = (NbaTransaction) itr.next();
			
			// if this transaction has restriction code of 1000500001
			//	and is outstanding set success = false				
			NbaLob lob = tempTrans.getNbaLob();
			System.out.println("tempTrans RQTP == " + lob.getReqType() + " tempTrans ID == " + tempTrans.getID());
			//begin SPR3391 
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			if(lob.getReqUniqueID() != null){
			    RequirementInfo reqInfo = getNbaTxLife().getRequirementInfo(lob.getReqUniqueID());
			    if(reqInfo != null){ //ALS5061
			    	oinkRequest.setRequirementIdFilter(reqInfo.getId());
			    }else{ //ALS5061
			    	throw new NbaBaseException("Unable to get RequirementInfo from TxLife for:" +lob.getReqUniqueID());//ALS5061
			    }//ALS5061
			}
			Map deOink = new HashMap();//ALS5573
			deOink.put("A_CaseStatus", lockedCase.getStatus()); //ALS5573
			String workItemToBeHeldRes = processRequirementPrintHoldVpms(lob, deOink, oinkRequest, NbaVpmsAdaptor.EP_GET_IF_PRINTWORKITEM_TO_BE_HOLD);//ALS5573
			//end SPR3391
			if (null == workItemToBeHeldRes) {
				// VP / MS call failed
				throw new NbaBaseException("VP/MS call to RequirementPrintHold model returned null ");
			}
			isWorkItemToBeHeld = new Boolean(workItemToBeHeldRes).booleanValue();
			if (isWorkItemToBeHeld) {
				nbaCurrentdst = retrieveRequireWork(tempTrans);
				nbaCurrentdst.getNbaLob().setUnsuspendWorkItem(printCRDA);
				nbaCurrentdst.setUpdate();
				System.out.println("tempTrans == " + tempTrans.getID() + " printCRDA == " + printCRDA );
				updateWork(getUser(), nbaCurrentdst);
				unlockWork(getLockedRequirementTransaction());				
				break;
			}
		}
		return isWorkItemToBeHeld;
	}

	/**
	 * This method checks whether print extract work item has reached the maximum allowable suspend days limit quering RequirementPrintHold VP/MS model 
	 * @return true if max suspend days limit is over else return false.
	 * @throws NbaBaseException 
	 */
	//	SPR3391 changed method signature
	public boolean isAllowableSuspendDaysOver() throws NbaBaseException {
		NbaLob lob = getWorkLobs(); //SPR3391
		Date appHoldSusDate = lob.getAppHoldSuspDate();
		if (null == appHoldSusDate) {
			appHoldSusDate = new Date();
			lob.setAppHoldSuspDate(appHoldSusDate);
			work.setUpdate();
		}

		int maxSuspendDaysNum = lob.getMaxNumSuspDays();

		if (-1 == maxSuspendDaysNum) {
			String suspendData = processRequirementPrintHoldVpms(lob, null, null, NbaVpmsAdaptor.EP_GET_MAX_SUSPEND_DAYS); //SPR3391
			if (null == suspendData) {
				// VP / MS call failed
				throw new NbaBaseException("VP/MS call to RequirementPrintHold model returned null ");
			}
			maxSuspendDaysNum =  NbaUtils.convertStringToInt(suspendData);
			lob.setMaxNumSuspDays(maxSuspendDaysNum);
			work.setUpdate();
		}
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(appHoldSusDate);
		calendar.add(Calendar.DAY_OF_WEEK, maxSuspendDaysNum);
		Date maxSuspendDate = (calendar.getTime());
		if (maxSuspendDate.before(new Date())) {
			return true;
		}
		return false;
	}

	/** 
	 *  Answers the suspend activation date for print workItem quering
	 *  RequirementPrintHold VP/MS model
	 *  @param lob the NbaLob object for print extract work item.
	 *  @return activation date object.
	 */
	//SPR3391 changed method signature. 
	public Date getSuspendActivationDate(NbaLob lob) throws NbaBaseException {
		Map deOink = new HashMap();//ALS5573
		deOink.put("A_CaseStatus", lockedCase.getStatus()); //ALS5573
		String activationDate = processRequirementPrintHoldVpms(lob, deOink, null, NbaVpmsAdaptor.EP_GET_SUSPEND_ACTIVATE_DATE); //SPR3391/ALS5573
		if (null == activationDate) {
			// VP / MS call failed			
			throw new NbaBaseException("VP/MS call to RequirementPrintHold model returned null ");
		}
		return NbaUtils.getDateFromStringInAWDFormat(activationDate);
	}

	/**
	 * Gets all the requirements for the work item if case else parent workitem if the passed workitem is a transaction
	 * @param  workItem, for which the sibiling requirements need to be retrieved
	 * @param  user, AWD user id  
	 * @return java.util.List containing NbaDst for all the requirements of the parent Case
	 * @throws NbaBaseException
	 */
	//NBA213 New Method
//	SPR3391 changed method signature /ALS5573 changed method signature
	protected List getAllRequirements() throws NbaBaseException {
		List tempList = getLockedCase().getNbaTransactions();//ALS5573
		Iterator itr = tempList.iterator();
		while (itr.hasNext()) {
			if (!NbaConstants.A_WT_REQUIREMENT.equals(((NbaTransaction) itr.next()).getTransaction().getWorkType())) {
				itr.remove();
			}
		}
		return tempList;
	}
	
	/**
	 * Retrieve the Case and Transactions associated
	 * @param  dst workItem / case, for which the sibiling transactions / child transactions need to be retrieved along with the case
	 * @param  user AWD user id  
	 * @param  locked lock indicator
	 * @return NbaDst containing the case and all the transactions
	 * @throws NbaBaseException
	 */
	//NBA213 New Method
	//SPR3391 changed method signature
	protected NbaDst retrieveCaseAndTransactions(NbaDst dst, NbaUserVO user, boolean locked) throws NbaBaseException {
		//create and set parent case retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		// if case
		if (dst.isCase()) {
			retOpt.setWorkItem(dst.getID(), true);
			retOpt.requestTransactionAsChild();
			if (locked) {
				retOpt.setLockWorkItem();
				retOpt.setLockTransaction();
			}
		} else { // if a transaction
			retOpt.setWorkItem(dst.getID(), false);
			retOpt.requestCaseAsParent();
			retOpt.requestTransactionAsSibling();			
			if (locked) {
				//retOpt.setLockWorkItem(); ALS5061 deleted
				retOpt.setLockParentCase();				
				//retOpt.setLockSiblingTransaction(); ALS5061 deleted
			}
		}
		//get case from awd
		NbaDst parentCase = retrieveWorkItem(user, retOpt);
		return parentCase;
	}
	
	/**
	 * @return NbaAutomatedProcessResult
	 * @throws NbaBaseException
	 */
	//new method AXAL3.7.40G
	protected NbaAutomatedProcessResult doPreventProcessing() throws NbaBaseException {
		NbaAutomatedProcessResult result = null;
		AxaPreventProcessVpmsData preventProcessData = new AxaPreventProcessVpmsData(user, getNbaTxLife(), NbaVpmsAdaptor.REQUIREMENT_PRINT_HOLD);
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
		boolean routetoAlternateStatus = false;
		boolean deleteSuspendInfo = false;
		if (preventProcessData.isPreventsProcess()) {
			if (preventProcessData.isNextOptSuspend()) {
				//check if case can be suspended. suspendCase() will return true if case has been suspended else return false
				//if it is to be suspended set suspendReason to 5
				if (suspendCase(preventProcessData, PREVENT_CONTRACTPRINT_REASON)) {
					result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "Suspended");
				} else {
					routetoAlternateStatus = true;
					deleteSuspendInfo = true;
					result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Maximum suspend duration elapsed.",//APSL4226
							getAlternateStatus());
				}
			}
		} else {
			//This will execute in two cases
			//1 case never had CVs with prevent code 5
			//2 Case had CVs with prevent code 5 once, which have been resolved, case should follow the normal processing
			if (policyExtension != null && policyExtension.hasSuspendInfo()) {
				SuspendInfo suspendInfo = policyExtension.getSuspendInfo();
				//Check if this has been suspended before because 5 were present, now that 1s have resolved (prevent process=true)
				if (PREVENT_CONTRACTPRINT_REASON == suspendInfo.getSuspendReason()) {
					deleteSuspendInfo = true;
				}
			}
		}
		if (policyExtension != null && deleteSuspendInfo == true) {
			policyExtension.getSuspendInfo().setActionDelete();
			nbaTxLife = doContractUpdate();
			handleHostResponse(nbaTxLife);
			if (routetoAlternateStatus == true) {
				changeStatus(getAlternateStatus());
				doUpdateWorkItem();
			}
		}
		return result;
	}
	//NBLXA-1632 New Method
	protected boolean doCoilPreventProcessing(){
		List messages = nbaTxLife.getPrimaryHolding().getSystemMessage();
		Iterator msgItr = messages.iterator();
		while (msgItr.hasNext()) {
			SystemMessage sysMsg = (SystemMessage) msgItr.next();
			if (!sysMsg.isActionDelete() && !sysMsg.isActionDeleteSuccessful()) {
                SystemMessageExtension systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMsg);
                if (systemMessageExtension != null) {
                    if (!systemMessageExtension.getMsgOverrideInd() && sysMsg.getMessageSeverityCode()!= 1) {
                    	addComment("Print WI Aggregate due to outstanding CV "+sysMsg.getMessageCode());
                        return true;
                    }
                }
            }
		}
		return false;
	}
	
	//ALS5061 New Method
	public NbaDst getLockedCase() {
		return lockedCase;
	}
	//ALS5061 New Method
	public void setLockedCase(NbaDst lockedCase) {
		this.lockedCase = lockedCase;
	}
	
	// APSL4419 New method
	protected void createPrintPreview() throws NbaBaseException {
		byte[] image = new AxaContractPrintProxy(getWork(), getUser(), getNbaTxLife()).generatePrintPreview();
		attachPrintPreview(image);
	}
	
	// APSL4419 New method
	protected void attachPrintPreview(byte[] image) throws NbaBaseException {
		retrieveWork();
		for (int z = 0; z < getWork().getTransaction().getSourceChildren().size(); z++) {
			WorkItemSource source = (WorkItemSource) getWork().getTransaction().getSourceChildren().get(z);
			if (NbaConstants.A_ST_PRINTPREVIEW.equals(source.getSourceType())) {
				source.setBreakRelation("Y");
			}
		}				
		getWork().addImageSource(getWork().getNbaTransaction(), NbaConstants.A_ST_PRINTPREVIEW, image);
	}
	
	// APSL4419 New method
	protected void retrieveWork() throws NbaBaseException {		
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(getWork().getID(), false);
		retrieveOptionsValueObject.requestSources();
		retrieveOptionsValueObject.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject));		
	}
	
	/** New Method - //NBLXA-2620
	 * @return the lockedRequirementTransaction
	 */
	public NbaDst getLockedRequirementTransaction() {
		return lockedRequirementTransaction;
	}
	
	/** New Method - //NBLXA-2620
	 * @param lockedRequirementTransaction the lockedRequirementTransaction to set
	 */
	public void setLockedRequirementTransaction(NbaDst lockedRequirementTransaction) {
		this.lockedRequirementTransaction = lockedRequirementTransaction;
	}
	
	// NBLXA-2620 New method
		protected NbaDst retrieveRequireWork(NbaTransaction tempTrans) throws NbaBaseException {		
			NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
			retrieveOptionsValueObject.setWorkItem(tempTrans.getID(), false);
			retrieveOptionsValueObject.setLockWorkItem();
			setLockedRequirementTransaction(retrieveWorkItem(getUser(), retrieveOptionsValueObject));
			return getLockedRequirementTransaction();
		}
		
}
