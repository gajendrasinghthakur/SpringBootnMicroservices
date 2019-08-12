package com.csc.fsg.nba.utility;
/*
 * ******************************************************************************* <BR> 
 * This program contains trade secrets and confidential
 * information which <BR> are proprietary to CSC Financial Services Group�. The use, <BR> reproduction, distribution or disclosure of this program, in
 * whole or in <BR> part, without the express written permission of CSC Financial Services <BR> Group is prohibited. This program is also an
 * unpublished work protected <BR> under the copyright laws of the United States of America and other <BR> countries. If this program becomes
 * published, the following notice shall <BR> apply: Property of Computer Sciences Corporation. <BR> Confidential. Not for publication. <BR> Copyright
 * (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved. <BR>
 * ******************************************************************************* <BR>
 */

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.vo.NbaConfiguration;

public class XmlXsdValidator {

	StringBuffer responseString = new StringBuffer();
	private int errCounter;
	private int fatalErrCounter;
	
	/**
	 * This method validates the incoming XML using XSD 
	 * @param xmlString an XML
	 * @return StringBuffer
	*/
	public StringBuffer validateSchema(String xmlString) {
		try {
			String schemaPath = NbaConfiguration.getInstance().getFileLocation(NbaConfigurationConstants.ACORDDTD);//NBALXA-2077
			responseString.append("\n<b>XML TO XSD VALIDATIONS </b>\n\n");
			responseString.append("<b>"); //APSL4508
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(true);
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", schemaPath); //xsd path //APSL4508 //NBALXA-2077
			DocumentBuilder builder = factory.newDocumentBuilder();
			Validator handler = new Validator();
			builder.setErrorHandler(handler);
			builder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));

		} catch (java.io.IOException ioe) {
			System.out.println("IOException " + ioe.getMessage());
		} catch (SAXException e) {
			System.out.println("SAXException" + e.getMessage());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			System.out.println("ParserConfigurationException" + e.getMessage());
		} catch (NbaBaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseString.append("</b>"); //APSL4508
	}

	private class Validator extends DefaultHandler {

		public boolean validationError = false;
		public SAXParseException saxParseException = null;

		public void error(SAXParseException exception) throws SAXException {
			validationError = true;
			saxParseException = exception;
			responseString.append("Errors(" + errCounter + ") : " + saxParseException.getMessage());
			responseString.append("\n");
			errCounter++;
		}
		public void fatalError(SAXParseException exception) throws SAXException {
			validationError = true;
			saxParseException = exception;
			responseString.append("Fatal Errors(" + fatalErrCounter + ") : " + saxParseException.getMessage());
			responseString.append("\n");
			fatalErrCounter++;
		}
		public void warning(SAXParseException exception) throws SAXException {
			responseString.append("Warning(" + ") : " + exception.getMessage());
			responseString.append("\n");
		}
	}
}
