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
 * *******************************************************************************<BR>
 */


import java.util.List;

import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;

/**
 * NbaRateUtilityCalculator
 * <p>
 * Type class decription here.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>SPR2590</td><td>Version 6</td><td>Proposed Table Substandard Ratings are not being excluded from premium calculations.</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaRateUtilityCalculator extends NbaContractCalculatorCommon implements NbaContractCalculator {

	/**
	 * Constructor for NbaRateUtilityCalculator.
	 * @param aCalcType
	 * @param aNbaTxLife
	 * @throws NbaBaseException
	 */
	public NbaRateUtilityCalculator(String aCalcType, NbaTXLife aNbaTxLife) throws NbaBaseException {
		super(aCalcType, aNbaTxLife);
	}

	/**
	 * Performs the calculation integration for Rate Utility model to resolve some properties
	 * needed by other models down stream and some values needed to create a valid inforce record.
	 * @return result of the calculation
	 */
	public NbaCalculation calculate() throws NbaBaseException, NbaVpmsException {
		try {
			if (noModelForCalc()) {
				createErrorResultForNoModel();
			} else {
				setDynamicAttribute(ATR_CALLING_PROGRAM, NbaRateUtilityCalculator.class.getName());
				initializeAttributesForObject();
				processCoverageObjects();
				processCovOptionObjects();
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
	 * Initializes the calculator specific attributes for the VPMS calculation
	 * control model.
	 */
	protected void setCalculatorAttributes() {
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	protected void processCoverageObjects() throws Exception {
		if (getCoverages().size() > 0) {
			setXmlObject(XO_COVERAGE);
			processSubstandardRatingObjects(getCoverages());
		}
		if (getRiders().size() > 0) {
			setXmlObject(XO_RIDER);
			processSubstandardRatingObjects(getRiders());
		}
	}

	/**
	 * 
	 * @param coverages
	 * @throws Exception
	 */
	protected void processSubstandardRatingObjects(List coverages) throws Exception {
		SubstandardRating substandardRating = null;
		int srCount = 0;
		int covCount = coverages.size();
		for (int i = 0; i < covCount; i++) {
			Coverage coverage = (Coverage)coverages.get(i);
			substandardRating = getTableRating(coverage);
			if (substandardRating != null) {
				if (srCount == 0) {          //first time thru
					setCalculatorAttributes();
					setSpecificModelAttributes();
				}
				initializeProperties(substandardRating.getId(), srCount++);
			}
		}
	}

	/**
	 * 
	 * @param coverage
	 * @return
	 */
	protected SubstandardRating getTableRating(Coverage coverage) {
		int lpCount = coverage.getLifeParticipantCount();
		for (int i=0; i < lpCount; i++) {
			LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(i);
			if (lifeParticipant != null && !lifeParticipant.isActionDelete()) {
				int srCount = lifeParticipant.getSubstandardRatingCount();
				for (int j=0; j < srCount; j++) {
					SubstandardRating subRating = lifeParticipant.getSubstandardRatingAt(j);
					if (NbaUtils.isValidRating(subRating) && (subRating.hasPermTableRating() || subRating.hasTempTableRating())) { //SPR2590
						SubstandardRatingExtension subRatingExt = NbaUtils.getFirstSubstandardExtension(subRating);
						if (subRatingExt != null && !subRatingExt.hasExtraPremPerUnit()) {
							return subRating;
						}
					}
				}
			}
		}
		return null;
	}
	/**
	 * Calculate the benefit percentage.
	 */
	protected void processCovOptionObjects() throws Exception {
		setXmlObject(XO_COVOPTION);
		setCalculatorAttributes();
		setSpecificModelAttributes();
		covOptionLevelCurrentValues();
	}
}
