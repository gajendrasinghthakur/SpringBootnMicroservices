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

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.business.process.NbaProcAutoUnderwriting;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;

/**
 * This class represents the results of an automated underwriting process by 
 * extending the NbaVpmsData class.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA058</td><td>Version 3</td><td>Upgrade to J-VPMS version 1.5.0</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA106</td><td>Version 4</td><td>Changes for storing sensitive informational messages</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProcAutoUnderwriting
 * @since New Business Accelerator - Version 1
 */

public class NbaVpmsAutoUnderwritingData extends NbaVpmsData {
	public java.util.ArrayList auErrors = null;
	private static NbaLogger logger = null; //NBA044 
/**
 * Default constructor.
 */
public NbaVpmsAutoUnderwritingData() {
	super();
}
/**
 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
// NBA044 New Method
protected static NbaLogger getLogger() {
	if (logger == null) {
		try {
			logger = NbaLogFactory.getLogger(NbaVpmsAutoUnderwritingData.class.getName());
		} catch (Exception e) {
			NbaBootLogger.log("NbaVpmsAutoUnderwritingData could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}
	}
	return logger;
}
/**
 * This constructor invokes the base class constructor to parse the 
 * VpmsComputeResult object and populates its data members with the 
 * result data by invoking the parseResults() method.
 * @param aResult the result of the call to the Automated Underwriting 
 *                VPMS model
 * @throws com.csc.fsg.nba.exception.NbaVpmsException
 */
// NBA021 Changed signature to remove String parameter
public NbaVpmsAutoUnderwritingData(VpmsComputeResult aResult) throws com.csc.fsg.nba.exception.NbaVpmsException {
    super(aResult);
    parseResults(); // NBA021
}
/**
 * Writes the errors from the AutomatedUndewriting process to the
 * console.
 */
 
public void displayErrors() {
	if (auErrors == null || auErrors.isEmpty())
		return;
	getLogger().logError("***** AU ERRORS *****"); //NBA044
	for (int x = 0; x < auErrors.size(); x++) {
		//NBA044 start
		if(getLogger().isErrorEnabled()){
			//NBA106
			NbaVpmsAuResultBean auResult = (NbaVpmsAuResultBean) auErrors.get(x);
			getLogger().logError((String) auResult.getErrText());
		}
		//NBA044 end
	}
	getLogger().logError("*********************"); //NBA044
}
/**
 * Answers the automated underwriting errors
 * @return <code>java.util.ArrayList</code> a list of errors returned by the
 *         automated underwriting VPM model.
 */
public java.util.ArrayList getAuErrors() {
	return auErrors;
}
/**
 * When a case fails automated underwriting, the errors returned by the
 * VPMS model are parsed from the result message and stored in the
 * auErrors data member.
 * @throws com.csc.fsg.nba.exception.NbaVpmsException
 */
// NBA021 Changed signature to remove delimiter
public void parseResults() throws com.csc.fsg.nba.exception.NbaVpmsException {
	//NBA106
	String aString1;
    if (result.getReturnCode() == 0) { // NBA021
        // NBA021 code deleted
        auErrors = new ArrayList();
        // NBA021 code deleted
		//NBA106 Begin
		NbaStringTokenizer tokens = new NbaStringTokenizer(result.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[1]); // NBA021
		String aToken; // NBA021  = tokens.nextToken(); // First token is empty - don't need it.
		while (tokens.hasMoreTokens()) {
			aToken = tokens.nextToken();
			StringTokenizer bToken = new StringTokenizer(aToken, NbaVpmsAdaptor.VPMS_DELIMITER[0]);
            String aString = bToken.nextToken();
            if (bToken.hasMoreTokens())
				aString1 = bToken.nextToken();
			else
				aString1 = "false";
				
			if (aString.length() != 0) {
				NbaVpmsAuResultBean wi = new NbaVpmsAuResultBean(aString, aString1);
				auErrors.add(wi);
			}
        }
        //NBA106 End
        //NBA021 Begin
    } else {
        String error = null;
        if (result.getRefField() != null && result.getRefField().length() > 0) {
            error = "missing field: " + result.getRefField();
        } else {
            if (result.getMessage() != null && result.getMessage().length() > 0) {
                error = result.getMessage();
            }
        }
        if (error != null) {
            auErrors = new ArrayList();
            // NBA106 added
			NbaVpmsAuResultBean wi = new NbaVpmsAuResultBean("Automated Underwriting failed due to " + error, "false");
			auErrors.add(wi);
			//NBA106 removed
            //auErrors.add("Automated Underwriting failed due to " + error);
        }
    }
    if (auErrors == null || auErrors.isEmpty()) {
        result.setResult("0");
    } else {
        result.setResult("1");
    }
    // NBA021 END
}
/**
 * Sets the auErrors with the new errors.
 * @param newAuErrors an array of automated underwriting errors
 */
public void setAuErrors(java.util.ArrayList newAuErrors) {
	auErrors = newAuErrors;
}
}
