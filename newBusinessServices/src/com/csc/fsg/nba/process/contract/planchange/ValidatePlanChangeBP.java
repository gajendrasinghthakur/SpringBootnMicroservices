package com.csc.fsg.nba.process.contract.planchange;

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
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.contract.MarkupPlanChangeBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaPlanChangeRequest;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA181</td><td>Version 7</td><td>Contract Plan Change Rewrite</td></tr>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class ValidatePlanChangeBP extends MarkupPlanChangeBP {

    /* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            result.addResult(validate((NbaPlanChangeRequest) input));
        } catch (Exception e) {
            result = new AccelResult();
            addExceptionMessage(result, e);
        }
        return result;
    }

	/**
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected NbaPlanChangeRequest validate(NbaPlanChangeRequest request) throws Exception {
	    NbaDst nbaDst = request.getNbaDst();
		NbaLob nbaLob = nbaDst.getNbaLob();
		String originalPlan = nbaLob.getPlan();
		nbaLob.setPlan(request.getSelectedPlan());
		setContractCopy(request.isRequestForContractCopy());
		setSelectedProduct(nbaDst, request.getSelectedPlan(), request.isOverriden());//NBA139
		
		NbaTXLife contract = getContract(request);
		contract = markupPlanChange(contract);
    	contract.setBusinessProcess(NbaConstants.PROC_VIEW_PLAN_CHANGE);
		validate(contract, nbaDst, request.getNbaUserVO());

		nbaLob.setPlan(originalPlan);
		request.setNbaTXLife(contract);
		request.setContractMessages(getMessages());
		request.setNewPlan(contract.getPolicy().getProductCode()); //NBA139
		return request;
	}
}


