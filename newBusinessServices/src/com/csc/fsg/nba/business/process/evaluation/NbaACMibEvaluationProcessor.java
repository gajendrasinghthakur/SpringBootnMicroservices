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

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.txlife.AssociatedResponseData;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.FormResponse;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PriorName;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;


/**
 * Class that will take care of the processing once ACMIBEvalaution model is invoked 
 * from NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACP006</td><td>Version 4</td><td>MIB Evaluation</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR2804</td><td>Version 5</td><td>REQEVAL process error stops as it fails to process results from ACMIBEVALUATION vpms model</td><tr>
 * <tr><td>SPR2955</td><td>Version 6</td><td>Requirement Evaluation automated process is erroring on MIB 'not found' results</td><tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3121</td><td>Version 7</td><td>MIB Impairment is not generated when a 401 result XML contains both Hit and Try responses</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.31</td><td>Version 7</td><td>Provider Interfaces</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACMibEvaluationProcessor extends NbaVpmsModelProcessor {

	protected ArrayList impairmentsMIB = new ArrayList();//ACN024
	protected ArrayList accepImpairmentsMIB = null;	

	/**
	 * Overridden method, calls the model and 
	 * updates the contract with impairments.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		// SPR3290 code deleted
		impSrc = NbaConstants.MIB_SRC; //ACN016
		if (performingRequirementsEvaluation()) { //SPR2652
			setPartyID(work); //ACN024
			callMIBEvaluation(); //SPR2955
            //Do the Impairments Merging //ACN024
            mergeImpairmentsAndAccep(impairmentsMIB, accepImpairmentsMIB); //ACN016			
		}
	}
		
	/**
	 * This method calls MIB Evaluation model	 
	 * @throws NbaBaseException, NbaVpmsException
	 */
	//ACP006 new method
	//SPR2955 changed return type to void
	protected void callMIBEvaluation() throws NbaBaseException, NbaVpmsException{
		//SPR2955 code deleted
		String subjectParty = getSubjectOfInquiry();
		ArrayList replyPartyList = getMIBReplyPartyList(subjectParty);
		NbaOinkDataAccess accessContract = new NbaOinkDataAccess(txLifeReqResult);
		accessContract.setLobSource(work.getNbaLob());
		accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
		int partyIndex = 0;
		HashMap deOink = new HashMap();
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		Object[] args = getKeys();
		oinkRequest.setArgs(args);
		ArrayList tempImpairmentList = null;
		int partySize = replyPartyList.size();
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		for(int i=0;i<partySize;i++){
			partyIndex = ((Integer)replyPartyList.get(i)).intValue();
			deOink = deOinkAttributesForMIBModel(partyIndex);	
			deOinkContractData(deOink);	
			if (updatePartyFilterInRequest(oinkRequest, txLifeReqResult, txLifeReqResult.getOLifE().getPartyAt(partyIndex).getId())) { //SPR2652, SPR2804					
				try {
					vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.AC_MIBEVALUATION); //SPR3362
					vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULTXML);
					vpmsProxy.setANbaOinkRequest(oinkRequest);
					oinkRequest.setPartyFilter(partyIndex); //SPR2804
					vpmsProxy.setSkipAttributesMap(deOink);
					NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
					vpmsResultsData.displayResultsData();
					if (vpmsResultsData == null) {
						//SPR3362 code deleted
						throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_MIBEVALUATION); //SPR2652
					} //SPR2652	
					String xmlString = (String) vpmsResultsData.getResultsData().get(0);
					NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
					VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
					tempImpairmentList = vpmsModelResult.getImpairmentInfo(); //ACN024 SPR3121
					if (tempImpairmentList != null && tempImpairmentList.size() != 0) {
					    impairmentsMIB.addAll(tempImpairmentList);//SPR3121
					}
					//SPR2955 code deleted
					// SPR2652 Code Deleted
					//SPR3362 code deleted
					// SPR2652 Code Deleted
				} catch (RemoteException e) { //SPR2652
					handleRemoteException(e, NbaVpmsAdaptor.AC_MIBEVALUATION); //SPR2652
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
		}
		//SPR2955 code deleted 
	}
	
	/**
	 * OINK does not support two xml, so deOINK used for contract data.
	 * @param deOink
	 */
	// ACP006 new method	
	protected void deOinkContractData(HashMap deOink){
		
		NbaParty nbaParty = nbaTxLife.getParty(partyID);
		Party party = nbaParty.getParty();
		ArrayList priorName = party.getPriorName();
		int pNameSize = priorName.size();
		PriorName pName = null;
		for(int i=0;i<pNameSize;i++){
			pName= (PriorName)priorName.get(i);
			if(i==0){
				deOink.put("A_PRIORFIRSTNAME_INS",pName.getFirstName());
				deOink.put("A_PRIORLASTNAME_INS",pName.getLastName());
			}
			else{
				deOink.put("A_PRIORFIRSTNAME_INS["+i+"]",pName.getFirstName());
				deOink.put("A_PRIORLASTNAME_INS["+i+"]",pName.getLastName());							
			}
		}
		deOink.put("A_no_of_PriorNames",String.valueOf(pNameSize)); 
		PersonOrOrganization personOrOrg = party.getPersonOrOrganization();
		if(personOrOrg.isPerson()){
			Person person = party.getPersonOrOrganization().getPerson();
			if(person !=null){
				deOink.put("A_FIRSTNAME_INS",person.getFirstName());
				deOink.put("A_LASTNAME_INS",person.getLastName());
				deOink.put("A_BIRTHDATE_INS",NbaUtils.getDateWithoutSeparator(person.getBirthDate()));
				deOink.put("A_BIRTHSTATE_INS",person.getBirthJurisdiction());
				deOink.put("A_CITIZENSHIP_INS",String.valueOf(person.getCitizenship()));
			}
		}
	}
	/**
	 * Method will return the id of subject party.
	 * @return String
	 */
	//ACP006 new method
	protected String getSubjectOfInquiry(){	
		Holding holding = getHoldingForMIB();
		Policy policy = null;
		boolean isLife = false;
			if(holding != null){
				policy = holding.getPolicy();
				if(policy != null && policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null){
					isLife = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife();
					if(isLife){					 
						ArrayList coverageList = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().getCoverage();
						int coverageListCount = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().getCoverageCount();
						ArrayList lifeParticipantList = new ArrayList();
						int lifeParticipantCount = 0;
						for(int j=0;j<coverageListCount;j++){
							lifeParticipantCount = ((Coverage)coverageList.get(j)).getLifeParticipantCount();
							lifeParticipantList = ((Coverage)coverageList.get(j)).getLifeParticipant();
							for(int k=0;k<lifeParticipantCount;k++){
								LifeParticipant lifeParticipant = (LifeParticipant)lifeParticipantList.get(k);
								if(lifeParticipant.getLifeParticipantRoleCode() == NbaOliConstants.OLI_PARTICROLE_PRIMARY){ 
									return lifeParticipant.getPartyID();
								}
							}
						}
					}	
				}
			}	
		return null;
	}

	
	/**
	 * Method returns MIBInquiryReason corresponding to a FormInstance. 
	 * @param formInstance
	 * @return
	 */
	//ACP006 new method
	public long getMIBInquiryReason(String formInstanceId){
		long inquiryReason = -1;
		Holding holding = getBusinessContextHoldingForMIB(formInstanceId);
		if(holding !=null && holding.hasPolicy()){
			Policy policy = holding.getPolicy();
			ArrayList reqInfoList = null;
			RequirementInfo reqInfo = null;
			reqInfoList = policy.getRequirementInfo();
			int reqInfoSize = reqInfoList.size();
			for(int j=0;j<reqInfoSize;j++){
				reqInfo = (RequirementInfo)reqInfoList.get(j);
				if(reqInfo.hasMIBInquiryReason()){
					inquiryReason = reqInfo.getMIBInquiryReason();
				}					
			}
		}
		return inquiryReason;
	}

	/**
	 * Method returns CarrierApptTypeCode corresponding to a FormInstance. 
	 * @param formInstance
	 * @return long
	 */
	//ACP006 new method
	public long getCarrierApptTypeCode(String formInstanceId){
		Relation relation = null;
		NbaParty nbaParty = null;
		String partyId = null;
		Party party = null;
		CarrierAppointment carrierAppt = null;
		ArrayList carrierApptList = new ArrayList();
		long carrierApptTypeCode = -1;
		Holding holding = getBusinessContextHoldingForMIB(formInstanceId);
		String holdingId = null;
		int relationCount = 0;
		if(holding !=null){
			ArrayList relationList = txLifeReqResult.getOLifE().getRelation();
			holdingId = holding.getId();
			relationCount = relationList.size();
			for(int j=0;j<relationCount;j++){
				relation = (Relation)relationList.get(j);
				if(relation!=null && holdingId.equals(relation.getOriginatingObjectID()) &&
					relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_PRIMAGENT){
					partyId = relation.getRelatedObjectID();
					nbaParty = txLifeReqResult.getParty(partyId);
					if(party!=null){
						party = nbaParty.getParty();
						if(party!=null && party.hasProducer()){
							carrierApptList = party.getProducer().getCarrierAppointment();
							int carrierApptCount = carrierApptList.size();
							for(int k=0;k<carrierApptCount;k++){
								carrierAppt = (CarrierAppointment)carrierApptList.get(k);
								if(carrierAppt.hasCarrierApptTypeCode()){
									carrierApptTypeCode = carrierAppt.getCarrierApptTypeCode();
								}										
							}
						}
					}
				}				
			}		 
		}
		return carrierApptTypeCode;
	}
	/**
	 * MIB response can contain more than one holding.
	 * @return the primary holding for MIB. 	  
	 */
	//ACP006 new method
	public Holding getHoldingForMIB(){
		OLifE oLifE = txLifeReqResult.getOLifE();
		ArrayList holdingList = oLifE.getHolding();
		Holding holding = null;
		Policy policy = null;
		boolean isLife = false;
		int coverageListCount = 0;
		ArrayList coverageList = new ArrayList();
		int lifeParticipantCount = 0;
		ArrayList lifeParticipantList = new ArrayList();
		LifeParticipant lifeParticipant = null;
		int holdingSize = holdingList.size();
		for(int i=0;i<holdingSize;i++){
			holding = ((Holding)holdingList.get(i));
			if(holding != null){
				policy = holding.getPolicy();
				if(policy != null && policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null){
					isLife = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife();
					if(isLife){					 
						coverageList = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().getCoverage();
						coverageListCount = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().getCoverageCount();
						for(int j=0;j<coverageListCount;j++){
							lifeParticipantCount = ((Coverage)coverageList.get(j)).getLifeParticipantCount();
							lifeParticipantList = ((Coverage)coverageList.get(j)).getLifeParticipant();
							for(int k=0;k<lifeParticipantCount;k++){
								lifeParticipant = (LifeParticipant)lifeParticipantList.get(k);
								if(lifeParticipant.getLifeParticipantRoleCode() == NbaOliConstants.OLI_PARTICROLE_PRIMARY){ 
									return holding;
								}
							}
						}
					}	
				}
			}	
		}
		return null;
	}
		
	/**
	 * MIB response can contain more than one holding. This method will retrieve the holding corresponding
	 * to a reply party.
	 * @param String 
	 * @return Holding  
	 */
	//ACP006 new method
	public Holding getBusinessContextHoldingForMIB(String formInstanceId){
		FormInstance formInstance = txLifeReqResult.getFormInstance(formInstanceId);
		ArrayList responseList = formInstance.getFormResponse();
		ArrayList associatedResponseList = null;
		FormResponse response = null;
		AssociatedResponseData associatedResponse = null;
		int responseCount = responseList.size();
		for(int i=0;i<responseCount;i++){
			response = (FormResponse)responseList.get(i);
			associatedResponseList = response.getAssociatedResponseData();
			int associatedResponseCount = associatedResponseList.size(); 
			for(int j=0;j<associatedResponseCount;j++){
				associatedResponse = (AssociatedResponseData)associatedResponseList.get(j);
				if(associatedResponse.hasTopLevelObjectID()){
					return txLifeReqResult.getHolding(associatedResponse.getTopLevelObjectID()); 
				}
			}
		}
		return null;
	}
	
	/**
	 * This method will return all reply parties corresponding to the subject party
	 * from the 401 response. 
	 * @param partyId
	 * @return ArrayList
	 */
	//ACP006 new method
	protected ArrayList getMIBReplyPartyList(String partyId){
		OLifE oLife = txLifeReqResult.getOLifE();
		List relationList  = oLife.getRelation(); // SPR3290
		ArrayList partyList = new ArrayList();
		// SPR3290 code deleted
		int partyCount = oLife.getPartyCount();
		int relationCount = relationList.size();
		Relation relation = null;
		for(int i=0;i<relationCount;i++){
			relation = (Relation)relationList.get(i);
			if((relation.getRelationRoleCode()== NbaOliConstants.OLI_REL_TRY ||
				relation.getRelationRoleCode()== NbaOliConstants.OLI_REL_HIT) 
				) { // AXAL3.7.31 code deleted				
				for(int j=0;j<partyCount;j++){
					if(oLife.getPartyAt(j).getId().equals(relation.getRelatedObjectID())){
						partyList.add(new Integer(j));
					}
				}
			}
		}
		return partyList;	
	}
	
	/**
	 * This function gets the deOink values for MIB Evaluation model
	 * @param partyIndex: Index of the Related object
	 * @return HashMap containing deOink values
	 * @throws NbaBaseException
	 */
	//ACP006 new method
	protected HashMap deOinkAttributesForMIBModel(int partyIndex) throws NbaBaseException{	
		
		// SPR2652 code deleted
		String partyId = txLifeReqResult.getOLifE().getPartyAt(partyIndex).getId();
		ArrayList formInstanceList = txLifeReqResult.getAllFormInstancesForParty(partyId);
		// SPR2652 code deleted
		HashMap deOink = new HashMap();
		FormInstance formInstance = null;
		String formInstanceId = null;
		int recordsCount = txLifeReqResult.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult().getRecordsFound();
		deOink.put("A_RECORDSFOUND",String.valueOf(recordsCount));
		int formInstanceCount = formInstanceList.size(); 
		for(int i=0;i<formInstanceCount;i++){
			ArrayList formResponseData = null;
			formInstance = (FormInstance)formInstanceList.get(i);
			formInstanceId = formInstance.getId();
			formResponseData = retrieveFormResponseData(formInstanceId);
				if(i==0){
						deOink.put("A_FormName",formInstance.getFormName());
						deOink.put("A_MIBSubmitDate",NbaUtils.getDateWithoutSeparator(formInstance.getSubmitDate()));
						deOink.put("A_MIBCarrierCode",getCarrierCode(formInstance));			
						deOink.put("A_no_of_Codes", (new Integer(formResponseData.size())).toString());
						deOink.put("A_LineOfBusiness",String.valueOf(getMIBLineOfBusiness(formInstanceId)));
						deOink.put("A_CarrierForm",String.valueOf(getCarrierForm(formInstance)));
						deOink.put("A_MIBInquiryReason",String.valueOf(getMIBInquiryReason(formInstanceId)));
						deOink.put("A_CARRIERAPPTTYPECODE",String.valueOf(getCarrierApptTypeCode(formInstanceId)));
					}
					else{
						deOink.put("A_FormName["+ i + "]",formInstance.getFormName());
						deOink.put("A_MIBSubmitDate["+ i + "]",NbaUtils.getDateWithoutSeparator(formInstance.getSubmitDate()));
						deOink.put("A_MIBCarrierCode["+ i + "]",getCarrierCode(formInstance));
						deOink.put("A_no_of_Codes["+ i + "]",(new Integer(formResponseData.size())).toString());
						deOink.put("A_LineOfBusiness["+ i + "]",String.valueOf(getMIBLineOfBusiness(formInstanceId)));
						deOink.put("A_CarrierForm["+ i + "]",String.valueOf(getCarrierForm(formInstance)));
						deOink.put("A_MIBInquiryReason["+ i + "]",String.valueOf(getMIBInquiryReason(formInstanceId)));
						deOink.put("A_CARRIERAPPTTYPECODE["+i+"]",String.valueOf(getCarrierApptTypeCode(formInstanceId)));
					}
			String codeMIB = null;
			for(int j=0;j<formResponseData.size();j++){
				codeMIB = (String)formResponseData.get(j);
				if(i==0 && j==0){
					deOink.put("A_FormResponseData",codeMIB);
				}
				else if (i==1 && j==0){
					deOink.put("A_FormResponseData["+i+"]",codeMIB);
				}
				else{
					deOink.put("A_FormResponseData["+i+","+j+"]",codeMIB);
				}				
			 }									
		}		
		
		// SPR3290 code deleted
		int countMIB = formInstanceList.size();
		deOink.put("A_no_of_Set_of_Codes",(String.valueOf(countMIB)));
		return deOink; 
	}
	
	/**
	 * Retrieves all the MIB Codes for one of the reply party in MIB response. 
	 * @param partyIndex, the index of the reply party.
	 * @param formInstanceId
	 * @return List
	 */
	//ACP006 new method
	public ArrayList retrieveFormResponseData(String formInstanceId) {
		FormInstance formInstance = null;
		FormResponse response = null;
		String codeMIB = null;
		formInstance = txLifeReqResult.getFormInstance(formInstanceId);
		ArrayList responseList = formInstance.getFormResponse();
		ArrayList responseDataList = new ArrayList();
		int responseCount = responseList.size(); 
		for(int k=0;k<responseCount;k++){
			response = (FormResponse)responseList.get(k);
			codeMIB = response.getResponseData();
			if(codeMIB !=null ){
				responseDataList.add(codeMIB);
			}
			else{
				responseDataList.add("");
			}
		}
		return responseDataList;		
	}
	
	/**
	 * Method returns carrierForm corresponding to FormInstance.
	 * @param formInstance
	 * @return long
	 */
	//ACP006 new method
	public long getCarrierForm(FormInstance formInstance){
		long carrierForm =-1;
		Party party = getCarrierParty(formInstance);
		if(party!=null){
			carrierForm = party.getCarrier().getCarrierForm();
		}					 
		return carrierForm;
	}
	
	/**
	 * Method returns carrierCode corresponding to a FormInstance.
	 * @param formInstance
	 * @return String
	 */
	//ACP006 new method
	public String getCarrierCode(FormInstance formInstance){
		String carrierCode = "";
		Party party = getCarrierParty(formInstance);
		if(party!=null){
			carrierCode = party.getCarrier().getCarrierCode();
		}
		return carrierCode;
	}
	
	/**
	 * Gets the carrier party for this FormInstance
	 * @param formInstance
	 * @return
	 */
	//ACP006 new method
	protected Party getCarrierParty(FormInstance formInstance){
		if(formInstance != null && formInstance.hasProviderPartyID()){
			String providerPartyId = formInstance.getProviderPartyID();
			if(providerPartyId != null && !providerPartyId.equals("")){
				NbaParty nbaParty = txLifeReqResult.getParty(providerPartyId);
				if(nbaParty !=null){
					Party party = nbaParty.getParty();
					if(party!=null && party.hasCarrier()){
						return party;
					}
				}
			}		
		}
		return null;
	}
	
	
	/**
	 * Method returns MIBLineOfBusiness corresponding to a FormInstance.
	 * @param formInstance
	 * @return long.
	 */
	//ACP006 new method
	public long getMIBLineOfBusiness(String formInstanceId){
		long lineOfBusiness = -1;
		Holding holding = getBusinessContextHoldingForMIB(formInstanceId);
		Policy policy = null;
		if(holding !=null ){
			policy = holding.getPolicy();
		}	 
		if(policy!= null && policy.hasLineOfBusiness()){
			lineOfBusiness = holding.getPolicy().getLineOfBusiness();
		}
		return lineOfBusiness;
	}


}
