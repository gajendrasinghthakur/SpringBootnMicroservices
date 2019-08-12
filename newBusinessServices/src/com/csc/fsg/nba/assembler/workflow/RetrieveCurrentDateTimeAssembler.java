/*************************************************************************
 *
 * Copyright Notice (2006)
 * (c) CSC Financial Services Limited 1996-2006.
 * All rights reserved. The software and associated documentation
 * supplied hereunder are the confidential and proprietary information
 * of CSC Financial Services Limited, Austin, Texas, USA and
 * are supplied subject to licence terms. In no event may the Licensee
 * reverse engineer, decompile, or otherwise attempt to discover the
 * underlying source code or confidential information herein.
 *
 *************************************************************************/
package com.csc.fsg.nba.assembler.workflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.WorkflowTimeStampResult;
import com.csc.fs.accel.valueobject.TimeStamp;
import com.csc.fs.accel.workflow.assembler.WorkflowAccelTransformation;
import com.csc.fs.dataobject.accel.workflow.DateTimeResponse;
import com.csc.fs.dataobject.accel.workflow.User;
import com.csc.fs.om.ObjectFactory;

/**
 * Retrieve Current DateTime Business Process dis-assembler/assembler.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * </table>
 * <p>
 */
public class RetrieveCurrentDateTimeAssembler extends WorkflowAccelTransformation {
    /*  
     * @see com.csc.fs.accel.AccelTransformation#disassemble(java.lang.Object)
     */
    public Result disassemble(Object input) {
        TimeStamp requestVO = (TimeStamp) ((List) input).get(0);
        User user = (User) ObjectFactory.create(User.class);
        user.setUserID(requestVO.getUserId());
        List result = new ArrayList();
        result.add(user);
        return Result.Factory.create().addResult(result);
    }

    /*  
     * @see com.csc.fs.accel.AccelTransformation#assemble(com.csc.fs.Result)
     */
    public Result assemble(Result result) {
        WorkflowTimeStampResult timeStampResult = new WorkflowTimeStampResult();
        if (result.hasErrors()) {
            timeStampResult.merge(result);
            return timeStampResult;
        }
        TimeStamp responseVO = new TimeStamp();
        List data = result.getReturnData();
        if (data != null && !data.isEmpty()) {
            Iterator dataObjects = data.iterator();
            while (dataObjects.hasNext()) {
                Object currentObj = dataObjects.next();
                if (currentObj instanceof DateTimeResponse) {
                    responseVO.setTimeStamp(((DateTimeResponse) currentObj).getTimestamp());
                }
            }
        }
        timeStampResult.addResult(responseVO);
        return timeStampResult;
    }
}