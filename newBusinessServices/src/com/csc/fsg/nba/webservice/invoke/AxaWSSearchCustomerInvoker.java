/*
 * ******************************************************************************* <BR> This program contains trade secrets and confidential
 * information which <BR> are proprietary to CSC Financial Services Group�. The use, <BR> reproduction, distribution or disclosure of this program, in
 * whole or in <BR> part, without the express written permission of CSC Financial Services <BR> Group is prohibited. This program is also an
 * unpublished work protected <BR> under the copyright laws of the United States of America and other <BR> countries. If this program becomes
 * published, the following notice shall <BR> apply: Property of Computer Sciences Corporation. <BR> Confidential. Not for publication. <BR> Copyright
 * (c) 2002-2013 Computer Sciences Corporation. All Rights Reserved. <BR>
 * 
 * ******************************************************************************* <BR>
 */
package com.csc.fsg.nba.webservice.invoke;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;

import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.SourceInfo;

/**
 * This class is responsible for creating request for searchCustomer webservice .
 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */



public class AxaWSSearchCustomerInvoker extends AxaWSInvokerBase {

	private static final String CATEGORY = "CIP";

	private static final String FUNCTIONID = "searchCustomer";
	

	/**
	 * @param operation
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 * @param object
	 */
	public AxaWSSearchCustomerInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}

	/**
	 * Create webservice request for searchCustomer
	 * @return nbaTXLife
	 */
	public NbaTXLife createRequest() throws NbaBaseException {

		NbaUserVO user = getUserVO();
		Policy policy = getNbaTXLife().getPolicy();
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_PARTYSRCH);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setNbaUser(user);
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		OLifE olife = nbaReqTXLife.getOLifE();
		SourceInfo sourceInfo = new SourceInfo();
		sourceInfo.setFileControlID(getBackEnd());
		sourceInfo.setSourceInfoName("nbA_Life");//NBLXA-2152
		olife.setSourceInfo(sourceInfo);
		Holding holding = new Holding();
		holding.setId(HOLDING_ID);
		Policy nbaPolicy = new Policy();
		nbaPolicy.setPolNumber(policy.getPolNumber());
		holding.setPolicy(nbaPolicy);
		olife.addHolding(holding);
		return nbaReqTXLife;

	}
	
}
