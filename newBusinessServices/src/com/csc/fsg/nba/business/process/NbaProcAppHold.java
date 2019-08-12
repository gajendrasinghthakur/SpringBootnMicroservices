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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.csc.fs.accel.ui.AxaStatusDefinitionLoader;
import com.csc.fs.accel.util.SortingHelper;
import com.csc.fs.accel.valueobject.Comment;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaRequirementInfoException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.statusDefinitions.Status;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsAppHoldData;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsWorkItem;

/**
 * NbaProcAppHold is the class that processes nbAccelerator cases found
 * on the AWD Application Hold queue (NBAPPHLD). It reviews the data associated
 * with a case to determine if the case can be moved to the next queue or if it
 * fails the application hold process.  It invokes the VP/MS Application Hold model
 * to retrieve information about sources and work items to assist in the decision
 * making process.
 * <p>The NbaProcAppHold class extends the NbaAutomatedProcess class.  
 * Although this class may be instantiated by any module, the NBA polling class 
 * will be the primary creator of objects of this type.
 * When the polling process finds a case on the Application Hold queue, 
 * it will create an object of this instance and call the object's 
 * executeProcess(NbaUserVO, NbaDst) method.  This method will manage the steps 
 * necessary to evaluate the case and determine the next step for a case.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>SPR1018</td><td>Version 2</td><td>JavaDoc, comments and minor source code changes.</td></tr>
 * <tr><td>NBA021</td><td>Version 2</td><td>Data Resolver</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA033</td><td>Version 3</td><td>Companion Case and HTML Indexing Views</td></tr>
 * <tr><td>NBA087</td><td>Version 3</td><td>Post Approval & Issue Requirements</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>SPR1961</td><td>Version 4</td><td>AppHold automated process should ignore overriden validation messages.</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN010</td><td>Version 4</td><td>Evaluation Control Model</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr>
 * <tr><td>NBA119</td><td>Version 5</td><td>Automated Process Suspend</td></tr> 
 * <tr><td>NBA122</td><td>Version 5</td><td>Underwriter Workbench Rewrite</td></tr> 
 * <tr><td>NBA128</td><td>Version 5</td><td>Workflow Changes Project</td></tr> 
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirement/Reinsurance Changes</td></tr>
 * <tr><td>SPR2670</td><td>Version 6</td><td>Correction needed in Companion Case VP/MS model </td></tr>
 * <tr><td>SPR3223</td><td>Version 6</td><td>Application Hold Not Finding application form source</td></tr>
 * <tr><td>SPR2638</td><td>Version 7</td><td>Application Hold Automated Process Should Not Review Errors on Contract</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-1 </td><td>Version 7</td><td>Performance Tuning and Testing - Deferred History Retrieval</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>ALS5135</td><td>AXA Life Phase 1</td><td>QC # 4301  - AWD Error received on Application WI in App </td></tr>
 * <tr><td>ALNA219</td><td>AXA Life Phase 2</td><td>PERF - AppHold Improvement</td></tr>
 * <tr><td>ALII1816</td><td>Discretionary</td><td>Business Optimization - AppHold Suspend - User Notification</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 * @see NbaAutomatedProcess
 */

public class NbaProcAppHold extends NbaAutomatedProcess {
	/** The suspend value object to send the require parameters to suspend a case */
	public com.csc.fsg.nba.vo.NbaSuspendVO suspendVO;
	protected static final String UNRESOLVED_CONTACT_VALIDATION_ERRORS = "Unresolved Contract Validation errors are present"; //NBLXA-1954

	/** The maximum severity constant */
	public final static int APP_HOLD_SEVERITY_MAXIMUM = 1;
	/** The OINK data access object used to retrieve the values from NbaTXLife or NbaLob objects using predefined variable names */	
	private com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess oinkData;
	/** The Vpms adaptor object which is the interface to the VPMS system */
	private com.csc.fsg.nba.vpms.NbaVpmsAdaptor vpmsProxy = null;
	//SPR2380 removed logger
/**
 * This constructor calls the superclass constructor which will set
 * the appropriate statues for the process.
 */
public NbaProcAppHold() {
	super();
}
/**
 * This method drives the application hold process.
 * <P>After obtaining a reference to the NbaNetServerAccessor EJB
 * and retrieving the statuses to be used for the process through 
 * user of the superclass' initialize method, it retrieves the 
 * Holding Inquiry for the case and updates the case's NbaLob. 
 * The NbaLob is then used to instantiate the NbaVpmsVO object 
 * that will be used for calls to the Application Hold VPMS model.
 * <P>The process verifies that the severity of the holding inquiry
 * does not exceed limits established for Application Hold.  If it does
 * the case fails Application Hold and is routed to a fail queue.
 * <P>Next, it invokes the verifySources method to ensure that all
 * required sources for the case are present. If not, the case is 
 * suspend for a specific number of days.
 * <P>The third check determines if work items present on the case
 * are in the correct status and/or have correct values.  If not,
 * the case is again suspended.
 * After these checks are made and processed, an <code>NbaAutomatedProcessResult</code>
 * is created and populated with values used by the polling process to
 * determine the success or failure of the case.  The case is then updated
 * to move it to the next queue and the result returned to the poller.
 * @param user  the user for whom the work was retrieved, in this case APAPPHLD.
 * @param work  the AWD case to be processed
 * @return NbaAutomatedProcessResult containing the results of the process
 * @throws NbaBaseException 
 */
public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
	// NBA027 - logging code deleted
	if (!initialize(user, work)) {
		return getResult(); // NBA050
	}
	try {
	    //SPR2638 code deleted
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), true);
		retOpt.requestTransactionAsChild();
		retOpt.requestSources();
		//NBA208-1 code deleted
		retOpt.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retOpt));  //NBA213
		
		if(getNbaTxLife().getPolicy().getPolicyStatus() != NbaOliConstants.OLI_POLSTAT_ISSUED) { //ALII2017
			sendUserNotificationWork(); //ALII1816
		} //ALII2017
		if(result == null) { //ALII1816
		
			oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			oinkData.setContractSource(nbaTxLife); //NBA050
			
			//NBA077 code deleted
			// Begin NBA130
			try {
			    if (!verifySources() || !verifyWorkItems()) {
			        return result;
			    }
			} catch (NbaRequirementInfoException nrie) {
			    addComment(nrie.getMessage());
			    setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Application Hold Failed", getHostErrorStatus()));
				changeStatus(getResult().getStatus());
				doUpdateWorkItem();
				return result;
			}
			// End NBA130
			//begin NBA033			
			NbaCompanionCaseRules rules = new NbaCompanionCaseRules(user, getWork());
			if (rules.isSuspendNeeded(NbaVpmsConstants.APPLICATION_HOLD)) { //SPR2670
				if(rules.isSuspendDurationWithinLimits()){
					addComment("Work Item suspended: Waiting for other companion cases");
					setSuspendVO(rules.getSuspendVO());
					updateForSuspend();	
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
				} else {
					addComment("Suspend limit exceeded.");
					changeStatus(getFailStatus());
					doUpdateWorkItem();
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Error", getFailStatus()));
				}
			} else {				
				//end NBA033
				changeStatus(getPassStatus());				
				resetLobFields();
				getWork().getNbaLob().setAppHoldSuspDate(null); //NBA033 reset this LOB 
				doUpdateWorkItem();
				setPolicyStatus(); // NBLXA-1782
				result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus());
			}	
		} //ALII1816
			
		return result;
		} catch (NbaAWDLockedException le) {//ALII1652
			unlockWork();
			throw le;
		} catch (NbaBaseException nbex) { //ALS5135
			throw nbex; //ALS5135
		} catch (Exception e) {
			NbaBaseException nbe = new NbaBaseException("An exception occured during Application Hold process", e);
			throw nbe;
		}
	}

//NBLXA-1782 New Method
	protected void setPolicyStatus() throws NbaBaseException {
		NbaTXLife nbaTxLife = getNbaTxLife();
		if (!NbaUtils.isBlankOrNull(nbaTxLife.getPolicy())) {
			PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
			if (policyExtn != null) {
				long adminSysPolicyStatus = policyExtn.getAdminSysPolicyStatus();
				if (adminSysPolicyStatus == NbaOliConstants.OLI_POLSTAT_ACTIVE && getPassStatus() != null
						&& getPassStatus().equalsIgnoreCase(getWork().getNbaLob().getArchivedStatus())) {
					nbaTxLife.getPolicy().setPolicyStatus(NbaOliConstants.OLI_POLSTAT_ISSUED); 
					nbaTxLife.getPolicy().setActionUpdate();
					setContractAccess(UPDATE);
					doContractUpdate();
				}

			}
		}
	}
	// APSL5370 New Method
	protected void routeWorkItem() {
		NbaTXLife nbaTxLife = getNbaTxLife();
		if (!NbaUtils.isBlankOrNull(nbaTxLife.getPolicy())) {
			PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
			if (policyExtn != null) {
				long adminSysPolicyStatus = policyExtn.getAdminSysPolicyStatus();
				switch ((int) adminSysPolicyStatus) {
				case (int) NbaOliConstants.OLI_POLSTAT_TERMINATE:
					Status passStatus = AxaStatusDefinitionLoader.determinePassStatus("N2ISSUE", null);
					getWork().setStatus(passStatus.getStatusCode());
					getWork().getNbaLob().setRouteReason(NbaUtils.getRouteReason(getWork(), getWork().getStatus()));
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getWork().getStatus()));

					break;
				case (int) NbaOliConstants.OLI_POLSTAT_ACTIVE:
					getWork().setStatus(getWork().getNbaLob().getArchivedStatus());
					getWork().getNbaLob().setRouteReason(NbaUtils.getRouteReason(getWork(), getWork().getStatus()));
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getWork().getStatus()));
					break;
				default:
					System.out.println("Case Should not be suspended");

				}

			}
		}
	}

//ALII1998 New method
public boolean existAllReqInENDQue() throws NbaBaseException {
	ListIterator li = getWork().getNbaTransactions().listIterator();
	while (li.hasNext()) {
		NbaTransaction trans = (NbaTransaction) li.next();
		if( A_WT_REQUIREMENT.equalsIgnoreCase(trans.getTransaction().getWorkType())) {
			if (!trans.getQueue().equalsIgnoreCase(END_QUEUE)) {
				return false;
			}
		}
	}
	return true;
}

//ALII1816 New method
	public void sendUserNotificationWork() throws NbaBaseException {
		NbaTXLife nbaTxLife = getNbaTxLife();
		// ONLY Agent CV present, Verify if Agent Licence WI present on the case. If yes, pull the WI and route it to Agent Licence CM. If not, create
		// a WI and send it to the Agent LCM.
		if (isSevereAgentCVExists()) {
			createAgentLicWorkItem();
			Map deOink = new HashMap();
			deOink.put("A_AgentLicErrorPresent", "true");
			NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), nbaTxLife, deOink);
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Agent CV present on case.", statusProvider.getPassStatus())); // Move
																				// case
																																							// to
																																							// N2CMHLD
			changeStatus(getResult().getStatus());
			addComment("Case sent to CM HOLD queue since Agent Licensing CV present on the case.");
		}

		// If (5002 CV and CWAAmt > 0) OR Any Severe. overridable CV present on the case (other than 5002 CV and Agent CV),
		// then Verify if Validation WI is present . If yes ,verify if it is aggregated to UWCM. If not, pull the WI and aggregate to UW.
		// if no Validation WI is present then create a new one and send it to UWCM .
		if (existAllReqInENDQue() && (isSevereNonAgentCVExists() && !only1778CVonCase())&& !checkCV1778forUnboundCase()) { // ALII1998
			createValErrorWorkItem();
			Map deOink = new HashMap();
			deOink.put("A_ValidationErrorPresent", "true");
			NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), nbaTxLife, deOink);

			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Severe or Non-overridden CV present on case..",
					statusProvider.getPassStatus()));
			changeStatus(getResult().getStatus());
			addComment("Case sent to CM HOLD queue since Validation CV present on the case.");
		}else if(checkCV1778forUnboundCase()){
			suspendCaseForRegisterDate();
		}

		// If Replacement Notification WI not in END queue and is in Replacement Hold queue- send it to RPCM with priority. b. If it is in ERROR
		// queue- keep it in same queue.
		NbaTransaction rplcNotifTrans = getRplcNotifNotInEnd(A_WT_REPL_NOTIFICATION);
		if (rplcNotifTrans != null) { // ALII1906 Code Cropped
			Map deOink = new HashMap();
			deOink.put("A_RplcNotifNotInEND", "true");
			NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOink);
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(rplcNotifTrans.getID(), false);
			retOpt.setLockWorkItem();
			NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
			if (rplcNotifTrans.getQueue().equalsIgnoreCase(PROC_REPL_PROCESSING)) {
				setPriorityandUpdate(aWorkItem, workProvider); // ALII1906 Redundant Code Moved to Method
			} else { // ALII1998
				setPriorityOnly(aWorkItem, workProvider); // ALII1998
			}
			NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), nbaTxLife, deOink);
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Replacement Notification WI not in END queue.",
					statusProvider.getPassStatus())); // Move case to N2CMHLD
			changeStatus(getResult().getStatus());
			addComment("Case sent to CM HOLD queue since Replacement Notification workitem is not in END queue.");
			// deleted code - ALII1998
		}

		// If Requirements in database are not in sync up with AWD status, move the case to NBERROR queue.
		if (isReqOutofSynch()) {
			// Code deleted for APSL3874
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_REQ_SYNCH); // APSL3874
		}
		if (result != null) {
			doUpdateWorkItem();
		}
	}

//ALII1816 New method
protected boolean isSevereAgentCVExists() {
	SystemMessage sysMessage;
	SystemMessageExtension systemMessageExtension;
	ArrayList messages = getNbaTxLife().getPrimaryHolding().getSystemMessage();
	for (int i = 0; i < messages.size(); i++) {
		sysMessage = (SystemMessage) messages.get(i);
		if ((sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_SEVERE
				|| sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE)//NBLXA-2280
				&& (sysMessage.getMessageCode() != MESSAGECODE_AGENTLIC_WI)){//APSL4234/QC15120
			systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage); 
			if (systemMessageExtension != null && systemMessageExtension.getMsgValidationType() == SUBSET_AGENT
					&& !systemMessageExtension.getMsgOverrideInd()) {
				return true;
			}
		}
	}
	return false;
}

//ALII1816 New method
protected boolean isSevereNonAgentCVExists() {
	SystemMessage sysMessage;
	SystemMessageExtension systemMessageExtension;
	ArrayList messages = getNbaTxLife().getPrimaryHolding().getSystemMessage();
	for (int i = 0; i < messages.size(); i++) {
		sysMessage = (SystemMessage) messages.get(i);
		if (sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_SEVERE
				|| sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE) { 
			systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage); 
			if (systemMessageExtension != null && systemMessageExtension.getMsgValidationType() != SUBSET_AGENT
					&& !systemMessageExtension.getMsgOverrideInd() && getNbaTxLife().getPolicy().getApplicationInfo().getCWAAmt() > 0) { //ALII1909
				return true;
			}
		}
	}
	return false;
}

//ALII1816 New method
protected void createValErrorWorkItem() throws NbaBaseException {
	Map deOink = new HashMap();
	deOink.put("A_ErrorSeverity", Long.toString(NbaOliConstants.OLI_MSGSEVERITY_SEVERE));
	deOink.put("A_CreateValidationWI", "true");
	NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOink);
	NbaTransaction validationTransaction = getTransaction(workProvider.getWorkType());
	if(validationTransaction==null){
		getWork().addTransaction(workProvider.getWorkType(), workProvider.getInitialStatus());
	} else{
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(validationTransaction.getID(), false);
		retOpt.setLockWorkItem();
		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
		setPriorityandUpdate(aWorkItem,workProvider); //ALII1906 Redundant Code Moved to Method
	}
}

//ALII1816 New method
protected void createAgentLicWorkItem() throws NbaBaseException {
	Map deOink = new HashMap();
	// NBLXA-1337 -- Check For Licensing WI
	NbaTransaction nbaTrans =null; //NBLXA-1337
	String appendRoutReason = NbaUtils.getAppendReason(getNbaTxLife());
	retrieveLicWorkItem(getNbaTxLife());
	if(licensingworkExists == false){
		deOink.put("A_CreateAgentLicWI", "true");
		NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOink);
		nbaTrans = getWork().addTransaction(workProvider.getWorkType(), workProvider.getInitialStatus());
		if(nbaTrans !=null){
			nbaTrans.getNbaLob().setRouteReason(nbaTrans.getNbaLob().getRouteReason()+" "+appendRoutReason);
		}
		
	}else if(licensingworkExists == true){
		if(licensingworkInEndQueue == true && searchResultForLicWIVO !=null){
			deOink.put("A_CreateAgentLicWIFromAppHold", "true");
			retrieveExisitngLicensingWIFromEndQueue(getWork(),getNbaTxLife(),deOink);
			
		}
	}
	// NBLXA-1337 -- END
	
	
	/*if(agentLicTransaction==null){
		NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOink);
		NbaTransaction agentLicTransaction = getTransaction(workProvider.getWorkType());
		getWork().addTransaction(workProvider.getWorkType(), workProvider.getInitialStatus());
	} else{
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(agentLicTransaction.getID(), false);
		retOpt.setLockWorkItem();
		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
		setPriorityandUpdate(aWorkItem,workProvider); //ALII1906 Redundant Code Moved to Method
	}*/
}

//ALII1816 New method
protected NbaTransaction getTransaction(String workType) throws NbaBaseException {
	List transactions = getWork().getNbaTransactions();
	int count = transactions.size();
	NbaTransaction nbaTransaction = null;
	for (int i = 0; i < count; i++) {
		nbaTransaction = (NbaTransaction) transactions.get(i);
		if (workType.equalsIgnoreCase(nbaTransaction.getWorkType())) {					
			return nbaTransaction;
		}
	}
	return null;
}	

//ALII1816 New method
protected NbaTransaction getRplcNotifNotInEnd(String workType) throws NbaBaseException {
	List transactions = getWork().getNbaTransactions();
	int count = transactions.size();
	NbaTransaction nbaTransaction = null;
	for (int i = 0; i < count; i++) {
		nbaTransaction = (NbaTransaction) transactions.get(i);
		if (workType.equalsIgnoreCase(nbaTransaction.getWorkType()) && !nbaTransaction.isInEndQueue()) {					
			return nbaTransaction;
		}
	}
	return null;
}	

//ALII1816 New method
public boolean isReqOutofSynch() throws NbaBaseException {
	int reqCount = 0;
	ListIterator li = getWork().getNbaTransactions().listIterator();
	RequirementInfo reqInfo = null;
	while (li.hasNext()) {
		NbaTransaction trans = (NbaTransaction) li.next();
		if( A_WT_REQUIREMENT.equalsIgnoreCase(trans.getTransaction().getWorkType())) {
			reqInfo = getNbaTxLife().getRequirementInfo(trans.getNbaLob().getReqUniqueID());
			if (null == reqInfo) {
				return true;
			}
			reqCount++;
		}
	}
	if(reqCount != getNbaTxLife().getPolicy().getRequirementInfoCount()) {
		return true;
	}
	return false;
}

///NBA103 - removed method

/**
 * Insert the method's description here.
 * @return com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 */
public com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess getOinkData() {
	return oinkData;
}
/**
 * Answers the NbaSuspendVO created for the case.
 * @return NbaSuspendVO a populated suspend value object
 */
public NbaSuspendVO getSuspendVO() {
	return suspendVO;
}
/**
 * This method resets certain LOB fields to default values when
 * a case successfully passes the Application Hold process.
 * @throws NbaBaseException
 */
public void resetLobFields() throws NbaBaseException {
	if (getWork().getNbaLob().getNeedApplication() == true) {
		getWork().getNbaLob().setNeedApplication(false);
	}
	if (getWork().getNbaLob().getNeedCompWorkItem() == true) {
		getWork().getNbaLob().setNeedCompWorkItem(false);
	}
	if (getWork().getNbaLob().getNeedCwa() == true) {
		getWork().getNbaLob().setNeedCwa(false);
	}
}
/**
 * Insert the method's description here.
 * @param newOinkData com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 */
public void setOinkData(com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess newOinkData) {
	oinkData = newOinkData;
}
/**
 * Sets the Nba suspend value object.
 * @param newSuspendVO
 */
public void setSuspendVO(NbaSuspendVO newSuspendVO) {
	suspendVO = newSuspendVO;
}
/**
 * This method will determine if the activate date time is exceeding allowable suspend time (i.e.
 * Initial suspend date in APHL LOB and the maximun suspend date from SXSD LOB). If activate date
 * time exceed this time than workitem routed to error queue else it is suspended for the given time.
 * It checks if SXSD LOB is not null if it is null than it will set it with maxSuspendDays. 
 * It also checks whether APHL LOB is initialized or not, if not initialzed it with the currect date.
 * @param activateDateTime the activate date time
 * @param reason the reason the case is being suspended
 * @param maxAllowableDays the maximum allowable suspend days
 * @throws NbaBaseException
 */
//NBA119 added new parameter maxAllowableDays. Also changed type of activateDateTime
//NBLXA-1954 Method overloaded
  public void suspendCase(Date activateDateTime, String reason, int maxAllowableDays,NbaTransaction transaction) throws NbaBaseException {//NBA119
	getLogger().logDebug("Starting suspendCase"); //NBA044
	
	//	begin NBA119
	NbaLob lob = getWork().getNbaLob();
	StringBuffer newReason = new StringBuffer();
	newReason.append("Case suspended due to ");
	newReason.append(reason);
	if (isDifferentReason(newReason.toString())) {
		lob.setMaxNumSuspDays(maxAllowableDays);
		getWork().setUpdate();
	}
	//end NBA119
	Date appHoldSusDate = getWork().getNbaLob().getAppHoldSuspDate();
	if (appHoldSusDate == null) {
		appHoldSusDate = new Date();
		lob.setAppHoldSuspDate(appHoldSusDate); //NBA119
		getWork().setUpdate();
	}
	GregorianCalendar calendar = new GregorianCalendar();
	calendar.setTime(appHoldSusDate);
	//	begin NBA119
	int maxSuspendDays = lob.getMaxNumSuspDays();
	if (-1 == maxSuspendDays) {
		maxSuspendDays = maxAllowableDays;
		lob.setMaxNumSuspDays(maxSuspendDays);
		getWork().setUpdate();
	}
	calendar.add(Calendar.DAY_OF_WEEK, maxSuspendDays); //NBA027
	//end NBA119
	
	Date maxSuspendDate = (calendar.getTime());
	if (maxSuspendDate.before(new Date())) { //NBA119
		addComment("Case cannot be suspended. Suspend limit exceeded."); //NBA119
		lob.deleteAppHoldSuspDate(); //NBA119
		changeStatus(getFailStatus());
		doUpdateWorkItem();
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Error", getFailStatus()));
		return;
	}
	//NBA020 code deleted
	suspendVO = new NbaSuspendVO();
	suspendVO.setCaseID(getWork().getID());

	//	NBA119 code deleted
	  suspendVO.setActivationDate(adjustSuspendTime(activateDateTime)); //NBA119	
	  addComment(newReason.toString()); //NBA119
	  setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
	if (reason.equalsIgnoreCase(UNRESOLVED_CONTACT_VALIDATION_ERRORS)) {
		updateForSuspend();
	}
    else {
		updateForSuspend(getTransactionDst(transaction)); // NBLXA-1954
	}
}

  public void suspendCase(Date activateDateTime, String reason, int maxAllowableDays) throws NbaBaseException {//NBA119
		getLogger().logDebug("Starting suspendCase"); //NBA044
		
		//	begin NBA119
		NbaLob lob = getWork().getNbaLob();
		StringBuffer newReason = new StringBuffer();
		newReason.append("Case suspended due to ");
		newReason.append(reason);
		if (isDifferentReason(newReason.toString())) {
			lob.setMaxNumSuspDays(maxAllowableDays);
			getWork().setUpdate();
		}
		//end NBA119
		Date appHoldSusDate = getWork().getNbaLob().getAppHoldSuspDate();
		if (appHoldSusDate == null) {
			appHoldSusDate = new Date();
			lob.setAppHoldSuspDate(appHoldSusDate); //NBA119
			getWork().setUpdate();
		}
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(appHoldSusDate);
		//	begin NBA119
		int maxSuspendDays = lob.getMaxNumSuspDays();
		if (-1 == maxSuspendDays) {
			maxSuspendDays = maxAllowableDays;
			lob.setMaxNumSuspDays(maxSuspendDays);
			getWork().setUpdate();
		}
		calendar.add(Calendar.DAY_OF_WEEK, maxSuspendDays); //NBA027
		//end NBA119
		
		Date maxSuspendDate = (calendar.getTime());
		if (maxSuspendDate.before(new Date())) { //NBA119
			addComment("Case cannot be suspended. Suspend limit exceeded."); //NBA119
			lob.deleteAppHoldSuspDate(); //NBA119
			changeStatus(getFailStatus());
			doUpdateWorkItem();
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Error", getFailStatus()));
			return;
		}
		//NBA020 code deleted
		suspendVO = new NbaSuspendVO();
		suspendVO.setCaseID(getWork().getID());

		//	NBA119 code deleted
		  suspendVO.setActivationDate(adjustSuspendTime(activateDateTime)); //NBA119	
		  addComment(newReason.toString()); //NBA119
		  setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
		  updateForSuspend();
	}
  
  
  
  
/**
 * Since the case must be suspended before it can be unlocked, this method
 * is used instead of the superclass method to update AWD.
 * <P>This method updates the case in the AWD system, suspends the 
 * case using the supsendVO, and then unlocks the case.
 * @throws NbaBaseException
 */
public void updateForSuspend() throws NbaBaseException {
	
	getLogger().logDebug("Starting updateForSuspend");//NBA044
	
	//begin NBA213
	getWork().getWorkItem().setWorkItemChildren(new ArrayList());//ALNA219
	updateWork(getUser(), getWork());
	suspendWork(getUser(), getSuspendVO());
	unlockWork(getUser(), getWork());
	//end NBA213
}

//Begin NBLXA-1954 Method overloaded
public void updateForSuspend(NbaDst transaction) throws NbaBaseException {
	
	getLogger().logDebug("Starting updateForSuspend transaction");//NBA044
	
	//begin NBA213
	getWork().getWorkItem().setWorkItemChildren(new ArrayList());//ALNA219
	updateWork(getUser(), transaction);
	updateWork(getUser(), getWork());
	suspendWork(getUser(), getSuspendVO());
	unlockWork(getUser(), getWork());
	//end NBA213
}
// End NBLXA-1954

// Change suspend duration for paid re-issue/Issued policies QC16262
public Date adjustSuspendTime(Date activateDateTime) {
	boolean isPaidReissue = getNbaTxLife().isPaidReIssue();
	ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
	if(isPaidReissue || (appInfoExt!= null && appInfoExt.getIssuedToAdminSysInd() ) ) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.HOUR,12);
		return calendar.getTime();
	} else {
		return activateDateTime;
	}	
}

//SPR2638 code deleted
/**
 * This method verifies that the source(s) required for an application
 * are present within the AWD system.  If not, then the case will be
 * suspended for a set number of days.
 * @return boolean true indicates sources present and verified; false indicates
 *                 failure and that the case should be suspended
 * @throws NbaBaseException
 */
public boolean verifySources() throws NbaBaseException {
	getLogger().logDebug("Starting verifySources"); //NBA044
	// get the sources needed from VPMS
	try {
		vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.APPLICATION_HOLD); // NBA077
		vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_SOURCES);
		//Begin ACN010
		Map deOinkMap = new HashMap();
		deOinkMap.put("A_INSTALLATION", getInstallationType());
		deOinkMap.put(A_UNDERWRITER_WORKBENCH_APPLET, Boolean.toString(NbaConfiguration.getInstance().isUnderwriterWorkbenchApplet()));  //NBA122
		vpmsProxy.setSkipAttributesMap(deOinkMap);
		//End ACN010
		NbaVpmsAppHoldData data = new NbaVpmsAppHoldData(vpmsProxy.getResults());
		// data contains the required sources for an application
		// if a source is not present in the list of Nba Sources, then it fails and we
		// suspend for the set number of days.
		for (int i = 0; i < data.getWorkItems().size(); i++) {
			boolean sourcePresent = false;
			NbaVpmsWorkItem vpmsItem = (NbaVpmsWorkItem) data.getWorkItems().get(i); //NBA119
			// for each work item, see if it is present in the list of sources for this
			List sources = getWork().getNbaCase().getAllNbaSources();  //SPR3223
			for (int j = 0; j < sources.size(); j++) {
				NbaSource aSource = (NbaSource) sources.get(j);
				if (aSource.getSource().getSourceType().equals(vpmsItem.getSource())) { //NBA119, NBA128, SPR3290
					sourcePresent = true;
					break;
				}
			}
			if (!sourcePresent) {
				getWork().getNbaLob().setNeedApplication(true); //ALII1816
				// If Application Source is not present, Aggregate Contract WI will be created and sent to UWCM. 
				if(vpmsItem.getSource() != null && vpmsItem.getSource().equalsIgnoreCase(A_ST_APPLICATION)){ //ALII1816
					if (getLogger().isDebugEnabled()) { 
						getLogger().logDebug("Case aggregated for UWCM since Application Source was not present."); 
					} 
					Map deOink = new HashMap();
					deOink.put("A_AppSourceMissing", "true");
					NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), nbaTxLife, deOink);
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Application Source is missing.", statusProvider.getPassStatus()));
					addComment("Case aggregated for UWCM since Application Source was not present."); 
					changeStatus(statusProvider.getPassStatus());
					getWork().setUpdate();
					update(getWork());
				} else { //ALII1816
					if (getLogger().isDebugEnabled()) { // NBA027
						getLogger().logDebug("Suspend for " + vpmsItem.getActivateDateTime()); //NBA119
					} // NBA027
					suspendCase(vpmsItem.getActivateDateTime(), vpmsItem.getSource() + " not present", vpmsItem.getMaxSuspendDays()); //NBA119, NBA128
					//SPR3362 code deleted
				}
				return false;
			}
		}
		//begin NBA077
		ListIterator li = getWork().getNbaTransactions().listIterator();
		while (li.hasNext()) {
			NbaTransaction trans = (NbaTransaction) li.next();
			oinkData = new NbaOinkDataAccess(trans.getNbaLob());
			vpmsProxy.setOinkSurrogate(oinkData);
			data = new NbaVpmsAppHoldData(vpmsProxy.getResults());
			if (data.wasSuccessful()) {
				for (int i = 0; i < data.getWorkItems().size(); i++) {
					boolean sourcePresent = false;
					NbaVpmsWorkItem vpmsItem = (NbaVpmsWorkItem) data.getWorkItems().get(i); //NBA119
					// for each work item, see if it is present in the list of sources for this
					List sources = trans.getNbaSources();
					String source = ((NbaVpmsWorkItem) data.getWorkItems().get(i)).getSource(); //NBA119 SPR3290
					for (int j = 0; j < sources.size(); j++) {
						NbaSource aSource = (NbaSource) sources.get(j);
						if (aSource.getSource().getSourceType().equals(source)) {
							sourcePresent = true;
							break;
						}
					}
					if (!sourcePresent) {
						//If case is Reissue case and Contract Change Source is not present, Aggregate Contract WI will be created and sent to PCCM. 
						if(vpmsItem.getSource() != null && vpmsItem.getSource().equalsIgnoreCase(A_ST_CHANGE_FORM)){ //ALII1816
							if (getLogger().isDebugEnabled()) { 
								getLogger().logDebug("Case sent to PCCM since Contract Change Form Source was not present."); //ALII1816 
							} 
							Map deOink = new HashMap();
							deOink.put("A_CntChgSourceMissing", "true");
							NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), nbaTxLife, deOink);
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Contract Change Form Source is missing.", statusProvider.getPassStatus()));
							addComment("Case sent to PCCM since Contract Change Form Source was not present."); //ALII1816 
							changeStatus(statusProvider.getPassStatus());					
							getWork().setUpdate();
							update(getWork());
						} else { //ALII1816
							if (getLogger().isDebugEnabled()) { // NBA027
								getLogger().logDebug("Suspend for " + ((NbaVpmsWorkItem) data.getWorkItems().get(i)).getActivateDateTime());
							}// NBA027
							suspendCase(vpmsItem.getActivateDateTime(), vpmsItem.getWorkItem() + " not present", vpmsItem.getMaxSuspendDays()); //NBA119
							//SPR3362 code deleted
						}
						return false;
					}
					// APSL4813 :: START -- APP WI should not move and suspend until CWA is not END Queue
					if( A_WT_CWA.equalsIgnoreCase(trans.getTransaction().getWorkType()))
					{
						if (!END_QUEUE.equalsIgnoreCase(trans.getQueue())) {
							suspendCase(vpmsItem.getActivateDateTime(), trans.getTransaction().getWorkType(), vpmsItem.getMaxSuspendDays(),trans); //NBA119,NBLXA-1954
							return false;
						}
					}
					// APSL4813 :: END
				}
			}
		}
		//SPR3362 code deleted
		//end NBA077
		return true;
	} catch (Exception e) {
		NbaBaseException nbe = new NbaBaseException("AppHold problem", e);
		throw nbe;
	} catch (Throwable t) {
		throw new NbaBaseException("AppHold problem", t);
		//SPR3362 code deleted
	} finally {
		try {
		    if (vpmsProxy != null) {
				vpmsProxy.remove();					
			}
		} catch (Exception e) {
			getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //Ignoring the exception SPR3362
		}
	}
	//end SPR3362
}
/**
 * This method verifies that the workitem(s) required for an application
 * are present within the AWD system.  If not, then the case will be
 * suspended for a set number of days.
 * @return boolean true indicates work item(s) present and verified; false indicates
 *                 failure and that the case should be suspended
 * @throws NbaBaseException
 */
public boolean verifyWorkItems() throws NbaBaseException {
	getLogger().logDebug("Starting verifyWorkItems"); //NBA044
	try {
		ListIterator li = getWork().getNbaTransactions().listIterator();
		vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.APPLICATION_HOLD); // NBA077
		//begin NBA087
		Map deOinkMap = new HashMap();
		deOinkMap.put("A_CaseStatusLOB", getWork().getStatus());
		deOinkMap.put("A_INSTALLATION", getInstallationType());//ACN010
		deOinkMap.put(A_UNDERWRITER_WORKBENCH_APPLET, Boolean.toString(NbaConfiguration.getInstance().isUnderwriterWorkbenchApplet()));  //NBA122
		//Begin QC9347/ALII1411
		deOinkMap.put("A_CaseFinalDispstnLOB", Integer.toString(getWork().getNbaLob().getCaseFinalDispstn()));
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		oinkData.setContractSource(getNbaTxLife());
		oinkRequest.setVariable("Exchange1035Ind"); 
		String exchange1035Ind = oinkData.getStringValueFor(oinkRequest);
		deOinkMap.put("A_Exchange1035Ind", exchange1035Ind);
		//End QC9347/ALII1411
		vpmsProxy.setSkipAttributesMap(deOinkMap);
		//end NBA087
		vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_WORKITEMS);
		NbaVpmsAppHoldData data = null;
		//Begin NBA130
		RequirementInfo reqInfo = null;
		vpmsProxy.setANbaOinkRequest(oinkRequest);
		//End NBA130
		List contractPrintWIsList= new java.util.ArrayList(); //APSL221
		while (li.hasNext()) {
			NbaTransaction trans = (NbaTransaction) li.next();
			if(A_WT_CONT_PRINT_EXTRACT.equalsIgnoreCase(trans.getTransaction().getWorkType())) { //APSL221
				contractPrintWIsList.add(trans);//APSL221
			} else { //APSL221
				oinkData = new NbaOinkDataAccess(trans.getNbaLob());  // NBA021
				//Begin NBA130
				if( A_WT_REQUIREMENT.equalsIgnoreCase(trans.getTransaction().getWorkType())) {
				    reqInfo = getNbaTxLife().getRequirementInfo(trans.getNbaLob().getReqUniqueID());
				    if (null == reqInfo) {
						throw new NbaRequirementInfoException("Unable to retrieve RequirementInfo for RequirementInfoUniqueId " + trans.getNbaLob().getReqUniqueID());
					}
				    oinkRequest.setRequirementIdFilter(reqInfo.getId());
				    oinkData.setContractSource(getNbaTxLife());
				} else {
				    oinkRequest.setRequirementIdFilter(null);
				}
				//End NBA130
				vpmsProxy.setOinkSurrogate(oinkData);                 // NBA021
				data = new NbaVpmsAppHoldData(vpmsProxy.getResults());
				if ((data.wasSuccessful()&& !only1778CVonCase()) && !checkCV1778forUnboundCase()) { //NBLXA-1317 -- Check if CV1778 present on case
					getWork().getNbaLob().setNeedCompWorkItem(true);
					NbaVpmsWorkItem vpmsItem = (NbaVpmsWorkItem) data.getWorkItems().get(0); //NBA119
					if (A_WT_CWA.equals(vpmsItem.getWorkItem()) || A_WT_CwA1035.equals(vpmsItem.getWorkItem())) { //NBA119
						getWork().getNbaLob().setNeedCwa(true);
					}
					if (!(A_WT_CWA.equals(vpmsItem.getWorkItem()) || A_WT_CwA1035.equals(vpmsItem.getWorkItem()))) { //APSL1540 QC#7611
						suspendCase(vpmsItem.getActivateDateTime(), vpmsItem.getWorkItem(), vpmsItem.getMaxSuspendDays(),trans); //NBA119 NBLXA-1954
						//SPR3362 code deleted
						return false;
					}
				}else if(checkCV1778forUnboundCase()){ //NBLXA-1317 -- Check if CV1778 present on case
						suspendCaseForRegisterDate();
									}
				}// APSL5370 Begin
				if (A_ST_INFORCE_UPDATE.equalsIgnoreCase(trans.getTransaction().getWorkType())
						&& !trans.getNbaLob().getQueue().equalsIgnoreCase(END_QUEUE)) {
					PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
					if (policyExt != null) {
						if (policyExt.getAdminSysPolicyStatus() == NbaOliConstants.OLI_POLSTAT_0) {
							deOinkMap.put("A_IsInforceEvaluation", "true");
						} else {
							deOinkMap.put("A_IsInforceEvaluation", "false");
						}
					}
					oinkData = new NbaOinkDataAccess(getNbaTxLife());
					oinkData.setContractSource(getNbaTxLife());
					oinkData.setLobSource(getWork().getNbaLob());
					vpmsProxy.setOinkSurrogate(oinkData);
					vpmsProxy.setSkipAttributesMap(deOinkMap);
					data = new NbaVpmsAppHoldData(vpmsProxy.getResults());
					if ((data.wasSuccessful() && !only1778CVonCase()) && !checkCV1778forUnboundCase()) { //NBLXA-1317 -- Check if CV1778 present on case
						NbaVpmsWorkItem vpmsItem = (NbaVpmsWorkItem) data.getWorkItems().get(0);
						suspendCase(vpmsItem.getActivateDateTime(), vpmsItem.getWorkItem(), vpmsItem.getMaxSuspendDays(),trans); //NBLXA-1954
						return false;
					}else if(checkCV1778forUnboundCase()){ //NBLXA-1317 -- Check if CV1778 present on case
						suspendCaseForRegisterDate();
					}
				}
				// APSL5370 End
		}
		//begin APSL221
		if (contractPrintWIsList.size() > 0) {	
			SortingHelper.sortData(contractPrintWIsList, true, SORT_BY_CREATEDATE);
			NbaTransaction trans = (NbaTransaction)contractPrintWIsList.get(contractPrintWIsList.size() - 1); //Get the NBPRTEXT WI latest by CRDA
			oinkData = new NbaOinkDataAccess(trans.getNbaLob());
			oinkRequest.setRequirementIdFilter(null);
			vpmsProxy.setOinkSurrogate(oinkData);
			data = new NbaVpmsAppHoldData(vpmsProxy.getResults());
			if ((data.wasSuccessful() && !only1778CVonCase()) && !checkCV1778forUnboundCase()) { //NBLXA-1317 -- Check if CV1778 present on case
				getWork().getNbaLob().setNeedCompWorkItem(true);
				NbaVpmsWorkItem vpmsItem = (NbaVpmsWorkItem) data.getWorkItems().get(0);
				suspendCase(vpmsItem.getActivateDateTime(), vpmsItem.getWorkItem(), vpmsItem.getMaxSuspendDays(),trans);
				return false;
			}else if(checkCV1778forUnboundCase()){ //NBLXA-1317 -- Check if CV1778 present on case
				suspendCaseForRegisterDate();
			}
		}
		//end APSL221
		//SPR3362 code deleted
		return true;
	} catch (Exception e) {
		NbaBaseException nbe = new NbaBaseException("AppHold problem", e);
		throw nbe;
	} catch (Throwable t) {
		throw new NbaBaseException("AppHold problem", t);
	//begin SPR3362
	} finally {
		try {
			if (null != vpmsProxy) {
				vpmsProxy.remove();
			}
		} catch (Exception e) {
			getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
		}
	}
	//end SPR3362

}
/**
 * Returns false if new suspend reason matches with the most recent reason on the workitem for this process. 
 * @param newReason the new suspend reason string
 * @return false if new suspend reason matches with the most recent reason on the workitem else return true
 */
//NBA119 New Method
protected boolean isDifferentReason(String newReason) {
    //begin NBA208-1
    if (!getWork().isHistoryRetrieved()) {
            try {
            	setWork(retrieveComments(getWork())); //SPRNBA-376
            } catch (NbaBaseException e) {
                return true; //No history
            }
        }
	List comments = getWork().getManualComments();
	//end NBA208-1
	//NBA208-32
	Comment comment = null;
	String commentText = null;
	String type = null;
	String typeString = null;
	NbaProcessingErrorComment errorComments = null;
	String userId = getUser().getUserID();
	for (int i = comments.size() - 1; i >= 0; i--) {
		//NBA208-32
		comment = (Comment) comments.get(i);
		commentText = comment.getText();
		type = commentText.length() >= 6 ? commentText.substring(0, 6) : "";//APSL649
		if ("type~|".equals(type)) {
			typeString = commentText.substring(type.length(), commentText.indexOf("|~"));
			if (NbaProcessingErrorComment.type.equalsIgnoreCase(typeString)) {
				errorComments = new NbaProcessingErrorComment(comment);
				//if matches user id and the reason text
				if (userId.equalsIgnoreCase(errorComments.getProcess()) && newReason.equalsIgnoreCase(errorComments.getText())) {
					return false;
				}
			}
		}
	}
	return true;
}
//ALII1906 New Method
	protected void setPriorityandUpdate(NbaDst aWorkItem, NbaProcessWorkItemProvider workProvider) throws NbaBaseException {
		if (workProvider.getWIPriority() != null) {
			aWorkItem.increasePriority(workProvider.getWIAction(), workProvider.getWIPriority());
		}
		changeStatus(aWorkItem, workProvider.getInitialStatus());
		aWorkItem.setUpdate();
		update(aWorkItem);
		unlockWork(aWorkItem);

	}
	
	//ALII1998 New Method
	protected void setPriorityOnly(NbaDst aWorkItem, NbaProcessWorkItemProvider workProvider) throws NbaBaseException {
		if (workProvider.getWIPriority() != null) {
			aWorkItem.increasePriority(workProvider.getWIAction(), workProvider.getWIPriority());
		}
		aWorkItem.setUpdate();
		update(aWorkItem);
		unlockWork(aWorkItem);

	}
    // New Method -- NBLXA-1317 -- Check CV for Unbound L70 Cases with Effective Date,when Effective is future Date.
	protected boolean checkCV1778forUnboundCase() {
		int com = 0;
		if (!NbaUtils.isBlankOrNull(getNbaTxLife().getPolicy())) {
			Policy policy = getNbaTxLife().getPolicy();
			PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
			if (policyExtn != null) {
				if (policyExtn.hasUnboundInd() && policyExtn.getUnboundInd() && SYST_LIFE70.equals(getNbaTxLife().getBackendSystem())) {
					if (only1778CVonCase()) {
						if (policy.getEffDate() != null) { 
							com = NbaUtils.compare(policy.getEffDate(), new Date());
						}
					}
				}

			}
		}
		if (com > 0) {
			return true;
		}

		return false;
	}
	 // New Method -- NBLXA-1317 --  When Only 1778 CV exist on case
	protected boolean only1778CVonCase() {
		SystemMessage systemMessage;
		Holding holding = getNbaTxLife().getPrimaryHolding();
		int count = holding.getSystemMessageCount();
		boolean isCVPresent = false;
		int severeCVCount = 0;
		for (int i = 0; i < count; i++) {
			systemMessage = holding.getSystemMessageAt(i);
			if (!systemMessage.isActionDelete()) {
				if (NbaOliConstants.OLI_MSGSEVERITY_SEVERE == systemMessage.getMessageSeverityCode()) {
					severeCVCount++;
					if (systemMessage.getMessageCode() == CV_1778) {
						isCVPresent = true;
						}

				}

			}
		}
		if(isCVPresent && severeCVCount==1){
			return true;
		}
		
		return false;
	}
	 // NBLXA-1317 -- END
	//NBLXA-1317 -- Case should not move from APPHLD if CV1778 exist and it will suspend till Future Date.
	protected void suspendCaseForRegisterDate()throws NbaBaseException {
		int com = 0;
		Date activateDateTime=null;
		if (!NbaUtils.isBlankOrNull(getNbaTxLife().getPolicy())) {
			Policy policy = getNbaTxLife().getPolicy();
					if (policy.getEffDate() != null) { 
							com = NbaUtils.compare(policy.getEffDate(), new Date());
							activateDateTime = policy.getEffDate();
						}
		
		}
		if (com > 0 && activateDateTime !=null) {
			String str = "Advanced Register Date CV Exist";
			suspendCase(activateDateTime,str ,55); //NBA119
		
		}

		// return false;
	}
    
	// Begin NBLXA-1954
	public NbaDst getTransactionDst(NbaTransaction transaction) throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(transaction.getID(), true);
		retOpt.setLockWorkItem();
		NbaDst nbadst = retrieveWorkItem(getUser(), retOpt);
		nbadst.setUpdate();
		nbadst.getNbaLob().setUnsuspendWorkItem(getWork().getID());
		return nbadst;
	}
	// End NBLXA-1954
	
	
	}
