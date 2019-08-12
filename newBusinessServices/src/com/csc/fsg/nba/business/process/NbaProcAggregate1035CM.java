package com.csc.fsg.nba.business.process;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.CopyLobsTaskConstants;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;

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
 * Drives the aggregate contract process for 1035 Exchange.  Controls the aggregation of work items being sent to the  
 * Exchange case manager queue for processing. 
 * Works on all the work items sent to "Aggregate Contract 1035 Exchange" queue and will perform aggregation of  
 * the work items into NBAGGCNT work item and routes this aggregated NBAGGCNT work item to Exchange case manager   
 * queue or to "End" queue depending upon the business logic.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>CR59174</td><td>XA Life Phase 2</td><td>1035 Exchange Case Manager</td></tr>
 * <tr><td>CR61627</td><td>AXA Life Phase 2</td><td>Assign Replacement Case Manager</td></tr>
 * <tr><td>CR1345857(APSL2575)</td><td>Discretionary</td>Predictive CR - Aggregate</tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class NbaProcAggregate1035CM extends NbaProcAggregateContract {

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work)
			throws NbaBaseException {
		// Initialization
		if (!initialize(user, work)) {
			return getResult();
		}
		try {
			if (null == getParentWork()) { //CR61627-PERF
				setParentWork(retrieveParentWithTransactions());
			} //CR61627-PERF
			resetCaseManagers(); //CR61627
			if(toUpdateTODOLOB()){
				updateTODOListCount();//APSL4342
			}
			boolean hasErrors = false;
			boolean newWorkCreated = false;
			NbaSuspendVO suspendItem = null;
			if(NbaUtils.isBlankOrNull(work.getNbaLob().getPolicyNumber())){
				suspendItem  = getSuspendWorkVO(Calendar.MINUTE, MIN);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));		
			}
			if (shouldSuspendWork()) {
				suspendItem = getSuspendWorkVO(Calendar.DATE, DAY);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Workitem suspended - Exchange Case Manager LOB is Null.",""));
			}
			if (getResult() == null){//APSL677
				if (NbaConstants.A_WT_AGGREGATE_1035.equalsIgnoreCase(work
						.getWorkType())) {
					if (getResult() == null) {
						if (hasOtherWorkItemsInAggregateContractQueue()) {
							setResult(new NbaAutomatedProcessResult(
									NbaAutomatedProcessResult.SUCCESSFUL, "",
									getOtherStatus()));
						} else {
							setResult(new NbaAutomatedProcessResult(
									NbaAutomatedProcessResult.SUCCESSFUL, "",
									getPassStatus()));
						}
					} else { // host error encounter
						hasErrors = true;
					}
					changeStatus(getResult().getStatus());
				} else {
					NbaTransaction aggregate1035 = findAggregate1035();
					if (null == aggregate1035) {
						NbaTransaction aggContract =createNewAggregateContract();//NBLXA-1554[NBLXA-2057]
						newWorkCreated = true;
						reinitializeFields(aggContract);//NBLXA-1554[NBLXA-2057]
					} else if (isAggregateContractInEndQueue(aggregate1035.getNbaLob())
							|| NbaConstants.A_WT_CWA.equalsIgnoreCase(work.getWorkType())) { //NBLXA-2623
						routeToExchangeCM(aggregate1035);
					}
					suspendItem = getSuspendWorkVO(Calendar.DATE, DAY);
					setResult(new NbaAutomatedProcessResult(
							NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
				}
			}
			commit(hasErrors, suspendItem, newWorkCreated);
			unlockWorkItems();
		} finally {
			// unlock any work items not already unlocked excluding the current
			// work item
			// this should only happen in an exception being thrown situation
			if (getParentWork() != null && getParentWork().isCase()
					&& getParentWork().isLocked(user.getUserID())) {
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
	protected boolean shouldSuspendWork() throws NbaBaseException {
		
		if(getParentWork() != null && getParentWork().getNbaLob().getExchCMQueue()==null || getParentWork().getNbaLob().getExchCMQueue().trim().length()<=0){
				return true;
		}
		return false;
	}
	/**
	 * Find Aggregate 1035 work item
	 * @return true if an open aggregate contract wokritem exists on the case else return false.
	 * @throws NbaBaseException
	 */
	protected NbaTransaction findAggregate1035() throws NbaBaseException {
		List transactions = getParentWork().getNbaTransactions();
		int count = transactions.size();
		NbaTransaction nbaTransaction = null;
		for (int i = 0; i < count; i++) {
			nbaTransaction = (NbaTransaction) transactions.get(i);
			if (NbaConstants.A_WT_AGGREGATE_1035.equalsIgnoreCase(nbaTransaction.getWorkType())) {
				return nbaTransaction;
				}
		}
		return null;
	}

	/*
	 * Obtain initial status from VPMS and then route Aggregate1035 work item to appropriate Queue
	 */
	protected void routeToExchangeCM(NbaTransaction nbaTransaction) throws NbaBaseException {
		NbaProcessWorkItemProvider workItemProvider = getWorkItemFromVPMS();
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(nbaTransaction.getID(), false);
		retOpt.setLockWorkItem();
		NbaDst nbaDst = retrieveWorkItem(getUser(), retOpt);
		nbaDst.setStatus(workItemProvider.getInitialStatus());
		setRouteReason(nbaDst, workItemProvider.getInitialStatus());
		reinitializeFields(nbaDst.getNbaTransaction());//NBLXA-1554[NBLXA-2057]
		nbaDst.setUpdate();
		updateWork(getUser(),nbaDst);
		unlockWork(nbaDst);
		
		
	}
	/**
	 * Creates new aggregate contract work item. Call VP/MS model to get work type, initial status, work priority and priority action.
	 * @throws NbaBaseException
	 */
	protected NbaTransaction createNewAggregateContract() throws NbaBaseException {
		
		NbaProcessWorkItemProvider provider = getWorkItemFromVPMS(); //CR59174
		NbaTransaction nbaTransaction = getParentWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
		nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		NbaLob transactionLOBs = nbaTransaction.getNbaLob();
		List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2AGGCNT_CREATE_AGGCNT);
		getParentWork().getNbaLob().copyLOBsTo(transactionLOBs, lobList);
		transactionLOBs.setQueueEntryDate(new Date());
		transactionLOBs.setWritingAgency(NbaUtils.getWritingAgencyId(getNbaTxLife()));		
		setRouteReason(nbaTransaction, provider.getInitialStatus());
		return nbaTransaction;
	}
	/**
	 * Returns �true� if one or more work items are residing in the �N21035AC� queue with same policy number and company name. 
	 * Called only when �Get work� gets an �Aggregate Contract� work item in the Aggregate Contract automated process.
	 * @return
	 * @throws NbaBaseException
	 */
	protected boolean hasOtherWorkItemsInAggregateContractQueue() throws NbaBaseException {
		List transactions = getParentWork().getNbaTransactions();
		int count = transactions.size();
		boolean hasMoreWorkFlag = false;
		NbaTransaction nbaTransaction = null;
		for (int i = 0; i < count; i++) {
			nbaTransaction = (NbaTransaction) transactions.get(i);
			if (!NbaConstants.A_WT_AGGREGATE_1035.equalsIgnoreCase(nbaTransaction.getWorkType())) {
				if (work.getQueue().equalsIgnoreCase(nbaTransaction.getQueue())) {
					hasMoreWorkFlag = true;
					break;
				}
			}
		}
		return hasMoreWorkFlag;
	}
	
	//CR61627 New Method
    protected boolean isCaseManagerMissing() {
    	try {
    	return null == getParentWork().getNbaLob().getExchCMQueue() || getParentWork().getNbaLob().getExchCMQueue().trim().length() <= 1;
    	} catch (NbaBaseException nbe) {
    		getLogger().logError("error retrieving Exchange CM Queue!");
    	}
    	return false;
    	
    }
    
    //CR61627 New Method
    protected String getCaseManagerAssignment() {
    	return NbaLob.A_LOB_EXCHANGE_CM;
    }
    
    //CR61627 New Method
    protected String getCaseManagerLOB() {
    	return exchange1035CM;
    }
    
    //CR1345857(APSL2575) New Method
    protected boolean getPredictiveIndicator() throws NbaBaseException {
		return false;
    }
}
