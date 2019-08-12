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
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fs.dataobject.nba.cash.CheckAllocation;
import com.csc.fsg.nba.business.transaction.NbaInforcePaymentTransaction;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaWebClientFaultException;
import com.csc.fsg.nba.exception.NbaWebServerFaultException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.tableaccess.NbaCashieringTable;
import com.csc.fsg.nba.tableaccess.NbaCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractsWireTransferData;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;
/**
* NbaProcInforcePayment is the class that sends the inforce payment to web services for both Vantage and CyberLife administration systems.
* This class
*		- creates XML508 transaction 
*		- calls a VPMS model to determine te back end system and then sends the transaction via a web service.
*		- if the transaction fails, send the work item to the error queue
*		- if there is no error, update the Cashering and Wire Transfer tables
* <p>The NbaProcInforcePayment class extends the NbaAutomatedProcess class.  
* Although this class may be instantiated by any module, the NBA polling class 
* will be the primary creator of objects of this type.
* <p>When the polling process finds a case on the Manual Money queue MANLMONY, 
* it will create an object of this instance and call the object's 
* executeProcess(NbaUserVO, NbaDst) method.
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead> 
* <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
* <tr><td>NBA068</td><td>Version 3</td><td>Inforce Payment</td></tr>
* <tr><td>NBA108</td><td>Version 4</td><td>Vantage Inforce Payment</td></tr>
* <tr><td>NBA109</td><td>Version 4</td><td>Vantage Loan Payment</td></tr>
* <tr><td>SPR1890</td><td>Version 4</td><td>Removal of TransSubType from 508 transaction</td></tr>
* <tr><td>NBA095</td><td>Version 4</td><td>Queues Accept Any Work Type</td></tr>
* <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
* <tr><td>SPR2034</td><td>Version 4</td><td>VPMS Model Names should be capitalized in the jvpmsSettings.xml file</td></tr>
* <tr><td>SPR1775</td><td>Version 4</td><td>NBMNYDTM process error stops with the message  "Other error during nbA update" for Wire transfer Inforce Payment transaction</td></tr>
* <tr><td>SPR2366</td><td>Version 5</td><td>Added unique file names for webservice stubs</td></tr>
* <tr><td>NBA115</td><td>Version 5</td><td>Credit Card payment and authorization</td></tr>
* <tr><td>SPR2300</td><td>Version 5</td><td>"DepositEligibility" tage in configuration file changed to "apply"</td></tr>
* <tr><td>SPR2380</td><td>Version 5</td><td>Replaced NbaConfigurationConstants.DETERMINEADMINSYSTEM with NbaVpmsConstants.DETERMINEADMINSYSTEM</td></tr>
* <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
* <tr><td>SPR2775</td><td>Version 5</td><td>Money Determination generates Out of Balance error when NBPAYMENT indexed with more than 5 contracts</td></tr>
* <tr><td>SPR2922</td><td>Version 6</td><td>Inforce Payment Process Does Not Handle Error Returns - Work Items Move On in Workflow as if Applied Successfully</td></tr>
* <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
* <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
* <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
* <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
* <tr><td>AXAL3.7.12</td><td>AXA Life Phase 1</td><td>Cash Management</td></tr>
* <tr><td>ALS3046</td><td>AXA Life Phase 1</td><td>non-nbA NBPAYMENT work item indexed from the wholesale index queue flowed to the N2NBCMR queue instead of N2NBCMW queue</td></tr>
* <tr><td>SR615900</td><td>Discretionary</td><td>Prevent Checks From Being Deposited When CWA Not Applied</td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 2
* @see NbaAutomatedProcess
*/
public class NbaProcInforcePayment extends NbaAutomatedProcess {
	/** The Financial Activity object */
	private FinancialActivity finActivity = null;
	/** The NbaOinkDataAccess object which allows information to be retrived from NbaTXLife or NbaLob objects for predefined variables */
	private com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess oinkData;
	/** Vpms adopter object which provides interface into VPMS system */
	private com.csc.fsg.nba.vpms.NbaVpmsAdaptor vpmsProxy = null;
	private int contractCount = -1;  //NBA331.1
	/**
	 * NbaProcInforcePayment constructor comment.
	 */
	public NbaProcInforcePayment() {
		super();
		//SPR1851 code deleted
	}
	//SPR2639 method deleted
	/**
	 * This abstract method must be implemented by each subclass in order to
	 * execute the automated process. This method creates Financial Activity Object if status is "PAYTDTMND". Then it calls to VPMS
	 * model to determine the back end system. After that it calls adapter for Cyberlife and Webservice for Vantage and passes
	 * xml508 transaction. It sends work item to error queue if the transaction fails otherwise updates the Cashering and Wire 
	 * Transfer tables.
	 * @param user the user/process for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// NBA095 code deleted
		if (!initialize(user, work)) {
			return getResult();
		}
		//NBA213 deleted code
			//retrieve the complete work item
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(getWork().getID(), true);
			retOpt.requestSources();
			retOpt.setLockWorkItem();
			setWork(retrieveWorkItem(user, retOpt));  //NBA213
			NbaSource source = (NbaSource) getWork().getNbaSources().get(0);
			//Create financial activity object if status is PAYDTMND
			String status = getWork().getStatus(); //NBA115
			if (A_STATUS_PAYMENT_DETERMINDED.equalsIgnoreCase(status) || A_STATUS_CREDIT_CARD_INFPAY_ACCEPTED.equalsIgnoreCase(status)) { //NBA115
				//				call to VPMS model to determine backend system
				NbaVpmsResultsData nbaVpmsResultsData = getDataFromVpms(NbaVpmsAdaptor.EP_GET_ADMINISTRATION_SYSTEM);
				if (((String) nbaVpmsResultsData.resultsData.get(0)).compareToIgnoreCase("Administration System could not be determined") == 0) {
					getLogger().logDebug("Inforce Payment failed: Administration System could not be determined");
					addComment("Inforce Payment failed: Administration System could not be determined");
					setResult(
						new NbaAutomatedProcessResult(
							NbaAutomatedProcessResult.FAILED,
							"Administration System could not be determined",
							getFailStatus()));
				}
				//	if Administration System could not be determined, change the status to HOSTERRD
				if (getResult() != null) {
					changeStatus(getResult().getStatus());
					doUpdateWorkItem();
					return getResult();
				}
				work.getNbaLob().setBackendSystem((String) nbaVpmsResultsData.resultsData.get(0));
				//create 508 transaction
				NbaTXLife txLifeInforcePaymentResp = null;
				NbaLob lob = null;
				setNumberOfContracts(source);  //NBA331.1, APSL5055
				if (numberOfContracts() > 1 || getSourceType().equals(NbaConstants.A_ST_XML508)) {
					lob = work.getNbaLob();
				} else {
					lob = source.getNbaLob();
				}
				try {
					NbaInforcePaymentTransaction inforcePaymentTrans = new NbaInforcePaymentTransaction();
					NbaTXLife txLife = inforcePaymentTrans.createTXLife508(work, lob, user);
					txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).deleteTransSubType();//SPR1890
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Outgoing 508 transaction " + txLife.toXmlString());
					}

					NbaWebServiceAdapter service =
						NbaWebServiceAdapterFactory.createWebServiceAdapter(work.getNbaLob().getBackendSystem(), "Payment", "InfPayment");
					txLifeInforcePaymentResp = service.invokeWebService(txLife);//SPR2366 //SPR2968

					if (getLogger().isDebugEnabled()) {//NBA108 NBA109
						getLogger().logDebug("Back from WebService Inforce Payment" + txLifeInforcePaymentResp.toXmlString());//NBA108 NBA109
					}//NBA108 NBA109
					
					if (txLifeInforcePaymentResp.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseCount() > 0) {
						TransResult transResult = txLifeInforcePaymentResp.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();//NBA108 NBA109
						if (transResult.getResultCode() == NbaOliConstants.TC_RESCODE_FAILURE) {//NBA108 NBA109 line changed
							getLogger().logDebug("Inforce Payment failed: Error returned from WebService ");
							//NBA108 NBA109 line deleted
							//begin NBA108 NBA109
							if (transResult.getResultInfoAt(0).hasResultInfoDesc()) {
								addComment(transResult.getResultInfoAt(0).getResultInfoDesc());
							} else if (transResult.getResultInfoAt(0).hasResultInfoCode()) {
								addComment(
									NbaTransOliCode.lookupText(
										NbaOliConstants.RESULT_INFO_CODES,
										transResult.getResultInfoAt(0).getResultInfoCode()));
							}
							if (transResult.getResultInfoAt(0).hasResultInfoCode()) {
								if (transResult.getResultInfoAt(0).getResultInfoCode() == NbaOliConstants.TC_RESINFO_GENERALDATAERR) {
									if (getLogger().isErrorEnabled()) {
										getLogger().logError("Vantage error returned from WebService");
									}
									throw new NbaWebClientFaultException(NbaWebClientFaultException.VANTAGE_ERROR);
								} else if (transResult.getResultInfoAt(0).getResultInfoCode() == NbaOliConstants.TC_RESINFO_ELEMENTMISSING) {
									if (getLogger().isErrorEnabled()) {
										getLogger().logError("Missing element error returned from Vantage WebService");
									}
									throw new NbaWebClientFaultException(NbaWebClientFaultException.MISSING_ELEMENT_ERROR);
								} else if (transResult.getResultInfoAt(0).getResultInfoCode() == NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL) {
									if (getLogger().isFatalEnabled()) {
										getLogger().logFatal("System not available message returned from WebService");
									}
									throw new NbaWebServerFaultException(NbaWebServerFaultException.SYSTEM_NOT_AVAILABLE_ERROR);
								} else if (transResult.getResultInfoAt(0).getResultInfoCode() == NbaOliConstants.TC_RESINFO_GENERALERROR) {
									if (getLogger().isFatalEnabled()) {
										getLogger().logFatal("Xml gateway application error occured");
									}
									throw new NbaWebServerFaultException(NbaWebServerFaultException.XML_GATEWAY_APP_ERROR);
									//begin SPR1775 - Rest of the error codes would be related to CLIF only
								} else { 
									if (getLogger().isFatalEnabled()) {
										getLogger().logError("CLIF Error: " + transResult.getResultInfoAt(0).getResultInfoDesc());										
									}
									throw new NbaWebClientFaultException(transResult.getResultInfoAt(0).getResultInfoDesc());
									//end SPR1775									 
								}
							//begin SPR2922	
							//If the result is a failure but the ResultInfoCode is missing, an error should still be thrown							
							}else { 
								if (getLogger().isFatalEnabled()) {
									getLogger().logError("ResultInfoCode not set " + transResult.getResultInfoAt(0).getResultInfoDesc());										
								}
								throw new NbaWebClientFaultException(transResult.getResultInfoAt(0).getResultInfoDesc());
							}
							//end SPR2922	
						} else if (transResult.getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESSINFO) {
								if (transResult.getResultInfoAt(0).hasResultInfoDesc()) {
									addComment(transResult.getResultInfoAt(0).getResultInfoDesc());
								} else if (transResult.getResultInfoAt(0).hasResultInfoCode()) {
									addComment(
										NbaTransOliCode.lookupText(
											NbaOliConstants.RESULT_INFO_CODES,
											transResult.getResultInfoAt(0).getResultInfoCode()));
								}
							}
						//end NBA108 NBA109
					}
				} catch (NbaWebClientFaultException e) {//NBA108 NBA109 line changed
					//begin NBA108 NBA109
					String resultDesc = "NbaWebClientFaultException occured due to data problem";
					if (getLogger().isErrorEnabled()) {
						getLogger().logError(resultDesc);
						getLogger().logError(e);
					}								
					//end NBA108 NBA109
					//NBA108 NBA109 line deleted
					setResult(
						new NbaAutomatedProcessResult(
							NbaAutomatedProcessResult.FAILED,
							resultDesc,
							getHostErrorStatus()));
					addComment(resultDesc); //NBA103
				}
				//NBA108 NBA109 code deleted

				//if there is a host error, change the status to HOSTERRD
				if (getResult() != null) {
					changeStatus(getResult().getStatus());
					doUpdateWorkItem();
					return getResult();
				} else {
					//Update AppliedIndicator
					setAppliedIndicator();
				}
			} else {
				//Update AppliedIndicator for IFMANPAYAP status
				setAppliedIndicator();
				setStatusProvider(new NbaProcessStatusProvider(getUser(), getWork(), getNbaTxLife(), getDeOinkMapForSources(getWork()))); //AXAL3.7.12
			}
			//The status needs to be changed to IFPAYAPLID
			if (getResult() == null) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}
			//Change the status
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
		//NBA213 deleted code
		return getResult();
	}
	
	// set deOink map for scan station
	//AXAL3.7.12 added new method
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
	
	
	/**
	 * Retrieve info about check
	 * @return com.csc.fsg.nba.tableaccess.NbaCheckData
	 */
	protected NbaCheckData getCheckData() {
		try {
			NbaLob lob = null;
			NbaSource source = (NbaSource) getWork().getNbaSources().get(0);
			if (numberOfContracts() > 1 || getSourceType().equals(NbaConstants.A_ST_XML508)) {
				lob = getWork().getNbaLob();
			} else {
				lob = source.getNbaLob();
			}
			NbaCashieringTable table = new NbaCashieringTable();
			if (lob.getCwaAmount() == 0.0) {
				lob.setCwaAmount(lob.getCheckAmount());
			}
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
			getLogger().logError("Inforce Payment get check data failed: " + nbe.getMessage());
			return null;
		} catch (Exception e) {
			getLogger().logError("Inforce Payment get check data failed: " + e.getMessage());
			return null;
		}
	}
	/**
	 * This method returns the credit card source
	 * @return NbaSource
	 */
	//New Method NBA115
	protected NbaSource getCreditCardSource() {
		NbaSource source = null;
		if (getWork().getNbaSources().size() > 0) {
			source = (NbaSource) getWork().getNbaSources().get(0);
			if(source.isCreditCard()) {
				return source;
			}
		}
		return source;
	}
	/**
	* This method sets the applied indicator for credit card payments.
	*/
   //New Method NBA115
   protected void setCreditCardAppliedIndicator() {
	   try {
		   NbaCreditCardData.updateCCAppliedPayment(getWork().getNbaLob().getCCTransactionId());					
	   } catch (NbaBaseException nbe) {
		   addComment("Database error while updating the credit card applied indicator(" + nbe.getMessage() + ")");
		   changeStatus(getSqlErrorStatus());
		   setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Credit Card SQL Error", getSqlErrorStatus()));
	   }

   }	
	
	/**
	 * This method returns the type of source that is attached to the work item
	 * @return java.lang.String Source type
	 */
	protected String getSourceType() {
		if (getWork().getNbaSources().size() > 0) {
			NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);
			return nbaSource.getSource().getSourceType();
		} else {
			return null;
		}
	}
	/**
	 * Depending on whether attached source is a check or a wire tranfer, this method 
	 * will call the appropriate method
	 */
	protected void setAppliedIndicator() {
		String sourceType = getSourceType();
		if(sourceType == null || sourceType.equals(NbaConstants.A_ST_CREDIT_CARD)){//NBA115
			setCreditCardAppliedIndicator();//NBA115
		} else if (sourceType.equals(NbaConstants.A_ST_CWA_CHECK) || sourceType.equals(NbaConstants.A_ST_PAYMENT)) { //NBA115
			updateContractToCheck();
		} else if (sourceType.equals(NbaConstants.A_ST_XML508)) {
			updateWireTransfer();
		}
	}
	/**
	 * This will see if the check is marked as deposited in the Check table. 
	 * If yes, write an error comment in AWD and send the work item to error queue
	 * If no, set the Applied Indicator to true in the contratcs_checks table. 
	 */
	protected void updateContractToCheck() {
		NbaCheckData check = getCheckData();
		if (check == null) {
			addComment("Inforce Payment failed: Check information not found in database");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Cashiering SQL Error", getSqlErrorStatus()));
			return;
		}
		String dbYear=null;
		if (check.getDepositTimeStamp() != null) {
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		    dbYear = df.format(check.getDepositTimeStamp());
		}
		if (check.getDepositTimeStamp() == null || (dbYear!=null && dbYear.endsWith("1900"))) { //APSL5289
			try {
				NbaLob lob = getWork().getNbaLob();
				NbaCashieringTable table = new NbaCashieringTable();
				//Set the values from LOB
				StringBuffer query = new StringBuffer("UPDATE NBA_CONTRACTS_CHECKS SET ");
				// begin SPR2992
				query.append(NbaCashieringTable.formatSQLUpdateValue(NbaCashieringTable.APPLIED_IND, true));
				query.append(" , " + NbaCashieringTable.formatSQLUpdateValue(NbaCashieringTable.SYSTEM_APPLIED_IND, true)); // SR615900
				query.append(" , " + NbaCashieringTable.formatSQLUpdateValue(NbaCashieringTable.INFORCE_PAYMENT_IND, 1));
				query.append(" WHERE " + NbaCashieringTable.formatSQLWhereCriterion(NbaCashieringTable.BUNDLE_ID, lob.getBundleNumber()));
				query.append(" AND " + NbaCashieringTable.formatSQLWhereCriterion(NbaCashieringTable.COMPANY, lob.getCompany()));
				query.append(" AND " + NbaCashieringTable.formatSQLWhereCriterion(NbaCashieringTable.CONTRACT_NUMBER, lob.getPolicyNumber()));
				query.append(
					" AND " + NbaCashieringTable.formatSQLWhereCriterion(NbaCashieringTable.AMOUNT_APPLIED, new BigDecimal(Double.toString(lob.getCwaAmount()))));
				query.append(" AND " + NbaCashieringTable.TECHNICAL_KEY);
				query.append(" IN (SELECT DISTINCT TECHNICAL_KEY FROM NBA_CHECKS WHERE ");
				query.append(NbaCashieringTable.formatSQLWhereCriterion(NbaCashieringTable.BUNDLE_ID, lob.getBundleNumber()));
				query.append(
					" AND " + NbaCashieringTable.formatSQLWhereCriterion(NbaCashieringTable.CHECK_AMT, new BigDecimal(Double.toString(lob.getCheckAmount()))));
				query.append(" AND " + NbaCashieringTable.formatSQLWhereCriterion(NbaCashieringTable.CHECK_NUMBER, lob.getCheckNumber()));
				// end SPR2992
				query.append(")");
				table.executeUpdate(query.toString());
			} catch (NbaBaseException nbe) {
				addComment("Inforce Payment failed: Database error while updating the check's applied indicator(" + nbe.getMessage() + ")");
				changeStatus(getSqlErrorStatus());
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Cashiering SQL Error", getSqlErrorStatus()));
			}
		} else {
			addComment("Inforce Payment failed: Check has already been deposited, the Applied indicator cannot be changed");
			try {
				//Read the value from the configuration to send the workitemt o error queue
				String depositIndicator = NbaConfiguration.getInstance().getCashiering().getDepositEligibility(); //ACN012
				if (depositIndicator != null && depositIndicator.trim().equalsIgnoreCase(DEPOSIT_INDICATOR_APPLIED)) { //SPR2300
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Cashiering Error", getOtherStatus())); //SPR2300
				}
			} catch (NbaBaseException e) {
				addComment("Inforce Payment failed: Error while reading deposit indicator(" + e.getMessage() + ")");
				changeStatus(getFailStatus());
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Cashiering Error", getFailStatus()));
			}
		}
	}
	/**
	 * This method updates Applied Indicator in the Wire transfer table
	 */
	protected void updateWireTransfer() {
		try {
			NbaLob lob = null;
			NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);
			if (numberOfContracts() > 1 || getSourceType().equals(NbaConstants.A_ST_XML508)) {
				lob = getWork().getNbaLob();
			} else {
				lob = nbaSource.getNbaLob();
			}
			NbaTXLife nbaTXlife = new NbaTXLife(nbaSource.getText());
			java.util.Date effDate = nbaTXlife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransExeDate();
			//Set the values from LOB	
			NbaContractsWireTransferData.updateAppliedInd(true, effDate, lob.getCompany(), lob.getPolicyNumber());
			//update the inforce indicator
			updateInforceInd(effDate, lob.getCompany(), lob.getPolicyNumber());
		} catch (NbaBaseException nbe) {
			addComment("Inforce Payment failed: Database error while updating the Applied indicator(" + nbe.getMessage() + ")");
			changeStatus(getSqlErrorStatus());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Cashiering SQL Error", getSqlErrorStatus()));
		} catch (Exception e) {
			addComment("Invalid XML");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getOtherStatus()));
		}
	}
	/**
	 * This method calls a VPMS model which responds with the
	 * backend system to send this payment to.
	 * @return NbaVpmsResultsData containing the backend system
	 * @param entryPoint is the entryPoint for VPMS model.
	 * @throws NbaBaseException
	 */
	public NbaVpmsResultsData getDataFromVpms(String entryPoint) throws com.csc.fsg.nba.exception.NbaBaseException {
	    NbaVpmsAdaptor vpmsProxy = null; //SPR3362
	    try {
			getLogger().logDebug("Starting Retrieval of data from VPMS model");
			//second parameter is the name of the model to be executed.
			oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.DETERMINEADMINSYSTEM); //SPR2034 SPR2380 SPR3362
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			//SPR3362 code deleted
			return data;
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("Problem in getting data from VPMS", re);
        //SPR3362
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
	/**
	 * Update the inforce indicator for a wire transfer.
	 * @param inforceInd int
	 * @param effDate java.util.Date
	 * @param company java.lang.String
	 * @param contractNumber java.lang.String
	 */
	public static void updateInforceInd(java.util.Date effDate, String company, String contractNumber)
		throws NbaBaseException, NbaDataAccessException {
		NbaCashieringTable table = new NbaCashieringTable();
		StringBuffer query = new StringBuffer("UPDATE NBA_CONTRACTS_WIRE_TRANSFER SET ");
		query.append(NbaCashieringTable.formatSQLUpdateValue(NbaCashieringTable.INFORCE_PAYMENT_IND, 1)); //SPR2992
		query.append(" WHERE " + NbaCashieringTable.formatSQLWhereCriterion(NbaCashieringTable.COMPANY, company)); //SPR2992
		query.append(" AND " + NbaCashieringTable.formatSQLWhereCriterion(NbaCashieringTable.CONTRACT_NUMBER, contractNumber)); //SPR2992
		query.append(" AND " + NbaCashieringTable.TECHNICAL_KEY);
		query.append(" IN (SELECT TECHNICAL_KEY FROM NBA_WIRE_TRANSFER WHERE ");
		query.append(table.formatSQLWhereCriterion(NbaCashieringTable.WIRE_TRANSFER_EFFDATE, effDate));
		query.append(")");
		table.executeUpdate(query.toString());
	}
	/**
	 * This method returns the numbers of contracts associated with the check
	 * @return count
	 *//*
	protected int numberOfContracts() {
		int count = 0;
		NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);
		//begin SPR2775
		int i = 1;
		while (nbaSource.getNbaLob().getPolicyNumberAt(i) != null && nbaSource.getNbaLob().getPolicyNumberAt(i).length() > 0) {
			count = count + 1;
			i++;
		}
		//end SPR2775
		return count;
	}*/
	
	/**
	 * This method returns the numbers of contracts associated with the check
	 * @return count
	 */
	protected int numberOfContracts() {
		return contractCount;  //NBA331.1
	}

	/**
	 * Calculates the number of contracts associated with this check by retrieving all
	 * check allocations.
	 * @param source
	 * @throws NbaBaseException
	 */
	//NBA331.1, APSL5055 New Method
	protected void setNumberOfContracts(NbaSource source) throws NbaBaseException {
		contractCount = 0;
		NbaLob sourceWorkValues = source.getNbaLob();
		String policyNumber = sourceWorkValues.getPolicyNumber();
		if (policyNumber != null && policyNumber.length() > 0) {
			contractCount++;
	}
		for (CheckAllocation allocation : retrieveCheckAllocations(source)) {
			if (allocation.getPolicyNumber() != null && !allocation.getPolicyNumber().isEmpty()) {
				contractCount++;
			}
		}
	}

	/**
	 * Returns a list of check allocations associated with the specified check source.
	 * @param source
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA331.1, APSL5055 New Method
	protected List<CheckAllocation> retrieveCheckAllocations(NbaSource nbaSource) throws NbaBaseException {
		Result result = getCurrentBP().callService(ServiceCatalog.RETRIEVE_CHECK_ALLOC_DISASSEMBLER, nbaSource);
		if (!result.hasErrors()) {
			result = getCurrentBP().invoke(ServiceCatalog.RETRIEVE_CHECK_ALLOCATIONS, result.getData());
		}		
		return result.getData();
	}
	
}
