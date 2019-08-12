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

import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeEntry;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
/**
 * 
 * This class encapsulates checks whenever following changes are made to the Insured.  First Name - gender - Middle Name
 * last Name - Gender/Sex. - Date of Birth - 
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>ALS3963</td><td>AXA Life Phase 2</td><td>MIB check requirement created when Evaluate is selected</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaUpdateMIBTransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;
	protected static long[] changeTypes = { 
											DC_INSURED_DOB, 
											DC_INSURED_GENDER, 
											DC_INSURED_FIRSTNAME,
											DC_INSURED_LASTNAME,
											DC_JNT_INSURED_DOB, 
											DC_JNT_INSURED_GENDER, 
											DC_JNT_INSURED_FIRSTNAME,
											DC_JNT_INSURED_LASTNAME,
											DC_DEPENDENT_DOB, 
											DC_DEPENDENT_GENDER, 
											DC_DEPENDENT_FIRSTNAME,
											DC_DEPENDENT_LASTNAME,
										    //DC_INSURED_MIDDLENAME
											//NBLXA-1812 --Added changeTypes for Joint Insured and Dependent  
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
		if (isCallNeeded() && nbaDst.isCase()) {
			List reqInfoList = null;			   
			// Start NBLXA-1812--deleted old code
			Iterator registerChangesItr = registeredChanges.iterator();
			while (registerChangesItr.hasNext()) {
				AxaDataChangeEntry change = (AxaDataChangeEntry) registerChangesItr.next();
				Party party = NbaTXLife.getPartyFromId(change.getChangedObjectId(), nbaTxLife.getOLifE().getParty());
				if (party != null) {
					reqInfoList = nbaTxLife.getRequirementInfoList(new NbaParty(party), NbaOliConstants.OLI_REQCODE_MIBCHECK);
					setReqSubType(reqInfoList);
				}
			}
			// End NBLXA-1812
		}
		return nbaDst;
	}
	
	//NBLXA-1812 new Method
	protected void setReqSubType(List reqInfoList) {
		if (!NbaUtils.isBlankOrNull(reqInfoList)) {
			int count = reqInfoList.size();
			for (int i = 0; i < count; i++) {
				RequirementInfo reqInfo = (RequirementInfo) reqInfoList.get(i);
				reqInfo.setReqSubStatus(NbaOliConstants.OLI_REQSUBSTAT_CNCLINSCO);
				reqInfo.setActionUpdate();
			}
		}
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
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ENABLE_MIB_INTERFACE_CALL);
	}

}
