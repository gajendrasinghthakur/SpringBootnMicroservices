/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

package com.csc.fsg.nba.business.transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeAppComparator;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeConstants;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeCovOptionComparator;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeCoverageComparator;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeEntry;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeFinancialActivityComparator;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeImpairmentComparator;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangePartyComparator;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeRelationComparator;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeRequirementInfoComparator;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeSubstandardRatingComparator;
import com.csc.fsg.nba.database.AxaGIAppOnboardingDataAccessor;
import com.csc.fsg.nba.database.NbaContractDataBaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.AxaGIAppOnboardingDataVO;
import com.csc.fsg.nba.vo.NbaContractVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProducerVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;

/**
 * 
 * This class encapsulates checks whenever following changes are made to the Insured or Owner roles on the policy. - Name. - Address. - Tax
 * Identification. - Tax Identification Type. - Gender/Sex. - Date of Birth and following changes are made on a contract - Policy Status - Plan Change -
 * Agent information
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
 * <tr><td>AXAL3.7.21</td><td>AXA Life Phase 1</td><td>Prior Insurance</td></tr>
 * <tr><td>AXAL3.7.22</td><td>AXA Life Phase 1</td><td>Compensation Interface</td></tr>
 * <tr><td>P2AXAL007</td><td>AXA Life Phase 2</td><td>Producer and Compensation</td></tr>
 * <tr><td>P2AXAL038</td><td>AXA Life Phase 2</td><td>Zip Code Interface</td></tr>
 * <tr><td>P2AXAL041</td><td>AXA Life Phase 2</td><td>Message received from OLSA Unit Number Validation Interface</td></tr>
 * <tr><td>CR61627</td><td>AXA Life Phase 2</td><td>Assign Replacement Case Manager</td></tr>
 * <tr><td>P2AXAL053</td><td>AXA Life Phase 2</td><td>R2 Auto Underwriting</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaDataChangeManager implements AxaDataChangeConstants{
	protected NbaLogger logger = null;
	private Map changeRegister= new HashMap();
	private  List interfaceListners;
	//AXAL3.7.22 begin
	private List dbPartyList;
	private List dbSystemMessagesList;
	private List dbRelatonList;
	private List dbCoverageList;
	private List dbHoldingList;
	private NbaDst nbaDst;//ALS4633
	private Holding dbPrimaryHolding;//ALS4633
	private AxaGIAppOnboardingDataAccessor onboardingDataAccessor = null; //NBLXA-2299
	//AXAL3.7.22 end


	/**
	 * 
	 */
	public AxaDataChangeManager() {
		super();
		//Add listners here
		interfaceListners = new ArrayList();
		interfaceListners.add(new AxaGenerateValidationTransaction());//ALS4153
		interfaceListners.add(new AxaGenerateReissueValidationTransaction()); //APSL3360
		interfaceListners.add(new AxaUpdateReEvalTransaction());
		interfaceListners.add(new AxaUpdateCIFTransaction());
		interfaceListners.add(new AxaUpdatePriorInsuranceTransaction());
		interfaceListners.add(new AxaUpdateReinsuranceTransaction());
		interfaceListners.add(new AxaSubmitExpressCommTransaction());//ALS4633
		//interfaceListners.add(new AxaUpdateExpressCommTransaction()); Commented for APSL3655 supressing Update ECS.
		interfaceListners.add(new AxaDeclineExpressCommTransaction());//ALS4633
		interfaceListners.add(new AxaUpdateMIBTransaction());//ALS3963
		interfaceListners.add(new AxaUpdatePIRSTransaction());//ALS4287
		interfaceListners.add(new AxaPostApprovalTransaction());//ALS4659
		interfaceListners.add(new AxaInformalUnapprovallTransaction()); //ALS5701
		interfaceListners.add(new AxaPrePaymentCommTransaction()); //P2AXAL007
		interfaceListners.add(new AxaValidateZipCodeTransaction()); //P2AXAL038
		interfaceListners.add(new AxaInvokeOLSATransaction()); //P2AXAL041
		interfaceListners.add(new AxaUpdateReplNotificationTransaction()); //CR61627
		interfaceListners.add(new AxaPDCRequirementTransaction()); // APSL4112
		//interfaceListners.add(new AxaUpdateCriticalDataChangeTransaction()); //APSL4067 
		interfaceListners.add(new Axa1035ExchangeKitRequirementTransaction()); // APSL4280
		interfaceListners.add(new AxaRetrieveCIPTransaction()); // APSL4412
		 interfaceListners.add(new AxaRetrieveMiscWorkForLoanCarryOver()); // APSL4871
		 interfaceListners.add(new AxaRetrieveMiscWorkTxnForPDR());//APSL4967
		interfaceListners.add(new AxaUpdatePrintTogetherForGI());// NBLXA-188(APSL5318) Legacy Decommissioning
		interfaceListners.add(new AxaRetrieveLicensingWorkTxnForLicensingError());// NBLXA-188(APSL5318) Legacy Decommissioning
		interfaceListners.add(new AxaUpdateRequestedPolDateReason());// NBLXA-1539 
		interfaceListners.add(new AxaUpdateReassingmentTransaction());// NBLXA-1538
		interfaceListners.add(new AxaUpdateBusinessStrategiesTransaction());// NBLXA-1823
		interfaceListners.add(new AxaSignificantValidationErrorTransaction());// NBLXA-1954 
		interfaceListners.add(new AxaCIPETransaction()); //NBLXA-2108
		interfaceListners.add(new AxaUpdateValidationErrorTransaction()); //NBLXA-1850
		interfaceListners.add(new AxaUpdateInsuredDataTransaction()); //NBLXA-2162
		interfaceListners.add(new AxaValidateUWCVsTransaction());//NBLXA-2398
		interfaceListners.add(new AxaUpdateOnboardingTransaction());//NBLXA-2299
		interfaceListners.add(new AxaUpdateValidationErrorTransactionForMultipleDraftCV()); //NBLXA-2519
		interfaceListners.add(new AxaMagnumReceiveDataSourcesTransaction());//NBLXA-2402 (NBLXA-2602) US#297686
		interfaceListners.add(new AxaPrintValidationErrorTransaction());//NBLXA-2620
	}
/**
	 * This method checks the changes made to the Insured, Owner, Agent roles on the policy or the contract
	 * @param newTXLife
	 * @return status of call
	 */
	public void determineDataChange(NbaTXLife newTXLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		int holdingCount = newTXLife.getOLifE().getHoldingCount();
		boolean primaryholdingInd = true;
		dbPartyList = NbaContractDataBaseAccessor.getInstance().selectParty(getArgs(newTXLife));
		dbHoldingList = NbaContractDataBaseAccessor.getInstance().selectHolding(getArgs(newTXLife));
		dbRelatonList = NbaContractDataBaseAccessor.getInstance().selectRelation(getArgs(newTXLife));
		dbSystemMessagesList = NbaContractDataBaseAccessor.getInstance().selectSystemMessage(getArgs(newTXLife.getOLifE().getHoldingAt(0)));//AXAL3.7.22
		this.nbaDst = nbaDst;//ALS4633
		if (dbPartyList == null && dbHoldingList == null && dbRelatonList == null) {
			//first time insert
			if (!NbaConstants.PROC_CONTRACT_CHANGE.equalsIgnoreCase(newTXLife.getBusinessProcess())) {//P2AXAL041
				new NbaOLifEId(newTXLife).resetIds(newTXLife);//APSL806
				registerChange(DC_NEW_CONTRACT, newTXLife.getPolicy());
				registerSystemMessageChanges(newTXLife,nbaDst);//P2AXAL007,NBLXA-2398
				registerSystemMessageChangesForAgentCV(newTXLife); //NBLXA-1337
			}
		} else {
			//ALS4633 code deleted
			Holding dbHolding = null;
			Holding newHolding = null;
			for (int i = 0; i < holdingCount; i++) {
				newHolding = newTXLife.getOLifE().getHoldingAt(i);
				dbHolding = getMatchingHolding(dbHoldingList, newHolding);
				if (primaryholdingInd) {
					dbPrimaryHolding = dbHolding;
				}
				registerHoldingChanges(newHolding, dbHolding, primaryholdingInd);
				primaryholdingInd = false;
			}
			Policy newPolicy = newTXLife.getPolicy();
			Policy dbPolicy = dbPrimaryHolding.getPolicy();
			//Register different Changes
			registerPolicyChanges(newPolicy, dbPolicy, user, nbaDst);//ALS4633, APSL4585 Register Date Change
			registerCWAChanges(newPolicy,dbPolicy,newTXLife);//APSl4871,APSL4967
			registerCoverageChanges(newPolicy, dbPolicy, newTXLife.getBusinessProcess());//APSL4697
			registerRelationChanges(newTXLife.getOLifE().getRelation());//AXAL3.7.22 method signature changed
			registerPartyChanges(newTXLife);//AXAL3.7.22 method signature changed
			registerImpairment(newTXLife);//ALS2611
			registerRequirementInfoChanges(newPolicy, dbPolicy, nbaDst, user);//ALS4287 //APSL550
			registerSystemMessageChanges(newTXLife,nbaDst);//P2AXAL007
			registerPrintTogetherChangesForGI(newPolicy, dbPolicy, newTXLife);// NBLXA-188(APSL5318) Legacy Decommissioning
			registerSystemMessageChangesForAgentCV(newTXLife);//NBLXA-1337
			registerAppInfoExtChangesForRequestedPolDateReason(newPolicy, dbPolicy, user, newTXLife);
			registerSystemMessageChangesForMoney(newTXLife); //NBLXA-1896
			registerBusinessStrategiesIndChange(newPolicy, dbPolicy);//NBLXA-1823
			registerSignificantValidationErrorResolved(newTXLife.getPrimaryHolding(),dbPrimaryHolding);//NBLXA-1954
			registerNCFValidationError(newTXLife);//NBLXA-1850
			registerSystemMessageChangesForMultipleDraftCV(newTXLife);//NBLXA-2519
			// NBLXA-2299 Starts
			if (!user.isAutomatedProcess()) {
				registerLNBridgerAlertValidationErrorResolved(newTXLife.getPrimaryHolding(), dbPrimaryHolding, newTXLife); // NBLXA-2299
				registerBAEAlertValidationErrorResolved(newTXLife.getPrimaryHolding(), dbPrimaryHolding, newTXLife); // NBLXA-2299
			}
			// NBLXA-2299 Ends
			registerPrintValidationErrorResolved(newTXLife.getPrimaryHolding(),dbPrimaryHolding);// NBLXA-2620
				}
		callInterfaces(newTXLife, user, nbaDst);
	}
	
	/**
	 * 
	 * @param contractVOObjects
	 * @param objectToBeMatched
	 * @return
	 */
	public NbaContractVO getMatchingObject(List contractVOObjects, NbaContractVO objectToBeMatched) {
		if(objectToBeMatched != null){
			int noOfObjects = (contractVOObjects == null) ? 0 : contractVOObjects.size();
			NbaContractVO dbObject;
			for (int i = 0; i < noOfObjects; i++) {
				dbObject = (contractVOObjects.get(i) instanceof NbaContractVO) ? (NbaContractVO) contractVOObjects.get(i) : null;
				if (dbObject != null && dbObject.getId().equalsIgnoreCase(objectToBeMatched.getId())) {
					return dbObject;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param contractVOObjects
	 * @param objectToBeMatched
	 * @return
	 */
	 //AXAL3.7.22 new method
	public NbaContractVO getContractVO(List contractVOObjects, String objectIdToBeMatched) {
		int noOfObjects = (contractVOObjects == null) ? 0 : contractVOObjects.size();
		NbaContractVO dbObject;
		for (int i = 0; i < noOfObjects; i++) {
			dbObject = (contractVOObjects.get(i) instanceof NbaContractVO) ? (NbaContractVO) contractVOObjects.get(i) : null;
			if (dbObject != null && dbObject.getId().equalsIgnoreCase(objectIdToBeMatched)) {
				return dbObject;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param contractVOObjects
	 * @param objectToBeMatched
	 * @return
	 */
	public Holding getMatchingHolding(List contractVOObjects, Holding objectToBeMatched) {
		NbaContractVO contractVO = getMatchingObject(contractVOObjects, objectToBeMatched);
		if (contractVO != null) {
			return (Holding) contractVO;
		}
		return null;
	}
	/**
	 * 
	 * @param contractVOObjects
	 * @param objectToBeMatched
	 * @return
	 */
	public NbaParty getMatchingParty(List contractVOObjects, NbaParty objectToBeMatched) {
		NbaContractVO contractVO = getMatchingObject(contractVOObjects, objectToBeMatched.getParty());
		if (contractVO != null) {
			return new NbaParty((Party) contractVO);
		}
		return null;
	}
	
	 //AXAL3.7.22 new method
	public NbaParty getParty(List contractVOObjects, String partyIdToBeMatched) {
		NbaContractVO contractVO = getContractVO(contractVOObjects, partyIdToBeMatched);
		if (contractVO != null) {
			return new NbaParty((Party) contractVO);
		}
		return null;
	}	
	/**
	 * 
	 * @param contractVOObjects
	 * @param objectToBeMatched
	 * @return
	 */
	public Relation getMatchingRelation(List contractVOObjects, Relation objectToBeMatched) {
		NbaContractVO contractVO = getMatchingObject(contractVOObjects, objectToBeMatched);
		if (contractVO != null) {
			return (Relation) contractVO;
		}
		return null;
	}
	
	/**
	 * 
	 * @param contractVOObjects
	 * @param objectToBeMatched
	 * @return
	 */
	  //AXAL3.7.22 new method
	public Relation getMatchingRelation(List contractVOObjects, long relationRoleCode) {
		Iterator contractVOItr = contractVOObjects.iterator();
		while(contractVOItr.hasNext()){
			NbaContractVO contractVO = (NbaContractVO) contractVOItr.next();
			if(relationRoleCode == ((Relation)contractVO).getRelationRoleCode()){
				return (Relation)contractVO;
			}
		}
		return null;
	}
	/**
	 * 
	 * @param contractVOObjects
	 * @param objectToBeMatched
	 * @return
	 */
	public Coverage getMatchingCoverage(List contractVOObjects, Coverage objectToBeMatched) {
		NbaContractVO contractVO = getMatchingObject(contractVOObjects, objectToBeMatched);
		if (contractVO != null) {
			return (Coverage) contractVO;
		}
		return null;
	}
	/**
	 * 
	 * @param contractVOObjects
	 * @param objectToBeMatched
	 * @return
	 */
	//ALS4287 New Method
	public RequirementInfo getMatchingRequirementInfo(List contractVOObjects, RequirementInfo objectToBeMatched) {
		NbaContractVO contractVO = getMatchingObject(contractVOObjects, objectToBeMatched);
		if (contractVO != null) {
			return (RequirementInfo) contractVO;
		}
		return null;
	}
	
	/**
	 * 
	 * @param contractVOObjects
	 * @param objectToBeMatched
	 * @return
	 */
	//ALS4153 New Method
	public CovOption getMatchingCovOption(List contractVOObjects, CovOption objectToBeMatched) {
		NbaContractVO contractVO = getMatchingObject(contractVOObjects, objectToBeMatched);
		if (contractVO != null) {
			return (CovOption) contractVO;
		}
		return null;
	}
	
	
	/**
	 * Return array of keys for query
	 * @param party
	 * @return query object array
	 */
	private Object[] getArgs(NbaTXLife nbaTXLife) {
		Object args[] = new Object[4];
		args[0] = nbaTXLife.getOLifE().getId();
		args[1] = nbaTXLife.getOLifE().getContractKey();
		args[2] = nbaTXLife.getOLifE().getCompanyKey();
		args[3] = nbaTXLife.getOLifE().getBackendKey();
		return args;
	}
	
	//AXAL3.7.22 getArgs() method overloaded
	public Object[] getArgs(Holding holding) {
		Object args[] = new Object[4];
		args[0] = holding.getId();
		args[1] = holding.getContractKey();
		args[2] = holding.getCompanyKey();
		args[3] = holding.getBackendKey();
		return args;
	}
	

	/**
	 * Checks change in Holding
	 * @param newHolding
	 * @param dbHolding
	 * @param primaryholdingInd
	 */
	protected void registerHoldingChanges(Holding newHolding,Holding dbHolding,boolean primaryholdingInd){
		//register Holding Changes
	}
	
	/**
	 * Checks change in Policy Information
	 * @param newPolicy
	 * @param oldPolicy
	 */
	protected void registerPolicyChanges(Policy newPolicy, Policy oldPolicy, NbaUserVO user, NbaDst nbaDst) {
		//register Policy Changes
		AxaDataChangeAppComparator appComparator = new AxaDataChangeAppComparator(newPolicy, oldPolicy);

		// Begin NBLXA-1538
		if (appComparator.isDistChannelChanged()) {
			registerChange(DC_DIST_CHANNEL_CHANGED, newPolicy);
		}
		// End NBLXA-1538
		
		if (appComparator.isFaceAmountChanged()) {
			registerChange(DC_FACE_AMT, newPolicy);
		}
		//Begin APSL3360
		if (appComparator.isFaceAmountIncreased()) {
			registerChange(DC_FACE_AMT_INCREASE, newPolicy);
		}
		//End APSL3360
		// BeginAPSL4112
		if (appComparator.isPaymentAmountChanged()) {
            registerChange(DC_PAYMENT_AMOUNT, newPolicy);
        }
		// End APSL4112
		if (appComparator.isPendingcontractStatusChanged()) {
			registerChange(DC_PENDING_CONTRACT_STATUS, newPolicy);
		}
		if (appComparator.isUnderwritingStatusChanged()) {
			registerChange(DC_UNDERWRITING_STATUS, newPolicy);
		}
		if (appComparator.isPlanChanged()) {
			registerChange(DC_PRODUCTCODE, newPolicy);
		}

		if (appComparator.isSignedDateChanged()) {
			registerChange(DC_SIGNEDDATE, newPolicy);
		}
		//Begin AXAL3.7.21
		if (appComparator.isIssueDateChanged()) {
			registerChange(DC_ISSUEDATE, newPolicy);
		}
		//End AXAL3.7.21
		//ALPC136
//		if(appComparator.isReplacementTypeChanged()){ //ALS4633 commented
		if(appComparator.isReplTypeChangedToDeclineECS()){//ALS4633
			registerChange(DC_REPL_TYPE_CHG, newPolicy);
		}
		
		//Begin ALS4153
		if(appComparator.isRequestedPolicyDateChanged()){
			//APSL4585 Register  Date Changes
			String commentText = NbaConstants.REGISTER_DATE_UPDATED;
			NbaUtils.addGeneralComment(nbaDst, user, commentText);
			registerChange(DC_POL_REQUESTEDDATE, newPolicy);
		}
		if(appComparator.isUnderwriterApprovalChanged()){
			registerChange(DC_UND_APPROVAL_CHANGED, newPolicy);
		}
		//End ALS4153
		if (appComparator.isInformalAppApprovalChanged()) { //ALS5701
			registerChange(DC_INFORMAL_UNAPPROVED, newPolicy); //ALS5701
		} //ALS5701
		//Begin P2AXAL041
		if(appComparator.isBillingNumberChanged()){
			registerChange(DC_BILLING_UNIT_NUMBER, newPolicy);
		}
		if(appComparator.isPaymentMethodChanged()){
			registerChange(DC_PAYMENT_METHOD, newPolicy);
		}
		//End P2AXAL041
		//Begin APSL3259
		if(appComparator.isAppStateChanged()){
			registerChange(DC_APP_STATE, newPolicy);
		}
		//End APSL3259
		int noOfNewFinancialActivities = newPolicy.getFinancialActivityCount();
		int noOfOldFinancialActivities = oldPolicy.getFinancialActivityCount();
		FinancialActivity newFinancialActivity;
		FinancialActivity oldFinancialActivity;
		for (int i = 0; i < noOfNewFinancialActivities; i++) {
			newFinancialActivity = newPolicy.getFinancialActivityAt(i);
			oldFinancialActivity = null;
			for (int j = 0; j < noOfOldFinancialActivities; j++) {
				if (newFinancialActivity.getId().equalsIgnoreCase(oldPolicy.getFinancialActivityAt(j).getId())) {
					oldFinancialActivity = oldPolicy.getFinancialActivityAt(j);
					break;
				}
			}
			AxaDataChangeFinancialActivityComparator finActcomparator = new AxaDataChangeFinancialActivityComparator(newFinancialActivity,
					oldFinancialActivity);
			if (finActcomparator.isFinancialActivityReversed()) {
				registerChange(DC_FIN_ACT_REVERSE, newFinancialActivity);
			}

		}
		if (appComparator.is1035ExchangeChanged() || appComparator.isReplacementIndChanged()) { //CR61627
			registerChange(DC_REPLACEMENT_IND, newPolicy);										//CR61627
		} //CR61627
	
		// Begin NBLXA-1831
		if (appComparator.isGoldenTicketIndChanged()) {
			registerChange(DC_GOLDEN_IND, newPolicy);
		}
		// End NBLXA-1831
		//NBLXA-2108 Begins
		if(appComparator.isCIPEIndRemoved()){
			registerChange(DC_INITIAL_PREMIUM_METHOD, newPolicy);
		}
		//NBLXA-2108 Ends
	}
	/**
	 * Checks change in Coverage
	 * @param newPolicy
	 * @param dbPolicy
	 */
	//ALS4287 New Method
	private void registerRequirementInfoChanges(Policy newPolicy, Policy dbPolicy, NbaDst nbaDst, NbaUserVO user) throws NbaBaseException { //APSL550
		if (newPolicy != null && dbPolicy != null) {
			int newReqCount = newPolicy.getRequirementInfoCount();
			for (int i = 0; i < newReqCount; i++) {
				RequirementInfo newReqInfo = newPolicy.getRequirementInfoAt(i);
				RequirementInfo oldReqInfo = getMatchingRequirementInfo(dbPolicy.getRequirementInfo(), newReqInfo);
				//APSL550 begin - If a new PostApproval requirement is added, send feed to PIRS if any of the Print work item is in End queue and
				// case is not reissued.
				if (NbaUtils.isBlankOrNull(oldReqInfo)) {
					if ((newReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_SIGNILLUS
							|| newReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT || newReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_1009800041)
							&& (NbaOliConstants.OLI_REQSTAT_ADD == newReqInfo.getReqStatus()
									|| NbaOliConstants.OLI_REQSTAT_ORDER == newReqInfo.getReqStatus()
									|| NbaOliConstants.OLI_REQSTAT_SUBMITTED == newReqInfo.getReqStatus() || NbaOliConstants.OLI_REQSTAT_RECEIVED == newReqInfo
									.getReqStatus())) { //APSL1190 - Added check for Received status incase new req is generated from MISCMAIL

						NbaSearchVO searchVO = new NbaSearchVO();
						searchVO.setContractNumber(nbaDst.getNbaLob().getPolicyNumber());
						searchVO.setWorkType(NbaConstants.A_WT_CONT_PRINT_EXTRACT);
						searchVO.setQueue(NbaConstants.END_QUEUE);
						searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
						searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
						ApplicationInfo appInfo = newPolicy.getApplicationInfo();
						boolean reIssue = appInfo != null && NbaOliConstants.OLI_APPTYPE_REISSUE == appInfo.getApplicationType();
						if (searchVO.getSearchResults() != null && searchVO.getSearchResults().size() > 0 && !reIssue) {
							registerChange(DC_PSTAPPREQ_ADDED, newReqInfo, NbaOliConstants.OLI_REQUIREMENTINFO);
						}
					}
					//APSL4280 begin 
					if (newReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_1009800081) {
						if (newReqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_APPROVED
								|| newReqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_COMPLETED
								|| newReqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED) {
							if(NbaUtils.is1035Case(newPolicy) && NbaUtils.isReg60Case(newPolicy)){ //APSL4472
								registerChange(DC_1035EX_KIT_RECEIVED, newReqInfo, NbaOliConstants.OLI_REQUIREMENTINFO);
							}
						}
					}
					//APSL4280 end
					
				}
				//APSL550 end
				AxaDataChangeRequirementInfoComparator reqInfoComparator = new AxaDataChangeRequirementInfoComparator(newReqInfo, oldReqInfo);
				if (reqInfoComparator.isRequirementStatusChanged()) {
					if (reqInfoComparator.isReceived()) {
						if (newReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_SIGNILLUS) {
							registerChange(DC_SIGNILLUS_RECEIVED, newReqInfo, NbaOliConstants.OLI_REQUIREMENTINFO);
						} else if (newReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT) {
							registerChange(DC_POLDELRECEIPT_RECEIVED, newReqInfo, NbaOliConstants.OLI_REQUIREMENTINFO);
						} // APSL550 begin - added condition for Premium Quote requirement(1009800041)
						else if (newReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_1009800041) {
							registerChange(DC_PREMQUOTE_RECEIVED, newReqInfo, NbaOliConstants.OLI_REQUIREMENTINFO);
						} else if (newReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED) { // NBLXA-2402 (NBLXA-2602) US#297686 |
																											// Start
							registerChange(DC_MAGNUM_DATASOURCE_RECIEVED, newReqInfo, NbaOliConstants.OLI_REQUIREMENTINFO);
						} // NBLXA-2402 (NBLXA-2602) US#297686 | End
					}
					//APSL1441 begin - send feed to PIRS if Premium Quote is waived
					if (reqInfoComparator.isWaived()) {
						if (newReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_1009800041) {
							registerChange(DC_PREMQUOTE_WAIVED, newReqInfo, NbaOliConstants.OLI_REQUIREMENTINFO);
						}
					}
					//APSL1441 end
					//APSL4280 begin 
					if (newReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_1009800081) {
						if (reqInfoComparator.isApproved()|| reqInfoComparator.isCompleted()|| reqInfoComparator.isReceived()){
							if(NbaUtils.is1035Case(newPolicy) && NbaUtils.isReg60Case(newPolicy)){ //APSL4472
								registerChange(DC_1035EX_KIT_RECEIVED, newReqInfo, NbaOliConstants.OLI_REQUIREMENTINFO);
							}
						}
					}
					//APSL4280 end
				}
			}
		}
	}
	/**
	 * Checks change in Coverage
	 * 
	 * @param newTXLife
	 * @param prevTXLife
	 */
	//APSL4697 method signature changed.
	private void registerCoverageChanges(Policy newPolicy, Policy dbPolicy, String businessProcess) {
		Life newLife = newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife() ? newPolicy
				.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife() : null;
		Life dbLife = dbPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife() ? dbPolicy
				.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife() : null;
		if (newLife != null && dbLife != null) {
			int newCoverageCount = newLife.getCoverageCount();
			Coverage oldCoverage;
			Coverage newCoverage;
			AxaDataChangeCoverageComparator coverageComparator;
			for (int i = 0; i < newCoverageCount; i++) {
				newCoverage = newLife.getCoverageAt(i);
				oldCoverage = getMatchingCoverage(dbLife.getCoverage(), newCoverage);
				coverageComparator = new AxaDataChangeCoverageComparator(newCoverage, oldCoverage);
				if (newCoverage.getIndicatorCode() != NbaOliConstants.OLI_COVIND_BASE) {
					if (coverageComparator.isNewCoverage()) {
						registerChange(DC_RIDER_ADDED, newCoverage, NbaOliConstants.OLI_LIFECOVERAGE);
						if (newCoverage.getLifeCovTypeCode() == NbaOliConstants.OLI_COVTYPE_CHILDTERM) {
							registerChange(DC_RIDER_CTIR_ADDED, newCoverage, NbaOliConstants.OLI_LIFECOVERAGE);
						}
					}
					if (coverageComparator.isCoverageDeleted()) {
						registerChange(DC_RIDER_DELETED, newCoverage, NbaOliConstants.OLI_LIFECOVERAGE);
						if (newCoverage.getLifeCovTypeCode() == NbaOliConstants.OLI_COVTYPE_CHILDTERM) {
							registerChange(DC_RIDER_CTIR_DELETED, newCoverage, NbaOliConstants.OLI_LIFECOVERAGE);
						}

					}
					if (coverageComparator.isCurrentAmountChanged()) {
						registerChange(DC_RIDER_AMT, newCoverage, NbaOliConstants.OLI_LIFECOVERAGE);
					}
				}
				if (newCoverage != null && oldCoverage != null && !coverageComparator.isNewCoverage()) {
					registerSubstandardRatingChanges(newCoverage, oldCoverage);
				}
				//ALS4153
				if (newCoverage != null && oldCoverage != null && !coverageComparator.isNewCoverage()) {
					registerCovOptionChanges(newCoverage, oldCoverage, businessProcess);//APSL4697
				} 
				//ALPC136
				if (coverageComparator.isModalPremAmtChanged()) {
					registerChange(DC_MODAL_AMT_CHG, newCoverage, NbaOliConstants.OLI_LIFECOVERAGE);
				}
			}
		}
	}
	/**
	 * Checks change in Sub-standard Rating information
	 * @param newCoverage
	 * @param oldCoverage
	 */
	private void registerSubstandardRatingChanges(Coverage newCoverage, Coverage oldCoverage) {
		int noOfNewLifeParticipants = newCoverage.getLifeParticipantCount();
		int noOfOldLifeParticipants = oldCoverage.getLifeParticipantCount();
		LifeParticipant newLifeParticipant;
		LifeParticipant oldLifeParticipant;
		SubstandardRating newSubStdRating;
		SubstandardRating oldSubStdRating;
		AxaDataChangeSubstandardRatingComparator ratingComparator;
		if (noOfNewLifeParticipants == noOfOldLifeParticipants) {
			for (int i = 0; i < noOfNewLifeParticipants; i++) {
				newLifeParticipant = newCoverage.getLifeParticipantAt(i);
				oldLifeParticipant = oldCoverage.getLifeParticipantAt(i);
				if (!newLifeParticipant.isActionDelete() && !oldLifeParticipant.isActionDelete()) {
					int noOfNewSubstandardRatings = newLifeParticipant.getSubstandardRatingCount();
					int noOfOldSubstandardRatings = oldLifeParticipant.getSubstandardRatingCount();
					for (int k = 0; k < noOfNewSubstandardRatings; k++) {
						newSubStdRating = newLifeParticipant.getSubstandardRatingAt(k);
						oldSubStdRating = null;
						for (int j = 0; j < noOfOldSubstandardRatings; j++) {
							if (newSubStdRating.getId().equalsIgnoreCase(oldLifeParticipant.getSubstandardRatingAt(j).getId())) {
								oldSubStdRating = oldLifeParticipant.getSubstandardRatingAt(j);
								break;
							}
						}

						ratingComparator = new AxaDataChangeSubstandardRatingComparator(newSubStdRating, oldSubStdRating);
						if (ratingComparator.isPermFlatExtraRatingAdded()) {
							registerChange(DC_PERMANENT_FLAT_EXTRA_RATING_ADDED, newSubStdRating);
						} else if (ratingComparator.isPermFlatExtraRatingDeleted()) {
							registerChange(DC_PERMANENT_FLAT_EXTRA_RATING_DELETED, newSubStdRating);
						} else if (ratingComparator.isTempFlatExtraRatingAdded()) {
							registerChange(DC_TEMP_FLAT_EXTRA_RATING_ADDED, newSubStdRating);
						} else if (ratingComparator.isTempFlatExtraRatingDeleted()) {
							registerChange(DC_TEMP_FLAT_EXTRA_RATING_DELETED, newSubStdRating);
						} else if (ratingComparator.isPermTableRatingAdded()) {
							registerChange(DC_PERMANENT_RATING_ADDED, newSubStdRating);
						} else if (ratingComparator.isPermTableRatingDeleted()) {
							registerChange(DC_PERMANENT_RATING_DELETED, newSubStdRating);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Checks change in CovOption information
	 * @param newCoverage
	 * @param oldCoverage
	 */
	//ALS4153 New Method APSL4697 signature changed.
	private void registerCovOptionChanges(Coverage newCoverage, Coverage oldCoverage, String businessProcess) {
		List newCovOptions =  newCoverage.getCovOption();
		List oldCovOptions =  oldCoverage.getCovOption();
		for (int i = 0; i < newCovOptions.size(); i++) {
			CovOption newCovOption = (CovOption) newCovOptions.get(i);
			CovOption oldCovOption = getMatchingCovOption(oldCovOptions, newCovOption);
			AxaDataChangeCovOptionComparator covOptionComparator = new AxaDataChangeCovOptionComparator(newCovOption, oldCovOption);
			if (covOptionComparator.isNewCovOption()) {
				registerChange(DC_BENEFIT_ADDED, newCovOption, NbaOliConstants.OLI_COVOPTION);//APSL3360
				//APSL5368  Starts
				CovOptionExtension covOptionExtn = NbaUtils.getFirstCovOptionExtension(newCovOption);
				if (covOptionExtn != null) {
					if(NbaOliConstants.OLI_RIDERSEL_INHERENTNOPREM != covOptionExtn.getSelectionRule() && NbaOliConstants.OLI_RIDERSEL_INHERENTADDLPREM != covOptionExtn.getSelectionRule()){
						registerChange(DC_OTHER_BENEFIT_ADDED, newCovOption, NbaOliConstants.OLI_COVOPTION);
					}
				}
				//APSL5368  End
				if (NbaOliConstants.OLI_COVTYPE_DREADDISEASE == newCovOption.getLifeCovOptTypeCode()) {
					registerChange(DC_RIDER_DPW_ADDED, newCovOption, NbaOliConstants.OLI_COVOPTION);
				}
				//APSL4697 -- Started
				if (NbaOliConstants.OLI_COVTYPE_LTCRIDER == newCovOption.getLifeCovOptTypeCode()) {
					registerChange(DC_RIDER_LTC_ADDED, newCoverage, NbaOliConstants.OLI_COVOPTION);
				}
				//APSL5128
				if (NbaOliConstants.OLI_OPTTYPE_ROPR == newCovOption.getLifeCovOptTypeCode()) {
					registerChange(DC_RIDER_ROPR_ADDED, newCoverage, NbaOliConstants.OLI_COVOPTION);
				}
			}
			if (covOptionComparator.isCovOptionDeleted()) {
					if (NbaOliConstants.OLI_COVTYPE_LTCRIDER == newCovOption.getLifeCovOptTypeCode()) {
					registerChange(DC_RIDER_LTC_DELETED, newCoverage, NbaOliConstants.OLI_COVOPTION);
				}
			}
			/*if (covOptionComparator.isCovOptionUpdated() && 
					(!NbaConstants.PROC_AUTO_UNDERWRITING.equalsIgnoreCase(businessProcess) || !NbaConstants.PROC_AUTO_UNDERWRITING_DYNA.equalsIgnoreCase(businessProcess))) {
					if (NbaOliConstants.OLI_COVTYPE_LTCRIDER == newCovOption.getLifeCovOptTypeCode()) {
					registerChange(DC_RIDER_LTC_UPDATED, newCoverage, NbaOliConstants.OLI_COVOPTION);
				}
			}*/
			// APSL4697  END
		}
	}
	/**
	 * Checks change in party information
	 * @param newTXLife
	 * @param parties
	 * @param dbParties
	 * @return status of Party Info check
	 * @throws NbaBaseException 
	 */
	private void registerPartyChanges(NbaTXLife newTXLife) throws NbaBaseException { //NBLXA-2299
		//compare Owners
		//AXAL3.7.22 code refactored.Created seperate methods for comparing Owner,Insured and Primary Writing Agent.
		compareOwners(newTXLife);
		compareInsureds(newTXLife);
		compareDependentParties(newTXLife);//NBLXA-1812
		compareBeneficiaries(newTXLife);//APSL3360
		compareJointInsureds(newTXLife);//P2AXAL053
 		comparePWAs(newTXLife);
		compareAWAs(newTXLife); //ALS4908
		//NBLXA-1254 Begins
		compareBeneficialOwners(newTXLife);
		compareAuthPersonOwners(newTXLife);
		compareTrusteeOwners(newTXLife);
		compareControllingPersonwners(newTXLife);
		//comparePayor(newTXLife);
		//NBLXA-1254 Ends
		registerWAEligibilityChanges(newTXLife);//ALS4908 call moved out from comparePWAs method
		if(!newTXLife.isInformalApp()){//ALII748/QC6240
			registerAddressZipChanges(newTXLife);//P2AXAL038
		}
	}
	//NBLXA-1254 Begins
	public void compareBeneficialOwners(NbaTXLife newTXLife) throws NbaBaseException { //NBLXA-2299
		NbaParty oldBenOwner = null;
		NbaParty newBenOwner = null;
		Relation newBenOwnerRelation = null;
		Iterator beneOwnerRelationsItr= newTXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_BENEFICIALOWNER).entrySet().iterator();		
		while(beneOwnerRelationsItr.hasNext()){
			newBenOwnerRelation =(Relation) ((Map.Entry) beneOwnerRelationsItr.next()).getValue();
			newBenOwner= newTXLife.getParty(newBenOwnerRelation.getRelatedObjectID());
			if(newBenOwner != null) {
				if (newBenOwner.getParty().isActionAdd()) {
					registerChange(DC_BENOWNER_ADD, newBenOwner.getParty(), NbaOliConstants.OLI_PARTY);
				}
				Relation oldBenOwnerRelation = getMatchingRelation(dbRelatonList, newBenOwnerRelation);
				if (oldBenOwnerRelation != null && !newBenOwner.getID().equalsIgnoreCase(oldBenOwnerRelation.getRelatedObjectID())) {
					registerChange(DC_BENOWNER_CHANGE, newBenOwner, NbaOliConstants.OLI_PARTY);
				} else {
					oldBenOwner = getMatchingParty(dbPartyList, newBenOwner);
					registerBeneficialOwnerChanges(newBenOwner, oldBenOwner, newTXLife); // NBLXA-2299
				}
			}
		}
	}
	
	public void compareTrusteeOwners(NbaTXLife newTXLife) throws NbaBaseException { //NBLXA-2299
		NbaParty oldTrusteeOwner = null;
		NbaParty newTrusteeOwner = null;
		Relation newTrusteeOwnerRelation = null;
		Iterator trusteeOwnerRelationsItr= newTXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_TRUSTEE).entrySet().iterator();		
		while(trusteeOwnerRelationsItr.hasNext()){
			newTrusteeOwnerRelation =(Relation) ((Map.Entry) trusteeOwnerRelationsItr.next()).getValue();
			newTrusteeOwner= newTXLife.getParty(newTrusteeOwnerRelation.getRelatedObjectID());
			if(newTrusteeOwner != null) {
				if (newTrusteeOwner.getParty().isActionAdd()) {
					registerChange(DC_TRUSTEEOWNER_ADD, newTrusteeOwner.getParty(), NbaOliConstants.OLI_PARTY);
				}
				Relation oldBenOwnerRelation = getMatchingRelation(dbRelatonList, newTrusteeOwnerRelation);
				if (oldBenOwnerRelation != null && !newTrusteeOwner.getID().equalsIgnoreCase(oldBenOwnerRelation.getRelatedObjectID())) {
					registerChange(DC_TRUSTEEOWNER_CHANGE, newTrusteeOwner, NbaOliConstants.OLI_PARTY);
				} else {
					oldTrusteeOwner = getMatchingParty(dbPartyList, newTrusteeOwner);
					registerTrusteeOwnerChanges(newTrusteeOwner, oldTrusteeOwner, newTXLife); // NBLXA-2299
				}
			}
		}
	}
	public void compareControllingPersonwners(NbaTXLife newTXLife) throws NbaBaseException { //NBLXA-2299
		NbaParty oldContPersonOwner = null;
		NbaParty newContPersonOwner = null;
		Relation newContPersonRelation = null;
		Iterator contPersonOwnerRelationsItr= newTXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_CONTROLLINGPERSON).entrySet().iterator();		
		while(contPersonOwnerRelationsItr.hasNext()){
			newContPersonRelation =(Relation) ((Map.Entry) contPersonOwnerRelationsItr.next()).getValue();
			newContPersonOwner= newTXLife.getParty(newContPersonRelation.getRelatedObjectID());
			if(newContPersonOwner != null) {
				if (newContPersonOwner.getParty().isActionAdd()) {
					registerChange(DC_CONTPERSONOWNER_ADD, newContPersonOwner.getParty(), NbaOliConstants.OLI_PARTY);
				}
				Relation oldBenOwnerRelation = getMatchingRelation(dbRelatonList, newContPersonRelation);
				if (oldBenOwnerRelation != null && !newContPersonOwner.getID().equalsIgnoreCase(oldBenOwnerRelation.getRelatedObjectID())) {
					registerChange(DC_CONTPERSONOWNER_CHANGE, newContPersonOwner, NbaOliConstants.OLI_PARTY);
				} else {
					oldContPersonOwner = getMatchingParty(dbPartyList, newContPersonOwner);
					registerControllingPersonOwnerChanges(newContPersonOwner, oldContPersonOwner, newTXLife); // NBLXA-2299
				}
			}
		}
	}
	public void compareAuthPersonOwners(NbaTXLife newTXLife) throws NbaBaseException { //NBLXA-2299
		NbaParty oldAuthPersonOwner = null;
		NbaParty newAuthPersonOwner = null;
		Relation newAuthPersonRelation = null;
		Iterator authPersonOwnerRelationsItr= newTXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_AUTHORIZEDPERSON).entrySet().iterator();		
		while(authPersonOwnerRelationsItr.hasNext()){
			newAuthPersonRelation =(Relation) ((Map.Entry) authPersonOwnerRelationsItr.next()).getValue();
			newAuthPersonOwner= newTXLife.getParty(newAuthPersonRelation.getRelatedObjectID());
			if(newAuthPersonOwner != null) {
				if (newAuthPersonOwner.getParty().isActionAdd()) {
					registerChange(DC_AUTHPERSONOWNER_ADD, newAuthPersonOwner.getParty(), NbaOliConstants.OLI_PARTY);
				}
				Relation oldBenOwnerRelation = getMatchingRelation(dbRelatonList, newAuthPersonRelation);
				if (oldBenOwnerRelation != null && !newAuthPersonOwner.getID().equalsIgnoreCase(oldBenOwnerRelation.getRelatedObjectID())) {
					registerChange(DC_AUTHPERSONOWNER_CHANGE, newAuthPersonOwner, NbaOliConstants.OLI_PARTY);
				} else {
					oldAuthPersonOwner = getMatchingParty(dbPartyList, newAuthPersonOwner);
					registerAuthorisedPersonChanges(newAuthPersonOwner, oldAuthPersonOwner, newTXLife); // NBLXA-2299
				}
			}
		}
	}

	public void comparePayor(NbaTXLife newTXLife) {
		NbaParty oldPayor = null;
		NbaParty newPayor = null;
		Relation newPayorRelation = null;
		Iterator payorRelationsItr = newTXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_PAYER).entrySet().iterator();
		while (payorRelationsItr.hasNext()) {
			newPayorRelation = (Relation) ((Map.Entry) payorRelationsItr.next()).getValue();
			newPayor = newTXLife.getParty(newPayorRelation.getRelatedObjectID());
			if (newPayor != null) {
				if (newPayor.getParty().isActionAdd()) {
					registerChange(DC_PAYOR_ADD, newPayor.getParty(), NbaOliConstants.OLI_PARTY);
				}
				Relation oldBenOwnerRelation = getMatchingRelation(dbRelatonList, newPayorRelation);
				if (oldBenOwnerRelation != null && !newPayor.getID().equalsIgnoreCase(oldBenOwnerRelation.getRelatedObjectID())) {
					registerChange(DC_PAYOR_CHANGE, newPayor, NbaOliConstants.OLI_PARTY);
				} else {
					oldPayor = getMatchingParty(dbPartyList, newPayor);
					registerPayorChanges(newPayor, oldPayor);
				}
			}
		}
	}
		
	//NBLXA-1254 End
	//AXAL3.7.22 new method
	
	//NBLXA-2152 Updated method to handle multiple Owners
	public void compareOwners(NbaTXLife newTXLife) throws NbaBaseException { //NBLXA-2299
		NbaParty oldOwner = null;
		NbaParty newOwner = null;
		Relation newOwnerRelation = null;
		Iterator ownerRelationsItr = newTXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_OWNER).entrySet().iterator();
		while (ownerRelationsItr.hasNext()) {
			newOwnerRelation = (Relation) ((Map.Entry) ownerRelationsItr.next()).getValue();
			newOwner = newTXLife.getParty(newOwnerRelation.getRelatedObjectID());
			if (newOwner != null) {
				if (newOwner.getParty().isActionAdd()) {
					registerChange(DC_OWNER_ADDED, newOwner.getParty(), NbaOliConstants.OLI_PARTY);
					registerChange(DC_NEWOWNER_ADDED, newOwner.getParty(), NbaOliConstants.OLI_PARTY);
				}
				Relation oldOwnerRelation = getMatchingRelation(dbRelatonList, newOwnerRelation);
				if (oldOwnerRelation != null && !newOwner.getID().equalsIgnoreCase(oldOwnerRelation.getRelatedObjectID())) {
					registerChange(DC_OWNER_CHANGE, newOwner, NbaOliConstants.OLI_PARTY);
				} else {
					oldOwner = getMatchingParty(dbPartyList, newOwner);
					registerOwnerChanges(newOwner, oldOwner, newTXLife); // NBLXA-2299
				}
			}
		}
	}
	
	//AXAL3.7.22 new method
	public void compareInsureds(NbaTXLife newTXLife) {
		NbaParty newParty = newTXLife.getPrimaryParty();
		NbaParty dbParty = null;
		if (newParty != null) {
			if (newParty.getParty().isActionAdd()) {
				registerChange(DC_INSURED_ADDED, newParty.getParty(), NbaOliConstants.OLI_PARTY);
			}
			dbParty = getMatchingParty(dbPartyList, newParty);
			registerInsuredChanges(newParty, dbParty);
		}

	}
	
	// NBLXA-1812 new method
	public void compareDependentParties(NbaTXLife newTXLife) {
		List<String> ids = NbaUtils.getPartyIds(newTXLife.getOLifE(), NbaOliConstants.OLI_REL_DEPENDENT);
		for (int i = 0; i < ids.size(); i++) {
			NbaParty newParty = newTXLife.getParty(ids.get(i));
			NbaParty dbParty = null;
			if (newParty != null) {
				dbParty = getMatchingParty(dbPartyList, newParty);
				registerDependentPartyChanges(newParty, dbParty);
			}
		}
	}
	
	//APSL3360 New Method
	public void compareBeneficiaries(NbaTXLife newTXLife) {
		NbaParty dbParty = null;
		NbaParty newParty = null;
		Relation  newBeneficiaryRelation =null;
		
		Iterator beneRelationsItr= newTXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_BENEFICIARY).entrySet().iterator();		
		while(beneRelationsItr.hasNext()){
			newBeneficiaryRelation =(Relation) ((Map.Entry) beneRelationsItr.next()).getValue();
			newParty= newTXLife.getParty(newBeneficiaryRelation.getRelatedObjectID());
			if(newParty != null) {
				dbParty = getMatchingParty(dbPartyList, newParty);
				registerBeneficiaryChanges(newParty, dbParty);	
			}
		}
		
		beneRelationsItr= newTXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_CONTGNTBENE).entrySet().iterator();		
		while(beneRelationsItr.hasNext()){
			newBeneficiaryRelation =(Relation) ((Map.Entry) beneRelationsItr.next()).getValue();
			newParty= newTXLife.getParty(newBeneficiaryRelation.getRelatedObjectID());
			if(newParty != null) {
				dbParty = getMatchingParty(dbPartyList, newParty);
				registerContingentBeneficiaryChanges(newParty, dbParty);	
			}
		}
	}
	
	//P2AXAL053 new method
	public void compareJointInsureds(NbaTXLife newTXLife) {
		NbaParty newParty = newTXLife.getJointParty();
		NbaParty dbParty = null;
		if (newParty != null) {
			if (newParty.getParty().isActionAdd()) {
				registerChange(DC_JNT_INSURED_ADDED, newParty.getParty(), NbaOliConstants.OLI_PARTY);
			}
			dbParty = getMatchingParty(dbPartyList, newParty);
			registerJointInsuredChanges(newParty, dbParty);
		}

	}
	
	//AXAL3.7.22 new method
	public void comparePWAs(NbaTXLife newTXLife) {
		NbaParty dbParty = null;
		Iterator relationsItr= newTXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_PRIMAGENT).entrySet().iterator();
		NbaParty newParty = null;
		Relation  newAgentRelation =null;
		Relation oldAgentRelation = null;
		while(relationsItr.hasNext()){
			newAgentRelation =(Relation) ((Map.Entry) relationsItr.next()).getValue();
			oldAgentRelation=getMatchingRelation(dbRelatonList,newAgentRelation);
			newParty= newTXLife.getParty(newAgentRelation.getRelatedObjectID());
			dbParty= oldAgentRelation != null ? getParty(dbPartyList, oldAgentRelation.getRelatedObjectID()) : null;
			//if new party has action indicator deleted and dbparty is null, this means that this change is previously register so skipping it from
			// registration
			if(NbaUtils.isDeletedOnly(newParty.getParty()) && dbParty == null){//ALS3680 changed the NbaUtils method called
				continue;
			}
			registerPWAChanges(newParty, dbParty);
		}
		//ALS4908 code deleted
	}
	
	//ALS4908 new method
	public void compareAWAs(NbaTXLife newTXLife) {
		NbaParty dbParty = null;
		Iterator relationsItr = newTXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_ADDWRITINGAGENT).entrySet().iterator();
		NbaParty newParty = null;
		Relation newAgentRelation = null;
		Relation oldAgentRelation = null;
		while (relationsItr.hasNext()) {
			newAgentRelation = (Relation) ((Map.Entry) relationsItr.next()).getValue();
			oldAgentRelation = getMatchingRelation(dbRelatonList, newAgentRelation);
			newParty = newTXLife.getParty(newAgentRelation.getRelatedObjectID());
			dbParty = oldAgentRelation != null ? getParty(dbPartyList, oldAgentRelation.getRelatedObjectID()) : null;
			//if new party has action indicator deleted and dbparty is null, this means that this change is previously register so skipping it from
			// registration
			if (NbaUtils.isDeletedOnly(newParty.getParty()) && dbParty == null) {//ALS3680 changed the NbaUtils method called
				continue;
			}
			registerAWAChanges(newParty, dbParty);
		}
	}	

		
	/**
	 * Checks change in Owner party information
	 * @param newTXLife
	 * @param prevTXLife
	 */
	//P2AXAL038 new method
	private void registerAddressZipChanges(NbaTXLife newTXLife) {
		Relation newRelation = null;
		NbaParty newParty = null;
		NbaParty dbParty = null;
		for (int index = 0; index < newTXLife.getOLifE().getRelationCount(); index++) {
			newRelation = newTXLife.getOLifE().getRelationAt(index);
			if(isIncludedZipRelation(newRelation.getRelationRoleCode())){
				newParty = newTXLife.getParty(newRelation.getRelatedObjectID());
				if (newParty != null) {
					if (newParty.getParty().isActionAdd()) {
						registerChange(DC_ADDRESS_ZIP, newParty, NbaOliConstants.OLI_PARTY);
						break;
					}else if(!NbaUtils.isDeleted(newParty.getParty())){
						dbParty = getMatchingParty(dbPartyList, newParty);
						AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newParty, dbParty);
						if (partyComparator.isAddressChanged()) {
							registerChange(DC_ADDRESS_ZIP, newParty.getParty(), NbaOliConstants.OLI_PARTY);
							break;
						}
					}
				}
			}
		}
	}
	
	//NBLXA-1254 Begins
	private void registerBeneficialOwnerChanges(NbaParty newBenOwner, NbaParty oldBenOwner, NbaTXLife newTXLife) throws NbaBaseException { // NBLXA-2299
		// compare Beneficial Owners
		if (newBenOwner != null && oldBenOwner != null) {
			AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newBenOwner, oldBenOwner); // APSL5128
			if (partyComparator.isPartyNameChanged()) {
				registerChange(DC_BENOWNER_NAME, newBenOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyDOBChanged()) {
				registerChange(DC_BENOWNER_DOB, newBenOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyAddressChanged()) {
				registerChange(DC_BENOWNER_ADDRESS, newBenOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartySSNChanged()) {
				registerChange(DC_BENOWNER_SSN, newBenOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			// NBLXA-2299 Begin
			if (NbaUtils.isEntityOwnedGIApplication(newTXLife)) {
				if (partyComparator.isPartyNameChanged() || partyComparator.isPartySSNChanged() || partyComparator.isDeletedParty()) {
					deleteOnboardingdata(oldBenOwner, newTXLife);
				} else if (partyComparator.isPartyDOBChanged() || partyComparator.isPartyAddressChanged()) {
					updateOnboardingdata(oldBenOwner, newTXLife);
				}
			}
			// NBLXA-2299 End
		}
	}

	private void registerTrusteeOwnerChanges(NbaParty newTrusteeOwner, NbaParty oldTrusteeOwner, NbaTXLife newTXLife) throws NbaBaseException { // NBLXA-2299
		// compare Trustee
		if (newTrusteeOwner != null && oldTrusteeOwner != null) {
			AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newTrusteeOwner, oldTrusteeOwner); // APSL5128
			if (partyComparator.isPartyNameChanged()) {
				registerChange(DC_TRUSTEEOWNER_NAME, newTrusteeOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyDOBChanged()) {
				registerChange(DC_TRUSTEEOWNER_DOB, newTrusteeOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartySSNChanged()) {
				registerChange(DC_TRUSTEEOWNER_SSN, newTrusteeOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyAddressChanged()) {
				registerChange(DC_TRUSTEEOWNER_ADDRESS, newTrusteeOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			// NBLXA-2299 Begin
			if (NbaUtils.isEntityOwnedGIApplication(newTXLife)) {
				if (partyComparator.isPartyNameChanged() || partyComparator.isPartySSNChanged() || partyComparator.isDeletedParty()) {
					deleteOnboardingdata(oldTrusteeOwner, newTXLife);
				} else if (partyComparator.isPartyDOBChanged() || partyComparator.isPartyAddressChanged()) {
					updateOnboardingdata(oldTrusteeOwner, newTXLife);
				}
			}
			// NBLXA-2299 End
		}
	}

	private void registerControllingPersonOwnerChanges(NbaParty newContPersonOwner, NbaParty oldContPersonOwner, NbaTXLife newTXLife)
			throws NbaBaseException { // NBLXA-2299
		// compare controlling Person
		if (newContPersonOwner != null && oldContPersonOwner != null) {
			AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newContPersonOwner, oldContPersonOwner); // APSL5128
			if (partyComparator.isPartyNameChanged()) {
				registerChange(DC_CONTPERSONOWNER_NAME, newContPersonOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyDOBChanged()) {
				registerChange(DC_CONTPERSONOWNER_DOB, newContPersonOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyAddressChanged()) {
				registerChange(DC_CONTPERSONOWNER_ADDRESS, newContPersonOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartySSNChanged()) {
				registerChange(DC_CONTPERSONOWNER_SSN, newContPersonOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			// NBLXA-2299 Begin
			if (NbaUtils.isEntityOwnedGIApplication(newTXLife)) {
				if (partyComparator.isPartyNameChanged() || partyComparator.isPartySSNChanged() || partyComparator.isDeletedParty()) {
					deleteOnboardingdata(oldContPersonOwner, newTXLife);
				} else if (partyComparator.isPartyDOBChanged() || partyComparator.isPartyAddressChanged()) {
					updateOnboardingdata(oldContPersonOwner, newTXLife);
				}
			}
			// NBLXA-2299 End
		}
	}

	private void registerAuthorisedPersonChanges(NbaParty newAuthPersonOwner, NbaParty oldAuthPersonOwner, NbaTXLife newTXLife)
			throws NbaBaseException { // NBLXA-2299
		// compare Authorised Persons
		if (newAuthPersonOwner != null && oldAuthPersonOwner != null) {
			AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newAuthPersonOwner, oldAuthPersonOwner); // APSL5128
			if (partyComparator.isPartyNameChanged()) {
				registerChange(DC_AUTHPERSONOWNER_NAME, newAuthPersonOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyDOBChanged()) {
				registerChange(DC_AUTHPERSONOWNER_DOB, newAuthPersonOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartySSNChanged()) {
				registerChange(DC_AUTHPERSONOWNER_SSN, newAuthPersonOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyAddressChanged()) {
				registerChange(DC_AUTHPERSONOWNER_ADDRESS, newAuthPersonOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			// NBLXA-2299 Begin
			if (NbaUtils.isEntityOwnedGIApplication(newTXLife)) {
				if (partyComparator.isPartyNameChanged() || partyComparator.isPartySSNChanged() || partyComparator.isDeletedParty()) {
					deleteOnboardingdata(oldAuthPersonOwner, newTXLife);
				} else if (partyComparator.isPartyDOBChanged() || partyComparator.isPartyAddressChanged()) {
					updateOnboardingdata(oldAuthPersonOwner, newTXLife);
				}
			}
			// NBLXA-2299 End
		}
	}
	
	private void registerPayorChanges(NbaParty newPayor, NbaParty oldPayor) {
		//compare Payor
		if (newPayor != null && oldPayor != null) {			
			AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newPayor, oldPayor); 
		
			if (partyComparator.isPartyDOBChanged()) {
				registerChange(DC_PAYOR_DOB, newPayor.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyNameChanged()) {
				registerChange(DC_PAYOR_NAME, newPayor.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartySSNChanged()) {
				registerChange(DC_PAYOR_SSN, newPayor.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyAddressChanged()) {
				registerChange(DC_PAYOR_ADDRESS, newPayor.getParty(), NbaOliConstants.OLI_PARTY);
			}		
				
		}		
	}
	
	//NBLXA-1254 Ends
	/**
	 * Checks change in Owner party information
	 * @param newTXLife
	 * @param prevTXLife
	 * @throws NbaBaseException 
	 */
	private void registerOwnerChanges(NbaParty newOwner, NbaParty oldOwner, NbaTXLife newTXLife) throws NbaBaseException { //NBLXA-2299
		//compare Owners
		if (newOwner != null && oldOwner != null) {
			AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newOwner, oldOwner);
			if (partyComparator.isPartyNameChanged()) {
				registerChange(DC_OWNER_NAME, newOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyDOBChanged()) {
				registerChange(DC_OWNER_DOB, newOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyGenderChanged()) {
				registerChange(DC_OWNER_GENDER, newOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartySSNChanged()) {
				registerChange(DC_OWNER_SSN, newOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyAddressChanged()) {
				registerChange(DC_OWNER_ADDRESS, newOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyTypeChanged()) {
				registerChange(DC_OWNER_PARTY_TYPE, newOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyChanged()) {
				registerChange(DC_OWNER_CHANGE, newOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isDeletedParty()) {
				registerChange(DC_OWNER_DELETED, newOwner.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if ((partyComparator.isPartyFirstNameChanged() && partyComparator.isPartyLastNameChanged() && partyComparator.isPartyDOBChanged()
					&& partyComparator.isPartySSNChanged()) 
					|| (partyComparator.isPartyDBAChanged() && partyComparator.isPartyAddressChanged() && partyComparator.isPartySSNChanged())) {
				registerChange(DC_OWNER_BAE_CHANGE, newOwner.getParty(), NbaOliConstants.OLI_PARTY);//NBLXA-2152
			}
			// NBLXA-2299 Begin
			if (NbaUtils.isEntityOwnedGIApplication(newTXLife)) { 
				if (partyComparator.isPartyNameChanged() || partyComparator.isPartySSNChanged() || partyComparator.isDeletedParty()) {
					deleteOnboardingdata(oldOwner, newTXLife);
				} else if (partyComparator.isPartyDOBChanged() || partyComparator.isPartyAddressChanged()) {
					updateOnboardingdata(oldOwner, newTXLife);
				}
			}
			// NBLXA-2299 End
		}
	}
	
	/** NBLXA-1812
	 * Checks change in Dependent party information
	 * @param newInsured
	 * @param oldInsured
	 */
	private void registerDependentPartyChanges(NbaParty newInsured, NbaParty oldInsured) {
		if (newInsured != null && oldInsured != null) {
			AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newInsured, oldInsured);
			if (partyComparator.isPartyDOBChanged()) {
				registerChange(DC_DEPENDENT_DOB, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyGenderChanged()) {
				registerChange(DC_DEPENDENT_GENDER, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}			
			if (partyComparator.isPartyFirstNameChanged()) {
				registerChange(DC_DEPENDENT_FIRSTNAME, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyLastNameChanged()) {
				registerChange(DC_DEPENDENT_LASTNAME, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}		
		}
	}
	/**
	 * Checks change in Insured party information
	 * @param newTXLife
	 * @param prevTXLife
	 */
	private void registerInsuredChanges(NbaParty newInsured, NbaParty oldInsured) {
		//compare Insureds
		if (newInsured != null && oldInsured != null) {
			AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newInsured, oldInsured);
			if (partyComparator.isPartyNameChanged()) {
				registerChange(DC_INSURED_NAME, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}

			if (partyComparator.isPartyDOBChanged()) {
				registerChange(DC_INSURED_DOB, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyGenderChanged()) {
				registerChange(DC_INSURED_GENDER, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartySSNChanged()) {
				registerChange(DC_INSURED_SSN, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyAddressChanged()) {
				registerChange(DC_INSURED_ADDRESS, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyTypeChanged()) {
				registerChange(DC_INSURED_PARTY_TYPE, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyChanged()) {
				registerChange(DC_INSURED_CHANGE, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			//Begin AXAL3.7.21
			if (partyComparator.isPartyFirstNameChanged()) {
				registerChange(DC_INSURED_FIRSTNAME, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyLastNameChanged()) {
				registerChange(DC_INSURED_LASTNAME, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyMiddleNameChanged()) {
				registerChange(DC_INSURED_MIDDLENAME, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartySuffixChanged()) {
				registerChange(DC_INSURED_SUFFIX, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyPrefixChanged()) {
				registerChange(DC_INSURED_PREFIX, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isNewParty()) {
				registerChange(DC_INSURED_ADDED, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isDeletedParty()) {
				registerChange(DC_INSURED_DELETED, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			
		}
		//End AXAL3.7.21
	}
	/**
	 * Checks change in Insured party information
	 * @param newTXLife
	 * @param prevTXLife
	 */
	//P2AXAL053 new method
	private void registerJointInsuredChanges(NbaParty newInsured, NbaParty oldInsured) {
		//compare Insureds
		if (newInsured != null && oldInsured != null) {
			AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newInsured, oldInsured);
			if (partyComparator.isPartyNameChanged()) {
				registerChange(DC_JNT_INSURED_NAME, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyDOBChanged()) {
				registerChange(DC_JNT_INSURED_DOB, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyGenderChanged()) {
				registerChange(DC_JNT_INSURED_GENDER, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartySSNChanged()) {
				registerChange(DC_JNT_INSURED_SSN, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyAddressChanged()) {
				registerChange(DC_JNT_INSURED_ADDRESS, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyTypeChanged()) {
				registerChange(DC_JNT_INSURED_PARTY_TYPE, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyChanged()) {
				registerChange(DC_JNT_INSURED_CHANGE, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyFirstNameChanged()) {
				registerChange(DC_JNT_INSURED_FIRSTNAME, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyLastNameChanged()) {
				registerChange(DC_JNT_INSURED_LASTNAME, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyMiddleNameChanged()) {
				registerChange(DC_JNT_INSURED_MIDDLENAME, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartySuffixChanged()) {
				registerChange(DC_JNT_INSURED_SUFFIX, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isPartyPrefixChanged()) {
				registerChange(DC_JNT_INSURED_PREFIX, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isNewParty()) {
				registerChange(DC_JNT_INSURED_ADDED, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
			if (partyComparator.isDeletedParty()) {
				registerChange(DC_JNT_INSURED_DELETED, newInsured.getParty(), NbaOliConstants.OLI_PARTY);
			}
		}
	}	
	
	/**
	 * Checks change in Primary Writing Agent party information
	 * @param newTXLife
	 * @param prevTXLife
	 */
	//ALS4908 new method
	private void registerWAEligibilityChanges(NbaTXLife newTXLife) {
		List producers = newTXLife.getWritingAgents();
		boolean eligibleBefore = false;
		boolean eligibleAfter = false;
		boolean evaluatedFlag1 = false;
		boolean evaluatedFlag2 = false;
		//SC: APSL5015- DOL 
		Party oldOwnParty = null;
		try {
			if (NbaUtils.isBICDOLApplicable() && NbaUtils.isBICDateApplicable(newTXLife)) {
				NbaParty newnbAParty = newTXLife.getPrimaryOwner();
				NbaParty oldnbaParty = getMatchingParty(dbPartyList, newnbAParty);
				oldOwnParty = NbaTXLife.getPartyFromId(oldnbaParty.getID(), dbPartyList);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//EC : APSL5015 - DOL 
		if (producers != null) {
			for (int i = 0; i < producers.size(); i++) {
				NbaProducerVO agentVo = ((NbaProducerVO) producers.get(i));
				NbaParty newParty = agentVo.getNbaParty();
				Relation newAgentRelation = NbaUtils.getRelationForParty(newParty.getParty().getId(), newTXLife.getOLifE().getRelation().toArray());
				Relation oldAgentRelation = getMatchingRelation(dbRelatonList, newAgentRelation.getRelationRoleCode());
				NbaParty dbParty = null;
				if (oldAgentRelation != null) {
					dbParty = getParty(dbPartyList, oldAgentRelation.getRelatedObjectID());
				}

				//Evaluate prior eligibility for all the producers and set evaluated flag to true when any one of them was ineligible
				if (dbParty != null && !evaluatedFlag1) {
					eligibleBefore = AxaUtils.getAgentsEligibilityForExpressCommissionBefore(oldOwnParty,dbParty, dbSystemMessagesList, dbHoldingList,
							dbPrimaryHolding, nbaDst); //APSL5015 - modified to add parameter oldOwnParty
					if (!eligibleBefore) {
						evaluatedFlag1 = true;
					}
				}

				//Evaluate current eligibility for all the producers and set evaluated flag to true when any one of them is ineligible
				if (newParty != null && !NbaUtils.isDeleted(newParty.getParty()) && !evaluatedFlag2) {
					eligibleAfter = AxaUtils.getAgentsEligibilityForExpressCommission(newTXLife, newParty, nbaDst);
					if (!eligibleAfter) {
						evaluatedFlag2 = true;
					}
				}
			}	
			// Begin APSL5168 
			long appType = (dbPrimaryHolding.getPolicy().getApplicationInfo() != null) ? (dbPrimaryHolding.getPolicy().getApplicationInfo()
					.getApplicationType()) : null;
			if (appType == NbaOliConstants.OLI_APPTYPE_CONVERSIONNEW || appType == NbaOliConstants.OLI_APPTYPE_CONVOPAIAD) {
				double oldcwa = dbPrimaryHolding.getPolicy().getApplicationInfo().getCWAAmt();
				Policy newpolicy = newTXLife.getPrimaryHolding().getPolicy();
				if (newpolicy != null) {
					ApplicationInfo appinfo = newpolicy.getApplicationInfo();
					if (appinfo != null) {
						double newCwa = appinfo.getCWAAmt();
						if (eligibleBefore && oldcwa == 0 && newCwa > 0) {
							eligibleBefore = false;
						}
					}
				}
			}
			// End APSL5168 
			if (!eligibleBefore && eligibleAfter) {
				registerChange(DC_AGENT_INELIGIBLE_TO_ELIGIBLE, null);
			} else if (eligibleBefore && eligibleAfter) {
				registerChange(DC_AGENT_ELIGIBLE_TO_ELIGIBLE, null);
			} else if (eligibleBefore && !eligibleAfter) {
				registerChange(DC_AGENT_ELIGIBLE_TO_INELIGIBLE, null);
			}
		}
	}

	/**
	 * Checks change in additional Writing Agent party information
	 * @param newTXLife
	 * @param prevTXLife
	 */
	 //ALS4908 new method
	private void registerAWAChanges(NbaParty newProducer, NbaParty oldProducer) {
		AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newProducer, oldProducer);
		if (partyComparator.isNewParty()) {
			registerChange(DC_ADDI_AGENT_ADD, newProducer.getParty(), NbaOliConstants.OLI_PARTY);
		}
		if (partyComparator.isDeletedParty()) {
			registerChange(DC_ADDI_AGENT_DELETE, newProducer.getParty(), NbaOliConstants.OLI_PARTY);
		}
	}
	
	/**
	 * Checks change in Primary Writing Agent party information
	 * @param newTXLife
	 * @param prevTXLife
	 */
	private void registerPWAChanges(NbaParty newProducer, NbaParty oldProducer) {
		AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newProducer, oldProducer);
		if (partyComparator.isPartyChanged()) {
			registerChange(DC_PRIMARY_AGENT_CHANGE, newProducer.getParty(), NbaOliConstants.OLI_PARTY);
		}
		if (partyComparator.isNewParty()) {
			registerChange(DC_PRIMARY_AGENT_ADD, newProducer.getParty(), NbaOliConstants.OLI_PARTY);
		}
		if (partyComparator.isDeletedParty()) {
			registerChange(DC_PRIMARY_AGENT_DELETE, newProducer.getParty(), NbaOliConstants.OLI_PARTY);
		}
	}
	
	/**
	 * Checks Agent count Update Changes
	 * @param newRelations
	 * @param oldRelations
	 */
	protected void registerRelationChanges(List newRelations) {
		//register App Info Changes here Changes
		Iterator relationItr = newRelations.iterator();
		while (relationItr.hasNext()) {
			Relation newRelation = (Relation) relationItr.next();
			Relation oldRelation = getMatchingRelation(dbRelatonList, newRelation);
			//Only track Holding -> Party relations
			if (newRelation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING
					&& newRelation.getRelatedObjectType() == NbaOliConstants.OLI_PARTY) {
				AxaDataChangeRelationComparator relationComparator = new AxaDataChangeRelationComparator(newRelation, oldRelation);
				if (relationComparator.isRelationChanged()) {
					registerChange(DC_RELATION_CHANGED, newRelation, NbaOliConstants.OLI_RELATION);
				}
				if (relationComparator.isNewRelation()) {
					registerChange(DC_RELATION_ADDED, newRelation, NbaOliConstants.OLI_RELATION);
				}
				if (relationComparator.isRelationDeleted()) {
					registerChange(DC_RELATION_DELETED, newRelation, NbaOliConstants.OLI_RELATION);
				}
			}
		}
		int noOfNewAgents = NbaUtils.getAgentCount(newRelations);
		int noOfOldAgents = NbaUtils.getAgentCount(dbRelatonList);
		if (noOfNewAgents != noOfOldAgents) {
			registerChange(DC_AGENT_COUNT, null);
		}
	}
	
	//P2AXAL007 New Method
	protected void registerSystemMessageChanges(NbaTXLife newTXLife,NbaDst nbaDst) {
		if (!NbaUtils.isMsgRestrictCodeExists(newTXLife, NbaOliConstants.NBA_MSGRESTRICTCODE_RESTCALLAXAWSPAL)) {
			registerChange(DC_SYSMSG_REST_RESOLVED, null);
		}	
		if (nbaDst!=null && nbaDst.isCase() && !NbaUtils.isBlankOrNull(nbaDst.getQueue()) && nbaDst.getQueue().equals(NbaConstants.A_QUEUE_UNDERWRITER_HOLD)) { // NBLXA-2398 ,NBLXA-2582            
			if (!NbaUtils.isMessageRestrictCodeExists(newTXLife, NbaOliConstants.NBA_MSGRESTRICTCODE_RESTAPPROVAL)
					&& !NbaUtils.isUWRequirementOutStanding(newTXLife.getPolicy(), NbaOliConstants.OLI_REQRESTRICT_APPROVAL)) { 
				registerChange(DC_UWAPPRVL_SEVERE_CV_RESOLVED, null);
			}
		}
	}
	
	/**
	 * Checks Impairment related changes 
	 * @param newPolicy
	 * @param oldPolicy
	 */
	//ALS2611 new method, P2AXAL053 Refac
	protected void registerImpairment(NbaTXLife newTXLife) {
		List insureds = newTXLife.getInsurableParties();
		for (int h = 0; h < insureds.size(); h++) {
			Party iParty = (Party) insureds.get(h);
			NbaParty newParty = new NbaParty(iParty);
			NbaParty dbParty = getMatchingParty(dbPartyList, newParty);
			if (dbParty != null) {
				Person newPerson = newParty.getPerson();
				Person oldPerson = dbParty.getPerson();
					if (newPerson != null && oldPerson != null) {
					PersonExtension newPersonExt = NbaUtils.getFirstPersonExtension(newPerson);
					PersonExtension oldPersonExt = NbaUtils.getFirstPersonExtension(oldPerson);
						if (newPersonExt != null && oldPersonExt != null) {
							if (newPersonExt.getImpairmentInfo().size() == oldPersonExt.getImpairmentInfo().size()) {
								for (int i = 0; i < newPersonExt.getImpairmentInfo().size(); i++) {
									ImpairmentInfo newImp = newPersonExt.getImpairmentInfoAt(i);
									ImpairmentInfo dbImp = oldPersonExt.getImpairmentInfoAt(i);
									registerImpairmentChanges(newImp, dbImp);
								}
							}

						}
					}
				}
			}
		}
	
	/**
	 * Checks change in ImpairmentInfo 
	 * @param newPolicy
	 * @param oldPolicy
	 */
	//ALS2611 new method
	protected void registerImpairmentChanges(ImpairmentInfo newImp, ImpairmentInfo dbImp) {
		if (newImp != null && dbImp != null) {
			if (newImp.getId().equalsIgnoreCase(dbImp.getId()) && (newImp.getActionIndicator().isUpdate())) {
				AxaDataChangeImpairmentComparator appComparator = new AxaDataChangeImpairmentComparator(newImp, dbImp);
				if (appComparator.isPermFlatAmtChanged()) {
					registerChange(DC_IMPAIRENT_PERM_AMT_UPDATED, newImp);
				}
				if (appComparator.isTempFlatAmtChanged()) {
					registerChange(DC_IMPAIRENT_TEMP_AMT_UPDATED, newImp);
				}
				if (appComparator.isCreditChanged()) {
					registerChange(DC_IMPAIRENT_CREDIT_UPDATED, newImp);
				}
				if (appComparator.isDebitChanged()) {
					registerChange(DC_IMPAIRENT_DEBIT_UPDATED, newImp);
				}
				if (appComparator.isDurationChanged()) {
					registerChange(DC_IMPAIRENT_DURATION_UPDATED, newImp);
				}
			}
		}
	}	
	/**
	 * Registers Changes
	 * @param changeType
	 * @param changedObject
	 * @param changedObjectType
	 */
	public void registerChange(long changeType, Object changedObject, long changedObjectType) {
		String changedObjectId = null;
		if (changedObject != null && changedObject instanceof NbaContractVO) {
			changedObjectId = ((NbaContractVO) changedObject).getId();
		}
		registerChange(changeType, changedObjectId, changedObjectType);
	}

/**
	 * Registers Changes
 * @param changeType
 * @param changedObject
 */
	public void registerChange(long changeType, Object changedObject) {
		registerChange(changeType,changedObject,NbaConstants.LONG_NULL_VALUE);
	}
	/**
	 * Registers Changes
	 * @param changeType
	 * @param changedObjectId
	 * @param changedObjectType
	 */
	public void registerChange(long changeType, String changedObjectId, long changedObjectType) {
	    System.out.println("Called Register change for  Change Type :- "+changeType);
		AxaDataChangeEntry dataChangeEntry = new AxaDataChangeEntry(changeType, changedObjectId, changedObjectType);
		propogateChange(dataChangeEntry);
		changeRegister.put(new Long(changeType), dataChangeEntry);
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("DATA CHANGE EVENTS:" + dataChangeEntry.toString());//P2AXAL038			
		}
	}
	/**
	 * 
	 * @param dataChangeEntry
	 */
	public void propogateChange(AxaDataChangeEntry dataChangeEntry) {
		AxaDataChangeTransaction transaction;
		int noOfTransactions = interfaceListners.size();
		for (int i = 0; i < noOfTransactions; i++) {
			transaction = (AxaDataChangeTransaction) interfaceListners.get(i);
			if (transaction.isTransactionAlive()) {
				transaction.sendEvent(dataChangeEntry);
			}
		}
	}
	/**
	 * Call Various Interfaces based upon change type supported 
	 * @param txLife
	 * @param user
	 * @param nbaDst
	 * @throws NbaBaseException
	 */
	public void callInterfaces(NbaTXLife txLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		AxaDataChangeTransaction transaction;
		int noOfTransactions = interfaceListners.size();
		getLogger().logDebug("DATA CHANGE EVENTS:" + changeRegister.toString());
		for (int i = 0; i < noOfTransactions; i++) {
			transaction = (AxaDataChangeTransaction) interfaceListners.get(i);
			try {
				if (transaction.isTransactionAlive()) {
					nbaDst=transaction.callInterface(txLife, user, nbaDst);
				}
			}catch (NbaTransactionValidationException e) {  //ALS4153
				throw e; //ALS4153
	        }catch (NbaBaseException e) {
				e.addMessage("Problems in calling interace " + transaction.getClass().getName());
				throw e;
			}
		}
	}


	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	
	/**
	 * @return Returns the changeRegister.
	 */
	public Map getChangeRegister() {
		return changeRegister;
	}
	/**
	 * @param changeRegister The changeRegister to set.
	 */
	public void setChangeRegister(Map changeRegister) {
		this.changeRegister = changeRegister;
	}

	//P2AXAL038 New Method
	protected boolean isIncludedZipRelation(long relDesc) {
		int length = NbaConstants.INCLUDED_ZIP_REL.length;
		for (int i = 0; i < length; i++) {
			if (relDesc == NbaConstants.INCLUDED_ZIP_REL[i]) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks change in Beneficiary party information
	 * @param newTXLife
	 * @param prevTXLife
	 */
	//New Method APSL3360
	private void registerBeneficiaryChanges(NbaParty newBeneficiary, NbaParty oldBeneficiary) {
		// compare Beneficiaries
		AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newBeneficiary, oldBeneficiary); //APSL5128
		if (newBeneficiary != null && oldBeneficiary != null) {
			//APSL5128 code deleted
			if (partyComparator.isPartyChanged()) {
				registerChange(DC_BENEFICIARY_CHANGE, newBeneficiary.getParty(), NbaOliConstants.OLI_PARTY);
			}
		}
		if (partyComparator.isNewParty()) { //APSL5128
			registerChange(DC_BENEFICIARY_ADD, newBeneficiary.getParty(), NbaOliConstants.OLI_PARTY); //APSL5128
		} //APSL5128
	}
	
	/**
	 * Checks change in Contigent Beneficiary party information
	 * @param newTXLife
	 * @param prevTXLife
	 */
	//New Method APSL3360
	private void registerContingentBeneficiaryChanges(NbaParty newBeneficiary, NbaParty oldBeneficiary) {
		// compare Beneficiaries
		AxaDataChangePartyComparator partyComparator = new AxaDataChangePartyComparator(newBeneficiary, oldBeneficiary);
		if (newBeneficiary != null && oldBeneficiary != null) {
			if (partyComparator.isPartyChanged()) {
				registerChange(DC_CONTBENEFICIARY_CHANGE, newBeneficiary.getParty(), NbaOliConstants.OLI_PARTY);
			}
		}
		//APSL5128 Begin
		if (partyComparator.isNewParty()) {
			registerChange(DC_CONTBENEFICIARY_ADD, newBeneficiary.getParty(), NbaOliConstants.OLI_PARTY);
		}
		//APSL5128 End
	}
	
	// APSl4871 New method
	protected void registerCWAChanges(Policy newPolicy, Policy oldPolicy,NbaTXLife newTXLife) {
		// register Policy Changes
		AxaDataChangeAppComparator appComparator = new AxaDataChangeAppComparator(newPolicy, oldPolicy);

		if (appComparator.isCWAAmtChanged()) {
			callCreateMiscWork(newPolicy);
		}
		//APSL 4967 begin
		if(appComparator.isCWAAmtChanged()){
			boolean isSeverePDRCVexists=false;
			isSeverePDRCVexists=NbaUtils.validatePDRCVOnCase(newTXLife);
			if(isSeverePDRCVexists)
			{
				registerChange(DC_CV_1797_EXISTS, null);
			}
		}
		//APSL4967 End
	}

	// APSl4871 New method
	private void callCreateMiscWork(Policy newPolicy) {
		int finActivityCount = newPolicy.getFinancialActivity().size();
		List<String> finEffDateForLoanCarryOver = new ArrayList();
		List<String> finEffDateForOtherPayments = new ArrayList();
		if (finActivityCount != 0) {
			for (int i = 0; i < finActivityCount; i++) {
				FinancialActivity finActivity = (FinancialActivity) newPolicy.getFinancialActivity().get(i);
				FinancialActivityExtension finActivityExtension = NbaUtils.getFirstFinancialActivityExtension(finActivity);
				boolean isDisbursed = false;
				if (finActivityExtension != null) {
					isDisbursed = finActivityExtension.getDisbursedInd();
				}
				if (finActivity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_REV
						&& finActivity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_REFUND
						&& finActivity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND && isDisbursed != true) {
					if (finActivity.getFinActivityType() == NbaOliConstants.OLI_FINANCIALACTIVITYTYPE_LOANCARRYOVER) {
						finEffDateForLoanCarryOver.add(NbaUtils.getStringFromDate(finActivity.getFinEffDate()));
					} else
						finEffDateForOtherPayments.add(NbaUtils.getStringFromDate((finActivity.getFinEffDate())));
				}
			}

			String finalFinEffDate = null;
			if (finEffDateForLoanCarryOver.size() != 0 && finEffDateForOtherPayments.size() != 0) {
				Collections.sort(finEffDateForLoanCarryOver);
				finalFinEffDate = finEffDateForLoanCarryOver.get(finEffDateForLoanCarryOver.size() - 1);
				if (finalFinEffDate != null) {
					int countOfPayments = finEffDateForOtherPayments.size();
					if (countOfPayments != 0) {
						for (int i = 0; i < countOfPayments; i++) {
							try {
								if (compareDates(finEffDateForOtherPayments.get(i), finalFinEffDate) == false) {
									registerChange(DC_CWA_AMT, newPolicy);
									break;

								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}

	}

	public static boolean compareDates(String creationDate, String compareDate) throws ParseException {
		if (creationDate != null && compareDate != null) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date fmtCreationDate = null;
			Date fmtcompareDate = null;
			fmtCreationDate = formatter.parse(creationDate);
			fmtcompareDate = formatter.parse(compareDate);
			if (!(fmtCreationDate.after(fmtcompareDate))) {
				return true;
			}
		}
		return false;
	}
	
	// NBLXA-188(APSL5318) Legacy Decommissioning New Method
	/**
	 * @purpose This method will check and register the event if PrintTogetherIND value changed
	 * @param newPolicy
	 * @param oldPolicy
	 * @param newTXLife
	 */
		protected void registerPrintTogetherChangesForGI(Policy newPolicy, Policy oldPolicy,NbaTXLife newTXLife) {
			// register print together changes
			AxaDataChangeAppComparator appComparator = new AxaDataChangeAppComparator(newPolicy, oldPolicy);
			if(appComparator.isPrintTogetherIndChangedForGI()){
				registerChange(DC_PRINT_TOGETHER_UPDATED, null);
			}
		}
		
		// NBLXA-1823 --  Check If Business Strategies value changed
		/**
		 * @purpose This method will check and register the event if BusinessStrategiesInd value changed
		 * @param newPolicy
		 * @param oldPolicy
		 **/
		protected void registerBusinessStrategiesIndChange(Policy newPolicy, Policy oldPolicy) {
			// register business strategies changes
			AxaDataChangeAppComparator appComparator = new AxaDataChangeAppComparator(newPolicy, oldPolicy);
			if(appComparator.isBusinessStrategiesIndChanged()){
				registerChange(DC_BUSINESS_STRATEGIES, null);
			}
		}
		
		// NBLXA-1337 --  Check If Licensing WI register change required or no 
		private void registerSystemMessageChangesForAgentCV(NbaTXLife newTXLife) {
			List messages = null;
			if(dbSystemMessagesList ==null){
				messages = newTXLife.getPrimaryHolding().getSystemMessage();
				if(getSevereAgentCV(messages)){
					registerChange(DC_SEVERE_LICENSING_CV_EXIST, null);
				}
			}else if(dbSystemMessagesList !=null && !dbSystemMessagesList.isEmpty()){
				if(!getSevereAgentCV(dbSystemMessagesList)){
					messages = newTXLife.getPrimaryHolding().getSystemMessage();
					if(getSevereAgentCV(messages)){
						registerChange(DC_SEVERE_LICENSING_CV_EXIST, null);
					}
					
				}
			}
			
					
}
		private boolean getSevereAgentCV(List SystemMessagesList) {
			SystemMessage sysMessage;
			SystemMessageExtension systemMessageExtension;
			if(SystemMessagesList !=null && !SystemMessagesList.isEmpty()){
			for (int i = 0; i < SystemMessagesList.size(); i++) {
				sysMessage = (SystemMessage) SystemMessagesList.get(i);
				if ((sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_SEVERE
						|| sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE) //NBLXA-2280
						&& (sysMessage.getMessageCode() != NbaConstants.MESSAGECODE_AGENTLIC_WI)){
					systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage); 
					if (systemMessageExtension != null && systemMessageExtension.getMsgValidationType() == NbaConstants.SUBSET_AGENT
							&& !systemMessageExtension.getMsgOverrideInd()) {
						return true;
					}
				}
			}
			}
			return false;
		}
		// NBLXA-1337 --  END
		
		//NBLXA-1896 New Method
		private void registerSystemMessageChangesForMoney(NbaTXLife newTXLife){
			 List newCVs = newTXLife.getPrimaryHolding().getSystemMessage();
			if(dbSystemMessagesList != null){
				if(AxaUtils.isShortageCVpresent(newCVs) && !(AxaUtils.isShortageCVpresent(dbSystemMessagesList))){
					registerChange(DC_CWA_Shortage_CV_EXIST, null);
				}else if (!(AxaUtils.isShortageCVpresent(newCVs)) && AxaUtils.isShortageCVpresent(dbSystemMessagesList)){
					registerChange(DC_CWA_RECEIVED, null);
				}
			}
		}
	
		
		
		// NBLXA-1539 --  Check If RequestedPolDateReason change then add or update Activity 
		private void registerAppInfoExtChangesForRequestedPolDateReason(Policy newPolicy, Policy oldPolicy, NbaUserVO user, NbaTXLife newTXLife) {
			AxaDataChangeAppComparator appComparator = new AxaDataChangeAppComparator(newPolicy, oldPolicy);
			if (appComparator.isRequestedPolDateReasonChanged()) {
				registerChange(DC_REQUESTED_POL_DATE_REASON_ADD, null);//DC_SEVERE_LICENSING_CV_EXIST??
			}			
			
			
		}
		
		// Begin NBLXA-1954	
	protected void registerSignificantValidationErrorResolved(Holding newHolding, Holding oldHolding) {
		if (hasSignificantValidationErrors(oldHolding) && !hasSignificantValidationErrors(newHolding)) {
			registerChange(DC_SEVERE_CV_RESOLVED, null);
		}
	}
			
	public boolean hasSignificantValidationErrors(Holding holding) {
		int count = holding.getSystemMessageCount();
		SystemMessage systemMessage;
		SystemMessageExtension msgExt;
		for (int i = 0; i < count; i++) {
			systemMessage = holding.getSystemMessageAt(i);
			if (!systemMessage.isActionDelete()) {
				if (NbaOliConstants.OLI_MSGSEVERITY_SEVERE == systemMessage.getMessageSeverityCode()) {
					return true;
				}
				if (NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE == systemMessage.getMessageSeverityCode()) {
					msgExt = NbaUtils.getFirstSystemMessageExtension(systemMessage);
					if (msgExt == null || !msgExt.getMsgOverrideInd()) {
						return true;
					}
				}
			}
		}
		return false;
	}
			
	    //End NBLXA-1954		
		
	protected void registerNCFValidationError(NbaTXLife newTXLife){ 
		List newCVs = newTXLife.getPrimaryHolding().getSystemMessage();
		if(dbSystemMessagesList != null){
			if(AxaUtils.isNCFCVpresent(newCVs) && !(AxaUtils.isNCFCVpresent(dbSystemMessagesList))){
				registerChange(DC_NCF_Miscmatch_CV_EXIST, null);
			}
		}
	}
	// NBLXA-2299 New method	
	protected void registerLNBridgerAlertValidationErrorResolved(Holding newHolding, Holding oldHolding, NbaTXLife newTXLife) {
		if (NbaUtils.isEntityOwnedGIApplication(newTXLife)) {
			if ((isValidationErrorOverridable(oldHolding, 6767) && isValidationErrorOverridden(newHolding, 6767))
					|| (isValidationErrorOverridable(oldHolding, 6767) && isValidationErrorOverridden(newHolding, 6767))) {
				registerChange(DC_ONBOARDING_BRIDGER_ALERT_CV_RESOLVED, null);
			}
			if ((isValidationErrorOverridable(oldHolding, 6768) && isValidationErrorOverridden(newHolding, 6768))
					|| (isValidationErrorOverridden(oldHolding, 6768) && isValidationErrorOverridable(newHolding, 6768))) {
				registerChange(DC_ONBOARDING_BRIDGER_ALERT_CV_RESOLVED, null);
			}
			if ((isValidationErrorOverridable(oldHolding, 6772) && isValidationErrorOverridden(newHolding, 6772))
					|| (isValidationErrorOverridden(oldHolding, 6772) && isValidationErrorOverridable(newHolding, 6772))) {
				registerChange(DC_ONBOARDING_BRIDGER_ALERT_CV_RESOLVED, null);
			}
		}
	}
	
	// NBLXA-2299 New method	
	protected void registerBAEAlertValidationErrorResolved(Holding newHolding, Holding oldHolding, NbaTXLife newTXLife) {
		if (NbaUtils.isEntityOwnedGIApplication(newTXLife)) {
			if ((isValidationErrorOverridable(oldHolding, 6770) && isValidationErrorOverridden(newHolding, 6770))
					|| (isValidationErrorOverridden(oldHolding, 6770) && isValidationErrorOverridable(newHolding, 6770))) {
				registerChange(DC_ONBOARDING_BAE_ALERT_CV_RESOLVED, null);
			}
			if ((isValidationErrorOverridable(oldHolding, 6773) && isValidationErrorOverridden(newHolding, 6773))
					|| (isValidationErrorOverridden(oldHolding, 6773) && isValidationErrorOverridable(newHolding, 6773))) {
				registerChange(DC_ONBOARDING_BAE_ALERT_CV_RESOLVED, null);
			}
		}
	}
	
	// NBLXA-2299 New method
	public boolean isValidationErrorOverridable(Holding holding, int messageCode) {
		int count = holding.getSystemMessageCount();
		SystemMessage systemMessage;
		SystemMessageExtension msgExt;
		for (int i = 0; i < count; i++) {
			systemMessage = holding.getSystemMessageAt(i);
			if (systemMessage.getMessageCode() == messageCode) {
				msgExt = NbaUtils.getFirstSystemMessageExtension(systemMessage);
				if (!systemMessage.isActionDelete() && msgExt != null && !msgExt.getMsgOverrideInd()) {
					return true;
				}
			}
		}
		return false;
	}
	
	// NBLXA-2299 New method
	public boolean isValidationErrorOverridden(Holding holding, int messageCode) {
		int count = holding.getSystemMessageCount();
		SystemMessage systemMessage;
		SystemMessageExtension msgExt;
		for (int i = 0; i < count; i++) {
			systemMessage = holding.getSystemMessageAt(i);
			if (systemMessage.getMessageCode() == messageCode) {
				msgExt = NbaUtils.getFirstSystemMessageExtension(systemMessage);
				if (!systemMessage.isActionDelete() && msgExt != null && msgExt.getMsgOverrideInd()) {
					return true;
				}
			}
		}
		return false;
	}

	// NBLXA-2299 New Method
	private void updateOnboardingdata(NbaParty oldBenOwner, NbaTXLife newTXLife) throws NbaBaseException {
		onboardingDataAccessor = new AxaGIAppOnboardingDataAccessor();
		onboardingDataAccessor.startTransaction();
		Party benParty = oldBenOwner.getParty();
		PolicyExtension polExtn = NbaUtils.getFirstPolicyExtension(newTXLife.getPolicy());
		AxaGIAppOnboardingDataVO onboardingDataVO = onboardingDataAccessor.selectPartyForOnboardingProcessing(polExtn.getGuarIssOfferNumber(),
				benParty.getFullName(), benParty.getGovtID());
		if (!NbaUtils.isBlankOrNull(onboardingDataVO)) {
			onboardingDataVO.setBridgerCallRequired(true);
			onboardingDataAccessor.updateBridgerDetails(onboardingDataVO);
		}
		if (onboardingDataAccessor != null) {
			onboardingDataAccessor.commitTransaction();
		}
	}

	// NBLXA-2299 New Method
	private void deleteOnboardingdata(NbaParty oldBenOwner, NbaTXLife newTXLife) throws NbaBaseException {
		onboardingDataAccessor = new AxaGIAppOnboardingDataAccessor();
		onboardingDataAccessor.startTransaction();
		Party benParty = oldBenOwner.getParty();
		PolicyExtension polExtn = NbaUtils.getFirstPolicyExtension(newTXLife.getPolicy());
		AxaGIAppOnboardingDataVO onboardingDataVO = onboardingDataAccessor.selectPartyForOnboardingProcessing(polExtn.getGuarIssOfferNumber(),
				benParty.getFullName(), benParty.getGovtID());
		if (!NbaUtils.isBlankOrNull(onboardingDataVO)) {
			onboardingDataAccessor.deleteOnboardingDetails(onboardingDataVO);
		}
		if (onboardingDataAccessor != null) {
			onboardingDataAccessor.commitTransaction();
		}
	}
	
	
	// New Method NBLXA-2620	
		protected void registerPrintValidationErrorResolved(Holding newHolding, Holding oldHolding) {
			if (hasPrintValidationErrors(oldHolding) && !hasPrintValidationErrors(newHolding)) {
				System.out.println("Called Register change for PrintCV, If Print CV Resolved");
				registerChange(DC_PRINT_CV_RESOLVED, null);
			}
		}
	
		// NBLXA-2519 --  Check If Contract Validation WI register change required 
		private void registerSystemMessageChangesForMultipleDraftCV(NbaTXLife newTXLife) {
			List newCVs = newTXLife.getPrimaryHolding().getSystemMessage();
			if(dbSystemMessagesList != null){
				if(AxaUtils.isMultipleDraftCVpresent(newCVs) && !(AxaUtils.isMultipleDraftCVpresent(dbSystemMessagesList))){
					registerChange(DC_MULTIPLE_DRAFT_CV_EXIST, null);
				}
			}		
		}
		
	// New Method NBLXA-2620		
		public boolean hasPrintValidationErrors(Holding holding) {
			int count = holding.getSystemMessageCount();
			SystemMessage systemMessage;
			SystemMessageExtension msgExt;
			for (int i = 0; i < count; i++) {
				systemMessage = holding.getSystemMessageAt(i);
				if (!systemMessage.isActionDelete()) {
					msgExt = NbaUtils.getFirstSystemMessageExtension(systemMessage);
					if (msgExt != null && msgExt.getMsgRestrictCode() == NbaOliConstants.NBA_MSGRESTRICTCODE_RESTCONTRACTPRINT
							&& !msgExt.getMsgOverrideInd()) {
						return true;
					}
				}
			}
			return false;
		}

}
