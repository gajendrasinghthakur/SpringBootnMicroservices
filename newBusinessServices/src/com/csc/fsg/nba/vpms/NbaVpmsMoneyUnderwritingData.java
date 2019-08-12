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

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;

/**
 * This class represents the results of an automated money underwriting process by 
 * extending the NbaVpmsData class.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA106</td><td>Version 4</td><td>Clone of original NbaVpmsAutoUnderwritingData class</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProcAutoMoneyUnderwriting
 * @since New Business Accelerator - Version 1
 */

public class NbaVpmsMoneyUnderwritingData extends NbaVpmsData {
	public java.util.ArrayList auErrors = null;
	private static NbaLogger logger = null; //NBA044
	public boolean isNameMismatched = false;//AXAL3.7.43
	public boolean paperCheckOnCIPE = false;//NBLXA-1250
/**
 * Default constructor.
 */
public NbaVpmsMoneyUnderwritingData() {
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
			logger = NbaLogFactory.getLogger(NbaVpmsMoneyUnderwritingData.class.getName());
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
 */
// NBA021 Changed signature to remove String parameter
public NbaVpmsMoneyUnderwritingData(VpmsComputeResult aResult) {
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
			getLogger().logError((String) auErrors.get(x));
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
 */
public void parseResults() {
    if (result.getReturnCode() == 0) { // NBA021
        // NBA021 code deleted
        auErrors = new ArrayList();
        // NBA021 code deleted
        NbaStringTokenizer tokens = new NbaStringTokenizer(result.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
        // for each token in the list, we create a requirement
        while (tokens.hasMoreTokens()) {
			String aString = tokens.nextToken();
			//AXAL3.7.43 Begin
				if ("PAPERCHECKONCIPE".equalsIgnoreCase(aString)) { //NBLXA-1250
					paperCheckOnCIPE = true;
				} else if ("FLAGNAMEMSMTCH".equalsIgnoreCase(aString)) {
					isNameMismatched = true;
				} else {// AXAL3.7.43 End
					if (aString.length() != 0)
						auErrors.add(aString);
				}// AXAL3.7.43

				// NBLXA-5300

		}
        // NBA021 Begin
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
            auErrors.add("Automated Underwriting failed due to " + error);
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

