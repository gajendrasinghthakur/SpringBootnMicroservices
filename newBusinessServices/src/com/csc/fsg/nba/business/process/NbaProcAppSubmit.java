package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group?.  The use,<BR>
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.business.rule.NbaDeterminePlan;
import com.csc.fsg.nba.business.uwAssignment.AxaUnderwriterAssignmentEngine;
import com.csc.fsg.nba.database.AxaRulesDataBaseAccessor;
import com.csc.fsg.nba.database.AxaWorkflowDetailsDatabaseAccessor;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaGroupPolicyAccessor;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaContractAccessException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.NbaSystemDataProcessor;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaCompanionCaseControlData;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.tableaccess.NbaFormsValidationData;
import com.csc.fsg.nba.tableaccess.NbaPartyData;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTable;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.AxaAssignmentRulesVO;
import com.csc.fsg.nba.vo.AxaGIAppSystemDataVO;
import com.csc.fsg.nba.vo.AxaUWAssignmentEngineVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaGroupPolicy;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vo.txlife.UserLoginNameAndUserPswd;
import com.csc.fsg.nba.vpms.CopyLobsTaskConstants;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;


/**
 * NbaProcAppSubmit is the class to process Cases found in NBAPPSUB queue.It:
 * - retrieves the child work items and sources
 * - assigns a default User ID if necesary
 * - assigns a policy number if necessary.
 * - sends the XML 103 message to the adaptor for processing. For duplicate Contract
 *   errors, it retries a user constant number of times with different Contract numbers.
 * - for each CWA source item attached to the case, it creates child Transactions 
 *   with LOB information from the Case and source item and attaches the source to
 *   the child transaction.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA004</td><td>Version 2</td><td>Automated Process Model Support for Work Items</td></tr>
 * <tr><td>NBA021</td><td>Version 2</td><td>Data Resolver</td></tr>
 * <tr><td>NBA009</td><td>Version 2</td><td>Cashiering</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA033</td><td>Version 3</td><td>Companion Case and HTML Indexing Views</td></tr>
 * <tr><td>NBA095</td><td>Version 4</td><td>Queues Accept Any Work Type</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR2034</td><td>Version 4</td><td>Inaccurate LOB fields after appsubmit</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
 * <tr><td>NBA107</td><td>Version 4</td><td>Vntg contract issue to admin</td></tr> 
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr> 
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit card payment and authorization</td></tr>
 * <tr><td>SPR1457</td><td>Version 5</td><td>Alternate/additional checkboxes in underwriter workbench and status page is not populated</td></tr>
 * <tr><td>NBA128</td><td>Version 5</td><td>Workflow changes project</td></tr>
 * <tr><td>NBA187</td><td>Version 6</td><td>Trial application</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3415</td><td>Version 7</td><td>Attachment Object for Pending nbA Contract Sometimes Not Created for Prior Insurance Display</td></tr>
 * <tr><td>SPR3573</td><td>Version 8</td><td>Credit Card information is not saved</td></tr>
 * <tr><td>AXAL3.7.34</td><td>AXA Life Phase 1</td><td>Contract Services</td></tr>
 * <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr>
 * <tr><td>ALS1388</td><td>AXA Life Phase 1</td><td>Data errors should not cause AppSubmit to Error Stop</td></tr>
 * <tr><td>NBA251</td><td>Version 8</td><td>nbA Case Manager and Companion Case Assignment</td></tr>
 * <tr><td>AXAL3.7.3M1</td><td>AXA Life Phase 1</td><td>Informal Implementation Miscellaneous Requirements Part 1</td></tr>
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td></tr>
 * <tr><td>ALS2204</td><td>AXA Life Phase 1</td><td>QC# 967 Ad Hoc:  Documents Received do not create Requirements</td></tr>
 * <tr><td>AXAL3.7.20R</td><td>AXA Life Phase 1</td><td>Replacement Workflow</td>
 * <tr><td>ALS2220</td><td>AXA Life Phase 1</td><td>QC# 1018  - Line 2 of the source tagline is missing when formal moves from nbA to AWD</td></tr>
 * <tr><td>ALS5900</td><td>AXA Life Phase 1</td><td>QC #4999 - 1 records in AWD and 2 in nbA - Defect for issue #208</td></tr>
 * <tr><td>P2AXAL026</td><td>AXA Life Phase 2</td><td>UnderWriting Requirements</td></tr>
 * <tr><td>APSL215</td><td>AXA Life Phase 1</td><td>CR49851 Underwriter Assignment Process</td><tr>
 * <tr><td>NBA300</td><td>AXA Life Phase 2</td><td>Term Conversion</td><tr>
 * <tr><td>P2AXAL039</td><td>AXA Life Phase 2</td><td>Reg60 Database Interface</td><tr>
 * <tr><td>SPR3614</td><td>AXA Life Phase 1</td><td>JVPMS Memory leak in Auto Contract Numbering logic</td></tr>
 * <tr><td>CR57950 and 57951</td><td>Version 8</td><td>Aggregate Contract - Pre-Sale/Reg60</td></tr>
 * <tr><td>SR494086.5</td><td>Discretionary</td><td>ADC Retrofit</td>
 * <tr><td>CR1343968</td><td>AXA Life Phase 2</td><td>Group Workfow and Miscellaneous</td></tr>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Analytics Full Implementation</td></tr>
 * <tr><td>APSL2735</td><td>Discretionary</td><td>Electronic Initial Premium</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * <tr><td>APSL3881</td><td>Discretionary</td><td>Follow the Sun</td></tr>
 * <tr><td>SR831136 APSL4088</td><td>Descretionary</td><td>PCCM Workflow</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 1
 */
public class NbaProcAppSubmit extends com.csc.fsg.nba.business.process.NbaAutomatedProcess {
	//SPR3703 code deleted
	/** The boolean variable to indicate whether the application is submitted or not */
	protected boolean submitted = false;

	/** The boolean variable to indicate whether the contract number is needed or not */
	protected boolean needContractNo = false;

	/** An integer representing number of tries */
	protected int tries = 0;

	/** An integer representing maximum number of tries */
	protected int maxtries = 0; //SPR2034

	//	SPR3573 code deleted

	//BEGIN : NBLXA 186 Term Processing Automate Kick Out rules
	protected String curpolNumber = null; //SPR2034
	
	protected boolean needAssignment = true; //SPR2034
	
	boolean resetTermExpInd=false;//NBLXA186- resetTermExpInd=true when base case is mloa , perm or term umbrella, 
								//resetTermExpind = true implies that  term express lob should set to off

	boolean channelConflict = false;
	double minTermUmbAmt = 2000001.0;
	protected static final String NO_LOGGER = "NbaProcAppSubmit could not get a logger from the factory.";
	//Begin:NBLXA1332
	boolean channelConflictComment=false;
	protected boolean companionCasesProcCalled = false;
	protected boolean noCompProcessingForIncCase = false;
	protected boolean concurrentCaseExists = false; 
	protected boolean commentAddedOnIncomingCase=false;//QC19131/NBLXA-1437
	//End NBLXA332
	//boolean showGeneralComment = true;//commented for QC18933/APSL5400
	//End : NBLXA 186 Term Processing Automate Kick Out rules
	//	
	/**
	 * NbaProcAppSubmit constructor comment.
	 */
	public NbaProcAppSubmit() {
		super();
	}

	/**
	 * Assign a policy number to the Case if one is not already present.
	 * @param work a NbaDst value object for which the process is to occur
	 * @throws NbaBaseException
	 */
	// SPR3290 - exception NbaBaseException no longer thrown
	//APSL4917 Refactored existing logic.
	protected void assignPolicyNumber() throws NbaBaseException { // AXAL3.7.34 added throws clause
		if (needContractNo) {
			// AXAL3.7.34 code deleted
			// NBA021 code deleted
			// Begin AXAL3.7.34
			String polNumber;
			// begin ALS1388
			// ALS5900 SELECT/UPDATE PolNumber to AuxilliaryDB //APSL4224 code deleted
			polNumber = selectPolNumberFromAuxilliaryDB(getWork().getID());
			if (NbaUtils.isBlankOrNull(polNumber)) {
				polNumber = generateContractNumber();//APSL4917
				updatePolNumberToAuxilliaryDB(polNumber, getWork().getID());
			}
			if (!NbaUtils.isBlankOrNull(polNumber)) {
				getXML103().getPolicy().setPolNumber(polNumber); // NBA187 //SPR3703
				getWorkLobs().setPolicyNumber(polNumber);//ALS2220
				updatePolNumberOnTransactions();
				curpolNumber = polNumber;// NBLXA 186 Term Processing Automate Kick Out rules
			} else { // ALS3918
				throw new NbaBaseException("Could not generate contract number ");// ALS3918
			}
			//End ALS1388
			// APSL4224 code deleted
			// begin NBA033
			// NBA095 code deleted
			NbaCompanionCaseControlData cccd = new NbaCompanionCaseControlData();
			NbaCompanionCaseVO ccVO = cccd.getNbaCompanionCaseVO(getWork().getID());
			if (ccVO != null) {
				ccVO.setActionUpdate();
				ccVO.setContractNumber(getXML103().getPolicy().getPolNumber()); // SPR3703
				cccd.update(ccVO);
			}
			// end NBA033
			// AXAL3.7.34 code deleted
		}
	}
	
	/**
	 * Generates a policy number to the Case if one is not already present.
	 * @param work a NbaDst value object for which the process is to occur
	 * @throws NbaBaseException
	 */
	// APSL4917 - New Method
	protected String generateContractNumber() throws NbaBaseException {
		String contractNumber = null;
		if (isReg60PreSale()) { // P2AXAL004 //NBA231 TODO remove isReg60PreSale eventually
			NbaOinkDataAccess aNbaOinkVO = new NbaOinkDataAccess(getWorkLobs()); // NBA021 NBA187
			AutoContractNumber autoContractNumbering = (AutoContractNumber) ServiceLocator.lookup(AutoContractNumber.AUTO_CONTRACT_NUMBERING);
			List inputParam = new ArrayList(2);
			inputParam.add(aNbaOinkVO);
			contractNumber = autoContractNumbering.generateContractNumber(inputParam);
		} else if (isRiskRighterCase()){//APL4878
			List sources = getWork().getSources();
			for (int i = 0; i < sources.size(); i++) {
				NbaSource aSource = (NbaSource) work.getNbaSources().get(i);
				if (aSource.getSource().getSourceType().equals(NbaConstants.A_WT_APPLICATION)) {
					contractNumber = aSource.getNbaLob().getPolicyNumber();
				}
			}
			//End APSL4878
		}else {
			try {// APSL4224
				contractNumber = NbaAutoContractNumber.getInstance().generateEIBContractNumber(getXML103(), getWork(), getUser());
			} catch (NbaBaseException ex) {
				if (!ex.isFatal()) {
					throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_WS, ex.getMessage());
				}
				throw ex;
			}
			// end APSL4224
		}
		return contractNumber;
	}

	//ALS5900 New Method, APSL2808 changed method visibility
	protected String selectPolNumberFromAuxilliaryDB(String workItemId) {
		String polNumber = null;
		try {
			polNumber = NbaSystemDataProcessor.selectPolNumberFromAuxilliaryDB(workItemId);
		} catch (NbaBaseException e) {
			e.printStackTrace();
		}
		return polNumber;
	}
	
	//ALS5900 New Method, APSL2808 changed method visibility
	protected void updatePolNumberToAuxilliaryDB(String polNumber, String workItemId) {
		try {
			NbaSystemDataProcessor.updatePolNumberToAuxilliaryDB(polNumber, workItemId);
		} catch (NbaBaseException e) {
			e.printStackTrace();
		}
	}
	
	//ALS2220 new method, APSL2808 changed method visibility
	protected void updatePolNumberOnTransactions() throws NbaBaseException {
		List transactions = getWork().getNbaTransactions();
		int transactionCount = transactions.size();
		for (int i = 0; i < transactionCount; i++) {
			NbaTransaction transaction = (NbaTransaction) transactions.get(i);
			transaction.getNbaLob().setPolicyNumber(getWorkLobs().getPolicyNumber()); //Update Pol number on all tranaction																													// transactions
			transaction.setUpdate();
		}
		List sources = getWork().getNbaSources();
		int sourceCount = sources.size();
		for (int i = 0; i < sourceCount; i++) {
			NbaSource source = (NbaSource) sources.get(i);
			source.getNbaLob().setPolicyNumber(getWorkLobs().getPolicyNumber()); //Update Pol number on all sources
			source.setUpdate();
		}
	}

	/**
	 * If the user login name is missing in the XML message, set the user
	 * id to the automated process user id.
	 * @throws NbaBaseException
	 */
	protected void assignUserID() throws NbaBaseException {
		UserLoginNameAndUserPswd aUserLogin = getXML103().getTXLife().getUserAuthRequestAndTXLifeRequest().getUserAuthRequest()
				.getUserLoginNameAndUserPswdOrUserSessionKey().getUserLoginNameAndUserPswd();
		if (aUserLogin.getUserLoginName().length() < 1) {
			aUserLogin.setUserLoginName(getUser().getUserID());
		}
	}

	/**
	 * Create a Work Item for each CWA source on the case. Move the CWA source to
	 * the Work Item. The back end system, company code and contract LOB fields
	 * are automatically copied from the Case when the transaction is created.
	 */
	//NBA115 new method
	//SPR3290 - exception NbaBaseException no longer thrown
	protected void createCWATransactions() {
		//begin NBA115
		try {
			processCCTransactions(); //SPR3573

			List allSources = getWork().getNbaSources();
			for (int i = 0; i < allSources.size(); i++) {
				NbaSource aSource = (NbaSource) allSources.get(i);
				if (!aSource.isCreditCard() && !aSource.isMiscMail() && !aSource.isApplication()) { //Handled by processCCTransactions() //SPR3573 //ALS2204 //ALS3319
					//Begin APSL2735
					Map deOinkMap = new HashMap();
					deOinkMap.put("A_SourceTypeLOB", aSource.getSource().getSourceType());
					deOinkMap.put("A_PaymentMoneySourceLOB",aSource.getNbaLob().getPaymentMoneySource());
					String[] workTypeAndStatus = getWorkTypeAndStatus(deOinkMap);
					//End APSL2735
					boolean createWI = true;
					boolean detachSource = true;
					if (workTypeAndStatus[0] != null) {
						//Begin APSL2735
						if (String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT).equalsIgnoreCase(aSource.getNbaLob().getPaymentMoneySource())) {
							createWI = false;
							if (NbaUtils.isAmountPaidWithApplication(getNbaTxLife())  || NbaUtils.isAdcApplication(getWork()) ) { //APSL4507
								createWI = true;
								createWI = true;
								detachSource = false;
							}
						} 
						if (createWI) {//End APSL2735
							NbaTransaction aTransaction;
							// Create a new transaction, set LOBs, and move the source to the transaction
							aTransaction = getWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]);
							aTransaction.increasePriority(workTypeAndStatus[2], workTypeAndStatus[3]); //NBA020
							aTransaction.getNbaLob().setCaseManagerQueue(getWork().getNbaLob().getCaseManagerQueue());//NBA251
							aTransaction.getNbaLob().setFormRecivedWithAppInd(aSource.getNbaLob().getFormRecivedWithAppInd());//ALS5276
							aTransaction.getNbaLob().setWorkFlowCaseId(aSource.getNbaLob().getWorkFlowCaseId());//APSL3460
							aTransaction.getNbaLob().setDisplayIconLob(getWork().getNbaLob().getDisplayIconLob());
							//begin NBA009
							//Begin APSL2735
							NbaSource newSource;
							if (detachSource) {
								newSource = getWork().getNbaCase().moveNbaSource(aTransaction, aSource);
							} else {
								newSource = aTransaction.addNbaSource(aSource);
								aTransaction.getNbaLob().setPaymentMoneySource(String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT));
								aTransaction.getNbaLob().setWorkSubType((int)NbaOliConstants.OLI_WORKSUBTYPE_ACH);//APSL3836
							}
							//End APSL2735
							setSourceLOBs(aTransaction, newSource);
							setCwaDateAndTime(newSource);//Added for APSL5164
							newSource.setUpdate();
							//end NBA009
						}
					} //SPR3573
				}
			}
		} catch (Exception e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getAWDFailStatus()));
		}
		//End NBA115
	}

	//SPR3573 code deleted

	/**
	 * Conditionally creates a MISCMAIL Work Item for each application source on the case. 
	 */
	//ALS3319 new method
	protected void createMiscMailTransationForAppl() {
		try {
			List allSources = getWork().getNbaSources();
			for (int i = 0; i < allSources.size(); i++) {
				NbaSource aSource = (NbaSource) allSources.get(i);
				NbaLob sourceLob = aSource.getNbaLob();
				if (aSource.isApplication() && (getWork().getNbaLob().getAppOriginType() == sourceLob.getAppOriginType())) { // Create MISCMAIL transaction for any NBAPPLCTN source //ALS4849
					String reqTypeLob = getRequirementTypeForSource(sourceLob);//ALS4760 ALNA84
					if (reqTypeLob != null) {
						String[] workTypeAndStatus = getWorkTypeAndStatus(aSource);
						if (workTypeAndStatus[0] != null) {
							NbaTransaction aTransaction;
							// Create a new transaction, set LOBs, and move the source to the transaction
							aTransaction = getWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]);
							aTransaction.increasePriority(workTypeAndStatus[2], workTypeAndStatus[3]);
							aTransaction.getNbaLob().setCaseManagerQueue(getWork().getNbaLob().getCaseManagerQueue());
							aTransaction.getNbaLob().setDisplayIconLob(getWork().getNbaLob().getDisplayIconLob());//APSL4992
							//ALS3483 begin
							WorkItemSource workItemSource = aSource.getSource();
							WorkItemSource newWorkItemSource = new WorkItemSource();
							newWorkItemSource.setCreate("Y");
							newWorkItemSource.setRelate("Y");
							newWorkItemSource.setLobData(workItemSource.getLobData());
							newWorkItemSource.setBusinessArea(workItemSource.getBusinessArea());
							newWorkItemSource.setSourceType(workItemSource.getSourceType());
							newWorkItemSource.setSystemName(workItemSource.getSystemName());
							List list = WorkflowServiceHelper.getBase64SourceImage(getUser(), aSource);
							if (list != null && list.size() > 0) {
								newWorkItemSource.setFormat("T");
								String guid = NbaUtils.getGUID();
								workItemSource.setSize(0);
								workItemSource.setPages(1);
								newWorkItemSource.setText(guid);
								newWorkItemSource.setFileName(null);
								newWorkItemSource.setSourceStream(list.get(0));
							}
							aTransaction.getSources().add(newWorkItemSource);
							//ALS3483 end
							List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2APPUB_CREATE_MISCMAIL_TASK);
							aSource.getNbaLob().copyLOBsTo(aTransaction.getNbaLob(), lobList);
							aTransaction.getNbaLob().setReqType(Integer.parseInt(reqTypeLob));
							aTransaction.getNbaLob().setReqReceiptDate(new Date());
							aTransaction.getNbaLob().setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date()));
							newWorkItemSource.setLobData(aTransaction.getNbaLob().convertToLobData());//ALS3483
						}
					}
				}
			}
		} catch (Exception e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getAWDFailStatus()));
		}
	}

	/**
	 * Checks whether the application state given in the source is same as the state value came out from database
	 * If both are not matching, the form number entered in indexing is not correct, then
	 * returns the requirement type to be generated.  
	 * @throws NbaBaseException
	 */
	//ALS3319 new method //ALS4760  signature changed //ALNA84 signature changed
	protected String getRequirementTypeForSource(NbaLob sourceLob)
			throws NbaBaseException {
		//Begin ALS4847 ALS4849 ALNA84
		
		NbaTableAccessor nta = new NbaTableAccessor();
		if (sourceLob.getFormNumber() == null || sourceLob.getAppState() == null || sourceLob.getApplicationType() == null || sourceLob.getCompany() == null) {
			return null;
		}
        Map caseData = new HashMap();
        String sDate = sourceLob.getAppDate() != null ? NbaUtils.getStringInUSFormatFromDate(sourceLob.getAppDate()) : NbaUtils
				.getStringInUSFormatFromDate(new Date());
		caseData.put(NbaTable.COMPANY_CODE, sourceLob.getCompany());
		caseData.put(NbaTable.APP_TYPE, sourceLob.getApplicationType());
		caseData.put(NbaTable.APPLICATION_STATE, sourceLob.getAppState());
		caseData.put(NbaTable.FORM_NUMBER, sourceLob.getFormNumber());
		caseData.put(NbaTable.APPLICATION_SIGNED_DATE, sDate);
		
		NbaFormsValidationData tableData = nta.getIncorrectFormNumberData(caseData);
		if (null != tableData) {
			return tableData.getRequirementType();
		}else if (!getNbaTxLife().isInformalApplication() && !NbaUtils.isAdcApplication(getWork()) && !String.valueOf(NbaOliConstants.OLI_APPTYPE_TRIAL).equals(sourceLob.getApplicationType())) { //SR494086.5 ADC Retrofit//APSL1464 
			return String.valueOf(NbaOliConstants.OLI_REQCODE_SIGNEDAPP);
		}
		//End ALS4849 ALS4847 ALNA84
		return null;
	}

	/**
	 * Creates a CWA work item for this banking object, which constitutes a
	 * credit card transaction without source.
	 */
	//NBA115 new method
	//SPR3573 changed method signature
	protected NbaTransaction createCreditCardCWATransaction(Banking banking, String[] workTypeAndStatus) throws NbaBaseException {

		NbaTransaction nbaTrans = null; //SPR3573
		BankingExtension bankingExt = NbaUtils.getFirstBankingExtension(banking); //SPR3573

		try {
			//SPR3573 code deleted
			if (workTypeAndStatus[0] != null) {
				nbaTrans = getWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]); //SPR3573
				nbaTrans.increasePriority(workTypeAndStatus[2], workTypeAndStatus[3]);
				NbaLob lob = nbaTrans.getNbaLob();
				Address address = getCreditCardAddress(bankingExt); //SPR3573
				if (address != null) {
					lob.setCCBillingAddr(address.getLine1());
					lob.setCCBillingCity(address.getCity());
					lob.setCCBillingState(address.getAddressStateTC());
					lob.setCCBillingZip(address.getZip());
					//TODO what about canadian zip
				}
				//SPR3573  code deleted
				lob.setCwaAmount(bankingExt.getPaymentChargeAmt());
				lob.setCwaDate(bankingExt.getBillControlEffDate());
				if (bankingExt.hasAccountHolderNameCC() && bankingExt.getAccountHolderNameCC().getAccountHolderNameCount() > 0) { //SPR3573
					lob.setCCBillingName(bankingExt.getAccountHolderNameCC().getAccountHolderNameAt(0));
				}
				//SPR3573 code deleted								
				lob.setCCExpDate(NbaUtils.formatToMMYYYY(banking.getCreditCardExpDate()));
				lob.setCCNumber(NbaBase64.encodeString(banking.getAccountNumber()));
				lob.setCCType(Long.toString(banking.getCreditCardType()));
				lob.setPendPaymentType((int) bankingExt.getPaymentType());
				lob.setCCTransactionId(NbaCreditCardData.createCCTransactionId());
				lob.setPaymentMoneySource(Long.toString(NbaOliConstants.NBA_ORIGINATION_PAYFORM_CREDCARD)); //NBA228
				lob.setCaseManagerQueue(getWork().getNbaLob().getCaseManagerQueue());//NBA251
			}
			return nbaTrans; //SPR3573
		} catch (Exception e) {
			throw new NbaBaseException("Error creating credit card CWA.", e);
		}

	}

	/**
	 * Returns the address associated with the credit card payment.
	 * @param BankingExtension
	 * @return Address
	 */
	//NBA115 new method
	protected Address getCreditCardAddress(BankingExtension bankingExt) throws NbaBaseException {
		NbaParty party = null;
		Address address = null;

		if (bankingExt != null) {
			if (bankingExt.hasAppliesToPartyID()) {
				party = getWork().getXML103Source().getParty(bankingExt.getAppliesToPartyID());
			}
			if (party != null && bankingExt.hasMailingAddressID()) {
				address = party.getAddressForId(bankingExt.getMailingAddressID());
			}
		}
		return address;
	}

	//SPR3573 code deleted

	/**
	 * - Perform XML markup processing to iniitialize values for the back end adaptor.
	 * - Assign a default User ID if necesary
	 * - Set the Risk LOB value
	 * - Assign a policy number if necessary.
	 * - Send the XML 103 message to the adaptor for processing. For duplicate Contract
	 *   error, retry a user constant number of times.
	 * - update the XML 103 source to reflect any changes
	 * - create CWA child transactions
	 * @throws NbaBaseException
	 */
	protected void doProcess() throws NbaBaseException { //AXAL3.7.34.  let it throw NbaBaseException and invoker can determine if we error stop
		NbaTXLife nbaTXLife = getXML103();//NBA139
		nbaTXLife.doXMLMarkUp();
		NbaLob aNbaLob = getWorkLobs(); //NBA187
		APSAWDInd = AxaUtils.getAPSAWDInd(work); //ALCP153, determine indicator before any relationship are broken

		//begin NBA139
		try {
			if (NbaConfiguration.getInstance().isGenericPlanImplementation() && !(NbaUtils.getGenericPlanOverrideInd(nbaTXLife.getPolicy()))
					&& (aNbaLob.getContractChgType() == null) && (aNbaLob.getAppOriginType() != 0)) {
				NbaDeterminePlan determinePlan = new NbaDeterminePlan();
				determinePlan.determinePlanCode(getWork(), nbaTXLife);
			}
		} catch (NbaVpmsException e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getVpmsErrorStatus()));
			return;
		}
		//Begin AXAL3.7.01
		NbaTableAccessor nta = new NbaTableAccessor();
		NbaPlansData nbaPlansData = nta.getPlanData(nta.setupTableMap(getWork()));
		String productType = nbaPlansData.getProductType();
		if (productType != null) {
			nbaTXLife.getPolicy().setProductType(productType);
		}
		//End AXAL3.7.01
		// end NBA139
		assignUserID();
		setOperatingModeLOB(); //NBA077
		setRiskLOB();
		setCaseCreateDate(aNbaLob, nbaTXLife); //P2AXAL039
		
		//begin SPR1457
		String companionType = aNbaLob.getCompanionType();
		if (companionType != null) {
			updateCompanionIndicator(companionType);
		}
		//end SPR1457
		if (aNbaLob.getPolicyNumber() == null || aNbaLob.getPolicyNumber().length() < 1) {
			setMaxtries(Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
					NbaConfigurationConstants.DUPLICATE_CONTRACT_RETRY))); //NBA027, ACN012, SPR2034
			needContractNo = true;
		}
		//NBLXA-2347 Begins
        Policy pol = nbaTXLife.getPolicy();
        if(pol != null ){
                ApplicationInfo app = pol.getApplicationInfo();
                if(app != null && app.hasSignedDate()) {
                       pol.setIssueDate(app.getSignedDate());
                }else {
                       pol.setIssueDate(new Date());
                }
        }
        //NBLXA-2347 Ends
        
		// Begin NBLXA-2580
		if (nbaTXLife != null) {
			NbaParty primaryParty = nbaTXLife.getPrimaryParty();
			if (primaryParty != null && primaryParty.isPerson()) {
				Person person = primaryParty.getPerson();
				if (person != null && aNbaLob != null && !NbaUtils.isBlankOrNull(aNbaLob.getMiddleInitial())) {
					person.setMiddleName(aNbaLob.getMiddleInitial());
					person.setActionUpdate();
				}
			}
		}
		// End NBLXA-2580

		//begin SPR2034 
		if (getMaxtries() < 1) {
			setMaxtries(1);
		}
		//AXAL3.7.34 code deleted
		assignPolicyNumber(); // Assign a policy number if necessary
		addReplacementInfo(nbaTXLife); //NBA231
		addCWAInd(getWork(), nbaTXLife); //ALS3347
		//processGiPolicies(nbaTXLife);//CR1343968 commented code for Start NBLXA-188
		setInitialReviewIndicator(nbaTXLife);//APSL4980
		
		//Begin NBLXA-2487
		assignLCMQueue(aNbaLob, nbaTXLife);//NBLXA-2328[NBLXA-2595]
		//End NBLXA-2487
		//APSL4412 CODE DELETED 
		if (getResult() == null) { // Submit the Application to the Back End System and handle the response
			//NBA050 BEGIN
			//rearrange primary insured name cases.
			//formatPrimaryPartyName(nbaTXLife); //SPR3415 ALS5525	
			setNbaTxLife(doContractInsert(nbaTXLife));//SPR2034//NBA115 //NBA139 SPR3573
			handleHostResponse();
			//NBA050 END
		}
		processExpressCommisionCall();
		//AXAL3.7.34 code deleted
		
		//call to assign Underwriter/Case Manager queues bypass for PreSale Cases
		//if (!isReg60PreSale()) {   //NBA231, Begin CR57950 and CR57951 code (if loop commented for this CR

		//APSL4224 code moved to other block
		//APSL4224 code moved to other block
		//Begin NBLXA186//Begin : Modified for NBLXA1332
		if (checkNewAppNoADC(nbaTXLife)) {
			if (getWorkLobs().getCompanionType() != null || !NbaConstants.NOT_A_COMPANION_CASE.equalsIgnoreCase(getWorkLobs().getCompanionType())) {
				NbaCompanionCaseRules rules = new NbaCompanionCaseRules(user, work);
				NbaCompanionCaseVO vo = null;
				List compCases = new ArrayList();// samidha
				compCases = rules.getCompanionCases();
				getLogger().logDebug("Companion cases size : "+compCases.size());
				if (compCases.size() > 1) {
					for (int i = compCases.size()-1; i>=0; i--) {//NBLXA1548-QC19319
						List concCases = new ArrayList();
						vo = (NbaCompanionCaseVO) compCases.get(i);
						String contractKey = vo.getNbaLob().getPolicyNumber();
						String companyCode = vo.getNbaLob().getCompany();
						String backendKey = vo.getNbaLob().getBackendSystem();
						NbaTXLife nbaTXLifeDatabase = null;
						if (contractKey != null && companyCode != null && backendKey != null) {
							nbaTXLifeDatabase = NbaUnderwritingRiskHelper
									.doHoldingInquiry(contractKey, companyCode, backendKey, getWork(), getUser());
							if (nbaTXLifeDatabase != null) {
								concCases = findConcurrentCases(nbaTXLifeDatabase);
							}
						}
						if (concCases.size() > 0) {  //NBLXA1548-QC19319
							compCases.remove(i);
						}
					}
					if (!noCompProcessingForIncCase) {
						if (compCases.size() > 1) {
							String underwriterLOBAsPerKickOutRules = companionCasesProcessing(compCases);
							getLogger().logDebug("UW for companion cases " + underwriterLOBAsPerKickOutRules);
						} else {
							noCompProcessingForIncCase = true;
						}
					}
				}
			}
			eligibleForTermXpressKickOut();
		}
		//End NBLXA186//End : Modified for NBLXA1332
		assignQueues(0.0);// NBA251 NBA231 //Modified for : NBLXA 186 Term Processing Automate Kick Out rules
		//}  //NBA231,End CR57950 and CR57951 code (if loop commented for this CR
		if (getResult() == null) {
			if (isFormalApplication()) { //NBA187
				updateWorkflowLobs(); //NBA187
			} //NBA187
			createCWATransactions(); // Create transactions for any CWA
			createReplacementTransaction(); //NBA231
			//Begin ALII537
			if(isTrialApplication()){
				createMiscMailTransationForAppl();//ALS3319
			}
			//End ALII537
			//ALS2584 Code Deleted
			createInitialReviewTransaction();// APSL3881
			createRelationshipCaseManagerTransaction();//APSL4412
			
			//START NBLXA-1632
			if(!isTrialApplication() && NbaUtils.isProductCOIL(getNbaTxLife().getProductCode())){ //NBLXA-2528
				createCSGReviewTransaction();
				updateCSGReviewIndicatorOnPolicy();
			}
			//END NBLXA-1632
			
			//START NBLXA-1823
			if(!isTrialApplication() && NbaUtils.isBusinessStrategy(getNbaTxLife())){
				createBusinessStrategyTransaction();
			}//END NBLXA-1823
			
			//Start NBLXA-188,NBLXA-1528
			if(isGIApplication(getNbaTxLife()) && !NbaUtils.isESLIProduct(getNbaTxLife()) && NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy()).getPrintTogetherIND()){
				Policy policy = getNbaTxLife().getPolicy();
				PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
				if(policyExt != null && policyExt.hasGIBatchID()){
					insertGISystemData(policy);//NBLXA-1680
				}
			}//End NBLXA-188
		}
	}
	
	//CR1343968 New Method
	/**
	 * @param nbaTXLife
	 * @throws NbaBaseException
	 * Inserts a record for policies with group offer numbers in Auxilary DB and sets ready to print as false
	 */
	private void processGiPolicies(NbaTXLife nbaTXLife) throws NbaBaseException {
		com.csc.fsg.nba.vo.txlife.Policy policy = nbaTXLife.getPolicy();
		PolicyExtension polExtn = NbaUtils.getFirstPolicyExtension(policy);
		if (polExtn != null && polExtn.getGuarIssOfferNumber() != null) {
			NbaGroupPolicy grouppolicy = new NbaGroupPolicy();
			grouppolicy.setGiOfferNumber(polExtn.getGuarIssOfferNumber());
			grouppolicy.setBackendKey(nbaTXLife.getOLifE().getSourceInfo().getFileControlID());
			grouppolicy.setContractKey(policy.getPolNumber());
			grouppolicy.setCompanyKey(policy.getCarrierCode());
			grouppolicy.setReadyForPrint(false);
			try {
				if (NbaGroupPolicyAccessor.selectByPolicyNumber(grouppolicy) == null) {
					NbaGroupPolicyAccessor.insertGroupRecord(grouppolicy);
				}
			} catch (NbaBaseException nbe) {
				getLogger().logException("During INSERT for NBA_GROUP_POLICY table", nbe);
				nbe.printStackTrace();
				throw nbe;
			}

		}
	}

	/**
	 * Perform the Application Submit business process: - Retrieve the child work items and sources. - Assign a default User ID if necesary - Assign a
	 * policy number if necessary. - Send the XML 103 message to the adaptor for processing. For duplicate Contract error, retry a user constant number of
	 * times. - update any CWA child transactions with LOB information from the Case
	 * 
	 * @param user
	 *            the NbaUser for whom the process is being executed
	 * @param work
	 *            a NbaDst value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// NBA095 BEGIN
		if (!isCorrectQueue(user, work)) {
			return getResult();
		}
		// NBA095 END
		if (!initializeFields(user, work)) {
			return getResult(); //NBA050
		}
		if (getResult() == null) {
			if ((NbaUtils.isBlankOrNull(getWorkLobs().getPolicyNumber()))) {// APSL4224
				doProcess();
			//begin APSL4224
			} else {
				setNbaTxLife(doHoldingInquiry());
				initializeStatusFields();
			}
			//processExpressCommisionCall();
			//end  APSL4224
		}
				
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		changeStatus(getResult().getStatus());
		// Update the Work Item with it's new status and update the work item in AWD
		doUpdateWorkItem();
		//NBA020 code deleted
		// Return the result
		return getResult();
	}
	
	/**
	 * Set Reg60 Review flag if case is determined to be a Reg60 case
	 * 
	 * @param NbaTXLife
	 */
	//NBA231 new method
	protected void addReplacementInfo(NbaTXLife nbaTXLife) {
		ApplicationInfo appInfo = nbaTXLife.getPolicy().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);

		if (null == appInfoExt) {
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
			appInfo.addOLifEExtension(olifeExt);
			olifeExt.getApplicationInfoExtension().setActionAdd();
			appInfoExt = olifeExt.getApplicationInfoExtension();
		}

		if (isReg60PreSale()) {
			setPreSaleInfo(nbaTXLife);
		} else if (isReg60(nbaTXLife)) {
			appInfoExt.setReg60Review(1); //Pending
		}
		appInfoExt.setActionUpdate();
	}

	//NBA231 new method
	protected boolean isReg60PreSale() {
		try {
			if (getWork().getNbaLob().getAppOriginType() == NbaOliConstants.OLI_APPORIGIN_REPLACEMENT) {//ALII1206
				return true;
			}
		} catch (Exception e) {

		}
		return false;

	}

	//NBA300 new method
	//P2AXAL040 modified for AXA
	//CR57950 and CR57951 NBA300 method moved to NbaUtils
	
		
	//NBA231 new method
	protected void setPreSaleInfo(NbaTXLife nbaTXLife103) {
		String polNumber = nbaTXLife103.getPolicy().getPolNumber();
		ApplicationInfo applInfo = nbaTXLife103.getPolicy().getApplicationInfo();
		applInfo.setTrackingID(polNumber);
		applInfo.setApplicationType(NbaOliConstants.OLI_APPTYPE_PRESALE);
		applInfo.setFormalAppInd(false);
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applInfo);
		appInfoExt.setReg60PSDecision(NbaOliConstants.NBA_REG60SPDECISION_PENDING);
		appInfoExt.setReg60Review(NbaOliConstants.NBA_REG60REVIEW_PENDING);
	}

	/**
	 * If ReplacementInd is set and Policy Jurisdiction is New York (tc 37) return true
	 * 
	 * @param NbaTXLife
	 * @result boolean
	 */
	protected boolean isReg60(NbaTXLife nbaTXLife) {
		if (nbaTXLife.getPolicy().getApplicationInfo().getReplacementInd()
				&& nbaTXLife.getPolicy().getApplicationInfo().getApplicationJurisdiction() == NbaOliConstants.OLI_USA_NY) {
			return true;
		}
		return false;
	}

	/*
	 * This method loops through all MiscMail work items to determine if a Replacement Work Item should be created. If no MiscMail work items exist,
	 * it will invoke VPMS one additional time to see if Replacement Work item should be created based off Policy info.
	 */
	protected void createReplacementTransaction() {

		try {
			Map deOinkMap = new HashMap();
			//deOinkMap.put("A_FormType","1");
			deOinkFormType(deOinkMap); //NBA231
			List allSources = getWork().getNbaSources();
			int size = allSources.size();
			NbaTransaction aTransaction = null;
			for (int i = 0; i < size; i++) {
				NbaSource aSource = (NbaSource) allSources.get(i);
				if (aSource.isMiscMail()) {
					deOinkMap.put("A_ReplacementTransaction", "true");
					deOinkMap.put("A_SourceTypeLOB", aSource.getSourceType());
					if (null != aSource.getNbaLob().getFormNumber()) {
						deOinkMap.put("A_FormNumberLOB", aSource.getNbaLob().getFormNumber());
					} else {
						deOinkMap.put("A_FormNumberLOB", "");
					}

					String[] workTypeAndStatus = getWorkTypeAndStatus(deOinkMap);
					if (workTypeAndStatus[0] != null) {

						// Create a new transaction, set LOBs, and move the source to the transaction
						aTransaction = getWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]);
						aTransaction.increasePriority(workTypeAndStatus[2], workTypeAndStatus[3]);
						aTransaction.getNbaLob().setDisplayIconLob(getWork().getNbaLob().getDisplayIconLob());//APSL4992
						break; //once we have a replacement work item, break
					}
				}

			}
			if (null == aTransaction) { //No MiscMail work items so make one call to see if ReplacementInd on Application requires a new replacement
				// work item
				deOinkMap.put("A_ReplacementTransaction", "true");
				deOinkMap.put("A_SourceTypeLOB", "NBMISCMAIL");
				String[] workTypeAndStatus = getWorkTypeAndStatus(deOinkMap);
				if (workTypeAndStatus[0] != null) {

					// Create a new transaction, set LOBs, and move the source to the transaction
					aTransaction = getWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]);
					aTransaction.increasePriority(workTypeAndStatus[2], workTypeAndStatus[3]);
				}
			}
			//if a transaction was created, copy LOBs
			if (null != aTransaction) {
				copyReplacementLOBs(aTransaction);
			}
		} catch (Exception e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getAWDFailStatus()));
		}
		//End NBA115
	}

	protected void copyReplacementLOBs(NbaTransaction aTransaction) {
		try {
			NbaLob workLobs = getWork().getNbaLob();
			NbaLob tempLobs = aTransaction.getNbaLob();
			tempLobs.setAppOriginType(workLobs.getAppOriginType());
			tempLobs.setAppState(workLobs.getAppState());
			tempLobs.setReplacementIndicator(workLobs.getReplacementIndicator());
			tempLobs.setDisplayIconLob(workLobs.getDisplayIconLob());
		} catch (NbaBaseException nbe) {
			getLogger().logException(nbe);
		}
	}

	//SPR3703 code deleted

	/**
	 * Return the Work Type and Status for the new transaction.
	 * @return String of work type and status
	 */
	protected String[] getWorkTypeAndStatus(NbaSource aSource) {
		String[] result = new String[4]; //NBA020
		// NBA004 begin
		try {
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), aSource.getSource().getSourceType()); //NBA020
			result[0] = provider.getWorkType();
			result[1] = provider.getInitialStatus();
			result[2] = provider.getWIAction(); //NBA020
			result[3] = provider.getWIPriority(); //NBA020
		} catch (NbaBaseException nbe) {
		}
		// NBA004 end
		// NBA004 deleted code
		return result;
	}

	/**
	 * Return the Work Type and Status for the new transaction.
	 * @param Map of deOink variables
	 * @return String of work type and status
	 */
	//NBA231 new method
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

	/**
	 * Return the Work Type and Status for a new transaction.
	 * @return String of work type and status
	 * @see createCreditCardCWATransactions
	 */
	//NBA115 new method
	protected String[] getWorkTypeAndStatus(String aSourceType) {
		String[] result = new String[4];
		try {
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), aSourceType);
			result[0] = provider.getWorkType();
			result[1] = provider.getInitialStatus();
			result[2] = provider.getWIAction();
			result[3] = provider.getWIPriority();
		} catch (NbaBaseException nbe) {
		}
		return result;
	}

	/**
	 * Handle the TXLife response from the Back End Adaptor.
	 * Override of superclass method to allow for duplicate contract number handling.
	 * @param aTXLifeResponse the TXLife response
	 */
	//NBA050 Modified method signature
	public void handleHostResponse() {
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify allResponses = getNbaTxLife().getTXLife()
				.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify(); //NBA050 SPR2034
		List responses = allResponses.getTXLifeResponse();
		TXLifeResponse theResponse = (TXLifeResponse) responses.get(0);
		TransResult aTransResult = theResponse.getTransResult();
		long resultCode = aTransResult.getResultCode();
		if (resultCode > 1) {
			if (getTries() >= getMaxtries() || !isDuplicatePolicyError(aTransResult.getResultInfo())) { //SPR2034
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Back End Processing failed", getHostFailStatus()));
				ArrayList errors = new ArrayList();
				for (int i = 0; i < aTransResult.getResultInfoCount(); i++) {
					errors.add(aTransResult.getResultInfoAt(i).getResultInfoDesc());
				}
				addComments(errors);
				submitted = true;
			} else {
				submitted = false;
			}
		} else {
			submitted = true;
		}
	}

	/**
	 * Retrieve and lock the child transactions and sources.
	 * Perform default initialization and initialize miscellaneous fields.
	 * @param newUser the AWD User for the process
	 * @param newWork the NbaDst value object to be processed
	 * @return a boolean value indicating the status of initialization of fields
	 * @throws NbaBaseException, NbaNetServerDataNotFoundException, NbaNetServerException
	 */
	protected boolean initializeFields(NbaUserVO newUser, NbaDst newWork) throws NbaBaseException, NbaNetServerDataNotFoundException,
			NbaNetServerException {
		boolean initializationSuccessful = true;
		NbaDst expandedWork;
		//NBA213 deleted code
		// Retrieve and lock the child transactions and sources.
		NbaAwdRetrieveOptionsVO aNbaAwdRetrieveOptionsVO = new NbaAwdRetrieveOptionsVO();
		aNbaAwdRetrieveOptionsVO.setWorkItem(newWork.getID(), true);
		aNbaAwdRetrieveOptionsVO.requestSources();
		aNbaAwdRetrieveOptionsVO.requestTransactionAsChild();
		aNbaAwdRetrieveOptionsVO.setLockTransaction();
		expandedWork = retrieveWorkItem(newUser, aNbaAwdRetrieveOptionsVO); //NBA213
		//NBA213 deleted code
		// Now continue with the expanded NbaDst
		initializationSuccessful = super.initialize(newUser, expandedWork);
		setStatusProvider(new NbaProcessStatusProvider(user, expandedWork, getDeOinkMapForSources(expandedWork))); //NBA128
		//SPR3703 Code deleted
		return initializationSuccessful;
	}

	/**
	 * Determine if a duplicate policy error was returned.
	 * @param resultInfo the TXLife response resultInfo
	 * @return boolean value representing duplicate policy error
	 */
	protected boolean isDuplicatePolicyError(ArrayList resultInfo) {
		for (int i = 0; i < resultInfo.size(); i++) {
			ResultInfo aResultInfo = (ResultInfo) resultInfo.get(i);
			String message = aResultInfo.getResultInfoDesc();
			if (message.indexOf("CK0015") != -1 || message.indexOf("B517") != -1 || message.indexOf("NBA0001") != -1) { //NBA050
				return true;
			}
		}
		return false;
	}

	//SPR3703 code deleted

	/**
	 * Set the Risk/Medical date LOB field to true if any Party
	 * has Risk or medical data.
	 * @throws NbaBaseException
	 */
	protected void setRiskLOB() throws NbaBaseException {
		OLifE olife = getXML103().getOLifE();
		boolean hasRisk = false;
		if (olife != null) {
			for (int index = 0; index < olife.getPartyCount(); index++) {
				Party party = olife.getPartyAt(index);
				if (party.hasRisk()) {
					hasRisk = true;
					break;
				}
			}
		}
		getWorkLobs().setRiskMedData(hasRisk); //NBA187
	}

	/**
	 * Update the LOBs in the child CWA transactions from the LOBs in the source.
	 * @param Transaction object
	 * @param Source object
	 * 
	 */
	// New NBA009
	//SPR3573 add throws clause
	protected void setSourceLOBs(NbaTransaction aTransaction, NbaSource aSource) throws NbaBaseException {
		NbaLob sourceLOBs = aSource.getNbaLob();
		NbaLob tranLOBs = aTransaction.getNbaLob();

		//ALS5026 removed 
		sourceLOBs.setPolicyNumber(tranLOBs.getPolicyNumber()); //APSL5055
		//begin SPR3573
		if (aSource.isCreditCard()) {
			sourceLOBs.setCCBillingAddr(tranLOBs.getCCBillingAddr());
			sourceLOBs.setCCBillingCity(tranLOBs.getCCBillingCity());
			sourceLOBs.setCCBillingState(tranLOBs.getCCBillingState());
			sourceLOBs.setCCBillingZip(tranLOBs.getCCBillingZip());
			sourceLOBs.setCwaAmount(tranLOBs.getCwaAmount());
			sourceLOBs.setCwaDate(tranLOBs.getCwaDate());
			sourceLOBs.setCCBillingName(tranLOBs.getCCBillingName());
			sourceLOBs.setCCExpDate(tranLOBs.getCCExpDate());
			sourceLOBs.setCCNumber(tranLOBs.getCCNumber());
			sourceLOBs.setCCType(tranLOBs.getCCType());
			sourceLOBs.setPendPaymentType(tranLOBs.getPendPaymentType());
			sourceLOBs.setCCTransactionId(tranLOBs.getCCTransactionId());
		}
		//end SPR3573  

	}

	/**
	 * Set the number of attempts to submit the contract
	 * @param tries
	 */
	// SPR2034 new method
	protected void setTries(int tries) {
		this.tries = tries;
	}

	/**
	 * Return the number of attempts to submit the contract 
	 */
	//SPR2034 new method
	protected int getTries() {
		return tries;
	}

	/**
	 * Increment the number of attempts to submit the contract
	 */
	//SPR2034 new method
	protected void incrementTries() {
		++tries;
	}

	/**
	 * Set the maximum number of attempts to submit the contract
	 * @param maxtries
	 */
	//SPR2034 new method
	protected void setMaxtries(int maxtries) {
		this.maxtries = maxtries;
	}

	/**
	 * Return the maximum number of attempts to submit the contract 
	 */
	//SPR2034 new method
	protected int getMaxtries() {
		return maxtries;
	}

	/**
	 * This method sets the OperatingMode LOB field.
	 * @throws NbaBaseException
	 */
	//NBA077 New Method
	protected void setOperatingModeLOB() throws NbaBaseException {
		//NBA187 code deleted
		getWorkLobs().setOperatingMode(NbaServerUtility.getDataStore(getWorkLobs(), getUser())); //NBA187	
	}

	/**
	 * Update alternate or additional indicator on the XML103 source based on type set on companion type LOB
	 * @param companionType String value representing alternate or additional type.
	 * @throws NbaBaseException 
	 */
	//SPR1457 New Method
	protected void updateCompanionIndicator(String companionType) throws NbaBaseException {
		ApplicationInfo appInfo = getXML103().getPrimaryHolding().getPolicy().getApplicationInfo();
		if (appInfo == null) {
			appInfo = new ApplicationInfo();
			appInfo.setActionAdd();
		}
		if (NbaConstants.COMPANION_TYPE_ALTERNATE.equals(companionType)) {
			appInfo.setAlternateInd(true);
			appInfo.deleteAdditionalInd();
		} else if (NbaConstants.COMPANION_TYPE_ADDITIONAL.equals(companionType)) {
			appInfo.setAdditionalInd(true);
			appInfo.deleteAlternateInd();
		}
	}

	//NBA128 New Method
	protected Map getDeOinkMapForSources(NbaDst work) throws NbaBaseException{
		Map deOinkMap = new HashMap(5, 0.8f);
		List sourceList = work.getSources();
		int count = sourceList.size();
		deOinkMap.put("A_SourceTypeCount", Integer.toString(count));
		//Code deleted ALII181
		for (int i = 0; i < count; i++) {
			if (i == 0) {
				//NBA208-32
				deOinkMap.put("A_SourceTypeLOB", ((WorkItemSource) sourceList.get(i)).getSourceType());
			} else {
				//NBA208-32
				deOinkMap.put("A_SourceTypeLOB" + "[" + i + "]", ((WorkItemSource) sourceList.get(i)).getSourceType());
			}
		}
		return deOinkMap;
	}

	/**
	 * Returns true if this work item is a formal application.
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA187 New Method
	protected boolean isFormalApplication() throws NbaBaseException {
		NbaHolding nbaHolding = getXML103().getNbaHolding();
		ApplicationInfo appInfo = nbaHolding.getApplicationInfo();
		if (appInfo != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			if (appInfoExt != null && appInfoExt.getApplicationOrigin() == NbaOliConstants.OLI_APPORIGIN_TRIAL) {
				return nbaHolding.getFormalAppInd();
			}
		}
		return false;
	}
	
	/**
	 * Returns true if this application is a trial application.
	 * @return
	 * @throws NbaBaseException
	 */
	//ALII537 New Method
	protected boolean isTrialApplication() throws NbaBaseException {
		NbaHolding nbaHolding = getXML103().getNbaHolding();
		ApplicationInfo appInfo = nbaHolding.getApplicationInfo();
		if (appInfo != null && appInfo.hasApplicationType()) {
			return appInfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_TRIAL
			|| appInfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_PRESALE;
		}
		return false;
	}
	/**
	 * Returns true if this application is a GI application.
	 * @return
	 * @throws NbaBaseException
	 */
	//NBLXA-188 New Method
	protected boolean isGIApplication(NbaTXLife nbaTxLife) throws NbaBaseException {
		NbaHolding nbaHolding = nbaTxLife.getNbaHolding();
		ApplicationInfo appInfo = nbaHolding.getApplicationInfo();
		if (appInfo != null && appInfo.hasApplicationType()) {
			return appInfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_GROUPAPP;
		}
		return false;
	}

	/**
	 * Updates the transactions LOBs that might have changed for a trial application. 
	 * @throws NbaNetServerDataNotFoundException
	 */
	//NBA187 New Method
	protected void updateWorkflowLobs() throws NbaNetServerDataNotFoundException {
		List transactions = getWork().getNbaTransactions();
		int count = transactions.size();
		for (int i = 0; i < count; i++) {
			NbaTransaction trx = (NbaTransaction) transactions.get(i);
			setFormalLobs(trx);
			trx.setUpdate();
		}
	}

	/**
	 * Format Primary party name such that first letter on each word is in capital letters and rest are small cases.
	 * @param aNbaTXLife
	 */
	//SPR3415 New Method
	protected void formatPrimaryPartyName(NbaTXLife aNbaTXLife) {
		NbaParty party = aNbaTXLife.getPrimaryParty();
		if (party.isPerson()) {
			Person person = party.getPerson();
			person.setFirstName(NbaUtils.convertStringInProperCase(person.getFirstName()));
			person.setMiddleName(NbaUtils.convertStringInProperCase(person.getMiddleName()));
			person.setLastName(NbaUtils.convertStringInProperCase(person.getLastName()));
			person.setActionUpdate();
		}
	}

	/**
	 * Create a CWA work item for a credit card Payments if a Banking object is present with a BankingExtension.CreditCardChargeUse of  1000500002
	 * (Credit Card Payment).  Set the CWA work item LOB credit card values from the Banking object. If a credit card Source is attached to the Case, move
	 * it from the Case to the CWA work item and update its LOB credit card values.
	 */
	//SPR3573 new method
	protected void processCCTransactions() throws NbaBaseException {
		Banking banking = getWork().getXML103Source().getBankingForCreditCardPayment();
		if (banking != null) {
			NbaSource nbaSource = getCreditCardSource();
			String[] workTypeAndStatus;
			if (nbaSource == null) {
				workTypeAndStatus = getWorkTypeAndStatus(NbaConstants.A_ST_CC_NO_SOURCE);
			} else {
				workTypeAndStatus = getWorkTypeAndStatus(nbaSource);
			}
			NbaTransaction nbaTransaction = createCreditCardCWATransaction(banking, workTypeAndStatus);
			nbaTransaction.getNbaLob().setFormRecivedWithAppInd(nbaSource.getNbaLob().getFormRecivedWithAppInd());//ALS5276 
			nbaTransaction.getNbaLob().setDisplayIconLob(getWork().getNbaLob().getDisplayIconLob());//APSL4992
			if (nbaSource != null) {
				setSourceLOBs(nbaTransaction, nbaSource);
				try {
					getWork().getNbaCase().moveNbaSource(nbaTransaction, nbaSource);
				} catch (Exception e) {
					throw new NbaBaseException("Error creating credit card CWA.", e);
				}
			}
		}
	}

	/**
	 * Return the credit card source (NBPAYCC.) for the Case if present. Otherwise return null.
	 * @return NbaSource or null
	 */
	//	SPR3573 new method
	protected NbaSource getCreditCardSource() {
		List allSources = getWork().getNbaSources();
		for (int i = 0; i < allSources.size(); i++) {
			NbaSource aSource = (NbaSource) allSources.get(i);
			if (aSource.isCreditCard()) {
				return aSource;
			}
		}
		return null;
	}

	/** 
	 * Responsible to check if UNDQ and CSMQ LOBs are already set on the case or not,
	 * calls assignUWCMQueue method if any of the LOB is not set.
	 * @return NbaSource or null
	 */
	//NBA251 new method
	protected void assignQueues(Double totalFaceAmt) throws NbaBaseException { //Modified for NBLXA 186 Term Processing Automate Kick Out rules
		boolean isUndqLOBPresent = false;
		boolean isCsmqLOBPresent = false;
		NbaLob caseLob = getWork().getNbaLob();//Updated LOBs
		if (caseLob.getUndwrtQueue() != null) {
			isUndqLOBPresent = true;
		}
		//NBLXA-2328[NBLXA-2595] Code Deleted
		//AXAL3.7.20R Code Deleted
		assignUWCMQueue(isUndqLOBPresent, isCsmqLOBPresent, caseLob,totalFaceAmt);//Modified for NBLXA 186 Term Processing Automate Kick Out rules
		//AXAL3.7.20R Code Deleted
	}

	/**
	 * Responsible to set UNDQ and CSMQ LOB on the work item, only if already not assigned. If the case is a companion case 
	 * then it copies the LOBs from companion case, otherwise is uses VP/MS autoprocessstatus model for Underwriter/Case Manager
	 * queues,passes them to getEquitableUWQueue and getEquitableCMQueue methods to determine which Underwriter/Case Manager 
	 * queues are having less load to assign this case.   
	 * @param isUndqLOBPresent
	 * @param isCsmqLOBPresent
	 * @param caseLob
	 * @throws NbaBaseException
	 */
	//NBA251 new method
	//AXAL3.7.20  isLCsmqLOBPresent  method parameter added
	protected void assignUWCMQueue(boolean isUndqLOBPresent, boolean isCsmqLOBPresent, NbaLob caseLob, Double totalFaceAmt) //Modified for NBLXA 186 Term Processing Automate Kick Out rules //NBLXA-2328[NBLXA-2595]
			throws NbaBaseException {		
		NbaTXLife caseTxLife= doHoldingInquiry(NbaConstants.READ, getUser().getUserID()); //AXAL3.7.3M1, ALS2597 ,CR57950 and CR57951
		//Begin for NBLXA 186 Term Processing Automate Kick Out rules
		if (totalFaceAmt > 0.0) {
			caseTxLife.getLife().setFaceAmt(totalFaceAmt);
		}
		//END : NBLXA 186 Term Processing Automate Kick Out rules
		//ALPC153 Code Deleted
		NbaProcessStatusProvider statusProvider = getProcesStatusProvider(caseLob, caseTxLife); //AXAL3.7.3M1, CR57950 and CR57951
		//Begin NBLXA-2343
		String replCMQueueList = AxaUtils.getMiscCMAssignmentRules(statusProvider.getReplCMQueue());
		if(!NbaUtils.isBlankOrNull(replCMQueueList)){
			caseLob.setReplCMQueue(getEquitableQueue(replCMQueueList,NbaLob.A_LOB_RPCM_QUEUE)); //CR57950 and CR57951
		}
		//End NBLXA-2343
		
		//Begin CR57950 and CR57951
        if(isReg60PreSale()){
        	return;
        }
        //End CR57950 and CR57951
		String underwriterLOB = null;
		String caseManagerLOB = null;
		String licenseCaseManagerLOB = null;//AXAL3.7.20
		boolean isMLOACompanyPresent = false;//QC15916/APSL4389
		//if any one of them are not assigned
		if (caseLob.getCompanionType() != null && !NbaConstants.NOT_A_COMPANION_CASE.equalsIgnoreCase(caseLob.getCompanionType()) && !noCompProcessingForIncCase) {//if not null and not of type "0" //Modified for NBLXA1332
			NbaCompanionCaseVO vo;
			NbaCompanionCaseRules rules = new NbaCompanionCaseRules(user, work);
			List compCases = rules.getCompanionCases();
			NbaLob companionLob = null;
			int size = compCases.size();
			for (int i = 0; i < size; i++) {
				vo = (NbaCompanionCaseVO) compCases.get(i);
				companionLob = vo.getNbaLob();
				if (caseLob.getPolicyNumber().equalsIgnoreCase(vo.getContractNumber())) {//when current work is retrieved 
					continue;
				}
				if (!companionCasesProcCalled  && companionLob != null && !NbaUtils.isBlankOrNull(companionLob.getUndwrtQueue()) && !isUndqLOBPresent) {
					underwriterLOB = companionLob.getUndwrtQueue();
					isUndqLOBPresent = true;
				}
				if (companionLob != null && !NbaUtils.isBlankOrNull(companionLob.getCaseManagerQueue()) && !isCsmqLOBPresent) {
					caseManagerLOB = companionLob.getCaseManagerQueue();
					isCsmqLOBPresent = true;
				}
				//Begin QC15916/APSL4389 //Modified for NBLXA1332 - added condition !comCalled
				if (!companionCasesProcCalled && companionLob != null && !NbaUtils.isBlankOrNull(companionLob.getCompany())
						&& NbaConstants.COMPANY_MLOA.equalsIgnoreCase(companionLob.getCompany()) && !isMLOACompanyPresent) {
					isMLOACompanyPresent = true;
				}
				//End QC15916/APSL4389
				//Begin AXAL3.7.20
				//NBLXA-2328[NBLXA-2595] Code Deleted
				if (isUndqLOBPresent && isCsmqLOBPresent) {//Both are assigned from companion cases
					break;
				}
				//End AXAL3.7.20
			}
		}

		//		 Code deleted ALS2597
		//moved code to top of the method CR57950 and CR57951
		//NbaTXLife caseTxLife = doHoldingInquiry(NbaConstants.READ, getUser().getUserID()); //AXAL3.7.3M1, ALS2597
		//ALPC153 Code Deleted
		//NbaProcessStatusProvider statusProvider = getProcesStatusProvider(caseLob, caseTxLife); //AXAL3.7.3M1
		//Move the initialization of statusprovider at the begining of the method for CR57950 and CR57951
		//ALPC153 Code Deleted
		//Begin AXAL3.7.3M1
		//Code deleted ALS2597
		//End AXAL3.7.3M1
		
		//CR57873 - Code for Equitable Assignment deleted
		
		//ALPC153 Code Deleted
		//Begin AXAL3.7.20
		//NBLXA-2328[NBLXA-2595] Code Deleted
		//End AXAL3.7.20
		getStatusProvider().setPassStatus(statusProvider.getPassStatus()); //AXAL3.7.3M1
		boolean isTermConvOPAI = NbaUtils.isTermConvOPAICase(caseTxLife); //NBA300 CR57950 and CR57951 //ALII875
		//ALPC153 Code Deleted
		//NBA300 - Do not set UNDQ and CSMQ for a Term Conversion case.
		if(!isTermConvOPAI){
			// Begin QC15916/APSL4389
			String uwLocation = NbaUtils.getUWLocation(underwriterLOB);
			if (underwriterLOB != null && !CONTRACT_DELIMITER.equalsIgnoreCase(underwriterLOB)) {
				if (String.valueOf(NbaOliConstants.OLI_APPTYPE_NEW).equalsIgnoreCase(caseLob.getApplicationType())
						&& NbaConstants.COMPANY_MLOA.equalsIgnoreCase(caseLob.getCompany())) {
					if (!NbaConstants.LOCATION_NY.equalsIgnoreCase(uwLocation)) {
						caseLob.setUndwrtQueue(underwriterLOB);
					}
				} else {
					caseLob.setUndwrtQueue(underwriterLOB);
				}

			}
			String uwcmLocation = NbaUtils.getUWLocation(caseManagerLOB);
			if (caseManagerLOB != null && !CONTRACT_DELIMITER.equalsIgnoreCase(caseManagerLOB)) {
				if (String.valueOf(NbaOliConstants.OLI_APPTYPE_NEW).equalsIgnoreCase(caseLob.getApplicationType())
						&& NbaConstants.COMPANY_MLOA.equalsIgnoreCase(caseLob.getCompany())) {
					if (!NbaConstants.LOCATION_NY.equalsIgnoreCase(uwcmLocation)) {
						caseLob.setCaseManagerQueue(caseManagerLOB);
					}
				} else {
					caseLob.setCaseManagerQueue(caseManagerLOB);
				}

			}
			//End QC15916/APSL4389
			//CR57873
			//QC# 9625- if() statement deleted
			AxaUWAssignmentEngineVO uwAssignment = new AxaUWAssignmentEngineVO();
			uwAssignment.setTxLife(caseTxLife);
			uwAssignment.setNbaDst(getWork());
			uwAssignment.setTermExpIndOff(resetTermExpInd);//NBLXA 186 Term Processing Automate Kick Out rules
			uwAssignment.setReassignment(false);
			uwAssignment.setMLOACompanyPresent(isMLOACompanyPresent);//QC15916/APSL4389
			new AxaUnderwriterAssignmentEngine().execute(uwAssignment);
		}
		//NBA300 - assign a Contract Change Case Manager Queue on Term Conversion and OPAI cases.
		if(isTermConvOPAI){
			//NBA300, P2AXAL040
			//caseLob.setPaidChgCMQueue(getEquitableQueue(statusProvider.getAlternateStatus(),NbaLob.A_LOB_PAIDCHANGE_CM)); //APSL4088
			//APSL4088			
			AxaUWAssignmentEngineVO pccmAssignment = new AxaUWAssignmentEngineVO();
			pccmAssignment.setTxLife(caseTxLife);
			pccmAssignment.setNbaDst(getWork());
			pccmAssignment.setPaidChangeCaseManagerRequired(true);
			new AxaUnderwriterAssignmentEngine().execute(pccmAssignment);
			NbaLob updatedCaseLob = getWork().getNbaLob(); //Retrieving updated NbaLob object for getting assigned PCCM value
			//End APSL4088
			
			getStatusProvider().setPassStatus(getProcesStatusProvider(updatedCaseLob, caseTxLife).getPassStatus()); //APSL4088
		}
		
		//NBLXA-2328[NBLXA-2595] Code Deleted
		//caseLob.setReg60CseMgrQueue(getEquitableQueue(statusProvider.getReg60CaseMangerQueue(), NbaLob.A_LOB_REG60_CM_QUEUE)); //AXAL3.7.20R,CR57950 and 57951
		//Begin AXAL3.7.3M1
		//Code deleted ALS2597
		//End AXAL3.7.3M1
	}

	//AXAL3.7.3M1 - Moved NBA251 code to the parent class
	//ALS3347
	public static void addCWAInd(NbaDst work, NbaTXLife nbaTXLife) {
		if (work != null && work.isCase()) {
			for (int i = 0; i < work.getNbaSources().size(); i++) {
				NbaSource aSource = (NbaSource) work.getNbaSources().get(i);
				if (aSource.isCwaCheck()) {
					ApplicationInfoExtension appExtn = NbaUtils.getFirstApplicationInfoExtension(nbaTXLife.getPolicy().getApplicationInfo());
					if (appExtn != null) {
						appExtn.setCWAInd(true);
						appExtn.setActionUpdate();
						break;
					}
				}
			} // end for
		}
	}
	
	/**
	 * To store the CRDA LOB on the nbA pending database in OLifE.Holding.Policy.OLifEExtension.PolicyExtension.CaseCreateDate tag
	 * @param caseLob
	 */
	//P2AXAL039
	public void setCaseCreateDate(NbaLob caseLob, NbaTXLife nbaTXLife){
		String createDate = caseLob.getCreateDate();
		PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy()); 
        if (null != polExt && createDate != null) {
        	polExt.setCaseCreateDate(NbaUtils.getDateFromStringInAWDFormat(createDate));
        	polExt.setActionUpdate();
        }
	}
	//APSL454 new method
	protected boolean isWholesale() throws NbaBaseException{
		if(getWork().getNbaLob().getDistChannel() == NbaOliConstants.OLI_DISTCHAN_6){
			return true;
		}
		return false;
	}

	//APSL4412 METHOD DELETED - doCIPInquiry()
	
	/*
	 * This method creates a initial Review WI for UWCM for formal cases.
	 */
	// APSL3881 New method
	protected void createInitialReviewTransaction() {
		try {
			if (!getNbaTxLife().isInformalApp() && !NbaUtils.isAdcApplication(getWork())) { // APSL4022
				Map deOinkMap = new HashMap();
				deOinkMap.put("A_InitialReviewTransaction", "true");
				deOinkMap.put("A_WORKTYPELOB","NBINIREV" );     //NBLXA-1658
				String[] workTypeAndStatus = getWorkTypeAndStatus(deOinkMap);
				if (workTypeAndStatus[0] != null) {
					NbaTransaction aTransaction;
					aTransaction = getWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]);
					aTransaction.getNbaLob().setDisplayIconLob(getWork().getNbaLob().getDisplayIconLob());
					
				}
			}
		} catch (Exception e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getAWDFailStatus()));
			
		}
	}
	/*
	 * This method creates a RelationshipCase Manager WI for RCM for formal cases.
	 */
	//  New Method APSL4412
	protected void createRelationshipCaseManagerTransaction() {
		try {
			if (!NbaUtils.isAdcApplication(getWork())) {
				Map deOinkMap = new HashMap();
				deOinkMap.put("A_RelationCaseManagerTransaction", "true");
				String[] workTypeAndStatus = getWorkTypeAndStatus(deOinkMap);
				if (workTypeAndStatus[0] != null) {
					NbaTransaction aTransaction;
					aTransaction = getWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]);
					aTransaction.getNbaLob().setRouteReason(NbaUtils.getStatusTranslation(workTypeAndStatus[0], workTypeAndStatus[1]) + " - New Case");
					aTransaction.getNbaLob().setCaseManagerQueue(getWork().getNbaLob().getCaseManagerQueue());
					aTransaction.getNbaLob().setFirstName(getWork().getNbaLob().getFirstName());
					aTransaction.getNbaLob().setLastName(getWork().getNbaLob().getLastName());
					aTransaction.getNbaLob().setSsnTin(getWork().getNbaLob().getSsnTin());
					aTransaction.getNbaLob().setDOB(getWork().getNbaLob().getDOB());
					aTransaction.getNbaLob().setAgentID(getWork().getNbaLob().getAgentID());
					aTransaction.getNbaLob().setFaceAmount(getWork().getNbaLob().getFaceAmount());
				}
				if(getNbaTxLife() != null && getNbaTxLife().getPrimaryHolding()!= null && getNbaTxLife().getPrimaryHolding().getPolicy()!= null){
					AxaWorkflowDetailsDatabaseAccessor.getInstance().updateWorkFlowDetails(getNbaTxLife().getPrimaryHolding().getPolicy().getPolNumber(), 1); //APSL4342 set value to 1 ? Active
				}
			}
		} catch (Exception e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getAWDFailStatus()));
		}
	}
	
	
	//APSL4224 New Method
	/**
	 *
	 * @throws NbaBaseException
	 * Express Commission WS calling code moved here from doProcess()
	 */
	private void processExpressCommisionCall() throws NbaBaseException{
		if (NbaOliConstants.OLI_APPTYPE_CONVERSIONNEW == nbaTxLife.getPolicy().getApplicationInfo().getApplicationType()
				|| NbaOliConstants.OLI_APPTYPE_CONVOPAIAD == nbaTxLife.getPolicy().getApplicationInfo().getApplicationType()) {			
			if (NbaUtils.isCompensationCallEnabled()) {				
				boolean eligibleForExpressCommission = AxaUtils.getAgentsEligibilityForExpressCommission(nbaTxLife, getWork());				
				if (eligibleForExpressCommission && !NbaUtils.hasECSTriggered(nbaTxLife.getOLifE().getActivity(), NbaOliConstants.OLI_ACTTYPE_TEMP_ECS_CALL)) { //APSL5232	
					try {
						AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_ECSSUBMIT, this.user,
								nbaTxLife, work, null);
						webServiceInvoker.execute();
						setContractAccess(UPDATE);
						NbaUtils.addECSActivity(nbaTxLife, user.getUserID(), NbaOliConstants.OLI_ACTTYPE_TEMP_ECS_CALL); //APSL5232
						setNbaTxLife(doContractUpdate());
						handleHostResponse();
					} catch (NbaBaseException ex) {
						if (!ex.isFatal()) {
							throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_WS, ex.getMessage());
						}
						throw ex;
					}	
				}
			}
		}	
	}

	//APSL4917 new method
	protected boolean isRiskRighterCase() {
		String riskRighterCase = getWork().getNbaLob().getRiskRighterCase();
			if (String.valueOf(NbaOliConstants.OLI_RISKRIGHTER_CASE).equalsIgnoreCase(riskRighterCase)) {
				return true;
			}
		return false;
	}
	
	
	//APSL4980 New Method
	/**
	 * @purpose This method will be used to set the value of initalReview 
	 */
	private void setInitialReviewIndicator(NbaTXLife nbaTXLife){
		ApplicationInfo appInfo = nbaTXLife.getPolicy().getApplicationInfo();
		ApplicationInfoExtension applicationInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);

		if (null == applicationInfoExt) {
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
			appInfo.addOLifEExtension(olifeExt);
			olifeExt.getApplicationInfoExtension().setActionAdd();
			applicationInfoExt = olifeExt.getApplicationInfoExtension();
		}
		
		if(applicationInfoExt != null){
			applicationInfoExt.setInitialReviewCode(NbaOliConstants.AXA_InitialReviewCode_NA);
			applicationInfoExt.setActionUpdate();
		}
	}
	//New Method APSl5164
	private void setCwaDateAndTime(NbaSource newSource){
		if (!NbaUtils.isBlankOrNull(getNbaTxLife())) {
			if (NbaUtils.isVariableProduct(getNbaTxLife())) {
				try {
					if (NbaUtils.isBlankOrNull(newSource.getNbaLob().getCwaDate()) || NbaUtils.isBlankOrNull(newSource.getNbaLob().getCwaTime())) {
						NbaDst workDst = getWork();
						List nbaSources = workDst.getNbaSources();
						if (nbaSources != null) {
							for (int j = 0; j < nbaSources.size(); j++) {
								NbaSource source = (NbaSource) nbaSources.get(j);
								if (NbaConstants.A_ST_APPLICATION.equals(source.getSourceType())) {
									Date sourceAppDate = NbaUtils.getDateFromStringInAWDFormat(source.getNbaLob().getCreateDate());
									if (!NbaUtils.isBlankOrNull(sourceAppDate)) {
										sourceAppDate = NbaUtils.convertCstToEst(sourceAppDate);
										if (!NbaUtils.isBlankOrNull(sourceAppDate)) {
											newSource.getNbaLob().setCwaDate(sourceAppDate);
											newSource.getNbaLob().setCwaTime(sourceAppDate);
										}
									}
								}
							}

						}
					}
				} catch (NbaBaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// new Method NBLXA-188 & NBLXA-1680
	/**
	 * @purpose This method will be used to insert GI App System Data
	 * @param nbaTXLife
	 * @throws NbaBaseException
	 */
	private void insertGISystemData(Policy policy) throws NbaBaseException {
		if (getNbaTxLife() != null) {
			PolicyExtension policyExt= NbaUtils.getFirstPolicyExtension(policy);
			NbaParty nbaParty = getNbaTxLife().getPrimaryParty();
			String ownerName = getNbaTxLife().getOwnerParty().getFullName();
			AxaGIAppSystemDataVO appSystemDataVO = new AxaGIAppSystemDataVO();
			appSystemDataVO.setBatchID(policyExt.getGIBatchID());
			appSystemDataVO.setPolicynumber(policy.getPolNumber());
			appSystemDataVO.setEmployerName(getWork().getNbaLob().getEmployerName());
			appSystemDataVO.setPrefix(nbaParty.getPrefix());
			appSystemDataVO.setFirstName(nbaParty.getFirstName());
			appSystemDataVO.setLastName(nbaParty.getLastName());
			appSystemDataVO.setSuffix(nbaParty.getSuffix());
			appSystemDataVO.setSsn(nbaParty.getSSN());
			appSystemDataVO.setContractPrintExtractDate(null);
			appSystemDataVO.setPDR_Update_ind(0);
			appSystemDataVO.setPrintPassInd(0);
			appSystemDataVO.setReleaseBatchID("");
			appSystemDataVO.setReleaseBatchInd(0);
			appSystemDataVO.setOwnFullName(ownerName);
			appSystemDataVO.setMdrConsentInd(policyExt.getMDRConsentIND());
			appSystemDataVO.setCompanyCode(policy.getCarrierCode());
			NbaSystemDataDatabaseAccessor.insertGIAppSystemData(appSystemDataVO);
		}
	}
	
	/**
	 * @purpose This method will be used to process assignment for concurrent cases as per kick out rules
	 * @throws NbaBaseException
	 */
	//Begin : NBLXA186 //Modified Method for NBLXA1332
	private void eligibleForTermXpressKickOut() throws NbaBaseException {
		List concurrentCases = new ArrayList();
		List permCases = new ArrayList();
		List conflictCases = new ArrayList();
		List nonConflictCases = new ArrayList();
		boolean currentTExInd = NbaUtils.isTermExpCase(getNbaTxLife());
		int termExpFoundForConflictCases = 0;
		int termExpFoundForNonConflictCases = 0;
		concurrentCases = findConcurrentCases(getNbaTxLife());
		for (int i = 0; i < concurrentCases.size(); i++) {
			NbaTXLife nbaTXLifeDatabase = (NbaTXLife) concurrentCases.get(i);
			Policy policy = nbaTXLifeDatabase.getPolicy();
			if (isChannelConflict(policy)) {
				conflictCases.add(nbaTXLifeDatabase);
				if (NbaUtils.isTermExpCase(nbaTXLifeDatabase)) {
					termExpFoundForConflictCases++;
				}
			} else {
				nonConflictCases.add(nbaTXLifeDatabase);
				if (NbaUtils.isTermExpCase(nbaTXLifeDatabase)) {
					termExpFoundForNonConflictCases++;
				}
			}
		}
		getLogger().logDebug("Existing Term Express Non Conflict Cases Found : " + termExpFoundForNonConflictCases);
		getLogger().logDebug("Existing Term Express Conflict Cases Found : " + termExpFoundForConflictCases);
		getLogger().logDebug("Size of existing Concurrent cases : " + concurrentCases.size());
		getLogger().logDebug("Channel Conflict : " + channelConflict);
		Policy incPolicy = getNbaTxLife().getPolicy();
		if (((currentTExInd || (termExpFoundForConflictCases > 0 || termExpFoundForNonConflictCases > 0)) && (concurrentCases.size() > 0 || channelConflict))) {
			// incoming case is perm/mloa, means there is one or more termexp case in the system and no other concurrent perm/mloa/termUmbrella
			if (conflictCases.size() > 0) {
				List casesToReAssign = findCases(conflictCases, true);
				double totalFaceAmtForConflictcases = calculateTotalFaceAmt(casesToReAssign);
				double faceAmount = (totalFaceAmtForConflictcases > minTermUmbAmt) ? totalFaceAmtForConflictcases : minTermUmbAmt;
				getLogger().logDebug("Face Amount for underwriter assigmment " + faceAmount);
				NbaTXLife baseCaseTx = getBaseCase(casesToReAssign, permCases, currentTExInd, termExpFoundForConflictCases, false);
				String uwQ =null;
				if (baseCaseTx != null) {
					uwQ=assignUW(faceAmount, baseCaseTx);
				processConcurrentCases(casesToReAssign, uwQ);
				if (currentTExInd) {
					getWork().getNbaLob().setUndwrtQueue(uwQ);
				}
			}
			}
			if (nonConflictCases.size() > 0) {
				double faceAmount = calculateTotalFaceAmt(nonConflictCases);
				faceAmount += getNbaTxLife().getLife().getFaceAmt();
				NbaTXLife baseCaseTx = null;
				if (/*conflictCases.size() > 0//why
						&& */((NbaConstants.COMPANY_MLOA).equalsIgnoreCase(incPolicy.getCarrierCode()) || !NbaUtils.isTermLife(incPolicy))) {
					baseCaseTx = getNbaTxLife();
					resetTermExpInd=true;//QC19150/NBLXA-1436
				} else {
					baseCaseTx = getBaseCase(nonConflictCases, permCases, currentTExInd, termExpFoundForNonConflictCases, true);
				}
				getLogger().logDebug("Face Amount for underwriter assigmment " + faceAmount);
				String uwQforNonConflictCases=null;
				if (baseCaseTx != null) {
					if (!NbaUtils.isTermLife(baseCaseTx.getPolicy())) {//QC19200/NBLXA-1508 Begin
						getLogger().logDebug("Inside base case is perm case for non conflict cases");
						NbaSearchVO searchVO = searchContract(baseCaseTx.getPolicy().getContractKey());
						if (searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
							ListIterator results = searchVO.getSearchResults().listIterator();
							while (results.hasNext()) {
								NbaDst aWorkItem = retrieveWorkItemForLockedCase((NbaSearchResultVO) results.next());
								uwQforNonConflictCases = aWorkItem.getNbaLob().getUndwrtQueue();
								getLogger().logDebug("UNDQ lob of perm case for non conflict cases is : " + uwQforNonConflictCases);
							}
						}
					}
					if (NbaUtils.isBlankOrNull(uwQforNonConflictCases)) {
						getLogger().logDebug("Call assignUW method to assign the underwriter on concurrent and base case");
						uwQforNonConflictCases = assignUW(faceAmount, baseCaseTx);//QC19200/NBLXA-1508 End
					}
				processConcurrentCases(nonConflictCases, uwQforNonConflictCases);
				getWork().getNbaLob().setUndwrtQueue(uwQforNonConflictCases);
			}
			}
			// If two term express case , not to be kicked out of term express
			if (!resetTermExpInd && currentTExInd) {
				getWork().getNbaLob().setDisplayIconLob("1");
			}
		}
	}
	
	/*
	 * New Method for QC18931/APSL5399  when incoming case is term and existing case is term express/perm/umbrella without channel conflict . reset term express indicator = true means
	 * turn off the term express indicator except for the below mentioned scenario. 
	 * 
	 * If incoming and existing case is term express and face amount not greater than "minTermUmbAmt" then no need
	 * to reset term express Indicator (resetTermExpInd=false)
	 */
	private void checkReset(Double totalFaceAmt, NbaTXLife baseCaseTx) {
		// TODO Auto-generated method stub
		if (!hasTermExpInd(baseCaseTx)) {
			resetTermExpInd = true;
		} else
			resetTermExpInd = totalFaceAmt >= minTermUmbAmt;
	}

	// will the assignment run even if the base case is a perm case??  
	private String assignUW(Double totalFaceAmt, NbaTXLife baseCaseTx) throws NbaBaseException {
		Double faceAmt = baseCaseTx.getLife().getFaceAmt();
		baseCaseTx.getLife().setFaceAmt(totalFaceAmt);
		NbaDst aWorkItem = null;
		if (baseCaseIsIncomingCase(baseCaseTx)) {
			getLogger().logDebug("Inside base case incoming case");
			assignQueues(totalFaceAmt);
			getNbaTxLife().getLife().setFaceAmt(faceAmt);
			return getWork().getNbaLob().getUndwrtQueue();
		}
		NbaSearchVO searchVO = searchContract(baseCaseTx.getPolicy().getContractKey());
		if (searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
			ListIterator results = searchVO.getSearchResults().listIterator();
			while (results.hasNext()) {
				aWorkItem = retrieveWorkItemForLockedCase((NbaSearchResultVO) results.next());
				boolean isMLOACompanyPresent = NbaConstants.COMPANY_MLOA.equalsIgnoreCase(baseCaseTx.getPolicy().getCarrierCode());
				AxaUWAssignmentEngineVO uwAssignment = new AxaUWAssignmentEngineVO();
				uwAssignment.setTxLife(baseCaseTx);
				uwAssignment.setNbaDst(aWorkItem);
				uwAssignment.setReassignment(true);
				uwAssignment.setCasemanagerRequired(false);
				uwAssignment.setMLOACompanyPresent(isMLOACompanyPresent);
				new AxaUnderwriterAssignmentEngine().execute(uwAssignment);

			}
		}
		baseCaseTx.getLife().setFaceAmt(faceAmt);
		return aWorkItem.getNbaLob().getUndwrtQueue();
	}

	// solution assumption - any new case assigned on the basis of existing case assumes existing cases in system are properly assigned. 
	//Modified for NBLXA1332
	private NbaTXLife getBaseCase(List concurrentCases, List permCases, boolean currentTExInd, int termExFound,boolean flag) { //Modified for NBLXA332
		for (int i = 0; i < concurrentCases.size(); i++) {
			NbaTXLife txlife = (NbaTXLife) concurrentCases.get(i);
			if ((NbaConstants.COMPANY_MLOA).equalsIgnoreCase(txlife.getPolicy().getCarrierCode())) {
				resetTermExpInd=true;
				return txlife;
			} else if (!NbaUtils.isTermLife(txlife.getPolicy())) {
				permCases.add(txlife);
			}
		}
		if (permCases.size() > 0) {
			resetTermExpInd=true;
			return (NbaTXLife) permCases.get(0);
		}
		return findBaseTermCase(concurrentCases, currentTExInd, termExFound,flag);
	}

	private NbaTXLife findBaseTermCase(List concurrentCases, boolean currentTExInd, int termExFound,boolean flag) { //Modifed for NBLXA1332
		// calculate total face amount
		Double totalFaceAmt = calculateTotalFaceAmt(concurrentCases);
		//Begin NBLXA1332
		if(flag){
			totalFaceAmt+=getNbaTxLife().getLife().getFaceAmt();
		}
		//End NBLXA1332
		int concCaseCount = 0;
		for (int i = 0; i < concurrentCases.size(); i++) {
			NbaTXLife txlife = (NbaTXLife) concurrentCases.get(i);
			if (txlife != null && NbaUtils.isTermLife(txlife.getPolicy())) {// Need to consider term Umbrella also ??
				concCaseCount++;
			}
		}
		// int concCaseCount = concurrentCases.size();
		if (concCaseCount > termExFound) {
			resetTermExpInd = true;
			return findCase(concurrentCases, false);// false means return Term Umbrella case
		} else if (currentTExInd && (concCaseCount == termExFound)) {
			if (totalFaceAmt > 2000000) {
				resetTermExpInd = true;
			}
			return findCase(concurrentCases, true);// true means return TEx Case
		} else if (!currentTExInd && channelConflict) {
			resetTermExpInd = true;
			return findCase(concurrentCases, true);
		} else if (!currentTExInd && (concCaseCount == termExFound)) {
			resetTermExpInd = true;
			return getNbaTxLife();
		}
		return null;
	}

	private NbaTXLife findCase(List concurrentCases, boolean texCase) {
		for (int i = 0; i < concurrentCases.size(); i++) {
			if (texCase == hasTermExpInd((NbaTXLife) concurrentCases.get(i))
					&& NbaUtils.isTermLife(((NbaTXLife) concurrentCases.get(i)).getPolicy())) {// need to check for perm case //Modifed for QC18957/APSL5404
				return (NbaTXLife) concurrentCases.get(i);
			}
		}
		return null;
	}
	
	// return the list of Term Exp cases, if texCase = true
	private List findCases(List concurrentCases, boolean texCase) {
		List tempList = new ArrayList();
		for (int i = 0; i < concurrentCases.size(); i++) {
			NbaTXLife txlife = (NbaTXLife) concurrentCases.get(i);
			if (texCase == hasTermExpInd(txlife)) {
				tempList.add(txlife);
			}
		}
		return tempList;
	}

/*	private boolean checkUWLimit(Double totalFaceAmt, NbaTXLife nbaTXLife) {		
		AxaAssignmentRulesVO assignmentRulesVO = constructAssignRulesVO(nbaTXLife, nbaDst,isMLOACompanyPresent);//QC15916/APSL4389
		return false;
	}*/
	
	public NbaSearchVO searchContract(String contractKey) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = lookupWork(getUser(), searchVO);
		return searchVO;
	}
	
	private boolean checkParameters(NbaTXLife txLife, Party party) throws NbaBaseException {// todo put check for apptype =1 and ADC cases as well
		if (txLife != null) {
			boolean newAppADC = checkNewAppNoADC(txLife);
			boolean isReissue = NbaUtils.isContractChange(txLife);
			boolean pendStat = NbaUtils.isStatusPending(txLife.getPolicy().getPolicyStatus());
			boolean isPIorJI = isPrimaryOrJointInsured(txLife, party);
			boolean isGIApp = isGIApplication(txLife);
			// boolean isDistQualify = isDistChannelQualify(txLife);
			return /* isDistQualify && */!isReissue && pendStat && isPIorJI && newAppADC && !isGIApp;
		}
		return false;
	}
	
	private boolean checkNewAppNoADC(NbaTXLife txLife) {
		return NbaOliConstants.OLI_APPTYPE_NEW == txLife.getPolicy().getApplicationInfo().getApplicationType()
				&& NbaOliConstants.OLI_PRODTYPE_305 != txLife.getPolicy().getProductType();
	}

	private boolean isPrimaryOrJointInsured(NbaTXLife txLife, Party party) {
		// TODO put null checks
		if (party.getGovtID() != null && txLife.getPrimaryParty()!=null &&  party.getGovtID().equals(txLife.getPrimaryParty().getSSN())) {
			return true;
		} else if (party.getGovtID() != null && txLife.getProposedInsured2Party()!=null && party.getGovtID().equals(txLife.getProposedInsured2Party().getSSN())) {
			return true;
		}
		return false;
	}
	
	private boolean baseCaseIsIncomingCase(NbaTXLife baseCaseTx) {
		if (baseCaseTx.getPolicy().getContractKey().equalsIgnoreCase(curpolNumber)) {
			//showGeneralComment = false;//commented for QC18933/APSL5400
			return true;
		}
		return false;
	}
	
	public NbaDst retrieveWorkItem(NbaSearchResultVO resultVO) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setNbaUserVO(getUser());
		retOpt.setWorkItem(resultVO.getWorkItemID(), true);
		retOpt.setLockWorkItem();
		aWorkItem = retrieveWorkItem(getUser(), retOpt);
		return aWorkItem;
	}
	
	public void processConcurrentCases(List concurrentCases, String uwQ) throws NbaBaseException {
		NbaSearchVO searchVO = null;
		ListIterator results = null;
		NbaDst aWorkItem = null;
		//Begin NBLXA1332
		if (!commentAddedOnIncomingCase) { //Added for QC19131/NBLXA-1437
			if (!companionCasesProcCalled && !channelConflict && !channelConflictComment) {// QC18937/APSL5403
				getWork().getNbaLob().setUndwrtQueue(uwQ);
				NbaUtils.addGeneralComment(getWork(), getUser(), "Concurrent Case");
			} else if (!companionCasesProcCalled && (channelConflict || channelConflictComment)) { // APSL5400/QC18933
				NbaUtils.addGeneralComment(getWork(), getUser(), "Channel conflict");
			} else if (companionCasesProcCalled) {
				NbaUtils.addGeneralComment(getWork(), getUser(), "Linked Case");
			}
			commentAddedOnIncomingCase = true;//Added for QC19131/NBLXA-1437
		}
		//End NBLXA1332
		for (int index = 0; index < concurrentCases.size(); index++) {
			NbaTXLife txLife = (NbaTXLife) concurrentCases.get(index);
			if (((!companionCasesProcCalled && (NbaUtils.isTermLife(txLife.getPolicy()))) || companionCasesProcCalled)) {//Modified for NBLXA1332
				searchVO = searchContract(txLife.getPolicy().getPolNumber());
				if (searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
					results = searchVO.getSearchResults().listIterator();
					try {
						aWorkItem = retrieveWorkItem((NbaSearchResultVO) results.next());
						getLogger().logDebug("Policy number of Concurrent term case getting processed : "+txLife.getPolicy().getPolNumber());
						if (resetTermExpInd) {
							ApplicationInfo appInfo = txLife.getPolicy().getApplicationInfo();
							ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
							appInfoExt.setTermExpressInd(false);
							appInfoExt.setActionUpdate();
							aWorkItem.getNbaLob().setDisplayIconLob("0");
						}
						updateUW(txLife, aWorkItem, uwQ);
						unlockWorkitemAndSessionCleanUP(aWorkItem);
					} catch (NbaLockedException exception) {
						aWorkItem = retrieveWorkItemForLockedCase((NbaSearchResultVO) results.previous());
						String userId = getUserIdForQueue(uwQ);
						createMiscWorkTransaction(aWorkItem, userId);
						results.next();
						continue;
					}

				}
			}
		}

	}

	private void updateUW(NbaTXLife txlife, NbaDst dst, String uwQ) throws NbaBaseException {
		boolean updateQueue = false;
		String oldUndQueue = dst.getNbaLob().getUndwrtQueue();
		String oldQueue = dst.getNbaLob().getQueue();
		if (!NbaUtils.isEmpty(oldUndQueue) && !oldUndQueue.equalsIgnoreCase(uwQ)) {
			dst.getNbaLob().setUndwrtQueue(uwQ);
			if (oldQueue.equalsIgnoreCase(oldUndQueue)) {
				updateQueue = true;
			}
		}

		if (null != uwQ && uwQ.length() > 0 && !uwQ.equalsIgnoreCase(oldUndQueue)) {
			/*
			 * String userId = getUserIdForQueue(uwQ); if(userId != null && userId.length() > 0){ appinfo.setHOUnderwriterName(userId);
			 * appinfo.setActionUpdate(); txlife.setAccessIntent(NbaConstants.UPDATE); txlife = NbaContractAccess.doContractUpdate(txlife, getWork(),
			 * getUser());
			 */

			if (updateQueue) {
				setStatusOnly(dst, NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT);
				// Update the Work Item with it's new status and update the work item in AWD
			}
			//Modified code to add general comments for for APSL5400/QC18933
			//Begin NBLXA1332
			if (!companionCasesProcCalled && !channelConflict && !channelConflictComment) {
				NbaUtils.addGeneralComment(dst, getUser(), "Concurrent Case ");
			} else if (!companionCasesProcCalled && (channelConflict || channelConflictComment)) {
				NbaUtils.addGeneralComment(dst, getUser(), "Channel conflict");
			} else if (companionCasesProcCalled) {
				NbaUtils.addGeneralComment(dst, getUser(), "Linked Case");
			}
			//End NBLXA1332
			txlife.setAccessIntent(NbaConstants.UPDATE);
			txlife.setBusinessProcess("REASSIGNMENT_QUEUE");
			NbaContractAccess.doContractUpdate(txlife, dst, getUser());
			updateWorkItem(dst);
			unlockWork(getUser(), dst);
		}
	}

	public void unlockWorkitemAndSessionCleanUP(NbaDst lockedWork) throws NbaBaseException {
		lockedWork.setNbaUserVO(getUser());
		unlockWork(getUser(), lockedWork);
		NbaContractLock.removeLock(lockedWork, getUser());
	}

	protected void setStatusOnly(NbaDst dst, String userID) throws NbaBaseException {
		NbaUserVO user = new NbaUserVO(userID, "");
		statusProvider = new NbaProcessStatusProvider(user, dst.getNbaLob());

		if (statusProvider != null && statusProvider.getPassStatus() != null && !statusProvider.getPassStatus().equals(dst.getStatus())) {
			dst.setStatus(statusProvider.getPassStatus());
			NbaUtils.setRouteReason(dst, statusProvider.getPassStatus());
		}
	}

	/**
	 * Update the work item in AWD. Unlock any locked children.
	 */
	public NbaDst updateWorkItem(NbaDst dst) throws NbaBaseException {
		getLogger().logDebug("Updating LOB for Work item ");
		if (dst.isCase())
			dst.getCase().setUpdate("Y");
		else
			dst.setUpdate();
		dst.setNbaUserVO(getUser());
		AccelResult accelResult = new AccelResult();
		accelResult.merge(currentBP.callBusinessService("NbaUpdateWorkBP", dst));
		NewBusinessAccelBP.processResult(accelResult);
		return (NbaDst) accelResult.getFirst();
	}

	public void createMiscWorkTransaction(NbaDst nbaDst, String userID) throws NbaBaseException {
		getLogger().logDebug("Inside createMiscWork transacction for locked case");
		// NBA208-32
		WorkItem transaction = new WorkItem();
		NbaDst nbadstforMiscWork = new NbaDst();
		// NBA208-32
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
		setStatusOnly(nbadstforMiscWork, NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT);
		transaction.setStatus(nbadstforMiscWork.getStatus());
		nbadstforMiscWork.getNbaLob().setRouteReason("Concurrent Case Routed for review");
		NbaUtils.addGeneralComment(nbadstforMiscWork, getUser(), "Concurrent case pending for review, need to reassign to underwriter " + userID);
		transaction.setCreate("Y");
		updateWork(getUser(), nbadstforMiscWork);
		unlockWork(getUser(), nbadstforMiscWork);

	}

	public NbaDst retrieveWorkItemForLockedCase(NbaSearchResultVO resultVO) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setNbaUserVO(getUser());
		retOpt.setWorkItem(resultVO.getWorkItemID(), true);
		aWorkItem = retrieveWorkItem(getUser(), retOpt);
		return aWorkItem;
	}
	
	
	private boolean isChannelConflict(Policy policy) {
		if (policy != null && getNbaTxLife().getPolicy() != null) {
			PolicyExtension polExtension = NbaUtils.getFirstPolicyExtension(policy);
			PolicyExtension currentCasePolExt = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
			if (polExtension != null && currentCasePolExt != null) {
				if ((!NbaUtils.isTermLife(policy) && NbaUtils.isTermLife(getNbaTxLife().getPolicy()))
						|| (NbaUtils.isTermLife(policy) && !NbaUtils.isTermLife(getNbaTxLife().getPolicy()))) { // Added for QC18937
					if (polExtension.getDistributionChannel() != currentCasePolExt.getDistributionChannel()) { // Need to add conditon for term cases.
						channelConflict = true;
						return true;
					}
				}
				//Begin : NBLXA1332
				if ((NbaUtils.isTermLife(policy) && NbaUtils.isTermLife(getNbaTxLife().getPolicy()))) { // Added for QC18937
					if (polExtension.getDistributionChannel() != currentCasePolExt.getDistributionChannel()) { // Need to add conditon for term cases.
						channelConflictComment = true;
					}
				}
				//End : NBLXA1332
			}
		}
		return false;
	}
	
	private boolean hasTermExpInd(NbaTXLife txlife) {
		boolean termExpInd = false;
		if (txlife != null && txlife.getPolicy() != null) {
		ApplicationInfo appInfo = txlife.getPolicy().getApplicationInfo();
			if (appInfo != null) {
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
				if (appInfoExt != null) {
					termExpInd = appInfoExt.getTermExpressInd();
				}
			}
		}
		return termExpInd;
	}
	
	
	private Double calculateTotalFaceAmt(List concurrentCases) {
		Double totalfaceAmt = 0.0;
		for (int i = 0; i < concurrentCases.size(); i++) {
			NbaTXLife txlife = (NbaTXLife) concurrentCases.get(i);
			if (channelConflict && NbaUtils.isTermLife(txlife.getPolicy())) { // Added to consider face amount of term cases only in case of channel
																				// conflict
				totalfaceAmt += txlife.getLife().getFaceAmt();
			} else if(!channelConflict){
				totalfaceAmt += txlife.getLife().getFaceAmt();
			}
		}
		getLogger().logDebug("Total Face Amount for concurrent cases : " + totalfaceAmt);
		return totalfaceAmt;
	}
	
	protected String getUserIdForQueue(String queue) {
		NbaTableAccessor nbaTableAccessor = new NbaTableAccessor();
		String userID = "";
		String userName = "";
		try {
			userID = nbaTableAccessor.getUserIdForQueue(queue);
			userName = nbaTableAccessor.getUserName(userID);
			getLogger().logDebug("Reassign the case to following user : "+ userName + " "+userID);
		} catch (NbaDataAccessException ndae) {
			addComment(ndae.getMessage());
		}
		return userName;
	}

	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaProcAppSubmit.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log(NO_LOGGER);
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	//End : NBLXA186
	
	//New Method for NBLXA1332
	protected String companionCasesProcessing(List companionCases) throws NbaBaseException {
		// NbaTXLife mloaCase = null;
		int termExpFound = 0;
		List mloaCases = new ArrayList();
		List permCases = new ArrayList();
		List compCases = new ArrayList();
		double totalFaceAmt = 0.0;
		String uw = null;
		NbaTXLife baseCaseTx = null;
		boolean currenttermExpInd = NbaUtils.isTermExpCase(nbaTxLife);
		for (int i = 0; i < companionCases.size(); i++) {
			NbaTXLife nbaTXLifeDatabase = null;
			NbaCompanionCaseVO vo = (NbaCompanionCaseVO) companionCases.get(i);
			String contractKey = vo.getNbaLob().getPolicyNumber();
			String companyCode = vo.getNbaLob().getCompany();
			String backendKey = vo.getNbaLob().getBackendSystem();
			if (contractKey != null && companyCode != null && backendKey != null) {
				nbaTXLifeDatabase = NbaUnderwritingRiskHelper.doHoldingInquiry(contractKey, companyCode, backendKey, getWork(), getUser());
			}
			if (nbaTXLifeDatabase != null) {
				Policy policy = nbaTXLifeDatabase.getPolicy();
				if (!nbaTxLife.getPolicy().getPolNumber().equalsIgnoreCase(contractKey)) {
					if (NbaConstants.COMPANY_MLOA.equalsIgnoreCase(companyCode)) {
						mloaCases.add(nbaTXLifeDatabase);
					} else if (!NbaUtils.isTermLife(policy)) {
						permCases.add(nbaTXLifeDatabase);
					} else if (hasTermExpInd(nbaTXLifeDatabase)) {
						termExpFound++;
					}
					totalFaceAmt += nbaTXLifeDatabase.getFaceAmount();
					if (!contractKey.equalsIgnoreCase(nbaTxLife.getPolicy().getContractKey()))
						compCases.add(nbaTXLifeDatabase);
				}
			}
		}
		if (NbaConstants.COMPANY_MLOA.equalsIgnoreCase(nbaTxLife.getPolicy().getCompanyKey())) {
			mloaCases.add(nbaTxLife);
		} else if (!NbaUtils.isTermLife(nbaTxLife.getPolicy())) {
			permCases.add(nbaTxLife);
		} else if (currenttermExpInd) { // when two term express cases less than 2 million combined face amount
			termExpFound++;
		}
		totalFaceAmt += nbaTxLife.getFaceAmount();
		compCases.add(getNbaTxLife());
		companionCasesProcCalled = true;
		if (/* (currenttermExpInd || termExpFound > 0) && */compCases.size() > 1) {
			baseCaseTx = getBaseCase(compCases, mloaCases, permCases, currenttermExpInd, termExpFound);
		} else if (compCases.size() == 1) {
			baseCaseTx = getNbaTxLife();
		}
		if (baseCaseTx != null) {
			if (termExpFound == 0) {
				totalFaceAmt = baseCaseTx.getLife().getFaceAmt();
			}
			uw = assignUW(totalFaceAmt, baseCaseTx);
			checkReset(totalFaceAmt, baseCaseTx);
			if (compCases.size() > 1) {
				processConcurrentCases(compCases, uw);
			}
			getWork().getNbaLob().setUndwrtQueue(uw);// need to verify
			if (!resetTermExpInd && currenttermExpInd) {
				getWork().getNbaLob().setDisplayIconLob("1");
			}
		}
		return uw;
	}
	
	//New Method for NBLXA1332
	protected List findConcurrentCases(NbaTXLife nbatxlife) throws NbaBaseException {
		Party orgParty = null;
		List partyPendingContracts;
		NbaTXLife nbaTXLifeDatabase = null;
		List concurrentCases = new ArrayList();
		List excludePolicies = new ArrayList();
		excludePolicies.add(nbatxlife.getOLifE().getHoldingAt(0).getPolicy().getContractKey());
		getLogger().logDebug("Finding concurrent cases for policy : "+nbatxlife.getLife().getContractKey());
		List insurableParties = nbatxlife.getInsuredPartiesForPrimHolding();// TODO CHECK Only for joint insured and PI
		int noOfParties = insurableParties.size();
		for (int i = 0; i < noOfParties; i++) {
			orgParty = (Party) insurableParties.get(i);
			partyPendingContracts = NbaContractAccess.doPartyInquiry(orgParty, excludePolicies);
			ListIterator partyIterator = partyPendingContracts.listIterator();
			NbaPartyData partyData = null;
			while (partyIterator.hasNext()) {
				partyData = (NbaPartyData) partyIterator.next();
				String contractKey = partyData.getContractKey();
				String companyCode = partyData.getCompanyKey();
				String backendKey = partyData.getBackendKey();
				nbaTXLifeDatabase = NbaUnderwritingRiskHelper.doHoldingInquiry(contractKey, companyCode, backendKey, getWork(), getUser());// QC4373
				if (checkParameters(nbaTXLifeDatabase, orgParty)) {
					// concurrentCaseExists = true;
					concurrentCases.add(nbaTXLifeDatabase);
					if (nbatxlife.getLife().getContractKey().equalsIgnoreCase(getNbaTxLife().getLife().getContractKey())) {
						noCompProcessingForIncCase = true;
					}
				}
			}
		}
		getLogger().logDebug("Concurrent cases size : "+concurrentCases.size());
		return concurrentCases;
	}
	
	//New Method for NBLXA1332
	private NbaTXLife getBaseCase(List companionCases, List mloaCases, List permCases, boolean currentTExInd, int termExFound) {
		if (mloaCases.size() > 0) {// assumption : mloa term cases are being assigned to mloa perm underwriters
			resetTermExpInd = true;
			return (NbaTXLife) mloaCases.get(0);
		} else if (permCases.size() > 0) {// TODO check current case is perm if yes return it
			resetTermExpInd = true;
			return (NbaTXLife) permCases.get(0);
		} else {
			return findBaseTermCase(companionCases, currentTExInd, termExFound, false);
		}
	}
	
	/*
	 * This method creates a initial Review WI for CSG case manager 
	 */
	// NBLXA-1632
	protected void createCSGReviewTransaction() {
		try {
				Map deOinkMap = new HashMap();
				deOinkMap.put("A_CSGReviewTransaction", "true");
				String[] workTypeAndStatus = getWorkTypeAndStatus(deOinkMap);
				if (workTypeAndStatus[0] != null) {
					NbaTransaction aTransaction;
					aTransaction = getWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]);
					aTransaction.getNbaLob().setDisplayIconLob(getWork().getNbaLob().getDisplayIconLob());
				}
		} catch (Exception e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getAWDFailStatus()));

		}
	}
	
	/*
	 * This method updates CSG Review Indicator to PENDING 
	 * NBLXA-1632
	 */
	private void updateCSGReviewIndicatorOnPolicy() throws NbaContractAccessException, NbaBaseException {
		NbaTXLife txLife = getNbaTxLife();
		if (txLife == null) {
			throw new NbaContractAccessException(NbaBaseException.AUTO_PROC_ERROR);
		}
		PolicyExtension extension = NbaUtils.getFirstPolicyExtension(txLife.getPolicy());
		if (extension == null) {
			throw new NbaContractAccessException(NbaBaseException.AUTO_PROC_ERROR);
		}
		txLife.setAccessIntent(UPDATE);
		extension.setCoilCsgreviewInd(NbaOliConstants.NBA_CSGREVIEW_PENDING);
		extension.setActionUpdate();
		NbaContractAccess.doContractUpdate(txLife, getWork(), getUser());
	}
	
	/*
	 * This method creates a Review WI for Business Strategy 
	 */
	// NBLXA-1823
	protected void createBusinessStrategyTransaction() {
		try {
				Map deOinkMap = new HashMap();
				deOinkMap.put("A_BusinessStrategyTransaction", "true");
				String[] workTypeAndStatus = getWorkTypeAndStatus(deOinkMap);
				if (workTypeAndStatus[0] != null) {
					NbaTransaction aTransaction;
					aTransaction = getWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]);
				}
		} catch (Exception e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getAWDFailStatus()));

		}
	}
	
	/**
	 * NBLXA-2487
	 * @param isLCsmqLOBPresent
	 * @throws NbaBaseException
	 */
	protected void assignLCMQueue(NbaLob caseLob, NbaTXLife caseTxLife) throws NbaBaseException { //NBLXA-2328[NBLXA-2595]
			NbaProcessStatusProvider statusProvider = getProcesStatusProvider(caseLob, caseTxLife);
			String licenseCaseManagerLOB = null;
			boolean isLCsmqLOBPresent = false;
			// if any one of them are not assigned
			if (!NbaUtils.isBlankOrNull(caseLob.getCompanionType()) && !NbaConstants.NOT_A_COMPANION_CASE.equalsIgnoreCase(caseLob.getCompanionType())
					&& !noCompProcessingForIncCase) {// if not null and not of type "0" //Modified for NBLXA1332
				NbaCompanionCaseVO vo;
				NbaCompanionCaseRules rules = new NbaCompanionCaseRules(user, work);
				List compCases = rules.getCompanionCases();
				NbaLob companionLob = null;
				int size = compCases.size();
				for (int i = 0; i < size; i++) {
					vo = (NbaCompanionCaseVO) compCases.get(i);
					companionLob = vo.getNbaLob();
					if (caseLob.getPolicyNumber().equalsIgnoreCase(vo.getContractNumber())) {// when current work is retrieved
						continue;
					}
					if (!NbaUtils.isBlankOrNull(companionLob) && !NbaUtils.isBlankOrNull(companionLob.getLicCaseMgrQueue()) && !isLCsmqLOBPresent) {
						licenseCaseManagerLOB = companionLob.getLicCaseMgrQueue();
						isLCsmqLOBPresent = true;
					}
					if (isLCsmqLOBPresent) {
						break;
					}
				}
			}
			//BEGIN NBLXA-2328[NBLXA-2595]
			if (!isLCsmqLOBPresent) {
				if(caseLob.getDistChannel() == NbaOliConstants.DISTIBUTION_CHANNEL_RETAIL) {
					licenseCaseManagerLOB = AxaUtils.getMiscCMAssignmentRules(statusProvider.getLicenseCaseMangerQueue());
					if (licenseCaseManagerLOB != null && !CONTRACT_DELIMITER.equalsIgnoreCase(licenseCaseManagerLOB)) {
						caseLob.setLicCaseMgrQueue(licenseCaseManagerLOB);
					}
				} else if((caseLob.getDistChannel() == NbaOliConstants.DISTIBUTION_CHANNEL_WHOLESALE) && !isGIApplication(caseTxLife)) { //NBLXA-2653
					AxaUWAssignmentEngineVO licmAssignment = new AxaUWAssignmentEngineVO();
					licmAssignment.setTxLife(caseTxLife);
					licmAssignment.setNbaDst(getWork());
					licmAssignment.setLicmAssignmentReq(true);
					licmAssignment.setGroupAssigned(statusProvider.getLicenseCaseMangerQueue());
					new AxaUnderwriterAssignmentEngine().execute(licmAssignment);
				}
			} else if (isLCsmqLOBPresent && licenseCaseManagerLOB != null && !CONTRACT_DELIMITER.equalsIgnoreCase(licenseCaseManagerLOB)) {
				caseLob.setLicCaseMgrQueue(licenseCaseManagerLOB);
			}
			//END NBLXA-2328[NBLXA-2595]
		}
}
