package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */

import java.util.List;

import com.csc.fsg.nba.database.NbaScorDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaScorSubmitContractVO;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class NbaProcSIScorePoll extends NbaAutomatedProcess {
		
	private static final int _5Min = 300;
	private static final int _24Hrs = 86400;
	
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		List records = selectSubmitedRecordForProcess();
		boolean continuePolling = false;
		for (int i = 0; i < records.size(); i++) {
			continuePolling = true;
			NbaScorSubmitContractVO contVO = (NbaScorSubmitContractVO) records.get(i);
			NbaTXLife WSResp = (NbaTXLife) AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_RETRIEVE_SCOR, user, null, work,
					contVO.getContractKey()).execute();
			TransResult transResult = WSResp.getTransResult();
			//If Response is not ready at SCOR
			if (transResult != null && transResult.getResultCode() == NbaUtils.TC_RESCODE_SUCCESSINFO) {
				if (doSCORProcessUsingJMS) {
					if (contVO.getOccurance() == 12) {
						contVO.setNextPollTime(NbaUtils.addSecondsToTimestamp(contVO.getNextPollTime(), _24Hrs));
					} else {
						contVO.setOccurance(contVO.getOccurance() + 1);
						contVO.setNextPollTime(NbaUtils.addSecondsToTimestamp(contVO.getNextPollTime(), _5Min));
					}
				} else {
					int newRetryFreq = NbaAutoUnderwritingHelper.getNextRetryFreq(contVO.getRetryFrequency(), contVO.getOccurance());
					int newOccurance = NbaAutoUnderwritingHelper.getNextOccurenceNum(contVO.getRetryFrequency(), contVO.getOccurance());
					int newIncrementSec = NbaAutoUnderwritingHelper.getNextIncrementSec(newRetryFreq);
					contVO.setOccurance(newOccurance);
					contVO.setRetryFrequency(newRetryFreq);
					contVO.setNextPollTime(NbaUtils.addSecondsToTimestamp(contVO.getNextPollTime(), newIncrementSec));
					if (contVO.getOccurance() == NbaAutoUnderwritingHelper.OCC_TIME_OUT
							&& contVO.getRetryFrequency() == NbaAutoUnderwritingHelper.RETRY_FREQ_THREE) {
						contVO.setNextPollTime(NbaUtils.addSecondsToTimestamp(contVO.getNextPollTime(), _24Hrs));
					}
				}
				updateSubmitedRecordForRetry(contVO);
			} else {
				contVO.setWebServiceResponse(WSResp.toXmlString());
				setWork(retrieveMatchingCase(user, contVO.getWorkItemId()));				
				unsuspendMatchingCase(user);
				updateSubmitedRecordForProcess(contVO);
			}
		}
		if (continuePolling) {
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "");
		}
		return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", "");
	}

	private List selectSubmitedRecordForProcess() {
		try {
			return NbaScorDatabaseAccessor.selectRecords();
		} catch (NbaBaseException nbe) {
			printError("Exeception occurred while retrieving contract from NBA_SCOR_SUBMIT_DATA");
			printError(nbe.getMessage());
		}
		return null;
	}

	private NbaDst retrieveMatchingCase(NbaUserVO user, String workItemID) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(workItemID, true);		
		retOpt.setLockWorkItem();
		aWorkItem = retrieveWorkItem(user, retOpt);
		return aWorkItem;
	}

	private void unsuspendMatchingCase(NbaUserVO user) throws NbaBaseException {
		if (getWork().isSuspended() && NbaConstants.A_QUEUE_SI_AUTO_UNDERWRITING.equalsIgnoreCase(getWork().getQueue())) {
			NbaSuspendVO suspendVO = new NbaSuspendVO();
			suspendVO.setCaseID(getWork().getID());
			//Change for ALII1929(QC11445)
			//suspendVO.setRetrieveWorkItem(false);			
			unsuspendWork(user, suspendVO);			
		}
		unlockWork(user, getWork());
	}

	private void updateSubmitedRecordForProcess(NbaScorSubmitContractVO scorSubmitContract) {
		try {
			NbaScorDatabaseAccessor.updateRecordResponse(scorSubmitContract);
		} catch (NbaBaseException nbe) {
			printError("Exeception occurred while updating response in NBA_SCOR_SUBMIT_DATA for contract " + scorSubmitContract.getContractKey());
			printError(nbe.getMessage());
		}
	}

	private void updateSubmitedRecordForRetry(NbaScorSubmitContractVO scorSubmitContract) {
		try {
			NbaScorDatabaseAccessor.updateNextPollTime(scorSubmitContract);
		} catch (NbaBaseException nbe) {
			printError("Error in updating retry frequency, Occurance in NBA_SCOR_SUBMIT_DATA for contract " + scorSubmitContract.getContractKey());
			printError(nbe.getMessage());
		}
	}
	
	private void printError(String error) {
		if (getLogger().isErrorEnabled())
			getLogger().logError(error);
	}
}