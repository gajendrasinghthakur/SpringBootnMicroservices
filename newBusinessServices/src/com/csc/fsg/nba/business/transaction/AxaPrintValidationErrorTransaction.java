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
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * 
 * This class encapsulates checks whenever following changes are made on the policy. - Print CV msg's are resolved
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>NBLXA-2620</td><td>AXA Life Phase 2</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaPrintValidationErrorTransaction extends AxaDataChangeTransaction implements NbaConstants{
	protected NbaLogger logger = null;
//	protected NbaDst parentCase;
	
	protected static	long[] changeTypes = {
			DC_PRINT_CV_RESOLVED
	};

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaSignificantValidationErrorTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife, NbaUserVO user, NbaDst nbaDst)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded()) {
			suspendPrintWI(user, nbaDst);
		}
		return nbaDst;
	}
	
	
	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#getDataChangeTypes()
	 */
	protected long[] getDataChangeTypes() {
		return changeTypes; 
	}

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaDataChangeTransaction#isTransactionAlive()
	 */
	protected boolean isTransactionAlive() {
		return true;
	}
	
	public NbaDst unSuspendWorkItem(NbaSearchResultVO serachResultVO, NbaUserVO user) throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(serachResultVO.getWorkItemID(), false);
		retOpt.setLockWorkItem();
		NbaDst nbaCurrentdst = WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
		/*int newPriority = 999;
		nbaCurrentdst.increasePriority("=", Integer.toString(newPriority));
		nbaCurrentdst.setUpdate();*/
		/*nbadst.getNbaLob().setUnsuspendWorkItem(nbaDst.getID());*/
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		suspendVO.setTransactionID(nbaCurrentdst.getID());
		System.out.println("Print Work Item Locked : , If Print CV Resolved == " + nbaCurrentdst.getID() );
		suspendVO.setNbaUserVO(user);
		WorkflowServiceHelper.unsuspendWork(user, suspendVO);
		WorkflowServiceHelper.unlockWork(user, nbaCurrentdst);
		return nbaCurrentdst;
	}

	public NbaSearchVO searchPrint(NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_CONT_PRINT_EXTRACT);
		searchVO.setContractNumber(nbaDst.getNbaLob().getPolicyNumber());
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		return searchVO;
	}

	public void suspendPrintWI(NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		NbaSearchVO searchPrintVO = searchPrint(user, nbaDst);
		System.out.println("searchPrintVO Print Size, If Print CV Resolved ==" + searchPrintVO.getSearchResults());
		if (!NbaUtils.isBlankOrNull(searchPrintVO) 
				&& searchPrintVO.getSearchResults() != null 
				&& !searchPrintVO.getSearchResults().isEmpty()) {
			List searchResultList = searchPrintVO.getSearchResults();
			System.out.println("searchPrintVO Print Size, If Print CV Resolved ==" + searchResultList.size());
			for (int i = 0; i < searchResultList.size(); i++) {
				NbaSearchResultVO searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
				if (searchResultVo != null && 
						!(searchResultVo.getQueue().equalsIgnoreCase(NbaConstants.END_QUEUE))) {
					System.out.println("Unsuspend Print Work Item, If Print CV Resolved");
					unSuspendWorkItem(searchResultVo, user);
				}
			}
		}
	}
	
	/*private void setParentCase(NbaDst parentWI) {
		parentCase = parentWI;

	}

	private NbaDst getParentCase() {
		return parentCase;

	}*/
	
	/*protected void unSuspendWorkItem (NbaDst nbaDst, NbaUserVO user ) throws NbaBaseException{
		NbaDst parent = getParentCase();
		int newPriority = 999;
		parent.increasePriority("=", Integer.toString(newPriority));
		update(nbaDst);
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		suspendVO.setCaseID(parent.getID());
		unsuspendWork(suspendVO);
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		suspendVO.setCaseID(nbaDst.getID());
		suspendVO.setNbaUserVO(user);
		WorkflowServiceHelper.unsuspendWork(user, suspendVO);
	}*/
	
	
		
		
		
		/*protected NbaDst retrieveParentCaseOnly(NbaDst nbaDst, NbaUserVO user) throws NbaBaseException {
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(nbaDst.getID(), false);
			retOpt.requestCaseAsParent();
			retOpt.setLockParentCase();
			return WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
		}*/
		
//		SPR3391 changed method signature /ALS5573 changed method signature
		/*protected List getAllPrint() throws NbaBaseException {
			List tempList = getLockedCase().getNbaTransactions();//ALS5573
			Iterator itr = tempList.iterator();
			while (itr.hasNext()) {
				if (!NbaConstants.A_WT_CONT_PRINT_EXTRACT.equals(((NbaTransaction) itr.next()).getTransaction().getWorkType())) {
					itr.remove();
				}
			}
			return tempList;
		}*/
		
		
}