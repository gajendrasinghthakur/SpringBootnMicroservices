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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDatabaseLockedException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Payment;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * NbaProcElectronicPaymentDraft is the class that processes nbAccelerator cases found on the AWD electronic payment draft queue (N2PYDRFT). The purpose of
 * this step in the workflow is to determine if money is nbA eletcronic money for routing, call the ACH interfaces and update the WI accordingly as per
 * response from ACH interface.
 * <p>
 * The NbaProcElectronicPaymentDraft class extends the NbaAutomatedProcess class. Although this class may be instantiated by any module, the NBA polling
 * class will be the primary creator of objects of this type.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th> <th align=left>Release</th> <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>APSL2735</td> <td>Discretionary</td> <td>Electronic Initial Payment</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaProcElectronicPaymentDraft extends NbaAutomatedProcess {
	private String awdTime;
	private NbaDst parentCase = null;
	protected static int MIN = 15;
	
	static {
		try {
			MIN = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
					NbaConfigurationConstants.ELECTRONIC_PAYMENT_SUSPEND_MINS));
		} catch (Exception ex) { }
	}
	
	
	/**
	 * This method drive the Electronic Payment Draft Automated process (A2PYDRFT).
	 * NBCWA WI - It will first call the submitAchPayment web service and if the response is failure due to data error, it will 
	 *          - aggregate the CWA WI for UWCM. If the failure response is due to any other reason, it will suspend the CWA WI for 15 minute
	 * 			- and again after 15 minutes, this will first call to inquireAchPayment payment and if payment is found it will move the CWA WI
	 * 			- with pass status. If  response from inquireAchPayment is failure (Payment Not Found) it will again call submitAchPayment
	 * 			- and if this time response submitAchPayment is failure, it will error stop the poller and if response is success, CWA WI will be
	 * 			- moved to next queue. 
	 * @param user the NbaUser for whom the process is being executed
	 * @param work a NbaWorkItem value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 * @throws NbaBaseException
	 * @throws NbaAWDLockedException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {
		if (!initialize(user, work)) {
			return getResult();
		}
		try {
			
			if (NbaConstants.A_WT_CWA.equalsIgnoreCase(getWork().getWorkType())) {
				//Begin APSL3836
				if (NbaUtils.isInitialPremiumPaymentForm(nbaTxLife)
						&& String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT).equalsIgnoreCase(work.getNbaLob().getPaymentMoneySource())) {
					// Begin APSL5164
					Date currentdate = new Date();
					if (NbaUtils.isVariableProduct(nbaTxLife) && !NbaUtils.isBlankOrNull(getWork().getNbaLob().getCwaDate())
							&& NbaUtils.compare(getWork().getNbaLob().getCwaDate(), currentdate) == 0) {
						if (!NbaUtils.isBlankOrNull(getWork().getNbaLob().getCwaTime())) {
							String cwadateString = getWork().getNbaLob().getCwaTime();
							try {
								if (NbaUtils.isDateAfterTodays4PM(cwadateString)) {
									Calendar calendar = new GregorianCalendar();
									calendar.setTime(currentdate);// convert in awd format if required
									calendar.add(Calendar.DATE, 1);
									calendar.set(Calendar.HOUR_OF_DAY, 0);
									calendar.set(Calendar.MINUTE, 0);
									calendar.set(Calendar.SECOND, 0);
									NbaSuspendVO suspendVO = new NbaSuspendVO();
									suspendVO.setCaseID(getWork().getID());
									suspendVO.setActivationDate(calendar.getTime());
									suspendWork(suspendVO);
									setStatusProvider(getProcesStatusProvider(getWork().getNbaLob(), nbaTxLife));
									changeStatus(getStatusProvider().getPassStatus(), getStatusProvider().getReason());
									setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
									updateWork(getUser(), getWork());
									return getResult();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					// End APSL5164
					if(!nbaTxLife.isUnderwriterApproved()){
						if(nbaTxLife.getPolicy()!=null && nbaTxLife.getPolicy().getApplicationInfo()!=null && nbaTxLife.getPolicy().getApplicationInfo().getCWAAmt()>0){
							addComment("Initial payment already present on the case. Possible duplicate payment");
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getOtherStatus()));
							changeStatus(getResult().getStatus());
							updateWork(getUser(), getWork());
							return getResult();
						}
					}
				}
				//End APSL3836
				boolean invokeFlag = false, submitFlag = false; //APSL4768
				if (getWork().getNbaLob().getSuspendIndicator()) {
					invokeFlag = invokeInquireAchWebService();
				} else {
					submitFlag = invokeSubmitAchWebService();
				}
				if(invokeFlag || submitFlag){
					processMoneyRequirement();//APSL4768
				}
				doContractUpdate();//APSL4768
			}
			
			if(NbaConstants.A_WT_SERVICE_RESULT.equalsIgnoreCase(getWork().getWorkType())){
				processServiceResult();
				NbaContractLock.removeLock(getUser());
			}
						
		} catch (NbaAWDLockedException le) {
			unlockWork();
			NbaContractLock.removeLock(getUser());
			if (getLogger().isDebugEnabled()) {
				getLogger().logError(le);
				le.printStackTrace();
			}
		} catch (NbaDatabaseLockedException dble) {
			NbaLockedException nle = new NbaLockedException(dble.getMessage());
			if (getLogger().isDebugEnabled()) {
				getLogger().logError(nle);
				nle.printStackTrace();
			}
			throw nle;
		}catch (NbaBaseException e) {
			NbaContractLock.removeLock(getUser());
			if(getLogger().isDebugEnabled()){
				getLogger().logError(e);
				e.printStackTrace();
			}
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			throw new NbaBaseException(e.getMessage(), NbaExceptionType.FATAL);
		}
		return getResult();
	}
	
	/**
	 * @return Returns the awdTime.
	 */
	protected String getAwdTime() {
		return awdTime;
	}

	/**
	 * @param awdTime The awdTime to set.
	 */
	protected void setAwdTime(String awdTime) {
		this.awdTime = awdTime;
	}

	protected Date parseTimeStamp(String dateForm) {
		try {
			java.util.Date date = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").parse(dateForm);
			return date;
		} catch (java.text.ParseException e) {
			return new Date();
		}
	}
	
	/**Takes activate minutes for a workitem, and returns suspendVO object for this work item.
	 * @throws NbaBaseException
	 */
	protected NbaSuspendVO getSuspendWorkVO(int type, int value) {
		GregorianCalendar cal = new GregorianCalendar();
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		cal.setTime(parseTimeStamp(getAwdTime()));
		cal.add(type, value);
		suspendVO.setTransactionID(getWork().getID());
		suspendVO.setActivationDate(cal.getTime());
		return suspendVO;
	}

	protected boolean invokeInquireAchWebService() throws NbaBaseException { // APSL4768
		setAwdTime(getTimeStamp(user));
		FinancialActivity ficAcct = getFinancialActivityObject(AxaWSConstants.WS_OP_INQUIRE_ACHPAYMENT);
		AxaWSInvoker wsInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_INQUIRE_ACHPAYMENT, user, nbaTxLife, getWork(), ficAcct);
		NbaTXLife nbaTXLifeResponse = (NbaTXLife) wsInvoker.execute();
		TransResult transResult = nbaTXLifeResponse.getTransResult();
		long resultCode = transResult.getResultCode();
		if (NbaOliConstants.TC_RESCODE_SUCCESS == resultCode) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			getWork().getNbaLob().setPaymentRefNumber(ficAcct.getReferenceNo()); //NBLXA-1631
			getWork().getNbaLob().setPaymentConfNumber(transResult.getConfirmationID()); //NBLXA-1631
			changeStatus(getResult().getStatus());
			updateWork(getUser(), getWork());
			return true;
		} else if (NbaOliConstants.TC_RESCODE_FAILURE == resultCode) {
			handleInquireAchFailureResponse(transResult);
		}
		return false;
	}
	
	protected boolean invokeSubmitAchWebService() throws NbaBaseException { // APSL4768
		FinancialActivity ficAcct = getFinancialActivityObject(AxaWSConstants.WS_OP_SUBMIT_ACHPAYMENT);
		try{ // NBLXA-1631
		setAwdTime(getTimeStamp(user));
		AxaWSInvoker wsInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SUBMIT_ACHPAYMENT, user, nbaTxLife, getWork(), ficAcct);
		NbaTXLife nbaTXLifeResponse = (NbaTXLife) wsInvoker.execute();
		TransResult transResult = nbaTXLifeResponse.getTransResult();
		long resultCode = transResult.getResultCode();
		if (NbaOliConstants.TC_RESCODE_SUCCESS == resultCode) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			getWork().getNbaLob().setPaymentRefNumber(ficAcct.getReferenceNo());
			getWork().getNbaLob().setPaymentConfNumber(transResult.getConfirmationID());
			getWork().setUpdate();
			changeStatus(getResult().getStatus());
			updateWork(getUser(), getWork());
			return true;
		} else if (NbaOliConstants.TC_RESCODE_FAILURE == resultCode) {
			handleSubmitAchFailureResponse(transResult, ficAcct);
		}
		}
		// NBLXA-1631 Begin
		catch(NbaBaseException exp){
			getWork().getNbaLob().setSuspendIndicator(true);
			getWork().getNbaLob().setPaymentRefNumber(ficAcct.getReferenceNo());
			getWork().setUpdate();
			setSuspendVO(getSuspendWorkVO(Calendar.MINUTE, MIN));
			updateForSuspend();
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			throw new NbaBaseException(exp.toString(), NbaExceptionType.FATAL);
		}
		//NBLXA-1631 End
		return false;
	}
	
	protected void handleSubmitAchFailureResponse(TransResult transResult, FinancialActivity ficAcct) throws NbaBaseException {
			//check if data error occured
			ResultInfo resultInfo = transResult != null && !transResult.getResultInfo().isEmpty() ? transResult.getResultInfoAt(0) : null;
			if(resultInfo != null && NbaOliConstants.TC_RESINFO_GENERALDATAERR == resultInfo.getResultInfoCode()){
				addComment("Data error has occurred while sending payment draft request, "+getErrorMessage(resultInfo));
				// aggregate WI
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
				getWork().getNbaLob().setSuspendIndicator(false);
				getWork().getNbaLob().deletePaymentConfNumber();
				getWork().getNbaLob().deletePaymentRefNumber();
				getWork().setUpdate();
				changeStatus(getResult().getStatus());
				updateWork(getUser(), getWork());
			} else if (getWork().getNbaLob().getSuspendIndicator()){ //already suspended WI once
				getWork().getNbaLob().setSuspendIndicator(false);
				getWork().getNbaLob().deletePaymentConfNumber();
				getWork().getNbaLob().deletePaymentRefNumber();
				getWork().setUpdate();
				updateWork();
				handleWebServiceFailureResponse(transResult, AxaWSConstants.WS_OP_SUBMIT_ACHPAYMENT);
			} else { //suspend WI
				addComment(getErrorMessage(resultInfo)+"\n suspending for "+MIN+" minutes");
				getWork().getNbaLob().setSuspendIndicator(true);
				getWork().getNbaLob().setPaymentRefNumber(ficAcct.getReferenceNo());
				getWork().getNbaLob().setPaymentConfNumber(transResult.getConfirmationID());
				getWork().setUpdate();
				setSuspendVO(getSuspendWorkVO(Calendar.MINUTE, MIN));
				updateForSuspend();
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
			}
	}
	
	
	protected void handleInquireAchFailureResponse(TransResult transResult) throws NbaBaseException {
		//check if payment not found
		ResultInfo resultInfo = transResult != null && !transResult.getResultInfo().isEmpty() ? transResult.getResultInfoAt(0) : null;
		if (resultInfo != null && NbaOliConstants.TC_RESINFO_FUNDNOTFOUND  == resultInfo.getResultInfoCode()) { //Payment not found, retrigger new
			boolean submitFlag = invokeSubmitAchWebService(); 																		 // submitAchPayment
			if(submitFlag){ //APSL4768
				processMoneyRequirement();
			}
		} else {
			getWork().getNbaLob().setSuspendIndicator(false);
			getWork().getNbaLob().deletePaymentConfNumber();
			getWork().getNbaLob().deletePaymentRefNumber();
			getWork().setUpdate();
			updateWork();
			handleWebServiceFailureResponse(transResult, AxaWSConstants.WS_OP_INQUIRE_ACHPAYMENT);
		}
	}
	/**
	 * Generates PaymentReferenceID
	 * 
	 * @return PaymentReferenceID in format PolNumberyyyyMMddHHmmssSSS
	 */
	protected String getPaymentReferenceID(String operationName) {
		if (AxaWSConstants.WS_OP_SUBMIT_ACHPAYMENT.equalsIgnoreCase(operationName)) {
			StringBuffer paymentRef = new StringBuffer();
			paymentRef.append(getNbaTxLife().getPolicy().getPolNumber());
			paymentRef.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
			return paymentRef.toString();
		}
		return getWork().getNbaLob().getPaymentRefNumber();
	}

	private FinancialActivity getFinancialActivityObject(String operationName) {
		FinancialActivity finAcct = new FinancialActivity();
		finAcct.setId("FinancialActivity_1");
		finAcct.setReferenceNo(getPaymentReferenceID(operationName));
		if (AxaWSConstants.WS_OP_INQUIRE_ACHPAYMENT.equals(operationName)) {
			OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
			oliExt.getFinancialActivityExtension().setConfirmationID(getWork().getNbaLob().getPaymentConfNumber());
			finAcct.addOLifEExtension(oliExt);
		}
		return finAcct;
	}
	
	private void handleWebServiceFailureResponse(TransResult transResult, String OperationName) throws NbaBaseException {
		StringBuffer errorString = new StringBuffer();
		List resultInfoList = transResult.getResultInfo();
		if (resultInfoList != null && resultInfoList.size() > 0) {
			for (int i = 0; i < resultInfoList.size(); i++) {
				ResultInfo resultInfo = (ResultInfo) resultInfoList.get(i);
				if (i > 0){
					errorString.append(" Error count : " + i);
				}
				errorString.append(getErrorMessage(resultInfo));
				errorString.append("\n");
			}
			throw new NbaBaseException("Failure response from : [Operation Name - "+OperationName+"] "+ errorString.toString(), NbaExceptionType.FATAL);
		}
	}
	
	private String getErrorMessage(ResultInfo resultInfo) {
		StringBuffer errorString = new StringBuffer();
		if(resultInfo != null){
			long resultInfoCode = resultInfo.getResultInfoCode();
			if (!NbaUtils.isBlankOrNull(resultInfoCode))
				errorString.append(" Error Code : (" + resultInfoCode + ") "
						+ NbaTransOliCode.lookupText(NbaOliConstants.RESULT_INFO_CODES, resultInfoCode) + "\n");
			if (!NbaUtils.isBlankOrNull(resultInfo.getResultInfoDesc()))
				errorString.append(" Error Desc : " + resultInfo.getResultInfoDesc());
		}
		return errorString.toString();
	}
	
	
	protected NbaDst retrieveWorkItem(NbaDst nbaDst) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("A2PYDRFT Starting retrieveWorkItem for " + nbaDst.getID());
		}
		NbaAwdRetrieveOptionsVO retrieveOptions = new NbaAwdRetrieveOptionsVO();
		retrieveOptions.setWorkItem(nbaDst.getID(), false);
		retrieveOptions.requestSources();
		return retrieveWorkItem(getUser(), retrieveOptions);
	}
	
	/**
	 * Search for Case work items which can be matched to the current work item.
	 * @return the search value object containing the results of the search
	 * @throws NbaBaseException
	 * @throws RemoteException
	 * @throws NbaNetServerDataNotFoundException
	 * @throws NbaVpmsException
	 */
	
	protected NbaTXLife lookup508() throws NbaBaseException {
		NbaTXLife aNbaTXLife = null;
		try {
			//Retrieve NBAPPLCTN with all NbaSource objects. Records in NBA_SYSTEM_DATA are also retrieved.
			NbaDst aWork = retrieveWorkItem(work);
			List sources = aWork.getNbaSources();
			NbaSource aSource = null;
			//Look for Predictive response
			for (int i = 0; i < sources.size(); i++) {
				aSource = (NbaSource) sources.get(i);
				if (aSource.getSourceType().equalsIgnoreCase(NbaConstants.A_ST_XML508)) {
					break;
				}
			}
			//Policy number is in Remittance request which will be key to find case.
			aNbaTXLife = new NbaTXLife(aSource.getText());
			String policyNumber = aNbaTXLife.getPolicy().getPolNumber();
			NbaSearchVO searchVO = new NbaSearchVO();
			searchVO.setResultClassName("NbaSearchResultVO");
			searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
			searchVO.setContractNumber(policyNumber);
			searchVO = lookupWork(getUser(), searchVO);
			List searchResult = searchVO.getSearchResults();
			if (searchResult.size() == 0) {
				getWork().getNbaLob().setPolicyNumber(policyNumber);
				addComment(getFailureComment(aNbaTXLife));
				getWork().setUpdate();
				updateWork();
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
				throw new NbaBaseException("No Matching case found for failure notification contract#"+ policyNumber, NbaExceptionType.FATAL);
			}
			//Obtain lock on NBAPPLCTN.
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(((NbaSearchResultVO) searchVO.getSearchResults().get(0)).getWorkItemID(), true);
			retOpt.setLockWorkItem();
			parentCase = retrieveWorkItem(getUser(), retOpt);
			NbaContractLock.requestLock(parentCase, getUser()); 
			populateLOBsOnServiceResult(aNbaTXLife);//copy LOBs from parent OR txlife
			parentCase.getNbaCase().addNbaTransaction(getWork().getNbaTransaction());			
			setNbaTxLife(doHoldingInquiry(parentCase, NbaConstants.UPDATE,NbaUtils.getBusinessProcessId(getUser())));	
		} catch (NbaLockedException e) {
			//Suspend NBAPPLCTN if lock can not be obtained, and increase failure count of poller.
			NbaBootLogger.log("Unable to get lock from AWD so suspending for 15 minutes");
			suspendWork(Calendar.MINUTE, MIN);			
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
			throw e;
		} catch (Exception exception) {
			throw new NbaBaseException(exception);
		}
		return aNbaTXLife;
	}
	
	
	protected void processServiceResult() throws NbaBaseException {
		NbaTXLife tx508 = lookup508();
		NbaTXLife caseTxLife = null;
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
		caseTxLife = getNbaTxLife();
		Banking banking = NbaUtils.getBankingByHoldingSubType(caseTxLife, NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		if(banking!= null){
			BankingExtension bankingExt = NbaUtils.getFirstBankingExtension(banking);
			if(bankingExt!=null){
				bankingExt.setElectronicDraftFailureInd(true);
				bankingExt.setActionUpdate();
			}
		}
		oinkData.setContractSource(getNbaTxLife());
		oinkData.setLobSource(work.getNbaLob());//ALII2019
		NbaProcessStatusProvider provider = new NbaProcessStatusProvider(getUser(), getWork(), getNbaTxLife(), getRequirementInfo());
		addComment(getFailureComment(tx508));
		if (NbaOliConstants.OLI_POLSTAT_ISSUED == getNbaTxLife().getPolicy().getPolicyStatus()) {
		    //Begin ALII2019
		    NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.INFORMALTOFORMAL);
            vpmsProxy.setVpmsEntryPoint(EP_LIFESRV_WI);
            NbaVpmsResultsData data;
			try {
				data = new NbaVpmsResultsData(vpmsProxy.getResults());
				List resultsData = data.getResultsData();
	            // The entry point in VP/MS is configured to return business area, work type and status.
	            if (resultsData != null && resultsData.size() >= 3) {
	                createLifeSrvTransaction((String) resultsData.get(0), (String) resultsData.get(1), (String) resultsData.get(2),tx508,getWork().getNbaLob());
	                addComment("Payment to be reversed for  the Draft failure notification received");
	    			NbaUtils.setRouteReason(getWork(),provider.getPassStatus(),"Reversal of Electronic Premium");
	            }else{
	            	setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", provider.getPassStatus()));
	            }
			} catch (java.rmi.RemoteException re) {
				throw new NbaVpmsException("InformalToFormal" + NbaVpmsException.VPMS_EXCEPTION, re);
			} finally {
				try {
					if (vpmsProxy != null) {
						vpmsProxy.remove();
					}
				} catch (Throwable th) {
					getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
			//End ALII2019
			
		} else {
			reverseMoney(tx508, caseTxLife);
			addComment("Payment reversed for the Draft failure notification received");
		}
		caseTxLife = NbaContractAccess.doContractUpdate(caseTxLife, parentCase, getUser());
		setNbaTxLife(caseTxLife);
		//Begin ALII1923
		Double cwaTotal = getNbaTxLife().getNbaHolding().getCwaTotal();
		if (cwaTotal != null && !Double.isNaN(cwaTotal.doubleValue())) {
			parentCase.getNbaLob().setCwaTotal(cwaTotal.doubleValue());
			parentCase.setUpdate();
		}
		//End ALII1923
		if(getResult() == null){
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", provider.getPassStatus()));
		}
		changeStatus(getResult().getStatus());
		update(parentCase);
		updateWork();
		unlockWork(parentCase);		
	}
	
	protected void populateLOBsOnServiceResult(NbaTXLife workTxLife){
		NbaLob workLob = getWork().getNbaLob();
		NbaLob parentLob = parentCase.getNbaLob();
		workLob.setPolicyNumber(parentLob.getPolicyNumber());
		workLob.setLastName(parentLob.getLastName());
		workLob.setFirstName(parentLob.getFirstName());
		workLob.setSsnTin(parentLob.getSsnTin());
		workLob.setCompany(parentLob.getCompany());
		workLob.setBackendSystem(parentLob.getBackendSystem());
		FinancialActivity finActivity = workTxLife.getPrimaryHolding().getPolicy().getFinancialActivityAt(0);
		if (finActivity != null && finActivity.hasFinActivityGrossAmt()) {
			workLob.setCheckAmount(finActivity.getFinActivityGrossAmt());
		}
		getWork().setUpdate();
	}
	
	/**
	 * Reverse the money with amount in FinancialActivity in 508 xml.
	 * And invokes transaction transmitAccountingInformation(508) for reversal 
	 * 
	 * @param srcTxLife
	 *            508 TxLife
	 * @param aNbaTXLife
	 *            Case TxLife
	 * @throws Exception
	 */
	// ALII1923 Method Refactored
	protected void reverseMoney(NbaTXLife srcTxLife, NbaTXLife aNbaTXLife)throws NbaBaseException {
		FinancialActivity revFinAct = srcTxLife.getPrimaryHolding().getPolicy().getFinancialActivityAt(0);
		String finActivityId="";
		if (revFinAct != null) {
			try {
				Policy policy = aNbaTXLife.getPrimaryHolding().getPolicy();
				List oliExtList = revFinAct.getOLifEExtension();
				if (!oliExtList.isEmpty()) {
					OLifEExtension oliExt = revFinAct.getOLifEExtensionAt(0);
					FinancialActivityExtension finactExt = oliExt.getFinancialActivityExtension();
					if (finactExt != null && finactExt.hasConfirmationID()) {
						finactExt.setActivityReasonCode(NbaOliConstants.OLI_FINACT_REVERSENGCHECKCIPE); // APSL4910
						FinancialActivity paymentFinAct = findPayment(policy, finactExt.getConfirmationID());
						if (paymentFinAct != null) {
							if (!isPaymentDisbursed(paymentFinAct)) {
								finActivityId=paymentFinAct.getId(); //NBLXA-1356
								revFinAct.deleteId();
								NbaOLifEId olifeid = new NbaOLifEId(aNbaTXLife);
								olifeid.setId(revFinAct);
								revFinAct.setAccountingActivityType(NbaConstants.TRUE);
								revFinAct.setAction(NbaActionIndicator.ACTION_ADD);
								revFinAct.setUserCode(getUser().getUserID());//APSL3836
								
								//Begin APSL2735 QC11962
								if(!revFinAct.getPayment().isEmpty()){
									Payment payment = revFinAct.getPaymentAt(0);
									payment.deleteId();
									olifeid.setId(payment);
									payment.setAction(NbaActionIndicator.ACTION_ADD);
								}
							// Code Commented for APSL5057 (QC17724) -- 1225 Call not required for reverse Money
								/*AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(
										AxaWSConstants.WS_OP_SEND_PREMIUM_REFUND, user, aNbaTXLife, null, revFinAct);
								webServiceInvoker.execute();*/
								// Code Commented for APSL5057 (QC17724) -- 1225 Call not required for reverse Money
								//End APSL2735 QC11962
								revFinAct.setOrderSource(finActivityId); //NBLXA-1356
								policy.addFinancialActivity(revFinAct);
								//Update existing
								setDisbursementInd(paymentFinAct, true);
								paymentFinAct.setAccountingActivityType(NbaConstants.TRUE);
								paymentFinAct.setActionUpdate();
							} else {
								addComment("Duplicate draft failure notification received");
								setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
							}
						} else {
							addComment("No Payment found for the draft failure notification received");
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
						}
					} else {
						addComment("Payment id missing in draft failure notification received");
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
					}
				}
			} catch (Exception e) {
				throw new NbaBaseException("Payment not reversed successfully. " + e.getMessage());
			}
		}
	}
	
	protected FinancialActivity findPayment(Policy policy, String confirmationID) {
		for (int i = 0; i < policy.getFinancialActivityCount(); i++) {
			FinancialActivity paymentFinAct = policy.getFinancialActivityAt(i);
			if (!(paymentFinAct.hasFinActivitySubType() && paymentFinAct.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND
					|| paymentFinAct.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_REFUND || paymentFinAct.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_REV)) {
				FinancialActivityExtension finActExt = NbaUtils.getFirstFinancialActivityExtension(paymentFinAct);
				if (finActExt != null && finActExt.hasConfirmationID() && finActExt.getConfirmationID().equalsIgnoreCase(confirmationID)) {
					return paymentFinAct;
				}
			}
		}
		return null;
	}
	
	protected void setDisbursementInd(FinancialActivity finAct, boolean disburseInd) {
		List oliExtList = finAct.getOLifEExtension();
		if (oliExtList.size() != 0) {
			OLifEExtension oliExt = finAct.getOLifEExtensionAt(0);
			FinancialActivityExtension finactExt = oliExt.getFinancialActivityExtension();
			if (finactExt != null) {
				finactExt.setDisbursedInd(disburseInd);
				finactExt.setActionUpdate();
			}
		}
	}
	
	protected boolean isPaymentDisbursed(FinancialActivity finAct) {
		List oliExtList = finAct.getOLifEExtension();
		if (oliExtList.size() != 0) {
			OLifEExtension oliExt = finAct.getOLifEExtensionAt(0);
			FinancialActivityExtension finactExt = oliExt.getFinancialActivityExtension();
			if (finactExt != null) {
				return finactExt.getDisbursedInd();
			}
		}
		return false;
	}
	
		
	protected void suspendWork(int durationType , int durationValue) throws NbaBaseException {
		NbaSuspendVO suspendItem = null;		
		suspendItem = getSuspendWorkVO(durationType, durationValue);		
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
		if (suspendItem != null) {
			suspendWork(suspendItem);
		}
	}
	
	protected String getFailureComment(NbaTXLife aNbaTXLife) {
		StringBuffer comment = new StringBuffer();
		comment.append("Draft request failed ");
		FinancialActivity finActivity = aNbaTXLife.getPrimaryHolding().getPolicy().getFinancialActivityAt(0);
		if (finActivity != null) {
			if(finActivity.hasFinActivityGrossAmt()){
				comment.append("for $"+finActivity.getFinActivityGrossAmt());
			}
			List oliExtList = finActivity.getOLifEExtension();
			if (!oliExtList.isEmpty()) {
				OLifEExtension oliExt = finActivity.getOLifEExtensionAt(0);
				FinancialActivityExtension finactExt = oliExt.getFinancialActivityExtension();
				if (finactExt != null && finactExt.hasPaymentDraftFailureReason()) {
					comment.append(" due to "+finactExt.getPaymentDraftFailureReason());
				}
			}
		}
		return comment.toString();
	}
	
	//New Methods ALII2019
	 /**
     * This method creates a new work item for LIFESRV business area 
     * @param workType
     * @param businessArea
     * @param status
     * @return
     * @throws NbaNetServerDataNotFoundException
     * @throws NbaBaseException
     */
    protected void createLifeSrvTransaction(String businessArea, String workType, String status,NbaTXLife tx508,NbaLob lob) throws NbaBaseException {
        try {
            NbaDst lifeSRVTransaction = new NbaDst();
            WorkItem workItem = new WorkItem();
            // Set AWD fields
            workItem.setCreate("Y");
            workItem.setWorkType(workType);
            workItem.setStatus(status);
            workItem.setBusinessArea(businessArea);
            workItem.setRecordType("T");
            setLIFESRVTransactionLOBs(workItem,tx508,lob); 
            lifeSRVTransaction.setNbaUserVO(getWork().getNbaUserVO());
            lifeSRVTransaction.addTransaction(workItem);
            update(lifeSRVTransaction);
            unlockWork(lifeSRVTransaction);
          } catch (Exception ex) {
            throw new NbaBaseException(ex);
}
    }
    /**
     * This methods sets the LOBs configured in VP/MS for LIFESRV transaction.
     * @param workItem
     * @throws NbaBaseException
     */
    protected void setLIFESRVTransactionLOBs(WorkItem workItem,NbaTXLife tx508,NbaLob lob) throws NbaBaseException {
    	NbaVpmsAdaptor vpmsProxy = null; 
		try{
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(tx508);
			oinkData.setLobSource(lob);
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.INFORMALTOFORMAL); //ALS5009
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_LIFESRV_TRANSACTION_LOBS);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			String resultXml = (String) vpmsResultsData.getResultsData().get(0);
			NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(resultXml);
			List strAttrs = nbaVpmsModelResult.getVpmsModelResult().getStandardAttr();
	        Iterator itr = strAttrs.iterator();
	        List newLobs = workItem.getLobData();
			NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
	        while (itr.hasNext()) {
				LobData newLob = new LobData();
				StandardAttr standardAttr = (StandardAttr) itr.next();
				newLob.setDataName(standardAttr.getAttrValue()); //VP/MS returns LIFESRV LOB as attribute value
				aNbaOinkRequest.setVariable(standardAttr.getAttrName());
				String lobValue = oinkData.getStringValueFor(aNbaOinkRequest);
				if (!NbaUtils.isBlankOrNull(lobValue)) {
					if(standardAttr.getAttrValue().equalsIgnoreCase("ADCD")){//LIFESRV accepts only 3 characters for BackEndSystem
						lobValue = lobValue.substring(0,3);
					}
					newLob.setDataValue(lobValue);
					newLobs.add(newLob);
				}
			}
		} catch (Exception ex) {
			throw new NbaBaseException(ex);
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
    
    // APSL4768 New Method
	protected void processMoneyRequirement() throws NbaBaseException {
		Policy policy = nbaTxLife.getPolicy();
		if (policy != null) {
			NbaDst requirementDst = getRequirementWI(policy.getPolNumber(), NbaOliConstants.OLI_REQCODE_PREMDUE);
			if (requirementDst != null) {
				NbaTransaction nbaTransaction = requirementDst.getNbaTransaction();
				NbaLob requirementLob = requirementDst.getNbaLob();
				
				requirementLob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED));
				requirementLob.setReqReceiptDate(Calendar.getInstance().getTime());
				requirementLob.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(Calendar.getInstance().getTime()));//QC20240
				RequirementInfo aReqInfo = nbaTxLife.getRequirementInfo(requirementLob.getReqUniqueID());
				aReqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_RECEIVED);
				aReqInfo.setReceivedDate(requirementLob.getReqReceiptDate());
				aReqInfo.setStatusDate(requirementLob.getReqReceiptDate());
				aReqInfo.setFulfilledDate(requirementLob.getReqReceiptDate());
				aReqInfo.setRequirementDetails("");
				aReqInfo.setActionUpdate();
				if (aReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_PREMDUE) {
					RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
					if (requirementInfoExt == null) {
						OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
						requirementInfoExt = olifeExt.getRequirementInfoExtension();
					}
					requirementInfoExt.setPremiumDueCarrierReceiptDate(requirementLob.getReqReceiptDate());
					//Begin APSL5144 
					//if (!nbaTxLife.isTermLife()) { NBLXA-1789 removed condition to ignore term cases
						requirementInfoExt.setReviewedInd(true);
						requirementInfoExt.setReviewID(user.getUserID());
						requirementInfoExt.setReviewDate(new Date());
						requirementInfoExt.setReceivedDateTime(requirementLob.getReqReceiptDateTime());//QC20240
				//	}
					//End APSL5144
					requirementInfoExt.setActionUpdate();
				}
				NbaProcessStatusProvider provider = new NbaProcessStatusProvider(getUser(), requirementLob);
				nbaTransaction.setStatus(provider.getPassStatus());
				nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
				if (nbaTransaction.isSuspended()) {
					NbaSuspendVO suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(nbaTransaction.getID());
					unsuspendWork(suspendVO);
				}
				unlockWork(requirementDst);
			}
		}
	}

	/**
	 * Retrieves requirement work item from AWD.
	 * @param policyNumber
	 * @param reqType
	 * @return the retrieved work item
	 * @throws NbaBaseException
	 */
	// APSL4768 New Method
	protected NbaDst getRequirementWI(String policyNumber, long reqType) throws NbaBaseException {
		NbaSearchResultVO resultVO = null;
		NbaLob lob = new NbaLob();
		lob.setPolicyNumber(policyNumber);
		lob.setReqType((int) reqType);
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_REQUIREMENT);
		searchVO.setNbaLob(lob);
		searchVO.setResultClassName("NbaSearchResultVO");
		searchVO = WorkflowServiceHelper.lookupWork(getUser(), searchVO);
		if (searchVO.isMaxResultsExceeded()) {
			throw new NbaBaseException(NbaBaseException.SEARCH_RESULT_EXCEEDED_SIZE, NbaExceptionType.FATAL);
		}
		List searchResult = searchVO.getSearchResults();
		if (searchResult != null && searchResult.size() > 0) {
			resultVO = (NbaSearchResultVO) searchResult.get(0);
		}
		NbaDst requirementDst = null;
		if (resultVO != null) {
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(resultVO.getWorkItemID(), true);
			retOpt.setLockWorkItem();
			requirementDst = WorkflowServiceHelper.retrieveWorkItem(getUser(), retOpt);
		}
		return requirementDst;
	}

   }

