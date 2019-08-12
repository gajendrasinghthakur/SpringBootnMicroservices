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

import com.csc.fsg.nba.database.AxaGIAppOnboardingDataAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.AxaGIAppOnboardingDataVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;

/**
 * 
 * This class encapsulates checks whenever following changes are made to the Insured or Owner roles on the policy. - Name. - Address. - Tax
 * Identification. - Tax Identification Type. - Gender/Sex. - Date of Birth and following changes are made on a contract - Policy Status - Plan Change
 * - Agent information
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr> * 
 * <td>GI CDD</td>
 * <td>NBLXA-2299</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaUpdateOnboardingTransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;	
	public static final int [] ONBOARDING_BRIDGER_ALERT_CV ={6767,6768,6772};
	public static final int [] ONBOARDING_BAE_ALERT_CV = {6770,6773};
	protected static long[] changeTypes = { DC_ONBOARDING_BRIDGER_ALERT_CV_RESOLVED,
			DC_ONBOARDING_BAE_ALERT_CV_RESOLVED
	};
	private AxaGIAppOnboardingDataAccessor onboardingDataAccessor = null;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded()) {
			Holding holding = nbaTxLife.getPrimaryHolding();
			int count = holding.getSystemMessageCount();
			SystemMessage systemMessage;
			SystemMessageExtension msgExt;
			onboardingDataAccessor = new AxaGIAppOnboardingDataAccessor();
			onboardingDataAccessor.startTransaction();
			for (int i = 0; i < count; i++) {
				systemMessage = holding.getSystemMessageAt(i);
				msgExt = NbaUtils.getFirstSystemMessageExtension(systemMessage);
				boolean isOnboardingBridgerAlertCV = isOnboardingBridgerAlertCV(systemMessage.getMessageCode());
				boolean isOnboardingBAEAlertCV = isOnboardingBAEAlertCV(systemMessage.getMessageCode());
				if (!systemMessage.isActionDelete() && msgExt != null && (isOnboardingBridgerAlertCV || isOnboardingBAEAlertCV)) {
					Party party = NbaTXLife.getPartyFromId(systemMessage.getRelatedObjectID(), nbaTxLife.getOLifE().getParty());
					PolicyExtension polExtn = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
					if (party != null && polExtn != null) {
						AxaGIAppOnboardingDataVO onboardingDataVO = onboardingDataAccessor
								.selectPartyForOnboardingProcessing(polExtn.getGuarIssOfferNumber(), party.getFullName(), party.getGovtID());
						if (onboardingDataVO != null) {
							if (isOnboardingBridgerAlertCV) {
								if (msgExt.getMsgOverrideInd()) {
									onboardingDataVO.setHoldForBridgerAlert(false);
								} else {
									onboardingDataVO.setHoldForBridgerAlert(true);
								}
								onboardingDataAccessor.updateHoldForBridgerAlertDetails(onboardingDataVO);
							} else if (isOnboardingBAEAlertCV) {
								if (msgExt.getMsgOverrideInd()) {
								onboardingDataVO.setHoldForBAEAlert(false);
								}
								else{
									onboardingDataVO.setHoldForBAEAlert(true);
									}
								onboardingDataAccessor.updateHoldForBAEAlertDetails(onboardingDataVO);
							}
						}
					}
				}
			}
			onboardingDataAccessor.commitTransaction();
		}
		return nbaDst;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#getDataChangeTypes()
	 */
	protected long[] getDataChangeTypes() {
		return changeTypes;
	}
	
	@Override
	protected boolean isTransactionAlive() {
		return true;
	}

	private boolean isOnboardingBridgerAlertCV(int msgCode) {
		boolean flag = false;
		for (int i = 0; i < ONBOARDING_BRIDGER_ALERT_CV.length; i++) {
			if (ONBOARDING_BRIDGER_ALERT_CV[i] == msgCode) {
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	private boolean isOnboardingBAEAlertCV(int msgCode) {
		boolean flag = false;
		for (int i = 0; i < ONBOARDING_BAE_ALERT_CV.length; i++) {
			if (ONBOARDING_BAE_ALERT_CV[i] == msgCode) {
				flag = true;
				break;
			}
		}
		return flag;
	}
}
