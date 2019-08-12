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
package com.csc.fsg.nba.business.transaction.datachange;

import java.util.List;

import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
/**
 * 
 * Helper classes to determine Data change 
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
 * </tr>
 * <tr><td>P2AXAL041</td><td>AXA Life Phase 2</td><td>Message received from OLSA Unit Number Validation Interface</td></tr>
 * <tr><td>CR61627</td><td>AXA Life Phase 2</td><td>Assign Replacement Case Manager</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaDataChangeAppComparator extends AxaDataChangeComparator {
	protected Policy oldPolicy;

	protected Policy newPolicy;

	protected PolicyExtension oldPolicyExtension;

	protected PolicyExtension newPolicyExtension;

	protected ApplicationInfo oldApplicationInfo;

	protected ApplicationInfo newApplicationInfo;

	protected ApplicationInfoExtension oldApplicationInfoExtension;

	protected ApplicationInfoExtension newApplicationInfoExtension;

	protected List oldFinancialActivities;

	protected List newFinancialActivities;

	protected Life oldLife;

	protected Life newLife;
	
	protected LifeUSAExtension newLifeUSAExtension;  //CR61627
	
	protected LifeUSAExtension oldLifeUSAExtension; //CR61627

	/**
	 * @param oldPolicy
	 * @param newPolicy
	 */
	public AxaDataChangeAppComparator(Policy newPolicy, Policy oldPolicy) {
		super();
		this.oldPolicy = oldPolicy;
		this.newPolicy = newPolicy;
		if (newPolicy != null && oldPolicy != null) {
			oldPolicyExtension = NbaUtils.getFirstPolicyExtension(oldPolicy);
			newPolicyExtension = NbaUtils.getFirstPolicyExtension(newPolicy);
			oldApplicationInfo = oldPolicy.getApplicationInfo();
			newApplicationInfo = newPolicy.getApplicationInfo();
			oldApplicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(oldApplicationInfo);
			newApplicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(newApplicationInfo);
			oldFinancialActivities = oldPolicy.getFinancialActivity();
			newFinancialActivities = newPolicy.getFinancialActivity();
			if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null
					&& newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife()) {
				newLife = newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
			}
			if (oldPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null
					&& newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife()) {
				oldLife = oldPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
			}
			//Begin CR61627
			if (null != newLife && newLife.hasLifeUSA()) {
				newLifeUSAExtension = NbaUtils.getFirstLifeUSAExtension(newLife.getLifeUSA());
			}
			if (null != oldLife && oldLife.hasLifeUSA()) {
				oldLifeUSAExtension = NbaUtils.getFirstLifeUSAExtension(oldLife.getLifeUSA());
			}
			//end CR61627
		}
	}

	public boolean isNewLife() {
		return (newLife != null && newLife.isActionAdd() && oldLife == null);
	}

	public boolean isNewPolicyExtension() {
		return (newPolicyExtension != null && newPolicyExtension.isActionAdd() && oldPolicyExtension == null);
	}

	public boolean isNewApplicationInfoExtension() {
		return (newApplicationInfoExtension != null && newApplicationInfoExtension.isActionAdd() && oldApplicationInfoExtension == null);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isNewApplicationInfo() {
		return (newApplicationInfo != null && newApplicationInfo.isActionAdd() && oldApplicationInfo == null);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isPendingcontractStatusChanged() {
		if (!isNewPolicyExtension()) {
			if (newPolicy.getPolicyStatus() != oldPolicy.getPolicyStatus()) {
				return true;
			} else if (newPolicyExtension.hasPendingContractStatus()
					&& !newPolicyExtension.getPendingContractStatus().equalsIgnoreCase(oldPolicyExtension.getPendingContractStatus())) {
				return true;
			}
		}
		return false;
	}

	public boolean isUnderwritingStatusChanged() {
		if (!isNewApplicationInfoExtension()) {
			if ((oldApplicationInfoExtension.getUnderwritingApproval() != newApplicationInfoExtension.getUnderwritingApproval())
					|| (oldApplicationInfoExtension.getUnderwritingStatus() != newApplicationInfoExtension.getUnderwritingStatus())
					|| (oldApplicationInfoExtension.getUnderwritingStatusReason() != newApplicationInfoExtension.getUnderwritingStatusReason())) {
				return true;
			}
		}
		if (isNewApplicationInfoExtension()
				&& (newApplicationInfoExtension.hasUnderwritingApproval() || newApplicationInfoExtension.hasUnderwritingStatusReason() || newApplicationInfoExtension
						.hasUnderwritingStatus())) {
			return true;
		}
		return false;
	}	
	/**
	 * 
	 * @return
	 */
	public boolean isPlanChanged() {
		if (newPolicy != null && oldPolicy != null) {
			return !matchAttributes(newPolicy.getProductCode(), oldPolicy.getProductCode());
		}
		return false;
	}
	/**
	 * 
	 * @return
	 */
	public boolean isFaceAmountChanged() {
		if (!isNewLife() && newLife.hasFaceAmt()) {
			return !matchAttributes(newLife.getFaceAmt(), oldLife.getFaceAmt());
		}
		return false;
	}
	/**
	 * 
	 * @return
	 */
	//New Method AXAL3.7.21
	public boolean isIssueDateChanged() {
		if (newPolicy.hasIssueDate() && oldPolicy.hasIssueDate() && !newPolicy.getIssueDate().equals(oldPolicy.getIssueDate())) {
			return true;
		}
		return false;
	}
	/**
	 * 
	 * @return
	 */
	public boolean isSignedDateChanged() {
		if (!isNewApplicationInfo() && newApplicationInfo.hasSignedDate() && oldApplicationInfo.hasSignedDate()) {
			if (!newApplicationInfo.getSignedDate().equals(oldApplicationInfo.getSignedDate())) {
				return true;
			}
		}
		if (isNewApplicationInfo() && newApplicationInfo.hasSignedDate()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	//ALPC136 new method added.
	public boolean isReplacementTypeChanged() {
		if (newPolicy != null && oldPolicy != null) {
			return !matchAttributes(newPolicy.getReplacementType(), oldPolicy.getReplacementType());
		}
		return false;
	}
	
	//ALS4633 new method
	public boolean isReplTypeChangedToDeclineECS() {
		if (newPolicy != null && oldPolicy != null) {
			if (oldPolicy.getReplacementType() == NbaOliConstants.OLI_REPTY_EXTERNAL
					|| oldPolicy.getReplacementType() == NbaOliConstants.OLI_REPTY_NONE
					|| oldPolicy.getReplacementType() == NbaOliConstants.OLI_TC_NULL){
				if (newPolicy.getReplacementType() == NbaOliConstants.OLI_REPTY_INTERNAL
						|| newPolicy.getReplacementType() == NbaOliConstants.OLI_REPTY_UNADREPSTA
						|| newPolicy.getReplacementType() == NbaOliConstants.OLI_REPTY_UNADREPDISB) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	//ALS4153 New Method
	public boolean isRequestedPolicyDateChanged() {
		if (!isNewApplicationInfo()) {
			if (newApplicationInfo.hasRequestedPolDate() && oldApplicationInfo.hasRequestedPolDate()
					&& !newApplicationInfo.getRequestedPolDate().equals(oldApplicationInfo.getRequestedPolDate())) {
				return true;
			} else if (newApplicationInfo.hasRequestedPolDate() && !oldApplicationInfo.hasRequestedPolDate()) {
				return true;
			} else if (!newApplicationInfo.hasRequestedPolDate() && oldApplicationInfo.hasRequestedPolDate()) {
				return true;
			}
		}
		if (isNewApplicationInfo() && newApplicationInfo.hasRequestedPolDate()) {
			return true;
		}
		return false;
	}
	/**
	 * 
	 * @return
	 */
	//ALS4153 New Method
	public boolean isUnderwriterApprovalChanged() {
		if (!isNewApplicationInfoExtension()) {
			if (oldApplicationInfoExtension.getUnderwritingApproval() != newApplicationInfoExtension.getUnderwritingApproval()) {
				return true;
			}
		}
		if (isNewApplicationInfoExtension() && newApplicationInfoExtension.hasUnderwritingApproval()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	//ALS5706 new method added.
	public boolean isApplicationTypeChanged() {
		if (!isNewApplicationInfo()) {
			if (oldApplicationInfo.getApplicationType() != newApplicationInfo.getApplicationType()) {
				return true;
			}
		}
		if (isNewApplicationInfo() && newApplicationInfo.hasApplicationType()) {
			return true;
		}
		return false;
	}
	//ALS5701 New Method
	public boolean isInformalAppApprovalChanged() {
		if (!isNewApplicationInfoExtension()) {
			if (oldApplicationInfoExtension.getInformalAppApproval() != newApplicationInfoExtension.getInformalAppApproval()) {
				return true;
			}
		}
		return false;
	}
	
	//P2AXAL041 new method added
	public boolean isBillingNumberChanged() {
		if (newPolicy != null && oldPolicy != null) {
			return !matchAttributes(newPolicy.getBillNumber(), oldPolicy.getBillNumber());
		}
		return false;
	}
	
	//P2AXAL041 new method added
	public boolean isPaymentMethodChanged() {
		if (newPolicy != null && oldPolicy != null) {
			return !matchAttributes(newPolicy.getPaymentMethod(), oldPolicy.getPaymentMethod());
		}
		return false;
	}
	
	//APSL3259 new method added
	public boolean isAppStateChanged() {
		if (newPolicy != null && oldPolicy != null) {
			return !matchAttributes(newPolicy.getJurisdiction(), oldPolicy.getJurisdiction());
		}
		return false;
	}
	
	//CR61627 New Method
	public boolean is1035ExchangeChanged() {
		if (newLifeUSAExtension != null && oldLifeUSAExtension != null) {
			return newApplicationInfoExtension.hasUnderwritingApproval() && (newLifeUSAExtension.getExchange1035Ind() && !oldLifeUSAExtension.getExchange1035Ind());
		}
		return false;
	}
	//CR61627 New Method
	public boolean isReplacementIndChanged() {
		if (oldApplicationInfo != null && newApplicationInfo != null) {
			return newApplicationInfo.getReplacementInd() && !oldApplicationInfo.getReplacementInd();
		}
		return false;
	}
	
	//APSL3360 New Method
	public boolean isFaceAmountIncreased() {
		if (!isNewLife() && newLife.hasFaceAmt()) {
			return newLife.getFaceAmt() > oldLife.getFaceAmt() ? true : false;
		}
		return false;
	}
	
	//APSL4112 New Method
	public boolean isPaymentAmountChanged() {
	    if (newPolicy != null && oldPolicy != null) {
            return !matchAttributes(newPolicy.getPaymentAmt(), oldPolicy.getPaymentAmt());
        }
        return false;
    }
	
	//APSL4871 New Method
			public boolean isCWAAmtChanged() {
				if (oldApplicationInfo != null && newApplicationInfo != null) {
					return !matchAttributes(newApplicationInfo.getCWAAmt(), oldApplicationInfo.getCWAAmt());
				}
				return false;
			}
			
	// NBLXA-1823 New Method
	public boolean isBusinessStrategiesIndChanged() {
		if (oldPolicyExtension != null && newPolicyExtension != null) {
			if ((newPolicyExtension.getBusinessStrategiesInd() != oldPolicyExtension.getBusinessStrategiesInd()) && newPolicyExtension.getBusinessStrategiesInd()) {
				return true;
			}
		}
		return false;
	}
			
	// NBLXA-188(APSL5318) Legacy Decommissioning New Method
	public boolean isPrintTogetherIndChangedForGI() {
		if (oldPolicyExtension != null && newPolicyExtension != null) {
			//return newPolicyExtension.getPrintTogetherIND() && !oldPolicyExtension.getPrintTogetherIND();
			return !(newPolicyExtension.getPrintTogetherIND() == oldPolicyExtension.getPrintTogetherIND());
		}
		return false;
	}
	
	
	//NBLXA-1539
	public boolean isRequestedPolDateReasonChanged() {
		if (!isNewApplicationInfoExtension()) {
			if (newApplicationInfoExtension.hasRequestedPolDateReason() && oldApplicationInfoExtension.hasRequestedPolDateReason()) {
				if ((oldApplicationInfoExtension.getRequestedPolDateReason() != newApplicationInfoExtension.getRequestedPolDateReason())) { // changed reason
					return true;
				}
			} else if (oldApplicationInfoExtension.hasRequestedPolDateReason() && !newApplicationInfoExtension.hasRequestedPolDateReason()) { //delete reason
				return true;
			} else	if (!oldApplicationInfoExtension.hasRequestedPolDateReason() && newApplicationInfoExtension.hasRequestedPolDateReason()) { //Added reason first time 
				return true;
			}
		}
		if (isNewApplicationInfoExtension() && (newApplicationInfoExtension.hasRequestedPolDateReason())) { // First time added 
			return true;
		}
		return false;
	}
	
	
	// NBLXA-1538
	public boolean isDistChannelChanged(){
		if (oldPolicyExtension != null && newPolicyExtension != null) {
			return !(newPolicyExtension.getDistributionChannel() == oldPolicyExtension.getDistributionChannel());
		}
		return false;
	}
    //NBLXA-1538
	
	// NBLXA-1831
		public boolean isGoldenTicketIndChanged(){
				if (oldPolicyExtension != null && newPolicyExtension != null) {
					return !(newPolicyExtension.getGoldenTicketInd() == oldPolicyExtension.getGoldenTicketInd());
				}
				return false;
			}
	//NBLXA-1831
	
		//NBLXA-2108 New Method
		public boolean isCIPEIndRemoved(){
			if(oldApplicationInfoExtension != null && newApplicationInfoExtension != null){
				if(oldApplicationInfoExtension.getInitialPremiumPaymentForm() == NbaConstants.PAYMENT_TYPE_ACH && newApplicationInfoExtension.getInitialPremiumPaymentForm() != NbaConstants.PAYMENT_TYPE_ACH){
					return true;
				}
			}
			return false;
		}
}
