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

import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.csc.fsg.nba.business.transaction.NbaAgentNameAddressRetrieve;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.configuration.Provider;
import com.csc.fsg.nba.vo.nbaschema.AutomatedProcess;
import com.csc.fsg.nba.vo.nbaschema.Correspondence;
import com.csc.fsg.nba.vo.nbaschema.InsurableParty;
import com.csc.fsg.nba.vo.nbaschema.Requirement;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.AttachmentExtension;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
//import com.ibm.a.a.a;

/**
 * <code>NbaProcProviderFollowUp</code> changes the XMLife request source(9001) to
 * send a follow-up request to a third party provider, policy owner or producer.
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1245</td><td>Version 3</td><td>Contract Number is added in the subject of email</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture changes</td></tr>
 * <tr><td>SPR1359</td><td>Version 3</td><td>Automated processes stop poller when unable to lock supplementary work items</td></tr>
 * <tr><td>NBA091</td><td>Version 3</td><td>Agent Name and Address</td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2157</td><td>Version 4</td><td>Exception is thrown if Agent email address is not present.</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr> 
 * <tr><td>SPR2599</td><td>Version 5</td><td>Provider Follow-Up Process error stops with NbaAWDLockedException</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR1311</td><td>Version 5</td><td>Manually ordered Requirements</td></tr>
 * <tr><td>SPR2662</td><td>Version 6</td><td>Poller stops when invalid contract data is present</td></tr> 
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project<td></tr> 
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr> 
 * <tr><td>NBA123</td><td>Version 6</td><td>Administrator Console Rewrite</td></tr>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.40</td><td>AXA Life Phase 1</td><td>Contract Validation for Agent Subset</td></tr>
 * <tr><td>NBA250</td><td>AXA Life Phase 1</td><td>nbA Requirement Form and Producer Email Management Project</td></tr>
 * <tr><td>ALPC96</td><td>AXA Life Phase 1</td><td>xPression OutBound Email</td></tr>
 * <tr><td>AL4379</td><td>AXA Life Phase 1</td><td>QC #3353 - UAT E2E: Rqmts in the error queue</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */

public class NbaProcProviderFollowUp extends NbaAutomatedProcess {
	//begin SPR2662
	private static final String UNKNOWN_ERROR = "Error in follow-Up processing: ";
	private static final String MAXIMUM_EXCEEDED = "Maximum number of follow-ups exceeded";
	private static final String REQUIREMENT_SOURCE_MISSING = "Requirement XML Transaction Source is missing";
	private static final String IN = " in ";
	private static final String NO_TRANSLATION = "No Translation found for ";
	private static final String INVALID_PROVIDER_CODE = "Invalid requirement provider code: ";
	private static final String INVALID_CONTROL_SOURCE = "Invalid Requirement control source";
	private static final String SOURCE_RETRIEVE_ERROR = "Unable to retrieve Source for work item";
	private static final String NOT_SUPPORTED = "Requirement follow-up not supported";
	private static final String AGENT_FOLLOW_UP_DONE = "Agent Ordered Requirement follow-up complete";
	private static final String FOLLOW_UP_FAILED = "Follow-up failed";
	private static final String THIRD_PARTY_FOLLOW_UP_DONE = "Third party follow-Up complete";
	private static final String PREVENT_ONE_CV_EXIST = "Prevent 1 CV exist on the Case";
	private static final String REQ_STATUS_SATISFIED = "SATISFIED"; //APSL4255
	private static final String REQ_STATUS_REQCANCELD = "REQCANCELD";  //APSL4255
 	//end SPR2662
	private static final String REQ_STATUS_MANUAL_RVW = "NONUNDRCPT"; //QC15868/APSL4393
	private static final String REQ_STATUS_RECEIPTED = "RECEIPTED";   //QC15868/APSL4393
	ArrayList resultData = null;
	protected NbaTableAccessor ntsAccess = null;
	//Begin NBA250
	protected NbaDst parentCase = null;
	protected List bundleReqArray = new ArrayList();
	protected List suspendList = new ArrayList();
	protected List unlockList = new ArrayList();
	protected boolean workSuspended = false;
	protected List xmlifeList = new ArrayList();
	protected String transCode = null;
	protected List unsuspendList = new ArrayList();
	protected NbaOinkDataAccess oinkData = null;
	//end NBA250
	//NBA050 CODE DELETED
	//SPR2380 removed logger
	private String followUpProvider = null; //NBA192
	private static final String REQ_STATUS_NEGDISPDN =  "NEGDISPDN" ; // APSL5301

/**
 * NbaProcProviderFollowUp constructor comment.
 */
public NbaProcProviderFollowUp() {
	super();
}

//NBA103 - removed method

/**
 * This method will take the old subject and new followUpNumber as input and will return
 * new subject.
 * @param subject is the old subject
 * @param followUpNumber is the new followUpNumber
 * @return String is the new subject value
 */
protected String changeSubject(String subject, int followUpNumber) {	//SPR2662 change method visibility
	if ((subject.indexOf("Follow-Up")) == 0) {
		subject = subject.substring(12, subject.length());
	}

	if (followUpNumber < 10) {
		subject = "Follow-Up0" + Integer.toString(followUpNumber) + ":" + subject;
	} else {
		subject = "Follow-Up" + Integer.toString(followUpNumber) + ":" + subject;
	}

	return subject;
}
/**
 * This method will call isFollowUpSupported method to check whether follow-up is supported
 * for "Producer" or "Owner or not. If supported then it will call getDataFromVpms method
 * to check that how many follow-ups are supported for this provider. Then it'll retrieve
 * the XMLife source and will pass this source to updateXMLife method to get the modified source.
 * @param user the user/process for whom the process is being executed
 * @param work a DST value object for which the process is to occur
 * @return NbaAutomatedProcessResult containing information about
 *         the success or failure of the process
 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		//Initialization
		if (!initialize(user, work)) { //NBA123
			return getResult(); //NBA050 NBA123
		} //NBA123
		try { //SPR2662
			//NBA123 code deleted
			//begin SPR1311
			// SPR3290 code deleted
			/*
			 * APSL5176 If current requirement is Beneficiary Supp requirement then Beneficiary Supp requirement will be waived if there is no delivery
			 * requirement outstanding on the case.*/
			//Begin APSL5176
			RequirementInfo reqquirementInfo = nbaTxLife.getRequirementInfo(getWork().getNbaLob().getReqUniqueID());
			ApplicationInfo appInfo = nbaTxLife.getPolicy().getApplicationInfo();
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			if (appInfoExt != null && appInfoExt.getUnderwritingApproval() == NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER) {
				if (reqquirementInfo != null && reqquirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_1009800109
						&& NbaUtils.isRequirementOutstanding(reqquirementInfo.getReqStatus())) {
					boolean isOutStanding = false;
					List reqInfoList = nbaTxLife.getPolicy().getRequirementInfo();
					if (reqInfoList != null && reqInfoList.size() > 0) {
						for (int i = 0; i < reqInfoList.size(); i++) {
							RequirementInfo requirementInfo = (RequirementInfo) reqInfoList.get(i);
							if (isDeliveryReq(requirementInfo.getReqCode())) {
								RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
								if (NbaUtils.isRequirementOutstanding(requirementInfo.getReqStatus())
										|| (NbaOliConstants.OLI_REQSTAT_RECEIVED == requirementInfo.getReqStatus() && reqInfoExt != null && !reqInfoExt
												.getReviewedInd())) {
									isOutStanding = true;
									break;
								}
							}

						}
					}
					if (!isOutStanding) {
						String passStatus = REQ_STATUS_REQCANCELD;
						String lobStatus = "3";
						reqquirementInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_WAIVED);
						reqquirementInfo.setStatusDate(new Date());
						reqquirementInfo.setActionUpdate();
						moveToEndQueue(passStatus, lobStatus);
						doContractUpdate(nbaTxLife);
						return getResult();
					}
				}
			}
			//End APSL5176
			if (NbaConstants.PROVIDER_MANUAL.equalsIgnoreCase(getFollowUpProvider()) && isFollowUpSupported(getFollowUpProvider())) { //NBA192
				processManualRequirement();
				return getResult();
			}
			// begin APSL4255
			if (reqquirementInfo != null
					&& (NbaOliConstants.OLI_REQSTAT_RECEIVED == reqquirementInfo.getReqStatus() || NbaOliConstants.OLI_REQSTAT_WAIVED == reqquirementInfo
							.getReqStatus())) {
				String passStatus = null;
				String lobStatus = null;
				if (NbaOliConstants.OLI_REQSTAT_RECEIVED == reqquirementInfo.getReqStatus()) {
					//Begin QC15868/APSL4393
					RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqquirementInfo);
					if (reqInfoExt != null) {
						if (reqInfoExt.getReviewedInd()) {
							passStatus = REQ_STATUS_SATISFIED;
						} else {
							if (String.valueOf(REVIEW_NOT_REQUIRED).equals(reqInfoExt.getReviewCode())
									|| String.valueOf(REVIEW_USER_REQUIRED).equals(reqInfoExt.getReviewCode())) {
								passStatus = REQ_STATUS_MANUAL_RVW;
							} else if (String.valueOf(REVIEW_SYSTEMATIC).equals(reqInfoExt.getReviewCode())) {
								passStatus = REQ_STATUS_RECEIPTED;
							}
						}
						lobStatus = "7";
					}
					//End QC15868/APSL4393
				} else {
					passStatus = REQ_STATUS_REQCANCELD;
					lobStatus = "3";
				}

				moveToEndQueue(passStatus, lobStatus);
				return getResult();
			}
			// End APSL4255
			//end SPR1311
			
			
			if(nbaTxLife.getPrimaryHolding() != null && nbaTxLife.getPrimaryHolding().getPolicy() != null){
				if(NbaOliConstants.NBA_FINALDISPOSITION_WITHDRAW == nbaTxLife.getPrimaryHolding().getPolicy().getPolicyStatus()){
	
					 String passStatus = REQ_STATUS_REQCANCELD;
					 String lobStatus = "3";
				     
				     moveToEndQueue(passStatus , lobStatus);
				     return getResult();
				}
			}
			//End APSL4330
			
			// APSL5301 -- STart-- Check If case is Neg. Disposed then ]
						// requirement should not go for correspondence and move to End Queue
			if (nbaTxLife.getPrimaryHolding() != null && nbaTxLife.getPrimaryHolding().getPolicy() != null) {
				Policy policy = nbaTxLife.getPrimaryHolding().getPolicy();
				PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(policy);
				String polStatus = null;
				String lobStatus = null;
				if (polExt != null) {
					polStatus = polExt.getPendingContractStatus();
					if (polStatus != null) {
						if (NbaUtils.isPolicyNegativeDisposed(polStatus)) {
							String passStatus = REQ_STATUS_NEGDISPDN;
							if (reqquirementInfo != null){
								lobStatus = String.valueOf(reqquirementInfo.getReqStatus());
							}

							moveToEndQueue(passStatus, lobStatus);
							return getResult();
						}
					}

				}

			}
						// APSL5301 -- END
			
			if (!NbaUtils.isMsgRestrictCodeOneExists(getNbaTxLife(), NbaOliConstants.NBA_MSGRESTRICTCODE_RESTREQDET)) {// APSL2352 QC#9379
				// NBA027 - logging code deleted
				// begin SPR2662 ;
				if (getlDecoratorForSource().getRequirement().getAgentOrdered()) { // agent ordered requirement
					processAgentOrderedRequirement();
				} else if (isThirdPartyFollowUp()) { // provider is third party provider
					processThirdPartyFollowUp();
				} else {
					// APSL4165 code deleted
					throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_REQ_FLWUP); // APSL4165
				}
			} else {//Begin APSL2352 QC#9379
				addComment(PREVENT_ONE_CV_EXIST);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", NbaConstants.A_STATUS_REQUIREMENT_ORDERED));
				NbaSuspendVO suspendVO = new NbaSuspendVO();
				suspendVO.setTransactionID(getWork().getID());
				RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(getWork().getNbaLob().getReqUniqueID());
				RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
				suspendVO.setActivationDate(getFollowUpDate(reqInfoExtn));
				suspendList.add(suspendVO);
			}//End APSL2352 QC#9379
		} catch (RemoteException re) { // SPR1359
			throw new NbaBaseException(NbaBaseException.RMI, re); // SPR1359
		} catch (NbaLockedException nle) { // ALS4379
			throw nle; // ALS4379
		} catch (AxaErrorStatusException exp) { // APSL4165
			throw exp; // APSL4165
		} catch (NbaBaseException e) {
			if (e.isFatal() || e instanceof NbaAWDLockedException) {
				throw e;
			}
			getLogger().logException(e);
			String message = e.getMessage();
			addComment(message);
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, message, getFailStatus()));
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
			unlockAWD();
			return getResult();

		} // APSL3874
		// code Deleted for APSL3874

		//end SPR2662
		doContractUpdate(nbaTxLife); //APSL2352 QC#9379
		changeStatus(getResult().getStatus());

		//ALS5018 code deleted
		//update to the AWD
		updateWork(); //ALS5018
		//suspend work item but it must be called after update
		suspendAWD(); //ALS5018                             
		//unlock work items
		unlockAWD();
		return getResult();
		// SPR2662 code deleted
	}

	
	
	/***
	 * This method is use to send in end queue. 
	 * @param passStatus
	 * @param lobStatus
	 * @throws NbaBaseException
	 */
		public void moveToEndQueue(String passStatus , String lobStatus) throws NbaBaseException{
			getWork().getNbaLob().setReqStatus(lobStatus);
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, passStatus, passStatus));
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
		}
		
/**
 * This method calls a VPMS model which responds with the
 * maximum number of follow-ups allowed for this requirement.
 * @return NbaVpmsResultsData which contains the maximum number of follow-ups allowed.
 * @param entryPoint "P_GetFollowUpDays" is the entryPoint for VPMS model.
 * @throws a fatal NbaBaseException if a problem occurs executing the model
 */
//NBA008 New Method
protected NbaVpmsResultsData getDataFromVpms(String entryPoint) throws NbaBaseException {
		//ALS4843 code moved to overriden method
	NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
	return getRequirementDataFromVpms(entryPoint, oinkData);//ALS4843
}
/**
 * Answer the table accessor * 
 * @return NbaTableAccessor
 */
protected NbaTableAccessor getTableAccessor() throws NbaBaseException {
	if (ntsAccess == null) {
		ntsAccess = new NbaTableAccessor();
	}
	return ntsAccess;
}
/**
 * Answer the translation text for an olife value
 * @param olifeValue the olife value
 * @param table the table name
 * @return String is the translated text
 */
protected String getTranslationText(String olifeValue, String table) throws NbaBaseException {
	NbaTableData[] data = getTableAccessor().getDisplayData(getWork(), table);
	for (int i = 0; i < data.length; i++) {
		if (data[i].code().equalsIgnoreCase(olifeValue)) {
			return data[i].text();
		}
	}
	throw new NbaBaseException((new StringBuffer()).append(NO_TRANSLATION).append(olifeValue).append(IN).append(table).toString()); //SPR2662
}

//ALS5018 removed method


/**
 * This method checks whether Follow-up is supported for a provider or not.
 * It takes this information from NbaConfiguration File.
 * @param vendor is the Requirement Vendor code.
 * @return boolean <code>true</code> if follow-up supported otherwise <code>false</code> 
 */
protected boolean isFollowUpSupported(String vendor) throws NbaBaseException { //SPR2662 changed method visibility	
	//begin SPR2662
	Provider configProvider;
	try {
		configProvider = NbaConfiguration.getInstance().getProvider(vendor);
	} catch (NbaBaseException e) {
		e.forceFatalExceptionType();
		e.addMessage((new StringBuffer()).append(INVALID_PROVIDER_CODE).append(vendor).toString());
		throw e;
	}
	return configProvider.getFollowup();
	//end SPR2662
}

/**
 * This method changes existing XMLife9001 source with new follow-up related information
 * If this is not first time follow-up then RQLF LOB will be incremented by 1, in subject the
 * old number will be replaced with new follow-up number, AttachmentRequestNumber value will be 
 * replaced with new follow-up number.
 * If this is first time follow-up then new OlifeExtension tag will be added in Attachment tag
 * with AttachmentRequestNumber, RQFL LOB value will be set with "1" and Transaction Mode will 
 * be changed from "Original" to "Duplicate".
 * @param aSource com.csc.fsg.nba.vo.NbaSource
 */
protected void updateXMLife(NbaSource aSource) throws NbaBaseException  { //SPR2662 changed method visibility
	try {
		 //SPR2662 code deleted
		// SPR3290 code deleted
		NbaTXLife txLife = new NbaTXLife(aSource.getText());	//SPR2662
	 
		if (NbaConstants.PROVIDER_PRODUCER.equals(getFollowUpProvider())) {//NBA091 SPR2662 NBA192
			NbaAgentNameAddressRetrieve retrieve = new NbaAgentNameAddressRetrieve();//NBA112
			retrieve.setLobData(getWork().getNbaLob()); //NBA112
			txLife = retrieve.getExternalAgentSystemData(txLife,getUser());//NBA112 //AXAL3.7.40
		}//NBA091
		//Begin NBA130
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
	    if( reqInfoExt == null ) {
	    	OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO); //SPR2992
	    	reqInfoExt = oliExt.getRequirementInfoExtension();
	    	reqInfoExt.setActionAdd();
	    	getRequirementInfo().addOLifEExtension(oliExt);
	    	getRequirementInfo().setActionUpdate();
	    } else {
	    	reqInfoExt.setActionUpdate();
	    }
	    //End NBA130
		//THIS if MEANS SECOND or MORE TIME FOLLOW-UP
		if ((txLife.getPrimaryHolding().getAttachmentAt(0).getOLifEExtensionCount()) > 0) { //NBA033
			if (txLife.getPrimaryHolding().getAttachmentAt(0).getOLifEExtensionAt(0).getAttachmentExtension().hasAttachmentRequestNumber()) { //NBA033
				int followUpNumber =
					Integer.parseInt(txLife.getPrimaryHolding().getAttachmentAt(0).getOLifEExtensionAt(0).getAttachmentExtension().getAttachmentRequestNumber()); //NBA033
				followUpNumber++;
				//Follow-up number update
				txLife.getPrimaryHolding().getAttachmentAt(0).getOLifEExtensionAt(0).getAttachmentExtension().setAttachmentRequestNumber("" + followUpNumber); //NBA033
				reqInfoExt.setFollowUpRequestNumber(String.valueOf(followUpNumber));//NBA130
				//Transaction Mode update
				txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransMode(NbaOliConstants.TC_MODE_DUPLICATE);
				//Transaction Date update
				txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeDate(new Date());
				//Transaction Time update
				txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeTime(new NbaTime());

				//Subject Change
				txLife.getPrimaryHolding().getAttachmentAt(0).setAttachmentKey(
					changeSubject(txLife.getPrimaryHolding().getAttachmentAt(0).getAttachmentKey(), followUpNumber)); //NBA044
			}
		} else { //THIS else MEANS FIRST TIME FOLLOW-UP
			OLifEExtension olExt = new OLifEExtension();
			AttachmentExtension attachExt = new AttachmentExtension();
			olExt.setAttachmentExtension(attachExt);
			olExt.setVendorCode(NbaOliConstants.CSC_VENDOR_CODE); //vendor code "5" is set
			attachExt.setAttachmentRequestNumber("1");
			txLife.getPrimaryHolding().getAttachmentAt(0).addOLifEExtension(olExt); //NBA044

			reqInfoExt.setFollowUpRequestNumber(1);//NBA130
			//Transaction Mode update
			txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransMode(NbaOliConstants.TC_MODE_DUPLICATE);
			//Transaction Date update
			txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeDate(new Date());
			//Transaction Time update
			txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeTime(new NbaTime());

			//Subject Change
			txLife.getPrimaryHolding().getAttachmentAt(0).setAttachmentKey(
				changeSubject(txLife.getPrimaryHolding().getAttachmentAt(0).getAttachmentKey(), 1)); //NBA044

		}

		aSource.setText(txLife.toXmlString());
		aSource.setUpdate();
		//SPR2599 code deleted
	} catch (NbaBaseException e) {	//SPR2662
		throw e;	//SPR2662
	} catch (Throwable t) {
		throw new NbaBaseException(t);	//SPR2157
	}
}

/**
 * Change the work item status to pass status. Update and unlock workitem. 
 * @throws NbaBaseException
 */
//SPR1311 New Method
protected void processManualRequirement() throws NbaBaseException{
	setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
	changeStatus(getResult().getStatus());
	doUpdateWorkItem();
}
	/**
	 * Retrieve the Requiremnt Control Source and return it in an NbaXMLDecorator wrapper.
	 * @return NbaXMLDecorator - a wrapper for an NbAccelerator XML object
	 * @throws NbaBaseException - throws a fatal exception if the Source cannot be located
	 * 							  throws an error exception if ths Source is invalid
	 * @throws RemoteException if an error occurs while messaging NbaNetServerAccessorBean
	 */
	//SPR2662 New Method
	protected NbaXMLDecorator getlDecoratorForSource() throws RemoteException, NbaBaseException {
		NbaAwdRetrieveOptionsVO retVO = new NbaAwdRetrieveOptionsVO();
		retVO.setWorkItem(getWork().getID(), false);
		retVO.requestSources();
		retVO.setLockWorkItem();
		try {
			setWork(retrieveWorkItem(getUser(), retVO));  //NBA213
		} catch (NbaBaseException e) {	//assume configuration or communication problem
			e.forceFatalExceptionType();
			throw e;			
		}
		try {
			return new NbaXMLDecorator(getWork().getRequirementControlSource().getText());
		} catch (NbaBaseException e1) {	//assume that problems are data related
			e1.addMessage(INVALID_CONTROL_SOURCE);
			throw e1;
		}		
	}
	
	/**
	 * Process an agent ordered Requirement by adding a NBREQRXML source item containing a TxLife 9001 transaction to the work item.
	 * @throws NbaBaseException if the 9001 transaction cannot be created.
	 */
	//SPR2662 New Method
	protected void processAgentOrderedRequirement() throws NbaBaseException {
		//Begin NBA250
		if(isBundleFollowUp()){
			if(NbaConstants.PROVIDER_PRODUCER.equalsIgnoreCase(getFollowUpProvider())){
				if(isFollowUpNeeded()){//ALS5018
					doBundling();
					if (getResult() == null) {
						addComment(AGENT_FOLLOW_UP_DONE);
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", NbaConstants.A_STATUS_REQ_CORR_NEEDED)); //ALPC96
					}
				}else{
					// Begin ALS5018
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", NbaConstants.A_STATUS_REQUIREMENT_ORDERED));
   					NbaSuspendVO suspendVO = new NbaSuspendVO();
   					suspendVO.setTransactionID(getWork().getID());
   					RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(getWork().getNbaLob().getReqUniqueID());
   					RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
   					suspendVO.setActivationDate(reqInfoExtn.getFollowUpDate());
   					suspendList.add(suspendVO); 
				}//End ALS5018
			}
		}else{  //End NBA250
			NbaTXLife txL = create9001Request(getWork());
			if (getResult() == null) {
				NbaAgentNameAddressRetrieve retrieve = new NbaAgentNameAddressRetrieve();
				retrieve.setLobData(getWork().getNbaLob());
				txL = retrieve.getExternalAgentSystemData(txL,getUser());
				getWork().addNbaSource(new NbaSource(getWork().getBusinessArea(), NbaConstants.A_ST_REQUIREMENT_XML_TRANSACTION, txL.toXmlString()));
				addComment(AGENT_FOLLOW_UP_DONE);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", NbaConstants.A_STATUS_REQ_EMAIL_NEEDED));
			}
		}
	}
		
	/**
	 * This method does the bundling of the requirements and then process the bundle requirements
	 * and if bundling is successful does the follow up for them.
	 */
	//NBA250 New Method
	protected void doBundling() throws NbaBaseException {
		try {
			if (!doBundleRequirements()) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspend", "Suspend"));
			}
		} catch (NbaNetServerException e) {
			throw e;
		}
		doFollowup();
	}
	
	
	/**
	 * This method first creates and adds XMLife message to the each work item. Then
	 * It call provider to transform list of XMLife message to the provider ready message.
	 * When provider ready message is receipt, it associates each work item with provider
	 * ready message as a source.Calls vpms to get pass status and suspend days. 
	 * Also changes the bundle work items status to pass status
	 */
	//NBA250 New Method
	protected void doFollowup() throws NbaBaseException {
		//ALPC96 Deleted
		updateReqInfoExtn(getWork());//ALS2425
		for (int i = 0; i < bundleReqArray.size(); i++) {
			//ALPC96 Deleted 
			NbaDst item = (NbaDst) bundleReqArray.get(i);
			updateReqInfoExtn(item);//ALS2425
		}
		//ALPC96 Deleted
		NbaProcessStatusProvider newStatuses = null;
		RequirementInfo reqInfo = null; //ALPC96
		//ALS5018 removed
		StringBuffer reqStr = new StringBuffer(); //ALPC96
		for (int i = 0; i < bundleReqArray.size(); i++) {
			NbaDst item = (NbaDst) bundleReqArray.get(i);
			if (!item.getStatus().equals(getFailStatus())) { // if ststus is set to fail ststus leave the work item
				// SR534920 code deleted
				//ALS5018 removed
       			reqStr.append(item.getNbaLob().getReqUniqueID()); //ALPC96//ALS5018 // SR534920
       			reqStr.append(", ");//ALPC96
       			item.getNbaLob().setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_SUBMITTED));
   				item.getNbaLob().setReqOrderDate(requirementInfo.getRequestedDate()); 
   				if (newStatuses == null) {
   					//get pass status from vpms for bundled requirements
  					try {
   							newStatuses = new NbaProcessStatusProvider(getUser(), item, nbaTxLife, nbaTxLife.getRequirementInfo(getWork().getNbaLob().getReqUniqueID()));//ALPC96
    					}catch (NbaBaseException e) {
    						throw new NbaVpmsException("Problem in getting next status from VPMS", e);
    					}
		    	}
   				//change awd status to pass status from vpms
   				changeStatus(item, newStatuses.getPassStatus());
   				item.increasePriority(newStatuses.getWIAction(), newStatuses.getWIPriority());
   				WorkflowServiceHelper.update(getUser(), item);//ALPC96
   				int suspendDays = getReqSuspendDays(item); 
   				if(suspendDays > 0){ 
   					//suspend work item to number of days from vpms
   					NbaSuspendVO suspendVO = new NbaSuspendVO();
   					suspendVO.setTransactionID(item.getID());
   					GregorianCalendar calendar = new GregorianCalendar();
   					calendar.setTime(new Date());
   					calendar.add(Calendar.DAY_OF_WEEK, suspendDays);
   					Date reqSusDate = (calendar.getTime());
   					suspendVO.setActivationDate(reqSusDate);
   					if (item.isSuspended()) {
   						//unsuspend first if suspended 
   						unsuspendWork(getUser(), suspendVO);//ALPC96
   					}
   					suspendWork(getUser(), suspendVO); //ALPC96
   				}
			}
		}
		String followUpList = null;
		reqInfo = nbaTxLife.getRequirementInfo(getWork().getNbaLob().getReqUniqueID());
		//ALS5018 removed
		//ALS5018 removed
		reqStr.append(getWork().getNbaLob().getReqUniqueID());//ALS5018 //SR534920
		reqStr.append(", ");
		//ALS5018 removed
		if(reqStr.length()>0){ //If there is any requirement in the list
			followUpList = reqStr.substring(0,reqStr.length()-2); //Remove last comma and space from the list
			addFolloupRequirementList(reqInfo, followUpList);//ALPC96
			createCorrespondenceSource();//ALPC96
		}
	}
	
	//ALPC96 New Method
	/**
	 * This Method create a correspondence source and attach it to the requirement WI
	 * @throws NbaVpmsException
	 */
	private void createCorrespondenceSource() throws NbaVpmsException{
		
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
		NbaVpmsAdaptor vpmsProxy = null;
		Map deOink = new HashMap();
		deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
		try {
				vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.CORRESPONDENCE);
				vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_LETTERS);
				vpmsProxy.setSkipAttributesMap(deOink);
				String letterName = parseVpmsResult(vpmsProxy.getResults().getResult());
				Correspondence corrXML = new Correspondence();
				corrXML.setLetterName(letterName);
				corrXML.setLetterType(NbaCorrespondenceUtils.LETTER_EVENTDRIVEN);
				corrXML.setPolicyNumber(getWork().getNbaLob().getPolicyNumber());
				corrXML.setObjectRef(getWork().getID());
		        ByteArrayOutputStream stream = new ByteArrayOutputStream();
		        corrXML.marshal(stream);
		        getWork().addNbaSource(new NbaSource(getWork().getBusinessArea(), NbaConstants.A_ST_CORRESPONDENCE_XML, stream.toString()));
		        getWork().getNbaLob().setLetterType(letterName);
		        setWork(WorkflowServiceHelper.update(getUser(), getWork()));
		} catch (RemoteException e1) {
			e1.printStackTrace();
		} catch (NbaVpmsException e1) {
			throw new NbaVpmsException("Problem in creating correspondence source", e1);
		} catch (NbaBaseException e) {
			throw new NbaVpmsException("Problem in creating correspondence source", e);
		}finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}			 
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}
	}
	
	
	/**
	 * This method parese a VP/MS result and return the letter name by omitting the dilimeter.
	 * @return String (Letter Name)
	 * @param aResult A result string
	 */
	//ALPC96 New Method
	protected String parseVpmsResult(String aResult) {
	    String letterName = null;
	    NbaStringTokenizer result = new NbaStringTokenizer(aResult, NbaVpmsAdaptor.VPMS_DELIMITER[1]);
	    if (result.countTokens() > 0) {
	        //Omit the first token
	        result.nextToken();
	        while (result.hasMoreTokens()) {
	            letterName = result.nextToken();
	        }
	    }
	    return letterName;
	}
	

	/**
	 * This method updated the list of requirement in the bundle to the RequirementInfoExtension -> RequirementFollowUpList 
	 * @param reqStr List of requirement in the bundle
	 * @throws NbaBaseException
	 */
	//	ALPC96 New Method
	private void addFolloupRequirementList(RequirementInfo reqInfo, String followUpList) throws NbaBaseException{
		
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		if( reqInfoExt == null ) {
	    	OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO); 
	    	reqInfoExt = oliExt.getRequirementInfoExtension();
	    	reqInfoExt.setActionAdd();
	    	getRequirementInfo().addOLifEExtension(oliExt);
	    	getRequirementInfo().setActionUpdate();
	    } else {
	    	reqInfoExt.setActionUpdate();
	    }
		reqInfoExt.setRequirementFollowUpList(followUpList);
		doContractUpdate(nbaTxLife);
	}
	
	
	/** This method checks if the Max Folloup has reached for the requirement or nor. 
	 * @param reqInfo
	 * @return
	 * @throws NbaBaseException
	 */
	//ALPC96 New Method
	private boolean isFollowUpNeeded() throws NbaBaseException{ //ALS5018 changes signature
		boolean followUp = false;
		RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(getWork().getNbaLob().getReqUniqueID());//ALS5018
		RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		if(IsFollowUpDateReached(reqInfoExtn)){//AL5018
			NbaVpmsResultsData followupData = getDataFromVpms(NbaVpmsAdaptor.EP_NUMBER_FOLLOW_UPS);
			int count = 0;
			try {
				resultData = followupData.getResultsData();
		        if (resultData == null || resultData.size() == 0) {
		            throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS);
		        }
				count = Integer.parseInt((String) resultData.get(0));
			} catch (NumberFormatException e1) {
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS);
			}
			int followUpReqNumber = 0;
			if( null != reqInfoExtn ) {
				followUpReqNumber = reqInfoExtn.getFollowUpRequestNumber();
			}
			if (followUpReqNumber < count) {
			    //if maxmimum number of follow-ups has not been reached then
				followUp = true;
			} else {
				addComment("Maximum number of follow-ups exceeded");
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Follow-up failed", getOtherStatus()));
			}
		}//ALS5018
		return followUp;
	}
	
	
	/**
	 * Updates the RequirementInfoExtension for the RequirementInfo object
	 * @param data Contains the follow up frequency for the requirement
	 * @param reqInfoExt RequirementInfoExtension which has to be updated
	 */
	//NBA250 New Method //ALS4843 signature changed
	protected void updateReqInfoExtn(RequirementInfoExtension reqInfoExt, NbaVpmsResultsData data, NbaDst nbaDst) throws NbaBaseException  {
		
		    //THIS if MEANS SECOND or MORE TIME FOLLOW-UP
			if (reqInfoExt.getFollowUpRequestNumber() > 0) { 
				int followUpNumber =
					reqInfoExt.getFollowUpRequestNumber();
				followUpNumber++;
				reqInfoExt.setFollowUpRequestNumber(String.valueOf(followUpNumber));
			} else { //THIS else MEANS FIRST TIME FOLLOW-UP
				reqInfoExt.setFollowUpRequestNumber(1);
			}
			reqInfoExt.getTrackingInfo().setFollowUpCompleted(false);
			if (data.getResultsData() != null && data.getResultsData().size() > 0) {
					updateRequirementInfoExtension(data, reqInfoExt, nbaDst);//ALS4843
				}
			reqInfoExt.setActionUpdate();
		}
	
	/**
	 * Updates the RequirementInfoExtension for the RequirementInfo object
	 * @param nbaDst 
	 */
	//ALS2425 New Method
	protected void updateReqInfoExtn(NbaDst nbaDst) throws NbaBaseException  {
		
		RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(nbaDst.getNbaLob().getReqUniqueID());
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		if( reqInfoExt == null ) {
	    	OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO); 
	    	reqInfoExt = oliExt.getRequirementInfoExtension();
	    	reqInfoExt.setActionAdd();
	    	getRequirementInfo().addOLifEExtension(oliExt);
	    	getRequirementInfo().setActionUpdate();
	    } else {
	    	reqInfoExt.setActionUpdate();
	    }
		NbaVpmsResultsData data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_FOLLOWUP_DAYS);
		updateReqInfoExtn(reqInfoExt, data, nbaDst);//ALS4843
		}

	/**
	 * Updates the RequirementInfoExtension for the RequirementInfo object
	 * @param data Contains the follow up frequency for the requirement
	 * @param reqInfoExt RequirementInfoExtension which has to be updated
	 */
	 //NBA250 New Method //ALS4843 signature changed
	private void updateRequirementInfoExtension(NbaVpmsResultsData data, RequirementInfoExtension reqInfoExt, NbaDst nbaDst) throws NbaBaseException{
	    //Begin ALS4843
	    int followupDays = Integer.parseInt((String) data.getResultsData().get(0));
	    if(isResetFollowUpDaysNeeded(nbaDst)){
	    	reqInfoExt.setFollowUpFreq(followupDays);
	    } 
	    if(reqInfoExt.hasFollowUpFreq()){
	    	followupDays = reqInfoExt.getFollowUpFreq(); //get the stored value or the latest updated value 
	    }
	    GregorianCalendar calendar = new GregorianCalendar();
	    calendar.setTime(new Date());
	    calendar.add(Calendar.DAY_OF_WEEK, followupDays);
	    reqInfoExt.setFollowUpDate(calendar.getTime());
	    reqInfoExt.setActionUpdate();
	    //End ALS4843
	}
	/**
	 * Answer order suspend days so requirement will wait to recieve response
	 * from provider before wake up in ordered process.
	 * 
	 * @return number of suspend days.
	 * @param item - the wotk item.
	 */
	//NBA250 New Method
	protected int getReqSuspendDays(NbaDst item) throws NbaBaseException {
		//call vpms to get suspend days
		//begin ALS4843 
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(item.getNbaLob());
		oinkData.setContractSource(nbaTxLife); //NBA130
		//begin ALS2425,ALS2411
		NbaVpmsAdaptor vpmsProxy = null;
		int suspendDays = 0;
		try {
			getLogger().logDebug("Starting Retrieval of data from VPMS model"); //NBA044
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaConfiguration.REQUIREMENTS);
			Map deOinkMap = new HashMap();
			deOinkMap.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_SUSPEND_DAYS);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			List suspendDayList = data.getResultsData();
			if (suspendDayList != null && suspendDayList.size() > 0) {
				suspendDays = Integer.parseInt(suspendDayList.get(0).toString());
			}
			if (isResetFollowUpDaysNeeded(item)) {
				return suspendDays;
			}
			return getFollowUpFrequency(item.getNbaLob().getReqUniqueID());
			//end ALS4843
		} catch (java.rmi.RemoteException re) {
			String desc = new StringBuffer().append("Model: ").append(NbaConfiguration.REQUIREMENTS).append(", entrypoint:  ").append(
					NbaVpmsAdaptor.EP_GET_SUSPEND_DAYS).toString();
			throw new NbaVpmsException(desc, re, NbaExceptionType.FATAL);
		} catch (NbaBaseException e) {
			throw new NbaVpmsException("Problem in getting suspend days from VPMS", e);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (RemoteException re) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		//end ALS2425,ALS2411
	}
	/**
	 * This method creates the awd source with generated xmlife message and attached to the wotk item
	 * 
	 * @param NbaDst - the work item
	 */
	//NBA250 New Method
	protected void addXMLifeTrans(NbaDst reqItem, String xmlTrans) throws NbaBaseException {
		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
		Policy policy = nbaTxLife.getPolicy();
		Iterator reqInfoIter = policy.getRequirementInfo().iterator();
		while( reqInfoIter.hasNext()) {
			RequirementInfo reqInfo = (RequirementInfo)reqInfoIter.next();
			if( reqInfo.getRequirementInfoUniqueID() != null && reqInfo.getRequirementInfoUniqueID().equalsIgnoreCase(reqItem.getNbaLob().getReqUniqueID())) {
				//Attachment
				Attachment attach = new Attachment();
				nbaOLifEId.setId(attach);     
				attach.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT);
				if(reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MIBCHECK){
					attach.setAttachmentType(NbaOliConstants.OLI_ATTACH_MIB401);   
				}else{
					attach.setAttachmentType(NbaOliConstants.OLI_ATTACH_REQUIREREQUEST);
				}
				AttachmentData attachData = new AttachmentData();
				attachData.setPCDATA(xmlTrans); 
				attach.setAttachmentData(attachData);
				attach.setActionAdd();
				reqInfo.addAttachment(attach);
				reqInfo.getActionIndicator().setUpdate();
				policy.getActionIndicator().setUpdate();
				return;
			}
		}
	}
	/**
	 * Answer the transaction code. it retrieve transaction code from original 
	 * work item requirement control source.
	 * 
	 * @return NbaDst which represent a awd case
	 */
	//NBA250 New Method
	protected String getTransactionCode() throws NbaBaseException {
		if (transCode == null) {
			NbaSource source = getWork().getRequirementControlSource();
			NbaXMLDecorator reqSource = new NbaXMLDecorator(source.getText());
			transCode = reqSource.getRequirement().getTransactionId();
		}
		return transCode;
	}
	/**
	 * This method first determine the bundle requirements based on provider id and
	 * transaction code.Call AWD to get array of bundle requirement dst objects.
	 * Then process the bundle requirements.
	 * @return true if bundle requirements processed succesfully.
	 */
	//NBA250 New Method
	protected boolean doBundleRequirements() throws NbaBaseException {
		List wfIdList = new ArrayList(); 
		NbaLob lob = getWork().getNbaLob();

		NbaSource source = getParentCase().getRequirementControlSource();
		if (source == null) {
			throw new NbaBaseException(NO_REQ_CTL_SRC); 
		}
		NbaXMLDecorator reqSource = new NbaXMLDecorator(source.getText());
		//get all insurable party object from requirement control source
		
		List parties = (ArrayList)reqSource.getInsurableParties();
		for (int i = 0; i < parties.size(); i++) {
			InsurableParty party = (InsurableParty) parties.get(i);
			Requirement req = null;
			for (int j = 0; j < party.getRequirementCount(); j++) {
				req = party.getRequirementAt(j);
					if(!(isDeliveryReq(req.getCode()) && !isContractPrintDone(getWork().getNbaLob().getPolicyNumber()))) { // APSL4902  For delivery requirements do not do fallow up until print passed 
					// APSL4426 -- Check FollowUp Service Provider
					String followupProvider = nbaTxLife.getFollowUpServiceProvider(req.getCode());
					if (req.getProvider().equals(lob.getReqVendor()) && !NbaConstants.PROVIDER_MANUAL.equalsIgnoreCase(followupProvider)) { // FollowUp Bundling should be for Only For Automated FollowUp, Not for MANL
						if (!req.getAwdId().equals(getWork().getID())) {
							// add bundle work items id to the list
							wfIdList.add(req.getAwdId());
						}
					}
				}
			}
		}
		// if bundle requirement ids are present on the requirement control source
		if (wfIdList.size() > 0) { 
			//create ans set retrive option
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(getWork().getID(), false);
			retOpt.setLockWorkItem();
			retOpt.requestSources();
			retOpt.setAutoSuspend();
			//get dst list
			bundleReqArray = retrieveWorkItemList(getUser(), retOpt, wfIdList);  //NBA213 SPR3290
			//add list to unlock list
			unlockList.addAll(bundleReqArray);
			// process the all bundle requirements
			return processBundleRequirements();
		}
		return true;

	}
	/**
	 * Check if Requirement has reached maximum number of follow-up's if yes then add comment and route it to case manager.
	 * @param followUpRequestNumber
	 * @return
	 * @throws NbaBaseException
	 */
	//ALS5018 New Method
	private boolean IsMaxFollowUpReached(int followUpRequestNumber)throws NbaBaseException {
		boolean maxFollowUpReached = true;
		NbaVpmsResultsData followupData = getDataFromVpms(NbaVpmsAdaptor.EP_NUMBER_FOLLOW_UPS);
		int count = 0;
		try {
			resultData = followupData.getResultsData();
	        if (resultData == null || resultData.size() == 0) {
	            throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS);
	        }
			count = Integer.parseInt((String) resultData.get(0));
			} catch (NumberFormatException e1) {
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS);
		}
		if (followUpRequestNumber < count) {
		    //if maxmimum number of follow-ups has not been reached then
			maxFollowUpReached = false;
		} else {
			addComment("Maximum number of follow-ups exceeded");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Follow-up failed", getOtherStatus()));
		}
		return maxFollowUpReached;
	}
	
	/**
	 * Check if Follow-up date is today
	 * @param reqInfoExtn
	 * @return
	 */
	//ALS5018 New Method
	private boolean IsFollowUpDateReached(RequirementInfoExtension reqInfoExtn){
		if(NbaUtils.compare(reqInfoExtn.getFollowUpDate(), new Date()) <= 0){
			return true;
		}
		return false;
	}
	/** 
	 * Check if Follow-up date is today
	 * @param reqInfoExtn
	 * @return
	 */
	//APSL257 New Method
	private boolean IsFollowUpDateInFuture(RequirementInfoExtension reqInfoExtn){
		if(NbaUtils.compare(reqInfoExtn.getFollowUpDate(), new Date()) >= 0){
			return true;
		}
		return false;
	}
	
	/**
	 * This method process the bundle requirements as below:
	 *  - First check whether work item in the order queue
	 *  - if it is in order queue continue process other work items.
	 *  - if not in order queue, check the requirement status
	 *		- if requirement status is order and original work item 
	 *			 is not previous suspended in order queue
	 *      	- suspend original work item for number of minutes from configuration file.
	 *		- else leave the delinquent work item and continue process other work items.
	 * @return true if original work item is not set for suspension and all
	 *	bundle requirements are processed successfully.
	 */
	//New Method NBA250
	protected boolean processBundleRequirements() throws NbaBaseException {
		boolean suspended = false;
		NbaSource source = null;
		NbaXMLDecorator reqSource = null;
		// SPR3290 code deleted
		NbaDst item = null;
		Iterator iterateReq = bundleReqArray.iterator();
		while (iterateReq.hasNext()) {
			item = (NbaDst) iterateReq.next();
			if (isReqInOrderQueue(item)) {
				continue;
			}
			if (isReqInEndQueue(item)) {//ALS5381
				iterateReq.remove(); //ALS5381
				continue; //ALS5381
			}
			// if work item is not in order queue.
			if (item.getNbaLob().getReqStatus().equals(Long.toString(NbaOliConstants.OLI_REQSTAT_ORDER))) {
				if (suspended == false) {
					//if requirement status is order 
					//check for previous suspension only one time
					source = getWork().getRequirementControlSource();
					reqSource = new NbaXMLDecorator(source.getText());
					AutomatedProcess process = reqSource.getAutomatedProcess(NbaUtils.getBusinessProcessId(getUser())); 
					//if requirement is previusly suspended in order process
					if (process != null && process.hasSuspendDate()) {
						suspended = true;
					} else {
						NbaSuspendVO suspendVO = new NbaSuspendVO();
						suspendVO.setTransactionID(getWork().getID());
						GregorianCalendar calendar = new GregorianCalendar();
						Date currDate = new Date();
						calendar.setTime(currDate);
						calendar.add(Calendar.MINUTE, getSuspendMinute());
						suspendVO.setActivationDate(calendar.getTime());
						//set requirement source control for suspend days
						if (process == null) {
							process = new AutomatedProcess();
							process.setProcessId(NbaUtils.getBusinessProcessId(getUser())); 
							reqSource.getRequirement().addAutomatedProcess(process);
						}
						addComment("Sibling requirements for Bundling are not in Order Queue"); 
						process.setSuspendDate(currDate);
						source.setText(reqSource.toXmlString());
						source.setUpdate();
						NbaRequirementUtils reqUtils = new NbaRequirementUtils(); 
						reqUtils.updateRequirementControlSource(getWork(), null, source.getText(), NbaRequirementUtils.actionUpdate); 
						suspendList.add(suspendVO);
						setWorkSuspended(true);
						return false;
					}
				}
			}
			if ((item.getNbaLob().getReqStatus().equals(Long.toString(NbaOliConstants.OLI_REQSTAT_ADD))
					|| item.getNbaLob().getReqStatus().equals(Long.toString(NbaOliConstants.OLI_REQSTAT_SUBMITTED)))) { //ALPC96 //ALS5018
				// SR500100 code deleted
				continue;
			}
			iterateReq.remove();
		}
		return true;
	}
	
	
//ALS5018 removed method	
	
	/**
	 * Answer the awd case and sources
	 * 
	 * @return NbaDst which represent a awd case
	 */
	//NBA250 New Method
	protected NbaDst getParentCase() throws NbaBaseException {
		if (parentCase == null) {
			//create and set parent case retrieve option
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(getWork().getID(), false);
			retOpt.requestCaseAsParent();
			retOpt.requestSources();
			//get case from awd
			parentCase = retrieveWorkItem(getUser(), retOpt);  //NBA213
			//remove original transaction
			parentCase.getTransactions().clear();
			
		}
		return parentCase;
	}
	/**
	 * Answer the work item queue
	 * 
	 * @return returns true if work item in the order requirement queue
	 * @param item - the wotk item.
	 */
	//NBA250 New Method
	protected boolean isReqInOrderQueue(NbaDst item) {
		return A_QUEUE_ORDER_REQUIREMENT.equals(item.getQueue());//ALS5018

	}
	
	/**
	 * Answer the work item queue
	 * 
	 * @return returns true if work item in the end queue
	 * @param item - the wotk item.
	 */
	//ALS5381 New Method
	protected boolean isReqInEndQueue(NbaDst item) {
		return END_QUEUE.equals(item.getQueue());

	}
	/**
	 * Answer the order requirement queue suspend minutes
	 * @return the number of minuteswhich a work item in the order requirement queue may be suspeneded 
	 */
	//NBA250 New Method
	protected int getSuspendMinute() throws NbaBaseException {
		String suspendMinutes = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.ORDER_REQUIREMENT_SUSPEND_MINUTES); //ACN012
		if (suspendMinutes != null) { 
			return Integer.parseInt(suspendMinutes); 
		}
		return 0;

	}
	/**
	 * set to true is work is suspended during current process
	 * 
	 * @param newWorkSuspended boolean
	 */
	protected void setWorkSuspended(boolean newWorkSuspended) {
		workSuspended = newWorkSuspended;
	}
	/**
	 * Process a third party Requirement follow-up. If the maximum number of follow-ups
	 * has not been exceeded, update the Requirement control source. Otherwise, cause the
	 * status of the work item to set to the failed status from the VPMS status model.
	 * @throws NbaBaseException
	 */
	//SPR2662 New Method
	protected void processThirdPartyFollowUp() throws NbaBaseException {
		NbaVpmsResultsData data = getDataFromVpms(NbaVpmsAdaptor.EP_NUMBER_FOLLOW_UPS);		
		int count = 0;
		try {
			resultData = data.getResultsData();
            if (resultData == null || resultData.size() == 0) {
                throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS);
            }
			count = Integer.parseInt((String) resultData.get(0));
		} catch (NumberFormatException e1) {
			throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS);
		}
		//Begin NBA130
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo); //NBA130
		int followUpReqNumber = 0;
		if( null != reqInfoExt ) {
			followUpReqNumber = reqInfoExt.getFollowUpRequestNumber();
		}
		if (followUpReqNumber < count) {
		    // NBA250 Bundle the Follow-up
		if(NbaConstants.PROVIDER_PRODUCER.equalsIgnoreCase(getFollowUpProvider())){
			doBundling();
				handleHostResponse(doContractUpdate());
				if (getResult() == null) {
					addComment(THIRD_PARTY_FOLLOW_UP_DONE);
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
					//Begin APSL804 QC#5909
					NbaSuspendVO suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(getWork().getID());
					RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(getWork().getNbaLob().getReqUniqueID());
					RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					suspendVO.setActivationDate(reqInfoExtn.getFollowUpDate());
					if (suspendList.size()>0) {
						boolean suspendInd=false;
						for (int i = 0; i < suspendList.size(); i++) {
							NbaSuspendVO newSuspendVO = (NbaSuspendVO) suspendList.get(i);
							if (newSuspendVO.getTransactionID().equalsIgnoreCase(suspendVO.getTransactionID())) {
								suspendInd=true;
								break;
							}
						}
						if(!suspendInd){
							suspendList.add(suspendVO);
						}
					} else {
						suspendList.add(suspendVO);
					}//End APSL804QC#5909
				}
				return;
			}//End NBA250
		//End NBA130
			//if maxmimum number of follow-ups has not been reached then
			ListIterator sources = getWork().getNbaSources().listIterator();
			NbaSource aSource = null;
			while (sources.hasNext()) {
				aSource = (NbaSource) sources.next();
				if (aSource.isRequirementXMLifeTransaction()) {
					break;
				}
				aSource = null;
			}
			if (aSource == null) {
				// APSL4165 code deleted
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_REQ_SRC);// APSL4165
			}// APSL4165 code deleted
			updateXMLife(aSource);
			handleHostResponse(doContractUpdate()); // NBA130 update the follow-up number
			if (getResult() == null) { // NBA130
				addComment(THIRD_PARTY_FOLLOW_UP_DONE);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			} // NBA130
			// APSL4165 code deleted
		} else {
			addComment(MAXIMUM_EXCEEDED);
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, FOLLOW_UP_FAILED, getFailStatus()));
		}
	}
	/**
	 * Determine if Follow-up is supported for a third party provider.
	 * It takes this information from NbaConfiguration File.
	 * @return boolean <code>true</code> if follow-up supported otherwise <code>false</code> 
	 */
	//SPR2662 New Method
	protected boolean isThirdPartyFollowUp() throws NbaBaseException {
		boolean supported = false;
		//NBA192 code deleted
		if (NbaConstants.PROVIDER_PRODUCER.equals(getFollowUpProvider()) || NbaConstants.PROVIDER_OWNER.equals(getFollowUpProvider())) { //Third party NBA192
			supported = isFollowUpSupported(getFollowUpProvider()); //NBA192
		}
		return supported;
	}
    /**
	 * Determine if bundling is supported for Follow-up.
	 * It takes this information from NbaConfiguration File.
	 * @return boolean <code>true</code> if bundling while follow-up supported otherwise <code>false</code> 
	 */
	//NBA250 New Method
	protected boolean isBundleFollowUp() throws NbaBaseException {
		boolean supported = false;
		Provider configProvider;
		try {
			configProvider = NbaConfiguration.getInstance().getProvider(getFollowUpProvider());
			supported = configProvider.getBundleflwup();
		} catch (NbaBaseException e) {
			e.forceFatalExceptionType();
			e.addMessage((new StringBuffer()).append(INVALID_PROVIDER_CODE).append(getFollowUpProvider()).toString());
			throw e;
		}
		return supported;
	}
    /**
     * Return the follow-up provider ID
     * @return the follow-up provider ID.
     */
	//NBA192 New Method
    public String getFollowUpProvider() {
        if (followUpProvider == null) {
            RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
            if (reqInfoExt != null && reqInfoExt.getTrackingInfo() != null) {
                followUpProvider = reqInfoExt.getTrackingInfo().getFollowUpServiceProvider();
            }
        }
        return followUpProvider;
    }

    /**
     * unlock the AWD.
     */
    //NBA250 New Method
    protected void unlockAWD() throws NbaBaseException {
    	String originalID = getOrigWorkItem().getID();	
        for (int i = 0; i < unlockList.size(); i++) {
            if (!originalID.equals(((NbaDst) unlockList.get(i)).getID())) {	
                unlockWork(getUser(), (NbaDst) unlockList.get(i)); 
            }
        }
    }
    
    //ALS5018 New Method
    protected void suspendAWD() throws NbaBaseException {
    	for (int i = 0; i < suspendList.size(); i++) {
    		NbaSuspendVO nbaSuspendVO = (NbaSuspendVO) suspendList.get(i); //APSL5055-NBA331.11
            nbaSuspendVO.setKeepLock(false); //APSL5055-NBA331.11
            suspendWork(getUser(), nbaSuspendVO); ////NBA213, APSL5055-NBA331.11
    	}
    }
    
    
	
	/**
	 * Calls VP/MS to check if resetting of followup days needed or not.
	 * @return True or False 
	 * @throws NbaBaseException
	 */
	//ALS4843 new Method
	protected boolean isResetFollowUpDaysNeeded(NbaDst req) throws NbaBaseException {
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(req.getNbaLob());
		NbaVpmsResultsData data = getRequirementDataFromVpms(NbaVpmsConstants.EP_IS_RESET_FOLLOWUP_DAYS_NEEDED, oinkData);
		if (data.getResultsData() != null && data.getResultsData().size() > 0) {
			String strResult = (String) data.getResultsData().get(0);
			if (strResult != null && !strResult.trim().equals("")) {
				return Boolean.valueOf(strResult).booleanValue();
			}
		}
		return false;
	}

    /**
     * This method first determine the bundle follow ups based on provider id and
     * transaction code. Call AWD to get array of bundle requirement dst objects.
     * Then process the bundle requirements.
     * @return true if bundle requirements processed succesfully.
     */
//    NBA250 New Method
    
    /*
    protected boolean doBundleFlwup() throws NbaBaseException {
    	List wfIdList = new ArrayList(); // SPR3290
    	// SPR3290 code deleted
    	NbaLob lob = getWork().getNbaLob();

    	NbaSource source = getParentCase().getRequirementControlSource();
    	if (source == null) {
    		throw new NbaBaseException(NO_REQ_CTL_SRC); //NBA050
    	}
    	NbaXMLDecorator reqSource = new NbaXMLDecorator(source.getText());
    	//get insurable party object from requirement control source
    	InsurableParty party = reqSource.getInsurableParty(Integer.toString(lob.getReqPersonSeq()), lob.getReqPersonCode());
    	Requirement req = null;
    	for (int i = 0; i < party.getRequirementCount(); i++) {
    		req = party.getRequirementAt(i);
    		if (req.getProvider().equals(lob.getReqVendor()) && req.getProvTransId().equals(getProvTransId())) {
    			if (!req.getAwdId().equals(getWork().getID())) {
    				//add bundle work items id to the list
    				wfIdList.add(req.getAwdId()); // SPR3290
    			}
    		}
    	}
    	// if bundle requirement ids are present on the requirement control source
    	if (wfIdList.size() > 0) { // SPR3290
    		//NBA213 deleted code
    		//create ans set retrive option
    		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
    		retOpt.setWorkItem(getWork().getID(), false);
    		retOpt.setLockWorkItem();
    		retOpt.requestSources();
    		retOpt.setAutoSuspend();
    		//get dst list
    		bundleReqArray = retrieveWorkItemList(getUser(), retOpt, wfIdList);  //NBA213 SPR3290
    		//add list to unlock list
    		unlockList.addAll(bundleReqArray);
    		//NBA213 deleted code

    		// process the all bundle requirements
    		return processBundleRequirements();
    	}
    	return true;
    }*/
	
	/**
	 * Updates the RequirementInfoExtension to set FollowUpDate 
	 * @param reqInfoExt RequirementInfoExtension which has to be updated
	 */
	 //QC#9379 APSL2352 New Method  
	private Date getFollowUpDate(RequirementInfoExtension reqInfoExt) throws NbaBaseException {
		int followupDays = 0;
		if (reqInfoExt.hasFollowUpFreq()) {
			followupDays = reqInfoExt.getFollowUpFreq(); //get the stored value or the latest updated value
		}
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_WEEK, followupDays);
		reqInfoExt.setFollowUpDate(calendar.getTime());
		reqInfoExt.setActionUpdate();
		return reqInfoExt.getFollowUpDate();
	}
	
	// APSL4165 New Method
	public void handleErrorStatus() {
		try {
			suspendAWD();
			unlockAWD();
		} catch (NbaBaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	// APSL4902 new method
	protected boolean isContractPrintDone(String polNumber) throws NbaBaseException {
		boolean printDone = false;
		NbaSearchVO searchPrintVO = searchWI(NbaConstants.A_WT_CONT_PRINT_EXTRACT, polNumber);
		if (searchPrintVO != null && searchPrintVO.getSearchResults() != null && !searchPrintVO.getSearchResults().isEmpty()) {
			List searchResultList = searchPrintVO.getSearchResults();
			for (int i = 0; i < searchResultList.size(); i++) {
				NbaSearchResultVO searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
				if ((searchResultVo.getQueue().equalsIgnoreCase(END_QUEUE) || searchResultVo.getQueue().equalsIgnoreCase(A_QUEUE_POST_ISSUE))) {
					printDone = true;
					break;
				}
			}
		}
		return printDone;
	}

	// APSL4902 new method
	protected NbaSearchVO searchWI(String workType, String policyNumber) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName("NbaSearchResultVO");
		searchVO.setWorkType(workType);
		searchVO.setContractNumber(policyNumber);
		searchVO = lookupWork(getUser(), searchVO);
		return searchVO;
	}

	// APSL4902 new method
	protected boolean isDeliveryReq(long reqCode) {
		boolean isDeliveryReq = false;
		if (reqCode == NbaOliConstants.OLI_REQCODE_STMTGOODHEALTH || reqCode == NbaOliConstants.OLI_REQCODE_PREMIUMQUOTE
				|| reqCode == NbaOliConstants.OLI_REQCODE_SIGNILLUS || reqCode == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT
				|| reqCode == NbaOliConstants.OLI_REQCODE_PREMDUE || reqCode == NbaOliConstants.OLI_REQCODE_AMENDMENT) {
			isDeliveryReq = true;
		}
		return isDeliveryReq;
	}

	}
