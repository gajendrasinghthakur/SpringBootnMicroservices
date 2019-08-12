package com.csc.fsg.nba.business.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.database.NbaScorDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaScorSubmitContractVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.DiseaseDescriptionCC;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.PrescriptionDrug;
import com.csc.fsg.nba.vo.txlife.PrescriptionDrugExtension;
import com.csc.fsg.nba.vo.txlife.PrescriptionFill;
import com.csc.fsg.nba.vo.txlife.PrescriptionFillExtension;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfoExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SuspendInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */

/**
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>APSL2808</td>
 * <td>Discretionary</td>
 * <td>Simplified Issue</td>
 * </tr>
 * <tr><td>SR787006-APSL3702</td>
 * <td>Discretionary</td><td>Simplified Issue</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class NbaProcSIMoneyRequirements extends NbaAutomatedProcess {

	/** String for Nba Money */
	protected static int DAY = 15;
	protected static final String VPMS_NBAMONEY = "1";
	private final static String SUSPENDED = "SUSPENDED";
	private final static String REQ_DONE_SCOR = "REQDONEFORSCOR";
	
	static {
		try {
			DAY = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
					NbaConfigurationConstants.SI_MONEY_REQUIREMENTS_SUSPEND_DAYS));
		} catch (Exception ex) {
			DAY = 15;
		}
	}

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		if (!initialize(user, work)) {
			return getResult();
		}
		
		//APSL3308(QC12368)
		if(getNbaTxLife() != null && !getNbaTxLife().isSIApplication()){
			handleNonSICaseTOSIQueue();
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
			return getResult();
		}// APSL3308(QC12368) end

		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestSources();
		retOpt.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retOpt));
		boolean doContractUpdate = true;
		boolean deleteRecordFromAUX = true;
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
		long scorDecision = appInfoExt.getSCORUnderWritingDecision();

		if (scorDecision == NbaOliConstants.AXA_SCORDECISION_ACCEPTED) { // Accepted by SCOR
			NbaTXLife notificationTxLife = getNotificationResponse();
			if (notificationTxLife != null) { // Response received from notification system
				StringBuffer comment = new StringBuffer();
				long policyStatus = notificationTxLife.getPolicy().getPolicyStatus();
				if (policyStatus == NbaOliConstants.OLI_POLSTAT_APPROVED) { // Accepted by customer
					updateFinalDispositionFields(NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER);
					addReinsuranceInfo();
					createCWA();
					comment.append("Customer Response:Approved ").append(NbaUtils.getStringInDateTimeFormatFromDate(new Date()));
					addComment(comment.toString());
				} else if (policyStatus == NbaOliConstants.OLI_POLSTAT_NOTAKE 
								|| policyStatus == NbaOliConstants.OLI_POLSTAT_EXPIRED) { // Rejected by customer, APSL3266 
					updateFinalDispositionFields(NbaOliConstants.OLI_POLSTAT_WITHDRAW);
					String decision = policyStatus == NbaOliConstants.OLI_POLSTAT_NOTAKE ? "Not Taken " : "Expired ";
					comment.append("Customer Response:").append(decision).append(NbaUtils.getStringInDateTimeFormatFromDate(new Date()));
					addComment(comment.toString());
				}
				setStatusProvider(new NbaProcessStatusProvider(getDataFromVpms(NbaVpmsAdaptor.AUTO_PROCESS_STATUS, NbaVpmsAdaptor.EP_WORKITEM_STATUSES, null, new HashMap(), null)));
			} else {
				processRequirementResults();
				suspendCaseTillResponseReceivedFormNotificationSystem();
				deleteRecordFromAUX = false;
			}
		} else if (scorDecision == NbaOliConstants.AXA_SCORDECISION_DECLINE) { // Declined
			updateFinalDispositionFields(NbaOliConstants.OLI_POLSTAT_DECISSUE);
			processRequirementResults();      //APSL3221
		} else if (scorDecision == NbaOliConstants.AXA_SCORDECISION_WITHDRW) { // Withdrawn
			updateFinalDispositionFields(NbaOliConstants.OLI_POLSTAT_WITHDRAW);
			processRequirementResults();  //APSL3221
		} else if (scorDecision == NbaOliConstants.AXA_SCORDECISION_REFFERD) { // Referred may be Approved
			processRequirementResults();
			if (appInfoExt.getTentativeDispCount() > 0 && appInfoExt.getTentativeDispAt(0).getDisposition() == NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED) {// Referred may be Approved
				NbaTXLife notificationTxLife = getNotificationResponse();
				if (notificationTxLife != null) { // Response received from notification system
					StringBuffer comment = new StringBuffer();
					long policyStatus = notificationTxLife.getPolicy().getPolicyStatus();
					if (policyStatus == NbaOliConstants.OLI_POLSTAT_APPROVED) { // Accepted by customer
						updateFinalDispositionFields(NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER);
						addReinsuranceInfo();
						createCWA();
						comment.append("Customer Response:Approved ").append(NbaUtils.getStringInDateTimeFormatFromDate(new Date()));
						addComment(comment.toString());
					} else if (policyStatus == NbaOliConstants.OLI_POLSTAT_NOTAKE
									|| policyStatus == NbaOliConstants.OLI_POLSTAT_EXPIRED) { // Rejected by customer, APSL3266 
						updateFinalDispositionFields(NbaOliConstants.OLI_POLSTAT_WITHDRAW);
						String decision = policyStatus == NbaOliConstants.OLI_POLSTAT_NOTAKE ? "Not Taken ": "Expired ";
						comment.append("Customer Response:").append(decision).append(NbaUtils.getStringInDateTimeFormatFromDate(new Date()));
						addComment(comment.toString());
					}
					setStatusProvider(new NbaProcessStatusProvider(getDataFromVpms(NbaVpmsAdaptor.AUTO_PROCESS_STATUS, NbaVpmsAdaptor.EP_WORKITEM_STATUSES, null, new HashMap(), null)));
				} else {
					suspendCaseTillResponseReceivedFormNotificationSystem();
					deleteRecordFromAUX = false;
				}
			} else {
				deleteRecordFromAUX = false;
			}
		}else if(scorDecision == NbaOliConstants.AXA_SCORDECISION_INFOREQ){ //Information Required move case to UWCM SR787006-APSL3702-QC12091 starts 
			doContractUpdate = false;
			deleteRecordFromAUX = false;
			NbaScorSubmitContractVO scorVO =  new NbaScorSubmitContractVO();
			scorVO.setResponseProcessedInd(PROCESSED_INDICATOR);
			scorVO.setContractKey(getWork().getNbaLob().getPolicyNumber());
			NbaScorDatabaseAccessor.updateResponseProcessedIndicator(scorVO);
		}
		//SR787006-APSL3702-QC12091 ends
		else {		
			doContractUpdate = false;
			deleteRecordFromAUX = false;
		}
		if (doContractUpdate) {
			nbaTxLife = doContractUpdate();
			handleHostResponse(nbaTxLife);
		}		
		if (getResult() == null) {// not a database update error
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			changeStatus(getResult().getStatus());
			// Update the Work Item with it's new status and update the work item in AWD
			doUpdateWorkItem();
		}
		if (deleteRecordFromAUX) {
			//Delete the Record from AUX table after processing is completed successfully.
			NbaScorDatabaseAccessor.deleteRecord(getWork().getNbaLob().getPolicyNumber());
		}
		return getResult();
	}

	private void processRequirementResults() throws NbaBaseException {
		int reqInfoCount = 0;
		NbaTXLife scorResponse;
		boolean isReqProcessingNeeded = NbaUtils.isAxaWSCallNeeded(getNbaTxLife().getPolicy(), REQ_DONE_SCOR);
		if (isReqProcessingNeeded) {
			NbaScorSubmitContractVO scorVO = NbaScorDatabaseAccessor.selectRecordResponse(getWork().getNbaLob().getPolicyNumber());
			try {
				scorResponse = new NbaTXLife(scorVO.getWebServiceResponse());
			} catch (Exception e) {
				throw new NbaBaseException("Error parsing in Scor webservice response.", e, NbaExceptionType.FATAL);
			}
			Policy responsePolicy = scorResponse.getPolicy();
			reqInfoCount = responsePolicy.getRequirementInfoCount();
			for (int i = 0; i < reqInfoCount; i++) {
				long reqCode = responsePolicy.getRequirementInfoAt(i).getReqCode();
				if (reqCode == NbaOliConstants.OLI_REQCODE_MVRPT || reqCode == NbaOliConstants.OLI_REQCODE_MIBCHECK
						|| reqCode == NbaOliConstants.OLI_REQCODE_SCRIPTCHK) {
					Iterator attachments = responsePolicy.getRequirementInfoAt(i).getAttachment().iterator();
					while (attachments.hasNext()) {
						Attachment attachment = (Attachment) attachments.next();
						if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_REQUIRERESULTS) {
							String attachmentData = attachment.getAttachmentData().getPCDATA();
							if (reqCode == NbaOliConstants.OLI_REQCODE_MVRPT) {
								evaluateMVRResponse(nbaTxLife, work, (int) reqCode, attachmentData);
							} else if (reqCode == NbaOliConstants.OLI_REQCODE_MIBCHECK) {
								evaluateMIBResponse(attachmentData, (int) reqCode);
							} else if (reqCode == NbaOliConstants.OLI_REQCODE_SCRIPTCHK) {
								evaluateRxResponse(attachmentData, (int) reqCode);
							}
						}
					}
				}
			}
			if (reqInfoCount > 0) {
				nbaTxLife.addTransResponse(createTransResponse(REQ_DONE_SCOR));
			}
		}
	}

	private void evaluateMIBResponse(String mibResponse, int reqCode) throws NbaBaseException {
		NbaTXLife mibRespTXLife;
		try {
			mibRespTXLife = new NbaTXLife(mibResponse);
		} catch (Exception e) {
			throw new NbaBaseException("Error parsing in MIB response.", e, NbaExceptionType.FATAL);
		}
		if (mibRespTXLife.isTransactionError()) {
			handleInvalidRequirementResult(mibRespTXLife);
		} else {
			if (mibRespTXLife.getTransType() == NbaOliConstants.TC_TYPE_MIBINQUIRY) {
				NbaDst mibTransDst = NbaAutoUnderwritingHelper.processMIBInquiryResponse(mibResponse, getUser(), getWork(), getWorkLobs(), reqCode);
				mibTransDst = update(mibTransDst);
				unlockWork(mibTransDst);
			}
		}
	}

	private void evaluateRxResponse(String scorRxResponse, int reqCode) throws NbaBaseException {
		NbaTXLife rxRespTXLife;
		String rxResponse = NbaAutoUnderwritingHelper.convertScorToNba(scorRxResponse);
		try {
			rxRespTXLife = new NbaTXLife(rxResponse);
			if (rxRespTXLife.getPolicy() != null) {
				RequirementInfo requirementInfo = rxRespTXLife.getPolicy().getRequirementInfoAt(0);
				if (requirementInfo != null) {
					requirementInfo.setRequirementInfoUniqueID(NbaRequirementUtils.generateRequirementInfoUniqueID(getNbaTxLife(), requirementInfo));
				}
			}
			// APSL3248 Begin
			if (rxRespTXLife.getPrimaryParty() != null && rxRespTXLife.getPrimaryParty().getParty().getRisk() != null) {
				int prescriptionDrugCount = rxRespTXLife.getPrimaryParty().getParty().getRisk().getPrescriptionDrugCount();
				for (int i = 0; i < prescriptionDrugCount; i++) {
					PrescriptionDrug aPrescriptionDrug = rxRespTXLife.getPrimaryParty().getParty().getRisk().getPrescriptionDrugAt(i);
					if (aPrescriptionDrug != null) {
						int highestDiseaseSeverityCode = 0;
						int fillCount = aPrescriptionDrug.getPrescriptionFillCount();
						for (int j = 0; j < fillCount; j++) {
							PrescriptionFill aPrescriptionFill = aPrescriptionDrug.getPrescriptionFillAt(j);
							PrescriptionFillExtension aPrescriptionFillExtension = NbaUtils.getFirstPrescriptionFillExtension(aPrescriptionFill);
							if (aPrescriptionFillExtension != null) {
								int diseaseDescriptionCCCount = aPrescriptionFillExtension.getDiseaseDescriptionCCCount();
								for (int k = 0; k < diseaseDescriptionCCCount; k++) {
									DiseaseDescriptionCC aDiseaseDescriptionCC = aPrescriptionFillExtension.getDiseaseDescriptionCCAt(k);		                        
									if (aDiseaseDescriptionCC.hasDiseaseSeverityCode()){
										try {
											int severityCode = Integer.parseInt(aDiseaseDescriptionCC.getDiseaseSeverityCode());
											if (severityCode > highestDiseaseSeverityCode) {
												highestDiseaseSeverityCode = severityCode;
											}
										} catch (NumberFormatException e) {
											//Ignore if not numeric
										}
									}
								}
							}
						}
						setTherapeuticClassScore(aPrescriptionDrug, highestDiseaseSeverityCode);						
					}
				}
			}
			// APSL3248 End
		} catch (Exception e) {
			throw new NbaBaseException("Error parsing in Rx response.", e, NbaExceptionType.FATAL);
		}
		if (rxRespTXLife.isTransactionError()) {
			handleInvalidRequirementResult(rxRespTXLife);
		} else {
			NbaDst rxTransDst = NbaAutoUnderwritingHelper.processRXCheckResponse(rxRespTXLife.toXmlString(), getUser(), getWork(), getWorkLobs(), reqCode);
			rxTransDst = update(rxTransDst);
			unlockWork(rxTransDst);
		}
	}

	private void handleInvalidRequirementResult(NbaTXLife nbaTXLifeResponse) throws NbaBaseException {
		TransResult transResult = nbaTXLifeResponse.getTransResult();
		int resultInfoCount = transResult.getResultInfoCount();
		List errors = new ArrayList();
		for (int i = 0; i < resultInfoCount; i++) {
			errors.add(transResult.getResultInfoAt(i).getResultInfoDesc());
		}
		addComments(errors); //add error messages on the workitem
		//if failure (5) result code with result info description is received, then throw a fatal exception to stop poller
		throw new NbaBaseException(NbaBaseException.WEBSERVICE_NOT_AVAILABLE, NbaExceptionType.FATAL);
	}

	/**
	 * Adds default Reinsurance Info for auto approved cases.
	 * 
	 * @throws NbaBaseException
	 */
	//ALS5273 New Method
	protected void addReinsuranceInfo() {
		List reinInfoList = nbaTxLife.getPrimaryCoverage().getReinsuranceInfo();
		ReinsuranceInfo reinInfo = null;
		if (reinInfoList != null && reinInfoList.size() >= 1) {
			for (int i = 0; i < reinInfoList.size(); i++) {
				reinInfo = (ReinsuranceInfo) reinInfoList.get(i);
				if (!reinInfo.hasCarrierPartyID()) {//If it is a default reinsurance info tag
					reinInfo.deleteReinsuredAmt();
					reinInfo.deleteRetentionAmt();
					reinInfo.setActionUpdate();
					break;
				}
			}
		} else {
			reinInfo = new ReinsuranceInfo();
			nbaTxLife.getPrimaryCoverage().addReinsuranceInfo(reinInfo);
			reinInfo.setActionAdd();
		}
		reinInfo.setReinsuranceRiskBasis(NbaOliConstants.OLI_REINRISKBASE_NONE);//Set reinsurance risk basis as 'TAI Determined'

		ReinsuranceInfoExtension reinInfoExtsn = NbaUtils.getFirstReinsuranceInfoExtension(reinInfo);
		if (reinInfoExtsn == null) {
			OLifEExtension olifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REINSURANCEINFO);
			reinInfo.addOLifEExtension(olifeExtension);
			reinInfoExtsn = olifeExtension.getReinsuranceInfoExtension();
			reinInfoExtsn.setActionAdd();
		} else {
			reinInfoExtsn.setActionUpdate();
		}

		Policy policy = nbaTxLife.getPrimaryHolding().getPolicy();
		policy.setReinsuranceInd(false);
		policy.setActionUpdate();
	}

	protected void updateFinalDispositionFields(long undStatus) {
		ApplicationInfo appInfo = getNbaTxLife().getPolicy().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		appInfo.setHOCompletionDate(new Date()); //APSL3177(QC12015) HOCompletionDate set in SI Auto underwriting overriden here  
		appInfo.setActionUpdate();
		if (undStatus == NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER) {
			appInfoExt.setUnderwritingApproval(undStatus);
		} else if (undStatus == NbaOliConstants.OLI_POLSTAT_DECISSUE || undStatus == NbaOliConstants.OLI_POLSTAT_WITHDRAW) {
			NbaLob lob = getWork().getNbaLob();
			lob.setCaseFinalDispstn((int) undStatus);
			if (appInfoExt.getReopenDate() != null) {
				appInfoExt.deleteReopenDate();
			}
			appInfoExt.setUnderwritingStatus(undStatus);
		}
		appInfoExt.setActionUpdate();
	}

	protected void createCWA() throws NbaBaseException {
		Map deOink = new HashMap();
		deOink.put("A_NBAMONEY", VPMS_NBAMONEY);
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOink);		
		NbaTransaction nbaTransaction = getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
		nbaTransaction.getNbaLob().setPaymentMoneySource(String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT)); //APSL2983
		try {
			List allSources = getWork().getNbaSources();
			boolean cwaSourceNotFound = true;
			for (int i = 0; i < allSources.size(); i++) {
				NbaSource aSource = (NbaSource) allSources.get(i);
				if (aSource.isCwaCheck()) {
					cwaSourceNotFound = false;
					NbaSource newSource = getWork().getNbaCase().moveNbaSource(nbaTransaction, aSource);
					newSource.getNbaLob().setCheckAmount(getNbaTxLife().getPolicy().getPaymentAmt());
					newSource.getNbaLob().setCwaDate(new Date()); // APSL3177
					newSource.setUpdate();
				}
			}
			if (cwaSourceNotFound) {
				throw new NbaBaseException("CWA source not attached to the case.");
			}
		} catch (Exception e) {
			addComment(e.getMessage());
			throw new NbaBaseException("Exception while creating CWA WI.", e);
		}		
	}

	private void suspendCaseTillResponseReceivedFormNotificationSystem() throws NbaBaseException {
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
		if (policyExtension == null) {
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			nbaTxLife.getPolicy().addOLifEExtension(olifeExt);
			policyExtension = olifeExt.getPolicyExtension();
			olifeExt.getPolicyExtension().setActionAdd();
		}
		SuspendInfo suspendInfo = new SuspendInfo();
		Date currentDate = new Date();
		suspendInfo.setSuspendDate(currentDate);
		suspendInfo.setUserCode(getUser().getUserID());
		policyExtension.setSuspendInfo(suspendInfo);
		suspendInfo.setActionAdd();
		policyExtension.setActionUpdate();

		if (result == null) { // not a database update error
			Date suspendActivationDate = NbaUtils.addDaysToDate(currentDate, DAY);
			NbaSuspendVO suspendVO = new NbaSuspendVO();
			suspendVO.setCaseID(getWork().getID());
			suspendVO.setActivationDate(suspendActivationDate);
			addComment("Case suspended, notification result has not been receipted and/or evaluated.");
			updateWork(user, work);
			suspendWork(suspendVO);
			result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", SUSPENDED);
		}
	}

	public void evaluateMVRResponse(NbaTXLife nbaTxLife, NbaDst parentCase, int reqType, String mvrRespFromScor) throws NbaBaseException {
		//1. create transaction
		NbaLob tempLob = new NbaLob();
		tempLob.setReqType(reqType);

		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, tempLob);//NBREQRMNT,SATISFIED
		NbaTransaction nbaTransaction = parentCase.addTransaction(provider.getWorkType(), provider.getInitialStatus());
		nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		nbaTransaction.getTransaction().setLock(NbaConstants.NO_VALUE);

		//2. populate lobs
		NbaLob lob = nbaTransaction.getNbaLob();
		NbaLob caseLob = parentCase.getNbaLob();
		lob.setCompany(caseLob.getCompany());
		lob.setBackendSystem(caseLob.getBackendSystem());
		lob.setPolicyNumber(caseLob.getPolicyNumber());
		lob.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED));
		lob.setReqType(reqType);
		lob.setReqVendor(PROVIDER_PRODUCER);
		lob.setFirstName(caseLob.getFirstName());
		lob.setLastName(caseLob.getLastName());
		lob.setGender(caseLob.getGender());
		lob.setDOB(caseLob.getDOB());
		lob.setAgentID(caseLob.getAgentID());
		lob.setProductTypSubtyp(caseLob.getProductTypSubtyp());
		lob.setFaceAmount(caseLob.getFaceAmount());
		lob.setSsnTin(caseLob.getSsnTin());
		lob.setReqPersonCode(32);
		lob.setReqPersonSeq(1);

		//3. populate txlife
		NbaRequirementUtils reqUtils = new NbaRequirementUtils();
		reqUtils.setHoldingInquiry(nbaTxLife);
		reqUtils.setAutoGeneratedInd(true);
		reqUtils.setEmployeeId(nbaTransaction.getTransaction().getWorkType());

		RequirementInfo aReqInfo = reqUtils.createNewRequirementInfoObject(nbaTxLife, nbaTxLife.getPrimaryParty().getID(), user, lob);
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
		reqInfoExt.setReviewedInd(true);
		reqInfoExt.setReviewID(user.getUserID());
		reqInfoExt.setReviewDate(new Date());
		reqInfoExt.setReviewCode(String.valueOf(NbaConstants.REVIEW_NOT_REQUIRED));
		
		Policy policy = nbaTxLife.getPolicy();
		policy.addRequirementInfo(aReqInfo);
		policy.setActionUpdate();
		lob.setReqUniqueID(aReqInfo.getRequirementInfoUniqueID());
		reqUtils.setReqPersonCodeAndSeq(lob.getReqPersonCode(), lob.getReqPersonSeq());

		parentCase = updateWork(user, parentCase);
		addProviderResultSource(parentCase, nbaTransaction.getID(), mvrRespFromScor, reqUtils);		
	}

	protected void addProviderResultSource(NbaDst parentCase, String nbaTransactionID, String mvrResponse, NbaRequirementUtils reqUtils)
			throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(nbaTransactionID, false);
		NbaDst workItem = retrieveWorkItem(user, retrieveOptionsValueObject);
		addProviderResultSource(workItem, mvrResponse);
		reqUtils.addRequirementControlSource(workItem.getNbaTransaction());
		reqUtils.addMasterRequirementControlSource(parentCase, workItem.getNbaTransaction());
		workItem.setUpdate();
		updateWork(user, workItem);
		unlockWork(user, workItem);
	}

	protected void addProviderResultSource(NbaDst workItem, String mvrResponse) throws NbaBaseException {
		WorkItemSource newSource = new WorkItemSource();

		newSource.setCreate(NbaConstants.YES_VALUE);
		newSource.setRelate(NbaConstants.YES_VALUE);
		newSource.setSize(0);
		newSource.setPages(1);
		newSource.setFormat("T");

		newSource.setBusinessArea(A_BA_NBA);
		newSource.setSourceType(NbaConstants.A_ST_PROVIDER_RESULT);

		newSource.setLobData(new ArrayList());

		LobData newLob1 = new LobData();
		newLob1.setDataName(NbaLob.A_LOB_DISTRIBUTION_CHANNEL);
		newLob1.setDataValue(Long.toString(getWorkLobs().getDistChannel()));
		newSource.getLobData().add(newLob1);
		LobData newLob2 = new LobData();
		newLob2.setDataName(NbaLob.A_LOB_PORTAL_CREATED_INDICATOR);
		newLob2.setDataValue(String.valueOf(NbaConstants.TRUE));
		newSource.getLobData().add(newLob2);
		LobData newLob3 = new LobData();
		newLob3.setDataName(NbaLob.A_LOB_POLICY_NUMBER);
		newLob3.setDataValue(getWorkLobs().getPolicyNumber());
		newSource.getLobData().add(newLob3);

		newSource.setText(NbaUtils.getGUID());
		newSource.setFileName(null);
		newSource.setSourceStream(NbaBase64.encodeString(mvrResponse));
		workItem.getNbaTransaction().addNbaSource(new NbaSource(newSource));
	}

	protected NbaTXLife getNotificationResponse() throws NbaBaseException {
		List sources = getWork().getSources();
		for (int i = 0; i < sources.size(); i++) {
			NbaSource nbaSource = new NbaSource((WorkItemSource) sources.get(i));
			if (nbaSource.getSource().getSourceType().equals(NbaConstants.A_ST_XML1125)) {
				try {
					return new NbaTXLife(nbaSource.getSource().getText());
				} catch (Exception e) {
					throw new NbaBaseException(e);
				}
			}
		}
		return null;
	}
	
	// APSL3248 New method
	protected void setTherapeuticClassScore(PrescriptionDrug prescriptionDrug, int highestDiseaseSeverityCode) {
        if (highestDiseaseSeverityCode < 50) {
            highestDiseaseSeverityCode = 0;
        } else if (highestDiseaseSeverityCode < 100) {
            highestDiseaseSeverityCode = 5;
        } else {
            highestDiseaseSeverityCode = 10;
        }
        OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PRESCRIPTIONDRUG);
        PrescriptionDrugExtension prescriptionDrugExtension = oLifEExtension.getPrescriptionDrugExtension();
        prescriptionDrug.addOLifEExtension(oLifEExtension);
        prescriptionDrugExtension.setTherapeuticClassScore(highestDiseaseSeverityCode);
    }
}