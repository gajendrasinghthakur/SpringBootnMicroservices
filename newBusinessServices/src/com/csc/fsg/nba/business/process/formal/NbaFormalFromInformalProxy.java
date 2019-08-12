package com.csc.fsg.nba.business.process.formal;
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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaAutomatedProcessResult;
import com.csc.fsg.nba.business.transaction.NbaMIBReportUtils;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * This class executes APFORMAL for a case whose Application Origin (APTP) is Formal Originating From Informal.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>ALS3091</td><td>AXA Life Phase 1</td><td>General code clean up of NbaProcFormal</td></tr>
 * <tr><td>QC1300</td><td>AXA Life Phase 1</td><td>Work itme created for NBCM, but do detail on what needs to be done</td></tr>
 * <tr><td>SR564247 Retrofit</td><td>Discretionary</td><td>Predictive Analytics Switch</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaFormalFromInformalProxy extends NbaProcFormalBaseProxy {
	
	//ALS4854 ALS4891 Code deleted
	protected NbaDst nbaDstWithAllTransactions = null; //APSL4417
	
	public NbaAutomatedProcessResult getDuplicateWorkResult() {
		return (new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getAlternateStatus()));
	}
	
	//ALS4005 New Method
	protected void processDuplicateWork(List duplicateWorks) throws NbaBaseException {
		if (duplicateWorks.size() == 1) {
			if (!getWorkLobs().getFaxedOrEmailedInd() && ((NbaSearchResultVO) duplicateWorks.get(0)).getNbaLob().getFaxedOrEmailedInd()) { 	//ALS4854 ALS4891
				//Check if UW decision has not taken upon for the matching app yet, merge current application with it.
				String businessProcess = NbaUtils.getBusinessProcessId(getUser());
				NbaSearchResultVO searchResult = (NbaSearchResultVO) duplicateWorks.get(0);
				boolean partialMerge = false;
				if (!NbaUtils.isBlankOrNull(searchResult.getContractNumber())) {
					NbaTXLife matchingContract = NbaContractAccess.doContractInquiry(createRequestObject(searchResult, businessProcess));
					ApplicationInfoExtension appInfoExtension = NbaUtils.getFirstApplicationInfoExtension(matchingContract.getPolicy()
							.getApplicationInfo());
					int disp = (searchResult.getNbaLob().getCaseFinalDispstn());
					if (!(disp > 0) && (appInfoExtension.getUnderwritingApproval() != NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER)) {
						partialMerge = true;
					} else {
						NbaProcessingErrorComment npec = createComment();
						npec.setText("Did not match as underwriting decision has already occurred.");
						getWork().addManualComment(npec.convertToManualComment());
						//ALS4664 Code deleted
					}
				} else {
					partialMerge = true;
				}
				if (partialMerge) {
					convertFaxedToPaper((NbaSearchResultVO) duplicateWorks.get(0));
					NbaProcessingErrorComment npec = createComment();
					npec.setText("Paper application received - needs review by NBCM.");
					getWork().addManualComment(npec.convertToManualComment());
				}
			//Begin ALS4854 ALS4891	
			} else {
				NbaProcessingErrorComment npec = createComment();
				npec.setText("Single match found- case not merged. Needs review.");
				getWork().addManualComment(npec.convertToManualComment());
			}
			//End ALS4854 ALS4891	
		} else if (duplicateWorks.size() > 1) {
			//Begin QC1300
			NbaProcessingErrorComment npec = createComment();
			npec.setText("Multiple matches found for the case.");
			getWork().addManualComment(npec.convertToManualComment());
			//End QC1300
			//ALS4664 Code deleted
		}
		setResult(getDuplicateWorkResult()); //ALS4664
	}

	public void doProcess() throws NbaBaseException {
		List informalCases = findMatchingInformalCases();
		getWorkLobs().deletePolicyNumber(); //ALS4833
		//ALS4005 Code Deleted
		processMatchingInformalCases(informalCases);
	}

	/**
	 * This method finds matching informal cases for the current case. The criterion for matching are configured in VP/MS.
	 * @return
	 * @throws NbaBaseException
	 */
	protected List findMatchingInformalCases() throws NbaBaseException {
		getLogger().logDebug("Find informal Case work items");
		NbaSearchVO searchVO = lookupMatchingWork("P_InformalFormalSearchKey", null, PRIMARY_SEARCH, NbaConstants.TRIAL_APPLICATION);
		if (searchVO.getSearchResults().isEmpty()) {
			searchVO = lookupMatchingWork("P_InformalFormalSearchKey", null, SECONDRY_SEARCH, NbaConstants.TRIAL_APPLICATION);
		}
		if (searchVO.getSearchResults().isEmpty()) {
			searchVO = lookupMatchingWork("P_InformalFormalSearchKey", null, TERTIARY_SEARCH, NbaConstants.TRIAL_APPLICATION);//APSL3856
		}
		NbaSearchResultVO informalResult = null;
		NbaTXLife informalContract = null;
		String businessProcess = NbaUtils.getBusinessProcessId(getUser());
		List informalCases = searchVO.getSearchResults();
		int count = informalCases.size();
		for (int i = count - 1; i >= 0; i--) {
			informalResult = (NbaSearchResultVO) informalCases.get(i);
			//Filter out the cases that have not been submitted yet or that are in END queue
			if (NbaUtils.isBlankOrNull(informalResult.getContractNumber()) || informalResult.getStatus().equalsIgnoreCase("DUPLICATE")
					|| informalResult.getStatus().equalsIgnoreCase(NbaConstants.A_STATUS_INFORMAL_APP_DONE) || A_STATUS_INFEXPIRD.equalsIgnoreCase(informalResult.getStatus()) || informalResult.getStatus().equalsIgnoreCase(NbaConstants.FINAL_DISPOSITION_DONE) ) { // NBLXA-1382 // APSL1498 //ALS5030 ALS5522
				informalCases.remove(i);
			} else {
				informalContract = NbaContractAccess.doContractInquiry(createRequestObject(informalResult, businessProcess));
				if (!isMergeNeeded(informalContract)) {
					informalCases.remove(i);
				}
			}
		}
		return informalCases;
	}

	protected void processMatchingInformalCases(List informalCases) throws NbaBaseException {
		if (informalCases.isEmpty()) {
			suspendWorkItem();
//        	start SR564247 Retrofit Predictive Analysis
			if (getWork().getNbaLob().getPredictiveInd()) {
				resetStatusForPredictive();
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));//APSL1488
			}
//        	End SR564247 Retrofit Predictive Analysis  
		} else if (informalCases.size() > 1) {
			addComment("Multiple matches to informal application found");
			//Begin QC1300
            NbaProcessingErrorComment npec = createComment();
			npec.setText("Multiple matches found for the case.");
        	getWork().addManualComment(npec.convertToManualComment());
        	//End QC1300
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getAlternateStatus()));////APSL4226
		} else {
			processTentativeOfferRequirement((NbaSearchResultVO) informalCases.get(0));//ALS4885
			mergeAppWithMatching((NbaSearchResultVO) informalCases.get(0)); //ALS4005
			//Begin ALS4742
			if (getMatchingTXLife() != null) {
				if (getMatchingTXLife().getNbaHolding().getInformalAppApproval() != NbaOliConstants.OLIX_INFORMALAPPROVAL_OFFERACCEPTED) {
					//APSL4417 ::Start
					
					NbaTXLife changedHolding = getMatchingTXLife();
					// setWork(getLockedMatchingWork());
					NbaMIBReportUtils mibUtils = new NbaMIBReportUtils(changedHolding, user);
					setNbaDstWithAllTransactions(getLockedMatchingWork());
					mibUtils.setNbaDstWithAllTransactions(getNbaDstWithAllTransactions());
	                mibUtils.processMIBReportsForAContract(getLockedMatchingWork(), true);
	                //APSL4417 ::END
					ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getMatchingTXLife().getPrimaryHolding()
							.getPolicy().getApplicationInfo());
					appInfoExt.setInformalAppApproval(NbaOliConstants.OLIX_INFORMALAPPROVAL_OFFERACCEPTED);
					appInfoExt.setClosureInd(NbaConstants.CLOSURE_ACTIVE);
					appInfoExt.setActionUpdate();
					getMatchingTXLife().setAccessIntent(UPDATE);
					NbaContractAccess.doContractUpdate(getMatchingTXLife(), getLockedMatchingWork(), getUser());
				}
			}
			//End ALS4742
		}
	}

	protected NbaTXRequestVO createRequestObject(NbaSearchResultVO searchResult, String businessProcess) {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
		nbaTXRequest.setNbaLob(searchResult.getNbaLob());
		nbaTXRequest.setNbaUser(getUser());
		nbaTXRequest.setWorkitemId(searchResult.getWorkItemID());
		nbaTXRequest.setCaseInd(searchResult.isCase());
		nbaTXRequest.setAccessIntent(READ);
		nbaTXRequest.setBusinessProcess(businessProcess);
		return nbaTXRequest;
	}

	protected boolean isMergeNeeded(NbaTXLife informalContract) throws NbaBaseException {
		getLogger().logDebug("Check if Merge Needed ");
		ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(informalContract.getPrimaryHolding().getPolicy()
				.getApplicationInfo());
		if (appInfoExtn != null) {
			Date informalOfferDate = appInfoExtn.getInformalOfferDate();
			// If no informal offer date, then this check is not required and merging can be done
			if (informalOfferDate == null) {
				return true;
			}
			Date currentDate = new Date();
			double days = (currentDate.getTime() - informalOfferDate.getTime()) / (1000D * 60 * 60 * 24);
			//Start APSL3466
			NbaVpmsResultsData data = null;
			String formalMergeCriteria = null;
			data = getDataFromVpms(NbaVpmsAdaptor.EP_FORMAL_MERGE_CRITERIA, null, -1);
			if (data.getResultsData() != null && data.getResultsData().size() > 0) {
				formalMergeCriteria = (String) data.getResultsData().get(0);
			}
			if(formalMergeCriteria != null){
				//if (days > Long.parseLong(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.FORMAL_MERGE_CRITERIA))) {
				if (days > Long.parseLong(formalMergeCriteria)) {
					return false;
				}
			}//End APSL3466
		}
		return true;
	}

	protected void suspendWorkItem() throws NbaBaseException, NbaVpmsException {
		if (!isAllowableSuspendDaysOver(getWorkLobs())) {
			NbaVpmsResultsData data = null;
			data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_SUSPEND_ACTIVATE_DATE, null, -1);
			if (data.getResultsData() != null && data.getResultsData().size() > 0) {
				String activateDate = (String) data.getResultsData().get(0);
				suspendWorkItem("Suspended waiting for informal match", NbaUtils.getDateFromStringInAWDFormat(activateDate));
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "Suspended"));
			}
		} else {
			addComment("Informal contract not received");
			//Begin QC1300
			NbaProcessingErrorComment npec = createComment();
			npec.setText("No matches found for the case.");
			getWork().addManualComment(npec.convertToManualComment());
			//End QC1300
			//APSL2867 QC#5954
			if (getWorkLobs() != null && NbaOliConstants.OLI_APPORIGIN_PARTIAL == getWorkLobs().getAppOriginType())
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getAlternateStatus()));//APSL4226
			else
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getFailStatus()));//APSL4226
		}
	}
	
	//ALS4005 Code Deleted - Moved methods mergeInformalToFormal, retrieveCaseWithTransactionsAndSources, createXML103, createPolicyInfo,
	// createCoverage, createApplicationInfo, createRelations, createPrimaryInsuredInfo, createAttachments, createXML103Source to
	// NbaProcFormalBaseProxy.java.
	
	//ALS5199 New Method - Overrided parent class method
	//This method deletes all TXLIfe objects related to Proposed Insured 2 of informal application
	protected void createXML103(NbaTXLife sourceTXLife) throws NbaBaseException {
		try {
			if (!(getWorkLobs().getProductTypSubtyp().equalsIgnoreCase("4") || getWorkLobs().getProductTypSubtyp().equalsIgnoreCase("106"))) {
				NbaTXLife cloneTxLife = new NbaTXLife(sourceTXLife.toXmlString());
				List parties = null;
				parties = cloneTxLife.getAllParties(NbaOliConstants.OLI_REL_COVINSURED);
				for (int i = 0; i < parties.size(); i++) {
					NbaParty party = (NbaParty) parties.get(i);
					deleteParty(cloneTxLife, party.getID());
				}
				super.createXML103(cloneTxLife);
			} else {
				super.createXML103(sourceTXLife);
			}

		} catch (Exception ex) {
			throw new NbaBaseException(ex);
		}

	}
	//ALS5199 New Method
	public void deleteParty(NbaTXLife txLife, String partyId) {
		deletePartyHoldings(txLife, partyId);
		deleteRelations(txLife, partyId);
		deleteCoverages(txLife, partyId);
		deleteSignatureInfo(txLife, partyId);
		deleteFormInstanceSignatureInfo(txLife, partyId);
		deletePartyFromTxLife(txLife, partyId);
		deleteRequirementInfoFromTxLife(txLife, partyId);
	}
	//ALS5199 New Method
	public void deletePartyHoldings(NbaTXLife txLife, String partyId) {
		if ( partyId == null ) return ;
		List relations = txLife.getOLifE().getRelation();
		Relation relation = null;
		String primaryHoldingId = txLife.getPrimaryHolding().getId();
		if (relations != null) {
			Iterator relItr = relations.iterator();
			while (relItr.hasNext()) {
				relation = (Relation) relItr.next();
				if (relation.isActionDelete()) {
					continue;
				}
				if (partyId.equalsIgnoreCase(relation.getRelatedObjectID()) && relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING) {
					if (!primaryHoldingId.equalsIgnoreCase(relation.getOriginatingObjectID())) {
						deleteHolding(txLife, relation.getOriginatingObjectID());
					}
				}
			}
		}
	}
	//ALS5199 New Method
	public void deleteHolding(NbaTXLife txLife, String holdingId) {
		deleteRelations(txLife, holdingId);
		Holding tempHolding = NbaTXLife.getHoldingFromId(holdingId, txLife.getOLifE().getHolding());
		if (tempHolding != null) {
			tempHolding.setActionDelete();
		}
	}
	//ALS5199 New Method
	public void deleteRelations(NbaTXLife txLife, String objectId) {
		if ( objectId == null ) return ;
		List relations = txLife.getOLifE().getRelation();
		Relation relation = null;
		if (relations != null) {
			Iterator relItr = relations.iterator();
			while (relItr.hasNext()) {
				relation = (Relation) relItr.next();
				if (relation.isActionDelete()) {
					continue;
				}
				if (objectId.equalsIgnoreCase(relation.getOriginatingObjectID()) || objectId.equalsIgnoreCase(relation.getRelatedObjectID())) {
					relation.setActionDelete();
					if (objectId.equalsIgnoreCase(relation.getOriginatingObjectID())) {
						deleteParty(txLife, relation.getRelatedObjectID());
					}
				}
			}
		}
	}
	//ALS5199 New Method
	public void deleteCoverages(NbaTXLife txLife, String partyId) {
		if (partyId == null) return;
		List coverages = txLife.getLife().getCoverage();
		Iterator covIt = coverages.iterator();
		Coverage coverage = null;
		while (covIt.hasNext()) {
			coverage = (Coverage) covIt.next();
			Iterator lifePartItr = coverage.getLifeParticipant().iterator();
			while (lifePartItr.hasNext()) {
				LifeParticipant lifePar = (LifeParticipant) lifePartItr.next();
				if (lifePar != null && partyId.equalsIgnoreCase(lifePar.getPartyID()) && NbaUtils.isInsuredParticipant(lifePar)) {
					coverage.setActionDelete();
				}
			}
		}
	}
	//ALS5199 New Method
	public void deleteSignatureInfo(NbaTXLife txLife, String partyId) {
		List signs = txLife.getPolicy().getApplicationInfo().getSignatureInfo();
		if (partyId != null && signs != null & signs.size() > 0) {
			Iterator signsIt = signs.iterator();
			while (signsIt.hasNext()) {
				SignatureInfo signInfo = (SignatureInfo) signsIt.next();
				if (partyId.equals(signInfo.getSignaturePartyID())) {
					signInfo.setActionDelete();
				}
			}
		}
	}
	//ALS5199 New Method
	public void deleteFormInstanceSignatureInfo(NbaTXLife txLife, String partyId) {
		ArrayList formInstanceList = txLife.getOLifE().getFormInstance();
		ArrayList signatureInfoList = null;
		SignatureInfo signInfo = null;
		Iterator itr = formInstanceList.iterator();
		FormInstance frmInstance = null;
		while (itr.hasNext()) {
			frmInstance = (FormInstance) itr.next();
			signatureInfoList = frmInstance.getSignatureInfo();
			Iterator itrSignInfo = signatureInfoList.iterator();
			while (itrSignInfo.hasNext()){
				signInfo = (SignatureInfo) itrSignInfo.next();
				if ( partyId != null && partyId.equals(signInfo.getSignaturePartyID())){
					signInfo.setActionDelete();
				}
			}
		}	
	}
	//ALS5199 New Method
	public void deletePartyFromTxLife(NbaTXLife txLife, String partyId) {
		NbaParty nbaParty = txLife.getParty(partyId);
		if (nbaParty != null) {
			Party party = nbaParty.getParty();
			party.setActionDelete();
		}
	}
	//ALS5199 New Method
	public void deleteRequirementInfoFromTxLife(NbaTXLife txLife, String partyId) {
		List reqInfos = txLife.getPolicy().getRequirementInfo();
		Iterator itr = reqInfos.iterator();
		while (itr.hasNext()) {
			RequirementInfo reqInfo = (RequirementInfo) itr.next();
			if (reqInfo.getAppliesToPartyID().equalsIgnoreCase(partyId)) {
				reqInfo.setActionDelete();
				deleteParty(txLife, reqInfo.getId());
			}
		}
	}
	/**
	 * @return Returns the nbaDstWithAllTransactions. APSL4417
	 */
	
	public NbaDst getNbaDstWithAllTransactions() {
		return nbaDstWithAllTransactions;
	}
	/**
	 * @param nbaDstWithAllTransactions The nbaDstWithAllTransactions to set.
	 */
	//APSL4417
	public void setNbaDstWithAllTransactions(NbaDst nbaDstWithAllTransactions) {
		this.nbaDstWithAllTransactions = nbaDstWithAllTransactions;
	}
}