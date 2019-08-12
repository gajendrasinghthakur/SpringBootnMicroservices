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
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaCompanionCaseControlData;
import com.csc.fsg.nba.vo.NbaDst;

/**
 * Retrieves a list of companion cases for the given case by retrieving  
 * the control source information using <code>NbaCompanionCaseControlData</code>.
 * Requires an <code>NbaDst</code> representing the current case as input.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA164</td><td>Version 6</td><td>nbA Contract Status View Rewrite Project</td></tr> 
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class RetrieveCompanionCasesStatusBP extends NewBusinessAccelBP {

    /**
     * Called to retrieve a List of companion cases for the given case
     * @param an instance of <code>NbaDst</code> object
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            result.addResult(retrieveCompanionCasesStatus((NbaDst) input));
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

	public List retrieveCompanionCasesStatus(NbaDst dst) throws Exception {
		//Retrieves a snapshot of what is in the control source
		return new NbaCompanionCaseControlData().getNbaCompanionCaseVOs(dst.getID());
	}
}
