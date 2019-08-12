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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.CreateWorkResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItemRequest;
import com.csc.fs.accel.workflow.Constants;
import com.csc.fs.accel.workflow.assembler.WorkflowAccelTransformation;
import com.csc.fs.dataobject.accel.workflow.CreateRelationship;
import com.csc.fs.dataobject.accel.workflow.LOB;
import com.csc.fs.dataobject.accel.workflow.Suspense;
import com.csc.fs.dataobject.accel.workflow.WorkItem;
import com.csc.fs.om.ObjectFactory;

public class CreateWorkAssembler extends WorkflowAccelTransformation { 

	public Result disassemble(Object input) {
		WorkItemRequest requestVO = (WorkItemRequest)input;
		String userID = getWorkflowUserId(requestVO.getSystemName());
		String hostName = getHostName();
		if ( (hostName != null) && (hostName.length() > 8) ) {
			hostName = hostName.substring(0, 8);		
		}
		String timestamp = "";	//NBA146 <createTime> is ignored by AWD so there is no reason to get a value
		List data = new ArrayList();
		List topLevelItems = requestVO.getWorkItems();
		if (topLevelItems != null) {
			Iterator iter = topLevelItems.iterator();
			while(iter.hasNext()){
				com.csc.fs.accel.valueobject.WorkItem wi = (com.csc.fs.accel.valueobject.WorkItem)iter.next();
				processWorkItem(wi, userID, hostName, timestamp, data);	
				List childWorkItems = wi.getWorkItemChildren();
				if (childWorkItems != null && !childWorkItems.isEmpty()) {
					Iterator childIter = childWorkItems.iterator();
					while(childIter.hasNext()){
						com.csc.fs.accel.valueobject.WorkItem child = (com.csc.fs.accel.valueobject.WorkItem)iter.next();
						processWorkItem(child, userID, hostName, timestamp, data);
						createRelationship(wi, child, data);
					}
				}
			}
		}
		return Result.Factory.create().addResult(data);
	}
	
	private void processWorkItem(com.csc.fs.accel.valueobject.WorkItem workItemVO, String userID, String hostName, String timestamp, List data) {
		// process work item fields
		
		WorkItem workItem = new WorkItem();
		workItem.setItemID(workItem.getIdentifier());
		workItem.setAction(Constants.ACTION_CREATE_LOCKED);
		workItem.setCreateTime(timestamp);
		workItem.setRecordType(workItemVO.getRecordType());
		workItem.setCreateNode(workItemVO.getCreateNode());
		workItem.setBusinessArea(workItemVO.getBusinessArea());
		workItem.setWorkType(workItemVO.getWorkType());
		workItem.setWorkStatus(workItemVO.getStatus());
		workItem.setQueue(workItemVO.getQueueID());
		workItem.setPriority(workItemVO.getPriority());
		workItem.setLockedBy(workItemVO.getLockStatus());
		workItem.setCsdScreen(workItemVO.getScreenName());
		workItem.setSuspended(workItemVO.getSuspendFlag());
		workItem.setUserID(userID);
		workItem.setCreateStation(hostName);
		workItem.setBeginWork(timestamp);
		workItem.setEndWork(timestamp);
		workItem.setIgnoreLock(workItemVO.isIgnoreLock());
		data.add(workItem);

		// process LOBs
		List inputLOBs = workItemVO.getLobData();
		Iterator iter = inputLOBs.iterator();
		while(iter.hasNext()){
			LobData lob = (LobData)iter.next();
			if(lob.getIndexFld() != null && lob.getIndexFld().startsWith("INXFLD")){
				String indexFieldName = lob.getIndexFld();
				if(indexFieldName.equals("INXFLD01")){
					workItem.setIndex_1(getLOBValue(lob.getDataValue()));
				} else if(indexFieldName.equals("INXFLD02")){
					workItem.setIndex_2(getLOBValue(lob.getDataValue()));
				} else if(indexFieldName.equals("INXFLD03")){
					workItem.setIndex_3(getLOBValue(lob.getDataValue()));
				} else if(indexFieldName.equals("INXFLD04")){
					workItem.setIndex_4(getLOBValue(lob.getDataValue()));
				}
			} else {
				LOB lobDO = (LOB) ObjectFactory.create(LOB.class);	
				lobDO.setItemID(workItem.getItemID());
				lobDO.setUpdateFlag(Constants.ACTION_ADD);
				
				//lshine, 06.13.2006: per Preston, default this if null since getter on DO requires it. 
				if(lob.getSequenceNmbr() == null)
				    lobDO.setSequence("001");	//lshine: this value is an AWD standard
				else
				    lobDO.setSequence(lob.getSequenceNmbr());
				
				lobDO.setName(lob.getDataName());
				lobDO.setValue(getLOBValue(lob.getDataValue()));
				int dataLength = lobDO.getValue().trim().length();
				lobDO.setLength(String.valueOf(dataLength));
				data.add(lobDO);
			}
		}		
		
		// check for suspense fields
		String suspendReason = workItemVO.getSuspendReason();
		if ( (suspendReason != null) && (suspendReason.trim().length() > 0) ) {
			Suspense suspenseDO = (Suspense) ObjectFactory.create(Suspense.class);								
			suspenseDO.setSuspCode(suspendReason.trim());
			Date actDateTime = workItemVO.getActivationDateTime();
			if (actDateTime != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS");
				suspenseDO.setActivateDateTime(sdf.format(actDateTime));
			}
			if (workItemVO.getActivationStatus() != null) {
				suspenseDO.setActivateStatus(workItemVO.getActivationStatus().trim());
			}
			suspenseDO.setSuspDateTime(timestamp);
			suspenseDO.setSuspUserID(userID);			
			suspenseDO.setViewAction(Constants.ACTION_SUSPEND);
			suspenseDO.setItemID(workItem.getItemID());
			data.add(suspenseDO);
		}
	}
	
	private void createRelationship(com.csc.fs.accel.valueobject.WorkItem parentItem,
									com.csc.fs.accel.valueobject.WorkItem childItem, List data) {
	
		CreateRelationship rltnshp = (CreateRelationship) ObjectFactory.create(CreateRelationship.class);
		rltnshp.setRelationshipType("2");
		rltnshp.setParentItemID(parentItem.getItemID());
		rltnshp.setChildItemID(childItem.getItemID());
		data.add(rltnshp);
	}
    /**  
     * Assemble the data objects into value objects. First collect the LOBs.
     * Then hydrate each WorkItem value object from its corresponding data object
     * and attach value objects for each LOB data object with a matching id from the
     * LOB collection.
     * @param result - map containing the data objects from the interaction map 
     * @return Result - a GetWorkResult containing the WorkItem value objects
     * @see com.csc.fs.accel.AccelTransformation#assemble(com.csc.fs.Result)  
     */
	// NBA146 New Method
    public Result assemble(Result result) {
        CreateWorkResult createWorkResult = new CreateWorkResult();
        if (result.hasErrors()) {
            createWorkResult.merge(result);
            return createWorkResult;
        }
        List data = result.getReturnData();
        List lobData = new ArrayList(); //NBA146
        if (data != null && !data.isEmpty()) {
            Iterator dataObjects = data.iterator();
            while (dataObjects.hasNext()) {
                Object currentObj = dataObjects.next();
                if (currentObj instanceof LOB) {
                    lobData.add(currentObj);
                }
            }
            dataObjects = data.iterator();
            boolean selectedFound = false;
            while (dataObjects.hasNext() && !selectedFound) {
                Object currentObj = dataObjects.next();
                if (currentObj instanceof WorkItem) {
                    WorkItem item = (WorkItem) currentObj;
                    selectedFound = true;
                    com.csc.fs.accel.valueobject.WorkItem workItem = new com.csc.fs.accel.valueobject.WorkItem();
                    assembleWorkItemFields(item, workItem);
                    workItem.setLobData(assembleLOBs(item, lobData));
                    createWorkResult.setWorkItem(workItem);
                }
            }
        }
        return createWorkResult;
    }
	
}
