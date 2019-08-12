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
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.ac.DefaultValues;
import com.csc.fsg.nba.vo.ac.SummaryValues;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.SubstanceUsage;
import com.csc.fsg.nba.vo.txlife.SubstanceUsageExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
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
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>ACP014</td><td>Version 4</td><td>Changes for Inforce value calculation</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR2741</td><td>Version 6</td><td>Re-evaluation is generating insert errors</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACContractSummaryProcessor extends NbaVpmsModelProcessor {
	// begin AXAL3.7.07
	protected ArrayList impairments = new ArrayList();
	protected ArrayList accepImpairments = null;
	// end AXAL3.7.07
	/**
	 * Overridden method, calls the model and 
	 * updates the contract.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		//ACP002 begins
		// Loop over all Insureds
		if(performingContractEvaluation()){	//SPR2652
			int partyIndex = 0;
			ArrayList al = getAllInsuredIndexes();
			OLifE oLifE = nbaTxLife.getOLifE();
			for (int i=0;i<al.size();i++) {
				partyIndex = ((Integer)al.get(i)).intValue();
				String partyId = oLifE.getPartyAt(partyIndex).getId();
				isSuccess = processContractSummary(partyId, i);
				if (!isSuccess){
					throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.ACCONTRACTSUMMARY);	//SPR2652
				}
				 mergeImpairmentsAndAccep(impairments,accepImpairments); //AXAL3.7.07
			}
			//ACP002 ends
		}
	}
		
	/**
	 * This function acts as an entry point for calling the ACCONTRACTSUMMARY model	 
	 * @param partyId: partyId of the insured
	 * @param insuredIndex: Index of the insured
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */	
	//ACP002 new method.
	public boolean processContractSummary(String partyId, int insuredIndex) throws NbaBaseException {
	
		boolean success = false;	//SPR2652
		NbaAcdb acdb = new NbaAcdb(); //SPR2741 
		Object[] args = getKeys(partyId); //SPR2741 
		
		ArrayList results = null;
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaTxLife);
		oinkData.setAcdbSource(new NbaAcdb(), nbaTxLife);
		oinkData.setLobSource(work.getNbaLob());		
			
		partyID = partyId;
		
		getLogger().logDebug("########### Testing Contract Summary ###########");
		getLogger().logDebug("########### insuredIndex: " + insuredIndex);
		getLogger().logDebug("########### PartyId: " + partyId);
		NbaOinkRequest oinkRequest = new NbaOinkRequest(); //SPR2652
        oinkRequest.setArgs(args);  //AXAL3.7.07
		if (updatePartyFilterInRequest(oinkRequest, partyId)) { //SPR2652
		    //begin SPR3362
		    NbaVpmsAdaptor vpmsProxy = null;
            try {
                vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.ACCONTRACTSUMMARY); //SPR
                vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_CALCXMLOBJECTS);

                Map deOink = new HashMap();
                //			######## DEOINK
                deOink.put(NbaVpmsConstants.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639

                deOinkCoverageFields(oinkData, deOink, partyId, insuredIndex);
                deOinkCovOptionFields(oinkData, deOink, partyId);
                deOinkOptionNumberOfUnitsGIR(oinkData, deOink, partyId);
                deOinkSubstanceUsage(oinkData, deOink, partyId); //AXAL3.7.07
                deOinkXMLResultFields(deOink);
                deOinkInforceValues(deOink); //ACP014
                // SPR2652 code deleted
                vpmsProxy.setANbaOinkRequest(oinkRequest);
                vpmsProxy.setSkipAttributesMap(deOink);
                VpmsComputeResult vcr;
                vcr = vpmsProxy.getResults();
                NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vcr);
                results = vpmsResultsData.getResultsData();
                //Resulting string will be the zeroth element.
                if (results == null) {
                    throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.ACCONTRACTSUMMARY); //SPR2652
                }
                // SPR2652 code deleted
                vpmsResult = (String) results.get(0);
                NbaVpmsModelResult vpmsOutput = new NbaVpmsModelResult(vpmsResult);
                VpmsModelResult vpmModelResult = vpmsOutput.getVpmsModelResult();
                //begin SPR2741
                if (null != acdb.getSummaryValues(args)) {
                    updateSummaryValues(vpmModelResult.getSummaryValues());
                } else {
                    addSummaryValues(vpmModelResult.getSummaryValues());
                }
                if (null != acdb.getDefaultValues(args)) {
                    updateDefaultValues(vpmModelResult.getDefaultValues());
                } else {
                    addDefaultValues(vpmModelResult.getDefaultValues());
                }
                //Begin ALII1822
                int insuredcount = nbaTxLife.getPrimaryCoverage().getLifeParticipantCount();
				for (int i = 0; i < insuredcount; i++) {
					LifeParticipant lifeParticipant = nbaTxLife.getPrimaryCoverage().getLifeParticipantAt(i);
					if (lifeParticipant != null && lifeParticipant.getPartyID().equalsIgnoreCase(partyId)) {
						if (vpmModelResult.getSummaryValues().getSumSmkPremium() == NbaOliConstants.OLI_TOBPREMBASIS_SMOKER) {
							lifeParticipant.setSmokerStat(NbaOliConstants.OLI_TOBACCO_CURRENT);
						} else {
							lifeParticipant.setSmokerStat(NbaOliConstants.OLI_TOBACCO_NEVER);
						}
						lifeParticipant.setActionUpdate();

					}
				}
                //End ALII1822
            	impairments = vpmModelResult.getImpairmentInfo(); //AXAL3.7.07
                //end SPR2741
                success = true;
            } catch (RemoteException re) {
                handleRemoteException(re, NbaVpmsAdaptor.ACCONTRACTSUMMARY); //SPR2652
                // SPR2652 Code Deleted
            } finally {
                if (vpmsProxy != null) {
                    try {
                        vpmsProxy.remove();
                    } catch (Exception e) {
                        getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                    }
                }
            }
            //end SPR3362
		}
		return success;
	}
	//ACP002 new method
	private void deOinkCoverageFields(NbaOinkDataAccess oinkData, Map deOink, String partyId, int insuredIndex) throws NbaBaseException {
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		if(getLogger().isDebugEnabled()) { //SPR3290
		    getLogger().logDebug("insuredIndex for oinkRequest...." + insuredIndex);
		    getLogger().logDebug("PartyId for oinkRequest...." + partyId);
		} //SPR3290
		if (updatePartyFilterInRequest(oinkRequest, partyId)) { //SPR2652
			ArrayList coverages = nbaTxLife.getCoveragesFor(partyId);
			for (int i = 0; i < coverages.size(); i++) {
				Coverage cov = (Coverage) coverages.get(i);
				if(getLogger().isDebugEnabled()) { //SPR3290
				    getLogger().logDebug("Coverage: " + cov.getId());
				}//SPR3290
			}
			int covCount = coverages.size();
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug("covCount: " + covCount);
			}
			deOink.put("A_NO_OF_PRODUCTCODE_INS", String.valueOf(covCount));
			oinkRequest.setCount(covCount);
			String oinkVarName = "CurrentAmtByCoverage_INS";
			String deOinkVarName = "CurrentAmt_INS";
			oinkRequest.setVariable(oinkVarName);
			String[] value = oinkData.getStringValuesFor(oinkRequest);
			for (int j = 0; j < value.length; j++) {
			    if(getLogger().isDebugEnabled()) { //SPR3290
			        getLogger().logDebug(deOinkVarName + ": " + value[j]);
			    }//SPR3290
				//begin SPR2652
				if (j == 0) {
					deOink.put("A_" + deOinkVarName, value[j]);
				} else {
					deOink.put("A_" + deOinkVarName + "[" + j + "]", value[j]);
				//end SPR2652
				}
			}	//SPR2652
			oinkVarName = "ProductCode_INS";
			oinkRequest.setVariable(oinkVarName);
			value = oinkData.getStringValuesFor(oinkRequest);
			for (int j = 0; j < value.length; j++) {
			    if(getLogger().isDebugEnabled()) { //SPR3290
			        getLogger().logDebug(oinkVarName + ": " + value[j]);
			    } //SPR3290
				//begin SPR2652
				if (j == 0) {
					deOink.put("A_" + oinkVarName, value[j]);
				} else {
					deOink.put("A_" + oinkVarName + "[" + j + "]", value[j]);
				//end SPR2652
				}
			}	//SPR2652
			oinkVarName = "AnnualPremAmt_INS";
			oinkRequest.setVariable(oinkVarName);
			value = oinkData.getStringValuesFor(oinkRequest);
			for (int j = 0; j < value.length; j++) {
			    if(getLogger().isDebugEnabled()) { //SPR3290
			        getLogger().logDebug(oinkVarName + ": " + value[j]);
			    } //SPR3290
				//begin SPR2652
				if (j == 0) {
					deOink.put("A_" + oinkVarName, value[j]);
				} else {
					deOink.put("A_" + oinkVarName + "[" + j + "]", value[j]);
				//end SPR2652
				}
			}
		}  
	}

	//ACP002 new method
	private void deOinkCovOptionFields(NbaOinkDataAccess oinkData, Map deOink, String partyId) throws NbaBaseException {
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		ArrayList coverages = nbaTxLife.getCoveragesFor(partyId);

		String oinkVarName = "OptionAmtByCoverage_ADB";
		String deOinkVarName = "OptionAmt_ADB";
		oinkRequest.setVariable(oinkVarName);
		int count = 0;
		for (int i=0; i<coverages.size(); i++) {
			Coverage cov = (Coverage)coverages.get(i);
			String covId = cov.getId(); //SPR3290
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug("covId: " + covId);
			} //SPR3290
			oinkRequest.setCoverageIdFilter(covId);
			String[] value = oinkData.getStringValuesFor(oinkRequest);
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug(deOinkVarName + ": value.length: " + value.length);
			}//SPR3290
			for (int j =0; j<value.length; j++) {
			    if(getLogger().isDebugEnabled()) { //SPR3290
			        getLogger().logDebug("count="+ count + "  value=" + value[j]);
			    }//SPR3290
				if (count==0) deOink.put("A_"+deOinkVarName, value[j]);
				else deOink.put("A_"+deOinkVarName+"["+count+"]", value[j]);
				count++;
			}
		}
		if (count == 0) deOink.put("A_"+deOinkVarName, "0");
		deOink.put("A_No_Of_"+deOinkVarName, String.valueOf(count));
		if(getLogger().isDebugEnabled()) { //SPR3290
		    getLogger().logDebug("A_No_Of_"+deOinkVarName+": " + String.valueOf(count));
		} //SPR3290
		
		oinkVarName = "OptionAmtByCoverage_WP";
		deOinkVarName = "OptionAmt_WP";
		oinkRequest.setVariable(oinkVarName);
		count = 0;
		for (int i=0; i<coverages.size(); i++) {
			Coverage cov = (Coverage)coverages.get(i);
			String covId = cov.getId(); //SPR3290
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug("covId: " + covId);
			} //SPR3290
			oinkRequest.setCoverageIdFilter(covId);
			String[] value = oinkData.getStringValuesFor(oinkRequest);
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug(deOinkVarName + ": value.length: " + value.length);
			}//SPR3290
			for (int j =0; j<value.length; j++) {
			    if(getLogger().isDebugEnabled()) { //SPR3290
			        getLogger().logDebug("count="+ count + "  value=" + value[j]);
			    }//SPR3290
				if (count==0) deOink.put("A_"+deOinkVarName, value[j]);
				else deOink.put("A_"+deOinkVarName+"["+count+"]", value[j]);
				count++;
			}
		}
		if (count == 0) deOink.put("A_"+deOinkVarName, "0");
		deOink.put("A_No_Of_"+deOinkVarName, String.valueOf(count));
		if(getLogger().isDebugEnabled()) { //SPR3290
		    getLogger().logDebug("A_No_Of_"+deOinkVarName+": " + String.valueOf(count));
		} //SPR3290
		
		oinkVarName = "OptionAmtByCoverage_GIR";
		deOinkVarName = "OptionAmt_GIR";
		oinkRequest.setVariable(oinkVarName);
		count = 0;
		for (int i=0; i<coverages.size(); i++) {
			Coverage cov = (Coverage)coverages.get(i);
			String covId = cov.getId(); //SPR3290
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug("covId: " + covId);
			}//SPR3290
			oinkRequest.setCoverageIdFilter(covId);
			String[] value = oinkData.getStringValuesFor(oinkRequest);
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug(deOinkVarName + ": value.length: " + value.length);
			}//SPR3290
			for (int j =0; j<value.length; j++) {
			    if(getLogger().isDebugEnabled()) { //SPR3290
			        getLogger().logDebug("count="+ count + "  value=" + value[j]);
			    } //SPR3290
				if (count==0) deOink.put("A_"+deOinkVarName, value[j]);
				else deOink.put("A_"+deOinkVarName+"["+count+"]", value[j]);
				count++;
			}
		}
		if (count == 0) deOink.put("A_"+deOinkVarName, "0");
		deOink.put("A_No_Of_"+deOinkVarName, String.valueOf(count));
		if(getLogger().isDebugEnabled()) { //SPR3290
		    getLogger().logDebug("A_No_Of_"+deOinkVarName+": " + String.valueOf(count));
		}//SPR3290
	}

	//ACP002 new method
	private void deOinkOptionNumberOfUnitsGIR(NbaOinkDataAccess oinkData, Map deOink, String partyId) throws NbaBaseException {
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		String oinkVarName = "OptionNumberOfUnits_GIR";
		oinkRequest.setVariable(oinkVarName);
		oinkRequest.setCoverageFilter((int)NbaOliConstants.OLI_COVIND_BASE); // Base IndicatorCode
		String value = oinkData.getStringValueFor(oinkRequest);
		if (value.trim().equals("")) value="0";
		if(getLogger().isDebugEnabled()) { //SPR3290
		    getLogger().logDebug(oinkVarName + ": " + value);
		}//SPR3290
		deOink.put("A_"+oinkVarName, value);
	}
	/**
     * Insert default values into the database
     * @param defValue values retrieve from vpms model
     * @throws NbaDataAccessException
     */
    //ACP002 new method
	//SPR2741 added throws clause
    private void addDefaultValues(DefaultValues defValue) throws NbaDataAccessException {
        setDefValuesKeys(defValue);//SPR2741
        nbaAcdb.addDefaultValues(defValue);
    }
	/**
     * Insert summary values into the database 
     * @param sumValue values retrieve from vpms model
     * @throws NbaDataAccessException
     */
    //ACP002 new method
    //SPR2741 added throws clause
    private void addSummaryValues(SummaryValues sumValue) throws NbaDataAccessException {
        setSumValuesKeys(sumValue);//SPR2741
        nbaAcdb.addSummaryValues(sumValue);
    }
	
	/**
	 * This function acts as an entry point for calculating the deOink values
	 * required for Inforce value calculation	 
	 * @param Map: Map to carry deOink values
	 */	
	//ACP014 new method
	protected void deOinkInforceValues(Map deOinkMap){				
		ArrayList currentAmtInfList 	= new ArrayList();
		ArrayList optionAmtInfADBList 	= new ArrayList();
		ArrayList optionAmtInfWPList 	= new ArrayList();
		ArrayList currentAmtInfREPList 	= new ArrayList();
		
		Holding primaryHolding = nbaTxLife.getPrimaryHolding();
		OLifE olife = nbaTxLife.getOLifE();		
		ArrayList holdingList = new ArrayList();
		if(olife!=null){
			holdingList = olife.getHolding();
		}
		int listSize = holdingList.size();
		Holding otherHolding;		
		for(int i=0;i<listSize;i++){
			otherHolding = (Holding) holdingList.get(i);
			if(otherHolding!=primaryHolding){
				if(isReplacementHolding(primaryHolding,otherHolding)){
					//Populate CurrentAmtInfREPList if the holding is going to be 
					//replaced by the new contract
					currentAmtInfREPList = getCurrentAmt(otherHolding);					
				}else{
					//Populate CurrentAmtInfList, OptionAmtInfADBList and OptionAmtInfWPList 
					//if the holding is NOT going to be replaced by the new contract
					currentAmtInfList	= getCurrentAmt(otherHolding);										
					optionAmtInfADBList = getOptionAmt(otherHolding,NbaOliConstants.OLI_OPTTYPE_ADB);										
					optionAmtInfWPList 	= getOptionAmt(otherHolding,NbaOliConstants.OLI_OPTTYPE_WP);					
				}	
			}
		}
		//Populate the deOink values		
		populateInforceDeoinkMap(deOinkMap,"CurrentAmtInf_INS",currentAmtInfList);
		populateInforceDeoinkMap(deOinkMap,"OptionAmtInfADB_INS",optionAmtInfADBList);
		populateInforceDeoinkMap(deOinkMap,"OptionAmtInfWP_INS",optionAmtInfWPList);
		populateInforceDeoinkMap(deOinkMap,"CurrentAmtInfREP_INS",currentAmtInfREPList);		
	}
	/**
	 * This function populates the values from the ArrayList to deOink Map 
	 * @param Map: Map to carry deOink values
	 * @param String: Name of the atrribute(without A_)
	 * @param ArrayList: Array List carrying the values of the attribute
	 */	
	//ACP014 New Method
	protected void populateInforceDeoinkMap(Map deOinkMap,String variableName, ArrayList valueList){
		String attributeCounter = "A_no_of_" + variableName;
		String attributeName	= "A_" + variableName;
		String value = "";
		int count = 0;
		if(valueList!=null){
			count = valueList.size(); 
		}
		deOinkMap.put(attributeCounter, String.valueOf(count));
		if(count==0){
			deOinkMap.put(attributeName,"0");
		}
		for(int i=0;i<count;i++){
			value = (String) valueList.get(i);
			if(i==0){
				deOinkMap.put(attributeName, value);
			}else{
				deOinkMap.put(attributeName+"["+i+"]", value);
			}
		}	 
	}
	/**
	 * This function returns a boolean value indicating whether the holding is 
	 * a replacement holding 
	 * @param Holding: The Primary Holding
	 * @param Holding: The Other Holding
	 * @return boolean : Returns true if the Other Holding is replaced by the Primary Holding
	 * 					 Else returns false
	 */		
	//ACP014 New Method	
	protected boolean isReplacementHolding(Holding primaryHolding, Holding otherHolding){
		boolean isReplacement = false;
		OLifE olife = nbaTxLife.getOLifE();
		if(olife!=null){
			int relationCount = olife.getRelationCount();
			for(int i=0; i<relationCount ; i++){
				Relation relation = olife.getRelationAt(i);
				if(relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING
					&& relation.getRelatedObjectType() == NbaOliConstants.OLI_HOLDING
					&& relation.getOriginatingObjectID().equals(primaryHolding.getId())
					&& relation.getRelatedObjectID().equals(otherHolding.getId())
					&& relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_REPLACEDBY) {
											
					isReplacement = true;
					break;
				}
			}
		}
		return isReplacement;
	}
	/**
	 * This function returns an Array List of OptionAmt
	 * @param Holding: The holding from which the value need to be extracted
	 * @param long: Coverage Option Type
	 * @return ArrayList: Array List of Option Amounts
	 */	
	//ACP014 New Method
	protected ArrayList getOptionAmt(Holding holding,long covOptionType){
		ArrayList optionAmountList = new ArrayList();
		// SPR3290 code deleted
		List covOptionList = getCovOptionList(holding,covOptionType); // SPR3290
		int listSize = covOptionList.size();
		CovOption covOption = null;
		for(int i=0;i< listSize;i++){
			covOption = (CovOption) covOptionList.get(i);
			if(covOption!= null && !Double.isNaN(covOption.getOptionAmt())){				
				optionAmountList.add(String.valueOf(covOption.getOptionAmt()));
			}	
		}
		return optionAmountList;
	}
	/**
	 * This function returns an Array List of CurrentAmt
	 * @param Holding: The holding from which the value need to be extracted	 
	 * @return ArrayList: Array List of Current Amounts
	 */	
	//ACP014 New Method
	protected ArrayList getCurrentAmt(Holding holding){
		ArrayList currentAmountList = new ArrayList();
		// SPR3290 code deleted
		List coverageList = getCoverageForParty(holding); // SPR3290
		int listSize = coverageList.size();
		Coverage coverage = null;
		for(int i=0;i< listSize;i++){
			coverage = (Coverage) coverageList.get(i);
			if(coverage!=null && !Double.isNaN(coverage.getCurrentAmt())){				
				currentAmountList.add(String.valueOf(coverage.getCurrentAmt()));
			}	
		}
		return currentAmountList;
	}
	/**
	 * This function returns an Array List of covOption objects
	 * @param Holding: The holding from which the value need to be extracted
	 * @param long: Coverage Option Type
	 * @return ArrayList: Array List of covOptions
	 */	
	//ACP014 New Method	
	public ArrayList getCovOptionList(Holding holding, long covOptionType){
		ArrayList covOptions = new ArrayList();
		ArrayList coverageList = new ArrayList();
		Life life = getLifeFromHolding(holding);
		if(life!=null){
			coverageList = getCoverageForParty(holding);
			int sizeCoverage = coverageList.size();
			for (int i = 0; i < sizeCoverage; i++) {
				Coverage coverage = (Coverage) coverageList.get(i);
				int sizeCovOption = coverage.getCovOptionCount();
				for (int j = 0; j < sizeCovOption; j++) {
					CovOption covOption = coverage.getCovOptionAt(j);
					if (covOption.getLifeCovOptTypeCode() == covOptionType && !covOption.isActionDelete()) {
						covOptions.add(covOption);
					}
				}
			}
		}
		return covOptions;
	}
	/**
	 * This function returns an Array List of coverage objects for the current insured
	 * @param Holding: The holding from which the value need to be extracted	
	 * @return ArrayList: Array List of Coverages
	 */	
	//ACP014 New Method	
	public ArrayList getCoverageForParty(Holding holding){		
		ArrayList coverageList = new ArrayList();		
		Life life = getLifeFromHolding(holding);
		if(life!=null){
			int sizeCoverage = life.getCoverageCount();
			int sizeLifeParticipant = 0;
			LifeParticipant lifeParticipant = null;
			ArrayList lifeParticipantList = null;
			for (int i = 0; i < sizeCoverage; i++) {				
				Coverage coverage = life.getCoverageAt(i);
				if (coverage.isActionDelete()) {
					continue;
				}
				lifeParticipantList = coverage.getLifeParticipant();
				sizeLifeParticipant = lifeParticipantList.size();
				for(int j=0;j<sizeLifeParticipant;j++){
					lifeParticipant = (LifeParticipant)lifeParticipantList.get(j);
					if(lifeParticipant.getPartyID().equals(partyID)){
						coverageList.add(coverage);
						break;			
					}
				}	
			}
		}
		return coverageList;	
	}
	/**
	 * This function retrieves the Life object from the Holding object
	 * @param Holding: The holding from which the value need to be extracted	
	 * @return Life: The Life object
	 */	
	//ACP014 New Method	
	protected Life getLifeFromHolding(Holding holding){
		Life life = null;		
		if(holding!=null){
			Policy policy = holding.getPolicy();
			if(policy!=null){
				Object obj = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
				if(obj!=null){
					life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
				}
			}
		}
		return life;
	}
	/**
     * Updates default values in acdb database
     * @param defValue Default values retrieve from vpms model
     * @throws NbaDataAccessException
     */
    //SPR2741 New Method
    private void updateDefaultValues(DefaultValues defValue) throws NbaDataAccessException {
        setDefValuesKeys(defValue);//SPR2741
        nbaAcdb.updateDefaultValues(defValue);
    }

    /**
     * Updates summary values in acdb database
     * @param sumValue summary values retrieve from vpms model
     * @throws NbaDataAccessException
     */
    //SPR2741 New Method
    private void updateSummaryValues(SummaryValues sumValue) throws NbaDataAccessException {
        setSumValuesKeys(sumValue); //SPR2741
        copyPreviousSummaryValues(sumValue);  //AXAL3.7.07
        nbaAcdb.updateSummaryValues(sumValue);
    }
    
	//AXAL3.7.07 new method.
	private void copyPreviousSummaryValues(SummaryValues sumValue) {
		// select the saved object from database
		String args[] = new String[4];
		args[0] = sumValue.getParentIdKey();
		args[1] = sumValue.getContractKey();
		args[2] = sumValue.getCompanyKey();
		args[3] = sumValue.getBackendKey();
		SummaryValues prevSumValue = new NbaAcdb().getSummaryValues(args);
		// copy prior requirement values to new Object
		sumValue.setSumNicotineTxt(prevSumValue.getSumNicotineTxt());
		sumValue.setSumNicotineCnt(prevSumValue.getSumNicotineCnt());
		sumValue.setSumNicotineAvg(prevSumValue.getSumNicotineAvg());
		sumValue.setSumHivTxt(prevSumValue.getSumHivTxt());
		sumValue.setSumChrolCnt(prevSumValue.getSumChrolCnt());
		sumValue.setSumChrolDate(prevSumValue.getSumChrolDate());
		sumValue.setSumHdlChrolCnt(prevSumValue.getSumHdlChrolCnt());
		sumValue.setSumHdlChrolDate(prevSumValue.getSumHdlChrolDate());
		sumValue.setSumHdlChlCnt(prevSumValue.getSumHdlChlCnt());
		sumValue.setSumHdlChlDate(prevSumValue.getSumHdlChlDate());
		sumValue.setSumCotinineSal(prevSumValue.getSumCotinineSal());
		sumValue.setSumDbsChrolTxt(prevSumValue.getSumDbsChrolTxt());
		sumValue.setSumDbsChrolDate(prevSumValue.getSumDbsChrolDate());
		sumValue.setSumPulseRestIrrInd (prevSumValue.getSumPulseRestIrrInd ());
		sumValue.setSumPulseExerciseIrrInd(prevSumValue.getSumPulseExerciseIrrInd());
		sumValue.setSumPulsePostExerciseIrrInd (prevSumValue.getSumPulsePostExerciseIrrInd ());
		sumValue.setSumPulseRest(prevSumValue.getSumPulseRest());
		sumValue.setSumPulseRestCnt(prevSumValue.getSumPulseRestCnt());
		sumValue.setSumPulseExercise(prevSumValue.getSumPulseExercise());
		sumValue.setSumPulseExerciseCnt(prevSumValue.getSumPulseExerciseCnt());
		sumValue.setSumPulsePostExercise(prevSumValue.getSumPulsePostExercise());
		sumValue.setSumPulsePostExerciseCnt(prevSumValue.getSumPulsePostExerciseCnt());
		sumValue.setSumExamNormalAppearance(prevSumValue.getSumExamNormalAppearance());
		sumValue.setSumBpAvgSystolic(prevSumValue.getSumBpAvgSystolic());
		sumValue.setSumBpAvgSystolicCnt(prevSumValue.getSumBpAvgSystolicCnt());
		sumValue.setSumBpAvgDiastolic(prevSumValue.getSumBpAvgDiastolic());
		sumValue.setSumBpAvgDiastolicCnt(prevSumValue.getSumBpAvgDiastolicCnt());
		sumValue.setSumBpHighSystolic(prevSumValue.getSumBpHighSystolic());
		sumValue.setSumBpHighDiastolic(prevSumValue.getSumBpHighDiastolic());
		sumValue.setSumTvcDataInd (prevSumValue.getSumTvcDataInd ());
		sumValue.setSumTvcPrFvcAct(prevSumValue.getSumTvcPrFvcAct());
		sumValue.setSumTvcPrFvcActCnt(prevSumValue.getSumTvcPrFvcActCnt());
		sumValue.setSumTvcPrFevAct(prevSumValue.getSumTvcPrFevAct());
		sumValue.setSumTvcPrFevActCnt(prevSumValue.getSumTvcPrFevActCnt());
		sumValue.setSumTvcPoFvcAct(prevSumValue.getSumTvcPoFvcAct());
		sumValue.setSumTvcPoFvcActCnt(prevSumValue.getSumTvcPoFvcActCnt());
		sumValue.setSumTvcPoFevAct(prevSumValue.getSumTvcPoFevAct());
		sumValue.setSumTvcPoFevActCnt(prevSumValue.getSumTvcPoFevActCnt());
		sumValue.setSumTvcPrFvcPec(prevSumValue.getSumTvcPrFvcPec());
		sumValue.setSumTvcPrFvcPecCnt(prevSumValue.getSumTvcPrFvcPecCnt());
		sumValue.setSumTvcPoFvcPec(prevSumValue.getSumTvcPoFvcPec());
		sumValue.setSumTvcPoFvcPecCnt(prevSumValue.getSumTvcPoFvcPecCnt());
		sumValue.setSumTvcPoFevPec(prevSumValue.getSumTvcPoFevPec());
		sumValue.setSumTvcPoFevPecCnt(prevSumValue.getSumTvcPoFevPecCnt());
		sumValue.setSumTvcPreInter(prevSumValue.getSumTvcPreInter());
		sumValue.setSumTvcPosInter(prevSumValue.getSumTvcPosInter());
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
						//ALS3830 start
						varName = "A_CurrentUseType_INS";
						if (i == 0) {
							deOink.put(varName, Long.toString(substanceUsageExtension.getCurrentUseType()));
						} else {
							deOink.put(varName + "[" + i + "]", Long.toString(substanceUsageExtension.getCurrentUseType()));
						}
						//ALS3830 end.
					}
				}
			}
		}
	}
	
}
