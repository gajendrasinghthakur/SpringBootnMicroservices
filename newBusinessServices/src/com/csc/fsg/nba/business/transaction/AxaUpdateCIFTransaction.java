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
 * <td>AXAL3.7.25</td><td>AXA Life Phase 2</td><td>Client Interface</td>
 * <td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
 * <td>ALS3374</td><td>AXA Life Phase 2</td><td>QC # 2032  - Wholesale case with "BGA agent" is going to error queue after Application submit</td>
 * <tr><td>SR534655</td><td>Discretionary</td><td>nbA ReStart – Underwriter Approval</td></tr>
 * <tr><td>P2AXAL053</td><td>AXA Life Phase 2</td><td>R2 Auto Underwriting</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaUpdateCIFTransaction extends AxaDataChangeTransaction {
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
			DC_PERMANENT_RATING_DELETED,
			
			DC_OWNER_CHANGE,
			DC_OWNER_NAME,
			DC_OWNER_SSN,
			DC_OWNER_SSNTYPE,
			DC_OWNER_PARTY_TYPE,
			DC_OWNER_DELETE,
			DC_OWNER_ADDRESS,
			DC_OWNER_DOB,
			DC_OWNER_GENDER,
			DC_OWNER_ADDED, //NBLXA-2152
			
			DC_INSURED_CHANGE,
			DC_INSURED_NAME,
			DC_INSURED_SSN,
			DC_INSURED_SSNTYPE,
			DC_INSURED_PARTY_TYPE,
			DC_INSURED_DELETE,
			DC_INSURED_ADDRESS,
			
			DC_AGENT_COUNT,
			DC_PRIMARY_AGENT_DELETE,
			DC_PRIMARY_AGENT_ADD,
			DC_PRIMARY_AGENT_CHANGE,
			
			//DC_UNDERWRITING_STATUS, //COMMENTED FOR SR534655 Retrofit
			DC_NEW_CONTRACT
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

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded() && !nbaTxLife.isPaidReIssue()) { // APSL2662
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CIF_TRANSMIT, user, nbaTxLife,nbaDst,null);
			NbaTXLife nbaTXLifeResponse = (NbaTXLife) webServiceInvoker.execute();
			verifyTxLifeResponse(nbaTXLifeResponse, nbaDst, user);
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
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ENABLE_CLIENT_INTERFACE_CALL);
	}
	/**
	 * 
	 * @param resultCode
	 * @return
	 */
	//ALS3374 new method
	protected boolean errorStop(long resultCode) {
		if (NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL == resultCode || NbaOliConstants.TC_RESINFO_SECVIOLATION == resultCode) {
			return true;
		}
		return false;
	}
}
