package com.csc.fsg.nba.datafeed.service;

/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2009 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelService;
import com.csc.fs.dataobject.nba.datafeed.NbaDataFeedDO;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.bean.accessors.Nba302WebService;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.webservice.client.AxaInvokeWebservice;

/**
 * NbaDataFeedService - This services invokes the
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA232</td><td>Version 8</td><td>nbA Feed for a Customer's Web Site</td></tr>
 * <tr><td>AXAL3.7.54</td><td>AXA Life Phase 1</td><td>AXAOnline / AXA Distributors Service</td></tr>
 * <tr><td>ALS4568</td><td>AXA Life Phase 1</td><td>QC # 3622 - 3.7.25 - transmitClientHolding fails with java.lang.OutOfMemoryError</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 *  
 */
public class CommitDataFeedService extends AccelService {

	private String ULBASE = "ULBASE"; //AXAL3.7.54
	private String PERM_PLAN = "022ULEIF"; //AXAL3.7.54
	protected static NbaLogger logger = null; //AXAL3.7.54
	private NbaTXLife request1203DataFeed;
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fs.accel.AccelService#execute(com.csc.fs.Result)
	 */
	public Result execute(Result request) {
		Result result = Result.Factory.create();
		Object dataObject = request.getData().get(0);
		try {
		retrieveNbaTXLife((NbaDataFeedDO) dataObject);
		String token = ((NbaDataFeedDO) dataObject).getToken(); //AXAL3.7.54
		NbaUserVO user = new NbaUserVO();
		Map tokens = new HashMap();
		tokens.put(NbaUserVO.EIB_TOKEN, token);
		user.setTokens(tokens);
		NbaTXLife nbaTXLifeResponse = invokeAXAWebService(user); //AXAL3.7.54
		if (null != nbaTXLifeResponse) {
			result.addResult(nbaTXLifeResponse);
		}
		} catch (Exception t) {
			result.addMessage(23401013, new Object[] {t.getMessage()});
		}
		//AXAL3.7.54 code deleted
		return result;
	}
		

	private void retrieveNbaTXLife(NbaDataFeedDO dataFeedDO)throws Exception {
		
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setAccessIntent(NbaConstants.READ);

		NbaUserVO userVo = new NbaUserVO();
		userVo.setUserID(dataFeedDO.getProcessName());
		userVo.setPassword(dataFeedDO.getProcessPwd());
		nbaTXRequest.setNbaUser(userVo);
		NbaLob lob = new NbaLob();
		lob.setTypeCase(true);
		lob.setPolicyNumber(dataFeedDO.getContractNumber());
		lob.setCompany(dataFeedDO.getCompanyCode());
		lob.setBackendSystem(dataFeedDO.getBackendSystem());
		lob.setOperatingMode(dataFeedDO.getOperatingMode());
		nbaTXRequest.setNbaLob(lob);
		NbaTXLife nbaTXLife;
		nbaTXLife = NbaContractAccess.doContractInquiry(nbaTXRequest);

		userVo.setUserID(dataFeedDO.getUserID());
		userVo.setPassword("NONE");
		nbaTXRequest.setNbaUser(userVo);
		NbaTXLife nbatxlifereq = new NbaTXLife(nbaTXRequest);
		
		//Begin ALS4672 Refactor // ALII308
		Date dataFeedDate = new Date();
		NbaTime dataFeedTime = new NbaTime();
		
		if (!NbaUtils.isBlankOrNull(dataFeedDO.getFeedDate())) {
			dataFeedDate  = dataFeedDO.getFeedDate();
		}
		if (!NbaUtils.isBlankOrNull(dataFeedDO.getFeedTime())) {
			dataFeedTime = dataFeedDO.getFeedTime();
		}
		nbatxlifereq.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeDate(dataFeedDate);
		nbatxlifereq.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeTime(dataFeedTime);
		nbatxlifereq.setOLifE(nbaTXLife.getOLifE());
		//Begin ALS2020
		nbatxlifereq.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE().getSourceInfo().setCreationDate(dataFeedDate); //AXAL3.7.54
		nbatxlifereq.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE().getSourceInfo().setCreationTime(dataFeedTime); //AXAL3.7.54
		NbaUtils.updateDeliveryInstruction(nbatxlifereq);//ALS4542
		request1203DataFeed = nbatxlifereq;
		
		//End ALS2020
        //End ALS4672 Refactor
	}
    //APSL795 method modified
	private NbaTXLife invokeAXAWebService(NbaUserVO user) {
		NbaTXLife nbaTXLifeResponse = null;
		RequirementInfo reqInfo = null;
		RequirementInfoExtension reqInfoExt = null;
		List statusTypeAttachment = new ArrayList();
		com.csc.fsg.nba.vo.txlife.Attachment attachment = null; //NBLXA-1656
		try {
			update1203();
			// begin ALS4568
			int reqCount = request1203DataFeed.getPolicy().getRequirementInfoCount();
			for (int x = 0; x < reqCount; x++) {
				reqInfo = request1203DataFeed.getPolicy().getRequirementInfoAt(x);
				reqInfo.assureAttachmentRetrieved();
				//NBLXA-1656 Starts
				int attachmentCount = reqInfo.getAttachmentCount();
				reqInfo.assureAttachmentRetrieved();
				//if (reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_PHYSSTMT) {  //NBLXA-1777
					for (int attachCount = 0; attachCount < attachmentCount; attachCount++) {
						attachment = reqInfo.getAttachmentAt(attachCount);
						if ((NbaOliConstants.OLI_ATTACH_STATUSCHG) != attachment.getAttachmentType()) {
							getLogger()
									.logDebug("The Attachments removed from the RequirementInfo Object has type " + attachment.getAttachmentType());
							statusTypeAttachment.add(attachment);
						}
					}
					reqInfo.getAttachment().removeAll(statusTypeAttachment);
					reqInfo.setActionUpdate();
				//} else { //NBLXA-1777
					//NBLXA-1656 Ends
				//	reqInfo.setAttachment(new ArrayList());
				//} //NBLXA-1656  //NBLXA-1777

				reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
				if (!NbaUtils.isBlankOrNull(reqInfo.getFormNo()) && reqInfoExt != null) {
					reqInfoExt.setFormNoDescription(NbaUtils.getFormTranslation(reqInfo.getFormNo(), request1203DataFeed));
				}
			}
			// end ALS4568
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("The 1203 Data Feed Transmission Request => " + request1203DataFeed.toXmlString());
			}
			nbaTXLifeResponse = (new AxaInvokeWebservice()).invokeAXAOnlineDistrWS(request1203DataFeed, user);
			if (getLogger().isDebugEnabled() && null != nbaTXLifeResponse) {
				getLogger().logDebug("The 1203 Data Feed Transmission Response => " + nbaTXLifeResponse.toXmlString());
			}

		} catch (Throwable e) {
			NbaLogFactory.getLogger(this.getClass()).logException(e);
		}
		return nbaTXLifeResponse;
	}
	private void update1203() {
		
		request1203DataFeed.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS);
		Policy policy = request1203DataFeed.getPolicy();
		//LineOfBusiness is set based on PPfL.  We do not have PPfL for these plans so set to '1'
		if (!policy.hasLineOfBusiness()) {//P2AXAL020/ALII316
			policy.setLineOfBusiness(1);
		}
		resetTermExpressInd(request1203DataFeed);	//NBLXA186-NBLXA1272
	}
	  /**
     * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * 
     * @return com.csc.fsg.nba.foundation.NbaLogger
     */
    protected static NbaLogger getLogger() {
		if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(Nba302WebService.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log("AXAInvokeWebservice could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }

    //New Method NBLXA186-NBLXA1272 //APSL5391/QC-18810
	protected void resetTermExpressInd(NbaTXLife request1203DataFeed) {
		String producerid = "";
		producerid = NbaUtils.getProducerID(request1203DataFeed);
		if (producerid != null) { // Added for QC19135/NBLXA-1434
			if (isValidAgent(producerid)) {
				ApplicationInfo appInfo = request1203DataFeed.getPolicy().getApplicationInfo();
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
				if (appInfoExt != null) {
					appInfoExt.setTermExpressInd(false);
				}
			}
		}

	}
	 //New Method NBLXA186-NBLXA1272 //APSL5391/QC-18810
	protected boolean isValidAgent(String producerid)  {
		boolean isCharlesBaileyAgentID = false;
		NbaVpmsResultsData agentData = getDataFromVPMSForIdentifyingAgent(NbaVpmsConstants.CONTRACTVALIDATIONCALCULATIONS, NbaVpmsConstants.EP_AGENTVALIDATION,
				producerid);
		if (agentData != null && agentData.getResultsData() != null) {
			isCharlesBaileyAgentID = Boolean.parseBoolean(((String) agentData.getResultsData().get(0)));
		}
		return isCharlesBaileyAgentID;
	}
	 //New Method NBLXA186-NBLXA1272 //APSL5391/QC-18810
	public NbaVpmsResultsData getDataFromVPMSForIdentifyingAgent(String modelName, String entryPoint, String producerID) {
		NbaVpmsAdaptor adapter = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(new NbaLob());
			adapter = new NbaVpmsAdaptor(oinkData, modelName);
			Map deOinkMap = new HashMap();
			deOinkMap.put("A_PRODUCERID", producerID);
			adapter.setSkipAttributesMap(deOinkMap);
			adapter.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(adapter.getResults());
			return vpmsResultsData;
		} catch (Exception e) {
			getLogger().logDebug("Problem in getting valid agent from VPMS" + e.getMessage());
		} finally {
			try {
				if (adapter != null) {
					adapter.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
				th.printStackTrace();
			}
		}
		return null;
	}
}
