package com.csc.fsg.nba.business.process;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.text.ParseException;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fs.Message;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.LockRetrieveWorkRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.bean.accessors.NbaCompanionCaseFacadeBean;
import com.csc.fsg.nba.business.uwAssignment.AxaUnderwriterAssignmentEngine;
import com.csc.fsg.nba.contract.validation.NbaContractValidationConstants;
import com.csc.fsg.nba.database.AxaRulesDataBaseAccessor;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaConfigurationException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.AxaReassignDataVO;
import com.csc.fsg.nba.vo.AxaUWAssignmentEngineVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaLookupResultVO;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaReassignmentQueuesVO;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransDate;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaTransactionSearchResultVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.AutomatedProcess;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.webservice.client.AxaInvokeWebservice;
import com.csc.fs.nba.utility.Nba331Conversion;

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

/**
 * NbaProcRouteWI is the class to process WI and route them to given queue/Status.
 *
 * @author CSC FSG Developer
 * @version NB-1001
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version NB-1001
 */
public class NbaProcRouteWI extends NbaAutomatedProcess implements NbaOliConstants{

	protected static final String WORKTYPE = "WorkType";
	protected String searchWorkType = A_WT_REQUIREMENT;
	protected static final String REQUIREMENTTYPE = "RQTP";
	protected int searchrequirementType = (int)OLI_REQCODE_MEDEXAMPARAMED;
	protected static final String TRANSACTION_SEARCH_VO = "NbaTransactionSearchResultVO";
	protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";
	protected static final DateFormat awdDateFormat = new SimpleDateFormat(NbaTransDate.AWD_DATETIMEFORMAT);
	protected static final String forfetchImageUtility = "ForFetchImageUtility";
	protected static final String moveToEndUtility = "moveToEndUtility"; //APSL5415
	protected String status;
	protected String routeReason;
	protected NbaDst parent = null;
	protected NbaTableAccessor nbaTableAccessor;
	private NbaTXLife request1203DataFeed;
	private AxaReassignDataVO reassignDataVo;
	HashMap <String,String> queueMap = new HashMap <String,String>();
	protected static final String UNDERWRITER = "UNDQ"; //NBLXA-1957
	protected static final String CASE_MANAGER = "CSMQ"; //NBLXA-1957
	NbaSearchVO searchVO = null; //NBLXA-2150
	String[] selectedReqUniqueIDList = null; //NBLXA-2150
	protected List unsuspendVOs = new ArrayList(); //NBLXA-2150
	protected NbaLogger arcoslogger = null; //NBLXA-2085

	/**
	 * NbaProcRequirementHold constructor comment.
	 */
	public NbaProcRouteWI() {
		super();
	}

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
	// Begin NBLXA-1538
		if (!initialize(user, work)) {
			return getResult();
		}
		AxaReassignDataVO reassignDataVo = NbaSystemDataDatabaseAccessor.selectReassingmentProcessing();
		setReassignDataVo(reassignDataVo);
		String PolNumber = reassignDataVo.getPolicynumber();
		String CreateDatetime=reassignDataVo.getCreateDateTime();
		String changedType = reassignDataVo.getChangedType();
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
		try {
			if (changedType != null && changedType.equals(JOB_DISTRIBUTION_CHANNEL)) {
				String changedValue = reassignDataVo.getChangedValue();
				String origValue = (changedValue.equals("10")) ? "6" : "10";
				changeDistributionChannel(changedValue, origValue, PolNumber, user);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}

			if (changedType != null && changedType.equals(JOB_AGENT_UPDATE)) {
				String origCaseManager = reassignDataVo.getChangedValue();
				updateCaseManager(origCaseManager, PolNumber, user);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			// Begin NBLXA-1829
			if (changedType != null && changedType.equals(JOB_CV_VALIDATE)) {
				List policyList = readInputFile(PolNumber, changedType);
				String busFunction = reassignDataVo.getChangedValue();
				validatePolicies(policyList, busFunction, PolNumber,reassignDataVo.getUserCode());//NBLXA-2156
				NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(PolNumber, JOB_CV_VALIDATE, busFunction, CASE_PROCESSED);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			// END NBLXA-1829

			// Begin NBLXA-1894
			if (changedType != null && changedType.equals(JOB_PRIOR_INSURANCE_RERUN)) {
				String changedValue = reassignDataVo.getChangedValue();
				List policyList = readInputFile(PolNumber, changedType);
				priorInsuranceRefresh(policyList, PolNumber);
				NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(PolNumber, changedType, changedValue, CASE_PROCESSED);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			if (changedType != null && changedType.equals(JOB_POLICY_NOTIFICATION)) {
				String changedValue = reassignDataVo.getChangedValue();
				List policyList = readInputFile(PolNumber, changedType);
				sendPolicyNotification(policyList, PolNumber);
				NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(PolNumber, changedType, changedValue, CASE_PROCESSED);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			// END NBLXA-1894

		// Begin NBLXA-1831
		if(changedType!=null && changedType.equals(NbaConstants.JOB_GOLDEN_TICKET)) {
			String changedValue = reassignDataVo.getChangedValue();
			updateGoldenTicketInd(changedValue,PolNumber);
			NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(PolNumber, NbaConstants.JOB_GOLDEN_TICKET, changedValue,CASE_PROCESSED);
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
		}
		// End NBLXA-1831

		// Begin NBLXA-1957
		if (changedType != null && (changedType.equals(NbaConstants.JOB_BULK_ASSIGNMENT) || (changedType.equals(JOB_ARCOS_BULK_ASSIGNMENT)
				&& reassignDataVo.getProcessedTime() == null))) { // NBLXA-2529 JOB_ARCOS_BULK_ASSIGNMENT
			String changedValue = reassignDataVo.getChangedValue();
			readBulkAssigmentData(PolNumber, changedType, changedValue);
			updateQueue(changedValue);
			NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(PolNumber, changedType, changedValue, CASE_PROCESSED); // NBLXA-2529
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
		}
		// End NBLXA-1957

		if (changedType != null && changedType.equals(JOB_UI_OPERATION)) {
				doProcess();
				deleteProcessedDocument();
				NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(PolNumber, JOB_UI_OPERATION, reassignDataVo.getChangedValue(),CASE_PROCESSED);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			// Begin NBLXA-2089
			if (changedType != null && changedType.equals(JOB_PLAN_CHANGE)) {
				String changedValue = reassignDataVo.getChangedValue();
				changePlan(changedValue, CreateDatetime, user,PolNumber);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			// End NBLXA-2089
			//Begin NBLXA-2472
			if (changedType != null && changedType.equals(JOB_UWCM_REASSIGNMENT)) {
				String changedValue = reassignDataVo.getChangedValue();
				changeUWCM(PolNumber,changedValue,user);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));

			}
			//End NBLXA-2472
			// Begin NBLXA-2085
			if (changedType != null && changedType.equals(JOB_ARCOS_UPDATE) && reassignDataVo.getProcessedTime()==null  ) {
				String changedValue = reassignDataVo.getChangedValue();
				AxaRulesDataBaseAccessor dbAccessor = AxaRulesDataBaseAccessor.getInstance();
				List columnName = dbAccessor.getColumnName(changedValue);
				columnName.add(changedValue);
				List PrimaryKeycolumnName = dbAccessor.getPrimaryKeyColumnName(changedValue);
				List<String> deleteQuery = dbAccessor.getDeleteSqlQuery(changedValue, columnName, PrimaryKeycolumnName);
				List<String> insertQuery;
				if(changedValue.equalsIgnoreCase(NbaTableConstants.UW_APPROVE_WITH_RATING) || changedValue.equalsIgnoreCase(NbaTableConstants.UW_NEG_DISP)){
				insertQuery = dbAccessor.getInsertSqlQueryForAdditionalApproval(changedValue, columnName);
				}
				else{
				insertQuery = dbAccessor.getInsertSqlQuery(changedValue, columnName, PrimaryKeycolumnName);
				}
				List<String> updateQuery = dbAccessor.getUpdateSqlQuery(changedValue, columnName, PrimaryKeycolumnName);
				List<String> sqlQuery = new ArrayList();
				sqlQuery.addAll(deleteQuery);
				sqlQuery.addAll(insertQuery);
				sqlQuery.addAll(updateQuery);
				for (String sql : sqlQuery) {
					arcosLogging(sql+" Sql to be Executed");
					if (dbAccessor.getConfigDateValue(NbaConstants.ARCOS_CACHE_MODE)!=null && dbAccessor.getConfigDateValue(NbaConstants.ARCOS_CACHE_MODE).after(new Date())) {
						if (changedValue.equalsIgnoreCase(NbaTableConstants.NBA_AWD_QUEUE) &&
								!NbaUtils.isBlankOrNull(sql) && !sql.contains(NbaTableConstants.UW_WORKLOAD)) {
							getNbaTableAccessor().migrateDateToOracle(sql, NbaTableConstants.NBA_AWD_QUEUE);
						} else {
							dbAccessor.migrateDataToOracle(sql);
						}
					arcosLogging(sql + "Executed and inserted to DB");
					}
				}
				int currentVersion = dbAccessor.getVersion(changedValue);
				dbAccessor.updateVersion(String.valueOf(currentVersion++), changedValue);
				NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(PolNumber, changedType, reassignDataVo.getChangedValue(),
						CASE_PROCESSED);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			// End NBLXA-2085
			// Begin NBLXA-2150
			if(changedType != null && changedType.equals(JOB_REQUTIL)){
				String fileNameToProcess = reassignDataVo.getChangedValue();
				String[] reqUniqueIDList=readRequirementInputFile(fileNameToProcess);
				//selectedReqUniqueIDList=reqUniqueIDs.split(",");
				NbaTXLife nbaTXLife = null;
				NbaDst aWorkItem = null;
				ListIterator results = null;
				//List<String> selectedReqUniqueIDList = Arrays.asList(reqUniqueIDs);
				System.out.println("Inside Method");
					try {
						if (reqUniqueIDList != null) {
							ArrayList<RequirementInfo> selectedReqInfoList = new ArrayList();
							searchVO = searchContract(PolNumber, user);
							if (searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
								results = searchVO.getSearchResults().listIterator();
								while (results.hasNext()) {
									aWorkItem = retrieveWorkItem((NbaSearchResultVO) results.next());
									setWork(aWorkItem);
									nbaTXLife = doHoldingInquiry();
									for (int i = 0; i < reqUniqueIDList.length; i++) {
										RequirementInfo reqInfo = nbaTXLife.getRequirementInfofromUID(reqUniqueIDList[i]);
										selectedReqInfoList.add(reqInfo);
									}
									if (selectedReqInfoList != null && !selectedReqInfoList.isEmpty()) {
										waiveSelectedRequirements(nbaTXLife, selectedReqInfoList);
										NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(PolNumber, JOB_REQUTIL, fileNameToProcess, CASE_PROCESSED);
										deleteProcessedReqDocs(fileNameToProcess);
									}
								}
							}
						}
						if (reqUniqueIDList == null) {
							NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(PolNumber, JOB_REQUTIL, fileNameToProcess, CASE_PROCESSED);
						}
					} catch (NbaLockedException exception) {
						getLogger().logError("Locked workitem being bypassed.  ContractNumber:" + PolNumber);
					} catch (NbaBaseException baseException) {
						// getLogger().logDebug("RoutePoller in Main Catch - pairs.getKey()="+pairs.getKey()+",pairs.getValue()="+pairs.getValue());
						baseException.printStackTrace();
					}

				}
			// End NBLXA-2150
			//NBLXA-1932 Start
			if(changedType != null && changedType.equals(JOB_BLK_ROUTING)){
				String toStatus = reassignDataVo.getChangedValue();
				String fromStatus = reassignDataVo.getBackendKey();
				String workType = reassignDataVo.getCompanyKey();
				String policyNumber = reassignDataVo.getUserCode();
				updateBulkRoutingData(toStatus,fromStatus,workType,policyNumber);
				NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(PolNumber, NbaConstants.JOB_BLK_ROUTING, toStatus,CASE_PROCESSED);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			//NBLXA-1932 End
			//Begins NBLXA-2162
			if (changedType != null && changedType.equals(JOB_PRIMARY_INS_NAME_UPDATE)) {
				String changedValue = reassignDataVo.getChangedValue();
				String crda = reassignDataVo.getCreateDateTime();
				changePrimaryInsuredName(changedValue, crda, user,PolNumber);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			if (changedType != null && changedType.equals(JOB_PRIMARY_INS_SSN_UPDATE)) {
				String changedValue = reassignDataVo.getChangedValue();
				String crda = reassignDataVo.getCreateDateTime();
				changePrimaryInsuredSSN(changedValue, crda, user,PolNumber);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			if (changedType != null && changedType.equals(JOB_PRIMARY_INS_DOB_UPDATE)) {
				String changedValue = reassignDataVo.getChangedValue();
				String crda = reassignDataVo.getCreateDateTime();
				changePrimaryInsuredDOB(changedValue, crda, user,PolNumber);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			if (changedType != null && changedType.equals(JOB_JOINT_INS_NAME_UPDATE)) {
				String changedValue = reassignDataVo.getChangedValue();
				String crda = reassignDataVo.getCreateDateTime();
				changeJointInsuredName(changedValue, crda, user,PolNumber);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			if (changedType != null && changedType.equals(JOB_JOINT_INS_SSN_UPDATE)) {
				String changedValue = reassignDataVo.getChangedValue();
				String crda = reassignDataVo.getCreateDateTime();
				changeJointInsuredSSN(changedValue, crda, user,PolNumber);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
			}
			if (changedType != null && changedType.equals(JOB_JOINT_INS_DOB_UPDATE)) {
				String changedValue = reassignDataVo.getChangedValue();
				String crda = reassignDataVo.getCreateDateTime();
				changeJointInsuredDOB(changedValue, crda, user,PolNumber);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
		}
			//Ends NBLXA-2162

		} catch (NbaBaseException ndae) {
			AxaReassignDataVO reassignData = getReassignDataVo();
			NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(reassignData.getPolicynumber(), reassignData.getChangedType(),
					reassignData.getChangedValue(), "Active");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			throw new NbaBaseException(ndae.getMessage(), NbaExceptionType.FATAL);

		} finally { // NBLXA-2593
			NbaContractLock.removeLock(user);
		}
		return result;
	}
	//End NBLXA-1538

	//NBLXA-1932 New Method
	public NbaSearchVO updateBulkRoutingData(String toStatus,String fromStatus,String workType,String policyNumber) throws NbaBaseException{
		NbaSearchVO searchVO = new NbaSearchVO();
		ListIterator results = null;
		NbaDst aWorkItem = null;
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO.setWorkType(workType);
		searchVO.setStatus(fromStatus);
		NbaLob searchLob = new NbaLob();
		searchLob.setStatus(toStatus.trim());
		if(!policyNumber.equalsIgnoreCase("Manual")){
			searchLob.setPolicyNumber(policyNumber);
		}
		searchVO.setNbaLob(searchLob);
		searchVO = lookupWork(getUser(),searchVO);
		results = searchVO.getSearchResults().listIterator();
		NbaUserVO userVo = getUser();
		while (results.hasNext()) {
			try {
				aWorkItem = retrieveWorkItem((NbaSearchResultVO) results.next());
				aWorkItem.setStatus(toStatus);
				setWork(aWorkItem);

				NbaUtils.setRouteReason(aWorkItem,toStatus);
				updateWork(userVo, aWorkItem);
			} catch (NbaBaseException baseException) {
				baseException.printStackTrace();
			}
		}

		return searchVO;
	}

	public NbaSearchVO searchContract(String contractKey) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = lookupWork(getUser(), searchVO);
		return searchVO;
	}

	public void create1203DataFeedRequest(NbaTXLife txLife203) throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setAccessIntent(NbaConstants.READ);
		Date dataFeedDate = new Date();
		NbaTime dataFeedTime = new NbaTime();

		NbaUserVO userVo = getUser();
		userVo.setPassword("NONE");
		nbaTXRequest.setNbaUser(userVo);
		NbaTXLife nbatxlifereq = new NbaTXLife(nbaTXRequest);


		nbatxlifereq.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeDate(dataFeedDate);
		nbatxlifereq.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeTime(dataFeedTime);
		nbatxlifereq.setOLifE(txLife203.getOLifE());

		nbatxlifereq.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE().getSourceInfo().setCreationDate(dataFeedDate);
		nbatxlifereq.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE().getSourceInfo().setCreationTime(dataFeedTime);
		NbaUtils.updateDeliveryInstruction(nbatxlifereq);
		request1203DataFeed = nbatxlifereq;
	}


	public NbaDst retrieveWorkItemCSMQ(NbaSearchResultVO resultVO) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setNbaUserVO(getUser());
		retOpt.setWorkItem(resultVO.getWorkItemID(), true);
		retOpt.setLockWorkItem();
		aWorkItem = retrieveWorkItem(getUser(), retOpt);
		return aWorkItem;
	}

	private NbaTXLife resetCMCOntactName(NbaTXLife txlife, NbaDst dst) throws NbaBaseException {

		String cmQueue = dst.getNbaLob().getCaseManagerQueue();
		ApplicationInfo appinfo = txlife.getPrimaryHolding().getPolicy().getApplicationInfo();
		if (null != cmQueue && cmQueue.length() > 0) { //ALII1623, if CM Queue is set on case with value other than N2OVR..
			String userId = getUserIdForQueue(cmQueue); //ALII1623
			if(userId != null && userId.length() > 0){ //ALII1623
				appinfo.setNBContactName(userId); //ALII1623
				appinfo.setActionUpdate();//ALS5409,ALS5410
				txlife.setBusinessProcess("REASSIGNMENT_QUEUE");
				txlife.setAccessIntent(NbaConstants.UPDATE); //SPR1851
				txlife = NbaContractAccess.doContractUpdate(txlife, getWork(), getUser()); //NBA213
			}
			getLogger().logDebug("RoutePoller, ResetCSMQ - cmQueue="+cmQueue+",userId="+userId);
		}
		return txlife;
	}
	// new method APSL4918
	private void updateUWAnd1203(NbaTXLife txlife, NbaDst dst) throws NbaBaseException {
		boolean updateQueue = false;
		String oldUndQueue = dst.getNbaLob().getUndwrtQueue();
		getLogger().logDebug("RoutePoller - updateUWAnd1203 - old UndQueue :: "+oldUndQueue);
		if(!NbaUtils.isEmpty(oldUndQueue) && oldUndQueue.equalsIgnoreCase(dst.getNbaLob().getQueue())){
			updateQueue = true;
		}
		getLogger().logDebug("RoutePoller - Queue update Required :: "+updateQueue);
		AxaUWAssignmentEngineVO uwAssignment = new AxaUWAssignmentEngineVO();
		uwAssignment.setTxLife(txlife);
		uwAssignment.setNbaDst(getWork());
		uwAssignment.setReassignment(true);
		uwAssignment.setCasemanagerRequired(false);
		new AxaUnderwriterAssignmentEngine().execute(uwAssignment);
		//Start APSL5122
		updateTermExpressInd(txlife);
		//End APSL5122
		String undQueue = dst.getNbaLob().getUndwrtQueue();
		getLogger().logDebug("RoutePoller - New UW Queue from assignment Rules :: "+undQueue);
		ApplicationInfo appinfo = txlife.getPrimaryHolding().getPolicy().getApplicationInfo();
		if (null != undQueue && undQueue.length() > 0 && !undQueue.equalsIgnoreCase(oldUndQueue)) {
			getLogger().logDebug("RoutePoller - New and Old UW Queue are diff starting DB and AWD update ");
			String userId = getUserIdForQueue(undQueue);
			if(userId != null && userId.length() > 0){
				appinfo.setHOUnderwriterName(userId);
				appinfo.setActionUpdate();
				txlife.setAccessIntent(NbaConstants.UPDATE);
				txlife = NbaContractAccess.doContractUpdate(txlife, getWork(), getUser());
				if(updateQueue){
					setStatusOnly(dst, NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT);
				// Update the Work Item with it's new status and update the work item in AWD
				doUpdateWorkItem();
				}else{
					// Update the Work Item LOB in AWD
					updateWorkItem();
				}
			}
			getLogger().logDebug("RoutePoller, reassign - UnderWriter = "+undQueue+",userId = "+userId);
			create1203DataFeedRequest(txlife);
			invokeAXAWebService(getUser());
		}else{
			getLogger().logDebug("RoutePoller - New and Old UW Queue are same Skip DB and AWD update ");
		}
	}

	// new method APSL4918
	/**
	 * Update the work item in AWD. Unlock any locked children.
	 */
	public void updateWorkItem() throws NbaBaseException {
		getLogger().logDebug("Updating LOB for Work item ");
		getWork().getCase().setUpdate("Y");
		getWork().setNbaUserVO(getUser());
		AccelResult accelResult = new AccelResult();
		accelResult.merge(currentBP.callBusinessService("NbaUpdateWorkBP", getWork()));
		NewBusinessAccelBP.processResult(accelResult);
		setWork((NbaDst) accelResult.getFirst());
		String origWorkItemId = null;
		String lockedUser = null;
		if (getOrigWorkItem() != null) {
			origWorkItemId = getOrigWorkItem().getID();
			lockedUser = setIdForUnlock(origWorkItemId, null);
		}
		if (lockedUser != null) {
			setIdForUnlock(origWorkItemId, lockedUser);
		}
	}
	/**
	 *
	 * Use the <code>NbaProcessStatusProvider</code> to determine the work item's next status. If a new status is returned back from the VP/MS
	 * model, then only update the transaction's status.
	 *
	 * @param work
	 * @throws NbaBaseException
	 */
	// new method APSL4918
	protected void setStatusOnly(NbaDst dst, String userID) throws NbaBaseException {
		NbaUserVO user = new NbaUserVO(userID, "");
		statusProvider = new NbaProcessStatusProvider(user, dst.getNbaLob());
		getLogger().logDebug("RoutePoller - Status from VPMS :: "+statusProvider.getPassStatus());
		if (statusProvider != null && statusProvider.getPassStatus() != null && !statusProvider.getPassStatus().equals(dst.getStatus())) {
			dst.setStatus(statusProvider.getPassStatus());
			NbaUtils.setRouteReason(dst,statusProvider.getPassStatus());
		}
	}

	/**
	 *
	 * Use the <code>NbaProcessStatusProvider</code> to determine the work item's next status. If a new status is returned back from the VP/MS
	 * model, then only update the transaction's status.
	 *
	 * @param work
	 * @throws NbaBaseException
	 */
	protected void setStatus(NbaTransaction transaction, String userID) throws NbaBaseException {

		NbaUserVO user = new NbaUserVO(userID, "");
		statusProvider = new NbaProcessStatusProvider(user, transaction.getNbaLob());
		if (statusProvider != null && statusProvider.getPassStatus() != null && !statusProvider.getPassStatus().equals(transaction.getStatus())) {
			transaction.setStatus(statusProvider.getPassStatus());
			NbaUtils.setRouteReason(transaction,statusProvider.getPassStatus());
		}
	}

	/**
	 * Gets value of fast indicator and brokerage by calling VPMS, and sets the values to class variables.
	 *
	 * @param NbaTXLife
	 * @param nbaLob
	 * @return void
	 * @throws NbaBaseException
	 */
	protected String getRCMStatusFromVPMS(NbaTXLife nbaTXLife, NbaLob nbaLob, HashMap deoinkMap) throws NbaBaseException {
		NbaVpmsAdaptor adapter = null;
		NbaOinkDataAccess oinkData;
		String rcmTeam = null;
		try {
			oinkData = new NbaOinkDataAccess(nbaLob);
			oinkData.setContractSource(nbaTXLife);
			adapter = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.AUTO_PROCESS_STATUS);
			adapter.setSkipAttributesMap(deoinkMap);
			adapter.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RCM_TEAM_ROUTE_STATUS);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(adapter.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				List values = vpmsResultsData.getResultsData();
				if (values.size() > 0) {
					rcmTeam = (String) values.get(0);
				}
				getLogger().logDebug("RoutePoller - RCM from VPMS :: "+rcmTeam);
			}
			return rcmTeam;
		} catch (Exception e) {
			getLogger().logDebug(this.getClass().getName() + "VPMS ERROR" + e.getMessage());
			throw new NbaBaseException(this.getClass().getName() + "VPMS ERROR" + e.getMessage());
		} finally {
			try {
				if (adapter != null) {
					adapter.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}

	}
	protected NbaTableAccessor getNbaTableAccessor() {
		if (nbaTableAccessor == null) {
			nbaTableAccessor = new NbaTableAccessor();
		}
		return nbaTableAccessor;
	}



	protected String getUserIdForQueue(String queue) {
			String userID = null;
			try {
				userID = getNbaTableAccessor().getUserIdForQueue(queue);
			} catch (NbaDataAccessException ndae) {
				getLogger().logError(ndae);
			}
			return userID;
		}


	private void update1203() {

		request1203DataFeed.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS);
		Policy policy = request1203DataFeed.getPolicy();
		if (!policy.hasLineOfBusiness()) {
			policy.setLineOfBusiness(1);
		}
	}

	private NbaTXLife invokeAXAWebService(NbaUserVO user) {
		NbaTXLife nbaTXLifeResponse = null;
		RequirementInfo reqInfo = null;
		RequirementInfoExtension reqInfoExt = null;
		try {
			update1203();
			int reqCount = request1203DataFeed.getPolicy().getRequirementInfoCount();
			for (int x = 0; x < reqCount; x++) {
				reqInfo = request1203DataFeed.getPolicy().getRequirementInfoAt(x);
				reqInfo.assureAttachmentRetrieved();
				reqInfo.setAttachment(new ArrayList());
				reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
				if (!NbaUtils.isBlankOrNull(reqInfo.getFormNo()) && reqInfoExt != null) {
					reqInfoExt.setFormNoDescription(NbaUtils.getFormTranslation(reqInfo.getFormNo(), request1203DataFeed));
				}
			}

			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("The 1203 Data Feed Transmission Request Sent.");
			}
			nbaTXLifeResponse = (new AxaInvokeWebservice()).invokeAXAOnlineDistrWS( request1203DataFeed, user);
			if (getLogger().isDebugEnabled() && null != nbaTXLifeResponse) {
				getLogger().logDebug("The 1203 Data Feed Transmission Response received.");
			}

		} catch (Throwable e) {
			NbaLogFactory.getLogger(this.getClass()).logException(e);
		}
		return nbaTXLifeResponse;
	}



	protected void doProcess() throws NbaBaseException {
		Map maps = readInputFile();
		ListIterator results = null;
		NbaDst aWorkItem = null;
		NbaSearchVO searchVO = null;
		Iterator it = maps.entrySet().iterator();
		NbaTXLife txLife = null;
		while (it.hasNext()) {
			//ALII2089 start
			Map.Entry pairs = (Map.Entry) it.next();
			writeToLogFile("RoutePoller doprocess()- pairs.getKey()="+pairs.getKey()+",pairs.getValue()="+pairs.getValue());
			if(pairs.getValue()!=null && pairs.getValue().toString().trim().equalsIgnoreCase("CSMQ")){
				try {
					searchVO = searchContract(pairs.getKey().toString().trim());
				} catch (NbaBaseException baseException) {
					baseException.printStackTrace();
				}
				if (searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
					results = searchVO.getSearchResults().listIterator();
					while (results.hasNext()) {
						try {
							aWorkItem = retrieveWorkItemCSMQ((NbaSearchResultVO) results.next());
							setWork(aWorkItem);
							txLife = doHoldingInquiry();
							txLife = resetCMCOntactName(txLife, aWorkItem);
							create1203DataFeedRequest(txLife);
							invokeAXAWebService(getUser());
						} catch (NbaBaseException baseException) {
							getLogger().logDebug("RoutePoller in Main Catch - pairs.getKey()="+pairs.getKey()+",pairs.getValue()="+pairs.getValue());
							baseException.printStackTrace();
						}
					}
					try {
						unlockWorkitemAndSessionCleanUP(aWorkItem);
					} catch (NbaBaseException e) {
						getLogger().logDebug("RoutePoller in Unlock Catch - pairs.getKey()="+pairs.getKey()+",pairs.getValue()="+pairs.getValue());
						e.printStackTrace();
					}
				}
			}else if(pairs.getValue()!=null && pairs.getValue().toString().trim().equalsIgnoreCase("UNDQ")){ //APSL4918
				reassignUnderWriter(pairs); //APSL4918
			}else if(pairs.getValue()!=null && pairs.getValue().toString().trim().equalsIgnoreCase("RCMR")){ //APSL4918
				reassignRetailRCM(pairs); //APSL4983
			}else if(pairs.getValue()!=null && pairs.getValue().toString().trim().equalsIgnoreCase("RQTP")){ //APSL5055 
				migrateLobForRQTP(); 
			}else if(pairs.getValue()!=null && pairs.getValue().toString().trim().equalsIgnoreCase("NBACASH")){ 
				migrateLobForNbaCash(); //APSL5055
			}
			else if(pairs.getValue()!=null && pairs.getValue().toString().trim().equalsIgnoreCase(moveToEndUtility)){ //APSL4918
				moveToEnd(pairs); //APSL5415
			}
			else if(!forfetchImageUtility.equals(pairs.getValue())){ // old routing process
				//perform Route function
				performRoute(pairs);
			} else {
				//perform Source Image pull function
				pullSourceImage(pairs);//ALII2089
			}
			it.remove();
		}

	}

	/**
	 * APSL5055 NEW METHOD
	 * migrate RQTP LOB from AWD DB to NbaAux
	 * @param pair
	 */
	public void migrateLobForRQTP() {
		writeToLogFile("RoutePoller - Start migration for RQTP LOB");
		Nba331Conversion.getInstance().convertRequirementType();			
	}

	/**
	 * APSL5055 NEW METHOD
	 * migrate NbaCashering LOBs from AWD DB to NbaCash
	 * @param pair
	 */
	public void migrateLobForNbaCash(){
		writeToLogFile("RoutePoller - Start migration for CASH LOB");
		Nba331Conversion.getInstance().convertCheckAllocation();
	}
	
	
	/**
	 * APSL4983 NEW METHOD
	 * reassign Retail RCM based on existing configured rules
	 * @param pair
	 */
	public void reassignRetailRCM(Map.Entry pairs){
		getLogger().logDebug("RoutePoller - Start reassignRetailRCM");
		NbaSearchVO searchVO = null;
		ListIterator results = null;
		NbaDst aWorkItem = null;
		NbaTXLife txLife = null;
		try {
			searchVO = searchContract(pairs.getKey().toString().trim());
		} catch (NbaBaseException baseException) {
			baseException.printStackTrace();
		}
		if (searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
			results = searchVO.getSearchResults().listIterator();
			while (results.hasNext()) {
				try {
					aWorkItem = retrieveWorkItem((NbaSearchResultVO) results.next());
					setWork(aWorkItem);
					txLife = doHoldingInquiry();
					updateRCMAnd1203(txLife, aWorkItem);
				} catch (NbaBaseException baseException) {
					getLogger().logDebug("RoutePoller in Main Catch - pairs.getKey()="+pairs.getKey()+",pairs.getValue()="+pairs.getValue());
					baseException.printStackTrace();
				}
			}
			try {
				unlockWorkitemAndSessionCleanUP(aWorkItem);
			} catch (NbaBaseException e) {
				getLogger().logDebug("RoutePoller in Unlock Catch - pairs.getKey()="+pairs.getKey()+",pairs.getValue()="+pairs.getValue());
				e.printStackTrace();
			}
		}
	}

	/**
	 * update RCM and trigger 1203 Request
	 * @param
	 */
	//APSL4983 NEW METHOD
	private void updateRCMAnd1203(NbaTXLife txlife, NbaDst dst) throws NbaBaseException {
		long distChannel = dst.getNbaLob().getDistChannel();
		getLogger().logDebug("RoutePoller - updateRCMAnd1203 - Dist Channel :: "+distChannel);
		if(NbaOliConstants.OLI_DISTCHNNL_CAPTIVE == distChannel){
			String oldRCMQueue = dst.getNbaLob().getCaseManagerQueue();
			getLogger().logDebug("RoutePoller - updateRCMAnd1203 - old RCM LOB :: "+oldRCMQueue);
			AxaUWAssignmentEngineVO uwAssignment = new AxaUWAssignmentEngineVO();
			uwAssignment.setTxLife(txlife);
			uwAssignment.setNbaDst(getWork());
			uwAssignment.setReassignment(true);
			uwAssignment.setUnderwriterRequired(false);
			new AxaUnderwriterAssignmentEngine().execute(uwAssignment);
			//Start APSL5122
			updateTermExpressInd(txlife);
			//End APSL5122
			String newRCMQueue = dst.getNbaLob().getCaseManagerQueue();//"N2UCMR01";
			getLogger().logDebug("RoutePoller - New RCM Queue from assignment Rules :: "+newRCMQueue);
			ApplicationInfo appinfo = txlife.getPrimaryHolding().getPolicy().getApplicationInfo();
			if (null != newRCMQueue && newRCMQueue.length() > 0 && !newRCMQueue.equalsIgnoreCase(oldRCMQueue)) {
				getLogger().logDebug("RoutePoller - New and Old RCM Queue are diff starting DB and AWD update ");
				String userId = getUserIdForQueue(newRCMQueue);
				if(userId != null && userId.length() > 0){
					appinfo.setNBContactName(userId);
					appinfo.setActionUpdate();
					txlife.setAccessIntent(NbaConstants.UPDATE);
					txlife = NbaContractAccess.doContractUpdate(txlife, getWork(), getUser());
					NbaDst newcase = assignRCMQueues(txlife, dst, newRCMQueue, oldRCMQueue);
					updateRCMWorkItem(newcase);
				}
				getLogger().logDebug("RoutePoller, reassign - RCM = "+newRCMQueue+",userId = "+userId);
				create1203DataFeedRequest(txlife);
				invokeAXAWebService(getUser());
			}else{
				getLogger().logDebug("RoutePoller - New and Old RCM Queue are same Skip DB and AWD update ");
			}
		}else{
			getLogger().logDebug("RoutePoller - updateRCMAnd1203 - Dist Channel is not Retail Skip  Skip DB and AWD update  :: ");
		}
	}

	/**
	 * Update the work item in AWD. Unlock any locked children
	 * @param
	 */
	//APSL4983 NEW METHOD
	public void updateRCMWorkItem(NbaDst rcmCase) throws NbaBaseException {
		getLogger().logDebug("Updating LOB for Work item ");
		rcmCase.getCase().setUpdate("Y");
		rcmCase.setNbaUserVO(getUser());
		AccelResult accelResult = new AccelResult();
		accelResult.merge(currentBP.callBusinessService("NbaUpdateWorkBP", rcmCase));
		NewBusinessAccelBP.processResult(accelResult);
		setWork((NbaDst) accelResult.getFirst());
		String origWorkItemId = null;
		String lockedUser = null;
		if (getOrigWorkItem() != null) {
			origWorkItemId = getOrigWorkItem().getID();
			lockedUser = setIdForUnlock(origWorkItemId, null);
		}
		if (lockedUser != null) {
			setIdForUnlock(origWorkItemId, lockedUser);
		}
	}

	/**
	 * Assign RCM Queue and Update CSMQ if required on the work item in AWD.
	 * move all the transactions attached with the case to new Queue, which are in current RCM's Teams or RCM's Queue.
	 * The new queue is determined on the basis of the work type and the current RCM (CSMQ LOB) queue and is resolved
	 * from the AUTOPROCESSSTATUS VP/MS model, If for any workitem the new queue is not defined for this business function.
	 * It will remain in its queue.
	 * @param
	 */
	//APSL4983 New Method
	protected NbaDst assignRCMQueues(NbaTXLife txlife, NbaDst parentCase, String newRCM, String oldRCM) throws NbaBaseException {
		NbaLob lob = parentCase.getNbaLob();
		List rcmTeamList = NbaUtils.getRCMTeamList();
		if (parentCase.isCase()) {
			parentCase = retrieveAllChildren(parentCase);
		}
		String producerid = getProducerID(txlife);
		NbaLob transactionLob = null;
		List transactions = parentCase.getNbaTransactions();
		parentCase.getNbaLob().setCaseManagerQueue(newRCM);
		for (int i = 0; i < transactions.size(); i++) {
			boolean transUpdateRequired = false;
			NbaTransaction nbaTransaction = (NbaTransaction) transactions.get(i);
			transactionLob= nbaTransaction.getNbaLob();
			if(!NbaUtils.isEmpty(transactionLob.getCaseManagerQueue())){
				transactionLob.setCaseManagerQueue(newRCM);
				transUpdateRequired = true;
				getLogger().logDebug("RoutePoller - Updating CSMQ LOB for Work Type :: "+nbaTransaction.getWorkType());
			}
			String queue = nbaTransaction.getQueue();
			if(queue!= null && queue.length() > 6 ){
				String oldRCMTeam = nbaTransaction.getQueue().substring(2,6);
				HashMap deoinkMap = new HashMap();
				if(NbaUtils.isEarcAgent(producerid)){
					if(oldRCM.equalsIgnoreCase(nbaTransaction.getQueue())){
						deoinkMap.put(NbaVpmsConstants.A_PROCESS_ID, NbaConstants.PROC_VIEW_CASEMANAGER_QUEUE_REASSIGNMENT);
						deoinkMap.put("A_WORKTYPELOB",nbaTransaction.getWorkType());
						String status = getRCMStatusFromVPMS(txlife,lob,deoinkMap);
						nbaTransaction.setStatus(status);
						NbaUtils.setRouteReason(nbaTransaction,status);
						transUpdateRequired = true;
						getLogger().logDebug("RoutePoller - Updating Queue for Work Type :: "+nbaTransaction.getWorkType());
					}
				}else if (isWIInRCMTeamQueue(rcmTeamList,oldRCMTeam)) {
					deoinkMap.put(NbaVpmsConstants.A_RCMTEAM, NbaUtils.getRCMTeam(NbaUtils.getAsuCodeForRetail(txlife), NbaUtils.getEPGInd(txlife))); //APSL4412
					deoinkMap.put("A_WORKTYPELOB",nbaTransaction.getWorkType());
					String rcmTeamStatus = getRCMStatusFromVPMS(txlife,lob,deoinkMap);
					nbaTransaction.setStatus(rcmTeamStatus);
					NbaUtils.setRouteReason(nbaTransaction,rcmTeamStatus);
					transUpdateRequired = true;
					getLogger().logDebug("RoutePoller - Updating Queue for Work Type :: "+nbaTransaction.getWorkType());
				}else if(oldRCM.equalsIgnoreCase(nbaTransaction.getQueue())){
					if(nbaTransaction.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_MISC_WORK)){
						deoinkMap.put(NbaVpmsConstants.A_PROCESS_ID, NbaConstants.PROC_RCM_WORK_CREATE);
						deoinkMap.put(NbaVpmsConstants.A_RCMQueueRequired, "true");
					}else{
						deoinkMap.put(NbaVpmsConstants.A_PROCESS_ID, NbaConstants.PROC_VIEW_CASEMANAGER_QUEUE_REASSIGNMENT);
					}
					deoinkMap.put("A_WORKTYPELOB",nbaTransaction.getWorkType());
					String status = getRCMStatusFromVPMS(txlife,lob,deoinkMap);
					nbaTransaction.setStatus(status);
					NbaUtils.setRouteReason(nbaTransaction,status);
					transUpdateRequired = true;
					getLogger().logDebug("RoutePoller - Updating Queue for Work Type :: "+nbaTransaction.getWorkType());
				}
			}
			if(transUpdateRequired){
				nbaTransaction.setUpdate();
			}
		}
		setWork(parentCase);
		return parentCase;
	}

	//APSL4983 New Method
	protected boolean isWIInRCMTeamQueue(List rcmTeamList, String oldRCMTeam){
		boolean present = false;
		for(int i=0; i<rcmTeamList.size(); i++){
			if(((String) rcmTeamList.get(i)).equalsIgnoreCase(oldRCMTeam)){
				present = true;
			}
		}
		return present;
	}

	//APSL4983 New Method
	protected String getProducerID(NbaTXLife txlife){
		String producerid = null;
		NbaParty party = null;
		Relation relation = txlife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
		party = txlife.getParty(relation.getRelatedObjectID());
		if (party != null) {
			CarrierAppointment carrierAppointment = party.getParty().getProducer().getCarrierAppointmentAt(0);
			if (carrierAppointment != null) {
				producerid = carrierAppointment.getCompanyProducerID();
			}
		}
		return producerid;

	}

	//APSL4983 New Method
	protected NbaDst retrieveAllChildren(NbaDst parentCase)  throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(parentCase.getID(), true);
		retOpt.requestTransactionAsChild();
		retOpt.setLockTransaction();
		retOpt.setLockParentCase();
		try {
			parentCase = retrieveWorkItem(getUser(), retOpt);
		} catch (NbaNetServerException e) {
			//throw new NbaBaseException("Unable to retrieve", e);
			getLogger().logDebug("Unable to retrieve ::" + e);
		}
		return parentCase;

	}

	// new method APSL4918
	public void reassignUnderWriter(Map.Entry pairs){
		getLogger().logDebug("RoutePoller - Start reassignUnderWriter");
		NbaSearchVO searchVO = null;
		ListIterator results = null;
		NbaDst aWorkItem = null;
		NbaTXLife txLife = null;
		try {
			searchVO = searchContract(pairs.getKey().toString().trim());
		} catch (NbaBaseException baseException) {
			baseException.printStackTrace();
		}
		if (searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
			results = searchVO.getSearchResults().listIterator();
			while (results.hasNext()) {
				try {
					aWorkItem = retrieveWorkItem((NbaSearchResultVO) results.next());
					setWork(aWorkItem);
					txLife = doHoldingInquiry();
					updateUWAnd1203(txLife, aWorkItem);
				} catch (NbaBaseException baseException) {
					getLogger().logDebug("RoutePoller in Main Catch - pairs.getKey()="+pairs.getKey()+",pairs.getValue()="+pairs.getValue());
					baseException.printStackTrace();
				}
			}
			try {
				unlockWorkitemAndSessionCleanUP(aWorkItem);
			} catch (NbaBaseException e) {
				getLogger().logDebug("RoutePoller in Unlock Catch - pairs.getKey()="+pairs.getKey()+",pairs.getValue()="+pairs.getValue());
				e.printStackTrace();
			}
		}
	}

	public void unlockWorkitemAndSessionCleanUP(NbaDst lockedWork) throws NbaBaseException {
		lockedWork.setNbaUserVO(getUser());
		unlockWork(getUser(), lockedWork);
		NbaContractLock.removeLock(lockedWork, getUser());
	}


	protected void performRoute(Entry pairs) throws NbaBaseException {
		writeToLogFile("Current CRDA " + pairs.getKey() + "    " + "Status to route   " + pairs.getValue());
		NbaDst aWorkItem = null;
		try {
			aWorkItem = searchWorkItemByCRDA(new String(pairs.getKey().toString().trim()));
		} catch (Exception ex) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			return;
		}

		if (aWorkItem != null) {
			initialize(user, aWorkItem);
			aWorkItem.setStatus(pairs.getValue().toString().trim()); // Setting new status to route
			update(aWorkItem);
			writeToLogFile( "Queue after routing  " + aWorkItem.getQueue());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
		}else {
			writeToLogFile("Work item not found for : " + pairs.getKey());
		}
	}

	//APSL5415 New Method
	protected void moveToEnd(Entry pairs) throws NbaBaseException {
		writeToLogFile("Current CRDA " + pairs.getKey() + "    " + "Status to route   " + pairs.getValue());
		NbaSearchVO searchVO = prepareSearchVOForRoute(pairs);
		NbaDst aWorkItem = null;
		try {
			if (searchVO.getSearchResults().size() > 0) {
				for (int i = 0; i < searchVO.getSearchResults().size(); i++) {
					NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
					retOpt.setNbaUserVO(getUser());
					NbaLookupResultVO resultVO = null;
					resultVO = (NbaSearchResultVO) searchVO.getSearchResults().get(i);
					retOpt.setWorkItem(((NbaSearchResultVO) resultVO).getWorkItemID(), false);
					retOpt.setLockWorkItem();
					aWorkItem = retrieveWorkItem(user, retOpt);
					writeToLogFile("Workitem " + retOpt.getWorkItemId());
					if (aWorkItem != null) {
						initialize(user, aWorkItem);
						aWorkItem.setStatus("ENDED"); // Setting new status to route to End
						update(aWorkItem);
						writeToLogFile("Workitem has been routed to Queue  " + aWorkItem.getQueue());
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
					} else {
						writeToLogFile("Work items not found ");
					}
				}
			}
		} catch (Exception ex) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Exception while routing work Items " + ex.getMessage(), ""));
			throw new NbaBaseException(ex.getMessage(), ex);
		}
	}



	/**
	 * ALII2089 NEW METHOD
	 * Fetch source for mentioned policiy number and other criteria
	 * @param pair
	 * @throws NbaBaseException
	 */
	protected void pullSourceImage(Entry pair) throws NbaBaseException {
		NbaSearchVO searchVO = prepareSearchVO(pair);
		String policyNumber = searchVO.getNbaLob().getPolicyNumber();
		writeToLogFile("Pulling Source Image of policyNumber "+policyNumber);
		String fileLocation = NbaConfiguration.getInstance().getFileLocation("ReqImageLocation");
		try {
	        if (searchVO.getSearchResults().size() > 0) {
	            for (int i = 0; i < searchVO.getSearchResults().size(); i++) {
	            	NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
	            	NbaLookupResultVO resultVO = null;
	            	NbaSource nbaSource = null;
	            	if(A_ST_APPLICATION.equals(searchVO.getWorkType())){
	            		resultVO = (NbaSearchResultVO)searchVO.getSearchResults().get(i);
	            		retOpt.setWorkItem(((NbaSearchResultVO)resultVO).getWorkItemID(), false);
	            	}else{
	            		resultVO = (NbaTransactionSearchResultVO) searchVO.getSearchResults().get(i);
	            		retOpt.setWorkItem(((NbaTransactionSearchResultVO)resultVO).getTransactionID(), false);
	            	}
	        		retOpt.requestSources();
	        		//retrieve Requirement workitem along with all its Sources
	        		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
	        		String crda = aWorkItem.getNbaLob().getCreateDate();
	        		List sources = aWorkItem.getNbaSources();
	        		for (int j = 0; j < sources.size();j++){
	        			nbaSource = (NbaSource) sources.get(j);
	        			if (nbaSource != null) {
	        				//look through all Sources and pull list of Base64 images
	        				crda = crda.replaceAll("[-.]", "");
	        				List base64Images = getBase64SourceImage(getUser(), nbaSource);
	        			    if(base64Images != null && base64Images.size() > 0) {
	        			    	for (int k = 0; k < base64Images.size(); k++) {
	        			    		String decodedStream = (String)base64Images.get(k);
	        			    		byte[] imageBytes= NbaBase64.decode(decodedStream);
	        			    		OutputStream out = new FileOutputStream(fileLocation+File.separator+policyNumber+"_"+crda+"Image_"+k+".tiff");
	        			    		out.write(imageBytes);
	        			    		out.close();
	        			    	}
	        					writeToLogFile("Image saved for Policy Number: " +policyNumber);
	        					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
	        			    }else{
	        			    	writeToLogFile("Image does not exist for Policy Number: " +policyNumber);
	        			    }
	        			}
	        		}
	        	}
	        }else{
	        	writeToLogFile("No match found for policyNumber = "+policyNumber);
	        }
		} catch (Exception ex) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Exception while pulling source image "+ex.getMessage(), ""));
			throw new NbaBaseException(ex.getMessage(),ex);
		}
	}
	/**
	 * ALII2089 NEW METHOD
	 * Prepare search result based on mentioned criteria
	 * @param pair
	 * @return
	 * @throws NbaBaseException
	 */
	protected NbaSearchVO prepareSearchVO(Entry pair) throws NbaBaseException {
		String policyNumber = String.valueOf(pair.getKey());
		NbaSearchVO searchVO = new NbaSearchVO();
		if(A_ST_APPLICATION.equalsIgnoreCase(searchWorkType)){
			searchVO.setResultClassName(CASE_SEARCH_VO);
		}else{
			searchVO.setResultClassName(TRANSACTION_SEARCH_VO);
		}
		searchVO.setWorkType(searchWorkType);
		NbaLob searchLob = new NbaLob();
		if(!A_ST_APPLICATION.equalsIgnoreCase(searchWorkType)){
			searchLob.setReqType(searchrequirementType);
		}
		searchLob.setPolicyNumber(policyNumber.trim());
		searchVO.setNbaLob(searchLob);
		writeToLogFile("Searching for policyNumber "+policyNumber);
		searchVO = lookupWork(getUser(),searchVO);
		return searchVO;
	}

	//APSL5415 New Method Route to End Utility
	protected NbaSearchVO prepareSearchVOForRoute(Entry pair) throws NbaBaseException {

		String toDate = String.valueOf(pair.getKey());
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setToDateTimeStamp(toDate);
		searchVO.setWorkType(A_ST_APPLICATION);
		writeToLogFile("Searching for all the work Items to"+toDate);
		searchVO = lookupWork(getUser(),searchVO);
		return searchVO;
	}


	public boolean initialize(NbaUserVO nbaUserVO, NbaDst nbaDst) throws NbaBaseException {
		setUser(nbaUserVO);
		setWork(nbaDst);

		AutomatedProcess automatedProcess = null;
		String userID;
		if (nbaUserVO != null) {
			userID = nbaUserVO.getUserID();
			automatedProcess = NbaConfiguration.getInstance().getAutomatedProcessConfigEntry("A2ROUTWI");
			if (automatedProcess == null) {
				// Necessary configuration could not be found so raise exception
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				writeToLogFile(userID + "Not Found");
				throw new NbaConfigurationException(userID + "Not Found");
			}
			return true;
		}
		return false;
	}

	public NbaDst searchWorkItemByCRDA(String crda) throws NbaBaseException {
		NbaDst aWorkItem = null;
		if (crda != null) {
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(crda, false);
			retOpt.setLockWorkItem();
			aWorkItem = retrieveWorkItem(user, retOpt);
		}
		return aWorkItem;
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

	public Map readInputFile() throws NbaBaseException {
		boolean documentFound = false;
		String path = null;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		Map maps = new HashMap();
		try {

			path = NbaConfiguration.getInstance().getFileLocation("RouteUtilityRip");
			if (path == null || path.length() == 0) {
				throw new NbaBaseException("Path not Found ", NbaExceptionType.FATAL);
			}
			getLogger().logDebug(path + " path 1");
			File folder = new File(path);
			getLogger().logDebug(folder + " folder 1");
			if (folder == null) {
				throw new NbaBaseException("RouteUtility Folder not Found at " + path, NbaExceptionType.FATAL);
			}
			File[] listOfFiles = folder.listFiles();
			getLogger().logDebug(listOfFiles + " listOfFiles");
			if (!NbaUtils.isBlankOrNull(listOfFiles)) {
				for (int index = 0; index < listOfFiles.length; index++) {
					getLogger().logDebug(listOfFiles[index] + " listOfFiles.index");
					if (listOfFiles[index].isFile()) {
						writeToLogFile(listOfFiles[index].getName() + " Reading file.");
						br = new BufferedReader(new FileReader(listOfFiles[index].getPath()));
						while ((line = br.readLine()) != null) {

							// use comma as separator
							if (line.equalsIgnoreCase("")) {
								continue;
							}
							String[] entrySet = line.split(cvsSplitBy);
							maps.put(entrySet[0], entrySet[1]);
							writeToLogFile(entrySet[0] + "  " + entrySet[1] + "  record found.");
							documentFound = true;
						}

					}
				}
			}
			// ALII2089 Starts
			getLogger().logDebug(documentFound + "  documentFound");
			path = NbaConfiguration.getInstance().getFileLocation("FetchImageUtility");
			if (path == null || path.length() == 0 && !documentFound) {
				throw new NbaBaseException("Path not Found ", NbaExceptionType.FATAL);
			}
			folder = new File(path);
			if (folder == null && !documentFound) {
				throw new NbaBaseException("FetchImageUtility Folder not Found at " + path, NbaExceptionType.FATAL);
			}
			listOfFiles = folder.listFiles();
			if (!NbaUtils.isBlankOrNull(listOfFiles)) {
				for (int index = 0; index < listOfFiles.length; index++) {
					if (listOfFiles[index].isFile()) {
						writeToLogFile(listOfFiles[index].getName() + " Reading FetchImageUtility file.");
						br = new BufferedReader(new FileReader(listOfFiles[index].getPath()));
						while ((line = br.readLine()) != null) {

							// use comma as separator
							if (line.equalsIgnoreCase("")) {
								continue;
							}
							String[] entrySet = line.split(cvsSplitBy);
							if (entrySet.length == 2 && (WORKTYPE.equalsIgnoreCase(entrySet[0]) || REQUIREMENTTYPE.equalsIgnoreCase(entrySet[0]))) {
								if (WORKTYPE.equalsIgnoreCase(entrySet[0])) {
									searchWorkType = entrySet[1].toUpperCase();
								} else if (REQUIREMENTTYPE.equalsIgnoreCase(entrySet[0])) {
									searchrequirementType = Integer.parseInt(entrySet[1]);
								}
							} else {
								for (int i = 0; i < entrySet.length; i++) {
									maps.put(entrySet[i], forfetchImageUtility);
								}
							}
							documentFound = true;
						}

					}
				}
			}
			getLogger().logDebug(maps.toString() + " MAPS String");
			// ALII2089 Ends

			// APSL5415 Starts
			getLogger().logDebug(documentFound + "  documentFound");

			path = NbaConfiguration.getInstance().getFileLocation("MoveToEndUtility");
			if (path == null || path.length() == 0 && !documentFound) {
				throw new NbaBaseException("Path not Found ", NbaExceptionType.FATAL);
			}
			folder = new File(path);
			if (folder == null && !documentFound) {
				throw new NbaBaseException("MoveToEndUtility Folder not Found at " + path, NbaExceptionType.FATAL);
			}
			listOfFiles = folder.listFiles();
			if (!NbaUtils.isBlankOrNull(listOfFiles)) {
				for (int index = 0; index < listOfFiles.length; index++) {
					if (listOfFiles[index].isFile()) {
						writeToLogFile(listOfFiles[index].getName() + " Reading MoveToEndUtility file.");
						br = new BufferedReader(new FileReader(listOfFiles[index].getPath()));
						while ((line = br.readLine()) != null) {

							// use comma as separator
							if (line.equalsIgnoreCase("")) {
								continue;
							}
							String[] entrySet = line.split(cvsSplitBy);
							maps.put(entrySet[0], entrySet[1]);
							writeToLogFile(entrySet[0] + "  " + entrySet[1] + "  record found.");
							documentFound = true;
						}

					}
				}
			}
			getLogger().logDebug(maps.toString() + " MAPS String");
			// APSL5415 Ends

			if (!documentFound) {
				writeToLogFile("No File to Process.");
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
			}
		} catch (ArrayIndexOutOfBoundsException indexOutoFBound) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			writeToLogFile(" No Matching separator , found");
			throw new NbaBaseException("No Matching separator , found " + this.getClass().getName() + " ", NbaExceptionType.FATAL);

		} catch (Exception exp) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			exp.printStackTrace();
			throw new NbaBaseException(" " + this.getClass().getName() + " ", NbaExceptionType.FATAL);
		} finally {
			if (br != null) {
				try {
					br.close();
					return maps;
				} catch (IOException e) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
					e.printStackTrace();
				}
			}
		}
		return maps;

	}

	public void deleteProcessedDocument() throws NbaBaseException {
		String path = NbaConfiguration.getInstance().getFileLocation("RouteUtilityRip");
		if(path == null || path.length()== 0) {
			throw new NbaBaseException( path + " Path not Found ", NbaExceptionType.FATAL);
		}

		File folder = new File(path);
		if(folder == null ) {
			throw new NbaBaseException("RouteUtility Folder not Found at " + path, NbaExceptionType.FATAL);
		}
		File[] listOfFiles = folder.listFiles();


		for (int index = 0; index < listOfFiles.length; index++) {
			if (listOfFiles[index].isFile()) {
				if(listOfFiles[index].delete()){
					writeToLogFile( listOfFiles[index].getName() + "Deleted Successfully.");
				}
			}
		}

		//ALII2089 Starts
		path = NbaConfiguration.getInstance().getFileLocation("FetchImageUtility");

		if(path == null || path.length()== 0) {
			throw new NbaBaseException( path + " Path not Found ", NbaExceptionType.FATAL);
		}

		folder = new File(path);
		if(folder == null ) {
			throw new NbaBaseException("FetchImageUtility Folder not Found at " + path, NbaExceptionType.FATAL);
		}
		listOfFiles = folder.listFiles();

		if(listOfFiles.length < 1) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
			return;
		}

		for (int index = 0; index < listOfFiles.length; index++) {
			if (listOfFiles[index].isFile()) {
				if(listOfFiles[index].delete()){
					writeToLogFile( listOfFiles[index].getName() + "Deleted Successfully.");
				}
			}
		}
		//ALII2089 Ends

		//APSL5415 Starts
				path = NbaConfiguration.getInstance().getFileLocation("MoveToEndUtility");

				if(path == null || path.length()== 0) {
					throw new NbaBaseException( path + " Path not Found ", NbaExceptionType.FATAL);
				}

				folder = new File(path);
				if(folder == null ) {
					throw new NbaBaseException("MoveToEndUtility Folder not Found at " + path, NbaExceptionType.FATAL);
				}
				listOfFiles = folder.listFiles();

				if(listOfFiles.length < 1) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
					return;
				}

				for (int index = 0; index < listOfFiles.length; index++) {
					if (listOfFiles[index].isFile()) {
						if(listOfFiles[index].delete()){
							writeToLogFile( listOfFiles[index].getName() + "Deleted Successfully.");
						}
					}
				}
				//APSL5415 Ends
	}

	protected void writeToLogFile(String entry) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(entry);
		}
	}
	//APSL5122
	private void updateTermExpressInd(NbaTXLife txlife) {
		if (getWork().getNbaLob().getDisplayIconLob() != null && getWork().getNbaLob().getDisplayIconLob().equals("1")) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(txlife.getPolicy().getApplicationInfo());
			if (appInfoExt != null) {
				appInfoExt.setTermExpressInd(true);
				appInfoExt.setActionUpdate();
			}
		}
	}

	public void changeDistributionChannel(String newValue, String oldValue, String polNumber, NbaUserVO userVo) throws NbaBaseException {
		NbaSearchVO searchWI = new NbaSearchVO();
		Map distchannel = new HashMap();
		distchannel.put("10", "Retail");
		distchannel.put("6", "Wholesale");
		NbaDst parent = new NbaDst();
		searchWI.setContractNumber(polNumber);
		searchWI.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchWI.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchWI = WorkflowServiceHelper.lookupWork(userVo, searchWI);
		NbaSearchResultVO resultVO = null;
		if (searchWI != null && searchWI.getSearchResults().size() > 0) {
			resultVO = (NbaSearchResultVO) searchWI.getSearchResults().get(0);
		}
		LockRetrieveWorkRequest lockRequest = new LockRetrieveWorkRequest();
		NbaLob tempLob = new NbaLob();
		tempLob.setDistChannel(oldValue);
		tempLob.setPolicyNumber(polNumber);
		lockRequest.setLobData((LobData[]) tempLob.getLobs().toArray(new LobData[tempLob.getLobs().size()]));
		lockRequest.setWorkItem(resultVO.getWorkItem());
		lockRequest.setBusinessArea(resultVO.getBusinessArea());
		lockRequest.setRetrieveWorkLocked(true);
		AccelResult resultnew = (AccelResult) currentBP.callBusinessService("LockRetrieveWorkBP", lockRequest);
		if (resultnew.hasErrors()) {
			String errMsg = "Error While Processing Work Item";
			currentBP.callBusinessService("NbaUnlockWorkBP", parent);
			List msg = resultnew.getMessagesList();
			if (msg.size() > 0) {
				Message mss = (Message) msg.get(0);
				Object[] obj = mss.getVariableData();
				if (obj.length > 0) {
					errMsg = obj[0].toString();
				}
				throw new NbaBaseException(errMsg);
			}
			throw new NbaBaseException(errMsg);
		}
		parent.addCase((WorkItem) resultnew.getFirst());
		updateDistChannel(parent, newValue);
		//NBLXA-2511 BEGIN
		if (NbaOliConstants.OLI_DISTCHNNL_BD == Long.valueOf(newValue)) {
			updateSuitability(parent);
		}
		//NBLXA-2511 ENDS
		parent.setNbaUserVO(user);
		String comments= "Distribution Channel changed from: " + distchannel.get(oldValue) + " to " + distchannel.get(newValue);
		addComments(parent,comments);
		resultnew.merge(currentBP.callService("NbaUpdateWorkBP", parent));
		currentBP.callBusinessService("NbaUnlockWorkBP", parent);
		NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(polNumber, JOB_DISTRIBUTION_CHANNEL, newValue, CASE_PROCESSED);

	}


	public void updateDistChannel(NbaDst parent, String distChannel) throws NbaBaseException {
		List list = parent.getTransactions();
		Iterator itr = list.iterator();
		NbaTransaction transaction = null;
		while (itr.hasNext()) {
			transaction = new NbaTransaction((WorkItem) itr.next());
			transaction.getNbaLob().setDistChannel(distChannel);
			transaction.setUpdate();
		}
	}



	public void updateQueue(NbaDst parent, String newcsmq) throws NbaBaseException {
		List list = parent.getTransactions();
		Iterator itr = list.iterator();
		NbaTransaction transaction = null;
		List awdStatus = new ArrayList();
		while (itr.hasNext()) {
			transaction = new NbaTransaction((WorkItem) itr.next());
			awdStatus = getStatusByWorkTypeAndStatus(transaction, newcsmq);
			if (awdStatus.size() > 0) {
				setWorkItemStatus(awdStatus, transaction.getStatus());
				transaction.setStatus(getStatus());
				transaction.getNbaLob().setRouteReason(getRouteReason());
				transaction.setUpdate();
			}
		}
	}

	public void setWorkItemStatus(List<String> status,String oldStatus)
   {
		String oldSubStatus = separateDigitsAndAlphabets(oldStatus);
		String newStatus = "";
		for (String var : status) {
			String string[] = var.split("@#");
			newStatus = string[0];
			setStatus(newStatus);
			setRouteReason(string[1]);
			if (separateDigitsAndAlphabets(newStatus).equalsIgnoreCase(oldSubStatus)) {
				setStatus(newStatus);
				setRouteReason(string[1]);
				break;
			}
		}

	}


	public void updateCaseManager(String origCaseManager, String polNumber, NbaUserVO userVo) throws NbaBaseException {
		setWork(getParentWorkItem(polNumber, userVo));
		NbaTXLife caseTxLife = doHoldingInquiry();
		reRunCaseManagerAssingments(caseTxLife);
		String newCsmq = getWork().getNbaLob().getCaseManagerQueue();
		if (!NbaUtils.isBlankOrNull(newCsmq) && !origCaseManager.equalsIgnoreCase(newCsmq)) {
			updateCaseManagerLob(origCaseManager, newCsmq, polNumber);
			routeWIToNewCaseManager(origCaseManager, newCsmq, polNumber, userVo);
			resetCMCOntactName(caseTxLife, getWork());
		} else {
			NbaDst parentWI = getWork();
			parentWI.setNbaUserVO(user);
			addComments(parentWI, "Automatic Reassingment done.No reassignment identified");
			currentBP.callService("NbaUpdateWorkBP", parentWI);
		}
		NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(polNumber, JOB_AGENT_UPDATE, origCaseManager, CASE_PROCESSED);
	}

	public static String separateDigitsAndAlphabets(String str) {
		String number = "";
		String letter = "";
		for (int i = 0; i < str.length(); i++) {
			char a = str.charAt(i);
			if (Character.isDigit(a)) {
				number = number + a;

			} else {
				letter = letter + a;

			}
		}
		return letter;
	}

	public NbaDst getParentWorkItem(String polNumber, NbaUserVO userVo) throws NbaBaseException {
		NbaSearchVO searchParentWI = new NbaSearchVO();
		searchParentWI.setContractNumber(polNumber);
		searchParentWI.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchParentWI.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchParentWI = WorkflowServiceHelper.lookupWork(userVo, searchParentWI);
		NbaDst aWorkItem = null;
		if (searchParentWI != null && searchParentWI.getSearchResults().size() > 0) {
			aWorkItem = retrieveWorkItem((NbaSearchResultVO) searchParentWI.getSearchResults().get(0));
		}
		return aWorkItem;
	}

	public void reRunCaseManagerAssingments(NbaTXLife caseTxLife) throws NbaDataAccessException, NbaBaseException {
		AxaUWAssignmentEngineVO uwAssignment = new AxaUWAssignmentEngineVO();
		boolean isMLOACompanyPresent = NbaConstants.COMPANY_MLOA.equalsIgnoreCase(caseTxLife.getPolicy().getCarrierCode());
		uwAssignment.setTxLife(caseTxLife);
		String undq=getWork().getNbaLob().getUndwrtQueue();
		getWork().getNbaLob().deleteCaseManagerQueue();
		getWork().getNbaLob().deleteUnderWriterQueue();
		uwAssignment.setNbaDst(getWork());
		uwAssignment.setTermExpIndOff(true);
		uwAssignment.setReassignment(true);
		uwAssignment.setUnderwriterRequired(true);
		uwAssignment.setCasemanagerRequired(true);
		uwAssignment.setMLOACompanyPresent(isMLOACompanyPresent);
		uwAssignment.setUndqAssingmentReq(false);
		new AxaUnderwriterAssignmentEngine().execute(uwAssignment);
		getWork().getNbaLob().setUndwrtQueue(undq);
	}

	public void updateCaseManagerLob(String origCaseManager,String newCsmq, String polNumber) throws NbaBaseException{
		LockRetrieveWorkRequest lockRequest = new LockRetrieveWorkRequest();
		NbaDst parentWI = getWork();
		NbaLob tempLob = new NbaLob();
		tempLob.setCaseManagerQueue(origCaseManager);
		tempLob.setPolicyNumber(polNumber);
		lockRequest.setLobData((LobData[]) tempLob.getLobs().toArray(new LobData[tempLob.getLobs().size()]));
		lockRequest.setWorkItem(getWork().getWorkItem());
		lockRequest.setBusinessArea(getWork().getBusinessArea());
		lockRequest.setRetrieveWorkLocked(true);
		parentWI.getNbaLob().setCaseManagerQueue(newCsmq);
		parentWI.setUpdate();
		AccelResult resultnew = (AccelResult) currentBP.callBusinessService("LockRetrieveWorkBP", lockRequest);
		if (!resultnew.hasErrors()) {
			parentWI.addCase((WorkItem) resultnew.getFirst());
			parentWI.setNbaUserVO(user);
			updateLob(parentWI, newCsmq ,null);
			NbaTableAccessor nta = new NbaTableAccessor();
			String origCaseManager_translation = nta.getTranslationString(NbaTableConstants.WF_CM_REASSIGN,origCaseManager);
			String newCsmq_translation = nta.getTranslationString(NbaTableConstants.WF_CM_REASSIGN,newCsmq);
			origCaseManager_translation=(origCaseManager_translation != null && origCaseManager_translation.length() > 0)?origCaseManager_translation:origCaseManager;
			newCsmq_translation=(newCsmq_translation != null && newCsmq_translation.length() > 0)?newCsmq_translation:newCsmq;
			String comments="Automatic Reassingment done. Case Manager changed from: " + origCaseManager_translation + " to: " + newCsmq_translation;
			addComments(parentWI, comments);
			resultnew.merge(currentBP.callService("NbaUpdateWorkBP", parentWI));
		}
		currentBP.callBusinessService("NbaUnlockWorkBP", parentWI);
	}

	public void updateLob(NbaDst parent, String csmq, String undq) throws NbaBaseException {
		List list = parent.getTransactions();
		Iterator itr = list.iterator();
		NbaTransaction transaction = null;
		while (itr.hasNext()) {
			transaction = new NbaTransaction((WorkItem) itr.next());
			if (csmq != null) {
				transaction.getNbaLob().setCaseManagerQueue(csmq);
			} else if (undq != null) {
				transaction.getNbaLob().setUndwrtQueue(undq);
			}
			transaction.setUpdate();
		}
	}

	public void routeWIToNewCaseManager(String origCaseManager, String newCsmq, String polNumber,NbaUserVO userVo) throws NbaBaseException {
		LockRetrieveWorkRequest lockRequest = new LockRetrieveWorkRequest();
		NbaDst parent = new NbaDst();
		setWork(getParentWorkItem(polNumber, userVo));
		NbaLob lob = new NbaLob();
		lob.setQueue(origCaseManager);
		lob.setPolicyNumber(polNumber);
		lockRequest.setLobData((LobData[]) lob.getLobs().toArray(new LobData[lob.getLobs().size()]));
		lockRequest.setWorkItem(getWork().getWorkItem());
		lockRequest.setQueueID(origCaseManager);
		lockRequest.setBusinessArea(getWork().getBusinessArea());
		lockRequest.setRetrieveWorkLocked(true);
		AccelResult result = (AccelResult) currentBP.callBusinessService("LockRetrieveWorkBP", lockRequest);
		if (!result.hasErrors()) {
			parent.addCase((WorkItem) result.getFirst());
			parent.setNbaUserVO(user);
			updateQueue(parent, newCsmq);
			result.merge(currentBP.callService("NbaUpdateWorkBP", parent));
		}
		currentBP.callBusinessService("NbaUnlockWorkBP", parent);

	}

	public List getStatusByWorkTypeAndStatus(NbaTransaction transaction, String newcsmq) throws NbaDataAccessException {
		HashMap aCase = new HashMap();
		aCase.put(NbaTableAccessConstants.C_AWD_TABLE, NbaTableAccessConstants.WF_STATUSES_BY_WORK_AND_QUEUE);
		aCase.put("DOMAIN_NAME", NbaTableAccessConstants.WF_STATUSES_BY_WORK_AND_QUEUE);
		aCase.put(NbaTableAccessConstants.C_WORK_TYPE, transaction.getWorkType());
		aCase.put(NbaTableAccessConstants.C_BUSINESS_AREA, transaction.getBusinessArea());
		aCase.put(NbaTableAccessConstants.C_DATANAME, newcsmq);
		NbaTableData[] tableData = getNbaTableAccessor().getDisplayData(aCase, NbaTableConstants.AWD_TABLES);
		List<String> status = new ArrayList<String>();
		if (tableData.length > 0) {
			tableData = getNbaTableAccessor().getStatusesTranslations("AWD_STATUS", tableData);
			if (tableData != null) {
				int count = tableData.length;
				for (int i = 0; i < count; i++) {
					status.add(tableData[i].code() + "@#" + tableData[i].text());
				}
			}
		}
		return status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRouteReason() {
		return routeReason;
	}

	public void setRouteReason(String routeReason) {
		this.routeReason = routeReason;
	}

	public AxaReassignDataVO getReassignDataVo() {
		return reassignDataVo;
	}

	public void setReassignDataVo(AxaReassignDataVO reassignDataVo) {
		this.reassignDataVo = reassignDataVo;
	}

	public void addComments(NbaDst parentWI, String comments) {
		NbaProcessingErrorComment comment = new NbaProcessingErrorComment();
		comment.setText(comments);
		comment.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
		comment.setOriginator(user.getUserID());
		comment.setUserNameEntered(user.getUserID());
		comment.setActionAdd();
		parentWI.addManualComment(comment.convertToManualComment());
	}

	/**
	 * NBLXA-1829 New Method reads file containing comma separated policy numbers based on the change type
	 * @param file name that needs to be retrieved and read
	 * @param changeType the value of change type based on which folder will be decided to read file
	 * @return a List having policy numbers
	 */
	public List readInputFile(String fileName, String changeType) throws NbaBaseException {
		boolean documentFound = false;
		String path = null;
		BufferedReader br = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		String line = "";
		String cvsSplitBy = ",";
		String fileLocation = "";
		List policyList = new ArrayList();
		try {
			if (changeType.equalsIgnoreCase(JOB_CV_VALIDATE)) {
				fileLocation = "BulkValidation";
			} else if (changeType.equalsIgnoreCase(JOB_POLICY_NOTIFICATION)) {
				fileLocation = "SendPolicyNotification";
			} else if (changeType.equalsIgnoreCase(JOB_PRIOR_INSURANCE_RERUN)) {
				fileLocation = "PriorInsuranceRefresh";
			}
			path = NbaConfiguration.getInstance().getFileLocation(fileLocation);
			if (path == null || path.length() == 0) {
				throw new NbaBaseException("Path not Found ", NbaExceptionType.FATAL);
			}
			getLogger().logDebug(path + " path 1");
			File folder = new File(path);
			getLogger().logDebug(folder + " folder 1");
			if (folder == null) {
				throw new NbaBaseException(fileLocation +" Folder not Found at " + path, NbaExceptionType.FATAL);
			}
			fw = new FileWriter(path+fileName + ".log", true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			File[] listOfFiles = folder.listFiles();
			getLogger().logDebug(listOfFiles + " listOfFiles");
			for (int index = 0; index < listOfFiles.length; index++) {
				getLogger().logDebug(listOfFiles[index] + " listOfFiles.index");
				if (listOfFiles[index].isFile() && listOfFiles[index].getName().equalsIgnoreCase(fileName+".txt")) {
					writeToLogFile(listOfFiles[index].getName() + " Reading file.");
					br = new BufferedReader(new FileReader(listOfFiles[index].getPath()));
					while ((line = br.readLine()) != null) {
						if (line.equalsIgnoreCase("")) {
							continue;
						}
						StringTokenizer tokenizer = new StringTokenizer(line, cvsSplitBy);
						while (tokenizer.hasMoreTokens()) {
							String policyNumber = tokenizer.nextToken().trim();
							getLogger().logDebug("policyNumber  ::" + policyNumber);
							if (policyNumber.length() < 5 || policyNumber.length() > 15) {
								getLogger().logDebug("Seems invalid policy Number : " + policyNumber);
								out.println("No File to Process.");
								continue;
							}
							policyList.add(policyNumber);
						}
						getLogger().logDebug(policyList.size() + "  record(s) found.");
						documentFound = true;
					}
				}
			}
			if (!documentFound) {
				getLogger().logDebug("No File to Process.");
				out.println("No File to Process.");
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
			}
		} catch (ArrayIndexOutOfBoundsException indexOutoFBound) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			getLogger().logDebug("No Matching separator , found");
			out.println("No Matching separator , found");
			throw new NbaBaseException("No Matching separator , found " + this.getClass().getName() + " ", NbaExceptionType.FATAL);

		} catch (Exception exp) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			exp.printStackTrace();
			throw new NbaBaseException(" " + this.getClass().getName() + " ", NbaExceptionType.FATAL);
		} finally {
			if (br != null) {
				try {
					br.close();
					return policyList;
				} catch (IOException e) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
					e.printStackTrace();
				}
			}
			if (out != null) {
				out.close();
			}
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				e.printStackTrace();
			}
			try {
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				e.printStackTrace();
			}
		}
		return policyList;

	}

	/**
	 * NBLXA-1829 New Method iterates over policy list and validates each policy at a time based on business function provided
	 * @param policyNumbers list of policy numbers on which validation needs to be performed
	 * @param busFunction business function that needs to be executed for CV validation
	 * @param fileName file to write output log file after execution
	 */
	public void validatePolicies(List<String> policyNumbers, String busFunction, String fileName,String updateDst) throws NbaBaseException {
		ListIterator results = null;
		NbaDst aWorkItem = null;
		NbaSearchVO searchVO = null;
		NbaTXLife txLife = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		String path = "";
		try {
			path = NbaConfiguration.getInstance().getFileLocation("BulkValidation");
			if (path == null || path.length() == 0) {
				throw new NbaBaseException("Path not Found ", NbaExceptionType.FATAL);
			}
			fw = new FileWriter(path+fileName + ".log", true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			for (String policyNumber : policyNumbers) {
				try {
					searchVO = searchContract(policyNumber.trim());
				} catch (NbaBaseException baseException) {
					getLogger().logDebug("Not able to lookup work in AWD");
					out.println("Not able to lookup work in AWD");
					baseException.printStackTrace();
				}
				if (searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
					results = searchVO.getSearchResults().listIterator();
					while (results.hasNext()) {
						try {
							aWorkItem = retrieveWorkItem((NbaSearchResultVO) results.next());
							setWork(aWorkItem);
							NbaContractLock.requestLock(aWorkItem, getUser());
							txLife = doHoldingInquiry();
							txLife.setBusinessProcess(busFunction);
							txLife.setAccessIntent(NbaConstants.UPDATE);
							txLife = NbaContractAccess.doContractUpdate(txLife, getWork(), getUser());
							// NBLXA-2156 Begin
							if (!NbaUtils.isBlankOrNull(updateDst) && updateDst.equalsIgnoreCase("True")) {
								updateWorkItem();
								// updateWork(getUser(), aWorkItem);
							} // NBLXA-2156 END
							unlockWork(getUser(), aWorkItem);
							NbaContractLock.removeLock(getWork(), getUser());
						} catch (NbaBaseException baseException) {
							getLogger().logDebug("Policy not processed - Policy number =" + policyNumber);
							out.println("Policy not processed - Policy number =" + policyNumber);
							baseException.printStackTrace();
						}
					}
				} else {
					getLogger().logDebug("Policy not found - Policy number =" + policyNumber);
					out.println("Policy not found - Policy number =" + policyNumber);
				}
			}
		} catch (Exception exp) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			exp.printStackTrace();
			throw new NbaBaseException(" " + this.getClass().getName() + " ", NbaExceptionType.FATAL);
		} finally {
			if (out != null) {
				out.close();
			}
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				e.printStackTrace();
			}
			try {
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				e.printStackTrace();
			}
		}
	}


    // Begin NBLXA-1831

	public void updateGoldenTicketInd(String changedvalue, String polNumber) {
		ListIterator results = null;
		NbaDst aWorkItem = null;
		try {
			NbaSearchVO searchVO = searchAggregateContract(polNumber);
			if (searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
				results = searchVO.getSearchResults().listIterator();
				while (results.hasNext()) {
					try {
						aWorkItem = retrieveWorkItem((NbaSearchResultVO) results.next());
						if (changedvalue.equalsIgnoreCase(TRUE_STR)) {
							aWorkItem.getNbaLob().setApplicationSpecial(GOLDEN_CASE);
						} else {
							aWorkItem.getNbaLob().deleteApplicationSpecial();
						}
						aWorkItem.setUpdate();
						aWorkItem.setNbaUserVO(getUser());
						setWork(aWorkItem);
						currentBP.callService("NbaUpdateWorkBP", getWork());
						unlockWork();
					} catch (NbaBaseException baseException) {
						unlockWork();
						baseException.printStackTrace();
					}
				}
			}
		} catch (NbaBaseException baseException) {
			baseException.printStackTrace();
		}
	}

	public NbaSearchVO searchAggregateContract(String contractKey) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_AGGREGATE_CONTRACT);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = lookupWork(getUser(), searchVO);
		return searchVO;
	}

	// END NBLXA-1831

	/**
	 * NBLXA-1894 New Method iterates over policy list and sends call for prior insurance refresh for each policy at a time
	 * @param policyNumbers list of policy numbers on which validation needs to be performed
	 * @param fileName file to write output log file after execution
	 */
	public void priorInsuranceRefresh(List<String> policyNumbers, String fileName) throws NbaBaseException {
		ListIterator results = null;
		NbaDst aWorkItem = null;
		NbaSearchVO searchVO = null;
		NbaTXLife txLife = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		String path = "";
		try {
			path = NbaConfiguration.getInstance().getFileLocation("PriorInsuranceRefresh");
			if (path == null || path.length() == 0) {
				throw new NbaBaseException("Path not Found ", NbaExceptionType.FATAL);
			}
			fw = new FileWriter(path+fileName + ".log", true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			NbaProcUnderwritingRisk proxy = new NbaProcUnderwritingRisk();
			for (String policyNumber : policyNumbers) {
				try {
					searchVO = searchContract(policyNumber.trim());
				} catch (NbaBaseException baseException) {
					getLogger().logDebug("Not able to lookup work in AWD");
					out.println("Not able to lookup work in AWD");
					baseException.printStackTrace();
				}
				if (searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
					results = searchVO.getSearchResults().listIterator();
					while (results.hasNext()) {
						try {
							aWorkItem = retrieveWorkItem((NbaSearchResultVO) results.next());
							setWork(aWorkItem);
							NbaContractLock.requestLock(aWorkItem, getUser());
							txLife = doHoldingInquiry();
							setNbaTxLife(txLife);
							rerunPriorIns(aWorkItem, txLife, proxy);
						} catch (NbaBaseException baseException) {
							getLogger().logDebug("Policy not processed - Policy number =" + policyNumber);
							out.println("Policy not processed - Policy number =" + policyNumber);
							baseException.printStackTrace();
						} finally {
							unlockWork(getUser(), aWorkItem);
							NbaContractLock.removeLock(getWork(), getUser());
						}
					}
				} else {
					getLogger().logDebug("Policy not found - Policy number =" + policyNumber);
					out.println("Policy not found - Policy number =" + policyNumber);
				}
			}
		} catch (Exception exp) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			exp.printStackTrace();
			throw new NbaBaseException(" " + this.getClass().getName() + " ", NbaExceptionType.FATAL);
		} finally {
			if (out != null) {
				out.close();
			}
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				e.printStackTrace();
			}
			try {
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				e.printStackTrace();
			}
		}
	}

	/**
	 * NBLXA-1894 New Method iterates over policy list and sends call for policy notification-1203 for each policy at a time
	 * @param policyNumbers list of policy numbers on which validation needs to be performed
	 * @param fileName file to write output log file after execution
	 */
	public void sendPolicyNotification(List<String> policyNumbers, String fileName) throws NbaBaseException {
		ListIterator results = null;
		NbaDst aWorkItem = null;
		NbaSearchVO searchVO = null;
		NbaTXLife txLife = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		String path = "";
		try {
			path = NbaConfiguration.getInstance().getFileLocation("SendPolicyNotification");
			if (path == null || path.length() == 0) {
				throw new NbaBaseException("Path not Found ", NbaExceptionType.FATAL);
			}
			fw = new FileWriter(path+fileName + ".log", true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			for (String policyNumber : policyNumbers) {
				try {
					searchVO = searchContract(policyNumber.trim());
				} catch (NbaBaseException baseException) {
					getLogger().logDebug("Not able to lookup work in AWD");
					out.println("Not able to lookup work in AWD");
					baseException.printStackTrace();
				}
				if (searchVO.getSearchResults() != null && !(searchVO.getSearchResults().isEmpty())) {
					results = searchVO.getSearchResults().listIterator();
					while (results.hasNext()) {
						try {
							aWorkItem = retrieveWorkItem((NbaSearchResultVO) results.next());
							setWork(aWorkItem);
							txLife = doHoldingInquiry();
							create1203DataFeedRequest(txLife);
							invokeAXAWebService(getUser());
							unlockWork(getUser(), aWorkItem);
						} catch (NbaBaseException baseException) {
							getLogger().logDebug("Policy not processed - Policy number =" + policyNumber);
							out.println("Policy not processed - Policy number =" + policyNumber);
							baseException.printStackTrace();
						}
					}
				} else {
					getLogger().logDebug("Policy not found - Policy number =" + policyNumber);
					out.println("Policy not found - Policy number =" + policyNumber);
				}
			}
		} catch (Exception exp) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			exp.printStackTrace();
			throw new NbaBaseException(" " + this.getClass().getName() + " ", NbaExceptionType.FATAL);
		} finally {
			if (out != null) {
				out.close();
			}
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				e.printStackTrace();
			}
			try {
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				e.printStackTrace();
			}
		}
	}

	/**
	 * NBLXA-1894 New Method sends call to calculateUnderwritingRisk to do prior insurance refresh for each policy at a time
	 * @param aWorkItem Dst work item object to send for prior insurance refresh
	 * @param txLife object to send for prior insurance refresh
	 * @param proxy NbaProcUnderwritingRisk object to invoke its calculateUnderwritingRisk method on given WorkItem and txLife objects
	 */
	public void rerunPriorIns(NbaDst aWorkItem, NbaTXLife txLife, NbaProcUnderwritingRisk proxy ) throws NbaBaseException{
		proxy.setUser(getUser());
		proxy.setNbaTxLife(txLife);
		proxy.setWork(aWorkItem);
		proxy.setContractAccess(NbaConstants.UPDATE);
		removeSystemMessage();
		proxy.calculateUnderwritingRisk();
		txLife = proxy.doContractUpdate();
		Holding holding = txLife.getPrimaryHolding();
		int systemMessageCount = holding.getSystemMessageCount();
		for (int msgIdx = systemMessageCount - 1; msgIdx > -1; msgIdx--) {
			SystemMessage systemMessage = holding.getSystemMessageAt(msgIdx);
			if (systemMessage.getMessageCode() == NbaConstants.UW_RISK_REQUEST_ERROR && !systemMessage.isActionDelete()
					&& !systemMessage.isActionDeleteSuccessful()) {
				addComment("Prior Insurance Refresh Failed Due to:" + systemMessage.getMessageDescription());
			}
		}
	}

	/**
	 * NBLXA-1894 New Method to delete CV messages of underwriting risk subset from current TxLife object
	 */
	protected void removeSystemMessage() {
		Holding holding = getNbaTxLife().getPrimaryHolding();
		ListIterator messageIterator = holding.getSystemMessage().listIterator();
		while (messageIterator.hasNext()) {
			SystemMessage currentMessage = (SystemMessage) messageIterator.next();
			SystemMessageExtension systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(currentMessage);
			if (systemMessageExtension != null && NbaContractValidationConstants.SUBSET_UW_RISK == systemMessageExtension.getMsgValidationType()) {
				currentMessage.setActionDelete();
				break;
			}
		}
	}

	//NBLXA-1957 new method
	public void updateCaseManagerQueue(String newCsmq, String polNumber, NbaUserVO userVo) throws NbaBaseException {
		setWork(getParentWorkItem(polNumber, userVo));
		NbaTXLife caseTxLife = doHoldingInquiry();
		String origCaseManager = "";
		if (getWork().getNbaLob() != null) {
			origCaseManager = getWork().getNbaLob().getCaseManagerQueue();
		}
		if (!NbaUtils.isBlankOrNull(newCsmq) && !origCaseManager.equalsIgnoreCase(newCsmq)) {
			updateCaseManagerLob(origCaseManager, newCsmq, polNumber);
			routeWIToNewCaseManager(origCaseManager, newCsmq, polNumber, userVo);
			resetCMCOntactName(caseTxLife, getWork());
		}
	}

	// NBLXA-1957 new method
	public void updateUnderwriterQueue(String newUNDQ, String polNumber, NbaUserVO userVo) throws NbaBaseException {
		setWork(getParentWorkItem(polNumber, userVo));
		NbaTXLife caseTxLife = doHoldingInquiry();
		String origUnderwriter = "";
		if (getWork().getNbaLob() != null) {
			origUnderwriter =getWork().getNbaLob().getUndwrtQueue();
		}
		if (!NbaUtils.isBlankOrNull(newUNDQ) && !origUnderwriter.equalsIgnoreCase(newUNDQ)) {
			updateUnderwriterLob(origUnderwriter, newUNDQ, polNumber);
			routeWIToUnderwriter(origUnderwriter, newUNDQ, polNumber, userVo);
			resetUWCOntactName(caseTxLife, getWork());
		}
	}

	// NBLXA-1957 new method
	public void updateUnderwriterLob(String origUndq, String newUndq, String polNumber) throws NbaBaseException {
		LockRetrieveWorkRequest lockRequest = new LockRetrieveWorkRequest();
		NbaDst parentWI = getWork();
		NbaLob tempLob = new NbaLob();
		tempLob.setUndwrtQueue(origUndq);
		tempLob.setPolicyNumber(polNumber);
		lockRequest.setLobData((LobData[]) tempLob.getLobs().toArray(new LobData[tempLob.getLobs().size()]));
		lockRequest.setWorkItem(getWork().getWorkItem());
		lockRequest.setBusinessArea(getWork().getBusinessArea());
		lockRequest.setRetrieveWorkLocked(true);
		String oldUndqQueue= parentWI.getNbaLob().getUndwrtQueue();
		String caseQueue= parentWI.getNbaLob().getQueue();
		parentWI.getNbaLob().setUndwrtQueue(newUndq);
		if (!NbaUtils.isBlankOrNull(caseQueue) && caseQueue.equalsIgnoreCase(oldUndqQueue)) {
			updateWorkItemQueue(parentWI, newUndq); // new added
		}
		parentWI.setUpdate();
		AccelResult resultnew = (AccelResult) currentBP.callBusinessService("LockRetrieveWorkBP", lockRequest);
		if (!resultnew.hasErrors()) {
			parentWI.addCase((WorkItem) resultnew.getFirst());
			parentWI.setNbaUserVO(user);
			updateLob(parentWI,null, newUndq);
			NbaTableAccessor nta = new NbaTableAccessor();
			String origUndManager_translation = AxaUtils.removeMultipleSpacesInText(nta.getUserName(getUserIdForQueue(origUndq)));
			String newUnd_translation = AxaUtils.removeMultipleSpacesInText(nta.getUserName(getUserIdForQueue(newUndq)));
			origUndManager_translation = (origUndManager_translation != null && origUndManager_translation.length() > 0) ? origUndManager_translation
					: origUndq;
			newUnd_translation = (newUnd_translation != null && newUnd_translation.length() > 0) ? newUnd_translation : newUndq;
			String comments = "Automatic Reassingment done. Underwriter changed from: " + origUndManager_translation + " to: "
					+ newUnd_translation;
			addComments(parentWI, comments);
			resultnew.merge(currentBP.callService("NbaUpdateWorkBP", parentWI));
		}
		currentBP.callBusinessService("NbaUnlockWorkBP", parentWI);
	}


	// NBLXA-1957 new method
	public void routeWIToUnderwriter(String origUndq, String newundq, String polNumber, NbaUserVO userVo) throws NbaBaseException {
		LockRetrieveWorkRequest lockRequest = new LockRetrieveWorkRequest();
		NbaDst parent = new NbaDst();
		setWork(getParentWorkItem(polNumber, userVo));
		NbaLob lob = new NbaLob();
		lob.setQueue(origUndq);
		lob.setPolicyNumber(polNumber);
		lockRequest.setLobData((LobData[]) lob.getLobs().toArray(new LobData[lob.getLobs().size()]));
		lockRequest.setWorkItem(getWork().getWorkItem());
		lockRequest.setQueueID(origUndq);
		lockRequest.setBusinessArea(getWork().getBusinessArea());
		lockRequest.setRetrieveWorkLocked(true);
		AccelResult result = (AccelResult) currentBP.callBusinessService("LockRetrieveWorkBP", lockRequest);
		if (!result.hasErrors()) {
			parent.addCase((WorkItem) result.getFirst());
			parent.setNbaUserVO(user);
			updateQueue(parent, newundq);
			result.merge(currentBP.callService("NbaUpdateWorkBP", parent));
		}
		currentBP.callBusinessService("NbaUnlockWorkBP", parent);

	}

	// NBLXA-1957 new method
	private NbaTXLife resetUWCOntactName(NbaTXLife txlife, NbaDst dst) throws NbaBaseException {
		String uwQueue = dst.getNbaLob().getUndwrtQueue();
		ApplicationInfo appinfo = txlife.getPrimaryHolding().getPolicy().getApplicationInfo();
		if (null != uwQueue && uwQueue.length() > 0) { // ALII1623, if CM Queue is set on case with value other than N2OVR..
			String userId = getUserIdForQueue(uwQueue); // ALII1623
			if (userId != null && userId.length() > 0) { // ALII1623
				appinfo.setHOUnderwriterName(userId); // ALII1623
				appinfo.setActionUpdate();// ALS5409,ALS5410
				txlife.setAccessIntent(NbaConstants.UPDATE); // SPR1851
				txlife.setBusinessProcess("REASSIGNMENT_QUEUE");
				txlife = NbaContractAccess.doContractUpdate(txlife, getWork(), getUser()); // NBA213
			}
			getLogger().logDebug("RoutePoller, ResetUNDQ - uwQueue=" + uwQueue + ",userId=" + userId);
		}
		return txlife;
	}

	// NBLXA-1957 new method
	private void updateQueue(String changedValue) throws NbaBaseException {
		if (queueMap.size() > 0) {
			if (CASE_MANAGER.equalsIgnoreCase(changedValue)) {
				for (String polNumber : queueMap.keySet()) {
					updateCaseManagerQueue(queueMap.get(polNumber), polNumber, user);
				}
			} else if (UNDERWRITER.equalsIgnoreCase(changedValue)) {
				for (String polNumber : queueMap.keySet()) {
					updateUnderwriterQueue(queueMap.get(polNumber), polNumber, user);
				}
			}

		}
	}

	// NBLXA-1957 new method
	public void updateWorkItemQueue(NbaDst parentWI, String newUndqLob) throws NbaDataAccessException {
		HashMap aCase = new HashMap();
		aCase.put(NbaTableAccessConstants.C_AWD_TABLE, NbaTableAccessConstants.WF_STATUSES_BY_WORK_AND_QUEUE);
		aCase.put("DOMAIN_NAME", NbaTableAccessConstants.WF_STATUSES_BY_WORK_AND_QUEUE);
		aCase.put(NbaTableAccessConstants.C_WORK_TYPE, parentWI.getWorkType());
		aCase.put(NbaTableAccessConstants.C_BUSINESS_AREA, parentWI.getBusinessArea());
		aCase.put(NbaTableAccessConstants.C_DATANAME, newUndqLob);
		NbaTableData[] tableData = getNbaTableAccessor().getDisplayData(aCase, NbaTableConstants.AWD_TABLES);
		List<String> status = new ArrayList<String>();
		if (tableData.length > 0) {
			tableData = getNbaTableAccessor().getStatusesTranslations("AWD_STATUS", tableData);
			if (tableData != null) {
				int count = tableData.length;
				for (int i = 0; i < count; i++) {
					status.add(tableData[i].code() + "@#" + tableData[i].text());
				}
			}
		}
		if (status.size() > 0) {
			setWorkItemStatus(status, parentWI.getStatus());
			parentWI.setStatus(getStatus());
			parentWI.getNbaLob().setRouteReason(getRouteReason());
		}
	}

	/**
	 * NBLXA-1957 New Method reads file containing SPACE separated policy numbers based on the change type
	 * @param file name that needs to be retrieved and read
	 * @param changeType the value of change type based on which folder will be decided to read file
	 * @return a List having policy numbers
	 */
	public void readBulkAssigmentData(String fileName, String changeType, String changedValue) throws NbaBaseException {
		boolean documentFound = false;
		String path = null;
		BufferedReader br = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		String line = "";
		String cvsSplitBy = ",";
		String fileLocation = "";
		try {
			if (changeType.equalsIgnoreCase(JOB_BULK_ASSIGNMENT) || changeType.equals(JOB_ARCOS_BULK_ASSIGNMENT)) {
				fileLocation = "BulkAssignment";
			}
			path = NbaConfiguration.getInstance().getFileLocation(fileLocation);
			if (path == null || path.length() == 0) {
				throw new NbaBaseException("Path not Found ", NbaExceptionType.FATAL);
			}
			getLogger().logDebug(path + " path 1");
			File folder = new File(path);
			getLogger().logDebug(folder + " folder 1");
			if (folder == null) {
				throw new NbaBaseException(fileLocation + " Folder not Found at " + path, NbaExceptionType.FATAL);
			}
			fw = new FileWriter(path + fileName + ".log", true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			File[] listOfFiles = folder.listFiles();
			getLogger().logDebug(listOfFiles + " listOfFiles");
			for (int index = 0; index < listOfFiles.length; index++) {
				getLogger().logDebug(listOfFiles[index] + " listOfFiles.index");
				if (listOfFiles[index].isFile() && listOfFiles[index].getName().equalsIgnoreCase(fileName + ".txt")) {
					writeToLogFile(listOfFiles[index].getName() + " Reading file.");
					br = new BufferedReader(new FileReader(listOfFiles[index].getPath()));
					int line_no = 0;
					while ((line = br.readLine()) != null) {
						if (line.equalsIgnoreCase("")) {
							continue;
						}
						StringTokenizer tokenizer = new StringTokenizer(line, cvsSplitBy);
						while (tokenizer.hasMoreTokens()) {
							line_no++;
							if (tokenizer.countTokens() == 2) {
								String policyNumber = tokenizer.nextToken().trim();
								String queue = tokenizer.nextToken().trim();
								getLogger().logDebug("PolicyNumber  ::" + policyNumber);
								if ((policyNumber.length() < 5 || policyNumber.length() > 15) || ! AxaUtils.isValidContract(policyNumber.trim())) {
									getLogger().logDebug("Seems invalid policy Number : " + policyNumber);
									out.println("Invalid policy Number : " + policyNumber);
									break;
								}
								if (CASE_MANAGER.equalsIgnoreCase(changedValue) && !queue.startsWith("N2UCM")) {
									out.println("Invalid Case Underwriter Queue :" + queue);
									break;
								} else if (UNDERWRITER.equalsIgnoreCase(changedValue) && !queue.startsWith("N2UW")) {
									out.println("Invalid Underwriter Queue :" + queue);
									break;
								}
								if(! AxaUtils.isValidQueue(queue.trim())){
									out.println("Invalid Queue :" + queue);
									break;
								}
								queueMap.put(policyNumber, queue);
							} else {
								out.println("Invalid input at line :" + line_no);
								break;
							}

						}
						if (queueMap.size() > 0) {
							getLogger().logDebug(queueMap.size() + " record(s) found.");
						}

						if (queueMap.size() == 0) {
							getLogger().logDebug("No record(s) found.");
						}
						documentFound = true;
					}
				}
			}
			if (!documentFound) {
				getLogger().logDebug("No File to Process.");
				out.println("No File to Process.");
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
			}
		} catch (ArrayIndexOutOfBoundsException indexOutoFBound) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			getLogger().logDebug("No Matching separator , found");
			out.println("No Matching separator , found");
			throw new NbaBaseException("No Matching separator , found " + this.getClass().getName() + " ", NbaExceptionType.FATAL);

		} catch (Exception exp) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			exp.printStackTrace();
			throw new NbaBaseException(" " + this.getClass().getName() + " ", NbaExceptionType.FATAL);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
					e.printStackTrace();
				}
			}
			if (out != null) {
				out.close();
			}
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				e.printStackTrace();
			}
			try {
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				e.printStackTrace();
			}
		}
	}
	/**
	 * NBLXA-2089 New Method to retrieve all the child work items and sources and update plan LOB on them based on user input
	 * @param newValue plan LOB that needs to be updated
	 * @param crda crda of the case that needs to be updated
	 * @return userVO user details that is updating
	 */
	public void changePlan(String newValue, String crda, NbaUserVO userVo,String polNumber) throws NbaBaseException {
		NbaDst expandedWork = null;
		// Retrieve and lock the child sources.
		NbaAwdRetrieveOptionsVO aNbaAwdRetrieveOptionsVO = new NbaAwdRetrieveOptionsVO();
		aNbaAwdRetrieveOptionsVO.setWorkItem(crda, true);
		aNbaAwdRetrieveOptionsVO.requestSources();
		aNbaAwdRetrieveOptionsVO.requestSources();
		aNbaAwdRetrieveOptionsVO.setLockWorkItem();
		aNbaAwdRetrieveOptionsVO.requestTransactionAsChild();
		aNbaAwdRetrieveOptionsVO.setLockTransaction();
		aNbaAwdRetrieveOptionsVO.setLockParentCase();
		expandedWork = retrieveWorkItem(userVo, aNbaAwdRetrieveOptionsVO);
		setWork(expandedWork);
		updatePlan(expandedWork, newValue);
		expandedWork.setNbaUserVO(user);
		currentBP.callService("NbaUpdateWorkBP", expandedWork);
		currentBP.callBusinessService("NbaUnlockWorkBP", expandedWork);
		try {
			unlockWorkitemAndSessionCleanUP(expandedWork);
		} catch (NbaBaseException e) {
			getLogger().logDebug("RoutePoller in Unlock Catch - crda = "+crda +" plan LOB new :"+newValue);
			e.printStackTrace();
		}
		NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(polNumber, JOB_PLAN_CHANGE, newValue, CASE_PROCESSED);

	}

	/**
	 * NBLXA-2089 New Method to retrieve all the child work items and sources and update plan LOB on them based on user input
	 * @param parent dst object of the case that needs to be updated
	 * @return plan will be the new plan LOB that needs to be updated
	 */
	public void updatePlan(NbaDst parent, String plan) throws NbaBaseException {
		List list = parent.getTransactions();
		Iterator itr = list.iterator();
		NbaTransaction transaction = null;
		String existingPlan = "";
		while (itr.hasNext()) {
			transaction = new NbaTransaction((WorkItem) itr.next());
			existingPlan = transaction.getNbaLob().getPlan();
			if(null != existingPlan &&  !existingPlan.isEmpty() && !existingPlan.equalsIgnoreCase(plan)){
				transaction.getNbaLob().setPlan(plan);
				transaction.setUpdate();
			}
		}
		list = parent.getNbaSources();
		NbaSource source = null;
		for (int srcIdx = 0; srcIdx < list.size(); srcIdx++) {
			source = (NbaSource) list.get(srcIdx);
			existingPlan = source.getNbaLob().getPlan();
			if(null != existingPlan &&  !existingPlan.isEmpty() && !existingPlan.equalsIgnoreCase(plan)){
				source.getNbaLob().setPlan(plan);
				source.setUpdate();
			}
		}
	}

	//NBLXA-2150
	public NbaSearchVO searchContract(String contractKey,NbaUserVO user) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		return searchVO;
	}


	/**
	 * NBLXA-2150 New method to waive selected requirements which are in add/order status or both and also update requirement DST Object.
	 * @param nbaTXLife
	 * @param selectedReqInfoList
	 * @throws NbaBaseException
	 */
	public void waiveSelectedRequirements(NbaTXLife nbaTXLife, ArrayList<RequirementInfo> selectedReqInfoList) throws NbaBaseException {
		NbaDst requirementDst = null;
		try {
			List<RequirementInfo> reqInfoList = selectedReqInfoList;
			Policy policy = nbaTXLife.getPolicy();
			if (null != reqInfoList && !reqInfoList.isEmpty()) {
				for (int i = 0; i < reqInfoList.size(); i++) {
					RequirementInfo reqInfo = reqInfoList.get(i);
					if (reqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_ADD || reqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_ORDER
							|| reqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_SUBMITTED) {
						reqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_WAIVED);
						reqInfo.setStatusDate(new Date());
						requirementDst = getRequirementWI(policy.getPolNumber(), reqInfo.getRequirementInfoUniqueID());
						RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
						if (reqInfoExt != null && null != requirementDst
								&& requirementDst.getNbaLob().getReqUniqueID().equalsIgnoreCase(reqInfo.getRequirementInfoUniqueID())) {

							requirementDst.getNbaLob().setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_WAIVED));
							requirementDst.setStatus(NbaConstants.A_STATUS_REQUIREMENT_CANCELLED);
							// requirementDst.getNbaLob().setRouteReason("Aggregated");
							requirementDst.getNbaLob().setRouteReason("Requirement Cancelled");
							if (requirementDst.getNbaTransaction().isSuspended()) {
								NbaSuspendVO suspendVO = new NbaSuspendVO();
								suspendVO.setTransactionID(requirementDst.getNbaTransaction().getID());
								unsuspendVOs.add(suspendVO);
							}
							unsuspendWorkitems();
							requirementDst.getNbaTransaction().setUpdate();
							reqInfoExt.setActionUpdate();
							update(requirementDst);
							unlockWork(requirementDst);

						}
						reqInfo.setActionUpdate();
						// break;
					}
				}
				String origBusinessProcess = nbaTXLife.getBusinessProcess();
				nbaTXLife.setBusinessProcess(NbaConstants.PROC_REQUIREMENTS);
				setContractAccess(UPDATE);
				doContractUpdate(nbaTXLife);
				// nbaTXLife.setAccessIntent(NbaConstants.UPDATE);
				// NbaContractAccess.doContractUpdate(nbaTXLife,getNbaDst(nbaTXLife), getNbaUserVO());
				nbaTXLife.setBusinessProcess(origBusinessProcess);
			}
		} catch (NbaBaseException e1) {
			throw new NbaBaseException("This workItem cannot be processed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null != requirementDst && requirementDst.isLocked(user.getUserID())) {
				try {
					unlockWork(requirementDst);
				} catch (NbaBaseException nbe) {
					getLogger().logException(nbe);
				}
			}
		}
	}


	/**
	 * NBLXA-2150 New Method to fetch dst object of requirement workitem.
	 * @param policyNumber
	 * @param reqUniqueID
	 * @return
	 * @throws Exception
	 */
	protected NbaDst getRequirementWI(String policyNumber, String reqUniqueID) throws Exception {
		NbaSearchResultVO resultVO = null;
		NbaLob lob = new NbaLob();
		lob.setPolicyNumber(policyNumber);
		lob.setReqUniqueID(reqUniqueID);
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


	/**
	 * NBLXA-2150 New Method to read requirement UIs from file
	 * @param fileName
	 * @return
	 * @throws NbaBaseException
	 */
	protected String[] readRequirementInputFile(String fileName) throws NbaBaseException{
		boolean documentFound = false;
		String path = null;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try{
			path = NbaConfiguration.getInstance().getFileLocation("BulkRequirementUtility");
			if(path == null || path.length()== 0) {
				throw new NbaBaseException("Path not Found ", NbaExceptionType.FATAL);
			}
			File folder = new File(path);
			if(folder == null ) {
				throw new NbaBaseException("BulkRequirementUtility Folder not Found at " + path, NbaExceptionType.FATAL);
			}
			File[] listOfFiles = folder.listFiles();
			getLogger().logDebug(listOfFiles + " listOfFiles");
			if (!NbaUtils.isBlankOrNull(listOfFiles)) {
				for (int index = 0; index < listOfFiles.length; index++) {
					getLogger().logDebug(listOfFiles[index]+  " listOfFiles.index");
					if (listOfFiles[index].isFile() && listOfFiles[index].getName().equalsIgnoreCase(fileName+".txt")) {
						writeToLogFile(listOfFiles[index].getName() + " Reading BulkRequirementUtility file.");
						br = new BufferedReader(new FileReader(listOfFiles[index].getPath()));
						while ((line = br.readLine()) != null) {

							// use comma as separator
							if (line.equalsIgnoreCase("")) {
								continue;
							}
							selectedReqUniqueIDList = line.split(cvsSplitBy);
							//maps.put(entrySet[0], entrySet[1]);
							//writeToLogFile(entrySet[0] + "  " + entrySet[1] + "  record found.");
							documentFound = true;
						}

					}
				}
			}
			getLogger().logDebug( documentFound + "  documentFound");

			if (!documentFound) {
				writeToLogFile("No File to Process.");
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
			}
		} catch (ArrayIndexOutOfBoundsException indexOutoFBound) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			writeToLogFile(" No Matching separator , found");
			throw new NbaBaseException("No Matching separator , found " + this.getClass().getName() + " ", NbaExceptionType.FATAL);

		} catch (Exception exp) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			exp.printStackTrace();
			throw new NbaBaseException(" " + this.getClass().getName() + " ", NbaExceptionType.FATAL);
		}finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
					e.printStackTrace();
				}
			}
		}
		return selectedReqUniqueIDList;
	}


	/**
	 * NBLXA-2150 New Method to delete Processed requirement file from specified location.
	 * @throws NbaBaseException
	 */
	public void deleteProcessedReqDocs(String fileName) throws NbaBaseException{
		String path = NbaConfiguration.getInstance().getFileLocation("BulkRequirementUtility");

		if(path == null || path.length()== 0) {
			throw new NbaBaseException( path + " Path not Found ", NbaExceptionType.FATAL);
		}

		File folder = new File(path);
		if(folder == null ) {
			throw new NbaBaseException("BulkRequirementUtility Folder not Found at " + path, NbaExceptionType.FATAL);
		}
		File[] listOfFiles = folder.listFiles();
		getLogger().logDebug(listOfFiles + " listOfFiles");
		if (!NbaUtils.isBlankOrNull(listOfFiles)) {
			if(listOfFiles.length < 1) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
				return;
			}

			for (int index = 0; index < listOfFiles.length; index++) {
				if (listOfFiles[index].isFile() && listOfFiles[index].getName().equalsIgnoreCase(fileName+".txt")) {
					if(listOfFiles[index].delete()){
						writeToLogFile( listOfFiles[index].getName() + "Deleted Successfully.");
					}
				}
			}
		}
	}

	/**
	 * Iterate thru unsuspendVO list and call unsuspendWork method
	 * on netserveraccessor bean for each value object.
	 * @throws NbaBaseException
	 */
	//NBLXA-2150 New Method
	protected void unsuspendWorkitems() throws NbaBaseException {
		int size = unsuspendVOs.size();
		for (int i = 0; i < size; i++) {
			unsuspendWork((NbaSuspendVO) unsuspendVOs.get(i));
		}
	}

	private static Date getZeroTimeDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		date = calendar.getTime();
		return date;
	}

	public void arcosLogging(String sql) {
		if (arcoslogger == null) {
			try {
				arcoslogger = NbaLogFactory.getLogger(NbaProcRouteWI.class.getName() + ".Arcos"); // NBA103
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + ".Arcos" + " could not get a logger from the factory."); // NBA103
				e.printStackTrace(System.out);
			}
		}
		if (arcoslogger.isDebugEnabled()) {
			arcoslogger.logDebug(sql);
		}
	}


	//Begins NBLXA-2162
		//NBLXA-2162 - New Method
		public void changePrimaryInsuredName(String correctedName, String crda, NbaUserVO userVo,String polnumber) throws NbaBaseException {
			NbaDst expandedWork = null;
			// Retrieve and lock the child transactions and sources.
			NbaAwdRetrieveOptionsVO aNbaAwdRetrieveOptionsVO = new NbaAwdRetrieveOptionsVO();
			aNbaAwdRetrieveOptionsVO.setWorkItem(crda, true);
			aNbaAwdRetrieveOptionsVO.setLockParentCase();
			aNbaAwdRetrieveOptionsVO.requestSources();
			aNbaAwdRetrieveOptionsVO.setLockWorkItem();
			aNbaAwdRetrieveOptionsVO.requestTransactionAsChild();
			aNbaAwdRetrieveOptionsVO.setLockTransaction();
			expandedWork = retrieveWorkItem(userVo, aNbaAwdRetrieveOptionsVO);
			String[] fullName = splitName(correctedName, ",");
			String firstName = fullName[0];
			String middleName = fullName[1];
			String lastName = fullName[2];
			long primInsRprs = NbaOliConstants.OLI_REL_INSURED;
			updatePrimaryInsuredName(expandedWork, firstName, middleName, lastName);
			updateInsureNameForRequirement(expandedWork, firstName, middleName, lastName, primInsRprs);
			expandedWork.setNbaUserVO(user);
			String comments= "Name for Primary Insured changed to " + correctedName;
			addComments(expandedWork,comments);
			currentBP.callService("NbaUpdateWorkBP", expandedWork);
			currentBP.callBusinessService("NbaUnlockWorkBP", expandedWork);
			try {
				unlockWorkitemAndSessionCleanUP(expandedWork);
			} catch (NbaBaseException e) {
				getLogger().logDebug("RoutePoller in Unlock Catch - for CRDA = "+crda +" changed name:"+correctedName);
				e.printStackTrace();
			}
			NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(polnumber, JOB_PRIMARY_INS_NAME_UPDATE, correctedName, CASE_PROCESSED);
		}

		//NBLXA-2162 - New Method
		public static String[] splitName(String fullName, String cvsSplitBy){
			String[] name = fullName.split(cvsSplitBy);
			return name;
		}

		//NBLXA-2162 - New Method
		public void updatePrimaryInsuredName(NbaDst parent, String insFirstName, String insMiddleName, String insLastName) throws NbaBaseException {
			List list = parent.getTransactions();
			Iterator itr = list.iterator();
			NbaTransaction transaction = null;
			while (itr.hasNext()) {
				transaction = new NbaTransaction((WorkItem) itr.next());
				if(!transaction.getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)
						&& !transaction.getWorkType().equals(NbaConstants.A_WT_MISC_MAIL)
						&& !transaction.getWorkType().equals(NbaConstants.A_WT_TEMP_REQUIREMENT)){
					transaction.getNbaLob().setFirstName(insFirstName);
					transaction.getNbaLob().setMiddleInitial(insMiddleName);
					transaction.getNbaLob().setLastName(insLastName);
					transaction.setUpdate();
				}
			}
			list = parent.getNbaSources();
			NbaSource source = null;
			for (int srcIdx = 0; srcIdx < list.size(); srcIdx++) {
				source = (NbaSource) list.get(srcIdx);
				if (!NbaUtils.isBlankOrNull(source)
						&& !source.getSourceType().equals(NbaConstants.A_ST_PROVIDER_RESULT)
						&& !source.getSourceType().equals(NbaConstants.A_ST_MISC_MAIL)
						&& !source.getSourceType().equals(NbaConstants.A_ST_CORRESPONDENCE_LETTER)){
					source.getNbaLob().setFirstName(insFirstName);
					source.getNbaLob().setLastName(insLastName);
					source.getNbaLob().setMiddleInitial(insMiddleName);
					source.setUpdate();
				}
			}
		}

		//NBLXA-2162 - New Method
		public void updateInsureNameForRequirement(NbaDst parent, String insFirstName, String insMiddleName, String insLastName, long rprs) throws NbaBaseException {
			List list = parent.getTransactions();
			Iterator itr = list.iterator();
			NbaTransaction transaction = null;
			while (itr.hasNext()) {
				transaction = new NbaTransaction((WorkItem) itr.next());
				NbaLob transactionLob = transaction.getNbaLob();
				int personSeqCode = transactionLob.getReqPersonCode();
				if (transaction.getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)
						&& !NbaUtils.isBlankOrNull(personSeqCode) && personSeqCode == rprs){
					transaction.getNbaLob().setFirstName(insFirstName);
					transaction.getNbaLob().setMiddleInitial(insMiddleName);
					transaction.getNbaLob().setLastName(insLastName);
					transaction.setUpdate();
				}
			}
			list = parent.getNbaSources();
			NbaSource source = null;
			for (int srcIdx = 0; srcIdx < list.size(); srcIdx++) {
				source = (NbaSource) list.get(srcIdx);
				if (!NbaUtils.isBlankOrNull(source)
						&& source.getSourceType().equals(NbaConstants.A_ST_PROVIDER_RESULT)
						&& source.getSourceType().equals(NbaConstants.A_ST_MISC_MAIL)){
					source.getNbaLob().setFirstName(insFirstName);
					source.getNbaLob().setLastName(insLastName);
					source.getNbaLob().setMiddleInitial(insMiddleName);
					source.setUpdate();
				}
			}

		}

		//NBLXA-2162 - New Method
		public void changePrimaryInsuredSSN(String correctedSSN, String crda, NbaUserVO userVo,String polNumber) throws NbaBaseException {
			NbaDst expandedWork = null;
			// Retrieve and lock the child transactions and sources.
			NbaAwdRetrieveOptionsVO aNbaAwdRetrieveOptionsVO = new NbaAwdRetrieveOptionsVO();
			aNbaAwdRetrieveOptionsVO.setWorkItem(crda, true);
			aNbaAwdRetrieveOptionsVO.setLockParentCase();
			aNbaAwdRetrieveOptionsVO.requestSources();
			aNbaAwdRetrieveOptionsVO.setLockWorkItem();
			aNbaAwdRetrieveOptionsVO.requestTransactionAsChild();
			aNbaAwdRetrieveOptionsVO.setLockTransaction();
			expandedWork = retrieveWorkItem(userVo, aNbaAwdRetrieveOptionsVO);
			long primInsRprs = NbaOliConstants.OLI_REL_INSURED;
			updatePrimaryInsuredSSN(expandedWork, correctedSSN);
			updateInsuredSSNForRequirement(expandedWork, correctedSSN, primInsRprs);
			expandedWork.setNbaUserVO(user);
			String comments= "SSN for Primary Insured changed to " + correctedSSN;
			addComments(expandedWork,comments);
			currentBP.callService("NbaUpdateWorkBP", expandedWork);
			currentBP.callBusinessService("NbaUnlockWorkBP", expandedWork);
			try {
				unlockWorkitemAndSessionCleanUP(expandedWork);
			} catch (NbaBaseException e) {
				getLogger().logDebug("RoutePoller in Unlock Catch - for CRDA = "+crda +" updated SSN:"+correctedSSN);
				e.printStackTrace();
			}
			NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(polNumber, JOB_PRIMARY_INS_SSN_UPDATE, correctedSSN, CASE_PROCESSED);
		}

		//NBLXA-2162 - New Method
		public void updatePrimaryInsuredSSN(NbaDst parent, String insSSN) throws NbaBaseException {
			List list = parent.getTransactions();
			Iterator itr = list.iterator();
			NbaTransaction transaction = null;
			while (itr.hasNext()) {
				transaction = new NbaTransaction((WorkItem) itr.next());
				if(!transaction.getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)
						&& !transaction.getWorkType().equals(NbaConstants.A_WT_MISC_MAIL)
						&& !transaction.getWorkType().equals(NbaConstants.A_WT_TEMP_REQUIREMENT)){
					transaction.getNbaLob().setSsnTin(insSSN);
					transaction.setUpdate();
				}
			}
			list = parent.getNbaSources();
			NbaSource source = null;
			for (int srcIdx = 0; srcIdx < list.size(); srcIdx++) {
				source = (NbaSource) list.get(srcIdx);
				if (!NbaUtils.isBlankOrNull(source)
						&& !source.getSourceType().equals(NbaConstants.A_ST_PROVIDER_RESULT)
						&& !source.getSourceType().equals(NbaConstants.A_ST_MISC_MAIL)
						&& !source.getSourceType().equals(NbaConstants.A_ST_CORRESPONDENCE_LETTER)){
					source.getNbaLob().setSsnTin(insSSN);
					source.setUpdate();
				}
			}

		}

		//NBLXA-2162 - New Method
		public void updateInsuredSSNForRequirement(NbaDst parent, String insSSN, long rprs) throws NbaBaseException {
			List list = parent.getTransactions();
			Iterator itr = list.iterator();
			NbaTransaction transaction = null;
			while (itr.hasNext()) {
				transaction = new NbaTransaction((WorkItem) itr.next());
				NbaLob transactionLob = transaction.getNbaLob();
				int personSeqCode = transactionLob.getReqPersonCode();
				if (transaction.getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)
						&& !NbaUtils.isBlankOrNull(personSeqCode) && personSeqCode == rprs){
					transaction.getNbaLob().setSsnTin(insSSN);
					transaction.setUpdate();
				}
			}
			list = parent.getNbaSources();
			NbaSource source = null;
			for (int srcIdx = 0; srcIdx < list.size(); srcIdx++) {
				source = (NbaSource) list.get(srcIdx);
				if (!NbaUtils.isBlankOrNull(source)
						&& source.getSourceType().equals(NbaConstants.A_ST_PROVIDER_RESULT)
						&& source.getSourceType().equals(NbaConstants.A_ST_MISC_MAIL)){
					source.getNbaLob().setSsnTin(insSSN);
					source.setUpdate();
				}
			}
		}

		//NBLXA-2162 - New Method
		public void changePrimaryInsuredDOB(String correctedDOB, String crda, NbaUserVO userVo,String polNumber) throws NbaBaseException {
			NbaDst expandedWork = null;
			// Retrieve and lock the child transactions and sources.
			NbaAwdRetrieveOptionsVO aNbaAwdRetrieveOptionsVO = new NbaAwdRetrieveOptionsVO();
			aNbaAwdRetrieveOptionsVO.setWorkItem(crda, true);
			aNbaAwdRetrieveOptionsVO.setLockParentCase();
			aNbaAwdRetrieveOptionsVO.requestSources();
			aNbaAwdRetrieveOptionsVO.setLockWorkItem();
			aNbaAwdRetrieveOptionsVO.requestTransactionAsChild();
			aNbaAwdRetrieveOptionsVO.setLockTransaction();
			expandedWork = retrieveWorkItem(userVo, aNbaAwdRetrieveOptionsVO);
			long primInsRprs = NbaOliConstants.OLI_REL_INSURED;
			updatePrimaryInsuredDOB(expandedWork, correctedDOB);
			updateInsuredDOBForRequirement(expandedWork, correctedDOB, primInsRprs);
			expandedWork.setNbaUserVO(user);
			String comments= "DOB for Primary Insured changed to " + correctedDOB;
			addComments(expandedWork,comments);
			currentBP.callService("NbaUpdateWorkBP", expandedWork);
			currentBP.callBusinessService("NbaUnlockWorkBP", expandedWork);
			try {
				unlockWorkitemAndSessionCleanUP(expandedWork);
			} catch (NbaBaseException e) {
				getLogger().logDebug("RoutePoller in Unlock Catch - for CRDA = "+crda +" updated DOB:"+correctedDOB);
				e.printStackTrace();
			}
			NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(polNumber, JOB_PRIMARY_INS_DOB_UPDATE, correctedDOB, CASE_PROCESSED);
		}

		//NBLXA-2162 - New Method
		public void updatePrimaryInsuredDOB(NbaDst parent, String insDOB) throws NbaBaseException {
			List list = parent.getTransactions();
			Iterator itr = list.iterator();
			NbaTransaction transaction = null;
			Date birthdDate = null;
			try {
				birthdDate = new SimpleDateFormat("yyyyMMdd").parse(insDOB);
			} catch (ParseException e) {
				getLogger().logDebug("RoutePoller in Unlock Catch for DOB Update");
				e.printStackTrace();
			}
			while (itr.hasNext()) {
				transaction = new NbaTransaction((WorkItem) itr.next());
				if(!transaction.getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)
						&& !transaction.getWorkType().equals(NbaConstants.A_WT_MISC_MAIL)
						&& !transaction.getWorkType().equals(NbaConstants.A_WT_TEMP_REQUIREMENT)){
					transaction.getNbaLob().setDOB(birthdDate);
					transaction.setUpdate();
				}
			}
			list = parent.getNbaSources();
			NbaSource source = null;
			for (int srcIdx = 0; srcIdx < list.size(); srcIdx++) {
				source = (NbaSource) list.get(srcIdx);
				if (!NbaUtils.isBlankOrNull(source)
						&& !source.getSourceType().equals(NbaConstants.A_ST_PROVIDER_RESULT)
						&& !source.getSourceType().equals(NbaConstants.A_ST_MISC_MAIL)
						&& !source.getSourceType().equals(NbaConstants.A_ST_CORRESPONDENCE_LETTER)){
					source.getNbaLob().setDOB(birthdDate);
					source.setUpdate();
				}
			}
		}

		//NBLXA-2162 - New Method
		public void updateInsuredDOBForRequirement(NbaDst parent, String insDOB, long rprs) throws NbaBaseException {
			List list = parent.getTransactions();
			Iterator itr = list.iterator();
			NbaTransaction transaction = null;
			Date birthdDate = null;
			try {
				birthdDate = new SimpleDateFormat("yyyyMMdd").parse(insDOB);
			} catch (ParseException e) {
				getLogger().logDebug("RoutePoller in Unlock Catch for DOB Update for Requirement");
				e.printStackTrace();
			}
			while (itr.hasNext()) {
				transaction = new NbaTransaction((WorkItem) itr.next());
				NbaLob transactionLob = transaction.getNbaLob();
				int personSeqCode = transactionLob.getReqPersonCode();
				if (transaction.getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)
						&& !NbaUtils.isBlankOrNull(personSeqCode) && personSeqCode == rprs){
					transaction.getNbaLob().setDOB(birthdDate);
					transaction.setUpdate();
				}
			}
			list = parent.getNbaSources();
			NbaSource source = null;
			for (int srcIdx = 0; srcIdx < list.size(); srcIdx++) {
				source = (NbaSource) list.get(srcIdx);
				if (!NbaUtils.isBlankOrNull(source)
						&& source.getSourceType().equals(NbaConstants.A_ST_PROVIDER_RESULT)
						&& source.getSourceType().equals(NbaConstants.A_ST_MISC_MAIL)){
					source.getNbaLob().setDOB(birthdDate);
					source.setUpdate();
				}
			}
		}

		//NBLXA-2162 - New Method
		public void changeJointInsuredName(String correctedName, String crda, NbaUserVO userVo, String polnumber) throws NbaBaseException {
			NbaDst expandedWork = null;
			// Retrieve and lock the child transactions and sources.
			NbaAwdRetrieveOptionsVO aNbaAwdRetrieveOptionsVO = new NbaAwdRetrieveOptionsVO();
			aNbaAwdRetrieveOptionsVO.setWorkItem(crda, true);
			aNbaAwdRetrieveOptionsVO.setLockParentCase();
			aNbaAwdRetrieveOptionsVO.requestSources();
			aNbaAwdRetrieveOptionsVO.setLockWorkItem();
			aNbaAwdRetrieveOptionsVO.requestTransactionAsChild();
			aNbaAwdRetrieveOptionsVO.setLockTransaction();
			expandedWork = retrieveWorkItem(userVo, aNbaAwdRetrieveOptionsVO);
			String[] fullName = splitName(correctedName, ",");
			String firstName = fullName[0];
			String middleName = fullName[1];
			String lastName = fullName[2];
			long jointInsRprs = NbaOliConstants.OLI_REL_JOINTINSURED;
			updateInsureNameForRequirement(expandedWork, firstName, middleName, lastName, jointInsRprs);
			expandedWork.setNbaUserVO(user);
			String comments= "Name for Joint Insured changed to " + correctedName;
			addComments(expandedWork,comments);
			currentBP.callService("NbaUpdateWorkBP", expandedWork);
			currentBP.callBusinessService("NbaUnlockWorkBP", expandedWork);
			try {
				unlockWorkitemAndSessionCleanUP(expandedWork);
			} catch (NbaBaseException e) {
				getLogger().logDebug("RoutePoller in Unlock Catch - for CRDA = "+crda +" changed name:"+correctedName);
				e.printStackTrace();
			}
			NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(polnumber, JOB_JOINT_INS_NAME_UPDATE, correctedName, CASE_PROCESSED);
		}

		//NBLXA-2162 - New Method
		public void changeJointInsuredSSN(String correctedSSN, String crda, NbaUserVO userVo,String polNumber) throws NbaBaseException {
			NbaDst expandedWork = null;
			// Retrieve and lock the child transactions and sources.
			NbaAwdRetrieveOptionsVO aNbaAwdRetrieveOptionsVO = new NbaAwdRetrieveOptionsVO();
			aNbaAwdRetrieveOptionsVO.setWorkItem(crda, true);
			aNbaAwdRetrieveOptionsVO.setLockParentCase();
			aNbaAwdRetrieveOptionsVO.requestSources();
			aNbaAwdRetrieveOptionsVO.setLockWorkItem();
			aNbaAwdRetrieveOptionsVO.requestTransactionAsChild();
			aNbaAwdRetrieveOptionsVO.setLockTransaction();
			expandedWork = retrieveWorkItem(userVo, aNbaAwdRetrieveOptionsVO);
			long jointInsRprs = NbaOliConstants.OLI_REL_JOINTINSURED;
			updateInsuredSSNForRequirement(expandedWork, correctedSSN, jointInsRprs);
			expandedWork.setNbaUserVO(user);
			String comments= "SSN for Joint Insured changed to " + correctedSSN;
			addComments(expandedWork,comments);
			currentBP.callService("NbaUpdateWorkBP", expandedWork);
			currentBP.callBusinessService("NbaUnlockWorkBP", expandedWork);
			try {
				unlockWorkitemAndSessionCleanUP(expandedWork);
			} catch (NbaBaseException e) {
				getLogger().logDebug("RoutePoller in Unlock Catch - for CRDA = "+crda +" updated SSN:"+correctedSSN);
				e.printStackTrace();
			}
			NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(polNumber, JOB_JOINT_INS_SSN_UPDATE, correctedSSN, CASE_PROCESSED);
		}

		//NBLXA-2162 - New Method
		public void changeJointInsuredDOB(String correctedDOB, String crda, NbaUserVO userVo,String polnumber) throws NbaBaseException {
			NbaDst expandedWork = null;
			// Retrieve and lock the child transactions and sources.
			NbaAwdRetrieveOptionsVO aNbaAwdRetrieveOptionsVO = new NbaAwdRetrieveOptionsVO();
			aNbaAwdRetrieveOptionsVO.setWorkItem(crda, true);
			aNbaAwdRetrieveOptionsVO.setLockParentCase();
			aNbaAwdRetrieveOptionsVO.requestSources();
			aNbaAwdRetrieveOptionsVO.setLockWorkItem();
			aNbaAwdRetrieveOptionsVO.requestTransactionAsChild();
			aNbaAwdRetrieveOptionsVO.setLockTransaction();
			expandedWork = retrieveWorkItem(userVo, aNbaAwdRetrieveOptionsVO);
			long jointInsRprs = NbaOliConstants.OLI_REL_JOINTINSURED;
			updateInsuredDOBForRequirement(expandedWork, correctedDOB, jointInsRprs);
			expandedWork.setNbaUserVO(user);
			String comments= "DOB for Joint Insured changed to " + correctedDOB;
			addComments(expandedWork,comments);
			currentBP.callService("NbaUpdateWorkBP", expandedWork);
			currentBP.callBusinessService("NbaUnlockWorkBP", expandedWork);
			try {
				unlockWorkitemAndSessionCleanUP(expandedWork);
			} catch (NbaBaseException e) {
				getLogger().logDebug("RoutePoller in Unlock Catch - for CRDA = "+crda +" updated DOB:"+correctedDOB);
				e.printStackTrace();
			}
			NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(polnumber, JOB_JOINT_INS_DOB_UPDATE, correctedDOB, CASE_PROCESSED);
		}
		//Ends NBLXA-2162

		//Begin NBLXA-2472
		public void changeUWCM(String polNumber,String newValue,NbaUserVO userVo) throws NbaBaseException {
			NbaReassignmentQueuesVO assignVO = new NbaReassignmentQueuesVO();
			setWork(getParentWorkItem(polNumber, userVo));
			assignVO.setNbaTXLife(doHoldingInquiry());
			NbaDst parentWI = getWork();
			parentWI.setNbaUserVO(userVo);
			addComments(parentWI, "Reassingment completed.");
			assignVO.setNbaDst(parentWI);  //ALS3222
			assignVO.setNbaUserVO(userVo);
			String[] splitArray = newValue.split(",");
			String uwvalue = "";
			String cmvalue = "";
			String[] uw = splitArray[0].split("=");
			if(! NbaConstants.EMPTY_LOB_STR.equals(uw[1])){
				uwvalue = uw[1];
			}
			String[] cm = splitArray[1].split("=");
			if(! NbaConstants.EMPTY_LOB_STR.equals(cm[1])){
				cmvalue = cm[1];
			}
			String[] term = splitArray[2].split("=");
			String[] comp = splitArray[3].split("=");

			assignVO.setNewUWQ(uwvalue);
			assignVO.setNewCMQ(cmvalue);
			assignVO.setTermExpressTeam(Boolean.parseBoolean(term[1]));
			assignVO.setApplyToAllCompanionCases(Boolean.parseBoolean(comp[1]));
			assignVO.setFromCommit(false);
			currentBP.callBusinessService("CommitReassignmentQueuesBP", assignVO);
			NbaSystemDataDatabaseAccessor.updateReassingmentProcessingData(polNumber, JOB_UWCM_REASSIGNMENT, newValue, CASE_PROCESSED);
		}
		//End NBLXA-2472

	/**
	 * NBLXA-2511 NEW METHOD
	 * update Suitability data on case if changed to wholesale
	 * @param case DST object
	 */
	public void updateSuitability(NbaDst parent)  throws NbaBaseException {
		getLogger().logDebug("RoutePoller - Start updateSuitability");
		NbaTXLife txLife = null;
		try {
			setWork(parent);
			txLife = doHoldingInquiry();
			updateSuitabilityIndicator(txLife);
			waiveSuitabilityRequirements(txLife);
			String policyNumber = txLife.getPolicy().getPolNumber();
			String companyKey = txLife.getPolicy().getCompanyKey();
			NbaSystemDataDatabaseAccessor.updateSuitabilityData(policyNumber,companyKey);
		} catch (NbaBaseException baseException) {
			getLogger().logDebug("Issue with RoutePoller in Suitability update");
			throw baseException;
		}
	}

	/**
	 * NBLXA-2511 NEW METHOD update Suitability Indicator in TXLife
	 * @param txlife
	 */
	private void updateSuitabilityIndicator(NbaTXLife txlife) throws NbaBaseException {
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(txlife.getPolicy().getApplicationInfo());
		if (appInfoExt != null) {
			appInfoExt.setQualifyForSuitabilityInd(false);
			appInfoExt.setActionUpdate();
		}
	}

	/**
	 * NBLXA-2511 New method to waive suitability requirements which are not in cancelled waived or received status and also update requirement DST
	 * Object.
	 * @param nbaTXLife
	 */
	public void waiveSuitabilityRequirements(NbaTXLife nbaTXLife) throws NbaBaseException {
		NbaDst requirementDst = null;
		try {
			List<RequirementInfo> reqInfoList = nbaTXLife.getRequirementInfoList(NbaOliConstants.OLI_REQCODE_SUITOUTSTANDING);
			Policy policy = nbaTXLife.getPolicy();
			if (null != reqInfoList && !reqInfoList.isEmpty()) {
				for (int i = 0; i < reqInfoList.size(); i++) {
					RequirementInfo reqInfo = reqInfoList.get(i);
					if (reqInfo != null && reqInfo.getReqStatus() != NbaOliConstants.OLI_REQSTAT_CANCELLED
							&& reqInfo.getReqStatus() != NbaOliConstants.OLI_REQSTAT_WAIVED
							&& reqInfo.getReqStatus() != NbaOliConstants.OLI_REQSTAT_RECEIVED) {
						reqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_WAIVED);
						reqInfo.setStatusDate(new Date());
						requirementDst = getRequirementWI(policy.getPolNumber(), reqInfo.getRequirementInfoUniqueID());
						RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
						if (reqInfoExt != null && null != requirementDst
								&& requirementDst.getNbaLob().getReqUniqueID().equalsIgnoreCase(reqInfo.getRequirementInfoUniqueID())) {
							requirementDst.getNbaLob().setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_WAIVED));
							requirementDst.setStatus(NbaConstants.A_STATUS_REQUIREMENT_CANCELLED);
							requirementDst.getNbaLob().setRouteReason("Suit Requirement Cancelled for Wholesale");
							if (requirementDst.getNbaTransaction().isSuspended()) {
								NbaSuspendVO suspendVO = new NbaSuspendVO();
								suspendVO.setTransactionID(requirementDst.getNbaTransaction().getID());
								unsuspendVOs.add(suspendVO);
							}
							unsuspendWorkitems();
							requirementDst.getNbaTransaction().setUpdate();
							reqInfoExt.setActionUpdate();
							update(requirementDst);
							unlockWork(requirementDst);
						}
						reqInfo.setActionUpdate();
					}
				}
				setContractAccess(UPDATE);
				doContractUpdate(nbaTXLife);
			}
		} catch (NbaBaseException e1) {
			throw new NbaBaseException("This workItem cannot be processed");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != requirementDst && requirementDst.isLocked(user.getUserID())) {
				try {
					unlockWork(requirementDst);
				} catch (NbaBaseException nbe) {
					getLogger().logDebug("Issue with RoutePoller in unlocking WI while suitability update for wholesale");
					throw nbe;
				}
			}
		}
	}
}