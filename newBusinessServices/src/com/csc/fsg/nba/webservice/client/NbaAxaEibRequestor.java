package com.csc.fsg.nba.webservice.client;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.csc.fsg.nba.exception.NbaBaseException;

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
 * <tr><td>AXAL3.7.34</td><td>AXA Life Phase 1</td><td>Contract Servies</td></tr>
 * <tr><td>PERF-APSL319</td><td>AXA Life Phase 1</td><td>PERF - AXA Interface Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaAxaEibRequestor extends NbaAxaServiceRequestor {
	private static String className = NbaAxaEibRequestor.class.getName();

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
	 * Create the SOAP header elements 
	 * @param parameters The input parameters from the calling process.
	 * @throws Exception
	 */
	protected void addHeader() throws Exception {
		SOAPElement eibHeader = header.addHeaderElement(envelope.createName("EIB_header", "h", "http://www.axa-equitable.com/schemas/EIB_header"));
		
		SOAPElement transaction = eibHeader.addChildElement("Transaction","h"); //PERF-APSL319
		transaction.addAttribute(envelope.createName("Trans_name"), getParameter(NbaAxaServiceRequestor.PARAM_SERVICEOPERATION));

		SOAPElement security = eibHeader.addChildElement(envelope.createName("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"));
		SOAPElement binarySecurityToken = security.addChildElement("BinarySecurityToken","wsse"); //PERF-APSL319
		binarySecurityToken.addTextNode(getParameter(NbaAxaServiceRequestor.PARAM_TOKEN));
	}
	/**
	 * Create the SOAP body elements.
	 * @param parameters The input parameters from the calling process.
	 * @throws Exception
	 */
	protected void addBody() throws Exception {
	    // Create the document for transformation into SOAPElement objects
    	Document document = createDocument();

        body.addDocument(document); //PERF-APSL319
        //PERF-APSL319 code deleted
	}
		

	/**
	 * Create a document representing the TxLife 103 transaction. 
	 * @return Document The document populated from the TxLife 103 XML
	 */
	//PERF-APSL319 New method
	protected Document createDocument() throws java.io.IOException {
		return loadXMLFromTXLife();  //ALII2055-5
	}
	//PERF-APSL319 new method
	protected org.w3c.dom.Document loadXMLFromTXLife() throws java.io.IOException {
		Document xmlDoc = null;
		StringReader sr = null;
		InputSource is = null;
		try {
			sr = new StringReader(getParameter(NbaAxaServiceRequestor.PARAM_NBATXLIFE)); //ALII2055-5
			is = new InputSource(sr);  //ALII2055-5
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = null;
			builder = factory.newDocumentBuilder();
			xmlDoc = builder.parse(is);  //ALII2055-5
		} catch (org.xml.sax.SAXException ex) {
			getLogger().logException(ex);
		} catch (javax.xml.parsers.ParserConfigurationException ex) {
			getLogger().logException(ex);
		} catch (java.io.IOException ex) {
		} catch (javax.xml.parsers.FactoryConfigurationError ex) {
			getLogger().logException(ex);
		} finally {
			if (sr != null) {
				sr.close();
			}
		}
		return xmlDoc;

	}
}
