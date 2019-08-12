package com.csc.fsg.nba.business.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;

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


/**
 * 
 * This class encapsulates checks to determine when the Replacement Notification (NBRPLNOTIF) work item is generated based on
 * the Replameent Indicator or 1035 Exchange Indicator (when approved).
 * Agent information
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <tr><td>CR61627</td><td>AXA Life Phase 2</td><td>Assign Replacement Case Manager</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaUpdateReplNotificationTransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;
	
	
	protected static	long[] changeTypes = { 	DC_REPLACEMENT_IND };
	

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
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		
		if (isCallNeeded(nbaDst)) {   
			
			NbaUserVO userVO = new NbaUserVO();
			userVO.setUserID(NbaConstants.PROC_REPLACEEMENT);
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(userVO, nbaDst, nbaTxLife, new HashMap());
			String workType = provider.getWorkType();
			if (null == workType || null == provider.getInitialStatus()) {
				getLogger().logError("Error in VPMS for Replacement Notification Processing for " + nbaTxLife.getPolicy().getPolNumber() );
				return nbaDst;
			}
			
			try {
				addReplacementNotification(nbaDst,provider, user);
			} catch (NbaBaseException nbe) {
				getLogger().logError("Error creating NBRPLNOTIF work item for contract " + nbaTxLife.getPolicy().getPolNumber() );
				throw nbe;
			}
			
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
		// TODO Auto-generated method stub
		return true;
	}

	

	
	protected boolean isCallNeeded(NbaDst nbaDst) {
		if (nbaDst.isCase()) {
			return super.isCallNeeded();
		}
		return false;
	}
	
	private void addReplacementNotification(NbaDst aCase, NbaProcessWorkItemProvider provider, NbaUserVO user) throws NbaBaseException {

		NbaDst work = (NbaDst) aCase.clone();
		work.getWorkItem().setWorkItemChildren(new ArrayList());
		NbaTransaction replTransaction = work.addTransaction(provider.getWorkType(), provider.getInitialStatus());
		replTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		replTransaction.getTransaction().setWorkType(provider.getWorkType());
		replTransaction.getTransaction().setLock("Y");
		replTransaction.setStatus(provider.getInitialStatus());

		//Copy lobs from the case to the new transaction
		NbaLob caseLob = work.getNbaLob();
		NbaLob replLob = replTransaction.getNbaLob();

		replLob.setPolicyNumber(caseLob.getPolicyNumber());
		replLob.setCompany(caseLob.getCompany());
		replLob.setLastName(caseLob.getLastName());
		replLob.setFirstName(caseLob.getFirstName());
		replLob.setSsnTin(caseLob.getSsnTin());
		replLob.setTaxIdType(caseLob.getTaxIdType());
		replLob.setAppOriginType(caseLob.getAppOriginType());
		replLob.setAppState(caseLob.getAppState());
		replLob.setReplacementIndicator(caseLob.getReplacementIndicator());
		work = WorkflowServiceHelper.updateWork(user, work);
		List transList = work.getNbaTransactions();
		for (int i=0;i<transList.size();i++) {
				NbaTransaction nbaTrans = (NbaTransaction) transList.get(i);
				NbaDst nbaDst = new NbaDst();
				nbaDst.addTransaction(nbaTrans.getWorkItem());
				WorkflowServiceHelper.unlockWork(user, nbaDst);
		}
	}
}
