package com.csc.fs.accel.async;
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


import commonj.work.WorkException;
import commonj.work.WorkItem;
/**
 * Instance of this class identifies submitted Jobs for asynchronous processing
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA208-9</td><td>Version 7</td><td>Asynchronous Services</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AccelWorkItem {

    private WorkItem workItem = null; 
    
    /**
     * Constructor wrapping the submitted Job
     * @param _workItem Instances of Submitted WorkItem
     */
    public AccelWorkItem(WorkItem _workItem) {
        workItem = _workItem;
    }
    
    /**
     * Answers the result from the job execution
     * @return result of Job execution
     * @throws AsynchronousServiceFailureException
     */
    public AccelWork getResult() throws AsynchronousServiceFailureException {
        try {
            return new AccelWork(workItem.getResult());
        } catch (WorkException e) {
            throw new AsynchronousServiceFailureException(e);
        }
    }
   
    /**
     * Answers the status of the submitted job
     * @return status
     */
    public int getStatus() {
        return workItem.getStatus();
    }
}
