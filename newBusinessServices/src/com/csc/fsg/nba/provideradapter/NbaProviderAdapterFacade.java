package com.csc.fsg.nba.provideradapter;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.communication.NbaAbsCommunicator;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaPollingException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Provider;

/**
 * NbaProviderAdapterFacade provides a common entry point into the Provider interface
 * in order to faciliate the ordering and receiving of requirements from different
 * providers.
 * <p>When a method needs to interface with a provider (for ordering or receiving
 * requirements), it will create a new NbaProviderAdapterFacade and use that to invoke
 * the appropriate adapter method.  For example, a process might code:<p>
 * <code>boolean response = new NbaProviderAdapterFacade(work).submitRequestToProvider();</code>
 * <p>This class would then get the requirement message from source, find the provider and create
 * an NbaProviderAdapter of that provider type. The request would then be processed using
 * the newly created adapter.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA081</td><td>Version 3</td><td>Requirements Ordering and Receipting - HOOPER HOLMES</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>ACORD 121/1122 General Requirements Migration</td></tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>ACORD 401/402 MIB Inquiry and Update Migration</td></tr>
 * <tr><td>NBA212</td><td>Version 7</td><td>Content Services</td></tr>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 1</td><td>Provider Interface - MIB</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProviderAdapter
 * @since New Business Accelerator - Version 2
 */
//ACN014 Changed to extend NbaProviderAdapter
public class NbaProviderAdapterFacade extends NbaProviderAdapter {
	public static com.csc.fsg.nba.foundation.NbaLogger logger;
	protected Provider configProvider; //ACN012	
	//public com.csc.fsg.nba.vo.NbaTXLife nbaTxLife;
	public com.csc.fsg.nba.vo.NbaDst work;
	public com.csc.fsg.nba.vo.configuration.Provider provider; //ACN012
	public List xmlTrans;
	private boolean batch;
	protected NbaUserVO user = null;   //AXAL3.7.31
	
/**
 * NbaProviderAdapterFacade constructor uses the <code>NbaDst</code> work item to
 * set the work object to be used for the process.
 * @param newWork an <code>NbaDst</code> object containing the requirement and it's
 * associated information.
 */
//ACN014 Changed signature
public NbaProviderAdapterFacade(NbaDst newWork, NbaUserVO newUser) throws NbaBaseException{
	super();
	setWork(newWork);
	setProvider(getProviderFromCnfg(newWork.getNbaLob().getReqVendor()));
	setBatch(getProvider().getBatch()); //ACN012
	setUser(newUser);	//AXAL3.7.31
}


/**
 * NbaProviderAdapterFacade constructor uses the <code>NbaWorkItem</code> work item to
 * set the work object to be used for the process.
 * @param newWork an <code>NbaWorkItem</code> object containing the requirement and it's
 * associated information.
 * @throws NbaBaseException 
 */
//NBA308 New Method
public NbaProviderAdapterFacade(String provider) throws NbaBaseException {    
    super();
    setWork(null);
    setProvider(getProviderFromCnfg(provider));   
    setBatch(false);
}
/**
 * This method converts the XML Requirement transactions into a format
 * that is understandable by the provider.
 * @param aList array list of requirement transactions
 * @return Map a provider ready message in a Map, along with any errors that might have occurred.
 * @exception NbaBaseException thrown if an error occurs.
 */
public Map convertXmlToProviderFormat(List aList) throws NbaBaseException {
	//ACN014 begin
	String providerID = work.getNbaLob().getReqVendor();
	configProvider = NbaConfiguration.getInstance().getProvider(providerID);
	NbaProviderAdapter adapter;
	try {
		adapter = (NbaProviderAdapter) NbaUtils.classForName(configProvider.getNbaAdapter()).newInstance();
	} catch (InstantiationException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_INVALID,e);
	} catch (IllegalAccessException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_ILLEGAL_ACCESS,e);
	} catch (ClassNotFoundException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_NOT_FOUND,e);
	}
	//ACN014 end
	return adapter.convertXmlToProviderFormat(aList);
}
/**
 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
private static NbaLogger getLogger() {
	if (logger == null) {
		try {
			logger = NbaLogFactory.getLogger(NbaProviderAdapterFacade.class.getName());
		} catch (Exception e) {
			NbaBootLogger.log("NbaProviderAdapterFacade could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}
	}
	return logger;
}
/**
 * Answers provider configuration.
 * @return com.csc.fsg.nba.configuration.Provider
 */
//ACN012 CHANGED SIGNATRUE
public Provider getProvider() {
	return provider;
}
/**
 * Get the provider information from the configuration file.
 * @param providerName name of the provider.
 * @return Provider the <code>Provider</code> for this object
 */
//ACN012 CHANGED SIGNATURE
private Provider getProviderFromCnfg(String providerName) throws NbaBaseException {
	 return NbaConfiguration.getInstance().getProvider(providerName);
}
/**
 * Answers the work item.
 * @return NbaDst
 */
public NbaDst getWork() {
	return work;
}
/**
 * Answers whether or not the provider handles batch responses.
 * @return boolean
 */
public boolean isBatch() {
	return batch;
}
/**
 * This method converts the Provider's response into an XML transaction. It 
 * also updates required LOBs and result source with converted XMLife.
 * @param work the requirement work item.
 * @return NbaDst the requirement work item with formated source.
 */
//ACN014 Changed signature
public ArrayList processResponseFromProvider(NbaDst work, NbaUserVO user) throws NbaBaseException {
	//ACN014 begin
	String providerID = work.getNbaLob().getReqVendor();
	configProvider = NbaConfiguration.getInstance().getProvider(providerID);
	
	NbaProviderAdapter adapter;
	try {
		adapter = (NbaProviderAdapter) NbaUtils.classForName(configProvider.getNbaAdapter()).newInstance();
	} catch (InstantiationException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_INVALID,e);
	} catch (IllegalAccessException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_ILLEGAL_ACCESS,e);
	} catch (ClassNotFoundException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_NOT_FOUND,e);
	}
	return adapter.processResponseFromProvider(work, user);
	//ACN014 end
}

/**
 * This method provides the means by which a message representing a request for 
 * a requirement is submitted to the provider.<p>
 * The means of communication varies and may include many different methods:
 * HTTP Post, writing the message to a folder on a server, or others.  The
 * <code>Provider</code> that contains information on how to communicate
 * with the provider is found by the method and the communication implementation
 * class from the configuration file is used to instantiate the appropriate
 * communicator, a subclass of the <code>NbaAbsCommunicator</code> class.
 * Once the initialize method is called to set the <code>Provider</code>, the
 * processMessage method is called to submit the message.
 * @param aTarget the destinatin of the message
 * @param aMessage the message to be sent to the provider
 * @return Object an Object that must be evaluated by the calling process
 */
public Object sendMessageToProvider(String aTarget, Object aMessage) throws NbaBaseException {
    getLogger().logDebug(aMessage);	//NBA212
	//ACN009 begin
	String providerID = work.getNbaLob().getReqVendor();
	configProvider = NbaConfiguration.getInstance().getProvider(providerID);
	NbaAbsCommunicator proComm;
	try {
		proComm = (NbaAbsCommunicator) NbaUtils.classForName(configProvider.getCommImpl()).newInstance();
	} catch (InstantiationException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_INVALID,e);
	} catch (IllegalAccessException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_ILLEGAL_ACCESS,e);
	} catch (ClassNotFoundException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_NOT_FOUND,e);
	}
	proComm.initialize(configProvider);
	
	//ACN009 end 
	Object response = proComm.processMessage(aTarget, aMessage); //NBA212
	getLogger().logDebug(response);	//NBA212
	return response;	//NBA212 
}

/**
 * This method provides the means by which a message representing a request for 
 * a requirement is submitted to the provider.<p>
 * The means of communication varies and may include many different methods:
 * HTTP Post, writing the message to a folder on a server, or others.  The
 * <code>Provider</code> that contains information on how to communicate
 * with the provider is found by the method and the communication implementation
 * class from the configuration file is used to instantiate the appropriate
 * communicator, a subclass of the <code>NbaAbsCommunicator</code> class.
 * Once the initialize method is called to set the <code>Provider</code>, the
 * processMessage method is called to submit the message.
 * @param aTarget the destinatin of the message
 * @param aMessage the message to be sent to the provider
 * @param user the user object sending the message to provider 
 * @return Object an Object that must be evaluated by the calling process
 */
//AXAL3.7.31 - New method
public Object sendMessageToProvider(String aTarget, Object aMessage, NbaUserVO user) throws NbaBaseException {
    getLogger().logDebug(aMessage);	
	String providerID = work.getNbaLob().getReqVendor();
	configProvider = NbaConfiguration.getInstance().getProvider(providerID);
	NbaAbsCommunicator proComm;
	try {
		proComm = (NbaAbsCommunicator) NbaUtils.classForName(configProvider.getCommImpl()).newInstance();
	} catch (InstantiationException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_INVALID,e);
	} catch (IllegalAccessException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_ILLEGAL_ACCESS,e);
	} catch (ClassNotFoundException e) {
		throw new NbaBaseException(NbaPollingException.CLASS_NOT_FOUND,e);
	}
	proComm.initialize(configProvider);
	
	//ACN009 end 
	Object response = proComm.processMessage(aTarget, aMessage, user); 
	getLogger().logDebug(response);	
	return response;	
}
/**
 * Sets whether or not this provider has batch responses.
 * @param newBatch <code>true</code> if this provider has batch responses <code>false</code> otherwise
 */
public void setBatch(boolean newBatch) {
	batch = newBatch;
}
/**
 * Sets provider configuration.
 * @param newProvider com.csc.fsg.nba.configuration.Provider
 */
//ACN012 CHANGED SIGNATURE
public void setProvider(Provider newProvider) {
	provider = newProvider;
}
/**
 * Sets work item.
 * @param newWork com.csc.fsg.nba.vo.NbaDst
 */
private void setWork(com.csc.fsg.nba.vo.NbaDst newWork) {
	work = newWork;
}


	/**
	 * @return Returns the user.
	 */
	//AXAL3.7.31 - New method
	public NbaUserVO getUser() {
		return user;
	}
	/**
	 * @param user The user to set.
	 */
	//AXAL3.7.31 - New method	
	public void setUser(NbaUserVO user) {
		this.user = user;
	}
	
	/**
	 * This method provides the means by which a message representing a request for 
	 * a requirement is submitted to the provider.<p>
	 * The means of communication varies and may include many different methods:
	 * HTTP Post, writing the message to a folder on a server, or others.  The
	 * <code>Provider</code> that contains information on how to communicate
	 * with the provider is found by the method and the communication implementation
	 * class from the configuration file is used to instantiate the appropriate
	 * communicator, a subclass of the <code>NbaAbsCommunicator</code> class.
	 * Once the initialize method is called to set the <code>Provider</code>, the
	 * processMessage method is called to submit the message.
	 * @param aTarget the destinatin of the message
	 * @param aMessage the message to be sent to the provider
	 * @param user the user object sending the message to provider 
	 * @return Object an Object that must be evaluated by the calling process
	 */
	//NBLXA-2433 New method
	public Object sendMessageToProvider(String aTarget, Object aMessage, NbaUserVO user,String providerID) throws NbaBaseException {
	    getLogger().logDebug(aMessage);	
		configProvider = NbaConfiguration.getInstance().getProvider(providerID);
		NbaAbsCommunicator proComm;
		try {
			proComm = (NbaAbsCommunicator) NbaUtils.classForName(configProvider.getCommImpl()).newInstance();
		} catch (InstantiationException e) {
			throw new NbaBaseException(NbaPollingException.CLASS_INVALID,e);
		} catch (IllegalAccessException e) {
			throw new NbaBaseException(NbaPollingException.CLASS_ILLEGAL_ACCESS,e);
		} catch (ClassNotFoundException e) {
			throw new NbaBaseException(NbaPollingException.CLASS_NOT_FOUND,e);
		}
		proComm.initialize(configProvider);
		
		//ACN009 end 
		Object response = proComm.processMessage(aTarget, aMessage, user); 
		getLogger().logDebug(response);	
		return response;	
	}
}
