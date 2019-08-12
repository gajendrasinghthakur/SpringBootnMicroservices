package com.csc.fs.accel.async;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import commonj.work.WorkException;
import commonj.work.WorkItem;
import commonj.work.WorkManager;
/**
 * AsynchronousProcessor implementation using commonJ.work API. 
 * @see http://vmgump.apache.org/gump/public/commonj/index.html  
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

public class AsynchronousProcessorCommonJImpl implements AsynchronousProcessor {

    /* (non-Javadoc)
     * @see com.csc.fs.accel.async.AsynchronousProcessor#submit(java.util.Collection)
     */
    public Result submit(Collection runnableCollection) {
        return submit(runnableCollection, null, WAIT_FOR_NONE,WorkManager.INDEFINITE);
    }

    /* (non-Javadoc)
     * @see com.csc.fs.accel.async.AsynchronousProcessor#submit(java.util.Collection, com.csc.fs.accel.async.AsynchronousProcessorListner)
     */
    public Result submit(Collection runnableCollection, AsynchronousProcessorListener listener) {
        return submit(runnableCollection, listener, WAIT_FOR_NONE,WorkManager.INDEFINITE);
    }

    /* (non-Javadoc)
     * @see com.csc.fs.accel.async.AsynchronousProcessor#submit(java.util.Collection, int)
     */
    public Result submit(Collection runnableCollection, int joinCondition) {
        return submit(runnableCollection, null, joinCondition,WorkManager.INDEFINITE);    
    }
    
    public Result submit(Collection runnableCollection, AsynchronousProcessorListener listener, int joinCondition) {
        return submit(runnableCollection, null, joinCondition,WorkManager.INDEFINITE);
    }

    /* (non-Javadoc)
     * @see com.csc.fs.accel.async.AsynchronousProcessor#submit(java.util.Collection, com.csc.fs.accel.async.AsynchronousProcessorListner, int)
     */
    public Result submit(Collection runnableCollection, AsynchronousProcessorListener listener, int joinCondition, long timeOut) {
        Result result = new AccelResult();
        try {
            // check if collection is not empty otherwise send back with message no runnable instance provided
            if (runnableCollection == null || runnableCollection.isEmpty()) {
                throw new AsynchronousServiceFailureException("Invalid Request ! No Work submitted for Asynchronous Processing");
            }
            // check if the join condition is proper otherwise throw error
            if (joinCondition != WAIT_FOR_ALL && joinCondition != WAIT_FOR_ANY && joinCondition != WAIT_FOR_NONE) {
                throw new AsynchronousServiceFailureException("Invalid Request ! Incorrect Join Condition");
            }
            // schedule work
            result.merge(scheduleWork(runnableCollection,listener,joinCondition, timeOut));
        } catch (AsynchronousServiceFailureException ex) {
            Message msg = new Message();
            msg = msg.setVariableData(new String[] { "Exception occurred during execution of [ AsynchronousProcessorCommonJImpl ] exception ["
                    + ex.getMessage() + "]" });
            result.addMessage(msg);
            result.setErrors(true);
        }
        return result;
    }
    protected AccelResult scheduleWork(Collection runnableCollection, AsynchronousProcessorListener listener,int joinCondition, long timeOut) throws AsynchronousServiceFailureException {
        // to store Output work items this will be used for setting up the Join condition
        List workItemList = new ArrayList(runnableCollection.size());
        // for joining
        List joinList = new ArrayList(runnableCollection.size());
        // Get iterator over all the submitted Jobs
        Iterator itr = runnableCollection.iterator();
        // get The manager implementation from Asunchronous Processor
        WorkManager mngr = AsynchronousProcessor.factory.getWorkManager(); 
        while(itr.hasNext()) {
            // While there are more Jobs
            Runnable runnable = (Runnable)itr.next();
            // Wrapping the Job in AccelWork
            AccelWork work  = new AccelWork(runnable);
            // call Manager 
            WorkItem workItem = null;
            try {
                if (listener == null) {
                    workItem = mngr.schedule(work); // scheduling job without listener
                } else {
                    workItem = mngr.schedule(work,listener); //schedule job with listener 
                }
                workItemList.add(new AccelWorkItem(workItem)); // store the response back to submitted jobs list
                joinList.add(workItem);
            } catch (IllegalArgumentException e) {
                // failed submitting Job or Job
                throw new AsynchronousServiceFailureException("Failed to schedule asynchronous work",e);
            } catch (WorkException e) {
                // failed submitting Job or Job
                throw new AsynchronousServiceFailureException("Failed to schedule asynchronous work",e);
            }
        }
        // setting the Wait condition for all the submitted Jobs
        try {
            if (joinCondition == WAIT_FOR_ANY) { // Wait till any one of the Job is complete
                mngr.waitForAny(joinList, timeOut);
            } else if (joinCondition == WAIT_FOR_ALL) { // Wait for all the jobs are complete
                mngr.waitForAll(joinList, timeOut);
            }
        } catch (IllegalArgumentException e) {
            // error setting up Join
            throw new AsynchronousServiceFailureException("Failed to set up Join condition for asynchrnous services ",e );
        } catch (InterruptedException e) {
            // error setting up Join
            throw new AsynchronousServiceFailureException("Failed to set up Join condition for asynchrnous services ",e );
        }
        AccelResult result = new AccelResult();
        result.addResults(workItemList);
        return result;
    }

    /* (non-Javadoc)
     * @see com.csc.fs.accel.async.AsynchronousProcessor#submit(java.lang.Runnable)
     */
    public Result submit(Runnable runnable) {
        List arrayList = new ArrayList(2);
        arrayList.add(runnable);
        return submit(arrayList);
    }
}
