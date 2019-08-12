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

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.contract.CommitContractBP;
import com.csc.fsg.nba.vo.NbaContractApprovalDispositionRequest;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;

/**
 * This BP is responsible for resetting the tentative dispositions on the contract. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class ResetFinalDispositionBP extends CommitContractBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaContractApprovalDispositionRequest request = (NbaContractApprovalDispositionRequest) input;
			resetDisposition(request.getContract());
			result = persistContract(request);
			if (!result.isErrors()) {
				cleanupContract(request.getContract());
			}
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}

	/**
	 * This method applies the reset disposition action
	 * @param nbaTXLife NbaTXLife object
	 */
	protected void resetDisposition(NbaTXLife nbaTXLife) {
		nbaTXLife.setBusinessProcess(NbaConstants.PROC_UW_RESET_TENTDISP);//ALII1348
		ApplicationInfo appInfo = nbaTXLife.getNbaHolding().getApplicationInfo();
		//Clean Holding.Policy.ApplicationInfo.ApplicationInfoExtension.TentativeDisp object for Reset Disposition Action
		ApplicationInfoExtension extension = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		removeTentativeDisp(extension);
		//Start NBLXA-1651: set ReinsuranceRiskBasis to blank on reset Tantative deposition
          if(!nbaTXLife.isInformalApplication()){	
	            ReinsuranceInfo reinInfo = nbaTXLife.getDefaultReinsuranceInfo();
	            if (!NbaUtils.isBlankOrNull(reinInfo) && reinInfo.hasReinsuranceRiskBasis()
                       && reinInfo.getReinsuranceRiskBasis() != NbaOliConstants.OLI_REINRISKBASE_FA) {
	               reinInfo.setReinsuranceRiskBasis(null); 
                   if (!reinInfo.isActionAdd()) {
                       reinInfo.setActionUpdate();
                   }
               }
          }
        //End NBLXA-1651 
	}

	/**
	 * This method removes all the tentative Dispositions on the case
	 * @param appInfoExt ApplicationInfoExtension object
	 */
	protected void removeTentativeDisp(ApplicationInfoExtension extension) {
		if (extension != null) {
			int count = extension.getTentativeDispCount();
			for (int i = 0; i < count; i++) {
				extension.getTentativeDispAt(i).setActionDelete();
			}
		}
	}

	/**
	 * On successful commit, clean up collections of removed data that would be typically
	 * just marked as deleted successfully. 
	 * @param contract
	 */
	protected void cleanupContract(NbaTXLife contract) {
		ApplicationInfo appInfo = contract.getNbaHolding().getApplicationInfo();
		//Clean Holding.Policy.ApplicationInfo.ApplicationInfoExtension.TentativeDisp object for Reset Disposition Action
		ApplicationInfoExtension extension = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (extension != null) {
			extension.getTentativeDisp().clear();
			extension.getTentativeDispGhost().clear();
		}
	}
}
