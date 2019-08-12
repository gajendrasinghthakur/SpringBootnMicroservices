package com.csc.fsg.nba.process.domaindata;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTable;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaConfiguration;

/**
 * Interface for retrieving domain data from NBADATA.  It provides the ability to retrieve
 * all data from a table or a specific table row.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA136</td><td>Version 6</td><td>In Tray and Search Rewrite</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class RetrieveDomainDataBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			if (input instanceof NbaPlansData) {
				result = getPlanData((NbaPlansData) input, result);
			}
		} catch (Exception e) {
			addExceptionMessage(result, e);
			return result;			
		}
		return result;
	}

	/**
	 * Retrieves plan information from NBA_PLANS.
	 * @param planData
	 * @return <code>NbaPlansData</code>
	 * @throws NbaBaseException
	 */
	protected AccelResult getPlanData(NbaPlansData planData, AccelResult result) throws NbaBaseException {
		NbaTable nbaTable = new NbaTable();
		Map tblKeys = createDefaultHashMap(NbaTableAccessConstants.WILDCARD);
		tblKeys.put(NbaTableAccessConstants.C_TABLE_NAME, NbaTableConstants.NBA_PLANS);
		if (planData.getCompanyCode() != null) {
			tblKeys.put(NbaTableAccessConstants.C_COMPANY_CODE, planData.getCompanyCode());
		}
		if (planData.getCoverageKey() != null) {
			tblKeys.put(NbaTableAccessConstants.C_COVERAGE_KEY, planData.getCoverageKey());
			result.addResult(nbaTable.translateOlifeData(tblKeys, planData.getCoverageKey()));
		} else {
			result = loadResults(result, nbaTable.getDisplayData(tblKeys));
		}
		return result;
	}

	/**
	 * Loads the <code>AccelResult</code> with a collection of <code>NbaTableData</code>.
	 * @param result AccelResult
	 * @param tableData collection of table rows
	 * @return
	 */
	protected AccelResult loadResults(AccelResult result, NbaTableData[] tableData) {
		int count = tableData.length;
		for (int i = 0; i < count; i++) {
			result.addResult(tableData[i]);
		}
		return result;
	}

	/**
	 * Provides a default HashMap that may be passed to other methods to retrieve
	 * data from the database.  Required keys are populated with wildcard value 
	 * with the exception of the back end system value, which can never be awildcard.
	 * @param aBackendSystem identifies the back end system to be used to retrieve data
	 *                       from the database.
	 * @return a HashMap populated with default values
	 */
	public Map createDefaultHashMap(String aBackendSystem) throws NbaBaseException {
		HashMap aDefault = new HashMap();
		HashMap parms = NbaConfiguration.getInstance().getDatabaseSearchKeys();
		Iterator it = parms.values().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (key.equalsIgnoreCase(NbaTableAccessConstants.C_SYSTEM_ID))
				aDefault.put(key, aBackendSystem); // SPR3290
			else
				aDefault.put(key, NbaTableAccessConstants.WILDCARD); // SPR3290
		}
		return aDefault;
	}
}
