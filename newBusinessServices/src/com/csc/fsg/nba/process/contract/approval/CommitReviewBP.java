package com.csc.fsg.nba.process.contract.approval;

/* 
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaContractApprovalDispositionRequest;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaVpmsRequestVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.InitialDecision;
import com.csc.fsg.nba.vo.txlife.PredictiveResult;
import com.csc.fsg.nba.vo.txlife.TentativeDisp;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/**
 * This business process is responsible for committing the review complete initial decision on the contract. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>SPR1362</td><td>Version 8</td><td>Values used for VPMS models (entry points, attributes) should be constant values.</td></tr>
 * <tr><td>AXAL3.7.09(R)</td><td>AXA Life Phase 1</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>SR657319</td><td>Discretionary</td><td>Manual selection of Rate Class</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class CommitReviewBP extends CommitFinalDispositionBP {


	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaContractApprovalDispositionRequest request = (NbaContractApprovalDispositionRequest) input;
			result = applyReviewCompleteDecision(request);
		} catch (NbaTransactionValidationException e) {//ALII1304
			addMessage(result, e.getMessage());
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}

	/**
	 * This method applies the review complete decision and updates the pending contract database with the modified nbaTXLife and the AWD with modified
	 * nbaDst.
	 * @param request NbaContractApprovalDispositionRequest
	 * @return AccelResult
	 * @throws NbaBaseException
	 */
	protected AccelResult applyReviewCompleteDecision(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
		request.getContract().setBusinessProcess(NbaConstants.PROC_UW_DISPOSITION);
		ApplicationInfo appInfo = request.getContract().getNbaHolding().getPolicy().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getAppInfoExtension(appInfo);
		AccelResult result = null;
		//Agree button is clicked
		if (NbaConstants.AGREE.equals(request.getReviewCompleteDecision())) {
			result = applyAgree(request, appInfoExt);
			//Agree with changes button is clicked
		} else if (NbaConstants.AGREE_WITH_CHANGE.equals(request.getReviewCompleteDecision())) {
			result = applyAgreeWithChanges(request, appInfoExt);
			//Disagree button is clicked
		} else if (NbaConstants.DISAGREE.equals(request.getReviewCompleteDecision())) {
			result = applyDisagree(request, appInfoExt);
		}
		return result;
	}

	/**
	 * This method applies the Agree decision
	 * @param request NbaContractApprovalDispositionRequest
	 * @param appInfoExt ApplicationInfoExtension object
	 * @return AccelResult
	 * @throws NbaBaseException
	 */
	protected AccelResult applyAgree(NbaContractApprovalDispositionRequest request, ApplicationInfoExtension appInfoExt) throws NbaBaseException {
		List tentativeDispList = appInfoExt.getTentativeDisp();
		String currentDecision = "";
		if (!hasPreviousAllAgreedOrNoDecisions(appInfoExt.getInitialDecision())){
			currentDecision = String.valueOf(NbaOliConstants.NBA_ADDITIONALDECISION_AGREE);
		}
		updateInitialDecisions(NbaOliConstants.NBA_ADDITIONALDECISION_AGREE, appInfoExt, request.getNbaUserVO(), request.getUnderwriterRole(),
				request.getUnderwriterRoleLevel());
		TentativeDisp currentTentativeDisposition = createTentativeDisposition(NbaConstants.LONG_NULL_VALUE, NbaConstants.LONG_NULL_VALUE, request
				.getNbaUserVO(), tentativeDispList, request.getUnderwriterRole(),request.getUnderwriterRoleLevel());
		appInfoExt.addTentativeDisp(currentTentativeDisposition);
		NbaVpmsRequestVO vpmsRequestVO = retrieveBusinessRulesDataForUserLevel(request, currentDecision);
		updateFinalAuthorityFields(vpmsRequestVO.getRule(), currentTentativeDisposition, request, appInfoExt);
		agreePredictiveUWDecision(request); //ALII1793
		if (vpmsRequestVO.getRule() == NbaConstants.FINAL_AUTHORITY) {
			applyUWApproveAndDisposition(request);
		}
		return updateContract(request, vpmsRequestVO);
	}

	/**
	 * This method applies the Agree With Changes decision
	 * @param request NbaContractApprovalDispositionRequest
	 * @param appInfoExt ApplicationInfoExtension object
	 * @return AccelResult
	 * @throws NbaBaseException
	 */
	protected AccelResult applyAgreeWithChanges(NbaContractApprovalDispositionRequest request,
			ApplicationInfoExtension appInfoExt) throws NbaBaseException {
		String currentDecision = "";
		if (!hasPreviousAllAgreedOrNoDecisions(appInfoExt.getInitialDecision())){
			currentDecision = String.valueOf(NbaOliConstants.NBA_ADDITIONALDECISION_AGREEWITHCHANGE);
		}
		updateInitialDecisions(NbaOliConstants.NBA_ADDITIONALDECISION_AGREEWITHCHANGE, appInfoExt, request.getNbaUserVO(), request.getUnderwriterRole(),
				request.getUnderwriterRoleLevel());
		NbaVpmsRequestVO vpmsRequestVO = retrieveBusinessRulesDataForUserLevel(request, currentDecision);
		agreePredictiveUWDecision(request); //ALII1793
		return updateContract(request, vpmsRequestVO);
	}

	/**
	 * This method applies the Disagree decision
	 * @param request NbaContractApprovalDispositionRequest
	 * @param appInfoExt ApplicationInfoExtension object
	 * @return AccelResult
	 * @throws NbaBaseException
	 */
	protected AccelResult applyDisagree(NbaContractApprovalDispositionRequest request, ApplicationInfoExtension appInfoExt)
			throws NbaBaseException {
		String currentDecision = "";
		if (!hasPreviousAllAgreedOrNoDecisions(appInfoExt.getInitialDecision())) {
			currentDecision = String.valueOf(NbaOliConstants.NBA_ADDITIONALDECISION_DISAGREE);
		}
		updateInitialDecisions(NbaOliConstants.NBA_ADDITIONALDECISION_DISAGREE, appInfoExt, request.getNbaUserVO(), request.getNbaUserVO().getUwRole(),
				request.getUnderwriterRoleLevel()); //AXAL3.7.09(R)
		NbaUtils.resetRateClass(request.getContract()); // SR657319
		NbaVpmsRequestVO vpmsRequestVO = retrieveBusinessRulesDataForUserLevel(request, currentDecision);
		return updateContract(request, vpmsRequestVO);
	}

	/**
	 * This method updates the initial decision. It checks if all previous decisions are "Agree" or are not set, it creates a new initial decision
	 * object
	 * @param decision long the decision to be set
	 * @param appInfoExt ApplicationInfoExtension object that contains the list of Initial decisions
	 * @param nbaUserVO NbaUserVO object
	 * @param underwriterRole The underwriter's role
	 * @param underwriterRoleLevel The underwriter's level for the role
	 * @throws NbaBaseException
	 */
	protected void updateInitialDecisions(long decision, ApplicationInfoExtension appInfoExt, NbaUserVO nbaUserVO, String underwriterRole,
			int underwriterRoleLevel) throws NbaBaseException {
		List initDecList = appInfoExt.getInitialDecision();
		if (hasPreviousAllAgreedOrNoDecisions(initDecList)) {
			InitialDecision initDec = createInitialDecision(decision, initDecList, nbaUserVO, underwriterRole, underwriterRoleLevel);
			appInfoExt.addInitialDecision(initDec);
		}
	}

	/**
	 * The method verifies if the user has the final authority for approval or negative disposition (based on value of rule returned from the vpms
	 * model, appropriate changes in holding inquiry and dst are performed, it creates a new initial decision object
	 * @param rule int represents the final authority
	 * @param currentDisp TentativeDisp object containing the decision to be set
	 * @param request NbaContractApprovalDispositionRequest
	 * @param appInfoExt ApplicationInfoExtension object that contains the list of Initial decision
	 */
	protected void updateFinalAuthorityFields(int rule, TentativeDisp currentDisp, NbaContractApprovalDispositionRequest request,
			ApplicationInfoExtension appInfoExt) {
		NbaLob lob = request.getWork().getNbaLob();
		if (rule == NbaConstants.FINAL_AUTHORITY && currentDisp.getDisposition() == NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED) {
			String originalUndId = null;
			List tentDispList = appInfoExt.getTentativeDisp();
			if (tentDispList.size() > 0) {
				TentativeDisp tentDisplevelOne = (TentativeDisp) tentDispList.get(0);
				if (tentDisplevelOne.getDispLevel() == NbaConstants.TENT_DISP_LEVEL_ONE) {
					originalUndId = tentDisplevelOne.getDispUndID();
				}
			}
			appInfoExt.setApprovalUnderwriterId(originalUndId);
			ApplicationInfo appInfo = request.getContract().getNbaHolding().getPolicy().getApplicationInfo();
			appInfoExt.setUnderwritingApproval(NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED);
			appInfo.setHOCompletionDate(new Date());
			appInfoExt.setActionUpdate();
			lob.setIssueDate(appInfo.getRequestedPolDate());
			request.getContract().setBusinessProcess(NbaConstants.PROC_UW_APPROVE_CONTRACT);
		} else if (rule == NbaConstants.FINAL_AUTHORITY && currentDisp.getDisposition() != NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED) {
			lob.setCaseFinalDispstn((int) currentDisp.getDisposition());
			lob.setFinalDispReason((int) currentDisp.getDispReason());
			appInfoExt.setUnderwritingStatus((int) currentDisp.getDisposition());
			appInfoExt.setUnderwritingStatusReason((int) currentDisp.getDispReason());
			appInfoExt.setActionUpdate();
		}
	}

	/**
	 * Call the business process to retrieve data from VP/MS model for the user level
	 * @param request NbaContractApprovalDispositionRequest
	 * @param currentDecision int decision currently selected by the underwriter
	 * @throws NbaBaseException
	 */
	protected NbaVpmsRequestVO retrieveBusinessRulesDataForUserLevel(NbaContractApprovalDispositionRequest request, String currentDecision)
			throws NbaBaseException {
		Map deOink = new HashMap();
		deOink.put("A_LogonRole", request.getUnderwriterRole()); //SPR1362
		deOink.put("A_UWRole", request.getNbaUserVO().getUwRole()); //AXAL3.7.09(R)
		deOink.put("A_UWQueue", request.getNbaUserVO().getUwQueue()); //NBLXA-2489
		deOink.put("A_CurrentDecision", currentDecision); //SPR1362
		deOink.put(NbaVpmsConstants.A_PROCESS_ID, NbaVpmsConstants.UISTATUS_UNDERWRITER_WORKBENCH);

		NbaVpmsRequestVO vpmsRequestVO = new NbaVpmsRequestVO();
		vpmsRequestVO.setModelName(NbaVpmsConstants.AUTO_PROCESS_STATUS);
		vpmsRequestVO.setEntryPoint(NbaVpmsConstants.EP_WORKITEM_STATUSES);
		vpmsRequestVO.setNbATXLife(request.getContract());
		vpmsRequestVO.setNbaLob(request.getWork().getNbaLob());
		vpmsRequestVO.setDeOinkMap(deOink);

		AccelResult result = (AccelResult) callBusinessService("RetrieveDataFromBusinessRulesBP", vpmsRequestVO);
		if (result.hasErrors()) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS);
		}
		return (NbaVpmsRequestVO) result.getFirst();
	}

	/**
	 * The method creates a new instance of the InitialDecision object
	 * @param decision long value of decision to be set in the new object
	 * @param initDecList the list of initial decision objects
	 * @param nbaUserVO NbaUserVO object
	 * @param underwriterRole The underwriter's role
	 * @param underwriterRoleLevel The underwriter's level for the role
	 * @return initDec a new InitialDecision object
	 * @throws NbaBaseException
	 */
	protected InitialDecision createInitialDecision(long decision, List initDecList, NbaUserVO nbaUserVO, String underwriterRole, int underwriterRoleLevel) throws NbaBaseException {
		InitialDecision initDec = new InitialDecision();
		initDec.setDecision(decision);
		initDec.setDecisionLevel(getHighestDecisionLevel(initDecList) + 1);
		initDec.setDecisionUndID(nbaUserVO.getUserID());
		initDec.setDecisionDate(getCurrentDateFromWorkflow(nbaUserVO));
		initDec.setUWRole(underwriterRole);
		initDec.setUWRoleLevel(underwriterRoleLevel);
		initDec.setActionAdd();
		return initDec;
	}

	/**
	 * The method returns the hightest level of decision among all the InitialDecision objects
	 * @param initDecList the list of initial decision objects
	 * @return decision int the highest level of decision
	 */
	protected int getHighestDecisionLevel(List initDecList) {
		int decisionLevel = 0;
		int initDecSize = initDecList.size();
		InitialDecision initDecision = null;
		for (int j = 0; j < initDecSize; j++) {
			initDecision = (InitialDecision) initDecList.get(j);
			if (initDecision.getDecisionLevel() > decisionLevel) {
				decisionLevel = initDecision.getDecisionLevel();
			}
		}
		return decisionLevel > 0 ? decisionLevel : NbaConstants.TENT_DISP_LEVEL_ONE;
	}

	/**
	 * The method checks if all previous decisions were agreed to the tentative dispsoition
	 * @param initDecList the list of initial decision objects
	 * @return boolean if previous decisions are all agree or there are no decisions
	 */
	protected boolean hasPreviousAllAgreedOrNoDecisions(List initDecList) {
		int initDecSize = initDecList.size();
		InitialDecision initDecision = null;
		for (int j = 0; j < initDecSize; j++) {
			initDecision = (InitialDecision) initDecList.get(j);
			if (initDecision.getDecision() != NbaOliConstants.NBA_ADDITIONALDECISION_AGREE) {
				return false;
			}
		}
		return true;
	}
	//ALII1793 New Method
	protected void agreePredictiveUWDecision(NbaContractApprovalDispositionRequest request) {
		Policy policy = request.getContract().getNbaHolding().getPolicy();
		PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(policy);
		if (!NbaUtils.isBlankOrNull(polExt) && !NbaUtils.isBlankOrNull(polExt.getPredictiveResult()) && polExt.getPredictiveResult().size() > 0) {
			List predictList = polExt.getPredictiveResult();
			PredictiveResult lastResult = (PredictiveResult) predictList.get(predictList.size() - 1);
			lastResult.setUWDecision(NbaOliConstants.OLIX_PAUWDECISON_AGREE);
			lastResult.setActionUpdate();
		}
	}
}
