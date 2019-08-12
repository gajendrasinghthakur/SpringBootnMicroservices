package com.csc.fsg.nba.webservice.client;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeaderElement;

import com.csc.fsg.nba.exception.NbaBaseException;


/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

/** 
 * This class interacts with the AXA Single Sign On verification service.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.68</td><td>Version 7</td><td>LDAP Interface</td></tr>
 * <tr><td>PERF-APSL319</td><td>AXA Life Phase 1</td><td>PERF - AXA Interface Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 6.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaAxaOnlineSingleSignOnRequestor extends NbaAxaServiceRequestor {
	private static String className = NbaAxaOnlineSingleSignOnRequestor.class.getName();
	
	/**
	 * Build the AXA Single Sign On SOAP request
	 * @param parameters The input parameters from the calling process.
	 * @throws NbaBaseException
	 */
	protected void createRequest() throws NbaBaseException {
	    try {
	    	envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
	    	envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
	    	envelope.addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");
	    	envelope.addNamespaceDeclaration("env", "http://schemas.xmlsoap.org/soap/envelope/");
	    	envelope.addNamespaceDeclaration("dp", "http://www.datapower.com/schemas/management");
	    	envelope.removeNamespaceDeclaration("soapenc");  //PERF-APSL319

	    	addHeader();
	    	addBody();

	    } catch (Exception e) {
		    getLogger().logException(e);
	        throw new NbaBaseException(NbaBaseException.INVALID_REQUEST);
	    }
	}
	/**
	 * Create the SOAP header elements 
	 * @param parameters The input parameters from the calling process.
	 * @throws Exception
	 */
	protected void addHeader() throws Exception {
		//Begin PERF-APSL319
		SOAPElement security = header.addChildElement("Security","wsse","http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		security.addNamespaceDeclaration("wsse","http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		SOAPElement binarySecurityToken =  security.addChildElement("BinarySecurityToken","wsse");
		binarySecurityToken.addTextNode(getParameter(NbaAxaServiceRequestor.PARAM_TOKEN));
		SOAPElement timestamp = security.addChildElement("Timestamp","wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		timestamp.setAttribute("wsu:timestamp",getParameter("Timestamp"));
		SOAPElement timestampExpires = timestamp.addChildElement("Expires","wsu");
		timestampExpires.addTextNode(getParameter("Expires"));
		//End PERF-APSL319

	}
	/**
	 * Create the SOAP body elements 
	 * @param parameters The input parameters from the calling process.
	 * @throws Exception
	 */
	protected void addBody() throws NbaBaseException {
		//begin PERF-APSL319
		try {
			Name name = envelope.createName("processSSORequest", "","http://www.bea.com/AXASSOService");
			SOAPBodyElement processSSORequest = body.addBodyElement(name); 
		
			SOAPElement pairs = processSSORequest.addChildElement("pairs","","http://www.bea.com/AXASSOService");
			//Source App Pair Element
			SOAPElement pairElement = pairs.addChildElement("Pair","","java:com.axf.common.valueobject");
			SOAPElement nameElement = pairElement.addChildElement("name","","java:com.axf.common.valueobject");
			nameElement.addTextNode("sourceapp");
			SOAPElement statusElement = pairElement.addChildElement("status","","java:com.axf.common.valueobject");
			statusElement.addTextNode("-1");
			SOAPElement valueElement = pairElement.addChildElement("value","","java:com.axf.common.valueobject");
			valueElement.addTextNode(getParameter("SourceApp"));
			//Destination App
			pairElement = pairs.addChildElement("Pair","","java:com.axf.common.valueobject");
			nameElement = pairElement.addChildElement("name","","java:com.axf.common.valueobject");
			nameElement.addTextNode("destinationapp");
			statusElement = pairElement.addChildElement("status","","java:com.axf.common.valueobject");
			statusElement.addTextNode("-1");
			valueElement = pairElement.addChildElement("value","","java:com.axf.common.valueobject");
			valueElement.addTextNode(getParameter("DestinationApp"));

			//PartnerID
			pairElement = pairs.addChildElement("Pair","", "java:com.axf.common.valueobject");
			nameElement = pairElement.addChildElement("name","","java:com.axf.common.valueobject");
			nameElement.addTextNode("axapartnerid");
			statusElement = pairElement.addChildElement("status","","java:com.axf.common.valueobject");
			statusElement.addTextNode("-1");
			valueElement = pairElement.addChildElement("value","","java:com.axf.common.valueobject");
			valueElement.addTextNode(PARTNER_ID);
			}
			catch (Exception e) {
				 getLogger().logException(e);
				 throw new NbaBaseException(e);
			}
		//end PERF-APSL319
	}
	/**
	 * Stub method for UDDI support, which is not applicable for this service operation.
	 * @param parameters The input parameters from the calling process.
	 */
	protected void getAddressFromUDDI() {
	    // Do nothing
	}

}
