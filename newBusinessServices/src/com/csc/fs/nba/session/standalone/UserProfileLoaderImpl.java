/*************************************************************************
*
* Copyright Notice (2006)
* (c) CSC Financial Services Limited 1996-2006.
* All rights reserved. The software and associated documentation
* supplied hereunder are the confidential and proprietary information
* of CSC Financial Services Limited, Austin, Texas, USA and
* are supplied subject to licence terms. In no event may the Licensee
* reverse engineer, decompile, or otherwise attempt to discover the
* underlying source code or confidential information herein.
*
*************************************************************************/

package com.csc.fs.nba.session.standalone;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.csc.fs.ErrorHandler;
import com.csc.fs.SystemSession;
import com.csc.fs.UserCredential;
import com.csc.fs.session.SystemSessionBase;
import com.csc.fs.session.UserProfileLoader;
import com.csc.fs.session.UserSessionGroup;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.vo.NbaConfiguration;

/**
 * Implementation of a Standard Offline User Profile Loader
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SPR3341</td><td>Version 7</td><td>ORA-01400 error while automated or manual user logon when using nbA's custom user profile loader</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class UserProfileLoaderImpl implements UserProfileLoader {
	
	private static String CREDCONFIG = "credentials.properties";
	private static String DEFAULTUSR = "NONE";
	private static String DEFAULTPWD = "NONE";
	private static String HOSTUID = null;
	private static String HOSTPWD = null;
	private static String CARRIER_ID = null;
	
	
	private void init() {
		if (HOSTUID == null) {
			synchronized (UserProfileLoaderImpl.class) {
				try {
					HOSTUID = DEFAULTUSR;
					HOSTPWD = DEFAULTPWD;
					InputStream is =
						getClass().getClassLoader().getResourceAsStream(
							CREDCONFIG);
					Properties props = new Properties();
					props.load(is);
					String uid = (String) props.get("HOSTUID");
					String pwd = (String) props.get("HOSTPWD");
					String carrierId = (String) props.get("CARRIERID");
					if (uid != null) {
						HOSTUID = uid;
						HOSTPWD = pwd;
						CARRIER_ID = carrierId;
					}
				} catch (Exception ex) {
					ErrorHandler.process(
						getClass(),
						"Error while Loading " + CREDCONFIG,
						new RuntimeException(ex.getMessage()));
				}
			}
		}
	}

	/**
	 * @see UserProfileLoader#getUserGroups(UserCredential)
	 */
	public List getUserGroups(UserCredential credential) {
		List groups = new ArrayList();
		UserSessionGroup ug = new UserSessionGroup();
		//NBA123 begin
		String userId = credential.getUserId();
		if (userId.startsWith("APMGR")){
		    ug.setName("APManager");
		}else if (userId.startsWith("APOPER")){
		    ug.setName("APOperator");
		} else { //SPR3341
		    ug.setName(userId); //SPR3341
		}
		//NBA123 end
		groups.add(ug);
		return groups;
	}

	/**
	 * @see UserProfileLoader#getUserProfile(UserCredential, String)
	 */
	public SystemSession getUserProfile(
		UserCredential credential,
		String systemName) {
		init();
		if (HOSTUID.equals(DEFAULTUSR)) {
			ErrorHandler.process(
				getClass(),
				new RuntimeException(
					"Invalid Credentials.Please check " + CREDCONFIG));
			return null;
		}
		SystemSession session = new SystemSessionBase(systemName);
		session.setSystemName(systemName);
		session.activate();
		session.setLogonMechanism(SystemSession.NEVER);
		Map currentSessionData = new HashMap();
		currentSessionData.put("CONNECTION_USER_ID", credential.getUserId());	//NBA146
		currentSessionData.put("CONNECTION_PASSWORD", credential.getPassword()); //NBA146	
		session.setUserId(credential.getUserId());//NBA146
		session.setPassword(credential.getPassword());//NBA146
		session.setUserProfileData((HashMap) currentSessionData);
		return session;
	}
	public SystemSession getCustDBProfile(
		UserCredential credential,
		String systemName) {
		SystemSession session = new SystemSessionBase(systemName);
		session.setSystemName(systemName);
		session.activate();
		session.setLogonMechanism(SystemSession.NEVER);
		Map currentSessionData = new HashMap();
		currentSessionData.put("CLIENT-DB", "");
		session.setUserProfileData((HashMap) currentSessionData);
		return session;
	}

	/**
	 * @see UserProfileLoader#getUserProfiles(UserCredential)
	 */
	public List getUserProfiles(UserCredential credential) {
		List profiles = new ArrayList();
		//Begin	NBA146
		try{
		    profiles.add(getAWDProfile(credential, NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
		            NbaConfigurationConstants.DEFAULT_SYSTEM))); 
		}catch(NbaBaseException exp){
		    profiles.add(getAWDProfile(credential, NbaConstants.AWD_EXTERNAL_SYSTEM)); 
		}  
		//end NBA146
		return profiles;
	}
	
	private SystemSession getAWDProfile(
		UserCredential credential,
		String systemName) {
			SystemSession session = new SystemSessionBase(systemName);
			session.activate();
			session.setLogonMechanism(SystemSession.MANDATORY);
			Map currentSessionData = new HashMap();
			currentSessionData.put("CONNECTION_USER_ID", credential.getUserId());
			currentSessionData.put("CONNECTION_PASSWORD", credential.getPassword());
			session.setUserId(credential.getUserId());
			session.setPassword(credential.getPassword());
			session.setUserProfileData((HashMap) currentSessionData);
			return session;
	}

	/* (non-Javadoc)
	 * @see com.csc.fs.session.UserProfileLoader#changePassword(com.csc.fs.UserCredential, java.lang.String, java.lang.String)
	 */
	public void changePassword(UserCredential arg0, String arg1, String arg2) {
	}

	/* (non-Javadoc)
	 * @see com.csc.fs.session.UserProfileLoader#changePassword(com.csc.fs.UserCredential, java.lang.String)
	 */
	public void changePassword(UserCredential arg0, String arg1) {
	}

}
