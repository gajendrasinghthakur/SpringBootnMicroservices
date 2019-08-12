package com.csc.fsg.nba.process.contract.distributionChannel;

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

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.contract.MarkupPlanChangeBP;
import com.csc.fsg.nba.vo.AxaReassignDataVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaValidateContractRequest;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;


/**
 * Commits a base plan change request for a contract.  The contract will be modified
 * to remove contract information that is no longer applicable due to the plan change.
 * A comment will also be added which audits this plan change.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <tr><td>NBLXA-1538</td><td>Version 7</td><td>Distribution Channel Update for TconV</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class UpdateDistChannelBP extends MarkupPlanChangeBP {

    /* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            result = commit((NbaValidateContractRequest) input);
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
	protected AccelResult commit(NbaValidateContractRequest request) throws Exception {
		NbaUserVO user = request.getNbaUserVO();
		NbaDst parent = request.getNbaDst();
		NbaTXLife txlife = request.getNbaTXLife();
		String msg = "Distribution Channel change initiated.";
		Policy policy = txlife.getPolicy();
		PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
		long olddistChannel=policyExt.getDistributionChannel();
		long newDistChannel=olddistChannel==6?10:6;
		updateDistChannel(parent,newDistChannel,policyExt);
		addComments(user, parent, msg);
		parent.setNbaUserVO(user);
		AccelResult result = (AccelResult) callService("NbaUpdateWorkBP", parent);
		if (!result.hasErrors()) {
		txlife.setBusinessProcess("DISTUPDATE");
		NbaContractAccess.doContractUpdate(txlife, parent, user);
		}
		return result;
	}
	
	protected void addComments(NbaUserVO user, NbaDst nbaDst,String msg) {
		NbaProcessingErrorComment comment = new NbaProcessingErrorComment();
		comment.setText(msg);
		comment.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
		comment.setOriginator(user.getUserID());
		comment.setUserNameEntered(user.getUserID());
		comment.setActionAdd();
		nbaDst.addManualComment(comment.convertToManualComment());
	}
	
	
	public void updateDistChannel(NbaDst parent, long distChannel, PolicyExtension policyExtension){
		parent.getNbaLob().setDistChannel(Long.toString(distChannel));
		parent.setUpdate();
		policyExtension.setDistributionChannel(distChannel);
		policyExtension.setActionUpdate();
	}
	
	
	public AxaReassignDataVO createReassignVO(Policy policy,NbaUserVO user, long olddistChannel){
		AxaReassignDataVO reassignVo = new AxaReassignDataVO();
		reassignVo.setPolicynumber(policy.getPolNumber());
		reassignVo.setCompanyKey(policy.getCompanyKey());
		reassignVo.setBackendKey(policy.getBackendKey());
		reassignVo.setChangedType("DistributionChannel");
		reassignVo.setUserCode(user.getUserID());
		reassignVo.setStatus("Active");
		reassignVo.setChangedValue(Long.toString(olddistChannel));
		return reassignVo;
	}
	
}