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
import com.csc.fs.dataobject.accel.workflow.Link;
import com.csc.fs.dataobject.accel.workflow.Response;
import com.csc.fs.dataobject.accel.workflow.WorkItem;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * GetItemPostProcessor contains post-process logic for retrieval of AWD work items
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
 
public class GetItemPostProcessor extends RestPostProcessor {
    
    /**
     * System API post-process logic for Item responses
     * 
     */
    public Result systemApi(List input, Result result, SystemService service, SystemAPI api, ObjectRepository or) {
        if (!result.hasErrors()) {
            List currentData = aggregate(result.getReturnData());
            List<Link> selectedLinks = processLinks(input, currentData);	//NBA331.1
            Iterator dataObjects = currentData.iterator();
            while (dataObjects.hasNext()) {
                Object currentObj = dataObjects.next();
                if (currentObj instanceof Response) {
                    examineResponse(result, (Response) currentObj);
                    if (result.hasErrors()) {
                        return result;
                    }
                } else {
                    if (currentObj instanceof WorkItem) {
                        WorkItem workItem = (WorkItem) currentObj;
                        processWorkItem(api, workItem, selectedLinks);  //NBA331.1
                    }
                }
            }
        }
        return result;
    }

    /**
     * Post-process logic for system service.
     *
     */
    public Result systemService(
            List input,
            Result result,
            SystemService service,
            ObjectRepository or) {
				return Result.Factory.create();
            }
						
}
