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

/**
 * Pre-process logic for  REST Lookup functions.
  * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Description</th>
 * </thead>
 *  <tr><td>NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 */
import java.util.Iterator;
import java.util.List;

import com.csc.fs.ObjectRepository;
import com.csc.fs.Result;
import com.csc.fs.SystemSession;
import com.csc.fs.UserSessionController;
import com.csc.fs.dataobject.accel.AccelDataObject;
import com.csc.fs.dataobject.accel.workflow.GetWorkRequest;
import com.csc.fs.dataobject.accel.workflow.LookupRequest;
import com.csc.fs.sa.PreProcess;
import com.csc.fs.sa.SAUtils;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;
import com.csc.fsg.nba.foundation.NbaConstants;

public class LookupPreProcessor extends com.csc.fsg.nba.workflow.api.rest.RestPreProcessor implements PreProcess {
    public Result systemApi(List input, SystemService service, SystemAPI api, ObjectRepository or) {
        Result result = super.systemApi(input, service, api, or);  //NBA331.1
        LookupRequest lookupReq = getLookupRequest(input, api);
        if (lookupReq != null) {
            generateArgumentValues(lookupReq);  //NBA331.1

        }
        return result;
    }

    //NBA331.1 code deleted    

    /**
     * @param input
     * @param api
     * @return
     */
    protected LookupRequest getLookupRequest(List input, SystemAPI api) {
        LookupRequest lookupReq = null;
        GetWorkRequest getWorkReq = null;
        //NBA331.1 code deleted
        Iterator it = input.iterator();
        SystemSession session = SAUtils.retrieveSystemSession(getServiceContext(), api.getSystemName());
        while (it.hasNext()) {
            Object dataObj = it.next();
            if (dataObj instanceof LookupRequest) {
                lookupReq = (LookupRequest) dataObj;
			//NBA331.1 code deleted
            } else if (dataObj instanceof GetWorkRequest) {
                getWorkReq = (GetWorkRequest) dataObj;
            }
        }
        if (lookupReq == null && getWorkReq == null) {
            return null;
        }
        if (getWorkReq != null) { // Personal Queue
            lookupReq = new LookupRequest();
            lookupReq.setQueue(session.getUserId());
            input.add(lookupReq);
        }
		//NBA331.1 code deleted
        return lookupReq;
    }

    /**
     * System Service Pre-processor
     * @see com.csc.fs.sa.PreProcess#systemService(java.util.List, com.csc.fs.sa.SystemService, com.csc.fs.ObjectRepository)
     */
    public Result systemService(List input, SystemService service, ObjectRepository or) {
        addTargetSystems(input);
        return Result.Factory.create();
    }

    /**
     * @param input
     */
    protected void setFieldDefinitionPageNo(List input) {
        LookupRequest lookupRequest = null;
        Iterator it = input.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof LookupRequest) {
                lookupRequest = (LookupRequest) obj;
                break;
            }
        }
        if (lookupRequest == null) {
            lookupRequest = new LookupRequest();
            input.add(lookupRequest);
        }
        lookupRequest.setFieldDefinitionPageNumber("1");
    }

    /**
     * Add an AccelDataObject for each target system.
     * If a target system is "AWDREST", and the AWD Field Definitions needed for formatting the SQL for the 
     * Stored Procedure have not been retrieved, set the LookupRequest.FieldDefinitionPageNumber value 
     * to cause an API to load the the AWD Field Definitions.
     * @param inputData
     */
    protected void addTargetSystems(List inputData) {
        AccelDataObject targetSystem = null;
        LookupRequest lookupRequest = null;
        Iterator tit = inputData.iterator();
        while (tit.hasNext()) {
            Object obj = tit.next();
            if (obj.getClass() == AccelDataObject.class) {
                targetSystem = (AccelDataObject) obj;
            } else if (obj.getClass() == LookupRequest.class) {
                lookupRequest = (LookupRequest) obj;
            }
        }
        if (targetSystem == null) {
            UserSessionController userSession = getServiceContext().getUserSession();
            Result systems = userSession.getActiveSystems();
            List data = systems.getData();
            if (data != null && !data.isEmpty()) {
                Iterator iter = data.iterator();
                while (iter.hasNext()) {
                    SystemSession session = (SystemSession) iter.next();
                    if (session != null) {
                        targetSystem = new AccelDataObject();
                        targetSystem.setSystemName(session.getSystemName());
                        lookupRequest.setSystemName(session.getSystemName());
                        if (NbaConstants.AWDREST.equals(targetSystem.getSystemName())) {
                            if (!WorkflowLookupRequestFormatter.getInstance().areFieldValuesInitialized()) {
                                targetSystem.setUpdatedBy(session.getUserId());
                                setFieldDefinitionPageNo(inputData);
                            }
                        }
                        inputData.add(targetSystem);
                    }
                }
            }
        }
    }
    /**
     * @param lookupReq
     * @return
     */
    //NBA331.1 New Method
    protected void generateArgumentValues(LookupRequest lookupReq) {
        WorkflowLookupRequestFormatter formatter = WorkflowLookupRequestFormatter.getInstance();
        formatter.generateArgumentValues(lookupReq);
    }    
}
