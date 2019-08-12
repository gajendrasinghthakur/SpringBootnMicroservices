package com.csc.fsg.nba.process.test;

/*
 * **************************************************************************<BR>
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
 *     Copyright (c) 2002-2009 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaTestWebServiceRequest;
import com.csc.fsg.nba.webservice.client.NbaTestWebService;

/**
 * Supports the processing of a request XML transaction destined for an web service.
 * A response XML transaction can be placed on a hard drive where it can be retrieved.
 * It also supports using a style sheet to use information from the request to 
 * simulate a more realistic response.  This business process requires a
 * <code>NbaTestWebServiceRequest</code> value object as input.  The response is 
 * returned in the <code>Result</code>.
 * 
 * The naming convention for the response XML file can be found in the request transformation
 * service:  <code>TestRequestTransformAssembler</code>. 
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

public class TestWebServiceBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaTestWebServiceRequest request = (NbaTestWebServiceRequest) input;
			NbaTestWebService ws = new NbaTestWebService();
			String response = ws.getXmlResponse(request.getXmlRequest(), request.getFileName());
			result.addResult(response);
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
		}
		return result;
	}
}
