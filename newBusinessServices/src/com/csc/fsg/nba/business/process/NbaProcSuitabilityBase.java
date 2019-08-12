package com.csc.fsg.nba.business.process;

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
 *     Copyright (c) 2002-2012 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import java.util.Date;

import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;

/**
 * NbaProcSuitability 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA297</td><td>Version ?</td><td>Suitability</td></tr>
 *  <tr><td>CR1345559</td><td>AXA Life Phase2</td><td>Suitability</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 1201
 * @see NbaAutomatedProcess
 */

public abstract class NbaProcSuitabilityBase extends NbaAutomatedProcess {

	public NbaProcSuitabilityBase() {
		super();
	}

	protected void updateApplicationInfoExtension(long suitabilityDecision, Date date, Date time) throws NbaBaseException {
		ApplicationInfoExtension extension = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
    	extension.setActionUpdate();
    	extension.setLastSuitabilityRunDate(date); 
    	extension.setLastSuitabilityRunTime(new NbaTime(time));
    	extension.setSuitabilityDecisionStatus(suitabilityDecision);
    	//CR1345559 refactored
	}

	protected NbaDst retrieveCaseWorkItem(NbaUserVO user, String contractNumber, String companyCode) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchVO.setContractNumber(contractNumber);
		searchVO.setCompanyCode(companyCode);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = lookupWork(getUser(), searchVO); 
		if (!searchVO.getSearchResults().isEmpty()) {
			NbaSearchResultVO nbaSearchResultVO = (NbaSearchResultVO) searchVO.getSearchResults().get(0);
			NbaAwdRetrieveOptionsVO retrieveOptions = new NbaAwdRetrieveOptionsVO();
			retrieveOptions.setWorkItem(nbaSearchResultVO.getWorkItemID(), true);
			retrieveOptions.setLockWorkItem();
			//retrieveOptions.setLockTransaction();  //CR1345559
			retrieveOptions.requestTransactionAsChild(); 
			return retrieveWorkItem(user, retrieveOptions);
		}
		String msg = "Workflow lookup failed.  ContractNumber:" + contractNumber;
       	getLogger().logError(msg);
		throw new NbaBaseException(msg); //throws Base and let inherited classes handle failure
	}
	
	protected void unlockCase() throws NbaBaseException {
		unlockWork(getWork());
		NbaContractLock.removeLock(getWork(), getUser());
	}
}
