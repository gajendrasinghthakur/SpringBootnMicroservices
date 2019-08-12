package com.csc.fsg.nba.communication;

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

public class AxaEibLNCommunicator extends NbaAbsCommunicator{
	public Provider provider = null;
	private static NbaLogger logger = null;   
	
	/**
	 * AxaEibCommunicator default constructor.
	 */
	public AxaEibLNCommunicator() {
		super();
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(AxaEibLNCommunicator.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("AxaEibLNCommunicator could not get a logger from the factory.");
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
        	params.put(NbaAxaServiceRequestor.PARAM_SERVICEOPERATION, NbaAxaServiceRequestor.OPERATION_RETRIEVE_REQUIREMENT_RESULTS);
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
