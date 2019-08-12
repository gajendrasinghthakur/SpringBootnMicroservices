package com.csc.fsg.nba.business.process;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.dataobject.nba.identification.RequirementsReceivedUpdateRequest;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTable;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vpms.CopyLobsTaskConstants;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.results.ResultData;

/**
 * Drives the aggregate contract process.  Controls the aggregation of work items being sent to the  
 * case manager queue for processing. 
 * Works on all the work items sent to "Aggregate Contract" queue and will perform aggregation of  
 * the work items into NBAGGCNT work item and routes this aggregated NBAGGCNT work item to case manager   
 * queue or to "End" queue depending upon the business logic.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA251</td><td>Version 8</td><td>nbA Case Manager and Companion Case Assignment</td></tr>
 * <tr><td>SR494086.5</td><td>Discretionary</td><td>ADC Workflow</td></tr>
 * <tr><td> ALII264</td><td>AXA life phase 2</td><td>Modified copy lob using VPMS</td></tr>
 * <tr><td>NBA300</td><td>AXA Life Phase 2</td><td>Term Conversion</td></tr>
 * <tr><td>CR57950 and 57951</td><td>Version 8</td><td>Aggregate Contract - Pre-Sale/Reg60</td></tr>
 * <tr><td>CR59174</td><td>XA Life Phase 2</td><td>1035 Exchange Case Manager</td></tr>
 * <tr><td>ALNA212</td><td>AXA Life Phase 2</td><td>Performance Improvement</td></tr> 
 * <tr><td>CR61627</td><td>AXA Life Phase 2</td><td>Assign Replacement Case Manager</td></tr>
 * <tr><td>CR1345857(APSL2575)</td><td>Discretionary</td>Predictive CR - Aggregate</tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class NbaProcAggregateContract extends NbaAutomatedProcess {

	protected NbaDst parentWork = null;   //CR61627
	protected String replacementCM = null; //CR61627
	protected String exchange1035CM = null; //CR61627
	protected String rcmTeam = null; //APSL4412
	
	// Begin APSL677,NBA300,ALII923 Refactoring
	protected static int MIN = 30; //CR59174
	protected static int DAY = 15; //CR59174  
	static {
		try {
			MIN = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
					NbaConfigurationConstants.AGGREGATE_CONTRACT_SUSPEND_MINUTES));
			DAY = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
					NbaConfigurationConstants.AGGREGATE_CONTRACT_SUSPEND_DAYS));
		} catch (Exception ex) {
			MIN = 30;
			DAY = 15;
		}
	}
	// End APSL677,NBA300,ALII923 Refactoring	
	/**
	 * NbaProcAggregateContract constructor comment.
	 */
	public NbaProcAggregateContract() {
		super();
	}

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// Initialization
		if (!initialize(user, work)) {
			return getResult();
		}
		//APSl4412 start
		if (retrieveTransactionsRequired()) {  //ALII1290, QC8443.
			setParentWork(retrieveParentWithTransactions()); //CR61627-PERF, 
		}
		rcmTeam = NbaUtils.getRCMTeam(NbaUtils.getAsuCodeForRetail(getNbaTxLife()), NbaUtils.getEPGInd(getNbaTxLife())); //APSL4412 
		String producerid = "";
		Relation bgaRelation = getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
		Relation relation = getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
		NbaParty party = null;
		if(relation != null){
			party = getNbaTxLife().getParty(relation.getRelatedObjectID());
		}else if (bgaRelation != null){
			party = getNbaTxLife().getParty(bgaRelation.getRelatedObjectID());
		}
		if (party != null) {
			CarrierAppointment carrierAppointment = party.getParty().getProducer().getCarrierAppointmentAt(0);
			if (carrierAppointment != null) {
				producerid = carrierAppointment.getCompanyProducerID();
			}
		}
		if( (rcmTeam == null || rcmTeam.length() < 2)  
				|| NbaUtils.isEarcAgent(producerid)
				|| NbaUtils.isAdcApplication(getParentWork())
				|| NbaUtils.isWholeSale(getNbaTxLife().getPolicy())
				|| !(work.getQueue().trim().equalsIgnoreCase(A_QUEUE_AGGREGATE_CONTRACT))
				|| (getParentWork().getNbaLob().getAppOriginType()!=NbaOliConstants.OLI_APPORIGIN_TRIAL && getParentWork().getNbaLob().getAppOriginType()!=NbaOliConstants.OLI_APPORIGIN_FORMAL)) {
			return aggregateToUWCM();
		} 
		return routeToRCMTeamQ();
		//APSl4412 end
	}
	
	/**
	 * 
	 * @return
	 * @throws NbaBaseException
	 */
	//APSL4412 New method
	public NbaAutomatedProcessResult aggregateToUWCM() throws NbaBaseException {
		try {
			resetCaseManagers(); //CR61627
			if(toUpdateTODOLOB()){
				updateTODOListCount();//APSL4342
			}
			boolean hasErrors = false;
			boolean newWorkCreated= false;
			NbaSuspendVO suspendItem = null;			
			//Begin APSL677,NBA300,ALII923 Refactoring
			if(NbaUtils.isBlankOrNull(work.getNbaLob().getPolicyNumber())){
				suspendItem  = getSuspendWorkVO(Calendar.MINUTE, MIN);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));				
			} else if (getParentWork().getNbaLob().getAppProdType()!=null && !getParentWork().getNbaLob().getAppProdType().equalsIgnoreCase(NbaConstants.APPPROD_TYPE_ADC) && NbaUtils.isBlankOrNull(getParentWork().getNbaLob().getCaseManagerQueue())) { //QC7867
				if (NbaUtils.isTermConvOPAICase(getNbaTxLife())) {//CR57950 and CR57951
					suspendItem = getSuspendWorkVO(Calendar.DATE, DAY);
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Workitem suspended - Case Manager LOB is Null.",""));
				} else if (!NbaUtils.isReg60PreSale(getParentWork().getNbaLob().getAppOriginType())) {
					hasErrors = true;
					addComment("Workitem sent to error queue since Case Manager LOB is null");
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getHostErrorStatus()));
					changeStatus(getResult().getStatus());
				}
			} else if (NbaUtils.isTermConvOPAICase(getNbaTxLife()) && NbaUtils.isBlankOrNull(getParentWork().getNbaLob().getCaseManagerQueue())) {//APSL4412
				addComment("Workitem suspended - Case Manager LOB on Parent Case is not set.");
				suspendItem = getSuspendWorkVO(Calendar.MINUTE, MIN);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Workitem suspended - Case Manager LOB is Null.",""));
			} 
			//Begin APSL677,ALII923, NBA300
			if(getResult() == null){//APSL677
				if (NbaConstants.A_WT_AGGREGATE_CONTRACT.equalsIgnoreCase(work.getWorkType())) {
					if (getResult() == null) {
						if (hasOtherWorkItemsInAggregateContractQueue()) {
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getOtherStatus()));
						} else {
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
						}
					} else { // host error encounter
						hasErrors = true;
					}
					changeStatus(getResult().getStatus());
				} else {
					if (!isAnyOpenAggregateContract(getAggReference())) {// CR57950 and CR57951
						NbaTransaction aggContract = createNewAggregateContract(); //NBLXA-1554[NBLXA-2055]
						newWorkCreated = true;
						reinitializeFields(aggContract);//NBLXA-1554[NBLXA-2055]
					}
					suspendItem = getSuspendWorkVO(Calendar.DATE, DAY);
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
				}
			}
			commit(hasErrors, suspendItem, newWorkCreated);
			unlockWorkItems();
		} finally {
			// unlock any work items not already unlocked excluding the current work item
			// this should only happen in an exception being thrown situation
			if (getParentWork() != null && getParentWork().isCase() && getParentWork().isLocked(user.getUserID())) {
				WorkItem childItem = null;
				Iterator iter = getParentWork().getTransactions().iterator();
				while (iter.hasNext()) {
					childItem = (WorkItem) iter.next();
					if (childItem.getItemID().equals(work.getID())) {
						iter.remove();
						break;
					}
				}
				unlockWorkItems();
			}
		}
		return getResult();
	}
	
	
	/**
	 * 
	 * @return
	 * @throws NbaBaseException
	 */
	//APSL4412 New method
	public NbaAutomatedProcessResult routeToRCMTeamQ() throws NbaBaseException {
		try {
			boolean hasErrors = false;
			boolean newWorkCreated= false;
			NbaSuspendVO suspendItem = null;			
			//Begin APSL677,NBA300,ALII923 Refactoring
			if(NbaUtils.isBlankOrNull(work.getNbaLob().getPolicyNumber())){
				suspendItem  = getSuspendWorkVO(Calendar.MINUTE, MIN);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));				
			} else if (getParentWork().getNbaLob().getAppProdType()!=null && !getParentWork().getNbaLob().getAppProdType().equalsIgnoreCase(NbaConstants.APPPROD_TYPE_ADC) && NbaUtils.isBlankOrNull(getParentWork().getNbaLob().getCaseManagerQueue())) { //QC7867
				if (NbaUtils.isTermConvOPAICase(getNbaTxLife())) {//CR57950 and CR57951
					suspendItem = getSuspendWorkVO(Calendar.DATE, DAY);
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Workitem suspended - Case Manager LOB is Null.",""));
				} else if (!NbaUtils.isReg60PreSale(getParentWork().getNbaLob().getAppOriginType())) {
					hasErrors = true;
					addComment("Workitem sent to error queue since Case Manager LOB is null");
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getHostErrorStatus()));
					changeStatus(getResult().getStatus());
				}
			} else if (NbaUtils.isTermConvOPAICase(getNbaTxLife()) && NbaUtils.isBlankOrNull(getParentWork().getNbaLob().getCaseManagerQueue())) {//APSL4412
				addComment("Workitem suspended - Case Manager LOB on Parent Case is not set.");
				suspendItem = getSuspendWorkVO(Calendar.MINUTE, MIN);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Workitem suspended - Case Manager LOB is Null.",""));
			} 
			//Begin APSL677,ALII923, NBA300
			if(getResult() == null){//APSL677
				if (NbaConstants.A_WT_AGGREGATE_CONTRACT.equalsIgnoreCase(work.getWorkType())) {
					if (getResult() == null) {
						if (hasOtherWorkItemsInAggregateContractQueue()) {
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getOtherStatus()));
						} else {
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
						}
					} else { // host error encounter
						hasErrors = true;
					}
					changeStatus(getResult().getStatus());
				} else {
					boolean undReqd = true;
					if(NbaUtils.isTermConvOPAICase(getNbaTxLife())){
						HashMap deOink = new HashMap();
						deOinkTermConvData(deOink);
						String undReqdFromVPMS = (String) deOink.get("A_UndRequired");
						if(!"true".equalsIgnoreCase(undReqdFromVPMS)){
							undReqd = false;
						}
					}
					if( undReqd	&& (rcmTeam != null && rcmTeam.length() > 2) 
							&& (NbaConstants.A_WT_PAYMENT_NOTIFICATION.equalsIgnoreCase(work.getWorkType())
							|| NbaConstants.A_WT_CWA.equalsIgnoreCase(work.getWorkType()))){
						changeStatus(getWork(), getOtherStatus());
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getOtherStatus()));
					}
					else {
						if (!isAnyOpenAggregateContract(getAggReference())) {// CR57950 and CR57951
							NbaTransaction aggContract = createNewAggregateContract(); //NBLXA-1554[NBLXA-2055]
							newWorkCreated = true;
							reinitializeFields(aggContract); //NBLXA-1554[NBLXA-2055]
						}
						suspendItem = getSuspendWorkVO(Calendar.DATE, DAY);
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
					}
				}
			}
			commit(hasErrors, suspendItem, newWorkCreated);
			unlockWorkItems();
		} finally {
			// unlock any work items not already unlocked excluding the current work item
			// this should only happen in an exception being thrown situation
			if (getParentWork() != null && getParentWork().isCase() && getParentWork().isLocked(user.getUserID())) {
				WorkItem childItem = null;
				Iterator iter = getParentWork().getTransactions().iterator();
				while (iter.hasNext()) {
					childItem = (WorkItem) iter.next();
					if (childItem.getItemID().equals(work.getID())) {
						iter.remove();
						break;
					}
				}
				unlockWorkItems();
			}
		}
		return getResult();
	}

	/**
	 * Calls VP/MS model to determine if an aggregate contract work item is in the end queue or not.
	 * @param nbaLob the work LOBS
	 * @return true if an aggregate contract is in the end queue else return false.
	 * @throws NbaBaseException
	 */
	protected boolean isAggregateContractInEndQueue(NbaLob nbaLob) throws NbaBaseException {
		NbaVpmsModelResult data = getXmlData(nbaLob, NbaVpmsConstants.QUEUE_STATUS_CHECK, NbaVpmsConstants.EP_GET_END_QUEUE_VERIFICATION);
		boolean inEndQueue = true;
		if (data.getVpmsModelResult() != null && data.getVpmsModelResult().getResultDataCount() > 0) {
			ResultData resultData = data.getVpmsModelResult().getResultDataAt(0);
			if (resultData.getResult().size() > 0) {
				inEndQueue = Boolean.valueOf(resultData.getResultAt(0)).booleanValue();
			}
		}
		return inEndQueue;
	}

	/**
	 * Creates new aggregate contract work item. Call VP/MS model to get work type, initial status, work priority and priority action.
	 * @throws NbaBaseException
	 */
	protected NbaTransaction createNewAggregateContract() throws NbaBaseException { //NBLXA-1554[NBLXA-2055]
		HashMap deOink = getDeOinkFromCase(); //ALNA204
		deOink.put(NbaVpmsConstants.A_RCMTEAM, rcmTeam); //APSL4412
		if (getAggReference().equalsIgnoreCase(NbaConstants.A_QUEUE_AGGREGATE_CONTRACT)) { //APSL4412
			deOink.put(NbaVpmsConstants.A_WorkTypeLOB, NbaConstants.A_WT_AGGREGATE_CONTRACT); //APSL4412
			deOinkTermConvData(deOink); //APSL4412
		}
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(),getNbaTxLife(),deOink);//ALS5718 //ALNA204
		NbaTransaction nbaTransaction = getParentWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
		nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		NbaLob transactionLOBs = nbaTransaction.getNbaLob();
		NbaLob parentLob = getParentWork().getNbaLob();//SR494086 ADC Retrofit
		// ALII264
		List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2AGGCNT_CREATE_AGGCNT);
		getParentWork().getNbaLob().copyLOBsTo(transactionLOBs, lobList);
		transactionLOBs.setQueueEntryDate(new Date());//ALS3923
		transactionLOBs.setWritingAgency(NbaUtils.getWritingAgencyId(getNbaTxLife()));//ALS4929		
		transactionLOBs.setAppProdType(parentLob.getAppProdType()); //SR494086.5, ADC Retrofit
		transactionLOBs.setAggrReference(getAggReference()); // CR57950 and CR57951
		transactionLOBs.setReplCMQueue(parentLob.getReplCMQueue()); //Retrofitted QC#5923 APSL925
		transactionLOBs.setDistChannel(String.valueOf(parentLob.getDistChannel()));//SR522166 APSL1753
		
		//Begin CR1345857(APSL2575)
		if (getPredictiveIndicator() && getAggReference().equalsIgnoreCase(NbaConstants.A_QUEUE_AGGREGATE_CONTRACT)) {
			transactionLOBs.setPredictiveInd(parentLob.getPredictiveInd());
		}
		//End CR1345857(APSL2575)
		//APSL4077(QC14992) Code to copy AGRF from new transaction to work item removed	
		setRouteReason(nbaTransaction, provider.getInitialStatus());			
		//NBLXA-1326-START - Defect-NBLXA-1438
		if (NbaConstants.A_QUEUE_AGGREGATE_CONTRACT_UW.equalsIgnoreCase(transactionLOBs.getAggrReference())) {
			//transactionLOBs.setReqTypeAt(work.getNbaLob().getReqType(), 1); //NBLXA-2264, APSL5055: deleted code 
			addReqTypeReceivedToWorkitem(nbaTransaction.getID(), String.valueOf(work.getNbaLob().getReqType())); //NBLXA-2264, APSL5055
		}
		//NBLXA-1326-END
		return nbaTransaction; //NBLXA-1554[NBLXA-2055]
	}

	//CR1345857(APSL2575) New Method
	protected boolean getPredictiveIndicator() throws NbaBaseException {
		return getParentWork().getNbaLob().getPredictiveInd();
	}

	/**
	 * refactored from createNewAggregateContract() method
	 */
	//CR59174 New Method// ALNA204 refactored
	protected NbaProcessWorkItemProvider getWorkItemFromVPMS() throws NbaBaseException {
		HashMap deOink = getDeOinkFromCase();
		return new NbaProcessWorkItemProvider(getUser(), getWork(),getNbaTxLife(),deOink);//ALS5718
	}
	
	/**
	 * refactored from getWorkItemFromVPMS() method
	 */
	//ALNA204 New Method
	protected HashMap getDeOinkFromCase() throws NbaBaseException {
		HashMap deOink = new HashMap();
		deOink.put("A_ContractChgTypeLOB",NbaUtils.isBlankOrNull(getParentWork().getNbaLob().getContractChgType()) ? "" : getParentWork().getNbaLob().getContractChgType());//ALS5815
		deOink.put("A_LstNonRevReqRecLOB",getParentWork().getNbaLob().getLstNonRevReqRec() ? "true" : "false");
		deOink.put("A_PaidChgCMQueueLOB",NbaUtils.isBlankOrNull(getParentWork().getNbaLob().getPaidChgCMQueue()) ? "" : getParentWork().getNbaLob().getPaidChgCMQueue());//ALS5815
		deOink.put("A_CaseManagerQueueLOB",NbaUtils.isBlankOrNull(getParentWork().getNbaLob().getCaseManagerQueue()) ? "" : getParentWork().getNbaLob().getCaseManagerQueue());//ALS5815
		//End ALS5718
		deOink.put("A_ReplCMQueueLOB",NbaUtils.isBlankOrNull(getParentWork().getNbaLob().getReplCMQueue()) ? "" : getParentWork().getNbaLob().getReplCMQueue());//CR57950 and CR57951
		deOink.put("A_ExchCMQueueLOB",NbaUtils.isBlankOrNull(getParentWork().getNbaLob().getExchCMQueue()) ? "" : getParentWork().getNbaLob().getExchCMQueue());//CR59174
		deOink.put("A_ApplicationStatus",getParentWork().getNbaLob().getStatus());//APSL2943 QC11357
		deOink.put("A_OriginalWorkTypeLOB",getWorkType());//APSL4121 QC14784
		deOink.put("A_UndwrtQueueLOB", getUnderwriter(getParentWork().getNbaLob()));//NBLXA-1326
		return deOink;
		
	}
	// New Method NBLXA-1326
	public String getUnderwriter(NbaLob parentWork) throws NbaBaseException {
		NbaTable nbaTable = new NbaTable();
		String underwriterQueue = String.valueOf(parentWork.getUndwrtQueue());
		String underwriterName = nbaTable.getAWDQueueTranslation(parentWork.getBusinessArea(), underwriterQueue);
		if (null == underwriterName || (underwriterName != null && underwriterName.trim().length() == 0)) {
			underwriterQueue = getDefaultUnderwriter();
		}
		return NbaUtils.isBlankOrNull(underwriterQueue) ? "" : underwriterQueue;
	}
	/**
	 * Returns �true� if one or more work items are residing in the �NBAGGCNT� queue with same policy number and company name. 
	 * Called only when �Get work� gets an �Aggregate Contract� work item in the Aggregate Contract automated process.
	 * @return
	 * @throws NbaBaseException
	 */
	protected boolean hasOtherWorkItemsInAggregateContractQueue() throws NbaBaseException {
		List transactions = getParentWork().getNbaTransactions();
		int count = transactions.size();
		boolean hasMoreWorkFlag = false;
		NbaTransaction nbaTransaction = null;
		for (int i = 0; i < count; i++) {
			nbaTransaction = (NbaTransaction) transactions.get(i);
			if (!NbaConstants.A_WT_AGGREGATE_CONTRACT.equalsIgnoreCase(nbaTransaction.getWorkType())) {
				if (work.getQueue().equalsIgnoreCase(nbaTransaction.getQueue())) {
					hasMoreWorkFlag = true;
					break;
				}
			}
		}
		return hasMoreWorkFlag;
	}

	/**
	 * Retrieve the parent case with lock and all its children without taking lock 
	 * WARNING: Call this method for transaction work only
	 * @return the parent case with all children
	 */
	protected NbaDst retrieveParentWithTransactions() throws NbaBaseException {
		//create and set parent case retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		// if case
		if (getWork().isCase()) {
			retOpt.setWorkItem(getWork().getID(), true);
			retOpt.requestTransactionAsChild();
			retOpt.setLockWorkItem();
		} else { // if a transaction
			retOpt.setWorkItem(getWork().getID(), false);
			retOpt.requestCaseAsParent();
			retOpt.requestTransactionAsSibling();
			retOpt.setLockWorkItem();
			retOpt.setLockParentCase();
		}
		//get case from awd
		NbaDst parentCase = retrieveWorkItem(getUser(), retOpt);
		return parentCase;
	}
	
	
	/**
	 * Verify if an open aggregate contract wokritem exists on the case. Calls VP/MS model to
	 * determine if the work item is in the end queue or not.
	 * @return true if an open aggregate contract wokritem exists on the case else return false.
	 * @throws NbaBaseException
	 */
	//ALNA204 refactored method 
	protected boolean isAnyOpenAggregateContract(String aggRef) throws NbaBaseException {//CR57950 and CR57951 changed signature of this method
		List transactions = getParentWork().getNbaTransactions();
		int count = transactions.size();
		NbaTransaction anyTransaction = null;
		NbaTransaction endedTransaction = null;
		boolean hasOpenSimilarAggCnt = false;
		boolean hasClosedSimilarAggCnt = false;
		for (int i = 0; i < count; i++) {
			anyTransaction = (NbaTransaction) transactions.get(i);
			if (NbaConstants.A_WT_AGGREGATE_CONTRACT.equalsIgnoreCase(anyTransaction.getWorkType())	&& aggRef.equalsIgnoreCase(anyTransaction.getNbaLob().getAggrReference())) {//CR57950 and CR57951
				if (isAggregateContractInEndQueue(anyTransaction.getNbaLob())) {
					hasClosedSimilarAggCnt = true;
					endedTransaction = anyTransaction;
					continue;
				}
				hasOpenSimilarAggCnt = true;
				break;
			}
		}
		if (hasClosedSimilarAggCnt && !hasOpenSimilarAggCnt) {
			updateExistingAggregateContract(endedTransaction);
			hasOpenSimilarAggCnt = true;
		} else if(hasOpenSimilarAggCnt && NbaConstants.A_QUEUE_AGGREGATE_CONTRACT_UW.equalsIgnoreCase(aggRef)) {//NBLXA-1326-START - Defect-NBLXA-1438
			updateExistingAggregateContractForReqTyp(anyTransaction);
			hasOpenSimilarAggCnt = true;
		}
		return hasOpenSimilarAggCnt;
	}
	
	/**
	 * Verify if an open aggregate contract wokritem exists on the case. Update the WI to send back to OtherStatus returned from VPMS
	 * @return true if an open aggregate contract wokritem exists on the case else return false.
	 * @throws NbaBaseException
	 */
	//ALNA204 new method added 
	protected void updateExistingAggregateContract(NbaTransaction endedTransaction) throws NbaBaseException {
		HashMap deOink = new HashMap(); // NBLXA-1554[NBLXA-2061]
		//code removed for NBLXA-1554[NBLXA-2061]
		deOink =(HashMap) getDeOinkMap(deOink); //NBLXA-1554[NBLXA-2061]
		if (getAggReference().equalsIgnoreCase(NbaConstants.A_QUEUE_AGGREGATE_CONTRACT)) { //APSL4412
			deOinkTermConvData(deOink); //APSL4412
		}
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(endedTransaction.getNbaLob());
		oinkData.setContractSource(getNbaTxLife());
		NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getDataFromVpms(NbaVpmsAdaptor.AUTO_PROCESS_STATUS,
				NbaVpmsAdaptor.EP_WORKITEM_STATUSES, oinkData, deOink, null));
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(endedTransaction.getID(), false);
		retOpt.setLockWorkItem();
		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
		//Begin APSL3952
		List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2AGGCNT_UPDATE_AGGCNT);
		getParentWork().getNbaLob().copyLOBsTo(aWorkItem.getNbaLob(), lobList);
		//End APSL3952
		aWorkItem.setStatus(statusProvider.getOtherStatus());
		aWorkItem.getNbaLob().setReplCMQueue(endedTransaction.getNbaLob().getReplCMQueue());//QC9278
		//BEGIN NBLXA-1554[NBLXA-2055]
		if (!NbaUtils.isBlankOrNull(statusProvider.getReason())) {
			aWorkItem.getNbaLob().setRouteReason(statusProvider.getReason());
			if (getWork().getNbaLob().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REPL_NOTIFICATION)) {
				getWork().getNbaLob().setRouteReason(statusProvider.getReason());
				getWork().setUpdate();
			}
		} else { //END NBLXA-1554[NBLXA-2055]
			setRouteReason(aWorkItem, statusProvider.getOtherStatus());
		}
		//Begin CR1345857(APSL2575)
		if (aWorkItem.getNbaLob().getAggrReference().equalsIgnoreCase(NbaConstants.A_QUEUE_AGGREGATE_CONTRACT)) {
			aWorkItem.getNbaLob().setPredictiveInd(getParentWork().getNbaLob().getPredictiveInd());
		}
		//End CR1345857(APSL2575)
		//Deleted code APSL3952
		
		//NBLXA-1326-START - Defect-NBLXA-1438
		if (NbaConstants.A_QUEUE_AGGREGATE_CONTRACT_UW.equalsIgnoreCase(aWorkItem.getNbaLob().getAggrReference())) {
			//NBLXA-2264, APSL5055: code commented
			/* 
			NbaLob lob = aWorkItem.getNbaLob();
			int seq = 1;
			while (lob.getReqTypeAt(seq) != 0) {
				seq++;
			}
			aWorkItem.getNbaLob().setReqTypeAt(work.getNbaLob().getReqType(), seq++);
			*/
			//NBLXA-2264, APSL5055: code commented
			addReqTypeReceivedToWorkitem(aWorkItem.getID(), String.valueOf(work.getNbaLob().getReqType())); //NBLXA-2264, APSL5055
		}
		//NBLXA-1326-END
		
		aWorkItem.setUpdate();
		update(aWorkItem);
		unlockWork(aWorkItem);
	}
	/**
	 * NBLXA-1326-START - Defect-NBLXA-1438
	 * Verify if an open aggregate contract work-item exists on the case. Append current requirement type to the aggregate WI. 
	 * @throws NbaBaseException
	 */
	protected void updateExistingAggregateContractForReqTyp(NbaTransaction endedTransaction) throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(endedTransaction.getID(), false);
		retOpt.setLockWorkItem();
		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
		if (NbaConstants.A_QUEUE_AGGREGATE_CONTRACT_UW.equalsIgnoreCase(aWorkItem.getNbaLob().getAggrReference())) {
			//NBLXA-2264, APSL5055: code commented
			/*
			NbaLob lob = aWorkItem.getNbaLob();
			int seq = 1;
			while (lob.getReqTypeAt(seq) != 0) {
				seq++;
			}
			aWorkItem.getNbaLob().setReqTypeAt(work.getNbaLob().getReqType(), seq++);
			*/ 
			//NBLXA-2264, APSL5055: code commented
			addReqTypeReceivedToWorkitem(aWorkItem.getID(), String.valueOf(work.getNbaLob().getReqType())); //NBLXA-2264, APSL5055
		}
		aWorkItem.setUpdate();
		update(aWorkItem);
		unlockWork(aWorkItem);
	}
	
	/**
	 * Calls VP/MS model and return XML result.
	 * @param nbaLob the work LOBs
	 * @param vpmsModelName the VP/MS model name
	 * @param entryPoint the VP/MS entry point to be called
	 * @return the VP/MS XML result
	 * @throws NbaBaseException
	 */
	protected NbaVpmsModelResult getXmlData(NbaLob nbaLob, String vpmsModelName, String entryPoint) throws NbaBaseException {
		return new NbaVpmsModelResult(getDataFromVpms(vpmsModelName, entryPoint, new NbaOinkDataAccess(nbaLob), null, null).getResult());
	}
	
	//APSL677 New method
	/**Takes activate minutes for a workitem, and returns suspendVO object for this work item.
	 * @throws NbaBaseException
	 */
	protected NbaSuspendVO getSuspendWorkVO(int type, int value) {
		GregorianCalendar cal = new GregorianCalendar();
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		cal.setTime(new Date());
		cal.add(type, value);
		suspendVO.setTransactionID(getWork().getID());
		suspendVO.setActivationDate(cal.getTime());
		//addComment("Suspended to allow for contract aggregation");//ALNA212
		return suspendVO;
	}

	//ALII923 deleted method for overloaded method.
	/**
	 * Commit any changes to the work flow system
	 * @param hasErrors the error indicator 
	 * @param suspendItem the NbaSuspendVo for work item
	 * @throws NbaBaseException
	 */
	protected void commit(boolean hasErrors, NbaSuspendVO suspendItem, boolean newWorkCreated) throws NbaBaseException {
		if (!hasErrors) {
			//update work
			if (newWorkCreated) {
				setParentWork(update(getParentWork()));
			}
			setWork(update(getWork()));
			//suspend workitem, if any
			if(suspendItem != null){
				suspendWork(suspendItem);
			}
		} else { //has errors, update original work only
			setWork(update(getWork()));
		}
	}

	/**
	 * Unlocks workitems
	 * @throws NbaBaseException
	 */
	protected void unlockWorkItems() throws NbaBaseException {
		NbaContractLock.removeLock(getUser());
		if (getParentWork() != null) {
			unlockWork(getParentWork());
		} else {
			unlockWork();
		}
	}

	/**
	 * Returns the parent workitem 
	 * @return
	 */
	protected NbaDst getParentWork() {
		return parentWork;
	}

	/**
	 * Sets the parent workitem
	 * @param newWork the parent workitem
	 */
	protected void setParentWork(NbaDst newWork) {
		parentWork = newWork;
	}

	/**
	 * Create a TX Request value object that will be used to retrieve the contract.
	 * @param nbaDst the workitem object for that holding request is required
	 * @param access the access intent to be used to retrieve the data, either READ or UPDATE
	 * @param businessProcess the name of the business function or process requesting the contract
	 * @return a value object that is the request
	 * @throws NbaBaseException
	 */
		//ALS5026 New Method
		//CR61627-PERF added throws clause
		public NbaTXRequestVO createRequestObject(NbaDst nbaDst, int access, String businessProcess) throws NbaBaseException{
			NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
			nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
			nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
			nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
			try {
				//ALII1290 begin
				if (getWork().isCase()) { 
					parentWork = getWork();
				} else {
					parentWork = retrieveParentWork(true); //CR61627-PERF
				}
				//ALII1290 end
			} catch (NbaBaseException e1) {
					e1.printStackTrace();
					throw e1; //CR61627-PERF
			}
			//Setting parent LOB as the check might be from different company, also for retriving the parent case TxLife parent case LOb should be used
			nbaTXRequest.setNbaLob(parentWork.getNbaLob());
			nbaTXRequest.setNbaUser(getUser());
			nbaTXRequest.setWorkitemId(nbaDst.getID());
			nbaTXRequest.setCaseInd(nbaDst.isCase());
		
			if (access != -1) {
				nbaTXRequest.setAccessIntent(access);
			} else {
				nbaTXRequest.setAccessIntent(READ);
			}
			if (businessProcess != null) {
				nbaTXRequest.setBusinessProcess(businessProcess);
			} else {
				nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser())); //SPR2639 
			}
			return nbaTXRequest;
		}
		/*
		 * Override the base method and do NOT lock siblings.
		 */
		//CR61627-PERF New Method
		protected NbaDst retrieveParentWork(NbaDst nbaDst, boolean lock, boolean retrieveSiblings) throws NbaBaseException {
			return retrieveParentWork(nbaDst,lock,retrieveSiblings,false);
		}	
		//New Method CR57950 and CR57951
		protected String getAggReference() {
			return getWork().getQueue();
		}
		
		//CR61627 New Method
		protected void resetCaseManagers() throws NbaBaseException{
			
			if (isCaseManagerMissing()) {
				reinitializeStatusFields();
				updateParentWork(getEquitableQueue(getCaseManagerLOB(),getCaseManagerAssignment()));
				
			}
		}
		 /**
	     * New method to add de-Oink variables
	     */
	    //CR61627 new method
	    protected void reinitializeStatusFields() throws NbaBaseException {
	    		
	    	NbaProcessStatusProvider statProvider = new NbaProcessStatusProvider(getUser(), parentWork, getNbaTxLife());
	    	//Begin NBLXA-2343
			replacementCM = AxaUtils.getMiscCMAssignmentRules(statProvider.getReplCMQueue());
			exchange1035CM = AxaUtils.getMiscCMAssignmentRules(statProvider.getExchangeCaseMgrQueue());
	    	//End NBLXA-2343
	    }
	    
	    //CR61627 New Method
	    protected boolean isCaseManagerMissing() {
	    	return false;
	    	
	    }
	    
	    //CR61627 New Method
	    protected String getCaseManagerAssignment() {
	    	return "";
	    }
	    
	    //CR61627 New Method
	    protected String getCaseManagerLOB() {
	    	return "";
	    }
	    //CR61627 New Method
	    protected void updateParentWork(String equitableQueue) throws NbaBaseException {

	    	parentWork.getNbaLob().setLob(getCaseManagerAssignment(),equitableQueue);
	    	List children = parentWork.getWorkItem().getWorkItemChildren();
	    	parentWork.getWorkItem().setWorkItemChildren(new ArrayList());
	    	parentWork.setUpdate();
	    	parentWork = update(parentWork);
	    	parentWork.getWorkItem().setWorkItemChildren(children);
	    	
	    }
	    //ALII1290
	    protected boolean retrieveTransactionsRequired() {
	    
	    	boolean retrieveTrans = false;
	    	try {
	    		if (null == getParentWork() || 0 >= getParentWork().getTransactions().size() ) {
	    			retrieveTrans = true;
	    		}
	    	} catch (NbaBaseException nbe) {
	    		getLogger().logError("Error retrieving transactions. Will try again.");
	    		retrieveTrans = true;
	    	}
	    	return retrieveTrans;
	    }
	    /**
		 * APSL4342 New Method
		 * call to update todo list count
		 * @throws NbaBaseException 
		 */
	    protected void updateTODOListCount() throws NbaBaseException {
	    	String newToDoCount = NbaUtils.getNewToDOCount(parentWork,ADDCOUNT);
	    	if(!QUESTIONMARK.equals(newToDoCount)){
	    		parentWork.getNbaLob().setToDoCount(newToDoCount);
	    		List children = parentWork.getWorkItem().getWorkItemChildren();
	    		parentWork.getWorkItem().setWorkItemChildren(new ArrayList());
	    		parentWork.setUpdate();
	    		parentWork = update(parentWork);
	    		parentWork.getWorkItem().setWorkItemChildren(children);
	    		setTODOLOBUpdated();
	    	}
	    }

		/**
		 * APSL4342 New Method
		 * check wether LOB is already updated.
		 */
		protected boolean toUpdateTODOLOB() {
			return !A_WT_AGGREGATE_CONTRACT.equalsIgnoreCase(work.getWorkType()) && 
			!A_WT_APPLICATION.equals(work.getWorkType()) && 
			!PROCESSED_INDICATOR.equals(getWork().getNbaLob().getToDoCount());
		}
		/**
		 * APSL4342 New Method
		 * set update flag on LOB.
		 */
		public void setTODOLOBUpdated(){
			getWork().getNbaLob().setToDoCount(PROCESSED_INDICATOR);
		}
		
	// New Method: NBLXA-1554[NBLXA-2055]
	protected void reinitializeFields(NbaTransaction aggContract) throws NbaBaseException {
		HashMap deOink = new HashMap();
		NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(user, getWork(), getDeOinkMap(deOink)); //NBLXA-1554[NBLXA-2061]
		if (getWork().getNbaLob().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REPL_NOTIFICATION) ||
				getWork().getNbaLob().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CWA)) {	//NBLXA-1554[NBLXA-2075]
			getWork().getNbaLob().setRouteReason(statusProvider.getReason());
			getWork().setUpdate();
		}
		aggContract.getNbaLob().setRouteReason(statusProvider.getReason());
	}

	// New Method: NBLXA-1554[NBLXA-2055],NBLXA-1554[NBLXA-2061],NBLXA-1554[NBLXA-2064],NBLXA-1554[NBLXA-2068]
	protected Map getDeOinkMap(HashMap deOink) throws NbaBaseException {
		deOink = getDeOinkFromCase();
		deOink.put(NbaVpmsConstants.A_RCMTEAM, rcmTeam);
		if (getWork() != null && getWork().getNbaLob() != null
				&& getWork().getNbaLob().getWorkType().equalsIgnoreCase((NbaConstants.A_WT_REPL_NOTIFICATION))) {
			if (!NbaUtils.isBlankOrNull(getWork().getNbaLob().getQCPassFail())
					&& (getWork().getNbaLob().getQCPassFail().equalsIgnoreCase(NbaOliConstants.NBALOB_QCPASS)
							|| getWork().getNbaLob().getQCPassFail().equalsIgnoreCase(NbaOliConstants.NBALOB_QCFAIL))) {
				deOink.put(NbaVpmsConstants.A_AGGCRITERIA, getWork().getNbaLob().getQCPassFail());
			} else if (!NbaUtils.isBlankOrNull(getWork().getNbaLob().getReqType())
					&& getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_1009800175) {
				deOink.put(NbaVpmsConstants.A_AGGCRITERIA, String.valueOf(getWork().getNbaLob().getReqType()));
			} else if (!NbaUtils.isBlankOrNull(getNbaTxLife().getPolicy().getReplacementType())
					&& (getNbaTxLife().getPolicy().getReplacementType() == NbaOliConstants.OLI_REPTY_UNADREPSTA
							|| getNbaTxLife().getPolicy().getReplacementType() == NbaOliConstants.OLI_REPTY_UNADREPDISB)
					&& (!NbaUtils.isBlankOrNull(getWork().getNbaLob().getStatus())
							&& (getWork().getNbaLob().getStatus().equalsIgnoreCase(A_STATUS_REG60_INITIAL_REVIEW)
									|| getWork().getNbaLob().getStatus().equalsIgnoreCase(A_STATUS_RPCM_INITIAL_REVIEW)))) {
				deOink.put(NbaVpmsConstants.A_AGGCRITERIA, INITIAL);
			} else {
				deOink.put(NbaVpmsConstants.A_AGGCRITERIA, "0");
			} 
		} else if (getWork() != null && getWork().getNbaLob() != null 
				&& getWork().getNbaLob().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REQUIREMENT)) {
			if (!NbaUtils.isBlankOrNull(getWork().getNbaLob().getReqType())) {
				if (NbaOliConstants.OLI_REQCODE_STATEDISC == getWork().getNbaLob().getReqType()) {
					deOink.put(NbaVpmsConstants.A_AGGCRITERIA, String.valueOf(getWork().getNbaLob().getReqType()));
				} else if (NbaOliConstants.OLI_REQCODE_1009800194 == getWork().getNbaLob().getReqType()) {
					deOink.put(NbaVpmsConstants.A_AGGCRITERIA, String.valueOf(getWork().getNbaLob().getReqType()));
				} else if (NbaOliConstants.OLI_REQCODE_1009800081 == getWork().getNbaLob().getReqType()) {// NBLXA1554[NBLXA-2074]
					deOink.put(NbaVpmsConstants.A_AGGCRITERIA, String.valueOf(getWork().getNbaLob().getReqType()));
				} else if (NbaOliConstants.OLI_REQCODE_1009800206 == getWork().getNbaLob().getReqType()) {// NBLXA1554[NBLXA-2082]
					deOink.put(NbaVpmsConstants.A_AGGCRITERIA, String.valueOf(getWork().getNbaLob().getReqType()));
				}
			} else {
				deOink.put(NbaVpmsConstants.A_AGGCRITERIA, "0");
			}
		} // NBLXA-2328[NBLXA-2496] Begin
		else if (getWork() != null && getWork().getNbaLob() != null
				&& getWork().getNbaLob().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CONT_PRINT_EXTRACT)) {
			// NBLXA2516[NBLXA2328] Begin
			if (getWork().getNbaLob().getStatus().equalsIgnoreCase(A_STATUS_REQ_PRINT_ERROR)) {
				deOink.put(NbaVpmsConstants.A_AGGCRITERIA, A_STATUS_REQ_PRINT_ERROR);
			}
			// NBLXA2516[NBLXA2328] End
			else if (getWork().getNbaLob().getStatus().equalsIgnoreCase(A_STATUS_REG_DATE_CONFLICT)) {
				deOink.put(NbaVpmsConstants.A_AGGCRITERIA, A_STATUS_REG_DATE_CONFLICT);
			} else {
				deOink.put(NbaVpmsConstants.A_AGGCRITERIA, "0");
			}
		} else {
			deOink.put(NbaVpmsConstants.A_AGGCRITERIA, "0");
		}
		// NBLXA-2328[NBLXA-2496] End
		deOink.put(NbaVpmsConstants.A_AGGWRKTYPE, work.getWorkType());
		return deOink;
	}
	
	//NBLXA-2264, NBA331.1, APSL5055 New Method
	protected void addReqTypeReceivedToWorkitem(String workitemID, String reqType) {
		RequirementsReceivedUpdateRequest request = new RequirementsReceivedUpdateRequest();
		request.setApplicationWorkItemID(workitemID);
		request.addRequirementWorkItem(reqType);
		Result result = new AccelResult();
		result.addResult(request);
		result = getCurrentBP().callService(ServiceCatalog.ADD_UW_REQTYPES_RECEIVED, result);
	}	
		
}