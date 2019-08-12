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
 * *******************************************************************************<BR>
 */
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.soap.SOAPException;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;

import com.csc.fsg.nba.exception.NbaWebClientFaultException;
import com.csc.fsg.nba.exception.NbaWebServerFaultException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaTXLife;
/**
 * NbaContractPrintExtractWebServiceClient is the client wrapper for the automatically generated Web Service Proxy for the 
 * Contract Print Extract Web Service: NbaContractPrintExtractWebServiceProxy.
 * NbaContractPrintExtractWebServiceClient is responsible for marhalling and un-marsalling an NbaTXLife to and from a DOM Element
 * and invoking the Web Service Proxy.
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
* <tr><td>NBA113</td><td>Version 5</td><td>V4 Software Upgrades </td></tr>
* <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
* <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 4
*/
public class NbaContractPrintExtractWebServiceClient extends NbaWebServiceAdapterBase {
	public static final String FAULT_ELEMENT = "soapenv:Fault";
	protected static NbaLogger logger = null;
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractPrintExtractWebServiceClient.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractPrintExtractWebServiceClient could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Marhall an NbaTXLife to a DOM Element. Invoke the Nba Contract Print Extract Web Service Client to 
	 * store Contract Print Extracts. Unmarshall the DOM Element response into an NbaTXLife. 
	 * @param nbATxLife An instance of <code>NbaTXLife</code>
	 * @param object
	 * @return NbaTXLife A <code>NbaTXLife</code> object containing the response from the webservice. 
	 */

	public NbaTXLife invokeWebService(NbaTXLife nbATxLife) throws NbaWebClientFaultException, NbaWebServerFaultException { // SPR2968
		// SPR3290 code deleted
		NbaTXLife nbaTXLife = null;
		try {
			NbaContractPrintExtractWebServiceProxy proxy = new NbaContractPrintExtractWebServiceProxy();
			proxy.setEndPoint(new URL(getWsdlUrl()));
			proxy.setTimeout(getTimeout());
			// NBA113 code deleted
			Response resp = proxy.service(nbATxLife.toXmlString());	//Pass the TxLife object	//NBA113
			Parameter refValue = resp.getReturnValue();	//NBA113
			nbaTXLife = new NbaTXLife((java.lang.String) refValue.getValue());	//NBA113
			// NBA113 code deleted
		} catch (MalformedURLException e) {
			throw new NbaWebServerFaultException("Configuration error: Malformed URL", e);
		// NBA113 code deleted
		} catch (IOException e) {
			throw new NbaWebServerFaultException("I/O Exception", e);
		} catch (SOAPException e) {
			throw new NbaWebServerFaultException("SOAP Exception", e);
			
		} catch (Exception e) {
			throw new NbaWebServerFaultException("Exception", e);
		}
		return nbaTXLife;
	}
}
