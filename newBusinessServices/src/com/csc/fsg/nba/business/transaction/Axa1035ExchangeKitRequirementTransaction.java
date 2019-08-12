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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

package com.csc.fsg.nba.business.transaction;

import java.util.List;
import java.util.ListIterator;

import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;


/**
 * 
 * This class encapsulates checks whenever Payment Amount is changed, Order message for PDC requirement is updated.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>APSL4280</td><td>NY Replacement Reg 60 nbA changes</td><td>NY Replacement Reg 60 nbA changes</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class Axa1035ExchangeKitRequirementTransaction extends AxaDataChangeTransaction implements NbaOliConstants {
	protected NbaLogger logger = null;

	protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";
	protected static long[] changeTypes = { 
	    DC_1035EX_KIT_RECEIVED};

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded()){
			createMiscWork(user, nbaDst);
		}
		return nbaDst;
	}

	
	protected void createMiscWork(NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		NbaDst parentWork = null;
		if (nbaDst.isCase()) {
			parentWork = retrieveParentCaseOnly(user, nbaDst); // QC16636/APSL4609
		} else {
			parentWork = retrieveParentWork(user, nbaDst.getNbaLob().getPolicyNumber());
		}
		if (parentWork != null) {
			NbaUserVO tempUserVO = new NbaUserVO("NBDATACNG", "");
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(tempUserVO, nbaDst);
			NbaTransaction nbaTransaction = parentWork.addTransaction(provider.getWorkType(), provider.getInitialStatus());
			nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
			copyLOBs(parentWork, nbaTransaction);
			parentWork.setUpdate();
			WorkflowServiceHelper.updateWork(user, parentWork);
			// Begin QC16636/APSL4609 - Unlock MISCWORK generated above
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(nbaTransaction.getID(), false);
			NbaDst aWorkItem = WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
			WorkflowServiceHelper.unlockWork(user, aWorkItem);
			// End QC16636/APSL4609
		}	
	}
	
	protected void copyLOBs(NbaDst parentWork, NbaTransaction work) {
		try {
			NbaLob parentWokLob = parentWork.getNbaLob();
			NbaLob wrkLob = work.getNbaLob();
			wrkLob.setCompany(parentWokLob.getCompany());
			wrkLob.setPolicyNumber(parentWokLob.getPolicyNumber());
			wrkLob.setBackendSystem(parentWokLob.getBackendSystem());
			wrkLob.setPlan(parentWokLob.getPlan());
			wrkLob.setOperatingMode(parentWokLob.getOperatingMode());
			wrkLob.setProductTypSubtyp(parentWokLob.getProductTypSubtyp());
			wrkLob.setContractChgType(parentWokLob.getContractChgType());
			wrkLob.setDistChannel(String.valueOf(parentWokLob.getDistChannel()));
			wrkLob.setLastName(parentWokLob.getLastName());
			wrkLob.setFirstName(parentWokLob.getFirstName());
			wrkLob.setMiddleInitial(parentWokLob.getMiddleInitial());
			wrkLob.setDOB(parentWokLob.getDOB());
			wrkLob.setGender(parentWokLob.getGender());
			wrkLob.setSsnTin(parentWokLob.getSsnTin());

		} catch (NbaBaseException nbe) {
			getLogger().logException(nbe);
		}

	}
	
	/**
	 * Retrieve the parent case WARNING: Call this method for transaction work only
	 * 
	 * @return the parent case
	 */
	protected NbaDst retrieveParentWork(NbaUserVO user, String polNumber) throws NbaBaseException {
		NbaDst parent = lookup(user, polNumber, NbaConstants.A_WT_APPLICATION); // SR787006-APSL3702
		return parent;
	}

	protected NbaDst lookup(NbaUserVO user, String polNumber, String workType) throws NbaBaseException {
		NbaSearchVO searchVO = performSearch(user, polNumber, workType); //Perform searches
		//Retrieve the work items referenced in the NbaSearchResultVO
		NbaDst parentWork = retrieveMatchingCases(searchVO.getSearchResults(), user);
		return parentWork;
	}
	
	protected NbaSearchVO performSearch(NbaUserVO user, String polNumber, String workType) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName(CASE_SEARCH_VO);
		searchVO.setWorkType(workType); 
		searchVO.setContractNumber(polNumber);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO); 
		return searchVO;
	}
	
	/**
	 * This method retrieves the matching cases found in the NbaSearchResultVO object. Each case is retrieved and locked. If unable to retrieve with
	 * lock, this work item will be suspended for a brief period of time due to the auto suspend flag being set.
	 * 
	 * @param searchResults
	 *            the results of the previous AWD lookup
	 * @throws NbaBaseException
	 *             NbaLockException
	 */
	public NbaDst retrieveMatchingCases(List searchResults, NbaUserVO user) throws NbaBaseException {
		NbaDst aWorkItem = null;
		ListIterator results = searchResults.listIterator();
		while (results.hasNext()) {
			NbaSearchResultVO resultVO = (NbaSearchResultVO) results.next();
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(resultVO.getWorkItemID(), true);
			retOpt.requestSources();
			retOpt.setLockWorkItem();
			aWorkItem = WorkflowServiceHelper.retrieveWork(user, retOpt);
		}
		return aWorkItem;
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
	
	/**
	 * Retrieve the Case without transactions
	 * 
	 * @throws NbaBaseException
	 */
	// QC16636/APSL4609
	protected NbaDst retrieveParentCaseOnly(NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(nbaDst.getID(), false);
		retOpt.requestCaseAsParent();
		retOpt.setLockParentCase();
		return WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
	}
	
}
