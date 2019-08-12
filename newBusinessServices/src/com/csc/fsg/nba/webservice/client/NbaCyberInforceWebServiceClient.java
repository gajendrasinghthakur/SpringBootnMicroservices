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
import com.csc.fsg.nba.backendadapter.cyberlifeInforce.NbaCyberInforceAdapter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * NbaCyberInforceWebServiceClient is the client class to call nbA hosted CyberLife adapters.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA076</td><td>Version 3</td><td>Initial Development</td></tr>
 * <tr><td>SPR1829</td><td>Version 4</td><td>Modify to propagate NbaBaseExceptions</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>Code clean up</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaCyberInforceWebServiceClient extends NbaWebServiceAdapterBase {
	//SPR2992 code deleted
	private NbaTXLife nbaTxlifeResponse = null;
	private NbaLogger logger;
	/**
	 * Constructor for NbaCyberInforceWebServiceClient.
	 */
	public NbaCyberInforceWebServiceClient() {
		super();
	}
	/**
	 * After getting the instance of this client class using Factory classes, this 
	 * invokeWebService method will be used to call TestWebService. This method will set the value of 
	 * txLifeResponse. After that, getTxLifeResponse() method will be used to access the value of 
	 * txLifeResponse(object of NbaTXLife).
	 * @throws NbaBaseException
	 */
	public NbaTXLife invokeWebService(NbaTXLife nbATxLife) throws NbaBaseException { //SPR1829 SPR2968
		try {
			if (getLogger().isDebugEnabled()) {	//SPR2992
				getLogger().logDebug(nbATxLife.toXmlString());	//SPR2992
			}	//SPR2992
			NbaCyberInforceAdapter adapter = new NbaCyberInforceAdapter();
			nbaTxlifeResponse = adapter.submitRequestToHost(nbATxLife);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug(nbaTxlifeResponse.toXmlString());
			}
		//SPR1829
		} catch (NbaBaseException e) {
			throw e;
		} catch (Exception e) {
			throw new NbaBaseException("Unexpected Error Backend Adapter Exception", e);
		}
		//End SPR1829
		return nbaTxlifeResponse;
	}
	/**
	 * This method returns the response of WebService in form of NbaTXLife object.
	 * @return txLifeResponse NbaTXLife
	 */
	public NbaTXLife getTxLifeResponse() {
		return nbaTxlifeResponse;
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private NbaLogger getLogger() {
		//private NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger("NbaTestWebService");
			} catch (Exception e) {
				NbaBootLogger.log("NbaTestWebService could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
