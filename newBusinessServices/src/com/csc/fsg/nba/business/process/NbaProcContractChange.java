package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.business.transaction.NbaContractChangeUtils;
import com.csc.fsg.nba.business.transaction.NbaHoldingInqTransaction;
import com.csc.fsg.nba.business.uwAssignment.AxaUnderwriterAssignmentEngine;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaContractChangeDataTable;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.AxaUWAssignmentEngineVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.Relation;
/**
 * NbaProcContractChange is the class to process cases found in NBCNTCHG queue. * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2366</td><td>Version 5</td><td>Added unique file names for webservice stubs</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr>
 * <tr><td>SPR2173</td><td>Version 5</td><td>Issue Date LOB is not reinitialized when the case is retrieved for reissues</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR2248</td><td>Version 6</td><td>Reinstatements not sent correctly for Traditional Term products and Advanced Products</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR1613</td><td>Version 8</td><td>Some Business Functions should be disabled on an Issued Contract</td></tr>
 * <tr><td>NBA251</td><td>Version 8</td><td>nbA Case Manager and Companion Case Assignment</td></tr>
 * <tr><td>AXAL3.7.04</td><td>Axa Life Phase 1</td><td>Paid Changes</td></tr>
 * <tr><td>CR60956</td><td>AXA Life Phase 2</td><td>Life 70 Reissue</td></tr>
 * <tr><td>SR831136 APSL4088</td><td>Descretionary</td><td>PCCM Workflow</td></tr>
 * <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 4
 */
public class NbaProcContractChange extends NbaAutomatedProcess {

	protected NbaDst caseWork = null;
	protected NbaTXLife mergedContract = null;
	protected boolean caseExists = false;
	protected boolean standalone = true;
	//SPR2380 removed logger
	protected static final String USER_CONTRACT_CHANGE = "NBCCWRK";
	protected long cntChgType = -1L;
	protected NbaProcessStatusProvider caseStatusProvider = null;//NBA251
	protected NbaProcessStatusProvider workStatusProvider = null;//APSL3603(QC13073)
	
	/**
	 * NbaProcContractChange constructor.
	 */
	public NbaProcContractChange() {
		super();
	}

//	NBA103 - removed method

	/**
	 * This method drive the Automated Contract Change process.
	 * @param user the NbaUser for whom the process is being executed
	 * @param work a NbaDst value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		if (!initialize(user, work)) {
			return getResult();
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Starting Contract Change Process for contract " + getWork().getNbaLob().getPolicyNumber());
		}
		
		//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestSources();
		retOpt.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retOpt));  //NBA213
		//NBA213 deleted code

		doContractChangeProcess();
		setWorkStatus(); //APSL3603(QC13073)
		createRelationshipCaseManagerTransaction();//APSL4412
		if (getResult() == null) {
			if(getWorkStatusProvider() != null){//APSL3603(QC13073) start
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getWorkStatusProvider().getPassStatus()));
			}else{
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}//APSL3603(QC13073) end
			updateLOBs();
			setCaseStatus();
			changeStatus(getWork(), getResult().getStatus());
			updateAWD(false);
			removeTempData();
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Completed Contract Change Process for contract " + getWork().getNbaLob().getPolicyNumber());
			}
		}else {
			getLogger().logDebug("Errors during Contract Change Process for contract " + getWork().getNbaLob().getPolicyNumber());
			changeStatus(getResult().getStatus());
			//we are here means some errors have occured
			updateAWD(true);	
		}
		NbaContractLock.removeLock(getWork(), getUser());//APSL3603(QC13073)
		return getResult();
	}
	
	/**
	 * It perform following contract change processes.
	 * 	Search and retrieve case from AWD if exists
	 * 	Find data store mode
	 * 	Get and merge holdings from pending and temp data(includes data from admin system) that retrieve on contract change sequence views 	 
	 * 	Create a case if not exists
	 * 	Add contract change workitem to the case
	 * 	Update merged contract to the pending backend system	 * 	
	 * @throws NbaBaseException
	 */
	protected void doContractChangeProcess() throws NbaBaseException {
		retrieveCaseFromAWD();
		setStandalone(NbaServerUtility.isDataStoreDB(work.getNbaLob(), getUser()));
		mergeTempData();
		setCntChgType(getWork().getNbaLob().getContractChgType() != null ? Long.parseLong(getWork().getNbaLob().getContractChgType()) : -1L);
		if (getResult() == null) {
			updateContractChangeInfo();
			if (!isCaseExists()) {
				setCaseWork(createCase());
				updateLOBs();//AXAL3.7.04
				getCaseWork().getNbaLob().setCaseFinalDispstn((int) NbaOliConstants.NBA_FINALDISPOSITION_ISSUED);//AXAL3.7.04
			}
			assignCaseQueues();//NBA251
			addWorkToCase();
			updateMergedContract();
		}
	}

	/**
	 * Search and Retrieve Case from AWD for a contract change workitem.
	 * @throws NbaBaseException
	 */
	protected void retrieveCaseFromAWD() throws NbaBaseException {
		NbaLob workLob = getWork().getNbaLob();
		NbaLob lob = new NbaLob();
		lob.setPolicyNumber(workLob.getPolicyNumber());
		lob.setCompany(workLob.getCompany());
		setCaseWork(getCaseFromAWD(getUser(), lob, true));//AXAL3.7.04
		if (getCaseWork() != null) {
			setCaseExists(true);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Found a case in AWD for contract change");
			}
		}
	}

	/**
	 * Update ApplicationType and ContractChangeTypebased on ContractChgType LOB. 
	 */
	protected void updateContractChangeInfo(){
		Policy policy = getMergedContract().getPolicy();
		PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExt == null) {
			OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			policyExt = oliExt.getPolicyExtension();
			policy.addOLifEExtension(oliExt);
		}
		ApplicationInfo appInfo = policy.getApplicationInfo();
		if (appInfo == null) {
			appInfo = new ApplicationInfo();
			policy.setApplicationInfo(appInfo);
		}
		if (getCntChgType() == NbaOliConstants.NBA_CHNGTYPE_REISSUE) {
			ApplicationInfoExtension appInfoExtn = NbaUtils.getAppInfoExtension(appInfo);//ALII1206			
			appInfoExtn.setApplicationSubType(NbaOliConstants.OLI_APPSUBTYPE_REISSUE);//ALII1206
			//APSL4585 Register Date changes removed reset unbound Indicator
			policyExt.setContractChangeReprintInd(false);//ALS5777
			policyExt.deleteContractChangeReprintDate(); //ALS5927	
			//APSL3525 Reissue SR october changes start
			if(getCaseWork() !=null && NbaConstants.SYST_CAPS.equals(getCaseWork().getNbaLob().getBackendSystem()) && policy.hasPaymentAmt() && (!policyExt.hasOldPremiumAmt()|| policyExt.getOldPremiumAmt()<= 0 )) {
				policyExt.setOldPremiumAmt(policy.getPaymentAmt());				
			}else if (getCaseWork() !=null && NbaConstants.SYST_LIFE70.equals(getCaseWork().getNbaLob().getBackendSystem()) && policy.hasMinPremiumInitialAmt() &&  (!policyExt.hasOldPremiumAmt()|| policyExt.getOldPremiumAmt()<= 0)){
				policyExt.setOldPremiumAmt(policy.getMinPremiumInitialAmt());				
			}		
			//APSL3525 Reissue SR october changes end
		} else if (getCntChgType() == NbaOliConstants.NBA_CHNGTYPE_COMPLEX_CHANGE) {
			policyExt.setContractChangeType(NbaOliConstants.OLIX_CHANGETYPE_ADVPRDCMPLX); //HB
			appInfo.setApplicationType(NbaOliConstants.OLI_APPTYPE_CHANGE);
		} else if (getCntChgType() == NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT) {
			appInfo.setApplicationType(NbaOliConstants.OLI_APPTYPE_REINSTATEMENT);
			//BEGIN NBA130
			Coverage primaryCoverage = getMergedContract().getPrimaryCoverage();
			int count = primaryCoverage.getReinsuranceInfoCount();
			for (int i = 0; i < count; i++) {
				ReinsuranceInfo reinInfo = primaryCoverage.getReinsuranceInfoAt(i);
				if (null == reinInfo.getCarrierPartyID() || reinInfo.getCarrierPartyID().length() == 0) {
					reinInfo.setReinsuranceRiskBasis(NbaConstants.REINSURANCE_NONE);
					break;
				}
			}
			//END NBA130
		}
		//If not a Reinstatement or Increase, set the Policy Status to Pending
		if (!(getCntChgType() == NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT || getCntChgType() == NbaOliConstants.OLIX_CHANGETYPE_INCREASE)) { //SPR1613
			policy.setPolicyStatus(NbaOliConstants.OLI_POLSTAT_PENDING); //SPR1613
		} //SPR1613
		//Start AXAL3.7.04
		//Begin ALS4824
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if(appInfoExt != null) {
			appInfoExt.setUnderwritingStatus(NbaOliConstants.OLI_TC_NULL);
			// Start CR60956
			if (appInfoExt.getIssuedToAdminSysInd()) {
				appInfoExt.setReissueType(NbaOliConstants.AXA_REISSUETYPE_PAID);
			} else {
				appInfoExt.setReissueType(NbaOliConstants.AXA_REISSUETYPE_UNPAID);
			}
			//END CR60956
		}
		//End ALS4824
		//ALS4153 Code Deleted
		//ALS4166 code deleted.
		//ALS4200 begin
		if(getNbaTxLife() == null){
			List allRelations = getMergedContract().getOLifE().getRelation();
			for (int i = 0; i < allRelations.size(); i++) {
				NbaUtils.setRelatedRefId((Relation) allRelations.get(i), allRelations);
			}
		}
		//ALS4200 end
		//End AXAL3.7.04
	}
	
	/**
	 * It gets the data from NBA_CONTRACT_CHANGE_DATA table and merge it with existing merged contract.
	 * It assumes that only OINK fields were used during intial contract change process. If any non OINK 
	 * field was used than modify this method to merge that non OINK field.
	 * @throws NbaBaseException
	 */
	// APSL3525(CHAUG005) Method modified
	protected void mergeTempData() throws NbaBaseException {
		// Start APSL3525(CHAUG005)
		NbaLob lob = getWork().getNbaLob();
		NbaContractChangeDataTable tableData = new NbaContractChangeDataTable();
		tableData.setWorkItemId(getWork().getID());
		tableData.setChangeType(Long.parseLong(lob.getContractChgType()));
		tableData.retrieveData();
		String tempData = tableData.getTempContract();
		NbaTXLife tempContract = null;
		if (tempData != null) {
			try {
				tempContract = new NbaTXLife(tempData);
			} catch (Exception e) {
				throw new NbaBaseException("Invalid tempority contract data", e);
			}
		}
		if(getCaseWork() != null && getWork().getNbaLob().getBackendSystem() == null){
			getWork().getNbaLob().setBackendSystem(getCaseWork().getNbaLob().getBackendSystem());
			if (tempData == null && getCaseWork().getNbaLob().getCaseFinalDispstn() == NbaOliConstants.NBA_FINALDISPOSITION_ISSUED) {
				NbaHoldingInqTransaction holdingTrx = new NbaHoldingInqTransaction();
				NbaTXRequestVO nbaTXRequest = holdingTrx.createRequestTransaction(getWork().getNbaLob(), NbaConstants.READ, getUser().getUserID(), getUser());
				tempContract = holdingTrx.processInforceTransaction(getWork().getNbaLob(), getUser());				
			}
		}
		//End APSL3525(CHAUG005)
		setNbaTxLife(getHoldingFromPending(getUser(), getWork()));
		if (getResult() == null) {
			if (tempContract != null && !tempContract.isTransactionError()) {				
				//Begin AXAL3.7.04
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("nbA Contract : " + getNbaTxLife().toXmlString());
					getLogger().logDebug("CAPS Contract : " + tempContract.toXmlString());
				}
				if(NbaConstants.SYST_CAPS.equals(getWork().getNbaLob().getBackendSystem())) { // CR60956
					NbaContractChangeUtils contractChange = new NbaContractChangeUtils(getNbaTxLife(), tempContract);
					//NbaTXLife mergedContract = contractChange.mergeContract();//APSL4094, SR#653140 commented as no need to merge entire CAPS data
					NbaTXLife mergedContract = contractChange.mergeBackendContractInfo();//APSL4094, SR#653140
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Merged Contract : " + mergedContract.toXmlString());
					}
					setMergedContract(mergedContract);
					//End AXAL3.7.04					
				} else if (NbaConstants.SYST_LIFE70.equals(getWork().getNbaLob().getBackendSystem())) { //Begin CR60956
					mergeLife70Details(tempContract, getNbaTxLife()); // APSL3928
					setMergedContract(getNbaTxLife());
				}
				// End CR60956
			} else {
				setMergedContract(getNbaTxLife());
			}
		}
	}

	/**
	 * Remove a row from NBA_CONTRACT_CHANGE_DATA table for this contract.
	 * @throws NbaBaseException
	 */
	protected void removeTempData() throws NbaBaseException {
		NbaContractChangeDataTable tableData = new NbaContractChangeDataTable();
		tableData.setWorkItemId(getWork().getID());
		tableData.setChangeType(Long.parseLong(getWork().getNbaLob().getContractChgType()));
		tableData.delete();
	}

	//SPR3290 code deleted

	/**
	 * Retrieve a holding inquiry from pending backend syatem. 
	 * @param nbaUserVO the NbaUser for whom the process is being executed
	 * @param nbaDst a NbaDst value object 
	 * @return a NbaTXLife object with holding from pending system.
	 * @throws NbaBaseException
	 */
	protected NbaTXLife getHoldingFromPending(NbaUserVO nbaUserVO, NbaDst nbaDst) throws NbaBaseException {
		NbaTXLife response = null;
		// SPR3290 code deleted
		NbaTXRequestVO requestVO = createRequestObject(NbaConstants.READ, nbaUserVO.getUserID());
		if (isStandalone()) {
			requestVO.setOverrideDataSource(STANDALONE);
		} else {
			requestVO.setOverrideDataSource(WRAPPERED);
		}

		// do not copy locking logic from here. Autoprocess automatically lock a database
		setContractAccess(UPDATE);
		NbaContractLock.requestLock(nbaDst, nbaUserVO);

		try {
			response = NbaContractAccess.doContractInquiry(requestVO);  //NBA213
		} catch (NbaDataAccessException de) {
			if (isStandalone()) {
				//if error found than assume case does not exist in pending database
				return null;
			} else {
				throw de;
			}
		}
		handleHostResponse(response, true);
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Completed holding retrieval from pending system");
		}
		return response;
	}

	/**
	 * Apply merged contract to the backend.
	 * @throws NbaBaseException
	 */
	protected void updateMergedContract() throws NbaBaseException {
		getMergedContract().setBusinessProcess(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
		NbaTXLife response = doContractUpdate(getMergedContract());
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Completed contract merge for contract change");
		}
		handleHostResponse(response);
	}

	/**
	 * Create a case NbaDst. Call Auto Process Status VP/MS model to retieve worktype and initial status.
	 * @return the new case NbaDst object.
	 * @throws NbaBaseException
	 */
	protected NbaDst createCase() throws NbaBaseException {
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), new NbaLob(), NbaConstants.A_ST_APPLICATION);
		NbaDst nbaDst = null;
		if (provider != null && provider.getWorkType() != null && provider.getInitialStatus() != null) {
			//NBA208-32
			WorkItem awdCase = new WorkItem();
			nbaDst = new NbaDst();
			//NBA208-32
			nbaDst.setUserID(getUser().getUserID());
			nbaDst.setPassword(getUser().getPassword());
			nbaDst.addCase(awdCase);
			//set Business Area, Work type and Status
			awdCase.setBusinessArea(A_BA_NBA);
			//NBA208-32
			awdCase.setLock("Y");
			awdCase.setAction("L");
			awdCase.setWorkType(provider.getWorkType());
			awdCase.setStatus(provider.getInitialStatus());
			//NBA208-32
			awdCase.setSystemName(getSystemForUser());  //APSL5055-NBA331
			awdCase.setCreate("Y");
			awdCase.setLobData(getWork().getNbaLob().getLobs()); //copy all LOBs from contract change work to case	
			nbaDst.increasePriority(provider.getWIAction(), provider.getWIPriority());
		} else {
			throw new NbaBaseException("Invalid Work Type or Initial Status");
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Completed create case for contract change");
		}

		return nbaDst;
	}

	/**
	 * Adds contract change workitem to the case. Copy ContractChgType LOB from work to case.
	 * @throws NbaBaseException
	 */
	protected void addWorkToCase() throws NbaBaseException {
		NbaTransaction transaction = getWork().getNbaTransaction();
		transaction.setRelate();
		transaction.setUpdate();
		getCaseWork().getNbaCase().addNbaTransaction(transaction); //because it is already created
		//ALS5351 Begin
		NbaSource contractChangeSource = getContractChangeSource();
		if(contractChangeSource != null) {
			getCaseWork().addNbaSource(contractChangeSource);
		}
		//ALS5351 End
		NbaSource paymentSource = getPaymentSource();
		if (paymentSource != null && NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT == getCntChgType()) {
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork().getNbaLob(), NbaConstants.A_ST_PAYMENT);
			if (provider != null && provider.getWorkType() != null && provider.getInitialStatus() != null) {
				NbaTransaction paymentTransaction = getCaseWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
				//NBA208-32
				WorkItem trans = paymentTransaction.getTransaction();
				paymentTransaction.addNbaSource(paymentSource);
				//NBA208-32
				trans.setLock("Y");
				trans.setAction("L");
				updatePaymentLOBs(paymentTransaction.getNbaLob(), getWork().getNbaLob(), paymentSource.getNbaLob());
				paymentSource.setBreakRelation(); //break relation with cntchg work
			}
		}
				
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Added contract change transaction to case");
		}

	}

	/**
	 * This method returns payment source is present on NBCNTCHG workitem
	 * @throws NbaBaseException		
	 */
	protected NbaSource getPaymentSource() throws NbaBaseException {
		List sources = getWork().getNbaSources();		
		for (int i = 0; i < sources.size(); i++) {
			NbaSource source = (NbaSource) sources.get(i);
			if (NbaConstants.A_ST_PAYMENT.equals(source.getSource().getSourceType())) {				
				return source;
			}
		}
		return null;
	}
	
	/**
	 * This method returns contract change source is present on NBCNTCHG workitem
	 * @throws NbaBaseException		
	 */
	//New Method ALS5351
	protected NbaSource getContractChangeSource(){
		List sources = getWork().getNbaSources();		
		for (int i = 0; i < sources.size(); i++) {
			NbaSource source = (NbaSource) sources.get(i);
			if (NbaConstants.A_ST_CHANGE_FORM.equals(source.getSource().getSourceType())) {				
				return source;
			}
		}
		return null;
	}

	/**
	 * This method updates payment workitem and source LOBS
	 * @param paymentLOBs the payment workitem LOBs
	 * @param cntChgLob the contract change work item LOBs
	 * @param sourceLob the payment source LOBs
	 * @throws NbaBaseException
	 */
	protected void updatePaymentLOBs(NbaLob paymentLOBs, NbaLob cntChgLob, NbaLob sourceLob) throws NbaBaseException {
		//update payment source LOBs
		NbaLob caseLob = getCaseWork().getNbaLob(); //CR57873
		if(isStandalone()){
			sourceLob.setInforcePaymentManInd("false");
		}else{
			sourceLob.setInforcePaymentManInd("true");
		}
		
		//Copy LOBS from source to payment workitem
		if(isStandalone()){ 
			paymentLOBs.setInforcePaymentType((int)NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN);	
		}else{ //for wrapperd payment type will remain as indexed
			paymentLOBs.setInforcePaymentType(sourceLob.getInforcePaymentType());
		}
		paymentLOBs.setCheckAmount(sourceLob.getCheckAmount());
		paymentLOBs.setCheckNumber(sourceLob.getCheckNumber());
		paymentLOBs.setCompany(sourceLob.getCompany());
		paymentLOBs.setInforcePaymentManInd(sourceLob.getInforcePaymentManInd());
		//begin SPR2248
		if (isStandalone() && getMergedContract().getPolicy().hasReinstatementDate()) {
			paymentLOBs.setInforcePaymentDate(getMergedContract().getPolicy().getReinstatementDate());	//Set to Due Date from Reinstatement View
		} else {
			paymentLOBs.setInforcePaymentDate(sourceLob.getInforcePaymentDate());
		}
		//end SPR2248
		paymentLOBs.setPolicyNumber(sourceLob.getPolicyNumber());		

		//Copy LOBS from change workitem to payment workitem if not already set by view
		if(paymentLOBs.getIFRxDueDate() == null){
			paymentLOBs.setIFRxDueDate(cntChgLob.getIFRxDueDate());
		}
		if(paymentLOBs.getIFRxPaidDate() == null){
			paymentLOBs.setIFRxPaidDate(cntChgLob.getIFRxPaidDate());
		}
		//CR57873 - Begin
		String underwriterLOB=caseLob.getUndwrtQueue();
		String underwriterCMLOB=caseLob.getCaseManagerQueue();
		if (underwriterLOB != null &&  !EMPTY_LOB_STR.equals(underwriterLOB)){
			paymentLOBs.setUndwrtQueue(underwriterLOB);
		}
		if (underwriterCMLOB != null &&  !EMPTY_LOB_STR.equals(underwriterCMLOB)){
			paymentLOBs.setCaseManagerQueue(underwriterCMLOB);
		}
			
		//CR57873 - End
	}
	
	/**
	 * This method deletes the Lob's whose values reset on 
	 * holding from admin to simulate reverse record build.
	 * @param caseLob the NbaLob     
	 */
	//SPR2173 New Method	
	protected void deleteLOBs(NbaLob caseLob){
		caseLob.deleteAgentID();
		caseLob.deleteAgency();
		caseLob.deleteAppDate();
		caseLob.deleteCwaTotal();
		caseLob.deleteDOB();
		caseLob.deleteFaceAmount();
		caseLob.deleteFirstName();
		caseLob.deleteGender();
		caseLob.deleteInforcePaymentType();
		caseLob.deleteInvalidAgent();
		caseLob.deleteSsnTin();
		caseLob.deleteTaxIdType();
		caseLob.deletePrimaryInsIssAge();
		caseLob.deleteLastName();
		caseLob.deleteAnnualModlPrem();
		caseLob.deleteMiddleInitial();
		caseLob.deletePlan();
		caseLob.deletePendPaymentType();
		caseLob.deletePolicyNumber();
		//NBA130 CODE DELETED
		caseLob.deleteRiskClassificatn();
		caseLob.deleteCaseFinalDispstn();
		caseLob.deleteIssueDate();
		caseLob.deleteAppHoldSuspDate();
	}

	/**
	 * Update case LOBs from merged contract. It also set backend system (SYST) LOB if not present.
	 * @throws NbaBaseException  
	 */
	protected void updateLOBs() throws NbaBaseException {
		NbaLob caseLob = getCaseWork().getNbaLob();
		deleteLOBs(caseLob);//SPR2173
		NbaTXLife holdingInq = getMergedContract();
		
		//set LOBs
		caseLob.updateLobFromNbaTxLife(holdingInq);
		if (caseLob.getBackendSystem() == null
			|| caseLob.getBackendSystem().trim().length() == 0
			|| caseLob.getProductTypSubtyp() == null
			|| caseLob.getProductTypSubtyp().trim().length() == 0) {
				
			NbaTableAccessor table = new NbaTableAccessor();
			HashMap map = table.createDefaultHashMap(NbaTableAccessConstants.WILDCARD);
			map.put(NbaTableAccessConstants.C_COMPANY_CODE, caseLob.getCompany());
			map.put(NbaTableAccessConstants.C_COVERAGE_KEY, caseLob.getPlan());
			NbaPlansData planData = table.getPlanData(map);
			if (planData == null) {
				throw new NbaBaseException("Plan data is missing or invalid");
			}
			caseLob.setBackendSystem(planData.getSystemId());
			caseLob.setProductTypSubtyp(planData.getProductType());
		}
		NbaHolding holding = holdingInq.getNbaHolding();
		// SPR3290 code deleted
		caseLob.setAppDate(holding.getAppDate());
		if(holding.getAppState() != null) {//AXAL3.7.04
			caseLob.setAppState(String.valueOf(holding.getAppState()));	
		}
		if(isStandalone()){
			caseLob.setOperatingMode(NbaConstants.STANDALONE);
			caseLob.setCaseFinalDispstn(0);//ALS4153
		}else{
			caseLob.setOperatingMode(NbaConstants.WRAPPERED);
			if(holding.getUnderwritingStatus() > -1){
				caseLob.setCaseFinalDispstn((int)holding.getUnderwritingStatus());
			}
		}
		caseLob.setContractChgType(getWork().getNbaLob().getContractChgType());	
		caseLob.setPassedAutoUnd(false);
		caseLob.setFailedAutoUnd(false);
		//NBA130 code deleted
		//SPR2173 code deleted
		if (NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT == getCntChgType()) {
			caseLob.deleteInforcePaymentType();
		}
		//NBA130 code deleted
		//SPR2380 code deleted. Removed commented line.
     //Beging AXAL3.7.20
		NbaProcessStatusProvider nbaProcStatusProvider = getProcesStatusProvider(caseLob, holdingInq);//AXAL3.7.04
		if (nbaProcStatusProvider != null) {
			//Begin NBLXA-2343
			//String licenseCaseManagerLOB = nbaProcStatusProvider.getLicenseCaseMangerQueue();
			String licenseCaseManagerLOB = AxaUtils.getMiscCMAssignmentRules(nbaProcStatusProvider.getLicenseCaseMangerQueue());
			//End NBLXA-2343
			if (licenseCaseManagerLOB != null && !CONTRACT_DELIMITER.equalsIgnoreCase(licenseCaseManagerLOB)) {
				caseLob.setLicCaseMgrQueue(licenseCaseManagerLOB); //NBLXA-2343
			}
		}
		//End AXAL3.7.20
		caseLob.setRequiresUnderwriting(getWork().getNbaLob().getRequiresUnderwriting());//ALS5351
	}

	/**
	 * Update merged NbaTXLife for addional markup  
	 * @throws NbaBaseException  
	 */
	protected void updateContractTXLife() throws NbaBaseException {
		NbaTXLife holdingInq = getMergedContract();
		NbaHolding nbaHolding = holdingInq.getNbaHolding();
		Policy policy = nbaHolding.getPolicy();
		if(isStandalone()){
			if(policy.hasApplicationInfo()){
				//reinitialize RequestedPolDate 
				policy.getApplicationInfo().setRequestedPolDate(policy.getIssueDate());
			}
		}
	}
		
	/**
	 * Set case pass status. Status is retrieve from Auto Process Status VP/MS model.
	 * @throws NbaBaseException
	 */
	protected void setCaseStatus() throws NbaBaseException {
		// APSL3525(CHAUG005)
		NbaTXLife holdingInq = getMergedContract();
		NbaProcessStatusProvider provider = getProcesStatusProvider(getCaseWork().getNbaLob(),holdingInq);
		changeStatus(getCaseWork(), provider.getPassStatus());
	}	

	/**
	 * Update case and contract change work to the AWD
	 * @param error true if an error has occured 
	 * @throws NbaBaseException
	 */
	protected void updateAWD(boolean error) throws NbaBaseException {
		try {
			if (getCaseWork() != null) {
				if(!error){
					setCaseWork(updateWork(getUser(), getCaseWork()));  //NBA213
				}else{
					//NBA208-32
					getWork().getTransaction().setRelate(null);
					doUpdateWorkItem();				
				}
				unlockWork(getUser(), getCaseWork());  //NBA213
				
			} else {
				doUpdateWorkItem();
			}
		} catch (Exception re) {  //NBA213
			unlockWork(getUser(), getCaseWork());  //NBA213
			throw new NbaBaseException("Error during AWD update", re);
		}
	}

	/**
	 * Returns the case workitem
	 * @return the case workitem
	 */
	public NbaDst getCaseWork() {
		return caseWork;
	}

	/**
	 * Sets the case workitem
	 * @param dst the case work
	 */
	public void setCaseWork(NbaDst dst) {
		caseWork = dst;
	}

	/**
	 * Returns merged contract TXLife
	 * @return the merged contract
	 */
	public NbaTXLife getMergedContract() {
		return mergedContract;
	}

	/**
	 * Sets the merged TXLife contract 
	 * @param life the merged TXLife
	 */
	public void setMergedContract(NbaTXLife life) {
		mergedContract = life;
	}

	/**
	 * Returns true is case already exists in AWD
	 * @return true id case exists in AWD 
	 */
	public boolean isCaseExists() {
		return caseExists;
	}

	/**
	 * Sets the case exists indicator
	 * @param b true is case exists in AWD
	 */
	public void setCaseExists(boolean b) {
		caseExists = b;
	}

	/**
	 * Returns true if workitem operate in standalone mode
	 * @return true is workitem operate in standalone mode
	 */
	public boolean isStandalone() {
		return standalone;
	}

	/**
	 * Sets the standalone indicator
	 * @param b true if workitem operate in standalone mode
	 */
	public void setStandalone(boolean b) {
		standalone = b;
	}

	/**
	 * Returns contract change type
	 * @return the contract change type
	 */
	public long getCntChgType() {
		return cntChgType;
	}

	/**
	 * Sets contract change type
	 * @param l the contract change type
	 */
	public void setCntChgType(long l) {
		cntChgType = l;
	}

	/**
	 * Responsible to set UNDQ and CSMQ LOB on the work item.    
	 * @return NbaSource or null
	 */
	//	NBA251 new method
	protected void assignCaseQueues() throws NbaBaseException {
		//ALII1504 begin
		HashMap deOink = new HashMap();
		deOinkTermConvData(deOink, getCaseWork().getNbaLob()); //ALII1524
		boolean undReqd = false;
		boolean reassignment=false; //QC15916/APSL4389
		if("true".equalsIgnoreCase((String)deOink.get("A_UndRequired"))){
			undReqd = true;
		}//ALII1504 end
		NbaLob caseLob = getCaseWork().getNbaLob();
		if(NbaUtils.isTermConvOPAICase(nbaTxLife)){//ALII2022, QC12479
			if(getWork().getNbaLob().getRequiresUnderwriting() && NbaUtils.isBlankOrNull(caseLob.getUndwrtQueue()) )
				undReqd = true;
		}//ALII2022, QC12479
		caseLob.setContractChgType(Long.toString(getCntChgType()));
		//NbaProcessStatusProvider provider = new NbaProcessStatusProvider(user, getCaseWork()); //APSL4088
		//Begin QC15916/APSL4389,APSL4608
		if(!String.valueOf(NbaOliConstants.OLI_APPTYPE_TRIAL).equalsIgnoreCase(caseLob.getApplicationType())){
			String location = NbaUtils.getUWLocation(caseLob.getUndwrtQueue());
			if(NbaConstants.COMPANY_MLOA.equalsIgnoreCase(caseLob.getCompany()) && LOCATION_NY.equalsIgnoreCase(location)){
				reassignment=true;
			}
		}
		//End QC15916/APSL4389
		if (caseLob.getUndwrtQueue() == null || EMPTY_LOB_STR.equals(caseLob.getUndwrtQueue()) || reassignment) { //QC15916/APSL4389
			//Code deleted for Equitable Assignment - CR 57873
			//CR 57873 Begin
			AxaUWAssignmentEngineVO uwAssignment = new AxaUWAssignmentEngineVO();
			uwAssignment.setTxLife(nbaTxLife);
			uwAssignment.setNbaDst(getCaseWork());
			uwAssignment.setReassignment(true);
			uwAssignment.setCasemanagerRequired(false);
			uwAssignment.setUnderwriterRequired(undReqd);//ALII1504
			new AxaUnderwriterAssignmentEngine().execute(uwAssignment); 
			//CR 57873 End	
		}
		//caseLob.setPaidChgCMQueue(getEquitableQueue(provider.getAlternateStatus(),NbaLob.A_LOB_PAIDCHANGE_CM)); //ALS5703 //APSL4088	
		//setCaseStatusProvider(provider); //APSL4088
		
		//Begin APSL4088
		AxaUWAssignmentEngineVO pccmAssignment = new AxaUWAssignmentEngineVO();
		pccmAssignment.setTxLife(nbaTxLife);
		pccmAssignment.setNbaDst(getCaseWork());
		pccmAssignment.setPaidChangeCaseManagerRequired(true);
		new AxaUnderwriterAssignmentEngine().execute(pccmAssignment);		
		//End APSL4088
	}
	
	
	/**
	 * @return Returns the caseStatusProvider.
	 */
	//NBA251 new method
	public NbaProcessStatusProvider getCaseStatusProvider() {
		return caseStatusProvider;
	}
	/**
	 * @param caseStatusProvider The caseStatusProvider to set.
	 */
	//NBA251 new method
	public void setCaseStatusProvider(NbaProcessStatusProvider caseStatusProvider) {
		this.caseStatusProvider = caseStatusProvider;
	}

	/**
	 * This method search the AWD for worktype NBAPPLCTN with search LOB criteria. If
	 * a match is found in AWD. It will then retrieve matched case and its children.
	 * @param nbaUserVO the user value object
	 * @param searchLOB the search LOBs
	 * @param lock set true if want to retrieve locked case and childs
	 * @return the case if found on AWD 
	 * @throws NbaBaseException
	 */
	//Moved from NbeContractChangeUtils AXAL3.7.04
	public static NbaDst getCaseFromAWD(NbaUserVO nbaUserVO, NbaLob searchLOB, boolean lock) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchVO.setNbaLob(searchLOB);
		searchVO.setResultClassName("NbaSearchResultVO");
		searchVO = WorkflowServiceHelper.lookupWork(nbaUserVO, searchVO); //NBA213
		if (searchVO.isMaxResultsExceeded()) { //NBA146
			throw new NbaBaseException(NbaBaseException.SEARCH_RESULT_EXCEEDED_SIZE, NbaExceptionType.FATAL); //NBA146
		} //NBA146
		List searchResult = searchVO.getSearchResults();
		NbaDst dst = null;
		if (searchResult != null && searchResult.size() > 0) {
			NbaSearchResultVO resultVO = (NbaSearchResultVO) searchResult.get(0);
			String id = resultVO.getNbaLob().getKey();
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(id, true);
			//begin NBA208-4
			retOpt.requestTransactionAsChild();
			retOpt.requestSources();
			if (lock) {
				retOpt.setLockWorkItem();
				retOpt.setLockTransaction();
			}
			//end NBA208-4
			try {
				dst = WorkflowServiceHelper.retrieveWorkItem(nbaUserVO, retOpt); //NBA213
				//NBA208-4 code deleted
				NbaContractLock.requestLock(dst, nbaUserVO);
			} catch (NbaLockedException e) {
				if (dst != null) {
					try {
						NbaContractLock.removeLock(dst, nbaUserVO);
						WorkflowServiceHelper.unlockWork(nbaUserVO, dst); //NBA213
					} catch (Throwable t) {
						//possible error and ignore it
					}
				}
				throw e;
			}
			//AXAL3.7.04 Code deleted
		}
		return dst;
	}
	//ALS4153 Code Deleted
	
	//APSL3603(QC13073)
	public void setWorkStatus() throws NbaBaseException{
		NbaTXLife txLife = getMergedContract();		
		if(txLife != null){
			NbaProcessStatusProvider provider = new NbaProcessStatusProvider(user,getWork(),txLife);
			setWorkStatusProvider(provider);
		}		
	}

	/**
	 * @return the workStatusProvider
	 */
	//APSL3603(QC13073)
	public NbaProcessStatusProvider getWorkStatusProvider() {
		return workStatusProvider;
	}

	/**
	 * @param workStatusProvider the workStatusProvider to set
	 */
	//APSL3603(QC13073)
	public void setWorkStatusProvider(NbaProcessStatusProvider workStatusProvider) {
		this.workStatusProvider = workStatusProvider;
	}
	
	/**
	 * 
	 * This method creates a RelationshipCase Manager WI for RCM for reissue cases.
	 */
	//APSL4412 - New Method
	protected void createRelationshipCaseManagerTransaction() {
		try {
			if (!NbaUtils.isAdcApplication(getCaseWork())) {
				Map deOinkMap = new HashMap();
				deOinkMap.put("A_RelationCaseManagerTransaction", "true");
				if (getCaseWork() != null) {
					deOinkMap.put("A_CaseManagerQueueLOB", NbaUtils.isBlankOrNull(getCaseWork().getNbaLob().getCaseManagerQueue()) ? ""
							: getCaseWork().getNbaLob().getCaseManagerQueue());
					String[] workTypeAndStatus = getWorkTypeAndStatus(deOinkMap);
					if (workTypeAndStatus[0] != null && workTypeAndStatus[1] != null) {
						NbaTransaction aTransaction = getCaseWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]);
						if(getMergedContract() != null && getMergedContract().isPaidReIssue()){
							aTransaction.getNbaLob().setRouteReason(
									NbaUtils.getStatusTranslation(workTypeAndStatus[0], workTypeAndStatus[1]) + " - Paid Reissue");
						} else {
							aTransaction.getNbaLob().setRouteReason(
									NbaUtils.getStatusTranslation(workTypeAndStatus[0], workTypeAndStatus[1]) + " - Unpaid Reissue");
						}
						aTransaction.getNbaLob().setCaseManagerQueue(getCaseWork().getNbaLob().getCaseManagerQueue());
						aTransaction.getNbaLob().setFirstName(getCaseWork().getNbaLob().getFirstName());
						aTransaction.getNbaLob().setLastName(getCaseWork().getNbaLob().getLastName());
						aTransaction.getNbaLob().setSsnTin(getCaseWork().getNbaLob().getSsnTin());
						aTransaction.getNbaLob().setDOB(getCaseWork().getNbaLob().getDOB());
						aTransaction.getNbaLob().setAgentID(getCaseWork().getNbaLob().getAgentID());
						aTransaction.getNbaLob().setFaceAmount(getCaseWork().getNbaLob().getFaceAmount());
					}
				}
			}
		} catch (Exception e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getAWDFailStatus()));
		}
	}

	protected String[] getWorkTypeAndStatus(Map deOinkMap) {
		String[] result = new String[4];
		try {
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOinkMap);
			result[0] = provider.getWorkType();
			result[1] = provider.getInitialStatus();
			result[2] = provider.getWIAction();
			result[3] = provider.getWIPriority();
		} catch (NbaBaseException nbe) {
		}
		return result;
	}

}