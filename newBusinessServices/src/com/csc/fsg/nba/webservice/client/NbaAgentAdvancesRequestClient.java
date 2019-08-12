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

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.csc.fsg.life.tools.xml.XmlWriter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
* Client class for NbaAgentAdvancesRequest web service, It exposes method to process TXLife request and send TXLife response
* Its a wrapper around the auto genrated proxy 
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
* </thead>
* <tr><td>NBA137</td><td>Version 6</td><td>nbA Agent Advances</td></tr>*
* </table></br>
* </p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 6
*/
public class NbaAgentAdvancesRequestClient extends NbaWebServiceAdapterBase {

	public static final String FAULT_ELEMENT = "soapenv:Fault";
	/**
	 * This method invokes the web service to process Agent Advances,
	 * @param nbATxLife Tx103 subtype1000500022 request
	 * @param object
	 * @return NbaTXLife Tx103 response from the webservice 
	 */
	public NbaTXLife invokeWebService(NbaTXLife nbATxLife) throws NbaBaseException {
		// SPR3290 code deleted
		NbaTXLife agentAdvancesResp = null;
		try {
		    AgentAdvancesRequestorProxy proxy = new AgentAdvancesRequestorProxy();
			proxy.setEndPoint(new URL(getWsdlUrl()));
			proxy.setTimeout(getTimeout()); // SPR3290
			StringReader stringReader = new java.io.StringReader(nbATxLife.toXmlString());

			InputSource inputSource = new InputSource(stringReader);
			DOMParser domParser = new DOMParser();
			domParser.parse(inputSource);
			Document document = domParser.getDocument();
			Element passedEle = document.getDocumentElement();
			Element responseEle = proxy.service(passedEle);
			if(FAULT_ELEMENT.equalsIgnoreCase(responseEle.getNodeName())) {
				throw new NbaBaseException("Webservice request timeout or invalid webservice URL", NbaExceptionType.FATAL);
			}
			XmlWriter xmlwriter = new XmlWriter();
			StringBuffer sb = new StringBuffer();
			xmlwriter.printDOM(responseEle, sb);
			agentAdvancesResp = new NbaTXLife(NbaUtils.convertDOMElementToString(responseEle));
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.AGENT_WEBSERVICE_PROCESSING_ERROR, e, NbaExceptionType.FATAL);
		}
		return agentAdvancesResp;
	}
}
