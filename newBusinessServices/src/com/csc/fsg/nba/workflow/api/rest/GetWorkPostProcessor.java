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
import com.csc.fs.dataobject.accel.workflow.ItemID;
import com.csc.fs.dataobject.accel.workflow.Response;
import com.csc.fs.dataobject.accel.workflow.WorkField;
import com.csc.fs.dataobject.accel.workflow.WorkItem;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * WorkPostProcessor.java contains post-process logic for GetWork
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

public class GetWorkPostProcessor extends com.csc.fsg.nba.workflow.api.rest.RestPostProcessor { //APSL5055-NBA331.1
	/**
     * Post-process logic for GetWork system api.
     * 
     */
    public Result systemApi(List input, Result result, SystemService service, SystemAPI api, ObjectRepository or) {
        if (!result.hasErrors()) {
            List returnData = result.getReturnData();
            if (returnData != null && !returnData.isEmpty()) {
                WorkItem currentWorkItem = getCurrentWorkItem(returnData);
                if (currentWorkItem == null) { //Simulate a NetServer SYS0070 response
                    ResultImpl resultImpl = (ResultImpl) returnData.get(0);
                    Response response = new Response();
                    response.markTransient();
                    resultImpl.addResult(response);
                    response.setCode("SYS0070");
                    response.setMsg("SYS0070 - No work available for employee");
                } else {
                    createItemID(input, currentWorkItem); // Add an ItemID for locking the work item
                    //begin APSL5055-NBA331.1
                    Iterator it = result.getReturnData().iterator();
                    while (it.hasNext()) {
                        Object obj = it.next();
                        if (obj instanceof ResultImpl) {
                            Iterator rit = ((ResultImpl) obj).getReturnData().iterator();
                            while (rit.hasNext()) {
                                if (!(rit.next() instanceof WorkField)) { // Keep only WorkFields. The Lock response does not return WorkFields.
                                    rit.remove();
                                }
                            }
                        }
                    }
                    //end APSL5055-NBA331.1
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
    protected void createItemID(List input, WorkItem currentWorkItem) {
        ItemID currentItemID = getItemID(input);
        if (currentItemID == null) {
            currentItemID = new ItemID();
            input.add(currentItemID);
        }
        currentItemID.setItemID(currentWorkItem.getItemID());
        currentItemID.setRecordType(currentWorkItem.getRecordType());
        currentItemID.setPageNo("1");
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
