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

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.RiskExtension;
import com.csc.fsg.nba.vo.txlife.SubstanceUsage;
import com.csc.fsg.nba.vo.txlife.SubstanceUsageExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Class that will take care of the processing once ACLabTest model is invoked 
 * from NBCTEVAL and NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * <tr><td>ALS4576</td><td>AXA Life Phase 1</td><td>QC # 3647 - 3.7.31 provider feed from CRL, lab results not displayed on preferred profile</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACLabTestProcessor extends NbaVpmsModelProcessor {

	protected ArrayList labTestImpairments =new ArrayList();//ACN024
	protected ArrayList labTestAccepImpairments = null;	

	/**
	 * Overridden method, calls the model and 
	 * updates the contract with impairments.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		impSrc = NbaConstants.LABTEST_SRC; //ACN016
		if (performingRequirementsEvaluation()) { //SPR2652
			setPartyID(work); //ACN024
			isSuccess = callLabTestModel();
			if (!isSuccess) {
				throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.AC_LAB_TEST);	//SPR2652
			}
			   //Do the Impairments Merging //ACN024
			  mergeImpairmentsAndAccep(labTestImpairments,labTestAccepImpairments); //ACN016							
		}
	}


	/**
	 * This function is used to call the ACLabTest model	 
	 * @param 
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */
	//ACP001 New Method	
	public boolean callLabTestModel() throws NbaBaseException {
		VpmsModelResult vpmsModelResult = null;
		boolean success = false;
		ArrayList tempImpairmentList = null;
		ArrayList tempAcceptableImpairmentList = null;
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess accessContract = new NbaOinkDataAccess(txLifeReqResult); //ACN009
			accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
			accessContract.setLobSource(work.getNbaLob());  //AXAL3.7.07
			vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.AC_LAB_TEST); //SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);
			Map deOink = getLabTestDeOINKValues();
			getTxLifeDeOINKValues(deOink);//ALS4328
			Object[] args = getKeys();
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setRequirementIdFilter(reqId);
			oinkRequest.setArgs(args);
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			if (vpmsResultsData == null) {
				//SPR3362 code deleted
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_LAB_TEST); //SPR2652
			} //SPR2652
			String xmlString = (String) vpmsResultsData.getResultsData().get(0);
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug("### Results from VPMS Model:" + NbaVpmsAdaptor.AC_LAB_TEST);
			    getLogger().logDebug(xmlString);
			} //SPR3290
			NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
			vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
			labTestImpairments = vpmsModelResult.getImpairmentInfo();   //ACN024
			labTestAccepImpairments = vpmsModelResult.getAcceptableImpairments(); //ACN024
			if (tempImpairmentList != null && tempImpairmentList.size() != 0) {
				//listImpairmentInfo.addAll(tempImpairmentList);  //ACN024
			}
			if (tempAcceptableImpairmentList != null && tempAcceptableImpairmentList.size() != 0) {
				//listAcceptableImpairments.addAll(tempAcceptableImpairmentList);  //ACN024
			}
			success = true;
			// SPR2652 Code Deleted
			//SPR3362 code deleted
		// SPR2652 Code Deleted
		} catch (RemoteException e) {
			handleRemoteException(e, NbaVpmsAdaptor.AC_LAB_TEST); //SPR2652
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
	
	//ALS4328 New Method
	public void getTxLifeDeOINKValues(Map deOink){
		
		NbaParty insuredParty = nbaTxLife.getParty(partyID);
		if(insuredParty != null){
			Party party = insuredParty.getParty();
			if(party!= null && party.hasRisk()){
				Risk risk = party.getRisk();
				if(risk != null){
					RiskExtension riskExtension = NbaUtils.getFirstRiskExtension(risk);
					if(riskExtension!= null){
						deOink.put("A_TobaccoDiscIndCode_INS_RIX", String.valueOf(riskExtension.getTobaccoDiscIndCode()));
					}
					String usage = String.valueOf(NbaOliConstants.OLI_TOBACCO_PRIOR);
					if (hasCurrentSubstanceUse(risk)) {
						usage = String.valueOf(NbaOliConstants.OLI_TOBACCO_CURRENT);
					} 
					deOink.put("A_CurrentUseType_INS", usage);
					
				}
			}
		}
	}
	/*
	 * Method loops over SubstanceUsage objects.
	 * if usage exists and any are current, return true otherwise return false for never/past
	 */
	//ALS4328 new method
	private boolean hasCurrentSubstanceUse(Risk risk) {
		
		boolean isCurrentUsage = false;
		int noOfSubstances = risk.getSubstanceUsageCount();
		
		SubstanceUsage subUsage = null;
		for (int i = 0; i < noOfSubstances; i++) {
			subUsage = risk.getSubstanceUsageAt(i);
			if (subUsage != null) {
				SubstanceUsageExtension substExt = NbaUtils.getFirstSubstanceUsageExtension(subUsage);
				if(substExt != null && substExt.hasCurrentUseType()) {
					if(NbaOliConstants.OLI_TOBACCO_CURRENT == substExt.getCurrentUseType()) {
						isCurrentUsage = true;
						break;
					}
					
				}
			}
		}
		return isCurrentUsage;
	}

	
	/**
	 * This function gets all the deOink variables for AcLabTest model	 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//ACP001 New Method	
	public Map getLabTestDeOINKValues() throws NbaBaseException {
		String testCode = "";
		String[] testCodeList = null;
		String labValue = "";
		String[] labValueList = null;
		int testCodeCount = 0;
		int labValueCount = 0;
		HashMap deOink = new HashMap();
		NbaOinkDataAccess accessContract = new NbaOinkDataAccess(txLifeReqResult); //ACN009
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		//begin ALS4576
		for (int a=0;a<txLifeReqResult.getOLifE().getPartyCount();a++) {
			if (txLifeReqResult.getOLifE().getPartyAt(a).getId().equalsIgnoreCase(this.reqRelatePartyID)) {
				oinkRequest.setPartyFilter(a);//APSL1094
				break;
			}
		}

		//end ALS4576
		// begin AXAL3.7.07
		getLabTestingRemarkCodeOINKValues(accessContract, oinkRequest, deOink);
		getLabTestingRemarkSubCodeOINKValues(accessContract, oinkRequest, deOink);
		// end AXAL3.7.07
		oinkRequest.setVariable("LabTestCode");
		oinkRequest.setRequirementIdFilter(reqId);
		testCodeList = accessContract.getStringValuesFor(oinkRequest);
		testCodeCount = testCodeList.length;
		deOink.put("A_no_of_LabTestCode", (new Integer(testCodeCount)).toString());
		for (int i = 0; i < testCodeCount; i++) {
			testCode = testCodeList[i];
			if (i == 0) {
				deOink.put("A_LabTestCode", testCode);
			} else {
				deOink.put("A_LabTestCode[" + i + "]", testCode);
			}
			oinkRequest.setVariable("LabValue");
			oinkRequest.setRequirementIdFilter(reqId);
			oinkRequest.setLabTestCodeFilter(testCode);
			labValueList = accessContract.getStringValuesFor(oinkRequest);
			labValueCount = labValueList.length;
			if (i == 0) {
				deOink.put("A_no_of_LabValue", (new Integer(labValueCount)).toString());
			} else {
				deOink.put("A_no_of_LabValue[" + i + "]", (new Integer(labValueCount)).toString());
			}
			for (int j = 0; j < labValueCount; j++) {
				labValue = labValueList[j];
				if (i == 0 && j == 0) {
					deOink.put("A_LabValue", labValue);
				} else {
					deOink.put("A_LabValue[" + i + "," + j + "]", labValue);
				}
			}
		}
		return deOink;
	}
	/**
	 * This function gets all the deOink variables for LabTesting LabTestRemark RemarkCode	 
	 * @param NbaOinkDataAccess accessContract
	 * @param NbaOinkRequest oinkRequest
	 * @param HashMap deOink
	 * @return none
	 * @throws NbaBaseException
	 */
	//AXAL3.7.07 New Method	
	public void getLabTestingRemarkCodeOINKValues(NbaOinkDataAccess accessContract, NbaOinkRequest oinkRequest, HashMap deOink) throws NbaBaseException {
		String[] remarkCodeList = null;
		int remarkCodeCount = 0;
		String remarkCode = "";
	    oinkRequest.setVariable("LabTestingRemarkCode_INS");
		oinkRequest.setRequirementIdFilter(reqId);
		remarkCodeList = accessContract.getStringValuesFor(oinkRequest);
		remarkCodeCount = remarkCodeList.length;
		deOink.put("A_LabTestingRemarkCount", (new Integer(remarkCodeCount)).toString());
		for (int i = 0; i < remarkCodeCount; i++) {
			remarkCode = remarkCodeList[i];
			if (i == 0) {
				deOink.put("A_LabTestingRemarkCode", remarkCode);
			} else {
				deOink.put("A_LabTestingRemarkCode[" + i + "]", remarkCode);
			}
		}  //end for 
	    return;
	}
	/**
	 * This function gets all the deOink variables for LabTesting LabTestRemark RemarkSubCode	 
	 * @param NbaOinkDataAccess accessContract
	 * @param NbaOinkRequest oinkRequest
	 * @param HashMap deOink
	 * @return none
	 * @throws NbaBaseException
	 */
	//AXAL3.7.07 New Method	
	public void getLabTestingRemarkSubCodeOINKValues(NbaOinkDataAccess accessContract, NbaOinkRequest oinkRequest, HashMap deOink) throws NbaBaseException {
		String[] remarkSubCodeList = null;
		int remarkSubCodeCount = 0;
		String remarkSubCode = "";
	    oinkRequest.setVariable("LabTestingRemarkSubCode_INS");
		oinkRequest.setRequirementIdFilter(reqId);
		remarkSubCodeList = accessContract.getStringValuesFor(oinkRequest);
		remarkSubCodeCount = remarkSubCodeList.length;
		for (int i = 0; i < remarkSubCodeCount; i++) {
			remarkSubCode = remarkSubCodeList[i];
			if (i == 0) {
				deOink.put("A_LabTestingRemarkSubCode", remarkSubCode);
			} else {
				deOink.put("A_LabTestingRemarkSubCode[" + i + "]", remarkSubCode);
			}
		}  //end for 
	    return;
	}
}
