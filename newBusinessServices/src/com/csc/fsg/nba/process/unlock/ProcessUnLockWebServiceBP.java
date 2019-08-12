package com.csc.fsg.nba.process.unlock;

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
 *     Copyright (c) 2002-2012 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */

import java.util.Date;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthResponse;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>APSL5203</td>
 * <td>Discretionary</td>
 * <td>Producer Communication</td>
 * </tr>
 * </td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 1201
 * @since New Business Accelerator - Version 1201
 */

public class ProcessUnLockWebServiceBP extends NewBusinessAccelBP {

	protected NbaLogger logger = null;
	NbaSearchVO searchVO = null;

	public Result process(Object input) {
		NbaTXLife request = (NbaTXLife) input;
		System.out.println("UnLock Request: " + request.toXmlString());
		
		NbaTXLife response = createResponse();
		try {
			NbaSearchVO searchWI = new NbaSearchVO();
			Policy policy = request.getPolicy();
			String polNumber = policy.getPolNumber();
			ApplicationInfo appInfo = policy.getApplicationInfo();
			searchWI.setContractNumber(polNumber);
			searchWI.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
			searchWI.getNbaLob().setLob(NbaConstants.LOCKEDBY, appInfo.getUserCode());
			searchWI = WorkflowServiceHelper.lookupWork(request.getUser(), searchWI);
			String company=null;
			String backend=null;
			
			for (int i = 0; i < searchWI.getSearchResults().size(); i++) {
				NbaSearchResultVO resultVO = (NbaSearchResultVO) searchWI.getSearchResults().get(i);
				NbaDst work = new NbaDst();
				
				if(resultVO.getCompany()!=null)
				company=resultVO.getCompany();
				
				if(resultVO.getBackendSystem()!=null)
				backend=resultVO.getBackendSystem();
		
				work.setWorkItem(resultVO.getWorkItem());
				work.setID(resultVO.getWorkItemID());
				work.setUserId(appInfo.getUserCode());
				UserAuthRequest userAuthRequest = request.getUserAuthRequest();
				userAuthRequest.getUserLoginNameAndUserPswdOrUserSessionKey().getUserLoginNameAndUserPswd().setUserLoginName(appInfo.getUserCode());
				WorkflowServiceHelper.unlockWork(request.getUser(), work);
	
			}
			
			if(searchWI.getSearchResults().size()>0)
				NbaContractLock.removeLock(polNumber,company,backend);
			
		} catch (NbaBaseException e) {
			if (e.getMessage().equalsIgnoreCase("The requested work item is currently locked by another user")) {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Response: " + response.toXmlString());
				}
				return new AccelResult().addResult(response);
			}
			handleException(response, e.getMessage());
		} finally {
		}

		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Response: " + response.toXmlString());
		}
		return new AccelResult().addResult(response);
	}

	/**
	 * @Purpose This method will create the response for the request
	 * @return
	 */
	protected NbaTXLife createResponse() {
		NbaTXLife nbaTXLife = new NbaTXLife();
		nbaTXLife.setTXLife(new TXLife());
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify ua = new UserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		nbaTXLife.getTXLife().setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(ua);
		ua.setUserAuthResponse(new UserAuthResponse());
		ua.getUserAuthResponse().setSvrDate(new Date());
		ua.getUserAuthResponse().setSvrTime(new NbaTime());
		TXLifeResponse tXLifeResponse = new TXLifeResponse();
		ua.addTXLifeResponse(tXLifeResponse);
		tXLifeResponse.setTransRefGUID(NbaUtils.getGUID());
		tXLifeResponse.setTransType(NbaOliConstants.TC_TYPE_ADD_UPDATE_MESSAGE);
		tXLifeResponse.setTransExeDate(new Date());
		tXLifeResponse.setTransExeTime(new NbaTime());
		tXLifeResponse.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		TransResult transResult = new TransResult();
		transResult.setResultCode(NbaOliConstants.TC_RESCODE_SUCCESS);
		tXLifeResponse.setTransResult(transResult);
		return nbaTXLife;
	}

	/**
	 * @Purpose This method will handle the exception
	 * @param nbaTXLife
	 * @param text
	 */
	protected void handleException(NbaTXLife nbaTXLife, String text) {
		TXLifeResponse response = nbaTXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0);
		response.getTransResult().setResultCode(NbaOliConstants.TC_RESCODE_FAILURE);
		ResultInfo resultInfo = new ResultInfo();
		resultInfo.setResultInfoCode(NbaOliConstants.TC_RESINFO_GENERALDATAERR);
		resultInfo.setResultInfoDesc(text);
		response.getTransResult().addResultInfo(resultInfo);
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
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

	/**
	 * Create a TX Request value object that will be used to retrieve the contract.
	 * 
	 * @param nbaDst
	 *            the workitem object for that holding request is required
	 * @param access
	 *            the access intent to be used to retrieve the data, either READ or UPDATE
	 * @param businessProcess
	 *            the name of the business function or process requesting the contract
	 * @return a value object that is the request
	 */
	// ACN026 New Method
	// CR61627-PERF throw NbaBaseException
	public NbaTXRequestVO createRequestObject(NbaDst nbaDst, int access, String businessProcess, NbaUserVO user) {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
		nbaTXRequest.setNbaLob(nbaDst.getNbaLob());
		nbaTXRequest.setNbaUser(user);
		nbaTXRequest.setWorkitemId(nbaDst.getID());
		nbaTXRequest.setCaseInd(nbaDst.isCase());
		if (access != -1) {
			nbaTXRequest.setAccessIntent(access);
		}
		if (businessProcess != null) {
			nbaTXRequest.setBusinessProcess(businessProcess);
		}
		return nbaTXRequest;
	}

	// New Method 5203
	/**
	 * Use the <code>NbaProcessStatusProvider</code> to determine the work item's next status. If a new status is returned back from the VP/MS model,
	 * then only update the transaction's status.
	 * 
	 * @param work
	 * @param userID
	 * @param nbaTXLife
	 * @param deOinkMap
	 * @throws NbaBaseException
	 */
	protected void setStatus(NbaDst work, String userID, NbaTXLife nbaTXLife, Map deOinkMap) throws NbaBaseException {
		NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(userID, ""), work, nbaTXLife, deOinkMap);
		work.setStatus(provider.getPassStatus());
		work.increasePriority(provider.getCaseAction(), provider.getCasePriority());
		if (work.getWorkItem().hasNewStatus()) {
			NbaUtils.setRouteReason(work, work.getStatus(), provider.getReason());
		}
	}

	public NbaSearchVO searchContract(String contractKey,NbaUserVO user) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		return searchVO;
	}

	public NbaDst retrieveWorkItem(NbaSearchResultVO resultVO, NbaUserVO user) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setNbaUserVO(user);
		retOpt.setWorkItem(resultVO.getWorkItemID(), true);
		retOpt.setLockWorkItem();
		aWorkItem = WorkflowServiceHelper.retrieveWork(user, retOpt);
		return aWorkItem;
	}
	
}