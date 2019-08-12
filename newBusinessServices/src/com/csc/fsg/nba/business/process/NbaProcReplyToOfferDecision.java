package com.csc.fsg.nba.business.process;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group�.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Message;
import com.csc.fs.ResultBase;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.ui.BaseServiceAction;
import com.csc.fs.accel.ui.ServiceDelegator;
import com.csc.fs.accel.ui.ServiceDelegator.ServiceConfig;
import com.csc.fs.accel.ui.util.XMLUtils;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.AXAMessageCenterVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaContractApprovalDispositionRequest;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaReopenCaseRequest;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.SecureComment;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.PolicyMessage;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;



public class NbaProcReplyToOfferDecision extends NbaAutomatedProcess implements NbaOliConstants {
	
	ServiceConfig currentConfig;
	Map deOink = new HashMap();
	long agentRespInd = 0;
	String policyNum = null;
	boolean isReplyToOfferExpiredInd = false;
	Map<String, List> expiredMedicalRequirementMap = new HashMap();
	StringBuilder medicalRequirements = new StringBuilder();
	String expiredNoOfDays = "0";
	boolean debugLogging = getLogger().isDebugEnabled();
	public static final String ROUTE_REASON_REISSUE = "Reply to Offer received on Reissue case,Case Routed for review";
	public static final String ROUTE_REASON_SURVIVORSHIP = "Reply to Offer received on Survivorship case,Case Routed for review";
	public static final String ROUTE_REASON_NON_UW = "Reply to Offer received, Routed for review";
	public static final String ROUTE_REASON_LOCKED = "Reply to Offer received,Case Locked for more than 2 hours";
	public static final String ROUTE_REASON_INCOMPLETE = "Reply to Offer received on case with Incomplete status";
	public static final String COMMENT_ACCEPT_RESPONSE = "Reply to Offer received and accepted.  nbA automatically approved.";
	public static final String COMMENT_REJECT_RESPONSE = "Reply to Offer received and rejected by agent.  nbA automatically withdrawn the case.";
	public static final String ROUTE_REASON_EXP_MEDICAL_REQ = "Reply to Offer received but medical requirement(s) out of date-see comments";
	public static final String COMMENT_AUTOAPPROVAL_FAILED = "Reply to Offer Received.  Auto-approval failed.";
	public static final String ROUTE_REASON_REOPEN = "Reply to Offer received on ReOpen case,Case Routed for review";
	
	
	/**
	 * Process the Agent Response for reply to offer requirement based on agentRespInd
	 * - Send the request to the adaptor for processing.
	 * - Check for transmission errors
	 * - Change status for the case
	 * - Update AWD
	 * - Updates nbAuxillary database
	 * @param user the user for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		String msgID=null;
		NbaSearchVO searchVO = null;
		AXAMessageCenterVO agentMsgProcRes=null;
		NbaContractApprovalDispositionRequest request = new NbaContractApprovalDispositionRequest();
		setUser(user);
		NbaDst nbaDst=null;
		List results = null;
		List<AXAMessageCenterVO> agentMessageProcVO=NbaSystemDataDatabaseAccessor.selectContractsForAgentMessageProcessing();
		int resultSize = agentMessageProcVO.size();
		if (debugLogging) { 
			getLogger().logDebug("Execution of Reply To Offer Decision Poller Started");
		}
		try{
			for (Iterator iter = agentMessageProcVO.iterator(); iter.hasNext();) {
				agentMsgProcRes = (AXAMessageCenterVO) iter.next();
				policyNum = agentMsgProcRes.getPolicynumber();
				getLogger().logDebug("Contract :" + policyNum + " is processing");
				agentRespInd = agentMsgProcRes.getAgentResponseCode();
				if (debugLogging) { 
					getLogger().logDebug("Agent response code :" + agentRespInd + " received for" + " contract :" + policyNum);
				}
				msgID = agentMsgProcRes.getMessageID();
				searchVO = searchContract(policyNum, user);
				try {

					if (searchVO != null && searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
						results = searchVO.getSearchResults();
						nbaDst = retrieveWorkItem((NbaSearchResultVO) results.get(0), user);
						setWork(nbaDst);
						setNbaTxLife(doHoldingInquiry());
						setReplyOfferReqStatus(getNbaTxLife(), getWork(),agentMsgProcRes.getRequirementInfoUniqueID());
						Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getNbaTxLife().getOLifE());
						long polStatus = holding.getPolicy().getPolicyStatus();
						if (nbaDst != null) {
							if (NbaConstants.A_QUEUE_UNDERWRITER_HOLD.equalsIgnoreCase(getWork().getQueue())) {
								if (!isReplyToOfferExpiredInd && !isMedicalRequirementsExpired(nbaDst, getNbaTxLife())) {
									ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
									if (!NbaUtils.isSurvivorshipProduct(nbaTxLife) && !(null != appInfoExt && appInfoExt.hasReopenDate())) {
										if(NbaUtils.isContractChange(nbaTxLife) && 
												(agentRespInd == NbaConstants.AGENT_RESPONSE_ACCEPT_CODE || agentRespInd == NbaConstants.AGENT_RESPONSE_ACCEPT_CHANGES_CODE)){
											deOink.put(NbaVpmsConstants.A_SETSTATUSFORUW_IND, "true");
											setStatus(getWork(), NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, nbaTxLife, deOink);
											getWork().getNbaLob().setRouteReason(ROUTE_REASON_REISSUE);
											WorkflowServiceHelper.updateWork(getUser(), getWork());
										}else{
											if (agentRespInd == NbaConstants.AGENT_RESPONSE_ACCEPT_CODE) {
												populateRequest(request, nbaTxLife, agentRespInd);
												applyCommitAction(nbaTxLife, request);
											} else if (agentRespInd == NbaConstants.AGENT_RESPONSE_ACCEPT_CHANGES_CODE) {
												deOink.put(NbaVpmsConstants.A_SETSTATUSFORUW_IND, "true");
												getWork().getNbaLob().setReqType((int) (NbaOliConstants.OLI_REQCODE_1009800012));
												if (!(getWork().getNbaLob().getLstNonRevReqRec())) {// NBLXA-2385
													getWork().getNbaLob().setQueueEntryDate(new Date()); // NBLXA-2385
												}
												getWork().getNbaLob().setLstNonRevReqRec(true);
												getWork().getNbaLob().setSigReqRecd(true);
												getWork().getNbaLob().setActionUpdate();
												setStatus(getWork(), NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, nbaTxLife, deOink);
												WorkflowServiceHelper.updateWork(getUser(), getWork());
											} else if (agentRespInd == NbaConstants.AGENT_RESPONSE_REJECT_CODE) {
												populateRequest(request, nbaTxLife, agentRespInd);
												applyCommitAction(nbaTxLife, request);
											}
										}
									} else {
										deOink.put(NbaVpmsConstants.A_SETSTATUSFORUW_IND, "true");
										setStatus(getWork(), NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, nbaTxLife, deOink);
										if (NbaUtils.isSurvivorshipProduct(nbaTxLife)) {
											getWork().getNbaLob().setRouteReason(ROUTE_REASON_SURVIVORSHIP);
										} else {
											getWork().getNbaLob().setRouteReason(ROUTE_REASON_REOPEN);
										}
										WorkflowServiceHelper.updateWork(getUser(), getWork());
									}
								} else {
									deOink.put(NbaVpmsConstants.A_SETSTATUSFORUW_IND, "true");
									setStatus(getWork(), NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, nbaTxLife, deOink);
									if (isReplyToOfferExpiredInd) {
										getWork().getNbaLob().setRouteReason(
												"RTO received but exceeds "+expiredNoOfDays+" days from the date the RTO req was posted");
									} else {
										if (expiredMedicalRequirementMap != null && !expiredMedicalRequirementMap.isEmpty()) {
											List medicalReqList = new ArrayList<String>();
											for(Entry<String, List> en : expiredMedicalRequirementMap.entrySet()){
												medicalReqList.add(en.getKey()+" - "+en.getValue());
											}
											for(int i=0;i<medicalReqList.size();i++){
												medicalRequirements.append(medicalReqList.get(i));
												if ( i != medicalReqList.size()-1){
													medicalRequirements.append(", ");
											    }
											}
				
											if (debugLogging) { 
												getLogger().logDebug("Reply to offer received but "+medicalRequirements.toString()+" is out of date");
											}
											addSecureComment("Reply to offer received but "+medicalRequirements.toString()+" is out of date");
											getWork().getNbaLob().setRouteReason(ROUTE_REASON_EXP_MEDICAL_REQ);
										}
									}
									WorkflowServiceHelper.updateWork(getUser(), getWork());
									setContractAccess(UPDATE);
									doContractUpdate();
								}

							} else if (polStatus == NbaOliConstants.OLI_POLSTAT_INCOMPLETE
									|| polStatus == NbaOliConstants.NBA_FINALDISPOSITION_REG60_PRESALE_EXPIRED
									&& NbaConstants.END_QUEUE.equalsIgnoreCase(getWork().getQueue())) {
								NbaReopenCaseRequest req = new NbaReopenCaseRequest();
								req.setNbaUserVO(getUser());
								req.setNbaDst(nbaDst);
								setContractAccess(UPDATE);
								nbaTxLife.setAccessIntent(getContractAccess());
								req.setTxlife(nbaTxLife);
								applyReopenAction(req);
							} else if ((!NbaConstants.A_QUEUE_UNDERWRITER_HOLD.equalsIgnoreCase(getWork().getQueue())
									|| !NbaConstants.END_QUEUE.equalsIgnoreCase(getWork().getQueue()))
									&& !getWork().getNbaLob().getUndwrtQueue().equalsIgnoreCase(getWork().getQueue())) {
								String routeReason = ROUTE_REASON_NON_UW;
								createMiscWorkTransaction(nbaDst, routeReason);
							}
						}
					}

				} catch (NbaLockedException exception) {
					getLogger().logError("Locked workitem being bypassed.  ContractNumber:" + policyNum);
					iter.remove();
					if (isSuspensionGreaterThanCreateDate(agentMsgProcRes)) {
						if (results != null && !results.isEmpty()) {
							nbaDst = retrieveWorkItemForLockedCase((NbaSearchResultVO) results.get(0));
							String routeReason = ROUTE_REASON_LOCKED;
							createMiscWorkTransaction(nbaDst, routeReason);
							NbaSystemDataDatabaseAccessor
									.resetProcessingIndicators(agentMsgProcRes.getPolicynumber(), agentMsgProcRes.getMessageID());
						}
					} else {
						NbaSystemDataDatabaseAccessor.suspend(policyNum, msgID);
					}
					continue;
				} catch (NbaBaseException nbe) {
					nbe.printStackTrace();
					getLogger().logError("Exception " + nbe.getMessage() + " occurred while processing response for " + policyNum);
					iter.remove();
					NbaSystemDataDatabaseAccessor.suspend(policyNum, msgID);
					continue;
				} catch (Exception nbe) {
					getLogger().logError("Unable to retrieve contract for: " + policyNum);
					iter.remove();
					NbaSystemDataDatabaseAccessor.suspend(policyNum, msgID);
					continue;
				} finally {
					if (null != getWork() && getWork().isLocked(user.getUserID())) {
						try {
							unlockCase();
						} catch (NbaBaseException nbe) {
							getLogger().logException(nbe);
							if (null != agentMsgProcRes) {
								getLogger().logError("Error unlocking work for contract: " + agentMsgProcRes.getPolicynumber());
								
							}
						}
					}
				}
				NbaSystemDataDatabaseAccessor.resetProcessingIndicators(agentMsgProcRes.getPolicynumber(), agentMsgProcRes.getMessageID());
				unlockCase();
			}
		}catch(Exception nbe){
			getLogger().logException(nbe);
			if(null !=agentMsgProcRes){
				getLogger().logError("Unable to process contract: " + agentMsgProcRes.getPolicynumber());
				NbaSystemDataDatabaseAccessor.suspend(agentMsgProcRes.getPolicynumber(),agentMsgProcRes.getMessageID());
			}
		}finally{
			if (null != getWork() && getWork().isLocked(user.getUserID())) {
				try {
					unlockCase();
				} catch (NbaBaseException nbe) {
					getLogger().logException(nbe);
					if(null !=agentMsgProcRes){
					getLogger().logError("Error unlocking work for contract: " + agentMsgProcRes.getPolicynumber());
					
					}
				}
			}
		}
		if(debugLogging){
			getLogger().logDebug("Execution of Reply to Offer Decision Poller Ends");
		}
		
		if (resultSize > 0) {
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "");
		}
		return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", "");
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
	
	protected void unlockCase() throws NbaBaseException {
		unlockWork(getWork());
		NbaContractLock.removeLock(getWork(), getUser());
	}
	
	/**
	 * set ReqStatus,Review id, Ind and Date for requirement reply to offer
	 * @param nbaTXLife
	 * @param work
	 * @throws NbaLockedException
	 * @throws NbaBaseException
	 */
	public void setReplyOfferReqStatus(NbaTXLife nbaTXLife, NbaDst work , String requirementInfoUniqueID) throws NbaLockedException,NbaBaseException {
		NbaDst requirementDst = null;
		try {
				List<RequirementInfo> reqInfoList = nbaTXLife.getRequirementInfoList(NbaOliConstants.OLI_REQCODE_1009800012,requirementInfoUniqueID);
				Policy policy = nbaTXLife.getPolicy();
				if (null != reqInfoList && !reqInfoList.isEmpty()) {
					for (int i = 0; i < reqInfoList.size(); i++) {
						RequirementInfo reqInfo = reqInfoList.get(i);
						if (reqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_ADD) {
							reqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_RECEIVED);
							requirementDst = getRequirementWI(policy.getPolNumber(), reqInfo.getRequirementInfoUniqueID());
							RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
							PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
							if (reqInfoExt != null && null != requirementDst && null != policyExtension
									&& requirementDst.getNbaLob().getReqUniqueID().equalsIgnoreCase(reqInfo.getRequirementInfoUniqueID())) {
								if (NbaConstants.A_QUEUE_UNDERWRITER_HOLD.equalsIgnoreCase(getWork().getQueue())) {
									reqInfoExt.setReviewedInd(true);
									reqInfoExt.setReviewID(user.getUserID());
									reqInfoExt.setReviewDate(new Date());
									reqInfoExt.setReceivedDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
									requirementDst.getNbaLob().setReview(NbaConstants.TRUE);
								}
								reqInfo.setReceivedDate(new Date());
								requirementDst.getNbaLob().setReqReceiptDate(new Date());
								requirementDst.getNbaLob().setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
								requirementDst.getNbaLob().setReqStatus(String.valueOf(reqInfo.getReqStatus()));
								requirementDst.setStatus(NbaConstants.A_STATUS_REQUIREMENTS_AGGREGATED);
								//requirementDst.getNbaLob().setRouteReason("Aggregated");
								requirementDst.getNbaTransaction().setUpdate();
								isReplyToOfferExpired(reqInfo,reqInfoExt,policyExtension);
								reqInfoExt.setActionUpdate();
								update(requirementDst);
								unlockWork(requirementDst);
							}					
							reqInfo.setActionUpdate();
							break;
							//setContractAccess(UPDATE);
						}
					}
					String origBusinessProcess = nbaTXLife.getBusinessProcess();
					nbaTXLife.setBusinessProcess(NbaConstants.PROC_REQUIREMENTS);
					setContractAccess(UPDATE);
					doContractUpdate(nbaTXLife);
					nbaTXLife.setBusinessProcess(origBusinessProcess);
				}
		}catch (NbaLockedException exception) {
			throw new NbaLockedException("This workItem cannot be processed");
		}catch (NbaBaseException e1) {
			throw new NbaBaseException("This workItem cannot be processed");
		}finally{
			if (null != requirementDst && requirementDst.isLocked(user.getUserID())) {
				try {
					unlockWork(requirementDst);
				} catch (NbaBaseException nbe) {
					getLogger().logException(nbe);
				}
			}
		}
	}

	/**
	 * Retrieves requirement work item from AWD.
	 * 
	 * @param policyNumber
	 * @param reqType
	 * @return the retrieved work item
	 * @throws NbaBaseException
	 */
	protected NbaDst getRequirementWI(String policyNumber, String reqUniqueID) throws NbaBaseException {
		NbaSearchResultVO resultVO = null;
		NbaLob lob = new NbaLob();
		lob.setPolicyNumber(policyNumber);
		lob.setReqUniqueID(reqUniqueID);
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_REQUIREMENT);
		searchVO.setNbaLob(lob);
		searchVO.setResultClassName("NbaSearchResultVO");
		searchVO = WorkflowServiceHelper.lookupWork(getUser(), searchVO);
		if (searchVO.isMaxResultsExceeded()) {
			throw new NbaBaseException(NbaBaseException.SEARCH_RESULT_EXCEEDED_SIZE, NbaExceptionType.FATAL);
		}
		List searchResult = searchVO.getSearchResults();
		if (searchResult != null && searchResult.size() > 0) {
			resultVO = (NbaSearchResultVO) searchResult.get(0);
		}
		NbaDst requirementDst = null;
		if (resultVO != null) {
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(resultVO.getWorkItemID(), true);
			retOpt.setLockWorkItem();
			requirementDst = WorkflowServiceHelper.retrieveWorkItem(getUser(), retOpt);
		}
		return requirementDst;
	}
	
	
	
	/**
	 * Applies the commit action.
	 * @param nbaTxLife
	 * @return String successIndicator
	 */	
	protected String applyCommitAction(NbaTXLife nbaTxLife, NbaContractApprovalDispositionRequest request) throws NbaBaseException {
		String status = BaseServiceAction.SUCCESS;
		AccelResult result = (AccelResult) ServiceHandler.invoke("CommitFinalDispositionBP", ServiceContext.currentContext(), request);
		if (hasErrors(result)) {
			addComment(COMMENT_AUTOAPPROVAL_FAILED);
			if (debugLogging) { 
				getLogger().logDebug("Reply to Offer Received.  Auto-approval failed for contract: " + policyNum);
			}
			deOink.put(NbaVpmsConstants.A_SETSTATUSFORUW_IND, "true");
			getWork().getNbaLob().setReqType((int) (NbaOliConstants.OLI_REQCODE_1009800012));
			if (!(getWork().getNbaLob().getLstNonRevReqRec())) {// NBLXA-2385
				getWork().getNbaLob().setQueueEntryDate(new Date()); // NBLXA-2385
			}
			getWork().getNbaLob().setLstNonRevReqRec(true);
			getWork().getNbaLob().setSigReqRecd(true);
			getWork().getNbaLob().setActionUpdate();
			setStatus(getWork(), NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, nbaTxLife, deOink);
			WorkflowServiceHelper.updateWork(getUser(), getWork());
		} else {
			getSecureCommentText(agentRespInd);
		}
		setContractAccess(UPDATE);
		doContractUpdate();
		return status;
	}
	
	/**
	 * Populates the <code>NbaContractApprovalDispositionRequest</code> value object sent
	 * to the business process to approve or deny the current contract.
	 * @param nbaTxLife
	 * @param agentRespInd
	 * @throws NbaBaseException
	 */
	protected void populateRequest(NbaContractApprovalDispositionRequest request, NbaTXLife nbaTxLife, long agentRespInd) throws NbaBaseException {
		boolean formalAppInd = false;
		request.setNbaUserVO(getUser());
		NbaDst requestDst = null;
		requestDst = getWork();
		request.setWork(requestDst);
		request.setContract(nbaTxLife);
		if (!NbaUtils.isBlankOrNull(nbaTxLife.getPolicy())) {
			if (!NbaUtils.isBlankOrNull(nbaTxLife.getPolicy().getApplicationInfo())) {
				formalAppInd = nbaTxLife.getPolicy().getApplicationInfo().getFormalAppInd();
			}
		}
		ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(nbaTxLife.getPolicy().getApplicationInfo());
		request.setInformalApp(formalAppInd);
		String underwriterQueue = requestDst.getNbaLob().getUndwrtQueue();
		//NBLXA-2489 remove code related underwriterRole
		if(debugLogging){
			getLogger().logDebug("Underwriter Queue for " + policyNum + " is" + underwriterQueue); //NBLXA-2489
		}
		request.setUnderwriterRoleLevel(getUnderwriterLevel(underwriterQueue, NbaTableAccessConstants.WILDCARD,
					NbaTableAccessConstants.WILDCARD)); //NBLXA-2489
		
		request.setOverrideDeliveryDays(appInfoExtn.getAddlPlacementDays());
		if(agentRespInd == NbaConstants.AGENT_RESPONSE_ACCEPT_CODE){
		request.setApproval(NbaOliConstants.OLI_POLSTAT_APPROVED);
		}
		else if(agentRespInd == NbaConstants.AGENT_RESPONSE_REJECT_CODE){
			request.setApproval(NbaOliConstants.OLI_POLSTAT_DECISSUE);
			request.setUnderwritingStatus(NbaOliConstants.OLI_POLSTAT_WITHDRAW);
		}
		if (nbaTxLife.isInformalApp()) {
			if (appInfoExtn != null) {
				request.setApproval(appInfoExtn.getInformalAppApproval());
				request.setInformalAppAccepted(NbaUtils.isInformalOfferMade(appInfoExtn));
			}
		} else {
			 request.setIssueOtherThanAppliedFor(requestDst.getNbaLob().getIssueOthrApplied());
			 // request.setReAutoUnderwrite(bean.isReAutoUnderwrite());
			//  request.setIssueDate(nbaTxLife.getPolicy().getIssueDate());
		}
	}
	
	/**
	 * determines if there was an error in the operation
	 * @param resultObject - result to check
	 */
	public boolean hasErrors(Object resultObject) {
		List currentErrors = new ArrayList();
		boolean containsError = false;
		if (resultObject != null) {
			if (resultObject instanceof String) {
				currentErrors = XMLUtils.getXMLValueList(ServiceDelegator.ERROR_TAG, (String) resultObject);
				containsError = !currentErrors.isEmpty();
			} else {
				ResultBase value = (ResultBase) resultObject;
				containsError = value.isErrors();
				Message messages[] = value.getMessages();
				if (messages != null && messages.length > 0) {
					for (int i = 0; i < messages.length; i++) {
						Message message = messages[i];
						String errorVal = "";
						try{
						    errorVal = message.format();
						}catch(Exception ex){
						    LogHandler.Factory.LogError("ServiceAction", "[{0}] Failed to obtain text from message with exception [{1}]", ex, new Object[]{this.currentConfig.id, ex.getMessage()});
						}
						if (errorVal == null || errorVal.equals(Message.ERR_MESSAGE_MISSING)) {
							List data = message.getData();
							if(data != null){
								containsError = true;
								errorVal += " data[" + data.toString() + "]";
							}
						}
						currentErrors.add(message);
					}
				}
			}
		}
		return containsError;
	}
	
	/**
	 * Adds a new secure comment.
	 * 
	 * @param agentRespInd
	 */
	protected void addSecureComment(String secureComment) {
		String comment = secureComment;
		
		nbaOLifEId = new NbaOLifEId(nbaTxLife);
		Attachment attachment = new Attachment();
		nbaOLifEId.setId(attachment);
		attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE);
		attachment.setActionAdd();
		attachment.setDateCreated(new Date());
		attachment.setUserCode(getUser().getUserID());
		AttachmentData attachmentData = new AttachmentData();
		attachmentData.setTc("8");

		SecureComment secure = new SecureComment();
		secure.setComment(comment);
		secure.setUserNameEntered(getUser().getUserID());
		secure.setAutoInd(true);
		attachmentData.setPCDATA(toXmlString(secure));

		attachmentData.setActionAdd();
		attachment.setAttachmentData(attachmentData);
		getNbaTxLife().getPrimaryHolding().addAttachment(attachment);
	}
	
	/**
	 * Convert the SecureComment to xml string
	 * @return java.lang.String
	 */
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
	
	/**
	 * Use the <code>NbaProcessStatusProvider</code> to determine the work item's next status. If a new status is returned back from the VP/MS model,
	 * then only update the transaction's status.
	 * @param work
	 * @param userID
	 * @param nbaTXLife
	 * @param deOinkMap
	 * @throws NbaBaseException
	 */
	protected void setStatus(NbaDst work, String userID, NbaTXLife nbaTXLife, Map deOink) throws NbaBaseException {
		NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(userID, ""), work, nbaTXLife, deOink);
		work.setStatus(provider.getPassStatus());
		work.increasePriority(provider.getCaseAction(), provider.getCasePriority());
		if (work.getWorkItem().hasNewStatus()) {
			NbaUtils.setRouteReason(work, work.getStatus(), provider.getReason());
		}
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 * @throws AxaErrorStatusException
	 * @throws NbaBaseException
	 */
	protected String applyReopenAction( NbaReopenCaseRequest request) throws AxaErrorStatusException,NbaBaseException {
		String status = BaseServiceAction.SUCCESS;
		try {
			AccelResult result = (AccelResult) ServiceHandler.invoke("ReopenCaseBP", ServiceContext.currentContext(), request);
			if (result.hasErrors()) {
				try {
					WorkflowServiceHelper.checkOutcome(result);
				} catch (NbaBaseException ex) {
					throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, ex.getMessage());// APSL3874
				}
			} else {
				deOink.put(NbaVpmsConstants.A_SETSTATUSFORUW_IND, "true");
				setReopenStatus(work, NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, nbaTxLife, deOink);
				update(work);
			}
		} catch (Exception ex) {
			throw new NbaBaseException(ex);
		}
		return status;
	}
	
	/**
	 * Setting routing reason for reopen case
	 * @param work
	 * @param userID
	 * @param nbaTXLife
	 * @param deOink
	 * @throws NbaBaseException
	 */
	protected void setReopenStatus(NbaDst work, String userID, NbaTXLife nbaTXLife, Map deOink) throws NbaBaseException {
		NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(userID, ""), work, nbaTXLife, deOink);
		work.setStatus(provider.getPassStatus());
		work.increasePriority(provider.getCaseAction(), provider.getCasePriority());
		provider.setReason(ROUTE_REASON_INCOMPLETE);
		if (work.getWorkItem().hasNewStatus()) {
			NbaUtils.setRouteReason(work, work.getStatus(), provider.getReason());
		}
	}
	
	/**
	 * Create Misc Work Transaction 
	 * @param nbaDst
	 * @param userID
	 * @param reason
	 * @throws NbaBaseException
	 */
	public void createMiscWorkTransaction(NbaDst nbaDst,String reason) throws NbaBaseException {
		if(debugLogging){
			getLogger().logDebug("Inside createMiscWork transacction for locked case");
		}
		WorkItem transaction = new WorkItem();
		NbaDst nbadstforMiscWork = new NbaDst();
		nbadstforMiscWork.setUserID(getUser().getUserID());
		nbadstforMiscWork.setPassword(getUser().getPassword());
		try {
			nbadstforMiscWork.addTransaction(transaction);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// set Business Area, Work type and Status
		transaction.setBusinessArea(nbaDst.getBusinessArea());
		transaction.setWorkType(NbaConstants.A_WT_MISC_WORK);
		transaction.setLobData(nbaDst.getNbaLob().getLobs());
		setStatus(nbadstforMiscWork, NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, nbaTxLife, deOink);
		transaction.setStatus(nbadstforMiscWork.getStatus());
		nbadstforMiscWork.getNbaLob().setQueueEntryDate(new Date());
		nbadstforMiscWork.getNbaLob().setRouteReason(reason);
		NbaUtils.addGeneralComment(nbadstforMiscWork, getUser(), reason);
		transaction.setCreate("Y");
		updateWork(getUser(), nbadstforMiscWork);
		unlockWork(getUser(), nbadstforMiscWork);

	}
	
	/**
	 * Compare Suspended date and create date for time difference of greater than 2 hours
	 * @param agentMsgProcRes
	 * @return
	 * @throws ParseException
	 */
	private boolean isSuspensionGreaterThanCreateDate(AXAMessageCenterVO agentMsgProcRes) throws ParseException{
		String suspendedDate=agentMsgProcRes.getSuspendedDate();
		String createDate=agentMsgProcRes.getCreateDate();
		if(suspendedDate != null && createDate != null ){
			Date suspendedDateTime=new SimpleDateFormat("yyyy-mm-dd HH:mm:ss").parse(suspendedDate);
			Date createDateTime=new SimpleDateFormat("yyyy-mm-dd HH:mm:ss").parse(createDate);
			//in milliseconds
			long diff = suspendedDateTime.getTime() - createDateTime.getTime();
			long diffHours = diff / (60 * 60 * 1000) % 24;
			if(diffHours >= 2){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retrieve work item for locked case
	 * @param resultVO
	 * @return
	 * @throws NbaBaseException
	 */
	public NbaDst retrieveWorkItemForLockedCase(NbaSearchResultVO resultVO) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setNbaUserVO(getUser());
		retOpt.setWorkItem(resultVO.getWorkItemID(), true);
		aWorkItem = retrieveWorkItem(getUser(), retOpt);
		return aWorkItem;
	}
	/**
	 * check Reply to offer requirement is out of date if recieved after 30 days
	 * set isReplyToOfferExpiredInd
	 * @param requirementInfo
	 * @param requirementInfoExt
	 */
	private void isReplyToOfferExpired(RequirementInfo requirementInfo, RequirementInfoExtension requirementInfoExt,PolicyExtension policyExt) {
		if(agentRespInd == NbaConstants.AGENT_RESPONSE_ACCEPT_CODE ){
			Date receivedDate = requirementInfo.getReceivedDate();
			Date createdDate = requirementInfoExt.getCreatedDate();
			if (receivedDate != null && createdDate != null) {
				long diff = receivedDate.getTime() - createdDate.getTime();
				long diffDays = diff / (24 * 60 * 60 * 1000);
				if(NbaOliConstants.OLI_DISTCHAN_10 == policyExt.getDistributionChannel() && diffDays > 30){
					isReplyToOfferExpiredInd = true;
					expiredNoOfDays="30";
				}else if (NbaOliConstants.OLI_DISTCHAN_6 == policyExt.getDistributionChannel() && diffDays > 14) {
					isReplyToOfferExpiredInd = true;
					expiredNoOfDays="14";
				}
			}
		}
	}
	
	/**
	 * Fetch expired medical requirement list for Primary and Joint Insured from VPMS 
	 * Invoke "P_MedicalRequirementsExpired" entrypoint.
	 * @param nbaDst
	 * @param nbaTxLife
	 * @return
	 * @throws NbaBaseException
	 * @throws NbaVpmsException
	 */
	private Map<String, List> getExpiredRequirementListfromVPMS(NbaDst nbaDst,NbaTXLife nbaTxLife)throws NbaBaseException, NbaVpmsException{
		NbaVpmsAdaptor vpmsProxy = null;
		List<String> expiredPrimaryInsRequirementList = new ArrayList<String>();
		List<String> expiredJointInsRequirementList = new ArrayList<String>();
		Map<String, List> requirementMap = new HashMap();
		NbaStringTokenizer tokens,reqcodes;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
			
			if (nbaTxLife != null) {
				oinkData.setContractSource(nbaTxLife);
				oinkData.setDstSource(nbaDst);
				oinkData.getFormatter().setDateFormat(NbaOinkFormatter.DATE_FORMAT_YYYYMMDD);  //NBA104
				oinkData.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);  //NBA104
			}
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_MEDICAL_REQUIREMENTS_EXPIRED);
			
			VpmsComputeResult compResult = vpmsProxy.getResults();
			tokens = new NbaStringTokenizer(compResult.getResult().trim(), "#");
			String aToken,reqcode;
			
			for (int i=0;i<=tokens.countTokens();i++) {
				aToken = tokens.nextToken();
				reqcodes = new NbaStringTokenizer(aToken.trim(), ",");
				while(reqcodes.hasMoreTokens()){
					reqcode = reqcodes.nextToken();
					if(i==0)
						expiredPrimaryInsRequirementList.add(reqcode);
					else if(i==1)
						expiredJointInsRequirementList.add(reqcode);
				}
				if(i==0){
					requirementMap.put("PrimaryIns", expiredPrimaryInsRequirementList);
				}else if(i==1){
					requirementMap.put("JointIns", expiredJointInsRequirementList);
				}
			}

		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("Problem with fetching result from VPMS " + NbaVpmsException.VPMS_EXCEPTION, re);
		} catch (NbaBaseException e) {
			getLogger().logDebug(
					"Exeception occurred while fetching result from VPMS Model : " + NbaUtils.getStackTrace(e));
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}
		return requirementMap;
	}
	
	/**
	 * check if medical requirement is expired
	 * fetch requirement translation of expired requirements based on their reqcode.
	 * @param nbaDst
	 * @param nbaTxLife
	 * @return
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	private boolean isMedicalRequirementsExpired(NbaDst nbaDst,NbaTXLife nbaTxLife) throws NbaVpmsException, NbaBaseException {
		if(agentRespInd == NbaConstants.AGENT_RESPONSE_ACCEPT_CODE){
			Map<String, List>  expiredRequirementMap = getExpiredRequirementListfromVPMS(nbaDst,nbaTxLife);
			List<String> expiredPrimaryInsRequirementList=expiredRequirementMap.get("PrimaryIns");
			List<String> expiredJointInsRequirementList = expiredRequirementMap.get("JointIns");
			List<String> expiredPrimaryMedicalReqTransList = new ArrayList<String>();
			List<String> expiredJointMedicalReqTransList = new ArrayList<String>();
			if(expiredPrimaryInsRequirementList != null && !expiredPrimaryInsRequirementList.isEmpty() 
					&& expiredJointInsRequirementList != null && !expiredJointInsRequirementList.isEmpty()){
				for(int i=0;i<expiredPrimaryInsRequirementList.size();i++){
					String reqCode=expiredPrimaryInsRequirementList.get(i);
					if(!reqCode.equals("0")){
						String reqTranslation=NbaUtils.getRequirementTranslation(reqCode, nbaTxLife.getPolicy());
						expiredPrimaryMedicalReqTransList.add(reqTranslation);
					}
					
				}for(int i=0;i<expiredJointInsRequirementList.size();i++){
					String reqCode=expiredJointInsRequirementList.get(i);
					if(!reqCode.equals("0")){
						String reqTranslation=NbaUtils.getRequirementTranslation(reqCode, nbaTxLife.getPolicy());
						expiredJointMedicalReqTransList.add(reqTranslation);
					}
					
				}
				if(expiredPrimaryMedicalReqTransList != null && !expiredPrimaryMedicalReqTransList.isEmpty()){
					expiredMedicalRequirementMap.put("For Primary Insured", expiredPrimaryMedicalReqTransList);
				}
				if(expiredJointMedicalReqTransList != null && !expiredJointMedicalReqTransList.isEmpty()){
					expiredMedicalRequirementMap.put("For Joint Insured", expiredJointMedicalReqTransList);
				}
				if(expiredMedicalRequirementMap != null && !expiredMedicalRequirementMap.isEmpty()){
					return true;
				}
				
			}
		}
		
		return false;
		
	}
	
	/**
	 * Adds a new secure comment for approval and withdrawal.
	 * @param agentRespInd
	 */
	public void getSecureCommentText(long agentRespInd){
		String comment = "";
		if (agentRespInd == NbaConstants.AGENT_RESPONSE_ACCEPT_CODE) {
			comment = COMMENT_ACCEPT_RESPONSE;
			if(debugLogging){
				getLogger().logDebug("Reply to Offer received and accepted.  nbA automatically approved for contract: " + policyNum);
			}
		} else if (agentRespInd == NbaConstants.AGENT_RESPONSE_REJECT_CODE) {
			comment = COMMENT_REJECT_RESPONSE;
			if(debugLogging){
				getLogger().logDebug("Reply to Offer received and rejected by agent.  nbA automatically withdrawn for contract: " + policyNum);
			}
		}
		addSecureComment(comment);
	}

}