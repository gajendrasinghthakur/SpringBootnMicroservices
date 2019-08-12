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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * 
 * This class encapsulates checks whenever following changes are made to the Insured or Owner roles on the policy. - Name. - Address. - Tax
 * Identification. - Tax Identification Type. - Gender/Sex. - Date of Birth and following changes are made on a contract - Policy Status - Plan Change -
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
 * <td>AXAL3.7.21</td><td>AXA Life Phase 2</td><td>Prior Insurance</td>
 * <td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
 * <td>AXAL3.7.07</td><td>ALS4589</td><td>Data Change Architecture</td>
* <tr><td>P2AXAL053</td><td>AXA Life Phase 2</td><td>R2 Auto Underwriting</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaUpdateReinsuranceTransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;
		
	protected static	long[] changeTypes = { 	
			DC_FACE_AMT,
			DC_APP_STATE,
			DC_SIGNEDDATE,
			DC_PRODUCTCODE,
			DC_INSURED_DOB, 
			DC_INSURED_GENDER, 
			DC_JNT_INSURED_DOB,
			DC_JNT_INSURED_GENDER,  
			DC_RIDER_AMT,	
			DC_RIDER_ADDED,
			DC_RIDER_DELETED,
			DC_PERMANENT_FLAT_EXTRA_RATING_ADDED,
			DC_PERMANENT_FLAT_EXTRA_RATING_DELETED,
			DC_TEMP_FLAT_EXTRA_RATING_ADDED,
			DC_TEMP_FLAT_EXTRA_RATING_DELETED,
			DC_PERMANENT_RATING_ADDED,
			DC_PERMANENT_RATING_DELETED
		};//P2AXAL053

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
		if (isCallNeeded()) {
			if (getUnderwritingApproval(nbaTxLife) == NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER) {//ALS4589
				AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_TAI_SERVICE_TRANSMIT, user,
						nbaTxLife, null, new Long(NbaOliConstants.CHANGE_TC_UPDATE));
				webServiceInvoker.execute();
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
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ENABLE_REINSURANCE_INTERFACE_CALL);
	}
	/**
	 * Extract the Underwriting Approval code from the Application Info Extension.
	 * 
	 * @param NbaTXLife
	 *            the Acord model for a holding object
	 * @return long
	 */
	//New Method
	protected long getUnderwritingApproval(NbaTXLife aHolding) {
		ApplicationInfo appInfo = aHolding.getPrimaryHolding().getPolicy().getApplicationInfo();
		//begin NBA208-15
		ApplicationInfoExtension appInfoExtension = null;
		long approvalCode = -1L;
		appInfoExtension = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (appInfoExtension != null) {
			approvalCode = appInfoExtension.getUnderwritingApproval();
		}
		return (approvalCode);
	}

}
