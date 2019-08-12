package com.csc.fsg.nba.process.workflow;

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

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.CompleteWorkRequest;
import com.csc.fs.accel.valueobject.WorkItemRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Unsuspends a work item using the <code>NbaNetServerAccessor</code>.
 * Requires an <code>NbaSuspendVO</code> with the appropriate case or transaction id set.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>PERF-APSL410</td><td>AXA Life Phase1</td><td>PERF - CommitRequirements optimization</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaUnsuspendWorkBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaSuspendVO suspendVO = (NbaSuspendVO)input;
			unSuspendOrActivateWork(suspendVO.getNbaUserVO(), suspendVO,  false);
		} catch (Exception e) {
			addExceptionMessage(result, e);
			return result;			
		}
		return result;
	}

    /**
     * Unsupend or Activate the designated work item
     * @param userVO user value object containing userId
     * @param suspendVO suspendVO which identifies work item to be unsuspended and the unsuspend parameters
     * @param activate unsuspends the workitem if the flag is false, otherwise activates it
     */
    protected void unSuspendOrActivateWork(NbaUserVO userVO, NbaSuspendVO suspendVO, boolean activate) throws NbaBaseException {
		WorkItemRequest workItemRequest = new WorkItemRequest();
		NbaAwdRetrieveOptionsVO retOptVO = new NbaAwdRetrieveOptionsVO();
		workItemRequest.setSystemName(getWorkflowSystemName());
	//	workItemRequest.setSystemName(suspendVO.getSystemName());   //NBA331-APSL5055
		boolean isAlreadyLocked = false;
		if (suspendVO.isRetrieveWorkItem()) { //PERF-APSL410
			retOptVO.setNbaUserVO(userVO);   //NBA331.1
          //   retOptVO.setSystemName(workItemRequest.getSystemName());   //NBA331.1-APSL5055
			if (suspendVO.getCaseID() != null) {
				workItemRequest.setWorkItemID(suspendVO.getCaseID());
				retOptVO.setWorkItem(workItemRequest.getWorkItemID(), true);
			//NBA331.1-APSL5055 code deleted	retOptVO.setNbaUserVO(userVO);
				AccelResult result = (AccelResult) callBusinessService("NbaRetrieveWorkBP", retOptVO);
				processResult(result);
				NbaDst dst = (NbaDst) result.getFirst();
				//NBA208-32
				isAlreadyLocked = (dst.getCase().getLockStatus() != null && !dst.getCase().getLockStatus().trim().equals(""));
				workItemRequest.setLocked(isAlreadyLocked); //NBLXA-1954
			} else {
				workItemRequest.setWorkItemID(suspendVO.getTransactionID());
				retOptVO.setWorkItem(workItemRequest.getWorkItemID(), false);
				//NBA331.1-APSL5055 code deleted retOptVO.setNbaUserVO(userVO);
				AccelResult result = (AccelResult) callBusinessService("NbaRetrieveWorkBP", retOptVO);
				processResult(result);
				NbaDst dst = (NbaDst) result.getFirst();
				//NBA208-32
				isAlreadyLocked = (dst.getTransaction().getLockStatus() != null && !dst.getTransaction().getLockStatus().trim().equals(""));
			}
			//Begin PERF-APSL410
		} else {
			isAlreadyLocked = true;
			workItemRequest.setLocked(true);
			if (suspendVO.getCaseID() != null) {
				workItemRequest.setWorkItemID(suspendVO.getCaseID());
			} else {
				workItemRequest.setWorkItemID(suspendVO.getTransactionID());
			}
		}
		//End PERF-APSL410
		workItemRequest.setUseActivation(new Boolean(activate)); // just unsuspend
		AccelResult result = (AccelResult) callBusinessService("UnsuspendWorkBP", workItemRequest);
		try {
			processResult(result);
		} catch (NbaBaseException e) {
			if (e.getMessage().startsWith(A_ERR_NOWORK)) {
				return;
			}
			throw e;
		}
		if (!isAlreadyLocked) {
			// Unlock work Item
			CompleteWorkRequest completeWorkRequest = new CompleteWorkRequest();
			completeWorkRequest.setSystemName(workItemRequest.getSystemName()); //NBA331-APSL5055
			completeWorkRequest.setSystemName(getWorkflowSystemName());
			completeWorkRequest.setStatus(EMPTYSTRING);
			completeWorkRequest.setWorkItemID(workItemRequest.getWorkItemID());
			AccelResult unlockresult = (AccelResult) callBusinessService("StandardFullCompleteWorkBP", completeWorkRequest);
			try {
				processResult(unlockresult);
			} catch (NbaBaseException e) {
				throw e;
			}
		}
	}
}
