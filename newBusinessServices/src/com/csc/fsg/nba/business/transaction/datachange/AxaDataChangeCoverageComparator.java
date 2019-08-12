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
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
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

public class AxaDataChangeCoverageComparator extends AxaDataChangeComparator {
	protected Coverage oldCoverage;

	protected Coverage newCoverage;

	protected CoverageExtension newCoverageExtension;

	protected CoverageExtension oldCoverageExtension;

	/**
	 * @param oldCoverage
	 * @param newCoverage
	 */
	public AxaDataChangeCoverageComparator(Coverage newCoverage, Coverage oldCoverage) {
		super();
		this.oldCoverage = oldCoverage;
		this.newCoverage = newCoverage;
		oldCoverageExtension = NbaUtils.getFirstCoverageExtension(oldCoverage);
		newCoverageExtension = NbaUtils.getFirstCoverageExtension(newCoverage);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isNewCoverageExtension() {
		return (newCoverageExtension != null && newCoverageExtension.isActionAdd() && oldCoverageExtension == null);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isNewCoverage() {
		return (newCoverage != null && newCoverage.isActionAdd() && oldCoverage == null);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isCoverageDeleted() {
		return newCoverage != null && oldCoverage != null && NbaUtils.isDeletedOnly(newCoverage);//ALS3680 changed the NbaUtils method called
	}
	/**
	 * 
	 * @return
	 */
	public boolean isCurrentAmountChanged() {
		return !isNewCoverage() && !isCoverageDeleted() && newCoverage.hasCurrentAmt() && newCoverage.getCurrentAmt() != oldCoverage.getCurrentAmt();
	}
	
	/**
	 * 
	 * @return
	 */
	//ALPC136 new method added.
	public boolean isModalPremAmtChanged() {
		return !isNewCoverage() && !isCoverageDeleted() && newCoverage.hasModalPremAmt() && newCoverage.getModalPremAmt() != oldCoverage.getModalPremAmt();
	}
}
