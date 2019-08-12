package com.csc.fsg.nba.process.datafeed;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */

/**
 * NbaDataFeedBP transforms the data feed and passes it to the data feed service for 
 * forwarding to the next process.
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA232</td><td>Version 8</td><td>nbA Feed for a Customer's Web Site</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 *
 */
public class NbaDataFeedBP extends AccelBP {

	/* Calls the disassembler to pull values from the NbaTXLife (input)
	 * and pass them to the datafeed service.
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 * @param input a NbaTXLife object
	 * @return Result containing success or failure of the process
	 */
	public Result process(Object input) {
        Result result = callService(ServiceCatalog.DATAFEED_DISASSEMBLER, input); 
        if (!result.hasErrors()) {
            result = callService(ServiceCatalog.DATAFEED_SERVICE, result);
            if (!result.hasErrors()) {
                result = callService(ServiceCatalog.DATAFEED_ASSEMBLER, result);
            }
        }
        return result;
	}

}
