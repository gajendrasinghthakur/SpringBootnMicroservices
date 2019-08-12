package com.csc.fsg.nba.process.test;

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
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;

/**
 * Transforms the output from the Test Web Service and places the response payload
 * in a <code>WebServiceCall</code> value object for return to the caller.
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
public class TestResponseTransformBP extends AccelBP {

	/*(non-Javadoc) 
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object request) {
		
		Result result = callService(ServiceCatalog.TEST_RESPONSE_DISASSEMBLER, request);
		if (!result.hasErrors()) {
			result = callService(ServiceCatalog.TEST_RESPONSE_ASSEMBLER, result);
		} else {
			result.getData().clear();
		}

		return result;
	}
}