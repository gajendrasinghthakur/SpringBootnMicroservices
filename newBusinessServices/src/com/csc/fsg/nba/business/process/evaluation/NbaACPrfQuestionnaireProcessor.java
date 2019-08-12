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
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.UnderwritingAnalysis;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Class that will take care of the processing once ACPrfQuestionnaire model is invoked 
 * from NBCTEVAL and NBREQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACP008</td><td>Version 4</td><td>Preferred Questionnaire</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACPrfQuestionnaireProcessor extends NbaVpmsModelProcessor{

	protected ArrayList prefQuestionnaireImpairments = new ArrayList();
	protected ArrayList prefQuestionnairePreferredScore = new ArrayList();
	NbaOinkRequest oinkRequest = new NbaOinkRequest();
	/**
	 * Overridden method, calls the model and 
	 * updates the contract with impairments and PreferredInfo object.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		impSrc = NbaConstants.PREFERRED_SRC; 
		if (performingContractEvaluation()) { //SPR2652
			int partyIndex = 0;
			ArrayList al = getAllInsuredIndexes();
			OLifE oLifE = nbaTxLife.getOLifE();
			int insListCount = al.size();
			for(int i=0;i<insListCount;i++){
				partyIndex = ((Integer)al.get(i)).intValue();
				partyID = oLifE.getPartyAt(partyIndex).getId();
				prefQuestionnaireImpairments.clear();	
				prefQuestionnairePreferredScore.clear();  				
				isSuccess = callPrefQuestionnaireModel(i);
				if (!isSuccess){
					throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsConstants.AC_PRFQUESTIONNAIRE);	//SPR2652
				}
				getContractImpairments(partyID);
				ArrayList[] mergedLists = mergeImpairments(contractImpairments, prefQuestionnaireImpairments, new ArrayList(), new ArrayList());
				ArrayList arrMerged = mergedLists[0];
				addImpairmentInfo(partyID, arrMerged);
				updatePreferredInfoList(partyID,prefQuestionnairePreferredScore);
			}
		} else if (performingRequirementsEvaluation()) { //SPR2652
			setPartyID(work); 
			prefQuestionnaireImpairments.clear();	
			prefQuestionnairePreferredScore.clear();
			isSuccess = callPrefQuestionnaireModel();
			if (!isSuccess){
				throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsConstants.AC_PRFQUESTIONNAIRE);	//SPR2652  
			}
			getContractImpairments(partyID);
			ArrayList[] mergedLists = mergeImpairments(contractImpairments, prefQuestionnaireImpairments, new ArrayList(), new ArrayList());
			ArrayList arrMerged = mergedLists[0];
			addImpairmentInfo(partyID, arrMerged);
			updatePreferredInfoList(partyID,prefQuestionnairePreferredScore);
		}		
	}
   /**
	* This method is used to call the ACPrfQuestionnaire model	 
	* @param  insuredIndex: Index of the insured
	* @return boolean : Returns true if the call is successful
	* 					 Else returns false 	
	* @throws NbaBaseException
	*/	
    
	public boolean callPrefQuestionnaireModel(int insuredIndex) throws NbaBaseException{
		ArrayList results = null;
		boolean success = false;	//SPR2652
		if (updatePartyFilterInRequest(oinkRequest, partyID)) { //SPR2652
			HashMap deOinkMap = new HashMap();
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaTxLife);
			oinkData.setAcdbSource(new NbaAcdb(), nbaTxLife);
			oinkData.setLobSource(work.getNbaLob());
			NbaVpmsAdaptor vpmsProxy = null; //SPR3362
            try { //SPR3362
                vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.AC_PRFQUESTIONNAIRE); //SPR2652 SPR3362
                vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULTXML);
                // SPR2652 code deleted
                oinkRequest.setRelatedObjectTypeFilter(String.valueOf(NbaOliConstants.OLI_PARTY));
                oinkRequest.setArgs(getKeys());
                deOinkImpairmentFields(deOinkMap);
                deOinkProductCodeFields(deOinkMap);
                deOinkFormResponseFields(oinkData, oinkRequest, deOinkMap, insuredIndex);
                vpmsProxy.setSkipAttributesMap(deOinkMap);
                vpmsProxy.setANbaOinkRequest(oinkRequest);
                try {
                    NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
                    results = vpmsResultsData.getResultsData();
                    if (results == null) {
                        //SPR3362 code deleted
                        throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_PRFQUESTIONNAIRE); //SPR2652
                    } //SPR2652
                    vpmsResult = (String) results.get(0);
                    NbaVpmsModelResult vpmsOutput = new NbaVpmsModelResult(vpmsResult);
                    VpmsModelResult vpmModelResult = vpmsOutput.getVpmsModelResult();
                    prefQuestionnaireImpairments = vpmModelResult.getImpairmentInfo();
                    prefQuestionnairePreferredScore = vpmModelResult.getPreferredInfo();
                    success = true;
                    // SPR2652 code deleted
                    //SPR3362 code deleted
                } catch (RemoteException re) {
                    handleRemoteException(re, NbaVpmsAdaptor.AC_PRFQUESTIONNAIRE); //SPR2652
                    // SPR2652 code deleted
                }
            //begin SPR3362
            } finally {
                if (vpmsProxy != null) {
                    try {
                        vpmsProxy.remove();
                    } catch (RemoteException e) {
                        getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                    }
                }
            }
            //end SPR3362
		}
		return success;								
	 } 
   /**
	* This method is used to call the PrefQuestionnaireModel model from ReqEval Process	 
	* @param 
	* @return boolean : Returns true if the call is successful
	* 					 Else returns false 	
	* @throws NbaBaseException
	*/
	public boolean callPrefQuestionnaireModel() throws NbaBaseException {
		
 	    VpmsModelResult vpmsModelResult = null;
	    boolean success = true;
	    HashMap deOinkMap = new HashMap();
	    // SPR3290 code deleted
	    NbaVpmsAdaptor vpmsProxy = null; //SPR3362
	    try {
   		    NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaTxLife);
		    NbaOinkDataAccess accessContract =	new NbaOinkDataAccess(txLifeReqResult);
		    accessContract.setLobSource(work.getNbaLob());
		    accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
		    vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.AC_PRFQUESTIONNAIRE);	//SPR2652 SPR3362
		    vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULTXML);
		    Object [] args = getKeys();
		    oinkRequest.setArgs(args);	
		    deOinkImpairmentFields(deOinkMap);	
		    deOinkProductCodeFields(deOinkMap);
		    deOinkPrfUFPResponseScore(oinkData, oinkRequest, deOinkMap);
		    oinkRequest.setRequirementIdFilter(reqId);
		    vpmsProxy.setANbaOinkRequest(oinkRequest);
		    vpmsProxy.setSkipAttributesMap(deOinkMap);
		    NbaVpmsResultsData vpmsResultsData;
		    vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
		    vpmsResultsData.displayResultsData();
		    if (vpmsResultsData == null) {
				//SPR3362 code deleted
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_PRFQUESTIONNAIRE); //SPR2652
		    }	//SPR2652
		    String xmlString = (String) vpmsResultsData.getResultsData().get(0);
		    if (getLogger().isDebugEnabled()) {
			    getLogger().logDebug(vpmsResult);
		    }				
		    NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
		    vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
		    prefQuestionnairePreferredScore = vpmsModelResult.getPreferredInfo();
		    prefQuestionnaireImpairments = vpmsModelResult.getImpairmentInfo();
		    success = true;
			// SPR2652 Code Deleted
		    //SPR3362 code deleted
		// SPR2652 Code Deleted
	    } catch (RemoteException e) {	//SPR2652
		    throw new NbaBaseException("NbaVpmsException Exception occured in callPrefQuestionnaireModel",e);
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
   
         
	/**
    * This method gets all the Impairments deOink variables for ACPrfQuestionnaire model	 
    * @param Map deOinkMap
    * @return java.util.Map : The Hash Map containing all the deOink variables 	
    * @throws NbaBaseException
    */
    protected void deOinkImpairmentFields(Map deOinkMap) throws NbaBaseException{
	    ArrayList impariments = nbaTxLife.getImpairments(partyID);
	    int impCount = impariments.size(); 
	    // SPR3290 code deleted
	    deOinkMap.put("A_no_of_Impairments" , String.valueOf(impCount));
	    for (int i =0 ; i < impCount; i++ ){
			ImpairmentInfo  impairmentInfo =(ImpairmentInfo)impariments.get(i);
			String credit = String.valueOf(impairmentInfo.getCredit());
			String debit  = String.valueOf(impairmentInfo.getDebit());
			String impairmentStatus = String.valueOf(impairmentInfo.getImpairmentStatus());
			String impairmentPermFlatExtraAmt= impairmentInfo.getImpairmentPermFlatExtraAmt();
			String impairmentTempFlatExtraAmt = impairmentInfo.getImpairmentTempFlatExtraAmt();
		if(i == 0){
			deOinkMap.put("A_Debit_INS",convertToDefault(debit));
			deOinkMap.put("A_Credit_INS",convertToDefault(credit));
			deOinkMap.put("A_ImpairmentStatus_INS",impairmentStatus);
			deOinkMap.put("A_ImpairmentPermFlatExtraAmt_INS",convertToDefault(impairmentPermFlatExtraAmt));
			deOinkMap.put("A_ImpairmentTempFlatExtraAmt_INS",convertToDefault(impairmentTempFlatExtraAmt));			
		}else{
			deOinkMap.put("A_Debit_INS["+ i + "]",convertToDefault(debit));
			deOinkMap.put("A_Credit_INS["+ i + "]",convertToDefault(credit));
			deOinkMap.put("A_ImpairmentStatus_INS["+ i + "]",impairmentStatus);
			deOinkMap.put("A_ImpairmentPermFlatExtraAmt_INS["+ i + "]",convertToDefault(impairmentPermFlatExtraAmt));
			deOinkMap.put("A_ImpairmentTempFlatExtraAmt_INS["+ i + "]",convertToDefault(impairmentTempFlatExtraAmt));			
		}
	  }		    	    
	}
	/**
     * This method gets all the FormResponse deOink variables for ACPrfQuestionnaire model
     * @param Map deOink
     * @param NbaOinkDataAccess oinkdata
     * @param NbaOinkRequrst oinkRequest
     * @throws NbaBaseException
     */
	protected void deOinkFormResponseFields(NbaOinkDataAccess oinkData, NbaOinkRequest oinkRequest, Map deOink , int insuredIndex) throws NbaBaseException {
		if (updatePartyFilterInRequest(oinkRequest, partyID)) { //SPR2652
			String oinkVarName = "FormResponseCode_INS";
			// SPR2652 code deleted
			oinkRequest.setRelatedObjectTypeFilter(String.valueOf(NbaOliConstants.OLI_PARTY));
			oinkRequest.setVariable(oinkVarName);
			// SPR2652 code deleted
			String[] value = oinkData.getStringValuesFor(oinkRequest);
			if (value == null) {
				deOink.put("A_no_of_FormResponseCode", "0");

			} else {
				deOink.put("A_no_of_FormResponseCode", String.valueOf(value.length));

				for (int j = 0; j < value.length; j++) {
					if (j == 0)
						deOink.put("A_FormResponseCode", value[j]);
					else
						deOink.put("A_FormResponseCode" + "[" + j + "]", value[j]);
				}
			}
			// SPR2652 code deleted			 
		}
	}
	/**
	 * This method gets all the FormResponse deOink variables for ACPrfQuestionnaire model
	 * @param Map deOink 
	 * @param NbaOinkDataAccess oinkdata
	 * @param NbaOinkRequrst oinkRequest
	 * @throws NbaBaseException
	 */
	protected void deOinkPrfUFPResponseScore(NbaOinkDataAccess oinkData, NbaOinkRequest oinkRequest, Map deOink) throws NbaBaseException {
	  String score = "";	
	  Party party = nbaTxLife.getParty(partyID).getParty();
	  if (party != null){
  		PartyExtension partyExtension = NbaUtils.getFirstPartyExtension(party);
	  	if(partyExtension!=null){
	  		UnderwritingAnalysis uwa = partyExtension.getUnderwritingAnalysis();
	  		if (uwa != null && uwa.hasPreferredInfo()){
	  			score = String.valueOf(uwa.getPreferredInfo().getPrfUFPResponsesScore());
	  		}
	  	}
	  }
    }
	/**
     * This method gets all the ProductCode deOink variables for ACPrfQuestionnaire model
     * @param HashMap deOink
     * @throws NbaBaseException
     */		
	 protected void deOinkProductCodeFields(Map deOinkMap)throws NbaBaseException {
		ArrayList coverageList = nbaTxLife.getCoveragesFor(partyID);
		ArrayList productCodeList = new ArrayList();
		int count = coverageList.size();
		Coverage coverage = null;
		String productCode = null;
		for(int i=0;i<count;i++){
			coverage = (Coverage)coverageList.get(i);
			if(coverage!=null){
				productCode = coverage.getProductCode();
				if(productCode!=null && !productCode.equals("")){
					productCodeList.add(productCode);
				}
			}
		}
		int productCodeCount = productCodeList.size();
		deOinkMap.put("A_no_of_ProductCode_INS",String.valueOf(productCodeCount));
		if(productCodeCount==0){
			deOinkMap.put("A_ProductCode_INS","");
		}else{
			for(int i=0;i<productCodeCount;i++){
				if(i==0){
					deOinkMap.put("A_ProductCode_INS",(String)productCodeList.get(i));
				}else{
					deOinkMap.put("A_ProductCode_INS["+i+"]",(String)productCodeList.get(i));
				}
			}
		}
	}
	
	private String convertToDefault(String str) {
			if (str == null || str.equalsIgnoreCase("null")) {
				return "";
			}
			else if (str.equalsIgnoreCase("NaN")) {
				return "-1";
			}
			return str;
		}	
} 
