package com.csc.fsg.nba.contract.calculations;
/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
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
 
import com.csc.dip.jvpms.core.RequestSequence;
import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Life;


/**
 * NbaPremiumLoadCalculator
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */
public class NbaPremiumLoadCalculator extends NbaContractCalculatorCommon implements NbaContractCalculator{

	/**
	 * Constructor for NbaPremiumLoadCalculator. 
	 * @param aCalcType
	 * @param aNbaTxLife
	 * @throws NbaBaseException
	 */
	public NbaPremiumLoadCalculator(String aCalcType, NbaTXLife aNbaTxLife) throws NbaBaseException {
		super(aCalcType, aNbaTxLife);
	}
	/**
	 * Performs the calculation integration for Premium Load for a contract.
	 * @return result of the calculation
	 */
	public NbaCalculation calculate() throws NbaBaseException, NbaVpmsException {
		try {
			if (noModelForCalc()) {
				createErrorResultForNoModel();
			} else {
				setDynamicAttribute(ATR_CALLING_PROGRAM, NbaPremiumLoadCalculator.class.getName());
				setDynamicAttribute(ATR_GCP_CALC_TYPE, getCalcType());
				processLifeObject();
				calculateResults();
				getCalcReturnValues().setCalcResultCode(TC_RESCODE_SUCCESS);
			}
		} catch (NbaBaseException nbe) {
			throw nbe;
		} catch (Exception e) {
			throw new NbaBaseException(e);
		}
		return getCalcReturnValues();
	}

	protected void setCalculatorAttributes() {
		setCalcReqSeq(new RequestSequence());
	}
	/**
	 * Perform the premium load target calculations for base policy.
	 */
	private void processLifeObject() throws Exception {
		Life life = nbaTxLife.getLife();
		if (life == null) {
			return;
		}
		setXmlObject(XO_POLICY);  
		setCalculatorAttributes();
		setSpecificModelAttributes();
		initializeAttributesForObject();
		initializeProperties(life.getId());  
	}
}
