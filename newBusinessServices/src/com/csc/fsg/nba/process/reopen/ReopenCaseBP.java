package com.csc.fsg.nba.process.reopen;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axa.fsg.nba.foundation.AxaConstants;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.database.NbaAutoClosureAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaReopenCaseRequest;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;

/**
 * Reopens a contract that has been negatively disposed.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3253</td><td>Version 8</td><td>Reopen Business Function Needs to Preserve Underwriter Approval</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>NBA230</td><td>Version 8</td><td>nbA Reopen Date Enhancement Project</td></tr>
 * <tr><td>ALS4431</td><td>AXA Life Phase 1</td><td>QC # 3068 - nbA186: Unable to log in as UW Mgr for cosign</td></tr>
 * <tr><td>SR657319</td><td>Discretionary</td><td>Manual selection of Rate Class</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class ReopenCaseBP extends NewBusinessAccelBP {
	// contract validation business process id
	public static final String NBA_PROC_REOPEN = "REOPEN";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaReopenCaseRequest req = (NbaReopenCaseRequest) input;
			result.addResult(reopenCase(req.getNbaUserVO(), req.getNbaDst(), req.getTxlife()));
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
		}
		return result;
	}

	/**
	 * Resets the DISP and DSPR workflow LOBs and the underwriting status, approval, and status reason on the contract. The changed contract is then
	 * updated in the backend system followed by updating the workflow changes.
	 * 
	 * @param user
	 * @param work
	 * @param contract
	 * @return
	 * @throws NbaBaseException
	 * @throws ParseException 
	 */
	protected NbaDst reopenCase(NbaUserVO user, NbaDst work, NbaTXLife contract) throws NbaBaseException, ParseException {
		NbaLob lobs = work.getNbaLob();
		lobs.setCaseFinalDispstn(0);
		lobs.setFinalDispReason(0);
		work.getNbaCase().setUpdate();

		Object[] dbgArgs = new Object[] { lobs.getPolicyNumber() };
		LogHandler.Factory.LogDebug(this, "Policy #({0}) - Reset of the DISP and DSPR LOBs complete.", dbgArgs);

		Holding holding = contract.getPrimaryHolding();
		if (holding.getPolicy() != null) {
			holding.getPolicy().setPolicyStatus(NbaOliConstants.OLI_POLSTAT_PENDING);// ALS2974 ALS2898
			holding.getPolicy().setActionUpdate(); // APSL4076(QC14465)
			// Begin ALS3072
			PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(holding.getPolicy());
			policyExt.setPendingContractStatus("0000");// Reset the Pending contract status for reopen contract
			policyExt.setActionUpdate();
			// End ALS3072
			//Start NBLXA-2112
			List activity = AxaUtils.getActivityForTypeCode(contract,NbaOliConstants.OLI_ACTTYPE_FOLLOWUPINDICATOR);
			if (!NbaUtils.isBlankOrNull(activity) && activity.size() > 0) {
				AxaUtils.updateActLastUpdateField((Activity) activity.get(0), new Date(), AxaConstants.DATE_FORMAT); //Used Activity's indexed 0 , since there will be only one follow up activity on a case
			}
			// End NBLXA-2112
			ApplicationInfoExtension extension = NbaUtils.getFirstApplicationInfoExtension(holding.getPolicy().getApplicationInfo());
			if (null != extension) {
				extension.setUnderwritingStatus(NbaOliConstants.OLI_TC_NULL);
				// //SPR3253 code deleted
				extension.setUnderwritingStatusReason(NbaOliConstants.OLI_TC_NULL);
				extension.setUnderwritingApproval(NbaOliConstants.OLI_TC_NULL); // ALS4431
				// Begin NBA186
				// Delete all the initial decisions
				resetInitialDecision(extension);
				// Delete the tentative dispositions if Initial disposition is Negative (is not Approved)
				if (isLevel1DispositionNegative(extension)) {
					resetTentativeDisp(extension);
				}
				// End NBA186
				extension.setReopenDate(new Date()); // NBA230
				extension.setClosureInd(String.valueOf(NbaConstants.CLOSURE_ACTIVE)); // NBA254 ALS4878
				extension.setClosureOverrideInd(NbaConstants.FALSE);// NAB254 ALS4878
				NbaAutoClosureAccessor.updateClosureIndicator(holding.getPolicy().getPolNumber(), NbaConstants.CLOSURE_ACTIVE); // NBA254 ALS4878

				if (NbaUtils.isReg60Nigo(contract)) {// APSL4140, SR#662330
					extension.setReg60Review(NbaOliConstants.NBA_REG60REVIEW_PENDING);
				}
				extension.setActionUpdate();
				LogHandler.Factory.LogDebug(this, "Policy #({0}) - Reset of contract fields complete.", dbgArgs);
			}
		}
		NbaUtils.resetReinsuranceOffer(contract);// ALS5732
		NbaUtils.resetRateClass(contract); // SR657319
		contract.setBusinessProcess(NBA_PROC_REOPEN);
		NbaContractAccess.doContractUpdate(contract, work, user);
		LogHandler.Factory.LogDebug(this, "Policy #({0}) - Contract updated to backend successfully.", dbgArgs);
		createRelationshipCaseManagerTransaction(user, work, contract);// APSL4412
		WorkflowServiceHelper.update(user, work);
		LogHandler.Factory.LogDebug(this, "Policy #({0}) - Contract updated to workflow successfully.", dbgArgs);
		return work;
	}

	/**
	 * Resets all the tentative Dispositions on the case
	 * 
	 * @param appInfoExt
	 *            ApplicationInfoExtension object
	 */
	// NBA186 New Method
	protected void resetTentativeDisp(ApplicationInfoExtension extension) {
		if (extension != null) {
			int count = extension.getTentativeDispCount();
			for (int i = 0; i < count; i++) {
				extension.getTentativeDispAt(i).setActionDelete();
			}
		}
	}

	/**
	 * Resets all the initial decisions on the case
	 * 
	 * @param appInfoExt
	 *            ApplicationInfoExtension object
	 */
	// NBA186 New Method
	protected void resetInitialDecision(ApplicationInfoExtension extension) {
		if (extension != null) {
			int count = extension.getInitialDecisionCount();
			for (int i = 0; i < count; i++) {
				extension.getInitialDecisionAt(i).setActionDelete();
			}
		}
	}

	/**
	 * This method checks if the level 1 disposition is negative, returns true
	 * 
	 * @param appInfoExt
	 *            ApplicationInfoExtension object
	 * @return isNegative boolean
	 */
	// NBA186 New Method
	private boolean isLevel1DispositionNegative(ApplicationInfoExtension extension) {
		if (extension != null) {
			int count = extension.getTentativeDispCount();
			for (int i = 0; i < count; i++) {
				if (extension.getTentativeDispAt(i).getDispLevel() == 1) {
					return extension.getTentativeDispAt(i).getDisposition() != NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * This method creates a RelationshipCase Manager WI for RCM for formal cases.
	 */
	// APSL4412 New Method
	protected void createRelationshipCaseManagerTransaction(NbaUserVO user, NbaDst work, NbaTXLife contract) throws NbaBaseException{
		try {
			if (!NbaUtils.isAdcApplication(work)) {
				Map deOinkMap = new HashMap();
				deOinkMap.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaConstants.PROC_CASE_REOPENED);
				deOinkMap.put("A_RelationCaseManagerTransaction", "true");
				NbaUserVO userVOTemp = new NbaUserVO();
	            userVOTemp.setUserID(NbaConstants.PROC_CASE_REOPENED);
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(userVOTemp, work, contract, deOinkMap);
				if (provider.getWorkType() != null && provider.getInitialStatus() != null) {
					NbaTransaction aTransaction = work.addTransaction(provider.getWorkType(), provider.getInitialStatus());
					aTransaction.getNbaLob().setRouteReason(
							NbaUtils.getStatusTranslation(provider.getWorkType(), provider.getInitialStatus()) + " - Case Reopened");
					aTransaction.getNbaLob().setCaseManagerQueue(work.getNbaLob().getCaseManagerQueue());
					aTransaction.getNbaLob().setFirstName(work.getNbaLob().getFirstName());
					aTransaction.getNbaLob().setLastName(work.getNbaLob().getLastName());
					aTransaction.getNbaLob().setSsnTin(work.getNbaLob().getSsnTin());
					aTransaction.getNbaLob().setDOB(work.getNbaLob().getDOB());
					aTransaction.getNbaLob().setAgentID(work.getNbaLob().getAgentID());
					aTransaction.getNbaLob().setFaceAmount(work.getNbaLob().getFaceAmount());
				}
			}
		}
		catch (Exception e) {
			NbaUtils.addGeneralComment(work, user, "Error creating workitem for RCM during reopen function, error message - " + e.getMessage());
			throw new NbaBaseException(e);
		}
	}
}
