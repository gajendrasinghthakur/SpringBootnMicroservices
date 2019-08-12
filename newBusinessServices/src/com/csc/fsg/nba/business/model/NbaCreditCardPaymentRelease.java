package com.csc.fsg.nba.business.model;

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
 * 
 * *******************************************************************************<BR>
 */
import java.util.List;

import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * NbaCreditCardPaymentRelease determines for a case whether credit card payments have
 * been suspended, awaiting approval.  If so, the payment work items will be unsuspended
 * and routed to the next step in the workflow.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit card payment and authorization</td></tr>
 * <tr><td>SPR2977</td><td>Version 6</td><td>APAPPRVL autoprocess is error stopping with message "NetServer error: NBSuspend SYS0003 - FILE = SQLCODE = MISC = (SYS0003)"</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaCreditCardPaymentRelease {
	protected static final String CC_USER = "NBCCPAY";

	/**
	 * Constructor for NbaCreditCardPaymentRelease
	 */
	public NbaCreditCardPaymentRelease() {	
	}
	/**
	 * Determines if there are credit card payments that have been held for approval.
	 * If so, those work items will be unsuspended and routed to the credit card payment
	 * process queue. (NBCCARD)
	 * @param NbaDst - a Case
	 * @param NbaUserVO - the Nba User
	 * @throws NbaBaseException
	 */
	public static void releasePayments(NbaDst nbaDst, NbaUserVO userVO) throws NbaBaseException{
		
		try {
			//SPR2977 code deleted
			if (NbaOliConstants.NBA_FINALDISPOSITION_DECLINED == nbaDst.getNbaLob().getCaseFinalDispstn()) {
				return; //if the policy is declined, leave it in the hold queue
			}
			List wiList = nbaDst.getNbaTransactions();
			NbaLob wiLob;
			for (int i = 0; i < wiList.size(); i++) {
				NbaTransaction wi = (NbaTransaction) wiList.get(i);
				wiLob = wi.getNbaLob();
				if (NbaConstants.A_WT_CWA.equals(wiLob.getWorkType()) && NbaConstants.A_QUEUE_CC_HOLD.equals(wiLob.getQueue())) {
					NbaUserVO ccUser = new NbaUserVO(CC_USER, CC_USER);
					NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(ccUser, wiLob);
					wi.setStatus(statusProvider.getOtherStatus());
					setRouteReason(wi);
					wi.setUpdate();
					//SPR2977 code deleted
				}
			}
		} catch (NbaBaseException e) {
			throw e;
		} catch (Throwable t) {
			NbaBaseException e = new NbaBaseException(t);
			throw e;
		}
	}
	/**
	 * Sets the route reason for the NbaTransaction based on the new status.
	 * @param trans the NbaTransaction
	 */
	public static void setRouteReason(NbaTransaction trans) {
		String workType = null;
		String routeReason = null;
		try{
			workType = trans.getTransaction().getWorkType();
			NbaTableAccessor tableAccessor = new NbaTableAccessor();
			routeReason = tableAccessor.getStatusTranslationString(workType, trans.getStatus());
		} catch (Exception e) {
			// just use the default route reason...
		}
		if (routeReason == null) {
			routeReason = trans.getStatus();	// default to the AWD status
		}
		trans.getNbaLob().setRouteReason(routeReason);
	}

}
