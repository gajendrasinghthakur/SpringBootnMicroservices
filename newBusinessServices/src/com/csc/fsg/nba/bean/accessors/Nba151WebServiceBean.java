package com.csc.fsg.nba.bean.accessors;


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
 *     Copyright (c) 2002-2013 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */


import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.tbf.xml.XmlValidationError;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SR564247</td><td>Discretionary</td><td>Predictive Full Implementation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator
 */
public class Nba151WebServiceBean {
	protected static NbaLogger logger = null;
	private javax.ejb.SessionContext mySessionCtx;
	/**
	 * getSessionContext
	 */
	public javax.ejb.SessionContext getSessionContext() {
		return mySessionCtx;
	}
	/**
	 * setSessionContext
	 */
	public void setSessionContext(javax.ejb.SessionContext ctx) {
		mySessionCtx = ctx;
	}
	/**
	 * ejbCreate
	 */
	public void ejbCreate() {//throws javax.ejb.CreateException {
	}
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {
	}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {
	}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() {
	}

	/**
	 * Submit a TXLife response containing a requirement result. 
	 * @param ele Element with the requirement result
	 * @return Element Formatted TXLife response
	 */
	public Element submitJetResult(Element ele) {
		String GUID = null;
		try {
			// Intercept the incoming requirement result
			StringBuffer txLifeString = null;
			String namespaceStr = null;
			String txLifeStr = DOM2String(ele);
			StringBuffer txLifeVal = null;
			try {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Nba151WebServiceBean - Submitted 151 result: " + txLifeStr);
				}
				txLifeString = new StringBuffer(txLifeStr);
				// Check for namespace prefixes
				if (txLifeStr.indexOf("<TXLife") == -1) {
					String prefix = "";
					for (int i = txLifeStr.indexOf("TXLife"); i > 0; i--) {
						if (txLifeString.charAt(i) == '<')
							break;
						prefix = txLifeStr.substring(i, txLifeStr.indexOf("TXLife"));
					}
					if (prefix.length() > 0) {
						txLifeString = new StringBuffer(txLifeString.toString().replaceAll("<" + prefix, "<"));
						txLifeString = new StringBuffer(txLifeString.toString().replaceAll("</" + prefix, "</"));
					}
					if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Nba151WebServiceBean Removed namespace prefix: " + txLifeString.toString());
					}
				}
				// Remove the EIB headers and trailers
				txLifeString.delete(0, txLifeString.indexOf("<TXLife"));
				txLifeString.delete(txLifeString.toString().indexOf("</TXLife>") + 9, txLifeString.length());
				//Remove the namespace definition from the result xml
				String start = "<TXLife";
				String end = ">";
				namespaceStr = substringBetween(txLifeString.toString(), start, end);
				int length = namespaceStr.length();
				txLifeVal = new StringBuffer(start + txLifeString.substring(start.length() + length).trim());
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Nba151WebServiceBean Revised Request: " + txLifeString.toString());
				}
			} catch (Exception exp) {
				getLogger().logException(exp);
				Vector errors = new Vector();
				errors.add(buildExceptionMessage(exp));
				return createResponse(ele, errors);
			}

			Vector errors = null;
			errors = validateRequest(txLifeVal.toString());
			if (errors != null && errors.size() > 0) {
				return createResponse(ele, errors);
			}
			String path = null;
			try {
				path = NbaConfiguration.getInstance().getFileLocation("predictive");
			} catch (Exception exp) {
				getLogger().logException(exp);
				errors = new Vector();
				errors.add(buildExceptionMessage(exp));
				return createResponse(ele, errors);
			}
			// Deliver the transformed provider requirement result to DocumentInput
			GUID = getNodeValue(ele, "TransRefGUID");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(txLifeString.toString())));
			TransformerFactory tranFactory = TransformerFactory.newInstance();
			Transformer aTransformer = tranFactory.newTransformer();
			Source src = new DOMSource(document);
			StreamResult dest = new StreamResult(new File(path + GUID + ".xml"));
			aTransformer.transform(src, dest);
			return createResponse(ele, null);
		} catch (Exception exp) {
			getLogger().logException(exp);
			Vector errors = new Vector();
			errors.add(buildExceptionMessage(exp));
			return createResponse(ele, errors);
		}
	}
	
	/**
	 * Uses the XPathAPI to retrieve the value of a node from the root node.
	 * 
	 * @param root
	 *            Node that is the root node
	 * @param pattern
	 *            String that defines the pattern which has to be traversed using XPathAPI
	 * @return Node containing the node
	 */
	protected static Node getNode(Node root, String pattern) throws Exception {
		return XPathAPI.selectSingleNode(root, pattern);
	}
	/**
	 * Uses the Xerces API create a response.
	 * @param ele Element that is the incoming XML
	 * @param txLife NbaTXLife that is the formatted incoming XML
	 * @param errors Vector containing error messages encountered while processing the incoming XML
	 * @return Element containing the response	 
	 */
	protected Element createResponse(Element ele, Vector errors) {
		Document xmlDoc = null;
		Element response = null;
		try {
			//Create an XML Document
			DocumentBuilderFactory dbFactory = DocumentBuilderFactoryImpl.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			xmlDoc = docBuilder.newDocument();
			response = xmlDoc.createElement("TXLife");
			Element userAuthResponse = xmlDoc.createElement("UserAuthResponse");
			Element txLifeResponse = xmlDoc.createElement("TXLifeResponse");
			response.appendChild(userAuthResponse);
			response.appendChild(txLifeResponse);
			//Add code for UserAuthResponse contents
			Element svrDate = xmlDoc.createElement("svrDate");
			Element svrTime = xmlDoc.createElement("svrTime");
			userAuthResponse.appendChild(svrDate);
			userAuthResponse.appendChild(svrTime);
			//Set the values in server date and server time
			// Build SvrDate
			Date today = new Date();
			svrDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			// Build SvrTime
			svrTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			//Add code for TXLifeResponse contents
			//Create first level element for TXLifeResponse
			Element transRefGUID = xmlDoc.createElement("TransRefGUID");
			Element transType = xmlDoc.createElement("TransType");
			Element transExeDate = xmlDoc.createElement("TransExeDate");
			Element transExeTime = xmlDoc.createElement("TransExeTime");
			Element transMode = xmlDoc.createElement("TransMode");
			Element oLifE = xmlDoc.createElement("OLifE");
			// create and set the value for result code
			Element transResult = xmlDoc.createElement("TransResult");
			Element resultCode = xmlDoc.createElement("ResultCode");
			//Add first level element for TXLifeResponse
			txLifeResponse.appendChild(transRefGUID);
			txLifeResponse.appendChild(transType);
			txLifeResponse.appendChild(transExeDate);
			txLifeResponse.appendChild(transExeTime);
			txLifeResponse.appendChild(transMode);
			txLifeResponse.appendChild(transResult);
			txLifeResponse.appendChild(oLifE);
			transResult.appendChild(resultCode);
			//set the values for trans ref guid, trans type, trans exe date, trans exe time and trans mode
			transRefGUID.appendChild(xmlDoc.createTextNode(getNodeValue(ele, "TransRefGUID")));
			transType.setAttribute("tc", getAttributeValue(ele, "TransType", "tc"));
			transType.appendChild(xmlDoc.createTextNode(getNodeValue(ele, "TransType")));
			transExeDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			transExeTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			if (errors != null && errors.size() > 0) {
				resultCode.setAttribute("tc", "5");
				resultCode.appendChild(xmlDoc.createTextNode("Failure"));
				Element resultInfo;
				Element resultInfoCode;
				Element resultInfoDesc;
				XmlValidationError xmlError;
				for (int i = 0; i < errors.size(); i++) {
					resultInfo = xmlDoc.createElement("ResultInfo");
					transResult.appendChild(resultInfo);
					resultInfoCode = xmlDoc.createElement("ResultInfoCode");
					resultInfoDesc = xmlDoc.createElement("ResultInfoDesc");
					resultInfoCode.setAttribute("tc", "200");
					resultInfoCode.appendChild(xmlDoc.createTextNode("General Data Error"));
					if (errors.get(i) instanceof XmlValidationError) {
						xmlError = (XmlValidationError) errors.get(i);
						resultInfoDesc.appendChild(xmlDoc.createTextNode("DTD validation error : " + xmlError.getErrorMessage()));
					} else if (errors.get(i) instanceof String) {
						resultInfoDesc.appendChild(xmlDoc.createTextNode((String) errors.get(i)));
					}
					resultInfo.appendChild(resultInfoCode);
					resultInfo.appendChild(resultInfoDesc);
				}
			} else {
				resultCode.setAttribute("tc", "1");
				resultCode.appendChild(xmlDoc.createTextNode("Success"));
			}
		} catch (Exception e) {
			getLogger().logException(e);
		}
		return response;
	}
	/**
	 * Formats a Date's date for use in the response node.
	 * 
	 * @param rawDate
	 *            the date/time in Java internal format.
	 * @return String the date as a properly formatted String.
	 */
	protected String formatDate(Date rawDate) {
		SimpleDateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate = dateFormat.format(rawDate);
		return formattedDate;
	}
	/**
	 * Formats a Date's time for use in the response node.
	 * @param rawTime   the date/time in Java internal format.
	 * @return String   the time as a properly formatted String.
	 */
	protected String formatTime(Date rawTime) {
		SimpleDateFormat dateFormat;
		dateFormat = new SimpleDateFormat("HH:mm:ss");
		long zoneOffset = (new GregorianCalendar()).get(Calendar.ZONE_OFFSET);
		DecimalFormat hoursFormat = new DecimalFormat("+00;-00");
		DecimalFormat minutesFormat = new DecimalFormat("00");
		String formattedTime = dateFormat.format(rawTime) + hoursFormat.format(zoneOffset / 3600000) + ":"
				+ minutesFormat.format((zoneOffset / 60000) % 60);
		return formattedTime;
	}
	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(Nba151WebServiceBean.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("Nba151WebServiceBean could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}      
   
	protected String getNodeValue(Element ele, String tagName) {
		try {
			NodeList nodes = ele.getElementsByTagName(tagName);
			Node node = nodes.item(0);
			return node.getFirstChild().getNodeValue();
		} catch (Exception e) {
			return "#ERROR#";
		}
	}

    protected String getAttributeValue(Element ele, String tagName, String attrName) {
		try {
			NodeList nodes = ele.getElementsByTagName(tagName);
			Node node = nodes.item(0).getAttributes().getNamedItem(attrName);
			return node.getFirstChild().getNodeValue();
		} catch (Exception e) {
			return "#ERROR#";
		}
	}
    
    /**
	 * Receives a document element and converts it into a String
	 * 
	 * @param doc
	 *            Document which contains the element to be converted into String
	 * @return String the string form of Element recieved as parameter
	 */
	protected static String DOM2String(Element ele) throws java.io.IOException {

		StringWriter sw = new StringWriter();
		OutputFormat oFormatter = new OutputFormat("XML", null, false);

		XMLSerializer oSerializer = new XMLSerializer(sw, oFormatter);
		oSerializer.serialize(ele);
		return sw.toString();
	}
    
	protected Vector validateRequest(String txLife) throws NbaBaseException, Exception {
		Vector errors = null;
		if (txLife != null) {
			if (NbaConfiguration.getInstance().isWebServiceDTDValidationON("nbA151WebService")) {
				NbaTXLife nbaTXLife = new NbaTXLife(txLife);
				Vector vctrErrors = nbaTXLife.getTXLife().getValidationErrors(false);
				if (vctrErrors != null && vctrErrors.size() > 0) {
					errors = new Vector();
					for (int i = 0; i < vctrErrors.size(); i++) {
						XmlValidationError temp = (XmlValidationError) vctrErrors.get(i);
						errors.add(temp.getErrorMessage());
					}
				}
			}
		}
		return errors;
	}
	
		
	/**
     * Retrieve a part from String.
     * @param str
     * @param open
     * @param close
     * @return String
     */
    protected String substringBetween(String str, String open, String close) {
		if (str == null || open == null || close == null) {
			return null;
		}
		int start = str.indexOf(open);
		if (start != -1) {
			int end = str.indexOf(close, start + open.length());
			if (end != -1) {
				return str.substring(start + open.length(), end);
			}
		}
		return null;
	}
    
    protected String buildExceptionMessage(Exception exp) {
		if (exp.getMessage() != null) {
			return exp.getMessage();
		}
		StackTraceElement trace = exp.getStackTrace()[0];
		StringBuffer sb = new StringBuffer();
		sb.append(exp.getClass().getName());
		sb.append(" ");
		sb.append(trace.toString());
		return sb.toString();
	}
}
