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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;


/**
 * 
 * This class encapsulates checks whenever the Primary Writing Agent is added, deleted on the case, Financial Activity is reversed on the case, ReplacementType is changed, ModalPremAmt is changed.
 * If any of the above change occurs then the class calls ECS update web service.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>ALS4633</td><td>AXA Life Phase 1</td><td>Compensation Interface</td>
 * </tr>
 * <tr><td>P2AXAL036</td><td>AXA Life Phase 2</td><td>Temporary Express Commission</td></tr>
 * <tr><td>SR494086</td><td>Discretionary</td><td>ADC Retrofit</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaSubmitExpressCommTransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;

	protected static long[] changeTypes = { 
			DC_AGENT_INELIGIBLE_TO_ELIGIBLE, DC_NEW_CONTRACT
	};//P2AXAL036

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		//Start APSL5232
		long activityType;
		if (nbaTxLife.getPolicy().getApplicationInfo().getCWAAmt() > 0) {
			activityType = NbaOliConstants.OLI_ACTTYPE_ECS_CALL;
		} else {
			activityType = NbaOliConstants.OLI_ACTTYPE_TEMP_ECS_CALL;
		}			
		// end APSL5232
		if (isCallNeeded() && isTempExpCommAllowed(nbaTxLife, nbaDst) && !nbaTxLife.isSIApplication() && !NbaUtils.hasECSTriggered(nbaTxLife.getOLifE().getActivity(), activityType)) {//P2AXAL036, APSL2808, APSL5232
			AxaWSInvoker webServiceInvoker = null;
			if (! calledFromApplyMoney(user)) {
				webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_ECSSUBMIT, user, nbaTxLife, nbaDst, null); //SR494086.7 ADC Retrofit
				webServiceInvoker.execute();				
				NbaUtils.addECSActivity(nbaTxLife, user.getUserID(), activityType); //APSL5232
			}
		}
		return nbaDst;
	}

	/**
	 * The method checks if the change agentInEligibleToEligible is registered and also if the user process that is running is not CWA.
	 * @return 
	 */
	protected boolean calledFromApplyMoney(NbaUserVO user) {
		return  NbaConstants.PROC_APPLY_MONEY.equalsIgnoreCase(user.getUserID()); 
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
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.COMPENSATION_INTERFACE_CALL_SWITCH);
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
	 * @return true if TempExpComm is allowed
	 */
	//P2AXAL036 New Method
	private boolean isTempExpCommAllowed(NbaTXLife nbaTxLife, NbaDst nbaDst) {
		boolean TempExpCommAllowed = false;
		if (hasChangeSubType(DC_NEW_CONTRACT)) {
			Policy pol = nbaTxLife.getPolicy();
			if (pol != null) {
				ApplicationInfo appInfo = pol.getApplicationInfo();
				if (appInfo != null) {
					ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
					if (appInfoExt != null) {
						if (appInfoExt.getChkInTransitInd()) {
							TempExpCommAllowed= AxaUtils.getAgentsEligibilityForExpressCommission(nbaTxLife, nbaDst);
						}
					}
				}
			}
		}else{
			TempExpCommAllowed = true;
		}
			
		return TempExpCommAllowed;
	}
	
}
