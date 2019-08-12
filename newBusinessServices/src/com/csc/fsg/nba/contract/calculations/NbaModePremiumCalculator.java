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

import java.util.List;

import com.csc.dip.jvpms.core.RequestSequence;
import com.csc.fsg.nba.contract.calculations.results.CalcProduct;
import com.csc.fsg.nba.contract.calculations.results.CalculationResult;
import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vpms.results.VpmsProp;


/**
 * NbaModePremiumCalculator
 * This class handles the calculations for Mode Premiums for a contract, 
 * and all coverages, riders, benefits and substandard ratings on that
 * contract (substandard ratings are also known as "extras" in the CyberLife
 * host system).  In addition, annualized premiums and "annual" premiums are
 * also calculated for and returned.
 * A VPMS model, Standard Mode Premium, is called to calculate the premium
 * values.  The attributes to be passed to the model will be set using values
 * retrieved from an NbaTXLife, as well as values retrieved from other sources.
 * Each time a value is calculated for an object, whether that object is a
 * Policy, Coverage, Rider, Benefit or SubstandardRating object, the object
 * id and resulting values will be added to an <code>NbaCalculation</code> object.
 * Once all values have been retrieved, the <code>NbaCalculation</code> object
 * will be returned to the calling proram. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA072</td><td>Version 3</td><td>Contract Calculations</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>SPR2590</td><td>Version 6</td><td>Proposed Table Substandard Ratings are not being excluded from premium calculations.</td></tr>
 * <tr><td>AXAL3.7.56</td><td>Version 6</td><td>Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaModePremiumCalculator extends NbaContractCalculatorCommon implements NbaContractCalculator {
	/**
	 * Initializes the calculator specific attributes for the VPMS calculation
	 * control model.
	 */
	protected void setCalculatorAttributes() {
		setCalcReqSeq(new RequestSequence());  //NBA104
		getCalcReqSeq().addSetAttribute(ATR_XML_OBJECT, getXmlObject());  //NBA104
	}
	/**
	 * Perform the mode premium calculations for each coverage/rider.
	 */
	// NBA104 change method signature to protected throwing Exception
	protected void processCoverageObjects() throws Exception {
		// NBA104 deleted code
		// for each coverage, we must calculate the mode premium
		// begin NBA104  
		if (getCoverages().size() > 0) {
			setXmlObject(XO_COVERAGE);
			processCoverageObjects(getCoverages());
		}
		if (getRiders().size() > 0) {
			setXmlObject(XO_RIDER);
			processCoverageObjects(getRiders());
		}
		// end NBA104
	}
	/**
	 * Perform the mode premium calculations for each coverge/rider.  By grouping
	 * the coverages and the riders separately, the number of VPMS calls can be
	 * minimized. 
	 * @param coverages List of coverages/riders
	 */
	// NBA104 New Method
	protected void processCoverageObjects(List coverages) throws Exception {
		setCalculatorAttributes();
		setSpecificModelAttributes();
		int covCount = coverages.size();
		for (int i = 0; i < covCount; i++) {
			Coverage coverage = (Coverage)coverages.get(i);
			getDynReqSeq().addSetAttribute(ATR_SPECIFIED_PHASE, coverage.getCoverageKey());
			initializeProperties(coverage.getId(), i);	// AXAL3.7.56
		}
	}
	/**
	 * Performs the calculation integration for Standard Mode Premium at the policy, coverage/rider,
	 * benefit and substandard ratings levels.
	 * @return result of the calculation
	 */
	public NbaCalculation calculate() throws NbaBaseException, NbaVpmsException {

		try {
			if (noModelForCalc()) { //NBA100
				createErrorResultForNoModel(); //NBA100
			} else { //NBA100				
				// NBA104 deleted code
				setDynamicAttribute(ATR_CALLING_PROGRAM, NbaModePremiumCalculator.class.getName()); //NBA104
				processPolicyObject();
				processCoverageObjects();
				processCovOptionObjects();
				processSubstandardRatingObjects();
				// begin NBA104
				calculateResults();
				if (getNbaTxLife().isPaymentModeSpecialFrequency()) {
					calculateNonStandardPremiums() ;
				}
				// end NBA104
				getCalcReturnValues().setCalcResultCode(TC_RESCODE_SUCCESS);	 //NBA100
			} //NBA100
			// begin NBA104
		} catch (NbaBaseException nbe) {
			throw nbe;
			// end NBA104
		} catch (Exception e) {
			throw new NbaBaseException(e);
		}
		return getCalcReturnValues(); //NBA100

		
	}
	/**
	 * Perform the mode premium calculation for each benefit.
	 */
	// NBA104 change method signature to protected throwing Exception
	protected void processCovOptionObjects() throws Exception {
		// begin NBA104
		setXmlObject(XO_COVOPTION);
		setCalculatorAttributes();
		setSpecificModelAttributes();
		// for each coverage, we must get the covOptions and
		// calculate the mode premium
		Life life = getNbaTxLife().getLife();
		if (life == null) {
			return;
		}
		int covCount = life.getCoverageCount();
		// end NBA104
		int covOptionElement = 0;
		for (int i = 0; i < getCovOptions().size(); i++) { //ALII1454
			//ALII1454 code deleted
				// begin NBA104
				CovOption covopt = (CovOption) getCovOptions().get(i); //ALII1454
				if (covopt == null || covopt.isActionDelete()) {
					continue;
				}
				getDynReqSeq().addSetAttribute(ATR_SPECIFIED_PHASE, covopt.getCovOptionKey()); //ALII1454
				getDynReqSeq().addSetAttribute(ATR_SPECIFIED_ELEMENT_INDEX, String.valueOf(covOptionElement++));
				initializeProperties(covopt.getId(), covOptionElement-1);		// AXAL3.7.56
				// end NBA104
				// NBA104 deleted code
		}
		// NBA104 deleted code
	}
	/**
	 * Perform the mode premium calculation for each substandard rating.
	 */
	// NBA104 change method signature to protected throwing Exception
	protected void processSubstandardRatingObjects() throws Exception {
		// NBA104 deleted code
		// begin NBA104
		setXmlObject(XO_SUBSTANDARDRATING);
		setCalculatorAttributes();
		setSpecificModelAttributes();
		Life life = nbaTxLife.getLife();
		if (life == null) {
			return;
		}
		int covCount = life.getCoverageCount();
		// end NBA104
		for (int i = 0; i < covCount; i++) {
			Coverage cov = life.getCoverageAt(i);
			// begin NBA104
			if (cov == null || cov.isActionDelete()) {
				continue;
			}
			// end NBA104
			int lifePartCount = cov.getLifeParticipantCount();
			for (int j = 0; j < lifePartCount; j++) {
				getDynReqSeq().addSetAttribute(ATR_SPECIFIED_PHASE, life.getCoverageAt(i).getCoverageKey());  //NBA104
				LifeParticipant lp = cov.getLifeParticipantAt(j);
				// begin NBA104
				if (lp == null || lp.isActionDelete()) {
					continue;
				}
				boolean firstFlat = true;
				// end NBA104
				int ratingCount = lp.getSubstandardRatingCount();
				for (int k = 0; k < ratingCount; k++) {
					SubstandardRating rating = lp.getSubstandardRatingAt(k);
					// begin NBA104
					if (!NbaUtils.isValidRating(rating)) { //SPR2590
						continue;
					}
					if (rating.hasPermTableRating() || rating.hasTempTableRating()) {
						getDynReqSeq().addSetAttribute(ATR_SPECIFIED_ELEMENT_INDEX, SE_TABLE_RATING);
					} else if (firstFlat) {
						getDynReqSeq().addSetAttribute(ATR_SPECIFIED_ELEMENT_INDEX, SE_FIRST_FLAT);
						firstFlat = false;
					} else {
						getDynReqSeq().addSetAttribute(ATR_SPECIFIED_ELEMENT_INDEX, SE_SECOND_FLAT);
					}
					initializeProperties(rating.getId());
					// end NBA104
					// NBA104 deleted code
				}
			}
		}
		// NBA104 deleted code
	}
	/**
	 * NbaModePremiumCalculator constructor.
	 */
	public NbaModePremiumCalculator(String aCalcType, NbaTXLife aNbaTxLife) throws NbaBaseException {
		super(aCalcType, aNbaTxLife);
	}

	/**
	 * Performs the calculation integration for NonStandard Mode Premium at the policy level.
	 * @return result of the calculation
	 */
	// NBA104 New Method
	protected void calculateNonStandardPremiums() throws NbaBaseException, NbaVpmsException {
		try {
			getCalcProxy().remove();
			resetDynReqSeq();
			setCalcType(CALC_TYPE_NON_STANDARD_MODE_PREMIUM);
			initialize();
			if (noModelForCalc()) {
				createErrorResultForNoModel();
			} else {
				setDynamicAttribute(ATR_MODE_PREMIUM, getPolicyModePremium());
				setDynamicAttribute(ATR_CALLING_PROGRAM, NbaModePremiumCalculator.class.getName());
				processPolicyObject();
				calculateResults();
			}
		} catch (NbaBaseException nbe) {
			throw nbe;
		} catch (Exception e) {
			throw new NbaBaseException(e);
		}

	}
	/**
	 * Loops through for the Payment amount and returns the value.
	 * @return result of the calculation
	 */
	// NBA104 New Method
	protected String getPolicyModePremium() {
		int count = getCalcReturnValues().getCalculationResultCount();
		for (int i = 0; i < count; i++) {
			CalculationResult aresult = getCalcReturnValues().getCalculationResultAt(i);

			for (int j = 0; j < aresult.getCalcProductCount(); j++) {
				CalcProduct aprod = aresult.getCalcProductAt(j);
				if (aprod.getType().equalsIgnoreCase("PaymentAmt")) {
					return aprod.getValue();
				}

			}
		}
		return "0" ;
	}
	// AXAL3.7.56
	protected boolean includeProperty(VpmsProp prop) {
		if (PROP_ALL.equals(prop.getDataType()) ||
			(XO_POLICY.equals(getXmlObject()) && PROP_POLICY.equals(prop.getDataType())) ||
			(XO_COVERAGE.equals(getXmlObject()) && PROP_COV.equals(prop.getDataType()))  ||
			(XO_RIDER.equals(getXmlObject()) && PROP_RDR.equals(prop.getDataType()))  ||
			(XO_COVOPTION.equals(getXmlObject()) && PROP_COVOPT.equals(prop.getDataType()))) {
				return true;
		}
		return false;
	}}

