package com.csc.fsg.nba.business.process;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.dataobject.nba.nbascorfeed.NbaScorFeedDO;
import com.csc.fsg.nba.database.NbaScorDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaScorSubmitContractVO;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.FormResponse;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.MedicalCondition;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.SuspendInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.TentativeDisp;
import com.csc.fsg.nba.vo.txlife.UnderwritingResult;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/*
 * ************************************************************** <BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group®.  The use,<BR>
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

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * <tr><td>SR787006-APSL3702</td>
 * <td>Discretionary</td><td>Simplified Issue</td>
 * </tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class NbaProcSIAutoUnderwriting extends NbaAutomatedProcess {

	private static final int INITIAL_OCCURANCE = 0;
	private final static String SUSPENDED = "SUSPENDED";	

	//APSL3152 code modified for handling multiple INFO REQSTESD scenario
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		if (!initialize(user, work)) {
			return getResult();
		}
		
		// APSL3308(QC12368)
		if(getNbaTxLife() != null && !getNbaTxLife().isSIApplication()){
			handleNonSICaseTOSIQueue();
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
			return getResult();
		}// APSL3308(QC12368) end
		
		Policy policy = nbaTxLife.getPolicy();		
		if (NbaUtils.isAxaWSCallNeeded(policy, AxaWSConstants.WS_OP_SUBMIT_SCOR)) {
			AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SUBMIT_SCOR, user, nbaTxLife, work, null).execute();
			nbaTxLife.addTransResponse(createTransResponse(AxaWSConstants.WS_OP_SUBMIT_SCOR));
			// SR787006-APSL3702-QC12091 Begin
			updateSuspendInfo(policy);
			if (result == null) { // not a database update error
				NbaScorSubmitContractVO scorSubmitVO = initScorSubmitVO();
				scorSubmitVO.setResponseProcessedInd(UNPROCESSED_INDICATOR);
				NbaScorDatabaseAccessor.insertRecord(scorSubmitVO);
				suspendCase(); // suspend untill ws resp not received
				unlockWork(user, work); // needed for adding JMS support
			}
			// SR787006-APSL3702-QC12091 End
			if (doSCORProcessUsingJMS) {
				submitNbaScorFeed();
			}
		} else {
			NbaScorSubmitContractVO scorVO = NbaScorDatabaseAccessor.getContractDetails(getWork().getNbaLob().getPolicyNumber());
			ApplicationInfoExtension appInfoExtnFromDB = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo());
			//case was unsuspended after response received.
			if (scorVO.getWebServiceResponse() != null && !scorVO.isResponseProcessed()) { //SR787006-APSL3702-QC12091 
				NbaTXLife scorResponse;
				try {
					scorResponse = new NbaTXLife(scorVO.getWebServiceResponse());
				} catch (Exception e) {
					throw new NbaBaseException("Error parsing in Scor webservice response.", e, NbaExceptionType.FATAL);
				}
				processSCORRespone(scorResponse);//set rate class premium etc
				processSCORDecision(appInfoExtnFromDB, scorResponse);//add auto comments.
				nbaTxLife = doContractUpdate();
				handleHostResponse(nbaTxLife);
				move2NextQueue();
			} else {//case was unsuspended and response is not received for INFO Requested.
				if (appInfoExtnFromDB.getSCORUnderWritingDecision() == NbaUtils.AXA_SCORDECISION_INFOREQ) {
					AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_UPDATE_SCOR, user, nbaTxLife, work, null).execute();
					nbaTxLife.addTransResponse(createTransResponse(AxaWSConstants.WS_OP_UPDATE_SCOR));
					// SR787006-APSL3702-QC12091 Begin
					updateSuspendInfo(policy);// this time set to null and new flag to false.
					if (result == null) { // not a database update error
						NbaScorSubmitContractVO scorSubmitVO = initScorSubmitVO();
						scorSubmitVO.setResponseProcessedInd(UNPROCESSED_INDICATOR);
						scorSubmitVO.setWebServiceResponse(null);					
						NbaScorDatabaseAccessor.resetSCORResponse(scorSubmitVO);
						suspendCase(); // suspend untill ws resp not received
						unlockWork(user, work); // needed for adding JMS support
					}
					// SR787006-APSL3702-QC12091 End
					if (doSCORProcessUsingJMS) {
						submitNbaScorFeed();
					}
				} else {//case was not suspended during submit ws call.
					suspendCase();
				}
			}
		}
		return getResult();
	}	

	private void move2NextQueue() throws NbaBaseException {
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
		}
	}

	// APSL3019 Method refactored
	private void processSCORRespone(NbaTXLife scorResponse) {
		Person person = nbaTxLife.getPrimaryParty().getPerson();
		PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
		if (personExtension == null) {
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaUtils.EXTCODE_PERSON);
			person.addOLifEExtension(olifeExt);
			personExtension = olifeExt.getPersonExtension();
			olifeExt.getPersonExtension().setActionAdd();
		}		
		
		//Read Rateclass from SCOR Response
		LifeParticipant scorLifeParticipant = scorResponse.getPrimaryInuredLifeParticipant();
		long uwClass = scorLifeParticipant.getUnderwritingClass();
		long tobaccoPremiumBasis = scorLifeParticipant.getTobaccoPremiumBasis();
		
		//Set Rate Class
		String rateClass = NbaAutoUnderwritingHelper.calculateRateClass(uwClass, tobaccoPremiumBasis);
		
		// APSL3019 Set Rate class as RateClassAppliedFor for SCOR not accepted decision
		ApplicationInfoExtension scorAppInfoExt = NbaUtils.getFirstApplicationInfoExtension(scorResponse.getPolicy().getApplicationInfo());
		if (scorAppInfoExt != null && scorAppInfoExt.getSCORUnderWritingDecision() != NbaUtils.AXA_SCORDECISION_ACCEPTED) {
			rateClass = personExtension.hasRateClassAppliedFor() ? personExtension.getRateClassAppliedFor() : calculateRateClassForScorDecision();
		}
		
		//inside Holding.Policy.Life.Coverage.OlifEExtension.CoverageExtension
		Coverage baseCoverage = nbaTxLife.getPrimaryCoverage();
		CoverageExtension baseCoverageExt = NbaUtils.getFirstCoverageExtension(baseCoverage);
		if (baseCoverageExt == null) {
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaUtils.EXTCODE_COVERAGE);
			baseCoverage.addOLifEExtension(olifeExt);
			baseCoverageExt = olifeExt.getCoverageExtension();
			olifeExt.getCoverageExtension().setActionAdd();
		}
		baseCoverageExt.setRateClass(rateClass);
		baseCoverageExt.setActionUpdate();
		
		//inside Rate Class and Approved Rate Class in Party.Person.OlifEExtension.PersonExtension
		personExtension.setApprovedRateClass(rateClass);
		personExtension.setRateClass(rateClass);
		personExtension.setRateClassAppliedFor(rateClass);
		personExtension.setProposedRateClass(rateClass);
		personExtension.setActionUpdate();		
		
		// APSL3019 Code deleted		
		NbaParty scorPrimaryParty = scorResponse.getPrimaryParty();
		Risk scorRisk = scorPrimaryParty != null ? scorPrimaryParty.getRisk() : null;
		if (scorRisk != null) {
			ArrayList scorMedConditionList = scorRisk.getMedicalCondition();
			int size = scorMedConditionList.size();
			if (size > 0) {
				Risk policyRisk = nbaTxLife.getPrimaryParty().getRisk();
				for (int i = 0; i < size; i++) {
					policyRisk.addMedicalCondition((MedicalCondition) scorMedConditionList.get(i));
					policyRisk.getMedicalConditionAt(i).setActionAdd();
				}
			}
		}
	}
	//Calculate Rate class for SCOR decision other than accepted
	private String calculateRateClassForScorDecision(){
		String rateClass= NbaConstants.RATE_CALSS_PREFERRED_NONTOBACCO;
		int MED_QUESTION_4_YES = 1;
		FormInstance medQuesFormInstance = NbaUtils.getFormInstance(nbaTxLife,NbaConstants.FORM_NAME_MEDICALQUESTION);
		if(medQuesFormInstance != null && medQuesFormInstance.getFormResponse() != null){
			for(int i=0; i< medQuesFormInstance.getFormResponse().size();i++){
				FormResponse response = (FormResponse)medQuesFormInstance.getFormResponse().get(i);
				if(response != null && NbaConstants.SI_MEDICAL_QUESTION_NUMBER_4.equalsIgnoreCase(response.getQuestionNumber())){
					if(MED_QUESTION_4_YES == response.getResponseCode()){
						rateClass = NbaConstants.RATE_CALSS_STANDARD_TOBACCO;
					}
				}
			}
		}
		return rateClass;
	}
	private void processSCORDecision(ApplicationInfoExtension appInfoExt, NbaTXLife scorResponse) throws NbaBaseException {
		String comment = "Recived invalid ApplicationInfoExtension.SCORUnderWritingDecision.";
		ApplicationInfoExtension scorAppInfoExt = NbaUtils.getFirstApplicationInfoExtension(scorResponse.getPolicy().getApplicationInfo());
		long scorDecisionFromResponse = scorAppInfoExt != null ? scorAppInfoExt.getSCORUnderWritingDecision() : 0;
		if (scorDecisionFromResponse == NbaUtils.AXA_SCORDECISION_ACCEPTED) {
			createTentativeDisp(appInfoExt);
			setLastStatusChangeDate(); // APSL3075(QC11730) Set HOCompletionDate
			comment = "Case is APPROVED from SCOR";
		} else if (scorDecisionFromResponse == NbaUtils.AXA_SCORDECISION_DECLINE) {
			setLastStatusChangeDate(); // APSL3075(QC11730) Set HOCompletionDate
			comment = "Case is DECLINE from SCOR";
		} else if (scorDecisionFromResponse == NbaUtils.AXA_SCORDECISION_INFOREQ) {
			String reasonCodes = addExceptionCodes(scorResponse);
			comment = "Case is INFORMATION REQUIRED from SCOR " + reasonCodes;		//APSL3036(QC11676). Code for generating CV moved to AXALValSiInsurance.
			deleteSystemMessage(MSGCODE_SI_INFOREQ);   //APSL3189
		} else if (scorDecisionFromResponse == NbaUtils.AXA_SCORDECISION_REFFERD) {
			setLastStatusChangeDate(); // APSL3075(QC11730) Set HOCompletionDate
			comment = "Case is REFERRED from SCOR";
		} else if (scorDecisionFromResponse == NbaUtils.AXA_SCORDECISION_WITHDRW) {
			setLastStatusChangeDate(); // APSL3075(QC11730) Set HOCompletionDate
			comment = "Case is WITHDRAWN from SCOR";
		}
		addComment(comment);
		appInfoExt.setActionUpdate();
		appInfoExt.setSCORUnderWritingDecision(scorDecisionFromResponse);
		ArrayList uwResultList = scorAppInfoExt.getUnderwritingResult();
		for (int i = 0; i < uwResultList.size(); i++) {
			appInfoExt.addUnderwritingResult((UnderwritingResult) uwResultList.get(i));
			appInfoExt.getUnderwritingResultAt(i).setActionAdd();
		}
	}
	
	//	APSL3075 (QC11730) Set HOCompletionDate. New method added
	private void setLastStatusChangeDate(){
		Policy policy = nbaTxLife.getPolicy();
		ApplicationInfo appInfo = policy.getApplicationInfo();
		if (!appInfo.hasHOCompletionDate()) {
			appInfo.setHOCompletionDate(new Date());
			appInfo.setActionUpdate();
		}
	}

	private String addExceptionCodes(NbaTXLife scorResponse) {
		StringBuffer sb = new StringBuffer("Exception reasons received : ");
		for (Iterator sysMsgIter = scorResponse.getPrimaryHolding().getSystemMessage().iterator(); sysMsgIter.hasNext();) {
			SystemMessage sm = (SystemMessage) sysMsgIter.next();
			//sb.append("MessageCode: " + sm.getMessageCode() + " MessageDescription: " + sm.getMessageDescription()); APSL3083
			sb.append(" MessageDescription: " + sm.getMessageDescription()); //APSL3083
		}
		return sb.toString();
	}
	
	// SR787006-APSL3702-QC12091 Method deleted	
	
	// SR787006-APSL3702-QC12091 New method
	private NbaScorSubmitContractVO initScorSubmitVO() {
		NbaScorSubmitContractVO scorSubmitVO = new NbaScorSubmitContractVO();
		scorSubmitVO.setContractKey(getWork().getNbaLob().getPolicyNumber());
		scorSubmitVO.setWorkItemId(getWork().getID());
		scorSubmitVO.setInsertTime(new Timestamp(System.currentTimeMillis()));
		scorSubmitVO.setOccurance(INITIAL_OCCURANCE);
		scorSubmitVO.setRetryFrequency(NbaAutoUnderwritingHelper.RETRY_FREQ_ONE);
		scorSubmitVO.setNextPollTime(NbaUtils.addSecondsToTimestamp(scorSubmitVO.getInsertTime(), NbaAutoUnderwritingHelper.INCR_SEC_RETRY_FREQ_ONE));
		if (doSCORProcessUsingJMS) {
			scorSubmitVO.setNextPollTime(NbaUtils.addSecondsToTimestamp(scorSubmitVO.getInsertTime(), 300)); //5 min
		}
		return scorSubmitVO; //SR787006-APSL3702-QC12091
	}

	// SR787006-APSL3702-QC12091 Method refactored
	private void updateSuspendInfo(Policy policy) throws NbaBaseException {
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExtension == null) {
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaUtils.EXTCODE_POLICY);
			policy.addOLifEExtension(olifeExt);
			policyExtension = olifeExt.getPolicyExtension();
			olifeExt.getPolicyExtension().setActionAdd();
		}
		// QC11840 Start
		SuspendInfo suspendInfo = policyExtension.getSuspendInfo();
		if (suspendInfo == null) {
			suspendInfo = new SuspendInfo();
			suspendInfo.setActionAdd();
		} else {
			suspendInfo.setActionUpdate();
		}
		// End QC11840		
		suspendInfo.setSuspendDate(new Date());
		suspendInfo.setUserCode(getUser().getUserID());
		policyExtension.setSuspendInfo(suspendInfo);
		policyExtension.setActionUpdate();
		nbaTxLife = doContractUpdate();
		handleHostResponse(nbaTxLife);
		// SR787006-APSL3702-QC12091 Code deleted
	}

	private void suspendCase() throws NbaBaseException {
		int suspendDays = NbaAutoUnderwritingHelper.getApplicationSuspendDays();
		Date suspendActivationDate = NbaUtils.addDaysToDate(new Date(), suspendDays);
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		suspendVO.setCaseID(getWork().getID());
		suspendVO.setActivationDate(suspendActivationDate);
		addComment("Case suspended, underwriting result has not been receipted and/or evaluated.");
		updateWork(user, work);
		suspendWork(suspendVO);
		result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", SUSPENDED);
	}

	/**
	 * This methods create a Tentative Disposition object
	 */
	protected void createTentativeDisp(ApplicationInfoExtension appInfoExt) throws NbaBaseException {
		TentativeDisp tentativeDisp = new TentativeDisp();
		tentativeDisp.setDisposition(NbaUtils.NBA_TENTATIVEDISPOSITION_APPROVED);
		tentativeDisp.setDispLevel(TENT_DISP_LEVEL_ONE);
		tentativeDisp.setDispUndID(getUser().getUserID());
		tentativeDisp.setDispDate(getCurrentDateFromWorkflow(getUser()));
		tentativeDisp.setDispReason("");
		tentativeDisp.setUWRole(getUser().getUserID());
		// Get UW_Role from table NBA_UNDAPPROVALHIERARCHY in mdb
		tentativeDisp.setUWRoleLevel(getUnderwriterLevel(getUser().getUserID(), NbaTableAccessConstants.WILDCARD, NbaTableAccessConstants.WILDCARD));
		tentativeDisp.setActionAdd();
		appInfoExt.addTentativeDisp(tentativeDisp);
	}

	/**
	 * Returns a string representing the current date from the workflow system. 
	 * @param nbaUserVO
	 *                 NbaUserVO object
	 * @return currentDate Date object
	 * @throws NbaBaseException
	 */
	protected Date getCurrentDateFromWorkflow(NbaUserVO nbaUserVO) throws NbaBaseException {
		Date currentDate = null;
		String timeStamp = getTimeStamp(nbaUserVO);
		if (timeStamp != null) {
			currentDate = NbaUtils.getDateFromStringInAWDFormat(timeStamp);
		}
		return currentDate;
	}
	
	private void submitNbaScorFeed() throws NbaBaseException {
		try {
			List nbaScorFeedList = new ArrayList();
			nbaScorFeedList.add(getUser());
			nbaScorFeedList.add(getWork());
			AccelResult accelResult = new AccelResult();
			accelResult.merge(ServiceHandler.invoke("NbaScorFeedBP", ServiceContext.currentContext(), nbaScorFeedList));
			checkOutcome(accelResult);
		} catch (Exception e) {
			getLogger().logError("Error feeding request to NBA-SCOR JMS Service Bus");
		}
	}
	
	private void checkOutcome(AccelResult accelResult) throws NbaBaseException {
		if (accelResult.hasErrors()) {
			WorkflowServiceHelper.checkOutcome(accelResult);
		} else {
			Object obj = accelResult.getFirst();
			if (obj != null) {
				NbaScorFeedDO nbaScorfeedDO = (NbaScorFeedDO) obj;
				// TODO Add code here to evaluate the nbaScorfeedDO
			}
		}
	}
	
	//New Method  APSL3189
	
	private void deleteSystemMessage(int messageCode) {
		SystemMessage message = getNbaTxLife().getSystemMessage(messageCode);
		if (message != null) {
			message.setActionDelete();
		}
	}
}