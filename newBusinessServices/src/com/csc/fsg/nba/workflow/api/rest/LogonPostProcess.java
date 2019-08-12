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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.axa.fs.accel.session.SSOUserCredential;
import com.csc.fs.ComponentBase;
import com.csc.fs.Encryptor;
import com.csc.fs.ObjectRepository;
import com.csc.fs.Result;
import com.csc.fs.SystemSession;
import com.csc.fs.UserSessionController;
import com.csc.fs.dataobject.accel.workflow.HttpHeader;
import com.csc.fs.dataobject.accel.workflow.User;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.sa.PostProcess;
import com.csc.fs.sa.SAUtils;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * LogonPostProcess contains Log On post-process logic
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
  * <tr><td>APSL5055-NBA331.1</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @since New Business Accelerator - Version NB-1401
 */

/**
 *  Logon Post process.
 */

public class LogonPostProcess extends ComponentBase implements PostProcess {
    protected static final String WORKFLOW_SECURITY_LEVEL_KEY = "workflowSecurityLevel";
    protected static final String _KEYR13PASSWORD = "r13password";
    protected static final String COOKIE_KEY = "COOKIE";
    protected static final String CSRF_KEY = "CSRF";
    public Result systemApi(
        List input,
        Result result,
        SystemService service,
        SystemAPI api,
        ObjectRepository or) {

        if (!result.hasErrors()) {
            UserSessionController ctlr = getServiceContext().getUserSession();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put(_KEYR13PASSWORD,getEncyptedR13Password(api));
            User user = getUser(result,api);
            if (user != null) {            	 
            	updateIfNonBlank(dataMap, WORKFLOW_SECURITY_LEVEL_KEY, user.getSecurityLevel());
            	updateIfNonBlank(dataMap, SSOUserCredential.USERNAME, user.getUserID());
            	updateIfNonBlank(dataMap, "CONNECTION_USER_ID", user.getUserID());
				updateIfNonBlank(dataMap, "CONNECTION_PASSWORD", " ");
				//APSL5055 start for Content viewer
				try {
					user.setAftUrl(NbaConfiguration.getInstance().getFileLocation(NbaConfigurationConstants.AWD_AFTURL));
					user.setHttpUrl(NbaConfiguration.getInstance().getFileLocation(NbaConfigurationConstants.AWD_HTTPURL));	
					updateIfNonBlank(dataMap, NbaUserVO.AFT_URL, NbaConfiguration.getInstance().getFileLocation(NbaConfigurationConstants.AWD_AFTURL));
					updateIfNonBlank(dataMap, NbaUserVO.HTTP_URL, NbaConfiguration.getInstance().getFileLocation(NbaConfigurationConstants.AWD_HTTPURL));
				} catch (NbaBaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//APSL5055 end for Content viewer
            }
			//APSL5055 start for Content viewer
			//APSL5055 end for Content viewer
			HttpHeader cookie = getCookie(result,api);
            if (cookie != null) {
            	if (cookie.getCookie() != null && cookie.getCookie().length() > 0) {
            		dataMap.put(COOKIE_KEY, cookie.getCookie());
            		//APSL5055 start for Content viewer
    				updateIfNonBlank(dataMap, SSOUserCredential.JSESSIONID, cookie.getCookie().substring(11));
    				//APSL5055 end for Content viewer
            	} else {
            	    LogHandler.Factory.LogError(this, "No COOKIE value in HTTP Response Header");
            	}
            	if (cookie.getCsrf() != null && cookie.getCsrf().length() > 0) {
            		dataMap.put(CSRF_KEY, cookie.getCsrf());
            	}
            } else {
                LogHandler.Factory.LogError(this, "HTTP Response Header values not found");
            }
            ctlr.updateHostData(api.getSystemName(), dataMap);
            ctlr.updateUserProfileData(api.getSystemName(), dataMap);
        }
        return result;
    }

    /**
     * Examine the result and return the value of the cookie
     * @param result
     * @param api
     * @return
     */
    protected HttpHeader getCookie(Result result, SystemAPI api) {
        if (!result.hasErrors()) {
            List data = result.getReturnData();
            if (data != null && !data.isEmpty()) {
                for (int i=0;i<data.size();i++){
                    Result result1 = (Result) data.get(i);
                    if (!result1.hasErrors()) {
                        List dataList = result1.getReturnData();
                        if (dataList != null && !dataList.isEmpty()) {
                            Iterator dataObjects = dataList.iterator();             
                            while (dataObjects.hasNext()) {
                                Object currentObj = dataObjects.next();
                                if (currentObj instanceof HttpHeader) {
                                    return (HttpHeader) currentObj;
                                }   
                            }   
                        }       
                    }
                }
                
            }
        }
        return null;
    }

    public Object getEncyptedR13Password(SystemAPI api) {
        SystemSession session = SAUtils.retrieveSystemSession(getServiceContext(), api.getSystemName());        
        String password = session.getPassword();
        byte[] encPassword = new byte[0];
        if (password != null) {
            try {
                password = doR13Pwd(password);
                Encryptor enc = (Encryptor) ServiceLocator.lookup(Encryptor.SERVICENAME);
                encPassword = enc.encrypt(password);
            } catch (Exception e) {
                LogHandler.Factory.LogError(this, "Error encrypting AWD password.");
            }
        }
        return encPassword;
    }
    
    protected String doR13Pwd(String awdpassword) {
        StringBuffer r13Pwd=new StringBuffer();
        for (int i = 0; i < awdpassword.length(); i++) {
            char c = awdpassword.charAt(i);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'A' && c <= 'Z') c -= 13;
            r13Pwd.append(c);
        }
        return r13Pwd.toString();
    }

    public Result systemService(
        List input,
        Result result,
        SystemService service,
        ObjectRepository or) {
        return null;
    }
    public User getUser(Result result, SystemAPI api) {

        if (!result.hasErrors()) {
            List data = result.getReturnData();
            if (data != null && !data.isEmpty()) {
                // need to change this because we could have multiple result objects
                for (int i=0;i<data.size();i++){
                    Result result1 = (Result) data.get(i);
                    if (!result1.hasErrors()) {
                        List dataList = result1.getReturnData();
                        if (dataList != null && !dataList.isEmpty()) {
                            Iterator dataObjects = dataList.iterator();             
                            while (dataObjects.hasNext()) {
                                Object currentObj = dataObjects.next();
                                if (currentObj instanceof User) {
                                    User usr = (User) currentObj;
                                    return usr;
                                }   
                            }   
                        }       
                    }
                }
            }
        }
        return null;
    }

    protected void updateIfNonBlank(Map data, String key, String value) {
	    if (value != null && value.length() > 0)
	        data.put(key, value);
	}
}
