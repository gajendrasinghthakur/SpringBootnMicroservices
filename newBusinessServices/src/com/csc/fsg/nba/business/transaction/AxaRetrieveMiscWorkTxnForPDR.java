package com.csc.fsg.nba.business.transaction;

import java.util.HashMap;
import java.util.List;

import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;

public class AxaRetrieveMiscWorkTxnForPDR extends AxaDataChangeTransaction implements NbaOliConstants{

protected NbaLogger logger = null;
protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";
	protected static	long[] changeTypes = { 	
		DC_CV_1797_EXISTS 
		};
	
protected boolean miscworkPDRTxnExists=false;
protected boolean miscworkInEndQueue=false;
protected NbaTransaction endedTransaction = null;

	@Override
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded()){
			createMiscWork(nbaTxLife,user, nbaDst);
		}
		return nbaDst;
	}
	
	protected void createMiscWork(NbaTXLife nbaTXLife, NbaUserVO userVO, NbaDst nbaDst) throws NbaBaseException {
		// NbaSearchVO searchVO = performSearch(userVO, nbaDst.getNbaLob().getPolicyNumber(), NbaConstants.A_WT_MISC_WORK);
		HashMap deoinkMap = new HashMap();
		NbaUserVO tempUserVO = new NbaUserVO();
		NbaDst parentWork = null;
		if (nbaDst.isCase()) {
			   parentWork = retrieveAllChildren(userVO, nbaDst, false);
		} else {
			  parentWork = retrieveAllChildren(userVO, nbaDst, true);
		}
		if (parentWork != null) {
			tempUserVO.setUserID(NbaConstants.PROC_MISC_WORK_CREATEPICM);
			// Invoke VP/MS to determine work type and status
			retrieveMiscWorkItemWithRPDRLob(parentWork);
			if (miscworkPDRTxnExists == false) {
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(tempUserVO, nbaDst, nbaTXLife, deoinkMap);
				NbaTransaction aTransaction = parentWork.addTransaction(provider.getWorkType(), provider.getInitialStatus());
				aTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
				// Initialize LOBs of the WI
				aTransaction.getNbaLob().setRouteReason(
				NbaUtils.getStatusTranslation(provider.getWorkType(), provider.getInitialStatus()) + " - New Case");
				aTransaction.getNbaLob().setCaseManagerQueue(parentWork.getNbaLob().getCaseManagerQueue());
				aTransaction.getNbaLob().setFirstName(parentWork.getNbaLob().getFirstName());
				aTransaction.getNbaLob().setLastName(parentWork.getNbaLob().getLastName());
				aTransaction.getNbaLob().setSsnTin(parentWork.getNbaLob().getSsnTin());
				aTransaction.getNbaLob().setDOB(parentWork.getNbaLob().getDOB());
				aTransaction.getNbaLob().setAgentID(parentWork.getNbaLob().getAgentID());
				aTransaction.getNbaLob().setFaceAmount(parentWork.getNbaLob().getFaceAmount());
				aTransaction.getNbaLob().setReceivedPDRInd(true);
				parentWork.setUpdate();
				WorkflowServiceHelper.updateWork(userVO, parentWork);
			} else if (miscworkPDRTxnExists == true) {
				if (miscworkInEndQueue == true) {
					tempUserVO.setUserID(NbaConstants.PROC_MISC_WORK_ROUTEPICM);
					NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(tempUserVO, nbaDst, nbaTXLife, deoinkMap);
					NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
					retOpt.setWorkItem(endedTransaction.getID(), false);
					retOpt.setLockWorkItem();
					NbaDst aWorkItem = WorkflowServiceHelper.retrieveWorkItem(userVO, retOpt);
					aWorkItem.getNbaLob().setRouteReason(endedTransaction.getNbaLob().getRouteReason());
					aWorkItem.getNbaLob().setCaseManagerQueue(endedTransaction.getNbaLob().getCaseManagerQueue());
					aWorkItem.getNbaLob().setFirstName(endedTransaction.getNbaLob().getFirstName());
					aWorkItem.getNbaLob().setLastName(endedTransaction.getNbaLob().getLastName());
					aWorkItem.getNbaLob().setSsnTin(endedTransaction.getNbaLob().getSsnTin());
					aWorkItem.getNbaLob().setDOB(endedTransaction.getNbaLob().getDOB());
					aWorkItem.getNbaLob().setAgentID(endedTransaction.getNbaLob().getAgentID());
					aWorkItem.getNbaLob().setFaceAmount(endedTransaction.getNbaLob().getFaceAmount());
					aWorkItem.getNbaLob().setReceivedPDRInd(endedTransaction.getNbaLob().getReceivedPDRInd());
					aWorkItem.setStatus(statusProvider.getPassStatus());
					aWorkItem.getNbaLob().setRouteReason(statusProvider.getOtherStatus());
					aWorkItem.setUpdate();
					WorkflowServiceHelper.updateWork(userVO, aWorkItem);
					WorkflowServiceHelper.unlockWork(userVO, aWorkItem);
				}
			}
		}
	}

	protected void retrieveMiscWorkItemWithRPDRLob(NbaDst parentWork) throws NbaBaseException {
		List transactions = parentWork.getNbaTransactions();
		for (int i = 0; i < transactions.size(); i++) {
			NbaTransaction nbaTransaction = (NbaTransaction) transactions.get(i);
			if (nbaTransaction.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_MISC_WORK) && nbaTransaction.getNbaLob().getReceivedPDRInd()) {
				miscworkPDRTxnExists = true;
				if (nbaTransaction.isInEndQueue()) {
					miscworkInEndQueue = true;
					endedTransaction = nbaTransaction;
				}
				break;
			}
		}
	}
	
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
			}
		}
		return logger;
	}
	@Override
	protected long[] getDataChangeTypes() {
		return changeTypes;
	}

	@Override
	protected boolean isTransactionAlive() {
		return true;
	}
	


		protected NbaDst retrieveAllChildren(NbaUserVO user, NbaDst parentCase)  throws NbaBaseException {
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			//retOpt.setWorkItem(parentCase.getID(), true);
			retOpt.setWorkItem(parentCase.getID(), false);
			retOpt.requestTransactionAsChild();
		//	retOpt.setLockTransaction();
			retOpt.setLockParentCase();
			return WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
			
		}
		
		protected NbaDst retrieveAllChildren(NbaUserVO user, NbaDst parentCase, boolean retrieveSiblings)     throws NbaBaseException{
			{
				NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
				retOpt.setWorkItem(parentCase.getID(), false);
				if (retrieveSiblings) {
					retOpt.requestCaseAsParent();
					retOpt.requestTransactionAsSibling();
				} else {
					retOpt.requestTransactionAsChild();
				}
				retOpt.setLockParentCase();
				return WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
			}
		}
		
	
	
	
}
