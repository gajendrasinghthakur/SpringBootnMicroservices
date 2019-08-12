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



import com.csc.fsg.nba.database.NbaCriticalDataChangeAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaCriticalDataChangeVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Policy;
/**
 * 
 * This class encapsulates checks whenever following changes are made to the Insured on the policy. - Name. - Tax
 * Identification. - Tax Identification Type. - Date of Birth 
 *  
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <tr><td>APSL4067</td><td>Discretionary</td><td>Critical Data Change</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaUpdateCriticalDataChangeTransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;
	
	
	protected static	long[] changeTypes = { 	DC_INSURED_FIRSTNAME,
												DC_INSURED_LASTNAME,
												DC_INSURED_MIDDLENAME,
												DC_INSURED_SSN,
												DC_INSURED_DOB,
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
		// Insert a record in DB with the details that have to be changed deep through the case.
		if (isCallNeeded() && nbaDst.isCase()) {
			String subTypesString = getChangeTypesString();			
			if (!NbaUtils.isBlankOrNull(subTypesString) && subTypesString.indexOf("#") != -1) {
				NbaCriticalDataChangeVO criticalDataChangeVO = new NbaCriticalDataChangeVO();
				Policy policy = nbaTxLife.getPolicy();
				criticalDataChangeVO.setCompanyKey(policy.getCarrierCode());
				criticalDataChangeVO.setBackendKey(policy.getCarrierAdminSystem());
				criticalDataChangeVO.setContractKey(policy.getPolNumber());
				criticalDataChangeVO.setCriticalData(subTypesString);
				criticalDataChangeVO.setWorkItemId(nbaDst.getID());
				NbaCriticalDataChangeAccessor.save(criticalDataChangeVO);
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
	 * @see com.csc.fsg.nba.business.transaction.AxaUpdateCriticalDataChangeTransaction#isTransactionAlive()
	 */
	protected boolean isTransactionAlive() {
		// TODO Auto-generated method stub
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ENABLE_CRITICAL_DATA_CHANGE_CALL);
	}
	
	protected String getChangeTypesString(){
		StringBuffer subTypesString = new StringBuffer();
		if(hasChangeSubType(DC_INSURED_FIRSTNAME)){
			subTypesString.append(NbaLob.A_LOB_FIRST_NAME);
			subTypesString.append("#");
		}if(hasChangeSubType(DC_INSURED_LASTNAME)){
			subTypesString.append(NbaLob.A_LOB_LAST_NAME);
			subTypesString.append("#");
		}if(hasChangeSubType(DC_INSURED_MIDDLENAME)){
			subTypesString.append(NbaLob.A_LOB_MIDDLE_INITIAL);
			subTypesString.append("#");
		}if(hasChangeSubType(DC_INSURED_SSN)){
			subTypesString.append(NbaLob.A_LOB_SSN_TIN);
			subTypesString.append("#");
		}if(hasChangeSubType(DC_INSURED_DOB)){
			subTypesString.append(NbaLob.A_LOB_DOB);
			subTypesString.append("#");
		}
		
		return subTypesString.toString();
	}
	
}

