package com.csc.fsg.nba.business.process;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaLob;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

/**
 * Drives the aggregate contract process.  Controls the aggregation of work items being sent to the  
 * case manager queue for processing. 
 * Works on all the work items sent to "Aggregate Contract" queue and will perform aggregation of  
 * the work items into NBAGGCNT work item and routes this aggregated NBAGGCNT work item to case manager   
 * queue or to "End" queue depending upon the business logic.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>CR57950 and 57951</td><td>Version 8</td><td>Aggregate Contract - Pre-Sale/Reg60</td></tr>
 * <tr><td>CR61627</td><td>AXA Life Phase 2</td><td>Assign Replacement Case Manager</td></tr>
 * <tr><td>CR1345857(APSL2575)</td><td>Discretionary</td>Predictive CR - Aggregate</tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class NbaProcAggregateContract2 extends NbaProcAggregateContract {

	/**
	 * NbaProcAggregateContract2 constructor comment.
	 */
	public NbaProcAggregateContract2() {
		super();
	}

	
	//CR61627 New Method
    protected boolean isCaseManagerMissing() {
    	return null == parentWork.getNbaLob().getReplCMQueue() || parentWork.getNbaLob().getReplCMQueue().trim().length() <= 1;
    	
    }
    
    //CR61627 New Method
    protected String getCaseManagerAssignment() {
    	return NbaLob.A_LOB_RPCM_QUEUE;
    }
    
    //CR61627 New Method
    protected String getCaseManagerLOB() {
    	return replacementCM;
    }

    //CR1345857(APSL2575) New Method
    protected boolean getPredictiveIndicator() throws NbaBaseException {
    	return false;
}

}
