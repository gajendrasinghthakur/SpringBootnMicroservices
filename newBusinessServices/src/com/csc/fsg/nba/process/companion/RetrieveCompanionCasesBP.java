package com.csc.fsg.nba.process.companion;

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

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.LockRetrieveWorkRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.bean.accessors.NbaCompanionCaseFacadeBean;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.rules.WorkItemIdentificationBP;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;

/**
 * Retrieves a list of companion cases for the given case by calling  
 * the <code>NbaCompanionCaseFacadeBean</code>. Requires an <code>NbaDst</code>.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA153</td><td>Version 6</td><td>Companion Case Rewrite</td></tr> 
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application Project</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Deferred Work Item Retrieval</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class RetrieveCompanionCasesBP extends NewBusinessAccelBP {

    /**
     * Called to retrieve a List of companion cases for the given case
     * @param an instance of <code>NbaDst</code> object
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            result.addResult(retrieveCompanionCases((NbaDst) input));
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

	public List retrieveCompanionCases(NbaDst dst) throws Exception {
		NbaCompanionCaseFacadeBean bean = new NbaCompanionCaseFacadeBean();  //NBA213
        List companionCases = bean.retrieveCompanionCases(dst.getNbaUserVO(), dst, true);  //NBA213
		WorkItemIdentificationBP workItemIDBP = new WorkItemIdentificationBP();
		NbaCompanionCaseVO companionCase; //NBA208-36
		int count = companionCases.size();
		//begin NBA208-36
		if (count == 0 && !isTrialApplication(dst)) {  //NBA187
			dst = retrievePaymentTransactions(dst);
			companionCase = new NbaCompanionCaseVO(dst, dst.getNbaUserVO()); // NBA331, APSL5055
			companionCase.setActionAdd();
			companionCases.add(companionCase);
		}
		//end NBA208-36
		for (int i = 0; i < count; i++) {
			companionCase = (NbaCompanionCaseVO) companionCases.get(i);
			Result result = workItemIDBP.process(companionCase.getNbaDst());
			if (!result.hasErrors()) {
				companionCase.setRouteReason((String) result.getFirst());
			}
		}
		return companionCases;
	}
	//NBA213 deleted code
	/**
	 * 
	 * @param work
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA208-36 New Method
	public NbaDst retrievePaymentTransactions(NbaDst work) throws NbaBaseException {

		NbaLob workLob = work.getNbaLob();
		if (!NbaUtils.isBlankOrNull(workLob.getCompany()) && !NbaUtils.isBlankOrNull(workLob.getPolicyNumber())) {//ALS2288
			//setup the search lobs
			NbaLob tempLob = new NbaLob();
			tempLob.setCompany(workLob.getCompany());
			tempLob.setPolicyNumber(workLob.getPolicyNumber());

			LockRetrieveWorkRequest request = new LockRetrieveWorkRequest();
			request.setLobData((LobData[]) tempLob.getLobs().toArray(new LobData[tempLob.getLobs().size()]));
			request.setBusinessArea(work.getBusinessArea());
			request.setWorkType(NbaConstants.A_WT_PAYMENT);
			request.setPageNumber("1"); // APSL5055-NBA331
			request.setRetrieveImages(true);
			request.setRetrieveSupportingSources(true);
			request.setRetrieveWorkLocked(false);
			request.setWorkItem(work.getWorkItem());
			AccelResult result = (AccelResult) callService("LockRetrieveWorkBP", request);
			if (!processResult(result)) {
				work.addCase((WorkItem) result.getFirst());
				request.setWorkItem(work.getWorkItem());
				request.setWorkType(NbaConstants.A_WT_CWA);
				result = (AccelResult) callService("LockRetrieveWorkBP", request);
				if (!processResult(result)) {
					work.addCase((WorkItem) result.getFirst());
				}
			}
		}
		return work;
	}

	/**
	 * Returns true if the case work item is a trial application.
	 * @param dst
	 * @return
	 */
	//NBA187 New Method
	protected boolean isTrialApplication(NbaDst dst) throws NbaBaseException {
		NbaLob nbaLob = dst.getNbaLob();
		return nbaLob != null && nbaLob.getAppOriginType() == NbaConstants.TRIAL_APPLICATION;
	}
}
