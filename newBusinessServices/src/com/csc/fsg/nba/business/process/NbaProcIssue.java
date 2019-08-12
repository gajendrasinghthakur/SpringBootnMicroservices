package com.csc.fsg.nba.business.process;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group�.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.csc.fs.accel.ui.util.SortingHelper;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.business.transaction.NbaClientSearchTransaction;
import com.csc.fsg.nba.business.transaction.NbaHoldingInqTransaction;
import com.csc.fsg.nba.contract.extracts.NbaCwaPaymentsExtract;
import com.csc.fsg.nba.contract.validation.NbaContractValidation;
import com.csc.fsg.nba.database.NbaAutoClosureAccessor;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaAutoClosureContract;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Category;
import com.csc.fsg.nba.vo.configuration.Integration;
import com.csc.fsg.nba.vo.txlife.AccountingActivity;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PaymentReportingFinActivity;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;
/**
 * NbaProcIssue is the class that processes nbAccelerator cases found
 * on the AWD issue queue (NBISSUE). It creates and sends a final disposition
 * to the back-end system to issue the contract.
 * <p>NbaProcIssue implements the Singleton pattern. The singleton is
 * accessed through the getInstance() method and the automated process
 * is initiated through the executeProcess method.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA012</td><td>Version 2</td><td>Contract Extract Print</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBP001</td><td>Version 3</td><td>nbProducer Initial Development</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>SPR1274</td><td>Version 3</td><td>The print process needs to be configurable in the configuration file</td></tr>
 * <tr><td>NBA051</td><td>Version 3</td><td>Allow Search on Work Items</td></tr>
 * <tr><td>NBA087</td><td>Version 3</td><td>Post Approval & Issue Requirements</td></tr>
 * <tr><td>NBA036</td><td>Version 3</td><td>nbA Underwriter Workbench Transaction to DB</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>NBA076</td><td>Version 3</td><td>Contract to Issue Admin</td></tr>
 * <tr><td>NBA067</td><td>Version 3</td><td>Client Search</td></tr>
 * <tr><td>SPR1719</td><td>Version 4</td><td>General source code clean up</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
 * <tr><td>SPR2050</td><td>Version 4</td><td>FCVRPRIN, FCVINTRW, FCVSRDUR, and FCVSRPRD are not set correctly</td></tr>
 * <tr><td>SPR2366</td><td>Version 5</td><td>Added unique file names for webservice stubs</td></tr>
 * <tr><td>SPR1931</td><td>Version 5</td><td>Order of Trans Val and Issue Process is Wrong - Should Validate First</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR2817</td><td>Version 6</td><td>Pending Accounting Needs to Be Added to nbA</td>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirement/Reinsurance Changes</td></tr>
 * <tr><td>NBA123</td><td>Version 6</td><td>Administrator Console Rewrite</td></tr>
 * <tr><td>AXAL3.7.17</td><td>AXA Life Phase 1</td><td>CAPS Interface</td>
 * <tr><td>SPR3711</td><td>Version 8</td><td>Issue Process Does Not Check for Severe or Overridable Errors - Allows Bad Records to Be Sent to Host</td></tr>
 * <tr><td>NBA254</td><td>Version 8</td><td>Automatic Closure and Refund of CWA</td></tr>
 * <tr><td>ALPC066</td><td>AXA Life Phase 1</td><td>Term Series Qualified</td></tr>
 * <tr><td>ALS4153</td><td>AXA Life Phase 1</td><td>QC # 3031  - 3.7.4.2.1_3:  Transactional Message should appear "warning user that case must be UNAPPROVED" is not being invoked</td></tr>
 * <tr><td>P2AXAL005</td><td>AXA Life Phase 2</td><td>Legal Policy Stop</td></tr>
 * <tr><td>CR60956</td><td>AXA Life Phase 2</td><td>Life 70 Reissue</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcIssue extends NbaAutomatedProcess implements NbaOliConstants {
protected static final String UNRESOLVED_CONTACT_VALIDATION_ERRORS = "Unresolved Contract Validation errors are present"; //ALS3813
protected static final String REINSTATEMENT_MONEY_NOT_FOUND_ON_CONTRACT = "Reinstatement Money not found on contract";	//SPR3711
protected boolean standalone;	//SPR1931
//SPR3711 code deleted
protected NbaOLifEId nbaOLifEId;	//SPR2817
protected Date currentDate;		//SPR2817
//APSL459 code deleted

/**
 * NbaProcIssue constructor comment.
 */
public NbaProcIssue() {
	super();
	//SPR1851 code deleted
}
/**
 * Perform the Contract Issue business process:
 * - Retrieve the child work items and sources.
 * - Create an issue transaction
 * - Send the request to the adaptor for processing.
 * - Check for transmission errors
 * - Change status for the case
 * - Update AWD
 * - Update nbProducer database
 * @param user the user for whom the process is being executed
 * @param work a DST value object for which the process is to occur
 * @return an NbaAutomatedProcessResult containing information about
 *         the success or failure of the process
 */
//SPR3711 renamed arguments
public NbaAutomatedProcessResult executeProcess(NbaUserVO nbaUserVO, NbaDst nbaDst)
 throws com.csc.fsg.nba.exception.NbaBaseException {
		// NBA050 CODE DELETED
		if (!initialize(nbaUserVO, nbaDst)) { //NBA123 SPR3711
			return getResult(); //NBA123
		} //NBA123
		// APSL4461 Begin//code deleted
		// APSL4461 End //code deleted
		try { //SPR2817
			//SPR3711 code deleted
			//NBA123 code deleted
			//NBA077 code deleted
			// APSL459 code deleted.
			// Start NBLXA-1723
			if (getNbaTxLife().getPolicy() != null && isCWAOutstanding(getNbaTxLife().getPolicy().getPolNumber())) {
				addComment("Suspending application work item due to CWA work item is outstanding");
				Date reqSusDate = AxaUtils.addMinutesToDate(new Date(), -30);
				suspendVO = new NbaSuspendVO();
				suspendVO.setCaseID(getWork().getID());
				suspendVO.setActivationDate(reqSusDate);
				setSuspendVO(suspendVO);
				updateForSuspend();				
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
				return getResult();
			}
			//End NBLXA-1723
			boolean isPaidReissue = getNbaTxLife().isPaidReIssue(); //APSL3800(QC13716)
			//SC:  PED code APSL4585 Sept Release
			if(NbaConstants.SYST_LIFE70.equals(getWorkLobs().getBackendSystem()) && getNbaTxLife() != null && getNbaTxLife().getPolicy() != null && getNbaTxLife().getPolicy().getApplicationInfo() != null ){
				//Calculate shortage amount
				int shortageAmt = 0;
				NbaVpmsResultsData vpmsShortageAmtResultsData = getDataFromVPMS(NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS,NbaVpmsConstants.EP_CWA_SHORTAGE_LIMIT);
				if (vpmsShortageAmtResultsData != null && vpmsShortageAmtResultsData.getResultsData() != null) {
					shortageAmt = Integer.parseInt((String) vpmsShortageAmtResultsData.getResultsData().get(0));
				}
				if(getNbaTxLife().getPolicy().getApplicationInfo().getCWAAmt() >= getNbaTxLife().getPolicy().getMinPremiumInitialAmt() - shortageAmt){
					calculateMIP_IPEDDate(shortageAmt);		
				}


			}//EC:  PED code APSL4585 Sept Release
                processIssue();
                if (getResult() == null) {
                    addPrintExtractTransaction(getUser(), getWork(), getBusfunc(), getNbaTxLife()); // Add Print Extract Work item //NBA100 SPR3711
                    setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
                    // NBA100 code deleted
                    getWorkLobs().setCaseFinalDispstn((int) NBA_FINALDISPOSITION_ISSUED); // NBA012 //NBA087 SPR3711
                    // Begin ALPC066
                    if (NbaUtils.isKeoughPlan(getNbaTxLife().getLife().getQualPlanType())
                            && NbaUtils.isBlankOrNull(getNbaTxLife().getPolicy().getBillNumber())
                            && getNbaTxLife().getPaymentMethod() != OLI_PAYMETH_LISTBILL) { // ALS3927
                        addMiscWorkItem(FALSE_STR, "");// ALPC234, ALII966
                    }
                    // End ALPC066
                    // begin ALPC234
                    if (getWorkLobs().getBackendSystem().equals(NbaConstants.SYST_CAPS) && isPaidReissue) { // ALS5703, APSL459 CR60956
                                                                                                            // ,APSL3800(QC13716)
                        addMiscWorkItem(TRUE_STR, ""); // ALII966
                    }
                    // end ALPC234
                    // Begin P2AXAL005, ALII966
                    if (isLegalPolicyStop()) {
                        addMiscWorkItem(FALSE_STR, "LEGAL"); // ALII966
                        getWork().getNbaLob().setPaidChgCMQueue(getAlternateStatus());
                    }
                    if (isMultipleAssignee()) {
                        addMiscWorkItem(FALSE_STR, "MULTI"); // ALII966
                        getWork().getNbaLob().setPaidChgCMQueue(getAlternateStatus());
                    }
                    // End P2AXAL005, ALII966
                    // QC#10404, APSL2719 If Term To Term Internal Replacement is on the case set Paid change case manager Lob
                    // QC#11357 APSL2943 Internal term to term replacement workflow change set PCCM is moved to Post Issue
                    /*
                     * if (NbaUtils.isTermToTermIntReplInd(getNbaTxLife())) { getWork().getNbaLob().setPaidChgCMQueue(getAlternateStatus()); }
                     */
                 // Begin NBLXA-2399
    				if(nbaTxLife.isPaidReIssue() && getNbaTxLife().getPolicy().getReplacementType() == 2l)
                        {
    					List<Policy> termToTermPolicies = NbaUtils.getTermToTermInternalReplPolicies(getNbaTxLife());
    					if (!termToTermPolicies.isEmpty()) {
    						NbaVpmsAdaptor vpmsProxy = null;
    						try {
    							NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
    							vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.INFORMALTOFORMAL);
    							vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_TERM_TO_TERM_REPLACEMENT);
    							NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
    							List resultsData = data.getResultsData();
    							// The entry point in VP/MS is configured to return business area, work type and status.
    							if (resultsData != null && resultsData.size() >= 3) {
    								NbaDst lifeSRVTransaction = AxaUtils.createLifeSrvTransaction((String) resultsData.get(0), (String) resultsData.get(1),
    										(String) resultsData.get(2), termToTermPolicies,getWork().getNbaUserVO());
    								update(lifeSRVTransaction);
    								unlockWork(lifeSRVTransaction);
    								addComment("Work item created in LIFESRV Business Area");
    							}
    						} catch (java.rmi.RemoteException re) {
    							throw new NbaVpmsException("APISS" + NbaVpmsException.VPMS_EXCEPTION, re);
    						} finally {
    							try {
    								if (vpmsProxy != null) {
    									vpmsProxy.remove();
    								}
    							} catch (Throwable th) {
    								getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
    							}
    						}
    					}
    				}
    				// End NBLXA-2399
                    deleteXMLDataFromAuxiliary(); // ALL2080
                    addBICInformation(); //APSL5015 - DOL - method to set BIC specific values on pending cases
                }
                // begin SPR2817
		} catch (NbaBaseException e) {
			if (e.isFatal()) {
				throw e;
			}
			getLogger().logException(e);
			throw e; // APSL3874
			 // code deleted for APSL3874
		}
		//end SPR2817
		
		changeStatus(getResult().getStatus());
		//NBA087 code deleted
		
		doUpdateWorkItem();
		//NBA020 code deleted
		return getResult();
}

/**
 * Verify that the transaction is logical for the Case. If so, process the Reinstatement, Increase or Issue transaction.
 * 
 * @throws NbaBaseException
 */
//NBA077 New Method
protected void processIssue() throws NbaBaseException {
//	SPR3711 code deleted
	setStandalone(NbaServerUtility.isDataStoreDB(getWorkLobs(), getUser())); //SPR1931 SPR3711
	validateContractForChangeRequest(); //SPR1931
	if(getResult() == null){	
		//Check if standalone or wrappered	
		if (isStandalone()) { //SPR1715 //SPR1931
			processStandalone();
		} else {
			processWrappered();
		}
	}
}

/**
 * Perform standalone mode processing for Reinstatements, Increases and Issue. 
 * If the case is Reinstatement case and Suspense money is present with that case,
 * create a xml508 transaction. Call the web service adaptor. If the communication is successful mark the the lapsed policy as active. 
 * If the Case is an increase, create a TXLife TransType=103, TransSubType=1000500004, changeSubType=1000500028 transaction 
 * from the increase rider in the nbaTxLife and workitem's LOBs. Call the web service adaptor.
 * Otherwise treat the case as a new issue. Create a TXLife TransType 103, TransSubType 1000500004 transaction 
 * and call the web service adaptor.
 * @throws NbaBaseException
 */
//NBA077 New Method
//SPR1931 changed method visibility
  protected void processStandalone() throws NbaBaseException {
  	performPreIssueValidation();	//SPR3711
  	if (getResult() == null) {  		//SPR3711
	//begin SPR1931
	if (isReinstatment()) {	 
		FinancialActivity suspenseFinActivity = getSuspenseMoney();
		if (suspenseFinActivity == null) {
			return;
		}
			//SPR3711 code deleted
			processReinstatementPayment(suspenseFinActivity);	//SPR3711
	} else if (!isIncrease()) { //Assume Issue
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getNbaTxLife().getOLifE());
		holding.getPolicy().setPolicyStatus(NbaOliConstants.OLI_POLSTAT_ISSUED);
		holding.getPolicy().setActionUpdate();
 	    setOverageAmount(nbaTxLife); //NBLXA-1457,NBLXA-1643
		// Begin CR60956
		if(NbaConstants.SYST_LIFE70.equals(getWorkLobs().getBackendSystem()) && nbaTxLife.isPaidReIssue()) {
			//Trigger the Reissue request 203 xml and retrieve the reissue data
			NbaTXLife reissueData = getReissueData();
			long policyStatus = mergeLife70Details(reissueData, null); // APSL3928
			
			if (policyStatus != NbaOliConstants.OLI_POLSTAT_REISSUE) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Bad Policy Status", getOtherStatus()));
			}
		}
		//End CR60956
		//begin SPR2817
		//ALII53 code commented
//		try {
//            generateIssueAccounting();	
//        } catch (NbaBaseException e) {
//            if (e.isFatal()) {
//                throw e;
//            }
//            getLogger().logException(e);
//            String message = e.getMessage();
//            addComment(message);
//            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, message, getHostErrorStatus()));
//        }
        //end SPR2817
	}
	if (getResult() == null) {	//SPR2817
		//Begin NBA254
		//begin ALS4153
		ApplicationInfo appInfo = nbaTxLife.getPrimaryHolding().getPolicy().getApplicationInfo();
		ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (appInfoExtn == null) {
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
			olifeExt.setActionAdd();
			appInfo.addOLifEExtension(olifeExt);
			appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		//end NBA093
		}
		appInfoExtn.setUnderwritingStatus((int) NBA_FINALDISPOSITION_ISSUED); //NBA012
		//end ALS4153
		appInfo.setPlacementEndDate((Date) null);
		//APSL459 code deleted 
		appInfoExtn.setClosureType(null);
		appInfoExtn.setClosureOverrideInd(FALSE);
		appInfoExtn.setClosureInd(NbaConstants.CLOSURE_NOT_APPLICABLE);
		appInfo.setActionUpdate();
		appInfoExtn.setActionUpdate();
		NbaAutoClosureContract autoClosureContract = new NbaAutoClosureContract();
		autoClosureContract.setContractNumber(nbaTxLife.getPrimaryHolding().getPolicy().getPolNumber());
		NbaAutoClosureAccessor.delete(autoClosureContract);
		//End NBA254
		// APSL5335 -- START Set IssueDate for Issued Case 
		Policy policy =  nbaTxLife.getPrimaryHolding().getPolicy();
		if(policy !=null){
			policy.setIssueDate(getCurrentDateFromWorkflow(getUser()));
			policy.setActionUpdate();
		}
		// APSL5335 -- END Set IssueDate for Issued Case 
	    handleHostResponse(doContractUpdate());
	}	//SPR2817
	}	//SPR3711
	//end SPR1931
}

/**
 * Process Issue transaction in wrappered mode
 * @throws NbaBaseException
 */
//NBA077 New Method
protected void processWrappered() throws NbaBaseException {
	//	Begin NBA067
	String backendSystem = getWorkLobs().getBackendSystem();	//SPR1931 SPR3711
	Integration nbaConfigIntegration = NbaConfiguration.getInstance().getIntegration(backendSystem);	//ACN012
	if (nbaConfigIntegration != null) {
		//begin ACN012
		ArrayList nbaConfigCategory = nbaConfigIntegration.getCategory();	//ACN012
		int count = nbaConfigCategory.size();
		Category category = null;
		for (int i = 0; i < count; i++) {
			category = nbaConfigIntegration.getCategoryAt(i);
			if (category.getId().equalsIgnoreCase("Client")){
				if (category.getValue().equalsIgnoreCase("CLIFIntegratedClient") || category.getValue().equalsIgnoreCase("VTG1Client")) {
					refreshClientInfo(work, user, nbaTxLife.getOLifE().getParty(), backendSystem);
					break;
				}
			}
		}
		//end ACN012
	} //END NBA067
	nbaTxLife.getPrimaryHolding().getPolicy().setApplicationInfo(createApplicationInfoObject()); // NBA050
	
	// APSL5335 -- START Set IssueDate for Issued Case 
	Policy policy =  nbaTxLife.getPrimaryHolding().getPolicy();
	if(policy !=null){
		policy.setIssueDate(getCurrentDateFromWorkflow(getUser()));
		policy.setActionUpdate();
	}
	// APSL5335 -- END Set IssueDate for Issued Case 
	
	
	handleHostResponse(doContractUpdate()); // NBA050
}
	/**
	 * Creates an application info object with information needed to approve the case.
	 * Updates the action indicator to ensure the back end system is updated properly.
	 * @return a newly created ApplicationInfo object containing necessary information
	 */
	// NBA050 NEW METHOD
	protected ApplicationInfo createApplicationInfoObject() throws NbaBaseException {
		ApplicationInfo appInfo;	//NBA093
		if (nbaTxLife.getPrimaryHolding().getPolicy().hasApplicationInfo()) {
			appInfo = nbaTxLife.getPrimaryHolding().getPolicy().getApplicationInfo();
		} else {
			appInfo = new ApplicationInfo();
			appInfo.setActionAdd();	//NBA093
			nbaTxLife.getPrimaryHolding().getPolicy().setApplicationInfo(appInfo);	//NBA093
		}
		// Add ApplicationInfoExtension
		//begin NBA093
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (appInfoExt == null) {
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
			olifeExt.setActionAdd();
			appInfo.addOLifEExtension(olifeExt);
			appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		//end NBA093
		}
		appInfo.setRequestedPolDate(getWork().getNbaLob().getIssueDate()); //NBA093
		appInfoExt.setUnderwritingStatus((int) NBA_FINALDISPOSITION_ISSUED); //NBA012
		appInfoExt.setActionUpdate(); //NBA036
		//NBA093 code deleted
		appInfo.setActionUpdate(); //NBA093
		return appInfo;
	}
	/**
	 * Calls the translation tables for UCT Tables
	 * @param tableName The name of the UCT table.
	 * @param compCode Company code.
	 * @param covKey Coverage key(pdfKey).
	 * @return tarray NbaTableData.
	 */
	//SPR1274 new method
	protected NbaUctData[] getUctTable() {
		HashMap aCase = new HashMap();
		aCase.put("company", "*");
		aCase.put("tableName", NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		aCase.put("plan", "*");
		aCase.put("backendSystem", "*");
		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loading UCT " + NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		NbaTableAccessor ntsAccess = new NbaTableAccessor();
		NbaUctData[] tableData = null;
		try {
			tableData = (NbaUctData[]) ntsAccess.getDisplayData(aCase, NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		} catch (NbaDataAccessException e) {
			if (getLogger().isWarnEnabled())
				getLogger().logWarn("NbaDataAccessException Loading UCT " + NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		}
		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loaded UCT " + NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		return (tableData);
	}
	//NBA067
	/**
	 * Calls the ClientSearch webservice to refresh the client information for all parties 
	 * @param work a DST value object for which the process is to occur 
	 * @param user the user for whom the process is being executed
	 * @param parties List of parties for which the client information has to be refreshed
	 * @param backendSystem Backend System name(example, CLIF, VNTG etc).
	 */
	//NBA067 new method
	protected void refreshClientInfo(NbaDst work, NbaUserVO user, java.util.List parties, String backendSystem) { 
		String partyType = "";
		NbaTXLife txLifeClientSearchResp = null;
		for (int p = 0; p < parties.size(); p++) {
			Party party = (Party) parties.get(p);
			partyType = String.valueOf(party.getPartyTypeCode());
			if (party.hasPersonOrOrganization()) {
				if (party.getPersonOrOrganization().getPerson() != null) {
					partyType = "1";
				}
				if (party.getPersonOrOrganization().getOrganization()!= null) {
					partyType = "2";
				}
				/*
				 * CALL the Client Search Web Service and retrieve the Response from it
				 * 
				 */
				try {
					NbaClientSearchTransaction clientSearch = 	new NbaClientSearchTransaction();
					NbaTXLife xmlTransaction = 	clientSearch.createTXLife301(user, party);

					NbaWebServiceAdapter service = 
						NbaWebServiceAdapterFactory.createWebServiceAdapter(backendSystem,	"Client", "ClntSearch");
					if (backendSystem.equalsIgnoreCase("CLIF")){
						txLifeClientSearchResp = service.invokeWebService(xmlTransaction); // SPR2968
					} else {
						txLifeClientSearchResp = service.invokeWebService(xmlTransaction);//SPR2366 SPR2968
					}
				} catch (Exception e) {
					getLogger().logException("Exception in Client Search process", e); //NBA103
				}

				if (txLifeClientSearchResp != null && txLifeClientSearchResp.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseCount()> 0) {
					TXLifeResponse aTXLifeResponse = txLifeClientSearchResp.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0);
					if ((aTXLifeResponse.getTransResult().getResultCode() != 1) || (aTXLifeResponse.getTransResult().getResultInfoCount() > 0)) {
							continue;
					}
					Party csParty = aTXLifeResponse.getOLifE().getPartyAt(0);
					if(csParty.hasGovtID()){
					  party.setGovtID(csParty.getGovtID());
					}
					if(csParty.hasGovtIDTC()){
					  party.setGovtIDTC(csParty.getGovtIDTC());
					}  
					if (partyType == "1") {
						party.getPersonOrOrganization().getPerson().setLastName(csParty.getPersonOrOrganization().getPerson().getLastName());
						party.getPersonOrOrganization().getPerson().setFirstName(csParty.getPersonOrOrganization().getPerson().getFirstName());
						party.getPersonOrOrganization().getPerson().setMiddleName(csParty.getPersonOrOrganization().getPerson().getMiddleName());
						party.getPersonOrOrganization().getPerson().setBirthDate(csParty.getPersonOrOrganization().getPerson().getBirthDate());

					} else {
						party.getPersonOrOrganization().getOrganization().setDBA(party.getPersonOrOrganization().getOrganization().getDBA());

					}
					java.util.List newAddresses = csParty.getAddress();
						java.util.List oldAddresses = party.getAddress();
						if (newAddresses.size() > 0) {

							for (int a = 0; a < newAddresses.size(); a++) {
								Address address = (Address) newAddresses.get(a);
								if (a < oldAddresses.size() && oldAddresses.size() != 0) {
									if (address.hasAddressTypeCode()){
									party.getAddressAt(a).setAddressTypeCode(address.getAddressTypeCode());
									}									
									if (address.hasLine1()){
									party.getAddressAt(a).setLine1(address.getLine1());
									}
									if (address.hasLine2()){
									party.getAddressAt(a).setLine1(address.getLine2());
									}
									if (address.hasLine3()){
									party.getAddressAt(a).setLine1(address.getLine3());
									}
									if (address.hasCity()){
									party.getAddressAt(a).setCity(address.getCity());
									}
									if (address.hasAddressState()){
									party.getAddressAt(a).setAddressState(address.getAddressState());
									}
									if (address.hasZip()){
									party.getAddressAt(a).setZip(address.getZip());
									}
									if (address.hasAddressCountry()){
									party.getAddressAt(a).setAddressCountry(address.getAddressCountry());
									}
								} else {
									Address newAddress = new Address();
									if (address.hasAddressTypeCode()){
										newAddress.setAddressTypeCode(address.getAddressTypeCode());
									 }
									if (address.hasLine1()){
										newAddress.setLine1(address.getLine1());
									}
									if (address.hasLine2()){
										newAddress.setLine2(address.getLine2());
									}
									if (address.hasLine3()){
										newAddress.setLine3(address.getLine3());
									}
									if (address.hasCity()){
										newAddress.setCity(address.getCity());
									}
									if (address.hasAddressState()){
										newAddress.setAddressState(address.getAddressState());
									}
									if (address.hasZip()){
										newAddress.setZip(address.getZip());
									}
									if (address.hasAddressCountry()){
										newAddress.setAddressCountry(address.getAddressCountry());
									}									
									party.addAddress(newAddress);
								}
							}
						}
					java.util.List newPhones = csParty.getPhone();
					java.util.List oldPhones = party.getPhone();
					if (newPhones.size() > 0) {

						for (int a = 0; a < newPhones.size(); a++) {
							Phone phone = (Phone) newPhones.get(a);
							if (a < oldPhones.size() && oldPhones.size() != 0) {
								if(phone.hasAreaCode()){
								party.getPhoneAt(a).setAreaCode(phone.getAreaCode());
								}
								if(phone.hasDialNumber()){
								party.getPhoneAt(a).setDialNumber(phone.getDialNumber());
								}
								if(phone.hasPhoneTypeCode()){
								party.getPhoneAt(a).setPhoneTypeCode(phone.getPhoneTypeCode());
								}
							} else {
								Phone newPhone = new Phone();
								if(phone.hasAreaCode()){
									newPhone.setAreaCode(phone.getAreaCode());
								}
								if(phone.hasDialNumber()){
									newPhone.setDialNumber(phone.getDialNumber());
								}
								if(phone.hasPhoneTypeCode()){
									newPhone.setPhoneTypeCode(phone.getPhoneTypeCode());
								}									
								party.addPhone(newPhone);
							}
						}
					}
					java.util.List newEmails = csParty.getEMailAddress();
					java.util.List oldEmails = party.getEMailAddress();
					if (newEmails.size() > 0) {

						for (int a = 0; a < newEmails.size(); a++) {
							EMailAddress email = (EMailAddress) newEmails.get(a);
							if (a < oldEmails.size() && oldEmails.size() != 0) {
								if(email.hasEMailType()){
								party.getEMailAddressAt(a).setEMailType(email.getEMailType());
								}
								if(email.hasAddrLine()){
								party.getEMailAddressAt(a).setAddrLine(email.getAddrLine());
								}
							} else {
								EMailAddress newEmail = new EMailAddress();
								if(email.hasEMailType()){
								  newEmail.setEMailType(email.getEMailType());
								}
								if(email.hasAddrLine()){
								  newEmail.setAddrLine(email.getAddrLine());
								}									
								party.addEMailAddress(newEmail);
							}
						}
					}

				}
			}
		}
	}
	/**
	 * This method processes Reinstatement payment to make the policy back to active status from lapsed.
	 * @param suspenseFinActivity FinancialActivity object
	 * @param olifeId NbaOLifEId
	 * @throws NbaBaseException  
	 */
	//NBA077 new method
//	SPR3711 removed NbaOLifEId operand. Removed throws clause.
	protected void processReinstatementPayment(FinancialActivity suspenseFinActivity) {
		createFinActToRemoveSuspenseMoney(suspenseFinActivity);	//SPR3711
		FinancialActivity finActForReinstatement = createFinActForReinstatement(suspenseFinActivity);	//SPR3711
		finActForReinstatement.setActionAdd();
		getNbaTxLife().getPrimaryHolding().getPolicy().addFinancialActivity(finActForReinstatement);
		updateSuspenseFinancialActivity(suspenseFinActivity);
		getNbaTxLife().getPrimaryHolding().getPolicy().setActionUpdate();
	}	
	/**
	 * This method returns the FinancialActivity object which has FinActivityType of UNAPPLDCASHIN - 278
	 * @return finActivity FinancialActivity object
	 */
	//NBA077 new method
	protected FinancialActivity getSuspenseMoney() {
		Policy policy = getNbaTxLife().getPrimaryHolding().getPolicy();	//SPR1931
		int finActCount = policy.getFinancialActivity().size();	//SPR1931
		FinancialActivity finActivity = null;
		for (int i = 0; i < finActCount; i++) {
			finActivity = policy.getFinancialActivityAt(i);	//SPR1931
			if (NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN == finActivity.getFinActivityType()) {
				return finActivity;
			}
		}
		addComment(REINSTATEMENT_MONEY_NOT_FOUND_ON_CONTRACT);	//SPR1931	SPR3711
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, REINSTATEMENT_MONEY_NOT_FOUND_ON_CONTRACT, getHostFailStatus()));	//SPR1931 SPR3711
		return null;
	}
	/**
	 * This method creates a new FinancialActivity object with FinActivityType of UNAPPLDCASHOUT - 279 to remove
	 * UNAPPLDCASHIN - 278 money
	 * @param suspenseFinActivity FinancialActivity object
	 * @param olifeId NbaOLifEId
	 */
	//NBA077 new method
//	SPR3711 removed NbaOLifEId operand
	protected void createFinActToRemoveSuspenseMoney(FinancialActivity suspenseFinActivity) {
		FinancialActivity removeSuspenseFinActivity = suspenseFinActivity.clone(false);
		removeSuspenseFinActivity.deleteId();
//		SPR3711 code deleted
		removeSuspenseFinActivity.setAccountingActivity(new ArrayList());
		removeSuspenseFinActivity.setFinActivityType(NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHOUT);
		FinancialActivityExtension finActExtension = NbaUtils.getFirstFinancialActivityExtension(removeSuspenseFinActivity);
		if (finActExtension != null) {
			finActExtension.setDisbursedInd(false);
			finActExtension.setAcctgExtractInd(false);
		}
		removeSuspenseFinActivity.setActionAdd();
		getNbaTxLife().getPrimaryHolding().getPolicy().addFinancialActivity(removeSuspenseFinActivity);
	}
	/**
	 * This method changes the value of DataRep attribute from "Full" to "Removed" in FinancialActivity object with
	 * FinActivityType of UNAPPLDCASHIN - 278
	 * @param suspenseFinActivity FinancialActivity object
	 */
	//NBA077 new method
	protected void updateSuspenseFinancialActivity(FinancialActivity suspenseFinActivity) {
		suspenseFinActivity.setDataRep(NbaOliConstants.DATAREP_TYPES_REMOVED);
		suspenseFinActivity.setActionUpdate();
	}	
	/**
	 * This method creates a new FinancialActivity object with FinActivityType of REINPYMT - 248. In this new
	 * FinancialActivity object the AcctgExtractInd will be set to true so that extracts will not be generated for this
	 * Reinstatement payment.
	 * @param suspenseFinActivity FinancialActivity object
	 * @param olifeId NbaOLifEId
	 * @return finActForReinstatement FinancialActivity object
	 */
	//NBA077 new method
//	SPR3711 removed NbaOLifEId operand
	protected FinancialActivity createFinActForReinstatement(FinancialActivity suspenseFinActivity) {
		FinancialActivity finActForReinstatement = suspenseFinActivity.clone(false);
		finActForReinstatement.deleteId();
//		SPR3711 code deleted
		finActForReinstatement.setAccountingActivity(new ArrayList());
		finActForReinstatement.setFinActivityType(NbaOliConstants.OLI_FINACT_REINPYMT);
		FinancialActivityExtension finActExtension = NbaUtils.getFirstFinancialActivityExtension(finActForReinstatement);
		if (finActExtension != null) {
			finActExtension.setDisbursedInd(false);
			finActExtension.setAcctgExtractInd(true);
			finActExtension.deleteErrCorrInd();
		}
		return finActForReinstatement;
	}

	/**
	 * This method will validate whether contract is in valid status for requested change type.
	 * If not valid than this method will add a comment to AWD and set status to host fail status.
	 * @param isStandAlone the operating mode indicator
	 */
	//NBA077 New Method
	// SPR1931 removed parameter, isStandAlone 
	protected void validateContractForChangeRequest() throws NbaBaseException{
		if (getWorkLobs().getContractChgType() != null) {	//SPR1931	SPR3711
			long cntchgType = Long.parseLong(getWorkLobs().getContractChgType());	//SPR1931	SPR3711
			if (cntchgType == NbaOliConstants.NBA_CHNGTYPE_RERATING) {
				addComment("Rerate case");
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Rerate case", getHostFailStatus()));
			} else if (cntchgType == NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT) {
				if (!isStandalone()) {	//SPR1931
					addComment("Wrappered Reinstatement Case");
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Wrappered Reinstatement Case", getHostFailStatus()));
				}
			} else if (cntchgType == NbaOliConstants.NBA_CHNGTYPE_INCREASE) {
				if (!isStandalone()) {	//SPR1931
					addComment("Wrappered Increase Case");
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Wrappered Increase Case", getHostFailStatus()));
				}
			}
		}
	}
	/**
	 * Return the boolean which indicates whether the Case is standalone mode 
	 * @return standalone
	 */
	// SPR1931 New Method
	protected boolean isStandalone() {
		return standalone;
	}
	/**
	 * Set the boolean which indicates whether the Case is standalone mode
	 * @param b
	 */
	// SPR1931 New Method
	protected void setStandalone(boolean b) {
		standalone = b;
	}
//	SPR3711 code deleted
	/**
	 * Determine if the Case is a reinstatement by checking the CHTP LOB field for a value of 1000500900 
	 * @return true if the Case is a reinstatement
	 */
	// SPR1931 New Method
	protected boolean isReinstatment() {
		return getWorkLobs().getContractChgType() != null
		&& NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT == Long.parseLong(getWorkLobs().getContractChgType());	//SPR3711
	}
	/**
	 * Determine if the Case is an increase by checking the CHTP LOB field for a value of 1000500028 
	 * @return true if the Case is a reinstatement
	 */
	// SPR1931 New Method
	protected boolean isIncrease() {
		return getWorkLobs().getContractChgType() != null && NbaOliConstants.NBA_CHNGTYPE_INCREASE == Long.parseLong(getWorkLobs().getContractChgType());	//SPR3711
	}
	/**
	 * Generate Issue Accounting for eligible FinancialActivity. Issue Accounting is not generated for Vantage contracts
	 * @throws NbaBaseException when Accounting cannot be created
	 */
	// SPR2817 New Method
	protected void generateIssueAccounting() throws NbaBaseException {
        if ((!NbaConstants.SYST_VANTAGE.equalsIgnoreCase(getWorkLobs().getBackendSystem())) && 
        		(!NbaConstants.SYST_CAPS.equalsIgnoreCase(getWorkLobs().getBackendSystem()))) { //AXAL3.7.17	//SPR3711
            Policy policy = getNbaTxLife().getPrimaryHolding().getPolicy();
            FinancialActivity financialActivity;
            int count = policy.getFinancialActivityCount();
            for (int i = 0; i < count; i++) {
                financialActivity = policy.getFinancialActivityAt(i);
                if (NbaUtils.isIssueAccountingNeeded(financialActivity)) {
                    createIssueAccounting(policy, financialActivity);
                }
            }
        }
    }
	/**
	 * Add debit and credit AccountingActivity objects for Issue Accounting to the FinancialActivity
	 * @param policy - the Policy
	 * @param financialActivity - the FinancialActivity
	 * @throws NbaBaseException when Accounting cannot be created
	 */
	// SPR2817 New Method
	protected void createIssueAccounting(Policy policy, FinancialActivity financialActivity) throws NbaBaseException {
        NbaTableData[] nbaTableData = NbaUtils.getAccountingTableEntry(getWork(), policy, financialActivity, NbaConstants.ACCOUNTING_FOR_ISSUE);
        Relation relation = getNbaTxLife().getPrimaryInsOrAnnuitantRelation();
        AccountingActivity debit = NbaUtils.createAccountingActivity(policy, financialActivity, relation, getNbaOLifEId(), getCurrentDate(),
                nbaTableData, OLI_ACCTDBCRTYPE_DEBIT, NbaConstants.ACCOUNTING_FOR_ISSUE);
        AccountingActivity credit = NbaUtils.createAccountingActivity(policy, financialActivity, relation, getNbaOLifEId(), getCurrentDate(),
                nbaTableData, OLI_ACCTDBCRTYPE_CREDIT, NbaConstants.ACCOUNTING_FOR_ISSUE);
        if (debit != null && credit != null) {
            financialActivity.addAccountingActivity(debit);
            financialActivity.addAccountingActivity(credit);
        } else {
            throw new NbaBaseException("Issue/Paid Accounting Failed - Account Number Not Found");
        }
	}
	//NBA130 Code Deleted
	/**
	 * Returns the currentDate.
	 * @return Date
	 */
	// SPR2817 New Method
	protected Date getCurrentDate() {
		if (currentDate == null) {
		    currentDate = new Date(System.currentTimeMillis());
		}
		return currentDate;
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
	protected void performPreIssueValidation() throws NbaBaseException {
		String origBusinessProcess = getNbaTxLife().getBusinessProcess();
		getNbaTxLife().setBusinessProcess(NbaConstants.PROC_PREISSUEVALIDATION);// P2AXAL038
		new NbaContractValidation().validate(getNbaTxLife(), getWork(), getUser());
		if (getNbaTxLife().hasSignificantValidationErrors()) {
			// code deleted for APSL4143
			handleHostResponse(doContractUpdate()); // Update the contract to commit any contract Validation changes.
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_FUNC_CV); // APSL4143
		}
		getNbaTxLife().setBusinessProcess(origBusinessProcess); // Restore the original Business Process

	}
	/**
	 * @param Map of deOink variables
	 * @throws NbaBaseException
	 */
	//ALPC066 new method added ALPC234,ALII966 method signature changed
	protected void addMiscWorkItem(String miscWorkForReissue, String legalStopOrMultiAssignInd) throws NbaBaseException {
		//ALPC234 begin
		Map deOink = new HashMap();
		deOink.put("A_MiscWorkForReissue", miscWorkForReissue);
		deOink.put("A_SourceTypeLOB", A_ST_MISC_MAIL);
		deOink.put("A_LegalStopOrMultiAssignInd", legalStopOrMultiAssignInd);//ALII966
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOink);
		//ALPC234 end
		if (provider != null && provider.getWorkType() != null && provider.getInitialStatus() != null) {
			getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
		}
	}
	
	//APSL459 Method deleted protected boolean isReissue()
	
	//P2AXAL005 New Method
	protected boolean isLegalPolicyStop(){
		PolicyExtension policyExtension =NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
		return policyExtension != null && !NbaUtils.isBlankOrNull(policyExtension.getLegalStopType());
	}
    //P2AXAL005 New Method
	protected boolean isMultipleAssignee(){
	    return getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_COLLASSIGNEE) != null || 
	            getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_ASSIGNEE) != null;
	}
	
	/**
	 * This method returns the reissue request 203 response from the admin system Life 70. 
	 * @return response NbaTXLife object
	 * @throws NbaBaseException
	 */
	//CR60956 New Method
	private NbaTXLife getReissueData() throws NbaBaseException {
		NbaHoldingInqTransaction holdingTrx = new NbaHoldingInqTransaction();
		NbaTXRequestVO nbaTXRequest = holdingTrx.createRequestTransaction(getWorkLobs(), NbaConstants.READ, getUser().getUserID(), getUser());
		NbaTXLife response = holdingTrx.processInforceTransaction(getWorkLobs(), getUser());
		if (response != null && response.isTransactionError()) {
			throw new NbaBaseException("Error in calling Reissue Inquiry Webservice");
		} 
		return response;
	}
	
	/**
	 * This method deletes data from AXA_IPIPELINE_DATA (EApp XML) and NBA_SYSTEM_DATA (NBPROVSUPP, NBCORRXML) tables for an Issued Case. 
	 * @return response NbaTXLife object
	 * @throws NbaBaseException
	 */
	//ALII2080 New Method
	private void deleteXMLDataFromAuxiliary() {
		try {
			if (getWork().isCase()) {
				NbaSystemDataDatabaseAccessor.deleteIPipelineXMLData(getWork().getID());
				NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
				retOpt.setWorkItem(getWork().getID(), true);
				retOpt.requestTransactionAsChild();
				retOpt.setLockWorkItem();
				NbaDst caseWithTxns = retrieveWorkItem(getUser(), retOpt);
				if(caseWithTxns != null){
					ListIterator li = caseWithTxns.getNbaTransactions().listIterator();
					while (li.hasNext()) {
						NbaTransaction trans = (NbaTransaction) li.next();
						if( A_WT_REQUIREMENT.equalsIgnoreCase(trans.getTransaction().getWorkType())
								|| A_WT_CORRESPONDENCE.equalsIgnoreCase(trans.getTransaction().getWorkType())) {
							if (END_QUEUE.equalsIgnoreCase(trans.getQueue())) {
								NbaSystemDataDatabaseAccessor.deleteSystemSourceXMLData(trans.getID());
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			getLogger().logException("failure occurred in NbaProcIssue.deleteXMLDataFromAuxiliary() while deleting XML data, ", ex);
		}
	}
	
	//APSL4585 New Method
	//SC:  PED Code APSL4585 Sept Release
	private void calculateMIP_IPEDDate(int shortageAmt) {
		OLifE olifeIssue = getNbaTxLife().getOLifE();
		Holding holdingIssue = NbaTXLife.getPrimaryHoldingFromOLifE(olifeIssue);
		Policy policyIssue = holdingIssue.getPolicy(); 
		List finActivityList = new ArrayList();
		finActivityList=policyIssue.getFinancialActivity();
		/**
		 * QC17266 rearranges the financial activities in the ascending order of financial activity date. 
		 * @param financialActivityList list of applied financial activities*/
		 	
		SortingHelper.sortData(finActivityList, true, "finEffDate");
		Iterator it = finActivityList.iterator();
		FinancialActivity issFinancialActivity;
		ArrayList paymentReportingFinActivityList;
		double mip = policyIssue.getMinPremiumInitialAmt();
		double totalPayment = 0 ;
		boolean firstTimeMIPMet = false;
		Date mipDate = null;
		int index = 0;
		
		
		ApplicationInfo appinfo = policyIssue.getApplicationInfo();
		ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(appinfo);
		paymentReportingFinActivityList = appInfoExtn.getPaymentReportingFinActivity();
		if(paymentReportingFinActivityList == null){
			paymentReportingFinActivityList = new ArrayList();;
		}

		for (int i = 0; i < paymentReportingFinActivityList.size(); i++) {
			PaymentReportingFinActivity prFinActivity = (PaymentReportingFinActivity) paymentReportingFinActivityList.get(i);			
			prFinActivity.setActionDelete()	;			
		}


		//Loop through all Financial Activities and and create PaymentReporting Financial Activity 
		while (it.hasNext()) {
			issFinancialActivity = (FinancialActivity) it.next();
			if(issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_PREMIUMINIT ||
					issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CWA ||
					issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_PYMNTSHORTAGE ||
					issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_ROLLOVEREXT1035 ||
					issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_1035INIT||
					issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_1035SUBS ||
					issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CARRYOVERLOAN ){
				OLifEExtension oliExt = issFinancialActivity.getOLifEExtensionAt(0);
				boolean isDisbursed = false;
				if(oliExt !=null && oliExt.getFinancialActivityExtension()!= null){
					isDisbursed = oliExt.getFinancialActivityExtension().getDisbursedInd();
				}
				if(issFinancialActivity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_REV &&
						issFinancialActivity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_REFUND &&
						issFinancialActivity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND &&
						isDisbursed  != true ){
					index++;
					totalPayment += issFinancialActivity.getFinActivityGrossAmt();
					PaymentReportingFinActivity prFinAct = new PaymentReportingFinActivity();
					prFinAct.setId(NbaOliConstants.PAYMENTREPORTINGFINACTIVITYID_PREFIX + index);
					prFinAct.setFinActivityType(issFinancialActivity.getFinActivityType());
					prFinAct.setFinEffDate(issFinancialActivity.getFinEffDate());
					prFinAct.setFinActivityGrossAmt(issFinancialActivity.getFinActivityGrossAmt());

					if((totalPayment >= mip-shortageAmt) && !firstTimeMIPMet){
						firstTimeMIPMet = true;
						mipDate = issFinancialActivity.getFinEffDate();
						prFinAct.setMIPDate(issFinancialActivity.getFinEffDate());
					}else if((totalPayment >= mip-shortageAmt) && firstTimeMIPMet){
						prFinAct.setMIPDate(issFinancialActivity.getFinEffDate());
					}
					prFinAct.setActionAdd();
					paymentReportingFinActivityList.add(prFinAct);

				}

			}

		}
		if(totalPayment < mip) {
		    double diff = totalPayment - mip;
	        double shortage = (diff < 0) ? Math.abs(diff) : 0;
		    PaymentReportingFinActivity prFinAct = new PaymentReportingFinActivity();
		    index++;
            prFinAct.setId(NbaOliConstants.PAYMENTREPORTINGFINACTIVITYID_PREFIX + index);
            prFinAct.setFinActivityType(NbaOliConstants.OLI_FINACT_PYMNTSHORTAGE);
            prFinAct.setFinEffDate(mipDate);
            prFinAct.setFinActivityGrossAmt(shortage);
            prFinAct.setActionAdd();
            paymentReportingFinActivityList.add(prFinAct);
		}
		//Set IPED Date
		boolean hasCarryOverLoan = false;
		if(policyIssue.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents() != null && ((Life)policyIssue.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents()).getLifeUSA() != null){
			LifeUSA lifeUSA =((Life)policyIssue.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents()).getLifeUSA();
			LifeUSAExtension lifeUSAExt= NbaUtils.getFirstLifeUSAExtension(lifeUSA);
			hasCarryOverLoan = lifeUSAExt.getExch1035LoanCarryoverInd();


		}
		PolicyExtension polExtIssue = NbaUtils.getFirstPolicyExtension(policyIssue);
		Date ipedDate = null;
		if(mipDate != null){
			if(NbaUtils.compare(mipDate, policyIssue.getEffDate()) >= 0){				
				ipedDate = mipDate;
			}else{
				ipedDate = policyIssue.getEffDate();				
			}
		}else{
			ipedDate = policyIssue.getEffDate();
		}
		//if loan carryover is present IPED date = register date
		if(hasCarryOverLoan){
			ipedDate = policyIssue.getEffDate();
		}

		polExtIssue.setInitialPremEffDate(ipedDate);
		polExtIssue.setActionUpdate();

		//Set all MIP dates earlier than IPED date to IPED date
		if(paymentReportingFinActivityList != null && paymentReportingFinActivityList.size() > 0 && firstTimeMIPMet){
			Iterator prIT = paymentReportingFinActivityList.iterator();
			PaymentReportingFinActivity prFinActivity ;
			while (prIT.hasNext()) {
				prFinActivity = (PaymentReportingFinActivity)prIT.next();
				if(prFinActivity.getMIPDate() == null || (prFinActivity.getMIPDate() != null && NbaUtils.compare(prFinActivity.getMIPDate(),ipedDate) < 0)){
					prFinActivity.setMIPDate(ipedDate);
				}
				//if loan carryover is present all MIP dates = iped date =  register date
				if(hasCarryOverLoan){
					prFinActivity.setMIPDate(ipedDate);
				}
			}

		}


		appInfoExtn.setPaymentReportingFinActivity(paymentReportingFinActivityList);
		appInfoExtn.setActionUpdate();
	}
	//EC:  PED code APSL4585

	//SC:  PED code APSL4585 Sept Release
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
	}//EC:  PED code APSL4585 Sept Release
	
	//APSL5015 - DOL - to set BIC specific values
	public void addBICInformation() {
		OLifE olife = getNbaTxLife().getOLifE();
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
		Policy policy = holding.getPolicy();
		boolean isRetail = NbaUtils.isRetail(policy);
		boolean isQual = false;
		boolean isQualMny = false;
		isQual = NbaUtils.isQualifiedPlanForBICButDateNotapplicable(getNbaTxLife());
		isQualMny = NbaUtils.isQualifiedMoneyButDateNotapplicable(getNbaTxLife());
		ApplicationInfo appInfo = policy.getApplicationInfo();
		if (appInfo != null) {
			ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			if (!isRetail) {
				if (appInfoExtn != null) {
					appInfoExtn.deleteBICStatus();
					appInfoExtn.deleteBICType();
					appInfoExtn.deleteBICEffectiveDate();
				}
			} else  {
				try {
					if (NbaUtils.isBICDOLApplicable() && !NbaUtils.isBICDateApplicable(getNbaTxLife())) {
						if (appInfoExtn != null) {
							if (isQual) {
								appInfoExtn.setBICStatus(NbaOliConstants.AXA_BIC_STATUSNO);
								appInfoExtn.setBICType(NbaOliConstants.AXA_BIC_ERISA);
								appInfoExtn.setBICEffectiveDate(policy.getEffDate());
							} else if (isQualMny) {
								appInfoExtn.setBICStatus(NbaOliConstants.AXA_BIC_STATUSNO);
								appInfoExtn.setBICType(NbaOliConstants.AXA_BIC_IRA);
								appInfoExtn.setBICEffectiveDate(policy.getEffDate());
							} else if (!isQual || !isQualMny) {
								appInfoExtn.deleteBICStatus();
								appInfoExtn.deleteBICType();
								appInfoExtn.deleteBICEffectiveDate();
							}
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			appInfoExtn.setActionUpdate();
		}
	}
	
	// NBLXA-1457,NBLXA-1643 New Method
    public void setOverageAmount(NbaTXLife nbaTxLife) throws NbaBaseException {
		if (NbaConstants.SYST_CAPS.equals(getWorkLobs().getBackendSystem()) && !nbaTxLife.isPaidReIssue()) {
			NbaVpmsResultsData vpmsOverageAmtResultsData = getDataFromVPMS(NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS,
					NbaVpmsConstants.EP_CWA_OVERAGE_LIMIT);
			if (vpmsOverageAmtResultsData != null && vpmsOverageAmtResultsData.getResultsData() != null && vpmsOverageAmtResultsData.wasSuccessful()
					&& vpmsOverageAmtResultsData.getResultsData().size() == 1) {
				String overageAmtString = (String) vpmsOverageAmtResultsData.getResultsData().get(0);
				if (!NbaUtils.isBlankOrNull(overageAmtString)) {
					String[] overageAmtArray = overageAmtString.split("@@");
					double overageAmt = Double.parseDouble(overageAmtArray[0]);
					if (overageAmt > 0) {
						NbaCwaPaymentsExtract cwaPaymentsExtract = new NbaCwaPaymentsExtract();
						cwaPaymentsExtract.createOverageAccountingExtract(getNbaTxLife(), user, overageAmt);
						addComment("Creating Overage Write Off/Refund of amount:$" + overageAmt + " Received:$"
								+ getNbaTxLife().getPolicy().getApplicationInfo().getCWAAmt());
					}
				}

			}

		}

	}

	// NBLXA-1723 new method
	protected boolean isCWAOutstanding(String polNumber) throws NbaBaseException {
		NbaSearchResultVO searchResultVo = null;
		NbaSearchVO searchCWAVO = searchWI(NbaConstants.A_WT_CWA, polNumber);
		if (searchCWAVO != null && searchCWAVO.getSearchResults() != null && !searchCWAVO.getSearchResults().isEmpty()) {
			List searchResultList = searchCWAVO.getSearchResults();
			for (int i = 0; i < searchResultList.size(); i++) {
				searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
				if ((!searchResultVo.getQueue().equalsIgnoreCase(END_QUEUE))) {
					return true;
				}
			}
		}
		return false;
	}

	// NBLXA-1723 new method
	protected NbaSearchVO searchWI(String workType, String policyNumber) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName("NbaSearchResultVO");
		searchVO.setWorkType(workType);
		searchVO.setContractNumber(policyNumber);
		searchVO = lookupWork(getUser(), searchVO);
		return searchVO;
	}
	
}
