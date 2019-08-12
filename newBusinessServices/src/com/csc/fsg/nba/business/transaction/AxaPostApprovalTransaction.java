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

package com.csc.fsg.nba.business.transaction;

import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;

/**
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Data Change Architecture</td>
 * <td>ALS4659</td><td>AXA Life Phase 1</td><td>Signed Illustration and Premium quote to be marked expired.</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaPostApprovalTransaction extends AxaDataChangeTransaction implements NbaConstants{
	protected NbaLogger logger = null;
	
	protected static	long[] changeTypes = { 	
			DC_UND_APPROVAL_CHANGED
		};

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded() && nbaTxLife.isUnderwriterApproved()) {
			//signed illustration
			List reqInfoList = nbaTxLife.getRequirementInfoList(nbaTxLife.getPrimaryParty(), NbaOliConstants.OLI_REQCODE_SIGNILLUS);
			updateReqSubStatus(reqInfoList); //QC#5277 - APSL291
			
			//premium quote
			reqInfoList = nbaTxLife.getRequirementInfoList(nbaTxLife.getPrimaryParty(), NbaOliConstants.OLI_REQCODE_1009800041);
			updateReqSubStatus(reqInfoList); //QC#5277 - APSL291
			
			//Begin ALS5706 Policy Delivery Receipt
			reqInfoList = nbaTxLife.getRequirementInfoList(nbaTxLife.getPrimaryParty(), NbaOliConstants.OLI_REQCODE_POLDELRECEIPT);
			updateReqSubStatus(reqInfoList); //QC#5277 - APSL291
			//End ALS5706
			
			//Begin APSL415 Premium Due Carrier
			reqInfoList = nbaTxLife.getRequirementInfoList(nbaTxLife.getPrimaryParty(), NbaOliConstants.OLI_REQCODE_PREMDUE);
			updateReqSubStatus(reqInfoList);
			//End APSL415
			
			//Begin APSL1322 Amendment
			reqInfoList = nbaTxLife.getRequirementInfoList(nbaTxLife.getPrimaryParty(), NbaOliConstants.OLI_REQCODE_AMENDMENT);
			updateReqSubStatus(reqInfoList);
			//End APSL1322			
			
//			Begin APSL2663 Disclosure Form 
			if(nbaDst.getNbaLob().getIssueOthrApplied()){
				Policy policy = nbaTxLife.getPrimaryHolding().getPolicy();
				long state = policy.getJurisdiction();
				ApplicationInfo applicationInfo = policy.getApplicationInfo();
				boolean replInd = applicationInfo.getReplacementInd();
				if(state == NbaOliConstants.NBA_STATES_NY && replInd == true){
					reqInfoList = nbaTxLife.getRequirementInfoList(nbaTxLife.getPrimaryParty(), NbaOliConstants.OLI_REQCODE_STATEDISC);
					//NBLXA-1782 Starts
					List reqInfoListForNYDisclosure = nbaTxLife.getRequirementInfoList(nbaTxLife.getPrimaryParty(), NbaOliConstants.OLI_REQCODE_1009800194);
					if(reqInfoListForNYDisclosure.size() > 0)
					{
						reqInfoList.addAll(reqInfoListForNYDisclosure);
					}
					//NBLXA-1782 Ends
					updateReqSubStatus(reqInfoList);
				}
			}
			//End APSL2663	
		}
		return nbaDst;
	}

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#getDataChangeTypes()
	 */
	protected long[] getDataChangeTypes() {
		return changeTypes; 
	}

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaDataChangeTransaction#isTransactionAlive()
	 */
	protected boolean isTransactionAlive() {
		return true;
	}
	/**
	 * Update requirement sub status to expired so new requirement can be added
	 * @param reqInfoList
	 * @param reqSubStatus
	 */
	//QC#5277 - APSL291 new method 
	protected void updateReqSubStatus(List reqInfoList) {
		if (reqInfoList != null) {
			int count = reqInfoList.size();
			RequirementInfo reqInfo = null;
			for (int i = 0; i < count; i++) {
				reqInfo = (RequirementInfo) reqInfoList.get(i);
				if (reqInfo != null) {
					//Making requirement expired
					reqInfo.setReqSubStatus(NbaOliConstants.OLI_REQSUBSTAT_CNCLINSCO);
					reqInfo.setActionUpdate();
				}

			}
		}
	}

}
