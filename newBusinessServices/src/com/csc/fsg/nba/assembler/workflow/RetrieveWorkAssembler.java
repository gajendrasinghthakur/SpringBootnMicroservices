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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.RetrieveWorkResult;
import com.csc.fs.accel.valueobject.Comment;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.accel.valueobject.WorkflowHistory;
import com.csc.fs.accel.valueobject.WorkflowSourceHeader;
import com.csc.fs.accel.workflow.assembler.WorkflowAccelTransformation;
import com.csc.fs.dataobject.accel.workflow.ConsolidatedComment;
import com.csc.fs.dataobject.accel.workflow.ConsolidatedHistory;
import com.csc.fs.dataobject.accel.workflow.Document;
import com.csc.fs.dataobject.accel.workflow.LOB;
import com.csc.fs.dataobject.accel.workflow.ObjectHeader;
import com.csc.fs.dataobject.accel.workflow.ParentChildRelationship;
import com.csc.fs.dataobject.accel.workflow.SourceItem;
import com.csc.fs.dataobject.accel.workflow.WorkItem;

public class RetrieveWorkAssembler extends WorkflowAccelTransformation {

	public Result assemble(Result result) {

		RetrieveWorkResult retrieveWorkResult = new RetrieveWorkResult();

		if (result.hasErrors()) {
			retrieveWorkResult.merge(result);
			return retrieveWorkResult;
		}

		List workItems = new ArrayList();
		List lobs = new ArrayList();
		List comments = new ArrayList();
		List historyComments = new ArrayList();	//NBA146
 		List objectHeaders =  new ArrayList();	//NBA146
		List sourceItems = new ArrayList();
		List documents = new ArrayList();
		Map pcRltnshpMap = new HashMap();
		//NBA146 code deleted	
				 		
		// long start = System.currentTimeMillis();
		List data = result.getReturnData();
		if (data != null && !data.isEmpty()) {
			Iterator dataObjects = data.iterator();
			while (dataObjects.hasNext()) {
				Object currentObj = dataObjects.next();
				// populate the temporary lists for each type of data object
				if (currentObj instanceof WorkItem) {
					if (!workItems.contains(currentObj)) { // SPR3290
						workItems.add(currentObj);
					}	
				} else if (currentObj instanceof ConsolidatedComment) {
					comments.add(currentObj);
				} else if (currentObj instanceof ConsolidatedHistory) {	//NBA146
					historyComments.add(currentObj);	//NBA146
				} else if (currentObj instanceof ObjectHeader) {	//NBA146
				    objectHeaders.add(currentObj);	//NBA146					
				} else if (currentObj instanceof LOB) {
					lobs.add(currentObj);
				} else if (currentObj instanceof SourceItem) {	
					sourceItems.add(currentObj);	
				} else if (currentObj instanceof Document) {
					documents.add(currentObj);
				} else if (currentObj instanceof ParentChildRelationship) {
					ParentChildRelationship pcRltnshp = (ParentChildRelationship) currentObj;
					pcRltnshpMap.put(pcRltnshp.getItemKey(), pcRltnshp);  
                }  
			}					
		}
		//long end = System.currentTimeMillis();
		//System.out.println(" Time taken to sort objects into lists [" + (end-start) + "]");

		// construct map of all source objects
		// start = System.currentTimeMillis();
		Map sourceMap = new HashMap(sourceItems.size());
		//NBA146 code deleted
		Iterator sourceItemIter = sourceItems.iterator();
		//begin NBA146
		boolean createDummy = false;
		if(workItems.isEmpty()) {
		    createDummy = true;
		}
		//end NBA146
		while (sourceItemIter.hasNext()) {
			SourceItem sourceDO = (SourceItem) sourceItemIter.next();
			sourceMap.put(sourceDO.getItemID(), sourceDO);
			//begin NBA146
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
			//end NBA146
		}	
		//end = System.currentTimeMillis();
		//System.out.println(" Time taken to sort sources into map [" + (end-start) + "]");
		
		//start = System.currentTimeMillis();
		Map lobMap = new HashMap();
		if(lobs != null){
			Iterator lobIterator = lobs.iterator();
			while (lobIterator.hasNext()) {
				LOB lobDO = (LOB) lobIterator.next();
				String lobItemID = lobDO.getItemID();
				if (lobItemID != null) {
				    List currentLOBs = (List)lobMap.get(lobItemID);
				    if(currentLOBs == null){
				        currentLOBs = new ArrayList();
				    }
					LobData lobDataVO = new LobData();
					lobDataVO.setDataName(lobDO.getName());
					lobDataVO.setDataValue(lobDO.getValue());
					lobDataVO.setIdentifier(lobDO.getIdentifier());
					lobDataVO.setSequenceNmbr(lobDO.getSequence());
					currentLOBs.add(lobDataVO);
					lobMap.put(lobItemID, currentLOBs);
				}
			}
		}
		//end = System.currentTimeMillis();
		//System.out.println(" Time taken to sort lob into map [" + (end-start) + "]");
		
		//start = System.currentTimeMillis();
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
			commentVO.setStartTime(commentDO.getStartTime());	//NBA146
			commentVO.setDescription(commentDO.getDescription());	//NBA146			
			commentVO.setRecordType(commentDO.getRecordType());	//NBA146			
			List currentComments = (List)commentsMap.get(commentDO.getItemID());
			if(currentComments == null){
			    currentComments = new ArrayList();
			}
			currentComments.add(commentVO);
			commentsMap.put(commentDO.getItemID(), currentComments);
		}
		//end = System.currentTimeMillis();
		//System.out.println(" Time taken to sort comments into map [" + (end-start) + "]");

		Map workHistoryMap = getHistoryMap(historyComments);	//NBA146
        Map workflowSourceHeaderMap = getWorkflowSourceHeaderMap(objectHeaders);	//NBA146

		Map systemDetailsMap = new HashMap();
		
		// loop thru the work items, for each one:
		// create a new WorkItem VO, and populate the individual fields
		List workItemVOs = new ArrayList(); 
		Iterator workItemsIterator = workItems.iterator();
		while (workItemsIterator.hasNext()) {
			// start = System.currentTimeMillis();
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
						workItemVO.setSourceChildren(assembleSourceItems(pcRel, sourceMap, documents, lobs, systemDetailsMap, commentsMap, workHistoryMap, workflowSourceHeaderMap));	//NBA146));
					}
				}
			}
			// set LOBs
			setLOBToDisplay(workItemVO, workItemDO, lobMap);  //NBA213

			//begin NBA146
			// set Comments
			String itemID = workItemDO.getItemID();	
			if (commentsMap.containsKey(itemID)) {	
				workItemVO.setComments((List)commentsMap.get(itemID));			
			}
			if (workHistoryMap.containsKey(itemID)) { 
                workItemVO.setHistory((List) workHistoryMap.get(itemID)); 			
            } 
			// end = System.currentTimeMillis();
			setSuspendInfo(workItemVO, workItemDO);  //NBA213
			//end NBA146
			workItemVOs.add(workItemVO);
		}
		retrieveWorkResult.setWorkItems(workItemVOs);
		return retrieveWorkResult;
	}
	
	/**
     * Convert ConsolidatedHistory DOs to WorkflowHistory VOs and store the results in a Map.
     * The Map keys are the parent item ids. The Map entries are arrays containing the WorkflowHistory VOs.
     * @param historyComments - ConsolidatedHistory DOs
     * @return the Map 
     */
	// NBA146 New Method
    private Map getHistoryMap(List historyComments) {
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
	// NBA146 New Method    
    private Map getWorkflowSourceHeaderMap(List objectHeaders) {
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

    private List assembleSourceItems(ParentChildRelationship pcRltnshp,
	        								Map sourceMap,
									 		List documents,
									 		List lobs,
									 		Map systemDetailsMap,
									 		Map commentsMap, 
									 		Map workHistoryMap,  
									 		Map workflowSourceHeaderMap) { //NBA146 changed method signature
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
				
				//NBA146 code deleted
				if (documents.size() > 0) {
					assembleSourceDocumentData(sourceVO, documents);
				}
				if (lobs.size() > 0) {
					sourceVO.setLobData(assembleLOBs(sourceDO, lobs)); // SPR3290
				}
				//begin NBA146
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
	            //end NBA146
				sourceVOs.add(sourceVO);
			}
		}
		return sourceVOs;
	}
}