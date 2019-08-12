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

import java.io.StringReader;
import java.net.URL;

import org.apache.soap.SOAPException;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.csc.fsg.life.tools.xml.XmlWriter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
* nbA's wrapper class for Underwriting Risk webservice auto generated proxy.
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>NBA124</td><td>Version 5</td><td>Underwriting Risk Remap</td></tr>
* <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 5
*/
public class NbaUnderwritingRiskClient extends NbaWebServiceAdapterBase {

	/**
	 * This method invokes the web service proxy to perform underwriting risk webservice call. 
	 * It processes the input <code>NbaTXLife</code> in a format which Proxy class understands and calls the proxy.
	 * It then handles the proxy class response and formats it to a <code>NbaTXLife</code>   
	 * @param nbATxLife Tx204 request transaction
	 * @param object a dummy object
	 * @return Tx204 response from the webservice 
	 */
	public NbaTXLife invokeWebService(NbaTXLife nbATxLife) throws NbaBaseException { // SPR2968

		NbaTXLife underwritingRiskResponse = null;
		try {
			//instantiate the proxy
			UnderwritingRiskProxy proxy = new UnderwritingRiskProxy();
			proxy.setEndPoint(new URL(getWsdlUrl()));
			proxy.setTimeout(getTimeout());
			//format the request XML in a format that proxy understands
			StringReader stringReader = new StringReader(nbATxLife.toXmlString());
			InputSource inputSource = new InputSource(stringReader);
			DOMParser domParser = new DOMParser();
			domParser.parse(inputSource);
			Document document = domParser.getDocument();
			Element passedEle = document.getDocumentElement();
			Element responseEle = proxy.service(passedEle);
			if (FAULT_ELEMENT.equalsIgnoreCase(responseEle.getNodeName())) {
				throw new NbaBaseException("Webservice fault occoured. Request timed out or an invalid webservice URL referred");
			}
			XmlWriter xmlwriter = new XmlWriter();
			StringBuffer sb = new StringBuffer();
			xmlwriter.printDOM(responseEle, sb);
			underwritingRiskResponse = new NbaTXLife(NbaUtils.convertDOMElementToString(responseEle));
		} catch (SOAPException e) {
			throw new NbaBaseException("Webservice timed out.", e);
		} catch (Exception e) {
			throw new NbaBaseException(e);
		}
		return underwritingRiskResponse;
	}
}
