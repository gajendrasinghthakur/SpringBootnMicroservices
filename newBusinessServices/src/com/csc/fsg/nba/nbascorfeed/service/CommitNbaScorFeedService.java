package com.csc.fsg.nba.nbascorfeed.service;

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
 *     Copyright (c) 2002-2009 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.ServiceContext;
import com.csc.fs.UserSessionController;
import com.csc.fs.UserSessionKey;
import com.csc.fs.accel.AccelService;
import com.csc.fs.dataobject.nba.nbascorfeed.NbaScorFeedDO;
import com.csc.fs.session.UserSessionControllerFactory;
import com.csc.fsg.nba.database.NbaScorDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaScorSubmitContractVO;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
import com.csc.fsg.nba.webservice.invoke.AxaWSSCORInvoker;

/**
 * NbaDataFeedService - This services invokes the
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> 
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808-NBA-SCOR</td><td>AXA Life</td><td>Simplified Issue</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 *  
 */
public class CommitNbaScorFeedService extends AccelService {

	private static final int _5Min = 300;
	private static final int _30Sec = 30000;
	private static final int INIT_OCC = 0;	
	private static final int RETRY_FREQ_ONE = 1;	
	private static final String correctQueue = "N2SAUUND";
	private static final String optName = AxaWSSCORInvoker.WS_OP_RETRIEVE_SCOR;

	public Result execute(Result request) {
		Result result = Result.Factory.create();
		NbaScorFeedDO nbaScorFeedDO = (NbaScorFeedDO) request.getData().get(0);
		try {
			NbaUserVO user = new NbaUserVO();
			user.setUserID(nbaScorFeedDO.getUserName());
			user.setPassword(nbaScorFeedDO.getPassword());
			Map tokens = new HashMap();
			tokens.put(NbaUserVO.EIB_TOKEN, nbaScorFeedDO.getToken());
			user.setTokens(tokens);
			result.addResult(doProcess(user, nbaScorFeedDO));
		} catch (Exception e) {
			result.addMessage(23401013, new Object[] { e.getMessage() });
		}
		return result;
	}

	private Boolean doProcess(NbaUserVO user, NbaScorFeedDO nbaScorFeedDO) throws NbaBaseException {
		boolean isRespReady = false;
		NbaScorSubmitContractVO contVO = new NbaScorSubmitContractVO();
		contVO.setContractKey(nbaScorFeedDO.getContractNumber());
		for (int wsTry = 0; wsTry <= 4; wsTry++) {
			Object wsResp = AxaWSInvokerFactory.createWebServiceRequestor(optName, user, null, null, contVO.getContractKey()).execute();
			TransResult transResult = ((NbaTXLife) wsResp).getTransResult();
			//If Response is not ready at SCOR
			if (transResult != null && transResult.getResultCode() == NbaUtils.TC_RESCODE_SUCCESSINFO) {
				if (wsTry == 4) {
					contVO.setOccurance(INIT_OCC);
					contVO.setRetryFrequency(RETRY_FREQ_ONE);
					contVO.setNextPollTime(NbaUtils.addSecondsToTimestamp(new Timestamp(new Date().getTime()), _5Min));
					NbaScorDatabaseAccessor.updateNextPollTime(contVO);
				} else {
					try {
						Thread.currentThread().sleep(_30Sec);
					} catch (InterruptedException e) {
						throw new NbaBaseException(e.getMessage());
					}
				}
			} else {
				contVO.setWebServiceResponse(((NbaTXLife) wsResp).toXmlString());
				retrieveAndUnsuspendMatchingCase(user, nbaScorFeedDO);
				NbaScorDatabaseAccessor.updateRecordResponse(contVO);
				isRespReady = true;
				break;
			}
		}
		return new Boolean(isRespReady);
	}

	private void retrieveAndUnsuspendMatchingCase(NbaUserVO user, NbaScorFeedDO nbaScorFeedDO) throws NbaBaseException {
		System.out.println("Starting process inside retrieveAndUnsuspendMatchingCase(): " + nbaScorFeedDO.getWorkitemID());
		UserSessionKey sessionKey = UserSessionKey.create(nbaScorFeedDO.getUserSessionKey());
		Result result = UserSessionControllerFactory.create(sessionKey);
		if (!result.hasErrors()) {
			ServiceContext.dispose();
			ServiceContext.create((UserSessionController) result.getFirst());
		}
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(nbaScorFeedDO.getWorkitemID(), true);
		NbaDst work = WorkflowServiceHelper.retrieveWorkItem(user, retOpt, true);
		if (work.isSuspended() && correctQueue.equalsIgnoreCase(work.getQueue())) {
			NbaSuspendVO suspendVO = new NbaSuspendVO();
			suspendVO.setCaseID(nbaScorFeedDO.getWorkitemID());
			//Change for ALII1929(QC11445)
			//suspendVO.setRetrieveWorkItem(false);
			WorkflowServiceHelper.unsuspendWork(user, suspendVO);
		}
		WorkflowServiceHelper.unlockWork(user, work);
		System.out.println("End process inside retrieveAndUnsuspendMatchingCase(): " + nbaScorFeedDO.getWorkitemID());
	}
}