package com.csc.fsg.nba.process.notification;

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

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.database.NbaWebServiceProcessAccessor;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponse;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * ProccessPrintPreview  webservice  accept the request from  the ePolicy for the 
 * update print GUID and store the GUID in pend database to show the preview to the user.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>APSL5100</td>
 * <td>Discretionary</td>
 * <td>Policy Print Preview</td>
 * </tr>
 * </td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 1201
 * @since New Business Accelerator - Version 1201
 */

public class ProcessPrintPreviewWebServiceBP extends NewBusinessAccelBP {
	protected NbaLogger logger = null;

	String polNumber = "";

	String workItemId = null;

	String printGuid = null;

	boolean errorFlag = true;
	protected static final String PRINT_PREVIEW = "PrintPreview";

	public Result process(Object input) {

		NbaTXLife request = (NbaTXLife) input;
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Print Preview Request: " + request.toXmlString());
		}
		NbaTXLife response = createResponse();
		try {

			// Sending Failure Response if policy number does not exist in request.
			polNumber = request.getPolicy().getPolNumber();
			if (NbaUtils.isBlankOrNull(polNumber)) {
				handleException(response, "Policy number does not exist");
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Response: " + response.toXmlString());
				}
				return new AccelResult().addResult(response);
			}

			// Retrieving print CRDA and print GUID from request.
			if (request.getPolicy().getOLifEExtensionCount() > 0) {
				for (int i = 0, j = request.getPolicy().getOLifEExtensionCount(); i < j; i++) {
					if (request.getPolicy().getOLifEExtensionAt(i).isPolicyExtension()) {
						PolicyExtension policyExtn = request.getPolicy().getOLifEExtensionAt(i).getPolicyExtension();
						if (policyExtn != null && policyExtn.getEPolicyData().size() > 0) {
							workItemId = policyExtn.getEPolicyDataAt(0).getPrintCRDA();
							printGuid = policyExtn.getEPolicyDataAt(0).getEPolicyPrintID();
							break;
						}
					}
				}
			}

			// Sending Failure Response if print CRDA does not exist in request.

			if (NbaUtils.isBlankOrNull(workItemId)) {
				handleException(response, "Print CRDA  does not exist");
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Response: " + response.toXmlString());
				}
				return new AccelResult().addResult(response);
			}

			// Sending Failure Response if print GUDI does not exist in request.

			if (NbaUtils.isBlankOrNull(printGuid)) {
				handleException(response, "Epolicy Print GUID does not exist");
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Response: " + response.toXmlString());
				}
				return new AccelResult().addResult(response);
			}

			
			NbaWebServiceProcessAccessor.insert(polNumber, PRINT_PREVIEW , request.toXmlString());
			
		} catch (Throwable t) {
			handleException(response, t.getMessage());
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Response: " + response.toXmlString());
		}
		return new AccelResult().addResult(response);
	}



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
		tXLifeResponse.setTransType(NbaOliConstants.TC_TYPE_CASESTATNOTIFTRN);
		tXLifeResponse.setTransExeDate(new Date());
		tXLifeResponse.setTransExeTime(new NbaTime());
		tXLifeResponse.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		TransResult transResult = new TransResult();
		transResult.setResultCode(NbaOliConstants.TC_RESCODE_SUCCESS);
		tXLifeResponse.setTransResult(transResult);
		return nbaTXLife;
	}

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
}