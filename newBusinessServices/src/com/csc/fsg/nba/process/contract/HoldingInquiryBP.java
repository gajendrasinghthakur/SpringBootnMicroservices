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
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaHoldingInqRequest;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * Returns a holding inquiry from the backend system matching the criteria specified
 * in the input <code>NbaHoldingInqRequest</code>.  The given request should include
 * the current user (<code>NbaUserVO</code>) and a work item (<code>NbaDst</code>).
 * <p>
 * A successful output will return the <code>NbaHoldingInqRequest</code> populated
 * with a <code>NbaTXLife</code>.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class HoldingInquiryBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
    		NbaHoldingInqRequest holdingReq = (NbaHoldingInqRequest)input;

    		NbaTXLife holdingInq = NbaContractAccess.doContractInquiry(createRequestObject(holdingReq));
    		TransResult transResult = holdingInq.getTransResult();
    		long resultCode = transResult.getResultCode();
    		if (resultCode > 1) {
    			String error = "Back end processing failed";
    			if (transResult.getResultInfoCount() > 0) {
    			    if (transResult.getResultInfoAt(0) != null && !NbaUtils.isBlankOrNull(transResult.getResultInfoAt(0).getResultInfoDesc())) {//ALS4823
    			        error = transResult.getResultInfoAt(0).getResultInfoDesc();
    			    }
    			}
    			throw new NbaBaseException(error);
    		}
    		// [TODO] verify performance difference if this line is removed in the 6.12 build
    		//holdingInq.toXmlString();  // force a complete load
    		NbaDst nbaDst = holdingReq.getWork();
    		if (nbaDst.isCase()) {
    			nbaDst.updateLobFromNbaTxLife(holdingInq);
    		} else {
    			nbaDst.getNbaLob().updateLobForTransactionFromNbaTxLife(holdingInq);
    		}
    		holdingReq.setContract(holdingInq);
    		result.addResult(holdingReq);
    	//begin NBA208-32 
        } catch (NbaBaseException e) {
            if (e.isLogged()) {
                result.setErrors(true);
            } else {
                addExceptionMessage(result, e);
            }
        //end NBA208-32
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Create a TX Request value object that will be used to retrieve the contract.
     * @param holdingReq a holding request value object
     * @return a value object that is the request
     */
    public NbaTXRequestVO createRequestObject(NbaHoldingInqRequest holdingReq) {
    	NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
    	nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
    	nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
    	nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
    	nbaTXRequest.setNbaLob(holdingReq.getWork().getNbaLob());
    	nbaTXRequest.setNbaUser(holdingReq.getNbaUserVO());
    	nbaTXRequest.setWorkitemId(holdingReq.getWork().getID()); 
    	nbaTXRequest.setCaseInd(holdingReq.getWork().isCase()); 
   		nbaTXRequest.setAccessIntent(holdingReq.getWork().isLocked(holdingReq.getNbaUserVO().getUserID()) ? NbaConstants.UPDATE : NbaConstants.READ); 
    	if (holdingReq.getBusinessFunction() != null) {
    		nbaTXRequest.setBusinessProcess(holdingReq.getBusinessFunction());
    	} else {
    		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(holdingReq.getNbaUserVO())); 
    	}
    	return nbaTXRequest;
    }
}
