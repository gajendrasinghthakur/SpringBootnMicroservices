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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeEntry;
import com.csc.fsg.nba.exception.NbaBaseException;
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
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ChangeSubType;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
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
 * <tr><td>P2AXAL053</td><td>AXA Life Phase 2</td><td>R2 Auto Underwriting</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaUpdatePriorInsuranceTransaction extends AxaDataChangeTransaction implements NbaConstants{
	protected NbaLogger logger = null;
		
	protected static	long[] changeTypes = { 	
			DC_FACE_AMT,
			DC_APP_STATE,
			DC_SIGNEDDATE,
			DC_ISSUEDATE,
			DC_PRODUCTCODE,
			DC_INSURED_NAME,
			DC_INSURED_FIRSTNAME,
			DC_INSURED_LASTNAME,
			DC_INSURED_MIDDLENAME,
			DC_INSURED_SUFFIX,
			DC_INSURED_PREFIX,
			DC_INSURED_DOB, 
			DC_INSURED_GENDER, 
			DC_JNT_INSURED_DOB,
			DC_JNT_INSURED_GENDER, 
			DC_RIDER_AMT,	
			DC_RIDER_ADDED,
			DC_RIDER_DELETED,
			DC_FIN_ACT_REVERSE,
			DC_PERMANENT_FLAT_EXTRA_RATING_ADDED,
			DC_PERMANENT_FLAT_EXTRA_RATING_DELETED,
			DC_TEMP_FLAT_EXTRA_RATING_ADDED,
			DC_TEMP_FLAT_EXTRA_RATING_DELETED,
			DC_PERMANENT_RATING_ADDED,
			DC_PERMANENT_RATING_DELETED,
			DC_RELATION_CHANGED,
			DC_RELATION_ADDED,
			DC_RELATION_DELETED,
			DC_OWNER_ADDED,
			DC_OWNER_DELETED,
			DC_INSURED_ADDED,
			DC_INSURED_DELETED,
			DC_PENDING_CONTRACT_STATUS  // QC6756
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
		if (isCallNeeded() && nbaTxLife.isReOpenedCase()) { //QC2630,ALS5459, APSL459,QC#6756, APSL2662
			List changeSubTypeList = createChangeSubTypeList(nbaTxLife);
			if (changeSubTypeList != null && !changeSubTypeList.isEmpty()) {
				AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_UPD_PRIOR_INSURANCE, user,
						nbaTxLife, null, changeSubTypeList);
				webServiceInvoker.execute();
			}
			
		}
		return nbaDst;
	}
	
	/**
	 * @return changeSubTypeList
	 */
	protected List createChangeSubTypeList(NbaTXLife nbaTxLife) {
		List changeSubTypeList = new ArrayList();
		Iterator registerChangesItr = registeredChanges.iterator();
		while (registerChangesItr.hasNext()) {
			AxaDataChangeEntry change = (AxaDataChangeEntry) registerChangesItr.next();
			ChangeSubType changeSubType = null;
			if (DC_FACE_AMT == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_FACEAMT, change.getChangedObjectId(), FACE_AMOUNT,
						NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_ISSUEDATE == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_REQISSUE, change.getChangedObjectId(), ISSUE_DATE,
						NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_PRODUCTCODE == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PLANINS, change.getChangedObjectId(), PRODUCT_CODE,
						NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_INSURED_FIRSTNAME == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(), FIRST_NAME,
						NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_INSURED_LASTNAME == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(), LAST_NAME,
						NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_INSURED_MIDDLENAME == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(), MIDDLE_NAME,
						NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_INSURED_SUFFIX == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(), SUFFIX,
						NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_INSURED_PREFIX == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(), PREFIX,
						NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_INSURED_DOB == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(), BIRTH_DATE,
						NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_INSURED_GENDER == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(), GENDER,
						NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_PERMANENT_FLAT_EXTRA_RATING_ADDED == change.getChangeType() || DC_TEMP_FLAT_EXTRA_RATING_ADDED == change.getChangeType()
					|| DC_PERMANENT_RATING_ADDED == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_SUB_STANDARD_RATING, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_INSERT);
			}
			if (DC_PERMANENT_FLAT_EXTRA_RATING_DELETED == change.getChangeType() || DC_TEMP_FLAT_EXTRA_RATING_DELETED == change.getChangeType()
					|| DC_PERMANENT_RATING_DELETED == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_SUB_STANDARD_RATING, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_DELETE);
			}
			if (DC_RIDER_ADDED == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_COVCHG, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_INSERT);
			}
			if (DC_RIDER_DELETED == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_COVCHG, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_DELETE);
			}
			if (DC_RELATION_DELETED == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_CHGRELATION, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_DELETE);
			}
			if (DC_RELATION_ADDED == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_CHGRELATION, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_INSERT);
			}
			if (DC_RELATION_CHANGED == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_CHGRELATION, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_OWNER_ADDED == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_INSERT);
			}
			if (DC_OWNER_DELETED == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_DELETE);
			}
			if (DC_INSURED_ADDED == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_INSERT);
			}
			if (DC_INSURED_DELETED == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_DELETE);
			}
			//Begin APSL853
			if (DC_RIDER_AMT == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_COVCHG, change.getChangedObjectId(),
						NbaOliConstants.TC_CONTENT_UPDATE);
			}//End APSL853
			
            //Begin QC#6756
			if (DC_PENDING_CONTRACT_STATUS == change.getChangeType() && nbaTxLife.isReOpenedCase()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHGTYPE_REOPEN_RESET, change.getChangedObjectId(),
						PENDING_CONTRACT_STATUS, NbaOliConstants.TC_CONTENT_UPDATE);
			}
			//END QC#6756
			//APSL2980,QC#11527
			if ((!NbaUtils.isBlankOrNull(changeSubType)) && (!NbaUtils.isBlankOrNull(changeSubType.getChangeTC())) && (!NbaUtils.isBlankOrNull(changeSubType.getChangeID()))) {
				changeSubTypeList.add(changeSubType);
			}
		
		}
		return changeSubTypeList;
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
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ENABLE_PRIORINSURANCE_INTERFACE_CALL);
	}
	
	//APSL459 Method Deleted protected boolean isPaidReissue(NbaTXLife nbaTxLife, NbaDst nbaDst)
}
