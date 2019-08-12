package com.csc.fsg.nba.process.tx203;

/*************************************************************************
 *
 * Copyright Notice (2006)
 * (c) CSC Financial Services Limited 1996-2006.
 * All rights reserved. The software and associated documentation
 * supplied hereunder are the confidential and proprietary information
 * of CSC Financial Services Limited, Austin, Texas, USA and
 * are supplied subject to licence terms. In no event may the Licensee
 * reverse engineer, decompile, or otherwise attempt to discover the
 * underlying source code or confidential information herein.
 *
 *************************************************************************/
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WebServiceCall;

/**
 * Business Process to Transform an outbound response for an ACORD TxLife transaction.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>nbA ACORD Transformation Service Project</td>
 * <tr><td> APSL4508 Soap to Axis2 conversion,Websphere 8.5.5 Upgrade</td></tr>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class TX203TransformOutboundBP extends AccelBP {
	public Result process(Object request) {
	    	String str = (String) ((List) request).get(0);
		    Result result = new AccelResult();
			WebServiceCall webserviceCall = new WebServiceCall();
			str = formatResponseXml(str);
			webserviceCall.setResponse(str);
			result.addResult(webserviceCall);
			return result;
	 }

	protected String formatResponseXml(String reqXml) {
		String startXml = "<SOAP-ENV:Envelope xmlns=\"http://tempuri.org/com.csc.fsg.nba.ejb.webservice.Nba203WebService\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><SOAP-ENV:Body> <getHoldingInquiryResponse> <return>";
		String endXml = "</return></getHoldingInquiryResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>";
		return startXml + reqXml + endXml;
	}

}
