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
import com.csc.fs.dataobject.accel.AccelDataObject;
import com.csc.fs.dataobject.accel.workflow.ItemID;
import com.csc.fs.dataobject.accel.workflow.Link;
import com.csc.fs.dataobject.accel.workflow.Response;
import com.csc.fs.dataobject.accel.workflow.SourceItem;
import com.csc.fs.dataobject.accel.workflow.WorkField;
import com.csc.fs.dataobject.accel.workflow.WorkItem;
import com.csc.fs.sa.PostProcess;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * WorkPostProcessor contains post-process logic for retrieval of AWD work items
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

public class WorkPostProcessor extends RestPostProcessor implements PostProcess {
    /**
     * Post-process logic for system api.
     * 
     */
    public Result systemApi(List input, Result result, SystemService service, SystemAPI api, ObjectRepository or) {
        ItemID currentItemID = getItemID(input); //NBA311
        boolean processingWorkItem = true;  //APSL5055 - NBA331.1
        String sourceItemId = "";  //APSL5055 - NBA331.1
        List newData = new ArrayList();
        if (!result.hasErrors()) {
            List<Link> selectedLinks = processLinks(input, result.getReturnData()); //APSL5055 - NBA331.1            
            List returnData = aggregate(result.getReturnData());
            Iterator it = returnData.iterator();
            while (it.hasNext()) {
                Object currentObj = it.next();
                if (currentObj instanceof Response) {
                    examineResponse(result, (Response) currentObj);
                    if (result.hasErrors()) {
                        return result;
                    }
                } else {
                    newData.add(currentObj);
                    if (currentObj instanceof WorkItem) {
                        processingWorkItem = true;  //APSL5055 - NBA331.1
                        WorkItem workItem = (WorkItem) currentObj;
                        processWorkItem(api, workItem, selectedLinks);  //APSL5055 - NBA331.1
                        updateItemID(currentItemID, workItem);
                    } else if (currentObj instanceof SourceItem) {
                        processingWorkItem = false;  //APSL5055 - NBA331.1                         
                        SourceItem sourceItem = (SourceItem) currentObj;
                        sourceItem.setParentWorkItemID(currentItemID.getItemID());  //APSL5055 - NBA331.1
                        sourceItemId = sourceItem.getItemID();  //APSL5055 - NBA331.1
                        processSourceItem(api, sourceItem);
                    } else if (currentObj instanceof WorkField) {
                        WorkField workField = (WorkField) currentObj;
                        if (processingWorkItem) {  //APSL5055 - NBA331.1
                            workField.setParentID(currentItemID.getItemID());
                        } else {  //APSL5055 - NBA331.1
                            workField.setParentID(sourceItemId);  //APSL5055 - NBA331.1
                        }  //APSL5055 - NBA331.1
                        workField.setSystemName(api.getSystemName());
                    } else {
                        ((AccelDataObject) currentObj).setSystemName(api.getSystemName());
                    }
                }
            }
  			//APSL5055 - NBA331.1 coded deleted
        }
        return result;
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
