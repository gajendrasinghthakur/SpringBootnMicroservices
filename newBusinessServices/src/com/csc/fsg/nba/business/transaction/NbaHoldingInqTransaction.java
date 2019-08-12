package com.csc.fsg.nba.business.transaction;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group®.  The use, reproduction,
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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaConfigurationException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * Creates a holding inquiry request transaction and performs a webservice call to the admin system
 * to retrieve the 203 holding inquiry. Once the holding inquiry has been received, any marking up of the 
 * XML is done.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application Project</td></tr>
 * <tr><td>AXAL3.7.04</td><td>Axa Life Phase 1</td><td>Paid Changes</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class NbaHoldingInqTransaction extends NbaBusinessTransactions implements NbaConstants, NbaConfigurationConstants{
	NbaTXRequestVO request = null;
	protected NbaLogger logger = null; 
	
	/**
	 * Constructor for NbaHoldingInqTransaction.
	 */
	public NbaHoldingInqTransaction() {
		super();
	}
	
	/**
	 * Create a TX Request value object that will be used to retrieve the contract.
	 * @param nbaDst the workitem object for that holding request is required
	 * @param access the access intent to be used to retrieve the data, either READ or UPDATE
	 * @param businessProcess the name of the business function or process requesting the contract
	 * @return a value object that is the request
	 */
	public NbaTXRequestVO createRequestTransaction(NbaLob lob, int access, String businessProcess, NbaUserVO user) {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
		nbaTXRequest.setTransSubType(Long.parseLong(lob.getContractChgType()));
		nbaTXRequest.setNbaLob(lob);
		nbaTXRequest.setNbaUser(user);
		if (access != -1) {
			nbaTXRequest.setAccessIntent(access);
		} else {
			nbaTXRequest.setAccessIntent(READ);
		}
		if (businessProcess != null) {
			nbaTXRequest.setBusinessProcess(businessProcess);
		} else {
			nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(user));
		}
		setRequest(nbaTXRequest);
		return nbaTXRequest;
	}
	
	/**
	 * Call the inforce web service to process the holding inquiry request for the admin system and return the 203 response
	 * @param nbaUserVO
	 * @param lob
	 * @return
	 * @throws NbaConfigurationException
	 * @throws NbaBaseException
	 */
	public NbaTXLife processInforceTransaction(NbaLob lob, NbaUserVO nbaUserVO) throws NbaConfigurationException, NbaBaseException {
		if (getRequest() == null) {
			throw new NbaBaseException(NbaBaseException.MISSING_REQUEST);
		}
		NbaTXLife response;
		//ALII53 code deleted
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_RETRIEVE_POLICY, nbaUserVO, null,
					null, lob);
			response = (NbaTXLife) webServiceInvoker.execute();
		//ALII53 code deleted
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Completed holding retrieval from inforce admin system: " + response == null ? null : response.toXmlString());
		}
		if (response != null && !response.isTransactionError()) {
			doInforceMarkUp(response);
		}
		return response;
	}
	
	/**
	 * Performs modifications to the contract retrieved from performing a holding inquiry transaction from another system. This typically includes
	 * adding default values required that might not come in from another system.
	 * 
	 * @param response
	 */
	protected void doInforceMarkUp(NbaTXLife response) {
		NbaHolding nbaHolding = response.getNbaHolding();
		if (nbaHolding == null) {
			return;
		}
		markUpAppInfo(nbaHolding);
	}
	/**
	 * Mark up the application info
	 * @param nbaHolding
	 */
	protected void markUpAppInfo(NbaHolding nbaHolding) {
		ApplicationInfo appInfo = nbaHolding.getApplicationInfo();
		if (appInfo == null) {
			appInfo = new ApplicationInfo();
			nbaHolding.getPolicy().setApplicationInfo(appInfo);
		}
		appInfo.setFormalAppInd(true);
	}

	protected NbaTXRequestVO getRequest() {
		return request;
	}
	protected void setRequest(NbaTXRequestVO request) {
		this.request = request;
	}
	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory."); 
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}

