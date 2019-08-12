package com.csc.fsg.nba.vpms; //NBA201

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.RemoveException;

import com.csc.dip.jvpms.core.IVpmsProduct;
import com.csc.dip.jvpms.core.RequestGetAttributeNames;
import com.csc.dip.jvpms.core.RequestSequence;
import com.csc.dip.jvpms.core.RequestSetAttribute;
import com.csc.dip.jvpms.core.VpmsProductFactory;
import com.csc.dip.jvpms.ejb.IVpmsProductHome;
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.dip.jvpms.runtime.base.VpmsException;
import com.csc.dip.jvpms.runtime.base.VpmsLoadFailedException;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaServiceLocator;

/**
 * This class provides the interface into the VPMS system by creating a reference
 * to a J-VPMS EJB that will be used by all the VPMS services.  In addition,
 * it retrieves data from the NbaConfiguration file and sets up the attributes
 * that are needed to call a VPMS model.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>NBA021</td><td>Version 2</td><td>Object Interactive Name Keeper</td></tr>
 * <tr><td>NBA022</td><td>Version 2</td><td>Support case manager views</td></tr>
 * <tr><td>NBA023</td><td>Version 2</td><td>Form Tracking and Decisioning</td></tr>
 * <tr><td>NBA058</td><td>Version 3</td><td>Upgrade to J-VPMS v1.50</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA033</td><td>Version 3</td><td>Companion Case and HTML Indexing Views</td></tr>
 * <tr><td>NBA010</td><td>Version 3</td><td>Hooks for Iterative Underwriting</td></tr>
 * <tr><td>NBA067</td><td>Version 3</td><td>Client Search</td></tr>
 * <tr><td>NBA068</td><td>Version 3</td><td>Inforce Payment</td></tr>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>NBA072</td><td>Version 3</td><td>Contract Calculations</td></tr>
 * <tr><td>NBA094</td><td>Version 3</td><td>Transaction Validation</td></tr>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>SPR1375</td><td>Version 3</td><td>Comments from VPMS model are not added to requirements when they are created during the requirements determination process.</td></tr>
 * <tr><td>SPR1273</td><td>Version 3</td><td>RIP does not update RQVN LOB field when ripping in temporary work items</td></tr>
 * <tr><td>SPR1770</td><td>Version 4</td><td>When requirements are first ordered and moved to the NBORDERD queue they should not be suspended initially.</td></tr>
 * <tr><td>SPR1778</td><td>Version 4</td><td>Remove duplicate resolution of OINK variables.</td></tr>
 * <tr><td>SPR1784</td><td>Version 4</td><td>The Temporary Requirement Work Item moves to NBEND queue even when there is no Requirement Work Item.</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>NBA098</td><td>Version 4</td><td>Work Completed</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACP001</td><td>Version 4</td><td>Constants defined for ACP001</td></tr>
 * <tr><td>ACP022</td><td>Version 4</td><td>Foreign Travel</td></tr>
 * <tr><td>ACP013</td><td>Version 4</td><td>Family History</td></tr>
 * <tr><td>SPR2034</td><td>Version 4</td><td>VPMS Model Names should be capitalized in the jvpmsSettings.xml file</td></tr>
 * <tr><td>ACP008</td><td>Version 4</td><td>IU Preferred Processing </td></tr>
 * <tr><td>ACP009</td><td>Version 4</td><td>Non Medical Screening</td></tr>
 * <tr><td>ACP007</td><td>Version 4</td><td>Medical Screening</td></tr>  
 * <tr><td>ACP006</td><td>Version 4</td><td>MIB Evaluation</td></tr>
 * <tr><td>ACP016</td><td>Version 4</td><td>Aviation Evaluation</td></tr>
 * <tr><td>ACP011</td><td>Version 4</td><td>Occupation Evaluation</td></tr>
 * <tr><td>ACP010</td><td>Version 4</td><td>nbA Automated Underwriting</td></tr>
 * <tr><td>ACP017</td><td>Version 4</td><td>Key Person</td></tr>
 * <tr><td>NBA096</td><td>Version 4</td><td>Modification of Indexing view</td></tr>
 * <tr><td>ACP018</td><td>Version 4</td><td>Buy Sell</td></tr>
 * <tr><td>ACP012</td><td>Version 4</td><td>Owner Beneficiary Relationship</td></tr>
 * <tr><td>SPR2575</td><td>Version 5</td><td>Inconsistent results when retrieving Attributes from VPMS model</td></tr>
 * <tr><td>SPR3189</td><td>Version 6</td><td>Memory leak of j-VP/MS objects in NbaBusinessModelHelper and NbaServerUtility</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.vpms.NbaVpmsConstants
 * @see com.csc.fsg.nba.configuration.NbaConfiguration
 * @see com.csc.dip.jvpms.ejb.VpmsProductBean
 * @since New Business Accelerator - Version 1
 */

public class NbaVpmsAdaptor implements NbaVpmsConstants {
	public javax.naming.Context initialContext;
	public com.csc.dip.jvpms.ejb.IVpmsProductRemote product = null;
	private RequestSequence reqSeq = null; // NBA058
	protected static com.csc.fsg.nba.foundation.NbaLogger logger = null;
	public NbaVpmsVO surrogate = null;
	// NBA058 code deleted
	public java.lang.String vpmsModel = null;
	public java.lang.String messageDelimiter = null;
	public java.lang.String vpmsEntryPoint = null;
	protected NbaOinkDataAccess aNbaOinkDataAccess; //NBA021 new member
	protected Map skipAttributesMap; //NBA021 new member
	private NbaOinkRequest aNbaOinkRequest = null;
	//ACN012 Removed constants to NbaVpmsConstants file
    
	private StringBuffer debugBuffer = new StringBuffer();	//ACP008
	public IVpmsProduct productStandAlone;//NBLXA-2390
	public static boolean offLine =false;//NBLXA-2390
	
	
	public static boolean isOffLine() {
		return offLine;
	}
	public static void setOffLine(boolean offLine) {
		NbaVpmsAdaptor.offLine = offLine;
	}
	//NBA103 removed unused constructor
	/**
	 * NbaVpmsAdaptor calls the initializeVpmsAdaptor to set up the VPMS EJB and initialize
	 * the fields.
	 * @param aVpmsModel the name of the model to be executed
	 * @exception <code>NbaBaseException</code> 
	 * @exception <code>NbaVpmsException</code> 
	 */
	//NBA033 NEW METHOD
	public NbaVpmsAdaptor(String aVpmsModel) throws NbaVpmsException, NbaBaseException {
		super();
		initializeVpmsAdaptor(aVpmsModel);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (10/16/2002 5:14:02 PM)
	 * @return com.csc.fsg.nba.datamanipulation.NbaOinkRequest
	 */
	public com.csc.fsg.nba.datamanipulation.NbaOinkRequest getANbaOinkRequest() {
		return aNbaOinkRequest;
	}

	/**
	 * Initializes the NbaVpmsAdaptor by finding the Vpms EJB, establishing a relationship
	 * with it and then loading the VPMS model specified by <code>aVpmsModel</code>.
	 * @param aVpmsModel the name of the model to be loaded into memory
	 * @throws VpmsLoadFailedException 
	 * @exception <code>NbaBaseException</code> if unable to get the instance of service locator. 
	 * @exception <code>NbaVpmsException</code> if Create Exception occurs while creating the VPMS ejb or
	 * 											unable to instantiate a product or RemoteException occurs 
	 */
	//NBA033 NEW METHOD
	protected void initializeVpmsAdaptor(String aVpmsModel) throws NbaVpmsException, NbaBaseException {
		//initialize attributes
		skipAttributesMap = new HashMap();
		setVpmsModel(aVpmsModel);
		try {
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("NbaVpmsAdaptor for " + aVpmsModel + " beginning");
				getLogger().logDebug("NbaVpmsAdaptor isOffLine call ==>" + isOffLine() );
			} 
			if(isOffLine()){//NBLXA-2390
				productStandAlone = VpmsProductFactory.create(getVpmsModel());
				if (productStandAlone == null) {
					throw new NbaVpmsException(NbaVpmsException.VPMS_LOAD_ERROR + getVpmsModel(), NbaExceptionType.FATAL);
				}
			}else{
				NbaServiceLocator sl = NbaServiceLocator.getInstance();
				javax.ejb.EJBHome home = null;
				home = sl.getHome(NbaServiceLocator.JNDI_VPMS_BEAN, NbaServiceLocator.JNDI_VPMS_HOME);
				IVpmsProductHome vpmsHome = (IVpmsProductHome) home;
				product = vpmsHome.create(getVpmsModel());
				if (product == null) {
					throw new NbaVpmsException(NbaVpmsException.VPMS_LOAD_ERROR + getVpmsModel(), NbaExceptionType.FATAL);
				}
			}
		} catch (VpmsLoadFailedException vfe) {
			throw new NbaVpmsException(NbaVpmsException.VPMS_EXCEPTION + getVpmsModel(), vfe, NbaExceptionType.FATAL);
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException(NbaVpmsException.VPMS_EXCEPTION + getVpmsModel(), re, NbaExceptionType.FATAL);
		} catch (CreateException ce) {
			throw new NbaVpmsException(NbaVpmsException.VPMS_EXCEPTION + getVpmsModel(), ce, NbaExceptionType.FATAL);
		}
	}


	/**
	 * NbaVpmsAdaptor calls the initializeVpmsAdaptor to set up the VPMS EJB and initialize
	 * the fields.  It also initializes the NbaOinkDataAccess object.
	 * @param aNbaOinkDataAccess an NbaOinkDataAccess populated with data from the NbaLob, XML103
	 *                   (optional) and XML203 (optional) objects
	 * @param aVpmsModel the name of the model to be executed
	 * @exception <code>NbaBaseException</code> 
	 * @exception <code>NbaVpmsException</code> 
	 */
	//NBA021 new Constructor added to make use of OINK 
	public NbaVpmsAdaptor(NbaOinkDataAccess newNbaOinkDataAccess, String aVpmsModel) throws NbaVpmsException, NbaBaseException {
		super();
		initializeVpmsAdaptor(aVpmsModel);
		// NBA033 CODE DELETED
		setOinkSurrogate(newNbaOinkDataAccess);
		// NBA033 CODE DELETED
	}

	/**
	 * Answers the EJBs context
	 * @return <code>javax.naming.Context</code>
	 */
	public javax.naming.Context getInitialContext() {
		return initialContext;
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaVpmsAdaptor.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaVpmsAdaptor could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Answers the message delimiter used to separate messages/errors
	 * @return <code>java.lang.String</code> the delimiter
	 */
	public java.lang.String getMessageDelimiter() {
		return messageDelimiter;
	}
	/**
	 * This method returns an instance of <code>NbaOinkDataAccess</code>
	 * @return com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
	 */
	//NBA021 new Method
	public NbaOinkDataAccess getOinkSurrogate() {
		return aNbaOinkDataAccess;
	}
	/**
	 * Answers the product member.
	 * @return <code>com.csc.dip.jvpms.ejb.IVpmsProductRemote</code> the VPMS model being executed
	 */
	public com.csc.dip.jvpms.ejb.IVpmsProductRemote getProduct() {
		return product;
	}
	/**
	 * This method invokes the VPMS model to get the results of the
	 * model's processing.  
	 * @param reqSeq Request Sequence
	 * @return <code>java.util.List</code> the result of the  VPMS model.
	 * @exception RemoteException thrown if an exception occurs when trying to invoke the methods on 
	 * 			  VPMS ejb.			
	 * @exception NbaVpmsException thrown if an error occurs while executing vpms model.
	 */
	// NBA072 NEW METHOD
	public List getResults(RequestSequence reqSeq) throws RemoteException, NbaVpmsException {
		try {
			List resultList = (List) product.processRequest(reqSeq);
			return resultList;
		} catch (VpmsException ve) {
			throw new NbaVpmsException(ve);
		} 
	}
	/**
	 * This method invokes the VPMS model to get the results of the
	 * model's processing.  It first invokes the setVpmsAttributes() to
	 * dynamically set up the attributes for the model and then executes
	 * the appropriate entry point for the model.  The results are returned
	 * to the calling class.<p>
	 * Minimal parsing of the result is conducted in this method as it is
	 * assumed the calling class will parse the results.  This method throws
	 * an exception if an error occurs, but does not validate the results of
	 * the VPMS model.
	 * @return <code>com.csc.dip.jvpms.core.VpmsComputeResult</code> the result of the 
	 *         VPMS compute.
	 * @exception RemoteException thrown if an exception occurs when trying to invoke the methods on 
	 * 			  VPMS ejb.			
	 * @exception NbaVpmsException thrown if an error occurs when executing vpms model.
	 */
	public VpmsComputeResult getResults() throws java.rmi.RemoteException, NbaVpmsException {
		try {
			//NBA021 Begin
			//NBA058 code deleted
			oinkVpmsAttributes();
			
			//NBA058 code deleted
			//NBA021 end
			//NBA058 Begin  - have to set up to use request object here
			int computeIndex = reqSeq.addCompute(getVpmsEntryPoint());
			List resultList;
			if(isOffLine()){//NBLXA-2390
				resultList = (List) productStandAlone.processRequest(reqSeq);
			}else{
				resultList = (List) product.processRequest(reqSeq);
			}
			// NBA058 End
			VpmsComputeResult aResult = (VpmsComputeResult) resultList.get(computeIndex); // NBA058
			boolean debugEnabled = getLogger().isDebugEnabled();//SPR3290
			switch (aResult.getReturnCode()) {
				case 0 :
					if (debugEnabled) { // NBA027 SPR3290
						debugBuffer.append("VPMS Model ");//SPR3290
						debugBuffer.append(getVpmsModel());//SPR3290
						debugBuffer.append(" executed properly");//ACP008 SPR3290
					} // NBA027
					break;
				case 1 :
					if (debugEnabled) { // NBA027 SPR3290
						//begin SPR3290
					    debugBuffer.append("\nVPMS Model ");
						debugBuffer.append(getVpmsModel());
						debugBuffer.append(" has return code of 1");
						constructMessage(debugBuffer, aResult);
						//end SPR3290
						getLogger().logDebug(debugBuffer.toString());//ACP008
					} // NBA027
					break;
				case -1 :
					if (debugEnabled) { // NBA027 SPR3290
					    //begin SPR3290
					    debugBuffer.append("\nVPMS Model ");
						debugBuffer.append(getVpmsModel());
						debugBuffer.append(" has return code of -1");
						constructMessage(debugBuffer, aResult);
						//end SPR3290						
						getLogger().logDebug(debugBuffer.toString());//ACP008
					} // NBA027
					throw new NbaVpmsException(NbaVpmsException.VPMS_EXCEPTION + getVpmsModel() + " (return code -1)");
				default :
					if (debugEnabled) { // NBA027 SPR3290
					    //begin SPR3290
					    debugBuffer.append("\nVPMS Model ");
						debugBuffer.append(getVpmsModel());
						debugBuffer.append(" has unknown return code");
						constructMessage(debugBuffer, aResult);
						//end SPR3290
						getLogger().logDebug(debugBuffer.toString());//ACP008
					} // NBA027
					throw new NbaVpmsException(NbaVpmsException.VPMS_EXCEPTION + getVpmsModel() + " (unknown return code)");
			}
			if (debugEnabled) { // NBA027 ACP008 SPR3290
				//begin SPR3290
			    debugBuffer.append("Model: ");
				debugBuffer.append(getVpmsModel());
				debugBuffer.append("/EntryPoint: ");
				debugBuffer.append(getVpmsEntryPoint());
				debugBuffer.append("/Result: ");
				debugBuffer.append(aResult.getResult());
				//end SPR3290
				getLogger().logDebug(debugBuffer.toString());//ACP008
				debugBuffer = new StringBuffer();//SPR3290
			} // NBA027
			return aResult;
		} catch (VpmsException ve) {
			throw new NbaVpmsException(ve);
		}
	}
	/**
	 * This method returns a Map of attibutes which not be resolved by OINK
	 * @return java.util.Map
	 */
	//NBA021 new Method 
	public Map getSkipAttributesMap() {
		return skipAttributesMap;
	}
	/**
	 * Answers the surrogate member.
	 * @return <code>NbaVpmsVO</code>
	 */
	public NbaVpmsVO getSurrogate() {
		return surrogate;
	}
	/**
	 * Answers the entry point for the VPMS Model
	 * @return <code>java.lang.String</code> the entry point
	 */
	public java.lang.String getVpmsEntryPoint() {
		return vpmsEntryPoint;
	}
	/**
	 * Answers the name of the VpmsModel
	 * @return <code>java.lang.String</code>
	 */
	public java.lang.String getVpmsModel() {
		return vpmsModel;
	}
	/**
	 * This method initializes the VPMS attributes with values from the
	 * <code>NbaVpmsVO</code>.  
	 * <P>After the VPMS attributes are retrieved from the VPMS model, 
	 * it parses the <code>NbaVpmsVO</code> data members and finds the 
	 * ones that are used for the VPMS model.  
	 * The value of the member is then used to set the VPMS attribute value.
	 * The parsing process occurs twice; once with the members of the <code>NbaVpmsVO</code> 
	 * and once with the members of the superclass, <code>NbaLob</code>.
	 * @exception RemoteException if an error occurs when trying to set the
	 *            product attribute
	 * @exception NbaVpmsException thrown if errors occur when trying to
	 *            access members and functions through Reflection.
	 */
	public void initializeVpmsAttributes() throws java.rmi.RemoteException, NbaVpmsException {
		boolean debugLogging = getLogger().isDebugEnabled(); // NBA027
		if (debugLogging) { // NBA027
			getLogger().logDebug("beginning initializeVpmsAttributes for " + getVpmsModel());
		} // NBA027
		try {		// NBA058 
			RequestGetAttributeNames attRequest = new RequestGetAttributeNames(); // NBA058
			List attNames = new ArrayList(); //SPR2575
			attNames.addAll((List) product.processRequest(attRequest)); //SPR2575
			//	NBA058 Code deleted
			for (int x = 0; x < NbaVpmsAdaptor.VPMS_DELIMITER.length; x++) {
				RequestSetAttribute setReq = new RequestSetAttribute("A_DELIMITER[" + String.valueOf(x) + "]", NbaVpmsAdaptor.VPMS_DELIMITER[x]); // NBA058
				product.processRequest(setReq);  // NBA058
			}
			Method[] meths = surrogate.getClass().getDeclaredMethods();
			String memberValue;
			// this processes the NbaVpmsVO members
			for (int i = 0; i < attNames.size(); i++) {
				String attributeName = ((String) attNames.get(i)).substring(2);
				for (int x = 0; x < meths.length; x++) {
					memberValue = null;
					if (meths[x].getName().startsWith("get")) {
						String methodName = meths[x].getName().substring(3);
						if (attributeName.equalsIgnoreCase(methodName)) {
							try {
								memberValue = (String) meths[x].invoke(surrogate, null);
								if (memberValue != null) {
									memberValue = memberValue.trim();
								}
							} catch (InvocationTargetException ie) {
								memberValue = "0";
							} catch (IllegalArgumentException iae) {
								throw new NbaVpmsException("error", iae);
							} catch (Throwable t) {
								memberValue = "0";
							}
							RequestSetAttribute setReq = new RequestSetAttribute((String) attNames.get(i), memberValue); // NBA058
							product.processRequest(setReq);  // NBA058
							if (debugLogging) { // NBA027
								getLogger().logDebug("setVpmsAttribute: method=" + meths[x].getName() + "/value = " + memberValue);
							} // NBA027
						}
					}
				}
			}
			// This processes the NbaLob (superclass) members
			meths = surrogate.getClass().getSuperclass().getDeclaredMethods();
			for (int i = 0; i < attNames.size(); i++) {
				String attributeName = ((String) attNames.get(i)).substring(2);
				for (int x = 0; x < meths.length; x++) {
					memberValue = null;
					if (meths[x].getName().startsWith("get")) {
						String methodName = meths[x].getName().substring(3);
						if (attributeName.equalsIgnoreCase(methodName)) {
							try {
								Object objVal = meths[x].invoke(surrogate, null);
								if (objVal instanceof String) {
									memberValue = (String) objVal;
								} else {
									memberValue = String.valueOf(objVal);
								}
								if (memberValue != null) {
									memberValue = memberValue.trim();
								}
							} catch (InvocationTargetException ie) {
								throw new NbaVpmsException("error", ie);
							} catch (IllegalArgumentException iae) {
								throw new NbaVpmsException("error", iae);
							} catch (Throwable t) {
								memberValue = "0";
							}
							RequestSetAttribute setReq = new RequestSetAttribute((String) attNames.get(i), memberValue); // NBA058
							product.processRequest(setReq); // NBA058
							if (debugLogging) { // NBA027
								getLogger().logDebug("setVpmsAttribute(Lob): method=" + meths[x].getName() + "/value = " + memberValue);
							} // NBA027
						}
					}
				}
			}
		} catch (VpmsException ve) {
			throw new NbaVpmsException(ve);
		}
	}

	/**
	 * Loads a vpms model
	 * @exception RemoteException if an error occurs when calling a method on VPMS ejb.
	 * @exception NbaVpmsException thrown if errors occur while loading the VPMS model,
	 */
	//NBA033 NEW METHOD
	public void loadVpmsModel() throws NbaVpmsException, NbaBaseException {
		try {
			product.load(getVpmsModel());
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException(NbaVpmsException.VPMS_EXCEPTION + getVpmsModel(), re, NbaExceptionType.FATAL);
		} catch (VpmsLoadFailedException vlfe) {
			throw new NbaVpmsException(NbaVpmsException.VPMS_EXCEPTION + getVpmsModel(), vlfe, NbaExceptionType.FATAL);
		}
	}

	/**
	 * This method initializes the VPMS attributes using <code>NbaOinkDataAccess</code>.
	 * <P>After the VPMS attributes are retrieved from the VPMS model, 
	 * it initiates a request to <code>NbaOinkDataAccess</code> to resolve
	 * ones that are used for the VPMS model.   
	 * @exception RemoteException if an error occurs when trying to set the
	 *            product attribute
	 * @exception NbaVpmsException thrown if errors occur when trying to
	 *            access members and functions through Reflection.
	 */
	//NBA021 new Method - This method uses OINK to resolve attributes
	public void oinkVpmsAttributes() throws java.rmi.RemoteException, NbaVpmsException {
		boolean debugLogging = getLogger().isDebugEnabled(); // NBA027
		if (debugLogging) { // NBA027
			debugBuffer.append("\nNbaVpmsAdaptor beginning oinkVpmsAttributes for model " + getVpmsModel() + "\n"); //SPR1778, ACP008
			debugBuffer.append("Model entry point = " + getVpmsEntryPoint() + "\n");//ACP008
		} // NBA027
		try {  // NBA058
			//product.processRequest(attrReq) returns a cached instance of Attributes.
			//Therefore we clone the List so that we do not write into the VPMS cache.
			RequestGetAttributeNames attrReq = new RequestGetAttributeNames();  // NBA058
			List attNames = new ArrayList(); //SPR2575
			if(isOffLine()){//NBLXA-2390
				attNames.addAll((List) productStandAlone.processRequest(attrReq)); //SPR2575
			}else{
				attNames.addAll((List) product.processRequest(attrReq)); //SPR2575
			}
			//NBA044 start
			if(debugLogging){
				//APSL4540 commented below line of code
				//debugBuffer.append("\n" + getVpmsModel() + " : " + attNames + "\n"); //APSL4540
			}
			//NBA044 end
			if (reqSeq == null) {   // NBA058
				reqSeq = new RequestSequence();  // NBA058
			}  // NBA058
			for (int x = 0; x < NbaVpmsAdaptor.VPMS_DELIMITER.length; x++) {
				reqSeq.addSetAttribute(A_DELIMITER + "[" + String.valueOf(x) + "]", NbaVpmsAdaptor.VPMS_DELIMITER[x]);  // NBA058, NBA072
			}
			//A_Delimiter is now case sensitve - temporary
			attNames.remove(A_DELIMITER);  //remove the Attribute from the list, it has already been resolved // NBA072  
			//For attributes that can't be resolved by Oink, their values must be resolved prior to invoking the getResults method
			Object[] keys = getSkipAttributesMap().keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				//ACN012 Begin
				if( skipAttributesMap.get(keys[i]) instanceof String[]) {
					String stringArray[] = (String[])skipAttributesMap.get(keys[i]);
					for (int j = 0; j < stringArray.length; j++) {
						//ACP008 begin
						if (j == 0) {
							reqSeq.addSetAttribute((String) keys[i], stringArray[j]);
						} else {
							reqSeq.addSetAttribute((String) keys[i]	+ "[" + String.valueOf(j) + "]", stringArray[j]);
						}
						if (debugLogging) {
							debugBuffer.append("NbaVpmsAdaptor setting deOINK value " + (String) keys[i] + "[" + String.valueOf(j) + "]" + "=" + stringArray[j]);
							debugBuffer.append("\n");
						}
						//ACP008 end
					}
				} else {
					reqSeq.addSetAttribute((String) keys[i], (String) skipAttributesMap.get(keys[i]));  // NBA058
					//ACP008 begin
					if (debugLogging) {
						debugBuffer.append("NbaVpmsAdaptor setting deOINK value " + (String)keys[i] + "=" + (String) skipAttributesMap.get(keys[i]));
						debugBuffer.append("\n");
					}
					//ACP008 end
				}
				//ANC012 END
				attNames.remove(keys[i].toString().toUpperCase()); 
				//remove the Attribute from the list, it has already been resolved
			}
			if (aNbaOinkRequest == null) {
				aNbaOinkRequest = new NbaOinkRequest();
			}
			//resolve rest of the attributes using OINK
			String[] oinkValues; //P2AXL028
			for (int i = 0; i < attNames.size(); i++) {
				// NBA072 BEGIN
				// Since we will be using non-nbA created models for calculations,
				// add this test to strip off the standard A_ when it exists;
				// otherwise, use the variable as retrieved from the model
				String attName =(String)attNames.get(i);////P2AXAL028
				if(!attName.startsWith("C_")){//P2AXAL028
					if(attName.startsWith("A_")) {
						aNbaOinkRequest.setVariable(attName.substring(2));
					} else{
						aNbaOinkRequest.setVariable(attName);
					}
					// NBA072 END
					try {
						oinkValues = aNbaOinkDataAccess.getStringValuesFor(aNbaOinkRequest); //P2AXAL028
						//Begin P2AXAL028 
							if(aNbaOinkRequest.isParseMultiple()){
								for (int j = 0; j < oinkValues.length; j++) {
									if (j == 0) {
										reqSeq.addSetAttribute(attName, oinkValues[j]);
									} else {
										reqSeq.addSetAttribute(attName	+ "[" + String.valueOf(j) + "]", oinkValues[j]);
									}
					 				if (debugLogging) {
										debugBuffer.append("NbaVpmsAdaptor setting " + attName + "[" + String.valueOf(j) + "]" + "=" + oinkValues[j]);
										debugBuffer.append("\n");
									}
								}
								aNbaOinkRequest.setParseMultiple(false);
								//Add the count attribute for the multiple values
								String countAtt = createCountAttribute(attName);
								reqSeq.addSetAttribute(countAtt, String.valueOf(oinkValues.length));
								if (debugLogging) {
									debugBuffer.append("NbaVpmsAdaptor setting " + countAtt + "=" + oinkValues.length); 
									debugBuffer.append("\n");
								}
							}else if(oinkValues.length > 0){
							reqSeq.addSetAttribute(attName, oinkValues[0]);  // NBA058 NBA064  SPR1778
							if (debugLogging) { // NBA027
								debugBuffer.append("NbaVpmsAdaptor setting " + attName + "=" + oinkValues[0]); //SPR1778, ACP008
								debugBuffer.append("\n");//ACP008
							} // NBA027
						}else if(oinkValues.length == 0){
							reqSeq.addSetAttribute(attName, "");
							if (debugLogging) {
								debugBuffer.append("NbaVpmsAdaptor setting " + attName + "=" + "");
								debugBuffer.append("\n");
							}
						}
						//End P2AXAL028
					} catch (NbaBaseException e) {
						if (!e.isWarning()) {    //ALII2041
							getLogger().logInfo("NbaVpmsAdaptor could not resolve:" + attNames.get(i).toString()); //SPR1778
						}   //ALII2041
					}
				}
			}

			if (debugLogging) { // NBA027
				//APSL4540 start
				if(!NbaVpmsConstants.WORKITEMIDENTIFICATION.equalsIgnoreCase(getVpmsModel())
						&& !NbaVpmsConstants.CORRESPONDENCE.equalsIgnoreCase(getVpmsModel())) { 
					getLogger().logDebug(debugBuffer.toString()); //ACP008
					getLogger().logDebug("NbaVpmsAdaptor end oinkVpmsAttributes for model " + getVpmsModel()); //SPR1778
				}
				//APSL4540 end
				debugBuffer = new StringBuffer();	//SPR2575
			} // NBA027
		} catch (VpmsException ve) {
			throw new NbaVpmsException(ve);
		}
	}
	/**
	 * This method removes references to the VPMS EJB.
	 * @exception RemoteException  
	 * @exception NbaVpmsException thrown if errors occur when trying to remove VPMS ejb.
	 */
	public void remove() throws java.rmi.RemoteException, NbaVpmsException {
		try {
			if(isOffLine()){//NBLXA-2390
				productStandAlone.close();
			}else{
				product.remove();
			}
			// SPR3189 - code deleted
		} catch (RemoveException re) {
			throw new NbaVpmsException(NbaVpmsException.VPMS_EXCEPTION + getVpmsModel(), re);
		} finally { // SPR3189
			cleanup(); // SPR3189
		}
	}

	/**
	 * This methods calls  sets vpms model attributes by calling oinkVpmsAttributes
	 * and then returns the RequestSequence
	 * @return RequestSequence reqSeq
 	 * @exception RemoteException
	 * @exception NbaVpmsException
	 */
	//NBA072 new Method - This method uses OINK to resolve attributes
	public RequestSequence setAttributes() throws RemoteException, NbaVpmsException {
		oinkVpmsAttributes();
		return reqSeq;
	}
	/**
	 * Sets NbaOinkRequest
	 * @param newANbaOinkRequest com.csc.fsg.nba.datamanipulation.NbaOinkRequest
	 */
	public void setANbaOinkRequest(com.csc.fsg.nba.datamanipulation.NbaOinkRequest newANbaOinkRequest) {
		aNbaOinkRequest = newANbaOinkRequest;
	}
	/**
	 * Initializes the EJB context.
	 * @param newInitialContext a new context
	 */
	public void setInitialContext(javax.naming.Context newInitialContext) {
		initialContext = newInitialContext;
	}
	/**
	 * Sets the message delimiter
	 * @param newMessageDelimiter a new delimiter
	 */
	public void setMessageDelimiter(java.lang.String newMessageDelimiter) {
		messageDelimiter = newMessageDelimiter;
	}
	/**
	 * Sets the OINK surrogate. 
	 * @param newNbaOinkDataAccess An NbaOinkDataAccess instance
	 */
	//NBA021 new Method
	public void setOinkSurrogate(NbaOinkDataAccess newNbaOinkDataAccess) {
		aNbaOinkDataAccess = newNbaOinkDataAccess;
	}
	/**
	 * Sets the product.
	 * @param newProduct a new VPMS model
	 */
	public void setProduct(com.csc.dip.jvpms.ejb.IVpmsProductRemote newProduct) {
		product = newProduct;
	}
	/**
	 * Specify a map of attributes that should not be resolved by Oink. 
	 * @param newSkipAttributesMap java.util.Map
	 */
	//NBA021 new Method
	public void setSkipAttributesMap(Map newSkipAttributesMap) {
		skipAttributesMap = newSkipAttributesMap;
	}
	/**
	 * Sets the surrogate.
	 * @param newSurrogate the NbaVpmsVO value object
	 */
	public void setSurrogate(NbaVpmsVO newSurrogate) {
		surrogate = newSurrogate;
	}
	/**
	 * Sets the VPMS entry point for the model.
	 * @param newVpmsEntryPoint java.lang.String
	 */
	public void setVpmsEntryPoint(java.lang.String newVpmsEntryPoint) {
		vpmsEntryPoint = newVpmsEntryPoint;
	}
	/**
	 * Sets the VPMS Model name.
	 * @param newVpmsModel java.lang.String
	 */
	public void setVpmsModel(java.lang.String newVpmsModel) {
		vpmsModel = newVpmsModel;
	}

	/**
	 * Performs object cleanup in preparation for garbage collection
	 */
	//SPR3189 New Method
	protected void cleanup() {
		initialContext = null;
		product = null;
		reqSeq = null;
		surrogate = null;
		vpmsModel = null;
		messageDelimiter = null;
		vpmsEntryPoint = null;
		aNbaOinkDataAccess = null;
		skipAttributesMap = null;
		aNbaOinkRequest = null;	    
	}

	// SPR3290 code deleted
	
	// SPR3290 New Method
	/**
	 * This method construct message for VPMS model results
	 * @param debugBuffer string buffer
	 */
	protected void constructMessage (StringBuffer debugBuffer, VpmsComputeResult aResult){
	    debugBuffer.append("\naRefField: ");
	    debugBuffer.append(aResult.getRefField());
		debugBuffer.append("\naMessage: ");
		debugBuffer.append(aResult.getMessage());
		debugBuffer.append("\naName: ");
		debugBuffer.append(aResult.getName());
		debugBuffer.append("\naResult: ");
		debugBuffer.append(aResult.getResult());
		debugBuffer.append("\naReturnCode: ");
		debugBuffer.append(aResult.getReturnCode()); 
	}
	
	//New Method P2AXAL028
	private String createCountAttribute(String att){
		StringBuffer attName = new StringBuffer(att);
		int index = attName.indexOf("_",attName.indexOf("_")+1);
		if(index < 0){
			index = attName.length();
		}
		attName.insert(index,"COUNT");
		attName.deleteCharAt(0);
		attName.insert(0,"C");
		return attName.toString();
	}
}
