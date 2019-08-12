package com.csc.fsg.nba.business.process;

/* 
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */


import java.rmi.RemoteException;
import java.text.DateFormat;
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

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.dataobject.nba.cash.CheckAllocation;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractCheckData;
import com.csc.fsg.nba.tableaccess.NbaFormsValidationData;
import com.csc.fsg.nba.tableaccess.NbaTable;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.CopyLobsTaskConstants;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsRequirement;
import com.csc.fsg.nba.vpms.NbaVpmsRequirementsData;

/**
 * NbaProcElectronicMoneyDetermination is the class that processes nbAccelerator cases found on the AWD electronic money determniation queue (NBELCMNY). The purpose of
 * this step in the workflow is to determine if money is nbA money for routing, assign a bundle number to checks, add wire transfers to the wire
 * transfer datbase for reconciliation, associate one check with multiple applications when send in and indexed for multiple contracts, and add checks
 * to the cashiering database tables for tracking and deposits.
 * <p>
 * The NbaProcElectronicMoneyDetermination class extends the NbaAutomatedProcess class. Although this class may be instantiated by any module, the NBA polling
 * class will be the primary creator of objects of this type.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th> <th align=left>Release</th> <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>APSL2735</td> <td>Discretionary</td> <td>Electronic Inforce Payment</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaProcElectronicMoneyDetermination extends NbaProcMoneyDetermination {
	
	
	public NbaProcElectronicMoneyDetermination(){
		super();
	}
	
	NbaDst nbaCase = null;
	
	/**
	 * @return Returns the nbaCase.
	 */
	public NbaDst getNbaCase() {
		return nbaCase;
	}
	/**
	 * @param nbaCase The nbaCase to set.
	 */
	public void setNbaCase(NbaDst nbaCase) {
		this.nbaCase = nbaCase;
	}
	
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {

		if (!initialize(user, work)) {
			return getResult();
		}
		if (!isPaymentDraftNeededSuspension()) { // APSL3351
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(getWork().getID(), false);
			retOpt.requestSources();
			retOpt.setLockWorkItem();
			setWork(retrieveWorkItem(getUser(), retOpt));
			setNbaCase(retrieveParentWork(getWork(), true, true, false));// APSL2735
			
			// APSL5055-NBA331 Begin
						if (getWork().getNbaSources().size() > 0) {
							 setNbaSource((NbaSource) getWork().getNbaSources().get(0));
						}
			// APSL5055-NBA331 End
						
			// begin APSL3836
			try { // Process Check Payment
				if (getWork().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CWA)) {
					processCheckPayment();
				} else if (getWork().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_SERVICE_RESULT)
						&& NbaConstants.ACH_ORDER_REQUIREMENT.equalsIgnoreCase(getWork().getNbaLob().getACHAction())) {
					createRequirement();
				} else if ((getWork().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_SERVICE_RESULT) ||getWork().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_AGGREGATE_CONTRACT)  ||getWork().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_NBVALDERR)  )
						&& NbaConstants.ACH_DRAFT_REQUEST.equalsIgnoreCase(getWork().getNbaLob().getACHAction())) {
					createCWA();
					unsuspendPDR();
				} else if (getWork().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REQUIREMENT) && getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_AUTHEFT && getWork().getNbaLob().getACHIndicator()) { //NBLXA-1789 
						createCWA();
						unsuspendPDR();
						//NBLXA-1789 begins
					/*} else {
						selectivelyRouteAllPDRs();
					}*/
						//NBLXA-1789 ends
				} else if (NbaConstants.A_WT_REQUIREMENT.equalsIgnoreCase(getWork().getWorkType())
						&& (getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT)
						&& (NbaUtils.getAppInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo()).getInitialPremiumPaymentForm() == NbaOliConstants.OLI_PAYMETH_ETRANS)) {
					if (!isOutStandingFailureReceived()) {// ALII2015
						suspendPaymentDraft(getPDRSuspendDays(), "Suspended awaiting Authorization - ACH Form to be received"); // APSL3351
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getWork().getStatus())); //NBLXA-1282
					} else if(!getNbaTxLife().isIssued() && !getNbaTxLife().isPaidReIssue()
							&& (NbaUtils.isBlankOrNull(getWork().getNbaLob().getPaymentMoneySource()) || (!NbaUtils.isBlankOrNull(getWork().getNbaLob().getPaymentMoneySource())
									&& !getWork().getNbaLob().getPaymentMoneySource().equalsIgnoreCase(String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT))))) { //APSL4241
						createCWAforPDRRequirement();
						//APSL4241 begins
						getWork().getNbaLob().setPaymentMoneySource(String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT));
						//APSL4241 ends
					}// End APSL3836
				}
			} catch (NbaDataAccessException dae) {
				addComment("SQL Error " + dae.getFormattedMessage());
				if (getLogger().isDebugEnabled()) {
					getLogger().logError(dae);
					dae.printStackTrace();
				}
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getSqlErrorStatus()));
			} catch (NbaBaseException nbe) {
				if (getLogger().isDebugEnabled()) {
					getLogger().logError(nbe);
					nbe.printStackTrace();
				}
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
			}
			if (getResult() == null) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}
			changeStatus(getResult().getStatus());

			if (getWork().isLocked(getUser().getUserID())) {
				doUpdateWorkItem();
			}
			unlockWork(getUser(), getNbaCase());
		}
		return getResult();
	}
		
	/**
	 * This methods processes the incoming check. It first updates the NBA_CHECKS table and then verifies the check amount It proceses Nba Money and
	 * non Nba Money and created cashiering work item if needed
	 * 
	 * @exception NbaBaseException
	 */
	protected void processCheckPayment() throws NbaBaseException {
	
		NbaCheckData nbaCheckDataTable = new NbaCheckData();
		NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);
		NbaLob sourceLOB = nbaSource.getNbaLob(); 
		setCheckAllocations(retrieveCheckAllocations()); // APSL5055-NBA331
		if (!performCheckValidations(nbaSource, sourceLOB)) {
			return; 
		}
		
		//Sets check number in MMDDYY99999 format
		if(sourceLOB.getCheckNumber() ==  null || sourceLOB.getCheckNumber().length() == 0){
		sourceLOB.setCheckNumber(NbaUtils.generateCheckNumber(getNbaTxLife().getOLifE().getHoldingAt(0).getPolicy().getPolNumber()));
		nbaSource.setUpdate();
		}
		
		insertCheckDataTable(nbaCheckDataTable, nbaSource, sourceLOB); 

		if (sourceLOB.getBundleNumber() == null || sourceLOB.getBundleNumber().length() == 0) { 
			sourceLOB.setBundleNumber(getBundleID()); 
			nbaSource.setUpdate();
		}
		
		NonNbaPayments nonNbaPayments = new NonNbaPayments(1);//cashMultipleCount); 
		NbaContractCheckData nbaContractCheckDataTable = new NbaContractCheckData();
		
		boolean processedNbaMoney = processCheckMoney(nonNbaPayments, nbaContractCheckDataTable); // APSL5055
		
		if (!processedNbaMoney) {
			//Update Check table to set Include Indicator to true if there is no nba money
			NbaCheckData.updateIncludeInd(true, getBundleID(), sourceLOB.getCheckAmount(), sourceLOB.getCwaDate(), sourceLOB.getCheckNumber(),
					nbaSource.getSource().getCreateStation(), nbaSource.getCreateDate()); 
		}
		if (nonNbaPayments.getCount() > 0) {
			//create non nbA work items
			createNonNbaPaymentWorkItems(nonNbaPayments.getIndexOfNbaNonPaymentArray(), processedNbaMoney);
		}
	}
		

	/**
	 * Check the source LOBS to make sure required data is present
	 * 
	 * @param nbaSource
	 * @param sourceLOB
	 * @return
	 * @throws NbaBaseException
	 */
	
	protected StringBuffer checkForMissingLOBs(NbaSource nbaSource, NbaLob sourceLOB) throws NbaBaseException {
		StringBuffer buf = new StringBuffer();
		String type = "Check";
		if (! NbaUtils.isAdcApplication(getWork()) && sourceLOB.getFinancialInstitutionName() == null) { //APSL4507
			appendToMissingDataBuffer(buf, type, "Bank Name");
		}
		if (String.valueOf(NbaOliConstants.OLI_PT_PERSON).equalsIgnoreCase(sourceLOB.getCheckIdentity())) {
			if (sourceLOB.getFirstName() == null) {
				appendToMissingDataBuffer(buf, type, "First Name");
			}
			if (sourceLOB.getLastName() == null) {
				appendToMissingDataBuffer(buf, type, "Last Name");
			}
		} else {
			if (sourceLOB.getCheckEntityName() == null) {
				appendToMissingDataBuffer(buf, type, "Entity Name");
			}
		}
		if (NbaUtils.isBlankOrNull(sourceLOB.getPaymentCategory())) {
			appendToMissingDataBuffer(buf, type, "Account Type");
		}
		if (sourceLOB.getBankRoutingNumber() == null) {
			appendToMissingDataBuffer(buf, type, "Routing/Transit#");
		}
		if (sourceLOB.getBankAccountNumber() == null) {
			appendToMissingDataBuffer(buf, type, "Account#");
		}
		if (sourceLOB.getCheckAmount() <= 0) {
			appendToMissingDataBuffer(buf, type, "Check Amount");
		}
		return buf;
	}
	
	
	/**
	 * This method processes Nba Money
	 * 
	 * @param caseID
	 *            the caseID for which payment has to be made
	 * @param position
	 *            the index to retrieve the information from the source
	 * @param primaryContractInd
	 *            determines if the contract is primary or not
	 * @param nbaContractCheckDataTable
	 *            instance of NbaContractCheckData class
	 * @exception NbaBaseException
	 */
	// NBA331.1 APSL5055
	protected void processNbaMoney(String caseID, CheckAllocation allocation, boolean primaryContractInd, NbaContractCheckData nbaContractCheckDataTable)
			throws NbaBaseException {
		String productType = null;
		String productCode = null; 
		StringBuffer primaryInsName = new StringBuffer();
		NbaDst nbaCase = retrieveCase(caseID);

		NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);

		productType = nbaCase.getNbaLob().getProductTypSubtyp();

		productCode = nbaCase.getNbaLob().getPlan(); 
		
		if (nbaCase.getNbaLob().getLastName() != null) {
			primaryInsName.append(nbaCase.getNbaLob().getLastName()).append(",");
		}
		if (nbaCase.getNbaLob().getFirstName() != null) {
			primaryInsName.append(nbaCase.getNbaLob().getFirstName()).append(" ");
		}
		if (nbaCase.getNbaLob().getMiddleInitial() != null) {
			primaryInsName.append(nbaCase.getNbaLob().getMiddleInitial());
		}
		// If there's only contract associated the check, the user only has to enter a check amount and
		// does not have to enter an amount to be applied.
		// Money determination should set CWAM equal to CKAM if not present.
		double amount = nbaSource.getNbaLob().getCheckAmount();
		long distChannel = nbaCase.getNbaLob().getDistChannel(); //APSL3410
		//Update Contract to Check table
		updateContractToCheckTable(allocation.getPolicyNumber(), allocation.getCompany(), amount, productType,
				NbaUtils.prepareSearchString(primaryInsName.toString()), nbaCase.getNbaLob().getBackendSystem(), primaryContractInd,
				nbaContractCheckDataTable, productCode, distChannel); // APSL3410, APSL5055

		//if the work item is CWA and if the case is one with which that CWA is attached
		// then do not create another CWA
		if (nbaCase.getNbaLob().getPolicyNumber().equalsIgnoreCase(getWork().getNbaLob().getPolicyNumber())
				&& getWork().getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CWA)) {
			//copy the lob fields from source to CWA work item
			NbaLob workLob = getWork().getNbaLob();
			NbaLob sourceLob = nbaSource.getNbaLob();
			workLob.setCwaAmount(amount);
			workLob.setCompany(allocation.getCompany()); // APSL5055
			workLob.setCheckIdentity(sourceLob.getCheckIdentity());
			if(sourceLob.getCheckIdentity().equalsIgnoreCase(String.valueOf(NbaOliConstants.OLI_PT_PERSON))){
				workLob.setLastName(sourceLob.getLastName());
				workLob.setMiddleInitial(sourceLob.getMiddleInitial());
				workLob.setFirstName(sourceLob.getFirstName());
			}else{
				workLob.setCheckEntityName(sourceLob.getCheckEntityName());
			}
			workLob.setFinancialInstitutionName(sourceLob.getFinancialInstitutionName());
			workLob.setAccountOwner(sourceLob.getAccountOwner());
			workLob.setBankRoutingNumber(sourceLob.getBankRoutingNumber());
			workLob.setBankAccountNumber(sourceLob.getBankAccountNumber());
			workLob.setPaymentMoneySource(sourceLob.getPaymentMoneySource());
			workLob.setPaymentCategory(sourceLob.getPaymentCategory());
			workLob.setCheckAmount(sourceLob.getCheckAmount());
			workLob.setCwaDate(sourceLob.getCwaDate());
			//APSL5164 Begin NBLXA-1256 code deleted for Variable product
			workLob.setCIPEPaymentIGO(sourceLob.getCIPEPaymentIGO());
			workLob.setCIPEPaymentReqType(sourceLob.getCIPEPaymentReqType());
			workLob.setCipeOtherReason(sourceLob.getCipeOtherReason());
				try {
					if (!NbaUtils.isBlankOrNull(sourceLob.getCwaTime()))
						workLob.setCwaTime(NbaUtils.getTimefromString(sourceLob.getCwaTime()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			//APSL5164 Ends
			List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2MNYDTM_NBCWA_APPLYMONEY_TASK);
			sourceLob.copyLOBsTo(workLob, lobList);
		} 
	}
	
	/**
	 * The method determines if all the payments are non nbA there are multiple contracts. If yes, it creates new work items for all the non nbA
	 * payments. Otherwise, it creates generic payment work items. and sets appropriate status.
	 * 
	 * @param nonNbaPayments
	 *            Total number of non NbaPayments
	 * @param nbaPayment
	 *            boolean value representing true or false
	 * @throws NbaBaseException
	 */
	protected void createNonNbaPaymentWorkItems(int[] nonNbaPayments, boolean nbaPayment) throws NbaBaseException {
		NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);
		//copy the lob fields from source to Generic Payment work item
		getWork().getNbaLob().setBundleNumber(nbaSource.getNbaLob().getBundleNumber());
		//Create Generic payment work items

		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, getWork(), nbaSource.getSource().getSourceType(),
				VPMS_NON_NBAMONEY);
		NbaLob sourceLob = nbaSource.getNbaLob();
		NbaLob nbaLob = null;
		for (int i = 0; i < nonNbaPayments.length; i++) {
			if (nonNbaPayments[i] != 0) {
				// If there's only one contrat associated with check, the user only has to enter a check amount and
				// does not have to enter an amount to be applied.
				// Money determination should set CWAM equal to CKAM if not present.								
				CheckAllocation allocation = getCheckAllocation(nonNbaPayments[i]);  //NBA331.1 APSL5055
				double amount;
				if (getNumberOfContracts() == 1 && allocation.getCwaAmount() == null) {				
					amount = sourceLob.getCheckAmount();
				} else {
					amount = allocation.getCwaAmount(); // APSL5055
				}
				
				nbaLob = new NbaLob();
				//set portal indicator to false
				nbaLob.setPortalCreated(false);
				//copy the lob fields from source to Generic Payment work item
				nbaLob.setPolicyNumber(allocation.getPolicyNumber());// APSL5055
				nbaLob.setCompany(allocation.getCompany()); //APSL5055
				nbaLob.setCwaAmount(amount);
				nbaLob.setFinancialInstitutionName(sourceLob.getFinancialInstitutionName());
				nbaLob.setAccountOwner(sourceLob.getAccountOwner());
				nbaLob.setBankAccountNumber(sourceLob.getBankAccountNumber());
				nbaLob.setBankRoutingNumber(sourceLob.getBankRoutingNumber());
				nbaLob.setPaymentCategory(sourceLob.getPaymentCategory());
				nbaLob.setPaymentMoneySource(sourceLob.getPaymentMoneySource());
				nbaLob.setCheckAmount(sourceLob.getCheckAmount());
				nbaLob.setCwaDate(sourceLob.getCwaDate());
				nbaLob.setCheckIdentity(sourceLob.getCheckIdentity());
				if (sourceLob.getCheckIdentity().equalsIgnoreCase(String.valueOf(NbaOliConstants.OLI_PT_PERSON))) {
					nbaLob.setLastName(sourceLob.getLastName());
					nbaLob.setMiddleInitial(sourceLob.getMiddleInitial());
					nbaLob.setFirstName(sourceLob.getFirstName());
				} else {
					nbaLob.setCheckEntityName(sourceLob.getCheckEntityName());
				}
				List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2MNYDTM_CREATE_GENERICPAYMENT_TASK);
				sourceLob.copyLOBsTo(nbaLob, lobList);
				//Create the work item with the same status as the current work item to prevent it from being locked by another process.
				NbaDst nbaDst = createCase(getUser(), NbaConstants.A_BA_NBA, provider.getWorkType(), provider.getInitialStatus(), nbaLob);

				nbaDst.addNbaSource(nbaSource);
				nbaDst.increasePriority(provider.getWIAction(), provider.getWIPriority());
				//Set the real status
				//Always route to status provided by provider.InitialStatus()
				nbaDst.getTransaction().setLockStatus(getUser().getUserID());
				updateWork(getUser(), nbaDst);
				unlockWork(getUser(), nbaDst);
			}
		}
	}

	
	public void createRequirement() {
		NbaVpmsAdaptor proxy = null;
		try {
			NbaLob workLOB = work.getNbaLob();
			Map deOinkMap = new HashMap();
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(work.getNbaLob());

			workLOB.setACHIndicator(true);
			work.setActionUpdate();
			
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsAdaptor.ACREQUIREMENTSDETERMINATION);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_ORDER_ACH_REQ);
			proxy.setSkipAttributesMap(deOinkMap);

			VpmsComputeResult aResult = proxy.getResults();
			if (aResult.getReturnCode() == 1) { // no requirements found
				aResult.setResult("0");
			}

			NbaVpmsRequirementsData vpmsRequirementsData = new NbaVpmsRequirementsData(aResult);

			if (!vpmsRequirementsData.wasSuccessful() && vpmsRequirementsData.getRequirements().size() > 0) {
				List requirements = vpmsRequirementsData.getRequirements();

				ListIterator li = requirements.listIterator();
				String tempReqStatus = workLOB.getReqStatus();
				workLOB.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_ORDER));
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), workLOB);
				workLOB.setReqStatus(tempReqStatus);

				NbaRequirementUtils reqUtils = new NbaRequirementUtils();
				reqUtils.setHoldingInquiry(nbaTxLife);
				reqUtils.setEmployeeId(getUser().getUserID());

				NbaTransaction nbaTransaction = null;
				NbaVpmsRequirement vpmsRequirement = null;
				while (li.hasNext()) {
					vpmsRequirement = (NbaVpmsRequirement) li.next();
					nbaTransaction = nbaCase.addTransaction(provider.getWorkType(), provider.getInitialStatus());
					nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
					nbaTransaction.getTransaction().setLock("Y");
					//set action flag so new transaction will be created with a lock
					nbaTransaction.getTransaction().setAction("L");
					processRequirement(nbaTransaction, vpmsRequirement, reqUtils, workLOB, nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_INSURED));
				}
				doContractUpdate(nbaTxLife);
				nbaCase = updateWork(getUser(), nbaCase);
			}
		} catch (Exception ex) {
			getLogger().logError(ex);
		}
		try {
			if (proxy != null) {
				proxy.remove();
			}
		} catch (RemoteException e) {
			LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
		} catch (NbaVpmsException e) {
			LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
		}
	}
	
	public String getGenericReqQueryForFormsTable(Policy policy, long relationRoleCode, long reqtype) {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		String appSignedDate = null;
		if(policy.getApplicationInfo().getSignedDate() != null) {
			appSignedDate = df.format(policy.getApplicationInfo().getSignedDate());
		}else {
			appSignedDate = df.format(new Date());
		}
		StringBuffer query = new StringBuffer("Select COMPANY_CODE, APPLICATION_TYPE, PRODUCT_TYPE, COVERAGE_KEY, REPLACEMENT_IND, "
				+ "INS_ROLE, START_DATE, END_DATE, APPLICATION_STATE, QUESTION_NUMBER, QUESTION_ANSWER, REQUIREMENT_TYPE, FORM_NUMBER, "
				+ "PROVIDER, FOLLOWUP_PROVIDER, FORM_TYPE from NBA_FORMS_VALIDATION"); 

		query.append(" WHERE (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COMPANY_CODE, policy.getCarrierCode()));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COMPANY_CODE, "*"));
		query.append(")");
		query.append(" AND ("
				+ NbaTableHelper.formatSQLWhereCriterion(NbaTable.APP_TYPE, Long.toString(policy.getApplicationInfo().getApplicationType())));
		query.append(") AND (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.PRODUCT_TYPE, Long.toString(policy.getProductType())));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.PRODUCT_TYPE, "*"));
		query.append(") AND (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COVERAGE_KEY, policy.getProductCode()));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COVERAGE_KEY, "*"));
		query.append(")");
		query.append(" AND (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.INS_ROLE, relationRoleCode));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.INS_ROLE, "*"));
		query.append(")");
		query.append(" AND " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.APPLICATION_STATE, "*"));
		query.append(" AND (to_date('" + appSignedDate.toString() + "', 'mm-dd-yy') BETWEEN START_DATE AND END_DATE)");
		query.append(" AND "+ NbaTableHelper.formatSQLWhereCriterion(NbaTable.REQUIREMENT_TYPE, reqtype));
		return query.toString();
	}

	
	protected void processRequirement(NbaTransaction nbaTransaction, NbaVpmsRequirement vpmsRequirement, NbaRequirementUtils reqUtils,
			NbaLob workLOB, String partyId) throws NbaBaseException {

		NbaTableAccessor tableaccessor = new NbaTableAccessor();
		Map caseData = tableaccessor.setupTableMap(getWork());
		Relation relation = NbaUtils.getPrimaryInsured(getNbaTxLife());
		String query = getGenericReqQueryForFormsTable(getNbaTxLife().getPrimaryHolding().getPolicy(), relation.getRelationRoleCode(), vpmsRequirement.getType());
		NbaFormsValidationData nbaFormsValidations[] = (NbaFormsValidationData[]) tableaccessor.getFormRequirementsForQuestions(caseData,
				"NBA_FORMS_VALIDATION", query);
		NbaLob lob = nbaTransaction.getNbaLob();
		if(nbaFormsValidations.length > 0 ){
			NbaFormsValidationData formsValidationData = nbaFormsValidations[0];
			lob.setReqVendor(formsValidationData.getProvider()); // Vendor code
			lob.setFormNumber(formsValidationData.getFormNumber());
		}
		lob.setCheckAmount(workLOB.getCheckAmount());
		lob.setReqType(vpmsRequirement.getType()); // Req Type
		lob.setAgency(workLOB.getAgency());
		lob.setAgentID(workLOB.getAgentID());
		lob.setCompany(workLOB.getCompany());
		lob.setAppState(workLOB.getAppState());
		lob.setPlan(workLOB.getPlan());
		lob.setProductTypSubtyp(workLOB.getProductTypSubtyp());
		lob.setFaceAmount(workLOB.getFaceAmount());
		lob.setAppOriginType(workLOB.getAppOriginType());
		lob.setReplacementIndicator(workLOB.getReplacementIndicator());
		lob.setPaidChgCMQueue(workLOB.getPaidChgCMQueue());
		lob.setACHIndicator(true);
		lob.setPaymentMoneySource(String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT));
		lob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_ADD)); //ALII1960
		RequirementInfo aReqInfo = reqUtils.createNewRequirementInfoObject(nbaTxLife, partyId, vpmsRequirement, getUser(), lob);
		Policy policy = nbaTxLife.getPolicy();
		policy.addRequirementInfo(aReqInfo);
		policy.setActionUpdate();
		lob.setReqUniqueID(aReqInfo.getRequirementInfoUniqueID());
		lob.setReqRestriction(aReqInfo.getRestrictIssueCode());
		aReqInfo.setRequirementDetails(vpmsRequirement.getComment()); //Variable data
		//Begin ALII1960
		if (relation != null) {
			reqUtils.setReqPersonCodeAndSeq((int) relation.getRelationRoleCode(), Integer.parseInt(relation.getRelatedRefID()));
		}
		//End ALII1960
		reqUtils.setReqType(vpmsRequirement.getType());
		reqUtils.processRequirementWorkItem(nbaCase, nbaTransaction); //update LOBs
		reqUtils.addRequirementControlSource(nbaTransaction);
		reqUtils.addMasterRequirementControlSource(getNbaCase(), nbaTransaction);
		updateRequirementInfo(nbaTransaction, nbaTxLife.getRequirementInfo(nbaTransaction.getNbaLob().getReqUniqueID())); //NBA130
		
	}
	
	protected void createCWA() throws NbaBaseException {
		if (getWork().getNbaLob().getCheckAmount() > 0) {
			
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, getWork(), NbaConstants.A_ST_XML508, VPMS_NBAMONEY);
			NbaTransaction nbaTransaction = nbaCase.addTransaction(provider.getWorkType(), provider.getInitialStatus());
			NbaLob transactionLob = nbaTransaction.getNbaLob();
			NbaLob workLOB = getWork().getNbaLob();
			nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
			transactionLob.setPortalCreated(false);
			transactionLob.setPolicyNumber(workLOB.getPolicyNumber());
			transactionLob.setCompany(workLOB.getCompany());
			transactionLob.setBackendSystem(workLOB.getBackendSystem());
			transactionLob.setLastName(workLOB.getLastName());
			transactionLob.setFirstName(workLOB.getFirstName());
			transactionLob.setSsnTin(workLOB.getSsnTin());
			transactionLob.setWorkSubType((int)NbaOliConstants.OLI_WORKSUBTYPE_ACH);//APSL3836
			nbaTransaction.getNbaLob().setOkToAdjust(getWork().getNbaLob().getOkToAdjust()); //APSL5254
			// APSL5164 Begin NBLXA-1256 code deleted for Variable product
				transactionLob.setCIPEPaymentIGO(workLOB.getCIPEPaymentIGO());
				transactionLob.setCIPEPaymentReqType(workLOB.getCIPEPaymentReqType());
				transactionLob.setCipeOtherReason(workLOB.getCipeOtherReason());
				transactionLob.setCwaDate(workLOB.getCwaDate());
				try {
					if (!NbaUtils.isBlankOrNull(workLOB.getCwaTime()))
						transactionLob.setCwaTime(NbaUtils.getTimefromString(workLOB.getCwaTime()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			//APSL5164 Ends
			addNewCwaCheckSource(nbaTransaction);

			update(nbaCase);
			update(getWork());
		}
		
	}
	
	protected NbaDst retrieveParent() throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestCaseAsParent();
		retOpt.setLockParentCase();
		return retrieveWorkItem(getUser(), retOpt);
	}
	
	/**
	 * Populate CWACHECK source Lob or NBCWA WI Lob
	 * @param sourceLob the NbaLob
	 * @param indexVO the input index row bean
	 * @throws NbaBaseException
	 */
	//APSL2735 New Method
	protected void populateCWAWorkItemSource(NbaLob nbaLob) throws NbaBaseException  {
		Banking banking = NbaUtils.getBankingByHoldingSubType(getNbaTxLife(), NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		nbaLob.setCompany(getNbaTxLife().getPrimaryHolding().getPolicy().getCarrierCode());
		Holding initialHolding = NbaUtils.getHoldingByTypeAndSubTypeCode(getNbaTxLife().getOLifE(), NbaOliConstants.OLI_HOLDTYPE_BANKING, NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		NbaParty payerParty = null;
		if(initialHolding != null){
			payerParty = getNbaTxLife().getParty(getNbaTxLife().getPayerPartyId(NbaOliConstants.OLI_REL_PAYER, initialHolding.getId()));
			if (payerParty != null && payerParty.isPerson()) {
				Person person = payerParty.getPerson();
				if (!NbaUtils.isBlankOrNull(person.getFirstName())) {
					nbaLob.setFirstName(person.getFirstName());
				}
				if (!NbaUtils.isBlankOrNull(person.getMiddleName())) {
					nbaLob.setMiddleInitial(person.getMiddleName());
				}
				if (!NbaUtils.isBlankOrNull(person.getLastName())) {
					nbaLob.setLastName(person.getLastName());
					nbaLob.setCheckLastName(person.getLastName());
				}
				nbaLob.setCheckIdentity(String.valueOf(payerParty.getPartyTypeCode()));
			}
		}
		if (banking != null) {
			BankingExtension bankingExt = NbaUtils.getFirstBankingExtension(banking);
			if (bankingExt != null && !NbaUtils.isBlankOrNull(bankingExt.getBankName())) {
				nbaLob.setFinancialInstitutionName(bankingExt.getBankName());
			}
			if (payerParty != null && payerParty.isOrganization()) {
				nbaLob.setCheckIdentity(String.valueOf(payerParty.getPartyTypeCode()));
				nbaLob.setCheckEntityName(payerParty.getOrganization().getDBA());
			}
			nbaLob.setAccountOwner(NbaUtils.getFullName(payerParty));
			banking.setAcctHolderName(NbaUtils.getFullName(payerParty));
			if (!NbaUtils.isBlankOrNull(banking.getRoutingNum())) {
				nbaLob.setBankRoutingNumber(banking.getRoutingNum());
			}
			if (!NbaUtils.isBlankOrNull(banking.getAccountNumber())) {
				nbaLob.setBankAccountNumber(banking.getAccountNumber());
			}
			
			nbaLob.setCheckAmount(getWork().getNbaLob().getCheckAmount());
			if (!NbaUtils.isBlankOrNull(banking.getBankAcctType())) {
				nbaLob.setPaymentCategory(banking.getBankAcctType());
			}
			//Begin APSL5164 NBLXA-1256 removed check for Variable product
			if ((getWork().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_SERVICE_RESULT) || getWork().getWorkType().equalsIgnoreCase(
						NbaConstants.A_WT_REQUIREMENT)
						&& getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_AUTHEFT)
						&& NbaConstants.ACH_DRAFT_REQUEST.equalsIgnoreCase(getWork().getNbaLob().getACHAction())) {
					nbaLob.setCwaDate(getWork().getNbaLob().getCwaDate());
					if (getWork().getNbaLob().getCwaTime() != null)
						try {
							nbaLob.setCwaTime(NbaUtils.getTimefromString(getWork().getNbaLob().getCwaTime()));
						} catch (ParseException e) {
							e.printStackTrace();
						}
				} else if (NbaConstants.A_WT_REQUIREMENT.equalsIgnoreCase(getWork().getWorkType())
						&& (getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT)
						&& (NbaUtils.getAppInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo()).getInitialPremiumPaymentForm() == NbaOliConstants.OLI_PAYMETH_ETRANS)) {
					// fetch source/trans---
					List sourceList1 = getWork().getNbaSources();
					Date sourceAppDate = null;
					if (sourceList1 != null) {
						for (int j = 0; j < sourceList1.size(); j++) {
							NbaSource source = (NbaSource) sourceList1.get(j);
							if ((NbaConstants.A_WT_REQUIREMENT.equals(source.getSourceType()) || NbaConstants.A_ST_PROVIDER_RESULT.equals(source
									.getSourceType())) && getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT) {
								if ((null == sourceAppDate || (null != sourceAppDate && (NbaUtils.getDateFromStringInAWDFormat(source.getNbaLob()
										.getCreateDate()).after(sourceAppDate))))) {
									sourceAppDate = NbaUtils.getDateFromStringInAWDFormat(source.getNbaLob().getCreateDate());

								}
							}
						}
						if (null != sourceAppDate) {
							Date estDate = NbaUtils.convertCstToEst(sourceAppDate);
							if (null != estDate) {
								nbaLob.setCwaDate(estDate);
								nbaLob.setCwaTime(estDate);
							}
						}
					}

				}
			
		}
	}
	
	//APSL2735 New Method
	protected void createCWAforPDRRequirement() throws NbaBaseException{
		//Begin APSL2735
		NbaTXLife txLife = getNbaTxLife();
		NbaDst parentCaseWithTrans = getNbaCase();
		Iterator transactionItr = parentCaseWithTrans.getNbaTransactions().listIterator();
		double outStandingOne = NbaVPMSHelper.getPremiumDue(txLife);
		double validatingAmt = 0.0;
		double txLifePDISValue = NbaUtils.getPolicydeliveryInstrutionAmt(txLife.getPolicy());
		while (transactionItr.hasNext()) {
			NbaTransaction nbaTrans = (NbaTransaction) transactionItr.next();
			if ((nbaTrans.getNbaLob().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_SERVICE_RESULT) && isServiceResultOutstanding(nbaTrans))
					|| (nbaTrans.getNbaLob().getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)
							&& (nbaTrans.getNbaLob().getReqType() == (int)NbaOliConstants.OLI_REQCODE_AUTHEFT) && isSystematicReqOutStanding(nbaTrans))) {
				outStandingOne = outStandingOne - nbaTrans.getNbaLob().getCheckAmount();
				break;
			}
		}
		if (outStandingOne > txLifePDISValue) {
			validatingAmt = txLifePDISValue;
		} else {
			validatingAmt = outStandingOne;
		}
		//End APSL2735
		if (validatingAmt != Double.NaN && validatingAmt > 0 && getWork().getNbaLob().getCheckAmount() > 0) { //End ALII1956,APSL2735
			Map deOink = new HashMap();
			deOink.put("A_NBAMONEY", VPMS_NBAMONEY);
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOink);
			NbaTransaction nbaTransaction = getNbaCase().addTransaction(provider.getWorkType(), provider.getInitialStatus());
			nbaTransaction.getNbaLob().setWorkSubType((int)NbaOliConstants.OLI_WORKSUBTYPE_ACH);//APSL3836
			addNewCwaCheckSource(nbaTransaction);
			getNbaCase().setUpdate();
			updateWork(getUser(), getNbaCase());
		}
	}
	
	
	
	/**
	 * Create new NBCWACHECK source
	 * @param work an NbaDst work item to which the new source will be added
	 */
	//APSL2735 New Method
	protected void addNewCwaCheckSource(NbaTransaction nbaTransaction) throws NbaBaseException {
		
		WorkItemSource newWorkItemSource = new WorkItemSource();
		newWorkItemSource.setCreate("Y");
		newWorkItemSource.setRelate("Y");
		newWorkItemSource.setLobData(new ArrayList());
		newWorkItemSource.setBusinessArea(A_BA_NBA);
		newWorkItemSource.setSourceType(NbaConstants.A_ST_CWA_CHECK);
		newWorkItemSource.setCreateStation(NbaConstants.SCAN_STATION_EAPPACH);
		
		newWorkItemSource.setSize(0); 
		newWorkItemSource.setPages(1);
		if (getNbaTxLife().getPolicy() != null) {
			PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
			if (policyExtension != null) {
				LobData newLob = new LobData();
				newLob.setDataName(NbaLob.A_LOB_DISTRIBUTION_CHANNEL);
				newLob.setDataValue(Long.toString(getNbaCase().getNbaLob().getDistChannel()));
				newWorkItemSource.getLobData().add(newLob);
			}
		}

		LobData newLob2 = new LobData();
		newLob2.setDataName(NbaLob.A_LOB_PORTAL_CREATED_INDICATOR);
		newLob2.setDataValue(String.valueOf(NbaConstants.TRUE));
		newWorkItemSource.getLobData().add(newLob2);
		
		if (getNbaTxLife().getPolicy() != null) {
			LobData newLob3 = new LobData();
			newLob3.setDataName(NbaLob.A_LOB_POLICY_NUMBER);
			newLob3.setDataValue(getNbaTxLife().getPolicy().getPolNumber());
			newWorkItemSource.getLobData().add(newLob3);
		}
		
		LobData newLob4 = new LobData();
		newLob4.setDataName(NbaLob.A_LOB_PAYMENT_MONEY_SOURCE);
		newLob4.setDataValue(String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT));
		newWorkItemSource.getLobData().add(newLob4);
		
		newWorkItemSource.setFormat("T");
		newWorkItemSource.setSize(0);
		newWorkItemSource.setPages(1);
		newWorkItemSource.setText(NbaUtils.getGUID());
		newWorkItemSource.setFileName(null);
		newWorkItemSource.setSourceStream(NbaBase64.encodeString("CWA For ACH Payment Created"));
		
		NbaSource cwaSource = new NbaSource(newWorkItemSource);
		populateCWAWorkItemSource(cwaSource.getNbaLob());
		
		nbaTransaction.addNbaSource(new NbaSource(newWorkItemSource));
	}
	
	
	/**
	 * Method Returns true if System-Matic Form requirement received or in end queue or not present on the case;
	 * Returns false if Service Result is outstanding in underwriter CM/Post issue CM queue
	 * Returns returns false if any outstanding System-Matic Form requirement present on the case. 
	 * @return boolean indicating if any outstanding systematic requirement present or not. 
	 * @exception NbaBaseException
	 * */
	//ALII2015 Method Name Changed
	protected boolean isOutStandingFailureReceived() throws NbaBaseException {
		NbaDst parentCase = getNbaCase();//APSL2735
		Iterator transactionItr = parentCase.getNbaTransactions().listIterator();
		NbaTransaction sysTrans;
		boolean received = true;
		while (transactionItr.hasNext()) {
			sysTrans = (NbaTransaction) transactionItr.next();
			NbaLob lob = sysTrans.getNbaLob();
			if ((sysTrans.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REQUIREMENT)) && (lob.getReqType() == NbaOliConstants.OLI_REQCODE_AUTHEFT) && (String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT)).equalsIgnoreCase(lob.getPaymentMoneySource())) {
				received = false;
				if (lob.getReqStatus().equalsIgnoreCase(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED))
						&& (NbaConstants.ACH_UPDATE_DATA.equalsIgnoreCase(String.valueOf(lob.getACHAction())) || sysTrans.isInEndQueue())) {
					received = true;
				}				
			}else if(sysTrans.getNbaLob().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_SERVICE_RESULT) && isServiceResultOutstanding(sysTrans)){//ALII2015
				received = false;
			}
		}
		return received;
	}
	
	/**
	 * Method suspends the PDR requirement.
	 * @exception NbaBaseException
	 * */
// Begin APSL3351
	protected void suspendPaymentDraft(int days,String reason) throws NbaBaseException {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, days);
		suspendWorkItem(reason, cal.getTime());
	}
// End APSL3351
	
	/**
	 * Method un-suspends the PDR requirement, if there is any suspended PDR present on the case with status - ELPYRETRGD.
	 * @exception NbaBaseException
	 * */
	protected void unsuspendPDR() throws NbaBaseException {
			//Deleted Code ALII2015
			NbaDst parentCase = getNbaCase();//APSL2735
			Iterator transactionItr = parentCase.getNbaTransactions().listIterator();
			NbaTransaction pdrTrans;
			// Loop over transactions. If a PDR transaction was suspended, unsuspend it;
			while (transactionItr.hasNext()) {
				pdrTrans = (NbaTransaction) transactionItr.next();
				if (pdrTrans != null && NbaConstants.A_WT_REQUIREMENT.equalsIgnoreCase(pdrTrans.getWorkType())
						&& NbaOliConstants.OLI_REQCODE_POLDELRECEIPT == pdrTrans.getNbaLob().getReqType()) {
					if (pdrTrans.isSuspended() && NbaConstants.A_STATUS_ELECTRONIC_PAYMENT_RETRIGERRED.equalsIgnoreCase(pdrTrans.getNbaLob().getStatus())) {
						NbaSuspendVO suspendVO = new NbaSuspendVO();
						suspendVO.setTransactionID(pdrTrans.getID());
						unsuspendWork(suspendVO);
					}
				}
			}
	}

	/**
	 * Route all PDR requirements
	 * @exception NbaBaseException
	 * */
	//APSL3836 New method
	//NBLXA-1789 call removed
/*	protected void selectivelyRouteAllPDRs() throws NbaBaseException {
		NbaTransaction pdrTrans;
		List allPDRs = new ArrayList();
		NbaTransaction recentPDR = null;
		Date recentDate = null;
		NbaDst parentCase = getNbaCase(); 
		Iterator transactionItr = parentCase.getNbaTransactions().listIterator();
		while (transactionItr.hasNext()) {
			pdrTrans = (NbaTransaction) transactionItr.next();
			if (pdrTrans != null && NbaConstants.A_WT_REQUIREMENT.equalsIgnoreCase(pdrTrans.getWorkType())
					&& NbaOliConstants.OLI_REQCODE_POLDELRECEIPT == pdrTrans.getNbaLob().getReqType()) {
				allPDRs.add(pdrTrans);
				Date pdrDate = NbaUtils.getDateFromStringInAWDFormat(pdrTrans.getNbaLob().getCreateDate());
				if (recentDate == null || recentDate.before(pdrDate)) {
					recentDate = pdrDate;
					recentPDR = pdrTrans;
				} 
			}
		}
		
		Iterator pdrItr = allPDRs.listIterator();
		NbaTransaction pdrToRoute;
		boolean lastPDRFlag;
		while (pdrItr.hasNext()) {
			pdrToRoute = (NbaTransaction) pdrItr.next();
			lastPDRFlag = (recentPDR.getID() == pdrToRoute.getID());
			routePDR(pdrToRoute, lastPDRFlag);
		} 
	}*/
	/**
	 * Method routes to all the PDR requirements to their respective next queue
	 * @exception NbaBaseException
	 * */
	//APSL3836 New method
	protected void routePDR(NbaTransaction pdrToRoute, boolean lastPDRFlag) throws NbaBaseException {
		if ((!lastPDRFlag && !pdrToRoute.getNbaLob().getQueue().equalsIgnoreCase("END"))
				|| (lastPDRFlag && pdrToRoute.getNbaLob().getQueue().equalsIgnoreCase("END"))) {
			HashMap deOink = new HashMap();
			deOink.put("A_AliveLastPDRForACH", lastPDRFlag ? "true" : "false");
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(pdrToRoute.getNbaLob());
			oinkData.setContractSource(getNbaTxLife());
			NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getDataFromVpms(NbaVpmsAdaptor.AUTO_PROCESS_STATUS,
					NbaVpmsAdaptor.EP_WORKITEM_STATUSES, oinkData, deOink, null));
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(pdrToRoute.getID(), false);
			retOpt.setLockWorkItem();
			NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
			aWorkItem.setStatus(statusProvider.getPassStatus());
			setRouteReason(aWorkItem, statusProvider.getPassStatus());
			aWorkItem.setUpdate();
			//Begin APSL4060
			if(lastPDRFlag){
				RequirementInfo pdrInfo = nbaTxLife.getRequirementInfo(aWorkItem.getNbaLob().getReqUniqueID());
				if(pdrInfo!=null){
					RequirementInfoExtension pdrReqInfoExt = pdrInfo.getOLifEExtensionAt(0).getRequirementInfoExtension();
					if(pdrReqInfoExt!=null){
						pdrReqInfoExt.deleteReviewedInd();
						pdrReqInfoExt.deleteReviewID();
						pdrReqInfoExt.deleteReviewDate();
						pdrReqInfoExt.setActionUpdate();
					}
				}
				
			}
			if(!lastPDRFlag){
				RequirementInfo pdrInfo = nbaTxLife.getRequirementInfo(aWorkItem.getNbaLob().getReqUniqueID());
				pdrInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_WAIVED);
				if(pdrInfo.hasReceivedDate()){
					pdrInfo.deleteReceivedDate();
				}
				RequirementInfoExtension pdrReqInfoExt = pdrInfo.getOLifEExtensionAt(0).getRequirementInfoExtension();
				if(pdrReqInfoExt!=null){
					pdrReqInfoExt.deleteReviewedInd();
					if(pdrReqInfoExt.hasReviewID()){
						pdrReqInfoExt.deleteReviewID();
					}
					if(pdrReqInfoExt.hasReviewDate()){
						pdrReqInfoExt.deleteReviewDate();
					}
					pdrReqInfoExt.setActionUpdate();
				}
				pdrInfo.setActionUpdate();
			}
			doContractUpdate(nbaTxLife);
			//End APSL4060
			update(aWorkItem);
			unlockWork(aWorkItem);
			if(lastPDRFlag){
				addComment("ACH updated, payment draft needed");
			}
		} 
	}
		
		
	/**
	 * Method calculates number of days to suspend the PDR requirement.
	 * @return  suspendDays
	 * 
	 * */
	private int getPDRSuspendDays() {
		int suspendDays = 1;
		List reqInfoList = getNbaTxLife().getPolicy().getRequirementInfo();
		for (int i = 0; i < reqInfoList.size(); i++) {
			RequirementInfo reqInfo = (RequirementInfo) reqInfoList.get(i);
			if (reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT) {
				RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
				if (reqInfoExtn != null && reqInfoExtn.hasFollowUpFreq()) {
					suspendDays = reqInfoExtn.getFollowUpFreq();
				}
			}
		}
		return suspendDays;
	}
	
	//New Methods APSL2735
	private boolean isSystematicReqOutStanding(NbaTransaction nbaTrans) {
		boolean flag = false;
		if (nbaTrans.getNbaLob().getStatus().equalsIgnoreCase("SYSREQRCD2")
				|| nbaTrans.getNbaLob().getStatus().equalsIgnoreCase("SYSREQRCD1")) {
			flag = true;
		}
		return flag;
	}

	private boolean isServiceResultOutstanding(NbaTransaction nbaTrans) {
		boolean flag = false;
		if (NbaUtils.suspenseTeam.contains(nbaTrans.getNbaLob().getStatus())) { //NBLXA-2535[NBLXA-2328]
			flag = true;
		}
		return flag;
	}
	
	// Begin APSL3351
	private boolean isPaymentDraftNeededSuspension() throws NbaBaseException {
		String cwor = getWork().getNbaLob().getPaymentMoneySource();
		String msg=""; //APSL4766
		boolean flag = false; //APSL4766
		int suspendDays = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.SUSPEND_ONE_DAY));
		if ((NbaUtils.isMsgCodeExists(getNbaTxLife(), 5905))
		&& ((getWork().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CWA) && cwor.equals(NbaConstants.CWOR)) || (NbaConstants.A_WT_REQUIREMENT
		.equalsIgnoreCase(getWork().getWorkType()) && getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT && NbaUtils.getAppInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo()).getInitialPremiumPaymentForm() == NbaOliConstants.OLI_PAYMETH_ETRANS))) {
			String cvMessage = NbaUtils.getCVMessage(getNbaTxLife(), 5905);//NBLXA-1791
			msg=cvMessage; //APSL4766,NBLXA-1791
			flag=true; //APSL4766
		}
		// End APSL3351
		// begin APSL4766
		if (NbaUtils.isMsgCodePendingToOverride(getNbaTxLife(), 1794, 1)
				&& (getWork().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CWA))) {
			String cvMessage = NbaUtils.getCVMessage(getNbaTxLife(), 1794);
					if(flag == true){
						msg = msg+", "+cvMessage+", ACH information is missing."; //NBLXA-1791
					} else {
						msg = cvMessage+ ", ACH information is missing."; //NBLXA-1791
					}
					flag=true;
		}
		if(flag){
			//APSL5164 begin NBLXA-1256 removed check for variable product.
				HashMap deOink = new HashMap();
				deOink.put("A_ProcessId", "CIPESUSPENDWORK");
				NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
				oinkData.setContractSource(getNbaTxLife());
				NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getDataFromVpms(NbaVpmsAdaptor.AUTO_PROCESS_STATUS,
						NbaVpmsAdaptor.EP_WORKITEM_STATUSES, oinkData, deOink, null));
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", statusProvider.getPassStatus()));// modified for apsl5164
				changeStatus(statusProvider.getPassStatus());
				addComment(msg); 	//NBLXA-1791
				
				if (getWork().isLocked(getUser().getUserID())) {
					doUpdateWorkItem();
				}
				
			// apsl5164 end
			return true;
		}
		// end APSL4766  
		return false; 
	}
}
