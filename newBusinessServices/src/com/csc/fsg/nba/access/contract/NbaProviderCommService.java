package com.csc.fsg.nba.access.contract;

import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;

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

/**
 * This class defines utility functions for nbA enterprise java beans (EJBs).
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA208-26</td><td>Version 7</td><td>Remove synchronized keyword from getLogger() methods</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaProviderCommService implements java.io.Serializable {
	protected static NbaLogger logger = null;
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	protected static NbaLogger getLogger() { // NBA208-26
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaProviderCommService.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaProviderCommService could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * This method recieves an NbaTXLife object. Then it checks if the Agent data needs to be called from 
	 * External System. If the answer is yes, then it calls for agent data, merges it with the NbaTXLife
	 * object and returns the object. 
	 * 
	 * @param com.csc.fsg.nba.vo.NbaTXLife.
	 * @return com.csc.fsg.nba.vo.NbaTXLife.
	 */
	//NBA091 New Method
	public String submitProviderRequest(String Request){
		String Response = new String("<1122Response>this is a response from the Test webservice</1122Response>");
		return Response;
	}
}
