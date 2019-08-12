package com.csc.fsg.nba.vpms; //NBA201
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
 
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.business.process.NbaAutoContractNumber;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
/**
 * This class represents the result of a vpms computation for the CompanionCaseSuspend model.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA033</td><td>Version 3</td><td>Companion Case</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaAutoContractNumber
 * @since New Business Accelerator - Version 3
 */

public class NbaVpmsCompanionCaseData extends NbaVpmsData {
	protected int applicableSuspendTime;
	protected int maxSuspendTime;
	protected String unit;
	/**
	 * NbaVpmsCompanionCaseData constructor passes the <code>VpmsComputeResult</code> object
	 * to the super class.
	 * @param aResult the results of a call to the CompanionCaseSuspend VPMS model.
	 * @throws NbaVpmsException 
	 */
	public NbaVpmsCompanionCaseData(VpmsComputeResult aResult) throws NbaVpmsException {
		super(aResult);
		parseResults(); // NBA021
	}
	/**
	 * Returns the applicableSuspendDays.
	 * @return int
	 */
	public int getApplicableSuspendTime() {
		return applicableSuspendTime;
	}

	/**
	 * Returns the maxSuspendDays.
	 * @return int
	 */
	public int getMaxSuspendTime() {
		return maxSuspendTime;
	}

	/**
	 * Returns the unit.
	 * @return String
	 */
	public String getUnit() {
		return unit;
	}
	/**
	* Parses the data from the results of the VPM model. A default delimiter is used for parsing. 
	* @throws NbaVpmsException
	*/
	public void parseResults() throws com.csc.fsg.nba.exception.NbaVpmsException {
		if (result.getReturnCode() == 0) {
			NbaStringTokenizer tokens = new NbaStringTokenizer(result.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			while (tokens.hasMoreTokens()) {
				applicableSuspendTime= Integer.parseInt(tokens.nextToken());
				maxSuspendTime = Integer.parseInt(tokens.nextToken());
				unit = tokens.nextToken();
			}
		}
		result.setResult(String.valueOf(result.getReturnCode()));
	}
}
