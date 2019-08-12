package com.csc.fsg.nba.process.contract.approval;

/* 
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import com.csc.fs.Result;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.business.transaction.NbaApproveContract;
import com.csc.fsg.nba.business.transaction.NbaReinsuranceUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaPrintLogger;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.contract.CommitContractBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaReasonsData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.AxaReinsuranceCalcVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaContractApprovalDispositionRequest;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSpecialInstructionComment;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaVpmsRequestVO;
import com.csc.fsg.nba.vo.nbaschema.SpecialInstruction;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.ActivityExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.ContractChangeInfo;
import com.csc.fsg.nba.vo.txlife.ContractChangeOutcome;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.EndorsementExtension;
import com.csc.fsg.nba.vo.txlife.InitialDecision;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.TentativeDisp;
import com.csc.fsg.nba.vo.txlife.UnderwritingResult;
import com.csc.fsg.nba.vo.txlife.UnderwritingResultExtension;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.workflow.NbaWorkflowDistribution;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;




/**
 * This business process is responsible for committing the final disposition done on the contract. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>NBA230</td><td>Version 8</td><td>nbA Reopen Date Enhancement Project</td></tr>
 * <tr><td>AXAL3.7.09(R)</td><td>AXA Life Phase 1</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>ALS4838</td><td>AXA Life Phase 1</td><td>Comma(,) missing between rating reasons</td></tr>
 * <tr><td>ALS5017</td><td>AXALife Phase 1</td><td>QC # 4181  - 3.7.31 MIB - reported codes did not transmit to MIB upon final disposition</td></tr>
 * <tr><td>ALS5149</td><td>AXALife Phase 1</td><td>QC # 4326 - 3.7.14.1_1: New approved case generated four (4) NBPRTEXT work items</td></tr>
 * <tr><td>ALS5344</td><td>AXA Life Phase 1</td><td>QC #4526 - MIB reported codes not being sent on final dispositionfor informals</td></tr>
 * <tr><td>SR534655</td><td>Discretionary</td><td>nbA ReStart � Underwriter Approval</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class CommitFinalDispositionBP extends CommitContractBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		NbaTXLife origTXLife = null; //ALS5697
		NbaContractApprovalDispositionRequest request = (NbaContractApprovalDispositionRequest) input; //ALS5697
		try {
			//ALS5697 Code Deleted
			// clone the work item, in case we have a transaction validation error
			// don't want to change the work item in the session unless we have success
            request.setWork((NbaDst) request.getWork().clone());
            // if approval or negative disposition was not specified, just save the changed data
            if (request.getApproval() == NbaOliConstants.OLI_TC_NULL) {
        		markupWorkItemForSave(request);
        		NbaTXLife contract = request.getContract();
        		contract.setBusinessProcess(NbaConstants.PROC_UW_DISPOSITION);//APSL1334
        	//	markupContractForSave(request);	//APSL915 Code commented
        		result = persistContract(request);//ALS5149
				processResult(result);//ALS5149
				return result;//ALS5149
            }
            origTXLife = (NbaTXLife) request.getContract().clone(false); //ALS5697
            deleteRQTPLob(request);
            //Start NBLXA-1542
			List activies = request.getContract().getOLifE().getActivity();
			if (NbaUtils.getActivityByTypeCode(activies, NbaOliConstants.OLI_ACTTYPE_1009900003).size() == 0) {
				addFirstApprovalActivity(request.getContract(), request.getNbaUserVO().getUserID());
			}
			//End NBLXA-1542           
			result = applyFinalDisposition(request);
			if (!result.hasErrors() && NbaUtils.isNegativeDisposition(request.getNbaDst()) && request.getNbaDst().isSuspended()) { //NBLXA-1246
				NbaSuspendVO suspendVO = new NbaSuspendVO();
				suspendVO.setCaseID(request.getNbaDst().getID());
				WorkflowServiceHelper.unsuspendWork(request.getNbaUserVO(), suspendVO);
			}
		} catch (NbaTransactionValidationException ntve) {
			resetAssignFinalDisp(request.getContract(), origTXLife); //ALS5697
			addMessage(result, ntve.getMessage());
		} catch (NbaBaseException nbe) {
			resetAssignFinalDisp(request.getContract(), origTXLife); //ALS5697
			addExceptionMessage(result, nbe);
		} catch (Exception e) {
			resetAssignFinalDisp(request.getContract(), origTXLife); //ALS5697
			addExceptionMessage(result, e);
		}
		return result;
	}
	
	/**
	 * @param request
	 * @throws NbaBaseException
	 */
	protected void deleteRQTPLob(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
		if(NbaConstants.A_WT_APPLICATION.equals(request.getWork().getWorkType())) {
			//Begin NBA331.1, APSL5055
			Result result = callService(ServiceCatalog.UW_REQTYPES_RECEIVED_DISASSEMBLER, request.getWork().getID());
			if (!result.hasErrors()) {
				result = invoke(ServiceCatalog.DELETE_UW_REQTYPES_RECEIVED, result.getData());
			}
			//Deleted code for lob deletion on DST object.
			//End NBA331.1, APSL5055
		}
	}
	
		
	/**
	 * Applies the approve/decline details from the <code>NbaContractApprovalDispositionRequest</code>
	 * to the contract.  The issue other than applied for indicator, the re-auto underwrite
	 * indicator, and the requested issue date can be saved as LOB data on the workitem.
	 * @param request
	 */
	protected void markupWorkItemForSave(NbaContractApprovalDispositionRequest request) {
		NbaLob workLob = request.getWork().getNbaLob();

		workLob.setIssueOthrApplied(request.isIssueOtherThanAppliedFor());
		workLob.setReUnderwriteInd(request.isReAutoUnderwrite());
		if (isAllowRequestedIssueDate(request.getContract())) {
			if (request.getIssueDate() == null) {
				workLob.deleteIssueDate();
			} else {
				workLob.setIssueDate(request.getIssueDate());
			}
		}
		
		// NBLXA-2174 Begins
		Policy policy = request.getContract().getPolicy();
		PolicyExtension policyExt = NbaUtils.getPolicyExtension(policy);
		if (!NbaUtils.isBlankOrNull(policyExt)) {
			policyExt.setSimplifiedIssueInd(request.isApprovedAsSimplifiedIssued());
			policyExt.setActionUpdate();
		}
		// NBLXA-2174 End
		
		// NBLXA-2223 Starts
		ApplicationInfo appInfo = request.getContract().getNbaHolding().getApplicationInfo();
		ApplicationInfoExtension appInfoExte = NbaUtils.getAppInfoExtension(appInfo);
		if (!NbaUtils.isBlankOrNull(appInfoExte)&& isIUPManualOverridden(request)) {
			appInfoExte.setInternationalUWProgInd(request.isIupIndicator());
			appInfoExte.setIupoverrideIndCode(NbaOliConstants.NBA_ANSWERS_YES);
			appInfoExte.setActionUpdate();
			workLob.setNbaIUPInd(appInfoExte.getInternationalUWProgInd());
		}
		// NBLXA-2223 Ends
				
		request.getWork().setUpdate();
		
	}
	
	/**
	 * Applies the approve/decline details from the <code>NbaContractApprovalDispositionRequest</code>
	 * to the contract.
	 * @param request
	 */
	protected void markupContractForSave(NbaContractApprovalDispositionRequest request) {
		NbaTXLife contract = request.getContract();
		contract.setBusinessProcess(NbaConstants.PROC_UW_DISPOSITION); //ALS2737, ALS2763

		if (isAllowRequestedIssueDate(contract)) {
			ApplicationInfo appInfo = contract.getPolicy().getApplicationInfo();
			if (request.getIssueDate() == null) {
				if (appInfo.hasRequestedPolDate()) {
					appInfo.deleteRequestedPolDate();
					appInfo.setActionUpdate();
				}
			} else {
				appInfo.setRequestedPolDate(request.getIssueDate());
				appInfo.setActionUpdate();
			}
		}
	}

	/**
	 * Returns true if the user can set a requested issue date.
	 * @param contract
	 * @return
	 */
	protected boolean isAllowRequestedIssueDate(NbaTXLife contract) {
		Policy policy = contract.getPolicy();
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		return !(policy.getApplicationInfo().getApplicationType() == NbaOliConstants.OLI_APPTYPE_CHANGE && contract.isFixedPremium() &&
				(policyExtension != null && policyExtension.getLoansInd()));
	}

	/**
	 * Apply final disposition on the case.
	 * @param request NbaContractApprovalDispositionRequest object
	 * @return NbaContractApprovalDispositionRequest
	 * @throws NbaBaseException
	 */
	protected AccelResult applyFinalDisposition(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
		//Create a tentative Disposition object for Level 1 with decision taken on the final disposition view
		long undStatus = getUnderwriterDisposition(request);
		ApplicationInfo appInfo = request.getContract().getNbaHolding().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getAppInfoExtension(appInfo);
		addTentativeDisposition(request.getUnderwritingStatusReason(), undStatus, request.getNbaUserVO(), appInfoExt, request.getUnderwriterRole(),
				request.getUnderwriterRoleLevel());
		//Check if we need to add the disposition for level 2 and level 3. This is based on the earlier decisions
		String currentDecision = null;
		if (hasPreviousDecisions(appInfoExt)) {
			updateDispositionsForEarlierDecisions(appInfoExt, request.getNbaUserVO());
			currentDecision = String.valueOf(NbaOliConstants.NBA_ADDITIONALDECISION_AGREE);
		}
		//Begin APSL5128
		if(NbaUtils.isUnapproveActivityforActiveContractChange(request.getContract())){
			request.setContractChangeApprovalInd("1");
		}
		//ENd APSL5128
		NbaVpmsRequestVO vpmsRequestVO = retrieveDataFromBusinessRules(request, currentDecision);
		updateFinalDispositionFields(vpmsRequestVO.getRule(), request, undStatus, appInfoExt);
		appInfoExt.setAddlPlacementDays(request.getOverrideDeliveryDays());//ALS4506/QC3504
		appInfoExt.setActionUpdate();
		appInfo.setActionUpdate();
		//Disable Reinsurance Calculcation APSL2242
//		Enabling Reinsurance Calculcation APSL2536 
		if(!NbaUtils.isNegativeDisposition(undStatus) && !request.getContract().isInformalApplication()
				&& !request.getContract().isSIApplication()){//AXAL3.7.10C , APSL2808
			boolean jumboLimitInd =  updateReinsuranceCalculcationLimits(request);	//APSL3491
			//QC10844/ALII1712;QC10884 moved here
			NbaReinsuranceUtils.updateReinsuranceInfoForTAI(request.getContract(),jumboLimitInd);//APSL2536,APSL2806,APSL3491
		}
		//Enabling Reinsurance Calculcation APSL2536
		if (vpmsRequestVO.getRule() == NbaConstants.FINAL_AUTHORITY 
				&& !request.getContract().isSIApplication()) { //ALS5344  APSL2808
			applyUWApproveAndDisposition(request);
		}
		//ALS4978 - void previous automated delivery instructions
		//ALS4967 - Begin ALS5862 Moved here
		//ALII623 Code moved to applyUWApproveAndDisposition method
		//ALS4967 - End
		//ALS4823 begin ALS5862 Moved the code here
				//APSL613 begin
		if(NbaPrintLogger.getLogger().isDebugEnabled()){
			if(NbaUtils.isPrintAttachedToDst(request.getWork())){
				NbaPrintLogger.getLogger().logDebug("Print attached to DST before updateContract() - "+request.getWork().getNbaLob().getPolicyNumber());
			}
		}
		//APSL613 end
		    return updateContract(request, vpmsRequestVO,false);
		//ALS4823 end
	}
	
	//AXAL3.7.10C New Method
	protected boolean updateReinsuranceCalculcationLimits(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
		NbaTXLife nbaTXLife = request.getContract();
		AxaReinsuranceCalcVO reinCalcVO = new AxaReinsuranceCalcVO();
		reinCalcVO.setNbaTXLife(nbaTXLife);
		reinCalcVO.setAutoRun(true);
		AccelResult result = (AccelResult) callBusinessService("CalculateReinsuranceBP", reinCalcVO);
		if (result.hasErrors()) {
			throw new NbaBaseException("Error in calculating the reinsurance retention limits");
		}
		request.setContract(reinCalcVO.getNbaTXLife());
		return reinCalcVO.isJumboLimitInd(); //APSL3491
	}

	/**
	 * This method applies the finalDisposition and update the contract
	 * @param request NbaContractApprovalDispositionRequest
	 * @return AccelResult
	 * @throws NbaBaseException
	 */
	//ALS4823 signature changed - add bolean for unlock work
	protected AccelResult updateContract(NbaContractApprovalDispositionRequest request, NbaVpmsRequestVO vpmsRequest,boolean unlockWork) throws NbaBaseException {
		// Set the new status and priority to be applied on a successful contract commit
		String passStatus = getEquitableApprovalStatus(request, vpmsRequest.getPassStatus()); //ALCP19AA
		request.setStatus(passStatus); //ALCP19AA
		request.setReason(vpmsRequest.getReason());//ALS5260
		request.setAction(vpmsRequest.getCaseAction());
		request.setPriority(vpmsRequest.getCasePriority());
		// Status is already retrieved, don't need to retrieve new status
		request.setUserID(null);
		request.setUpdateWork(true);
		request.setUnlockWork(unlockWork);
		AccelResult result = persistContract(request);//ALS5149
		processResult(result);//ALS5149
		return result;//ALS5149
	}

	
	/**
	 * This method applies the finalDisposition and update the contract
	 * @param request NbaContractApprovalDispositionRequest
	 * @return AccelResult
	 * @throws NbaBaseException
	 */
	protected AccelResult updateContract(NbaContractApprovalDispositionRequest request, NbaVpmsRequestVO vpmsRequest) throws NbaBaseException {
		return updateContract(request,vpmsRequest,true); //ALS4823 signature changed
	}

	/**
	 * The method updates Final disposition fields with values from the HTML components. if no final authority, final disposition fields will not be
	 * set
	 * @param rule integer representing the final authority on the case.
	 * @param request NbaContractApprovalDispositionRequest object
	 * @param undStatus long the disposition made by underwriter7*
	 * @param appInfoExt ApplicationInfoExtension object that contains the list of tentative dispositions
	 */
	protected void updateFinalDispositionFields(int rule, NbaContractApprovalDispositionRequest request, long undStatus,
			ApplicationInfoExtension appInfoExt) throws NbaBaseException {
		NbaLob lob = request.getWork().getNbaLob();
		if (request.getApproval() == NbaOliConstants.OLI_POLSTAT_APPROVED) {
			lob.setIssueOthrApplied(request.isIssueOtherThanAppliedFor());
			lob.setReUnderwriteInd(request.isReAutoUnderwrite());
			updateLastReqIndicator(request.getContract(),request.getWork(),request.getNbaUserVO()); //Added for NBLXA-1432
		} else if (request.getApproval() == NbaOliConstants.OLI_POLSTAT_DECISSUE) {
			//begin NBA230
			ApplicationInfo applicationInfo = request.getContract().getPolicy().getApplicationInfo();
			ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
			if (applicationInfoExtension != null && applicationInfoExtension.getReopenDate() != null) {
				applicationInfoExtension.deleteReopenDate();
				applicationInfoExtension.setActionUpdate();
			}
			//end NBA230
		}
		NbaTXLife contract = request.getContract();
		contract.setBusinessProcess(NbaConstants.PROC_UW_APPROVE_CONTRACT); //ALS5614
		Policy policy = contract.getPolicy();
		if (NbaUtils.isNegativeDisposition(undStatus)) { //ALS2774 //ALS3121 //ALS3251 //ALS5614
			policy.getApplicationInfo().setHOCompletionDate(new Date());
			contract.setBusinessProcess(NbaConstants.PROC_UW_DISPOSITION);
		}
		
		//NBLXA-2174 Begins
		PolicyExtension policyExt = NbaUtils.getPolicyExtension(policy);
		if(!NbaUtils.isBlankOrNull(policyExt)){
			policyExt.setSimplifiedIssueInd(request.isApprovedAsSimplifiedIssued());
			policyExt.setActionUpdate();
		}
		//NBLXA-2174 End
		
		// NBLXA-2223 Starts
		ApplicationInfo appInfo = request.getContract().getNbaHolding().getApplicationInfo();
		ApplicationInfoExtension appInfoExte = NbaUtils.getAppInfoExtension(appInfo);
		if (!NbaUtils.isBlankOrNull(appInfoExte)&& isIUPManualOverridden(request)) {
			appInfoExte.setInternationalUWProgInd(request.isIupIndicator());
			appInfoExte.setIupoverrideIndCode(NbaOliConstants.NBA_ANSWERS_YES);
			appInfoExte.setActionUpdate();
			lob.setNbaIUPInd(appInfoExte.getInternationalUWProgInd());
		}
		// NBLXA-2223 Ends
		
		
		//APSL5128 Begin
		if (!NbaUtils.isBlankOrNull(request.getContract())) {
			ContractChangeInfo conChngInfo = NbaUtils.getActiveContractChangeInfo(request.getContract());
			if (!NbaUtils.isBlankOrNull(conChngInfo)) {
				List<ContractChangeOutcome> outcomeList = conChngInfo.getContractChangeOutcome();
				if (!NbaUtils.isBlankOrNull(outcomeList) && !outcomeList.isEmpty()) {
					for (ContractChangeOutcome outcome : outcomeList) {
						if (NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_UWREVIEW == outcome.getOutcomeType()) {
							outcome.setOutcomeProcessed(true);
							outcome.setActionUpdate();
							if (!NbaUtils.isDuplicateActivityPresent(contract,NbaOliConstants.OLI_ACTTYPE_CONTRACTCHANGE,conChngInfo.getId(),String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_UWRVCMLTD))) {
								Activity activity = createActivity(conChngInfo.getId(), String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_UWRVCMLTD));
								contract.getOLifE().addActivity(activity);
							}
							break;
						}
					}
				}
			}
		}
		//APSL5128 End
		
		//ALS5862 Code Moved
		
		//Final Authority and Disposition = APPROVE
		if (rule == NbaConstants.FINAL_AUTHORITY  ) { //APSL2808
			if (request.isInformalApp() && request.isInformalAppAccepted()) {
				appInfoExt.setInformalAppApproval(request.getApproval());
				appInfoExt.setInformalOfferDate(new java.util.Date());//ALS1946
				appInfoExt.getTentativeDisp().clear();
				contract.setBusinessProcess(NbaConstants.PROC_UW_APPROVE_CONTRACT);
			} else if (undStatus == NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED  ) {
				if(!request.getContract().isSIApplication()){  // APSL3159(QC11964) Approved case handling for non SI cases
				String originalUndId = null;
				List tentDispList = appInfoExt.getTentativeDisp();
				if (tentDispList.size() > 0) {
					TentativeDisp tentDisplevelOne = (TentativeDisp) tentDispList.get(0);
					if (tentDisplevelOne.getDispLevel() == NbaConstants.TENT_DISP_LEVEL_ONE) {
						originalUndId = tentDisplevelOne.getDispUndID();
					}
				}
				appInfoExt.setApprovalUnderwriterId(originalUndId);
				appInfoExt.setUnderwritingApproval(undStatus);				
				policy.getApplicationInfo().setHOCompletionDate(new Date());
				if (isAllowRequestedIssueDate(contract)) {
					policy.getApplicationInfo().setRequestedPolDate(request.getIssueDate());
					lob.setIssueDate(request.getIssueDate());
				}
				contract.setBusinessProcess(NbaConstants.PROC_UW_APPROVE_CONTRACT);
				// ALS4967 - deleted code
				}else{ //	APSL3159(QC11964)
					// is this the first tentative disposition for approving the contract and in standalone mode
					if (appInfoExt.getTentativeDispCount() == 1 
							&& undStatus == NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED
							&& NbaServerUtility.isDataStoreDB(lob, request.getNbaUserVO())) {
						contract.setBusinessProcess(NbaConstants.PROC_UW_APPROVE_CONTRACT);
					}
				}
			} else if (undStatus != NbaConstants.LONG_NULL_VALUE && undStatus != NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED) {
				//If final authority and Final Disp = Do not Issue
				lob.setCaseFinalDispstn((int) undStatus);
				lob.setFinalDispReason((int) (request.getUnderwritingStatusReason()));
				appInfoExt.setUnderwritingStatus(request.getUnderwritingStatus());
				appInfoExt.setUnderwritingStatusReason(request.getUnderwritingStatusReason());
				if (request.isInformalApp()) {
					appInfoExt.getTentativeDisp().clear();
				}
				contract.setBusinessProcess(NbaConstants.PROC_UW_DISPOSITION); //ALS5614
			}
		} else {
			// is this the first tentative disposition for approving the contract and in standalone mode
			if (appInfoExt.getTentativeDispCount() == 1 
					&& undStatus == NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED
					&& NbaServerUtility.isDataStoreDB(lob, request.getNbaUserVO())) {
				contract.setBusinessProcess(NbaConstants.PROC_UW_APPROVE_CONTRACT);
			}
		}
	}

	/**
	 * This method returns the disposition selected by user from the managed bean
	 * 
	 * @param undApprove
	 *                long value
	 * @param undStatus
	 *                status selected by underwriter while selecting "Do Not Issue" radio on final disposition view
	 * @return disposition long value
	 */
	protected long getUnderwriterDisposition(NbaContractApprovalDispositionRequest request) {
		long disposition = -1;
		if (request.getApproval() == NbaOliConstants.OLI_POLSTAT_APPROVED) {
			disposition = NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED;
		} else if (request.isInformalApp() && request.isInformalAppAccepted()) {
			disposition = request.getApproval();
		} else {
			disposition = request.getUnderwritingStatus();
		}
		return disposition;
	}

	/**
	 * The method creates a new TentativeDisp object with the disposition parameter passed to the method and adds the new object to the list
	 * @param undStatusReason 
	 * @param disposition disposition selected by user
	 * @param nbaUserVO NbaUserVO object
	 * @param appInfoExt ApplicationInfoExtension object that contains the list of tentative dispositions
	 * @param underwriterRole The underwriter's role
	 * @param underwriterRoleLevel The underwriter's level for the role
	 * @throws NbaBaseException
	 */
	protected void addTentativeDisposition(long undStatusReason, long disposition, NbaUserVO nbaUserVO, ApplicationInfoExtension appInfoExt,
			String underwriterRole, int underwriterRoleLevel) throws NbaBaseException {
		List tentativeDispList = appInfoExt.getTentativeDisp();
		TentativeDisp tentativeDisp = null;
		if (disposition != NbaConstants.LONG_NULL_VALUE) {
			tentativeDisp = createTentativeDisposition(disposition, undStatusReason, nbaUserVO, tentativeDispList, underwriterRole,
					underwriterRoleLevel);
			appInfoExt.addTentativeDisp(tentativeDisp);
		}
	}

	/**
	 * Call the business process to retrieve the results from the VPMS model
	 * @param request NbaContractApprovalDispositionRequest
	 * @param currentDecision
	 * @return NbaVpmsRequestVO
	 * @throws NbaBaseException
	 */
	protected NbaVpmsRequestVO retrieveDataFromBusinessRules(NbaContractApprovalDispositionRequest request, String currentDecision) throws NbaBaseException {
		Map deOink = new HashMap();
		//APSL4395
		if(NbaUtils.isTermConvOPAICase(request.getContract())){
			NbaVPMSHelper.deOinkTermConvData(deOink,request.getContract(), request.getWork().getNbaLob());
		}
		//APSL4395
		deOink.put("A_SecondLvlDecisionQueue", request.getSecondLevelDecisionQueue());
		deOink.put("A_LogonRole", request.getUnderwriterRole());
		deOink.put("A_UWRole", request.getNbaUserVO().getUwRole()); //AXAL3.7.09(R)
		deOink.put("A_UWQueue", request.getNbaUserVO().getUwQueue()); //NBLXA-2489
		deOink.put(NbaVpmsConstants.A_PROCESS_ID, NbaVpmsConstants.UISTATUS_UNDERWRITER_WORKBENCH);
		deOink.put("A_ContractChangeApproved", request.getContractChangeApprovalInd());
		if (currentDecision != null) {
			deOink.put("A_CurrentDecision", currentDecision);
		}
		NbaVpmsRequestVO vpmsRequestVO = new NbaVpmsRequestVO();
		vpmsRequestVO.setModelName(NbaVpmsConstants.AUTO_PROCESS_STATUS);
		vpmsRequestVO.setEntryPoint(NbaVpmsConstants.EP_WORKITEM_STATUSES);
		vpmsRequestVO.setNbATXLife(request.getContract());
		vpmsRequestVO.setNbaLob(request.getNbaDst().getNbaLob());
		vpmsRequestVO.setDeOinkMap(deOink);

		AccelResult result = (AccelResult) callBusinessService("RetrieveDataFromBusinessRulesBP", vpmsRequestVO);
		if (result.hasErrors()) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS);
		}
		return (NbaVpmsRequestVO) result.getFirst();
	}

	/**
	 * The method checks if the case has previous initial decisions
	 * @param appInfoExt the ApplicationInfoExtension object containing the initial decisions.
	 * @return boolean true if previous decisions are present
	 */
	protected boolean hasPreviousDecisions(ApplicationInfoExtension appInfoExt) {
		return appInfoExt.getInitialDecisionCount() > 0;
	}

	/**
	 * This method updates the tentative dispositions based on earlier decisions. First get all earlier decisions present, at level 2 and 3. If any of
	 * the earlier decisions is Agree with Changes, create a Tentative Dispositions for level 2 to the level at which "Agree with Changes" decision
	 * was found. The UndID for the new disposition will be same as the UndID for Initial decision.
	 * @param appInfoExt the ApplicationInfoExtension object containing the initial decisions.
	 * @param nbaUserVO NbaUserVO object
	 * @throws NbaBaseException
	 */
	protected void updateDispositionsForEarlierDecisions(ApplicationInfoExtension appInfoExt, NbaUserVO nbaUserVO) throws NbaBaseException {
		InitialDecision decision = null;
		TentativeDisp tentativeDisp = null;
		int count = appInfoExt.getInitialDecisionCount();
		//Assume that there is no initial decision with "Agree with changes" until one is found in Initial Decision list, so initialize this variable
		// with -1.
		int undLvlForAgreeWithChangesDec = -1;
		List tentativeDispList = appInfoExt.getTentativeDisp();
		for (int i = 0; i < count; i++) {
			decision = appInfoExt.getInitialDecisionAt(i);
			if (decision.getDecision() == NbaOliConstants.NBA_ADDITIONALDECISION_AGREEWITHCHANGE) {
				undLvlForAgreeWithChangesDec = decision.getDecisionLevel();
			}
		}
		//If an Initial decision of "Agree with changes" is found at level "n", we need to record tentative dispositions for 1 to n levels.
		decision = null;
		for (int j = 0; j < count; j++) {
			decision = appInfoExt.getInitialDecisionAt(j);
			if (decision.getDecisionLevel() <= undLvlForAgreeWithChangesDec) {
				//Create a tentaive disposition
				tentativeDisp = createTentativeDisposition(decision.getDecisionUndID(), decision.getDecisionLevel(), nbaUserVO, tentativeDispList,
						nbaUserVO.getUwRole(), decision.getUWRoleLevel()); //AXAL3.7.09(R)
				appInfoExt.addTentativeDisp(tentativeDisp);
			} else {
				break;
			}
		}
	}

	/**
	 * Creates an instance of tentative Dispsosition object
	 * @param userID The underwriter ID
	 * @param level disposition level
	 * @param nbaUserVO NbaUserVO object
	 * @param tentDispList the list of Tentative disposition objects
	 * @param underwriterRole The underwriter's role
	 * @param underwriterRoleLevel The underwriter's level for the role
	 * @return tentativeDisp Tentative Disposition object
	 * @throws NbaBaseException
	 */
	protected TentativeDisp createTentativeDisposition(String userID, int level, NbaUserVO nbaUserVO, List tentDispList, String underwriterRole,
			int underwriterRoleLevel) throws NbaBaseException {
		TentativeDisp tentDisplevelOne = null;
		long disposition = NbaConstants.LONG_NULL_VALUE;
		long reason = NbaConstants.LONG_NULL_VALUE;
		tentDisplevelOne = (TentativeDisp) tentDispList.get(0);

		if (tentDisplevelOne.getDispLevel() == NbaConstants.TENT_DISP_LEVEL_ONE) {
			disposition = tentDisplevelOne.getDisposition();
			reason = tentDisplevelOne.getDispReason();
		}

		TentativeDisp tentativeDisp = new TentativeDisp();
		tentativeDisp.setDisposition(disposition);
		tentativeDisp.setDispLevel(level);
		tentativeDisp.setDispUndID(userID);
		tentativeDisp.setDispDate(getCurrentDateFromWorkflow(nbaUserVO));
		tentativeDisp.setDispReason(reason);
		tentativeDisp.setUWRole(underwriterRole);
		tentativeDisp.setUWRoleLevel(underwriterRoleLevel);
		tentativeDisp.setActionAdd();
		return tentativeDisp;
	}

	
	
	/**
	 * This method applies the contract approval and final disposition to the host. 
	 * @param newBusinessUWAppRequest NewBusinessUWApproveRequest object
	 * @return result AccelResult object
	 * @throws NbaBaseException
	 */
    protected void applyUWApproveAndDisposition(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
    	//Begin ALS5017
//    	NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO(); Code commented for SR534655 Retrofit
//		retOpt.setWorkItem(request.getWork().getID(), true);
//		retOpt.setLockWorkItem();
//		retOpt.requestSources();
//		retOpt.requestTransactionAsChild(); 
//		//End ALS5017
//		NbaUnderwriterWorkbenchFacadeBean bean = new NbaUnderwriterWorkbenchFacadeBean();
//		bean.setNbaDstWithAllTransactions(retrieveWorkItem(request.getNbaUserVO(), retOpt)); //ALS5017
//		bean.applyApproveAndDisposition(request.getNbaUserVO(), request.getContract(), request.getWork(),
//				NbaVpmsAdaptor.UISTATUS_UNDERWRITER_WORKBENCH, request.getSecondLevelDecisionQueue(), request.isInformalApp());//ALS5017, ALS5344
	    //New Code Begin for SPR534655 Retrofit; This Code is moved from  NbaUnderwriterWorkbenchFacadeBean.applyApproveAndDisposition() to this method
    	//APSL2806 Moved calling method outside this method.
    	if(!NbaUtils.isNegativeDisposition(request.getWork())){
            NbaApproveContract nac = new NbaApproveContract(request.getNbaUserVO(), request.getContract(), request.getWork(), NbaVpmsAdaptor.UISTATUS_UNDERWRITER_WORKBENCH, request.getSecondLevelDecisionQueue());
            nac.checkReinsurance(request.getContract(),  request.getWork()); 
            nac.checkEndorsement(request.getContract());
		}
    	//New Code End for SPR534655
		//APSL613 begin
		if(NbaPrintLogger.getLogger().isDebugEnabled()){
			if(NbaUtils.isPrintAttachedToDst(request.getWork())){
				NbaPrintLogger.getLogger().logDebug("Print attached to DST after applyUWApproveAndDisposition()) - "+request.getWork().getNbaLob().getPolicyNumber());
			}
		}
		//APSL613 end
		//Begin ALII623 
		try{
			voidPreviousDeliveryInstructions(request); //ALS4978
		}catch ( Exception e ){
			throw new NbaBaseException(e); 
		}
		//Add Delivery Instructions
		if(!request.isInformalApp()){//ALS5432 only if not an informal application 
			addDeliveryInstructions(request); //QC2342
		}
		//end ALII623
	}
    
    /**
	 * Automated underwriting has Approval.  This work item will be equitably distributed
	 * among the returned Approval statuses.  If only one Approval status is allowed, that status
	 * will be used.
	 * @return
	 */
	//ALCP19AA New Method
	protected String getEquitableApprovalStatus(NbaContractApprovalDispositionRequest request, String approvalStatus) throws NbaBaseException {
		List approvalStatuses = getApprovalStatuses(approvalStatus);
		if (approvalStatuses.size() == 1) {
			return (String) approvalStatuses.get(0);
		} else if (approvalStatuses.size() == 0) {
		    throw new NbaBaseException("Unable to determine a valid Approval status.", NbaExceptionType.FATAL);
		}
		return determineEquitableStatus(request, approvalStatuses);
	}
    
    /**
	 * Parses the Approval status string returned from the AutoProcessStatus model and
	 * converts it into a usable list.
	 * @return
	 */
	//ALCP19AA New Method
	protected List getApprovalStatuses(String approvalStatus) {
		ArrayList statuses = new ArrayList();
		if (approvalStatus != null) {
			NbaStringTokenizer st = new NbaStringTokenizer(approvalStatus, ",");
			while (st.hasMoreTokens()) {
			    statuses.add(st.nextToken());
			}
		}
		return statuses;
	}
    
    /**
	 * Determines the equitable status from a list of possible statuses.  The work items
	 * for each underwriter associated with the status are counted and the status with
	 * the fewest amount of work, will be assigned.  If some or all queues have no work
	 * items, the first status with no work will be returned.
	 * @param statuses
	 * @return
	 */
	//ALCP19AA New Method
	protected String determineEquitableStatus(NbaContractApprovalDispositionRequest request, List statuses) throws NbaBaseException {
	    String equitableStatus = null;
	    String status = null;
	    int workCount = 0;
	    int equitableCount = 0;
		int count = statuses.size();
		NbaWorkflowDistribution distribution = new NbaWorkflowDistribution(request.getWork().getNbaLob());
		for (int i = 0; i < count; i++) {
		    status = (String)statuses.get(i);
		    workCount = distribution.getWorkAssignedCount(status);
		    if (i == 0 || workCount < equitableCount) {
		        equitableCount = workCount;
		        equitableStatus = status;
		    }
		}
		return equitableStatus;
	}
	
	/**
	 * This method adds Delivery Istructions onto the case
	 * @param request
	 * @throws NbaBaseException
	 * 
	 */
	//QC2342 (ALS3677) New Method 
	protected void addDeliveryInstructions(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null; //ALS5009
		try {
			Map deOink = new HashMap();
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
			oinkData.setLobSource(request.getWork().getNbaLob());
			oinkData.setContractSource(request.getNbaTXLife());
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.AXAENDORSEMENTS);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_AUTO_DELV_INST);
			
			List reqInfoList = request.getNbaTXLife().getPolicy().getRequirementInfo();
			int count = reqInfoList.size();
			deOink.put("A_RequirementInfoCount", Long.toString(count));
			if (count == 0) {
				deOink.put("A_ReqCodeList", "");
			} else {
				for (int i = 0; i < count; i++) {
					if (i == 0) {
						deOink.put("A_ReqCodeList", Long.toString(((RequirementInfo) reqInfoList.get(i)).getReqCode()));
						deOink.put("A_ReqStatus", Long.toString(((RequirementInfo) reqInfoList.get(i)).getReqStatus()));
					} else {
						deOink.put("A_ReqCodeList[" + i + "]", Long.toString(((RequirementInfo) reqInfoList.get(i)).getReqCode()));
						deOink.put("A_ReqStatus[" + i + "]", Long.toString(((RequirementInfo) reqInfoList.get(i)).getReqStatus()));
					}
				}
			}
			
			ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(request.getNbaTXLife().getPolicy().getApplicationInfo());
			int uwResCounter = 0;
			if (appInfoExtn != null) {
				int underwritingResultCount = appInfoExtn.getUnderwritingResultCount();
				if (underwritingResultCount > 0) {
					for (; uwResCounter < underwritingResultCount; uwResCounter++) { 
						UnderwritingResult underwritingResult = appInfoExtn.getUnderwritingResultAt(uwResCounter);
						UnderwritingResultExtension underwritingResultExtn = NbaUtils.getFirstUnderwritingResultExtension(underwritingResult);
						if (underwritingResultExtn != null) {
							if (uwResCounter == 0) {
								deOink.put("A_UnderwritingReasonType", Long.toString(underwritingResultExtn.getUnderwritingReasonType()));
							} else {
								deOink.put("A_UnderwritingReasonType[" + uwResCounter + "]", Long.toString(underwritingResultExtn.getUnderwritingReasonType()));
							}
						}
						if (uwResCounter == 0) {
							deOink.put("A_UnderwritingResultReason", Long.toString(underwritingResult.getUnderwritingResultReason()));
						} else {
							deOink.put("A_UnderwritingResultReason[" + uwResCounter + "]", Long.toString(underwritingResult.getUnderwritingResultReason()));
						}
					}
				}
			}
			
			int substandardRatingCount = 0;
			for (int i = 0; i < request.getNbaTXLife().getLife().getCoverageCount(); i++) {
				Coverage coverage = request.getNbaTXLife().getLife().getCoverageAt(i);
				for (int j = 0; j < coverage.getLifeParticipantCount(); j++) {
					LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(j);
					substandardRatingCount += lifeParticipant.getSubstandardRatingCount();
					for (int k = 0; k < lifeParticipant.getSubstandardRatingCount(); k++) {
						SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(k);
						SubstandardRatingExtension substandardRatingExtn = NbaUtils.getFirstSubstandardExtension(substandardRating);
						boolean isProposedRate = false;
						if(substandardRatingExtn.hasProposedInd()){
							if(substandardRatingExtn.getProposedInd()){
								isProposedRate = true;
							}else{
								isProposedRate = false;
							}							
						}
						//End APSL3948
						//ALS4361 begin
						
						if(k == 0) {
							//Begin APSL4230
							if (substandardRatingExtn.hasPermFlatExtraAmt()) {
								deOink.put("A_PermFlatExtraAmount", convertToDefault(String.valueOf(substandardRatingExtn.getPermFlatExtraAmt()))); // ALII1787
							}
						    //End APSL4230
							if (!substandardRating.isActionDelete() && !isProposedRate) { //Begin ALII1732, APSL3948
								deOink.put("A_TableRating", convertToDefault(String.valueOf(substandardRating.getPermTableRating()))); 
							} else {
								deOink.put("A_TableRating","-1");
							} // End ALII1732
						} else {
							//Begin APSL4230
							if (substandardRatingExtn.hasPermFlatExtraAmt()) {
								deOink.put("A_PermFlatExtraAmount[" + k + "]", convertToDefault(String.valueOf(substandardRatingExtn
										.getPermFlatExtraAmt()))); // ALII1787
							}
							//End APSL4230
							if (!substandardRating.isActionDelete() && !isProposedRate) { // Begin ALII1732,APSL3948
								deOink.put("A_TableRating[" + k + "]", convertToDefault(String.valueOf(substandardRating.getPermTableRating()))); 
							} else {
								deOink.put("A_TableRating", "-1");
							} // End ALII1732
						}
						//ALS4361 end
						for (int l = 0; l < substandardRatingExtn.getUnderwritingResultCount(); l++) {
							UnderwritingResult underwritingResult = substandardRatingExtn.getUnderwritingResultAt(l);
							UnderwritingResultExtension underwritingResultExtn = NbaUtils.getFirstUnderwritingResultExtension(underwritingResult);
							int index = uwResCounter;
							if (underwritingResultExtn != null) {
								if (uwResCounter == 0) {
									deOink.put("A_UnderwritingReasonType", Long.toString(underwritingResultExtn.getUnderwritingReasonType()));
								} else {
									deOink.put("A_UnderwritingReasonType[" + index + "]", Long.toString(underwritingResultExtn.getUnderwritingReasonType()));
								}
							}
							if (uwResCounter == 0) {
								deOink.put("A_UnderwritingResultReason", Long.toString(underwritingResult.getUnderwritingResultReason()));
							} else {
								deOink.put("A_UnderwritingResultReason[" + index + "]", Long.toString(underwritingResult.getUnderwritingResultReason()));
							}
							++uwResCounter;
						}
					}
				}
			}
			if (uwResCounter == 0) {
				deOink.put("A_UnderwritingReasonType", "");
				deOink.put("A_UnderwritingResultReason", "");
			}
			deOink.put("A_UnderwritingReasonTypeCount", Long.toString(uwResCounter));
			deOink.put("A_SubstandardRatingCount", Long.toString(substandardRatingCount));
			
			ArrayList endorsementList = request.getNbaTXLife().getPolicy().getEndorsement();
			int endorsementCount = endorsementList.size();
			deOink.put("A_EndorsementCount", Long.toString(endorsementCount));
			if (endorsementCount == 0) {
				deOink.put("A_EndorsementCodeContent", "");
			} else {
				for (int i = 0; i < endorsementList.size(); i++) {
					Endorsement endorsement = (Endorsement) endorsementList.get(i);
					if (endorsement != null) {
						EndorsementExtension endorsementExtension = NbaUtils.getFirstEndorseExtension(endorsement);
						if (endorsementExtension != null) {
							if (i == 0) {
								deOink.put("A_EndorsementCodeContent", endorsementExtension.getEndorsementCodeContent());
							} else {
								deOink.put("A_EndorsementCodeContent[" + i + "]", endorsementExtension.getEndorsementCodeContent());
							}
						}
					}
				}
			}
			vpmsProxy.setSkipAttributesMap(deOink);
			deOink.put("A_UWRatingResultReasonsText", retrieveUWRatingResultReasonsText(request));
			
			//ALS4978 Begin - code for new delivery instruction - DPW
			boolean ratedDpwPresent = false;
			Life life = request.getNbaTXLife().getLife(); 
			int covCount = life.getCoverageCount();
			for (int j = 0; j < covCount; j++) {
				Coverage coverage = life.getCoverageAt(j);
				List covOptCount = coverage.getCovOption();
				for (int k = 0; k < covOptCount.size(); k++) {
					CovOption covOption = coverage.getCovOptionAt(k);
					if (covOption.getLifeCovOptTypeCode() == NbaOliConstants.OLI_COVTYPE_DREADDISEASE) {
						if (!NbaUtils.isDeleted(covOption) && NbaOliConstants.OLI_POLSTAT_DECISSUE != covOption.getCovOptionStatus()) {
							List ratingList = covOption.getSubstandardRating();
							for ( int l=0; l<covOption.getSubstandardRatingCount(); l++){
								SubstandardRating rating = (SubstandardRating ) ratingList.get(l);
								if ( rating.hasPermTableRating()) {
									ratedDpwPresent = true;
									break;
								}
							}
						} 
					}
				}
			}
			if ( ratedDpwPresent){
				deOink.put("A_RatedDPWPresent", "true");
			}else{
				deOink.put("A_RatedDPWPresent", "false");
			}
			//ALS4978 End 			
			NbaVpmsResultsData result = new NbaVpmsResultsData(vpmsProxy.getResults());
			List instructions = result.getResultsData();
			for (int i = 0; i < instructions.size(); i++) {
				String instruction = (String) instructions.get(i);
				if (! NbaUtils.isBlankOrNull(instruction)) {
					NbaSpecialInstructionComment nsic = new NbaSpecialInstructionComment();
		        	nsic.setActionAdd();
		        	nsic.setOriginator(NbaConstants.AUTOMATED_DELIVERY_INST_USER);  //ALS4978
		        	nsic.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
		        	nsic.setText(instruction);
		        	nsic.setUserNameEntered(NbaConstants.AUTOMATED_DELIVERY_INST_USER); //ALS5163  //ALS4978
		        	nsic.setVoidInd(NbaConstants.FALSE);   //ALS5163
		        	nsic.setInstructionType(NbaConstants.INSTRUCTION_TYPE);
		        	NbaUtils.addAttachmentForSpecialInstruction(request.getNbaTXLife().getPrimaryHolding(),nsic);//ALS4542
		        	//request.getWork().addManualComment(nsic.convertToManualComment());
				}
			}
		} catch (RemoteException re) {
			throw new NbaVpmsException("CommitFinalDispositionBP" + NbaVpmsException.VPMS_EXCEPTION, re);
		//begin ALS5009
		} finally {
			try {
                if (vpmsProxy != null) {
                	vpmsProxy.remove();
                }
            } catch (Throwable th) {
                //do nothing
            }
		}
		//end ALS5009
	}
	
	/**
	 * Converts deOink String values to defaults, if null Or Nan
	 * @param String: input String
	 * @return String: converted String
	 */
	//ALS4361 new method
	protected String convertToDefault(String str) {
		if (str == null || str.equalsIgnoreCase("null")) {
			return "";
		}
		else if (str.equalsIgnoreCase("NaN")) {
			return "-1";
		}
		return str;
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 * @throws NbaBaseException
	 */
	//QC2342 New Method
	public String retrieveUWRatingResultReasonsText(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
		String reasonsText = "";
		String reason=""; //ALS5401
		NbaTableAccessor nta = new NbaTableAccessor();
		HashMap caseData = new HashMap();
		caseData.put("backendSystem", request.getNbaDst().getNbaLob().getBackendSystem());
		caseData.put("plan", request.getNbaDst().getNbaLob().getPlan());
		caseData.put("company", request.getNbaDst().getNbaLob().getCompany());
		//Begin ALS4517
		Life life = request.getNbaTXLife().getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
		for (int i = 0; i < life.getCoverageCount(); i++) {
			Coverage coverage = life.getCoverageAt(i);
			for (int j = 0; j < coverage.getLifeParticipantCount(); j++) {
				LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(j);
				for (int k = 0; k < lifeParticipant.getSubstandardRatingCount(); k++) {
					SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(k);
					SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
					for (int l = 0; l < substandardRatingExtension.getUnderwritingResultCount(); l++) {
						UnderwritingResult underwritingResult = substandardRatingExtension.getUnderwritingResultAt(l);
						UnderwritingResultExtension underwritingResultExtn = NbaUtils.getFirstUnderwritingResultExtension(underwritingResult);
						if (underwritingResultExtn != null
								&& (underwritingResultExtn.getUnderwritingReasonType() == NbaOliConstants.OLI_UWREASON_EXT_RATING ||
										underwritingResultExtn.getUnderwritingReasonType() == NbaOliConstants.OLI_UWREASON_1009800001)) {//ALS5422
							//Begin ALS5401
							if (underwritingResult.getUnderwritingResultReason() != -1) {
								NbaTableData data[] = nta.getAUDLetterText(caseData, String.valueOf(underwritingResultExtn.getUnderwritingReasonType()), String
										.valueOf(underwritingResult.getUnderwritingResultReason()));//ALS5519
								if ( data.length > 0){ //ALS5511
									reason = ((NbaReasonsData) data[0]).getIndexTranslation();
								}
							} else {
								reason = underwritingResult.getDescription();
							}
							if (!reasonsText.equals(""))//ALS4838
								reasonsText = reasonsText + ", " + reason;
							else
								reasonsText = reason;
							/*
							 * reasonsText += ((NbaReasonsData) data[0]).getIndexTranslation(); if (l <
							 * substandardRatingExtension.getUnderwritingResultCount() - 1) { reasonsText += ", "; }
							 */
						}//End ALS5401

					}
				}
			}
		}
		//End ALS4517
		return reasonsText;
	}
	
	/**
	 * This method voids the previously added automated delivery istructions 
	 * @param request
	 * @throws Exception
	 * 
	 */
	//ALS4978 New Method 
	protected void voidPreviousDeliveryInstructions(NbaContractApprovalDispositionRequest request) throws Exception {
		NbaTXLife txLife = request.getNbaTXLife();
		ArrayList attList = txLife.getPrimaryHolding().getAttachment();
		Attachment attach = null;
		if ( attList !=null ){
			for ( int i = 0; i< attList.size(); i ++){
				attach = (Attachment) attList.get(i);
				if ( NbaOliConstants.OLI_ATTACH_INSTRUCTION == attach.getAttachmentType() && 
						NbaConstants.AUTOMATED_DELIVERY_INST_USER.equalsIgnoreCase(attach.getUserCode())){
					AttachmentData attchData = attach.getAttachmentData();
					SpecialInstruction spclInst = SpecialInstruction.unmarshal(new ByteArrayInputStream(attchData.getPCDATA().getBytes()));
					spclInst.setVoidInd(String.valueOf(NbaConstants.TRUE));
					spclInst.setUserNameVoided(NbaConstants.AUTOMATED_DELIVERY_INST_USER);
					spclInst.setDateVoided(NbaUtils.getStringFromDate(new java.sql.Date(System.currentTimeMillis())));
					spclInst.setUserVoided(NbaConstants.AUTOMATED_DELIVERY_INST_USER);
					attchData.setPCDATA(NbaUtils.commentDataToXmlString(spclInst)); 
					attchData.setActionUpdate();
					attach.setActionUpdate();
				}
			}
		}
	}
	
	
	/**
	 * Retrieves all the transactions for the case
	 * @param nbaUserVO NbaUserVO object
	 * @param retOpt NbaAwdRetrieveOptionsVO object
	 * @return NbaDst containing all the transaction for the case
	 * @throws NbaBaseException
	 */
	//ALS5017 New Method
	protected NbaDst retrieveWorkItem(NbaUserVO nbaUserVO, NbaAwdRetrieveOptionsVO retOpt) throws NbaBaseException {
		retOpt.setNbaUserVO(nbaUserVO);
		AccelResult accelResult = (AccelResult) callBusinessService("NbaRetrieveWorkBP", retOpt);
		processResult(accelResult);
		return (NbaDst) accelResult.getFirst();
	}
	 //ALS5697 New Method
	protected void resetAssignFinalDisp(NbaTXLife updatedTXLife, NbaTXLife origTXLife) {
		if (updatedTXLife != null && origTXLife != null) {
			updatedTXLife.getPolicy().setApplicationInfo(origTXLife.getPolicy().getApplicationInfo());
		}
	}
	
	//BEGIN: APSL5128
	protected Activity createActivity(String contractChangeInfoId, String activityKey) {
		Activity activity = new Activity();
		activity.setActivityTypeCode(NbaOliConstants.OLI_ACTTYPE_CONTRACTCHANGE);
		activity.setStartTime(new NbaTime(new Date()));
		activity.setEndTime(new NbaTime(new Date()));
		activity.setActivityKey(activityKey);
		ActivityExtension activityExtn = NbaUtils.createActivityExtension(activity);
		activityExtn.setRelatedObjectId(contractChangeInfoId);
		activityExtn.setActionAdd();
		activity.setActionAdd();
		return activity;
	}
    //END: APSL5128
	  //New Method NBLXA-1432
	private void updateLastReqIndicator(NbaTXLife aNbaTXLife, NbaDst nbadst, NbaUserVO userVO) throws NbaBaseException {
		if (nbadst.getNbaLob().getSigReqRecd()) {
			nbadst.getNbaLob().setSigReqRecd(false);// updating last requirement received LOB
			nbadst.getNbaLob().setLstNonRevReqRec(false);
			NbaUtils.addAutomatedComment(nbadst, userVO, "Last requirement indicator has been turned off due to Final action."); //NBLXA-1718 changed from general to automated
		} else {
			nbadst.getNbaLob().setSigReqRecd(false);// updating last requirement received LOB
			nbadst.getNbaLob().setLstNonRevReqRec(false);
		}
		Policy policy = aNbaTXLife.getPolicy();
		ApplicationInfo appInfo = policy.getApplicationInfo();
		ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (appInfoExtn != null) {
			appInfoExtn.setLastRequirementInd(NbaOliConstants.OLI_LU_LASTREQSTAT_COMPLETE); // updating last requirement received composite indicator
			appInfoExtn.setActionUpdate();
		}
		int partyCount = aNbaTXLife.getOLifE().getPartyCount();
		for (int k = 0; k < partyCount; k++) {
			Party party = aNbaTXLife.getOLifE().getPartyAt(k);
			if (party != null) {
				PartyExtension partyExt = NbaUtils.getFirstPartyExtension(party);
				if (partyExt != null) {
					partyExt.setLastRequirementIndForParty(NbaOliConstants.OLI_LU_LASTREQSTAT_COMPLETE);// updating last requirement received
					partyExt.setActionUpdate(); // indicator on party level
				}

			}
		}

	}

	//Start NBLXA-1542 new method
	public static void addFirstApprovalActivity(NbaTXLife nbaTXLife, String userRole) {
		Activity newActivity = new Activity();
		NbaOLifEId olifeId = new NbaOLifEId(nbaTXLife);
		olifeId.setId(newActivity);
		newActivity.setDoneDate(new Date());
		newActivity.setStartTime(new NbaTime(new java.sql.Timestamp(System.currentTimeMillis())));
		newActivity.setUserCode(userRole);
		newActivity.setActivityStatus(NbaOliConstants.OLI_ACTSTAT_COMPLETE);
		newActivity.setActivityTypeCode(NbaOliConstants.OLI_ACTTYPE_1009900003);
		newActivity.setActionAdd();		
		nbaTXLife.getOLifE().setActionUpdate();
		nbaTXLife.getOLifE().addActivity(newActivity);

	}

	// NBLXA-2223 Starts
	protected boolean isIUPManualOverridden(NbaContractApprovalDispositionRequest request) {
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(request.getContract().getNbaHolding().getApplicationInfo());

		if (appInfoExt != null) {
			return (appInfoExt.getInternationalUWProgInd() != request.isIupIndicator());
		}
		return false;
	}
	// NBLXA-2223 Ends
	
	
	
}

