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
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vpms.results.VpmsAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;
import com.csc.fsg.nba.vpms.results.VpmsProp;
/**
 * NbaAllStdModeslPremiumCalculator calculates the current mode premium for a contract 
 * for the following payment modes: Monthly, Quarterly, Semi-Annual and Annual.
 * The Standard Mode Premium VPMS model is called to calculate the premium
 * values.  The attributes which are passed to the model as arguments are set using values
 * retrieved from an NbaTXLife, as well as values retrieved from other sources.
 * The result of each modal calculation is stored as a <code>CalculationResult</code> whose ObjectId identifies
 * the payment mode. An <code>NbaCalculation</code> object is used to return the Vector
 * of <code>CalculationResult</code>s to the caller.
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
 * @see NbaContractCalculatorCommon
 * @since New Business Accelerator - Version 4
 */
public class NbaAllStdModeslPremiumCalculator extends NbaContractCalculatorCommon {
	protected static final String[] modes =
		{
			String.valueOf(NbaOliConstants.OLI_PAYMODE_MNTHLY),
			String.valueOf(NbaOliConstants.OLI_PAYMODE_QUARTLY),
			String.valueOf(NbaOliConstants.OLI_PAYMODE_BIANNUAL),
			String.valueOf(NbaOliConstants.OLI_PAYMODE_ANNUAL)};
	protected static final String P_MODE_PREMIUM = "P_MODE_PREMIUM";
	/**
	 * Return the OLI constants for the modes
	 * @return String[]
	 */
	protected static String[] getModes() {
		return modes;
	}
	protected String[] translatedModes;
	/**
	 * NbaModePremiumCalculator constructor.
	 * @param aCalcType indicates the calculation type to be performed. 					   
	 * @param aNbaTxLife contains contract data that will be used to supply values for VPMS attributes 					 
	 * @exception NbaBaseException 
	 */
	protected NbaAllStdModeslPremiumCalculator(
		String aCalcType,
		NbaTXLife aNbaTxLife)
		throws NbaBaseException {
		super(aCalcType, aNbaTxLife);
	}
	/**
	 * Calculate the current Mode Premium for the policy for the standard modes: Monthly, Quarterly,
	 * Semi-Annual and Annual.
	 * @return result of the calculation
	 * @exception NbaBaseException 
	 * @exception NbaVpmsException 
	 */
	public NbaCalculation calculate() throws NbaBaseException, NbaVpmsException {
		try {
			if (noModelForCalc()) {
				getCalcReturnValues().setCalcResultCode(TC_RESCODE_SUCCESS);
			} else {
				setDynamicAttribute(ATR_CALLING_PROGRAM, NbaAllStdModeslPremiumCalculator.class.getName());
				processPolicyObject(); //Set the attributes and properties to be used.
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
	 * Locate the A_Payment_Mode attribute in the VpmsModelResult. It contains information identifying the 
	 * translation to be used for A_Payment_Mode. Get the VPMS translations of the OLIFE values for the standard mode codes.
	 * @param name - the Attribute
	 * @exception NbaBaseException 
	 * @exception NbaVpmsException 
	 */
	protected void getModeTableValues(String name) throws NbaBaseException, NbaVpmsException {
		VpmsModelResult vpmsModelResult = nbaResult.getVpmsModelResult();
		int count = vpmsModelResult.getVpmsAttrCount();
		VpmsAttr anAttr;
		for (int cnt = 0; cnt < count; cnt++) {
			anAttr = vpmsModelResult.getVpmsAttrAt(cnt);
			if (anAttr.getName().equalsIgnoreCase(name)) {
				translateModes(anAttr);
				break;
			}
		}
	}
	/**
	 * Retrieve the VPMS mode code translations using lazy initializaton.
	 * @return a String[] of the VPMS codes. The occurrences correspond to the OLIFE 
	 * codes in the <code>modes[]</code>.
	 * @exception NbaBaseException  
	 * @exception NbaVpmsException 
	 */
	protected String[] getTranslatedModes() throws NbaVpmsException, NbaBaseException {
		if (translatedModes == null) {
			getModeTableValues(ATR_PAYMENT_MODE);
		}
		return translatedModes;
	}
	/** 
	 * Create the Atribute/Property entries in the RequestSequence to calculate the current premiums for
	 * all standard modes. 
	 * @exception NbaBaseException 
	 * @exception NbaVpmsException 
	 * @exception RemoteException 
	 */
	protected void initializeAttibutesAndPropertiesForAllModes() throws NbaVpmsException, NbaBaseException {
		for (int i = nbaResult.getVpmsModelResult().getVpmsPropCount() - 1; i > -1; i--) {
			VpmsProp prop = nbaResult.getVpmsModelResult().getVpmsPropAt(i);
			if (!prop.getName().equalsIgnoreCase(P_MODE_PREMIUM)) { //only interested in mode premiums
				nbaResult.getVpmsModelResult().removeVpmsPropAt(i);
			}
		}
		for (int i = 0; i < modes.length; i++) { //Add Attribute/Property pairs to set the mode and compute the premium for the modes
			String val = translateValueFor(modes[i]);
			getDynReqSeq().addSetAttribute(ATR_PAYMENT_MODE, val);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("NbaAllStdModeslPremiumCalculator: Attribute " + ATR_PAYMENT_MODE + " = " + val);
			}
			try {
				initializeProperties(getModes()[i]);
			} catch (RemoteException e) {
				throw new NbaBaseException(e);
			}
		}
	}
	/**
	 * Perform processing for the policy. Retrieve the Model specific attributes and properties.
	 * Resolve the generic mode premium attribute values. Set the Attibutes/Properties to cause 
	 * premiums to be calculated for the standard modes
	 * @exception NbaBaseException 
	 * @exception NbaVpmsException 
	 */
	public void processPolicyObject() throws NbaVpmsException, NbaBaseException {
		setXmlObject(XO_POLICY);
		setCalculatorAttributes();
		setSpecificModelAttributes(); //Retrieve the Model specific attributes and properties
		initializeAttributesForObject(); //Resolve the generic mode premium attribute values  
		initializeAttibutesAndPropertiesForAllModes(); //Set the Attibutes/Properties to cause premiums to be calculated for the standard modes
	}
	/**
	 * Initialize the calculator specific attributes for the VPMS calculation
	 * control model.
	 */
	protected void setCalculatorAttributes() {
		calcReqSeq.addSetAttribute(ATR_XML_OBJECT, getXmlObject());
	}
	/**
	 * Obtain the VPMS tranlations for the OLIFE standard mode codes from using the 
	 * A_Payment_Mode in VPMS.  Populate the translatedModes table with the results.
	 * @param anAttr the A_Payment_Mode attribute
	 * @exception NbaBaseException 
	 * @exception NbaVpmsException 
	 */
	protected void translateModes(VpmsAttr anAttr) throws NbaVpmsException, NbaBaseException {
		translatedModes = new String[getModes().length];
		for (int i = 0; i < getModes().length; i++) {
			translatedModes[i] = getTranslationValueForAttribute(anAttr, getModes()[i]);
		}
	}
	/**
	 * Translate an OLIFE mode code to the value used by VPMS.
	 * @param untranslatedMode - OLIFE mode code
	 * @return the value used by VPMS
	 * @exception NbaBaseException 
	 * @exception NbaVpmsException 
	 */
	protected String translateValueFor(String untranslatedMode) throws NbaVpmsException, NbaBaseException {
		String value = untranslatedMode;
		for (int i = 0; i < getModes().length; i++) {
			if (getModes()[i].equals(untranslatedMode)) {
				value = getTranslatedModes()[i];
				break;
			}
		}
		return value;
	}
}
