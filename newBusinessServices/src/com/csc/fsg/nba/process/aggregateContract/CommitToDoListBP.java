package com.csc.fsg.nba.process.aggregateContract;

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
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaToDoListVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaWorkCompleteRequest;

/**
 * Performs Work Complete, Lock and Unlock operations on  work items.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA251</td><td>Version 8</td><td>nbA Case Manager and Companion Case Assignment</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class CommitToDoListBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaToDoListVO toDoListVO = ((NbaToDoListVO) input);
			List lockedItems = toDoListVO.getLockedWorkItems();
			if (toDoListVO.isLockWork()) {
				result = lockWork(toDoListVO.getWorkItemID(), toDoListVO.getNbaUserVO());
			} else if (toDoListVO.isCompleteWork()) {
				NbaWorkCompleteRequest workCompleteRequest = toDoListVO.getWorkCompleteRequest();
				if(workCompleteRequest != null){
					result = workComplete(workCompleteRequest);
				}
			} else if (toDoListVO.isUnlockAllWorkItems()) {
				result = unlockAllWorkItems(lockedItems, toDoListVO.getNbaUserVO());
			}
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
			if (e instanceof NbaLockedException) {
				result.addResult(e);
			}
		}
		return result;
	}

    /**
     * Sets NbaAwdRetrieveOptionsVO request object for a work item and calls 
     * NbaRetrieveWorkBP business service to lock the work item.
     * @param workId
     * @param user
     * @return AccelResult
     */	
	protected AccelResult lockWork(String workId, NbaUserVO user) {
		NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();
		options.setWorkItem(workId, false);
		options.setNbaUserVO(user);
		options.setLockWorkItem();
		return (AccelResult) callBusinessService("NbaRetrieveWorkBP", options);
	}

	/**
	 * Calls WorkCompleteBP business service for a work item, and also calls NbaUnlockWorkBP
	 * business service to unlock the work item after work complete.
	 * @param workCompleteRequest
	 * @return AccelResult
	 */
	protected AccelResult workComplete(NbaWorkCompleteRequest workCompleteRequest) {
        AccelResult accelResult = null;
        NbaSuspendVO request = new NbaSuspendVO();
        request.setNbaUserVO(workCompleteRequest.getNbaUserVO());
        request.setRetrieveWorkItem(false); //APSL5055-NBA331.1 It is already locked.
        if (workCompleteRequest.getNbaDst().isCase()) {
            request.setCaseID(workCompleteRequest.getNbaDst().getID());
        } else {
            request.setTransactionID(workCompleteRequest.getNbaDst().getID());
        }
        accelResult = (AccelResult) callBusinessService("NbaUnsuspendWorkBP", request);
        if (!accelResult.hasErrors()) {
            accelResult = (AccelResult) callBusinessService("WorkCompleteBP", workCompleteRequest);
            if (!accelResult.hasErrors()) {
	            if (!workCompleteRequest.getNbaDst().isCase()) {
	                accelResult = (AccelResult) callBusinessService("NbaUnlockWorkBP", workCompleteRequest.getNbaDst());
		            //start ALII1816
	                NbaDst work = workCompleteRequest.getNbaDst();
	                NbaDst parentCase = null;
		            if(work.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_AGENT_LICENSE) || work.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_NBVALDERR)
							|| work.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REPL_NOTIFICATION) ) {
						NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
						retOpt.setWorkItem(work.getID(), false);
						retOpt.requestCaseAsParent();
						//ALII2041 code deleted
						retOpt.setLockParentCase();
						retOpt.setNbaUserVO(workCompleteRequest.getNbaUserVO());
						accelResult = (AccelResult) callBusinessService("NbaRetrieveWorkBP", retOpt);
						parentCase = (NbaDst) accelResult.getFirst();
						if(parentCase != null && parentCase.getQueue().equalsIgnoreCase(NbaConstants.A_QUEUE_CM_HOLD)) {
							parentCase.setNbaUserVO(workCompleteRequest.getNbaUserVO());
							workCompleteRequest.setNbaDst(parentCase);
							accelResult = (AccelResult) callBusinessService("WorkCompleteBP", workCompleteRequest);
						}
					}
		          //APSL4342 STARTS
		            if(!accelResult.hasErrors()){
		            	if(parentCase == null){
		            		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
							retOpt.setWorkItem(work.getID(),false);
							retOpt.requestCaseAsParent();
							retOpt.setLockParentCase();
							retOpt.setNbaUserVO(workCompleteRequest.getNbaUserVO());
							accelResult = (AccelResult) callBusinessService("NbaRetrieveWorkBP", retOpt);
							parentCase = (NbaDst) accelResult.getFirst();
		            	}
		            	if(parentCase!=null){
		            		String newToDoCount = NbaUtils.getNewToDOCount(parentCase,NbaConstants.DECREASECOUNT);
		            		if(!NbaConstants.QUESTIONMARK.equals(newToDoCount)){
		            			parentCase.getNbaLob().setToDoCount(newToDoCount);
						    	List children = parentCase.getWorkItem().getWorkItemChildren();
						    	parentCase.getWorkItem().setWorkItemChildren(new ArrayList());
						    	parentCase.setUpdate();
						    	parentCase.setNbaUserVO(workCompleteRequest.getNbaUserVO());
								accelResult = (AccelResult)callBusinessService("NbaUpdateWorkBP",parentCase);
								parentCase.getWorkItem().setWorkItemChildren(children);
		            		}
						}
		            }
		            //APSL4342 ENDS
	            //end ALII1816
	            }
            }
        }
        return accelResult;
    }

	/**
	 * Creates a list of work items to be unlocked, and then calls NbaUnlockWorkBP 
	 * business service to unlock the work item(s).
	 * @param workItems
	 * @param user
	 * @return
	 */
	protected AccelResult unlockAllWorkItems(List workItems, NbaUserVO user){
		return (AccelResult) callBusinessService("NbaUnlockWorkBP", workItems);
	}
}
