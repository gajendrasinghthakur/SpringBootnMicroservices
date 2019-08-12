package com.csc.fsg.nba.process.tx203;

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

import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaConfiguration;
 
/**
 * TX203WebServiceBP is used for do Contract Inquiry.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead> <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th> </thead>
 * <tr>
 * <td>NBA326</td><td>Websphere 8.5.5 Upgrade</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @since New Business Accelerator - APSL4508
 */

public class TX203TransformInboundBP extends AccelBP {

    /**
     * This method supports validation and Transform.
     * 
     * @param input requires a TXLife request object
     * @return the TXLife response is returned in the Result.
     */
	public Result process(Object request) {

		Result result = callService(ServiceCatalog.TX203_WEBSERVICE_DISASSEMBLER, request);
	    // Build TXLife response, process errors
		if (result.hasErrors()) {
			result = callService(ServiceCatalog.TX203_WEBSERVICE_ASSEMBLER, result);
			result.setErrors(true);
			return result;
		}
		return result;

	}
}
