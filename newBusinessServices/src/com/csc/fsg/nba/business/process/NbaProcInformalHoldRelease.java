package com.csc.fsg.nba.business.process;

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
 *     Copyright (c) 2002-2009 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */

import java.util.List;

import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * NbaProcInformalHoldRelease is the class that processes cases in N2INLDR queue. Inormal wholesale cases that need prioritization are assigned
 * underwriter and case manager and then send to underwriting risk auto process.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.3M1</td><td>AXA Life Phase 1</td><td>Informal Implementation Miscellaneous Requirements Part 1</td></tr>
 * <tr><td>AXAL3.7.20G</td><td>AXA Life Phase 1</td><td>Workflow Gaps</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 */
public class NbaProcInformalHoldRelease extends NbaAutomatedProcess {

	/**
	 * This is the entry point for the auto process. After processing, it updates pending contract and updates case in AWD.
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {
		if (!initialize(user, work)) {
			return getResult();
		}

		doProcess();
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		changeStatus(getResult().getStatus());
		doContractUpdate();
		doUpdateWorkItem();
		return getResult();
	}

	/**
	 * This is the core method which call other methods.
	 * @throws NbaBaseException
	 */
	protected void doProcess() throws NbaBaseException {
		//ALS4707 Code deleted
		assignUWCMQueue(false, false, getWorkLobs());
		//Clear the close out date
		getNbaTxLife().getPolicy().getApplicationInfo().setPlacementEndDate(""); //AXAL3.7.20G
		getNbaTxLife().getPolicy().getApplicationInfo().setActionUpdate(); //AXAL3.7.20G
	}

	/**
	 * Assign underwriter and case manager to the case.
	 * @param isUndqLOBPresent
	 * @param isCsmqLOBPresent
	 * @param caseLob
	 * @throws NbaBaseException
	 */
	protected void assignUWCMQueue(boolean isUndqLOBPresent, boolean isCsmqLOBPresent, NbaLob caseLob) throws NbaBaseException {

		String underwriterLOB = null;
		String caseManagerLOB = null;
		//if any one of them are not assigned
		if (caseLob.getCompanionType() != null && !NbaConstants.NOT_A_COMPANION_CASE.equalsIgnoreCase(caseLob.getCompanionType())) {//if not null and
			// not of type "0"
			NbaCompanionCaseVO vo;
			NbaCompanionCaseRules rules = new NbaCompanionCaseRules(user, work);
			List compCases = rules.getCompanionCases();
			NbaLob companionLob = null;
			int size = compCases.size();
			for (int i = 0; i < size; i++) {
				vo = (NbaCompanionCaseVO) compCases.get(i);
				companionLob = vo.getNbaLob();
				if (caseLob.getPolicyNumber().equalsIgnoreCase(vo.getContractNumber())) {//when current work is retrieved
					continue;
				}
				if (companionLob != null && !NbaUtils.isBlankOrNull(companionLob.getUndwrtQueue()) && !isUndqLOBPresent) {
					underwriterLOB = companionLob.getUndwrtQueue();
					isUndqLOBPresent = true;
				}
				if (companionLob != null && !NbaUtils.isBlankOrNull(companionLob.getCaseManagerQueue()) && !isCsmqLOBPresent) {
					caseManagerLOB = companionLob.getCaseManagerQueue();
					isCsmqLOBPresent = true;
				}
				if (isUndqLOBPresent && isCsmqLOBPresent) {//Both are assigned from companion cases
					break;
				}
			}
		}

		if (!(isUndqLOBPresent && isCsmqLOBPresent)) {//if any one of them still not assigned
			NbaProcessStatusProvider statusProvider = getProcesStatusProvider(caseLob, getNbaTxLife());
			if (!isUndqLOBPresent) {
				underwriterLOB = getEquitableQueue(statusProvider.getOtherStatus(), NbaLob.A_LOB_ORIGINAL_UW_WB_QUEUE);
			}
			if (!isCsmqLOBPresent) {
				String caseManagerReult = statusProvider.getAlternateStatus();
				if (caseManagerReult.indexOf("|") != -1) {
					caseManagerReult = getListOfCMQueues(caseManagerReult, underwriterLOB);
				}
				caseManagerLOB = getEquitableQueue(caseManagerReult, NbaLob.A_LOB_CM_QUEUE);
			}
		}

		if (underwriterLOB != null && !CONTRACT_DELIMITER.equalsIgnoreCase(underwriterLOB)) {
			caseLob.setUndwrtQueue(underwriterLOB);
		}
		if (caseManagerLOB != null && !CONTRACT_DELIMITER.equalsIgnoreCase(caseManagerLOB)) {
			caseLob.setCaseManagerQueue(caseManagerLOB);
		}
	}

}
