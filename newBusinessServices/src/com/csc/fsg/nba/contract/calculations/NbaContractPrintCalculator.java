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
import java.util.ArrayList;
import java.util.HashMap;

import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vpms.results.VpmsAttr;
/**
 * NbaContractPrintCalculator calculates the values needed for policy pages, 
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
 * <tr><td>AXAL3.7.14</td><td>Version 4</td><td>Create Contract Print Extracts for AXA Documents</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */
public class NbaContractPrintCalculator extends NbaContractCalculatorCommon {
	
	//ALS4624
	public static String DEOINK_VARIABLES [] = {   
			"A_COV_FLAT_EXTRA",
			"A_COV_FLAT_YEARS",
			"A_COV_2ND_FLAT_EXTRA",
			"A_COV_2ND_FLAT_YEARS",
			"A_COV_3RD_FLAT_EXTRA",
			"A_COV_3RD_FLAT_YEARS"
		};
	
	/**
	 * NbaContractDocsCalculator constructor.
	 */
	protected NbaContractPrintCalculator(String aCalcType, NbaTXLife aNbaTxLife) throws NbaBaseException {
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
				setDynamicAttribute(ATR_CALLING_PROGRAM, NbaContractPrintCalculator.class.getName());
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

	
	// ALS4624 - New method
    private void populateRatingAttributes() {
        NbaTXLife nbaTxLife = getNbaTxLife();
        double extraAmt = 0.0;
        int years = 0;
        LifeParticipant lifePart = nbaTxLife.getPrimaryInuredLifeParticipant();
        ArrayList ratingArray = lifePart.getSubstandardRating();
        SubstandardRating rating = null;
        int j = -1;
        for (int i = 0; i < ratingArray.size(); i++) {
            rating = (SubstandardRating) ratingArray.get(i);
            String type = NbaUtils.getSubstandardRatingType(rating); //ALS5491
            //Only fix Out of Bound exception now.
            if (NbaUtils.isValidRating(rating)
            		&& (NbaConstants.SUB_STAND_TYPE_TEMP_FLAT.equals(type) || NbaConstants.SUB_STAND_TYPE_PERM_FLAT.equals(type))) {//ALS5491) {
                extraAmt = getFlatExtraAmt(rating);
                years = getFlatExtraYears(rating);
                if (j < (DEOINK_VARIABLES.length - 1)) { //ALS5491
                    getDynReqSeq().addSetAttribute(DEOINK_VARIABLES[++j] + "[0]", String.valueOf(extraAmt));
                    getDynReqSeq().addSetAttribute(DEOINK_VARIABLES[++j] + "[0]", String.valueOf(years));
                }
            }
        }
    }
	
	//ALS4624 - New method
	private int getFlatExtraYears(SubstandardRating rate ){
		int year = 0;
		SubstandardRatingExtension subRatExt = null;
		if (NbaUtils.isValidRating(rate)) { 
			subRatExt = NbaUtils.getFirstSubstandardExtension(rate);
			String type = NbaUtils.getSubstandardRatingType(rate);
			if (NbaConstants.SUB_STAND_TYPE_TEMP_FLAT.equals(type)) {
				year = subRatExt.getDuration();
			} else if (NbaConstants.SUB_STAND_TYPE_PERM_FLAT.equals(type)) {
				if (subRatExt.hasEndDate() && subRatExt.hasEffDate()) {
					year = NbaUtils.calcYearsDiff(subRatExt.getEndDate(), subRatExt.getEffDate());
				}
			}
		} 		
		return year;
	}

	//ALS4624 - New method
	private double getFlatExtraAmt(SubstandardRating rate ){
		double extraAmt = 0;
		SubstandardRatingExtension substandardExtension = null;
		if (NbaUtils.isValidRating(rate)) { 
			String type = NbaUtils.getSubstandardRatingType(rate);
			substandardExtension = NbaUtils.getFirstSubstandardExtension(rate);
			if (NbaConstants.SUB_STAND_TYPE_PERM_FLAT.equals(type) && substandardExtension != null) {
					extraAmt = substandardExtension.getPermFlatExtraAmt();
			} else if (NbaConstants.SUB_STAND_TYPE_TEMP_FLAT.equals(type)) {
				extraAmt = rate.getTempFlatExtraAmt();
			}
		} 		
		return extraAmt;
	}
	//ALS4624
	public void initializeAttributesForObject() throws NbaBaseException, NbaVpmsException {
		super.initializeAttributesForObject();
		populateRatingAttributes();
		
	}
	
	//ALS4624
	public boolean includeAttribute(VpmsAttr vAttr) {
		String attName = vAttr.getName();
		for (int i =0 ; i < DEOINK_VARIABLES.length; i ++){
    		if ( DEOINK_VARIABLES[i].equalsIgnoreCase(attName)){
    			return false;
    		}
    	}
		if (!super.includeAttribute(vAttr)) {
			return false;
		}

		return true;
		
	}
	
	
	
}
