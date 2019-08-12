package com.csc.fsg.nba.backendadapter.cyberlife;

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
import com.csc.fsg.nba.backendadapter.NbaBackEndAdapter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;

/**
 * @(#)NbaCyberAdapter.java
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 * <p>
 * <b>Description:</b>&nbsp; This class acts as the entry point for the CyberLife
 *  backend adapter.  SubmitRequestToHost is the primary method that processes the   
 *   XML transaction to the host and then returns the host response in XML format.     
 * <br>
 * <br>
 * <b>Collaborators:</b>&nbsp; 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecure changes</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA195</td><td>Version 7</td><td>JCA Adapter for DXE Interface to CyberLife</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 */

public class NbaCyberAdapter implements NbaBackEndAdapter,  NbaCyberConstants {
	
	
/**
 * NbaCyberAdapter constructor.
 */
public NbaCyberAdapter() {
    super();
}

// NBA195 code deleted

/**
 * Get the transaction type coming in for current request
 * @param txLife Current txLife transaction request
 * @return transType
 * @exception throws NbaBaseException
 */
public int getTransType(TXLife txLife) throws NbaBaseException {
    // SPR3290 code deleted
    UserAuthRequestAndTXLifeRequest request = 
        txLife.getUserAuthRequestAndTXLifeRequest();
    if (request == null) {
        throw new NbaBaseException("ERROR: Could not create a UserAuthRequestAndTXLifeRequest object");
    }
    TXLifeRequest txlife = request.getTXLifeRequestAt(0);
                  

    return ((int)txlife.getTransType());
}

/**
 * Primary method that handles the method calls to send DXE to the host
 * 
 * @param TXLife domain objects Contains the transaction request for the host
 * @return TXLife domain objects response from the host
 * @exception throws NbaBaseException
 */
// NBA195 - fixed spelling of parameter aNbaTXLife
public NbaTXLife submitRequestToHost(NbaTXLife aNbaTXLife) throws NbaBaseException {
	//Begin NBA195
	// Get System Access
	SystemAccess sysAccess = (SystemAccess) ServiceLocator.lookup(SystemAccess.SERVICENAME);
	// Prepare Input as to the service
	List list = new ArrayList(1);
	list.add(aNbaTXLife);
	// invoke CyberAdapter Service for CyberLifeDXE External System
	Result result = sysAccess.invoke("CyberLifeDXE/CyberAdapter", list);
	NbaTXLife nbaTxLifeResponse = null;
	if (!result.hasErrors()) {
		// if there are no errors, get the response out of result object
		nbaTxLifeResponse = (NbaTXLife) result.getData().get(0);
	} else {
		Message msgs[] = result.getMessages();
		if (msgs != null && msgs.length > 0) {
			List data = msgs[0].getData();
			if (data != null && data.size() > 0) {
				throw new NbaBaseException(data.get(0).toString());
			}
		}
	}
	//End NBA195
	return nbaTxLifeResponse;
}
/**
 * Secondary method that handles the method calls to send DXE to the host
 * 
 * @param XML document Contains the transaction request for the host
 * @return XML response from the host
 * @exception throws NbaBaseException
 */
public String submitRequestToHost(String xmlDoc) throws NbaBaseException {
	// Begin NBA195
	try {
		NbaTXLife nbaTXLife = new NbaTXLife(xmlDoc);
		return submitRequestToHost(nbaTXLife).toXmlString();
	} catch (Exception exp) {
		throw new NbaBaseException(NbaBaseException.SOURCE_XML, exp);
	}
	// End NBA195
}
}
