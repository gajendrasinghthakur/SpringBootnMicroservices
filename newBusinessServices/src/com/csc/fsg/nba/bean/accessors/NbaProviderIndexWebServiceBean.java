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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.ejb.SessionBean;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.configuration.Provider;
import com.csc.fsg.nba.vo.configuration.RequirementResult;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.tbf.xml.XmlValidationError;

/**
 * This is a stateless Session Bean that is used for submitting a new 
 * application request for a policy in NBA. It uses XPathAPI to check the
 * existence of a policy number in the incoming XML. If a policy number 
 * is not attached with the XML with the request, then it retrieves a 
 * policy number nbA system and assigns it to the XML. Then 
 * it creates a file in the AWD_RIP folder and creates the response using the Xerces API.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 1</td><td>Provider Interfaces</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaProviderIndexWebServiceBean implements SessionBean {
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
	public void ejbCreate() throws javax.ejb.CreateException {
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
	public Element submitRequirementResult(Element ele) {
		String GUID = null;
		NbaTXLife txLife = null;
		try {
			// Intercept the incoming requirement result
			StringBuffer txLifeString = null;
			String namespaceStr = null;
			try {
				String txLifeStr = DOM2String(ele.getOwnerDocument());
	            getLogger().logDebug("NbaProviderIndexWebService - Submitted requirement result: " + txLifeStr);
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
		    		getLogger().logDebug("ProviderIndexWebService Removed namespace prefix: " + txLifeString.toString());
				}
	            // Remove the EIB headers and trailers
	            txLifeString.delete(0, txLifeString.indexOf("<TXLife"));
	            txLifeString.delete(txLifeString.toString().indexOf("</TXLife>") + 9, txLifeString.length());
	            //Remove the namespace definition from the result xml
	            String start="<TXLife";
	    		String end=">";
	    		namespaceStr = substringBetween(txLifeString.toString(),start,end);
	    		int length=namespaceStr.length();
	    		txLifeString = new StringBuffer(start + txLifeString.substring(start.length()+length).trim());
	    		getLogger().logDebug("ProviderIndexWebService Revised Request: " + txLifeString.toString());
			} catch (Exception exp) {
				getLogger().logException(exp);
				Vector errors = new Vector();
				errors.add(buildExceptionMessage(exp));
				return createResponse(ele, errors);
			}
			
            //Retrieve the VendorCode from the requirement result
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(txLifeString.toString())));
			String vendorCode = getVendorCode(doc);

            //Call the VPMS model to get the translation of VendorCode
            NbaVpmsResultsData vpmsResultsData = getDataFromVpms(NbaVpmsAdaptor.REQUIREMENTS, NbaVpmsAdaptor.EP_GET_PROVIDER_FOR_RESULT, vendorCode);
            String providerID = (String) vpmsResultsData.getResultsData().get(0);
            
            // Get the ACORD version from the requirement result
            String version = getVersionFromNamespace(namespaceStr, providerID);
            if (version == null) {
				version = getVersionFromTxLife(doc);
            }

            // Find the XSL transform file, if one exists
			File xslFile = getXslFile(providerID, version);
			if (xslFile == null) {
				throw new NbaBaseException("XSL Stylesheet not found!");
			}

			// Transform the TXLife from the provider's ACORD version into nbA's
            TransformerFactory factory = TransformerFactory.newInstance();
            Source xsl = new StreamSource(xslFile);
            Transformer x = factory.newTransformer(xsl);
            x.setParameter("CurrentDate", NbaUtils.getCurrentDateForXSL()); //Code Formatted
            x.setParameter("BackEndSystem", NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_INTEGRATION));
            x.setParameter("CSCVendorCode", NbaOliConstants.CSC_VENDOR_CODE);
            
            BufferedReader reader = new BufferedReader(new StringReader(txLifeString.toString()));
            
            Source source = new StreamSource(reader);
            StringWriter outputStream = new StringWriter();
            Result target = new javax.xml.transform.stream.StreamResult(outputStream);
            x.transform(source,target);
            getLogger().logDebug("NbaProviderIndexWebService Transformed Result: " + outputStream.toString());
            
            txLife = new NbaTXLife(outputStream.toString());

            Vector errors = null;
            if (NbaConfiguration.getInstance().isWebServiceDTDValidationON("NBAPROVIDERINDEXWEBSERVICE")) {
                errors = txLife.getTXLife().getValidationErrors(false);
            }
            if (errors != null && errors.size() > 0) {
                return createResponse(ele, errors);
            }
			String path = null;
			try {
				path = NbaConfiguration.getInstance().getFileLocation(providerID + "Rip");
			} catch (Exception exp) {
				getLogger().logException(exp);
				errors = new Vector();
				errors.add(buildExceptionMessage(exp));
				return createResponse(ele, errors);
			}
			// Deliver the transformed provider requirement result to DocumentInput
			GUID = getNodeValue(ele, "TransRefGUID");
			SimpleDateFormat GUID_SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String datetime = GUID_SDF.format(new Date());
			File xmlFile = new File(path, GUID + ".xml");
			if (xmlFile.exists()) {
				xmlFile = new File(path, GUID + "_" + datetime + ".xml");
			}
			getLogger().logDebug("NbaProviderIndexWebService Writing to file: " + xmlFile.getPath());
			OutputStream fileOut = new FileOutputStream(xmlFile);
			fileOut.write(outputStream.toString().getBytes());
			fileOut.close();
			reader.close(); //ALII959
			return createResponse(ele, null);
		} catch (Exception exp) {
			getLogger().logException(exp);
			Vector errors = new Vector();
			errors.add(buildExceptionMessage(exp));
			return createResponse(ele, errors);
		}
	}
	/**
	 * Receives a document and converts it into a String
	 * @param doc Document which contains the element to be converted into String
	 * @return String 
	 */
	protected static String DOM2String(Document doc) throws java.io.IOException {
		StringWriter sw = new StringWriter();
		OutputFormat oFormatter = new OutputFormat("XML", null, false);
		XMLSerializer oSerializer = new XMLSerializer(sw, oFormatter);
		oSerializer.serialize(doc);
		return sw.toString();
	}
	/**
	 * Uses the XPathAPI to retrieve the value of a node from the root node.
	 * @param root Node that is the root node 
	 * @param pattern String that defines the pattern which has to be traversed using XPathAPI
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
			transExeDate.appendChild(xmlDoc.createTextNode(getNodeValue(ele, "TransExeDate")));
			transExeTime.appendChild(xmlDoc.createTextNode(getNodeValue(ele, "TransExeTime")));
			transMode.setAttribute("tc", getAttributeValue(ele, "TransMode", "tc"));
			transMode.appendChild(xmlDoc.createTextNode(getNodeValue(ele, "TransMode")));
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
	 * @param  rawDate  the date/time in Java internal format.
	 * @return String   the date as a properly formatted String.
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
		String formattedTime =
			dateFormat.format(rawTime)
				+ hoursFormat.format(zoneOffset / 3600000)
				+ ":"
				+ minutesFormat.format((zoneOffset / 60000) % 60);
		return formattedTime;
	}
	/**
	 * Builds the map for retrieving the table values.
	 * Since there is no case at this time, so cannot use 
	 * @param company the company code
	 * @param coverageKey the coverage key
	 * @return <code>Map</code> contains fields and values for retrieving data
	 * @exception NbaBaseException If errors occur while trying to create the HashMap. This
	 *                             exception is thrown for <code>InvocationTargetException</code>, 
	 *                             </code>IllegalArgumentException</code> and <code>IllegalAccessException</code>. 
	 */
	protected Map setupTableMap(String company, String coverageKey) throws NbaBaseException {
		NbaTableAccessor nbaTableAccessor = new NbaTableAccessor();
		Map myCaseMap = nbaTableAccessor.createDefaultHashMap("*");
		if (company != null) {
			myCaseMap.put(NbaTableAccessConstants.C_COMPANY_CODE, company);
		}
		if (coverageKey != null) {
			myCaseMap.put(NbaTableAccessConstants.C_COVERAGE_KEY, coverageKey);
		}
		return myCaseMap;
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaProviderIndexWebServiceBean.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaProviderIndexWebServiceBean could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
    /**
     * Create and initialize an <code>NbaVpmsAutoUnderwritingData</code>
     * object to find matching work items.
     * @param entryPoint the VP/MS model's entry point
     * @return NbaVpmsAutoUnderwritingData the VP/MS results
     * @throws NbaBaseException
     */
    protected NbaVpmsResultsData getDataFromVpms(String model, String entryPoint, String vendorCode) throws NbaBaseException {
    	NbaVpmsAdaptor vpmsAdaptor = null;
        try {
            NbaOinkRequest oinkRequest = new NbaOinkRequest();
            NbaOinkDataAccess oinkData = new NbaOinkDataAccess(new NbaLob());
            Map skipMap = new HashMap();
            skipMap.put("A_XmlVendorCode", vendorCode);
            vpmsAdaptor = new NbaVpmsAdaptor(oinkData, model);
            vpmsAdaptor.setVpmsEntryPoint(entryPoint);
            vpmsAdaptor.setANbaOinkRequest(oinkRequest);
            vpmsAdaptor.setSkipAttributesMap(skipMap);
            return new NbaVpmsResultsData(vpmsAdaptor.getResults());
        } catch (java.rmi.RemoteException re) {
            throw new NbaBaseException("Requirement provider problem", re);
  		} finally {
            try {
                if (vpmsAdaptor != null) {
                    vpmsAdaptor.remove();
                }
            } catch (Throwable th) {
            		getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
            }
  		}
    }   
    
    /**
     * Retrieve Vendor code from the input xml 1122
     * Get the vendor code attribute from VendorName element.
     * @param doc
     * @return string
     * @throws NbaBaseException
     */
    private String getVendorCode(Document doc) throws NbaBaseException {
        NodeList nodeList = doc.getElementsByTagName("VendorName");
        if(nodeList==null)
        	throw new NbaBaseException("Vendor code is not found/matches! Please verify the vendor code");
        String vendorCode = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element vendorElement = (Element) nodeList.item(i);
            vendorCode = vendorElement.getAttribute("VendorCode");
        }
        return vendorCode;
    }
    /**
     * Retrieve Version from the input Txml 1122
     * Get the version attribute value from OLife element.
     * @param doc
     * @return string
     * @throws NbaBaseException
     */
    private String getVersionFromTxLife(Document doc) throws NbaBaseException{
        NodeList nodeList = doc.getElementsByTagName("OLifE");
        if(nodeList != null){
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element vendorElement = (Element) nodeList.item(i);
        		return vendorElement.getAttribute("Version");
        	}
    	}
        throw new NbaBaseException("TxLife version is not found/matched! Please verify the version");
    }
    /**
     * Get the cached xsl file.
     * @param providerID
     * @param version
     * @return xsl file
     * @throws NbaBaseException
     */
    private File getXslFile(String providerID, String version) throws Exception {
        String xslFileName = NbaUtils.loadTransformationXSL(providerID, NbaUtils.XSL_REQUIREMENT_RESULT, version);
        if (xslFileName != null && xslFileName.length() > 0) {
            return new File(xslFileName);
        }
        return null;
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
    
    /**
     * Retrieve acord verion of result xml from the namespace definition
     * @param namespace
     * @param providerID
     * @return
     * @throws NbaBaseException
     */
    protected String getVersionFromNamespace(String namespace, String providerID )throws NbaBaseException{
        Provider provider = NbaConfiguration.getInstance().getProvider(providerID);
        List xslResults = provider.getXsltStyleSheet().getRequirementResult();
        for (int i = 0; i < xslResults.size(); i++) {
            RequirementResult result = (RequirementResult) xslResults.get(i);
            if (null != result.getVersion() && namespace.indexOf(result.getVersion())>0) {
                return result.getVersion();
            }
        }
    	return null;
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
