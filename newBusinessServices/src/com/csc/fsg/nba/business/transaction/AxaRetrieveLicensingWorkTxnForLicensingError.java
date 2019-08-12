package com.csc.fsg.nba.business.transaction;

import java.util.HashMap;
import java.util.List;

import com.csc.fs.accel.valueobject.WorkItem;
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

public class AxaRetrieveLicensingWorkTxnForLicensingError extends AxaDataChangeTransaction implements NbaOliConstants{

protected NbaLogger logger = null;
protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";
	protected static	long[] changeTypes = { 	
		DC_SEVERE_LICENSING_CV_EXIST 
		};
	
protected boolean licensingworkExists=false;
protected boolean licensingworkInEndQueue=false;
protected NbaSearchResultVO searchResultVo =null;
public static final String END_QUEUE = "END";
public String contractKey = null;

	@Override
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded()){
			createAgentLicensingWork(nbaTxLife,user, nbaDst);
		}
		return nbaDst;
	}
	
	protected void createAgentLicensingWork(NbaTXLife nbaTXLife, NbaUserVO userVO, NbaDst nbaDst) throws NbaBaseException {
		HashMap deoinkMap = new HashMap();
		NbaUserVO tempUserVO = new NbaUserVO();
		NbaSearchVO searchVO = null;
		NbaDst parentWork = null;
		String appendRoutReason = null;
		List searchResultList = null;
		
		if (nbaTXLife != null && nbaTXLife.getPolicy() != null && nbaTXLife.getPolicy().getPolNumber() != null) {
			contractKey = nbaTXLife.getPolicy().getPolNumber();
		}
		NbaSearchVO searchAgentLicensingVO = searchAgentLicesingWI(userVO);
		if (!NbaUtils.isBlankOrNull(searchAgentLicensingVO) 
				&& searchAgentLicensingVO.getSearchResults()!=null 
				&& !searchAgentLicensingVO.getSearchResults().isEmpty()) {
			 searchResultList = searchAgentLicensingVO.getSearchResults();
		}
		
		if (nbaDst.isCase()) {
			   parentWork = nbaDst;
		}else{
			searchVO = searchContract(userVO);
			if (searchVO != null && searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
				List results = searchVO.getSearchResults();
				parentWork = retrieveWorkItem((NbaSearchResultVO) results.get(0),userVO);
			}
		}
		
		appendRoutReason = NbaUtils.getAppendReason(nbaTXLife);
		if(searchResultList != null){
			retrieveLicWorkItem(searchResultList);
		}
		
		if (parentWork !=null) {
			tempUserVO.setUserID(NbaConstants.PROC_AGENT_LIC_WORK_CREATE);
			if (licensingworkExists == false) {
				deoinkMap.put("A_CreateAgentLicWI", "true");
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(tempUserVO, parentWork, nbaTXLife, deoinkMap);
				NbaTransaction aTransaction = parentWork.addTransaction(provider.getWorkType(), provider.getInitialStatus());
				aTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
				// Initialize LOBs of the WI
				aTransaction.getNbaLob().setRouteReason(
				NbaUtils.getStatusTranslation(provider.getWorkType(), provider.getInitialStatus())+" " + appendRoutReason);
				aTransaction.getNbaLob().setCaseManagerQueue(parentWork.getNbaLob().getCaseManagerQueue());
				aTransaction.getNbaLob().setLicCaseMgrQueue(parentWork.getNbaLob().getLicCaseMgrQueue()); //NBLXA-2487
				aTransaction.getNbaLob().setFirstName(parentWork.getNbaLob().getFirstName());
				aTransaction.getNbaLob().setLastName(parentWork.getNbaLob().getLastName());
				aTransaction.getNbaLob().setSsnTin(parentWork.getNbaLob().getSsnTin());
				aTransaction.getNbaLob().setDOB(parentWork.getNbaLob().getDOB());
				aTransaction.getNbaLob().setAgentID(parentWork.getNbaLob().getAgentID());
				aTransaction.getNbaLob().setFaceAmount(parentWork.getNbaLob().getFaceAmount());
				parentWork.setUpdate();
				WorkflowServiceHelper.updateWork(userVO, parentWork);
			} else if (licensingworkExists == true) {
				if (licensingworkInEndQueue == true && searchResultVo !=null) {
					tempUserVO.setUserID(NbaConstants.PROC_AGENT_LIC_WORK_ROUTE);
					deoinkMap.put("A_CreateAgentLicWI", "true");
					NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(tempUserVO, parentWork, nbaTXLife, deoinkMap);
					NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
					NbaLob nbaLob = searchResultVo.getNbaLob();
					retOpt.setWorkItem(searchResultVo.getWorkItemID(), false);
					retOpt.setLockWorkItem();
					NbaDst aWorkItem = WorkflowServiceHelper.retrieveWorkItem(userVO, retOpt);
					aWorkItem.getNbaLob().setRouteReason(
							NbaUtils.getStatusTranslation(searchResultVo.getWorkType(), statusProvider.getPassStatus())+" " + appendRoutReason);
					aWorkItem.getNbaLob().setCaseManagerQueue(nbaLob.getCaseManagerQueue());
					aWorkItem.getNbaLob().setLicCaseMgrQueue(nbaLob.getLicCaseMgrQueue()); //NBLXA-2487
					aWorkItem.getNbaLob().setFirstName(nbaLob.getFirstName());
					aWorkItem.getNbaLob().setLastName(nbaLob.getLastName());
					aWorkItem.getNbaLob().setSsnTin(nbaLob.getSsnTin());
					aWorkItem.getNbaLob().setDOB(nbaLob.getDOB());
					aWorkItem.getNbaLob().setAgentID(nbaLob.getAgentID());
					aWorkItem.getNbaLob().setFaceAmount(nbaLob.getFaceAmount());
					aWorkItem.setStatus(statusProvider.getPassStatus());
					aWorkItem.setUpdate();
					WorkflowServiceHelper.updateWork(userVO, aWorkItem);
					WorkflowServiceHelper.unlockWork(userVO, aWorkItem);
					statusProvider.getPassStatus();
				}
			}
		}
	}

	protected void retrieveLicWorkItem(List searchResultList) {
		for (int i = 0; i < searchResultList.size();i++) {
				searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
				licensingworkExists = true;
				if (searchResultVo.getQueue().equalsIgnoreCase(END_QUEUE)) {
					licensingworkInEndQueue = true;
				}
				break;
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
	


	/*	protected NbaDst retrieveAllChildren(NbaUserVO user, NbaDst parentCase)  throws NbaBaseException {
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
		}*/
		
	public NbaSearchVO searchAgentLicesingWI(NbaUserVO user) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		if (contractKey != null) {
			searchVO.setWorkType(NbaConstants.A_WT_AGENT_LICENSE);
			searchVO.setContractNumber(contractKey);
			searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
			searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		}
		return searchVO;
	}
		public NbaSearchVO searchContract(NbaUserVO user) throws NbaBaseException {
			NbaSearchVO searchVO = new NbaSearchVO();
			searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
			searchVO.setContractNumber(contractKey);
			searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
			searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
			return searchVO;
		}
		public NbaDst retrieveWorkItem(NbaSearchResultVO resultVO,NbaUserVO user) throws NbaBaseException {
			NbaDst aWorkItem = null;
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setNbaUserVO(user);
			retOpt.setWorkItem(resultVO.getWorkItemID(), true);
			//retOpt.setLockWorkItem();
			aWorkItem = WorkflowServiceHelper.retrieveWork(user, retOpt);
			return aWorkItem;
		}
		
	
}
