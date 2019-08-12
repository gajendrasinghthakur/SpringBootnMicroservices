package com.csc.fsg.nba.process.tx151;

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

public class TX151TransformInboundBP extends AccelBP {

    /**
     * This method supports validation and Transform.
     * 
     * @param input requires a TXLife request object
     * @return the TXLife response is returned in the Result.
     */
	public Result process(Object request) {
		Result result = callService(ServiceCatalog.TX151_DISASSEMBLER, request);
    	return result;
	}
}
