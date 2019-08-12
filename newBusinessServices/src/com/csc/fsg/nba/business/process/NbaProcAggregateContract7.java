package com.csc.fsg.nba.business.process;

import java.util.Calendar;
import java.util.Iterator;

import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaUserVO;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

/**
 * Drives the aggregate contract process. Controls the aggregation of work items being sent to the UW queue for processing. Works on all the work
 * items sent to "Aggregate Contract 7" queue and will perform aggregation of the work items into NBAGGCNT work item and routes this aggregated
 * NBAGGCNT work item to UW queue or to "End" queue depending upon the business logic.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * 	<th align=left>Project</th>
 * 	<th align=left>Release</th>
 * 	<th align=left>Description</th>
 * </thead>
 * <tr>
 * 	<td>NBLXA-1326</td>
 * 	<td>Discretionary</td>
 * 	<td>Created Class with aggregateToUW()</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class NbaProcAggregateContract7 extends NbaProcAggregateContract {

	@Override
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		if (!initialize(user, work)) {// Initialization
			return getResult();
		}
		if (retrieveTransactionsRequired()) {
			setParentWork(retrieveParentWithTransactions()); // to get parent item and/or all its child /sibling transactions
		}
		return aggregateToUW();
	}

	/**
	 * NBLXA-1326
	 * Requirement WI will get suspended with Aggregated status and one AggregateContract WI will be created for UW review.
	 * @return NbaAutomatedProcessResult
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult aggregateToUW() throws NbaBaseException {
		try {
			if (toUpdateTODOLOB()) {
				updateTODOListCount();
			}
			boolean hasErrors = false;
			boolean newWorkCreated = false;
			NbaSuspendVO suspendItem = null;
			if (NbaUtils.isBlankOrNull(work.getNbaLob().getPolicyNumber())) {
				suspendItem = getSuspendWorkVO(Calendar.MINUTE, MIN);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, EMPTY_STRING, EMPTY_STRING));
			}
			if (getResult() == null) {
				if (NbaConstants.A_WT_AGGREGATE_CONTRACT.equalsIgnoreCase(work.getWorkType())) {
					if (getResult() == null) {
						if (hasOtherWorkItemsInAggregateContractQueue()) {
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, EMPTY_STRING, getOtherStatus()));
						} else {
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, EMPTY_STRING, getPassStatus()));
						}
					} else {
						hasErrors = true;
					}
					changeStatus(getResult().getStatus());
				} else {
					if (!isAnyOpenAggregateContract(getAggReference())) {
						createNewAggregateContract();
						newWorkCreated = true;
					}
					suspendItem = getSuspendWorkVO(Calendar.DATE, DAY);
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, EMPTY_STRING, EMPTY_STRING));
				}
			}
			commit(hasErrors, suspendItem, newWorkCreated);
			unlockWorkItems();
		} finally {
			// unlock any work items not already unlocked excluding the current work item
			// this should only happen in an exception being thrown situation
			if (getParentWork() != null && getParentWork().isCase() && getParentWork().isLocked(user.getUserID())) {
				WorkItem childItem = null;
				Iterator iter = getParentWork().getTransactions().iterator();
				while (iter.hasNext()) {
					childItem = (WorkItem) iter.next();
					if (childItem.getItemID().equals(work.getID())) {
						iter.remove();
						break;
					}
				}
				unlockWorkItems();
			}
		}
		return getResult();
	}
}
