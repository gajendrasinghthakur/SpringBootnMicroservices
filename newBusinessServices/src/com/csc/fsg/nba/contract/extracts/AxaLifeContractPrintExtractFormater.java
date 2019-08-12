package com.csc.fsg.nba.contract.extracts;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.accel.util.SortingHelper;
import com.csc.fsg.nba.contract.calculations.NbaContractCalculationsConstants;
import com.csc.fsg.nba.contract.calculations.NbaContractCalculatorFactory;
import com.csc.fsg.nba.contract.calculations.backend.AxaPrintCalculationUtil;
import com.csc.fsg.nba.contract.calculations.backend.NbaBackendContractCalculator;
import com.csc.fsg.nba.contract.calculations.backend.NbaBackendContractCalculatorFactory;
import com.csc.fsg.nba.contract.calculations.results.CalcProduct;
import com.csc.fsg.nba.contract.calculations.results.CalculationResult;
import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaReasonsData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaRetrieveCommentsRequest;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.AltPremMode;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.AttachmentExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.ExtractIllusSummaryInfo;
import com.csc.fsg.nba.vo.txlife.ExtractScheduleInfo;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeExtension;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.MedicalExam;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UnderwritingResult;
import com.csc.fsg.nba.vo.txlife.UnderwritingResultExtension;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;


/**
 * AxaLifeContractPrintExtractFormater extends NbaContractPrintExtractFormater and overrides certain methods which were required as part of the
 * enhancement AXAL3.7.14 Contract Print Interface.
 *  
 * AxaLifeContractPrintExtractFormater 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL048</td><td>AXA Life Phase 2 R2</td><td>P2R2 ISWL CAPS Issue</td></tr>
 * <tr><td>CR1455063</td><td>Discretionary</td><td>Joint Insured Correspondence</td></tr>
 * </table>
 * <p> 
 */
public class AxaLifeContractPrintExtractFormater extends NbaContractPrintExtractFormater implements NbaConstants {
	
	protected static final String BP_RETRIEVE_COMMENTS = "RetrieveCommentsBP";  
	/**
	 * Create Objects as needed based on the report types requested.
	 */
	public void createReportObjects() throws NbaBaseException, NbaVpmsException {
		try {
			String extComp = getTransactionDst().getNbaLob().getExtractComp();	// List of extract types
			String extComp2 = getTransactionDst().getNbaLob().getPrintExtract();	// APSL5055 reprint indicator
			boolean reprintInd = (NbaConstants.REPRINT_EXTRACT.equalsIgnoreCase(extComp2)); //ALS5927
			NbaStringTokenizer extTokens = new NbaStringTokenizer(extComp, ",");
			TXLifeRequest tXLifeRequest =  createReportObjectsForExtract();
			String reportCode;
			long reportValue;
			while (extTokens.hasMoreTokens()) {
				reportCode = extTokens.nextToken();
				reportValue = Long.parseLong(reportCode);
				if (isValidAttachmentType(reportValue)) {
					addAttachmentToTXLifeRequest(reportValue, tXLifeRequest, OLI_LU_BASICATTMNTTY_TEXT, -1, reprintInd);
				} else {
					throw new NbaBaseException("Invalid report type in EXTC LOB field: " + reportCode);
				}
				if (tXLifeRequest != null) {
					addAttachmentsForReport(reportCode, tXLifeRequest);
				}
				setCommonTXLifeRequest(tXLifeRequest);
			}
			addTXLifeRequestForExtract(tXLifeRequest); // Add the completed TXLifeRequest
		} finally {
			removePrintAttachmentsVpmsAdaptor();
		}

	}
	/**
	 * This method will create the report objects as per the type of extract passed.
	 * @param extractType
	 * @param reprintInd
	 * @param reportAttachmentsOnly
	 * @return txLifeRequest
	 * @throws NbaBaseException
	 */
	
	private TXLifeRequest createReportObjectsForExtract() throws NbaBaseException {
		TXLifeRequest tXLifeRequest = getCopyOfCommonTXLifeRequest();
		if (!NbaUtils.isProductCodeCOIL(getNbaTxLife())) {
			if (NbaConstants.SYST_LIFE70.equalsIgnoreCase(getNbaTxLife().getBackendSystem())) {// P2AXAL029
				updateContractPrintBackendCalculations(tXLifeRequest);
			} else {
				// Create ExtractIllusSummaryInfo for each duration of the policy, each coverage, and each benefit
				addExtractIllusSummaryInfo(tXLifeRequest);
				// Update Life death benefit and lapse fields
				updateProjectedAmts(tXLifeRequest);
				// Create SummaryProjections for each duration of the policy, each coverage, and each benefit
				addExtractSummaryProjections(tXLifeRequest);
				// initializePolicyProductInfo(tXLifeRequest);
				setAdditionalRiskYearsInfoForCoverage(tXLifeRequest);
				// Create ExtractScheduleInfo for each duration of each coverage
				addExtractScheduleInfo(tXLifeRequest);
				// Create ExtractValuesInfo for each durtation for the primary coverage
				addExtractValuesInfo(tXLifeRequest);
			}
		}
		setAUDLetters(tXLifeRequest); 
		setAUDLettersText(tXLifeRequest);  //ALPC195
		setCommonTXLifeRequest(tXLifeRequest);
		return tXLifeRequest;
	}

	/**
	 * Retrieve the NbaCalculation from the applicable Document calculation model.
	 * @return NbaCalculation
	 */
	public NbaCalculation getDocsCalculation() throws NbaBaseException {
		if (docsCalculation == null) {
			Date startTime = Calendar.getInstance().getTime();
			docsCalculation = NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_AXA_CONTRACT_PRINT, getNbaTxLife());
			logElapsedTime(startTime, "Contract Documents VPMS calculation");
			if (docsCalculation.getCalcResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS) {
				throw new NbaVpmsException("Contract Documents VPMS calculation failure");
			}
		}
		return docsCalculation;
	}

	/**
	 * Construct ExtractScheduleInfo from the values returned from the model
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	public ExtractScheduleInfo createExtractScheduleInfoFromCalc(CalculationResult calculationResult, int issueAge, int dur) {
		CalcProduct calcProduct;
		String field;
		int prodCount;
		int prdIdx;
		ExtractScheduleInfo extractScheduleInfo = new ExtractScheduleInfo();
		extractScheduleInfo.setDuration(dur);
		extractScheduleInfo.setAge(issueAge + dur - 1);
		prodCount = calculationResult.getCalcProductCount();
		for (prdIdx = 0; prdIdx < prodCount; prdIdx++) { //Get the values for the current ExtractScheduleInfo
			calcProduct = calculationResult.getCalcProductAt(prdIdx);
			field = calcProduct.getType();
			if (field.equalsIgnoreCase("PolicyCurrCsv")) {
				extractScheduleInfo.setCashSurrValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyGuarCsv")) { //ALNA607 - QC9576
				extractScheduleInfo.setGuarCashValueAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovGuarCOIRate")) { //ALNA608 - QC9578
				extractScheduleInfo.setMaxMonthlyCOIRate(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CVATPercent")) { //ALNA609 - QC9580
				extractScheduleInfo.setLifeTestPercentage(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovSurrCharge")) { //ALNA623 - QC9712
				extractScheduleInfo.setSurrCharge(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovCurrAnnualPremium")) {
				extractScheduleInfo.setAnnualPremAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovGuarAnnualPremium")) {
				extractScheduleInfo.setGuarPrem(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovCurrModePremium")) {
				extractScheduleInfo.setModalPremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("BenefitCurrModePremium")) {
				extractScheduleInfo.setModalPremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("CovGuarModePremium")) {//Deleted the property "BenefitCurrPremium" from here as it would now be set in ExtractScheduleInfo
																	  // at the Policy level
				extractScheduleInfo.setGuarModalPremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("PolicyCurrModePremium")) {
				extractScheduleInfo.setModalPremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("PolicyGuarModePremium")) {
				extractScheduleInfo.setGuarModalPremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("RiderCurrModePremium")) {
				extractScheduleInfo.setModalPremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("CovCurrFlatExtraModePrem")) {
				extractScheduleInfo.setModalTotallFlatExtraCost(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("PolicyGuarAccumROPPremium")) {
				extractScheduleInfo.setGuarGrossCashValue(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("TotalSubstdPremium")) {
				extractScheduleInfo.setTotalSubstdModalPremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("PolicyCurrDeathBenefit")) {
				extractScheduleInfo.setDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovGuarDeathBenefit")) {
				extractScheduleInfo.setAmtOfIns(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovRpuAmount")) {
				extractScheduleInfo.setRPUAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovRenewalDate") || field.equalsIgnoreCase("BenefitRenewalDate") || field.equalsIgnoreCase("PolicyRenewalDate")) {
				extractScheduleInfo.setRenewalDate(NbaUtils.convertDateStringFromUSToISOFormat(calcProduct.getValue()));
			} else if (field.equalsIgnoreCase("CovCurrAnnualPremiumWithRatings")) {
				extractScheduleInfo.setAnnRenPremWithRatgAmt(calcProduct.getValue()); //ALS2402
			} else if (field.equalsIgnoreCase("TotalDPWBenCurrModePrem")) {
				extractScheduleInfo.setTotalRenewModalWPPremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("TotalDPWBenCurrPrem")){
				extractScheduleInfo.setTotalWPPremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("PolicyGuarAnnualPremiumWithRatings")){
				extractScheduleInfo.setAnnualGuarPremAmtWithRatg(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("SubstdModalPremAmt")){ //P2AXAL048
				extractScheduleInfo.setTotalModalTablePremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("TotBaseSubstdModalPremAmt")){ //P2AXAL048
				extractScheduleInfo.setTotBaseSubstdModalPremAmt(new Double(calcProduct.getValue()).doubleValue());
			}  
		}
		return extractScheduleInfo;
	}

	/**
	 * Update Life death benefit and lapse fields: Life.ProjectedCurrLapseDate, Life.ProjectedGuarLapseDate, Life.DeathBenefitAmt, Life.GDBValue
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	public void updateProjectedAmts(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		if (getNewNbaTXLife().isLife()) {
			Policy policy = getPolicy(tXLifeRequest);
			Life life = getLife(tXLifeRequest);
			CalculationResult calculationResult = getDocsResultForID(policy.getId());
			int prodCount;
			CalcProduct calcProduct;
			String field;
			double assumedInterestRate = 0.0;
			if (calculationResult != null) {
				prodCount = calculationResult.getCalcProductCount();
				for (int prdIdx = 0; prdIdx < prodCount; prdIdx++) { //Get the values for the current ExtractScheduleInfo
					calcProduct = calculationResult.getCalcProductAt(prdIdx);
					field = calcProduct.getType();
					if (field.equalsIgnoreCase("AssumedInterestRate")) {
						assumedInterestRate = new Double(calcProduct.getValue()).doubleValue();
					}
				}
			}
			LifeExtension lifeExtension = NbaUtils.getFirstLifeExtension(life);
			lifeExtension.setAssumedInterestRate(assumedInterestRate);
		}
	}

	/**
	 * Construct ExtractScheduleInfo objects for each Coverage for which there are values in the VPMS model calculation results. Depending on the
	 * values, an ExtractScheduleInfo may be created for each duration from zero to maturity.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	public void addExtractScheduleInfoForLife(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		CalculationResult calculationResult;
		int dur;
		Policy policy = getPolicy(tXLifeRequest);
		Coverage baseCoverage = getPrimarycoverage(policy);
		PolicyExtension policyExtension = getPolicyExtension(policy);
		calculationResult = getDocsResultForID(policy.getId());
		Life life = getLife(tXLifeRequest);
		Map calculationResultMap = new HashMap();
		if (calculationResult != null) {
			updatePolicyExtensionInExtract(policyExtension, life, calculationResult,baseCoverage);//AXAL3.7.14
		}

		int maxDur = getMaxDuration(policy);
		int issueAge = getIssueAge(tXLifeRequest);
		for (dur = 1; dur < maxDur; dur++) {
			calculationResult = getDocsResultForID(getObjectDurId(policy.getId(), dur));
			if (calculationResult != null) {
				policyExtension.addExtractScheduleInfo(createExtractScheduleInfoFromCalc(calculationResult, issueAge, dur));
				policy.setAltPremMode(getAltPremModes(policy, calculationResult));//AXAL3.7.14
				setDeathBenefitAmtForLife(calculationResult,life,dur);//ALS2402
			}
		}
		int covCount = life.getCoverageCount();
		Coverage coverage;
		CoverageExtension coverageExtension;
		for (int cov = 0; cov < covCount; cov++) {
			coverage = life.getCoverageAt(cov);
			coverageExtension = getCoverageExtension(coverage);
			maxDur = getMaxDuration(coverage);
			issueAge = getIssueAge(coverage);
			calculationResult = getDocsResultForID(coverage.getId());
			updatePremPayPeriodCoverage(calculationResult,coverageExtension);
			for (dur = 1; dur < maxDur; dur++) {
				calculationResult = getDocsResultForID(getObjectDurId(coverage.getId(), dur));
				//Find Results than match coverage id + duration
				if (calculationResult != null) {
					coverageExtension.addExtractScheduleInfo(createExtractScheduleInfoFromCalc(calculationResult, issueAge, dur));
					if (dur == 1){//ALS4668
						updateCoverageInfoForExtract(calculationResult, coverage, calculationResultMap);//ALS4168 new attribute added in the method signature
						setFlatExtraAmountsForRatings(coverage, calculationResultMap);//ALS4168
					}
				}
			}
			updateExtractScheduleInfo(coverage,coverageExtension);
			//begin ALS2402
			if (coverage.getEffDate() != null) {
				if (coverageExtension.getPayUpDate() != null) {
					coverageExtension.setPremPayPeriod(Math.abs(NbaUtils.getYears(coverage.getEffDate(), coverageExtension.getPayUpDate())));
				}
			}
			// end ALS2402
			if (coverage.getIndicatorCode() == OLI_COVIND_BASE) {
				//P2AXAL048 starts
				if (NbaUtils.isISWLProduct(policy) && coverage.getEffDate() != null && coverage.getTermDate() != null) {
					coverageExtension.setLevelPremiumPeriod(getYears(coverage.getEffDate(), coverage.getTermDate()));
					coverageExtension.setPremPayPeriod(getYears(coverage.getEffDate(), coverage.getTermDate()));//ALII1565
				}
				//P2AXAL048 ends
				ExtractScheduleInfo cesi;
				ExtractScheduleInfo pesi;
				int covExtcnt = coverageExtension.getExtractScheduleInfoCount();
				int polExtcnt = policyExtension.getExtractScheduleInfoCount();
				for (int j = 0; j < covExtcnt; j++) {
					cesi = coverageExtension.getExtractScheduleInfoAt(j);
					for (int k = 0; k < polExtcnt; k++) {
						pesi = policyExtension.getExtractScheduleInfoAt(k);
						if (cesi.getDuration() == pesi.getDuration()) {
							cesi.setCOIAmt(pesi.getCOIAmt());
							cesi.setDeathBenefitAmt(pesi.getDeathBenefitAmt());
							break;
						}
					}
				}
			}
		}
		//Create ExtractScheduleInfo for CovOptions
		CovOption covOption;
		CovOptionExtension covOptionExtension;
		for (int cov = 0; cov < covCount; cov++) {
			coverage = life.getCoverageAt(cov);
			issueAge = getIssueAge(coverage);
			int covOptionCount = coverage.getCovOptionCount();
			for (int i = 0; i < covOptionCount; i++) {
				covOption = coverage.getCovOptionAt(i);
				covOptionExtension = getCovOptionExtension(covOption);
				maxDur = getMaxDuration(covOption);
				calculationResult = getDocsResultForID(covOption.getId());
				updatePremPayPeriodBenefit(calculationResult,covOptionExtension);
				for (dur = 1; dur < maxDur; dur++) {
					calculationResult = getDocsResultForID(getObjectDurId(covOption.getId(), dur)); //Find Results than match CovOption id + duration
					if (calculationResult != null) {
						if (NbaOliConstants.OLI_OPTTYPE_ABE != covOption.getLifeCovOptTypeCode()) {//No need to create ExtractScheduleinfo For "LBR"
																								   // as no Premium is required for it.
							covOptionExtension.addExtractScheduleInfo(createExtractScheduleInfoFromCalc(calculationResult, issueAge, dur));
						}
						updateCoverageInfoForExtract(calculationResult, coverage, calculationResultMap);//AXAL3.7.14
					}
				}
			}
		}
	}

	/**
	 * @param calculationResult
	 * @param covOptionExtension
	 */
	protected void updatePremPayPeriodBenefit(CalculationResult calculationResult, CovOptionExtension covOptionExtension) {
		CalcProduct calcProduct;
		String field;
		int prodCount = calculationResult.getCalcProductCount();
		for (int prdIdx = 0; prdIdx < prodCount; prdIdx++) { //Get the values for the current ExtractScheduleInfo
			calcProduct = calculationResult.getCalcProductAt(prdIdx);
			field = calcProduct.getType();
			if (field.equalsIgnoreCase("BenefitPremiumPeriod")) {
				covOptionExtension.setPremPayPeriod(calcProduct.getValue());
			}
		}
	}
	/**
	 * @param calculationResult
	 * @param coverageExtension
	 */
	protected void updatePremPayPeriodCoverage(CalculationResult calculationResult, CoverageExtension coverageExtension) {
		CalcProduct calcProduct;
		String field;
		int prodCount = calculationResult.getCalcProductCount();
		for (int prdIdx = 0; prdIdx < prodCount; prdIdx++) { //Get the values for the current ExtractScheduleInfo
			calcProduct = calculationResult.getCalcProductAt(prdIdx);
			field = calcProduct.getType();
			if (field.equalsIgnoreCase("CovPremiumPeriod") || field.equalsIgnoreCase("RiderPremiumPeriod")) {
				coverageExtension.setPremPayPeriod(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovAnnualPremAmtWDiscount")) {
				coverageExtension.setAnnualPremAmtWDiscount(new Double(calcProduct.getValue()).doubleValue());
			}
		}
	}
	/**
	 * @param calculationResult
	 */
	 // New Method ALS2402
	private void setDeathBenefitAmtForLife(CalculationResult calculationResult, Life life, int dur) {
		CalcProduct calcProduct;
		String field;
		LifeExtension lifeExtension = NbaUtils.getFirstLifeExtension(life);
		int prodCount = calculationResult.getCalcProductCount();
		for (int prdIdx = 0; prdIdx < prodCount; prdIdx++) { //Get the values for the current ExtractScheduleInfo
			calcProduct = calculationResult.getCalcProductAt(prdIdx);
			field = calcProduct.getType();
			if (field.equalsIgnoreCase("PolicyGuarDeathBenefit")) {
				life.setDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("TotalDPWBenCurrPrem") && dur == 1) {
				lifeExtension.setTotalWaiverPremCost(new Double(calcProduct.getValue()).doubleValue());
			}
		}
	}
	/**
	 * This method will read the result from CalculationResult coming from VPMS Calulation Model and will populate LifeExtension, Coverage,
	 * CoverageExtension objects.
	 * @param calculationResult
	 * @param coverage
	 * @param life
	 */
	//ALS4168 new attribute added in the method signature
	private void updateCoverageInfoForExtract(CalculationResult calculationResult, Coverage coverage, Map calculationResultMap) {
		Iterator calculationResultIterator = calculationResult.getCalcProduct().iterator();
		String field = null;
		CoverageExtension coverageExtension = getCoverageExtension(coverage);
		while (calculationResultIterator.hasNext()) {
			CalcProduct calcProduct = (CalcProduct) calculationResultIterator.next();
			field = calcProduct.getType();
			if (field.equalsIgnoreCase("CovCurrAnnualPremiumWithRatings")) {
				coverage.setTotAnnualPremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("RiderCurrAnnualPremium")) {
				coverageExtension.setAnnualPremAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("CovModePremWTableRating")) {//ALS3576 begin
				if (OLI_COVIND_BASE == coverage.getIndicatorCode()){
					coverageExtension.setModalPremAmtwTableRatg(new Double(calcProduct.getValue()).doubleValue());
				}
			} //ALS3576 end
			calculationResultMap.put(field, calcProduct.getValue());
		}
	}

	/**
	 * The method sets SubstandardRatingExtension.FlatExtraModalPremAmt
	 * @param coverage
	 * @param calculationResultMap
	 */
	//ALS4168 new method
	protected void setFlatExtraAmountsForRatings(Coverage coverage, Map calculationResultMap) {
		//ALS4624 - Begin
		ArrayList subRatingList = coverage.getLifeParticipantAt(0).getSubstandardRating();
		int subRatingindex = 1;
		for ( int i=0 ; i < subRatingList.size(); i++) {
			SubstandardRating rating = (SubstandardRating)subRatingList.get(i); //ALS5581
			SubstandardRatingExtension subRatingExt = NbaUtils.getFirstSubstandardExtension(rating);
			//ALS4624 - End
			//begin ALS4668
			//ALS5496 Begin
			if (!subRatingExt.getProposedInd()){  
				//begin ALS5581
				String type = NbaUtils.getSubstandardRatingType(rating); 
				if (NbaConstants.SUB_STAND_TYPE_TEMP_FLAT.equals(type) || NbaConstants.SUB_STAND_TYPE_PERM_FLAT.equals(type)) {
					String calAmt = (String)calculationResultMap.get("CovCurrFlatExtraModePrem" + subRatingindex);
					if(! NbaUtils.isBlankOrNull(calAmt)){ 
						subRatingExt.setFlatExtraModalPremAmt(Double.parseDouble(calAmt));	
					}
					//end ALS4668
					subRatingindex++;
				}
				else {
					subRatingExt.setFlatExtraModalPremAmt(0);
				}
				//end ALS5581
			}//ALS5496 End
		}
		
	}
	/**
	 * This method will read the result from CalculationResult coming from VPMS Calulation Model and will populate PolicyExtension
	 * @param policyExtension
	 * @param calculationResult
	 */
	private void updatePolicyExtensionInExtract(PolicyExtension policyExtension, Life life, CalculationResult calculationResult, Coverage baseCoverage) {
		String field = null;
		LifeExtension lifeExtension = NbaUtils.getFirstLifeExtension(life);
		CoverageExtension coverageExtension = NbaUtils.getFirstCoverageExtension(baseCoverage);
		Iterator calculationResultIterator = calculationResult.getCalcProduct().iterator();
		while (calculationResultIterator.hasNext()) {
			CalcProduct calcProduct = (CalcProduct) calculationResultIterator.next();
			field = calcProduct.getType();
			if (field.equalsIgnoreCase("BaseDiscount")) {
				policyExtension.setFirstYrPremDiscountAmt(new Double(calcProduct.getValue()).doubleValue());
			} else if (field.equalsIgnoreCase("LifeTotalModePremAmt")) {
				lifeExtension.setTotalModalPremAmt(new Double(calcProduct.getValue()).doubleValue());
				updateParentTXLifeAttributes(getNbaTxLife(), lifeExtension);//ALS4492
			} else if (field.equalsIgnoreCase("InitialTermExpiryDate")) {
				coverageExtension.setLevelPremiumPeriod(getYears(baseCoverage.getEffDate(),NbaUtils.getDateFromStringInUSFormat(calcProduct.getValue())));//ALS4575
			}
		}
	}

	/**
	 * Construct ExtractIllusSummaryInfo objects for each Coverage for which there are values in the VPMS model calculation results.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	public void addExtractIllusSummaryInfoForLife(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		Life life = getLife(tXLifeRequest);
		int covCount = life.getCoverageCount();
		Coverage coverage;
		CalculationResult calculationResult;
		CalcProduct calcProduct;
		ExtractIllusSummaryInfo e5 = null;
		ExtractIllusSummaryInfo e10 = null;
		ExtractIllusSummaryInfo e20 = null;
		String field;
		int prodCount;
		int prdIdx;
		for (int i = 0; i < covCount; i++) {
			coverage = life.getCoverageAt(i);
			CoverageExtension coverageExtension = getCoverageExtension(coverage);
			calculationResult = getDocsResultForID(coverage.getId());
			e5 = new ExtractIllusSummaryInfo();
			e10 = new ExtractIllusSummaryInfo();
			e20 = new ExtractIllusSummaryInfo();
			if (calculationResult != null) {
				prodCount = calculationResult.getCalcProductCount();
				for (prdIdx = 0; prdIdx < prodCount; prdIdx++) {
					calcProduct = calculationResult.getCalcProductAt(prdIdx);
					field = calcProduct.getType();
					if (field.equalsIgnoreCase("CovGuarNetPmtCostIndex10")) {
						e10.setDuration(10);
						e10.setGuarNetPayment(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovGuarNetPmtCostIndex20")) {
						e20.setDuration(20);
						e20.setGuarNetPayment(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovGuarSurrenderCostIndex10")) {
						e10.setDuration(10);
						e10.setGuarSurrValue(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovGuarSurrenderCostIndex20")) {
						e20.setDuration(20);
						e20.setGuarSurrValue(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovCurrNetPmtCostIndex5")) {
						e5.setDuration(5);
						e5.setCurrentNetPayment(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovCurrNetPmtCostIndex10")) {
						e10.setDuration(10);
						e10.setCurrentNetPayment(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovCurrNetPmtCostIndex20")) {
						e20.setDuration(20);
						e20.setCurrentNetPayment(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovCurrSurrenderCostIndex5")) {
						e5.setDuration(5);
						e5.setCurrentSurrValue(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovCurrSurrenderCostIndex10")) {
						e10.setDuration(10);
						e10.setCurrentSurrValue(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovCurrSurrenderCostIndex20")) {
						e20.setDuration(20);
						e20.setCurrentSurrValue(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovInitialTermPremiumPeriod")) {
						coverageExtension.setLevelPremiumPeriod(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovInitialTermExpiryDate")) {
						coverageExtension.setNextRenewalDate(NbaUtils.convertDateStringFromUSToISOFormat(calcProduct.getValue()));
					}
				}
			}
			if (e5.hasDuration()) {
				coverageExtension.addExtractIllusSummaryInfo(e5);
			}
			if (e10.hasDuration()) {
				coverageExtension.addExtractIllusSummaryInfo(e10);
			}
			if (e20.hasDuration()) {
				coverageExtension.addExtractIllusSummaryInfo(e20);
			}
		}
	}

	/**
	 * Calculate the payment amounts for and create AltPremMode objects for each of the Standard Modes: Monthly, Quarterly, SemiAnnual and Annual.
	 * @param policy
	 * @param calculationResult
	 * @return list of AltPremModes objects.
	 * @throws NbaBaseException
	 */
	public ArrayList getAltPremModes(Policy policy, CalculationResult calculationResult) {
		if (altPremModes == null) {
			altPremModes = new ArrayList();
			CalcProduct calcProduct;
			AltPremMode altPremMode;
			Iterator calculationResultIterator = calculationResult.getCalcProduct().iterator();
			String field = null;
			while (calculationResultIterator.hasNext()) {
				calcProduct = (CalcProduct) calculationResultIterator.next();
				field = calcProduct.getType();
				altPremMode = new AltPremMode();
				if (field.equalsIgnoreCase("PolicyCurrModePremMonthly")) {
					altPremMode.setPaymentAmt(new Double(calcProduct.getValue()).doubleValue());
					altPremMode.setPaymentMode(NbaOliConstants.OLI_PAYMODE_MNTHLY);
					altPremMode.setPaymentMethod(policy.getPaymentMethod());
					altPremModes.add(altPremMode);
				}
				if (field.equalsIgnoreCase("PolicyCurrModePremQuaterly")) {
					altPremMode.setPaymentAmt(new Double(calcProduct.getValue()).doubleValue());
					altPremMode.setPaymentMode(NbaOliConstants.OLI_PAYMODE_QUARTLY);
					altPremMode.setPaymentMethod(policy.getPaymentMethod());
					altPremModes.add(altPremMode);
				}
				if (field.equalsIgnoreCase("PolicyCurrModePremSemiAnnual")) {
					altPremMode.setPaymentAmt(new Double(calcProduct.getValue()).doubleValue());
					altPremMode.setPaymentMode(NbaOliConstants.OLI_PAYMODE_BIANNUAL);
					altPremMode.setPaymentMethod(policy.getPaymentMethod());
					altPremModes.add(altPremMode);
				}
				if (field.equalsIgnoreCase("PolicyCurrModePremAnnual")) {
					altPremMode.setPaymentAmt(new Double(calcProduct.getValue()).doubleValue());
					altPremMode.setPaymentMode(NbaOliConstants.OLI_PAYMODE_ANNUAL);
					altPremMode.setPaymentMethod(policy.getPaymentMethod());
					altPremModes.add(altPremMode);
				}

			}
		}
		return altPremModes;
	}
	/**
	 * Add a FormInstance containing an Attachment which identifies the extract type
	 * @param type - the value obtained from the VPMS model to be used as the Attachment.AttachmentType
	 * @param tXLifeRequest - the TXLifeRequest to which the FormInstance is to be added
	 * @param basicType - a value indicating whether the attachment represents a Text or Image 
	 * @param imageSubmissionType - for Attachments for Sources, this contains a value of "8" (OLI_APPSUBMITTYPE_ATTACHED).
	 * @param reprintInd - a value indicating whether the extract type represents a reprint
	 * @return Attachment - the Attachment object that was added
	 */
	public Attachment addAttachmentToTXLifeRequest(long type, TXLifeRequest tXLifeRequest, long basicType, long imageSubmissionType, boolean reprintInd) {
		Attachment attachment = new Attachment();
		attachment.setUserCode(getNbaUserVO().getUserID());
		attachment.setAttachmentBasicType(basicType);
		attachment.setAttachmentType(type);
		FormInstance formInstance = new FormInstance();
		if (imageSubmissionType != -1) {
			formInstance.setImageSubmissionType(imageSubmissionType);
		}
		AttachmentExtension attachmentExtension = NbaUtils.getFirstAttachmentExtension(attachment);
		if (attachmentExtension == null) {
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_ATTACHMENT);
			attachment.addOLifEExtension(olifeExt);
			attachmentExtension = olifeExt.getAttachmentExtension();
		}
		attachmentExtension.setReprintInd(reprintInd);
		//new code added
		addAttachmentForDeliveryInstruction(tXLifeRequest);
		formInstance.addAttachment(attachment);
		
		tXLifeRequest.getOLifE().addFormInstance(formInstance);
		
		return attachment;
	}

	/**
	 * 
	 */
	protected void addAttachmentForDeliveryInstruction(TXLifeRequest tXLifeRequest) {
		//ALS4542 Begin
		List attachmentList = new ArrayList();
		Holding holding = getNbaTxLife().getPrimaryHolding();
		for (int i=0; i<holding.getAttachmentCount();i++)
		{
			Attachment  attach = holding.getAttachmentAt(i);
			if(OLI_ATTACH_INSTRUCTION ==attach.getAttachmentType()||
					OLI_ATTACH_SPEC_HANDL_INST == attach.getAttachmentType()){
				Attachment  newAttach = attach.clone(false); //ALS4954
				newAttach.setId(null);	//ALS4954
				attachmentList.add(newAttach);  //ALS4954
			}
		}
		FormInstance formInstance = new FormInstance();
		formInstance.setAttachment((ArrayList)attachmentList);
		
		tXLifeRequest.getOLifE().addFormInstance(formInstance);
		//ALS4542 End
	}
	
	/**
	 * @param transactionDst
	 * @return
	 */
	protected NbaRetrieveCommentsRequest constructRetrieveCommentsRequest(NbaDst work) {
		NbaRetrieveCommentsRequest commentsReq = new NbaRetrieveCommentsRequest();
		commentsReq.setNbaDst(work);
		commentsReq.setRetrieveChildren(false);
		return commentsReq;
	}
	protected boolean isValidAttachmentType(long reportValue) {
		boolean isValid = false;
		for (int i = 0; i < NbaConstants.CONTRACT_PRINT_EXTRACT_TYPES.length; i++) {
			if (NbaConstants.CONTRACT_PRINT_EXTRACT_TYPES[i] == reportValue)
				return true;
		}
		return isValid;
	}

	/**
	 * Update the new NbaTXLife request object with Policy Product information. 
	 */
	/*protected void initializePolicyProductInfo(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		AccelProduct product;
		NbaProductAccessFacadeBean nbaProductAccessFacade = new NbaProductAccessFacadeBean();
		product = nbaProductAccessFacade.doProductInquiry(getNewNbaTXLife());
		OLifE oLifE = tXLifeRequest.getOLifE();		
		if (getNewNbaTXLife().isLife()) {
			Life life = getLife(tXLifeRequest);
			String plan;
			int covCount = life.getCoverageCount();
			Coverage cov = null;
			CoverageExtension covExtn = null;
			for (int i = 0; i < covCount; i++) {
				cov = life.getCoverageAt(i);
				plan = cov.getProductCode();				
				oLifE.addPolicyProduct(product.getPolicyProduct(plan));
				covExtn = NbaUtils.getFirstCoverageExtension(cov);
				if (covExtn == null) {
					OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_COVERAGE);
					cov.addOLifEExtension(olifeExt);
					covExtn = olifeExt.getCoverageExtension();
				}
				if (product.getCoverageProduct(plan) != null && NbaUtils.getCovOption(cov, ROP) != null) {
					List covOptionProducts = product.getCoverageProduct(plan).getCovOptionProduct();
					for (int k = 0; k < covOptionProducts.size(); k++) {
						CovOptionProduct covOptionProduct = (CovOptionProduct) covOptionProducts.get(k);
						if (covOptionProduct != null && ROP.equals(covOptionProduct.getProductCode())) {
							//NBA237 modified the reference from NBAUtils to Accelproduct
							CovOptionProductExtension covOptionProductExtn = AccelProduct.getFirstCovOptionProductExtension(covOptionProduct);
							covExtn.setMaturityDuration(covOptionProductExtn.getMaturityDuration());
						}
					}
				}
			}
		} else if (getNewNbaTXLife().isAnnuity()) {
			oLifE.addPolicyProduct(product.getPrimaryPolicyProduct());
		}
		PolicyProduct policyProduct;
		for (int i = 0; i < oLifE.getPolicyProductCount(); i++) {
			policyProduct = oLifE.getPolicyProductAt(i);
			policyProduct.setInvestProductInfoGhost(new ArrayList());
			policyProduct.setLifeProductOrAnnuityProductGhost(new LifeProductOrAnnuityProduct());
			policyProduct.setOwnershipGhost(new ArrayList());
			policyProduct.setBusinessProcessAllowedGhost(new ArrayList());
			policyProduct.setJurisdictionApprovalGhost(new ArrayList());
		}
	}*/
	public void setAdditionalRiskYearsInfoForCoverage(TXLifeRequest tXLifeRequest){
		Life life = getLife(tXLifeRequest);
		Iterator coverageItr = life.getCoverage().iterator();
		while(coverageItr.hasNext()){
			Coverage coverage = (Coverage)coverageItr.next();
			Iterator lifePartItr = coverage.getLifeParticipant().iterator();
			while(lifePartItr.hasNext()){
				Iterator subsRatingItr = ((LifeParticipant)lifePartItr.next()).getSubstandardRating().iterator();
				while(subsRatingItr.hasNext()){
					SubstandardRating substandardRating = (SubstandardRating)subsRatingItr.next();
					setAdditionalRiskYearsInfo(substandardRating, coverage);
				}
			}
		}
		
	}
	public void setAdditionalRiskYearsInfo(SubstandardRating substandardRating, Coverage coverage) {
		SubstandardRatingExtension substanRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
		CoverageExtension coverageExtension = NbaUtils.getFirstCoverageExtension(coverage);
		
		if (substandardRating.getTempFlatExtraAmt() > 0) {
			substanRatingExt.setAdditionalRiskYears(substanRatingExt.getDuration() < coverageExtension.getLevelPremiumPeriod() ? substanRatingExt
					.getDuration() : coverageExtension.getLevelPremiumPeriod());
		} else if (substanRatingExt.getPermFlatExtraAmt() > 0 || substandardRating.getPermTableRating() > 0) {
			if (coverageExtension.getPayUpDate() != null && substanRatingExt.getEffDate() != null) {
				int absYears = Math.abs(NbaUtils.getYears(substanRatingExt.getEffDate(), coverageExtension.getPayUpDate()));
				substanRatingExt.setAdditionalRiskYears(absYears < coverageExtension.getLevelPremiumPeriod() ? absYears : coverageExtension
						.getLevelPremiumPeriod());
			}
		}
	}
	
	/**
	 * @param coverageExtension
	 */
	protected void updateExtractScheduleInfo(Coverage coverage,CoverageExtension coverageExtension) {
		List additionalRiskYrsList = getAdditionalRiskYrsList(coverage);
		List modifiedAddRiskYrsList = getModifiedAdditionalRiskYrsList(additionalRiskYrsList);
		Iterator extractSchduleInfoItr = coverageExtension.getExtractScheduleInfo().iterator();
		int currentAddRiskYrs = 0;
		int modifiedAddRiskYrsCounter = 0;
		int page3PointerValue = 0;
		if(modifiedAddRiskYrsList != null && !modifiedAddRiskYrsList.isEmpty()){
			currentAddRiskYrs = ((Integer)modifiedAddRiskYrsList.get(modifiedAddRiskYrsCounter)).intValue();
			while(extractSchduleInfoItr.hasNext()){
				ExtractScheduleInfo extractScheduleInfo = (ExtractScheduleInfo)extractSchduleInfoItr.next();
				extractScheduleInfo.setRiskPremRemainingYrs(currentAddRiskYrs);
				if(extractScheduleInfo.getDuration() == 1){
					page3PointerValue = extractScheduleInfo.getRiskPremRemainingYrs() + extractScheduleInfo.getDuration();
					extractScheduleInfo.setPage3Pointer(page3PointerValue);
				} else{
					if(extractScheduleInfo.getDuration() == page3PointerValue && page3PointerValue <= coverageExtension.getLevelPremiumPeriod()){
						page3PointerValue = extractScheduleInfo.getRiskPremRemainingYrs() + extractScheduleInfo.getDuration();
						if(page3PointerValue <= coverageExtension.getLevelPremiumPeriod()){
							extractScheduleInfo.setPage3Pointer(page3PointerValue);
						}
					}
				}
				if(currentAddRiskYrs>0){
					currentAddRiskYrs--;
				}
				if(currentAddRiskYrs == 0){
					modifiedAddRiskYrsCounter ++;
					if(modifiedAddRiskYrsCounter < modifiedAddRiskYrsList.size()){
						currentAddRiskYrs = ((Integer)modifiedAddRiskYrsList.get(modifiedAddRiskYrsCounter)).intValue();
					}
				}
			}
		}
	}
	/**
	 * @param coverage
	 * @return
	 */
	protected List getAdditionalRiskYrsList(Coverage coverage) {
		Iterator substandardRatingItr = coverage.getLifeParticipantAt(0).getSubstandardRating().iterator();
		List additionalRiskYrsList = new ArrayList();
		while(substandardRatingItr.hasNext()){
			SubstandardRating substandardRating = (SubstandardRating)substandardRatingItr.next();
			SubstandardRatingExtension substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
			additionalRiskYrsList.add(new Integer(substandardRatingExt.getAdditionalRiskYears()));
		}
		Collections.sort(additionalRiskYrsList);
		return additionalRiskYrsList;
	}
	/**
	 * @param additionalRiskYrsList
	 */
	protected List getModifiedAdditionalRiskYrsList(List additionalRiskYrsList) {
		List modifiedAdditionalRiskYrsList = new ArrayList();
		int addRiskYrsCount = additionalRiskYrsList.size();
		int modifiedAdditionalRiskYrsListCounter = 0;
		for(int i=0;i<addRiskYrsCount;i++){
			if(i==0){
				modifiedAdditionalRiskYrsList.add(additionalRiskYrsList.get(i));
			} else{
				modifiedAdditionalRiskYrsListCounter = i;
				modifiedAdditionalRiskYrsList.add(new Integer(((Integer) additionalRiskYrsList.get(i)).intValue()
						- ((Integer) additionalRiskYrsList.get(--modifiedAdditionalRiskYrsListCounter)).intValue()));
			}
		}
		return modifiedAdditionalRiskYrsList;
	}
	protected void setAUDLetters(TXLifeRequest tXLifeRequest) throws NbaBaseException{ 
		NbaVpmsAdaptor audLettersVpmsAdaptor;
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
		if(applicationInfoExtension != null && applicationInfoExtension.getUnderwritingResult().size() > 0){
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
			oinkData.setContractSource(getNbaTxLife(),getCaseDst().getNbaLob());
			// CR1455063 begin
			int uwResultCount = applicationInfoExtension.getUnderwritingResult().size();			
			for (int j = 0; j < uwResultCount; j++) {
				UnderwritingResult underwritingResult = (UnderwritingResult) applicationInfoExtension.getUnderwritingResult().get(j);				
				String firstUndWrtResultReason = Long.toString(underwritingResult.getUnderwritingResultReason());
				// CR1455063 end
				audLettersVpmsAdaptor = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.CORRESPONDENCE);
				audLettersVpmsAdaptor.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GETLETTERS);
				Map deOinkMap = new HashMap();
				// CR1455063 begin
				if (underwritingResult.getRelatedObjectID() != null && underwritingResult.getRelatedObjectID().equalsIgnoreCase(getNbaTxLife().getPartyId(NbaOliConstants.OLI_REL_JOINTINSURED))) {
					deOinkMap.put("A_FirstUnderwritingResultReason_JNT", firstUndWrtResultReason);
					deOinkMap.put("A_FirstUnderwritingResultReason_PINS", "");
				} else {
					deOinkMap.put("A_FirstUnderwritingResultReason_PINS", firstUndWrtResultReason);
					deOinkMap.put("A_FirstUnderwritingResultReason_JNT", "");
				}
				// CR1455063 end
				deOinkMap.put("A_hasNewStatusDST", NbaConstants.TRUE_STR);
				deOinkMap.put("A_GenAUDLetterForPrint", NbaConstants.TRUE_STR);//ALS4495
				audLettersVpmsAdaptor.setSkipAttributesMap(deOinkMap);
				VpmsComputeResult computeResult = null;
				try{
					computeResult = audLettersVpmsAdaptor.getResults();
				 } catch (java.rmi.RemoteException re) {
			        throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS, re);
			    //begin ALS5009
			    } finally {
			    	 try {
		                if (audLettersVpmsAdaptor != null) {
		                	audLettersVpmsAdaptor.remove();
		                }
		            } catch (Throwable th) {
		                getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
		            }
			    }
			    //end ALS5009
				setAUDLettersForExtract(tXLifeRequest, computeResult, j); // CR1455063				
			}
		}
	}
	protected void setAUDLettersForExtract(TXLifeRequest tXLifeRequest,VpmsComputeResult computeResult, int index){ // CR1455063 method signature changed
		NbaStringTokenizer audLettersToken = new NbaStringTokenizer(computeResult.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[1]);
		Policy policy = getPolicy(tXLifeRequest); 
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo()); 
		String audLetterInternalToken = null;
		UnderwritingResultExtension undResultExt = null;
		UnderwritingResult undResult = appInfoExt.getUnderwritingResultAt(index); // CR1455063
		if(appInfoExt.getUnderwritingResult().size() > 0){ 
			undResultExt = NbaUtils.getFirstUnderwritingResultExtension(undResult);
		} 
		if(undResultExt == null){
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_UNDERWRITINGRESULT);
			undResultExt = oLifEExtension.getUnderwritingResultExtension();
			undResult.addOLifEExtension(oLifEExtension);
			
		}
		while (audLettersToken.hasMoreTokens()) {
			audLetterInternalToken = audLettersToken.nextToken();
			if(!NbaUtils.isBlankOrNull(audLetterInternalToken)){
				StringTokenizer audLetterInsOwn = new StringTokenizer(audLetterInternalToken, NbaVpmsAdaptor.VPMS_DELIMITER[0]); // NBA021
				String relationRoleCode = audLetterInsOwn.hasMoreTokens() ? audLetterInsOwn.nextToken() : null; //APSL716 //ALII1215 also did similar code in branch
				String audLetter = audLetterInsOwn.hasMoreTokens() ? audLetterInsOwn.nextToken() : null;//APSL716 //ALII1215 also did similar code in branch
					if (Long.toString(NbaOliConstants.OLI_REL_INSURED).equals(relationRoleCode) || Long.toString(NbaOliConstants.OLI_REL_JOINTINSURED).equals(relationRoleCode)) { // CR1455063
						undResultExt.setInsuredAUDLetterName(audLetter);
					}
					else if (Long.toString(NbaOliConstants.OLI_REL_OWNER).equals(relationRoleCode)){
						undResultExt.setOwnerAUDLetterName(audLetter);
					}
				}
			}
		}
	
	//ALPC195 - New method
	protected void setAUDLettersText(TXLifeRequest tXLifeRequest) throws NbaBaseException{
		// retrieve all the UW ReasultReason and get the Deswcription for that from NbaTable
		NbaTableAccessor nta = new NbaTableAccessor();
		Policy policy = getPolicy(tXLifeRequest);
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo());
		ArrayList undResultList = appInfoExt.getUnderwritingResult();
		int uwResultCount = undResultList.size();
		UnderwritingResultExtension uwResultExt = null; 
		for (int j = 0; j < uwResultCount; j++) {
			UnderwritingResult uwResult = (UnderwritingResult) undResultList.get(j);
			//get the reason and reasontype and execute the query to get AUD letter text.
			long uwReasonTc = uwResult.getUnderwritingResultReason();
			uwResultExt = NbaUtils.getFirstUnderwritingResultExtension(uwResult);
			if ( uwResultExt != null  && uwResultExt.getUnderwritingReasonType() == Long.valueOf(NbaOliConstants.OLI_EXT_FINAL_DISP).longValue()){
				HashMap caseData = new HashMap();
				caseData.put("backendSystem", getCaseDst().getNbaLob().getBackendSystem());
				caseData.put("plan", getCaseDst().getNbaLob().getPlan());
				caseData.put("company", getCaseDst().getNbaLob().getCompany());
				NbaTableData data[] = nta.getAUDLetterText(caseData, NbaOliConstants.OLI_EXT_FINAL_DISP, String.valueOf(uwReasonTc));
				if (data.length > 0) {
					String audLetterText = ((NbaReasonsData) data[0]).getAudLettersText();
					//QC#5822 APSL728 SR540843 begin
					if (audLetterText != null && audLetterText.indexOf(AUDLETTER_VAR) != -1) {
						audLetterText = uwResult.getSupplementalText() != null ? audLetterText.replaceFirst(AUDLETTER_VAR, uwResult
								.getSupplementalText()) : audLetterText.replaceFirst(AUDLETTER_VAR, "");
					}
					//QC#5822 APSL728 SR540843 end
					uwResult.setDescription(audLetterText);
				}
			}
		}
	}

	
	/**
	 * Calculate the number of full years between two dates.
	 * @param lowDate - the low date
	 * @param highDate - the high date
	 * @return the number of full years between two dates
	 */
	//ALS4575 new method
	public int getYears(Date lowDate, Date highDate) {
		int diff = 0;
		if (lowDate != null && highDate != null) {
			long temp = (highDate.getTime() - lowDate.getTime()) / (1000*60*60*24); //SPR2226
			diff =  Math.round(((float)temp / 365));
}
		return diff;
	}
	//ALS4492 New Method	
	protected void updateParentTXLifeAttributes(NbaTXLife parentTxLife, LifeExtension lifeExtension) {
		Life parentLife = parentTxLife.getLife();
		LifeExtension parentLifeExtension = NbaUtils.getFirstLifeExtension(parentLife);
		if (lifeExtension != null && parentLifeExtension != null) {
			parentLifeExtension.setTotalModalPremAmt(lifeExtension.getTotalModalPremAmt());	
			parentLifeExtension.setActionUpdate();
		}
	}	
	//ALS4567 overridden the base method in order to include all the remove objects.
	protected void removeExistingObjects() {
		//Begin APSL209
		List attachments = getNewNbaTXLife().getPrimaryHolding().getAttachment();
		Iterator itr = attachments.iterator();
		for (int i = 0; i < attachments.size(); i++) {
			Attachment attachment = (Attachment) attachments.get(i);
			if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE) {
				attachment.setAttachmentDataGhost(new AttachmentData());
			}
		}
		//End APSL209
	}
	
	//ALS4611 - method overridnen to set the ExamDate in XML500
	public void initializeNewNbaTXLife() throws NbaBaseException {
		super.initializeNewNbaTXLife();
		updateExamDate(); //ALS4611
	}
	
	/*
	 * This method sets the Exam date to XML500 if the Paramedical requirement Type(10) is received (7) 
	 * and Risk.MedicalExam.ExamDate is not populated. 
	 */
	//ALS4611 - new method
	private void updateExamDate(){
		NbaTXLife txLife = getNewNbaTXLife();
		NbaParty primaryParty = txLife.getPrimaryParty();
		Risk risk = primaryParty.getRisk(); 
		if (risk.getMedicalExam()!=null && risk.getMedicalExam().size()>0 && risk.getMedicalExamAt(0).hasExamDate()){
			return;
		}
		// Begin APSL3649
		Date receivedDate=null;
		boolean isDateUpdate = false;
		List reqList = null;
		RequirementInfo reqInfo = null;
		List dateList = new ArrayList();
		reqList = txLife.getRequirementInfoList(primaryParty, NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED);
		if(reqList != null && !reqList.isEmpty()) {
		    Iterator itr =  reqList.iterator();		    
		    while (itr.hasNext()) {
		        reqInfo = (RequirementInfo)itr.next();
		        if (reqInfo !=null){
		            if (reqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED){
		                isDateUpdate = true;
		                //RequirementInfoExtension ReqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		                dateList.add(reqInfo);
		            }
		        }
		    }
		}	
		if (isDateUpdate){
		    SortingHelper.sortData(dateList, false, "receivedDate");
		    RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(((RequirementInfo)dateList.get(0)));
		    if(reqInfoExt != null) {
		        receivedDate = reqInfoExt.getParamedSignedDate(); 
		    }		    
		 // end APSL3649
  
		    	if (risk.getMedicalExamCount()>0){
					risk.getMedicalExamAt(0).setExamDate(receivedDate);
				}else{
					MedicalExam medExm = new MedicalExam();
					medExm.setExamDate(receivedDate);
					risk.addMedicalExam(medExm);
				}
		    	
		 			
		}
	}
	
	/**
	 * Update the contract print backend calculations
	 * @return The updated NbTxLifeRequest
	 * @throws NbaBaseException
	 */
	//P2AXAL029 New Method, ALII1215 refactored
	protected void updateContractPrintBackendCalculations(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		NbaTXLife aNbaTXLife = new NbaTXLife(createNbaTXRequestVO());
		UserAuthRequestAndTXLifeRequest userAuthRequestAndTXLifeRequest = aNbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest();
		userAuthRequestAndTXLifeRequest.setTXLifeRequest(new ArrayList());
		userAuthRequestAndTXLifeRequest.addTXLifeRequest(tXLifeRequest);
		NbaBackendContractCalculator backendL70Calcs = NbaBackendContractCalculatorFactory.getCalculator(getNbaTxLife().getBackendSystem());
		backendL70Calcs.setNbaUserVO(getNbaUserVO());
		NbaTXLife calcData = backendL70Calcs.calculate(NbaContractCalculationsConstants.CALC_TYPE_PRINT_CALC, aNbaTXLife);
		AxaPrintCalculationUtil calcUtil = new AxaPrintCalculationUtil(aNbaTXLife, calcData, NbaContractCalculationsConstants.CALC_TYPE_PRINT_CALC);
		calcUtil.performCalculationMerge();
	}
}
