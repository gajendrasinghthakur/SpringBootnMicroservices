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

import java.util.Iterator;
import java.util.List;

import com.csc.fs.ObjectRepository;
import com.csc.fs.Result;
import com.csc.fs.dataobject.accel.AccelDataObject;
import com.csc.fs.dataobject.accel.workflow.ItemID;
import com.csc.fs.dataobject.accel.workflow.Link;
import com.csc.fs.dataobject.accel.workflow.WorkField;
import com.csc.fs.dataobject.accel.workflow.WorkItem;
import com.csc.fs.sa.PostProcess;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * RetrieveParentPostProcessor contains post-process logic for RetrieveWorkPostProcessor
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL5055 - NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @since New Business Accelerator - Version NB-1401
 */

public class RetrieveWorkPostProcessor extends com.csc.fsg.nba.workflow.api.rest.RestPostProcessor implements PostProcess {
    /**
     * Post-process logic for RetrieveWorkPostProcessor api.
     * If there is no work item in the response, set ItemID.itemID to null to prevent the Lock API from being performed.
     * Call processWorkItem() to process the work item.
     * 
     */
    public Result systemApi(List input, Result result, SystemService service, SystemAPI api, ObjectRepository or) {
        if (!result.hasErrors()) {
            List returnData = result.getReturnData();
            if (returnData != null && !returnData.isEmpty()) {
                ItemID currentItemID = getItemID(input);
                WorkItem currentWorkItem = getCurrentWorkItem(returnData);
                if (currentWorkItem == null) {
                    currentItemID.setItemID("");
                } else {
                    List<Link> selectedLinks = processLinks(input, returnData);	//APSL5055 - NBA331.1
                    processWorkItem(result, api, input, currentWorkItem, currentItemID, selectedLinks); //APSL5055 - NBA331,1
                }
            }
        }
        return result;
    }

    /**
     * Add an ItemID to the input to allow subsequent API calls to point to the current work item
     * @param input
     * @param it
     * @param currentObj
     */
    protected void processWorkItem(Result result, SystemAPI api, List input, WorkItem currentWorkItem, ItemID currentItemID, List<Link> selectedLinks) { //APSL5055 - NBA331.1
        processWorkItem(api, currentWorkItem, selectedLinks);  //APSL5055 - NBA331.1
        currentItemID.setItemID(currentWorkItem.getItemID());
        currentItemID.setRecordType(currentWorkItem.getRecordType());
        if ("L".equals(currentItemID.getAction())) {
            result.getReturnData().clear();
        } else {
            List returnData = result.getReturnData();
            Iterator rit = returnData.iterator(); //Assume multiple Results
            while (rit.hasNext()) {
                Result currentResult = (Result) rit.next();
                if (!currentResult.hasErrors()) {
                    List currentData = currentResult.getReturnData();
                    Iterator dataObjects = currentData.iterator();
                    while (dataObjects.hasNext()) {
                        Object currentObj = dataObjects.next();
                        if (currentObj instanceof AccelDataObject) {
                            if (!(currentObj instanceof WorkItem)) {
                                AccelDataObject ado = (AccelDataObject) currentObj;
                                ado.setSystemName(api.getSystemName());
                                ado.setParentIdentifier(currentWorkItem.getIdentifier());
                                if (ado instanceof WorkField) { //APSL5055 - NBA331.1
                                    ((WorkField) ado).setParentID(currentWorkItem.getItemID());    //APSL5055 - NBA331.1
                                }   //APSL5055 - NBA331.1
                                ado.markTransient();
                            }
                        }
                    }
                }
            }
        }
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
