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
import com.csc.fs.accel.result.GetWorkResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemRequest;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Updates a work item using the <code>NbaNetServerAccessor</code>.  Requires an <code>NbaDst</code>
 * as input.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 *  <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaGetWorkBP extends NewBusinessAccelBP {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
        AccelResult result = new AccelResult(); 
        //NBA208-32
        NbaDst nbaDst = new NbaDst();
        try {
            NbaUserVO userVO = (NbaUserVO) input;
            WorkItemRequest workItemRequest = new WorkItemRequest();
            if (userVO.isAutomatedProcess()) {
                workItemRequest.setJobName(A_JOB_AUTOPROCESSGETWORK);
            } else {
                workItemRequest.setJobName(A_JOB_GETWORK);
            }
            Result res = callBusinessService("GetFullWorkBP", workItemRequest);
            if (res.hasErrors()) {
                return res;
            }
            //NBA208-32 code deleted
            if (res instanceof GetWorkResult) {
                GetWorkResult getWorkResult = (GetWorkResult) res;
                WorkItem workitem = getWorkResult.getWorkItem();
                //NBA208-32 code deleted
                nbaDst.setUserID(workitem.getAwduser());
                if (workitem.getRecordType().equals(CASERECORDTYPE)) {
                	//NBA208-32
                	nbaDst.addCase(workitem);
                } else if (workitem.getRecordType().equals(TRANSACTIONRECORDTYPE)) {
                	//NBA208-32
                	nbaDst.addTransaction(workitem);
                }
            }
            //NBA208-32 code deleted
            if(LogHandler.Factory.isLogging(LogHandler.LOG_DEBUG)) { //SPR3290
                LogHandler.Factory.LogDebug(this, nbaDst.toString()); 
            } //SPR3290
            result.addResult(nbaDst);
        } catch (Throwable e) { //APSL5055-NBA331.1
        	e.printStackTrace();    //APSL5055-NBA331.1
            addExceptionMessage(result, e);
            result.addResult(e);
            return result;
        }
        return result;
    }
}
