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
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;

/**
 * Unlocks workitems part of <code>NbaDst</code>
 * or a list of <code>NbaDst<code> instances as input.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-20</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 20</td></tr> 
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaUnlockWorkBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		Result result = new AccelResult(); 
		try {
			// begin NBA153
			NbaDst dst;
			if (input instanceof List) {
				List workItems = (List) input;
				int count = workItems.size();
				for (int i = 0; i < count; i++) {
					dst = (NbaDst) workItems.get(i);
					result.merge(processResult(dst, unlockWork(dst))); //NBA208-20
				}
			} else {
			// end NBA153
				dst = (NbaDst) input;
				result.merge(processResult(dst, unlockWork(dst)));//NBA153 NBA208-20
			}  //NBA153
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}
	
    /**
     * Unlock all AWD object within the NbaDst which are currently locked by this user.
     * @param nbaDst the NbaDst object encapsulating the AWD Case, Transactions and/or Sources
     */
     //NBA208-20 modified method signature removed userVO as input parameter
    public Result unlockWork(NbaDst nbaDst) {
    	//begin NBA208-20
        // create WorkItem objects out of NbaDst 
        List inputData = new ArrayList(); // input list to disassembler
        String userid = nbaDst.getNbaUserVO().getUserID(); // get the user id 
        //APSL5055-NBA331 code deleted
        try {
            if (nbaDst.isCase()) { // if top level workitem is case 
            	//NBA208-32
                if (userid.equals(nbaDst.getCase().getLockStatus())) { // if locked by userid add it to inputlist
                    nbaDst.getWorkItem().setUserID(userid);    //APSL5055-NBA331
                    //APSL5055-NBA331 code deleted
                    inputData.add(nbaDst.getWorkItem());  //APSL5055-NBA331
                }
                List transactions = nbaDst.getTransactions(); // get all the transactions
                Iterator transactionItr = transactions.iterator(); 
                while (transactionItr.hasNext()) {
                	//NBA208-32
                	WorkItem transaction = (WorkItem) transactionItr.next(); 
                    if (userid.equals(transaction.getLockStatus())) { // if transaction is locked by current user add it to inputlist 
                        transaction.setUserID(userid);    //APSL5055-NBA331
                        //APSL5055-NBA331 code deleted
                        inputData.add(transaction);  //APSL5055-NBA331
                    }
                }
            } else { // if top level workitem is transaction 
            	//NBA208-32
                if (userid.equals(nbaDst.getTransaction().getLockStatus())) { // if the transaction is locked by userid adding it to input list
                    nbaDst.getTransaction().setUserID(userid);     //APSL5055-NBA331
                    //APSL5055-NBA331 code deleted
                    inputData.add(nbaDst.getWorkItem());  //APSL5055-NBA331
                }
            }
        } catch (NbaNetServerDataNotFoundException e) {
            addExceptionMessage(new AccelResult(), e);
        }
		//begin APSL5055-NBA331
        if (inputData.size() > 0) { //APSL5055-NBA331
            return callBusinessService("UnlockAllworkBP", inputData);
        }  
        Result aResult = new AccelResult();
        aResult.addResult(new ArrayList(0));
        return aResult;  
        //end APSL5055-NBA331
		//end NBA208-20
    }
    /**
     * Process the result from UnlockAlworkBP
     * @param serviceResult Result From Service
     */
     //NBA208-20 new method
    public Result processResult(NbaDst dst, Result serviceResult) throws NbaBaseException {
        Iterator serviceResultItr = serviceResult.getData().iterator();
        List workItemIdLst = new ArrayList(); 
        while(serviceResultItr.hasNext()) {
            Object responseObj = serviceResultItr.next();
            if(responseObj instanceof WorkItem) {
                workItemIdLst.add(((WorkItem)responseObj).getItemID());
                serviceResultItr.remove();
            }
        }
        // we have workitems which are not unlocked. 
        if (dst.isCase()) { // if top level workitem is case 
            if (!workItemIdLst.contains(dst.getID())) { // if locked by userid add it to inputlist
            	//NBA208-32
                dst.getCase().setLockStatus("");
            }
            List transactions = dst.getTransactions(); // get all the transactions
            Iterator transactionItr = transactions.iterator();
            while (transactionItr.hasNext()) {
            	//NBA208-32
                WorkItem transaction = (WorkItem) transactionItr.next();
                if (!workItemIdLst.contains(transaction.getItemID())) { // if transaction is locked by current user add it to inputlist
                    transaction.setLockStatus("");
                }
            }
        } else { // if top level workitem is transaction
            //NBA208-32
            if (!workItemIdLst.contains(dst.getTransaction().getItemID())) { // if the transaction is locked by userid adding it to input list
            	dst.getTransaction().setLockStatus("");
            }

        }
        serviceResult.getData().add(dst);
        return serviceResult; 
    }
}
