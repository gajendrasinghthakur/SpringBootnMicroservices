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
 
import com.csc.dip.jvpms.core.RequestSequence;
import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Coverage;

/**
 * NbaSurrenderChargeCalculator
 * A VPMS model, determined by the NbaCalculationsControlModel, is called to
 * calculate the surrender charge targets.  The attributes to be passed to the
 * model will be set using values retrieved from an NbaTXLife, as well as
 * values retrieved from other sources.
 * For each calculated value for an object, the object id and resulting values
 * will be added to an <code>NbaCalculation</code> object.  Once all values
 * have been retrieved, the <code>NbaCalculation</code> object will be returned
 * to the calling program. 
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
public class NbaSurrenderChargeCalculator extends NbaContractCalculatorCommon implements NbaContractCalculator{
	/**
	 * Constructor for NbaSurrenderChargeCalculator.
	 * @param aCalcType
	 * @param aNbaTxLife
	 * @throws NbaBaseException
	 */
	public NbaSurrenderChargeCalculator(String aCalcType, NbaTXLife aNbaTxLife) throws NbaBaseException {
		super(aCalcType, aNbaTxLife);
	}
	/**
	 * Performs the calculation integration for Surrender Charge for a contract
	 * @return result of the calculation
	 */
	public NbaCalculation calculate() throws NbaBaseException, NbaVpmsException {
		try {
			if (noModelForCalc()) {
				createErrorResultForNoModel();
			} else {
				setDynamicAttribute(ATR_CALLING_PROGRAM, NbaSurrenderChargeCalculator.class.getName());
				setDynamicAttribute(ATR_GCP_CALC_TYPE, getCalcType());
				processCoverageObjects();
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

	/**
	 * This method initializes the skip attributes map for the VPMS model and
	 * then calls the NbaVpmsAdaptor.setAttribute() method to initialize the
	 * OINK variables.  That method returns a <code>RequestSequence</code> object
	 * with the all attributes set.
	 * @param calcType a String value representing the calculation type to be performed
	 */
	public void setCalculatorAttributes() {
		setCalcReqSeq(new RequestSequence());
	}

	/**
	 * Perform the surrender charge target calculations to store on the base coverage.
	 */
	private void processCoverageObjects() throws Exception {
		Coverage coverage = getNbaTxLife().getPrimaryCoverage();
		if (coverage!= null) {
			setXmlObject(XO_COVERAGE);
			setCalculatorAttributes();
			setSpecificModelAttributes();
			initializeAttributesForObject();
			initializeProperties(coverage.getId());
		}
	}

}	
