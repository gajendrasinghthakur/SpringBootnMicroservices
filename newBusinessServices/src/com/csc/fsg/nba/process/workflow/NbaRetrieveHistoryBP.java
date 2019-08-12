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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.result.RetrieveWorkResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemRequest;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.tbf.xml.XmlString;

/**
 * Retrieve the workflow History for the work items identified in an <code>NbaDst</code>.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA208-1 </td><td>Version 7</td><td>Performance Tuning and Testing - Deferred History Retrieval</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3531</td><td>Version 8</td><td>Sources Not Displayed in Case History</td></tr>
 * <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class NbaRetrieveHistoryBP extends NewBusinessAccelBP {    

    /**
     * Add WorkItemSource children to a WorkItem.
     * @param sources 
     * @param workSourceMap
     * @param workItemRequest- Value object 
     */
     //SPR3531 added a new parameter - workItemRequest
    protected void addSourcesToHistoryRequest(List sources, Map workSourceMap, WorkItemRequest workItemRequest) {
        Iterator sit = sources.iterator();
        WorkItemSource source;	//NBA208-32
        String id;	//NBA208-32 
        while (sit.hasNext()) {        	
        	source = (WorkItemSource) sit.next();	//NBA208-32
            if (!isSystemData(source.getSourceType())) {
                id = source.getItemID();	//NBA208-32
                if (id != null && id.length() > 0) {
                    workSourceMap.put(id, source);
                    workItemRequest.getSourceRequestedList().add(id); //SPR3531
                    //NBA208-32 code deleted
                }
            }
        }
    }

    /**
     * Determine if the Source type is a real Workflow system Source or a 
     * Source created from a row in the NBA_SYSTEM_DATA table of the NBAAUXILIARY schems. 
     * @param sourceType - the Source type
     * @return true if the Source type was created from a row in the NBA_SYSTEM_DATA table of the NBAAUXILIARY schems.
     */
    protected boolean isSystemData(String sourceType) {
        if (sourceType != null) {
            String sourceCompare = sourceType.trim();
            Iterator it = NbaSystemDataProcessor.getSystemDataTypes().getType().iterator();
            while (it.hasNext()) {
                XmlString obj = (XmlString) it.next();
                if (sourceCompare.equalsIgnoreCase(obj.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Merge the case, transaction and source history from retrieved items onto the original object(s).
     * @param workItemMap
     * @param result
     * @throws NbaNetServerDataNotFoundException
     */
    protected void mergeHistory(Map workItemMap, RetrieveWorkResult result) {
        Iterator it = result.getWorkItems().iterator();
        WorkItem workobj;	//NBA208-32
        WorkItemSource source;	//NBA208-32
        while (it.hasNext()) {
            WorkItem workitem = (WorkItem) it.next();
            Object obj = workItemMap.get(workitem.getItemID());
            if (obj != null) {
            	//begin NBA208-32
                if (obj instanceof WorkItem) {
                    workobj = (WorkItem) obj;
                    workobj.setHistory(workitem.getHistory());
                    workobj.setComments(workitem.getComments());
                } else if (obj instanceof WorkItemSource) {
                	source = (WorkItemSource) obj;
                    source.setHistory(workitem.getHistory());
                    source.setComments(workitem.getComments());
                //end NBA208-32
                }
            }
        }
    }
 
    //NBA208-32 code deleted

    /**
     * Prepare the get history request with minimal required information like case, transaction and source IDs
     * @param originalDst
     * @param workItemRequest
     * @return
     * @throws NbaBaseException
     */
    protected WorkItemRequest prepareHistoryRequest(NbaDst originalDst, Map workItemMap) throws NbaBaseException {
        WorkItemRequest workItemRequest = new WorkItemRequest();
        WorkItem transaction; //NBA208-32
        String id;
        if (originalDst.isCase()) {    	
            WorkItem origCase = originalDst.getCase();	//NBA208-32
            id = originalDst.getID();
            if (id != null && id.length() > 0) {            	
            	workItemMap.put(id, origCase);
            	workItemRequest.getWorkItems().add(origCase);	//NBA208-32
            	addSourcesToHistoryRequest(origCase.getSourceChildren(), workItemMap, workItemRequest); //NBA208-32 SPR3531
                Iterator tit = origCase.getWorkItemChildren().iterator();	//NBA208-32
                while (tit.hasNext()) {
                    transaction = (WorkItem) tit.next();	//NBA208-32
                    workItemRequest.setSystemName(transaction.getSystemName()); //APSL5055-NBA331
                   	id = transaction.getItemID();	//NBA208-32
                    if (id != null && id.length() > 0) {
                        workItemMap.put(id, transaction);
                        addSourcesToHistoryRequest(transaction.getSourceChildren(), workItemMap, workItemRequest); //NBA208-32 SPR3531
                    }
                }
            }
        } else {
            transaction = originalDst.getTransaction();	//NBA208-32
            id = originalDst.getID();
            if (id != null && id.length() > 0) { 
            	workItemRequest.getWorkItems().add(transaction);	//NBA208-32
                workItemMap.put(id, transaction); 
                addSourcesToHistoryRequest(transaction.getSourceChildren(), workItemMap, workItemRequest); //NBA208-32 SPR3531
            }
        }
        return workItemRequest;
    }

    /**
     * Retrieve history information for complete NbaDst hierarchy. Retrieves history for parent case, and each transaction and source in the hierarchy
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            if (input instanceof NbaDst) {
                result.addResult(retrieveHistory((NbaDst) input));
            } else {
                throw new NbaBaseException("Unsupported input");
            }
        } catch (Exception e) {
            addExceptionMessage(result, e);
            return result;
        }
        return result;
    }

    /**
     * Retrieve history information for complete NbaDst hierarchy. Retrieves history for parent case, and each transaction and source in the hierarchy
     * @param origNbaDst
     * @return
     * @throws NbaBaseException
     */
    protected NbaDst retrieveHistory(NbaDst origNbaDst) throws NbaBaseException {
        Map workItemMap = new HashMap();
        WorkItemRequest workItemRequest = prepareHistoryRequest(origNbaDst, workItemMap);
        if (workItemRequest.getWorkItems().size() > 0) {
            RetrieveWorkResult result = new RetrieveWorkResult();
            result.merge(callBusinessService("RetrieveFullHistoryBP", workItemRequest));
            mergeHistory(workItemMap, result);
        }
        origNbaDst.setHistoryRetrieved(true);
        return origNbaDst;
    }
}
