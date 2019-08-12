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

import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.AxaReassignDataVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;

/**
 *
 * This class encapsulates checks whenever following changes are made on the policy. - Insured's Name. - Insured's DOB. - Requested Policy Date changed
 * RequestedPolDateReason in
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>NBLXA-1539</td><td>AXA Life Phase 2</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaUpdateReassingmentTransaction extends AxaDataChangeTransaction implements NbaConstants{
	protected NbaLogger logger = null;

	protected static	long[] changeTypes = {
		DC_DIST_CHANNEL_CHANGED,DC_PRIMARY_AGENT_CHANGE,DC_GOLDEN_IND,DC_PRODUCTCODE
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
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
		boolean rerunAssingment = polExt.getRerunAssignmentInd();
		String msg = "";
		if (isCallNeeded()) {
			if (hasChangeSubType(DC_DIST_CHANNEL_CHANGED) && !nbaTxLife.isInformalApplication()) {
				AxaReassignDataVO reassignVo = new AxaReassignDataVO();
				msg = "Distribution Channel change initiated.";
				Policy policy = nbaTxLife.getPolicy();
				PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
				String crda = nbaDst.getID();
				reassignVo.setCreateDateTime(crda);
				reassignVo.setPolicynumber(policy.getPolNumber());
				reassignVo.setCompanyKey(policy.getCompanyKey());
				reassignVo.setBackendKey(policy.getBackendKey());
				reassignVo.setChangedType(NbaConstants.JOB_DISTRIBUTION_CHANNEL);
				reassignVo.setUserCode(user.getUserID());
				reassignVo.setStatus("Active");
				reassignVo.setChangedValue(Long.toString(policyExt.getDistributionChannel()));
				NbaSystemDataDatabaseAccessor.insertReassingmentProcessing(reassignVo);
				addComments(user, nbaDst, msg);
			}

			if (hasChangeSubType(DC_PRIMARY_AGENT_CHANGE) && rerunAssingment) {
				AxaReassignDataVO reassignVo = new AxaReassignDataVO();
				msg = "Automatic reassingment initiated.";
				String csmq = nbaDst.getNbaLob().getCaseManagerQueue();
				Policy policy = nbaTxLife.getPolicy();
				String crda = nbaDst.getID();
				reassignVo.setCreateDateTime(crda);
				reassignVo.setPolicynumber(policy.getPolNumber());
				reassignVo.setCompanyKey(policy.getCompanyKey());
				reassignVo.setBackendKey(policy.getBackendKey());
				reassignVo.setChangedType(NbaConstants.JOB_AGENT_UPDATE);
				reassignVo.setUserCode(user.getUserID());
				reassignVo.setStatus("Active");
				reassignVo.setChangedValue(csmq);
				NbaSystemDataDatabaseAccessor.insertReassingmentProcessing(reassignVo);
				addComments(user, nbaDst, msg);

			}
			// Begin NBLXA-1831
			if (hasChangeSubType(DC_GOLDEN_IND)) {
				AxaReassignDataVO reassignVo = new AxaReassignDataVO();
				Policy policy = nbaTxLife.getPolicy();
				String crda = nbaDst.getID();
				reassignVo.setCreateDateTime(crda);
				reassignVo.setPolicynumber(policy.getPolNumber());
				reassignVo.setCompanyKey(policy.getCompanyKey());
				reassignVo.setBackendKey(policy.getBackendKey());
				reassignVo.setChangedType(NbaConstants.JOB_GOLDEN_TICKET);
				reassignVo.setUserCode(user.getUserID());
				reassignVo.setStatus("Active");
				reassignVo.setChangedValue(Boolean.toString(polExt.getGoldenTicketInd()));
				NbaSystemDataDatabaseAccessor.insertReassingmentProcessing(reassignVo);
			}
			// End NBLXA-1831
			// Begin NBLXA-2089
			if (hasChangeSubType(DC_PRODUCTCODE)) {
				String plan = nbaDst.getNbaLob().getPlan();
				String crda = nbaDst.getID();
				AxaReassignDataVO reassignVo = new AxaReassignDataVO();
				Policy policy = nbaTxLife.getPolicy();
				reassignVo.setCreateDateTime(crda);
				reassignVo.setPolicynumber(policy.getPolNumber());
				reassignVo.setCompanyKey(policy.getCompanyKey());
				reassignVo.setBackendKey(policy.getBackendKey());
				reassignVo.setChangedType(NbaConstants.JOB_PLAN_CHANGE);
				reassignVo.setUserCode(user.getUserID());
				reassignVo.setStatus("Active");
				reassignVo.setChangedValue(plan);
				NbaSystemDataDatabaseAccessor.insertReassingmentProcessing(reassignVo);
			}
			// End NBLXA-2089
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

	protected void addComments(NbaUserVO user, NbaDst nbaDst,String msg) {
		NbaProcessingErrorComment comment = new NbaProcessingErrorComment();
		comment.setText(msg);
		comment.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
		comment.setOriginator(user.getUserID());
		comment.setUserNameEntered(user.getUserID());
		comment.setActionAdd();
		nbaDst.addManualComment(comment.convertToManualComment());
	}

}