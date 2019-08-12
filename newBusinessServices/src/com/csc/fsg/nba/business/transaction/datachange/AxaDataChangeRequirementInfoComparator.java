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
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
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

public class AxaDataChangeRequirementInfoComparator extends AxaDataChangeComparator {
	protected RequirementInfo oldReqInfo;

	protected RequirementInfo newReqInfo;

	protected RequirementInfoExtension newReqInfoExtension;

	protected RequirementInfoExtension oldReqInfoExtension;

	/**
	 * @param oldReqInfo
	 * @param newReqInfo
	 */
	public AxaDataChangeRequirementInfoComparator(RequirementInfo newReqInfo, RequirementInfo oldReqInfo) {
		super();
		this.oldReqInfo = oldReqInfo;
		this.newReqInfo = newReqInfo;
		oldReqInfoExtension = NbaUtils.getFirstRequirementInfoExtension(oldReqInfo);
		newReqInfoExtension = NbaUtils.getFirstRequirementInfoExtension(newReqInfo);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isRequirementStatusChanged() {
		if (newReqInfo != null && oldReqInfo != null) {
			return !matchAttributes(newReqInfo.getReqStatus(), oldReqInfo.getReqStatus());
		}
		//NBLXA-2402 (NBLXA-2602) US#297686 | Start
		if (newReqInfo != null && oldReqInfo == null) {
			return true;
		}
		//NBLXA-2402 (NBLXA-2602) US#297686 | End
		return false;
	}


	public boolean isReceived(){
		if ( newReqInfo != null ){
			return matchAttributes(newReqInfo.getReqStatus(), NbaOliConstants.OLI_REQSTAT_RECEIVED);
		}
		return false;
	}
//	APSL1441 - New Method
	public boolean isWaived(){
		if ( newReqInfo != null ){
			return matchAttributes(newReqInfo.getReqStatus(), NbaOliConstants.OLI_REQSTAT_WAIVED);
		}
		return false;
	}
//	APSL4280 - New Method	
	public boolean isApproved(){
		if ( newReqInfo != null ){
			return matchAttributes(newReqInfo.getReqStatus(), NbaOliConstants.OLI_REQSTAT_APPROVED);
		}
		return false;
	}
//	APSL4280 - New Method
	public boolean isCompleted(){
		if ( newReqInfo != null ){
			return matchAttributes(newReqInfo.getReqStatus(), NbaOliConstants.OLI_REQSTAT_COMPLETED);
		}
		return false;
	}
}
