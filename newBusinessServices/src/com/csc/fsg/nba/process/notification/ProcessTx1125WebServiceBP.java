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

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaScorDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.SecureComment;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponse;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * <tr><td>SR787006-APSL3702</td><td>Discretionary</td><td>Simplified Issue Phase 2</br>
 * <tr><td>APSL3878</td><td>Discretionary</td><td>Update routing reason while withdrawing</br>
 * </td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 1201
 * @since New Business Accelerator - Version 1201
 */

public class ProcessTx1125WebServiceBP extends NewBusinessAccelBP {

	protected NbaLogger logger = null;
	
	public Result process(Object input) {
		NbaTXLife request = (NbaTXLife)input;
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("TX1125 Request: " + request.toXmlString());
		}
		NbaTXLife response = createResponse();
		try {
			String workItemId = NbaScorDatabaseAccessor.selectRecordCRDA(request.getPolicy().getPolNumber()).getWorkItemId();
			if (workItemId == null) {
				handleException(response, "Policy number does not exist");
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Response: " + response.toXmlString());
				}
				return new AccelResult().addResult(response);
			}
			
			NbaAwdRetrieveOptionsVO retOptVO = new NbaAwdRetrieveOptionsVO();
			retOptVO.setWorkItem(workItemId, true);
			retOptVO.setNbaUserVO(request.getUser());
			retOptVO.setLockWorkItem();
			//Get work details from awd
			NbaDst work = WorkflowServiceHelper.retrieveWork(request.getUser(), retOptVO);
			
			NbaSource source =
				new NbaSource(work.getBusinessArea(), NbaConstants.A_ST_XML1125, request.toXmlString());
			work.addNbaSource(source);
			work.setNbaUserVO(request.getUser());
			if (request.getTransSubType() == NbaOliConstants.TC_SUBTYPE_CASESTATUS_CASESTATNOTIFTRN
					&& request.getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_EXPIRED) { // APSL3266 
				//Removed Underwiter Queue Condition - SR787006-APSL3702-QC12091
				NbaTXLife nbaTXLife = NbaContractAccess.doContractInquiry(createRequestObject(work, NbaConstants.READ, NbaConstants.PROC_SPN,
						request.getUser()));
				ApplicationInfo appInfo = nbaTXLife.getPolicy().getApplicationInfo();
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
				if (!appInfo.hasHOCompletionDate()) {
					appInfo.setHOCompletionDate(new Date());
					appInfo.setActionUpdate();
				}
				NbaLob lob = work.getNbaLob();
				lob.setCaseFinalDispstn((int) NbaOliConstants.OLI_POLSTAT_WITHDRAW);
				if (appInfoExt.getReopenDate() != null) {
					appInfoExt.deleteReopenDate();
				}
				appInfoExt.setUnderwritingStatus(NbaOliConstants.OLI_POLSTAT_WITHDRAW);
				appInfoExt.setActionUpdate();
				nbaTXLife.setAccessIntent(NbaConstants.UPDATE);
				//Begin APSL5183 -Case will be routed to Underwriter, prior to auto closure to determine if MIB codes are required to be added
				if (appInfoExt.getSCORUnderWritingDecision() == NbaUtils.AXA_SCORDECISION_REFFERD) {
					HashMap deOink = new HashMap();
					NbaOLifEId nbaOLifEId = null;
					String comment = "Case routed to Underwriter prior to auto closure to determine if MIB codes are required to be added. Please add MIB codes if required.";
					nbaOLifEId = new NbaOLifEId(nbaTXLife);
					Attachment attachment = new Attachment();
					nbaOLifEId.setId(attachment);
					attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE);
					attachment.setActionAdd();
					attachment.setDateCreated(new Date());
					attachment.setUserCode(request.getUser().getUserID());
					AttachmentData attachmentData = new AttachmentData();
					attachmentData.setTc("8");

					SecureComment secure = new SecureComment();
					secure.setComment(comment);
					secure.setUserNameEntered(request.getUser().getUserID());
					secure.setAutoInd(true);
					attachmentData.setPCDATA(toXmlString(secure));

					attachmentData.setActionAdd();
					attachment.setAttachmentData(attachmentData);
					nbaTXLife.getPrimaryHolding().addAttachment(attachment);
					VpmsComputeResult data = getDataFromVpms(NbaVpmsAdaptor.EP_WORKITEM_STATUSES, NbaVpmsAdaptor.AUTO_PROCESS_STATUS, deOink, work,
							nbaTXLife);
					NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(data);
					String passStatus = statusProvider.getPassStatus();
					if (!NbaUtils.isBlankOrNull(passStatus) && !passStatus.equals("-")) {
						work.setStatus(passStatus);
						work.getNbaLob().setRouteReason("See Automated Comments");// APSL3878
					} else {
						work.setStatus(NbaConstants.FINAL_DISPOSITION_DONE);
						work.getNbaLob().setRouteReason(NbaUtils.getRouteReason(work, work.getStatus()));
					}

				} else { // End APSL5183
					work.setStatus(NbaConstants.FINAL_DISPOSITION_DONE);
					work.getNbaLob().setRouteReason(NbaUtils.getRouteReason(work, work.getStatus()));
				}//APSL3878
				NbaContractAccess.doContractUpdate(nbaTXLife, work, request.getUser());
				work = WorkflowServiceHelper.updateWork(request.getUser(), work);
				WorkflowServiceHelper.unlockWork(request.getUser(), work);
				NbaContractLock.removeLock(work, request.getUser());//APSL3603(QC13073)
				// Delete the Record from AUX table after processing is completed successfully.
				NbaScorDatabaseAccessor.deleteRecord(work.getNbaLob().getPolicyNumber());//SR787006-APSL3702-QC12091
			} else {
				work = WorkflowServiceHelper.updateWork(request.getUser(), work);
				WorkflowServiceHelper.unlockWork(request.getUser(), work);
				NbaSuspendVO suspendVo = new NbaSuspendVO();
				suspendVo.setNbaUserVO(request.getUser());
				suspendVo.setCaseID(workItemId);
				WorkflowServiceHelper.unsuspendWork(request.getUser(), suspendVo);//SR787006-APSL3702-QC12091
			}
		} catch (NbaBaseException e) {
			handleException(response, e.getMessage());
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
	 * @param nbaDst the workitem object for that holding request is required
	 * @param access the access intent to be used to retrieve the data, either READ or UPDATE
	 * @param businessProcess the name of the business function or process requesting the contract
	 * @return a value object that is the request
	 */
	// ACN026 New Method
	//CR61627-PERF throw NbaBaseException
	public NbaTXRequestVO createRequestObject(NbaDst nbaDst, int access, String businessProcess,NbaUserVO user) {
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
	
	//APSL5183 New method 
	public VpmsComputeResult getDataFromVpms(String entryPoint, String model, Map deOink, NbaDst work, NbaTXLife nbaTXLife) throws NbaBaseException,
			NbaVpmsException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(work);
			oinkData.setContractSource(nbaTXLife);
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, "NTONOTFCTN");
			vpmsProxy = new NbaVpmsAdaptor(oinkData, model);
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			vpmsProxy.setSkipAttributesMap(deOink);
			return vpmsProxy.getResults();

		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("Error in getting VPMS data from  VPMS model " + model + " :"
					+ NbaVpmsException.VPMS_EXCEPTION, re);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}

	}
    
	//APSL5183 New method
	protected String toXmlString(SecureComment secureComment) {
		String xml = "";
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		if (stream != null) {
			secureComment.marshal(stream);
			xml = stream.toString();
			try {
				stream.close();
			} catch (java.io.IOException e) {
			}
		}
		return (xml);
	}
}