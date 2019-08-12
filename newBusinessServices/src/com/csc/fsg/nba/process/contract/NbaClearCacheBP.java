package com.csc.fsg.nba.process.contract;

import java.util.Iterator;
import java.util.Map;

import com.axa.fs.accel.console.valueobject.ExecuteUtilityProcessVO;

/*
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.foundation.NbaCache;
import com.csc.fsg.nba.process.NewBusinessAccelBP;


/**
 * Commits a base plan change request for a contract.  The contract will be modified
 * to remove contract information that is no longer applicable due to the plan change.
 * A comment will also be added which audits this plan change.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <tr><td>NBLXA-1538</td><td>Version 7</td><td>Distribution Channel Update for TconV</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class NbaClearCacheBP extends NewBusinessAccelBP {

    /* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	@Override
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {

			ExecuteUtilityProcessVO vo=(ExecuteUtilityProcessVO)input;
			Map tableMap=vo.getValueToModify();
			String[] tableArray=(String[]) tableMap.get("ClearCache");
			Map cache=NbaCache.getInstance().getCache();
			System.out.println("Cache Size before removing..."+cache.size());
			int cacheSize=cache.size();
			for(Iterator iterator = cache.keySet().iterator(); iterator.hasNext(); ) {
				  String key = (String)iterator.next();
				  if(tableValueFound(key,tableArray)) {
					System.out.println("Removing..."+key);
				    iterator.remove();
				  }
				}

			cacheSize=cacheSize-cache.size();
			result.addResult(cacheSize);
			System.out.println("Cache Size after removing..."+cache.size());
			result.setErrors(false);
		} catch (Exception e) {
			result = new AccelResult();
			result.setErrors(true);
			addExceptionMessage(result, e);
		}
		return result;
	}

	public boolean tableValueFound(String key, String[] table) {
		for (String str : table) {
			if (key.contains(str))
				return true;
		}
		return false;
	}

}