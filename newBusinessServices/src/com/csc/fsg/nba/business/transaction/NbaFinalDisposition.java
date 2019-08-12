package com.csc.fsg.nba.business.transaction;

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

import java.util.HashMap;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.access.contract.NbaContractAccess;//ALS2062
import com.csc.fsg.nba.exception.NbaTransactionValidationException;//ALS2062

/**
 * Produce and execute the AWD transaction to dispose the contract.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA012</td><td>Version 2</td><td>Contract Extract Print</td></tr>
 * <tr><td>NBA022</td><td>Version 2</td><td>Case Manager view support</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA036</td><td>Version 3</td><td>Underwriter Workbench Trx to DB</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>NBA122</td><td>Version 5</td><td>Underwriter Workbench Rewrite</td></tr>
 * <tr><td>SPR2673</td><td>Version 6</td><td>Routing Reason is not updated when underwriter or case manager approves or declines a case</td></tr>
 * <tr><td>NBA208-15</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 15 - Avoid holding inquiry during Contract approve decline</td></tr>
 * <tr><td>AXAL3.7.25</td><td>AXA Life Phase 2</td><td>Client Interface</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>ALS2062</td><td>AXA Life Phase 1</td><td>QC# 857 E2E Xpression On demand letter 4 - Informal Decline Retail</td></tr>   
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaFinalDisposition extends NbaEventToRequest {
//NBA208-15 code deleted
	protected NbaTXLife originalHoldingInq = null;//NBA186
    protected String secondLvlDecisionQueue = null;//NBA186
/**
 * Create an NbaEventToRequest for a user.
 * @param newUser an nbA user
 * @param originalHolding the holding inquiry as retrieved from a back-end system
 * @param changedHolding a holding inquiry with changes to base the request on
 * @param newCase the AWD case that the holding inquiry is for
 * @param newStatusKey the logical "user" key to the auto process status VP/MS model
 */
// NBA022 - new constructor
//NBA208-15 modfied constructor - removed parameter
public NbaFinalDisposition(NbaUserVO newUser, NbaTXLife changedHolding, NbaDst newDst, String newStatusKey) {
 	super(newUser);
   	//NBA208-15 code deleted
   	holdingInq = changedHolding;
   	work = newDst; //NBA012 Changed from newCase to newDst
   	statusKey = newStatusKey; // NBA022 - added
}
/**
 * Create a NbaFinalDisposition for a user.
 * @param newUser an nbA user
 * @param changedHolding a holding inquiry with changes to base the request on
 * @param newDst
 * @param newStatusKey the logical "user" key to the auto process status VP/MS model
 * @param newSecondLvlDecisionQueue the second level underwriter queue
 */
//NBA186 added new constructor
public NbaFinalDisposition(NbaUserVO newUser, NbaTXLife changedHolding, NbaDst newDst, String newStatusKey, String newSecondLvlDecisionQueue) {
	super(newUser);
	holdingInq = changedHolding;
	work = newDst;
	statusKey = newStatusKey;
	secondLvlDecisionQueue = newSecondLvlDecisionQueue;
}
/**
 * Has a contract final disposition been requested?  The answer is based upon the
 * policy-level action indicator and the value of the Underwriting Status field.
 * @return whether a contract final disposition has been requested.
 */
protected boolean dispositionRequested() {
	Policy newPolicy = getHoldingInq().getPrimaryHolding().getPolicy();
	if (!(newPolicy.isActionUpdate() || newPolicy.isActionSuccessful())) {
		// If the action indicator is wrong then no request has been made.
		return false;
	}
	Long newStatus = getUnderwritingStatus(getHoldingInq(), false); //NBA208-15
	// If null then it couldn't be determined // NBA036
	if (newStatus == null || newStatus.longValue() == -1) { // NBA036 - no longer consider oldStatus
		return false;
	}
	// begin NBA036
	Long oldStatus = getUnderwritingStatus(getHoldingInq(), true); //NBA208-15
	if (oldStatus == null) {
		return true; // newStatus is not null, but oldStatus is. They must be different
	}
	// end NBA036
	return newStatus.longValue() != oldStatus.longValue();
}
/**
 * This method checks if a tentative negative disposition has been requested on the contract. The answer is based upon the
 * policy-level action indicator and the value of the Disposition field.
 * @return boolean whether a negative tentative disposition has been requested
 */
//NBA186 New Method
protected boolean dispositionTentativelyRequested() {
	Policy newPolicy = getHoldingInq().getPrimaryHolding().getPolicy(); //get policy from Nbatxlife directly
	if (!(newPolicy.isActionUpdate() || newPolicy.isActionSuccessful())) {
		// If the action indicator is wrong then no request has been made.
		return false;
	}
	NbaTXLife aHolding = getHoldingInq();
	return getTentativeNegativeDisposition(aHolding) != LONG_NULL_VALUE;
}
/**
 * Execute the event-to-request.
 * @return information about the success or failure of the event-to-request
 */
public NbaEventToRequestResult executeRequest() throws NbaBaseException {
	String passStatus = null;
	try {
		if (dispositionRequested()|| dispositionTentativelyRequested()) {//NBA186
			NbaTXLife contract = getHoldingInq();//NBA186
			contract.setBusinessProcess(PROC_UW);//ALS2062
			NbaDst newDst = getWork();
			//Begin NBA186
			//507 Transaction will be triggered only when the case is finally disposed
            contract.setBusinessProcess(PROC_FINAL_DISPOSITION);
            HashMap deOink = new HashMap();
            //DeOink the second level Decision Queue so that the model returns the correct status
            deOink.put("A_SecondLvlDecisionQueue", secondLvlDecisionQueue);
            //End NBA186
			// NBA020 begin
			//Begin NBA186
			VpmsComputeResult data = getDataFromVpms(NbaVpmsAdaptor.EP_WORKITEM_STATUSES, NbaVpmsAdaptor.AUTO_PROCESS_STATUS, deOink);
            NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(data);
            try {
                NbaContractAccess.doContractUpdate(contract, newDst, user);
            } catch (NbaBaseException nbe) {
            	if (nbe instanceof NbaTransactionValidationException) {//ALS2062
                throw nbe;
            	}
            } catch (Exception ex) {
                return new NbaEventToRequestResult(NbaEventToRequestResult.FAILED, "Couldn't create request.", null);//ALS2062
            }
            //End NBA186
			passStatus = statusProvider.getPassStatus();
			newDst.increasePriority(statusProvider.getCaseAction(), statusProvider.getCasePriority());
			// NBA020 end
			newDst.setStatus(passStatus);
			NbaUtils.setRouteReason(newDst, passStatus); //SPR2673
			//Call Client Interface For Policy Update
			//begin AXAL3.7.25
			if(NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ENABLE_CLIENT_INTERFACE_CALL)){
				AxaUpdateCIFTransaction axaCIFUpdateTrans = new AxaUpdateCIFTransaction();
				//axaCIFUpdateTrans.callCIFUpdate(getHoldingInq(), getUser());//AXAL3.7.07 data change architecture
			}
			//end AXAL3.7.25
		} else {
			return new NbaEventToRequestResult(NbaEventToRequestResult.NO_ATTEMPT, null, null);
		}
	} catch (NbaBaseException nbe) {
		throw nbe;
	} catch (Exception e) {
		throw new NbaBaseException(e);
	}
	return new NbaEventToRequestResult(NbaEventToRequestResult.SUCCESSFUL, null, passStatus);
}
/**
 * Extract the Underwriting Status code from the Application Info Extension.
 * @param aHolding the Acord model for a holding object
 * @return the Underwriting Status
 */
//NBA208-15 changed method signature (changed input parameter type to ApplicationInfoExtension)
protected Long getUnderwritingStatus(NbaTXLife aHolding, boolean originalUndApproval) {
	ApplicationInfo appInfo = aHolding.getPrimaryHolding().getPolicy().getApplicationInfo();
    //begin NBA208-15
	ApplicationInfoExtension appInfoExtension = null;
    if(originalUndApproval){
    	OLifEExtension olifeExt = appInfo.getOLifEExtensionAt(0);
    	appInfoExtension = olifeExt.getApplicationInfoExtensionGhost();
    } else {
        appInfoExtension = NbaUtils.getFirstApplicationInfoExtension(appInfo);
    }
	//end NBA208-15
	if (appInfoExtension != null) {
		return new Long(appInfoExtension.getUnderwritingStatus());
	}
	return null; // none could be found
}
/**
 * This method extracts the Tentative Disp from the Application Info Extension and returns the disposition.
 * @param aHolding the Acord model for a holding object
 * @return long the Underwriting Disposition
 */
//NBA186 New Method
protected long getTentativeNegativeDisposition(NbaTXLife aHolding) {
	ApplicationInfo appInfo = aHolding.getPrimaryHolding().getPolicy().getApplicationInfo();
	if (appInfo.getOLifEExtensionCount() > 0) {
		OLifEExtension extension = appInfo.getOLifEExtensionAt(0); // only 1 is expected
		if (extension != null) {
			if ((NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode())) && (extension.isApplicationInfoExtension())) {
				ApplicationInfoExtension appInfoExtension = null;
				appInfoExtension = extension.getApplicationInfoExtension();
				if (appInfoExtension != null && appInfoExtension.getTentativeDispCount() > 0) {
					if (NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED != appInfoExtension.getTentativeDispAt(0).getDisposition()) {
						return appInfoExtension.getTentativeDispAt(0).getDisposition();
					}
				}

			}
		}
	}

	return LONG_NULL_VALUE;
}
/**
 * Extract the Underwriting Status Reason code from the Application Info Extension.
 * @param aHolding the Acord model for a holding object
 * @return the Underwriting Status
 */
//NBA122 new method
protected Long getUnderwritingStatusReason(NbaTXLife aHolding) {
	ApplicationInfo appInfo = aHolding.getPrimaryHolding().getPolicy().getApplicationInfo();
	ApplicationInfoExtension appInfoExtension = NbaUtils.getFirstApplicationInfoExtension(appInfo);// NBA036 - only 1 is expected
	if (appInfoExtension != null) {
		return new Long(appInfoExtension.getUnderwritingStatusReason());
	}
	return null; // none could be found
}
}
