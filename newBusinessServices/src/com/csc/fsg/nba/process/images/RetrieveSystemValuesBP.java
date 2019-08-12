package com.csc.fsg.nba.process.images;

/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group®.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.util.HashMap;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.session.SystemSessionBase;
import com.csc.fsg.nba.process.NewBusinessAccelBP;


/**
 * Business Process to retrieve System values.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA212</td><td>Version 7</td><td>Content Services</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class RetrieveSystemValuesBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	protected static final String PASSWORD = "PASSWORD";
    protected static final String USERID = "USERID";
    
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        Result systemResult = getServiceContext().getUserSession().getSystem((String) input);
        //Copy values to the Map
        SystemSessionBase systemSessionBase = (SystemSessionBase) systemResult.getFirst();
        Map systemMap = new HashMap();
        systemMap.putAll(systemSessionBase.getData());
        systemMap.putAll(systemSessionBase.getUpd());
        systemMap.put(USERID, systemSessionBase.getUserId());
        systemMap.put(PASSWORD, systemSessionBase.getPassword());
        result.addResult(systemMap);
        return result;
    }
}
