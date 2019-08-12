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


package com.csc.fsg.nba.process.datafeed;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fs.dataobject.accel.*;

/**
 * Implementation of the Business Process Class CommitDataFeedBP
 * 
 * @author: 
 */
public class CommitDataFeedBP extends AccelBP {

	public Result process(Object request) {
		Result result = null;
		result = callService(ServiceCatalog.COMMITDATAFEED_DISASSEMBLER, request);
		if (!result.hasErrors()) {
			// Invoke Service CommitDataFeedBP
			result = callService(ServiceCatalog.COMMITDATAFEED_SERVICE, result);
			if (!result.hasErrors()) {
				result = callService(ServiceCatalog.COMMITDATAFEED_ASSEMBLER, result);
			}
		}
		return result;
	}
}
/**
 * This part documents the development status of classes during the
 * initial phase.  It will be removed during the build process.
 *
 * It uses the CVS keyword substitution to embed information.
 * The commit message should be entered in the format:
 *
 *      scarabid: text
 * 
 * @author   
 * 
 * $Revision$
 * 
 * Changes:
 *  $Log$
 *  Revision 1.1  2007/12/20 23:58:42  jknifto2
 *  *** empty log message ***
 *
 *  Revision 1.3  2005/11/06 03:33:26  jknifto2
 *  *** empty log message ***
 *
 *  Revision 1.2  2005/11/06 00:11:45  jknifto2
 *  *** empty log message ***
 *
 *  *** empty log message ***
 *
 */
