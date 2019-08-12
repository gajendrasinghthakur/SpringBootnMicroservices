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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Policy;

/**
 * This class is responsible for creating request for agent search webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL041</td><td>AXA Life Phase 2</td><td>Message received from OLSA Unit Number Validation Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSCheckBillingUnitNumberInvoker extends AxaWSInvokerBase {

    private static final String CATEGORY = "OLSA";

    private static final String FUNCTIONID = "validateSalaryAllotmentUnitCode";

	/**
	 * constructor for this invoker
	 * @param userVO
     * @param nbaTXLife
     * @param nbaDst
     * @param object
     */
    public AxaWSCheckBillingUnitNumberInvoker(String operation,NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super(operation,userVO, nbaTXLife, nbaDst, object);
        setBackEnd(ADMIN_ID);
        setCategory(CATEGORY);
        setFunctionId(FUNCTIONID);
    }

	/**
	 * Creates the request object for the webservice call
     * @throws NbaBaseException
     */
    public NbaTXLife createRequest() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.OLI_TRANSTYPE_BILLINGUNITNUMBER);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setNbaUser(getUserVO());
		NbaTXLife nbaTXLife = new NbaTXLife(nbaTXRequest);
        OLifE oLife = new OLifE();
        oLife.setVersion("2.9.03");
        Holding holding = new Holding();
        holding.setId(getNbaTXLife().getPrimaryHolding().getId());
        Policy policy = getNbaTXLife().getPolicy();
        Policy newPolicy = new Policy();
    	newPolicy.setPolNumber(policy.getPolNumber());
        newPolicy.setPaymentMethod(policy.getPaymentMethod());
        newPolicy.setBillNumber(policy.getBillNumber());
        holding.setPolicy(newPolicy);
        oLife.addHolding(holding);
		nbaTXLife.setOLifE(oLife);
	    return nbaTXLife;
    }
}
