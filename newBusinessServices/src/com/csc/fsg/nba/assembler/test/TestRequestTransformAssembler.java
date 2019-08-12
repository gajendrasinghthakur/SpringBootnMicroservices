package com.csc.fsg.nba.assembler.test;

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

import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WebServiceCall;
import com.csc.fs.dataobject.accel.Transform;
import com.csc.fsg.nba.vo.NbaTestWebServiceRequest;

/**
 * Transformation services for the Test Web Service to prepare the incoming request for
 * consumption by a business process.  The disassembler populates a <code>Transform</code>
 * data object from the <code>WebServiceCall</code> value object.  The assembler separates
 * the TXLife transaction from the soap message and determines an appropriate response
 * filename.  The result is a <code>NbaTestWebServiceRequest</code> populated with a
 * TXLife XML transaction and response filename based on information from the incoming
 * request.
 * 
 * The filename will have the following naming convention:
 * <br><b>[FileControlID]_[TransType]_[TransSubType]_Response</b><br> where FileControlID
 * and TransSubType could be optional depending on the transaction.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA234</td><td>Version 8</td><td>ACORD Transformation Service</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class TestRequestTransformAssembler extends AccelTransformation {

	/**
	 * Populates a <code>Transform</code> data object from the <code>WebServiceCall</code>
	 * value object.
	 * @param input WebServiceCall
	 * @return
	 */
	public Result disassemble(Object input) {
		Transform transdo = new Transform();
		transdo.setPayload(((WebServiceCall)input).getPayload());

		Result result = new AccelResult();
		result.addResult(transdo);
		return result;
	}

	/**
	 * Returns a <code>Result</code> with a <code>NbaTestWebServiceRequest</code>
	 * populated with a TXLife XML transaction and response filename based on information
	 * from the incoming request.  The expected input is a <code>Transform</code> data
	 * object containing and TXLife transaction as part of a SOAP message.
	 * @param request
	 * @return
	 */
	public Result assemble(Result request) {
		Transform transform = (Transform) request.getFirst();
		String payload = transform.getPayload();

		NbaTestWebServiceRequest wsRequest = new NbaTestWebServiceRequest();

		int begin = payload.indexOf("<TXLife");
		if (begin >= 0) {
			int end = payload.indexOf("</TXLife>") + 9;
			String xmlRequest = payload.substring(begin, end);
			wsRequest.setXmlRequest(xmlRequest);
			wsRequest.setFileName(getFileName(xmlRequest));
		}
		
		Result result = new AccelResult();
		result.addResult(wsRequest);
		return result;
	}

	/**
	 * Returns the filename to find the appropriate response for the given <code>TXLife</code>
	 * request.  The filename will have the following naming convention:
	 * <br><b>FileControlID_TransType_TransSubType</b><br> where FileControlID and TransSubType
	 * could be optional.
	 * @param xmlRequest
	 * @return
	 */
	protected String getFileName(String xmlRequest) {
		StringBuffer fileName = new StringBuffer();

		String value = findValue("FileControlID", xmlRequest);
		if (value != null && value.length() > 0) {
			fileName.append(value).append("_");
		}
		fileName.append(findTcValue("TransType", xmlRequest));
		value = findTcValue("TransSubType", xmlRequest);
		if (value != null && value.length() > 0) {
			fileName.append("_").append(value);
		}
		fileName.append("_Response");

		return fileName.toString();
	}

	/**
	 * Returns a tc attribute value for a given tag from the specified XML request.
	 * @param tagName
	 * @param xmlRequest
	 * @return
	 */
	protected String findTcValue(String tagName, String xmlRequest) {
		String startTag = "<" + tagName + " tc=\"";
		int begin = xmlRequest.indexOf(startTag);
		if (begin >= 0) {
			begin += startTag.length();
			int end = xmlRequest.indexOf("\"", begin);
			return xmlRequest.substring(begin, end);
		}
		return null;
	}

	/**
	 * Returns the value for a given tag from the specified XML request.
	 * @param tagName
	 * @param xmlRequest
	 * @return
	 */
	protected String findValue(String tagName, String xmlRequest) {
		String startTag = "<" + tagName;
		int begin = xmlRequest.indexOf(startTag);
		if (begin >= 0) {
			begin = xmlRequest.indexOf(">", begin) + 1;
			int end = xmlRequest.indexOf("</", begin);
			return xmlRequest.substring(begin, end);
		}
		return null;
	}
}
