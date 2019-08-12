package com.csc.fsg.nba.transform.ACORD;

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
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * Business Process to Transform an outbound response for an ACORD TxLife transaction. 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA234</td><td>Version 8</td><td>nbA ACORD Transformation Service Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class TransformOutboundBP extends AccelBP {
	public Result process(Object request) {
		Result result = callService(ServiceCatalog.TXLIFE_DISASSEMBLER, ((List) request).get(0));
		if (!result.hasErrors()) {
			result = invoke(ServiceCatalog.TXLIFE_TRANSFORM_OUTBOUND , result.getData());
		}
		if (!result.hasErrors()) {
			Transform transform = (Transform) result.getFirst();
			result = new AccelResult();
			
			WebServiceCall webserviceCall = new WebServiceCall();
			webserviceCall.setResponse(transform.getPayload());
			result.addResult(webserviceCall);
			return result;
		} else {
			result.getData().clear();
		}
		return result;
	}
}
