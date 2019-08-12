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
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;

/**
 * Retrieves the sources for the specified work item using <code>NbaNetServerAccessor</code>.
 * Requires an <code>NbaDst</code> or <code>NbaSearchResultVO</code> with the appropriate
 * case or transaction id set.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA153</td><td>Version 6</td><td>Companion Case Rewrite</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class RetrieveSourcesBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			if (input instanceof NbaDst) {
				result.addResult(retrieveSources((NbaDst) input));
			} else if (input instanceof NbaSearchResultVO) {
				result.addResult(retrieveSources((NbaSearchResultVO) input));
			} else {
				throw new NbaBaseException("Unsupported input");
			}
		} catch (Exception e) {
			addExceptionMessage(result, e);
			return result;			
		}
		return result;
	}

	/**
	 * Retrieves the sources for the specified work item.
	 * @param nbaDst the work item
	 * @return the retrieved work item
	 * @throws Exception
	 */
	public NbaDst retrieveSources(NbaDst nbaDst) throws Exception {
		NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();
		options.setWorkItem(nbaDst.getID(), nbaDst.isCase());
		options.requestSources();
		return WorkflowServiceHelper.retrieveWorkItem(nbaDst.getNbaUserVO(), options);  //NBA213
	}

	/**
	 * Retrieves the sources for the specified search result.
	 * @param searchResult the work item
	 * @return the retrieved work item
	 * @throws Exception
	 */
	public NbaDst retrieveSources(NbaSearchResultVO searchResult) throws Exception {
		NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();
		options.setWorkItem(searchResult.getWorkItemID(), searchResult.isCase());
		options.requestSources();
		return WorkflowServiceHelper.retrieveWorkItem(searchResult.getNbaUserVO(), options);  //NBA213
	}

	//NBA213 deleted code
}
