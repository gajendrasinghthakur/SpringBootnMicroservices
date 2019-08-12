package com.csc.fsg.nba.datafeed.service;

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

import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelService;
import com.csc.fs.accel.result.AccelResult;

/**
 * NbaDataFeedService invokes the  
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
public class NbaDataFeedService extends AccelService {

	/* Begins the process that will place the data feed on the JMS queue.
	 * @see com.csc.fs.accel.AccelService#execute(com.csc.fs.Result)
	 * @param request an NbaDataFeedDO object containing contract information
	 * @return Result object indicatating success or failure of the process
	 */
	public Result execute(Result request) {
		AccelResult result = null;
		result = invoke("DataFeedTransmission/1203DataFeed", request.getData(), true);
		return result;
	}

}
