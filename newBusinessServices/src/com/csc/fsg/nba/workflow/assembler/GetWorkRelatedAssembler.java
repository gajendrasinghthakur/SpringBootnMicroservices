package com.csc.fsg.nba.workflow.assembler;

/*
 * *******************************************************************************<BR>
 * Copyright 2015, Computer Sciences Corporation. All Rights Reserved.<BR>
 *
 * CSC, the CSC logo, nbAccelerator, and csc.com are trademarks or registered
 * trademarks of Computer Sciences Corporation, registered in the United States
 * and other jurisdictions worldwide. Other product and service names might be
 * trademarks of CSC or other companies.<BR>
 *
 * Warning: This computer program is protected by copyright law and international
 * treaties. Unauthorized reproduction or distribution of this program, or any
 * portion of it, may result in severe civil and criminal penalties, and will be
 * prosecuted to the maximum extent possible under the law.<BR>
 * *******************************************************************************<BR>
 */

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.GetWorkResult;
/**
* GetWorkRelatedAssembler
* <p>
* <b>Modifications: </b> <br> 
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr> 
* </table>
* <p>
* @author CSC FSG Developer
 * @version NB-1402
* @since New Business Accelerator - NB-1401
*/

public class GetWorkRelatedAssembler extends com.csc.fsg.nba.workflow.assembler.WorkflowAccelTransformation {
    public Result assemble(Result result) {
        GetWorkResult getWorkResult = new GetWorkResult();
        if (result.hasErrors()) {
            getWorkResult.merge(result);
            return getWorkResult;
        }
        List data = result.getReturnData();
        if (data != null && !data.isEmpty()) {
            if (isREST()) {
                assembleREST(result, getWorkResult, data);
            } else {
                assembleNetServer(result, getWorkResult, data);
            }
        }
        return getWorkResult;
    }
}
