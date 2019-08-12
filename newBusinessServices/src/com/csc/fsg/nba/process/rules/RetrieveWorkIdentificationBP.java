package com.csc.fsg.nba.process.rules;

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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;

/**
 * The Business Process class is responsible for retrieving workidentification service for work items
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA163</td><td>Version 6</td><td>Case History Rewrite</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-17</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 17</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends WorkItemIdentificationBP
public class RetrieveWorkIdentificationBP extends WorkItemIdentificationBP {
    //NBA213 code deleted

    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            result.addResult(processWorkItemIdentificationModel((NbaDst) input));  //NBA208-17
        } catch (Exception e) {
            //NBA213 code deleted
            addExceptionMessage(result, e);
            //NBA213 code deleted
        }
        return result;
    }

	/**
	 * Calls the WorkItemIdentification VP/MS model and returns Map resolving all the case and transaction work Items 
	 * @param  work nbADst
	 * @return Map of workItem identification String for workItem Id's
	 * @throws NbaBaseException
	 */
	//NBA208-17 New Method
	public Map processWorkItemIdentificationModel(NbaDst work) throws NbaBaseException {
        Map workIdentification = new HashMap();
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			vpmsProxy = new NbaVpmsAdaptor(NbaVpmsAdaptor.WORKITEMIDENTIFICATION); 
            workIdentification.put(work.getID(), processWorkItemIdentificationModel(work.getNbaLob(), vpmsProxy, work.getID())); //APSL5055
	        if (work.isCase()) {
	            Iterator itr = work.getNbaTransactions().iterator();
	            NbaTransaction nbaTransaction = null;
	            while (itr.hasNext()) {
	                nbaTransaction = (NbaTransaction) itr.next();
	                workIdentification.put(nbaTransaction.getID(), processWorkItemIdentificationModel(nbaTransaction.getNbaLob(), vpmsProxy, nbaTransaction.getID())); // APSL5055
	            }
	        }
		} finally {
			try {
				if (vpmsProxy != null) {					
					vpmsProxy.remove();
					vpmsProxy = null;
				}
			} catch (RemoteException e) {
			    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
			}
		}
        return workIdentification;
	}

	//NBA213 code deleted
}
