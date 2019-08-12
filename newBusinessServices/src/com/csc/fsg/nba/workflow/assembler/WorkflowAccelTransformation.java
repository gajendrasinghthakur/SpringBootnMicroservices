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
import com.csc.fs.accel.result.GetWorkResult;
import com.csc.fs.accel.result.RetrieveWorkResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.dataobject.accel.workflow.Item;
import com.csc.fs.dataobject.accel.workflow.LOB;
import com.csc.fs.dataobject.accel.workflow.Link;
import com.csc.fs.dataobject.accel.workflow.SourceItem;
import com.csc.fs.dataobject.accel.workflow.WorkField;
import com.csc.fs.dataobject.accel.workflow.WorkItem;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
/**
 * WorkflowAccelTransformation
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 *  <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @since New Business Accelerator - Version NB-1401
 */
public abstract class WorkflowAccelTransformation extends com.csc.fs.accel.workflow.assembler.WorkflowAccelTransformation {
    /**
     * Determine if the system is AWDREST
     * @return
     */
    protected boolean isREST() {
        Result result = getServiceContext().getUserSession().getSystem(NbaConstants.AWDREST);
        if (!result.hasErrors()) {
            return result.getData() != null;
        }
        return false;
    }

    /**
     * Assemble a work item retrieved with NetServer.
     * @param result
     * @param getWorkResult
     * @param data
     */
    protected void assembleNetServer(Result result, GetWorkResult getWorkResult, List data) {
        Iterator dataObjects = data.iterator();
        Map processedWorkItems = new HashMap();
        Map processedSourceItems = new HashMap();
        com.csc.fs.accel.valueobject.WorkItem mycase = null;
        List transactions = new ArrayList();
        com.csc.fs.accel.valueobject.WorkItem lastWorkItem = null;
        while (dataObjects.hasNext()) {
            Object currentObj = dataObjects.next();
            if (currentObj instanceof WorkItem) {
                WorkItem item = (WorkItem) currentObj;
                if (!processedWorkItems.containsKey(item.getItemID())) {
                    processedWorkItems.put(item.getItemID(), null);
                    com.csc.fs.accel.valueobject.WorkItem workItem = new com.csc.fs.accel.valueobject.WorkItem();
                    assembleWorkItemFields(item, workItem);
                    lastWorkItem = workItem;
                    getLOBsForWorkItem(item, workItem, result); 
                    if (workItem.isCase()) {
                        mycase = workItem;
                    } else {
                        transactions.add(workItem);
                    }
                    if ("Y".equalsIgnoreCase(item.getSelected())) {
                        getWorkResult.setWorkItem(workItem);
                    } else {
                        if (getWorkResult.getRelatedWorkItems() == null) {
                            getWorkResult.setRelatedWorkItems(new ArrayList());
                        }
                        getWorkResult.getRelatedWorkItems().add(workItem);
                    }
                }
            } else if (currentObj instanceof SourceItem) {
                SourceItem sourceItem = (SourceItem) currentObj;
                if (!processedSourceItems.containsKey(sourceItem.getItemID()) && lastWorkItem != null) {
                    processedSourceItems.put(sourceItem.getItemID(), null);
                    WorkItemSource workItemSource = new WorkItemSource();
                    assembleSourceFields(sourceItem, workItemSource);
                    getLOBsForSourceItem(sourceItem, workItemSource,  result); 
                    lastWorkItem.getSourceChildren().add(workItemSource);
                }
            }
        }
        if (mycase != null) {
            mycase.setWorkItemChildren(transactions);
            getWorkResult.setCaseworkItem(mycase);
        }
    }

    /**
     * Assemble the LOB values for a work item retrieved with NetServer.
     * @param item
     * @param workItem
     */
    protected void getLOBsForWorkItem(WorkItem item, com.csc.fs.accel.valueobject.WorkItem workItem, Result lobResult) {
        // lob data is already retrieved for the workitem as part of lobResult, so instead of making
        // new call assembling them back. 
        List inputLOBS = getDataObjects(LOB.class, lobResult); 
        if (!inputLOBS.isEmpty()) {
            List lobs = assembleLOBs(item, inputLOBS);
            Map map = new HashMap();
            map.put(item.getItemID(), lobs);
            setLOBToDisplay(workItem, item, map);
        }
    }

    /**
     * Assemble the LOB values for a Source item retrieved with NetServer.
     * @param item
     * @param workItem
     */
    protected void getLOBsForSourceItem(SourceItem item, WorkItemSource workItemSource, Result lobResult) {
        // lob data is already retrieved for the workitem as part of lobResult, so instead of making
        // new call assembling them back. 
        List inputLOBS = getDataObjects(LOB.class, lobResult); 
        if (!inputLOBS.isEmpty()) {
            List lobs = assembleLOBs(item, inputLOBS);
            Map map = new HashMap();
            map.put(item.getItemID(), lobs);
            setLOBToDisplay(workItemSource, item, map);
        }
    } 
    
    /**
     * Assemble the various values for a Work item retrieved with REST.
     * @param workItemDO
     * @param workItemVO
     */
    protected void assembleRESTWorkItemFields(WorkItem workItemDO, com.csc.fs.accel.valueobject.WorkItem workItemVO) {
        super.assembleWorkItemFields(workItemDO, workItemVO);
        workItemVO.setSelected("true".equals(workItemDO.getSelected()));
        workItemVO.setAssignedTo(workItemDO.getAssignedTo());
        workItemVO.setSuspendOriginator(workItemDO.getSuspendOriginator());	//APSL5055-NBA331.1
        workItemVO.setHasChildren("Y".equals(workItemDO.getHasChildren())); //APSL5055-NBA331.1
        workItemVO.setHasParent("Y".equals(workItemDO.getHasParent())); //APSL5055-NBA331.1
        workItemVO.setParentWorkItemID(workItemDO.getParentID()); //APSL5055-NBA331.1
        workItemVO.setSuspendReason(workItemDO.getSuspendReason());        
    }

    /**
     * Assemble the work values for a Work item retrieved with REST.
     * @param workItemDO
     * @param workItemVO
     */
    protected void assembleRESTWorkValues(WorkItem workItemDO, com.csc.fs.accel.valueobject.WorkItem workItemVO, List<WorkField> workFields) {
        List workValues = assembleRESTWorkValues(workItemDO, workFields);
        Map map = new HashMap();
        map.put(workItemDO.getItemID(), workValues);
        setLOBToDisplay(workItemVO, workItemDO, map);
    }

    /**
     * Assemble the work values for a Source item retrieved with REST.
     * @param sourceItemDO
     * @param sourceItemVO
     */
    protected void assembleRESTWorkValues(SourceItem sourceItemDO, WorkItemSource sourceItemVO, List<WorkField> workFields) {
        List workValues = assembleRESTWorkValues(sourceItemDO, workFields);
        Map map = new HashMap();
        map.put(sourceItemDO.getItemID(), workValues);
        setLOBToDisplay(sourceItemVO, sourceItemDO, map);
    }

    /**
     * Create LobData objects from the WorkField objects for the current item.
     * @param parentItem
     * @param workFields
     * @return
     */
    protected List assembleRESTWorkValues(Item parentItem, List<WorkField> workFields) {
        List currentWorkValues = null;
        if (!workFields.isEmpty()) {
            currentWorkValues = new ArrayList(25);
            Map currentWorkValuesMap = new HashMap();
            Iterator it = workFields.iterator();
            while (it.hasNext()) {
                WorkField workField = (WorkField) it.next();
                String workFieldParentID = workField.getParentID();
                if (workFieldParentID != null && workFieldParentID.equals(parentItem.getItemID())) {
                  //In certain situations, the Case is retrieve multiple times, so there may be duplicate work values
                    if (!currentWorkValuesMap.containsKey(workField.getFieldName())) {  //APSL5055-NBA331.1  
                        currentWorkValuesMap.put(workField.getFieldName(), workField.getFieldName());  //APSL5055-NBA331.1  
                        LobData lobDataVO = new LobData();
                        lobDataVO.setDataName(workField.getFieldName());
                        lobDataVO.setDataValue(workField.getFieldValue());
                        lobDataVO.setIdentifier(workField.getIdentifier());
                        lobDataVO.setSequenceNmbr("001"); // REST does not return sequence numbers
                        currentWorkValues.add(lobDataVO);
                        //APSL5055-NBA331.1 code deleted
                    }
                    it.remove();
                }
            }
            //APSL5055-NBA331.1 code deleted
        } else {
            currentWorkValues = new ArrayList(1);
        }
        return currentWorkValues;
    }

    //APSL5055-NBA331.1 code deleted

    /**
     * Assemble a Work or Source item retrieved with REST.
     * @param result
     * @param getWorkResult
     * @param data
     * @return
     */
    protected Result assembleREST(Result result, GetWorkResult getWorkResult, List data) {
        Map processedWorkItems = new HashMap();
        Map processedSourceItems = new HashMap();
        com.csc.fs.accel.valueobject.WorkItem mycase = null;
        List<com.csc.fs.accel.valueobject.WorkItem> transactions = new ArrayList();
        com.csc.fs.accel.valueobject.WorkItem lastWorkItem = null;
        getWorkResult.setRelatedWorkItems(new ArrayList());
        List<WorkField> workFields = getDataObjectsWithRemove(WorkField.class, result);
        List<Link> links = getDataObjectsWithRemove(Link.class, result);
        Iterator it = data.iterator();
        while (it.hasNext()) {
            Object currentObject = it.next();
            if (currentObject instanceof WorkItem) {
                WorkItem workItemDO = (WorkItem) currentObject;
                if (!processedWorkItems.containsKey(workItemDO.getItemID())) {
                    processedWorkItems.put(workItemDO.getItemID(), null);
                    com.csc.fs.accel.valueobject.WorkItem workItemVO = new com.csc.fs.accel.valueobject.WorkItem();
                    assembleRESTWorkItemFields(workItemDO, workItemVO);
                    lastWorkItem = workItemVO;
                    assembleRESTWorkValues(workItemDO, workItemVO, workFields);
                    //APSL5055-NBA331.1 code deleted
                    getWorkResult.setSystemName(workItemVO.getSystemName());
                    
                    if (workItemVO.isCase()) {
                        mycase = workItemVO;
                    } else {
                        transactions.add(workItemVO);
                    }
                    if (workItemVO.isSelected()) {
                        getWorkResult.setWorkItem(workItemVO);
                    } else {
                        getWorkResult.getRelatedWorkItems().add(workItemVO);
                    }
                }
            } else if (currentObject instanceof SourceItem) {
                SourceItem sourceItem = (SourceItem) currentObject;
                if (!processedSourceItems.containsKey(sourceItem.getItemID()) && lastWorkItem != null) {
                    processedSourceItems.put(sourceItem.getItemID(), null);
                    WorkItemSource workItemSource = new WorkItemSource();
                    assembleSourceFields(sourceItem, workItemSource);
                    getLOBsForSourceItem(sourceItem, workItemSource, result);
                    assembleRESTLinks(workItemSource, links);  //APSL5055-NBA331.1
                    lastWorkItem.getSourceChildren().add(workItemSource);
                }
            }
        }
        if (mycase != null) {
            mycase.setWorkItemChildren(transactions);
            getWorkResult.setCaseworkItem(mycase);
        }
        return getWorkResult;
    }
    /**
     * @param systemDetailsMap
     * @param sourceDO
     * @param sourceVO
     */
    protected void assembleRESTSourceFields(Map systemDetailsMap, SourceItem sourceDO, WorkItemSource sourceVO) {
        super.assembleSourceFields(sourceDO, sourceVO);
        sourceVO.setParentWorkItemID(sourceDO.getParentWorkItemID());
        SystemDetails details = (SystemDetails) systemDetailsMap.get(sourceDO.getSystemName());
        if (details == null) {
            details = getSystemDetailsForUser(sourceDO.getSystemName());
            systemDetailsMap.put(sourceDO.getSystemName(), details);
        }
        setSystemDetails(sourceVO, details);
    }
    /**
     * Assemble a Work or Source item retrieved with REST.
     * @param result
     * @param retrieveWorkResult
     * @param data
     * @return
     */
    protected Result assembleREST(Result result, RetrieveWorkResult retrieveWorkResult, List data) {
        Map processedWorkItems = new HashMap();
        Map processedSourceItems = new HashMap();
        Map systemDetailsMap = new HashMap(); 
        boolean hasSourceProcessed = false; //APSL5055
        List<WorkField> workFields = getDataObjectsWithRemove(WorkField.class, result);
        List<Link> links = getDataObjectsWithRemove(Link.class, result);
        Iterator it = data.iterator();
        while (it.hasNext()) {
            Object currentObject = it.next();
            if (currentObject instanceof WorkItem) {
                WorkItem workItemDO = (WorkItem) currentObject;
                if (!processedWorkItems.containsKey(workItemDO.getItemID())) {
                    processedWorkItems.put(workItemDO.getItemID(), null);
                    com.csc.fs.accel.valueobject.WorkItem workItemVO = new com.csc.fs.accel.valueobject.WorkItem();
                    assembleRESTWorkItemFields(workItemDO, workItemVO);

                    assembleRESTWorkValues(workItemDO, workItemVO, workFields);
                    //APSL5055-NBA331.1 code deleted
                    retrieveWorkResult.getWorkItems().add(workItemVO);
                    it.remove();
                }
            }
        }
        it = data.iterator();
        while (it.hasNext()) {
            Object currentObject = it.next();
            if (currentObject instanceof SourceItem) {
                SourceItem sourceItemDO = (SourceItem) currentObject;
                //Begin APSL5055
                hasSourceProcessed = false;
                List parentWorkItemIdList = new ArrayList();
                if (!processedSourceItems.containsKey(sourceItemDO.getItemID())) {
                	hasSourceProcessed = false;
                } else if (processedSourceItems.containsKey(sourceItemDO.getItemID()) && sourceItemDO.getParentWorkItemID() == null){
                	hasSourceProcessed = true;
                } else if (processedSourceItems.containsKey(sourceItemDO.getItemID()) && processedSourceItems.get(sourceItemDO.getItemID()) instanceof List){
                	parentWorkItemIdList = (ArrayList)processedSourceItems.get(sourceItemDO.getItemID());
                	if(!NbaUtils.isBlankOrNull(parentWorkItemIdList)){
                		Iterator wiit = parentWorkItemIdList.iterator();
                        while (wiit.hasNext()) {
                            String parentWorkItemId = (String) wiit.next();
                            if(sourceItemDO.getParentWorkItemID().equals(parentWorkItemId)){
                            	hasSourceProcessed = true; 
                            	break;
                            }
                        }                        
                	}
                }
                
                if(!hasSourceProcessed && sourceItemDO.getParentWorkItemID() != null){
                	parentWorkItemIdList.add(sourceItemDO.getParentWorkItemID());
                }                
                //End APSL5055           
                
                if (!hasSourceProcessed) {   //APSL5055             	
                    processedSourceItems.put(sourceItemDO.getItemID(), parentWorkItemIdList);   //APSL5055               
                    WorkItemSource workItemSourceVO = new WorkItemSource();
                    assembleRESTSourceFields(systemDetailsMap, sourceItemDO, workItemSourceVO);
                    assembleRESTWorkValues(sourceItemDO, workItemSourceVO, workFields);
                    assembleRESTLinks(workItemSourceVO, links);
                    com.csc.fs.accel.valueobject.WorkItem parentWorkItem = getRESTParent(retrieveWorkResult, workItemSourceVO.getParentWorkItemID());
                    if (parentWorkItem == null) {
                        retrieveWorkResult.getSourceItems().add(workItemSourceVO);
                    } else {
                        parentWorkItem.getSourceChildren().add(workItemSourceVO);
                    }
                }
            }
        }
        return retrieveWorkResult;
    }
    /**
     * Locate the parent work item for a Work or Source item retrieved with REST.
     * @param retrieveWorkResult
     * @param parentWorkItemID
     * @return
     */
    protected com.csc.fs.accel.valueobject.WorkItem getRESTParent(RetrieveWorkResult retrieveWorkResult, String parentWorkItemID) {
        Iterator it = retrieveWorkResult.getWorkItems().iterator();
        while (it.hasNext()) {
            com.csc.fs.accel.valueobject.WorkItem workItem = (com.csc.fs.accel.valueobject.WorkItem) it.next();
            if (workItem.getItemID().equals(parentWorkItemID)) {
                return workItem;
            }
        }
        return null;
    }

    //APSL5055-NBA331.1 code deleted
    /**
     * Assemble a Link
     * @param sourceItemVO
     * @param links
     */
    protected void assembleRESTLinks( com.csc.fs.accel.valueobject.WorkItemSource sourceItemVO, List<Link> links) {
        if (!links.isEmpty()) {
            String workItemIdentifier = sourceItemVO.getIdentifier();
            Iterator it = links.iterator();
            while (it.hasNext()) {
                Link linkDO = (Link) it.next();
                String linkParenIdentifier = linkDO.getParentIdentifier();
                if (workItemIdentifier.equals(linkParenIdentifier)) {
                    com.csc.fs.accel.valueobject.Link linkVO = new com.csc.fs.accel.valueobject.Link();
                    sourceItemVO.getLinks().add(linkVO);
                    linkVO.setLinkType(linkDO.getLinkType());
                    linkVO.setLinkHref(linkDO.getLinkHref());
                    it.remove();
                }
            }
        }
    }
    /**
     * Return a list of all objects of the resultType, removing them from Result.data,
     * @param resultType
     * @param source
     * @return
     */
    protected List getDataObjectsWithRemove(Class resultType, Object source) {
        List results = new ArrayList();
        if (resultType.isInstance(source)) {
            results.add(source);
        } else {
            if (source instanceof Result) {
                List data = ((Result) source).getData();
                Iterator iter = data.iterator();
                while (iter.hasNext()) {
                    Object item = iter.next();
                    if (item instanceof Result) {
                        results.addAll(getDataObjects(resultType, item));
                    } else if (resultType.isInstance(item)) {
                        results.add(item);
                        iter.remove(); //Remove from Result
                    }
                }
            }
        }
        return results;
    }
}
