package com.csc.fsg.nba.contract.calculations;
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

import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;

/**
 * NbaContractCalculator
 * <p>
 * NbaContractCalculator is the interface which supports calculations within nbA.
 * Calculations are performed using a VPMS model.  A VPMS model is created
 * for each unique type of calculation, i.e., Mode Premium, Surrender Charge.
 * A model, however, may contain multiple types of calulations. For example,
 * the Mode Premium model may calculate mode premium, annualized mode premium and
 * annual mode premium.  For each model that is added to nbA, an NbaContractCalculator
 * must be added to support setting up the data for performing the calculations. Each
 * of these calculators must implement this interface as it provides standardized
 * functionality needed by the calculator.
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA072</td><td>Version 3</td><td>Contract Calculations - Initial Development</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public interface NbaContractCalculator {
	/**
	 * This method will be implemented by each NbaContractCalculator
	 * to peform specific processing needed in order to obtain
	 * the calculated values.
	 * @return NbaCalculation contains the calculation results for each object
	 * @throws NbaBaseException
	 * @throws NbaVpmsException
	 */
	NbaCalculation calculate() throws NbaBaseException, NbaVpmsException;
	/**
	 * This method performs specific initialization for a calculator.
	 */
	void initialize() throws NbaBaseException;
	/**
	 * This method clears the debugging options for a calculator.
	 */
	void clear() throws NbaBaseException;  //NBA104
}
