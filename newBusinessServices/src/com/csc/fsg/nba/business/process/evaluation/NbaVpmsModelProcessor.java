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
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.business.process.NbaImpairmentMerger;
import com.csc.fsg.nba.datamanipulation.NbaContractDataAccessConstants;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.ac.AcceptableImpairments;
import com.csc.fsg.nba.vo.ac.DefaultValues;
import com.csc.fsg.nba.vo.ac.SummaryValues;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.FormResponse;
import com.csc.fsg.nba.vo.txlife.FormResponseExtension;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.MedicalExam;
import com.csc.fsg.nba.vo.txlife.MedicalExamExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PreferredInfo;
import com.csc.fsg.nba.vo.txlife.ProfileInfo;
import com.csc.fsg.nba.vo.txlife.ProfileScoreInfo;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.UnderwritingAnalysis;

// ACP008


/**
 * This is a class that provides the basis for doing the processing 
 * after a model is invoked either from NBCTEVAL or NBRQEVAL
 * <p>All classes corresponding to models should implement this interface. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>ACP014</td><td>Version 4</td><td>Financial Screening</td></tr>
 * <tr><td>ACP015</td><td>Version 4</td><td>Profile </td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>ACP008</td><td>Version 4</td><td>Preferred Questionnaire</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR2804</td><td>Version 5</td><td>REQEVAL process error stops as it fails to process results from ACMIBEVALUATION vpms model</td><tr>
 * <tr><td>SPR2741</td><td>Version 6</td><td>Re-evaluation is generating insert errors</td></tr>
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * <tr><td>NBA224</td><td>Version 8</td><td>nbA Underwriter Workbench Requirements and Impairments Enhancement</td></tr>
 * <tr><td>ALS4576</td><td>AXA Life Phase 1</td><td>QC # 3647 - 3.7.31 provider feed from CRL, lab results not displayed on preferred profile</td></tr>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Full Implementation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public abstract class NbaVpmsModelProcessor {
	protected NbaTXLife nbaTxLife = null;
	protected NbaTXLife txLifeReqResult = null;
	protected NbaUserVO user = null;
	protected NbaDst work = null;
	protected static NbaLogger logger = null;
	protected ArrayList contractImpairments = null;

	protected String vpmsResult = "";
	protected String backEndKey = "";
	protected String companyKey = "";
	protected String contractNo = "";
	protected String partyID = "";
	protected NbaAcdb nbaAcdb = new NbaAcdb();
	protected String reqId = "";
	protected String impSrc =""; //ACN016
	protected String reqRelatePartyID = null; //ALS4576
	

	/**
	 * Initializes the instance variables.
	 * @param nbaTXLife
	 * @param user
	 * @param workItem
	 * @param listImpairmentInfo
	 */
	public void initialize(NbaTXLife nbaTXLife,  NbaUserVO user, NbaDst workItem){
		this.nbaTxLife = nbaTXLife;
		this.user = user;
		this.work = workItem;
		this.backEndKey = work.getNbaLob().getBackendSystem();
		this.companyKey = work.getNbaLob().getCompany();
		this.contractNo = work.getNbaLob().getPolicyNumber();
	}
	
	/**
	 * Initializes the instance variables.
	 * @param nbaTXLife
	 * @param user
	 * @param workItem
	 * @param listImpairmentInfo
	 * @param txLifeReqResult
	 */
	public void initialize(NbaTXLife nbaTXLife,  NbaUserVO user, NbaDst workItem, NbaTXLife txLifeReqResult) throws NbaBaseException{
		this.nbaTxLife = nbaTXLife;
		this.user = user;
		this.work = workItem;
		this.txLifeReqResult = txLifeReqResult;
		this.backEndKey = work.getNbaLob().getBackendSystem();
		this.companyKey = work.getNbaLob().getCompany();
		this.contractNo = work.getNbaLob().getPolicyNumber();
		setReqIdFromRequirementCode();//ACP001 Removed the Unique ID from Parameter and renamed the function
	}
	
	
	//ACP002 new method
	protected void getContractImpairments(String partyId) {
	    contractImpairments = new ArrayList(); //NBA187
		Party party = nbaTxLife.getParty(partyId).getParty();
		Person person = party.getPersonOrOrganization().getPerson();
		PersonExtension personExt = NbaUtils.getFirstPersonExtension(person);
		//begin NBA187
		if (personExt != null ){
		    contractImpairments = personExt.getImpairmentInfo();
		}
		//end NBA187
	}
	
	//ACP002 new method.
	protected ArrayList getAllInsuredIndexes(){
		
		OLifE oLifE = nbaTxLife.getOLifE();
		ArrayList al = new ArrayList();
		for (int i = 0; i < oLifE.getPartyCount(); i++) {
			String partyId = oLifE.getPartyAt(i).getId();
			if (nbaTxLife.isInsured(partyId)) {
				al.add(new Integer(i));
				if(getLogger().isDebugEnabled()) { //SPR3290				
				    getLogger().logDebug("partyId: " + partyId + "  index " + i);
				} //SPR3290
			}
		}
		return al;
	}

	//ACP002 new method
	protected void addImpairmentInfo(String partyId, ArrayList listImpair) throws NbaBaseException{
		Party party = nbaTxLife.getParty(partyId).getParty();	
		Person person = party.getPersonOrOrganization().getPerson();
		PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
		//begin NBA187
		if (personExtension == null) {
		    OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
		    personExtension = oliExt.getPersonExtension();
            person.addOLifEExtension(oliExt);
            personExtension.setActionAdd(); 
		}
		//end NBA187
		//Begin NBA224
		if (listImpair != null && !listImpair.isEmpty()) {
			int count = listImpair.size();
			for (int i = 0; i < count; i++) {
				ImpairmentInfo impairInfo = (ImpairmentInfo) listImpair.get(i);
				impairInfo.setRestrictApprovalInd(nbaAcdb.getRestrictApprovalInd(impairInfo.getDescription()));
			}
		}
		// End NBA224
		personExtension.setImpairmentInfo(listImpair);		
		//ACP013-ACP022 line deleted.
	}
	
	
	/**
	 * This method is the used for adding ProfileInfo Object Into the DataBase
	 * This Method is called from The CTEVAL Process.
	 * @param partyId
	 * @param profileScore
	 * @return void
	 */
	// ACP015 New Method
	protected void addProfileInfo(String partyId, ArrayList profileScore) throws NbaBaseException{
	 Party party = nbaTxLife.getParty(partyId).getParty();
	 UnderwritingAnalysis underwritingAnalysis = null;
	 PartyExtension partyExtension = null;
	 if (party != null){
		 party.setActionUpdate();
		 partyExtension = NbaUtils.getFirstPartyExtension(party);
		 if (partyExtension == null){						// add new partyExtension Object
			 OLifEExtension oli = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PARTY);
			 oli.setActionAdd();
			 party.addOLifEExtension(oli);
			 partyExtension = oli.getPartyExtension();
			 partyExtension.setActionAdd();
			 underwritingAnalysis = partyExtension.getUnderwritingAnalysis();
		 }
		 else{
		 	 partyExtension.setActionUpdate();
			 underwritingAnalysis = partyExtension.getUnderwritingAnalysis();
		 }
		 if (underwritingAnalysis == null){
			 UnderwritingAnalysis uwanalysis = new UnderwritingAnalysis();
			 uwanalysis.setActionAdd();
			 partyExtension.setUnderwritingAnalysis(uwanalysis);
			 underwritingAnalysis = uwanalysis;
			 
			 for (int j =0 ; j < profileScore.size(); j ++ ){
				updateProfileScoreInfo(underwritingAnalysis,(ProfileInfo)profileScore.get(j));
	  		 }
		 }
		 else{
	        for (int j =0 ; j < profileScore.size(); j ++ ){
			 	updateProfileScoreInfo(underwritingAnalysis,(ProfileInfo)profileScore.get(j));
	        }
	     }
	  }
	}
	
	/**
	 * This method is the used for adding ProfileScoreInfo Object Into the DataBase 
	 * @param UnderwritingAnalysis aUnderwritingAnalysis
	 * @param profileInfo aProfileInfo
	 * @return void
	 */
	// ACP015 New Method
	protected  void updateProfileScoreInfo(UnderwritingAnalysis underwritingAnalysis , ProfileInfo profileInfo){
		ProfileInfo profileInfo2 = underwritingAnalysis.getProfileInfo();
		if (profileInfo2 == null){
		   profileInfo2 = new ProfileInfo();
		   profileInfo2.setActionAdd();
		   profileInfo2.setProfileSysCalcCredits(profileInfo.getProfileSysCalcCredits());
		   profileInfo2.setProfileSysCalcDebits(profileInfo.getProfileSysCalcDebits());
		   profileInfo2.setProfileTotalScore(profileInfo.getProfileTotalScore());
		   profileInfo2.setProfileTotalScoreSign(profileInfo.getProfileTotalScoreSign());
		   for (int a = 0 ; a< profileInfo.getProfileScoreInfoCount(); a++){
			   underwritingAnalysis.setProfileInfo(profileInfo2);
			   if (profileInfo.getProfileScoreInfoAt(a)!= null){
				   ProfileScoreInfo profileScoreInfo = (ProfileScoreInfo)profileInfo.getProfileScoreInfoAt(a);
				   NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
				   nbaOLifEId.setId(profileScoreInfo); 
				   profileScoreInfo.setActionAdd();
				   profileInfo2.addProfileScoreInfo(profileScoreInfo);
			   }
		   }
		}
		else{
			profileInfo2.setActionUpdate();
			profileInfo2.setProfileSysCalcCredits(profileInfo.getProfileSysCalcCredits());
			profileInfo2.setProfileSysCalcDebits(profileInfo.getProfileSysCalcDebits());
			profileInfo2.setProfileTotalScore(profileInfo.getProfileTotalScore());
			profileInfo2.setProfileTotalScoreSign(profileInfo.getProfileTotalScoreSign());
			for(int i=0;i<profileInfo2.getProfileScoreInfoCount();i++){
				profileInfo2.getProfileScoreInfoAt(i).setActionDelete();			
			}
			for (int a = 0 ; a< profileInfo.getProfileScoreInfoCount(); a++){			 
			  if (profileInfo.getProfileScoreInfoAt(a)!= null){
				  ProfileScoreInfo profileScoreInfo = (ProfileScoreInfo)profileInfo.getProfileScoreInfoAt(a);
				  NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
				  nbaOLifEId.setId(profileScoreInfo); 
				  profileScoreInfo.setActionAdd();
				  profileInfo2.addProfileScoreInfo(profileScoreInfo);
			  }
			}	
		}
	}
	
	
	/**
	 * This method is the used for adding Profile Score Info
	 * This Method is called from ReqEval Process.
	 * @param profileScore
	 * @return void
	 */
	// ACP015 New Method
	protected void addProfileInfo(ArrayList profileScore) throws NbaBaseException{
		Party party = nbaTxLife.getParty(partyID).getParty();
		UnderwritingAnalysis underwritingAnalysis = null;
		PartyExtension partyExtension = null;
		if (party != null){
			party.setActionUpdate();
			partyExtension = NbaUtils.getFirstPartyExtension(party);
			if (partyExtension == null){
				OLifEExtension oli = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PARTY);
				oli.setActionAdd();
				party.addOLifEExtension(oli);
				partyExtension = oli.getPartyExtension();
				partyExtension.setActionAdd();
				underwritingAnalysis = partyExtension.getUnderwritingAnalysis();
			}
			else{
                partyExtension.setActionUpdate();
				underwritingAnalysis = partyExtension.getUnderwritingAnalysis();
			}
			if (underwritingAnalysis == null){
				UnderwritingAnalysis uwanalysis = new UnderwritingAnalysis();
				uwanalysis.setActionAdd();
				partyExtension.setUnderwritingAnalysis(uwanalysis);
				underwritingAnalysis = uwanalysis;
				for (int j =0 ; j < profileScore.size(); j ++ ){
					updateProfileScoreInfo(underwritingAnalysis,(ProfileInfo)profileScore.get(j));
				}
			}
			else{
				for (int j =0 ; j < profileScore.size(); j ++ ){
					updateProfileScoreInfo(underwritingAnalysis,(ProfileInfo)profileScore.get(j));
				}
			}
		}
	}
	/**
	 * This method is the used for getting StressECGValue from OLife.Party.Risk.MedicalExam.MedicalExamExtension
	 * if there is no StressECG Value than it returns Blank String;
	 * @param partyIndex
	 * @return String StressECGValue
	 */
	// ACP015 New Method

	protected String getStressECGValue(int partyIndex){
		OLifE olife = nbaTxLife.getOLifE();
		MedicalExam medicalExam = null;
		MedicalExamExtension medicalExamExtension = null;
		String StressECGValue = "";
		if (olife != null){
			Party party = olife.getPartyAt(partyIndex);
			if (party != null){
				if (party.hasRisk()){
					Risk risk = party.getRisk();
					for (int j = 0 ; j < risk.getMedicalExamCount(); j++){
						medicalExam = risk.getMedicalExamAt(j);
						if (medicalExam != null){
							medicalExamExtension = NbaUtils.getFirstMedicalExamExtension(medicalExam);
							if (medicalExamExtension != null && medicalExamExtension.hasStressECGValue()){
								StressECGValue = String.valueOf(medicalExamExtension.getStressECGValue());
								break;
							}
						}
					}
				}
			}
		}
		return StressECGValue;
	}
	/**
	 * This method is the main method for Merging Impairments and acceptable impairments
	 * @param existingImps
	 * @param newImps
	 * @param existingAcceptableImps
	 * @param newAcceptableImps
	 * @return
	 */
	// ACN016 New Method
	public ArrayList[] mergeImpairments(
			ArrayList existingImps,
			ArrayList newImps,
			ArrayList existingAcceptableImps,
			ArrayList newAcceptableImps) throws NbaBaseException {
			NbaImpairmentMerger acImpMerge= null;
			if (performingContractEvaluation()) {  //SPR2652
				acImpMerge=new NbaImpairmentMerger("NbaProcContractEvalution");
				acImpMerge.updateStatusResolvedForContract(existingImps,newImps,impSrc,nbaTxLife);  //AXAL3.7.07
			}
			else if (performingRequirementsEvaluation()) { //SPR2652
				acImpMerge=new NbaImpairmentMerger("NbaProcRequirementEvaluation");
				acImpMerge.updateStatusResolvedForRequirement(existingImps,newImps,impSrc,nbaTxLife);  //AXAL3.7.07
			}
			//Begin SR564247(APSL2525)-Full
			else if(performingPredictiveHoldProcessing()){
				acImpMerge=new NbaImpairmentMerger("NbaProcPredictiveHold");
				acImpMerge.updateStatusResolvedForServiceResult(existingImps,newImps,impSrc,nbaTxLife);  //AXAL3.7.07
			}
			//End SR564247(APSL2525)-Full
			acImpMerge.generalImpMerging(existingImps, newImps,nbaTxLife);
			ArrayList NewAcceptableImps= acImpMerge.mergeAccepImp(existingImps, existingAcceptableImps, newAcceptableImps,nbaTxLife);
			ArrayList[] obj=new ArrayList[2];
			obj[0]=existingImps;
			obj[1]=NewAcceptableImps;
			return obj;			
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaVpmsModelProcessor.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaAutomatedProcess could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	//ACN010 New Method
	protected Object[] getKeys() {
		Object[] keys = new Object[4];
		keys[0] = partyID; //Parentid key.
		keys[1] = contractNo; //contract key
		keys[2] = companyKey; //company key
		keys[3] = backEndKey; //backend key
		return keys;
	}
	
	//ACP014 New Method
	protected Object[] getKeys(String partyId) {
		Object[] keys = new Object[4];
		keys[0] = partyId; //Parentid key.
		keys[1] = contractNo; //contract key
		keys[2] = companyKey; //company key
		keys[3] = backEndKey; //backend key
		return keys;
	}	
	/**
     * Merge Impairments and acceptable impairments
     * @param listImpairmentInfo
     * @param listAcceptableImpairments
     * @throws NbaBaseException
     */
    //ACN024 new method.
	//SPR2741 added throws clause
    public void mergeImpairmentsAndAccep(ArrayList listImpairmentInfo, ArrayList listAcceptableImpairments) throws NbaBaseException {
		if (!(nbaTxLife.isOwner(partyID) && !nbaTxLife.isOwnerSameAsInsured())) { // APSL5011
			PersonExtension personExt = getPersonExtension();
			// Start APSL1799
			ArrayList txLifeImpairmentInfo = new ArrayList();
			if (personExt != null) {
				txLifeImpairmentInfo = personExt.getImpairmentInfo();
			}
			// end APSL1799
			ArrayList vpmImpairmentInfo;
			vpmImpairmentInfo = listImpairmentInfo;
			Object[] keys = getKeys();
			ArrayList txLifeAcceptableImp = nbaAcdb.getAcceptableImpairments(keys);
			ArrayList vpmAcceptableImp = listAcceptableImpairments;
			ArrayList[] arrMergedAll = mergeImpairments(txLifeImpairmentInfo, vpmImpairmentInfo, txLifeAcceptableImp, vpmAcceptableImp);
			ArrayList arrMerged = arrMergedAll[0];
			ArrayList arrAccepMerged = arrMergedAll[1];
			addImpairmentInfo(partyID, arrMerged);
			addAcceptableImpairments(arrAccepMerged);
		}
        //SPR2741 code deleted
    }
	/**
     * Inserts acceptable impairments
     * @param txLifeAcceptableImp
     * @throws NbaDataAccessException
     */
    //ACN016 New Method
    //SPR2741 added throws clause
    private void addAcceptableImpairments(ArrayList txLifeAcceptableImp) throws NbaDataAccessException {
        Object[] obj = getKeys();
        for (int i = 0; i < txLifeAcceptableImp.size(); i++) {
            ((AcceptableImpairments) txLifeAcceptableImp.get(i)).setId(work.getNbaLob().getReqUniqueID() + "_" + (i + 1));
            ((AcceptableImpairments) txLifeAcceptableImp.get(i)).setParentIdKey(obj[0].toString());
            ((AcceptableImpairments) txLifeAcceptableImp.get(i)).setContractKey(obj[1].toString());
            ((AcceptableImpairments) txLifeAcceptableImp.get(i)).setCompanyKey(obj[2].toString());
            ((AcceptableImpairments) txLifeAcceptableImp.get(i)).setBackendKey(obj[3].toString());
        }
        nbaAcdb.addAcceptableImpairments(txLifeAcceptableImp);
    }
	
	//	ACN016 New Method
	protected PersonExtension getPersonExtension() { //ACN024
		Party party = nbaTxLife.getParty(partyID).getParty();
		Person person = party.getPersonOrOrganization().getPerson();
		PersonExtension personExt = NbaUtils.getFirstPersonExtension(person);
		return personExt;
	}
	
	//ACP002 new method.
	//ACP001 Changed this function to get requirement id using requirement code
	protected void setReqIdFromRequirementCode() throws NbaBaseException {		
		long reqCode = work.getNbaLob().getReqType(); //ACP001
		ArrayList reqInfos = txLifeReqResult.getPolicy().getRequirementInfo(); //ACN009
		for (int i = 0; i < reqInfos.size(); i++) {
			RequirementInfo reqInfo = (RequirementInfo) reqInfos.get(i);
			if (reqInfo.getReqCode() == reqCode) {//ACP001
				this.reqId = reqInfo.getId();
				this.reqRelatePartyID = reqInfo.getAppliesToPartyID(); //ALS4576
				break;
			}
		}
	}
	
	//ACP002 new method.
	protected void deOinkContractFieldsForRequirement(Map deOink) throws NbaBaseException {
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaTxLife);
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		String value = "";//ALNA583
		String oinkVarName = "ProductCode_BASE";
		oinkRequest.setVariable(oinkVarName);
		value = oinkData.getStringValueFor(oinkRequest);
		deOink.put("A_PRODUCTCODE_INS", value);
		if (getLogger().isDebugEnabled()) { //SPR3290
		    getLogger().logDebug("A_PRODUCTCODE_INS: " + value);
		}//SPR3290
		//Begin ALNA583
		String applicationType = "ApplicationType";
		oinkRequest.setVariable(applicationType);
		value = oinkData.getStringValueFor(oinkRequest);
		deOink.put("A_ApplicationType", value);
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("A_PRODUCTCODE_INS: " + value);
		}//End ALNA583
	}
	
	//ACP002 new method.
	protected void deOinkXMLResultFields(Map deOink) throws NbaBaseException {
		deOink.put("A_VPMSMODELRESULTTAG", "VpmsModelResult");
		deOink.put("A_XMLRESPONSE", "true");
	}
	
	protected void deOinkUFSFields(NbaOinkDataAccess oinkData, NbaOinkRequest oinkRequest, Map deOink){
		String oinkVarName = "FormResponseCode";		
		try {
		oinkRequest.setRelatedObjectTypeFilter(String.valueOf(NbaOliConstants.OLI_MEDCONDITION));
		oinkRequest.setVariable(oinkVarName);
		String[] value = oinkData.getStringValuesFor(oinkRequest);
			deOink.put("A_no_of_" + oinkVarName, String.valueOf(value.length));
		for (int j =0; j<value.length; j++) {
			if (j==0) deOink.put("A_"+oinkVarName, value[j]);
			else deOink.put("A_"+oinkVarName+"["+j+"]", value[j]);
			}
		}catch(NbaBaseException e){
			deOink.put("A_no_of_" + oinkVarName, "0");
			deOink.put("A_"+oinkVarName,"");					
		}
	}
	
	/**
	 * This function gets the deOink values for ACNONMEDICALHISTORY model
	 * @param relatedObjectType: Type of the Related Object for Form Instance
	 * @param elementIndex: Index of the Related object
	 * @return HashMap containing deOink values
	 * @throws NbaBaseException
	 */
	//ACP009 New Method	
	protected HashMap getNonMedDeOINKValues(long relatedObjectType, int elementIndex){
		HashMap deOink = new HashMap();
		try{
			
			NbaOinkDataAccess dataAccess = new NbaOinkDataAccess(txLifeReqResult);
			NbaOinkRequest oinkRequest = new NbaOinkRequest();		
	  		
			String[] responseCodeList;
			String responseCode;
			int count = 0;
			//Get the Form Response Code
			oinkRequest.setVariable("FormResponseCode_INS");
			oinkRequest.setRelatedObjectTypeFilter(String.valueOf(relatedObjectType));
			oinkRequest.setElementIndexFilter(elementIndex);
			
			responseCodeList = dataAccess.getStringValuesFor(oinkRequest);
			if(responseCodeList !=null){
				count = responseCodeList.length;
			}
			deOink.put("A_no_of_FormResponseCode", String.valueOf(count));
			if(count==0){
				deOink.put("A_FormResponseCode","");
			}
			for(int i=0;i<count;i++){
				responseCode = responseCodeList[i];
				if(i==0){
					deOink.put("A_FormResponseCode",responseCode);
				}else{
					deOink.put("A_FormResponseCode["+i+"]",responseCode);
				}
			}
		}catch(NbaBaseException e){
			deOink.put("A_no_of_FormResponseCode", "0");
			deOink.put("A_FormResponseCode","");					
		}
		return deOink;
	}
	
	/**
	 * This function gets the deOink values for ACNONMEDICALHISTORY model
	 * @param relatedObjectType: Type of the Related Object for Form Instance
	 * @param elementIndex: Index of the Related object
	 * @return HashMap containing deOink values
	 * @throws NbaBaseException
	 */
	//ACP009 New Method
	public HashMap getNonMedDeOINKValues(int partyIndex, long relatedObjectType, int elementIndex, String partyID){ //SPR2652 changed method signature
		HashMap deOink = new HashMap();
		try {
			NbaOinkDataAccess dataAccess = new NbaOinkDataAccess(nbaTxLife);
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			if (updatePartyFilterInRequest(oinkRequest, partyID)) { //SPR2652	

				String[] responseCodeList;
				String responseCode;
				int count = 0;
				//Get the Form Response Code
				oinkRequest.setVariable("FormResponseCode_INS");
				// SPR2652 code deleted
				oinkRequest.setRelatedObjectTypeFilter(String.valueOf(relatedObjectType));
				oinkRequest.setElementIndexFilter(elementIndex);
				responseCodeList = dataAccess.getStringValuesFor(oinkRequest);
				if (responseCodeList != null) {
					count = responseCodeList.length;
				}
				deOink.put("A_no_of_FormResponseCode", String.valueOf(count));
				if (count == 0) {
					deOink.put("A_FormResponseCode", "");
				}
				for (int i = 0; i < count; i++) {
					responseCode = responseCodeList[i];
					if (i == 0) {
						deOink.put("A_FormResponseCode", responseCode);
					} else {
						deOink.put("A_FormResponseCode[" + i + "]", responseCode);
					}
				}
			} //SPR2652
		} catch (NbaBaseException e) {
			deOink.put("A_no_of_FormResponseCode", "0");
			deOink.put("A_FormResponseCode", "");

		}
		return deOink;
	}
	
	//ACP002 new method.
	public void setPartyID(NbaDst work) throws NbaBaseException {
		int relationCode = work.getNbaLob().getReqPersonCode();
		int personSeq = work.getNbaLob().getReqPersonSeq();
		partyID = nbaTxLife.getPartyId(relationCode, String.valueOf(personSeq));
	}
	
	
	/**
	 * This method is the used for getting Impairmment Object
	 * if there is no  Value than it returns Blank String;
	 * @param partyIndex
	 * @return String ImpairmentPermFlatExtraAmt */
	// ACP015 New Method
	protected ArrayList getImpairmentPermFlatExtraList(int partyIndex){
		Party party = null;
		OLifE olife = nbaTxLife.getOLifE();
		party = olife.getPartyAt(partyIndex);
		ArrayList impairmentInfo = new ArrayList();
		ArrayList result = new ArrayList();
		Person person = party.getPersonOrOrganization().getPerson();
		PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
		if (personExtension!= null){
			impairmentInfo = personExtension.getImpairmentInfo();
			if (impairmentInfo != null){
				for (int i = 0 ; i < impairmentInfo.size(); i ++){
					result.add(i, ((ImpairmentInfo)impairmentInfo.get(i)).getImpairmentPermFlatExtraAmt());
				}
			}
		}
		return result;
				
	}
	/**
	 * This method is the used for getting Impairmment Object
	 * if there is no  Value than it returns Blank String;
	 * @param partyIndex
	 * @return String ImpairmentTempFlatExtraAmt */
	// ACP015 New Method
	protected ArrayList getImpairmentTempFlatExtraAmtList(int partyIndex){
		Party party = null;
		OLifE olife = nbaTxLife.getOLifE();
		party = olife.getPartyAt(partyIndex);
		ArrayList impairmentInfo = new ArrayList();
		ArrayList result = new ArrayList();
		Person person = party.getPersonOrOrganization().getPerson();
		PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
		if (personExtension!= null){
			impairmentInfo = personExtension.getImpairmentInfo();
			if (impairmentInfo != null){
				for (int i = 0 ; i < impairmentInfo.size(); i ++){
					result.add(i, ((ImpairmentInfo)impairmentInfo.get(i)).getImpairmentTempFlatExtraAmt());
				}
			}

		}
		return result;
	}
	/**
	 * This method is the used for getting Impairmment Object
	 * if there is no  Value than it returns Blank String;
	 * @param partyIndex
	 * @return String ImpairmentPermFlatExtraAmt */
	// ACP015 New Method
	protected ArrayList getCreditList(int partyIndex){
		Party party = null;
		OLifE olife = nbaTxLife.getOLifE();
		party = olife.getPartyAt(partyIndex);
		ArrayList impairmentInfo = new ArrayList();
		ArrayList result = new ArrayList();
		Person person = party.getPersonOrOrganization().getPerson();
		PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
		if (personExtension!= null){
			impairmentInfo = personExtension.getImpairmentInfo();
			if (impairmentInfo != null){
				for (int i = 0 ; i < impairmentInfo.size(); i ++){
					result.add(i, String.valueOf(((ImpairmentInfo)impairmentInfo.get(i)).getCredit()));
				}
			}

		}
		return result;
	}
	/**
	 * This method is the used for getting Impairmment Object
	 * if there is no  Value than it returns Blank String;
	 * @param partyIndex
	 * @return String ImpairmentDebit */
	// ACP015 New Method

	protected ArrayList getDebitList(int partyIndex){
		Party party = null;
		OLifE olife = nbaTxLife.getOLifE();
		party = olife.getPartyAt(partyIndex);
		ArrayList impairmentInfo = new ArrayList();
		ArrayList result = new ArrayList();
		Person person = party.getPersonOrOrganization().getPerson();
		PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
		if (personExtension!= null){
			impairmentInfo = personExtension.getImpairmentInfo();
			if (impairmentInfo != null){
				for (int i = 0 ; i < impairmentInfo.size(); i ++){
					result.add(i, String.valueOf(((ImpairmentInfo)impairmentInfo.get(i)).getDebit()));
				}
			}

		}
		return result;
	}
	/**
	 * This method is the used for getting Impairmment Object
	 * if there is no  Value than it returns Blank String;
	 * @param partyIndex
	 * @return String ImpairmentCredit */
	// ACP015 New Method
	
	protected ArrayList getimpairmentClassList(int partyIndex){
		Party party = null;
		OLifE olife = nbaTxLife.getOLifE();
		party = olife.getPartyAt(partyIndex);
		ArrayList impairmentInfo = new ArrayList();
		ArrayList result = new ArrayList();
		Person person = party.getPersonOrOrganization().getPerson();
		PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
		if (personExtension!= null){
			impairmentInfo = personExtension.getImpairmentInfo();
			if (impairmentInfo != null){
				for (int i = 0 ; i < impairmentInfo.size(); i ++){
					result.add(i, String.valueOf(((ImpairmentInfo)impairmentInfo.get(i)).getImpairmentClass()));
				}
			}

		}
		return result;
	}
	/**
	 * This method is the used for getting Impairmment Object
	 * if there is no  Value than it returns Blank String;
	 * @param partyIndex
	 * @return String ImpairmentCredit */
	// ACP015 New Method
	

	protected ArrayList getimpairmentTypeList(int partyIndex){
		Party party = null;
		OLifE olife = nbaTxLife.getOLifE();
		party = olife.getPartyAt(partyIndex);
		ArrayList impairmentInfo = new ArrayList();
		ArrayList result = new ArrayList();
		Person person = party.getPersonOrOrganization().getPerson();
		PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
		if (personExtension!= null){
			impairmentInfo = personExtension.getImpairmentInfo();
			if (impairmentInfo != null){
				for (int i = 0 ; i < impairmentInfo.size(); i ++){
					result.add(i, String.valueOf(((ImpairmentInfo)impairmentInfo.get(i)).getImpairmentType()));
				}
			}

		}
		return result;
	}
	/**
	 * This method is the used for getting Impairmment Object
	 * if there is no  Value than it returns Blank String;
	 * @param partyIndex
	 * @return String ImpairmentCredit */
	// ACP015 New Method
	

	protected ArrayList getimpairmentStatusList(int partyIndex){
		Party party = null;
		OLifE olife = nbaTxLife.getOLifE();
		party = olife.getPartyAt(partyIndex);
		ArrayList impairmentInfo = new ArrayList();
		ArrayList result = new ArrayList();
		Person person = party.getPersonOrOrganization().getPerson();
		PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
		if (personExtension!= null){
			impairmentInfo = personExtension.getImpairmentInfo();
			if (impairmentInfo != null){
				for (int i = 0 ; i < impairmentInfo.size(); i ++){
					result.add(i, String.valueOf(((ImpairmentInfo)impairmentInfo.get(i)).getImpairmentStatus()));
				}
			}

		}
		return result;
	}
	
	/**
	 * This method is the used for adding PreferredInfo Object Into the DataBase
	 * @param ArrayList preferredScore
	 * @return void
	 */
	// ACP008 New Method
	protected void updatePreferredInfoList(String partyId, ArrayList prefInfoList) throws NbaBaseException {
		if (prefInfoList != null && prefInfoList.size() != 0) {
			Party party = nbaTxLife.getParty(partyId).getParty();
			UnderwritingAnalysis underwritingAnalysis = null;
			PartyExtension partyExtension = null;
			if (party != null) {
				party.setActionUpdate();
				partyExtension = NbaUtils.getFirstPartyExtension(party);
				if (partyExtension == null) {
					OLifEExtension oli = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PARTY);
					oli.setActionAdd();
					party.addOLifEExtension(oli);
					partyExtension = oli.getPartyExtension();
					partyExtension.setActionAdd();
				}
				underwritingAnalysis = partyExtension.getUnderwritingAnalysis();
				partyExtension.setActionUpdate();
				if (underwritingAnalysis == null) {
					underwritingAnalysis = new UnderwritingAnalysis();
					underwritingAnalysis.setActionAdd();
					partyExtension.setUnderwritingAnalysis(underwritingAnalysis);
				}
				underwritingAnalysis.setActionUpdate();
				for (int j = 0; j < prefInfoList.size(); j++) {
					updatePreferredInfo(underwritingAnalysis, (PreferredInfo)prefInfoList.get(j));
				}
			}
		}
	}

	/**
	 * This method is the used for adding preferredScoreInfo Object Into the DataBase 
	 * @param UnderwritingAnalysis underwritingAnalysis
	 * @param profileInfo profileInfo
	 * @return void
	 */
	// ACP008 New Method

	protected void updatePreferredInfo(UnderwritingAnalysis underwritingAnalysis, PreferredInfo preferredInfo) {
		PreferredInfo preferredInfo2 = underwritingAnalysis.getPreferredInfo();
		if (preferredInfo2 == null) {
			preferredInfo2 = new PreferredInfo();
			preferredInfo2.setActionAdd();
		}
		preferredInfo2.setActionUpdate();
		preferredInfo2.setPrfSet(preferredInfo.getPrfSet());
		preferredInfo2.setPrfApprovedLevel(preferredInfo.getPrfApprovedLevel());
		preferredInfo2.setPrfCalculatedLevel(preferredInfo.getPrfCalculatedLevel());
		preferredInfo2.setPrfUFPResponsesScore(preferredInfo.getPrfUFPResponsesScore());
		preferredInfo2.setPrfTobaccoPremiumBasis(preferredInfo.getPrfTobaccoPremiumBasis());
		underwritingAnalysis.setPreferredInfo(preferredInfo2);

	}


	

	/**
	 * Method that will be overridden by inherited classes to do the proper processing.
	 * @throws NbaBaseException
	 */
	public abstract void execute() throws NbaBaseException;
	
 	/**
	 * Return true if the Contract Evaluation automated process is being performed by comparing
	 * the business process from the configuration entry for the user against the String "APCTEVAL".
	 * @return true if the business process matches the String
	 * @throws NbaBaseException
	 */
	// SPR2652 New Method
	protected boolean performingContractEvaluation() throws NbaBaseException {
		return NbaConstants.PROC_CONTRACT_EVALUATION.equalsIgnoreCase(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
	}
	/**
	 * Return true if the Requirements Evaluation automated process is being performed by comparing
	 * the business process from the configuration entry for the user against the String "APRQEVAL".
	 * @return true if the business process matches the String
	 * @throws NbaBaseException
	 */
	// SPR2652 New Method
	protected boolean performingRequirementsEvaluation() throws NbaBaseException {
		return NbaConstants.PROC_REQUIREMENT_EVALUATION.equalsIgnoreCase(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
	}
	/**
	 * Return true if the Predictive Hold automated process is being performed by comparing
	 * the business process from the configuration entry for the user against the String "APPRDHLD".
	 * @return true if the business process matches the String
	 * @throws NbaBaseException
	 */
	// SR564247(APSL2525)-Full New Method
	protected boolean performingPredictiveHoldProcessing() throws NbaBaseException {
		return NbaConstants.PROC_PREDICTIVE_HOLD.equalsIgnoreCase(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
	}
	/**
	 * Answer the NbaUserVO
	 * @return NbaUserVO
	 */
	// SPR2652 New Method
	protected NbaUserVO getUser() {
		return user;
	}

	/**
	 * Set the NbaUserVO
	 * @param userVO
	 */
	// SPR2652 New Method
	protected void setUser(NbaUserVO userVO) {
		user = userVO;
	}
	/**
	 * Update the NbaOinkRequest with the relation role code and related reference ID from the
	 * primary relationship for the Party. These values are used in OINK processing to locate the
	 * values related to the Party.
	 * @param nbaOinkRequest the NbaOinkRequest
	 * @param partyId - the ID string which uniqualy identifies the Party
	 * @return a boolean which indicates where the values could be set correctly
	 */
	// SPR2652 New Method
	protected boolean updatePartyFilterInRequest(NbaOinkRequest nbaOinkRequest, String partyId) {
		Relation relation = NbaUtils.getRelationForParty(partyId, nbaTxLife.getOLifE().getRelation().toArray());
		if (null != relation) {
			nbaOinkRequest.setPartyFilter(relation.getRelationRoleCode(), relation.getRelatedRefID());
			return true;
		}
		return false;
	}
	/**
	 * Update the NbaOinkRequest with the relation role code and related reference ID from the
	 * primary relationship for the Party. These values are used in OINK processing to locate the
	 * values related to the Party.
	 * @param nbaOinkRequest the NbaOinkRequest
	 * @param aNbaTXLife the NbaTXLife object to use for processing
	 * @param partyId - the ID string which uniqualy identifies the Party
	 * @return a boolean which indicates where the values could be set correctly
	 */
	// SPR2804 New Method
	protected boolean updatePartyFilterInRequest(NbaOinkRequest nbaOinkRequest, NbaTXLife aNbaTXLife, String partyId) {
		Relation relation = NbaUtils.getRelationForParty(partyId, aNbaTXLife.getOLifE().getRelation().toArray());
		if (null != relation) {
			nbaOinkRequest.setPartyFilter(relation.getRelationRoleCode(), relation.getRelatedRefID());
			return true;
		}
		return false;
	}
	/**
	 * Handle communication-related exceptions that may occur during the execution of a remote method call.
	 * Generate a fatal NbaBaseException encapsulating the error.
	 * @param remoteException - the Exception
	 * @param evaluationType - String identifying the Evaluation Type 
	 * @throws NbaBaseException
	 */
	// SPR2652 New Method
	protected void handleRemoteException(RemoteException remoteException, String evaluationType) throws NbaBaseException { 
		throw new NbaBaseException("Remote Exception occured in " + evaluationType, remoteException, NbaExceptionType.FATAL);
	}
	
    /**
     * Sets keys for Summary value object
     * @param sumValue Summary value object
     */
    //SPR2741 New Method
    protected void setSumValuesKeys(SummaryValues sumValue){
        sumValue.setParentIdKey(partyID);
        sumValue.setCompanyKey(companyKey);
        sumValue.setContractKey(contractNo);
        sumValue.setBackendKey(backEndKey);
    }
    
    /**
     * Sets keys for Default value object
     * @param defValue defaultvalue object 
     */
    //SPR27411 New Method
    protected void setDefValuesKeys(DefaultValues defValue){
        defValue.setParentIdKey(partyID);
        defValue.setCompanyKey(companyKey);
        defValue.setContractKey(contractNo);
        defValue.setBackendKey(backEndKey); 
    }
	/**
	 * This method deOinks the OLiFE FormInstance information.
	 * @param deOink: deOink Map
	 * @param oinkRequest
	 * @param accessContract
	 * @return void	
	 * @throws NbaBaseException
	 */		
	// AXAL3.7.07 New Method
   protected void deOinkFormInstance(Map deOink){
  	ArrayList formInstanceList = nbaTxLife.getOLifE().getFormInstance();
	FormInstance formInstance = null;
	FormResponse formResponse = null;
	FormResponseExtension formResponseX = null; //CR61047
	int formCnt = formInstanceList.size();
	int listSize = 0;
	String formName = "";
	String queNo = null;
	String qAtt = null ; //CR61047
	String queText = null;
	int responseCode = 0;
	deOink.put("A_no_of_Forms", (new Integer(formCnt)).toString());

	for (int i=0; i < formCnt; i++){
		formInstance = (FormInstance) formInstanceList.get(i);
		if (formInstance != null) {
			formName = formInstance.getFormName();
			listSize = formInstance.getFormResponseCount();
		}
		else {
			formName = "";
			listSize = 0;
		}
		
		if (i == 0) {
			deOink.put("A_FormName", formName);
			deOink.put("A_no_of_Responses", (new Integer(listSize)).toString());
		} else {
			deOink.put("A_FormName[" + i + "]", formName);
			deOink.put("A_no_of_Responses[" + i + "]", (new Integer(listSize)).toString());
		}
		for(int k =0; k<listSize; k++){
			formResponse = formInstance.getFormResponseAt(k);
			formResponseX = NbaUtils.getFirstFormResponseExtension(formResponse); //CR61047
			if (formResponse != null) {
				queNo = formResponse.getQuestionNumber();
				responseCode = formResponse.getResponseCode();
				queText = formResponse.getQuestionText();
				qAtt = formResponseX != null ? formResponseX.getQuestionTypeAbbr() : ""; //CR61047
			}
			else {
				queNo = "";
				responseCode = 0;
				queText = "";
				qAtt = ""; //CR61047
			}
			if(i == 0 && k == 0){
				deOink.put("A_QuestionNumber", queNo);
				deOink.put("A_ResponseCode", String.valueOf(responseCode));
				deOink.put("A_QuestionText", queText);
				deOink.put("A_QuestionAbbr", qAtt); //CR61047
			}
			else {
				deOink.put("A_QuestionNumber[" + i + "," + k + "]", queNo);
				deOink.put("A_ResponseCode[" + i + "," + k + "]", String.valueOf(responseCode));
				deOink.put("A_QuestionText[" + i + "," + k + "]", queText);
				deOink.put("A_QuestionAbbr[" + i + "," + k + "]", qAtt); //CR61047
			}
		}  // end inner for
	 } // end outter for
   }
	/**
	 * This function is to deOink ExpenseNeedTypeCode values for the contract.
	 * @param accessContract: The NbaOinkDataAccess having all the oink sources	 	
	 */	
	// AXAL3.7.07 New Method
	protected void deOinkExpenseNeedTypeCode(Map deOinkMap, NbaOinkDataAccess accessContract, NbaOinkRequest oinkRequest) throws NbaBaseException {
		 oinkRequest.setVariable("ExpenseNeedTypeCodeCC");
		 String[] codeList = accessContract.getStringValuesFor(oinkRequest);
		 int count = codeList.length;
		 deOinkMap.put("A_no_of_ExpenseNeedTypeCode", new Integer(count).toString());
		 if (count == 0) {
			 deOinkMap.put("A_ExpenseNeedTypeCode", "");
		 } else {
			 for (int i = 0; i < count; i++) {
			 	if (i==0) {
			 		 deOinkMap.put("A_ExpenseNeedTypeCode", codeList[i]);
			 	}
			 	else {
			 		 deOinkMap.put("A_ExpenseNeedTypeCode[" + i + "]", codeList[i]);
			 	}
			 }
		 }
	}	
	
	/**
	 * This function is to deOink FundingDisclosureTC values for the contract.
	 * @param accessContract: The NbaOinkDataAccess having all the oink sources	 	
	 */	
	// AXAL3.7.07 New Method
	protected void deOinkFundingDisclosureTC(Map deOinkMap, NbaOinkDataAccess accessContract, NbaOinkRequest oinkRequest) throws NbaBaseException {
		 oinkRequest.setVariable("FundingDisclosureTC");
		 String[] codeList = accessContract.getStringValuesFor(oinkRequest);
		 int count = codeList.length;
		 deOinkMap.put("A_no_of_FundingDisclosureTC", new Integer(count).toString());
		 if (count == 0) {
			 deOinkMap.put("A_FundingDisclosureTC", "");
		 } else {
			 for (int i = 0; i < count; i++) {
			 	if (i==0) {
			 		 deOinkMap.put("A_FundingDisclosureTC", codeList[i]);
			 	}
			 	else {
			 		 deOinkMap.put("A_FundingDisclosureTC[" + i + "]", codeList[i]);
			 	}
			 }
		 }
	}	

	/**
	 * This method deOinks the Agent information.
	 * @param deOink: deOink Map
	 * @param accessContract
	 * @return void	
	 * @throws NbaBaseException
	 */		
	// AXAL3.7.07 New Method
   protected void deOinkAgentInfo(Map deOink, NbaOinkDataAccess accessContract) throws NbaBaseException{
	 NbaOinkRequest oinkRequest = new NbaOinkRequest();
	 oinkRequest.setVariable("AgentLicNumList_PWA");
	 String[] codeList = accessContract.getStringValuesFor(oinkRequest);
	 int count = codeList.length;
	 deOink.put("A_no_of_Agents", new Integer(count).toString());
	 if (count == 0) {
		 deOink.put("A_AgentLicNum", "");
	 } else {
		 for (int i = 0; i < count; i++) {
		 	if (i==0) {
		 		 deOink.put("A_AgentLicNum", codeList[i]);
		 	}
		 	else {
		 		 deOink.put("A_AgentLicNum[" + i + "]", codeList[i]);
		 	}
		 }
	 }
  }
   
	/**
	 * This method deOinks the Owner USCitizenIndCode information.
	 * @param deOink: deOink Map
	 * @return void	
	 * @throws NbaBaseException
	 */		
	// AXAL3.7.07 New Method
  protected void deOinkOwnerInfo(Map deOinkMap) throws NbaBaseException{
  	int count = getOwnersCount();
  	 NbaOinkRequest oinkRequest = new NbaOinkRequest();
  	 NbaOinkDataAccess dataAccess = new NbaOinkDataAccess(nbaTxLife);
  	 String[] valueList = null;
  	
  	if (count > 0) {
  		oinkRequest.setVariable("USCitizenshipIndCodeList_OWN");
  		oinkRequest.setCount(count);
		valueList = dataAccess.getStringValuesFor(oinkRequest);
		deOinkMap.put("A_USCitizenIndCodeOwnerList", valueList);
	 } else {
		deOinkMap.put("A_USCitizenIndCodeOwnerList", "");
	 }

	 deOinkMap.put("A_no_of_USCitizenIndCodeOwnerList", new Integer(count).toString());
  }
  
	/**
	 * Return the number of contingent beneficiaries in the contract
	 * @return int: number of contingent beneficiaries in the contract
	 */
  //AXAL3.7.07 New Method
	public int getBenOrCoBenCount(long relationRoleCode) {
		ArrayList relations = nbaTxLife.getOLifE().getRelation();
		Relation relation = null;
		int count = 0;
		int listSize = relations.size();
		HashMap partyMap = new HashMap();
		String relatedObjectId = "";
		for (int i = 0; i < listSize; i++) {
			relation = (Relation) relations.get(i);
			if (relation != null) {
				relatedObjectId = relation.getRelatedObjectID();
				if (relation.getRelationRoleCode() == relationRoleCode && !partyMap.containsKey(relatedObjectId)
						&& partyID.equals(relation.getOriginatingObjectID())) {//ALII711 QC6125 
					partyMap.put(relatedObjectId, "");
					count++;
				}
			}
		}
		return count;
	}
	
	/**
	 * Return the number of owners in the contract
	 * @return int: number of owners in the contract
	 */
//	AXAL3.7.07 New Method
	public int getOwnersCount(){
		ArrayList relations = nbaTxLife.getOLifE().getRelation();
		Relation relation = null;
		int ownCount = 0;
		int listSize = relations.size();
		for (int i = 0; i < listSize; i++) {
			relation = (Relation) relations.get(i);
			if (relation != null) {
				if(relation.getRelationRoleCode()== NbaOliConstants.OLI_REL_OWNER){
					ownCount++;
				}
			}
		}
		return ownCount;
	}
		
	/**
	 * This function calculates the ReltoAnnOrIns deOink values
	 * @param deOinkMap	 	
	 * @param qualifier: Party Qualifier, valid qualifiers are OWN, BEN, CBN
	 * @throws NbaBaseException
	 */		
	//AXAL3.7.07 New Method
	public void getRelationDeOinkValues(HashMap deOinkMap, String qualifier) throws NbaBaseException {
		int count = 0;
		String oinkVarName = "ReltoAnnOrIns_" + qualifier;
		String lengthAttrName = "A_no_of_" + oinkVarName;
		String attrName = "A_" + oinkVarName;
		NbaOinkDataAccess dataAccess = new NbaOinkDataAccess(nbaTxLife);
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		if (qualifier.equals(NbaContractDataAccessConstants.PARTY_OWNER)) {
			count = getOwnersCount();
		} else if (qualifier.equals(NbaContractDataAccessConstants.PARTY_BENEFICIARY)) {
			count = getBenOrCoBenCount(NbaOliConstants.OLI_REL_BENEFICIARY);
		} else if (qualifier.equals(NbaContractDataAccessConstants.PARTY_COBENEFICIARY)) {
			count = getBenOrCoBenCount(NbaOliConstants.OLI_REL_CONTGNTBENE);
		} else if (qualifier.equals(NbaContractDataAccessConstants.PARTY_APPLCNT)) {//ALII105
			oinkRequest.setVariable(oinkVarName);
			deOinkMap.put(attrName, dataAccess.getStringValueFor(oinkRequest));
			return;
		}
		deOinkMap.put(lengthAttrName, String.valueOf(count));
		if (count > 0) {
			oinkRequest.setVariable(oinkVarName);
			oinkRequest.setCount(count);
			String[] valueList = dataAccess.getStringValuesFor(oinkRequest);
			deOinkMap.put(attrName, valueList);
		} else {
			deOinkMap.put(attrName, "");
		}
	}
    //AXAL3.7.07 code deleted.
}
