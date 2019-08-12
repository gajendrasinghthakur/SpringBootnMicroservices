package com.csc.fs.accel.newBusiness.markup;

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

import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.OLifE;

/**
 * HoldingInquiryMarkup performs base implementation of modifying the internal representation
 * of the ACORD 203 transaction prior to the transaction being created as XML.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>Holding Inquiry (XML203) Service AttachmentData Object Needs Character Data Markup</td></tr>
 * <tr><td>APSL4508</td>Websphere 8.5.5 Upgrade</tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * * @see com.csc.fs.accel.newBusiness.markup.BaseContractMarkup
 */

public class HoldingInquiryMarkup extends BaseContractMarkup implements ContractMarkup {

	
	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.access.contract.ContractMarkup#markup(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	@Override
	public NbaTXLife markup(NbaTXLife nbatxlife) {
		if (LogHandler.Factory.isLogging(LogHandler.LOG_LOW_LEVEL_DEBUG)) {
			LogHandler.Factory.LogLowLevelDebug(this, "Holding Inquiry Pre-markup:\n" + nbatxlife.toXmlString());
		}

		markupOLifE(nbatxlife);

		if (LogHandler.Factory.isLogging(LogHandler.LOG_DEBUG)) {
			LogHandler.Factory.LogDebug(this, "Holding Inquiry Post-markup:\n" + nbatxlife.toXmlString());
		}
		return nbatxlife;
	}
}
