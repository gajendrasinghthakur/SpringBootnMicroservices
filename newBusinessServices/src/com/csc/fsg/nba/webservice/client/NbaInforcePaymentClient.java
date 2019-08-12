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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.soap.transport.http.SOAPHTTPConnection;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.csc.fsg.life.tools.xml.XmlWriter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * NbaInforcePaymentClient is the warpper class on actual client class for NbaInforcePaymentClient WebService.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA068</td><td>Version 3</td><td>Inforce Payment</td></tr>
 * <tr><td>SPR1751</td><td>Version 4</td><td>Remove import statements referencing com.csc.fsg.nba.development package</td></tr>
 * <tr><td>NBA108</td><td>Version 4</td><td>Vantage Inforce Payment</td></tr>
 * <tr><td>NBA109</td><td>Version 4</td><td>Vantage Loan Payment</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaInforcePaymentClient extends NbaWebServiceAdapterBase {
	protected InforcePaymentWebProxy proxyClient = null;
	private static NbaLogger logger = null; //NBA108 NBA109

	/**
	 * Constructor for NbaInforcePaymentClient.
	 */
	public NbaInforcePaymentClient() {
		super();
	}

	/**
	 * This method accepts NbaTXLife object as parameter which is xml508 transaction for inforce payments. After that this method
	 * calls webservice proxy client and pass xml transaction to it. The proxy class communicates with webservice and returns the 
	 * response back to this method. Then this method returns the response in form of NbaTXLife to the calling class.
	 * @param nbATxLife NbaTXLife
	 * @param obj Object
	 * @return NbaTXLife
	 * @throws NbaBaseException
	 */
	//NBA108 NBA109 new method
	public NbaTXLife invokeWebService(NbaTXLife nbATxLife)throws NbaBaseException { // SPR2968
		// SPR3290 code deleted
		NbaTXLife txLifeInforcePaymentResp = null;
		java.lang.reflect.Method setTcpNoDelayMethod;
		try {
			proxyClient = new InforcePaymentWebProxy();
			java.net.URL url = new java.net.URL(getWsdlUrl());
			proxyClient.setEndPoint(url);
			proxyClient.setTimeout(getTimeout());
			StringReader stringReader = new java.io.StringReader(nbATxLife.toXmlString());

			InputSource inputSource = new org.xml.sax.InputSource(stringReader);
			DOMParser domParser = new org.apache.xerces.parsers.DOMParser();
			domParser.parse(inputSource);
			Document document = domParser.getDocument();
			Element passedEle = document.getDocumentElement();

			try {
				setTcpNoDelayMethod = SOAPHTTPConnection.class.getMethod("setTcpNoDelay", new Class[] { Boolean.class });
				} catch (Exception e) {
					throw new NbaBaseException(e);
				}

			// call = createCall();
			Element responseEle = proxyClient.service(passedEle);
			if("soapenv:Fault".equalsIgnoreCase(responseEle.getNodeName())) {
				throw new NbaBaseException("Fault received as response : either webservice timed out or url is wrong or some other reason" );
			}
			XmlWriter xmlwriter = new XmlWriter();
			StringBuffer sb = new StringBuffer();
			xmlwriter.printDOM(responseEle, sb);
			txLifeInforcePaymentResp = new NbaTXLife(element2String(responseEle));
		} catch (Exception e) {
			//NBA108 NBA109 line deleted
			//begin NBA108 NBA109
			if(e instanceof NbaBaseException) {
				throw (NbaBaseException)e ;
			}else{ 
				throw new NbaBaseException(e);
			}
			//end NBA108 NBA109
		}
		return txLifeInforcePaymentResp;
	}

	/**
	* Receives an Element and converts it into a String
	* @param ele Element which contains the element to be converted into String
	* @return String 
	* @throws IOException
	*/
	//NBA108 NBA109 new method
	protected static String element2String(Element ele) throws java.io.IOException {
		StringWriter sw = new StringWriter();
		OutputFormat oFormatter = new OutputFormat("XML", null, false);
		XMLSerializer oSerializer = new XMLSerializer(sw, oFormatter);
		oSerializer.serialize(ele);
		return sw.toString();
	}  

	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return the logger implementation
	*/
	//NBA108 NBA109 new method
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaInforcePaymentClient.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaInforcePaymentClient could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
