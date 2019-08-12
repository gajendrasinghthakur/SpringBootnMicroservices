package com.csc.fsg.nba.webservice.client;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaConfiguration;


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
 * This class interacts with the AXA Controller service.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.68</td><td>AXA Life Phase 1</td><td>LDAP Interface</td></tr>
 * <tr><td>PERF-APSL319</td><td>AXA Life Phase 1</td><td>PERF - AXA Interface Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 6.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaAxaControllerRequestor extends NbaAxaServiceRequestor {
	private static String className = NbaAxaControllerRequestor.class.getName();

	
	/**
	 * Build the AXA Controller SOAP request
	 * @param parameters The input parameters from the calling process.
	 * @throws NbaBaseException
	 */
	protected void createRequest() throws NbaBaseException {
	    try {
	    	envelope.addNamespaceDeclaration(XSD, XSD_VALUE);
	    	envelope.addNamespaceDeclaration(XSI, XSI_VALUE);
	    	envelope.removeNamespaceDeclaration("soapenc"); //PERF-APSL319
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
	}
	/**
	 * Create the SOAP body elements.
	 * @param parameters The input parameters from the calling process.
	 * @throws Exception
	 */
	protected void addBody() throws Exception {
		//Begin PERF-APSL319
		Name name = envelope.createName("processRASRequest", "","http://www.bea.com/AXAControllerService");
		SOAPBodyElement processRASRequest = body.addBodyElement(name); 
		
		SOAPElement pairs = processRASRequest.addChildElement("pairs","","http://www.bea.com/AXAControllerService");
		
		SOAPElement pairElement = pairs.addChildElement("Pair","","java:com.axf.common.valueobject");
		SOAPElement nameElement = pairElement.addChildElement("name","","java:com.axf.common.valueobject");
		nameElement.addTextNode("axapartnerid");
		SOAPElement statusElement = pairElement.addChildElement("status","","java:com.axf.common.valueobject");
		statusElement.addTextNode("0");
		SOAPElement valueElement = pairElement.addChildElement("value","","java:com.axf.common.valueobject");
		String serviceOperation = getParameter(PARAM_SERVICEOPERATION);
		//begin ALS3400
		if (serviceOperation.equalsIgnoreCase(OPERATION_ROBOTIC_SINGLE_SIGNON)) {
			valueElement.addTextNode(PARTNER_ID);
		} else if (serviceOperation.equalsIgnoreCase(OPERATION_SECURE_FILE_TRANSFER)) {
			valueElement.addTextNode(NbaConfiguration.getInstance().getBusinessRulesAttributeValue("SFTPAxaPartnerId"));
		}
		//end ALS3400
		pairElement = pairs.addChildElement("Pair","","java:com.axf.common.valueobject");
		nameElement = pairElement.addChildElement("name","","java:com.axf.common.valueobject");
		nameElement.addTextNode("axaresourceid");
		statusElement = pairElement.addChildElement("status","","java:com.axf.common.valueobject");
		statusElement.addTextNode("0");
		valueElement = pairElement.addChildElement("value","","java:com.axf.common.valueobject");
		valueElement.addTextNode(serviceOperation);
		if (serviceOperation.equalsIgnoreCase(OPERATION_ROBOTIC_SINGLE_SIGNON)) {
			pairElement = pairs.addChildElement("Pair","","java:com.axf.common.valueobject");
			nameElement = pairElement.addChildElement("name","","java:com.axf.common.valueobject");
			nameElement.addTextNode("processname");
			statusElement = pairElement.addChildElement("status","","java:com.axf.common.valueobject");
			statusElement.addTextNode("0");
			valueElement = pairElement.addChildElement("value","","java:com.axf.common.valueobject");
			valueElement.addTextNode("NBALIFE_" + getParameter(PARAM_PROCESSNAME));
			 pairElement = pairs.addChildElement("Pair","","java:com.axf.common.valueobject");
			nameElement = pairElement.addChildElement("name","","java:com.axf.common.valueobject");
			nameElement.addTextNode("processtype");
			statusElement = pairElement.addChildElement("status","","java:com.axf.common.valueobject");
			statusElement.addTextNode("0");
			valueElement = pairElement.addChildElement("value","","java:com.axf.common.valueobject");
			valueElement.addTextNode("SPECIAL");
			
		}
		//end PERF-APSL319
		parameters.put(PARAM_SERVICEOPERATION, "processRASRequest");
	}

}
