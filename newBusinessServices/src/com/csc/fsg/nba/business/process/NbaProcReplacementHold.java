package com.csc.fsg.nba.business.process;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.database.NbaAutoClosureAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.AutoClosureCriteria;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vpms.NbaReplacementHoldData;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;

/*
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

/**
 * NbaProcReplacementHold is the class to process Cases found in NBRPLHLD queue.
 * Two types of work will be sent to this queue:
 *  - A case (NBAPPLCTN) that is either a pre-sale Reg 60 case after requirement determination, or aReg 60 case after automated underwriting.  
 *    - The pre-sale case will be held in this queue while it is valid 
 *    - The Reg 60 case will be held here until the Reg 60 case manager deems the case to be in good order, 
 *    - or until the number of days to put the case in good order expires
 *  - A replacement work item (NBRPLNOTIF) that will be used to trigger processing for Reg 60 cases and then sent to the end queue.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 */
public class NbaProcReplacementHold extends NbaAutomatedProcess {
	/** The vpmsadaptor object, which provides an interface into the VPMS system */	
	private NbaVpmsAdaptor vpmsProxy = null;
	protected NbaDst parentCase = null;
	private final static String SUSPENDED = "SUSPENDED";
	private final static java.lang.String PROCESS_PROBLEM = "Replacement Hold problem:";
	private AutoClosureCriteria autoClosure = null;
	private NbaReplacementHoldData replData= null;
	private String awdTime; // APSL4488
	/**
	 * This constructor calls the superclass constructor which will set
	 * the appropriate statues for the process.
	 */
	public NbaProcReplacementHold() {
		super();
	}
	/**

	 * @param user the user for whom the work was retrieved, in this case blah blah blah.
	 * @param work the AWD case to be processed
	 * @return NbaAutomatedProcessResult the results of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		if (!initialize(user, work)) {
			return getResult();
		}
		if (getResult() == null) {
			// Set AWD Time to suspend RPLNOTIF as according to AWD time
			setAwdTime(getTimeStamp(user)); // APSL4488
			doProcess();
		}
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		if (getResult() != null && !getResult().getText().equalsIgnoreCase(SUSPENDED)) {
			changeStatus(getResult().getStatus());
			// Update the Work Item with it's new status and update the work item in AWD
			doUpdateWorkItem();
		}
		if (null != parentCase) {
			unlockWork(parentCase);
		}
		//NBA020 code deleted
		// Return the result
		return getResult();
	}
	public void doProcess() throws NbaBaseException {

		if (isCase()) {
			processReg60Case();
		} else {
			processReplacementTransaction();
		}
		
	}
	public boolean isCase() {
		return getWork().isCase();
	}
	/*
	 * if closure exists, then check closure dates, otherwise create new closure record
	 */
	public void processReg60Case() throws NbaBaseException {
		
		initializeClosureInformation();
		if (closureRecordExists()) {
			checkClosureDate();
		} else {
			processInitialUpdate();
		}

	}
	
	/*
	 * invoke ReplacementProcessing to gather closure information so that it can be used to determine if record exists
	 */
	public void initializeClosureInformation() {
		try {
		replData = new NbaReplacementHoldData(getDataFromVpms("P_GetClosure", getWork(), new HashMap()));
		} catch (NbaBaseException nbe) {
			
		}
	}
	 /*
	 * If the ClosureDate is passed the current date, then route case to end queue otherwise re-suspend it.
	 */
	public void checkClosureDate() throws NbaBaseException {
		//QC12928 APSL3573 if the ClosureDate is passed the current date , SUSPEND it in the same status so that Auto Closure Poller will pick and work on the case
		int suspendDays = 1;
		Date suspendDate = autoClosure.getClosureDate();
		
		if (suspendDate.before(new Date())) {
			//QC12928 APSL3573 deleted code
			suspendDate = NbaUtils.addDaysToDate(new Date(), suspendDays);
			suspendWork(" Suspended for Auto Closure", suspendDate);
			//QC14032/APSL3919 changed the text to SUSPENDED
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, SUSPENDED, getFailStatus()));// APSL3084-QC11827
			// doUpdateWorkItem();
		} else {
			// resuspend;
			suspendWork("Initial Suspend", suspendDate);
			// QC11828 APSL3085
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, SUSPENDED, getWork().getStatus()));

		}
	}

	/**
	 * This method controls deletionf auto closure record if application.  Will then route work items to appropriate queue 
	 * based on Reg60Review and Reg60PSDecision fields
	 *
	 */
	private void processReg60WorkItems() throws NbaBaseException {
		//Delete auto closure record when it's type '5'
		deleteNIGORecord();
		//route parent case to next queue
		routeParentWork();
	}
	/**
	 * if a NIGO auto closure record exists (Closure Type=5 or 100050002), then delete it
	 *
	 */
	private void deleteNIGORecord() throws NbaDataAccessException, NbaBaseException {
		NbaAutoClosureAccessor naca = new NbaAutoClosureAccessor();
		AutoClosureCriteria acc = new AutoClosureCriteria();
		acc.setWorkItemID(getParentCase().getID());
		acc.setContractNumber(getWork().getNbaLob().getPolicyNumber());
		// ALS5090 Code deleted
		naca.deleteNIGORecord(acc);

	}
	private void routeParentWork() throws NbaBaseException {
		//obtain new status for parent case
		NbaProcessStatusProvider caseProvider = initializeCaseStatusFields();
		getParentCase().setStatus(caseProvider.getPassStatus());
		getParentCase().setActionUpdate();
		parentCase = WorkflowServiceHelper.updateWork(getUser(),getParentCase());
        //Begin ALS5010
		if(parentCase.isSuspended())
		{	
		 unsuspendWorkitem(parentCase);
		}
		//End ALS5010
	}
	public void processInitialUpdate() throws NbaBaseException {
		
		String activationDateStr = getActivationDate();
		Date activationDate = NbaUtils.getDateFromStringInAWDFormat(activationDateStr);

		createClosureRecord();
		
		doContractUpdate();
		
		suspendWork("Initial Suspend",activationDate);
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, SUSPENDED, getPassStatus()));
		
	}
	private void suspendWork(String comment, Date activationDate) throws NbaBaseException {
		

			NbaSuspendVO suspendVO = new NbaSuspendVO();
			suspendVO.setTransactionID(getWork().getID());
			suspendVO.setActivationDate(activationDate);
			
			addComment(comment, getUser().getUserID()); //Add comment
			update(getWork()); //update to AWD
			suspendWork(getUser(), suspendVO); //suspend work
	}
	private void createClosureRecord() throws NbaBaseException {

		AutoClosureCriteria acc = new AutoClosureCriteria();
		acc.setClosureInd(Integer.parseInt(replData.getClosureInd()));
		acc.setClosureType(Integer.parseInt(replData.getClosureType()));
		acc.setClosureDate(NbaUtils.getDateFromStringInAWDFormat(replData.getClosureDate()));
		NbaLob nbaLOBs = getWork().getNbaLob();
		acc.setCompany(nbaLOBs.getCompany());
		acc.setContractNumber(nbaLOBs.getPolicyNumber());
		acc.setWorkItemID(getWork().getID());
		acc.setSystemID(nbaLOBs.getBackendSystem());
		NbaAutoClosureAccessor naca = new NbaAutoClosureAccessor();
		naca.insertClosureRecord(acc);

		//update TXLife now...
		NbaTXLife nbaTXLife = this.getNbaTxLife();
		ApplicationInfo appInfo = nbaTXLife.getPrimaryHolding().getPolicy().getApplicationInfo();
		Date placementDate = null;
		try {
			placementDate = NbaUtils.getDateFromString(replData.getClosureDate().substring(0,10));
		} catch (Exception e) {
			placementDate = acc.getClosureDate();
		}
		appInfo.setPlacementEndDate(placementDate);
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (null != appInfoExt) {
			appInfoExt.setActionUpdate();
			appInfoExt.setClosureInd(replData.getClosureInd());
			appInfoExt.setClosureType(replData.getClosureType());
		}
		appInfo.setActionUpdate();

	}
	private String getActivationDate() throws NbaBaseException {
		VpmsComputeResult result = getDataFromVpms("P_GetCaseSuspendDays", getWork(), new HashMap());
		return result.toString();
		
	}
	public VpmsComputeResult getDataFromVpms(String entryPoint, NbaDst workItem, Map deOink) throws NbaBaseException, NbaVpmsException {
	    NbaVpmsAdaptor vpmsProxy = null;
	    try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(workItem.getNbaLob());
			oinkData.setContractSource(getNbaTxLife(), workItem.getNbaLob());
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			deOink.put(NbaVpmsAdaptor.A_INSTALLATION, getInstallationType());
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REPLACEMENTS_PROCESSING); 
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			vpmsProxy.setSkipAttributesMap(deOink);
			return vpmsProxy.getResults();
			  
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException(PROCESS_PROBLEM + NbaVpmsException.VPMS_EXCEPTION, re);
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
	public boolean closureRecordExists() {
		try {
		NbaAutoClosureAccessor naca = new NbaAutoClosureAccessor();
		autoClosure = naca.retrieveClosureRecord(getWork().getID(),getWork().getNbaLob().getPolicyNumber(), replData.getClosureType());
		if (null != autoClosure) {
			
			return true;
		}
		} catch (Exception e ) {
			//does not matter
		}
		return false;
	}
	/**
	 * This method will retrieve the Parent Work Item.  Once retrieved, it will determine if the following should occur:
	 * Suspend work item if case is not in the same queue.
	 * if the case is IGO, route case to UW
	 * if case is NIGO, route work item to end queue
	 */
	public void processReplacementTransaction() throws NbaBaseException {

		if (PROC_REPL_PROCESSING.equalsIgnoreCase(getParentCase().getQueue())) {
			determineReg60Status();
		} else {
			if (getWorkType() != null && getWorkType().equals(NbaConstants.A_WT_REPL_NOTIFICATION)) {
				// Begin QC17040/APSL4719
				ApplicationInfo applInfo = getNbaTxLife().getPolicy().getApplicationInfo();
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applInfo);
				if (null != appInfoExt
						&& appInfoExt.hasReg60Review()
						&& (appInfoExt.getReg60Review() == NbaOliConstants.NBA_REG60REVIEW_IGO || appInfoExt.getReg60Review() == NbaOliConstants.NBA_REG60REVIEW_NIGO)) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
				}
				// End QC17040/APSL4719
				else {
					suspendWork();
				}
			} else {
				unlockParentWork(); // APSL4318"
				throw new NbaLockedException(); // allow AccelBP to process Locked Exception processing and suspend work item
			}

		}
	}
	
	public void determineReg60Status() throws NbaBaseException {
		
   		ApplicationInfo applInfo = getNbaTxLife().getPolicy().getApplicationInfo();
   		
   		if (isReg60InGoodOrder(applInfo)) {
   			processReg60WorkItems();
   			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Case is IGO", getPassStatus()));
   			return;
   		} 
 
   		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Case is NIGO", getPassStatus()));		
	}
	
	public boolean isReg60InGoodOrder(ApplicationInfo applInfo) {
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applInfo);
   		if (null != appInfoExt && appInfoExt.hasReg60Review() && appInfoExt.getReg60Review() == NbaOliConstants.NBA_REG60REVIEW_IGO) {
   			return true;
   		}
   		return false;
	}
  
	/**
	 * Return the NbaDst for the Case and Sources.
	 * 
	 * If the NbaDst has not been retrieved yet:
	 * - retrieve the Case, Sources and sibling Transactions of the current Transaction from the workflow system.
	 * @return NbaDst - the NbaDst for the Case and Sources
	 */
	protected NbaDst getParentCase() throws NbaBaseException {
		if (this.parentCase == null) {
			retrieveParentCase();
		}
		return this.parentCase;
	}
	/**
	 * Retrieve the Case
	 * @throws NbaBaseException
	 */
	//SPR3611 New Method
	protected void retrieveParentCase() throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestCaseAsParent();
		retOpt.setLockParentCase();
		this.parentCase = retrieveWorkItem(getUser(), retOpt); //Retrieve the Case, Sources and sibling Transactions 
	}

    protected NbaProcessStatusProvider initializeCaseStatusFields()  throws NbaBaseException {  	
     	return new NbaProcessStatusProvider(getUser(), parentCase, getNbaTxLife());
    	
    }
    //ALS5010 New Method 
    protected void unsuspendWorkitem(NbaDst work) throws NbaBaseException {
    	NbaSuspendVO suspendVO = new NbaSuspendVO();
		suspendVO.setCaseID(work.getID());
		unsuspendWork(getUser(),suspendVO);
    }
    
    // APSL4318 New method 
    protected void unlockParentWork() throws NbaBaseException {		
		if (getParentCase() != null) {			
			getParentCase().getWorkItem().getWorkItemChildren().clear();
			unlockWork(getParentCase());
		}
	}
    /**APSL4488
	 * Suspend the case for 4 hours.
	 * AWD time is 60 minutes behind so 180 minutes suspension is actually 4 hours
	 * @throws NbaBaseException
	 */
	public void suspendWork() throws NbaBaseException {
		suspendVO = new NbaSuspendVO();
		int suspentionMinutes = getSuspendMinute();
		if (getWork().isCase()) {
			suspendVO.setCaseID(getWork().getID());
		} else {
			suspendVO.setTransactionID(getWork().getID());
			
		}
		getLogger().logDebug("Starting suspendCase");
		Calendar calendar = new GregorianCalendar();
		// calendar.setTime(new Date());
		calendar.setTime(parseTimeStamp(getAwdTime()));
		calendar.add(Calendar.MINUTE, suspentionMinutes); 
		suspendVO.setActivationDate(calendar.getTime());
		suspendVO.setSuspendCode(NbaConstants.AWD_SUSPEND_REASON_NOWORK);
		setSuspendVO(suspendVO);
		updateForSuspend();
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
	}
	/*
	 * APSL4488
	 */
	protected int getSuspendMinute() throws NbaBaseException {
		String suspendMinutes = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.REPL_NOTIFI_WORK_SUSPEND_MINUTES); //ACN012
		if (suspendMinutes != null) { 
			return Integer.parseInt(suspendMinutes); 
		}
		return 0;

	}
	
	protected Date parseTimeStamp(String dateForm) {
		try {
			java.util.Date date = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").parse(dateForm);
			return date;
		} catch (java.text.ParseException e) {
			return new Date();
		}
	}
	// APSL4488 Ends
	/**
	 * @param awdTime the awdTime to set
	 */
	public void setAwdTime(String awdTime) {
		this.awdTime = awdTime;
	}
	/**
	 * @return the awdTime
	 */
	public String getAwdTime() {
		return awdTime;
	}
}
