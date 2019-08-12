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
import com.csc.fs.sa.PreProcess;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * UnlockWorkPreProcessor contains pre-process logic for Unlocking work items
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

public class UnlockWorkPreProcessor extends RestPreProcessor  implements PreProcess {
    /**
     * Pre-process logic for system api.
     * 
     */
    public Result systemApi(List input, SystemService service, SystemAPI api, ObjectRepository or) {
        Result result = super.systemApi(input, service, api, or);	//NBA331.1
        ItemID initalItemID = null;
        StringBuffer buff = new StringBuffer();
        Iterator it = input.iterator();
        while (it.hasNext()){
            Object obj = it.next();
            if (obj instanceof ItemID){
                ItemID itemIdDO = (ItemID) obj;
                if (initalItemID == null){
                    initalItemID = itemIdDO;
                    buff.append(itemIdDO.getItemID());
                } else {
                    buff.append("," + itemIdDO.getItemID());
                    it.remove();
                }
            }
        }
        if (initalItemID != null){
            initalItemID.setItemID(buff.toString());
        }
        return result;	//NBA331.1
    }

    /**
     * Pre-process logic for system service.
     * 
     */
    public Result systemService(List input, SystemService service, ObjectRepository or) {
        return null;
    }
}
