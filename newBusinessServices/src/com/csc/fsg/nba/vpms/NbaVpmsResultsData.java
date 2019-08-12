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

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.business.process.NbaProcRequirementsDetermination;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
/**
 * This class represents the results of the requirements determination process
 * by extending the NbaVpmsData class.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>NBA058</td><td>Version 3</td><td>Upgrade to J-VPMS version 1.5.0</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProcRequirementsDetermination
 * @since New Business Accelerator - Version 1
 */
public class NbaVpmsResultsData extends NbaVpmsData {
	public java.util.ArrayList resultsData = null;
	private static NbaLogger logger = null; //NBA044 
	/**
	 * Default constructor.
	 */
	public NbaVpmsResultsData() {
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
				logger = NbaLogFactory.getLogger(NbaVpmsResultsData.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaVpmsResultsData could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * This constructor calls the super class constructor passing it aResult
	 * and then parses the data from aResult into its data member.
	 * @param aResult the result of the call to the Requirements Determination
	 *                VPMS model
	 * @throws NbaVpmsException
	 */
	// NBA021 Changed signature to remove String parameter
	public NbaVpmsResultsData(VpmsComputeResult aResult) throws NbaVpmsException {
		super(aResult);
		parseResults(); // NBA021
	}
	/**
	 * Writes the results data to the console.
	 */
	public void displayResultsData() {
		if (resultsData == null || resultsData.isEmpty())
			return;
		getLogger().logError("***** REQUIREMENTS *****");//NBA044
		for (int x = 0; x < resultsData.size(); x++) {
			//NBA044 start
			if(getLogger().isErrorEnabled()){
				getLogger().logError((String) resultsData.get(x));
			}
			//NBA044 end
		}
		getLogger().logError("*********************"); //NBA044
	}
	/**
	 * Answer the resultsData.
	 * @return java.util.ArrayList a list of results
	 */
	public java.util.ArrayList getResultsData() {
		return resultsData;
	}
	/**
	 * Parses the data from the results of the VPMS model and uses that
	 * data to create an <code>NbaVpmsRequirment</code> object that will be 
	 * added to the requirements member.  Uses aDelimiter to find the separate
	 * requirements/messages in the result's message field.
	 * @throws NbaVpmsException
	 */
	public void parseResults() throws NbaVpmsException {
		if (result.getReturnCode() == 0) {
			resultsData = new ArrayList();
			NbaStringTokenizer tokens = new NbaStringTokenizer(result.getResult().trim(),	NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			// for each token in the list, we create add a resultData
			while (tokens.hasMoreTokens()) {
				resultsData.add(tokens.nextToken());
			}
		}
		result.setResult(String.valueOf(result.getReturnCode()));
	}
	/**
	 * Sets the resultsData data member.
	 * @param newResultsData the new results
	 */
	public void setResultsData(java.util.ArrayList newResultsData) {
		resultsData = newResultsData;
	}
}
