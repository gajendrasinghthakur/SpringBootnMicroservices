package com.csc.fsg.nba.business.process;
/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */
/**
 * NbaProcContractPrint processes Transaction work items found in NBPRINT queue. If the 
 * Transaction aleady contains a NBPRTEXT Source item, the Transaction is a reprint request.
 * Otherwise it is an initial print request. For initial print requests, a new NBPRTEXT Source item
 * is created and added to the Transaction. An NBPRTEXT Source item is a Text file which contains  
 * a TXLife XML file.  The TXLife contains TXLifeRequest objects for each Contract Print Extract
 * type identified in the "EXTC" LOB field of the Transaction.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL5100</td><td>Discretionary</td><td>Print Preview</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 2
 */
public class NbaProcContractPrint2 extends NbaProcContractPrint {


	/**
	 * NbaProcContractPrint constructor.
	 */
	public NbaProcContractPrint2() {
		super();
	}
	

}