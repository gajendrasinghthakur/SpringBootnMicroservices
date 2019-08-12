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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.RetrieveWorkResult;
import com.csc.fs.accel.util.ServiceHelper;
import com.csc.fs.accel.valueobject.Comment;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.accel.valueobject.WorkflowHistory;
import com.csc.fs.accel.valueobject.WorkflowSourceHeader;
import com.csc.fs.dataobject.accel.workflow.ConsolidatedComment;
import com.csc.fs.dataobject.accel.workflow.ConsolidatedHistory;
import com.csc.fs.dataobject.accel.workflow.Document;
import com.csc.fs.dataobject.accel.workflow.LOB;
import com.csc.fs.dataobject.accel.workflow.ObjectHeader;
import com.csc.fs.dataobject.accel.workflow.ParentChildRelationship;
import com.csc.fs.dataobject.accel.workflow.SourceItem;
import com.csc.fs.dataobject.accel.workflow.WorkItem;

/**
* RetrieveFullWorkAssembler
* <p>
* <b>Modifications: </b> <br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version NB-1402
* @since New Business Accelerator - Version NB-1401
*/

public class RetrieveFullWorkAssembler extends com.csc.fsg.nba.workflow.assembler.WorkflowAccelTransformation {

	public Result assemble(Result result) {

		RetrieveWorkResult retrieveWorkResult = new RetrieveWorkResult();
        retrieveWorkResult.setWorkItems(new ArrayList());   
        retrieveWorkResult.setSourceItems(new ArrayList());

		if (result.hasErrors()) {
			retrieveWorkResult.merge(result);
			return retrieveWorkResult;
		}
        List data = result.getReturnData();
        if (data != null && !data.isEmpty()) {
            if (isREST()) {
                assembleREST(result, retrieveWorkResult, data);
            } else {
                assembleNetServer(result, retrieveWorkResult, data);
            }
        }        
		return retrieveWorkResult;
	}
	
	/**
     * Convert ConsolidatedHistory DOs to WorkflowHistory VOs and store the results in a Map.
     * The Map keys are the parent item ids. The Map entries are arrays containing the WorkflowHistory VOs.
     * @param historyComments - ConsolidatedHistory DOs
     * @return the Map 
     */
    protected Map getHistoryMap(List historyComments) {
        Map workHistoryMap = new HashMap();
        Iterator it = historyComments.iterator();
        while (it.hasNext()) {
            ConsolidatedHistory historyDO = (ConsolidatedHistory) it.next();
            WorkflowHistory historyVO = new WorkflowHistory();
            historyVO.setIdentifier(historyDO.getIdentifier());
            historyVO.setBusinessArea(historyDO.getEndUnitCode());
            historyVO.setDateTime(historyDO.getDateTime());
            historyVO.setEndDateTime(historyDO.getEndDateTime());
            historyVO.setPriority(historyDO.getEndPrty());
            historyVO.setProcessor(historyDO.getUserID());
            historyVO.setQueueID(historyDO.getEndQueueCode());
            historyVO.setStatus(historyDO.getEndStatCode());
            historyVO.setWorkType(historyDO.getEndWorkType());
            historyVO.setDescription(historyDO.getDescription());
            historyVO.setUserID(historyDO.getUserID());
            historyVO.setStartTime(historyDO.getStartTime());
            historyVO.setRecordType(historyDO.getRecordType());
            List currentHistory = (List) workHistoryMap.get(historyDO.getItemID());
            if (currentHistory == null) {
                currentHistory = new ArrayList();
            }
            currentHistory.add(historyVO);
            workHistoryMap.put(historyDO.getItemID(), currentHistory);
        }
        return workHistoryMap;
    }
    /**
     * Convert ObjectHeader DOs to WorkflowSourceHeader VOs and store the results in a Map.
     * The Map keys are the parent item ids. The Map entries are the WorkflowSourceHeaders VOs.
     * @param objectHeaders - Comment DOs
     * @return the Map 
     */   
    protected Map getWorkflowSourceHeaderMap(List objectHeaders) {
        Map workHistoryMap = new HashMap();
        Iterator it = objectHeaders.iterator();
        while (it.hasNext()) {
            ObjectHeader objectHeaderDO = (ObjectHeader) it.next();
            WorkflowSourceHeader workflowSourceHeaderVO = new WorkflowSourceHeader();
            workflowSourceHeaderVO.setRecordType(objectHeaderDO.getRecordType());
            workflowSourceHeaderVO.setDateTime(objectHeaderDO.getDateTime());
            workflowSourceHeaderVO.setHistStatusChange(objectHeaderDO.getHistStatusChange());
            workflowSourceHeaderVO.setStartTime(objectHeaderDO.getStartTime());
            workflowSourceHeaderVO.setUserID(objectHeaderDO.getUserID());
            workflowSourceHeaderVO.setUserName(objectHeaderDO.getUserName());
            workflowSourceHeaderVO.setDescription(objectHeaderDO.getDescription());
            workHistoryMap.put(objectHeaderDO.getItemID(), workflowSourceHeaderVO);
        }
        return workHistoryMap;
    }

    /**
     * @param pcRltnshp
     * @param sourceMap
     * @param documents
     * @param lobs
     * @param systemDetailsMap
     * @param commentsMap
     * @param workHistoryMap
     * @param workflowSourceHeaderMap
     * @return
     */
    protected List assembleSourceItems(ParentChildRelationship pcRltnshp,
	        								Map sourceMap,
									 		List documents,
									 		List lobs,
									 		Map systemDetailsMap,
									 		Map commentsMap, 
									 		Map workHistoryMap,  
									 		Map workflowSourceHeaderMap) { 
		List sourceVOs = new ArrayList();
		List children = pcRltnshp.getChildren();	
		Iterator childIterator = children.iterator();	
		while (childIterator.hasNext()) {	
			String childKey = (String) childIterator.next();
			Object obj = sourceMap.get(childKey);
			if (obj != null) {
				SourceItem sourceDO = (SourceItem) obj;		
				WorkItemSource sourceVO = new WorkItemSource();
				assembleSourceFields(sourceDO, sourceVO);
				SystemDetails details = (SystemDetails)systemDetailsMap.get(sourceDO.getSystemName());
				if(details == null){
				    details = getSystemDetailsForUser(sourceDO.getSystemName());
				    systemDetailsMap.put(sourceDO.getSystemName(), details);
				}
				setSystemDetails(sourceVO, details);
				
				if (documents.size() > 0) {
					assembleSourceDocumentData(sourceVO, documents);
				}
				if (lobs.size() > 0) {
					sourceVO.setLobData(assembleLOBs(sourceDO, lobs)); 
				}

				String sourceItemID = sourceVO.getItemID();
				if (commentsMap.containsKey(sourceItemID)) {	 
				    sourceVO.setComments((List)commentsMap.get(sourceItemID));			
				}
				if (workHistoryMap.containsKey(sourceItemID)) {  
				    sourceVO.setHistory((List) workHistoryMap.get(sourceItemID));  			
	            }   
	            if (workflowSourceHeaderMap.containsKey(sourceItemID)) { 
	                sourceVO.setWorkflowSourceHeader((WorkflowSourceHeader) workflowSourceHeaderMap.get(sourceItemID));  
	            } 	            
				sourceVOs.add(sourceVO);
			}
		}
		return sourceVOs;
	}

    /**
     * @param result
     * @param retrieveWorkResult
     */
    protected void assembleNetServer(Result result, RetrieveWorkResult retrieveWorkResult, List data ) {
        List workItems = new ArrayList();
        List lobs = new ArrayList();
        List comments = new ArrayList();
        List historyComments = new ArrayList(); 
        List objectHeaders =  new ArrayList();  
        List sourceItems = new ArrayList();
        List documents = new ArrayList();
        Map pcRltnshpMap = new HashMap();
 
        if (data != null && !data.isEmpty()) {
            Iterator dataObjects = data.iterator();
            Object currentObj;
            ParentChildRelationship pcRltnshp; 
            while (dataObjects.hasNext()) {
                currentObj = dataObjects.next();
                // populate the temporary lists for each type of data object
                if (currentObj instanceof WorkItem) {
                    if (!workItems.contains(currentObj)) {
                        workItems.add(currentObj);
                    }   
                } else if (currentObj instanceof ConsolidatedComment) {
                    comments.add(currentObj);
                } else if (currentObj instanceof ConsolidatedHistory) { 
                    historyComments.add(currentObj); 
                } else if (currentObj instanceof ObjectHeader) { 
                    objectHeaders.add(currentObj);                     
                } else if (currentObj instanceof LOB) {
                    lobs.add(currentObj);
                } else if (currentObj instanceof SourceItem) {  
                    sourceItems.add(currentObj);    
                } else if (currentObj instanceof Document) {
                    documents.add(currentObj);
                } else if (currentObj instanceof ParentChildRelationship) {
                    pcRltnshp = (ParentChildRelationship) currentObj;
                    pcRltnshpMap.put(pcRltnshp.getItemKey(), pcRltnshp);  
                }  
            }                   
        }


        // construct map of all source objects

        Map sourceMap = new HashMap(sourceItems.size());

        Iterator sourceItemIter = sourceItems.iterator();

        boolean createDummy = false;
        if(workItems.isEmpty()) {
            createDummy = true;
        }

        while (sourceItemIter.hasNext()) {
            SourceItem sourceDO = (SourceItem) sourceItemIter.next();
            sourceMap.put(sourceDO.getItemID(), sourceDO);

            if(createDummy ) {
                WorkItem workItemDummy = new WorkItem(); 
                workItemDummy.setItemID(sourceDO.getItemID());
                workItemDummy.setRecordType(sourceDO.getRecordType());
                workItems.add(workItemDummy);
                ParentChildRelationship pcRltnshp = new ParentChildRelationship();
                pcRltnshp.setItemKey(sourceDO.getItemID());
                List childs = new ArrayList();
                childs.add(sourceDO.getItemID());
                pcRltnshp.setChildren(childs);
                pcRltnshp.setParentKey(workItemDummy);
                pcRltnshpMap.put(pcRltnshp.getItemKey(), pcRltnshp);
            }

        }   
 
        Map lobMap = new HashMap();

        Iterator lobIterator = lobs.iterator();
        while (lobIterator.hasNext()) {
            LOB lobDO = (LOB) lobIterator.next();
            String lobItemID = lobDO.getItemID();
            if (lobItemID != null) {
                List currentLOBs = (List)lobMap.get(lobItemID);
                if(currentLOBs == null){
                    currentLOBs = new ArrayList();
                }

                Iterator currentLOBiterator = currentLOBs.iterator();
                boolean flag = false;
                String dataname = null;
                String seqNo = null;
                LobData lobdata = null;
                while(currentLOBiterator.hasNext()){
                    lobdata = (LobData)currentLOBiterator.next();
                    dataname = lobdata.getDataName();
                    seqNo = lobdata.getSequenceNmbr();
                    if(dataname.equals(lobDO.getName()) && seqNo.equals(lobDO.getSequence())){
                        flag = true;
                        break;
                    }
                }                   
                if(flag == false){

                    LobData lobDataVO = new LobData();
                    lobDataVO.setDataName(lobDO.getName());
                    lobDataVO.setDataValue(lobDO.getValue());
                    lobDataVO.setIdentifier(lobDO.getIdentifier());
                    lobDataVO.setSequenceNmbr(lobDO.getSequence());
                    currentLOBs.add(lobDataVO);
                }                  
                lobMap.put(lobItemID, currentLOBs);
            }
        }
 
        Map commentsMap = new HashMap();
        Iterator inputComments = comments.iterator();
        while (inputComments.hasNext()) {
            ConsolidatedComment  commentDO = (ConsolidatedComment) inputComments.next();            
            Comment commentVO = new Comment();
            commentVO.setCommentType(commentDO.getCommentType());
            commentVO.setIdentifier(commentDO.getIdentifier());
            commentVO.setDateTime(commentDO.getDateTime());
            commentVO.setUserID(commentDO.getUserID());
            commentVO.setText(commentDO.getText());
            commentVO.setStartTime(commentDO.getStartTime());
            commentVO.setDescription(commentDO.getDescription());            
            commentVO.setRecordType(commentDO.getRecordType());            
            List currentComments = (List)commentsMap.get(commentDO.getItemID());
            if(currentComments == null){
                currentComments = new ArrayList();
            }
            currentComments.add(commentVO);
            commentsMap.put(commentDO.getItemID(), currentComments);
        }
 

        Map workHistoryMap = getHistoryMap(historyComments);
        Map workflowSourceHeaderMap = getWorkflowSourceHeaderMap(objectHeaders);   

        Map systemDetailsMap = new HashMap();
        
        // loop thru the work items, for each one:
        // create a new WorkItem VO, and populate the individual fields
        List workItemVOs = new ArrayList(); 
        Iterator workItemsIterator = workItems.iterator();
        while (workItemsIterator.hasNext()) {
            WorkItem workItemDO = (WorkItem) workItemsIterator.next();      
            com.csc.fs.accel.valueobject.WorkItem workItemVO = new com.csc.fs.accel.valueobject.WorkItem();
            assembleWorkItemFields(workItemDO, workItemVO);
            SystemDetails details = (SystemDetails)systemDetailsMap.get(workItemDO.getSystemName());
            if(details == null){
                details = getSystemDetailsForUser(workItemDO.getSystemName());
                systemDetailsMap.put(workItemDO.getSystemName(), details);
            }
            setSystemDetails(workItemVO, details);
        
            // set external systems data
            // workItemVO.setExternalSystems(assembleExtSysData((Item)workItemDO));
            // set source children
            if(!sourceMap.isEmpty()){
                ParentChildRelationship pcRel = (ParentChildRelationship) pcRltnshpMap.get(workItemDO.getItemID());     
                if (pcRel != null) {
                    if (sourceItems.size() > 0) {
                        workItemVO.setSourceChildren(assembleSourceItems(pcRel, sourceMap, documents, lobs, systemDetailsMap, commentsMap, workHistoryMap, workflowSourceHeaderMap));   
                    }
                }
            }
            // set LOBs
            setLOBToDisplay(workItemVO, workItemDO, lobMap);  

            // set Comments
            String itemID = workItemDO.getItemID(); 
            if (commentsMap.containsKey(itemID)) {  
                workItemVO.setComments((List)commentsMap.get(itemID));          
            }
            if (workHistoryMap.containsKey(itemID)) { 
                workItemVO.setHistory((List) workHistoryMap.get(itemID));           
            } 

            workItemVO.setSuspendDateTime(workItemDO.getSuspendDateTime());
            workItemVO.setSuspendFlag(workItemDO.getSuspended());
            if(null != workItemDO.getActivationDateTime()) {
                workItemVO.setActivationDateTime(ServiceHelper.stringToDate(workItemDO.getActivationDateTime()));
            }
            workItemVO.setActivationStatus(workItemDO.getActivationStatus());
            workItemVO.setSuspendOriginator(workItemDO.getUserID());
            workItemVO.setSuspendReason(workItemDO.getSuspendReason());
            workItemVO.setUserID(workItemDO.getUserID());

            workItemVOs.add(workItemVO);
        }
        retrieveWorkResult.setWorkItems(workItemVOs);
    }
    
}
