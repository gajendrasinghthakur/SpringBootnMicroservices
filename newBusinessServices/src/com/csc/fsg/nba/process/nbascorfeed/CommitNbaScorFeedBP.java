package com.csc.fsg.nba.process.nbascorfeed;

/*************************************************************************
*
* Copyright Notice (2005)
* (c) CSC Financial Services Limited 1996-2005.
* All rights reserved. The software and associated documentation
* supplied hereunder are the confidential and proprietary information
* of CSC Financial Services Limited, Austin, Texas, USA and
* are supplied subject to licence terms. In no event may the Licensee
* reverse engineer, decompile, or otherwise attempt to discover the
* underlying source code or confidential information herein.
*
*************************************************************************/
import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;

/**
 * NbaDataFeedService - This services invokes the
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808-NBA-SCOR</td><td>AXA Life</td><td>Simplified Issue</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 *  
 */
public class CommitNbaScorFeedBP extends AccelBP {
	public Result process(Object request) {
		Result result = null;
		result = callService(ServiceCatalog.COMMITNBASCORFEED_DISASSEMBLER, request);
		if (!result.hasErrors()) {
			// Invoke Service CommitNbaScorFeedBP
			result = callService(ServiceCatalog.COMMITNBASCORFEED_SERVICE, result);
			if (!result.hasErrors()) {
				result = callService(ServiceCatalog.COMMITNBASCORFEED_ASSEMBLER, result);
			}
		}
		return result;
	}
}