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

package com.csc.fs.accel.workflow.api.netserver;

import com.csc.fs.ComponentBase;
import com.csc.fs.ObjectRepository;
import com.csc.fs.Result;
import com.csc.fs.SystemSession;
import com.csc.fs.dataobject.accel.workflow.GetWorkRequest;
import com.csc.fs.dataobject.accel.workflow.LOB;
import com.csc.fs.dataobject.accel.workflow.LookupCriteria;
import com.csc.fs.dataobject.accel.workflow.LookupRequest;
import com.csc.fs.dataobject.accel.workflow.User;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.om.ObjectFactory;
import com.csc.fs.sa.PreProcess;
import com.csc.fs.sa.SAUtils;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaLob;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Pre-process logic for  Lookup functions.
 * Find the Lookup data object in the input list then
 * build the appropriate criteria objects need to format
 * the Netserver call.
 * Note that view calls use a different preprocessor.   
  * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SPR3673</td><td>Incorporate the New Optional honorOperand Tag for Lookup Introduced by AWD/NetServer 3.1 PTF 08352101</td></tr>
 * </table>
 * <p>
 */

public class LookupPreProcessor extends ComponentBase implements PreProcess {
    /**
     * Pre-process logic for system api.
     * 
     */
	protected final static String EQUAL_OPERATOR = "=";
	protected final static String LIKE_OPERATOR = "LIKE";
	private static String wildcardChars = null;
	static{
		try {
			wildcardChars = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.WILDCARD_CHARACTERS);
		} catch (NbaBaseException e) {
			LogHandler.Factory.LogError("LookupPreProcessor", NbaBaseException.CONFIGURATION_MISSING);
			wildcardChars = "%";
		}
	}


    public Result systemApi(
            List input,
            SystemService service,
            SystemAPI api,
            ObjectRepository or) {
                           	        	
        LookupRequest lookupReq = null;
        GetWorkRequest getWorkReq = null;
        Iterator i = input.iterator();
        SystemSession session = SAUtils.retrieveSystemSession(getServiceContext(), api.getSystemName());
        while (i.hasNext()) {
        	Object dataObj = i.next();
        	if (dataObj instanceof LookupRequest) {
        		lookupReq = (LookupRequest) dataObj;
        		if (lookupReq.getLockStat()!=null && lookupReq.getLockStat().equals("CURRENT_USER")){
        		    lookupReq.setLockStat(session.getUserId());
        		}        		
        	}
        	if (dataObj instanceof User) {
        	    User user = (User) dataObj;
        	    if (user.getUserID()==null){
        	        user.setUserID(session.getUserId());
        	    }
        	}
        	if (dataObj instanceof GetWorkRequest) {
        	    getWorkReq = (GetWorkRequest) dataObj; 		
        	}
       }
        if (lookupReq == null && getWorkReq==null) {
			return Result.Factory.create(); 	
        }
        // For netserver, AWDLookup up is called to to get everything in the personal que.  Note that
        // for view calls ACBL0003 is called used a specific action code.
        if (getWorkReq!=null){
            lookupReq=new LookupRequest();
            lookupReq.setQueue(session.getUserId());
            input.add(lookupReq);
            User user=new User();
            user.setUserID(session.getUserId());
            input.add(user);
        }
        
        input.addAll(getLookupCriteria(lookupReq));        

		// check for index fields
        List lobs = lookupReq.getLobData();
        if(lobs != null && lobs.size() > 0){
	        int nmbrLOBs = lobs.size();
			int count = 0;
			Iterator lobIterator = lobs.iterator();
			while (lobIterator.hasNext()) {
				Object obj = lobIterator.next();
				if (obj instanceof LOB) {
					LOB currentLOB = (LOB) obj;
		    		LookupCriteria lookupCriteria = (LookupCriteria) ObjectFactory.create(LookupCriteria.class);
		    		lookupCriteria.setName(convertName(currentLOB.getName()));
		    		lookupCriteria.setValue(currentLOB.getValue());
		    		lookupCriteria.setOperand(handleWildcards(currentLOB.getValue()));
		    		input.add(lookupCriteria);
				}
			}
        }

		return Result.Factory.create();
         
	}     
    private String convertName(String lobDOName){
        if (lobDOName==null){
            return lobDOName;
        }
        //begin NBA213
        if (lobDOName.equals("INXFLD01")){
            // govt id
            return "index_1";
        }
        //end NBA213
        if (lobDOName.equals("INXFLD02")){
            // pol number
            return "index_2";
        }
        else
        if (lobDOName.equals("INXFLD03")){
            // claim
            return "index_3";
        }
        return lobDOName;
    }
	private List getLookupCriteria(LookupRequest lookupReq) {     
                        	            	
	    List criteria = new ArrayList();
	    
	    if (lookupReq.getBusinessArea() != null) {
    		LookupCriteria lookupCriteria = (LookupCriteria) ObjectFactory.create(LookupCriteria.class);
    		lookupCriteria.setName("businessArea");
    		lookupCriteria.setValue(lookupReq.getBusinessArea());
    		lookupCriteria.setOperand("=");
    		criteria.add(lookupCriteria);
        }
        
		if (lookupReq.getWorkType() != null) {
    		LookupCriteria lookupCriteria = (LookupCriteria) ObjectFactory.create(LookupCriteria.class);
    		lookupCriteria.setName("workType");
    		lookupCriteria.setValue(lookupReq.getWorkType());
    		lookupCriteria.setOperand("=");
    		criteria.add(lookupCriteria);
		}

		if (lookupReq.getSourceType() != null) {
    		LookupCriteria lookupCriteria = (LookupCriteria) ObjectFactory.create(LookupCriteria.class);
    		lookupCriteria.setName("sourceType");
    		lookupCriteria.setValue(lookupReq.getSourceType());
    		lookupCriteria.setOperand("=");
    		criteria.add(lookupCriteria);
		}
		
		if (lookupReq.getWorkStatus() != null) {
    		LookupCriteria lookupCriteria = (LookupCriteria) ObjectFactory.create(LookupCriteria.class);
    		lookupCriteria.setName("status");
    		lookupCriteria.setValue(lookupReq.getWorkStatus());
    		lookupCriteria.setOperand("=");
    		criteria.add(lookupCriteria);
		}
		
		if (lookupReq.getQueue() != null) {
    		LookupCriteria lookupCriteria = (LookupCriteria) ObjectFactory.create(LookupCriteria.class);
    		lookupCriteria.setName("queue");
    		lookupCriteria.setValue(lookupReq.getQueue());
    		if(!NbaUtils.isBlankOrNull(lookupReq.getQueueOperand())){
    			lookupCriteria.setOperand(lookupReq.getQueueOperand());
    		}else{
    			lookupCriteria.setOperand(EQUAL_OPERATOR);
    		}
    		criteria.add(lookupCriteria);
		}
         
		if (lookupReq.getLockStat() != null) {
    		LookupCriteria lookupCriteria = (LookupCriteria) ObjectFactory.create(LookupCriteria.class);
    		lookupCriteria.setName("lockedBy");
    		lookupCriteria.setValue(lookupReq.getLockStat().toUpperCase());
    		lookupCriteria.setOperand("=");
    		criteria.add(lookupCriteria);
		}
         
		if (lookupReq.getBeginDateTime() != null) {
    		LookupCriteria lookupCriteria = (LookupCriteria) ObjectFactory.create(LookupCriteria.class);
    		// no field def for "beginTime" - in AWD use createTime
    		lookupCriteria.setName("createTime");
    		lookupCriteria.setValue(lookupReq.getBeginDateTime());
    		lookupCriteria.setOperand(">");//ALS95
    		criteria.add(lookupCriteria);
		}
        //TODO - UI let me enter both.  Looks wrong to have both begin & end use '>' although that's how it
		//       looks in the view call preprocessor
		if (lookupReq.getEndDateTime() != null) {
    		LookupCriteria lookupCriteria = (LookupCriteria) ObjectFactory.create(LookupCriteria.class);
    		lookupCriteria.setName("createTime");
    		lookupCriteria.setValue(lookupReq.getEndDateTime());
    		lookupCriteria.setOperand("<");//ALS95
    		criteria.add(lookupCriteria);
		}
        return criteria;
    }
	public String handleWildcards(String value) {
		String wildcardChars = null;
		int i =0;
		String operand = EQUAL_OPERATOR;
		wildcardChars = getWildcardChars(); 
		for(i=0;i<wildcardChars.length();i++){		
			if (value.indexOf(wildcardChars.charAt(i)) != -1) {
				operand = LIKE_OPERATOR;
				break;
			} 
		}
		return operand;
	}
	public String getWildcardChars() {
		return wildcardChars;
	}

    /**
     * Pre-process logic for system service.
     *
     */
    public Result systemService(
            List input,
            SystemService service,
            ObjectRepository or) {
				return Result.Factory.create();
            }

}