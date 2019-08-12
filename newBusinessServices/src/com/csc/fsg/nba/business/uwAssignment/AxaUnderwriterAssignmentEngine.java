package com.csc.fsg.nba.business.uwAssignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.AxaRulesDataProcessor;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.AxaUWAssignmentEngineVO;
import com.csc.fsg.nba.vo.AxaAssignmentRulesVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.AxaUnderWriterPODVO;
import com.csc.fsg.nba.vo.AxaUnderwriterWeightRules;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CarrierAppointmentExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.workflow.NbaWorkflowDistribution;
import com.csc.fsg.nba.vo.txlife.Policy;

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
 * This class is contains logic and calculations for UW Assignment.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>CR57873</td>
 * <td>Version 1</td>
 * <td>Initial Development</td>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Analytics Full Implementation</td></tr>
 * <tr><td>SR831136 APSL4088</td><td>Descretionary</td><td>PCCM Workflow</td></tr>
 * <tr><td>APSL4662</td><td>Discretionary</td><td>Term Conversion Cases' Underwriter Assignment</td></tr>
 * </tr>
 * 
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 1.0.0
 */

public class AxaUnderwriterAssignmentEngine{
	
	private String fastInd = "0";

	private String brokerage = "NA";

	private String agentDesignation = "0";

	private NbaLogger logger = null;

	private String underWriterLOB;
	
	private boolean undqAssingmentReq =true; //NBLXA-1538
	
	private String underWriterCMLOB;
	
	private static final String VPMS_BROKERAGE_VALUE = "1";
	
	private static final String SPECIAL_CASE_DEFAULT_VALUE = "0";
	
	private static final int NO_OF_APS_DEFAULT_VALUE = 0;
	
	private static final String ANY_REAL_VALUE = "U";
	
	private boolean skipAdvanceScoreAndCount;
	
	private boolean underwriterRequired = true;
	
	private boolean casemanagerRequired = true;
	
	private String overageQueue="";//CR57873(APSL2497)
	
	private static final String PICM_RULE = "0";
	
	private static final String PICM_PRTNIGO_RULE = "1";
	
	public AxaUnderwriterAssignmentEngine(){
		
	}
	/**
	 * Underwriter Assignment process begins
	 * @param uwAssignment
	 * @throws NbaDataAccessException
	 * @throws NbaBaseException
	 * @throws Exception
	 */
	public synchronized void execute(AxaUWAssignmentEngineVO uwAssignment) throws NbaDataAccessException, NbaBaseException {
		//Assumed that null check for TXLife and NbaDst is applied before calling execute() of this class
		NbaTXLife nbaTXLife = uwAssignment.getTxLife();
		NbaDst nbaDst = uwAssignment.getNbaDst();
		NbaLob nbaLob = nbaDst.getNbaLob();
		underwriterRequired = uwAssignment.isUnderwriterRequired();
		undqAssingmentReq=uwAssignment.isUndqAssingmentReq(); //NBLXA-1538
		casemanagerRequired = uwAssignment.isCasemanagerRequired();
		boolean Reassignment = uwAssignment.isReassignment();
		boolean isUndqLOBPresent = (nbaLob.getUndwrtQueue() == null || NbaConstants.EMPTY_LOB_STR.equals(nbaLob.getUndwrtQueue())) ? false:true;
		boolean isCsmqLOBPresent = (nbaLob.getCaseManagerQueue() == null || NbaConstants.EMPTY_LOB_STR.equals(nbaLob.getCaseManagerQueue())) ? false:true;
		boolean isMLOACompanyPresent = uwAssignment.isMLOACompanyPresent();//QC15916/APSL4389
		 boolean termExpIndNotPresent=uwAssignment.isTermExpIndOff();//NBLXA186-Term Processing Automate
		writeToLogFile("Contract Number: " + nbaLob.getPolicyNumber());
		writeToLogFile("First Name: " + nbaLob.getFirstName());
		writeToLogFile("Last Name: " + nbaLob.getLastName());
		writeToLogFile("Underwriter Required: "+underwriterRequired);//APSL3099
		writeToLogFile("Casemanager Required: "+casemanagerRequired);//APSL3099
		writeToLogFile("Is Reassignment: "+Reassignment);//APSL3099
				
		if(!uwAssignment.isPaidChangeCaseManagerRequired() && !uwAssignment.isPostIssueCaseManagerRequired() && !uwAssignment.isLicmAssignmentReq()){ //APSL4088 and APSL4685, NBLXA-2328[NBLXA-2595]
			//---APSL2767---Start Block---
			// ADC cases will not have UWCM and UW assignments
			if(NbaUtils.isAdcApplication(nbaDst)){
				writeToLogFile("It is an ADC application, no need to run UW & UWCM Assignment.");
				return;
			}
			//---APSL2767---End Block---
			
			if(NbaUtils.isBlankOrNull(nbaLob.getApplicationScore())){
				setApplicationScoreLOB(nbaTXLife, nbaLob);
			}
			if (isUndqLOBPresent && isCsmqLOBPresent && !Reassignment) {
				writeToLogFile("UNDQ and CSMQ is already set and isReassignment is false");
				assignCaseTo(nbaLob, true);// QC# 9625
				writeToLogFile("Case Assigned to UW>> " + underWriterLOB);
				writeToLogFile("Case Assigned to UWCM>> " + underWriterCMLOB);
				return;
			}
			if ((isUndqLOBPresent && isCsmqLOBPresent && Reassignment)
					|| (nbaTXLife != null && NbaConstants.ARCOS_SIMULATOR.equals(nbaTXLife.getBusinessProcess()))) { // NBLXA-2085
				skipAdvanceScoreAndCount = true;
			}
			processAssignment(nbaTXLife, nbaDst, Reassignment, isUndqLOBPresent, isCsmqLOBPresent,isMLOACompanyPresent,termExpIndNotPresent);//QC15916/APSL4389 // // NBLXA186-Term Processing Automate
			
			writeToLogFile("Case Assigned to UW>> " + underWriterLOB);
			writeToLogFile("Case Assigned to UWCM>> " + underWriterCMLOB);
		} else if(uwAssignment.isPaidChangeCaseManagerRequired()) {
			processPCCMAssignment(nbaTXLife, nbaDst);			
		} else if(uwAssignment.isPostIssueCaseManagerRequired()){
			processPICMAssignment(nbaTXLife, nbaDst, uwAssignment.isWIForPICMPrintNigo());	// APSL4685		
		} else if (uwAssignment.isLicmAssignmentReq()) { //NBLXA-2328[NBLXA-2595]
			processLICMAssignment(nbaTXLife, nbaDst, uwAssignment.getGroupAssigned());
		}

	}
	
	/**
	 * Gets UWCM corresponding to an UW.
	 * @param String underwriter
	 * @return String Casemanager
	 * @throws NbaBaseException
	 */
	protected String getCaseManagerQueue(String underwriter) throws NbaBaseException {
		return AxaRulesDataProcessor.getUWCMQueue(underwriter);
	}
	
	/**
	 * Assignment process starts
	 * @param nbaTXLife
	 * @param nbaLob
	 * @throws NbaBaseException
	 */
	protected void processAssignment(NbaTXLife nbaTXLife, NbaDst nbaDst, boolean Reassignment, boolean isUndqLOBPresent, boolean isCsmqLOBPresent, boolean isMLOACompanyPresent,boolean termExpIndNotPresent) throws NbaBaseException {
		NbaLob nbaLob = nbaDst.getNbaLob();
		AxaAssignmentRulesVO assignmentRulesVO = constructAssignRulesVO(nbaTXLife, nbaDst,isMLOACompanyPresent);//QC15916/APSL4389
		if (isUndqLOBPresent && !isCsmqLOBPresent && !Reassignment && casemanagerRequired) {
			//UNDQ is already set, need to set CSMQ
			AxaUnderWriterPODVO aUWQueueVO = new AxaUnderWriterPODVO();
			aUWQueueVO.setUwQueue(nbaLob.getUndwrtQueue());//CR57873(APSL2514)
			aUWQueueVO = setUWCMQValue(aUWQueueVO, assignmentRulesVO, nbaLob);
			underWriterCMLOB = aUWQueueVO.getUwcmQueue();
			underWriterLOB = nbaLob.getUndwrtQueue();
			nbaLob.setCaseManagerQueue(underWriterCMLOB);
			assignCaseTo(nbaLob, true);
			writeToLogFile("underWriterLOB is already present("+underWriterLOB+") setting underWriterCMLOB as: "+underWriterCMLOB);
			return;
		}
		if (Reassignment && casemanagerRequired && !underwriterRequired) { //APSL4983
			AxaUnderWriterPODVO aUWQueueVO = new AxaUnderWriterPODVO();
			aUWQueueVO.setUwQueue(nbaLob.getUndwrtQueue());//CR57873(APSL2514)
			aUWQueueVO = setUWCMQValue(aUWQueueVO, assignmentRulesVO, nbaLob);
			underWriterCMLOB = aUWQueueVO.getUwcmQueue();
			underWriterLOB = nbaLob.getUndwrtQueue();
			nbaLob.setCaseManagerQueue(underWriterCMLOB);
			assignCaseTo(nbaLob, true);
			writeToLogFile("Reassignment case, setting underWriterCMLOB as: "+underWriterCMLOB);
			return;
		}
		List uwPODList = AxaRulesDataProcessor.getUWQueuesForPOD(assignmentRulesVO);
		if (uwPODList != null) {
			if (uwPODList.size() != 0) {
				if(Reassignment && isOldUWPresentInTheList(uwPODList, nbaLob.getUndwrtQueue())){
					writeToLogFile("Is a Reassignment case and underwriter "+nbaLob.getUndwrtQueue()+" is among the list of qualified UWs");
					return;
				}
				int assignType = ((AxaUnderWriterPODVO) uwPODList.get(0)).getAssignType();
				switch (assignType) {
				case NbaConstants.UW_EQUITABLE_ASSIGNMENT:
					processEquitableAssignment(nbaTXLife.isRetail(), uwPODList, nbaLob, assignmentRulesVO);
					break;
				case NbaConstants.UW_WEIGHTED_SCORE_ASSIGNMENT:
					processWeightedAssignment(nbaTXLife, nbaLob, uwPODList, assignmentRulesVO);
					break;
				default:
					processInvalidAssignType();
				}
			} else {
				assignDefaultQueues(nbaTXLife.isRetail(), assignmentRulesVO, nbaLob);
			}
			
			if(!NbaUtils.isBlankOrNull(underWriterLOB) && underwriterRequired  && undqAssingmentReq){ //NBLXA-1538
				nbaLob.setUndwrtQueue(underWriterLOB);
			}
			if(!NbaUtils.isBlankOrNull(underWriterCMLOB)){
				nbaLob.setCaseManagerQueue(underWriterCMLOB);
			}
			//APSL4862   //Modified for NBLXA186-Term Processing Automate
			if(NbaUtils.isTermExpCase(nbaTXLife) && !termExpIndNotPresent && (nbaTXLife.getPolicy()!=null && !(NbaConstants.COMPANY_MLOA.equalsIgnoreCase(nbaTXLife.getPolicy().getCarrierCode())))){ //Added check of mloa company for NBLXA1456/QC#19179
				nbaLob.setDisplayIconLob("1");
			}
		} else {
			processInvalidAssignType();
		}
	}
	
	/**
	 * Defines the behaviour, in case, AssignType of a underwriter queue is other than 1 or 2 (NBARULES.UW_POD.ASSIGN_TYPE)
	 * @return void
	 * @throws NbaBaseException
	 */
	protected void processInvalidAssignType() throws NbaBaseException {
		//hard stop the calling poller
		writeToLogFile("Invalid assign type found");
		NbaBaseException nbe = new NbaBaseException("Invalid assign type found");
		nbe.forceFatalExceptionType();
		throw nbe;
	}
	
	/**
	 * Equitable Assignment Logic
	 * 
	 * @param isRetail(case
	 *            is retail or wholesale)
	 * @param NbaLob
	 * @param List
	 *            of NbaUnderwriterPODVO objects
	 * @return void
	 * @throws NbaBaseException
	 * @throws Exception
	 */
	protected void processEquitableAssignment(boolean isRetail, List uwPODList, NbaLob nbaLob, AxaAssignmentRulesVO assignmentRulesVO) throws NbaBaseException {
		List underWriters = getListOfAvailableUnderwriters(uwPODList);
		String uwQueue = determineEquitableQueue(underWriters, nbaLob, NbaLob.A_LOB_ORIGINAL_UW_WB_QUEUE);
		if (!NbaUtils.isBlankOrNull(uwQueue)) {
			AxaUnderWriterPODVO uwQueueVO = getUnderwriterPod(uwPODList, uwQueue);
			if(casemanagerRequired){
				uwQueueVO = setUWCMQValue(uwQueueVO, assignmentRulesVO, nbaLob);
			}
			if (uwQueueVO != null) {
				assignCaseTo(uwQueueVO, 0, false);
			} else {
				throw new NbaBaseException("Underwriter " + uwQueue + " is not among the " + underWriters.toString());
			}
		} else {
			assignDefaultQueues(isRetail, assignmentRulesVO, nbaLob);
		}
	}
	/**
	 * Weighted Assignment Logic
	 * 
	 * @param NbaTXLife
	 * @param NbaLob
	 * @param List
	 *            of NbaUnderwriterPODVO objects
	 * @return void
	 * @throws NbaDataAccessException
	 * @throws NbaBaseException
	 */
	protected void processWeightedAssignment(NbaTXLife nbaTXLife, NbaLob nbaLob, List uwPODList, AxaAssignmentRulesVO assignmentRulesVO) throws NbaDataAccessException, NbaBaseException {
		double caseFactor = nbaLob.getApplicationScore();
		List availableUWriters = getScoreBasedAvailableUWs(uwPODList);//APSL3624
		if (availableUWriters.size() > 0) {
			availableUWriters = getPresenceBasedAvailableUWs(availableUWriters);//APSL3624
			availableUWriters = getUWWithLowestScore(availableUWriters);
			if (availableUWriters.size() == 1) {
				AxaUnderWriterPODVO aUnderWriter = ((AxaUnderWriterPODVO) availableUWriters.get(0));
				if(casemanagerRequired){
					aUnderWriter = setUWCMQValue(((AxaUnderWriterPODVO) availableUWriters.get(0)), assignmentRulesVO, nbaLob);
				}
				assignCaseTo(aUnderWriter, caseFactor, true);
			} else if (availableUWriters.size() > 1) {
				availableUWriters = getUWWithLowestCount(availableUWriters);
				if (availableUWriters.size() >= 1) {//round robin assignment
					AxaUnderWriterPODVO aUnderWriter = ((AxaUnderWriterPODVO) availableUWriters.get(0));
					if(casemanagerRequired){
						aUnderWriter = setUWCMQValue(((AxaUnderWriterPODVO) availableUWriters.get(0)), assignmentRulesVO, nbaLob);
					}
					assignCaseTo(aUnderWriter, caseFactor, true);
				} else {
					assignDefaultQueues(nbaTXLife.isRetail(), assignmentRulesVO, nbaLob);
				}
			} else {
				assignDefaultQueues(nbaTXLife.isRetail(), assignmentRulesVO, nbaLob);
			}
		} else {
			overageQueue = ((AxaUnderWriterPODVO)uwPODList.get(0)).getOverageQueue();//CR57873(APSL2497)//APSL3624 - moved to else part
			assignDefaultQueues(nbaTXLife.isRetail(), assignmentRulesVO, nbaLob);
		}
	}
	/**
	 * @param underwriterPODVO
	 * @param assignmentRulesVO
	 * @throws NbaBaseException, nbadata
	 */
	protected AxaUnderWriterPODVO setUWCMQValue(AxaUnderWriterPODVO underwriterPODVO, AxaAssignmentRulesVO assignmentRulesVO, NbaLob nbaLob) throws NbaBaseException, NbaDataAccessException
	{
		List uwcmQueues = AxaRulesDataProcessor.getUWCMQueue(assignmentRulesVO, underwriterPODVO.getUwQueue());
		int size = uwcmQueues.size();
		if(size == 1){
			underwriterPODVO.setUwcmQueue((String) uwcmQueues.get(0)); 
		}else if(size > 1){
			String uwcm = determineEquitableQueue(uwcmQueues, nbaLob, NbaLob.A_LOB_CM_QUEUE);
			underwriterPODVO.setUwcmQueue(uwcm);
		}else{
			if (assignmentRulesVO.isRetail()) {
				underwriterPODVO.setUwcmQueue(NbaConstants.DEFAULT_UWCSMR_RETAIL);
			} else {
				underwriterPODVO.setUwcmQueue(NbaConstants.DEFAULT_UWCSMR_WHOLESALE);
			}
		}
		return underwriterPODVO;
		
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
	protected String determineEquitableQueue(List queues, NbaLob nbaLob, String lobName) throws NbaBaseException {
		String equitableQueue = null;
		String queue = null;
		int workCount = 0;
		int equitableCount = 0;
		int count = queues.size();		
		NbaWorkflowDistribution distribution = new NbaWorkflowDistribution(nbaLob);
		for (int i = 0; i < count; i++) {
			queue = (String) queues.get(i);
			workCount = distribution.getAssignedWorkCountByQueue(queue, lobName);
			writeToLogFile("equitable queue check: " + queue + " - " + workCount);
			if (i==0 || workCount < equitableCount) {//assigns first queue Or a queue having lesser work count to equitableQueue
				equitableCount = workCount;
				equitableQueue = queue;
			}
		}
		writeToLogFile("equitable queue: " + equitableQueue);
		return equitableQueue;

	}
	
	/**
	 * Takes a list containing NbaUnderWriterPODVO objects and returns a List containing UwQueue.
	 * 
	 * @param list
	 *            of queues
	 * @return List
	 * @throws NbaDataAccessException
	 */
	protected List getListOfAvailableUnderwriters(List uwPod) throws NbaDataAccessException {
		List underWriters = new ArrayList();
		for (int i = 0; i < uwPod.size(); i++) {
			//CR57873 (APSL2430) Begin
			String uw = ((AxaUnderWriterPODVO) uwPod.get(i)).getUwQueue();
			if (!isUWOnVacation(uw)) {
				underWriters.add(uw);
			}
			//CR57873 (APSL2430) End
		}
		return underWriters;
	}
	
	/**
	 * Returns NbaUnderWriterPODVO object for a given under writer queue name.
	 * 
	 * @param List
	 *            of NbaUnderWriterPODVO objects
	 * @param uwQ
	 *            name
	 * @return NbaUnderWriterPODVO object
	 */
	protected AxaUnderWriterPODVO getUnderwriterPod(List uwPOD, String uwQueue) {
		for (int i = 0; i < uwPOD.size(); i++) {
			AxaUnderWriterPODVO aUW = (AxaUnderWriterPODVO) uwPOD.get(i);
			if (aUW.getUwQueue().equalsIgnoreCase(uwQueue)) {
				return aUW;
			}
		}
		return null;
	}
	
	/**
	 * Finds number of Lives associated with a case.
	 * 
	 * @param NbaTXLife
	 * @return int
	 */
	protected static int getAssociatedLives(NbaTXLife txLife) {
		int numberOfLives = 1;
		String covIns = txLife.getPartyId(NbaOliConstants.OLI_REL_COVINSURED);
		String jointIns = txLife.getPartyId(NbaOliConstants.OLI_REL_JOINTINSURED);
		if (covIns != null && covIns.length() > 0) {
			numberOfLives++;
		} else if (jointIns != null && jointIns.length() > 0) {
			numberOfLives++;
		}
		return numberOfLives;
	}
	
	/**
	 * Sets final UWQueue and UWCMQueue to NbaUnderWriterPODVO object and updates current count and score to NBARULES schema
	 * 
	 * @param NbaUnderWriterPODVO
	 *            object
	 * @param factor
	 *            of the case
	 * @param WeightedAssignment
	 * @return void
	 * @throws NbaDataAccessException
	 */
	protected void assignCaseTo(AxaUnderWriterPODVO uwpod, double factor, boolean WeightedAssignment) throws NbaDataAccessException {
		underWriterLOB = uwpod.getUwQueue();
		underWriterCMLOB = uwpod.getUwcmQueue();
		if(WeightedAssignment && !skipAdvanceScoreAndCount && underwriterRequired) {
			//database is to be updated with new score and count, only for weighted assignment scheme, if underwriter is required on the case.
			//Since factor is used only for Weighted Assignment, 
			//therefore value of factor will be 0, if assignCaseTo is called from Equitable Distribution
			advanceScoreAndCount(uwpod, factor);
			AxaRulesDataProcessor.updateScoreAndCount(uwpod);
	    }
	}
	
	/**
	 * Sets predecided UWQueue and updates current count and score to NBARULES schema.
	 * Is used for companion cases and predecided UWQueue during indexing. 
	 * @param NbaLob
	 *            object
	 * @param WeightedAssignment
	 * @return void
	 * @throws NbaDataAccessException
	 * @throws NbaBaseException
	 */
	protected void assignCaseTo(NbaLob nbaLob, boolean WeightedAssignment) throws NbaDataAccessException, NbaBaseException {
		List aUnderwriter=new ArrayList();
		aUnderwriter.add(nbaLob.getUndwrtQueue());
		List aUnderwriterVO=AxaRulesDataProcessor.getUWQueues(aUnderwriter);
		if(!NbaUtils.isBlankOrNull(aUnderwriterVO)){
			AxaUnderWriterPODVO uwpod=(AxaUnderWriterPODVO)aUnderwriterVO.get(0);
			uwpod.setUwcmQueue(nbaLob.getCaseManagerQueue());
			assignCaseTo(uwpod, nbaLob.getApplicationScore(), WeightedAssignment);
		}else{
			writeToLogFile("Underwriter "+nbaLob.getUndwrtQueue()+" not found in NBARULES.UW_WORKLOAD");
		}
	}
	
	/**
	 * Increases the current count(by 1) and current score(by case factor) for a underwriter
	 * 
	 * @param NbaUnderWriterPODVO
	 *            object
	 * @param factor
	 *            of the case
	 * @return void
	 */
	protected void advanceScoreAndCount(AxaUnderWriterPODVO uwpod, double factor) {
		uwpod.setCurrentCount(uwpod.getCurrentCount() + 1);
		uwpod.setCurrentScore(uwpod.getCurrentScore() + factor);
		uwpod.setYtdCount(uwpod.getYtdCount() + 1);
		uwpod.setYtdScore(uwpod.getYtdScore() + factor);
	}
	
	/**
	 * Finds underwriter(s) with minimum score
	 * 
	 * @param List
	 *            containing NbaUnderWriterPODVO objects
	 * @return List containing NbaUnderWriterPODVO objects
	 */
	protected List getUWWithLowestScore(List availableUWriters) {
		List lowestScoresUWs = new ArrayList();
		double minValue = 9999;//APSL3624
		for (int x = 0; x < availableUWriters.size(); x++) {
			if (((AxaUnderWriterPODVO) availableUWriters.get(x)).getCurrentScore() < minValue) {
				minValue = ((AxaUnderWriterPODVO) availableUWriters.get(x)).getCurrentScore();
			}
		}
		for (int m = 0; m < availableUWriters.size(); m++) {
			if (((AxaUnderWriterPODVO) availableUWriters.get(m)).getCurrentScore() == minValue) {
				lowestScoresUWs.add(availableUWriters.get(m));
			}
		}
		return lowestScoresUWs;
	}
	
	/**
	 * Finds underwriter(s) with minimum case count
	 * 
	 * @param List
	 *            containing NbaUnderWriterPODVO objects
	 * @return List containing NbaUnderWriterPODVO objects
	 */
	protected List getUWWithLowestCount(List availableUWriters) {
		List lowestCountUWs = new ArrayList();
		double minValue = 999;
		for (int x = 0; x < availableUWriters.size(); x++) {
			if (((AxaUnderWriterPODVO) availableUWriters.get(x)).getCurrentCount() < minValue) {
				minValue = ((AxaUnderWriterPODVO) availableUWriters.get(x)).getCurrentCount();
			}
		}
		for (int m = 0; m < availableUWriters.size(); m++) {
			if (((AxaUnderWriterPODVO) availableUWriters.get(m)).getCurrentCount() == minValue) {
				lowestCountUWs.add(availableUWriters.get(m));
			}
		}
		return lowestCountUWs;
	}
	
	/**
	 * Finds underwriter(s) whose factor limits are not reached
	 * 
	 * @param List
	 *            containing NbaUnderWriterPODVO objects
	 * @return List containing NbaUnderWriterPODVO objects
	 * @throws NbaDataAccessException
	 */
	//APSL3624 - Code refactored
	protected List getScoreBasedAvailableUWs(List uwPODList) throws NbaDataAccessException {
		List availableUWriters = new ArrayList();
		for (int i = 0; i < uwPODList.size(); i++) {
			AxaUnderWriterPODVO aUWQueue = (AxaUnderWriterPODVO) uwPODList.get(i);
			writeToLogFile("Current score= "+aUWQueue.getCurrentScore());
			writeToLogFile("Current Count= "+aUWQueue.getCurrentCount());
			if (aUWQueue.getCurrentScore() < aUWQueue.getMaxScore()) {
				availableUWriters.add(aUWQueue);
				writeToLogFile("Underwriter Available(score based) :"+aUWQueue.getUwQueue());
			}
		}

		return availableUWriters;
	}
	
	/**
	 * Finds underwriter(s) who are not in vacation
	 * 
	 * @param List
	 *            containing NbaUnderWriterPODVO objects
	 * @return List containing NbaUnderWriterPODVO objects
	 * @throws NbaDataAccessException
	 */
	//APSL3624 - Code refactored
	protected List getPresenceBasedAvailableUWs(List uwPODList) throws NbaDataAccessException {
		List availableUWriters = new ArrayList();
		for (int i = 0; i < uwPODList.size(); i++) {
			AxaUnderWriterPODVO aUWQueue = (AxaUnderWriterPODVO) uwPODList.get(i);
			if (!isUWOnVacation(aUWQueue.getUwQueue())) {
				availableUWriters.add(aUWQueue);
				writeToLogFile("Underwriter Available(presence based) :"+aUWQueue.getUwQueue());
			}else{
				writeToLogFile("Underwriter NOT Available(presence based) :"+aUWQueue.getUwQueue());
			}
		}

		return availableUWriters;
	}
	
	/**
	 * Finds if an underwriter is on vacation or not
	 * 
	 * @param Underwriter
	 *            ID
	 * @return true, if on vacation; false, otherwise.
	 */
	protected boolean isUWOnVacation(String queue) throws NbaDataAccessException {
		NbaTableAccessor nta = new NbaTableAccessor();
		String userId = nta.getUserIdForQueue(queue);
		if (NbaConstants.USER_STAT_AVAILABLE.equalsIgnoreCase(nta.getUserStatus(userId))) {
			writeToLogFile("User id for Queue "+queue+" is "+userId+" and is available");
			return false;
		}
		writeToLogFile("User id for Queue "+queue+" is "+userId+" and is not available");
		return true;
	}
	
	/**
	 * Gets value of fast indicator and brokerage by calling VPMS, and sets the values to class variables.
	 * 
	 * @param NbaTXLife
	 * @param nbaLob
	 * @return void
	 * @throws NbaBaseException
	 */
	protected void getVPMSValues(NbaTXLife nbaTXLife, NbaLob nbaLob) throws NbaBaseException {
		NbaVpmsAdaptor adapter = null;
		NbaOinkDataAccess oinkData;
		try {
			oinkData = new NbaOinkDataAccess(nbaLob);
			oinkData.setContractSource(nbaTXLife);
			adapter = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.AUTO_PROCESS_STATUS);
			adapter.setVpmsEntryPoint(NbaVpmsAdaptor.EP_UW_ASSIGNMENT_VALUES);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(adapter.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				List values = vpmsResultsData.getResultsData();
				if (values.size() > 0) {
					fastInd = (String) values.get(0);
				}
				if (values.size() > 1) {
					if (VPMS_BROKERAGE_VALUE.equals(values.get(1))) {
						brokerage = NbaConstants.ASH_BROKERAGE;
					}
				}
				if (values.size() > 2) {
					agentDesignation = (String) values.get(2);
				}
				writeToLogFile("fastInd >>> " + fastInd + "\n brokerage >>> " + brokerage + "\n agentDesignation >>> " + agentDesignation);
			}
		} catch (Exception e) {
			writeToLogFile(this.getClass().getName() + "VPMS ERROR" + e.getMessage());
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

	/**
	 * Creates and returns NbaAssignmentRulesVO by setting required values in value object
	 * 
	 * @param NbaTXLife
	 * @param NbaLob
	 * @return NbaAssignmentRulesVO object.
	 */
	protected AxaAssignmentRulesVO constructAssignRulesVO(NbaTXLife nbaTXLife, NbaDst nbaDst, boolean isMLOACompanyPresent) throws NbaBaseException {
		NbaLob nbaLob = nbaDst.getNbaLob();
		AxaAssignmentRulesVO rulesvo = new AxaAssignmentRulesVO();
		rulesvo.setAppType(String.valueOf(nbaLob.getApplicationType()));
		if (null != nbaDst){
		rulesvo.setProdCode(nbaDst.getNbaLob().getPlan());//NBLXA-1653
		}
		//Begin QC15916/APSL4389
		if(isMLOACompanyPresent){
			writeToLogFile("MLOA company present:"+isMLOACompanyPresent);
			rulesvo.setCompanyCode(NbaConstants.COMPANY_MLOA);
		}else{
			rulesvo.setCompanyCode(nbaLob.getCompany());
		}
		//End QC15916/APSL4389
		if (nbaTXLife != null) {
			getVPMSValues(nbaTXLife, nbaLob);
			ApplicationInfo appInfo = nbaTXLife.getPolicy().getApplicationInfo();
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			LifeParticipant lifePart = nbaTXLife.getPrimaryInuredLifeParticipant();
			if (lifePart != null && lifePart.getIssueAge() >= 0) {
				rulesvo.setAge(lifePart.getIssueAge());
			} else {
				rulesvo.setAge(0);
			}
			if (appInfoExt != null) {
				if (appInfoExt.hasSpecialCase()) {
					rulesvo.setSpecialCase(String.valueOf(appInfoExt.getSpecialCase()));
				} else {
					rulesvo.setSpecialCase(SPECIAL_CASE_DEFAULT_VALUE);
				}
			} else {
				rulesvo.setSpecialCase(SPECIAL_CASE_DEFAULT_VALUE);
			}
			rulesvo.setFaceAmount(nbaTXLife.getLife().getFaceAmt());
			Relation relation = nbaTXLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
			Relation processingRelation = nbaTXLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PROCESSINGFIRM);// APSL3447

			// APSL3447 starts
			if (processingRelation != null) {
				NbaParty processingParty = nbaTXLife.getParty(processingRelation.getRelatedObjectID());
				CarrierAppointment carrierAppointment = processingParty.getParty().getProducer().getCarrierAppointmentAt(0);
				CarrierAppointmentExtension extension = NbaUtils.getFirstCarrierAppointmentExtension(carrierAppointment);
				if (extension != null) {
					rulesvo.setBgaTeam(extension.getBGAUWTeam());
				}
			}
			// APSL3447 ends
			else if (relation != null) {
				NbaParty bgaParty = nbaTXLife.getParty(relation.getRelatedObjectID());
				CarrierAppointment carrierAppointment = bgaParty.getParty().getProducer().getCarrierAppointmentAt(0);
				CarrierAppointmentExtension extension = NbaUtils.getFirstCarrierAppointmentExtension(carrierAppointment);
				if (extension != null) {
					rulesvo.setBgaTeam(extension.getBGAUWTeam());
				}
				rulesvo.setProducer(carrierAppointment.getCompanyProducerID()); // APSL3650
			}

			// APSL4104 starts
			if (relation != null) {

				NbaParty bgaParty = nbaTXLife.getParty(relation.getRelatedObjectID());
				CarrierAppointment carrierAppointment = bgaParty.getParty().getProducer().getCarrierAppointmentAt(0);
				rulesvo.setProducer(carrierAppointment.getCompanyProducerID());

			}

			// APSL4104 ends

			if (nbaTXLife.isRetail()) {
				setRetailValues(rulesvo, nbaTXLife, nbaDst);
			} else {
				// NBLXA-1466 Begin - set Producer as Contracting firm if BGA is Farmers
				NbaParty contractingParty = NbaUtils.getFirmPartyByRelationRoleCode(nbaTXLife, NbaOliConstants.OLI_REL_CONTRACTINGFIRM);
				String contractingFirmId = NbaUtils.getFirmAccountId(contractingParty);
				if (contractingFirmId != null && Arrays.asList(NbaConstants.EXCEPTION_BGA_CONTRACTINGFIRM).contains(contractingFirmId)) {
					rulesvo.setProducer(contractingFirmId);
				}
				//NBLXA-1466 End
				setWholesaleValues(rulesvo, nbaTXLife, nbaDst);
			}
			
			
			//Start:APSL4862
			//UW Reconfiguration Term Assignment
			Policy policy = nbaTXLife.getPolicy();
			if(NbaUtils.isTermLife(policy) && !(NbaConstants.COMPANY_MLOA.equalsIgnoreCase(rulesvo.getCompanyCode()))){//Modified for NBLXA1388/QC18939
				rulesvo.setTermCaseInd("1");
			} else {
				rulesvo.setTermCaseInd("0");
			}
			//End:APSL4862
			
			// APSL 4662:Start
			if (rulesvo.getAppType().equals("4") || rulesvo.getAppType().equals("20")) {
				List tConvList = nbaTXLife.getTermConversionHoldingList();
				Iterator itr = tConvList.iterator();
				boolean unWritingReq = false;
				while (itr.hasNext()) {
					Holding tConvHol = (Holding) itr.next();
					Policy pol = tConvHol.getPolicy();
					if (pol != null) {
						PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(pol);
						if (polExt != null) {

							if (polExt.getTermConvTableRatingInd() || polExt.getTermConvBenefitAddUWReqrdInd() || polExt.getTermConvFlatExtraInd()
									|| polExt.getTermConvReinsuranceInd() || polExt.getRemoveTermConvExclRdrInd()
									|| polExt.getTermConvRatingReductionInd() || polExt.getTermConvIncreaseUWReqrdInd()
									|| polExt.getTermConvRiderAddUWReqrdInd()) {
								unWritingReq = true;
								break;
							}
						}
					}
				}

				if (unWritingReq) {

					rulesvo.setTermConvType("0");
				} else {

					rulesvo.setTermConvType("1");
				} // APSL 4662 :End

			} else {
				rulesvo.setTermConvType("0");
			}
		}

		return rulesvo;

	}

	/**
	 * Sets Retail specific values.
	 * @param rulesvo
	 * @param nbaTXLife
	 * @param nbaLob
	 * @throws NbaBaseException 
	 */
	protected void setRetailValues(AxaAssignmentRulesVO rulesvo, NbaTXLife nbaTXLife, NbaDst nbaDst) throws NbaBaseException {
		NbaLob nbaLob = nbaDst.getNbaLob();
		rulesvo.setRetail(true);
		rulesvo.setAgentDesigInd(agentDesignation);
		rulesvo.setAsuCode(NbaUtils.getWritingAgencyId(nbaTXLife));
		rulesvo.setProducerDesignation(nbaLob.getAgentDesignation());
		rulesvo.setFastInd(fastInd);
		rulesvo.setBga(ANY_REAL_VALUE);
		rulesvo.setBgaAgent(ANY_REAL_VALUE);
		//Relation relation = nbaTXLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT); // APSL3188
		Relation relation = nbaTXLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);  // APSL3188
		if (relation != null) {
			NbaParty bgaParty = nbaTXLife.getParty(relation.getRelatedObjectID());
			// Begin NBLXA-2292
			if (bgaParty.getPerson() != null) {
				rulesvo.setAgentLastName(bgaParty.getPerson().getLastName());
			}
			// End NBLXA-2292
			CarrierAppointment carrierAppointment = bgaParty.getParty().getProducer().getCarrierAppointmentAt(0);
			if (carrierAppointment != null) {
				rulesvo.setProducer(carrierAppointment.getCompanyProducerID());
			}
		}
		ApplicationInfo appInfo = nbaTXLife.getPolicy().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		String iup = appInfoExt.getInternationalUWProgInd() ? "1" : "0";// CR57873(APSL2429), APSL2724
		rulesvo.setIup(iup);// CR57873(APSL2429)		
		// APSL4056 - UW Admin additional SR to support APS for Retail 
		rulesvo.setAps(NO_OF_APS_DEFAULT_VALUE);//setting default value for APS
		List allSources = nbaDst.getNbaSources();
		for (int i = 0; i < allSources.size(); i++) {
			NbaSource aSource = (NbaSource) allSources.get(i);
			if (aSource.isMiscMail() || aSource.isProviderResult()) {
				if (aSource.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_PHYSSTMT) {
					rulesvo.setAps(1);
					break;
				}
			}
		}
		if (rulesvo.getAps() != 1) {
			NbaTransaction nbaTransaction = null;
			List transactions = nbaDst.getNbaTransactions();
			for (int t = 0; t < transactions.size(); t++) {
				nbaTransaction = (NbaTransaction) transactions.get(t);
				NbaLob tranLOB = nbaTransaction.getNbaLob();
				if (tranLOB != null && (tranLOB.getReqType() == NbaOliConstants.OLI_REQCODE_PHYSSTMT)) {
					rulesvo.setAps(1);
					break;
				}
			}
		}		
		
		//Starts SR564247(APSL2525)
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy());
		boolean predictiveInd = policyExtension.getPredictiveInd();
		if(predictiveInd){
			rulesvo.setPredictiveInd(String.valueOf(NbaConstants.TRUE));
		}else{
			rulesvo.setPredictiveInd(String.valueOf(NbaConstants.FALSE));
	}
		//End SR564247(APSL2525)
	}
	
	/**
	 * Sets Wholesale specific values.
	 * @param rulesvo
	 * @param nbaTXLife
	 * @param nbaLob
	 * @throws NbaBaseException
	 */
	protected void setWholesaleValues(AxaAssignmentRulesVO rulesvo, NbaTXLife nbaTXLife, NbaDst nbaDst) throws NbaBaseException {
		rulesvo.setRetail(false);
		rulesvo.setProdType(nbaDst.getNbaLob().getProductTypSubtyp());
		rulesvo.setBrokerage(brokerage);

		rulesvo.setAps(NO_OF_APS_DEFAULT_VALUE);//setting default value for APS
		List allSources = nbaDst.getNbaSources();
		for (int i = 0; i < allSources.size(); i++) {
			NbaSource aSource = (NbaSource) allSources.get(i);
			if (aSource.isMiscMail() || aSource.isProviderResult()) {
				if (aSource.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_PHYSSTMT) {
					rulesvo.setAps(1);
					break;
				}
			}
		}
		if (rulesvo.getAps() != 1) {
			NbaTransaction nbaTransaction = null;
			List transactions = nbaDst.getNbaTransactions();
			for (int t = 0; t < transactions.size(); t++) {
				nbaTransaction = (NbaTransaction) transactions.get(t);
				NbaLob tranLOB = nbaTransaction.getNbaLob();
				if (tranLOB != null && (tranLOB.getReqType() == NbaOliConstants.OLI_REQCODE_PHYSSTMT)) {
					rulesvo.setAps(1);
					break;
				}
			}
		}		
		//Starts SR564247(APSL2525)
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy());
		boolean predictiveInd = policyExtension.getPredictiveInd();
		if(predictiveInd){
			rulesvo.setPredictiveInd(String.valueOf(NbaConstants.TRUE));
		}else{
			rulesvo.setPredictiveInd(String.valueOf(NbaConstants.FALSE));
		}
		//End SR564247(APSL2525)
		getRVPandAccIdforWholesale(rulesvo,nbaDst.getNbaLob(), nbaTXLife); //APSL4839		
	}
	
	/**
	 * Sets underwriter and underwritercasemanager queues for default assignment to NbaRulesAssignToUWAndUWCMVO object.
	 * 
	 * @param isRetail(case
	 *            is retail or wholesale)
	 * @return void
	 * @throws NbaBaseException
	 * @throws NbaDataAccessException
	 */
	protected void assignDefaultQueues(boolean isRetail, AxaAssignmentRulesVO assignmentRulesVO, NbaLob nbaLob) throws NbaDataAccessException, NbaBaseException {
		if(NbaUtils.isBlankOrNull(overageQueue)){//CR57873(APSL2497)
			if (isRetail) {
				underWriterLOB = NbaConstants.DEFAULT_UW_RETAIL;
			} else {
				underWriterLOB = NbaConstants.DEFAULT_UW_WHOLESALE;
			}
		}else{
			underWriterLOB = overageQueue;//CR57873(APSL2497)
		}
		if(casemanagerRequired){
			AxaUnderWriterPODVO aUWQueueVO = new AxaUnderWriterPODVO();
			aUWQueueVO.setUwQueue(underWriterLOB);//CR57873(APSL2514)
			aUWQueueVO = setUWCMQValue(aUWQueueVO, assignmentRulesVO, nbaLob);
			underWriterCMLOB = aUWQueueVO.getUwcmQueue();
		}
	}
	
	/**
	 * Sets underwriter and underwritercasemanager queues for default assignment to NbaRulesAssignToUWAndUWCMVO object.
	 * 
	 * @param isRetail(case
	 *            is retail or wholesale)
	 * @return void
	 */
	protected void writeToLogFile(String entry) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(entry);
		}
	}
	
	/**
	 * Return my NbaLogger implementation.
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
		
	protected boolean isOldUWPresentInTheList(List uwPODList, String oldUnderwriter){
		boolean present = false;
		for(int i=0; i<uwPODList.size(); i++){
			if(((AxaUnderWriterPODVO) uwPODList.get(i)).getUwQueue().equalsIgnoreCase(oldUnderwriter)){
				present = true;
			}
		}
		return present;
		
	}
	
	protected void setApplicationScoreLOB(NbaTXLife nbaTXLife, NbaLob nbaLob) throws NbaDataAccessException, NbaBaseException{
		AxaUnderwriterWeightRules uwWeightRulesVO = new AxaUnderwriterWeightRules();
		double caseFactor;
		List productCodes = new ArrayList();
		LifeParticipant lifePart = nbaTXLife.getPrimaryInuredLifeParticipant();
		if (lifePart != null) {
			uwWeightRulesVO.setAge(lifePart.getIssueAge());
		}
		//Begin QC10448/APSL2720
		if(nbaTXLife.getLife().getFaceAmt()>0){
			uwWeightRulesVO.setFaceAmount(nbaTXLife.getLife().getFaceAmt());
		}else{
			uwWeightRulesVO.setFaceAmount(0.0);
		}
		//End QC10448/APSL2720
		uwWeightRulesVO.setDistChannel(String.valueOf(nbaLob.getDistChannel()));
		uwWeightRulesVO.setAppType(nbaLob.getApplicationType());
		uwWeightRulesVO.setProdType(nbaLob.getProductTypSubtyp());
		uwWeightRulesVO.setLives(String.valueOf(getAssociatedLives(nbaTXLife)));
		uwWeightRulesVO.setAppOrigin(String.valueOf(nbaLob.getAppOriginType()));		
		//ALII1778
		/*String informalAppType = nbaLob.getInformalAppType();
		if (NbaUtils.isBlankOrNull(informalAppType)) {
			informalAppType = ANY_REAL_VALUE;
		}*/		
		uwWeightRulesVO.setInformalAppType(nbaLob.getInformalAppType());//ALII1778
		productCodes.addAll(new HashSet(NbaUtils.getRiderProductCodes(nbaTXLife)));
		uwWeightRulesVO.setProductCodes(productCodes);
		
		uwWeightRulesVO.setRiskRighterCase(nbaLob.getRiskRighterCase());//APSL5334
		uwWeightRulesVO.setRiskRighterDecision(String.valueOf(nbaLob.getCaseFinalDispstn()));//APSL5334
		caseFactor = (AxaRulesDataProcessor.getFactor(uwWeightRulesVO)).getFactor();
		nbaLob.setApplicationScore(String.valueOf(caseFactor));//set casefactor to LOB APSC
	}
	
	/**
	 * PCCM Assignment process starts
	 * @param nbaTXLife
	 * @param nbaLob
	 * @throws NbaBaseException
	 */
	//APSL4088
	protected void processPCCMAssignment(NbaTXLife nbaTXLife, NbaDst nbaDst) throws NbaBaseException {		
		AxaAssignmentRulesVO assignmentRulesVO = constructPCCMAssignRulesVO(nbaTXLife, nbaDst);		
		List pccmQueues = AxaRulesDataProcessor.getPCCMQueues(assignmentRulesVO);
		NbaLob nbaLob = nbaDst.getNbaLob();
		nbaLob.setPaidChgCMQueue(getEquitableQueue(pccmQueues, nbaLob, NbaLob.A_LOB_PAIDCHANGE_CM,false));//APSL4778
		writeToLogFile("Case Assigned to PCCM >> " + nbaLob.getPaidChgCMQueue());
	}
	
	/**
	 * PICM Assignment process starts
	 * @param nbaTXLife
	 * @param nbaLob
 	 * @param isWIForPrintNigo
	 * @throws NbaBaseException
	 */
	//APSL4685 New Method
	protected void processPICMAssignment(NbaTXLife nbaTXLife, NbaDst nbaDst, boolean isWIForPrintNigo) throws NbaBaseException {
		AxaAssignmentRulesVO assignmentRulesVO = new AxaAssignmentRulesVO();
		assignmentRulesVO.setWIForPrintNigo(isWIForPrintNigo);
		if(isWIForPrintNigo){
			assignmentRulesVO.setPrtNigo(PICM_PRTNIGO_RULE);
		}else {
			assignmentRulesVO.setPrtNigo(PICM_RULE);
		}
		List picmQueues = AxaRulesDataProcessor.getPICMQueues(assignmentRulesVO);
		NbaLob nbaLob = nbaDst.getNbaLob();
		nbaLob.setPostIssueCMQueue(getEquitableQueue(picmQueues, nbaLob, NbaLob.A_LOB_POSTISSUECM,true));//APSL4778
		writeToLogFile("Case Assigned to PICM >> " + nbaLob.getPostIssueCMQueue());
	}
	/**
	 * Creates and returns NbaAssignmentRulesVO by setting required values in value object
	 * 
	 * @param NbaTXLife
	 * @param NbaLob
	 * @return NbaAssignmentRulesVO object.
	 */
	//APSL4088
	protected AxaAssignmentRulesVO constructPCCMAssignRulesVO(NbaTXLife nbaTXLife, NbaDst nbaDst) throws NbaBaseException {
		AxaAssignmentRulesVO rulesvo = new AxaAssignmentRulesVO();
		if (nbaTXLife != null) {			
			ApplicationInfo appInfo = nbaTXLife.getPolicy().getApplicationInfo();
			
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			
			if (appInfoExt != null) {
				if (appInfoExt.hasSpecialCase()) {
					rulesvo.setSpecialCase(String.valueOf(appInfoExt.getSpecialCase()));
				} else {
					rulesvo.setSpecialCase(SPECIAL_CASE_DEFAULT_VALUE);
				}
			} else {
				rulesvo.setSpecialCase(SPECIAL_CASE_DEFAULT_VALUE);
			}			
		}

		return rulesvo;

	}

	//APSL4088 New Method
	//APSL4778 Method Renamed
	protected String getEquitableQueue(List queuesList, NbaLob nbaLob, String lobName, boolean chkAvailableQueueStat) throws NbaBaseException {
		if (queuesList.size() == 1) {
			return (String) queuesList.get(0);
		} else if (queuesList.size() == 0) {
			throw new NbaBaseException("Unable to determine a valid Queue.", NbaExceptionType.FATAL);
		}		
		//Begin APSL4778
		List availableQueues = getPresenceBasedAvailableUser(queuesList);
		if (!chkAvailableQueueStat && (availableQueues.size() == 0 || availableQueues.isEmpty())) {
			throw new NbaBaseException("Unable to determine available Queue.", NbaExceptionType.FATAL);
		}		
		return determineEquitableQueue(availableQueues, nbaLob, lobName); 
		//End APSL4778
	}
	
	/**
	 * Finds CM(s) who are not in vacation
	 * 
	 * @param List
	 *            containing CM queues
	 * @return List containing CM queues for which user not on vacation
	 * @throws NbaDataAccessException
	 */
	//APSL4778 New Method
	protected List getPresenceBasedAvailableUser(List cmList) throws NbaDataAccessException {
		List availableCM = new ArrayList();
		for (int i = 0; i < cmList.size(); i++) {			
			if (!isUWOnVacation((String)cmList.get(i))) {
				availableCM.add(cmList.get(i));
				writeToLogFile("User Available(presence based) for :"+cmList.get(i));
			}else{
				writeToLogFile("User NOT Available(presence based) for :"+cmList.get(i));
			}
		}
		return availableCM;
	}
	
	// APSL4839 New Method
	protected void getRVPandAccIdforWholesale(AxaAssignmentRulesVO assignmentRulesVO, NbaLob nbaLob, NbaTXLife nbaTXLife) {
		if (!assignmentRulesVO.isRetail()) {
			String rvpName = NbaUtils.getRVPName(nbaTXLife);
			assignmentRulesVO.setRvpName(rvpName);
			NbaParty indexedParty = NbaUtils.getIndexedFirmParty(nbaTXLife);
			String indxdFirmRegOrAccID = null;
			indxdFirmRegOrAccID = NbaUtils.getFirmAccountId(indexedParty);
			assignmentRulesVO.setAccountID(indxdFirmRegOrAccID);
			assignmentRulesVO.setSiCase(NbaUtils.isSimplifiedIssueCase(nbaTXLife));
			assignmentRulesVO.setTconvOPAI(NbaUtils.isTermConvOPAICase(nbaTXLife));
			assignmentRulesVO.setGiApp(NbaUtils.isGIApplication(nbaTXLife));//NBLXA-1632
			assignmentRulesVO.setCoilProduct(NbaUtils.isProductCodeCOIL(nbaTXLife));//NBLXA-1632,NBLXA-1801 
		}		
	}

	//New Method: NBLXA-2328[NBLXA-2595]
	protected void processLICMAssignment(NbaTXLife nbaTXLife, NbaDst nbaDst, String licmGroup) throws NbaDataAccessException, NbaBaseException {
		AxaUnderWriterPODVO axaLicmAssignVO = new AxaUnderWriterPODVO();
		axaLicmAssignVO.setAssignType(NbaConstants.ROUND_ROBIN_ASSIGNMENT);
		axaLicmAssignVO.setTeam(licmGroup);
		List<AxaUnderWriterPODVO> licmQueues = AxaRulesDataProcessor.getQueueListByTeam(axaLicmAssignVO);
		if (licmQueues.size() > 0) { // NBLXA-2653
			String nextAvailableQueue = getNextQueueByRoundRobin(licmQueues);
			NbaLob nbaLob = nbaDst.getNbaLob();
			nbaLob.setLicCaseMgrQueue(nextAvailableQueue);
			writeToLogFile("Case Assigned to LICM >> " + nbaLob.getLicCaseMgrQueue());
		}
	}
	
	//New Method: NBLXA-2328[NBLXA-2595]
	protected String getNextQueueByRoundRobin(List<AxaUnderWriterPODVO> queueList) throws NbaDataAccessException {
		double minScore = queueList.get(0).getCurrentScore();
		String queueWithMinScore = queueList.get(0).getUwQueue();
		AxaUnderWriterPODVO axaMinScoreVO = queueList.get(0);
		for (AxaUnderWriterPODVO axaUnderWriterPODVO : queueList) {
			if(minScore > axaUnderWriterPODVO.getCurrentScore()) {
				minScore = axaUnderWriterPODVO.getCurrentScore();
				queueWithMinScore = axaUnderWriterPODVO.getUwQueue();
				axaMinScoreVO = axaUnderWriterPODVO;
			}
		}
		advanceScoreAndCount(axaMinScoreVO,1);
		AxaRulesDataProcessor.updateScoreAndCount(axaMinScoreVO);
		return queueWithMinScore;
	}
}