package com.csc.fsg.nba.contract.validator;

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

import com.csc.fsg.nba.foundation.NbaUtils;

/**
 * This interface implements NBA_ANSWERS field validation utility functions for nbA.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL068</td><td>AXA Life Phase 2</td><td>Group Contract Validations</td></tr>
 * <tr><td>P2AXAL065</td><td>AXA Life Phase 2</td><td>Group Requirements</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 */

public class UnAnsweredValidator implements ValidatorBase {

	public boolean isOmitted(Object fieldValue) {
		long value = ((Long) fieldValue).longValue();
		return NbaUtils.isUnanswered(value);
	}

}
