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

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.contract.CommitContractBP;
import com.csc.fsg.nba.vo.NbaContractApprovalDispositionRequest;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.ActivityExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ContractChangeInfo;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfoExtension;

/**
 * Commits the contract decline values in support of the Contract Approve/Decline
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
 * <tr><td>NBA208-15</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 15 - Avoid holding inquiry during Contract approve decline</td></tr>
 * <tr><td>SPR3591</td><td>Version 8</td><td>Underwriter Final Disposition and Contract Approval BF duplicated functionality</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr> 
 * <tr><td>SPR3682</td><td>Version 8</td><td>Negatively disposed contract on Workbench and Contract Decline Business Function does not save contract changes</td></tr>
 * <tr><td>SR657319</td><td>Discretionary</td><td>Manual selection of Rate Class</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
//SPR3591 extends CommitContractBP
public class UnApproveContractBP extends CommitContractBP {
    //NBA213 code deleted

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
        	//begin SPR3591
			NbaContractApprovalDispositionRequest request = (NbaContractApprovalDispositionRequest) input;
			unapproveContract(request.getContract());
            //Begin QC#7889,APSL1700
			NbaDst workDst=request.getNbaDst();
			if (workDst != null) {
				NbaLob lob = workDst.getNbaLob();
				lob.setCaseFinalDispstn((int) NbaOliConstants.TC_ILLSEC_ZERO);//QC#9136
				lob.setUnderwriterActionLob(NbaOliConstants.OLI_UW_UNAPPROVE_ACTION);//APSL4981
				lob.setActionUpdate();
			}
           //END QC#7889,APSL1700
			result = persistContract(request);
			cleanUpContract(result);//NBA186
			//end SPR3591
        } catch (NbaBaseException e) {
            addErrorMessage(result, e.getMessage());
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

	// SPR3591 code deleted
    
	/**			
	 * Cleans the following data on the contract.
	 *   > ApplicationInfo.UnderwritingApproval
	 *   > ApplicationInfo.ApprovalUnderwriterId
	 *   > ApplicationInfo.InformalAppApproval 
	 *   > ApplicationInfo.HOCompletionDate
	 *   > ApplicationInfo.TentativeDisp
	 *   > ApplicationInfo.InitialDescision
	 */
	//SPR3591 New Method
	protected void unapproveContract(NbaTXLife contract) {
		contract.setBusinessProcess(NbaConstants.PROC_UW_UNAPPROVE_CONTRACT);  //SPR3682//ALS5747

		ApplicationInfo appInfo = contract.getNbaHolding().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (appInfoExt != null) {
			appInfoExt.setUnderwritingApproval(NbaOliConstants.OLI_TC_NULL);
			appInfoExt.setApprovalUnderwriterId(null);
			appInfoExt.setInformalAppApproval(NbaOliConstants.OLI_TC_NULL);
			removeTentativeDisp(appInfoExt); //NBA186
			removeInitialDecision(appInfoExt); //NBA186
            OLifEExtension olifeExt = appInfo.getOLifEExtensionAt(0);
            olifeExt.setApplicationInfoExtensionGhost(appInfoExt.clone(false));
            NbaUtils.resetReinsuranceOffer(contract);//ALS5732
            
//          Begin APSL2765
           if(!contract.isInformalApplication()){	//APSL2906 if condition added to skip informal cases as reinsurance type does not get set at final disposition for informal cases
	            ReinsuranceInfo reinInfo = contract.getDefaultReinsuranceInfo();
	            if (!NbaUtils.isBlankOrNull(reinInfo) && reinInfo.hasReinsuranceRiskBasis()
                        && reinInfo.getReinsuranceRiskBasis() != NbaOliConstants.OLI_REINRISKBASE_FA) {// APSL2906 added null check for reinInfo
                                                                                                       // object , APSL4127 - added check for facultative reinsurance.
                    //reinInfo.deleteReinsuranceRiskBasis();
	            	reinInfo.setReinsuranceRiskBasis(null); //NBLXA-244
                    if (!reinInfo.isActionAdd()) {
                        reinInfo.setActionUpdate();
                    }
                }
	            ReinsuranceInfoExtension reinsuranceInfoExtension = NbaUtils.getFirstReinsuranceInfoExtension(reinInfo);
	    		if(!NbaUtils.isBlankOrNull(reinsuranceInfoExtension)){
	    			reinsuranceInfoExtension.setPreventOverrideInd(false);
	    			if(!reinsuranceInfoExtension.isActionAdd()){
	    				reinsuranceInfoExtension.setActionUpdate();
	    			}
	    		}
           }
//    		End APSL2765
    		
	        appInfoExt.setActionUpdate();
		}
		appInfo.setHOCompletionDate((Date) null);
		appInfo.setActionUpdate();
		//begin ALS5927 
		//reset unbound/contractchangereprint information
		Policy policy = contract.getNbaHolding().getPolicy();
		PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(policy);
		if (null != polExt) {
			//APSL4585 Register Date changes removed reset unbound Indicator
			polExt.deleteContractChangeReprintDate();
			polExt.setContractChangeReprintInd(false);
			polExt.setInitialPaymentDueDate((Date) null);//QC12083/APSL3278
			polExt.setActionUpdate();
		}
		//end ALS5927
		String contractChangeInfoId = getContractChangeInfoId(contract); //APSL5128,APSL5376
		createActivity(contract , contractChangeInfoId); //APSL5128,APSL5376
		NbaUtils.resetRateClass(contract); // SR657319
    }

	// APSL5128 New Method
	private String getContractChangeInfoId(NbaTXLife nbaTXLife) {
		// TODO Auto-generated method stub
		String contractChangeInfoId = "";
		Policy pol = nbaTXLife.getPolicy();
		PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(pol);
		ContractChangeInfo contractChangeInfo = NbaUtils.getContractChangeInfoByStatus(polExt, NbaOliConstants.OLIEXT_LU_ACTSTAT_INITIATED);
		if (!NbaUtils.isBlankOrNull(contractChangeInfo)) {
			contractChangeInfoId = contractChangeInfo.getId();
		}
		return contractChangeInfoId;
	}

	// APSL5128 New Method
	private void createActivity(NbaTXLife nbaTXLife, String contractChangeInfoId) {
		// TODO Auto-generated method stub
		Activity activity = new Activity();
		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTXLife); //APSL5376
		nbaOLifEId.setId(activity); //APSL5376
		activity.setActivityTypeCode(NbaOliConstants.OLI_ACTTYPE_CONTRACTUNAPPROVE);
		activity.setStartTime(new NbaTime(new Date()));
		activity.setEndTime(new NbaTime(new Date()));
		activity.setActivityStatus(NbaOliConstants.OLI_LU_ACTUNAPPROVE_STATUS_INITIATED);
		ActivityExtension activityExtn = NbaUtils.createActivityExtension(activity);
		activityExtn.setRelatedObjectId(contractChangeInfoId);
		activityExtn.setActionAdd();
		activity.setActionAdd();
		nbaTXLife.getOLifE().addActivity(activity);
	}

	/**
	 * The method removes all the tentative Dispositions on the case
	 * @param appInfoExt ApplicationInfoExtension object
	 */	
	//NBA186 New Method
	protected void removeTentativeDisp(ApplicationInfoExtension appInfoExt) {
		int count = appInfoExt.getTentativeDispCount();
		for (int i = 0; i < count; i++) {
			appInfoExt.getTentativeDispAt(i).setActionDelete();
		}
	}

	/**
	 * The method removes all the initial decisions on the case
	 * @param appInfoExt ApplicationInfoExtension object
	 */	
	//NBA186 New Method
	protected void removeInitialDecision(ApplicationInfoExtension appInfoExt) {
		int count = appInfoExt.getInitialDecisionCount();
		for (int i = 0; i < count; i++) {
			appInfoExt.getInitialDecisionAt(i).setActionDelete();
		}
	}
	
	/**
	 * This method clears the TentativeDisp and InitialDecision objects from in memory ApplicationInfoExtension after we update the holding inquiry to
	 * the back end system.
	 * @param result AccelResult object containing NbaTXLife.
	 */
	//NBA186 New Method
	protected void cleanUpContract(AccelResult result) {
		NbaContractApprovalDispositionRequest request = (NbaContractApprovalDispositionRequest) result.getFirst();
		NbaTXLife contract = request.getContract();
		ApplicationInfo appInfo = contract.getNbaHolding().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (appInfoExt != null) {
			// Remove deleted tentative disp entries from contract
			appInfoExt.getTentativeDisp().clear();
			appInfoExt.getTentativeDispGhost().clear();
			// Remove deleted initial decision entries from contract
			appInfoExt.getInitialDecision().clear();
			appInfoExt.getInitialDecisionGhost().clear();
		}
	}
	
}
