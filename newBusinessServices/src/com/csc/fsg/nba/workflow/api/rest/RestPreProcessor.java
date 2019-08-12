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
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.csc.fs.ObjectRepository;
import com.csc.fs.Result;
import com.csc.fs.SystemSession;
import com.csc.fs.accel.workflow.api.nativeXML.NativeXMLPreProcessor;
import com.csc.fs.dataobject.accel.workflow.ItemID;
import com.csc.fs.dataobject.accel.workflow.ParentChildFind;
import com.csc.fs.dataobject.accel.workflow.User;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.sa.PreProcess;
import com.csc.fs.sa.SAUtils;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;
import com.csc.fs.session.SystemSessionBase;

/**
 * RestPreProcessor
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
 
public class RestPreProcessor extends NativeXMLPreProcessor implements PreProcess {
    protected static final String COOKIE = "COOKIE";	//APSL5055 - NBA331.1
    protected static final String CSRF_KEY = "CSRF";    //APSL5055 - NBA331.1

    public Result systemApi(List input, SystemService service, SystemAPI api, ObjectRepository or) {
        User user = null;
        ItemID itemID = null;
        ParentChildFind parentChildFind = null;
        Iterator it = input.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof User) {
                user = (User) obj;
            } else if (obj instanceof ItemID) {
                itemID = (ItemID) obj;
            } else if (obj instanceof ParentChildFind) {
                parentChildFind = (ParentChildFind) obj;
            }
        }
        String systemName = StringUtils.substringBefore(api.getSystemName(), "_");	//APSL5055 - NBA331.1        
        if (user == null) {
            user = getUserObject(systemName);	//APSL5055 - NBA331.1
            input.add(user);
        } else {
        	//begin APSL5055 - NBA331.1
            User sessionUser = getUserObject(systemName);
            user.setUserID(sessionUser.getUserID());
            user.setPassword(sessionUser.getPassword()); 
            user.setCookie(sessionUser.getCookie());
            user.setCsrf(sessionUser.getCsrf());
        	//end APSL5055 - NBA331.1
        }
        LogHandler.Factory.LogResourceAdapter(this, "SYSTEM NAME=" + systemName + ", JSESSIONID value=" + user.getCookie() + ", csrf_token value=" + user.getCsrf()); //APSL5055 - NBA331.1
        if (parentChildFind != null) {
            if (itemID == null) {
                itemID = new ItemID();
                input.add(itemID);
            }
            itemID.setItemID(parentChildFind.getItemID());
            itemID.setPageNo(parentChildFind.getPageNo());
        }
        if (itemID != null && itemID.getUserID() == null) {
            itemID.setUserID(user.getUserID());
        }
        return Result.Factory.create();
    }
    /**
     * Create a User object using the SystemSession information for the systemName
     * @see com.csc.fs.accel.workflow.api.nativeXML.NativeXMLPreProcessor#getUserObject(java.lang.String)
     */
	//APSL5055 - NBA331.1 New Method     
    protected User getUserObject(String systemName) {
        SystemSession session = SAUtils.retrieveSystemSession(getServiceContext(), systemName);
        User user = new User();
        if(session != null ){ // APSL5055-NBA331 Temporary changes for now 
	        user.setUserID(session.getUserId());
	        user.setPassword(session.getPassword());
	        Map data = ((SystemSessionBase) session).getData();
	        if (data.containsKey(COOKIE)) {
	            user.setCookie((String) data.get(COOKIE));
	        }
	        if (data.containsKey(CSRF_KEY)) {
	        	user.setCsrf((String) data.get(CSRF_KEY));
	        }
        }
        return user;
    }
}
