package com.csc.fsg.nba.business.process;

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
 *     Copyright (c) 2002-2012 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.RetrieveWorkItemsRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.contract.validation.NbaSuitabilityCVResultsProcessor;
import com.csc.fsg.nba.database.NbaSuitabilityProcessingAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSuitabilityProcessingContract;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.SuitabilityVO;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.ActivityEvent;
import com.csc.fsg.nba.vo.txlife.ActivityExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ChangeSubType;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * NbaProcSuitability 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA297</td><td>Version ?</td><td>Suitability</td></tr>
 *  <tr><td>CR1345559</td><td>AXA Life Phase2</td><td>Suitability</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 1201
 * @see NbaAutomatedProcess
 */

public class NbaProcSuitability extends NbaProcSuitabilityBase {
	NbaProcessWorkItemProvider provider = null;

	public NbaProcSuitability() {
		super();
	}

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		NbaSuitabilityProcessingContract suitabilityCandidate = null;
		setUser(user);
		List suitabilityCandidates = NbaSuitabilityProcessingAccessor.selectContractsForSuitabilityCheck();
		for (Iterator iter = suitabilityCandidates.iterator(); iter.hasNext();) {
			try {
				suitabilityCandidate = (NbaSuitabilityProcessingContract) iter.next();
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Processing contract: " + suitabilityCandidate.getContractNumber());
				}
				initializeSuitabilityRecord(suitabilityCandidate);
			 	if (shouldCallSuitability(suitabilityCandidate)) {
			 		callSuitabilityBP();
			 		getWork().setNbaUserVO(getUser());
					updateWorkflow();
			 	} else if (shouldCreateNigoWorkItem()) {
			 		createMiscWork();
			 	}
			 	NbaSuitabilityProcessingAccessor.resetProcessingIndicators(suitabilityCandidate.getCompanyCode(), suitabilityCandidate
					.getContractNumber());
		

			} catch (NbaDataAccessException ndae) {
				getLogger().logException(ndae);
				ndae.forceFatalExceptionType(); //force error stop on database errors
				throw ndae;
			} catch (NbaBaseException nbe) {
				getLogger().logException(nbe);
				getLogger().logError("Unable to process contract: " + suitabilityCandidate.getContractNumber());
				if (nbe.isFatal()) {
					throw nbe;
				}
			} catch (Throwable e) {
				getLogger().logException(e);
				getLogger().logError("Exception occurred while processing Suitability for contract -" + suitabilityCandidate.getContractNumber());
			} finally {
				if (null != getWork() && getWork().isLocked(user.getUserID())) {
					try {
						unlockCase();
					} catch (NbaBaseException nbe) {
						getLogger().logError("Error unlocking work for contract: " + suitabilityCandidate.getContractNumber());
					}
				}
			}
		}

		if (suitabilityCandidates.size() > 0) {
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "");
		}
		return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", "");
	}

	protected void initializeSuitabilityRecord(NbaSuitabilityProcessingContract suitabilityCandidate) throws NbaBaseException {
		boolean shouldSuspend = true;
		try {
			setWork(retrieveCaseWorkItem(user, suitabilityCandidate.getContractNumber(), suitabilityCandidate.getCompanyCode()));
			setNbaTxLife(doHoldingInquiry());
			provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), new HashMap());
			if (null ==provider.getWorkType()) {
				throw new NbaBaseException("Invalid work type returned from VP/MS");
			}
			shouldSuspend = false;
		} catch (NbaBaseException exception) {
        	if (exception instanceof NbaLockedException) {
        		getLogger().logError("Locked workitem being bypassed.  ContractNumber:" + suitabilityCandidate.getContractNumber());
        	}
        	throw exception;
        } catch (Exception exp) {
        	throw new NbaBaseException(exp.getMessage());
        } finally {
        	try {
				if (shouldSuspend) {
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Suspending suitability record for contract:" + suitabilityCandidate.getContractNumber());
					}
					NbaSuitabilityProcessingAccessor.suspend(suitabilityCandidate);
				}
			} catch (NbaBaseException nbe) {
				nbe.forceFatalExceptionType();  //force error stop
				throw nbe;
			}
        }
	}
	protected void createMiscWork() throws NbaBaseException {
		getWork().getCase().getWorkItemChildren().add(prepareWorkItem());
		getWork().setNbaUserVO(getUser());
		updateWorkflow();
	}
	
	protected void updateWorkflow() throws NbaBaseException {
		AccelResult accelResult = new AccelResult();
		accelResult.merge(currentBP.callBusinessService("NbaUpdateWorkBP", getWork()));
		NewBusinessAccelBP.processResult(accelResult);
	}
	//NBLXA-1378 added pending status check
	protected boolean shouldCallSuitability(NbaSuitabilityProcessingContract suitabilityCandidate) {
		ApplicationInfoExtension extension = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
		if (null != extension && isIgo(extension) && (isFirstInvocation(extension) || suitabilityCandidate.isSubmitRequired())
				&& (getNbaTxLife().getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_PENDING)) {
			return true;
		}
		return false;
	}

	protected boolean isIgo(ApplicationInfoExtension extension) {
		return extension.getReadyForSuitabilityInd();
	}
	
	protected boolean isNigo(ApplicationInfoExtension extension) {
		return !isIgo(extension); 
	}
		
	protected boolean isFirstInvocation(ApplicationInfoExtension extension) {
		return extension.getSuitabilityDecisionStatus() == -1;
	}
	
	protected void callSuitabilityBP() throws Exception {
		AccelResult result = (AccelResult) ServiceHandler.invoke("SendSuitabilityRequestBP", ServiceContext.currentContext(),
				new SuitabilityVO(getUser(), getNbaTxLife()));
		if (result.hasErrors()) {
			throwNbaException(result, NbaExceptionType.FATAL);
		}
		addCommentForResubmit(); // NBLXA-2308[NBLXA-2303]
		// Begin NBLXA2303[NBLXA2317]
		ApplicationInfoExtension extension = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
		if (!NbaUtils.isBlankOrNull(extension)) {
			Date now = new Date();
			updateApplicationInfoExtension(NbaOliConstants.NBA_SUITABILITYDECISIONSTATUS_PENDING, now, now);
			setContractAccess(UPDATE);
			doContractUpdate();
		}
		// End NBLXA2303[NBLXA2317]
		NbaTXLife response = (NbaTXLife) result.getFirst();

		if (isSuitabilityCallSuccessful(response)) {
			if (response.getOLifE().getActivityCount() > 0) { // P2AXAL021, FUNC80839 - removed update check
				ActivityExtension activityExtension = NbaUtils.getFirstActivityExtension((Activity) response.getOLifE().getActivity().get(0));
				if (null != activityExtension && activityExtension.getActivityEventCount() > 0) { // P2AXAL021
					ActivityEvent activityEvent = (ActivityEvent) activityExtension.getActivityEvent().get(0);
					Date now = new Date();
					if (activityEvent.hasStatusEventCode()
							&& activityEvent.getStatusEventCode() != NbaOliConstants.NBA_SUITABILITYDECISIONSTATUS_INVALID) { // NBLXA2303[NBLXA2317]
																																// // P2AXAL021 //
						updateApplicationInfoExtension(activityEvent.getStatusEventCode(), now, now);
						setContractAccess(UPDATE); // CR1345559
						doContractUpdate(); // CR1345559
					} // P2AXAL021
				} // P2AXAL021
			} // P2AXA021
		} else {
			// Begin NBLXA2303[NBLXA2598]
			TransResult transResult = response.getTransResult();
			List list = new ArrayList();
			for (Iterator iter = transResult.getResultInfo().iterator(); iter.hasNext();) {
				list.add(((ResultInfo) iter.next()).getResultInfoDesc());
			}
			addComments(list); // TODO should this be done always rather than in this guard?
			if (failedWorkItemNeeded() && !isSuitabilitySystemErrors(response)) {
				createMiscWork();
			} else {
				if (isSuitabilitySystemErrors(response)) {
					if (!NbaUtils.isBlankOrNull(extension)) {
						Date now = new Date();
						updateApplicationInfoExtension(LONG_NULL_VALUE, now, now);
						setContractAccess(UPDATE);
						doContractUpdate();
					}
				}
			}
			getWork().setNbaUserVO(getUser());
			updateWorkflow();
			// End NBLXA2303[NBLXA2598]
			NbaSuitabilityProcessingAccessor.suspend(getNbaTxLife().getPolicy().getCarrierCode(), getNbaTxLife().getPolicy().getPolNumber());
			throw new NbaBaseException("Suitability system returned a failure.");
		}
	}

	//NBLXA2303[NBLXA2598] New Method
	private boolean isSuitabilitySystemErrors(NbaTXLife response) {
		TransResult transResult = response.getTransResult();
		List<ResultInfo> resultInfoList = transResult.getResultInfo();
		for (ResultInfo resInfo : resultInfoList) {
			if (!NbaUtils.isBlankOrNull(resInfo.getResultInfoCode()) && ((resInfo.getResultInfoCode() == NbaOliConstants.TC_RESINFO_DUPLICATEOBJ
					&& resInfo.getResultInfoDesc().contains(SUITFAIL_RESUBMITPOL))
					|| resInfo.getResultInfoCode() == NbaOliConstants.TC_RESINFO_OBJECTNOTFOUND
					|| resInfo.getResultInfoCode() == NbaOliConstants.OLI_OTHER
					|| resInfo.getResultInfoCode() == NbaOliConstants.TC_RESINFO_GENERALERROR 
					|| resInfo.getResultInfoCode() == NbaOliConstants.TC_RESINFO_ELEMENTINVALID)) {
				return true;
			}
		}
		return false;
	}
	protected boolean shouldCreateNigoWorkItem() throws NbaBaseException {
		ApplicationInfoExtension extension = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
		if (null != extension && isNigo(extension) && nigoWorkItemNeeded()){
			return true;
		}
		return false;
	}

	protected boolean nigoWorkItemNeeded() throws NbaNetServerDataNotFoundException {
		return !workItemExisits();
	}
	
	protected boolean failedWorkItemNeeded() throws NbaNetServerDataNotFoundException {
		return !workItemExisits();
	}

	protected boolean workItemExisits() throws NbaNetServerDataNotFoundException {
		List txns = getWork().getTransactions();
		for (int x=0;x<txns.size();x++) {
			WorkItem workItem = (WorkItem)txns.get(x);
			if (provider.getWorkType().equals(workItem.getWorkType())) {
				if (!END_QUEUE.equals(workItem.getQueueID())) {
					if (provider.getInitialStatus().equals(workItem.getStatus())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected boolean isSuitabilityCallSuccessful(NbaTXLife response) {
		return response.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS || 
		response.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESSINFO;
	}
	
	protected void throwNbaException(Result result, NbaExceptionType type) throws NbaBaseException {
		Message[] messages = result.getMessages();
		Message message;
		String messagesStr = ""; 
		String errorVal = "";
		List data;
		if (messages != null && messages.length > 0) {
			for (int i = 0; i < messages.length; i++) {
				message = messages[i];
			    errorVal = message.format();
				if (errorVal == null || errorVal.equals(Message.ERR_MESSAGE_MISSING)) {
					data = message.getData();
					if(data != null){
						errorVal += " data[" + data.toString() + "]";
					}
				}
				messagesStr += " " + errorVal;
			}
		}
		throw new NbaBaseException(messagesStr, type);
	}

	protected WorkItem prepareWorkItem() throws NbaBaseException {
		WorkItem workItem = null;
		List txns = getWork().getTransactions();
		for (int x=0;x<txns.size();x++) {
			workItem = (WorkItem)txns.get(x);
			if (provider.getWorkType().equals(workItem.getWorkType())) {
				if (END_QUEUE.equals(workItem.getQueueID())) {
					workItem.setLobData(makeLobDataList());
					lockMiscWork(workItem.getItemID());
					workItem.setStatus(provider.getInitialStatus());
					workItem.setUpdate("Y");
					return workItem;
				}
			}
		}
		
        workItem = new WorkItem();
        workItem.setBusinessArea(getWork().getBusinessArea());
		workItem.setLobData(makeLobDataList());
        workItem.setWorkType(provider.getWorkType());
		workItem.setStatus(provider.getInitialStatus());
        workItem.setCreate("Y");
		return workItem;
	}

	protected void lockMiscWork(String workItemID) throws NbaBaseException {
		RetrieveWorkItemsRequest retrieveWorkItemsRequest = new RetrieveWorkItemsRequest();
		retrieveWorkItemsRequest.setUserID(getUser().getUserID());
		retrieveWorkItemsRequest.setWorkItemID(workItemID);
		retrieveWorkItemsRequest.setRecordType(workItemID.substring(26, 27));
		retrieveWorkItemsRequest.setLockWorkItem();
		lockWorkItem(retrieveWorkItemsRequest);	
		
	}
	protected List makeLobDataList() {
		List list = new ArrayList();
		list.add(getWork().getWorkItem().getLob(NbaLob.A_LOB_POLICY_NUMBER));
		list.add(getWork().getWorkItem().getLob(NbaLob.A_LOB_FIRST_NAME)); //FSNM
		list.add(getWork().getWorkItem().getLob(NbaLob.A_LOB_LAST_NAME)); //LSNM
		list.add(getWork().getWorkItem().getLob(NbaLob.A_LOB_SSN_TIN)); //IRSN
		list.add(getWork().getWorkItem().getLob(NbaLob.A_LOB_COMPANY)); //COID
		list.add(getWork().getWorkItem().getLob(NbaLob.A_LOB_BACKEND_SYSTEM)); //SYST // ALII1248
		list.add(getWork().getWorkItem().getLob(NbaLob.A_LOB_OPERATING_MODE)); //OPMD // ALII1248
		return list;
	}
	
	////NBLXA-2308[NBLXA-2303]: New Mthod
	protected void addCommentForResubmit() throws Exception {
		String commentString = null;
		String companyCode = getNbaTxLife().getCarrierCode();
		String polNumber = getNbaTxLife().getPolicy().getPolNumber();
		NbaSuitabilityProcessingContract suitabilityContract = NbaSuitabilityProcessingAccessor.retrieve(companyCode, polNumber);
		List<ChangeSubType> changeSubTypeList = NbaSuitabilityCVResultsProcessor
				.parseSuitabilityResubmitData(suitabilityContract.getSuitabilityResubmitData());
		if (!NbaUtils.isBlankOrNull(changeSubTypeList)) {
			for (ChangeSubType changeSubType : changeSubTypeList) {
				if (!NbaUtils.isBlankOrNull(changeSubType)) {
					if (NbaUtils.isBlankOrNull(commentString)) {
						commentString = "Re-Evaluation triggered for the following field changes: ";
					}
					if (changeSubType.getChangeTC() == NbaOliConstants.OLI_CHG_PREMAMT) {
						commentString = commentString + "Annualized Premium Amount Increase ";
					} else if (changeSubType.getChangeTC() == NbaOliConstants.OLI_CHG_CHGPARTICBIRTHDATE) {
						commentString = commentString + "Insured Date of Birth ";
					} else if (changeSubType.getChangeTC() == NbaOliConstants.OLI_CHG_OPTCHG
							&& NbaConstants.LTCREPLACEMENT_IND_CODE.equalsIgnoreCase(changeSubType.getElementName())) {// NBLXA2303[NBLXA2316]
						commentString = commentString + "LTC Rider Replacement Yes/No";
					} else if (changeSubType.getChangeTC() == NbaOliConstants.OLI_CHG_OPTCHG) {// NBLXA2303[NBLXA2316]
						commentString = commentString + "LTC Rider Add/Delete ";
					}
				}
			}
		}
		if (!NbaUtils.isBlankOrNull(commentString)) {
			addComment(commentString);
		}
	}
}
