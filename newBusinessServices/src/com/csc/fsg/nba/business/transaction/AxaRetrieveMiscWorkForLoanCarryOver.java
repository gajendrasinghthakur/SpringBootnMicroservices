package com.csc.fsg.nba.business.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;

public class AxaRetrieveMiscWorkForLoanCarryOver extends AxaDataChangeTransaction {
protected NbaLogger logger = null;
protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";
	protected static	long[] changeTypes = { 	
		DC_CWA_AMT
		};



	@Override
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded()){
			createMiscWork(nbaTxLife,user, nbaDst);
		}
		return nbaDst;
	}
	
	protected void createMiscWork(NbaTXLife nbaTXLife, NbaUserVO userVO, NbaDst nbaDst) throws NbaBaseException {
			NbaSearchVO searchVO = performSearch(userVO, nbaDst.getNbaLob().getPolicyNumber(), NbaConstants.A_WT_MISC_WORK);
			HashMap deoinkMap = new HashMap();
			NbaUserVO tempUserVO = new NbaUserVO();
			tempUserVO.setUserID(NbaConstants.PROC_MISC_WORK_CREATE);
			NbaDst parentWork = null;
			if (nbaDst.isCase()) {
				   parentWork = retrieveAllChildren(userVO, nbaDst, false);
			} else {
				  parentWork = retrieveAllChildren(userVO, nbaDst, true);
			}
			if (parentWork != null) {
			//Invoke VP/MS to determine work type and status
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(tempUserVO, parentWork, nbaTXLife, deoinkMap);
			if(!retrieveMiscWorkItem(searchVO,provider.getWorkType(), provider.getInitialStatus())){
			NbaTransaction aTransaction = parentWork.addTransaction(provider.getWorkType(), provider.getInitialStatus());
			aTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
			//Initialize LOBs of the WI
			aTransaction.getNbaLob().setRouteReason(NbaUtils.getStatusTranslation(provider.getWorkType(),provider.getInitialStatus()) + " - New Case");
			aTransaction.getNbaLob().setCaseManagerQueue(parentWork.getNbaLob().getCaseManagerQueue());
			aTransaction.getNbaLob().setFirstName(parentWork.getNbaLob().getFirstName());
			aTransaction.getNbaLob().setLastName(parentWork.getNbaLob().getLastName());
			aTransaction.getNbaLob().setSsnTin(parentWork.getNbaLob().getSsnTin());
			aTransaction.getNbaLob().setDOB(parentWork.getNbaLob().getDOB());
			aTransaction.getNbaLob().setAgentID(parentWork.getNbaLob().getAgentID());
			aTransaction.getNbaLob().setFaceAmount(parentWork.getNbaLob().getFaceAmount());
			parentWork.setUpdate();
			WorkflowServiceHelper.updateWork(userVO, parentWork);
			}
			}
		
	}
	
	protected boolean retrieveMiscWorkItem(NbaSearchVO searchVO,String workType,String initialStatus) throws NbaNetServerDataNotFoundException
	{
		
		if (searchVO != null) {
			List aList = searchVO.getSearchResults();
			NbaSearchResultVO miscWorkTransaction = null;
			int count = 0;
			if (aList != null)
				count = aList.size();
			for (int i = 0; i < count; i++) {
				miscWorkTransaction = (NbaSearchResultVO) aList.get(i);
				if (workType.equalsIgnoreCase(miscWorkTransaction.getWorkType())) {
					if (initialStatus.equalsIgnoreCase(miscWorkTransaction.getStatus()))
						return true;
				}
			}
		}
		return false;
	}
	
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
	@Override
	protected long[] getDataChangeTypes() {
		return changeTypes;
	}

	@Override
	protected boolean isTransactionAlive() {
		return true;
	}
	
	
	protected NbaSearchVO performSearch(NbaUserVO user, String polNumber, String workType) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName(CASE_SEARCH_VO);
		searchVO.setWorkType(workType); 
		searchVO.setContractNumber(polNumber);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO); 
		return searchVO;
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
