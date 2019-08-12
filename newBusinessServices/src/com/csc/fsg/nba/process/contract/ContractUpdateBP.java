package com.csc.fsg.nba.process.contract;

/* 
 * *******************************************************************************<BR>
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NewBusinessContractUpdateRequest;

/**
 * Commits a contract to the backend system.  Any new development should use
 * <code>CommitContractBP</code>.
 * <p>
 * This class is provided for backwards compatibility for views that have not been 
 * rewritten to JSF but require addess to the services tier to save contract data.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>AXAL3.7.09</td><td>AXA Life Phase 1</td><td>Underwriter Workbench</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 * @see CommitContractBP
 * @deprecated
 */

public class ContractUpdateBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
    		NewBusinessContractUpdateRequest contractUpdate = (NewBusinessContractUpdateRequest)input;
    		contractUpdate.contract.setAccessIntent(contractUpdate.dst.isLocked(contractUpdate.user.getUserID()) ? NbaConstants.UPDATE : NbaConstants.READ);
    		NbaTXLife nbaTXLife = NbaContractAccess.doContractUpdate(contractUpdate.contract, contractUpdate.dst, contractUpdate.user);
    		if (contractUpdate.dst.isCase()) {
    			contractUpdate.dst.getNbaLob().updateLobFromNbaTxLife(nbaTXLife);
    			contractUpdate.dst.setUpdate();
    		}
   			result.addResult(nbaTXLife);
        } catch (NbaTransactionValidationException e) {
            addMessage(result, e.getMessage());
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }
}
