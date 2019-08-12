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

import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.result.RetrieveWorkResult;
import com.csc.fs.accel.valueobject.RetrieveWorkItemsRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * The Business Process class responsible for retrieving dst object from database
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>PERF-APSL324</td><td>AXA Life Phase1</td><td>PERF - Optimize RetrieveComments</td></tr>
 * <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaRetrieveWorkBP extends NewBusinessAccelBP {

	/*
	 * Calls NbaBusinessModelHelper.getCaseForWork() to retrieve dst object from database
	 * 
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		//NBA208-32
		NbaDst nbaDst = new NbaDst();
		try {
			// begin NBA153
			NbaAwdRetrieveOptionsVO retOptVO = (NbaAwdRetrieveOptionsVO) input;
			NbaUserVO userVO = retOptVO.getNbaUserVO();
			RetrieveWorkItemsRequest request = new RetrieveWorkItemsRequest();
	        request.setUserID(userVO.getUserID());
	        request.setWorkItemID(retOptVO.getWorkItemId());
	        request.setTransactionAsChildIndicator(retOptVO.isTransactionAsChildRequested());
	        request.setLockTransactionIndicator(retOptVO.isLockTransactionSet());
	        request.setCaseAsParentIndicator(retOptVO.isCaseAsParentRequested());
	        request.setLockParentCaseIndicator(retOptVO.isLockParentCaseSet());
	        request.setTransactionAsSiblingIndicator(retOptVO.isTransactionAsSiblingRequested());
	        request.setLockSiblingTransactionIndicator(retOptVO.isLockSiblingTransactionSet());
	        request.setHistoryIndicator(retOptVO.isHistoryRequested());
	        request.setSourcesIndicator(retOptVO.isSourceRequested());
	        request.setLockWorkItemIndicator(retOptVO.isLockWorkItemSet());
	        request.setRetrieveLOBIndicator(retOptVO.isRetrieveLOBIndicator()); //PERF - APSL324
	        request.setSystemName(WorkflowServiceHelper.getSystemName());  //APSL5055-NBA331	        
	        AccelResult hieracrhyresult = (AccelResult)callBusinessService("RetrieveHierarchyBP", request);
	        if (hieracrhyresult.hasErrors()){ 
	            return hieracrhyresult;	 
	        }	 
	        
	        RetrieveWorkResult retrieveWorkResult = (RetrieveWorkResult)hieracrhyresult;
	        //NBA208-32
	        WorkItem aCase = null;
	        List transactions = new ArrayList();
	        List items = retrieveWorkResult.getWorkItems();
	        for (int i = 0; i < items.size(); i++) {
	            WorkItem workitem = (WorkItem) items.get(i);
	            if (workitem.getRecordType().equals(CASERECORDTYPE)) {
	            	//NBA208-32
	                aCase = workitem;
                    if (workitem.getItemID().equals(retOptVO.getWorkItemId())) {
                    	if(!(retOptVO.isTransactionAsSiblingRequested() || retOptVO.isCaseAsParentRequested())){
                    		aCase.setSelected(true);
                    	}
                    }
	            } else if (workitem.getRecordType().equals(TRANSACTIONRECORDTYPE)) {
	            	//NBA208-32
	                transactions.add(workitem);
                    if (workitem.getItemID().equals(retOptVO.getWorkItemId())) {
                    	if(aCase == null || (retOptVO.isTransactionAsSiblingRequested() || retOptVO.isCaseAsParentRequested())){
                    		workitem.setSelected(true);
                    	}
                    }
	            }
	        }
	        //NBA208-32
	        nbaDst.setUserID(userVO.getUserID());
	        if (aCase != null) {
	        	//NBA208-32
	            aCase.getWorkItemChildren().addAll(transactions);
	            nbaDst.addCase(aCase);
	        } else if(!transactions.isEmpty()){
	        	nbaDst.addTransaction((WorkItem)transactions.get(0));
	        }
	        //NBA208-32 code deleted
	        WorkflowServiceHelper.retrieveTextSources(nbaDst, retOptVO.isSourceRequested());
	        
	        result.addResult(nbaDst);
        } catch (Exception e) {
            addExceptionMessage(result, e);
            result.setErrors(true);
            return result;
		} 
        return result;
    }

}
