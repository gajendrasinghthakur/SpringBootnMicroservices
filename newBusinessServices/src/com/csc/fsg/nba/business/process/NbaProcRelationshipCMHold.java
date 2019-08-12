package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Groupï¿½.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************<BR>
 */
/**
 * APSL4412 Changes related to Relationship CM.
 */

import java.util.List;

import com.csc.fsg.nba.database.AxaWorkflowDetailsDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;

/**
 * 
 * Class for poller A2RCMHLD
 *
 */
//APSL4412 New Class
public class NbaProcRelationshipCMHold extends NbaAutomatedProcess {

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		if (!initialize(user, work)) {
			return getResult();
		}
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getNbaTxLife().getOLifE());
		long polStatus = holding.getPolicy().getPolicyStatus();
		String pendContSts = null;
		if(NbaUtils.getFirstPolicyExtension(holding.getPolicy()) != null) {
			pendContSts = NbaUtils.getFirstPolicyExtension(holding.getPolicy()).getPendingContractStatus();
		}
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		changeStatus(getPassStatus());
		if (holding.getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_ISSUED 
				&& !isReqOutstanding()) {
			setRouteReason(work, getPassStatus(), NbaUtils.getRouteReason(work, getPassStatus()) + " - Case Completed");
		} else if (polStatus == NbaOliConstants.OLI_POLSTAT_WITHDRAW
				|| polStatus == NbaOliConstants.OLI_POLSTAT_OFFEREXPIRED
				|| polStatus == NbaOliConstants.OLI_POLSTAT_WAIVERCOI
				|| polStatus == NbaOliConstants.OLI_POLSTAT_INCOMPLETE
				|| polStatus == NbaOliConstants.OLI_POLSTAT_DECISSUE
				|| polStatus == NbaOliConstants.OLI_POLSTAT_POSTPONED
				|| polStatus == NbaOliConstants.OLI_POLSTAT_29
				|| polStatus == NbaOliConstants.OLI_POLSTAT_CANCELLED
				|| polStatus == NbaOliConstants.OLI_POLSTAT_LAPSEPEND
				|| polStatus == NbaOliConstants.OLI_POLSTAT_NOTAKE) {
			setRouteReason(work, getPassStatus(), NbaUtils.getRouteReason(work, getPassStatus()) + " - Case Closed");
		} else if((pendContSts != null) && ("8005".equalsIgnoreCase(pendContSts)
					|| "8010".equalsIgnoreCase(pendContSts)
					|| "8015".equalsIgnoreCase(pendContSts)
					|| "8105".equalsIgnoreCase(pendContSts))){ 
			setRouteReason(work, getPassStatus(), NbaUtils.getRouteReason(work, getPassStatus()) + " - Case Closed");
		} else {
			setRouteReason(work, getPassStatus(), NbaUtils.getRouteReason(work, getPassStatus()) + " - Followup Required");
		}
		AxaWorkflowDetailsDatabaseAccessor.getInstance().updateWorkFlowDetails(holding.getPolicy().getPolNumber(), 1); //APSL4342 set value to 1 – Active
		doUpdateWorkItem();
		return getResult();
	}
	
	/**
	 * 
	 * @return boolean if any Requirement is outstanding on case
	 */
	public boolean isReqOutstanding() {
 		List reqInfoList = getNbaTxLife().getPolicy().getRequirementInfo();
 		if(reqInfoList != null && reqInfoList.size() > 0){
			for(int i=0;i<reqInfoList.size();i++) {
				RequirementInfo requirementInfo = (RequirementInfo) reqInfoList.get(i);
				if(NbaUtils.isRequirementOutstanding(requirementInfo.getReqStatus())) {
					return true;
				}
			}
 		}
 		return false;
 	}
}
