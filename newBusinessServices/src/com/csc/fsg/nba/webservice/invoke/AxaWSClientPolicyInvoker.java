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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
package com.csc.fsg.nba.webservice.invoke;

import java.util.Date;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * This class is responsible for validating non nba contract number by calling Call the CIF Client Webservice.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.25</td>
 * <td>AXA Life Phase 1</td>
 * <td>Compensation Interface</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSClientPolicyInvoker extends AxaWSInvokerBase {
	
	private static final String CATEGORY = "CIFRETRIEVE";

	private static final String FUNCTIONID = "CIFServiceRetrieve";
	
	
	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSClientPolicyInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}

	public NbaTXLife createRequest() throws NbaBaseException {
		String policyNumber =(String)getObject();
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setNbaUser(getUserVO());
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
		nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_CIF_HOLDING_INQUIRY);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTONLY);
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		nbaReqTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeDate(new Date());
		nbaReqTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeTime(new NbaTime());
		OLifE olife = new OLifE();
		olife.setVersion(NbaOliConstants.OLIFE_VERSION);
		olife.setSourceInfo(createSoureInfo());
		Holding holding = new Holding();
		holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		holding.setCarrierAdminSystem(getBackEnd());
		Policy policy = new Policy();
		policy.setPolNumber(policyNumber);
		holding.setPolicy(policy);
		holding.setId(HOLDING_ID);
		olife.addHolding(holding);
		nbaReqTXLife.setOLifE(olife);
		return nbaReqTXLife;
		
	}
	
	//ALS4106 - Added response handling
    protected void handleResponse() throws NbaBaseException {
    	super.handleResponse();
		NbaTXLife nbaTXLifeResponse = (NbaTXLife)  getWebserviceResponse();
		if (nbaTXLifeResponse != null) {
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Transaction 203 - ValidatePolicyNumber - Response -> " + nbaTXLifeResponse.toXmlString());
			}
			UserAuthResponseAndTXLifeResponseAndTXLifeNotify txlifeResponse = nbaTXLifeResponse.getTXLife()
					.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
			if (txlifeResponse.getTXLifeResponseCount() > 0 && txlifeResponse.getTXLifeResponseAt(0).hasTransResult()) {
				TransResult transResult = txlifeResponse.getTXLifeResponseAt(0).getTransResult();
				if (NbaOliConstants.TC_RESCODE_FAILURE == transResult.getResultCode() || transResult.getRecordsFound() < 1) {
					throw new NbaBaseException("Policy Number "+ nbaTXLifeResponse.getOLifE().getHoldingAt(0).getPolicy().getPolNumber()+ " is Not Valid");
				}
			}
		}					
    }
	
}
