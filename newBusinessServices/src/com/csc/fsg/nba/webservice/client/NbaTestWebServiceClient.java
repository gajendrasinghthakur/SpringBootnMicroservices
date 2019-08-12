package com.csc.fsg.nba.webservice.client;

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
 * 
 * *******************************************************************************<BR>
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import java.util.Vector;

import org.apache.soap.Constants;
import org.apache.soap.Fault;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.apache.soap.transport.http.SOAPHTTPConnection;

import com.csc.fsg.nba.business.transaction.NbaAxaServiceResponse;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * NbaTestWebServiceClient is the client class to call TestWebService.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA092</td><td>Version 3</td><td>Initial Development</td></tr>
 * <tr><td>NBA129</td><td>Version 5</td><td>xPression Correspondence</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR3337</td><td>Version 7</td><td>PDF data for letters is not stored in the workflow system</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaTestWebServiceClient extends NbaWebServiceAdapterBase {
	private java.lang.reflect.Method setTcpNoDelayMethod;
	private Call call;
	private NbaLogger logger;

	/**
	 * Constructor for TestWebServiceClient.
	 */
	public NbaTestWebServiceClient() {
		super();
	}

	/**
	 * This invokeWebService() method will be used to call TestWebService. This will accept two arguments, first is NbaTXLife object,
	 * which is xml transaction for actual WebService and another is fileName of the response xml.
	 * @param nbATxLife An instance of <code>NbaTXLife</code>
	 * @param fileName An instance of <code>Object</code> is the response file name
	 * @return NbaTXLife is the response xml
	 */
	public NbaTXLife invokeWebService(NbaTXLife nbATxLife) { // SPR2968
		NbaTXLife response = null;
		try {
			try {
				setTcpNoDelayMethod = SOAPHTTPConnection.class.getMethod("setTcpNoDelay", new Class[] { Boolean.class });
			} catch (Exception e) {
			}

			call = createCall();
			String returnedEle = getXmlResponse(nbATxLife.toXmlString(), getTestWsFileName()); // SPR2968
			response = string2TxLife(returnedEle);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return response;
	}
	
	/**
	 * This invokeCorrespondenceWebService() method will be used to call TestWebService. This will accept six arguments,
	 * the xpression user, password category/Letter name, transformation/batch type and the file name 
	 * @param user xPression user id
	 * @param password xPression password
	 * @param categoryLetter Name of the xPression category of letter name
	 * @param type xPression transformation or batch named defined in the xPressionAdapter.properties file
	 * @param fileName Name of the webservice function id 
	 * which is xml transaction for actual WebService and another is fileName of the response xml.
	 * @param nbATxLife An instance of <code>NbaTXLife</code>
	 * @param fileName An instance of <code>Object</code> is the response file name
	 * @return Object A response xml or byte[] depending on the type of call
	 */
	//NBA129 new method
	//SPR3337 changed method signature
	public Object invokeCorrespondenceWebService(String user, String password, String categoryLetter, String type, String xml, String fileName) throws com.csc.fsg.nba.exception.NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Request message received by NbaTestWebServiceClient for Correspondence : ");	
		}
		
		StringBuffer qualifiedFile = new StringBuffer(); // SPR2968
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		StringBuffer lines = new StringBuffer();
		try {
			// begin SPR2968
			qualifiedFile.append(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.TEST_WS_FOLDER));
			qualifiedFile.append(getTestWsFileName());
			fr = new FileReader(qualifiedFile.toString());
			// end SPR2968
		} catch (java.io.FileNotFoundException fnfe) {
			System.out.println(fnfe.getMessage());
		}
		try {
		br = new BufferedReader(fr);
		while (true) {
			line = br.readLine();
			if (line == null) {
				break;
			} else {
				lines.append(line);
			}
		}
		br.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	    if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Response received from Stubbed service for Correspondence From file: " + qualifiedFile.toString() );	
			getLogger().logDebug(lines.toString());
		}

		
		return lines.toString();
	}

	/**
	 * This method will be invoked from invokeWebService() method to call WebService.
	 * @param ele An instance of <code>String</code> is the xml transaction for WebService
	 * @param fileName An instance of <code>String</code> is the response xml filename
	 * @return String is the response xml string
	 */
	public synchronized String getXmlResponse(String ele, String fileName) throws Exception {
		// begin SPR2968
	    String targetObjectURI = "http://tempuri.org/com.csc.fsg.nba.webservice.client.NbaTestWebService";
	    String SOAPActionURI = "";

	    if(getWsdlUrl() == null || getWsdlUrl().trim().length()==0)
	    {
	      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
	      "A URL must be specified via NbaTestWebServiceClient.setEndPoint(URL).");
	    }

	    call.setMethodName("getXmlResponse");
	    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
	    call.setTargetObjectURI(targetObjectURI);
	    Vector params = new Vector();
	    Parameter elementParam = new Parameter("element", java.lang.String.class, ele, Constants.NS_URI_SOAP_ENC);
	    params.addElement(elementParam);
	    Parameter fileNameParam = new Parameter("fileName", java.lang.String.class, fileName, Constants.NS_URI_SOAP_ENC);
	    params.addElement(fileNameParam);
	    call.setParams(params);
	    Response resp = call.invoke(new java.net.URL(getWsdlUrl()), SOAPActionURI);

	    //Check the response.
	    if (resp.generatedFault())
	    {
	      Fault fault = resp.getFault();
	      call.setFullTargetObjectURI(targetObjectURI);
	      throw new SOAPException(fault.getFaultCode(), fault.getFaultString());
	    }
	    else
	    {
	      Parameter refValue = resp.getReturnValue();
	      return ((java.lang.String)refValue.getValue());
	    }
	    // end SPR2968
	  }

	/**
	 * This method will be invoked from invokeWebService() method to call the WebService.
	 * @return Call Call
	 */
	protected Call createCall() {
		SOAPHTTPConnection soapHTTPConnection = new SOAPHTTPConnection();
		if (setTcpNoDelayMethod != null) {
			try {
				setTcpNoDelayMethod.invoke(soapHTTPConnection, new Object[] { Boolean.TRUE });
			} catch (Exception ex) {
			}
		}
		Call call = new Call();
		call.setSOAPTransport(soapHTTPConnection);
		SOAPMappingRegistry smr = call.getSOAPMappingRegistry();
		return call;
	}

	/**
	 * This method is used to convert the String representation of response XML to NbaTxLife.
	 * @param response String
	 * @return NbaTXLife
	 * @throws Exception
	 */
	protected NbaTXLife string2TxLife(String response) throws Exception {
		StringBuffer txLifeStrBuf = new StringBuffer(response);
		return new NbaTXLife(txLifeStrBuf.toString());
	}
	/**
	 * This invokeAxaWebService() method will be used to call TestWebService. This will accept one Map argument,
	 * containg request parameters 
	 * @param parameters request parameters
	 * @param password xPression password
	 * @param categoryLetter Name of the xPression category of letter name
	 * @param type xPression transformation or batch named defined in the xPressionAdapter.properties file
	 * @param fileName Name of the webservice function id 
	 * which is xml transaction for actual WebService and another is fileName of the response xml.
	 * @param nbATxLife An instance of <code>NbaTXLife</code>
	 * @param fileName An instance of <code>Object</code> is the response file name
	 * @return Object A response xml or byte[] depending on the type of call
	 */
	public Map invokeAxaWebService(Map parameters) throws NbaBaseException { 
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Request message received by NbaTestWebServiceClient : ");	
			getLogger().logDebug(parameters.get(NbaAxaServiceRequestor.PARAM_NBATXLIFE));
		}
		parameters.put(NbaAxaServiceRequestor.PARAM_SOAPENVELOP, parameters.get(NbaAxaServiceRequestor.PARAM_NBATXLIFE));//TODO Remove this after testing
		StringBuffer qualifiedFile = new StringBuffer();
		qualifiedFile.append(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.TEST_WS_FOLDER));
		qualifiedFile.append(getTestWsFileName());
		qualifiedFile.append(".xml");
		NbaAxaServiceResponse response;
		String transformationRequired = (String)parameters.get("transformationRequired");     
		if(!NbaUtils.isBlankOrNull(transformationRequired) && transformationRequired.equalsIgnoreCase("YES")){
			response = new NbaAxaServiceResponse(NbaAxaServiceResponse.FILE_RESPONSE, qualifiedFile.toString(),"MIB");
		}
		else{
			response = new NbaAxaServiceResponse(NbaAxaServiceResponse.FILE_RESPONSE, qualifiedFile.toString());
		}		
	    if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Response received from Stubbed service for operation: " + parameters.get(NbaAxaServiceRequestor.PARAM_SERVICEOPERATION) +
					", From file: " + qualifiedFile.toString() );	
			//NbaTXLife txlife = (NbaTXLife)  response.getResponseMap().get("NbaTXLife");
			//getLogger().logDebug(txlife.toXmlString());
		}
		return response.getResponseMap();
	}
	private NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger("com.csc.fsg.nba.webservice.client.NbaTestWebServiceClient");
			} catch (Exception e) {
				NbaBootLogger.log("NbaTestWebService could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
