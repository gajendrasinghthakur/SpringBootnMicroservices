                                              package com.csc.fsg.nba.process.assignQueues;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.LockRetrieveWorkRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.bean.accessors.NbaCompanionCaseFacadeBean;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.contract.CommitContractBP;
import com.csc.fsg.nba.vo.AxaReassignDataVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaReassignmentQueuesVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Policy;

/**
 * Accepts an <code>NbaReassignmentQueuesVO</code> as input to update a work item 
 * with the new underwriter and Case Manager queues.
 * <p>
 * Additionally, all work items attached to the case and currently in the underwriting
 * queue and Case Manager queue are locked and moved to new Queues. New queues are resolved from the
 * <b>AUTOPROCESSSTATUS<b> VP/MS model based on the work type and current Underwriter and Case Manager
 * queues.  If for any work item the new queues are not defined for this business function,
 * it will remain in its current queue.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA251</td><td>Version 8</td><td>nbA Case Manager and Companion Case Assignment</td></tr>
 * <tr><td>ALPC178</td><td>AXA Life Phase 1</td><td>User ID</td></tr>
 * <tr><td>ALS3216</td><td>AXA Life Phase 1</td><td>QC #1839 - Status name doesn't match queue name</td></tr>
 * <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class CommitReassignmentQueuesBP extends CommitContractBP {

	public static final String CASE = "C";
	public static final String TRANS = "T";
	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result;
		try {
			NbaReassignmentQueuesVO reqeue = (NbaReassignmentQueuesVO) input;
			//Begin NBLXA-2472
		    if(!reqeue.isFromCommit()){
				result = updateWork((NbaReassignmentQueuesVO) input);
			} else {
				NbaUserVO user = reqeue.getNbaUserVO();
				NbaTXLife nbaTxLife = reqeue.getNbaTXLife();
				String newuwq = reqeue.getNewUWQ();
				String newcmq = reqeue.getNewCMQ();
				String changedValue = "";
				changedValue = "UWQ=" + newuwq+",CMQ=" + newcmq+",TERM=" + reqeue.isTermExpressTeam()+",COMP=" + reqeue.isApplyToAllCompanionCases();
				AxaReassignDataVO reassignVo = new AxaReassignDataVO();
				Policy policy = nbaTxLife.getPolicy();
				reassignVo.setPolicynumber(policy.getPolNumber());
				reassignVo.setCompanyKey(policy.getCompanyKey());
				reassignVo.setBackendKey(policy.getBackendKey());
				reassignVo.setChangedType(NbaConstants.JOB_UWCM_REASSIGNMENT);
				reassignVo.setUserCode(user.getUserID());
				reassignVo.setStatus("Active");
				reassignVo.setChangedValue(changedValue);
				String msg = "Reassignment Initiated";
				NbaSystemDataDatabaseAccessor.insertReassingmentProcessing(reassignVo);
				addComments(user, reqeue.getNbaDst(), msg);
				result = doWorkUpdate(reqeue.getNbaDst(), user, false);
				return result;
			}
		 //End NBLXA-2472
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
			if (e instanceof NbaLockedException) {
				result.addResult(e);
			}
		}
		return result;
	}
	//Begin NBLXA-2472
	protected void addComments(NbaUserVO user, NbaDst nbaDst,String msg) {
		NbaProcessingErrorComment comment = new NbaProcessingErrorComment();
		comment.setText(msg);
		comment.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
		comment.setOriginator(user.getUserID());
		comment.setUserNameEntered(user.getUserID());
		comment.setActionAdd();
		nbaDst.addManualComment(comment.convertToManualComment());
	}
	 //End NBLXA-2472
	
	/**
	 * Responsible to update the work with Underwriter and Case Manager queue values, and also to process companion cases.
	 * @param req
	 * @return
	 * @throws NbaBaseException
	 */
	protected AccelResult updateWork(NbaReassignmentQueuesVO req) throws NbaBaseException {
		// only move the case when the current Queue matches the old Queue
		NbaUserVO user = req.getNbaUserVO();
		NbaDst work = req.getNbaDst();
		NbaLob lob = work.getNbaLob();
		List lockedCases = new ArrayList();
		int listSize;
		//Get the current Underwriter and Case Manager Queues for the case
		String oldUWQ = lob.getUndwrtQueue();
		String oldCMQ = lob.getCaseManagerQueue();
		//Get the new Underwriter and Case Manager Queues for the case
		String newUWQ = req.getNewUWQ();
		String newCMQ = req.getNewCMQ();
		boolean isTermExpTeamUW=req.isTermExpressTeam();//NBLXA-186
		String termExpInd=lob.getDisplayIconLob();//NBLXA-186
		AccelResult lockResult = null;
		AccelResult updateResult = null;
		AccelResult unlockResult = null;

		//if user has selected "Apply to all companion cases" option
		if (req.isApplyToAllCompanionCases()) {
			NbaCompanionCaseVO vo;
			NbaCompanionCaseFacadeBean bean = new NbaCompanionCaseFacadeBean();
			List companionCases = bean.retrieveCompanionCases(user, work, true);
			int count = companionCases.size();
			NbaDst dst = null;
			
			//loop all the companion cases, add locked cases (excluding the current work) into a list
			for (int i = 0; i < count; i++) {
				vo = (NbaCompanionCaseVO) companionCases.get(i);
				dst= vo.getNbaDst();
				if (dst != null) {
					dst.setNbaUserVO(user); //NBLXA-2472
					if (!dst.isLocked(user.getUserID())) {
						callBusinessService("NbaUnlockWorkBP", lockedCases);//unlock all locked items
						lockResult = new AccelResult();
						lockResult.setErrors(true);
						lockResult.addResult(new NbaLockedException());
						return lockResult;
					} else if (!lob.getPolicyNumber().equalsIgnoreCase(vo.getContractNumber())) { //current work should not be added
						
						lockedCases.add(dst);
					}
				}
			}
			
			listSize = lockedCases.size();
			NbaDst compCase = null;
			NbaLob compCaseLob = null;
			//iterate over locked companion cases and assign new Underwriter and/or Case Manager queue
			for (int i = 0; i < listSize; i++) {
			    compCase= (NbaDst) lockedCases.get(i);
			    compCaseLob= compCase.getNbaLob();
				if (! NbaConstants.EMPTY_LOB_STR.equals(newUWQ)){
					compCaseLob.setUndwrtQueue(newUWQ);
				}
				if (! NbaConstants.EMPTY_LOB_STR.equals(newCMQ)) {
					compCaseLob.setCaseManagerQueue(newCMQ);
				}
				compCase.setUpdate();
				
				//change status only if the companion case is in currently assigned Underwriter queue
				lockResult = (AccelResult) callBusinessService("RetrieveTXLifeBP", compCase); //ALPC178
				if (compCase.getQueue().equals(oldUWQ)) {
				    //ALPC178 code deleted
					if (!lockResult.hasErrors()) {
						setStatus(compCase, NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, (NbaTXLife) lockResult.getFirst());
					}
				}
				//begin ALPC178
				if (!lockResult.hasErrors()) {
					persistContract( (NbaTXLife) lockResult.getFirst(), compCase, user);
				}
				compCase = getTransactions(compCase, compCaseLob);
				compCase = assignQueues(compCase, oldUWQ, oldCMQ, newUWQ, newCMQ);
				updateResult = doWorkUpdate(compCase, user, true);
				if(updateResult.hasErrors()){
				    return updateResult;
				}
			}
			unlockResult = (AccelResult)callBusinessService("NbaUnlockWorkBP", lockedCases);//unlock all locked items
			if(unlockResult.hasErrors()){
			    return unlockResult;
			}
		}

		// Update UNDQ and CSMQ LOB for the current work, to new Underwriter and Case Manager Queues
		if (! NbaConstants.EMPTY_LOB_STR.equals(newUWQ)) {
			lob.setUndwrtQueue(newUWQ);
		}
		if (! NbaConstants.EMPTY_LOB_STR.equals(newCMQ)) {
			lob.setCaseManagerQueue(newCMQ);
		}
		//Begin - NBLXA-186
		if (!isTermExpTeamUW && ("1").equals(termExpInd)) {
			lob.setDisplayIconLob("0");
			if (null != req.getNbaTXLife()) {
				ApplicationInfo appInfo = req.getNbaTXLife().getPolicy().getApplicationInfo();
				if (null != appInfo) {
					ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(appInfo);
					appInfoExtn.setTermExpressInd(false);
				}
			}
		}
		//End NBLXA-186
		work.setUpdate();
		
		//change status only if the case is in currently assigned Underwriter queue
		if (work.getQueue().equals(oldUWQ)) {
			setStatus(work, NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, req.getNbaTXLife());
		}
		updateContract(req.getNbaTXLife(),work,user); //ALPC178
		work = getTransactions(work, lob);
		work = assignQueues(work, oldUWQ, oldCMQ, newUWQ, newCMQ);
		updateResult = doWorkUpdate(work, user, false);
		if(updateResult.hasErrors()){
		    return updateResult;
		}
		

		unlockResult = (AccelResult)callBusinessService("NbaUnlockWorkBP", work);//unlock all locked transactions of current work item
		return unlockResult;
	}

	/**
	 * 
	 * Use the <code>NbaProcessStatusProvider</code> to determine the work item's next status. If a new status is returned back from the VP/MS
	 * model, then only update the transaction's status.
	 * 
	 * @param work
	 * @throws NbaBaseException
	 */
	protected void setStatusOnly(NbaTransaction transaction, String userID) throws NbaBaseException {
		NbaUserVO user = new NbaUserVO(userID, "");
		NbaProcessStatusProvider provider = new NbaProcessStatusProvider(user, transaction.getNbaLob());
		if (provider != null && provider.getPassStatus() != null && !provider.getPassStatus().equals(transaction.getStatus())) {
			transaction.setStatus(provider.getPassStatus());
			NbaUtils.setRouteReason(transaction,provider.getPassStatus());//ALS3216
		}
	}

	/**
	 * Retrieves a list of transactions located in the same queue as the case and
	 * appends them to the current case.
	 * @param work
	 * @param workLob
	 * @param queue
	 * @return
	 */
	protected NbaDst getTransactions(NbaDst work, NbaLob workLob) throws NbaBaseException {
		LockRetrieveWorkRequest request = new LockRetrieveWorkRequest();
		//setup the search lobs
		NbaLob tempLob = new NbaLob();
		tempLob.setCompany(workLob.getCompany());
        //Begin APSL1354
		if (workLob.getPolicyNumber() != null) {
			tempLob.setPolicyNumber(workLob.getPolicyNumber());
		} else {
			throw new NbaBaseException(NbaBaseException.NO_POLNUMBER);
		}		
		//End APSL135
		tempLob.setPolicyNumber(workLob.getPolicyNumber());
		request.setBusinessArea(workLob.getBusinessArea());
		request.setLobData((LobData[]) tempLob.getLobs().toArray(new LobData[tempLob.getLobs().size()]));
		request.setWorkItem(work.getCase());
		request.setRetrieveWorkLocked(true);
		request.setPageNumber("1");   //APSL5055-NBA331.1
		AccelResult result = (AccelResult) callBusinessService("LockRetrieveWorkBP", request);
		if (!processResult(result)) {
			work.addCase((WorkItem) result.getFirst());
		}
		return work;
	}

	protected AccelResult retrieveCase(String workId, NbaUserVO user) {
		NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();
		options.setWorkItem(workId, true);
		options.setNbaUserVO(user);
		return (AccelResult) callBusinessService("NbaRetrieveWorkBP", options);
	}

	protected NbaDst assignQueues(NbaDst parentCase, String oldUWQ, String oldCMQ, String newUWQ, String newCMQ) throws NbaBaseException {

		// move all the transactions attached with the case to new Queue, which
		// are in current Underwriting queue. The new queue is determined on the basis
		// of the work type and the current Underwriter queue and is resolved from the AUTOPROCESSSTATUS VP/MS model
		// If for any workitem the new queue is not defined for this business function. It
		// will remain in its queue.
		List list = parentCase.getTransactions();
	    Iterator itr = list.iterator();
		NbaLob transactionLob = null;
		NbaTransaction transaction = null;
		while (itr.hasNext()) {
			transaction = new NbaTransaction((WorkItem) itr.next());
			transactionLob= transaction.getNbaLob();
			if(! NbaConstants.EMPTY_LOB_STR.equals(newUWQ)){
				transactionLob.setUndwrtQueue(newUWQ);
				if (transaction.getQueue().equals(oldUWQ)) {
					setStatusOnly(transaction, NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT);
				}
			}
			if(! NbaConstants.EMPTY_LOB_STR.equals(newCMQ)){
				transactionLob.setCaseManagerQueue(newCMQ);
				if (transaction.getQueue().equals(oldCMQ)) {
					setStatusOnly(transaction, NbaConstants.PROC_VIEW_CASEMANAGER_QUEUE_REASSIGNMENT);
				}
			}
			transaction.setUpdate();
		}
		return parentCase;
	}
	//ALPC178 new method
	protected void persistContract(NbaTXLife nbaTXlife, NbaDst nbaDst, NbaUserVO user) {
		AccelResult result;
		WorkItem workitem = createWorkItem(nbaDst);
		result = (AccelResult) lockContract(workitem);
		boolean lockcontract = false;
		if (!result.hasErrors()) {
			lockcontract = true;
			result.mergeResults(this.updateContract(nbaTXlife, nbaDst, user));
		}
		if (lockcontract) { //contract is locked
			result.mergeResults(unlockContract(workitem));
		}
	}
	//ALPC178 new method
	protected WorkItem createWorkItem(NbaDst nbaDst) {
		WorkItem selectedWorkItem = new WorkItem();
        selectedWorkItem.setLobData(nbaDst.getNbaLob().getLobs());
        selectedWorkItem.setRecordType(nbaDst.getWorkType());
        selectedWorkItem.setItemID(nbaDst.getID());
        selectedWorkItem.setRecordType(nbaDst.isCase() ? CASE : TRANS);  
        return selectedWorkItem;
	}
	//ALPC178 new method
	protected Result lockContract(WorkItem selectedWorkItem) {

		return callBusinessService("ContractLockBP", selectedWorkItem);
	}
	//ALPC178 new method
	protected Result unlockContract(WorkItem selectedWorkItem) {

		return callBusinessService("ContractUnlockBP", selectedWorkItem);
	}
	//ALPC178 new method
	protected Result updateContract(NbaTXLife contract, NbaDst work, NbaUserVO user) {
		AccelResult result = null;
		try {
		if (contract != null) {
			contract.setBusinessProcess("REASSIGNMENT_QUEUE");
			contract = doContractUpdate(contract, work, user);
			if (contract.isTransactionError()) {
				result =  processErrors(contract);				
				}
		}
		} catch (NbaBaseException nbe) {
			result = new AccelResult();
			addExceptionMessage(result, nbe);
		}
		return result;
	}
}

