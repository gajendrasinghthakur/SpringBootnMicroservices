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

import java.util.HashMap;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.exception.NbaBaseException;

/**
 * This Business Process class is responsible for retrieving case data such as passStatus, action, priority, rule, and level from the VP/MS model
 * results.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class RetrieveReferStatusBP extends AccelBP {

    /**
	 * Called to retrieve the the data from a VP/MS model.
	 * @param input array list object containing following - the VP/MS model name - the entry point - Contract's holding inquiry - an instance of NbaLob -
	 *            	deOink HashMap
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			if (input instanceof VpmsComputeResult) {
				VpmsComputeResult data = (VpmsComputeResult) input;
				Map caseData = getReferData(data);
				result.addResult(caseData);
			}
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}

    /**
	 * Retrieves the passStatus, action, priority, rule, and level for the case from VP/MS results
	 * @param data VpmsComputeResult object
	 * @return caseData This is a Map containing the passStatus, action, priority, rule, and level
	 * @throws NbaBaseException
	 */ 
	protected Map getReferData(VpmsComputeResult data) throws NbaBaseException {
		NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(data);
		Map caseData = new HashMap(6);
		caseData.put("passStatus", statusProvider.getPassStatus());
		caseData.put("action", statusProvider.getCaseAction());
		caseData.put("priority", statusProvider.getCasePriority());
		caseData.put("rule", String.valueOf(statusProvider.getRule()));
		caseData.put("level", String.valueOf(statusProvider.getLevel()));
		return caseData;
	}
}
