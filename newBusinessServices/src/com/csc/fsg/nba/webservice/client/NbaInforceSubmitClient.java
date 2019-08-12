
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
 * NbaInforceSubmitClient is the wrapper class on actual client class for AccountEntry WebService.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA076</td><td>Version 3</td><td>Contract to Issue Admin</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaInforceSubmitClient extends NbaWebServiceAdapterBase {
	protected InforceSubmitProxy proxyClient = null;
	private static NbaLogger logger = null;
		/**
		 * Constructor for NbaInforceSubmitClient.
		 */
		public NbaInforceSubmitClient() {
			super();
		}

		/**
		 * The invokeWebService method accepts two arguments and calls the InforcePaymentWeb proxy Client class. After instantiating the
		 * proxy client this method passes txLife508 transaction and one more object in client and gets the response xml back from WebService.
		 * params@ nbATxLife NbaTXLife object
		 * params@ obj Object
		 * returns@ response NbaTXLife object returned from WebService
		 */
		public NbaTXLife invokeWebService(NbaTXLife nbATxLife) { // SPR2968
			NbaTXLife response = null;
			try{
				proxyClient = new InforceSubmitProxy();
				proxyClient.setAccess(getAccess());
				proxyClient.setTargetUri(getTargetUri());
				proxyClient.setWsdlUrl(getWsdlUrl());
				response = proxyClient.invokeWebService(nbATxLife); // SPR2968
			}catch(Exception e){
				if (getLogger().isErrorEnabled())
		   			getLogger().logError(e);
			}
			return response;
		}
		/**
		 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
		 * @return the logger implementation
		 */
		private static NbaLogger getLogger() {
			if (logger == null) {
				try {
					logger = NbaLogFactory.getLogger(NbaInforceSubmitClient.class.getName());
				} catch (Exception e) {
					NbaBootLogger.log("NbaAccountEntryClient could not get a logger from the factory.");
					e.printStackTrace(System.out);
				}
			}
			return logger;
		}

}
