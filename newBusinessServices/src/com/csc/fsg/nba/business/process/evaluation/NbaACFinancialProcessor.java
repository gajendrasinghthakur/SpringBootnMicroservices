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

import com.csc.fsg.nba.datamanipulation.NbaContractDataAccessConstants;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Class that will take care of the processing once ACContractSummary model is invoked 
 * from NBCTEVAL and NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACP014</td><td>Version 4</td><td>Financial Screening</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * <tr><td>ALCP161</td><td>AXA Life Phase 1</td><td>Ultimate Amounts</td></tr>
 * <tr><td>P2AXAL053</td><td>AXA Life Phase 2</td><td>R2 Auto Underwriting</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACFinancialProcessor extends NbaVpmsModelProcessor {
	ArrayList financialImpairments = new ArrayList();
	ArrayList accepImpairments = null;
	HashMap deOinkMap = new HashMap();
	
	/**
	 * Overridden method, calls the model and 
	 * updates the contract.
	 * @throws NbaBaseException
	 */	
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		impSrc = NbaConstants.FINANCIAL_SRC;//ACN016		 	
		if(performingContractEvaluation()){	//SPR2652
			int partyIndex = 0;
			ArrayList al = getAllInsuredIndexes();
			OLifE oLifE = nbaTxLife.getOLifE();			
			int insuredCount = al.size();
			financialImpairments = new ArrayList();
			for(int i=0;i<insuredCount;i++){
				isSuccess = false;
				partyIndex = ((Integer)al.get(i)).intValue();
				partyID = oLifE.getPartyAt(partyIndex).getId();											
				financialImpairments.clear();
				deOinkMap.clear();
				isSuccess = callFinancialModel(partyID, i);	//SPR2652
				if (!isSuccess){
					throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.AC_FINANCIAL);	//SPR2652
				}
				getContractImpairments(partyID);
				ArrayList[] mergedLists = mergeImpairments(contractImpairments, financialImpairments, new ArrayList(), new ArrayList());
				addImpairmentInfo(partyID, mergedLists[0]);	
			}
		}
	}
	/**
	 * This function acts as an entry point for calling the ACFINANCIAL model
	 * @param insuredIndex: Index of the insured
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */	
	public boolean callFinancialModel(String partyId, int insuredIndex) throws NbaBaseException { //SPR2652
		boolean success = true;
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			if(getLogger().isDebugEnabled()){				
				getLogger().logDebug("Calling the Financial Model for Party : "+partyID);				
			}			
			NbaOinkDataAccess accessContract = new NbaOinkDataAccess(nbaTxLife);
			accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
			accessContract.setLobSource(work.getNbaLob());
			vpmsProxy =	new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.AC_FINANCIAL); //SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			if (updatePartyFilterInRequest(oinkRequest, partyId)) { //SPR2652
			//Get the Financial DeOink Values
				getFinancialDeOINKValues(accessContract, oinkRequest);//P2AXAL053
				vpmsProxy.setANbaOinkRequest(oinkRequest);
				// begin AXAL3.7.07
				deOinkExpenseNeedTypeCode(deOinkMap, accessContract, oinkRequest);
				deOinkFundingDisclosureTC(deOinkMap, accessContract, oinkRequest);
				deOinkFormInstance(deOinkMap);//A2_AXAL007
				// end AXAL3.7.07
				
				//Begin ALII1299 QC8469
            	ArrayList coverageList = nbaTxLife.getCoveragesFor(partyId);
            	if(!coverageList.isEmpty()){
            		deOinkMap.put("A_LIFECOVTYPECODE", String.valueOf(((Coverage) coverageList.get(0)).getLifeCovTypeCode())); 
            	}
            	//End ALII1299 QC8469
            	
				vpmsProxy.setSkipAttributesMap(deOinkMap);
				oinkRequest.setArgs(getKeys());
				vpmsProxy.setANbaOinkRequest(oinkRequest);
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
				if (vpmsResultsData.getResult().isError()) {
					//SPR3362 code deleted
					throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_FINANCIAL); //SPR2652			
				}
				// SPR1652 code deleted
				String xmlString = (String) vpmsResultsData.getResultsData().get(0);
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Results from VPMS Model: " + NbaVpmsAdaptor.AC_FINANCIAL);
					getLogger().logDebug(xmlString);
				}
				NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
				VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
				financialImpairments = vpmsModelResult.getImpairmentInfo();				
				success = true;
			}
			//SPR3362 code deleted
			// SPR1652 code deleted				
		}		
		catch(RemoteException e){	//SPR1652
			handleRemoteException(e, NbaVpmsAdaptor.AC_FINANCIAL); //SPR2652			
		//begin SPR3362
		} finally {
		    if(vpmsProxy != null){
		        try {
		            vpmsProxy.remove();
                } catch (Exception e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
		    }
		//end SPR3362
		}
		return success;
	}
	/**
	 * This function acts as an entry point for calculating all the deOink values
	 * @param accessContract: The NbaOinkDataAccess having all the oink sources	 	
	 * @throws NbaBaseException
	 */	
	// P2AXAL053 method signature changed
	public void getFinancialDeOINKValues(NbaOinkDataAccess accessContract, NbaOinkRequest oinkRequest) throws NbaBaseException {
		if (nbaTxLife != null) {			
			// SPR3290 code deleted
			NbaOinkDataAccess dataAccess = new NbaOinkDataAccess(nbaTxLife);
			dataAccess.setAcdbSource(new NbaAcdb(), nbaTxLife);
			// begin AXAL3.7.07
			getRelationDeOinkValues(deOinkMap, NbaContractDataAccessConstants.PARTY_OWNER);
			getRelationDeOinkValues(deOinkMap, NbaContractDataAccessConstants.PARTY_BENEFICIARY); 
			getRelationDeOinkValues(deOinkMap, NbaContractDataAccessConstants.PARTY_COBENEFICIARY); 
			// end AXAL3.7.07
			getRelationDeOinkValues(deOinkMap, NbaContractDataAccessConstants.PARTY_APPLCNT);//ALII105
			// begin ALCP161, P2AXAL053 
			deOinkTotalRiskAmt(accessContract, oinkRequest); //ALII1658
			deOinkReplacedInd(accessContract, oinkRequest); //ALII1658
			deOinkAppliedForInsAmt(accessContract,oinkRequest);
			deOinkReasonForOtherAppl(accessContract,oinkRequest);
			// end ALCP161, P2AXAL053
			getOtherInsValues();
			getSumPolicyDeOinkValues(NbaContractDataAccessConstants.PARTY_PRIM_INSURED);
			getSumPolicyDeOinkValues(NbaContractDataAccessConstants.PARTY_SPOUSE);
			
			deOinkOptionNumberOfUnitsGIR(accessContract);
			//DeOink the fields which have a qualifier other than _INS
			//because of a different qualifier these fields can NOT be called
			//with the same party filter
			deOinkOtherPartyValues(accessContract,"TOTALINFORCE_PYR");
			deOinkOtherPartyValues(accessContract,"ESTGROSSANNUALOTHERINCOME_PYR");
			deOinkOtherPartyValues(accessContract,"ESTSALARY_PYR");
			deOinkOtherPartyValues(accessContract,"PREVYRTAXABLEEARNINGSAMT_PYR");
			deOinkOtherPartyValues(accessContract,"YRENDNETWORTHAMT_PYR");		
			deOinkOtherPartyValues(accessContract,"HOMLINE1_PINS");
			deOinkOtherPartyValues(accessContract,"PREVYRTAXABLEEARNINGSAMT_OWN");
			deOinkOtherPartyValues(accessContract,"YRENDNETWORTHAMT_OWN");
			deOinkOtherPartyValues(accessContract,"RELTOANNORINS_PYR");
		
			oinkRequest.setVariable("ReplacementInd");
			String[] replacementIndList = dataAccess.getStringValuesFor(oinkRequest);
		
			deOinkMap.put("A_no_of_ReplacementInd",String.valueOf(replacementIndList.length));
			deOinkMap.put("A_ReplacementInd",replacementIndList);
		}
	}	

	/**
	 * Return the Array List containing all the Insureds in the contract
	 * (apart from the primary insured)	
	 * @return ArrayList: Array List containing party ids of other insured  
	 */
	public ArrayList getOthInsuredList(){
		ArrayList otherInsList = new ArrayList();
		ArrayList partyList = nbaTxLife.getOLifE().getParty();
		Party party = null;
		String otherInsPartyID = "";
		int listSize = partyList.size();		
		for(int i=0; i<listSize; i++){
			party = (Party)partyList.get(i);
			if(party!=null){
				otherInsPartyID = party.getId();
				if(nbaTxLife.isInsured(otherInsPartyID) && !isPrimaryInsured(otherInsPartyID)){
					otherInsList.add(otherInsPartyID);	
				}
			}
		}
		return otherInsList;
	}
	/**
	 * Determine if the Party is Primary insured
	 * @param partyId - the party id
	 * @return true if the party is a primary insured
	 */	
	public boolean isPrimaryInsured(String partyId) {
		ArrayList relations = nbaTxLife.getOLifE().getRelation();
		int listSize = relations.size();
		Relation relation = null;
		for (int i = 0; i < listSize; i++) {
			relation = (Relation) relations.get(i);
			if(relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode()== NbaOliConstants.OLI_REL_INSURED){
				return true;
			}	
		}
		return false;
	}
	//AXAL3.7.07 Code Deleted


	/**
	 * This function calculates the ReltoAnnOrIns and SumPolicyLife deOink values
	 * for Other Insured (OTH)	 	 	
	 * @throws NbaBaseException
	 */			
	public void getOtherInsValues() throws NbaBaseException{	
		NbaOinkDataAccess dataAccess = new NbaOinkDataAccess(nbaTxLife);		
		dataAccess.setAcdbSource(new NbaAcdb(), nbaTxLife);
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		ArrayList othInsList = getOthInsuredList();
		int othInsListSize = othInsList.size();
		int othInsCount = 0;
		String othPartyId = "";			
		ArrayList reltoAnnOrInsList = new ArrayList();
		ArrayList sumPolicyLifeList = new ArrayList();
		for(int i =0; i< othInsListSize; i++){
			othPartyId = (String) othInsList.get(i);
			if(!othPartyId.equals(partyID)){ //check if the party id matches the current insured party id	
				if (updatePartyFilterInRequest(oinkRequest, othPartyId)) { //SPR2652				
					oinkRequest.setVariable("ReltoAnnOrIns_OTH");
					// SPR2652 code deleted
					reltoAnnOrInsList.add(dataAccess.getStringValueFor(oinkRequest));

					oinkRequest.setVariable("SumPolicyLife");
					oinkRequest.setArgs(getKeys(othPartyId));
					sumPolicyLifeList.add(dataAccess.getStringValueFor(oinkRequest));
					othInsCount++;
				}
			}
		}
		deOinkMap.put("A_no_of_ReltoAnnOrIns_OTH",String.valueOf(othInsCount));
		populateDeoinkMap("A_ReltoAnnOrIns_OTH",reltoAnnOrInsList);
		populateDeoinkMap("A_SumPolicyLife_OTH",sumPolicyLifeList);		
	}
	/**
	 * This function calculates the SumPolicyLife deOink values
	 * @param qualifier: Party Qualifier, valid qualifiers are PINS, SPS	 	
	 * @throws NbaBaseException
	 */		
	public void getSumPolicyDeOinkValues(String qualifier) throws NbaBaseException{
		// SPR3290 code deleted
		String oinkVarName = "SumPolicyLife";
		String attrName = "A_" + oinkVarName + "_" + qualifier;
		String partyId = null;						
		if(qualifier.equals(NbaContractDataAccessConstants.PARTY_PRIM_INSURED)){
			partyId = nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_INSURED);
		}else if(qualifier.equals(NbaContractDataAccessConstants.PARTY_SPOUSE)){
			partyId = nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_SPOUSE);
		}
		if(partyId!=null && !partyId.equals("")){			
			NbaOinkDataAccess dataAccess = new NbaOinkDataAccess(nbaTxLife);
			dataAccess.setAcdbSource(new NbaAcdb(), nbaTxLife);
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setVariable("SumPolicyLife");										
			oinkRequest.setArgs(getKeys(partyId));			
			String value = dataAccess.getStringValueFor(oinkRequest);
			deOinkMap.put(attrName, value);
		}else{
			deOinkMap.put(attrName,"0");
		}		
	}
	/**
	 * This function populates deOink Map from the ArrayList
	 * @param varName : Name of the attribute
	 * @param valueList : ArrayList containing the values
	 */	
	public void populateDeoinkMap(String varName, ArrayList valueList){		
		String value = "";
		int count = 0;
		if(valueList!=null){
			count = valueList.size(); 
		}		
		if(count==0){
			deOinkMap.put(varName,"0");
		}
		for(int i=0;i<count;i++){
			value = (String) valueList.get(i);
			if(i==0){
				deOinkMap.put(varName, value);
			}else{
				deOinkMap.put(varName+"["+i+"]", value);
			}
		}
	}
	/**
	 * This function calculates the OptionNumberOfUnits_GIR deOink value
	 * @param accessContract: The NbaOinkDataAccess having all the oink sources	 	
	 * @throws NbaBaseException
	 */	
	private void deOinkOptionNumberOfUnitsGIR(NbaOinkDataAccess accessContract) throws NbaBaseException {
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		String oinkVarName = "OptionNumberOfUnits_GIR";
		oinkRequest.setVariable(oinkVarName);
		oinkRequest.setCoverageFilter((int)NbaOliConstants.OLI_COVIND_BASE);
		String value = accessContract.getStringValueFor(oinkRequest);			
		deOinkMap.put("A_"+oinkVarName, value);
	}
	/**
	 * This function calculates the deOink values for other parties
	 * @param accessContract: The NbaOinkDataAccess having all the oink sources	 	
	 * @throws NbaBaseException
	 */	
	private void deOinkOtherPartyValues(NbaOinkDataAccess accessContract, String variableName) throws NbaBaseException {
		NbaOinkRequest oinkRequest = new NbaOinkRequest();				
		oinkRequest.setVariable(variableName);
		// SPR2652 code deleted	
		String value = accessContract.getStringValueFor(oinkRequest);			
		deOinkMap.put("A_"+variableName, value);
	}	
	/**
	 * This function deOinks questions 43 TotalRiskAmt values
	 * @param accessContract: The NbaOinkDataAccess having all the oink sources	 	
	 * @throws NbaBaseException
	 */	
	//ALCP161 New Method, ALII1658 Signature Changed
	private void deOinkTotalRiskAmt(NbaOinkDataAccess accessContract, NbaOinkRequest oinkRequest) throws NbaBaseException {
		 //NbaOinkRequest oinkRequest = new NbaOinkRequest(); //ALII1658
		 oinkRequest.setVariable("TotalRiskAmt");
		 String[] totalRiskAmtList = accessContract.getStringValuesFor(oinkRequest);
		 int count = totalRiskAmtList.length;
		 deOinkMap.put("A_no_of_TotalRiskAmt", new Integer(count).toString());
		 if (count == 0) {
			 deOinkMap.put("A_TotalRiskAmt", "");
		 } 
		 else {
			 for (int i = 0; i < count; i++) {
			 	if (i==0) {
			 		 deOinkMap.put("A_TotalRiskAmt", totalRiskAmtList[i]);
			 	}
			 	else {
			 		 deOinkMap.put("A_TotalRiskAmt[" + i + "]", totalRiskAmtList[i]);
			 	}
			 }
		 }
	}
	/**
	 * This function deOinks questions 43 ReplacedInd values
	 * @param accessContract: The NbaOinkDataAccess having all the oink sources	 	
	 * @throws NbaBaseException
	 */	
	//ALCP161 New Method, ALII1658 Signature Changed
	private void deOinkReplacedInd(NbaOinkDataAccess accessContract, NbaOinkRequest oinkRequest) throws NbaBaseException {
		 //NbaOinkRequest oinkRequest = new NbaOinkRequest(); //ALII1658
		 oinkRequest.setVariable("ReplacedInd");
		 String[] replacedIndList = accessContract.getStringValuesFor(oinkRequest);
		 int count = replacedIndList.length;
		 for (int i = 0; i < count; i++) {
		 	if (i==0) {
		 		 deOinkMap.put("A_ReplacedInd", replacedIndList[i]);
		 	}
		 	else {
		 		 deOinkMap.put("A_ReplacedInd[" + i + "]", replacedIndList[i]);
		 	}
		 }
	}
	/**
	 * This function deOinks questions 44 AppliedForInsAmt values
	 * @param accessContract: The NbaOinkDataAccess having all the oink sources	 	
	 * @throws NbaBaseException
	 */	
	//ALCP161 New Method P2AXAL053 Signature changed
	private void deOinkAppliedForInsAmt(NbaOinkDataAccess accessContract, NbaOinkRequest oinkRequest) throws NbaBaseException {
		//NbaOinkRequest oinkRequest = new NbaOinkRequest();//P2AXAL053
		 oinkRequest.setVariable("AppliedForInsAmt");
		 String[] appliedForInsAmtList = accessContract.getStringValuesFor(oinkRequest);
		 int count = appliedForInsAmtList.length;
		 deOinkMap.put("A_no_of_AppliedForInsAmt", new Integer(count).toString());
		 if (count == 0) {
			 deOinkMap.put("A_AppliedForInsAmt", "");
		 } 
		 else {
			 for (int i = 0; i < count; i++) {
			 	if (i==0) {
			 		 deOinkMap.put("A_AppliedForInsAmt", appliedForInsAmtList[i]);
			 	}
			 	else {
			 		 deOinkMap.put("A_AppliedForInsAmt[" + i + "]", appliedForInsAmtList[i]);
			 	}
			 }
		 }
	}
	/**
	 * This function deOinks questions 44 ReasonForOtherAppl values
	 * @param accessContract: The NbaOinkDataAccess having all the oink sources	 	
	 * @throws NbaBaseException
	 */	
	//ALCP161 New Method P2AXAL053 Signature changed
	private void deOinkReasonForOtherAppl(NbaOinkDataAccess accessContract, NbaOinkRequest oinkRequest) throws NbaBaseException {
		//NbaOinkRequest oinkRequest = new NbaOinkRequest();//P2AXAL053
		 oinkRequest.setVariable("ReasonForOtherAppl");
		 String[] reasonForOtherApplList = accessContract.getStringValuesFor(oinkRequest);
		 int count = reasonForOtherApplList.length;
		 for (int i = 0; i < count; i++) {
		 	if (i==0) {
		 		 deOinkMap.put("A_ReasonForOtherAppl", reasonForOtherApplList[i]);
		 	}
		 	else {
		 		 deOinkMap.put("A_ReasonForOtherAppl[" + i + "]", reasonForOtherApplList[i]);
		 	}
		 }
	}
}
