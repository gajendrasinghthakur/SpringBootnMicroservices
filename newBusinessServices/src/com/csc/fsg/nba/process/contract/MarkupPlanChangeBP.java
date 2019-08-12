package com.csc.fsg.nba.process.contract;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaAllowablePlanChangeData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaContractMessage;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaPlanChangeRequest;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.Arrangement;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.HoldingExtension;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeExtension;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.TaxReporting;

/**
 * Provides markup changes to an existing contract required due to a plan change.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA181</td><td>Version 7</td><td>Contract Plan Change Rewrite</td></tr>
 * <tr><td>NBA180</td><td>Version 7</td><td>Contract Copy Rewrite</td></tr>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class MarkupPlanChangeBP extends ValidateContractBP {

	// contract validation - auto delete messages
	protected static String Mx_AutoDel_Funds = "mx_AutoDel_Funds";
	protected static String Mx_AutoDel_DCA = "mx_AutoDel_DCA";
	protected static String Mx_AutoDel_AssetRealloc = "mx_AutoDel_AssetRealloc";
	protected static String Mx_AutoDel_SchedWtdrwl = "mx_AutoDel_SchedWtdrwl";
	protected static String Mx_AutoDel_PremAmt = "mx_AutoDel_PremAmt";
	protected static String Mx_AutoDel_BillingOpt = "mx_AutoDel_BillingOpt";
	protected static String Mx_AutoDel_QuotedPremFreq = "mx_AutoDel_QuotedPremFreq";
	protected static String Mx_AutoDel_QuotedPremAmt = "mx_AutoDel_QuotedPremAmt";
	protected static String Mx_AutoDel_AddlLevelAmt = "mx_AutoDel_AddlLevelAmt";
	protected static String Mx_AutoDel_ExcessAmt = "mx_AutoDel_ExcessAmt";
	protected static String Mx_AutoDel_TaxYear = "mx_AutoDel_TaxYear";
	protected static String Mx_AutoDel_TaxableAmt = "mx_AutoDel_TaxableAmt";
	protected static String Mx_AutoDel_PaidUpAdd = "mx_AutoDel_PaidUpAdd";
	protected static String Mx_AutoDel_PlanAddlPrem = "mx_AutoDel_PlanAddlPrem";
	protected static String Mx_AutoDel_InitAnnual = "mx_AutoDel_InitAnnual";
	protected static String Mx_AutoDel_QualPlan = "mx_AutoDel_QualPlan";
	protected static String Mx_AutoDel_FirstTaxYr = "mx_AutoDel_FirstTaxYr";
	protected static String Mx_AutoDel_IncomeOpt = "mx_AutoDel_IncomeOpt";
	protected static String Mx_AutoDel_ReqMaturity = "mx_AutoDel_ReqMaturity";
	protected static String Mx_AutoDel_RothIRA = "mx_AutoDel_RothIRA";
	protected static String Mx_AutoDel_NonFortProv = "mx_AutoDel_NonFortProv";
	protected static String Mx_AutoDel_DivType = "mx_AutoDel_DivType";
	protected static String Mx_AutoDel_InitPremAmt = "mx_AutoDel_InitPremAmt";
	protected static String Mx_AutoDel_Amt1035 = "mx_AutoDel_Amt1035";
	protected static String Mx_AutoDel_SecDivType = "mx_AutoDel_SecDivType";
	protected static String Mx_AutoDel_DeathBeneOpt = "mx_AutoDel_DeathBeneOpt";
	protected static String Mx_AutoDel_CurrentAmt = "mx_AutoDel_CurrentAmt";
	protected static String Mx_AutoDel_RateClass = "mx_AutoDel_RateClass";
	protected static String Mx_AutoDel_BlendTrgtDBPct = "mx_AutoDel_BlendTrgtDBPct";
	protected static String Mx_AutoDel_BlendTrgtDBInd = "mx_AutoDel_BlendTrgtDBInd";
	protected static String Mx_AutoDel_GuidelineExempt = "mx_AutoDel_GuidelineExempt";
	protected static String Mx_AutoDel_UnitTypeInd = "mx_AutoDel_UnitTypeInd";
	protected static String AUTO_DEL_MSG_PREFIX = "AutoDel_";
	//Start	ALII770
	protected static String Mx_AutoDel_AssetAlloc = "mx_AutoDel_AssetAlloc";
	protected static String Mx_AutoDel_AcctValAdj = "mx_AutoDel_AcctValAdj";
	protected static String Mx_AutoDel_StandingAlloc = "mx_AutoDel_StandingAlloc";
	protected static String Mx_AutoDel_Chargededuction = "mx_AutoDel_Chargededuction";
	protected static String Mx_AutoDel_OtherArrangement = "mx_AutoDel_OtherArrangement";
	//End ALII770
	private long selectedProduct = NbaOliConstants.OLI_TC_NULL;
	private long selectedCovType = NbaOliConstants.OLI_TC_NULL;  //NBA180
	private boolean contractCopy = false;
	private int msgCount = 0;

	protected NbaTXLife markupPlanChange(NbaTXLife contract) throws Exception {

		NbaHolding nbaHolding = contract.getNbaHolding();
		deleteFunds(nbaHolding.getHolding());
		deleteArrangements(nbaHolding.getHolding());
		deleteInvalidBillingInfo(nbaHolding.getPolicy());
		deleteInvalidCovPartyInfo(nbaHolding);
		deletePlanName(nbaHolding.getPolicy());	//ALS3363
		return contract;
	}
	//ALS3363 New method
	protected void deletePlanName(Policy policy) {
		if (policy.hasPlanName()) {
			policy.deletePlanName();
			policy.setActionUpdate();
		}
		List covList = new ArrayList();
		covList = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().getCoverage();
		int covCount = covList.size();
		for (int i = 0; i < covCount; i++) {
			Coverage coverage = (Coverage) covList.get(i);
			if (coverage.hasPlanName()) {
				coverage.deletePlanName();
				coverage.setActionUpdate();
			}
		}
	}
	/**
	 * Deletes all funds from the existing contract.
	 * @param holding
	 */
	protected void deleteFunds(Holding holding) {
		if (holding.hasInvestment()) {
			if (isContractCopy()) {
				holding.deleteInvestment();
			} else {
				holding.getInvestment().setActionDelete();
			}
			addAutoDeletedMessage(Mx_AutoDel_Funds);
		}
	}

	/**
	 * Deletes any Dollar Cost Averaging, Asset Allocations, and Scheduled Withdrawals from the existing contract.
	 * @param holding 
	 */
	protected void deleteArrangements(Holding holding) {
		int count = holding.getArrangementCount();
		for (int i = 0; i < count; i++) {
			Arrangement arr = holding.getArrangementAt(i);
			//ALII770, Remove the arrangement specific condition,all the arrangements should be deleted
			if (isContractCopy()) {
				holding.removeArrangement(arr);
			} else {
				arr.setActionDelete();
			}
			if (arr.getArrType() == NbaOliConstants.OLI_ARRTYPE_COSTAVG) {
				addAutoDeletedMessage(Mx_AutoDel_DCA);
			} else if (arr.getArrType() == NbaOliConstants.OLI_ARRTYPE_ASSALLO) {
				addAutoDeletedMessage(Mx_AutoDel_AssetRealloc);
			//Start ALII770
			} else if (arr.getArrType() == NbaOliConstants.OLI_ARRTYPE_SPECAMTNETWITH) {
				addAutoDeletedMessage(Mx_AutoDel_SchedWtdrwl);
			} else if (arr.getArrType() == NbaOliConstants.OLI_ARRTYPE_AA) {
				addAutoDeletedMessage(Mx_AutoDel_AssetAlloc);
			} else if (arr.getArrType() == NbaOliConstants.OLI_ARRTYPE_ACCTVALUEADJ) {
				addAutoDeletedMessage(Mx_AutoDel_AcctValAdj);
			} else if (arr.getArrType() == NbaOliConstants.OLI_ARRTYPE_STANDINGALLOC) {
				addAutoDeletedMessage(Mx_AutoDel_StandingAlloc);
			} else if (arr.getArrType() == NbaOliConstants.OLI_ARRTYPE_CHARGEDEDUCTION) {
				addAutoDeletedMessage(Mx_AutoDel_Chargededuction);
			} else {
				addAutoDeletedMessage(Mx_AutoDel_OtherArrangement);
			}
			//End ALII770
		}
	}

	/**
	 * Deletes contract data specific to the Billing business function that is no longer applicable because of the change in product type.
	 * 
	 * @param policy
	 */
	protected void deleteInvalidBillingInfo(Policy policy) {
		if (policy.hasPaymentAmt()) {
			if (isSelectedProductTraditional() || isSelectedProductISWL()) {
				policy.deletePaymentAmt();
				policy.setActionUpdate();
				addAutoDeletedMessage(Mx_AutoDel_PremAmt);
			}
		}

		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExtension != null) {
			if (!(isSelectedProductFlexiblePremium())) {
				if (policyExtension.hasBillingOption()) {
					policyExtension.deleteBillingOption();
					policyExtension.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_BillingOpt);
				}
				if (policyExtension.hasQuotedPremiumBasisFrequency()) {
					policyExtension.deleteQuotedPremiumBasisFrequency();
					policyExtension.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_QuotedPremFreq);
				}
				if (policyExtension.hasQuotedPremiumBasisAmt()) {
					policyExtension.deleteQuotedPremiumBasisAmt();
					policyExtension.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_QuotedPremAmt);
				}
			}

			if (policyExtension.hasAdditionalLevelPremiumAmt()) {
				if (!(getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_WL || getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_INTWL)) {
					policyExtension.deleteAdditionalLevelPremiumAmt();
					policyExtension.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_AddlLevelAmt);
				}
			}

			if (policyExtension.hasExcessCollectedAmt()) {
				if (!(isSelectedProductTraditional() || isSelectedProductISWL() || isSelectedProductUL())) {
					policyExtension.deleteExcessCollectedAmt();
					policyExtension.setActionUpdate();
					if (!(Double.isNaN(policyExtension.getExcessCollectedAmt()))) {
						addAutoDeletedMessage(Mx_AutoDel_ExcessAmt);
					}
				}
			}
		}
	}

	/**
	 * Deletes contract data specific to the Coverage/Party business function that is no
	 * longer applicable because of the change in product type.
	 * @param nbaHolding
	 */
	protected void deleteInvalidCovPartyInfo(NbaHolding nbaHolding) {
		deleteCoveragePartyInvalidInfo(nbaHolding.getHolding());
		deleteCoveragePartyInvalidInfo(nbaHolding.getPolicy());
		deleteCoveragePartyInvalidInfo(nbaHolding.getAnnuity());
		deleteCoveragePartyInvalidInfo(nbaHolding.getLife());
	}

	/**
	 * Deletes contract <code>Holding</code> data specific to the Coverage/Party business
	 * function that is no longer applicable because of the change in product type.
	 * @param holding
	 */
	protected void deleteCoveragePartyInvalidInfo(Holding holding) {
		HoldingExtension holdingExtension = NbaUtils.getFirstHoldingExtension(holding);
		if (holdingExtension != null) {
			if (!isSelectedProductAnnuity()) {
				if (holdingExtension.getTaxReportingCount() > 0) {
					TaxReporting taxReporting = holdingExtension.getTaxReportingAt(0); 
					if (taxReporting.hasTaxYear()) {
						taxReporting.deleteTaxYear();
						taxReporting.setActionUpdate();
						addAutoDeletedMessage(Mx_AutoDel_TaxYear);
					}
					if (taxReporting.hasTaxableAmt()) {
						taxReporting.deleteTaxableAmt();
						taxReporting.setActionUpdate();
						addAutoDeletedMessage(Mx_AutoDel_TaxableAmt);
					}
				}
			}
		}
	}

	/**
	 * Deletes contract <code>Policy</code> data specific to the Coverage/Party business
	 * function that is no longer applicable because of the change in product type.
	 * @param policy
	 */
	protected void deleteCoveragePartyInvalidInfo(Policy policy) {
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExtension != null) {
			if (!isSelectedProductISWL()) {
				if (policyExtension.hasPaidUpAdditionsOptionElected()) {
					policyExtension.deletePaidUpAdditionsOptionElected();
					policyExtension.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_PaidUpAdd);
				}

				if (policyExtension.hasPlannedAdditionalPremium()) {
					policyExtension.deletePlannedAdditionalPremium();
					policyExtension.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_PlanAddlPrem);
				}

				if (policyExtension.hasInitialAnnualPremiumAmt()) {
					policyExtension.deleteInitialAnnualPremiumAmt();
					policyExtension.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_InitAnnual);
				}
			}
		}
	}

	/**
	 * Deletes contract <code>Annuity</code> data specific to the Coverage/Party business
	 * function that is no longer applicable because of the change in product type.
	 * @param policy
	 */
	protected void deleteCoveragePartyInvalidInfo(Annuity annuity) {
		if (annuity != null && !isSelectedProductAnnuity()) {
			if (annuity.hasQualPlanType()) {
				annuity.deleteQualPlanType();
				annuity.setActionUpdate();
				addAutoDeletedMessage(Mx_AutoDel_QualPlan);
			}
			if (annuity.hasFirstTaxYear()) {
				annuity.deleteFirstTaxYear();
				annuity.setActionUpdate();
				addAutoDeletedMessage(Mx_AutoDel_FirstTaxYr);
			}
			if (annuity.getPayoutCount() > 0) {
				Payout payout = annuity.getPayoutAt(0);
				if (payout.hasIncomeOption()) {
					payout.deleteIncomeOption();
					payout.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_IncomeOpt);
				}
			}
			if (annuity.hasRequestedMaturityAge() || annuity.hasRequestedMaturityDate() || annuity.hasRequestedMaturityDur()) {
				annuity.deleteRequestedMaturityAge();
				annuity.deleteRequestedMaturityDate();
				annuity.deleteRequestedMaturityDur();
				annuity.setActionUpdate();
				addAutoDeletedMessage(Mx_AutoDel_ReqMaturity);
			}
			if (annuity.hasRothIraNetContributionAmt()) {
				annuity.deleteRothIraNetContributionAmt();
				annuity.setActionUpdate();
				addAutoDeletedMessage(Mx_AutoDel_RothIRA);
			}
		}
	}

	/**
	 * Deletes contract <code>Life</code> data specific to the Coverage/Party business
	 * function that is no longer applicable because of the change in product type.
	 * @param policy
	 */
	protected void deleteCoveragePartyInvalidInfo(Life life) {
		if (life != null) {
			if (isSelectedProductFlexiblePremium()) {
				if (life.hasNonFortProv()) {
					life.deleteNonFortProv();
					life.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_NonFortProv);
				}
				if (life.hasDivType()) {
					life.deleteDivType();
					life.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_DivType);
				}
			} else {
				if (life.hasInitialPremAmt()) {
					life.deleteInitialPremAmt();
					life.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_InitPremAmt);
				}
			}
			if (life.hasLifeUSA()) {
				LifeUSA lifeUsa = life.getLifeUSA();
				if (lifeUsa.hasAmount1035()) {
					if (!(getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_WL || getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_TERM
						|| getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_UL || getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_VUL
						|| getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_ANN || getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_VAR
						|| getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_EXINTL || getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_TERMCV
						|| getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_TRADITIONAL || getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_INDETERPREM
						|| getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_INTWL)) {
						lifeUsa.deleteAmount1035();
						lifeUsa.setActionUpdate();
						addAutoDeletedMessage(Mx_AutoDel_Amt1035);
					}
				}
			}

			LifeExtension lifeExtension = NbaUtils.getFirstLifeExtension(life);
			if (lifeExtension != null) {
				if (lifeExtension.hasSecondaryDividendType()) {
					if (isSelectedProductFlexiblePremium()) {
						lifeExtension.deleteSecondaryDividendType();
						lifeExtension.setActionUpdate();
						addAutoDeletedMessage(Mx_AutoDel_SecDivType);
					}
				}
				if (lifeExtension.hasRequestedMaturityAge() || lifeExtension.hasRequestedMaturityDate() || lifeExtension.hasRequestedMaturityDur()) {
					if (!isSelectedProductUL()) {
						lifeExtension.deleteRequestedMaturityAge();
						lifeExtension.deleteRequestedMaturityDate();
						lifeExtension.deleteRequestedMaturityDur();
						lifeExtension.setActionUpdate();
						addAutoDeletedMessage(Mx_AutoDel_ReqMaturity);
					}
				}
			}

			// process invalid info on coverages 
			int count = life.getCoverageCount();
			for (int i = 0; i < count; i++) {
				deleteCoveragePartyInvalidInfo(life.getCoverageAt(i));
			}
		}
	}

	/**
	 * Deletes contract <code>Coverage</code> data specific to the Coverage/Party business
	 * function that is no longer applicable because of the change in product type.
	 * @param policy
	 */
	protected void deleteCoveragePartyInvalidInfo(Coverage coverage) {
		if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) { //retrieving Base coverage
			if (coverage.hasDeathBenefitOptType()) {
				if (!isSelectedProductUL()) {
					coverage.deleteDeathBenefitOptType();
					coverage.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_DeathBeneOpt);
				}
			}
			if (coverage.hasCurrentAmt()) {
				if (isSelectedProductAnnuity() || isSelectedProductBlended()) {
					coverage.deleteCurrentAmt();
					coverage.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_CurrentAmt);
				}
			}

			CoverageExtension coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
			if (coverageExt != null) {
				if (coverageExt.hasRateClass() && isSelectedProductAnnuity()) {
					coverageExt.deleteRateClass();
					coverageExt.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_RateClass);
				}
				if (coverageExt.hasBlendedInsTargetDBAmtPct() && !isSelectedProductBlended()) {
					coverageExt.deleteBlendedInsTargetDBAmtPct();
					coverageExt.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_BlendTrgtDBPct);
				}
				if (coverageExt.hasBlendedInsTargetDBInd() && !isSelectedProductBlended()) {
					coverageExt.deleteBlendedInsTargetDBInd();
					coverageExt.setActionUpdate();
					addAutoDeletedMessage(Mx_AutoDel_BlendTrgtDBInd);
				}
				if (coverageExt.hasGuidelineExemptInd()) {
					if (!isSelectedProductTerm()) {
						coverageExt.deleteGuidelineExemptInd();
						coverageExt.setActionUpdate();
						addAutoDeletedMessage(Mx_AutoDel_GuidelineExempt);
					}
				}
				if (coverageExt.hasUnitTypeInd()) {
					if (isSelectedProductAnnuity() || isSelectedProductBlended()) {
						coverageExt.deleteUnitTypeInd();
						coverageExt.setActionUpdate();
						addAutoDeletedMessage(Mx_AutoDel_UnitTypeInd);
					}
				}
			}
		}
	}

	/**
	 * Returns true if the selected product is an advanced product.
	 * @return
	 */
	protected boolean isSelectedProductAdvanced() {
		return NbaUtils.isAdvancedLifeProduct(getSelectedProduct());
	}

	/**
	 * Returns true if the selected product is an annuity.
	 * @return
	 */
	protected boolean isSelectedProductAnnuity() {
		return NbaUtils.isAnnuityProduct(getSelectedProduct());
	}

	/**
	 * Returns true if the selected product is blended.
	 * @return
	 */
	protected boolean isSelectedProductBlended() {
		return getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_BLENDED;
	}

	/**
	 * Returns true if the selected product is a fixed premium product.
	 * @return
	 */
	protected boolean isSelectedProductFixedPremium() {
		return NbaUtils.isFixedPremiumLifeProduct(getSelectedProduct());
	}

	/**
	 * Returns true if the selected product is a flexible premium product.
	 * @return
	 */
	protected boolean isSelectedProductFlexiblePremium() {
		return NbaUtils.isFlexiblePremiumProduct(getSelectedProduct());
	}

	/**
	 * Returns true if the selected product is an interest sensitive whole life product.
	 * @return
	 */
	protected boolean isSelectedProductISWL() {
		return getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_INTWL;
	}

	/**
	 * Returns true if the selected product is a traditional product.
	 * @return
	 */
	protected boolean isSelectedProductTraditional() {
		return NbaUtils.isTraditional(String.valueOf(getSelectedProduct()));
	}

	/**
	 * Returns true if the selected product is a term product.
	 * @return
	 */
	protected boolean isSelectedProductTerm() {
		return getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_TERM || getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_TERMCV;
	}

	/**
	 * Returns true if the selected product is a whole life product.
	 * @return
	 */
	protected boolean isSelectedProductUL() {
		return getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_UL || getSelectedProduct() == NbaOliConstants.OLI_PRODTYPE_VUL;
	}

	/**
	 * Returns the plan change selected product type.
	 * @return
	 */
	protected long getSelectedProduct() {
		return selectedProduct;
	}

	/**
	 * Returns the plan change selected product type.
	 * @return
	 */
	//NBA180 New Method
	protected long getSelectedCovType() {
		return selectedCovType;
	}

	/**
	 * Determines the selected product type based on the selected plan.
	 * @param work
	 * @param selectedPlan
	 * @throws Exception
	 */
	protected void setSelectedProduct(NbaDst work, String selectedPlan, boolean isOverriden) throws Exception {
		NbaTableAccessor tableAccessor = new NbaTableAccessor();
		Map tableMap = tableAccessor.setupTableMap(work);
		tableMap.put(NbaTableAccessConstants.USAGE, "NbaPlanChange"); //passing the value of USAGE column to the query
		tableMap.put(NbaTableAccessConstants.C_TABLE_NAME, NbaTableConstants.NBA_ALLOWABLE_PLAN_CHANGES);
		//begin NBA139
		if (NbaConfiguration.getInstance().isGenericPlanImplementation()) {
            if (isOverriden) {
                tableMap.put(NbaTableAccessConstants.C_GENERIC_PLAN, NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
                        NbaConfigurationConstants.GENERIC_PLAN_OVERRIDE_QUERYKEY));
            } else {
                tableMap.put(NbaTableAccessConstants.C_GENERIC_PLAN, NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
                        NbaConfigurationConstants.GENERIC_PLAN_QUERYKEY));
            }
        } else {
            tableMap.put(NbaTableAccessConstants.C_GENERIC_PLAN, NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
                    NbaConfigurationConstants.GENERIC_PLAN_OVERRIDE_QUERYKEY));
        }
		NbaAllowablePlanChangeData[] planChangeObj =(NbaAllowablePlanChangeData[]) tableAccessor.getPlansDisplayData(tableMap);
		//end NBA139
		for (int i = 0; i < planChangeObj.length; i++) {
			if (planChangeObj[i].getCoverageKey().equalsIgnoreCase(selectedPlan)) {
				selectedProduct = Long.parseLong(planChangeObj[i].getProductType());
				selectedCovType = Long.parseLong(planChangeObj[i].getCoverageType());  //NBA180
				break;
			}
		}
	}

	/**
	 * Returns true if validating for plan change.
	 * @return
	 */
	protected boolean isContractCopy() {
		return contractCopy;
	}

	/**
	 * Set to true if validating for plan change.
	 * @param value
	 */
	protected void setContractCopy(boolean value) {
		contractCopy = value;
	}

	/**
	 * Adds a contract validation error message with the auto deleted flag set to true.
	 * @param description 
	 */
	protected void addAutoDeletedMessage(String description) {
		NbaContractMessage message = new NbaContractMessage();
		message.setID(AUTO_DEL_MSG_PREFIX + ++msgCount);
		message.setCode(NbaOliConstants.OLI_TC_NULL);
		message.setDescription(description);
		message.setAutoDeleted(true);
		getMessages().add(message);
	}
	
	/**
     * Returns a marked up contract <code>NbaTXLife</code> for a plan change request.
     * @param request
     * @return
     * @throws Exception
     */
    protected NbaTXLife getContract(NbaPlanChangeRequest request) throws Exception {
    	NbaTXRequestVO txRequest = createRequestObject(request.getNbaUserVO(), request.getNbaDst());
    	NbaTXLife contract = NbaContractAccess.doContractInquiry(txRequest);
    	Policy policy = contract.getPolicy();
    	if (request.getSelectedPlan() == null || request.getSelectedPlan().equals(policy.getProductCode())) {
    		policy.deleteFiledFormNumber();  //Contract validation will reset
    	} else {
    		policy.setProductCode(request.getSelectedPlan());
    		policy.setProductType(getSelectedProduct());
    		policy.setActionUpdate();
    		//begin NBA139	
    		PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(policy);
            if (polExt != null) {
                polExt.setGenericPlanOverrideInd(request.isOverriden());
                if (request.getGenericPlanLevelPeriod() != null) {
                    polExt.setGenericPlanLevelPeriod(request.getGenericPlanLevelPeriod());
                }
                polExt.setGenericPlanCalculationMethod(request.getGenericPlanCalculationMethod());
                polExt.setGenericPlanOverrideInd(request.isOverriden());
                polExt.setGenericPlan(request.getSelectedPlan());
                polExt.setActionUpdate();
            }
            //end NBA139
    		// if it's a Life product, update the base coverage
    		Coverage baseCoverage = contract.getPrimaryCoverage();
    		if (baseCoverage != null) {
    			if (!request.getSelectedPlan().equals(baseCoverage.getProductCode())) {
    				baseCoverage.deleteFormNo(); //Contract validation will reset
    			}
    			baseCoverage.setProductCode(request.getSelectedPlan());
    			baseCoverage.setLifeCovTypeCode(getSelectedCovType());  //NBA180
    			baseCoverage.setActionUpdate();
    		}
    	}
    	return contract;
    }

    /**
     * Create a <code>NbaTXRequestVO</code> value object that will be used to
     * retrieve the contract.
     * @param user current user
     * @param work current work item
     * @return a value object that is the request
     */
    protected NbaTXRequestVO createRequestObject(NbaUserVO user, NbaDst work) {
    	NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
    	nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
    	nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
    	nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
    	nbaTXRequest.setNbaLob(work.getNbaLob());
    	nbaTXRequest.setNbaUser(user);
    	nbaTXRequest.setWorkitemId(work.getID()); 
    	nbaTXRequest.setCaseInd(work.isCase()); 
    	nbaTXRequest.setAccessIntent(NbaConstants.UPDATE); 
    	nbaTXRequest.setBusinessProcess(NbaConstants.PROC_VIEW_PLAN_CHANGE);
    	return nbaTXRequest;
    }
}


