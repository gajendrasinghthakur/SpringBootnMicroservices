package com.csc.fsg.nba.webservice.client;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
 * <tr><td>AXAL3.7.32</td><td>AXA Life Phase 2</td><td>Reinsurance Interface</td></tr>
 * <tr><td>PERF-APSL319</td><td>AXA Life Phase 1</td><td>PERF - AXA Interface Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaAxaEibReinsuranceRequestor extends NbaAxaEibRequestor {
	private static String className = NbaAxaEibReinsuranceRequestor.class.getName();
	/**
	 * Create the SOAP body elements.
	 * @param parameters The input parameters from the calling process.
	 * @throws Exception
	 */
	protected void addBody() throws Exception {
		//Create the document for transformation into SOAPElement objects
    	Document document = createDocument();
        Element documentElement = document.getDocumentElement();

        // Create the root TxLife object in the SOAPBody
        SOAPElement txLifeElement = body.addBodyElement(envelope.createName("ReinsuranceCases"));

		
		buildSOAPElements(documentElement, txLifeElement);
		//body.addTextNode(getParameter("reinsuranceRequest"));//AXAL3.7.32		
	}
	
	protected Document createDocument() {
	    try {
	        StringBuffer stringBuffer = new StringBuffer(getParameter("reinsuranceRequest"));
	        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	        return documentBuilder.parse(new InputSource(new StringReader(stringBuffer.toString())));
        } catch (Exception e) {
            getLogger().logError("An error occurred while parsing the XML stream");
            getLogger().logException(e);
            return null;
        }
	}
	/**
	 * Create a SOAPElement object representing a particular element from the TxLife 103 document. 
	 * @param element  The element to create from the TxLife 103 document.
	 * @param soapElement  The SOAPElement object that is currently under construction.
	 * @throws Exception
	 */
	//PERF-APSL319 New Method
	protected void buildSOAPElements(Element element, SOAPElement soapElement) throws Exception {
		Node node = null;
	    for (int i = 0; i < element.getAttributes().getLength(); i++) {
	        node = element.getAttributes().item(i);
	        processNode(node, soapElement);
	    }
		NodeList nodeList = element.getChildNodes();
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        node = nodeList.item(i);
	        processNode(node, soapElement);
	    }
	}
	/**
	 * Create the appropriate component on the SOAP message for the given component from the 
	 * TxLife 103 document. 
	 * @param node  A component of the TxLife 103 document.
	 * @param element  The SOAPElement that is currently under construction.
	 * @throws Exception
	 */
	//PERF-APSL319 new method
	protected void processNode(Node node, SOAPElement element) throws Exception {
        switch (node.getNodeType()) {
        	case (Node.ELEMENT_NODE):
        		buildSOAPElements((Element)node, element.addChildElement(node.getNodeName()));
        		break;
        	case (Node.ATTRIBUTE_NODE):
        		element.addAttribute(envelope.createName(node.getNodeName()), node.getNodeValue());
   	    		break;
        	case (Node.TEXT_NODE):
        	    String nodeValue = node.getNodeValue().trim();
        	    if (nodeValue != null && nodeValue.length() > 0) {
        	        element.addTextNode(nodeValue);
        	    }
    	    	break;
        	default:
        	    if (getLogger().isDebugEnabled())
        	        getLogger().logDebug("Unknown node:  Type=" + node.getNodeType() + ", Name = " + node.getNodeName() + " Value=" + node.getNodeValue());
        }
	}
	
}
