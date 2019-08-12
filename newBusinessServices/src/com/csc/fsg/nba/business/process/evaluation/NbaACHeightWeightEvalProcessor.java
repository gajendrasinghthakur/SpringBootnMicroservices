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
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.nbaschema.RequirementControlSource;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Class that will do the processing once ACHeightWeightEval model is invoked 
 * from NBCTEVAL or NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACP007</td><td>Version 4</td><td>Medical Screening</td></tr>
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACHeightWeightEvalProcessor extends NbaVpmsModelProcessor {

	protected ArrayList heightWeightImpairments = new ArrayList();
	protected ArrayList heightWeightAccepImpairments = null;
	protected RequirementControlSource rcs = null;

	/**
	 * Overridden method, calls the model and 
	 * updates the contract with impairments.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		impSrc = NbaConstants.HEIGHTWEIGHTEVAL_SRC;//ACN016
		NbaSource src = work.getRequirementControlSource();
		NbaXMLDecorator dec = null;
		if(src != null) {
			dec = new NbaXMLDecorator(src.getText());
		} 
		if(dec != null) {
			rcs = dec.getRequirementControlSource();
		}		
		if (performingContractEvaluation()) { //SPR2652
 			int partyIndex = 0;
			ArrayList al = getAllInsuredIndexes();
			OLifE oLifE = nbaTxLife.getOLifE();
			int insListCount = al.size();
			String partyId = "";
			for(int i=0;i<insListCount;i++){
				partyIndex = ((Integer)al.get(i)).intValue();
				partyId = oLifE.getPartyAt(partyIndex).getId();
				partyID = partyId;
				heightWeightImpairments.clear();							
				isSuccess = callHeightWeightEvalModel(partyId, i);
				if (!isSuccess){
					throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.ACHEIGHTWEIGHTEVAL);	//SPR2652 
				}
				getContractImpairments(partyId);
				ArrayList[] mergedLists = mergeImpairments(contractImpairments, heightWeightImpairments, new ArrayList(), new ArrayList());
				ArrayList arrMerged = mergedLists[0];
				addImpairmentInfo(partyId, arrMerged);						
			}
		}
		else if (performingRequirementsEvaluation()) { //SPR2652
			setPartyID(work); //ACN024
			isSuccess = callHeightWeightEvalModelForReqEval();
			if(!isSuccess){
				throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.ACHEIGHTWEIGHTEVAL);	//SPR2652
			}
			//Do the Impairments Merging //ACN024
		   	mergeImpairmentsAndAccep(heightWeightImpairments,heightWeightAccepImpairments); //ACN016			
		}
	}

	/**
	 * This function acts as an entry point for calling the ACHEIGHTWEIGHTEVAL model from RequirementEvaluation automated process. 
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */	
	public boolean callHeightWeightEvalModelForReqEval() throws NbaBaseException {
		boolean success = false;
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(txLifeReqResult); //ACN009
			oinkData.setAcdbSource(new NbaAcdb(), nbaTxLife);
			oinkData.setLobSource(work.getNbaLob());
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug("########### Testing HeightWeightEval ###########");
			}//SPR3290
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.ACHEIGHTWEIGHTEVAL); //SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);
			Object[] args = getKeys();
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setRequirementIdFilter(reqId);
			oinkRequest.setArgs(args);
			Map deOink = new HashMap();
			//			######## DEOINK
			deOink.put(NbaVpmsConstants.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
			deOinkContractFieldsForRequirement(deOink);
			deOinkRequirementFields(deOink, oinkRequest);

			vpmsProxy.setANbaOinkRequest(oinkRequest);
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult vcr = vpmsProxy.getResults();
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vcr);
			ArrayList results = vpmsResultsData.getResultsData();
			results = vpmsResultsData.getResultsData();
			//Resulting string will be the zeroth element.
			if (results == null) {
				//SPR3362 code deleted
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.ACHEIGHTWEIGHTEVAL); //SPR2652
			} //SPR2652
			vpmsResult = (String)results.get(0);
			NbaVpmsModelResult vpmsOutput = new NbaVpmsModelResult(vpmsResult);
			VpmsModelResult vpmModelResult = vpmsOutput.getVpmsModelResult();
			heightWeightImpairments = vpmModelResult.getImpairmentInfo();
			success = true;
			// SPR2652 code deleted
			//SPR3362 code deleted
		} catch (RemoteException re) {
			handleRemoteException(re, NbaVpmsAdaptor.ACHEIGHTWEIGHTEVAL); //SPR2652
			// SPR2652 code deleted
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
	 * This function acts as an entry point for calling the ACHEIGHTWEIGHTEVAL model from ContractEvaluation automated process. 
	 * @param partyId: partyId of the insured
	 * @param insuredIndex: Index of the insured
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */	
	public boolean callHeightWeightEvalModel(String partyId, int insuredIndex) throws NbaBaseException {
		
		boolean success = true;
		ArrayList results = null;
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaTxLife);
		oinkData.setAcdbSource(new NbaAcdb(), nbaTxLife);
		oinkData.setLobSource(work.getNbaLob());				
		partyID = partyId;
		NbaOinkRequest oinkRequest = new NbaOinkRequest(); //SPR2652	
		if (updatePartyFilterInRequest(oinkRequest, partyId)) { //SPR2652	
			NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.ACHEIGHTWEIGHTEVAL);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);

			Map deOink = new HashMap();

			NbaACUtils.deOinkRequirementFields(deOink, partyID, rcs, nbaTxLife);
			// SPR2652 code deleted
			oinkRequest.setArgs(getKeys());

			vpmsProxy.setANbaOinkRequest(oinkRequest);
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult vcr;
			try {
				vcr = vpmsProxy.getResults();
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vcr);
				results = vpmsResultsData.getResultsData();
				//Resulting string will be the zeroth element.
				if (results == null) {
					//SPR3362 code deleted
					throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.ACHEIGHTWEIGHTEVAL); //SPR2652
				}
				//SPR2652
				vpmsResult = (String) results.get(0);
				NbaVpmsModelResult vpmsOutput = new NbaVpmsModelResult(vpmsResult);
				VpmsModelResult vpmModelResult = vpmsOutput.getVpmsModelResult();
				heightWeightImpairments = vpmModelResult.getImpairmentInfo(); //ACN024
				success = true;
				// SPR2652 code deleted
				//SPR3362 code deleted
			} catch (RemoteException re) {
				handleRemoteException(re, NbaVpmsAdaptor.ACHEIGHTWEIGHTEVAL); //SPR2652
				// SPR2652 code deleted
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
		}	//SPR2652
		return success;
	}
	
	private void deOinkRequirementFields(Map deOink, NbaOinkRequest oinkRequest) throws NbaBaseException {
		NbaOinkDataAccess accessContract = new NbaOinkDataAccess(nbaTxLife);
		//Get ReqCodeList
		oinkRequest.setVariable("ReqCodeList");
		String[] codeList = accessContract.getStringValuesFor(oinkRequest);
		int count = codeList.length;
		deOink.put("A_no_of_RequirementList_INS", (new Integer(count)).toString());
		if (count == 0) {
			deOink.put("A_RequirementList_INS", "");
		} else {
			for (int i = 0; i < count; i++) {
				if (i == 0) {
					deOink.put("A_RequirementList_INS", codeList[i]);
				} else {
					deOink.put("A_RequirementList_INS[" + i + "]", codeList[i]);
				}
			}
		}
	}	
}
