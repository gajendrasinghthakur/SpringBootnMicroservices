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

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.TimeStamp;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Retrieves time stamp information of awd server using the <code>NbaNetServerAccessor</code>. 
 * Requires an <code>NbaUserVO</code>.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaRetrieveTimeStampBP extends NewBusinessAccelBP {

    /*
     * (non-Javadoc)
     * 
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            NbaUserVO userVO = (NbaUserVO) input;
            //NBA208-32 code deleted
            TimeStamp timeStamp = new TimeStamp();
            timeStamp.setUserId(userVO.getUserID());
            //NBA208-32 code deleted
            AccelResult res = (AccelResult)callBusinessService("RetrieveCurrentDateTimeBP", timeStamp);
            processResult(res);
            NbaDst nbaDst = new NbaDst();
            //NBA208-32 code deleted
            //NBA208-32
            nbaDst.setUserID(userVO.getUserID());
            List timestampResult = res.getReturnData();  //NBA208-32
            for (int i = 0; i < timestampResult.size(); i++) {
                if (timestampResult.get(i) instanceof TimeStamp) {
                    timeStamp = (TimeStamp) timestampResult.get(i);
                    //NBA208-32
                    nbaDst.setTimestamp(timeStamp.getTimeStamp());
                }
            }
            //NBA208-32 code deleted
            result.addResult(nbaDst);
        } catch (Exception e) {
            addExceptionMessage(result, e);
            return result;
        }
        return result;
    }
}
