package com.csc.fsg.nba.process.workflow;
/* 
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.result.RetrieveWorkResult;
import com.csc.fs.accel.valueobject.RetrieveWorkItemsRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.workflow.process.LockRetrieveWorkBP;
import com.csc.fsg.nba.vo.NbaDst;
/**
 * Retrieve all sources for a work item and its children
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Deferred Work Item Retrieval</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class RetrieveAllSourcesBP extends LockRetrieveWorkBP {

	public Result process(Object request) {
		//find the work item
		NbaDst nbaDst = (NbaDst) request;
		WorkItem workItem = nbaDst.getWorkItem();
		RetrieveWorkResult retrieveWorkResult = (RetrieveWorkResult) retrieveWorkItems(workItem);
		if (retrieveWorkResult.hasErrors()) {
			return retrieveWorkResult;
		}
		retrieveWorkResult.setWorkItems(checkForDuplicates(retrieveWorkResult.getWorkItems(), workItem, false));
		return attachChildren(workItem, retrieveWorkResult, true);

	}
    /**
     * Retrieve the sources and children sources 
     * @param nbaDst
     * @param includeTransactions
     */
    protected AccelResult retrieveWorkItems(WorkItem workItem) {
    	RetrieveWorkItemsRequest request = new RetrieveWorkItemsRequest();
    	request.setUserID(getCurrentUserId());
    	request.setWorkItemID(workItem.getItemID());
    	request.setSystemName(workItem.getSystemName());  //NBA331 NBLXA-2264
    	request.setSourcesIndicator(true);
    	if (workItem.isCase()) {
    		request.setTransactionAsChildIndicator(true);
    	} else {
    		request.setTransactionAsSiblingIndicator(true);
    	}
    	AccelResult hierarchyresult = (AccelResult) callBusinessService("RetrieveHierarchyBP", request);
    	
    	return hierarchyresult;
    }
}
