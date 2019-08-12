package com.csc.fsg.nba.assembler.datafeed;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDataFeedRequest;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.nbaschema.NbaDataFeedData;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponse;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * Services for the Data Feed Web Service to prepare the incoming request  and handle the result for
 * data feed hibernate sevice.  The disassembler populates the 
 * hibernate data object <code>NbaDataFeedData</code> from the <code>NbaDataFeedRequest</code> 
 * value object. The <code>NbaDataFeedData</code> is then returned in a <code>Result</code>.
 * The assembler builds the TXLife response. The incoming <code>Result</code> is checked for errors
 * If errors are present, they are used to build the <code>ResultInfo</code> object in the TXLife response.
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA232</td><td>Version 8</td><td>nbA Feed for a Customer's Web Site</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class NbaDataFeedWebServiceAssembler extends AccelTransformation {

	/**
	 * Populates a <code>Transform</code> data object from the <code>WebServiceCall</code>
	 * value object.
	 * @param input WebServiceCall
	 * @return
	 */
	public Result disassemble(Object input) {
		Result result = new AccelResult();
		NbaDataFeedRequest wsRequest = (NbaDataFeedRequest) input;
		result.addResult(createDataFeedData(wsRequest));
		return result;
	}
	/**
	 * 
	 * @param wsRequest
	 * @return
	 */
	protected NbaDataFeedData createDataFeedData(NbaDataFeedRequest wsRequest) {
		NbaDataFeedData dataFeed = new NbaDataFeedData();
		dataFeed.setContractKey(wsRequest.getContractKey());
		dataFeed.setUserID(wsRequest.getUserID());
		dataFeed.setEventTimeStamp(wsRequest.getTimeStamp());
		dataFeed.setFeedData(wsRequest.getFeedData());
		return dataFeed;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public Result assemble(Result request) {
		
		return buildResponse(request);
		
	}
	/**
	 * Initialize a successful TxLife response.
	 */
	protected Result buildResponse(Result request) {
		NbaTXLife nbaTXLifeResponse = new NbaTXLife();
		nbaTXLifeResponse.setTXLife(new TXLife());
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify ua = new UserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		nbaTXLifeResponse.getTXLife().setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(ua);
		ua.setUserAuthResponse(new UserAuthResponse());
		ua.getUserAuthResponse().setSvrDate(new Date());
		ua.getUserAuthResponse().setSvrTime(new NbaTime());
		TXLifeResponse tXLifeResponse = new TXLifeResponse();
		ua.addTXLifeResponse(tXLifeResponse);
		tXLifeResponse.setTransRefGUID(NbaUtils.getGUID());
		tXLifeResponse.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS);
		tXLifeResponse.setTransExeDate(new Date());
		tXLifeResponse.setTransExeTime(new NbaTime());
		tXLifeResponse.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		TransResult transResult = new TransResult();
		if (request.hasErrors()) {
			handleErrors(transResult, request.getMessagesList());
		} else {
			transResult.setResultCode(NbaOliConstants.TC_RESCODE_SUCCESS);
		}
		tXLifeResponse.setTransResult(transResult);

		Result result = new AccelResult();
		return result.addResult(nbaTXLifeResponse);
	}
	
	/**
	 * Set the TransResult.ResultCode to failure and create a ResultInfo object for the Exception.
	 * @param e - the Exception
	 */
	protected void handleErrors(TransResult transResult, List messages) {
		transResult.setResultCode(NbaOliConstants.TC_RESCODE_FAILURE);
		int messageSize = messages.size();
		ResultInfo resultInfo;
		ArrayList errorList = new ArrayList(messageSize); //This is an ArrayList due to what setResultInfo is expecting
		for (int i = 0; i < messageSize; i++) {
			Message msg = (Message) messages.get(i);
			resultInfo = new ResultInfo();
			resultInfo.setResultInfoCode(NbaOliConstants.TC_RESINFO_GENERALDATAERR);
			resultInfo.setResultInfoDesc(msg.format());
			errorList.add(resultInfo);
		}
		transResult.setResultInfo(errorList);
	}
	
}
