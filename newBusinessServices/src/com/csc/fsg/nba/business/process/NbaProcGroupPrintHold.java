package com.csc.fsg.nba.business.process;
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.csc.fs.logging.Logger;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaGroupPolicyAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaGroupPolicy;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;

/**
 * NbaProcGroupPrintHold is the class that processes work items found on the N2GRPHLD queue.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>CR1343968</td><td>AXA Life Phase 2</td><td>Group Workfow and Miscellaneous</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class NbaProcGroupPrintHold extends NbaAutomatedProcess  {

	/**
	 * This constructor calls the superclass constructor which will set the appropriate statues for the process.
	 */
	public NbaProcGroupPrintHold() {
		super();
	}

	/**
	 * @param user
	 *            the user for whom the work was retrieved, in this case APGRPHLD.
	 * @param work
	 *            the AWD case to be processed
	 * @return NbaAutomatedProcessResult the results of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		if (!initialize(user, work)) {
			return getResult();
		}
		Policy policy = getNbaTxLife().getPolicy();
		PolicyExtension polExtn = NbaUtils.getFirstPolicyExtension(policy);
		ArrayList gicases = new ArrayList();		
		if (polExtn != null && polExtn.hasGuarIssOfferNumber()) {
			NbaGroupPolicy groupPolicy = new NbaGroupPolicy();
			groupPolicy.setReadyForPrint(true);
			groupPolicy.setGiOfferNumber(polExtn.getGuarIssOfferNumber());
			groupPolicy.setBackendKey(policy.getCarrierAdminSystem());
			groupPolicy.setContractKey(policy.getPolNumber());
			groupPolicy.setCompanyKey(policy.getCarrierCode());
			NbaGroupPolicyAccessor.updateGroupRecord(groupPolicy);
			int recordsCount = NbaGroupPolicyAccessor.recordsNotReadyForPrint(groupPolicy);
			if (recordsCount == 0) {
				gicases = NbaGroupPolicyAccessor.selectByGroupNumber(groupPolicy);
				boolean success = true;
				for (int i = 0; i < gicases.size(); i++) {
					NbaGroupPolicy nbaGrouppolicy = (NbaGroupPolicy) gicases.get(i);
					if (groupPolicy.getContractKey().equalsIgnoreCase(nbaGrouppolicy.getContractKey())) {
						continue; //Let the original work item process last.
					}
					try {
					processWorkItem(nbaGrouppolicy);
					}
					catch (NbaBaseException nbe) {
						if (nbe instanceof NbaLockedException) {  //track any locked exceptions so we can retry
							success = false;
						} else if (nbe.isFatal()){  //throw any fatal exceptions back to framework so we error stop the poller...usually a DB exception
								throw nbe;
						} else {
							getLogger().logError(nbe.getStackTrace());
						}
					}
				}
				if (!success) {
					throw new NbaLockedException("Locking Exception in Group Print Processing.");
				}
				//finally, update the original work item
				changeStatus(getWork(), getPassStatus());
				update(getWork());
				NbaGroupPolicyAccessor.deletePolicyRecord(groupPolicy);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
			} else {
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTime(new Date());
				cal.add(Calendar.DAY_OF_WEEK, 90);
				NbaSuspendVO suspendVO = new NbaSuspendVO();
				suspendVO.setTransactionID(getWork().getID());
				suspendVO.setActivationDate(cal.getTime());
				suspendWork(getUser(), suspendVO);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
				//unlockWork();
			}
		}
		return result;

	}

	private void processWorkItem(NbaGroupPolicy nbaGrouppolicy) throws NbaBaseException {
		NbaLob lob = new NbaLob();
		lob.setPolicyNumber(nbaGrouppolicy.getContractKey());
		lob.setCompany(nbaGrouppolicy.getCompanyKey());
		NbaDst nbadst = getTransactionFromAWD(getUser(), lob);/* call to AWD to get the required Workitem */
		
		if (nbadst == null) {
			NbaGroupPolicyAccessor.deletePolicyRecord(nbaGrouppolicy);
			return;
		}
		if (nbadst.isSuspended()) {
			NbaSuspendVO suspendVO = new NbaSuspendVO();
			suspendVO.setTransactionID(nbadst.getID());
			unsuspendWork(suspendVO);
		}
		changeStatus(nbadst, getPassStatus());
		update(nbadst);
		NbaGroupPolicyAccessor.deletePolicyRecord(nbaGrouppolicy);
		unlockWork(nbadst);
	}
		

	/* Gets the printtext Work item in N2GRPHLD queue from AWD with lock on it. If unable to obtain lock throws exception */
	/**
	 * @param nbaUserVO
	 *            the user for whom the work was retrieved, in this case APGRPHLD.
	 * @param SearchLOB
	 *            lobs to look up AWD for the print WorkItem
	 * @return the print Work item if found on AWD
	 * @throws NbaBaseException
	 */
	public NbaDst getTransactionFromAWD(NbaUserVO nbaUserVO, NbaLob searchLOB) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_ST_CONTRACT_PRINT_EXTRACT);
		searchVO.setQueue(NbaConstants.A_QUEUE_GROUP_PRINT_HOLD);
		searchVO.setNbaLob(searchLOB);
		searchVO.setResultClassName("NbaSearchResultVO");
		searchVO = WorkflowServiceHelper.lookupWork(nbaUserVO, searchVO);
		if (searchVO.isMaxResultsExceeded()) {
			throw new NbaBaseException(NbaBaseException.SEARCH_RESULT_EXCEEDED_SIZE, NbaExceptionType.FATAL);
		}
		List searchResult = searchVO.getSearchResults();
		NbaDst dst = null;
		if (searchResult != null && searchResult.size() > 0) {
			NbaSearchResultVO resultVO = (NbaSearchResultVO) searchResult.get(0);
			String id = resultVO.getNbaLob().getKey();
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(id, true);
			retOpt.setLockWorkItem();

			try {
				dst = WorkflowServiceHelper.retrieveWorkItem(nbaUserVO, retOpt);

			} catch (NbaLockedException e) {
				NbaBootLogger.log("Unable to get lock from AWD so suspending for 20 seconds");
				throw e;
			}

		}
		return dst;
	}


	
}
