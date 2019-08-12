package com.csc.fsg.nba.webservice.client;

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
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPHeader;

import org.apache.axis.message.SOAPEnvelope;

import com.csc.fsg.nba.business.transaction.NbaAxaServiceResponse;
import com.csc.fsg.nba.correspondence.NbaxPressionAdapter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;

/** 
 * 
 * This class encapsulates interaction with AXA Correspondence services as other services use different invocation now.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>PERF-APSL319</td><td>AXA Life Phase 1</td><td>PERF - AXA Interface Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 6.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaAxaCorrespondenceRequestor extends NbaWebServiceAdapterBase {
	private static String className = NbaAxaServiceRequestor.class.getName();
	protected SOAPEnvelope envelope = null;
	protected SOAPHeader header = null;
	protected SOAPBody body = null;
	protected static NbaLogger logger = null;
	protected Map requestParameters = null;
	protected Map properties = new HashMap();
	protected Map parameters = null;

	public static final String PARTNER_ID = "LIFECSC";  

	public static final String PARAM_NBATXLIFE = "NbaTxLife";
	public static final String PARAM_SERVICEOPERATION = "operation";
	public static final String PARAM_SOAPENVELOP = "envelop";//AXA3.7.22
	public static final String PARAM_TOKEN = "token";
	public static final String PARAM_USERID = "userid";
	public static final String PARAM_UDDIKEY = "uddikey";
	public static final String PARAM_PROCESSNAME = "processname";

	public static final String OPERATION_USER_SINGLE_SIGNON = "processSSOrequest";  //AXAL3.7.68
	public static final String OPERATION_ROBOTIC_SINGLE_SIGNON = "ROBOTICTOKEN";	//AXAL3.7.68
	public static final String OPERATION_SECURE_FILE_TRANSFER = "SFTP"; //ALS3400
	public static final String OPERATION_CONTRACT_SERVICE = "generateContractNumber";  //AXAL3.7.34
 	public static final String OPERATION_UNADMITTED_REPLACEMENT = "retrievePolicyActivity"; //AXAL3.7.24
 	public static final String OPERATION_AGENT_SEARCH_SERVICE = "SearchProducer"; //AXAL3.7.18
 	public static final String OPERATION_AGENT_VALIDATION_SERVICE = "ProducerLicenseStatus"; //AXAL3.7.18
 	public static final String OPERATION_AGENT_DEMOGRAPHIC_SERVICE = "GetDistributorInfo"; //AXAL3.
 	public static final String OPERATION_CAPS_INFORCE_SUBMIT_SERVICE = "submitAdministrationPolicy"; //AXAL3.7.17
 	public static final String OPERATION_CONTRACT_PRINT_SERVICE = "PolicyPrint"; //AXAL3.7.14
	//Begin AXAL3.7.68
	public static final String XSD = "xsd";
	public static final String XSD_VALUE = "http://www.w3.org/2001/XMLSchema";
	public static final String XSI = "xsi";
	public static final String XSI_VALUE = "http://www.w3.org/2001/XMLSchema-instance";
	//End AXAL3.7.68
	
 	public static final String OPERATION_MIB_RETRIEVEMEDICALINFO_SERVICE = "retrieveMedicalInfo"; //AXAL3.7.31
 	public static final String OPERATION_SEND_REQUIREMENT_ORDER = "sendRequirementOrders"; //AXAL3.7.31
 	
 	public static final String OPERATION_CIF_TRANSMIT = "transmitClientHolding";//AXAL3.7.25
 	
	public final String XPRESSION_GETCATEGORIES_RESPONSE= "getListOfCategoriesReturn";  //AXAL3.7.13I
	
	public final String XPRESSION_GETDOCUMENTS_RESPONSE= "getListOfDocumentsReturn";	//AXAL3.7.13I
	
	public final String XPRESSION_GETVARIABLES_RESPONSE= "getDocumentVariablesReturn";	//AXAL3.7.13I
	public final String XPRESSION_PDF_PREVIEW_RESPONSE= "streamContent";				//AXAL3.7.13I
 	
	public static final String OPERATION_SEND_POLICY_NOTIFICATIONS = "sendPolicyNotifications"; //AXAL3.7.54

 	
/**
* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
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
 * Retrieves a specific parameter from the parameter map.
 * @param params The input parameters from the calling process.
 * @param key The parameter name to obtain.
 * @return String The corresponding parameter's value.
 */
protected String getParameter(String key) {
    Iterator i = parameters.entrySet().iterator();
    Map.Entry param = null;
    while (i.hasNext()) {
        param = (Map.Entry)i.next();
        String mapKey = (String)param.getKey();
        if (mapKey.equalsIgnoreCase(key)) {
            if (param.getValue() instanceof String[]) {
                String mapValues[] = (String[])param.getValue();
                if (mapValues.length > 0)
                    return mapValues[0];
            } else if (param.getValue() instanceof char[]) {
                String mapValue = String.valueOf(param.getValue());
                return mapValue;
            } else if (param.getValue() instanceof NbaTXLife) {
                return ((NbaTXLife)param.getValue()).toXmlString();
            } else if (param.getValue()== null){
            	return null;
            }else {
                return param.getValue().toString();
            }
        }
    }
    return null;
}
/**
 * A stub method to ensure correct utilization of this class.
 * Requests should be created by the subclasses.
 */
protected void createRequest() throws NbaBaseException {
    throw new NbaBaseException("Cannot create request for "  + this.getClass().getName());
}


/**
 * A stub method to ensure correct utilization of this class.
 * Requests should be created by the subclasses.
 */
// AXAL3.7.13I - New method
protected void createRequestPDF() throws NbaBaseException {
    throw new NbaBaseException("Cannot create request for "  + this.getClass().getName());
}

/**
 * A stub method to ensure correct utilization of this class.
 * Service operations should be performed using the invokeAxaWebService method.
 */
public NbaTXLife invokeWebService(NbaTXLife nbATxLife) throws NbaBaseException {
    throw new NbaBaseException("Cannot create web service for "  + this.getClass().getName());
}
/**
 * Invoke an AXA service.
 * @param parameters The input parameters from the calling process.
 * @return Map representing the values contained within the service response.
 * @throws NbaBaseException
 */
public Map invokeAxaWebService(Map requestParameters) throws NbaBaseException {
    try {
        parameters = requestParameters;
        envelope = new SOAPEnvelope();
        header = envelope.getHeader();
        body = envelope.getBody();
        //AXAL3.7.14 start
        if(NbaConstants.TRUE_STR.equalsIgnoreCase(NbaConfiguration.getInstance().
        		getBusinessRulesAttributeValue(NbaConfigurationConstants.EMPTY_TAG_REMOVAL_SWITCH))){
        	String nbaTXLifeString = (String)requestParameters.get(NbaAxaServiceRequestor.PARAM_NBATXLIFE);
            requestParameters.put(NbaAxaServiceRequestor.PARAM_NBATXLIFE, NbaUtils.
            		getTxlifeWithEmptyTagsRem(nbaTXLifeString));	
        }
        //AXAL3.7.14 end
        createRequest();
        assignProperties();
    } catch (Exception e) {
        getLogger().logException(e);
		throw new NbaBaseException(NbaBaseException.INVALID_REQUEST);
    }
	try {
	    NbaAxaServiceResponse response = new NbaAxaServiceResponse(NbaAxaServiceResponse.STRING_RESPONSE, invoke());
	    requestParameters.put(PARAM_SOAPENVELOP, envelope.toString());
	    return response.getResponseMap();
	} catch (Exception e) {
	    getLogger().logException(e);
	    NbaBaseException nbe = new NbaBaseException(e.getMessage());
	    // enforcing the exception to be a Fatal exception so that All autoprocesses calling a web service will error stop. 
	    nbe.forceFatalExceptionType();
	    if (null != e.getCause() && e.getCause() instanceof SocketTimeoutException) {
	    	nbe.forceFatalExceptionType();
	    }
		throw nbe;
	}
}
/**
 * Test the connection and perform the communication.
 * @param parameters The input parameters from the calling process.
 * @return String The service response.
 * @throws Exception
 */
public synchronized String invoke() throws Exception {
    String response = null;
    if (doHandshake()) {
    	long startTimeInMs = System.currentTimeMillis();//NBLXA-2349
        AxisInvoker invoker = new AxisInvoker();
        if (parameters.containsKey(NbaAxaServiceRequestor.PARAM_PROCESSNAME)) {
            invoker.setUser("NBALIFE_" + getParameter(NbaAxaServiceRequestor.PARAM_PROCESSNAME));
        }
  		if (parameters.containsKey(NbaAxaServiceRequestor.PARAM_TOKEN)) {
  		    invoker.setToken(getParameter(NbaAxaServiceRequestor.PARAM_TOKEN));
  		}
  	    if (getLogger().isDebugEnabled()) {
  	        getLogger().logDebug(className + " Request: " + envelope.toString());
  	    }
  		response = invoker.invoke(properties, envelope);

  		if (getLogger().isDebugEnabled()) {
  			long diff = System.currentTimeMillis() - startTimeInMs;//NBLXA-2349
  	        getLogger().logDebug(className + " Response: Time Taken in calling web service ##  " + String.valueOf(diff) +" ## "  + response);//NBLXA-2349
  	    }
  	}
    return response;
}
/**
 * Connect to the service's WSDL URL to verify that the connection is operating correctly. 
 * @return boolean Was the connection test successful?
 * @throws Exception
 */
protected boolean doHandshake() throws Exception {
    return true;
}
/**
 * Prepare properties for the service communication. 
 * @param parameters The input parameters from the calling process.
 * @throws NbaBaseException
 */
protected void assignProperties() {
    //ALS5899 code deleted and moved to WAS Configuration    
    // Build service information
    properties.put("url", getTargetUri());			
	properties.put("port", "");
	properties.put("operationName", getParameter(PARAM_SERVICEOPERATION));
	properties.put("soapAction", "");
	properties.put("SoapFaultNS", "");
	properties.put("SoapFaultClass", "");
	properties.put("SoapFaultNode", "");
	properties.put("timeout", getTimeout());
	properties.put("wsdl", getWsdlUrl());
}
/**
 * Invoke a call to the UDDI to obtain the target URI and WSDL URL 
 * @param parameters The input parameters from the calling process.
 */
protected void getAddressFromUDDI() {
//	try {
//		NbaWebServiceAdapter service =
//			NbaWebServiceAdapterFactory.createWebServiceAdapter("AXA", "UDDI", "UDDIRegistryLookup");
//	    Map params = new HashMap();
//	    params.put(PARAM_SERVICEOPERATION, OPERATION_UDDI_LOOKUP);
//	    params.put(PARAM_UDDIKEY, getParameter(PARAM_UDDIKEY));
//		Map responseMap = service.invokeAxaWebService(params);
//		if (responseMap.containsKey(NbaAxaServiceResponse.UDDI_ADDRESS_ELEMENT)) {
//		    setTargetUri((String)responseMap.get(NbaAxaServiceResponse.UDDI_ADDRESS_ELEMENT));
//		    setWsdlUrl((String)responseMap.get(NbaAxaServiceResponse.UDDI_ADDRESS_ELEMENT) + "?wsdl");
//		}
//	} catch (Exception e) {
//	    getLogger().logException(e);
//	}
}

	//AXAL3.7.13I - New method
	public Object invokeCorrespondenceWebService(String user, String password, String categoryLetter, String type, String xml, 
			String operation, String token, Map keys) throws com.csc.fsg.nba.exception.NbaBaseException {
		try {
    	
	    	Map param = new HashMap();
	    	param.put("operation", operation);
	    	param.put("token", token);
	    	param.put("username", user);
	    	param.put("password", password);
	    	param.put("categoryName", categoryLetter);
	    	param.put("docName", type);
	    	param.put("customerData", xml);
	    	if ( keys != null ){
		    	param.put("keysMap", keys);
	    	}
	    	
	        parameters = param;
	        envelope = new SOAPEnvelope();
	        header = envelope.getHeader();
	        body = envelope.getBody();
	        if ( operation.equals(NbaxPressionAdapter.PDF_OPERATION)){
	        	createRequestPDF();
	        }else{
	        	createRequest();
	        }
	        assignProperties();
	    } catch (Exception e) {
	        getLogger().logException(e);
			throw new NbaBaseException(NbaBaseException.INVALID_REQUEST);
	    }
		try {
		    NbaAxaServiceResponse response = new NbaAxaServiceResponse(NbaAxaServiceResponse.STRING_RESPONSE, invoke());
		    if ( operation.equals(NbaxPressionAdapter.CATEGORY_REQUEST)){
		    	return response.getResponseMap().get(XPRESSION_GETCATEGORIES_RESPONSE);	
		    }else if (operation.equals(NbaxPressionAdapter.LETTER_REQUEST)){
		    	return response.getResponseMap().get(XPRESSION_GETDOCUMENTS_RESPONSE);
		    }else if (operation.equals(NbaxPressionAdapter.VARIABLE_REQUEST)){
		    	return response.getResponseMap().get(XPRESSION_GETVARIABLES_RESPONSE);
		    }else if (operation.equals(NbaxPressionAdapter.PDF_OPERATION)){
		    	return response.getResponseMap().get(XPRESSION_PDF_PREVIEW_RESPONSE);
		    }
		    return null; 
		} catch (Exception e) {
		    getLogger().logException(e);
		    NbaBaseException nbe = new NbaBaseException(e.getMessage());
		    // enforcing the exception to be a Fatal exception so that All autoprocesses calling a web service will error stop. 
		    nbe.forceFatalExceptionType();
		    if (null != e.getCause() && e.getCause() instanceof SocketTimeoutException) {
		    	nbe.forceFatalExceptionType();
		    }
			throw nbe;
		}
	}

	
	
	/**
	 * @return Returns the parameters.
	 */
	//AXAL3.7.13I - New method
	public Map getParameters() {
		return parameters;
	}
	/**
	 * @param parameters The parameters to set.
	 */
	//AXAL3.7.13I - New method
	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}
}

