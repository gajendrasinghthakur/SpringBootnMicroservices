package com.csc.fsg.nba.process.companion;

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

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.bean.accessors.NbaCompanionCaseFacadeBean;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Maintains the linked companion cases using the <code>NbaCompanionCaseFacadeBean</code>.
 * Requires as input a list containing three members in order: <code>NbaUserVO</code>,
 * <code>NbaDst</code>, and a list of <code>NbaCompanionCaseVO</code> instances for
 * each linked case.
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
public class CommitCompanionCasesBP extends NewBusinessAccelBP {

    /**
     * Called to retrieve a List of companion cases for the given case
     * @param an instance of <code>NbaDst</code> object
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
        	List inputParams = (List) input;
            commitCompanionCases((NbaUserVO) inputParams.get(0), (NbaDst) inputParams.get(1), (List) inputParams.get(2));
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Performs maintenance on the list of companion cases.  The list of companion cases
     * is interrogated first to determine what maintenance needs to be performed to reduce
     * the number of calls to the EJB.  
     * @param user
     * @param dst
     * @param companionCases
     * @throws Exception
     */
	public void commitCompanionCases(NbaUserVO user, NbaDst dst, List companionCases) throws Exception {
        boolean updatesNeeded = false;
        boolean deletesNeeded = false;
		int count = companionCases.size();
		for (int i = 0; i < count; i++) {
			NbaCompanionCaseVO companionCase = (NbaCompanionCaseVO) companionCases.get(i);
			deletesNeeded = deletesNeeded || companionCase.isActionDelete();
			updatesNeeded = updatesNeeded || companionCase.isActionAdd() || companionCase.isActionUpdate();
		}

		if (updatesNeeded || deletesNeeded) {  //NBA213
			NbaCompanionCaseFacadeBean bean = new NbaCompanionCaseFacadeBean();  //NBA213
			if (updatesNeeded) {
				bean.addCompanionCases(user, dst, companionCases, NbaConstants.PROC_VIEW_COMPANION_CASE);  //NBA213
			}
	
			if (deletesNeeded) {
				bean.removeCasesFromLink(user, dst, companionCases);  //NBA213
			}
		}
	}
	//NBA213 deleted code
}
