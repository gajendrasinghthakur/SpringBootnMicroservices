package com.csc.fsg.nba.business.process;
/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.csc.fsg.nba.business.transaction.NbaCreditCardTransaction;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.tableaccess.NbaCashieringTable;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;

/**
 * NbaProcCreditCard is the class that processes nbAccelerator cases found
 * in the AWD credit card queue (NBCCARD). The purpose of this step in the workflow
 * is to determine if all required credit card data is present, and perform check digit
 * validation on the card number. If the data is valid, this process will create an 
 * xml stream for submission to a credit card clearing house adapter.  Such adapters 
 * are created uniquely for each clearing house, and are a part of each nbA customer 
 * implementation that requires them.  The adapters are not included with the nbA base
 * system source.
 * <p>The NbaProcCreditCard class extends the NbaAutomatedProcess class.  
 * Although this class may be instantiated by any module, the NBA polling class 
 * will be the primary creator of objects of this type.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit card payment and authorization</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>NBA148</td><td>Version 6</td><td>VPMS naming standards</td></tr>
 * <tr><td>NBA123</td><td>Version 6</td><td>Administrator Console Rewrite</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaProcCreditCard extends NbaAutomatedProcess {
	protected static NbaLogger logger = null;
	protected static final String VPMS_NBAMONEY = "1";
	protected static final String VPMS_NON_NBAMONEY = "0";
	protected String validationErrors = "";
	protected boolean nbaMoney = false;


	/**
	 * NbaProcCreditCard constructor .
	 */
	//NBA123 removed "throws NbaBaseException"
	public NbaProcCreditCard() {
		super();
	}
	/**
	 * This abstract method must be implemented by each subclass in order to
	 * execute the automated process.
	 * @param user the user/process for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 * the success or failure of the process
	 * @throws NbaBaseException
	 */	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		if (!initializeWithoutStatus(user, work)) {
			return getResult(); 
		}				
		if(!initializeVpmsStatus()) {//determine nba vs non-nba money and reinitialize statuses	
			return getResult();
		}			
		if(!validateCreditCardData()){
			return getResult();
		}		
		if (getValidationErrors().length() > 0) {
			NbaCreditCardData.updateCCRefusedPayment(getWork().getNbaLob().getCCTransactionId());
			addComment(getValidationErrors());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();		
			return getResult();
		}

		NbaCreditCardTransaction creditCardTransaction = new NbaCreditCardTransaction();				
		NbaTXLife request = creditCardTransaction.generateDebitRequest(getWork(), getUser());
		NbaTXLife response = null;	
		
		//begin NBA148
		if (getLogger().isDebugEnabled()){
		    getLogger().logDebug("Credit Card clearing house XML" + request.toXmlString()); 
		}
		//end NBA148
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.DETERMINEADMINSYSTEM); //SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_ADMINISTRATION_SYSTEM);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			work.getNbaLob().setBackendSystem((String) data.resultsData.get(0));
			NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(work.getNbaLob().getBackendSystem(), "CreditCard", "CreditCardPayment"); //SPR2968
			response = service.invokeWebService(request); // SPR2968
			if(response == null){
				throw new NbaBaseException("Webservice returned null value.");
			}
		} catch(RemoteException re){
			addComment("Administration System could not be determined");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Backend System could not be determined", getFailStatus()));
		} catch (NbaBaseException nbe) {
			addComment("Credit Card webservice failed: " + nbe.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, nbe.getMessage(), getHostFailStatus()));
			changeStatus(getHostFailStatus());
			doUpdateWorkItem();		
			return getResult();
		//begin SPR3362
		} finally {
			try {
			    if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
			} catch (Exception e) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //Ignoring the exception SPR3362
			}
		}
		//end SPR3362
		TransResult ccResult = response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
		processResponse(ccResult);

		if (getResult() == null) {
			if(isNbaMoney()){
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}else {
				NbaLob lob = getWork().getNbaLob();
				if(lob.getInforcePaymentManInd() != null && lob.getInforcePaymentManInd().compareTo("1") == 0){
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getAlternateStatus()));
				}
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}
		}	
		changeStatus(getResult().getStatus());
		doUpdateWorkItem();		
		return getResult();
}
	/**
	 * Invoke the Credit Card vpms model to validate credit card data.
	 * @return String containing the parsed credit card validation results.
	 * @throws NbaBaseException
	 */
	protected boolean validateCreditCardData() throws NbaBaseException {
		String errors = "";
		try {
			Map deOinkMap = new HashMap();
			deOinkMap.put("A_ReturnType", "value"); 
			if(!isNbaMoney()){
				deOinkMap.put("A_InforcePayment", "1"); 
			}

			boolean deCoded = false;
			String ccNumber = getWork().getNbaLob().getCCNumber();		
			if(ccNumber != null && ccNumber.length() > 0){
				getWork().getNbaLob().setCCNumber(NbaBase64.decodeToString(ccNumber));
				deCoded = true;
			}
			String results = processVpms(getWork().getNbaLob(), deOinkMap);	
			if(deCoded){
				getWork().getNbaLob().setCCNumber(NbaBase64.encodeString(getWork().getNbaLob().getCCNumber()));
			}

			NbaStringTokenizer tokens = new NbaStringTokenizer(results, "##"); 
			errors = tokens.nextToken();
			while (tokens.hasMoreTokens()) {
				errors = errors + ", " + tokens.nextToken();
			}						
			setValidationErrors(errors);
					
		} catch (NbaBaseException nbe) {
			addComment(nbe.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, nbe.getMessage(),getVpmsErrorStatus()));
			doUpdateWorkItem();
			changeStatus(getResult().getStatus());
			return false;
		}
		return true;
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaProcCreditCard.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaProcCreditCard could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	/**
	 * Makes call to vpms for validation data
	 * @param NbaLob - the credit card lob.
	 * @param NbaMap - the deOinkMap.
	 * @throws NbaBaseException
	 */	
	protected String processVpms(NbaLob lobData, Map deOinkMap) throws NbaBaseException {
	    NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess(lobData);
			NbaOinkFormatter dataFormatter = data.getFormatter();
			dataFormatter.setDateFormat(NbaOinkFormatter.DATE_FORMAT_MMDDYYYY);
			dataFormatter.setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_SLASH);
			vpmsProxy = new NbaVpmsAdaptor(data, NbaVpmsAdaptor.CREDIT_CARD_VALIDATION); //SPR3362
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_CCVALIDATION_XML);

			// get the string out of XML returned by VP / MS Model and parse it to create the object structure
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			String xmlString = (String) vpmsResultsData.getResultsData().get(0);
			NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
			VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
			ArrayList strAttrs = vpmsModelResult.getStandardAttr();
			//Generate delimited string if there are more than one parameters returned
			Iterator itr = strAttrs.iterator();
			String returnStr = "";
			if (itr.hasNext())
				returnStr += ((StandardAttr) itr.next()).getAttrValue();
			while (true) {
				if (itr.hasNext()) {
					returnStr += NbaVpmsAdaptor.VPMS_DELIMITER[0];
					returnStr += ((StandardAttr) itr.next()).getAttrValue();
					continue;
				}
				break;
			}
			//SPR3362 code deleted
			return returnStr;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
        //begin SPR3362
		} finally {
			try {
				if (null != vpmsProxy) {
					vpmsProxy.remove();
				}
			} catch (Exception e) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //Ignoring the exception SPR3362
			}
		}
		//end SPR3362
	}

	/**
	 * Reinitializes the vpms status fields after determining nba vs non-nba money.
	 * @param NbaSource - the credit card source.
	 * @throws NbaBaseException
	 */
	private boolean initializeVpmsStatus() throws NbaBaseException {
		
		try {
			if(getWork().getNbaLob().getCCTransactionId() != null){
				NbaCashieringTable cashTable = new NbaCashieringTable();
				NbaCreditCardData ccData = cashTable.getCreditCardData(getWork().getNbaLob().getCCTransactionId());
				if(!ccData.isInforceInd()){
					setNbaMoney(true);			
				}			
				Map deOink = new HashMap();
				if(isNbaMoney()) {
					deOink.put("A_NBAMONEY", VPMS_NBAMONEY);
				}else {
					deOink.put("A_NBAMONEY", VPMS_NON_NBAMONEY);			
				}			
				statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), deOink);	
				return true;
			}else {
				addComment("Credit card transaction id is missing or invalid.");
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Credit card transaction id is missing or invalid.", getSqlErrorStatus()));				
			}
		} catch (NbaDataAccessException dae) {
			addComment("SQL Error: Unable to retrieve credit card data.");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, dae.getMessage(), getSqlErrorStatus()));
		}
		changeStatus(getSqlErrorStatus());
		doUpdateWorkItem();		
		return false;
	}

	
	/**
	 * Process codes returned by the clearing house if the payment is refused.
	 * Do nothing if the payment is accepted.
	 * @param TransResult - returned from the clearing house
	 * @throws NbaBaseException
	 */
	protected void processResponse(TransResult transResult) throws NbaBaseException {
		if (transResult.getResultCode() == NbaOliConstants.TC_RESCODE_FAILURE) { 
			ResultInfo resultInfo;
			ArrayList results = transResult.getResultInfo();
			for (int i = 0; i < results.size(); i++) {
				resultInfo = (ResultInfo) results.get(i);
				addComment("Clearing house code: " + resultInfo.getResultInfoCode());
			}
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Credit Card transaction refused.", getFailStatus()));
			NbaCreditCardData.updateCCRefusedPayment(getWork().getNbaLob().getCCTransactionId());
		}else {
			NbaCashieringTable cashTable = new NbaCashieringTable();
			cashTable.updateCCChargeDate(getWork().getNbaLob().getCCTransactionId(), new Date());
		}
	}
	/**
	 * Returns the validation errors determined by the Credit Card vpms model
	 * @return
	 */
	public String getValidationErrors() {
		return validationErrors;
	}

	/**
	 * Sets validation errors determined by the Credit Card vpms model
	 * @param string
	 */
	public void setValidationErrors(String string) {
		validationErrors = string;
	}

	//NBA148 method deleted
	/**
	 * Returns true if the work item is an inforce payment.
	 * @return
	 */
	public boolean isNbaMoney() {
		return nbaMoney;
	}


	/**
	 * Set value of property nbaMoney
	 * @param b
	 */
	public void setNbaMoney(boolean b) {
		nbaMoney = b;
	}

}
