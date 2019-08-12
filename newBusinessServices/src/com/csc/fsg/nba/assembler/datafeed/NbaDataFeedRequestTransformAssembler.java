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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WebServiceCall;
import com.csc.fs.dataobject.accel.Transform;
import com.csc.fs.logging.Logger;
import com.csc.fsg.nba.vo.NbaDataFeedRequest;

/**
 * Transformation services for the Data Feed Service to prepare the incoming request for
 * consumption by a business process.  The disassembler populates a <code>Transform</code>
 * data object from the <code>WebServiceCall</code> value object.  The assembler separates
 * the TXLife transaction from the soap message and determines the appropriate keys needed
 * by the business process.
 * The result is a <code>NbaDataFeedRequest</code> populated with a contract number, user name,
 * time stemap and TXLife XML transaction.
 * request.
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

public class NbaDataFeedRequestTransformAssembler extends AccelTransformation {
	private final static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:sszzz";

	private final static int TIME_LENGTH = 8;

	private final static SimpleDateFormat formatFullTime = new SimpleDateFormat(DATE_TIME_FORMAT);

	protected static Logger logger = null;
	
	//Contstants used to parse out the TXLife from the SOAP package
	private final static String TXLIFE_BEGIN = "<TXLife";
	private final static String TXLIFEREQUEST_END = "</TXLifeRequest>";
	private final static String TXLIFE_END = "</TXLife>";
	
	//Constants used to find values in the TXLife XML
	private final static String POLICY_NUMBER = "PolNumber";  //XML tag Name for Policy Number
	private final static String USER_LOGIN_NAME = "UserLoginName"; //XML tag Name for User ID
	private final static String TRANS_EXE_DATE = "TransExeDate"; //XML tag Name for extract creation date
	private final static String TRANS_EXE_TIME = "TransExeTime"; //XML tag Name for extract creation time
	
	//Constants used in parsing fields from the TXLife
	private final static String TAG_BEGIN = "<"; 
	private final static String TAG_END = ">"; 
	private final static String ENDING_TAG_BEGIN = "</"; 
	
	
	


	/**
	 * Populates a <code>Transform</code> data object from the <code>WebServiceCall</code>
	 * value object.
	 * @param input WebServiceCall
	 * @return
	 */
	public Result disassemble(Object input) {
		Transform transdo = new Transform();
		transdo.setPayload(((WebServiceCall) input).getPayload());

		Result result = new AccelResult();
		result.addResult(transdo);
		return result;
	}

	/**
	 * Returns a <code>Result</code> with a <code>NbaDataFeedRequest</code>
	 * populated with a contract number, user ID, time stamp and TXLife XML transaction 
	 * from the incoming request.  The expected input is a <code>Transform</code> data
	 * based on information object containing and TXLife transaction as part of a SOAP message.
	 * @param request
	 * @return
	 */
	public Result assemble(Result request) {
		Transform transform = (Transform) request.getFirst();
		String payload = transform.getPayload();

		NbaDataFeedRequest wsRequest = new NbaDataFeedRequest();

		int begin = payload.indexOf(TXLIFE_BEGIN);
		if (begin >= 0) {
			String xmlRequest = createDataFeedRequestXML(payload);
			wsRequest.setContractKey(findValue(POLICY_NUMBER, xmlRequest));
			wsRequest.setUserID(findValue(USER_LOGIN_NAME, xmlRequest));
			wsRequest.setTimeStamp(getTimeStamp(xmlRequest));
			wsRequest.setFeedData(xmlRequest);
			if (getLogger().isDebugEnabled()) {
				StringBuffer sb = new StringBuffer(150);
				sb.append("NbaDataFeedWebservice, input parameters: ContractKey:");
				sb.append(wsRequest.getContractKey());
				sb.append(" User ID:");
				sb.append(wsRequest.getUserID());
				sb.append(" Time stamp:");
				sb.append(wsRequest.getTimeStamp().toString());
				sb.append(" Feed Data:");
				sb.append(wsRequest.getFeedData());
			}
		}

		Result result = new AccelResult();
		result.addResult(wsRequest);
		return result;
	}

	/**
	 * Create the TXLife XML string to send to the Data Base. This assumes that UserName and Password or BinarySecurityToken
	 * tags have been added to the TXLife for the web service authorization after the TXLifeRequest ending tag. The 
	 * <Username> and <Password> or <BinarySecurityToken> will be removed.
	 * @param payload
	 * @param begin
	 * @return
	 */
	protected String createDataFeedRequestXML(String payload) {
		//removes the SOAP package from around the TXLife and the userid/pw needed by the Accel Web Service
		int begin = payload.indexOf(TXLIFE_BEGIN);
		int end = payload.lastIndexOf(TXLIFEREQUEST_END) + 16; //16 is the length of </TXLifeRequest>
		String xmlRequest = payload.substring(begin, end);
		//The user ID And password for the web service login follow the TxLifeRequest before the TXLife closing.
		xmlRequest = xmlRequest.concat(TXLIFE_END);

		return xmlRequest;
	}

	/**
	 * Returns the date and time stamp from the TXLife transaction
	 * request.  The filename will have the following naming convention:
	 * <br><b>FileControlID_TransType_TransSubType</b><br> where FileControlID and TransSubType
	 * could be optional.
	 * @param xmlRequest
	 * @return
	 */
	protected Date getTimeStamp(String xmlRequest) {
		StringBuffer dateBuffer = new StringBuffer();
		dateBuffer.append(findValue(TRANS_EXE_DATE, xmlRequest));
		dateBuffer.append(" ");
		dateBuffer.append(formatTime(findValue(TRANS_EXE_TIME, xmlRequest)));
		Date date;
		try {
			date = formatFullTime.parse(dateBuffer.toString());
		} catch (ParseException e) {
			StringBuffer sb = new StringBuffer(150);
			sb.append("DataFeed - A problem has occured parsing the time '");
			sb.append(dateBuffer);
			getLogger().warn(sb.toString());
			date = new Date(System.currentTimeMillis());
		}
		return date;
	}

	/**
	 * As of the WAS 5.1 JRE, the SimpleDateFormat.parse method no longer supports
	 * a colon in the timezone (ie. 12:00:00-06:00).  This is the ISO 8601 format
	 * required by the ACORD XML must be modified before it is parsed.  Therefore,
	 * the following code changes the above example to (12:00:00-0600) for parsing.
	 * @param String time from extract
	 * @return String Formatted Time for database insertion
	 */
	protected String formatTime(String str) {
		String strTime = str;
		if (str.length() > TIME_LENGTH) {
			int lastColon = str.lastIndexOf(':');
			if (lastColon > TIME_LENGTH) {
				strTime = str.substring(0, lastColon) + str.substring(lastColon + 1);
			}
		}
		return strTime;
	}

	/**
	 * Returns the value for a given tag from the specified XML request.
	 * @param tagName
	 * @param xmlRequest
	 * @return
	 */
	protected String findValue(String tagName, String xmlRequest) {
		String startTag = TAG_BEGIN + tagName;
		int begin = xmlRequest.indexOf(startTag);
		if (begin >= 0) {
			begin = xmlRequest.indexOf(TAG_END, begin) + 1;
			int end = xmlRequest.indexOf(ENDING_TAG_BEGIN, begin);
			return xmlRequest.substring(begin, end);
		}
		return null;
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static synchronized Logger getLogger() {
		if (logger == null) {
			try {
				logger = Logger.getLogger(NbaDataFeedRequestTransformAssembler.class.getName());
			} catch (Exception e) {
				//NbaBootLogger.log("ProductDatabaseAccessor could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
