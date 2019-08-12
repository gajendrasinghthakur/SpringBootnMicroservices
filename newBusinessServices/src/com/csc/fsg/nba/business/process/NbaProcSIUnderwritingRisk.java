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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.contract.validation.NbaContractValidation;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * <tr><td>APSL3118</td><td>Discretionary</td><td>Simplified Issue Icon Changes</td></tr>
 * <tr><td>APSL4601</td><td>Discretionary</td><td>Transformation(Secure Now)</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class NbaProcSIUnderwritingRisk extends NbaProcUnderwritingRisk {
	
	protected static int MIN = 30;
	
	private NbaDst parentWork = null;
	
	/**
	 * This abstract method must be implemented by each subclass in order to execute the automated process.
	 * @param user the user/process for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {		
		if (!initialize(user, work)) {
			return getResult();
		}
		
		//APSL3308(QC12368)
		if(getNbaTxLife() != null && !getNbaTxLife().isSIApplication()){
			handleNonSICaseTOSIQueue();
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
			return getResult();
		}// APSL3308(QC12368) end
		
		if (work.isTransaction()) {
			ArrayList messages = getNbaTxLife().getPrimaryHolding().getSystemMessage();
			setStatusProvider(new NbaProcessStatusProvider(getDataFromVpms(NbaVpmsAdaptor.AUTO_PROCESS_STATUS,
					NbaVpmsAdaptor.EP_WORKITEM_STATUSES, null, new HashMap(), null)));
			if (getWork().getWorkType().equalsIgnoreCase(A_WT_AGENT_LICENSE)) {
				if (NbaUtils.hasSignificantValErrors(messages, SUBSET_SI_AGENT)) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getFailStatus()));
				} else {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
					setSuspendVO(getUnsuspendWorkVO());
				}				
			} else if (getWork().getWorkType().equalsIgnoreCase(A_WT_NBVALDERR)) {
				if (isSeverityExists(messages)) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getFailStatus()));
				} else {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
					setSuspendVO(getUnsuspendWorkVO());
				}
			}
			changeStatus(getResult().getStatus());
		} else {
			SystemMessageExtension sysMsgExtn = NbaUtils.getFirstSystemMessageExtension(getNbaTxLife().getSystemMessage(MSGCODE_SI_INFOREQ));
			if (sysMsgExtn != null && !sysMsgExtn.getMsgOverrideInd()) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getAlternateStatus()));
				changeStatus(getResult().getStatus());
				update(getWork());
				return getResult();
			}
			retrieveCaseWithTransactions();
			
			List attachmentList = AxaUtils.getAttachmentsByType(getNbaTxLife().getPrimaryParty().getParty(), NbaOliConstants.OLI_ATTACH_PRIORINS);
			if (attachmentList == null || (attachmentList != null && attachmentList.size() == 0)) {
				calculateUnderwritingRisk();
			}
			new NbaContractValidation().validate(getNbaTxLife(), getWork(), getUser());
			ArrayList messages = getNbaTxLife().getPrimaryHolding().getSystemMessage();
			HashMap deOink = new HashMap();
			if (NbaUtils.hasSignificantValErrors(messages, SUBSET_SI_PHI)) {
				deOink.put("A_PHICVExists", "true");
				//Prior Insurance changes
				if(checkCvExistOnHitCase(messages, SUBSET_SI_PHI).equalsIgnoreCase("HitDeclined")){
					updateFinalDispositionFieldsForDeclinCase(); // Prior Insurance changes
					deOink.put("A_HitDeclined","true");
				}else{
					deOink.put("A_HitDeclined","false");
				}				
				//Prior Insurance changes
				setStatusProvider(new NbaProcessStatusProvider(getDataFromVpms(NbaVpmsAdaptor.AUTO_PROCESS_STATUS,
						NbaVpmsAdaptor.EP_WORKITEM_STATUSES, null, deOink, null)));
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
				changeStatus(getResult().getStatus());				
			} else {
				processAgent(messages);							
				processValErr(messages);
				if (getSuspendVO() != null) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
				} else {					
					deOink.put("A_PHICVExists", "false");
					setStatusProvider(new NbaProcessStatusProvider(getDataFromVpms(NbaVpmsAdaptor.AUTO_PROCESS_STATUS,
							NbaVpmsAdaptor.EP_WORKITEM_STATUSES, null, deOink, null)));
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
					changeStatus(getResult().getStatus());
				}			
			}
			super.setRCMTeamConciergeInd(getNbaTxLife()); //APSL4412
			doContractUpdate(getNbaTxLife());
		}
		updateCaseAndTransactions();
		return getResult();
	}
	
	/**
	 * Checks all the SystemMessage objects in xml203 and returns true if any of the SystemMessage object has message severity 2 or 4. 
	 */ 
	protected boolean isSeverityExists(ArrayList messages) {
		boolean severity = false;
		SystemMessage sysMessage;
		SystemMessageExtension systemMessageExtension;//ALS4242		
		for (int i = 0; i < messages.size(); i++) {
			sysMessage = (SystemMessage) messages.get(i);
			if (sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_SEVERE
					|| sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE) { //P2AXAL007
				systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage); //ALS4242
				if (systemMessageExtension != null && (systemMessageExtension.getMsgValidationType() == SUBSET_SI_AGENT || systemMessageExtension.getMsgValidationType() == SUBSET_SI_CWA)) { // APSL3080
					continue; 
				}
				severity = true;
				break;
			}
		}
		return severity;
	}	
	
	/**
	 * The method updates Final disposition fields for declining a case.
	 */
	protected void updateFinalDispositionFieldsForDeclinCase() {
		ApplicationInfo applicationInfo = getNbaTxLife().getPolicy().getApplicationInfo();
		applicationInfo.setHOCompletionDate(new Date());
		applicationInfo.setActionUpdate();
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
		if (applicationInfoExtension != null) {
			applicationInfoExtension.setUnderwritingStatus(NbaOliConstants.OLI_POLSTAT_WITHDRAW);
			if (applicationInfoExtension.getReopenDate() != null) {
				applicationInfoExtension.deleteReopenDate();				
			}
			applicationInfoExtension.setActionUpdate();
		}		
		NbaLob lob = getWork().getNbaLob();
		lob.setCaseFinalDispstn((int) NbaOliConstants.OLI_POLSTAT_WITHDRAW);		
	}
	
	/**
	 * This method creates Validation Error WorkItem if any of the SystemMessage object has message severity 2 or 4.
	 * @throws NbaBaseException
	 */	
	protected void createValErrorWorkItem() throws NbaBaseException {
		if (!isAnyOpenErrWI(A_WT_NBVALDERR)) {
			Map deOink = new HashMap();	
			NbaLob lob = getWork().getNbaLob();
			deOink.put("A_CreateValidationWI", "true");			
			NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), nbaTxLife, deOink);
			NbaTransaction nbaTrans = getWork().addTransaction(workProvider.getWorkType(), workProvider.getInitialStatus());
			String caseManagerLOB = lob.getCaseManagerQueue();
			if (caseManagerLOB != null && !CONTRACT_DELIMITER.equalsIgnoreCase(caseManagerLOB)) {
				nbaTrans.getNbaLob().setCaseManagerQueue(caseManagerLOB);
			}
			// Start APSL3118 SI Icon Changes
			if(!NbaUtils.isBlankOrNull(lob.getApplicationType()) ){
				nbaTrans.getNbaLob().setApplicationType(lob.getApplicationType());
			}else{
				nbaTrans.getNbaLob().setApplicationType(String.valueOf(NbaOliConstants.OLI_APPTYPE_SIMPLIFIEDISSUE));
			}//	End APSL3118 SI Icon Changes
			//Begin APSL4576 
			if(!NbaUtils.isBlankOrNull(lob.getSalesChannel())){
				nbaTrans.getNbaLob().setSalesChannel(lob.getSalesChannel()); 
			}
			//APSL4576 end 
		}
	}
	
	/**
	 * - If Case is entered from portal and has no agent information,
	 *	 move the Case to the next queue.
	 * - In other scenario Check for Invalid Agent. If true, Create an 
	 *	 agent problem work item with the status "AGTNOTLICD".
	 * @return boolean true when the case is entered from portal and has no agent 
	 * 			information or when the agent is invalid. 
	 * 			false indicating nbProducer database need not be updated.
	 * @throws NbaBaseException
	 */
	protected void processAgent(ArrayList messages) throws NbaBaseException {
		NbaLob lob = getWork().getNbaLob();
		if (lob.getPortalCreated() && lob.getAgentID() == null) {
			return;
		}

		if (NbaUtils.hasSignificantValErrors(messages, SUBSET_SI_AGENT)) { // APSL3236
			if (!isAnyOpenErrWI(A_WT_AGENT_LICENSE)) { // APSL3236
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), new HashMap());
				NbaTransaction nbaTrans = getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());			
				nbaTrans.getTransaction().setAction("L");
				nbaTrans.increasePriority(provider.getWIAction(), provider.getWIPriority());
				NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
				npec.setActionAdd();
				npec.setOriginator(getUser().getUserID());
				npec.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
				npec.setProcess(getWork().getQueue());
				npec.setText("Agent not licensed");
				nbaTrans.addManualComment(npec.convertToManualComment());
				
				String licenseCaseManagerLOB = lob.getLicCaseMgrQueue();
				if (licenseCaseManagerLOB != null && !CONTRACT_DELIMITER.equalsIgnoreCase(licenseCaseManagerLOB)) {
					nbaTrans.getNbaLob().setLicCaseMgrQueue(licenseCaseManagerLOB);
				}
				nbaTrans.getNbaLob().setLastName(lob.getLastName());
				nbaTrans.getNbaLob().setFirstName(lob.getFirstName());
				nbaTrans.getNbaLob().setSpecialCase(lob.getSpecialCase());
				nbaTrans.getNbaLob().setApplicationType(lob.getApplicationType());//	APSL3118 SI Icon Changes
				//Begin APSL4576 
				if(!NbaUtils.isBlankOrNull(lob.getSalesChannel())){
					nbaTrans.getNbaLob().setSalesChannel(lob.getSalesChannel()); 
				}
				//APSL4576 end 
			}
			if (getSuspendVO() == null) { // APSL3236
				setSuspendVO(getSuspendWorkVO(Calendar.MINUTE, MIN));
			}						
		}		
	}
	
	protected void processValErr(ArrayList messages) throws NbaBaseException {
		if (isSeverityExists(messages)) {
			createValErrorWorkItem();
			if (getSuspendVO() == null) {
				setSuspendVO(getSuspendWorkVO(Calendar.MINUTE, MIN));				
			}
		}
	}
	
	/**
	 * Unlocks workitems
	 * @throws NbaBaseException
	 */
	protected void unlockWorkItems() throws NbaBaseException {
		NbaContractLock.removeLock(getUser());
		if (getParentWork() != null) {
			unlockWork(getParentWork());
		} else {
			unlockWork();
		}
	}
	
	/**Takes activate minutes for a workitem, and returns suspendVO object for this work item.
	 * @throws NbaBaseException
	 */
	protected NbaSuspendVO getUnsuspendWorkVO() throws NbaBaseException {
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		setParentWork(retrieveParentWork(work, true, false));
		suspendVO.setCaseID(getParentWork().getID());
		return suspendVO;
	}
	
	/**Takes activate minutes for a workitem, and returns suspendVO object for this work item.
	 * @throws NbaBaseException
	 */
	protected NbaSuspendVO getSuspendWorkVO(int type, int value) {
		GregorianCalendar cal = new GregorianCalendar();
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		cal.setTime(new Date());
		cal.add(type, value);
		suspendVO.setTransactionID(getWork().getID());
		suspendVO.setActivationDate(cal.getTime());
		//addComment("Suspended to allow for contract aggregation");//ALNA212
		return suspendVO;
	}
	
	/**
	 * Commit any changes to the work flow system
	 * @param hasErrors the error indicator 
	 * @param suspendItem the NbaSuspendVo for work item
	 * @throws NbaBaseException
	 */
	protected void updateCaseAndTransactions() throws NbaBaseException {
		update(getWork());
		if (getSuspendVO() != null && getWork().isTransaction()) {			
			unsuspendWork(getSuspendVO());	
		} else if (getSuspendVO() != null) {
			addComment("Case suspended due to unresolved CVs");
			suspendWork(getUser(), getSuspendVO());
		}
		unlockWorkItems();
	}
	
	protected boolean isAnyOpenErrWI(String workType) throws NbaBaseException {
		List transactions = getWork().getTransactions(); // get all the transactions
		Iterator transactionItr = transactions.iterator();
		WorkItem workItem;
		while (transactionItr.hasNext()) {
			workItem = (WorkItem) transactionItr.next();
			if (workType.equals(workItem.getWorkType()) && !END_QUEUE.equals(workItem.getQueueID())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the parent workitem 
	 * @return
	 */
	protected NbaDst getParentWork() {
		return parentWork;
	}

	/**
	 * Sets the parent workitem
	 * @param newWork the parent workitem
	 */
	protected void setParentWork(NbaDst newWork) {
		parentWork = newWork;
	}
	
	/**
	 * Retrieve the parent case with lock and all its children without taking lock
	 * @return the parent case with all children
	 */
	protected void retrieveCaseWithTransactions() throws NbaBaseException {
		//create and set parent case retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		// if case
		if (getWork().isCase()) {
			retOpt.setWorkItem(getWork().getID(), true);
			retOpt.requestTransactionAsChild();
			retOpt.setLockWorkItem();
		} 
		//get case from awd
		setWork(retrieveWorkItem(getUser(), retOpt));
	}
	
	//Prior Insurance changes
	public static String checkCvExistOnHitCase(List systemMessages, long subset) {
		SystemMessage systemMessage;
		SystemMessageExtension msgExt;
		int count = (systemMessages == null) ? 0 : systemMessages.size();
		for (int i = 0; i < count; i++) {
			systemMessage = (SystemMessage) systemMessages.get(i);
			msgExt = NbaUtils.getFirstSystemMessageExtension(systemMessage);
			if (!NbaUtils.isDeleted(systemMessage) && msgExt != null && msgExt.getMsgValidationType() == subset
					&& systemMessage.getMessageDescription() != null) {
				String desc = systemMessage.getMessageDescription();
				//APSL2977 Start
				// SR787006-APSL3702 if else condition deleted
				if ((desc.indexOf("Hit") > -1)) {
					return "HitDeclined";
				} else if (desc.indexOf("Try") > -1) {
					return "TryDeclined";
				}				
				//APSL2977 End
			}
		}
		return "";
	}
}	
