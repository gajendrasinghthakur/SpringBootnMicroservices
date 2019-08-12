package com.csc.fsg.nba.business.contract.merge.process;

/**
 * ************************************************************** <BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
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
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;

/** 
 * 
 * This class provides common functions for copying the value of the fields from one NbaContractVO object to another NbaContractVO object.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL016CV</td><td>Axa Life Phase 2</td><td>Product Val - Life 70 Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class NbaCVContractMergeProcessor extends NbaContractMergeProcessorBase {

	private static NbaLogger logger;
	

	public NbaCVContractMergeProcessor() {
		
	}
	
	/**Process the SystemMessageExtension
	 * @param systemExtension
	 * 
	 */
	public void processSystemMessage(SystemMessage systemMessage, Holding pendingHolding) {
		//Life 70 returns system message without following fields
		//1. Related Object ID
		systemMessage.setRelatedObjectID(pendingHolding.getPolicy().getId());
		//2. SystemMessageExtension.MsgValidationType
		SystemMessageExtension sysExtension = NbaUtils.getFirstSystemMessageExtension(systemMessage);
		if(sysExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SYSTEMMESSAGE);
			systemMessage.addOLifEExtension(oLifEExtension);
			sysExtension = oLifEExtension.getSystemMessageExtension();
		}
		//NBLXA-1297 remove code NBLXA-1297
		sysExtension.setMsgValidationType(NbaConstants.SUBSET_INSURANCE);
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
	public static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCVContractMergeProcessor.class);
			} catch (Exception e) {
				NbaBootLogger.log("NbaCVContractMergeProcessor could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

}
