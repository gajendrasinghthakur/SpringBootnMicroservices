
package com.csc.fsg.nba.business.transaction.datachange;
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
import java.util.List;

import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProducerVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Producer;

/**
 * 
 * Helper classes to determine Data change 
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */


public class AxaDataChangeComparator {
	/**
	 * Compare two string attributes
	 * @param s_newValue
	 * @param s_oldValue
	 * @return status
	 */
	protected boolean matchAttributes(String s_newValue, String s_oldValue) {
		boolean matchFound = false;
		if (s_newValue != null && s_oldValue != null) {
			if ((s_newValue.equalsIgnoreCase(s_oldValue))) {
				matchFound = true;
			}
		} else if ((s_newValue == null && s_oldValue == null)) {
			matchFound = true;
		} else if ((s_newValue == null && (s_oldValue != null && s_oldValue.trim().length() == 0))
				|| (s_oldValue == null && (s_newValue != null && s_newValue.trim().length() == 0))) {
			matchFound = true;
		}
		return matchFound;
	}

	/**
	 * Compare two long attributes
	 * @param l_newValue
	 * @param l_oldValue
	 * @return status
	 */
	protected boolean matchAttributes(long l_newValue, long l_oldValue) {
		boolean matchFound = false;
		if (l_newValue == l_oldValue) {
			matchFound = true;
		}
		return matchFound;
	}

	/**
	 * Compare two double attributes
	 * @param d_newValue
	 * @param d_oldValue
	 * @return status
	 */
	protected boolean matchAttributes(double d_newValue, double d_oldValue) {
		if (Double.isNaN(d_newValue) && Double.isNaN(d_oldValue)) {//ALS2611
			return true;
		}
		if (d_newValue == d_oldValue) {
			return true;
		}
		return false;
	}
	/**
	 * Compare two date attributes
	 * @param dt_newValue
	 * @param dt_oldValue
	 * @return status
	 */
	//APSL1451 New Method
	protected boolean matchAttributes(java.util.Date dt_newValue, java.util.Date dt_oldValue) {
		boolean matchFound = false;
		if (dt_newValue == null && dt_oldValue == null) {
			matchFound = true;
		} else if (dt_newValue != null && dt_oldValue != null && dt_newValue.equals(dt_oldValue)) {
			matchFound = true;
		}
		return matchFound;
	}
}
