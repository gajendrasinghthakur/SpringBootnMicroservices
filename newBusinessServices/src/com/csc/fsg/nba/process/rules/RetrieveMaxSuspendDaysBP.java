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

import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.tableaccess.NbaAwdValuesData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaDst;

/**
 * Retrieves maximum suspend days information for a work item using the <code>NbaTableAccessor</code>. 
 * Requires an <code>NbaDst</code>.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA168</td><td>Version 6</td><td>Suspend Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */

public class RetrieveMaxSuspendDaysBP extends AccelBP {

    /*
     * (non-Javadoc)
     * 
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            NbaTableAccessor tableAccessor = new NbaTableAccessor();
            NbaDst nbaDst = (NbaDst) input;

            Map data = new HashMap();
            data.put(NbaTableAccessConstants.C_TABLE_TYPE, NbaTableConstants.AWD_MAX_SUSPEND);
            data.put(NbaTableAccessConstants.C_BUSINESS_AREA, NbaTableAccessConstants.WILDCARD);
            data.put(NbaTableAccessConstants.C_WORK_TYPE, nbaDst.getWorkType());

            NbaAwdValuesData[] awdValuesTable = (NbaAwdValuesData[]) tableAccessor.getDisplayData(data, NbaTableConstants.NBA_AWD_VALUES);

            if (awdValuesTable.length > 0) {
                result.addResult(awdValuesTable[0].getAwdValue());
            }
        } catch (Exception e) {
            addExceptionMessage(result, e);
            return result;
        }
        return result;
    }
}
