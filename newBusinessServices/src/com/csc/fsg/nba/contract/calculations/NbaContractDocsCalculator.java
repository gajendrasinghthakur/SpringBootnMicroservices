package com.csc.fsg.nba.contract.calculations;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group®.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.rmi.RemoteException;

import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaTXLife;
/**
 * NbaContractDocsCalculator calculates the values needed for policy pages, 
 * summaries, and illustrations. The VPMS model calculates premium using premium
 * rate tables based on issue age and duration.  Ten and twenty year net payment 
 * and surrender cost indices are calculated for policy summaries.  Sub models 
 * are used to store and retrieve base premium rates, rider and benefit rates.  
 * The key to the rate tables include Plan Desc Search Key, Rate Type, Sex, 
 * Rate Class, Band, Issue Age, and Duration.
 * Model calculations: 
 * 		Rider or Benefit Calculations
 * 		Initial Premium, Renewal Premium
 * 		Current values
 * 			Annual Premium
 * 		Guaranteed values
 * 			Annual Premium
 * 			Death Benefit
 * 		Guaranteed Cash Values, RPU, and ETI values (if applicable)
 * 		Net Payment Cost Index (Projected and Guaranteed)
 * 		Surrender Cost Index (Projected and Guaranteed)
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */
public class NbaContractDocsCalculator extends NbaContractCalculatorCommon {
	/**
	 * NbaContractDocsCalculator constructor.
	 */
	protected NbaContractDocsCalculator(String aCalcType, NbaTXLife aNbaTxLife) throws NbaBaseException {
		super(aCalcType, aNbaTxLife);
	}
	protected void setCalculatorAttributes() {
		//Not used
	}
	/**
	 * Calculate the values needed for level term policy pages, summaries, and illustrations. 
	 * @return result of the calculation
	 */
	public NbaCalculation calculate() throws NbaBaseException {
		try {
			if (noModelForCalc()) {
				getCalcReturnValues().setCalcResultCode(TC_RESCODE_SUCCESS); //No calculation needed 
			} else {
				setDynamicAttribute(ATR_CALLING_PROGRAM, NbaContractDocsCalculator.class.getName());
				setModelAttributes(); //Set the attributes 
				addPropertiesToBeComputed(); //Add the properties to be computed
				calculateResults(); //Caclulate and marshall the results
				getCalcReturnValues().setCalcResultCode(TC_RESCODE_SUCCESS);
			}
		} catch (RemoteException e) {
			throw new NbaBaseException(e);
		}
		return getCalcReturnValues();
	}

}
