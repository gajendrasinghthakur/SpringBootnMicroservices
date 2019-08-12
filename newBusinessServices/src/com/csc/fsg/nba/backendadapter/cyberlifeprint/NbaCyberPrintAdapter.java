package com.csc.fsg.nba.backendadapter.cyberlifeprint;
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
 * 
 * *******************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.SystemAccess;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaTXLife;
/**
 * This class acts as the entry point for the CyberLife backend print adapter.  
 * It processes the XML holding to the host and then returns the host response in XML format.
 * <br>
 * table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * <tr><td>NBA195</td><td>Version 7</td><td>JCA Adapter for DXE Interface to CyberLife</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
public class NbaCyberPrintAdapter {
	private NbaLogger logger = null;
	/**
	 * NbaCyberPrintAdapter constructor.
	 */
	public NbaCyberPrintAdapter() {
		super();
	}
	// NBA195 code deleted
	/**
	 * Main method that handles the method calls to send DXE to the host
	 * @param XML document Contains the transaction request for the host 
	 * @return XML reponse from the host
	 * @exception throws NbaBaseException and java.rmi.RemoteException
	 */
	public NbaTXLife print(NbaTXLife request) throws NbaBaseException {
		//Begin NBA195
		NbaTXLife response = null;
		// Get System Access
		SystemAccess sysAccess = (SystemAccess) ServiceLocator.lookup(SystemAccess.SERVICENAME);
		// Prepare Input as to the service
		List list = new ArrayList(1);
		list.add(request);
		// invoke CyberAdapter Service for CyberLifeDXE External System
		Result result = sysAccess.invoke("CyberLifeDXE/CyberPrintAdapter", list);

		if (!result.hasErrors()) {
			// if there are no errors, get the response out of result object
			response = (NbaTXLife) result.getData().get(0);
		} else {
			Message msgs[] = result.getMessages();
			if (msgs != null && msgs.length > 0) {
				List data = msgs[0].getData();
				if (data != null && data.size() > 0) {
					throw new NbaBaseException(data.get(0).toString());
				}
			}
		}
		return response;
		//End NBA195
	}
	
	// NBA195 code deleted
	
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	protected NbaLogger getLogger() {
        if (logger == null) {
            logger = NbaLogFactory.getLogger(this.getClass());
        }
        return logger;
    }	
}
