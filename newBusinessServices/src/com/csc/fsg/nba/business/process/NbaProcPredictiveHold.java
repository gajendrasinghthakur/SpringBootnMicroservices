package com.csc.fsg.nba.business.process;

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
 * 
 * *******************************************************************************<BR>
 */
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.evaluation.NbaVpmsModelProcessor;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDatabaseLockedException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaPollingException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaEvaluateRequest;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.Message;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.PredictiveResult;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.TentativeDisp;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UnderwritingResult;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.EvaluationControlModelResults;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * NbaProcPredictiveHold is the class that processes work items found
 * in AP Predictive Hold queue (N2PRDHLD).
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SR564247-Full</td><td>AXA Life Phase 2</td><td>Predictive Full Implementation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 * @see NbaAutomatedProcess
 */
public class NbaProcPredictiveHold extends NbaAutomatedProcess {
	protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";

	private NbaDst parentCase = null;

	private String awdTime; // APSL2634

	private boolean isUnsuspensionNeeded = false;// ALII1786

	/**
	 * This method drive the Predictive Hold Automated process.
	 * - It will suspend case for 15 minutes if MIB requirement is not received. 
	 * - The suspension of case will continue after every 15 minutes if the requirement is not received.
	 * - It Will invoke Predictive service
	 * - After invoking the service, the process will suspend case for 1 day to wait for the result
	 * - Reviews a list of requirements systematically if they are received and case is jet Issue eligible
	 * - Update and Unlock the WorkItem  
	 * @param user the NbaUser for whom the process is being executed
	 * @param work a NbaWorkItem value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 * @throws NbaBaseException
	 * @throws NbaNetServerDataNotFoundException
	 * @throws NbaVpmsException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaNetServerDataNotFoundException, NbaBaseException, NbaAWDLockedException { //ALII1652
		if (!initialize(user, work)) {
			return getResult();
		}
		try {
			boolean updateContract = false;
			// Set AWD Time to suspend cases as according to AWD time
			setAwdTime(getTimeStamp(user)); // APSL2634
			// Do processing for transaction.
			if (work.isTransaction()) {
				if (work.getWorkType().equalsIgnoreCase("NBSVRSLT")) {
					updateContract = processServiceResult();
				} else if (work.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REEVALUATE)) {
					updateContract = processReevaluation();
				} else {
					throw new NbaBaseException("The work item can not be processed by Predictive Hold process.");
				}
			} else {
				// Do processing for NBAPPLCTN.
				updateContract = processApplication();
			}
			// If the poller result has not been set so far, increase its success count.
			if (getResult() == null) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}
			// Update work
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();

			if (updateContract) {
				getNbaTxLife().setAccessIntent(getContractAccess());
				NbaContractAccess.doContractUpdate(getNbaTxLife(), (parentCase != null) ? parentCase : getWork(), getUser());
				// doContractUpdate();
			}
			NbaContractLock.removeLock(getUser());
			if (parentCase != null) {
				if (isUnsuspensionNeeded) { // ALII1786
					unsuspendCase(parentCase);
				}
				unlockWork(parentCase);
			}
		} catch (NbaAWDLockedException le) { // ALII1652
			unlockWork();
		} catch (NbaDatabaseLockedException dble) {// SR564247(ALII1688) Starts
			unlockWork();
			setWork(getOrigWorkItem());
			NbaLockedException nle = new NbaLockedException(dble.getMessage());
			throw nle; // SR564247(ALII1688) Ends
		} //APSL3874 
		//APSL3874 code deleted 
		 finally { // Begin ALII1792
			if (getResult() == null) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
			}
		} // End ALII1792
		return result;
	}

	protected boolean processServiceResult() throws NbaBaseException, NbaAWDLockedException { // ALII1652
		boolean updateContract = false;
		// Take backup of status provider as we want to do main processing with NBAPPLCTN work item.
		// originalTransaction = getWork();
		NbaProcessStatusProvider originalStatus = getStatusProvider();
		// Search for matching NBAPPLCTN and set it as work for this auto process.
		NbaTXLife tx151 = lookupCase();
		// If matching NBAPPLCTN is found in N2PRDHLD, we need to merge the response.
		if (getWork().getNbaLob().getQueue().equalsIgnoreCase(getQueue())) {
			// Get outbound statuses from VPMS for NBAPPLCTN. We are calling initialize again because the first call was made for NBSVRSLT.
			// initialize(user, getWork());
			NbaProcessStatusProvider provider = new NbaProcessStatusProvider(getUser(), getWork());
			// Merge Pending DB with Tx151 response.
			mergeResponse(tx151);
			// Unsuspend matching case.
			// Removed code- ALII1786
			isUnsuspensionNeeded = true; // ALII1786
			// Wao! The response has matched, so set pass status returned by VPMS as the outbound status for NBAPPLCTN.
			// getWork().setStatus(provider.getPassStatus());
			// If due to some reason the response was not merged earlier, NBSVRSLT would have been linked to NBAPPLCTN and its POLN LOB would have
			// been set. We need to unlink them now and remove POLN from NBSVRSLT so that it does not shows up in Search results when searched by
			// policy number.
			unlinkPredictiveResultWI();
			getOrigWorkItem().getNbaLob().deletePolicyNumber();
			// Processing related to the case qualified for Jet Issue...
			if (nbaTxLife.getPolicy().getIssueType() == NbaOliConstants.OLI_COVISSU_REDUCEDUNDERWRITING) {
				setRateClass(tx151);
				reviewRequirements();
			} else {
				// As the case did not qualified for Jet Issue, it can not be Predictive anymore.
				// Begin CR1345857
				setWork(retrieveParentWithTransactions(work, user));
				List transactions = getWork().getNbaTransactions();
				int count = transactions.size();
				NbaTransaction nbaTransaction = null;
				for (int i = 0; i < count; i++) {
					nbaTransaction = (NbaTransaction) transactions.get(i);
					if (NbaConstants.A_WT_AGGREGATE_CONTRACT.equalsIgnoreCase(nbaTransaction.getWorkType())&&NbaConstants.A_QUEUE_AGGREGATE_CONTRACT.equalsIgnoreCase(nbaTransaction.getNbaLob().getAggrReference())) {
						nbaTransaction.getNbaLob().setPredictiveInd(false);
						nbaTransaction.setUpdate();
					}
				}
				// End CR1345857
				getWork().getNbaLob().setPredictiveInd(false);
				PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
				policyExtension.setPredictiveInd(false);
				policyExtension.setActionUpdate();
			}
			// Wao! The response has matched, so set pass status returned by VPMS as the outbound status for NBAPPLCTN.
			getWork().setStatus(provider.getPassStatus());
			// Update Pending DB.
			updateContract = true;
		} else {
			// Matching case was found , but it is not in N2PRDHLD.
			// Link NBSVRSLT with NBAPPLCTN and route it to ERROR queue. This is required because when NBCM will open the work item, he should be able
			// to view case data on different tabs.
			linkPredictiveResultWI();
			getOrigWorkItem().getNbaLob().setPolicyNumber(getWork().getNbaLob().getPolicyNumber());
			getOrigWorkItem().getNbaLob().setCompany(getWork().getNbaLob().getCompany()); // ALII1812
			getOrigWorkItem().getNbaLob().setBackendSystem(getWork().getNbaLob().getBackendSystem()); // ALII1812
			// Add an automated comment
			addComment("Predictive response cannot be matched as the case is not in Hold queue");
			// Because the response was not merged, next status of NBSVRSLT should be failure status from VPMS and poller failure count should
			// increase.
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED,
					"Predictive response cannot be matched as the case is not in Hold queue", getFailStatus()));
			getOrigWorkItem().setStatus(getFailStatus());
			getOrigWorkItem().setActionUpdate();
			// update(getOrigWorkItem());
		}
		// Whether the response was merged or not, NBAPPLCTN was updated, and we need to commit the changes.
		parentCase = update(getWork());
		// Processing related to NBAPPLCTN is over, so we need to set back NBSVRSLT as work for auto process.
		setWork(getOrigWorkItem());
		setStatusProvider(originalStatus);
		return updateContract;
	}

	protected boolean processReevaluation() throws NbaBaseException {
		removeErrorMessage(); // ALII1609
		// Deleted code for "ALII1958"
		setWork(retrieveParentWork(work, true, false));
		NbaProcessStatusProvider origProvider = getStatusProvider();
		setStatusProvider(new NbaProcessStatusProvider(getUser(), getWork()));
		// updateContract = processApplication();
		getWork().setStatus(getOrigWorkItem().getStatus());
		getWork().getNbaLob().deleteRouteReason(); // ALII1717
		parentCase = update(getWork());
		// Deleted code for "ALII1958" and moved the processApplication() call below.
		processApplication();
		setWork(getOrigWorkItem());
		setStatusProvider(origProvider);
		getWork().getNbaLob().setReevalSubType(false);
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		return true;
	}

	protected boolean processApplication() throws NbaBaseException {
		boolean updateContract = false;
		// If Predictive service has not been invoked yet, NBAPPLCTN is being processed for the first time.
		boolean predictiveServiceInvoked = isPredictiveServiceInvoked();
		if (!predictiveServiceInvoked) {
			// If MIB is in received status, invoke Predictive service.
			boolean mibReceived = isMIBReceived(work.getNbaLob());
			if (mibReceived) {
				if (invokePredictiveWebService()) {
					// Create PredictiveResult with CreateDate and suspend NBAPPLCTN to wait for matching response.
					ArrayList predictiveResultList = new ArrayList();
					NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
					Policy policy = nbaTxLife.getPolicy();
					PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
					PredictiveResult predictiveResult = new PredictiveResult();
					predictiveResult.setCreateTime(new NbaTime(new Date()));
					predictiveResult.setActionAdd();
					nbaOLifEId.setId(predictiveResult);
					predictiveResultList.add(predictiveResult);
					policyExt.setPredictiveResult(predictiveResultList);
					suspendWork(Calendar.DATE, Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
							NbaConfigurationConstants.PRED_SUSPEND_DAYS)));
					updateContract = true;
				}
			} else {
				// Suspend NBAPPLCTN for 1 day if MIB is not received ( for the first Time)
				// Verify if the poller is obtaining lock for the second time & MIB still not received
				// Begin CR1454508(APSL2681)
				if (work.getNbaLob().getPredictiveSusp()) {
					// In case MIB is still not received, error Stop the Poller.
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "MIB not Received for Policy - "
							+ work.getNbaLob().getPolicyNumber(), getHostErrorStatus()));
					throw new NbaBaseException("MIB not Received for Policy - " + work.getNbaLob().getPolicyNumber() + "  "
							+ this.getClass().getName() + " ", NbaExceptionType.FATAL);
				} else {
					work.getNbaLob().setPredictiveSusp(true);
					work.setUpdate();
					update(work);
					suspendWork(Calendar.DATE, Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
							NbaConfigurationConstants.PRED_SUSPEND_DAYS)));
				}
				// End CR1454508(APSL2681)
			}

		} else {
			// Begin SR564247(APSL2180)
			PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
			ArrayList predictiveResultList = policyExt.getPredictiveResult();
			int errorCounter = 0;
			for (int i = 0; i < predictiveResultList.size(); i++) {
				PredictiveResult predResult = (PredictiveResult) predictiveResultList.get(i);
				if (predResult.getResponseTime().getTime() == null && predResult.getCreateTime().getTime() != null) {
					if (predResult.getSuspendForResponseInd()) {
						errorCounter++;
					}
				}
			}
			// If the case is routed to PRDHLD then suspend the case for a day and set the SuspendForResponseInd to false for all the predictive
			// results of that case
			if (errorCounter > 0) {
				suspendWork(Calendar.DATE, Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
						NbaConfigurationConstants.PRED_SUSPEND_DAYS)));
				for (int i = 0; i < predictiveResultList.size(); i++) {
					PredictiveResult predResult = (PredictiveResult) predictiveResultList.get(i);
					if (predResult.getResponseTime().getTime() == null && predResult.getCreateTime().getTime() != null) {
						predResult.setSuspendForResponseInd(false);
						predResult.setActionUpdate();
						updateContract = true;
					}
				}
			} else {
				// NBAPPLCTN is being processed once more, that means response has not came yet. Set next status as failure status and increase
				// failure count of the poller and set SuspendForResponseInd to true for all the predictive results ot the case
				addComment("Predictive response not received - Needs Resolution");
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Predictive Webservice Response Not Received",
						getHostErrorStatus()));
				for (int i = 0; i < predictiveResultList.size(); i++) {
					PredictiveResult predResult = (PredictiveResult) predictiveResultList.get(i);
					if (predResult.getResponseTime().getTime() == null && predResult.getCreateTime().getTime() != null) {
						predResult.setSuspendForResponseInd(true);
						predResult.setActionUpdate();
						updateContract = true;
					}
				}
			}
			// END SR564247(APSL2180)
		}
		return updateContract;
	}

	protected void reviewRequirements() throws NbaBaseException {
		Policy policy = getNbaTxLife().getPolicy();
		int size = policy.getRequirementInfoCount();
		RequirementInfo reqInfo = null;
		ArrayList reqList = getRequirementsForSystematicReview();
		for (int i = 0; i < size; i++) {
			reqInfo = policy.getRequirementInfoAt(i);
			String reqCodeStr = Long.toString(reqInfo.getReqCode());
			long reqStatus = reqInfo.getReqStatus();
			if (reqList.contains(reqCodeStr)) {
				NbaDst requirementDst = getRequirementWI(policy.getPolNumber(), reqInfo.getReqCode());
				RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
				if (null != reqInfoExt && null != requirementDst) {
					if (reqStatus == NbaOliConstants.OLI_REQSTAT_RECEIVED) {
						reqInfoExt.setReviewedInd(true);
						reqInfoExt.setReviewDate(new Date());
						reqInfoExt.setReviewID(user.getUserID());
					}
					requirementDst.getNbaLob().setReview(NbaConstants.REVIEW_SYSTEMATIC);
					reqInfoExt.setReviewCode(String.valueOf(NbaConstants.REVIEW_SYSTEMATIC));
					requirementDst.getNbaTransaction().setUpdate();
					reqInfoExt.setActionUpdate();
					update(requirementDst);
					unlockWork(requirementDst);
				}
			}
		}
	}

	/**
	 * This method is used to call the Evaluation Control model
	 * @param
	 * @return ArrayList
	 * @throws NbaBaseException
	 */

	private ArrayList callEvaluationControl(NbaDst work) throws NbaBaseException {
		ArrayList sortedList = null; // ACP007
		NbaVpmsAdaptor vpmsProxy = null; // SPR3362
		try {

			NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(nbaTxLife); // SPR2450, AXAL3.7.07
			oinkDataAccess.setLobSource(work.getNbaLob()); // SPR2450
			vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, NbaVpmsAdaptor.EVALUATIONCONTROL); // ACP008,SPR2450
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_CALCXMLOBJECTS); // ACP008
			Map deOink = new HashMap();
			int reqCode = work.getNbaLob().getReqType();
			deOink.put("A_REQCODE_INS", String.valueOf(reqCode));
			deOink.put("A_XMLRESPONSE", "true");
			deOink.put("A_INSTALLATION", getInstallationType());
			deOink.put("A_WORKTYPE_LOB", work.getNbaLob().getWorkType());
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult vcr = vpmsProxy.getResults();
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vcr);
			ArrayList results = vpmsResultsData.getResultsData();
			results = vpmsResultsData.getResultsData();
			// Resulting string will be the zeroth element.
			NbaVpmsModelResult vpmsOutput = null;
			VpmsModelResult vpmsModelResult = null;
			if (results == null) {
				throw new NullPointerException("ERROR: NULL RESULTS from VPMS");
			} else {
				String result = (String) results.get(0);
				vpmsOutput = new NbaVpmsModelResult(result);
				vpmsModelResult = vpmsOutput.getVpmsModelResult();
			}
			// SPR3362
			ArrayList modelResults = vpmsModelResult.getEvaluationControlModelResults();
			SortedMap map = new TreeMap();
			// Begin ACP007
			// Modified logic to use SortedMap
			for (int i = 0; i < modelResults.size(); i++) {
				EvaluationControlModelResults modelResult = (EvaluationControlModelResults) modelResults.get(i);
				Integer key = new Integer(modelResult.getProcessSequence().toString());
				map.put(key, modelResult);
			}
			// Iterate on treemap and convert to sorted ArrayList
			Set set = map.entrySet();
			Iterator itr = set.iterator();
			sortedList = new ArrayList();
			while (itr.hasNext()) {
				Map.Entry me = (Map.Entry) itr.next();
				sortedList.add(me.getValue());
			}
			// End ACP007
		} catch (NbaVpmsException e) {
			throw new NbaBaseException("NbaVpmsException Exception occured in callEvaluationControl", e);
		} catch (RemoteException e) {
			throw new NbaBaseException("NbaVpmsException Exception occured in callEvaluationControl", e);
			// begin SPR3362
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (RemoteException re) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		// end SPR3362
		return sortedList;
	}

	// Generate Impairments
	protected void generateImpairments() throws NbaBaseException {
		// ACN010
		ArrayList al = callEvaluationControl(work);
		EvaluationControlModelResults evalModel = null;
		// SPR3290 code deleted
		for (int i = 0; i < al.size(); i++) {
			evalModel = (EvaluationControlModelResults) al.get(i);
			NbaVpmsModelProcessor processor;
			try {
				processor = (NbaVpmsModelProcessor) NbaUtils.classForName(evalModel.getJavaImplClass()).newInstance();
				processor.initialize(nbaTxLife, user, work); // SPR2741
				processor.execute(); // SPR2741
			} catch (InstantiationException e) {
				throw new NbaBaseException(NbaPollingException.CLASS_INVALID, e);
			} catch (IllegalAccessException e) {
				throw new NbaBaseException(NbaPollingException.CLASS_ILLEGAL_ACCESS, e);
			} catch (ClassNotFoundException e) {
				throw new NbaBaseException(NbaPollingException.CLASS_NOT_FOUND, e);
				// begin SPR2741
			} catch (NbaBaseException nbe) {
				if (nbe.isFatal()) {
					throw nbe;
				} else {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, nbe.toString(), getHostErrorStatus()));
					addComment(getResult().getText());
					changeStatus(getResult().getStatus());
					break;
				}
			}
			// end SPR2741
		}

	}

	/**
	 * This method will return list of requirements which are 
	 * eligible for systematic review by predictive poller if case is eligible for jet issue.	 
	 */
	protected ArrayList getRequirementsForSystematicReview() throws NbaVpmsException {
		NbaVpmsAdaptor vpmsProxy = null;
		ArrayList reqList = new ArrayList();
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.PREDICTIVE_ANALYSIS);
			vpmsProxy.setVpmsEntryPoint(EP_GET_ELIGIBLESYSREV_REQUIREMENT_LIST);
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult compResult = vpmsProxy.getResults();
			NbaStringTokenizer tokens = new NbaStringTokenizer(compResult.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			String aToken;
			while (tokens.hasMoreTokens()) {
				aToken = tokens.nextToken();
				reqList.add(aToken);
			}
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("Predictive Hold Poller Problem" + NbaVpmsException.VPMS_EXCEPTION, re);
		} catch (NbaBaseException e) {
			getLogger().logDebug(
					"Exception occurred while getting Eligible System Review Requirement Types For Jet Cases From PREDICTIVE_ANALYSIS VPMS Model : "
							+ NbaUtils.getStackTrace(e));
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}
		return reqList;
	}

	/**
	 * Search for Case work items which can be matched to the current work item.
	 * @return the search value object containing the results of the search
	 * @throws NbaBaseException
	 * @throws RemoteException
	 * @throws NbaNetServerDataNotFoundException
	 * @throws NbaVpmsException
	 */

	protected NbaTXLife lookupCase() throws NbaBaseException {
		NbaTXLife aNbaTXLife = null;
		try {
			// Retrieve NBAPPLCTN with all NbaSource objects. Records in NBA_SYSTEM_DATA are also retrieved.
			NbaDst aWork = retrieveWorkItem(work);
			List sources = aWork.getNbaSources();
			NbaSource aSource = null;
			// Look for Predictive response
			for (int i = 0; i < sources.size(); i++) {
				aSource = (NbaSource) sources.get(i);
				if (aSource.getSourceType().equalsIgnoreCase(NbaConstants.A_ST_XML151)) {
					break;
				}
			}
			// Policy number is in Predictive response which will be key to find case.
			aNbaTXLife = new NbaTXLife(aSource.getText());
			String policyNumber = aNbaTXLife.getPolicy().getPolNumber();
			NbaSearchVO searchVO = new NbaSearchVO();
			searchVO.setResultClassName(CASE_SEARCH_VO);
			searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
			searchVO.setContractNumber(policyNumber);
			searchVO = lookupWork(getUser(), searchVO);
			List searchResult = searchVO.getSearchResults();
			if (searchResult.size() == 0) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
				throw new NbaBaseException("No Matching case found " + this.getClass().getName() + " ", NbaExceptionType.FATAL);
			}
			// Obtain lock on NBAPPLCTN.
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(((NbaSearchResultVO) searchVO.getSearchResults().get(0)).getWorkItemID(), true);
			retOpt.setLockWorkItem();
			setWork(retrieveWorkItem(getUser(), retOpt));
		} catch (NbaLockedException e) {
			// Suspend NBAPPLCTN if lock can not be obtained, and increase failure count of poller.
			NbaBootLogger.log("Unable to get lock from AWD so suspending for 15 minutes");
			setWork(getOrigWorkItem());
			suspendWork(Calendar.MINUTE, Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
					NbaConfigurationConstants.PRED_SUSPEND_MINS)));
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
			throw e;
		} catch (Exception exception) {
			throw new NbaBaseException(exception);
		}
		return aNbaTXLife;
	}

	/**
	 * If ReplacementInd is set and Policy Jurisdiction is New York (tc 37) return true
	 * 
	 * @param NbaTXLife
	 * @throws NbaBaseException
	 * @result boolean
	 */
	protected boolean isMIBReceived(NbaLob nbaLob) throws NbaBaseException {
		NbaLob lob = new NbaLob();
		lob.setPolicyNumber(nbaLob.getPolicyNumber());
		lob.setReqType(NbaConstants.MIB_REQT_TYPE);
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
			NbaSearchResultVO latestMibReqVO = null;// ALII1969
			Date aDate = null; // ALII1969
			for (int i = 0; i < searchResult.size(); i++) {// ALII1786
				NbaSearchResultVO resultVO = (NbaSearchResultVO) searchResult.get(i);
				// Start ALII1969
				try {
					String stringDate = resultVO.getNbaLob().getCreateDate();
					Date createDate = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").parse(stringDate.substring(0, stringDate.length() - 3));
					if (aDate == null) {
						aDate = createDate;
						latestMibReqVO = resultVO;
					} else if (createDate.after(aDate)) {
						aDate = createDate;
						latestMibReqVO = resultVO;
					}
				} catch (ParseException ex) {
				}
			}
			if (latestMibReqVO.getNbaLob().getReqStatus().equalsIgnoreCase(Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED))) {
				return true;
			}
			// End ALII1969
		}
		return false;
	}

	/**
	 * If ReplacementInd is set and Policy Jurisdiction is New York (tc 37) return true
	 * 
	 * @param NbaTXLife
	 * @result boolean
	 */
	protected boolean isPredictiveServiceInvoked() {
		PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
		ArrayList predictiveResultList = policyExt.getPredictiveResult();
		for (int i = 0; i < predictiveResultList.size(); i++) {
			PredictiveResult predResult = (PredictiveResult) predictiveResultList.get(i);
			if (predResult.getResponseTime().getTime() == null && predResult.getCreateTime().getTime() != null) {
				return true;
			}
		}
		return false;
	}

	/**Takes activate minutes for a workitem, and returns suspendVO object for this work item.
	 * @throws NbaBaseException
	 */
	protected NbaSuspendVO getSuspendWorkVO(int type, int value) {
		GregorianCalendar cal = new GregorianCalendar();
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		cal.setTime(parseTimeStamp(getAwdTime())); // APSL2634
		cal.add(type, value);
		suspendVO.setTransactionID(getWork().getID());
		suspendVO.setActivationDate(cal.getTime());
		suspendVO.setKeepLock(false);//APSL4400 NBA-932
		return suspendVO;
	}

	protected boolean invokePredictiveWebService() throws NbaBaseException {
		//APSl3874 code deleted 
		NbaTXLife nbaTXLifeResponse = null;// APSL 3874
		try {
			AxaWSInvoker wsInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_PREDICTIVE_ORCHESTRATION_REQUEST, user,
					nbaTxLife, work, null);
			nbaTXLifeResponse = (NbaTXLife) wsInvoker.execute();
			//APSL3874 code deleted
		} catch (NbaBaseException e) {
			//Begin APSL3874
			if (nbaTXLifeResponse != null) {
				TransResult transResult = nbaTXLifeResponse.getTransResult();
				long resultCode = transResult.getResultCode();
				if (!((NbaOliConstants.TC_RESCODE_SUCCESS == resultCode) || (NbaOliConstants.TC_RESCODE_PENDINGINFO == resultCode))) {
                if (NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL == resultCode || NbaOliConstants.TC_RESINFO_SECVIOLATION == resultCode
							|| NbaOliConstants.TC_RESINFO_UNABLETOPROCESS == resultCode) {
						throw new NbaBaseException("Error accessing WebService " + this.getClass().getName() + " ", NbaExceptionType.FATAL);
					}
				}
			} else {
				throw e;

			}
		}
		return true;
		// End APSL3874
	}

	/**
	 * Calls unsuspendWork method to unsuspend the workitem.
	 * @throws NbaBaseException
	 */
	// ALII1786 - Updated code
	protected void unsuspendCase(NbaDst parentCase) throws NbaBaseException {
		if (parentCase.isSuspended()) {
			NbaSuspendVO suspendVO = new NbaSuspendVO();
			suspendVO.setCaseID(parentCase.getID());
			unsuspendWork(suspendVO);
		}
	}

	protected void mergeResponse(NbaTXLife responseTXLife) throws NbaBaseException {
		NbaContractLock.requestLock(getWork(), getUser()); // SR564247(ALII1688)
		setNbaTxLife(doHoldingInquiry(getWork(), NbaConstants.UPDATE, NbaUtils.getBusinessProcessId(getUser())));
		PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
		PolicyExtension respPolicyExtn = NbaUtils.getFirstPolicyExtension(responseTXLife.getPolicy());
		int score = -1; // APSL2209
		// Code Deleted for QC APSL4582,APSL4583
		if (respPolicyExtn != null) {
			policyExtn.setPredRateClassVerifiyInd(respPolicyExtn.getPredRateClassVerifiyInd());
			policyExtn.setPredMissingDataInd(respPolicyExtn.getPredMissingDataInd());
			policyExtn.setPredApplicationReviewInd(respPolicyExtn.getPredApplicationReviewInd());
			policyExtn.setPredDisabilityWaiverReviewInd(respPolicyExtn.getPredDisabilityWaiverReviewInd());
			policyExtn.setPredDoctorVisitInd(respPolicyExtn.getPredDoctorVisitInd());
			policyExtn.setPredConcurrentAppInd(respPolicyExtn.getPredConcurrentAppInd());
			policyExtn.setPredAuditInd(respPolicyExtn.getPredAuditInd()); // APSL4502
			// APSL2209 begin
			List respPredictiveResult = respPolicyExtn.getPredictiveResult();
			if (respPredictiveResult != null && respPredictiveResult.size() > 0) {
				score = ((PredictiveResult) respPredictiveResult.get(0)).getScore();
			}
			// APSL2209 end

			// Code Deleted for QC APSL4582,APSL4583
			policyExtn.setActionUpdate();
		}
		List predictiveResultList = policyExtn.getPredictiveResult();
		boolean isIssueTypeSet = false; // APSL4600
		for (int i = 0; i < predictiveResultList.size(); i++) {
			PredictiveResult predResult = (PredictiveResult) predictiveResultList.get(i);
			if (predResult.getResponseTime().getTime() == null && predResult.getCreateTime().getTime() != null) {
				predResult.setPolicyStatus(responseTXLife.getPolicy().getIssueType() == 7 ? NbaOliConstants.OLI_POLSTAT_ELIGISSPEND
						: NbaOliConstants.OLI_POLSTAT_DECNOTELIG);
				predResult.setResponseTime(new NbaTime(new Date()));
				predResult.setScore(score);
				// Code Deleted for APSL4583,APSL4582
				// APSL4502 Begin
				if (respPolicyExtn.getPredAuditInd() && responseTXLife.getPolicy().getIssueType() == 7) { // APSL4586
					// APSL4600 Begin
					// Code moved from here to below APSL4600 block
					setRateClass(responseTXLife);
					generateEvaluateWI();
					nbaTxLife.getPolicy().setReinsuranceInd(false);
					nbaTxLife.getPolicy().setIssueType(NbaOliConstants.OLI_COVISSU_FULL);
					isIssueTypeSet = true;
					nbaTxLife.getPolicy().setActionUpdate();
					deleteTentativeDispostion();
					deleteReinsuranceType();
					if (policyExtn != null) {
						policyExtn.setPredictiveInd(false);
						policyExtn.setActionUpdate();
						getWork().getNbaLob().setPredictiveInd(false);
						List transactions = getWork().getNbaTransactions();
						int count = transactions.size();
						NbaTransaction nbaTransaction = null;
						for (int j = 0; j < count; j++) {
							nbaTransaction = (NbaTransaction) transactions.get(j);
							if (NbaConstants.A_WT_AGGREGATE_CONTRACT.equalsIgnoreCase(nbaTransaction.getWorkType())
									&& NbaConstants.A_QUEUE_AGGREGATE_CONTRACT.equalsIgnoreCase(nbaTransaction.getNbaLob().getAggrReference())) {
								nbaTransaction.getNbaLob().setPredictiveInd(false);
								nbaTransaction.setUpdate();
							}
						}
						if (predictiveResultList != null && predictiveResultList.size() > 0) {
							if (predResult.getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_ELIGISSPEND
									&& predResult.getUWDecision() == NbaOliConstants.OLI_TC_NULL) {
								predResult.setUWDecision(NbaOliConstants.OLIX_PAUWDECISON_DISAGREE);
								UnderwritingResult uwResult = new UnderwritingResult();
								NbaOLifEId oLifeid = new NbaOLifEId(responseTXLife);
								oLifeid.setId(uwResult);
								uwResult.setDescription(NbaOliConstants.NBA_AUDIT);
								uwResult.setActionAdd();
								predResult.addUnderwritingResult(uwResult);
								predResult.setActionUpdate();
							}
						}
					} // APSL4600 End
				}
				// APSL4502 End
				int msgCount = responseTXLife.getPrimaryHolding().getSystemMessageCount();
				for (int j = 0; j < msgCount; j++) {
					SystemMessage sysMsg = responseTXLife.getPrimaryHolding().getSystemMessageAt(j);
					Message aMessage = new Message();
					aMessage.setOriginator(sysMsg.getCarrierAdminSystem());
					aMessage.setReason(sysMsg.getMessageDescription());
					aMessage.setActionAdd();
					predResult.getMessage().add(aMessage); // APSL2210
				}
				predResult.setActionUpdate();
				policyExtn.setActionUpdate(); // APSL4502
				break;

			}
		}
		if (!isIssueTypeSet) { // APSL4600
			getNbaTxLife().getPolicy().setIssueType(responseTXLife.getPolicy().getIssueType());
			getNbaTxLife().getPolicy().setActionUpdate();
		} // APSL4600

		generateImpairments();

	}

	protected void suspendWork(int durationType, int durationValue) throws NbaBaseException {
		NbaSuspendVO suspendItem = null;
		suspendItem = getSuspendWorkVO(durationType, durationValue);
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
		if (suspendItem != null) {
			suspendWork(suspendItem);
		}
	}

	protected void linkPredictiveResultWI() throws NbaNetServerDataNotFoundException, NbaBaseException {
		getWork().getNbaCase().addNbaTransaction(new NbaTransaction(getOrigWorkItem().getWorkItem()));
		getWork().setUpdate();
	}

	protected void unlinkPredictiveResultWI() throws NbaNetServerDataNotFoundException, NbaBaseException {

		if (!NbaUtils.isBlankOrNull(getOrigWorkItem().getWorkItem().getParentWorkItemID())) {
			getOrigWorkItem().getNbaTransaction().setBreakRelation();
			getOrigWorkItem().getNbaTransaction().setUpdate();
		}
	}

	protected void setRateClass(NbaTXLife tx151) throws NbaBaseException {
		Coverage baseCoverage = NbaUtils.getBaseCoverage(tx151.getLife());
		long underwritingClass = (baseCoverage.getLifeParticipantAt(0)).getUnderwritingClass();
		String rateClass = null;
		int partyCount = nbaTxLife.getOLifE().getPartyCount();
		// Begin SR564247(APSL2161)
		for (int i = 0; i < nbaTxLife.getLife().getCoverageCount(); i++) {
			int lifeParticipantCount = nbaTxLife.getLife().getCoverageAt(i).getLifeParticipantCount();
			for (int j = 0; j < lifeParticipantCount; j++) {
				nbaTxLife.getLife().getCoverageAt(i).getLifeParticipantAt(j).setUnderwritingClass(underwritingClass);
			}
		}
		NbaVpmsAdaptor proxy = null;
		Map deOink = new HashMap();
		try {
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaTxLife);
			deOink.put(A_UNDERWRITINGCLASS, String.valueOf(underwritingClass));
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.CONTRACTVALIDATIONCALCULATIONS);
			proxy.setSkipAttributesMap(deOink);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_PredictiveRateClassData);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				rateClass = (String) vpmsResultsData.getResultsData().get(0);
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaProcPredictiveHold").logError(re);
				}
			}
		}
		// Begin SR564247(APSL2161)
		for (int k = 0; k < partyCount; k++) {
			Party party = nbaTxLife.getOLifE().getPartyAt(k);
			if (nbaTxLife.isInsured(party.getId())) {
				Person person = party.getPersonOrOrganization().getPerson();
				PersonExtension personExt = NbaUtils.getFirstPersonExtension(person);
				if (personExt != null) {
					personExt.setActionUpdate();
					personExt.setRateClass(rateClass);// SR564247(APSL2161)
					personExt.setProposedRateClass(rateClass);// SR564247(APSL2199 & APSL2247)

				}
			}
		}
		int coverageCount = nbaTxLife.getLife().getCoverageCount();
		for (int k = 0; k < coverageCount; k++) {
			Coverage coverage = nbaTxLife.getLife().getCoverageAt(k);
			CoverageExtension covExt = NbaUtils.getFirstCoverageExtension(coverage);
			if (covExt != null) {
				covExt.setRateClass(rateClass);// SR564247(APSL2161)
				covExt.setProposedRateClass(rateClass);// SR564247(APSL2199 & APSL2247)
				covExt.setActionUpdate();
			}
		}
		// APSL4582,APSL4583 Begin
		PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
		List predictiveResultList = policyExtn.getPredictiveResult();
		// Code deleted for APSL4587
		if (predictiveResultList.size() > 0) { // APSL4587
			PredictiveResult predResult = (PredictiveResult) predictiveResultList.get(predictiveResultList.size() - 1);
			if (predResult != null) {
				predResult.setRecommendedRateClass(rateClass);
				predResult.setActionUpdate();
			}
		} // APSL4587
		// Code Deleted for APSL4587
		// APSL4582,APSL4583 End
	}

	protected NbaDst retrieveWorkItem(NbaDst nbaDst) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("APPRDANL Starting retrieveWorkItem for " + nbaDst.getID());
		}
		NbaAwdRetrieveOptionsVO retrieveOptions = new NbaAwdRetrieveOptionsVO();
		retrieveOptions.setWorkItem(nbaDst.getID(), false);
		retrieveOptions.requestSources();
		return retrieveWorkItem(getUser(), retrieveOptions);
	}

	protected boolean handleWebServiceFailure(NbaTXLife nbaTXLifeResponse) throws NbaBaseException {
		TransResult transResult = nbaTXLifeResponse.getTransResult();
		long resultCode = transResult.getResultCode();
		if (!((NbaOliConstants.TC_RESCODE_SUCCESS == resultCode) || (NbaOliConstants.TC_RESCODE_PENDINGINFO == resultCode))) {
			//Deleted Code SR564247-Full(ALII1619)
			if (NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL == resultCode || NbaOliConstants.TC_RESINFO_SECVIOLATION == resultCode
					|| NbaOliConstants.TC_RESINFO_UNABLETOPROCESS == resultCode) {
				throw new NbaBaseException("Error accessing WebService " + this.getClass().getName() + " ", NbaExceptionType.FATAL);
			} else {//Begin SR564247-Full(ALII1619)
				if (getLogger().isDebugEnabled()) {
					getLogger().logError(" Data error occured during web service invoke of Predictive 1203 Request");
				}
				addComment("Webservice invokation failed - " + AxaWSConstants.WS_OP_PREDICTIVE_ORCHESTRATION_REQUEST);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Predictive Hold Processing Failed", getHostErrorStatus()));
			}
			//End SR564247-Full(ALII1619)
			return false;
		}
		return true;
	}

	/**
	 * Retrieves requirement work item from AWD.
	 * 
	 * @param policyNumber
	 * @param reqType
	 * @return the retrieved work item
	 * @throws NbaBaseException
	 */
	protected NbaDst getRequirementWI(String policyNumber, long reqType) throws NbaBaseException {
		NbaSearchResultVO resultVO = null;
		NbaLob lob = new NbaLob();
		lob.setPolicyNumber(policyNumber);
		lob.setReqType((int) reqType);
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

	// New Method CR1345857
	protected NbaDst retrieveParentWithTransactions(NbaDst dst, NbaUserVO user) throws NbaBaseException, NbaAWDLockedException { // ALS5177,ALII1652
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(dst.getID(), true);
		retOpt.requestTransactionAsChild();
		retOpt.setLockWorkItem();
		retOpt.setLockTransaction();
		retOpt.setNbaUserVO(user);
		AccelResult workResult = (AccelResult) ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), retOpt);
		NewBusinessAccelBP.processResult(workResult);
		return (NbaDst) workResult.getFirst();
	}

	/**
	 * @return Returns the awdTime.
	 */
	protected String getAwdTime() {
		return awdTime;
	}

	/**
	 * @param awdTime
	 *            The awdTime to set.
	 */
	protected void setAwdTime(String awdTime) {
		this.awdTime = awdTime;
	}

	// APSL2634 Starts Parse AWD Date into date. In case of fail return System Date
	protected Date parseTimeStamp(String dateForm) {
		try {
			java.util.Date date = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").parse(dateForm);
			return date;
		} catch (java.text.ParseException e) {
			return new Date();
		}
	}
	// APSL2634 Ends
	
	// APSL4600 New method
	public void generateEvaluateWI() {
		NbaEvaluateRequest req = new NbaEvaluateRequest();
		req.setNbaUserVO(getUser());
		req.setWork(getWork());
		req.setContract(getNbaTxLife());
		req.setUserFunction("NBPREDEVAL");
		req.setUnderwritingWB(false);
		req.setResetUWWB(true);
		AccelResult result = (AccelResult) currentBP.callBusinessService("GenerateEvaluateWorkItemBP", req);
	}
	
	// APSL4600 New method
	protected void deleteTentativeDispostion() {
		ApplicationInfo appInfo = nbaTxLife.getPolicy().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		int size = (appInfoExt != null) ? appInfoExt.getTentativeDisp().size() : 0;
		for (int i = 0; i < size; i++) {
			TentativeDisp tentDisp = appInfoExt.getTentativeDispAt(i);
			tentDisp.setActionDelete();
		}
	}
	
	// APSL4600 New method
	protected void deleteReinsuranceType() {
		Life life = nbaTxLife.getLife();
		if (life != null) {
			List coverages = life.getCoverage();
			for (int i = 0; i < coverages.size(); i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				List reinInfoList = coverage.getReinsuranceInfo();
				ReinsuranceInfo reinInfo = null;
				for (int k = 0; k < reinInfoList.size(); k++) {
					reinInfo = (ReinsuranceInfo) reinInfoList.get(k);
					// if (reinInfo.hasCarrierPartyID()) {
					reinInfo.setActionDelete();
					// }
				}
			}
		}
	}
	
}