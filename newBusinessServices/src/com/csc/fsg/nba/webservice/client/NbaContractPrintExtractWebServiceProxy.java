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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.soap.Constants;
import org.apache.soap.Fault;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.apache.soap.transport.http.SOAPHTTPConnection;

import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
/**
 * NbaContractPrintExtractWebServiceProxy is a Web Service proxy for the 
 * Contract Print Extract Web Service. The Service is accessed using SOAP
 * with RPC Literal style encoding. 
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>NBA113</td><td>Version 5</td><td>V4 Software Upgrades </td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */
public class NbaContractPrintExtractWebServiceProxy {
	private Call call;
	private URL url = null;
	private String stringURL = "http://localhost:8080/nba_webservice_WAR/servlet/rpcrouter";
	private java.lang.reflect.Method setTcpNoDelayMethod;
	private String timeout = "300000"; //NBA100
	private static NbaLogger logger = null; //NBA100
	public NbaContractPrintExtractWebServiceProxy() {
		try {
			setTcpNoDelayMethod = SOAPHTTPConnection.class.getMethod("setTcpNoDelay", new Class[] { Boolean.class });
		} catch (Exception e) {
		}
		call = createCall();
	}
	public synchronized void setEndPoint(URL url) {
		this.url = url;
	}
	public synchronized URL getEndPoint() throws MalformedURLException {
		return getURL();
	}
	private URL getURL() throws MalformedURLException {
		if (url == null && stringURL != null && stringURL.length() > 0) {
			url = new URL(stringURL);
		}
		return url;
	}
	public synchronized Response service(String ele) throws Exception { //NBA113
		String targetObjectURI = "http://tempuri.org/com.csc.fsg.nba.ejb.webservice.NbaContractPrintExtractWebService";
		String SOAPActionURI = "";
		if (getURL() == null) {
			throw new SOAPException(
				Constants.FAULT_CODE_CLIENT,
				"A URL must be specified via NbaContractPrintExtractWebServiceProxy.setEndPoint(URL).");
		}
		call.setMethodName("service");
		call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);	//NBA113 
		call.setTargetObjectURI(targetObjectURI);
		Vector params = new Vector();
		Parameter eleParam = new Parameter("ele", java.lang.String.class, ele, Constants.NS_URI_SOAP_ENC);	//NBA113 
		params.addElement(eleParam);
		call.setParams(params);
		Response resp = call.invoke(getURL(), SOAPActionURI);
		//Check the response.
		if (resp.generatedFault()) {
			Fault fault = resp.getFault();
			call.setFullTargetObjectURI(targetObjectURI);
			throw new SOAPException(fault.getFaultCode(), fault.getFaultString());
		} else {
			// NBA113 code deleted
			return resp;	//NBA113 
		}
	}
	protected Call createCall() {
		SOAPHTTPConnection soapHTTPConnection = new SOAPHTTPConnection();
		soapHTTPConnection.setTimeout(getTimeout()); //NBA100
		if (setTcpNoDelayMethod != null) {
			try {
				setTcpNoDelayMethod.invoke(soapHTTPConnection, new Object[] { Boolean.TRUE });
			} catch (Exception ex) {
			}
		}
		Call call = new Call();
		call.setSOAPTransport(soapHTTPConnection);
		SOAPMappingRegistry smr = call.getSOAPMappingRegistry();
		return call;
	}
	/**
	 * Returns timeout value in miliseconds mentioned in NbaConfiguration.xml file
	 * @return
	 */
	// NBA100 New Method
	public int getTimeout() {
		return Integer.parseInt(timeout);
	}
	/**
	 * Sets the timeout value in miliseconds mentioned in NbaConfiguration.xml file
	 * @param string
	 */
	// NBA100 New Method
	public void setTimeout(String string) {
		timeout = string;
	}
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return the logger implementation
	*/
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractPrintExtractWebServiceProxy.class.getName()); //NBA103
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractPrintExtractWebServiceProxy could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
