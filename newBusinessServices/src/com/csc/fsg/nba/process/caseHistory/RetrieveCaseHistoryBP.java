package com.csc.fsg.nba.process.caseHistory;

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

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.process.workflow.NbaRetrieveHistoryBP;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Retrieves the workflow history for a given work item to populate the Case History
 * business function.  This process supports a <code>NbaDst</code> representation of
 * a work item.
 * <p>
 * If the input work item is a case, then all children transactions will be retrieved
 * from the workflow system including their history.  No lock will be attempted for
 * the children transactions. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Deferred Work Item Retrieval</td></tr>
 * <tr><td>SPR3531</td><td>Version 8</td><td>Sources Not Displayed in Case History</td></tr>
 * <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class RetrieveCaseHistoryBP extends NbaRetrieveHistoryBP {    
	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    public Result process(Object input) {
        Result result = new AccelResult();
        try {
			NbaDst nbaDst = (NbaDst) input;
			if (nbaDst.isCase()) {
				result = getChildrenWorkItems(nbaDst);
				if (!result.hasErrors()) {
					nbaDst = (NbaDst) result.getFirst();
				}
			}
			result = super.process(nbaDst);
		} catch (Exception e) {
			addExceptionMessage(result, e);
			return result;
		}
		return result;
    }

    /**
	 * Retrieve the children for a work item. This will be read only and a lock is not obtained
	 * 
	 * @param nbaDst
	 * @return the updated DST
	 */
    protected AccelResult getChildrenWorkItems(NbaDst nbaDst){
    	NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();
		options.setWorkItem(nbaDst.getID(), true);
		options.setSystemName(nbaDst.getSystemName());  //APSL5055-NBA331
		options.requestTransactionAsChild();
		options.requestSources(); //SPR3531
		NbaUserVO user = new NbaUserVO();
		user.setUserID(getCurrentUserId());
		options.setNbaUserVO(user);
    	AccelResult result =  (AccelResult) callBusinessService("NbaRetrieveWorkBP", options);
    	return result;
    }
}
