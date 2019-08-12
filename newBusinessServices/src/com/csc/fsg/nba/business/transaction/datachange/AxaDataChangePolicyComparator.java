
package com.csc.fsg.nba.business.transaction.datachange;
/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
import java.util.List;

import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProducerVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;

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
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaDataChangePolicyComparator extends AxaDataChangeComparator {
	protected Policy oldPolicy;

	protected Policy newPolicy;

	protected PolicyExtension oldPolicyExtension;

	protected PolicyExtension newPolicyExtension;

	protected ApplicationInfo oldApplicationInfo;

	protected ApplicationInfo newApplicationInfo;

	protected ApplicationInfoExtension oldApplicationInfoExtension;

	protected ApplicationInfoExtension newApplicationInfoExtension;

	/**
	 * @param oldPolicy
	 * @param newPolicy
	 */
	public AxaDataChangePolicyComparator(Policy newPolicy, Policy oldPolicy) {
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
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean isNewPolicyExtension() {
		return (newPolicyExtension != null && newPolicyExtension.isActionAdd() && oldPolicyExtension == null);
	}

	/**
	 * 
	 * @return
	 */
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

	/**
	 * 
	 * @return
	 */
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
}
