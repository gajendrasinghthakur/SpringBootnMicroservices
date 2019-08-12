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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.SystemSession;
import com.csc.fs.UserSessionController;
import com.csc.fs.accel.result.GetWorkResult;
import com.csc.fs.accel.valueobject.WorkItemRequest;
import com.csc.fs.dataobject.accel.workflow.GetWorkRequest;

public class GetFullWorkAssembler extends com.csc.fsg.nba.workflow.assembler.WorkflowAccelTransformation {

	/**
    * Assemble the data objects into value objects. First collect the LOBs.
    * Then hydrate each WorkItem value object from its corresponding data object
    * and attach value objects for each LOB data object with a matching id from the
    * LOB collection.
	 <p>
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

    /**
     * Disassemble the WorkItemRequest value object into data objects. For each system identified in 
     * userSession.getActiveSystems(), create a GetWorkRequest data object containing the jobname
     * and SystemName.  
     * @param input - the WorkItemRequest value object
     * @return a ResultImpl containing a List of the GetWorkRequest date objects
     */
    public Result disassemble(Object input) {
        WorkItemRequest requestVO = (WorkItemRequest) input; 
        List inputData = new ArrayList(1);
        //begin NBA146
        UserSessionController userSession = getServiceContext().getUserSession();   
        Result systems = userSession.getActiveSystems();
        List data = systems.getData();
        if (data != null && !data.isEmpty()) {
            Iterator iter = data.iterator();
            while (iter.hasNext()) {
                SystemSession session = (SystemSession) iter.next();
                if (session != null) {                   
                    GetWorkRequest req = new GetWorkRequest();
                    req.setJobName(requestVO.getJobName());
                    req.setSystemName(session.getSystemName()); //used in GetWork.XML to determine the systemapi to use
                    inputData.add(req);                     
                }
            }
        }
        //end NBA146
        return Result.Factory.create().addResult(inputData);
    }
}
