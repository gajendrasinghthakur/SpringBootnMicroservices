package com.csc.fsg.nba.process.datafeed;

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
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fsg.nba.process.NewBusinessAccelBP;

/**
 * Supports the storing of a request XML transaction into a data base table
 * This business process requires a <code>NbaDataFeedRequest</code> value object as input.  
 * The TXLife response is returned in the <code>Result</code>.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA232</td><td>Version 8</td><td>nbA Feed for a Customer's Web Site</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class NbaDataFeedWebServiceBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		Result result = callService(ServiceCatalog.DATA_FEED_WEBSERVICE_DISASSEMBLER, input);

		if (!result.hasErrors()) {
			result = invoke("hibernate/CommitDataFeed", result.getData());
		} else {
			result.getData().clear();
		}
		//Build TXLife response.This will add errors to the TXLife if they exist

		result = callService(ServiceCatalog.DATA_FEED_WEBSERVICE_ASSEMBLER, result);

		return result;
	}
}
