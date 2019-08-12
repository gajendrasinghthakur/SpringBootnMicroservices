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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.configuration.Function;

/** 
 * This class is an abstract class that implements the NbaWebServiceAdapter partially.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA092</td><td>Version 3</td><td>Architecture Changes for Phase2C</td></tr>
 * <tr><td>SPR1829</td><td>Version 4</td><td>Modify to allow NbaBaseExceptions to be propagated</td></tr>
 * <tr><td>NBA108</td><td>Version 4</td><td>Vantage Inforce Payment</td></tr>
 * <tr><td>NBA109</td><td>Version 4</td><td>Vantage Loan Payment</td></tr>
 * <tr><td>NBA129</td><td>Version 5</td><td>xPression Correspondence</td></tr>
 * <tr><td>NBA124</td><td>Version 5</td><td>Underwriting Risk Remap</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR3337</td><td>Version 7</td><td>PDF data for letters is not stored in the workflow system</td></tr>
 * <tr><td>AXAL3.7.68</td><td>Version 7</td><td>LDAP Interface</td></tr>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal Correspondence</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public abstract class NbaWebServiceAdapterBase implements NbaWebServiceAdapter {
	protected String wsdlUrl = null;
	protected String targetUri = null;
	protected String access = null;
	protected String timeout = null;//NBA108 NBA109
	protected NbaTXLife txLife = null;
	protected NbaDst nbaDst = null;
	protected Object object = null;
	public static final String FAULT_ELEMENT = "soapenv:Fault"; //NBA124
	protected String testWsFileName = null; // SPR2968
	protected Function function = null; // APSL4010

	/**
	 * Returns the access.
	 * @return String
	 */
	public String getAccess() {
		return access;
	}

	/**
	 * Returns the targetUri.
	 * @return String
	 */
	public String getTargetUri() {
		return targetUri;
	}

	/**
	 * Returns the wsdlUrl.
	 * @return String
	 */
	public String getWsdlUrl() {
		return wsdlUrl;
	}

	/**
	 * Sets the access.
	 * @param access The adapterClass to set
	 */
	public void setAccess(String access) {
		this.access = access;
	}

	/**
	 * Sets the targetUri.
	 * @param targetUri The targetUri to set
	 */
	public void setTargetUri(String targetUri) {
		this.targetUri = targetUri;
	}

	/**
	 * Sets the wsdlUrl.
	 * @param wsdlUrl The wsdlUrl to set
	 */
	public void setWsdlUrl(String wsdlUrl) {
		this.wsdlUrl = wsdlUrl;
	}

	/**
	 * Returns the txLife.
	 * @return NbaTXLife
	 */
	public NbaTXLife getNbaTXLife() {
		return txLife;
	}

	/**
	 * Returns the object.
	 * @return Object
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * This invokeWebService() method will be used to call WebService. Two arguments will be passed, first is NbaTXLife object,
	 * which is xml transaction for the WebService and another argument can be used for any other purpose if needed.
	 * @param nbATxLife An instance of <code>NbaTXLife</code>
	 * @param fileName An instance of <code>Object</code> is the response file name
	 * @return NbaTXLife is the response xml
	 * @throws NbaBaseException
	 */
	public abstract NbaTXLife invokeWebService(NbaTXLife nbATxLife) throws NbaBaseException; //SPR1829 SPR2968
	
	/**
	 * This invokeCorrespondenceWebService will throw an error saying that a correspondence web service is 
	 * invalid.
	 * @param user xPression user id
	 * @param password xPression password
	 * @param categoryLetter Name of the xPression category of letter name
	 * @param type xPression transformation or batch named defined in the xPressionAdapter.properties file
	 * @param xml Customer data xml 
	 * @param fileName Name of the webservice function id
	 * @return Object response returned from WebService
	 */
	//NBA129 new method
	//SPR3337 changed method signature
	//AXAL3.7.13I changed method signature
	public Object invokeCorrespondenceWebService(String user, String password, String categoryLetter, String type, String xml, String fileName, String token, Map keys) 
		throws com.csc.fsg.nba.exception.NbaBaseException { 
		
		throw  new com.csc.fsg.nba.exception.NbaBaseException("Cannot create correspondece web service for "  + this.getClass().getName());
	}

	/**
	 * Returns timeout value in miliseconds mentioned in NbaConfiguration.xml file
	 * @return String timeout
	 */
	//NBA108 NBA109 new method
	public String getTimeout() {
		return timeout;
	}

	/**
	 * Sets the timeout value in miliseconds mentioned in NbaConfiguration.xml file
	 * @param string timeout value
	 */
	//NBA108 NBA109 new method
	public void setTimeout(String string) {
		timeout = string;
	}

	/** 
	 * Gets the test web service file name, which is based on config file keys, for this web service adapter.
	 * @return the test web service file name
	 */
	// SPR2968 - new method
	public String getTestWsFileName() {
		return testWsFileName;
	}

	/** 
	 * Sets the test web service file name, which is based on config file keys, for this web service adapter.
	 * @param string concatenated configuration keys
	 */
	// SPR2968 - new method
	public void setTestWsFileName(String string) {
		testWsFileName = string;
	}
	/**
	 * This invokeAxaWebService will throw an error saying that the web service is 
	 * invalid.
	 * @param Map parameters from the request
	 * @return Map response returned from WebService
	 */
	//AXAL3.7.68 new method
	public Map invokeAxaWebService(Map parameters) throws com.csc.fsg.nba.exception.NbaBaseException { 
		throw  new com.csc.fsg.nba.exception.NbaBaseException("Cannot create web service for "  + this.getClass().getName());
	}

	/**
	 * @return the function
	 */
	//APSL4010 New method
	public Function getFunction() {
		return function;
	}

	/**
	 * @param function the function to set
	 */
	//APSL4010 New method
	public void setFunction(Function function) {
		this.function = function;
	}
	
	protected List<String> uriParamsList = new ArrayList<String>(); //NBLXA-2402(NBLXA-2476)

	//NBLXA-2402(NBLXA-2476) New Method
	public void addUriParam(String aParam) {
		uriParamsList.add(aParam);
	}
	
}
