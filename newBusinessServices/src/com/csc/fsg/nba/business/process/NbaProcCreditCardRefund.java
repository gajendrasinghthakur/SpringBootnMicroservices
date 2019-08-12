package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.ArrayList;

import com.csc.fsg.nba.business.transaction.NbaCreditCardTransaction;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;

/**
 * Automated process for credit card refund queue. It processes CWA work item generated for refund of credit card payments.
 * Generates an XML transaction for credit card 'credit' and sends it to clearing house or credit card vendor. 
 * Also processes the response from the clearing house and updates CWA refund work item accordingly.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit Card Payment Reversal and Refund</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaProcCreditCardRefund extends NbaAutomatedProcess {

	/**
	 * NbaProcCreditCardRefund constructor
	 */
	public NbaProcCreditCardRefund() {
		super();
	}

	/** 
	 * Autoprocess execution entry point. Generates the credit card credit request XML transaction and sends it to the TEST webservices. 
	 * The test web services serves as a credit card vendor webservices. It returns back a flat file XML response to the autoprocess 
	 * which then routes work item to appropriate queue based on whether the response was success or failure. 
	 * @param user an instance of <NbaUserVO> representing credit card refund automated user
	 * @param work credit card refund work item
	 * @return autoprocess processing result in an instance of <NbaAutomatedProcessResult>
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {
		if (!initialize(user, work)) {
			return getResult();
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Starting Credit Card Refund Process for work " + getWork().getID());
		}

		NbaCreditCardTransaction creditCardTransaction = new NbaCreditCardTransaction();
		NbaTXLife refundRequest = creditCardTransaction.generateCreditRequest(work, getUser());
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Credit card credit transaction request : " + refundRequest.toXmlString());
		}
		//process credit request by invoking clearing house adapter 
		//for base system, it will be a Test webservice stub 
		NbaTXLife refundResponse = invokeRefundAdapter(refundRequest);
		try {
			//process response XML received from webservice 			
			handleAdapterResponse(refundResponse);
		} catch (NbaBaseException e) {
			//add comment and set status to error if an error occurred during webservice CC refund processing
			String error = e.getMessage();
			getLogger().logError(error);
			addComment("Error occurred during Credit Card refund processing : " + error);
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, error, getFailStatus()));
		}
		//update status of refund work item
		changeStatus(getResult().getStatus());
		doUpdateWorkItem();
		return getResult();
	}

	/**
	 * Invokes credit card clearing house adapter for a credit card credit request.
	 * @param request an instance of <NbaTXLife> containing request to be sent to clearing house adapter
	 * @return an instance of <NbaTXLife> containing response from adapter
	 * @throws NbaBaseException if service call could not be completed or VPMS error has occurred
	 */
	protected NbaTXLife invokeRefundAdapter(NbaTXLife request) throws NbaBaseException {
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
		NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.DETERMINEADMINSYSTEM);
		NbaTXLife response = null;
		try {
			//determine backend system to resolve applicable webservice adapter from configuration file 
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_ADMINISTRATION_SYSTEM);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			work.getNbaLob().setBackendSystem((String) data.resultsData.get(0));
			NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(work.getNbaLob().getBackendSystem(), "CreditCard", "CreditCardRefund"); //SPR2968
			//invoke the webservices
			response = service.invokeWebService(request); // SPR2968
			//if there is no response from webservice, throw an exception to stop the automated process and report the problem in poller console
			if (response == null) {
				throw new NbaBaseException("Credit Card Refund webservice call failed");
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Credit card credit transaction response : " + response.toXmlString());
			}
		} catch (RemoteException re) {
			getLogger().logError("Error processing Credit Card Refund : " + re.getMessage());
			throw new NbaBaseException(NbaBaseException.RMI, re);
		} finally {
			//The excpetions are suppressed based on the assumption that if there is a problem with VPMS communication, 
			//a VPMS call in try block will throw exception
		    //begin SPR3362
		    try {
                if (vpmsProxy != null) {
                    vpmsProxy.remove();
                }
            } catch (Throwable th) {
                getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
            }
            //end SPR3362
		}
		return response;
	}

	/**
	 * Handles response from credit card clearing house webservice adapter for a card credit request
	 * Also requests cashiering table updates. 
	 * @param response the response received from service
	 */
	protected void handleAdapterResponse(NbaTXLife response) throws NbaBaseException {
		TransResult transResult = response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
		boolean refundSuccessful = transResult.getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS;
		if (refundSuccessful) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Credit Card Refund Success.", getPassStatus()));
		} else {
			ResultInfo resultInfo = null;
			ArrayList results = transResult.getResultInfo();
			int resultInfoCount = results.size();
			StringBuffer commentBuffer = new StringBuffer("Credit Card Refund Failed. ");
			for (int i = 0; i < resultInfoCount; i++) {
				resultInfo = (ResultInfo) results.get(i);
				if (resultInfo.hasResultInfoCode()) {
					commentBuffer.append("Error Code: ");
					commentBuffer.append(resultInfo.getResultInfoCode());
					commentBuffer.append(", ");
				}
				if (resultInfo.hasResultInfoDesc()) {
					commentBuffer.append("Message: ");
					commentBuffer.append(resultInfo.getResultInfoDesc());
					commentBuffer.append(". ");
				}
			}
			addComment(commentBuffer.toString());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Credit Card Refund Failed.", getFailStatus()));
		}
		//update cashiering table for a success or failure 
		updateCashieringTable(refundSuccessful);
	}

	/**
	 * Inserts a record into cashiering table NBA_CREDIT_CARD for the refund transaction, values of which are derived from CWA work item LOBs 
	 * If the refund was successful, Refunded indicator is set true. Otherwise refund failed indicator is set true. 
	 * @param refundSuccessful true if refund was successful, false otherwise
	 * @throws NbaBaseException if not able to update the cashiering table
	 */
	protected void updateCashieringTable(boolean refundSuccessful) throws NbaBaseException {
		NbaLob workLob = getWork().getNbaLob();
		NbaCreditCardData ccData = new NbaCreditCardData();
		ccData.setTransactionId(workLob.getCCTransactionId());
		ccData.setCompany(workLob.getCompany());
		ccData.setContractNumber(workLob.getPolicyNumber());
		ccData.setCreditCardNumber(workLob.getCCNumber());
		ccData.setCreditCardType(workLob.getCCType());
		ccData.setAmount(workLob.getCwaAmount());
		ccData.setDepositDate(workLob.getCwaDate());
		if (refundSuccessful) {
			ccData.setRefundInd(true);
		} else {
			ccData.setRefundFailInd(true);
		}
		ccData.setReportInd(true);
		ccData.insert();
	}

}
