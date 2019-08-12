package com.csc.fsg.nba.business.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.ui.AxaStatusDefinitionLoader;
import com.csc.fsg.nba.accel.process.AxaStatusDefinitionHelper;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.bean.accessors.NbaContractPrintFacadeBean;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaVpmsRequestVO;
import com.csc.fsg.nba.vo.statusDefinitions.Status;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.ActivityExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ContractChangeInfo;
import com.csc.fsg.nba.vo.txlife.ContractChangeOutcome;
import com.csc.fsg.nba.vo.txlife.EPolicyData;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/**
 * This poller processes the outcomes from contract change screens. The code has been written based on "Chain of Responsibility" design pattern. The
 * jobs/chains are always executed in the defined order.
 */

public class NbaProcPostApprovalContractChange extends NbaAutomatedProcess {

	@Override
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		if (!initialize(user, work)) {
			if (!work.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CONTRACT_CHANGE)) {
				return getResult();
			}
		}
		JobProcessor aProccessor = new JobProcessor();
		aProccessor.initiate();
		aProccessor.getInitialChain().execute(work);
		getNbaTxLife().getOLifE().setActionUpdate();
		setContractAccess(UPDATE); 		
		getNbaTxLife().setAccessIntent(getContractAccess());
		handleHostResponse(doContractUpdate(getNbaTxLife()));
		doUpdateWorkItem();
		if (null == getResult()) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		return getResult();
	}
	
	abstract class JobChainImpl implements Chainable {
		private Chainable nextChain;

		public void setNextChain(Chainable nextChain) {
			this.nextChain = nextChain;
		}

		public Chainable getNextChain() {
			return nextChain;
		}

		protected void invokeNextChain(NbaDst work) throws NbaBaseException {
			if (getNextChain() != null) {
				getNextChain().execute(work);
			}
		}
		
		protected NbaTXRequestVO createTxLifeRequestObject(NbaDst work) {
			NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
			nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
			nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
			nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
			nbaTXRequest.setNbaLob(work.getNbaLob());
			nbaTXRequest.setNbaUser(getUser());
			nbaTXRequest.setWorkitemId(work.getID());
			nbaTXRequest.setCaseInd(work.isCase());
			nbaTXRequest.setAccessIntent(UPDATE);
			nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser()));
			return nbaTXRequest;
		}
		
		// Creates Reissue related Activity
		protected Activity createActivity(String contractChangeInfoId, String activityKey) {
			Activity activity = new Activity();
			activity.setActivityTypeCode(NbaOliConstants.OLI_ACTTYPE_CONTRACTCHANGE);
			activity.setStartTime(new NbaTime(new Date()));
			activity.setEndTime(new NbaTime(new Date()));
			activity.setActivityKey(activityKey);
			ActivityExtension activityExtn = NbaUtils.createActivityExtension(activity);
			activityExtn.setRelatedObjectId(contractChangeInfoId);
			activityExtn.setActionAdd();
			activity.setActionAdd();
			return activity;
		}

		//APSL5370 Creates activity for AMICA
		protected Activity createAmicaActivity(NbaTXLife txLife) { //APSL5389
			Activity activity = new Activity();
			NbaOLifEId nbaOLifEId = new NbaOLifEId(txLife); //APSL5389
			nbaOLifEId.setId(activity); //APSL5389
			activity.setActivityTypeCode(NbaOliConstants.OLI_ACTTYPE_AMICACONTRACTCHANGE);
			activity.setStartTime(new NbaTime(new Date()));
			activity.setEndTime(new NbaTime(new Date()));
			ActivityExtension activityExtn = NbaUtils.createActivityExtension(activity);
			activityExtn.setActionAdd();
			activity.setActionAdd();
			return activity;
		}
		
		protected boolean isOutcomeProcessedForActiveContractChange(long outcome, NbaTXLife txLife) {
			ContractChangeInfo activeCCInfo = NbaUtils.getActiveContractChangeInfo(txLife);
			if (!NbaUtils.isBlankOrNull(activeCCInfo)) {
				List<ContractChangeOutcome> contractChangeOutcomeList = activeCCInfo.getContractChangeOutcome();
				for (ContractChangeOutcome contractChangeOutcome : contractChangeOutcomeList) {
					if (contractChangeOutcome.getOutcomeType() == outcome && contractChangeOutcome.getOutcomeProcessed() != true) {
						return true;
					}
				}
			}
			return false;
		}
		
		protected ContractChangeOutcome getOutcomeForActiveContractChange(long outcome, NbaTXLife txLife) {
			ContractChangeInfo activeCCInfo = NbaUtils.getActiveContractChangeInfo(txLife);
			if (!NbaUtils.isBlankOrNull(activeCCInfo)) {
				List<ContractChangeOutcome> contractChangeOutcomeList = activeCCInfo.getContractChangeOutcome();
				for (ContractChangeOutcome contractChangeOutcome : contractChangeOutcomeList) {
					if (contractChangeOutcome.getOutcomeType() == outcome) {
						return contractChangeOutcome;
					}
				}
			}
			return null;
		}
		
		protected AccelResult getWorkStatus(String processId, NbaDst work, NbaTXLife contract, Map deOink) throws NbaBaseException {
			NbaLob lobs = work.getNbaLob();
			deOink.put(NbaVpmsConstants.A_PROCESS_ID, processId);			
			NbaVpmsRequestVO vpmsRequestVO = new NbaVpmsRequestVO();
			vpmsRequestVO.setModelName(NbaVpmsConstants.AUTO_PROCESS_STATUS);
			vpmsRequestVO.setEntryPoint(NbaVpmsConstants.EP_WORKITEM_STATUSES);
			vpmsRequestVO.setNbaLob(lobs);
			vpmsRequestVO.setDeOinkMap(deOink);
			vpmsRequestVO.setNbATXLife(contract);			
			AccelResult result = (AccelResult) currentBP.callBusinessService("RetrieveDataFromBusinessRulesBP", vpmsRequestVO);//APSL1438
			if (result.hasErrors()) {
				throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS);
			}
			if (getResult() == null) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}
			return result;
		}
	}

	class WorkTypeJobChain extends JobChainImpl {
		public void execute(NbaDst work) throws NbaBaseException {
			if (work.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_APPLICATION)) {
				invokeNextChain(work);
			} else if (work.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CONTRACT_CHANGE)) {
				if (!NbaUtils.isBlankOrNull(work.getNbaLob())) {
					NbaSearchVO searchVO = searchWI(NbaConstants.A_WT_APPLICATION, work.getNbaLob().getPolicyNumber());
					if (searchVO != null && searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
						initializeStatusFields();
						Map deOink = new HashMap();
						List results = searchVO.getSearchResults();
						NbaDst applicationWI = retrieveWorkItem((NbaSearchResultVO) results.get(0), getUser());
						//Fetch CC source
						work = retrieveWorkItemAndSource(work, getUser());
						//attach CC source to NBAPPLCTN WI
						if (!work.getNbaSources().isEmpty()) {
							applicationWI.addNbaSource((NbaSource) work.getNbaSources().get(0));
							deOink.put("A_CntChgSourceMissing", "false");
						} else {
							deOink.put("A_CntChgSourceMissing", "true");							
						}
						//Fetch txlife APSL5407
						NbaTXLife txLife = NbaContractAccess.doContractInquiry(createTxLifeRequestObject(applicationWI));
						setNbaTxLife(txLife);
						// Archive current status					
						NbaLob lob = applicationWI.getNbaLob();
						//BEGIN: APSL5407
						if (txLife.isUnderwriterApproved() && lob.getUndwrtQueue().equalsIgnoreCase(lob.getQueue())) {
							lob.setArchivedStatus("PSMNAPRLPR");
						} else {
						lob.setArchivedStatus(lob.getStatus());		
						}
						//END:APSL5407
						//move CC to End
						Status passStatus = AxaStatusDefinitionLoader.determinePassStatus("NBCNTCHGWI", null);
						work.setStatus(passStatus.getStatusCode());
						work.getNbaLob().setRouteReason(passStatus.getRoutingReason());
						updateWork(getUser(), work);
						unlockWork(work);
						//Create activity for AMICA cases without contract change id association
						Activity activity = createAmicaActivity(txLife); //APSL5370,APSL5389
						getNbaTxLife().getOLifE().addActivity(activity);
						//move to RCM or PDCM applicationWI
						AccelResult	result = getWorkStatus("APPAPCCH", applicationWI, txLife, deOink);						
						applicationWI.setStatus(((NbaVpmsRequestVO) result.getFirst()).getPassStatus());
						applicationWI.getNbaLob().setRouteReason(((NbaVpmsRequestVO) result.getFirst()).getReason());
						setWork(applicationWI);											
					}
				}
			}
		}
	}

	class UnderwriterReviewJobChain extends JobChainImpl {
		public void execute(NbaDst work) throws NbaBaseException {
			boolean nextChainFlag = true;		
			NbaTXLife txLife = getNbaTxLife();
			ContractChangeInfo contractChangeInfo = NbaUtils.getActiveContractChangeInfo(txLife);
			if (isOutcomeProcessedForActiveContractChange(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_UWREVIEW, txLife)) {
				if(!NbaUtils.isDuplicateActivityPresent(txLife,NbaOliConstants.OLI_ACTTYPE_CONTRACTCHANGE,
					contractChangeInfo.getId(),String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_UWRV))) {
					Activity activity = createActivity(contractChangeInfo.getId(), String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_UWRV));
					txLife.getOLifE().addActivity(activity);
				}
				Status passStatus = AxaStatusDefinitionLoader.determinePassStatus("A2PAPCCH", null);
				String nextStatus = AxaStatusDefinitionHelper.determineStatus(passStatus, work, txLife);
				work.setStatus(nextStatus);
				work.getNbaLob().setRouteReason(passStatus.getRoutingReason());
				nextChainFlag = false;
			}
			if (nextChainFlag) {
				invokeNextChain(work);
			}
		}
	}
	
	//APSL5382: New Inner Class
	class ReturnToInitiatorJobChain extends JobChainImpl {
		/*
		 * If Return to Initiator outcome is selected then case should aggregate/move
		 * back to initiator queue
		 * */
		public void execute(NbaDst work) throws NbaBaseException {
			boolean nextChainFlag = true;
			NbaTXLife txLife = getNbaTxLife();
			ContractChangeInfo contractChangeInfo = NbaUtils.getActiveContractChangeInfo(txLife);
			if (!NbaUtils.isBlankOrNull(contractChangeInfo) && !NbaUtils.isBlankOrNull(contractChangeInfo.getContractChangeOutcome())) {// NBLXA-1679
				for (Object obj : contractChangeInfo.getContractChangeOutcome()) {
					ContractChangeOutcome outcome = (ContractChangeOutcome) obj;
					if (NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_RETURNTOINIT == outcome.getOutcomeType()) {
						// select initiation activity and fetch userQueue
						List<Activity> activityList = new ArrayList<Activity>();
						activityList = NbaUtils.getActivityByTypeCodeAndRelatedObjId(txLife.getOLifE().getActivity(),
								NbaOliConstants.OLI_ACTTYPE_CONTRACTCHANGE, contractChangeInfo.getId());
						String nextQueue = "";
						boolean processOutcome = false;
						for (Activity activity : activityList) {
							if (NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_BACKTOINIT == Long.parseLong(activity.getActivityKey())
									&& outcome.getOutcomeProcessed() == false) {
								processOutcome = true;
								outcome.setOutcomeProcessed(true);
								outcome.setActionUpdate();
								if (!NbaUtils.isDuplicateActivityPresent(txLife, NbaOliConstants.OLI_ACTTYPE_CONTRACTCHANGE,
										contractChangeInfo.getId(), String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_INITRVCMLTD))) {
									Activity activityCompleted = createActivity(contractChangeInfo.getId(),
											String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_INITRVCMLTD));
									txLife.getOLifE().addActivity(activityCompleted);
								}
								break;
							}
							if (NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_INITIATED == Long.parseLong(activity.getActivityKey())) {
								ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(activity);
								if (!NbaUtils.isBlankOrNull(activityExtn)) {
									nextQueue = activityExtn.getUserQueue();
								}
							}
						}
						if (!processOutcome) {
							if (!NbaUtils.isDuplicateActivityPresent(txLife, NbaOliConstants.OLI_ACTTYPE_CONTRACTCHANGE, contractChangeInfo.getId(),
									String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_BACKTOINIT))) {
								Activity activity = createActivity(contractChangeInfo.getId(),
										String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_BACKTOINIT));
								txLife.getOLifE().addActivity(activity);
							}
							Map deOink = new HashMap();
							// deOink.put("A_ReturnToInitiator", "true");
							deOink.put("A_AggregationQueue", nextQueue);
							AccelResult result = getWorkStatus("APRTINIT", work, txLife, deOink);
							work.setStatus(((NbaVpmsRequestVO) result.getFirst()).getPassStatus());
							work.getNbaLob().setRouteReason(((NbaVpmsRequestVO) result.getFirst()).getReason());
							nextChainFlag = false;
						}
					}
				}
			}
			if (nextChainFlag) {
				invokeNextChain(work);
			}
		}
	}
	
	class RequirementsDeterminationJobChain extends JobChainImpl {
		public void execute(NbaDst work) {
			/*
			 * if (conforming illustration outcome is present and not completed) { obj = new NbaProcRequirementsDetermination(); obj.executeProcess();
			 * mark outcome complete; } invokeNextChain(work);
			 */
			//txLife.getOLifE().addActivity(createActivity(NbaUtils.getActiveContractChangeInfo(txLife).getId(),String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_CONFILSTRATION)));
		}
	}

	class PrintJobChain extends JobChainImpl {
		public void execute(NbaDst work) throws NbaBaseException {
			boolean isPrintWorkItemRouteToEnd = false;
			boolean outcomeProcessed = false;
			NbaTXLife txLife = getNbaTxLife();
			// Begin APSL5362
			int disp = 0;
			disp = work.getNbaLob().getCaseFinalDispstn();
			// End APSL5362
			ContractChangeInfo activeCCInfo = NbaUtils.getActiveContractChangeInfo(txLife);
			if (activeCCInfo != null && getPrintStatus(txLife.getPolicy().getPolNumber(), txLife, isPrintWorkItemRouteToEnd) != null) {//APSL5407
				if (!getPrintStatus(txLife.getPolicy().getPolNumber(), txLife, isPrintWorkItemRouteToEnd).equalsIgnoreCase(
						NbaConstants.STATE_OF_PRINT_NOT_GENERATED)
						&& disp == 0) { // APSL5362
					if (NbaUtils.isUnapproveActivityforActiveContractChange(txLife)) {
						isPrintWorkItemRouteToEnd = true;
						getPrintStatus(txLife.getPolicy().getPolNumber(), txLife, isPrintWorkItemRouteToEnd); // This method also Route existing print
						// in end queue if print is in progress
					} else {
						if (isOutcomeProcessedForActiveContractChange(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_PAGE3, txLife)) {
							txLife.getOLifE().addActivity(
									createActivity(activeCCInfo.getId(), String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_PRINT)));
							generatePrintWorkItem(String.valueOf(NbaOliConstants.OLI_ATTACH_PAGE3), work);
						} else {
							if (isOutcomeProcessedForActiveContractChange(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_WHOLEPOLICY, txLife)) {
								txLife.getOLifE().addActivity(
										createActivity(activeCCInfo.getId(), String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_PRINT)));
								isPrintWorkItemRouteToEnd = true;
								getPrintStatus(txLife.getPolicy().getPolNumber(), txLife, isPrintWorkItemRouteToEnd); // This method also Route existing print workItem in end queue if print is in progress
								generatePrintWorkItem(String.valueOf(NbaOliConstants.OLI_ATTACH_WHOLEPOLICY), work);
							}
						}
					}
					outcomeProcessed = true;
				}
			}//APSL5407
			if (outcomeProcessed
					&& !NbaUtils.isBlankOrNull(getOutcomeForActiveContractChange(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_PAGE3, txLife))) {
				ContractChangeOutcome contractChangeOutcome = getOutcomeForActiveContractChange(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_PAGE3,
						txLife);
				contractChangeOutcome.setOutcomeProcessed(true);
				contractChangeOutcome.setActionUpdate();
			}
			if (outcomeProcessed
					&& !NbaUtils.isBlankOrNull(getOutcomeForActiveContractChange(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_PRINTRQ, txLife))) {
				ContractChangeOutcome contractChangeOutcome = getOutcomeForActiveContractChange(
						NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_PRINTRQ, txLife);
				contractChangeOutcome.setOutcomeProcessed(true);
				contractChangeOutcome.setActionUpdate();
			}
			if (outcomeProcessed
					&& !NbaUtils.isBlankOrNull(getOutcomeForActiveContractChange(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_WHOLEPOLICY, txLife))) {
				ContractChangeOutcome contractChangeOutcome = getOutcomeForActiveContractChange(
						NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_WHOLEPOLICY, txLife);
				contractChangeOutcome.setOutcomeProcessed(true);
				contractChangeOutcome.setActionUpdate();
			}
			invokeNextChain(work);
		}

		protected EPolicyData retrieveActiveEPolicyData(NbaTXLife holdingInq, String workItemId) {
			PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(holdingInq.getPolicy());
			if (policyExtension != null && policyExtension.getEPolicyDataCount() > 0) {
				ArrayList<EPolicyData> ePolicyDataList = policyExtension.getEPolicyData();
				for (EPolicyData ePolData : ePolicyDataList) {
					if (ePolData.getActive() && workItemId.equalsIgnoreCase(ePolData.getPrintCRDA())) {
						return ePolData;
					}
				}
			}
			return null;
		}

		protected void generatePrintWorkItem(String extComp, NbaDst work) {
			NbaContractPrintFacadeBean facade = new NbaContractPrintFacadeBean();
			try {
				facade.generateContractExtract(work.getNbaUserVO(), work, extComp, false, null);
			} catch (NbaBaseException e) {
				e.printStackTrace();
			}
		}

		public String getPrintStatus(String polNumber, NbaTXLife holdingInq, boolean isPrintWorkItemRouteToEnd) throws NbaBaseException {// APSL5100
			String printStatus = null;
			boolean allPrintInEndInd = true;
			NbaSearchResultVO searchResultVO = null;
			EPolicyData ePolicyData = null;
			NbaSearchVO searchPrintVO = searchWI(NbaConstants.A_WT_CONT_PRINT_EXTRACT, polNumber);
			if (!NbaUtils.isBlankOrNull(searchPrintVO) && searchPrintVO.getSearchResults() != null && !searchPrintVO.getSearchResults().isEmpty()) {
				List searchResultList = searchPrintVO.getSearchResults();
				for (int i = 0; i < searchResultList.size(); i++) {
					searchResultVO = (NbaSearchResultVO) searchResultList.get(i);
					if (!searchResultVO.getQueue().equals(NbaConstants.END_QUEUE)) {
						allPrintInEndInd = false;
						break;
					}
				}
				ePolicyData = retrieveActiveEPolicyData(holdingInq, searchResultVO.getWorkItemID());
				if (searchResultVO != null && allPrintInEndInd) {
					if (ePolicyData != null && NbaUtils.isBlankOrNull(ePolicyData.getPrintStatus())) {
						printStatus = NbaConstants.STATE_OF_PRINT_IN_PROGRESS;
					} else {
						printStatus = NbaConstants.STATE_OF_PRINT_COMPLETED;
					}
				}
				if (searchResultVO != null && !allPrintInEndInd) {
					if (ePolicyData != null) {
						printStatus = NbaConstants.STATE_OF_PRINT_IN_PROGRESS;
					} else {
						printStatus = NbaConstants.STATE_OF_PRINT_NOT_GENERATED;
					}
				}
			}
			if (isPrintWorkItemRouteToEnd && printStatus.equalsIgnoreCase(NbaConstants.STATE_OF_PRINT_IN_PROGRESS)) {
				NbaDst printWork = retrieveWorkItem(searchResultVO, getUser());
				// changeStatus("PRNTCANCLD");
				printWork.setStatus("PRNTCANCLD");
				printWork.getNbaLob().setRouteReason(NbaUtils.getRouteReason(printWork, printWork.getStatus()));
				printWork.setUpdate();
				updateWork(getUser(), printWork);
				unlockWork(printWork);
				unsuspendPrintWorkItem(printWork);
			}
			return printStatus;
		}
	}

	class InforceUpdateJobChain extends JobChainImpl {
		public void execute(NbaDst work) throws NbaBaseException {
			boolean outcomeProcessed = false;
			NbaTXLife txLife = getNbaTxLife();
			// Begin APSL5362
			int disp = 0;
			disp = work.getNbaLob().getCaseFinalDispstn();
			// End APSL5362
			ContractChangeInfo activeCCInfo = NbaUtils.getActiveContractChangeInfo(txLife);
			if (isOutcomeProcessedForActiveContractChange(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_INFORCE, txLife) && disp == 0) {    //APSL5362
				txLife.getOLifE().addActivity(
						createActivity(activeCCInfo.getId(),
								String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_INFUPDATE)));
				createInforceUpdateWorkItem(work);
				//APSL5370 Start
				PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(txLife.getPolicy());
				if (policyExt != null) {
					policyExt.setAdminSysPolicyStatus(NbaOliConstants.OLI_POLSTAT_0); 
					policyExt.setActionUpdate();
				}
				//APSL5370 End
				outcomeProcessed = true;
			}
			if (outcomeProcessed
					&& !NbaUtils.isBlankOrNull(getOutcomeForActiveContractChange(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_INFORCE, txLife))) {
				ContractChangeOutcome contractChangeOutcome = getOutcomeForActiveContractChange(
						NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_INFORCE, txLife);
				contractChangeOutcome.setOutcomeProcessed(true);
				contractChangeOutcome.setActionUpdate();
			}
			invokeNextChain(work);
		}

		public void createInforceUpdateWorkItem(NbaDst work) {
			NbaUserVO tempUserVO = new NbaUserVO("NBINFORCWRK", "");
			try {
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(tempUserVO, work);
				NbaTransaction nbaTransaction = work.addTransaction(provider.getWorkType(), provider.getInitialStatus());
				nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
				NbaLob workNbaLob = work.getNbaLob();
				NbaLob tempTransNbaLob = nbaTransaction.getNbaLob();
				tempTransNbaLob.setPolicyNumber(workNbaLob.getPolicyNumber());
				tempTransNbaLob.setSsnTin(workNbaLob.getSsnTin());
				tempTransNbaLob.setTaxIdType(workNbaLob.getTaxIdType());
				tempTransNbaLob.setLastName(workNbaLob.getLastName());
				tempTransNbaLob.setFirstName(workNbaLob.getFirstName());
				tempTransNbaLob.setMiddleInitial(workNbaLob.getMiddleInitial());
				tempTransNbaLob.setCompany(workNbaLob.getCompany());
				tempTransNbaLob.setAppDate(workNbaLob.getAppDate());
				tempTransNbaLob.setAppState(workNbaLob.getAppState());
				tempTransNbaLob.setFaceAmount(workNbaLob.getFaceAmount());
				tempTransNbaLob.setAgentID(workNbaLob.getAgentID());
				tempTransNbaLob.setDistChannel(String.valueOf(workNbaLob.getDistChannel()));
				work.setUpdate();
				WorkflowServiceHelper.update(getUser(), work);
			} catch (NbaBaseException e) {
				e.printStackTrace();
			}
		}
	}
	
	class PostApprovalJobChain extends JobChainImpl {
		/*
		 * If unapproval activity for active contract changes is present then route the case to Requirement Determination else route the case back to
		 * archived status-queue. For partial rejections route the case to ReqDet else for full rejection route to archived status-queue.
		 */
		public void execute(NbaDst work) throws NbaBaseException {
			NbaTXLife txLife = getNbaTxLife();
			//Begin APSL5362
			int disp = 0;
			disp = work.getNbaLob().getCaseFinalDispstn();
			ContractChangeInfo activeCCInfo = NbaUtils.getActiveContractChangeInfo(txLife);
			//If no Contract Change is initiated(Contract Change WI work complete scenario)
			if (NbaUtils.isBlankOrNull(activeCCInfo)) {
				//If orphan AMICA activity exists then delete the activity
				//BEGIN: APSL5370
				List<Activity> amicaActivityList = new ArrayList<Activity>();
				amicaActivityList = NbaUtils.getActivityByTypeCode(txLife.getOLifE().getActivity(), NbaOliConstants.OLI_ACTTYPE_AMICACONTRACTCHANGE);
				for(Activity amicaActivity : amicaActivityList) {
					ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(amicaActivity);
					if (!NbaUtils.isBlankOrNull(activityExtn) &&
							NbaUtils.isBlankOrNull(activityExtn.getRelatedObjectId())) {
						activityExtn.setActionDelete();
						amicaActivity.setActionDelete();
					}
				}
				//END: APSL5370
				work.setStatus(work.getNbaLob().getArchivedStatus());
				work.getNbaLob().setRouteReason(NbaUtils.getRouteReason(work, work.getStatus()));
			}
			if (NbaUtils.isUnapproveActivityforActiveContractChange(txLife)) {
				//ContractChangeInfo activeCCInfo = NbaUtils.getActiveContractChangeInfo(txLife);
				if (!NbaUtils.isBlankOrNull(activeCCInfo) && disp != 0 ){
					Status passStatus = AxaStatusDefinitionLoader.determinePassStatus("N2FNLDSP", null);
					work.setStatus(passStatus.getStatusCode());
					work.getNbaLob().setRouteReason(NbaUtils.getRouteReason(work, work.getStatus()));
					// create CC completed activity
					activeCCInfo.setContractChangeStatus(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGSTATUS_COMPLETED);
					activeCCInfo.setActionUpdate();
					txLife.getOLifE().addActivity(
							createActivity(activeCCInfo.getId(), String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_COMPLETED)));
				} // End APSL5362
				else if (!NbaUtils.isBlankOrNull(activeCCInfo) && activeCCInfo.getContractChangeOutcomeCount() == 0) {
					if (NbaUtils.isContractChangeRejected(activeCCInfo)) {
						work.setStatus(work.getNbaLob().getArchivedStatus());
						work.getNbaLob().setRouteReason(NbaUtils.getRouteReason(work, work.getStatus()));
						// create CC cancelled activity
						activeCCInfo.setContractChangeStatus(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGSTATUS_CANCELLED);
						activeCCInfo.setActionUpdate();
						txLife.getOLifE().addActivity(
								createActivity(activeCCInfo.getId(), String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_CANCELLED)));
					} else {
						work.setStatus(work.getNbaLob().getArchivedStatus());
						work.getNbaLob().setRouteReason(NbaUtils.getRouteReason(work, work.getStatus()));
						// create CC completed activity
						activeCCInfo.setContractChangeStatus(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGSTATUS_COMPLETED);
						activeCCInfo.setActionUpdate();
						txLife.getOLifE().addActivity(
								createActivity(activeCCInfo.getId(), String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_COMPLETED)));
					}
				} else {
					Status passStatus = AxaStatusDefinitionLoader.determinePassStatus("N2PSMNAP", null);
					work.setStatus(passStatus.getStatusCode());
					work.getNbaLob().setRouteReason(passStatus.getRoutingReason());
					// create CC completed activity
					activeCCInfo.setContractChangeStatus(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGSTATUS_COMPLETED);
					activeCCInfo.setActionUpdate();
					txLife.getOLifE().addActivity(
							createActivity(activeCCInfo.getId(), String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_COMPLETED)));
				}
			} else {
				//ContractChangeInfo activeCCInfo = NbaUtils.getActiveContractChangeInfo(txLife);
				if (activeCCInfo != null) {
					work.setStatus(work.getNbaLob().getArchivedStatus());
					work.getNbaLob().setRouteReason(NbaUtils.getRouteReason(work, work.getStatus()));
					if (NbaUtils.isContractChangeRejected(activeCCInfo)) {
						// create CC cancelled activity
						activeCCInfo.setContractChangeStatus(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGSTATUS_CANCELLED);
						activeCCInfo.setActionUpdate();
						txLife.getOLifE().addActivity(
								createActivity(activeCCInfo.getId(), String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_CANCELLED)));
					} else {
						//Begin APSL5370
						if(!NbaUtils.isBlankOrNull(getOutcomeForActiveContractChange(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_INFORCE, txLife))){
							Status passStatus = AxaStatusDefinitionLoader.determinePassStatus("N2APPHLD", null);
							work.setStatus(passStatus.getStatusCode());
							work.getNbaLob().setRouteReason(passStatus.getRoutingReason());
							//BEGIN: APSL5401
							ApplicationInfo appInfo = txLife.getPolicy().getApplicationInfo();
							ApplicationInfoExtension appInfoExtn = NbaUtils.getAppInfoExtension(appInfo);
							if (!NbaUtils.isBlankOrNull(appInfoExtn)) {
								if (appInfoExtn.getIssuedToAdminSysInd()) {
									appInfoExtn.setUnderwritingStatus(NbaOliConstants.NBA_FINALDISPOSITION_ISSUED);
									work.getNbaLob().setCaseFinalDispstn((int) NbaOliConstants.NBA_FINALDISPOSITION_ISSUED);
								} 
							}
							//END: APSL5401
						}
						//End APSL5370
						// create CC completed activity
						activeCCInfo.setContractChangeStatus(NbaOliConstants.OLIEXT_LU_CONTRACTCHNGSTATUS_COMPLETED);
						activeCCInfo.setActionUpdate();
						txLife.getOLifE().addActivity(
								createActivity(activeCCInfo.getId(), String.valueOf(NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_COMPLETED)));
					}
				}
			}
			invokeNextChain(work);
		}
	}

	class JobProcessor {
		private Chainable initialChain = null;

		void initiate() throws NbaBaseException {
			Chainable previousChain = null;
			for (Jobs aJob : Jobs.values()) {
				Chainable aChain = getChainObj(aJob);
				if (initialChain == null) {
					initialChain = aChain;
				}
				if (previousChain != null) {
					previousChain.setNextChain(aChain);
				}
				previousChain = aChain;
			}
		}

		Chainable getInitialChain() {
			return initialChain;
		}

		Chainable getChainObj(Jobs aJob) throws NbaBaseException {
			switch (aJob) {
			case WORKTYPE:
				return new WorkTypeJobChain();
			case UNDR:
				return new UnderwriterReviewJobChain();
			/*
			 * case REQDET: return new RequirementsDeterminationJobChain();
			 */
			case PRINT:
				return new PrintJobChain();
			case INFORCE_UPDATE:
				return new InforceUpdateJobChain();
			case RETURNTOINITIATOR: //APSL5382
				return new ReturnToInitiatorJobChain();					
			case POSTAPPROVAL:
				return new PostApprovalJobChain();
			default:
				throw new NbaBaseException("The given job " + aJob + " has not been defined.");
			}
		}
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
	
	public NbaDst retrieveWorkItemAndSource(NbaDst work, NbaUserVO user) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setNbaUserVO(user);
		retOpt.requestSources();
		retOpt.setWorkItem(work.getID(), true);
		retOpt.setLockWorkItem();
		aWorkItem = WorkflowServiceHelper.retrieveWork(user, retOpt);
		return aWorkItem;
	}

	protected NbaSearchVO searchWI(String workType, String policyNumber) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName("NbaSearchResultVO");
		searchVO.setWorkType(workType);
		searchVO.setContractNumber(policyNumber);
		searchVO = lookupWork(getUser(), searchVO);
		return searchVO;
	}
	protected void unsuspendPrintWorkItem(NbaDst work) throws NbaBaseException {
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		if (work != null && work.isSuspended()) {
			suspendVO.setTransactionID(work.getID());
			unsuspendWork(suspendVO);
		}
	}
}

enum Jobs {
	WORKTYPE, UNDR, RETURNTOINITIATOR, INFORCE_UPDATE, PRINT, POSTAPPROVAL; //APSL5382
}

interface Chainable {
	Chainable getNextChain();

	void setNextChain(Chainable nextChain);

	void execute(NbaDst work) throws NbaBaseException;
}