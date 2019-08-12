package com.csc.fsg.nba.business.process.evaluation;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.ac.DefaultValues;
import com.csc.fsg.nba.vo.ac.SummaryValues;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.SubstanceUsage;
import com.csc.fsg.nba.vo.txlife.SubstanceUsageExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Class that will take care of the processing once ACRequirementSummary model is invoked 
 * from NBCTEVAL and NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR2741</td><td>Version 6</td><td>Re-evaluation is generating insert errors</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACRequirementSummaryProcessor extends NbaVpmsModelProcessor {

	/**
	 * Overridden method, calls the model and 
	 * updates the contract.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;		
		if (performingRequirementsEvaluation()) { //SPR2652
			setPartyID(work); //ACN024
			isSuccess = processRequirementSummary();
			if (!isSuccess) {				
				throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.ACREQUIREMENTSUMMARY);	//SPR2652
			}
		}
	}
	/**
	 * This function acts as an entry point for calling the ACREQUIREMENTSUMMARY model	 
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */	
	//ACP002 new method.
	public boolean processRequirementSummary() throws NbaBaseException {
		boolean success = false;
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(txLifeReqResult); //ACN009
			oinkData.setAcdbSource(new NbaAcdb(), nbaTxLife);
			oinkData.setLobSource(work.getNbaLob());
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug("########### Testing Requirment Summary ###########");
			    getLogger().logDebug("########### PartyId: " + partyID);
			}//SPR3290
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.ACREQUIREMENTSUMMARY); //SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_CALCXMLOBJECTS);
			Map deOink = new HashMap();
			//			######## DEOINK
			deOink.put(NbaVpmsConstants.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
			deOinkContractFieldsForRequirement(deOink);
			deOinkXMLResultFields(deOink);
            deOinkSubstanceUsage(oinkData, deOink, partyID); //AXAL3.7.07
			deOinkLabTestResults(deOink);
			Object[] args = getKeys();
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setRequirementIdFilter(reqId);
			oinkRequest.setArgs(args);
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult vcr = vpmsProxy.getResults();
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vcr);
			ArrayList results = vpmsResultsData.getResultsData();
			results = vpmsResultsData.getResultsData();
			//Resulting string will be the zeroth element.
			if (results == null) {
				//SPR3362 code deleted
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.ACREQUIREMENTSUMMARY); //SPR2652
			} //SPR2652
			vpmsResult = (String) results.get(0);
			NbaVpmsModelResult vpmsOutput = new NbaVpmsModelResult(vpmsResult);
			VpmsModelResult vpmModelResult = vpmsOutput.getVpmsModelResult();
			updateDefaultValues(vpmModelResult.getDefaultValues());
			updateSummaryValues(vpmModelResult.getSummaryValues());
			success = true;
			// SPR2652 Code Deleted
			//SPR3362 code deleted
		} catch (RemoteException re) {
			throw new NbaBaseException("Remote Exception occured in processRequirementSummary", re);
		// SPR2652 Code Deleted
		//begin SPR3362
		} finally {
		    if(vpmsProxy != null){
		        try {
                    vpmsProxy.remove();
                } catch (RemoteException e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
		    }
		//end SPR3362
		}
		return success;
	}
	
	//ACP002 new method.
	private void deOinkLabTestResults(Map deOink) throws NbaBaseException {
		String testCode = "";
		String specimanType = "";
		String[] testCodeList = null;
		String[] specimanTypeList = null;
		String labValue = "";
		String[] labValueList = null;
		int testCodeCount = 0;
		int labValueCount = 0;
		if(getLogger().isDebugEnabled()) { //SPR3290
		    getLogger().logDebug("######### deOinkLabTestResults Values.....");
		}//SPR3290
		NbaOinkDataAccess accessContract = new NbaOinkDataAccess(txLifeReqResult); //ACN009
		String oinkVarLabTestCode = "LabTestCode";
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		oinkRequest.setVariable(oinkVarLabTestCode);
		oinkRequest.setRequirementIdFilter(reqId);
		testCodeList = accessContract.getStringValuesFor(oinkRequest);
		testCodeCount = testCodeList.length;
		deOink.put("A_no_of_" + oinkVarLabTestCode, String.valueOf(testCodeCount));
		if(getLogger().isDebugEnabled()) { //SPR3290
		    getLogger().logDebug("A_no_of_" + oinkVarLabTestCode + ": " + testCodeCount);
		}//SPR3290
		String oinkVarLabSpecimanType = "LabSpecimanType";
		oinkRequest.setVariable(oinkVarLabSpecimanType);
		specimanTypeList = accessContract.getStringValuesFor(oinkRequest);
		for (int i = 0; i < testCodeCount; i++) {
			testCode = testCodeList[i];
			specimanType = specimanTypeList[i];
			if (i == 0) {
				deOink.put("A_" + oinkVarLabTestCode, testCode);
				if(getLogger().isDebugEnabled()) { //SPR3290
				    getLogger().logDebug("A_" + oinkVarLabTestCode + ": " + testCode);
				} //SPR3290
				deOink.put("A_" + oinkVarLabSpecimanType, specimanType);
				if(getLogger().isDebugEnabled()) { //SPR3290 
				    getLogger().logDebug("A_" + oinkVarLabSpecimanType + ": " + specimanType);
				}//SPR3290
			} else {
				deOink.put("A_" + oinkVarLabTestCode + "[" + i + "]", testCode);
				if(getLogger().isDebugEnabled()) { //SPR3290
				    getLogger().logDebug("A_" + oinkVarLabTestCode + "[" + i + "]: " + testCode);
				}//SPR3290
				deOink.put("A_" + oinkVarLabSpecimanType + "[" + i + "]", specimanType);
				if(getLogger().isDebugEnabled()) { //SPR3290
				    getLogger().logDebug("A_" + oinkVarLabSpecimanType + "[" + i + "]: " + specimanType);
				}//SPR3290
			}
			String oinkVarLabValue = "LabValue";
			oinkRequest.setVariable(oinkVarLabValue);
			oinkRequest.setRequirementIdFilter(reqId);
			oinkRequest.setLabTestCodeFilter(testCode);
			labValueList = accessContract.getStringValuesFor(oinkRequest);
			labValueCount = labValueList.length;
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug("i=" + i + ", labValueCount: " + labValueCount); // should be <= 2
			}//SPR3290
			// pick the first Numeric value
			for (int j = 0; j < labValueCount; j++) {
				labValue = labValueList[j];
				if (isNumeric(labValueList[j]))
					break;
			}
			if (i == 0) {
				deOink.put("A_" + oinkVarLabValue, labValue);
				if(getLogger().isDebugEnabled()) { //SPR3290
				    getLogger().logDebug("A_" + oinkVarLabValue + ": " + labValue);
				}//SPR3290
			} else {
				deOink.put("A_" + oinkVarLabValue + "[" + i + "]", labValue);
				if(getLogger().isDebugEnabled()) { //SPR3290
				    getLogger().logDebug("A_" + oinkVarLabValue + "[" + i + "]: " + labValue);
				}//SPR3290
			}
		}
	}
	/**
     * Updates Default values into the database
     * @param defValue values to be stored
     * @throws NbaDataAccessException
     */
    // ACP002 new method
    //SPR2741 added throws clause
    private void updateDefaultValues(DefaultValues defValue) throws NbaDataAccessException {
        setDefValuesKeys(defValue); //SPR2741
        nbaAcdb.updateDefaultValues(defValue);
    }

    /**
     * Updates Summary Values into the database
     * @param sumValue values to be stored
     * @throws NbaDataAccessException
     */
    // ACP002 new method
    //SPR2741 added throws clause
    private void updateSummaryValues(SummaryValues sumValue) throws NbaDataAccessException {
        setSumValuesKeys(sumValue); //SPR2741
        copyPreviousSummaryValues(sumValue);
        nbaAcdb.updateSummaryValues(sumValue);
    }

	//ACP002 new method.
	private void copyPreviousSummaryValues(SummaryValues sumValue) {
		// select the saved object from database
		String args[] = new String[4];
		args[0] = sumValue.getParentIdKey();
		args[1] = sumValue.getContractKey();
		args[2] = sumValue.getCompanyKey();
		args[3] = sumValue.getBackendKey();
		SummaryValues prevSumValue = new NbaAcdb().getSummaryValues(args);
		// copy Policy Amt values to new Object
		if (prevSumValue != null) {//APSL609 added if condition
			sumValue.setSumPolicyLife(prevSumValue.getSumPolicyLife());
			sumValue.setSumPolicyADB(prevSumValue.getSumPolicyADB());
			sumValue.setSumPolicyWP(prevSumValue.getSumPolicyWP());
			sumValue.setSumPolicyGIR(prevSumValue.getSumPolicyGIR());
			sumValue.setSumPolicyAA(prevSumValue.getSumPolicyAA());
			sumValue.setSumPolicyPremiumAmt(prevSumValue.getSumPolicyPremiumAmt());
			sumValue.setSumPolicyGIRUnitCnt(prevSumValue.getSumPolicyGIRUnitCnt());
			sumValue.setSumPolicySinglePremium(prevSumValue.getSumPolicySinglePremium());
			sumValue.setSumTotalAmtGIR(prevSumValue.getSumTotalAmtGIR());
			sumValue.setSumApplicationAmt(prevSumValue.getSumApplicationAmt());
			sumValue.setSumRetentionAmt(prevSumValue.getSumRetentionAmt());
			sumValue.setSumTotalAmt(prevSumValue.getSumTotalAmt());
		}
	}

	//ACP002 new method.
	private boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	/**
	 * This method will deOINK the SubstanceUsage objects on the contract. 
	 * @param NbaOinkDataAccess oinkData
	 * @return Map deOink 	
	 * @param String partyId: partyId of the insured
	 * @throws NbaBaseException
	 */	
	//AXAL3.7.07 New Method
	private void deOinkSubstanceUsage(NbaOinkDataAccess oinkData, Map deOink, String partyId) throws NbaBaseException {
		NbaParty nbaParty = nbaTxLife.getParty(partyId);
		Party party = nbaParty.getParty();
		if (party.hasRisk()) {
			Risk risk = party.getRisk();
			deOink.put("A_SUBSTANCEUSAGECOUNT_INS", Integer.toString(risk.getSubstanceUsageCount()));
			for(int i=0; i < risk.getSubstanceUsageCount(); i++) {
				SubstanceUsage substanceUsage = risk.getSubstanceUsageAt(i);
				if(substanceUsage != null) {
					String varName = "A_SubstanceAmt_INS";
					if (i == 0) {
						deOink.put(varName, Integer.toString(substanceUsage.getSubstanceAmt()));
					} else {
						deOink.put(varName + "[" + i + "]", Integer.toString(substanceUsage.getSubstanceAmt()));
					}
	
					varName = "A_SubstanceType_INS";
					if (i == 0) {
						deOink.put(varName, Long.toString(substanceUsage.getSubstanceType()));
					} else {
						deOink.put(varName + "[" + i + "]", Long.toString(substanceUsage.getSubstanceType()));
					}
					
					varName = "A_SubstanceTobaccoType_INS";
					if (i == 0) {
						deOink.put(varName, Long.toString(substanceUsage.getTobaccoType()));
					} else {
						deOink.put(varName + "[" + i + "]", Long.toString(substanceUsage.getTobaccoType()));
					}
	
					varName = "A_SubstanceEndDate_INS";
					if(substanceUsage.hasSubstanceEndDate()) {
						if (i == 0) {
							deOink.put(varName, NbaUtils.getDateWithoutSeparator(substanceUsage.getSubstanceEndDate()));							
						} else {
							deOink.put(varName + "[" + i + "]", NbaUtils.getDateWithoutSeparator(substanceUsage.getSubstanceEndDate()));
						}
					}
	
					varName = "A_SubstanceMode_INS";
					if (i == 0) {
						deOink.put(varName, Long.toString(substanceUsage.getSubstanceMode()));
					} else {
						deOink.put(varName + "[" + i + "]", Long.toString(substanceUsage.getSubstanceMode()));
					}
	
					SubstanceUsageExtension substanceUsageExtension = NbaUtils.getFirstSubstanceUsageExtension(substanceUsage);
					if(null != substanceUsageExtension) {
					varName = "A_HabitUsage_INS";
						if (i == 0) {
							deOink.put(varName, Long.toString(substanceUsageExtension.getHabitUsage()));
						} else {
							deOink.put(varName + "[" + i + "]", Long.toString(substanceUsageExtension.getHabitUsage()));
						}
						// begin AXAL3.7.07
						varName = "A_SubstanceEndMthYr_INS";
						if (i == 0) {
							deOink.put(varName, substanceUsageExtension.getSubstanceEndMthYr());
						} else {
							deOink.put(varName + "[" + i + "]", substanceUsageExtension.getSubstanceEndMthYr());
						}
						// end AXAL3.7.07
					}
				}
			}
		}
	}
}
