package com.csc.fsg.nba.business.process.formal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaAutomatedProcessResult;
import com.csc.fsg.nba.business.transaction.NbaMIBReportUtils;
import com.csc.fsg.nba.database.NbaAutoClosureAccessor;
import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAutoClosureContract;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;

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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */

/**
 * This class executes APFORMAL for a case whose Application Origin (APTP) is Formal.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>ALS3091</td><td>AXA Life Phase 1</td><td>General code clean up of NbaProcFormal</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaFormaFromInformalMergeProxy extends NbaFormalFromInformalProxy {
	private NbaTXLife matchingTXLife;
	private NbaTXLife originalTXLife;
	static List excludedsourceIds = new ArrayList();
	private static boolean isFormalJIMerged = false;
	private static String[] parts ={};
	
	public NbaTXLife getOriginalTXLife() {
		return originalTXLife;
	}

	public void setOriginalTXLife(NbaTXLife originalTXLife) {
		this.originalTXLife = originalTXLife;
	}

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {
		initializeWork();	
		doProcess();
		if (!"Suspended".equals(getResult().getStatus())) {
			changeStatus(getResult().getStatus(),getRouteReason());
			//	changeStatus(getResult().getStatus());
			doUpdateWorkItem();
			if (getLockedMatchingWork() != null) {
				NbaContractLock.removeLock(getUser()); //ALS4355
				unlockWork(getLockedMatchingWork());
			}
		}

		return getResult();
	}

	public void doProcess() throws NbaBaseException {
		setOriginalTXLife(doHoldingInquiry(getWork(), NbaConstants.UPDATE, null));	
		processMatchingInformalCases();
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));//APSL4226\
		setContractAccess(NbaConstants.UPDATE);
		getOriginalTXLife().setAccessIntent(UPDATE);
		NbaContractAccess.doContractUpdate(getOriginalTXLife(), getWork(), getUser());
	}

	public NbaDst retrieveWorkItem(NbaSearchResultVO resultVO, NbaUserVO user) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setNbaUserVO(user);
		retOpt.setWorkItem(resultVO.getWorkItemID(), true);
		retOpt.setLockWorkItem();
		aWorkItem = WorkflowServiceHelper.retrieveWork(user, retOpt);
		return aWorkItem;
	}

	public NbaSearchVO searchContract(String contractKey,NbaUserVO user) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		return searchVO;
	}

	protected void processMatchingInformalCases() throws NbaBaseException { 
		String InformalPolicyNumber =originalTXLife.getNbaHolding().getApplicationInfo().getTrackingID();
		NbaSearchVO searchVO = searchContract(InformalPolicyNumber, getUser());	
		NbaSearchResultVO informalResult = (NbaSearchResultVO)searchVO.getSearchResults().get(0);
		processTentativeOfferRequirement(informalResult);//ALS4885
		mergeAppWithMatching(informalResult); //ALS4005
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
			} else {
				ApplicationInfoExtension appInfoExt = NbaUtils
						.getFirstApplicationInfoExtension(getMatchingTXLife().getPrimaryHolding().getPolicy().getApplicationInfo());
				appInfoExt.setClosureInd(NbaConstants.CLOSURE_ACTIVE);
				appInfoExt.setActionUpdate();
				getMatchingTXLife().setAccessIntent(UPDATE);
				NbaContractAccess.doContractUpdate(getMatchingTXLife(), getLockedMatchingWork(), getUser());
			}
		}
	}

	protected void mergeAppWithMatching(NbaSearchResultVO resultVO) throws NbaBaseException, NbaVpmsException {
		NbaDst matchingCase = retrieveCaseWithTransactionsAndSources(resultVO.getWorkItemID());
		//Clone matchingCase to retrieve comments - RetrieveComments does not retrieves sources
		NbaDst matchingClone = (NbaDst) matchingCase.clone(); //ALS4752
		matchingTXLife = doHoldingInquiry(matchingCase, NbaConstants.UPDATE, null); //ALS4742
		//Copy matching to current
		mergeMatchingCase(getWork().getNbaCase(), matchingCase.getNbaCase(), true,originalTXLife);
		matchingClone = retrieveWorkItemComments(matchingClone); //ALS4752
		NbaProcFormalUtils.mergeMatchingCaseComments(getWork().getNbaCase(), matchingClone.getNbaCase()); //ALS4752
		if(originalTXLife != null)
			updateXML203(matchingTXLife);
		Policy informalPolicy = matchingTXLife.getPolicy(); //APSL2714
		setDisplayImagesInd(informalPolicy.getRequirementInfo());//APSL202,2714
		NbaAutoClosureContract autoClosureContract = new NbaAutoClosureContract();
		autoClosureContract.setContractNumber(matchingTXLife.getPrimaryHolding().getPolicy().getPolNumber());
		NbaAutoClosureAccessor.delete(autoClosureContract);
		//delete the informal work from the database
		if (matchingTXLife.isTransactionError()) {
			addComment("Matching work database delete failed");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
			return;
		}
		matchingCase.setStatus(getOtherStatus());
		//ALS4374 Code Deleted
		matchingCase.increasePriority(getStatusProvider().getCaseAction(), getStatusProvider().getCasePriority());
		NbaUtils.setRouteReason(matchingCase, matchingCase.getStatus());
		try {
			WorkflowServiceHelper.update(getUser(), matchingCase);
			setLockedMatchingWork(matchingCase);
		} catch (NbaBaseException e) {
			throw e;
		}
	}


	protected NbaTXLife getMatchingTXLife() {
		return matchingTXLife;
	}
	//ALS4742 New Method
	protected void setMatchingTXLife(NbaTXLife matchingTXLife) {
		this.matchingTXLife = matchingTXLife;
	}

	protected String getInformalmergeInfo(){
		String informalMergeInfo=null;
		ApplicationInfoExtension appInfoExt =NbaUtils.getFirstApplicationInfoExtension(originalTXLife.getNbaHolding().getApplicationInfo());
		if(!NbaUtils.isBlankOrNull(appInfoExt)){
			informalMergeInfo =appInfoExt.getInformalMergeInfo();
		}
		return informalMergeInfo;
	}
	protected void updateXML203(NbaTXLife matchingTXLife) throws NbaBaseException {
		Policy policy = originalTXLife.getPrimaryHolding().getPolicy();
		Policy informalPolicy = matchingTXLife.getPolicy();
		setExpireMIBCheckReq(informalPolicy.getRequirementInfo());//ALS4665
		String informalMergeInfo =getInformalmergeInfo();
		if(!NbaUtils.isBlankOrNull(informalMergeInfo)){
			String[] parts =informalMergeInfo.split("_");
			if(parts[0].equals(originalTXLife.getNbaHolding().getApplicationInfo().getTrackingID())){			
				if(Long.parseLong(parts[1])== NbaOliConstants.OLI_REL_INSURED){
					mergeRequirementInfo(matchingTXLife,false,NbaOliConstants.OLI_REL_INSURED);
				}
				else if(Long.parseLong(parts[1])== NbaOliConstants.OLI_REL_JOINTINSURED){
					mergeRequirementInfo(matchingTXLife,false,NbaOliConstants.OLI_REL_JOINTINSURED);
				}				
			}
			else if(parts[0].equals(originalTXLife.getNbaHolding().getPolicyNumber())){			
				if(Long.parseLong(parts[1])== NbaOliConstants.OLI_REL_INSURED){
					mergeRequirementInfo(matchingTXLife,true,NbaOliConstants.OLI_REL_INSURED);
				}
				else if(Long.parseLong(parts[1])== NbaOliConstants.OLI_REL_JOINTINSURED){
					mergeRequirementInfo(matchingTXLife,true,NbaOliConstants.OLI_REL_JOINTINSURED);
				}				
			}
			else{
				mergeRequirementInfo(matchingTXLife);
			}
		}
		else{
			mergeRequirementInfo(matchingTXLife);
		}		
		policy.setEndorsement(informalPolicy.getEndorsement());//TO DO
		//	setDisplayImagesInd(informalPolicy.getRequirementInfo());//APSL202 commented for APSL2714
		ApplicationInfo appInfo = policy.getApplicationInfo();
		appInfo.setFormalAppInd(true);
		appInfo.setTrackingID(informalPolicy.getPolNumber());
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo());
		ApplicationInfoExtension informalAppInfoExt = NbaUtils.getFirstApplicationInfoExtension(informalPolicy.getApplicationInfo());
		if (informalAppInfoExt != null) {
			appInfoExt.setInformalOfferDate(informalAppInfoExt.getInformalOfferDate());
			// InformalApplicationDate of informal application is its signed date
			appInfoExt.setInformalApplicationDate(informalPolicy.getApplicationInfo().getSignedDate());
			appInfoExt.setApplicationOrigin(NbaOliConstants.OLI_APPORIGIN_PARTIAL);
			appInfoExt.setActionUpdate();
		}
		appInfo.setActionUpdate();
		mergeSecureComments(matchingTXLife, originalTXLife);//QC11569/APSL2973
		//	resetSecureCommentsFromAttachments(originalTXLife);//APSL1490
		setOriginalTXLife(originalTXLife);
	}

	protected void mergeRequirementInfo( NbaTXLife matchingTXLife) {
		Policy policy = originalTXLife.getPrimaryHolding().getPolicy();
		Policy informalPolicy = matchingTXLife.getPolicy();
		RequirementInfo reqInfo = null;
		for (int i = 0; i < informalPolicy.getRequirementInfo().size(); i++) {
			reqInfo = informalPolicy.getRequirementInfoAt(i);
			Relation informalRelation = matchingTXLife.getRelationByRelatedId(reqInfo.getAppliesToPartyID());
			if (informalRelation != null) {
				//Find matching formal relation
				Relation formalRelation = originalTXLife.getRelationForRelationRoleCode(informalRelation.getRelationRoleCode());
				if (formalRelation != null) {
					RequirementInfo formalReqInfo = reqInfo.clone(false);
					//Reset applies to party id
					formalReqInfo.deleteId();
					NbaOLifEId oLifeId = new NbaOLifEId(originalTXLife);
					oLifeId.setId(formalReqInfo);
					formalReqInfo.getOLifEExtensionAt(0).setActionAdd();
					RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(formalReqInfo);
					if(!NbaUtils.isBlankOrNull(reqInfoExt)){
						TrackingInfo trackingInfo = reqInfoExt.getTrackingInfo();
						if(!NbaUtils.isBlankOrNull(trackingInfo)){
							trackingInfo.setActionAdd();
						}
						Attachment reqinfoAttachment = null;
						int reqInfoAttachmentCount = formalReqInfo.getAttachmentCount();
						for (int j = 0; j < reqInfoAttachmentCount; j++) {
							reqinfoAttachment = formalReqInfo.getAttachmentAt(j);
							reqinfoAttachment.deleteId();
							oLifeId.setId(reqinfoAttachment);
							reqinfoAttachment.setActionAdd();
						}	
						reqInfoExt.setActionAdd();
					}
					formalReqInfo.setAppliesToPartyID(formalRelation.getRelatedObjectID());
					formalReqInfo.setAction("A");		
					policy.addRequirementInfo(formalReqInfo);
					policy.setActionUpdate();
				} 
			}
		}
		setOriginalTXLife(originalTXLife);
	}


	//ALS5199 New Method
	protected static List getExcludedTransactions(NbaCase matchingCase,NbaTXLife originalTXLife) throws NbaBaseException {
		List excludedTransactions = new ArrayList();
		if (!(matchingCase.getNbaLob().getProductTypSubtyp().equalsIgnoreCase("4") || matchingCase.getNbaLob().getProductTypSubtyp().equalsIgnoreCase("106"))) {
			List transactions = matchingCase.getNbaTransactions();
			for (int i = 0; i< transactions.size(); i++) {
				NbaTransaction transaction = (NbaTransaction) transactions.get(i);
				//Exclude Proposed Insured 2 NBREQRMNT transaction
				if (transaction.getNbaLob().getReqPersonCode() == NbaOliConstants.OLI_REL_COVINSURED) {
					excludedTransactions.add(transaction);
				}
			}
		}
		ApplicationInfoExtension appInfoExt =NbaUtils.getFirstApplicationInfoExtension(originalTXLife.getNbaHolding().getApplicationInfo());
		if(!NbaUtils.isBlankOrNull(appInfoExt)){
			String informalMergeInfo =appInfoExt.getInformalMergeInfo();
			if(!NbaUtils.isBlankOrNull(informalMergeInfo)){
				parts =informalMergeInfo.split("_");
				if(parts[0].equals(originalTXLife.getNbaHolding().getApplicationInfo().getTrackingID())){
					List transactions = matchingCase.getNbaTransactions();
					for (int i = 0; i< transactions.size(); i++) {
						NbaTransaction transaction = (NbaTransaction) transactions.get(i);
						if(!NbaUtils.isBlankOrNull(parts[1])){
							if(Long.parseLong(parts[1])== NbaOliConstants.OLI_REL_INSURED){
								if (transaction.getNbaLob().getReqPersonCode() == NbaOliConstants.OLI_REL_JOINTINSURED){
									excludedTransactions.add(transaction);
									List listOfSources = transaction.getNbaSources();
									for (int j = 0; j < listOfSources.size(); j++) {
										NbaSource source = (NbaSource) listOfSources.get(j);
										excludedsourceIds.add(source.getID());

									}

								}
							}
							else if (Long.parseLong(parts[1]) == NbaOliConstants.OLI_REL_JOINTINSURED) {
								if (transaction.getNbaLob().getReqPersonCode() == NbaOliConstants.OLI_REL_INSURED) {
									excludedTransactions.add(transaction);
									List listOfSources = transaction.getNbaSources();
									for (int j = 0; j < listOfSources.size(); j++) {
										NbaSource source = (NbaSource) listOfSources.get(j);
										excludedsourceIds.add(source.getID());
									}
								}
							}
						}					
					}
				}
			}
		}	
		return excludedTransactions;
	}

	public static void copyLobs(NbaLob sourceLobs, NbaLob destLobs, NbaTXLife originalTXLife) {
		destLobs.setBackendSystem(sourceLobs.getBackendSystem());
		destLobs.setCompany(sourceLobs.getCompany());
		destLobs.setOperatingMode(sourceLobs.getOperatingMode());
		//begin ALS5901
		if (sourceLobs.getPolicyNumber() == null) {
			destLobs.deletePolicyNumber();
		} else {
			destLobs.setPolicyNumber(sourceLobs.getPolicyNumber());
		}//end ALS5901
		destLobs.setPlan(sourceLobs.getPlan());
		destLobs.setProductTypSubtyp(sourceLobs.getProductTypSubtyp());
		destLobs.setMiddleInitial(sourceLobs.getMiddleInitial());
		destLobs.setAppState(sourceLobs.getAppState());
		if(isFormalJIMerged && NbaUtils.isSurvivorshipProduct(originalTXLife)){
			destLobs.setLastName(sourceLobs.getJointLastName());
			destLobs.setFirstName(sourceLobs.getJointFirstName());
			destLobs.setSsnTin(sourceLobs.getJointSsnTin());
		}
		else{
		destLobs.setLastName(sourceLobs.getLastName());
		destLobs.setFirstName(sourceLobs.getFirstName());
		destLobs.setSsnTin(sourceLobs.getSsnTin());
		}
	}


	protected static void mergeMatchingCase(NbaCase originalCase, NbaCase matchingCase, boolean breakRelation,NbaTXLife originalTXLife) throws NbaBaseException,
	NbaVpmsException {
		//Move the transactions of matching work to original work
		List transactions = matchingCase.getNbaTransactions();
		List excludedTransactions = getExcludedTransactions(matchingCase, originalTXLife); // ALS5199
		ApplicationInfoExtension appInfoExt =NbaUtils.getFirstApplicationInfoExtension(originalTXLife.getNbaHolding().getApplicationInfo());
		if (!NbaUtils.isBlankOrNull(appInfoExt)) {
			String informalMergeInfo = appInfoExt.getInformalMergeInfo();
			if (!NbaUtils.isBlankOrNull(informalMergeInfo) && !NbaUtils.isBlankOrNull(Long.parseLong(parts[1]))
					&& Long.parseLong(parts[1]) == NbaOliConstants.OLI_REL_JOINTINSURED) {
				isFormalJIMerged = true;
			}
		}
		NbaTransaction sourceTxn = null;
		NbaTransaction newTxn = null;
		  for (int i = 0; i < transactions.size(); i++) {
			sourceTxn = (NbaTransaction) transactions.get(i);
			//Begin ALS5199
			boolean flag = true;
			for (int j = 0; j < excludedTransactions.size(); j++) {
				NbaTransaction excludedWorkItem = (NbaTransaction) excludedTransactions.get(j);
				if (sourceTxn.getID().equalsIgnoreCase(excludedWorkItem.getID())) {
					flag = false;
					break;
				}
			}
			//End ALS5199
			if (flag) { // ALS5199
				newTxn = sourceTxn.clone(false);
				newTxn.getNbaLob().setDisplayIconLob("2");
				NbaSource source = null;
				Iterator listOfSources = newTxn.getNbaSources().iterator();
				while (listOfSources.hasNext()) {
					source = (NbaSource) listOfSources.next();
					source.getNbaLob().setDisplayIconLob("2");
					source.setUpdate();
				}
				copyLobs(originalCase.getNbaLob(), newTxn.getNbaLob(), originalTXLife);
				originalCase.addNbaTransaction(newTxn);
				if (breakRelation) {
					sourceTxn.setBreakRelation();
					sourceTxn.setUpdate();
				} else {
					newTxn.getTransaction().setCreate(NbaConstants.YES_VALUE);
				}
			} // ALS5199
		}

		//Move sources
		List sources = matchingCase.getNbaSources();
		NbaSource matchingSource = null;
		NbaSource newSource = null;
		for (int i = 0; i < sources.size(); i++) {
			matchingSource = (NbaSource) sources.get(i);
			if (!(originalCase.getNbaLob().getPortalCreated() && matchingSource.isXML103()) && !excludedsourceIds.contains(matchingSource.getID())) { // QC8401(APSL1988)
				newSource = matchingSource.clone(false);
				newSource.getNbaLob().setDisplayIconLob("2");
				copyLobs(originalCase.getNbaLob(), newSource.getNbaLob(),originalTXLife);
				originalCase.addNbaSource(newSource);
				if (breakRelation) {
					matchingSource.setBreakRelation();
					matchingSource.setUpdate();
				}
			}
		}
	}

	protected void mergeRequirementInfo( NbaTXLife matchingTXLife,boolean isFormalSurvivorship, Long insured) {
		Policy policy = originalTXLife.getPrimaryHolding().getPolicy();
		Policy informalPolicy = matchingTXLife.getPolicy();
		RequirementInfo reqInfo = null;
		for (int i = 0; i < informalPolicy.getRequirementInfo().size(); i++) {
			reqInfo = informalPolicy.getRequirementInfoAt(i);			
			if((!isFormalSurvivorship && reqInfo.getAppliesToPartyID().contains(insured.toString())) || isFormalSurvivorship){
				Relation formalRelation = null;
				if(isFormalSurvivorship){
					formalRelation = originalTXLife.getRelationForRelationRoleCode(insured);
				}
				else{
					formalRelation = originalTXLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_INSURED);
				}
				if (formalRelation != null) {
					RequirementInfo formalReqInfo = reqInfo.clone(false);
					//Reset applies to party id
					formalReqInfo.deleteId();
					NbaOLifEId oLifeId = new NbaOLifEId(originalTXLife);
					oLifeId.setId(formalReqInfo);
					formalReqInfo.getOLifEExtensionAt(0).setActionAdd();
					RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(formalReqInfo);
					if(!NbaUtils.isBlankOrNull(reqInfoExt)){
						TrackingInfo trackingInfo = reqInfoExt.getTrackingInfo();
						if(!NbaUtils.isBlankOrNull(trackingInfo)){
							trackingInfo.setActionAdd();
						}
						Attachment reqinfoAttachment = null;
						int reqInfoAttachmentCount = formalReqInfo.getAttachmentCount();
						for (int j = 0; j < reqInfoAttachmentCount; j++) {
							reqinfoAttachment = formalReqInfo.getAttachmentAt(j);
							reqinfoAttachment.deleteId();
							oLifeId.setId(reqinfoAttachment);
							reqinfoAttachment.setActionAdd();
						}	
						reqInfoExt.setActionAdd();
					}
					formalReqInfo.setAppliesToPartyID(formalRelation.getRelatedObjectID());
					formalReqInfo.setAction("A");		
					policy.addRequirementInfo(formalReqInfo);
					policy.setActionUpdate();
				} 
			}
		}
		setOriginalTXLife(originalTXLife);
	}

	protected void mergeSecureComments(NbaTXLife informalTXLife, NbaTXLife nbaTXLife) {
		Holding informalHolding = informalTXLife.getPrimaryHolding();
		Holding formalHolding = nbaTXLife.getPrimaryHolding();
		int attachmentCount = informalHolding.getAttachmentCount();
		Attachment attachment = null;
		NbaOLifEId oLifeId = new NbaOLifEId(nbaTXLife);
		for (int i = 0; i < attachmentCount; i++) {
			attachment = informalHolding.getAttachmentAt(i);
			if (NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE == attachment.getAttachmentType()) {
				Attachment formalAttachment =attachment.clone(false);
				if (formalAttachment.hasAttachmentData()) {
					formalAttachment.deleteId();
					oLifeId.setId(formalAttachment);
					formalAttachment.setActionAdd();				
					formalAttachment.getAttachmentData().setActionAdd();
					formalHolding.addAttachment(formalAttachment);
					formalHolding.setActionUpdate();
				}
			}
		}
		String informalMergeInfo = getInformalmergeInfo();
		Party insuredPartyFormal = null;
		Party insuredParty =null;
		int partyAttachmentCount = 0;
		boolean isSurvivorshipCase=false;
		if(!NbaUtils.isBlankOrNull(informalMergeInfo)){
			String[] parts =informalMergeInfo.split("_");
			if(parts[0].equals(originalTXLife.getNbaHolding().getApplicationInfo().getTrackingID())){			
				insuredPartyFormal = nbaTXLife.getPrimaryParty().getParty();
				if(Long.parseLong(parts[1])== NbaOliConstants.OLI_REL_INSURED){
					isSurvivorshipCase=true;
					insuredParty = informalTXLife.getPrimaryParty().getParty();
					partyAttachmentCount = informalTXLife.getPrimaryParty().getParty().getAttachmentCount();				
				}
				else if(Long.parseLong(parts[1])== NbaOliConstants.OLI_REL_JOINTINSURED){
					isSurvivorshipCase=true;
					insuredParty = informalTXLife.getJointParty().getParty();
					partyAttachmentCount = informalTXLife.getJointParty().getParty().getAttachmentCount();									
				}				
			}
			else if(parts[0].equals(originalTXLife.getNbaHolding().getPolicyNumber())){
				insuredParty = informalTXLife.getPrimaryParty().getParty();
				partyAttachmentCount = informalTXLife.getPrimaryParty().getParty().getAttachmentCount();
				if(Long.parseLong(parts[1])== NbaOliConstants.OLI_REL_INSURED){
					isSurvivorshipCase=true;
					insuredPartyFormal = nbaTXLife.getPrimaryParty().getParty();					
				}
				else if(Long.parseLong(parts[1])== NbaOliConstants.OLI_REL_JOINTINSURED){
					isSurvivorshipCase=true;
					insuredPartyFormal = nbaTXLife.getJointParty().getParty();					
				}				
			}
		}		
		
		if(!isSurvivorshipCase){
			Attachment partyAttachment = null;
			 insuredParty = informalTXLife.getPrimaryParty().getParty();
			 insuredPartyFormal = nbaTXLife.getPrimaryParty().getParty();
			 partyAttachmentCount = informalTXLife.getPrimaryParty().getParty().getAttachmentCount();
			for (int i = 0; i < partyAttachmentCount; i++) {
				partyAttachment = insuredParty.getAttachmentAt(i);
				if (NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE == partyAttachment.getAttachmentType()) {
					Attachment formalPartyAttachment =partyAttachment.clone(false);
					if (formalPartyAttachment.hasAttachmentData()) {
						formalPartyAttachment.deleteId();
						oLifeId.setId(formalPartyAttachment);
						formalPartyAttachment.setActionAdd();	
						formalPartyAttachment.getAttachmentData().setActionAdd();
						insuredPartyFormal.addAttachment(formalPartyAttachment);
						insuredPartyFormal.setActionUpdate();
					}
				}
			}	

			if(!NbaUtils.isBlankOrNull(informalTXLife.getJointParty()) && !NbaUtils.isBlankOrNull(nbaTXLife.getJointParty())){
				Attachment jointPartyAttachment = null;
				Party jointInsuredParty = informalTXLife.getJointParty().getParty();
				Party jointIinsuredPartyFormal = nbaTXLife.getJointParty().getParty();
				int jointPartyAttachmentCount = informalTXLife.getJointParty().getParty().getAttachmentCount();
				for (int i = 0; i < jointPartyAttachmentCount; i++) {
					jointPartyAttachment = jointInsuredParty.getAttachmentAt(i);
					if (NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE == jointPartyAttachment.getAttachmentType()) {
						Attachment formalPartyAttachment =jointPartyAttachment.clone(false);
						if (formalPartyAttachment.hasAttachmentData()) {
							formalPartyAttachment.deleteId();
							oLifeId.setId(formalPartyAttachment);
							formalPartyAttachment.setActionAdd();	
							formalPartyAttachment.getAttachmentData().setActionAdd();
							jointIinsuredPartyFormal.addAttachment(formalPartyAttachment);
							jointIinsuredPartyFormal.setActionUpdate();
						}
					}
				}	
			}
		}
		else if(isSurvivorshipCase){
			Attachment partyAttachment = null;
			for (int i = 0; i < partyAttachmentCount; i++) {
				partyAttachment = insuredParty.getAttachmentAt(i);
				if (NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE == partyAttachment.getAttachmentType()) {
					Attachment formalPartyAttachment =partyAttachment.clone(false);
					if (formalPartyAttachment.hasAttachmentData()) {
						formalPartyAttachment.deleteId();
						oLifeId.setId(formalPartyAttachment);
						formalPartyAttachment.setActionAdd();	
						formalPartyAttachment.getAttachmentData().setActionAdd();
						insuredPartyFormal.addAttachment(formalPartyAttachment);
						insuredPartyFormal.setActionUpdate();
					}
				}
			}	
		}
	}
}