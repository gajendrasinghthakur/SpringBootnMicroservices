package com.csc.fsg.nba.webservice.client;

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

import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * NbaClientSearchClient is the warpper class on actual client class for ClientSearch WebService.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA067</td><td>Version 3</td><td>Client Search</td></tr>
 * <tr><td>SPR1751</td><td>Version 4</td><td>Remove import statements referencing com.csc.fsg.nba.development package</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaClientSearchClient extends NbaWebServiceAdapterBase {
	protected ClientSearchWebProxy proxyClient = null;
	protected static NbaLogger logger = null; //SPR3290

	/**
	 * Constructor for NbaClientSearchClient.
	 */
	public NbaClientSearchClient() {
		super();
	}

	/**
	 * The invokeWebService method accepts two arguments and calls the ClientSearch proxy Client class. After instantiating the
	 * proxy client this method passes txLife301 transaction and one more object in client and gets the response xml back from WebService.
	 * @param nbATxLife The NbaTXLife object
	 * @param obj The object
	 * @return response NbaTXLife object returned from WebService
	 */
	public NbaTXLife invokeWebService(NbaTXLife nbATxLife) { // SPR2968
		NbaTXLife response = null;
		try{
			//begin SPR3290
			if (getLogger().isDebugEnabled()) {
                getLogger().logDebug("client search webservice request  " + nbATxLife.toXmlString());
            }
            //end SPR3290
            proxyClient = new ClientSearchWebProxy();
            proxyClient.setAccess(getAccess());
            proxyClient.setTargetUri(getTargetUri());
            proxyClient.setWsdlUrl(getWsdlUrl());
            response = proxyClient.invokeWebService(nbATxLife); // SPR2968
            
            //beging SPR3290
            if (getLogger().isDebugEnabled()) {
                getLogger().logDebug("client search webservice response  " + response.toXmlString());
            }
            //end SPR3290
		}catch(Exception e){
			getLogger().logException(e); //NBA103 SPR3290
		}
		return response;
	}
	 /**
      * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
      * 
      * @return com.csc.fsg.nba.foundation.NbaLogger
      */
    //SPR3290 New Method
    protected static NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(NbaContractPrintExtractWebServiceClient.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log("NbaClientSearchClient could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }
}
