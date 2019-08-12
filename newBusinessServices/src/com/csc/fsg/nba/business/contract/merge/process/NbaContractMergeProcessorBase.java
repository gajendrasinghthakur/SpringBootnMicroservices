package com.csc.fsg.nba.business.contract.merge.process;

/**
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.SystemMessage;

/** 
 * 
 * This class provides common functions for copying the value of the fields from one NbaContractVO object to another NbaContractVO object.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL016CV</td><td>Axa Life Phase 2</td><td>Product Val - Life70 Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class NbaContractMergeProcessorBase {

	private static NbaLogger logger;
	

	public NbaContractMergeProcessorBase() {
		
	}
	
	/**Process the SystemMessageExtension
	 * @param systemExtension
	 * 
	 */
	public void processSystemMessage(SystemMessage systemMessage, Holding pendingHolding) {
		//Dummy implementation of the base class
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
	public static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractMergeProcessorBase.class);
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractMergeProcessorBase could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

}
