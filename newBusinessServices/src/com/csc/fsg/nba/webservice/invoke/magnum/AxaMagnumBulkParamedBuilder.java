package com.csc.fsg.nba.webservice.invoke.magnum;

import static com.csc.fsg.nba.foundation.AxaMagnumUtils.REF_DATA_VALUE;
import static com.csc.fsg.nba.foundation.AxaMagnumUtils.SIMPLE_VALUE;
import static com.csc.fsg.nba.foundation.AxaMagnumUtils.constructAttribute;
import static com.csc.fsg.nba.foundation.AxaMagnumUtils.getMagnumTranslation;
import static com.csc.fsg.nba.foundation.AxaMagnumUtils.getOliBoolean;
import static com.csc.fsg.nba.foundation.AxaMagnumUtils.removeAttribute;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.Height2;
import com.csc.fsg.nba.vo.txlife.LabTestResult;
import com.csc.fsg.nba.vo.txlife.LabTesting;
import com.csc.fsg.nba.vo.txlife.MedicalExam;
import com.csc.fsg.nba.vo.txlife.MedicalExamExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.QualitativeResult;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.RiskExtension;
import com.csc.fsg.nba.vo.txlife.Weight2;
import com.swissre.magnum.client.BulkSubmitObject;

//NBLXA-2402(NBLXA-2534)
public class AxaMagnumBulkParamedBuilder implements AxaMagnumBulkBuildable, NbaOliConstants {

	@Override
	public void constructReqData(RequirementInfo aReqInfo, BulkSubmitObject bulkSubmitObj, int partyIndex) throws NbaBaseException {
		try {
			if (!NbaUtils.isBlankOrNull(aReqInfo.getAttachment())) {
				Attachment attachment = NbaUtils.getAttachmentsByType(aReqInfo, OLI_ATTACH_REQUIRERESULTS);
				if (!NbaUtils.isBlankOrNull(attachment)) {
					NbaTXLife aTXLife = new NbaTXLife(attachment.getAttachmentData().getPCDATA());
					if (!NbaUtils.isBlankOrNull(aTXLife)) {
						Party insParty = aTXLife.getPrimaryParty().getParty();
						if (!NbaUtils.isBlankOrNull(insParty)) {
							constructReqData(bulkSubmitObj, partyIndex, insParty.getRisk());
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new NbaBaseException(ex);
		}
	}

	protected void constructReqData(BulkSubmitObject bulkSubmitObj, int partyIndex, Risk risk) throws NbaBaseException {
		String reqName = getMagnumTranslation(NbaTableConstants.NBA_REQUIREMENTS, String.valueOf(OLI_REQCODE_MEDEXAMPARAMED));
		if (!NbaUtils.isBlankOrNull(risk)) {
			/* NBLXA-2402(NBLXA-2643) code commented
			// Tobacco Use
			if (risk.hasTobaccoInd()) {
				if (risk.getTobaccoInd()) {
					// Current Use
					constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".TobaccoUse",
							getMagnumTranslation(NbaTableConstants.MGNM_TOBACCO_USE, String.valueOf(MGNM_TOBACCO_USE_CURRENT)), REF_DATA_VALUE);
				}
			} else {
				// Other
				constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".TobaccoUse",
						getMagnumTranslation(NbaTableConstants.MGNM_TOBACCO_USE, String.valueOf(MGNM_TOBACCO_USE_OTHER)), REF_DATA_VALUE);
			}
			*/
			if (!NbaUtils.isBlankOrNull(risk.getMedicalExam())) {
				constructReqData(bulkSubmitObj, partyIndex, risk.getMedicalExamAt(0));
			}

			RiskExtension riskExtn = NbaUtils.getFirstRiskExtension(risk);
			if (!NbaUtils.isBlankOrNull(riskExtn)) {
				constructReqData(bulkSubmitObj, partyIndex, riskExtn.getLabTesting());
			}
		}
	}

	protected void constructReqData(BulkSubmitObject bulkSubmitObj, int partyIndex, MedicalExam medicalExam) throws NbaBaseException {
		String reqName = getMagnumTranslation(NbaTableConstants.NBA_REQUIREMENTS, String.valueOf(OLI_REQCODE_MEDEXAMPARAMED));
		GregorianCalendar gc = new GregorianCalendar();
		// Exam Date, Requirement Date
		if (!NbaUtils.isBlankOrNull(medicalExam.getExamDate())) {
			gc.setTime(medicalExam.getExamDate());
			try {
				removeAttribute(bulkSubmitObj, "case.life[" + partyIndex + "].Requirements." + reqName + ".RequirementDate");
				constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "].Requirements." + reqName + ".RequirementDate", DatatypeFactory
						.newInstance().newXMLGregorianCalendar(gc), SIMPLE_VALUE);
				constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".ExamDate", DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(gc), SIMPLE_VALUE);
			} catch (DatatypeConfigurationException ex) {
				ex.printStackTrace();
			}
		}
		// Is Pulse Irregular
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".IsPulseIrregular",
				getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(getOliBoolean(medicalExam.getPulseIrregularInd()))),
				REF_DATA_VALUE);
		// Menses
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".Menses",
				getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(getOliBoolean(medicalExam.getMenstruation()))), REF_DATA_VALUE);
		// Systolic
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".ParamedBloodPressure[0].Systolic",
				Long.valueOf(medicalExam.getFirstSystolicBPReading()), SIMPLE_VALUE);
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".ParamedBloodPressure[1].Systolic",
				Long.valueOf(medicalExam.getSecondSystolicBPReading()), SIMPLE_VALUE);
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".ParamedBloodPressure[2].Systolic",
				Long.valueOf(medicalExam.getThirdSystolicBPReading()), SIMPLE_VALUE);
		// Diastolic
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".ParamedBloodPressure[0].Diastolic",
				Long.valueOf(medicalExam.getFirstDiastolicBPReading()), SIMPLE_VALUE);
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".ParamedBloodPressure[1].Diastolic",
				Long.valueOf(medicalExam.getSecondDiastolicBPReading()), SIMPLE_VALUE);
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".ParamedBloodPressure[2].Diastolic",
				Long.valueOf(medicalExam.getThirdDiastolicBPReading()), SIMPLE_VALUE);
		// Pulse Reading
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".ParamedPulse[0].PulseReading",
				Long.valueOf(medicalExam.getFirstPulseReading()), SIMPLE_VALUE);
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".ParamedPulse[1].PulseReading",
				Long.valueOf(medicalExam.getSecondPulseReading()), SIMPLE_VALUE);

		MedicalExamExtension medicalExamExtn = NbaUtils.getFirstMedicalExamExtension(medicalExam);
		if (!NbaUtils.isBlankOrNull(medicalExamExtn)) {
			// Hours Since Last Ate
			if (!NbaUtils.isBlankOrNull(medicalExamExtn.getHoursSinceLastAte())) {
				constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".HoursSinceLastAte",
						Long.valueOf(medicalExamExtn.getHoursSinceLastAte()), SIMPLE_VALUE);
			}
			// Urine Temperature
			if (!NbaUtils.isBlankOrNull(medicalExamExtn.getUrineTemperature())) {
				constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".UrineTemperature",
						String.valueOf(medicalExamExtn.getUrineTemperature().getMeasureValue()), SIMPLE_VALUE);
			}
			// Is Height Included
			constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".IsHeightIncluded",
					getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(medicalExamExtn.getHeightMeasuredInd())), REF_DATA_VALUE);
			// Is Weight Included
			constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".IsWeightIncluded",
					getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(medicalExamExtn.getWeightMeasuredInd())), REF_DATA_VALUE);
			/* NBLXA-2402(NBLXA-2643) code commented
			// Months Since Last Tobacco Use
			if (!NbaUtils.isBlankOrNull(medicalExamExtn.getLastTobaccoMonths())) {
				constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".MonthsSinceLastTobaccoUse",
						Long.valueOf(medicalExamExtn.getLastTobaccoMonths()), SIMPLE_VALUE);
				// Tobacco Use
				if (NbaUtils.isBlankOrNull(getAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".TobaccoUse"))) {
					if (medicalExamExtn.getLastTobaccoMonths() == 9999) {
						// Never Used
						constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".TobaccoUse",
								getMagnumTranslation(NbaTableConstants.MGNM_TOBACCO_USE, String.valueOf(MGNM_TOBACCO_USE_NEVER)), REF_DATA_VALUE);
					} else {
						// Prior Use
						constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".TobaccoUse",
								getMagnumTranslation(NbaTableConstants.MGNM_TOBACCO_USE, String.valueOf(MGNM_TOBACCO_USE_PRIOR)), REF_DATA_VALUE);
					}
				}
			}
			*/
			// Current Drug Use
			constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".CurrentDrugUse",
					getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(getOliBoolean(medicalExamExtn.getCurrentDrugUseInd()))),
					REF_DATA_VALUE);
			// Current Drugs
			constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".CurrentDrugs",
					String.valueOf(medicalExamExtn.getCurrentDrugUseDesc()), SIMPLE_VALUE);
		}

		constructReqData(bulkSubmitObj, partyIndex, medicalExamExtn.getHeight2());
		constructReqData(bulkSubmitObj, partyIndex, medicalExamExtn.getWeight2());
	}

	protected void constructReqData(BulkSubmitObject bulkSubmitObj, int partyIndex, Height2 height2) throws NbaBaseException {
		String reqName = getMagnumTranslation(NbaTableConstants.NBA_REQUIREMENTS, String.valueOf(OLI_REQCODE_MEDEXAMPARAMED));
		if (!NbaUtils.isBlankOrNull(height2)) {
			// Height Total Inches
			constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".HeightTotalInches", (long) height2.getMeasureValue(),
					SIMPLE_VALUE);
		}
	}

	protected void constructReqData(BulkSubmitObject bulkSubmitObj, int partyIndex, Weight2 weight2) throws NbaBaseException {
		String reqName = getMagnumTranslation(NbaTableConstants.NBA_REQUIREMENTS, String.valueOf(OLI_REQCODE_MEDEXAMPARAMED));
		if (!NbaUtils.isBlankOrNull(weight2)) {
			// Weight Pounds
			constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".WeightPounds",
					String.valueOf(weight2.getMeasureValue()), SIMPLE_VALUE);
		}
	}

	protected void constructReqData(BulkSubmitObject bulkSubmitObj, int partyIndex, LabTesting labTesting) throws NbaBaseException {
		String reqName = getMagnumTranslation(NbaTableConstants.NBA_REQUIREMENTS, String.valueOf(OLI_REQCODE_MEDEXAMPARAMED));
		if (!NbaUtils.isBlankOrNull(labTesting)) {
			if (!NbaUtils.isBlankOrNull(labTesting.getLabTestResult())) {
				/* NBLXA-2402(NBLXA-2643) code commented
				// Tobacco Types
				List<LabTestResult> tobaccoResultList = getTobaccoTypeLabResult(labTesting.getLabTestResult());
				if (!NbaUtils.isBlankOrNull(tobaccoResultList)) {
					for (LabTestResult aResult : tobaccoResultList) {
						addRefDataValue(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".TobaccoTypes",
								getMagnumTranslation(NbaTableConstants.NBA_REQUIREMENTS, String.valueOf(aResult.getTestCode())));
					}
				}
				*/
				// Is Pregnant
				LabTestResult aLabTestResult = NbaUtils.getLabTestResultByProviderTestCode(labTesting.getLabTestResult(), "B318");
				if (!NbaUtils.isBlankOrNull(aLabTestResult)) {
					if (!NbaUtils.isBlankOrNull(aLabTestResult.getQualitativeResult())) {
						QualitativeResult aQualitativeResult = aLabTestResult.getQualitativeResultAt(0);
						constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".IsPregnant",
								getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(aQualitativeResult.getValueCode())),
								REF_DATA_VALUE);
					}
				}
				/* NBLXA-2402(NBLXA-2643) code commented
				// Nicotine Delivery System
				LabTestResult bLabTestResult = NbaUtils.getLabTestResultByTestCode(labTesting.getLabTestResult(), OLI_REQCODE_1003800814);
				if (!NbaUtils.isBlankOrNull(bLabTestResult)) {
					if (!NbaUtils.isBlankOrNull(bLabTestResult.getQualitativeResult())) {
						QualitativeResult aQualitativeResult = bLabTestResult.getQualitativeResultAt(0);
						constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".NicotineDeliverySystem",
								getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(aQualitativeResult.getValueCode())),
								REF_DATA_VALUE);
					}
				}
				*/
				// High Blood Pressure
				LabTestResult cLabTestResult = NbaUtils.getLabTestResultByTestCode(labTesting.getLabTestResult(), OLI_REQCODE_BLOODPRESSQ);
				if (!NbaUtils.isBlankOrNull(cLabTestResult)) {
					if (!NbaUtils.isBlankOrNull(cLabTestResult.getQualitativeResult())) {
						QualitativeResult aQualitativeResult = cLabTestResult.getQualitativeResultAt(0);
						constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".HighBloodPressure",
								getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(aQualitativeResult.getValueCode())),
								REF_DATA_VALUE);
					}
				}
				// Diabetes
				LabTestResult dLabTestResult = NbaUtils.getLabTestResultByTestCode(labTesting.getLabTestResult(), OLI_REQCODE_QDIABETES);
				if (!NbaUtils.isBlankOrNull(dLabTestResult)) {
					if (!NbaUtils.isBlankOrNull(dLabTestResult.getQualitativeResult())) {
						QualitativeResult aQualitativeResult = dLabTestResult.getQualitativeResultAt(0);
						constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".Diabetes",
								getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(aQualitativeResult.getValueCode())),
								REF_DATA_VALUE);
					}
				}
				// Cancer
				LabTestResult eLabTestResult = NbaUtils.getLabTestResultByTestCode(labTesting.getLabTestResult(), OLI_REQCODE_CANCERHISTORY);
				if (!NbaUtils.isBlankOrNull(eLabTestResult)) {
					if (!NbaUtils.isBlankOrNull(eLabTestResult.getQualitativeResult())) {
						QualitativeResult aQualitativeResult = eLabTestResult.getQualitativeResultAt(0);
						constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".Cancer",
								getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(aQualitativeResult.getValueCode())),
								REF_DATA_VALUE);
					}
				}
				// Heart Disease
				LabTestResult fLabTestResult = NbaUtils.getLabTestResultByTestCode(labTesting.getLabTestResult(), OLI_REQCODE_HEARTMURMURQUEST);
				if (!NbaUtils.isBlankOrNull(fLabTestResult)) {
					if (!NbaUtils.isBlankOrNull(fLabTestResult.getQualitativeResult())) {
						QualitativeResult aQualitativeResult = fLabTestResult.getQualitativeResultAt(0);
						constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".HeartDisease",
								getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(aQualitativeResult.getValueCode())),
								REF_DATA_VALUE);
					}
				}
				// Has Moving Violations
				LabTestResult gLabTestResult = NbaUtils.getLabTestResultByTestCode(labTesting.getLabTestResult(), OLI_REQCODE_QVIOLATION);
				if (!NbaUtils.isBlankOrNull(gLabTestResult)) {
					if (!NbaUtils.isBlankOrNull(gLabTestResult.getQualitativeResult())) {
						QualitativeResult aQualitativeResult = gLabTestResult.getQualitativeResultAt(0);
						constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "]." + reqName + ".HasMovingViolations",
								getMagnumTranslation(NbaTableConstants.MGNM_YES_NO, String.valueOf(aQualitativeResult.getValueCode())),
								REF_DATA_VALUE);
					}
				}
			}
		}
	}

	protected List<LabTestResult> getTobaccoTypeLabResult(List<LabTestResult> labTestResultList) {
		List<LabTestResult> tobaccoResultList = new ArrayList<LabTestResult>();
		tobaccoResultList.add(NbaUtils.getLabTestResultByTestCode(labTestResultList, OLI_REQCODE_1003800806));
		tobaccoResultList.add(NbaUtils.getLabTestResultByTestCode(labTestResultList, OLI_REQCODE_1003800811));
		tobaccoResultList.add(NbaUtils.getLabTestResultByTestCode(labTestResultList, OLI_REQCODE_1003800812));
		tobaccoResultList.add(NbaUtils.getLabTestResultByTestCode(labTestResultList, OLI_REQCODE_1003800813));
		Iterator<LabTestResult> itr = tobaccoResultList.iterator();
		while (itr.hasNext()) {
			LabTestResult aResult = itr.next();
			if (aResult == null || NbaUtils.isBlankOrNull(aResult.getQualitativeResult())) {
				itr.remove();
				continue;
			}
			QualitativeResult aQualitativeResult = aResult.getQualitativeResultAt(0);
			if (aQualitativeResult.getValueCode() != OLI_QUALVALUE_YES) {
				itr.remove();
			}
		}
		return tobaccoResultList;
	}

}
