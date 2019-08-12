package com.csc.fsg.nba.transaction.validation;

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

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.BusValidation;

/**
 * This class acts as entry point for all the transaction validations. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA094</td><td>Version 3</td><td>Transaction Validation</td></tr>
 * <tr><td>SPR1629</td><td>Version 4</td><td>Changed the bussiness function to upper case</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
 * <tr><td>NBA208-26</td><td>Version 7</td><td>Remove synchronized keyword from getLogger() methods</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */

public class NbaTransactionValidationFacade {
	
	private static NbaLogger logger = null;

	/**
	 * This is the entry point for validating the transaction for business processes.
	 * @param nbaTXLife the NbaTXLife instance
	 * @param nbaDst the NbaDst object
	 * @param user the NbaUserVO object
	 * @throws NbaBaseException 
	 */
	public static void validateBusinessProcess(NbaTXLife nbaTXLife, NbaDst nbaDst, NbaUserVO user, AccelProduct nbaProduct) throws NbaBaseException {//P2AXAL016
		try {
			validate(nbaTXLife, nbaDst, user, nbaProduct);//P2AXAL016
		} catch (NbaBaseException nbe) {
			getLogger().logDebug("Exception encountered while validating business process");
				throw nbe;
		}
	}


	/**
	 * This method finds out the business process from the nbaTXLife, passed as an argument.
	 * For this business process it finds out if any subset values are metioned in the nba configuration
	 * file. If this business process has a subset value of 900 or greater, the implemetation class is found for
	 * that subset value and validate method is invoked on that NbaTransactionValidation implementation class.
	 * The NbaTransactionValidationFactory intantiates and returns the implementation class for this subset.
	 * @param nbaTXLife the NbaTXLife instance
	 * @param nbaDst the NbaDst object
	 * @param user the NbaUserVO object
	 * @throws NbaBaseException throws the exception in case of any validation problem
	 * @see com.csc.fsg.nba.transaction.validation.NbaTransactionValidation 
	 * @see com.csc.fsg.nba.transaction.validation.NbaTransactionValidationFactory 
	 */
	private static void validate(NbaTXLife nbaTXLife, NbaDst nbaDst, NbaUserVO user, AccelProduct nbaProduct) throws NbaBaseException {//P2AXAL016
		//if the backend is not NBA return
		NbaConfiguration configuration = NbaConfiguration.getInstance();
		if (!NbaServerUtility.isDataStoreDB(nbaDst.getNbaLob(), user)) { //SPR1715 NBA077
			return;
		}
		//Get the business process from the nbaTXLife
		String busfunc = nbaTXLife.getBusinessProcess().toUpperCase(); //SPR1629
		//Find out the subset code for BusinessValidatons for this business process
		BusValidation configBusValidation = null; //ACN012
		try {
			configBusValidation = configuration.getBusValidation(busfunc); //ACN012
		} catch (Exception e) {
			return;
		}
		int count = configBusValidation.getSubsetCount(); //ACN012
		for (int i = 0; i < count; i++) { //ACN012
			int subsetCode = configBusValidation.getSubsetAt(i); //ACN012
			if(subsetCode >= 900){ //ACN012
				// If business code is greater than or equal to 900 invoke the transactionvalidation factory
				// to get the implementation instance and invoke validate method on it.
				try {
					NbaTransactionValidation transValidation = NbaTransactionValidationFactory.getTransactionValidatonImplementation(nbaTXLife, new Integer(subsetCode)); //ACN012
					transValidation.validate(nbaTXLife, nbaDst, user, nbaProduct);//P2AXAL016				
				}catch(NbaBaseException nbe) {
					throw nbe;
				}
			}
		}
	}
	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return NbaLogger instace for this class
	 */
	private static NbaLogger getLogger() { // NBA208-26
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaTransactionValidationFacade.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaTransactionValidationFacade could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

}
