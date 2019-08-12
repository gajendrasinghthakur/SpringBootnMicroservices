package com.csc.fsg.nba.business.transaction.datachange;
/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.txlife.CovOption;
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

public class AxaDataChangeCovOptionComparator extends AxaDataChangeComparator {
	protected CovOption oldCovOption;

	protected CovOption newCovOption;

	/**
	 * @param oldCovOption
	 * @param newCovOption
	 */
	public AxaDataChangeCovOptionComparator(CovOption newCovOption, CovOption oldCovOption) {
		super();
		this.oldCovOption = oldCovOption;
		this.newCovOption = newCovOption;
	}
	/**
	 * 
	 * @return
	 */
	public boolean isNewCovOption() {
		return (newCovOption != null && newCovOption.isActionAdd() && oldCovOption == null);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isCovOptionDeleted() {
		return newCovOption != null && oldCovOption != null && NbaUtils.isDeletedOnly(newCovOption);
	}
	/**
	 * APSL4697  
	 * @return
	 */
	public boolean isCovOptionUpdated() {
		return newCovOption != null && newCovOption.isActionUpdate() ;
	}
}
