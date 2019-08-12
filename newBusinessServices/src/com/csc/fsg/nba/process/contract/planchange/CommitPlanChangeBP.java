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
import com.csc.fs.accel.valueobject.Comment;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.contract.MarkupPlanChangeBP;
import com.csc.fsg.nba.vo.NbaGeneralComment;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaOverrideContractUpdateVO;
import com.csc.fsg.nba.vo.NbaPlanChangeRequest;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;


/**
 * Commits a base plan change request for a contract.  The contract will be modified
 * to remove contract information that is no longer applicable due to the plan change.
 * A comment will also be added which audits this plan change.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA181</td><td>Version 7</td><td>Contract Plan Change Rewrite</td></tr>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class CommitPlanChangeBP extends MarkupPlanChangeBP {

    /* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            result = commit((NbaPlanChangeRequest) input);
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
	protected AccelResult commit(NbaPlanChangeRequest request) throws Exception {

		setContractCopy(request.isRequestForContractCopy());
		setSelectedProduct(request.getNbaDst(), request.getSelectedPlan(), request.isOverriden());//NBA139
		NbaTXLife contract = getContract(request);
		contract = markupPlanChange(contract);
		updateWorkItem(request);
		
		NbaOverrideContractUpdateVO contractVO = new NbaOverrideContractUpdateVO();//NBA139
		contractVO.setNbaDst(request.getNbaDst());
		contractVO.setNbaTXLife(contract);
		contractVO.setNbaUserVO(request.getNbaUserVO());
		contractVO.setUpdateWork(true); 
		contractVO.setOverriden(request.isOverriden()); //NBA139
		Result res = callService("DeterminePlanCommitContractBP", contractVO);  //NBA139
		AccelResult accelResult = new AccelResult();
		if (res.hasErrors()) {
			accelResult.setErrors(res.hasErrors());
			accelResult.addMessages(res.getMessages());
		} else {
			contractVO = (NbaOverrideContractUpdateVO) res.getFirst();//NBA139
			request.setNbaTXLife(contractVO.getNbaTXLife());
			request.setNbaDst(contractVO.getNbaDst());
			accelResult.addResult(request);
		}
		return accelResult;
	}
	/**
	 * Updates the current work item by updating the PLAN and PROD LOBs, and adding
	 * a comment with the information of old plan and new plan to audit the change.
	 * @param request
	 * @return
	 */
	public void updateWorkItem(NbaPlanChangeRequest request) throws NbaBaseException {
	    NbaLob nbaLob = request.getNbaDst().getNbaLob();
        request.getNbaDst().addManualComment(getGeneralComment(nbaLob.getPlan(), request.getSelectedPlan(), request.getNbaUserVO()));
        nbaLob.setPlan(request.getSelectedPlan());
        nbaLob.setProductTypSubtyp(Long.toString(getSelectedProduct()));
        nbaLob.setActionUpdate();
        request.getNbaDst().setUpdate();
	}
	
	/**
	 * Creates a <code>Comment</code>, which will be added as a workflow
	 * comment for the contract when the base plan has been changed.  The comment
	 * contains the name of the original plan and the name of the new plan.
	 * @param currentPlan
	 * @param selectedPlan
	 * @param nbaUserVO
	 * @return Comment
	 */
	 //NBA208-32
	protected Comment getGeneralComment(String currentPlan, String selectedPlan, NbaUserVO nbaUserVO) throws NbaBaseException  {
		NbaGeneralComment generalComment = new NbaGeneralComment();
		generalComment.setEnterDate(getTimeStamp(nbaUserVO)); 
		generalComment.setText("The Base Plan has been changed from " + currentPlan + " to " + selectedPlan);
		generalComment.setOriginator(nbaUserVO.getUserID());
		generalComment.setActionAdd();
		return generalComment.convertToManualComment();
	}
}


