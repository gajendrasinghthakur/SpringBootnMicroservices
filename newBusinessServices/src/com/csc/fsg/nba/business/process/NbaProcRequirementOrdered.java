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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapterFacade;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaRetrieveCommentsRequest;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaTransactionSearchResultVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.configuration.Company;
import com.csc.fsg.nba.vo.nbaschema.AutomatedProcess;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Carrier;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.FormInstanceExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeStyleActivity;
import com.csc.fsg.nba.vo.txlife.MIBRequest;
import com.csc.fsg.nba.vo.txlife.MIBServiceDescriptor;
import com.csc.fsg.nba.vo.txlife.MIBServiceDescriptorOrMIBServiceConfigurationID;
import com.csc.fsg.nba.vo.txlife.MIBServiceOptions;
import com.csc.fsg.nba.vo.txlife.MedicalCertification;
import com.csc.fsg.nba.vo.txlife.MedicalExam;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.RequirementMessage;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.RiskExtension;
import com.csc.fsg.nba.vo.txlife.TentativeDisp;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.ResultData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * NbaProcRequirementOrdered attempts to match ordered requirements with requirement
 * results received directly from the provider or from a manual source.
 * <p>Two types of requirements are handled by this process:
 * <ul><li>Requirement work items that have been ordered but have no source
 * <li>Temporary work items whose source needs to be associated with a
 * requirement work item.
 * </ul>
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>SPR1359</td><td>Version 3</td><td>Automated processes stop poller when unable to lock supplementary work items</td></tr>
 * <tr><td>SPR1784</td><td>Version 4</td><td>The Temporary Requirement Work Item moves to NBEND queue even when there is no Requirement Work Item.</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>NBA097</td><td>Version 4</td><td>Work Routing Reason Displayed</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>nbA 121 and 1122 General Requirement Migration</td></tr>
 * <tr><td>ACN020</td><td>Version 4</td><td>VP/MS Model-Modify Requirements Matching Process</td></tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>MIB 401/402 Migration</td></tr>
 * <tr><td>SPR1601</td><td>Version 4</td><td>Update OLI_LU_REQSTAT to 2.8.90</td></tr>
 * <tr><td>ACP016</td><td>Version 4</td><td>Aviation Evaluation</td></tr>
 * <tr><td>ACN020</td><td>Version 4</td><td>Requirement matching</td></tr>
 * <tr><td>NBA080</td><td>Version 4</td><td>Unsolicited Unmatched Mail</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr>
 * <tr><td>ACN026</td><td>Version 5</td><td>Receiving Different Requirement And Electronic Unsolicited Mail</td></tr>
 * <tr><td>SPR2244</td><td>Version 5</td><td>Spec changes for NBA080</td></tr>
 * <tr><td>SPR2565</td><td>Version 5</td><td>ORDERD process is not unsuspending the lower level requirements automatically when higher level requirement result is received.</td></tr>
 * <tr><td>SPR2636</td><td>Version 5</td><td>MISCMAIL Not Setting SYST LOB - Comments Will Not Open</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR2778</td><td>Version 5</td><td>NBREQRMNT work items created in NBORDERD are erroring in NBPSTREQ automated with the status HOSTERRD</td></tr>
 * <tr><td>SPR2803</td><td>Version 5</td><td>Requirements matching errors when requirement unique ID and or Policy Number is not included in the requirement results</td></tr>
 * <tr><td>SPR2917</td><td>Version 6</td><td>Image is displayed when Actions View Images or View All Images are selected for a NBTEMPREQ (XML 1122 with Images is receipted) work item in end queue</td></tr>
 * <tr><td>SPR2385</td><td>Version 6</td><td>Requirement result is not getting stored in the database for the X-Referenced work item when the requirement result is received for the original requirement work item.</td></tr>
 * <tr><td>SPR3104</td><td>Version 6</td><td>APORDERD poller error stops when misc mail work item receipts two cross referenced APS requirements on the same policy for an insured.</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>SPR3160</td><td>Version 6</td><td>Requirement Evaluation is expecting Requirement Results attachment to be OLI_LU_BASICATTACHMENTTYP(271)  instead of OLI_LU_BASICATTMNTTY_TEXT (1)</td></tr>
 * <tr><td>SPR2697</td><td>Version 6</td><td>Requirement Matching Criteria Needs to Be Expanded</td></tr>
 * <tr><td>SPR3129</td><td>Version 6</td><td>Contract Lock problems</td></tr>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * <tr><td>SPR3237</td><td>Version 6</td><td>After Indexing the MISCMAIL workitem is going to Case Manager queue instead of Case Manager Annuity New Business for annuity case</td></tr>
 * <tr><td>SPR3301</td><td>Version 7</td><td>Changes to processing for work items matched to a Case</td></tr>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>
 * <tr><td>NBA211</td><td>Version 7</td><td>Partial Application</td></tr>
 * <tr><td>NBA212</td><td>Version 7</td><td>Content Services</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Deferred Work Item retrieval</td></tr>
 * <tr><td>AXAL3.7.01</td><td>AxaLife Phase 1</td><td>Scan and Indexing UI</td></tr>
 * <tr><td>AXAL3.7.06</td><td>AxaLife Phase 1</td><td>Requirements Management</td></tr>
 * <tr><td>AXAL3.7.22</td><td>AxaLife Phase 1</td><td>Compensation Interface</td></tr>
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td></tr>
 * <tr><td>ALS2136,ALS2175</td><td>AxaLife Phase 1</td><td>Scanned and indexed 1 piece of misc mail to a case in "Offer Pending Acceptance" status and two requirement work items were created.</td></tr>
 * <tr><td>ALS2204</td><td>AxaLife Phase 1</td><td> Documents Received do not create Requirements when contract number is not entered during MISCMAIL indexing..</td></tr>
 * <tr><td>ALS2280</td><td>AxaLife Phase 1</td><td> Requirement result for Acknowledgement of EOLI requirement is going to error queue..</td></tr>
 * <tr><td>AXAL3.7.20G</td><td>AxaLife Phase 1</td><td>Workflow Gaps</td></tr>
 * <tr><td>ALS2544</td><td>AxaLife Phase 1</td><td>QC# 1352 Ad Hoc: Misc Mail reqt not linked to the image</td></tr>
 * <tr><td>ALS2746</td><td>AxaLife Phase 1</td><td>Reg60 PreSale applications need to default DistributionChannel for downstream processing.</td></tr>
 * <tr><td>ALS2872</td><td>AxaLife Phase 1</td><td>NBMISCMAIL workitem moved to the incorrect queue. </td></tr>
 * <tr><td>ALS3123</td><td>AxaLife Phase 1</td><td>Case matched requirements not evaluating correctly.</td></tr>
 * <tr><td>ALS2814</td><td>AxaLife Phase 1</td><td>QC# 1555 Ad hoc: requirement followup functionality</td></tr>
 * <tr><td>AXAL3.7.20R</td><td>AxaLife Phase 1</td><td>Replacement Processing</td></tr>
 * <tr><td>ALPC153</td><td>AxaLife Phase 1</td><td>Fast Team</td></tr>
 * <tr><td>NBLXA-1656</td><td>Version NB-1501</td><td>nbA Requirement Order Statuses from Third Party Providers</td></tr>
 * <tr><td>ALS2344</td><td>AxaLife Phase 1</td><td> Documents Received do not create Requirements.</td></tr>
 * <tr><td>ALS4494</td><td>AxaLife Phase 1</td><td>QC # 3461 - 3.7.31 mvr not found, multiple mvr rqmts added</td></tr>
 * <tr><td>ALS4398</td><td>AxaLife Phase 1</td><td>QC # 3366 - UAT E2E: Scanned rqmts not matched/routed correctly</td></tr>
 * <tr><td>ALS4937</td><td>AxaLife Phase 1</td><td>QC # 4095 - AXAL03.07.06 Lab Results go to Error when they arrive before an Application is scanned/entered</td></tr>
 * <tr><td>ALS4858</td><td>AxaLife Phase 1</td><td>QC # 4013 - New Presale Case was created when a Definition of Replacement was indexed for a Formal App</td></tr>
 * <tr><td>ALS4995</td><td>AxaLife Phase 1</td><td>AWDERRD status recieved on a NBMISCMAIl from APORDER when it spawning off a NBREQRMNT</td></tr>
 * <tr><td>ALS4650</td><td>AxaLife Phase 1</td><td>QC # 3734 - E2E - new APS indexed with no policy number matches to old case, not new case</td></tr>
 * <tr><td>ALS4966</td><td>AXA Life Phase 1</td><td>QC#4129-AXAL03.07.31 Provider Results for "Joint Life #2" error when Requirements are not posted</td></tr>
 * <tr><td>ALPC234</td><td>AXA Life Phase 1</td><td>Unbound Processing</td></tr>
 * <tr><td>ALS5546</td><td>AXA Life Phase 1</td><td>QC 4746 Provider Results going to Error Queue</td></tr>
 * <tr><td>SR550608</td><td>AXA Life Phase 1</td><td>AXAL3.7.6.33.1 (SR � 550608) Provide ability to match requirement result received as Miscellaneous mail (NBMISCMAIL- ripped or scanned) to the requirement or case of Joint Insured if the miscmail is indexed with Joint Insured data (Joint insured First Name, Last Name, DOB or SSN) when the application exists with Joint Life Case.</td></tr>
 * <tr><td>CR1346708</td><td>Discretionary</td><td></td>Joint Insured</tr>
 * <tr><td>CR1454508(APSL2681)</td><td>Discretionary</td><td>Unsuspend Predictive Case when MIB received</td></tr>\
 * <tr><td>SR679590 (APSL3215)</td><td>Discretionary</td><td>Auto-receive Notice and Consent Form when blood or urine requirement are received for Kansas or Hawaii</td></tr>
 * <tr><td>APSL4872</td><td>Discretionary</td><td>Requirement As Data</td></tr>
 * <tr><td>NBLXA-188</td><td>Discretionary</td><td>Legacy Decommision</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaProcRequirementOrdered extends NbaAutomatedProcess {
	/** An Array list of matching work items */
	private List matchingWorkItems;	//SPR3129
	/** An Array list of matching work items */
	private List matchingCases = new ArrayList(); //NBA080 SPR3129
	/** The NbaProcessStatusProvider object, which provides the status for a given process */
	private NbaProcessStatusProvider wiStatus = null;
	/** Requirement control Soruce object */
	protected com.csc.fsg.nba.vo.NbaSource reqCtrlSrc;  //ALS4937
	/** The Requirement control source xml */
	protected com.csc.fsg.nba.vo.NbaXMLDecorator reqCtlSrcXml; //ALS4937
	//SPR2639 code deleted
	/** The static string representing PROCESS_PROBLEM */
	private final static java.lang.String PROCESS_PROBLEM = "Requirements Ordered problem:";
	/** An Array list of temp work items */
	private List tempWorkItems = new ArrayList();	//SPR3129
	/** An Array list of perm work items */
	private List permWorkItems = new ArrayList();	//SPR3129
	/** An Array list of Cross Referenced work items */
	private List crossReferencedWorkItems = new ArrayList(); // SPR1359 SPR3129
	//SPR2380 removed logger
	/** NbaUserVo */
	private NbaUserVO userVO = null;
	/** Minimum data for AWD lookup presence indicator **/
	private boolean lookupDataFound = false; //SPR2244 SPR2697
	/** An Array list of NbaSuspendVO to unsuspend workitems */
	protected List unsuspendVOs = new ArrayList(); //SPR2565
	/** A set of matching workitem IDs */
	protected Set matchingWorkIds = new HashSet(); //SPR3104
	private Date reqReceiptDate = null;	//NBA130
	private Date reqStatusDate = null;	//NBA130
	private Date labCollectionDate = null; //NBLXA-1794
	protected static final String TRANSACTION = "T";	//SPR2697
	protected static final String CASE = "C";	//SPR2697
	protected static final String TRANSACTION_SEARCH_VO = "NbaTransactionSearchResultVO";	//SPR2697
	protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";	//SPR2697
	protected Map contracts = new HashMap();	//SPR3129
	//ALS4420 code deleted
	protected static final String ACTION_LOCK = "L";  //NBA231
	protected static final String WT_CASE = "C";  //NBA231
	protected boolean hasParentInd = true;  //NBA231
	protected Date reqParamedSignDate = null;	//AXAL3.7.31
	protected static final String MULTIPLE_REQS = "1";	//AXAL3.7.31
	protected static final String SINGLE_REQ = "*";	//AXAL3.7.31
	protected String scenarioCode = null;	//AXAL3.7.31
	protected String reqProvider = null;	//AXAL3.7.31
	protected String reqStatus = null;	//AXAL3.7.31
	protected Map reqCodeTransform = new HashMap();	//AXAL3.7.31
	protected String formNumber; 	//ALS4181
	private List ignoredMatchingCases = new ArrayList(); //ALS5414
	protected static final String POL_NUM_NOT_ASSIGNED = "not assigned";//ALS5632
    //Begin APSL460,APSL464,APSL557
	private List permAppDoneMatchingCases = new ArrayList();
	private List termFormalCases=new ArrayList();
	private List termInFormalCases=new ArrayList();
	//End APSL460,APSL464,APSL557
	private List ignoredCrossReferencedWorkItems = new ArrayList(); //QC12187/APSL3284
	protected static final String requirementForExistingUniqueId = "This requirement was created due to receiving the additional document that was submitted as part of the original requirement";//APSL3643
	private String matchedCriteria = null; //ALII2077
	protected Date reqSignDate = null;	//APSL4872
	protected List sixMonthsOldAppMatchingCases = new ArrayList(); // APSL5335
	protected List twelveMonthsOldAppMatchingCases = new ArrayList(); // NBLXA-2437
	protected static final String WT_TRANSACTION = "T"; //NBLXA-188
	private boolean updateRequired = true; //NBLXA-188
	protected final static String UNKNOWN = "UNKNOWN"; // NBLXA-1714
	protected static final String TXLIFE_START_TAG = "<TXLife"; //NBLXA-1822
	protected static final String TXLIFE_END_TAG = "</TXLife>"; //NBLXA-1822
	private static final String SUSPEND_DAYS_335 = "335"; //NBLXA-2450 NBLXA-2328
	private int actualSuspendDays = 0; //NBLXA-2072 Start
	protected Date deliveryRecepitSignDate = null;	//NBLXA-2133
	private String reqReceiptDateTime = null;	//QC20240
/**
 * NbaProcRequirementOrdered default constructor.
 */
public NbaProcRequirementOrdered() {
	super();
	//ACN014 code deleted
	setContractAccess(UPDATE); //NBA130
}

/**
 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
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
//NBA103 - removed method

/**
 * This method associates the Provider Result source(s) attached to the
 * temporary work item to the requirement work item.  There may be both
 * NBPROVRSLT and NBPROVSUPP sources associated with a work item.
 * @param permWorkItem the permanent work item to which the source(s) is(are) to be added
 * @param tempWorkItem the temporary work item that contains the source(s)
 * @throws NbaBaseException
 */
public void associateSource(NbaDst permWorkItem, NbaDst tempWorkItem) throws NbaBaseException {

	//begin ACN014
	// SPR3290 code deleted
	RequirementInfo requirementInfo = null;
	NbaTXLife holdingInq = null; //NBA208-36
	Attachment attachment = null;
	AttachmentData attachmentData = null;
	//end ACN014
	for (int i = 0; i < tempWorkItem.getNbaSources().size(); i++) {
		NbaSource aSource = (NbaSource) tempWorkItem.getNbaSources().get(i);
//		ALS4052 code deleted
		//begin ACN014
		boolean isNotSubStatus = !(NbaConstants.SUB_STATUS.equalsIgnoreCase(getWork().getNbaLob().getReqSubStatus())); //NBLXA-1656
		boolean isReqAgentResponse =(NbaConstants.REQ_AGENT_CODE.equalsIgnoreCase(getWork().getNbaLob().getReqSubStatus())); //NBLXA-1822
		if (aSource.getSource().getSourceType().equals(NbaConstants.A_ST_PROVIDER_SUPPLEMENT) && (validSource(aSource.getSource()) || !isNotSubStatus)) { //ALS4052 //NBLXA-1656
			//NBA208-36 code deleted
			// BEGIN SPR2803
			tempWorkItem.getNbaLob().setPolicyNumber(permWorkItem.getNbaLob().getPolicyNumber()); //SPR3129
			// END SPR2803
			holdingInq = (NbaTXLife) getContracts().get(permWorkItem.getNbaLob().getPolicyNumber());//SPR2385

			if (getResult() == null || contractLocked(holdingInq)) {
				requirementInfo = getMatchingRequirementInfo(holdingInq, permWorkItem);
				if (requirementInfo != null) {
					NbaOLifEId nbaOLifEId = new NbaOLifEId(holdingInq);
					requirementInfo.setActionUpdate();
					attachment = new Attachment();
					nbaOLifEId.setId(attachment);
					attachment.setActionAdd();
					//ACN009 begin
					if (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MIBCHECK) {
						attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_MIB_SERVRESP);
						//} else if (requirementInfo.getReqCode()==NbaOliConstants.OLI_REQCODE_PHYSSTMT && NbaConstants.SUB_STATUS.equalsIgnoreCase(work.getNbaLob().getReqSubStatus())){ //NBLXA-1656 //NBLXA-1777
					} else if (NbaConstants.SUB_STATUS.equalsIgnoreCase(work.getNbaLob().getReqSubStatus())){ //NBLXA-1656 //NBLXA-1777
						attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_STATUSCHG); //NBLXA-1656
						//attachment.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT); //NBLXA-1656
					} else {
						attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_REQUIRERESULTS);
						 //attachment.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT); //SPR3160 //NBLXA-1656
					}
					//ACN009 end
					determineMVRSuspension(permWorkItem, tempWorkItem, aSource);//NBLXA-2072
					attachment.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT); //NBLXA-1656
					attachment.setDateCreated(new Date());
					attachmentData = new AttachmentData();
					attachmentData.setActionAdd();
					attachmentData.setPCDATA(getSourceText(aSource.getText()));	//AXAL3.7.31
					attachment.setAttachmentData(attachmentData);
					requirementInfo.addAttachment(attachment);
					//ACP016 begin
					if (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_QAVIATION
							|| requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_INSPRPTQUES) {
						addLifeStyleActivity(holdingInq, permWorkItem, attachmentData);
					}
					//ACP016 end
					//ALS4135 begin
					if (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMMD
							|| requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED) {
						addParamedSignDate(holdingInq, permWorkItem, attachmentData);
					}
					//ALS4135 end
					// NBLXA-1822 Start
						if ((!NbaUtils.isBlankOrNull(aSource.getText())) && (isReqAgentResponse)) {
							RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
							insertRequirementMessage(holdingInq, requirementInfoExt, aSource);
						}
					// NBLXA-1822 Ends
					// NBLXA-2072 Begin,NBLXA-2410
					if (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER) {
						String appliesToPartyId = requirementInfo.getAppliesToPartyID();
						if (aSource != null && !NbaUtils.isBlankOrNull(aSource.getText())) {
							copyRiskClassifierSourceResult(aSource, holdingInq, appliesToPartyId);
							copyProductReferenceNumber(aSource, holdingInq, appliesToPartyId);
						}
					}
					// NBLXA-2072 End

					 // NBLXA-2410 Begin
					if (aSource != null && !NbaUtils.isBlankOrNull(aSource.getText())) {
			    	    updateRequirementTrackingIDAndOrderNum(requirementInfo, aSource); // NBLXA-2410
			    	    if(permWorkItem.getNbaLob().getReqVendor().equalsIgnoreCase(NbaConstants.PROVIDER_PRODUCER)) { // NBLXA-2617
			    	      updateVendorAndTrackingProvider(requirementInfo, permWorkItem,tempWorkItem); // NBLXA-2617
			    	    }
					}
					// NBLXA-2410 End
					try {
						holdingInq = NbaContractAccess.doContractUpdate(holdingInq, permWorkItem, userVO); //NBA213
						//ACN020 CODE DELETED
					} catch (NbaBaseException nbe) {
						if (nbe instanceof NbaTransactionValidationException) {
							handleTransactionValidationErrors(nbe.getMessage());
						} else {
							throw nbe;
						}
						//NBA213 deleted code
					}
					//SPR3129 code deleted
					handleHostResponse(holdingInq);
				}
			}

		} else {
			//end ACN014
			//Begin SPR2244
			//NBa208-32
			WorkItemSource source = aSource.getSource();
			if (NbaConstants.A_ST_MISC_MAIL.equalsIgnoreCase(source.getSourceType())) {
				/*source.setSourceType(NbaConstants.A_ST_PROVIDER_RESULT);
				//NBA208-32,NBLXA-188- Code commented
				source.setUpdate("Y"); */
				//Start NBLXA-188
				/** If MISCMAIL of PDR requirement is indexed for GI application,it checks whether MDR Consent is true for this policy,which indicates that
				Master Delivery Reciept needs to be attached on all policies for the batch in which this policy belongs.
				Retrieves releaseBatchId of the policy,then retrieves all policies for this batch and creates NBMISCMAIL workitems
				for all retrieved policies by attaching same source to it,which indexed for the PDR result. */
				holdingInq = (NbaTXLife) getContracts().get(permWorkItem.getNbaLob().getPolicyNumber());
				ApplicationInfo applicationInfo = holdingInq.getPolicy().getApplicationInfo();
				if (getResult() == null || contractLocked(holdingInq)) {
				    if(applicationInfo!=null && applicationInfo.getApplicationType()==NbaOliConstants.OLI_APPTYPE_GROUPAPP && permWorkItem.getNbaLob().getReqType()==NbaOliConstants.OLI_REQCODE_POLDELRECEIPT && NbaUtils.getPolicyExtension(holdingInq.getPolicy()).getMDRConsentIND() && !permWorkItem.getNbaLob().getGIWorkItemInd()){
				    	generatePDRMiscWorkForGIPolicies(holdingInq.getPolicy().getCarrierCode()); //NBLXA-1680
				    }
					if(updateRequired){
						source.setSourceType(NbaConstants.A_ST_PROVIDER_RESULT);
						//NBA208-32
						source.setUpdate("Y");
					}
				//End NBLXA-188
			}
			//End SPR2244
			//BEGIN SPR2803
		}
		} //QC19476
		if (!NbaConstants.A_ST_REQUIREMENT_CONTROL.equalsIgnoreCase(aSource.getSource().getSourceType()) && updateRequired) { //NBLXA-188
				//begin ALS5729
				NbaDst permWorkitemCase = retrieveParentWork(permWorkItem, true, false);
				aSource.getNbaLob().setApplicationType(permWorkitemCase.getNbaLob().getApplicationType());
				aSource.getNbaLob().setReqUniqueID(permWorkItem.getNbaLob().getReqUniqueID());//SR657984/APSL3211
				aSource.setUpdate();
				//end ALS5729

				permWorkItem.getNbaTransaction().addNbaSource(aSource);
				//Begin APSL425
				boolean updateRqd = false;
				updateRqd = mergeMatchingCaseComments(permWorkitemCase.getNbaCase(), retrieveWorkItemComments(tempWorkItem).getNbaTransaction());
				//End APSL425
				//Begin ALS2584
				permWorkitemCase.getNbaCase().getTransactions().clear();//clear transactions so that perm workitem does not get unlocked
				if (NbaVPMSHelper.isSupplementTabForm(permWorkItem.getNbaLob())) { //ALS3828, NA_AXAL004
					copyCaseLobToSource(aSource,permWorkitemCase.getNbaLob());//APSL1076 retrfitted From Prod
					permWorkitemCase.addNbaSource(aSource);
					updateRqd = true;	//APSL425
				}
				//Begin APSL425
				if(updateRqd){
					update(permWorkitemCase);	//APSL425 Code moved outside if block
				}
				//End APSL425
				unlockWork(permWorkitemCase);
				//End ALS2584
				markSourceDisplayable(permWorkItem, requirementInfo, holdingInq, aSource); //NBA208-36
				updateJointInsuredLOB(aSource,(NbaTXLife) getContracts().get(permWorkItem.getNbaLob().getPolicyNumber()));//ALII1674

			}
		// END SPR2803
	}
}

	// NBLXA-2072 New Method


	//NBLXA-2184 New method | UserStory 296825|296826

	public boolean isMVRResponsePresent(String txLifeString) {

		boolean isMVRPresent = false;
		if (!NbaUtils.isBlankOrNull(txLifeString)) {
			NbaTXLife nbatxLifeLocal = null;
			try {
				nbatxLifeLocal = new NbaTXLife(txLifeString);
				List requirementInfo = nbatxLifeLocal.getRequirementInfoList();
				if (requirementInfo != null) {
					Iterator reqInfoIterator = requirementInfo.iterator();
					while (reqInfoIterator.hasNext()) {
						RequirementInfo currentRequirementInfo = (RequirementInfo) reqInfoIterator.next();
						if (currentRequirementInfo != null && NbaOliConstants.OLI_REQCODE_MVRPT == currentRequirementInfo.getReqCode()) {
							isMVRPresent = true;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return isMVRPresent;
	}

	// NBLXA-2072 New Method
	protected NbaTransaction retrieveMVRWorkItem(NbaDst parentWork) throws NbaBaseException {
		NbaTransaction nbaTransactionMVR = null;
		if (parentWork != null) {
			List transactions = parentWork.getNbaTransactions();
			for (int i = 0; transactions != null && i < transactions.size(); i++) {
				NbaTransaction nbaTransaction = (NbaTransaction) transactions.get(i);
				if (nbaTransaction != null && nbaTransaction.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REQUIREMENT)
						&& NbaOliConstants.OLI_REQCODE_MVRPT == nbaTransaction.getNbaLob().getReqType() && nbaTransaction.isSuspended()
						&& NbaConstants.PROVIDER_LEXISNEXIS.equalsIgnoreCase(nbaTransaction.getNbaLob().getReqVendor())
						&& NbaConstants.A_STATUS_REQUIREMENT_ORDERED.equalsIgnoreCase(nbaTransaction.getNbaLob().getStatus())) {

					nbaTransactionMVR = nbaTransaction;

					break;
				}
			}
		}
		return nbaTransactionMVR;
	}



//APSL425 new method
protected NbaDst retrieveWorkItemComments(NbaDst nbaDst) throws NbaBaseException {
	if (getLogger().isDebugEnabled()) {
		getLogger().logDebug("A2ORDERD Starting retrieveWorkItemComments for " + nbaDst.getID());
	}
	AccelResult accelResult = (AccelResult) currentBP.callBusinessService("RetrieveCommentsBP", setRetrievalProperties(nbaDst));
	NewBusinessAccelBP.processResult(accelResult);
	return (NbaDst) accelResult.getFirst();

}

//APSL425 new method
protected NbaRetrieveCommentsRequest setRetrievalProperties(NbaDst work) {
	NbaRetrieveCommentsRequest commentsReq = new NbaRetrieveCommentsRequest();
	commentsReq.setNbaDst(work);
	if (work.isCase()) {
		commentsReq.setRetrieveChildren(true);
	} else {
		commentsReq.setRetrieveChildren(false);
	}

	return commentsReq;
}

/**
 * @param holdingInq
 * @param permWorkItem
 * @param attachmentData
 * @throws NbaBaseException
 */
//ALS4135 new method
public void addParamedSignDate(NbaTXLife holdingInq, NbaDst permWorkItem, AttachmentData attachmentData) throws NbaBaseException{
	Date examDate = null;
	NbaTXLife txLifeReqResult = getReqResult(attachmentData);
	OLifE oLifeReqResult = txLifeReqResult.getOLifE();
	int relationCode = permWorkItem.getNbaLob().getReqPersonCode();
	int personSeq = 0;
	String partyId = txLifeReqResult.getPartyId(relationCode, String.valueOf(personSeq));
	if(partyId != null && !NbaUtils.isBlankOrNull(txLifeReqResult.getParty(partyId))){//APSL5146
	Party insPartyReqResult = txLifeReqResult.getParty(partyId).getParty();
	Risk risk = insPartyReqResult.getRisk();
	//Start NBLXA -1710
	if(!NbaUtils.isBlankOrNull(risk) && risk.getMedicalExamCount()>0 &&
			!NbaUtils.isBlankOrNull(risk.getMedicalExamAt(0))  && !NbaUtils.isBlankOrNull(risk.getMedicalExamAt(0).getExamDate())) {
	examDate = risk.getMedicalExamAt(0).getExamDate();
	}
	//End NBLXA -1710
	Party insPartyHolding = getInsuredParty(permWorkItem,holdingInq);
	Risk riskHolding = insPartyHolding.getRisk();
	riskHolding.setActionUpdate();
	RiskExtension riskExt = NbaUtils.getFirstRiskExtension(riskHolding);
	if (riskExt != null) {
		MedicalCertification medCert = null;
		if ( riskExt.getMedicalCertification().size()>0){
			medCert =(MedicalCertification)riskExt.getMedicalCertificationAt(0);
			medCert.setActionUpdate();
		}else{
			medCert = new MedicalCertification();
			medCert.setActionAdd();
			riskExt.getMedicalCertification().add(medCert);
		}
		if(!NbaUtils.isBlankOrNull(examDate)){// NBLXA-1710
		medCert.setExamDate(examDate);
		}
		//ALII2033 deleted line of code to not generate Exam From Other Comp Req.
		}
	}
}
/**
 * @param holdingInq
 * @param permWorkItem
 * @param attachmentData
 * @throws NbaBaseException
 */
//ALS4611 new method
public void setParamedSignDateInTxlife(NbaTXLife holdingInq, RequirementInfo reqInfo, Date examDate) {
	String partyId = reqInfo.getAppliesToPartyID();
	Party insPartyReqResult = holdingInq.getParty(partyId).getParty();
	Risk risk = insPartyReqResult.getRisk();
	if ( risk !=null){
		if (risk.getMedicalExamCount()>0){
			risk.getMedicalExamAt(0).setExamDate(examDate);
			risk.getMedicalExamAt(0).setActionUpdate();
		}else{
			MedicalExam medExm = new MedicalExam();
			medExm.setExamDate(examDate);
			medExm.setActionAdd();
			risk.addMedicalExam(medExm);
			risk.setActionUpdate();
		}
	}
}

/**
 * Determines if an incoming source is displayable and updates the requirement info extension
 * @param permWorkItem
 * @param requirementInfo
 * @param holdingInq
 * @param aSource
 * @throws NbaBaseException
 */
//NBA208-36
protected void markSourceDisplayable(NbaDst permWorkItem, RequirementInfo requirementInfo, NbaTXLife holdingInq, NbaSource aSource) throws NbaBaseException {
	if (NbaRequirementUtils.isSourceDisplayable(aSource.getSourceType(),getWork())) {//ALS4420
			if (holdingInq == null) {
				holdingInq = (NbaTXLife) getContracts().get(permWorkItem.getNbaLob().getPolicyNumber());
			}
			if (holdingInq != null) {
				if (requirementInfo == null) {
					requirementInfo = getMatchingRequirementInfo(holdingInq, permWorkItem);
				}
				if (requirementInfo != null) {
					RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
					if (requirementInfoExt == null) {
						OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
						requirementInfoExt = olifeExt.getRequirementInfoExtension();
					}
					requirementInfoExt.setDisplayImagesInd(true);
                    // Begin AXAL3.7.01
					NbaLob lob = getWork().getNbaLob();
					if (requirementInfoExt != null) {
						if (lob != null) {
							requirementInfoExt.setDeliveryReceiptSignDate(lob.getDeliveryReceiptSignDate());
							requirementInfoExt.setParamedSignedDate(lob.getParamedSignDate());
							requirementInfoExt.setPremiumDueCarrierReceiptDate(lob.getReqReceiptDate());
							requirementInfoExt.setSignDate(lob.getReqSignDate());//APSL4872
							requirementInfoExt.setLabCollectedDate(lob.getLabCollectionDate());//NBLXA-1794
							requirementInfoExt.setActionUpdate();
						}
					}
					//End AXAL3.7.01
					requirementInfoExt.setActionUpdate();
				}
			}

		}
}

// ACN014 New Method
public boolean contractLocked(NbaTXLife aTXLifeResponse) {
	if (aTXLifeResponse != null && aTXLifeResponse.isTransactionError()) {//NBA094
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify allResponses = aTXLifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		int count = allResponses.getTXLifeResponseCount();
		TransResult transResult = allResponses.getTXLifeResponseAt(count - 1).getTransResult();
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Back End Processing failed", getHostFailStatus()));
		// SPR3290 code deleted
		for (int i = 0; i < transResult.getResultInfoCount(); i++) {
			if( transResult.getResultInfoAt(i).getResultInfoDesc().indexOf(getUser().getUserID()) != -1 ) {
				return true;
			}
		}
	}
	return false;
}
/**
 * This method drives the RequirementOrdered process.  It process both permanent requirement
 * and temporary requirement work items.
 * It starts by retrieving all the sources for the work item being processed and then looks up
 * any matching work items.  If none are found, the work item will be suspended or sent to
 * an error queue.
 * When matching work items are located, they are updated by moving the sources associated
 * with the temporary work item to the permanent work item.  Then all work items are updated
 * moved to an appropriate queue.
 * @param user the user/process for whom the process is being executed
 * @param work a requirement work item, either permanent or temporary, to be matched
 * @return an NbaAutomatedProcessResult containing information about
 *         the success or failure of the process
 * @throws NbaBaseException
 */
public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
    try {	//SPR3129
    	userVO = user;//ACN014
		// NBA027 - logging code deleted

		if (!initialize(user, work)) {
				return getResult();//NBA050
		}

		//QC #8669 APSL3025
		//If policy number is not available on requirement
		//suspend requirement work item by obtaining number of suspend days from VP/MS
		if((A_WT_REQUIREMENT).equals(work.getWorkType()) && NbaUtils.isBlankOrNull(work.getNbaLob().getPolicyNumber()))
		{
			GregorianCalendar calendar = new GregorianCalendar();
			int suspendDay = getSuspendDays(true);
			if (suspendDay > 0) {
				calendar = new GregorianCalendar();
				calendar.setTime(new Date());
				calendar.add(Calendar.DAY_OF_WEEK, suspendDay);
				Date suspendDate = (calendar.getTime());
				addComment("Suspended awaiting Application to be submitted");
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "SUSPENDED", "SUSPENDED"));
				suspendTransaction(suspendDate);
			}
			return getResult();
		}
		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("RequirementOrdered for contract " + getWork().getNbaLob().getPolicyNumber());
		} // NBA027
		// retrieve the sources for this work item
		//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(getWork().getID(), false);
		retrieveOptionsValueObject.requestSources();
		// APSL5055-NBA331.1 code deleted
		setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject));  //NBA213
		//NBA213 deleted code
		//ACN014 begin
		//NBA080 code deleted
		//ACN014 end
		// call awd to lookup matching items
		//SPR1784 code deleted
		reinitializeFields(user, getWork()); //AXAL3.7.20
		boolean isNotSubStatus  = !(NbaConstants.SUB_STATUS.equalsIgnoreCase(getWork().getNbaLob().getReqSubStatus())); //NBLXA-1656
		if(!(isOmissionRequirementResult() && getWork().getNbaLob().getReqUniqueID() == null) && !isFormWithApp()){//ALS4638,ALNA208,APSL4021
 			lookupWork(); //SPR1784
 			 filterGreaterThanSixMonthsCases(); // APSL5335
		}//ALS4638
		//NBLXA-1924 Starts
			if (getWorkType().equals(A_WT_REQUIREMENT) && !matchingWorkItems.isEmpty()) {
				ListIterator matchingWorks = matchingWorkItems.listIterator();
				while (matchingWorks.hasNext()) {
					NbaDst matchingWorkItem = (NbaDst) matchingWorks.next();
					if (A_WT_TEMP_REQUIREMENT.equalsIgnoreCase(matchingWorkItem.getWorkType())
							&& (((NbaConstants.SUB_STATUS.equalsIgnoreCase(matchingWorkItem.getNbaLob().getReqSubStatus()))) || (NbaConstants.REQ_AGENT_CODE
									.equalsIgnoreCase(matchingWorkItem.getNbaLob().getReqSubStatus())))){
						matchingWorks.remove();
					}
				}
			}
		//NBLXA-1924 Ends
		if (matchingWorkItems.isEmpty()) { // if there are no matching work items found //SPR1784
			//Begin NBA080
			//if a requirement is not matched and is a miscellaneous mail or temp requirement, check for a matching case
			if ((getWorkType().equals(A_WT_MISC_MAIL) || getWorkType().equals(A_WT_TEMP_REQUIREMENT)) && isNotSubStatus) { //ACN026 //NBLXA-1656
				lookupCase();
				 filterGreaterThanSixMonthsCases(); // APSL5335
				if (lookupDataWasFound()) { // SPR2244 SPR2697
							// Begin AXAL3.7.31
							// If this is a generic requirement type, change the req type to one that nbA will recognize
							List reqTypes = determineLookupReqTypes(); // call model to determine alias req types
							if (reqTypes.size() > 0 && !reqTypes.contains(String.valueOf(getWork().getNbaLob().getReqType()))) {
								String sReqType = (String) reqTypes.get(0);
								int iReqType = Integer.parseInt(sReqType);
								getReqCodeTransform().put(String.valueOf(getWork().getNbaLob().getReqType()), sReqType);
								getWork().getNbaLob().setReqType(iReqType);
							}

							// Begin ALS5753
							// Begin APSL460,APSL464,APSL557
							Map deOinkMap = new HashMap();
							filterEndQueueCases();
							boolean duplicateTermFormal = duplicateTermFormalMatched();
							boolean duplicateTermInFormal = duplicateTermInFormalMatched();
							boolean singleTermFormal = singleTermForml();
							boolean singleTermInFormal = singleTermInFormal();
							if (duplicateTermFormal || (!singleTermFormal && duplicateTermInFormal)) {
								processMultipleCaseMatches();// APSL240
								processDuplicateTermCases(deOinkMap);
							} else if (processPermanentApplication()) {
								attachTempRequirement();// End ALS5753
								// End APSL460,APSL464,APSL557
						} else if (getMatchingCases().isEmpty()) { // SPR3129 //ALS4150 //ALS4858
							// begin ACN026
							if (work.getTransaction().getWorkType().equals(A_WT_MISC_MAIL)) {
								processUnmatchedMiscWork();
							} else {
								processUnmatchedWorkitem();

							}
							// end ACN026
							// begin ALS4650
						} else if (getMatchingCases().size() > 1) {
								processMultipleCaseMatches();
								changeStatus(getFailStatus());
								doUpdateWorkItem();
								setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getFailStatus(), getFailStatus()));
								// end ALS4650
							} else {
								// begin SPR3129
								retrieveContract((NbaDst) getMatchingCases().get(0));
								if (getResult() == null) {
									processWorkForMatchingCase(); // ACN026
									// Begin AXAL3.7.31
								} else {
									// Begin ALS2814
									// Begin ALS5546
									if (work.getTransaction().getWorkType().equals(A_WT_MISC_MAIL)) {
										processUnmatchedMiscWork();
									} else {
										processUnmatchedWorkitem();
									}
									// end ALS5546
									// End ALS2814
									// End AXAL3.7.31
								}
								// end SPR3129
							}
							// begin SPR2244
						} else {
							changeStatus(getFailStatus());
							addComment("Minimum data for AWD lookup not present");
							doUpdateWorkItem();
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getFailStatus(), getFailStatus()));
						}

				//end SPR2244
			}else{
            	if (isNotSubStatus) { //NBLXA-1656
            		//Otherwise suspend or route the work item status to upa failed status depending on a VPMS model
            		setNbaTxLife((NbaTXLife) getContracts().get(getWork().getNbaLob().getPolicyNumber()));  //SPRNBA-547 get the parent case for the current requirement we are processing)
            	} //NBLXA-1656
				processUnmatchedWorkitem();
			}
			if (result == null) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "SUSPENDED", "SUSPENDED"));
			}
			return result;
			//End NBA080
		}
		//SPR1784 comments deleted
		//begin SPR3129
		retrieveContractsForMatchingWorkItems();
		if (getResult() == null) {
		    processMatchingWorkItems(); //SPR1784
		}
		//end SPR3129
		if( getResult()== null) { // SPR2803
			// SPR1359 BEGIN
			//SPR3129 code deleted
        	if (isNotSubStatus) { //NBLXA-1656
			updateCrossRefWorkItems();
        	} //NBLXA-1656
			//SPR3129 code deleted
			// SPR1359 END
			if (getResult() == null && updateRequired) { //SPR3129,NBLXA-188
			    if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Updating Miscmail and Requirement WorkItem");
	    		    }
                if (isNotSubStatus) { //NBLXA-1656
			    updateRequirementWorkItems();
                } //NBLXA-1656
			    updateTempWorkItems(); //NBLXA-1551
			} //SPR3129
		} //NBA130
		if( getResult() == null) { //NBA130
			if (isNotSubStatus) { //NBLXA-1656
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
			} else { //NBLXA-1656 suspend the temp WI if matching req not found
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "SUSPENDED", "SUSPENDED")); //NBLXA-1656
			} //NBLXA-1656
			//SPR1851 code deleted
		// BEGIN SPR2803
		} else {
			//SPR3129 code deleted
			if (isNotSubStatus) { //NBLXA-1656
			changeStatus(getResult().getStatus());
			} //NBLXA-1656
			doUpdateWorkItem();
		}
		// END SPR2803
		return result;
	//begin SPR3129
    } finally {
        NbaContractLock.removeLock(getUser());
        unlockWorkItems();
        unlockIgnoredMatchingCases(); //ALS5414
    }
    //end SPR3129
}


	//ALS4858 new method
	private boolean matchingPreSaleCase(NbaDst aCase) {
		String appType = aCase.getNbaLob().getApplicationType();
		if (String.valueOf(NbaOliConstants.OLI_APPTYPE_TRIAL).equalsIgnoreCase(appType)) {
			return true;
		}
		return false;
	}

/**
 * This method associates the Provider Result source(s) attached to the
 * temporary work item to the requirement work item.  There may be both
 * NBPROVRSLT and NBPROVSUPP sources associated with a work item.
 * @param permWorkItem the permanent work item to which the source(s) is(are) to be added
 * @param tempWorkItem the temporary work item that contains the source(s)
 * @throws NbaBaseException
 */
//ACN014 New method
public RequirementInfo getMatchingRequirementInfo(NbaTXLife aContract, NbaDst permWorkItem) throws NbaBaseException {
	String reqUniqueId = "";
	if( permWorkItem.getNbaLob().getReqUniqueID() != null && permWorkItem.getNbaLob().getReqUniqueID() != "" ) {
		reqUniqueId = permWorkItem.getNbaLob().getReqUniqueID();
	}
	String reqInfoId = "";
	if( getReqCtlSrcXml() != null) {
		if (getReqCtlSrcXml().getRequirement().getRequirementInfoId() != null && getReqCtlSrcXml().getRequirement().getRequirementInfoId().length() > 0) {
			reqInfoId = getReqCtlSrcXml().getRequirement().getRequirementInfoId();
		}
	}
	int count = aContract.getPrimaryHolding().getPolicy().getRequirementInfoCount();
	for( int i = 0; i < count; i++ ) {
		RequirementInfo reqInfo = aContract.getPrimaryHolding().getPolicy().getRequirementInfoAt(i);
		if( reqInfo.getRequirementInfoUniqueID() != null) {
			if( reqInfo.getRequirementInfoUniqueID().equals(reqUniqueId)) {
				return reqInfo;
			}
		} else if ( reqInfo.getId().equals(reqInfoId) ) {
			return reqInfo;
		}
	}
	return null;
}
/**
 * This method creates and initializes an <code>NbaVpmsAdaptor</code> object to
 * call VP/MS to execute the supplied entryPoint.
 * @param entryPoint the entry point to be called in the VP/MS model
 * @return the results from the VP/MS call in the form of an <code>VpmsModelResult</code> object
 * @throws NbaBaseException
 */
//ACN020 Changed signature to return VpmsModelResult
public VpmsModelResult getDataFromVpms(String entryPoint) throws NbaBaseException, NbaVpmsException {
	//SPR1784 code deleted
	return getDataFromVpms(entryPoint,getWork()); //SPR1784
}
/**
 * This method creates and initializes an <code>NbaVpmsAdaptor</code> object to
 * call VP/MS to execute the supplied entryPoint.
 * @param entryPoint the entry point to be called in the VP/MS model
 * @return the results from the VP/MS call in the form of an <code>VpmsModelResult</code> object
 * @throws NbaBaseException
 */
//SPR1784 New Method
//ACN020 Changed signature to return VpmsModelResult
public VpmsModelResult getDataFromVpms(String entryPoint, NbaDst workItem) throws NbaBaseException, NbaVpmsException {
    NbaVpmsAdaptor vpmsProxy = null;
    try {
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(workItem.getNbaLob());
		Map deOink = new HashMap();
		deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
		deOink.put(NbaVpmsAdaptor.A_INSTALLATION, getInstallationType()); //ACN026
		vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS); //SPR3362
		vpmsProxy.setVpmsEntryPoint(entryPoint);
		vpmsProxy.setSkipAttributesMap(deOink);
		VpmsComputeResult compResult = vpmsProxy.getResults();
		NbaVpmsModelResult data = new NbaVpmsModelResult(compResult.getResult()); // SPR3290
		//SPR3362 code deleted
		return data.getVpmsModelResult();  //ACN020
	} catch (java.rmi.RemoteException re) {
		throw new NbaVpmsException(PROCESS_PROBLEM + NbaVpmsException.VPMS_EXCEPTION, re);
	//begin SPR3362
	} finally {
		try {
            if (vpmsProxy != null) {
                vpmsProxy.remove();
            }
        } catch (RemoteException re) {
            getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
        }
	}
	//end SPR3362
}
/**
 * This method creates and initializes an <code>NbaVpmsAdaptor</code> object to
 * call VP/MS to execute the supplied entryPoint.
 * @param entryPoint the entry point to be called in the VP/MS model
 * @return the results from the VP/MS call in the form of an <code>NbaVpmsResultsData</code> object
 * @throws NbaBaseException
 */
//ACN020 New method
public NbaVpmsModelResult getXmlDataFromVpms(String entryPoint) throws NbaBaseException, NbaVpmsException {
    NbaVpmsAdaptor vpmsProxy = null; //SPR3362
    try {
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
		Map deOink = new HashMap();
		deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
		deOink.put(NbaVpmsAdaptor.A_INSTALLATION, getInstallationType()); //ACN026
		// Begin AXAL3.7.31
		deOink.put("A_ScenarioCode", getScenarioCode());
		//Begin ALS4398
		if (!A_WT_REQUIREMENT.equals(getOrigWorkItem().getTransaction().getWorkType())) {
			if (null!= getOrigWorkItem().getNbaLob().getReqVendor()) {
				deOink.put("A_ReqVendorLOB", getOrigWorkItem().getNbaLob().getReqVendor());
			} else {
				deOink.put("A_ReqVendorLOB", "*");
			}
		} else if (getReqProvider() != null) {
			deOink.put("A_ReqVendorLOB", getReqProvider());
		}
		//End ALS4398
		// End AXAL3.7.31
		vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS); //SPR3362
		vpmsProxy.setVpmsEntryPoint(entryPoint);
		vpmsProxy.setSkipAttributesMap(deOink);
		VpmsComputeResult compResult = vpmsProxy.getResults();
		NbaVpmsModelResult data = new NbaVpmsModelResult(compResult.getResult()); // SPR3290
		//SPR3362 code deleted
		return data;
	} catch (java.rmi.RemoteException re) {
		throw new NbaVpmsException(PROCESS_PROBLEM + NbaVpmsException.VPMS_EXCEPTION, re);
	 //begin SPR3362
	} finally {
			try {
			    if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (RemoteException re) {
			    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
	}
	//end SPR3362
}
/**
 * Answers the requirement control source xml
 * @return the NbaXMLDecorator associated with the Requirement Control Source
 */
public com.csc.fsg.nba.vo.NbaXMLDecorator getReqCtlSrcXml() {
	return reqCtlSrcXml;
}
/**
 * Answers the Requirement Control Source for the work item
 * @return the NbaSource that is the Requirement Control Source
 */
public com.csc.fsg.nba.vo.NbaSource getReqCtrlSrc() {
	return reqCtrlSrc;
}
/**
 * Answers the work item status member
 * @return the work item statuses in the form of an NbaProcessStatusProvider object
 */
public NbaProcessStatusProvider getWiStatus() {
	return wiStatus;
}

    /**
     * Search for Transaction work items which can be matched to the current work item.
     * @return the search value object containing the results of the search
     * @throws NbaBaseException
     */
    protected NbaSearchVO lookupWork() throws NbaBaseException { //SPR2697 changed method visibility
        getLogger().logDebug("Performing lookupWork()");//SPR2697
        return lookup(TRANSACTION);//SPR2697
    }

    /**
     * Search for Case work items which can be matched to the current work item.
     * @return the search value object containing the results of the search
     * @throws NbaBaseException
     */
    //NBA080 New Method
    protected NbaSearchVO lookupCase() throws NbaBaseException { //SPR2697 changed method visibility
        getLogger().logDebug("Performing lookupCase()");//SPR2697
        return lookup(CASE);//SPR2697
    }
/**
 * This method process each work item by associating the source from the temp work item to the
 * permanent work item and then updating any cross-referenced work items.
 * @throws NbaBaseException NbaLockException
 */
//SPR1784 removed method parameter searchResults
public void processMatchingWorkItems() throws NbaBaseException {
		//SPR1784 code deleted
		// all matching work items have been retrieved
		//SPR3129 code deleted
		for (int i = 0; i < getPermWorkItems().size(); i++) {	//SPR3129
				NbaDst perm = (NbaDst) getPermWorkItems().get(i);	//SPR3129
				for (int j = 0; j < getTempWorkItems().size(); j++) {	//SPR3129
					//Begin NBA130
					NbaDst temp = (NbaDst) getTempWorkItems().get(j);	//SPR3129
					if( null == getReqReceiptDate() ) {
						if( null == temp.getNbaLob().getReqReceiptDate()) {
							setReqReceiptDate(new Date());
							setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
						} else {
							setReqReceiptDate(temp.getNbaLob().getReqReceiptDate());
							setReqReceiptDateTime(temp.getNbaLob().getReqReceiptDateTime());//QC20240
						}
					}
					//End NBA130
					// Begin AXAL3.7.31
					if (getReqParamedSignDate() == null) {
						if (temp.getNbaLob().getParamedSignDate() != null) {
							setReqParamedSignDate(temp.getNbaLob().getParamedSignDate());
						}
					}
					//Strat NBLXA-2133
					if (getDeliveryRecepitSignDate() == null) {
						if (temp.getNbaLob().getDeliveryReceiptSignDate() != null) {
							setDeliveryRecepitSignDate(temp.getNbaLob().getDeliveryReceiptSignDate());
						}
					}
					//End NBLXA-2133
					// Begin APSL4872
					if (getReqSignDate() == null) {
						if (temp.getNbaLob().getReqSignDate() != null) {
							setReqSignDate(temp.getNbaLob().getReqSignDate());
						}
					}
					// End APSL4872
					if (getReqProvider() == null) {
						setReqProvider(temp.getNbaLob().getReqVendor());
					}
					// End AXAL3.7.31
					// Begin NBLXA-1794
					if (getLabCollectionDate() == null) {
						if (temp.getNbaLob().getLabCollectionDate() != null) {
							setLabCollectionDate(temp.getNbaLob().getLabCollectionDate());
						}
					}
					//End NBLXA-1794
					//ACN014 Begin
					//Update control source with redundancy check section
					NbaSource source = perm.getRequirementControlSource();
					if (source != null) {
						setReqCtlSrcXml(new NbaXMLDecorator(source.getText()));
					}
					// APSL3405,QC#12555 begin
					if (getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_1009800031 && getWork().getNbaLob().getFormNumber() != null) {
						addFormInstancesForSupp(getWork(), perm ,FORM_NAME_MEDSUP);
					}// APSL3405,QC#12555 end
					//NBLXA-2179 Start
					if (getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_688 && getWork().getNbaLob().getFormNumber()!=null) {
						addFormInstancesForSupp(getWork(),perm,FORM_NAME_FRTS);
					}
					//NBLXA-2179 End
					//ACN014 End
					//NBA130 code deleted
					associateSource(perm, temp);
					//Begin APSL4460
					if(getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_PPR && String.valueOf(NbaConstants.A_WT_MISC_MAIL).equalsIgnoreCase(temp.getNbaLob().getWorkType())) {
						perm.getNbaLob().setReview(NbaConstants.REVIEW_USER_REQUIRED);
					}
					//End APSL4460
					// BEGIN SPR2803
					if (getResult() != null) {
						return;
					}
						// END SPR2803
						// SPR1359 CODE DELETED

				}
				perm.getNbaLob().setFormNumber(getWork().getNbaLob().getFormNumber());

				if(!NbaUtils.isBlankOrNull(getWork().getNbaLob().getProviderOrder())){ //ALII1818, ALII2000
					perm.getNbaLob().setProviderOrder(getWork().getNbaLob().getProviderOrder()); //ALII2000
				}
				copyAgentDetails(perm,getWork()); //NBLXA-2493

				//Begin APSL3707
				if(perm.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_AUTHEFT && String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT).equalsIgnoreCase(getWork().getNbaLob().getPaymentMoneySource())) {
					perm.getNbaLob().setPaymentMoneySource(String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT));
				}
				//End APSL3707
		}
}

/**
 * Updates status and dates for the requirementInfo object
 * @param requirementInfo
 * @throws NbaBaseException
 */
 //NBA130 new method
private void updateRequirementInfoObject(RequirementInfo aReqInfo, String action) throws NbaBaseException {
	boolean isReqAgentResponse =(NbaConstants.REQ_AGENT_CODE.equalsIgnoreCase(getWork().getNbaLob().getReqSubStatus())); //NBLXA-1822
	aReqInfo.setReqStatus(getReqStatus());	//AXAL3.7.31
	aReqInfo.setReceivedDate(getReqReceiptDate());
	aReqInfo.setStatusDate(getReqStatusDate());
	aReqInfo.setFulfilledDate(getReqReceiptDate());
	if (!NbaUtils.isBlankOrNull(getFormNumber())) { //ALS4181
		aReqInfo.setFormNo(getFormNumber());
	}
	//Begin AXAL3.7.01
	RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
	//Begin ALCP234
	if (requirementInfoExt == null) {
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
		requirementInfoExt = olifeExt.getRequirementInfoExtension();
		requirementInfoExt.setActionAdd();
	}

	requirementInfoExt.setReceivedDateTime(getReqReceiptDateTime());//QC20240
		// Begin NBLXA-1822
		NbaSource aSource = (NbaSource) getWork().getNbaSources().get(0);
		if ((!NbaUtils.isBlankOrNull(aSource.getText())) && (isReqAgentResponse)) {
			if (aReqInfo != null) {
				requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
					insertRequirementMessage(nbaTxLife, requirementInfoExt, aSource);

			}
		}
		// Ends NBLXA-1822

		// NBLXA-2072 Begin
		if (aReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER) {
			String appliesToPartyId = aReqInfo.getAppliesToPartyID();
			if (aSource != null && !NbaUtils.isBlankOrNull(aSource.getText())) {
				copyRiskClassifierSourceResult(aSource, nbaTxLife, appliesToPartyId);
				copyProductReferenceNumber(aSource, nbaTxLife, appliesToPartyId);
			}
		}
		// NBLXA-2072 End

		// NBLXA-2410 Begin
  		  NbaSource provSuppSource = null;
		  List<NbaSource> aSources = getWork().getNbaSources();
		  Iterator<NbaSource> sourceIterator = aSources.iterator();
		   while (sourceIterator.hasNext()) {
			NbaSource currentSource = sourceIterator.next();
			if (currentSource.getSourceType().equalsIgnoreCase(NbaConstants.A_ST_PROVIDER_SUPPLEMENT)) {
				provSuppSource = currentSource;
				break;
			}
		  }
		   updateRequirementTrackingIDAndOrderNum(aReqInfo, provSuppSource);
		// NBLXA-2410 End

	if (NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED == aReqInfo.getReqCode()) {
		requirementInfoExt.setParamedSignedDate(getReqParamedSignDate());
	}

	//NBLXA-1794 Begin
	if ((NbaOliConstants.OLI_REQCODE_BLOOD == aReqInfo.getReqCode() || NbaOliConstants.OLI_REQCODE_URINE == aReqInfo.getReqCode()
			|| NbaOliConstants.OLI_REQCODE_VITALS == aReqInfo.getReqCode()) && getLabCollectionDate() != null) {
		requirementInfoExt.setLabCollectedDate(getLabCollectionDate());
	}
	//NBLXA-1794 End

	//NBLXA-2133 Begin
	if ((NbaOliConstants.OLI_REQCODE_POLDELRECEIPT == aReqInfo.getReqCode()) &&  getDeliveryRecepitSignDate()!= null) {
		requirementInfoExt.setDeliveryReceiptSignDate(getDeliveryRecepitSignDate());
	}
	//NBLXA-2133 End

	//APSL4872::Start
	if (NbaOliConstants.OLI_REQCODE_MEDFME == aReqInfo.getReqCode()) {
		requirementInfoExt.setSignDate(getReqSignDate());
	}
	//APSL4872:: END

	//Begin APSL3941
	if(getNbaTxLife().isSIApplication() && isPDR(aReqInfo.getReqCode())
			&& PROVIDER_NOTIFICATION.equalsIgnoreCase(getReqProvider()) && ! NbaUtils.isPDR_NIGO(requirementInfoExt.getDeliveryReceiptSignDate(), new Date())){ //NBLXA-2133 added isPDR_NIGO check
		requirementInfoExt.setReviewedInd(true);
		requirementInfoExt.setReviewID(user.getUserID());
		requirementInfoExt.setReviewDate(new Date());
	}
	//end APSL3941

	// APSL4898 Begin :: epolicy requirement should auto review/received when Case is not "Not Taken"
			if(PROVIDER_NOTIFICATION.equalsIgnoreCase(getReqProvider()) && isAutoReviewNeededRequirements(aReqInfo.getReqCode()))
			{
				Policy policy = getNbaTxLife().getPrimaryHolding().getPolicy();
				PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(policy);
			if (policy.getApplicationInfo() != null
					&& (polExt != null && (polExt.getPendingContractStatus().equalsIgnoreCase(NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_0001)
							|| polExt.getPendingContractStatus().equalsIgnoreCase(NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_0010)))) {
					ApplicationInfo appInfo = policy.getApplicationInfo();
					ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
					//Begin NBLXA-1726
					NbaParty nbAParty = null;
					RequirementInfo ncfReqInfo = getNbaTxLife().getRequirementInfo(getWork().getNbaLob().getReqUniqueID());
					if(ncfReqInfo != null && ncfReqInfo.hasAppliesToPartyID()) {
						nbAParty = getNbaTxLife().getParty(ncfReqInfo.getAppliesToPartyID());
					}else {
						nbAParty = getNbaTxLife().getPrimaryParty();
					}
					//End NBLXA-1726
				if (appInfoExt != null && appInfoExt.getReqPolicyDeliverMethod() == NbaOliConstants.OLI_POLDELMETHOD_EMAIL && !(aReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT
						&& NbaUtils.isPDR_NIGO(requirementInfoExt.getDeliveryReceiptSignDate(), new Date()))) { ////NBLXA-2133 added
					if (appInfoExt.getInitialPremiumPaymentForm() != NbaOliConstants.OLI_PAYFORM_EFT
							|| ((appInfoExt.getInitialPremiumPaymentForm() == NbaOliConstants.OLI_PAYFORM_EFT && aReqInfo.getReqCode() != NbaOliConstants.OLI_REQCODE_POLDELRECEIPT)
							|| (aReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT) && !isReqOutstanding(NbaOliConstants.OLI_REQCODE_PREMDUE, nbAParty)) //NBLXA-1726
							) {
						requirementInfoExt.setReviewedInd(true);
						requirementInfoExt.setReviewID(user.getUserID());
						requirementInfoExt.setReviewDate(new Date());

					}
				}
				}
			}
	// APSL4898 End
	requirementInfoExt.setPremiumDueCarrierReceiptDate(getReqReceiptDate());
	if(matchedCriteria != null) { //ALII2077
		requirementInfoExt.setMatchedCriteria(matchedCriteria); //ALII2077
	} //ALII2077
	requirementInfoExt.setActionUpdate();
	//End ALCP234
	//End AXAL3.7.01
	if( action == null ) {
		aReqInfo.getActionIndicator().setUpdate();
	} else {
		aReqInfo.setAction(action);
	}
}

/**
 * This method inserts the Agent Response in the form of the Requirement Message tag to the TxLife
 * @param holdingInq
 * @return the updated parent case
 * @throws NbaBaseException
 */
//NBLXA-1822 New Method
	private void insertRequirementMessage(NbaTXLife holdingInq, RequirementInfoExtension requirementInfoExt, NbaSource aSource) {
		RequirementMessage reqAgentMessage = null;
		int reqInfoStart = 0;
		int reqInfoEnd = 0;
		String reqInfoString = null;
		RequirementInfoExtension requirementInfoExtension = null;
		reqInfoStart = getSourceText(aSource.getText().toString()).indexOf(TXLIFE_START_TAG);
		reqInfoEnd = getSourceText(aSource.getText().toString()).indexOf(TXLIFE_END_TAG) + (TXLIFE_END_TAG).length(); // added 18 to include
		if (reqInfoStart > NbaConstants.LONG_NULL_VALUE) {
		reqInfoString = getSourceText(aSource.getText().toString());
		if (!NbaUtils.isBlankOrNull(reqInfoString)) {
			NbaTXLife nbatxLifeLocal = null;
			try {
				nbatxLifeLocal = new NbaTXLife(reqInfoString);
				System.out.println("Printing Source XML " + nbatxLifeLocal.toXmlString());
					if (nbatxLifeLocal.getPolicy() != null && nbatxLifeLocal.getPolicy().getRequirementInfoAt(0) != null) {
					requirementInfoExtension = NbaUtils.getFirstRequirementInfoExtension(nbatxLifeLocal.getPolicy().getRequirementInfoAt(0));
						if (requirementInfoExtension != null)  {
						reqAgentMessage = new RequirementMessage();
						NbaOLifEId olifeId = new NbaOLifEId(holdingInq);
						olifeId.setId(reqAgentMessage);
						RequirementMessage reqInfoFromAttachment = requirementInfoExtension.getRequirementMessageAt(0);
						reqAgentMessage.setSenderName(reqInfoFromAttachment.getSenderName());
						reqAgentMessage.setMessageText(reqInfoFromAttachment.getMessageText());
						reqAgentMessage.setMessageDateTime(reqInfoFromAttachment.getMessageDateTime());
						reqAgentMessage.setActionAdd();
						  System.out.println("Printing the Requirement Message" + reqAgentMessage.getMessageDateTime() + "" +
						  reqAgentMessage.getSenderName() + "" + reqAgentMessage.getMessageText());
						if (requirementInfoExt == null) {
							OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
							requirementInfoExt = olifeExt.getRequirementInfoExtension();
							requirementInfoExt.setActionAdd();
						} else {
							requirementInfoExt.setActionUpdate();
						}
							ArrayList reqMessageList = requirementInfoExt.getRequirementMessage();
							if (reqMessageList == null) {
								reqMessageList = new ArrayList();
							}
							reqMessageList.add(reqAgentMessage);
							requirementInfoExt.setRequirementMessage(reqMessageList);

					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}

	}

/**
 * When a temporary requirement (NBTEMPREQ) or miscellaneous mail (NBMISCMAIL) work item cannot be matched to a permanent
 * requirement work item (NBREQRMNT) but can be matched to a Case it will be converted/copied to a permanent requirement NBREQRMNT
 * work item if it has a requirement type and vendor (NBTEMPREQ only) and the person can be located on the Contract.
 *
 * If the work item cannot be converted/copied to a permanent requirement because of missing or invalid information:
 * - for temporary requirements the status of the work item is set the error status.
 * - for miscellaneous mail the work item is added to the case. Its status is set the successful status.
 *
 * If the work item can be converted/copied to a permanent requirement:
 * - for miscellaneous mail a new requirement is created from the Work item
 * - for temporary requirements the Requirements VP/MS model is called to determine if any lower level requirements are defined for
 *   the requirement type of the work item. A lower level requirement is a requirement which is considered a child or subset
 *   of another requirement. If a requirement has lower level requirements, the requirement is a superset of the information
 *   contained in the lower level requirements.
 * 		- If lower level requirements are defined
 * 			- Retrieve all children of the matching case.
 * 			- Convert the temporary requirement to a permanent requirement
 * 			- waive all lower level requirements associated with the Case
 * 		- If no lower level requirements are defined
 * 			- Convert the temporary requirement to a permanent requirement
 * @throws NbaBaseException throw exceptions on failure in netServer calls
 */
//ACN026 New Method
protected void processWorkForMatchingCase() throws NbaBaseException {	//SPR3301 changed method visibility
	NbaDst currentMatch = (NbaDst) getMatchingCases().get(0);
	//begin SPR3301
if (!validateAndUpdateRequiredLobsForRequirement(currentMatch)) {	//cannot be converted/copied to a permanent requirement because of missing or invalid information
		if (A_WT_TEMP_REQUIREMENT.equals(getWorkType())) {
	        //APSL3874 code deleted
	    	throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_REQINFO_MISSING);  //APSL3874
	    }  //APSL3874
	    attachMiscWorkToMatchingCase(currentMatch);
	    //APSL3874 code deleted
	    return;
	}
	//ALS2136,2175 code moved.

getLogger().logDebug("robertt");
	//end SPR3301
	NbaVpmsModelResult data = getXmlDataFromVpms(NbaVpmsAdaptor.EP_GET_LOWER_LEVEL_REQUIREMENTS);
	VpmsModelResult modelResult = data.getVpmsModelResult();
	if (modelResult.getResultDataCount() > 0 && modelResult.getResultDataAt(0).getResultCount() > 0) { 	//SPR2697 SPR3301
	    //retrive all work for parent
	    NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
	    retOpt.setWorkItem(currentMatch.getID(), true);
	    retOpt.requestSources();
	    retOpt.requestTransactionAsChild();
	    retOpt.setLockWorkItem();
	    retOpt.setLockTransaction();
	    currentMatch = retrieveWorkItem(getUser(), retOpt);  //NBA213
	} else if (isFormWithApp()) {		//ALNA208
		currentMatch = retrieveParentWork(getWork(), true, false); //ALNA208
	}
	//begin ALS2136,2175
	try {
		if (A_WT_MISC_MAIL.equals(getWorkType())) {
			//Start NBLXA-188
			NbaTXLife holdingInq = null;
			ApplicationInfo applicationInfo = null;
			holdingInq = (NbaTXLife) getContracts().get(currentMatch.getNbaLob().getPolicyNumber());//NBLXA-188
			applicationInfo = holdingInq.getPolicy().getApplicationInfo();
			if(applicationInfo!=null && applicationInfo.getApplicationType()==NbaOliConstants.OLI_APPTYPE_GROUPAPP && getWork().getNbaLob().getReqType()==NbaOliConstants.OLI_REQCODE_POLDELRECEIPT && NbaUtils.getPolicyExtension(holdingInq.getPolicy()).getMDRConsentIND() && !getWork().getNbaLob().getGIWorkItemInd()){
				generatePDRMiscWorkForGIPolicies(holdingInq.getPolicy().getCarrierCode()); //NBLXA-1680
				//End NBLXA-188
			}
			//begin ALS2204
			if(updateRequired) //NBLXA-188
			{
					NbaLob lob = getWork().getNbaLob();
					NbaLob parentLob = currentMatch.getNbaLob();
					lob.setPolicyNumber(parentLob.getPolicyNumber());
					// end ALS2204
					lob.setCompany(parentLob.getCompany());// APSL565,QC# 5564
					currentMatch = addRequirementForMiscWork(currentMatch, modelResult);
					// APSL4213
					if (getWork().getNbaLob().getReqType() == NbaOliConstants.SIUL_JI_SUPP) {
						holdingInq = null; //NBLXA-188
						// Holding inquiry retrieved on the basis of POLN provided on the miscmail else null
						if (!NbaUtils.isBlankOrNull(getWork().getNbaLob().getPolicyNumber())) {
							holdingInq = (NbaTXLife) getContracts().get(getWork().getNbaLob().getPolicyNumber());
						}
						if (holdingInq != null) {
							Relation rel = NbaUtils.getRelation(holdingInq.getOLifE(), NbaOliConstants.OLI_REL_JOINTINSURED);
							if (rel == null) {
								addJointInsParty(getWork());
							}
							ApplicationInfoExtension appInfoExt = NbaUtils.getAppInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
							List tentativeDispList = appInfoExt.getTentativeDisp();
							if (tentativeDispList != null && tentativeDispList.size() > 0) {

							}
						}
					}
					getMatchingCases().set(0, currentMatch);
					return;
				}
		}
	} catch (NbaBaseException nbe) {
		//APSL3874 code deleted
		getMatchingCases().set(0, currentMatch);
		throw nbe;
	}
	//end ALS2136,2175
	if(updateRequired) //NBLXA-188
	{
		currentMatch = updateRequirementForMatchingCase(currentMatch, modelResult);
		//SPR3301 code deleted
		getMatchingCases().set(0, currentMatch); //update back to array. //SPR3129
		//SPR3301 code deleted
	}
}
/**
 * Verify that the required information to create a requirement work item is present. The requirement type
 * and vendor code (NBTEMPREQ only) must be present, and the person associated with the requirement must be able to be located on the contract.
 * If the required information is present, the person code and sequence LOBs are updated.
 * @param parentCase the maching parent case
 * @throws NbaBaseException
 * @return boolean true if a permanant requirement should be created
 */
//ACN026 New Method
	//SPR3301 removed vpmsResult from method signature and changed visibility
    protected boolean validateAndUpdateRequiredLobsForRequirement(NbaDst parentCase) throws NbaBaseException { //SPR2697
		boolean createRequirement = false; //SPR2697
		Relation relation = null;//P2AXAL054
		setNbaTxLife((NbaTXLife) getContracts().get(parentCase.getNbaLob().getPolicyNumber())); //SPR3129
		if (getResult() == null) {
			NbaLob lob = getWork().getNbaLob();
			if (lob.getReqType() > 0 && (A_WT_MISC_MAIL.equals(getWorkType()) || lob.getReqVendor() != null)) { //vendor applicable only to NBTEMPREQ
				// //SPR3301
				//P2AXAL054 begin
				Party matchingParty = getPartyForMatchingResult(lob);
				if (matchingParty != null) {
					relation = NbaUtils.getRelationForParty(matchingParty.getId(), nbaTxLife.getOLifE().getRelation().toArray());
				}
				//end P2AXAL054
				if (relation != null && (getNbaTxLife().isInsurableRelation(relation)
					|| relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_OWNER)){ //ALS5336, ALNA159
					lob.setReqPersonCode((int) relation.getRelationRoleCode());
					lob.setReqPersonSeq(Integer.parseInt(relation.getRelatedRefID()));
					createRequirement = true; //SPR2697
				} else {
					relation = NbaUtils.getPrimaryInsured(getNbaTxLife());//ALS2862 //ALS3123
					lob.setReqPersonCode((int) relation.getRelationRoleCode());//ALS2862
					lob.setReqPersonSeq(Integer.parseInt(relation.getRelatedRefID()));//ALS2862
					createRequirement = true;
					//addComment("Unable to locate party on contract using information on work item"); //SPR3301
				}
			} //APSL3874
			//APSL3874 code deleted
			//SPR3301 code deleted
		}
		//SPR3301 code deleted
		return createRequirement; //SPR2697
	}

	//P2AXAL054 method moved into NbaAutomatedProcess
/**
 * This method add the requirement workitem from the misc workitem. It will copy sources from misc workitem to requirement workitem. This method also
 * add requirement contract source on workitem and update requirement control source on the matching case. It assumes that parent case has all child
 * transactions in the object.
 *
 * @param parentCase
 *            the maching parent case
 * @return the updated parent case
 * @throws NbaBaseException
 */
//ACN026 New Method
protected NbaDst addRequirementForMiscWork(NbaDst parentCase,VpmsModelResult vpmsResult) throws NbaBaseException{	//SPR3301 removed vpmsResult //ALS2136 Added vpmsResult
	//NBA213 deleted code
		NbaLob lob = getWork().getNbaLob();
		NbaLob parentLob = parentCase.getNbaLob();
		Set requirementsId = new HashSet();
		//QC12292 APSL3402 Deleted code
		lob.setAppOriginType(parentLob.getAppOriginType()); //ALS4842,ALS5327
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), lob);

		NbaRequirementUtils reqUtils = new NbaRequirementUtils();
		reqUtils.setHoldingInquiry(nbaTxLife);
		reqUtils.setAutoGeneratedInd(true);
		reqUtils.setEmployeeId(getUser().getUserID());
		reqUtils.setReqPersonCodeAndSeq(lob.getReqPersonCode(), lob.getReqPersonSeq());
		reqUtils.setReqType(lob.getReqType());

		List exstingWorkItems = parentCase.getNbaTransactions();
		for (int t = 0; t < exstingWorkItems.size(); t++) {
			NbaTransaction requirement = (NbaTransaction) exstingWorkItems.get(t);
			if (requirement.getID() != null) {
				requirementsId.add(requirement.getID());
			}
		}
		lob.setBackendSystem(parentLob.getBackendSystem()); // SPR2697
		NbaTransaction newRequirement = parentCase.addTransaction(provider.getWorkType(), provider.getInitialStatus());
		newRequirement.increasePriority(provider.getWIAction(), provider.getWIPriority());
		NbaLob requirementLOBs = newRequirement.getNbaLob();
		requirementLOBs.setBackendSystem(lob.getBackendSystem()); // SPR2697
		requirementLOBs.setReqVendor(lob.getReqVendor()); // Vendor code
		requirementLOBs.setReqType(lob.getReqType()); // Req Type
		requirementLOBs.setFormNumber(lob.getFormNumber()); // Req form number	//SPR2697
		requirementLOBs.setReqDrName(lob.getReqDrName()); // Req physician	//SPR2697
		requirementLOBs.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED));
		requirementLOBs.setFormRecivedWithAppInd(lob.getFormRecivedWithAppInd());//ALS5276
		requirementLOBs.setRiskRighterCase(parentLob.getRiskRighterCase()); //APSL4926
		// APSL5321
		if ("1".equals(parentLob.getRiskRighterCase()) && NbaConstants.A_ST_MISC_MAIL.equalsIgnoreCase(getWork().getWorkType())) {
			requirementLOBs.setReinVendorID(lob.getReinVendorID());
		}
		// APSL5321
		//Begin APSL2735
		if(lob.getReqType() == NbaOliConstants.OLI_REQCODE_AUTHEFT
				&& String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT).equalsIgnoreCase(lob.getPaymentMoneySource())){
			requirementLOBs.setPaymentMoneySource(String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT));
		}
		//End APSL2735
		if (lob.getProviderOrder() != null && NbaUtils.isAPSRequirement(lob.getReqType())) { //ALII1818
			requirementLOBs.setProviderOrder(lob.getProviderOrder());
		} else {
			requirementLOBs.setProviderOrder(parentLob.getProviderOrder());
		}

		//begin AXAL3.7.01
		if(lob.getDeliveryReceiptSignDate()!=null){
			requirementLOBs.setDeliveryReceiptSignDate(lob.getDeliveryReceiptSignDate());
			setDeliveryRecepitSignDate(lob.getDeliveryReceiptSignDate());//NBLXA-2133
		}
		//Begin ALPC234
		if (null != lob.getParamedSignDate()) {
			requirementLOBs.setParamedSignDate(lob.getParamedSignDate());
		}
		//Begin APSL4872
		if (null != lob.getReqSignDate()) {
			requirementLOBs.setReqSignDate(lob.getReqSignDate());
		}
		// End APSL4872
		if (null != lob.getReqReceiptDate()) {
			requirementLOBs.setReqReceiptDate(lob.getReqReceiptDate());
			setReqReceiptDate(lob.getReqReceiptDate());
			requirementLOBs.setReqReceiptDateTime(lob.getReqReceiptDateTime());//QC20240
			setReqReceiptDateTime(lob.getReqReceiptDateTime());//QC20240
		}
		//end ALPC234
		//end AXAL3.7.01

		//NBLXA-1794 Begin
		if (null != lob.getLabCollectionDate()) {
			requirementLOBs.setLabCollectionDate(lob.getLabCollectionDate());
			setLabCollectionDate(lob.getLabCollectionDate());
		}
		//NBLXA-1794 End

	    // Begin AXAL3.7.31
		// Determine if additional results are expected.
		// First, determine the scenario by validating the sources.
		for (int i = 0; i < getWork().getNbaSources().size(); i++) {
			NbaSource aSource = (NbaSource) getWork().getNbaSources().get(i);
			validSource(aSource.getSource());
		}
		//begin ALS5904
		List miscSources = getWork().getNbaSources();
		if(miscSources.size()>0){
			NbaSource source = (NbaSource)miscSources.get(0);
			source.getSource().setSourceType(NbaConstants.A_ST_PROVIDER_RESULT);
			updateJointInsuredLOB(source,null);//CR1346708,ALII1674
			source.getNbaLob().setProviderOrder(lob.getProviderOrder()); //ALII1818
			source.setUpdate();

		}
		// end ALS5904

		ArrayList results = waitForAdditionalResults(getWork());
		if (results.size() > 0) {
			StringBuffer comment = new StringBuffer("Requirement result received but additional results (");
			for (int j = 0; j < results.size(); j++) {
				if (j > 0) {
					comment.append(",");
				}
				comment.append((String)results.get(j));
			}
			comment.append(") are expected.");
			addComment(comment.toString());
			requirementLOBs.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_SUBMITTED));  //ALS2280
			// Change status for newly converted requirement so that it gets re-processed and suspended
			newRequirement.setStatus(NbaConstants.A_STATUS_REQUIREMENT_ORDERED);
		}
		// AXAL3.7.31 end
		setReqStatus(requirementLOBs.getReqStatus()); //ALS2280
		if (lob.getParamedSignDate() != null)
			setReqParamedSignDate(lob.getParamedSignDate());

		if (lob.getDeliveryReceiptSignDate() != null) { //NBLXA-2133
			setDeliveryRecepitSignDate(lob.getDeliveryReceiptSignDate());
		}
		if (lob.getReqVendor() != null) {
			setReqProvider(lob.getReqVendor());
		}
		if (lob.getFormNumber() != null) { //ALS4181
			setFormNumber(lob.getFormNumber());
		}
		if (lob.getLabCollectionDate() != null){ //NBLXA-1794
			setLabCollectionDate(lob.getLabCollectionDate()); //NBLXA-1794
		}
		if (lob.getEntityName() != null){ //NBLXA-1895
			requirementLOBs.setEntityName(lob.getEntityName()); //NBLXA-1895
		}
	    // AXAL3.7.31 end
		//copy all default LOBs from case to requirement
		requirementLOBs.setAgency(parentLob.getAgency()); //agency
		requirementLOBs.setAgentID(parentLob.getAgentID()); //agent
		requirementLOBs.setCompany(parentLob.getCompany());
		requirementLOBs.setAppState(parentLob.getAppState());
		requirementLOBs.setPlan(parentLob.getPlan());
		requirementLOBs.setProductTypSubtyp(parentLob.getProductTypSubtyp());
		requirementLOBs.setFaceAmount(parentLob.getFaceAmount());
		requirementLOBs.setApplicationType(parentLob.getApplicationType());//ALS5729
		requirementLOBs.setPaidChgCMQueue(parentLob.getPaidChgCMQueue()); //ALII1250
		reqUtils.processRequirementWorkItem(parentCase, newRequirement, requirementLOBs);
		//Removed APSL2515 QC#10056 for APSL3960
		//Begin NBA130
		RequirementInfo aReqInfo = null;
		Policy policy = nbaTxLife.getPolicy();
		//Start NBLXA-1611
		if((lob.getReqType() == NbaOliConstants.OLI_REQCODE_TRUSTEDCONTACT) ||(lob.getReqType() == NbaOliConstants.OLI_REQCODE_SEOSUPP)){
			aReqInfo = reqUtils.createNewRequirementInfoObject(nbaTxLife, nbaTxLife.getPartyId(Long.valueOf(lob.getReqPersonCode()),String.valueOf(lob.getReqPersonSeq())), getUser(), requirementLOBs);
		}else{//End NBLXA-1611
			aReqInfo = reqUtils.createNewRequirementInfoObject(nbaTxLife, nbaTxLife.getPartyId(lob.getReqPersonCode()), getUser(), requirementLOBs); // SPR3290
		}
		updateRequirementInfoObject(aReqInfo, NbaActionIndicator.ACTION_ADD);

		if(isReqForExistingUniqueID(getWork().getNbaLob().getReqUniqueID())){//APSL3643
			aReqInfo.setRequirementDetails(requirementForExistingUniqueId);//APSL3643
		}

		policy.addRequirementInfo(aReqInfo);
		//begin ALS2859
		if (!NbaUtils.isBlankOrNull(lob.getReqDrName())) {
			NbaOLifEId olifeId = new NbaOLifEId(nbaTxLife);
			Party newDoctor = NbaRequirementUtils.createDoctorForRequirement(lob, olifeId);
			nbaTxLife.createRelation(aReqInfo, newDoctor, NbaOliConstants.OLI_REL_PHYSICIAN);
			nbaTxLife.getOLifE().addParty(newDoctor);
			RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
			requirementInfoExt.setPhysicianPartyID(newDoctor.getId());
		}
		//end ALS2859
		// Begin NBLXA-1895
		if (!NbaUtils.isBlankOrNull(lob.getEntityName()) && NbaUtils.isAPSRequirement(lob.getReqType()) && NbaUtils.isBlankOrNull(lob.getReqDrName())) {
			NbaOLifEId olifeId = new NbaOLifEId(nbaTxLife);
			Party newDoctor = NbaRequirementUtils.createOrgForRequirement(lob, olifeId);
			nbaTxLife.createRelation(aReqInfo, newDoctor, NbaOliConstants.OLI_REL_MEDPROVIDER);
			nbaTxLife.getOLifE().addParty(newDoctor);
			RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
			reqInfoExt.setPhysicianPartyID(newDoctor.getId());
		}
		// End NBLXA-1895
		policy.setActionUpdate();
		// begin APSL3361
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo()); //APSL5191
		if(lob.getReqType() == NbaOliConstants.OLI_REQCODE_EPOLDELSUPP && !isFormWithApp()) {

			if (appInfoExt == null) {
				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
				olifeExt.setActionAdd();
				policy.getApplicationInfo().addOLifEExtension(olifeExt);
				appInfoExt = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo());
			}
			appInfoExt.setReqPolicyDeliverMethod(NbaOliConstants.OLI_POLDELMETHOD_EMAIL);
			appInfoExt.setActionUpdate();
		}
		// end APSL3361
		requirementLOBs.setReqUniqueID(aReqInfo.getRequirementInfoUniqueID());
		//End NBA130
		requirementLOBs.setReqRestriction(aReqInfo.getRestrictIssueCode());//ALS5718

		//attach source from work to new requirement
		List sources = getWork().getNbaSources();
		if (sources.size() > 0) {
			NbaSource source = (NbaSource) sources.get(0);
			//Code deleted ALS5904
			source.getNbaLob().setApplicationType(newRequirement.getNbaLob().getApplicationType());//ALS5729
			source.getNbaLob().setReqUniqueID(aReqInfo.getRequirementInfoUniqueID());//SR657984/APSL3211
			source.setUpdate();
			newRequirement.addNbaSource(source);
			//begin NBA212
			if (source.isTextFormat()) {
			    long attachmentType = lob.getReqType() == NbaOliConstants.OLI_REQCODE_MIBCHECK ? NbaOliConstants.OLI_ATTACH_MIB_SERVRESP
			            : NbaOliConstants.OLI_ATTACH_REQUIRERESULTS;
			    aReqInfo.getAttachment().addAll(createAttachments(source, attachmentType, true)); //NBA130
			}

			//end NBA212
			NbaRequirementUtils.markSourceDisplayable(aReqInfo, source, getWork()); // ALS2544//ALS4420
		}
		//APSL3405,QC#12555 begin
		if (getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_1009800031 && getWork().getNbaLob().getFormNumber()!=null) {
			addFormInstancesForSupp(getWork(),parentCase,FORM_NAME_MEDSUP);
		}//APSL3405,QC#12555 end
		//NBLXA-2179 Start
		if (getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_688 && getWork().getNbaLob().getFormNumber()!=null) {
			addFormInstancesForSupp(getWork(),parentCase,FORM_NAME_FRTS);
		}
		//NBLXA-2179 End
		doContractUpdate(nbaTxLife); //NBA130
		//ALS4287 code deleted
		//Begin ALS2584
		if (sources.size() > 0 && NbaVPMSHelper.isSupplementTabForm(newRequirement.getNbaLob())) { //ALS3828, NA_AXAL004
			copyCaseLobToSource((NbaSource)sources.get(0),parentLob);//APSL1076
			parentCase.addNbaSource((NbaSource) sources.get(0));
	        //ALII355 deleted
		}

		//End ALS2584
		//update to AWD
		//APSL5382 Commented code for Reissue Phase 2 CIPE changes
		/**
		if (getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_AUTHEFT && !NbaUtils.isBlankOrNull(getWork().getNbaLob().getPaymentMoneySource())
				&& getWork().getNbaLob().getPaymentMoneySource().equals(NbaConstants.CWOR) && appInfoExt != null
				&& appInfoExt.getInitialPremiumPaymentForm() != NbaOliConstants.OLI_PAYFORM_EFT && hasUnderwriterApprovalStatus()) { // APSL5191
			createContractChangeWorkItem(aReqInfo); // APSL4768
		}
		*/

		parentCase = updateWork(user, parentCase);  //NBA213


		//update requirement control source on case and new requirement
		List nbaTransactions = parentCase.getNbaTransactions();
		for (int j = 0; j < nbaTransactions.size(); j++) {
			NbaTransaction nbaTransaction = (NbaTransaction) nbaTransactions.get(j);
			if (nbaTransaction.getTransaction().getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)) {
				if (!requirementsId.contains(nbaTransaction.getID())) {
					reqUtils.addRequirementControlSource(nbaTransaction);
					reqUtils.addMasterRequirementControlSource(parentCase, nbaTransaction);
					break; //there is only one requirement
				}
			}
		}
		parentCase.setUpdate();

		//SPR3301 code deleted


		if (requirementLOBs.getReqStatus().equals(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED))) { //ALS2280
			//waive lower level requirements
			parentCase = waiveRequirementsOnMatchingCase(parentCase, vpmsResult);	//ALS2136,2175
		} //ALS2280

		//get new status from vp/ms
		Map deOink = new HashMap();
		deOink.put("A_MiscMatchType", "2"); //created new requirement
		lob.setAppDate(parentLob.getAppDate()); //NBA130
		lob.setOperatingMode(parentLob.getOperatingMode()); //NBA130
		NbaProcessStatusProvider miscStatus = new NbaProcessStatusProvider(getUser(), getWork(), deOink);
		//update status on work
		changeStatus(miscStatus.getPassStatus());
		if(isFormWithApp()){ //ALNA208
			parentCase = breakMiscMail(parentCase); //ALNA208
		} //ALNA208
		parentCase = updateWork(getUser(), parentCase);  //NBA213
		unsuspendWorkitems(); //SPR2565
		//SPR3129 code deleted
		//Begin APSL581
		List sourcesList = getWork().getNbaSources();
		for(int k=0;k<sourcesList.size();k++) {
			NbaSource source = (NbaSource) sourcesList.get(k);
			if(NbaConstants.A_ST_PROVIDER_RESULT.equalsIgnoreCase(source.getNbaLob().getWorkType()) && NbaConstants.A_ST_MISC_MAIL.equalsIgnoreCase(getWorkType())){
				source.setBreakRelation();
			}
		}//End APSL581
		doUpdateWorkItem();
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
		//NBA213 deleted code
		resetSignificantRequirementReceivedLOB(parentCase, requirementLOBs ); //APSL1526
	return parentCase;
}

	//ALNA208 New Method
	protected NbaDst breakMiscMail(NbaDst parentCase) throws NbaBaseException {
		//NbaDst parentCase = retrieveParentWork(getWork(), true, false);
		List nbaTransactions = parentCase.getNbaTransactions();
		for (int j = 0; j < nbaTransactions.size(); j++) {
			NbaTransaction nbaTransaction = (NbaTransaction) nbaTransactions.get(j);
			if (nbaTransaction.getID().equals(getWork().getID())){
				nbaTransaction.setBreakRelation();
				nbaTransaction.setActionUpdate();
				break;
			}
		}
		return parentCase;
	}

  //ALS4287 code deleted
/**
 * This method convert the temporary workitem worktype to worktype return from VP/MS model. This
 * method also add requirement contract source on workitem and update requirement control
 * source on the matching case.
 * @param parentCase the maching parent case
 * @param vpmsResult the lower level requirement result from requirement vpms model
 * @return the updated parent case
 * @throws NbaBaseException
 */
//ACN026 New Method
protected NbaDst updateRequirementForMatchingCase(NbaDst parentCase, VpmsModelResult vpmsResult) throws NbaBaseException{
	NbaLob lob = getWork().getNbaLob();
 if (!A_WT_MISC_MAIL.equals(getWorkType())) //ALS2136,ALS2175
  {
	NbaTransaction requirement = null;
	if (getWork().isTransaction()) {
		requirement = getWork().getNbaTransaction();
	} else {
		throw new NbaBaseException("Invalid work item to process.");
	}

	NbaLob parentLob = parentCase.getNbaLob();
	lob.setAppDate(parentLob.getAppDate()); //NBA130
	lob.setOperatingMode(parentLob.getOperatingMode()); //NBA130
	lob.setAppOriginType(parentLob.getAppOriginType()); //ALS4966
	NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), lob);

	requirement.getTransaction().setWorkType(provider.getWorkType());
	requirement.setStatus(provider.getInitialStatus());
	setNbaTxLife((NbaTXLife) getContracts().get(parentLob.getPolicyNumber())); //NBA130 SPR3129
	NbaRequirementUtils reqUtils = new NbaRequirementUtils();
	reqUtils.setHoldingInquiry(nbaTxLife);
	reqUtils.setAutoGeneratedInd(true);
	reqUtils.setEmployeeId(user.getUserID());
	reqUtils.setReqPersonCodeAndSeq(lob.getReqPersonCode(), lob.getReqPersonSeq());
	reqUtils.setReqType(lob.getReqType());

	parentCase.getNbaCase().addNbaTransaction(requirement);
	requirement.increasePriority(provider.getWIAction(), provider.getWIPriority());

	//Copy default LOBs from case to workitem
	lob.setPolicyNumber(parentLob.getPolicyNumber()); //NBA130
	lob.setAgency(parentLob.getAgency()); //agency
	lob.setAgentID(parentLob.getAgentID()); //agent
	lob.setCompany(parentLob.getCompany());
	lob.setAppState(parentLob.getAppState());
	lob.setPlan(parentLob.getPlan());
	lob.setProductTypSubtyp(parentLob.getProductTypSubtyp());
	lob.setFaceAmount(parentLob.getFaceAmount());
	lob.setOperatingMode(parentLob.getOperatingMode());
	lob.setBackendSystem(parentLob.getBackendSystem()); // SPR2778 SPR3129
	lob.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED));
	lob.setApplicationType(parentLob.getApplicationType());//ALS5729
	lob.setPaidChgCMQueue(parentLob.getPaidChgCMQueue()); //ALII1250

	// Begin AXAL3.7.31
	// Determine if additional results are expected.
	// First, determine the scenario by validating the sources.
	for (int i = 0; i < getWork().getNbaSources().size(); i++) {
		NbaSource aSource = (NbaSource) getWork().getNbaSources().get(i);
		validSource(aSource.getSource());
	}
	ArrayList results = waitForAdditionalResults(getWork());
	boolean awaitResults = false; //ALS4494
	String reqStat = Long.toString(NbaOliConstants.OLI_REQSTAT_SUBMITTED); //ALS4494
	if (results.size() > 0) {
		StringBuffer comment = new StringBuffer("Requirement result received but additional results (");
		for (int j = 0; j < results.size(); j++) {
			if (j > 0) {
				comment.append(",");
			}
			comment.append((String)results.get(j));
		}
		comment.append(") are expected.");
		addComment(comment.toString());
		lob.setReqStatus(reqStat); //ALS4494
		awaitResults = true;  //ALS4494
		// Change status for newly converted requirement so that it gets re-processed and suspended
		requirement.setStatus(NbaConstants.A_STATUS_REQUIREMENT_ORDERED);
	}
	// AXAL3.7.31 end

	reqUtils.processRequirementWorkItem(parentCase, requirement);
	//Begin NBA130
	if( null == lob.getReqReceiptDate()) {
		setReqReceiptDate(new Date());
		setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
	} else {
		setReqReceiptDate(lob.getReqReceiptDate());
		setReqReceiptDateTime(lob.getReqReceiptDateTime());//QC20240
	}
	lob.setReqReceiptDate(getReqReceiptDate()); //ALS2344
	lob.setReqReceiptDateTime(getReqReceiptDateTime());//QC20240
    // Begin AXAL3.7.31
	setReqStatus(lob.getReqStatus());
	if (lob.getParamedSignDate() != null)
		setReqParamedSignDate(lob.getParamedSignDate());

	if (lob.getDeliveryReceiptSignDate() != null) { //NBLXA-2133
		setDeliveryRecepitSignDate(lob.getDeliveryReceiptSignDate());
	}
	//Begin APSL4872
	if (lob.getReqSignDate() != null)
		setReqSignDate(lob.getReqSignDate());
	//End APSL 4872

	if (lob.getReqVendor() != null) {
		setReqProvider(lob.getReqVendor());
	}
    // AXAL3.7.31 end

	//NBLXA-1794 Begin
	if (lob.getLabCollectionDate() != null){
		setLabCollectionDate(lob.getLabCollectionDate());
	}
	//NBLXA-1794 end

	Policy policy = nbaTxLife.getPolicy();
	RequirementInfo aReqInfo = reqUtils.createNewRequirementInfoObject(nbaTxLife, nbaTxLife.getPartyId(lob.getReqPersonCode()), getUser(), lob); // SPR3290
	lob.setReqUniqueID(aReqInfo.getRequirementInfoUniqueID());
	lob.setReqRestriction(aReqInfo.getRestrictIssueCode());//ALS5718
	System.out.println("case matching");
	updateRequirementInfoObject(aReqInfo, NbaActionIndicator.ACTION_ADD);
	updateRequirementInfo(requirement, aReqInfo);
	policy.addRequirementInfo(aReqInfo);
	//begin ALS2859
	if (!NbaUtils.isBlankOrNull(lob.getReqDrName())) {
		NbaOLifEId olifeId = new NbaOLifEId(nbaTxLife);
		Party newDoctor = NbaRequirementUtils.createDoctorForRequirement(lob, olifeId);
		nbaTxLife.createRelation(aReqInfo, newDoctor, NbaOliConstants.OLI_REL_PHYSICIAN);
		nbaTxLife.getOLifE().addParty(newDoctor);
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
		reqInfoExt.setPhysicianPartyID(newDoctor.getId());
	}
	//end ALS2859
	// Begin NBLXA-1895
	if (!NbaUtils.isBlankOrNull(lob.getEntityName()) && NbaUtils.isAPSRequirement(lob.getReqType()) && NbaUtils.isBlankOrNull(lob.getReqDrName())) {
		NbaOLifEId olifeId = new NbaOLifEId(nbaTxLife);
		Party newDoctor = NbaRequirementUtils.createOrgForRequirement(lob, olifeId);
		nbaTxLife.createRelation(aReqInfo, newDoctor, NbaOliConstants.OLI_REL_MEDPROVIDER);
		nbaTxLife.getOLifE().addParty(newDoctor);
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
		reqInfoExt.setPhysicianPartyID(newDoctor.getId());
	}
	// End NBLXA-1895
	policy.setActionUpdate();
	//attach source from work to new requirement
	List sources = getWork().getNbaSources();
	// Begin AXAL3.7.31
	// More than one source is possible, so loop through them
	for (int i = 0; i < sources.size(); i++) {
		NbaSource source = (NbaSource) sources.get(i);
		//begin NBA212
		if (source.isTextFormat()) {
			source.getSource().setSourceType(NbaConstants.A_ST_PROVIDER_RESULT);
	// End AXAL3.7.31
		    long attachmentType = lob.getReqType() == NbaOliConstants.OLI_REQCODE_MIBCHECK ? NbaOliConstants.OLI_ATTACH_MIB_SERVRESP
		            : NbaOliConstants.OLI_ATTACH_REQUIRERESULTS;
		    source.setText(getSourceText(source.getText()));	// AXAL3.7.31
		    aReqInfo.getAttachment().addAll(createAttachments(source, attachmentType, true)); //NBA130
		    // APSL3175 Begin
		    if (lob.getReqType() == NbaOliConstants.OLI_REQCODE_MIBCHECK && nbaTxLife.isSIApplication()) {
		    	createMIBInquiry(aReqInfo);
		    }
		    // APSL3175 End
		}
		//end NBA212
		if (i==0) { //ALII1818, APSL3060
			source.getNbaLob().setProviderOrder(lob.getProviderOrder());
			source.setUpdate(); //APSL3871 Code reverted
		}
		if (source.isImageFormat()) {//APSL3871
			source.getNbaLob().setReqUniqueID(aReqInfo.getRequirementInfoUniqueID());//SR657984/APSL3211/APSL3871
			source.setUpdate();
		}

	}

	doContractUpdate(nbaTxLife);
	//End NBA130
	//update LOBs and add requirement control source
	// SPR3290 code deleted
	reqUtils.addRequirementControlSource(requirement);
	reqUtils.addMasterRequirementControlSource(parentCase, requirement);
	//reset reqStatus since RequirementControlSource functionality overrides it
	if (awaitResults) { //ALS4494
		lob.setReqStatus(reqStat);  //ALS4494
	}  //ALS4494
	if(lob.getReqType() == NbaOliConstants.OLI_REQCODE_MIBCHECK){ //ALII1639,ALII1656
		unSuspendMatchingPredictiveCase(getWork());//CR1454508(APSL2681)
	}
  } //ALS2136,ALS2175

	//Begin AXAL3.7.31
	if (lob.getReqStatus().equals(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED))) {
	//waive lower level requirements
	parentCase = waiveRequirementsOnMatchingCase(parentCase, vpmsResult);

	//NBA213 deleted code
	//workitem already converted to requirement workitem and having in correct status
	updateWork(getUser(), parentCase);  //NBA213
	unsuspendWorkitems(); //SPR2565

	//SPR3129 code deleted
	setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
	//NBA213 deleted code
	} else {
		updateWork(getUser(), parentCase);  //NBA213
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, NbaConstants.A_STATUS_REQUIREMENT_ORDERED,
					NbaConstants.A_STATUS_REQUIREMENT_ORDERED));
	}
	//AXAL3.7.31 end
	resetSignificantRequirementReceivedLOB(parentCase, lob); //APSL1526
	return parentCase;
}

/**
 * This method check for the lower level requirements on the provided case. If lower level requirements are
 * found, change thier status to waived.
 * @param parentCase the parent matched case
 * @param vpmsResult the lower level requriement result from requirement vpms model
 * @return the updated parent case object
 * @throws NbaBaseException
 */
//ACN026 New Method
protected NbaDst waiveRequirementsOnMatchingCase(NbaDst parentCase, VpmsModelResult vpmsResult) throws NbaBaseException {
	if (vpmsResult.getResultDataCount() > 0 && vpmsResult.getResultDataAt(0) != null) {
		ResultData resultData = vpmsResult.getResultDataAt(0);
		NbaLob lob = getWork().getNbaLob();
		NbaLob tempLOB = new NbaLob();
		tempLOB.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_WAIVED));
		tempLOB.setAppDate(parentCase.getNbaLob().getAppDate()); //NBA130
		tempLOB.setOperatingMode(parentCase.getNbaLob().getOperatingMode()); //NBA130
		NbaProcessStatusProvider provider = new NbaProcessStatusProvider(getUser(), tempLOB);
		for (int i = 0; i < resultData.getResultCount(); i++) {
			if (resultData.getResultAt(i) != null) {
				int lowerLevelReqType = Integer.parseInt(resultData.getResultAt(i));
				List nbaTransactions = parentCase.getNbaTransactions();
				for (int j = 0; j < nbaTransactions.size(); j++) {
					NbaTransaction nbaTransaction = (NbaTransaction) nbaTransactions.get(j);
					if (nbaTransaction.getTransaction().getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)) {
						NbaLob requirementLOB = nbaTransaction.getNbaLob();
						if (requirementLOB.getReqType() == lowerLevelReqType
							&& requirementLOB.getReqPersonCode() == lob.getReqPersonCode()
							&& requirementLOB.getReqPersonSeq() == lob.getReqPersonSeq()) {
							requirementLOB.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_WAIVED));
							nbaTransaction.setStatus(provider.getPassStatus());
							//begin SPR2565
							if (nbaTransaction.isSuspended()) {
								NbaSuspendVO suspendVO = new NbaSuspendVO();
								suspendVO.setTransactionID(nbaTransaction.getID());
								unsuspendVOs.add(suspendVO);
							}
							//end SPR2565
							break;
						}
					}
				}
			}
		}
	}
	return parentCase;
}

/**
 * This method attach the Misc work to the matching case.
 * @param parentCase the maching parent case
 */
//ACN026 New Method
protected void attachMiscWorkToMatchingCase(NbaDst parentCase) throws NbaBaseException {
	//NBA213 deleted code
		//Attach the miscellaneous work to first match of case
		parentCase.getNbaCase().addNbaTransaction(getWork().getNbaTransaction());
		//begin SPR2636
		NbaLob workLob = getWork().getNbaLob();
		NbaLob parentLob = parentCase.getNbaLob();
		workLob.setBackendSystem(parentLob.getBackendSystem());
		workLob.setCompany(parentLob.getCompany());
		workLob.setPolicyNumber(parentLob.getPolicyNumber());
		workLob.setPlan(parentLob.getPlan());
		workLob.setProductTypSubtyp(parentLob.getProductTypSubtyp()); //SPR3237
		workLob.setAppOriginType(parentLob.getAppOriginType()); //ALS4966
		//end SPR2636
		//SPR3301 code deleted

		Map deOink = new HashMap();
		deOink.put("A_MiscMatchType", "0");
		// Begin ALS2872
		List sourceList = getWork().getNbaSources();
		int count = sourceList.size();
		NbaSource aSource = null;
		for (int i = 0; i < count; i++) {
			aSource = (NbaSource) sourceList.get(i);
			if (aSource.getSource().getSourceType().equals(NbaConstants.A_WT_MISC_MAIL)) {
				break;
			}
		}
		if (aSource != null) {
			deOink.put("A_CreateStationLOB", aSource.getNbaLob().getCreateStation()); //ALS2872
		}
		// End ALS2872
		workLob.setAppDate(parentLob.getAppDate()); //NBA130
		workLob.setOperatingMode(parentLob.getOperatingMode()); //NBA130
		NbaTXLife nbaTxLife= (NbaTXLife)getContracts().get(parentLob.getPolicyNumber());//ALS3233
		deOink.put("A_HasParentInd","true");//ALS3233
		NbaProcessStatusProvider miscStatus = new NbaProcessStatusProvider(getUser(), getWork(), nbaTxLife,deOink);//ALS3233
		changeStatus(miscStatus.getPassStatus());
		addComment("Miscellaneous mail matched");
		//remove the relate indicator set during addNbaTransaction call
		//Begin ALS2584
		List sources = getWork().getNbaSources();
		if (sources.size() > 0) {//ALS3828, NA_AXAL004
			NbaSource nbaSource= (NbaSource) sources.get(0);//APSL1076
		    if(!isFormAvailableOnCase(parentCase,nbaSource)){ //ALS3483
		    	copyCaseLobToSource(nbaSource,parentCase.getNbaLob());//APSL1076
		    	parentCase.addNbaSource(nbaSource);//APSL1076
		    }
		}
		//Begin APSL581
		for (int k = 0; k < sources.size(); k++) {
			NbaSource source = (NbaSource) sources.get(k);
			if (NbaConstants.A_ST_PROVIDER_RESULT.equalsIgnoreCase(source.getNbaLob().getWorkType()) && NbaConstants.A_ST_MISC_MAIL.equalsIgnoreCase(getWorkType())) {
				source.setBreakRelation();
			}
		}//End APSL581
		//End ALS2584
		updateWork(getUser(), parentCase); //SPR3301, NBA213
		//SPR3129 code deleted
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
	//NBA213 deleted code
}

/**
 * This method looks at the APORDERD entry in the Requirement Control Source
 * for the work item to see if the work item has been previously suspended.
 * If not, it calculates the activation date by making a call to the Requirements VP/MS
 * model to get the number of suspend days and then suspends the work item.
 * If previously suspended, the process sets the work item's status to
 * the fail status to move it to either the Follow up queue (if an NBREQRMNT
 * work item) or the Error queue (if an NBTEMPREQ work item).
 * @throws NbaBaseException
 */
public void processUnmatchedWorkitem() throws NbaBaseException {
		setReqCtrlSrc(getWork().getRequirementControlSource());
		//NBA188 code deleted
		if (reqCtrlSrc == null) { // add a new source to the case
				NbaXMLDecorator xmlDecorator = new NbaXMLDecorator();
				xmlDecorator.addRequirement(getWork().getNbaTransaction(), getWork().getID());
			NbaRequirementUtils reqUtils = new NbaRequirementUtils(); //ACN014
			reqUtils.updateRequirementControlSource(null,work.getNbaTransaction(),xmlDecorator.toXmlString(),NbaRequirementUtils.actionAdd); //ACN014 SPR2992
				setReqCtrlSrc(getWork().getRequirementControlSource());
		}
		setReqCtlSrcXml(new NbaXMLDecorator(reqCtrlSrc.getText()));
		AutomatedProcess ap = reqCtlSrcXml.getAutomatedProcess(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
		if (ap == null) {
				ap = new AutomatedProcess();
				ap.setProcessId(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
				getReqCtlSrcXml().getRequirement().addAutomatedProcess(ap); //NBA192
		}

		//Begin SR679590 (APSL3215)
		//If Application State is Kansas or Hawai
		if(getContracts().get(getWork().getNbaLob().getPolicyNumber()) == null) {
			retrieveContract(getWork());
		}
		setNbaTxLife((NbaTXLife)getContracts().get(getWork().getNbaLob().getPolicyNumber()));
		if(getNbaTxLife().getNbaHolding().getAppState().longValue() ==  NbaOliConstants.OLI_USA_KS ||
				getNbaTxLife().getNbaHolding().getAppState().longValue() ==	NbaOliConstants.OLI_USA_HI) {
			if(getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_1009800030) {
				NbaParty nbAParty = null;
				RequirementInfo ncfReqInfo = getNbaTxLife().getRequirementInfo(getWork().getNbaLob().getReqUniqueID());
				if(ncfReqInfo != null && ncfReqInfo.hasAppliesToPartyID()) {
					nbAParty = getNbaTxLife().getParty(ncfReqInfo.getAppliesToPartyID());
				}else {
					nbAParty = getNbaTxLife().getPrimaryParty();
				}
				if(getNbaTxLife().getRequirementInfoList(nbAParty, NbaOliConstants.OLI_REQCODE_BLOOD).size() > 0
						|| getNbaTxLife().getRequirementInfoList(nbAParty, NbaOliConstants.OLI_REQCODE_URINE).size() >0) {
					if(!isReqOutstanding(NbaOliConstants.OLI_REQCODE_BLOOD, nbAParty)
							|| !isReqOutstanding(NbaOliConstants.OLI_REQCODE_URINE, nbAParty)) {
						satisfyRequirement(getWork().getNbaLob(), ncfReqInfo);
						doContractUpdate(getNbaTxLife());
				 		doUpdateWorkItem();
				 		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
				 		return;
					}
				}
			}
		}
		//End SR679590(APSL3215)
		//Begin ALS2814
		 NbaLob lob = work.getNbaLob();
        if (lob.getReqVendor() != null && lob.getReqVendor().equalsIgnoreCase("MANL")
				&& (ap.getSuspendDate() == null || isSuspendNeeded(lob))) { //NBLXA-1732 -added suspended check
			// APSL3447 code changes reverted from APSL3751
        	suspendUnmatchedWorkitem(getFollowUpFrequency(work.getNbaLob().getReqUniqueID()));
		} else {//End ALS2814
	        int suspendDays = getSuspendDays(); //APSL5312 -- Moved from if Condition
	        boolean isReqSusReq = isSuspendRequired(); // APSL5312 -- new Method to check is requirement should suspend.
			if (ap.getSuspendDate() == null || isSuspendNeeded(lob)) { // if it hasn't been suspended ,NBLXA-1732 -added suspended check
				//ALS2814 Code deleted
				//Begin AXAL3.7.06
					if (suspendDays == 0 && ! isSuspendNeeded(lob) && !isLNRCRequirement(lob)){ //AXAL3.7.06 if 0, route to fail status, ALS2814,NBLXA-1732 -added suspended check, NBLXA-2072 check for Risk Classifier
						GregorianCalendar calendar = new GregorianCalendar();
				        calendar.setTime(new Date());
				        calendar.add(Calendar.DAY_OF_WEEK, suspendDays); //ALS2814
				        setFollowupdate(calendar, suspendDays);//ALS4843
						changeStatus(getFailStatus());	//
						addComment("Matching work item not found");
						doUpdateWorkItem();
						//NBA020 code deleted
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getFailStatus(), getFailStatus()));
					} else {
						//NBLXA-2072 Start
						if(isMVRRequirementForLexisNexis(lob) && lob.getSuspensionCount() != null){
							if(lob.getSuspensionCount().equalsIgnoreCase(NbaConstants.LOB_SUSPENSION_COUNT_01)){
								actualSuspendDays = suspendDays;
								suspendDays = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.MVR_SUSPEND_DAYS));
								getWork().getNbaLob().setSuspensionCount(NbaConstants.LOB_SUSPENSION_COUNT_02);
								getWork().setUpdate();
							}
						}
						//NBLXA-2072 End
					    suspendUnmatchedWorkitem(suspendDays); //NBA192, ALS2814
					    //Begin ALPC153
//					    if (getWork().getWorkType() == A_WT_REQUIREMENT) {
//						    if (getMatchingCases().size() == 0) {
//						    	findCase();
//						    }
//						    NbaDst parentCase = (NbaDst) getMatchingCases().get(0);
//						    resetUWCM(parentCase);
//						    parentCase = updateWork(getUser(), parentCase);
//						    getMatchingCases().set(0, parentCase);
//					    }
					    //End ALPC153
					}
					//End AXAL3.7.06
			}else if (isReqSusReq && ap.getSuspendDate() != null ){ // APSL5312 One More Check for suspension
				suspendUnmatchedWorkitem(suspendDays); // // APSL5312
			//NBLXA-2072 Start
			} else if (isMVRRequirementForLexisNexis(lob) && lob.getSuspensionCount() != null) {
				if (lob.getSuspensionCount().equalsIgnoreCase(NbaConstants.LOB_SUSPENSION_COUNT_02)) {
					Map deOink = new HashMap();
					deOink.put("A_OrderMVR", "true");
					setStatusProvider(new NbaProcessStatusProvider(getUser(), getWork(), getNbaTxLife(), deOink));
					changeStatus(getPassStatus());
					getWork().getNbaLob().setSuspensionCount(NbaConstants.LOB_SUSPENSION_COUNT_03);
					getWork().setUpdate();
					doUpdateWorkItem();
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
					return;
				}
				suspendUnmatchedWorkitem(suspendDays);
				//NBLXA-2072 End
			} else {
					changeStatus(getFailStatus());	//NBA097
					addComment("Matching work item not found");
					doUpdateWorkItem();
					//NBA020 code deleted
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getFailStatus(), getFailStatus()));
			}
	 	}//ALS2814
}

	//New Method added for SR679590(APSL3215)
	public boolean isReqOutstanding(long reqCode, NbaParty nbAParty) {
 		List reqList = getNbaTxLife().getRequirementInfoList(nbAParty, reqCode);
		for(int i=0;i<reqList.size();i++) {
			RequirementInfo requirementInfo = (RequirementInfo) reqList.get(i);
			if(NbaUtils.isRequirementOutstanding(requirementInfo.getReqStatus())) {
				return true;
			}
		}
 		return false;
 	}


	//New Method added for SR679590(APSL3215)
	public void satisfyRequirement(NbaLob nbaLob, RequirementInfo requirementInfo)  {
		nbaLob.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED));
		nbaLob.setReqReceiptDate(new Date());
		nbaLob.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date()));
		requirementInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_RECEIVED);
		requirementInfo.setReceivedDate(new Date());
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
		reqInfoExt.setReceivedDateTime(dateFormat.format(new Date()));//QC20240
		requirementInfo.setActionUpdate();
		reqInfoExt.setActionUpdate();
		changeStatus(getPassStatus());
	}
/**
 * Process unmatched miscellaneous mail work items, suspend them if APHL is not present on it
 * Send to error queue with unmatched status if already suspended once
 * @throws NbaBaseException throw base exception if unable to process unmatched work
 */
//NBA080 New Method
	public void processUnmatchedMiscWork() throws NbaBaseException {
		NbaLob workLob = work.getNbaLob();
		// begin NBA231
		if (isReg60PreSale()) {
			hasParentInd = false;
			reinitializeStatusFields();
			generateApplication();
			changeStatus(getPassStatus());
			addComment("Reg60 PreSale work item.");
			doUpdateWorkItem();
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));

			return;
		}
		// end NBA231
		// Begin ALS5113
		// APSL5335 :: START -- Informative Comments when Six month Old Cases, NBLXA-2437 moved code outside if
		String strComments = null;
		String commentsForTwelveMonth = null; //NBLXA-2437
		if (sixMonthsOldAppMatchingCases != null && !sixMonthsOldAppMatchingCases.isEmpty() && sixMonthsOldAppMatchingCases.size() > 0) {
			strComments = getCommentsWithSixMonthsOldDispPolicy(sixMonthsOldAppMatchingCases,6);
		}
		if (strComments != null) {
			addComment(strComments);
		}
		//Begin NBLXA-2437
		if (!NbaUtils.isBlankOrNull(twelveMonthsOldAppMatchingCases)) {
			commentsForTwelveMonth = getCommentsWithSixMonthsOldDispPolicy(twelveMonthsOldAppMatchingCases,12);
			if (commentsForTwelveMonth !=null) {
				addComment(commentsForTwelveMonth);
			}
		}
		// End NBLXA-2437
		if(strComments == null && commentsForTwelveMonth == null  ) { //NBLXA-2437
			addComment("Miscellaneous work not matched");
		}
		// APSL5335 :: END
		VpmsModelResult maxSuspendData = getDataFromVpms(NbaVpmsAdaptor.EP_GET_MAXIMUM_SUSPENDS_ALLOWED);
		String maxSuspendCount = getFirstResult(maxSuspendData);
		int maxSuspends = Integer.parseInt(maxSuspendCount);
		Date createDate = NbaUtils.getDateFromStringInAWDFormat(workLob.getCreateDate());
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(createDate);
		calendar.add(Calendar.DAY_OF_WEEK, maxSuspends);
		Date maxSuspendDate = (calendar.getTime());

		if ((new Date()).after(maxSuspendDate)) {
			// End ALS5113
			changeStatus(getFailStatus());

			doUpdateWorkItem();
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getFailStatus(), getFailStatus()));
		} else {
			// Begin NBLXA-2450[NBLXA-2328]
			calendar = new GregorianCalendar();
			calendar.setTime(createDate);
			calendar.add(Calendar.DAY_OF_WEEK, 30);
			Date afterOneMonthTime = (calendar.getTime());
			String suspendDays = "0";
			if ((new Date()).after(afterOneMonthTime)) {
				suspendDays = SUSPEND_DAYS_335;
			} else {
				// End NBLXA-2450[NBLXA-2328]
				VpmsModelResult data = getDataFromVpms(NbaVpmsAdaptor.EP_CASE_MATCH_SUSPEND_DAYS);
				suspendDays = getFirstResult(data);
			}
			int suspendDay = Integer.parseInt(suspendDays);
			if (suspendDay > 0) {
				calendar = new GregorianCalendar();// ALS5113
				calendar.setTime(new Date());
				calendar.add(Calendar.DAY_OF_WEEK, suspendDay);
				Date suspendDate = (calendar.getTime());
				addComment("Suspended awaiting matching case");
				// ALS5113 Removed
				suspendTransaction(suspendDate);

			}
			
			
			
			
			//Begin NBLXA-1719
			 suspendDay = SUSPEND_DAYS_365; // NBLXA-2579
			calendar = new GregorianCalendar(); // ALS5113
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_WEEK, suspendDay);
			Date suspendDate = (calendar.getTime());
			//setFollowupdate(calendar, suspendDay);// ALS4843
			addComment("Suspended awaiting matching case");
			// Update the Requirement Control Source to indicate that the work item was suspended by this process
			/*NbaRequirementUtils reqUtils = new NbaRequirementUtils();
			reqUtils.updateRequirementControlSource(null, work.getNbaTransaction(), getReqCtlSrcXml().toXmlString(),
					NbaRequirementUtils.actionUpdate);*/
			suspendTransaction(suspendDate);
			// End ALS5113
		}
	}


/**
 * This method retrieves the matching work items found in the NbaSearchResultVO object.
 * Each work item is retrieved and locked.  If unable to retrieve with lock, this work item
 * will be suspended for a brief period of time due to the auto suspend flag being set.
 * @param searchResults the results of the previous AWD lookup
 * @throws NbaBaseException NbaLockException
 */
//ACN020 NbaLockException DELETED
public void retrieveMatchingWorkItems(List searchResults) throws NbaBaseException {
		//NBA213 deleted code
				//Begin NBA080
				//if the AWD search was never called, searchResults will be null.
				matchingWorkItems = new ArrayList();
				if(searchResults == null){
					return;
				}
				ListIterator results = searchResults.listIterator();
				if (getLogger().isDebugEnabled()) { //NBLXA-2421
					getLogger().logDebug("results count== " + searchResults.size());
				} // NBLXA-2421
				//End NBA080
				while (results.hasNext()) {
					NbaTransactionSearchResultVO resultVO = (NbaTransactionSearchResultVO) results.next();
					NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
					retOpt.setWorkItem(resultVO.getTransactionID(), false);
					retOpt.requestSources();
					retOpt.setLockWorkItem();
					retOpt.setAutoSuspend();
					NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);  //NBA213

					// Start NBLXA-1787
					boolean polNumFlag = true;
					if (!(getWork().getWorkType().equals(A_WT_REQUIREMENT)) && !NbaUtils.isBlankOrNull(getWork().getNbaLob().getPolicyNumber()) //NBLXA-2091
							&& !NbaUtils.isBlankOrNull(aWorkItem.getNbaLob().getPolicyNumber())
							&& !(getWork().getNbaLob().getPolicyNumber().equals(aWorkItem.getNbaLob().getPolicyNumber()))
							&& validatePolicyNumber(getWork().getNbaLob().getPolicyNumber()) >= 1) {
						polNumFlag = false;
					}
					// End NBLXA-1787

					//begin SPR1784
					if (checkMatchedWorkItem(aWorkItem) && polNumFlag) { // NBLXA-1787 added polNumFlag
						boolean addWorkitem = true;//NBLXA-2421
						//begin ALS4961
						//if we are working on a perm work item, reverse the map
						if (A_WT_REQUIREMENT.equals(getWork().getTransaction().getWorkType())) {
							getReqCodeTransform().put(String.valueOf(aWorkItem.getNbaLob().getReqType()),String.valueOf(getWork().getNbaLob().getReqType()));
						} else {
				    	getReqCodeTransform().put(String.valueOf(getWork().getNbaLob().getReqType()), String.valueOf(aWorkItem.getNbaLob().getReqType()));	//AXAL3.7.31
						}
						//end ALS4961
						// Start NBLXA-2421
						if (searchResults.size() > 1 && (A_WT_TEMP_REQUIREMENT.equals(getWork().getTransaction().getWorkType())
								|| A_WT_MISC_MAIL.equals(getWork().getTransaction().getWorkType()))) {
							addWorkitem = isFirstNameMatch(getWork(), aWorkItem);
						}
						// End NBLXA-2421

						// end ALS4961
						if (addWorkitem) { // NBLXA-2421
							matchingWorkItems.add(aWorkItem);
							matchingWorkIds.add(aWorkItem.getID()); // SPR3104
							retrieveCrossRefWorkItems(aWorkItem); // SPR1359
						}
					}else{
						unlockWork(getUser(),aWorkItem);  //NBA213
					}
					//end SPR1784
					//SPR1784 code deleted
				}
		//NBA213 deleted code
}
/**
 * This method retrieves the matching cases found in the NbaSearchResultVO object.
 * Each case is retrieved and locked.  If unable to retrieve with lock, this work item
 * will be suspended for a brief period of time due to the auto suspend flag being set.
 * @param searchResults the results of the previous AWD lookup
 * @throws NbaBaseException NbaLockException
 */
//NBA080 New Method
public void retrieveMatchingCases(List searchResults) throws NbaBaseException {
	//NBA213 deleted code
		//SPR3129 code deleted
		if(searchResults == null){
			return;
		}
		ListIterator results = searchResults.listIterator();
		while (results.hasNext()) { //retrive only first case if available ALS3723 change the condition to loop
			NbaSearchResultVO resultVO = (NbaSearchResultVO) results.next();
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(resultVO.getWorkItemID(), true);
			retOpt.requestSources();
			retOpt.setLockWorkItem();
			//ACN026 code deleted
			NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt); //NBA213
			if (A_STATUS_BADSCAN.equalsIgnoreCase(aWorkItem.getStatus()) || A_STATUS_PERMAPPDN.equalsIgnoreCase(aWorkItem.getStatus())) {//ALS5753, APSL4407
				getIgnoredMatchingCases().add(aWorkItem);
				continue;
			} else if (isReg60PreSale() && matchingPreSaleCase(aWorkItem)) {//ALS4858 continue looping if the workitem is presale or reg60 and case is informal////ALS4741 changed the if condition
				getIgnoredMatchingCases().add(aWorkItem); //ALS5414
				continue;
			}else if (END_QUEUE.equalsIgnoreCase(aWorkItem.getQueue()) && aWorkItem.getNbaLob().getAppOriginType()== NbaOliConstants.OLI_APPORIGIN_REPLACEMENT) {//ALS3723 continue looping the matched case is in END queue
				getIgnoredMatchingCases().add(aWorkItem); //ALS5414
				continue;//ALS3723
			}else if (NbaConstants.APPPROD_TYPE_ADC.equalsIgnoreCase(aWorkItem.getNbaLob().getAppProdType())
					&& (getWorkType().equals(A_WT_TEMP_REQUIREMENT) || (getWorkType().equals(A_WT_MISC_MAIL) && !A_FRNM_ONDMND
							.equalsIgnoreCase(getWork().getNbaLob().getFormNumber())))) {//Begin APSL1068,APSL1902
				getIgnoredMatchingCases().add(aWorkItem);
				continue;
			}
            //End APSL1068
			else {
				getMatchingCases().add(aWorkItem); //SPR3129
			}
			//ALS4650 code deleted
		}
	//NBA213 deleted code
}
/**
 * Sets the requirements control source XML string
 * @param the new Requirement Control Source Xml string
 */
public void setReqCtlSrcXml(NbaXMLDecorator newReqCtlSrcXml) {
	reqCtlSrcXml = newReqCtlSrcXml;
}
/**
 * Initializes the Requirement Control Source
 * @param newReqCtrlSrc
 */
public void setReqCtrlSrc(NbaSource newReqCtrlSrc) {
	reqCtrlSrc = newReqCtrlSrc;
}
/**
 * Sets the work item status member
 * @param newWiStatus
 */
public void setWiStatus(NbaProcessStatusProvider newWiStatus) {
	wiStatus = newWiStatus;
}
/**
 * This method suspends a work item by using the work item information and
 * the supplied suspend date to populate the suspendVO.
 * @param suspendDays the number of days to suspend
 * @throws NbaBaseException
 */
public void suspendTransaction(Date reqSusDate) throws NbaBaseException {
	getLogger().logDebug("Starting suspendTransaction");//NBA044
	NbaSuspendVO suspendVO = new NbaSuspendVO();
	suspendVO.setTransactionID(getWork().getID());
	suspendVO.setActivationDate(reqSusDate);
	updateForSuspend(suspendVO);
}
/**
 * Since the work item must be suspended before it can be unlocked, this method
 * is used instead of the superclass method to update AWD.
 * <P>This method updates the work item in the AWD system, suspends the
 * work item using the supsendVO, and then unlocks the work item.
 * @param suspendVO the suspend value object created by the process to be used
 *                                  in suspending the work item.
 * @throws NbaBaseException
 */
//SPR3129 changed method visibility
protected void updateForSuspend(NbaSuspendVO suspendVO) throws NbaBaseException {
	getLogger().logDebug("Starting updateForSuspend");
	updateWork(getUser(), getWork());  //NBA213
	suspendWork(getUser(), suspendVO);  //NBA213
		//SPR3129 code deleted
}
/**
 * This method gets cross-reference information from the Requirement Control Source and,
 * for each cross-referenced work items, associates the temporary work item's source to
 * the cross-referenced work item and then updates the cross-referenced work item.
 * @throws NbaBaseException
 */
// SPR1359 Changed method signature
//SPR3129 changed method visibility
protected void updateCrossRefWorkItems()
	throws NbaBaseException {
	//NBA213 deleted code
		// SPR1359 CODE DELETED
		NbaDst tempWorkItem = (NbaDst) getTempWorkItems().get(0); // SPR1359 SPR3129
		for (int i = 0; i < getCrossReferencedWorkItems().size(); i++) { // SPR1359 SPR3129
			NbaDst crossRefWorkItem = (NbaDst) getCrossReferencedWorkItems().get(i); // SPR1359 SPR3129
			//If a cross reference workitem is part of matching workitem list, do not process it as cross reference workitem
			if(matchingWorkIds.contains(crossRefWorkItem.getID())){ //SPR3104
			    continue; //SPR3104
			} //SPR3104
			//Begin NBA130
			//SPR3129 code deleted
			NbaLob xRefLob = crossRefWorkItem.getNbaLob();
			NbaTXLife xRefHldInq = (NbaTXLife) getContracts().get(xRefLob.getPolicyNumber()); //SPR3129
			NbaLob tempLob = tempWorkItem.getNbaLob();
			xRefLob.setReqReceiptDate(tempLob.getReqReceiptDate());
			xRefLob.setReqReceiptDateTime(tempLob.getReqReceiptDateTime());
			setReqReceiptDate(tempLob.getReqReceiptDate());
			setReqReceiptDateTime(tempLob.getReqReceiptDateTime());//QC20240
			//Begin AXAL3.7.31
			if (getReqParamedSignDate() == null) {
				if (tempLob.getParamedSignDate() != null) {
					xRefLob.setParamedSignDate(tempLob.getParamedSignDate());
					setReqParamedSignDate(tempLob.getParamedSignDate());
				}
			}
			if (getDeliveryRecepitSignDate() == null) { //NBLXA-2133
				if (tempLob.getDeliveryReceiptSignDate() != null) {
					xRefLob.setDeliveryReceiptSignDate(tempLob.getDeliveryReceiptSignDate());
					setDeliveryRecepitSignDate(tempLob.getDeliveryReceiptSignDate());
				}
			}
			if (getReqProvider() == null) {
				setReqProvider(tempLob.getReqVendor());
			}
			//End AXAL3.7.31

			//NBLXA-1794 Begin
			if (getLabCollectionDate() == null) {
				if (tempLob.getLabCollectionDate() != null) {
					xRefLob.setLabCollectionDate(tempLob.getLabCollectionDate());
					setLabCollectionDate(tempLob.getLabCollectionDate());
				}
			}
			//NBLXA-1794 End

			//End NBA130
			associateSource(crossRefWorkItem, tempWorkItem);
			//Begin AXAL3.7.31
			NbaDst currentWork = getWork();
			setWork(crossRefWorkItem);
			ArrayList results = waitForAdditionalResults(crossRefWorkItem);
			if (results.size() > 0) {
				StringBuffer comment = new StringBuffer("Requirement result received but additional results (");
				for (int j = 0; j < results.size(); j++) {
					if (j > 0) {
						comment.append(",");
					}
					comment.append((String)results.get(j));
				}
				comment.append(") are expected.");
				addComment(comment.toString());
			} else {
				xRefLob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED));
				xRefLob.setReqReceiptDate(getReqReceiptDate());
				xRefLob.setReqReceiptDateTime(getReqReceiptDateTime());//QC20240
			}
			setReqStatus(xRefLob.getReqStatus());
			setWork(currentWork);
			//End AXAL3.7.31

			wiStatus = new NbaProcessStatusProvider(getUser(), crossRefWorkItem, nbaTxLife); //SPR1715
			changeStatus(crossRefWorkItem, wiStatus.getPassStatus());	//NBA097
			crossRefWorkItem.getNbaTransaction().increasePriority(wiStatus.getWIAction(), wiStatus.getWIPriority()); //NBA020
			//Begin NBA130
			RequirementInfo xRefReqInfo = xRefHldInq.getRequirementInfo(xRefLob.getReqUniqueID());
			updateRequirementInfoObject(xRefReqInfo, null);
			handleHostResponse(NbaContractAccess.doContractUpdate(xRefHldInq, crossRefWorkItem, getUser()));	//SPR3129, NBA213

			//End NBA130
			if (getResult() == null) { //SPR3129
			    updateWork(getUser(), crossRefWorkItem);  //NBA213
			    if (crossRefWorkItem.isSuspended()) {
			        NbaSuspendVO suspendVO = new NbaSuspendVO();
			        suspendVO.setTransactionID(crossRefWorkItem.getID());
			        unsuspendWork(getUser(), suspendVO);  //NBA213
			    }
			} //SPR3129
		}
	//NBA213 deleted code
}
/**
 * This method gets cross-reference information from the Requirement Control Source and,
 * retrieves them so that they can be updated later.
 * @param tempWorkItem the temporary work item containing the sources.
 * @throws NbaBaseException
 */
// SPR1359 New Method
public void retrieveCrossRefWorkItems(NbaDst tempWorkItem) throws NbaBaseException {
	//NBA213 deleted code
		if (tempWorkItem.getRequirementControlSource() != null) {
			setReqCtrlSrc(tempWorkItem.getRequirementControlSource());
			reqCtlSrcXml = new NbaXMLDecorator(reqCtrlSrc.getText());
			AutomatedProcess apProcess = reqCtlSrcXml.getAutomatedProcess(NbaConstants.PROC_REDUNDANCY_CHECK); //SPR2639
			if (apProcess != null) { //SPR2639
				if (apProcess.hasCrossReference()) { //SPR2639
				    //SPR3129 code deleted
					NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
					int count = apProcess.getCrossReference().getReqItemCount(); //SPR2639
					for (int i = 0; i < count; i++) {
						retOpt.setWorkItem(apProcess.getCrossReference().getReqItemAt(i).getAwdId(), false); //SPR2639
						retOpt.setLockWorkItem();
						NbaDst crossRefWorkItem = retrieveWorkItem(getUser(), retOpt);  //NBA213
						//Begin QC12187/APSL3284
						if(END_QUEUE.equalsIgnoreCase(crossRefWorkItem.getNbaLob().getQueue())){
							getIgnoredCrossReferencedWorkItems().add(crossRefWorkItem);
						}else{
							getCrossReferencedWorkItems().add(crossRefWorkItem);	//SPR3129
						}
						//End QC12187/APSL3284
					}
				}

			}
		}
	//NBA213 deleted code
}
/**
 * This method updates the requirement work item so that the source will
 * be added and the status updated.
 * @throws NbaBaseException
 */
public void updateRequirementWorkItems() throws NbaBaseException {
	getLogger().logDebug("Starting updateRequirementWorkItems");//NBA044
	// get the receipt date from the temporary work item
	// SPR3290 code deleted
	NbaDst perm; //SPR3129
	// SPR3290 code deleted
	for (int i = 0; i < getPermWorkItems().size(); i++) {	//SPR3129
		perm = (NbaDst) getPermWorkItems().get(i);	//SPR3129
		//NBA213 deleted code
		//Begin NBA130
		NbaLob permNbaLob = perm.getNbaLob();
		// Begin AXAL3.7.31
		// Do not receive the requirement until all expected results have arrived

		//End NBA130
		//Begin NBA080
		//we should use the permanent work to get next status from VPMS
		NbaDst currentWork = getWork();
		setWork(perm);
		ArrayList results = waitForAdditionalResults(perm);
		if (results.size() > 0) {
			StringBuffer comment = new StringBuffer("Requirement result received but additional results (");
			for (int j = 0; j < results.size(); j++) {
				if (j > 0) {
					comment.append(",");
				}
				comment.append((String)results.get(j));
			}
			comment.append(") are expected.");
			addComment(comment.toString());
		} else {
			permNbaLob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED));
			permNbaLob.setReqReceiptDate(getReqReceiptDate());
			permNbaLob.setReqReceiptDateTime(getReqReceiptDateTime());//QC20240
		}
		setReqStatus(permNbaLob.getReqStatus());
		if (getReqParamedSignDate() != null)
			permNbaLob.setParamedSignDate(getReqParamedSignDate());
		if (getLabCollectionDate() != null){ //NBLXA-1794
			permNbaLob.setLabCollectionDate(getLabCollectionDate()); //NBLXA-1794
		}
		if (getDeliveryRecepitSignDate() != null){ //NBLXA-2133
			permNbaLob.setDeliveryReceiptSignDate(getDeliveryRecepitSignDate());
		}

		// APSL3290 Begin,// APSL4898 for Amendment and Sign Illustration
		if (currentWork.getWorkType().equals(A_WT_TEMP_REQUIREMENT) &&
				( permNbaLob.getReqType() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT  // APSL4898 Begin -- When TEMPREQ comes
				|| permNbaLob.getReqType() == NbaOliConstants.OLI_REQCODE_SIGNILLUS   //for Amendment and Sign Illustration  RQVN for Reqs would ne as in TEMPREQ
				|| permNbaLob.getReqType() == NbaOliConstants.OLI_REQCODE_AMENDMENT ) && getReqProvider() != null) {
			permNbaLob.setReqVendor(getReqProvider());
		}
		// APSL3290 End
		// AXAL3.7.31 end
		//Begin NBA130
		NbaTXLife permNbaTXLife = (NbaTXLife) getContracts().get(permNbaLob.getPolicyNumber());//SPR3129
		RequirementInfo permReqInfo = permNbaTXLife.getRequirementInfo(permNbaLob.getReqUniqueID());
		if ( null == permReqInfo ) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "RequirementInfo unavailable for " + permNbaLob.getReqUniqueID(), getHostFailStatus()));
			return;
		}
		//ALS4611 - Begin
		if (permReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED) {
			setParamedSignDateInTxlife(permNbaTXLife,permReqInfo,getReqParamedSignDate());
		}
		//ALS4611 - End
		if(!NbaUtils.isBlankOrNull(permNbaLob.getProviderOrder())){ //ALII1818
			permReqInfo.setProviderOrderNum(permNbaLob.getProviderOrder());
		}
		setNbaTxLife(permNbaTXLife); //NBA211
		permNbaLob.setReqRestriction(permReqInfo.getRestrictIssueCode());//ALS5718
		updateRequirementInfoObject(permReqInfo, null);

		//Begin SR679590(APSL3215)
		if(getNbaTxLife().getNbaHolding().getAppState().longValue() ==  NbaOliConstants.OLI_USA_KS ||
				getNbaTxLife().getNbaHolding().getAppState().longValue() ==	NbaOliConstants.OLI_USA_HI) {
			if(permReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_BLOOD
					|| permReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_URINE) {
				if(!NbaUtils.isRequirementOutstanding(permReqInfo.getReqStatus())) {
					List ncfReqInfoList = getNbaTxLife().getRequirementInfoList(getNbaTxLife().getParty(permReqInfo.getAppliesToPartyID()), NbaOliConstants.OLI_REQCODE_1009800030);
					if(ncfReqInfoList != null && ncfReqInfoList.size() >0) {
						//for each Notice and consent form not received, set status to received in TxLife
						for(int req = 0; req < ncfReqInfoList.size() ; req++){
							RequirementInfo ncfReqInfo = (RequirementInfo)ncfReqInfoList.get(req);
							if(NbaUtils.isRequirementOutstanding(ncfReqInfo.getReqStatus())) {
								ncfReqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_RECEIVED);
								ncfReqInfo.setReceivedDate(new Date());
								DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(ncfReqInfo);
								reqInfoExt.setReceivedDateTime(dateFormat.format(ncfReqInfo.getReceivedDate()));
								ncfReqInfo.setActionUpdate();
								reqInfoExt.setActionUpdate();
							}
						}
						//for each Notice and consent form not received, set status to received in AWD
						updateRelatedRequirements( permReqInfo.getContractKey(), NbaOliConstants.OLI_REQCODE_1009800030);
					}
				}
			}
		}
		//End SR679590(APSL3215)

		doContractUpdate(permNbaTXLife);
		//End NBA130
		//Begin AXAL3.7.31
		if (!permNbaLob.getReqStatus().equals(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED))) {
			setWork(currentWork);
			perm.setUpdate();
			updateWork(getUser(), perm);
			return;
		}
		//AXAL3.7.31 end
		//ALS4287 code deleted
		//begin ALS3323
		wiStatus = new NbaProcessStatusProvider(getUser(), perm, permNbaTXLife,permReqInfo);
		if(currentWork.getID().equalsIgnoreCase(perm.getID())){
			//reinitialize instance variable statusProvider if
			setRequirementInfo(permReqInfo);
			statusProvider=wiStatus;
		}
		//end ALS3323
		changeStatus(perm, wiStatus.getPassStatus());	// NBA097//ALS3323
		setWork(currentWork);
		//ALS3323 code deleted
		//End NBA080
		perm.increasePriority(wiStatus.getWIAction(),wiStatus.getWIPriority()); //NBA020//ALS3323
		updateWork(getUser(), perm);  //NBA213
		if (perm.isSuspended()) {
			NbaSuspendVO suspendVO = new NbaSuspendVO();
			suspendVO.setTransactionID(perm.getID());
			unsuspendWork(getUser(), suspendVO);  //NBA213
		}
		if(permNbaLob.getReqType() == NbaOliConstants.OLI_REQCODE_MIBCHECK){ //ALII1639
			unSuspendMatchingPredictiveCase(perm);//CR1454508(APSL2681),ALII1656
		}

		//SPR3129 code deleted
		//NBA213 deleted code
	}
}


//New Method added for SR679590(APSL3215)
public void updateRelatedRequirements(String contractKey, long reqType) throws NbaBaseException{
	NbaSearchVO searchVO = new NbaSearchVO();
	searchVO.setContractNumber(contractKey);
	searchVO.getNbaLob().setReqType((int)reqType);

	NbaSearchVO searchResult = lookupWork(getUser(), searchVO);
	if(searchResult != null && searchResult.getSearchResults() !=null && searchResult.getSearchResults().size() > 0){
		ListIterator searchResultVo = searchResult.getSearchResults().listIterator();
		while (searchResultVo.hasNext()) {
			NbaSearchResultVO resultVO = (NbaSearchResultVO) searchResultVo.next();
			if(NbaUtils.isRequirementOutstanding(NbaUtils.convertStringToLong(resultVO.getNbaLob().getReqStatus()))){
				NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
				retOpt.setWorkItem(resultVO.getWorkItemID(), false);
				retOpt.requestSources();
				retOpt.setLockWorkItem();
				retOpt.setAutoSuspend();
				NbaDst aWorkItem = null;
				try{
					aWorkItem = retrieveWorkItem(getUser(), retOpt);
					aWorkItem.getNbaLob().setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED));
					aWorkItem.getNbaLob().setReqReceiptDate(new Date());
					aWorkItem.getNbaLob().setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
					NbaProcessStatusProvider wiStatus = new NbaProcessStatusProvider(getUser(), aWorkItem);
					changeStatus(aWorkItem, wiStatus.getPassStatus());
					updateWork(getUser(), aWorkItem);
					if (aWorkItem.isSuspended()) {
						NbaSuspendVO suspendVO = new NbaSuspendVO();
						suspendVO.setTransactionID(aWorkItem.getID());
						unsuspendWork(suspendVO);
					}
				}catch(NbaBaseException e){
					throw e;
				}finally{
					if(aWorkItem != null){
						unlockWork(getUser(), aWorkItem);
					}
				}
			}
		}
	}
}

/**
 * This method breaks the association between the temporary work item
 * and it's source and then changes the status of this work item to send
 * it to the end queue.
 * @throws NbaBaseException
 */
public void updateTempWorkItems() throws NbaBaseException {
	getLogger().logDebug("Starting updateTempWorkItems");//NBA044
	// break association
	NbaDst temp; //SPR3129
	ListIterator sources; //SPR3129
		for (int i = 0; i < getTempWorkItems().size(); i++) {	//SPR3129
				temp = (NbaDst) getTempWorkItems().get(i);	//SPR3129
				sources = temp.getNbaSources().listIterator();	//SPR3129
				//begin SPR2917
				while (sources.hasNext()) {
					NbaSource source = (NbaSource) sources.next();
					if(NbaConstants.A_ST_PROVIDER_RESULT.equalsIgnoreCase(source.getSource().getSourceType())){
						source.setBreakRelation();
					}
				}
				//end SPR2917
				temp.getNbaLob().setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_COMPLETED)); // OLI_REQSTAT_COMPLETED	//SPR1601
				//Begin NBA080
				if (A_WT_MISC_MAIL.equals(temp.getNbaLob().getWorkType())) {
					Map deOink = new HashMap();
					deOink.put("A_MiscMatchType", "1");
					wiStatus = new NbaProcessStatusProvider(getUser(), temp, deOink);
				} else {
					wiStatus = new NbaProcessStatusProvider(getUser(), temp, nbaTxLife); //SPR1715
				}
				//End NBA080
				changeStatus(temp, wiStatus.getPassStatus());	//NBA097
				//NBA213 deleted code
				updateWork(getUser(), temp);  //NBA213
				if (temp.isSuspended()) {
					NbaSuspendVO suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(temp.getID());
					unsuspendWork(getUser(), suspendVO);  //NBA213
				}
				//SPR3129 code deleted
				//NBA213 deleted code
		}
}

/**
 * Call VP/MS model to check if matched work item is correct for matching.
 * Returns true if passed workitem can be include in matching process.
 * @param dst the matched workitem
 * @return true if passed workitem can be include in matching process else false.
 * @throws NbaBaseException
 */
//SPR1784 New Method
protected boolean checkMatchedWorkItem(NbaDst dst)throws NbaBaseException {
	//ACN020 begin
	VpmsModelResult data = getDataFromVpms(NbaVpmsAdaptor.EP_VERIFY_MATCHED_REQ, dst);
	if(YES_VALUE.equalsIgnoreCase(getFirstResult(data))) {
		return true;
	}
	//ACN020 end
	return false;
}

/**
 * This method locates the first Result within the ResultData(s)
 * contained in the VpmsModelResult and returns the string at that
 * location. If any of the fields are null or their count is 0, it
 * returns an empty string.
 * @param VpmsModelResult contains the result from the VPMS call
 * @return String containing the value from the first Result
 */
//ACN020 New Method
protected String getFirstResult(VpmsModelResult modResult ) {
	String result = "";
	if( modResult == null || modResult.getResultData() == null || modResult.getResultDataCount() == 0 ) {
		return result;
	}
	if( modResult.getResultDataAt(0).getResult() == null || modResult.getResultDataAt(0).getResultCount() == 0) {
		return result;
	}
	return modResult.getResultDataAt(0).getResultAt(0);
}

//NBA080 deleted updateTempWorkItemLobFields method and added checkLobPresence to avoid lookup with empty LOB fields


	/**
	 * @param holdingInq
	 * @param permWorkItem
	 * @param attachmentData
	 * @throws NbaBaseException
	 */
	//ACP016 new method
	public void addLifeStyleActivity(NbaTXLife holdingInq, NbaDst permWorkItem, AttachmentData attachmentData) throws NbaBaseException{
		NbaTXLife txLifeReqResult = getReqResult(attachmentData);
		OLifE oLifeReqResult = txLifeReqResult.getOLifE();
		int relationCode = permWorkItem.getNbaLob().getReqPersonCode();
		int personSeq = 0;
		String partyId = txLifeReqResult.getPartyId(relationCode, String.valueOf(personSeq));
		Party insPartyReqResult = txLifeReqResult.getParty(partyId).getParty();

		Risk risk = insPartyReqResult.getRisk();
		int lifeStyleCount = risk.getLifeStyleActivityCount();
		LifeStyleActivity lsActivityTemp = null;
		long lsActivityType = 0;
		String oldLsActivityId = null;
		for(int i=0;i<lifeStyleCount;i++){
			lsActivityTemp = risk.getLifeStyleActivityAt(i); // SPR3290
			lsActivityType = lsActivityTemp.getLifeStyleActivityType();
			if(lsActivityType == NbaOliConstants.OLI_LIFEACTTYPE_AVIATION ||
			 lsActivityType == NbaOliConstants.OLI_LIFEACTTYPE_BALLOON ||
			 lsActivityType == NbaOliConstants.OLI_LIFEACTTYPE_HANGGLIDE ||
			 lsActivityType == NbaOliConstants.OLI_LIFEACTTYPE_ULTRALITE){

				Party insPartyHolding = getInsuredParty(permWorkItem,holdingInq);
				if(existsLifeStyleActivity(insPartyHolding,lsActivityType)){
					continue;
				}
				NbaOLifEId nbaOLifEId = new NbaOLifEId(holdingInq);
				oldLsActivityId = lsActivityTemp.getId();
				lsActivityTemp.setId(null);
				nbaOLifEId.setId(lsActivityTemp);
				lsActivityTemp.setActionAdd();
				Risk riskHolding = insPartyHolding.getRisk();
				riskHolding.addLifeStyleActivity(lsActivityTemp);
				addFormInstances(holdingInq,oLifeReqResult,oldLsActivityId,lsActivityTemp.getId());
			}
		}
	}

	/**
	 * @param holdingInq
	 * @param oLifeReqResult
	 * @param lsActivityId
	 */
	//ACP016 new method
	public void addFormInstances(NbaTXLife holdingInq, OLifE oLifeReqResult, String oldLsActivityId, String lsActivityId){
		int formInstanceCount = oLifeReqResult.getFormInstanceCount();
		FormInstance formInstance = null;
		OLifE primaryOLife = null;
		NbaOLifEId nbaOLifeId = null;
		for(int i=0;i<formInstanceCount;i++){
			formInstance = oLifeReqResult.getFormInstanceAt(i); // SPR3290
			if(oldLsActivityId.equalsIgnoreCase(formInstance.getRelatedObjectID())){
				primaryOLife = holdingInq.getOLifE();
				formInstance.setId(null);
				nbaOLifeId = new NbaOLifEId(holdingInq);
				formInstance.setActionAdd();
				formInstance.setRelatedObjectID(lsActivityId);
				nbaOLifeId.setId(formInstance);
				primaryOLife.addFormInstance(formInstance);
			}
		}
	}

	/**
	 * @param party
	 * @param lsActivityId
	 * @return boolean
	 */
	//ACP016 new method
	public boolean existsLifeStyleActivity(Party party, long lsActivityId){
		Risk risk = party.getRisk();
		if(risk != null){
			int lsCount = risk.getLifeStyleActivityCount();
			LifeStyleActivity lsActivity = null;
			for(int i=0;i<lsCount;i++){
				lsActivity = risk.getLifeStyleActivityAt(i);
				if(lsActivity.getLifeStyleActivityType() == lsActivityId){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param permWorkItem
	 * @param holdingInq
	 * @return Party
	 * @throws NbaBaseException
	 */
	//ACP016 new method
	protected Party getInsuredParty(NbaDst permWorkItem, NbaTXLife holdingInq) throws NbaBaseException{
		int relationCode = permWorkItem.getNbaLob().getReqPersonCode();
		int personSeq = permWorkItem.getNbaLob().getReqPersonSeq();
		String partyId = holdingInq.getPartyId(relationCode, String.valueOf(personSeq));
		Party party  = holdingInq.getParty(partyId).getParty();
		return party;
	}

	/**
	 * @param attachmentData
	 * @return NbaTXLife
	 * @throws NbaBaseException
	 */
	//ACP016 new method
	protected NbaTXLife getReqResult(AttachmentData attachmentData) throws NbaBaseException{
		NbaTXLife txLifeReqResult = null;
		if (attachmentData != null && attachmentData.hasPCDATA()) {
			String pcData = attachmentData.getPCDATA();
			// SPR3290 code deleted
			try {
				txLifeReqResult = new NbaTXLife(pcData);
			} catch (Exception e) {
				throw new NbaBaseException(e);
			}
		}
		return txLifeReqResult;
	}

	//NBA192 code deleted

	/**
	 * Iterate thru unsuspendVO list and call unsuspendWork method
	 * on netserveraccessor bean for each value object.
	 * @throws NbaBaseException
	 */
	//SPR2565 New Method
	protected void unsuspendWorkitems() throws NbaBaseException {
		int size = unsuspendVOs.size();
		for (int i = 0; i < size; i++) {
			unsuspendWork((NbaSuspendVO) unsuspendVOs.get(i));
		}
	}

	/**
	 * This method unlocks any temp work items retrieved by the lookup process
	 * @throws NbaBaseException
	 */
	// SPR2803 NEW METHOD
	//SPR32129 change method visibility
	protected void unlockTempWorkItems() throws NbaBaseException {
		getLogger().logDebug("Starting unlockTempWorkItems");
		for (int i = 0; i < getTempWorkItems().size(); i++) {	//SPR3129
			NbaDst temp = (NbaDst) getTempWorkItems().get(i);	//SPR3129
			//NBA213 deleted code
			//NBA208-32
			if (!temp.getTransaction().getItemID().equals(getWork().getTransaction().getItemID())) {
				unlockWork(getUser(), temp);  //NBA213
			}
			//NBA213 deleted code
		}
	}

	/**
	 * This method unlocks any permanent work items retrieved by the lookup process
	 * @throws NbaBaseException
	 */
	// SPR2803 NEW METHOD
	//SPR3129 change method visibility
	protected void unlockRequirementWorkItems() throws NbaBaseException {
		getLogger().logDebug("Starting updateRequirementWorkItems");//NBA044
		for (int i = 0; i < getPermWorkItems().size(); i++) {	//SPR3129
			NbaDst perm = (NbaDst) getPermWorkItems().get(i);	//SPR3129
			//NBA213 deleted code
			//NBA208-32
			if(!perm.getTransaction().getItemID().equals(getWork().getTransaction().getItemID())) {
				unlockWork(getUser(), perm);  //NBA213
			}
			//NBA213 deleted code
		}
	}
	/**
	 * Answers the reqReceiptDate.
	 * @return the reqReceiptDate.
	 */
	//NBA130 New Method
	public Date getReqReceiptDate() {
		//ALPC234 code deleted
          return reqReceiptDate;
		}
	/**
	 * Sets the reqReceiptDate
	 * @param the reqReceiptDate to set.
	 */
	//NBA130 New Method
	public void setReqReceiptDate(Date reqReceiptDate) {
		this.reqReceiptDate = reqReceiptDate;
	}

	/**
	 * Answers the labCollectionDate.
	 * @return the labCollectionDate.
	 */
	//NBLXA-1794 New Method
	public Date getLabCollectionDate() {
		//ALPC234 code deleted
          return labCollectionDate;
		}
	/**
	 * Sets the labCollectionDate
	 * @param the labCollectionDate to set.
	 */
	//NBLXA-1794 New Method
	public void setLabCollectionDate(Date labCollectionDate) {
		this.labCollectionDate = labCollectionDate;
	}


	/**
	 * If the reqStatusDate is null, this method initializes it with
	 * the current date; otherwise, it returns the value in reqStatusDate.
	 * @return the reqStatusDate.
	 */
	public Date getReqStatusDate() {
		if( reqStatusDate == null) {
			reqStatusDate = new Date();
		}
		return reqStatusDate;
	}
    /**
     * Return the field which indicates that at least one lookup was performed
     * @return lookupDataFound.
     */
	// SPR2697 New Method
    protected boolean lookupDataWasFound() {
        return lookupDataFound;
    }
    /**
     * Set the field which indicates that at least one lookup was performed
     * @param lookupDataFound - the lookupDataFound to set.
     */
    // SPR2697 New Method
    protected void setLookupDataFound(boolean lookupDataFound) {
        this.lookupDataFound = lookupDataFound;
    }
    /**
     * Create and initialize an <code>NbaSearchVO</code> object to find any matching work items.
     * Call the Requirements VP/MS model to get the criteria (sets of LOB fields) to be used in the search.
     * Different criteria is applicable depending on whether a Transaction or Case is being searched for.
     * The sets are iterated over until a successful search is performed.
     * For each search, the LOB values identifed in the set are copied from the work item to the SearchVo. Then the LOB
     * values are examined to verify that values for all LOBs were present on the work item. If not, the set is bypassed.
     * For each search, the worktypes to search against are determined by a VPMS model. The worktypes vary based on
     * whether a Transaction or Case is being searched for.
     * If a successful search is performed, the work item referenced in the NbaSearchResultVO object are retrieved.
     * @return the search value object containing the results of the search
     * @throws NbaBaseException
     */
    // SPR2697 New Method
    protected NbaSearchVO lookup(String type) throws NbaBaseException, NbaVpmsException, NbaNetServerDataNotFoundException {
        try {
            NbaSearchVO searchVO = new NbaSearchVO();
            List workItemTypes = determineLookupWorkTypes(type); // call model to determine work types to search for
            List criteraSets = determineLookupCritera(type); // call model to retrieve criteria sets
            if (TRANSACTION.equals(type)) {
                searchVO.setResultClassName(TRANSACTION_SEARCH_VO);
            } else {
                searchVO.setResultClassName(CASE_SEARCH_VO);
            }
            searchVO = performSearch(searchVO, workItemTypes, criteraSets);	//Perform searches
            //Retrieve the work items referenced in the NbaSearchResultVO
            if (TRANSACTION.equals(type)) {
                retrieveMatchingWorkItems(searchVO.getSearchResults());
            } else {
                retrieveMatchingCases(searchVO.getSearchResults());
            }
	        if (searchVO.isMaxResultsExceeded()){	//NBA146
	            throw new NbaBaseException(NbaBaseException.SEARCH_RESULT_EXCEEDED_SIZE, NbaExceptionType.FATAL);	//NBA146
	        }	//NBA146
            return searchVO;
        } catch (java.rmi.RemoteException re) {
            throw new NbaBaseException(PROCESS_PROBLEM, re);
        }
    }

    /**
     * Call a model to determine the work types to search for
     * @param type - indicates whether the model is to return Transaction or Case work types
     * @return a List containing the work types
     * @throws NbaBaseException
     * @throws NbaVpmsException
     */
    // SPR2697 New Method
    protected List determineLookupWorkTypes(String type) throws NbaVpmsException, NbaBaseException {
        List types = new ArrayList();
        NbaVpmsModelResult nbaVpmsModelResult = null;
        if (TRANSACTION.equals(type)) {
            nbaVpmsModelResult = getXmlDataFromVpms(NbaVpmsAdaptor.EP_GET_WORK_TYPE_FOR_TRANSACTION_MATCH);
        } else {
            nbaVpmsModelResult = getXmlDataFromVpms(NbaVpmsAdaptor.EP_GET_WORK_TYPE_FOR_CASE_MATCH);
        }
        ResultData resultData;
        int resultDataCount = nbaVpmsModelResult.getVpmsModelResult().getResultDataCount();
        for (int i = 0; i < resultDataCount; i++) {
            resultData = nbaVpmsModelResult.getVpmsModelResult().getResultDataAt(i); //Try next set
            for (int j = 0; j < resultData.getResultCount(); j++) {
            	types.add(new RequirementVpmsResult(resultData.getResultAt(j))); //AXAL3.7.20G
            }
        }
        if (getLogger().isDebugEnabled()) {
            StringBuffer buff = new StringBuffer();
            buff.append("Search will include ");
            int count = types.size();
            for (int i = 0; i < count; i++) {
                buff.append(((RequirementVpmsResult) types.get(i)).getWorkType()); //AXAL3.7.20G
                if (i < (count - 1)) {
                    buff.append(", ");
                }
            }
            buff.append(" work types");
            getLogger().logDebug(buff.toString());
        }
        return types;
    }
    /**
     * Call a model to retrieve the search criteria sets (LOBs to be compared). The results
     * are returned as an array with an entry for each set. Each entry set contains the ids for the LOBs.
     * @param type - indicates whether the model is to return criteria sets for Transaction or Case work types
     * @return a List containing the search criteria sets
     * @throws NbaBaseException
     * @throws NbaVpmsException
     */
    // SPR2697 New Method
    protected List determineLookupCritera(String type) throws NbaVpmsException, NbaBaseException {
        List criteriaSets = new ArrayList();
        NbaVpmsModelResult resultSets = null;
        if (TRANSACTION.equals(type)) {
            resultSets = getXmlDataFromVpms(NbaVpmsAdaptor.EP_GET_MATCHING_SEARCH_CRITERIA);
        } else {
            resultSets = getXmlDataFromVpms(NbaVpmsAdaptor.EP_GET_CASE_MATCHING_SEARCH_CRITERIA);
        }
        int setCount = resultSets.getVpmsModelResult().getResultDataCount();
        ResultData searchLOBs;
        ArrayList lobList;
        for (int i = 0; i < setCount; i++) {
            searchLOBs = resultSets.getVpmsModelResult().getResultDataAt(i); //Next set
            lobList = new ArrayList();
            for (int j = 0; j < searchLOBs.getResultCount(); j++) {
                lobList.add(searchLOBs.getResultAt(j));
            }
            criteriaSets.add(lobList);
        }
        return criteriaSets;
    }
    /**
     * Search for matching work items.
     * Iterate over the criteria (sets of LOB fields) to be used in the search until a successfull search is performed.
     * For each set, try each work type in the workItemTypes list.
     * For each search, the LOB values identifed in the criteria set are copied from the work item to the SearchVo. Then the LOB
     * values are examined to verify that values for all LOBs were present on the work item. If not, the set is bypassed.
     * @return the search value object containing the results of the search
     * @throws RemoteException
     * @throws NbaBaseException
     */
    // SPR2697 New Method
    protected NbaSearchVO performSearch(NbaSearchVO searchVO, List workItemTypes, List criteraSets) throws RemoteException, NbaBaseException {
        int setCount = criteraSets.size();
        int workItemCount = workItemTypes.size();
        // SPR3290 code deleted
        // Begin AXAL3.7.31
        List reqTypes = null;
        int reqTypeCount = 0;
        if (searchVO.getResultClassName().equals(TRANSACTION_SEARCH_VO)) {
            reqTypes = determineLookupReqTypes();	// call model to determine req types to search for
        } else {
        	reqTypes = new ArrayList();
        	reqTypes.add("");
        }
        reqTypeCount = reqTypes.size();
        // AXAL3.7.31 end
        ArrayList lobList;
        main: for (int i = 0; i < setCount; i++) {
            lobList = (ArrayList) criteraSets.get(i);
            if (getLogger().isDebugEnabled()) {
                StringBuffer buff = new StringBuffer();
                buff.append("Performing search for criteria set number ").append((i + 1)).append(" of ").append(setCount).append(" with criteria: ");
                int count = lobList.size();
                for (int x = 0; x < count; x++) {
                    buff.append((String) lobList.get(x));
                    if (x < (count - 1)) {
                        buff.append(", ");
                    }
                }
                getLogger().logDebug(buff.toString());
            }

            NbaLob lookupLOBs = getNbaLobForLookup(lobList);
            if (checkLobPresence(lookupLOBs, lobList)) { //Perform a search only if all LOB values are present on work.
                setLookupDataFound(true);
                for (int j = 0; j < workItemCount; j++) { //Try each work type
                	// Begin AXAL3.7.31
                	searchVO.setWorkType(((RequirementVpmsResult) workItemTypes.get(j)).getWorkType()); //AXAL3.7.20G
                    searchVO.setQueue(((RequirementVpmsResult) workItemTypes.get(j)).getQueue()); //AXAL3.7.20G
                    for (int k = 0; k < reqTypeCount; k++) {	// Try each req type
                    	if (lobList.contains("ReqTypeLOB")) {
                    		String sReqType = (String)reqTypes.get(k);
                    		lookupLOBs.setReqType(Integer.parseInt(sReqType));
                    	}
                        searchVO.setNbaLob(lookupLOBs.clone(true)); //clone to prevent orig from getting values from setReqStatus() method call below


                    // When searchiing for temporary requirements, only consider temporary requirement work items with a status of "RECEIVED".
                    // After a temporary requirement work item is successfully matched, it's status is changed from "RECEIVED" to "COMPLETED" to move it to the end queue.
                    // Checking for a status of "RECEIVED" prevents any temporary requirement work items that have already been processed from being
                    // returned as possibilities for a match.
                    if (A_WT_TEMP_REQUIREMENT.equals(searchVO.getWorkType())) {
                        searchVO.getNbaLob().setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED));
                    }
                    searchVO = lookupWork(getUser(), searchVO);  //NBA213
                    if (!(searchVO.getSearchResults().isEmpty())) {
                    	matchedCriteria = lobList.toString(); //ALII2077
                        break main;
                    }
                }
                    // End AXAL3.7.31
            }
        }
        }
        return searchVO;
    }

    /**
     * Returns the contracts
     * @return contracts.
     */
     //SPR3129 New Method
    protected Map getContracts() {
        return contracts;
    }

    /**
     * Retrieve a contract
     * @param dst
     * @throws NbaBaseException
     */
    //SPR3129 New Method
    protected void retrieveContract(NbaDst dst) throws NbaBaseException {
        NbaContractLock.requestLock(dst, getUser()); //throws NbaLockedException if cannot obtain a lock
        NbaTXLife nbaTXLife;
        String businessProcess = NbaUtils.getBusinessProcessId(getUser());
        try {
            nbaTXLife = doHoldingInquiry(dst, getContractAccess(), businessProcess);
        } catch (NbaDataAccessException e1) {
            addComment("Unable to retrieve contract " + e1.getMessage());
            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e1.getMessage(), getFailStatus()));
            return;
        }
        handleHostResponse(nbaTXLife);
        if (getResult() == null) {
            getContracts().put(dst.getNbaLob().getPolicyNumber(), nbaTXLife);
        }
    }
    /**
     * Retrieve all contracts identified in the permanent and cross reference work item lists
     * @param dst
     * @throws NbaBaseException
     */
    //SPR3129 New Method
    protected void retrieveContractsForMatchingWorkItems() throws NbaBaseException {
        List workItems = new ArrayList();
        if (A_WT_REQUIREMENT.equals(getWork().getTransaction().getWorkType())) {
            getPermWorkItems().add(getWork());
            setTempWorkItems(getMatchingWorkItems());
        } else {
            getTempWorkItems().add(getWork());
            setPermWorkItems(getMatchingWorkItems());
        }
        workItems.addAll(getPermWorkItems());
        workItems.addAll(getCrossReferencedWorkItems());
        int workItemSize = workItems.size(); // SPR3290
        for (int i = 0; i < workItemSize; i++) {
            retrieveContract((NbaDst) workItems.get(i));
            if (getResult() != null) {
                break;
            }
        }
    }

    /**
     * Returns the matchingWorkItems
     * @return matchingWorkItems
     */
    //SPR3129 New Method
    protected List getMatchingWorkItems() {
        return matchingWorkItems;
    }
    /**
     * Set the matchingWorkItems
     * @param matchingWorkItems the matchingWorkItems to set.
     */
    //SPR3129 New Method
    protected void setMatchingWorkItems(List items) {
        matchingWorkItems = items;
    }
    /**
     * Returns the crossReferencedWorkItems
     * @return crossReferencedWorkItems
     */
    //SPR3129 New Method
    protected List getCrossReferencedWorkItems() {
        return crossReferencedWorkItems;
    }
    /**
	 * @return the ignoredCrossReferencedWorkItems
	 */
    //QC12187/APSL3284 New Method
	protected List getIgnoredCrossReferencedWorkItems() {
		return ignoredCrossReferencedWorkItems;
	}
    /**
     * Returns the matchingCases
     * @return matchingCases
     */
    //SPR3129 New Method
    protected List getMatchingCases() {
        return matchingCases;
    }
	/**
	 * Unlock all work items retrieved during processing
	 * @throws NbaBaseException
	 */
	// SPR3129 New Method
    protected void unlockWorkItems() throws NbaBaseException {
		getLogger().logDebug("Unlocking Work Items");
		unlockTempWorkItems();
		unlockRequirementWorkItems();
		unlockCrossReferencedWorkItems();
		unlockIgnoredCrossReferencedWorkItems();//QC12187/APSL3284
		if (getMatchingCases().size() > 0) {
			//NBA213 deleted code
			int listSize = getMatchingCases().size();//ALS5753
			for (int j = 0; j < listSize; j++) {//ALS5753
				String userID = getUser().getUserID();
				NbaDst dst = (NbaDst) getMatchingCases().get(j);
				NbaTransaction trx;
				List trxs = dst.getNbaTransactions();
				int count = trxs.size();
				String getWorkID = getWork().getWorkItem().getItemID(); //prevent duplicate unlock if main work
				for (int i = 0; i < count; i++) {
					trx = (NbaTransaction) trxs.get(i);
					if (trx.isLocked(userID) && !(trx.getWorkItem().getItemID().equalsIgnoreCase(getWorkID))) { //prevent unlock dupe
						trx.setUnlock(userID);
					}
				}
				//Begin ALS4995
				if (dst.isLocked(userID)) {
					unlockWork(getUser(), dst); //NBA213
				}
				//End ALS4995
				//NBA213 deleted code
			}//ALS5753
		}
	}
	/**
	 * Unlock any cross reference work items
	 * @throws NbaBaseException
	 */
	//SPR3129 New Method
	protected void unlockCrossReferencedWorkItems() throws NbaBaseException {
        for (int i = 0; i < getCrossReferencedWorkItems().size(); i++) {
            NbaDst temp = (NbaDst) getCrossReferencedWorkItems().get(i);
                unlockWork(getUser(), temp);  //NBA213
        }
    }

	/**
	 * Unlock any ignored cross reference work items
	 * @throws NbaBaseException
	 */
	//QC12187/APSL3284 New Method
	protected void unlockIgnoredCrossReferencedWorkItems() throws NbaBaseException {
        for (int i = 0; i < getIgnoredCrossReferencedWorkItems().size(); i++) {
            NbaDst temp = (NbaDst) getIgnoredCrossReferencedWorkItems().get(i);
                unlockWork(getUser(), temp);  //NBA213
        }
    }
	/**
	 * Calculates the activation date for the number of suspend days and then suspends the work item.
	 * It also update the followup date with the activation date
	 * @param suspendDays the number of days for the work to be suspended
	 */
	//NBA192 New Method
	protected void suspendUnmatchedWorkitem(int suspendDays) throws NbaBaseException {

		GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_WEEK, suspendDays);
        Date reqSusDate = (calendar.getTime());
        setFollowupdate(calendar, suspendDays);//ALS4843
        //Update the Requirement Control Source to indicate that the work item was suspended by this process
        if (null != reqCtlSrcXml) { //ALS5546
        	NbaRequirementUtils reqUtils = new NbaRequirementUtils();
        	AutomatedProcess ap = reqCtlSrcXml.getAutomatedProcess(NbaUtils.getBusinessProcessId(getUser()));
        	ap.setSuspendDate(reqSusDate);
        	reqUtils.updateRequirementControlSource(null, work.getNbaTransaction(), getReqCtlSrcXml().toXmlString(), NbaRequirementUtils.actionUpdate);
        } //ALS5546
        //Suspend the work item
        addComment("Suspended awaiting matching work item");
        suspendTransaction(reqSusDate);
    }

    /**
	 * @param calendar
	 * @throws NbaBaseException
	 */
	//ALS4843/ALS5113 method signature modified
	protected void setFollowupdate(GregorianCalendar calendar, int suspendDays) throws NbaBaseException {
		if (A_WT_REQUIREMENT.equals(getWorkType())) {
			String businessProcess = NbaUtils.getBusinessProcessId(getUser());
			NbaContractLock.requestLock(getWork(), getUser()); //throws NbaLockedException if cannot obtain a lock //ALS4606
			NbaTXLife nbaTXLife = doHoldingInquiry(getWork(), getContractAccess(), businessProcess);
			if (getWork().getNbaLob().getReqUniqueID() != null) {
				RequirementInfo requirementInfo = nbaTXLife.getRequirementInfo(getWork().getNbaLob().getReqUniqueID());
				RequirementInfoExtension extension = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
				if (extension == null) {//Begin ALS2895
					OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
					extension = olifeExt.getRequirementInfoExtension();
				}//End ALS2895
				//NBLXA-2072 Start
				NbaLob lob = work.getNbaLob();
				if (lob != null && isMVRRequirementForLexisNexis(lob) && lob.getSuspensionCount() != null
						&& lob.getSuspensionCount().equalsIgnoreCase(NbaConstants.LOB_SUSPENSION_COUNT_02) && actualSuspendDays != 0) {
						extension.setFollowUpFreq(actualSuspendDays);
				} else {
					extension.setFollowUpFreq(suspendDays);
				}
				//extension.setFollowUpFreq(suspendDays);
				//NBLXA-2072 End
				extension.setFollowUpDate(calendar.getTime());
				extension.setActionUpdate(); //AXAL3.7.06
				doContractUpdate(nbaTXLife); //AXAL3.7.06
			}
			NbaContractLock.removeLock(getWork(), getUser());  //ALS4606
		}

	}

	/**
	 * Returns the permWorkItems
	 *
	 * @return permWorkItems
	 */
	//SPR3129 New Method
    protected List getPermWorkItems() {
        return permWorkItems;
    }
    /**
     * Set the permWorkItems
     * @param permWorkItems the permWorkItems to set.
     */
    //SPR3129 New Method
    protected void setPermWorkItems(List permWorkItems) {
        this.permWorkItems = permWorkItems;    }

    /**
     * Returns the tempWorkItems
     * @return tempWorkItems
     */
    //SPR3129 New Method
    protected List getTempWorkItems() {
        return tempWorkItems;
    }
    /**
     * Set the tempWorkItems
     * @param tempWorkItems the tempWorkItems to set.
     */
    //SPR3129 New Method
    protected void setTempWorkItems(List tempWorkItems) {
        this.tempWorkItems = tempWorkItems;
    }

    //ALS4420 code deleted - moved to Requirementutils

	//Reset the auto process status fields if MISCMAIL.
	//AXAL3.7.06
	protected void reinitializeFields(NbaUserVO newUser, NbaDst newWork)
		throws NbaBaseException, NbaNetServerDataNotFoundException, NbaNetServerException {
	 	if (work.getTransaction().getWorkType().equals(A_WT_MISC_MAIL)) {

	 		setStatusProvider(new NbaProcessStatusProvider(user, newWork, getDeOinkMapForSources(newWork)));
	 	}

	}
	//If a MISCMAIL source, set deOink map
	//AXAL3.7.06
	protected Map getDeOinkMapForSources(NbaDst work){

		Map deOinkMap = new HashMap();
		List sourceList = work.getSources();
		int count = sourceList.size();
		NbaSource aSource = null;
		for (int i = 0; i < count; i++) {
			aSource = (NbaSource) work.getNbaSources().get(i);
			if (aSource.getSource().getSourceType().equals(NbaConstants.A_WT_MISC_MAIL)) {
				break;
			}
			aSource = null;
		}
		if (null != aSource) {
			deOinkMap.put("A_CreateStationLOB", aSource.getNbaLob().getCreateStation()); //ALS2872
		}
		//begin NBA231
		if (hasParentInd) {
    		deOinkMap.put("A_HasParentInd","true");
    	} else {
    		deOinkMap.put("A_HasParentInd","false");
    	}
		//end NBA231
		return deOinkMap;
	}
	/**
	 * Invoke VPMS to get new NBAPPLCTN worktype and status. Then copy information to new app
	 * Once that is done, route Misc. Mail work item to next queue.
	 */
	//NBA231 new method
	protected void generateApplication() throws NbaBaseException {

		Map deOink = new HashMap();
		deOink.put("A_FormType","2");
		deOink.put("A_PresaleProcess","true"); //ALS4858
		//begin AXAL3.7.53
		List sourceList = getWork().getNbaSources();
		int count = sourceList.size();
		NbaSource aSource = null;
		String sourceType = null;
		for (int i = 0; i < count; i++) {
			aSource = (NbaSource) sourceList.get(i);
			sourceType = aSource.getSource().getSourceType();
			if (NbaConstants.A_WT_MISC_MAIL.equals(sourceType)) {
				deOink.put("A_CreateStationLOB", aSource.getNbaLob().getCreateStation());
				break;
			}
		}
		//end AXAL3.7.53
		NbaProcessWorkItemProvider wip = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOink);
		String workType = wip.getWorkType();
		NbaDst newDst = new NbaDst();
		WorkItem awdCase = new WorkItem();
		awdCase.setBusinessArea("NBALIFE");
        awdCase.setWorkType(workType);
        awdCase.setStatus(wip.getInitialStatus());
        //NBA208-32
        awdCase.setLock("Y");
        awdCase.setAction(ACTION_LOCK);
        awdCase.setRecordType(WT_CASE);
        //NBA208-32
        awdCase.setCreate("Y");
        newDst.addCase(awdCase);
        copyLOBs(newDst);
        copySources(newDst);
        updateWork(getUser(),newDst);
        unlockWork(getUser(), newDst);

	}

	   /**
     * Call a model to retrieve the requirement types to be searched. The results
     * are returned as an array of arrays containing lists of requirement types.
     * @return a List containing the search requirement types
     * @throws NbaBaseException
     * @throws NbaVpmsException
     */
    // AXAL3.7.31 New Method
    protected List determineLookupReqTypes() throws NbaVpmsException, NbaBaseException {
        NbaVpmsModelResult resultSets = getXmlDataFromVpms(NbaVpmsAdaptor.EP_GET_REQUIREMENT_TYPE_ALIASES);
        int setCount = resultSets.getVpmsModelResult().getResultDataCount();
        ResultData searchReqTypes;
        ArrayList reqList = new ArrayList();
        for (int i = 0; i < setCount; i++) {
            searchReqTypes = resultSets.getVpmsModelResult().getResultDataAt(i); //Next set
            for (int j = 0; j < searchReqTypes.getResultCount(); j++) {
                reqList.add(searchReqTypes.getResultAt(j));
            }
        }
        return reqList;
    }
    /**
     * Determine whether the results received to date are sufficient to fulfill the requirement.
     * @param permWork The permanent work item containing the results received thus far
     * @return ArrayList Additional results that are still expected
     * @throws NbaVpmsException
     * @throws NbaBaseException
     */
    // AXAL3.7.31 New Method
    protected ArrayList waitForAdditionalResults(NbaDst permWork) throws NbaVpmsException, NbaBaseException {
    	ArrayList waitForAdditionalResults = new ArrayList();
        NbaVpmsModelResult resultsExpected = getXmlDataFromVpms(NbaVpmsAdaptor.EP_GET_REQUIREMENT_RESULT_COUNTS);
        int setCount = resultsExpected.getVpmsModelResult().getResultDataCount();
        ResultData resultExpected;
        String sourceTypeExpected;
        int countExpected;
        int actualCount;
        for (int i = 0; i < setCount; i++) {
            resultExpected = resultsExpected.getVpmsModelResult().getResultDataAt(i); //Next set
            if (resultExpected.getResultCount() < 2) {
                throw new NbaBaseException("VPMS error while computing expected results");
            }
            sourceTypeExpected = resultExpected.getResultAt(0);
            countExpected = Integer.parseInt(resultExpected.getResultAt(1));
            actualCount = countSources(permWork, sourceTypeExpected);
            if (countExpected > actualCount) {
            	waitForAdditionalResults.add(String.valueOf(countExpected - actualCount) + " " + sourceTypeExpected);
            }
        }
    	return waitForAdditionalResults;
    }
    /**
     * Count the number of sources of a given type on a given work item.
     * @param work The work item to inspect
     * @param sourceType The source type to search for
     * @return int The number of sources on the work item
     */
    //AXAL3.7.31 New Method
    protected int countSources(NbaDst work, String sourceType) throws NbaBaseException {
    	int count = 0;
    	WorkItemSource source;
    	for (int i = 0; i < work.getSources().size(); i++) {
    		source = (WorkItemSource)work.getSources().get(i);
    		if (source.getSourceType().equals(sourceType)) {
    			if (sourceType.equals(NbaConstants.A_ST_PROVIDER_SUPPLEMENT)) {
    				// Only count NBPROVSUPP if it is a valid data result
    				String response = source.getText();
    				if (response != null && response.trim().length() > 0) {
    					try {
    						NbaTXLife txLife = new NbaTXLife(response);
        					if (txLife != null && AxaUtils.isDataResult(txLife)) {
        						count++;
        					}
    					} catch (Exception e) {
    						throw new NbaBaseException("Provider response source is missing or invalid", e);
    					}
    				}
    			} else {
        			count++;
    			}
    		}
    	}
    	return count;
    }
    /**
     * Retrieves the current ParamedSignDate.
     * @return Date
     */
    //AXAL3.7.31 New Method
	public Date getReqParamedSignDate() {
		return reqParamedSignDate;
	}
	/**
	 * Sets the current ParamedSignDate.
	 * @param aDate
	 */
	//AXAL3.7.31 New Method
	public void setReqParamedSignDate(Date aDate) {
		reqParamedSignDate = aDate;
	}

	/**
	 * Retrieves the scenario code.
	 * @return String
	 */
	//AXAL3.7.31 New Method
	public String getScenarioCode() {
		return scenarioCode == null ? SINGLE_REQ : scenarioCode;
	}

	/**
	 * Sets the scenario code.
	 * @param aScenario
	 */
	//AXAL3.7.31 New Method
	public void setScenarioCode(String aScenario) {
		scenarioCode = aScenario;
	}

    /**
     * Retrieves the current provider.
     * @return String
     */
    //AXAL3.7.31 New Method
	public String getReqProvider() {
		return reqProvider;
	}
	/**
	 * Sets the current provider.
	 * @param String
	 */
	//AXAL3.7.31 New Method
	public void setReqProvider(String aProvider) {
		reqProvider = aProvider;
	}
    /**
     * Retrieves the current requirement status.
     * @return String
     */
    //AXAL3.7.31 New Method
	public String getReqStatus() {
		return reqStatus;
	}
	/**
	 * Sets the current requirement status.
	 * @param String
	 */
	//AXAL3.7.31 New Method
	public void setReqStatus(String aValue) {
		reqStatus = aValue;
	}
	/**
	 * Determines whether the source is a valid one.  Some providers send transactions containing
	 * blanks or false receipts, which need to be discarded.
	 * @param aSource
	 * @return
	 * @throws NbaBaseException
	 */
	//AXAL3.7.31 New Method
	protected boolean validSource(WorkItemSource aSource) throws NbaBaseException {
		boolean valid = false;
		if (aSource.getSourceType().equalsIgnoreCase(NbaConstants.A_ST_PROVIDER_SUPPLEMENT)) {
			// Verify NBPROVSUPP is a valid data result.
			// Some providers will send "blank" transactions, which we should ignore.
			String response = aSource.getText();
			if (response != null && response.trim().length() > 0) {
				try {
					NbaTXLife txLife = new NbaTXLife(response);
					if (txLife != null) {
						if (AxaUtils.isDataResult(txLife)) {
    						valid = true;
						}
						if (txLife.getPolicy().getRequirementInfoCount() > 1) {
							setScenarioCode(MULTIPLE_REQS);
						}
					}
				} catch (Exception e) {
					throw new NbaBaseException("Provider response source is missing or invalid", e);
				}
			}
		} else {
			valid = true;
		}
		return valid;
	}
	//NBA231 new method
	protected void copySources(NbaDst newDst) throws NbaBaseException {
		for (int i = 0; i < getWork().getNbaSources().size(); i++) {
			NbaSource aSource = (NbaSource) getWork().getNbaSources().get(i);
			setDefaults(aSource.getNbaLob());	//ALS2746
			newDst.addNbaSource(aSource);
			aSource.setBreakRelation();
		}

	}
	//NBA231 new method
	protected void copyLOBs(NbaDst newDst) {
		try {
		NbaLob newLob = newDst.getNbaLob();
		NbaLob wrkLob = getWork().getNbaLob();
		try {
			newLob.setBackendSystem(NbaServerUtility.getBackendSystem(wrkLob));

		} catch (NbaBaseException nbe) {
			//do nothing as plan code might be missing, in which case VPMS will default
		}
		setDefaults(newLob);

		newLob.setLastName(wrkLob.getLastName());
		newLob.setFirstName(wrkLob.getFirstName());
		newLob.setMiddleInitial(wrkLob.getMiddleInitial()); //APSL442
		newLob.setDOB(wrkLob.getDOB());
		newLob.setGender(wrkLob.getGender());
		newLob.setSsnTin(wrkLob.getSsnTin());
		newLob.setAppOriginType(NbaOliConstants.OLI_APPORIGIN_REPLACEMENT); //REISSUE = REPLACEMENT//ALII1206
		newLob.setOperatingMode(NbaServerUtility.getDataStore(newLob,getUser()));

		} catch (NbaBaseException nbe) {
		  getLogger().logException(nbe);
		}

	}
	/*
	 * Calls NBA_FORMS_VALIDATE to determine if Form # is a PreSale
	 */
	//NBA231 new method
	protected boolean isReg60PreSale() {
		String formNumber = getWork().getNbaLob().getFormNumber();
		if (null == formNumber) {
			return false;
		}
		try {
			NbaTableAccessor tableaccessor = new NbaTableAccessor();
			String formType = tableaccessor.getFormType(formNumber);
			if ("2".equals(formType)) {
				return true;
			}
		} catch (NbaBaseException nbe) {
			getLogger().logException(nbe);//ALS4858
		}
		return false;
	}
	/**
	 * Set the LOB values to the default values if no data was entered for them
	 *
	 * @param indexVO
	 *            NbaIndexingVO object
	 * @return AccelResult object
	 * @throws Exception
	 */
	//NBA231 new method
    protected void setDefaults(NbaLob newLob) throws NbaBaseException {
        //VpmsModelResult vpmsModelResult = processRules(getWork().getNbaLob());
        String results = processRules(getWork().getNbaLob());

        StringTokenizer st = new StringTokenizer(results,"##");
        int a = 0;
        while (st.hasMoreTokens()) {
        	String value = st.nextToken();
        	switch (a) {
        	case 0:
        		newLob.setCompany(value);
        		break;
        	case 1:
        	    newLob.setPlan(value);
        	    break;
        	case 2:
        		newLob.setAppState(value);
        		break;
        	case 3:
        	    newLob.setOperatingMode(value);
        	    break;
        	case 4:		// ALS2746
        	    newLob.setDistChannel(value);
        	    break;
        	}
        	a++;
        }
    }

	/**
     * Call Replacement processing VP/MS model for Reg60 defaults
     *
     * @param lobData Lob data which from which OINK auto populated the input vpms values
     * @return VpmsModelResult
     * @throws NbaBaseException
     */
	//NBA231 new method
    protected String processRules(NbaLob sourceLob) throws NbaBaseException {
        NbaVpmsAdaptor proxy = null;
        try {
            NbaOinkDataAccess data = new NbaOinkDataAccess(sourceLob);
            proxy = new NbaVpmsAdaptor(data, NbaVpmsConstants.REPLACEMENTS_PROCESSING);
            proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_DEFAULTFIELDS);
            Map deOink = getDeOinkMapForSources(getWork());
    		deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
    		deOink.put(NbaVpmsAdaptor.A_INSTALLATION, getInstallationType());
    		proxy.setSkipAttributesMap(deOink);
            VpmsComputeResult compResult = proxy.getResults();
			return compResult.getResult();
        } catch (RemoteException t) {
            throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
        } finally {
            if (proxy != null) {
                try {
                    proxy.remove();
                } catch (RemoteException re) {
                    LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
                }
            }
        }
    }
    /**
     * New method to add de-Oink variables
     */
    //NBA231 new method
    protected void reinitializeStatusFields() throws NbaBaseException {

    	Map deOinkMap = new HashMap();
    	if (hasParentInd) {
    		deOinkMap.put("A_HasParentInd","true");
    	} else {
    		deOinkMap.put("A_HasParentInd","false");
    	}

    	statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), getNbaTxLife(), getRequirementInfo(), deOinkMap);
    }
    /**
     * Retrieves the current requirement transform map.
     * @return String
     */
    //AXAL3.7.31 New Method
	public Map getReqCodeTransform() {
		return reqCodeTransform;
	}
    //AXAL3.7.31 New Method
    protected String getSourceText(String rawSource) {
    	String revisedSource = rawSource;
    	Iterator it = getReqCodeTransform().keySet().iterator();
    	String origReqCode = null;
    	String newReqCode = null;
    	String key = null;
    	while (it.hasNext()) {
    		key = (String)it.next();
    		if (!key.equals(getReqCodeTransform().get(key))) {
            	origReqCode = "<ReqCode tc=\"" + key + "\">";
            	newReqCode = "<ReqCode tc=\"" + (String)getReqCodeTransform().get(key) + "\">";
        		revisedSource = revisedSource.replaceAll(origReqCode, newReqCode);
    		}
    	}
    	return revisedSource;
    }

    //ALS4420 code deleted - moved to Requirementutils

    /**
	 * Return the followUpFreq from the RequirementInfoExtension
	 * @param refid
	 * @return int
	 * @throws NbaBaseException
	 */
	// ALS2814
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
     * Determine how many times the MiscMail has been suspended.
     * @param lob
     * @return int Number of times the MiscMail has previously been suspended.
     */
    //ALPC137 New Method
    protected int getSuspendCount(NbaLob lob) {
    	int count = 0;
    	int index = 1;
    	try {
        	while (lob.getAppHoldSuspDateAt(index) != null) {
        		count++;
        		index++;
        	}
    	} catch (NbaBaseException ex) {
            getLogger().logError(ex.getMessage());
    	}
    	return count;
    }

    /**
     * This method creates and initializes an <code>NbaVpmsAdaptor</code> object to
     * call VP/MS to execute the supplied entryPoint.
     * @param model The VPMS model to be called
     * @param entryPoint The entry point to be called in the VP/MS model
     * @param deOink A Map object containing deOink variables to be passed to the model
     * @param execptText AXAL3.7.20R
     * @return The results from the VP/MS call in the form of an <code>VpmsModelResult</code> object
     * @throws NbaBaseException
     */
    //ALPC137 New Method
    public VpmsModelResult getDataFromVpms(String model, String entryPoint, HashMap deOink, String execptText) throws NbaBaseException, NbaVpmsException {
        NbaVpmsAdaptor vpmsProxy = null;
        try {
    		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
    		deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
    		deOink.put(NbaVpmsAdaptor.A_INSTALLATION, getInstallationType());
    		vpmsProxy = new NbaVpmsAdaptor(oinkData, model);
    		vpmsProxy.setVpmsEntryPoint(entryPoint);
    		vpmsProxy.setSkipAttributesMap(deOink);
    		VpmsComputeResult compResult = vpmsProxy.getResults();
    		NbaVpmsModelResult data = new NbaVpmsModelResult(compResult.getResult());
    		return data.getVpmsModelResult();
    	} catch (java.rmi.RemoteException re) {
    		throw new NbaVpmsException(execptText + NbaVpmsException.VPMS_EXCEPTION, re);  //AXAL3.7.20R
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
    /*
     * getSuspendDays
     * @return int suspend dasy
     * @throws NbaBaseException
     */
    // AXAL3.7.20R New Method
    protected int getSuspendDays() throws NbaBaseException, NbaVpmsException {
		//Begin AXAL3.7.20R
		//First try VPMS, if "-" is returned, use FollowupFrequency instead.
		int nextSuspend = getSuspendCount(work.getNbaLob()) + 1;
		HashMap deOink = new HashMap();
		deOink.put("A_NumberOfSuspends", String.valueOf(nextSuspend));
		VpmsModelResult data = getDataFromVpms(NbaVpmsAdaptor.REQUIREMENTS, NbaVpmsAdaptor.EP_MATCH_SUSPEND_DAYS, deOink, PROCESS_PROBLEM);

		String suspendDaysStr = getFirstResult(data); //ACN020
		//begin ALS4843
		int followUpFreq = getFollowUpFrequency(work.getNbaLob().getReqUniqueID());
		if (suspendDaysStr.length() > 0 && !suspendDaysStr.equals("-")) {
			int suspendDays = Integer.parseInt(suspendDaysStr.trim());
			if (isResetFollowUpDaysNeeded()) {
				return suspendDays;
			}
		}
		return followUpFreq;
		//end ALS4843
		//end AXAL3.7.20R
	}

	/**
	 * This inner class represents the result returned by Requirements VP/MS model
	 */
    //AXAL3.7.20G
    protected class RequirementVpmsResult {
    	private String workType;
    	private String queue;

    	/**
    	 * This contructor initializes the object's state from the input VP/MS result
    	 * @param result
    	 */
    	public RequirementVpmsResult(String result) {
    		StringTokenizer tokenizer = new StringTokenizer(result, "##");
    		workType = tokenizer.nextToken();
    		if (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (! NbaUtils.isBlankOrNull(token)) {
					queue = token;
				}
			}
    	}

		public String toString() {
    		return "WorkType: " + workType + ", Queue: " + queue;
    	}

		/**
		 * @return Returns the queue.
		 */
		public String getQueue() {
			return queue;
		}
		/**
		 * @param queue The queue to set.
		 */
		public void setQueue(String queue) {
			this.queue = queue;
		}
		/**
		 * @return Returns the workType.
		 */
		public String getWorkType() {
			return workType;
		}
		/**
		 * @param workType The workType to set.
		 */
		public void setWorkType(String workType) {
			this.workType = workType;
		}
    }


    //QC12292 APSL3402 Deleted code

    /**
     * This method resets undwriter and case manager queues.
     * @param nbaTXLife
     * @param lob
     * @throws NbaBaseException
     */
    //ALPC153 New Method
    //ALS4381 Modified logic
    public void resetUWCM_Old(NbaDst work) throws NbaBaseException {
    	if (getWorkLobs().getReqType() == NbaOliConstants.OLI_REQCODE_PHYSSTMT && nbaTxLife.isWholeSale()) {//APSL705
    		NbaLob lob = work.getNbaLob();
    		NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(NbaVpmsConstants.UISTATUS_UNDERWRITER_WORKBENCH, ""), work, nbaTxLife);
    		String otherStatus = provider.getOtherStatus();
			if (otherStatus.indexOf(lob.getUndwrtQueue()) == -1) {
				String underwriterLOB = getEquitableQueue(otherStatus, NbaLob.A_LOB_ORIGINAL_UW_WB_QUEUE);
				String oldUWQueue = lob.getUndwrtQueue();
				lob.setUndwrtQueue(underwriterLOB);
				String caseManagerResult = provider.getAlternateStatus();
				if (caseManagerResult.indexOf("|") != -1) {
					caseManagerResult = getListOfCMQueues(caseManagerResult, underwriterLOB);
				}
				lob.setCaseManagerQueue(caseManagerResult);
				work.setUpdate();
				if (work.getQueue().equals(oldUWQueue) && (! oldUWQueue.equals(underwriterLOB))) {//ALS5763
					setStatus(work, NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, nbaTxLife);
				}
			}
		}
	}

    /**
     * This method finds the case to which this transaction is attached to.
     * @throws NbaBaseException
     */
    //ALPC153 New Method
    protected void findCase() throws NbaBaseException {
    	NbaSearchVO searchVO = new NbaSearchVO();
    	NbaLob lookupLob = new NbaLob();
    	lookupLob.setPolicyNumber(getWorkLobs().getPolicyNumber());
    	searchVO.setNbaLob(lookupLob);
    	searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
    	searchVO = lookupWork(getUser(), searchVO);

    	NbaSearchResultVO resultVO = (NbaSearchResultVO) searchVO.getSearchResults().get(0);
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(resultVO.getWorkItemID(), true);
		retOpt.requestSources();
		retOpt.setLockWorkItem();
		NbaDst parentCase = retrieveWorkItem(getUser(), retOpt);
		getMatchingCases().add(parentCase);
    }

    //ALS4381 New Method
    protected void setStatus(NbaDst work, String userID, NbaTXLife contract) throws NbaBaseException {
        NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(userID, ""), work, contract);
        work.setStatus(provider.getPassStatus());
        work.increasePriority(provider.getCaseAction(), provider.getCasePriority());
        NbaUtils.setRouteReason(work, work.getStatus());
	}

    /**
     * This method check, is the current WI is a result to any of the Omission Requirement.
     * @return
     * @throws NbaBaseException
     */
    //ALS4638 new Method
    protected boolean isOmissionRequirementResult() throws NbaBaseException{
    	matchingWorkItems = new ArrayList();
    	boolean ommissionReqRes = false;
    	NbaVpmsAdaptor vpmsProxy = null;
    	if(getWorkType().equals(A_WT_MISC_MAIL) || getWorkType().equals(A_WT_TEMP_REQUIREMENT)){
    		 try {
    			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
    			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS);
    			vpmsProxy.setVpmsEntryPoint(EP_CHECK_OMISSION_REQ);
    			VpmsComputeResult compResult = vpmsProxy.getResults();
    			if(TRUE_STR.equals(compResult.getResult().trim())){
    				ommissionReqRes = true;
    			}
    		} catch (java.rmi.RemoteException re) {
    			throw new NbaVpmsException("Error in verifying Is Omission Requirement "+ NbaVpmsException.VPMS_EXCEPTION, re);
    		} catch (NbaBaseException e) {
    			getLogger().logDebug("Exeception occoured while verifying Is Omission Requirement from REQUIREMENTS VPMS Model : "+NbaUtils.getStackTrace(e));
    		} finally {
    			try {
    				if (vpmsProxy != null) {
    					vpmsProxy.remove();
    				}
    			} catch (Throwable th) {
    				// ignore, nothing can be done
    			}
    		}
    	}
    	return ommissionReqRes;
    }

	/**
	 * @return Returns the formNumber.
	 */
	public String getFormNumber() {
		return formNumber;
	}
	/**
	 * @param formNumber The formNumber to set.
	 */
	public void setFormNumber(String formNumber) {
		this.formNumber = formNumber;
	}

	/**
	 * Check if misc mail source with form number already attached on case then do not attached it to case
	 * @param permWorkitemCase
	 * @param aSource
	 * @return
	 */
	//ALS3483 new method
	public boolean isFormAvailableOnCase(NbaDst permWorkitemCase, NbaSource aSource){
	    List allSources = permWorkitemCase.getNbaSources();
	    boolean isFormNumberAvailable = false;
	    String wrkitemFromNumber = aSource.getNbaLob().getFormNumber();
        for (int j = 0; j < allSources.size(); j++) {
            NbaSource caseSource = (NbaSource) allSources.get(j);
            String caseSrcFormNumber = caseSource.getNbaLob().getFormNumber();
            if(!NbaUtils.isBlankOrNull(caseSrcFormNumber) && caseSrcFormNumber.equals(wrkitemFromNumber) && !A_ST_INVALID_FORM.equalsIgnoreCase(caseSource.getSourceType())){ //APSL1537
                isFormNumberAvailable = true;
            }
        }
        return isFormNumberAvailable;
	}
	/**
	 * add comments to work item when multiple cases are found
	 */
	//ALS4650 new method
	public void processMultipleCaseMatches() {
		NbaDst caseDst;
		StringBuffer caseComment = new StringBuffer();
		caseComment.append("Possible matching policies: ");
		String polNumber;
		int matchingCaseSize = getMatchingCases().size();
		for (int i=0;i<matchingCaseSize;i++) {
			caseDst = (NbaDst) getMatchingCases().get(i);
			polNumber = caseDst.getNbaLob().getPolicyNumber();
			// Begin ALS5632
			if(polNumber != null){
			caseComment.append(polNumber);
			}else{
				caseComment.append(POL_NUM_NOT_ASSIGNED);
			}
			caseComment.append(", ");
		}
		String caseCommentStr = caseComment.toString();
		if (caseCommentStr.length() > 0) {
			caseCommentStr = caseComment.substring(0, caseComment.length() - 2); //Remove last comma and space
	}
		addComment(caseCommentStr);
		//End ALS5632
	}


	/**
	 * Calls VP/MS to check if resetting of followup days needed or not.
	 * @return True or False
	 * @throws NbaBaseException
	 */
	//ALS4843 new Method
	protected boolean isResetFollowUpDaysNeeded() throws NbaBaseException, NbaVpmsException {
		NbaVpmsResultsData data = new NbaVpmsResultsData(getDataFromVpms(NbaVpmsAdaptor.REQUIREMENTS, NbaVpmsAdaptor.EP_IS_RESET_FOLLOWUP_DAYS_NEEDED,
	            new NbaOinkDataAccess(work.getNbaLob()), null, null));
		if (data.getResultsData() != null && data.getResultsData().size() > 0) {
			String strResult = (String) data.getResultsData().get(0);
			if (strResult != null && !strResult.trim().equals("")) {
				return Boolean.valueOf(strResult).booleanValue();
			}
		}
		return false;
	}
	//ALS5414 New Method
	public List getIgnoredMatchingCases() {
		return ignoredMatchingCases;
	}
	//ALS5414 New Method
	public void setIgnoredMatchingCases(List unmatchedCases) {
		this.ignoredMatchingCases = unmatchedCases;
	}
	//ALS5414 New Method
	protected void unlockIgnoredMatchingCases() throws NbaBaseException {
		int listSize = getIgnoredMatchingCases().size();
		for (int i = 0; i < listSize; i++) {
			NbaDst dstCase = (NbaDst) getIgnoredMatchingCases().get(i);
			unlockWork(getUser(), dstCase);
		}
		//Begin APSL460,APSL464,APSL557
		String userID = getUser().getUserID();
		listSize = getPermApDoneMatchingCases().size();
		for (int i = 0; i < listSize; i++) {
			NbaDst dstCase = (NbaDst) getPermApDoneMatchingCases().get(i);
			if (dstCase.isLocked(userID)) {
				unlockWork(getUser(), dstCase);
			}
		}
		listSize = getTermFormalCases().size();
		for (int i = 0; i < listSize; i++) {
			NbaDst dstCase = (NbaDst) getTermFormalCases().get(i);
			if (dstCase.isLocked(userID)) {
				unlockWork(getUser(), dstCase);
			}
		}
		listSize = getTermInFormalCases().size();
		for (int i = 0; i < listSize; i++) {
			NbaDst dstCase = (NbaDst) getTermInFormalCases().get(i);
			if (dstCase.isLocked(userID)) {
				unlockWork(getUser(), dstCase);
			}
		}
		// ENd APSL460,APSL464,APSL557
	}

	/**
	 * @throws NbaBaseException
	 *
	 */
	//ALS5753
	private void attachTempRequirement() throws NbaBaseException {
		NbaDst parentCase = (NbaDst) getPermApDoneMatchingCases().get(0);
		parentCase.getNbaCase().addNbaTransaction(getWork().getNbaTransaction());
		updateWork(getUser(),parentCase);
		NbaProcessStatusProvider tempReqStatus = new NbaProcessStatusProvider(getUser(), getWork());
		changeStatus(tempReqStatus.getPassStatus());
		addComment("Requirement matched to AWD Case");
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
		doUpdateWorkItem();

	}
    /**
     * @param deOinkMap
     * @throws NbaBaseException
     */
	//APSL460,APSL464,APSL557
    private void filterEndQueueCases() throws NbaBaseException {
		if (A_WT_TEMP_REQUIREMENT.equalsIgnoreCase(getWorkType())) {
			NbaDst matchingCase = null;
			Iterator it = getMatchingCases().iterator();
			while (it.hasNext()) {
				matchingCase = (NbaDst) it.next();
				// APSL4407 Begin code Commented
//				if (A_STATUS_PERMAPPDN.equalsIgnoreCase(matchingCase.getStatus())) {
//					it.remove();
//					getPermApDoneMatchingCases().add(matchingCase);
//					continue;
//				}
				// APSL4407 End Code Commented
				if (END_QUEUE.equalsIgnoreCase(matchingCase.getNbaLob().getQueue())
						&& NbaUtils.isBlankOrNull(matchingCase.getNbaLob().getPolicyNumber())) {
					it.remove();
					getIgnoredMatchingCases().add(matchingCase);
				}
			}

		}
	}

	/**
	 * @return Returns the permApDoneMatchingCases.
	 */
   // APSL460,APSL464,APSL557
	public List getPermApDoneMatchingCases() {
		return permAppDoneMatchingCases;
	}
	/**
	 * @param permApDoneMatchingCases The permApDoneMatchingCases to set.
	 */
	//APSL460,APSL464,APSL557
	public void setPermApDoneMatchingCases(List permApDoneMatchingCases) {
		this.permAppDoneMatchingCases = permApDoneMatchingCases;
	}
	/**
	 * @return Returns the termFormalCases.
	 */
	//APSL460,APSL464,APSL557
	public List getTermFormalCases() {
		return termFormalCases;
	}
	/**
	 * @param termFormalCases The termFormalCases to set.
	 */
	//APSL460,APSL464,APSL557
	public void setTermFormalCases(List termFormalCases) {
		this.termFormalCases = termFormalCases;
	}
	/**
	 * @return Returns the termInFormalCases.
	 */
	//APSL460,APSL464,APSL557
	public List getTermInFormalCases() {
		return termInFormalCases;
	}
	/**
	 * @param termInFormalCases The termInFormalCases to set.
	 */
	//APSL460,APSL464,APSL557
	public void setTermInFormalCases(List termInFormalCases) {
		this.termInFormalCases = termInFormalCases;
	}

	/**
	 * @return
	 * @throws NbaBaseException
	 *  Check for Perm Formal case in end queue with parmappdone status.
	 *  Check for Term Informal which is in End queue with Informal APP Done staus.Informal APP Done status is used for perm Informal cases. Need to verify it
	 */
	//ALS5753
	//APSL460,APSL464,APSL557
	private boolean processPermanentApplication() throws NbaBaseException {
		boolean flag = false;
		NbaDst matchingCase = null;
		matchingCase=getWorkType().equals(A_WT_TEMP_REQUIREMENT)&&getPermApDoneMatchingCases().size()>0?(NbaDst)getPermApDoneMatchingCases().get(0):null;
        //Begin SC#13
		  if (matchingCase != null && singleTermInFormal()) {
			NbaDst parentCase = (NbaDst) getTermInFormalCases().get(0);
			if (A_STATUS_INFORMAL_APP_DONE.equalsIgnoreCase(parentCase.getStatus())) {
				getIgnoredMatchingCases().add(getTermInFormalCases().get(0));//Add in Ignored list
				getTermInFormalCases().remove(0);//Remove from the Informal list
				getMatchingCases().remove(0);//Remove from the matching list
			}

		}
		//End SC#13
		if (getMatchingCases().isEmpty() && matchingCase != null) {
			flag = true;
		}
		return flag;
	}
	/**
	 * @return
	 * @throws NbaBaseException
	 * Check for duplicate term formal cases
	 * there is no condition for duplicate term informal cass ????
	 */
	//APSL460,APSL464,APSL557
   	private boolean singleTermForml() throws NbaBaseException {
		if (getWorkType().equals(A_WT_TEMP_REQUIREMENT) && getTermFormalCases().size() == 1) {
			if (getMatchingCases().isEmpty()) {
				getMatchingCases().add(getTermFormalCases().get(0));
			}
			return true;
		}
		return false;
	}
	/**
	 * @return
	 * @throws NbaBaseException
	 * Check for duplicate term formal cases
	 * there is no condition for duplicate term informal cass ????
	 */
   	//APSL460,APSL464,APSL557
	private boolean singleTermInFormal() throws NbaBaseException {
		if (getWorkType().equals(A_WT_TEMP_REQUIREMENT) && getTermInFormalCases().size() == 1) {
			if (getMatchingCases().isEmpty()) {
				getMatchingCases().add(getTermInFormalCases().get(0));
			}
			return true;
		}
		return false;
	}
	 /**
	 * @return
	 * @throws NbaBaseException
	 */
	 //APSL460,APSL464,APSL557
	private boolean duplicateTermFormalMatched() throws NbaBaseException {
		NbaDst matchingCase = null;
		if (A_WT_TEMP_REQUIREMENT.equalsIgnoreCase(getWorkType())) {
			Iterator it = getMatchingCases().iterator();
			while (it.hasNext()) {
				matchingCase = (NbaDst) it.next();
				if (matchingCase.getNbaLob().getAppOriginType() == NbaOliConstants.OLI_APPORIGIN_FORMAL) {
					it.remove();
					getTermFormalCases().add(matchingCase);
					/*getIgnoredMatchingCases().add(matchingCase);*/
				}
			}
			if (getTermFormalCases().size() > 1) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return
	 * @throws NbaBaseException
	 */
	 //APSL460,APSL464,APSL557
	private boolean duplicateTermInFormalMatched() throws NbaBaseException {
		NbaDst matchingCase = null;
		if (A_WT_TEMP_REQUIREMENT.equalsIgnoreCase(getWorkType())) {
			Iterator it = getMatchingCases().iterator();
			while (it.hasNext()) {
				matchingCase = (NbaDst) it.next();
				if (matchingCase.getNbaLob().getAppOriginType() != NbaOliConstants.OLI_APPORIGIN_FORMAL) {
					it.remove();
					getTermInFormalCases().add(matchingCase);
					/*getIgnoredMatchingCases().add(matchingCase);*/
				}
			}
			if (getTermInFormalCases().size() > 1) {
				return true;
			}
		}

		return false;
	}
	/**
     * @param deOinkMap
     * @throws NbaBaseException
     */
   //APSL460,APSL464,APSL557
    private void processDuplicateTermCases(Map deOinkMap) throws NbaBaseException {
		deOinkMap.put("A_DUPLICATEMATCH", "true");
		NbaProcessStatusProvider tempReqStatus = new NbaProcessStatusProvider(getUser(), getWork(), deOinkMap);
		changeStatus(tempReqStatus.getPassStatus());
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, tempReqStatus.getPassStatus(), tempReqStatus.getPassStatus()));
		doUpdateWorkItem();
	}
	/**
	 * @return
	 * @throws NbaBaseException
	 */
    //	ALS5753
	private boolean findDuplicateTermForml() throws NbaBaseException {
		NbaDst matchingCase = null;
		int formalTermCaseCount = 0;
		if (A_WT_TEMP_REQUIREMENT.equalsIgnoreCase(getWorkType())) {
			Iterator it = getMatchingCases().iterator();
			while (it.hasNext()) {
				matchingCase = (NbaDst) it.next();
				if (matchingCase.getNbaLob().getAppOriginType() == NbaOliConstants.OLI_APPORIGIN_FORMAL
						&& !PLAN_ULBASE.equalsIgnoreCase(matchingCase.getNbaLob().getPlan())
						&& !A_STATUS_PERMAPPDN.equalsIgnoreCase(matchingCase.getStatus())) {
					if (END_QUEUE.equalsIgnoreCase(matchingCase.getNbaLob().getQueue())) {
						if (!NbaUtils.isBlankOrNull(matchingCase.getNbaLob().getPolicyNumber())){
							formalTermCaseCount++;
						}
					} else {
						formalTermCaseCount++;
					}
				}
			}
		}
		return formalTermCaseCount >= 2;
	}

	//ALNA208
	private boolean isFormWithApp() {
		try {
		return A_WT_MISC_MAIL.equals(getWorkType()) && getWork().getNbaLob().getFormRecivedWithAppInd();
		} catch (NbaBaseException nbe) {
			getLogger().logError(nbe.getMessage());
		}
		return false;
	}

    /**
     * @param deOinkMap
     * @throws NbaBaseException
     */
	//ALS5753
    private void processDuplicateTermFormal(Map deOinkMap) throws NbaBaseException {
		deOinkMap.put("A_DUPLICATEMATCH", "true");
		NbaProcessStatusProvider tempReqStatus = new NbaProcessStatusProvider(getUser(), getWork(), deOinkMap);
		changeStatus(tempReqStatus.getPassStatus());
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, tempReqStatus.getPassStatus(), tempReqStatus.getPassStatus()));
		doUpdateWorkItem();
	}

    /**
     * @param NbaSource
     * @param NbaLob
     * copyCaseLobToSource method used to copy Parent Case Lob to Source(Miscmail/Provider result),
     * If user not entered company/policy/plan/product while indexing Miscmail/Provider result
     */
   //APSL1076 New Method Retroftted from Prod
    public void copyCaseLobToSource(NbaSource source, NbaLob parentCaseLob) {
		if (!NbaUtils.isBlankOrNull(parentCaseLob)) {
			if (!NbaUtils.isBlankOrNull(parentCaseLob.getCompany())) {
				source.getNbaLob().setCompany(parentCaseLob.getCompany());
			}
			if (!NbaUtils.isBlankOrNull(parentCaseLob.getPolicyNumber())) {
				source.getNbaLob().setPolicyNumber(parentCaseLob.getPolicyNumber());
			}
			if (!NbaUtils.isBlankOrNull(parentCaseLob.getPlan())) {
				source.getNbaLob().setPlan(parentCaseLob.getPlan());
			}
			if (!NbaUtils.isBlankOrNull(parentCaseLob.getProductTypSubtyp())) {
				source.getNbaLob().setProductTypSubtyp(parentCaseLob.getProductTypSubtyp());
			}
			if (!NbaUtils.isBlankOrNull(parentCaseLob.getApplicationType())) {
				source.getNbaLob().setApplicationType(parentCaseLob.getApplicationType());
			}
			source.setUpdate();
		}
	}

    //CR1346708
	protected void updateJointInsuredLOB(NbaSource source,NbaTXLife nTxLife) throws NbaBaseException {
		String validReq = "";
		NbaLob lob = source.getNbaLob();
		NbaVpmsAdaptor proxy = null;
		Relation rel = null;
		if (nTxLife != null) { //ALII1674
			rel = NbaUtils.getRelation(nTxLife.getOLifE(), NbaOliConstants.OLI_REL_JOINTINSURED);
		} else {
			rel = NbaUtils.getRelation(nbaTxLife.getOLifE(), NbaOliConstants.OLI_REL_JOINTINSURED);
		}
		if (rel != null) {
			try {
				NbaOinkDataAccess data = new NbaOinkDataAccess(lob);
				Map deOinkMap = new HashMap();
				proxy = new NbaVpmsAdaptor(data, NbaVpmsConstants.REQUIREMENTS);
				proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_ORD_REQ_FOR_JINS);
				deOinkMap.put(NbaVpmsConstants.A_REQ_CODE, String.valueOf(lob.getReqType()));
				proxy.setSkipAttributesMap(deOinkMap);
				VpmsComputeResult rulesProxyResult = proxy.getResults();
				if (!rulesProxyResult.isError()) {
					validReq = rulesProxyResult.getResult();
				}
			} catch (RemoteException t) {
				throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
			} finally {
				if (proxy != null) {
					try {
						proxy.remove();
					} catch (RemoteException re) {
						LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
					}
				}
			}
			if (validReq.equals("1")) {
				String partyId = null;
				Party party = null;
				//begin QC10917
				if (nTxLife != null) {
					partyId = nTxLife.getPartyId(NbaOliConstants.OLI_REL_JOINTINSURED, rel.getRelatedRefID());
					party = nTxLife.getParty(partyId).getParty();
				} else {
					partyId = nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_JOINTINSURED, rel.getRelatedRefID());
					party = nbaTxLife.getParty(partyId).getParty();
				}
//				//End QC10917
				Person person = party.getPersonOrOrganization().getPerson();
				if (lob.getFirstName().equalsIgnoreCase(person.getFirstName()) && lob.getLastName().equalsIgnoreCase(person.getLastName())) { //ALII1545
					lob.setJointInsured(true);
				}
			}
		}
	}

	//CR1454508(APSL2681) New Method
	protected void unSuspendMatchingPredictiveCase(NbaDst requirement) throws NbaBaseException { //ALII1656
		NbaDst parentCase = null;
		NbaSuspendVO suspendedCase = new NbaSuspendVO();
		if (getMatchingCases() != null && getMatchingCases().size() > 0) {
			parentCase = (NbaDst) getMatchingCases().get(0);
		} else {
			NbaDst workitem = lookupPredictiveCase(requirement); //ALII1656
			if (!NbaUtils.isBlankOrNull(workitem)) {
				suspendedCase.setCaseID(workitem.getID());
				workitem.getNbaLob().setPredictiveSusp(false);
				workitem.setUpdate();
				update(workitem); //ALII1656
				unsuspendWork(suspendedCase);
				getMatchingCases().add(workitem);
			}
		}
		if (parentCase!=null && parentCase.getQueue().equalsIgnoreCase(A_QUEUE_PREDICTIVEHOLD)) { //ALII1639
			suspendedCase.setCaseID(parentCase.getID());
			parentCase.getNbaLob().setPredictiveSusp(false);
			parentCase.setUpdate();
			update(parentCase); //ALII1656
			unsuspendWork(suspendedCase);
		}

	}

	//CR1454508(APSL2681) New Method
	protected NbaDst lookupPredictiveCase(NbaDst requirement) throws NbaBaseException { //ALII1656
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName(CASE_SEARCH_VO);
		searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchVO.setContractNumber(getWork().getNbaLob().getPolicyNumber()); //ALII1639
		searchVO.setQueue(A_QUEUE_PREDICTIVEHOLD);
		searchVO = lookupWork(getUser(), searchVO);
		List results = searchVO.getSearchResults();
		if (results.size() == 0) {
			return null; //ALII1639
		}
		NbaAwdRetrieveOptionsVO retrieveOpt = new NbaAwdRetrieveOptionsVO();
		retrieveOpt.setWorkItem(((NbaSearchResultVO) searchVO.getSearchResults().get(0)).getWorkItemID(), true);
		retrieveOpt.setLockWorkItem();
		NbaDst workItem = retrieveParentWork(requirement, true, false, false); //,ALII1656
		//Deleted code - ALII1639
		return workItem;
	}
	//End CR1454508(APSL2681)
	//QC #8669 APSL3025
	protected int getSuspendDays(boolean ind) throws NbaBaseException, NbaVpmsException {
		int nextSuspend = getSuspendCount(work.getNbaLob()) + 1;
		int suspendDays =1;//If no result or "-" is obtained from vpms then suspend WI for 1 day
		HashMap deOink = new HashMap();
		deOink.put("A_NumberOfSuspends", String.valueOf(nextSuspend));
		VpmsModelResult data = getDataFromVpms(NbaVpmsAdaptor.REQUIREMENTS, NbaVpmsAdaptor.EP_MATCH_SUSPEND_DAYS, deOink, PROCESS_PROBLEM);
		String suspendDaysStr = getFirstResult(data);
		if (suspendDaysStr.length() > 0 && !suspendDaysStr.equals("-"))
			 suspendDays = Integer.parseInt(suspendDaysStr.trim());
		return suspendDays;
	}

	// APSL3175 New method
	protected void createMIBInquiry(RequirementInfo reqInfo) throws NbaBaseException {
		List xmlifeList = new ArrayList();
		xmlifeList.add(create401Request(getWork(), reqInfo));

		HashMap reqMsg = null;
		//call provider to get provider ready message
		NbaProviderAdapterFacade facade = new NbaProviderAdapterFacade(getWork(), getUser());
		reqMsg = (HashMap) facade.convertXmlToProviderFormat(new ArrayList(xmlifeList));

		String xmlTrans = (String) reqMsg.get(NbaConstants.TRANSACTION);
		addXMLifeTrans(reqInfo, xmlTrans);
	}

	/**
	 * This method creates the XMLife message for 401 transaction code.
	 *
	 * @return the generated XMLife message
	 * @param NbaDst - the work item
	 */
	// APSL3175 New method
	protected NbaTXLife create401Request(NbaDst reqItem, RequirementInfo requirementInfo) throws NbaBaseException {
		NbaTableAccessor nbaTableAccessor = new NbaTableAccessor();
		NbaLob lob = reqItem.getNbaLob();
		String pendingResponseOK = "0";
		boolean testIndicator = NbaConfiguration.getInstance().getProvider(lob.getReqVendor()).getTestIndicator();
		String mibCheckingFollowUpInd = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.MIB_CHECKING_FOLLOWUP_INDICATOR); //NBLXA-1524
		String mibIaiFollowUpInd = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.MIB_IAI_FOLLOWUP_INDICATOR); //NBLXA-1524

		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_MIBINQUIRY);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
	    nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser())); //NBA050 SPR2639
		nbaTXRequest.setNbaLob(lob);


		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife); //NBA050

		//create txlife with default request fields
		NbaTXLife txLife = new NbaTXLife(nbaTXRequest);

		//ACN009 begin
		txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setPendingResponseOK(pendingResponseOK);
		txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTestIndicator(testIndicator);
		txLife.getTXLife().setVersion(NbaOliConstants.OLIFE_VERSION_15_02);			//AXAL3.7.31

		MIBRequest mibRequest = new MIBRequest();
		mibRequest.setMIBPriority(NbaOliConstants.TC_MIBPRIORITY_STANDARD);
		mibRequest.setMIBSearchDepth(NbaOliConstants.TC_MIBSEARCH_STANDARD);

		MIBServiceDescriptor cMibServiceDescriptor = new MIBServiceDescriptor();
		cMibServiceDescriptor.setMIBService(NbaOliConstants.TC_MIBSERVICE_CHECKING);
		MIBServiceOptions cMibServiceOptions = new MIBServiceOptions();
		cMibServiceOptions.setMIBFollowUpInd(mibCheckingFollowUpInd);
		cMibServiceDescriptor.setMIBServiceOptions(cMibServiceOptions);
		MIBServiceDescriptorOrMIBServiceConfigurationID mibServiceDescriptorOrMIBServiceConfigurationID =  new MIBServiceDescriptorOrMIBServiceConfigurationID();
		mibServiceDescriptorOrMIBServiceConfigurationID.addMIBServiceDescriptor(cMibServiceDescriptor);

		MIBServiceDescriptor iMibServiceDescriptor = new MIBServiceDescriptor();
		iMibServiceDescriptor.setMIBService(NbaOliConstants.TC_MIBSERVICE_IAI);
		MIBServiceOptions iMibServiceOptions = new MIBServiceOptions();
		iMibServiceOptions.setMIBFollowUpInd(mibIaiFollowUpInd);
		iMibServiceDescriptor.setMIBServiceOptions(iMibServiceOptions);
		mibServiceDescriptorOrMIBServiceConfigurationID.addMIBServiceDescriptor(iMibServiceDescriptor);
		mibRequest.setMIBServiceDescriptorOrMIBServiceConfigurationID(mibServiceDescriptorOrMIBServiceConfigurationID);

		txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setMIBRequest(mibRequest);
		//ACN009 end

		//get olife
		OLifE olife = txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
		olife.setVersion(NbaOliConstants.OLIFE_VERSION_15_02);  //AXAL3.7.31

		//ACN009 begin
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
		txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setPrimaryObjectID(holding.getId()); //AXAL3.7.31
		Policy policy = holding.getPolicy();
		long productType = nbaTxLife.getPrimaryHolding().getPolicy().getProductType();
		policy.setProductType(productType);
		policy.setPolNumber(generateCompoundContractNumber()); //ACN009
		holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		holding.setHoldingStatus(NbaOliConstants.OLI_HOLDSTATE_PROPOSED);
		holding.setCurrencyTypeCode(NbaOliConstants.OLI_CURRENCY_USD);
		holding.setHoldingForm(NbaOliConstants.OLI_HOLDFORM_IND);
		holding.getPolicy().setLineOfBusiness(NbaOliConstants.OLI_LINEBUS_LIFE);

		olife.getSourceInfo().setCreationDate(new Date());
		olife.getSourceInfo().setCreationTime(new NbaTime());
		olife.setCurrentLanguage(NbaOliConstants.OLI_LANG_ENGLISH);

		//Life
		Life life = new Life();
		getNbaOLifEId().setId(life);//ALPC7
		life.setFaceAmt(lob.getFaceAmount());
		Coverage coverage = new Coverage();
		LifeParticipant lifeParticipant = new LifeParticipant();
		lifeParticipant.setLifeParticipantRoleCode(NbaOliConstants.OLI_PARTICROLE_PRIMARY);
		coverage.addLifeParticipant(lifeParticipant);
		life.addCoverage(coverage);
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeAnnut = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		lifeAnnut.setLife(life);
		policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeAnnut);
		//ACN009 end

		//RequirementInfo
		RequirementInfo reqInfo = new RequirementInfo();
		nbaOLifEId.setId(reqInfo);
		reqInfo.setReqCode(lob.getReqType());
		reqInfo.setRequirementInfoUniqueID(lob.getReqUniqueID());//ACN014
		reqInfo.setRequestedDate(new Date());
		reqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_SUBMITTED);//ACN009
		reqInfo.setMIBInquiryReason(NbaOliConstants.OLI_MIBREASON_NB);//ACN009
		//ACN009 code deleted
		//ACN009 begin
		//ApplicationInfo
		ApplicationInfo applInfo = new ApplicationInfo();
		applInfo.setHOAssignedAppNumber(nbaTxLife.getNbaHolding().getPolicy().getApplicationInfo().getHOAssignedAppNumber());
		applInfo.setTrackingID(nbaTxLife.getNbaHolding().getPolicy().getApplicationInfo().getTrackingID()); //AXAL3.7.31
		policy.setApplicationInfo(applInfo);
		//ACN009 end

		//NBA130 code deleted
		reqInfo.setRequirementDetails(requirementInfo.getRequirementDetails()); //NBA130
		//begin ACN014
		policy.addRequirementInfo(reqInfo);

		// do holding inquiry
		//NBA050 DELETED CODE
		Relation partyRel = getRelation(nbaTxLife, NbaOliConstants.OLI_PARTY, lob.getReqPersonCode(), lob.getReqPersonSeq());//NBA050
		NbaParty holdingParty = nbaTxLife.getParty(partyRel.getRelatedObjectID());//NBA050
		if (holdingParty == null) {
			throw new NbaBaseException("Could not get party information from holding inquiry");
		//end ACN014
		}
		//ACN014 code deleted
		// create person party
		Party party = new Party();
		nbaOLifEId.setId(party); //NBA050
		//ACN009 begin
		party.setGovtID(holdingParty.getSSN());
		//Begin APSL357
		if (holdingParty.getParty().hasResidenceState() && !(holdingParty.getParty().getResidenceState() == NbaOliConstants.OLI_STATE_1009800001)) {
			//birth place
			// NBA093 deleted 4 lines
			party.setResidenceState(holdingParty.getParty().getResidenceState()); //AXAL3.7.31
		} else {
			party.setResidenceState(NbaOliConstants.OLI_UNKNOWN); //AXAL3.7.31
		}

		if (holdingParty.getParty().hasResidenceCountry()) { //ALS4914
			long residenceCountry = holdingParty.getParty().getResidenceCountry();
			String besValue = nbaTableAccessor.translateOlifeValue(getKeyMap(), NbaTableConstants.OLI_LU_NATION, String.valueOf(residenceCountry),
					-1);
			if (!NbaUtils.isBlankOrNull(besValue)) {
				party.setResidenceCountry(besValue); //AXAL3.7.31
			} else {
				party.setResidenceCountry(residenceCountry); //AXAL3.7.31
			}

		} else {
			party.setResidenceCountry(NbaOliConstants.OLI_UNKNOWN);
		}
		//End APSL357

		party.setPartyKey(holdingParty.getParty().getPartyKey());
		// SPR3290 code deleted
		if (holdingParty.getParty().hasResidenceZip()){
			party.setResidenceZip(holdingParty.getParty().getResidenceZip());
		}else if (holdingParty.getParty().getAddress() != null && holdingParty.getParty().getAddressCount() > 0){
			party.setResidenceZip(holdingParty.getParty().getAddressAt(0).getZip());
		}else {
			party.setResidenceZip("");
		}
		//ACN009 end
		PersonOrOrganization perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		Person person = new Person();
		perOrg.setPerson(person);
		person.setLastName(holdingParty.getLastName());
		person.setFirstName(holdingParty.getFirstName());
		person.setMiddleName(holdingParty.getMiddleInitial());
		person.setBirthDate(holdingParty.getParty().getPersonOrOrganization().getPerson().getBirthDate());
		person.setGender(holdingParty.getParty().getPersonOrOrganization().getPerson().getGender()); //ACN009
		// begin ALS4914
		if (holdingParty.getParty().getPersonOrOrganization().getPerson().hasBirthCountry()) {  //ALS4914
			//begin APSL357
			long birthCountry = holdingParty.getParty().getPersonOrOrganization().getPerson().getBirthCountry();
			String besValue = nbaTableAccessor.translateOlifeValue(getKeyMap(), NbaTableConstants.OLI_LU_NATION, String.valueOf(birthCountry), -1);
			if (!NbaUtils.isBlankOrNull(besValue)) {
				person.setBirthCountry(besValue);
			} else {
				person.setBirthCountry(birthCountry);
			}
		//End APSL357
		} else {
			person.setBirthCountry(NbaOliConstants.OLI_UNKNOWN);
		}
		// end ALS4914
		if (holdingParty.getParty().getPersonOrOrganization().getPerson().hasBirthJurisdictionTC() &&  												  //ALS4914
				!(holdingParty.getParty().getPersonOrOrganization().getPerson().getBirthJurisdictionTC() == NbaOliConstants.OLI_STATE_1009800001)) {  //ALS4914
			//birth place
			// NBA093 deleted 4 lines
			person.setBirthJurisdictionTC(holdingParty.getParty().getPersonOrOrganization().getPerson().getBirthJurisdictionTC());  //AXAL3.7.31
		}else{
			person.setBirthJurisdictionTC(NbaOliConstants.OLI_UNKNOWN);   //AXAL3.7.31
		}
		// Start NBLXA-1714
		if (lob.getReqPersonCode() == NbaOliConstants.OLI_REL_DEPENDENT) {
			NbaParty primaryParty = nbaTxLife.getPrimaryParty();
			Party insuredParty = null;
			if (primaryParty != null) {
				insuredParty = primaryParty.getParty();
			}
			if (insuredParty != null) {
				party.setGovtID(UNKNOWN);
				if (insuredParty.hasResidenceState() && !(insuredParty.getResidenceState() == NbaOliConstants.OLI_STATE_1009800001)) {
					party.setResidenceState(insuredParty.getResidenceState());
				} else {
					party.setResidenceState(NbaOliConstants.OLI_UNKNOWN);
				}

				if (insuredParty.hasResidenceCountry()) {
					long residenceCountry = insuredParty.getResidenceCountry();
					String besValue = nbaTableAccessor.translateOlifeValue(getKeyMap(), NbaTableConstants.OLI_LU_NATION,
							String.valueOf(residenceCountry), -1);
					if (!NbaUtils.isBlankOrNull(besValue)) {
						party.setResidenceCountry(besValue);
					} else {
						party.setResidenceCountry(residenceCountry);
					}

				} else {
					party.setResidenceCountry(NbaOliConstants.OLI_UNKNOWN);
				}

				if (insuredParty.hasResidenceZip()) {
					party.setResidenceZip(insuredParty.getResidenceZip());
				} else if (insuredParty.getAddress() != null && insuredParty.getAddressCount() > 0) {
					party.setResidenceZip(insuredParty.getAddressAt(0).getZip());
				} else {
					party.setResidenceZip("");
				}
				person.setBirthJurisdictionTC(NbaOliConstants.OLI_UNKNOWN);
				person.setBirthCountry(NbaOliConstants.OLI_UNKNOWN);
				person.setOccupation(UNKNOWN);
			}

		}
		// End NBLXA-1714
//	ACN009 code deleted
		olife.addParty(party);
		reqInfo.setAppliesToPartyID(party.getId());//ACN009
		lifeParticipant.setPartyID(party.getId());//ACN009

//	ACN009 begin create organization party
		party = new Party();
		nbaOLifEId.setId(party); //NBA050
		perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		Organization org = new Organization();
		//NBA093 code deleted
		perOrg.setOrganization(org);
		Producer producer = new Producer();
		CarrierAppointment carrierAppointment = new CarrierAppointment();
		carrierAppointment.setCarrierApptTypeCode(NbaOliConstants.OLI_UNKNOWN); //SPR2514
		producer.addCarrierAppointment(carrierAppointment);
		party.setProducer(producer);
		olife.addParty(party);
		//ACN014 code deleted

		party = new Party();
		nbaOLifEId.setId(party);
		perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		org = new Organization();
		perOrg.setOrganization(org);
		Carrier carrier = new Carrier();
		Company comp = NbaConfiguration.getInstance().getProviderOrganizationKeyCompany("MIB", holding.getPolicy().getCarrierCode()); //ACN014
		carrier.setCarrierCode(comp.getOrgCode()); //ACN014
		carrier.setCarrierForm(NbaOliConstants.OLI_CARRIERFORM_DIRECT);
		party.setCarrier(carrier);
		olife.addParty(party);
		reqInfo.setRequesterPartyID(party.getId()); //ACN014
		holding.getPolicy().setCarrierPartyID(party.getId());
		//ACN009 end

		//add relations to the olife
		olife.addRelation(
			createRelation(txLife, //ACN014
				olife.getRelationCount(),
				holding.getId(),//ACN014
				olife.getPartyAt(0).getId(),
				NbaOliConstants.OLI_HOLDING,
				NbaOliConstants.OLI_PARTY,
				reqItem.getNbaLob().getReqPersonCode())); //NBA044

		//ACN009 begin
		olife.addRelation(
			createRelation(txLife,
				olife.getRelationCount(),
				holding.getId(),
				olife.getPartyAt(1).getId(),
				NbaOliConstants.OLI_HOLDING,
				NbaOliConstants.OLI_PARTY,
				NbaOliConstants.OLI_REL_PRIMAGENT));

		olife.addRelation(
			createRelation(txLife, //ACN014
				olife.getRelationCount(),
			   	holding.getId(),
			   	olife.getPartyAt(2).getId(),
			   	NbaOliConstants.OLI_HOLDING,
				NbaOliConstants.OLI_PARTY,
			   	NbaOliConstants.OLI_REL_REQUESTOR)); //ACN014
		return txLife;
	}

	// APSL3175 New method
	private Map getKeyMap(){
		Map params = new HashMap();
		params.put(NbaTableAccessConstants.C_COMPANY_CODE, "*");
		params.put(NbaTableAccessConstants.C_SYSTEM_ID, "*");
		params.put(NbaTableAccessConstants.C_COVERAGE_KEY, "*");

		return params;
	}

	// APSL3175 New method
	protected void addXMLifeTrans(RequirementInfo reqInfo, String xmlTrans) throws NbaBaseException {
		//Attachment
		Attachment attach = new Attachment();
		nbaOLifEId.setId(attach);
		attach.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT);
		attach.setAttachmentType(NbaOliConstants.OLI_ATTACH_MIB401);

		AttachmentData attachData = new AttachmentData();
		attachData.setPCDATA(xmlTrans);
		attach.setAttachmentData(attachData);
		attach.setActionAdd();
		reqInfo.addAttachment(attach);
		reqInfo.getActionIndicator().setUpdate();
		return;
	}



	/**
	 * @param newWorkItem
	 * @param currentMatch
	 * This method is used to create FormInstance for Medical Supplement if not created already.
	 */
	//APSL3405,QC#12555 new method
	public void addFormInstancesForSupp(NbaDst newWorkItem, NbaDst currentMatch ,String formName){ //NBLXA-2179 Start signare change
		FormInstance formInst = new FormInstance();
		NbaTXLife holdingInq = null;
		if (!NbaUtils.isBlankOrNull(getWork().getNbaLob().getPolicyNumber())) {
			holdingInq = (NbaTXLife) getContracts().get(getWork().getNbaLob().getPolicyNumber());
		} else {
			holdingInq = (NbaTXLife) getContracts().get(currentMatch.getNbaLob().getPolicyNumber());
		}
		if (!NbaUtils.isBlankOrNull(holdingInq)) {
			FormInstance formInstance = NbaUtils.getFormInstance(holdingInq, formName);
			if (NbaUtils.isBlankOrNull(formInstance)) {
				OLifE olife = holdingInq.getOLifE();
				NbaOLifEId nbaOLifEId = new NbaOLifEId(holdingInq);
				if (NbaUtils.isBlankOrNull(newWorkItem.getNbaLob().getLastName()) || NbaUtils.isBlankOrNull(newWorkItem.getNbaLob().getSsnTin())) {
						copyCurrentLobToWork(currentMatch, newWorkItem);
				}
				setNbaTxLife((NbaTXLife) getContracts().get(holdingInq.getNbaHolding().getPolicyNumber()));
				Party matchingParty = getPartyForMatchingResult(newWorkItem.getNbaLob());
				Relation relation = null;
				if (matchingParty != null) {
					relation = NbaUtils.getRelationForParty(matchingParty.getId(), holdingInq.getOLifE().getRelation().toArray());
				}
				formInst.setFormName(formName);
				formInst.setActionAdd();
				if (relation != null) {
					formInst.setRelatedObjectID(relation.getRelatedObjectID());
				}
				nbaOLifEId.setId(formInst);
				olife.addFormInstance(formInst);
				if (!olife.isActionAdd()) {
					olife.setActionUpdate();
				}
				OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FORMINSTANCE);
				formInst.addOLifEExtension(oLifEExtension);
				FormInstanceExtension formInstanceExtension = oLifEExtension.getFormInstanceExtension();
				formInstanceExtension.setFormRecdConfirmInd(true);
				formInstanceExtension.setActionAdd();
			}
		}
	}

	/**
	 * @param parentWork
	 * @param work
	 * This method is used to copy LOB from current matching workitem to New Workitem
	 */
	//APSL3405,QC#12555 new method
	protected void copyCurrentLobToWork(NbaDst parentWork, NbaDst work) {
		try {
			NbaLob parentWokLob = parentWork.getNbaLob();
			NbaLob wrkLob = work.getNbaLob();
			if(NbaUtils.isBlankOrNull(wrkLob.getCompany()));
				wrkLob.setCompany(parentWokLob.getCompany());
			if(NbaUtils.isBlankOrNull(wrkLob.getPolicyNumber()));
				wrkLob.setPolicyNumber(parentWokLob.getPolicyNumber());
			if(NbaUtils.isBlankOrNull(wrkLob.getBackendSystem()));
				wrkLob.setBackendSystem(parentWokLob.getBackendSystem());
			if(NbaUtils.isBlankOrNull(wrkLob.getDistChannel()));
				wrkLob.setDistChannel(String.valueOf(parentWokLob.getDistChannel()));
			if(NbaUtils.isBlankOrNull(wrkLob.getLastName()));
				wrkLob.setLastName(parentWokLob.getLastName());
			if(NbaUtils.isBlankOrNull(wrkLob.getFirstName()));
				wrkLob.setFirstName(parentWokLob.getFirstName());
			if(NbaUtils.isBlankOrNull(wrkLob.getMiddleInitial()));
				wrkLob.setMiddleInitial(parentWokLob.getMiddleInitial());
			if(NbaUtils.isBlankOrNull(wrkLob.getDOB()));
				wrkLob.setDOB(parentWokLob.getDOB());
			if(NbaUtils.isBlankOrNull(wrkLob.getGender()));
				wrkLob.setGender(parentWokLob.getGender());
			if(NbaUtils.isBlankOrNull(wrkLob.getSsnTin()));
				wrkLob.setSsnTin(parentWokLob.getSsnTin());

		} catch (NbaBaseException nbe) {
			getLogger().logException(nbe);
		}

	}

	//APSL3643 new method
	protected boolean isReqForExistingUniqueID(String reqUniqueID){
		Policy policy = nbaTxLife.getPrimaryHolding().getPolicy();
		int count = policy.getRequirementInfoCount();
		for (int i = 0; i < count; i++) {
			RequirementInfo reqInfo = policy.getRequirementInfoAt(i);
			if (!NbaUtils.isBlankOrNull(reqInfo.getRequirementInfoUniqueID()) && !NbaUtils.isBlankOrNull(reqUniqueID)) {
				if (reqInfo.getRequirementInfoUniqueID().equals(reqUniqueID)) {
					return true;
				}
			}
		}
		return false;
	}

	//APSL3941 New Method
	public boolean isPDR(long reqCode){
		if(reqCode == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT) {
			return true;
		}
		return false;
	}

	// APSL4213
	public void addJointInsParty(NbaDst newWorkItem) throws NbaBaseException
	{
		NbaOLifEId oLifeid = new NbaOLifEId(nbaTxLife);
		Party jointInsParty = new Party();
		oLifeid.setId(jointInsParty);
		jointInsParty.setActionAdd();
		OLifEExtension oLifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PARTY);
		jointInsParty.addOLifEExtension(oLifeExt);
		nbaTxLife.getOLifE().addParty(jointInsParty);
		Relation jointInsRelation = new Relation();
		jointInsRelation.setRelatedObjectID(jointInsParty.getId());
		jointInsRelation.setRelationRoleCode(NbaOliConstants.OLI_REL_JOINTINSURED);
		jointInsRelation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);
		jointInsRelation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);
		jointInsRelation.setRelatedRefID("01");
		Holding aHolding = nbaTxLife.getPrimaryHolding();
		if (aHolding != null) {
			jointInsRelation.setOriginatingObjectID(aHolding.getId());
		}
		oLifeid.setId(jointInsRelation);
		jointInsRelation.setActionAdd();
		OLifEExtension relationExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_RELATION);
		jointInsRelation.addOLifEExtension(relationExt);
//		RelationExtension jointInsRelationExt = relationExt.getRelationExtension();
//		if (jointInsRelationExt != null) {
//			jointInsRelationExt.setActionAdd();
//		}
		nbaTxLife.getOLifE().addRelation(jointInsRelation);

		PersonOrOrganization personOrOrganization = new PersonOrOrganization();
		personOrOrganization.setActionAdd();

		Person aPerson = new Person();

		aPerson.setFirstName(newWorkItem.getNbaLob().getFirstName());
		aPerson.setLastName(newWorkItem.getNbaLob().getLastName());
		aPerson.setGender(newWorkItem.getNbaLob().getGender());
		aPerson.setBirthDate(newWorkItem.getNbaLob().getDOB());
		aPerson.setActionAdd();

		personOrOrganization.setPerson(aPerson);

		OLifEExtension olifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
		aPerson.addOLifEExtension(olifeExtension);
//		PersonExtension personExtension = olifeExtension.getPersonExtension();
//		if (personExtension != null) {
//			personExtension.setActionAdd();
//		}

		// jointInsParty.setFullName(NbaUtils.getFullName());
		jointInsParty.setGovtID(newWorkItem.getNbaLob().getSsnTin());
		jointInsParty.setPartyTypeCode((NbaOliConstants.OLI_PT_PERSON));
		jointInsParty.setPersonOrOrganization(personOrOrganization);
		jointInsParty.setActionUpdate();
		nbaTxLife.getOLifE().setActionUpdate();
		doContractUpdate(nbaTxLife);

	}
	//APSL4898 New Method :: PDR/ILLUS/AMENDMENT req should Auto recieve/Reviewed
		public boolean isAutoReviewNeededRequirements(long reqCode){
			if(reqCode == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT || reqCode == NbaOliConstants.OLI_REQCODE_SIGNILLUS
					|| reqCode == NbaOliConstants.OLI_REQCODE_AMENDMENT) {
				return true;
			}
			return false;
		}

	 /*APSL4768 new method
	 * This method will auto generate ContractChangeWorkItem whenever result is received
	 * for System-Matic Form requirement for a not CIPE case and
	 * and it will be aggareagted to PICM
	 * */
	protected void createContractChangeWorkItem(RequirementInfo requirementInfo){
		try {
			Policy policy = nbaTxLife.getPrimaryHolding().getPolicy();
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo());
			Map deOink = new HashMap();
			deOink.put("A_CreateContractChangeWI", "true");
			NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), nbaTxLife, deOink);
			if (requirementInfo != null && requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_AUTHEFT
					&& appInfoExt.getInitialPremiumPaymentForm() != NbaOliConstants.NBA_InitialPremiumPaymentForm) {
				NbaDst caseDst = (NbaDst) getMatchingCases().get(0);
				if (caseDst != null) {
					NbaTransaction transaction = caseDst.addTransaction(workProvider.getWorkType(), workProvider.getInitialStatus());
					if (transaction != null) {
						transaction.getNbaLob().setNoChngFrmReqd(true);
						transaction.getNbaLob().setCompany(work.getNbaLob().getCompany());
						transaction.getNbaLob().setPlan(work.getNbaLob().getPlan());
						transaction.getNbaLob().setPolicyNumber(work.getNbaLob().getPolicyNumber());
						transaction.getNbaLob().setAppDate(work.getNbaLob().getAppDate());
						transaction.getNbaLob().setContractChgType(String.valueOf(NbaOliConstants.OLIX_CHANGETYPE_REISSUE));
						transaction.getNbaLob().setRequiresUnderwriting(false);
						transaction.getNbaLob().setBackendSystem(work.getNbaLob().getBackendSystem());
						transaction.getNbaLob().setOperatingMode(work.getNbaLob().getOperatingMode());
						caseDst.getNbaLob().setContractChgType(String.valueOf(NbaOliConstants.OLIX_CHANGETYPE_REISSUE));
						caseDst.getNbaLob().setRequiresUnderwriting(false);
						caseDst.getNbaLob().setNoChngFrmReqd(true);
						caseDst.setActionUpdate();
					}
				}
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Added contract change transaction to case");
				}
			}
		} catch (Exception e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getAWDFailStatus()));
		}
	}


	/**
	 * @return the reqSignDate
	 */
	//APSL4872 New method
	public Date getReqSignDate() {
		return reqSignDate;
	}

	/**
	 * @param reqSignDate the reqSignDate to set
	 */
	//APSL4872 New method
	public void setReqSignDate(Date reqSignDate) {
		this.reqSignDate = reqSignDate;
	}

	/*
	 * APSL4768 new method
	 * This method will return true if policy status is Underwriter Approval otherwise false
	 */
	public boolean hasUnderwriterApprovalStatus() {
		String status = nbaTxLife.getPrimaryContractStatus();
		if (status != null && status.equals(NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_0010)) {
			return true;
		}
		return false;
	}
	/*
	 * APSL5312 -- new Method to check is requirement should suspend.If ReqCode is for
	 * Reg 60 Disclosure Statement Signed by the Agent (1009800175) Then first it should check
	 * If Disclosure sent from Reg60 CM Then Only Req should go for Follow Up
	 *
	 */
	private boolean isSuspendRequired() {
		Policy policy = getNbaTxLife().getPrimaryHolding().getPolicy();
		NbaLob lob = getWork().getNbaLob();
		try {
			if (lob != null && lob.getWorkType().equals(A_WT_REQUIREMENT)
					&& getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_1009800175) {
				if (policy.getApplicationInfo() != null) {
					ApplicationInfo appInfo = policy.getApplicationInfo();
					ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
					if (appInfoExt != null && appInfoExt.getDisclosureSentCode() != 1) {
						return true;
					}
				}
				//NBLXA-1611 start
			}else if(lob != null && lob.getWorkType().equals(A_WT_REQUIREMENT)
					&& getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_TRUSTEDCONTACT){
				return true;
				//NBLXA-1611 end
			} else if(isLNRCRequirement(lob)){ //NBLXA-2072
				return true;
			}
		} catch (NbaBaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	// APSL5335 START :: New method to find Cases which is six months old, MISCMAIL or TEMPREQ should not match.
	private void filterGreaterThanSixMonthsCases() throws NbaBaseException {
		if (A_WT_TEMP_REQUIREMENT.equalsIgnoreCase(getWorkType()) || A_ST_MISC_MAIL.equalsIgnoreCase(getWorkType())) {
			NbaDst matchingTransWI = null;
			Iterator it = getMatchingCases().iterator();
			getLogger().logDebug("Iterator "+it);
			while (it.hasNext()) {
				matchingTransWI = (NbaDst) it.next();
				if (matchingTransWI != null && matchingTransWI.getNbaLob() != null) {
					getLogger().logDebug("Now in When Mathcin WI is not null");
					Policy policy = null;
					PolicyExtension polExt = null;
					String polStatus = null;
					Date dispDate = null;
					Date dateForSixMonthsOldCases = null;
					String polNo = matchingTransWI.getNbaLob().getPolicyNumber();
					getLogger().logDebug("Now in When Mathcin WI is not null and Pol no--"+polNo);
					getLogger().logDebug("Company Code ---"+matchingTransWI.getNbaLob().getCompany());

					getLogger().logDebug("back End System -- "+matchingTransWI.getNbaLob().getBackendSystem());
					String businessProcess = NbaUtils.getBusinessProcessId(getUser());
					getLogger().logDebug("Now in When Mathcin WI is not null and businessProcess --"+businessProcess);
					if (polNo != null) {
						getLogger().logDebug("Policy no is not null");
						NbaTXLife nbaTXLife = doHoldingInquiry(matchingTransWI, READ, businessProcess);
						getLogger().logDebug("nbaTXLife Found-- "+nbaTXLife);
						if (nbaTXLife != null && nbaTXLife.getPolicy()!=null && nbaTXLife.getPolicy().getApplicationInfo() != null
								&& NbaUtils.getAppInfoExtension(nbaTXLife.getPolicy().getApplicationInfo()) != null) {
							getLogger().logDebug("When nbaTXLife Found and APP,APPINFO,POLICY Found-- ");
							ApplicationInfo appInfo = nbaTXLife.getPolicy().getApplicationInfo();
							ApplicationInfoExtension appInfoExt = NbaUtils.getAppInfoExtension(appInfo);
							if (nbaTXLife.getPrimaryHolding().getPolicy() != null) {
								policy = nbaTXLife.getPrimaryHolding().getPolicy();
							}
							if (policy != null) {
								polExt = NbaUtils.getFirstPolicyExtension(policy);
							}

							dateForSixMonthsOldCases = NbaUtils.getDateFromStringInAWDFormat(NbaConfiguration.getInstance()
									.getBusinessRulesAttributeValue(NbaConstants.SIX_MONTHS_OLD_DISP_CASES_DATE));

							if (polExt != null) {
								polStatus = polExt.getPendingContractStatus();
								if (polStatus != null && polStatus.equals(NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_0001)) { // 0001 for Issued Case
									Date issueDate = policy.getIssueDate();
									if (issueDate != null && issueDate.compareTo(dateForSixMonthsOldCases) >= 0) {
										dispDate = policy.getIssueDate();
									} else {
										dispDate = getDispDateFromAudit(polNo, PROC_ISSUE);
										//policy.setIssueDate(dispDate);
										//policy.setActionUpdate();
									}
									if (isIgnoreMatchingCase(dispDate)) {
										sixMonthsOldAppMatchingCases.add(polNo);
										it.remove();
										getIgnoredMatchingCases().add(matchingTransWI);
									}
								} else if (polStatus != null && NbaUtils.isPolicyNegativeDisposed(polStatus)) {
									if (nbaTXLife.isInformalApplication()) {
										if (appInfo != null && appInfo.hasHOCompletionDate()) {
											dispDate = appInfo.getHOCompletionDate();
											if (isIgnoreMatchingCase(dispDate)) {
												it.remove();
												sixMonthsOldAppMatchingCases.add(polNo);
												getIgnoredMatchingCases().add(matchingTransWI);
											}
										}
									} else {
										List tentativeDispList = appInfoExt.getTentativeDisp();
										if (tentativeDispList != null && tentativeDispList.size() == 1) {
											TentativeDisp tentDisp = (TentativeDisp) tentativeDispList.get(0);
											dispDate = tentDisp.getDispDate();
										} else if (tentativeDispList != null && tentativeDispList.size() > 1) {
											int count = tentativeDispList.size();
											int initailLevelValue = 0;
											int nextLevelvalue = 0;
											TentativeDisp nextTentDisp = null;
											for (int i = 0; i < count - 1; i++) {
												TentativeDisp tentDisp = (TentativeDisp) tentativeDispList.get(i);
												nextTentDisp = (TentativeDisp) tentativeDispList.get(i + 1);
												long tantativeDisp = tentDisp.getDisposition();
												long nexttantativeDisp = nextTentDisp.getDisposition();
												if (NbaUtils.isNegativeTantativeDisposed(tantativeDisp)) {
													initailLevelValue = tentDisp.getDispLevel();
												}
												if (NbaUtils.isNegativeTantativeDisposed(nexttantativeDisp)) {
													nextLevelvalue = nextTentDisp.getDispLevel();
												}
												if (nextLevelvalue > initailLevelValue) {
													dispDate = nextTentDisp.getDispDate();
												} else {
													dispDate = tentDisp.getDispDate();
												}

											}

										}
										if (dispDate != null && dispDate.compareTo(dateForSixMonthsOldCases) < 0) {
											dispDate = getDispDateFromAudit(polNo, PROC_FINAL_DISPOSITION);
										}

										if (isIgnoreMatchingCase(dispDate)) {
											it.remove();
											sixMonthsOldAppMatchingCases.add(polNo);
											getIgnoredMatchingCases().add(matchingTransWI);
										}
									}

								} else if (nbaTXLife.isInformalApplication()) {
									if (appInfoExt != null
											&& (appInfoExt.getInformalAppApproval() == NbaOliConstants.NBA_FINALDISPOSITION_OFFEREXPIRED || appInfoExt
													.getInformalAppApproval() == NbaOliConstants.NBA_FINALDISPOSITION_OFFERACCEPTED)) {
										if (appInfoExt != null && appInfoExt.hasInformalOfferDate()) {
											dispDate = appInfoExt.getInformalOfferDate();
											if (isIgnoreMatchingCase(dispDate)) {
												it.remove();
												sixMonthsOldAppMatchingCases.add(polNo);
												getIgnoredMatchingCases().add(matchingTransWI);
											}
										}

									}

								}
								//Begin NBLXA-2437
								if(dispDate== null && appInfo != null && appInfo.getSubmissionDate()!= null) {
									dispDate = appInfo.getSubmissionDate();
									if (isIgnoreMatchingCaseForAYear(dispDate)) {
										it.remove();
										twelveMonthsOldAppMatchingCases.add(polNo);
										getIgnoredMatchingCases().add(matchingTransWI);
									}
								}
								//End NBLXA-2437
							}
						}
					}
				}
			}
		}

	}

	// APSL5335 START ::  New Method when Disp Date would less then AUG Release Date then fetch the date from Audit DB
	private Date getDispDateFromAudit(String contractNo,String businessFunction) throws NbaDataAccessException {
		Date dispDate = null;
		getLogger().logDebug("Preparing to execute SELECT Query From Audit to find Disp Date");

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList results = new ArrayList();
		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfiguration.NBAAUDIT);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			stmt = conn.prepareStatement("SELECT  max(EVENTTIMESTAMP) AS DISPDATE from NBA_TXN_HISTORY WHERE CONTRACTKEY = ? AND BUSINESSFUNCTION = ? ");
			stmt.setString(1, contractNo);
			stmt.setString(2, businessFunction);
			rs = stmt.executeQuery();
			while (rs.next()) {
				dispDate = rs.getDate("DISPDATE");
				//results.add(autoClosureContract);
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Returned " + results.size() + " rows");
			}
		} catch (Throwable t) {
			throw new NbaDataAccessException("NBA_TXN_HISTORY select for list of contract to be Date failed ", t);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				NbaConnectionManager.returnConnection(conn);
				//logElapsedTime(startTime);
			} catch (Throwable t) {
				getLogger().logException("NBA_TXN_HISTORY select for list of contract to be Date failed " , t);
			}
		}
		return dispDate;
	}


	 // NBLXA-1787 - This method will retrieve polciy number for a contract key and return count for Policy object
    private int validatePolicyNumber(String polnumber) throws NbaBaseException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count =0;
		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfiguration.NBAPEND);
			String query = "select count(*) as count from Policy pol where  pol.parentidkey = 'Holding_1' and pol.contractkey = ?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, polnumber);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count= rs.getInt("count");
			}
		} catch (SQLException sqle) {
			NbaLogFactory.getLogger(this.getClass()).logException(sqle);
			throw new NbaBaseException("An error occured while performing DB operation for validatePolicyNumber.", sqle);
		} catch (NbaBaseException nbe) {
			nbe.forceFatalExceptionType();
			NbaLogFactory.getLogger(this.getClass()).logException(nbe);
			throw new NbaBaseException("An error occured while validatePolicyNumber .", nbe);
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
			} catch (Throwable t) {
				getLogger().logException("An error occured while closing connection in finally validatePolicyNumber" , t);
			}
		}
		return count;
	}

	// APSL5335 START :: Check when no Of Days will greater then 180 Days.
	protected boolean isIgnoreMatchingCase(Date dispDate) {
		Date currentDate = new Date();
		double dateDiff = 180;
		if (dispDate != null) {
			double days = (currentDate.getTime() - dispDate.getTime()) / (1000D * 60 * 60 * 24);
			if (days > dateDiff) {
				return true;
			}
		}

		return false;

	}

	// NBLXA-2437 START :: Check when no Of Days will greater then 360 Days.
		protected boolean isIgnoreMatchingCaseForAYear(Date dispDate) {
			Date currentDate = new Date();
			double dateDiff = 360;
			if (dispDate != null) {
				double days = (currentDate.getTime() - dispDate.getTime()) / (1000D * 60 * 60 * 24);
				if (days > dateDiff) {
					return true;
				}
			}

			return false;

		}

	protected String getCommentsWithSixMonthsOldDispPolicy(List sixMontshOldAppMatchingCases,int month) {
		StringBuffer matchingWIComment = new StringBuffer();
		String polNumber;
		String matchingWICommentStr = null;
		matchingWIComment.append("Could not match to policies as these were Disposed/Issued "+month+" months back: ");
		for (int i = 0; i < sixMontshOldAppMatchingCases.size(); i++) {
			polNumber = (String) sixMontshOldAppMatchingCases.get(i);
			matchingWIComment.append(polNumber);
			matchingWIComment.append(", ");
		}
		matchingWICommentStr = matchingWIComment.toString();
		if (matchingWICommentStr.length() > 0) {
			matchingWICommentStr = matchingWIComment.substring(0, matchingWIComment.length() - 2); // Remove last comma and space
		}

		return matchingWICommentStr;
	}

	//NBLXA-188 - New Method
	protected List getPolicyNumberList(String companyCode,String policyNumber,int batchSize) throws NbaBaseException {//NBLXA-1680
		NbaSystemDataDatabaseAccessor nbaSystemDataDBAccessor = new NbaSystemDataDatabaseAccessor();
		List<String> policyNumberList = nbaSystemDataDBAccessor.getPolicyNumbersForReleaseBatchId(companyCode,policyNumber,batchSize);
		return policyNumberList;
	}

	/**
	 * Creates a Transaction work item
	 * Attach Source to the work item.
	 * @throws NbaBaseException
	 */
	//NBLXA-188 - New Method
	protected NbaDst constructWorkItem(String policyNumber,NbaSource nbaSource,NbaDst dst) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Creating workitem for "+ policyNumber);
	    	}
		WorkItem awdTransaction = new WorkItem();
		awdTransaction.setBusinessArea(NbaConstants.A_BA_NBA);
		awdTransaction.setWorkType(NbaConstants.A_WT_MISC_MAIL);
		awdTransaction.setStatus(getWork().getStatus());
		awdTransaction.setLock("Y");
		awdTransaction.setAction(ACTION_LOCK);
		awdTransaction.setRecordType(WT_TRANSACTION);
		awdTransaction.setCreate("Y");
		NbaDst nbaDst = new NbaDst();
		NbaSystemDataDatabaseAccessor nbaSystemDataDBAccessor = new NbaSystemDataDatabaseAccessor();
		List<String> requirementMatchingCriteria = nbaSystemDataDBAccessor.getRequirementMatchingCriteria(policyNumber);
		nbaDst.setWork(new NbaTransaction(awdTransaction));
		nbaDst.addNbaSource(nbaSource);
		NbaLob lobdata = nbaDst.getNbaLob();
		lobdata.setPolicyNumber(policyNumber);
		lobdata.setReqType((int)(NbaOliConstants.OLI_REQCODE_POLDELRECEIPT));
		lobdata.setFirstName(requirementMatchingCriteria.get(0));
		lobdata.setLastName(requirementMatchingCriteria.get(1));
		lobdata.setSsnTin(requirementMatchingCriteria.get(2));
		nbaDst.getNbaLob().setGIWorkItemInd(true);
		return nbaDst;
	}

	//NBLXA-188 - New Method
	protected void updatePolicyNumbers(String policyNumber) throws NbaBaseException {
	    if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Updating PDR_UPDATE_IND for policy "+ policyNumber);
    		}
	    	NbaSystemDataDatabaseAccessor nbaSystemDataDBAccessor = new NbaSystemDataDatabaseAccessor();
	    	nbaSystemDataDBAccessor.updateGIPolicyNumber(policyNumber);
	}

	//NBLXA-188 - New Method
	protected void generatePDRMiscWorkForGIPolicies(String companyCode) throws NbaBaseException {
		NbaSource aSource = null;
		List sources = getWork().getNbaSources();

		for (int i = 0; i < sources.size(); i++) {
			aSource = (NbaSource) sources.get(i);
			if (aSource.getSource().getSourceType().equals(NbaConstants.A_WT_MISC_MAIL)) {

				// Retrieves batchSize from the NbaConfiguration.xml file,which sets maximum rows to be retreived from the table
				int policyBatchSize = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
						NbaConfigurationConstants.GI_BATCH_SIZE));

				updateRequired = false;
				List<String> policyNumberList = getPolicyNumberList(companyCode,getWork().getNbaLob().getPolicyNumber(), policyBatchSize);//NBLXA-1680
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("GI policy number list size retrieved from AXA_GIAPP_SYSTEM_DATA table:" + policyNumberList.size());
				}
				for (int j = 0; j < policyNumberList.size(); j++) {
					update(constructWorkItem(policyNumberList.get(j).toString(), aSource, getWork())); // Constructs NBMISCMAIL workitem for the
																										// retrieved policy.
					updatePolicyNumbers(policyNumberList.get(j)); // Updates PDR_UPDATE_IND as true,which indicates NBMISCMAIL workitem is created for
																	// this policy.
				}
				if (policyNumberList.size() < policyBatchSize) {
					updateRequired = true; // when all the policies are processed for the batch,then only update workitem to further queue.
				}
			}
		}
	}

	// NBLXA-1732 new method
	public boolean isArchAgent() throws NbaDataAccessException {
		String producerid = "";
		Relation bgaRelation = getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
		Relation relation = getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
		NbaParty party = null;
		if (relation != null) {
			party = getNbaTxLife().getParty(relation.getRelatedObjectID());
		} else if (bgaRelation != null) {
			party = getNbaTxLife().getParty(bgaRelation.getRelatedObjectID());
		}
		if (party != null) {
			CarrierAppointment carrierAppointment = party.getParty().getProducer().getCarrierAppointmentAt(0);
			if (carrierAppointment != null) {
				producerid = carrierAppointment.getCompanyProducerID();
			}
		}
		if (NbaUtils.isEarcAgent(producerid)) {
			return true;
		}
		return false;
	}

	// NBLXA-1732 new method
	public boolean isSuspendNeeded(NbaLob lob) throws NbaDataAccessException, NbaBaseException {
		//NBLXA-2184 Fix Task 319195/313502
		if(isMVRRequirementForLexisNexis(lob) && lob.getSuspensionCount() != null && lob.getSuspensionCount().equalsIgnoreCase(NbaConstants.LOB_SUSPENSION_COUNT_02)){
			return false;
		}else if (((NbaUtils.isRetail(getNbaTxLife().getPolicy()) && !isArchAgent()) || NbaUtils.isWholeSale(getNbaTxLife().getPolicy())) && isUWCMFollowup()) {
			return true;
		}
		return false;
	}

	// NBLXA-1732 new method
	protected boolean isUWCMFollowup() throws NbaBaseException {
		NbaDst nbaDst = getWork();
		NbaUserVO userVO = getUser();
		try {
			if (!NbaUtils.isAdcApplication(nbaDst)) {
				Map deOinkMap = new HashMap();
				boolean matchedStatus = false;
				NbaUserVO userVOTemp = new NbaUserVO();
				userVOTemp.setUserID("TA2ORDERD");
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(userVOTemp, nbaDst, getNbaTxLife(), deOinkMap);
				String vpmsStatus = provider.getInitialStatus();
				for (int z = 0; z < NbaConstants.UWCM_STATUS.length; z++) {
					String uwcmStatus = NbaConstants.UWCM_STATUS[z];
					if (uwcmStatus.equalsIgnoreCase(vpmsStatus)) {
						matchedStatus = true;
						break;
					}
				}
				return matchedStatus;
			}
		} catch (Exception e) {
			NbaUtils.addGeneralComment(nbaDst, userVO, "Error accessing workitem status for UWCM during A2ORDERD, error message - " + e.getMessage());
			throw new NbaBaseException(e);
		}
		return false;
	}

	/*
	 * Check for LNRC requirement
	 */
	//NBLXA-2072 New Method
	private boolean isLNRCRequirement(NbaLob lob) throws NbaBaseException{
		if(lob != null && lob.getWorkType().equals(A_WT_REQUIREMENT)
					&& lob.getReqType() == NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER){
				return true;
		}
		return false;
	}

	//NBLXA-2072 New Method
	private boolean isMVRRequirementForLexisNexis(NbaLob lob) throws NbaBaseException{
		if(lob != null && lob.getWorkType().equals(A_WT_REQUIREMENT)
					&& lob.getReqType() == NbaOliConstants.OLI_REQCODE_MVRPT
					&& lob.getReqVendor() != null && PROVIDER_LEXISNEXIS.equalsIgnoreCase(lob.getReqVendor())){
				return true;
		}
		return false;
	}

	// NBLXA-2072 new method
	private void copyRiskClassifierAttachmentDetails(ArrayList attachmentList, NbaTXLife txLife, String partyId) {
		for (int j = 0; !NbaUtils.isBlankOrNull(attachmentList) && j < attachmentList.size(); j++) {
			Attachment attach = (Attachment) attachmentList.get(j);
			long attType = attach.getAttachmentType();
			if (attach.getAttachmentBasicType() == NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT && attType == NbaOliConstants.OLI_ATTACH_REQUIRERESULTS) {
				AttachmentData attachData = attach.getAttachmentData();
				if (attachData != null) {
					String pcData = attachData.getPCDATA();
					try {
						NbaTXLife txLifeReqRslt = new NbaTXLife(pcData);
						if (txLifeReqRslt != null && txLifeReqRslt.getTransType() == NbaOliConstants.TC_TYPE_GENREQUIRERESTRN) {
							NbaRequirementUtils.copyRiskClassifierData(txLifeReqRslt, txLife, partyId);
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	// NBLXA-2072 new method
	private void copyRiskClassifierSourceResult(NbaSource aSource, NbaTXLife txLife, String partyId) {
		if (aSource != null && !NbaUtils.isBlankOrNull(aSource.getText())) {
			try {
				NbaTXLife rsltTxLife = new NbaTXLife(aSource.getText());
				NbaRequirementUtils.copyRiskClassifierData(rsltTxLife, txLife, partyId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the deliveryRecepitSignDate
	 */
	public Date getDeliveryRecepitSignDate() {
		return deliveryRecepitSignDate;
	}

	/**
	 * @param deliveryRecepitSignDate the deliveryRecepitSignDate to set
	 */
	public void setDeliveryRecepitSignDate(Date deliveryRecepitSignDate) {
		this.deliveryRecepitSignDate = deliveryRecepitSignDate;
	}

	//QC20240 New Method
	public String getReqReceiptDateTime() {
	       return reqReceiptDateTime;
	}
	/**
		* Sets the reqReceiptDateTime
		* @param the reqReceiptDateTime to set.
	*/
	//QC20240 New Method
	public void setReqReceiptDateTime(String reqReceiptDateTime) {
		this.reqReceiptDateTime = reqReceiptDateTime;
	}

	//NBLXA-2184 New Method
	private void copyProductReferenceNumber(NbaSource aSource, NbaTXLife txLife, String partyId) {
		if (aSource != null && !NbaUtils.isBlankOrNull(aSource.getText())) {
			try {
				NbaTXLife rsltTxLife = new NbaTXLife(aSource.getText());
				NbaRequirementUtils.copyProductReferenceNumber(rsltTxLife, txLife, partyId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//NBLXA-2072 New Method
	protected void determineMVRSuspension(NbaDst permWorkItem, NbaDst tempWorkItem, NbaSource aSource) throws NbaBaseException {
		if (NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER == tempWorkItem.getNbaLob().getReqType()
				&& NbaConstants.A_ST_PROVIDER_SUPPLEMENT.equalsIgnoreCase(aSource.getSource().getSourceType())) {
			if (!isMVRResponsePresent(getSourceText(aSource.getText()))) {
				NbaDst permWorkitemCaseWithSiblings = retrieveParentWork(permWorkItem, true, true);
				NbaTransaction nbaTransactionMVR = retrieveMVRWorkItem(permWorkitemCaseWithSiblings);
				if (nbaTransactionMVR != null) {
					NbaSuspendVO suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(nbaTransactionMVR.getID());
					unsuspendWork(suspendVO);
				}
			}
		}
	}

	// NBLXA-2410 New method
	public void updateRequirementTrackingIDAndOrderNum(RequirementInfo requirementInfo, NbaSource aSource) throws NbaBaseException {
		if (aSource != null && !NbaUtils.isBlankOrNull(aSource.getText())) {
			try {
				NbaTXLife txLifeReqResult = new NbaTXLife(aSource.getText());
				if (txLifeReqResult!=null && txLifeReqResult.getPolicy() != null) {
					Policy policy = txLifeReqResult.getPolicy();
					for (int i = 0; i < policy.getRequirementInfoCount(); i++) {
						if (policy.getRequirementInfoAt(i).getReqCode() == requirementInfo.getReqCode()
								&& !NbaUtils.isBlankOrNull(policy.getRequirementInfoAt(i).getProviderOrderNum())) {
							requirementInfo.setProviderOrderNum(policy.getRequirementInfoAt(i).getProviderOrderNum());
							requirementInfo.setActionUpdate();
						}
					}
					if (policy.getApplicationInfo() != null) {
						String trackingID = policy.getApplicationInfo().getTrackingID();
						if (!NbaUtils.isBlankOrNull(trackingID)) {
							RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
							TrackingInfo tracinfo = requirementInfoExt.getTrackingInfo();
							if (tracinfo != null) {
								tracinfo.setTrackingNum(trackingID);
								tracinfo.setActionUpdate();
							} else {
								TrackingInfo trackingInfo = new TrackingInfo();
								trackingInfo.setTrackingNum(trackingID);
								trackingInfo.setActionAdd();
								requirementInfoExt.setTrackingInfo(trackingInfo);
								requirementInfoExt.setActionUpdate();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	// New Method NBLXA-2421
		/**
		 * This function retrieve the First Name of the Requirement and compare to TempReq/MiscMail FirstName If matches return true
		 *
		 * @param NbaDst
		 * @return - firstNameFound
		 */
		private boolean isFirstNameMatch(NbaDst currentWorkItem, NbaDst matchingWorkItem) {
			boolean firstNameFound = false;
			String firstName = currentWorkItem.getNbaLob().getFirstName();
			if ((!NbaUtils.isBlankOrNull(firstName)) && firstName.equalsIgnoreCase(matchingWorkItem.getNbaLob().getFirstName())) {
				firstNameFound = true;
			}
			return firstNameFound;
		}

	// Begin NBLXA-2493
	private void copyAgentDetails(NbaDst currentWorkItem, NbaDst matchingWorkItem) {
		String firstName = matchingWorkItem.getNbaLob().getReqAgentFirstName();
		if (!NbaUtils.isBlankOrNull(firstName)) {
			currentWorkItem.getNbaLob().setReqAgentFirstName(firstName);
		}
		String lastName = matchingWorkItem.getNbaLob().getReqAgentLastName();
		if (!NbaUtils.isBlankOrNull(lastName)) {
			currentWorkItem.getNbaLob().setReqAgentLastName(lastName);
		}
		String agentId = matchingWorkItem.getNbaLob().getAgentID();
		if (!NbaUtils.isBlankOrNull(agentId)) {
			currentWorkItem.getNbaLob().setAgentID(agentId);
		}
	}
    //End NBLXA-2493

	// Begin NBLXA-2617
	private void updateVendorAndTrackingProvider(RequirementInfo requirementInfo, NbaDst permWorkItem, NbaDst tempWorkItem) {
		String vendor = tempWorkItem.getNbaLob().getReqVendor();
		if (!NbaUtils.isBlankOrNull(vendor)) {
			permWorkItem.getNbaLob().setReqVendor(vendor);
			RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
			TrackingInfo tracinfo = requirementInfoExt.getTrackingInfo();
			if (tracinfo != null) {
				tracinfo.setTrackingServiceProvider(vendor);
				tracinfo.setActionUpdate();
			}
		}
	}
	// End NBLXA-2617

}