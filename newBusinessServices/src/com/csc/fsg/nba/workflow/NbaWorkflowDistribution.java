package com.csc.fsg.nba.workflow;

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

import java.util.ArrayList;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaLob;

/**
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA132</td><td>Version 6</td><td>Equitable Distribution of Work</td></tr>
 * <tr><td>NBA251</td><td>Version 8</td><td>nbA Case Manager and Companion Case Assignment</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */

public class NbaWorkflowDistribution {

    private NbaTableAccessor nta = new NbaTableAccessor();
    private String businessArea = null;
    private String workType = null;
    //NBA251 code deleted

    /**
     * Constructor.
     * @param businessArea
     * @param workType
     */
    public NbaWorkflowDistribution(String businessArea, String workType) {
        this.businessArea = businessArea;
        this.workType = workType;
    }

    /**
     * Constructor.
     * @param nbaLob
     */
    public NbaWorkflowDistribution(NbaLob nbaLob) {
        if (nbaLob != null) {
            businessArea = nbaLob.getBusinessArea();
            workType = nbaLob.getWorkType();
        }
    }

    /**
     * Returns the count of work items assigned to this business area, work type,
     * and status.
     * @param status
     * @return
     * @throws NbaBaseException
     */
    public int getWorkAssignedCount(String status) throws NbaBaseException {
        return getTableAccessor().getWorkAtCount(getBusinessArea(), getWorkType(), getQueueByStatus(status));
    }

    //NBA251 methods deleted

    /**
     * Returns the count of work items assigned residing in a queue.
     * @param queue
     * @return
     * @throws NbaBaseException
     */
    public int getWorkCountByQueue(String queue) throws NbaBaseException {
        return getTableAccessor().getWorkAtCount(getBusinessArea(), getWorkType(), queue);
    }

    //NBA251 methods deleted

   /**
     * Returns the corresponding queue based on a given status.
     * @param status
     * @return
     * @throws NbaBaseException
     */
    protected String getQueueByStatus(String status) throws NbaBaseException {
	    return getTableAccessor().getNextQueue(getBusinessArea(), getWorkType(), status);
    }

    /**
     * Returns the business area.
     * @return
     */
    protected String getBusinessArea() {
        return businessArea;
    }

    /**
     * Returns the work type.
     * @return
     */
    protected String getWorkType() {
        return workType;
    }

    /**
     * Returns the common table accessor for the multiple queries executed by this class.
     * @return
     */
    protected NbaTableAccessor getTableAccessor() {
        return nta;
    }
    

    /**
     * Returns the count of all work items assigned to an queue.  This includes
     * work items currently in the queue and additional work items assigned to them
     * but temporarily held up in another queue.
     * @param queue
     * @param lob
     * @return count
     * @throws NbaBaseException
     */
     //NBA251 New method
    public int getAssignedWorkCountByQueue(String queue, String lobName) throws NbaBaseException {
        int count = getWorkCountByQueue(queue);
		count = count + getAdditionalAssignedWorkCount(queue, lobName);
        return count;
    }  

    /**
     * Returns the count of work items assigned to a queue, but currently residing in a
     * queue other than it is assigned.  The work items are counted if the LOB value passed to it
     * matches the value of the queue.  These temporary queues are configurable in the
     * NBA_AWD_TRANSLATIONS table.
     * @param queue is assigned queue
     * @param lob
     * @return
     * @throws NbaBaseException
     */
    //NBA251 New method
    protected int getAdditionalAssignedWorkCount(String queue, String lobName) throws NbaBaseException {
        int count = 0;
        List queues = getAdditionalQueues(lobName);
	    if(! NbaUtils.isBlankOrNull(queues)){
	        int queueCount = queues.size();
	        for (int i = 0; i < queueCount; i++) {
	            count = count + getTableAccessor().getWorkCountByLOB((String)queues.get(i), queue, lobName);
	        }
        }
        return count;
    }

    /**
     * Returns a list of additional queues to include in the work assigned count.
     * This list is configurable in the NBA_AWD_TRANSLATIONS table.
     * @return
     * @throws NbaBaseException
     */
    //NBA251 New method
    protected List getAdditionalQueues(String lobName) throws NbaBaseException {
    	ArrayList additionalQueues = null;
    	NbaTableData[] includeQueues = null;
    	int count = 0;
    	if(NbaLob.A_LOB_ORIGINAL_UW_WB_QUEUE.equalsIgnoreCase(lobName)){
    		includeQueues = getTableAccessor().getTranslationObjects(NbaTableAccessConstants.AWD_QUEUE_INCLUDE);	
    	}else if(NbaLob.A_LOB_CM_QUEUE.equalsIgnoreCase(lobName)){
    		//Additional queues are not applicable for case manager    		
    	}
    	if(includeQueues != null){
    		count = includeQueues.length;
			additionalQueues = new ArrayList(count + 1);
			for (int i = 0; i < count; i++) {
				additionalQueues.add(includeQueues[i].code());
			}
    	}
		return additionalQueues;
    }    
}
