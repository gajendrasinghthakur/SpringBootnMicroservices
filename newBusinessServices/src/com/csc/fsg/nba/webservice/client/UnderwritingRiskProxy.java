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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.soap.Body;
import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.messaging.Message;
import org.apache.soap.rpc.Call;
import org.apache.soap.transport.http.SOAPHTTPConnection;
import org.w3c.dom.Element;

import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;

/**
 * This class makes a call to the Underwriting Risk Webservice.
 * It passes the request element received from client wrapper to webservice 
 * and gets the response element back from webservice, which is returned to client wrapper
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA124</td><td>Version 5</td><td>Underwriting Risk Remap</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */

public class UnderwritingRiskProxy {
	private Call call;
	private URL url = null;
	private String stringURL = "http://da067872:9081/lifews/services/Tx204";
	private java.lang.reflect.Method setTcpNoDelayMethod;
	private String timeout = "30000";
	private static NbaLogger logger = null;

	public UnderwritingRiskProxy() {
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

	public synchronized org.w3c.dom.Element service(org.w3c.dom.Element part) throws Exception {
		String targetObjectURI = "http://webservices.life.fsg.csc.com";
		String SOAPActionURI = "";
		SOAPHTTPConnection soapHTTPConnection = new SOAPHTTPConnection();

		if (getURL() == null) {
			throw new SOAPException(Constants.FAULT_CODE_CLIENT, "A URL must be specified via UnderwritingRiskProxy.setEndPoint(URL).");
		}

		// create message envelope and body
		Envelope msgEnv = new Envelope();
		Body msgBody = new Body();
		Vector vect = new Vector();

		vect.add(part);
		msgBody.setBodyEntries(vect);
		msgEnv.setBody(msgBody);

		// create and send message
		Message msg = new Message();
		soapHTTPConnection.setTimeout(Integer.parseInt(getTimeout()));
		msg.setSOAPTransport(soapHTTPConnection);
		msg.send(getURL(), SOAPActionURI, msgEnv);

		// receive response envelope
		Envelope env = msg.receiveEnvelope();
		Body retbody = env.getBody();
		java.util.Vector v = retbody.getBodyEntries();

		return (Element) v.firstElement();

	}

	protected Call createCall() {
		SOAPHTTPConnection soapHTTPConnection = new SOAPHTTPConnection();
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
	public String getTimeout() {
		return timeout;
	}

	/**
	 * Sets the timeout value in miliseconds mentioned in NbaConfiguration.xml file
	 * @param string
	 */
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
				logger = NbaLogFactory.getLogger(UnderwritingRiskProxy.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("UnderwritingRiskProxy could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

}
