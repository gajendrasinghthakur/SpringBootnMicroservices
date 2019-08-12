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

import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeEntry;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;

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
 * <td>APSL3360</td><td>AXA Life Phase 2</td><td>Transactional Message should appear �Requires UW approval�</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaGenerateReissueValidationTransaction extends AxaDataChangeTransaction implements NbaConstants{
	protected NbaLogger logger = null;
	
	protected static	long[] changeTypes = {
			DC_INSURED_FIRSTNAME,//APSL3808
			//DC_INSURED_MIDDLENAME,//APSL3808,APSL5128
			DC_INSURED_LASTNAME,
			DC_INSURED_DOB,
			DC_INSURED_SSN,
			//DC_APP_STATE, //APSL5128
			DC_POL_REQUESTEDDATE,
			DC_FACE_AMT_INCREASE,
			//DC_BENEFIT_ADDED, //commented for APSL5368 
			DC_OTHER_BENEFIT_ADDED, //APSL5368 
			DC_RIDER_ADDED,
			DC_RIDER_CTIR_ADDED, //APSL5128
			DC_RIDER_DPW_ADDED, //APSL5128
			DC_RIDER_LTC_ADDED, //APSL5128
			DC_INSURED_GENDER, //APSL5128
			//DC_OWNER_CHANGE, //APSL5128
			DC_NEWOWNER_ADDED,//APSL5128
			DC_JNT_INSURED_FIRSTNAME, //APSL5128
			DC_JNT_INSURED_LASTNAME, //APSL5128
			DC_JNT_INSURED_MIDDLENAME, //APSL5128
			DC_JNT_INSURED_DOB, //APSL5128
			DC_JNT_INSURED_SSN, //APSL5128
			DC_JNT_INSURED_GENDER, //APSL5128
			//DC_BENEFICIARY_CHANGE, //APSL5128
			//DC_CONTBENEFICIARY_CHANGE, //5128
			DC_BENEFICIARY_ADD,
			DC_CONTBENEFICIARY_ADD,
			DC_UND_APPROVAL_CHANGED,
			DC_RIDER_ROPR_ADDED //APSL5128
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
		if (isCallNeeded() && nbaTxLife.isUnderwriterApproved() && verifyRestrictedChanges(nbaTxLife) && !NbaUtils.isAdcApplication(nbaDst)) {
			throw new NbaTransactionValidationException(UND_APPROVAL_REQ);
		}
		return nbaDst;
	}
	
	/**Returns true if the change is a restricted change
	 * @return boolean
	 */
	protected boolean verifyRestrictedChanges(NbaTXLife nbaTxLife) {
		boolean restricted = false;
		Iterator registerChangesItr = registeredChanges.iterator();
		while (registerChangesItr.hasNext()) {
			AxaDataChangeEntry change = (AxaDataChangeEntry) registerChangesItr.next();
			for(int i = 0; i < changeTypes.length; i++) { 
				if((DC_UND_APPROVAL_CHANGED == change.getChangeType()) && nbaTxLife.isUnderwriterApproved()) {//If case is underwriter first time
					return false;
				}
				if (changeTypes[i] == change.getChangeType()
						&& change.getChangeType() != DC_POL_REQUESTEDDATE 
						&& change.getChangeType() != DC_BENEFICIARY_ADD
						&& change.getChangeType() != DC_CONTBENEFICIARY_ADD
						&& change.getChangeType() != DC_UND_APPROVAL_CHANGED) {
					return true;
				}	
				ApplicationInfo appInfo = nbaTxLife.getPolicy().getApplicationInfo();
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
				//For start date change, do not generate the validation if medical evidence is not expired.
				if(appInfoExt != null) {
					if ((DC_POL_REQUESTEDDATE == change.getChangeType() && appInfoExt.getMedicalEvidenceExpiredInd())) {
						return true;
					}	
				}
				//For beneficiary change, do not generate the validation if owner is same as insured.
				if((DC_BENEFICIARY_ADD == change.getChangeType() || DC_CONTBENEFICIARY_ADD == change.getChangeType()) 
						&& !nbaTxLife.isOwnerSameAsPrimaryIns()) {
					return true;
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

}
