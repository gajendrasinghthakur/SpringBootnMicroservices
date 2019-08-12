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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.RetrieveWorkItemsRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.dataobject.nba.cash.CheckAllocation;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractsWireTransferData;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.tableaccess.NbaTable;
import com.csc.fsg.nba.tableaccess.NbaWireTransferData;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaVpmsRequestVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vpms.CopyLobsTaskConstants;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * NbaProcMoneyDetermination is the class that processes nbAccelerator cases found on the AWD money determniation queue (NBMNYDTM). The purpose of
 * this step in the workflow is to determine if money is nbA money for routing, assign a bundle number to checks, add wire transfers to the wire
 * transfer datbase for reconciliation, associate one check with multiple applications when send in and indexed for multiple contracts, and add checks
 * to the cashiering database tables for tracking and deposits.
 * <p>
 * The NbaProcMoneyDetermination class extends the NbaAutomatedProcess class. Although this class may be instantiated by any module, the NBA polling
 * class will be the primary creator of objects of this type.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>NBA009</td>
 * <td>Version 2</td>
 * <td>Cashiering</td>
 * </tr>
 * <tr>
 * <td>SPR1171</td>
 * <td>Version 2</td>
 * <td>Secondary Contracts for Wire Transfers</td>
 * </tr>
 * <tr>
 * <td>NBA050</td>
 * <td>Version 3</td>
 * <td>Pending Database</td>
 * </tr>
 * <tr>
 * <td>NBA033</td>
 * <td>Version 3</td>
 * <td>Companion Case and HTML Indexing Views</td>
 * </tr>
 * <tr>
 * <td>NBA093</td>
 * <td>Version 3</td>
 * <td>Upgrade to ACORD 2.8</td>
 * </tr>
 * <tr>
 * <td>SPR1359</td>
 * <td>Version 3</td>
 * <td>Automated processes stop poller when unable to lock supplementary work items</td>
 * </tr>
 * </td>
 * </tr>
 * <tr>
 * <td>NBA068</td>
 * <td>Version 3</td>
 * <td>Inforce Payment</td>
 * </tr>
 * </td>
 * </tr>
 * <tr>
 * <td>NBA086</td>
 * <td>Version 2</td>
 * <td>Performance Testing and Tuning</td>
 * </tr>
 * <tr>
 * <td>SPR1018</td>
 * <td>Version 3</td>
 * <td>General Cleanup</td>
 * </tr>
 * <tr>
 * <td>SPR1851</td>
 * <td>Version 4</td>
 * <td>Locking Issues</td>
 * </tr>
 * <tr>
 * <td>NBA097</td>
 * <td>Version 4</td>
 * <td>Work Routing Reason Displayed</td>
 * </tr>
 * <tr>
 * <td>SPR1928</td>
 * <td>Version 4</td>
 * <td>Money Determination process throws error 'Unknown Error - CSHCREATED'</td>
 * </tr>
 * <tr>
 * <td>NBA095</td>
 * <td>Version 4</td>
 * <td>Queues Accept Any Work Type</td>
 * </tr>
 * <tr>
 * <td>SPR1844</td>
 * <td>Version 4</td>
 * <td>Non-nbA money not determined correctly</td>
 * </tr>
 * <tr>
 * <td>SPR1841</td>
 * <td>Version 4</td>
 * <td>Wrong Entry Date and no As of Date in CWA and in reversal of CWA.</td>
 * </tr>
 * <tr>
 * <td>NBA077</td>
 * <td>Version 4</td>
 * <td>Reissues and Complex Change etc.</td>
 * </tr>
 * <tr>
 * <td>NBA115</td>
 * <td>Version 5</td>
 * <td>Credit Card payment and authorization</td>
 * </tr>
 * <tr>
 * <td>SPR2300</td>
 * <td>Version 5</td>
 * <td>"DepositEligibility" tage in configuration file changed to "apply"</td>
 * </tr>
 * <tr>
 * <td>SPR2775</td>
 * <td>Version 5</td>
 * <td>Money Determination generates Out of Balance error when NBPAYMENT indexed with more than 5 contracts</td>
 * </tr>
 * <tr>
 * <td>SPR2238</td>
 * <td>Version 6</td>
 * <td>Money coming into nbA while contract is undergoing complex change is not treated as inforce payment</td>
 * </tr>
 * <tr>
 * <td>NBA130</td>
 * <td>Version 6</td>
 * <td>Requirements Reinsurance Project</td>
 * </tr>
 * <tr>
 * <td>SPR3169</td>
 * <td>Version 6</td>
 * <td>Error during creation of inforce payment Transaction</td>
 * </tr>
 * <tr>
 * <td>NBA123</td>
 * <td>Version 6</td>
 * <td>Administrator Console Rewrite</td>
 * </tr>
 * <tr>
 * <td>NBA153</td>
 * <td>Version 6</td>
 * <td>Companion Case Rewrite</td>
 * </tr>
 * <tr>
 * <td>SPR2670</td>
 * <td>Version 6</td>
 * <td>Correction needed in Companion Case VP/MS model</td>
 * </tr>
 * <tr>
 * <td>NBA213</td>
 * <td>Version 7</td>
 * <td>Unified User Interface</td>
 * </tr>
 * <tr>
 * <td>SPR3299</td>
 * <td>Version 7</td>
 * <td>In New Install with Convergence Configuration Money Determination errors CWA when NBCASHIER Work Item Not in CSHCREATED status</td>
 * </tr>
 * <tr>
 * <td>NBA173</td>
 * <td>Version 7</td>
 * <td>Indexing UI Rewrite Project</td>
 * </tr>
 * <tr>
 * <td>SPR2618</td>
 * <td>Version 7</td>
 * <td>Apostrophies are not supported</td>
 * </tr>
 * <tr>
 * <td>NBA208-32</td>
 * <td>Version 7</td>
 * <td>Workflow VO Convergence</td>
 * </tr>
 * <tr>
 * <td>SPR3431</td>
 * <td>Version 7</td>
 * <td>Financial Activity Type of Payment (FinActivityType) Not Set on XML508 Transaction When Check is Split Between Contracts and the Inforce
 * Payment is Second</td>
 * </tr>
 * <tr>
 * <td>NBA182</td>
 * <td>Version 7</td>
 * <td>Cashiering Rewrite</td>
 * </tr>
 * <tr>
 * <td>SPR3562</td>
 * <td>Version 8</td>
 * <td>Premium Amounts Larger than Seven Whole Numbers and Two Decimals Are Not Handled Correctly</td>
 * </tr>
 * <tr>
 * <td>NBA221</td>
 * <td>Version 8</td>
 * <td>nbA Payment Origination Project</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.20</td>
 * <td>AXA Life Phase 1</td>
 * <td>Workflow</td>
 * </tr>
 * <tr>
 * <td>NBA228</td>
 * <td>Version 8</td>
 * <td>Cash Management</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.43</td>
 * <td>AXA Life Phase 1</td>
 * <td>Money UnderWriting</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.23</td>
 * <td>AXA Life Phase 1</td>
 * <td>Accounting Interface</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.12</td>
 * <td>AXA Life Phase 1</td>
 * <td>Cash Management</td>
 * </tr>
 * <tr>
 * <td>ALS3046</td>
 * <td>AXA Life Phase 1</td>
 * <td>non-nbA NBPAYMENT work item indexed from the wholesale index queue flowed to the N2NBCMR queue instead of N2NBCMW queue</td>
 * </tr>
 * <tr>
 * <td>SR615900</td>
 * <td>Discretionary</td>
 * <td>Prevent Checks From Being Deposited When CWA Not Applied</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaProcMoneyDetermination extends NbaAutomatedProcess {
	/** The technical Key for the check */
	protected long technicalKey;

	/** The bundle ID in which the check was included */
	protected String bundleID;

	//SPR2238 code deleted
	/** Object for the data in the NBA_CONTRACTS_CHECKS table */
	protected NbaContractCheckData nbaContractCheckDataTable;

	/** OnikDataAccess object which allows information to be retrieved from the NbaTXLife or NbaLob objects */
	protected NbaOinkDataAccess oinkDataAccess = null;

	/** String for Nba Money */
	protected static final String VPMS_NBAMONEY = "1";

	/** String for Non Nba Money */
	protected static final String VPMS_NON_NBAMONEY = "0";

	 /* String representation of a boolean true value */
    protected static final String TRUE = Integer.toString(NbaConstants.TRUE);  //NBA331.1, APSL5055
    
	/** Number of companion cases */
	protected int numCompanionCases = 0; //NBA033

	/** Boolean variable representing whether the work is suspended */
	protected boolean workSuspended = false; //NBA033

	protected String moneyType; //SPR2238

	protected String caseID; //SPR2238

	protected NbaDst parentCase; //SPR2238

	NbaDst sourceParentCase = null; //ALS5583

	protected boolean case1035; //CR1346004

	boolean isInvalidPolicy = false; //QC#9215/APSL2255
	
	//begin NBA331.1
		private NbaSource source = null;
		private NbaLob sourceLob = null;
		private int numberOfContracts = -1;
		private List<CheckAllocation> checkAllocations = null;
	//end NBA331.1

	/**
	 * NbaProcProviderEmsi constructor comment.
	 */
	//	NBA123 removed "throws NbaBaseException"
	public NbaProcMoneyDetermination() {
		super();
	}

	//NBA182 code deleted

	/**
	 * Te method determines if all the payments are non nbA there are multiple contracts. If yes, it creates new work items for all the non nbA
	 * payments. Otherwise, it creates generic payment work items. and sets appropriate status.
	 * 
	 * @param nonNbaPayments
	 *            Total number of non NbaPayments
	 * @param nbaPayment
	 *            boolean value representing true or false
	 * @throws NbaBaseException
	 */
	protected void createNonNbaPaymentWorkItems(int[] nonNbaPayments, boolean nbaPayment) throws NbaBaseException {
		//NBA331.1, APSL5055 code deleted
		//copy the lob fields from source to Generic Payment work item
		getWork().getNbaLob().setBundleNumber(getSourceLob().getBundleNumber()); // NBA331.1, APSL5055
		//if all the payments are non nbA
		if (nbaPayment == false) {
			//begin NBA068
			//If multiple contracts....create new work items for all the non nbA payments and set appropriate status
			if (getNumberOfContracts() > 1) {
				createNewInforcePaymentItems(getNbaSource()); // NBA331.1, APSL5055
			} else { //begin SPR2300
				//update the current payment workitem with source LOBs
				NbaLob workItemLob = getWork().getNbaLob();
				NbaLob sourceLob = getSourceLob(); // NBA331.1, APSL5055
				workItemLob.setCreateStation(sourceLob.getCreateStation()); //NBA228
				//Code Refractored
				List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2MNYDTM_CREATE_NONNBAPAYMENT_TASK);
				sourceLob.copyLOBsTo(workItemLob, lobList);
				//set the status on the parent level
				//AXAL3.7.01
				String manualPaymentInd = getSourceLob().getInforcePaymentManInd(); //NBA182
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getNonNbaStatus(manualPaymentInd))); //NBA182
				//AXAL3.7.01
			} //end SPR2300
			//end NBA068
			addComment("All the payments are non nbA"); //NBA093

		} else { //Create Generic payment work items

			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, getWork(), getNbaSource().getSource().getSourceType(),
					VPMS_NON_NBAMONEY);
			NbaLob sourceLob = getSourceLob(); //NBA182, NBA331.1, APSL5055
			NbaLob nbaLob = null; //NBA182
			for (int i = 0; i < nonNbaPayments.length; i++) {
				if (nonNbaPayments[i] != 0) {
					// If there's only one contrat associated with check, the user only has to enter a check amount and
					// does not have to enter an amount to be applied.
					// Money determination should set CWAM equal to CKAM if not present.
					CheckAllocation allocation = getCheckAllocation(nonNbaPayments[i]);  //NBA331.1 APSL5055
					double amount;
					//begin NBA182
					if (getNumberOfContracts() == 1 && allocation.getCwaAmount() == null) {
						allocation.setCwaAmount(sourceLob.getCheckAmount());
					} 
					nbaLob = new NbaLob();
					//end NBA182
					//set portal indicator to false
					nbaLob.setPortalCreated(false);

					//copy the lob fields from source to Generic Payment work item
					//begin NBA182
					nbaLob.setPolicyNumber(allocation.getPolicyNumber());
					nbaLob.setCompany(allocation.getCompany());
					nbaLob.setCwaAmount(allocation.getCwaAmount());
					if (allocation.getCostBasis() != null) {
						nbaLob.setCostBasis(allocation.getCostBasis());
					}
					nbaLob.setExchangeReplace(sourceLob.getExchangeReplaceAt(nonNbaPayments[i]));
					//begin SPR3431
					if (allocation.getInforcePaymentDate() != null) {
						nbaLob.setInforcePaymentDate(allocation.getInforcePaymentDate());
					}
					if (allocation.isInforcePaymentManual()) {
						nbaLob.setInforcePaymentManInd(TRUE);
					}
					if (allocation.getInforcePaymentType() != null) {
						nbaLob.setInforcePaymentType(allocation.getInforcePaymentType().intValue());
					}
					//end SPR3431
					//end NBA182
					
					if(allocation.getPaymentMoneySource() != null) {
						nbaLob.setPaymentMoneySource(allocation.getPaymentMoneySource().toString()); //NBA221
					}
					
					String caseID = getCaseIDForPayment(nbaLob.getPolicyNumber(), nbaLob.getCompany());
					NbaDst nbaCase = retrieveCase(caseID);
					nbaLob.setCaseManagerQueue(nbaCase.getNbaLob().getCaseManagerQueue()); //NBLXA-2548[NBLXA-2328]
					
					nbaLob.setCreateStation(sourceLob.getCreateStation()); //NBA228
					//Code Refractored
					List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2MNYDTM_CREATE_GENERICPAYMENT_TASK);
					sourceLob.copyLOBsTo(nbaLob, lobList);
					//NBA213 deleted code
					//ALS4896 code deleted
					//Create the work item with the same status as the current work item to prevent it from being locked by another process.
					NbaDst nbaDst = createCase(getUser(), NbaConstants.A_BA_NBA, provider.getWorkType(), provider.getInitialStatus(), nbaLob); //SPR3169,
																																			   // NBA213,
																																			   // NBA182/ALS4896

					nbaDst.addNbaSource(getNbaSource()); //NBA331.1 APSL5055
					nbaDst.increasePriority(provider.getWIAction(), provider.getWIPriority());
					//begin SPR3169
					//Set the real status
					//AXAL3.7.20 code deleted. Always route to status provided by provider.InitialStatus() //NBA208-32
					nbaDst.getTransaction().setLockStatus(getUser().getUserID()); //NBA213
					//end SPR3169
					//AXAL3.7.01 code deleted
					updateWork(getUser(), nbaDst); //NBA213
					unlockWork(getUser(), nbaDst); //NBA213
					//NBA213 deleted code
				}
			}
		}

	}

	/**
	 * Determine the status of a non nbA money work item based on the manual payment indicator
	 * 
	 * @param String
	 *            manual payment indicator
	 */
	//NBA182 New Method
	protected String getNonNbaStatus(String manualInd) {
		if (manualInd == null) {
			return getFailStatus();
		}
		if ("0".equals(manualInd)) { //automatic payment
			return getFailStatus();
		}
		return getFailStatus(); //manual payment //AXAL3.7.12
	}

	/**
	 * This abstract method must be implemented by each subclass in order to execute the automated process. NBA115 introduced 'no source' processing,
	 * where credit card transactions that are recieved via the portal or over the telephone will not have scanned sources attached to the work items.
	 * 
	 * @param user
	 *            the user/process for whom the process is being executed
	 * @param work
	 *            a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws com.csc.fsg.nba.exception.NbaBaseException {

		// NBA095 code deleted
		if (!initialize(user, work)) {
			
			return getResult(); //NBA050
		}
		
		boolean noSource = false;//NBA115
		//NBA213 deleted code
		//	retrieve the complete work item
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestSources();
		retOpt.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retOpt)); //NBA213
		//BEGIN: APSL4844
		String status = checkForNTOCases(getWork());
		if(!NbaUtils.isBlankOrNull(status) && 
				!"-".equalsIgnoreCase(status)) {
			return getResult();
		}
		//END: APSL4844
		
		//begin SPR1018
		NbaSource nbaSource = null;

		if (getWork().getNbaSources().size() > 0) {
			 setNbaSource((NbaSource) getWork().getNbaSources().get(0));
		} else {
			//Begin NBA115
			String ccTransactionId = getWork().getNbaLob().getCCTransactionId();
			if (ccTransactionId != null && ccTransactionId.length() > 0) {
				noSource = true; //a valid credit card transaction without source
			} else {
				addComment("Required Source information missing.");
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getOtherStatus()));
				changeStatus(getResult().getStatus());
				doUpdateWorkItem();
				return getResult();
			}
			//End NBA115
		}
		//end SPR1018

		try {
			//begin NBA115
			if (noSource) {
				processNoSourcePayment();
			} else {
				String sourcetype = getNbaSource().getSourceType();
				if (sourcetype.equalsIgnoreCase(NbaConstants.A_ST_CREDIT_CARD)) {
					processCreditCardPayment();
					//If source type is XML508, it is a wire transfer
				} else if (sourcetype.equalsIgnoreCase(NbaConstants.A_ST_XML508)) {
					//Process Wire transfer
					processWireTransfer();
				} else {
					//Process Check Payment
					processCheckPayment();
				}
			}
			//end NBA115
		} catch (NbaDataAccessException dae) {
			addComment("SQL Error " + dae.getFormattedMessage()); //SPR3562
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getSqlErrorStatus()));
		}

		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		changeStatus(getResult().getStatus());
		if (!workSuspended) { //NBA033
			doUpdateWorkItem();
		} //NBA033

		return getResult();
		//NBA213 deleted code

	}

	/**
	 * Returns the Bundle ID
	 * 
	 * @return Bundle ID
	 */
	protected java.lang.String getBundleID() {
		return bundleID;
	}

	/**
	 * Returns the Case ID for which the payment has to be made
	 * 
	 * @param contractNumber
	 *            Contract Number
	 * @param companyCode
	 *            Company Code
	 * @return caseID
	 * @throws NbaBaseException
	 */
	protected String getCaseIDForPayment(String contractNumber, String companyCode) throws NbaBaseException {
		//begin SPR2238
		NbaSearchResultVO nbaSearchResultVO = getNbaSearchResultVO(contractNumber, companyCode);
		if (nbaSearchResultVO != null) {
			return nbaSearchResultVO.getWorkItemID();
		}
		//end SPR2238
		return null;

	}

	/**
	 * Returns the technical key
	 * 
	 * @return technicalKey
	 */
	protected long getTechnicalKey() {
		return technicalKey;
	}

	/**
	 * This method returns the numbers of contracts associated with the check
	 * 
	 * @return count
	 */
/*	protected int numberOfContracts() {
		int count = 0;
		NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);
		//begin SPR2775
		int i = 1;
		while (nbaSource.getNbaLob().getPolicyNumberAt(i) != null && nbaSource.getNbaLob().getPolicyNumberAt(i).length() > 0) {
			count = count + 1;
			i++;
		}
		//end SPR2775
		//begin NBA033
		if (numCompanionCases > 0) {
			count += numCompanionCases - 1;
		}
		//end NBA033
		return count;
	}*/
	
	/**
	 * This method returns the numbers of contracts associated with the check
	 * @return count
	 */
	//NBA331.1, APSL5055 New Method
	protected int getNumberOfContracts() {
		if (numberOfContracts == -1) {
			numberOfContracts = 0;
			for (CheckAllocation allocation : getCheckAllocations()) {
				if (allocation.getPolicyNumber() != null && !allocation.getPolicyNumber().isEmpty()) {
					numberOfContracts++;
				}
			}
			if (numCompanionCases > 0) {
				numberOfContracts += numCompanionCases - 1;
			}
		}
		return numberOfContracts;
	}

	/**
	 * This methods processes the incoming check. It first updates the NBA_CHECKS table and then verifies the check amount It proceses Nba Money and
	 * non Nba Money and created cashiering work item if needed
	 * 
	 * @exception NbaBaseException
	 */
	protected void processCheckPayment() throws NbaBaseException {
		//NBA182 code deleted
		NbaCheckData nbaCheckDataTable = new NbaCheckData();
		NbaSource nbaSource = getNbaSource();
		NbaLob sourceLOB = getSourceLob(); //SPR2238
		//SPR2238 code deleted
		//begin NBA331.1, APSL5055
		double totalAmt = 0; //NBA182
	    List<CheckAllocation> checkAllocations = retrieveCheckAllocations();
	    int cashMultipleCount = checkAllocations.size();
	    for (CheckAllocation allocation : checkAllocations) {
	    	if (allocation.getPolicyNumber() != null && allocation.getCompany() != null) {
	    		if (allocation.getCwaAmount() != null) {
	    			totalAmt += allocation.getCwaAmount();
	    		}
	    	}
	    }
	  //end NBA331.1, APSL5055

		if (!performCheckValidations(nbaSource, sourceLOB)) { //AXAL3.7.12
			return; //AXAL3.7.12
		}

		//Begin CR1346004
		boolean isNbaMoney = VPMS_NBAMONEY.equals(getMoneyTypeForNextContact(sourceLOB.getPolicyNumber(), null, sourceLOB.getInforcePaymentManInd())); //SPR2238,
																																					   // NBA182//ALS5026 // Company name not to be used for search 
		//Begin QC13983/APSL3924																																		   
		if (getParentCase() != null) {
			NbaTXLife contract = doHoldingInquiry(getParentCase());
			if (NbaUtils.is1035Case(contract)) {
				setCase1035(true);
			}
		}	
		//End QC13983/APSL3924
		String pendPaymentTypeStr = Integer.toString(sourceLOB.getPendPaymentType());
		if (!isCase1035() && NbaUtils.isPaymentType1035(pendPaymentTypeStr)) {
			addComment("1035 Payment type is only allowed on 1035 Exchange Case- Change the payment type");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getOtherStatus()));
			return;
		}// End CR1346004

		insertCheckDataTable(nbaCheckDataTable, nbaSource, sourceLOB); //NBA182

		if (sourceLOB.getBundleNumber() == null || sourceLOB.getBundleNumber().length() == 0) { //SPR2238
			sourceLOB.setBundleNumber(getBundleID()); //SPR2238
			nbaSource.setUpdate();
		}
		//begin NBA033
		//NBA182 code deleted
		// SPR2238 code deleted
		NbaLob caseLob = null;
		//CR1346004 Code moved above
		if (isNbaMoney) { //SPR2238
			caseLob = getParentCase().getNbaLob(); //SPR2238
		}

		NbaCompanionCaseRules rules = null; //NBA182
		if (isNbaMoney && caseLob.getCwaSameSource()) { //Is a WI on a companion case that shares a source //SPR2238
			sourceParentCase = getParentCase(); //ALS5583
			rules = new NbaCompanionCaseRules(getUser(), getParentCase()); //SPR2238
			if (rules.isSuspendNeeded(NbaVpmsConstants.MONEY_DETERMINATION)) { //SPR2670
				suspendForCompanionCaseChecks(rules); //NBA182
				return;
				//SPR2238 code deleted
			}
			lockSourceCase(); //ALS5583
		}
		//end NBA033
		//Verify check amount
		//begin SPR2775

		
		//end SPR2775
		//begin NBA033
		/*  APSL5219 -- Split Check for Companion case -AMount addtion removed
		NbaCompanionCaseVO vo = null;
		if (isNbaMoney && caseLob.getCwaSameSource()) { //SPR2238
			for (Iterator iter = rules.getCompanionCases().iterator(); iter.hasNext();) {
				vo = (NbaCompanionCaseVO) iter.next();
				// begin NBA153
				// case isn't the one who's check amount has already been added above
				if (!vo.isCwaCheckShared()) {
					totalAmt += vo.getCwaAmount();
				}
				// end NBA153
			}
			numCompanionCases = rules.getCompanionCases().size(); //adds to the number of contracts
		}*/ // APSL519 END
		
		//end NBA033

		if (NbaUtils.isEqualTo2DecimalPlaces(sourceLOB.getCheckAmount(), totalAmt) != 0
				&& !(getNumberOfContracts() == 1 && sourceLOB.getCwaAmount() == 0)) { //SPR2238 ALS5707
			addComment("Amount out of balance");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getHostErrorStatus()));
			return;
		} //SPR2238
		NonNbaPayments nonNbaPayments = new NonNbaPayments(cashMultipleCount); //NBA182
		NbaContractCheckData nbaContractCheckDataTable = new NbaContractCheckData();
		//begin NBA182
		boolean processedNbaMoney = processCheckMoney(nonNbaPayments, nbaContractCheckDataTable); // NBA331.1, APSL5055
		processedNbaMoney = processCompanionCaseCheckMoney(rules, caseLob, isNbaMoney, nbaContractCheckDataTable, processedNbaMoney);
		//end NBA033
		if (!processedNbaMoney) {
			//Update Check table to set Include Indicator to true if there is no nba money
			NbaCheckData.updateIncludeInd(true, getBundleID(), sourceLOB.getCheckAmount(), sourceLOB.getCwaDate(), sourceLOB.getCheckNumber(),
					nbaSource.getSource().getCreateStation(), nbaSource.getCreateDate()); //SPR2238
		}
		if (nonNbaPayments.getCount() > 0) {
			//create non nbA work items
			createNonNbaPaymentWorkItems(nonNbaPayments.getIndexOfNbaNonPaymentArray(), processedNbaMoney); //NBA182
		}
		//begin ALS5583..Reset the CWSM on the primary companion case
		if (null != sourceParentCase) {
			//sourceParentCase = updateWork(getUser(), sourceParentCase); //update with new transaction/sources.
			sourceParentCase.getNbaLob().setCwaSameSource(false);
			sourceParentCase.setUpdate();
			sourceParentCase = updateWork(getUser(), sourceParentCase); //now update LOB on case.
			unlockWork(getUser(), sourceParentCase);
		}
		//end ALS5583
		//end NBA182
		//SPR2238 code deleted
	}

	/**
	 * process the Checks and determine if the any money is nbA money
	 * 
	 * @param sourceLOB
	 * @param cashMultipleCount
	 * @param nonNbaPaymentz
	 * @param nbaContractCheckDataTable
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA182 New Method, NBA331.1, APSL5055 Method signature changed.
	protected boolean processCheckMoney(NonNbaPayments nonNbaPayments,
			NbaContractCheckData nbaContractCheckDataTable) throws NbaBaseException {
		int counterForPrimaryInd = 0;
		String contractNumber;
		String companyCode;
		boolean processedNbaMoney = false;
		for (CheckAllocation allocation : getCheckAllocations()) {
	        contractNumber = allocation.getPolicyNumber();	//FNB011 NBA331.1
	        companyCode = allocation.getCompany();	//FNB011 NBA331.1
	        if (contractNumber != null && contractNumber.length() > 0 && companyCode != null && companyCode.length() > 0) {
	            counterForPrimaryInd = counterForPrimaryInd + 1;
	            if (VPMS_NBAMONEY.equals(getMoneyTypeForNextContract(contractNumber, companyCode, allocation.isInforcePaymentManual()))) {	//FNB011 NBA331.1
	                processNbaMoney(getCaseID(), allocation, (counterForPrimaryInd == 1), nbaContractCheckDataTable);  //NBA331.1
	                processedNbaMoney = true; //NBA182 set to true if any check is nbaMoney
	            } else {
	                processNonNbaMoney(allocation, (counterForPrimaryInd == 1), nbaContractCheckDataTable);  //NBA331.1
	                //Keep track of non Nba Payment Generic items to be created
	                //This is done because we do not need new generic payments work items
	                //if all the payments are generic payments
	                nonNbaPayments.addIndex(allocation.getSequence());  //NBA331.1
	            }
	        } else { //Come out of the loop once the contract number is not found on workValues
	            break;
	        }
	        
	    }
		return processedNbaMoney;
	}
	
	/**
	 * Determine if the payment is nbA or non-nbA money for the next contract.
	 * @param contractNumber
	 * @param companyCode
	 * @param manualInd
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA331.1, APSL5055 New Method
	protected String getMoneyTypeForNextContract(String contractNumber, String companyCode, boolean manualInd) throws NbaBaseException {
		setMoneyType(null);
		return getMoneyType(contractNumber, companyCode, manualInd);
	}

	/**
	 * Set the suspend status while waiting on companion cases
	 * 
	 * @param rules
	 * @throws NbaBaseException
	 */
	//NBA182 New Method
	private void suspendForCompanionCaseChecks(NbaCompanionCaseRules rules) throws NbaBaseException {
		if (rules.isSuspendDurationWithinLimits()) {
			addComment("Companion case. Waiting for other cases.");
			updateForSuspend(rules.getSuspendVO());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
			return;
		}
		addComment("Maximum suspend time exceeded");
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "error", "Error"));
		changeStatus(getFailStatus());
	}

	/**
	 * Process split money for companion cases
	 * 
	 * @param rules
	 * @param caseLob
	 * @param isNbaMoney
	 * @param nbaContractCheckDataTable
	 * @param processedNbaMoney
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA182 New Method
	private boolean processCompanionCaseCheckMoney(NbaCompanionCaseRules rules, NbaLob caseLob, boolean isNbaMoney,
			NbaContractCheckData nbaContractCheckDataTable, boolean processedNbaMoney) throws NbaBaseException {
		NbaCompanionCaseVO vo;
		if (isNbaMoney && caseLob.getCwaSameSource()) { //Is a WI on a companion case that shares a source
			String casePolicyNumber = caseLob.getPolicyNumber().trim();
			for (Iterator iter = rules.getCompanionCases().iterator(); iter.hasNext();) {
				vo = (NbaCompanionCaseVO) iter.next();
				if (!Double.isNaN(vo.getCwaAmount()) && vo.getCwaAmount() > 0 && !vo.getContractNumber().trim().equals(casePolicyNumber)) {
					processNbaMoney(vo, nbaContractCheckDataTable);
					processedNbaMoney = true; //NBA182 set to true if any source is nbaMoney
				}
			}
		}
		return processedNbaMoney;
	}

	/**
	 * Insert data into the CheckDataTable
	 * 
	 * @param nbaCheckDataTable
	 * @param nbaSource
	 * @param sourceLOB
	 * @throws NbaBaseException
	 * @throws NbaDataAccessException
	 */
	//NBA182 New Method
	protected void insertCheckDataTable(NbaCheckData nbaCheckDataTable, NbaSource nbaSource, NbaLob sourceLOB) throws NbaBaseException,
			NbaDataAccessException {
		//Check if the technical key already exists for this check
		NbaCheckData nbaTempCheckDataTable = nbaCheckDataTable.getCheckData(sourceLOB.getCheckAmount(), sourceLOB.getCwaDate(), sourceLOB
				.getCheckNumber(), nbaSource.getSource().getCreateStation(), nbaSource.getCreateDate());

		//if yes then dont insert it again
		if (nbaTempCheckDataTable != null) {
			setTechnicalKey(nbaTempCheckDataTable.getTechnicalKey());
			setBundleID(nbaTempCheckDataTable.getBundleID());
		} else {
			// APSL4590 Begin
			try {
				nbaCheckDataTable.startTransaction();
				String[] bundleIds = nbaCheckDataTable.getBundleID(sourceLOB.getCreateStation(), sourceLOB.getCompany());
				setBundleID(bundleIds[0]);
				if (bundleIds[1] != null) {
					updateCWABundleId(bundleIds[0], bundleIds[1], nbaCheckDataTable.selectUpdateBundle(bundleIds[1]));
					// Update the existing checks in bundle with new bundle Id
					nbaCheckDataTable.updateBundle(bundleIds[0], bundleIds[1]);
				}
			} catch (Exception se) {
				nbaCheckDataTable.rollbackTransaction();
				throw new NbaDataAccessException(NbaTable.SQL_ERROR, se);
			}
			nbaCheckDataTable.commitTransaction();
			// APSL4590 End
			//get the technical key
			setTechnicalKey(nbaCheckDataTable.getNextTechnicalKey());

			//Add update Check table
			nbaCheckDataTable.setTechnicalKey(getTechnicalKey());
			nbaCheckDataTable.setBundleID(getBundleID());
			if (NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN == getWork().getNbaLob().getInforcePaymentType()) {
				nbaCheckDataTable.setIncludeInd(true);
			} else {
				nbaCheckDataTable.setIncludeInd(false);
			}
			nbaCheckDataTable.setCheckAmount(sourceLOB.getCheckAmount());
			nbaCheckDataTable.setCheckDate(sourceLOB.getCwaDate());
			nbaCheckDataTable.setCheckNumber(sourceLOB.getCheckNumber());
			nbaCheckDataTable.setScanStationID(nbaSource.getSource().getCreateStation());
			nbaCheckDataTable.setSourceCreateTimeStamp(nbaSource.getCreateDate());
			nbaCheckDataTable.setCheckLastName(sourceLOB.getCheckLastName()); //NBA228
			nbaCheckDataTable.setPaymentForm(Long.parseLong(sourceLOB.getPaymentMoneySource())); //AXAL3.7.23
			nbaCheckDataTable.insert();
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
	//NBA182 New Method
	protected StringBuffer checkForMissingLOBs(NbaSource nbaSource, NbaLob sourceLOB) throws NbaBaseException {
		StringBuffer buf = new StringBuffer();
		String type = "Check";
		if (sourceLOB.getCheckAmount() <= 0) {
			appendToMissingDataBuffer(buf, type, "Check Amount");
		}
		if (sourceLOB.getCwaDate() == null) {
			appendToMissingDataBuffer(buf, type, "CWA Date");
		}
		if (sourceLOB.getCheckNumber() == null) {
			appendToMissingDataBuffer(buf, type, "Check Number");
		}
		if (sourceLOB.getCreateStation() == null) {
			appendToMissingDataBuffer(buf, type, "Create Station");
		}
		if (nbaSource.getCreateDate() == null) {
			appendToMissingDataBuffer(buf, type, "Create Date");
		}
		if (sourceLOB.getCompany() == null) {
			appendToMissingDataBuffer(buf, type, "Company");
		} 
		//APSL3836 begin
		if (sourceLOB.getPaymentMoneySource() != null && String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT).equals(sourceLOB.getPaymentMoneySource())) {
			appendToMissingDataBuffer(buf, type, "Paper check scanned as ACH");
		}//APSL3836 end
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
	//NBA331.1, APSL5055 change position parameter to CheckAllocation
	protected void processNbaMoney(String caseID, CheckAllocation allocation, boolean primaryContractInd, NbaContractCheckData nbaContractCheckDataTable)
			throws NbaBaseException {
		//NBA213 deleted code
		String productType = null;
		String productCode = null; //AXAL3.7.23
		StringBuffer primaryInsName = new StringBuffer();
		//begin ALS5583
		NbaDst nbaCase = null;
		if (isSourceParentCase(caseID)) {
			nbaCase = this.sourceParentCase;
		} else {
			nbaCase = retrieveCase(caseID);
		}

		NbaSource nbaSource = getNbaSource();
		NbaLob sourceLob = getSourceLob();

		productType = nbaCase.getNbaLob().getProductTypSubtyp();

		productCode = nbaCase.getNbaLob().getPlan(); //AXAL3.7.23

		if (nbaCase.getNbaLob().getLastName() != null) {
			primaryInsName.append(nbaCase.getNbaLob().getLastName()).append(",");
		}
		if (nbaCase.getNbaLob().getFirstName() != null) {
			primaryInsName.append(nbaCase.getNbaLob().getFirstName()).append(" ");
		}
		if (nbaCase.getNbaLob().getMiddleInitial() != null) {
			primaryInsName.append(nbaCase.getNbaLob().getMiddleInitial());
		}
		long distChannel = nbaCase.getNbaLob().getDistChannel(); // APSL3410
		// If there's only contract associated the check, the user only has to enter a check amount and
		// does not have to enter an amount to be applied.
		// Money determination should set CWAM equal to CKAM if not present.
		double amount;
		if (getNumberOfContracts() == 1 && allocation.getCwaAmount() == null) { // NBA331.1, APSL5055
			allocation.setCwaAmount(sourceLob.getCheckAmount());		// NBA331.1, APSL5055
		}

		//Update Contract to Check table
		updateContractToCheckTable(allocation.getPolicyNumber(), allocation.getCompany(), allocation.getCwaAmount(),
                productType, NbaUtils.prepareSearchString(primaryInsName.toString()), nbaCase.getNbaLob().getBackendSystem(), primaryContractInd,
                nbaContractCheckDataTable, productCode, distChannel);//SPR2618 AXAL3.7.23, APSL3410

		//if the work item is CWA and if the case is one which with that CWA is attached
		// then do not create another CWA
		if (nbaCase.getNbaLob().getPolicyNumber().equalsIgnoreCase(getWork().getNbaLob().getPolicyNumber())
				&& getWork().getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CWA)) {
			//copy the lob fields from source to CWA work item
			NbaLob workLob = getWork().getNbaLob();
			//NbaLob sourceLob = nbaSource.getNbaLob();
			getWork().getNbaLob().setCwaAmount(allocation.getCwaAmount());
			//copy last, middle, first name from case to CWA work item
			workLob.setLastName(nbaCase.getNbaLob().getLastName());
			workLob.setMiddleInitial(nbaCase.getNbaLob().getMiddleInitial());
			workLob.setFirstName(nbaCase.getNbaLob().getFirstName());
			workLob.setCompany(allocation.getCompany());
			if (allocation.getCostBasis() != null) {
				workLob.setCostBasis(allocation.getCostBasis());
			}			
			//workLob.setExchangeReplace(sourceLob.getExchangeReplaceAt(position));
			if (allocation.getPendingPaymentType() != null) {
				workLob.setPendPaymentType(allocation.getPendingPaymentType().intValue());
			}			
			workLob.setPreviousTaxYear(allocation.isPreviousTaxYear()); //NBA173
			workLob.setCreateStation(sourceLob.getCreateStation()); //NBA228
			workLob.setCaseManagerQueue(nbaCase.getNbaLob().getCaseManagerQueue()); // AppSub - NBLXA-2548[NBLXA-2328]
			//Code Refractored
			List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2MNYDTM_NBCWA_APPLYMONEY_TASK);
			sourceLob.copyLOBsTo(workLob, lobList);
		} else if (!(NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN == getWork().getNbaLob().getInforcePaymentType())) { //NBA077
			//Don't create cwa workitem if IFPT LOB is 278 and move existing payment workitem to apply money process.
			//Otherwise create new CWA workitem and attach it to the case
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, getWork(), nbaSource.getSource().getSourceType(),
					VPMS_NBAMONEY);
			NbaTransaction nbaTransaction = nbaCase.addTransaction(provider.getWorkType(), provider.getInitialStatus());
			NbaLob transactionLob = nbaTransaction.getNbaLob();
			//NbaLob sourceLob = nbaSource.getNbaLob();
			nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
			nbaTransaction.addNbaSource(nbaSource);
			//set portal indicator to false
			transactionLob.setPortalCreated(false);
			//copy the lob fields from source to CWA work item
			
			transactionLob.setPolicyNumber(allocation.getPolicyNumber());
			transactionLob.setCompany(allocation.getCompany());
			if (allocation.getCostBasis() != null) {
				transactionLob.setCostBasis(allocation.getCostBasis());
			}
			//transactionLob.setExchangeReplace(sourceLob.getExchangeReplaceAt(position));
			if (allocation.getPendingPaymentType() != null) {
				transactionLob.setPendPaymentType(allocation.getPendingPaymentType().intValue()); //NBA173
			}			
			transactionLob.setPreviousTaxYear(allocation.isPreviousTaxYear()); //NBA173
			transactionLob.setCwaAmount(allocation.getCwaAmount());
			//			copy last, middle, first name from case to CWA work item
			transactionLob.setLastName(nbaCase.getNbaLob().getLastName());
			transactionLob.setMiddleInitial(nbaCase.getNbaLob().getMiddleInitial());
			transactionLob.setFirstName(nbaCase.getNbaLob().getFirstName());
			transactionLob.setCreateStation(sourceLob.getCreateStation()); //NBA228
			transactionLob.setUserId(getWork().getNbaLob().getUserId()); //ALS4102
			transactionLob.setCaseManagerQueue(nbaCase.getNbaLob().getCaseManagerQueue()); //NBLXA-2548[NBLXA-2328]
			//Code Refractored
			List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2MNYDTM_NBCWA_APPLYMONEY_TASK);
			sourceLob.copyLOBsTo(transactionLob, lobList);
			if (!isSourceParentCase(nbaCase.getID())) { //ALS5583
				nbaCase = updateWork(getUser(), nbaCase); //SPR1851, NBA213
				unlockWork(getUser(), nbaCase); //SPR1851, NBA213
			} else {
				sourceParentCase = nbaCase;//ALS5583
			}
		}
		//NBA213 deleted code
	}

	/**
	 * This method processes Nba Money for companion cases.
	 * 
	 * @param vo
	 *            A companion case value object which shares a CWA amount with a work item in this queue.
	 * @param nbaContractCheckDataTable
	 *            instance of NbaContractCheckData class
	 * @exception NbaBaseException
	 */
	//NBA033 new method
	protected void processNbaMoney(NbaCompanionCaseVO vo, NbaContractCheckData nbaContractCheckDataTable) throws NbaBaseException {
		//NBA213 deleted code
		NbaDst nbaCase = retrieveCase(vo.getWorkItemID());
		NbaSource nbaSource = getNbaSource();
		NbaLob sourceLob =getSourceLob();
		String productCode = nbaCase.getNbaLob().getPlan(); //AXAL3.7.23
		long distChannel = nbaCase.getNbaLob().getDistChannel(); //APSL3410
		//Update Contract to Check table
		updateContractToCheckTable(vo.getContractNumber(), sourceLob.getCompany(), //APSL1252, APSL1249
				vo.getCwaAmount(), vo.getProductTypeSubType(), vo.getFullName(), vo.getBackendSystem(), false, nbaContractCheckDataTable, productCode, distChannel); //AXAL3.7.23, APSL3410

		//Don't create cwa workitem if IFPT LOB is 278 and move existing payment workitem to apply money process.
		if (!(NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN == getWork().getNbaLob().getInforcePaymentType())) { //NBA077
			//Create new CWA workitem and attach it to the case
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, getWork(), nbaSource.getSource().getSourceType(),
					VPMS_NBAMONEY);

			NbaTransaction nbaTransaction = nbaCase.addTransaction(provider.getWorkType(), provider.getInitialStatus());
			nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
			nbaTransaction.addNbaSource(nbaSource);

			//set portal indicator to false
			NbaLob txLob = nbaTransaction.getNbaLob();
			txLob.setPortalCreated(false);

			//copy the lob fields from source to CWA work item
			txLob.setBundleNumber(nbaSource.getNbaLob().getBundleNumber());
			txLob.setPolicyNumber(vo.getContractNumber());
			txLob.setCompany(sourceLob.getCompany());//APSL1249,APSL1252
			txLob.setCwaAmount(vo.getCwaAmount());
			txLob.setCwaDate(sourceLob.getCwaDate());
			txLob.setCheckAmount(sourceLob.getCheckAmount());
			txLob.setCheckNumber(sourceLob.getCheckNumber());

			//copy last, middle, first name from case to CWA work item
			txLob.setLastName(vo.getLastName());
			txLob.setMiddleInitial(vo.getMiddleInitial());
			txLob.setFirstName(vo.getFirstName());
			txLob.setCaseManagerQueue(nbaCase.getNbaLob().getCaseManagerQueue()); //NBLXA-2548[NBLXA-2328]
			txLob.setCreateStation(nbaSource.getNbaLob().getCreateStation()); //NBA228
			txLob.setCheckLastName(sourceLob.getCheckLastName()); //NBA228

			//AXA-SPECIFIC
			txLob.setPaymentMoneySource(nbaSource.getNbaLob().getPaymentMoneySource());//NBA221
			// begin AXAL3.7.01
			txLob.setCheckIdentity(nbaSource.getNbaLob().getCheckIdentity());
			txLob.setForeignBankAddress(nbaSource.getNbaLob().getForeignBankAddress());
			txLob.setReceiptDate(nbaSource.getNbaLob().getReceiptDate());
			//end AXAL3.7.01
			txLob.setHandWrittenCheck(nbaSource.getNbaLob().getHandWrittenCheck());//AXAL3.7.43
			txLob.setUserId(getWork().getNbaLob().getUserId()); //ALS4102
			txLob.setCheckEntityName(nbaSource.getNbaLob().getCheckEntityName());//ALS5502
			//AXA-SPECIFIC
			nbaCase = updateWork(getUser(), nbaCase); //SPR1851, NBA213
			unlockWork(getUser(), nbaCase); //SPR1851, NBA213
		} //NBA077
		//NBA213 deleted code
	}

	/**
	 * This method processes Non Nba Money
	 * 
	 * @param position
	 *            the index to retrieve the information from the source
	 * @param primaryContractInd
	 *            determines if the contract is primary or not
	 * @param nbaContractCheckDataTable
	 *            instance of NbaContractCheckData class
	 * @exception NbaBaseException
	 */
	protected void processNonNbaMoney(CheckAllocation allocation, boolean primaryContractInd, NbaContractCheckData nbaContractCheckDataTable)
			throws NbaBaseException {

		NbaSource nbaSource = getNbaSource();
		long distChannel = -1; // APSL3410
		//Begin APSL3194,QC#11974 get CarrierAdminSystem from nbaTxLifeRes of ValidateClientPolicy ws
		NbaTXLife nbaTxLifeRes = processValidateClientPolicy(allocation.getPolicyNumber());
		String carrierAdminSystem = null;
		if (nbaTxLifeRes != null) {
			OLifE olife = nbaTxLifeRes.getOLifE();
			if (olife != null) {
				Holding holding = olife.getHoldingAt(0);
				carrierAdminSystem = holding.getCarrierAdminSystem();
			}

		}//End APSL3194,QC#11974
		// If there's only one contrat associated with check, the user only has to enter a check amount and
		// does not have to enter an amount to be applied.
		// Money determination should set CWAM equal to CKAM if not present.
		double amount;
		if (getNumberOfContracts() == 1 && allocation.getCwaAmount() == null) {
			amount = nbaSource.getNbaLob().getCheckAmount();
		} else {
			amount = allocation.getCwaAmount();
		}
		// Begin APSL3410
        if (getParentCase() != null && getParentCase().getNbaLob() != null && getParentCase().getNbaLob().getPolicyNumber() != null
                && getParentCase().getNbaLob().getPolicyNumber().equals(allocation.getPolicyNumber())) {
            distChannel = getParentCase().getNbaLob().getDistChannel(); // APSL3410
        }
		// End APSL3410
		//Update Contract to Check table
		updateContractToCheckTable(allocation.getPolicyNumber(),
		        allocation.getCompany(), amount, null,
				null, carrierAdminSystem, primaryContractInd, nbaContractCheckDataTable, null, distChannel); //AXAL3.7.23,APSL3194/QC#11974, APSL3410

	}

	/**
	 * This methods processes the incoming credit card payment. A row is created in the Contract to Credit Card database table with an indicator for
	 * Nba or Non-Nba money. This table is used for reporting on credit card payment work flow activity. If the work item is a Generic Payment
	 * (NBPAYMENT), a new CWA work item will be created and hydrated with lob data from the original credit card source. The new CWA work item will be
	 * attached to the case and the original work item will be sent to the end queue.
	 * 
	 * @exception NbaBaseException
	 */
	//New Method NBA115
	protected void processCreditCardPayment() throws NbaBaseException {

		NbaSource ccSource = (NbaSource) getWork().getNbaSources().get(0);
		NbaLob sourceLob = ccSource.getNbaLob();

		boolean isGenericPayment = getWork().getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_PAYMENT);
		boolean isNbaMoney = VPMS_NBAMONEY.equals(getMoneyTypeForNextContact(sourceLob.getPolicyNumber(), null, sourceLob.getInforcePaymentManInd())); //SPR2238,
																																					   // NBA182//ALS5026
																																					   // Company
																																					   // name
																																					   // not
																																					   // to
																																					   // be
																																					   // used
																																					   // for
																																					   // search

		NbaCreditCardData ccData = new NbaCreditCardData();
		ccData.setTransactionId(sourceLob.getCCTransactionId());
		ccData.setCompany(sourceLob.getCompany());
		ccData.setContractNumber(sourceLob.getPolicyNumber());
		ccData.setCreditCardNumber(sourceLob.getCCNumber());
		ccData.setCreditCardType(sourceLob.getCCType());
		ccData.setAmount(sourceLob.getCwaAmount());
		ccData.setDepositDate(sourceLob.getCwaDate());

		StringBuffer buf = new StringBuffer();
		String type = "Credit Card";
		if (ccData.getCompany() == null) {
			appendToMissingDataBuffer(buf, type, "Company");
		}
		if (ccData.getContractNumber() == null) {
			appendToMissingDataBuffer(buf, type, "Policy Number");
		}
		if (buf.length() > 0) {
			addComment(buf.toString());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Insufficent Credit Card data.", getFailStatus()));
			return;
		}

		if (isNbaMoney) {
			if (isGenericPayment) {//NBPAYMENT
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, getWork(), NbaConstants.A_WT_PAYMENT, VPMS_NBAMONEY);
				NbaTransaction nbaTransaction = getParentCase().addTransaction(provider.getWorkType(), provider.getInitialStatus()); //SPR2238
				nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
				nbaTransaction.addNbaSource(ccSource);
				nbaTransaction.getNbaLob().setPortalCreated(false);
				nbaTransaction.getNbaLob().setPolicyNumber(sourceLob.getPolicyNumber());
				nbaTransaction.getNbaLob().setCompany(sourceLob.getCompany());
				nbaTransaction.getNbaLob().setCwaDate(sourceLob.getCwaDate());
				nbaTransaction.getNbaLob().setCwaAmount(sourceLob.getCwaAmount());
				nbaTransaction.getNbaLob().setCCBillingAddr(sourceLob.getCCBillingAddr());
				nbaTransaction.getNbaLob().setCCBillingCity(sourceLob.getCCBillingCity());
				nbaTransaction.getNbaLob().setCCBillingState(sourceLob.getCCBillingState());
				nbaTransaction.getNbaLob().setCCBillingZip(sourceLob.getCCBillingZip());
				nbaTransaction.getNbaLob().setCCType(sourceLob.getCCType());
				nbaTransaction.getNbaLob().setCCExpDate(sourceLob.getCCExpDate());
				nbaTransaction.getNbaLob().setCCBillingName(sourceLob.getCCBillingName());
				nbaTransaction.getNbaLob().setCCNumber(sourceLob.getCCNumber());
				nbaTransaction.getNbaLob().setCCTransactionId(sourceLob.getCCTransactionId());
				nbaTransaction.getNbaLob().setPendPaymentType(sourceLob.getPendPaymentType());
				nbaTransaction.getNbaLob().setPaymentMoneySource(sourceLob.getPaymentMoneySource());//NBA221
				nbaTransaction.getNbaLob().setUserId(getWork().getNbaLob().getUserId()); //ALS4102
				//begin SPR2238
				setParentCase(updateWork(getUser(), getParentCase())); //NBA213
				unlockWork(getUser(), getParentCase()); //NBA213
				//end SPR2238
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getAlternateStatus()));
			} else {//NBCWA
				getWork().getNbaLob().setPolicyNumber(sourceLob.getPolicyNumber());
				getWork().getNbaLob().setCompany(sourceLob.getCompany());
				getWork().getNbaLob().setCwaAmount(sourceLob.getCwaAmount());
				getWork().getNbaLob().setCwaDate(sourceLob.getCwaDate());
				getWork().getNbaLob().setCCBillingAddr(sourceLob.getCCBillingAddr());
				getWork().getNbaLob().setCCBillingCity(sourceLob.getCCBillingCity());
				getWork().getNbaLob().setCCBillingState(sourceLob.getCCBillingState());
				getWork().getNbaLob().setCCBillingZip(sourceLob.getCCBillingZip());
				getWork().getNbaLob().setCCType(sourceLob.getCCType());
				getWork().getNbaLob().setCCExpDate(sourceLob.getCCExpDate());
				getWork().getNbaLob().setCCBillingName(sourceLob.getCCBillingName());
				getWork().getNbaLob().setCCNumber(sourceLob.getCCNumber());
				getWork().getNbaLob().setCCTransactionId(sourceLob.getCCTransactionId());
				getWork().getNbaLob().setPendPaymentType(sourceLob.getPendPaymentType());
				getWork().getNbaLob().setPaymentMoneySource(sourceLob.getPaymentMoneySource());//NBA221
				getWork().getNbaLob().setHandWrittenCheck(sourceLob.getHandWrittenCheck());//AXAL3.7.43
				getWork().setUpdate();
			}
		} else { //InForce
			ccData.setInforceInd(true);
			NbaLob workLob = getWork().getNbaLob();
			workLob.setPolicyNumber(sourceLob.getPolicyNumber());
			workLob.setCompany(sourceLob.getCompany());
			workLob.setCwaAmount(sourceLob.getCwaAmount());
			workLob.setCCBillingAddr(sourceLob.getCCBillingAddr());
			workLob.setCCBillingCity(sourceLob.getCCBillingCity());
			workLob.setCCBillingState(sourceLob.getCCBillingState());
			workLob.setCCBillingZip(sourceLob.getCCBillingZip());
			workLob.setCCType(sourceLob.getCCType());
			workLob.setCCExpDate(sourceLob.getCCExpDate());
			workLob.setCCBillingName(sourceLob.getCCBillingName());
			workLob.setCCNumber(sourceLob.getCCNumber());
			workLob.setCCTransactionId(sourceLob.getCCTransactionId());
			workLob.setInforcePaymentDate(sourceLob.getInforcePaymentDate());
			workLob.setInforcePaymentType(sourceLob.getInforcePaymentType());
			workLob.setInforcePaymentManInd(sourceLob.getInforcePaymentManInd());
			workLob.setPaymentMoneySource(sourceLob.getPaymentMoneySource());//NBA221
			workLob.setHandWrittenCheck(sourceLob.getHandWrittenCheck());//AXAL3.7.43
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		ccData.insert();

		//SPR2238 code deleted
	}

	/**
	 * This methods processes payments without sources, which are exclusively credit card payments at the time of this enhancement. These payments
	 * will be credit card transactions via the portal, application entry cases with credit card payments and no attached source, or telephone call
	 * payments entered via the Credit Card Payment view. A row is created in the Contract to Credit Card database table with an indicator for Nba or
	 * Non-Nba money. This table is used for reporting on credit card payment work flow activity. If the work item is a Generic Payment (NBPAYMENT), a
	 * new CWA work item will be created and hydrated with lob data from the original credit card source. The new CWA work item will be attached to
	 * the case and the original work item will be sent to the end queue.
	 * 
	 * @exception NbaBaseException
	 */
	//New Method NBA115
	protected void processNoSourcePayment() throws NbaBaseException {

		NbaLob lob = getWork().getNbaLob();
		boolean isGenericPayment = getWork().getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_PAYMENT);
		String caseID = getCaseIDForPayment(lob.getPolicyNumber(), lob.getCompany());
		NbaDst nbaCase = retrieveCase(caseID);

		NbaCreditCardData ccData = new NbaCreditCardData();
		ccData.setTransactionId(lob.getCCTransactionId());
		ccData.setCompany(lob.getCompany());
		ccData.setContractNumber(lob.getPolicyNumber());
		ccData.setCreditCardType(lob.getCCType());
		ccData.setAmount(lob.getCwaAmount());
		ccData.setCreditCardNumber(lob.getCCNumber());
		ccData.setDepositDate(lob.getCwaDate());

		StringBuffer buf = new StringBuffer();
		String type = "Credit Card";
		if (ccData.getCompany() == null) {
			appendToMissingDataBuffer(buf, type, "Company");
		}
		if (ccData.getContractNumber() == null) {
			appendToMissingDataBuffer(buf, type, "Policy Number");
		}
		if (buf.length() > 0) {
			addComment(buf.toString());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Insufficent Credit Card data.", getFailStatus()));
			return;
		}
		if (isGenericPayment) { //NBPAYMENT
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, getWork(), NbaConstants.A_ST_CC_NO_SOURCE, VPMS_NBAMONEY);
			NbaTransaction nbaTransaction = nbaCase.addTransaction(provider.getWorkType(), provider.getInitialStatus());
			nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
			nbaTransaction.getNbaLob().setPortalCreated(false);
			nbaTransaction.getNbaLob().setPolicyNumber(lob.getPolicyNumber());
			nbaTransaction.getNbaLob().setCompany(lob.getCompany());
			nbaTransaction.getNbaLob().setCwaAmount(lob.getCwaAmount());
			nbaTransaction.getNbaLob().setCCBillingAddr(lob.getCCBillingAddr());
			nbaTransaction.getNbaLob().setCCBillingCity(lob.getCCBillingCity());
			nbaTransaction.getNbaLob().setCCBillingState(lob.getCCBillingState());
			nbaTransaction.getNbaLob().setCCBillingZip(lob.getCCBillingZip());
			nbaTransaction.getNbaLob().setCCType(lob.getCCType());
			nbaTransaction.getNbaLob().setCCExpDate(lob.getCCExpDate());
			nbaTransaction.getNbaLob().setCCBillingName(lob.getCCBillingName());
			nbaTransaction.getNbaLob().setCCNumber(lob.getCCNumber());
			nbaTransaction.getNbaLob().setCCTransactionId(lob.getCCTransactionId());
			nbaTransaction.getNbaLob().setPendPaymentType(lob.getPendPaymentType());
			nbaTransaction.getNbaLob().setPaymentMoneySource(Long.toString(NbaOliConstants.NBA_ORIGINATION_PAYFORM_CREDCARD));//NBA221 //NBA228
			nbaTransaction.getNbaLob().setHandWrittenCheck(lob.getHandWrittenCheck());//AXAL3.7.43
			nbaTransaction.getNbaLob().setCwaDate(lob.getCwaDate());
			nbaTransaction.getNbaLob().setUserId(lob.getUserId()); //ALS4102
			nbaTransaction.getNbaLob().setCaseManagerQueue(nbaCase.getNbaLob().getCaseManagerQueue()); //NBLXA-2548[NBLXA-2328]
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getAlternateStatus()));
		}
		ccData.insert();
		nbaCase = updateWork(getUser(), nbaCase); //NBA213
		unlockWork(getUser(), nbaCase); //NBA213
	}

	/**
	 * This methods processes the incoming wire transfer payment.
	 * 
	 * @exception NbaBaseException
	 */
	protected void processWireTransfer() throws NbaBaseException {

		//NBA213 deleted code
		NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);
		NbaLob sourceLob = nbaSource.getNbaLob(); //NBA182
		String contractNumber = null;
		String companyCode = null;
		NbaTXLife nbaTXlife = null;
		Date wireTransferEffDate = null; //SPR2238
		String institutionID = null; //SPR2238
		FinancialActivity financialActivity = null; //SPR2238
		long paymentForm; //NBA228
		try {
			//retrive the contract number and company code from the source
			nbaTXlife = new NbaTXLife(nbaSource.getText());
			contractNumber = nbaTXlife.getNbaHolding().getPolicy().getPolNumber();
			companyCode = nbaTXlife.getNbaHolding().getPolicy().getCarrierCode();
			financialActivity = nbaTXlife.getNbaHolding().getPolicy().getFinancialActivityAt(0); //SPR2238
			wireTransferEffDate = nbaTXlife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransExeDate(); //SPR2238
			institutionID = nbaTXlife.getOLifE().getSourceInfo().getSourceInfoDescription(); //SPR2238
			paymentForm = financialActivity.getPaymentAt(0).getPaymentForm(); //NBA228
		} catch (Exception e) {
			addComment("Invalid XML");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getOtherStatus()));
			return; //NBA093
		}
		//Update Wire Tranfer Database table
		NbaWireTransferData nbaWireTransferData = new NbaWireTransferData();
		setTechnicalKey(nbaWireTransferData.getNextTechnicalKey());
		nbaWireTransferData.setTechnicalKey(getTechnicalKey());
		nbaWireTransferData.setWireTransferEffDate(wireTransferEffDate); //SPR2238
		nbaWireTransferData.setInstitutionID(institutionID); //SPR2238
		//begin NBA093
		StringBuffer buf = new StringBuffer();
		String type = "Wire Transfer";
		//begin SPR2238
		if (companyCode == null || companyCode.length() == 0) {
			appendToMissingDataBuffer(buf, type, "Company Code");
		}
		if (contractNumber == null || contractNumber.length() == 0) {
			appendToMissingDataBuffer(buf, type, "Contract Number");
		}
		//end SPR2238
		if (nbaWireTransferData.getTechnicalKey() < 0) {
			appendToMissingDataBuffer(buf, type, "Technical Key");
		}
		if (nbaWireTransferData.getWireTransferEffDate() == null) {
			appendToMissingDataBuffer(buf, type, "Wire Transfer Eff Date");
		}
		if (nbaWireTransferData.getInstitutionID() == null) {
			appendToMissingDataBuffer(buf, type, "Institution ID");
		}
		//Begin NBA228
		//TODO NBA228
		if (paymentForm <= 0) {
			appendToMissingDataBuffer(buf, type, "Payment Form");
		}
		//End NBA228
		if (buf.length() > 0) {
			addComment(buf.toString());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getOtherStatus()));
			return;
		}
		//end NBA093
		nbaWireTransferData.insert();

		NbaContractsWireTransferData nbaContractsWireTransferData = new NbaContractsWireTransferData();
		nbaContractsWireTransferData.setTechnicalKey(getTechnicalKey());
		nbaContractsWireTransferData.setCompany(companyCode);
		nbaContractsWireTransferData.setContractNumber(contractNumber);
		nbaContractsWireTransferData.setSequence(nbaContractsWireTransferData.getWireTransferSequence(nbaWireTransferData.getWireTransferEffDate(),
				nbaWireTransferData.getInstitutionID())); //SPR1171
		nbaContractsWireTransferData.setAppliedInd(false);
		nbaContractsWireTransferData.setReturnedInd(false);
		nbaContractsWireTransferData.setAmount(financialActivity.getFinActivityGrossAmt()); //NBA093 SPR2238

		//SPR1359 deleted 2 lines
		//begin NBA068

		NbaTransaction nbaTransaction = null;
		NbaProcessWorkItemProvider provider = null;
		nbaContractsWireTransferData.setReportInd(false);
		//SPR1359 deleted 2 lines
		//Create new CWA workitem and attach it to the case if present
		if (VPMS_NBAMONEY.equals(getMoneyType(contractNumber, companyCode, TRUE.equals(sourceLob.getInforcePaymentManInd())))) { //SPR2238, NBA182
			provider = new NbaProcessWorkItemProvider(user, getWork(), nbaSource.getSource().getSourceType(), VPMS_NBAMONEY);
			nbaTransaction = getParentCase().addTransaction(provider.getWorkType(), provider.getInitialStatus()); //SPR2238
		} else {
			provider = new NbaProcessWorkItemProvider(user, getWork(), nbaSource.getSource().getSourceType(), VPMS_NON_NBAMONEY);
			//NBA208-32
			WorkItem transaction = new WorkItem();
			getWork().addTransaction(transaction);
			transaction.setBusinessArea(getWork().getBusinessArea());
			transaction.setWorkType(provider.getWorkType());
			transaction.setStatus(provider.getInitialStatus());
			//NBA208-32
			transaction.setCreate("Y");
			nbaTransaction = new NbaTransaction(transaction);
			nbaTransaction.getNbaLob().setRouteReason(nbaTransaction.findRouteReason(provider.getWorkType(), provider.getInitialStatus())); //NBA097
		}

		nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		//NBA208-32
		nbaTransaction.getTransaction().setLock("Y"); //SPR2238
		nbaTransaction.addNbaSource(nbaSource);
		//set portal indicator to true
		nbaTransaction.getNbaLob().setPortalCreated(true);
		nbaTransaction.getNbaLob().setUserId(getWork().getNbaLob().getUserId()); //ALS4102
		//copy the lob fields from source to CWA work item

		nbaTransaction.getNbaLob().setPolicyNumber(contractNumber);
		nbaTransaction.getNbaLob().setCompany(companyCode);
		nbaTransaction.getNbaLob().setCwaAmount(financialActivity.getFinActivityGrossAmt()); //NBA093 SPR2238
		if (financialActivity.hasFinEffDate()) { //NBA093 SPR2238
			nbaTransaction.getNbaLob().setCwaDate(financialActivity.getFinEffDate()); //NBA093 SPR2238
			nbaTransaction.getNbaLob().setInforcePaymentDate(financialActivity.getFinEffDate()); //SPR2238 AXAL3.7.26
		} else if (financialActivity.hasFinActivityDate()) { //ALS3.7.26
			nbaTransaction.getNbaLob().setCwaDate(financialActivity.getFinActivityDate());
			nbaTransaction.getNbaLob().setInforcePaymentDate(financialActivity.getFinActivityDate());
		} else { //AXAL3.7.26
			Date today = new Date();
			nbaTransaction.getNbaLob().setCwaDate(today);
			nbaTransaction.getNbaLob().setInforcePaymentDate(today);
		}
		if (financialActivity.hasFinActivityType()) { //SPR2238
			nbaTransaction.getNbaLob().setInforcePaymentType((int) (financialActivity.getFinActivityType())); //SPR2238
		}
		if (financialActivity.hasCostBasisAdjAmt()) { //NBA093 SPR2238
			nbaTransaction.getNbaLob().setCostBasis(financialActivity.getCostBasisAdjAmt()); //NBA093 SPR2238
		}
		nbaTransaction.getNbaLob().setExchangeReplace((int) financialActivity.getFinActivityType()); //NBA093 SPR2238

		nbaTransaction.getNbaLob().setPaymentMoneySource(Long.toString(paymentForm)); //TODO NBA228 - Check the correct mapping
		if (VPMS_NBAMONEY.equals(getMoneyType(contractNumber, companyCode, TRUE.equals(sourceLob.getInforcePaymentManInd())))) { //SPR2238, NBA182
			//copy last, middle, first name from case to CWA work item
			nbaTransaction.getNbaLob().setLastName(getParentCase().getNbaLob().getLastName()); //SPR2238
			nbaTransaction.getNbaLob().setMiddleInitial(getParentCase().getNbaLob().getMiddleInitial()); //SPR2238
			nbaTransaction.getNbaLob().setFirstName(getParentCase().getNbaLob().getFirstName()); //SPR2238
			// begin SPR1171
			//save the sequence number in an LOB
			nbaTransaction.getNbaLob().setSequenceNumber((int) nbaContractsWireTransferData.getSequence()); //NBA130
			// end SPR1171
			nbaTransaction.getNbaLob().setBackendSystem(getParentCase().getNbaLob().getBackendSystem());//SPR1018 SPR2238

			setParentCase(updateWork(getUser(), getParentCase())); //SPR1851, SPR2238, NBA213
			unlockWork(getUser(), getParentCase()); //SPR1851, SPR2238, NBA213
		}
		//break the relationship of the source from the generic work item
		nbaSource.setBreakRelation();

		nbaContractsWireTransferData.insert();
		//end NBA068
		//NBA213 deleted code
	}

	/**
	 * Retrieves the case for which payment has to be made
	 * 
	 * @return NbaDst value object containing the case data
	 * @param caseID
	 *            case ID
	 * @throws NbaBaseException
	 */
	protected NbaDst retrieveCase(String caseID) throws NbaBaseException {
		//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(caseID, true);
		return retrieveWorkItem(getUser(), retOpt); //NBA213
		//NBA213 deleted code
	}

	/**
	 * Sets the bundle ID
	 * 
	 * @param newBundleID
	 *            new Bundle ID
	 */
	//SPR2238 changed method visibility
	protected void setBundleID(java.lang.String newBundleID) {
		bundleID = newBundleID;
	}

	/**
	 * Sets the Technical Key
	 * 
	 * @param newTechnicalKey
	 *            new Technical Key
	 */
	//SPR2238 changed method visibility
	protected void setTechnicalKey(long newTechnicalKey) {
		technicalKey = newTechnicalKey;
	}

	/**
	 * Updates NBA_CONTRACTS_CHECKS table with check and contract data
	 * 
	 * @param contractNumber -
	 *            contract number for which amount has to be applied
	 * @param companyCode -
	 *            company code of the case for which amount has to be applied
	 * @param amount -
	 *            amount which has to be applied
	 * @param productType -
	 *            product type of the case for which amount has to be applied
	 * @param primaryInsName -
	 *            primary insured name for the case for which amount has to be applied
	 * @param backEndSystem -
	 *            back end system id of the case for which amount has to be applied
	 * @param primaryContractInd -
	 *            boolean indicating if the contract is primary or not
	 * @param nbaContractCheckDataTable -
	 *            instance of Contract Check Data table accessor
	 * @throws NbaBaseException
	 *             NbaDataAccessException
	 */
	protected void updateContractToCheckTable(String contractNumber, String companyCode, double amount, String productType, String primaryInsName,
			String backEndSystem, boolean primaryContractInd, NbaContractCheckData nbaContractCheckDataTable, String productCode, long distChannel)
			throws NbaBaseException, NbaDataAccessException { //AXAL3.7.23 - updated signature, APSL3410

		//Insert only if enrty is already not there
		if (nbaContractCheckDataTable.getContractCheckData(getBundleID(), companyCode, contractNumber, getTechnicalKey()) == null) {

			nbaContractCheckDataTable.setTechnicalKey(getTechnicalKey());
			nbaContractCheckDataTable.setBundleID(getBundleID());
			nbaContractCheckDataTable.setCompany(companyCode);
			nbaContractCheckDataTable.setBackendSystem(backEndSystem);
			nbaContractCheckDataTable.setPrimaryContractInd(primaryContractInd);
			nbaContractCheckDataTable.setContractNumber(contractNumber);
			nbaContractCheckDataTable.setAmountApplied(amount);
			nbaContractCheckDataTable.setProductType(productType);
			nbaContractCheckDataTable.setRejectInd(false);
			nbaContractCheckDataTable.setProductCode(productCode); //AXAL3.7.23
			nbaContractCheckDataTable.setDistributionChannel(distChannel); // APSL3410

			//begin NBA077
			if (NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN == getWork().getNbaLob().getInforcePaymentType()) {
				nbaContractCheckDataTable.setAppliedInd(true);
			} else {
				nbaContractCheckDataTable.setAppliedInd(false);
			}
			//end NBA077
			//NBA077 line deleted
			nbaContractCheckDataTable.setPrimaryInsuredName(primaryInsName);
			nbaContractCheckDataTable.setSystemAppliedInd(false); // SR615900
			nbaContractCheckDataTable.setReturnedInd(false); // APSL4513
			nbaContractCheckDataTable.setRescannedInd(false); // APSL4513

			nbaContractCheckDataTable.insert();
		}
	}

	/**
	 * Since the case must be suspended before it can be unlocked, this method is used instead of the superclass method to update AWD.
	 * <P>
	 * Updates the case in AWD, suspend the case using the supsendVO, and unlock the case.
	 * 
	 * @param Suspend
	 *            Value object to send the required parameters to suspend a case
	 * @throws NbaBaseException
	 */
	//NBA033 new method
	protected void updateForSuspend(NbaSuspendVO vo) throws NbaBaseException {
		vo.setTransactionID(getWork().getID());
		vo.setCaseID(null); //reset the case id if it had any
		//begin NBA213
		updateWork(getUser(), getWork());
		suspendWork(getUser(), vo);
		unlockWork(getUser(), getWork());
		workSuspended = true;
		//end NBA213
	}

	/**
	 * Add a missing field to the message.
	 * 
	 * @param String
	 *            buffer to hold the message
	 * @param Type
	 *            of the message
	 * @param detailed
	 *            message
	 */
	//NBA093 new method
	protected void appendToMissingDataBuffer(StringBuffer buf, String type, String detail) {
		if (buf.length() == 0) {
			buf.append("Required information is missing or invalid for ");
			buf.append(type);
			buf.append(": ");
			buf.append(detail);
		} else {
			buf.append(", ");
			buf.append(detail);
		}
	}

	/**
	 * Add new inforce payement work items.
	 * 
	 * @param NbaSource
	 *            value object
	 * @throws NbaBaseException
	 */
	// NBA068 new method
	protected void createNewInforcePaymentItems(NbaSource nbaSource) throws NbaBaseException {

		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, getWork(), nbaSource.getSource().getSourceType(),
				VPMS_NON_NBAMONEY);
		//begin NBA182
		String manualPaymentInd;
		NbaLob nbaLob;
		NbaLob sourceLob;
		double amount;
		NbaAutomatedProcessResult status;
		NbaDst nbaDst;
		CheckAllocation allocation = null;
		nbaLob = new NbaLob(); //NBA182
		sourceLob = getSourceLob();
		//end NBA182
		int seqNumber = 1;//AXAL3.7.01
		for (int i = 1; i <= getNumberOfContracts(); i++) {//AXAL3.7.01
			if (getNumberOfContracts() != 0) {				
				if(i == 1) {
					amount = nbaSource.getNbaLob().getCwaAmount();
					nbaLob.setPolicyNumber(sourceLob.getPolicyNumber());
					nbaLob.setCostBasis(sourceLob.getCostBasis());
					nbaLob.setInforcePaymentDate(sourceLob.getInforcePaymentDate());
					nbaLob.setPaymentMoneySource(sourceLob.getPaymentMoneySource());//NBA221
					nbaLob.setInforcePaymentManInd(sourceLob.getInforcePaymentManInd());
					nbaLob.setInforcePaymentType(sourceLob.getInforcePaymentType());
					manualPaymentInd = nbaSource.getNbaLob().getInforcePaymentManInd();
					nbaLob.setCompany(sourceLob.getCompany());
				} else {
					allocation = getCheckAllocation(i); 
					amount = allocation.getCwaAmount();
					nbaLob.setPolicyNumber(allocation.getPolicyNumber());
					if(allocation.getCostBasis() != null) {
						nbaLob.setCostBasis(allocation.getCostBasis());
					}
					if (allocation.getInforcePaymentDate() != null) {
						nbaLob.setInforcePaymentDate(allocation.getInforcePaymentDate());
					}	
					nbaLob.setPaymentMoneySource(Long.toString(allocation.getPaymentMoneySource()));//NBA221
					if (allocation.isInforcePaymentManual()) {
						nbaLob.setInforcePaymentManInd(TRUE);
					} else {
						nbaLob.setInforcePaymentManInd(Integer.toString(0));
					}
					if (allocation.getInforcePaymentType() != null) {
						nbaLob.setInforcePaymentType(allocation.getInforcePaymentType().intValue());
					}
					manualPaymentInd = nbaLob.getInforcePaymentManInd();
					nbaLob.setCompany(allocation.getCompany());
				}
				
				//NBA182 code deleted				
				
				//set portal indicator to false
				nbaLob.setPortalCreated(false);
				//copy the lob fields from source to Generic Payment work item
				nbaLob.setCwaAmount(amount);			
				nbaLob.setExchangeReplace(sourceLob.getExchangeReplaceAt(seqNumber));
				
				nbaLob.setCreateStation(sourceLob.getCreateStation()); //NBA228
				String caseID = getCaseIDForPayment(nbaLob.getPolicyNumber(), nbaLob.getCompany());
				NbaDst nbaCase = retrieveCase(caseID);
				nbaLob.setCaseManagerQueue(nbaCase.getNbaLob().getCaseManagerQueue()); //NBLXA-2548[NBLXA-2328]			
				//Code Refractor
				List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2MNYDTM_CREATE_INFORACEPAYMENT_TASK);
				sourceLob.copyLOBsTo(nbaLob, lobList);
				//NBA213 deleted code
				//NBA182 code deleted				
				status = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getNonNbaStatus(manualPaymentInd)); //NBA182

				nbaDst = createCase(getUser(), NbaConstants.A_BA_NBA, provider.getWorkType(), status.getStatus(), nbaLob); //NBA213, NBA182
				nbaDst.addNbaSource(nbaSource);
				setRouteReason(nbaDst, status.getStatus()); // NBA097
				updateWork(getUser(), nbaDst); //NBA213
				unlockWork(getUser(), nbaDst); //NBA213
				//NBA213 deleted code
			}
			seqNumber++;
		}
	}

	//SPR2238 code deleted
	/**
	 * Determine if the payment is nbA or non-nbA money. It is nbA money if: - the manual money indicator is not true ("IFMP" LOB is not equal "1") -
	 * and an Application work item is present in the workflow system which matches the company code and contract number - and the Application work
	 * item is not in the end queue - if the Application work item contract change type ("CHTP" LOB) has a value - and if the payment type is
	 * un-applied cash Suspense item for a reinstatement ("IFPT" is equal to "278") - if the Application work item contract change type does not have
	 * a value - and if the policy is not active or issued (Holding.Policy.PolicyStatus = "1" "25") Otherwise it is non-nbA money.
	 * 
	 * @param contractNumber
	 * @param companyCode
	 * @return
	 */
	// SPR2238 New Method
	//NBA182 added manualInd to method signature
	//NBA331.1, APSL5055 changed manualInd to boolean
	protected String getMoneyType(String contractNumber, String companyCode, boolean manualInd) throws NbaBaseException {

		if (getMoneyType() == null) {
			setMoneyType(VPMS_NON_NBAMONEY); // default
			if (!manualInd) { // is not "manual" money //NBA182
				NbaSearchResultVO nbaSearchResultVO = getNbaSearchResultVO(contractNumber, companyCode);
				if (nbaSearchResultVO != null) {
					setCaseID(nbaSearchResultVO.getWorkItemID());
					setParentCase(retrieveCase(getCaseID()));
					NbaTXLife contract=null; 
					//Start APSL5289
					long status=0l;
					if(getParentCase() != null){
						contract = doHoldingInquiry(getParentCase());
						status = contract.getPolicy().getPolicyStatus();
					}
					//End APSL5289
					if (getParentCase() != null && (!getParentCase().isInEndQueue() || NbaOliConstants.OLI_POLSTAT_ISSUED != status) ) { //APSL5289
						//Begin APSL4102
						ApplicationInfoExtension appInfoExt = contract != null ? NbaUtils.getFirstApplicationInfoExtension(contract.getPolicy().getApplicationInfo()) : null;
						if (appInfoExt != null && appInfoExt.getIssuedToAdminSysInd()) {
							setMoneyType(VPMS_NON_NBAMONEY);
						}//End APSL4102
						else if (getParentCase().getNbaLob().getContractChgType() != null) {
							if (NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN == getWork().getNbaLob().getInforcePaymentType()
									|| Long.parseLong(getParentCase().getNbaLob().getContractChgType()) == NbaOliConstants.NBA_CHNGTYPE_REISSUE) { // ALS5386
								setMoneyType(VPMS_NBAMONEY);
							}
						} else {
							//APSL4102 code deleted
							// QC13983/APSL3924 deleted code
							if (NbaOliConstants.OLI_POLSTAT_ACTIVE != status && NbaOliConstants.OLI_POLSTAT_ISSUED != status) {
								setMoneyType(VPMS_NBAMONEY);
							}
						}
					}
					// Begin QC9215/APSL2255
					else if (getParentCase() != null && getParentCase().isInEndQueue()) {
						if (contract != null && NbaUtils.isNegativeDisposedPolicy(contract)) {
							setInvalidPolicy(true);
						}
						// End QC9215/APSL2255
					}
				}
			}
		}
		return moneyType;
	}

	/**
	 * Determine if the payment is nbA or non-nbA money for the next contract.
	 * 
	 * @param contractNumber
	 * @param companyCode
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA182 added manualInd to method signature
	protected String getMoneyTypeForNextContact(String contractNumber, String companyCode, String manualInd) throws NbaBaseException {
		setMoneyType(null);
		return getMoneyType(contractNumber, companyCode, TRUE.equals(manualInd)); //NBA182
	}

	/**
	 * Perform a lookup in the workflow system for an application work item using the company code and contract number as criteria.
	 * 
	 * @param contractNumber
	 *            Contract Number
	 * @param companyCode
	 *            Company Code
	 * @return NbaSearchResultVO
	 * @throws NbaBaseException
	 */
	// SPR2238 New Method
	protected NbaSearchResultVO getNbaSearchResultVO(String contractNumber, String companyCode) throws NbaBaseException {
		NbaSearchResultVO nbaSearchResultVO = null;
		//NBA213 deleted code
		if (contractNumber != null) {//ALS5026
			NbaSearchVO searchVO = new NbaSearchVO();
			searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
			searchVO.setContractNumber(contractNumber);
			if (companyCode != null) {//ALS5026
				searchVO.setCompanyCode(companyCode);
			}//ALS5026

			searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
			searchVO = lookupWork(getUser(), searchVO); //NBA213
			if (!searchVO.getSearchResults().isEmpty()) {
				nbaSearchResultVO = (NbaSearchResultVO) searchVO.getSearchResults().get(0);
			}
		}
		//NBA213 deleted code
		return nbaSearchResultVO;
	}

	/**
	 * Return the nbA/non-nbA money type
	 * 
	 * @return moneyType
	 */
	// SPR2238 New Method
	protected String getMoneyType() {
		return moneyType;
	}

	/**
	 * Set the nbA/non-nbA money type
	 * 
	 * @param the
	 *            new type
	 */
	// SPR2238 New Method
	protected void setMoneyType(String string) {
		moneyType = string;
	}

	/**
	 * Return the id of the parent Case
	 * 
	 * @return caseID
	 */
	// SPR2238 New Method
	protected String getCaseID() {
		return caseID;
	}

	/**
	 * Set the id of the parent Case
	 * 
	 * @param the
	 *            new id
	 */
	// SPR2238 New Method
	protected void setCaseID(String string) {
		caseID = string;
	}

	/**
	 * Return the NbaDst for the parent case
	 * 
	 * @return parentCase
	 */
	// SPR2238 New Method
	protected NbaDst getParentCase() {
		return parentCase;
	}

	/**
	 * Set the NbaDst for the parent case
	 * 
	 * @param the
	 *            new NbaDst
	 */
	// SPR2238 New Method
	protected void setParentCase(NbaDst dst) {
		parentCase = dst;
	}

	/**
	 * Hold values needed to process a non nba payment
	 */
	//NBA182 New Inner Class
	protected class NonNbaPayments {
		private int count = 0;

		private int indexOfNbaNonPaymentArray[] = null;

		/**
		 * Construct the NbaPayments with the number of non nbA payments
		 * 
		 * @param size
		 */
		public NonNbaPayments(int size) {
			setIndexOfNbaNonPaymentArray(new int[size]);
		}

		/**
		 * get the number of indexes stored in the array
		 * 
		 * @return
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Add an index value to the array
		 * 
		 * @param i
		 */
		public void addIndex(int i) {
			indexOfNbaNonPaymentArray[count] = i;
			count++;
		}

		/**
		 * Get the array of the nbA non-payment indexes
		 * 
		 * @return
		 */
		public int[] getIndexOfNbaNonPaymentArray() {
			return indexOfNbaNonPaymentArray;
		}

		/**
		 * set the array of the nbA non-payment indexes
		 * 
		 * @param indexofNbaNonPaymentArray
		 */
		private void setIndexOfNbaNonPaymentArray(int[] indexOfNbaNonPaymentArray) {
			this.indexOfNbaNonPaymentArray = indexOfNbaNonPaymentArray;
		}
	}

	//AXAL3.7.12 mew method
	protected boolean performCheckValidations(NbaSource nbaSource, NbaLob sourceLOB) throws NbaBaseException {
		StringBuffer buf = checkForMissingLOBs(nbaSource, sourceLOB);
		if (buf.length() > 0) {
			addComment(buf.toString());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getOtherStatus()));
			return false;
		}
		//Verify Scan Station is Valid
		if (!NbaUtils.isValidScanStation(sourceLOB.getCreateStation())) {
			addComment("Scan Station is not Valid.");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getOtherStatus()));
			return false;
		}

		boolean validContracts = checkForValidContracts(sourceLOB);
		if (!validContracts) {
			NbaProcessStatusProvider provider = new NbaProcessStatusProvider(getUser(), getWork(), getNbaTxLife(), getDeOinkMapForSources(getWork()));
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", provider.getAlternateStatus()));
			return false;
		}
		return true;
	}

	//AXAL3.7.12 new method Re-factored this method APSL3194,QC#11974 
	protected boolean checkForValidContracts(NbaLob sourceLOB) throws NbaBaseException {
		boolean isValid = true;	
		
		for (CheckAllocation allocation : getCheckAllocations()) {		
			if (allocation.getPolicyNumber() == null || allocation.getPolicyNumber().isEmpty()) {//ALS4551 Check the contract number for null value
				return false;//ALS4551
			}
			boolean isNbaMoney = VPMS_NBAMONEY.equals(getMoneyTypeForNextContact(allocation.getPolicyNumber(), null, String.valueOf(allocation
					.getInforcePaymentManInd())));//ALS5026 company name not to be used to search
			if (!isNbaMoney) {
				try {
					processValidateClientPolicy(allocation.getPolicyNumber());//APSL3194,QC#11974
				} catch (NbaBaseException nbe) {
					getLogger().logException(nbe);
					addComment("Policy Number " + allocation.getPolicyNumber() + " is Not Valid");
					isValid = false;
				}
			}
			if (isInvalidPolicy()) {//Begin QC#9215
				addComment("Policy Number " + allocation.getPolicyNumber() + " Do Not Have Valid Status");
				isValid = false;
			}//END QC#9215
			
		}
		return isValid;
	}

	// set deOink map for scan station
	//AXAL3.7.12 added new method
	protected Map getDeOinkMapForSources(NbaDst work) {

		Map deOinkMap = new HashMap();
		List sourceList = work.getNbaSources();
		int count = sourceList.size();
		NbaSource aSource = null;
		String sourceType = null;
		for (int i = 0; i < count; i++) {
			aSource = (NbaSource) sourceList.get(i);
			sourceType = aSource.getSource().getSourceType();
			if (NbaConstants.A_WT_PAYMENT.equals(sourceType) || NbaConstants.A_ST_CWA_CHECK.equals(sourceType)) {
				deOinkMap.put(NbaVpmsConstants.A_CREATE_STATION, aSource.getNbaLob().getCreateStation()); // ALS3046
				break;
			}
		}
		return deOinkMap;
	}

	/**
	 * @return Returns the sourceParentCase.
	 */
	//ALS5583 new method
	public NbaDst getSourceParentCase() {
		return sourceParentCase;
	}

	/**
	 * @param sourceParentCase
	 *            The sourceParentCase to set.
	 */
	//ALS5583 new method
	public void setSourceParentCase(NbaDst sourceParentCase) {
		this.sourceParentCase = sourceParentCase;
	}

	//if the CaseId is the same as the source's primary case, return true
	//ALS5583 new method
	protected boolean isSourceParentCase(String id) {
		if (null != getSourceParentCase() && getSourceParentCase().getID().equalsIgnoreCase(id)) {
			return true;
		}
		return false;
	}

	protected void lockSourceCase() throws NbaNetServerDataNotFoundException, NbaBaseException {
		RetrieveWorkItemsRequest retrieveWorkItemsRequest = new RetrieveWorkItemsRequest();
		retrieveWorkItemsRequest.setUserID(getUser().getUserID());
		String id = this.sourceParentCase.getCase().getItemID();
		retrieveWorkItemsRequest.setWorkItemID(id);
		retrieveWorkItemsRequest.setRecordType(id.substring(26, 27));
		retrieveWorkItemsRequest.setLockWorkItem();
		lockWorkItem(retrieveWorkItemsRequest); //Lock the Case
		this.sourceParentCase.getCase().setLockStatus(getUser().getUserID());
	}

	//Begin CR1346004
	/**
	 * @return Returns the case1035.
	 */
	public boolean isCase1035() {
		return case1035;
	}

	/**
	 * @param case1035
	 *            The case1035 to set.
	 */
	public void setCase1035(boolean case1035) {
		this.case1035 = case1035;
	}//End CR1346004

	/**
	 * @return Returns the isInvalidPolicy.
	 */
	//	 New Method QC#9215/APSL2255
	public boolean isInvalidPolicy() {
		return isInvalidPolicy;
	}

	/**
	 * @param isInvalidPolicy The isInvalidPolicy to set.
	 */
	//	New Method QC#9215/APSL2255
	public void setInvalidPolicy(boolean isInvalidPolicy) {
		this.isInvalidPolicy = isInvalidPolicy;
	}
	
	/**
	 * This method is used to call ValidateClientPolicy webservice
	 * 
	 * @param sourceLOB
	 *            the nbaLob retrieve the lob from the source
	 * @param contractCount
	 *            determines the contract count
	 * @exception NbaBaseException
	 * New method APSL3194,QC#11974
	 */
	//APSL5055 Method Signature Changed
	protected NbaTXLife processValidateClientPolicy(String policyNumber) throws NbaBaseException {
		// 1. Validate the non nbA contract number - Call the CIF Client Webservice
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_VALIDATE_POLICY, user, nbaTxLife,
				getParentCase(), policyNumber); // ALII1418, APSL5055
		NbaTXLife nbaTxLifeRes = (NbaTXLife) webServiceInvoker.execute();
		return nbaTxLifeRes;
	}
	
	// APSL4590 New method
	public NbaSearchVO searchCWA(String contractKey) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_CWA);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = lookupWork(getUser(), searchVO);
		return searchVO;
	}
	
	// APSL4590 New method
	public void updateCWABundleId(String bundleId, String oldBundleId, List contractKeyList) throws NbaBaseException {
		for (int j = 0; j < contractKeyList.size(); j++) {
			String contractKey = (String) contractKeyList.get(j);
			NbaSearchVO searchResultVO = searchCWA(contractKey);
			if (searchResultVO != null && searchResultVO.getSearchResults() != null && !(searchResultVO.getSearchResults().isEmpty())) {
				List searchResultList = searchResultVO.getSearchResults();			
				for (int i = 0; i < searchResultList.size(); i++) {
					NbaSearchResultVO searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
					if (searchResultVo.getNbaLob().getBundleNumber().equals(oldBundleId)) {
						NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
						retOpt.setWorkItem(searchResultVo.getWorkItemID(), false);
						retOpt.setLockWorkItem();
						NbaDst nbaDst = retrieveWorkItem(getUser(), retOpt);
						nbaDst.getNbaLob().setBundleNumber(bundleId);
						nbaDst.setNbaUserVO(getUser());
						nbaDst.setUpdate();
						updateWork(getUser(), nbaDst);
						unlockWork(getUser(), nbaDst);
					}
				}
			}
		}
	}
	
	//BEGIN APSL4844
	protected String checkForNTOCases(NbaDst work) throws NbaBaseException {
		NbaSource nbaSource = (NbaSource) work.getNbaSources().get(0);
		if (nbaSource != null) {
			NbaSearchResultVO nbaSearchResultVO = getNbaSearchResultVO(nbaSource.getNbaLob().getPolicyNumber(),work.getNbaLob().getCompany());
			if (nbaSearchResultVO != null) {
				setCaseID(nbaSearchResultVO.getWorkItemID());
				setParentCase(retrieveCase(getCaseID()));
				if (getParentCase() != null) {
					NbaTXLife contract = doHoldingInquiry(getParentCase());
					if (contract != null) {
						work.getNbaLob().setBackendSystem(contract.getPolicy().getCarrierAdminSystem());
						work.getNbaLob().setCompany(nbaSource.getNbaLob().getCompany());
						String status = changeWorkStatus(work,contract);
						if(!NbaUtils.isBlankOrNull(status) && 
								!"-".equalsIgnoreCase(status)) {
							getWork().getNbaLob().setPolicyNumber(nbaSource.getNbaLob().getPolicyNumber());
							getWork().getNbaLob().setCaseManagerQueue(getParentCase().getNbaLob().getCaseManagerQueue());//NBLXA-2548[NBLXA-2328]
							attachWIToParent();
						}
						return status;
					}
				}
			}
		}
		return "";
	}
	
	protected String changeWorkStatus(NbaDst work, NbaTXLife contract) throws NbaBaseException {
		NbaLob lobs = work.getNbaLob();
		Map deOink = new HashMap();
		deOink.put(NbaVpmsConstants.A_PROCESS_ID, NbaVpmsConstants.STATUS_NBPAYMENT_NTO);
		
		NbaVpmsRequestVO vpmsRequestVO = new NbaVpmsRequestVO();
		vpmsRequestVO.setModelName(NbaVpmsConstants.AUTO_PROCESS_STATUS);
		vpmsRequestVO.setEntryPoint(NbaVpmsConstants.EP_WORKITEM_STATUSES);
		vpmsRequestVO.setNbaLob(lobs);
		vpmsRequestVO.setDeOinkMap(deOink);
		vpmsRequestVO.setNbATXLife(contract);
		
		AccelResult result = (AccelResult) currentBP.callBusinessService("RetrieveDataFromBusinessRulesBP", vpmsRequestVO);//APSL1438
		if (result.hasErrors()) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS);
		}
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		work.setStatus(((NbaVpmsRequestVO) result.getFirst()).getPassStatus());
		work.getNbaLob().setRouteReason(((NbaVpmsRequestVO) result.getFirst()).getReason());
		return ((NbaVpmsRequestVO) result.getFirst()).getPassStatus(); 
	}
	
	protected void attachWIToParent() throws NbaNetServerDataNotFoundException, NbaBaseException {
		getWork().setUpdate();
		updateWork(getUser(), getWork());
		
		NbaAwdRetrieveOptionsVO r = new NbaAwdRetrieveOptionsVO();
		r.setWorkItem(getParentCase().getID(), false);
		r.setLockWorkItem();
		NbaDst nbaCase = retrieveWorkItem(getUser(), r);
		nbaCase.getNbaCase().addNbaTransaction(getWork().getNbaTransaction());		
		updateWork(getUser(), nbaCase); 
		//unlockWork(getUser(), getWork());		
		unlockWork(getUser(), nbaCase);		
	}
	//END APSL4844
	
	
	/**
	 * Returns the source attached to the current work item. 
	 * @return
	 */
	//NBA331.1, APSL5055 New Method
	protected NbaSource getNbaSource() {
		return source;
	}

	/**
	 * Sets the source attached to the current work item. 
	 * @param nbaSource
	 */
	//NBA331.1, APSL5055 New Method
	protected void setNbaSource(NbaSource nbaSource) {
		source = nbaSource;
	}
	/**
	 * Returns the source's <code>WorkValues</code> attached to the current work item.
	 * The source's work values are lazy initialized and only created once. 
	 * @return
	 */
	//NBA331.1, APSL5055 New Method
	protected NbaLob getSourceLob() {
		if (sourceLob == null) {
			sourceLob = getNbaSource().getNbaLob();
		}
		return sourceLob;
	}
	
	/**
	 * Returns a list of check allocations associated with the specified check source.
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA331.1, APSL5055 New Method
    protected List<CheckAllocation> retrieveCheckAllocations() throws NbaBaseException {
		Result result = getCurrentBP().callService(ServiceCatalog.RETRIEVE_CHECK_ALLOC_DISASSEMBLER, getNbaSource());
		if (!result.hasErrors()) {
			result = getCurrentBP().invoke(ServiceCatalog.RETRIEVE_CHECK_ALLOCATIONS, result.getData());
		}		
		if (result.getData().isEmpty()) {
			result.addResult(createPrimaryCheckAllocation());
		} else {
			result.getData().add(0, createPrimaryCheckAllocation());
		}
		checkAllocations = result.getData();
		return checkAllocations;
	}

	/**
	 * Returns the <code>CheckAllocationDO</code> instance with the specified sequence
	 * number.
	 * @param sequence
	 * @return
	 */
	//NBA331.1, APSL5055 New Method
	protected CheckAllocation getCheckAllocation(int sequence) {
		for (CheckAllocation allocation : getCheckAllocations()) {
			if (allocation.getSequence() == sequence) {
				return allocation;
			}
		}
		return null;
	}

	/**
	 * Returns a list of check allocations associated with the current check.
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA331.1, APSL5055 New Method
	protected List<CheckAllocation> getCheckAllocations() {
		return checkAllocations;
	}
	
	/**
	 * set a list of check allocations associated with the current check.
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA331.1, APSL5055 New Method
	protected void setCheckAllocations(List<CheckAllocation> checkAllocations) {
		this.checkAllocations = checkAllocations;
	}
	
	/**
	 * Returns a new instance of a <code>CheckAllocation</code> representing the
	 * first/primary check allocation.  The details of the first check allocation
	 * are stored as work values on the check source workflow item.
	 * @param sourceValues
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA331.1, APSL5055 New Method
	protected CheckAllocation createPrimaryCheckAllocation() throws NbaBaseException {
		NbaLob sourceValues = getSourceLob();
		CheckAllocation allocation = new CheckAllocation();
		allocation.setSequence(1);
		allocation.setCompany(sourceValues.getCompany());
		allocation.setPolicyNumber(sourceValues.getPolicyNumber());
		allocation.setCwaAmount(sourceValues.getCwaAmount());
		if (sourceValues.getCostBasis() != 0) {
			allocation.setCostBasis(sourceValues.getCostBasis());
		}
		allocation.setPendingPaymentType(new Long(sourceValues.getPendPaymentType()));
		if (sourceValues.getPreviousTaxYear()) {
			allocation.setPreviousTaxYear(NbaConstants.TRUE);
		}
		if (!NbaUtils.isBlankOrNull(sourceValues.getInforcePaymentManInd())) {
			allocation.setInforcePaymentManInd(Integer.valueOf(sourceValues.getInforcePaymentManInd()));			
		}
		return allocation;
	}
}