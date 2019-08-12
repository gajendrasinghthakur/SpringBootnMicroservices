package com.csc.fsg.nba.business.process;
/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

/**
 * Drives the aggregate contract process.  Controls the aggregation of work items being sent to the  
 * PDCM queue for processing. 
 * Works on all the work items sent to "Aggregate Contract 8" queue and will perform aggregation of  
 * the work items into NBAGGCNT work item and routes this aggregated NBAGGCNT work item to PDCM    
 * queue or to "End" queue depending upon the business logic.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL3881</td><td>Discretionary</td><td>Follow The Sun</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class NbaProcAggregateContract8 extends NbaProcAggregateContract {

	
	/**
	 * NbaProcAggregateContract6 constructor comment.
	 */
	public NbaProcAggregateContract8() {
		super();
	}
	
}
