package com.csc.fsg.nba.process.reallocate;

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

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.bean.accessors.NbaReallocateFacadeBean;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * This creates a instance of facade been pass the list of reallocate value objects to be routed.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA179</td><td>Version 7</td><td>Reallocate UI Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class ReallocateBP extends AccelBP {

    /* (non-Javadoc)
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            List inputList = (List) input;
            result.addResult(new NbaReallocateFacadeBean().reallocate((NbaUserVO) inputList.get(0), (List) inputList.get(1)));
        } catch (Exception e) {
            result = new AccelResult();
            addExceptionMessage(result, e);
        }
        return result;
    }
}
