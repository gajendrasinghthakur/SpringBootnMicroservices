package com.csc.fsg.nba.business.process;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * This class serves as a proxy to the functionality available on the 
 * <code>NbaAutomatedProcess</code> class.  Not all functions on
 * <code>NbaAutomatedProcess</code> make sense outside the inheritance graph, 
 * so care should be executed before calling methods on this proxy.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA035</td><td>Version 3</td><td>Application Submit Transaction to nbA Pending Database</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 1
 */
public class NbaAutoProcessProviderProxy extends NbaAutomatedProcess {
/**
 * NbaAutoProcessProviderProxy constructor.
 * @param user the AWD user
 * @param aCase the case to be inquired upon
 */
public NbaAutoProcessProviderProxy(NbaUserVO user, NbaDst aCase) throws NbaBaseException{
	super();
	this.initialize(user, aCase);
}
/**
 * Initialize automated process context.
 * @param user the AWD automated process user that the work was returned for
 * @param work a DST value object for which the process is to occur
 * @return information about the success or failure of the process
 */
public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
	// Initialization
	initialize(user, work);
	if (getResult() == null) {
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
	}
	return getResult();
}

/**
 * NbaAutoProcessProviderProxy constructor.
 * @param user the AWD user
 * @param aCase the case to be inquired upon
 */
// NBA035 NEW METHOD
public NbaAutoProcessProviderProxy(NbaUserVO user, NbaDst aCase, boolean retrieveContract) throws NbaBaseException{
	super();
	autoProxyRetrieveContract = retrieveContract;
	this.initialize(user, aCase);
}

}
