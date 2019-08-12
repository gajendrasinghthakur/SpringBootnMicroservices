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
import com.csc.fs.dataobject.accel.workflow.ItemID;
import com.csc.fs.dataobject.accel.workflow.WorkField;
import com.csc.fs.dataobject.accel.workflow.WorkItem;
import com.csc.fs.sa.PostProcess;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * RetrieveDataValuesPostProcessor contains post-process logic for Data Values
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @since New Business Accelerator - Version NB-1401
 */

 public class RetrieveDataValuesPostProcessor extends com.csc.fsg.nba.workflow.api.rest.RestPostProcessor implements PostProcess {
    /**
     * Post-process logic for system api.
     * 
     */
    public Result systemApi(List input, Result result, SystemService service, SystemAPI api, ObjectRepository or) {
        ItemID currentItemID = getItemID(input);
        if (!result.hasErrors()) {
            List returnData = result.getReturnData();
            if (returnData != null && !returnData.isEmpty()) {
                String itemId = currentItemID.getItemID();
                String identifier = currentItemID.getIdentifier();
                WorkItem currentWorkItem = getCurrentWorkItem(returnData);
                if (currentWorkItem != null) {
                    itemId = currentItemID.getItemID();
                    identifier = currentWorkItem.getIdentifier();
                }
                Iterator rit = returnData.iterator(); //Assume multiple Results
                while (rit.hasNext()) {
                    Result currentResult = (Result) rit.next();
                    if (!currentResult.hasErrors()) {
                        List currentData = currentResult.getReturnData();
                        Iterator dataObjects = currentData.iterator();
                        while (dataObjects.hasNext()) {
                            Object currentObj = dataObjects.next();
                            if (currentObj instanceof WorkField) {
                                WorkField workField = (WorkField) currentObj;
                                processWorkField(api, workField, itemId, identifier);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
