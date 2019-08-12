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
 * 
 * *******************************************************************************<BR>
 */
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.configuration.ValidationClass;
/**
 * Transaction validation factory.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA094</td><td>Version 3</td><td>Transaction Validation</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaTransactionValidationFactory {
	/**
	 * Constructor for NbaContractValidationImplFactory.
	 */
	private NbaTransactionValidationFactory() {
		super();
	}

	/**
	 * This method intantiates the implementation class for NbaTransactionValidation. It finds out the implementation
	 * class name from nba configuration file based on the subset value. 
	 * @param nbaTXLife the NbaTXLife instance
	 * @param subset an integer that decides which implementation class will be intantiated 
	 * @throws NbaBaseException 
	 */
	public static NbaTransactionValidation getTransactionValidatonImplementation(NbaTXLife nbaTXLife, Integer subset) throws NbaBaseException {
		NbaTransactionValidation nbaTransactionValidationImpl = null;
		ValidationClass implClass = NbaConfiguration.getInstance().getValidationClass(subset.intValue(), nbaTXLife.getBackendSystem()); //ACN012
		//ACN012 CODE DELETED
		if (implClass == null) {
			postErrorForImplementationNotDefined("Configuration information missing for subset");
		}
		String baseImplName = implClass.getBaseimpl(); //ACN012
		if (baseImplName.length() > 0) {
			try {
				nbaTransactionValidationImpl = (NbaTransactionValidation)NbaUtils.classForName(baseImplName).newInstance();
			} catch (Exception e) {
				postErrorForImplementationNotDefined("Base implementation class not found " + baseImplName);
			}
		} else {
			postErrorForImplementationNotDefined("No implementation classes defined for subset");
		}
		return nbaTransactionValidationImpl;
	}

	/**
	 * Format the Error and thro NbaBaseException. 
	 * @param reason a String describing the error.
	 * @throws NbaBaseException.
	 */
	public static void postErrorForImplementationNotDefined(String reason) throws NbaBaseException {
		StringBuffer buf = new StringBuffer();
		buf.append("Transaction Validation implementation error: ");
		buf.append(reason);
		throw new NbaBaseException(buf.toString());
	}
}
