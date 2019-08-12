package com.csc.fsg.nba.process.tx1122;

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
import com.csc.fsg.nba.assembler.tx1122.TX1122Assembler;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;

/**
 * Business Process to Transform an outbound response for an ACORD TxLife transaction. 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td> APSL4508 Soap to Axis2 conversion,Websphere 8.5.5 Upgrade</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class Transform1122OutboundBP extends AccelBP {
	protected static NbaLogger logger = null;
	public Result process(Object request) {
		Result result = callService(ServiceCatalog.TXLIFE_DISASSEMBLER, ((List) request).get(0));
		if (!result.hasErrors()) {
			result = invoke(ServiceCatalog.TXLIFE_TRANSFORM_OUTBOUND , result.getData());
		}
		if (!result.hasErrors()) {
			Transform transform = (Transform) result.getFirst();
			result = new AccelResult();
			String transformResult = transform.getPayload();
			transformResult = formatXml(transformResult);
			WebServiceCall webserviceCall = new WebServiceCall();
			webserviceCall.setResponse(transformResult);
			result.addResult(webserviceCall);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Transform1122OutboundBP Finished Response: " + transformResult);
			}
			return result;
		}
		result.getData().clear();
		return result;
	}
	
	protected String formatXml(String reqXml)
	{
		String startXml="<submitRequirementResultResponse> <return>";
		String endXml="</return> </submitRequirementResultResponse>";
	    return startXml+reqXml+endXml;
	
	}
	
	
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(TX1122Assembler.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("Transform1122OutboundBP could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}	
	
}