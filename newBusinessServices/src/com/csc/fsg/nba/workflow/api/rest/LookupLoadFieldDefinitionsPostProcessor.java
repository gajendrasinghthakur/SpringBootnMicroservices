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
import com.csc.fs.dataobject.accel.workflow.Field;
import com.csc.fs.dataobject.accel.workflow.LookupRequest;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * LookupLoadFieldDefinitionsPostProcessor contains post-process logic for AWD Field Definitions
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
public class LookupLoadFieldDefinitionsPostProcessor extends com.csc.fsg.nba.workflow.api.rest.RestPostProcessor {
    /**
     * Post-process logic for System API.     
     */
    public Result systemApi(List input, Result result, SystemService service, SystemAPI api, ObjectRepository or) {
        if (!result.hasErrors()) {
            LookupRequest lookupRequest = getLookupRequest(input);
            if (result.getData().size() > 0) {
                ResultImpl resultImpl = (ResultImpl) result.getFirst();
                WorkflowLookupRequestFormatter instance = WorkflowLookupRequestFormatter.getInstance();
                instance.addFieldDefinition(resultImpl.getData(Field.class));
                String pageNo = getNextPageNo(resultImpl.getData());
                if (lookupRequest != null) {
                    lookupRequest.setFieldDefinitionPageNumber(pageNo);
                }
                instance.setFieldValuesInitialized(pageNo.length() == 0);
            }
        }
        result.clear(); //clear for next iteration
        return result;
    }

    /**
     * Locate the LookupRequest in the input. Initialize the LookupRequest.pageNumber
     * @param input
     * @return the LookupRequest
     */
    protected LookupRequest getLookupRequest(List input) {
        LookupRequest lookupRequest = null;
        Iterator it = input.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof LookupRequest) {
                lookupRequest = (LookupRequest) obj;
                lookupRequest.setFieldDefinitionPageNumber("");     //Re-initialize
                break;
            }
        }
        return lookupRequest;
    }

    /**
     * Post-process logic for System Service.
     * 
     */
    public Result systemService(List input, Result result, SystemService service, ObjectRepository or) {
        return Result.Factory.create();
    }
}
