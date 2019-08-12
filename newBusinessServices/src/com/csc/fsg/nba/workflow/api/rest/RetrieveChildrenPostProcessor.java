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
import com.csc.fs.ResultImpl;
import com.csc.fs.dataobject.accel.AccelDataObject;
import com.csc.fs.dataobject.accel.workflow.ItemID;
import com.csc.fs.dataobject.accel.workflow.Link;
import com.csc.fs.dataobject.accel.workflow.SourceItem;
import com.csc.fs.dataobject.accel.workflow.WorkField;
import com.csc.fs.dataobject.accel.workflow.WorkItem;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * RetrieveChildrenPostProcessor contains post-process logic for RetrieveChildren
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

public class RetrieveChildrenPostProcessor extends com.csc.fsg.nba.workflow.api.rest.RestPostProcessor {    //APSL5055 - NBA331.1
    /**
     * Post-process logic for system api.
     * 
     */
    public Result systemApi(List input, Result result, SystemService service, SystemAPI api, ObjectRepository or) {
        if (!result.hasErrors()) {
            List<Link> selectedLinks = processLinks(input, result.getReturnData());	//APSL5055 - NBA331.1
            List returnData = aggregate(result.getReturnData());
            if (returnData != null && !returnData.isEmpty()) {
                ItemID currentItemID = getItemID(input);
                Iterator it = returnData.iterator();
                WorkItem currentWorkItem = null;
                SourceItem currentSourceItem = null;
                String lastIdentifier = "";
                String lastParentId = null;	//APSL5055 - NBA331.1
                while (it.hasNext()) {
                    Object currentObj = it.next();
                    if (currentObj instanceof WorkItem) {
                        currentWorkItem = (WorkItem) currentObj;
                        processWorkItem(api, currentWorkItem, selectedLinks);  //APSL5055 - NBA331.1
                        if (!currentItemID.getItemID().equals(currentWorkItem.getItemID())) {
                            currentWorkItem.setParentID(currentItemID.getItemID());
                        }
                        lastIdentifier = currentWorkItem.getIdentifier();
                        lastParentId = currentWorkItem.getItemID();	//APSL5055 - NBA331.1
                    } else if (currentObj instanceof SourceItem) {
                        currentSourceItem = (SourceItem) currentObj;
                        processSourceItem(api, currentSourceItem);
                        currentSourceItem.setParentWorkItemID(currentItemID.getItemID());
                        lastIdentifier = currentSourceItem.getIdentifier();
                        lastParentId = currentSourceItem.getItemID();	//APSL5055 - NBA331.1
                    } else if (currentObj instanceof AccelDataObject) {
                        AccelDataObject ado = (AccelDataObject) currentObj;
                        ado.setSystemName(api.getSystemName());
                        if (!lastIdentifier.isEmpty()) {
                            ado.setParentIdentifier(lastIdentifier);
                        }
                        if (ado instanceof WorkField) {	//APSL5055 - NBA331.1
                            ((WorkField) ado).setParentID(lastParentId);	//APSL5055 - NBA331.1
                        }	//APSL5055 - NBA331.1
                        ado.markTransient();
                    }
                }
            }
            result.getReturnData().clear();
            ResultImpl resultImpl = new ResultImpl();
            resultImpl.addResults(returnData);
            result.addResult(resultImpl);
        }
        return result;
    }

    /**
     * Post-process logic for system service.
     * 
     */
    public Result systemService(List input, Result result, SystemService service, ObjectRepository or) {
        return Result.Factory.create();
    }
}
