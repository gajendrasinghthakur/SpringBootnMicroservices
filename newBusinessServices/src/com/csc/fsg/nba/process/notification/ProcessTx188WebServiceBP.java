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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.util.GUID;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
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
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.PolicyMessage;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponse;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

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

public class ProcessTx188WebServiceBP extends NewBusinessAccelBP {

	protected NbaLogger logger = null;
	NbaSearchVO searchVO = null;
	public static final String GENERIC_PROVIDER = "PRDC"; // NBLXA-1822
	public Result process(Object input) {
		
		NbaTXLife request = (NbaTXLife) input;
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("TX188 Request: " + request.toXmlString());
		}
		Result result = new AccelResult();
		String providerID = null;

		NbaTXLife response = createResponse();
		try {
			String policyNumber = request.getPolicy().getPolNumber();

			if (policyNumber == null) {
				handleException(response, "Policy number does not exist");
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Response: " + response.toXmlString());
				}
				return new AccelResult().addResult(response);
			}
			// NBLXA-1822 Starts
			if (!NbaUtils.isBlankOrNull(request.getTXLife().getUserAuthRequestAndTXLifeRequest().getUserAuthRequest())
					&& !NbaUtils.isBlankOrNull(request.getTXLife().getUserAuthRequestAndTXLifeRequest().getUserAuthRequest().getVendorApp()
							.getVendorName())) {
				providerID = request.getTXLife().getUserAuthRequestAndTXLifeRequest().getUserAuthRequest().getVendorApp().getVendorName().toString();
			} else {
				providerID = GENERIC_PROVIDER;
			}
			// Intercept the incoming requirement result
			if (!NbaUtils.isBlankOrNull(request.getPolicy().getRequirementInfo())) 
			{
				result = generateAgentFileResponse(request, result, providerID);
			} else 
			{
				PolicyExtension polExtension = NbaUtils.getPolicyExtension(request.getPolicy());
				if (polExtension != null) {
					PolicyMessage polMessage = polExtension.getPolicyMessageAt(0);
					if (polMessage != null) {
						GUID guid = new GUID();
						String policyMessageID = guid.getKeyString();
						String policyMessageDateTimeStamp = polMessage.getDateTimeStamp();
						String policyMessageText = polMessage.getMessageText();
						String policySenderName = polMessage.getSenderName();
						long policyAgentResponseCode = polMessage.getAgentResponseIndCode();
						String policyRequirementInfoUniqueID = polMessage.getRequirementInfoUniqueID();//NBLXA -1794
						// This will insert the data into the NBAAuxiliary database
						NbaSystemDataDatabaseAccessor.insertAgentMessage(policyMessageID, policyMessageText, policyMessageDateTimeStamp,
								policyNumber, policySenderName, policyAgentResponseCode,policyRequirementInfoUniqueID);//NBLXA -1794


					}
				}
			}
			// NBLXA-1822 Ends
		}
		catch (NbaBaseException e) {
			if (e.getMessage().equalsIgnoreCase("The requested work item is currently locked by another user")) {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Response: " + response.toXmlString());
				}
				return new AccelResult().addResult(response);
			}
			handleException(response, e.getMessage());
		} catch (IOException e) {

			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Response: " + response.toXmlString());
			}
			return new AccelResult().addResult(response);

		}
		

		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Response: " + response.toXmlString());
		}
		return new AccelResult().addResult(response);
	}

	private Result generateAgentFileResponse(NbaTXLife request, Result result, String providerID) throws IOException {
		String GUID;
		Vector errors;
		OutputStream fileOut = null;
		try {
			String path = null;
			try {
				path = NbaConfiguration.getInstance().getFileLocation(providerID + "Rip");
			} catch (Exception exp) {
				getLogger().logException(exp);
				errors = new Vector();
				return result.addResult(createResponse());
			}
			// Deliver the transformed provider requirement result to DocumentInput
			GUID = request.getTransRefGuid();
			SimpleDateFormat GUID_SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String datetime = GUID_SDF.format(new Date());
			File xmlFile = new File(path, GUID + ".xml");
			if (xmlFile.exists()) {
				xmlFile = new File(path, GUID + "_" + datetime + ".xml");
			}
			fileOut = new FileOutputStream(xmlFile);
			String output = request.toXmlString();
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("TX188WebServiceBP Finished Request: " + output);
			}
			fileOut.write(output.getBytes());
			fileOut.close();
		} catch (Exception exp) {
			NbaLogFactory.getLogger(this.getClass()).logException(exp);
			exp.printStackTrace();
			errors = new Vector();
			return result.addResult(createResponse());
		}
		finally
		{
			getLogger().logDebug("TX188WebServiceBP Finished Request: Closing the File Stream");
			fileOut.close();
		}

		return result;
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