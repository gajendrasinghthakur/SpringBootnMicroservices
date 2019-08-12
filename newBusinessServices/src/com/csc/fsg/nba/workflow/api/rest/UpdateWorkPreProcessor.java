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
import com.csc.fs.dataobject.accel.workflow.LOB;
import com.csc.fs.dataobject.accel.workflow.User;
import com.csc.fs.dataobject.accel.workflow.WorkField;
import com.csc.fs.dataobject.accel.workflow.WorkItem;
import com.csc.fs.sa.PreProcess;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * Preprocessor for Update Work
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
public class UpdateWorkPreProcessor extends RestPreProcessor implements PreProcess {
    
    public Result systemApi(List input, SystemService service, SystemAPI api, ObjectRepository or) {
        // get user session information
        Result result = super.systemApi(input, service, api, or);	//APSL5055-NBA331.1
        User userObj = super.getUserObject(api.getSystemName());
        
        String userID = "";
        if (userObj != null) {
            userID = userObj.getUserID();
        }
        Iterator it = input.iterator();
        while (it.hasNext()) {
            Object dataObj = it.next();
            if (dataObj instanceof WorkItem) {
                WorkItem workItem = (WorkItem) dataObj;
                if (workItem.getUserID() == null) {
                    workItem.setUserID(userID);
                }
            //begin APSL5055-NBA331.1
            } else if (dataObj instanceof WorkField) {
                WorkField fieldValue = (WorkField) dataObj;
                if (fieldValue.getFieldValue() == null || fieldValue.getFieldValue().length() < 1) {
                    fieldValue.setFieldValue(" ");   //trick the Message Manager into sending <value/> instead of omitting the tag 
                }
            //end APSL5055-NBA331.1
            } else if (dataObj instanceof LOB) {
                LOB fieldValue = (LOB) dataObj;
                if (fieldValue.getValue() == null || fieldValue.getValue().length() < 1) {
                    fieldValue.setValue(" ");   //trick the Message Manager into sending <value/> instead of omitting the tag 
                }
                
            }
        }
        
        return result;	//NBA331.1
    }
    
}
