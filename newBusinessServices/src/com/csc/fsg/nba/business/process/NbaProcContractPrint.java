package com.csc.fsg.nba.business.process;
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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.contract.extracts.AxaLifeContractPrintExtractFormater;
import com.csc.fsg.nba.contract.validation.NbaContractValidation;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.ActivityExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.EPolicyData;
import com.csc.fsg.nba.vo.txlife.ExtractScheduleInfo;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UnderwritingResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vo.txlife.UserLoginNameAndUserPswd;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
/**
 * NbaProcContractPrint processes Transaction work items found in NBPRINT queue. If the 
 * Transaction aleady contains a NBPRTEXT Source item, the Transaction is a reprint request.
 * Otherwise it is an initial print request. For initial print requests, a new NBPRTEXT Source item
 * is created and added to the Transaction. An NBPRTEXT Source item is a Text file which contains  
 * a TXLife XML file.  The TXLife contains TXLifeRequest objects for each Contract Print Extract
 * type identified in the "EXTC" LOB field of the Transaction.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA012</td><td>Version 2</td><td>Contract Print Extract</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architectural changes</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>SPR1359</td><td>Version 3</td><td>Automated processes stop poller when unable to lock supplementary work items</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>NBA095</td><td>Version 4</td><td>Queues Accept Any Work Type</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2366</td><td>Version 5</td><td>Added unique file names for webservice stubs</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr> 
 * <tr><td>NBA129</td><td>Version 5</td><td>xPression Integration</td></tr> 
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>NBA212</td><td>Version 7</td><td>Content Services</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>AXAL3.7.14</td><td>AXA Life Phase 1</td><td>Contract Print</td></tr>
 * <tr><td>ALS1246</td><td>AXA Life Phase 1</td><td>Removed all the System Messages from the TX 500</td></tr>
 * <tr><td>AXAL3.7.22</td><td>AXA Life Phase 1</td><td>Compensation Interface</td></tr>
 * <tr><td>AXAL3.7.27</td><td>AXA Life Phase 1</td><td>RTS Interface</td></tr>
 * <tr><td>ALS4467</td><td>AXA Life Phase 1</td><td>QC #3235 - 3.7.17 CAPS - duplicate policies sent to CAPS submitAdministrationPolicy in QA</td></tr>
 * <tr><td>ALPC255</td><td>AXA Life Phase 1</td><td>Compensation Interface Substandard Premium</td></tr>
 * <tr><td>ALPC234</td><td>AXA Life Phase 1</td><td>Unbound Processing</td></tr>
 * <tr><td>SR522849</td><td>Discretionary</td><td>Reducing Multiple contract generation at unpaid reissue</td></tr>
 * <tr><td>SR514766</td><td>Discretionary</td><td>Premium Quote</td></tr>
 * <tr><td>CR1453745</td><td>Discretionary</td><td>Revised Illustration Indicator</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 2
 */
public class NbaProcContractPrint extends NbaAutomatedProcess {

//	NBA103 - removed method

	protected NbaSource nbaSource; //NBA100
	protected boolean newSource = false; //NBA100
	protected String modalPremAmt = null;  //ALPC255
    NbaDst parentCase;    //APSL4374
	protected boolean reprintInd = false;  //ALPC234, APSL4419
	protected EPolicyData ePolicyData = null;
	protected boolean isSuspendNeeded = false;
	protected boolean unboundInd = false;  //ALPC234, APSL4419
	/**
	 * NbaProcContractPrint constructor.
	 */
	public NbaProcContractPrint() {
		super();
	}
	/**
	 * Write the extracts for the new or pre-existing Contract Print Extract Source.
	 * @throws NbaBaseException
	 */
	protected void doProcess() throws NbaBaseException {
		try {
			// begin NBA100 ALS4492
			setUnboundIndicator(); // ALPC234 ALS5667
			// If this is just a reprint, don't run validations
			// if (!NbaConstants.REPRINT_EXTRACT.equalsIgnoreCase(getWork().getNbaLob().getExtractCompAt(2))) { //ALII793 //CR61047
			performPrePrintValidation();// ALS4705 //CR61047 Commented code which checks rePrint -- run CVs all the time
			// } //ALII793
			// Reverted Code APSL3992
			
			//APSL5100
			PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
			EPolicyData ePolData = retrieveActiveEPolicyData();
			//NBLXA-1528, ESLI product: web service call exclusion
			if (ePolData != null  && !unboundInd && policyExtension != null && policyExtension.getPrintPreviewNeededInd()
					&& NbaUtils.isEmpty(ePolData.getPrintStatus()) && !NbaUtils.isEmpty(ePolData.getEPolicyPrintID())
					&& !NbaUtils.isESLIProduct(getNbaTxLife()) ) { ///NBLXA-1308
				performPrintStatusWebServiceCall(ePolData);
			}
			NbaSource nbaSource = getNbaSource();
			if (nbaSource != null) {// ALII1215
				saveTotalModalPremiumAmt(nbaSource); // ALPC255
				// ALS5556 code deleted
			//	setNbaTxLife(doContractUpdate());// ALS5086
				writeExtracts(nbaSource);
				setNbaTxLife(doContractUpdate());// APSL5100
				// end ALS4492
				//getWork().getNbaLob().setContractPrinted(true); // SR522849,APSL5100 moved to post issue process
			}
		} catch (NbaBaseException e) {
			getLogger().logFatal(e.getMessage());
			throw e;
			// end NBA100
		}
	}
	/**
	 * Access the VPMS model to retieve the Automatic process statuses for this
	 * automated process. Perform the processing for this business process and 
	 * update the Work Item.  Return an NbaAutomatedProcessResult containing 
	 * information about the success or failure of the process.
	 * 
	 * @param user the NbaUser for whom the process is being executed
	 * @param work a NbaDst value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException when a processing situation exists which will prevent other work items from being processed.
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		// NBA095 begin
		if (!initialize(user, work)) {
			return getResult();
		}
		// NBA095 end
		// begin ALS4467

		String extComp2 = getWork().getNbaLob().getPrintExtract();	// APSL5055 ALPC234 refactor
		reprintInd = (extComp2 != null && extComp2.trim().length() > 0); // ALPC234 refactor
		unboundInd = (extComp2 != null && NbaConstants.UNBOUND_EXTRACT.equalsIgnoreCase(extComp2.trim())); //NBLXA-1308
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
		boolean previewNeededInd = isPreviewNeeded();//APSL5100
		policyExtension.setPrintPreviewNeededInd(previewNeededInd);//APSL5100
		policyExtension.setActionUpdate();//APSL5100
		reinitializeStatusProvider();
		ePolicyData = retrieveActiveEPolicyDataForCurrentPrint();
		
		changeInDeliveryMethodNeeded(); //NBLXA-2603[NBLXA-2610]		
		if (unboundInd ||(policyExtension != null && !policyExtension.getPrintPreviewNeededInd())) { ///NBLXA-1308
			processPrint();
		} else if (ePolicyData == null) {
			isSuspendNeeded = true;
			updatePDRIndForGI();// NBLXA-188(APSL5318) Legacy Decommissioning
			processPrint();
		} else if (ePolicyData != null && NbaUtils.isEmpty(ePolicyData.getEPolicyPrintID())) {
			suspendPrintWork();
			} else if (ePolicyData != null && policyExtension.getUpdatedPreviewRecievedInd()) {
			isSuspendNeeded = false;
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			changeStatus(getResult().getStatus(),getRouteReason()); //APSL5128
			updateOldActivity(getNbaTxLife());
			policyExtension.setUpdatedPreviewRecievedInd(false);
			policyExtension.setActionUpdate();
			doUpdateWorkItem();
			doContractUpdate();
		} else {
			isSuspendNeeded = true;
			processPrint();
			}
		return getResult();
	}
	
	protected void updateOldActivity(NbaTXLife txLife) {
		ArrayList<Activity> activityList = txLife.getOLifE().getActivity();
		for(Activity activity : activityList){
			if(activity != null && activity.getActivityTypeCode() == NbaOliConstants.OLI_ACTTYPE_1009800006 && activity.getActivityStatus() == NbaOliConstants.OLI_ACTSTAT_ACTIVE){
				ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(activity);
				if(activityExtn != null && (activityExtn.getConditions() == NbaOliConstants.OLI_PRINT_PREVIEW_CONDITIONS_CORRECTED_NO_PREVIEW_NEEDED || activityExtn.getConditions() == NbaOliConstants.OLI_PRINT_PREVIEW_CONDITIONS_CORRECTED_PREVIEW_NEEDED)){
					activity.setActivityStatus(NbaOliConstants.OLI_ACTSTAT_COMPLETE);
					activity.setActionUpdate();
				}
			}
		}
	}
	
	/**
	 * Create the Contract Print Extract Source.
	 * @return Contract Print Extract Source to be attached to the work item
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	protected NbaSource generateContractPrintSource() throws NbaBaseException {//ALS5797
		getLogger().logDebug("Creating Contract Print Extract Source");
		try {
			Date start = new GregorianCalendar().getTime();
			NbaSource nbaSource = new AxaLifeContractPrintExtractFormater().
										generateContractPrintSource(getUser(), getWork(), getNbaTxLife());//AXAL3.7.14				
			logElapsedTime(start, "AxaLifeContractPrintExtractFormater processing");
			return nbaSource;
		} catch (NbaDataException nde) { //ALII1215
			addComment(nde.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getFailStatus(), getFailStatus()));
		} catch (NbaBaseException e) {
			e.forceFatalExceptionType();//ALS5797
			addComment(e.getMessage());//ALS5797
			throw e;//ALS5797
		} 
		return null;
	}
	/**
	 * Retrieve the Contract Print Extract Source with lazy initialization.
	 * If not already retrived:
	 * - Check for the existance of Contract Print Extract Source attached to the Transaction Work Item. 
	 * - If missing, create a new Contract Print Extract Source. 
	 * @return
	 */
	// NBA100 New Method
	protected NbaSource getNbaSource() throws NbaBaseException {
		if (nbaSource == null) {
			setNbaSource(retrieveContractPrintSource());
			if (isNewSource()) {
				setNbaSource(generateContractPrintSource());
			}
		}
		return nbaSource;
	}
	/**
	 * Reteieve the DST objectfor the Print Extract (NBPRTEXT) Transaction
	 * @return the DST for the Print Extract Transaction
	 */
	// NBA100 New Method
	protected NbaDst getTransactionDst() {
		return getWork();
	}
	/**
	 * Handle an exception generated because of a problem with the contract by
	 * adding a comment identifying the exception to the Work Item and creating
	 * an NbaAutomatedProcessResult for the failure.
	 * @param e
	 */
	// NBA100 New Method
	protected void handleContractProblem(Exception e) {
		addComment(e.getMessage());
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getFailStatus()));
	}
	/**
	 * Retrieve the indicator for a new Source item.
	 * @return the new source indicator
	 */
	// NBA100 New Method
	protected boolean isNewSource() {
		return newSource;
	}
	/**
	 * Examine the Sources of the work item and return the Contract Print Extract Source if present
	 * @return Contract Print Extract Source or null
	 */
	// NBA100 New Method
	protected NbaSource retrieveContractPrintSource() throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getTransactionDst().getID(), true);
		retOpt.requestSources();
		try {
			Date start = new GregorianCalendar().getTime();
			setTransactionDst(retrieveWorkItem(getUser(), retOpt));  //NBA213
			logElapsedTime(start, "Retrieving Sources from AWD");
		} catch (NbaNetServerException e) {
			throw new NbaBaseException("Unable to retrieve", e);
		//NBA213 deleted code
		}
		
		List sources = getTransactionDst().getNbaSources();
		for (int i = 0; i < sources.size(); i++) {
			if (((NbaSource) sources.get(i)).isContractPrintExtract()) {
				setNewSource(false);
				return (NbaSource) sources.get(i);
			}
		}
		setNewSource(true);
		return null;
	}
	/**
	 * Store the Contract Print Extract Source 
	 * @param source
	 */
	// NBA100 New Method
	protected void setNbaSource(NbaSource source) {
		nbaSource = source;
	}
	/**
	 * Set the indicator for a new Source item.
	 * @param b
	 */
	// NBA100 New Method
	protected void setNewSource(boolean b) {
		newSource = b;
	}
	/**
	 * Set the DST object for the Print Extract (NBPRTEXT) Transaction
	 * @param the DST for the Print Extract Transaction
	 */
	// NBA100 New Method
	protected void setTransactionDst(NbaDst nbaDst) {
		setWork(nbaDst);
	}
	/**
	 * Send the Contract Print Extracts to the Web Service implementation defined in the configuration.
	 * For Base System, the implementation is a SOAP Service which wrappers the NbaContractPrintExtractWebService EJB.
	 * The NbaContractPrintExtractWebService EJB writes the extacts to a database.
	 * @param NbaSource Object
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	protected void writeExtracts(NbaSource nbaSource) throws NbaBaseException {
		if (getResult() == null) {
			getLogger().logDebug("Sending Contract Print Extracts to the Web Service");
			NbaTXLife nbaTXLife = null;
			// NBLXA-188(APSL5318) Legacy Decommissioning
			PolicyExtension origPolicyExtn = null;
			Date contractPrintExtractDate = null;
			// NBLXA-188(APSL5318) Legacy Decommissioning
			try {
				nbaTXLife = new NbaTXLife(nbaSource.getText());
				contractPrintExtractDate = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo())
						.getContractPrintExtractDate(); // NBLXA-188(APSL5318) Legacy Decommissioning
				NbaUtils.getFirstApplicationInfoExtension(nbaTXLife.getPolicy().getApplicationInfo())
						.setContractPrintExtractDate(contractPrintExtractDate); // NBLXA-188(APSL5318) Legacy Decommissioning
				nbaTXLife.getPolicy().setEffDate(getNbaTxLife().getPolicy().getEffDate()); // ALS4705
				// Begin NBLXA-2155[NBLXA-2309]
				if (NbaUtils.isWholeSale(getNbaTxLife().getPolicy())) {
					NbaUtils.getFirstApplicationInfoExtension(nbaTXLife.getPolicy().getApplicationInfo()).setIllustrationInd(false);
					NbaUtils.getFirstApplicationInfoExtension(nbaTXLife.getPolicy().getApplicationInfo()).setRevisedIllustrationInd(false);
				} else {
					NbaUtils.getFirstApplicationInfoExtension(nbaTXLife.getPolicy().getApplicationInfo()).setIllustrationInd(
							NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo()).getIllustrationInd()); // SR514766
					NbaUtils.getFirstApplicationInfoExtension(nbaTXLife.getPolicy().getApplicationInfo()).setRevisedIllustrationInd(
							NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo()).getRevisedIllustrationInd()); // CR1453745
				} // End NBLXA-2155[NBLXA-2309]
				NbaUtils.getFirstApplicationInfoExtension(nbaTXLife.getPolicy().getApplicationInfo()).setReprintInd(reprintInd); // NBLXA-1997
				// Begin QC5895/APSL810/APSL648
				boolean unboundPrintInd = false;
				Double cwaTotal = getNbaTxLife().getNbaHolding().getCwaTotal();
				if (cwaTotal.doubleValue() <= 0 && !reprintInd) {
					unboundPrintInd = true;
				}
				origPolicyExtn = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy()); // APSL4585 // NBLXA-188(APSL5318) Legacy
																								// Decommissioning
				PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy());
				if (policyExtn != null) {
					policyExtn.setUnboundInd(origPolicyExtn.getUnboundInd()); // APSL4585
					if (unboundInd) {// NBLXA-1308
						policyExtn.setPrintPreviewNeededInd(false);// NBLXA-1308
					}
				}
				// End QC5895/APSL810/APSL648
			} catch (Exception e) {
				handleContractProblem(e);
			}
			// NBLXA-1528: ESLI product: web service call exclusion
			if (getResult() == null && !NbaUtils.isESLIProduct(nbaTXLife)) {
				// AXAL3.7.14 start
				Date start = new GregorianCalendar().getTime();
				AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CONTRACT_PRINT, user, nbaTXLife,
						null, null);
				webServiceInvoker.execute();
				/*
				 * begin code commented PERF-APSL290(ALS5981) try { new NbaContractPrintExtractAccessor().write(nbaTXLife); } catch (Exception e2) {
				 * throw new NbaBaseException(e2); }
				 */
				// end PERF-APSL290(ALS5981)
				// ALS4467 code deleted...moved to NbaProcPostIssue
				// P2AXAL004 code deleted
				// NBLXA-188(APSL5318) Legacy Decommissioning
				if (NbaUtils.isGIApplication(getNbaTxLife()) && null != origPolicyExtn && origPolicyExtn.hasGIBatchID()
						&& origPolicyExtn.getPrintTogetherIND())
					NbaSystemDataDatabaseAccessor.updateContractPrintExtractDate(contractPrintExtractDate, origPolicyExtn.getGIBatchID(),
							getNbaTxLife().getPolicy().getPolNumber());
				// NBLXA-188(APSL5318) Legacy Decommissioning
				// Set TransRefGUID in Tx203.
				setSenderTransRefGUID(nbaTXLife); // APSL5100
				logElapsedTime(start, "ContractPrintExtract Web Service processing");
			}
		}
	}

	//AXAL3.7.22 new method added
    //ALS4972 code deleted
	//AXAL3.7.22 new method added
    //ALS4972 code deleted
	//AXAL3.7.22 new method added
	//ALS4972 code deleted
	//AXAL3.7.27 new method added.
   //ALS4972 code deleted
	/**
	 * Handle the NbaTXLife response form the Service. A TXLifeResponse.TransResult.ResultInfo.ResultInfoCode of
	 * TC_RESINFO_UNABLETOPROCESS (600) indicates that there was a service failure.
	 * @param nbaTXLifeResponse
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	protected void handleResponse(NbaTXLife nbaTXLifeResponse) throws NbaBaseException {
		if (nbaTXLifeResponse != null && nbaTXLifeResponse.isTransactionError()) {
			UserAuthResponseAndTXLifeResponseAndTXLifeNotify allResponses =
				nbaTXLifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
			int count = allResponses.getTXLifeResponseCount();
			TransResult transResult = allResponses.getTXLifeResponseAt(count - 1).getTransResult();
			ResultInfo resultInfo;
			for (int i = 0; i < transResult.getResultInfoCount(); i++) {
				resultInfo = transResult.getResultInfoAt(i);
				if (resultInfo.getResultInfoCode() == NbaOliConstants.TC_RESINFO_UNABLETOPROCESS) { //Service level error
					throw new NbaBaseException(resultInfo.getResultInfoDesc());
				}
				addComment(resultInfo.getResultInfoDesc());
			}
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Unable to write extracts", getFailStatus()));
		}
	}
	/**
	 * Log the elapsed time.
	 */
	// NBA100 New Method	
	protected void logElapsedTime(Date startTime, String message) {
		if (getLogger().isDebugEnabled()) {
			if (startTime == null) {
				return;
			}
			Date endTime = java.util.Calendar.getInstance().getTime();
			float elapsed = ((float) (endTime.getTime() - startTime.getTime())) / 1000;
			StringBuffer elStr = new StringBuffer();
			elStr.append("Elapsed time: ");
			elStr.append(elapsed);
			elStr.append(" seconds ");
			elStr.append(message);
			getLogger().logDebug(elStr.toString());
		}
	}
	/**
	 * Set the user and password to the automated process
	 * @param nbaTXLife
	 * @return Updated nbaTXLife
	 */
	// NBA129 New Method	
	protected NbaTXLife setUserPassword(NbaTXLife nbaTxLife) {
		
		UserLoginNameAndUserPswd userPswd = nbaTxLife
			.getTXLife()
			.getUserAuthRequestAndTXLifeRequest()
			.getUserAuthRequest()
			.getUserLoginNameAndUserPswdOrUserSessionKey()
			.getUserLoginNameAndUserPswd();
		userPswd.setUserLoginName(getUser().getUserID());
		userPswd.getUserPswd().getPswdOrCryptPswd().setPswd(getUser().getPassword());
		
		nbaTxLife
			.getTXLife()
			.getUserAuthRequestAndTXLifeRequest()
			.getUserAuthRequest()
			.getUserLoginNameAndUserPswdOrUserSessionKey()
			.setUserLoginNameAndUserPswd(userPswd);
			
		return nbaTxLife;
	}
	
	//ALS4467 new method
	protected void reinitializeStatusProvider() throws NbaBaseException {
		//ALPC234 code refactored
		Map deOinkMap = new HashMap();
		deOinkMap.put("A_IsReprintInd", String.valueOf(reprintInd));
		deOinkMap.put("A_IsUnboundInd", String.valueOf(unboundInd)); ///NBLXA-1308
		setStatusProvider(new NbaProcessStatusProvider(getUser(), getWork(), getNbaTxLife(),deOinkMap)); //ALPC234
 	}
	/**
	 * @return Returns the modalPremAmt.
	 */
	//ALPC255
	public String getModalPremAmt() {
		return modalPremAmt;
	}
	/**
	 * @param modalPremAmt The modalPremAmt to set.
	 */
	//ALPC255
	public void setModalPremAmt(String modalPremAmt) {
		this.modalPremAmt = modalPremAmt;
	}
	//ALPC255
	private void saveTotalModalPremiumAmt(NbaSource nbaSource) {
		NbaTXLife nbaTXLife = null;
		try {
			nbaTXLife = new NbaTXLife(nbaSource.getText());
		} catch (Exception e) {
			handleContractProblem(e);
		}
		if (getResult() == null) {
			ExtractScheduleInfo extSchedule = getFirstExtractSchedule(nbaTXLife);
			
			if (null != extSchedule) {
				double totalPrmAmt = extSchedule.getTotalSubstdModalPremAmt();
				Attachment attach;
				attach = getSubstandardAttachment();
				AttachmentData attachData = null;
				if (null == attach) {
					attach = new Attachment();
					NbaOLifEId nbaOLifEId = new NbaOLifEId(getNbaTxLife());  
					nbaOLifEId.setId(attach);
					attach.setAttachmentType(NbaOliConstants.OLI_ATTACH_SUBSTANDARD_PREM);
					attach.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT);
					attach.setActionAdd();
					getNbaTxLife().getPrimaryHolding().addAttachment(attach);
					attachData = new AttachmentData();
					attachData.setActionAdd();
				} else {
					attachData = attach.getAttachmentData();
				}
				attach.setActionUpdate();
			    attachData.setPCDATA(String.valueOf(totalPrmAmt));
			    attachData.setActionUpdate();
			    attach.setAttachmentData(attachData);
			    
				
				
			}
		}
	}
	
	private Attachment getSubstandardAttachment() {
		Attachment attachment = null;
		List attachmentList = getNbaTxLife().getPrimaryHolding().getAttachment();
		int attachmentListSize = attachmentList.size();
		if (attachmentList != null && attachmentListSize > 0) {
			for (int k = 0; k < attachmentListSize; k++) {
				attachment = (Attachment) attachmentList.get(k);
				if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_SUBSTANDARD_PREM) {
					return attachment;
					}
				}
		}
		return null;
		
	}
	private ExtractScheduleInfo getFirstExtractSchedule(NbaTXLife nbaTxLife) {
		Life life = nbaTxLife.getLife();
		int covCount = life.getCoverageCount();
		Coverage coverage = nbaTxLife.getPrimaryCoverage();
		CoverageExtension coverageExtension = null;
		if (null != coverage) {
			coverageExtension = NbaUtils.getFirstCoverageExtension(coverage);	 
		}
		ExtractScheduleInfo esi = null;
		if (null != coverageExtension) {
			int scheduleCount = coverageExtension.getExtractScheduleInfoCount();
			for (int i=0;i<scheduleCount;i++) {
				esi = coverageExtension.getExtractScheduleInfoAt(i);
				if (esi.getDuration() == 1) {
					return esi;
				}
			}
		}
		return null;
		
	}	
	
	//ALS5556 code deleted..moved to CV '234' in Misc subset
	//ALCP234 new method, APSL4419
	protected void setUnboundIndicator() {
		ApplicationInfo appInfo = getNbaTxLife().getNbaHolding().getApplicationInfo();
		NbaTXLife txLife = getNbaTxLife();
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (null == appInfo) {
			return;
		}
		//APSL4585 - Code deleted
		Double cwaTotal = getNbaTxLife().getNbaHolding().getCwaTotal();
		boolean unboundInd = false;
		// APSL4585 start
		//Call VPMS to determine is approved at rate class is less favourable
/*		int rateClassLessFav = NbaConstants.FALSE;
		NbaVpmsResultsData vpmsRateClassResultsData = getDataFromVPMS(NbaVpmsConstants.CONTRACTVALIDATIONCALCULATIONS,NbaVpmsConstants.EP_VALIDATE_RATE_CLASS);
		if (vpmsRateClassResultsData != null && vpmsRateClassResultsData.getResultsData() != null) {
			rateClassLessFav = Integer.parseInt((String) vpmsRateClassResultsData.getResultsData().get(0));
		}*/		
		
		//Calculate shortage amount
		int shortageAmt = 0;
		NbaVpmsResultsData vpmsShortageAmtResultsData = getDataFromVPMS(NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS,NbaVpmsConstants.EP_CWA_SHORTAGE_LIMIT);
		if (vpmsShortageAmtResultsData != null && vpmsShortageAmtResultsData.getResultsData() != null) {
			shortageAmt = Integer.parseInt((String) vpmsShortageAmtResultsData.getResultsData().get(0));
		}
		
		/*Set unbound indicator to true if
		 * 1. reprint indicator is false
		 * 2. MIP not met
		 * 3. MIP met but Approved rate class less favorable than applied for rate class
		 */
		
		/*SC:set unbound indicator to true if Mip is met after first print date  */
		boolean paymentMetAfterFirstPrint=false;
		/*To fecth first time MIp met date*/
		Date mipDate = calcFistTimeMipMetDate(shortageAmt);
		PolicyExtension origPolicyExtn = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
		Date firstPrintDate = null;
		if (appInfoExt != null) {
			if (appInfoExt.hasFirstPrintExtractDate()) {
				firstPrintDate = appInfoExt.getFirstPrintExtractDate();
				if (firstPrintDate != null && mipDate != null) {
					int result = NbaUtils.compare(firstPrintDate, mipDate);
					if (result < 0) {
						paymentMetAfterFirstPrint = true;
					} else if (result == 0 && origPolicyExtn != null && origPolicyExtn.getUnboundInd()) {
						paymentMetAfterFirstPrint = true;
					}
				}
			}
		}
		/*EC set unbound indicator to true if Mip is met after first print date*/

		if(getParentCase() != null && NbaConstants.SYST_LIFE70.equals(parentCase.getNbaLob().getBackendSystem())){			
			if(txLife.getPolicy() != null && txLife.getPolicy().hasMinPremiumInitialAmt() && 
					(cwaTotal.doubleValue() < txLife.getPolicy().getMinPremiumInitialAmt()- shortageAmt)){
				unboundInd = true;				
			}
			if(paymentMetAfterFirstPrint)
			{
				unboundInd = true;	
			}
		}else if(getParentCase() != null && NbaConstants.SYST_CAPS.equals(parentCase.getNbaLob().getBackendSystem())){			
			if(txLife.getPolicy() != null && txLife.getPolicy().hasPaymentAmt() && 
					(cwaTotal.doubleValue() < txLife.getPolicy().getPaymentAmt()- shortageAmt)){
				unboundInd = true;				
			}
			if(paymentMetAfterFirstPrint)
			{
				unboundInd = true;	
			}
		}
		else {
		    unboundInd = false;
		}
		PolicyExtension polExt = getPolicyExtension();
		//if not already unbound, then determine unbound rule
		polExt.setUnboundInd(unboundInd);
		polExt.setActionUpdate();
		
	}
	/*
	 * Case is unbound only when Payment Method is direct (2) or Bank Draft (7)
	 */
	//ALPC234
	private boolean isUnboundPaymentMethod(){
		long paymentMethod = getNbaTxLife().getPolicy().getPaymentMethod();
		if (NbaOliConstants.OLI_PAYMETH_REGBILL == paymentMethod || NbaOliConstants.OLI_PAYMETH_ETRANS == paymentMethod) {
			return true;
		}
		return false;
	}
	//ALPC234 new method
	private PolicyExtension getPolicyExtension() {
		Policy pol = getNbaTxLife().getPolicy();
		PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(pol);
		if (null == polExt) {
			OLifEExtension olifeExt =  NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
	        polExt = new PolicyExtension();
	        polExt.setActionAdd();
	        olifeExt.setPolicyExtension(polExt);
	        pol.addOLifEExtension(olifeExt);
			pol.setActionUpdate();
		}
		return polExt;
	}
	/**
	 * Set the Business Process to Pre Issue Validation and perfrom Contract Validation.
	 * If Severe or non-Overridden errors are present 
	 *  - update the contract to commit any contract validation changes
	 * - add an "Unresolved Contact Validation errors are present" message to the contract
	 * - set the status to cause the work item to be routed to the host error queue
	 * @throws NbaBaseException
	 * 
	 */
	//SPR3711 New Method
	protected void performPrePrintValidation() {
		String origBusinessProcess = getNbaTxLife().getBusinessProcess();
		getNbaTxLife().setBusinessProcess(NbaConstants.PROC_VALIDATE_PRINT);
		try {
		new NbaContractValidation().validate(getNbaTxLife(), getWork(), getUser());
		} catch (NbaBaseException nbe) {
			getLogger().logError("Error running preprint validation");
		}
		getNbaTxLife().setBusinessProcess(origBusinessProcess);	//Restore the original Business Process 
	}
		
	// Begin APSL4374
	
	protected NbaDst retrieveParent(NbaUserVO user, boolean locked) throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestCaseAsParent();
		retOpt.requestTransactionAsSibling();			
		if (locked) {
			retOpt.setLockParentCase();			
		}
		
		//get case from awd
		NbaDst parentCase = retrieveWorkItem(user, retOpt);
		setParentCase(parentCase);
		return parentCase;
	}

	private void setParentCase(NbaDst parentWI) {
		parentCase = parentWI;

	}

	private NbaDst getParentCase() {
		return parentCase;

	}
	
	public NbaSearchVO searchPrint(String contractKey) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_CONT_PRINT_EXTRACT);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = lookupWork(getUser(), searchVO);
		return searchVO;
	}

	public boolean isAllPrintInEnd(String contractKey) throws NbaBaseException {
		NbaSearchVO searchPrintVO = searchPrint(contractKey);
		if (!NbaUtils.isBlankOrNull(searchPrintVO) 
				&& searchPrintVO.getSearchResults() != null 
				&& !searchPrintVO.getSearchResults().isEmpty()) {
			List searchResultList = searchPrintVO.getSearchResults();
			for (int i = 0; i < searchResultList.size(); i++) {
				NbaSearchResultVO searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
				if (searchResultVo != null && 
						!(searchResultVo.getQueue().equalsIgnoreCase(NbaConstants.END_QUEUE))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	//APSL4585 New Method
	public NbaVpmsResultsData getDataFromVPMS(String modelName, String entryPoint) {		
		NbaVpmsAdaptor adapter = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
			if(getNbaTxLife() != null){
				oinkData.setContractSource(getNbaTxLife());
			}
			adapter = new NbaVpmsAdaptor(oinkData, modelName);
			Map deOinkMap = new HashMap();
			adapter.setSkipAttributesMap(deOinkMap);
			adapter.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(adapter.getResults());
			return vpmsResultsData;
		} catch (Exception e) {
			getLogger().logDebug("Problem in getting rate class from VPMS" + e.getMessage());
		} finally {
			try {
				if (adapter != null) {
					adapter.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}
		return null;
	}
	/* APSL4585 Calculate First Time MIP met Date */
	protected Date calcFistTimeMipMetDate(int shortageAmt) {
		OLifE olifeIssue = getNbaTxLife().getOLifE();
		Holding holdingIssue = NbaTXLife.getPrimaryHoldingFromOLifE(olifeIssue);
		Policy policyIssue = holdingIssue.getPolicy();
		Iterator it = policyIssue.getFinancialActivity().iterator();
		FinancialActivity issFinancialActivity;
		double mip = policyIssue.getMinPremiumInitialAmt();
		double paymentAmount = policyIssue.getPaymentAmt();
		double totalPayment = 0;
		boolean firstTimeMIPMet = false;
		Date mipDate = null;
		int index = 0;
		while (it.hasNext()) {
			issFinancialActivity = (FinancialActivity) it.next();
			if (issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_PREMIUMINIT
					|| issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CWA
					|| issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_PYMNTSHORTAGE
					|| issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_ROLLOVEREXT1035
					|| issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_1035INIT
					|| issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_1035SUBS
					|| issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CARRYOVERLOAN) {
				OLifEExtension oliExt = issFinancialActivity.getOLifEExtensionAt(0);
				boolean isDisbursed = false;
				if (oliExt != null && oliExt.getFinancialActivityExtension() != null) {
					isDisbursed = oliExt.getFinancialActivityExtension().getDisbursedInd();
				}
				if (issFinancialActivity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_REV
						&& issFinancialActivity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_REFUND
						&& issFinancialActivity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND && isDisbursed != true) {
					index++;
					totalPayment += issFinancialActivity.getFinActivityGrossAmt();

					if (getParentCase() != null && NbaConstants.SYST_LIFE70.equals(parentCase.getNbaLob().getBackendSystem())) {
						if ((totalPayment >= mip - shortageAmt) && !firstTimeMIPMet) {
							firstTimeMIPMet = true;
							mipDate = issFinancialActivity.getFinEffDate();
						}
					} else if (getParentCase() != null && NbaConstants.SYST_CAPS.equals(parentCase.getNbaLob().getBackendSystem())) {
						if ((totalPayment >= paymentAmount - shortageAmt) && !firstTimeMIPMet) {
							firstTimeMIPMet = true;
							mipDate = issFinancialActivity.getFinEffDate();
						}
					}

				}

			}

		}
		return mipDate;
	}
	
	protected void processPrint() throws NbaBaseException {
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
		if (policyExtension != null && reprintInd) {
			policyExtension.setContractChangeReprintInd(reprintInd);
			policyExtension.setActionUpdate();
		}
		// End APSL915
		// begin NBA100
		// NBA095 line removed
		setParentCase(retrieveParentCaseOnly());// APSL4585
		doProcess();
		if (getResult() == null) {
			if (isNewSource()) {
				getTransactionDst().addNbaSource(nbaSource); // Attach the Contract Print Extract Source to the Work Item
			}
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		} else {
			isSuspendNeeded = false;//APSL5100
		}
		if (isSuspendNeeded) {
			suspendPrintWork();
		} else {
			changeStatus(getResult().getStatus(), getRouteReason()); //APSL5128
			// NBA095 line removed
			// end NBA100
			doUpdateWorkItem(); // also unlocks the case
			// Begin APSL4374

			if (getParentCase() != null
					&& (getParentCase().getQueue().equalsIgnoreCase(NbaConstants.QUEUE_REQHLD) || getParentCase().getQueue().equalsIgnoreCase(
							NbaConstants.QUEUE_APPHLD)) && isAllPrintInEnd(getWork().getNbaLob().getPolicyNumber())) {
				NbaDst parent = getParentCase();
				int newPriority = 999;
				parent.increasePriority("=", Integer.toString(newPriority));
				update(parent);
				NbaSuspendVO suspendVO = new NbaSuspendVO();
				suspendVO.setCaseID(parent.getID());
				unsuspendWork(suspendVO);
			}
		}
		unlockWork(getParentCase()); // End APSL4374
	}
	
	  protected EPolicyData retrieveActiveEPolicyDataForCurrentPrint(){
		  PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
		  if(policyExtension != null && policyExtension.getEPolicyDataCount()>0 ){
			  ArrayList<EPolicyData> ePolicyDataList = policyExtension.getEPolicyData();
			  for(EPolicyData ePolData : ePolicyDataList){
				if (ePolData.getActive() && ePolData.getPrintCRDA() != null && (ePolData.getPrintCRDA()).equalsIgnoreCase(getWork().getID())) {
					return ePolData;
				}
			  }
		  }
	  return null;
	  }
	  
	  
	  protected EPolicyData retrieveActiveEPolicyData(){
		  PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
		  if(policyExtension != null && policyExtension.getEPolicyDataCount()>0 ){
			  ArrayList<EPolicyData> ePolicyDataList = policyExtension.getEPolicyData();
			  for(EPolicyData ePolData : ePolicyDataList){
				if (ePolData.getActive()) {
					return ePolData;
				}
			  }
		  }
	  return null;
	  }
	  
	  /**
		 * Suspends the workitem in AWD.
		 * @param suspendVO the NbaSuspendVO object containing workitem id
		 * @throws NbaBaseException if a RemoteException occurs
		 */
		//APSL5100 New Method
		public void suspendPrintWork() throws NbaBaseException {
			suspendVO = new NbaSuspendVO();
			int suspentionMinutes = 60;
			suspendVO.setTransactionID(getWork().getID());
			getLogger().logDebug("Starting suspendPrint");
			StringBuffer newReason = new StringBuffer();
			newReason.append("Print work is suspended due to ePolicy link not received ");//APSL5162
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(new Date());
			calendar.add(Calendar.MINUTE, suspentionMinutes); 
			suspendVO.setActivationDate(calendar.getTime());
			setSuspendVO(suspendVO);
			addComment(newReason.toString());
			updateForSuspend();
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
		}
		
		
	/**
	 * Business Process to call send Print Status to Epolicy. 
	 * 
	 * @throws NbaBaseException
	 * 
	 */
	// APSL5100 New Method
	protected void performPrintStatusWebServiceCall(EPolicyData ePolData) throws NbaBaseException {
		try {
			ePolData.setPrintStatus(NbaOliConstants.OLI_PRINT_STATUS_NIGO);
			ePolData.setActionUpdate();
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SEND_PRINT_PREVIEW_STATUS, this.user,
					getNbaTxLife(), work, null);
			webServiceInvoker.execute();
		} catch (NbaBaseException ex) {
			if (ex.getMessage()!=null && ex.getMessage().contains(ePolData.getEPolicyPrintID())) {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug(ex.getMessage());
				}} else {
					if (!ex.isFatal()) {
						throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_WS, ex.getMessage());
					}
					throw ex;
				}
			}
		
	}
	
	
	
	/**
	 * Business Process to Set TransRefGUID in Epolicy. 
	 * 
	 * 
	 * 
	 */
	// APSL5100 New Method
	protected void setSenderTransRefGUID(NbaTXLife nbaTXLife) {
		String transRefGUID = nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransRefGUID();
		EPolicyData ePolicyData = retrieveActiveEPolicyDataForCurrentPrint();
		if (ePolicyData != null && !NbaUtils.isBlankOrNull(transRefGUID)) {
			ePolicyData.setSenderTransrefGUID(transRefGUID);
			ePolicyData.setActionUpdate();
		}
		
	}
	//APSL5100 New Method
		/**
		 * Calls VP/MS model to determine if Preview Needed for this Case
		 * @param nbaLob the work LOBS
		 * @return true if an Preview needed else return false 
		 * @throws NbaBaseException
		 */
		protected boolean isPreviewNeeded() throws NbaBaseException {
			boolean previewNeededInd = true;
			NbaVpmsAdaptor vpmsAdaptor = null;
			try {
				Map deOink = new HashMap();
				NbaLob lob= getWork().getNbaLob();
				NbaOinkDataAccess data = new NbaOinkDataAccess(lob);
				data.setContractSource(nbaTxLife, lob);
				vpmsAdaptor = new NbaVpmsAdaptor(data, NbaVpmsConstants.AUTO_PROCESS_STATUS);
				vpmsAdaptor.setVpmsEntryPoint(NbaVpmsConstants.EP_PREVIEW_NEEDED);
				vpmsAdaptor.setSkipAttributesMap(deOink);
				// get the string out returned by VP / MS Model
				VpmsComputeResult rulesProxyResult = vpmsAdaptor.getResults();
				if (!rulesProxyResult.isError()) {
					NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
					List resultData = vpmsResultsData.getResultsData();
					if (!resultData.isEmpty()) {
						previewNeededInd = Boolean.parseBoolean((String)resultData.get(0));
					 
					}
				}
				return previewNeededInd;
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
		
		//NBLXA-188(APSL5318) Legacy Decommissioning New Method
	protected void updatePDRIndForGI() throws NbaBaseException {
		if (NbaUtils.isGIApplication(getNbaTxLife(), work)) {
			Policy policy = getNbaTxLife().getPolicy();
			PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
			if (null != policyExt) {
				// String extComp2 = getWork().getNbaLob().getExtractCompAt(2);
				// boolean reprintInd = (extComp2 != null && extComp2.trim().length() > 0);
				if (reprintInd || getNbaTxLife().isPaidReIssue()) {
					policyExt.setMDRConsentIND(false);
					policyExt.setPrintTogetherIND(false);
				}
				NbaSystemDataDatabaseAccessor.updateMDRConsentInd(policyExt.getMDRConsentIND(), policyExt.getContractKey());
				if (!policyExt.getMDRConsentIND()) {
					policyExt.setPdrInd(true);
				} else {
					policyExt.setPdrInd(false);
				}
				/*
				 * ApplicationInfoExtension appInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy()
				 * .getApplicationInfo()); boolean isEmployeeOwned = false; if (null != appInfoExtension) { isEmployeeOwned =
				 * (appInfoExtension.getOwnerTypeCode() == 2); } if (policyExt.getMDRConsentIND() && policyExt.hasGIBatchID()) { boolean
				 * isContractPrintExtractUpdatedForAll = NbaSystemDataDatabaseAccessor.isContractPrintExtractDateUpdatedForAll(policyExt
				 * .getGIBatchID()); if (isContractPrintExtractUpdatedForAll) { policyExt.setPdrInd(true); } else { policyExt.setPdrInd(false); } //
				 * isContractUpdateRequired = true; } else if (isEmployeeOwned && !policyExt.getMDRConsentIND()) { policyExt.setPdrInd(true); //
				 * isContractUpdateRequired = true; }
				 */
				policyExt.setActionUpdate();
			}
		}
	}
		
	//New Method: NBLXA-2603[NBLXA-2610]
	protected void changeInDeliveryMethodNeeded() throws NbaBaseException {
		List<String> comments = new ArrayList<String>();
		Policy policy = getNbaTxLife().getPolicy();
		if (!NbaUtils.isBlankOrNull(policy)) {
			ApplicationInfo appInfo = policy.getApplicationInfo();
			if (!NbaUtils.isBlankOrNull(appInfo)) {
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
				long deliveryMethod = appInfoExt.getReqPolicyDeliverMethod();
				if (deliveryMethod == NbaOliConstants.OLI_POLDELMETHOD_EMAIL) {
					List<UnderwritingResult> uwResultList = appInfoExt.getUnderwritingResult();
					if (!NbaUtils.isBlankOrNull(uwResultList) && uwResultList.size() > 0) {
						for (UnderwritingResult uwResult : uwResultList) {
							if (!NbaUtils.isBlankOrNull(uwResult.getUnderwritingResultReason())) {
								comments.add("AUD letter is present");
							}
						}
					}
					if (NbaUtils.isWholeSale(policy)) {
						comments.add("Wholesale policy");
					}
					// NBLXA-2603[NBLXA-2630] Begin
					if(NbaUtils.isSurvivorshipProduct(getNbaTxLife())) {
						comments.add("Survivorship");
					}  // NBLXA-2603[NBLXA-2630] End
					if(appInfoExt.getInternationalUWProgInd()) {
						comments.add("IUP Case");
					}
					//Add conditions here for rest of the scenarios
					if (comments.size() > 0) {
						appInfoExt.setReqPolicyDeliverMethod(NbaOliConstants.OLI_POLDELMETHOD_REGULARMAIL);
						appInfoExt.setActionUpdate();
						doContractUpdate(); //For persisting delivery method
						//Combining & formatting comment
						String comment = "";
						for (String temp: comments) {
							if (NbaUtils.isBlankOrNull(comment)) {
								comment = temp;
							} else {
								comment = comment + "," + temp;
							}	
						}		
						addComment("Print delivery method changed from electronic to paper: "+ comment);
						updateWork();
					}
				}
			}
		}
	} 	
}