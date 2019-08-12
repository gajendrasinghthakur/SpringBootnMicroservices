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
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * NbaContractCalculationFactory
 * <p>
 * Implements a factory pattern for performing pending contract calculations.  The appropriate
 * calculator is created based on the specified calculation type.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA072</td><td>Version 3</td><td>Contract Calculations - Initial Development</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>NBA142</td><td>Version 6</td><td>Minimum Initial Premium</td></tr>
 * <tr><td>AXAL3.7.14</td><td>Version 6</td><td>Contract Print</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */

public class NbaContractCalculatorFactory implements NbaContractCalculationsConstants {
	public static NbaContractCalculator getCalculator(String aCalcType, NbaTXLife aNbaTxLife) throws NbaBaseException {
		if (aCalcType.equalsIgnoreCase(CALC_TYPE_MODE_PREMIUM)) {
			return new NbaModePremiumCalculator(aCalcType, aNbaTxLife);
		} else if(aCalcType.equalsIgnoreCase(CALC_TYPE_SURRENDER_CHARGE)) {
			return new NbaSurrenderChargeCalculator(aCalcType, aNbaTxLife);
		} else if(aCalcType.equalsIgnoreCase(CALC_TYPE_GUIDELINE_PREMIUM)) {
			return new NbaGuidelinePremiumCalculator(aCalcType, aNbaTxLife);
		//begin NBA100
		} else if (aCalcType.equalsIgnoreCase(CALC_TYPE_ALL_STD_MODES_PREMIUM)) {
			return new NbaAllStdModeslPremiumCalculator(CALC_TYPE_MODE_PREMIUM, aNbaTxLife); //override the calc type
		} else if (aCalcType.equalsIgnoreCase(CALC_CONTRACT_DOCS)) {
			return new NbaContractDocsCalculator(aCalcType, aNbaTxLife);  
		//end NBA100
		//begin NBA104
		} else if(aCalcType.equalsIgnoreCase(CALC_TYPE_NON_STANDARD_MODE_PREMIUM)) {
			return new NbaNonstandardModePremiumCalculator(aCalcType, aNbaTxLife);
		} else if(aCalcType.equalsIgnoreCase(CALC_TYPE_JOINT_EQUAL_AGE)) {
			return new NbaJointEqualAgeCalculator(aCalcType, aNbaTxLife);			
		} else if(aCalcType.equalsIgnoreCase(CALC_TYPE_PREMIUM_LOAD)) {
			return new NbaPremiumLoadCalculator(aCalcType, aNbaTxLife);			
		} else if(aCalcType.equalsIgnoreCase(CALC_TYPE_COMMISSION_TARGET)) {
			return new NbaCommissionTargetPremiumCalculator(aCalcType, aNbaTxLife);			
		} else if(aCalcType.equalsIgnoreCase(CALC_TYPE_RATE_UTIL)) {
			return new NbaRateUtilityCalculator(aCalcType, aNbaTxLife);			
		} else if(aCalcType.equalsIgnoreCase(CALC_TYPE_7PAY_PREMIUM)) {
			return new Nba7PayCalculator(aCalcType, aNbaTxLife);			
		} else if(aCalcType.equalsIgnoreCase(CALC_TYPE_MIN_NO_LAPSE_PREMIUM)) {
			return new NbaMinNoLapsePremCalculator(aCalcType, aNbaTxLife);
        //end NBA104	
		} else if(aCalcType.equalsIgnoreCase(CALC_TYPE_MINIMUM_INITIAL_PREMIUM)) {//NBA142
			return new NbaMinInitPremiumCalculator(aCalcType, aNbaTxLife);//NBA142		
		} else if(aCalcType.equalsIgnoreCase(CALC_AXA_CONTRACT_PRINT)) {//AXAL3.7.14
			return new NbaContractPrintCalculator(aCalcType, aNbaTxLife);//AXAL3.7.14		
		}
		
		throw new IllegalArgumentException(aCalcType);
	}
	public static NbaCalculation calculate(String aCalcType, NbaTXLife aNbaTxLife) throws NbaBaseException {
		NbaContractCalculator myCalculator = getCalculator(aCalcType, aNbaTxLife);  //NBA104
		// NBA104 deleted code
		try {  //NBA104
			myCalculator.initialize();
			return myCalculator.calculate();
		} finally {  //NBA104
			myCalculator.clear();  //NBA104
		}  //NBA104
	}
}
