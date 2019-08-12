package com.csc.fsg.nba.process.requirements;
/* 
 * *******************************************************************************<BR>
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.bean.accessors.NbaUnderwriterWorkbenchFacadeBean;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaUnderwriterWorkbenchVO;

/**
 * The Business Process class responsible for transmitting MIB Reports for a contract on the Underwriter Workbench.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SPR3093</td><td>Version 6</td><td>Coding style change in Service Action - use fsConnect Service Controller</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class TransmitMIBReportsBP extends NewBusinessAccelBP {
	protected NbaLogger logger = null;
	
	/* Calls UnderwriterWorkBenchFacadeBean to transmit MIB Reports
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		NbaUnderwriterWorkbenchVO uwVO = (NbaUnderwriterWorkbenchVO)input;
		//NBA213 deleted code
		try {
			NbaUnderwriterWorkbenchFacadeBean uwFacade = new NbaUnderwriterWorkbenchFacadeBean();  //NBA213
			uwVO = uwFacade.transmitMIBReports(uwVO.getNbaUserVO(), uwVO);
		} catch (Exception e) {
			getLogger().logException(e);
			addExceptionMessage(result, e);
			result.setErrors(true);
			return result;			
		} 
		result.addResult(uwVO);
		return result;
	}
	//NBA213 deleted code
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
