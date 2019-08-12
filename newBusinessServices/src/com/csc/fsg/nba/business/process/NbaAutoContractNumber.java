package com.csc.fsg.nba.business.process;

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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.core.RequestCompute;
import com.csc.dip.jvpms.core.RequestSequence;
import com.csc.dip.jvpms.core.RequestSetAttribute;
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.dip.jvpms.runtime.base.VpmsException;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsAutoContractNumberData;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/**
 * NbaAutoContractNumber is an implentation of Auto contract numbering logic and generates a contract number
 * using a seed number from the database and a VPMS model. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA021</td><td>Version 2</td><td>Data Resolver</td></tr> 
 * <tr><td>SPR1234</td><td>Version 3</td><td>Code cleanup</td></tr> 
 * <tr><td>NBA058</td><td>Version 3</td><td>Upgrade to J-VPMS version 1.5.0</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA092</td><td>Version 3</td><td>Architecture changes for phase 2c</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr>
 * <tr><td>AXAL3.7.34</td><td>AXA Life Phase 1</td><td>Contract Services</td></tr>
 * <tr><td>SPR3614</td><td>AXA Life Phase 1</td><td>JVPMS Memory leak in Auto Contract Numbering logic</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */

//SPR3614 added AutoContractNumber to implements clause
public class NbaAutoContractNumber implements com.csc.fsg.nba.foundation.NbaTableAccessConstants, AutoContractNumber { 
	/**Holds the Contract Number.*/
	protected static NbaAutoContractNumber instance = null; // SPR3290
	/**Holds the logger for logging errors.*/
	private static NbaLogger logger;
	//begin SPR3614
	private final static String A_SEQUENCE = "A_Sequence";
	private final static String A_SEEDNUMBER = "A_SeedNumber";
	private final static String A_ENVIRONMENT = "A_Environment";
	private final static String ERR_MSG = "Unable to create contract number";
	private final static String ERR_RUNTIME = "Runtime error";
	private final static String ERR_NO_LOGGER = "NbaAutoContractNumber could not get a logger from the factory.";
	private final static String MAX_SEED_CODE = "-1";
	//end SPR3614	
	/**
	 * Default constructor.
	 */
	public NbaAutoContractNumber() {}//SPR3614
	/**
	 * Generates the contract number for a case. This method invokes VPMS to get the sequence name to be used for the given contract type.
	 * It then perform a database query to get the next seed number for that sequence name and updates the table with the current seed number.   
	 * @param List contains an instance of <code>NbaOinkDataAccess</code> to hold the values for the case used to initialize the VPMS attributes
	 * @return String the generated contract number
	 * @throws NbaBaseException
	 **/
	// NBA021 NEW METHOD
	// SPR3290 - exception java.rmi.RemoteException no longer thrown
	// SPR3614 changed method signature, parameter type to List
	public String generateContractNumber(List inputData) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null; //SPR3614
		if(getLogger().isDebugEnabled()) { //SPR3290
		    getLogger().logDebug("NbaAutoContractNumber starting");
		} //SPR3290
		try {
			if (inputData == null || inputData.size() == 0 || !(inputData.get(0) instanceof NbaOinkDataAccess)) {//SPR3614
				throw new NbaBaseException(ERR_MSG); //SPR3614
			} //SPR3614
			vpmsProxy = new NbaVpmsAdaptor((NbaOinkDataAccess) inputData.get(0), NbaVpmsAdaptor.AUTOCONTRACTNUMBERING); //SPR3614
			Map deOinkMap = new HashMap();
			deOinkMap.put(A_SEEDNUMBER, getContractSeedNumber(vpmsProxy)); //SPR3614
			deOinkMap.put(A_ENVIRONMENT, NbaConfiguration.getInstance().getEnvironment());//SPR3614
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_CONTRACT_NUMBER); //SPR3614
			NbaVpmsAutoContractNumberData data = new NbaVpmsAutoContractNumberData(vpmsProxy.getResults());
			if (data.wasSuccessful()) {
				if (getLogger().isDebugEnabled()) { // NBA027
					getLogger().logDebug("NbaAutoContractNumber generated contract number " + data.getContractNumber());
				} // NBA027
				return data.getContractNumber();
			} //SPR3614
			throw new NbaBaseException(ERR_MSG + " - " + data.getResult().getMessage()); //SPR3614
		} catch (NbaBaseException e) {
			throw e;
		} catch (Throwable t) {
			throw new NbaBaseException(ERR_RUNTIME, t); //SPR3614
		//begin SPR3362
		} finally {
		    if(vpmsProxy != null){
		        try {
                    vpmsProxy.remove();
                } catch (RemoteException e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
		    }
		//end SPR3362
		}
	}
	/**
	 * Gets the seed number from the database by calling the VPMS model to
	 * get the sequence number and then calling the <code>getSequenceNumber</code> method
	 * to execute the query that retrieves the seed number from the database.
	 * @param vpmsProxy an instance of <code>NbaVpmsAdaptor</code> referring to auto contract numbering model. 
	 * @return String   contains the seed number
	 * @exception <code>NbaBaseException</code> is thrown if vpms call fails.
	 */
	// NBA021 NEW METHOD
	// SPR3290 - exception java.rmi.RemoteException no longer thrown
	// SPR3614 changed method signature - added vpmsProxy parameter
	protected String getContractSeedNumber(NbaVpmsAdaptor vpmsProxy) throws NbaBaseException {
		// before starting the VPMS calls, I must get a policy number from the database
		try {
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_SEQUENCE_NAME); //SPR3614
			NbaVpmsAutoContractNumberData data = new NbaVpmsAutoContractNumberData(vpmsProxy.getResults(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			if (data.wasSuccessful()) {
				// SPR1230 Begin
				RequestSetAttribute setReq = new RequestSetAttribute(A_SEQUENCE, data.getSequenceName()); // NBA058
				vpmsProxy.getProduct().processRequest(setReq);  // NBA058
				RequestCompute computeReq = new RequestCompute(NbaVpmsConstants.EP_GET_INCREMENT);  // NBA058 SPR3614
				VpmsComputeResult result = (VpmsComputeResult) vpmsProxy.getProduct().processRequest(computeReq);  // NBA058
				if (result.getReturnCode() == 0) {
					data.setIncrement(result.getResult());
				} else {
					throw new NbaDataAccessException("Increment for "	+ data.getSequenceName() + " not found in VPMS model");
				}
				// SPR1230 End
				return getSequenceNumber(data);
			} //SPR3614
				throw new NbaBaseException(data.getResult().getMessage());
		} catch (NbaBaseException e) {
			throw e;
		} catch (VpmsException ve) {  // NBA058
			throw new NbaBaseException(ve); // NBA058
		} catch (Throwable t) {
			throw new NbaBaseException(ERR_RUNTIME, t); //SPR3614
		}
	}
	/**
	 * Answers the instance of the class
	 * @return com.csc.fsg.nba.business.process.NbaAutoContractNumber
	 */
	public static NbaAutoContractNumber getInstance() {
		if (instance == null) {
			instance = new NbaAutoContractNumber();
		}
		return instance;
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() { //SPR3614
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaAutoContractNumber.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log(ERR_NO_LOGGER); //SPR3614
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Establishes a connection with the data and executes an SQL query to retrieve the next available seed number from the database.
	 * If maximum contract seed number exceeds, then an exception is thrown. Assumption here is that all the seed number rows have been
	 * pre-initialized into the database table from vpms model.  
	 * @param  data   values obtained from the VPMS model for sequence number retrieval/creation
	 * @return String contains the seed number
	 * @exception NbaDataAccessException is thrown if Maximum contract seed number exceeds 
	 */
	//SPR3614 removed NbaVpmsException from throws
	protected String getSequenceNumber(NbaVpmsAutoContractNumberData data) throws NbaDataAccessException { 
		NbaTableAccessor nts = new NbaTableAccessor();
		String contractSeed = null;
		//begin SPR3614
		contractSeed = nts.getSeedNumber(data.getSequenceName(), data.getIncrement()); // SPR1230 //NBA092
		if (MAX_SEED_CODE.equals(contractSeed)) {
			// maximum value exceeded... throw an exception
			throw new NbaDataAccessException("Maximum contract seed number value for " + data.getSequenceName() + " has been exceeded.");
			// SPR1230 END
		}
		//end SPR3614
		return contractSeed;
	}
	/**
	 * Call EIB to retrieve Policy Number.  Any error is added as an AWD Comment.
	 *
	 */
	//AXAL3.7.34 new method
	public String generateEIBContractNumber(NbaTXLife xml103, NbaDst nbaDst, NbaUserVO userVO) throws NbaBaseException {
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CONTRACT_SERVICE, userVO, xml103, nbaDst,
				null);
		NbaTXLife txLifeResult = (NbaTXLife) webServiceInvoker.execute();
		return txLifeResult.getPolicy().getPolNumber();
	}
	
}
