package com.csc.fsg.nba.communication;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.business.transaction.NbaAxaServiceResponse;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Provider;
import com.csc.fsg.nba.webservice.client.NbaAxaServiceRequestor;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;

/**
 * NbaEibCommunicator provides communcation services between nbAccelerator and
 * AXA's EIB via HTTP post.  It extends the NbaAbsCommunicator class.
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
 * @since New Business Accelerator - Version 4
 */
public class AxaEibCommunicator extends NbaAbsCommunicator {
	public Provider provider = null;
	private static NbaLogger logger = null;   
	
	/**
	 * AxaEibCommunicator default constructor.
	 */
	public AxaEibCommunicator() {
		super();
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(AxaEibCommunicator.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("AxaEibCommunicator could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Using aNewProvider input, initializes the provider and the access method.
	 * @param aNewProvider the provider to whom the message will be sent
	 */
	public void initialize(Provider aNewProvider) {
		setProvider(aNewProvider);
	}
	/**
	 * Answers the provider 
	 * @return NbaConfigProvider
	 */
	public Provider getProvider() {
		return provider;
	}
	/**
	 * This method interrogates the accessMethod for the provider and
	 * determines which class method to use to communicate with the
	 * provider.
	 * @param target a path or web address to which the message should be writtern/posted
	 * @param message the message to be sent to the provider
	 * @return Object an Object representing the result of the communication
	 */
	public Object processMessage(String target, Object message) throws NbaBaseException {
		return invokeWebservice(target, message);
	}
	
	/**
	 * This method interrogates the accessMethod for the provider and
	 * determines which class method to use to communicate with the
	 * provider.
	 * @param target a path or web address to which the message should be writtern/posted
	 * @param message the message to be sent to the provider
	 * @param user the user object sending the message to provider 
	 * @return Object an Object representing the result of the communication
	 */
	public Object processMessage(String target, Object message, NbaUserVO user) throws NbaBaseException {
		return invokeWebservice(target, message, user); 
	}

	/**
	 * Initializes the provider with whom communication is desired
	 * @param newProvider
	 */
	public void setProvider(Provider newProvider) {
		provider = newProvider;
	}
	
	/**
	 * Invokes the MIB web service
	 * @param target the URL to which the message should be posted
	 * @param message the message to be sent to the provider
	 * @return the response from the target web service
	 */
	private Object invokeWebservice(String target, Object message) throws NbaBaseException {
		throw new NbaBaseException("Provider communications via EIB require user credentials.");
	}
	
	/**
	 * Invokes the EIB web service
	 * @param target the URL to which the message should be posted
	 * @param message the message to be sent to the provider
	 * @param user the user object sending the message to provider
	 * @return the response from the target web service
	 */
	private Object invokeWebservice(String target, Object message, NbaUserVO user) throws NbaBaseException {
        String xml = (String)message;
        Map params = new HashMap();
        try {
    		if(getLogger().isDebugEnabled()) { 
    		    getLogger().logDebug("invokeWebservice : Sending xml : " + xml);
    		} 
        	params.put(NbaAxaServiceRequestor.PARAM_SERVICEOPERATION, NbaAxaServiceRequestor.OPERATION_SEND_REQUIREMENT_ORDER);
        	params.put(NbaAxaServiceRequestor.PARAM_NBATXLIFE, xml);
            params.put(NbaAxaServiceRequestor.PARAM_TOKEN, user.getToken() );
            params.put(NbaAxaServiceRequestor.PARAM_UDDIKEY, "ToBeDetermined");
        } catch (Exception e) {
            throw new NbaBaseException(NbaBaseException.INVALID_REQUEST, e);
        }
        String defaultIntegration = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_INTEGRATION);
        NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(defaultIntegration, 
        		NbaConfigurationConstants.WEBSERVICE_CATEGORY_PROVIDER_COMMUNICATION, 
        		NbaConfigurationConstants.WEBSERVICE_FUNCTION_PROVIDER_OTHER);
   	    Map results = service.invokeAxaWebService(params);
   	    NbaTXLife txLifeResult = (NbaTXLife) results.get(NbaAxaServiceResponse.NBATXLIFE_ELEMENT);
   	    if (txLifeResult == null) {
   	    	String error = "Error (" + results.get(NbaAxaServiceResponse.ERRORCODE_ELEMENT) + ") " + results.get(NbaAxaServiceResponse.ERRORMSG_ELEMENT);
   	   	    if(getLogger().isDebugEnabled()) { 
   			    getLogger().logDebug("invokeWebservice : Response received from webservice: " + error);
   			} 
   	    	throw new NbaBaseException(error);
   	    }
   	    
   	    if(getLogger().isDebugEnabled()) { 
		    getLogger().logDebug("invokeWebservice : Response received from webservice: " + txLifeResult.toXmlString());
		} 
   	    
   	    return txLifeResult.toXmlString();
	}
}
