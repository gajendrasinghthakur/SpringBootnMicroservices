package com.csc.fsg.nba.workflow.api.rest;

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

import com.csc.fs.ObjectRepository;
import com.csc.fs.Result;
import com.csc.fs.dataobject.accel.workflow.Item;
import com.csc.fs.dataobject.accel.workflow.Link;
import com.csc.fs.dataobject.accel.workflow.ParentChildFind;
import com.csc.fs.dataobject.accel.workflow.ParentChildRelationship;
import com.csc.fs.dataobject.accel.workflow.SourceItem;
import com.csc.fs.dataobject.accel.workflow.WorkItem;
import com.csc.fs.om.ObjectFactory;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * ParentChildPostProcess contains post-process logic for retrieval of AWD Parent/Child Items
 * <p>
 * <b>Modifications:</b><br>
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
 
public class ParentChildPostProcess extends RestPostProcessor {

	/**
	 * Post-process logic for system api.
	 * 
	 */
	public Result systemApi(List input, Result result, SystemService service,
			SystemAPI api, ObjectRepository or) {

		if (result.hasErrors()) {
			return result;
		}
		List resultList = new ArrayList();
		ParentChildFind parentChildFind = null;
		WorkItem parentWorkItem = null;
		Iterator inputIterator = input.iterator();
		while (inputIterator.hasNext()) {
			Object inputObj = inputIterator.next();
			if (inputObj instanceof ParentChildFind) {
				parentChildFind = (ParentChildFind) inputObj;
			}
			if (inputObj instanceof WorkItem) {
                parentWorkItem = (WorkItem) inputObj;
                parentWorkItem.setChildren("N");
            }
		}

		if (parentChildFind == null) {
			return result;
		}

		String relationFlag = parentChildFind.getRelationFlag();
		if (relationFlag != null && relationFlag.equals("P")) { // items in result are children
			ParentChildRelationship pcRltnshp = (ParentChildRelationship) ObjectFactory
					.create(ParentChildRelationship.class);
			pcRltnshp.setItemKey(parentChildFind.getItemID());
			List children = pcRltnshp.getChildren();
			List data = result.getReturnData();
			List<Link> selectedLinks = processLinks(input, data);	//NBA331.1
			if (data != null && !data.isEmpty()) {
				Result result1 = (Result) data.get(0);
				if (!result1.hasErrors()) {
					List currentData = result1.getReturnData();
					Iterator dataObjects = currentData.iterator();
					while (dataObjects.hasNext()) {
                        Object currentObj = dataObjects.next();
                        if (currentObj instanceof WorkItem) {
                            parentWorkItem.setChildren("Y");
                            parentWorkItem.setRelation("1");
                            WorkItem workItem = (WorkItem) currentObj;
                            processWorkItem(api, workItem, selectedLinks);  //NBA331.1
                            workItem.setRelation("2");
                            ParentChildRelationship pcRltnshp1 = (ParentChildRelationship) ObjectFactory.create(ParentChildRelationship.class);
                            pcRltnshp1.setItemKey(workItem.getItemID());
                            children.add(workItem.getItemID());
                            pcRltnshp1 = addChildren(pcRltnshp1, result1);
                            resultList.add(pcRltnshp1);
                        } else if (currentObj instanceof SourceItem) {
                            parentWorkItem.setChildren("Y");
                            parentWorkItem.setRelation("1");
                            SourceItem sourceItem = (SourceItem) currentObj;
                            processSourceItem(api, sourceItem);
                            sourceItem.setParent("Y");
                            sourceItem.setRelation("2");
                            children.add(((Item) currentObj).getItemID());
                        }
                    }
					resultList.add(pcRltnshp);
					result1.addResults(resultList);
				}
			}
		}
		return result;
	}

	public ParentChildRelationship addChildren(
			ParentChildRelationship pcRltnshp, Result result) {
		String workitemID = pcRltnshp.getItemKey();
		List children1 = pcRltnshp.getChildren();
		List currentData = result.getReturnData();
		Iterator dataObjects = currentData.iterator();
		while (dataObjects.hasNext()) {
			Object currentObj = dataObjects.next();
			if (currentObj instanceof SourceItem) {
				if (((SourceItem) currentObj).getParentWorkItemID() != null) {
					if (((SourceItem) currentObj).getParentWorkItemID()
							.equalsIgnoreCase(workitemID)) {
						children1.add(((SourceItem) currentObj).getItemID());
					}
				}
			}
		}
		return pcRltnshp;
	}

	public List<String> getChildTransItemList(Result result) {
		List<String> childTransItemList = new ArrayList();
		List data = result.getReturnData();
		if (data != null && !data.isEmpty()) {
			Result result1 = (Result) data.get(0);
			if (!result1.hasErrors()) {
				List currentData = result1.getReturnData();
				Iterator dataObjects = currentData.iterator();
				while (dataObjects.hasNext()) {
					Object currentObj = dataObjects.next();
					if (currentObj instanceof WorkItem) {
						childTransItemList.add(((WorkItem) currentObj)
								.getItemID());
					}
				}
			}
		}
		return childTransItemList;
	}

	/**
	 * Post-process logic for system service.
	 * 
	 */
	public Result systemService(List input, Result result,
			SystemService service, ObjectRepository or) {
		return Result.Factory.create();
	}

}
