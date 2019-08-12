package com.csc.fsg.nba.business.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * 
 * @author apatel235
 * NBLXA-1823
 *
 */

public class AxaUpdateBusinessStrategiesTransaction extends AxaDataChangeTransaction implements NbaOliConstants {
	protected NbaLogger logger = null;

	protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";

	protected static long[] changeTypes = { DC_BUSINESS_STRATEGIES };

	@Override
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		// TODO Auto-generated method stub
		if (isCallNeeded()) {
			NbaUserVO userVO = new NbaUserVO();
			userVO.setUserID(NbaConstants.PROC_BSTG_WORK_CREATE);
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(userVO, nbaDst, nbaTxLife, new HashMap());
			String workType = provider.getWorkType();
			if (null == workType || null == provider.getInitialStatus()) {
				getLogger().logError("Error in VPMS for Business Strategies Transaction Processing for " + nbaTxLife.getPolicy().getPolNumber());
				return nbaDst;
			}

			try {
				NbaSearchVO searchVO = performSearch(userVO, nbaDst.getNbaLob().getPolicyNumber(), NbaConstants.A_WT_BUSINESS_STRATEGIES);
				if (!isBusinessStrategiesWIPresent(searchVO, provider.getWorkType())) {
					createBusinessStrategiesTransaction(nbaDst, provider, user);
				}
			} catch (NbaBaseException nbe) {
				getLogger().logError("Error creating NBRPLNOTIF work item for contract " + nbaTxLife.getPolicy().getPolNumber());
				throw nbe;
			}
		}
		return nbaDst;
	}

	@Override
	protected long[] getDataChangeTypes() {
		// TODO Auto-generated method stub
		return changeTypes;
	}

	@Override
	protected boolean isTransactionAlive() {
		// TODO Auto-generated method stub
		return true;
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

	private void createBusinessStrategiesTransaction(NbaDst aCase, NbaProcessWorkItemProvider provider, NbaUserVO user) throws NbaBaseException {

		NbaDst work = (NbaDst) aCase.clone();
		work.getWorkItem().setWorkItemChildren(new ArrayList());
		NbaTransaction busStrategiesTransaction = work.addTransaction(provider.getWorkType(), provider.getInitialStatus());
		busStrategiesTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		busStrategiesTransaction.getTransaction().setWorkType(provider.getWorkType());
		busStrategiesTransaction.getTransaction().setLock("Y");
		busStrategiesTransaction.setStatus(provider.getInitialStatus());

		// Copy lobs from the case to the new transaction
		NbaLob caseLob = work.getNbaLob();
		NbaLob replLob = busStrategiesTransaction.getNbaLob();

		replLob.setPolicyNumber(caseLob.getPolicyNumber());
		replLob.setCompany(caseLob.getCompany());
		replLob.setLastName(caseLob.getLastName());
		replLob.setFirstName(caseLob.getFirstName());
		replLob.setSsnTin(caseLob.getSsnTin());
		replLob.setTaxIdType(caseLob.getTaxIdType());
		replLob.setAppOriginType(caseLob.getAppOriginType());
		replLob.setAppState(caseLob.getAppState());
		replLob.setBusinessStrategyInd(caseLob.getBusinessStrategyInd());
		work = WorkflowServiceHelper.updateWork(user, work);
		List transList = work.getNbaTransactions();
		for (int i = 0; i < transList.size(); i++) {
			NbaTransaction nbaTrans = (NbaTransaction) transList.get(i);
			NbaDst nbaDst = new NbaDst();
			nbaDst.addTransaction(nbaTrans.getWorkItem());
			WorkflowServiceHelper.unlockWork(user, nbaDst);
		}
	}

	protected boolean isBusinessStrategiesWIPresent(NbaSearchVO searchVO, String workType) {
		if (searchVO != null) {
			List aList = searchVO.getSearchResults();
			NbaSearchResultVO businessStrategiesTransaction = null;
			if (aList != null && !aList.isEmpty()) {
				for (int i = 0; aList.size() > i; i++) {
					businessStrategiesTransaction = (NbaSearchResultVO) aList.get(i);
					if (workType.equalsIgnoreCase(businessStrategiesTransaction.getWorkType())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected NbaSearchVO performSearch(NbaUserVO user, String polNumber, String workType) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName(CASE_SEARCH_VO);
		searchVO.setWorkType(workType);
		searchVO.setContractNumber(polNumber);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		return searchVO;
	}
	
}
