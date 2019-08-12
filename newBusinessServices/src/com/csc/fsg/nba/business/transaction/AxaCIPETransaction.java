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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

package com.csc.fsg.nba.business.transaction;

import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * 
 * This class encapsulates checks whenever following changes are made on the policy. - Severe CV msg's are resolved
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>NBLXA-2108</td><td>AXA Life Phase 2</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaCIPETransaction extends AxaDataChangeTransaction implements NbaConstants {

	protected static	long[] changeTypes = {
		DC_INITIAL_PREMIUM_METHOD
	};
	
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if(hasChangeSubType(DC_INITIAL_PREMIUM_METHOD)){
		    routeCIPECWAToend(nbaTxLife, user);
			}
		return nbaDst;
	}
	
	protected void routeCIPECWAToend(NbaTXLife nbaTxLife, NbaUserVO user) throws NbaBaseException {
		String contractKey = null;
		if (nbaTxLife != null && nbaTxLife.getPolicy() != null && nbaTxLife.getPolicy().getPolNumber() != null) {
			contractKey = nbaTxLife.getPolicy().getPolNumber();
		}
		if (!NbaUtils.isBlankOrNull(contractKey)) {
			NbaSearchVO searchVOResult = searchCIPECWA(user, contractKey);
			if (!NbaUtils.isBlankOrNull(searchVOResult)) {
				List cwaWIs = searchVOResult.getSearchResults();
				for (int i = 0; i < cwaWIs.size(); i++) {
					NbaSearchResultVO cwaVO = (NbaSearchResultVO) cwaWIs.get(i);

					NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
					retOpt.setWorkItem(cwaVO.getWorkItemID(), false);
					retOpt.setLockWorkItem();
					NbaDst aWorkItem = WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
					aWorkItem.setStatus("MONYRETRND");
					aWorkItem.getNbaLob().setRouteReason("CWA routed to end due to case conversion to NON-CIPE");
					NbaUtils.addAutomatedComment(aWorkItem, user, "Case converted to Non-CIPE");
					aWorkItem.setUpdate();
					WorkflowServiceHelper.updateWork(user, aWorkItem);
					WorkflowServiceHelper.unlockWork(user, aWorkItem);
				}
			}
		}
	}
	public NbaSearchVO searchCIPECWA(NbaUserVO user, String contractKey) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_CWA);
		searchVO.setContractNumber(contractKey);
		searchVO.getNbaLob().setPaymentMoneySource(String.valueOf(PAYMENT_TYPE_ACH));
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO.setOperand(NOTEQUALOPERATOR);
		searchVO.setQueue(END);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		return searchVO;
	}
	
	protected long[] getDataChangeTypes() {
		return changeTypes; 
	}

	protected boolean isTransactionAlive() {
		return true;
	}
	
}
