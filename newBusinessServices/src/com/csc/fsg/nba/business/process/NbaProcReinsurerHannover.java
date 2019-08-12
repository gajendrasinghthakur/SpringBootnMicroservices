package com.csc.fsg.nba.business.process;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import com.csc.fsg.nba.exception.NbaBaseException;

/**
 * <code>NbaProcReinsurerHannover</code> handles communications between nbAccelerator
 * and AXA EIB for Hannover reinsurance.  It extends the NbaProcReinsurerCommunications class, which drives the process,
 * and supplies Hannover specific functionality.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.32</td><td>Axa Life Phase 2</td><td>Reinsurance Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaProcReinsurerHannover extends NbaProcReinsurerCommunications {
	/**
	 * NbaProcReinsurerSwiss default constructor.
	 */
	public NbaProcReinsurerHannover() {
		super();
	}
	/**
	 * For Hannover processing,  this will sets the URL (path) from the NbaConfiguration file.
	 */
	public void initializeTarget() throws NbaBaseException {
		setTarget(getConfigRien().getUrl());
	}
}
