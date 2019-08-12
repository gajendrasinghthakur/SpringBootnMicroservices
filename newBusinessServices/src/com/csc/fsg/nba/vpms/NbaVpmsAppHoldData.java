package com.csc.fsg.nba.vpms; //NBA201

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.business.process.NbaProcAppHold;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;

/**
 * NbaVpmsAppHoldData processes data passed to it by the NbaProcAppHold process.
 * This data comes from the ApplicationHold VP/MS model.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td><tr>
 * <tr><td>SPR1018</td><td>Version 2</td><td>JavaDoc, comments and minor source code changes.</td></tr>
 * <tr><td>NBA058</td><td>Version 3</td><td>Upgrade to J-VPMS version 1.5.0</td></tr>
 * <tr><td>SPR1780</td><td>Version 4</td><td>Update application hold to accept a null return from VPMS</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>NBA119</td><td>Version 5</td><td>Automated Process Suspend</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProcAppHold
 * @since New Business Accelerator - Version 1
 */
public class NbaVpmsAppHoldData extends NbaVpmsData {
	public java.util.ArrayList workItems = new ArrayList(); //SPR1780
/**
 * NbaVpmsAppHoldData default constructor.
 */
public NbaVpmsAppHoldData() {
	super();
}
/**
 * NbaVpmsAppHoldData constructor passes the <code>VpmsComputeResult</code> object
 * to the super class.
 * @param aResult the results of a call to the ApplicationHold VPMS model.
 * @throws NbaVpmsException is thrown if some exception occurs while executing the vpms model.
 */
// NBA058 Changed signature
public NbaVpmsAppHoldData(VpmsComputeResult aResult) throws NbaVpmsException {
	super(aResult);
	parseResults(); // NBA021
}
/**
 * NbaVpmsAppHoldData constructor passes the <code>VpmsComputeResult</code> object
 * to the super class and then parses the results of that object.
 * @param aResult the results of a call to the ApplicationHold VPMS model.
 * @throws NbaVpmsException is thrown if some exception occurs while executing the vpms model.
 */
public NbaVpmsAppHoldData(VpmsComputeResult aResult, String aDelimiter) throws NbaVpmsException {
	super(aResult);
	parseResults(aDelimiter);
}
/**
 * Answers the workItems data members.
 * @return java.util.ArrayList
 */
public java.util.ArrayList getWorkItems() {
	return workItems;
}
/**
 * Parses the data from the results of the VPM model and saves that
 * data in the workItems data member.  A default delimiter is used to tokenize
 * the result message and those tokens are then parsed and stored in the
 * workItems data member.
 * @throws NbaVpmsException is thrown if some exception occurs while executing the vpms model.
 */
public void parseResults() throws com.csc.fsg.nba.exception.NbaVpmsException {
	if (result.getReturnCode() == 0 && !(result.getResult().trim().equals("0") || result.getResult().trim().equals(""))) { //APSL4641 
		//SPR1780 code deleted
		NbaStringTokenizer tokens = new NbaStringTokenizer(result.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[1]); // NBA021
		String aToken; // NBA021  = tokens.nextToken(); // First token is empty - don't need it.
		while (tokens.hasMoreTokens()) {
			aToken = tokens.nextToken();
			StringTokenizer bToken = new StringTokenizer(aToken, NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			String workItem = bToken.nextToken(); //NBA077
			String suspendDays = bToken.nextToken();
			String maxSuspendDays = bToken.nextToken(); //NBA119
			//begin NBA077			
			String source = null;
			if (bToken.hasMoreTokens()) {
				source = bToken.nextToken();
			}
			NbaVpmsWorkItem wi = new NbaVpmsWorkItem(workItem, suspendDays, source, maxSuspendDays);//NBA119
			//end NBA077
			workItems.add(wi);
		}
	}
	result.setResult(String.valueOf(result.getReturnCode()));
}
/**
 * Parses the data from the results of the VPM model and saves that
 * data in the workItems data member.  The passed-in delimiter is used to tokenize
 * the result message and those tokens are then parsed and stored in the
 * workItems data member.
 * @param aDelimiter the value used by the VPM model to separate responses in the message
 * @throws NbaVpmsException is thrown if some exception occurs while executing the vpms model.
 */
public void parseResults(String aDelimiter) throws com.csc.fsg.nba.exception.NbaVpmsException {
	if (result.getResult().trim().equals("0") || result.getResult().trim().equals(""))
		return;
	//SPR1780 code deleted
	NbaStringTokenizer tokens = new NbaStringTokenizer(result.getMessage().trim(), aDelimiter);
	String aToken = tokens.nextToken(); // First token is empty - don't need it.
	while (tokens.hasMoreTokens()) {
		aToken = tokens.nextToken();
		StringTokenizer bToken = new StringTokenizer(aToken, "#");
		String workItem = bToken.nextToken(); //NBA077
		String suspendDays = bToken.nextToken();
		String maxSuspendDays = bToken.nextToken(); //NBA119
		//begin NBA077
		String source = null;
		if (bToken.hasMoreTokens()) {
			source = bToken.nextToken();
		}
		NbaVpmsWorkItem wi = new NbaVpmsWorkItem(workItem, suspendDays, source,maxSuspendDays);//NBA119
		//end NBA077

		workItems.add(wi);
	}
}
/**
 * Initializes the workItem data member.
 * @param newWorkItems java.util.ArrayList
 */
public void setWorkItems(java.util.ArrayList newWorkItems) {
	workItems = newWorkItems;
}
}
