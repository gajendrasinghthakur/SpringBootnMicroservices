package com.csc.fsg.nba.vpms;

import java.util.StringTokenizer;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;

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

/**
 * Holder for VPMS results for auto closure records in replacement processing
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 */
public class NbaReplacementHoldData extends NbaVpmsData {
	private String closureDate = null;
	private String closureType = "";
	private String closureInd = "";
	public NbaReplacementHoldData() {
		super();
	}
	public NbaReplacementHoldData(VpmsComputeResult aResult) throws NbaVpmsException {
		super(aResult);
		parseResults();
	}
	public void parseResults() throws NbaVpmsException {
		if (result.getReturnCode() == 0) {
			parseResults(result.getResult());
			result.setResult(String.valueOf(result.getReturnCode()));
		}
	}
	/**
	* Parses the data from the results of the VPM model 
	* @param result Result of VPMS model
	* @throws NbaVpmsException
	*/
	protected void parseResults(String result)  {
		NbaStringTokenizer resultToken = new NbaStringTokenizer(result.trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
		String closureDate = null;
		String closureType = "";
		String closureInd = "";
		if (resultToken.countTokens() == 3) {
			setClosureDate(resultToken.nextToken());
			setClosureType(resultToken.nextToken());
			setClosureInd(resultToken.nextToken());
	}
}
	/**
	 * @return Returns the closureDate.
	 */
	public String getClosureDate() {
		return closureDate;
	}
	/**
	 * @param closureDate The closureDate to set.
	 */
	public void setClosureDate(String closureDate) {
		this.closureDate = closureDate;
	}
	/**
	 * @return Returns the closureInd.
	 */
	public String getClosureInd() {
		return closureInd;
	}
	/**
	 * @param closureInd The closureInd to set.
	 */
	public void setClosureInd(String closureInd) {
		this.closureInd = closureInd;
	}
	/**
	 * @return Returns the closureType.
	 */
	public String getClosureType() {
		return closureType;
	}
	/**
	 * @param closureType The closureType to set.
	 */
	public void setClosureType(String closureType) {
		this.closureType = closureType;
	}
}
