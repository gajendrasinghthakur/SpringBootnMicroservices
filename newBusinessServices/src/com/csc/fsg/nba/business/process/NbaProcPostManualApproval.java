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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.bean.accessors.NbaUnderwriterWorkbenchFacadeBean;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapter;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapterFactory;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.AxaMagnumUtils;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.ReinsuranceOffer;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
/**
 * NbaProcPostManualApproval is the class to process cases found in NBPSMNAP queue.
 * Once the case gets approved from Manual Approval, the NBAPPLCTN Work Item will be sent to NBPSMNAP queue with status as PSMNAPRLPR. 
 * This process will perform a full scale contract validation on the contract (all 6 subsets). 
 * If there are no severe/overridable errors on the contract, this automated process will generate the Contract Print extract work item,
 * if configured to do so, during approval.
 * It will also create the Correspondence Work Item and send the case for post approval requirements determination. 
 * If there are any severe/overridable errors found on the contract, the case will be moved for Case Manager review. 
 * Case Manager will review the case and perform necessary corrections to remove validation errors and then perform a Work Complete
 * which will move the case back to the NBPSMNAP queue with status as PSMNAPRLPR.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA239</td><td>AXA Life Phase 1</td><td>Improving nbA Manual Approval Performance</td></tr> 
 * <tr><td>SPRNBA-466</td><td>Version NB-1101</td><td>Design Flaw in Post Manual Approval and Prevent Print Errors</td></tr>
 * <tr><td>SR534655</td><td>Discretionary</td><td>nbA ReStart � Underwriter Approval</td></tr>
 * <tr><td>AXAL3.7.10A</td><td>AXA Life Phase 2</td><td>Automatic Reinsurance UI</td></tr>
 * <tr><td>CR59174</td><td>XA Life Phase 2</td><td>1035 Exchange Case Manager</td></tr>
 * <tr><td>APSL2297</td><td>Discretionary</td><td>CR UW Alerts for CV messages</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1001
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version NB-1001
 */


public class NbaProcPostManualApproval extends NbaAutomatedProcess {
    /**
     * NbaProcPostManualApproval constructor.
     */
    public NbaProcPostManualApproval() {
        super();
    }
	
/**
     * This method drive the Post Manual Approval Automated process.It is
     * - Get Holding Inquiry and Update Work Values.
     * - Run all the 6 subsets of Contract Validation
     * - Check whether there are any severe/overridable errors on the contract
     * - If there are any severe/overridable errors on the contract, it will send the case for case Manager review
     * - If there are no seever/overridable errors, the case will be moved for Post Approval Requirements Determination.
     *   It will also generate a Print Extract WorkItem if configured to do so
     * - Update and Unlock the WorkItem  
     * @param user the NbaUser for whom the process is being executed
     * @param work a NbaWorkItem value object for which the process is to occur
     * @return an NbaAutomatedProcessResult containing information about
     *         the success or failure of the process
     * @throws NbaBaseException
     */
    public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {    //FNB011

        if (!initialize(user, work)) {
            return getResult();
        }
       
        //APSL2297
        try {
			if (retrieveTransactionsRequired()) {  
				setWork(retrieveParentWithTransactions());
			}}catch (NbaBaseException e) {

				if (getLogger().isDebugEnabled()) {
					getLogger().logError(" Error occured :" + e.getMessage());
				}
				addComment("Error occured: - " + e.getMessage());
				// APSL3187 Begin
				if (e instanceof NbaAWDLockedException || e.getMessage().equalsIgnoreCase(NbaAWDLockedException.LOCKED_BY_USER)) {
					//APSl3087
					throw new NbaAWDLockedException("The requested Transaction is currently locked by another user");
				}
				// APSL3187 End
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Post Manual Approval Processing Failed", getFailStatus()));
			}
        //AXAL3.7.10A code deleted
		//New Code For SR534655 Retrofit
		//Begin APSL4417 Code deleted and moved to processFormalApp and processInformalApp methods
			if(nbaTxLife.isInformalApp()){
				processInformalApp();
			}else{
				processFormalApp();
		}
		//End APSL4417
		
		//Start NBLXA-2402(NBLXA-2569)
		boolean isMagnumCase = AxaMagnumUtils.isMagnumCase(nbaTxLife);
		if(isMagnumCase) {
			callMagnumWebServices(nbaTxLife);//NBLXA-2402(NBLXA-2569)
		}
		
        doUpdateWorkItem(); // also unlocks the cases
        return getResult();
    }
    
	//NBLXA-2402 New Method
    public void callMagnumWebServices(NbaTXLife nbaTxLife)throws NbaBaseException {
			Holding paramedHolding = AxaMagnumUtils.getParamedMagnumHolding(nbaTxLife);
			//NBLXA-2569
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.RS_MAGNUM_GET_CASE_SUMMARY,
					new NbaUserVO(), getNbaTxLife(), null, paramedHolding); 
			webServiceInvoker.execute();
			//NBLXA-2591
			webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.RS_MAGNUM_GET_CASE_DATA,
					new NbaUserVO(), getNbaTxLife(), null, paramedHolding);
			webServiceInvoker.execute();
    }
	//New Method QC5389
	public boolean verifyTxLifeResponse(NbaTXLife response) throws NbaBaseException {
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify txlifeResponseParent = response.getTXLife()
				.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		if (txlifeResponseParent.getTXLifeResponseCount() > 0 && txlifeResponseParent.getTXLifeResponseAt(0).hasTransResult()) {
			TXLifeResponse txLifeResponse = txlifeResponseParent.getTXLifeResponseAt(0);
			TransResult transResult = txLifeResponse.getTransResult();
			long resultCode = transResult.getResultCode();
			if ((NbaOliConstants.TC_RESCODE_FAILURE == resultCode)) {
				if (getLogger().isDebugEnabled()) {
					getLogger().logError(" Data error occured during web service invoke of CIF Submit");
				}
				//APSL3874 code deleted
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_WS, AxaWSConstants.WS_OP_CIF_TRANSMIT);// APSL3874				
			}
		} else {
			throw new NbaBaseException(this.getClass().getName() + " WebService returned invalid response.", NbaExceptionType.FATAL);
		}
		return true;
	}
    //CR59174 New Method
    protected boolean isRetail() {
    	return NbaUtils.isRetail(getNbaTxLife().getPolicy());
    	
    }
    //CR59174 New Method
    protected void assignExchangeCaseManager() throws NbaBaseException {
    	String excmQueue = AxaUtils.getMiscCMAssignmentRules(statusProvider.getExchangeCaseMgrQueue()); // NBLXA-2343
    	String XCMQ = getWork().getNbaLob().getExchCMQueue();//QC15211/APSL4492
    	if(NbaUtils.isBlankOrNull(XCMQ) || "-".equals(XCMQ) || "-1".equals(XCMQ)){//QC15211/APSL4492
    		if (null!= excmQueue && !"-".equals(excmQueue)) {
        		getWork().getNbaLob().setExchCMQueue(getEquitableQueue(excmQueue,NbaLob.A_LOB_EXCHANGE_CM));
        	}
    	}
    }
    
    //APSL2297 Begins
	protected boolean isSeverityExists(NbaTXLife nbaTxLife) throws NbaBaseException {
		boolean severity = false;
		SystemMessage sysMessage;
		SystemMessageExtension systemMessageExtension;
		ArrayList messages = nbaTxLife.getPrimaryHolding().getSystemMessage();
		for (int i = 0; i < messages.size(); i++) {
			sysMessage = (SystemMessage) messages.get(i);
			// Begin NBLXA-2155[NBLXA-2233]
			if (sysMessage.getMessageCode() == NbaConstants.MESSAGECODE_1937
					&& sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_SEVERE) {
				severity = true;
			} // End NBLXA-2155[NBLXA-2233]
			if (sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_SEVERE
					|| sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE) {//NBLXA-2280
				systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage);
				if (systemMessageExtension != null && systemMessageExtension.getMsgValidationType() == NbaConstants.SUBSET_AGENT
						&& getWork().getNbaLob().getInvalidAgent()) {
					continue;
				}
				severity = true;
				break;
			}
		}
		return severity;
	}

    protected boolean retrieveTransactionsRequired() {
	    
	    	boolean retrieveTrans = false;
	    	try {
	    		if (null == getWork() || 0 >= getWork().getTransactions().size() ) {
	    			retrieveTrans = true;
	    		}
	    	} catch (NbaBaseException nbe) {
	    		getLogger().logError("Error retrieving transactions. Will try again.");
	    		retrieveTrans = true;
	    	}
	    	return retrieveTrans;
	    }
		
	protected NbaDst retrieveParentWithTransactions() throws NbaBaseException {
		//create and set parent case retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		// if case
		if (getWork().isCase()) {
			retOpt.setWorkItem(getWork().getID(), true);
			retOpt.requestTransactionAsChild();
			retOpt.requestSources(); //APSL3211/SR657984
			retOpt.setLockWorkItem();
			retOpt.setLockTransaction();
		} else { // if a transaction
			retOpt.setWorkItem(getWork().getID(), false);
			retOpt.requestCaseAsParent();
			retOpt.requestTransactionAsSibling();
			retOpt.setLockWorkItem();
			retOpt.setLockParentCase();
			retOpt.requestSources(); //APSL3211/SR657984
		}
		//get case from awd
		NbaDst parentCase = retrieveWorkItem(getUser(), retOpt);
		return parentCase;
	}
	
	
	 protected void createValErrorWorkItem() throws NbaBaseException {
		
			Map deOink = new HashMap();
			deOink.put("A_ErrorSeverity", Long.toString(NbaOliConstants.OLI_MSGSEVERITY_SEVERE));
			deOink.put("A_CreateValidationWI", "true");
			NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), nbaTxLife, deOink);
			NbaTransaction validationTransaction = getValidationTransaction(workProvider.getWorkType());
			if(validationTransaction==null){
			getWork().addTransaction(workProvider.getWorkType(), workProvider.getInitialStatus());
			}else{
				changeStatus(validationTransaction,workProvider.getInitialStatus());
				validationTransaction.getNbaLob().setFaceAmount(nbaTxLife.getFaceAmount());//APSL3952
				validationTransaction.setUpdate();
			}
		}

		protected NbaTransaction getValidationTransaction(String workType) throws NbaBaseException {
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
		// APSL2297 Ends
		
	//Begin APSL4417  New Methods
	protected void processInformalApp() throws NbaBaseException {
		try {
			NbaUnderwriterWorkbenchFacadeBean bean = new NbaUnderwriterWorkbenchFacadeBean();
			bean.setNbaDstWithAllTransactions(getWork());
			bean.applyApproveAndDisposition(user, nbaTxLife, getWork(), user.getUserID(), "-1", nbaTxLife.isInformalApp());// UW alert
		} catch (NbaBaseException e) {

			if (getLogger().isDebugEnabled()) {
				getLogger().logError(" Error occured :" + e.getMessage());
			}
			addComment("Error occured: - " + e.getMessage()); // Code Change for QC5389
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Post Manual Approval Processing Failed", getFailStatus()));
		}

		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}

		changeStatus(getResult().getStatus());

		nbaTxLife.setBusinessProcess(NbaConstants.PROC_POSTMANUALAPPROVAL);

		setNbaTxLife(doContractUpdate());
		handleHostResponse(getNbaTxLife());
	}
		
		
	protected void processFormalApp() throws NbaBaseException {
		boolean isCIFSuccessful = true;
		boolean isTAISuccessful = true;

		if (!nbaTxLife.isPaidReIssue()) { // APSL2662
			AxaWSInvoker cifwebServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CIF_TRANSMIT, user, nbaTxLife,
					work, null);
			NbaTXLife nbaTXLifeResponse = (NbaTXLife) cifwebServiceInvoker.execute();
			isCIFSuccessful = verifyTxLifeResponse(nbaTXLifeResponse);
		}

		// APSL3874 code deleted
		AxaWSInvoker taiwebServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_TAI_SERVICE_TRANSMIT, user, nbaTxLife,
				null, new Long(NbaOliConstants.TC_SUBTYPE_GET_TAI_HOLDING_TRANSMIT));
		NbaTXLife taiNbaTXLifeResponse = (NbaTXLife) taiwebServiceInvoker.execute();
		// APSL3874 code deleted

		try {
			if (isCIFSuccessful && isTAISuccessful) {
				// APSL2297 code moved to retrieveParentWithTransactions() method
				NbaUnderwriterWorkbenchFacadeBean bean = new NbaUnderwriterWorkbenchFacadeBean();
				bean.setNbaDstWithAllTransactions(getWork());
				bean.applyApproveAndDisposition(user, nbaTxLife, getWork(), user.getUserID(), "-1", nbaTxLife.isInformalApp());// UW alert
			}

		} catch (NbaBaseException e) {

			if (getLogger().isDebugEnabled()) {
				getLogger().logError(" Error occured :" + e.getMessage());
			}
			addComment("Error occured: - " + e.getMessage()); // Code Change for QC5389
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Post Manual Approval Processing Failed", getFailStatus()));
		}

		// APSL2297
		try {
			if (!nbaTxLife.isSIApplication() && isSeverityExists(nbaTxLife)) { // APSL2808,NBLXA-2155[NBLXA-2233]
				createValErrorWorkItem();
			}
		} catch (NbaBaseException e) {

			if (getLogger().isDebugEnabled()) {
				getLogger().logError(" Error occured :" + e.getMessage());
			}
			addComment("Error occured: - " + e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Post Manual Approval Processing Failed", getFailStatus()));
		}

		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}

		changeStatus(getResult().getStatus());
		// Setting the actual business process which will not run any of the contract validation subsets
		nbaTxLife.setBusinessProcess(NbaConstants.PROC_POSTMANUALAPPROVAL);
		// begun SR566149 and SR519592

		// Setting PartialReqInd to true for generating only Partial Requirements
		// If this Indicator is true no Post approval Requirement will generate, only if there is any amendment than
		// it will generate only Amendment requirement.

		ApplicationInfo applicationInfo = nbaTxLife.getPolicy().getApplicationInfo();
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
		if (applicationInfoExtension != null) {
			applicationInfoExtension.setPartialReqInd(true);
			// APSL3525 Reissue SR October Changesstart
			Policy policy = nbaTxLife.getPolicy();
			PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
			if (policyExt != null && policyExt.hasOldPremiumAmt() && policyExt.getOldPremiumAmt() > 0) {
				if (getWork() != null && NbaConstants.SYST_CAPS.equals(getWork().getNbaLob().getBackendSystem()) && policy.hasPaymentAmt()) {
					if (policyExt.getOldPremiumAmt() != policy.getPaymentAmt()) {
						applicationInfoExtension.setConfirmingIllustrationInd(true);
					}
				} else if (getWork() != null && NbaConstants.SYST_LIFE70.equals(getWork().getNbaLob().getBackendSystem())
						&& policy.hasMinPremiumInitialAmt()) {
					if (policyExt.getOldPremiumAmt() != policy.getMinPremiumInitialAmt()) {
						applicationInfoExtension.setConfirmingIllustrationInd(true);
					}
				}
			}
			//Start APSL5100
			boolean previewNeededInd = isPreviewNeeded();
			policyExt.setPrintPreviewNeededInd(previewNeededInd);
			policyExt.setActionUpdate();
			//End APSL5100
			// APSL3525 Reissue SR October Changes end
			applicationInfoExtension.setActionUpdate();
		}

		// end SR566149 and SR519592
		
		generateCorrospondance(); // NBLXA-2114
		
		setNbaTxLife(doContractUpdate());
		handleHostResponse(getNbaTxLife());
		// doUpdateWorkItem(); // also unlocks the cases //APSL1874

		// CR59174, APSL3670(removed the check for retail case)
		assignExchangeCaseManager(); // CR59174
		// CR59174
		// setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Post Manual Approval Processing Successful",
		// getPassStatus()));//APSL1874
		// changeStatus(getResult().getStatus());//APSL1874
		// AXAL3.7.10A Code Deleted
    }
	//End APSL4417 
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
}
