package com.csc.fs.accel.newBusiness.tx203.service;

/*
 * *******************************************************************************<BR>
 * Copyright 2014, Computer Sciences Corporation. All Rights Reserved.<BR>
 *
 * CSC, the CSC logo, nbAccelerator, and csc.com are trademarks or registered
 * trademarks of Computer Sciences Corporation, registered in the United States
 * and other jurisdictions worldwide. Other product and service names might be
 * trademarks of CSC or other companies.<BR>
 *
 * Warning: This computer program is protected by copyright law and international
 * treaties. Unauthorized reproduction or distribution of this program, or any
 * portion of it, may result in severe civil and criminal penalties, and will be
 * prosecuted to the maximum extent possible under the law.<BR>
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXParseException;

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.accel.AccelService;
import com.csc.fs.dataobject.accel.Transform;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaValidator;
import com.csc.fsg.nba.vo.NbaConfiguration;

/**
 * 
 * service class to validate the required fields on the incoming TX203. 
 * If the fields are missing, an error is generated and returned to the caller.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead> <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th> </thead>
 * <tr>
 * <td>APSL4508 Websphere 8.5.5 Upgrade</td>
 * </tr>
 * </table>
 * <p>
 */

public class RequestValidationTX203WS extends AccelService {

    /**
     * This method supports validation of a request XML file.
     * 
     * @param input requires a TXLife request object
     * @return the TXLife response is returned in the Result.
     */
	public Result execute(Result result) {
		try {
			result = validation(result);
		} catch (Throwable t) {
			addExceptionMessage(result, t);
		}
		return result;
	}
    
    
    /**
     * Process xml validation on incoming XML203 request against nbA acord schema.
     * 
     * @param result
     * @return Result object
     */
	private Result validation(Result result) {
		Transform transdo = (Transform) result.getFirst();
		StringBuffer txLifeStrBuf = null;
		String txLifeStr = transdo.getPayload();
		String schemaPath;
		try {
			schemaPath = NbaConfiguration.getInstance().getFileLocation("acordSchemaTX203");
			// Insert the schema against which the request will be validated
			if (txLifeStr != null && txLifeStr.indexOf("<TXLife") > -1) {
				// Remove SOAP header and footer. The 9 returns the index of the last character of </TXLife>
				txLifeStr = txLifeStr.substring(txLifeStr.indexOf("<TXLife"), txLifeStr.indexOf("</TXLife>") + 9);
				txLifeStrBuf = new StringBuffer(txLifeStr);
				int start = txLifeStr.indexOf("<TXLife");
				int end = txLifeStr.indexOf(">", start) + 1;
				// Setting up schema in txLife tag with namespace for validation
				txLifeStrBuf.replace(start, end, "<TXLife xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xsi:schemaLocation=\""
						+ schemaPath + "\">");
				NbaValidator validator = new NbaValidator();
				validator.validate(txLifeStrBuf.toString());
				List<SAXParseException> expList = validator.getExpList();
				List msgList = new ArrayList<Message>();
				Message msg = null;
				String error = null;
				for (SAXParseException exception : expList) {
					msg = new Message();
					error = exception.getMessage();
					msg = msg.setVariableData(new String[] { error });
					msgList.add(msg);
				}
				int size = msgList.size();
				if (size > 0) {
					result.setErrors(true);
					result.addMessages(msgList);
				}
			}
		} catch (NbaBaseException e) {
			addExceptionMessage(result, e);
		}

		return result;

	}
}