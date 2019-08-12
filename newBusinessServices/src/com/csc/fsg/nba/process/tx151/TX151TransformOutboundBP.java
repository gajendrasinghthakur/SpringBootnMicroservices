package com.csc.fsg.nba.process.tx151;

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
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WebServiceCall;
import com.csc.fs.dataobject.accel.Transform;

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
public class TX151TransformOutboundBP extends AccelBP {
	public Result process(Object request) {
		String transformResult = (String) ((List) request).get(0);
		Result result = new AccelResult();
		transformResult = formatResponseXml(transformResult);
		WebServiceCall webserviceCall = new WebServiceCall();
		webserviceCall.setResponse(transformResult);
		result.addResult(webserviceCall);
		return result;
	}

	protected String formatResponseXml(String reqXml) {
		String startXml = "<submitJetResultResponse> <return>";
		String endXml = "</return></submitJetResultResponse>";
		return startXml + reqXml + endXml;
	}

}
