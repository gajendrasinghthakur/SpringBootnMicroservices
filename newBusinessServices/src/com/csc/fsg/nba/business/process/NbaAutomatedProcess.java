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

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import com.axa.fsg.nba.foundation.AxaConstants;
import com.axa.fsg.nba.vo.AxaProducerVO;
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.CloneObject;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.ui.AxaStatusDefinitionLoader;
import com.csc.fs.accel.valueobject.Comment;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.RetrieveWorkItemsRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.accel.process.NbaAutoProcessAccelBP;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapter;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapterFactory;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceUtils;
import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.exception.NbaDataStoreModeNotFoundException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaTable;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;

import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaEmailVO;
import com.csc.fsg.nba.vo.NbaGeneralComment;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaRetrieveCommentsRequest;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaTransResponseVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaTransactionSearchResultVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.AutomatedProcess;
import com.csc.fsg.nba.vo.configuration.Category;
import com.csc.fsg.nba.vo.nbaschema.MessageTrailer;
import com.csc.fsg.nba.vo.statusDefinitions.Status;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CarrierAppointmentExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.HoldingExtension;
import com.csc.fsg.nba.vo.txlife.InforcePartyDetails;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.ReinsuranceOffer;
import com.csc.fsg.nba.vo.txlife.ReissueInforceDetails;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SuspendInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.ResultData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
import com.csc.fsg.nba.workflow.NbaWorkflowDistribution;
import com.csc.fs.Result;
import com.csc.fs.ServiceContext;
import com.csc.fs.UserSessionController;

/**
 * This class is an abstract class that provides the basis for executing the automated processes within nbAccelerator.
 * <p>
 * All automated process subclasses are initiated by NbaPoller through the <code>executeProcess</code> method.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th> </thead>
 * <tr>
 * <td>NBA001</td>
 * <td>Version 1</td>
 * <td>Initial Development</td>
 * </tr>
 * <tr>
 * <td>SPR1018</td>
 * <td>Version 2</td>
 * <td>JavaDoc, comments and minor source code changes.</td>
 * </tr>
 * <tr>
 * <td>SPR1050</td>
 * <td>Version 2</td>
 * <td>Add contract error status</td>
 * </tr>
 * <tr>
 * <td>NBA008</td>
 * <td>Version 2</td>
 * <td>Requirements Order and Receipting</td>
 * </tr>
 * <tr>
 * <td>NBA013</td>
 * <td>Version 2</td>
 * <td>Correspondence System</td>
 * </tr>
 * <tr>
 * <td>NBA020</td>
 * <td>Version 2</td>
 * <td>AWD Priority</td>
 * </tr>
 * <tr>
 * <td>NBA022</td>
 * <td>Version 2</td>
 * <td>Case Manager HTML Views</td>
 * </tr>
 * <tr>
 * <td>NBA027</td>
 * <td>Version 3</td>
 * <td>Performance Tuning</td>
 * </tr>
 * <tr>
 * <td>NBA050</td>
 * <td>Version 3</td>
 * <td>Nba Pending Database</td>
 * </tr>
 * <tr>
 * <td>NBA035</td>
 * <td>Version 3</td>
 * <td>Application Submit Transaction to nbA Pending Database</td>
 * </tr>
 * <tr>
 * <td>NBA044</td>
 * <td>Version 3</td>
 * <td>Architecure changes</td>
 * </tr>
 * <tr>
 * <td>NBA036</td>
 * <td>Version 3</td>
 * <td>nbA Underwriter Workbench Transactions to DB</td>
 * </tr>
 * <tr>
 * <td>NBA093</td>
 * <td>Version 3</td>
 * <td>Upgrade to ACORD 2.8</td>
 * </tr>
 * <tr>
 * <td>SPR1359</td>
 * <td>Version 3</td>
 * <td>Automated processes stop poller when unable to lock supplementary work items</td>
 * </tr>
 * </td>
 * </tr>
 * <tr>
 * <td>NBA064</td>
 * <td>Version 3</td>
 * <td>Contract Validation</td>
 * </tr>
 * <tr>
 * <td>NBA068</td>
 * <td>Version 3</td>
 * <td>Inforce Payment</td>
 * </tr>
 * <tr>
 * <td>NBA094</td>
 * <td>Version 3</td>
 * <td>Transaction Validation</td>
 * </tr>
 * <tr>
 * <td>SPR1851</td>
 * <td>Version 4</td>
 * <td>Locking Issues</td>
 * </tr>
 * <tr>
 * <td>NBA097</td>
 * <td>Version 4</td>
 * <td>Work Routing Reason Displayed</td>
 * </tr>
 * <tr>
 * <td>NBA100</td>
 * <td>Version 4</td>
 * <td>Create Contract Print Extracts for new Business Documents</td>
 * </tr>
 * <tr>
 * <td>NBA077</td>
 * <td>Version 4</td>
 * <td>Reissues and Complex Change</td>
 * </tr>
 * <tr>
 * <td>NBA095</td>
 * <td>Version 4</td>
 * <td>Queues Accept Any Work Type</td>
 * </tr>
 * <tr>
 * <td>ACN012</td>
 * <td>Version 4</td>
 * <td>Architecture Changes</td>
 * </tr>
 * <tr>
 * <td>ACN014</td>
 * <td>Version 4</td>
 * <td>121/1122 Migration</td>
 * </tr>
 * <tr>
 * <td>ACN009</td>
 * <td>Version 4</td>
 * <td>401/402 Migration</td>
 * </tr>
 * <tr>
 * <td>ACP008</td>
 * <td>Version 4</td>
 * <td>IU Preferred Processing</td>
 * </tr>
 * <tr>
 * <td>ACN008</td>
 * <td>Version 4</td>
 * <td>nbA Underwriting Workflow Changes</td>
 * </tr>
 * <tr>
 * <td>ACN024</td>
 * <td>Version 4</td>
 * <td>Automated Process Restructuring</td>
 * </tr>
 * <tr>
 * <td>NBA103</td>
 * <td>Version 4</td>
 * <td>Logging</td>
 * </tr>
 * <tr>
 * <td>SPR1777</td>
 * <td>Version 4</td>
 * <td>Update AWD LOB fields after comitting a contact to the database.</td>
 * </tr>
 * <tr>
 * <td>ACP010</td>
 * <td>Version 4</td>
 * <td>Added new Method for getting the loop flag</td>
 * </tr>
 * <tr>
 * <td>ACN026</td>
 * <td>Version 5</td>
 * <td>Receiving Different Requirement And Electronic Unsolicited Mail</td>
 * </tr>
 * <tr>
 * <td>SPR2386</td>
 * <td>Version 5</td>
 * <td>Logger definition</td>
 * </tr>
 * <tr>
 * <td>SPR1931</td>
 * <td>Version 5</td>
 * <td>Order of Trans Val and Issue Process is Wrong - Should Validate First</td>
 * </tr>
 * <tr>
 * <td>SPR2602</td>
 * <td>Version 5</td>
 * <td>APPORTAL error Stops when the Ripped 103 XML is not well formed.</td>
 * </tr>
 * <tr>
 * <td>SPR1753</td>
 * <td>Version 5</td>
 * <td>Automated Underwriting and Requirements Determination should detect severe errors for both AC and Non - AC.</td>
 * </tr>
 * <tr>
 * <td>SPR2565</td>
 * <td>Version 5</td>
 * <td>ORDERD process is not unsuspending the lower level requirements automatically when higher level requirement result is received.</td>
 * </tr>
 * <tr>
 * <td>SPR2399</td>
 * <td>Version 5</td>
 * <td>Requirements are not getting generated properly on Primary Insured.</td>
 * </tr>
 * <tr>
 * <td>NBA120</td>
 * <td>Version 5</td>
 * <td>Requirement Print Restrict Code</td>
 * </tr>
 * <tr>
 * <td>SPR2639</td>
 * <td>Version 5</td>
 * <td>Automated process status should be based business function</td>
 * </tr>
 * <tr>
 * <td>SPR2906</td>
 * <td>Version 6</td>
 * <td>For requirements ordered on other insured the APPSTREQ error stops with the reason BackEndAdapter Parse Error</td>
 * </tr>
 * <tr>
 * <td>SPR2662</td>
 * <td>Version 6</td>
 * <td>Poller stops when invalid contract data is present</td>
 * </tr>
 * <tr>
 * <td>SPR2544</td>
 * <td>Version 6</td>
 * <td>Duplicate Requirements get generated for REEVAL workitem.</td>
 * </tr>
 * <tr>
 * <td>SPR2199</td>
 * <td>Version 6</td>
 * <td>P&R Requirements Merging Logic Needs to Change to Not Discard some Requirements</td>
 * </tr>
 * <tr>
 * <td>NBA130</td>
 * <td>Version 6</td>
 * <td>Requirement/Reinsurance Changes</td>
 * </tr>
 * <tr>
 * <td>SPR3009</td>
 * <td>Version 6</td>
 * <td>When a reinsurance result is received, it should bring the case out of underwriter hold queue.</td>
 * </tr>
 * <tr>
 * <td>SPR2992</td>
 * <td>Version 6</td>
 * <td>General Code Cleanup</td>
 * </tr>
 * <tr>
 * <td>SPR2697</td>
 * <td>Version 6</td>
 * <td>Requirement Matching Criteria Needs to Be Expanded</td>
 * </tr>
 * <tr>
 * <td>NBA192</td>
 * <td>Version 7</td>
 * <td>Requirement Management Enhancement</td>
 * </tr>
 * <tr>
 * <td>NBA187</td>
 * <td>Version 7</td>
 * <td>nbA Trial Application</td>
 * </tr>
 * <tr>
 * <td>NBA212</td>
 * <td>Version 7</td>
 * <td>Content Services</td>
 * </tr>
 * <tr>
 * <td>NBA213</td>
 * <td>Version 7</td>
 * <td>Unified User Interface</td>
 * </tr>
 * <tr>
 * <td>NBA208</td>
 * <td>Version 7</td>
 * <td>Performance Tuning and Testing</td>
 * </tr>
 * <tr>
 * <td>SPR3362</td>
 * <td>Version 7</td>
 * <td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td>
 * </tr>
 * <tr>
 * <td>NBA208-1</td>
 * <td>Version 7</td>
 * <td>Performance Tuning and Testing - Deferred History Retrieval</td>
 * </tr>
 * <tr>
 * <td>NBA208-2</td>
 * <td>Version 7</td>
 * <td>Performance Tuning and Testing - Incremental change 2</td>
 * </tr>
 * <tr>
 * <td>NBA208-11</td>
 * <td>Version 7</td>
 * <td>Performance Tuning and Testing - Incremental change 11</td>
 * </tr>
 * <tr>
 * <td>NBA196</td>
 * <td>Version 7</td>
 * <td>JCA Adapter for Email</td>
 * </tr>
 * <tr>
 * <td>NBA208-32</td>
 * <td>Version 7</td>
 * <td>Workflow VO Convergence</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.79</td>
 * <td>AXA Life Phase 1</td>
 * <td>Shared AWD</td>
 * </tr>
 * <tr>
 * <td>SPR3611</td>
 * <td>Version 8</td>
 * <td>The Aggregate process is not setting LOBs on a Case correctly</td>
 * </tr>
 * <tr>
 * <td>AXAL.7.20</td>
 * <td>AXA Life Phase 1</td>
 * <td>Workflow</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.43</td>
 * <td>AXA Life Phase 1</td>
 * <td>Money Underwriting</td>
 * </tr>
 * <tr>
 * <td>NBA251</td>
 * <td>Version 8</td>
 * <td>nbA Case Manager and Companion Case Assignment</td>
 * </tr>
 * <tr>
 * <td>NBA231</td>
 * <td>Version 8</td>
 * <td>Replacement Processing</td>
 * </tr>
 * <tr>
 * <td>ALCP153</td>
 * <td>Version 8</td>
 * <td>FAST TEAM</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.20R</td>
 * <td>AXA Life Phase 1</td>
 * <td>Replacement Workflow</td>
 * <tr>
 * <td>APSL215</td>
 * <td>AXA Life Phase 1</td>
 * <td>CR49851 Underwriter Assignment Process</td>
 * <tr>
 * <tr>
 * <td>NBA300</td>
 * <td>AXA Life Phase 2</td>
 * <td>Term Conversion</td>
 * <tr>
 * <tr>
 * <td>P2AXAL018</td>
 * <td>AXA Life Phase 2</td>
 * <td>Omission Requirements</td>
 * </tr>
 * <tr>
 * <td>CR60956</td>
 * <td>AXA Life Phase 2</td>
 * <td>Life 70 Reissue</td>
 * </tr>
 * <tr>
 * <td>SR566149 and SR519592</td>
 * <td>Discretionary</td>
 * <td>Reissue and Delivery Requirement Follow Up</td>
 * </tr>
 * <tr>
 * <td>SR564247(APSL2525)</td>
 * <td>Discretionary</td>
 * <td>Predictive Analytics Full Implementation</td>
 * </tr>
 * <tr>
 * <td>APSL2808</td>
 * <td>Discretionary</td>
 * <td>Simplified Issue</td>
 * </tr>
 * <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
// NBA130 Added NbaTableConstants as an implemented class
public abstract class NbaAutomatedProcess implements NbaConstants, NbaVpmsConstants, NbaTableConstants {
	protected NbaAutoProcessAccelBP currentBP = null; //NBA213
	//NBA050 BEGIN
	public final static String EMAIL_CONTRACT = "Contract:";
	public final static String EMAIL_INSURED = "Insured:";
	public final static String EMAIL_REQUIREMENT = "Requirement:";
	public final static String CONTRACT_DELIMITER = "-";//ACN009
	public final static String LOB_NOT_AVAILABLE = "N/A"; //ACN014
	public final static String LOB_NOT_AVAILABLE_NUMBER = "0"; //NBA192
	public final static String APPORIGINTYPELOB = "AppOriginTypeLOB"; //NBA187
	public final java.lang.String DATA = "DATA";                        //NBLXA-2114
    public final java.lang.String FILENAME = "FILENAME";                //NBLXA-2114
	//NBA050 END
	protected NbaUserVO user = null;
	protected NbaDst parent = null;
	//NBA213 deleted code
	protected String nsaFacade = null;
	protected NbaDst work = null;
	private NbaLob workLobs = null; //SPR2992
	public NbaAutomatedProcessResult result = null;
	protected NbaLogger logger = null; //SPR2386
	protected NbaTXLife xML103Source = null;
	public NbaProcessStatusProvider statusProvider;
	protected NbaTXLife nbaTxLife = null; // NBA050
	protected int contractAccess = READ; //SPR1851
	public final static String NO_REQ_CTL_SRC = "Could not find Requirement Control Source"; //NBA050
	//NBA213 deleted code
	public boolean autoProxyRetrieveContract = true; // NBA035
	protected RequirementInfo requirementInfo = null; //NBA130
	protected NbaOLifEId nbaOLifEId = null; // NBA130
	protected NbaDst origWorkItem; //SPR3009
	public NbaSuspendVO suspendVO; //AXAL3.7.40G
	public final static String UW_DELIMITER = "|";//NBA251
	public final static String CM_DELIMITER = "=";//NBA251
	protected boolean APSAWDInd = false; //ALCP153
	protected String reason = null; //ALS5260
	protected boolean defaultUWInd = false; //APSL454
	protected static final String SORT_BY_CREATEDATE = "createDate"; //APSL221
	protected static final boolean doSCORProcessUsingJMS = NbaUtils.isConfigCallEnabled(NbaUtils.SCOR_PROCESS_USING_JMS);//APSL2808
	protected boolean A_PDR_CV_IND=false;//APSL4967
	protected boolean licensingworkExists=false; //NBLXA-1337
	protected boolean licensingworkInEndQueue=false;//NBLXA-1337
	protected NbaTransaction endedTransaction = null;//NBLXA-1337
	protected NbaSearchResultVO searchResultForLicWIVO =null;
	/**
	 * Default constructor.
	 */
	public NbaAutomatedProcess() {
		super();
	}

	/**
	 * Adds a new comment to the AWD system. The process that added the comment is recorded as the queue name of the AWD work item this process is
	 * operating on.
	 * 
	 * @param aComment
	 *            the comment to be added to the AWD system.
	 */
	public void addComment(String aComment) {
		addComment(aComment, getUser().getUserID());
	}

	/**
	 * Adds a new comment to the AWD system.
	 * 
	 * @param aComment
	 *            the comment to be added to the AWD system.
	 * @param aProcess
	 *            the process that added the comment.
	 */
	public void addComment(String aComment, String aProcess) {
		NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
		npec.setActionAdd();
		npec.setOriginator(getUser().getUserID());
		npec.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
		npec.setProcess(aProcess);
		npec.setText(aComment);
		work.addManualComment(npec.convertToManualComment());
		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("Comment added: " + aComment);
		} // NBA027
	}

	/**
	 * Adds new comments to the AWD system.
	 * 
	 * @param aList
	 *            multiple comments that are to be added to the AWD system.
	 */
	public void addComments(List aList) {
		for (int i = 0; i < aList.size(); i++) {
			addComment((String) aList.get(i));
		}
		;
	}

	/**
	 * Changes the status of the current Work Item, including the Route Reason.
	 * 
	 * @param newStatus
	 *            the new status for the Work Item
	 * @param newReaon
	 *            for the Work Item
	 */
	// ALS5260 New Method
	public void changeStatus(String newStatus, String newReason) {
		setRouteReason(getWork(), newStatus, newReason);
		if (!NbaUtils.isBlankOrNull(newReason)) {
			addComment(newReason); // ALS5337,APSL5128
		}
		getWork().setStatus(newStatus);
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Work Item status changed to " + newStatus);
		}
	}

	
	/**
	 * Changes the status of the current Work Item, including the Route Reason.
	 * 
	 * @param newStatus
	 *            the new status for the Work Item
	 */
	public void changeStatus(String newStatus) {
		setRouteReason(getWork(), newStatus); // NBA097	
		getWork().setStatus(newStatus);
		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("Work Item status changed to " + newStatus);
		} // NBA027
	}

	/**
	 * Changes the status of the given NbaDst Work Item. Includes a Route Reason update.
	 * 
	 * @param workItem
	 *            the NbaDst Work Item to update
	 * @param newStatus
	 *            the new status for the Work Item
	 */
	// NBA097 New Method
	public void changeStatus(NbaDst workItem, String newStatus) {
		setRouteReason(workItem, newStatus);
		workItem.setStatus(newStatus);
	}

	/**
	 * Changes the status of the given NbaTransaction. Includes a Route Reason update.
	 * 
	 * @param workItem
	 *            the NbaTransaction Work Item to update
	 * @param newStatus
	 *            the new status for the Work Item
	 */
	// NBA097 New Method
	public void changeStatus(NbaTransaction workItem, String newStatus) {
		setRouteReason(workItem, newStatus);
		workItem.setStatus(newStatus);
	}

	/**
	 * Create a TX Request value object that will be used to retrieve the contract.
	 * 
	 * @param access
	 *            the access intent to be used to retrieve the data, either READ or UPDATE
	 * @param businessProcess
	 *            the name of the business function or process requesting the contract
	 * @return a value object that is the request
	 */
	// NBA050 New Method
	//CR61627-PERF throw NbaBaseException
	public NbaTXRequestVO createRequestObject(int access, String businessProcess) throws NbaBaseException{
		//ACN026 code deleted
		return createRequestObject(getWork(), access, businessProcess); //ACN026
	}

	/**
	 * Create a TX Request value object that will be used to retrieve the contract.
	 * 
	 * @param nbaDst
	 *            the workitem object for that holding request is required
	 * @param access
	 *            the access intent to be used to retrieve the data, either READ or UPDATE
	 * @param businessProcess
	 *            the name of the business function or process requesting the contract
	 * @return a value object that is the request
	 */
	// ACN026 New Method
	//CR61627-PERF throw NbaBaseException
	public NbaTXRequestVO createRequestObject(NbaDst nbaDst, int access, String businessProcess) throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
		nbaTXRequest.setNbaLob(nbaDst.getNbaLob());
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

	//NBA130 Code Deleted

	/**
	 * Creates a request object that will be used to perform an update to the contract. Request is manufactured using input values, NbaLob object and
	 * User objectdefault values.
	 * 
	 * @param transMode
	 *            indicates the type of transaction (add, update, delete, etc.)
	 * @param transContentCode
	 *            indicates the contents of the transaction (insert, update, etc)
	 * @return com.csc.fsg.nba.vo.NbaTXLife a value object with the newly created transaction
	 */
	// NBA050 New method
	protected NbaTXRequestVO createRequirementRequestObject(long transType, long transMode, long transContentCode) throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(transType);
		nbaTXRequest.setTransMode(transMode);
		nbaTXRequest.setTranContentCode(transContentCode);
		nbaTXRequest.setNbaLob(getWork().getNbaLob());
		nbaTXRequest.setNbaUser(getUser());
		return nbaTXRequest;
	}

	//NBA130 Code Deleted

	/**
	 * Submit a holding inquiry request to the back end system.
	 * 
	 * @return a value object response from the back end system
	 */
	public NbaTXLife doHoldingInquiry() throws NbaBaseException {
		return doHoldingInquiry(READ, null); //NBA050
	}

	/**
	 * Submit a holding inquiry request to the back end system.
	 * 
	 * @param nbaDst
	 *            the workitem object for that holding inquiry is required
	 * @return a value object response from the back end system
	 */
	//ACN026 New Method
	public NbaTXLife doHoldingInquiry(NbaDst nbaDst) throws NbaBaseException {
		return doHoldingInquiry(nbaDst, READ, null); //NBA050
	}

	/**
	 * Submit a holding inquiry request to the back end system.
	 * 
	 * @return a value object response from the back end system
	 */
	// NBA050 New Method
	public NbaTXLife doContractUpdate(NbaTXLife nbaTxLife) throws NbaBaseException {
		try {
			nbaTxLife.setAccessIntent(getContractAccess()); //SPR1851
			nbaTxLife = NbaContractAccess.doContractUpdate(nbaTxLife, getWork(), getUser()); //NBA213
			//begin SPR1777
			if (getWork().isCase() && !nbaTxLife.isTransactionError()) { //SPR1931
				getWork().getNbaLob().updateLobFromNbaTxLife(nbaTxLife);
				getWork().setUpdate();
			}
			//end SPR1777
			//Begin NBA094	
		} catch (NbaBaseException nbe) {
			/*
			 * If the transaction validation fails the returned error messages will be added to the work item being processed, the status of the work
			 * item being processed will be changed to validation error (VALDERRD) sending the work item to the error queue.
			 */
			if (nbe instanceof NbaTransactionValidationException) {
				handleTransactionValidationErrors(nbe.getMessage());
			} else {
				throw nbe;
			}
			//End NBA094
			//NBA213 deleted code
		}
		return nbaTxLife;
	}

	/**
	 * Submit a holding inquiry request to the back end system.
	 * 
	 * @return a value object response from the back end system
	 */
	// NBA050 New Method
	public NbaTXLife doContractInsert(NbaTXLife application) throws NbaBaseException {
		try {
			application.setAccessIntent(UPDATE);
			application.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser())); //APSL2808
			application = NbaContractAccess.doContractUpdate(application, getWork(), getUser()); //NBA213
			//begin SPR1777
			if (getWork().isCase()) {
				getWork().getNbaLob().updateLobFromNbaTxLife(application);
				getWork().setUpdate();
			}
			//end SPR1777		
			//Begin NBA094	
		} catch (NbaBaseException nbe) {
			/*
			 * If the transaction validation fails the returned error messages will be added to the work item being processed, the status of the work
			 * item being processed will be changed to validation error (VALDERRD) sending the work item to the error queue.
			 */
			if (nbe instanceof NbaTransactionValidationException) {
				handleTransactionValidationErrors(nbe.getMessage());
			} else {
				throw nbe;
			}
			//End NBA094
			//NBA213 deleted code
		}
		return application;
	}

	/**
	 * Update the work item in AWD. Unlock any locked children. 
	 */
	public void doUpdateWorkItem() throws NbaBaseException {
		//begin NBA020 code change
		updateWork();
		//begin NBA208-32
		String origWorkItemId = null;
		String lockedUser = null;
		if (getOrigWorkItem() != null) {
			origWorkItemId = getOrigWorkItem().getID();
			lockedUser = setIdForUnlock(origWorkItemId, null);
		}
		//end NBA208-32
		unlockWork(); //SPR1851
		if (lockedUser != null) { //NBA208-32
			setIdForUnlock(origWorkItemId, lockedUser); //NBA208-32
		} //NBA208-32
		//end NBA020
	}

	/**
	 * Toggle the value of the locked user id in the original work item to prevent it from being unlocked. 
	 * 
	 * @param origId
	 * @param object
	 * @return
	 * @throws NbaNetServerDataNotFoundException
	 */
	//NBA208-32 New Method
	protected String setIdForUnlock(String origWorkItemId, String newLockUser) throws NbaNetServerDataNotFoundException {
		String lockUser = null;
		if (getWork().isCase()) {
			if (origWorkItemId.equals(getWork().getID())) {
				lockUser = getWork().getCase().getLockStatus();
				getWork().getCase().setLockStatus(newLockUser);
				getWork().getCase().setLockedByMe(newLockUser != null);
			} else {
				List transactions = getWork().getTransactions(); // get all the transactions
				Iterator transactionItr = transactions.iterator();
				WorkItem transaction;
				while (transactionItr.hasNext()) {
					transaction = (WorkItem) transactionItr.next();
					if (origWorkItemId.equals(transaction.getItemID())) {
						lockUser = transaction.getLockStatus();
						transaction.setLockStatus(newLockUser);
						transaction.setLockedByMe(newLockUser != null);
						break;
					}
				}
			}
		} else {
			if (origWorkItemId.equals(getWork().getID())) {
				lockUser = getWork().getTransaction().getLockStatus();
				getWork().getTransaction().setLockStatus(newLockUser);
				getWork().getTransaction().setLockedByMe(newLockUser != null);
			}
		}
		return lockUser;
	}

	/**
	 * Submit a holding inquiry request to the back end system.
	 * 
	 * @return a value object response from the back end system
	 */
	// NBA050 New Method
	public NbaTXLife doHoldingInquiry(int access, String businessProcess) throws NbaBaseException {
		return doHoldingInquiry(getWork(), access, businessProcess); //ACN026	
	}

	/**
	 * Submit a holding inquiry request to the back end system.
	 * 
	 * @param nbaDst
	 *            the workitem object for that holding inquiry is required
	 * @param access
	 *            the access rights
	 * @param businessProcess
	 *            the business process name
	 * @return a value object response from the back end system
	 */
	// ACN026 New Method
	public NbaTXLife doHoldingInquiry(NbaDst nbaDst, int access, String businessProcess) throws NbaBaseException {
		return NbaContractAccess.doContractInquiry(createRequestObject(nbaDst, access, businessProcess)); //NBA213
	}

	/**
	 * Submit a holding inquiry request to the back end system.
	 * 
	 * @return a value object response from the back end system
	 */
	// NBA050 New Method
	public NbaTXLife doContractUpdate() throws NbaBaseException {
		//SPR1851 code deleted
		return doContractUpdate(getNbaTxLife()); //SPR1851 NBA100	
	}

	/**
	 * 
	 */
	//NBA094 new method
	//ACN014 - changed the access specifier from private to protected
	protected void handleTransactionValidationErrors(String errorMessage) {
		/*
		 * If the transaction validation fails the returned error messages will be added to the work item being processed, the status of the work item
		 * being processed will be changed to validation error (VALDERRD) sending the work item to the error queue.
		 */
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Transaction Validation Error encountered for user -" + getUser());
		}
		addComment(errorMessage);
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Transaction validation failed", "VALDERRD"));

	}

	/**
	 * Updates the NbaTXLife value object with information required for the back end system.
	 * 
	 * @param aNbaTXLife
	 *            a <code>NbaTXLife</code> value object
	 */
	public void doXMLMarkup(NbaTXLife aNbaTXLife) {
		aNbaTXLife.doXMLMarkUp();
	}

	/**
	 * This abstract method must be implemented by each subclass in order to execute the automated process.
	 * 
	 * @param user
	 *            the user/process for whom the process is being executed
	 * @param work
	 *            a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 */
	public abstract NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException; //SPR1359

	/**
	 * Answers the AWD error status for a process by requesting that status from the statusProvider object.
	 * 
	 * @return a String containing the AWD error status
	 */
	public java.lang.String getAwdErrorStatus() {
		return getStatusProvider().getAwdErrorStatus();
	}

	/**
	 * Answers the FAIL status to use when an AWD error occurs
	 * 
	 * @return String containing the AWD Fail status
	 */
	public java.lang.String getAWDFailStatus() {
		return A_STATUS_AWD_ERROR;
	}

	/**
	 * Answers the fail status for a process by requesting that status from the statusProvider object.
	 * 
	 * @return a string containing the fail status
	 */
	public java.lang.String getFailStatus() {
		return getStatusProvider().getFailStatus();
	}

	/**
	 * Answers the Host error status for a process by requesting that status from the statusProvider object.
	 * 
	 * @return a string containing the Host error status
	 */
	public java.lang.String getHostErrorStatus() {
		return getStatusProvider().getHostErrorStatus();
	}

	/**
	 * Answers the status to use when a Host process fails
	 * 
	 * @return String containing the HOST fail status
	 */
	public java.lang.String getHostFailStatus() {
		return A_STATUS_HOST_ERROR;
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {//NBA103
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass()); //NBA103
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory."); //NBA103
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * Returns the NbaLob object that is required for Lookup.
	 * 
	 * @return NbaLob containing the initialized members
	 */
	public NbaLob getNbaLobForLookup(ArrayList aOinkVocabularyName) {
		NbaOinkDataAccess nbaOinkDataAcess = new NbaOinkDataAccess();
		return nbaOinkDataAcess.cloneNbaLobValues(getWork().getNbaLob(), aOinkVocabularyName);
	}

	/**
	 * Check if NbaLob object passed contains a non null values for all the lobs mentioned in lobList. If any of the LOBs mentioned in the list is
	 * null or empty string, return false indicating that this workflow search criteria cannot be used, otherwise return true.
	 * 
	 * @param workLob
	 *            the NbaLob object
	 * @param lobList
	 *            the list of LOB names
	 * @return true indicating that this search criteria can be used for workflow search else return false
	 */
	//NBA192 New Method
	public boolean checkLobPresence(NbaLob workLob, List lobList) {
		int size = lobList.size();
		String lobName = null;
		String lobValue = null;
		NbaOinkDataAccess aNbaOinkDataAccess = new NbaOinkDataAccess(workLob);
		NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
		for (int i = 0; i < size; i++) {
			lobName = (String) lobList.get(i);
			//begin NBA187
			if (lobName.startsWith("A_")) {
				lobName = lobName.substring(2);
			}
			aNbaOinkRequest.setVariable(lobName);
			//end NBA187
			try {
				lobValue = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
				if (lobValue == null || lobValue.trim().length() == 0 || NbaAutomatedProcess.LOB_NOT_AVAILABLE.equals(lobValue)
						|| (!APPORIGINTYPELOB.equals(lobName) && LOB_NOT_AVAILABLE_NUMBER.equals(lobValue))
						|| NbaConstants.OLI_TC_NULL.equals(lobValue)) { //NBA187
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Search bypassed. No value for " + aNbaOinkRequest.getVariable() + " on work item");
					}
					return false;
				}
			} catch (NbaBaseException e) {
				getLogger().logInfo("Could not resolve:" + lobName);
				return false;
			}
		}
		return true;
	}

	//NBA213 deleted code
	/**
	 * Answers the other status for a process by requesting that status from the statusProvider object.
	 * 
	 * @return a string containing the other status
	 */
	// SPR1050 Added new method for contract error status
	// NBA004 changed method name as status in VPMS model has changed.
	public java.lang.String getOtherStatus() {
		return getStatusProvider().getOtherStatus();
	}

	/**
	 * Answers the pass status for a process by requesting that status from the statusProvider object.
	 * 
	 * @return a string containing the pass status
	 */
	public java.lang.String getPassStatus() {
		if (getStatusProvider() == null) {
			return null;
		}
		return getStatusProvider().getPassStatus();
	}

	/**
	 * Answer the NbaAutomatedProcessResult.
	 * 
	 * @return an NbaAutomatedProcessResult containing the results of the process
	 */
	public NbaAutomatedProcessResult getResult() {
		return result;
	}

	/**
	 * Answers the SQL error status for a process by requesting that status from the statusProvider object.
	 * 
	 * @return a string containing the SQL error status
	 */
	public java.lang.String getSqlErrorStatus() {
		return getStatusProvider().getSqlErrorStatus();
	}

	/**
	 * Answers the statusProvider for the object
	 * 
	 * @return an NbaProcessStatusProvider that contains the different statuses for a process
	 */
	public NbaProcessStatusProvider getStatusProvider() {
		return statusProvider;
	}

	/**
	 * Answers the user on whose behalf the process is executing
	 * 
	 * @return the user for the process
	 */
	public com.csc.fsg.nba.vo.NbaUserVO getUser() {
		return user;
	}

	/**
	 * Answers the VPMS error status for a process by requesting that status from the statusProvider object.
	 * 
	 * @return a string containing the VPMS error status
	 */
	public java.lang.String getVpmsErrorStatus() {
		return getStatusProvider().getVpmsErrorStatus();
	}

	//NBA103 - removed unused method

	/**
	 * Answers the work item for the process
	 * 
	 * @return the work item
	 */
	public com.csc.fsg.nba.vo.NbaDst getWork() {
		return work;
	}

	/**
	 * Answers the value object for the XML 103 message from the AWD system.
	 * 
	 * @return an NbaTXLife value object containing the application data
	 */
	public NbaTXLife getXML103() throws NbaBaseException {
		//SPR2602 code deleted
		if (xML103Source == null) {
			xML103Source = getWork().getXML103Source();
		}
		return xML103Source;
		//SPR2602 code deleted
	}

	/**
	 * Handle the NbaTXLife response from the Back End Adaptor.
	 * 
	 * @param aTXLifeResponse
	 *            the NbaTXLife response
	 */
	public void handleHostResponse(NbaTXLife aTXLifeResponse) {
		//NBA077 code deleted
		handleHostResponse(aTXLifeResponse, true); //NBA077
	}

	/**
	 * Handle the NbaTXLife response from the Back End Adaptor.
	 * 
	 * @param aTXLifeResponse
	 *            the NbaTXLife response
	 * @param addAddComments
	 *            if true host error messages will be added as AWD comments
	 */
	//NBA077 New Method
	public void handleHostResponse(NbaTXLife aTXLifeResponse, boolean addAddComments) {
		if (aTXLifeResponse != null && aTXLifeResponse.isTransactionError()) {//NBA094
			UserAuthResponseAndTXLifeResponseAndTXLifeNotify allResponses = aTXLifeResponse.getTXLife()
					.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
			int count = allResponses.getTXLifeResponseCount();
			TransResult transResult = allResponses.getTXLifeResponseAt(count - 1).getTransResult();
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Back End Processing failed", getHostFailStatus()));
			ArrayList errors = new ArrayList();
			for (int i = 0; i < transResult.getResultInfoCount(); i++) {
				errors.add(transResult.getResultInfoAt(i).getResultInfoDesc());
			}
			if (addAddComments) {
				addComments(errors);
			}
		}
	}

	/**
	 * Increase the work item priority based on the action flag and priority value from VPMS. The supported action flags are: '+': increase existing
	 * priority increment by '-': decrease existing priority increment by '=': set priority increment value to '*': ignore or not applicable
	 * 
	 * @throws NbaBaseException
	 */
	// NBA020 New Method 
	public void increasePriority() throws NbaBaseException {
		if (work.isCase()) {
			//Begin AXAL3.7.20
			String processId = NbaUtils.getBusinessProcessId(getUser());
			if (PROC_AUTO_UNDERWRITING.equalsIgnoreCase(processId) || PROC_NBACREATE.equalsIgnoreCase(processId)) {
				work.increasePriority(statusProvider.getCaseAction(), getProcesStatusProvider(work.getNbaLob(), getNbaTxLife()).getCasePriority());
				// End AXAL3.7.20
			} else {
				work.increasePriority(statusProvider.getCaseAction(), statusProvider.getCasePriority());
			}

		} else if (work.isTransaction()) {
			work.increasePriority(statusProvider.getWIAction(), statusProvider.getWIPriority());
		}
	}

	/**
	 * Handle the NbaTXLife response from the Back End Adaptor.
	 * 
	 * @param aTXLifeResponse
	 *            the NbaTXLife response
	 */
	public void evaluateResponse() {
		handleHostResponse(getNbaTxLife()); //NBA093 NBA100
	}

	/**
	 * Provides additional initialization support by setting the case and user objects to the passed in parameters and by creating a reference to the
	 * NbaNetServerAccessor EJB.
	 * 
	 * @param newUser
	 *            the AWD User for the process
	 * @param newWork
	 *            the NbaDst value object to be processed
	 * @return <code>true</code> indicates the statuses were successfully retrieved while <code>false</code> indicates failure.
	 */
	public boolean initialize(NbaUserVO newUser, NbaDst newWork) throws NbaBaseException {
		setWork(newWork);
		//NBA208-2 code deleted
		setUser(newUser);
		try {
			//NBA213 deleted code
			//NBA095 Begin
			if (!isProxyClass()) {
				if (!isCorrectQueue()) {
					handleIncorrectQueue();
					return false;
				}
			}
			//NBA095 End
			//NBA050 BEGIN
			//NBA213 deleted code
			AutomatedProcess aNbaConfigAutomaticProcess = getAutomaticProcessUser(); //ACN012
			//begin SPR1851
			boolean retriveContract = false;
			if (aNbaConfigAutomaticProcess != null) {
				//save original work item for automated processes only.
				saveOrigWorkItem(); //NBA208-2
				if (aNbaConfigAutomaticProcess.getRetrieveContract().equalsIgnoreCase(CONTRACT_RETRIEVE_LOCK)) {
					retriveContract = true;
					setContractAccess(UPDATE);
					NbaContractLock.requestLock(newWork, newUser);
					// Will throw NbaDatabaseLockedException(Error) if could not get lock on database. Please
					// Do not catch it. NbaAutoProcessBean class will handle it.			
				} else if (aNbaConfigAutomaticProcess.getRetrieveContract().equalsIgnoreCase(CONTRACT_RETRIEVE_VIEW)) {
					retriveContract = true;
				}
			}
			//end SPR1851
			//APSL677 Begin
			String newUserID = newUser.getUserID();
			if(retriveContract == true				
					&& ("A2AGGCNT".equalsIgnoreCase(newUserID) || "A2AGGCN2".equalsIgnoreCase(newUserID) || "A2PYDRFT".equalsIgnoreCase(newUserID)
							|| "A2AGGCN3".equalsIgnoreCase(newUserID) || "A2PRDHLD".equalsIgnoreCase(newUserID))
				&& NbaUtils.isBlankOrNull(newWork.getNbaLob().getPolicyNumber())){ //SR564247(APSL2525), APSL2808
				//if the policy number is not set on workitem then reset the retriveContract to false
				retriveContract = false;
			}
			//APSL677 End
			if ((aNbaConfigAutomaticProcess != null && retriveContract)
					|| (aNbaConfigAutomaticProcess == null && autoProxyRetrieveContract == true)) { // NBA050 and NBA035 SPR1851
				if (!retrieveHoldingInquiry()) {
					return false;
				}
			}
			// BEGIN NBA130
			if (retriveContract == true && newWork.isTransaction()) {
				if (A_WT_REQUIREMENT.equals(newWork.getWorkType()) || A_WT_TEMP_REQUIREMENT.equals(newWork.getWorkType())) {
					requirementInfo = nbaTxLife.getRequirementInfo(newWork.getNbaLob().getReqUniqueID());
					if (requirementInfo == null) {
						//APSL3874 Code deleted
						throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_REQINFO); //APSL3874
					}
				}
			}
			// END NBA130
			//NBA050 END
			initializeStatusFields();
			if (aNbaConfigAutomaticProcess == null) {
				return false;
			}
			
			if (getPassStatus() == null || getPassStatus().length() == 0) {
				statusProcessFailed();
				return false;
			//Begin ALS3928
			} else if (PROCESS_BYPASS.equals(getPassStatus())) {
				changeStatus(getFailStatus());
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getFailStatus()));
				doUpdateWorkItem();
				return false;
			}//end ALS3928
			
			return true;
		} catch (NbaBaseException e) {
			throw e;
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.VPMS_GENERIC, e);
		}
	}

	/**
	 * Provides additional initialization support by setting the case and user objects to the passed in parameters and by creating a reference to the
	 * NbaNetServerAccessor EJB.
	 * 
	 * @param newUser
	 *            the AWD User for the process
	 * @param newWork
	 *            the NbaDst value object to be processed
	 * @return <code>true</code> indicates the statuses were successfully retrieved while <code>false</code> indicates failure.
	 */
	//NBA115 new method
	public boolean initializeWithoutStatus(NbaUserVO newUser, NbaDst newWork) throws NbaBaseException {
		setWork(newWork);
		saveOrigWorkItem(); //SPR3009
		setUser(newUser);
		try {
			//NBA213 deleted code
			if (!isProxyClass()) {
				if (!isCorrectQueue()) {
					handleIncorrectQueue();
					return false;
				}
			}
			//NBA213 deleted code
			AutomatedProcess aNbaConfigAutomaticProcess = getAutomaticProcessUser();
			boolean retriveContract = false;
			if (aNbaConfigAutomaticProcess != null) {
				if (aNbaConfigAutomaticProcess.getRetrieveContract().equalsIgnoreCase(CONTRACT_RETRIEVE_LOCK)) {
					retriveContract = true;
					setContractAccess(UPDATE);
					NbaContractLock.requestLock(newWork, newUser);
					// Will throw NbaDatabaseLockedException(Error) if could not get lock on database. Please
					// Do not catch it. NbaAutoProcessBean class will handle it.			
				} else if (aNbaConfigAutomaticProcess.getRetrieveContract().equalsIgnoreCase(CONTRACT_RETRIEVE_VIEW)) {
					retriveContract = true;
				}
			}
			if ((aNbaConfigAutomaticProcess != null && retriveContract)
					|| (aNbaConfigAutomaticProcess == null && autoProxyRetrieveContract == true)) { // NBA050 and NBA035 SPR1851
				if (!retrieveHoldingInquiry()) {
					return false;
				}
			}
			return true;
		} catch (NbaBaseException e) {
			throw e;
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.VPMS_GENERIC, e);
		}
	}

	/**
	 * Provides additional initialization support by setting the pass/fail/error status values from VP/MS.
	 * 
	 * @see com.csc.fsg.nba.business.process.NbaProcessStatusProvider
	 */
	protected void initializeStatusFields() throws NbaBaseException {
		//ACN024 BEGIN
		String businessProcess = NbaUtils.getBusinessProcessId(getUser()); //SPR2697
		 //ALS3928 commented Cache logic
//		statusProvider = NbaProcessStatusCache.getInstance().getStatusProvider(businessProcess); //SPR2697
//		if (statusProvider != null) {
//			return;
//		}
		//ACN024 END
		// begin AXAL3.7.20
		if (NbaConstants.PROC_NBAFORMAL.equals(businessProcess)) {
			getCaseAWDSources();
		}
		// end AXAL3.7.20
		statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), getNbaTxLife(), getRequirementInfo()); //NBA192
		if (PROCESS_BYPASS.equals(statusProvider.getPassStatus())) {
			statusProvider.setPassStatus(getFailStatus());//ALII616
	}
	}

	/**
	 * Retrieves the contract from the datasource and evaluates the reponse for any errors. If the work item is a case, it automatically updates the
	 * LOB fields from the retrieved holding inquiry.
	 * 
	 * @return <code>true</code> indicates the contract was successfully retrieved while <code>false</code> indicates failure.
	 */
	public boolean retrieveHoldingInquiry() throws NbaBaseException {
		try {
			setNbaTxLife(doHoldingInquiry()); //NBA100
			evaluateResponse();
			if (getResult() == null) {
				if (getWork().isCase()) {
					updateLobFromNbaTxLife(getNbaTxLife()); //NBA100
				}
				return true;
			} else {
				contractRetrieveFailed(); //SPR1851
				return false;
			}
		} catch (NbaDataAccessException ndae) {
			addComment("Unable to retrieve contract from backend"); //SPR1851
			contractRetrieveFailed();
			return false;
			//begin SPR1851
		} catch (NbaDataStoreModeNotFoundException dsmnfe) {
			addComment(dsmnfe.getMessage());
			contractRetrieveFailed();
			return false;
		}
		//end SPR1851
	}

	/**
	 * Locates the automatic process user by parsing the NbaConfiguration collection of AutomatedProcesses for the user id of this process.
	 * 
	 * @return the NbaConfigAutomaticProcess for this automated process
	 */
	// NBA050 NEW METHOD
	// ACN012 changed signature
	public AutomatedProcess getAutomaticProcessUser() throws NbaBaseException {
		try {
			Iterator iter = NbaConfiguration.getInstance().getAllAutomatedProcesses().iterator(); //ACN012
			while (iter.hasNext()) {
				AutomatedProcess aProcess = (AutomatedProcess) iter.next(); //ACN012
				if (aProcess.getUser().equals(getUser().getUserID())) {
					return aProcess;
				}
			}
			return null;
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.VPMS_GENERIC, e);
		}
	}

	//NBA213 deleted code
	/**
	 * Initializes the result data member
	 * 
	 * @param newResult
	 *            the result of an automated process
	 */
	public void setResult(NbaAutomatedProcessResult newResult) {
		result = newResult;
	}

	/**
	 * Initializes the statusProvider data member
	 * 
	 * @param newStatusProvider
	 *            a new status provider
	 */
	public void setStatusProvider(NbaProcessStatusProvider newStatusProvider) {
		statusProvider = newStatusProvider;
	}

	/**
	 * Initizlizes the user data member
	 * 
	 * @param newUser
	 *            a new user/process
	 */
	public void setUser(com.csc.fsg.nba.vo.NbaUserVO newUser) {
		user = newUser;
	}

	/**
	 * Initializes the work data member
	 * 
	 * @param newWork
	 *            a new NbaDst case
	 */
	public void setWork(com.csc.fsg.nba.vo.NbaDst newWork) {
		work = newWork;
		xML103Source = null; //NBA187
	}

	/**
	 * When statuses cannot be retrieved from the VPMS model for any reason, this method adds a comment to the case indicating that failure, changes
	 * the status of the case to indicate an error and updates the work item. It also creates an <code>NbaAutomatedProcessResult</code> object to
	 * return to the <code>NbaPoller</code> so that the error can be recorded.
	 * 
	 * @return an NbaAutomatedProcessResult object containing error information
	 * @throws an
	 *             NbaBaseException is thrown if a Remote Exception occurs when trying to write changes to the AWD system.
	 */
	public NbaAutomatedProcessResult statusProcessFailed() throws NbaBaseException {
		//NBA020 code deleted
		//begin SPR1851
		if (getResult() != null) {
			return getResult();
		}
		//end SPR1851
		addComment("Unable to determine statuses for process");
		result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED,
				NbaConfiguration.getInstance().getNetserverDefaultStatus("VPMSERROR"), // ACN012
				NbaConfiguration.getInstance().getNetserverDefaultStatus("VPMSERROR")); //ACN012
		statusProvider.setVpmsErrorStatus(NbaConfiguration.getInstance().getNetserverDefaultStatus("VPMSERROR")); //ACN012
		changeStatus(getVpmsErrorStatus());
		doUpdateWorkItem();
		return getResult();
		//NBA020 code deleted
	}

	/**
	 * When a contract cannot be retrieved from the database for any reason, this method adds a comment to the case indicating that failure, changes
	 * the status of the case to indicate an error and updates the work item. It also creates an <code>NbaAutomatedProcessResult</code> object to
	 * return to the <code>NbaPoller</code> so that the error can be recorded.
	 * 
	 * @throws an
	 *             NbaBaseException is thrown if a Remote Exception occurs when trying to write changes to the AWD system.
	 */
	// NBA050 NEW METHOD
	public void contractRetrieveFailed() throws NbaBaseException {
		//SPR1851 code deleted
		//begin SPR1851
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, NbaConstants.A_STATUS_HOST_ERROR,
					NbaConstants.A_STATUS_HOST_ERROR));
		}
		if (getWork().isLocked(getUser().getUserID())) {
			changeStatus(getResult().getStatus());
			//begin NBA213		 		 
			getWork().setNbaUserVO(getUser());
			AccelResult accelResult = new AccelResult();
			accelResult.merge(currentBP.callBusinessService("NbaUpdateWorkBP", getWork()));
			NewBusinessAccelBP.processResult(accelResult);
			accelResult.merge(currentBP.callBusinessService("NbaUnlockWorkBP", getWork()));
			NewBusinessAccelBP.processResult(accelResult);
			//end NBA213
		}
		//end SPR1851
	}

	/**
	 * Update the LOB fields from a TXLife such as a holding inquiry or application (type 103).
	 * 
	 * @param nbaTxLife
	 *            an nbA wrapper to the Acord TXLife object
	 */
	public void updateLobFromNbaTxLife(NbaTXLife nbaTxLife) {
		getWork().updateLobFromNbaTxLife(nbaTxLife);
	}

	/**
	 * Update the work item in AWD. 
	 */
	//NBA020 New Method
	public void updateWork() throws NbaBaseException {
		increasePriority();
		//begin NBA213
		getWork().setNbaUserVO(getUser());
		AccelResult accelResult = new AccelResult();
		accelResult.merge(currentBP.callBusinessService("NbaUpdateWorkBP", getWork()));
		NewBusinessAccelBP.processResult(accelResult);
		setWork((NbaDst) accelResult.getFirst());
		//end NBA213
	}

	/**
	 * Create and initialize an <code>NbaVpmsAutoUnderwritingData</code> object to find matching work items.
	 * 
	 * @param entryPoint
	 *            the VP/MS model's entry point
	 * @return NbaVpmsAutoUnderwritingData the VP/MS results
	 * @throws NbaBaseException
	 */
	// ACN012 NEW METHOD
	protected VpmsComputeResult getDataFromVpms(String model, String request, NbaOinkDataAccess oinkData, HashMap skipMap, NbaOinkRequest oinkRequest)
			throws NbaBaseException {
		NbaVpmsAdaptor vpmsAdaptor = null; //SPR3362
		try {
			if (oinkData == null) {
				oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
				if (nbaTxLife != null) {
					oinkData.setContractSource(nbaTxLife);
				}
			}

			if (oinkRequest == null) {
				oinkRequest = new NbaOinkRequest();
			}
			//begin NBA192
			if ((oinkRequest.getRequirementIdFilter() == null || oinkRequest.getRequirementIdFilter().trim().length() == 0)
					&& getRequirementInfo() != null) {
				oinkRequest.setRequirementIdFilter(getRequirementInfo().getId());
			}
			//end NBA192
			if (skipMap == null) {
				skipMap = new HashMap();
			}
			if (getNbaTxLife()!= null && AxaConstants.PROC_PRIOR_INS_REFRESH.equals(getNbaTxLife().getBusinessProcess())) { //APSL1169
				skipMap.put(A_PROCESS_ID, AxaConstants.PROC_PRIOR_INS_REFRESH);
			} else {
				skipMap.put(A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR1753
			}
			skipMap.put(A_REQUEST, request);
			vpmsAdaptor = new NbaVpmsAdaptor(oinkData, model); //SPR3362
			vpmsAdaptor.setVpmsEntryPoint(request);
			vpmsAdaptor.setANbaOinkRequest(oinkRequest);
			vpmsAdaptor.setSkipAttributesMap(skipMap);
			return vpmsAdaptor.getResults();
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("Autounderwriting problem", re);
			//begin SPR3362
		} finally {
			if (vpmsAdaptor != null) {
				try {
					vpmsAdaptor.remove();
				} catch (RemoteException e) {
					getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
			//end SPR3362
		}

	}

	/**
	 * Creates the relation object..
	 * 
	 * @return  - the created relation object
	 * @param relCount
	 *            - the count of relation on the olife.
	 * @param orgObjId
	 *            - the originating object id
	 * @param relObjId
	 *            - the relation object id
	 * @param orgType
	 *            - the originating object type
	 * @param relType
	 *            - the relation object type
	 * @param roleCode
	 *            - the relation role code
	 */
	//NBA050 NEW METHOD moved from NbaProcRequirementCancel, 
	//NbaProcProviderFollowUp and NbaProcOrderRequirement
	//ACN014 Changed method signature
	protected Relation createRelation(NbaTXLife txlife, int relCount, String orgObjId, String relObjId, long orgType, long relType, long roleCode)
			throws NbaBaseException {
		//ACN014 Begin
		NbaOLifEId nbaOLifEId = null;
		if (txlife == null) {
			nbaOLifEId = new NbaOLifEId(nbaTxLife); //NBA050
		} else {
			nbaOLifEId = new NbaOLifEId(txlife);
		}
		//ACN014 End
		Relation rel = new Relation();
		nbaOLifEId.setId(rel); //NBA050
		rel.setOriginatingObjectID(orgObjId);
		rel.setRelatedObjectID(relObjId);
		rel.setOriginatingObjectType(orgType);
		rel.setRelatedObjectType(relType);
		rel.setRelationRoleCode(roleCode);
		return rel;
	}

	/**
	 * Answer relation object that matched relation type,person code and person sequence number for a XMLife.
	 * 
	 * @param life
	 *            - the XMLife object
	 * @param relType
	 *            - the relation object type
	 * @param personCode
	 *            - the person relation role code
	 * @param personSeq
	 *            - the person sequence number
	 * @return the relation object
	 */
	//NBA050 NEW METHOD moved from NbaProcRequirementCancel, 
	//NbaProcProviderFollowUp and NbaProcOrderRequirement
	protected Relation getRelation(NbaTXLife life, long relType, long personCode, long personSeq) throws NbaBaseException {
		List list = life.getOLifE().getRelation();
		Relation rel = null;
		for (int i = 0; i < list.size(); i++) {
			rel = (Relation) list.get(i);
			if (rel.getRelatedObjectType() == relType && rel.getRelationRoleCode() == personCode) {
				if (rel.getRelatedRefID() == null || Long.parseLong(rel.getRelatedRefID()) == personSeq) {
					return rel;
				}
			}
		}
		return null;
	}

	/**
	 * This method creates the XMLife message for 9001 transaction code.
	 * 
	 * @return the generated XMLife message
	 * @param NbaDst
	 *            - the work item
	 * @throws NbaBaseException
	 *             - a fatal exception is thrown if the contract cannot be retrieved. An error exception (default) is thrown if the contract is
	 *             missing required information.
	 */
	//NBA050 NEW METHOD ADDED TO NbaAutomatedProcess
	//Consolidated from NbaProcOrderRequirments and NbaProcProviderFollowUp.
	protected NbaTXLife create9001Request(NbaDst reqItem) throws NbaBaseException {
		List errorList = new ArrayList();
		NbaLob lob = reqItem.getNbaLob();
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_EMAIL);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser())); //NBA050 SPR2639
		nbaTXRequest.setNbaLob(lob);

		NbaOLifEId nbaOLifEId = new NbaOLifEId(getNbaTxLife()); //NBA050	NBA100

		//create txlife with default request fields
		NbaTXLife txLife = new NbaTXLife(nbaTXRequest);
		//NBA050 CODE DELETED
		//NBA050 BEGIN
		if (getNbaTxLife() == null) { //NBA100
			//begin SPR2662
			try {
				if (!retrieveHoldingInquiry()) {
					throw new NbaBaseException("Unable to retrieve contract from datasource", NbaExceptionType.FATAL);
				}
			} catch (NbaBaseException e) {
				e.forceFatalExceptionType();
				throw e;
				//end SPR2662
			}
		}
		//NBA050 END

		//get olife 
		OLifE olife = txLife.getOLifE(); //NBA044
		Holding holding = txLife.getPrimaryHolding(); //NBA044
		Policy policy = holding.getPolicy();
		holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		//RequirementInfo
		RequirementInfo reqInfo = new RequirementInfo();
		nbaOLifEId.setId(reqInfo); //NBA050				
		reqInfo.setAppliesToPartyID("Party_1");
		reqInfo.setReqCode(lob.getReqType());
		reqInfo.setRequirementDetails(NbaTransOliCode.lookupText(NbaOliConstants.OLI_LU_REQCODE, lob.getReqType()));
		policy.addRequirementInfo(reqInfo);

		//get insured party information
		Relation partyRel = getRelation(getNbaTxLife(), NbaOliConstants.OLI_PARTY, lob.getReqPersonCode(), lob.getReqPersonSeq()); //NBA100
		//NBA050
		NbaParty holdingParty = getNbaTxLife().getParty(partyRel.getRelatedObjectID()); //NBA050 NBA100
		if (holdingParty == null) {
			throw new NbaBaseException("Could not get party information from holding inquiry");
		}

		Party party = new Party();
		nbaOLifEId.setId(party); //NBA050		
		party.setPartyTypeCode(holdingParty.getParty().getPartyTypeCode());
		olife.addParty(party);
		//person
		PersonOrOrganization perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		Person person = new Person();
		perOrg.setPerson(person);
		Person holdingPerson = holdingParty.getParty().getPersonOrOrganization().getPerson();
		person.setLastName(holdingPerson.getLastName());
		person.setFirstName(holdingPerson.getFirstName());
		person.setMiddleName(holdingPerson.getMiddleName());

		//Attachment
		//Email Subject
		Attachment attach = new Attachment();
		nbaOLifEId.setId(attach); //NBA050				

		StringBuffer subject = new StringBuffer();
		StringBuffer body = new StringBuffer();
		if (NbaConstants.PROC_REQUIREMENT_CANCEL.equals(NbaUtils.getBusinessProcessId(getUser()))) { //SPR2639
			subject.append("Requirement Cancelled: ");
			subject.append(EMAIL_CONTRACT);
			subject.append(":");
			subject.append(lob.getPolicyNumber());
			subject.append(", ");
			subject.append(NbaTransOliCode.lookupText(NbaOliConstants.OLI_LU_REQCODE, getWork().getNbaLob().getReqType()));
			subject.append(" - ");
			subject.append(getWork().getNbaLob().getReqVendor());
			body.append("This requirement has been cancelled");
		} else if (NbaConstants.PROC_PROVIDER_FOLLOWUP.equals(NbaUtils.getBusinessProcessId(getUser()))) { //SPR2639
			subject.append("Requirement Follow-Up Needed: ");
			//begin SPR1245
			subject.append(EMAIL_CONTRACT);
			subject.append(":");
			subject.append(lob.getPolicyNumber());
			subject.append(", ");
			//end SPR1245
			subject.append(NbaTransOliCode.lookupText(NbaOliConstants.OLI_LU_REQCODE, getWork().getNbaLob().getReqType()));
			subject.append(" - ");
			subject.append(getWork().getNbaLob().getReqVendor());
			body.append("The requirement noted above has not yet been received. Please follow up with the Vendor");
		}
		attach.setAttachmentKey(subject.toString());
		attach.setAttachmentType(NbaOliConstants.OLI_ATTACH_EMAIL);
		AttachmentData attachData = new AttachmentData();

		attachData.setPCDATA(body.toString());
		attach.setAttachmentData(attachData);
		holding.addAttachment(attach);

		//get writing agent (from holding inquiry)
		holdingParty = getNbaTxLife().getWritingAgent(); //NBA050 NBA100
		if (holdingParty == null) {
			throw new NbaBaseException("Could not get producer information from holding inquiry");
		}

		party = new Party();
		nbaOLifEId.setId(party); //NBA050				
		party.setPartyTypeCode(holdingParty.getParty().getPartyTypeCode());
		olife.addParty(party);
		//person

		String mailId = null;
		perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		person = new Person();
		perOrg.setPerson(person);
		holdingPerson = holdingParty.getParty().getPersonOrOrganization().getPerson();
		person.setLastName(holdingPerson.getLastName());
		person.setFirstName(holdingPerson.getFirstName());
		person.setMiddleName(holdingPerson.getMiddleName());
		if (holdingParty.getParty().getEMailAddress() != null && holdingParty.getParty().getEMailAddress().size() > 0
				&& holdingParty.getParty().getEMailAddressAt(0).getAddrLine() != null) {
			mailId = holdingParty.getParty().getEMailAddressAt(0).getAddrLine();
			party.addEMailAddress(holdingParty.getParty().getEMailAddressAt(0));
		} else {
			errorList.add("Agent Email Address");
		}

		Producer prdc = holdingParty.getParty().getProducer();
		if (prdc != null) {
			for (int i = 0; i < prdc.getCarrierAppointmentCount(); i++) {
				prdc.getCarrierAppointmentAt(i).setPartyID(party.getId());
			}
			party.setProducer(holdingParty.getParty().getProducer());
		}

		//Organization
		party = new Party();
		nbaOLifEId.setId(party); //NBA050				
		party.setPartyTypeCode(NbaOliConstants.OLIX_PARTYTYPE_CORPORATION);
		olife.addParty(party);
		party.setFullName("");

		EMailAddress mail = new EMailAddress();
		nbaOLifEId.setId(mail); //NBA050				
		mail.setEMailType(NbaOliConstants.OLI_EMAIL_BUSINESS);
		//mail.setAddrLine(NbaConfiguration.getInstance().getEmailConfig().getFrom());
		mail.setAddrLine(mailId);
		party.addEMailAddress(mail);

		//Relations
		olife.addRelation(createRelation(txLife, //ACN014
				olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), olife.getPartyAt(0).getId(),
				NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, reqItem.getNbaLob().getReqPersonCode())); //NBA044

		olife.addRelation(createRelation(txLife, //ACN014
				olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), olife.getPartyAt(1).getId(),
				NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_PRIMAGENT)); //NBA044

		olife.addRelation(createRelation(txLife, //ACN014
				olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), olife.getPartyAt(2).getId(),
				NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_REQUESTEDBY)); //NBA044

		olife.addRelation(createRelation(txLife, //ACN014
				olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getPolicy().getRequirementInfoAt(0).getId(),
				olife.getPartyAt(0).getId(), NbaOliConstants.OLI_REQUIREMENTINFO, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_FORMFOR)); // NBA044

		if (errorList.size() > 0) {
			throw new NbaDataException(errorList);
		}
		return txLife;
	}

	/**
	 * Returns the busfunc.
	 * 
	 * @return String
	 */
	// NBA064 New Method
	public String getBusfunc() throws NbaBaseException {
		String processName = this.getClass().getName();
		processName = processName.substring(processName.lastIndexOf('.') + 1);
		try {
			Iterator iter = NbaConfiguration.getInstance().getAllAutomatedProcesses().iterator(); //ACN012
			//ACN012 CODE DELETED
			while (iter.hasNext()) {
				AutomatedProcess aProcess = (AutomatedProcess) iter.next(); //ACN012
				if (aProcess.getName().equals(processName)) {
					return aProcess.getBusfunc();
				}
			}
			return null;
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BUSINESS_FUNCTION, e);
		}
	}

	/**
	 * Answers the alternate status for a process by requesting that status from the statusProvider object.
	 * 
	 * @return a string containing the AlternateStatus status
	 */
	//	  new method NBA068
	public java.lang.String getAlternateStatus() {
		if (getStatusProvider() == null) {
			return null;
		}
		return getStatusProvider().getAlternateStatus();
	}

	// ACP002 New Method
	public String getInstallationType() {
		NbaConfiguration nbaConfiguration = NbaConfiguration.getInstance();
		return String.valueOf(nbaConfiguration.isAcNba()); //ACP008
		//ACP008 code deleted
	}

	//SPR2499 code deleted
	/**
	 * Unlock the work item in AWD. It will aslo remove the lock on datbase if locked.
	 */
	//SPR1851 New Method
	public void unlockWork() throws NbaBaseException {
		unlockWork(getWork()); //NBA192
	}

	/**
	 * Unlocks the work in the workflow system. It will also remove the lock on database.
	 * 
	 * @param lockedWork
	 *            the workitem to be unlocked
	 * @throws NbaBaseException
	 */
	//NBA192 New Method
	public void unlockWork(NbaDst lockedWork) throws NbaBaseException {
		//begin NBA213	 
		lockedWork.setNbaUserVO(getUser());
		AccelResult accelResult = new AccelResult();
		accelResult.merge(currentBP.callBusinessService("NbaUnlockWorkBP", lockedWork));
		NewBusinessAccelBP.processResult(accelResult);
		//end NBA213
	}

	/**
	 * Returns contract access intent
	 * 
	 * @return
	 */
	//SPR1851 New Method
	public int getContractAccess() {
		return contractAccess;
	}

	/**
	 * Sets contract access intent
	 * 
	 * @param i
	 *            the access intent
	 */
	//	SPR1851 New Method
	public void setContractAccess(int i) {
		contractAccess = i;
	}

	/**
	 * Sets the route reason for the NbaTransaction based on the new status.
	 * 
	 * @param trans
	 *            the NbaTransaction
	 * @param newStatus
	 *            the new status for the Transaction
	 */
	// NBA097 New Method
	public void setRouteReason(NbaTransaction trans, String newStatus) {
		String workType = null;
		String routeReason = null;
		try {
			workType = trans.getTransaction().getWorkType();
			NbaTableAccessor tableAccessor = new NbaTableAccessor();
			routeReason = tableAccessor.getStatusTranslationString(workType, newStatus);
		} catch (Exception e) {
			// just use the default route reason...
		}
		if (routeReason == null) {
			routeReason = newStatus; // default to the AWD status
		}
		trans.getNbaLob().setRouteReason(routeReason);
	}

	/**
	 * Sets the route reason for the NbaDst object based on the new status.
	 * 
	 * @param workItem
	 *            the NbaDst Work Item to update
	 * @param newStatus
	 *            the new status for the Work Item
	 */
	// NBA097 New Method
	public void setRouteReason(NbaDst workItem, String newStatus) {
		NbaUtils.setRouteReason(workItem, newStatus);
	}
	
	/**
	 * Sets the route reason for the NbaDst object based on the new status and newReason.
	 * 
	 * @param workItem
	 *            the NbaDst Work Item to update
	 * @param newStatus
	 *            the new status for the Work Item
	 * @param newReason
	 *            for the Wor Item
	 */
	// ALS5260 New Method
	public void setRouteReason(NbaDst workItem, String newStatus, String newReason) {
		NbaUtils.setRouteReason(workItem, newStatus, newReason);
	}


	/**
	 * Retrieve the nbaTxLife object.
	 * 
	 * @return NbaTxLife
	 */
	//NBA100 New Method
	public NbaTXLife getNbaTxLife() {
		return nbaTxLife;
	}

	/**
	 * Set the nbaTxLife object.
	 * 
	 * @param life
	 */
	//NBA100 New Method
	public void setNbaTxLife(NbaTXLife life) {
		nbaTxLife = life;
	}

	/**
	 * Returns the correct queue from the AWD W62U999S table -- in other words, given the Business Area, Work Type, and Status, this function will
	 * return the Queue that this piece of work should reside in.
	 * 
	 * @param businessArea
	 *            the AWD business area
	 * @param workType
	 *            the AWD work type
	 * @param status
	 *            the AWD status
	 * @return <code>java.lang.String</code> contains the queue
	 * @exception NbaBaseException
	 *                if an error occurs while reading the database.
	 */
	//	NBA095 new method
	public String getCorrectQueue(String businessArea, String workType, String status) throws NbaBaseException {

		try {
			NbaTableAccessor nta = new NbaTableAccessor();
			return nta.getNextQueue(businessArea, workType, status);
		} catch (Exception e) {
			throw new NbaBaseException("Error while accessing the AWD database W62U999S table", e);
		}

	}

	/**
	 * Returns the current queue that this process should be drawing work from based on the business function name.
	 * 
	 * @return String the AWD Queue name
	 * @exception NbaBaseException
	 *                if an error occurs during getBusfunc.
	 */
	// NBA095 New Method
	public String getQueue() throws NbaBaseException {
		String bizFunc = getBusfunc();
		if (bizFunc == null || bizFunc.length() < 2) {
			return "UNKNOWN";
		} else {
			return "N2" + bizFunc.substring(2); //AXAL3.7.79
		}
	}

	/**
	 * Returns the AWD worktype for the current work object.
	 * 
	 * @return String the AWD Work Type.
	 * @throws NbaBaseException
	 *             if an error occurs.
	 */
	// NBA095 NEW METHOD
	public String getWorkType() throws NbaBaseException {

		String workType = null;
		try {
			if (getWork().isCase()) {
				workType = getWork().getCase().getWorkType();
			} else {
				workType = getWork().getTransaction().getWorkType();
			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BUSINESS_FUNCTION, e);
		}

		return workType;

	}

	/**
	 * Returns true if "this" is a proxy class.
	 * 
	 * @return boolean
	 */
	// NBA095 New Method
	public boolean isProxyClass() {
		String processName = this.getClass().getName();
		processName = processName.substring(processName.lastIndexOf('.') + 1);
		return (processName.equalsIgnoreCase("NbaAutoProcessProxy") || processName.equalsIgnoreCase("NbaAutoProcessProviderProxy")); //NBA095
	}

	/**
	 * When the work object (retrieved by performing the AWD Get Work) is in the wrong queue for some reason, this method adds a comment to the case
	 * indicating that failure, changes the status of the case to indicate an error, and updates/unlocks the work item. It also creates an
	 * <code>NbaAutomatedProcessResult</code> object and uses setResult() so that the error result can be recorded.
	 * 
	 * @throws an
	 *             NbaBaseException is thrown if a Remote Exception occurs when trying to write changes to the AWD system.
	 */
	// NBA095 NEW METHOD
	public void handleIncorrectQueue() throws NbaBaseException {
		//APSL3874 code deleted
		throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_INVALIDQUEUE,""); //APSL3874
	}

	/**
	 * Returns true if the work item is in the correct queue for this poller; otherwise returns false.
	 * 
	 * @return boolean indicating if the current work item is in the correct queue.
	 * @exception NbaBaseException
	 *                if an error occurs during processing.
	 */
	// NBA095 NEW METHOD
	public boolean isCorrectQueue() throws NbaBaseException {

		String businessArea = getWork().getBusinessArea();
		String status = getWork().getStatus();
		String workType = getWorkType();

		String correctQueue = getCorrectQueue(businessArea, workType, status);
		return ((correctQueue != null) && (correctQueue.equalsIgnoreCase(getQueue())));

	}

	/**
	 * Returns true if the work item is in the correct queue for this poller; otherwise returns false. This version should only be used prior to a
	 * full initialization.
	 * 
	 * @param newUser
	 *            the AWD User for the process
	 * @param newWork
	 *            the NbaDst value object to be processed
	 * @return true if the work object is in an appropriate queue.
	 * @throws NbaBaseException
	 */
	// NBA095 NEW METHOD
	public boolean isCorrectQueue(NbaUserVO newUser, NbaDst newWork) throws NbaBaseException {
		setWork(newWork);
		setUser(newUser);
		try {
			if (!isCorrectQueue()) {
				//NBA213 deleted code
				handleIncorrectQueue();
				return false;
			}
			return true;
		} catch (NbaBaseException e) {
			throw e;
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.VPMS_GENERIC, e);
		}
	}

	/**
	 * Use a VPMS model to determine if a Contact Print Extract Transaction is needed. If true, create one and add it to the Case.
	 * 
	 * @param nbaUserVO
	 * @param nbaDst
	 * @param busFunc
	 * @param nbaTXLife
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	public void addPrintExtractTransaction(NbaUserVO nbaUserVO, NbaDst thisnbaDst, String busFunc, NbaTXLife nbaTXLife)
			throws NbaVpmsException, NbaBaseException {
		VpmsModelResult vpmsModelResult = getPrintExtractCodes(thisnbaDst, busFunc, nbaTXLife);
		int count = vpmsModelResult.getContractPrintExtractCodesCount();
		if (count > 0) { //Print Extract needed
			//Create Transaction and add it to the nbaDst for the Case
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(nbaUserVO, thisnbaDst, nbaTXLife, new HashMap()); //NBA208-11
			NbaTransaction nbaTransaction = thisnbaDst.addTransaction(provider.getWorkType(), provider.getInitialStatus());
			nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
			//NBA208-32
			nbaTransaction.getTransaction().setLock("Y");
			//Copy  LOBS from the case to transaction
			NbaLob tempTransNbaLob = nbaTransaction.getNbaLob();
			NbaLob workNbaLob = work.getNbaLob();
			tempTransNbaLob.setPolicyNumber(workNbaLob.getPolicyNumber());
			tempTransNbaLob.setSsnTin(workNbaLob.getSsnTin());
			tempTransNbaLob.setTaxIdType(workNbaLob.getTaxIdType());
			tempTransNbaLob.setAgentID(workNbaLob.getAgentID()); //NBLXA-2585
			tempTransNbaLob.setWritingAgency(workNbaLob.getWritingAgency()); //NBLXA-2585
			tempTransNbaLob.setLastName(workNbaLob.getLastName());
			tempTransNbaLob.setFirstName(workNbaLob.getFirstName());
			tempTransNbaLob.setMiddleInitial(workNbaLob.getMiddleInitial());
			tempTransNbaLob.setCompany(workNbaLob.getCompany());
			tempTransNbaLob.setReview(workNbaLob.getReview());
			tempTransNbaLob.setIssueOthrApplied(workNbaLob.getIssueOthrApplied());
			tempTransNbaLob.setAppDate(workNbaLob.getAppDate());
			tempTransNbaLob.setAppState(workNbaLob.getAppState());
			tempTransNbaLob.setDistChannel(String.valueOf(workNbaLob.getDistChannel()));  //AXAL3.7.20 //ALS1667
			tempTransNbaLob.setSpecialCase(workNbaLob.getSpecialCase()); //AXAL3.7.20
			tempTransNbaLob.setReplacementIndicator(workNbaLob.getReplacementIndicator()); //AXAL3.7.20R
			tempTransNbaLob.setExchangeReplace(workNbaLob.getExchangeReplace());//ALS5718
			tempTransNbaLob.setReceiptDate(workNbaLob.getReceiptDate());//QC8125
			tempTransNbaLob.setPaidChgCMQueue(workNbaLob.getPaidChgCMQueue());//ALII1480
			tempTransNbaLob.setCaseManagerQueue(workNbaLob.getCaseManagerQueue());//ALII1726, ALII1623
			tempTransNbaLob.setUndwrtQueue(workNbaLob.getUndwrtQueue());//ALII1726, ALII1623
			//Set the transaction EXTC Lob field
			StringBuffer extComponents = new StringBuffer();
			for (int i = 0; i < count; i++) {
				extComponents.append(vpmsModelResult.getContractPrintExtractCodesAt(i).getCode());
				if (i < count - 1) {
					extComponents.append(",");
				}
			}
			nbaTransaction.getNbaLob().setExtractComp(extComponents.toString());
			nbaTransaction.setUpdate();
		}
	}

	/**
	 * Use a VPMS model to determine the print extract types.
	 * 
	 * @param nbaDst
	 * @param busFunc
	 * @param nbaTXLife
	 * @return
	 * @throws NbaBaseException
	 * @throws NbaVpmsException
	 */
	// NBA100 New Method
	protected VpmsModelResult getPrintExtractCodes(NbaDst nbaDst, String busFunc, NbaTXLife nbaTXLife) throws NbaBaseException, NbaVpmsException {
		VpmsModelResult vpmsModelResult = new VpmsModelResult();
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaDst.getNbaLob());
		NbaVpmsAdaptor nbaVpmsAdaptor = null; //SPR3362
		try { //SPR3362
			nbaVpmsAdaptor = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.CONTRACT_PRINT_EXTRACT_TYPES); //SPR3362
			nbaVpmsAdaptor.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_EXTRACT_TYPES);
			String mode = NbaServerUtility.getDataStore(nbaDst.getNbaLob(), null); //NBA077
			nbaVpmsAdaptor.getSkipAttributesMap().put("A_MODE", mode);
			nbaVpmsAdaptor.getSkipAttributesMap().put("A_BUSFUNC", busFunc);
			//NBA208 code deleted
			try {
				VpmsComputeResult vpmsComputeResult = nbaVpmsAdaptor.getResults();
				if (vpmsComputeResult.getReturnCode() == 0) {
					NbaVpmsModelResult nbaResult = new NbaVpmsModelResult(vpmsComputeResult.getResult());
					vpmsModelResult = nbaResult.getVpmsModelResult();
				} else {
					throw createNbaVpmsException(vpmsComputeResult, NbaVpmsAdaptor.CONTRACT_PRINT_EXTRACT_TYPES, NbaVpmsAdaptor.EP_GET_EXTRACT_TYPES);
				}
			} catch (RemoteException e) {
				throw new NbaVpmsException(e);
			} //SPR3362
		} finally {
			if (nbaVpmsAdaptor != null) {
				try {
					nbaVpmsAdaptor.remove();
				} catch (RemoteException e) {
					getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //SPR3362
				}
			}
		}
		return vpmsModelResult;
	}

	/**
	 * Create a NbaVpmsException to describe a VPMS problem
	 * 
	 * @param vpmsComputeResult
	 * @return NbaVpmsException
	 */
	// NBA100 New Method
	protected NbaVpmsException createNbaVpmsException(VpmsComputeResult vpmsComputeResult, String adaptor, String entrypoint) {
		StringBuffer buff = new StringBuffer();
		buff.append("VPMS error in ");
		buff.append(adaptor);
		buff.append(".");
		buff.append(entrypoint);
		buff.append(": ");
		if (vpmsComputeResult.getRefField() != null && vpmsComputeResult.getRefField().length() > 0) {
			buff.append("missing field: ");
			buff.append(vpmsComputeResult.getRefField());
		} else {
			if (vpmsComputeResult.getMessage() != null && vpmsComputeResult.getMessage().length() > 0) {
				buff.append(vpmsComputeResult.getMessage());
			}
		}
		return new NbaVpmsException(buff.toString());
	}

	/**
	 * Generate a compound contract number to be sent to the providers in an XML transaction. This compound contract number is a combination of
	 * company code, back end system and policy number. This will allow us to recreate this information when the response comes back from the
	 * provider. The genereated compound contract number has the format COMPANYID-BACKENDSYSTEM-POLICYNUMBER (i.e., 00-CLIF-D000000061).
	 * 
	 * @return a string containing the newly generated contract number
	 */
	//ACN009 new method
	public String generateCompoundContractNumber() {
		StringBuffer contractNumber = new StringBuffer();
		contractNumber.append(getWork().getNbaLob().getCompany());
		contractNumber.append(CONTRACT_DELIMITER);
		contractNumber.append(getWork().getNbaLob().getBackendSystem());
		contractNumber.append(CONTRACT_DELIMITER);
		contractNumber.append(getWork().getNbaLob().getPolicyNumber());
		return contractNumber.toString();
	}

	/**
	 * Updates the work item with data from a compound contract number returned by a provider in an XML requirement result. The compound contract
	 * number has the format COMPANYID-BACKENDSYSTEM-POLICYNUMBER (i.e., 00-CLIF-D000000061).
	 * 
	 * @param a
	 *            String containing the compound contract number
	 */
	//ACN009 new method
	public void updateWorkItemFromCompoundContractNumber(String contractNumber) {
		NbaStringTokenizer tokens = new NbaStringTokenizer(contractNumber, CONTRACT_DELIMITER);
		// for each token in the list, we create add a resultData
		if (tokens.hasMoreTokens()) {
			getWork().getNbaLob().setCompany(tokens.nextToken());
			getWork().getNbaLob().setBackendSystem(tokens.nextToken());
			getWork().getNbaLob().setPolicyNumber(tokens.nextToken());
		}
	}

	/**
	 * This method calls the NbaRulesControlModel to determine the VPMS model to call for the process.
	 * 
	 * @return String containing the VPMS model to execute
	 * @throws NbaBaseException
	 */
	//ACN008 New Method
	public String getVpmsModelToExecute() throws NbaBaseException {
		HashMap skipMap = new HashMap();
		skipMap.put(NbaVpmsAdaptor.A_INSTALLATION, getInstallationType());
		VpmsComputeResult aResult = getDataFromVpms(NbaVpmsAdaptor.NBARULESCONTROLMODEL, NbaVpmsAdaptor.EP_GET_MODEL, null, skipMap, null);
		NbaVpmsModelResult modresult = new NbaVpmsModelResult(aResult.getResult());
		return modresult.getModelName();
	}

	/**
	 * This method calls the NbaRulesControlModel to determine the looping flag for the process.
	 * 
	 * @return String containing looping flag for the Automated Underwriting
	 * @throws NbaBaseException
	 */
	//ACP010 New Method
	public String getloopingFlagForAutomatedProcess() throws NbaBaseException {
		HashMap skipMap = new HashMap();
		skipMap.put(NbaVpmsAdaptor.A_INSTALLATION, getInstallationType());
		VpmsComputeResult aResult = getDataFromVpms(NbaVpmsAdaptor.NBARULESCONTROLMODEL, NbaVpmsAdaptor.EP_LOOP, null, skipMap, null);
		NbaVpmsModelResult modresult = new NbaVpmsModelResult(aResult.getResult());
		return modresult.getVpmsModelResult().getResultAt(0);
	}

	/**
	 * Calls unsuspendWork method on netserveraccessor to unsuspend a workitem.
	 * 
	 * @param suspendVO
	 *            the NbaSuspendVO object containing workitem id
	 * @throws NbaBaseException
	 */
	//SPR2565 New Method
	public void unsuspendWork(NbaSuspendVO suspendVO) throws NbaBaseException {
		//begin NBA213		  
		suspendVO.setNbaUserVO(getUser());
		AccelResult accelResult = new AccelResult();
		accelResult.merge(currentBP.callBusinessService("NbaUnsuspendWorkBP", suspendVO));
		NewBusinessAccelBP.processResult(accelResult);
		//end NBA213
	}

	/**
	 * Calls update method on netserveraccessor to update a workitem to AWD.
	 * 
	 * @param nbaDst
	 *            the AWD workitem
	 * @return the updated workitem object
	 * @throws NbaBaseException
	 *             if any problem during AWD update.
	 */
	//SPR2399 New Method
	public NbaDst update(NbaDst nbaDst) throws NbaBaseException {
		//begin NBA213		 
		nbaDst.setNbaUserVO(getUser());
		AccelResult accelResult = (AccelResult) currentBP.callBusinessService("NbaUpdateWorkBP", nbaDst);
		NewBusinessAccelBP.processResult(accelResult);
		nbaDst = (NbaDst) accelResult.getFirst();
		return nbaDst;
		//end NBA213
	}

	/**
	 * Suspends the workitem in AWD.
	 * 
	 * @param suspendVO
	 *            the NbaSuspendVO object containing workitem id
	 * @throws NbaBaseException
	 *             if a RemoteException occurs
	 */
	//NBA120 New Method
	public void suspendWork(NbaSuspendVO suspendVO) throws NbaBaseException {
		//begin NBA213
		suspendVO.setNbaUserVO(getUser());
		AccelResult accelResult = (AccelResult) currentBP.callBusinessService("NbaSuspendWorkBP", suspendVO);
		NewBusinessAccelBP.processResult(accelResult);
		//end NBA213
	}

	/**
	 * It performs: - Add manual comment. - Update workitem in AWD. - Suspend workitem with activate date. - Unlock the workitem in AWD.
	 * 
	 * @param comment
	 *            Comment to be added to the work item
	 * @param activationDate
	 *            activationDate for the workItem to be suspended
	 * @throws NbaBaseException
	 */
	//NBA120 New Method
	protected void suspendWorkItem(String comment, Date activationDate) throws NbaBaseException {
		// prepare the suspend VP for workitem to be suspended 
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		suspendVO.setTransactionID(getWork().getID());
		suspendVO.setSystemName(getWork().getSystemName());      //APSL5055-NBA331
		suspendVO.setActivationDate(activationDate);
		suspendVO.setKeepLock(true);// APSL4400 NBA-932

		addComment(comment, getUser().getUserID()); //Add comment
		update(getWork()); //update to AWD
		suspendWork(suspendVO); //suspend work
		//unlockWork(); //APSL4400 NBA-932
	}

	//NBA212 code deleted 
	/**
	 * Updates the RequirementInfoExtension with necessary information
	 * 
	 * @param aTransaction
	 *            the NbaTransaction used to update the RequirementInfoExtension
	 * @param aReqInfo
	 *            the RequirementInfo object containing the extension to be updated
	 */
	// NBA130 New Method
	protected void updateRequirementInfo(NbaTransaction aTransaction, RequirementInfo aReqInfo){
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
		if (null == reqInfoExt) {
			OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
			reqInfoExt = oliExt.getRequirementInfoExtension();
			reqInfoExt.setActionAdd();
			aReqInfo.addOLifEExtension(oliExt);
		} else {
			if (null == reqInfoExt.getActionIndicator()) {
				reqInfoExt.setActionUpdate();
			}
		}
		reqInfoExt.setWorkitemID(aTransaction.getID());
		//Start APSL2808
		if (nbaTxLife.isSIApplication() && aReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_SCRIPTCHK) {
			reqInfoExt.setReviewedInd(true);
			reqInfoExt.setReviewID(user.getUserID());
			reqInfoExt.setReviewDate(new Date());
			reqInfoExt.setReviewCode(String.valueOf(NbaConstants.REVIEW_NOT_REQUIRED));
		}
		//End APSL2808
		return;
	}

	/**
	 * Answers the NbaOLifEId object if initialized. If not initialized, it creates one from the existing NbaTXLife object.
	 * 
	 * @return the NbaOLifEId object
	 */
	// NBA130 New Method
	public NbaOLifEId getNbaOLifEId() {
		if (null == nbaOLifEId) {
			nbaOLifEId = new NbaOLifEId(nbaTxLife);
			setNbaOLifEId(nbaOLifEId);
		}
		return nbaOLifEId;
	}

	/**
	 * Initializes the NbaOLifEId object
	 * 
	 * @param nbaOLifEId
	 */
	// NBA130 New Method
	public void setNbaOLifEId(NbaOLifEId nbaOLifEId) {
		this.nbaOLifEId = nbaOLifEId;
	}

	/**
	 * Update RequirementInfo object to add to NbaTXLife.
	 * 
	 * @param action
	 *            indicates the type of transaction (post, receipt, etc.)
	 * @return a RequirementInfo object containing the necessary information to update the requirement
	 */
	// NBA130 New Method
	public void updateRequirementInfoObject(long action) throws NbaBaseException {
		//RequirementInfoExtension
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(getRequirementInfo());
		if (null == reqInfoExt) {
			OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
			reqInfoExt = oliExt.getRequirementInfoExtension();
			reqInfoExt.setActionAdd();
			getRequirementInfo().addOLifEExtension(oliExt);
		} else {
			reqInfoExt.setActionUpdate();
		}

		TrackingInfo trackingInfo = new TrackingInfo();
		trackingInfo.setTrackingServiceProvider(work.getNbaLob().getReqVendor());
		trackingInfo.setActionAdd();
		reqInfoExt.setTrackingInfo(trackingInfo);
		requirementInfo.setActionUpdate();
		Iterator sources = getWork().getNbaSources().iterator();
		List newAttachments; //NBA212
		while (sources.hasNext()) {
			NbaSource aSource = (NbaSource) sources.next();
			if (aSource.getSource().getSourceType().equals(NbaConstants.A_ST_PROVIDER_SUPPLEMENT)) {
				long attachmentType = requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MIBCHECK ? NbaOliConstants.OLI_ATTACH_MIB_SERVRESP
						: NbaOliConstants.OLI_ATTACH_REQUIRERESULTS;
				newAttachments = createAttachments(aSource, attachmentType, true); //NBA212
				requirementInfo.getAttachment().addAll(newAttachments); //NBA212
			}
		}
	}

	/**
	 * @return Returns the requirementInfo.
	 */
	public RequirementInfo getRequirementInfo() {
		return requirementInfo;
	}

	/**
	 * @param requirementInfo
	 *            The requirementInfo to set.
	 */
	public void setRequirementInfo(RequirementInfo requirementInfo) {
		this.requirementInfo = requirementInfo;
	}

	/**
	 * Return the original Work Item.
	 * 
	 * @return origWorkItem.
	 */
	// SPR3009 New Method    
	protected NbaDst getOrigWorkItem() {
		return origWorkItem;
	}

	/**
	 * Return the original Work Item.
	 * 
	 * @param origWorkItem
	 *            The origWorkItem to set.
	 */
	// SPR3009 New Method    
	protected void setOrigWorkItem(NbaDst origWorkItem) {
		this.origWorkItem = origWorkItem;
	}

	/**
	 * Save a copy of the original work item
	 * 
	 * @throws NbaBaseException
	 * @throws Exception
	 */
	// SPR3009 New Method    
	protected void saveOrigWorkItem() throws NbaBaseException {
		try {
			//NBA208-32
			NbaDst dst = new NbaDst();
			CloneObject.clone(getWork(), dst);
			setOrigWorkItem(dst);
		} catch (Exception e) {
			throw new NbaBaseException(e);
		}
	}

	/**
	 * Returns an <code>NbaLob</code> instance containing the LOB values for the current work.
	 * 
	 * @return
	 */
	// SPR2992 New Method
	public NbaLob getWorkLobs() {
		if (workLobs == null) {
			if (getWork() != null) {
				workLobs = getWork().getNbaLob();
			}
		}
		return workLobs;
	}

	/**
	 * Retrieve the parent case and all its children WARNING: Call this method for transaction work only
	 * 
	 * @return the parent case with all children
	 */
	//NBA192 new Method
	//AXAL3.7.43 Method Signature changed
	//ALS2584 Refractor method body
	protected NbaDst retrieveParentWork(boolean lock) throws NbaBaseException {
		return retrieveParentWork(getWork(), lock, true);
		//end NBA213
	}

	/**
	 * Retrieve the parent case and all its children WARNING: Call this method for transaction work only
	 * 
	 * @return the parent case with all children
	 */
	//ALS2584 New Method 
	//SR566149 & SR519592 - Moved the implementation to a new method and added parameter to lock siblings in the new method
	protected NbaDst retrieveParentWork(NbaDst nbaDst, boolean lock, boolean retrieveSiblings) throws NbaBaseException {
		return retrieveParentWork(nbaDst,lock,retrieveSiblings,retrieveSiblings);
	}
	//SR566149 & SR519592
	protected NbaDst retrieveParentWork(NbaDst nbaDst, boolean lock, boolean retrieveSiblings, boolean lockSiblings) throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(nbaDst.getID(), false);
		retOpt.setSystemName(nbaDst.getSystemName());  //APSL5055-NBA331
		retOpt.requestCaseAsParent();
		if (retrieveSiblings) {
			retOpt.requestTransactionAsSibling();
		}
		if (lock) {//AXAL3.7.43
			retOpt.setLockWorkItem();
			retOpt.setLockParentCase();
			if (lockSiblings) {
				retOpt.setLockSiblingTransaction();
			}
		}//AXAL3.7.43
		//begin NBA213
		retOpt.setNbaUserVO(getUser());
		AccelResult accelResult = (AccelResult) currentBP.callBusinessService("NbaRetrieveWorkBP", retOpt);
		NewBusinessAccelBP.processResult(accelResult);
		return (NbaDst) accelResult.getFirst();
		//end NBA213
	}

	/**
	 * Retrieve the parent case and all its children without setting a lock WARNING: Call this method for transaction work only
	 * 
	 * @return the parent case with all children
	 */
	//AXAL3.7.43 new Method
	protected NbaDst retrieveParentWork() throws NbaBaseException {
		return retrieveParentWork(true);
	}

	/**
	 * Copy LOBs from formal work to original informal transactions
	 * 
	 * @throws NbaBaseException
	 * @throws NbaVpmsException
	 */
	//NBA187 New Method
	protected void setFormalLobs(NbaTransaction copiedWork) {
		NbaLob copiedLobs = copiedWork.getNbaLob();
//		Begin ALS2220
		NbaLob workLobs = getWorkLobs();
		copiedLobs.setBackendSystem(workLobs.getBackendSystem());
		copiedLobs.setCompany(workLobs.getCompany());
		copiedLobs.setOperatingMode(workLobs.getOperatingMode());
		copiedLobs.setPolicyNumber(workLobs.getPolicyNumber());
		copiedLobs.setPlan(workLobs.getPlan());
		copiedLobs.setProductTypSubtyp(workLobs.getProductTypSubtyp());
		copiedLobs.setLastName(workLobs.getLastName());
		copiedLobs.setFirstName(workLobs.getFirstName());
		copiedLobs.setMiddleInitial(workLobs.getMiddleInitial());
		copiedLobs.setAppState(workLobs.getAppState());
		copiedLobs.setSsnTin(workLobs.getSsnTin());
		// End ALS2220
	}

	/**
	 * Copy LOBs from formal work to copied informal sources
	 * 
	 * @throws NbaBaseException
	 * @throws NbaVpmsException
	 */
	//NBA187 New Method
	protected void setFormalLobs(NbaSource copiedSource) {
		NbaLob copiedLobs = copiedSource.getNbaLob();
//		Begin ALS2220
		NbaLob workLobs = getWorkLobs();
		copiedLobs.setBackendSystem(workLobs.getBackendSystem());
		copiedLobs.setCompany(workLobs.getCompany());
		copiedLobs.setOperatingMode(workLobs.getOperatingMode());
		//copiedLobs.setPolicyNumber(workLobs.getPolicyNumber()); No need to set it as the policy number of work is null
		copiedLobs.setPlan(workLobs.getPlan());
		copiedLobs.setProductTypSubtyp(workLobs.getProductTypSubtyp());
		copiedLobs.setLastName(workLobs.getLastName());
		copiedLobs.setFirstName(workLobs.getFirstName());
		copiedLobs.setMiddleInitial(workLobs.getMiddleInitial());
		copiedLobs.setAppState(workLobs.getAppState());
		copiedLobs.setSsnTin(workLobs.getSsnTin());
		//End ALS2220
	}

	/**
	 * Create an Attachments for the text data contained in the Source.
	 * 
	 * @param aSource
	 *            - the Source
	 * @param attachmentType
	 *            - the type of attachments to create
	 * @return Attachment
	 */
	//NBA212 New Method
	protected Attachment createAttachmentsForText(NbaSource aSource, long attachmentType) {
		Attachment attach = initializeNewAttachment(attachmentType);
		attach.setAttachmentLocation(NbaOliConstants.OLI_INLINE);
		attach.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT);
		AttachmentData attachData = new AttachmentData();
		attachData.setPCDATA(aSource.getText());
		attach.setAttachmentData(attachData);
		return attach;
	}

	/**
	 * Create Attachments for the Images associated with the Source. For Content Services, there may be more than one image.
	 * 
	 * @param aSource
	 *            - the Source containing the Image identification information
	 * @param attachmentType
	 *            - the type of attachments to create
	 * @return List containing the newly created Attachments
	 * @throws NbaBaseException
	 * @throws NbaNetServerException
	 */
	//NBA212 New Method
	protected List createAttachmentsForImages(NbaSource aSource, long attachmentType) throws NbaBaseException, NbaNetServerException {
		List newAttachments = new ArrayList();
		List images = new ArrayList();
		try {
			images = WorkflowServiceHelper.getBase64SourceImage(getUser(), aSource);
		} catch (NbaBaseException e) {
			e.forceFatalExceptionType();
			throw e;
		} catch (Throwable t) {
			NbaNetServerException e = new NbaNetServerException(NbaNetServerException.GET_SOURCE_IMAGE, t, NbaExceptionType.FATAL);
			throw e;
		}
		for (int i = 0; i < images.size(); i++) {
			Attachment attach = initializeNewAttachment(attachmentType);
			attach.setAttachmentLocation(NbaOliConstants.OLI_INLINE);
			attach.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_IMAGE);
			attach.setImageType(NbaOliConstants.OLI_IMAGE_TIFF);
			AttachmentData attachData = new AttachmentData();
			attachData.setPCDATA((String) images.get(i));
			attach.setAttachmentData(attachData);
			newAttachments.add(attach);	//CR60669-APS Order Authorization
		}
		return newAttachments;
	}

	/**
	 * Create and initialize an Attachment
	 * 
	 * @param attachmentType
	 * @return the new Attachment
	 */
	//NBA212 New Method
	protected Attachment initializeNewAttachment(long attachmentType) {
		Attachment attach = new Attachment();
		attach.setAttachmentType(attachmentType);
		attach.setUserCode(NbaUtils.getBusinessProcessId(getUser()));
		attach.setDateCreated(new Date());
		attach.setActionAdd();
		return attach;
	}

	/**
	 * Create Attachment objects using the data  passed in the parameters. 
	 * 
	 * @param aSource
	 *            the NbaSource object that may contain the data for the AttachmentData or the identification of the Images
	 * @param attachmentType
	 *            the type of attachment to create
	 * @return List containing the newly created Attachments
	 */
	//NBA212 New Method
	protected List createAttachments(NbaSource aSource, long attachmentType, boolean embedData) throws NbaBaseException {
		Attachment attach;
		List newAttachments = new ArrayList();
		if (embedData) {
			if (aSource.isImageFormat()) {
				newAttachments = createAttachmentsForImages(aSource, attachmentType);
			} else {
				newAttachments.add(createAttachmentsForText(aSource, attachmentType));
			}
		} else {
			attach = initializeNewAttachment(attachmentType);
			attach.setAttachmentSource(aSource.getID());
			attach.setAttachmentLocation(NbaOliConstants.OLI_URLREFERENCE);
			attach.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_FILE);
			newAttachments.add(attach);
		}
		return newAttachments;
	}

	//NBA213 New Method
	protected NbaDst retrieveWorkItem(NbaUserVO nbaUserVO, NbaAwdRetrieveOptionsVO retOpt) throws NbaBaseException {
		retOpt.setNbaUserVO(nbaUserVO);
		AccelResult accelResult = (AccelResult) currentBP.callBusinessService("NbaRetrieveWorkBP", retOpt);
		NewBusinessAccelBP.processResult(accelResult);
		return (NbaDst) accelResult.getFirst();
	}

	//NBA213 New Method
	protected List retrieveWorkItemList(NbaUserVO nbaUserVO, NbaAwdRetrieveOptionsVO retOpt, List workItemIdList) throws NbaBaseException {
		retOpt.setNbaUserVO(nbaUserVO);
		List dstList = new ArrayList();
		Iterator it = workItemIdList.iterator();
		String id;
		boolean caseInd;
		while (it.hasNext()) {
			id = (String) it.next();
			caseInd = "C".equals(id.substring(26, 27));
			retOpt.setWorkItem(id, caseInd);
			AccelResult accelResult = (AccelResult) currentBP.callBusinessService("NbaRetrieveWorkBP", retOpt);
			NewBusinessAccelBP.processResult(accelResult);
			NbaDst dst = (NbaDst) accelResult.getFirst();
			dstList.add(dst);
		}
		return dstList;
	}

	//NBA213 New Method
	protected void suspendWork(NbaUserVO nbaUserVO, NbaSuspendVO suspend) throws NbaBaseException {
		suspend.setNbaUserVO(nbaUserVO);
		AccelResult accelResult = (AccelResult) currentBP.callBusinessService("NbaSuspendWorkBP", suspend);
		NewBusinessAccelBP.processResult(accelResult);
	}

	//NBA213 New Method
	protected void unlockWork(NbaUserVO nbaUserVO, NbaDst item) throws NbaBaseException {
		item.setNbaUserVO(nbaUserVO);
		AccelResult accelResult = new AccelResult();
		accelResult.merge(currentBP.callBusinessService("NbaUnlockWorkBP", item));
		NewBusinessAccelBP.processResult(accelResult);
	}

	//NBA213 New Method
	protected void unsuspendWork(NbaUserVO nbaUserVO, NbaSuspendVO suspendVO) throws NbaBaseException {
		suspendVO.setNbaUserVO(nbaUserVO);
		AccelResult accelResult = new AccelResult();
		accelResult.merge(currentBP.callBusinessService("NbaUnsuspendWorkBP", suspendVO));
		NewBusinessAccelBP.processResult(accelResult);
	}

	//NBA213 New Method
	protected NbaDst updateWork(NbaUserVO nbaUserVO, NbaDst item) throws NbaBaseException {
		item.setNbaUserVO(nbaUserVO);
		AccelResult accelResult = (AccelResult) currentBP.callBusinessService("NbaUpdateWorkBP", item);
		NewBusinessAccelBP.processResult(accelResult);
		return (NbaDst) accelResult.getFirst();
	}

	//NBA213 New Method
	protected NbaSearchVO lookupWork(NbaUserVO nbaUserVO, NbaSearchVO item) throws NbaBaseException {
		item.setNbaUserVO(nbaUserVO);
		AccelResult accelResult = (AccelResult) currentBP.callBusinessService("SearchWorkflowBP", item);
		NewBusinessAccelBP.processResult(accelResult);
		return (NbaSearchVO) accelResult.getFirst();
	}

	//NBA213 New Method
	protected NbaDst createCase(NbaUserVO userVO, String businessArea, String workType, String status, NbaLob nbaLob) throws NbaBaseException {
		return WorkflowServiceHelper.createCase(userVO, businessArea, workType, status, nbaLob);
	}

	//NBA213 New Method
	protected List getBase64SourceImage(NbaUserVO userVO, NbaSource source) throws NbaBaseException {
		return WorkflowServiceHelper.getBase64SourceImage(userVO, source);
	}

	/**
	 * @return Returns the currentBP.
	 */
	//NBA213 New Method
	public NbaAutoProcessAccelBP getCurrentBP() {
		return currentBP;
	}

	/**
	 * @param currentBP
	 *            The currentBP to set.
	 */
	//NBA213 New Method
	public void setCurrentBP(NbaAutoProcessAccelBP currentBP) {
		this.currentBP = currentBP;
	}

	/**
	 * Get history for the NbaDst. 
	 * 
	 * @param nbaDst
	 * @return the NbaDst including history
	 * @throws NbaBaseException
	 */
	//NBA08-1 New Method
	protected NbaDst retrieveHistory(NbaDst nbaDst) throws NbaBaseException {
		AccelResult accelResult = (AccelResult) currentBP.callBusinessService("NbaRetrieveHistoryBP", nbaDst);
		NewBusinessAccelBP.processResult(accelResult);
		return (NbaDst) accelResult.getFirst();
	}

	/*
	 * The method is responsible for retrieving a list of comments from the database
	 * 
     * @return nbaWorkItem   
	 * 
     * @throws NbaBaseException
     */
	//SPRNBA-376 new method
    protected NbaDst retrieveComments(NbaDst work) throws NbaBaseException {	
        AccelResult accelResult = (AccelResult) currentBP.callBusinessService("RetrieveCommentsBP", setRetrievalProperties(work));	
        NewBusinessAccelBP.processResult(accelResult);
        return (NbaDst) accelResult.getFirst();	
    }	
	
    /**
     * This method sets the properties to retrieve comments rom the database
	 * 
	 * @param work
	 *            NbaWorkItem
     * @return NbaRetrieveCommentsRequest
     * @return String containing the rule to execute     
     */
    //SPRNBA-376 new method
    protected NbaRetrieveCommentsRequest setRetrievalProperties(NbaDst work) {  
        NbaRetrieveCommentsRequest commentsReq = new NbaRetrieveCommentsRequest();
        commentsReq.setNbaDst(work);   
        commentsReq.setRetrieveChildren(false);
        return commentsReq;
    }
	
	//NBA196 New Method
	public void sendEmail(NbaUserVO nbaUserVO, NbaEmailVO emailVO) throws NbaBaseException {
		AccelResult accelResult = (AccelResult) currentBP.callBusinessService("SendEmailBP", emailVO);
		NewBusinessAccelBP.processResult(accelResult);
	}

	/**
	 * Lock a Work Item
	 * 
	 * @param retrieveWorkItemsRequest
	 * @return AccelResult
	 * @throws NbaBaseException
	 */
	//SPR3611 New Method
	protected AccelResult lockWorkItem(RetrieveWorkItemsRequest retrieveWorkItemsRequest) throws NbaBaseException {
		AccelResult accelResult = (AccelResult) getCurrentBP().callBusinessService("LockItemsBP", retrieveWorkItemsRequest); //APSL5055-NBA331.1
		NewBusinessAccelBP.processResult(accelResult);
		return accelResult;
	}

	/**
	 * @param preventProcessData
	 * @return
	 * @throws NbaBaseException
	 */
	//new method AXAL3.7.40G
	protected boolean suspendCase(AxaPreventProcessVpmsData preventProcessData, long suspendReason) throws NbaBaseException {
		SuspendInfo suspendInfo = null;
		Policy policy = nbaTxLife.getPolicy();
		Date initialSuspendDate = new Date();
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExtension == null) {
			OLifEExtension olife = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			olife.setActionAdd();
			policy.addOLifEExtension(olife);
			policyExtension = olife.getPolicyExtension();
			policyExtension.setActionAdd();
		}

		if (policyExtension.hasSuspendInfo()) {
			suspendInfo = policyExtension.getSuspendInfo();
			suspendInfo.setActionUpdate();
			if (suspendInfo.hasSuspendDate() && suspendInfo.getUserCode().equals(getUser().getUserID())) {
				initialSuspendDate = policyExtension.getSuspendInfo().getSuspendDate();
			}
		} else {
			suspendInfo = new SuspendInfo();
			suspendInfo.setActionAdd();
			suspendInfo.setSuspendDate(initialSuspendDate);
			suspendInfo.setUserCode(getUser().getUserID());
			policyExtension.setSuspendInfo(suspendInfo);
			policyExtension.setActionUpdate();
		}
		if (getResult() == null) {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(initialSuspendDate);
			Date currentDate = new Date();
			calendar.add(preventProcessData.getCalenderSuspendUnit(), Integer.parseInt(preventProcessData.getMaxSuspendTime()));
			Date maxSuspendDurationDate = calendar.getTime();
			if (currentDate.after(maxSuspendDurationDate)) {
				addComment("Case cannot be suspended because the maximum suspend duration has been exceeded");
				return false;
			} else {
				//set SuspendReason flag here
				suspendInfo.setSuspendReason(suspendReason);
				nbaTxLife = doContractUpdate();
				handleHostResponse(nbaTxLife);
				NbaSuspendVO tempsuspendVO = new NbaSuspendVO();
				tempsuspendVO.setCaseID(getWork().getID());
				tempsuspendVO.setActivationDate(preventProcessData.getSuspendActivationDate());
				setSuspendVO(tempsuspendVO);
				updateForSuspend();
				return true;
			}

		}
		return false;
	}

	/**
	 * This Method Updates the Case for getting suspend in the AWD and if there is some error in case getting suspended it throws the Exception
	 * 
	 */
	// AXAL3.7.40G New Method	
	public void updateForSuspend() throws NbaBaseException {
		getLogger().logDebug("Starting updateForSuspend");
		updateWork(getUser(), getWork());
		suspendWork(getUser(), getSuspendVO());
		unlockWork(getUser(), getWork());
	}

	/**
	 * This Methods Returns the Suspend VO
	 * 
	 */
	//AXAL3.7.40G New Method 
	public NbaSuspendVO getSuspendVO() {
		return suspendVO;
	}

	/**
	 * This Method Sets the Suspend VO.
	 * 
	 */
	// AXAL3.7.40G New Method.
	public void setSuspendVO(NbaSuspendVO newSuspendVO) {
		suspendVO = newSuspendVO;
	}

	/**
	 * Work item will be equitably distributed among the returned queues. If only one queue is allowed, that queue will be returned.
	 * 
	 * @return equitable queue
	 */
	//NBA251 New Method
	protected String getEquitableQueue(String queue, String lob) throws NbaBaseException {
		List listQueues = getListOfQueues(queue);
		if (listQueues.size() == 1) {
			return (String) listQueues.get(0);
		} else if (listQueues.size() == 0) {
			throw new NbaBaseException("Unable to determine a valid Queue.", NbaExceptionType.FATAL);
		}
		return determineEquitableQueue(listQueues, lob);
	}

	/**
	 * Parses the queues string returned from the AutoProcessStatus model and converts it into a list containing Strings.
	 * 
	 * @return list of queues
	 */
	//NBA251 New Method
	protected List getListOfQueues(String resultQueues) {
		ArrayList queues = new ArrayList();
		if (resultQueues != null) {
			NbaStringTokenizer st = new NbaStringTokenizer(resultQueues, ",");
			while (st.hasMoreTokens()) {
				queues.add(st.nextToken());
			}
		}
		return queues;
	}

	/**
	 * Parses the queues string returned from the AutoProcessStatus model and converts it into a list containing String having CM queues.
	 * 
	 * @return list of queues
	 */
	//NBA251 New Method
	protected String getListOfCMQueues(String resultQueues, String underwriterLOB) {
		String token = null;
		String underwriterToken = null;
		NbaStringTokenizer tokens = new NbaStringTokenizer(resultQueues, UW_DELIMITER);
		while (tokens.hasMoreTokens()) {
			token = tokens.nextToken();
			underwriterToken = token.substring(0, token.indexOf(CM_DELIMITER));
			if (underwriterToken.equalsIgnoreCase(underwriterLOB)) {
				return token.substring(token.indexOf(CM_DELIMITER) + 1);
			}
		}
		return CONTRACT_DELIMITER;
	}

	/**
	 * Determines the equitable queue from a list of possible queues. The work items for each Underwriter/Case Manager associated with the queue are
	 * counted and the queue with the fewest amount of work, will be assigned. If some or all queues have no work items, the first queue with no work
	 * will be returned.
	 * 
	 * @param list
	 *            of queues
	 * @param lob
	 *            value
	 * @return equitable queue
	 */
	//NBA251 New Method
	protected String determineEquitableQueue(List queues, String queueLOB) throws NbaBaseException {
		String equitableQueue = null;
		String queue = null;
		int workCount = 0;
		int equitableCount = 0;
		int count = queues.size();
		NbaWorkflowDistribution distribution = new NbaWorkflowDistribution(getWork().getNbaLob());
		for (int i = 0; i < count; i++) {
			queue = (String) queues.get(i);
			workCount = distribution.getAssignedWorkCountByQueue(queue, queueLOB);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("equitable queue check:" + queue + " - " + workCount);
			}
			if (i == 0 || workCount < equitableCount) {//assigns first queue Or a queue having lesser work count to equitableQueue 
				equitableCount = workCount;
				equitableQueue = queue;
			}
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("equitable queue:" + equitableQueue);
		}
		return equitableQueue;
	}

	/**
	 * Returns NbaProcessStatusProvider with calling VP/MS model.
	 * 
	 * @param caseLob
	 * @return NbaProcessStatusProvider
	 * @throws NbaBaseException
	 */
	//NBA251 new method - AXAL3.7.3M1 modified and moved this method from NbaProcAppSubmit
	protected NbaProcessStatusProvider getProcesStatusProvider(NbaLob caseLob, NbaTXLife nbaTxLife) throws NbaBaseException {
		HashMap deOink = new HashMap();
		//DeOink the itegrated client
		Category configCategory = NbaConfiguration.getInstance().getIntegrationCategory(caseLob.getBackendSystem(),
				NbaConfigurationConstants.UNDERWRITINGRISK);
		if (configCategory != null && configCategory.hasValue()) {
			deOink.put(NbaVpmsConstants.A_INTEGRATED_CLIENT, configCategory.getValue());
		} else {
			deOink.put(NbaVpmsConstants.A_INTEGRATED_CLIENT, "");
		}
		//begin AXAL3.7.20
		String processId = NbaUtils.getBusinessProcessId(getUser());
		if (PROC_NBAFORMAL.equalsIgnoreCase(processId) ||
				//PROC_AUTO_UNDERWRITING.equalsIgnoreCase(processId) || 	//Code commented by ALS3347 
				PROC_NBACREATE.equalsIgnoreCase(processId)) {
			AxaUtils.deOinkCWAInd(getWork(), deOink);
		}
		//end AXAL3.7.20
		//begin ALCP153
		if (PROC_APP_SUBMIT.equalsIgnoreCase(processId)) {
			deOink.put("A_APSAWDInd", String.valueOf(APSAWDInd));
			//NBLXA-2343, NBLXA-2658 deleted Switch over logic code
			} 
		//end ALCP153
		//deOinkAgentFields(deOink, nbaTxLife);
		//start NBA300
		if (PROC_AUTO_UNDERWRITING.equalsIgnoreCase(processId)) {
			deOinkTermConvData(deOink);
		}
		//end NBA300
		//APSL4967 begin
		if (PROC_POST_ISSUE_ASSIGNMENT.equalsIgnoreCase(processId)) {
			A_PDR_CV_IND=NbaUtils.validatePDRCVOnCase(nbaTxLife);
			deOink.put("A_PDR_CV_IND", String.valueOf(A_PDR_CV_IND));
		}
		//APSL4967 End
		//Begin APSl5164
		if (PROC_ELEC_DRAFT_MONEY.equalsIgnoreCase(processId)) {
			try {
				Date currentdate = new Date();
				if (!NbaUtils.isBlankOrNull(getWork().getNbaLob().getCwaDate())
						&& NbaUtils.compare(getWork().getNbaLob().getCwaDate(), currentdate) == 0) {
					if (getWork().getNbaLob().getCwaTime() != null) {
						boolean suspendIndForCipe = NbaUtils.isDateAfterTodays4PM(getWork().getNbaLob().getCwaTime());
						deOink.put("A_CIPE_Suspend_Ind", String.valueOf(suspendIndForCipe));
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		//End APSL5164
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(caseLob);
		oinkData.setContractSource(nbaTxLife); //AXAL3.7.3M1
		NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(
				getDataFromVpms(NbaVpmsAdaptor.AUTO_PROCESS_STATUS, NbaVpmsAdaptor.EP_WORKITEM_STATUSES, oinkData, deOink, null));
		return statusProvider;
	}

	/**
	 * This method deOinks agent fields by calling the webservice
	 * 
	 * @param deOinkMap
	 * @param nbaTxLife
	 * @throws NbaBaseException
	 */
	//AXAL3.7.3M1 New Method
	protected void deOinkAgentFields(HashMap deOinkMap, NbaTXLife nbaTxLife) throws NbaBaseException {

		Relation relation = nbaTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
		if (relation == null) {
			relation = nbaTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_ADDWRITINGAGENT);
			if (relation == null) {
				relation = nbaTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
			}
		}
		if (relation != null) {
			AxaProducerVO producerVO = new AxaProducerVO().createProducerVOfromTXLife(nbaTxLife, relation);
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_AGENTDEMOGRAPICS, getUser(), null,
					null, producerVO);// AXAL3.7.18
			NbaTXLife resTXLife= (NbaTXLife) webServiceInvoker.execute();//AXAL3.7.18
			TransResult transResult = resTXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0)
					.getTransResult();

			if (transResult.getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS) {
				Relation generalAgentRel = resTXLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
				if (generalAgentRel != null) {
					NbaParty generalAgentParty = resTXLife.getParty(generalAgentRel.getRelatedObjectID());
					if (generalAgentParty.getParty().hasProducer() && generalAgentParty.getParty().getProducer().getCarrierAppointmentCount() > 0) {
						CarrierAppointment generalCarrierAppointment = (CarrierAppointment) generalAgentParty.getParty().getProducer()
								.getCarrierAppointment().get(0);
						CarrierAppointmentExtension extn = NbaUtils.getFirstCarrierAppointmentExtension(generalCarrierAppointment);
						if (extn != null) {
							deOinkMap.put("A_BGAUWTeam_PWA", extn.getBGAUWTeam());
							deOinkMap.put("A_SalesRegion_PWA", extn.getSalesRegion());
						}
					}
				}

				Relation superAgentRel = resTXLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_SUPERIORAGENT);
				if (superAgentRel != null) {
					NbaParty superAgentParty = resTXLife.getParty(superAgentRel.getRelatedObjectID());
					if (superAgentParty.getParty().hasProducer() && superAgentParty.getParty().getProducer().getCarrierAppointmentCount() > 0) {
						deOinkMap.put("A_SuperBGANbr_PWA",
								((CarrierAppointment) superAgentParty.getParty().getProducer().getCarrierAppointment().get(0))
										.getCompanyProducerID());
					}
				}

			} else {

			}
		}

	}

	/**
	 * Answers the LCMQ for a process by requesting that status from the statusProvider object.
	 * 
	 * @return a string containing the LCMQ
	 */
	//AXAL3.7.20	  New method 
	public java.lang.String getLicenseCaseMangerQueue() {
		if (getStatusProvider() == null) {
			return null;
		}
		return getStatusProvider().getLicenseCaseMangerQueue();
	}

	/*
	 * DeOink a Form # based on NBA_Forms_Validation table
	 */
	//NBA231 new method
	protected void deOinkFormType(Map deOinkMap) {
		if (getWork().isTransaction()) {
			return;
		}
		int form = 0;
		List allSources = getWork().getNbaSources();
		int size = allSources.size();
		NbaTableAccessor nta = new NbaTableAccessor();
		String formNumber;
		int localInt;
		for (int i = 0; i < size; i++) {
			NbaSource aSource = (NbaSource) allSources.get(i);
			if (aSource.isMiscMail() && null != aSource.getNbaLob().getFormNumber()) {
				formNumber = aSource.getNbaLob().getFormNumber();
				localInt = getFormType(nta, formNumber);
				if (localInt > form) {
					form = localInt;
				}
			}

		}
		deOinkMap.put("A_FormType", String.valueOf(form));
	}

	/*
	 * Retrieve form # from NBA_Forms_Validation
	 */
	//NBA231 new method
	private int getFormType(NbaTableAccessor nta, String formNumber) {
		String formType;
		int localInt = 0;
		try {
			formType = nta.getFormType(formNumber);
			localInt = Integer.parseInt(formType);
		} catch (Exception exp) {

		}
		return localInt;
	}

	/**
	 * Answer the awd case and sources 
	 * 
	 * @return NbaDst which represent a awd case
	 */
	// AXAL3.7.20 New Method
	protected void getCaseAWDSources() throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(getWork().getID(), true);
		retrieveOptionsValueObject.requestSources();
		retrieveOptionsValueObject.requestTransactionAsChild();
		retrieveOptionsValueObject.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject));
	}
	
	/**
	 * Retrieve the Case without transactions
	 * 
	 * @throws NbaBaseException
	 */
	//ALS3959 new method
	protected NbaDst retrieveParentCaseOnly() throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestCaseAsParent();
		retOpt.setLockParentCase();
		return retrieveWorkItem(getUser(), retOpt);
	}
	
	//ALS5260 New Method
	public String getRouteReason(){
		String routeReason = "";
		if (statusProvider != null){
			routeReason = statusProvider.getReason();
		}
		return routeReason;		
	}
	/**
	 * @return Returns the reason.
	 */
	//ALS5260 New Method
	public String getReason() {
		return reason;
	}
	/**
	 * @param reason
	 *            The reason to set.
	 */
	//ALS5260 New Method
	public void setReason(String reason) {
		this.reason = reason;
	}
	/**
	 * Determine if an Evaluate system message is present and, if yes, delete the message from the contract.
	 * 
	 * @throws NbaBaseException
	 */
	// ALS5252 refactored
	protected boolean removeErrorMessage() {
		boolean msgRemoved = false;
		Holding primaryHolding = nbaTxLife.getPrimaryHolding();
		int count = primaryHolding.getSystemMessageCount();
		for (int i = 0; i < count; i++) {
			SystemMessage sysMsg = primaryHolding.getSystemMessageAt(i);
			if (sysMsg.getMessageCode() == NbaConstants.EVAL_NEEDED_ERROR) {
				sysMsg.setActionDelete();
				msgRemoved = true;
				break;
			}
		}
		return msgRemoved;
	}
	

	//ALS4843 new Method added
	protected int getFollowUpFrequency(String refid) throws NbaBaseException {
		if (null == refid || refid.length() == 0) {
			return 0;
		}
		NbaTXLife nbaTxLife = doHoldingInquiry();
		RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(refid);
		if (null != reqInfo ) {
			RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
			if (null != reqInfoExt && reqInfoExt.hasFollowUpFreq()) {
				return reqInfoExt.getFollowUpFreq();
			}
		}
		return 0;	
	}
	

	/**
	 * @return NbaVpmsResultsData which contains the maximum number of follow-ups allowed.
	 * @param entryPoint
	 *            "P_GetFollowUpDays" is the entryPoint for VPMS model.
	 * @throws a
	 *             fatal NbaBaseException if a problem occurs executing the model
	 */
	//ALS4843 new method added 
	protected NbaVpmsResultsData getRequirementDataFromVpms(String entryPoint, NbaOinkDataAccess oinkData) throws NbaBaseException {
	    NbaVpmsAdaptor vpmsProxy = null; 
	    try {
			getLogger().logDebug("Starting Retrieval of data from VPMS model"); 
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaConfiguration.REQUIREMENTS); 
			Map deOinkMap = new HashMap();
			deOinkMap.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			return data;
		} catch (java.rmi.RemoteException re) {
			String desc = new StringBuffer().append("Model: ").append(NbaConfiguration.REQUIREMENTS).append(", entrypoint:  ").append(entryPoint)
					.toString();
			throw new NbaVpmsException(desc, re, NbaExceptionType.FATAL);
		} catch (NbaBaseException e) {
			e.forceFatalExceptionType();
			throw e;
		} finally {
			try {
			    if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
			} catch (RemoteException re) {
			    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
			}
		}
	}	
	
    /**
     * Returns a string representing the current time stamp from the workflow system. 
	 * 
     * @param nbaUserVO
     * @return
     * @throws NbaBaseException
     */
	//ALS5428 New Method
    protected String getTimeStamp(NbaUserVO nbaUserVO) throws NbaBaseException {
        AccelResult res = (AccelResult)currentBP.callBusinessService("NbaRetrieveTimeStampBP", nbaUserVO);
        NewBusinessAccelBP.processResult(res);
        NbaDst timeStamp = (NbaDst)res.getFirst();
        //NBA208-32
        return timeStamp.getTimestamp();
    }
    
    /**
	 * This method retrieves underwriter level for underwriter role from the NBA_UNDAPPROVALHIERARCHY table.
	 * 
	 * @param undRole
	 *            underwriter role
	 * @param systemID
	 *            SYSTEM_ID column value
	 * @param companyCode
	 *            COMPANY_CODE column value
	 * @return int undRoleLevel
	 */
 // ALS5428 New Method
 	public int getUnderwriterLevel(String undQueue, String systemID, String companyCode) throws NbaBaseException { //NBLXA-2489
 		String undRoleLevel = null;
 		Map tableKeys = new HashMap();
 		tableKeys.put(NbaTableAccessConstants.SYSTEM_ID, systemID);
 		tableKeys.put(NbaTableAccessConstants.COMPANY_CODE, companyCode);
 		tableKeys.put(NbaTableAccessConstants.UW_QUEUE, undQueue); //NBLXA-2489
 		tableKeys.put(NbaTableAccessConstants.C_TABLE_NAME, NbaTableConstants.NBA_UNDAPPROVALHIERARCHY);
 		NbaTableAccessor tableAccessor = new NbaTableAccessor();
 		undRoleLevel = tableAccessor.getUnderwriterLevel(tableKeys);
 		if (!NbaUtils.isBlankOrNull(undRoleLevel)) { //NBLXA-2489
 			return Integer.parseInt(undRoleLevel);
 		}
 		return 0;
 	}
	//APSL215 New Method
	protected String getEquitableQueue(List queuesList, String lob) throws NbaBaseException {
		if (queuesList.size() == 1) {
			return (String) queuesList.get(0);
		} else if (queuesList.size() == 0) {
			throw new NbaBaseException("Unable to determine a valid Queue.", NbaExceptionType.FATAL);
		}
		return determineEquitableQueue(queuesList, lob);
	}
	//APSL215 New Method
	protected String getUnedrwriterQueue(String undQueueValues) throws NbaBaseException {
		String undQueue = null;
		NbaTableAccessor nta = new NbaTableAccessor();
		List uwQueues = getListOfQueues(undQueueValues);
		//Check if the UW determined by equitable distribution is Available, if not look for another.
		while (uwQueues.size() > 0) {
			if (uwQueues.size() > 1) {
				undQueue = getEquitableQueue(uwQueues, NbaLob.A_LOB_ORIGINAL_UW_WB_QUEUE);
			} else {
				undQueue = (String) uwQueues.get(0);
			}
			String userId = nta.getUserIdForQueue(undQueue);
			if (NbaConstants.USER_STAT_AVAILABLE.equalsIgnoreCase(nta.getUserStatus(userId))) {
				return undQueue;
			}
			uwQueues.remove(undQueue);
		}
		//If all UWs are not Available, assign default UW.
		NbaVpmsAdaptor vpmsProxy = null; //APSL588
		try {
			vpmsProxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(getWork().getNbaLob()), NbaVpmsConstants.AUTO_PROCESS_STATUS); //APSL588
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_DEFAULT_UW_QUEUE);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			List resultsData = data.getResultsData();
			if (resultsData != null && resultsData.size() > 0) {
				undQueue = (String) resultsData.get(0);
				defaultUWInd = true; //APSL454
			}
		} catch (Exception ex) {
			throw new NbaBaseException(ex);
		//Begin APSL588	
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		//End APSL588	
		}
		
		return undQueue;
	}
	
	/**
	 * This method gets all Term Conv deOink variables by calling CONVERSIONUNDERWRITING model	 
	 * 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//NBA300 new method
	protected void deOinkTermConvData(Map deOink) throws NbaBaseException {
		//Calling a new Term Conversion model to determine if underwriting is required based on certain data 
		//entered on the Replacement view for the term conversion. This model will return two pieces of data:
		//(a)a Boolean to indicate if underwriting is required, (b) a conversion increase amount to be used for underwriting, if applicable
		//The vpmsadaptor object, which provides an interface into the VPMS system
	    NbaVpmsAdaptor vpmsProxy = null;
	    String entryPoint = EP_RESULTXML;
	    try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getNbaTxLife());
			oinkData.setContractSource(getNbaTxLife(), getWork().getNbaLob());
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.CONVERSIONUNDERWRITING); 
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			VpmsComputeResult result = vpmsProxy.getResults();
			String undReqdValue = "true";
			String convIncrAmtValue = "0";
			if (!result.isError()) {
	            NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(result);
	            List rulesList = vpmsResultsData.getResultsData();
	            if (!rulesList.isEmpty()) {
	                String xmlString = (String) rulesList.get(0);
		            NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
		            VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
		            List strAttrs = vpmsModelResult.getStandardAttr();
		            //Generate delimited string if there are more than one parameters returned
		            Iterator itr = strAttrs.iterator();
	        		while (itr.hasNext()) {
		            	StandardAttr stdAttr = (StandardAttr) itr.next();
		            	if("UndRequired".equalsIgnoreCase(stdAttr.getAttrName())){
		            		undReqdValue = stdAttr.getAttrValue();
		            	} else if("ConvIncreaseAmt".equalsIgnoreCase(stdAttr.getAttrName())){
		            		convIncrAmtValue = stdAttr.getAttrValue();
		            	}
	                }
	            }
	        }
			deOink.put("A_UndRequired", undReqdValue);
			deOink.put("A_ConvIncreaseAmt", convIncrAmtValue);
			
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("Auto Process, Term Conv deoink: " + NbaVpmsException.VPMS_EXCEPTION, re);
		} finally {
			try {
	            if (vpmsProxy != null) {
	                vpmsProxy.remove();
	            }
	        } catch (RemoteException re) {
	            getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
	        }
		}
	}
	
	//APSL425 New Method
	protected static boolean mergeMatchingCaseComments(NbaCase originalCase, NbaCase matchingCase) throws NbaBaseException {
		List matchingCaseComments = matchingCase.getWorkItem().getComments();
		boolean updateRqd = false;
        for (int i = 0; matchingCaseComments != null && i < matchingCaseComments.size(); i++) {
			Comment caseComment = (Comment) matchingCaseComments.get(i);
			if (NbaConstants.COMMENTS_TYPE_GENERAL.equalsIgnoreCase(caseComment.getType())) {
				NbaGeneralComment gc = new NbaGeneralComment(caseComment);
				gc.setActionAdd();
				gc.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
				Comment comment = gc.convertToManualComment();
				comment.setDateEntered(caseComment.getDateEntered());
				originalCase.addManualComment(comment);
				updateRqd = true;
			}
		}
        return updateRqd;
	}
	
	//APSL454 new method
	protected String getDefaultCaseManager() throws NbaBaseException{
		NbaVpmsAdaptor vpmsProxy = null; //APSL588
		String defCMQueue = null;
		try{
			vpmsProxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(getWork().getNbaLob()),NbaVpmsConstants.AUTO_PROCESS_STATUS); //APSL588
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_DEFAULT_CM_QUEUE);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			List result = data.getResultsData();
			if(result != null && result.size() > 0){
				defCMQueue = (String)result.get(0);
			}
		} catch(Exception ex){
			throw new NbaBaseException(ex);
				}
		return defCMQueue;
			}
	
	/**
	 * Calls VP/MS to check the CWA amount is sufficient or not.
	 * 
	 * @return True or False 
	 * @throws NbaBaseException
	 */
//	AXAL3.7.15 New Method
	//P2AXAL018 method moved from NbaprocMoneyUnderwriting class
	protected boolean isTotalCWAAmtValid() throws NbaBaseException {
		String strResult = getDataFromVpms(NbaVpmsConstants.AUTOMONEYUNDERWRITING, NbaVpmsConstants.EP_IS_TOTAL_CWA_AMT_VALID, null, null, null)
				.getResult();
		if (strResult != null && !strResult.trim().equals("")) {
			return Boolean.valueOf(strResult).booleanValue();
		}
		return false;
	}
	
	/**
	 * Calls VP/MS to get list of valid requirement queues to determine whether a requirement can be satisfied or not.
	 * 
	 * @return the list of valid requirement queues
	 * @throws NbaBaseException
	 */
//	NBA192 New Method
	//P2AXAL018 method moved from NbaprocMoneyUnderwriting class
	
	protected List getMoneyRequirementQueues() throws NbaBaseException {
		List moneyRequirementQueues = new ArrayList();
		NbaVpmsModelResult data = new NbaVpmsModelResult(getDataFromVpms(NbaVpmsConstants.REQUIREMENTS, NbaVpmsConstants.EP_GET_MONEY_REQ_QUEUES,
				new NbaOinkDataAccess(getWorkLobs()), null, null).getResult());
		if (data.getVpmsModelResult() != null && data.getVpmsModelResult().getResultDataCount() > 0) {
			ResultData resultData = data.getVpmsModelResult().getResultDataAt(0);
			int resultSize = resultData.getResult().size();
			for (int i = 0; i < resultSize; i++) {
				moneyRequirementQueues.add(resultData.getResultAt(i));
			}
		}
		return moneyRequirementQueues;
	}
	/**
	 * This method merges the details received from Life 70 system for an inforce contract. The details include policy status and address changes.
	 * These changes are added in the pending database as the system comment. 1. Retrieve HoldingExtension.ReissueInforceDetails object from the
	 * tempData 2. Retrieve Policy Status. Get the translated value from OLI_LU_POLSTAT NBA_UCT table. (ReissueInforceDetails.InforcePolicyStatus) 3.
	 * Retrieve name and address details (ReissueInforceDetails.InforcePartyDetails.NameAddrString) 4. Loop through the InforcePartyDetails objects
	 * and create a concatenated string � �Policy status is <<UCT translated status>>; <<InforceParty1.NameAddString>>; <
	 * <InforceParty2.NameAddString >>;<<InforceParty3.NameAddString >>;� 5. Add the concatenated string as the system comment.
	 * 
	 * @param tempData
	 *            an object of NbaTXLife retrieved from admin system
	 * @throws NbaBaseException
	 */	
	//CR60956 new method added
	protected long mergeLife70Details(NbaTXLife tempData, NbaTXLife nbAContract) throws NbaBaseException {
		long policyStatus = NbaOliConstants.OLI_POLSTAT_0;
		if (tempData != null) {
			HoldingExtension holdingExt = NbaUtils.getFirstHoldingExtension(tempData.getPrimaryHolding());
			List inforceDetailsList = holdingExt.getReissueInforceDetails();

			for (int i = 0; i < inforceDetailsList.size(); i++) { //This size is 1 at present
				StringBuffer comment = new StringBuffer();
				ReissueInforceDetails inforceDetails = (ReissueInforceDetails) inforceDetailsList.get(i);
				String nameAddress;
				InforcePartyDetails partyDetails;

				//Get inforce policy status
				policyStatus = inforceDetails.getInforcePolicyStatus();
				//Get Inforce party details
				List partyDetailsList = inforceDetails.getInforcePartyDetails();
				//translate from Policy status code
				String statusStr = NbaUtils.getTranslatedTableValue(OLI_LU_POLSTAT, Long.toString(policyStatus), getWorkLobs()); 
				comment.append("Policy status is " + statusStr + ";");

				for (int j = 0; j < partyDetailsList.size(); j++) {
					partyDetails = (InforcePartyDetails) partyDetailsList.get(j);
					//Get name and address
					nameAddress = partyDetails.getNameAddrString();
					//update the comment
					comment.append(nameAddress + ";");
				}
				addComment(comment.toString());
			}
			
			// APSL3928 Begin
			ArrayList tempAttachmentList = tempData.getPrimaryHolding().getAttachment();
	    	if (nbAContract != null && tempAttachmentList != null && tempAttachmentList.size() > 0) {
	    		Attachment tempDataAttachment = (Attachment)tempAttachmentList.get(0);
	    		boolean frelkMessageTrailerPresent = false;
	    		Attachment attachment = null;
	    		Holding holding = nbAContract.getPrimaryHolding();
	    		List attachmentList = holding.getAttachment();
	    		Iterator iterator = attachmentList.iterator();
	    		while (iterator.hasNext()) {
					attachment = (Attachment) iterator.next();
					if (NbaOliConstants.OLI_ATTACH_L70_MESSAGE_TRAILER == attachment.getAttachmentType()) {
						MessageTrailer messageTrailer = null;
						try {
							messageTrailer = MessageTrailer
									.unmarshal(new ByteArrayInputStream(attachment.getAttachmentData().getPCDATA().getBytes()));
							if (messageTrailer.getComment().startsWith(MESSAGETRAILER_FRELK)) {
								frelkMessageTrailerPresent = true;
								if (SYST_LIFE70.equalsIgnoreCase(attachment.getUserCode())) {
									MessageTrailer tempDataMessageTrailer = MessageTrailer
											.unmarshal(new ByteArrayInputStream(tempDataAttachment.getAttachmentData().getPCDATA().getBytes()));
									messageTrailer.setComment(tempDataMessageTrailer.getComment());
									messageTrailer.setUserNameEntered(tempDataMessageTrailer.getUserNameEntered());
									messageTrailer.setVoidInd(tempDataMessageTrailer.getVoidInd());									
									attachment.setDateCreated(new java.sql.Date(System.currentTimeMillis()));
									attachment.setActionUpdate();
								}
							}
						} catch (Exception exp) {
							throw new NbaBaseException("Unable to unmarshal Message Trailer comment", exp, NbaExceptionType.ERROR);
						}
					}
				}
	    		if (!frelkMessageTrailerPresent) {
	    			Attachment messageTrailerAttachment = (Attachment)tempAttachmentList.get(0);
	    			holding.addAttachment(messageTrailerAttachment);
	    			messageTrailerAttachment.setActionAdd();	    			
	    		}
			}
	    	// APSL3928 End
		}
		return policyStatus; // InforceDetailsList size is 1 at present
	}	
	
	/**
	 * APSL1526 new Method If the significant requirement received LOB is set to true on the case work item, then reset it to false. If the parent
	 * case has not been retrieved from the workflow system, then retrieve it now. If the LOB is reset, then update the case.
	 * 
	 * @throws RemoteException
	 * @throws NbaBaseException
	 */
	public void resetSignificantRequirementReceivedLOB(NbaDst parentCase, NbaLob reqLob) throws NbaBaseException {
 		NbaLob lobs = parentCase.getNbaLob();
		if (lobs.getSigReqRecd() && NbaRequirementUtils.isMedicalInd(nbaTxLife, reqLob)) {
			lobs.setSigReqRecd(false);
			parentCase.setUpdate();	
			updateWork(getUser(), parentCase);  
		}
		
	}
	
	public void resetSignificantRequirementReceivedLOB() throws  NbaBaseException {
 		NbaDst parentCase = retrieveParentWork(getWork(),true,false);
 		NbaLob reqLob = getWork().getNbaLob();
 		resetSignificantRequirementReceivedLOB(parentCase, reqLob);
 		List transactions = new ArrayList();
		WorkItem aCase = parentCase.getCase(); 
		transactions.addAll(aCase.getWorkItemChildren());		//Save the origional Transactions
		aCase.getWorkItemChildren().clear();
 		unlockWork(parentCase);
 		aCase.setWorkItemChildren(transactions);
	}

	// New Method QC9307/APSL2344
	public String getUnderwriter(NbaLob parentWork) throws NbaBaseException {
		NbaTable nbaTable = new NbaTable();
		String underwriterQueue = String.valueOf(parentWork.getUndwrtQueue());
		String underwriterName = nbaTable.getAWDQueueTranslation(parentWork.getBusinessArea(), underwriterQueue);
		if (null == underwriterName || (underwriterName != null && underwriterName.trim().length() == 0)) {
			underwriterQueue = getDefaultUnderwriter();
		}

		return underwriterQueue;
	}

	//	 QC9307 New Method/APSL2344
	protected String getDefaultUnderwriter() throws NbaBaseException {
		String undQueue = null;
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			vpmsProxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(getWork().getNbaLob()), NbaVpmsConstants.AUTO_PROCESS_STATUS);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_DEFAULT_UW_QUEUE);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			List resultsData = data.getResultsData();
			if (resultsData != null && resultsData.size() > 0) {
				undQueue = (String) resultsData.get(0);
			}
		} catch (Exception ex) {
			throw new NbaBaseException(ex);

		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		return undQueue;
	}

	/**
	 * This method first locate the party on contract using information on the workitem LOBs. If party is found that it retreive and return matched
	 * party.
	 * 
	 * @param lob
	 *            the workitem LOBs
	 * @return primary relation to contract for matched party.
	 */
	//Refactored ALNA159
	//P2AXAL054 renamed and refactored method
	protected Party getPartyForMatchingResult(NbaLob lob) {
		OLifE oLife = nbaTxLife.getOLifE();
		Party matchingParty = null;
		int partyCount = oLife.getPartyCount();
		ArrayList matchedPartyList = new ArrayList();
		//NBLXA-1254 start
		for (int i = 0; i < partyCount; i++) {
			NbaParty nbaParty = new NbaParty(oLife.getPartyAt(i));
			String entityName = "";
			String einTin = "";
			if (nbaParty.isOrganization()) {
				entityName = nbaParty.getFullName();
				einTin = nbaParty.getSSN();
				LogHandler.Factory.LogDebug(this,"CDD ********** entityName " + entityName);
			}
			if (entityName != null && entityName.equalsIgnoreCase(lob.getEntityName())) {
				matchingParty = nbaParty.getParty();
				LogHandler.Factory.LogDebug(this,"CDD ********** matched entityname");
				return matchingParty;
			}else if(einTin != null && einTin.equalsIgnoreCase(lob.getEntityEinTin())) {
				matchingParty = nbaParty.getParty();
				LogHandler.Factory.LogDebug(this,"CDD ********** matched entityein");
				return matchingParty;
			}
		}
		//NBLXA-1254 end		
		
		for (int i = 0; i < partyCount; i++) {
			NbaParty nbaParty = new NbaParty(oLife.getPartyAt(i));
			//	Begin ALNA159, ALNA161
			String lastName = "";
			if (nbaParty.isPerson()) {
				lastName = nbaParty.getLastName();
			} else if (nbaParty.isOrganization()) {
				lastName = nbaParty.getFullName();
			} //End ALNA159, ALNA161
			if (lastName != null && lastName.equalsIgnoreCase(lob.getLastName())) {
				matchedPartyList.add(nbaParty);
			}
		}

		if (matchedPartyList.size() == 1) {
			matchingParty = ((NbaParty) matchedPartyList.get(0)).getParty();
		} else if (matchedPartyList.size() > 1) {
			Iterator iterate = matchedPartyList.iterator();
			while (iterate.hasNext()) {
				NbaParty nbaParty = (NbaParty) iterate.next();
				String firstName = nbaParty.getFirstName();
				if (firstName == null || !firstName.equalsIgnoreCase(lob.getFirstName())) {
					iterate.remove();
				}
			}
			if (matchedPartyList.size() == 1) {
				matchingParty = ((NbaParty) matchedPartyList.get(0)).getParty();
			}else if (matchedPartyList.size() > 1) {
				Iterator it = matchedPartyList.iterator();
				while (it.hasNext()) {
					NbaParty matchingInsurableParty = (NbaParty) it.next();
					Relation relation = NbaUtils.getRelationForParty(matchingInsurableParty.getID(), nbaTxLife.getOLifE().getRelation().toArray());
					if (nbaTxLife.isInsured(matchingInsurableParty.getID())
							|| (!NbaUtils.isBlankOrNull(relation) && relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_OWNER))// NBLXA-2158
					{
						String firstNameTemp = matchingInsurableParty.getFirstName();
						if (!(firstNameTemp != null && firstNameTemp.equalsIgnoreCase(lob.getFirstName()))) {
							//return matchingInsurableParty.getParty();
							it.remove(); //NBLXA-2002
							}
					}
					//NBLXA-2002 begins
					else{
						it.remove();
					}
					//NBLXA-2002 ends
				}
				//NBLXA-2002 begins
				if (matchedPartyList.size() == 1) {
					matchingParty = ((NbaParty) matchedPartyList.get(0)).getParty();
				}else if (matchedPartyList.size() > 1) {
					Iterator ita = matchedPartyList.iterator();
					while (ita.hasNext()) {
						NbaParty matchedParty = (NbaParty) ita.next();
						String govID = matchedParty.getParty().getGovtID();
						if (govID != null && govID.equalsIgnoreCase(lob.getSsnTin())) {
							return matchedParty.getParty();
						}
					}
				}
				//NBLXA-2002 ends	
				}
		}
		if (matchingParty == null) {
			for (int i = 0; i < partyCount; i++) {
				Party party = oLife.getPartyAt(i);
				String ssn = party.getGovtID();
				if (ssn != null && ssn.equalsIgnoreCase(lob.getSsnTin())) {
					matchingParty = party;
				}
			}
		}
		return matchingParty;
	}

	/**
	 * This method gets all Term Conv deOink variables by calling CONVERSIONUNDERWRITING model	 
	 * 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//ALII1524 new method
	protected void deOinkTermConvData(Map deOink, NbaLob caseLob) throws NbaBaseException {
	    NbaVpmsAdaptor vpmsProxy = null;
	    String entryPoint = EP_RESULTXML;
	    try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getNbaTxLife());
			oinkData.setContractSource(getNbaTxLife(), caseLob);
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.CONVERSIONUNDERWRITING); 
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			VpmsComputeResult result = vpmsProxy.getResults();
			String undReqdValue = "true";
			String convIncrAmtValue = "0";
			if (!result.isError()) {
	            NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(result);
	            List rulesList = vpmsResultsData.getResultsData();
	            if (!rulesList.isEmpty()) {
	                String xmlString = (String) rulesList.get(0);
		            NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
		            VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
		            List strAttrs = vpmsModelResult.getStandardAttr();
		            //Generate delimited string if there are more than one parameters returned
		            Iterator itr = strAttrs.iterator();
	        		while (itr.hasNext()) {
		            	StandardAttr stdAttr = (StandardAttr) itr.next();
		            	if("UndRequired".equalsIgnoreCase(stdAttr.getAttrName())){
		            		undReqdValue = stdAttr.getAttrValue();
		            	} else if("ConvIncreaseAmt".equalsIgnoreCase(stdAttr.getAttrName())){
		            		convIncrAmtValue = stdAttr.getAttrValue();
		            	}
	                }
	            }
	        }
			deOink.put("A_UndRequired", undReqdValue);
			deOink.put("A_ConvIncreaseAmt", convIncrAmtValue);
			
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("Auto Process, Term Conv deoink: " + NbaVpmsException.VPMS_EXCEPTION, re);
		} finally {
			try {
	            if (vpmsProxy != null) {
	                vpmsProxy.remove();
	            }
	        } catch (RemoteException re) {
	            getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
	        }
		}
	}
	
	//APSL2808 added new method
	protected NbaTransResponseVO createTransResponse(String operationName) {
		NbaTransResponseVO transResponseVO = new NbaTransResponseVO();
		transResponseVO.setOperationName(operationName);
		transResponseVO.setUserLoginName(getUser().getUserID());		
		transResponseVO.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS);
		transResponseVO.setTransSubType(NbaOliConstants.TC_SUBTYPE_RTS_HOLDING_TRANSMIT);
		return transResponseVO;
	}
	
	/**
	 * When a non SI policy is routed to SI queue this method adds a comment to the case indicating that failure, changes the status of the case to
	 * indicate an error,
	 */
	// APSL3308(QC12368) NEW METHOD
	public void handleNonSICaseTOSIQueue() {
		addComment("Policy routed to SI queue, however this is not an SI Policy.");
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Incorrect queue", getAWDFailStatus()));		
	}
	
	   /**
     * Search for All APS requirement present on Case and check count of APS pages presents
	 * 
     * @return the search value object containing the result of the search
     * @throws NbaBaseException
     */
	//APSL3701 new method
    public int getAPSRecivedWithAppPageCount(NbaDst dst, String workType) throws NbaBaseException {        
        List transactions = dst.getTransactions();
        WorkItemSource nbaSource = null;
        int pageCount = 0;
        if (transactions != null && transactions.size() > 0) {
            int size = transactions.size();
            for (int i = 0; i < size; i++) {
                WorkItem aWorkItem = (WorkItem) transactions.get(i);
                if (workType.equals(aWorkItem.getWorkType())) {
                    String reqType = aWorkItem.getReqType();
                    if(String.valueOf(NbaOliConstants.OLI_REQCODE_PHYSSTMT).equals(reqType)) {
                        String frwaLob = aWorkItem.getLobValue("FRWA");
                        if(A_WT_REQUIREMENT.equalsIgnoreCase(workType) || (A_WT_MISC_MAIL.equalsIgnoreCase(workType) && "1".equals(frwaLob))) {
                            List sources = aWorkItem.getSourceChildren();
                            for (int j = 0; j < sources.size(); j++) {
                                nbaSource = (WorkItemSource) sources.get(j);
                                if (nbaSource != null) {
                                    String fileName = nbaSource.getFileName();                           
                                    if (!NbaUtils.isBlankOrNull(fileName)) {
                                        pageCount = pageCount + retrievePageCount(fileName); 
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        return pageCount;
    }
	
	//SR762709 (APSL3426)
	/**
     * Search for All APS requirement present on Case and check count of APS pages presents
	 * 
     * @return the search value object containing the result of the search
     * @throws NbaBaseException
     */
    public int getAPSPageCount(NbaLob nbaLob) throws NbaBaseException {
        NbaSearchVO searchVO = new NbaSearchVO();
        searchVO.setResultClassName(NbaSearchVO.TRANSACTION_SEARCH_RESULT_CLASS);
        searchVO.setWorkType(A_WT_REQUIREMENT);
        NbaLob searchLob = new NbaLob();
        NbaSource nbaSource = null;       
        int pageCount = 0;
        searchLob.setPolicyNumber(nbaLob.getPolicyNumber());
        searchLob.setReqType((int)NbaOliConstants.OLI_REQCODE_PHYSSTMT);
        searchVO.setNbaLob(searchLob);
        searchVO = lookupWork(getUser(), searchVO); 
        if (searchVO.getSearchResults().size() > 0) {
            for (int i = 0; i < searchVO.getSearchResults().size(); i++) { //APSL3701
                NbaTransactionSearchResultVO resultVO = (NbaTransactionSearchResultVO) searchVO.getSearchResults().get(i);
        		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
        		retOpt.setWorkItem(resultVO.getTransactionID(), false);
        		retOpt.requestSources();
        		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
        		List sources = aWorkItem.getNbaSources();
        		for (int j = 0; j < sources.size();j++){ //APSL3701
        			nbaSource = (NbaSource) sources.get(j);
        			if (nbaSource != null) {
        			    // Begin APSL3701  
        			    String fileName = nbaSource.getWorkItemSource().getFileName();       			    
        				//images = WorkflowServiceHelper.getBase64SourceImage(getUser(), nbaSource);
        			    if(!NbaUtils.isBlankOrNull(fileName)) {
        			        pageCount = pageCount + retrievePageCount(fileName); //APSL3701
        			    }
        			     // End APSL3701  
    				}
        		}
        	}
        }
        return pageCount;
    }	
    //APSL3701 new method
    public int retrievePageCount(String fileName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int pageCount = 0;
        try {
            conn = NbaConnectionManager.borrowConnection("AWDTables");
            HashMap sqlKey = new HashMap();
            sqlKey.putAll(NbaConfiguration.getInstance().getDatabaseSqlKeys(NbaTableConstants.AWD_TABLES));            
            String query = (String)sqlKey.get("FINDSOURCEPAGECOUNT");                
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, fileName);
            rs = pstmt.executeQuery();            
            if (rs.next()) {
                pageCount = rs.getInt(1);   
            }
        } catch (NbaBaseException nbae) {
            nbae.printStackTrace();
        } catch (Exception sqle) {
            String sqlMessage = sqle.getMessage();

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        return pageCount;
    }
	
    /**
     * Search for All APS requirement present on Case and check count of APS pages presents
	 * 
     * @return the search value object containing the result of the search
     * @throws NbaBaseException
     */
    public int getAPSRecivedWithAppPageCount(NbaLob nbaLob) throws NbaBaseException {
        NbaSearchVO searchVO = new NbaSearchVO();
        searchVO.setResultClassName(NbaSearchVO.TRANSACTION_SEARCH_RESULT_CLASS);
        searchVO.setWorkType(A_WT_MISC_MAIL);
        NbaLob searchLob = new NbaLob();             
        NbaSource nbaSource = null;
        int pageCount = 0;
        searchLob.setPolicyNumber(nbaLob.getPolicyNumber());
        searchLob.setReqType((int)NbaOliConstants.OLI_REQCODE_PHYSSTMT);
        searchLob.setFormRecivedWithAppInd(true);
        searchVO.setNbaLob(searchLob);
        searchVO = lookupWork(getUser(), searchVO); 
        
        if (searchVO.getSearchResults().size() > 0) {
            for (int i = 0; i < searchVO.getSearchResults().size(); i++) { //APSL3701
            	 
                NbaTransactionSearchResultVO resultVO = (NbaTransactionSearchResultVO) searchVO.getSearchResults().get(i);
                NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
        		retOpt.setWorkItem(resultVO.getTransactionID(), false);
        		retOpt.requestSources();
        		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
        		List sources = aWorkItem.getNbaSources();
        		     for (int j = 0; j < sources.size();j++){ //APSL3701
        		    	 nbaSource = (NbaSource) sources.get(j);
        		   			if (nbaSource != null) {
        		   			// Begin APSL3701  
                                String fileName = nbaSource.getWorkItemSource().getFileName();                      
                                //images = WorkflowServiceHelper.getBase64SourceImage(getUser(), nbaSource);
                                if(!NbaUtils.isBlankOrNull(fileName)) {
                                    pageCount = pageCount + retrievePageCount(fileName); //APSL3701
                                }
                                 // End APSL3701  
    			
    				}
        		}
        	}
        }
        return pageCount;
    }
  //SR762709 (APSL3426) new method
	/**
	 * new method to get status for Case that fulfill Retail Risk Righter criteria for informal cases
	 * 
	  * @throws NbaBaseException
	 */
	
	public List getRiskRighterStatus(NbaLob lob,int apsPageCount) throws NbaBaseException {
		NbaVpmsAdaptor vpmsAdaptor = null;
		try {
			Map deOink = new HashMap();
			deOink.put("A_APSPAGECOUNT", String.valueOf(apsPageCount));
			deOink.put("A_RiskRighterCaseStatus", "");
			NbaOinkDataAccess data = new NbaOinkDataAccess(lob);
			data.setContractSource(getNbaTxLife(), lob);
			vpmsAdaptor = new NbaVpmsAdaptor(data, NbaVpmsConstants.AUTO_PROCESS_STATUS);
			vpmsAdaptor.setVpmsEntryPoint(NbaVpmsConstants.EP_RISK_RIGHTER);
			vpmsAdaptor.setSkipAttributesMap(deOink);
			// get the string out returned by VP / MS Model
			VpmsComputeResult rulesProxyResult = vpmsAdaptor.getResults();
			if (!rulesProxyResult.isError()) {
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
				List resultData = vpmsResultsData.getResultsData();
				if (!resultData.isEmpty()) {
					return resultData;
				}
			}
			return null;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (vpmsAdaptor != null) {
				try {
					vpmsAdaptor.remove();
				} catch (RemoteException re) {
					LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
		}
	}	
	
	/**
	 * new method to get status for Case that fulfill Retail Risk Righter criteria for informal cases
	 * 
	  * @throws NbaBaseException
	 */
	
	public List getRiskRighterStatus(NbaLob lob,int apsPageCount,String riskRighterCaseStatus) throws NbaBaseException {
		NbaVpmsAdaptor vpmsAdaptor = null;
		try {
			Map deOink = new HashMap();
			deOink.put("A_APSPAGECOUNT", String.valueOf(apsPageCount));
			deOink.put("A_RiskRighterCaseStatus", riskRighterCaseStatus);
			NbaOinkDataAccess data = new NbaOinkDataAccess(lob);
			data.setContractSource(getNbaTxLife(), lob);
			vpmsAdaptor = new NbaVpmsAdaptor(data, NbaVpmsConstants.AUTO_PROCESS_STATUS);
			vpmsAdaptor.setVpmsEntryPoint(NbaVpmsConstants.EP_RISK_RIGHTER);
			vpmsAdaptor.setSkipAttributesMap(deOink);
			// get the string out returned by VP / MS Model
			VpmsComputeResult rulesProxyResult = vpmsAdaptor.getResults();
			if (!rulesProxyResult.isError()) {
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
				List resultData = vpmsResultsData.getResultsData();
				if (!resultData.isEmpty()) {
					return resultData;
				}
			}
			return null;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (vpmsAdaptor != null) {
				try {
					vpmsAdaptor.remove();
				} catch (RemoteException re) {
					LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
		}
	}
	
	//APSL3874 New Method
	public void handleErrorStatus() throws Exception{
		
	}
	
	//APSL3874 New Method
	protected String getPassStatusComment(String variance, String status) {
		Status aStatus = AxaStatusDefinitionLoader.determinePassStatus(getUser().getUserID(), variance);
		return aStatus.getComment();
	}
	/**
	 * Returns a string representing the current date from the workflow system. 
	 * 
	 * @param nbaUserVO
	 *            NbaUserVO object
	 * @return currentDate Date object
	 * @throws NbaBaseException
	 */
	//APSL5335 -- START
	protected Date getCurrentDateFromWorkflow(NbaUserVO nbaUserVO) throws NbaBaseException {
		Date currentDate = null;
		String timeStamp = getTimeStamp(nbaUserVO);
		if (timeStamp != null) {
			currentDate = NbaUtils.getDateFromStringInAWDFormat(timeStamp);
		}
		return currentDate;
	}
	//APSL5335 -- END
	// NBLXA -- 1337 :: START 
	protected void retrieveLicWorkItem(NbaTXLife nbaTXLife) {
		NbaSearchVO searchAgentLicensingVO;
			try {
				searchAgentLicensingVO = searchAgentLicesingWI(nbaTXLife,getUser());
				if (!NbaUtils.isBlankOrNull(searchAgentLicensingVO) && searchAgentLicensingVO.getSearchResults()!=null 
						&& !searchAgentLicensingVO.getSearchResults().isEmpty()) {
						 retrieveLicWorkItem(searchAgentLicensingVO.getSearchResults());
				}
				
			} catch (NbaBaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		
		}
			
	protected void retrieveExisitngLicensingWIFromEndQueue(NbaDst nbaDst, NbaTXLife nbaTXLife, Map deoinkMap) throws NbaBaseException {
		NbaUserVO tempUserVO = new NbaUserVO();
		String appendRoutReason = NbaUtils.getAppendReason(nbaTXLife);
		tempUserVO.setUserID(NbaConstants.PROC_AGENT_LIC_WORK_ROUTE);
		NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(tempUserVO, nbaDst, nbaTXLife, deoinkMap);
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		NbaLob nbaLob = searchResultForLicWIVO.getNbaLob();
		retOpt.setWorkItem(searchResultForLicWIVO.getWorkItemID(), false);
		retOpt.setLockWorkItem();
		NbaDst aWorkItem = WorkflowServiceHelper.retrieveWorkItem(getUser(), retOpt);
		
		aWorkItem.getNbaLob().setRouteReason(
				NbaUtils.getStatusTranslation(searchResultForLicWIVO.getWorkType(), statusProvider.getPassStatus())+" " + appendRoutReason);
		aWorkItem.getNbaLob().setCaseManagerQueue(nbaLob.getCaseManagerQueue());
		aWorkItem.getNbaLob().setLicCaseMgrQueue(nbaLob.getLicCaseMgrQueue()); //NBLXA-2487
		aWorkItem.getNbaLob().setFirstName(nbaLob.getFirstName());
		aWorkItem.getNbaLob().setLastName(nbaLob.getLastName());
		aWorkItem.getNbaLob().setSsnTin(nbaLob.getSsnTin());
		aWorkItem.getNbaLob().setDOB(nbaLob.getDOB());
		aWorkItem.getNbaLob().setAgentID(nbaLob.getAgentID());
		aWorkItem.getNbaLob().setFaceAmount(nbaLob.getFaceAmount());
		aWorkItem.setStatus(statusProvider.getPassStatus());
		aWorkItem.setUpdate();
		updateWork(getUser(), aWorkItem);
		endedTransaction = aWorkItem.getNbaTransaction();
		unlockWork(getUser(), aWorkItem);
		
		
	}
	
	protected NbaSearchVO searchAgentLicesingWI(NbaTXLife nbaTXLife,NbaUserVO user) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		String contractKey = null;
		if (nbaTXLife != null && nbaTXLife.getPolicy() != null && nbaTXLife.getPolicy().getPolNumber() != null) {
			contractKey = nbaTXLife.getPolicy().getPolNumber();
		}
		if (contractKey != null) {
			searchVO.setWorkType(NbaConstants.A_WT_AGENT_LICENSE);
			searchVO.setContractNumber(contractKey);
			searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
			searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		}
		return searchVO;
	}
	protected void retrieveLicWorkItem(List searchResultList) {
		for (int i = 0; i < searchResultList.size();i++) {
			searchResultForLicWIVO = (NbaSearchResultVO) searchResultList.get(i);
				licensingworkExists = true;
				if (searchResultForLicWIVO.getQueue().equalsIgnoreCase(END_QUEUE)) {
					licensingworkInEndQueue = true;
				}
				break;
			}
		}
	

	 // Begin NBLXA-1379
	public void processUnSuspendEvent(NbaUserVO nbaUserVO, NbaDst dst) throws NbaBaseException {
		NbaSearchVO searchVO = searchWINotInEnd(nbaUserVO, dst,A_WT_CONT_PRINT_EXTRACT); //NBLXA-1716
		if ((searchVO == null) || (searchVO != null && searchVO.getSearchResults() == null)
				|| (searchVO != null && searchVO.getSearchResults() != null && searchVO.getSearchResults().isEmpty())) {
			List resultData = getWorkToUnSuspend(dst);
			if (!resultData.isEmpty()) {
				String queue = (String) resultData.get(0);
				if (!NbaUtils.isBlankOrNull(queue)) {
					NbaSearchVO searchWI = new NbaSearchVO();
					searchWI.setContractNumber(dst.getNbaLob().getPolicyNumber());
					searchWI.setQueue(queue);
					searchWI.setSuspendedFlag(YES_VALUE);
					searchWI.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
					searchWI = lookupWork(nbaUserVO, searchWI);
					if (searchWI.getSearchResults().size() > 0) {
						NbaSearchResultVO resultVO = (NbaSearchResultVO) searchWI.getSearchResults().get(0);
						NbaSuspendVO suspendVO = new NbaSuspendVO();
						if (resultVO.isCase()) {
							suspendVO.setCaseID(resultVO.getWorkItemID());
						} else {
							suspendVO.setTransactionID(resultVO.getWorkItemID());
						}
						unsuspendWork(suspendVO);
					}

				}

			}

		}

	}
		
		
	// Searches List of W.I not in End Queue NBLXA-1716
	public NbaSearchVO searchWINotInEnd(NbaUserVO nbaUserVO, NbaDst dst,String workType) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(workType);
		searchVO.setContractNumber(dst.getNbaLob().getPolicyNumber());
		searchVO.setOperand(NOTEQUALOPERATOR);
		searchVO.setQueue(END);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = lookupWork(nbaUserVO, searchVO);
		return searchVO;
	}	
	    
		
	protected List getWorkToUnSuspend(NbaDst dst) throws NbaBaseException {
		NbaVpmsAdaptor vpmsAdaptor = null;
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess(dst.getNbaLob());
			Map deOinkMap = new HashMap();
			deOinkMap.put(A_PROCESS_ID,PROC_PSTISS);
			vpmsAdaptor = new NbaVpmsAdaptor(NbaVpmsAdaptor.WORKITEMIDENTIFICATION);
			vpmsAdaptor.setVpmsEntryPoint(EP_GETWORK_TO_UNSUSPEND);
			vpmsAdaptor.setSkipAttributesMap(deOinkMap);
			vpmsAdaptor.setOinkSurrogate(data);
			VpmsComputeResult rulesProxyResult = vpmsAdaptor.getResults();
			if (!rulesProxyResult.isError()) {
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
				List resultData = vpmsResultsData.getResultsData();
				if (!resultData.isEmpty()) {
					return resultData;
				}
			}
			return null;

		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("problem", re);
		} finally {
			if (vpmsAdaptor != null) {
				try {
					vpmsAdaptor.remove();
				} catch (RemoteException e) {
					getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}

		}

	}
		
	// End NBLXA-1379	
	
	// Begin NBLXA-1722
	public NbaSearchVO searchWI(NbaUserVO nbaUserVO, NbaDst dst,String workType) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(workType);
		searchVO.setContractNumber(dst.getNbaLob().getPolicyNumber());
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = lookupWork(nbaUserVO, searchVO);
		return searchVO;
	}	
	// End NBLXA-1722

	/**
	 * @return the parent
	 */
	public NbaDst getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(NbaDst parent) {
		this.parent = parent;
	}
	
	/**
	 * This method is used to retrive the cases and souces from workItem
	 * 
	 * @return NbaDst work Item
	 * @throws NbaBaseException
	 */
	//NBLXA-2114 new Method
	protected NbaDst retrieveCase() throws NbaBaseException {
		//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false); //Retrieve the Case
		retOpt.requestCaseAsParent();
		retOpt.requestSources(); //Retrieve the Sources
		retOpt.requestTransactionAsSibling(); //ALS5296
	return retrieveWorkItem(getUser(), retOpt);  //NBA213
		//NBA213 deleted code
	}
	
	/**
	 * NBLXA-2184 New Method This method retrieves the list of application sources attached with work item
	 * 
	 * @param work
	 *            A work item to be proccessed
	 * @return List of sources
	 * @throws NbaBaseException
	 */
	protected List getApplicationSourceInfo(NbaDst work) throws NbaBaseException {
		List forms = new ArrayList();
		List transactions = work.getNbaTransactions();
		String formNumber;
		for (int i = 0; i < transactions.size(); i++) {
			NbaTransaction transaction = (NbaTransaction) transactions.get(i);
			List sources = transaction.getNbaSources();
			for (int j = 0; j < sources.size(); j++) {
				NbaSource aSource = (NbaSource) sources.get(j);
				String sType = aSource.getSourceType();

				if (sType.equals(NbaConstants.A_ST_MISC_MAIL) || sType.equals(NbaConstants.A_ST_FORMS)) {
					formNumber = aSource.getNbaLob().getFormNumber();
					if (formNumber != null && formNumber.length() > 0) {
						forms.add(formNumber);
					}
				}
			}
		}
		List caseSources = work.getNbaSources();
		for (int i = 0; i < caseSources.size(); i++) {
			NbaSource source = (NbaSource) caseSources.get(i);
			formNumber = source.getNbaLob().getFormNumber();
			if (formNumber != null && formNumber.length() > 0) {
				forms.add(formNumber);
			}
		}
		return forms;
	}

	/**
	 * Return the System for the user
	 */
	// APSL5055-NBA331 New Method
	protected String getSystemForUser() {
		UserSessionController userSession = ServiceContext.currentContext().getUserSession();
		if (userSession != null) {
			Result systemSessionResult = userSession.getSystem(AWDNETSERVER);
			if (!systemSessionResult.hasErrors()) {
				return AWDNETSERVER;
			}
			systemSessionResult = userSession.getSystem(WORK_TRACKING);
			if (!systemSessionResult.hasErrors()) {
				return WORK_TRACKING;
			}
		}
		return AWDREST;
	}

	
	
	// NBLXA-2114 new Method
	protected byte[] generateCorrespondenceLetter(String letterName) throws NbaBaseException {
		try {
			NbaCorrespondenceAdapter adapter = new NbaCorrespondenceAdapterFactory().getAdapterInstance();
			adapter.initializeObjects(getWork(), getUser(), getNbaTxLife());
			adapter.setLetterType(NbaCorrespondenceUtils.LETTER_EVENTDRIVEN);
			return adapter.getLetterAsPDF(letterName, null);
		} catch (NbaBaseException e) {
			getLogger().logError("Error in generating the reinsurance correspondence letter " + e.getMessage());
			throw new NbaBaseException(e, NbaExceptionType.FATAL);
		}
	}
	
	// NBLXA-2114 new Method
	protected void saveReinsuranceLetterAsSource(NbaDst work, byte[] letter, String sourceType, String letterName, ReinsuranceOffer reinoffer)
			throws NbaBaseException {// TODO verify lob values to
		NbaTransaction aTransaction = null;
		String encodeFinalImage = NbaBase64.encodeBytes(letter);
		if (encodeFinalImage != null) {
			NbaParty party = getNbaTxLife().getParty(reinoffer.getPartyID()); // NBLXA-2114
			WorkItemSource newWorkItemSource = new WorkItemSource();
			newWorkItemSource.setCreate("Y");
			newWorkItemSource.setRelate("Y");
			newWorkItemSource.setLobData(new ArrayList());
			newWorkItemSource.setBusinessArea(A_BA_NBA);
			newWorkItemSource.setSourceType(sourceType);
			newWorkItemSource.setSize(0);
			newWorkItemSource.setPages(1);
			LobData newLob1 = new LobData();
			newLob1.setDataName(NbaLob.A_LOB_DISTRIBUTION_CHANNEL);
			newLob1.setDataValue(Long.toString(getWorkLobs().getDistChannel()));
			newWorkItemSource.getLobData().add(newLob1);
			LobData newLob2 = new LobData();
			newLob2.setDataName(NbaLob.A_LOB_LETTER_TYPE); // NBXA-2114
			newLob2.setDataValue(letterName); // NBXA-2114
			newWorkItemSource.getLobData().add(newLob2);
			LobData newLob3 = new LobData();
			newLob3.setDataName(NbaLob.A_LOB_POLICY_NUMBER);
			newLob3.setDataValue(getWorkLobs().getPolicyNumber());
			newWorkItemSource.getLobData().add(newLob3);
			LobData newLob4 = new LobData();
			newLob4.setDataName(NbaLob.A_LOB_REIN_VENDOR_ID); // NBXA-2114
			newLob4.setDataValue(party.getOrganization().getOrgCode()); // NBXA-2114
			newWorkItemSource.getLobData().add(newLob4); // NBXA-2114
			LobData newLob5 = new LobData();
			newLob5.setDataName(NbaLob.A_LOB_UNDERWRITER_ACTION);
			newLob5.setDataValue(getWork().getNbaLob().getUnderwriterActionLob());
			newWorkItemSource.getLobData().add(newLob5);
			newWorkItemSource.setText(NbaUtils.getGUID());
			newWorkItemSource.setFileName(null);
			newWorkItemSource.setFormat(NbaConstants.A_SOURCE_IMAGE);
	//		newWorkItemSource.setSourceStream(encodeFinalImage);
			newWorkItemSource.setSourceStream(letter); // NBLXA-2264 64 encodeBytes is not required for Restful
			Map deOink = new HashMap();
			deOink.put("A_CreatreReinCorrWI", "true"); // NBXA-2114
			NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), nbaTxLife, deOink);
			aTransaction = getWork().addTransaction(workProvider.getWorkType(), workProvider.getInitialStatus());
			aTransaction.getNbaLob().setLetterType(letterName); // NBXA-2114
			if (reinoffer.getAcceptRejectCode() == NbaConstants.REINSURANCE_REJECT_TRUE_CODE) {
				aTransaction.getNbaLob().setUnderwriterActionLob(NbaOliConstants.OLI_REJECT_ACTION);
			} else if (reinoffer.getAcceptRejectCode() == NbaConstants.REINSURANCE_REJECT_FALSE_CODE) {
				aTransaction.getNbaLob().setUnderwriterActionLob(NbaOliConstants.OLI_ACCEPT_ACTION);
			} // NBLXA-2114 setting file name for accept and reject offer for GenRe
			aTransaction.getNbaLob().setReinVendorID(party.getOrganization().getOrgCode()); // NBXA-2114
			aTransaction.getSources().add(newWorkItemSource);
		} else {
			addComment("No Image returned from xPressions ");
		}
	}

	// NBLXA-2114 new Method
	public boolean generateCorrospondance() throws NbaBaseException {
		boolean txLifeUpdated = true;
		List listOfPartyIds = new ArrayList();
		Coverage cov = getNbaTxLife().getPrimaryCoverage();
		CoverageExtension covExt = NbaUtils.getFirstCoverageExtension(cov);
		List ReiOffer = covExt.getReinsuranceOffer();
		if (ReiOffer.size() > 0) {
			txLifeUpdated = true;
			Iterator ReiOfferitr = ReiOffer.iterator();
			while (ReiOfferitr.hasNext()) {
				ReinsuranceOffer reinoffer = (ReinsuranceOffer) ReiOfferitr.next();
				NbaParty party = getNbaTxLife().getParty(reinoffer.getPartyID());
				String PartyCode = party.getOrganization().getOrgCode();
				if (reinoffer != null && !(NbaConstants.PROC_POSTMANUALAPPROVAL).equalsIgnoreCase(reinoffer.getSourceID())
						&& !(NbaConstants.PROC_FINAL_DISPOSITION).equalsIgnoreCase(reinoffer.getSourceID()) && !PartyCode.equalsIgnoreCase(NbaConstants.SR_TR)
						&& !PartyCode.equalsIgnoreCase(NbaConstants.RGA_ASAP) && !listOfPartyIds.contains(reinoffer.getPartyID())) {
					if (user.getUserID().equalsIgnoreCase(NbaConstants.PROC_FINAL_DISPOSITION)) {
						reinoffer.setSourceID(NbaConfigurationConstants.Negative_Dispose);
					}
					listOfPartyIds.add(reinoffer.getPartyID());
					String letterName = NbaConfiguration.getInstance()
							.getBusinessRulesAttributeValue(NbaConfigurationConstants.REINSURANCE_ACCEPT_REJECT_LETTER);
					byte[] letter = generateCorrespondenceLetter(letterName);
					saveReinsuranceLetterAsSource(work, letter, A_ST_CORRESPONDENCE_LETTER, letterName, reinoffer);
				}
				reinoffer.setSourceID(user.getUserID());
			}

		}
		return txLifeUpdated;
	}
}