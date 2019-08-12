package com.csc.fsg.nba.contract.validator;


/*
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import java.util.Date;
import java.util.List;

import com.csc.fsg.nba.foundation.NbaUtils;

/**
 * This class implements general field validation utility functions for nbA.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA297</td><td>Version 1201</td><td>Suitability</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 1201
 * @since New Business Accelerator - Version 1201
 */

public class DefaultValidator implements ValidatorBase{
	
	public boolean isOmitted(Object fieldValue) {
		if (fieldValue instanceof String) {
			return fieldValue == null || ((String) fieldValue).trim().length() == 0;
		} else if (fieldValue instanceof Double) {
			return fieldValue == null || ((Double) fieldValue).isNaN() || NbaUtils.isEqualToZero(fieldValue) || NbaUtils.isLessThanZero(fieldValue);
		} else if (fieldValue instanceof Long) {
			return fieldValue == null || NbaUtils.isNull(((Long) fieldValue).longValue());
		} else if (fieldValue instanceof List) {
			return fieldValue == null || ((List) fieldValue).isEmpty();
		} else if (fieldValue instanceof Date) {
			return fieldValue == null;
		} else if (fieldValue instanceof Integer) {
			return fieldValue == null || NbaUtils.isLessThanZero(fieldValue);
		}
		//APSL5015 to add a check for boolean value
		else if (fieldValue instanceof Boolean) {
			return fieldValue == null || (Boolean)fieldValue==false ;
		}
		return fieldValue == null;
	}
}
