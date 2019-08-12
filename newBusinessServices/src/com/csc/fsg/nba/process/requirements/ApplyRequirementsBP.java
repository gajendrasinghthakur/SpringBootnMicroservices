package com.csc.fsg.nba.process.requirements;

/* 
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.bean.accessors.NbaUnderwriterWorkbenchFacadeBean;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaApplyRequirementsRequest;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaEvaluateRequest;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * Class Description.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>PERF-APSL410</td><td>AXA Life Phase1</td><td>PERF - CommitRequirements optimization</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class ApplyRequirementsBP extends NewBusinessAccelBP {

	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaApplyRequirementsRequest request = (NbaApplyRequirementsRequest) input;
			NbaUnderwriterWorkbenchFacadeBean bean = new NbaUnderwriterWorkbenchFacadeBean();
			if (request.isImpairmentAdded()) {//ALS4655 refactor
				reEvaluateContract(request);
			}
			List results = bean.applyRequirements(request.getNbaUserVO(), request.getContract(), request.getWork());
            //Begin APSL1427
			NbaTXLife tx=(NbaTXLife)results.get(1);
			if (tx != null && tx.isTransactionError()) {
				result = processErrors(tx);
			} else {
			request.setWork((NbaDst) results.get(0));
			request.setContract((NbaTXLife) results.get(1));
			unsuspendWork(request.getNbaUserVO(), (Set) results.get(2));
			result.addResult(request);
			}
			//End APSL1427
		} catch (NbaTransactionValidationException ntve) {
			//result.setErrors(true);//ALII1294
			result.addResult(ntve);
			addMessage(result, ntve.getMessage());//ALII1294
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}

	/**
	 * 
	 * @param request
	 * @throws NbaBaseException
	 */
	protected void reEvaluateContract(NbaApplyRequirementsRequest request) throws NbaBaseException {
		NbaEvaluateRequest evaluateReq = new NbaEvaluateRequest();
		evaluateReq.setNbaUserVO(request.getNbaUserVO());
		evaluateReq.setWork(request.getWork());
		evaluateReq.setContract(request.getContract());
		evaluateReq.setUserFunction("NBEVLIMP");
		evaluateReq.setOverrideContractCommit(true);//ALS4655 refactor
		evaluateReq.setUnderwritingWB(true);//ALS3972
		evaluateReq.setResetUWWB(true);//ALS3972
		AccelResult result = (AccelResult) callService("GenerateEvaluateWorkItemBP", evaluateReq);
		if (!processResult(result)) {
			evaluateReq = (NbaEvaluateRequest) result.getFirst();
			request.setWork(evaluateReq.getWork());
			request.setContract(evaluateReq.getContract());
		}
	}

	/**
	 * 
	 * @param user
	 * @param unsuspendSet
	 */
	protected void unsuspendWork(NbaUserVO user, Set unsuspendSet) {
		NbaSuspendVO request = new NbaSuspendVO();
		request.setNbaUserVO(user);
		request.setRetrieveWorkItem(false); //PERF-APSL410
		Iterator iterator = unsuspendSet.iterator();
		while (iterator.hasNext()) {
			request.setTransactionID((String) iterator.next());
			callService("NbaUnsuspendWorkBP", request);
		}
	}
	
//	Added new method for APSL1427
	/**
	 * Processes errors retrieved from the back end system and creates a new AccelResult
	 * to place them in so they can be returned to the caller.
	 * @param contract
	 * @return
	 */
	protected AccelResult processErrors(NbaTXLife contract) {
		AccelResult result = new AccelResult();
		TransResult transResult = contract.getTransResult();
		if (transResult != null) {
			addMessage(result, "System Error Occurred - Please click on OK and try again to add requirement. \n\n");
			Message msg = new Message();
			int count = transResult.getResultInfoCount();
			String[] messages = new String[count];
			for (int i = 0; i < count; i++) {
				ResultInfo resultInfo = transResult.getResultInfoAt(i);
				messages[i] = resultInfo.getResultInfoDesc();
			}
			msg = msg.setVariableData(messages);
			result.setErrors(true);
			result.addMessage(msg);
		}
		return result;
	}
}
