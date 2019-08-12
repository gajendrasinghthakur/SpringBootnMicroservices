package com.csc.fsg.nba.process.contract;

/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group?.  The use,<BR>
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
import com.csc.fsg.nba.business.rule.NbaDeterminePlan;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaOverrideContractUpdateVO;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * Accepts an <code>NbaOverrideContractUpdateVO</code> as input to commit contract and work item
 * changes to the appropriate systems. It also performs Plan code determination prior to commit changes 
 * for contract and work If the <code>NbaTXLife</code> is present in the
 * NbaOverrideContractUpdateVO, then any change will be persisted to the appropriate back end
 * system.  The work item, <code>NbaDst</code>, will be updated based on the UpdateWork
 * and UnlockWork flags set on the NbaOverrideContractUpdateVO.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> 
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr> 
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class DeterminePlanCommitContractBP extends CommitContractBP {

    /**
     * 
     * @return <code>AccelResult</code> object containing the updated <code>NbaDst</code> object.
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
                NbaOverrideContractUpdateVO requestVO = (NbaOverrideContractUpdateVO) input;
                requestVO.setNbaTXLife(determinePlanCode(requestVO));
                result = persistContract(requestVO);
        } catch (Exception e) {
			result = new AccelResult();
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * @param requestVO object of NbaContractUpdateVO to get the NbaTXLife, NbaDst and UserID instances
     * @throws NbaBaseException
     */
    protected NbaTXLife determinePlanCode(NbaOverrideContractUpdateVO requestVO) throws NbaBaseException {
        NbaDst dst = requestVO.getNbaDst();
        NbaTXLife nbaTXLife = requestVO.getNbaTXLife();
        NbaLob aNbaLob = dst.getNbaLob();
        NbaDeterminePlan determinePlan = new NbaDeterminePlan(); //NBLXA-2181
        determinePlan.updateBenefitsAndRiders(dst, nbaTXLife); //NBLXA-2181
        if (NbaConfiguration.getInstance().isGenericPlanImplementation() && !requestVO.isOverriden() && (aNbaLob.getContractChgType() == null)
                && (aNbaLob.getAppOriginType() != 0)) {
            nbaTXLife = determinePlan.determinePlanCode(dst, nbaTXLife);
        }
        return nbaTXLife;
    } 
}
