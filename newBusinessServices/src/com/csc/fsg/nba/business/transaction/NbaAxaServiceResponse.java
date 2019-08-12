package com.csc.fsg.nba.business.transaction;

/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.webservice.client.NbaAxaServiceRequestor;


/**
 * NbaAxaServiceResponse encapsulates accessing all AXA service responses.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.68</td><td>AXA Life Phase 1</td><td>LDAP Interface</td></tr>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal Correspondence</td></tr> 
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 6.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaAxaServiceResponse extends NbaBusinessTransactions{
	private static String className = NbaAxaServiceResponse.class.getName();
	protected Map responseMap = new HashMap();
	protected boolean controllerException = false;
	protected SOAPEnvelope responseEnvelope = null;
	protected static NbaLogger logger = null;
    
	public static final String FILE_RESPONSE = "FILE";
	public static final String STRING_RESPONSE = "STRING";

	public static final String SUCCESS = "Success";

	public static final String RESPONSE_NAME = "SOAPResponseName";
	public static final String SSO_RESPONSE_ELEMENT = "processSSORequestResponse";
	public static final String CONTROLLER_RESPONSE_ELEMENT = "processRASRequestResponse";
	
	public static final String PAIR_ELEMENT = "Pair";
	public static final String PAIRNAME_ELEMENT = "name";
	public static final String PAIRSTATUS_ELEMENT = "status";
	public static final String PAIRVALUE_ELEMENT = "value";
	public static final String USERID_ELEMENT = "uid";
	public static final String USERFULLNAME_ELEMENT = "cn";
	public static final String TOKEN_ELEMENT = "token";
	public static final String ERRORCODE_ELEMENT = "errorcode";
	public static final String ERRORMSG_ELEMENT = "errormsg";
	public static final String STATUS_ELEMENT = "status";
	public static final String UDDI_ADDRESS_ELEMENT = "accessPoint";
	public static final String NBATXLIFE_ELEMENT = "NbaTXLife";
	public static final String BINARYSECURITYTOKEN_ELEMENT = "BinarySecurityToken";
	public static final String TIMESTAMP_ELEMENT = "timestamp";
	public static final String PASSWORD_ELEMENT = "userpassword";	
	public static final String URL_ELEMENT = "url";	
	public static final String UPLOAD_FOLDER_ELEMENT = "uploadfolder";	
	public static final String ERRORMSG_CHILD = "errorMsg";
	public static final String ERRORCODE_CHILD = "errorCode";
	public static final String CONTROLLER_EXCEPTION = "AXAControllerException";
	public static final String ROLE_ASSIGNEES = "roleassignees";
	public static final String NBA_PROJECT = "AXFLifenbA"; 
	public static final String INCLUSIVE_RIGHTS = "inclusiverightassignees"; //NBA186
	public static final String INCLUSIVE_PREPEND = "AXFLifenbA_"; //NBA186
	public static final String XPRESSION_GETCATEGORIES_RESPONSE= "getListOfCategoriesResponse"; //AXAL3.7.13I
	public static final String XPRESSION_GETDOCUMENTS_RESPONSE= "getListOfDocumentsResponse";	//AXAL3.7.13I
	public static final String XPRESSION_GETVARIABLES_RESPONSE= "getDocumentVariablesResponse";	//AXAL3.7.13I
	public static final String XPRESSION_PREVIEW_PDF_RESPONSE= "requestDocumentsWithDataResponse";		//AXAL3.7.13I
	public static final String EIB_ERROR_SCHEMA = "http://www.axa-equitable.com/schemas/EIB_error";	//AXAL3.7.31
	public static final String EIB_ERRORCODE_ELEMENT = "Error_code";
	public static final String EIB_ERRORMSG_ELEMENT = "Error_text";
	public static final String EIB_ERRORTYPE_ELEMENT ="Error_type";//AXAL3.7.54
	public static final String ERRORTYPE_ELEMENT = "errortype";//AXAL3.7.54
	public static final String REINSURANCE_RESPONSE = "ReinsuranceResponse";//AXAL3.7.32
	public static final String EAI_PROJECT ="EAI"; //APSL4342
	
	
	/**
	 * Constructor
	 */
	public NbaAxaServiceResponse(String responseType, String aString) throws NbaBaseException {
		if (responseType == null || aString == null || (responseType != FILE_RESPONSE && responseType != STRING_RESPONSE) || aString.length() < 0) {
			throw new NbaBaseException(NbaBaseException.INVALID_RESPONSE);
		}
		try {
			String responseString = aString;
			if (responseType.equalsIgnoreCase(FILE_RESPONSE)) {
				responseString = readResponseFile(aString);
				//Begin NBLXA-1416
				if (NbaUtils.isNbaOffline()) {
					InputStream is = new FileInputStream(new File(aString));
					SOAPMessage resp = MessageFactory.newInstance().createMessage(null, is);
					try {
						responseString = resp.getSOAPPart().getEnvelope().toString();
					} catch (Exception ex) {
						
					}
				}
				//End NBLXA-1416
			}
			if (getLogger().isDebugEnabled())
				getLogger().logDebug("Processing response from service...");
			responseString = responseString.replaceAll("\t", "").trim();
			if (responseString.indexOf(EIB_ERROR_SCHEMA) >= 0) {
				processResponse(responseString);//AXAL3.7.54
			} else if (responseString.indexOf("<TXLife") >= 0 || responseString.indexOf("TXLife") >= 0) {
				String responseBody = responseString.replaceAll("\t", "").trim();
				if (responseBody.indexOf(":Body>") > -1) {
					responseBody = responseBody.substring(responseBody.indexOf(":Body>") + ":Body>".length());
					responseBody = responseBody.substring(0, responseBody.indexOf(":Body"));
					responseBody = responseBody.substring(0, responseBody.lastIndexOf("<"));
				}
				NbaTXLife txLife = new NbaTXLife(responseBody);
				responseMap.put(NBATXLIFE_ELEMENT, txLife);
			} else if (responseString.indexOf(CONTROLLER_EXCEPTION) >= 0) {
				this.setControllerException(true);
				processControllerException(responseString);
			} else if (responseString.indexOf("<TransResult") >= 0 || responseString.indexOf("TransResult") >= 0) {
				String responseBody = responseString.replaceAll("\t", "").trim();
				if (responseBody.indexOf(":Body>") > -1) {
					responseBody = responseBody.substring(responseBody.indexOf(":Body>") + ":Body>".length());
					responseBody = responseBody.substring(0, responseBody.indexOf(":Body"));
					responseBody = responseBody.substring(0, responseBody.lastIndexOf("<"));
				}
				responseMap.put(REINSURANCE_RESPONSE, responseBody);
			} else {
				Document document = createDocument(responseString);
				Element element = document.getDocumentElement();
				buildResponseMap(element);
				validateResponse();
			}
			if (getLogger().isDebugEnabled())
				getLogger().logDebug("The service response has been processed.");
		} catch (NbaBaseException e) {
			getLogger().logException(e);
			processResponse(aString);//AXAL3.7.54
		}  catch (Exception e) {
			getLogger().logException(e);
			throw new NbaBaseException(NbaBaseException.INVALID_RESPONSE + e.getMessage());
		}
	}
	/**
	 * Get the response's values.
	 * @return Map A key-value representation of the response.
	 */
	public Map getResponseMap() {
	    return responseMap;
	}
    /**
	 * Read a file's contents into a String for processing.
	 * @param fileName The name of the file to read.
	 * @return String The contents of the response file.
	 * @throws NbaBaseException
	 */
	protected String readResponseFile(String fileName) throws NbaBaseException {
		StringBuffer sb = new StringBuffer();
		String line = null;
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			while (true) {
				line = br.readLine();
				if (line == null) {
					break;
				} else {
					line = line.replaceAll("\t", "").trim();
					sb.append(line);
				}
			}
			br.close();
			return sb.toString();
		} catch (FileNotFoundException e) {
			throw new NbaBaseException("Stubbed Response: " + fileName + "not found");
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.INVALID_RESPONSE);
		}
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return the logger implementation
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(className);
			} catch (Exception e) {
				NbaBootLogger.log(className + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Create a document representing the SOAP response. 
	 * @param String The SOAP response as a string
	 * @return Document The document populated from the response
	 */
	protected Document createDocument(String soapResponse) throws NbaBaseException {
	    try {
	        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	        documentBuilderFactory.setNamespaceAware(true);
	        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	        return documentBuilder.parse(new InputSource(new StringReader(soapResponse)));
        } catch (Exception e) {
            throw new NbaBaseException("An error occurred while parsing the XML stream: " + e.getMessage());
        }
	}
	protected static Node getNode(Node root, String pattern) throws Exception {
	    XObject list = XPathAPI.eval(root, pattern);
        NodeIterator nl =list.nodeset();
        return nl.nextNode();
	}
	/**
	 * Populate the response map object with the values present on the SOAP response.
	 * Throws an exception if any required response fields are missing.
	 * @param Element The root document element built from the SOAP response.
	 * @throws Exception
	 */
	protected void buildResponseMap(Element element) throws Exception {
	    Vector v = new Vector();
	    buildVectorOfResponses(element, v);
        for (int i = 0; (i + 1) < v.size(); i+=2) {
            getResponseMap().put(v.get(i), v.get(i+1));
        }
	}
	/**
	 * Build a vector containing the key-value pairs present on a particular element of the response.  
	 * @param element The response element to examine.
	 * @param v A vector in which to accumulate the responses.
	 */
	protected void buildVectorOfResponses(Element element, Vector v) {
		NodeList nodeList = element.getChildNodes();
		String vectorItem = null;
        if (element.getLocalName().equalsIgnoreCase("BODY")) {
            // Retain the name of the response element for later validation
		    v.add(RESPONSE_NAME);
        	v.add(element.getFirstChild().getLocalName());
        }

	    for (int i = 0; i < nodeList.getLength(); i++) {
	        processNode(nodeList.item(i), v);
	    }
	}
	/**
	 * Processes a component of the response.
	 * @param node The component of the response to process.
	 * @param v A vector in which to accumulate responses.
	 */
	protected void processNode(Node node, Vector v) {
        switch (node.getNodeType()) {
        	case (Node.ELEMENT_NODE):
        	    if (getLogger().isDebugEnabled())
        	        getLogger().logDebug("Element " + node.getNodeName());
        	    Vector nodeVector = new Vector();
        	    buildVectorOfResponses((Element)node, nodeVector);
        	    if (node.getLocalName().equalsIgnoreCase(PAIR_ELEMENT)) {
        	        String pairName = null;
        	        String pairValue = null;
        	        String vectorItem = null;
        	        for (int i = 0; i < nodeVector.size(); i+=2) {
        	            vectorItem = (String)nodeVector.get(i);
        	            if (vectorItem.equalsIgnoreCase(PAIRNAME_ELEMENT)) {
            	            pairName = (String)nodeVector.get(i + 1);
        	            } else if (vectorItem.equalsIgnoreCase(PAIRVALUE_ELEMENT)) {
            	            pairValue = (String)nodeVector.get(i + 1);
        	            }
        	        }
        	       pairValue = validatePairValue(pairName,pairValue);
        	       //APSL4342 Begins
        	       if(pairValue!=null && pairValue.equalsIgnoreCase(NbaConstants.NBA_LIFE_CASE_VIEW)){
        	    	   pairName = "SPECIALRIGHT";
        	       }
        	       //APSL4342 Ends
    	            if (pairName != null && pairValue != null) {
            	        v.add(pairName);
            	        v.add(pairValue);
    	            }
        	    } else if (nodeVector.size() > 0){
        	        v.addAll(nodeVector);
        	    }
        		break;
        	case (Node.TEXT_NODE):
        	    String nodeValue = node.getNodeValue();
        	    if (nodeValue != null && nodeValue.length() > 0) {
            	    if (getLogger().isDebugEnabled())
            	        getLogger().logDebug("Text " + node.getNodeValue());
        	        v.add(node.getParentNode().getLocalName());
        	        v.add(nodeValue.trim());
        	    }
    	    	break;
        }
	}
	/**
	 * Examines the processed response for errors. 
	 * @throws NbaBaseException
	 */
	protected void validateResponse() throws NbaBaseException {
		Vector errors = new Vector();
		String responseName = null;
		if (!getResponseMap().containsKey(RESPONSE_NAME)) {
		    errors.add("Unable to process response");
		} else {
		    responseName = (String)getResponseMap().get(RESPONSE_NAME);
		    if (responseName.equalsIgnoreCase(SSO_RESPONSE_ELEMENT)) {
		        errors.addAll(validateSingleSignOnResponse());
		    } else if (responseName.equalsIgnoreCase(CONTROLLER_RESPONSE_ELEMENT)) {
		        errors.addAll(validateControllerResponse());
		    } else if (responseName.equalsIgnoreCase(XPRESSION_GETCATEGORIES_RESPONSE)) {   //AXAL3.7.13I
		        //errors.addAll(validateControllerResponse());
		    } else if (responseName.equalsIgnoreCase(XPRESSION_GETDOCUMENTS_RESPONSE)) {	//AXAL3.7.13I
		        //errors.addAll(validateControllerResponse());
		    } else if (responseName.equalsIgnoreCase(XPRESSION_GETVARIABLES_RESPONSE)) {	//AXAL3.7.13I
		        //errors.addAll(validateControllerResponse());
		    } else if (responseName.equalsIgnoreCase(XPRESSION_PREVIEW_PDF_RESPONSE)) {		//AXAL3.7.13I
		        //errors.addAll(validateControllerResponse());
		    } else {
		        errors.add("Unrecognized response (" + responseName + ")");
		    }
		}
	    if (errors.size() > 0) {
            throw new NbaBaseException("An error occurred while parsing the XML stream: " + errors.toString());
	    }
	}
	/**
	 * Validates a Single Sign On response.
	 * @return A vector containing the errors.
	 */
	protected Vector validateSingleSignOnResponse() {
		Vector errors = new Vector();
	    if (!getResponseMap().containsKey(STATUS_ELEMENT)) {
            errors.add(STATUS_ELEMENT + " is missing");
    	} else {
    	    String status = (String)getResponseMap().get(STATUS_ELEMENT);
    	    if (status.equalsIgnoreCase(SUCCESS)) {
    	        if (!getResponseMap().containsKey(USERID_ELEMENT)) {
    		        errors.add(USERID_ELEMENT + " is missing");
    	        }
    	        if (!getResponseMap().containsKey(TOKEN_ELEMENT)) {
    		        errors.add(TOKEN_ELEMENT + " is missing");
    	        }
    	    } else {
    	        if (!getResponseMap().containsKey(ERRORCODE_ELEMENT)) {
    		        errors.add(ERRORCODE_ELEMENT + " is missing");
    	        }
    	        if (!getResponseMap().containsKey(ERRORMSG_ELEMENT)) {
    		        errors.add(ERRORMSG_ELEMENT + " is missing");
    	        }
    	    }
	    }
	    return errors;
	}

	/**
	 * Validates a Controller response.
	 * @return A vector containing the errors.
	 */
	protected Vector validateControllerResponse() {
		Vector errors = new Vector();
		String status = null;
		if (getResponseMap().containsKey(STATUS_ELEMENT)) {
		 status = (String)getResponseMap().get(STATUS_ELEMENT);
		 if (status.equalsIgnoreCase(SUCCESS)) { 
		 	if (getResponseMap().containsKey(BINARYSECURITYTOKEN_ELEMENT)) {
		 		return validateControllerSSOResponse();
		 	} else if (getResponseMap().containsKey(USERID_ELEMENT)) {
		 		return validateControllerSFTPResponse();
		 	} else {
		 		errors.add("The Controller Service response is unrecognized.");
		 	}
		 } else {
	        if (!getResponseMap().containsKey(ERRORCODE_ELEMENT)) {
		        errors.add(ERRORCODE_ELEMENT + " is missing");
	        }
	        if (!getResponseMap().containsKey(ERRORMSG_ELEMENT)) {
		        errors.add(ERRORMSG_ELEMENT + " is missing");
	        }
	    }
		}
	    return errors;
	}
	/**
	 * Validates a Controller SSO response.
	 * @return A vector containing the errors.
	 */
	protected Vector validateControllerSSOResponse() {
		Vector errors = new Vector();
		String status = (String)getResponseMap().get(STATUS_ELEMENT);
		if (status.equalsIgnoreCase(SUCCESS)) {
			if (!getResponseMap().containsKey(BINARYSECURITYTOKEN_ELEMENT)) {
				errors.add(BINARYSECURITYTOKEN_ELEMENT + " is missing");
			}
			if (!getResponseMap().containsKey(TOKEN_ELEMENT)) {
				errors.add(TOKEN_ELEMENT + " is missing");
			}
			if (!getResponseMap().containsKey(TIMESTAMP_ELEMENT)) {
				errors.add(TIMESTAMP_ELEMENT + " is missing");
			}
		} else {
	        if (!getResponseMap().containsKey(ERRORCODE_ELEMENT)) {
		        errors.add(ERRORCODE_ELEMENT + " is missing");
	        }
	        if (!getResponseMap().containsKey(ERRORMSG_ELEMENT)) {
		        errors.add(ERRORMSG_ELEMENT + " is missing");
	        }
	    }
	    return errors;
	}
	/**
	 * Validates a Controller SFTP response.
	 * @return A vector containing the errors.
	 */
	protected Vector validateControllerSFTPResponse() {
		Vector errors = new Vector();
        if (!getResponseMap().containsKey(USERID_ELEMENT)) {
            errors.add(USERID_ELEMENT + " is missing");
        }
        if (!getResponseMap().containsKey(PASSWORD_ELEMENT)) {
            errors.add(PASSWORD_ELEMENT + " is missing");
        }
        if (!getResponseMap().containsKey(URL_ELEMENT)) {
            errors.add(URL_ELEMENT + " is missing");
        }
        if (!getResponseMap().containsKey(UPLOAD_FOLDER_ELEMENT)) {
            errors.add(UPLOAD_FOLDER_ELEMENT + " is missing");
        }
	    return errors;
	}
	protected void processControllerException(String aString) {
		com.tbf.xml.XmlParser.setDefaultFeature("http://xml.org/sax/features/validation", false);
		com.tbf.xml.XmlParser parser = new com.tbf.xml.XmlParser();
		parser.setEntityResolver(new com.csc.fsg.nba.foundation.NbaEntityResolver());
		com.tbf.xml.XmlElement xml = parser.parse(new java.io.ByteArrayInputStream(aString.getBytes()));
		responseMap.put(ERRORCODE_ELEMENT, xml.getChild(ERRORCODE_CHILD).getData());
		responseMap.put(ERRORMSG_ELEMENT, xml.getChild(ERRORMSG_CHILD).getData());
	}
	/**
	 * @return Returns the controllerException.
	 */
	public boolean isControllerException() {
		return controllerException;
	}
	/**
	 * @param controllerException The controllerException to set.
	 */
	public void setControllerException(boolean controllerException) {
		this.controllerException = controllerException;
	}
	/**
	 * Determine if we have correct pair/value.  
	 * if correct pair, then determine if correct project
	 * if not, set pairName to null so it's not returned to user credentials.
	 * @param pairName
	 * @param pairValue
	 */
	protected String validatePairValue(String pairName,String pairValue) {
		
		if (ROLE_ASSIGNEES.equalsIgnoreCase(pairName)) {
				pairValue = validateValue(pairValue);
			pairName = null;
		}
		
		if (INCLUSIVE_RIGHTS.equalsIgnoreCase(pairName)) {//NBA186
			pairValue = validateInclusiveRight(pairValue); //NBA186
		} //NBA186
		return pairValue;
	}
	/**
	 * parse pairValue to retrieve correct role
	 * also, determine if we have correct project
	 * @param pairValue
	 * @return boolean
	 */
	protected String validateValue(String pairValue) {
		StringTokenizer st = new StringTokenizer(pairValue, ",");
		String role = null;
		String project = "";
		try {
			String roleString = st.nextToken();
			role = roleString.substring(5);
			String projectString = st.nextToken();
			project = projectString.substring(8);
		} catch (Exception e) {
			//do nothing..SSO didn't return a role
		}
		//if the project is not the nbA project, set role to null
		if (NBA_PROJECT.equalsIgnoreCase(project)) {
			return role;
		}
		
		return null;
	}
	/**
	 * parse pairValue to retrieve correct UW role
	 * also, determine if we have correct project
	 * @param pairValue
	 * @return boolean
	 */
	//NBA186 new method
	protected String validateInclusiveRight(String pairValue) {
		StringTokenizer st = new StringTokenizer(pairValue, ",");
		String uwRole = null;
		String uwRoleTemp = null;
		String project = "";
		try {
			String rightString = st.nextToken();
			uwRoleTemp = rightString.substring(6);
			uwRole = extractRight(uwRoleTemp);
			String projectString = st.nextToken();
			project = projectString.substring(8);
			//APSL4342 Begins
			if(NbaUtils.isBlankOrNull(uwRole) && uwRoleTemp.equalsIgnoreCase(NbaConstants.NBA_LIFE_CASE_VIEW)&& ("EAI").equalsIgnoreCase(project)){
				return uwRoleTemp;
			}
			//APSL4342 Ends
		} catch (Exception e) {
			//do nothing..SSO didn't return a role
		}
		//if the project is not the nbA project, set role to null
		if (NBA_PROJECT.equalsIgnoreCase(project)) {
			return uwRole;
		}
		
		return null;
	}
	//NBA186 new method
	private String extractRight(String tempRight) {
		String project;
		if (tempRight.startsWith(INCLUSIVE_PREPEND)) { //must start with this..
			StringTokenizer st = new StringTokenizer(tempRight, "_");
			project = st.nextToken();
			return st.nextToken(); //right is in 2nd token
		}
		return null;
	}
	
	//deleted processEibException

	//AXAL3.7.54 - New Method
	protected void processResponse(String aString) throws NbaBaseException {
		Document doc = createDocument(aString);
		responseMap.put(ERRORCODE_ELEMENT, getNodeValue(doc, EIB_ERRORCODE_ELEMENT));
		responseMap.put(ERRORMSG_ELEMENT, getNodeValue(doc, EIB_ERRORMSG_ELEMENT));
		responseMap.put(ERRORTYPE_ELEMENT, getNodeValue(doc, EIB_ERRORTYPE_ELEMENT));
	}
	
	//AXAL3.7.54 - New Method
	protected String getNodeValue(Document doc, String name) {
		NodeList nodeList = doc.getElementsByTagName(name);
		if (nodeList != null && nodeList.getLength() > 0) {
			Element elm = (Element) nodeList.item(0);
			if (elm != null && elm.getFirstChild() != null) {
				return "[" + name + "=" + elm.getFirstChild().getNodeValue() + "]";
			}
		}
		return "";
	}

	// NBLXA-2433- New Method
	protected NbaTXLife processMIBFollowUpResponse(String aString) throws NbaBaseException {
		StringBuffer txLifeString = null;
		String namespaceStr = null;
			try{
				String txLifeStr = new String(aString);
				txLifeString =new StringBuffer(aString);
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
				// Remove the namespace definition from the result xml
				String start = "<TXLife";
				String end = ">";
				namespaceStr = substringBetween(txLifeString.toString(), start, end);
				int length = namespaceStr.length();
				txLifeString = new StringBuffer(start + txLifeString.substring(start.length() + length).trim());
			 DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	         Document doc = db.parse(new InputSource(new StringReader(txLifeString.toString())));
	         String version  = getVersionFromTxLife(doc);
	         File xslFile = getXslFile(NbaConfigurationConstants.WEBSERVICE_FUNCTION_PROVIDER_MIB,version);
			if (xslFile == null) {
				throw new NbaBaseException("XSL Stylesheet not found!");
			}
			// Transform the TXLife from the provider's ACORD version into nbA's
			TransformerFactory factory = new org.apache.xalan.processor.TransformerFactoryImpl();
	        Source xsl = new StreamSource(xslFile);
	        Transformer x = factory.newTransformer(xsl);
	        x.setParameter("CurrentDate", NbaUtils.getCurrentDateForXSL()); //Code Formatted
	        x.setParameter("BackEndSystem", NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_INTEGRATION));
	        x.setParameter("CSCVendorCode", NbaOliConstants.CSC_VENDOR_CODE);	        
	        BufferedReader reader = new BufferedReader(new StringReader(txLifeString.toString()));	        
	        Source source = new StreamSource(reader);
	        StringWriter outputStream = new StringWriter();
	        javax.xml.transform.Result target = new javax.xml.transform.stream.StreamResult(outputStream);
	        x.transform(source,target);
	        getLogger().logDebug("NbaProviderIndexWebService Transformed Result: " + outputStream.toString());
	        NbaTXLife txLife = new NbaTXLife(outputStream.toString());
	        return txLife;}
	        catch(Exception e)
	        {
	        	throw new NbaBaseException(e.getMessage());
	        }
		}
	
		// NBLXA-2433- New Method
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
	    
		// NBLXA-2433- New Method
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
		
		// NBLXA-2433- New Method
		public NbaAxaServiceResponse(String responseType, String aString, String providerID) throws NbaBaseException {
			if (responseType == null || aString == null || (responseType != FILE_RESPONSE && responseType != STRING_RESPONSE) || aString.length() < 0) {
				throw new NbaBaseException(NbaBaseException.INVALID_RESPONSE);
			}
			try {
				String responseString = aString;
				if (responseType.equalsIgnoreCase(FILE_RESPONSE)) {
					responseString = readResponseFile(aString);
					//Begin NBLXA-1416
					if (NbaUtils.isNbaOffline()) {
						InputStream is = new FileInputStream(new File(aString));
						SOAPMessage resp = MessageFactory.newInstance().createMessage(null, is);
						try {
							responseString = resp.getSOAPPart().getEnvelope().toString();
						} catch (Exception ex) {
							
						}
					}
					//End NBLXA-1416
				}
				if (getLogger().isDebugEnabled())
					getLogger().logDebug("Processing response from service...");
				responseString = responseString.replaceAll("\t", "").trim();
				if (responseString.indexOf(EIB_ERROR_SCHEMA) >= 0) {
					processResponse(responseString);//AXAL3.7.54
				} else if (responseString.indexOf("<TXLife") >= 0 || responseString.indexOf("TXLife") >= 0) {
					String responseBody = responseString.replaceAll("\t", "").trim();
					if (responseBody.indexOf(":Body>") > -1) {
						responseBody = responseBody.substring(responseBody.indexOf(":Body>") + ":Body>".length());
						responseBody = responseBody.substring(0, responseBody.indexOf(":Body"));
						responseBody = responseBody.substring(0, responseBody.lastIndexOf("<"));
					}
					
					NbaTXLife txLife = null;
						try {
							txLife = processMIBFollowUpResponse(responseBody);
						} catch (Exception e) {
							throw new NbaBaseException(e.getMessage());
						}
					
					responseMap.put(NBATXLIFE_ELEMENT, txLife);
				} else if (responseString.indexOf(CONTROLLER_EXCEPTION) >= 0) {
					this.setControllerException(true);
					processControllerException(responseString);
				} else if (responseString.indexOf("<TransResult") >= 0 || responseString.indexOf("TransResult") >= 0) {
					String responseBody = responseString.replaceAll("\t", "").trim();
					if (responseBody.indexOf(":Body>") > -1) {
						responseBody = responseBody.substring(responseBody.indexOf(":Body>") + ":Body>".length());
						responseBody = responseBody.substring(0, responseBody.indexOf(":Body"));
						responseBody = responseBody.substring(0, responseBody.lastIndexOf("<"));
					}
					responseMap.put(REINSURANCE_RESPONSE, responseBody);
				} else {
					Document document = createDocument(responseString);
					Element element = document.getDocumentElement();
					buildResponseMap(element);
					validateResponse();
				}
				if (getLogger().isDebugEnabled())
					getLogger().logDebug("The service response has been processed.");
			} catch (NbaBaseException e) {
				getLogger().logException(e);
				processResponse(aString);//AXAL3.7.54
			}  catch (Exception e) {
				getLogger().logException(e);
				throw new NbaBaseException(NbaBaseException.INVALID_RESPONSE + e.getMessage());
			}
		}
		
}
