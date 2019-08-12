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

import java.util.Date;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.LockRetrieveWorkRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.bean.accessors.NbaUnderwriterWorkbenchFacadeBean;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.contract.CommitContractBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaContractApprovalDispositionRequest;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.TentativeDisp;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;

/**
 * Commits the contract approval values in support of the Contract Approve/Decline
 * business function.  Supported input for this process is a <code>List</code> of value
 * objects in the following order:
 * <p>
 * <ul>
 * <li><code>NbaUserVO</code> - index 0 </li>
 * <li><code>NbaDst</code> - index 1</li>
 * <li><code>NbaTXLife</code> - index 2</li>
 * </ul>
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA165</td><td>Version 6</td><td>nbA Contract Approve Decline View Rewrite Project</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-4</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 4</td></tr>
 * <tr><td>NBA208-15</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 15 - Avoid holding inquiry during Contract approve decline</td></tr>
 * <tr><td>SPR3310</td><td>Version 8</td><td>MIB codes are not automatically transmitted</td></tr>
 * <tr><td>SPR3591</td><td>Version 8</td><td>Underwriter Final Disposition and Contract Approval BF duplicated functionality</td></tr>
 * <tr><td>SPR3682</td><td>Version 8</td><td>Negatively disposed contract on Workbench and Contract Decline Business Function does not save contract changes</td></tr>
 * <tr><td>ALS5344</td><td>AXA Life Phase 1</td><td>QC #4526 - MIB reported codes not being sent on final dispositionfor informals</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
//SPR3591 extends CommitContractBP
public class CommitApproveDeclineBP extends CommitContractBP {
    //NBA213 code deleted

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
        	//begin SPR3591
        	NbaContractApprovalDispositionRequest request = (NbaContractApprovalDispositionRequest) input;
        	markupWorkItem(request);
        	markupContract(request);
            //NBA235 code deleted
            // if approval or negative disposition was not specified, just save the changed data
            if (request.getApproval() == NbaOliConstants.OLI_TC_NULL) {
            	result = saveNoDecision(request);
            } else {
            	if(request.getCurrentAction().equalsIgnoreCase("Unapprove") ){ //APSL752
            		callService("UnApproveContractBP", request);			   //APSL752	
            	}else if(!request.isInformalApp()) {
            			applyUWApproveAndDisposition(request);
            			result = updateContract(request);
            	}
            }
            //end SPR3591
            //begin NBA213
            if (!result.hasErrors()){ 
            	result.addResult(request); //SPR3591
            	if (NbaUtils.isNegativeDisposition(request.getNbaDst()) && request.getNbaDst().isSuspended()) { //NBLXA-1246
    				NbaSuspendVO suspendVO = new NbaSuspendVO();
    				suspendVO.setCaseID(request.getNbaDst().getID());
    				WorkflowServiceHelper.unsuspendWork(request.getNbaUserVO(), suspendVO);
    			}
            }
            //end NBA213
        } catch (NbaTransactionValidationException ntve) {  //SPR3591
			addMessage(result, ntve.getMessage());  //SPR3591 
        } catch (NbaBaseException e) {
            addErrorMessage(result, e.getMessage());
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }
	//NBA213 deleted code
    /**
     * Retrieve all MIB Check work items from the workflow system if they have not been
     * previously loaded. 
     * @param work
     */
    //SPR3310 New Method
    protected void retrieveMIBCheckWorkItems(NbaDst work) {
		//setup the search lobs
		NbaLob workLob = work.getNbaLob();
		NbaLob lookupLob = new NbaLob();
		lookupLob.setCompany(workLob.getCompany());
		lookupLob.setPolicyNumber(workLob.getPolicyNumber());
		lookupLob.setReqType((int)NbaOliConstants.OLI_REQCODE_MIBCHECK);

		LockRetrieveWorkRequest request = new LockRetrieveWorkRequest();
		request.setBusinessArea(workLob.getBusinessArea());
		request.setWorkType(NbaConstants.A_WT_REQUIREMENT);
		request.setLobData((LobData[]) lookupLob.getLobs().toArray(new LobData[lookupLob.getLobs().size()]));
		request.setRetrieveWorkLocked(false);
		request.setRetrieveImages(false);
		request.setRetrieveSupportingSources(false);
		request.setWorkItem(work.getWorkItem());
		AccelResult result = (AccelResult) callService("LockRetrieveWorkBP", request);
		if (result.hasErrors()) {
			LogHandler.Factory.LogError(this, "Unable to retrieve associated MIB Check work items: {0}", new Object[] {result.getMessagesList().toString()});
		} else {
			WorkItem workItem = (WorkItem) result.getFirst();
			work.addCase(workItem);
		}
    }
    /**
     * Applies the approve/decline details from the <code>NbaContractApprovalDispositionRequest</code>
     * to the current work item.
     * @param request
     */
    //SPR3591 New Method
	protected void markupWorkItem(NbaContractApprovalDispositionRequest request) {
		NbaLob workLob = request.getWork().getNbaLob();
		workLob.setIssueOthrApplied(request.isIssueOtherThanAppliedFor());

		// incomplete
		if (request.getApproval() == NbaOliConstants.OLI_POLSTAT_INCOMPLETE) {
			if (NbaConstants.SYST_VANTAGE.equals(workLob.getBackendSystem())) {
				request.setUnderwritingStatus(NbaOliConstants.NBA_FINALDISPOSITION_REJINCOMPLETE);
			}
		}

		if (isAllowRequestedIssueDate(request.getContract())) {
			if (request.getIssueDate() == null) {
				workLob.deleteIssueDate();
			} else {
				workLob.setIssueDate(request.getIssueDate());
			}
		}
		if (request.getUnderwritingStatus() != NbaOliConstants.OLI_TC_NULL) {
			workLob.setCaseFinalDispstn((int) request.getUnderwritingStatus());
		}
		if (request.getUnderwritingStatusReason() != NbaOliConstants.OLI_TC_NULL) {
			workLob.setFinalDispReason((int) request.getUnderwritingStatusReason());
		}
		workLob.setAutoClosureStat(workLob.getStatus());//NBLXA-2062
		request.getWork().setUpdate();
	}

	/**
	 * Applies the approve/decline details from the <code>NbaContractApprovalDispositionRequest</code>
	 * to the contract.
	 * @param request
	 */
	//SPR3591 New Method
	protected void markupContract(NbaContractApprovalDispositionRequest request) {
		NbaTXLife contract = request.getContract();

		ApplicationInfo appInfo = getApplicationInfo(contract.getNbaHolding());
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);

		//Contract has been approved
		if (request.getApproval() == NbaOliConstants.OLI_POLSTAT_APPROVED) {
			appInfo.setHOCompletionDate(new Date());
			appInfoExt.setUnderwritingApproval(NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER);
			appInfoExt.setApprovalUnderwriterId(request.getNbaUserVO().getUserID());
			if (isAllowRequestedIssueDate(contract)) {
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
			appInfo.setActionUpdate();
			contract.setBusinessProcess(NbaConstants.PROC_UW_APPROVE_CONTRACT);
		} else if (request.getApproval() == NbaOliConstants.OLI_POLSTAT_DECISSUE ||
					request.getApproval() == NbaOliConstants.OLI_POLSTAT_CANCELLED ||
					request.getApproval() == NbaOliConstants.OLI_POLSTAT_INCOMPLETE) {
			appInfoExt.setUnderwritingStatus(request.getUnderwritingStatus());
			appInfoExt.setUnderwritingStatusReason(request.getUnderwritingStatusReason());
			contract.setBusinessProcess(NbaConstants.PROC_UW_DISPOSITION);  //SPR3682
		}

		appInfoExt.setActionUpdate();
	}

	/**
	 * Returns the <code>ApplicationInfo</code> for the contract.  If the <code>ApplicationInfo</code>
	 * does not exist on the contract, it will be created.  It will also create the
	 * <code>ApplicationInfoExtension</code> if needed.
	 * @param holding
	 * @return
	 */
	//SPR3591 New Method
	protected ApplicationInfo getApplicationInfo(NbaHolding holding) {
		ApplicationInfo appInfo = null;
		appInfo = holding.getApplicationInfo();
		if (appInfo == null) {
			appInfo = new ApplicationInfo();
			appInfo.setActionAdd();
			holding.getPolicy().setApplicationInfo(appInfo);
			createApplicationInfoExtension(appInfo);
		} else {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			if (appInfoExt == null) {
				createApplicationInfoExtension(appInfo);
			}
		}
		return appInfo;
	}

	/**
	 * Creates the <code>ApplicationInfoExtension</code> and attaches it to the given <code>ApplicationInfo</code>.
	 * @param appInfo
	 */
	//SPR3591 New Method
	protected void createApplicationInfoExtension(ApplicationInfo appInfo) {
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
		olifeExt.getApplicationInfoExtension().setActionAdd();
		appInfo.addOLifEExtension(olifeExt);
	}

	/**
	 * Returns true if the user can set a requested issue date.
	 * @param contract
	 * @return
	 */
	//SPR3591 New Method
	protected boolean isAllowRequestedIssueDate(NbaTXLife contract) {
		Policy policy = contract.getPolicy();
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		return !(policy.getApplicationInfo().getApplicationType() == NbaOliConstants.OLI_APPTYPE_CHANGE && contract.isFixedPremium() &&
				(policyExtension != null && policyExtension.getLoansInd()));
	}

	/**
	 * This method applies the contract approval and final disposition to the host. 
	 * @param newBusinessUWAppRequest NewBusinessUWApproveRequest object
	 * @return result AccelResult object
	 * @throws NbaBaseException
	 */
	//SPR3591 New Method
    protected void applyUWApproveAndDisposition(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
    	//Begin ALS5192
    	NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(request.getWork().getID(), true);
		retOpt.setLockWorkItem();
		retOpt.requestSources();
		retOpt.requestTransactionAsChild(); 
		//End ALS5192
		NbaUnderwriterWorkbenchFacadeBean bean = new NbaUnderwriterWorkbenchFacadeBean();
		bean.setNbaDstWithAllTransactions(retrieveWorkItem(request.getNbaUserVO(), retOpt)); //ALS5192
		bean.applyApproveAndDisposition(request.getNbaUserVO(), request.getContract(), request.getWork(),
				NbaVpmsAdaptor.UISTATUS_CASE_MANAGER, request.getSecondLevelDecisionQueue(), request.isInformalApp()); //ALS5344
		//APSL5335 :: START
		long undStatus = getUnderwriterDisposition(request);
		ApplicationInfo appInfo = request.getContract().getNbaHolding().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getAppInfoExtension(appInfo);
		addTentativeDisposition(request.getUnderwritingStatusReason(), undStatus, request.getNbaUserVO(), appInfoExt, request.getUnderwriterRole(),
				request.getUnderwriterRoleLevel());
		//APSL5335 :: END
	}

	/**
	 * This method applies the finalDisposition and update the contract
	 * @param request NbaContractApprovalDispositionRequest
	 * @return AccelResult
	 * @throws NbaBaseException
	 */
    //SPR3591 New Method
	protected AccelResult updateContract(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
		request.setUpdateWork(true);
		request.setUnlockWork(true);
		request.setUserID(NbaVpmsAdaptor.UISTATUS_CASE_MANAGER);
		return persistContract(request);
	}

	/**
	 * If no decision has been made, there is no changes to save on the contract.
	 * It is temporarily removed from the request so that only the work item is
	 * persisted.  It is replaced back in the request after the work item is
	 * persisted.
	 * @param request
	 * @return
	 */
	//SPR3591 New Method
	protected AccelResult saveNoDecision(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
		NbaTXLife contract = request.getContract();
		request.setContract(null);
        request.setUpdateWork(true);
    	AccelResult result = persistContract(request);
    	request.setContract(contract);
    	return result;
	}
	
	/**
	 * Retrieves all the transactions for the case
	 * @param nbaUserVO NbaUserVO object
	 * @param retOpt NbaAwdRetrieveOptionsVO object
	 * @return NbaDst containing all the transaction for the case
	 * @throws NbaBaseException
	 */
	//ALS5192 New Method
	protected NbaDst retrieveWorkItem(NbaUserVO nbaUserVO, NbaAwdRetrieveOptionsVO retOpt) throws NbaBaseException {
		retOpt.setNbaUserVO(nbaUserVO);
		AccelResult accelResult = (AccelResult) callBusinessService("NbaRetrieveWorkBP", retOpt);
		processResult(accelResult);
		return (NbaDst) accelResult.getFirst();
	}
	


}
