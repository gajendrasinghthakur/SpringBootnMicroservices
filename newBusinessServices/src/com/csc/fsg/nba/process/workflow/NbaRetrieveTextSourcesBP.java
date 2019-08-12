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
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;

/**
 * The Business Process class responsible for retrieving Text Sources from the database
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Deferred Work Item Retrieval</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaRetrieveTextSourcesBP extends NewBusinessAccelBP {

	/*
	 * 
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
        AccelResult result = new AccelResult();   
        WorkItem workItem = (WorkItem) input; //NBA208-36
        NbaDst nbaDst = new NbaDst(); //NBA208-36
       //NBA208-36 code deleted
        try {
        	nbaDst.setWorkItem(workItem); //NBA208-36
            WorkflowServiceHelper.retrieveTextSources(nbaDst, true); //NBA208-36
            result.addResult(nbaDst.getWorkItem()); //NBA208-36
        } catch (Exception e) {
            addExceptionMessage(result, e);
            result.setErrors(true);
        }
        return result;
    }

}
