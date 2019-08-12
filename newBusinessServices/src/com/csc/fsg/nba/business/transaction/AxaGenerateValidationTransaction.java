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

import java.util.Iterator;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeEntry;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaSessionUtils;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;

/**
 * 
 * This class encapsulates checks whenever following changes are made on the policy. - Insured's Name. - Insured's DOB. - Requested Policy Date changed
 * - DPW added - CTIR added - Face amount changed
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
 * <td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Data Change Architecture</td>
 * <td>ALS4153</td><td>AXA Life Phase 1</td><td>Transactional Message should appear "warning user that case must be UNAPPROVED"</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaGenerateValidationTransaction extends AxaDataChangeTransaction implements NbaConstants{
	protected NbaLogger logger = null;
	
	protected static	long[] changeTypes = { 	
			DC_POL_REQUESTEDDATE,
			DC_INSURED_DOB,
			DC_RIDER_DPW_ADDED,
			DC_RIDER_CTIR_ADDED,
			DC_FACE_AMT,
			DC_INSURED_NAME,
			DC_UND_APPROVAL_CHANGED
		/*// APSL5128 Begin
		DC_INSURED_FIRSTNAME,
		DC_INSURED_LASTNAME,
		DC_INSURED_DOB,
		DC_INSURED_SSN,
		DC_POL_REQUESTEDDATE,
		DC_FACE_AMT_INCREASE,
		DC_BENEFIT_ADDED,
		DC_RIDER_ADDED,
		DC_RIDER_CTIR_ADDED, 
		DC_RIDER_DPW_ADDED,
		DC_RIDER_LTC_ADDED,
		DC_INSURED_GENDER,
		DC_OWNER_CHANGE, 
		DC_JNT_INSURED_FIRSTNAME,
		DC_JNT_INSURED_LASTNAME, 
		DC_JNT_INSURED_MIDDLENAME, 
		DC_JNT_INSURED_DOB,
		DC_JNT_INSURED_SSN,
		DC_JNT_INSURED_GENDER, 
		DC_CONTBENEFICIARY_CHANGE,
		DC_UND_APPROVAL_CHANGED,
		DC_RIDER_ROPR_ADDED  // APSL5128 End
*/		};

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
		if (PROC_VIEW_APPLICATION_ENTRY.equalsIgnoreCase(nbaTxLife.getBusinessProcess()) && nbaTxLife.isUnderwriterApproved()
				&& verifyRestrictedChanges(nbaTxLife) && !NbaUtils.isAdcApplication(nbaDst)
				&& !(!NbaUtils.isBlankOrNull(NbaUtils.getActiveContractChangeInfo(nbaTxLife)) || isCurrentWorkItemOpenPrint(nbaDst))) { // APSL2720,
																																		// CHAUG005,APSL5128
			throw new NbaTransactionValidationException(UND_REVIEW_REQ);
		}

		return nbaDst;
	}

	/**Returns true if the change is a restricted change
	 * @return boolean
	 */
	protected boolean verifyRestrictedChanges(NbaTXLife nbaTxLife) {
		boolean restricted = true; //APSL5128
		Iterator registerChangesItr = registeredChanges.iterator();
		while (registerChangesItr.hasNext()) {
			AxaDataChangeEntry change = (AxaDataChangeEntry) registerChangesItr.next();
			for(int i = 0; i < changeTypes.length; i++) { 
				if((DC_UND_APPROVAL_CHANGED == change.getChangeType()) && nbaTxLife.isUnderwriterApproved()) {//If case is underwriter first time
					return false;
				}
					/*if (changeTypes[i] == change.getChangeType()) { // APSL5128 - code commented
						restricted = true;
					}*/
			}
		}
		return restricted;
	}

	protected boolean verifyRestrictedUWChanges() {
		boolean restricted = false; // APSL5128
		Iterator registerChangesItr = registeredChanges.iterator();
		while (registerChangesItr.hasNext()) {
			AxaDataChangeEntry change = (AxaDataChangeEntry) registerChangesItr.next();
			for (int i = 0; i < changeTypes.length; i++) {
				if (changeTypes[i] == change.getChangeType()) {
					restricted = true;
				}
			}
		}
		return restricted;
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

	// APSL5128 - New Method
	private boolean isCurrentWorkItemOpenPrint(NbaDst nbaDst) {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			ExternalContext extContext = context.getExternalContext();
			nbaDst = NbaSessionUtils.getOriginalWork((HttpSession) extContext.getSession(false));
			if (nbaDst.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CONT_PRINT_EXTRACT)
					&& !nbaDst.getQueue().equalsIgnoreCase(NbaConstants.END_QUEUE)) {
				return true;
			}
		}
		return false;
	}

}
