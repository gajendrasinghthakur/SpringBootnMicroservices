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

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
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

import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.datamanipulation.AxaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaCashieringTable;
import com.csc.fsg.nba.tableaccess.NbaCheckData;
import com.csc.fsg.nba.tableaccess.NbaCompanionCaseControlData;
import com.csc.fsg.nba.tableaccess.NbaContractCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractsWireTransferData;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProducerVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsMoneyUnderwritingData;
import com.csc.fsg.nba.vpms.results.ResultData;

/**
 * NbaProcMoneyUnderwriting is the class that processes nbAccelerator transactions
 * found on the AWD money underwriting queue (NBMNYUND). If the associated case
 * is not already approved, it invokes the VP/MS Automated Money Underwriting
 * model to determine if the case is within an acceptable risk range to allow
 * the CWA to be applied.
 * <p>The NbaProcMoneyUnderwriting class extends the NbaAutomatedProcess class.  
 * Although this class may be instantiated by any module, the NBA polling class 
 * will be the primary creator of objects of this type.
 * <p>When the polling process finds a case on the Money Underwriting queue, 
 * it will create an object of this instance and call the object's 
 * executeProcess(NbaUserVO, NbaDst) method.  This method will manage the steps 
 * necessary to determine if the case is either already approved or within an
 * acceptable risk range, using a VP/MS model, to allow the CWA to be applied.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA009</td><td>Version 2</td><td>Cashiering</td></tr>
 * <tr><td>SPR1171</td><td>Version 2</td><td>Secondary Contracts for Wire Transfers</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA035</td><td>Version 3</td><td>App submit to nbA Pending DB</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA106</td><td>Version 4</td><td>Changes for enhancement added need for NbaVpmsMoneyUnderwritingData</td></tr>
 * <tr><td>NBA095</td><td>Version 4</td><td>Queues Accept Any Work Type</td></tr>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit Card payment and authorization</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>s
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr> 
 * <tr><td>AXAL3.7.15</td><td>AXA Life Phase 1</td><td>Issue</td></tr> 
 * <tr><td>AXAL3.7.43</td><td>AXA Life Phase 1</td><td>Money Underwriting</td></tr>
 * <tr><td>AXAL3.7.20</td><td>AXA Life Phase 1</td><td>Workflow</td></tr>
 * <tr><td>ALS3046</td><td>AXA Life Phase 1</td><td>non-nbA NBPAYMENT work item indexed from the wholesale index queue flowed to the N2NBCMR queue instead of N2NBCMW queue</td></tr>
 * <tr><td>SR545390</td><td>AXA Life Phase 1</td><td>Back Due Premium at Issue</td></tr>
 * <tr><td>CR1346006</td><td>AXA Life Phase 2</td><td>Change for Money Underwriting during Temp Express</td></tr>
 * <tr><td>CR1453744</td><td>AXA Life Phase 2.1</td><td>Changes related to Money underwriting</td></tr>
 * <tr><td>SPRNBA-932/APSL4400</td><td>Version NB-1501</td><td>Correct Logic Errors for Work Item Lock and Unlock</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 2
 */
public class NbaProcMoneyUnderwriting extends NbaAutomatedProcess {
    private NbaDst parentWork = null; //NBA192
    private boolean contractUpdated = false; //NBA192
    private List unsuspendList = null; //NBA192
        
/**
 * NbaProcMoneyUnderwriting constructor comment.
 */
public NbaProcMoneyUnderwriting() {
	super();
}
/**
 * This abstract method must be implemented by each subclass in order to
 * execute the automated process.
 * @param user the user/process for whom the process is being executed
 * @param work a DST value object for which the process is to occur
 * @return an NbaAutomatedProcessResult containing information about
 *         the success or failure of the process
 */
public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
	// NBA027 - logging code deleted
	// NBA095 code deleted
	if (!initialize(user, work)) {
		return getResult(); //NBA050
	}
	//LIM-ADC APSL3460 :: START
    if (getWork().getNbaLob() != null && getWork().getNbaLob().getWorkFlowCaseId() != null
            && getWork().getNbaLob().getWorkFlowCaseId().length() > 0) {
        setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
        changeStatus(getPassStatus());
        doUpdateWorkItem();
        return getResult();
    }
	//LIM-ADC APSL3460 :: END
	
	//CR1346006 Begin	
	if (getLogger().isDebugEnabled()) { // NBA027
		getLogger().logDebug("MoneyUnderwriting contract " + getWork().getNbaLob().getPolicyNumber());
	} // NBA027
	
	boolean tempExpCommInd = false;
	boolean fullAppInd = false;
	boolean PaidChngind = false; //CR1453744
	long applicationtype=nbaTxLife.getPolicy().getApplicationInfo().getApplicationType(); //CR1346006, APSL2526
	if(nbaTxLife != null && nbaTxLife.getPolicy() != null){
		
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(nbaTxLife.getPolicy().getApplicationInfo());
		if(applicationInfoExtension != null){
			fullAppInd = applicationInfoExtension.getFullApplicationCompleteInd();
			PaidChngind = applicationInfoExtension.getPaidChngCompletedInd(); //CR1453744
			
		}
	}		
	if(NbaUtils.isTempExpComission(nbaTxLife) && !fullAppInd ){
		GregorianCalendar cal = new GregorianCalendar();
		int suspendDays = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
				NbaConfigurationConstants.TEMPEXPRESS_PAYMENT_SUSPEND_DAYS));
		cal.setTime(new Date());
		cal.add(Calendar.DATE, suspendDays);
		suspendWorkItem("Temporary Express Commission case -- Full Application data entry not completed",cal.getTime());
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));	//APSL4226	
		return getResult();
	}	
//	CR1346006 End
	
//APSL4730 deleted changes for CR1453744, APSL2526
	
	try {	//NBA095

		//retrieve the complete work item
				NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
				retOpt.setWorkItem(getWork().getID(), getWork().isCase());	//NBA146
				retOpt.requestSources();
				retOpt.setLockWorkItem();
				setWork(retrieveWorkItem(getUser(), retOpt));  //NBA213
		//NBA050 CODE DELETED		
		//check underwriter approval
		NbaVpmsMoneyUnderwritingData data = null;   //ALS5029
		if (hasUnderwriterApproval(nbaTxLife)) { //NBA050
		// Begin ALS5029
			data = underwriteMoneyApprovedPolicy(nbaTxLife);
		} else {
			//perform money underwriting
			//NBA106 Update data class
			data = underwriteMoney(nbaTxLife); //NBA050
		}
		if (data.wasSuccessful()) {	
		    processMoneyRequirements(); //NBA192
			routePassed();
		} else {
			routeFailed(data);
		}
		//End ALS5029
		commit(); //NBA192
		return getResult();
	//SPR3362 code deleted
		} finally { // NBA192
			if (this.parentWork != null && this.parentWork.isLocked(user.getUserID())) { // APSL3874
				// SPRNBA-932/APSL4400 Begin
				WorkItem childItem = null;
				Iterator iter = getParentWork().getTransactions().iterator();
				while (iter.hasNext()) {
					childItem = (WorkItem) iter.next();					
					if (childItem.getItemID().equals(getWork().getID())) {
						iter.remove();
						break;
					}
				}
				// SPRNBA-932/APSL4400 End
				unlockWorkItems(); // NBA192
			}
		} // NBA192
}
/**
 * Retrieve the check information from the cashiering database.
 * @return com.csc.fsg.nba.tableaccess.NbaCheckData
 */
protected NbaCheckData getCheckData() {

	try {
		NbaLob lob = getWork().getNbaLob();
		NbaCashieringTable table = new NbaCashieringTable();
		NbaContractCheckData contractCheck =
			table.getContractCheckData(
				lob.getBundleNumber(),
				lob.getCompany(),
				lob.getPolicyNumber(),
				lob.getCheckAmount(),
				lob.getCheckNumber(),
				lob.getCwaAmount());
		return (table.getCheckData(contractCheck.getTechnicalKey()));
	} catch (NbaBaseException nbe) {
		getLogger().logError("Money Underwriting get check data failed: " + nbe.getMessage());
		return null;
	} catch (Exception e) {
		getLogger().logError("Money Underwriting get check data failed: " + e.getMessage());
		return null;
	}
}
/**
 * Return true if this is a credit card payment
 * @return boolean
 */
//New Method NBA115
protected boolean isCreditCardPayment() {
	String ccTransactionId = getWork().getNbaLob().getCCTransactionId();
	if(ccTransactionId != null && ccTransactionId.length() > 0 ){
		return true;
	}
	return false;
}

/**
 * Returns true if the contract has underwriter approval and false if not.
 * 
 * @return boolean
 * @param nbaTXLife Holding Inquiry 
 */
protected boolean hasUnderwriterApproval(NbaTXLife nbaTXLife) {

	try {
		ApplicationInfo appInfo = nbaTXLife.getPrimaryHolding().getPolicy().getApplicationInfo();
		int extCount = appInfo.getOLifEExtensionCount();
		for (int i = 0; i < extCount; i++){
			ApplicationInfoExtension appInfoExt = appInfo.getOLifEExtensionAt(0).getApplicationInfoExtension();
			if (appInfoExt != null) {
				if (appInfoExt.getUnderwritingApproval() == NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	} catch (Exception e) {
		getLogger().logError("Money Underwriting hasUnderwriterApproval: " + e.getMessage());
		return false;
	}
}
/**
 * When auto money underwriting fails, this method adds comments indicating why
 * the process failed and creates a new <code>NbaAutomatedProcessResult</code> to return
 * to the polling program.
 * A null value may be passed to indicate failure prior to the executing
 * the VPMS model.
 * @param data   null or the results of the VPMS processing
 */
//NBA106 Changed from NbaVpmsAutoUnderwritingData
protected void routeFailed(NbaVpmsMoneyUnderwritingData data) throws NbaBaseException {	

	if (data == null) {
				addComment("Automated Money Underwriting failed: for an unknown reason");
		} else {
				if (getLogger().isDebugEnabled())
						data.displayErrors();
				if (data.getAuErrors() == null) {
						addComment("Automated Money Underwriting failed due to: " + data.getResult().getMessage() + "-" + data.getResult().getRefField());
				} else {
						addComments(data.getAuErrors());
				}
		}
	
	// APSL3874 Code Deleted
		if (isCreditCardPayment()) {
			NbaCreditCardData.updateCCFailedMoneyUnderwriting(getWork().getNbaLob().getCCTransactionId());
		}else {		
			// is this a wire transfer?
			if (work.getNbaLob().getPortalCreated() && !(String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT).equalsIgnoreCase(work.getNbaLob().getPaymentMoneySource()))) { //APSL2735
				setWireTransferIndicators(false);
			} else {
				setIncludeIndicator(false);
				setRejectedIndicator();
				Map deOinkMap =getDeOinkMapForSources(getWork());
				if (data.paperCheckOnCIPE) {
					deOinkMap.put("A_paperCheckOnCIPE", "true");
				}
				setStatusProvider( new NbaProcessStatusProvider(getUser(), getWork(), getNbaTxLife(), deOinkMap));//AXAL3.7.20
			}
		}//end NBA115	
		// APSL3874 Code Deleted

	
	if (getLogger().isDebugEnabled()) { // NBA027
		getLogger().logDebug("Failed Money Underwriting: change status to " + getFailStatus());
	} // NBA027

	//begin AXAL3.7.43
	//Start NBLXA-1250
		if (data.paperCheckOnCIPE && getAlternateStatus() != null && getAlternateStatus().trim().length() > 0) {
			changeStatus(getAlternateStatus());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Failed", getAlternateStatus()));
		} // End NBLXA-1250
		else if (data.isNameMismatched) {
			changeStatus(getAlternateStatus());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Failed", getAlternateStatus()));
		} else {
			// end AXAL3.7.43
			changeStatus(getFailStatus());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Failed", getFailStatus()));
		}// AXAL3.7.43
}
/**
 * Underwritng has been approved.  If this is a wire transfer, set the pass status
 * and return a new <code>NbaAutomatedProcessResult</code>.  
 */ 

	protected void routePassed() throws NbaBaseException { // APSL3874 Declared throws exception

		// APSL3874 Code Deleted
		// begin NBA192
		if (getResult() == null) {
			// is this a wire transfer
			if (getWork().getNbaLob().getPortalCreated()) {
				if (getLogger().isDebugEnabled()) { // NBA027
					getLogger().logDebug("Passed Money Underwriting: change status to " + getPassStatus());
				} // NBA027
				setWireTransferIndicators(true);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
			} else {
				// Begin NBA115
				if (isCreditCardPayment()) {
					addComment("Passed Money Underwriting");
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
				} else {
					 String status = setIncludeIndicator(true);
	                 setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", status));
				}
				// End NBA115
			}
		}
		changeStatus(getResult().getStatus());
		// end NBA192
		// APSL3874 Code Deleted
	}
/**
 * Set the check's include indicator, to make the check available for deposit.
 * @return status
 */
protected String setIncludeIndicator(boolean includeInd) throws NbaBaseException {

		NbaCheckData check = getCheckData();
		if (check == null) {
			// APSL3874 Code Deleted
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH); // APSL3874
		}
		if (includeInd) {
			String dbYear=null;
			if (check.getDepositTimeStamp() != null) {
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			    dbYear = df.format(check.getDepositTimeStamp());
			}
			if (check.getDepositTimeStamp() == null || (dbYear!=null && dbYear.endsWith("1900"))) { //APSL5289	
				NbaCashieringTable cashTable = new NbaCashieringTable();
				NbaContractCheckData[] contracts = cashTable.getContractCheckData(check.getTechnicalKey());
				for (int i = 0; i < contracts.length; i++) {
					if (contracts[i].isRejected()) {
						return (getPassStatus());
					}
				}
			} else {
				addComment("Money Underwriting failed: Check has already been deposited, the include indicator cannot be changed");
				return (getOtherStatus());
			}
		}

	check.setIncludeInd(includeInd);
	check.update();
	return(getPassStatus());
}
/**
 * Set the rejected indicator for this contract.
 */
protected void setRejectedIndicator() throws NbaBaseException {

	NbaLob lob = getWork().getNbaLob();
	NbaContractCheckData.updateRejectedInd(
		true,
		lob.getBundleNumber(),
		lob.getCompany(),
		lob.getPolicyNumber(),
		lob.getCheckAmount(),
		lob.getCheckNumber(),
		lob.getCwaAmount());
}
/**
 * Updates the appropriate indicators based on the underwriting status.
 * @param passedUnderwriting boolean
 */
protected void setWireTransferIndicators(boolean passedUnderwriting) throws NbaBaseException {

	NbaLob lob = getWork().getNbaLob();
	List sources = getWork().getSources();
	for (int i=0; i<sources.size(); i++) {
		//NBA208-32
		NbaSource nbaSource = new NbaSource((WorkItemSource)sources.get(i));
		if (nbaSource.getSource().getSourceType().equals(NbaConstants.A_ST_XML508)) {
			try {
				NbaTXLife txLife = new NbaTXLife(nbaSource.getSource().getText());
				Date effDate = txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransExeDate();

				NbaCashieringTable table = new NbaCashieringTable();
				NbaContractsWireTransferData contractWire = table.getWireTransferContractData(effDate, lob.getCompany(), lob.getPolicyNumber(), (long) lob.getSequenceNumber());  //SPR1171, NBA130
				contractWire.setReportInd(true);
				if (!passedUnderwriting) {
					contractWire.setReturnedInd(true);
				}
				contractWire.update();
			} catch (NbaBaseException nbe) {
				throw nbe;
			} catch (Exception e) {
				throw new NbaBaseException(e);
			}
		}
	}
}
/**
 * This method is invoked when automated money underwriting is needed.
 * It instantiates an NbaVpmsAdaptor object using the input parameter and 
 * the VP/MS model name.  It then executes the NbaVpmsAdaptor method 
 * getResults() and, using the results from the method call, instantiates 
 * and returns an NbaVpmsAutoUnderwritingData object.
 * @return NbaVpmsAutoUnderwritingData object that contains results of the call
 * to the VPMS model.
*/
//NBA106 Updated from NbaVpmsAutoUnderwritingDate
//SPR3362 changed method signature
protected NbaVpmsMoneyUnderwritingData underwriteMoney(NbaTXLife xml203) throws NbaBaseException {

	//Code deleted AXAL3.7.43 
	if(parentWork == null){//ALS5026
		parentWork = retrieveParentWork(false); //AXAL3.7.43//ALS5026
	}//ALS5026
	AxaOinkDataAccess oinkData = new AxaOinkDataAccess();//AXAL3.7.43
	oinkData.setContractSource(nbaTxLife, parentWork.getNbaLob());//NBA035 set with 203, not 103/203 merge // NBA050 updated//ALS5026
	oinkData.setSecondaryLobSource(getWork().getNbaLob());//AXAL3.7.43

	NbaOinkRequest oinkRequest = new NbaOinkRequest(); // set up the NbaOinkRequest object
	//begin SPR3362
	NbaVpmsAdaptor vpmsProxy = null;
    try {
        vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.AUTOMONEYUNDERWRITING);
        //end SPR3362
		vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_AUTO_UNDERWRITING_RESULTS);
		vpmsProxy.setANbaOinkRequest(oinkRequest);
		//Begin AXAL3.7.43
		Map deOinkMap = new HashMap();
		if (parentWork != null) {//ALS5026
			deOinkMap.put("A_PrintWIStatus", getPrintWIStatus(parentWork));//ALS5026
			List statusList = getRequirementStatus(parentWork);//ALS5026
			deOinkMap.put("A_RequirementStatusList", statusList.toArray(new String[statusList.size()]));
			deOinkMap.put("A_RequirementCount", String.valueOf(statusList.size()));
			deOinkMap.put("A_TotalFaceAmount", String.valueOf(getTotalFaceAmount(parentWork)));//ALS5026
		}
		NbaParty primeryInsured = nbaTxLife.getPrimaryParty();// Insured
		deOinkMap.put("A_LastName_PINS", primeryInsured.getLastName());//QC 11172(APSL2735)
		//Owners
		Set ownerSet = getAllPartyLastName(nbaTxLife.getAllPartiesForRole(nbaTxLife.getPrimaryHolding().getId(),
				NbaOliConstants.OLI_REL_OWNER), false); 
		deOinkMap.put("A_OwnerCount", String.valueOf(ownerSet.size()));
		deOinkMap.put("A_AllOwnerLastName", ownerSet.toArray(new String[ownerSet.size()]));
		//Agents
		Set agentSet = getAllPartyLastName(nbaTxLife.getProducers(), true);
		deOinkMap.put("A_AgentCount", String.valueOf(agentSet.size()));
		deOinkMap.put("A_AllAgentLastName", agentSet.toArray(new String[agentSet.size()]));
		//Beneficiary
		List beneficiaryList = new ArrayList();
		for (int i = 0; i < NbaConstants.BENEFICIARY_ROLE_CODES.length; i++) {
		beneficiaryList.addAll(nbaTxLife.getAllPartiesForRole(primeryInsured.getID(),  Long.valueOf(NbaConstants.BENEFICIARY_ROLE_CODES[i]).longValue())); //Beneficiary
		}    
		Set beneficiarySet = getAllPartyLastName(beneficiaryList, false);
		deOinkMap.put("A_BeneficiaryCount", String.valueOf(beneficiarySet.size()));
		deOinkMap.put("A_AllBeneficiaryLastName", beneficiarySet.toArray(new String[beneficiarySet.size()]));
		//Trustee
		NbaParty trustee = nbaTxLife.getParty(nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_TRUSTEE)); // Trustee
		deOinkMap.put("A_TrusteeLastName", getPartyLastName(trustee));
		//Corporate Owner And Trust
		List OwnerList = nbaTxLife.getAllPartiesForRole(nbaTxLife.getPrimaryHolding().getId(), NbaOliConstants.OLI_REL_OWNER);
		Set corporateOwnerSet = getAllEntityName(OwnerList);
		deOinkMap.put("A_CorOwnerCount", String.valueOf(corporateOwnerSet.size()));
		deOinkMap.put("A_AllCorOwnerName", corporateOwnerSet.toArray(new String[corporateOwnerSet.size()]));
		//Employer
		NbaParty employer = nbaTxLife.getEmployer(primeryInsured.getID());
		String employerName = getEntityName(employer);
		if (employerName != null && !"".equalsIgnoreCase(employerName)) {
			deOinkMap.put("A_EmployerName", employerName);
		}
		deOinkTermConvData(deOinkMap);	//CR1453744	
		deOinkMap.put("A_ROPRPresent", NbaUtils.isROPR(nbaTxLife)? "Yes":"No");//P2AXAL013
		//Begin APSL3521
		String date = "";
		int noOfNbaSources = getWork().getNbaSources().size();
		for (int i = 0; i < noOfNbaSources; i++) {
			
			if (NbaConstants.A_ST_CWA_CHECK.equals(((NbaSource) getWork().getNbaSources().get(i)).getSourceType()) ||
					NbaConstants.A_ST_PAYMENT.equals(((NbaSource) getWork().getNbaSources().get(i)).getSourceType())) {//APSL3604
				
				date = NbaUtils.getStringFromDateInAWDFormat((((NbaSource) getWork().getNbaSources().get(i)).getCreateDate()));
				deOinkMap.put("A_CheckScanDate", date);
				deOinkMap.put("A_SourceTypeLOB2", ((NbaSource) getWork().getNbaSources().get(i)).getSourceType());//APSL3836
				break;
			}
			
		}
		//End APSL3521
		vpmsProxy.setSkipAttributesMap(deOinkMap);
		//End AXAL3.7.43

	//	NBA106 Changed from NbaVpmsAutoUnderwritingData
	NbaVpmsMoneyUnderwritingData data = new NbaVpmsMoneyUnderwritingData(vpmsProxy.getResults());
    //begin SPR3362
        return data;
    } catch (java.rmi.RemoteException re) {
        throw new NbaBaseException("Problem in getting data from VPMS", re);
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

//AXAL3.7.43 New Mehtod
private Set getAllPartyLastName(List partyList, boolean isAgent) {
	Set partySet = new HashSet();
	if (partyList != null) {
		for (int i = 0; i < partyList.size(); i++) {
			String partyLastName = "";
			if (isAgent) {
				partyLastName = getAgentLastName((NbaProducerVO) partyList.get(i));
			} else {
				partyLastName = getPartyLastName((NbaParty) partyList.get(i));
			}
			if (!NbaUtils.isBlankOrNull(partyLastName)) {
				partySet.add(partyLastName);
			}
		}
	}
	return partySet;
}

//AXAL3.7.43 New Mehtod
private String getAgentLastName(NbaProducerVO agentVo) {
	if (agentVo != null) {
		return getPartyLastName(agentVo.getNbaParty());
	}
	return null;
}

//AXAL3.7.43 New Mehtod
private String getPartyLastName(NbaParty party) {
	String lastName =""; 
	if (party != null && party.isPerson() && party.getLastName()!=null) {
		lastName= party.getLastName();
	}
	return lastName;
}

//AXAL3.7.43 New Mehtod
private String getEntityName(NbaParty entity) {
	String lastName ="";
	if (entity != null && entity.isOrganization() && entity.getFullName()!=null) {
		lastName= entity.getFullName();
	}
	return lastName;
}

//AXAL3.7.43 New Mehtod
private Set getAllEntityName(List entityList) {
	Set entitySet = new HashSet();
	if (entityList != null) {
		String entityName ;
		for (int i = 0; i < entityList.size(); i++) {
			entityName = getEntityName((NbaParty) entityList.get(i));
			if (!NbaUtils.isBlankOrNull(entityName)) {
				entitySet.add(entityName);
			}
		}
	}
	return entitySet;
}

//AXAL3.7.43 New Mehtod
private double getTotalFaceAmount(NbaDst parentCase) throws NbaBaseException {
	double totalFaceAmount = 0.0;
	double maxFaceAmount = 0.0;//ALS5502
	try {
		//Retrieves a snapshot of what is in the control source
		List companionCases = new NbaCompanionCaseControlData().getNbaCompanionCaseVOs(parentCase.getID());
		NbaCompanionCaseVO companionCase = null;
		NbaDst dst = null;
		//Begin ALS5502 
		boolean useMaxFaceAmount = false;
		if(companionCases.size()== 0){
			totalFaceAmount= parentCase.getNbaLob().getFaceAmount();
		}
		//End ALS5502
		for (int i = 0; i < companionCases.size(); i++) {
			companionCase = (NbaCompanionCaseVO) companionCases.get(i); //a snapshot of what is in the control source
				if (companionCase.getWorkItemID().equals(parentCase.getID())) {
					dst = parentCase;
				} else {
					NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();
					options.requestSources();
					options.requestTransactionAsChild();
					options.setWorkItem(companionCase.getWorkItemID(), true); // true indicates Case
					dst = WorkflowServiceHelper.retrieveWorkItem(user, options);
					if(!(COMPANION_TYPE_ALTERNATE.equals(dst.getNbaLob().getCompanionType()) || 
							COMPANION_TYPE_ADDITIONAL.equals(dst.getNbaLob().getCompanionType())) ){//ALS5502 Not an Alternate or Additional companion case
						return parentCase.getNbaLob().getFaceAmount();//ALS5502
					}
					//Begin ALS5502
					if(COMPANION_TYPE_ALTERNATE.equals(dst.getNbaLob().getCompanionType())){
						useMaxFaceAmount = true;
					}
				}
				if(dst.getNbaLob().getFaceAmount() > maxFaceAmount){
					maxFaceAmount = dst.getNbaLob().getFaceAmount();
				}
				//End ALS5502
				totalFaceAmount += dst.getNbaLob().getFaceAmount();
		}
		if(useMaxFaceAmount){//ALS5502
			totalFaceAmount = maxFaceAmount;//ALS5502
		}//ALS5502
	} catch (NbaBaseException be) {
		getLogger().logException(be);
		throw be;
	}
	return totalFaceAmount;
}

//AXAL3.7.43 New Mehtod
private String getPrintWIStatus(NbaDst parentCase) throws NbaBaseException {
	String status = "";
	try {
		ListIterator li = parentCase.getNbaTransactions().listIterator();
		while (li.hasNext()) {
			NbaTransaction trans = (NbaTransaction) li.next();
			if (A_WT_CONT_PRINT_EXTRACT.equalsIgnoreCase(trans.getTransaction().getWorkType())) {
				status = trans.getTransaction().getStatus();
			}
		}
	} catch (NbaNetServerDataNotFoundException e) {
		getLogger().logException(e);
		throw new NbaBaseException("Problem while retriving Status of Print Extract Work Item", e);
	}
	return status;
}

//AXAL3.7.43 New Mehtod
private List getRequirementStatus(NbaDst parentCase) throws NbaBaseException {
	List status = new ArrayList();
	try {
		ListIterator li = parentCase.getNbaTransactions().listIterator();

		while (li.hasNext()) {
			NbaTransaction trans = (NbaTransaction) li.next();
			if (A_WT_REQUIREMENT.equalsIgnoreCase(trans.getTransaction().getWorkType())) {
				//long reqStatus = NbaUtils.convertStringToLong(trans.getNbaLob().getReqStatus());
				//if( reqStatus!= NbaOliConstants.OLI_REQCODE_PREMDUE){
				if (NbaOliConstants.OLI_REQCODE_PREMDUE != trans.getNbaLob().getReqType()) {
					status.add(trans.getNbaLob().getReqStatus());
				}
			}
		}
	} catch (NbaNetServerDataNotFoundException e) {
		getLogger().logException(e);
		throw new NbaBaseException("Problem at Requirement Status conditions", e);
	}
	return status;
}

/**
 * Processes all money requirements. 
 * @throws NbaBaseException
 */
//NBA192 New Method
protected void processMoneyRequirements() throws NbaBaseException {
    receiveMoneyRequirements();
    if(hasContractUpdated()){
        handleHostResponse(doContractUpdate());
    }
}

/**
 * Calls VP/MS to check if any requirement can be received for money. It then retrieve all work items
 * from work flow system to match requirements. If matched requirement is not earlier received or waived 
 * and resides in one of the valid queue then it will be receipted.
 * @throws NbaBaseException
 */
//NBA192 New Method
protected void receiveMoneyRequirements() throws NbaBaseException {
    if (NbaConstants.A_WT_CWA.equalsIgnoreCase(getWork().getWorkType())) {
        List moneyRequirements = getMoneyRequirements();
        int size = moneyRequirements.size();
        if (size > 0) {
            setParentWork(retrieveParentWork());
            List requirements = getParentWork().getNbaTransactions();
            int reqCount = requirements.size();
            NbaTransaction nbaTransaction = null;
            String moneyReqType = null;
            for (int i = 0; i < reqCount; i++) {
                nbaTransaction = (NbaTransaction) requirements.get(i);
                if (NbaConstants.A_WT_REQUIREMENT.equalsIgnoreCase(nbaTransaction.getWorkType())) {
                    NbaLob lob = nbaTransaction.getNbaLob();
                    long reqStatus = NbaUtils.convertStringToLong(lob.getReqStatus());
                    for (int j = 0; j < size; j++) {
                        moneyReqType = (String) moneyRequirements.get(j);
                        if (lob.getReqType() == NbaUtils.convertStringToInt(moneyReqType)
                                && (reqStatus != NbaOliConstants.OLI_REQSTAT_RECEIVED || reqStatus != NbaOliConstants.OLI_REQSTAT_WAIVED)) {
                            processMoneyRequirement(nbaTransaction);
                            break;
                        }
                    }
                }
            }
        }
    }
}

/**
 * Process and received money related requirements for received money. 
 * @param nbaTransaction the requirement workitem to be processed
 * @throws NbaBaseException
 */
//NBA192 New Method
protected void processMoneyRequirement(NbaTransaction nbaTransaction) throws NbaBaseException {
    List moneyRequirementQueues = getMoneyRequirementQueues();
    int size = moneyRequirementQueues.size();
    if (size > 0) {
        for (int i = 0; i < size; i++) {
            if (nbaTransaction.getQueue().equalsIgnoreCase((String) moneyRequirementQueues.get(i))) {
                NbaLob lob = nbaTransaction.getNbaLob();
            	if (NbaOliConstants.OLI_REQCODE_PREMDUE == lob.getReqType() && !(isTotalCWAAmtValid())) {// AXAL3.7.15
						//QC12083/APSL3278 begin
						RequirementInfo aReqInfo = nbaTxLife.getRequirementInfo(lob.getReqUniqueID());
						try {
							String comment = updatePDCBalanceDue(aReqInfo.getRequirementDetails());
							aReqInfo.setRequirementDetails(comment);
							aReqInfo.setActionUpdate();
							setContractUpdated(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
						//QC12083/APSL3278 end
						break;// AXAL3.7.15
				}//AXAL3.7.15
            	/*Begin APSL351
				 * If the case was negatively disposed. Requirements were sent to END. 
				 * case was reopened and reapproved. Premium Due did not generate a 2nd. time. 1st Premium Due is still sitting in the END. 
				 * NBCWA comes in and goes through Money Und- this is process that will receipt the Premium Due (if it one of 3 active queues). This did not occur as there was no active Prem Due. 
				 * added END Queue in the list of Money requirements queue to process Premium due carrier.
				 */
				boolean reqFlag = true;
				//APSL4768
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(nbaTxLife.getPrimaryHolding().getPolicy().getApplicationInfo());
				boolean cipeCase = appInfoExt.getInitialPremiumPaymentForm() == NbaOliConstants.OLI_PAYFORM_EFT;
				if (NbaOliConstants.OLI_REQCODE_PREMDUE == lob.getReqType()) {
					if(cipeCase){
						reqFlag = false;
					}else if(NbaConstants.A_STATUS_NEG_DISPOSED.equalsIgnoreCase(lob.getStatus()) && NbaConstants.END_QUEUE.equalsIgnoreCase(lob.getQueue())){
						reqFlag = true;
					}
				}else if (NbaConstants.END_QUEUE.equalsIgnoreCase(lob.getQueue())) {
					reqFlag = false;
				} 
				if (reqFlag) { //APSL351 added if
	                lob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED));
	                lob.setReqReceiptDate(Calendar.getInstance().getTime());
	                lob.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(Calendar.getInstance().getTime()));//QC20240
	                RequirementInfo aReqInfo = nbaTxLife.getRequirementInfo(lob.getReqUniqueID());
	                aReqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_RECEIVED);
	                aReqInfo.setReceivedDate(lob.getReqReceiptDate());
	                aReqInfo.setStatusDate(lob.getReqReceiptDate());
	                aReqInfo.setFulfilledDate(lob.getReqReceiptDate());
	                aReqInfo.setRequirementDetails("");//QC12083/APSL3278
	                aReqInfo.setActionUpdate();
	               // Begin AXAL3.7.01	
	                RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
					if (requirementInfoExt == null) {
						OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
						requirementInfoExt = olifeExt.getRequirementInfoExtension();
					}
	        		if (aReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_PREMDUE) {					
							
							requirementInfoExt.setPremiumDueCarrierReceiptDate(lob.getReqReceiptDate());
							//Begin APSL5144 
							if (!nbaTxLife.isTermLife()) {
								requirementInfoExt.setReviewedInd(true);
								requirementInfoExt.setReviewID(user.getUserID());
								requirementInfoExt.setReviewDate(new Date());
							}
							//End APSL5144 
							//Removed code - QC20240
						}        		
	        		//End AXAL3.7.01
	        		requirementInfoExt.setReceivedDateTime(lob.getReqReceiptDateTime());//QC20240
	        		requirementInfoExt.setActionUpdate();//QC20240
	                setContractUpdated(true);
	                NbaProcessStatusProvider provider = new NbaProcessStatusProvider(getUser(), lob);
	                nbaTransaction.setStatus(provider.getPassStatus());
	                nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
	                if (nbaTransaction.isSuspended()) {
	                    NbaSuspendVO suspendVO = new NbaSuspendVO();
	                    suspendVO.setTransactionID(nbaTransaction.getID());
	                    getUnsuspendList().add(suspendVO);
	                }
	                break;
				}// APSL351 End if
            }
        }
    }
}

/**
 * Calls VP/MS to check the CWA amount is sufficient or not.
 * 
 * @return True or False
 * @throws NbaBaseException
 */
//AXAL3.7.15 New Method
protected boolean isTotalCWAAmtValid() throws NbaBaseException {
	//SR545390 Retrofit Begin
	NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
	if (nbaTxLife != null) {
		oinkData.setContractSource(nbaTxLife);
	}
	oinkData.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
	//SR545390 Retrofit End
	String strResult = getDataFromVpms(NbaVpmsConstants.AUTOMONEYUNDERWRITING, NbaVpmsConstants.EP_IS_TOTAL_CWA_AMT_VALID, oinkData, null, null)
			.getResult();//SR545390
	if (strResult != null && !strResult.trim().equals("")) {
		return Boolean.valueOf(strResult).booleanValue();
	}
	return false;
}


/**
 * Calls VP/MS to get list of requirements that can be received.
 * @return the list of requirements that can be received 
 * @throws NbaBaseException
 */
//NBA192 New Method
protected List getMoneyRequirements() throws NbaBaseException {
    List moneyRequirements = new ArrayList();
    NbaVpmsModelResult data = new NbaVpmsModelResult(getDataFromVpms(NbaVpmsConstants.REQUIREMENTS, NbaVpmsConstants.EP_GET_MONEY_REQ_TYPES,
            new NbaOinkDataAccess(getWorkLobs()), null, null).getResult());
    if (data.getVpmsModelResult() != null && data.getVpmsModelResult().getResultDataCount() > 0) {
        ResultData resultData = data.getVpmsModelResult().getResultDataAt(0);
        int resultSize = resultData.getResult().size();
        for (int i = 0; i < resultSize; i++) {
            moneyRequirements.add(resultData.getResultAt(i));
        }
    }
    return moneyRequirements;
}



/**
 * Commit the changes to the work flow system.
 * @throws NbaBaseException
 */
//NBA192 New Method
protected void commit() throws NbaBaseException {
    if (getParentWork() != null) {
        setParentWork(update(getParentWork()));
        int size = getUnsuspendList().size();
        for (int i = 0; i < size; i++) {
            unsuspendWork((NbaSuspendVO) getUnsuspendList().get(i));
        }
    }
    updateWork();
}

/**
 * Unlocks the workitems in the work flow system.
 * @throws NbaBaseException
 */
//NBA192 New Method
protected void unlockWorkItems() throws NbaBaseException {
    NbaContractLock.removeLock(getUser());
    if (getParentWork() != null) {
        unlockWork(getParentWork());
    }  
}

/**
 * Returns the parent work
 * @return Returns the parentWork.
 */
//NBA192 New Method
protected NbaDst getParentWork() {
    return parentWork;
}
/**
 * Sets the parent work
 * @param parentWork the parentWork to set.
 */
//NBA192 New Method
protected void setParentWork(NbaDst parentWork) {
    this.parentWork = parentWork;
}
/**
 * Returns true if contract has any update to be commited
 * @return true if contract has any update to be commited
 */
//NBA192 New Method
protected boolean hasContractUpdated() {
    return contractUpdated;
}
/**
 * Sets true if contract has any update to be commited
 * @param contractUpdated the contractUpdated to set.
 */
//NBA192 New Method
protected void setContractUpdated(boolean contractUpdated) {
    this.contractUpdated = contractUpdated;
}
/**
 * Retuens unsuspend list.
 * @return the unsuspendList.
 */
//NBA192 New Method
protected List getUnsuspendList() {
    if (unsuspendList == null) {
        unsuspendList = new ArrayList();
    }
    return unsuspendList;
}

// set deOink map for scan station
//AXAL3.7.20 added new method
protected Map getDeOinkMapForSources(NbaDst work){
 
	Map deOinkMap = new HashMap();
	List sourceList = work.getNbaSources();
	int count = sourceList.size();
	NbaSource aSource = null;
	String sourceType = null;
	for (int i = 0; i < count; i++) {
		aSource = (NbaSource) sourceList.get(i);
		sourceType = aSource.getSource().getSourceType();
		if (NbaConstants.A_WT_PAYMENT.equals(sourceType)|| NbaConstants.A_ST_CWA_CHECK.equals(sourceType)) {
			deOinkMap.put(NbaVpmsConstants.A_CREATE_STATION, aSource.getNbaLob().getCreateStation()); // ALS3046
			break;
		}
	}
	return deOinkMap;
}	

//ALS5029 New method 
protected NbaVpmsMoneyUnderwritingData underwriteMoneyApprovedPolicy(NbaTXLife xml203) throws NbaBaseException {
	NbaDst parentCase = retrieveParentWork(false); 	
	AxaOinkDataAccess oinkData = new AxaOinkDataAccess();
	oinkData.setContractSource(nbaTxLife, parentCase.getNbaLob());
	oinkData.setSecondaryLobSource(getWork().getNbaLob());

	NbaOinkRequest oinkRequest = new NbaOinkRequest(); 
	NbaVpmsAdaptor vpmsProxy = null;
    try {
        vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.AUTOMONEYUNDERWRITING);
		vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_AUTO_MONEY_UNDERWRITING_APPROVED_CASE_RESULTS);
		vpmsProxy.setANbaOinkRequest(oinkRequest);

		NbaVpmsMoneyUnderwritingData data = new NbaVpmsMoneyUnderwritingData(vpmsProxy.getResults());
		return data;
    } catch (java.rmi.RemoteException re) {
        throw new NbaBaseException("Problem in getting data from VPMS", re);
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


/**
 * Create a TX Request value object that will be used to retrieve the contract.
 * @param nbaDst the workitem object for that holding request is required
 * @param access the access intent to be used to retrieve the data, either READ or UPDATE
 * @param businessProcess the name of the business function or process requesting the contract
 * @return a value object that is the request
 * @throws NbaBaseException
 */
	//ALS5026 New Method
	public NbaTXRequestVO createRequestObject(NbaDst nbaDst, int access, String businessProcess){
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
		try {
			parentWork = retrieveParentWork(false);
		} catch (NbaBaseException e1) {
				e1.printStackTrace();
		}
		//Setting parent LOB as the check might be from different company, also for retriving the parent case TxLife parent case LOb should be used
		nbaTXRequest.setNbaLob(parentWork.getNbaLob());
		nbaTXRequest.setNbaUser(getUser());
		nbaTXRequest.setWorkitemId(nbaDst.getID());
		nbaTXRequest.setCaseInd(nbaDst.isCase());
	
		if (access != -1) {
			nbaTXRequest.setAccessIntent(access);
		} else {
			nbaTXRequest.setAccessIntent(READ);
		}
		if (businessProcess != null) {
			nbaTXRequest.setBusinessProcess(businessProcess);
		} else {
			nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser())); //SPR2639 
		}
		return nbaTXRequest;
	}
	
	/**
	 * Updates a Premium Due Carrier requirement wokitem details with balance due.
	 */
	
	//New Method-QC12083/APSL3278
	protected String updatePDCBalanceDue(String comment) throws NbaBaseException {
		double cwaLob = getWork().getNbaLob().getCwaAmount();
		ApplicationInfo appInfo = nbaTxLife.getPolicy().getApplicationInfo();
		double cwaOnCase=appInfo.getCWAAmt();
		double premDue = appInfo.getPremBalDue();
		DecimalFormat df = new DecimalFormat("#,###,##0.00");
		String strResult = "$ "+df.format(premDue-cwaLob)+"of $"+df.format((premDue+cwaOnCase))+"due";
		return strResult;
	
	}
	
	
    
}