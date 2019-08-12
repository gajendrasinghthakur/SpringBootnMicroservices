package com.csc.fsg.nba.bean.accessors;
/*
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.ejb.SessionBean;

import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.database.NbaContractDataBaseInfo;
import com.csc.fsg.nba.database.NbaContractPrintExtractAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponse;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
/**
 * NbaContractPrintExtractWebService is a stateless Session Bean used to 
 * provide access to the Contract Print Extract table (NBA_CONTRACT_PRINT_EXTRACT). 
 * 
 * The Contract Print Extract table contains rows for Contract print documents. These include 
 * Agent Cards, Policy Summaries, Policy Values Pages and Face/Schedule Pages. 
 * 
 * The writeExtracts() method of NbaContractPrintExtractWebService provides the Base System 
 * implementation for making the extracts available to a print formatting application. 
 * This implementation creates rows in the NBA_CONTRACT_PRINT_EXTRACT 
 * table from a TXLIFE input object.
 *    
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>NBA113</td><td>Version 5</td><td>V4 Software Upgrades </td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */
public class NbaContractPrintExtractWebServiceBean implements SessionBean, NbaOliConstants {
	protected static NbaLogger logger = null;
	// NBA113 code deleted
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractPrintExtractWebServiceBean.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractPrintExtractWebServiceBean could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	private javax.ejb.SessionContext mySessionCtx;
	private NbaTXLife responseNbaTXLife;
	/**
	 * Add the TransResult to the Responses
	 * @param transResult
	 */
	protected void addTransResultToResponses(TransResult transResult) {
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify responses =
			getResponseNbaTXLife().getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		int count = responses.getTXLifeResponseCount();
		for (int j = 0; j < count; j++) {
			responses.getTXLifeResponseAt(j).setTransResult(transResult);
		}
	}
	/**
	 * Creates a new <code>TransResult</code> object with the appropriate
	 * error information for a problem related to writing rows
	 * 
	 * @param e an exception for an unexpected problem
	 * @return com.csc.fsg.nba.vo.txlife.TransResult
	 */
	protected TransResult createTransResult(NbaDataAccessException e) {
		 
		TransResult	transResult = new TransResult();
		transResult.setResultCode(TC_RESCODE_FAILURE);
		 
	ResultInfo r1 = new ResultInfo();
	r1.setResultInfoCode(TC_RESINFO_GENERALERROR);
	r1.setResultInfoDesc(e.getMessage());
	transResult.addResultInfo(r1);
	if (e.getDbInfo() != null) {
		NbaContractDataBaseInfo nbaContractDataBaseInfo = e.getDbInfo();
		ResultInfo r2 = r1.clone(true);
		r2.setResultInfoDesc(nbaContractDataBaseInfo.getRefId());
		transResult.addResultInfo(r2);
	}
	return transResult;
	}
 
	/**
	 * Create a TransResult for a Service level problem
	 * @param exception
	 * @return TransResult
	 */
	protected TransResult createTransResultForServiceLevelException(String msg, Throwable t) { //NBA103
		TransResult transResult = new TransResult();
		transResult.setResultCode(TC_RESCODE_FAILURE);
		ResultInfo r1 = new ResultInfo();
		r1.setResultInfoCode(TC_RESINFO_UNABLETOPROCESS);
		r1.setResultInfoDesc(t.getMessage());
		transResult.addResultInfo(r1);
		ResultInfo r2 = r1.clone(true);
		r2.setResultInfoDesc(msg);
		return transResult;
	}
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {
	}
	/**
	 * ejbCreate
	 */
	public void ejbCreate() throws javax.ejb.CreateException {
	}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {
	}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() {
	}
	/**
	 * Retrieve the response NbaTXLife. If a failure occurred before it could be 
	 * created from the request, initialize with default values.
	 * @return the response NbaTXLife
	 */
	protected NbaTXLife getResponseNbaTXLife() {
		if (responseNbaTXLife == null){
			responseNbaTXLife = new NbaTXLife();
			responseNbaTXLife.setTXLife(new TXLife());
			UserAuthResponseAndTXLifeResponseAndTXLifeNotify ua = new UserAuthResponseAndTXLifeResponseAndTXLifeNotify();
			responseNbaTXLife.getTXLife().setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(ua);
			ua.setUserAuthResponse(new UserAuthResponse());
			ua.getUserAuthResponse().setSvrDate(new Date());
			ua.getUserAuthResponse().setSvrTime(new NbaTime());
			TXLifeResponse tXLifeResponse = new TXLifeResponse();
			ua.addTXLifeResponse(tXLifeResponse);
			tXLifeResponse.setTransRefGUID(NbaUtils.getGUID());
			tXLifeResponse.setTransType(TC_TYPE_CONTRACTPRINTEXT);
			tXLifeResponse.setTransExeDate(new Date());
			tXLifeResponse.setTransExeTime(new NbaTime());
			tXLifeResponse.setTransMode(TC_MODE_ORIGINAL);
			 
		}
		return responseNbaTXLife;
	}
	/**
	 * getSessionContext
	 */
	public javax.ejb.SessionContext getSessionContext() {
		return mySessionCtx;
	}
	/**
	 * Create TXLifeResponses for a data problem
	 * @param exception
	 */
	protected void handleException(NbaDataAccessException exception) {
		TransResult transResult = createTransResult(exception);
		addTransResultToResponses(transResult);
	}
	/**
	 * Create TXLifeResponses for a Service level Exception
	 * @param exception
	 */
	protected void handleServiceLevelException(String msg, Throwable t) { //NBA103
		if (t instanceof NbaBaseException) {
			((NbaBaseException)t).forceFatalExceptionType();
		}
		getLogger().logFatalException(t); //NBA103
		TransResult transResult = createTransResultForServiceLevelException(msg, t);
		addTransResultToResponses(transResult);
	}
	/**
	 * Marshall the TXLifeResponses into a DocumentElement
	 * @return a DocumentElement containing the TXLifeResponses. 
	 */
	protected String marshallResponse() {	//NBA113
		// NBA113 code deleted
		return getResponseNbaTXLife().toXmlString();	//NBA113
	}
	/**
	 * Write extracts for the Contract Print documents contained in the request.
	 * Hydrate a TxLife request from the DocumentElement and create a TXLife response.
	 * @param element - a DocumentElement for a TXLife request
	 * @return the response TXLife as a DocumentElement
	 */
	public String service(String element) {	//NBA113
		if (getLogger().isDebugEnabled()) {
			// NBA113 code deleted
			getLogger().logDebug("SOAP Request= " + element);	//NBA113
			// NBA113 code deleted
		}
		try {
			// NBA113 code deleted
			NbaTXLife nbaTXLifeRequest = new NbaTXLife(element); //Hydrate a TXLife  //NBA113
			return writeExtracts(nbaTXLifeRequest);
		} catch (Throwable t) { //NBA103
			handleServiceLevelException("Unable to marshal TXLife Request", t);
			return marshallResponse();
		}
	}
	/**
	 * Set the response NbaTXLife
	 * @param life
	 */
	protected void setResponseNbaTXLife(NbaTXLife life) {
		responseNbaTXLife = life;
	}
	/**
	 * setSessionContext
	 */
	public void setSessionContext(javax.ejb.SessionContext ctx) {
		mySessionCtx = ctx;
	}
	/**
	 * Write the Contract Print extracts to a database. Write a row for each TXLifeRequest.
	 * @param nbaTXLifeRequest - the NbaTXLife containing the TXLifeRequests.
	 */
	protected void writeExtractRows(NbaTXLife nbaTXLife) {
		Connection conn;
		try {
			conn = new NbaContractPrintExtractAccessor().write(nbaTXLife);
			try {
				conn.commit(); //handle only for successful processing, NbaContractPrintExtractAccessor handles rollback, etc for failures 
			} catch (SQLException e) {
				handleServiceLevelException("During commit", e);
			}
			try {
				NbaConnectionManager.returnConnection(conn);
			} catch (SQLException e) {
				handleServiceLevelException("During Connection close", e);
			}
		} catch (NbaBaseException e) {
			if (e instanceof NbaDataAccessException) {
				handleException((NbaDataAccessException) e);
			} else {
				handleServiceLevelException("During write", e);
			}
		} catch (SQLException e) {
			handleServiceLevelException("During write", e);
		}
	}
	/**
	 * Write database rows for the TXLifeRequest objects contained in the TXLife.
	 * @param nbaTXLifeRequest
	 * @return a DocumentElement containing the TXLifeResponses.  
	 */
	protected String writeExtracts(NbaTXLife nbaTXLifeRequest) {	//NBA113
		//Initialize a response from the request
		setResponseNbaTXLife(new NbaTXLife(nbaTXLifeRequest)); 
		//Write the extracts
		writeExtractRows(nbaTXLifeRequest);
		//Create a DocumentElement for the response
		return marshallResponse();
	}
}
