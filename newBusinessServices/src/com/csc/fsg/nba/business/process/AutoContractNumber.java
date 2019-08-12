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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */

import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;

/**
 * This interface is a specification to be implemented by contract number generators.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SPR3614</td><td>AXA Life Phase 1</td><td>JVPMS Memory leak in Auto Contract Numbering logic</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1001
 * @since New Business Accelerator - Version NB-1001
 */

public interface AutoContractNumber {

	public final static String AUTO_CONTRACT_NUMBERING = "AutoContractNumbering";

	/**
	 * Generates the contract number based on data input  
	 * @param inputData List input containing the input object(s).
	 * @return contract number in form of a String
	 * @throws NbaBaseException
	 */
	public String generateContractNumber(List inputData) throws NbaBaseException;
}
