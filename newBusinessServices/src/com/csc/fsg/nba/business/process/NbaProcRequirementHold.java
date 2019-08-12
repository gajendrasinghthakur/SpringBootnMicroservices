package com.csc.fsg.nba.business.process;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */

/**
 * NbaProcRequirementHold is the class to process cases and Requiements found in N2REQHLD queue.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SR566149 and SR519592</td><td>Discretionary</td><td>Reissue and Delivery Requirement Follow Up</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1001
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version NB-1001
 */
public class NbaProcRequirementHold extends NbaAutomatedProcess {

	protected NbaDst parent = null;
	private List transactionWI = null;

	/**
	 * NbaProcRequirementHold constructor comment.
	 */
	public NbaProcRequirementHold() {
		super();
	}

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		if (!initialize(user, work)) {
			return getResult();
		}
		if (getResult() == null) {
			doProcess();
			setTransactionWI(null);
		}
		return getResult();
	}

	protected void doProcess() throws NbaBaseException {
		if (isContractPrinted()) {
			// NBLXA-1379 Begin
			if (!getWork().isCase()) {
				NbaDst parentDst = getParent();
				if (parentDst.getQueue().equals(NbaConstants.QUEUE_REQHLD)) {
					NbaDst transactioDst = getWork();
					setWork(parentDst);
					if (parentDst.isSuspended()) {
						NbaSuspendVO suspendVO = new NbaSuspendVO();
						suspendVO.setCaseID(parentDst.getID());
						unsuspendWork(suspendVO);
					}
					changeStatus(getPassStatus());
					// End NBLXA-1379
					updateWork();
					setWork(transactioDst);
				}
			}
			unsuspendAlltheRequirementInCurrentQueue();
			waiveRequirement(); //NBLXA-1611
			applicationWIProcessing();
			if (getResult() == null) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}
			// Begin NBLXA-1379
			if (getWork().isCase()) {
				changeStatus(getPassStatus());
			}
            else {
				changeStatus(getAlternateStatus());
			}
			// End NBLXA-1379
			doUpdateWorkItem();
			if (!getWork().isCase()) {
				unlockWork(getParent());
			}
			
		} else {
			suspendWork();
		}
	}
	
	
	
	private void applicationWIProcessing() throws NbaBaseException {
		ApplicationInfo applicationInfo = nbaTxLife.getPolicy().getApplicationInfo();
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
		if (applicationInfoExtension != null) {
			applicationInfoExtension.setPartialReqInd(false);
			applicationInfoExtension.setActionUpdate();
		}
		doContractUpdate();		
	}
	
	private void unsuspendAlltheRequirementInCurrentQueue() throws NbaBaseException {
		List requirementInReqhld = searchAndRetrieveRequirements();
		NbaDst dst = getWork();
		if (requirementInReqhld.size() > 0) {
			NbaSuspendVO suspendVO = new NbaSuspendVO();
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			for (int index = 0; index < requirementInReqhld.size(); index++) {
				NbaTransaction reqWI = ((NbaTransaction) (requirementInReqhld.get(index)));
		  	     // Begin NBLXA-1379
				if (!dst.getID().equals(reqWI.getID())) {
					retOpt.setWorkItem(reqWI.getID(), false);
					retOpt.setLockWorkItem();
					NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
					setWork(aWorkItem);
					suspendVO.setTransactionID(aWorkItem.getID());
					if (reqWI.isSuspended()) {
						unsuspendWork(suspendVO);
					}
					changeStatus(getAlternateStatus());
					// End NBLXA-1379
					updateWork();
					unlockWork(aWorkItem);
				}
			}
		}
		setWork(dst);
	}
	
	//NBLXA-1611 New method
	private void waiveRequirement() throws NbaBaseException {
		List requirementsToWaive = searchAndRetrieveRequirementsForWaive();		
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		NbaDst dst = getWork();
		for (int index = 0; index < requirementsToWaive.size(); index++) {
			NbaTransaction reqWI = ((NbaTransaction) (requirementsToWaive.get(index)));
			retOpt.setWorkItem(reqWI.getID(), false);
			retOpt.setLockWorkItem();
			NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
			aWorkItem.getNbaLob().setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_WAIVED));
			setWork(aWorkItem);			
			changeStatus(A_STATUS_REQUIREMENT_WAIVED);			
			updateWork();
			unlockWork(aWorkItem);
			
			RequirementInfo requirementInfo = nbaTxLife.getRequirementInfo(aWorkItem.getNbaLob().getReqUniqueID());
			requirementInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_WAIVED);
			if(requirementInfo.hasReceivedDate()){
				requirementInfo.deleteReceivedDate();
			}
			RequirementInfoExtension reqInfoExt = requirementInfo.getOLifEExtensionAt(0).getRequirementInfoExtension();
			if(reqInfoExt!=null){
				reqInfoExt.deleteReviewedInd();
				if(reqInfoExt.hasReviewID()){
					reqInfoExt.deleteReviewID();
				}
				if(reqInfoExt.hasReviewDate()){
					reqInfoExt.deleteReviewDate();
				}
				reqInfoExt.setActionUpdate();
			}
			requirementInfo.setActionUpdate();
			addComment("Trusted Requirement Waived");
		}
		setWork(dst);
	}
	
	private List searchAndRetrieveRequirementsForWaive() throws NbaBaseException {
		List requirementsToWaive = new ArrayList();
		for (int i = 0; i < getTransactionWI().size(); i++) {
			NbaTransaction nbaTransaction = (NbaTransaction) getTransactionWI().get(i);
			if (nbaTransaction.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REQUIREMENT)
				 && nbaTransaction.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_TRUSTEDCONTACT				 
				 && nbaTransaction.getNbaLob().getReqStatus().equalsIgnoreCase(String.valueOf(NbaOliConstants.OLI_REQSTAT_ADD))){
				requirementsToWaive.add(nbaTransaction);
			}
		}		
		return requirementsToWaive;		
	}
	
	private List searchAndRetrieveRequirements(){
		List requirementInReqhld = new ArrayList();
		for (int i = 0; i < getTransactionWI().size(); i++) {
			NbaTransaction nbaTransaction = (NbaTransaction) getTransactionWI().get(i);
			if (nbaTransaction.getQueue().equalsIgnoreCase(NbaConstants.QUEUE_REQHLD)
				 && nbaTransaction.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REQUIREMENT)){
				requirementInReqhld.add(nbaTransaction);
			}
		}		
		return requirementInReqhld;		
	}	

	
	/**Check if there is at least one Print WI is not in end queue
	 * than return false otherwise return true 
	 * @return boolean
	 * @throws NbaBaseException
	 */
	protected boolean isContractPrinted() throws NbaBaseException {
		NbaDst caseWork = null;
		if (getWork().isCase()) {
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(getWork().getID(), true);
			retOpt.requestTransactionAsChild();
			try {
				caseWork = retrieveWorkItem(getUser(), retOpt);
			} catch (NbaNetServerException e) {
				throw new NbaBaseException("Unable to retrieve", e);
			}
		} else {
			caseWork = retrieveParentWork(getWork(), true, true, false);
			setParent(caseWork);
		}
		List transactions = caseWork.getNbaTransactions();
		for (int i = 0; i < transactions.size(); i++) {
			NbaTransaction nbaTransaction = (NbaTransaction) transactions.get(i);
			if (nbaTransaction.getWorkType().equalsIgnoreCase(NbaConstants.A_ST_CONTRACT_PRINT_EXTRACT)
					&& !(nbaTransaction.isInEndQueue() || nbaTransaction.getQueue().equalsIgnoreCase(NbaConstants.A_QUEUE_POST_ISSUE))) {
				return false;
			}
		}
		setTransactionWI(transactions);
		return true;
	}

	/**
	 * Suspend the case for 2 hours and Requirements for 4 hour
	 * AWD time is 60 minutes behind so 60 minutes suspension is actually 2 hours
	 * @throws NbaBaseException
	 */
	public void suspendWork() throws NbaBaseException {
		suspendVO = new NbaSuspendVO();
		int suspentionMinutes = 60;
		if (getWork().isCase()) {
			suspendVO.setCaseID(getWork().getID());
		} else {
			suspendVO.setTransactionID(getWork().getID());
			suspentionMinutes = 300;
		}
		getLogger().logDebug("Starting suspendCase");
		StringBuffer newReason = new StringBuffer();
		newReason.append("Case suspended due to ");
		newReason.append("Print work items is not in end queue "); //APSL1134
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, suspentionMinutes); //Suspend for 1 hr APSL1134
		suspendVO.setActivationDate(calendar.getTime());
		setSuspendVO(suspendVO);
		if (getWork().isCase()) { //ALII2071 start
			addComment(newReason.toString());
		} //ALII2071 end
		updateForSuspend();
		if (!getWork().isCase()) {
			unlockWork(getParent());
		}
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
	}

	/**
	 * @return Returns the parent.
	 */
	public NbaDst getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            The parent to set.
	 */
	public void setParent(NbaDst parent) {
		this.parent = parent;
	}

	/**
	 * @return the transactionWI
	 */
	public List getTransactionWI() {
		return transactionWI;
	}

	/**
	 * @param transactionWI the transactionWI to set
	 */
	public void setTransactionWI(List transactionWI) {
		this.transactionWI = transactionWI;
	}
	
	
}