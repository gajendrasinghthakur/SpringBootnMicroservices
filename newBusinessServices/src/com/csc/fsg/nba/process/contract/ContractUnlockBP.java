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

import com.csc.fs.Result;
import com.csc.fs.UserSessionKey;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaContractLockData;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Remove the contract locks for this user. The contract locks prevents multiple concurrent
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

public class ContractUnlockBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            UserSessionKey userSessionKey = getServiceContext().getUserSession().getUserSessionKey();
            NbaUserVO nbaUserVO = new NbaUserVO(userSessionKey.getUserId(), "");
            if (input != null && input instanceof WorkItem) {
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
                //Attempt unlock only if all the required fields are present
                if (validData(nbaContractLockData, nbaUserVO)) {    
                    NbaContractLock.removeLock(nbaContractLockData, nbaUserVO);
                }
            } else if (input != null && input instanceof NbaDst) {
                NbaContractLockData nbaContractLockData = new NbaContractLockData();
                NbaDst nbaDst = (NbaDst) input;
                NbaLob nbaLob = nbaDst.getNbaLob();
                
                nbaContractLockData.setBackendSystem(nbaLob.getBackendSystem());
                nbaContractLockData.setCompanyCode(nbaLob.getCompany());
                nbaContractLockData.setContractKey(nbaLob.getPolicyNumber());
                if (validData(nbaContractLockData, nbaUserVO)) {                	
                    NbaContractLock.removeLock(nbaContractLockData, nbaUserVO);
                }
            } else { //Default to unlocking everything for the user
                NbaContractLock.removeLock(nbaUserVO);
            }            
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }
    /**
     * Determine if all the required fields are present in the NbaContractLockData and NbaUserVO
     * @param nbaContractLockData
     * @param nbaUserVO
     * @return true if all the required fields are present
     */
    private boolean validData(NbaContractLockData nbaContractLockData, NbaUserVO nbaUserVO) {
        return (nbaContractLockData.getBackendSystem() != null && nbaContractLockData.getCompanyCode() != null
                && nbaContractLockData.getContractKey() != null && nbaUserVO.getUserID() != null);
    }
}
