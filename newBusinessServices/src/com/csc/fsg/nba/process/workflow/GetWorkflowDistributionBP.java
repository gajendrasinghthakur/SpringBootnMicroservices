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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.workflow.NbaWorkflowDistribution;

/**
 * Class Description.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA251</td><td>Version 8</td><td>nbA Case Manager and Companion Case Assignment</td></tr>
 * <tr><td>ALS3208</td><td>AXA Life Phase 1</td><td>Performance</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class GetWorkflowDistributionBP extends NewBusinessAccelBP implements NbaConstants {
	private boolean hasErrors = false;//ALS3208
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
        	if(input instanceof String){
	    		NbaWorkflowDistribution nwd = new NbaWorkflowDistribution(NbaConstants.A_BA_NBA, NbaConstants.A_WT_APPLICATION);
	    		result.addResult(new Integer(nwd.getAssignedWorkCountByQueue((String)input, NbaLob.A_LOB_ORIGINAL_UW_WB_QUEUE)));//NBA251
	    		//begin ALS3208
        	} else if (input instanceof List) {
        		Map queueMap = countWorkByQueues((List)input);
    			result.addResult(queueMap);
    			result.setErrors(hasErrors);
    		}
    	//end ALS3208
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }
    
//  ALS3208 new method
    private Map countWorkByQueues(List queues) {
    	Map queueMap = new HashMap();
    	String queue;
    	Integer queueCount;
    	int count = queues.size();
    	
		for (int i = 0; i < count; i++) {
			queue = (String) queues.get(i);
			try {
			NbaWorkflowDistribution nwd = new NbaWorkflowDistribution(NbaConstants.A_BA_NBA, NbaConstants.A_WT_APPLICATION);
			queueCount = new Integer(nwd.getAssignedWorkCountByQueue(queue, NbaLob.A_LOB_ORIGINAL_UW_WB_QUEUE));
			queueMap.put(queue,queueCount);
			} catch (Exception e) {
				queueMap.put(queue,new Integer(0));
				hasErrors = true;
			}
			
			
		}
		return queueMap;
    }
    
}
