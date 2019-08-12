package com.csc.fsg.nba.backendadapter;

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
import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberAdapter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.SourceInfo;

/** 
 * This class is used as the entry point for the client call to the backend adapters.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA013</td><td>Version 2</td><td>Correspondence System <BR>Added a parameter to submitRequestToHost method</td></tr>
 * <tr><td>SPR1018</td><td>Version 2</td><td>General cleanup</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Nba Pending Database</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8.90</td></tr>
 * <tr><td>SPR2131</td><td>Version 4</td><td>Remove Vantage wrappered support</td></tr>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaBackEndAdapterFacade {
    private static NbaLogger logger = null;
/**
 * Checks XML transaction for application name
 * @param xmlDoc XML document string 
 * @return java.lang.String containing application name(descriptor for backend system)
 * @exception NbaBaseException 
 */
public String getAppName(String xmlDoc) throws NbaBaseException {

	String appName = null;

	
	int beginIndex = xmlDoc.indexOf("FileControlID", 0);
	if (beginIndex == -1) {
		throw new NbaBaseException(NbaBaseException.BACKEND_SYSTEM_TYPE);
	}
	beginIndex = xmlDoc.indexOf(">", beginIndex);
	int endIndex = xmlDoc.indexOf("</", beginIndex);
	appName = xmlDoc.substring(beginIndex + 1, endIndex);

	return appName;
}
/**
 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
private static NbaLogger getLogger() {
    if (logger == null) {
        try {
            logger = NbaLogFactory.getLogger(NbaBackEndAdapterFacade.class.getName());
        } catch (Exception e) {
            NbaBootLogger.log(
                "NbaBackEndAdapterFacade could not get a logger from the factory.");
            e.printStackTrace(System.out);
        }
    }
    return logger;
}
/**
 * Main method that handles the method calls from client to send DXE to the host
 * @param NbatxLife A <code>NbatxLife</code> request
 * @param workItemId A Work Item (could be Case or Transaction) Id for which the Host transaction is initiated.
 * @return NbaTXLife A <code>NbatxLife</code> object containing XML reponse from the host
 * @exception java.rmi.RemoteException and NbaBaseException.
 */
//NBA013 new method parameter workItemId added 
public NbaTXLife submitRequestToHost(NbaTXLife NbatxLife, String workItemId) throws NbaBaseException {

    String appname = null;
    NbaBackEndAdapter adapter = null;

    if (getLogger().isDebugEnabled()) { // NBA027
    	getLogger().logDebug(NbatxLife.toXmlString());
    } // NBA027

    OLifE olife = NbatxLife.getOLifE();
    SourceInfo sourceInfo = olife.getSourceInfo();
    try {
        appname = sourceInfo.getFileControlID();

        if (appname.compareToIgnoreCase(NbaConstants.SYST_CYBERLIFE) == 0) { // SPR1018
            adapter = new NbaCyberAdapter();
        } else { // SPR2131
			throw new NbaBaseException(NbaBaseException.BACKEND_SYSTEM_TYPE); // SPR2131
        }
    } catch (Exception e) {
        throw new NbaBaseException(NbaBaseException.BACKEND_SYSTEM_TYPE, e);
    }

    //Begin NBA013
    NbaTXLife nbaTxlifeResponse = adapter.submitRequestToHost(NbatxLife);

	//NBA050 code deleted
    if (getLogger().isDebugEnabled()) { // NBA027
		getLogger().logDebug(nbaTxlifeResponse.toXmlString());
    } // NBA027

    return nbaTxlifeResponse;
    //End NBA013

     
}
/**
 * Main method that handles the method calls from client to send DXE to the host
 * @param XML document java.lang.String
 * @param workItemId A work item (maybe Case or Transaction) Id for which the Host Transaction was initiated.
 * @return java.lang.String containing XML reponse from the host
 * @exception java.rmi.RemoteException and NbaBaseException.
 */
//NBA013 new method parameter workItemId added  
public String submitRequestToHost(String xmlDoc, String workItemId) throws NbaBaseException {

    String appname = null;
    NbaBackEndAdapter adapter = null;

    getLogger().logDebug(xmlDoc);

    try {
        appname = getAppName(xmlDoc);

        if (appname.compareToIgnoreCase(NbaConstants.SYST_CYBERLIFE) == 0) { // SPR1018
            adapter = new NbaCyberAdapter();
        } else { // SPR2131
		    throw new NbaBaseException(NbaBaseException.BACKEND_SYSTEM_TYPE); // SPR2131
        }
    } catch (Exception e) {
        throw new NbaBaseException(NbaBaseException.BACKEND_SYSTEM_TYPE, e);
    }

    //Begin NBA013
    String responseXml = adapter.submitRequestToHost(xmlDoc);
    //NBA146 code deleted    
    getLogger().logDebug(responseXml);

    return responseXml;
    //End NBA013
}
}
