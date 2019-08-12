package com.csc.fsg.nba.process.contract;

/* 
 * *******************************************************************************<BR>
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.util.Iterator;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import com.csc.fs.Message;
import com.csc.fs.Messages;
import com.csc.fs.Result;
import com.csc.fs.UserSessionKey;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaContractLockData;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Create a contract a lock for this user and contract. The contract locks prevents multiple concurrent
 * updates to the contract data by different users or processes. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class ContractLockBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            WorkItem workItem = (WorkItem) input;
            NbaContractLockData nbaContractLockData = new NbaContractLockData();
            Iterator it = workItem.getLobData().iterator();
            while (it.hasNext()) {
                LobData lobData = (LobData) it.next();
                if (NbaLob.A_LOB_BACKEND_SYSTEM.equals(lobData.getDataName())) {
                    nbaContractLockData.setBackendSystem((String) lobData.getDataValue());
                } else if (NbaLob.A_LOB_COMPANY.equals(lobData.getDataName())) {
                    nbaContractLockData.setCompanyCode((String) lobData.getDataValue());
                } else if (NbaLob.A_LOB_POLICY_NUMBER.equals(lobData.getDataName())) {
                    nbaContractLockData.setContractKey((String) lobData.getDataValue());
                }
            }
            UserSessionKey userSessionKey = getServiceContext().getUserSession().getUserSessionKey();
            nbaContractLockData.setUserId(userSessionKey.getUserId());
            //Begin NBLXA-1978
            HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			NbaUserVO user=(NbaUserVO)session.getAttribute(NbaConstants.S_KEY_USER);
			String fullName=user.getFullName();
			nbaContractLockData.setUserName(fullName);
            //End NBLXA-1978
            //Attempt lock only if all the required fields are present
            if (validData(nbaContractLockData)) {
                nbaContractLockData = NbaContractLock.processLockRequest(nbaContractLockData, NbaConstants.UPDATE);
                if (!nbaContractLockData.isLockedForUser()) {
                    result.addMessage(formatErrorMessage(nbaContractLockData));
                    result.addResult(nbaContractLockData.getLockedBy()); //ALS556
                    result.setErrors(true);
                }
            }
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Format the error message to be returned to the user.
     * 
     * @param nbaContractLockData
     * @return
     */
    private Message formatErrorMessage(NbaContractLockData nbaContractLockData) {
        StringBuffer buff = new StringBuffer();
        buff.append("Unable to lock contract data for BackendSystem=");
        buff.append(nbaContractLockData.getBackendSystem());
        buff.append(" CompanyCode=");
        buff.append(nbaContractLockData.getCompanyCode());
        buff.append(" Contract=");
        buff.append(nbaContractLockData.getContractKey());
        buff.append(" User=");
        buff.append(nbaContractLockData.getUserId());
        buff.append("  Locked By="); //ALS5566
        buff.append(nbaContractLockData.getLockedBy()); //ALS5566
        Message msg = Messages.STD_INFO.setVariableData(new Object[] { buff.toString() });
        return msg;
    }

    /**
     * Determine if all the required fields are present in the
     * NbaContractLockData
     * 
     * @param nbaContractLockData
     * @return true if all the required fields are present
     */
    private boolean validData(NbaContractLockData nbaContractLockData) {
		return (!NbaUtils.isBlankOrNull(nbaContractLockData.getBackendSystem()) 
				&& !NbaUtils.isBlankOrNull(nbaContractLockData.getCompanyCode())
				&& !NbaUtils.isBlankOrNull(nbaContractLockData.getContractKey()) 
				&& !NbaUtils.isBlankOrNull(nbaContractLockData.getUserId()));
	}
}
