package com.csc.fsg.nba.access.contract;
/*
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.dataobject.nba.datafeed.NbaDataFeedDO;
import com.csc.fsg.nba.backendadapter.NbaBackEndAdapterFacade;
import com.csc.fsg.nba.business.process.NbaIssueStandalone;
import com.csc.fsg.nba.business.transaction.AxaDataChangeManager;
import com.csc.fsg.nba.contract.auditor.NbaAuditorFactory;
import com.csc.fsg.nba.contract.extracts.NbaContractExtracts;
import com.csc.fsg.nba.contract.extracts.NbaCwaPaymentsExtract;
import com.csc.fsg.nba.contract.validation.NbaContractValidation;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceEvents;
import com.csc.fsg.nba.database.NbaAuditDataBaseAccessor;
import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.database.NbaContractDataBaseAccessor;
import com.csc.fsg.nba.database.NbaContractDataBaseInfo;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaContractLockData;
import com.csc.fsg.nba.database.NbaDatabaseUtils;
import com.csc.fsg.nba.database.NbaPartyInquiryDataBaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaContractAccessException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaAuditingContext;
import com.csc.fsg.nba.foundation.NbaBatchProcessingContext;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaPerformanceLogger;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.nbproducer.NbaNbproducerEvents;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAuditorVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.BackEnd;
import com.csc.fsg.nba.vo.configuration.UnsupportedPartyType;
import com.csc.fsg.nba.vo.configuration.UnsupportedPartyTypes;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Client;
import com.csc.fsg.nba.vo.txlife.ClientExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.EPolicyData;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Intent;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeParticipantExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.OrganizationExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationExtension;
import com.csc.fsg.nba.vo.txlife.RelationProducerExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
/**
 * NbaContractAccess provides an interface to the pending contract datastore to retrieve, 
 * insert, update and delete contract data.  
 * This class will, based on the primary datastore, invoke other classes to provide
 * services.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA050</td><td>Version 3</td><td>NBA Pending Database</td></tr> 
 * <tr><td>NBP001</td><td>Version 3</td><td>nbProducer Initial Development</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>NBA091</td><td>Version 3</td><td>Agent Name and Address</td></tr>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>NBA094</td><td>Version 3</td><td>Transaction Validation</td></tr>
 * <tr><td>NBA066</td><td>Version 3</td><td>nbA Accounting and Disbursements extracts</td></tr>
 * <tr><td>SPR1656</td><td>Version 4</td><td>Allow for Refund/Reversal Extracts.</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging Enhancement</td></tr> 
 * <tr><td>SPR1719</td><td>Version 4</td><td>Impairments from database are ignored</td></tr>
 * <tr><td>NBA105</td><td>Version 4</td><td>Underwriting Risk</td></tr>
 * <tr><td>ACN003</td><td>Version 4</td><td>Key Person Buy Sell</td></tr>
 * <tr><td>ACN005</td><td>Version 4</td><td>UW Aviation</td></tr>
 * <tr><td>ACN013</td><td>Version 4</td><td>UW Mortality</td></tr>
 * <tr><td>SPR1931</td><td>Version 5</td><td>UOrder of Trans Val and Issue Process is Wrong - Should Validate First</td></tr>
 * <tr><td>SPR1163</td><td>Version 5</td><td>Adding Endorsements to an Annuity does not work/td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA102</td><td>Version 5</td><td>nbA Transaction Logging Project</td><tr> 
 * <tr><td>SPR2816</td><td>Version 6</td><td>Contract Change Invokes Admin Holding for Wrappered Plans Instead of Pending Holding</td><tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>New class created from code refactored from NbaContractAccessBean. All pre-existing audit numbers have been preserved in the re-factored code.</td><tr>
 * <tr><td>SPR2863</td><td>Version 6</td><td>Reinsurance Tab - Reinsurance Response information is not committed for wrappered cases</td><tr>
 * <tr><td>NBA126</td><td>Version 6</td><td>Vantage Contract Change</td><tr>
 * <tr><td>SPR2817</td><td>Version 6</td><td>Pending Accounting Needs to Be Added to nbA</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>NBA137</td><td>Version 6</td><td>nbA Agent Advances</td></tr>
 * <tr><td>SPR2697</td><td>Version 6</td><td>Requirement Matching Criteria Needs to Be Expanded</td></tr>
 * <tr><td>SPR3185</td><td>Version 6</td><td>Error from Host when submitting a receipted duplicate temp requirement.</td></tr>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * <tr><td>SPR3019</td><td>Version 6</td><td>Database update error for RelationProducerExtension </td></tr>
 * <tr><td>SPR3216</td><td>Version 6</td><td>RelationProducerExtension.SituationCode is not set from value returned in FAGSITCD</td></tr>
 * <tr><td>SPR3203</td><td>Version 7</td><td>Only Proposed Substandard Ratings Should be Stored in nbA Pending Contract Database</td></tr>
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application Project</td></tr>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>
 * <tr><td>NBA211</td><td>Version 7</td><td>Partial Application</td></tr>
 * <tr><td>SPR2889</td><td>Version 7</td><td>Reinsurance Tab - Reinsurance indicator is not set and also reinsurance company name is not displayed in the tool tip when mouse is rolled over</td></tr>
 * <tr><td>NBA208</td><td>Version 7</td><td>nbA version 7 performance tuning and testing</td></tr>
 * <tr><td>NBA208-10</td><td>Version 7</td><td>Improve merging logic</td></tr>
 * <tr><td>NBA208-18</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 18</td></tr> 
 * <tr><td>NBA208-6</td><td>Version 7</td><td>Performance logging</td></tr>
 * <tr><td>NBA208-15</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 15 - Avoid holding inquiry during Contract approve decline</td></tr>
 * <tr><td>NBA208-26</td><td>Version 7</td><td>Remove synchronized keyword from getLogger() methods</td></tr>
 * <tr><td>NBA195</td><td>Version 7</td><td>JCA Adapter for DXE Interface to CyberLife</td></tr>  
 * <tr><td>NBA208-22</td><td>Version 7</td><td>Performance Changes for Batch Updates</td></tr>
 * <tr><td>SPR3407</td><td>Version 7</td><td>NBCWA work item went to END queue with status CWANOTSGND when quality check question was 'yes'</td></tr>
 * <tr><td>NBA176</td><td>Version 7</td><td>Annuity Application Entry Rewrite</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.38</td><td>AXA Life Phase 1</td><td>Policy Product For Life (PPfL)</td></tr>
 * <tr><td>AXAL3.7.23</td><td>AXA Life Phase 1</td><td>Accounting interface (CBANC)</td></tr>
 *  <tr><td>AXAL3.7.25</td><td>AXA Life Phase 2</td><td>Client Interface</td></tr>
 * <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr>  
 * <tr><td>NBA232</td><td>Version 8</td><td>nbA Feed for a Customer's Web Site</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 *  <tr><td>NBA230</td><td>Version 8</td><td>nbA Reopen Date Enhancement Project</td></tr>
 * <tr><td>ALS4441</td><td>AXA Life Phase 1</td><td>#QC2365 - Multiple policies are being created in nbA for the same insured</td></tr>
 * <tr><td>SR494086.6</td><td>Discretionary</td><td>ADC Retrofit</td></tr> 
 * <tr><td>P2AXAL005</td><td>AXA Life Phase 2</td><td>Legal Policy Stop</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
*/
public class NbaContractAccess  {
	protected static NbaLogger logger = null;
	private List connections = new ArrayList(); //NBA187
	/**
	 * Update contract data in the nbA contract database.
	 * @param nbaTXLife the original NbaTXLife object created and returned by
	 * the doContractInquiry() method
	 * @param work an NbaDst object containing information about the work item
	 * being processed
	 * @param user an NbaUserVO containing information about the requesting user 
	 * @return the NbaTXLife object response information included.  The action indicators
	 * for affected objects will be reset by the process.
	 * @throws NbaContractAccessException
	 * @throws NbaBaseException
	 */
	protected static NbaTXLife updateDatabaseContract(NbaTXLife nbaTXLife, NbaDst nbaDst, NbaUserVO user)
		throws NbaContractAccessException, NbaBaseException {
	    long startTimeInMs = System.currentTimeMillis(); //NBA208-6
	    NbaPerformanceLogger.initMethod("updateDatabaseContract");//NBA208-6
		//NBA064 code deleted
		NbaActionIndicator action = new NbaActionIndicator();
		boolean contractChange = isContractChange(nbaTXLife); //NBA077
		boolean PROC_ISSUE = isIssue(nbaTXLife, nbaDst.getNbaLob()); //SPR2817
		if (isSubmit(nbaTXLife) || contractChange) { //NBA077
			new NbaOLifEId(nbaTXLife).resetIds(nbaTXLife);
			action.setAdd();
		} else {
			new NbaOLifEId(nbaTXLife).assureId(nbaTXLife);
			action.setDisplay();
		}
		//AXAPerformanceLogging
		long valStartTimeinMs = System.currentTimeMillis();
		new NbaContractValidation().validate(nbaTXLife, nbaDst, user); 
		NbaPerformanceLogger.logElapsed("[CONTRACTVALIDATION]",valStartTimeinMs);
//		long transValStartTimeinMs = System.currentTimeMillis();//P2AXAL016
//	    NbaTransactionValidationFacade.validateBusinessProcess(nbaTXLife, nbaDst, user);//NBA094//P2AXAL016
//	    NbaPerformanceLogger.logElapsed("[TRANSACTIONCONTRACTVALIDATION]",transValStartTimeinMs);//P2AXAL016
		//AXAL3.7.07 begin
		if ((NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ENABLE_DATA_CHANGE_CALLS))
			&& (!(NbaConstants.GI_CASE.equalsIgnoreCase(nbaDst.getNbaLob().getAppProdType())))){// NBLXA-188(APSL5318) Legacy Decommissioning
			long dataChangeStartTimeInMs = System.currentTimeMillis(); //NBA208-6
			AxaDataChangeManager changeManager = new AxaDataChangeManager();
			changeManager.determineDataChange(nbaTXLife, user, nbaDst);
			NbaPerformanceLogger.logElapsed("[AXADATACHANGE]",dataChangeStartTimeInMs); //NBA208-6
		}
		//AXAL3.7.07 end
	    //begin NBA137
        //	begin APSL2413
		/*if (!NbaConstants.PROC_FINAL_DISPOSITION.equalsIgnoreCase(nbaTXLife.getBusinessProcess())//FinalDisposition process handles Agent Advance Chargebacks
                && NbaConfiguration.getInstance().isAgentAdvanceSupported() && nbaDst.isCase()) {
            NbaAgentAdvancesRequestor agentAdvancesRequestor = new NbaAgentAdvancesRequestor();
            agentAdvancesRequestor.processAgentAdvances(nbaTXLife, nbaDst.getNbaLob(), NbaConfigurationConstants.WEBSERVICE_FUNCTION_AGT_ADV);
        }*/
        //	end APSL2413
		//end NBA137		
		//SPR2817 code deleted
		//SPR1656 Code Deleted
		NbaLob nbaLob = nbaDst.getNbaLob();
		Connection pendConn = null; //NBA102
		Connection auditConn = null; //NBA102
		ArrayList connections = new ArrayList(); //SPR1656
		String dbResult = NbaActionIndicator.ACTION_FAILED;
		TransResult transResult = new TransResult();
		try {
			if (!nbaTXLife.isPaidReIssue()){ //QC10618/APSL2792
				createExtracts(nbaTXLife, nbaDst, PROC_ISSUE, user); //SPR1656 SPR2817 AXAL3.7.23 NBA228,APSL2440 Internal Perfomance Issue
			}	
			//Begin QC13592/APSL4000
			if (nbaTXLife.isPaidReIssue()){ 
				new NbaCwaPaymentsExtract().createAccountingExtractForPaidReissue(nbaTXLife, nbaDst, PROC_ISSUE, user); 
			}
			//End QC13592/APSL4000
			pendConn = NbaConnectionManager.borrowConnection(NbaConfiguration.PENDING_CONTRACT); //NBA102
			pendConn.setAutoCommit(false); //NBA102
			//begin NBA077
			if (contractChange) {
				deleteContract(nbaDst, user, pendConn); //NBA102
			}
			//end NBA077
			connections.add(pendConn);//SPR1656 //NBA102
            NbaAuditingContext.addAuditingContext(nbaTXLife, user); //NBA102
            //begin SPR1931 SPR2817
			if (PROC_ISSUE) { //NBA126, SPR2817, APSL459
				//Create XML transaction for Reinstatement/Increase/Issue and invoke Web Service to process
				boolean isReadyForIssueToCAPS = false;
				ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(nbaTXLife.getPolicy().getApplicationInfo());
				if (!appInfoExtn.getIssuedToAdminSysInd()) {
					isReadyForIssueToCAPS = true;
				}
				NbaTXLife issueResponse = new NbaIssueStandalone(isReadyForIssueToCAPS).processStandalone(user, nbaDst, nbaTXLife);
				if (issueResponse != null && issueResponse.isTransactionError()) {
					rollbackConnections(connections);
					return issueResponse;
				}
				appInfoExtn.setIssuedToAdminSysInd(true);
			}
			//end SPR1931 SPR2817
            nbaTXLife.getOLifE().processChanges(nbaLob.getBackendSystem(), nbaLob.getCompany(), nbaLob.getPolicyNumber(), action, pendConn); //NBA102
            // NBA208-18 line deleted
            // begin NBA208-22
            if (NbaConfiguration.isConfigurationEnabledBatch() && NbaDatabaseUtils.isBatchEnabled(pendConn)) {
                Statement stmt = NbaBatchProcessingContext.getStatementForBatch(nbaLob.getPolicyNumber(), nbaLob.getBackendSystem(), nbaLob
                        .getCompany());
                if (stmt != null) {
                    stmt.executeBatch();
                    NbaBatchProcessingContext.clear(nbaLob.getPolicyNumber(), nbaLob.getBackendSystem(), nbaLob.getCompany());
                }
            }
            //end NBA208-22
            transResult = createTransactionResult();
            addResultResponse(nbaTXLife, transResult);
            //begin NBA093
			if (nbaDst.getID() != null) {
				//ID is null if the DST is not a real DST
			    NbaCorrespondenceEvents events = new NbaCorrespondenceEvents(user); //NBA146
			    events.validateDatabaseEvent(nbaTXLife, nbaDst);
			    events.createWorkItems();
				//NBP001 begin
			    NbaNbproducerEvents nbPevents = new NbaNbproducerEvents();
			    nbPevents.updateNbpendingInfoDatabaseForDatabaseEvent(nbaTXLife, nbaDst);
				//NBP001 end					
			}
			//end NBA093
			//SPR1656 line deleted
			//begin NBA102
			if(!NbaConfigurationConstants.NO_LOGGING.equals((NbaConfiguration.getInstance().getAuditConfiguration().getAuditLevel()))) {
				NbaAuditorVO auditorVO = NbaAuditingContext.getTxnAuditObjectsFromStack(nbaTXLife); //NBA208-18
				auditConn = NbaConnectionManager.borrowConnection(NbaConfiguration.AUDITING_CONTRACT);
				auditConn.setAutoCommit(false);
				connections.add(auditConn);
			    NbaAuditorFactory.getInstance().getAuditor().audit(auditorVO,auditConn);
			    //begin NBA208-22
			    if (NbaConfiguration.isConfigurationEnabledBatch() && NbaDatabaseUtils.isBatchEnabled(auditConn)) {
			        Statement stmt = NbaBatchProcessingContext.getStatementForBatch(nbaLob.getPolicyNumber(), nbaLob.getBackendSystem(), nbaLob
                           .getCompany());
			        if (stmt != null) {
			            stmt.executeBatch();
			            NbaBatchProcessingContext.clear(nbaLob.getPolicyNumber(), nbaLob.getBackendSystem(), nbaLob.getCompany());
			        }
			    }
                //end NBA208-22
            }//end NBA102
			//APSL459 Timing issue Code moved before processChanges
			commitConnections(connections); //SPR1656
			dbResult = NbaActionIndicator.ACTION_SUCCESSFUL;	
		} catch (NbaDataAccessException ndae) {
			transResult = createTransactionResult(ndae);
			addResultResponse(nbaTXLife, transResult);
			//SPR1656 code deleted
			rollbackConnections(connections); //SPR1656
		} catch (NbaBaseException nbe) {
			//SPR1656 code deleted
			//begin SPR1656
			transResult = createExceptionResult(nbe.getMessage());
			addResultResponse(nbaTXLife, transResult);
			rollbackConnections(connections);
			throw nbe;
			//end SPR1656
		} catch (SQLException e) {
			getLogger().logException(e); //NBA103 SPR2992
			//SPR1656 code deleted
			//begin SPR1656
			transResult = createExceptionResult(e.getMessage());
			addResultResponse(nbaTXLife, transResult);
			rollbackConnections(connections);
			//end SPR1656
		} finally {
			try {
				//SPR1656 line deleted
				NbaConnectionManager.closeConnections(connections); //SPR1656
			} catch (SQLException sqle) {
				throw new NbaContractAccessException(NbaBaseException.CLOSE_CONNECTIONS_FAILED, sqle);
			}
			NbaAuditingContext.clearTxnObjectStack(nbaTXLife); //NBA102
		}
		if (dbResult.equals(NbaActionIndicator.ACTION_SUCCESSFUL)) {
			nbaTXLife.getOLifE().applyResult(dbResult, null);
			//SPR1656 line deleted
			//begin NBA232
			if (isDataFeedOn() && isDataFeedNeeded(nbaTXLife, nbaDst) &&
					(!(NbaConstants.GI_CASE.equalsIgnoreCase(nbaDst.getNbaLob().getAppProdType())))) { //NBLXA-2141 SR494086.6 ADC Retrofit
				List dataFeedList = new ArrayList();
				dataFeedList.add(nbaTXLife);
				dataFeedList.add(user);
				dataFeedList.add(nbaLob.getOperatingMode());
				submitDataFeed(dataFeedList);
			}
			//end NBA232
		} else {
			List resultList = new ArrayList();
			resultList.add(transResult.getResultInfoAt(0));
			// Send only the first
			nbaTXLife.getOLifE().applyResult(dbResult, resultList);
		}
		NbaPerformanceLogger.logElapsed("[Database]",startTimeInMs); //NBA208-6
		NbaPerformanceLogger.removeMethod();//NBA208-6
		return nbaTXLife;
	}
	/**
	 * Invokes the data feed business process that begins the data feed process
	 * @throws NbaBaseException
	 */
	//NBA232 new method
	private static void submitDataFeed(List dataList) throws NbaBaseException {
		try { //AXAL3.7.54
        AccelResult accelResult = new AccelResult();
        accelResult.merge(ServiceHandler.invoke("NbaDataFeedBP", ServiceContext.currentContext(), dataList)); 
        checkOutcome(accelResult);
		} catch (Exception e) {   //AXAL3.7.54
			getLogger().logError("Error feeding request to JMS Service Bus");   //AXAL3.7.54
		} //AXAL3.7.54
	}
	/**
	 * @param accelResult
	 */
    /**
     * Check the outcome of a Service Action. If any accelerator errors are present wrapper the errors in a fatal NbaBaseException.
     * If any error messages are present in the response, log any informatial, warning, or error severity 
     * messages. Wrapper any fatal severity messages in a fatal NbaBaseException.
     * @param resultParams
     * @param outcome
     * @throws NbaBaseException
     */
	//NBA232 new method
	private static void checkOutcome(AccelResult accelResult) throws NbaBaseException {
    	if (accelResult.hasErrors()) {  
    	    WorkflowServiceHelper.checkOutcome(accelResult);
    	} else { 
            Object obj = accelResult.getFirst();
            if (obj != null) {
                NbaDataFeedDO datafeedDO = (NbaDataFeedDO) obj;
// TO DO Add code here to evaluate the datafeedDO
            }
        }
    }
	/**
	 * Evaluates the CustomerDataFeed attribute in the BusinessRules of the NbaConfiguration.xml file 
	 * to determine if data feed should be transmitted.
	 * @return boolean true if CustomerDataFeed attribute=ON; otherwise, false
	 */
	// NBA232 new method
	private static boolean isDataFeedOn() {
		try {
			return NbaConstants.ON_VALUE.equalsIgnoreCase(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.CUSTOMER_DATAFEED));
		} catch (NbaBaseException nbe) {
            // return false if configuration could not be found
            return false;
		}
	}
	/**
	 * Update contract data in the back end system.
	 * @param nbaTXLife the original NbaTXLife object created and returned by
	 * the doContractInquiry() method
	 * @param nbaDst an NbaDst object containing information about the work item
	 * being processed
	 * @param user an NbaUserVO containing information about the requesting user 
	 * @return the NbaTXLife object response information included.  The action indicators
	 * for affected objects will be reset by the process.
	 * @throws NbaContractAccessException
	 * @throws NbaBaseException
	 */
	protected static NbaTXLife updateBackEndContract(NbaTXLife nbaTXLife, NbaDst nbaDst, NbaUserVO user)
		throws NbaContractAccessException, NbaBaseException {
		boolean contractChange = isContractChange(nbaTXLife); //NBA077
		NbaLob nbaLob = nbaDst.getNbaLob(); //NBA208-22
		if (isSubmit(nbaTXLife) || contractChange) { //NBA077
			new NbaOLifEId(nbaTXLife).resetIds(nbaTXLife);
			nbaTXLife.getOLifE().setActionAdd();
		} else {
			new NbaOLifEId(nbaTXLife).assureId(nbaTXLife);
		}
		nbaTXLife.getOLifE().setKeys(getArgs(nbaDst));
		NbaTXLife newNbaTXLife = null;
		Connection pendConn = null; //NBA102
		Connection auditConn = null; //NBA102
		ArrayList connections = new ArrayList(); //NBA102
		String dbResult = NbaActionIndicator.ACTION_SUCCESSFUL;
		TransResult transResult = new TransResult();
		try {
			//begin NBA102
			pendConn = NbaConnectionManager.borrowConnection(NbaConfiguration.PENDING_CONTRACT);
			pendConn.setAutoCommit(false);
			connections.add(pendConn);
			NbaAuditingContext.addAuditingContext(nbaTXLife,user);
			//end NBA102
			//begin NBA077
			if (contractChange) {
				deleteDataForWrapperedContract(nbaDst, user, pendConn); //NBA102
			}
			//end NBA077
			updateDbOnly(nbaTXLife, pendConn); //NBA102
			// NBA208-18 line deleted
			//begin NBA102
			if (!NbaConfigurationConstants.NO_LOGGING.equals((NbaConfiguration.getInstance().getAuditConfiguration().getAuditLevel()))) {
                NbaAuditorVO auditorVO = NbaAuditingContext.getTxnAuditObjectsFromStack(nbaTXLife);//NBA102 NBA208-18
                auditConn = NbaConnectionManager.borrowConnection(NbaConfiguration.AUDITING_CONTRACT);
                auditConn.setAutoCommit(false);
                connections.add(auditConn);
                NbaAuditorFactory.getInstance().getAuditor().audit(auditorVO, auditConn);
                // begin NBA208-22
                if (NbaConfiguration.isConfigurationEnabledBatch() && NbaDatabaseUtils.isBatchEnabled(pendConn)) {
                    Statement stmt = NbaBatchProcessingContext.getStatementForBatch(nbaLob.getPolicyNumber(), nbaLob.getBackendSystem(), nbaLob
                            .getCompany());
                    if (stmt != null)
                        stmt.executeBatch();
                }
                // end NBA208-22
            }
            // end NBA102
            try {
				newNbaTXLife =
					NbaBackendTransactionProcesser.processContractUpdate(nbaTXLife, createRequestObject(nbaDst, user));
                // begin NBA208-22
				if (NbaConfiguration.isConfigurationEnabledBatch() && NbaDatabaseUtils.isBatchEnabled(auditConn)) {
                    Statement stmt = NbaBatchProcessingContext.getStatementForBatch(nbaLob.getPolicyNumber(), nbaLob.getBackendSystem(), nbaLob
                            .getCompany());
                    if (stmt != null) {
                        stmt.executeBatch();
                        NbaBatchProcessingContext.clear(nbaLob.getPolicyNumber(), nbaLob.getBackendSystem(), nbaLob.getCompany());
                    }
                }
                //end NBA208-22
				//begin NBA093
				if (!newNbaTXLife.isTransactionError()) {
					NbaCorrespondenceEvents events = new NbaCorrespondenceEvents(user); //NBA146
					events.validateHostEvent(nbaTXLife, newNbaTXLife, nbaTXLife.getWorkitemId());
					events.createWorkItems();
					//NBP001 begin
					NbaNbproducerEvents nbPevents = new NbaNbproducerEvents();
					nbPevents.updateNbpendingInfoDatabaseForHostEvent(nbaTXLife, newNbaTXLife, nbaDst);
				}
				//end NBA093
				//NBP001 end
			} catch (NbaBaseException nbe) {
				rollbackConnections(connections); //NBA102
			}
			//begin NBA093
			if (newNbaTXLife.isTransactionError()) {
				rollbackConnections(connections); //NBA102
			} else {
				commitConnections(connections); //NBA102
			}
			//end NBA093		
		} catch (NbaDataAccessException ndae) {
			transResult = createTransactionResult(ndae);
			dbResult = NbaActionIndicator.ACTION_FAILED;
			rollbackConnections(connections); //NBA102
		} catch (SQLException e) {
			getLogger().logException(e); //NBA103 SPR2992
			rollbackConnections(connections); //NBA102
		} finally {
			try {
				NbaConnectionManager.closeConnections(connections); //NBA102
			} catch (SQLException sqle) {
				throw new NbaContractAccessException(NbaBaseException.RETURN_CONNECTION_FAILED, sqle);
			}
			NbaAuditingContext.clearTxnObjectStack(nbaTXLife); //NBA102
		}
		if (newNbaTXLife == null) {
			newNbaTXLife = nbaTXLife;
		}
		//Only add a ResultResponse for database failures. If no failure, assume that 
		//NbaBackendTransactionProcesser has already added ResultResponse(s) based 
		//on its successes or failures.
		if (dbResult.equals(NbaActionIndicator.ACTION_FAILED)) {
			addResultResponse(newNbaTXLife, transResult);
			List resultList = new ArrayList();
			resultList.add(transResult.getResultInfoAt(0));
			// Send only the first
			newNbaTXLife.getOLifE().applyResult(dbResult, resultList);
		}
		return newNbaTXLife;
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
		 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() { // NBA208-26
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractAccess.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractAccess could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Creates a request object that will be used to perform an update to the contract.
	 * Request is manufactured using input values, NbaLob object and User objectdefault values.
	 * @param transMode indicates the type of transaction (add, update, delete, etc.)
	 * @param transContentCode indicates the contents of the transaction (insert, update, etc)
	 * @return an NbaTXRequestVO with initialized values
	 * @throws NbaBaseException
	 */
	public static NbaTXRequestVO createRequestObject(NbaDst work, NbaUserVO user) throws NbaBaseException {
		try {
			NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
			nbaTXRequest.setNbaLob(work.getNbaLob());
			nbaTXRequest.setNbaUser(user);
			nbaTXRequest.setWorkitemId(work.getID()); //SPR1715
			nbaTXRequest.setCaseInd(work.isCase()); //ACN014
			return nbaTXRequest;
		} catch (Throwable t) {
			NbaBaseException e = new NbaBaseException(t);
			getLogger().logException(t);
			throw e;
		}
	}
	/**
	 * This method retrieves contract data from the appropriate datastore.
	 * When the contract has two sources (a primary and secondary datastore),
	 * information is retrieved from both.
	 * @param nbaTXRequest an NbaTXRequestVO object containing the type of transaction
	 * to be executed, along with values used to locate the data.
	 * @param nbaTXLife an NbaTXLife object which will be updated with a response if
	 * the contract is unavailable
	 * @return the value returned from the call to the checkContractAvailability() method
	 * @throws NbaBaseException
	 */
	protected static boolean contractAvailable(NbaTXRequestVO nbaTXRequest, NbaTXLife nbaTXLife) throws NbaBaseException {
		NbaContractLockData lockData = new NbaContractLockData(nbaTXRequest.getNbaLob(), nbaTXRequest.getNbaUser());
		return checkContractAvailability(lockData, nbaTXLife, nbaTXRequest.getAccessIntent());
	}
	/**
	 * This method determines if the contract is available for read or update.
	 * @param nbaTXLife an NbaTXLife object containing basic contract information. 
	 * It will will be updated with a response if the contract is unavailable.
	 * @return the value returned from the call to the checkContractAvailability() method
	 * @throws NbaBaseException
	 */
	protected static boolean contractAvailable(NbaTXLife nbaTXLife) throws NbaBaseException {
		NbaContractLockData lockData =
			new NbaContractLockData(
				nbaTXLife.getPrimaryHolding().getPolicy().getPolNumber(),
				nbaTXLife.getPrimaryHolding().getPolicy().getCarrierCode(),
				nbaTXLife.getOLifE().getSourceInfo().getFileControlID(),
				nbaTXLife.getBusinessProcess());
		return checkContractAvailability(lockData, nbaTXLife, nbaTXLife.getAccessIntent());
	}
	/**
	 * This method determines if the contract is available for read or update.
	 * @param nbaDst an AWD work item containing identifying information
	 * @param nbaUser an AWD user
	 * @param nbaTXLife an NbaTXLife object which will be updated with a response if
	 * the contract is unavailable
	 * @return the value returned from the call to the checkContractAvailability() method
	 * @throws NbaBaseException
	 */
	protected static boolean contractAvailable(NbaDst nbaDst, NbaUserVO nbaUser, NbaTXLife nbaTXLife)
		throws NbaBaseException {
		NbaContractLockData lockData = new NbaContractLockData(nbaDst.getNbaLob(), nbaUser);
		return checkContractAvailability(lockData, nbaTXLife, nbaTXLife.getAccessIntent());
	}
	/**
	 * This method checks to see if the contract is available for update.  If not, it
	 * creates an TXLifeResponse object and add its to the nbaTXLife object.
	 * @param lockData an NbaContractLockData object containing information
	 * necessary to query the database table.
	 * @param nbaTXLife an NbaTXLife object that will be updated if the contract
	 * is unavailable
	 * @return <code>true</code> if the contract is available; <code>false</code> if not
	 * @throws NbaBaseException
	 */
	protected static boolean checkContractAvailability(NbaContractLockData lockData, NbaTXLife nbaTXLife, int accessIntent)
		throws NbaBaseException {
		try {
			NbaContractLock.processLockRequest(lockData, accessIntent);
			if (!lockData.isLockedForUser() && accessIntent == NbaConstants.UPDATE) {
				TXLife txLife = null;
				UserAuthResponseAndTXLifeResponseAndTXLifeNotify response = null;
				TXLifeResponse txLifeResponse = null;
				//NBA093 code deleted
				txLife = nbaTXLife.getTXLife(); //NBA093
				response = txLife.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
				//NBA093
				txLifeResponse = response.getTXLifeResponseAt(0); //NBA093
				//NBA093 code deleted
				TransResult transResult = new TransResult();
				transResult.setResultCode(5);
				ResultInfo resultInfo = new ResultInfo();
				resultInfo.setResultInfoCode(999);
				resultInfo.setResultInfoDesc("Contract is in use by user " + lockData.getLockedBy());
				//SPR1851
				transResult.addResultInfo(resultInfo);
				txLifeResponse.setTransResult(transResult);
				return false;
			}
			return true;
		} catch (NbaDataAccessException ndae) {
			throw new NbaBaseException("Contract is in use by user " + lockData.getUserId(), ndae);
		}
	}
	/**
	 * This method retrieves contract data from the appropriate datastore.
	 * When the contract has two sources (a primary and secondary datastore),
	 * information is retrieved from both.
	 * @param nbaTXRequest a request object containing the type of transaction
	 * to be executed, along with values used to locate the data.
	 * @return an NbaTXLife with the newly retrieved data and values
	 * @throws NbaBaseException
	 */
	public static NbaTXLife doContractInquiry(NbaTXRequestVO nbaTXRequest) throws NbaBaseException {
		try {
			getLogger().logDebug("doContractInquiry in Contrcat Access--"+nbaTXRequest.getBusinessProcess());
			ArrayList results = new ArrayList(); //NBA093
			NbaTXLife nbaTXLife; //NBA093
			//begin SPR1715
			boolean isStandalone = false;
			if (nbaTXRequest.getOverrideDataSource() == null) {
				isStandalone = NbaServerUtility.isDataStoreDB(nbaTXRequest.getNbaLob(), nbaTXRequest.getNbaUser());
			} else if (nbaTXRequest.getOverrideDataSource().equals(NbaConstants.STANDALONE)) {
				isStandalone = true;
			} else if (nbaTXRequest.getOverrideDataSource().equals(NbaConstants.WRAPPERED)) {
				isStandalone = false;
			} else {
				throw new NbaBaseException("Not a valid data store source");
			}
			getLogger().logDebug("doContractInquiry in Contrcat Access :: isStandalone--"+isStandalone);
			//end SPR1715
			if (isStandalone) { //NBA091 //SPR1715
				OLifE olife = new OLifE(); //NBA093
				results.add(olife); //NBA093
				getLogger().logDebug("doContractInquiry in Contrcat Access :: Before Response From Query--");
				nbaTXLife = nbaTXLife = responseFromQuery(nbaTXRequest, results);
				getLogger().logDebug("doContractInquiry in Contrcat Access :: nbaTXLife--"+nbaTXLife);
				//NBA093
				if (!contractAvailable(nbaTXRequest, nbaTXLife)) {
					getLogger().logDebug("doContractInquiry in Contrcat Access :: nWhen Contract is not available--");
					return nbaTXLife;
				}
				String[] args = getArgs(nbaTXRequest);
				getLogger().logDebug("doContractInquiry in Contrcat Access :: args--"+args);
				results = NbaContractDataBaseAccessor.getInstance().selectOLifE(args);
				getLogger().logDebug("doContractInquiry in Contrcat Access :: results--"+results);
				// getLogger().logDebug("doContractInquiry in Contrcat Access :: results size--"+results.size());
				//NBA093
				if (results == null || results.size() < 1) {
				    //begin NBA208
				    NbaDataAccessException ndae = new NbaDataAccessException("Unable to retrieve information from Contract Database for " + args[1]);
                    ndae.markAsLogged();
                    throw ndae;
				}
				nbaTXLife = responseFromQuery(nbaTXRequest, results);
  				//end NBA208
			} else {
				getLogger().logDebug("doContractInquiry in Contrcat Access :: if not stand alone");
				nbaTXLife = new NbaTXLife(nbaTXRequest);
				nbaTXLife = new NbaBackEndAdapterFacade().submitRequestToHost(nbaTXLife, nbaTXLife.getWorkitemId());
				updateResponseFromRequest(nbaTXRequest, nbaTXLife);
				if (wasSuccessful(nbaTXLife)) { //NBA093
					getLogger().logDebug("doContractInquiry in Contrcat Access :: wasSuccessful");
					mergeDbOnlyIntoResponse(nbaTXLife); //NBA093
					getLogger().logDebug("doContractInquiry in Contrcat Access :: after mergeDbOnlyIntoResponse");
				} //NBA093
			}
			getLogger().logDebug("doContractInquiry in Contrcat Access :: Final nbaTXLife"+nbaTXLife);
			return nbaTXLife;
		} catch (NbaBaseException e) {
			getLogger().logException(e);	//SPR2992
			throw e;
		} catch (Throwable t) {
			NbaBaseException e = new NbaBaseException(t);
			getLogger().logException(t);	//SPR2992
			throw e;
		}
	}
	/**
	 * This method retrieves contract data from the appropriate datastore.
	 * When the contract has two sources (a primary and secondary datastore),
	 * information is retrieved from both.
	 * @param nbaTXLifeRequest a request object containing the type of transaction
	 * @param userVO NbaUserVO
	 * @param nbalob NbaLob
	 * to be executed, along with values used to locate the data.
	 * @return an NbaTXLife with the newly retrieved data and values
	 * @throws NbaBaseException
	 */
	//QC 4373 APSL 178 new method 
	public static NbaTXLife doContractInquiry(NbaTXLife nbaTXLifeRequest, NbaUserVO userVO, NbaLob nbalob) throws NbaBaseException {
		try {
			NbaTXLife nbaTXLife = null;
			if (NbaServerUtility.isDataStoreDB(nbalob, userVO)) { 
				if (!contractAvailable(nbaTXLifeRequest)) {
					return nbaTXLife;
				}
				String[] args = getArgs(nbaTXLifeRequest);
				ArrayList results = NbaContractDataBaseAccessor.getInstance().selectOLifE(args);
				if (results == null || results.size() < 1) {
					throw new NbaContractAccessException(
						"Unable to retrieve information from Contract Database for " + args[1]);
				} else {
					nbaTXLife = responseFromQuery(nbaTXLifeRequest, results);
				}
			} else {
				nbaTXLife = new NbaBackEndAdapterFacade().submitRequestToHost(nbaTXLifeRequest, (String) null);
				nbaTXLife.setAccessIntent(nbaTXLifeRequest.getAccessIntent());
				nbaTXLife.setBusinessProcess(nbaTXLifeRequest.getBusinessProcess());
				nbaTXLife.setWorkitemId(nbaTXLifeRequest.getWorkitemId());
				if (wasSuccessful(nbaTXLife)) { //NBA093
					mergeDbOnlyIntoResponse(nbaTXLife); //NBA093
				} //NBA093
			}
			return nbaTXLife;
		} catch (NbaBaseException e) {
			getLogger().logException(e);	//SPR2992
			throw e;
		} catch (Throwable t) {
			NbaBaseException e = new NbaBaseException(t);
			getLogger().logException(t);	//SPR2992
			throw e;
		}
	}
	/**
	 * Determine if a sucessful response was returned.
	 * @param NbaTXLife
	 * @return boolean
	 */
	//NBA093 new method
	protected static boolean wasSuccessful(NbaTXLife nbaTXLife) {
		boolean wasSuccessful = true;
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify allResponses =
			nbaTXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		if (allResponses != null) {
			for (int i = 0; i < allResponses.getTXLifeResponseCount(); i++) {
				TXLifeResponse theResponse = allResponses.getTXLifeResponseAt(i);
				TransResult aTransResult = theResponse.getTransResult();
				if (aTransResult.getResultCode() > 1) {
					wasSuccessful = false;
					break;
				}
			}
		}
		return wasSuccessful;
	}
	/**
	 * This method retrieves contract data from the appropriate datastore.
	 * When the contract has two sources (a primary and secondary datastore),
	 * information is retrieved from both.
	 * @param nbaTXLifeRequest a request object containing the type of transaction
	 * to be executed, along with values used to locate the data.
	 * @return an NbaTXLife with the newly retrieved data and values
	 * @throws NbaBaseException
	 */
	//NBA103
	public static NbaTXLife doContractInquiry(NbaTXLife nbaTXLifeRequest) throws NbaBaseException {
		try {
			NbaTXLife nbaTXLife = null;
			if (NbaServerUtility.isDataStoreDB(nbaTXLifeRequest)) { //NBA091
				if (!contractAvailable(nbaTXLifeRequest)) {
					return nbaTXLife;
				}
				String[] args = getArgs(nbaTXLifeRequest);
				ArrayList results = NbaContractDataBaseAccessor.getInstance().selectOLifE(args);
				if (results == null || results.size() < 1) {
					throw new NbaContractAccessException(
						"Unable to retrieve information from Contract Database for " + args[1]);
				} else {
					nbaTXLife = responseFromQuery(nbaTXLifeRequest, results);
				}
			} else {
				nbaTXLife = new NbaBackEndAdapterFacade().submitRequestToHost(nbaTXLifeRequest, (String) null);
				nbaTXLife.setAccessIntent(nbaTXLifeRequest.getAccessIntent());
				nbaTXLife.setBusinessProcess(nbaTXLifeRequest.getBusinessProcess());
				nbaTXLife.setWorkitemId(nbaTXLifeRequest.getWorkitemId());
				if (wasSuccessful(nbaTXLife)) { //NBA093
					mergeDbOnlyIntoResponse(nbaTXLife); //NBA093
				} //NBA093
			}
			return nbaTXLife;
		} catch (NbaBaseException e) {
			getLogger().logException(e);	//SPR2992
			throw e;
		} catch (Throwable t) {
			NbaBaseException e = new NbaBaseException(t);
			getLogger().logException(t);	//SPR2992
			throw e;
		}
	}
	/**
	 * Creates a successful TransResult object to be added to the NbaTXLife object
	 * @return a newly created TransResult object
	 * @throws NbaBaseException
	 */
	protected static TransResult createTransactionResult() throws NbaBaseException {
		TransResult transResult = new TransResult();
		ArrayList aList = new ArrayList();
		transResult.setResultCode(0); // MEANS SUCCESS
		ResultInfo resultInfo = new ResultInfo();
		resultInfo.setResultInfoCode(999);
		resultInfo.setResultInfoDesc("Success");
		aList.add(resultInfo);
		transResult.setResultInfo(aList);
		return transResult;
	}
	/**
	 * Creates a TransResult object that contains information about the failure of the 
	 * contract update. 
	 * @param ndae an NbaDataAccessException containing the table on which the
	 * error occurred as well as exception information
	 * @return a newly created TransResult object
	 * @throws NbaBaseException
	 */
	protected static TransResult createTransactionResult(NbaDataAccessException ndae) throws NbaBaseException {
		TransResult transResult = new TransResult();
		ArrayList aList = new ArrayList();
		transResult.setResultCode(5); // MEANS FAILURE
		ResultInfo resultInfo = new ResultInfo();
		// add generic result first
		resultInfo.setResultInfoCode(999);
		if (ndae.getDbInfo() == null) {
			ndae.setDbInfo(new NbaContractDataBaseInfo());
		}
		resultInfo.setResultInfoDesc("Database update (" + ndae.getDbInfo().getUpdateType() + ") failed.");
		aList.add(resultInfo);
		// now add specific information
		resultInfo = new ResultInfo();
		resultInfo.setResultInfoCode(999);
		resultInfo.setResultInfoDesc(
			"Database update ("
				+ ndae.getDbInfo().getUpdateType()
				+ ") for "
				+ ndae.getDbInfo().getTable()
				+ " for refId "
				+ ndae.getDbInfo().getRefId()
				+ " failed.");
		aList.add(resultInfo);
		resultInfo = new ResultInfo();
		if (ndae.getCause() != null) {
			resultInfo.setResultInfoCode(ndae.getCause().getMessage().substring(0, 9));
			if (ndae.getCause().getMessage().indexOf("OLIFE_PK") != -1) {
				resultInfo.setResultInfoDesc("NBA00001 " + ndae.getCause().getMessage());
			} else {
				resultInfo.setResultInfoDesc(ndae.getCause().getMessage());
			}
		} else {
			resultInfo.setResultInfoCode(999);
			resultInfo.setResultInfoDesc("Reason:  " + ndae.getMessage());
		}
		aList.add(resultInfo);
		transResult.setResultInfo(aList);
		return transResult;
	}
	/**
	 * Answer a String[] containing the arguments for a contract inquiry.
	 * @param nbaTXRequest a request object containing the type of transaction
	 * @return String[] containing the arguments used to access the contract.
	 */
	protected static String[] getArgs(NbaTXRequestVO nbaTXRequest) {
		return getArgs(nbaTXRequest.getNbaLob());
	}
	/**
	 * Answer a String[] containing the arguments for a contract update.
	 * @param nbaTXRequest a request object containing the type of transaction
	 * @return String[] containing the arguments used to access the contract.
	 */
	protected static String[] getArgs(NbaDst nbaDst) {
		return getArgs(nbaDst.getNbaLob());
	}
	/**
	 * Answer a String[] containing the arguments for a contract.
	 * @param nbaLob  
	 * @return String[] containing the arguments used to access the contract.
	 */
	protected static String[] getArgs(NbaLob nbaLob) {
		String[] args = new String[4];
		getLogger().logDebug("Policy no from getArgs --"+nbaLob.getPolicyNumber());
		getLogger().logDebug("Company Code from getArgs ---"+nbaLob.getCompany());
		getLogger().logDebug("back End System from getArgs -- "+nbaLob.getBackendSystem());
		args[0] = (new OLifE()).getId();
		args[1] = nbaLob.getPolicyNumber();
		args[2] = nbaLob.getCompany();
		args[3] = nbaLob.getBackendSystem();
		return args;
	}
	/**
	 * Answer a String[] containing the arguments for a contract inquiry.
	 * @param nbaTXLifeRequest a request object containing the type of transaction
	 * @return String[] containing the arguments used to access the contract.
	 */
	protected static String[] getArgs(NbaTXLife nbaTXLife) {
		OLifE oLifE = nbaTXLife.getOLifE();
		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();
		String[] args = new String[4];
		args[0] = oLifE.getId();
		args[1] = policy.getPolNumber();
		args[2] = policy.getCarrierCode();
		args[3] = oLifE.getSourceInfo().getFileControlID();
		return args;
	}
	/**
	 * Answer a NbaTXLife containing the contract inquiry results.
	 * @param nbaTXRequest a request object containing the type of transaction
	 * @param results the query result objects
	 * @return an NbaTXLife with the newly retrieved data and values
	 */
	protected static NbaTXLife responseFromQuery(NbaTXRequestVO nbaTXRequest, ArrayList results) {
		getLogger().logDebug("responseFromQuery in Contrcat Access :: isStandalone--"+nbaTXRequest);
		getLogger().logDebug("responseFromQuery in Contrcat Access :: results--"+results);
		NbaTXLife nbaTXLife = new NbaTXLife(new NbaTXLife(nbaTXRequest));
		// Create response from request
		TXLife txLife = nbaTXLife.getTXLife();
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify response =
			txLife.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		TXLifeResponse txLifeResponse = response.getTXLifeResponseAt(0); // SPR3290
		txLifeResponse.setOLifE((OLifE) results.get(0));
		//Only one OLifE object
		updateResponseFromRequest(nbaTXRequest, nbaTXLife);
		getLogger().logDebug("After Update response From Request----"+nbaTXLife);
		return nbaTXLife;
	}
	/**
	 * Answer a NbaTXLife containing the contract inquiry results.
	 * @param nbaTXLifeRequest a request object containing the type of transaction
	 * @param results the query result objects
	 * @return an NbaTXLife with the newly retrieved data and values
	 */
	protected static NbaTXLife responseFromQuery(NbaTXLife nbaTXLifeRequest, ArrayList results) {
		NbaTXLife nbaTXLife = new NbaTXLife(nbaTXLifeRequest);
		// Create response from request
		TXLife txLife = nbaTXLife.getTXLife();
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify response =
			txLife.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		TXLifeResponse txLifeResponse = response.getTXLifeResponseAt(0); // SPR3290
		txLifeResponse.setOLifE((OLifE) results.get(0));
		//Only one OLifE object
		nbaTXLife.setAccessIntent(nbaTXLifeRequest.getAccessIntent());
		nbaTXLife.setBusinessProcess(nbaTXLifeRequest.getBusinessProcess());
		nbaTXLife.setWorkitemId(nbaTXLifeRequest.getWorkitemId());
		return nbaTXLife;
	}
	/**
	 * Update the NbaTXLife with values from the request.
	 * @param nbaTXRequest a request object containing the type of transaction
	 * @param nbaTXLife the NbaTXLife with the newly retrieved data and values
	 */
	protected static void updateResponseFromRequest(NbaTXRequestVO nbaTXRequest, NbaTXLife nbaTXLife) {
		nbaTXLife.setAccessIntent(nbaTXRequest.getAccessIntent());
		nbaTXLife.setBusinessProcess(nbaTXRequest.getBusinessProcess());
		nbaTXLife.setWorkitemId(nbaTXRequest.getWorkitemId());
	}
	/**
	 * This method updates contract data in the appropriate datastore.
	 * When the contract has two sources (a primary and secondary datastore),
	 * information is updated in both as needed.
	 * @param nbaTXLife the original NbaTXLife object created and returned by
	 * the doContractInquiry() method
	 * @param work an NbaDst object containing information about the work item
	 * being processed
	 * @param user an NbaUserVO containing information about the requesting user 
	 * @return the NbaTXLife object response information included.  The action indicators
	 * for affected objects will be reset by the process.
	 * @throws NbaContractAccessException
	 * @throws NbaBaseException
	 */
	//NBA103
	public static NbaTXLife doContractUpdate(NbaTXLife nbaTXLife, NbaDst nbaDst, NbaUserVO user)
		throws NbaContractAccessException, NbaBaseException {
		boolean status = false;//AXAL3.7.25
		try {
			if (nbaTXLife.getAccessIntent() != NbaConstants.UPDATE) {
				throw new NbaContractAccessException(NbaBaseException.CONTRACT_NOT_OPENED_FOR_UPDATE);
			}
			if (!isSubmit(nbaTXLife)) {
				if (!contractAvailable(nbaDst, user, nbaTXLife)) {
					return nbaTXLife;
				}
			}
			if (NbaServerUtility.isDataStoreDB(nbaDst.getNbaLob(), user)) { //NBA091 SPR1715 NBA077
				return updateDatabaseContract(nbaTXLife, nbaDst, user);
			} else {
				return updateBackEndContract(nbaTXLife, nbaDst, user);
			}
		} catch (NbaBaseException e) {
			getLogger().logException(e);	//SPR2992
			throw e;
		} catch (Throwable t) {
			NbaBaseException e = new NbaBaseException(t);
			getLogger().logException(t);	//SPR2992
			throw e;
		}
	}
	/**
	 * Return true if an application submit is being requested.
	 */
	protected static boolean isSubmit(NbaTXLife nbaTXLife) {
		if (NbaConstants.PROC_APP_SUBMIT.equalsIgnoreCase(nbaTXLife.getBusinessProcess())
				|| NbaConstants.PROC_SI_APP_SUBMIT.equalsIgnoreCase(nbaTXLife.getBusinessProcess())
				|| NbaConstants.PROC_GI_APP_SUBMIT.equalsIgnoreCase(nbaTXLife.getBusinessProcess())
				|| NbaConstants.PROC_GI_CASE_SUBMIT.equalsIgnoreCase(nbaTXLife.getBusinessProcess())) { //SPR2639, APSL2808 // NBLXA-188(APSL5318) Legacy Decommissioning
			TXLife tXLife = nbaTXLife.getTXLife();
			if (tXLife != null && tXLife.isUserAuthRequestAndTXLifeRequest()) {
				TXLifeRequest tXLifeRequest = tXLife.getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
				if (tXLifeRequest != null && tXLifeRequest.getTransType() == NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Return true if a contract change function is being requested.
	 */
	//NBA077 New Method
	protected static boolean isContractChange(NbaTXLife nbaTXLife) {
		return (NbaConstants.PROC_CONTRACT_CHANGE.equalsIgnoreCase(nbaTXLife.getBusinessProcess())); //SPR2639
	}
	/**
	 * Adds a result response to an NbaTXLife object.
	 * @param nbaTXLife the NbaTXLife object that will be updated
	 * @param transResult the TransResult object that contains the resposne information
	 */
	protected static void addResultResponse(NbaTXLife nbaTXLife, TransResult transResult) {
		// If this is for App Submit, remove the TXLife Request and add a TXLifeResponse with the results!
		TXLifeResponse txLifeResponse = null;
		if (nbaTXLife.getTXLife().isUserAuthRequestAndTXLifeRequest()) {
			nbaTXLife.getTXLife().removeContents();
			nbaTXLife.getTXLife().setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(
				new UserAuthResponseAndTXLifeResponseAndTXLifeNotify());
			txLifeResponse = new TXLifeResponse();
			nbaTXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().addTXLifeResponse(
				txLifeResponse);
		} else {
			txLifeResponse =
				nbaTXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0);
		}
		txLifeResponse.setTransResult(transResult);
	}
	/**
	 * Checks to see if the NbaTXLife object is available to be opened with UPDATE
	 * access.  If so, the NbaTXLife object is locked for the user.  
	 * @param nbaTXLife an NbaTXLife object to be locked
	 * @return a boolean value of <code>true</code> is returned if the object 
	 * has been successfully locked; else, <code>false</code> is returned.
	 * @throws NbaBaseException
	 */
	public static boolean requestUpdateAccess(NbaTXLife nbaTXLife) throws NbaBaseException {
		nbaTXLife.setAccessIntent(NbaConstants.UPDATE);
		return false;
	}
	/**
	 * Merge database only information into the NbaTXLife object.
	 * @param nbaTXLife the NbaTXLife object
	 * @throws NbaDataAccessException
	 * @throws NbaBaseException 
	 */
	protected static void mergeDbOnlyIntoResponse(NbaTXLife nbaTXLife) throws NbaDataAccessException, NbaBaseException {
		new NbaOLifEId(nbaTXLife).resetIds(nbaTXLife);
		OLifE oLifE = nbaTXLife.getOLifE();
		OLifE tempOLifE = new OLifE(true);
		//Initialize ghost objects to force database access
		String[] args = getArgs(nbaTXLife);
		oLifE.setKeys(args);
		oLifE.setFormInstanceGhost(null); //Allow to lazy initialize	//ACN005
		tempOLifE.setKeys(args);
		mergeDbOnlyHoldingInfo(oLifE, tempOLifE, nbaTXLife.getPrimaryHolding());
		mergeDbOnlyPartyInfo(oLifE, tempOLifE);
		mergeDbOnlyRelationInfo(oLifE, tempOLifE);	//ACN003
		mergeDbOnlyFormInstanceInfo(oLifE, tempOLifE);	//NBA211
	}
	/**
	 * Merge database only holding information into the nbaTXLife. 
	 * @param oLifE the NbaTXLife OLifE object
	 * @param tempOLifE the database OLifE object
	 * @throws NbaDataAccessException
	 */
	protected static void mergeDbOnlyHoldingInfo(OLifE oLifE, OLifE tempOLifE, Holding primaryHolding)
		throws NbaDataAccessException {
		primaryHolding.setKeys(oLifE.getKeys());
		primaryHolding.setAttachmentGhost(null);
		//NBA047 Allow to lazy initialize
		if (primaryHolding.getIntentCount() == 0) {
			primaryHolding.setIntentGhost(null); //Allow to lazy initialize
		} else {
			ArrayList origIntent = primaryHolding.getIntent();
			primaryHolding.setIntentGhost(null); //Allow to lazy initialize
			primaryHolding.setIntent(new ArrayList()); 
			ArrayList dbIntent = primaryHolding.getIntent();
			primaryHolding.setIntent(origIntent); //Restore original
			for (int i = 0; i < primaryHolding.getIntentCount(); i++) { //Locate matching
				Intent intent = primaryHolding.getIntentAt(i);
				for (int j = 0; j < dbIntent.size(); j++) {
					Intent tempIntent = (Intent) dbIntent.get(i);
					if (tempIntent.getId().equals(intent.getId())) {
						intent.setKeys(tempIntent.getKeys());
						intent.setObjective(tempIntent.getObjective());
						//NBA093
						//NBA093 deleted 11 lines
						break;
					}
				}
			}
		}
		//begin ACN014
		Policy policy = primaryHolding.getPolicy();
		//begin ACN013
		primaryHolding.setPolicyGhost(null);
		Policy tempPolicy = primaryHolding.getPolicy();
		primaryHolding.setPolicy(policy);
		policy.setId(tempPolicy.getId());	//Assure that ID contains database value
		//end ACN013
		policy.setReinsuranceInd(tempPolicy.getReinsuranceInd());//SPR2889
		policy.setKeys(oLifE.getKeysForChildren(oLifE.getId()));
		if (policy.getRequirementInfoCount() == 0) {
			policy.setRequirementInfoGhost(null); //Allow to lazy initialize
		} else {
			ArrayList origRequirement = policy.getRequirementInfo();
			policy.setRequirementInfoGhost(null); 
			policy.setRequirementInfo(new ArrayList());  
			ArrayList dbRequirement = policy.getRequirementInfo();
			List notFoundReqs = policy.getRequirementInfo(); //NBA130
			policy.setRequirementInfo(origRequirement);
			int count = policy.getRequirementInfoCount();
			matchRequirements(policy, dbRequirement, notFoundReqs, count); //SPR3185
			//Begin NBA130
			for( int i = 0; i < notFoundReqs.size(); i++) {
				policy.addRequirementInfo((RequirementInfo)notFoundReqs.get(i));
			}
			//End NBA130
		}
		//end ACN014
		int tempHoldingCount = tempOLifE.getHoldingCount();
		for (int temp = 0; temp < tempHoldingCount; temp++) {
			Holding tempHolding = tempOLifE.getHoldingAt(temp);
			if (!tempHolding.getId().equals(primaryHolding.getId())) {
			} else {
				oLifE.addHolding(tempHolding);
			}
		}
		mergeDbOnlyPolicyInfo(policy);	//ACN013
	}
    /**
     * Merge database only RequirementInfo into the nbaTXLife. 
     * @param reqInfo the NbaTXLife RequirementInfo object
     * @param temprReqInfo the database RequirementInfo object
     */
    //ACN014 New Method
    protected static void mergeDBOnlyRequirementInfo(RequirementInfo reqInfo, RequirementInfo tempReqInfo) {
    	reqInfo.setKeys(tempReqInfo.getKeys());
    	reqInfo.setId(tempReqInfo.getId());
    	reqInfo.setAttachmentGhost(null);	//NBA208-10
    	mergeDBOnlyRequirementInfoExtension(reqInfo, tempReqInfo); //NBA130
    	reqInfo.setRequirementInfoUniqueID(tempReqInfo.getRequirementInfoUniqueID());
    	reqInfo.setRequestedScheduleTimeEnd(tempReqInfo.getRequestedScheduleTimeEnd());
    	reqInfo.setRequestedScheduleTimeStart(tempReqInfo.getRequestedScheduleTimeStart());
    	reqInfo.setRequirementAcctNum(tempReqInfo.getRequirementAcctNum());
    	reqInfo.setRequirementDetails(tempReqInfo.getRequirementDetails());
    	//Begin NBA130
       	reqInfo.setRequestedDate(tempReqInfo.getRequestedDate());
    	reqInfo.setReqStatus(tempReqInfo.getReqStatus());
    	reqInfo.setUserCode(tempReqInfo.getUserCode());
    	reqInfo.setRestrictIssueCode(tempReqInfo.getRestrictIssueCode());
    	//End NBA130
    	reqInfo.setReqSubStatus(tempReqInfo.getReqSubStatus()); //AXAL3.7.38
    }
	/**
	 * Merge Data Base only party information into the nbaTXLife. 
	 * @param oLifE the NbaTXLife OLifE object
	 * @param tempOLifE the database  object
	 * @throws NbaDataAccessException
	 */
	protected static void mergeDbOnlyPartyInfo(OLifE oLifE, OLifE tempOLifE) throws NbaDataAccessException {
		int partyCount = oLifE.getPartyCount();
		int tempPartyCount = tempOLifE.getPartyCount();
		for (int i = 0; i < partyCount; i++) {
			Party party = oLifE.getPartyAt(i);
			party.setKeys(oLifE.getKeysForChildren(oLifE.getId()));
			party.setRiskGhost(null); // Risk will be lazy initiailized
			party.setEmploymentGhost(null); // Employment will be lazy initiailized	//ACN003
			party.setPriorNameGhost(null); // PriorName will be lazy initiailized	//ACN003
			party.setCarrierGhost(null); // Carrier will be lazy initiailized	//ACN003
			party.setAttachmentGhost(null);//NBA105
			for (int temp = 0; temp < tempPartyCount; temp++) {
				Party tempParty = tempOLifE.getPartyAt(temp);
				if (party.getId().equals(tempParty.getId())) {
					party.setEstNetWorth(tempParty.getEstNetWorth());
					// Handle Client information
					mergeDbOnlyCLientInfo(party, tempParty);
					// Handle PersonOrOrganization information
					mergeDbOnlyPersonOrOrganization(party, tempParty);
					mergeDbOnlyPartyExtension(party, tempParty); // ACP008 
					//merge address information
					mergeDBOnlyAddress(party, tempParty); //NBA211
					//merge phone information
					mergeDBOnlyPhone(party, tempParty); //NBA211
					//merge the risk items from the database
					party.setRisk(tempParty.getRisk()); //NBA211
					break;
				}
			}
		}
		//Begin NBA130
        UnsupportedPartyTypes upts = getUnsupportedPartyTypes(oLifE.getSourceInfo());
        if (null != upts) {
	        for (int i = 0; i < tempPartyCount; i++) {
	            Party tempParty = tempOLifE.getPartyAt(i);
	            Relation tempRelation = NbaUtils.getRelationForParty(tempParty.getId(), tempOLifE.getRelation().toArray());        
	            if (null != tempParty && null != tempRelation && isUnsupportedParty(upts, String.valueOf(tempRelation.getRelationRoleCode()))) {
	                if( null == NbaTXLife.getPartyFromId(tempParty.getId(), oLifE.getParty())) {
	                    oLifE.addParty(tempParty);
	            	}
	            }
	        }
        }
		//End NBA130
	}
	/**
	 * Determines if the tempParty is an unsupported party by looking through the 
	 * Unsupported Party list provided by the BackendAdapter. 
	 * The "key" value in the UnsupportedPartyType uses the values in the
	 * OLI_LU_REL ACORD lookup. 
	 * @param olifE the OLifE object containing the parties on the contract
	 * @param tempParty the Party for which the inquiry is being executed 
	 * @return true if tempParty is not supported on the backend system; else, false.
	 */
	 //NBA130 New Method
	protected static boolean isUnsupportedParty(UnsupportedPartyTypes upts, String relationRoleCode) {
	    Iterator uptIter = upts.getUnsupportedPartyType().iterator();
		while( uptIter.hasNext()) {
		    UnsupportedPartyType upt = (UnsupportedPartyType)uptIter.next();
		    if(upt.getKey().equalsIgnoreCase(relationRoleCode)) {
		    	return true;
		    }
		}
		return false;
	}
	/**
	 * Returns the UnsupportedPartyTypes element from the NbaConfiguration.xml file
     * for the backend system contained in the SourceInfo object.
	 * @param sourceInfo contains the backend system
	 * @return UnsupportedPartyTypes from the NbaConfiguration.xml file
	 */
	 //NBA130 New Method
	protected static UnsupportedPartyTypes getUnsupportedPartyTypes(SourceInfo sourceInfo) {
        try {
            if (!sourceInfo.hasFileControlID()) {
                return null;
            }
            String backendSystem = sourceInfo.getFileControlID();
            // begin NBA195
            BackEnd backend = NbaConfiguration.getInstance().getBackEndSystem(backendSystem);
            if (backend != null) {
                return backend.getUnsupportedPartyTypes();
            }
            return null;
            // end NBA195
        } catch (NbaBaseException nbe) {
            // Callers expect null if configuration could not be found
            return null;
        }
    }
	/**
	 * Merge database only partyExtension information into the partyExtension 
	 * @param party the nbaTXLife party
	 * @param tempParty the database party
	 */
	// ACP008 new Method
	protected static void mergeDbOnlyPartyExtension(Party party, Party tempParty){
		PartyExtension partyExtension = NbaUtils.getFirstPartyExtension(party);
		if (partyExtension == null){	 
		    party.setOLifEExtensionGhost(null);	//Allow to lazy initialize	NBA208-10
		} else {		//NBA208-10
			partyExtension.setKeys(party.getKeysForChildren(party.getId()));
			partyExtension.setUnderwritingAnalysisGhost(null);
            PartyExtension tempPartyExtension = NbaUtils.getFirstPartyExtension(tempParty);	//NBA208-10
            if (tempPartyExtension == null) {	//NBA208-10
                partyExtension.setActionAdd();	//NBA208-10	
			}
		}
	}
	/**
	 * Merge database only PersonOrOrganization information into the PersonOrOrganization. 
	 * @param party the nbaTXLife party
	 * @param tempParty the database party
	 */
	protected static void mergeDbOnlyPersonOrOrganization(Party party, Party tempParty) {
		if (party.hasPersonOrOrganization()) {
			PersonOrOrganization personOrOrganization = party.getPersonOrOrganization();
			personOrOrganization.setKeys(party.getKeysForChildren(party.getId()));
			if (personOrOrganization.isPerson()) {
				Person person = personOrOrganization.getPerson();
				person.setKeys(personOrOrganization.getKeys());
				if (tempParty.hasPersonOrOrganization()) {
					PersonOrOrganization tempPersonOrOrganization = tempParty.getPersonOrOrganization();
					if (tempPersonOrOrganization.isPerson()) {
						Person tempPerson = tempPersonOrOrganization.getPerson();
						person.setId(tempPerson.getId());
						person.setOccupation(tempPerson.getOccupation());
						person.setDriversLicenseNum(tempPerson.getDriversLicenseNum());
						person.setDriversLicenseState(tempPerson.getDriversLicenseState());
						person.setHeight(tempPerson.getHeight());
						person.setWeight(tempPerson.getWeight());
						person.setCitizenship(tempPerson.getCitizenship());
						person.setEstSalary(tempPerson.getEstSalary());
						//begin NBA093
						//NBA077 code deleted
						//begin NBA077
						person.setHeight2Ghost(null);	//NBA208-10
						person.setWeight2Ghost(null);	//NBA208-10
						//end NBA077
						//end NBA093
						person.setVisaExpDate(tempPerson.getVisaExpDate()); //NBA211
						person.setBirthCountry(tempPerson.getBirthCountry()); //NBA211
						//begin SPR1719
						PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person); //ACN013
						if (personExtension == null) { //ACN013 NBA208-10
                            person.setOLifEExtensionGhost(null); //Allow to lazy initialize //NBA208-10
                        } else { //NBA208-10
                            personExtension.setKeys(person.getKeys()); //ACN013
                            //NBA208-10 code deleted
                            PersonExtension tempPersonExtension = NbaUtils.getFirstPersonExtension(tempPerson);
                            if (tempPersonExtension != null) {
                                //NBA077 code deleted	
                                // ACN013 code deleted						
                                //NBA208-10 code deleted
                                personExtension.setImpairmentInfo(tempPersonExtension.getImpairmentInfo());
                                personExtension.setRateClassAppliedFor(tempPersonExtension.getRateClassAppliedFor()); //ACN013
                                personExtension.setProposedRateClass(tempPersonExtension.getProposedRateClass()); //ACN013
                                personExtension.setBonusCommAmt(tempPersonExtension.getBonusCommAmt()); //ACN003
                                personExtension.setOccupationYrExperience(tempPersonExtension.getOccupationYrExperience()); //ACN003
                                personExtension.setDateOfArrival(tempPersonExtension.getDateOfArrival()); //NBA211
                                personExtension.setUSCitizenInd(tempPersonExtension.getUSCitizenInd()); //NBA211
                                personExtension.setKeys(person.getKeys()); //NBA077
                                //NBA077 code deleted
                            } else { //NBA208-10
                                personExtension.setActionAdd();
                            }
                        } //NBA208-10						
						//end SPR1719
					}
				}
			} else if (personOrOrganization.isOrganization()) {
				Organization oganization = personOrOrganization.getOrganization();
				oganization.setKeys(personOrOrganization.getKeys());
				mergeDbOnlyOrganization(tempParty, oganization);	//ACN003
			}
		}
	}
	/**
	 * Merge database only Organization information into the nbaTXLife.
	 * @param party	
	 * @param tempParty
	 * @param oganization
	 */
	// ACN003 New Method
	protected static void mergeDbOnlyOrganization(Party tempParty, Organization oganization) {
		oganization.setOrganizationFinancialDataGhost(null); //Lazy initialize if necessary
		OrganizationExtension organizationExtension = NbaUtils.getFirstOrganizationExtension(oganization);
		if (organizationExtension != null) {
			organizationExtension.setKeys(oganization.getKeysForChildren(oganization.getId()));
		} else {	//NBA208-10
		    oganization.setOLifEExtensionGhost(null);	//Allow to lazy initialize	NBA208-10						
		}
		if (tempParty.hasPersonOrOrganization()) {
            PersonOrOrganization tempPersonOrOrganization = tempParty.getPersonOrOrganization();
            if (tempPersonOrOrganization.isOrganization()) {
                Organization tempOrganization = tempPersonOrOrganization.getOrganization();
                oganization.setKeys(tempOrganization.getKeys());
                oganization.setId(tempOrganization.getId());
                //NBA208-10 code deleted
                oganization.setEstabDate(tempOrganization.getEstabDate());
                oganization.setOrgForm(tempOrganization.getOrgForm());
                oganization.setNumOwners(tempOrganization.getNumOwners());
                if (organizationExtension != null) { //NBA208-10
                    organizationExtension.setKeys(oganization.getKeysForChildren(oganization.getId()));
                    OrganizationExtension tempOrganizationExtension = NbaUtils.getFirstOrganizationExtension(tempOrganization);
                    if (tempOrganizationExtension != null) {
                        //NBA208-10 code deleted
                        organizationExtension.setKeys(tempOrganizationExtension.getKeys());
                        organizationExtension.setKeyPersonsInsInd(tempOrganizationExtension.getKeyPersonsInsInd());
                        organizationExtension.setBuySellPersonsInsInd(tempOrganizationExtension.getBuySellPersonsInsInd());
                    } else { //NBA208-10
                        organizationExtension.setActionAdd();
                    }
                }
            }
        }		
	}		//NBA208-10
	/**
	 * Merge database only Client information into the client. 
	 * @param party the nbaTXLife party
	 * @param tempParty the database party
	 */
	protected static void mergeDbOnlyCLientInfo(Party party, Party tempParty) {
		if (party.hasClient()) {
			Client client = party.getClient();
			client.setKeys(party.getKeysForChildren(party.getId()));
			if (tempParty.hasClient()) {
				Client tempClient = tempParty.getClient();
				client.setEstTaxBracket(tempClient.getEstTaxBracket());
				mergeDbOnlyClientExtension(client, tempClient);
			}
		} else {
			party.setClientGhost(null); //Lazy initialize if necessary
		}
	}
	
	/**
	* Merge database only ClientExtension information into the clientExtension. 
	* @param client the nbaTXLife client
	* @param tempClient the database client
	*/
	protected static void mergeDbOnlyClientExtension(Client client, Client tempClient) {
	    ClientExtension clientExtension = getClientExtension(client); //NBA208-10
	    if (clientExtension == null){	//NBA208-10
	        client.setOLifEExtensionGhost(null);	//Allow to lazy initialize NBA208-10
	    } else {	//NBA208-10
            ClientExtension tempClientExtension = getClientExtension(tempClient);
            if (tempClientExtension != null) {
                clientExtension.setKeys(client.getKeysForChildren(client.getId()));
                clientExtension.setEmployerName(tempClientExtension.getEmployerName());
                clientExtension.setNumDependents(tempClientExtension.getNumDependents());
                clientExtension.setReasonSpouseNotCovered(tempClientExtension.getReasonSpouseNotCovered());
                clientExtension.setSpouseInsAmt(tempClientExtension.getSpouseInsAmt());
                clientExtension.setSpouseName(tempClientExtension.getSpouseName());
            } else {
                clientExtension.setActionAdd();	//NBA208-10
            }
        } 
	}
	/**
	 * Locate the ClientExtension.  
	 * @param aClient the Client object
	 * 
	 */
	protected static ClientExtension getClientExtension(Client aClient) {
		int count = aClient.getOLifEExtensionCount();
		for (int i = 0; i < count; i++) {
			OLifEExtension oLifEExtension = aClient.getOLifEExtensionAt(i);
			if (oLifEExtension.isClientExtension()) {
				return oLifEExtension.getClientExtension();
			}
		}
		return null;
	}
	
	/**
	 * Locate the PersonExtension.  
	 * @param aPerson the Person object
	 * 
	 */
	protected static PersonExtension getPersonExtension(Person aPerson) {
		int count = aPerson.getOLifEExtensionCount();
		for (int i = 0; i < count; i++) {
			OLifEExtension oLifEExtension = aPerson.getOLifEExtensionAt(i);
			if (oLifEExtension.isPersonExtension()) {
				return oLifEExtension.getPersonExtension();
			}
		}
		return null;
	}
	/**
	 * Commit Data Base only information.
	 * @param nbaTXLife the NbaTXLife object
	 * @throws NbaDataAccessException
	 * @throws SQLException
	 */
	protected static void updateDbOnly(NbaTXLife nbaTXLife, Connection conn)
		throws NbaDataAccessException, NbaDataAccessException, SQLException {
		nbaTXLife.getOLifE().setKeys(getArgs(nbaTXLife));
		updateDbOnlyFormInstanceInfo(nbaTXLife, conn); 	//ACN013
		updateDbOnlyHoldingInfo(nbaTXLife, conn);
		updateDbOnlyPartyInfo(nbaTXLife, conn);
		updateDbOnlyRelationInfo(nbaTXLife, conn);
	}
	/**
	 * Update Data Base only Party information. 
	 * For the sake of simplicity, all Party obects and
	 * their children are eligible to be updated.
	 * @param nbaTXLife the NbaTXLife object
	 * @throws NbaDataAccessException
	 * 
	 */
	protected static void updateDbOnlyPartyInfo(NbaTXLife nbaTXLife, Connection conn) throws NbaDataAccessException {
		OLifE oLifE = nbaTXLife.getOLifE();
		int count = oLifE.getPartyCount();
		// SPR3290 code deleted
		for (int i = 0; i < count; i++) {
			oLifE.getPartyAt(i).processChanges(oLifE.getKeys(), oLifE.getActionIndicator(), conn);
		}
	}
	/**
	 * Update Data Base only Holding information.
	 * For the primary holding, only the Intent, Attachment, and Policy objects are 
	 * eligible to be updated.  For other holdings, the 
	 * holding and all it's child objects are eligible to 
	 * be updated.
	 * @param nbaTXLife the NbaTXLife object
	 * @throws NbaDataAccessException
	 * 
	 */
	protected static void updateDbOnlyHoldingInfo(NbaTXLife nbaTXLife, Connection conn) throws NbaDataAccessException {
		OLifE oLifE = nbaTXLife.getOLifE();
		int count = oLifE.getHoldingCount();
		String primaryHoldingId = nbaTXLife.getPrimaryHolding().getId();
		Holding holding;
		for (int i = 0; i < count; i++) {
			NbaActionIndicator cascadeAction = oLifE.getActionIndicator();
			holding = oLifE.getHoldingAt(i);
			if (holding.getId().equals(primaryHoldingId)) {
				holding.setKeys(oLifE.getKeys());
				if (cascadeAction.isDisplay()) {
					cascadeAction = holding.getActionIndicator();
				}
				for (int j = 0; j < holding.getIntentCount(); j++) {
					holding.getIntentAt(j).processChanges(
						holding.getKeysForChildren(holding.getId()),
						cascadeAction,
						conn);
				}
				//begin NBA047
				for (int j = 0; j < holding.getAttachmentCount(); j++) {
					holding.getAttachmentAt(j).processChanges(
						holding.getKeysForChildren(holding.getId()),
						cascadeAction,
						conn);
				}
				//end NBA047
				updateDbOnlyPolicyInfo(holding.getPolicy(), holding, cascadeAction, conn); //ACN014				 
			} else {
				holding.processChanges(oLifE.getKeys(), cascadeAction, conn);
			}
		}
	}
	/**
	 * Update Data Base only FormInstance information. 
	 * @param nbaTXLife the NbaTXLife object
	 * @param conn the database connection
	 * @throws NbaDataAccessException 
	 */
	// ACN013 New Method
	protected static void updateDbOnlyFormInstanceInfo(NbaTXLife nbaTXLife, Connection conn) throws NbaDataAccessException {
		OLifE oLifE = nbaTXLife.getOLifE();
		int count = oLifE.getFormInstanceCount();
		NbaActionIndicator cascadeAction = oLifE.getActionIndicator();
		FormInstance formInstance;
		for (int i = 0; i < count; i++) {
			formInstance = oLifE.getFormInstanceAt(i);
			formInstance.processChanges(oLifE.getKeys(), cascadeAction, conn);
		}
	}
	/**
	 * Update Data Base only Policy information.
	 * For the Policy, everything is updated except regular SusbstandardRatings and endorsements.
	 * @param nbaTXLife the NbaTXLife object
	 * @throws NbaDataAccessException
	 */
	//ACN014 ACN013 New Method
	protected static void updateDbOnlyPolicyInfo(Policy policy, Holding holding, NbaActionIndicator cascadeAction, Connection conn)
            throws NbaDataAccessException {
        policy.setKeys(holding.getKeysForChildren(holding.getId()));//SPR2889
        ArrayList endorsements = policy.getEndorsement(); //SPR1163
        policy.setEndorsement(new ArrayList()); //prevent endorsements from being applied //SPR1163
        
        //begin SPR3203
        LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
        //create cache of all rating lists to be restored after pending database commit
        Map ratingListCache = cacheWrapperedOnlyRatings(ladh);
        //perform commit on complete policy hierarchy 
        policy.processChanges(holding.getKeysForChildren(holding.getId()), cascadeAction, conn);
        policy.setEndorsement(endorsements); //restore endorsements //SPR1163
        //restore all but proposed ratings back to original policy object for wrappered processing.
        restoreNonProposedRatings(ladh, ratingListCache);
        //end SPR3203
    }
	/**
	* Create Accounting Extract for Pending CWA Payments and Disbursement Extracts for the refund of CWA.
	* @param nbaTxLife the NbaTXLife object
	* @return conn Connection object
	* @throws NbaBaseException
	*/
	//NBA066 New Method
	//SPR2817 added new parameter, isIssue
	//NBA228 method signature changed
	//APSL2440 Internal Perfomance Issue changed method signature
	protected static void createExtracts(NbaTXLife nbaTxLife, NbaDst dst, boolean isIssue, NbaUserVO user)	
		throws NbaBaseException { //SPR1656 signature changed
		//SPR1656 code deleted
		//SPR2817 code deleted
		//SPR1656
		new NbaContractExtracts().createExtracts(nbaTxLife, dst, isIssue, user); //SPR1656 SPR2817 AXAL3.7.23 NBA228
	}
	/**
	* This method is used to commit all the connections to the database
	* @param connections List object
	* @throws NbaDataAccessException
	*/
	//SPR1656 New Method
	//NBA187 changed parameter to List
	protected static void commitConnections(List connections) throws NbaDataAccessException {
		Connection conn = null;
		boolean connCommitted = false;
		try {
			for (int i = 0; i < connections.size(); i++) {
				conn = (Connection) connections.get(i);
				conn.commit();
				connCommitted = true;
			}
		} catch (SQLException ex) { //NBA103
			NbaDataAccessException e = new NbaDataAccessException(ex);
			if (connCommitted) {
				e.addMessage("Database inconsistent: Partial data committed.");
			} else {
				e.addMessage("Error in database updation: No data committed.");
			}
			getLogger().logException(e);
			throw e;
		}
	}
	/**
	* This method is used to rollback all the connections if there is some problem in commiting any of the connections
	* @param connections ArrayList object
	* @throws NbaDataAccessException
	*/
	//SPR1656 New Method
	//NBA187 changed parameter to List
	protected static void rollbackConnections(List connections){
		for (int i = 0; i < connections.size(); i++) {
			Connection conn = (Connection) connections.get(i);
			try {
				conn.rollback();
			} catch (SQLException e) {
				getLogger().logException("Connection rollback failed", e);
				//NBA103
			}
		}
	}
	/**
	 * Creates a TransResult object that contains information about the failure of the 
	 * contract update. 
	 * @param message String contains the reason of failure
	 * @return a newly created TransResult object
	 * @throws NbaBaseException
	 */
	//SPR1656 new method
	protected static TransResult createExceptionResult(String message) throws NbaBaseException {
		TransResult transResult = new TransResult();
		ArrayList aList = new ArrayList();
		transResult.setResultCode(5); // MEANS FAILURE
		ResultInfo resultInfo = new ResultInfo();
		resultInfo.setResultInfoCode(999);
		resultInfo.setResultInfoDesc(message);
		aList.add(resultInfo);
		transResult.setResultInfo(aList);
		return transResult;
	}
	/**
	 * This method retieve and delete the contract from pending database if exists
	 * @param nbaDst an AWD work item containing identifying information
	 * @param nbaUser an AWD user
	 * @param conn the connection object
	 * @throws NbaBaseException
	 */
	//NBA077 New Method
	protected static void deleteContract(NbaDst nbaDst, NbaUserVO nbaUser, Connection conn) throws NbaBaseException {
		try {
			NbaLob lob = nbaDst.getNbaLob();
			NbaTXRequestVO requestVO = createRequestObject(nbaDst, nbaUser);
			requestVO.setOverrideDataSource(NbaConstants.STANDALONE);
			NbaTXLife holding = doContractInquiry(requestVO);
			holding.toXmlString(); //get all data
			NbaActionIndicator action = new NbaActionIndicator();
			action.setDelete();
			holding.getOLifE().processChanges(
				lob.getBackendSystem(),
				lob.getCompany(),
				lob.getPolicyNumber(),
				action,
				conn);
		} catch (NbaDataAccessException de) {
			getLogger().logException("deleteContract failed", de);		
		}
	}
	/**
	 * This method retieve and delete the contract from pending database if exists
	 * @param nbaDst an AWD work item containing identifying information
	 * @param nbaUser an AWD user
	 * @param conn the connection object
	 * @throws NbaBaseException
	 */
	//NBA077 New Method
	protected static void deleteDataForWrapperedContract(NbaDst nbaDst, NbaUserVO nbaUser, Connection conn) throws NbaBaseException {
		try {
			NbaLob lob = nbaDst.getNbaLob();
			NbaTXRequestVO requestVO = createRequestObject(nbaDst, nbaUser);
			requestVO.setOverrideDataSource(NbaConstants.WRAPPERED);
			requestVO.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
			requestVO.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
			requestVO.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
			NbaTXLife holding = doContractInquiry(requestVO);
			//retireve full parties from pending database
			OLifE olife = holding.getOLifE();
			olife.setPartyGhost(null);
			olife.setParty(null);
			//begin SPR2816
			Holding primaryHolding = new Holding(true);
			olife.addHolding(primaryHolding);
			primaryHolding.setKeys(olife.getKeys());
			primaryHolding.setId(NbaConstants.PRIMARY_HOLDING_ID);
			//end SPR2816
			holding.toXmlString(); //get all data
			NbaActionIndicator action = new NbaActionIndicator();
			action.setDelete();
			holding.getOLifE().processChanges(lob.getBackendSystem(), lob.getCompany(), lob.getPolicyNumber(), action, conn);
		} catch (NbaDataAccessException de) {
			getLogger().logException("deleteDataForWrapperedContract failed", de);
		}
	}
	/**
	 * This method first retrive all remaining objects left during clone of OLifE object. Then this method
	 * process casecade delete on olife object and delete all information in pending database.
	 * @param tempOLifE the database  object
	 * @param lob the NbaLOb object for key information
	 * @param conn the connection object
	 * @throws NbaDataAccessException
	 */
	
	protected static void deleleMergedContract(OLifE tempOLifE, NbaLob lob, Connection conn) throws NbaDataAccessException {
		//Get primary holding data All other holding retrieved during clone
		Holding primaryHolding = new Holding();
		tempOLifE.addHolding(primaryHolding);
		primaryHolding.setKeys(tempOLifE.getKeys());
		primaryHolding.setAttachmentGhost(null);
		//NBA047 Allow to lazy initialize
		if (primaryHolding.getIntentCount() == 0) {
			primaryHolding.setIntentGhost(null); //Allow to lazy initialize
		} else {
			ArrayList origIntent = primaryHolding.getIntent();
			primaryHolding.setIntentGhost(null); //Allow to lazy initialize
			ArrayList dbIntent = primaryHolding.getIntent();
			primaryHolding.setIntent(origIntent); //Restore original
			for (int i = 0; i < primaryHolding.getIntentCount(); i++) { //Locate matching
				Intent intent = primaryHolding.getIntentAt(i);
				for (int j = 0; j < dbIntent.size(); j++) {
					Intent tempIntent = (Intent) dbIntent.get(i);
					if (tempIntent.getId().equals(intent.getId())) {
						intent.setKeys(tempIntent.getKeys());
						intent.setObjective(tempIntent.getObjective());
						break;
					}
				}
			}
		}
		NbaActionIndicator action = new NbaActionIndicator();
		action.setDelete();
		tempOLifE.processChanges(lob.getBackendSystem(), lob.getCompany(), lob.getPolicyNumber(), action, conn);
	}
	/**
	 * This method retrieves party's data from the Party table based on the
	 * values contained in the incoming party object. If incoming party object 
	 * contains only first name then all the parties with that name would be 
	 * returned. It would consider the  standalone mode contracts only.  
	 * @param Party a party object containing the crieteria for query
	 * @param excludePolicies a comma seperated list of policies which are to be excluded while querying the data
	 * @return an List containing the query results. Its a list of NbaPartyData
	 * @throws NbaBaseException
	 */
	//NBA105 new method 
	//ALS4644 Change data type of excludePolicies from String to List
	public static List doPartyInquiry(Party party, List excludePolicies) throws NbaBaseException {
		try {
			List resultList = NbaPartyInquiryDataBaseAccessor.getInstance().getContractKeysForParty(party, excludePolicies);
			return resultList;
		} catch (Exception e) {
			NbaBaseException nbe = new NbaBaseException(e);
			getLogger().logException(e);	//SPR2992
			throw nbe;
		}
	}
	/**
	 * Merge database only Policy information into the nbaTXLife. 
	 * @param policy the Policy object
	 */
	// ACN013 New Method
	protected static void mergeDbOnlyPolicyInfo(Policy policy) {
		ApplicationInfo applicationInfo = policy.getApplicationInfo();
		policy.setApplicationInfoGhost(null); //Allow to lazy initialize
		policy.setApplicationInfo(null);
		ApplicationInfo tempApplicationInfo = policy.getApplicationInfo();
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExtension != null){
			policyExtension.setKeys(policy.getKeysForChildren(policy.getId()));
			policyExtension.setSuspendInfoGhost(null);
		}
		policy.setApplicationInfo(applicationInfo); //Restore original
		if (tempApplicationInfo != null) {
            if (applicationInfo == null) {
                policy.setApplicationInfo(tempApplicationInfo);
                applicationInfo = tempApplicationInfo;
            } else {
                applicationInfo.setKeys(policy.getKeysForChildren(policy.getId()));
                applicationInfo.setId(tempApplicationInfo.getId());
                applicationInfo.setHOCompletionDate(tempApplicationInfo.getHOCompletionDate());
                applicationInfo.setReplacementInd(tempApplicationInfo.getReplacementInd()); //SPR2697
                applicationInfo.setFormalAppInd(tempApplicationInfo.getFormalAppInd()); //NBA187
                applicationInfo.setTrackingID(tempApplicationInfo.getTrackingID()); //NBA187
                //begin NBA211
                applicationInfo.setApplicationType(tempApplicationInfo.getApplicationType());
                applicationInfo.setSignatureInfo(tempApplicationInfo.getSignatureInfo());
                if(tempApplicationInfo.hasProducerReplacementDisclosureInd()){
                    applicationInfo.setProducerReplacementDisclosureInd(tempApplicationInfo.getProducerReplacementDisclosureInd());
                }
                //end NBA211
            }
            ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
            if (applicationInfoExtension == null) { //NBA208-10
                applicationInfo.setOLifEExtensionGhost(null); //Allow to lazy initialize	NBA208-10
            } else { //NBA208-10
                applicationInfoExtension.setKeys(applicationInfo.getKeysForChildren(applicationInfo.getId()));
                //NBA208-10 code deleted
                ApplicationInfoExtension tempApplicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(tempApplicationInfo);
                if (tempApplicationInfoExtension != null) {
                    //NBA208-10 code deleted
                    applicationInfoExtension.setKeys(tempApplicationInfoExtension.getKeys());
                    applicationInfoExtension.setApprovalUnderwriterId(tempApplicationInfoExtension.getApprovalUnderwriterId());
                    applicationInfoExtension.setInformalAppApproval(tempApplicationInfoExtension.getInformalAppApproval()); //NBA187
                    applicationInfoExtension.setApplicationOrigin(tempApplicationInfoExtension.getApplicationOrigin()); //NBA187
                    applicationInfoExtension.setCheckSignedOK(tempApplicationInfoExtension.getCheckSignedOK()); //SPR3407
                    applicationInfoExtension.setCWAInd(tempApplicationInfoExtension.getCWAInd()); //NBA211
                    applicationInfoExtension.setAppTranslatedInd(tempApplicationInfoExtension.getAppTranslatedInd()); //NBA211
                    applicationInfoExtension.setProducerOtherInsDisclosureInd(tempApplicationInfoExtension.getProducerOtherInsDisclosureInd()); //NBA211
                    applicationInfoExtension.setTentativeDisp(tempApplicationInfoExtension.getTentativeDisp()); //NBA186
					applicationInfoExtension.setInitialDecision(tempApplicationInfoExtension.getInitialDecision()); //NBA186
					applicationInfoExtension.setReopenDate(tempApplicationInfoExtension.getReopenDate());//NBA230
                } else { //NBA208-10
                    applicationInfoExtension.setActionAdd();
                }
            }
        } //NBA208-10
		//begin NBA208-15
		applicationInfo = policy.getApplicationInfo();
        if (applicationInfo.getOLifEExtensionCount() > 0) {
            OLifEExtension olifeExt = applicationInfo.getOLifEExtensionAt(0);
            ApplicationInfoExtension origAppInfoExt = olifeExt.getApplicationInfoExtension();
            olifeExt.setApplicationInfoExtensionGhost(origAppInfoExt.clone(false));
        }
		//end NBA208-15
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		if (ladh.isLife()) {
			ladh.getLife().setKeys(policy.getKeysForChildren(policy.getId()));
			mergeDbOnlyCoverageInfo(ladh.getLife());
		} else if(ladh.isAnnuity()){ //SPR3203
		    ladh.getAnnuity().setKeys(policy.getKeysForChildren(policy.getId())); //SPR3203
		    mergeDbOnlyAnnuityInfo(ladh.getAnnuity()); //NBA176
		}
	}
	/**
	 * Merge database only Coverage information into the nbaTXLife.
	 * @param life
	 */
	// ACN013 New Method
	protected static void mergeDbOnlyCoverageInfo(Life life) {
		ArrayList origCoverages = life.getCoverage();
		life.setCoverageGhost(null); //Allow to lazy initialize
		life.setCoverage(new ArrayList());
		ArrayList dbCoverages = life.getCoverage();
		life.setCoverage(origCoverages); //Restore original
		Coverage coverage;
		Coverage tempCoverage;
		CoverageExtension coverageExtension;
		CoverageExtension tempCoverageExtension;
		int coverageKey;
		for (int i = 0; i < life.getCoverageCount(); i++) {
			coverage = life.getCoverageAt(i);
			coverage.setKeys(life.getKeysForChildren(life.getId()));
			coverageExtension = NbaUtils.getFirstCoverageExtension(coverage);
			if (coverageExtension != null) {
				coverageExtension.setKeys(coverage.getKeysForChildren(coverage.getId()));
				coverageExtension.setReinsuranceOfferGhost(null); //SPR2863
			} else {	//NBA208-10
			    coverage.setOLifEExtensionGhost(null);	//Allow to lazy initialize	NBA208-10				
			}
			coverageKey = Integer.parseInt(coverage.getCoverageKey());
			for (int j = 0; j < dbCoverages.size(); j++) {
                tempCoverage = (Coverage) dbCoverages.get(j);
                if (coverageKey == Integer.parseInt(tempCoverage.getCoverageKey())) {
                    coverage.setKeys(tempCoverage.getKeys());
                    coverage.setId(tempCoverage.getId());
                    coverage.setPurpose(tempCoverage.getPurpose()); //NBA208-10
                    if (coverageExtension != null) {
                        coverageExtension.setKeys(coverage.getKeysForChildren(coverage.getId()));
                        //NBA208-10 code deleted
                        tempCoverageExtension = NbaUtils.getFirstCoverageExtension(tempCoverage);
                        if (tempCoverageExtension != null) {
                            //NBA208-10 code deleted
                            coverageExtension.setKeys(tempCoverageExtension.getKeys());
                            coverageExtension.setOverrideRatingReason(tempCoverageExtension.getOverrideRatingReason());
                            coverageExtension.setRateClass(tempCoverageExtension.getRateClass());
                        } else { //NBA208-10
                            coverageExtension.setActionAdd();
                        }
                    }	//NBA208-10
                    break;
                } 
            }
			mergeDbOnlyCovOptionInfo(coverage);
			mergeDbOnlyLifeParticipantInfo(coverage);
			coverage.setReinsuranceInfoGhost(null);//SPR2863
		}
	}
	/**
	 * Merge database only CovOption information into the nbaTXLife.
	 * @param coverage
	 */
	// ACN013 New Method
	protected static void mergeDbOnlyCovOptionInfo(Coverage coverage) {
		ArrayList origCovOptions = coverage.getCovOption();
		coverage.setCovOptionGhost(null); //Allow to lazy initialize
		coverage.setCovOption(new ArrayList());
		ArrayList dbCovOptions = coverage.getCovOption();
		coverage.setCovOption(origCovOptions); //Restore original
		CovOption covOption;
		CovOption tempCovOption;
		CovOptionExtension covOptionExtension;
		CovOptionExtension tempCovOptionExtension;
		for (int i = 0; i < coverage.getCovOptionCount(); i++) {
			covOption = coverage.getCovOptionAt(i);
			covOption.setKeys(coverage.getKeysForChildren(coverage.getId()));
			covOptionExtension = NbaUtils.getFirstCovOptionExtension(covOption);
			if (covOptionExtension != null) {
				covOptionExtension.setKeys(covOption.getKeysForChildren(covOption.getId()));
			} else {	//NBA208-10
			    covOption.setOLifEExtensionGhost(null);	//Allow to lazy initialize	NBA208-10				
			}
			for (int j = 0; j < dbCovOptions.size(); j++) {
                tempCovOption = (CovOption) dbCovOptions.get(j);
                if (covOption.getLifeCovOptTypeCode() == tempCovOption.getLifeCovOptTypeCode()) {
					if (!covOption.hasLifeParticipantRefID()
						|| covOption.getLifeParticipantRefID().equals(tempCovOption.getLifeParticipantRefID())) {
                        covOption.setKeys(tempCovOption.getKeys());
                        covOption.setId(tempCovOption.getId());
                        if (covOptionExtension != null) {
                            covOptionExtension.setKeys(covOption.getKeysForChildren(covOption.getId()));
                            //NBA208-10 code deleted
                            tempCovOptionExtension = NbaUtils.getFirstCovOptionExtension(tempCovOption);
                            if (tempCovOptionExtension != null) {
                                //NBA208-10 code deleted
                                covOptionExtension.setKeys(tempCovOptionExtension.getKeys());
                                covOptionExtension.setOverrideRatingReason(tempCovOptionExtension.getOverrideRatingReason());
                                covOptionExtension.setProposedUnderwritingClass(tempCovOptionExtension.getProposedUnderwritingClass());
                            } else { //NBA208-10
                                covOptionExtension.setActionAdd();
                            }
                        }	 //NBA208-10 
                        break;
                    }
                }
            }
			mergeDbOnlySubstandardRatingInfo(covOption);
		}
	}
	/**
	 * Merge database only LifeParticipant information into the nbaTXLife.
	 * @param coverage
	 */
	// ACN013 New Method
	protected static void mergeDbOnlyLifeParticipantInfo(Coverage coverage) {
		ArrayList origLifeParticipants = coverage.getLifeParticipant();
		coverage.setLifeParticipantGhost(null); //Allow to lazy initialize
		coverage.setLifeParticipant(new ArrayList()); 
		ArrayList dbLifeParticipants = coverage.getLifeParticipant();
		coverage.setLifeParticipant(origLifeParticipants); //Restore original
		LifeParticipant lifeParticipant;
		LifeParticipant tempLifeParticipant;
		LifeParticipantExtension lifeParticipantExtension;
		LifeParticipantExtension tempLifeParticipantExtension;
		for (int i = 0; i < coverage.getLifeParticipantCount(); i++) {
			lifeParticipant = coverage.getLifeParticipantAt(i);
			lifeParticipant.setKeys(coverage.getKeysForChildren(coverage.getId()));
			lifeParticipantExtension = NbaUtils.getFirstLifeParticipantExtension(lifeParticipant);
			if (lifeParticipantExtension != null) {
				lifeParticipantExtension.setKeys(lifeParticipant.getKeysForChildren(lifeParticipant.getId()));
			} else {	//NBA208-10
			    lifeParticipant.setOLifEExtensionGhost(null);	//Allow to lazy initialize	NBA208-10				
			}
			for (int j = 0; j < dbLifeParticipants.size(); j++) {
                tempLifeParticipant = (LifeParticipant) dbLifeParticipants.get(j);
                if (lifeParticipant.getLifeParticipantRoleCode() == tempLifeParticipant.getLifeParticipantRoleCode()
                    && lifeParticipant.getPartyID().equals(tempLifeParticipant.getPartyID())) {
                    lifeParticipant.setKeys(tempLifeParticipant.getKeys());
                    lifeParticipant.setId(tempLifeParticipant.getId());
                    lifeParticipant.setTobaccoPremiumBasis(tempLifeParticipant.getTobaccoPremiumBasis()); //NBA208-10
                    if (lifeParticipantExtension != null) {
                        lifeParticipantExtension.setKeys(lifeParticipant.getKeysForChildren(lifeParticipant.getId()));
                        //NBA208-10 code deleted
                        tempLifeParticipantExtension = NbaUtils.getFirstLifeParticipantExtension(tempLifeParticipant);
                        if (tempLifeParticipantExtension != null) {
                            //NBA208-10 code deleted
                            lifeParticipantExtension.setKeys(tempLifeParticipantExtension.getKeys());
                            lifeParticipantExtension.setProposedTobaccoPremiumBasis(tempLifeParticipantExtension.getProposedTobaccoPremiumBasis());
                            lifeParticipantExtension.setProposedUnderwritingClass(tempLifeParticipantExtension.getProposedUnderwritingClass());
                        } else { //NBA208-10  
                            lifeParticipantExtension.setActionAdd();
                        }
                    } //NBA208-10 					
                    break;
                }
            }
			mergeDbOnlySubstandardRatingInfo(lifeParticipant);
		}
	}
	/**
	 * Merge database only LifeParticipant.SubstandardRating information into the nbaTXLife.
	 * All proposed ratings are copied from database object to holding inquiry
     * while regular rating are kept as it is.
	 * @param lifeParticipant
	 */
	// ACN013 New Method
	protected static void mergeDbOnlySubstandardRatingInfo(LifeParticipant lifeParticipant) {
		ArrayList origSubstandardRatings = lifeParticipant.getSubstandardRating();
		lifeParticipant.setSubstandardRatingGhost(null); //Allow to lazy initialize
		lifeParticipant.setSubstandardRating(new ArrayList());
		// SPR3290 code deleted
		//begin SPR3203
		int ratingCount = lifeParticipant.getSubstandardRatingCount();
		SubstandardRating currentRating = null;
		SubstandardRatingExtension currentRatingExt = null;
		for (int i = 0; i < ratingCount; i++) {
		    currentRating = lifeParticipant.getSubstandardRatingAt(i);
		    currentRatingExt = NbaUtils.getFirstSubstandardExtension(currentRating);
		    if(currentRatingExt != null && currentRatingExt.getProposedInd()){
		        origSubstandardRatings.add(currentRating);	
		    }
        }
        //end SPR3203
		lifeParticipant.setSubstandardRating(origSubstandardRatings); //Restore original
		//SPR3203 code deleted
	}
	/**
	 * Merge database only CovOption.SubstandardRating information into the nbaTXLife.
	 * @param covOption
	 */
	// ACN013 New Method
	protected static void mergeDbOnlySubstandardRatingInfo(CovOption covOption) {
		ArrayList origSubstandardRatings = covOption.getSubstandardRating();
		covOption.setSubstandardRatingGhost(null); //Allow to lazy initialize
		covOption.setSubstandardRating(new ArrayList());
		// SPR3290 code deleted
		//begin SPR3203
		int ratingCount = covOption.getSubstandardRatingCount();
		SubstandardRating currentRating = null;
		SubstandardRatingExtension currentRatingExt = null;
		for (int i = 0; i < ratingCount; i++) {
		    currentRating = covOption.getSubstandardRatingAt(i);
		    currentRatingExt = NbaUtils.getFirstSubstandardExtension(currentRating);
		    if(currentRatingExt != null && currentRatingExt.getProposedInd()){
		        origSubstandardRatings.add(currentRating);	
		    }
        }
        //end SPR3203
		covOption.setSubstandardRating(origSubstandardRatings); //Restore original
		//SPR3203 code deleted
	}
	/**
	 * Merge database only SubstandardRating information into the nbaTXLife.
	 * @param covOption
	 */
	// ACN013 New Method
	protected static void mergeDbOnlySubstandardRatingInfo(SubstandardRating substandardRating, ArrayList dbSubstandardRatings) {
		SubstandardRating tempSubstandardRating;
		SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
		if (substandardRatingExtension != null) {
			substandardRatingExtension.setKeys(substandardRating.getKeysForChildren(substandardRating.getId()));
		}
		SubstandardRatingExtension tempSubstandardRatingExtension;
		String substandardRatingType;
		String tempSubstandardRatingType;
		substandardRatingType = NbaUtils.getSubstandardRatingType(substandardRating);
		boolean proposed;
		for (int j = 0; j < dbSubstandardRatings.size(); j++) {
			tempSubstandardRating = (SubstandardRating) dbSubstandardRatings.get(j);
			tempSubstandardRatingType = NbaUtils.getSubstandardRatingType(tempSubstandardRating);
			tempSubstandardRatingExtension = NbaUtils.getFirstSubstandardExtension(tempSubstandardRating);
			if (tempSubstandardRatingExtension != null) {
				proposed = tempSubstandardRatingExtension.getProposedInd();
			} else {
				proposed = false;
			}
			if (!proposed && substandardRatingType.equals(tempSubstandardRatingType)) {	//Do not match proposed Ratings
				substandardRating.setKeys(tempSubstandardRating.getKeys());
				substandardRating.setId(tempSubstandardRating.getId());
				if (substandardRatingExtension != null) {
					substandardRatingExtension.setKeys(substandardRating.getKeysForChildren(substandardRating.getId()));
				}
				if (tempSubstandardRatingExtension != null) {
					if (substandardRatingExtension == null) {
						substandardRating.setOLifEExtension(tempSubstandardRating.getOLifEExtension());
					} else {
						substandardRatingExtension.setKeys(tempSubstandardRatingExtension.getKeys());
						substandardRatingExtension.setProposedInd(tempSubstandardRatingExtension.getProposedInd());
					}
				} else if (substandardRatingExtension != null) {
					substandardRatingExtension.setActionAdd();
				}
				break;
			}
		}
	}
	/**
	 * Update Data Base only Relation information. 
	 * For the sake of simplicity, all Relation obects and
	 * their children are eligible to be updated.
	 * @param nbaTXLife the NbaTXLife object
	 * @throws NbaDataAccessException
	 * 
	 */
	// ACN003 New Method
	protected static void updateDbOnlyRelationInfo(NbaTXLife nbaTXLife, Connection conn) throws NbaDataAccessException {
		OLifE oLifE = nbaTXLife.getOLifE();
		int count = oLifE.getRelationCount();
		// SPR3290 code deleted
		for (int i = 0; i < count; i++) {
			oLifE.getRelationAt(i).processChanges(oLifE.getKeys(), oLifE.getActionIndicator(), conn);
		}
	}
	/**
	 * Merge database only Relation information into the nbaTXLife.
	 * @param covOption
	 */
	// ACN003 New Method
	protected static void mergeDbOnlyRelationInfo(OLifE oLifE, OLifE tempOLifE) {
		Relation relation;
		Relation tempRelation;
		String relatedRefID;
		String tempRelatedRefID;
		int tempRelationCount = tempOLifE.getRelationCount(); //NBA130
		for (int i = 0; i < oLifE.getRelationCount(); i++) {
			relation = oLifE.getRelationAt(i);
			for (int j = 0; j < tempRelationCount; j++) { //NBA130
				tempRelation = tempOLifE.getRelationAt(j);
				if (relation.getRelationRoleCode() == tempRelation.getRelationRoleCode()
					&& relation.getOriginatingObjectType() == tempRelation.getOriginatingObjectType()
					&& relation.getRelatedObjectType() == tempRelation.getRelatedObjectType()) { //SPR2816
					if (relation.hasRelatedRefID()){
						relatedRefID = relation.getRelatedRefID();
					} else {
						relatedRefID = "";
					}
					if (tempRelation.hasRelatedRefID()){
						tempRelatedRefID = tempRelation.getRelatedRefID();
					} else {
						tempRelatedRefID = "";
					}
					if (relatedRefID.equals(tempRelatedRefID)) {
						relation.setKeys(tempRelation.getKeys());	
						relation.setId(tempRelation.getId());	//SPR3019
						relation.setBeneficiaryDesignation(tempRelation.getBeneficiaryDesignation());
						relation.setRelationDescription(tempRelation.getRelationDescription());
						//begin SPR2816
						 
						RelationProducerExtension relationProducerExtension = NbaUtils.getFirstRelationProducerExtension(relation);
                        RelationExtension relationExtension = NbaUtils.getFirstRelationExtension(relation);	//NBA208-10
                        if (relationProducerExtension == null && relationExtension == null) {	//NBA208-10
                            relation.setOLifEExtensionGhost(null);	//Allow to lazy initialize NBA208-10
                        } else { //NBA208-10						
                            if (relationProducerExtension != null) {
                                relationProducerExtension.setKeys(relation.getKeysForChildren(relation.getId()));
                            }
                            RelationProducerExtension tempRelationProducerExtension = NbaUtils.getFirstRelationProducerExtension(tempRelation);
                            if (tempRelationProducerExtension != null) {
                                if (relationProducerExtension == null) {
                                    OLifEExtension oLifEExtension = new OLifEExtension(); //NBA208-10
                                    relation.addOLifEExtension(oLifEExtension); //NBA208-10
                                    oLifEExtension.setRelationProducerExtension(tempRelationProducerExtension); //NBA208-10
                                } else {
                                    relationProducerExtension.setKeys(tempRelationProducerExtension.getKeys());
                                    //SPR3216 code deleted
                                }
                            } else if (relationProducerExtension != null) {
                                relationProducerExtension.setActionAdd();
                            }
                            //NBA208-10 code deleted
                            if (relationExtension != null) {
                                relationExtension.setKeys(relation.getKeysForChildren(relation.getId()));
                            }
                            RelationExtension tempRelationExtension = NbaUtils.getFirstRelationExtension(tempRelation);
                            if (tempRelationExtension != null) {
                                if (relationExtension == null) {
                                    OLifEExtension oLifEExtension = new OLifEExtension(); //NBA208-10
                                    relation.addOLifEExtension(oLifEExtension); //NBA208-10
                                    oLifEExtension.setRelationExtension(tempRelationExtension); //NBA208-10
                                } else {
                                    relationExtension.setKeys(tempRelationExtension.getKeys());
                                }
                            } else if (relationExtension != null) {
                                relationExtension.setActionAdd();
                            }
                        }	//NBA208-10						
						//end SPR2816
						break;
					}
				}
			}
		}
		//Begin NBA130
        UnsupportedPartyTypes upts = getUnsupportedPartyTypes(oLifE.getSourceInfo());
        if( null != upts) {
	        for (int i = 0; i < tempRelationCount; i++) {
	            tempRelation = tempOLifE.getRelationAt(i);
				if(isUnsupportedParty(upts, Long.toString(tempRelation.getRelationRoleCode()))) {
					oLifE.addRelation(tempRelation);
				}
			}
        }
		//End NBA130
	}
	/**
	 * Return true if an application Issue is being requested. Return false for a vantage complex change
	 */
	// SPR1931 New Method
	//NBA126 changed method signature, added NbaLob
	protected static boolean isIssue(NbaTXLife nbaTXLife, NbaLob caseLob) {
		//begin NBA126
		boolean issue = false;
		if (NbaConstants.PROC_ISSUE.equalsIgnoreCase(nbaTXLife.getBusinessProcess())) { //SPR2639
			issue = true;
		}
		if (NbaConstants.SYST_VANTAGE.equalsIgnoreCase(caseLob.getBackendSystem())
			&& (caseLob.getContractChgType() != null && NbaOliConstants.NBA_CHNGTYPE_COMPLEX_CHANGE == Long.parseLong(caseLob.getContractChgType()))) {
			issue = false;
		}
		return issue;
		//end NBA126
	}
	/**
	 * Merge database only RequirementInfoExtension into the nbaTXLife. 
	 * @param reqInfo the NbaTXLife RequirementInfo object
	 * @param temprReqInfo the database Requirement object
	 */
	//NBA130 New Method
	protected static void mergeDBOnlyRequirementInfoExtension(RequirementInfo reqInfo, RequirementInfo tempReqInfo) {
	    
	    RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		if (reqInfoExt == null){	//NBA208-10
			reqInfo.setOLifEExtensionGhost(null);	//Allow lazy initialization NBA208-10
		} else { //NBA208-10
            RequirementInfoExtension tempReqInfoExt = NbaUtils.getFirstRequirementInfoExtension(tempReqInfo);
            if (tempReqInfoExt != null) { //NBA208-10	        
                reqInfoExt.setKeys(tempReqInfoExt.getKeys());
                reqInfoExt.setAutoOrderIndicator(tempReqInfoExt.getAutoOrderIndicator());
                reqInfoExt.setAgentOrdered(tempReqInfoExt.getAgentOrdered());
                reqInfoExt.setCrossReference(tempReqInfoExt.getCrossReference());
                reqInfoExt.setFollowUpFreq(tempReqInfoExt.getFollowUpFreq());
                reqInfoExt.setFollowUpRequestNumber(tempReqInfoExt.getFollowUpRequestNumber());
                reqInfoExt.setFollowUpRequestRoute(tempReqInfoExt.getFollowUpRequestRoute());
                reqInfoExt.setMedicalIndicator(tempReqInfoExt.getMedicalIndicator());
                //AXAL3.7.38 code deleted
                reqInfoExt.setReviewCode(tempReqInfoExt.getReviewCode());
                reqInfoExt.setReviewDate(tempReqInfoExt.getReviewDate());
                reqInfoExt.setReviewedInd(tempReqInfoExt.getReviewedInd());
                reqInfoExt.setReviewID(tempReqInfoExt.getReviewID());
                reqInfoExt.setInitialRoute(tempReqInfoExt.getInitialRoute());
                reqInfoExt.setPhysicianPartyID(tempReqInfoExt.getPhysicianPartyID());
                reqInfoExt.setWorkitemID(tempReqInfoExt.getWorkitemID());
                reqInfoExt.setFollowUpDate(tempReqInfoExt.getFollowUpDate()); //NBA192
                reqInfoExt.setTrackingInfo(tempReqInfoExt.getTrackingInfo());
                reqInfoExt.setSuspendInfo(tempReqInfoExt.getSuspendInfo());
            } else { //NBA208-10
                reqInfoExt.setActionAdd(); //NBA208-10
            } //NBA208-10
        } //NBA208-10
	}
	/**
     * Match the requirements from the host with the database
     * 
     * @param policy
     * @param dbRequirement
     * @param notFoundReqs
     * @param count
     */
    //SPR3185 New Method
    protected static void matchRequirements(Policy policy, ArrayList dbRequirement, List notFoundReqs, int count) {
        boolean found = false;
        // SPR3290 code deleted
        for (int i = 0; i < count; i++) {
            RequirementInfo reqInfo = policy.getRequirementInfoAt(i);
            found = false;
            reqInfo.setAttachmentGhost(null);
            //Match that has been submitted and updated in the Database
            found = matchExistingRequirement(dbRequirement, notFoundReqs, reqInfo);
            //if there is no time stamp check and see if a newrequirement was added
            //to the host that the time stamp has not been send back yet.
            if (!found) {
                matchNewRequirement(dbRequirement, notFoundReqs, reqInfo);
            }
        }
    }
    /**
     * Match a new requirement to the database entry based on the status and type.
     * 
     * @param dbRequirement
     * @param notFoundReqs
     * @param found
     * @param reqInfo
     * @param tempCount
     * @param tempReqInfo
     */
    //SPR3185 New Method
    protected static void matchNewRequirement(ArrayList dbRequirement, List notFoundReqs, RequirementInfo reqInfo) {
        int tempCount = dbRequirement.size();
        boolean found = false;
        for (int j = 0; j < tempCount; j++) {
            RequirementInfo tempReqInfo = (RequirementInfo) dbRequirement.get(j);
            if (tempReqInfo.getAppliesToPartyID().equals(reqInfo.getAppliesToPartyID()) && tempReqInfo.getReqCode() == reqInfo.getReqCode()
                    && null == tempReqInfo.getHORequirementRefID() && tempReqInfo.getReqStatus() == reqInfo.getReqStatus()) {
                reqInfo.setId(tempReqInfo.getId()); //ACN013 Assure that ID containss database value
                mergeDBOnlyRequirementInfo(reqInfo, tempReqInfo);
                notFoundReqs.remove(tempReqInfo);
                found = true;
                break;
            }
        }
        //If there is no time stamp match and the statuses are different
        //assume the host status has been updated and match to type.
        if (!found) {
            for (int j = 0; j < tempCount; j++) {
                RequirementInfo tempReqInfo = (RequirementInfo) dbRequirement.get(j);
                if (tempReqInfo.getAppliesToPartyID().equals(reqInfo.getAppliesToPartyID()) && tempReqInfo.getReqCode() == reqInfo.getReqCode()) {
                    mergeDBOnlyRequirementInfo(reqInfo, tempReqInfo);
                    notFoundReqs.remove(tempReqInfo);
                    found = true;
                    break;
                }
            }
        }
    }
    /**
     * Attempt to match the requirement based on the time stamp generated by the host
     * 
     * @param dbRequirement
     * @param notFoundReqs
     * @param found
     * @param reqInfo
     * @param tempCount
     * @return boolean If a match was found
     */
    //SPR3185 New Method
    protected static boolean matchExistingRequirement(ArrayList dbRequirement, List notFoundReqs, RequirementInfo reqInfo) {
        int tempCount = dbRequirement.size();
        for (int j = 0; j < tempCount; j++) {
            RequirementInfo tempReqInfo = (RequirementInfo) dbRequirement.get(j);
            if (tempReqInfo.getAppliesToPartyID().equals(reqInfo.getAppliesToPartyID()) && tempReqInfo.getReqCode() == reqInfo.getReqCode()
                    && null != tempReqInfo.getHORequirementRefID() && tempReqInfo.getHORequirementRefID().equals(reqInfo.getHORequirementRefID())) {
                reqInfo.setId(tempReqInfo.getId()); //ACN013 Assure that ID containss database value
                mergeDBOnlyRequirementInfo(reqInfo, tempReqInfo);
                notFoundReqs.remove(tempReqInfo);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Restores cached SubstandardRating lists back to original policy tree for wrappered processing
     * @param ladh original policy tree
     * @param ratingLists Map of SubstandardRating lists to be restored
     */
    //SPR3203 New Method
    protected static void restoreNonProposedRatings(LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh, Map ratingListCache) {
        if (ladh.isLife()) {
            Life life = ladh.getLife();
            Coverage currCoverage = null;
            int covCount = life.getCoverageCount();
            for (int i = 0; i < covCount; i++) {
                LifeParticipant currLifeParticipant = null;
                currCoverage = life.getCoverageAt(i);
                int lifePartCount = currCoverage.getLifeParticipantCount();
                for (int j = 0; j < lifePartCount; j++) {
                    currLifeParticipant = currCoverage.getLifeParticipantAt(j);
                    if (ratingListCache.containsKey(currLifeParticipant.getId())) {
                        currLifeParticipant.getSubstandardRating().addAll((ArrayList) ratingListCache.get(currLifeParticipant.getId()));
                    }
                }
                int covOptionCount = currCoverage.getCovOptionCount();
                CovOption currCovOption = null;
                for (int j = 0; j < covOptionCount; j++) {
                    currCovOption = currCoverage.getCovOptionAt(j);
                    if (ratingListCache.containsKey(currCovOption.getId())) {
                        currCovOption.getSubstandardRating().addAll((ArrayList) ratingListCache.get(currCovOption.getId()));
                    }
                }
            }
        } else if (ladh.isAnnuity()) {
            Annuity annuity = ladh.getAnnuity();
            Rider currRider = null;
            int riderCount = annuity.getRiderCount();
            for (int i = 0; i < riderCount; i++) {
                currRider = annuity.getRiderAt(i);
                int covOptionCount = currRider.getCovOptionCount();
                CovOption currCovOption = null;
                for (int j = 0; j < covOptionCount; j++) {
                    currCovOption = currRider.getCovOptionAt(j);
                    if (ratingListCache.containsKey(currCovOption.getId())) {
                        currCovOption.getSubstandardRating().addAll((ArrayList) ratingListCache.get(currCovOption.getId()));
                    }
                }
            }
        }
    }
    
    /**
     * Cache all regular (non proposed) ratings from given rating list into passed map.
     * @param ratingList List of ratings to be looped through
     * @param parentID key to store cached values in map
     * @param ratingListCache cache map
     */
    //SPR3203 New Method
    protected static void cacheNonProposedRatings(List ratingList, String parentID, Map ratingListCache) {
        int origCount = ratingList.size();
        List currentRegularRatings = new ArrayList(); //list of non-proposed ratings on current object (LifeParticipant or CovOption)
        SubstandardRating currentRating = null;
        SubstandardRatingExtension currRatingExt = null;
        int cachedCount = 0;
        for (int i = 0; i < (origCount-cachedCount); i++) {
            currentRating = (SubstandardRating) ratingList.get(i);
            currRatingExt = NbaUtils.getFirstSubstandardExtension(currentRating);
            if (currRatingExt == null || !currRatingExt.getProposedInd()) {
                currentRegularRatings.add(currentRating);
                ratingList.remove(currentRating);
                i--;
                cachedCount++;
            }
        }
        if (currentRegularRatings.size() > 0) {
            ratingListCache.put(parentID, currentRegularRatings);
        }
    }
    
    /**
     * Merge database only Rider information into holding inquiry from backend
     * @param annuity Annuity object from holding inquiry
     */
    //SPR3203 New Method
    protected static void mergeDbOnlyRiderInfo(Annuity annuity){
		// SPR3290 code deleted
		int riderCount = annuity.getRiderCount();
		Rider origRider = null;
		for (int i = 0; i < riderCount; i++) {
		    origRider = annuity.getRiderAt(i);
		    mergeDbOnlyCovOptionInfo(origRider); 
		}
    }
    
    /**
     * Merge database only CovOption information into holding inquiry from backend
     * @param rider current Rider object from holding inquiry
     */
    //SPR3203 New Method
	protected static void mergeDbOnlyCovOptionInfo(Rider rider) {
		// SPR3290 code deleted
		int covOptionCount = rider.getCovOptionCount();
		CovOption origCovOption = null;
		for (int i = 0; i < covOptionCount; i++) {
		    origCovOption = rider.getCovOptionAt(i);
			mergeDbOnlySubstandardRatingInfo(origCovOption);
		}
	}
	
	/**
	 * Cache all regular wrappered ratings from policy tree. 
     * @param ratingListCache cache of rating lists
     * @param ladh
     */
    //SPR3203 New Method 
    protected static Map cacheWrapperedOnlyRatings(LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh) {
        Map ratingListCache = new HashMap(); 
        if (ladh.isLife()) {
            Life life = ladh.getLife();
            Coverage currCoverage = null;
            int coverageCount = life.getCoverageCount();
            for (int i = 0; i < coverageCount; i++) {
                currCoverage = life.getCoverageAt(i);
                //perform caching for life participants
                int lifePartCount = currCoverage.getLifeParticipantCount();
                LifeParticipant currLifeParticipant = null;
                for (int j = 0; j < lifePartCount; j++) {
                    currLifeParticipant = currCoverage.getLifeParticipantAt(j);
                    cacheNonProposedRatings(currLifeParticipant.getSubstandardRating(), currLifeParticipant.getId(), ratingListCache);
                }
                //perform caching for coverage options on current coverage object.
                CovOption currCovOption = null;
                int covOptionCount = currCoverage.getCovOptionCount();
                for (int j = 0; j < covOptionCount; j++) {
                    currCovOption = currCoverage.getCovOptionAt(j);
                    cacheNonProposedRatings(currCovOption.getSubstandardRating(), currCovOption.getId(), ratingListCache);
                }
            }
        } else if (ladh.isAnnuity()) {
            Annuity annuity = ladh.getAnnuity();
            Rider currRider = null;
            int riderCount = annuity.getRiderCount();
            for (int i = 0; i < riderCount; i++) {
                //perform caching for coverage options on current Rider object.
                currRider = annuity.getRiderAt(i);
                CovOption currCovOption = null;
                int covOptionCount = currRider.getCovOptionCount();
                for (int j = 0; j < covOptionCount; j++) {
                    currCovOption = currRider.getCovOptionAt(j);
                    cacheNonProposedRatings(currCovOption.getSubstandardRating(), currCovOption.getId(), ratingListCache);
                }
            }
        }
        return ratingListCache; 
    }
	/**
	 * This method retieves and deletes the contract from pending database if exists
	 * @param nbaDst an AWD work item containing identifying information
	 * @param nbaUser an AWD user
	 * @param conn the connection object
	 * @throws NbaBaseException
	 */
	//NBA187 New Method
	public NbaTXLife deleteContract(NbaTXLife nbaTxLife, NbaUserVO user) throws NbaBaseException {
	    String dbResult = NbaActionIndicator.ACTION_FAILED;
		TransResult transResult = new TransResult(); 
	    try {
		    nbaTxLife.toXmlString(); //get all data
	        Connection pendConn = NbaConnectionManager.borrowConnection(NbaConfiguration.PENDING_CONTRACT); 
			pendConn.setAutoCommit(false);
			getConnections().add(pendConn);
			NbaActionIndicator action = new NbaActionIndicator();
			action.setDelete();
			NbaAuditingContext.addAuditingContext(nbaTxLife,user); //audit changes
			nbaTxLife.getOLifE().processChanges(
				nbaTxLife.getBackendSystem(),
				nbaTxLife.getCarrierCode(),
				nbaTxLife.getPolicy().getPolNumber(),
				action,
				pendConn);
			NbaAuditorVO auditorVO = NbaAuditingContext.getTxnAuditObjectsFromStack(nbaTxLife); //audit changes
			transResult = createTransactionResult();
			addResultResponse(nbaTxLife, transResult);
			
			if(!NbaConfigurationConstants.NO_LOGGING.equals((NbaConfiguration.getInstance().getAuditConfiguration().getAuditLevel()))) {
				Connection auditConn = NbaConnectionManager.borrowConnection(NbaConfiguration.AUDITING_CONTRACT);
				auditConn.setAutoCommit(false);
				getConnections().add(auditConn);
				NbaAuditorFactory.getInstance().getAuditor().audit(auditorVO,auditConn);
			}
			commitConnections(getConnections()); // ALS4441			
			dbResult = NbaActionIndicator.ACTION_SUCCESSFUL; // ALS4441			
		} catch (NbaDataAccessException de) {
			getLogger().logException("deleteContract failed", de);	
		} catch (SQLException e) {
			getLogger().logException(e); 
			transResult = createExceptionResult(e.getMessage());
			addResultResponse(nbaTxLife, transResult);
			rollbackConnections();
		} finally {
			NbaAuditingContext.clearTxnObjectStack(nbaTxLife); 
		}
		if (NbaActionIndicator.ACTION_SUCCESSFUL.equals(dbResult)) {
			nbaTxLife.getOLifE().applyResult(dbResult, null);
		} else {
			List resultList = new ArrayList();
			resultList.add(transResult.getResultInfoAt(0));
			// Send only the first
			nbaTxLife.getOLifE().applyResult(dbResult, resultList);
		}
		return nbaTxLife;
	}
	/**
	 * Merge the phone information with the holding inquiry
	 * @param party the NbaTXLife Party object
	 * @param tempParty the database Party object
	 * @throws NbaDataAccessException
     */
    //NBA211 New Method
	protected static void mergeDBOnlyPhone(Party party, Party tempParty) {
	    int phoneCount = party.getPhoneCount();
		int tempPhoneCount = tempParty.getPhoneCount();
		for (int i = 0; i < phoneCount; i++) {
		    Phone phone = party.getPhoneAt(i);
			for (int temp = 0; temp < tempPhoneCount; temp++) {
			    Phone tempPhone = tempParty.getPhoneAt(i);
			    if (phone.getPhoneTypeCode() == tempPhone.getPhoneTypeCode()) {
			       phone.setBestDayToCallCC(tempPhone.getBestDayToCallCC());
			       phone.setBestTimeToCallFrom(tempPhone.getBestTimeToCallFrom());
			       phone.setBestTimeToCallTo(tempPhone.getBestTimeToCallTo());
			       break;
			    }
			}    
	       
	    }
	    
	}
	/**
	 * Merge the FormInstance information from the database to the holding inq. 
	 * @param nbaTXLife the NbaTXLife object
	 * @param conn the database connection
	 * @throws NbaDataAccessException 
	 */
	// NBA211 New Method
	protected static void mergeDbOnlyFormInstanceInfo(OLifE oLifE, OLifE tempOLifE) {			
		FormInstance formInstance;
        int count = tempOLifE.getFormInstanceCount();
        for (int i = 0; i < count; i++) {
            formInstance = tempOLifE.getFormInstanceAt(i);
            oLifE.addFormInstance(formInstance);
        }
	}
	/**
	 * Merge the Annuity object from the database to the holding inq. 
	 * @param annuity the Annuity object from holding inquiry 
	 * @throws NbaDataAccessException 
	 */
	// NBA176 New Method
	protected static void mergeDbOnlyAnnuityInfo(Annuity annuity) {			
	    mergeDbOnlyRiderInfo(annuity);
	    mergeDbOnlyPayoutInfo(annuity);
	}
	
	/**
	 * Merge the Payout object from the database to the holding inq. 
	 * @param annuity the Annuity object from holding inquiry 
	 * @throws NbaDataAccessException 
	 */
	// NBA176 New Method
	protected static void mergeDbOnlyPayoutInfo(Annuity annuity) {
        if (annuity.getPayoutCount() > 0) {
            ArrayList origPayouts = annuity.getPayout();
            Payout origPayout = annuity.getPayoutAt(0);
            annuity.setPayoutGhost(null); //Allow to lazy initialize
            annuity.setPayout(new ArrayList());
            List dbPayouts = annuity.getPayout();
            if (dbPayouts.size() > 0) {
                Payout dbPayout = (Payout) dbPayouts.get(0);
                origPayout.setStartDate(dbPayout.getStartDate());
                annuity.setPayout(origPayouts);
            }
        }
    }
	
	/**
	 * Merge the phone information with the holding inquiry
	 * @param party the NbaTXLife Party object
	 * @param tempParty the database Party object
	 * @throws NbaDataAccessException
     */
    //NBA211 New Method
	protected static void mergeDBOnlyAddress(Party origParty, Party dbParty) {
        int origAddressCount = origParty.getAddressCount();
        int dbAddressCount = dbParty.getAddressCount();
        for (int i = 0; i < origAddressCount; i++) {
            Address currOrigAddress = origParty.getAddressAt(i);
            for (int temp = 0; temp < dbAddressCount; temp++) {
                Address currDbaddress = dbParty.getAddressAt(i);
                if (currOrigAddress.getAddressTypeCode() == currDbaddress.getAddressTypeCode()) {
                    currOrigAddress.setYearsAtAddress(currDbaddress.getYearsAtAddress());
                    break;
                }
            }
        }
    }
	
	/**
	 * Returns the list of connections 
	 * @return
	 */
	 //NBA187 New Method
	public List getConnections() {
		return connections;
	}
	/**
	* Commits all the connections to the database
	* @param connections ArrayList object
	 * @throws NbaContractAccessException
	 * @throws NbaDataAccessException
	*/
	//NBA187 New Method
	public void commitConnections() throws NbaBaseException {
		try {
			commitConnections(getConnections());
		} finally {
			try {
				NbaConnectionManager.closeConnections((ArrayList) getConnections());
			} catch (SQLException sqle) {
				throw new NbaContractAccessException(NbaBaseException.CLOSE_CONNECTIONS_FAILED, sqle);
			}
		}
	}
	/**
	 * Rolls the database back if there is an error
	 * @throws NbaContractAccessException
	 */
	 //NBA187 New Method
	public void rollbackConnections() throws NbaContractAccessException {
		try {
			rollbackConnections(getConnections());
		} finally {
			try {
				NbaConnectionManager.closeConnections((ArrayList) getConnections());
			} catch (SQLException sqle) {
				throw new NbaContractAccessException(NbaBaseException.CLOSE_CONNECTIONS_FAILED, sqle);
			}
		}
	}
	//SR494086.6 New Method ADC Retrofit
	protected static boolean isDataFeedNeeded(NbaTXLife nbaTXLife, NbaDst nbaDst) {
		if (NbaUtils.isAdcApplication(nbaDst) && !isIssue(nbaTXLife, nbaDst.getNbaLob())) {
			return false;
}
 	 
		return true;
	}
	
	//APSL3313 New Method
	public static NbaTXLife doAuditInquiry(NbaTXLife nbaTXLifeRequest, int auditNumber) throws NbaBaseException {
		try {
			NbaTXLife nbaTXLife = null;
			if (!contractAvailable(nbaTXLifeRequest)) {
				return nbaTXLife;
			}
			String[] args = getAuditArgs(nbaTXLifeRequest, auditNumber);
			ArrayList results = NbaAuditDataBaseAccessor.getInstance().selectOLifE(args);
			if (results == null || results.size() < 1) {
				throw new NbaContractAccessException("Unable to retrieve information from Contract Database for " + args[1]);
			}
			nbaTXLife = responseFromQuery(nbaTXLifeRequest, results);
			return nbaTXLife;
		} catch (NbaBaseException e) {
			getLogger().logException(e);
			throw e;
		} catch (Throwable t) {
			NbaBaseException e = new NbaBaseException(t);
			getLogger().logException(t);
			throw e;
		}
	}
	
	//APSL3313 New Method
	protected static String[] getAuditArgs(NbaTXLife nbaTXLife, int auditNumber) {
		OLifE oLifE = nbaTXLife.getOLifE();
		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();
		String[] args = new String[5];
		args[0] = oLifE.getId();
		args[1] = policy.getPolNumber();
		args[2] = policy.getCarrierCode();
		args[3] = oLifE.getSourceInfo().getFileControlID();
		args[4] = Integer.toString(auditNumber);
		return args;
	}
	
	
	//APSL4508 BEGIN
		public static Policy selectPolicy(String poln) {
			Date startTime = Calendar.getInstance().getTime();
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Preparing to execute SELECT Query for Policy");
			}
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			Policy valueObject = new Policy(true);
			try {
				conn = NbaConnectionManager.borrowConnection(NbaConfiguration.PENDING_CONTRACT);
				stmt = conn.prepareStatement("SELECT COMPANYKEY,BACKENDKEY,PRODUCTCODE,JURISDICTION FROM \"POLICY\" WHERE PARENTIDKEY = 'Holding_1' AND CONTRACTKEY = ? ");
				stmt.setString(1, poln);
				rs = stmt.executeQuery();
				while (rs.next()) {
					valueObject.setCompanyKey(rs.getString("COMPANYKEY"));
					valueObject.setBackendKey(rs.getString("BACKENDKEY"));
					valueObject.setProductCode(rs.getString("PRODUCTCODE"));
					valueObject.setJurisdiction(rs.getString("JURISDICTION"));
					
				}
				
			} catch (Throwable t) {
				getLogger().logException("Exception during SELECT for Policy", t);
				return null;
			} finally {
				try {
					if (rs != null) {
						rs.close();
					}
					if (stmt != null) {
						stmt.close();
					}
					NbaConnectionManager.returnConnection(conn);
					logElapsedTime(startTime);
				} catch (Throwable t) {
					getLogger().logException("Exception during SELECT for Policy", t);
					return null;
				}
			}
			return valueObject;
		}
		//NBLXA-2353
		public static String selectEPolicyPrintCRDA(String poln) {
			Date startTime = Calendar.getInstance().getTime();
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Preparing to execute SELECT Query for EPolicyData");
			}
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			String printCRDA = null;
			try {
				conn = NbaConnectionManager.borrowConnection(NbaConfiguration.PENDING_CONTRACT);
				stmt = conn.prepareStatement("SELECT PRINTCRDA FROM \"EPOLICYDATA\" WHERE PARENTIDKEY = 'Policy_1' AND ACTIVE = '1' AND CONTRACTKEY = ? ");
				stmt.setString(1, poln);
				rs = stmt.executeQuery();
				while (rs.next()) {
					printCRDA = rs.getString("PRINTCRDA");
				}
				
			} catch (Throwable t) {
				getLogger().logException("Exception during SELECT for EPolicyData", t);
				return null;
			} finally {
				try {
					if (rs != null) {
						rs.close();
					}
					if (stmt != null) {
						stmt.close();
					}
					NbaConnectionManager.returnConnection(conn);
					logElapsedTime(startTime);
				} catch (Throwable t) {
					getLogger().logException("Exception during SELECT for Policy", t);
					return null;
				}
			}
			return printCRDA;
		}
		
		/**
		* Log the elapsed time.
		*/
		private static void logElapsedTime(Date startTime) {
			if (getLogger().isDebugEnabled()) {
				if (startTime != null) {
					Date endTime = java.util.Calendar.getInstance().getTime();
					float elapsed = ((float) (endTime.getTime() - startTime.getTime())) / 1000;
					StringBuffer elStr = new StringBuffer();
					elStr.append("Elapsed time: ");
					elStr.append(elapsed);
					elStr.append(" seconds ");
					getLogger().logDebug(elStr.toString());
				}
			}
		}
		//APSL4508
		
	/**
	 * New Method NBLXA2204 User Story 258812
	 *
	 * @param policyNumber
	 * @return HOUnderwriterName from applicationInfo
	 */
	public static String fetchHOUnderwriterName(String policyNumber) {
		String HOUnderWriterName = NbaTableConstants.EMPTY_STRING;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.PENDING_CONTRACT);
			String query = "select HOUNDERWRITERNAME from ApplicationInfo appInfo where  appInfo.contractkey = ?";
			if (conn != null) {
				pstmt = conn.prepareStatement(query);
			}
			if (!NbaUtils.isBlankOrNull(policyNumber)) {
				pstmt.setString(1, policyNumber);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					HOUnderWriterName = rs.getString("HOUNDERWRITERNAME");
				}

			}
		} catch (SQLException e) {
			getLogger().logException("Exception during SELECT HOUnderwriterName ", e);
			return null;
		} catch (NbaBaseException e) {
			getLogger().logException("Exception while borrowing connection for select HOUnderwriterName ", e);
			return null;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				NbaConnectionManager.returnConnection(conn);
			} catch (SQLException e) {
				getLogger().logException("Exception during SELECT HOUnderwriterName ", e);
				return null;
			}
		}
		return HOUnderWriterName;
	}
}