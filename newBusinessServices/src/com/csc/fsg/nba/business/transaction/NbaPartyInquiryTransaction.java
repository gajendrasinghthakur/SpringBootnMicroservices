package com.csc.fsg.nba.business.transaction;

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
 * 
 * *******************************************************************************<BR>
 */


/**
 * NbaPartyInquiryTransaction will be used to create NbaTXLife 204 transaction for WebService.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA105</td><td>Version 4</td><td>Underwriting Risk</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.21</td><td>AXA Life Phase 1</td><td>Prior Insurance Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */
public class NbaPartyInquiryTransaction extends NbaBusinessTransactions {
	private String HOLDING_ID = "Holding_1";//AXAL3.7.21
	/**
	 * Constructor for NbaClientSearchTransaction.
	 */
	public NbaPartyInquiryTransaction() {
		super();
	}
}
