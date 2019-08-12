package com.csc.fsg.nba.process.contract.approval;

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

import java.util.HashMap;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaContractApprovalDispositionRequest;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaVpmsRequestVO;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/**
 * This business process is responsible for refering work to queue selected by user in underwriter 
 * workbench refer popup. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class CommitReferBP extends CommitFinalDispositionBP {
	
	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaContractApprovalDispositionRequest request = (NbaContractApprovalDispositionRequest) input;
			applyReferWork(request);
			result.addResult(request);
		} catch (NbaBaseException nbe) {
			addExceptionMessage(result, nbe); 
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}
	
	/**
	 * This method refers the work to the selected underwriter queue
	 * @param request NbaContractApprovalDispositionRequest
	 * @return NbaContractApprovalDispositionRequest containing NbaDst returned from update work business process service call
	 * @throws NbaBaseException
	 */
	protected NbaContractApprovalDispositionRequest applyReferWork(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
		NbaVpmsRequestVO vpmsRequestVO = retrieveBusinessRulesDataForUnderwriterQueue(request);
		updateWork(vpmsRequestVO, request);
		AccelResult result = (AccelResult) callBusinessService("NbaUpdateWorkBP", request.getWork());
		if (result.hasErrors()) {
			throw new NbaBaseException(NbaBaseException.UPDATE_WORK);
		}
		// Begin: NBLXA2744 One View
		NbaTXLife txlife = request.getContract();
		Activity act = NbaUtils.getOutStandingRefActivityOneView(txlife);
		if (!NbaUtils.isBlankOrNull(act)) {
			txlife.setBusinessProcess(NbaConstants.REFER_ONEVIEW);
			doContractUpdate(txlife, (NbaDst) result.getFirst(), request.getNbaUserVO());
		}
		//End : NBLXA2744 One View
		request.setWork((NbaDst) result.getFirst());
		return request;
	}
	
	/**
	 * Call the business process to retrieve the results from the VPMS model for referred underwriter queue
	 * @param request NbaContractApprovalDispositionRequest
	 * @return NbaVpmsRequestVO
	 * @throws NbaBaseException
	 */
	protected NbaVpmsRequestVO retrieveBusinessRulesDataForUnderwriterQueue(NbaContractApprovalDispositionRequest request) throws NbaBaseException {
		Map deOink = new HashMap();
		deOink.put("A_UnderwriterQueue", request.getReferredWorkUndQueue());
		deOink.put(NbaVpmsConstants.A_PROCESS_ID, NbaVpmsConstants.UISTATUS_UNDERWRITER_WORKBENCH);
		NbaVpmsRequestVO vpmsRequestVO = new NbaVpmsRequestVO();
		vpmsRequestVO.setModelName(NbaVpmsConstants.AUTO_PROCESS_STATUS);
		vpmsRequestVO.setEntryPoint(NbaVpmsConstants.EP_WORKITEM_STATUSES);
		vpmsRequestVO.setNbATXLife(request.getContract());
		vpmsRequestVO.setNbaLob(request.getWork().getNbaLob());
		vpmsRequestVO.setDeOinkMap(deOink);

		AccelResult result = (AccelResult) callBusinessService("RetrieveDataFromBusinessRulesBP", vpmsRequestVO);
		if (result.hasErrors()) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS);
		}
		return (NbaVpmsRequestVO) result.getFirst();
	}

	/**
	 * Update the original work item from managed bean.
	 * @param vpmsRequestVO NbaVpmsRequestVO
	 * @param request NbaContractApprovalDispositionRequest
	 */
	protected void updateWork(NbaVpmsRequestVO vpmsRequestVO, NbaContractApprovalDispositionRequest request) {
		NbaDst nbaDst = request.getWork();
		nbaDst.setNbaUserVO(request.getNbaUserVO());
		nbaDst.setStatus(vpmsRequestVO.getPassStatus());
		NbaUtils.setRouteReason(nbaDst, vpmsRequestVO.getPassStatus(),vpmsRequestVO.getReason()); //ALS5260
		if(!NbaUtils.isBlankOrNull(vpmsRequestVO.getReason())){ //ALS5337
		    NbaUtils.addGeneralComment(nbaDst,request.getNbaUserVO(),vpmsRequestVO.getReason());//ALS5337
		}//ALS5337
		String priority = NbaUtils.isBlankOrNull(request.getReferredWorkPriority()) ? vpmsRequestVO.getCasePriority() : request
				.getReferredWorkPriority();
		nbaDst.increasePriority(vpmsRequestVO.getCaseAction(), priority);
		nbaDst.setUpdate();
	}
}
