package com.csc.fsg.nba.contract.validation;
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
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.dataobject.accel.product.AdditionalPaymentProvision;
import com.csc.fs.dataobject.accel.product.AllowedSubstandard;
import com.csc.fs.dataobject.accel.product.AnnuityProduct;
import com.csc.fs.dataobject.accel.product.AnnuityProductExtension;
import com.csc.fs.dataobject.accel.product.CovOptionProduct;
import com.csc.fs.dataobject.accel.product.CovOptionProductExtension;
import com.csc.fs.dataobject.accel.product.CoverageProduct;
import com.csc.fs.dataobject.accel.product.CoverageProductExtension;
import com.csc.fs.dataobject.accel.product.CoverageProductFeature;
import com.csc.fs.dataobject.accel.product.DeathBenefitOptCC;
import com.csc.fs.dataobject.accel.product.FeatureOptProduct;
import com.csc.fs.dataobject.accel.product.FeatureProduct;
import com.csc.fs.dataobject.accel.product.IssueGenderCC;
/*import com.csc.fs.dataobject.accel.product.FinancialStatement; NBA237 deleted*/
import com.csc.fs.dataobject.accel.product.InvestProductInfo;
import com.csc.fs.dataobject.accel.product.InvestProductInfoExtension;
import com.csc.fs.dataobject.accel.product.LifeProductExtension;
import com.csc.fs.dataobject.accel.product.NonForProvision;
import com.csc.fs.dataobject.accel.product.PolicyProduct;
import com.csc.fs.dataobject.accel.product.PolicyProductExtension;
import com.csc.fs.dataobject.accel.product.QualifiedPlanCC;
import com.csc.fs.dataobject.accel.product.SubstandardRisk;
import com.csc.fs.dataobject.accel.product.UnderwritingClassProduct;
import com.csc.fs.dataobject.accel.product.UnderwritingClassProductExtension;



import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaObjectPrinter;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaAllowableRidersData;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.ValProc;
import com.csc.fsg.nba.vo.txlife.AnnuityExtension;
import com.csc.fsg.nba.vo.txlife.AnnuityRiderExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vo.txlife.LifeExtension;
/**
 * NbaValInsurance performs Insurance validation.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>SPR1705</td><td>Version 4</td><td>Vantage Annuity validation</td></tr>
 * <tr><td>SPR1728</td><td>Version 4</td><td>IssueAge is not calculated for Annuitant</td></tr>
 * <tr><td>SPR1764</td><td>Version 4</td><td>Redundant error messages are generated when ApplicationInfo.SignedDate or Requested Issue </td></tr>
 * <tr><td>SPR1771</td><td>Version 4</td><td>Face amount is incorrect on UW and Coverage/Party in stand alone</td></tr>
 * <tr><td>SPR1707</td><td>Version 4</td><td>Severe errors are generated for Substandard Extras</td></tr> 
 * <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
 * <tr><td>SPR1729</td><td>Version 4</td><td>Age is not calculated for Person</td></tr>
 * <tr><td>SPR1684</td><td>Version 4</td><td>Correct Age validation edits</td></tr>
 * <tr><td>SPR1706</td><td>Version 4</td><td>Severe Errors pertaining to Plan and Rates are generated</td></tr>
 * <tr><td>SPR1580</td><td>Version 4</td><td>Edits for verifying the presence of a Participant with an insurable role are not working</td></tr>
 * <tr><td>SPR1795</td><td>Version 4</td><td>Changing Insurance Amount through Application Update BF is not updating CurrentAmt and FaceAmt in nbA pending database and FACE LOB in AWD</td></tr>
 * <tr><td>SPR1800</td><td>Version 4</td><td>Effective and Issue date usage is incorrect.</td></tr>
 * <tr><td>SPR1917</td><td>Version 4</td><td>Validation routines which have a CTL value of HOLD.INV are bypassed</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>NBA107</td><td>Version 4</td><td>Allow for Vantage Trad/UL Issue to Admin</td></tr>
 * <tr><td>SPR1956</td><td>Version 4</td><td>Set TermDate and PayUpDate for CovOptions</td></tr>
 * <tr><td>SPR1945</td><td>Version 4</td><td>Correct inconsistent contract validation edits for String values</td></tr>
 * <tr><td>SPR1994</td><td>Version 4</td><td>Correct user validation example </td></tr>
 * <tr><td>SPR1996</td><td>Version 4</td><td>Insurance Validation Min-Max Amts Need to Vary by Issue Age/Add Min-Max for Units/Prem Amt</td></tr>
 * <tr><td>SPR1946</td><td>Version 4</td><td>CoverageOption as a Percent Indicator Set Wrong in Contract Validation Insurance.P118 and P909 </td></tr>
 * <tr><td>SPR1986</td><td>Version 4</td><td>Remove CLIF Adapter Defaults for Pay up and Term date of Coverages and Covoptions</td></tr>
 * <tr><td>NBA111</td><td>Version 4</td><td>Joint Coverage</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1641</td><td>Version 4</td><td>When UnitTypeInd is False and CurrentAmt is null, Error Message 5 is not displayed and CurrentAmt is calculated</td></tr>
 * <tr><td>SPR2048</td><td>Version 4</td><td>Renewal period is not calculated correctly for coverages with Select rates</td></tr>
 * <tr><td>SPR1234</td><td>Version 4</td><td>General source code clean up </td></tr>
 * <tr><td>SPR2093</td><td>Version 4</td><td>Change Contract Validation to set CovOption.LifeCovOptTypeCode from CovOptionProductExtension.LifeCovOptTypeCode</td></tr>
 * <tr><td>SPR2094</td><td>Version 4</td><td>Corrected Fund Tax Qualification edit</td></tr>
 * <tr><td>SPR2051</td><td>Version 4</td><td>Addition of Temporary Flat Rating is generating severity error message 2076  Invalid duration for temporary rating</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
 * <tr><td>SPR2051</td><td>Version 4</td><td>Coverage Approve/Deny tab - Approve/deny radio button group is not using the correct translation table.</td></tr>
 * <tr><td>SPR2149</td><td>Version 4</td><td>Face amount LOB is not set when Current amount has 9 or more digits</td></tr>
 * <tr><td>SPR2070</td><td>version 4</td><td> changeDescription="Insurance validation process (P020) is not needed." </td></tr>
 * <tr><td>SPR2367</td><td>version 4</td><td>Vntg Interface work</td></tr>
 * <tr><td>SPR1737</td><td>version 5</td><td>Error Messages 2027 is not displayed when Requested Maturity Date is less than the minimum allowed, in respect of Annuities.</td></tr>
 * <tr><td>SPR2368</td><td>version 5</td><td>Set the coverage refresh age need to be implemented for coverage options</td></tr>
 * <tr><td>SPR1833</td><td>version 5</td><td>Remove P060</td></tr> 
 * <tr><td>SPR1744</td><td>Version 5</td><td>Insurance validation Process ID P088 is not set</td></tr>
 * <tr><td>SPR2408</td><td>version 5</td><td>Validation error 2035 should be generated for Annuities when the issue date of a rider is before the issue date of Annuity.</td></tr>
 * <tr><td>SPR1646</td><td>Version 5</td><td>2053 Error Message is not displayed when duplicate Fund Allocations (ProductCode) were requested for the same systematic activity type.</td></tr>
 * <tr><td>SPR2133</td><td>Version 5</td><td>Insurance validation P217 is not taking the value from input xml.  It is taking value from PolicyProduct.FieldFormNumber</td></tr>
 * <tr><td>SPR1845</td><td>Version 5</td><td>Process ID P066 is not displaying error message when validation fails.</td></tr>
 * <tr><td>SPR2172</td><td>Version 5</td><td>FACE LOB should contain Face Amount of Primary Coverage</td></tr>
 * <tr><td>SPR2129</td><td>Version 5</td><td>Insurance Process ID P209 is not validating the InitPaymentAmt (planned initial) against Ownership.MinPremiumInitialAmt</td></tr>
 * <tr><td>SPR1859</td><td>Version 5</td><td>Process ID P076 does not validate requested QualPlanType against OLI-LU_QUALPLAN.</td></tr>
 * <tr><td>SPR1860</td><td>version 5</td><td>DeathBenefitOptType is not set to the plan default, when it is not specified.</td></tr>  
 * <tr><td>SPR2413</td><td>version 5</td><td>Insurance Process ID P103 defines to - Validate extended insurance allowed with substandard rating based on plan business rules (when NonFortProv is eti).</td></tr>  
 * <tr><td>SPR2099</td><td>version 5</td><td>MEC processing is incorrect.</td></tr>
 * <tr><td>SPR2654</td><td>version 5</td><td>Insurance Validation P077 is not validating for Vantage Plans.</td></tr>
 * <tr><td>SPR2103</td><td>version 5</td><td>Process ID P102 is not validating SpecialClass value against table NBA_Special Class.</td></tr>
 * <tr><td>SPR2986</td><td>version 6</td><td>Validation Process P154 is not validating the allocation percentage of the funds if it is less than defined in FeatureOptProduct.MinPercent.</td></tr>
 * <tr><td>SPR2067</td><td>version 6</td><td>Requested Maturity Date or Age is Required for Vantage Annuities - New Contract Val Step Needed</td></tr>  
 * <tr><td>SPR3043</td><td>version 6</td><td>Increase Coverage PWRateUsage and PWRateFactor Should Default from Base Coverage</td></tr>
 * <tr><td>NBA143</td><td>version 6</td><td>Inherent benefits processing</td></tr>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * <tr><td>SPR3098</td><td>Version 6</td><td>Contract Validation Processes and Edits Should Bypass Proposed Substandard Ratings</td></tr>
 * <tr><td>SPR2921</td><td>Version 6</td><td>Contract Validation Processes Should also be Performed for all The Benefits of The Coverage</td></tr>
 * <tr><td>SPR2919</td><td>Version 6</td><td>EndorsementID Should be set only for the Endorsements Whose ActionDelete flag is not set</td></tr> 
 * <tr><td>NBA142</td><td>Version 6</td><td>Minimum Initial Premium</td></tr>
 * <tr><td>SPR3147</td><td>Version 6</td><td>Renewable Plans Pay Up Date is Set Incorrectly Which May Cause Abend in Offline (Billing)</td></tr>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr>
 * <tr><td>NBA117</td><td>Version 7</td><td>Pending VANTAGE-ONE Calculations</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3157</td><td>Version 8</td><td>NonForfeiture Default for Rated Contract Needs to Be Defined to Customer Constant</td></tr>
 * <tr><td>SPR2731</td><td>Version 8</td><td>Deny Person Does Not Correctly Remove All Coverages Causing an Exception on the View</td></tr>
 * <tr><td>SPR3610</td><td>Version 8</td><td>Correct problems with Insurance Validation edit P145</td></tr>
 * <tr><td>AXAL3.7.40</td> <td>AXA Life Phase 1</td><td>Contract Validation for Biling Subset</td></tr>
 * <tr><td>SPR2230</td><td>Version 8</td><td>Issued as Applied Indicator Should Be Set to False if Issued Other Than Applied Set to True</td></tr>
 * <tr><td>AXAL3.7.18</td><td>AXA Life Phase 1</td><td>Producer Interfaces</td></tr>
 * <tr><td>NBA223</td><td>AXA Life Phase 1</td><td>Underwriter Final Disposition</td></tr>
 * <tr><td>ALS2561</td><td>AXA Life Phase 1</td><td>QC #1368 - Informal XML - missing Face Amount</td></tr>
 * <tr><td>QC2543</td><td>AXA Life Phase 1</td><td>Contract Message is encountered with "TEMPORARY Extra Years exceeds the coverage pay up date" every time when Temporary Flat Extra is added</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>P2AXAL054</td><td>AXA Life Phase 2</td><td>Omissions and Contract Validations</td></tr>
 * <tr><td>P2AXAL055</td><td>AXA Life Phase 2</td><td>Phase 2 Release 2 Product Validation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaValInsurance
	extends NbaContractValidationCommon
	implements NbaContractValidationBaseImpl, NbaContractValidationImpl { //SPR1234 
	/**
	 * Perform one time initialization.
	 */
	 //NBA237 changed method signature
	public void initialze(NbaDst nbaDst, NbaTXLife nbaTXLife, Integer subset, NbaOLifEId nbaOLifEId, AccelProduct nbaProduct, NbaUserVO userVO) { //AXAL3.7.18
		super.initialze(nbaDst, nbaTXLife, subset, nbaOLifEId, nbaProduct, userVO); //AXAL3.7.18
		initProcesses();
		Method[] allMethods = this.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("process_")) {
				// SPR3290 code deleted
				processes.put(aMethodName.substring(8).toUpperCase(), aMethod);
			}
		}
	}

	//NBA103 - removed get logger

	// SPR1994 code deleted
	/**
	 * @see com.csc.fsg.nba.contract.validation.NbaContractValidationImpl#validate()
	 */
	// ACN012 changed signature
	public void validate(ValProc nbaConfigValProc, ArrayList objects) {
		if (nbaConfigValProc.getUsebase()) { //ACN012
			super.validate(nbaConfigValProc, objects);
		} else { //ALS2600
		    if (getUserImplementation() != null) {
		        getUserImplementation().validate(nbaConfigValProc, objects);
		    }
		} //ALS2600    
	}

	/**
	 * Set ProductType from ProductType in NBA_PLANS table.
	 */
	protected void process_P001() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P001()"); //NBA103
			try {
				NbaPlansData nbaPlansData = getNbaTableAccessor().getPlanData(getTblKeys());
				if (nbaPlansData == null) {
					addNewSystemMessage(INVALID_PLAN_INFO, concat("Product code: ", getPolicy().getProductCode()), getIdOf(getPolicy()));
				} else {
					getPolicy().setProductType(nbaPlansData.getProductType());
					getPolicy().setActionUpdate();
				}
			} catch (NbaDataAccessException e) {
				addNewSystemMessage(INVALID_PLAN_INFO, e.toString(), getIdOf(getCoverage()));
			}
		}
	}

	/**
	 * Set LifeCovTypeCode value from CoverageType in NBA_PLANS table.
	 */
	protected void process_P911() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P911() for ", getCoverage()); //NBA103
			try {
				NbaPlansData nbaPlansData = getNbaTableAccessor().getPlanData(getTblKeys());
				if (nbaPlansData == null) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Product code: ", getCoverage().getProductCode()),
							getIdOf(getCoverage()));
				} else {
					getCoverage().setLifeCovTypeCode(nbaPlansData.getCoverageType());
					getCoverage().setActionUpdate();
				}
			} catch (NbaDataAccessException e) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), e.toString(), getIdOf(getCoverage()));
			}
		}
	}

	/**
	 * Set LifeCovTypeCode value from ProductType in NBA_ALLOWABLE_RIDERS table.
	 */
	protected void process_P912() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P912() for ", getCoverage()); //NBA103
			Map tblKeys = new HashMap();
			tblKeys.putAll(getTblKeys());
			try {
				NbaTableData nbaTableData = getNbaTableAccessor().getDataForOlifeValue(tblKeys, NbaTableConstants.NBA_ALLOWABLE_RIDERS,
						getCoverage().getProductCode());
				if (nbaTableData == null) {
					tblKeys.put(NbaTableAccessConstants.C_COVERAGE_KEY, "*"); //See if Rider is applicable to all plans
					nbaTableData = getNbaTableAccessor().getDataForOlifeValue(tblKeys, NbaTableConstants.NBA_ALLOWABLE_RIDERS,
							getCoverage().getProductCode());
				}
				if (nbaTableData == null) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Product code: ", getCoverage().getProductCode()),
							getIdOf(getCoverage()));
				} else {
					NbaAllowableRidersData nbaAllowableRidersData = (NbaAllowableRidersData) nbaTableData;
					getCoverage().setLifeCovTypeCode(nbaAllowableRidersData.getProductType());
					getCoverage().setPlanName(nbaAllowableRidersData.getRiderCovKeyTranslation()) ; //ALS5430
					getCoverage().setActionUpdate();
				}
			} catch (NbaDataAccessException e) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), e.toString(), getIdOf(getCoverage()));
			}
		}
	}

	/**
	 * Determine RiderTypeCode based on plan business rules.
	 */
	protected void process_P002() {
		if (verifyCtl(RIDER)) {
			// begin NBA104
			logDebug("Performing NbaValInsurance.process_P002() for Rider ", getRider());
			FeatureProduct featureProduct = getFeatureProductForPlan(getRider());
			if (featureProduct == null || featureProduct.getRiderTypeCode() != OLI_RIDER_DB) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Product code: ", getRider().getRiderCode()), getIdOf(getRider()));
			}

			Map tblKeys = new HashMap();
			tblKeys.putAll(getTblKeys());
			try {
				NbaTableData nbaTableData = getNbaTableAccessor().getDataForOlifeValue(tblKeys, NbaTableConstants.NBA_ALLOWABLE_RIDERS,
						getRider().getRiderCode());
				if (nbaTableData == null) {
					tblKeys.put(NbaTableAccessConstants.C_COVERAGE_KEY, "*"); //See if Rider is applicable to all plans
					nbaTableData = getNbaTableAccessor().getDataForOlifeValue(tblKeys, NbaTableConstants.NBA_ALLOWABLE_RIDERS,
							getRider().getRiderCode());
				}
				if (nbaTableData == null) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Product code: ", getRider().getRiderCode()), getIdOf(getRider()));
				} else {
					NbaAllowableRidersData nbaAllowableRidersData = (NbaAllowableRidersData) nbaTableData;
					getRider().setRiderTypeCode(nbaAllowableRidersData.getProductType());
					getRider().setActionUpdate();
				}
			} catch (NbaDataAccessException e) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), e.toString(), getIdOf(getRider()));
			}
			// end NBA104
		}
	}

	/**
	 * Examine IndicatorCode of Coverages to verify that a coverage 
	 * has been indicated as the base (primary) coverage.
	 */
	protected void process_P004() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P003()"); //NBA103
			for (int i = 0; i < getLife().getCoverageCount(); i++) {
				if (getLife().getCoverageAt(i).getIndicatorCode() == OLI_COVIND_BASE) {
					return;
				}
			}
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getLife()));
		}
	}

	/**
	 * Examine IndicatorCode of Coverages to verify that multiple coverage 
	 * have not been indicated as the base (primary) coverage.
	 */
	protected void process_P901() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P901()"); //NBA103
			boolean primaryCovFound = false;
			for (int i = 0; i < getLife().getCoverageCount(); i++) {
				if (getLife().getCoverageAt(i).getIndicatorCode() == OLI_COVIND_BASE) {
					if (primaryCovFound) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getLife()));
						return;
					} else {
						primaryCovFound = true;
					}
				}
			}
		}
	}

	/**
	 * Validate that Coverage is allowed as primary coverage based on PolicyProduct.LifeProduct.CoverageProduct.IndicatorCode.
	 */
	protected void process_P005() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P005() for ", getCoverage()); //NBA103
			CoverageProduct coverageProduct = getCoverageProductForPlan(getCoverage());
			if (coverageProduct == null) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
			}
		}
	}

	/**
	 * Validate that Coverage is allowed as a rider based on PolicyProduct.LifeProduct.CoverageProduct.IndicatorCode.
	 */
	protected void process_P006() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P006() for ", getCoverage()); //NBA103
			CoverageProduct coverageProduct = getCoverageProductForPlan(getCoverage());
			if (coverageProduct == null || coverageProduct.getIndicatorCode() != OLI_COVIND_RIDER) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
			}
		}
	}

	/**
	 * Validate that Rider is allowed for the Annuity based on PolicyProduct.AnnuityProduct.FeatureProduct.RiderTypeCode.
	 */
	protected void process_P007() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P007() for ", getRider()); //NBA103
			FeatureProduct featureProduct = getFeatureProductForPlan(getRider()); //NBA104
			if (featureProduct == null || featureProduct.getRiderTypeCode() != OLI_RIDER_DB) { //NBA104
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getRider()));
			}
		}
	}

	/**
	 * Verify the presence of a LifeParticipant with a primary insured role.
	 */
	protected void process_P010() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P010() for ", getCoverage()); //NBA103
			//begin SPR1580
			LifeParticipant lifeParticipant;
			if (getCoverage().getIndicatorCode() == OLI_COVIND_BASE) {
				lifeParticipant = NbaUtils.findPrimaryInsuredLifeParticipant(getCoverage());
			} else if (NbaUtils.isCTIRCoverage(getCoverage()) && !getCoverageExtension().getUnbornChildInd()) { //ALS5430
				lifeParticipant = NbaUtils.getLifeParticipantWithRoleCode(getCoverage(), NbaOliConstants.OLI_PARTICROLE_DEP);
			} else {
				lifeParticipant = NbaUtils.getInsurableLifeParticipant(getCoverage());
			}
			if (lifeParticipant == null) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Coverage: ", getCoverage().getPlanName()), getIdOf(getCoverage()));
			}
			//end SPR1580
		}
	}

	/**
	 * Verify the presence of a Participant with an annuitant role. 
	 */
	protected void process_P011() {
		if (verifyCtl(PAYOUT)) {
			logDebug("Performing NbaValInsurance.process_P011() for ", getPayout()); //NBA103
			for (int i = 0; i < getPayout().getParticipantCount(); i++) {
				if (getPayout().getParticipantAt(i).getParticipantRoleCode() == OLI_PARTICROLE_ANNUITANT) {
					return;
				}
			}
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPayout()));
		}
	}

	/**
	 * Verify the presence of a Participant with an insurable role.
	 */
	protected void process_P012() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P012() for ", getRider()); //NBA103
			//begin SPR1580
			if (NbaUtils.getInsurableParticipant(getRider()) == null) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Rider: ", getRider().getRiderCode()), getIdOf(getRider()));
			}
			//end SPR1580
		}
	}

	/**
	 * Verify presence of primary and joint LifeParticpant on joint life coverages
	 */
	protected void process_P013() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P013() for ", getCoverage()); //NBA103
			if (NbaUtils.isJointLife(getCoverage().getLivesType())) {
				boolean primaryFound = false;
				boolean jointFound = false;
				for (int i = 0; i < getCoverage().getLifeParticipantCount(); i++) {
					if (getCoverage().getLifeParticipantAt(i).getLifeParticipantRoleCode() == OLI_PARTICROLE_PRIMARY) {
						primaryFound = true;
					} else if (getCoverage().getLifeParticipantAt(i).getLifeParticipantRoleCode() == OLI_PARTICROLE_JOINT) {
						jointFound = true;
					}
				}
				if (!(primaryFound && jointFound)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
				}
			}
		}
	}

	/**
	 * Verify presence of primary and joint Particpant on a joint annuitanty.
	 */
	protected void process_P014() {
		if (verifyCtl(PAYOUT)) {
			logDebug("Performing NbaValInsurance.process_P014() for ", getPayout()); //NBA103
			if (isJointAnnuity()) {
				boolean annuitantFound = false;
				boolean jointFound = false;
				for (int i = 0; i < getPayout().getParticipantCount(); i++) {
					long roleCode = getPayout().getParticipantAt(i).getParticipantRoleCode();
					if (roleCode == OLI_PARTICROLE_ANNUITANT) {
						annuitantFound = true;
					} else if (roleCode == OLI_PARTICROLE_JNTANNUITANT) {
						jointFound = true;
					}
				}
				if (!(annuitantFound && jointFound)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPayout()));
				}
			}
		}
	}

	/**
	 * Verify presence of spouse LifeParticipant based on plan business rules.
	 */
	protected void process_P015() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P015() for ", getCoverage()); //NBA103
			CoverageProduct coverageProduct = getCoverageProductFor(getCoverage());
			if (coverageProduct == null) {
				addPlanInfoMissingMessage("CoverageProduct", getIdOf(getCoverage()));
			} else {
				long reqRelToPrimaryIns = coverageProduct.getReqRelToPrimaryIns();
				if (reqRelToPrimaryIns == OLI_REL_SPOUSE) {
					for (int i = 0; i < getCoverage().getLifeParticipantCount(); i++) {
						if (getCoverage().getLifeParticipantAt(i).getLifeParticipantRoleCode() == OLI_PARTICROLE_SPOUSE) {
							return;
						}
					}
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
				}
			}
		}
	}

	/**
	 * Verify the presence of a party person BirthDate for a LifeParticipant 
	 */
	protected void process_P016() {
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P016() for ", getLifeParticipant()); //NBA103
			if (findParty(getLifeParticipant().getPartyID())) {
				if (getParty().hasPersonOrOrganization() && getParty().getPersonOrOrganization().isPerson()) {
					if (getParty().getPersonOrOrganization().getPerson().hasBirthDate()) {
						return;
					}
				}
			}
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), " - " + getTranslatedRole(getLifeParticipant()), getIdOf(getLifeParticipant()));//P2AXAL054
		}
	}

	/**
	 * Verify the presence of a party person BirthDate for a Participant 
	 */
	protected void process_P017() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P017() for ", getParticipant()); //NBA103
			if (findParty(getParticipant().getPartyID())) {
				if (getParty().hasPersonOrOrganization() && getParty().getPersonOrOrganization().isPerson()) {
					if (getParty().getPersonOrOrganization().getPerson().hasBirthDate()) {
						return;
					}
				}
			}
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getParticipant()));
		}
	}

	/**
	 * Verify that LifeParticipant is a valid insurable person based on plan business rules.
	 */
	protected void process_P019() {
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P019() for ", getLifeParticipant()); //NBA103
			if (!isValidParticipantRole(getCoverage(), getLifeParticipant().getLifeParticipantRoleCode())) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Role Code: ", getLifeParticipant().getLifeParticipantRoleCode()),
						getIdOf(getLifeParticipant())); //SPR1706
			}
		}
	}

	//SPR2070 code deleted
	/**
	 * Verify that Participant is a valid insurable person based on plan business rules.
	 */
	protected void process_P021() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P021() for ", getParticipant()); //NBA103
			if (!isValidParticipantRole(getRider(), getParticipant().getParticipantRoleCode())) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getParticipant()));
			}
		}
	}

	/**
	 * Delete coverages and coverage options associated with a denied LifeParticipant.
	 * On the CyberLife backend system, the following processes currently take place when a Life, a Coverage, or a Benefit is denied in pending.
	 * Deny Life automatically deletes the coverage(s) and the benefit(s) associated with that person.
	 * Deny Coverage automatically deletes the coverage, the benefit(s), and the substandard extras associated with that coverage.
	 */
	protected void process_P022() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P022() for " + getCoverage().getId()); //NBA103
			if (canDenyCoverage(getCoverage())) {
				denyCoverage(getCoverage());
			}
		}
	}

	/**
	 * Delete coverage, coverage options and substandard extras associated with a denied coverage.
	 */
	protected void process_P023() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P023() for ", getCoverage()); //NBA103
			String action = getCoverage().getActionIndicatorCode();
			if (NbaActionIndicator.ACTION_DENY.equals(action)) { 
				denyCoverage(getCoverage());
			}
		}
	}

	/**
	 * Delete coverages and coverage options associated with a denied Participant.
	 */
	protected void process_P024() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P024() for ", getRider()); //NBA103
			for (int i = 0; i < getRider().getParticipantCount(); i++) {
				String action = getRider().getParticipantAt(i).getActionIndicatorCode();
				if (NbaActionIndicator.ACTION_DENY.equals(action)) {
					denyRider(getRider());
				} 
			}
		}
	}

	/**
	 * Delete coverage, coverage options and substandard extras associated with a denied rider
	 */
	protected void process_P025() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P025() for ", getRider()); //NBA103
			String action = getRider().getActionIndicatorCode();
			if (NbaActionIndicator.ACTION_DENY.equals(action)) {
				denyRider(getRider());
			}
		}
	}

	/**
	 * Calculate IssueAge of life participant based on effective date of the contract, birth date 
	 * and PolicyProduct.AgeCalculationType (age calculation type - near or last birthday, etc).
	 */
	protected void process_P026() {
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P026() for ", getLifeParticipant()); //NBA103
			if (getPolicy().hasEffDate()) {
				String effDate = NbaUtils.getStringFromDate(getPolicy().getEffDate());
				String birthDate = getBirthDateString(getLifeParticipant().getPartyID());
				if (birthDate != null) {
					PolicyProduct policyProduct = getPolicyProductFor(getCoverage());
					long ageRule = 4; //AXAL3.7.40, age nearest birthday
					if (policyProduct == null || !policyProduct.hasAgeCalculationType()) {
						addPlanInfoMissingMessage("PolicyProduct.AgeCalculationType", getIdOf(getLifeParticipant())); //SPR1728
					} else {
						ageRule = policyProduct.getAgeCalculationType();
					}
					/* AXAL3.7.40.PV.79 The Child's or Children's AGE as of the APPLICATION DATE; 
					 * Children covered under the Childrens Term Rider are not subjected to the Insurance age calculation
					 * nor affected by Policy Effective Date - it is the Application Date
					 * Begin APSL391
					*/
					if (getLifeParticipant().getLifeParticipantRoleCode() == NbaOliConstants.OLI_PARTICROLE_DEP) {
						if (getApplicationInfo().hasSignedDate()) {
							effDate = NbaUtils.getStringFromDate(getApplicationInfo().getSignedDate());
						}
					}//End APSL391
					int age = calcIssueAge(effDate, birthDate, Long.toString(ageRule));
					if (age >= 0) { //ALS4708
						getLifeParticipant().setIssueAge(age);
						getLifeParticipant().setActionUpdate();
					}
				}
			}
		}
	}

	/**
	 * Calculate IssueAge of participant based on effective date of the contract, birth date 
	 * and PolicyProduct.AgeCalculationType (age calculation type - near or last birthday, etc).
	 */
	protected void process_P027() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P027() for ", getParticipant()); //NBA103
			if (getPolicy().hasEffDate()) {
				String effDate = NbaUtils.getStringFromDate(getPolicy().getEffDate());
				String birthDate = getBirthDateString(getParticipant().getPartyID());
				if (birthDate != null) {
					PolicyProduct policyProduct = getPolicyProductForPlan();
					long ageRule = 4; //AXAL3.7.40, age nearest birthday
					if (policyProduct == null || !policyProduct.hasAgeCalculationType()) {
						addPlanInfoMissingMessage("PolicyProduct.AgeCalculationType", getIdOf(getParticipant())); //SPR1728
					} else {
						ageRule = policyProduct.getAgeCalculationType();
					}
					int age = calcIssueAge(effDate, birthDate, Long.toString(ageRule));
					if (age > 0) {
						getParticipant().setIssueAge(age);
						getParticipant().setActionUpdate();
					}
				}
			}
		}
	}

	/**
	 * Calculate IssueAge of participant based on effective date of the contract, birth date 
	 * and PolicyProduct.AgeCalculationType (age calculation type - near or last birthday, etc).
	 */
	protected void process_P028() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P028() for ", getParticipant()); //NBA103
			if (getPolicy().hasEffDate()) {
				String effDate = NbaUtils.getStringFromDate(getPolicy().getEffDate());
				String birthDate = getBirthDateString(getParticipant().getPartyID());
				if (birthDate != null) {
					PolicyProduct policyProduct = getPolicyProductFor(getRider());
					long ageRule = 4; //AXAL3.7.40, age nearest birthday
					if (policyProduct == null || !policyProduct.hasAgeCalculationType()) {
						addPlanInfoMissingMessage("PolicyProduct.AgeCalculationType", getIdOf(getParticipant())); //SPR1728
					} else {
						ageRule = policyProduct.getAgeCalculationType();
					}
					int age = calcIssueAge(effDate, birthDate, Long.toString(ageRule));
					if (age > 0) {
						getParticipant().setIssueAge(age);
						getParticipant().setActionUpdate();
					}
				}
			}
		}
	}

	/**
	 * Calculate true age to be the Party's age last birthday, based on the 
	 * Party.BirthDate property and the system date returned by the operating system. 
	 */
	protected void process_P029() {
		if (verifyCtl(PERSON)) {
			logDebug("Performing NbaValInsurance.process_P029() for ", getPerson()); //NBA103
			if (getPerson().hasBirthDate()) {
				//begin SPR3379
				if (NbaUtils.compare(getPerson().getBirthDate(), getCurrentDate()) < 1) {
					getPerson().setAge(NbaUtils.getYears(getPerson().getBirthDate(), getCurrentDate()));//NBA139
					getPerson().setActionUpdate(); //SPR1729
				} else {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPerson()));
				}
				//end SPR3379
			}
		}
	}

	/**
	 * Validate LifeParticipant IssueAge against maximum age allowed based on plan business rules.
	 */
	protected void process_P030() throws NbaBaseException{ //ALS4095
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P030() for ", getLifeParticipant()); //NBA103
			int maxAge = getMaxIssueAge(getCoverage()); //SPR1996
			int age = getLifeParticipant().getIssueAge();
			if (age > maxAge) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", age, ", Maximum age: ", maxAge),
						getIdOf(getLifeParticipant()));
			}
		}
	}

	/**
	 * Validate annuity Participant IssueAge against maximum age allowed based on plan business rules.
	 */
	protected void process_P031() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P031() for ", getParticipant()); //NBA103
			int maxAge = getMaxIssueAge(getParticipant().getIssueGender());
			int age = getParticipant().getIssueAge();
			if (age > maxAge) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", age, ", Maximum age: ", maxAge),
						getIdOf(getParticipant()));
			}
		}
	}

	/**
	 * Validate rider Participant IssueAge against maximum age allowed based on plan business rules.
	 */
	protected void process_P032() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P032() for ", getParticipant()); //NBA103
			int maxAge = getMaxIssueAge(getRider(), getParticipant().getIssueGender());
			int age = getParticipant().getIssueAge();
			if (age > maxAge) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", age, ", Maximum age: ", maxAge),
						getIdOf(getParticipant())); //SPR1956
			}
		}
	}

	/**
	 * Validate LifeParticipant IssueAge against minimum age allowed based on plan business rules.
	 */
	protected void process_P904() throws NbaBaseException { //ALS4095
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P904() for ", getLifeParticipant()); //NBA103
			int minAge = getMinIssueAge(getCoverage()); //SPR1996
			int age = getLifeParticipant().getIssueAge();
			if (age < minAge) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", age, ", Minimum age: ", minAge),
						getIdOf(getLifeParticipant()));
			}
		}
	}

	/**
	 * Validate annuity Participant IssueAge against maximum age allowed based on plan business rules.
	 */
	protected void process_P905() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P905() for ", getParticipant()); //NBA103
			int minAge = getMinIssueAge(getParticipant().getIssueGender());
			int age = getParticipant().getIssueAge();
			if (age < minAge) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", age, ", Minimum age: ", minAge),
						getIdOf(getParticipant())); //SPR1728
			}
		}
	}

	/**
	 * Validate Rider Participant IssueAge against maximum age allowed based on plan business rules.
	 */
	protected void process_P906() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P906() for ", getParticipant()); //NBA103
			int minAge = getMinIssueAge(getRider(), getParticipant().getIssueGender());
			int age = getParticipant().getIssueAge();
			if (age < minAge) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", age, ", Minimum age: ", minAge),
						getIdOf(getParticipant())); //NBA104
			}
		}
	}

	/**
	 * Compare IssueAge of primary insured to joint life participant.  Primary insured age must be younger.
	 * Primary LifePart tc="1"
	 * Joint LifePart tc="6"
	 */
	protected void process_P033() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P033() for ", getCoverage()); //NBA103
			if (NbaUtils.isJointLife(getCoverage().getLivesType())) {
				String primaryPartyID = null; //NBA111
				String jointPartyID = null; //NBA111
				int primaryAge = 0;
				int jointAge = 0;
				for (int i = 0; i < getCoverage().getLifeParticipantCount(); i++) {
					LifeParticipant lifeParticipant = getCoverage().getLifeParticipantAt(i);
					if (lifeParticipant.getLifeParticipantRoleCode() == OLI_PARTICROLE_PRIMARY) {
						primaryPartyID = lifeParticipant.getPartyID(); //NBA111
						primaryAge = getTrueAge(primaryPartyID); //NBA111
					} else if (lifeParticipant.getLifeParticipantRoleCode() == OLI_PARTICROLE_JOINT) {
						jointPartyID = lifeParticipant.getPartyID(); //NBA111
						jointAge = getTrueAge(jointPartyID); //NBA111
					}
				}
				if (primaryAge > jointAge) { //NBA111
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Primary: ", primaryAge, ", Joint: ", jointAge),
							getIdOf(getCoverage()));
					// begin NBA111
				} else if (primaryAge == jointAge) {
					Date primaryBirthdate = getBirthDate(primaryPartyID);
					Date jointBirthdate = getBirthDate(jointPartyID);
					if (primaryBirthdate == null || jointBirthdate == null || primaryBirthdate.before(jointBirthdate)) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Primary: ", primaryBirthdate, ", Joint: ", jointBirthdate),
								getIdOf(getCoverage()));
					}
					// end NBA111
				}
			}
		}
	}

	/**
	 * Compare IssueAge of primary annuitant to joint annuitant participant.   
	 * Primary annuitant age must be younger.
	 * Primary Part tc="27"
	 * Joint Part tc="28"
	 */
	protected void process_P034() {
		if (verifyCtl(PAYOUT)) {
			logDebug("Performing NbaValInsurance.process_P034() for ", getPayout()); //NBA103
			if (isJointAnnuity()) {
				int primaryAge = 0;
				int jointAge = 0;
				for (int i = 0; i < getPayout().getParticipantCount(); i++) {
					Participant participant = getPayout().getParticipantAt(i);
					if (participant.getParticipantRoleCode() == OLI_PARTICROLE_ANNUITANT) {
						primaryAge = participant.getIssueAge();
					} else if (participant.getParticipantRoleCode() == OLI_PARTICROLE_JNTANNUITANT) {
						primaryAge = participant.getIssueAge();
					}
				}
				if (!(primaryAge < jointAge)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Primary: ", primaryAge, ", Joint: ", jointAge),
							getIdOf(getPayout()));
				}
			}
		}
	}

	/**
	 * Verify dependent IssueAge does not exceed maximum age limit based on plan business rules.  
	 * If the maximum age limit is exceeded, the dependent is excluded from from coverage.
	 */
	protected void process_P035() throws NbaBaseException{ //ALS4095
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P035() for ", getCoverage()); //NBA103
			for (int i = 0; i < getCoverage().getLifeParticipantCount(); i++) {
				LifeParticipant lifeParticipant = getCoverage().getLifeParticipantAt(i);
				long role = lifeParticipant.getLifeParticipantRoleCode();
				if (role == OLI_PARTICROLE_CHILD || role == OLI_PARTICROLE_DEP) {
					int maxAge = getMaxIssueAge(getCoverage()); //SPR1996
					int age = lifeParticipant.getIssueAge(); //AXAL3.7.40
					if (age > maxAge) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Age: ", age, ", Maximum: ", maxAge), getIdOf(lifeParticipant));
					}
				}
			}
		}
	}

	/**
	 * Verify dependent IssueAge is not less than minimum age limit based on plan business rules.  
	 * If the minimum age limit is exceeded, the dependent is excluded from from coverage.
	 */
	protected void process_P036() throws NbaBaseException{ //ALS4095
		if (verifyCtl(LIFEPARTICIPANT)) { //ALS3293
			logDebug("Performing NbaValInsurance.process_P036() for ", getLifeParticipant());
			int minAge = getMinIssueAge(getCoverage());
			int age = getLifeParticipant().getIssueAge();
			if (age < minAge) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Age: ", age, ", Minimum: ", minAge), getIdOf(getLifeParticipant()));
			}
		}
	}

	/**
	 * Validate IssueGender value against table OLI_LU_GENDER for LifeParticipant.  
	 * If missing or not valid generate AXA severe error 2903:  Application question omitted - Gender
	 */
	protected void process_P037() {
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P037() for ", getLifeParticipant()); //NBA103
			long gender = getLifeParticipant().getIssueGender();
			if (!isValidTableValue(NbaTableConstants.OLIEXT_LU_ISSUEGENDER, gender)) { //AXAL3.7.40
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Gender - " + getTranslatedRole(getLifeParticipant()), getIdOf(getLifeParticipant())); //AXAL3.7.40 //P2AXAL054
			}
		}
	}

	/**
	 * Validate IssueGender value against OLI_LU_GENDER for Participant role.  
	 */
	protected void process_P038() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P038() for ", getParticipant()); //NBA103
			long gender = getParticipant().getIssueGender();
			if (!isValidTableValue(NbaTableConstants.OLI_LU_GENDER, gender)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue Gender: ", gender), getIdOf(getParticipant()));
			}
		}
	}

	/**
	 * Validate IssueGender against plan business rules to determine if coverage is issued for the indicated gender.
	 * PolicyProduct.LifeProduct.CoverageProduct.IssueGenderCC  
	 */
	protected void process_P039() throws NbaBaseException { //ALS4095
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P039() for ", getLifeParticipant()); //NBA103
			//Begin ALNA223 P2AXAL055
			CoverageProduct coverageProduct = getCoverageProductFor(getCoverage());
			if (coverageProduct != null) {
				long issueGender = getLifeParticipant().getIssueGender();
				if (getCoverage().getIndicatorCode() != OLI_COVIND_BASE) {
					issueGender = getNbaTXLife().getParty(getLifeParticipant().getPartyID()).getGender();
				}
				if (coverageProduct.getIssueGenderCC() != null && coverageProduct.getIssueGenderCC().getIssueGenderCount() > 0) {
					IssueGenderCC issueGenderCC = coverageProduct.getIssueGenderCC();
					for (int j = 0; j < issueGenderCC.getIssueGenderCount(); j++) {
						if (issueGenderCC.getIssueGenderAt(j) == issueGender) {
							return;
						}
					}
					//No return if there is no any match on Gender //SPR1996
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getLifeParticipant()));
			}
		}
			//End ALNA223 P2AXAL055
	}
	}

	/**
	 * Validate IssueGender against plan business rules to determine if Annuity can be issued for the indicated gender.
	 * PolicyProduct.AnnuityProduct.FeatureProduct.UnderwritingClassProduct
	 */
	protected void process_P040() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P040() for ", getParticipant()); //NBA103
			if (getMaxIssueAge(getParticipant().getIssueGender()) < 0) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getParticipant()));
			}
		}
	}

	/**
	 * Validate IssueGender against plan business rules to determine if rider can be issued for the indicated gender.
	 * PolicyProduct.AnnuityProduct.FeatureProduct.UnderwritingClassProduct
	 */
	protected void process_P041() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P041() for ", getParticipant()); //NBA103
			if (getMaxIssueAge(getRider(), getParticipant().getIssueGender()) < 0) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getParticipant()));
			}
		}
	}

	/**
	 * Use a VPMS model to set Coverage.CoverageExtension.RateClass.
	 */
	protected void process_P042() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P042() for ", getCoverage()); //NBA103
			//begin NBA077
			NbaLob lob = getNbaDst().getNbaLob();
			if ((lob.getContractChgType() != null) && (Long.parseLong(lob.getContractChgType()) == NBA_CHNGTYPE_INCREASE)
					&& (getCoverageExtension().hasRateClass())) {
				return;
			}
			//end NBA077
			LifeParticipant lifeParticipant = NbaUtils.findInsuredLifeParticipant(getCoverage(), false); //SPR1778 NBA104
			if (lifeParticipant != null) {
				//begin SPR1778
				if (findParty(lifeParticipant.getPartyID())) { //Get the Party for the insured LifeParticipant
					NbaOinkRequest nbaOinkRequest = new NbaOinkRequest();
					nbaOinkRequest.setPartyFilter(getOinkPartyFilter(getParty().getId()));
					nbaOinkRequest.setCoverageFilter(getOinkCoverageFilter(getCoverage().getId()));
					NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(new HashMap(), nbaOinkRequest, true); //Get the  rateclass //NBA100
					if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
						if (nbaVpmsResultsData.getResultsData().size() == 1) {
							String rateclass = (String) nbaVpmsResultsData.getResultsData().get(0);
							getCoverageExtension().setRateClass(rateclass);
							getCoverageExtension().setActionUpdate();
						} else {
							addUnexpectedVpmsResultMessage(1, nbaVpmsResultsData.getResultsData().size());
							//end SPR1778
						}
					}
				}
			}
			// NBA104 deleted code
			//SPR1778 code deleted
		}
	}

	/**
	 * Check the BandPremInd to determine if rates vary by band.  If so, retreive the BandTableIdentity.   
	 * Check the CovBandRuleCode to determine plan business rules for banding of the rider (i.e. is coverage banded?).
	 * If so is it banded separately from or with primary coverage?).  
	 * Use VP/MS model to determine the appropriate Band Identities.  
	 */
	protected void process_P045() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P045() for ", getCoverage()); //NBA103
			CoverageProductExtension coverageProductExtension = getCoverageProductExtensionFor(getCoverage()); //SPR1706
			if (coverageProductExtension == null) { //SPR1706
				addPlanInfoMissingMessage("CoverageProductExtension", getIdOf(getCoverage())); //SPR1706
			} else {
				//begin SPR1706
				String band = null;
				if (coverageProductExtension.getBandPremInd()) {
					String bandTableIdentity = coverageProductExtension.getBandTableIdentity();
					if (Character.isLetter(bandTableIdentity.charAt(0))) {
						Map skipAttributes = new HashMap();
						skipAttributes.put("A_BandStructureCode", bandTableIdentity);
						skipAttributes.put("A_BandAmount", NbaObjectPrinter.localeUnformattedDecimal(getCoverage().getCurrentAmt())); //SPR2149 
						NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(skipAttributes); //Get the bands
						if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
							if (nbaVpmsResultsData.getResultsData().size() == 1) {
								band = (String) nbaVpmsResultsData.getResultsData().get(0);
							} else {
								addUnexpectedVpmsResultMessage(1, nbaVpmsResultsData.getResultsData().size());
							}
						}
					}
				}
				getCoverageExtension().setBandTableIdentity(band); //Field is incorrectly named
				getCoverageExtension().setActionUpdate();
				//end SPR1706
			}
		}
	}

	//SPR3147 code deleted
	/**
	 * Retrieve the coverage ValuePerUnit amount from Rate tables
	 */
	protected void process_P048() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P048() for ", getCoverage()); //NBA103
			if (!getCoverage().hasValuePerUnit()) { //Should the plan amount override any input value?
				// begin NBA104
				CoverageProduct coverageProduct = getCoverageProductFor(getCoverage());
				if (coverageProduct != null && coverageProduct.hasValuePerUnit()) {
					getCoverage().setValuePerUnit(coverageProduct.getValuePerUnit());
				} else {
					getCoverage().setValuePerUnit(1000);
				}
				// end NBA104
				getCoverage().setActionUpdate();
			}
		}
	}

	/**
	 * Calculate CurrentAmt (coverage amount) when CurrentNumberOfUnits (units) are requested.  
	 * Calculate CurrentNumberOfUnits (units) when CurrentAmt (coverage amount) is requested.
	 * Calculate CurrentAmt (coverage amount) even if the UnitTypeInd is false and 
	 * CurrentNumberOfUnits (units) are requested and set UnitTypeInd to true.   
	 */
	protected void process_P049() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P049() for ", getCoverage()); //NBA103
			//Begin SPR1641
			//If CurrentNumberOfUnits is not set,set CurrentNumberOfUnits to 0 
			if (!getCoverage().hasCurrentNumberOfUnits()) {
				getCoverage().setCurrentNumberOfUnits(0);
			}
			//If CurrentAmt is not set,set CurrentAmt to 0
			if (!getCoverage().hasCurrentAmt()) {
				getCoverage().setCurrentAmt(0);
			}
			//End SPR1641
			if (getCoverageExtension().getUnitTypeInd()) { //Units = true //SPR1795
				getCoverage().setCurrentAmt(getCoverage().getCurrentNumberOfUnits() * getCoverage().getValuePerUnit());
			} else if (getCoverage().getCurrentAmt() > 0) { //Units = false and currentamt > 0  //SPR1795 SPR1641
				getCoverage().setCurrentNumberOfUnits(getCoverage().getCurrentAmt() / getCoverage().getValuePerUnit());
			} else if (getCoverage().getCurrentNumberOfUnits() > 0 && getCoverage().getCurrentAmt()== 0 ) { //Units = false and currentamt !> 0 //SPR1795 SPR1641 //ALS2561
				getCoverageExtension().setUnitTypeInd(true); //SPR1795
				getCoverage().setCurrentAmt(getCoverage().getCurrentNumberOfUnits() * getCoverage().getValuePerUnit()); //SPR1795
			}
			if (!getCoverage().hasIntialNumberOfUnits()) { //SPR1795
				getCoverage().setIntialNumberOfUnits(getCoverage().getCurrentNumberOfUnits()); //SPR1795
			} //SPR1795
			if (!getCoverage().hasInitCovAmt()) { //SPR1795
				getCoverage().setInitCovAmt(getCoverage().getCurrentAmt()); //SPR1795
			} //SPR1795
			getCoverage().setActionUpdate();
			getCoverageExtension().setActionUpdate();
		}
	}

	/**
	 * Verify requested CurrentAmt and CurrentNumberOfUnits are not less than minimum coverage amount or units
	 * allowed based on plan business rules. CurrentAmt is compared against UnderwritingClassProduct.MinIssueAmt.
	 * CurrentNumberOfUnits is compared against UnderwritingClassProductExtension.MinIssueUnits.
	 */
	protected void process_P050() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P050() for ", getCoverage()); //NBA103
			UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProductFor(getCoverage());
			if (underwritingClassProduct != null) { //SPR1966
				// SPR1996 code deleted
				if (underwritingClassProduct.hasMinIssueAmt()) { //SPR1996
					double min = underwritingClassProduct.getMinIssueAmt();
					double amt = getCoverage().getCurrentAmt();
					if (amt < min) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Requested Amount: ", amt, ", Minimum: ", min),
								getIdOf(getCoverage()));
					}
				}
				//begin SPR1996
				UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(underwritingClassProduct);
				if (ext != null) {
					if (ext.hasMinIssueUnits()) {
						double min = ext.getMinIssueUnits();
						double units = getCoverage().getCurrentNumberOfUnits();
						if (units < min) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested Units: ", units, ", Minimum: ", min),
									getIdOf(getCoverage()));
						}
					}
				}
				//end SPR1996
			}
		}
	}

	/**
	 * Verify requested CurrentAmt and CurrentNumberOfUnits are not greater than maximum coverage amount or units
	 * allowed based on plan business rules. CurrentAmt is compared against UnderwritingClassProduct.MaxIssueAmt.
	 * CurrentNumberOfUnits is compared against UnderwritingClassProductExtension.MaxIssueUnits.
	 */
	protected void process_P051() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P051() for ", getCoverage()); //NBA103
			UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProductFor(getCoverage());
			if (underwritingClassProduct != null) { //SPR1966
				// SPR1996 code deleted
				if (underwritingClassProduct.hasMaxIssueAmt() && underwritingClassProduct.getMaxIssueAmt() > 0) { //SPR1996
					double max = underwritingClassProduct.getMaxIssueAmt();
					double amt = getCoverage().getCurrentAmt();
					if (amt > max) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Requested Amount: ", amt, ", Maximum: ", max),
								getIdOf(getCoverage()));
					}
				}
				//begin SPR1996
				UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(underwritingClassProduct);
				if (ext != null) {
					if (ext.hasMaxIssueUnits() && ext.getMaxIssueUnits() > 0) {
						double max = ext.getMaxIssueUnits();
						double units = getCoverage().getCurrentNumberOfUnits();
						if (units > max) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested Units: ", units, ", Maximum: ", max),
									getIdOf(getCoverage()));
						}
					}
				}
				//end SPR1996				
			}
		}
	}

	/**
	 * Calculate TotAmt (rider coverage amount) from NumberOfUnits (units) when UnitTypeInd is true.
	 * When UnitTypeInd is false and TotAmt is greater than zero, calculate NumberOfUnits.  Otherwise 
	 * if NumberOfUnits is greater than zero, calculate TotAmt and set UnitTypeInd to true. 
	 * Retrieve the coverage ValuePerUnit amount from PolicyProduct.LifeProduct.CoverageProduct.ValuePerUnit.
	 * If ValuePerUnit is not present in policy product, default to 1000.
	 */
	protected void process_P053() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P053() for ", getRider()); //NBA103
			// begin NBA104
			double valuePerUnit = 1000;
			CoverageProduct coverageProduct = getCoverageProductFor(getRider());
			if (coverageProduct == null || !coverageProduct.hasValuePerUnit()) {
				valuePerUnit = coverageProduct.getValuePerUnit();
			}
			if (getAnnuityRiderExtension().getUnitTypeInd()) {
				getRider().setTotAmt(getRider().getNumberOfUnits() * valuePerUnit);
			} else if (getRider().getTotAmt() > 0) {
				getRider().setNumberOfUnits(NbaUtils.divideTo2DecimalPlaces(getRider().getTotAmt(), valuePerUnit));
			} else if (getRider().getNumberOfUnits() > 0) {
				getRider().setTotAmt(getRider().getNumberOfUnits() * valuePerUnit);
				getAnnuityRiderExtension().setUnitTypeInd(true);
				getAnnuityRiderExtension().setActionUpdate();
			}
			getRider().setActionUpdate();
			// end NBA104
		}
	}

	/**
	 * Verify requested TotAmt (Rider amount) and NumberOfUnits (units) are not less than minimum Rider amount
	 * allowed based on plan business rules.  TotAmt is compared against UnderwritingClassProduct.MinIssueAmt.
	 * NumberOfUnits is compared against UnderwritingClassProductExtension.MinIssueUnits.
	 */
	protected void process_P054() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P054() for ", getRider()); //NBA103
			UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProductFor(getRider());
			if (underwritingClassProduct == null) {
				addPlanInfoMissingMessage("UnderwritingClassProduct Min", getIdOf(getRider()));
			} else if (underwritingClassProduct.hasMinIssueAmt()) { //NBA104
				double min = underwritingClassProduct.getMinIssueAmt();
				double amt = getRider().getTotAmt();
				if (amt < min) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Requested: ", amt, ", Minimum: ", min), getIdOf(getRider()));
				}
				//begin NBA104
			} else {
				UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(underwritingClassProduct);
				if (ext != null && ext.hasMinIssueUnits()) {
					double min = ext.getMinIssueUnits();
					double units = getRider().getNumberOfUnits();
					if (units < min) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested Units: ", units, ", Minimum: ", min),
								getIdOf(getRider()));
					}
				}
				//end NBA104				
			}
		}
	}

	/**
	 * Verify requested TotAmt (Rider amount) and NumberOfUnits (units) are less than maximum Rider amount allowed
	 * based on plan business rules.  TotAmt is compared against UnderwritingClassProduct.MaxIssueAmt.
	 * NumberOfUnits is compared against UnderwritingClassProductExtension.MaxIssueUnits.
	 */
	protected void process_P055() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P055() for ", getRider()); //NBA103
			UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProductFor(getRider());
			if (underwritingClassProduct == null) {
				addPlanInfoMissingMessage("UnderwritingClassProduct Max", getIdOf(getRider()));
			} else if (underwritingClassProduct.hasMaxIssueAmt() && underwritingClassProduct.getMaxIssueAmt() > 0) { //NBA104
				double max = underwritingClassProduct.getMaxIssueAmt();
				double amt = getRider().getTotAmt();
				if (amt > max) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Requested: ", amt, ", Maximum: ", max), getIdOf(getRider()));
				}
				//begin NBA104
			} else {
				UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(underwritingClassProduct);
				if (ext != null && ext.hasMaxIssueUnits() && ext.getMaxIssueUnits() > 0) {
					double max = ext.getMaxIssueUnits();
					double units = getRider().getNumberOfUnits();
					if (units > max) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested Units: ", units, ", Maximum: ", max),
								getIdOf(getRider()));
					}
				}
				//end NBA104				
			}
		}
	}

	/**
	 * Set valuation code based on plan business rules:  Lives Type, Class, Base, Subseries.
	 */
	protected void process_P056() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P056() for ", getCoverage()); //NBA103
			PolicyProduct policyProduct = getPolicyProductFor(getCoverage());
			if (policyProduct == null) {
				addPlanInfoMissingMessage("PolicyProduct", getIdOf(getCoverage()));
			} else {
				PolicyProductExtension policyProductExtension = getPolicyProductExtensionFor(policyProduct);
				if (policyProductExtension == null) {
					addPlanInfoMissingMessage("PolicyProductExtension", getIdOf(getCoverage()));
				} else {
					CoverageExtension coverageExtension = getCoverageExtension();
					coverageExtension.setValuationClassType(policyProductExtension.getValuationClassType());
					coverageExtension.setValuationBaseSeries(policyProductExtension.getValuationBaseSeries());
					coverageExtension.setValuationSubSeries(policyProductExtension.getValuationSubSeries());
					coverageExtension.setActionUpdate();
					CoverageProduct coverageProduct = getCoverageProductFor(getCoverage());
					if (coverageProduct == null) {
						addPlanInfoMissingMessage("CoverageProduct", getIdOf(getCoverage()));
					} else {
						getCoverage().setLivesType(coverageProduct.getLivesType());
						getCoverage().setActionUpdate();
					}
				}
			}
		}
	}

	/**
	 * Set valuation code based on plan business rules:  Lives Type, Class, Base, Subseries.
	 */
	protected void process_P057() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P057()");
			PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan(); //NBA103
			if (policyProductExtension == null) {
				addPlanInfoMissingMessage("PolicyProductExtension", getIdOf(getAnnuity()));
			} else {
				AnnuityProductExtension annuityProductExtension = getAnnuityProductExtension();
				if (annuityProductExtension == null) {
					addPlanInfoMissingMessage("AnnuityProductExtension", getIdOf(getAnnuity()));
				} else {
					AnnuityExtension annuityExtension = getAnnuityExtension();
					annuityExtension.setLivesType(annuityProductExtension.getLivesType());
					annuityExtension.setValuationClassType(policyProductExtension.getValuationClassType());
					annuityExtension.setValuationBaseSeries(policyProductExtension.getValuationBaseSeries());
					annuityExtension.setValuationSubSeries(policyProductExtension.getValuationSubSeries());
					annuityExtension.setActionUpdate();
				}
			}
		}
	}

	/**
	 * Set valuation code based on plan business rules:  Lives Type, Class, Base, Subseries.
	 */
	protected void process_P058() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P058() for ", getRider()); //NBA103
			PolicyProduct policyProduct = getPolicyProductFor(getRider());
			if (policyProduct == null) {
				addPlanInfoMissingMessage("PolicyProduct", getIdOf(getRider()));
			} else {
				PolicyProductExtension policyProductExtension = getPolicyProductExtensionFor(policyProduct);
				if (policyProductExtension == null) {
					addPlanInfoMissingMessage("PolicyProductExtension", getIdOf(getRider()));
				} else {
					AnnuityRiderExtension annuityRiderExtension = getAnnuityRiderExtension();
					annuityRiderExtension.setLivesType(OLI_COVLIVES_SINGLE);
					annuityRiderExtension.setValuationClassType(policyProductExtension.getValuationClassType());
					annuityRiderExtension.setValuationBaseSeries(policyProductExtension.getValuationBaseSeries());
					annuityRiderExtension.setValuationSubSeries(policyProductExtension.getValuationSubSeries());
					annuityRiderExtension.setActionUpdate();
					CoverageProduct coverageProduct = getCoverageProductFor(getRider());
					if (coverageProduct == null) {
						addPlanInfoMissingMessage("CoverageProduct", getIdOf(getRider()));
					} else {
						annuityRiderExtension.setLivesType(coverageProduct.getLivesType());
					}
				}
			}
		}
	}

	/**
	 * Validate LivesType (number of lives) code.  Set based on plan business rules if none present.
	 */
	protected void process_P059() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P059()"); //NBA103
			AnnuityExtension annuityExtension = getAnnuityExtension();
			if (!isValidTableValue(NbaTableConstants.OLI_LU_LIVESTYPE, annuityExtension.getLivesType())) {
				AnnuityProductExtension annuityProductExtension = getAnnuityProductExtension();
				if (annuityProductExtension == null || !annuityProductExtension.hasLivesType()) {
					addPlanInfoMissingMessage("AnnuityProductExtension.LivesType", getIdOf(getAnnuity()));
				} else {
					annuityExtension.setLivesType(annuityProductExtension.getLivesType());
					annuityExtension.setActionUpdate();
				}
			}
		}
	}

	//SPR1833 removed P060

	/**
	 * Validate requested issue day against user-defined maximum from a VPMS model.
	 * Vantage does not allow issue day greater than 28.  
	 * CyberLife is controlled by user constant UDC182 which is set to 31 in base system. 
	 */
	protected void process_P061() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValInsurance.process_P061()"); //NBA103
			if (getApplicationInfo().hasRequestedPolDate()) {
				NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //Get the effective date
				if (nbaVpmsResultsData != null) {
					if (nbaVpmsResultsData.wasSuccessful()) {
						String maxStringDate = (String) nbaVpmsResultsData.getResultsData().get(0);
						try {
							Date RequestedPolDate = getApplicationInfo().getRequestedPolDate();
							Date maxDate = get_YYYY_MM_DD_sdf().parse(maxStringDate);
							if (NbaUtils.compare(RequestedPolDate, maxDate) > 0) {
								addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested issue date: ", RequestedPolDate),
										getIdOf(getPolicy()));
							}
						} catch (ParseException e) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Unknown date format: ", maxStringDate),
									getIdOf(getPolicy()));
						}
					}
				} else {
					addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), ", Model: ", getNbaConfigValProc()
							.getModel()), getIdOf(getPolicy()));
				}
			}
		}
	}

	/**
	 * Validate InitialPremAmt (planned initial) against the Plan Rules
	 */
	protected void process_P062() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P062()"); //NBA103
			//begin NBA142
			LifeProductExtension lifeProdExtn = getLifeProductExtensionForPlan();
			if (lifeProdExtn == null) {
				addPlanInfoMissingMessage("LifeProduct", getIdOf(getPolicy()));
			} else if (NbaOliConstants.OLIX_MINPREMINITRULE_USEAMT == lifeProdExtn.getMinPremInitialRule()) {
				editInitialPremium(getLife().getInitialPremAmt(), lifeProdExtn.getMinPremiumInitialAmt(), lifeProdExtn.getMaxPremiumInitialAmt());
			}
			//end NBA142
		}
	}

	/**
	 * Validate InitPaymentAmt (planned initial) against the Initial Premium Limits
	 */
	protected void process_P063() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P063()"); //NBA103
			editInitialPremiumByQualType(getAnnuity().getQualPlanType(), getAnnuity().getInitPaymentAmt()); //NBA104
		}
	}

	/**
	 * Validate Annuity.InitPaymentAmt (planned initial) against Ownership.MinPremiumInitialAmt and Ownership.MaxPremiumInitialAmt
	 * Absence of Annuity.InitPaymentAmt in input XML103 is considered to be 0.0 for validation 
	 */
	//SPR1705 New Method
	protected void process_P209() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P209()"); //NBA103
			editInitialPremiumAgainstPlan(getAnnuity().hasInitPaymentAmt() ? getAnnuity().getInitPaymentAmt() : 0.0); //SPR2129
		}
	}

	/**
	 * Verify that a PaymentAmt (periodic premium) was specified.
	 */
	protected void process_P064() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P064()"); //NBA103
			if (!getPolicy().hasPaymentAmt() || !(getPolicy().getPaymentAmt() > 0)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPolicy()));
			}
		}
	}

	/**
	 * Validate PaymentAmt (periodic premium) against NBA_PREMIUM_LIMITS
	 */
	protected void process_P065() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P065()"); //NBA103
			// begin NBA104
			if (getNbaTXLife().isAnnuity()) {
				editPeriodicPremiumByQualType(getAnnuity().getQualPlanType(), getPolicy().getPaymentAmt());
			} else {
				editPeriodicPremiumForLife(getPolicy().getPaymentAmt());
			}
			// end NBA104
		}
	}

	/**
	 * Validate PaymentAmt (periodic premium) against NBA_PREMIUM_LIMITS
	 */
	//SPR1704 New Method
	protected void process_P210() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P210()"); //NBA103
			editPeriodicPremiumAgainstPlan(getPolicy().getPaymentAmt());
		}
	}

	/**
	 * If PlannedAdditionalPremium > 0, verify PlannedAdditionalPremium is allowed based on 
	 * CoverageProductExtension.CoverageProductFeature.AdditionalPaymentProvision.AddlPayEffectsRule 
	 * Use the first CoverageProductFeature and first AdditionalPaymentProvision. 
	 * Assumes that if there are no CoverageProductFeatures or AdditionalPaymentProvisions, that 
	 * additional premiums are not allowed. 
	 */
	protected void process_P066() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P066()"); //NBA103
			PolicyExtension ext = getPolicyExtension();
			if (ext.hasPlannedAdditionalPremium() && ext.getPlannedAdditionalPremium() > 0) {
				//begin SPR1706
				CoverageProductExtension coverageProductExtension = getCoverageProductExtensionFor(getCoverage());
				if (coverageProductExtension == null || coverageProductExtension.getCoverageProductFeatureCount() < 1) { //SPR1845
					//SPR1845 code deleted
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPolicy())); //SPR1845		
				} else {
					CoverageProductFeature coverageProductFeature = coverageProductExtension.getCoverageProductFeatureAt(0);
					if (coverageProductFeature.getAdditionalPaymentProvisionCount() < 1) {
						//SPR1845 code deleted
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPolicy())); //SPR1845				
					} else {
						AdditionalPaymentProvision additionalPaymentProvision = coverageProductFeature.getAdditionalPaymentProvisionAt(0);
						if (!additionalPaymentProvision.hasAddlPayEffectsRule() || additionalPaymentProvision.getAddlPayEffectsRule() == 1000500004) { //tc = "1000500004" means "Not allowed"
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPolicy()));
						}
					}
					//end SPR1706
				}
			}
		}
	}

	/**
	 * Verify requested MaturityAge, MaturityDate or MaturityDuration is not less than the 
	 * minimum allowed for the plan based on plan business rules.
	 */
	protected void process_P067() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P067()"); //NBA103
			Date regMatDate = NbaUtils.getRequestedMaturityDate(getPolicy(), getLife(), getNbaTXLife().getPrimaryCoverage()); //SPR1986
			if (regMatDate != null) {
				//begin SPR1706
				PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
				if (policyProductExtension != null && policyProductExtension.hasMinMatureDuration()) {
					int minMatureDuration = policyProductExtension.getMinMatureDuration();
					if (minMatureDuration < 999) {
						Calendar req = GregorianCalendar.getInstance();
						req.setTime(regMatDate);
						Calendar calc = GregorianCalendar.getInstance();
						calc.setTime(getPolicy().getEffDate());
						calc.add(Calendar.YEAR, minMatureDuration);
						if (calc.after(req)) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested maturity date: ", regMatDate),
									getIdOf(getLife()));
						}
						//end SPR1706
					}
				}
			}
			//SPR1705 code deleted
		}
	}

	/**
	 * Verify requested MaturityAge, MaturityDate or MaturityDuration is not less than the 
	 * minimum allowed for the plan based on plan business rules.
	 */
	protected void process_P902() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P902()"); //NBA103
			Date regMatDate = NbaUtils.getRequestedMaturityDate(getPolicy(), getAnnuity()); //SPR1986
			if (regMatDate != null) {
				Calendar req = GregorianCalendar.getInstance();
				req.setTime(regMatDate);
				Calendar calc = GregorianCalendar.getInstance();
				calc.setTime(getPolicy().getEffDate());
				PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
				if (policyProductExtension != null && policyProductExtension.hasMinMatureDuration()) { //SPR1705
					//SPR1705 code deleted
					int minMatureDuration = policyProductExtension.getMinMatureDuration(); //SPR1705
					calc.add(Calendar.YEAR, minMatureDuration);
					if (calc.after(req)) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested maturity date: ", regMatDate),
								getIdOf(getAnnuity())); //SPR1737 
					}
				}
			}
			//SPR1764 code deleted
		}
	}

	/**
	 * Verify requested MaturityAge, MaturityDate or MaturityDuration 
	 * does not exceed the calculated plan TermDate (maturity date).
	 */
	protected void process_P068() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P068()"); //NBA103
			Date regMatDate = NbaUtils.getRequestedMaturityDate(getPolicy(), getLife(), getNbaTXLife().getPrimaryCoverage()); //SPR1986
			Date termDate = getPolicy().getTermDate();
			if (regMatDate != null && termDate != null) {
				if (NbaUtils.compare(regMatDate, termDate) > 0) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested maturity date: ", regMatDate, ", Plan maximum date: ",
							termDate), getIdOf(getLife()));
				}
			}
		}
	}

	/**
	 * Verify requested MaturityAge, MaturityDate or MaturityDuration 
	 * does not exceed the calculated plan TermDate (maturity date).
	 */
	protected void process_P903() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P903()"); //NBA103
			Date regMatDate = NbaUtils.getRequestedMaturityDate(getPolicy(), getAnnuity()); //SPR1986
			Date termDate = getPolicy().getTermDate();
			if (regMatDate != null && termDate != null) {
				if (NbaUtils.compare(regMatDate, termDate) > 0) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested maturity date: ", regMatDate, ", Plan maximum date: ",
							termDate), getIdOf(getAnnuity()));
				}
			}
		}
	}

	/**
	 * Verify requested MaturityDate month and day equal IssueDate month and day.
	 */
	protected void process_P069() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P069()"); //NBA103
			Date regMatDate = NbaUtils.getRequestedMaturityDate(getPolicy(), getLife(), getNbaTXLife().getPrimaryCoverage()); //SPR1986
			if (getPolicy().hasEffDate() && regMatDate != null) {
				Calendar eff = GregorianCalendar.getInstance();
				eff.setTime(getPolicy().getEffDate());
				Calendar term = GregorianCalendar.getInstance();
				term.setTime(regMatDate);
				if (eff.get(Calendar.MONTH) != term.get(Calendar.MONTH) || eff.get(Calendar.DAY_OF_MONTH) != term.get(Calendar.DAY_OF_MONTH)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested maturity date: ", regMatDate, ", Issue date: ",
							getPolicy().getEffDate()), getIdOf(getPolicy()));
				}
			}
		}
	}

	/**
	 * Verify requested MaturityDate month and day equal IssueDate month and day.
	 */
	protected void process_P920() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P920()"); //NBA103
			Date regMatDate = NbaUtils.getRequestedMaturityDate(getPolicy(), getAnnuity()); //SPR1986
			if (getPolicy().hasEffDate() && regMatDate != null) {
				Calendar eff = GregorianCalendar.getInstance();
				eff.setTime(getPolicy().getEffDate());
				Calendar term = GregorianCalendar.getInstance();
				term.setTime(regMatDate);
				if (eff.get(Calendar.MONTH) != term.get(Calendar.MONTH) || eff.get(Calendar.DAY_OF_MONTH) != term.get(Calendar.DAY_OF_MONTH)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested maturity date: ", regMatDate, ", Issue date: ",
							getPolicy().getEffDate()), getIdOf(getPolicy()));
				}
			}
		}
	}

	/*NBA237 deleted	
	 * Validate StatementBasis against table OLI_LU_STMTBASIS.  Set based upon plan business rules if not specified.  
	 * Set basis to policy year if not defined by plan business rules.
	 
	protected void process_P070() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P070()"); //NBA103
			if (!isValidTableValue(NbaTableConstants.OLI_LU_STMTBASIS, getPolicy().getStatementBasis())) {
				getPolicy().setStatementBasis(OLI_STMTBASIS_CALENDAR); //Default
				PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
				if (policyProductExtension != null) {
					for (int i = 0; i < policyProductExtension.getFinancialStatementCount(); i++) {
						FinancialStatement financialStatement = policyProductExtension.getFinancialStatementAt(i);
						if (financialStatement.getStatementType() == OLI_STMTTYPE_POLANNUAL) {
							getPolicy().setStatementBasis(financialStatement.getStatementBasis());
							break;
						}
					}
				}
				getPolicy().setActionUpdate();
			}
		}
	}*/

	
	/*NBA237 deleted	 
 	 * Validate Policyowner StatementFreq (frequency) in PolicyExtension.StatementFreq against table OLI_LU_PAYMODE.  
	 * Set based upon plan business rules if none present.
	 
	protected void process_P071() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P071()"); //NBA103
			if (!isValidTableValue(NbaTableConstants.OLI_LU_PAYMODE, getPolicyExtension().getStatementFreq())) {
				PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
				if (policyProductExtension != null) {
					for (int i = 0; i < policyProductExtension.getFinancialStatementCount(); i++) {
						FinancialStatement financialStatement = policyProductExtension.getFinancialStatementAt(i);
						if (financialStatement.getStatementType() == OLI_STMTTYPE_POLANNUAL) {
							getPolicyExtension().setStatementFreq(financialStatement.getStatementMode());
							getPolicyExtension().setActionUpdate();
							break;
						}
					}
				}
			}
		}
	}*/

	/*NBA237 deleted	
	 * Validate Confirmation Statement Frequency in PolicyExtension.ConfirmationFreq against table OLI_LU_PAYMODE.  
	 * Set based upon plan business rules if none present.
	 
	protected void process_P072() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P072()"); //NBA103
			if (!isValidTableValue(NbaTableConstants.OLI_LU_PAYMODE, getPolicyExtension().getConfirmationFreq())) {
				PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
				if (policyProductExtension != null) {
					for (int i = 0; i < policyProductExtension.getFinancialStatementCount(); i++) {
						FinancialStatement financialStatement = policyProductExtension.getFinancialStatementAt(i);
						if (financialStatement.getStatementType() == OLI_STMTTYPE_DISBEXT) {
							getPolicyExtension().setConfirmationFreq(financialStatement.getStatementMode());
							getPolicyExtension().setActionUpdate();
							break;
						}
					}
				}
			}
		}
	}
	*/
	/**
	 * Set EndorsementInd to true or false depending on whether there are any endorsements on the contract.
	 */
	protected void process_P073() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P073()"); //NBA103
			//begin SPR2919
			boolean hasEndorsement = false;
			int size = getPolicy().getEndorsementCount();
			for (int i = 0; i < size; i++) {
				if (!getPolicy().getEndorsementAt(i).isActionDelete()) {
					hasEndorsement = true;
					break;
				}
			}
			getPolicy().setEndorsementInd(hasEndorsement);
			getPolicy().setActionUpdate();
			//end SPR2919
		}
	}

	/**
	 * Set PremType (flexible or single) from AnnuityProduct.PremType.
	 */
	protected void process_P074() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P074()"); //NBA103
			AnnuityProduct annuityProduct = getAnnuityProductForPlan();
			if (annuityProduct == null || !annuityProduct.hasPremType()) {
				addPlanInfoMissingMessage("AnnuityProduct.PremType", getIdOf(getAnnuity()));
			} else {
				getAnnuity().setPremType(annuityProduct.getPremType());
				getAnnuity().setActionUpdate();
			}
		}
	}

	/**
	 * Set PremType (flexible or single) from LifeProductExtension.PremType.
	 */
	protected void process_P908() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P908()"); //NBA103
			LifeProductExtension lifeProductExtension = getLifeProductExtensionForPlan();
			if (lifeProductExtension == null || !lifeProductExtension.hasPremType()) {
				addPlanInfoMissingMessage("LifeProductExtension.PremType", getIdOf(getLife()));
			} else {
				getLifeExtension().setPremType(lifeProductExtension.getPremType());
				getLifeExtension().setActionUpdate();
			}
		}
	}

	/**
	 * Set PayoutType (Immediate or Deferred) from AnnuityProduct.PayoutType.
	 */
	protected void process_P075() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P075()"); //NBA103
			AnnuityProduct annuityProduct = getAnnuityProductForPlan();
			if (annuityProduct == null || !annuityProduct.hasPayoutType()) {
				addPlanInfoMissingMessage("AnnuityProduct.PayoutType", getIdOf(getAnnuity()));
			} else {
				getAnnuity().setPayoutType(annuityProduct.getPayoutType());
				getAnnuity().setActionUpdate();
			}
		}
	}

	/**
	 * Validate requested QualPlanType (qualification type) against table OLI_LU_QUALPLAN. 
	 * Verify that QualPlanType is allowed based on Policy.Product.QualifiedPlanCC. 
	 * Assume plan is non-qualified (tc=1) if Policy.Product.QualifiedPlanCC is not present.
	 */
	protected void process_P076() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P076()"); //NBA103
			// SPR3290 code deleted
			//begin	SPR1705
			//SPR1859 deleted code
			long qualPlanType = getAnnuity().getQualPlanType();
			boolean invalid = true;
			if (isValidTableValue(NbaTableConstants.OLI_LU_QUALPLAN, qualPlanType)) {
				if (qualPlanType == OLI_QUALPLN_NONE) {
					invalid = false;
				} else {
					PolicyProduct policyProduct = getPolicyProductForPlan();
					if (policyProduct == null || !policyProduct.hasQualifiedPlanCC()) {
						getAnnuity().setQualPlanType(OLI_QUALPLN_NONE);
						getAnnuity().setActionUpdate();
						invalid = false;
					} else {
						QualifiedPlanCC qualifiedPlanCC = policyProduct.getQualifiedPlanCC();
						for (int i = 0; i < qualifiedPlanCC.getQualifiedPlanCount(); i++) {
							if (qualPlanType == qualifiedPlanCC.getQualifiedPlanAt(i)) {
								invalid = false;
								break;
							}
						}
					}
				}
			}
			if (invalid) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Qualification Type: ", qualPlanType), getIdOf(getAnnuity()));
			}
			//end SPR1705
		}
	}

	/**
	 * Validate requested DeathBenefitOptType against table OLI_LU_DTHBENETYPE.  
	 * Copy DeathBenefitOptType from Base Coverage if none specified and present in Base coverage else
	 * Set to PolicyProduct.LifeProduct.CoverageProduct.DeathBenefitOptCC (plan default).
	 */
	protected void process_P077() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P077() for ", getCoverage()); //NBA103
			if (!getCoverage().hasDeathBenefitOptType()) {
				//begin SPR1706
				boolean updated = false; //SPR1860
				if (getCoverage().getIndicatorCode() != OLI_COVIND_BASE) {
					for (int i = 0; i < getLife().getCoverageCount(); i++) { //Use value from Basic Coverage
						Coverage tempCov = getLife().getCoverageAt(i);
						if (tempCov.getIndicatorCode() == OLI_COVIND_BASE) {
							if (tempCov.hasDeathBenefitOptType()) { //SPR1860
								getCoverage().setDeathBenefitOptType(tempCov.getDeathBenefitOptType());
								updated = true; //SPR1860
								break;//SPR1860
							}
						}
					}
				}
				if (!updated) { //SPR1860
					CoverageProduct coverageProduct = getCoverageProductFor(getCoverage());
					if (coverageProduct != null && coverageProduct.hasDeathBenefitOptCC()) {
						DeathBenefitOptCC deathBenefitOptCC = coverageProduct.getDeathBenefitOptCC();
						if (deathBenefitOptCC.getDeathBenefitOptCount() > 0) {
							getCoverage().setDeathBenefitOptType(deathBenefitOptCC.getDeathBenefitOptAt(0));
						}
					}
					//end SPR1706
				}
			}
			long deathBenefitOptType = getCoverage().getDeathBenefitOptType();
			if (!isValidTableValue(NbaTableConstants.OLI_LU_DTHBENETYPE, deathBenefitOptType)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Requested death benefit option: ", deathBenefitOptType),
						getIdOf(getCoverage()));
			}
		}
	}

	/**
	 * Validate coverage EffDate is later than or equal to effective date of primary coverage for other than a submitted application.
	 * Update the Policy EffDate from the primary coverage EffDate.
	 */
	protected void process_P078() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P078() for ", getCoverage()); //NBA103
			Coverage primaryCov = getNbaTXLife().getPrimaryCoverage();
			if (primaryCov.hasEffDate() && getCoverage().hasEffDate() && primaryCov.getEffDate().after(getCoverage().getEffDate())) { //SPR1800
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
			}
			//SPR1800 code deleted
		}
	}

	/**
	 * Calculate CoverageExtension.PayUpDate. Use PolicyProductExtension.PayUpCalcMethod to determine if the PayUpDate is calculated 
	 * based upon attained age or duration.  If attained age, then calculate the years difference between 
	 * issue age and payup age.  Add years difference to EffDate to get PayUpDate result.  If duration, 
	 * add pay up duration number of years to EffDate to get PayUpDate result.
	 * If there is a Requested Maturity date and the PayUpDate cannot be calculated or the PayUpDate is 
	 * greater than the Requested Maturity date, set the PayUpDate to the Requested Maturity date.
	 * Verify PayUpDate date for a non-primary coverage is less than or equal to that of primary coverage.  
	 * If not, set PayUpDate date equal to that of the primary coverage.
	 * If the pay up date cannot be calculated, set it to the effective date as a default value and generate a validation error.
	 */
	protected void process_P079() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P079() for ", getCoverage()); //NBA103
			//begin SPR1986			
			boolean calculated = false;
			CoverageExtension coverageExtension = null;
			if (getCoverage().hasEffDate()) {
				coverageExtension = getCoverageExtension();
				//SPR3147 code deleted
				PolicyProduct policyProduct = getPolicyProductFor(getCoverage());
				if (policyProduct != null) {
					PolicyProductExtension policyProductExtension = getPolicyProductExtensionFor(getCoverage());
					if (policyProductExtension != null && policyProductExtension.hasPayUpCalcMethod()) {
						if (policyProductExtension.getPayUpCalcMethod() == OLIX_MATURITYCALCMETH_DURATION && policyProduct.hasPayToYear()) {
							coverageExtension.setPayUpDate(addYears(getCoverage().getEffDate(), policyProduct.getPayToYear()));
							calculated = true;
						} else if (policyProductExtension.getPayUpCalcMethod() == OLIX_MATURITYCALCMETH_ATTAINEDAGE) {
							LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(getCoverage());
							if (lifeParticipant != null && lifeParticipant.hasIssueAge()) {
								int years = policyProduct.getPayToAge() - lifeParticipant.getIssueAge();
								if (years < 0) {
									years = 0;
								}
								coverageExtension.setPayUpDate(addYears(getCoverage().getEffDate(), years));
								calculated = true;
							}
						}
					}
				}
				//SPR3147 code deleted
				Date regMatDate = NbaUtils.getRequestedMaturityDate(getPolicy(), getLife(), getCoverage());
				if (regMatDate != null) {
					if (!calculated || NbaUtils.compare(coverageExtension.getPayUpDate(), regMatDate) > 0) {
						coverageExtension.setPayUpDate(regMatDate);
						calculated = true;
					}
				}
				if (calculated) {
					if (getCoverage().getIndicatorCode() != OLI_COVIND_BASE) {
						Coverage primaryCov = getNbaTXLife().getPrimaryCoverage();
						CoverageExtension primaryCovExtension = NbaUtils.getFirstCoverageExtension(primaryCov);
						if (primaryCovExtension.hasPayUpDate()) {
							if (NbaUtils.compare(primaryCovExtension.getPayUpDate(), coverageExtension.getPayUpDate()) < 0) {
								coverageExtension.setPayUpDate(primaryCovExtension.getPayUpDate());
							}
						}
					}
				} else {
					coverageExtension.setPayUpDate(getCoverage().getEffDate());
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
				}
				coverageExtension.setActionUpdate();
			}
			//end SPR1986			
		}
	}

	/**
	 * Calculate Coverage.TermDate (mature/expiry date). Use CoverageProductExtension.MaturityAgeUse to 
	 * determine if the TermDate is calculated based upon attained age or duration.  If attained age, then 
	 * calculate the years difference between issue age and maturity age.  Add years difference to EffDate 
	 * to get TermDate result.  If duration, add maturity duration number of years to EffDate to get TermDate result.
	 * If there is a Requested Maturity date and the TermDate cannot be calculated or the TermDate is 
	 * greater than the Requested Maturity date, set the TermDate to the Requested Maturity date.  
	 * Verify TermDate date for a rider coverage is less than or equal to that of primary coverage.  
	 * If not, set rider TermDate date equal to that of the primary coverage.
	 * If the TermDate cannot be calculated and a Requested Maturity date is not present, set TermDate to the 
	 * effective date as a default value and generate a validation error.
	 * When calculating the TermDate for the primary coverage, also update Policy.TermDate.
	 */
	protected void process_P080() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P080() for ", getCoverage()); //NBA103
			//begin SPR1986
			boolean calculated = false;
			// SPR3290 code deleted
			if (getCoverage().hasEffDate()) {
				CoverageProduct coverageProduct = getCoverageProductFor(getCoverage());
				if (coverageProduct != null) {
					CoverageProductExtension cpe = getCoverageProductExtensionFor(getCoverage());
					if (cpe != null && cpe.hasMaturityAgeUse()) {
						if (cpe.getMaturityAgeUse() == OLI_AGEDATEUSE_COVANNBEG && cpe.hasMaturityDuration()) {
							getCoverage().setTermDate(addYears(getCoverage().getEffDate(), cpe.getMaturityDuration()));
							calculated = true;
						} else if (cpe.getMaturityAgeUse() == OLI_AGEDATEUSE_BIRTHDAY && coverageProduct.hasMaturityAge()) {
							LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(getCoverage());
							if (lifeParticipant != null && lifeParticipant.hasIssueAge()) {
								int years = coverageProduct.getMaturityAge() - lifeParticipant.getIssueAge();
								if (years < 0) {
									years = 0;
								}
								getCoverage().setTermDate(addYears(getCoverage().getEffDate(), years));
								calculated = true;
							}
						}
					}
				}
				Date regMatDate = NbaUtils.getRequestedMaturityDate(getPolicy(), getLife(), getCoverage());
				if (regMatDate != null) {
					if (!calculated || NbaUtils.compare(getCoverage().getTermDate(), regMatDate) > 0) {
						getCoverage().setTermDate(regMatDate);
						calculated = true;
					}
				}
				if (calculated) {
					if (getCoverage().getIndicatorCode() != OLI_COVIND_BASE) {
						Coverage primaryCov = getNbaTXLife().getPrimaryCoverage();
						if (primaryCov.hasTermDate()) {
							if (NbaUtils.compare(primaryCov.getTermDate(), getCoverage().getTermDate()) < 0) {
								getCoverage().setTermDate(primaryCov.getTermDate());
							}
						}
					}
				} else {
					getCoverage().setTermDate(getCoverage().getEffDate());
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
				}
				getCoverage().setActionUpdate();
				//begin NBA100
				if (getCoverage().getIndicatorCode() == OLI_COVIND_BASE) {
					getPolicy().setTermDate(getCoverage().getTermDate());
					getPolicy().setActionUpdate();
				}
				//end NBA100
			}
			//end SPR1986			
		}
	}

	/**
	 * Set CovOption.PlanName from CovOptionProduct.PlanName.
	 */
	//NBA100 New Method
	protected void process_P223() throws Exception {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValMisc.process_P223 for ", getCovOption()); //NBA103
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
			if (covOptionProduct != null) {
				getCovOption().setPlanName(covOptionProduct.getPlanName());
				getCovOption().setShortName(covOptionProduct.getPlanName());//ALII2013
				getCovOption().setActionUpdate();
			}
		}
	}

	/**
	 * Validate rider EffDate is not less than the EffDate of the Annuity for other than submitted application.
	 */
	protected void process_P081() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P081() for ", getRider()); //NBA103
			if (getPolicy().getEffDate().after(getRider().getEffDate())) { //SPR2408
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getRider()));
			}
		}
	}

	/**
	 * Calculate the Policy.TermDate (mature/expiry date) for an Annuity.  Use AnnuityProductExtension.MaturityAgeUse to 
	 * determine if the TermDate is calculated based upon attained age or duration.  If attained age, then 
	 * calculate the years difference between issue age and maturity age.  Add years difference to EffDate 
	 * to get TermDate result.  If duration, add maturity duration number of years to EffDate to get TermDate result.
	 * If there is a Requested Maturity date and the TermDate cannot be calculated or the TermDate is 
	 * greater than the Requested Maturity date, set the TermDate to the Requested Maturity date.  
	 * If the TermDate cannot be calculated and a Requested Maturity date is not present, set TermDate to the 
	 * effective date as a default value and generate a validation error.
	 */
	protected void process_P082() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P082()"); //NBA103
			//begin SPR1986		
			boolean calculated = false;
			if (getPolicy().hasEffDate()) {
				AnnuityProductExtension ape = getAnnuityProductExtension();
				if (ape != null && ape.hasMaturityAgeUse()) {
					if (ape.getMaturityAgeUse() == OLI_AGEDATEUSE_BIRTHDAY && ape.hasMaturityAge()) {
						Participant participant = NbaUtils.findPrimaryInsuredParticipant(getAnnuity().getPayoutAt(0).getParticipant());
						if (participant != null && participant.hasIssueAge()) {
							// SPR3290 code deleted
							int years = ape.getMaturityAge() - participant.getIssueAge();
							if (years < 0) {
								years = 0;
							}
							getPolicy().setTermDate(addYears(getPolicy().getEffDate(), years));
							calculated = true;
						}
					} else if (ape.getMaturityAgeUse() == OLI_AGEDATEUSE_COVANNBEG && ape.hasMaturityDuration()) {
						getPolicy().setTermDate(addYears(getPolicy().getEffDate(), ape.getMaturityDuration()));
						calculated = true;
					}
				}
				Date regMatDate = NbaUtils.getRequestedMaturityDate(getPolicy(), getAnnuity());
				if (regMatDate != null) {
					if (!calculated || NbaUtils.compare(getPolicy().getTermDate(), regMatDate) > 0) {
						getPolicy().setTermDate(regMatDate);
						calculated = true;
					}
				}
				if (!calculated) {
					getPolicy().setTermDate(getPolicy().getEffDate());
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getAnnuity()));
				} else {
					getPolicy().setActionUpdate();
				}
			}
		}
		//end SPR1986		
	}

	/**
	 * Calculate AnnuityRiderExtension.PayUpDate. Use PolicyProductExtension.PayUpCalcMethod to determine if 
	 * the PayUpDate is calculated based upon attained age or duration.  If attained age, then calculate 
	 * the years difference betweenissue age and payup age.  Add years difference to EffDate to get PayUpDate result.  
	 * If duration, add pay up duration number of years to EffDate to get PayUpDate result.
	 * If there is a Requested Maturity date and the PayUpDate cannot be calculated or the PayUpDate is 
	 * greater than the Requested Maturity date, set the PayUpDate to the Requested Maturity date.
	 * If the PayUpDate cannot be calculated and a Requested Maturity date is not present, set PayUpDate to the 
	 * effective date as a default value and generate a validation error.
	 * Verify PayUpDate for a rider coverage is not greater than Policy.TermDate. If it is, 
	 * set rider PayUpDate to Policy.TermDate.
	 */
	protected void process_P083() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P083() for ", getRider()); //NBA103
			//begin SPR1986			
			boolean calculated = false;
			if (getRider().hasEffDate()) {
				PolicyProduct policyProduct = getPolicyProductFor(getRider());
				if (policyProduct != null) {
					PolicyProductExtension policyProductExtension = getPolicyProductExtensionFor(policyProduct);
					if (policyProductExtension != null && policyProductExtension.hasPayUpCalcMethod()) {
						if (policyProductExtension.getPayUpCalcMethod() == OLIX_MATURITYCALCMETH_DURATION && policyProduct.hasPayToYear()) {
							getAnnuityRiderExtension().setPayUpDate(addYears(getRider().getEffDate(), policyProduct.getPayToYear()));
							calculated = true;
						} else if (policyProductExtension.getPayUpCalcMethod() == OLIX_MATURITYCALCMETH_ATTAINEDAGE) {
							Participant participant = NbaUtils.getInsurableParticipant(getRider());
							if (participant != null && participant.hasIssueAge()) {
								int years = policyProduct.getPayToAge() - participant.getIssueAge();
								if (years < 0) {
									years = 0;
								}
								getAnnuityRiderExtension().setPayUpDate(addYears(getRider().getEffDate(), years));
								calculated = true;
							}
						}
					}
				}
				Date regMatDate = NbaUtils.getRequestedMaturityDate(getPolicy(), getAnnuity());
				if (regMatDate != null) {
					if (!calculated || NbaUtils.compare(getAnnuityRiderExtension().getPayUpDate(), regMatDate) > 0) {
						getAnnuityRiderExtension().setPayUpDate(regMatDate);
						calculated = true;
					}
				}
				if (!calculated) {
					getAnnuityRiderExtension().setPayUpDate(getRider().getEffDate());
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getRider()));
				}
				if (getPolicy().hasTermDate()) {
					if (NbaUtils.compare(getPolicy().getTermDate(), getAnnuityRiderExtension().getPayUpDate()) < 0) {
						getAnnuityRiderExtension().setPayUpDate(getPolicy().getTermDate());
					}
				}
				getAnnuityRiderExtension().setActionUpdate();
				//end SPR1986				
			}
		}
	}

	/**
	 * Calculate the TermDate (mature/expiry date) for rider coverage.  Use CoverageProductExtension.MaturityAgeUse to 
	 * determine if the TermDate is calculated based upon attained age or duration.  If attained age, then 
	 * calculate the years difference between issue age and maturity age.  Add years difference to EffDate 
	 * to get TermDate result.  If duration, add maturity duration number of years to EffDate to get TermDate result.
	 * If there is a Requested Maturity date and the TermDate cannot be calculated or the TermDate is 
	 * greater than the Requested Maturity date, set the TermDate to the Requested Maturity date.  
	 * If the TermDate cannot be calculated and a Requested Maturity date is not present, set TermDate to the 
	 * effective date as a default value and generate a validation error. 
	 * Verify TermDate for rider coverage is not greater than Policy.TermDate. 
	 * If it is, set TermDate equal to Policy.TermDate.
	 */
	protected void process_P084() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P084() for ", getRider()); //NBA103
			//begin SPR1986
			if (getRider().hasEffDate()) {
				boolean calculated = false;
				CoverageProduct coverageProduct = getCoverageProductFor(getRider());
				CoverageProductExtension cpe = getCoverageProductExtensionFor(coverageProduct);
				if (cpe != null && cpe.hasMaturityAgeUse()) {
					if (cpe.getMaturityAgeUse() == OLI_AGEDATEUSE_COVANNBEG && cpe.hasMaturityDuration()) {
						getRider().setTermDate(addYears(getRider().getEffDate(), cpe.getMaturityDuration()));
						calculated = true;
					} else if (cpe.getMaturityAgeUse() == OLI_AGEDATEUSE_BIRTHDAY && coverageProduct.hasMaturityAge()) {
						Participant participant = NbaUtils.getInsurableParticipant(getRider());
						if (participant != null && participant.hasIssueAge()) {
							int years = coverageProduct.getMaturityAge() - participant.getIssueAge();
							if (years < 0) {
								years = 0;
							}
							getRider().setTermDate(addYears(getRider().getEffDate(), years));
							calculated = true;
						}
					}
				}
				Date regMatDate = NbaUtils.getRequestedMaturityDate(getPolicy(), getAnnuity());
				if (regMatDate != null) {
					if (!calculated || NbaUtils.compare(getRider().getTermDate(), regMatDate) > 0) {
						getRider().setTermDate(regMatDate);
						calculated = true;
					}
				}
				if (!calculated) {
					getRider().setTermDate(getRider().getEffDate());
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getRider()));
				}
				if (getPolicy().hasTermDate()) {
					if (NbaUtils.compare(getPolicy().getTermDate(), getRider().getTermDate()) < 0) {
						getRider().setTermDate(getPolicy().getTermDate());
					}
				}
				getRider().setActionUpdate();
			}
			//end SPR1986	
		}
	}

	/**
	 * Verify EffDate is present for reissue or complex change contract
	 */
	protected void process_P085() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P085() for ", getCoverage()); //NBA103
			if (!getCoverage().hasEffDate()) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
			}
		}
	}

	/**
	 * Verify EffDate is present for reissue or complex change contract
	 */
	protected void process_P086() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P086() for ", getPolicy()); //NBA103
			if (!getPolicy().hasEffDate()) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPolicy()));
			}
		}
	}

	/**
	 * Verify EffDate is present for reissue or complex change contract
	 */
	protected void process_P087() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P087() for ", getRider()); //NBA103
			if (!getRider().hasEffDate()) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getRider()));
			}
		}
	}

	/**
	 * Determine whether the contract is participating or nonparticipating based on plan data (PolicyProductExtension.ParticipatingType).
	 * If nonparticipating (PolicyProductExtension.ParticipatingType is OLI_PARTYPE_BASIC or OLI_UNKNOWN or - 1), 
	 * set Life.DivType(primary dividend option) and LifeExtension.SecondaryDividendType(secondary dividend option) to OLI_DIVOPT_NONE = 1.
	 * If Participating and no value is present for the primary or secondary option, set the missing value(s) to the plan default 
	 * (PolicyProductExtension.Dividend.DivType where Dividend.DefaultInd = true), if present.
	 * If Participating, verify that the primary and secondary dividend options allowed by matching against PolicyProductExtension.Dividend.DivType.
	 */
	protected void process_P088() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P088()"); //NBA103
			//begin SPR1744
			//Begin AXAL3.7.40
			PolicyProduct policyProduct = getPolicyProductForPlan();
			if (policyProduct == null || !policyProduct.hasParticipatingInd()) {
				addPlanInfoMissingMessage("PolicyProductExtension.ParticipatingType", getIdOf(getLife()));
				//end SPR1744
			} else {
				boolean participatingInd = policyProduct.getParticipatingInd();//SPR1744
				if (participatingInd) { //SPR1744
					//NBA107 - refactored inline code
					validateDivType();
					//End AXAL3.7.40
				} else {
					getLife().setDivType(OLI_DIVOPT_NONE);
					getLifeExtension().setSecondaryDividendType(OLI_DIVOPT_NONE);
					getLife().setActionUpdate();
					getLifeExtension().setActionUpdate();
				}
			}
		}
	}

	//P2AXAL055 Code deleted-P089

	/**
	 * Retrieve required minimum distribution payout option and calculation method 
	 * based on plan business rules.
	 */
	protected void process_P090() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P090()"); //NBA103
		}
	}

	/**
	 * Verify BlendedInsTargetDBAmtPct (target death benefit) present for blended insurance rider 
	 * (PolicyProduct.TypeCode tc="107").
	 */
	protected void process_P091() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P091() for ", getCoverage()); //NBA103
			PolicyProduct policyProduct = getPolicyProductFor(getCoverage());
			if (policyProduct == null) {
				addPlanInfoMissingMessage("PolicyProduct", getIdOf(getCoverage()));
			} else {
				if (policyProduct.getPolicyProductTypeCode() == OLI_PRODTYPE_BLENDED) {
					getCoverageExtension().setBlendedInsTargetDBInd(OLIX_TARGETDB_RATIO);
					getCoverageExtension().setActionUpdate();
					if (!(getCoverageExtension().hasBlendedInsTargetDBAmtPct() && getCoverageExtension().getBlendedInsTargetDBAmtPct() > 0)) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
					}
				}
			}
		}
	}

	/**
	 * Validate value for BlendedInsTargetDBInd against OLIEXT_LU_TARGETDB when a BlendedInsTargetDBAmtPct has been specified. Set to "0" (unknown) if missing or invalid. 
	 */
	protected void process_P910() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P910() for ", getCoverage()); //NBA103
			PolicyProduct policyProduct = getPolicyProductFor(getCoverage());
			if (policyProduct == null) {
				addPlanInfoMissingMessage("PolicyProduct", getIdOf(getCoverage()));
			} else {
				if (policyProduct.getPolicyProductTypeCode() == OLI_PRODTYPE_BLENDED) {
					if (getCoverageExtension().hasBlendedInsTargetDBAmtPct() && getCoverageExtension().getBlendedInsTargetDBAmtPct() > 0) {
						if (isValidTableValue(NbaTableConstants.OLIEXT_LU_TARGETDB, getCoverageExtension().getBlendedInsTargetDBInd())) {
							return;
						}
					}
					getCoverageExtension().setBlendedInsTargetDBInd(0);
					getCoverageExtension().setActionUpdate();
				}
			}
		}
	}

	/**
	 * Validate coverage amount against plan limits for maximum amount allowed to issue substandard.
	 */
	protected void process_P092() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P092() for ", getCoverage()); //NBA103
			LifeParticipant lifeParticipant = getNbaTXLife().getPrimaryInuredLifeParticipant();
			if (lifeParticipant != null) {
				SubstandardRisk substandardRisk = getSubstandardRiskFor(getCoverage()); //SPR1707
				if (substandardRisk != null) { //SPR1707
					//SPR1707 code deleted
					double max = substandardRisk.getMaxIssueAmt();
					double amt = getCoverage().getCurrentAmt();
					if (!Double.isNaN(max) && amt > max) { //SPR1707
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat(concatAmt("Requested coverage amount: ", amt), concatAmt(
								", Plan maximum limit amount: ", max)), getIdOf(getCoverage()));
					}
				}
			}
		}
	}

	/**
	 * Validate rated coverage to determine whether the coverage can be issued substandard by checking for the presence of a 
	 * CoverageProductExtension.SubstandardRisk object. If one does not exist, then the coverage cannot be issued substandard.
	 */
	protected void process_P093() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P093() for ", getCoverage()); //NBA103
			//begin SPR1706
			if (getSubstandardRiskFor(getCoverage()) == null) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
			}
			//end SPR1706			 
		}
	}

	/**
	 * Validate IssueAge against SubstandardRisk.MaxIssueAge for maximum age allowed to issue substandard.
	 */
	protected void process_P094() {
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P094() for ", getLifeParticipant()); //NBA103
			SubstandardRisk substandardRisk = getSubstandardRiskFor(getCoverage()); //SPR1707
			if (substandardRisk != null && substandardRisk.hasMaxIssueAge()) { //SPR1707
				//SPR1707 coded deleted
				int maxAge = substandardRisk.getMaxIssueAge();
				int age = getLifeParticipant().getIssueAge();
				if (age > maxAge) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", age, ", Maximum age: ", maxAge),
							getIdOf(getLifeParticipant()));
				}
			}
		}
	}

	/**
	 * Validate IssueAge against SubstandardRisk.MinIssueAge for minimum age allowed to issue substandard..
	 */
	protected void process_P907() {
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P907() for ", getLifeParticipant()); //NBA103
			SubstandardRisk substandardRisk = getSubstandardRiskFor(getCoverage()); //SPR1707
			if (substandardRisk != null && substandardRisk.hasMinIssueAge()) { //SPR1707
				//SPR1707 coded deleted
				int minAge = substandardRisk.getMinIssueAge();
				int age = getLifeParticipant().getIssueAge();
				if (age < minAge) { //SPR1707
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", age, ", Minimum age: ", minAge),
							getIdOf(getLifeParticipant()));
				}
			}
		}
	}

	/**
	 * Validate PermTableRating or TempTableRating does not exceed maximum allowed for plan based on plan business rules.
	 */
	protected void process_P095() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			if (getSubstandardRating().hasPermTableRating() || getSubstandardRating().hasTempTableRating()) {
				logDebug("Performing NbaValInsurance.process_P095() for ", getSubstandardRating()); //NBA103
				long tableRating = 999;
				if (getSubstandardRating().hasPermTableRating()) {
					tableRating = getSubstandardRating().getPermTableRating();
				} else if (getSubstandardRating().hasTempTableRating()) {
					tableRating = getSubstandardRating().getTempTableRating();
				}
				CoverageProduct coverageProduct = getCoverageProductFor(getCoverage());
				if (coverageProduct != null && coverageProduct.hasMaxTableRating()) { //SPR1707
					//SPR1707 code deleted
					long maxRating = coverageProduct.getMaxTableRating();
					if (tableRating > maxRating) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Table rating: ", tableRating, ", Maximum table rating: ",
								maxRating), getIdOf(getLifeParticipant()));
					}
				}
			}
		}
	}

	/**
	 * Calculate TempTableRatingEndDate/TempFlatEndDate based on Duration value and Cease date.
	 */
	protected void process_P096() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			if (getSubstandardRating().hasTempFlatExtraAmt() || getSubstandardRating().hasTempTableRating()) {
				//ALS5282 code deleted to set TempFlatEndDate everytime depending upon the EffDate
				logDebug("Performing NbaValInsurance.process_P096() for ", getSubstandardRating()); //NBA103
				SubstandardRatingExtension substandardRatingExtension = getSubstandardRatingExtension();
				if (!substandardRatingExtension.hasEffDate()) {
					substandardRatingExtension.setEffDate(getCoverage().getEffDate());
				}
				Calendar cal = GregorianCalendar.getInstance();
				cal.setTime(substandardRatingExtension.getEffDate());
				if (substandardRatingExtension.hasDuration()) {
					cal.add(Calendar.YEAR, substandardRatingExtension.getDuration());
					if (getSubstandardRating().hasTempTableRating()) {
						getSubstandardRating().setTempTableRatingEndDate(cal.getTime());
						getLifeParticipant().setTempTableRatingEndDate(cal.getTime());
					} else {
						getSubstandardRating().setTempFlatEndDate(cal.getTime());
						getLifeParticipant().setTempFlatEndDate(cal.getTime());
					}
					getSubstandardRating().setActionUpdate();
					getLifeParticipant().setActionUpdate();
				} else {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getSubstandardRating()));
				}
			}
		}
	}

	/**
	 * Validate TempTableRatingEndDate/TempFlatEndDate does not exceed the associated coverage pay up date
	 */
	protected void process_P097() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			if (getSubstandardRating().hasTempFlatExtraAmt() || getSubstandardRating().hasTempTableRating()) {
				logDebug("Performing NbaValInsurance.process_P097() for ", getSubstandardRating()); //NBA103
				Date endDate = null;
				if (getSubstandardRating().hasTempTableRating()) {
					endDate = getSubstandardRating().getTempTableRatingEndDate();
				} else {
					endDate = getSubstandardRating().getTempFlatEndDate();
				}
				if (endDate != null) {
					if (!(NbaUtils.compare(getCoverage().getTermDate(), endDate) < 0)) { //QC2543
						return;
					}
				}
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getLifeParticipant()));
			}
		}
	}

	/**
	 * Validate EffDate of rating.  If no EffDate present, or if EffDate is prior to the associated coverage EffDate, use the associated coverage EffDate.
	 */
	protected void process_P098() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			logDebug("Performing NbaValInsurance.process_P098() for ", getSubstandardRating()); //NBA103
			SubstandardRatingExtension substandardRatingExtension = getSubstandardRatingExtension();
			if (!substandardRatingExtension.hasEffDate() || NbaUtils.compare(getCoverage().getEffDate(), substandardRatingExtension.getEffDate()) < 0) {
				substandardRatingExtension.setEffDate(getCoverage().getEffDate());
				getSubstandardRating().setActionUpdate();
			}
		}
	}

	/**
	 * Set coverage, substandard extra, and coverage option statuses from the action indicators.
	 */
	//New Method SPR1105
	protected void process_P099() {
		if (verifyCtl(COVERAGE)) {
			Coverage coverage = getCoverage();
			String thisCovID = getIdOf(coverage);
			logDebug("Performing NbaValInsurance.process_P099() for " + thisCovID);
			if (!coverage.isActionDelete() && !coverage.hasLifeCovStatus()) {
				coverage.setLifeCovStatus(NbaOliConstants.OLI_POLSTAT_ACTIVE);
				coverage.setActionUpdate();
			}
			for (int i = 0; i < coverage.getLifeParticipantCount(); i++) {
				LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(i);
				for (int j = 0; j < lifeParticipant.getSubstandardRatingCount(); j++) {
					SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(j);
					if (NbaUtils.isValidRating(substandardRating)) { //SPR3098
						SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
						if (substandardRatingExtension == null) {
							OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SUBSTANDARDRATING);
							substandardRating.addOLifEExtension(olifeExt);
							substandardRatingExtension = olifeExt.getSubstandardRatingExtension();
							substandardRatingExtension.setActionAdd();
						}
						if (!substandardRatingExtension.hasRatingStatus()) {
							substandardRatingExtension.setRatingStatus(NbaOliConstants.OLI_POLSTAT_ACTIVE);
							substandardRatingExtension.setActionUpdate();
						}
					}
				}
			}
			for (int i = 0; i < coverage.getCovOptionCount(); i++) {
				CovOption covOption = coverage.getCovOptionAt(i);
				if (covOption.isAction(NbaActionIndicator.ACTION_DENY)) { //ALS5098 refactored
					covOption.setCovOptionStatus(NbaOliConstants.OLI_POLSTAT_DECISSUE);
					covOption.setAction(null); //reset it because Update can not override Deny
					covOption.setActionUpdate();
				} else if (!covOption.hasCovOptionStatus()) {
					covOption.setCovOptionStatus(NbaOliConstants.OLI_POLSTAT_ACTIVE);
					covOption.setActionUpdate();
				}
			}
		}
	}

	/**
	 * Set RatingCommissionRule (whether premium for the substandard rating is to be commissionable) from SubstandardRisk.RatingCommissionRule.
	 */
	protected void process_P100() {
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P100() for ", getLifeParticipant()); //NBA103
			SubstandardRisk substandardRisk = getSubstandardRiskFor(getCoverage()); //SPR1707
			if (substandardRisk != null && substandardRisk.hasRatingCommissionRule()) { //SPR1707
				//SPR1707 coded deleted
				getLifeParticipant().setRatingCommissionRule(substandardRisk.getRatingCommissionRule());
				getLifeParticipant().setActionUpdate();
				// Begin SPR1707 
				for (int i = 0; i < getLifeParticipant().getSubstandardRatingCount(); i++) {
					getLifeParticipant().getSubstandardRatingAt(i).setRatingCommissionRule(substandardRisk.getRatingCommissionRule());
					getLifeParticipant().getSubstandardRatingAt(i).setActionUpdate();
				}
				// End SPR1707				
			}
		}
	}

	/**
	 * Retrieve substandard ExtraPremPerUnit rate based on plan business rules.
	 */
	protected void process_P101() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			logDebug("Performing NbaValInsurance.process_P101() for ", getSubstandardRating()); //NBA103
			// begin NBA104
			if (isValidCoverageTableRating()) {
				SubstandardRatingExtension substandardRatingExt = NbaUtils.getFirstSubstandardExtension(getSubstandardRating());
				if (substandardRatingExt == null || !substandardRatingExt.hasExtraPremPerUnit()) {
					updateExtraPremPerUnit(getSubstandardRating());
				}
			}
			// end NBA104
		}
	}

	/**
	 * Validate SpecialClass code value against table NBA_SpecialClass.
	 */
	protected void process_P102() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			logDebug("Performing NbaValInsurance.process_P102() for ", getSubstandardRating()); //NBA103
			String specialClass = "";
			if (getSubstandardRatingExtension().hasSpecialClass()) {
				specialClass = getSubstandardRatingExtension().getSpecialClass().trim(); //SPR1945
			}
			//begin SPR2103
			CoverageProductExtension coverageProductExtension = getCoverageProductExtensionFor(getCoverage());
			if (null != coverageProductExtension && specialClass.length() > 0) {
				int count = coverageProductExtension.getSubstandardRiskCount();
				SubstandardRisk substandardRisk = null;
				for (int i = 0; i < count; i++) {
					substandardRisk = coverageProductExtension.getSubstandardRiskAt(i);
					String coverageSpecialClass = substandardRisk.getSpecialClass();
					if (null != coverageSpecialClass) {
						if (coverageSpecialClass.equals(specialClass)
								|| (NbaUtils.isStringAsterisk(coverageSpecialClass) && coverageSpecialClass.length() == specialClass.length())) {
							return;
						}
					}
				}
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Special class: ", specialClass), getIdOf(getSubstandardRating()));
			}
			//end SPR2103
		}
	}

	/**
	 * Validate SpecialClass code value against CovOption.
	 */
	//SPR2103 New Method
	protected void process_P236() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			logDebug("Performing NbaValInsurance.process_P235() for ", getSubstandardRating());
			String specialClass = "";
			if (getSubstandardRatingExtension() != null && getSubstandardRatingExtension().hasSpecialClass()) {
				specialClass = getSubstandardRatingExtension().getSpecialClass().trim();
			}
			CovOptionProduct covOptionProduct = getCovOptionProductFor(coverage, covOption);
			if (covOptionProduct != null) {
				CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(covOptionProduct);
				if (null != covOptionProductExtension && specialClass.length() > 0) {
					int count = covOptionProductExtension.getSubstandardRiskCount();
					SubstandardRisk substandardRisk = null;
					for (int i = 0; i < count; i++) {
						substandardRisk = covOptionProductExtension.getSubstandardRiskAt(i);
						String covOptionSpecialClass = substandardRisk.getSpecialClass();
						if (null != covOptionSpecialClass) {
							if (covOptionSpecialClass.equals(specialClass)
									|| (NbaUtils.isStringAsterisk(covOptionSpecialClass) && covOptionSpecialClass.length() == specialClass.length())) {
								return;
							}
						}
					}
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Special class: ", specialClass), getIdOf(getSubstandardRating()));
				}
			}
		}
	}

	/**
	 * Validate extended insurance allowed with substandard rating based on PolicyProduct.PolicyProductExtension.NonForProvision.AllowedSubstandard 
	 * when Life.NonFortProv is ETI.
	 * If not allowed, change to default nonforfeiture option.
	 */
	protected void process_P103() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			long nonFortProv = getLife().getNonFortProv();
			if (nonFortProv == OLI_NONFORF_EXTERM || nonFortProv == OLI_NONFORF_APLEXTTERM) {
				boolean useDefault = true;
				logDebug("Performing NbaValInsurance.process_P103() for ", getSubstandardRating()); //NBA103
				NonForProvision nonForProvision = getNonForProvisionForPlan(nonFortProv);
				if (nonForProvision == null) {
					addPlanInfoMissingMessage("NonForProvision", getIdOf(getLife()));
				} else {
					for (int j = 0; j < nonForProvision.getAllowedSubstandardCount(); j++) {
						AllowedSubstandard allowedSubstandard = nonForProvision.getAllowedSubstandardAt(j);
						if (allowedSubstandard.getAllowPctRatingInd()) { //SPR2413
							useDefault = false;
							break;
						}
					}
				}
				if (useDefault) {
					// Begin SPR3157
					NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation();
					if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful() && nbaVpmsResultsData.getResultsData().size() == 1) {
						String defaultNonFortProv = (String) nbaVpmsResultsData.getResultsData().get(0);
						getLife().setNonFortProv(NbaUtils.convertStringToLong(defaultNonFortProv));
						// End SPR3157
						getLife().setActionUpdate();
					} else { //SPR3157
						addPlanInfoMissingMessage("Default NonForProvision.NonFortProv", getIdOf(getLife())); //SPR3157
					} //SPR3157
				}
			}
		}
	}

	/**
	 * Set coverage and coverage option statuses from the action indicators.
	 */
	//New Method SPR1105
	protected void process_P104() {
		if (verifyCtl(RIDER)) {
			Rider rider = getRider();
			String thisCovID = getIdOf(rider);
			logDebug("Performing NbaValInsurance.process_P104() for " + thisCovID);
			if (!rider.isActionDelete() && !rider.hasRiderStatus()) {
				rider.setRiderStatus(NbaOliConstants.OLI_POLSTAT_ACTIVE);
				rider.setActionUpdate();
			}
			for (int i = 0; i < rider.getCovOptionCount(); i++) {
				CovOption covOption = rider.getCovOptionAt(i);
				if (covOption.isAction(NbaActionIndicator.ACTION_DENY) && !covOption.hasCovOptionStatus()) {
					covOption.setCovOptionStatus(NbaOliConstants.OLI_POLSTAT_DECISSUE);
					covOption.setAction(null); //reset it because Update can not override Deny
					covOption.setActionUpdate();
				}
			}
		}
	}

	/**
	 * Set RatedInd (indicator) if there are ratings present on the contract.
	 */
	protected void process_P105() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P105() for ", getPolicy()); //NBA103
			boolean rated = false;
			//begin SPR2921
			Coverage cov = null;
			int size = getLife().getCoverageCount();
			for (int i = 0; i < size; i++) {
				cov = getLife().getCoverageAt(i);
				if (NbaUtils.isRated(cov) || NbaUtils.isChildBenefitRated(cov)) { //SPR3098
					rated = true;
					break;
				}
			}
			//end SPR2921
			getPolicy().setRatedInd(rated);
			getPolicy().setActionUpdate();
			getPolicyExtension().setRatedInd(rated);
			getPolicyExtension().setActionUpdate();
		}
	}

	/**
	 * Verify that a coverage option is allowed with associated coverage by locating a CovOptionProduct 
	 * within the CoverageProduct for the Coverage with the same ProductCode as the CovOption. 
	 */
	protected void process_P106() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P106() for ", getCovOption()); //NBA103
			if (getCovOptionProductFor(getCoverage(), getCovOption()) == null) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Coverage Option: ", getCovOption().getProductCode()),
						getIdOf(getCovOption()));
			}
		}
	}

	/**
	 * Verify that a coverage option is allowed with associated rider by locating a CovOptionProduct 
	 * within the CoverageProduct for the rider with the same ProductCode as the CovOption. 
	 */
	protected void process_P107() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P107() for ", getCovOption()); //NBA103
			if (getCovOptionProductFor(getRider(), getCovOption()) == null) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Coverage Option: ", getCovOption().getProductCode()),
						getIdOf(getCovOption()));
			}
		}
	}

	/**
	 * Validate IssueGender of the associated LifeParticipant against CovOptionProduct.UnderwritingClassProduct.IssueGender 
	 * to determine if coverage option can be issued for the indicated gender.
	 */
	protected void process_P108() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P108() for ", getCovOption()); //NBA103
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());//P2AXAL016
			if (covOptionProduct == null || covOptionProduct.getUnderwritingClassProductCount()<1)  return;//P2AXAL016
			
			boolean notAllowed = getUnderwritingClassProduct(getCoverage(), getCovOption()) == null; //SPR1996
			//SPR1996 code deleted
			if (notAllowed) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Coverage Option: ", getCovOption().getProductCode()),
						getIdOf(getCovOption()));
			}
		}
	}

	/**
	 * Validate IssueGender of the associated Participant against CovOptionProduct.UnderwritingClassProduct.IssueGender 
	 * to determine if coverage option can be issued for the indicated gender.
	 */
	protected void process_P109() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P109() for ", getCovOption()); //NBA103
			boolean notAllowed = getUnderwritingClassProduct(getRider(), getCovOption()) == null; //SPR1996
			//SPR1996 code deleted
			if (notAllowed) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Coverage Option: ", getCovOption().getProductCode()),
						getIdOf(getCovOption()));
			}
		}
	}

	/**
	 * Validate LifeParticipant IssueAge against maximum age allowed for 
	 * coverage option based on plan business rules.
	 */
	protected void process_P110() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P110() for ", getCovOption()); //NBA103
			//SPR1684 code deleted
			LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(getCoverage());
			if (lifeParticipant != null) {
				//P2AXAL055 Code Restructuring
				int maxAge = getMaxIssueAge(getCoverage(), covOption);
				//P2AXAL055 Code Restructuring
				int issueAge = lifeParticipant.getIssueAge();
				if (Integer.MAX_VALUE != maxAge && issueAge > maxAge) { //P2AXAL055
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", issueAge, ", Maximum age: ", maxAge),
							getIdOf(getCovOption()));
					//end SPR1684
				}
			}
			//SPR1684 code deleted
		}
	}

	/**
	 * Validate rider Participant IssueAge against maximum age allowed for 
	 * coverage option based on plan business rules.
	 */
	protected void process_P112() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P112() for ", getCovOption()); //NBA103
			//SPR1684 code deleted
			Participant participant = NbaUtils.getInsurableParticipant(getRider());
			if (participant != null) {
				//begin SPR1684
				int maxAge = getMaxIssueAge(getRider(), getCovOption()); //SPR1996
				int issueAge = participant.getIssueAge();
				if (issueAge > maxAge) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", issueAge, ", Maximum age: ", maxAge),
							getIdOf(getCovOption()));
					//end SPR1684
				}
			}
			//SPR1684 code deleted
		}
	}

	/**
	 * Validate LifeParticipant IssueAge against minimum age allowed for 
	 * coverage option based on plan business rules.
	 */
	protected void process_P113() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P113() for ", getCovOption()); //NBA103
			//SPR1684 code deleted
			LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(getCoverage());
			if (lifeParticipant != null) {
				//begin SPR1684
				int minAge = getMinIssueAge(getCoverage(), getCovOption()); //SPR1996
				int issueAge = lifeParticipant.getIssueAge();
				if (issueAge < minAge) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", issueAge, ", Minimum age: ", minAge),
							getIdOf(getCovOption()));
					//end SPR1684
				}
			}
			//SPR1684 code deleted
		}
	}

	/**
	 * Validate rider Participant IssueAge against Minimum age allowed for 
	 * coverage option based on plan business rules.
	 */
	protected void process_P115() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P115() for ", getCovOption()); //NBA103
			//SPR1684 code deleted
			Participant participant = NbaUtils.getInsurableParticipant(getRider());
			if (participant != null) {
				//begin SPR1684				
				int minAge = getMinIssueAge(getRider(), getCovOption()); //SPR1996
				int issueAge = participant.getIssueAge();
				if (issueAge < minAge) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue age: ", getParticipant().getIssueAge(), ", Minimum age: ",
							minAge), getIdOf(getParticipant()));
					//end SPR1684
				}
			}
			//SPR1684 code deleted
		}
	}

	/**
	 * Set OptionAmt (coverage option amount) based on plan business rules if amount not specified. 
	 * Validate coverage option units against plan limits.
	 */
	protected void process_P116() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P116() for ", getCovOption()); //NBA103
			//SPR1996 code deleted
			UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProduct(getCoverage(), getCovOption()); //SPR1996
			if (underwritingClassProduct != null) {
				editCovOptionValues(underwritingClassProduct, getCovOption(), getCoverage().getCurrentAmt(), getCoverage().getCurrentNumberOfUnits(),
						getCoverage().getValuePerUnit());
			}
			//SPR1996 code deleted
		}
	}

	/**
	 * Set OptionAmt (coverage option amount) based on plan business rules if amount not specified.
	 * Validate coverage option units against plan limits.
	 */
	protected void process_P117() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P117() for ", getCovOption()); //NBA103
			Participant participant = NbaUtils.getInsurableParticipant(getRider());
			if (participant != null) {
				UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProduct(getRider(), getCovOption()); //SPR1996
				// begin NBA104
				double valuePerUnit = 1000;
				CoverageProduct coverageProduct = getCoverageProductFor(getRider());
				if (coverageProduct == null || !coverageProduct.hasValuePerUnit()) {
					valuePerUnit = coverageProduct.getValuePerUnit();
				}
				// end NBA104
				if (underwritingClassProduct != null) {
					editCovOptionValues(underwritingClassProduct, getCovOption(), getRider().getTotAmt(), getRider().getNumberOfUnits(), valuePerUnit); // NBA104
				}
			}
		}
	}

	/**
	 * Set the CovOptionPctInd (coverage option percentage indicator) based on whether the 
	 * OptionAmt field contains a ratio percentage as defined by plan business rules.
	 */
	protected void process_P118() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P118() for ", getCovOption()); //NBA103
			boolean covOptionPctInd = false;
			//begin SPR1946
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
			if (covOptionProduct != null) {
				CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(covOptionProduct);
				if (covOptionProductExtension != null
						&& (covOptionProductExtension.getRateBasedOn() == OLI_RATEBASEDON_INITDTHBENPU || covOptionProductExtension.getRateBasedOn() == OLI_RATEBASEDON_ULTMATVPU)) {
					covOptionPctInd = true;
				}
			}
			//end SPR1946
			getCovOption().setCovOptionPctInd(covOptionPctInd);
			getCovOption().setActionUpdate();
		}
	}

	/**
	 * Set the CovOptionPctInd (coverage option percentage indicator) based on whether the 
	 * OptionAmt field contains a ratio percentage as defined by plan business rules.
	 */
	protected void process_P909() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P909() for ", getCovOption()); //SPR1946 //NBA103
			boolean covOptionPctInd = false;
			//begin SPR1946
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getRider(), getCovOption());
			if (covOptionProduct != null) {
				CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(covOptionProduct);
				if (covOptionProductExtension != null
						&& (covOptionProductExtension.getRateBasedOn() == OLI_RATEBASEDON_INITDTHBENPU || covOptionProductExtension.getRateBasedOn() == OLI_RATEBASEDON_ULTMATVPU)) {
					covOptionPctInd = true;
				}
			}
			//end SPR1946
			getCovOption().setCovOptionPctInd(covOptionPctInd);
			getCovOption().setActionUpdate();
		}
	}

	/**
	 * Verify coverage option EffDate equal to or later than associated coverage EffDate.
	 */
	protected void process_P119() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P119() for ", getCovOption()); //NBA103
			if (NbaUtils.compare(getCovOption().getEffDate(), getCoverage().getEffDate()) < 0) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCovOption()));
			}
		}
	}

	/**
	 * Verify coverage option EffDate equal to or later than associated rider EffDate.
	 */
	protected void process_P120() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P120() for ", getCovOption()); //NBA103
			if (NbaUtils.compare(getCovOption().getEffDate(), getRider().getEffDate()) < 0) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCovOption()));
			}
		}
	}

	/**
	 * Calculated the pay-up date for a coverage option based on plan business rules.  
	 * Verify pay-up date for coverage option is less than or equal to that of the associated coverage.  
	 * If not, set coverage option pay-up date equal to that of associated coverage.
	 */
	protected void process_P121() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P121() for ", getCovOption()); //NBA103
			//begin SPR1956
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
			LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(getCoverage());
			CoverageExtension coverageExtension = NbaUtils.getFirstCoverageExtension(getCoverage());
			updateCovOptionPayUpDate(getCovOption(), covOptionProduct, lifeParticipant.getIssueAge(), coverageExtension.getPayUpDate(), getCoverage()
					.getTermDate());
			//end SPR1956
		}
	}

	/**
	 * Calculated the pay-up date for a coverage option based on plan business rules.  
	 * Verify pay-up date for coverage option is less than or equal to that of the associated rider  
	 * If not, set coverage option pay-up date equal to that of associated rider.
	 */
	protected void process_P122() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P121() for ", getCovOption()); //NBA103
			//begin SPR1956
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getRider(), getCovOption());
			Participant participant = NbaUtils.getInsurableParticipant(getRider());
			AnnuityRiderExtension annuityRiderExtension = NbaUtils.getFirstAnnuityRiderExtension(getRider());
			updateCovOptionPayUpDate(getCovOption(), covOptionProduct, participant.getIssueAge(), annuityRiderExtension.getPayUpDate(), getRider()
					.getTermDate());
			//end SPR1956
		}
	}

	/**
	 * Calculate the CovOption.TermDate (mature/expiry date) for coverage option based on CovOptionProductExtension.MaturityCalcMethod.
	 * If TermDate is based on attained age (tc= "1000500002","1000500004" or "1000500006"), then calculate the years difference 
	 * between LifeParticipant.IssueAge and CovOptionProductExtension.MaturityAge.  Add years difference to CovOption.EffDate 
	 * to get TermDate result. 
	 * If TermDate is based on duration (tc= "1000500001","1000500003" or "1000500005")" add CovOptionProductExtension.MaturityDuration 
	 * (number of years) to CovOption.EffDate to get TermDate result.
	 * Then, depending on the MaturityCalcMethod, verify that CovOption.TermDate does not exceed the for pay-up or cease date of the 
	 * associuated coverage. If it does, set it equal to the appropriate date.
	 */
	protected void process_P123() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P123() for ", getCovOption()); //NBA103
			//begin SPR1956
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
			LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(getCoverage());
			CoverageExtension coverageExtension = NbaUtils.getFirstCoverageExtension(getCoverage());
			updateCovOptionTermDate(getCovOption(), covOptionProduct, lifeParticipant.getIssueAge(), coverageExtension.getPayUpDate(), getCoverage()
					.getTermDate());
			//end SPR1956
		}
	}

	/**
	 * Calculate the CovOption.TermDate (mature/expiry date) for coverage option based on CovOptionProductExtension.MaturityCalcMethod.
	 * If TermDate is based on attained age (tc= "1000500002","1000500004" or "1000500006"), then calculate the years difference 
	 * between Participant.IssueAge and CovOptionProductExtension.MaturityAge.  Add years difference to CovOption.EffDate 
	 * to get TermDate result. 
	 * If TermDate is based on duration (tc= "1000500001","1000500003" or "1000500005")" add CovOptionProductExtension.MaturityDuration 
	 * (number of years) to CovOption.EffDate to get TermDate result.
	 * Then, depending on the MaturityCalcMethod, verify that CovOption.TermDate does not exceed the for pay-up or cease date of the 
	 * associuated rider. If it does, set it equal to the appropriate date.
	 */
	protected void process_P124() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P124() for ", getCovOption()); //NBA103
			//begin SPR1956
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getRider(), getCovOption());
			Participant participant = NbaUtils.getInsurableParticipant(getRider());
			AnnuityRiderExtension annuityRiderExtension = NbaUtils.getFirstAnnuityRiderExtension(getRider());
			updateCovOptionTermDate(getCovOption(), covOptionProduct, participant.getIssueAge(), annuityRiderExtension.getPayUpDate(), getRider()
					.getTermDate());
			//end SPR1956
		}
	}

	/**
	 * Validate IncCalculationMethod (Increasing Death Benefit calculation method) value against table 
	 * OLIEXT_LU_CALCMETHOD for IDB coverage option OLI_LU_OPTTYPE tc="1").
	 */
	protected void process_P125() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P125() for ", getCovOption()); //NBA103
			if (getCovOption().getLifeCovOptTypeCode() == OLI_OPTTYPE_ABE) {
				long method = getCovOptionExtension().getIncCalculationMethod();
				if (!isValidTableValue(NbaTableConstants.OLIEXT_LU_CALCMETHOD, method)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Increasing Death Benefit calculation method: ", method),
							getIdOf(getCovOption()));
				}
			}
		}
	}

	/**
	 * Validate associated coverage table rating does not exceed plan limits for coverage option.
	 */
	protected void process_P126() {
		if (verifyCtl(COVOPTION)) {
			if (getCovOption().hasPermTableRating() || getCovOption().hasTempTableRating()) {
				logDebug("Performing NbaValInsurance.process_P126() for ", getCovOption()); //NBA103
				long tableRating = -1;
				if (getCovOption().hasPermTableRating()) {
					tableRating = getCovOption().getPermTableRating();
				} else if (getCovOption().hasTempTableRating()) {
					tableRating = getCovOption().getTempTableRating();
				}
				CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
				if (covOptionProduct == null || !covOptionProduct.hasMaxTableRating()) {
					addPlanInfoMissingMessage("CovOptionProduct.MaxTableRating", getIdOf(getSubstandardRating()));
				} else {
					long maxRating = covOptionProduct.getMaxTableRating();
					if (tableRating > maxRating) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Table rating: ", tableRating, ", Maximum table rating: ",
								maxRating), getIdOf(getCovOption()));
					}
				}
			}
		}
	}

	/**
	 * Validate associated rider table rating does not exceed plan limits for coverage option.
	 */
	protected void process_P127() {
		if (verifyCtl(COVOPTION)) {
			if (getCovOption().hasPermTableRating() || getCovOption().hasTempTableRating()) {
				logDebug("Performing NbaValInsurance.process_P127() for ", getCovOption()); //NBA103
				long tableRating = -1;
				if (getCovOption().hasPermTableRating()) {
					tableRating = getCovOption().getPermTableRating();
				} else if (getCovOption().hasTempTableRating()) {
					tableRating = getCovOption().getTempTableRating();
				}
				CovOptionProduct covOptionProduct = getCovOptionProductFor(getRider(), getCovOption());
				if (covOptionProduct == null || !covOptionProduct.hasMaxTableRating()) {
					addPlanInfoMissingMessage("CovOptionProduct.MaxTableRating", getIdOf(getSubstandardRating()));
				} else {
					long maxRating = covOptionProduct.getMaxTableRating();
					if (tableRating > maxRating) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Table rating: ", tableRating, ", Maximum table rating: ",
								maxRating), getIdOf(getCovOption()));
					}
				}
			}
		}
	}

	/**
	 * Validate to determine whether the coverage option can be issued when the related coverage is rated substandard.  
	 * Based upon plan business rules.
	 */
	protected void process_P128() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P128() for ", getCovOption());//NBA103
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
			if (covOptionProduct == null) {
				addPlanInfoMissingMessage("CovOptionProduct", getIdOf(getCovOption()));
			} else {
				CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(covOptionProduct);
				if (covOptionProductExtension == null) {
					addPlanInfoMissingMessage("CovOptionProductExtension", getIdOf(getCovOption()));
				} else {
					boolean invalid = false;
					if (covOptionProductExtension.getMaxTableRestrictType() == 1000500002) {
						// Do not issue if table rating on related coverage exceeds max.
						LifeParticipant lifeParticipant = NbaUtils.findPrimaryInsuredLifeParticipant(getCoverage());
						if (lifeParticipant != null && lifeParticipant.getSubstandardRatingCount() > 0) {
							long max = covOptionProduct.getMaxTableOnParent();
							for (int i = 0; i < lifeParticipant.getSubstandardRatingCount(); i++) {
								SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(i);
								if (NbaUtils.isValidRating(substandardRating)
										&& (substandardRating.getPermTableRating() > max || substandardRating.getTempTableRating() > max)) { //SPR3098
									invalid = true;
									break;
								}
							}
						}
					} else if (covOptionProductExtension.getMaxTableRestrictType() == 1000500003) {
						// Do not issue if the related coverage is issued substandard 
						LifeParticipant lifeParticipant = NbaUtils.findPrimaryInsuredLifeParticipant(getCoverage());
						if (NbaUtils.isRated(lifeParticipant)) { //SPR3098
							invalid = true;
						}
					}
					if (invalid) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCovOption()));
					}
				}
			}
		}
	}

	/**
	 * Validate coverage option PermPercentageLoading (permanent rating factor) does not exceed the maximum 
	 * rating factor allowed based upon plan business rules.  
	 */
	protected void process_P130() {
		if (verifyCtl(COVOPTION)) {
			if (getCovOption().hasPermPercentageLoading()) {
				logDebug("Performing NbaValInsurance.process_P130() for ", getCovOption()); //NBA103
				CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
				if (covOptionProduct == null) {
					addPlanInfoMissingMessage("CovOptionProduct", getIdOf(getCovOption()));
				} else {
					CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(covOptionProduct);
					if (covOptionProductExtension == null) {
						addPlanInfoMissingMessage("CovOptionProductExtension", getIdOf(getCovOption()));
					} else {
						double max = covOptionProductExtension.getMaxFlatExtraPct();
						double factor = getCovOption().getPermPercentageLoading();
						if (factor > max) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Coverage Option: ", factor,
									" , Maximum permanent rating factor allowed: ", max), getIdOf(getCovOption()));
						}
					}
				}
			}
		}
	}

	/**
	 * Validate coverage option PermPercentageLoading (permanent rating factor) does not exceed the maximum 
	 * rating factor allowed based upon plan business rules.  
	 */
	protected void process_P131() {
		if (verifyCtl(COVOPTION)) {
			if (getCovOption().hasPermPercentageLoading()) {
				logDebug("Performing NbaValInsurance.process_P131() for ", getCovOption());//NBA103
				CovOptionProduct covOptionProduct = getCovOptionProductFor(getRider(), getCovOption());
				if (covOptionProduct == null) {
					addPlanInfoMissingMessage("CovOptionProduct", getIdOf(getCovOption()));
				} else {
					CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(covOptionProduct);
					if (covOptionProductExtension == null) {
						addPlanInfoMissingMessage("CovOptionProductExtension", getIdOf(getCovOption()));
					} else {
						double max = covOptionProductExtension.getMaxFlatExtraPct();
						double factor = getCovOption().getPermPercentageLoading();
						if (factor > max) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Coverage Option: ", factor,
									" , Maximum permanent rating factor allowed: ", max), getIdOf(getCovOption()));
						}
					}
				}
			}
		}
	}

	/**
	 * Validate coverage option TempPercentageLoading (temporary rating factor) does not exceed the maximum 
	 * rating factor allowed based upon plan business rules.  
	 */
	protected void process_P132() {
		if (verifyCtl(COVOPTION)) {
			if (getCovOption().hasTempPercentageLoading()) {
				logDebug("Performing NbaValInsurance.process_P132() for ", getCovOption());//NBA103
				CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
				if (covOptionProduct == null) {
					addPlanInfoMissingMessage("CovOptionProduct", getIdOf(getCovOption()));
				} else {
					CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(covOptionProduct);
					if (covOptionProductExtension == null) {
						addPlanInfoMissingMessage("CovOptionProductExtension", getIdOf(getCovOption()));
					} else {
						double max = covOptionProductExtension.getMaxFlatExtraPct();
						double factor = getCovOption().getTempPercentageLoading();
						if (factor > max) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Coverage Option: ", factor,
									" , Maximum temporary rating factor allowed: ", max), getIdOf(getCovOption()));
						}
					}
				}
			}
		}
	}

	/**
	 * Validate that coverage option TempFlatEndDate does not exceed the associated coverage option pay up date.
	 */
	protected void process_P133() {
		if (verifyCtl(COVOPTION)) {
			if (getCovOption().hasTempFlatExtraAmt()) { //Temporary flat extra
				logDebug("Performing NbaValInsurance.process_P133() for ", getCovOption());//NBA103
				if (!getCovOption().hasTempFlatEndDate() // A temp flat must have an end date
						|| NbaUtils.compare(getCovOption().getTempFlatEndDate(), getCovOption().getTempFlatEndDate()) > 0) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCovOption()));
				}
			}
		}
	}

	/**
	 * Determine RatingCommissionRule (whether premium for substandard rating is to be commissionable) based on plan business rules.
	 */
	protected void process_P134() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P134() for ", getCovOption());//NBA103
			SubstandardRisk substandardRisk = getSubstandardRiskFor(getCoverage(), getCovOption(), getLifeParticipant().getSmokerStat(),
					getLifeParticipant().getIssueGender());
			if (substandardRisk == null || !substandardRisk.hasRatingCommissionRule()) {
				addPlanInfoMissingMessage("SubstandardRisk.RatingCommissionRule", getIdOf(getCovOption()));
			} else {
				getCovOption().setRatingCommissionRule(substandardRisk.getRatingCommissionRule());
				getCovOption().setActionUpdate();
			}
		}
	}

	/**
	 * Retrieve the coverage option ValuePerUnit amount based on plan business rules.
	 */
	protected void process_P137() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P137() for ", getCovOption());//NBA103
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
			if (covOptionProduct == null) {
				addPlanInfoMissingMessage("CovOptionProduct", getIdOf(getCovOption()));
			} else if (covOptionProduct.hasValuePerUnit()) {
				getCovOption().setValuePerUnit(covOptionProduct.getValuePerUnit());
				getCovOption().setActionUpdate();
			}
		}
	}

	/**
	 * Retrieve the coverage option ValuePerUnit amount based on plan business rules.
	 */
	protected void process_P138() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P138() for ", getCovOption());//NBA103
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getRider(), getCovOption());
			if (covOptionProduct == null) { //NBA104
				addPlanInfoMissingMessage("CovOptionProduct", getIdOf(getCovOption())); //NBA104
			} else if (covOptionProduct.hasValuePerUnit()) { //NBA104
				getCovOption().setValuePerUnit(covOptionProduct.getValuePerUnit());
				getCovOption().setActionUpdate();
			}
		}
	}

	/**
	 * Set FormNo (form number) to CovOptionProduct.FiledFormNumber if none present.
	 */
	protected void process_P141() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P141() for ", getCovOption());//NBA103
			if (!getCovOption().hasFormNo()) {
				CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
				if (covOptionProduct == null || !(covOptionProduct.hasFiledFormNumber() && covOptionProduct.getFiledFormNumber().trim().length() > 0)) { //SPR1945
					addPlanInfoMissingMessage("CovOptionProduct.FiledFormNumber", getIdOf(getCovOption()));
				} else {
					getCovOption().setFormNo(covOptionProduct.getFiledFormNumber());
					getCovOption().setActionUpdate();
				}
			}
		}
	}

	/**
	 * Set FormNo (form number) to CovOptionProduct.FiledFormNumber if none present.
	 */
	protected void process_P142() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P142() for ", getCovOption());//NBA103
			if (!getCovOption().hasFormNo()) {
				CovOptionProduct covOptionProduct = getCovOptionProductFor(getRider(), getCovOption());
				if (covOptionProduct == null || !(covOptionProduct.hasFiledFormNumber() && covOptionProduct.getFiledFormNumber().trim().length() > 0)) { //SPR1945
					addPlanInfoMissingMessage("CovOptionProduct.FiledFormNumber", getIdOf(getCovOption()));
				} else {
					getCovOption().setFormNo(covOptionProduct.getFiledFormNumber());
					getCovOption().setActionUpdate();
				}
			}
		}
	}

	/**
	 * Verify presence of one or more investment funds (ProductCode). Do not generate error if only one fund is allowed.
	 */
	protected void process_P145() {
		if (verifyCtl(HOLDING)) { //SPR3610
			logDebug("Performing NbaValInsurance.process_P145()");//NBA103
			if (getInvestment() == null || !(getInvestment().getSubAccountCount() > 0)) { //No <SubAccount> objects present  //SPR3610
				PolicyProduct policyProduct = getPolicyProductForPlan();
				if (policyProduct == null) {
					addPlanInfoMissingMessage("PolicyProduct", getIdOf(getPolicy()));
				} else {
					//begin SPR3610
					String productCode = getHolding().getPolicy().getProductCode();
					int funds = 0;
					InvestProductInfo investProductInfo;
					for (int i = 0; i < policyProduct.getInvestProductInfoCount(); i++) { //Ignore plan level 
						investProductInfo = policyProduct.getInvestProductInfoAt(i);
						if (!productCode.equals(investProductInfo.getProductCode())) {
							funds++;
						}
					}
					if (funds > 1) { //Generate error if not a plan which allows only one fund
						//end SPR3610
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getInvestment()));
					}
				}
			}
		}
	}

	/**
	 * Verify that a SystematicActivityType has been specified and is valid.
	 */
	protected void process_P146() {
		if (verifyCtl(SUBACCOUNT)) {
			logDebug("Performing NbaValInsurance.process_P146() for ", getSubAccount());//NBA103

			if (getSubAccount().hasSystematicActivityType()) { //SPR1917
				if (!isValidTableValue(NbaTableConstants.OLI_LU_SYSTEMATIC, getSubAccount().getSystematicActivityType())) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getSubAccount()));
				}
			}
		}
	} //SPR1917

	/**
	 * Verify that total fund AllocPercent equals 100 when the allocation type is a percentage for each systematic activity type.  
	 */
	protected void process_P147() {
		if (verifyCtl(INVESTMENT)) {
			logDebug("Performing NbaValInsurance.process_P147()");//NBA103
			double percent;
			if (getInvestment().getPolicyChargeAllocation() == OLI_LU_POLCHG_BYSPECIFIEDPCT) {
				percent = getTotalPercent(getInvestment(), OLI_SYSACTTYPE_CHARGES);
				if (!(percent == 0 || percent == 100)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Charge: ", percent), getIdOf(getInvestment()));
				}
			}
			percent = getTotalPercent(getInvestment(), OLI_SYSACTTYPE_WTHDRW);
			if (!(percent == 0 || percent == 100)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Withdrawal: ", percent), getIdOf(getInvestment()));
			}
			percent = getTotalPercent(getInvestment(), OLI_SYSACTTYPE_DOLLARCOSTAVG);
			if (!(percent == 0 || percent == 100)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Dollar Cost Average: ", percent), getIdOf(getInvestment()));
			}
			percent = getTotalPercent(getInvestment(), OLI_SYSACTTYPE_ASSETREALLOC);
			if (!(percent == 0 || percent == 100)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Asset Reallocation: ", percent), getIdOf(getInvestment()));
			}
		}
	}

	/**
	 * When PolicyProduct.MaxNumberInvestProducts = 1, verify that multiple funds have 
	 * not been requested for the same systematic activity type.
	 */
	protected void process_P148() {
		if (verifyCtl(INVESTMENT)) {
			logDebug("Performing NbaValInsurance.process_P148()");//NBA103
			PolicyProduct policyProduct = getPolicyProductForPlan();
			if (policyProduct == null || !policyProduct.hasMaxNumInvestProducts()) {
				addPlanInfoMissingMessage("PolicyProduct.MaxNumInvestProducts", getIdOf(getPolicy()));
			} else {
				if (policyProduct.getMaxNumInvestProducts() == 1) {
					editMaxFunds(policyProduct.getMaxNumInvestProducts());
				}
			}
		}
	}

	/**
	 * Verify that duplicate fund allocations (ProductCode) were not requested for the same systematic activty type.
	 */
	protected void process_P149() {
		if (verifyCtl(INVESTMENT)) {
			logDebug("Performing NbaValInsurance.process_P149()");//NBA103
			//begin SPR1646
			if (hasDuplicateSubAccount(getInvestment(), OLI_SYSACTTYPE_DEPT)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Deposit SubAccounts", getIdOf(getInvestment()));
			}
			//end SPR1646
			if (hasDuplicateSubAccount(getInvestment(), OLI_SYSACTTYPE_CHARGES)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Charge SubAccounts", getIdOf(getInvestment()));
			}
			if (hasDuplicateSubAccount(getInvestment(), OLI_SYSACTTYPE_WTHDRW)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Withdrawal SubAccounts", getIdOf(getInvestment()));
			}
			if (hasDuplicateSubAccount(getInvestment(), OLI_SYSACTTYPE_DOLLARCOSTAVG)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Dollar Cost Average SubAccounts", getIdOf(getInvestment()));
			}
			if (hasDuplicateSubAccount(getInvestment(), OLI_SYSACTTYPE_ASSETREALLOC)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Asset Reallocation SubAccounts", getIdOf(getInvestment()));
			}
		}
	}

	/**
	 * When PolicyProduct.MaxNumberInvestProducts > 1, verify that the number of fund allocations requested 
	 * for the same systematic activity type.are within plan limits based on plan business rules.
	 */
	protected void process_P150() {
		if (verifyCtl(INVESTMENT)) {
			logDebug("Performing NbaValInsurance.process_P150()");//NBA103
			PolicyProduct policyProduct = getPolicyProductForPlan();
			if (policyProduct == null || !policyProduct.hasMaxNumInvestProducts()) {
				addPlanInfoMissingMessage("PolicyProduct.MaxNumInvestProducts", getIdOf(getPolicy()));
			} else {
				if (policyProduct.getMaxNumInvestProducts() > 1) {
					editMaxFunds(policyProduct.getMaxNumInvestProducts());
				}
			}
		}
	}

	/**
	 * Verify that fund ProductCode is valid for plan according to plan business rules.
	 */
	protected void process_P151() {
		if (verifyCtl(SUBACCOUNT)) {
			logDebug("Performing NbaValInsurance.process_P151() for ", getSubAccount());//NBA103
			String productCode = getSubAccount().getProductCode();
			if (getInvestProductInfo(productCode) == null) { //ALII1990
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Investment Product Code: ", productCode), getIdOf(getSubAccount()));
			}
		}
	}

	/**
	 * Set AssetClass depending on whether fund type is fixed or variable 
	 */
	protected void process_P152() {
		if (verifyCtl(SUBACCOUNT)) {
			logDebug("Performing NbaValInsurance.process_P152() for ", getSubAccount());//NBA103
			InvestProductInfo investProductInfo = getInvestProductInfo(getSubAccount().getProductCode());
			if (investProductInfo != null) {
				InvestProductInfoExtension investProductInfoExtension = getInvestProductInfoExtensionFor(investProductInfo);
				if (investProductInfoExtension == null || !investProductInfoExtension.hasRateType()) {
					addPlanInfoMissingMessage("InvestProductInfoExtension.RateType", getIdOf(getSubAccount()));
				} else {
					getSubAccount().setAssetClass(investProductInfoExtension.getRateType());
					getSubAccount().setActionUpdate();
				}
			}
		}
	}

	/**
	 * The SubAccount Tax Qualification type must match the Annuity Tax Qualification type.
	 * The SubAccount Tax Qualification type is obtained from InvestProductInfoExtension.QualifiedCode.  
	 * The Annuity Tax Qualification type is determined from Annuity.QualPlanType.
	 */
	protected void process_P153() {
		if (verifyCtl(SUBACCOUNT)) {
			logDebug("Performing NbaValInsurance.process_P153() for ", getSubAccount());//NBA103
			InvestProductInfo investProductInfo = getInvestProductInfo(getSubAccount().getProductCode());
			if (investProductInfo != null) {
				InvestProductInfoExtension investProductInfoExtension = getInvestProductInfoExtensionFor(investProductInfo);
				if (investProductInfoExtension == null || !investProductInfoExtension.hasQualifiedCode()) {
					addPlanInfoMissingMessage("InvestProductInfoExtension.QualifiedCode", getIdOf(getSubAccount()));
				} else {
					//begin SPR2094
					long planQualCode = getAnnuity().getQualPlanType();
					long invstQualifiedCode = investProductInfoExtension.getQualifiedCode();
					boolean planIsQualified = planQualCode != OLI_QUALPLN_NONE;
					String error = "";
					if (planIsQualified && invstQualifiedCode == OLI_QUALIFIED_NONE) {
						error = " is not allowed on a Qualified Annuity";
					} else if (!planIsQualified && invstQualifiedCode == OLI_QUALIFIED_MUSTBE) {
						error = " is not allowed on a non-Qualified Annuity";
					}
					if (error.length() > 0) {
						//end SPR2094
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat(concat("Fund: ", getSubAccount().getProductCode()), error), //SPR2094
								getIdOf(getSubAccount()));
					}
				}
			}
		}
	}

	/**
	 * Verify AllocPercent greater than or equal to the minimum allowed based on plan business rules for Life products
	 * Please refer to process_P242 for Annuity products
	 * This process test if the allocation is a percentage, and test if the allocation (SubAccount) is a payment or charge. 
	 * If both conditions are true, and the MinPct value is greater than plan level value (SubAccount.AllocPercent) for payments, or (SubAccount.PolicyChargePct) for charges, add a new System Message.
	 */
	protected void process_P154() {
		if (verifyCtl(SUBACCOUNT)) {
			//begin SPR2986
			if (getLogger().isDebugEnabled()) {
				logDebug("Performing NbaValInsurance.process_P154() for ", getSubAccount());
			}
			//check if the allocation is a percentage and  the allocation is a payment or change
			long systematicActivityCode = getSubAccount().getSystematicActivityType();
			if ((-1L == systematicActivityCode || OLI_SYSACTTYPE_CHARGES == systematicActivityCode)
					&& OLI_TRANSAMTTYPE_PCT == getSubAccountExtension().getAllocType()) {
				FeatureProduct featureProd = null;
				FeatureOptProduct featureOptProd = null;
				int featureOptProdSize = -1;
				ArrayList allFeatureProd = getLifeProductExtensionForPlan().getFeatureProduct();
				int count = allFeatureProd.size();
				Validate: for (int i = 0; i < count; i++) {
					featureProd = (FeatureProduct) allFeatureProd.get(i);
					featureOptProdSize = featureProd.getFeatureOptProductCount();
					if (OLI_ARRTYPE_STANDINGALLOC == featureProd.getArrType() && featureOptProdSize > 0) {
						for (int j = 0; j < featureOptProdSize; j++) {
							featureOptProd = featureProd.getFeatureOptProductAt(j);
							if (featureOptProd.hasMinPct()) {
								validateMinPct(featureOptProd.getMinPct(), systematicActivityCode);
								break Validate;
							}
						}
					}
				}
			}
			//End SPR2986
		}
	}

	/**
	 * Determine InvestmentType and InvestmentSubtype method for fixed fund.
	 */
	protected void process_P157() {
		if (verifyCtl(SUBACCOUNT)) {
			logDebug("Performing NbaValInsurance.process_P157() for ", getSubAccount());//NBA103
			//		getSubAccount().setInvestmentType();
			//		getSubAccount().setInvestmentSubtype();
			getSubAccount().setActionUpdate();
		}
	}

	/**
	 * Determine key for lookup of current interest rate, initial current interest rate to be used, 
	 * and calculate end date of the initial current interest guarantee period.
	 */
	protected void process_P158() {
		if (verifyCtl(SUBACCOUNT)) {
			logDebug("Performing NbaValInsurance.process_P158() for ", getSubAccount());
			double rate = 5;
			getSubAccount().setCurrRate(rate);
			//		getSubAccount().setInitialRate();
			//		getSubAccount().setInitialEndDate();
			getSubAccount().setActionUpdate();
		}
	}

	/**
	 * Retrieve target premiums based on plan business rules.
	 */
	protected void process_P160() {
		//begin NBA104
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P160 for ", getLife());
			if (hasPremLoadRules()) {
				updatePremLoadTarget(getLife());
			}
		}
		//end NBA104
	}

	/**
	 * Retrieve premium load targets based on plan business rules. This is in PolicyProduct.LifeProduct.CoverageProduct.PremiumRate.PremiumRateType
	 * Loop through all of the <PremiumRate>If any of the occurrences of PremiumRateType contains value of "11", plan has premium load targets and
	 * should be retrieved.
	 */
	//NBA117 New Method
	protected void process_P252() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P252 for ", getLife());
			if (hasPremLoadTarget()) {
				LifeExtension lifeExtn = NbaUtils.getFirstLifeExtension(getLife());
				lifeExtn.setPremLoadTargetAmt(null);
				lifeExtn.setActionUpdate();
				updateVNTGPremLoadTarget();
			}
		}
	}

	/**
	 * Set LifeUSA.DefLifeInsMethod (whether policy is under TEFRA/DEFRA guideline premium regulation) from LifeProductExtension.DefLifeInsMethod.
	 */
	protected void process_P161() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P161()");//NBA103
			LifeProductExtension lifeProductExtension = getLifeProductExtensionForPlan();
			if (lifeProductExtension == null || !lifeProductExtension.hasDefLifeInsMethod()) {
				addPlanInfoMissingMessage("LifeProductExtension.DefLifeInsMethod", getIdOf(getLife()));
			} else {
				getLifeUSA().setDefLifeInsMethod(lifeProductExtension.getDefLifeInsMethod());
				getLifeUSA().setActionUpdate();
			}
		}
	}

	/**
	 * Calculate the GuidelineSinglePrem (GSP) and GuidelineAnnPrem (GLP) using the system calculation method
	 * or the rate file method depending on plan rules.
	 */
	protected void process_P163() {
		// begin NBA104
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P163()");//NBA103
			if (isValidContractForGuidelines() && hasGuidelinePremiumRule()) {
				updateGuidelinePremiums(getLifeUSA());
			}
		}
		// end NBA104
	}

	/**
	 * If GuidelinePremCalcRule tc="1000500001" (calculated using the system calculation method) OR if If GuidelinePremCalcRule tc="1000500002" 
	 * (calculated using the rate file method), retrieve  GuidelineSinglePrem (GSP)  and GuidelineAnnPrem (GLP) from backend system.
	 */
	//NBA133 New Method
	protected void process_P239() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P239()");
			if (isValidContractForGuidelines() && hasGuidelinePremiumRule()) {
				LifeUSA lifeUSA = getLifeUSA();
				lifeUSA.setGuidelineAnnPrem(null);
				lifeUSA.setGuidelineSinglePrem(null);
				lifeUSA.setActionUpdate();
				updateBESGuidelinePremiums(lifeUSA);
			}
		}
	}

	/**
	 * Retrieve GuidelineSinglePrem (GSP) and GuidelineAnnPrem (GLP) from backend system. nbA assumes all flexible premium advanced life products -
	 * not specific plan rules
	 */
	//NBA117 New Method
	protected void process_P249() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P249()");
			if (isValidContractForGuidelines()) {
				LifeUSA lifeUSA = getLifeUSA();
				lifeUSA.setGuidelineAnnPrem(null);
				lifeUSA.setGuidelineSinglePrem(null);
				lifeUSA.setActionUpdate();
				updateBESGuidelinePremiums(lifeUSA);
			}
		}
	}

	/**
	 * Verify InitialPremAmt (planned initial Premium) does not exceed GuidelineSinglePrem (guideline single premium) amount.
	 */
	protected void process_P164() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P164()");//NBA103
			double max = getLifeUSA().getGuidelineSinglePrem();
			double prem = getLife().getInitialPremAmt();
			if (prem > max) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Planned initial premium: ", prem, ", Guideline Single premium: ",
						max), getIdOf(getCoverage()));
			}
		}
	}

	/**
	 * If guideline premium > 0, verify total first year premium amounts do not exceed guideline premium amount for both CyberLife and Vantage backend
	 * system and nbA calculated plans
	 */
	protected void process_P165() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P165()");//NBA103
			//begin NBA117
			double guideLineSinglePrem = getLifeUSA().getGuidelineSinglePrem(); //APSL3458
			if (guideLineSinglePrem > 0) {
				double totalFirstYearPremAmt = NbaUtils.getAnnualizedPaymentAmt(getPolicy());
				if (totalFirstYearPremAmt > guideLineSinglePrem) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Total First year premium amount: ", totalFirstYearPremAmt,
							", Guideline premium amount: ", guideLineSinglePrem), getIdOf(getLife()));
				}
			}
			//end NBA117
		}
	}

	/**
	 * Verify renewal premium does not exceed guideline premium amount.
	 */
	protected void process_P166() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P166()");//NBA103
			//begin NBA117
			double guideLineSinglePrem = getLifeUSA().getGuidelineSinglePrem(); //APSL3458
			if (guideLineSinglePrem > 0) {
				double renewalPremium = NbaUtils.getAnnualizedPaymentAmt(getPolicy());
				if (renewalPremium > guideLineSinglePrem) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Renewal premium amount: ", renewalPremium,
							", Guideline premium amount: ", guideLineSinglePrem), getIdOf(getLife()));
				}
			}
			//end NBA117
		}
	}

	/**
	 * Determine Minimum no lapse premium based on plan business rules.
	 */
	protected void process_P917() {
		//begin NBA104
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P917 for ", getLife());
			if (hasMinNoLapseRules()) {
				updateMinNoLapseTarget(getLife());
			}
		}
		// end NBA104
	}

	/**
	 * Retrieve MinPremAmt and TargetEndDate for Minimum No Lapse Premium (MAP) from backend system.
	 */
	//NBA133 New Method
	protected void process_P240() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P240 for ", getLife());
			if (hasMinNoLapseRules()) {
				getLife().setMinPremAmt(null);
				getLife().setActionUpdate();
				getLifeExtension().setMapTargetEndDate((Date) null);
				getLifeExtension().setActionUpdate();
				updateBESMinNoLapsePrem(getLife());
			}
		}
	}

	/**
	 * Retrieve MinPremAmt (MAP) from backend system if plan rules define applicable: This is in
	 * PolicyProduct.LifeProduct.CoverageProduct.PremiumRate.PremiumRateType Loop through all of the <PremiumRate>. If any of the occurrences of
	 * PremiumRateType contains value of "7", plan has MAP and should be retrieved.
	 */
	//NBA117 New Method	
	protected void process_P250() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P250 for ", getLife());
			if (hasMinPremAmt()) {
				getLife().setMinPremAmt(null);
				getLife().setActionUpdate();
				updateBESMinNoLapsePrem(getLife());
			}
		}
	}

	/**
	 * Verify total of InitialPremAmt plus annualized planned PaymentAmt (periodic premium) 
	 * is greater than or equal to MinPremAmt (minimum annual premium).
	 */
	protected void process_P167() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P167()");//NBA103
			double projected = getPolicy().getAnnualPaymentAmt() + getLife().getInitialPremAmt();
			double min = getLife().getMinPremAmt();
			if (min > projected) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Projected: ", projected, ", Minimum: ", min), getIdOf(getLife()));
			}
		}
	}

	//SPR2099 code deleted
	/**
	 * If SevenPayPrem is system calculated, use VPMS Model to calculate SevenPayPrem
	 */
	protected void process_P169() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P169()");//NBA103
			if (isValidContractForGuidelines() && has7PayPremiumRule()) { //NBA104
				update7PayPremiums(getLifeUSA()); //NBA104
			} //NBA104
		}
	}

	/**
	 * Retrieve life advanced or traditional 7 Pay Premium from backend system if applicable per the plan rules. If
	 * LifeProductExtension.MECIssueType=1, subject to seven pay and info will be obtained from the backend system to set the following field.
	 * Holding.Policy.Life.LifeUSA.SevenPayPrem
	 */
	//NBA117 New Method
	protected void process_P253() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P253()", getLife());
			if (has7PayPremium()) {
				getLifeUSA().setSevenPayPrem(null);
				getLifeUSA().setActionUpdate();
				updateVNTG7PayPremiums();
			}
		}
	}

	/**
	 * Verify that InitialPremAmt (initial premium) does not exceed InitSevenPayPrem (7-pay premium limit). 
	 */
	protected void process_P170() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P170()");//NBA103
			double initial = getLife().getInitialPremAmt();
			double sevenPay = getLifeUSA().getInitSevenPayPrem();
			if (initial > sevenPay) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Initial premium amount: ", initial, ", 7-pay premium amount: ",
						sevenPay), getIdOf(getLife()));
			}
		}
	}

	/**
	 * If 7-Pay premium > 0 for new app except trial app, verify first year premium within 7-pay premium limits for both CyberLife and Vantage backend
	 * system and nbA calculated plans. Applicable to flexible premium advanced life products and traditional fixed premium life products.
	 */
	protected void process_P171() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P171()");//NBA103
			//begin NBA117
			double sevenPayPrem = getLifeUSA().getSevenPayPrem();
			if (sevenPayPrem > 0) {			    
				double firstYearPremAmt = getPolicy().getApplicationInfo().getCWAAmt(); //APSL3695
				if (firstYearPremAmt > sevenPayPrem) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Total First year premium amount: ", firstYearPremAmt,
							", SevenPay premium amount: ", sevenPayPrem), getIdOf(getLife()));
				}
			}
			//end NBA117
		}
	}

	/**
	 * If 7-Pay premium  > 0 for new app except trial app, verify renewal premium within 7-pay premium limits
	 */
	protected void process_P172() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P172()");//NBA103
			//begin NBA117
			double sevenPayPrem = getLifeUSA().getSevenPayPrem();
			if (sevenPayPrem > 0) {
				double renewalPremAmt = getPolicy().getApplicationInfo().getCWAAmt(); //APSL3695
				if (renewalPremAmt > sevenPayPrem) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Renewal premium amount: ", renewalPremAmt,
							", SevenPay premium amount: ", sevenPayPrem), getIdOf(getLife()));
				}
			}
			//end NBA117
		}
	}

	/**
	 * Set CommissionPlanCode for coverage from PolicyProduct.PolicyProductInfo.DefaultCommCode.
	 */
	protected void process_P173() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P173() for ", getCoverage());//NBA103
			PolicyProduct policyProduct = getPolicyProductFor(getCoverage());
			if (policyProduct == null || policyProduct.getPolicyProductInfoCount() == 0) {
				addPlanInfoMissingMessage("PolicyProduct.PolicyProductInfo", getIdOf(getCoverage()));
			} else {
				getCoverageExtension().setCommissionPlanCode(policyProduct.getPolicyProductInfoAt(0).getDefaultCommCode());
				getCoverageExtension().setActionUpdate();
			}
		}
	}

	/**
	 * Set CommissionPlanCode for Annuity from PolicyProduct.PolicyProductInfo.DefaultCommCode.
	 */
	protected void process_P174() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P174()");//NBA103
			PolicyProduct policyProduct = getPolicyProductForPlan();
			if (policyProduct == null || policyProduct.getPolicyProductInfoCount() == 0) {
				addPlanInfoMissingMessage("PolicyProduct.PolicyProductInfo", getIdOf(getAnnuity())); //SPR1728
			} else {
				getAnnuityExtension().setCommissionPlanCode(policyProduct.getPolicyProductInfoAt(0).getDefaultCommCode());
				getAnnuityExtension().setActionUpdate();
			}
		}
	}

	/**
	 * Set CommissionPlanCode for Rider from PolicyProduct.PolicyProductInfo.DefaultCommCode.
	 */
	protected void process_P175() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValInsurance.process_P175() for ", getRider());//NBA103
			PolicyProduct policyProduct = getPolicyProductFor(getRider());
			if (policyProduct == null || policyProduct.getPolicyProductInfoCount() == 0) {
				addPlanInfoMissingMessage("PolicyProduct.PolicyProductInfo", getIdOf(getRider()));
			} else {
				getAnnuityRiderExtension().setCommissionPlanCode(policyProduct.getPolicyProductInfoAt(0).getDefaultCommCode());
				getAnnuityRiderExtension().setActionUpdate();
			}
		}
	}

	/**
	 * Set the Current Interest Rate (Holding.Policy.Life.CurrIntRate) with the value returned from a VPMS Model.
	 */
	protected void process_P176() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P176()");//NBA103
			getLife().setCurrIntRate(getNewMoneyRateFromModel()); //NBA100
			getLife().setCurrIntRateDate(getCurrentDate());
			getLife().setActionUpdate();
		}
	}

	/**
	 * Set the Initial Deposit Interest Rate (Holding.Policy.Annuity.InitDepIntRateCurrent) with the value returned from a VPMS Model
	 */
	protected void process_P177() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P177()");//NBA103
			getAnnuity().setInitDepIntRateCurrent(getNewMoneyRateFromModel());
			getAnnuity().setInitDepositDate(getCurrentDate());
			getAnnuity().setActionUpdate();
		}
	}

	/**
	 * Set Coverage.GuarIntRate from PolicyProduct.GuarIntRate. For the Primary Coverage, 
	 * also update Life.ProjectedGuarIntRate.
	 */
	protected void process_P178() {
		if (verifyCtl(COVERAGE)) { //NBA100
			logDebug("Performing NbaValInsurance.process_P178() for ", getCoverage()); //NBA100//NBA103
			PolicyProduct policyProduct = getPolicyProductFor(getCoverage());
			if (policyProduct == null || !policyProduct.hasGuarIntRate()) {
				getCoverage().setGuarIntRate(0); // 0.00 % //SPR1706 	//NBA100
			} else {
				getCoverage().setGuarIntRate(policyProduct.getGuarIntRate()); //NBA100
				//SPR1706 code deleted
			}
			//begin NBA100
			getCoverage().setActionUpdate(); //SPR1706
			if (OLI_COVIND_BASE == getCoverage().getIndicatorCode()) {
				getLife().setProjectedGuarIntRate(getCoverage().getGuarIntRate());
				getLife().setActionUpdate();
				//end NBA100				
			}
		}
	}

	/**
	 * Set contract GuarIntRate (guaranteed interest rate) from PolicyProduct.GuarIntRate.
	 */
	protected void process_P179() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P179()");//NBA103
			PolicyProduct policyProduct = getPolicyProductForPlan();
			if (policyProduct == null || !policyProduct.hasGuarIntRate()) {
				getAnnuity().setGuarIntRate(0); // 0.00 %  //SPR1706
			} else {
				getAnnuity().setGuarIntRate(policyProduct.getGuarIntRate()); //SPR1706				
			}
			getAnnuity().setActionUpdate(); //SPR1706
		}
	}

	/**
	 * Determine surrender load targets
	 */
	protected void process_P191() {
		//begin NBA104
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P191 for ", getCoverage());
			if (hasSurrenderChargeRules()) {
				updateSurrenderChargeTarget(getCoverage());
			}
		}
		//end NBA104
	}

	/**
	 * Retrieve surrender targets based on plan business rules. This is in PolicyProduct.LifeProduct.CoverageProduct.PremiumRate.PremiumRateType Loop
	 * through all of the <PremiumRate>. If any of the occurrences of PremiumRateType contains value of "10", plan has surrender targets and should be
	 * retrieved.
	 */
	//NBA117 New Method
	protected void process_P251() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P251 for ", getLife());
			if (hasSurrenderTarget()) {
				LifeExtension lifeExtn = NbaUtils.getFirstLifeExtension(getLife());
				lifeExtn.setSurrTargetPrem(null);
				lifeExtn.setActionUpdate();
				updateVNTGSurrenderChargeTarget();
			}
		}
	}

	/**
	 * Verify that paid up additions are allowed(PolicyProductExtension.PUAAllowedInd) for the plan when Policy.PaidUpAdditionsOptionElected is true.
	 */
	protected void process_P197() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P197()");//NBA103
			if (getPolicyExtension().getPaidUpAdditionsOptionElected()) {
				PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
				if (policyProductExtension == null) {
					addPlanInfoMissingMessage("PolicyProductExtension", getIdOf(getPolicy()));
				} else if (!getPolicyProductExtensionForPlan().getPUAAllowedInd()) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPolicy()));
				}
			}
		}
	}

	/**
	 * Verify IssueAge less than high age limit for PUA based on PolicyProductExtension.PUAExcessInterestHighAge 
	 * when PaidUpAdditonsOptionElected.
	 */
	protected void process_P198() {
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValInsurance.process_P198()");//NBA103
			if (getPolicyExtension().getPaidUpAdditionsOptionElected()) {
				PolicyProductExtension policyProductExtension = getPolicyProductExtensionFor(getCoverage());
				if (policyProductExtension == null) {
					addPlanInfoMissingMessage("PolicyProductExtension", getIdOf(getCoverage()));
				} else {
					int age = getLifeParticipant().getIssueAge();
					int maxAge = policyProductExtension.getPUAExcessInterestHighAge();
					if (age > maxAge) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Issue Age: ", age, ", PUA high age: ", maxAge),
								getIdOf(lifeParticipant));
					}
				}
			}
		}
	}

	/**
	 * Verify substandard TableRating is within PolicyProductExtension.PUAMaxTableRating limits for PUA option
	 */
	protected void process_P199() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			if (getSubstandardRating().hasPermTableRating() || getSubstandardRating().hasTempTableRating()) {
				logDebug("Performing NbaValInsurance.process_P199()");//NBA103
				if (getPolicyExtension().getPaidUpAdditionsOptionElected()) {
					PolicyProductExtension policyProductExtension = getPolicyProductExtensionFor(getCoverage());
					if (policyProductExtension == null || !policyProductExtension.hasPUAMaxTableRating()) {
						addPlanInfoMissingMessage("PolicyProductExtension.PUAMaxTableRating", getIdOf(getCoverage()));
					} else {
						long tableRating = 999;
						if (getSubstandardRating().hasPermTableRating()) {
							tableRating = getSubstandardRating().getPermTableRating();
						} else if (getSubstandardRating().hasTempTableRating()) {
							tableRating = getSubstandardRating().getTempTableRating();
						}
						long maxRating = policyProductExtension.getPUAMaxTableRating();
						if (tableRating > maxRating) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Table rating: ", tableRating, ", Maximum table rating: ",
									maxRating), getIdOf(getSubstandardRating()));
						}
					}
				}
			}
		}
	}

	/**
	 * Verify PUA mortality record valid based on plan business rules.
	 */
	protected void process_P201() {
	}

	/**
	 * Verify extra rating valid for 7-pay premium calculation (CKUDT208)
	 */
	protected void process_P202() {
	}

	/**
	 * Retrieve plan business rules for future interest rate changes.
	 */
	protected void process_P206() {
	}

	/**
	 * Retrieve plan business rules for future premium per unit changes.
	 */
	protected void process_P207() {
	}

	/**
	 * Retrieve plan business rules for future death benefit value per unit changes.
	 
	 */
	protected void process_P208() {
	}

	/**
	 * Calculate Life.FaceAmt as the face amount of the primary coverage 
	 */
	//SPR1771 New Method
	protected void process_P211() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P211()");//NBA103
			getLife().setFaceAmt(getNbaTXLife().getFaceAmount());
			getLife().setActionUpdate();
		}
	}

	/**
	 * Edit Annuity.PremType against table OLI_LU_ANNPREM
	 */
	//SPR1705 New Method
	protected void process_P212() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P212()");//NBA103
			long premType = getAnnuity().getPremType();
			if (!isValidTableValue(NbaTableConstants.OLI_LU_ANNPREM, premType)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Premium Type: ", premType), getIdOf(getAnnuity()));
			}
		}
	}

	// NBA107 code deleted

	/**
	 * If the rateclass has not been overridden, use a VP/MS model to set Person.PersonExtension.RateClass    
	 */
	//SPR1778 New Method
	protected void process_P214() {
		if (verifyCtl(PERSON)) {
			logDebug("Performing NbaValInsurance.process_P214() for ", getPerson());//NBA103
			PersonExtension personExtension = getPersonExtension(); //SPR3572
			if (!(NbaConfiguration.getInstance().isAcNba() && personExtension.hasRateClass())) { //Bypass for AC if there already is a value
				// //SPR3572
				if (!personExtension.getRateClassOverrideInd()) { //Not overridden //SPR3572
					NbaOinkRequest nbaOinkRequest = new NbaOinkRequest();
					nbaOinkRequest.setPartyFilter(getOinkPartyFilter(getParty().getId()));
					NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(new HashMap(), nbaOinkRequest, true); //Get the rateclass //NBA100
					if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
						if (nbaVpmsResultsData.getResultsData().size() == 1) {
							String rateclass = (String) nbaVpmsResultsData.getResultsData().get(0);
							personExtension.setRateClass(rateclass); //SPR3572
							personExtension.setActionUpdate(); //SPR3572
						} else {
							addUnexpectedVpmsResultMessage(1, nbaVpmsResultsData.getResultsData().size());
						}
					}
				}
			}//SPR3572
		}
	}

	/**
	 * Set Coverage.IssuedAsAppliedInd from the IssueOthrApplied (ISOA) LOB field.
	 */
	//NBA100 New Method
	protected void process_P215() throws Exception {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValMisc.process_P215 for ", getCoverage());//NBA103
			getCoverage().setIssuedAsAppliedInd(!getNbaDst().getNbaLob().getIssueOthrApplied());//SPR2230
			getCoverage().setActionUpdate();
		}
	}

	/**
	 * Set CovOption.PlanName from CovOptionProduct.PlanName.
	 */
	//NBA100 New Method
	protected void process_P216() throws Exception {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValMisc.process_P216 for ", getCovOption());//NBA103
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getRider(), getCovOption());
			if (covOptionProduct != null) {
				getCovOption().setPlanName(covOptionProduct.getPlanName());
				getCovOption().setShortName(covOptionProduct.getPlanName());//ALII2013
				getCovOption().setActionUpdate();
			}
		}
	}

	/**
	 * If there is a primary Coverage and it has a value for FiledFormNumber, update Policy.FiledFormNumber with the value.
	 * Otherwise if Policy.FiledFormNumber does not have a value, set Policy.FiledFormNumber from the FiledFormNumber
	 * of the PolicyProduct associated with the primary coverage or annuity. If there is a primary Coverage, also 
	 * update it with the value.
	 */
	//NBA100 New Method
	protected void process_P217() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValInsurance.process_P217()");//NBA103
			Coverage primaryCov = getNbaTXLife().getPrimaryCoverage();
			if (primaryCov != null && (primaryCov.hasFormNo() && primaryCov.getFormNo().trim().length() > 0)) { //SPR1945 //SPR2133
				getPolicy().setFiledFormNumber(primaryCov.getFormNo()); //SPR2133
				if (!(getPolicy().hasFormNo() && getPolicy().getFormNo().trim().length() > 0)) {
					getPolicy().setFormNo(primaryCov.getFormNo());
				}
				getPolicy().setActionUpdate();
			} else if (!(getPolicy().hasFiledFormNumber() && getPolicy().getFiledFormNumber().trim().length() > 0)) { //SPR1945
				PolicyProduct policyProduct = getPolicyProductForPlan();
				if (policyProduct != null) {
					getPolicy().setFiledFormNumber(policyProduct.getFiledFormNumber());
					if (!(getPolicy().hasFormNo() && getPolicy().getFormNo().trim().length() > 0)) {
						getPolicy().setFormNo(getPolicy().getFiledFormNumber());
					}
					getPolicy().setActionUpdate();
					if (primaryCov != null) {
						primaryCov.setFiledFormNumber(getPolicy().getFiledFormNumber());
						primaryCov.setFormNo(getPolicy().getFormNo());
						primaryCov.setActionUpdate();
					}
				}
			}
		}
	}

	/**
	 * Verify Joint Equal Age.
	 * 
	 */
	//NBA104 New Method                             
	protected void process_P218() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValBilling.process_P218 for ", getCoverage());//NBA103
			updateJointEqualAge(getCoverage());
		}
	}

	/**
	 * Joint Equal Age for First to Die Coverage. If the coverage lives type indicates a first to die coverage the joint equal 
	 * age will be retrieved from the backend system.
	 * 
	 */
	//NBA133 New Method                             
	protected void process_P241() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValBilling.process_P241 for ", getCoverage());
			int partCount = getCoverage().getLifeParticipantCount();
			for (int i = 0; i < partCount; i++) {
				LifeParticipant lifepart = getCoverage().getLifeParticipantAt(i);
				lifepart.setIssueAge(null);
				lifepart.setActionUpdate();
			}
			updateBESJointEqualAge(getCoverage());
		}
	}

	/**
	 * Set primary coverage Holding.Policy.Life.Coverage.LivesType from plan definition file CoverageProduct.LivesType.
	 */
	//NBA111 New Method
	protected void process_P219() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P219() for ", getCoverage());//NBA103
			CoverageProduct coverageProduct = getCoverageProductFor(getCoverage());
			if (coverageProduct == null) {
				addPlanInfoMissingMessage("CoverageProduct", getIdOf(getCoverage()));
			} else {
				getCoverage().setLivesType(coverageProduct.getLivesType());
				getCoverage().setActionUpdate();
			}

		}
	}

	//P220 removed SPR2367

	/**
	 * Set Holding.Policy.Life.CovOption.LivesType from CovOptionProductExtension.LivesType
	 */
	//NBA111 New Method
	protected void process_P221() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P221() for ", getCovOption());//NBA103
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
			if (covOptionProduct != null) {
				CovOptionProductExtension covOptionProductExt = getCovOptionProductExtensionFor(covOptionProduct);
				if (covOptionProductExt != null && covOptionProductExt.hasLivesType()) {
					getCovOption().setLivesType(covOptionProductExt.getLivesType());
					getCovOption().setActionUpdate();
				} else {
					addPlanInfoMissingMessage("CovOptionProductExtension", getIdOf(getCovOption()));
				}
			} else
				addPlanInfoMissingMessage("CovOptionProduct", getIdOf(getCovOption()));
		}
	}

	/**
	 * Validates the  presence of life participant for single life coverage option
	 */
	//NBA111 New Method
	protected void process_P222() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P222() for ", getCovOption());//NBA103
			CovOption covOption = getCovOption();
			String lifeParticipantRefID = null;
			int lifePartiCount;
			LifeParticipant lifeParticipant = null;
			Coverage coverage = getCoverage();
			if (NbaUtils.isJointLife(coverage.getLivesType())) {
				if (covOption.getLivesType() <= OLI_COVLIVES_SINGLE) {
					lifeParticipantRefID = covOption.getLifeParticipantRefID();
					if (lifeParticipantRefID != null && lifeParticipantRefID.trim().length() > 0) {
						lifePartiCount = coverage.getLifeParticipantCount();
						if (lifePartiCount > 0) {
							for (int i = 0; i < lifePartiCount; i++) {
								lifeParticipant = coverage.getLifeParticipantAt(i);
								if (lifeParticipant.getId().equals(lifeParticipantRefID))
									return; //No error,exits successfully from the method
							}
						}

					}
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", covOption.getId());

				}
			}
		}
	}

	/**
	 * Verify presence of primary and joint LifeParticpant on joint life coverages for Ventage One
	 */
	//NBA111 New Method
	protected void process_P224() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P224() for ", getCoverage());//NBA103
			if (NbaUtils.isJointLife(getCoverage().getLivesType())) {
				boolean primaryFound = false;
				boolean jointFound = false;
				for (int i = 0; i < getCoverage().getLifeParticipantCount(); i++) {
					if (getCoverage().getLifeParticipantAt(i).getLifeParticipantRoleCode() == OLI_PARTICROLE_PRIMARY) {
						primaryFound = true;
					} else if (getCoverage().getLifeParticipantAt(i).getLifeParticipantRoleCode() == OLI_PARTICROLE_JOINT) {
						jointFound = true;
					}
				}
				if (!(primaryFound && jointFound)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
				}
			}
		}
	}

	/**
	 * Set CovOption.LifeCovOptTypeCode from CovOptionProductExtension.LifeCovOptTypeCode
	 */
	// SPR2093 New Method
	protected void process_P225() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P225() for ", getCovOption());
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getCoverage(), getCovOption());
			if (covOptionProduct != null) {
				CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(covOptionProduct);
				if (covOptionProductExtension != null) {
					getCovOption().setLifeCovOptTypeCode(covOptionProductExtension.getLifeCovOptTypeCode());
					getCovOption().setActionUpdate();
				}
			}
		}
	}

	/**
	 * Set CovOption.LifeCovOptTypeCode from CovOptionProductExtension.LifeCovOptTypeCode 
	 */
	// SPR2093 New Method
	protected void process_P226() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P226() for ", getCovOption());
			CovOptionProduct covOptionProduct = getCovOptionProductFor(getRider(), getCovOption());
			if (covOptionProduct != null) {
				CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(covOptionProduct);
				if (covOptionProductExtension != null) {
					getCovOption().setLifeCovOptTypeCode(covOptionProductExtension.getLifeCovOptTypeCode());
					getCovOption().setActionUpdate();
				}
			}
		}
	}

	/**
	 * Calculate the Refresh/Renewal Age for New Applications and For Renewable Plans for Holding.Policy.Life.Coverage.RenewalAge.
	 * The refresh/renewal age = the insured's age at the next renewal period (Issue Age + Renewal Period)
	 */
	// SPR2368 New Method
	protected void process_P228() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P228() for ", getCoverage());
			Coverage coverage = getCoverage();
			if (coverage.getRenewableInd()) {
				LifeParticipant lifeParticipant = NbaUtils.findInsuredLifeParticipant(coverage, false);
				if (lifeParticipant == null || !lifeParticipant.hasIssueAge()) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Invalid Issue Age", coverage.getId());
				} else if (!coverage.hasDurationDesign()) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Invalid Renewal Period", coverage.getId());
				} else {
					getCoverageExtension().setRenewalAge(lifeParticipant.getIssueAge() + coverage.getDurationDesign());
				}
			}
		}
	}

	/**
	 * Set CovOption.OptionPct from the utility calculation model.
	 */
	// NBA104 New Method
	protected void process_P230() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P230() for ", getCovOption());
			if (getCovOption().getCovOptionPctInd()) {
				updateOptionPct(getCovOption());
			}
		}
	}

	/**
	 * Set CovOption.OptionPct from the utility calculation model.
	 */
	// NBA104 New Method
	protected void process_P231() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValInsurance.process_P231() for ", getCovOption());
			if (getCovOption().getCovOptionPctInd()) {
				updateOptionPct(getCovOption());
			}
		}
	}

	/**
	 * Retrieve substandard PermPercentageLoading or TempPercentageLoading rate based on plan business rules.
	 */
	// NBA104 New Method
	protected void process_P227() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			logDebug("Performing NbaValInsurance.process_P227() for ", getSubstandardRating());
			if (isValidPercentageTableRating()) {
				updatePercentageLoading(getSubstandardRating());
			}
		}
	}

	/**
	 * Determine commission load targets for a new application
	 */
	//NBA104 New Method
	protected void process_P921() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P921 for ", getCoverage());//NBA103
			if (hasCommissionRules()) {
				updateCommissionTargetPrem(getCoverage());
			}
		}
	}

	/**
	 * Retrieve commission targets based on plan business rules - if plan has commission rules defined on plan file 
	 * that require targets (DULCRULE=A or C).  This is in PolicyProduct.PolicyProductInfoExtension. CommExtractCode  
	 * If PolicyProduct contains a value of  1 or 1000500003, the plan has commission targets and retrieved from backend system.
	 */
	//NBA133 New Method
	protected void process_P237() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P237 for ", getCoverage());
			if (hasCommissionRules()) {
				getCoverageExtension().setCommTargetPrem(null);
				getCoverageExtension().setActionUpdate();
				updateBESCommissionTargetPrem(getCoverage());
			}
		}
	}

	/**
	 * Retrieve commission targets based on plan business rules. This is in PolicyProduct.LifeProduct.CoverageProduct.PremiumRate.PremiumRateType Loop
	 * through all of the <PremiumRate>. If any of the occurrences of PremiumRateType contains value of "8", plan has commission load targets and
	 * should be retrieved.
	 */
	//NBA117 New Method
	protected void process_P248() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P248 for ", getCoverage());
			if (hasCommissionLoadTarget()) {
				LifeExtension lifeExtn = NbaUtils.getFirstLifeExtension(getLife());
				lifeExtn.setCommTargetPrem(null);
				lifeExtn.setActionUpdate();
				updateVNTGCommissionTargetPrem();
			}
		}
	}

	/**
	 * Determine commission load targets for a NON-new application
	 */
	//NBA104 New Method
	protected void process_P922() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P921 for ", getCoverage());//NBA103
			if (hasCommissionRules()) {
				updateCommissionTargetPrem(getCoverage());
			}
		}
	}

	/**
	 * Determine ParticipationType (whether the contract the contract is participating or nonparticipating).  
	 * Validate requested primary and/or secondary DivType are allowed based on plan business rules. VNTG  
	 */
	//NBA107
	protected void process_P232() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P088()"); //NBA103
			PolicyProduct policyProduct = getPolicyProductForPlan();
			if (policyProduct == null || !policyProduct.hasParticipatingInd()) {
				addPlanInfoMissingMessage("PolicyProduct.ParticipatingInd", getIdOf(getLife()));
			} else {
				boolean participatingInd = policyProduct.getParticipatingInd();
				if (participatingInd) {
					validateDivType();
				}
			}
		}
	}

	//NBA117 code deleted
	/**
	 * VNTG death benefit option type validation
	 */
	//SPR2654 New Method
	protected void process_P235() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValInsurance.process_P235() for ", getCoverage());
			if (!getCoverage().hasDeathBenefitOptType()) {
				if (getCoverage().getIndicatorCode() != OLI_COVIND_BASE) {
					for (int i = 0; i < getLife().getCoverageCount(); i++) { //Use value from Basic Coverage
						Coverage tempCov = getLife().getCoverageAt(i);
						if (tempCov.getIndicatorCode() == OLI_COVIND_BASE) {
							if (tempCov.hasDeathBenefitOptType()) {
								getCoverage().setDeathBenefitOptType(tempCov.getDeathBenefitOptType());
							}
							break;
						}
					}
				}

			}
			long deathBenefitOptType = getCoverage().getDeathBenefitOptType();
			if (!isValidTableValue(NbaTableConstants.OLI_LU_DTHBENETYPE, deathBenefitOptType)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Invalid death benefit option: ", deathBenefitOptType),
						getIdOf(getCoverage()));
			}
		}
	}

	/**
	 * Verify AllocPercent greater than or equal to the minimum allowed based on plan business rules for Annuity products
	 * Please refer to process_P154 for Life products.
	 * This process test if the allocation is a percentage, and test if the allocation (SubAccount) is a payment or charge. 
	 * If both conditions are true, and the MinPct value is greater than plan level value (SubAccount.AllocPercent) for payments, or (SubAccount.PolicyChargePct) for charges, add a new System Message.
	 */
	//SPR2986 New Method
	protected void process_P242() {
		if (verifyCtl(SUBACCOUNT)) {
			if (getLogger().isDebugEnabled()) {
				logDebug("Performing NbaValInsurance.process_P242() for ", getSubAccount());
			}
			//check if the allocation is a percentage and  the allocation is a payment or change
			long systematicActivityCode = getSubAccount().getSystematicActivityType();
			if ((-1L == systematicActivityCode || OLI_SYSACTTYPE_CHARGES == systematicActivityCode)
					&& OLI_TRANSAMTTYPE_PCT == getSubAccountExtension().getAllocType()) {
				FeatureProduct featureProd = null;
				FeatureOptProduct featureOptProd = null;
				int featureOptProdSize = -1;
				ArrayList allFeatureProd = getAnnuityProductFor(getPolicyProductForPlan()).getFeatureProduct();
				int count = allFeatureProd.size();
				Validate: for (int i = 0; i < count; i++) {
					featureProd = (FeatureProduct) allFeatureProd.get(i);
					featureOptProdSize = featureProd.getFeatureOptProductCount();
					if (OLI_ARRTYPE_STANDINGALLOC == featureProd.getArrType() && featureOptProdSize > 0) {
						for (int j = 0; j < featureOptProdSize; j++) {
							featureOptProd = featureProd.getFeatureOptProductAt(j);
							if (featureOptProd.hasMinPct()) {
								validateMinPct(featureOptProd.getMinPct(), systematicActivityCode);
								break Validate;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Verify that the requested MaturityAge or MaturityDate is present of Vantage Annuty.
	 */
	//SPR2067 new method
	protected void process_P243() {
		if (verifyCtl(ANNUITY)) {
			logDebug("Performing NbaValInsurance.process_P243()");
			if (!(annuity.hasRequestedMaturityAge() || annuity.hasRequestedMaturityDate())) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getAnnuity()));
			}
		}
	}

	/**
	 * Verify PWRateUsage=1000500003(3 BES value) and PWRateFactor present on increase rider.  If not, 
	 * set according to these rules from the value on the base coverage:
	 * If base coverage rate usage=1000500006 (6 BES value)then base have a rating factor
	 * set increase with rate usage=1000500002 (2 BES value) no need to send PWRateFactor since it will default from base.
	 * If base coverage rate usage=1000500005 (5 BES value)then base does not have a rating factor
	 * set increase with a rate usage=1000500004 (4 BES value)no need to send PWRateFactor
	 * For other rate usage set increase with a rate usage and rate factor from base
	 */
	//SPR3043 New Method
	protected void process_P244() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValInsurance.process_P244()");
			CoverageExtension baseCovExt = getBaseCoverageExtension(getLife());
			if (baseCovExt != null) {
				int covCount = getLife().getCoverageCount();
				Coverage coverage = null;
				CoverageExtension coverageExt = null;
				for (int i = 0; i < covCount; i++) {
					coverage = getLife().getCoverageAt(i);
                    if ((coverage.getIndicatorCode() == OLI_COVIND_RIDER || coverage.getIndicatorCode() == OLI_COVIND_INTEGRATED)
							&& DATAREP_TYPES_FULL.equalsIgnoreCase(coverage.getDataRep())) { //NBA237
						coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
						if (coverageExt == null) {
							OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_COVERAGE);
							oLifEExtension.setActionAdd();
							coverage.addOLifEExtension(oLifEExtension);
							coverageExt = oLifEExtension.getCoverageExtension();
						}
						if (!(coverageExt.getPWRateUsage() == NBA_PW_RATEUSAGE_ENTERED_RATE_FACTOR && coverageExt.hasPWRateFactor())) {
							if (baseCovExt.getPWRateUsage() == NBA_PW_RATEUSAGE_ENTERED_BENEFIT_RATE_FACTOR) {
								coverageExt.setPWRateUsage(NBA_PW_RATEUSAGE_SAME_AS_PW_BENEFIT);
								coverageExt.setActionUpdate();
							} else if (baseCovExt.getPWRateUsage() == NBA_PW_RATEUSAGE_NO_BENEFIT_RATE_FACTOR) {
								coverageExt.setPWRateUsage(NBA_PW_RATEUSAGE_NO_RATE_FACTOR);
								coverageExt.setActionUpdate();
							} else {
								coverageExt.setPWRateUsage(baseCovExt.getPWRateUsage());
								coverageExt.setPWRateFactor(baseCovExt.getPWRateFactor());
								coverageExt.setActionUpdate();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * For all coverages, add inherent coverage options where applicable. Refresh the inherent coverage options on the contract with those from the
	 * plan definition. The ones that have been modified by the user will be left intact. The benefit that has been modified or denied will have the
	 * same benefit type and subtype and will have an existing Holding.Policy.Life.Coverage.CovOption.CovOptionExtension. SelectionRule=1000500014.
	 * Remove any existing inherent benefits that are not defined to the plan rules.
	 */
	//NBA143 New Method
	protected void process_P245() {
		if (verifyCtl(COVERAGE)) {
			if (getLogger().isDebugEnabled()) {
				logDebug("Performing NbaValInsurance.process_P245() for ", getCoverage());
			}
			Coverage cov = getCoverage();
			CoverageProduct covProd = getCoverageProductFor(cov);
			//removeAllUnmodifiedInherentBenefits(cov.getCovOption()); //ALS4958, ALII134 
			if (covProd != null && hasInherentBenefit(covProd)) {
				addOrRefreshInherentBenefits(covProd, cov);
			}
		}
	}

	/**
	 * For all Riders, add inherent coverage options where applicable. Refresh the inherent coverage options on the contract with those from the plan
	 * definition. The ones that have been modified by the user will be left intact. The benefit that has been modified or denied will have the same
	 * benefit type and subtype and will have an existing Holding.Policy.Annuity.Rider.CovOption.CovOptionExtension.SelectionRule=1000500014. Remove
	 * any existing inherent benefits that are not defined to the plan rules.
	 */
	//NBA143 New Method
	protected void process_P246() {
		if (verifyCtl(RIDER)) {
			if (getLogger().isDebugEnabled()) {
				logDebug("Performing NbaValInsurance.process_P246() for ", getRider());
			}
			Rider rider = getRider();
			CoverageProduct covProd = getCoverageProductFor(rider);
			removeAllUnmodifiedInherentBenefits(rider.getCovOption());
			if (covProd != null && hasInherentBenefit(covProd)) {
				addOrRefreshInherentBenefits(covProd, rider);
			}
		}
	}

	/**
	 * Validate InitialPremAmt (planned initial) against NBACONTRACTVALIDATIONCALCULATIONS
	 */
	//NBA142 New Method
	protected void process_P247() {
		if (verifyCtl(LIFE)) {
			if (getLogger().isDebugEnabled()) {
				logDebug("Performing NbaValInsurance.process_P247() for ", getLife());
			}
			LifeProductExtension lifeProdExtn = getLifeProductExtensionForPlan();
			if (null == lifeProdExtn) {
				addPlanInfoMissingMessage("LifeProduct", getIdOf(getLife()));
			} else if (NbaOliConstants.OLIX_MINPREMINITRULE_GREATER == lifeProdExtn.getMinPremInitialRule()) {
				validateMinInitPremium(lifeProdExtn);
			}
		}
	}
	//NBA117 code deleted
	
	/**
	 * Reset MEC Indicator and MEC1035 indicators before calling L70 to recalculate both again.
	 */
	//NBA298 New Method
	protected void process_P265() {
		if (verifyCtl(LIFE)) {
			getLifeUSA().setMECInd(false);
			getLifeUSA().setMEC1035(false);
			getLifeUSA().setActionUpdate();
		}
	}
	
	/**
	 * Set MEC Indicator depending upon the value entered for MECStatus on replacement view along with the error set by P262
	 * Also set the MECReason to null if the MEC indicator is determined as false by this process 
	 */
	//NBA298 New Method
	protected void process_P263() {
		if (verifyCtl(HOLDING)) {
			boolean caseIsMEC = getLifeUSA().getMECInd();//If L70 has made it a MEC case (Base code will check 2104 message code instead)
			if (!caseIsMEC) {
				List replHoldings = NbaUtils.getReplacementHolding(getNbaTXLife());
				for (int i = 0; i < replHoldings.size(); i++) {
					Holding holding = (Holding) replHoldings.get(i);
					LifeUSA lifeUSA = ((Life) holding.getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents()).getLifeUSA();
					if (lifeUSA != null) {
						LifeUSAExtension replLifeUSAExt = NbaUtils.getFirstLifeUSAExtension(lifeUSA);
						if (replLifeUSAExt != null && replLifeUSAExt.getMECStatus() == OLI_MECSTATUS_CONTRACT_IS_MEC) {
							caseIsMEC = true;
							break;
						}
					}
				}
			}

			if (caseIsMEC) {
				getLifeUSA().setMECInd(true);
				getLifeUSA().setMEC1035(true);
			} else {
				getLifeUSA().setMECInd(false);
				getLifeUSA().setMEC1035(false);
				//APSL3589 code deleted 
				getLifeUSAExtension().setActionUpdate();
			}
			getLifeUSA().setActionUpdate();
		}
	}	

	/**
	 * Validate if the MEC Indicator is set to true but the MEC Reason field is null
	 */
	//NBA298 New Method
	protected void process_P264() {
		if (verifyCtl(LIFE)) {
			if (getLifeUSA().getMECInd() && getLifeUSAExtension().getMECReason() == NbaOliConstants.OLI_TC_NULL) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getLife()));
			}
		}
	}	
}
