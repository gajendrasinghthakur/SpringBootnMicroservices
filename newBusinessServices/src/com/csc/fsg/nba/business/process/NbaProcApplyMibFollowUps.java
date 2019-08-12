package com.csc.fsg.nba.business.process;

/*
 * *******************************************************************************<BR>
 * Copyright 2015, Computer Sciences Corporation. All Rights Reserved.<BR>
 *
 * CSC, the CSC logo, nbAccelerator, and csc.com are trademarks or registered
 * trademarks of Computer Sciences Corporation, registered in the United States
 * and other jurisdictions worldwide. Other product and service names might be
 * trademarks of CSC or other companies.<BR>
 *
 * Warning: This computer program is protected by copyright law and international
 * treaties. Unauthorized reproduction or distribution of this program, or any
 * portion of it, may result in severe civil and criminal penalties, and will be
 * prosecuted to the maximum extent possible under the law.<BR>
 * *******************************************************************************<BR>
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.database.NbaDatabaseUtils;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.contract.auditor.NbaAuditorFactory;
import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaContractAccessException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.foundation.NbaAuditingContext;
import com.csc.fsg.nba.foundation.NbaBatchProcessingContext;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAuditorVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaMibPlanFDatabaseRequestVO;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaWorkItem;
import com.csc.fsg.nba.vo.nbaschema.NbaMibPlanF;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/**
 * This automated process retrieves MIB follow up responses from the NBA_MIB_PLANF table in the NBAAUXILIARY  schema and
 *  applies the responses to the matching contracts in the NBAPEND schema.  The number of responses which are processed by 
 *  each execution of this process is controlled by the "maxRows" property of RetrievePlanFResponses.xml.
 *  
 *  After processing the current set of records this process returns a response to the poller: 
 *  - If no MIB responses were found,  a "NOWORK" response is returned to cause the poller to enter into its sleep state.
 *  - If MIB responses were found, a "SUCCESSFULL" response is returned. The poller will update the success count and execute this
 *    automated process again if it has not been stopped
 *   
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA308</td><td>Version NB-1301</td><td>MIB Follow Ups</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see NbaAutomatedProcess
 */
public class NbaProcApplyMibFollowUps extends NbaAutomatedProcess {
    protected static final String ATTACHMENT_ID_BEGIN = "MIB_Follow_Up_Attachment_";
    protected static final String BACKENDKEY = "BACKENDKEY";
    protected static final String COMMA_SPACE = ", ";
    protected static final String COMPANY_COLON = "Company: ";
    protected static final String COMPANYKEY = "COMPANYKEY";
    protected static final String CONTRACT = "Contract: ";
    protected static final String CONTRACTKEY = "CONTRACTKEY";
    protected static final String DUPLICATE_RESPONSE = "Duplicate Response.";
    protected static final String ID = "ID";
    protected static final String INVALID_ATTACHMENT_DATA_ON_CONTRACT = "Invalid AttachmentData on Contract: ";
    protected static final String INVALID_TXLIFE_IN_MIB_FOLLOW_UP_RESPONSE = "Invalid TXLife in MIB Follow Up Response.";
    protected static final String MIB_FOLLOW_UP_RESPONSE = "MIB Follow Up Response";
    protected static final String MIB_PLAN_F_DATABASE_ACCESS_BP = "MibPlanFDatabaseAccessBP";
    protected static final String MULTIPLE_MATCHING_CONTRACTS_LOCATED = "Multiple matching contracts located. ";
    protected static final String OLIFE = "<OLifE";
    protected static final String PARENTIDKEY = "PARENTIDKEY";
    protected static final String REQUIREMENTINFOUNIQUEID = "REQUIREMENTINFOUNIQUEID";
    protected static final String SELECT_POLICY = "SELECT PARENTIDKEY,CONTRACTKEY,COMPANYKEY,BACKENDKEY,ID FROM \"POLICY\" WHERE ID = ? AND CONTRACTKEY = ? AND COMPANYKEY = ? AND BACKENDKEY = ?";
    protected static final String SELECT_REQUIREMENTINFO = "SELECT PARENTIDKEY,CONTRACTKEY,COMPANYKEY,BACKENDKEY,ID,REQUIREMENTINFOUNIQUEID FROM \"REQUIREMENTINFO\" WHERE REQUIREMENTINFOUNIQUEID = ?";
    protected static final String TRUE_STRING = Integer.toString(NbaConstants.TRUE);
    protected static final String UNABLE_TO_LOCATE_A_MATCHING_CONTRACT = "Unable to locate a matching contract.";
    protected static final String UNDERSCORE = "_";
    protected static final long PENDING_INFORCE = 8;
    protected static final long INFORCE = 25;
    protected String auditLevel;
    protected String businessProcess;
    protected String company;
    protected String contractNumber;
    protected NbaMibPlanF currentNbaMibPlanF;
    protected int recordsProcessed;
    protected List responseMessages;
    protected Date today;

    /**
     * Default constructor. 
     */
    public NbaProcApplyMibFollowUps() {
        super();
    }

    /**
     * Apply the MIB follow up responses to the matching contracts in the NBAPEND schema.
     * For each response:
     * - query the NBAPEND schema for RequirementInfo rows which match the TrackingId of the response
     *      - if no RequirementInfo records match, update the MIB follow up to signal the it could not be matched.
     *      - if multiple RequirementInfo records match, update the MIB follow up to signal that multiple matching contracts were found,
     *      and identify the contract numbers in the error message.
     *      - if a single match was found, apply the information from the MIB follow up responses to the matching contract.
     *      - update the  MIB follow up response with the success/failure information.  If successful, also commit the changes
     *      to the matching contract.
     * @throws NbaBaseException
     */
    protected void applyFollowupRecords() throws NbaBaseException {
        int count = getResponseMessages().size();
        for (int i = 0; i < count; i++) {
            setRequirementInfo(null);
            setCurrentNbaMibPlanF((NbaMibPlanF) getResponseMessages().get(i));
            List requirementInfoList = getRequirementInfoFor(getCurrentNbaMibPlanF().getTrackingId());
            int requirementInfoListCount = requirementInfoList.size();
            if (requirementInfoListCount == 0) {
                markFollowUpFailed(UNABLE_TO_LOCATE_A_MATCHING_CONTRACT);
            } else if (requirementInfoListCount > 1) {
				if (requirementInfoListCount == 2) {
					for (int j = 0; j < requirementInfoListCount; j++) {
						RequirementInfo tempRequirementInfo = (RequirementInfo) requirementInfoList.get(j);
						if (tempRequirementInfo.getRequirementInfoUniqueID().contains(tempRequirementInfo.getContractKey())) {
							requirementInfoList.remove(j);
							break;
						}
					}
					setRequirementInfo((RequirementInfo) requirementInfoList.get(0));
					applyFollowupToContract();
				} else {
					StringBuffer buff = new StringBuffer(MULTIPLE_MATCHING_CONTRACTS_LOCATED);
                for (int j = 0; j < requirementInfoListCount; j++) {
                    RequirementInfo tempRequirementInfo = (RequirementInfo) requirementInfoList.get(j);
                    if (j > 0) {
                        buff.append(COMMA_SPACE);
                    }
                    buff.append(COMPANY_COLON).append(tempRequirementInfo.getCompanyKey());
                    buff.append(CONTRACT).append(tempRequirementInfo.getContractKey());
                }
                markFollowUpFailed(buff.toString());
            	}
            } else {
                setRequirementInfo((RequirementInfo) requirementInfoList.get(0));
                applyFollowupToContract();
            }
            commitCurrentData();
        }
    }

    /**
     * Apply the information from the MIB follow up responses to the matching contract.
     * Verify that the MIB follow up has not been previously applied to the contract.
     * If it has been previously applied, set the RequirementInfo to null to prevent it from being updated.
     * If it has not, add an Attachment to the RequirementInfo with the following value:
     *      id - "MIB_Follow_Up_Attachment_" + TransRefGUID (of the MIB follow up)
     *      description - "MIB Follow Up Response"
     *      dateCreated - today
     *       userCode - current user id
     *      attachmentBasicType - 1 (Text)
     *      attachmentType - 1000500005 (MIB Follow Up)
     *      attachmentData
     *              tc - 8 (String)
     *              pDDATA - contents of the FOLLOWUP_DATA value of the current NBA_MIB_PLANF record
     * 
     */
    protected void applyFollowupToContract() {
        setCompany(getRequirementInfo().getCompanyKey());
        setContractNumber(getRequirementInfo().getContractKey());
        if (isDuplicate()) {
            getCurrentNbaMibPlanF().setCompanyCode(getCompany());
            getCurrentNbaMibPlanF().setContractNumber(getContractNumber());
            setRequirementInfo(null); //Do not update
        } else {
            Attachment attachment = new Attachment();
            attachment.setActionAdd();
            attachment.setId(getAttachmentId());
            attachment.setDescription(MIB_FOLLOW_UP_RESPONSE);
            attachment.setDateCreated(getToday());
            attachment.setUserCode(getUser().getUserID());
            attachment.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT);
            attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_MIB_FOLLOW_UP);
            AttachmentData attachmentData = new AttachmentData();
            attachment.setAttachmentData(attachmentData);
            attachmentData.setTc(Long.toString(NbaOliConstants.OLI_VARIANT_STRING));
            attachmentData.setPCDATA(getCurrentNbaMibPlanF().getData());
            getRequirementInfo().addAttachment(attachment);
			if (getRequirementInfo().getOLifEExtension().size() > 0
					&& getRequirementInfo().getOLifEExtensionAt(0).getRequirementInfoExtension() != null) {
				getRequirementInfo().getOLifEExtensionAt(0).getRequirementInfoExtension().setMIBPlanFReviewedInd(false);
				getRequirementInfo().getOLifEExtensionAt(0).getRequirementInfoExtension().setActionUpdate();

			}
        }
    }

    /**
     * Commit all the connections. 
     * @param connections List object
     * @throws NbaDataAccessException
     */
    protected void commitConnections(List connections) throws NbaDataAccessException {
        Connection conn = null;
        boolean connCommitted = false;
        try {
            for (int i = 0; i < connections.size(); i++) {
                conn = (Connection) connections.get(i);
                conn.commit();
                connCommitted = true;
            }
        } catch (SQLException ex) {
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
     * Commit the changes to the NBAPEND and NBAAUXILIARY schemas.
     * If a RequirementInfo is present, commit the changes to it.
     * If the MIB follow up was correctly matched with a contract, set the Matched incicator and set the 
     * Company Code and Contract number to the  values of the matched contract. Set the ContractStatus
     * to the value of Policy.PolicyExtension.PendingContractStatus
     * Commit the MIB follow up response record and increment the number of records processed.
     * @throws NbaBaseException
     */
    protected void commitCurrentData() throws NbaBaseException {
        if (getRequirementInfo() != null) {
            try {
                commitRequirementInfo();
                uwNotificationProcess();
                
            } catch (NbaBaseException e) {
                markFollowUpFailed(e.toString());
            }
        }
        if (!NbaMibPlanF.STATUS_ERROR.equals(getCurrentNbaMibPlanF().getStatusCode())) {
            getCurrentNbaMibPlanF().setStatusCode(NbaMibPlanF.STATUS_APPLIED_FOLLOW_UP);
            getCurrentNbaMibPlanF().setCompanyCode(getCompany());
            getCurrentNbaMibPlanF().setContractNumber(getContractNumber());
            List policyList = getPolicyList();
            if (!policyList.isEmpty()) {
                PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension((Policy) policyList.get(0));
                if (policyExtension != null) {
                    getCurrentNbaMibPlanF().setContractStatus(policyExtension.getPendingContractStatus());
                }
            }
            getCurrentNbaMibPlanF().setErrorMessage("  ");
        }
        NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO = new NbaMibPlanFDatabaseRequestVO();
        nbaMibPlanFDatabaseRequestVO.setOperation(NbaMibPlanFDatabaseRequestVO.UPDATE_RESPONSES);
        nbaMibPlanFDatabaseRequestVO.setNbaMibPlanFResponseList(new ArrayList());
        nbaMibPlanFDatabaseRequestVO.getNbaMibPlanFResponseList().add(getCurrentNbaMibPlanF());
        AccelResult accelResult = (AccelResult) ServiceHandler.invoke(MIB_PLAN_F_DATABASE_ACCESS_BP, ServiceContext.currentContext(),
                nbaMibPlanFDatabaseRequestVO);
        NewBusinessAccelBP.processResult(accelResult);
        setRecordsProcessed(getRecordsProcessed() + 1);
    }

    /**
     * Commit the changes to the RequirementIfo to the NBAPEND and NBAAUDIT schemas.
     * @throws NbaBaseException
     */
    protected void commitRequirementInfo() throws NbaBaseException {
        Connection pendConn = null;
        Connection auditConn = null;
        ArrayList connections = new ArrayList();
        try {
            pendConn = NbaConnectionManager.borrowConnection(NbaConfiguration.PENDING_CONTRACT);
            pendConn.setAutoCommit(false);
            connections.add(pendConn);
            NbaAuditingContext.updateAuditingContext(getContractNumber(), getCompany(), getRequirementInfo().getBackendKey(), getUser().getUserID(),
                    getBusinessProcess(), getAuditLevel());
            if (NbaConfiguration.isConfigurationEnabledBatch() && NbaDatabaseUtils.isBatchEnabled(pendConn)) {
                Statement batchStmt = NbaBatchProcessingContext.getStatementForBatch(getContractNumber(), getRequirementInfo().getBackendKey(),
                        getCompany(), pendConn);
                if (batchStmt != null) {
                    batchStmt.executeBatch();
                    NbaBatchProcessingContext.clear(getContractNumber(), getRequirementInfo().getBackendKey(), getCompany(), pendConn);
                }
            }
            getRequirementInfo().processChanges(getRequirementInfo().getKeysForChildren(getRequirementInfo().getId()),
                    getRequirementInfo().getActionIndicator(), pendConn);
            if (!NbaConfigurationConstants.NO_LOGGING.equals(getAuditLevel())) {
                NbaAuditorVO auditorVO = NbaAuditingContext.getTxnAuditObjectsFromStack(getContractNumber(), getRequirementInfo().getBackendKey(),
                        getCompany());
                auditConn = NbaConnectionManager.borrowConnection(NbaConfiguration.AUDITING_CONTRACT);
                auditConn.setAutoCommit(false);
                connections.add(auditConn);
                NbaAuditorFactory.getInstance().getAuditor().audit(auditorVO, auditConn);
                if (NbaConfiguration.isConfigurationEnabledBatch() && NbaDatabaseUtils.isBatchEnabled(auditConn)) {
                    Statement batchStmt = NbaBatchProcessingContext.getStatementForBatch(getContractNumber(), getRequirementInfo().getBackendKey(),
                            getCompany(), auditConn);
                    if (batchStmt != null) {
                        batchStmt.executeBatch();
                        NbaBatchProcessingContext.clear(getContractNumber(), getRequirementInfo().getBackendKey(), getCompany(), auditConn);
                    }
                }
            }
            commitConnections(connections);
        } catch (NbaBaseException nbe) {
            getLogger().logException(nbe);
            rollbackConnections(connections);
            throw nbe;
        } catch (SQLException e) {
            getLogger().logException(e);
            rollbackConnections(connections);
            throw new NbaContractAccessException("Unable to commit contract data", e);
        } finally {
            try {
                NbaConnectionManager.closeConnections(connections);
            } catch (SQLException sqle) {
                throw new NbaContractAccessException(NbaBaseException.CLOSE_CONNECTIONS_FAILED, sqle);
            }
            NbaAuditingContext.clearTxnObjectStack(getContractNumber(), getRequirementInfo().getBackendKey(), getCompany());
        }
    }

    /**
     * Retrieve MIB follow up responses from the NBA_MIB_PLANF table in the NBAAUXILIARY  schema.
     * Apply the responses to the matching contracts in the NBAPEND schema.
     * Return to the caller after processing the records.
     * @throws Exception 
     */
    protected void doProcess() throws Exception {
        retrieveConfigurationValues();
        setBusinessProcess(NbaUtils.getBusinessProcessId(getUser()));
        setRequirementInfo(null);
        setRecordsProcessed(0);
        retrieveFollowupRecords();
        applyFollowupRecords();
    }

    /**
     * Retrieve MIB follow up responses from the NBA_MIB_PLANF table in the NBAAUXILIARY  schema.
     * Apply the responses to the matching contracts in the NBAPEND schema.
     * Return to the caller after processing the records.  
     *      - If no MIB responses were found,  return a "NOWORK" response to cause the poller to enter into its sleep state.
     *      - If MIB responses were found, return a "SUCCESSFULL" response. The poller will update the success count and invoke this
     *      logic again if it has not been stopped.
     * @param currentUser the user for whom the work was retrieved, in this case APCLSCHK.
     * @param work the AWD case to be processed
     * @return NbaAutomatedProcessResult containing the results of the process
     * @throws NbaBaseException 
     */

    public NbaAutomatedProcessResult executeProcess(NbaUserVO currentUser, NbaDst work) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Begin Execution of Apply MIB Follow Ups.");
        }
        try {
            setToday(new Date());
            setUser(currentUser);
            doProcess();
        } catch (NbaBaseException e) {
            e.forceFatalExceptionType();
            throw e;
        } catch (Exception e) {
            NbaBaseException nbaBaseException = new NbaBaseException(e.toString(), e, NbaExceptionType.FATAL);
            throw nbaBaseException;
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("End Execution of Apply MIB Follow Ups. Records processed = " + getRecordsProcessed());
        }
        if (getRecordsProcessed() > 0) {
            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
        } else {
            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
        }
        getResult().setCountSuccessful(getRecordsProcessed());
        return getResult();
    }

    /**
     * Query the Attachment table of the NBAPEND schema to generate a unique id for the new Attachment.
     */
    protected String getAttachmentId() {
        int highNum = 0;
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Preparing to execute SELECT Query for Attachment");
        }
        if (getRequirementInfo() != null) {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                conn = NbaConnectionManager.borrowConnection(NbaConfiguration.PENDING_CONTRACT);
                stmt = conn.prepareStatement("SELECT ID  FROM \"ATTACHMENT\" WHERE  CONTRACTKEY = ? AND COMPANYKEY = ? AND BACKENDKEY = ?");
                stmt.setString(1, getRequirementInfo().getContractKey());
                stmt.setString(2, getRequirementInfo().getCompanyKey());
                stmt.setString(3, getRequirementInfo().getBackendKey());
                rs = stmt.executeQuery();
                while (rs.next()) {
                    String id = rs.getString(ID);
                    String last = id.substring(id.lastIndexOf(UNDERSCORE) + 1, id.length());
                    int newNum = NbaUtils.isInteger(last) ? (new Integer(last)).intValue() : -1;
                    if (newNum > highNum) {
                        highNum = newNum;
                    }
                }
            } catch (Throwable t) {
                getLogger().logException("Exception during SELECT for Attachment", t);
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
                } catch (Throwable t) {
                    getLogger().logException("Exception during SELECT for Attachment", t);
                    return null;
                }
            }
        }
        return (new StringBuffer(Attachment.$ATTACHMENT).append(UNDERSCORE).append(highNum + 1)).toString();
    }

    /**
    * Retrieve the audit level for Audit database processing
    */
    protected String getAuditLevel() {
        return auditLevel;
    }

    /**
    * Retrive the current business process
    */
    protected String getBusinessProcess() {
        return businessProcess;
    }

    /**
    * Retrieve  the company code for the current contract
    */
    protected String getCompany() {
        return company;
    }

    /**
    * Retrieve the contract number for the current contract
    */
    protected String getContractNumber() {
        return contractNumber;
    }

    /**
    * Return the current NBA_MIB_PLANF result record
    */
    protected NbaMibPlanF getCurrentNbaMibPlanF() {
        return currentNbaMibPlanF;
    }

    /**
    * Query the Policy table of the NBAPEND schema to locate the Policy parent of the current RequirementInfo.
    */
    protected List getPolicyList() {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Preparing to execute SELECT Query for Policy");
        }
        ArrayList results = new ArrayList();
        if (getRequirementInfo() != null) {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                conn = NbaConnectionManager.borrowConnection(NbaConfiguration.PENDING_CONTRACT);
                stmt = conn.prepareStatement(SELECT_POLICY);
                stmt.setString(1, getRequirementInfo().getParentIdKey());
                stmt.setString(2, getRequirementInfo().getContractKey());
                stmt.setString(3, getRequirementInfo().getCompanyKey());
                stmt.setString(4, getRequirementInfo().getBackendKey());
                rs = stmt.executeQuery();
                while (rs.next()) {
                    Policy policy = new Policy(true);
                    policy.setParentIdKey(rs.getString(PARENTIDKEY));
                    policy.setContractKey(rs.getString(CONTRACTKEY));
                    policy.setCompanyKey(rs.getString(COMPANYKEY));
                    policy.setBackendKey(rs.getString(BACKENDKEY));
                    policy.setId(rs.getString(ID));
                    results.add(policy);
                }
                return results;
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
                } catch (Throwable t) {
                    getLogger().logException("Exception during SELECT for Policy", t);
                    return null;
                }
            }
        }
        return results;
    }

    /**
    * Retrieve the number NBA_MIB_PLANF results successfully applied to contracts
    */
    protected int getRecordsProcessed() {
        return recordsProcessed;
    }

    /**
     * Query the RequirementInfo table in the NBAPEND schema for retrieve the rows for which the 
     * REQUIREMENTINFOUNIQUEID matches the uniqueId. For each matching row, return a RequirementInfo
     * object containing the key fields of the matching rows. 
     * @param uniqueId
     * @return a List of RequirementInfo objects containing the key fields of the matching rows. 
     * @throws NbaBaseException
     */
    protected List getRequirementInfoFor(String uniqueId) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Preparing to execute SELECT Query for RequirementInfo");
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList results = new ArrayList();
        try {
            conn = NbaConnectionManager.borrowConnection(NbaConfiguration.PENDING_CONTRACT);
            stmt = conn.prepareStatement(SELECT_REQUIREMENTINFO);
            stmt.setString(1, uniqueId);
            rs = stmt.executeQuery();
            RequirementInfo tempRequirementInfo = null;
            while (rs.next()) {
                tempRequirementInfo = new RequirementInfo(true);
                tempRequirementInfo.setParentIdKey(rs.getString(PARENTIDKEY));
                tempRequirementInfo.setContractKey(rs.getString(CONTRACTKEY));
                tempRequirementInfo.setCompanyKey(rs.getString(COMPANYKEY));
                tempRequirementInfo.setBackendKey(rs.getString(BACKENDKEY));
                tempRequirementInfo.setId(rs.getString(ID));
                tempRequirementInfo.setRequirementInfoUniqueID(rs.getString(REQUIREMENTINFOUNIQUEID));
                results.add(tempRequirementInfo);
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().logDebug("Returned " + results.size() + " rows");
            }
            return results;
        } catch (NbaBaseException t) {
            getLogger().logException("Exception during SELECT for RequirementInfo", t);
            throw t;
        } catch (Throwable t) {
            getLogger().logException("Exception during SELECT for RequirementInfo", t);
            throw new NbaDataAccessException(t);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                NbaConnectionManager.returnConnection(conn);
            } catch (Throwable t) {
                getLogger().logException("Exception during SELECT for RequirementInfo", t);
                return results;
            }
        }
    }

    /**
    * Return the list containing the NBA_MIB_PLANF query results
    */
    protected List getResponseMessages() {
        return responseMessages;
    }

    /**
     * Retrieve the current date
     */
    protected Date getToday() {
        return today;
    }

    /**
     * Verify that the MIB follow up has not been previously applied to the contract.
     * If the RequirementInfo hase an MIB Follow Up Attachment (type 1000500005) whose
     * AttachmentData.PCDATA matches the current MIB follow up data, the current
     * MIB follow up has already been applied to the contract.  Mark the current MIB follow up
     * as a duplicate and return true. 
     * If no duplicates are found return false. 
     * @param count - the number of Attachments on the current RequirementInfo
     * @return boolean indicating whether the follow up has been previously applied.
     */
    protected boolean isDuplicate() {
        int count = getRequirementInfo().getAttachmentCount();
        String newAttachmentData = getCurrentNbaMibPlanF().getData();
        int newOlifeStart = newAttachmentData.indexOf(OLIFE);
        if (newOlifeStart > -1) {
            for (int i = 0; i < count; i++) {
                Attachment attachment = getRequirementInfo().getAttachmentAt(i);
                if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_MIB_FOLLOW_UP) {
                    AttachmentData attachmentData = attachment.getAttachmentData();
                    int oldOlifeStart = attachmentData.getPCDATA().indexOf(OLIFE);
                    if (oldOlifeStart > -1) {
                        if (newAttachmentData.substring(newOlifeStart).equals(attachmentData.getPCDATA().substring(oldOlifeStart))) {   //Compare starting at the <OLifE> 
                            markFollowUpFailed(DUPLICATE_RESPONSE);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Indicate that the MIB follow up could not be applied to a contract and set the error message.
     * @param message
     */
    protected void markFollowUpFailed(String message) {
        getCurrentNbaMibPlanF().setStatusCode(NbaMibPlanF.STATUS_ERROR);
        getCurrentNbaMibPlanF().setErrorMessage(message);
    }

    /**
     * Retrieve configuration values to be used in processing.  
     * If MIB follow ups are not supported, throw a fatal exception to cause the poller to error stop.
     * @throws NbaBaseException
     */
    protected void retrieveConfigurationValues() throws NbaBaseException {
        NbaConfiguration config = NbaConfiguration.getInstance();
        String followUpInd = config.getBusinessRulesAttributeValue(NbaConfigurationConstants.MIB_FOLLOWUP_INDICATOR);
        if (!TRUE_STRING.equals(followUpInd)) {
            throw new NbaBaseException("Support for MIB Follow Ups is not configured.", NbaExceptionType.FATAL);
        }
        try {
            setAuditLevel(NbaConfiguration.getInstance().getAuditConfiguration().getAuditLevel()); //Set audit level for commits
        } catch (NbaBaseException e) {
            //default logging level is NO Logging
            setAuditLevel(NbaConfigurationConstants.NO_LOGGING);
        }
    }

    /**
     * Retrieve MIB follow up responses from the NBA_MIB_PLANF table in the NBAAUXILIARY  schema.
     * The maximum number of responses  returned from the table is controlled by the "maxRows" property of RetrievePlanFResponses.xml.  
     * @throws NbaBaseException
     */
    protected void retrieveFollowupRecords() throws NbaBaseException {
        NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO = new NbaMibPlanFDatabaseRequestVO();
        nbaMibPlanFDatabaseRequestVO.setOperation(NbaMibPlanFDatabaseRequestVO.RETRIEVE_RESPONSES);
        nbaMibPlanFDatabaseRequestVO.getKeyMap().put(NbaMibPlanF.STATUS_COLUMN, NbaMibPlanF.STATUS_UNAPPLIED_FOLLOW_UP);
        AccelResult accelResult = (AccelResult) ServiceHandler.invoke(MIB_PLAN_F_DATABASE_ACCESS_BP, ServiceContext.currentContext(),
                nbaMibPlanFDatabaseRequestVO);
        NewBusinessAccelBP.processResult(accelResult);
        nbaMibPlanFDatabaseRequestVO = (NbaMibPlanFDatabaseRequestVO) accelResult.getFirst();
        setResponseMessages(nbaMibPlanFDatabaseRequestVO.getNbaMibPlanFResponseList());
    }

    /**
     *Roll back all connections.
     * @param connections ArrayList  
     */
    protected void rollbackConnections(List connections) {
        for (int i = 0; i < connections.size(); i++) {
            Connection conn = (Connection) connections.get(i);
            try {
                conn.rollback();
            } catch (SQLException e) {
                getLogger().logException("Connection rollback failed", e);
            }
        }
    }
    
    /**
     *UW Notification for follow up.
     * 
     */
    
	protected void uwNotificationProcess() throws NbaBaseException {
		NbaSearchVO searchVO = null;
		List results = null;
		NbaDst nbaDst = null;
		Map deOink = new HashMap();
		try {
			searchVO = searchContract(getContractNumber(), getUser());
			if (searchVO != null && searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
				results = searchVO.getSearchResults();
				nbaDst = retrieveWorkItem((NbaSearchResultVO) results.get(0), user);
				setWork(nbaDst);
				setNbaTxLife(doHoldingInquiry());
				if (nbaTxLife.getPolicy().getPolicyStatus() == INFORCE || nbaTxLife.getPolicy().getPolicyStatus() == PENDING_INFORCE) {
					if (NbaConstants.A_QUEUE_UNDERWRITER_HOLD.equals(getWork().getQueue())) {
						deOink.put(NbaVpmsConstants.A_SETSTATUSFORUW_IND, "true");
						getWork().getNbaLob().setActionUpdate();
						setStatus(getWork(), NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, nbaTxLife, deOink);
						getWork().getNbaLob().setRouteReason("MIB Plan F Code received. Need review");

					} else if (!(getWork().getNbaLob().getUndwrtQueue().equalsIgnoreCase(getWork().getQueue()))) {
						WorkItem transaction = new WorkItem();
						deOink.put(NbaVpmsConstants.A_SETSTATUSFORUW_IND, "true");
						NbaDst nbadstforMiscWork = new NbaDst();
						nbadstforMiscWork.setUserID(getUser().getUserID());
						nbadstforMiscWork.setPassword(getUser().getPassword());
						try {
							nbadstforMiscWork.addTransaction(transaction);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						// set Business Area, Work type and Status
						transaction.setBusinessArea(getWork().getBusinessArea());
						transaction.setWorkType(NbaConstants.A_WT_MISC_WORK);
						transaction.setLobData(getWork().getNbaLob().getLobs());
						// setStatus(nbadstforMiscWork, NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, nbaTxLife, deOink);
						NbaTransaction aTransaction = null;
						NbaProcessStatusProvider provider = new NbaProcessStatusProvider(
								new NbaUserVO(NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, ""), nbadstforMiscWork, getNbaTxLife(), deOink);
						aTransaction = getWork().addTransaction(NbaConstants.A_WT_MISC_WORK, provider.getPassStatus());
						aTransaction.getWorkItem().setLobData(getWork().getNbaLob().getLobs());
						aTransaction.getNbaLob().setQueueEntryDate(new Date());
						aTransaction.getNbaLob().setRouteReason("MIB Plan F Code received. Need review");
						aTransaction.getNbaLob().setUnderwriterActionLob(NbaOliConstants.OLI_MIB_PLANF_REVIEW_ACTION);// NBLXA-2433
						// createMiscWorkTransaction(getWork(), "MIB Plan F Code received. Need review");
					}
					WorkflowServiceHelper.updateWork(getUser(), getWork());
				}
				unlockCase();
			}
		} catch (NbaLockedException exception) {

			if (results != null && !results.isEmpty()) {
				nbaDst = retrieveWorkItemForLockedCase((NbaSearchResultVO) results.get(0));
				if (!(nbaDst.getNbaLob().getUndwrtQueue().equalsIgnoreCase(nbaDst.getQueue()))) {
					createMiscWorkTransaction(nbaDst, "MIB Plan F Code received for Locked case. Need review");
				}
				// unlockCase();

			}
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
		WorkItem transaction = new WorkItem();
		Map deOink = new HashMap();
		deOink.put(NbaVpmsConstants.A_SETSTATUSFORUW_IND, "true");
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
		nbadstforMiscWork.getNbaLob().setUnderwriterActionLob(NbaOliConstants.OLI_MIB_PLANF_REVIEW_ACTION);//NBLXA-2433 US 372704
		NbaUtils.addGeneralComment(nbadstforMiscWork, getUser(), reason);
		transaction.setCreate("Y");
		updateWork(getUser(), nbadstforMiscWork);
		unlockWork(getUser(), nbadstforMiscWork);
	}

	/**
	 * Use the <code>NbaProcessStatusProvider</code> to determine the work item's next status. If a new status is returned back from the VP/MS model,
	 * then only update the transaction's status.
	 * 
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
	 * Use the <code>NbaProcessStatusProvider</code> to determine the work item's next status. If a new status is returned back from the VP/MS model,
	 * then only update the transaction's status.
	 * 
	 * @param contractKey
	 * @param User
	 * @throws NbaBaseException
	 */

	public NbaSearchVO searchContract(String contractKey, NbaUserVO user) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		return searchVO;
	}
	
	
	/**
	 * Retrieve work item for locked case
	 * @param resultVO
	 * @return
	 * @throws NbaBaseException
	 */
	public NbaDst retrieveWorkItem(NbaSearchResultVO resultVO, NbaUserVO user) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setNbaUserVO(user);
		retOpt.setWorkItem(resultVO.getWorkItemID(), true);
		retOpt.setLockWorkItem();
		aWorkItem = WorkflowServiceHelper.retrieveWork(user, retOpt);
		return aWorkItem;
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
	
	protected void unlockCase() throws NbaBaseException {
		unlockWork(getWork());
		NbaContractLock.removeLock(getWork(), getUser());
	}
	

	/**
	 * Set the audit level for Audit database processing
	 */
	protected void setAuditLevel(String value) {
		this.auditLevel = value;
	}

    /**
    * Set the current business process
    */
    protected void setBusinessProcess(String value) {
        this.businessProcess = value;
    }

    /**
    * Set the company code for the current contract
    */
    protected void setCompany(String value) {
        this.company = value;
    }

    /**
    * Set the contract number for the current contract
    */
    protected void setContractNumber(String value) {
        this.contractNumber = value;
    }

    /**
    * Set the current NBA_MIB_PLANF result record
    */
    protected void setCurrentNbaMibPlanF(NbaMibPlanF value) {
        this.currentNbaMibPlanF = value;
    }

    /**
    * Set the number NBA_MIB_PLANF results successfully applied to contracts
    */
    protected void setRecordsProcessed(int value) {
        this.recordsProcessed = value;
    }

    /**
    * Set the list containing the NBA_MIB_PLANF query results
    */
    protected void setResponseMessages(List value) {
        this.responseMessages = value;
    }

    /**
    * Set the current date
    */
    protected void setToday(Date value) {
        this.today = value;
    }
}
