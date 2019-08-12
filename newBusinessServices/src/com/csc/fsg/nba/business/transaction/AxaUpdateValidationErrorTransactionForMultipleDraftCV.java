package com.csc.fsg.nba.business.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;

/**

 * NBLXA-2519
 *
 */

public class AxaUpdateValidationErrorTransactionForMultipleDraftCV extends AxaDataChangeTransaction implements NbaOliConstants {
	protected NbaLogger logger = null;

	protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";
	public final static String CONTRACT_DELIMITER = "-";
	protected static long[] changeTypes = { DC_MULTIPLE_DRAFT_CV_EXIST };
	protected boolean validationErrorInEndQueue=false;
	protected boolean validationErrorInCaseManagerQueue=false;
	protected NbaSearchResultVO searchResultVo =null;
	protected boolean validationErrorExists=false;
	public static final String END_QUEUE = "END";
	public static final String CONTRACT_VAL_ERR = "CONTRACTVALERR";

	@Override
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		// TODO Auto-generated method stub
		if (isCallNeeded()) {
			NbaUserVO userVO = new NbaUserVO();
			userVO.setUserID(CONTRACT_VAL_ERR);
			Map deOink =new HashMap();
			System.out.println("contract validation error transaction triggered for CV_MULtiple_DRAFT");
			deOink.put("A_CreateValidationWI", "true"); 
			deOink.put("A_ErrorSeverity", Long.toString(NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE));
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(userVO, nbaDst, nbaTxLife, deOink);
			String workType = provider.getWorkType();
			if (null == workType || null == provider.getInitialStatus()) {
				getLogger().logError("Error in VPMS for Business Strategies Transaction Processing for " + nbaTxLife.getPolicy().getPolNumber());
				return nbaDst;
			}
			try {
				NbaSearchVO searchVO = performSearch(userVO, nbaDst.getNbaLob().getPolicyNumber(), NbaConstants.A_WT_NBVALDERR);
				List aList = searchVO.getSearchResults();
				if (aList != null) {
					retrieveValidationErrorWorkItem(aList);
				}
				if (validationErrorExists == true && validationErrorInEndQueue == true
						&& searchResultVo != null) {
					routeValidationErrorTransaction(nbaDst, provider, user, nbaTxLife);
				} else {
					if (!isValidationErrorTransactionForCVExist(searchVO, workType)) {
						createValidationErrorTransaction(nbaDst, provider, user);
					}

				}
				
			} catch (NbaBaseException nbe) {
				getLogger().logError("Error creating NBVALDERR work item for contract " + nbaTxLife.getPolicy().getPolNumber());
				throw nbe;
			}
		}
		return nbaDst;
	}

	protected void routeValidationErrorTransaction(NbaDst aCase, NbaProcessWorkItemProvider provider, NbaUserVO user, NbaTXLife nbaTxLife) throws NbaBaseException {
		System.out.println("contract validation error transaction routed for CV_MULtiple_DRAFT");
		NbaUserVO tempUserVO = new NbaUserVO();
		//String appendRoutReason = NbaUtils.getAppendReason(nbaTxLife);
		tempUserVO.setUserID(CONTRACT_VAL_ERR);
		//NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(tempUserVO, aCase, nbaTxLife, deOink);
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		NbaLob nbaLob = searchResultVo.getNbaLob();
		retOpt.setWorkItem(searchResultVo.getWorkItemID(), false);
		retOpt.setLockWorkItem();
		NbaDst aWorkItem = WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
//		aWorkItem.getNbaLob().setRouteReason(
//				NbaUtils.getStatusTranslation(searchResultVo.getWorkType(), provider.getInitialStatus())+" " + appendRoutReason);
		aWorkItem.getNbaLob().setRouteReason("Validation genrated for Multiple drafts");
		aWorkItem.getNbaLob().setCaseManagerQueue(aCase.getNbaLob().getCaseManagerQueue());
		System.out.println("CSMQ == " + nbaLob.getCaseManagerQueue() + "CSMQ2 == " + aCase.getNbaLob().getCaseManagerQueue());
		aWorkItem.getNbaLob().setFirstName(nbaLob.getFirstName());
		aWorkItem.getNbaLob().setLastName(nbaLob.getLastName());
		aWorkItem.getNbaLob().setSsnTin(nbaLob.getSsnTin());
		aWorkItem.getNbaLob().setDOB(nbaLob.getDOB());
		aWorkItem.getNbaLob().setAgentID(nbaLob.getAgentID());
		aWorkItem.getNbaLob().setFaceAmount(nbaLob.getFaceAmount());
		aWorkItem.setStatus(provider.getInitialStatus());
		aWorkItem.setUpdate();
		WorkflowServiceHelper.updateWork(user, aWorkItem);
		WorkflowServiceHelper.unlockWork(user, aWorkItem);
		provider.getInitialStatus();
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


	protected void retrieveValidationErrorWorkItem(List searchResultList) {
		for (int i = 0; i < searchResultList.size();i++) {
			searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
			validationErrorExists = true;
			if (searchResultVo.getQueue().equalsIgnoreCase(END_QUEUE)) {
				validationErrorInEndQueue = true;
			}
			break;
		}
	}
	
	protected boolean isValidationErrorTransactionForCVExist(NbaSearchVO searchVO, String workType) {
		if (searchVO != null) {
			List aList = searchVO.getSearchResults();
			NbaSearchResultVO validationErrorTransaction = null;
			if (aList != null && !aList.isEmpty()) {
				for (int i = 0; aList.size() > i; i++) {
					validationErrorTransaction = (NbaSearchResultVO) aList.get(i);
					if(workType.equalsIgnoreCase(validationErrorTransaction.getWorkType()) && 
							 validationErrorTransaction.getStatus().equalsIgnoreCase("RQPRTERRD") && 
							!validationErrorTransaction.getQueue().equalsIgnoreCase(END_QUEUE)){
						validationErrorInCaseManagerQueue = true;
						
					}
				}
			}
		}
		return validationErrorInCaseManagerQueue;
	}
	
	private void createValidationErrorTransaction(NbaDst aCase, NbaProcessWorkItemProvider provider, NbaUserVO user) throws NbaBaseException {
		System.out.println("contract validation error transaction Created for CV_MULtiple_DRAFT");
		NbaDst work;
		if(aCase.isCase()){
			work = (NbaDst) aCase.clone();
		}else{
			work = getParentCase(user, aCase);
		}
		work.getWorkItem().setWorkItemChildren(new ArrayList());
		NbaTransaction validationErrorTransaction = work.addTransaction(provider.getWorkType(), provider.getInitialStatus());
		validationErrorTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		validationErrorTransaction.getTransaction().setWorkType(provider.getWorkType());
		validationErrorTransaction.getTransaction().setLock("Y");
		validationErrorTransaction.setStatus(provider.getInitialStatus());
		NbaLob lob = work.getNbaLob();
		String caseManagerLOB = lob.getCaseManagerQueue();
		System.out.println("CSMQ3 == " + caseManagerLOB);
		if (caseManagerLOB != null && !CONTRACT_DELIMITER.equalsIgnoreCase(caseManagerLOB)) {
			validationErrorTransaction.getNbaLob().setCaseManagerQueue(caseManagerLOB);
		}
		if(!NbaUtils.isBlankOrNull(lob.getApplicationType()) ){
			validationErrorTransaction.getNbaLob().setApplicationType(lob.getApplicationType());
		}
		if(!NbaUtils.isBlankOrNull(lob.getSalesChannel())){
			validationErrorTransaction.getNbaLob().setSalesChannel(lob.getSalesChannel()); 
		}	
		validationErrorTransaction.getNbaLob().setRouteReason("Validation genrated for Multiple drafts");
		work = WorkflowServiceHelper.updateWork(user, work);
		List transList = work.getNbaTransactions();
		for (int i = 0; i < transList.size(); i++) {
			NbaTransaction nbaTrans = (NbaTransaction) transList.get(i);
			NbaDst nbaDst = new NbaDst();
			nbaDst.addTransaction(nbaTrans.getWorkItem());
			WorkflowServiceHelper.unlockWork(user, nbaDst);
		}
	}

	protected boolean isValidationErrorTransaction(NbaSearchVO searchVO, String workType) {
		if (searchVO != null) {
			List aList = searchVO.getSearchResults();
			NbaSearchResultVO validationErrorTransaction = null;
			if (aList != null && !aList.isEmpty()) {
				for (int i = 0; aList.size() > i; i++) {
					validationErrorTransaction = (NbaSearchResultVO) aList.get(i);
					if (workType.equalsIgnoreCase(validationErrorTransaction.getWorkType())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected NbaSearchResultVO getValidationErrorTransaction(NbaSearchVO searchVO, String workType) {
		NbaSearchResultVO validationErrorTransaction = null;
		if (searchVO != null) {
			List aList = searchVO.getSearchResults();
			if (aList != null && !aList.isEmpty()) {
				for (int i = 0; aList.size() > i; i++) {
					validationErrorTransaction = (NbaSearchResultVO) aList.get(i);
					if (workType.equalsIgnoreCase(validationErrorTransaction.getWorkType())) {
						return validationErrorTransaction;
					}
				}
			}
		}
		return validationErrorTransaction;
	}

	protected NbaSearchVO performSearch(NbaUserVO user, String polNumber, String workType) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName(CASE_SEARCH_VO);
		searchVO.setWorkType(workType);
		searchVO.setContractNumber(polNumber);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		return searchVO;
	}
	
	//New Method NBLXA-2498
		protected NbaDst getParentCase(NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
			NbaDst parentCase = null;
			if (parentCase == null) {
				//NBA213 deleted code
				//create and set parent case retrieve option
				NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
				retOpt.setWorkItem(nbaDst.getID(), false);
				retOpt.requestCaseAsParent();
				retOpt.requestSources();
				retOpt.requestTransactionAsSibling();//SPR2544
				retOpt.setLockWorkItem();
				retOpt.setLockParentCase();
				retOpt.setAutoSuspend();
				//get case from awd
				parentCase = WorkflowServiceHelper.retrieveWorkItem(user, retOpt); //NBA213
				//NBA213 deleted code
			}
			return parentCase;
		}

}
