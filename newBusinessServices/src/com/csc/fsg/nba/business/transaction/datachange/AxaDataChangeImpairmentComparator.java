
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
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;

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
 * <td>ALS2611</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaDataChangeImpairmentComparator extends AxaDataChangeComparator {
	
	protected ImpairmentInfo oldImpairmentInfo;

	protected ImpairmentInfo newImpairmentInfo;


	/**
	 * @param oldParty
	 * @param newParty
	 */
	public AxaDataChangeImpairmentComparator(ImpairmentInfo newImpairmentInfo, ImpairmentInfo oldImpairmentInfo) {
		super();
		this.newImpairmentInfo = newImpairmentInfo;
		this.oldImpairmentInfo = oldImpairmentInfo;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isCreditChanged() {
		return !matchAttributes(newImpairmentInfo.getCredit(), oldImpairmentInfo.getCredit());
	}
	
	/**
	 * 
	 * @return
	 */	
	public boolean isDebitChanged() {
		return !matchAttributes(newImpairmentInfo.getDebit(), oldImpairmentInfo.getDebit());
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isDurationChanged() {
		return !matchAttributes(newImpairmentInfo.getImpairmentDuration(), oldImpairmentInfo.getImpairmentDuration());
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isPermFlatAmtChanged() {
		return !matchAttributes(newImpairmentInfo.getImpairmentPermFlatExtraAmt(), oldImpairmentInfo.getImpairmentPermFlatExtraAmt());
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isTempFlatAmtChanged() {
		return !matchAttributes(newImpairmentInfo.getImpairmentTempFlatExtraAmt(), oldImpairmentInfo.getImpairmentTempFlatExtraAmt());
	}	

}
