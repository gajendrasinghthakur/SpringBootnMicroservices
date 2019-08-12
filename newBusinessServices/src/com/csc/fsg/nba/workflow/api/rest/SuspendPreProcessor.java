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
import com.csc.fs.dataobject.accel.workflow.Suspense;
import com.csc.fs.sa.PreProcess;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * SuspendPreProcessor contains pre-process logic for Suspending a Work Item
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

public class SuspendPreProcessor extends com.csc.fsg.nba.workflow.api.rest.RestPreProcessor implements PreProcess {
    
    
    public Result systemApi(List input, SystemService service, SystemAPI api, ObjectRepository or) {
        Result result = super.systemApi(input, service, api, or);	//NBA331.1
        Suspense suspense = null;
        Iterator it = input.iterator();
        while (it.hasNext()) {
            Object dataObj = it.next();
            if (dataObj instanceof Suspense) {
                suspense = (Suspense) dataObj;
                break;
            }
        }
        if (suspense != null && suspense.getActivateDateTime() != null) {
            if (suspense.getActivateDateTime().length() > 9) {
                suspense.setActivationDate(suspense.getActivateDateTime().substring(0, 10));
                
            }
            if (suspense.getActivateDateTime().length() > 9) {
            	// APSL5055 Begins    
            	suspense.setActivationTime((suspense.getActivateDateTime().substring(11, 19)+suspense.getActivateDateTime().substring(26)).replaceAll("\\.", ":"));
            	// APSL5055 Ends   
            }
        }
        if (suspense.getSuspCode() == null) {
            suspense.setSuspCode("");
        }
        if (suspense.getActivateStatus() == null) {
            suspense.setActivateStatus("");
        }
        return result;
    }
    
    
}
