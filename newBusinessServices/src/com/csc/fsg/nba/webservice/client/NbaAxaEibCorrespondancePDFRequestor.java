package com.csc.fsg.nba.webservice.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.ibm.ws.webservices.engine.xmlsoap.SOAPFactory;

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

/**
 * This class interacts with all AXA service operations over the AXA EIB.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal Correspondence</td></tr>
 * <tr><td>PERF-APSL319</td><td>AXA Life Phase 1</td><td>PERF - AXA Interface Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaAxaEibCorrespondancePDFRequestor extends NbaAxaCorrespondenceRequestor { //PERF-APSL319
	private static String className = NbaAxaEibCorrespondancePDFRequestor.class.getName();

	//<Keys>	<Key name=\"CONTRACTNUMBER\">IWP6500375</Key>	</Keys>
	
	private final String KEYS_BEGIN = "<Keys>";
	private final String KEY_BEGIN = "<Key ";
	private final String NAME_ATTRIBUTE_BEGIN = "name=\"";
	private final String NAME_ATTRIBUTE_END = "\">";
	private final String KEY_END = "</Key>";
	private final String KEYS_END = "</Keys>";
	

	/**
	 * Build the AXA EIB SOAP request
	 * @param parameters The input parameters from the calling process.
	 * @throws NbaBaseException
	 */
	protected void createRequest() throws NbaBaseException {
	    try {
	    	envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
	    	envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");

	        addHeader();
	        addBody();
	    } catch (Exception e) {
		    getLogger().logException(e);
	        throw new NbaBaseException(NbaBaseException.INVALID_REQUEST);
	    }
	}

	/**
	 * Build the AXA EIB SOAP request
	 * @param parameters The input parameters from the calling process.
	 * @throws NbaBaseException
	 */
	protected void createRequestPDF() throws NbaBaseException {
	    try {
	    	envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
	    	envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");

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
		SOAPElement eibHeader = header.addHeaderElement(envelope.createName("EIB_header", "h", "http://www.axa-equitable.com/schemas/EIB_header"));
		
		SOAPElement transaction = eibHeader.addChildElement("Transaction");
		transaction.addAttribute(envelope.createName("Trans_name"), getParameter(NbaAxaServiceRequestor.PARAM_SERVICEOPERATION));

		SOAPElement security = eibHeader.addChildElement(envelope.createName("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"));
		SOAPElement binarySecurityToken = security.addChildElement("BinarySecurityToken");
		binarySecurityToken.addTextNode(getParameter(NbaAxaServiceRequestor.PARAM_TOKEN));
	}
	/**
	 * Create the SOAP body elements.
	 * @param parameters The input parameters from the calling process.
	 * @throws Exception
	 */
	protected void addBody() throws Exception {
		SOAPElement eibBody = body.addBodyElement(envelope.createName(getParameter("operation"),"xpr", "http://www.axa-equitable.com/xPression"));
		
		SOAPFactory factory = new SOAPFactory();

		Name docName = factory.createName("documentName");
		SOAPElement soapDocName = eibBody.addChildElement(docName);
		soapDocName.addTextNode(getParameter("docName")); 
		
		// customer Keys
		Name custKeys = factory.createName("customerKeys");
		SOAPElement soapCustKeys = eibBody.addChildElement(custKeys);
		HashMap keysMap = (HashMap) getParameters().get("keysMap");
		Iterator i = keysMap.entrySet().iterator();
		Map.Entry param = null;
		StringBuffer custKeysData = new StringBuffer();
		custKeysData.append(KEYS_BEGIN);
		while (i.hasNext()) {
			param = (Map.Entry)i.next();
		    String attribute = (String)param.getKey();
		    String keyValue = (String)param.getValue();
		    custKeysData.append(KEY_BEGIN);
		    custKeysData.append(NAME_ATTRIBUTE_BEGIN);
		    custKeysData.append(attribute);
		    custKeysData.append(NAME_ATTRIBUTE_END);
		    custKeysData.append(keyValue);
		    custKeysData.append(KEY_END);
		}		
		custKeysData.append(KEYS_END);
		soapCustKeys.addTextNode(custKeysData.toString());

		Name customerData = factory.createName("customerData");
		SOAPElement custData = eibBody.addChildElement(customerData);
		String customerDataStr = getParameter("customerData");
		// get the data at position 38 to remove the <?xml version="1.0" encoding="UTF-8"?>  
		if (customerDataStr.length() > 38) {
			customerDataStr = customerDataStr.substring(38);
        }
		customerDataStr = "<root>" + customerDataStr + "</root>";  
		custData.addTextNode(customerDataStr); 		
		
		// Data source name
		Name dataSourceName = factory.createName("dataSourceName");
		SOAPElement soapProfileDocName = eibBody.addChildElement(dataSourceName);
		soapProfileDocName.addTextNode(NbaConfiguration.getInstance().getCorrespondence().getEventDrivenAt(0).getDataSourceName()); 		
		
		
		Name outputProfile = factory.createName("outputProfile");
		SOAPElement soapOutputProfile = eibBody.addChildElement(outputProfile);
		soapOutputProfile.addTextNode(NbaConfiguration.getInstance().getCorrespondence().getEventDrivenAt(0).getOutputProfileName()); 		
		
		Name nameUsr = factory.createName("userName");
		SOAPElement soapUsr = eibBody.addChildElement(nameUsr);
		soapUsr.addTextNode(getParameter("userName"));

		// Read from NbaCOnfiguration.xml
		Name namePwd = factory.createName("password");
		SOAPElement soapPwd = eibBody.addChildElement(namePwd);
		soapPwd.addTextNode(getParameter("password")); 		
		
		Name applicationName = factory.createName("applicationName");
		SOAPElement transformationName = eibBody.addChildElement(applicationName);
		transformationName.addTextNode(NbaConfiguration.getInstance().getCorrespondence().getEventDrivenAt(0).getApplicationName()); 	
		
	}
	
	
}
