package com.csc.fsg.nba.business.process.evaluation;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Class that will take care of the processing once acNonMedicalHistory model is invoked 
 * from NBCTEVAL and NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR3329</td><td>Version 7</td><td>Prevent erroneous "Retrieve variable name is invalid" messages from being generated by OINK</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3002</td><td>Version 8</td><td>Impairment Message "Insurance is pending" not generated</td></tr>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 *  * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACNonMedicalHistoryProcessor extends NbaVpmsModelProcessor {

	protected ArrayList nonMedicalImpairments = new ArrayList();//ACN024
	protected ArrayList accepImpairments = null;	

	/**
	 * Overridden method, calls the model and 
	 * updates the contract with impairments.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		impSrc = NbaConstants.NONMEDICALHISTORY_SRC; //ACN016
		if (performingContractEvaluation()) { //SPR2652
			int partyIndex = 0;
			ArrayList al = getAllInsuredIndexes();
			OLifE oLifE = nbaTxLife.getOLifE();
			//int listCount = 0; //ACN024
			int insuredCount = al.size();

			for(int i=0;i<insuredCount;i++){
				isSuccess = false;
				partyIndex = ((Integer)al.get(i)).intValue();
				String partyId = oLifE.getPartyAt(partyIndex).getId();
				partyID = partyId;							
				//listCount = nonMedicalImpairments.size(); //ACN024
				nonMedicalImpairments.clear();
				
				isSuccess = callNonMedicalHistoryModel(i);
				if (!isSuccess){
					throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.AC_NON_MEDICAL_HISTORY);	//SPR2652
				}
				getContractImpairments(partyId);
				ArrayList[] mergedLists = mergeImpairments(contractImpairments, nonMedicalImpairments, new ArrayList(), new ArrayList());
				ArrayList arrMerged = mergedLists[0];								
				addImpairmentInfo(partyId, arrMerged);	

			}
		} else if (performingRequirementsEvaluation()) { //SPR2652
			    setPartyID(work); //ACN024
				isSuccess = callNonMedicalHistoryModel();
				if (!isSuccess) {
					throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.AC_NON_MEDICAL_HISTORY);	//SPR2652
				}
				   //Do the Impairments Merging //ACN024
				  mergeImpairmentsAndAccep(nonMedicalImpairments,accepImpairments); //ACN016							
			}
	}
	
	/**
	 * This function acts as an entry point for calling the ACNONMEDICALHISTORY model
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */
	//ACP009 New Method
	public boolean callNonMedicalHistoryModel() throws NbaBaseException{
		boolean success = false;
		int relationCode = work.getNbaLob().getReqPersonCode();
		int personSeq = 0;
		String partyID1122 = txLifeReqResult.getPartyId(relationCode,String.valueOf(personSeq));
		Party party = txLifeReqResult.getParty(partyID1122).getParty();
		Risk risk = null;
		ArrayList elementList = null;
		int count = 0;
		if(party!=null){
			risk = party.getRisk();
			if(risk!=null){
				boolean isVPMScalled = false; //SPR3002
				//Call the VPMS model for each Life Style Activity Object
				elementList = risk.getLifeStyleActivity();
				count = elementList.size();
				for(int i=0; i< count; i++){					
					success = getNonMedModelResults(NbaOliConstants.OLI_LIFESTYLEACTIVITY, i);
					isVPMScalled = true; //SPR3002
					if(!success){
						return success;
					}
				}
				//Call the VPMS model for each Substance Usage Object
				elementList = risk.getSubstanceUsage();
				count = elementList.size();
				for(int i=0; i< count; i++){					
					success = getNonMedModelResults(NbaOliConstants.OLI_SUBSTANCEUSAGE, i);
					isVPMScalled = true; //SPR3002
					if(!success){
						return success;
					}
				}
				//Call the VPMS model for each Criminal Conviction Object
				elementList = risk.getCriminalConviction();
				count = elementList.size();
				for(int i=0; i< count; i++){					
					success = getNonMedModelResults(NbaOliConstants.OLI_CRIMCONVICTION, i);
					isVPMScalled = true; //SPR3002
					if(!success){
						return success;
					}
				}
				//Call the VPMS model for each Violation Object
				elementList = risk.getViolation();
				count = elementList.size();
				for(int i=0; i< count; i++){					
					success = getNonMedModelResults(NbaOliConstants.OLI_VIOLATION, i);
					isVPMScalled = true; //SPR3002
					if(!success){
						return success;
					}
				}
				//begin SPR3002
				//Check to see if the VP/MS model was called for any risk objects. If not,
				//call the model to calculate the indicators such as AppPendingInd that may be
				//present outside of the risk objects.
				if (!isVPMScalled) {
					success = getNonMedModelResults(NbaOliConstants.OLI_TC_NULL, 0);
					if (!success) {
						return success;
					}
				}
				//end SPR3002
			}	
		}
		
		return true;
	}
		
	/**
	 * This function calls the ACNONMEDICALHISTORY model
	 * @param relatedObjectType: Type of the Related Object for Form Instance
	 * @param elementIndex: Index of the Related object
	 * @return  Returns true if the call is successful
	 * 					 Else returns false 
	 * @throws NbaBaseException
	 */
	//ACP009 New Method
	public boolean getNonMedModelResults(long relatedObjectType, int elementIndex) throws NbaBaseException{
		VpmsModelResult vpmsModelResult = null;
		boolean success = false;
		ArrayList tempImpairmentList = null;
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess accessContract =	new NbaOinkDataAccess(txLifeReqResult);
			accessContract.setLobSource(work.getNbaLob());
			accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
			accessContract.setContractSource(nbaTxLife);//SPR3329
			vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.AC_NON_MEDICAL_HISTORY); //SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);
			
			Object [] args = getKeys();
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setArgs(args);
			oinkRequest.setRelatedObjectTypeFilter(String.valueOf(relatedObjectType));
			oinkRequest.setElementIndexFilter(elementIndex);
			Map deOink = getNonMedDeOINKValues(relatedObjectType,elementIndex);
			deOinkContractFieldsForRequirement(deOink);
			vpmsProxy.setSkipAttributesMap(deOink);
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			if (vpmsResultsData == null) {
				//SPR3362 code deleted
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_NON_MEDICAL_HISTORY); //SPR2652
			} //SPR2652
			String xmlString = (String) vpmsResultsData.getResultsData().get(0);
			if(getLogger().isDebugEnabled()){				
				getLogger().logDebug("Results from VPMS Model: "+NbaVpmsAdaptor.AC_NON_MEDICAL_HISTORY);
				getLogger().logDebug(xmlString);
			}
			NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
			vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
			tempImpairmentList = vpmsModelResult.getImpairmentInfo();  
			if(tempImpairmentList!=null && tempImpairmentList.size() != 0){
				nonMedicalImpairments.addAll(tempImpairmentList); 
			// SPR2652 Code Deleted
			success = true;			
			}
			//SPR3362 code deleted
		// SPR2652 Code Deleted
		} catch (RemoteException e) {	//SPR2652
			handleRemoteException(e, NbaVpmsAdaptor.AC_NON_MEDICAL_HISTORY); //SPR2652
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
	 * This function acts as an entry point for calling the ACNONMEDICALHISTORY model	 
	 * @param partyIndex: Index of the insured
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */
	//ACP009 New Method
	public boolean callNonMedicalHistoryModel(int partyIndex) throws NbaBaseException{
		boolean success = false;
		// SPR3290 code deleted	
		Party party = nbaTxLife.getParty(partyID).getParty();
		Risk risk = null;
		List elementList = null; // SPR3290
		int count = 0;
		if(party!=null){
			risk = party.getRisk();
			//APSL3761 Start
			boolean isVPMScalled = false; //SPR3002
			if(risk!=null){
				
				//Call the VPMS model for each Life Style Activity Object
				elementList = risk.getLifeStyleActivity();
				count = elementList.size();
				for(int i=0; i< count; i++){					
					success = getNonMedModelResults(partyIndex, NbaOliConstants.OLI_LIFESTYLEACTIVITY, i);
					isVPMScalled = true; //SPR3002
					if(!success){
						return success;
					}
				}
				//Call the VPMS model for each Substance Usage Object
				elementList = risk.getSubstanceUsage();
				count = elementList.size();
				for(int i=0; i< count; i++){					
					success = getNonMedModelResults(partyIndex, NbaOliConstants.OLI_SUBSTANCEUSAGE, i);
					isVPMScalled = true; //SPR3002
					if(!success){
						return success;
					}
				}
				//Call the VPMS model for each Criminal Conviction Object
				elementList = risk.getCriminalConviction();
				count = elementList.size();
				for(int i=0; i< count; i++){					
					success = getNonMedModelResults(partyIndex, NbaOliConstants.OLI_CRIMCONVICTION, i);
					isVPMScalled = true; //SPR3002
					if(!success){
						return success;
					}
				}
				//Call the VPMS model for each Violation Object
				elementList = risk.getViolation();
				count = elementList.size();
				for(int i=0; i< count; i++){					
					success = getNonMedModelResults(partyIndex, NbaOliConstants.OLI_VIOLATION, i);
					isVPMScalled = true; //SPR3002
					if(!success){
						return success;
					}
				}
				
				//end SPR3002
			}
			
			//begin SPR3002
            //Check to see if the VP/MS model was called for any risk objects. If not,
            //call the model to calculate the indicators such as AppPendingInd that may be
            //present outside of the risk objects.
            if (!isVPMScalled) {
                success = getNonMedModelResults(partyIndex, NbaOliConstants.OLI_TC_NULL, 0);
                if (!success) {
                    return success;
                }
            }
            //APSL3761 End
		}
		return true;
	}
	
	/**
	 * This function calls the ACNONMEDICALHISTORY model
	 * @param partyIndex: Index of the insured
	 * @param relatedObjectType: Type of the Related Object for Form Instance
	 * @param elementIndex: Index of the Related object
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */	
	//ACP009 New Method
	public boolean getNonMedModelResults(int partyIndex, long relatedObjectType, int elementIndex) throws NbaBaseException{
		VpmsModelResult vpmsModelResult = null;
		boolean success = false;
		NbaOinkRequest oinkRequest = new NbaOinkRequest(); //SPR2652
		if (updatePartyFilterInRequest(oinkRequest, partyID)) { //SPR2652
			ArrayList tempImpairmentList = null;
			NbaVpmsAdaptor vpmsProxy = null; //SPR3362
			try {
				NbaOinkDataAccess accessContract = new NbaOinkDataAccess(nbaTxLife);
				accessContract.setLobSource(work.getNbaLob());
				accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
				accessContract.setContractSource(nbaTxLife);//SPR3329
				vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.AC_NON_MEDICAL_HISTORY); //SPR3362
				vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);

				Object[] args = getKeys();
				oinkRequest.setArgs(args);
				// SPR2652 code deleted
				oinkRequest.setRelatedObjectTypeFilter(String.valueOf(relatedObjectType));
				oinkRequest.setElementIndexFilter(elementIndex);
				Map deOink = getNonMedDeOINKValues(partyIndex, relatedObjectType, elementIndex, partyID);	//SPR2652
				//begin AXAL3.7.07
				deOinkAgentInfo(deOink, accessContract);  
				deOinkOwnerInfo(deOink);  
				deOinkFormInstance(deOink);  
				//end AXAL3.7.07
				//Begin ALII1299 QC8469
            	ArrayList coverageList = nbaTxLife.getCoveragesFor(partyID);
            	if(!coverageList.isEmpty()){
            		deOink.put("A_LIFECOVTYPECODE", String.valueOf(((Coverage) coverageList.get(0)).getLifeCovTypeCode())); 
            	}
            	//End ALII1299 QC8469
          		//APSL3761 EndImpairement Change
                Iterator itr = coverageList.iterator();
                while (itr.hasNext()) {
                    Coverage coverage = (Coverage) itr.next();
                    if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_RIDER && coverage.getLifeCovTypeCode() == NbaOliConstants.OLI_COVTYPE_CHILDTERM) {
                        List lifeParticipantList = coverage.getLifeParticipant();
                        Iterator lpItr = lifeParticipantList.iterator();
                        while (lpItr.hasNext()) {
                            LifeParticipant lParticipant = (LifeParticipant) lpItr.next();
                            if (lParticipant.getLifeParticipantRoleCode() == NbaOliConstants.OLI_PARTICROLE_DEP) {
                                String partyId = lParticipant.getPartyID();
                                if (null != nbaTxLife.getParty(partyId)) {
                                    Relation relation = nbaTxLife.getRelationForRoleAndRelatedId(NbaOliConstants.OLI_PARTICROLE_40, partyId);
                                    deOink.put("A_RELDESC_CTR", String.valueOf(relation.getRelationDescription()));
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
                
                //APSL3761 End :: Impairement Change
				vpmsProxy.setSkipAttributesMap(deOink);
				vpmsProxy.setANbaOinkRequest(oinkRequest);
				NbaVpmsResultsData vpmsResultsData;
				vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
				if (vpmsResultsData == null || vpmsResultsData.getResultsData() == null) {	//SPR3290
					//SPR3362 code deleted
					throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_NON_MEDICAL_HISTORY); //SPR2652
				} //SPR2652
				String xmlString = (String) vpmsResultsData.getResultsData().get(0);
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Results from VPMS Model: " + NbaVpmsAdaptor.AC_NON_MEDICAL_HISTORY);
					getLogger().logDebug(xmlString);
				}
				NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
				vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
				tempImpairmentList = vpmsModelResult.getImpairmentInfo();
				if (tempImpairmentList != null && tempImpairmentList.size() != 0) {
					nonMedicalImpairments.addAll(tempImpairmentList);
				}
				success = true;
				// SPR2652 code deleted
				//SPR3362 code deleted
				// SPR2652 Code Deleted
			} catch (RemoteException e) { //SPR2652
				handleRemoteException(e, NbaVpmsAdaptor.AC_NON_MEDICAL_HISTORY); //SPR2652
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
		}
		return success;
	}



}
