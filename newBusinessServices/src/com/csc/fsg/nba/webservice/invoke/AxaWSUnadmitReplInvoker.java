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
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.FinancialActivityType;
import com.csc.fsg.nba.vo.configuration.FinancialActivityTypes;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;

/**
 * This class is responsible for creating request for unadmitted replacement webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.24</td>
 * <td>Version 7</td>
 * <td>Unadmitted Replacement</td>
 * </tr>
 * <tr><td>SR494086</td><td>Discretionary</td><td>ADC Retrofit</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSUnadmitReplInvoker extends AxaWSInvokerBase {

    private static final String CATEGORY = "AxaUnadRepl";

    private static final String FUNCTIONID = "AxaUnadRepl";

    /**
     * @param userVO
     * @param nbaTXLife
     * @param nbaDst
     * @param object
     */
    public AxaWSUnadmitReplInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super(operation, userVO, nbaTXLife, nbaDst, object);
        setBackEnd(ADMIN_ID);
        setCategory(CATEGORY);
        setFunctionId(FUNCTIONID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.csc.fsg.nba.webservice.invoke.AxaWSInvokerBase#createRequest()
     */
    /**
     * Create request for unadmitted replacement webservice
     */
    public NbaTXLife createRequest() throws NbaBaseException {
        String priorPolicyNumber = (String) getObject();
        NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
        nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_UNADMITTED_REPLACEMENT);
        nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_UNADMITTED_REPLACEMENT);
        nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
        nbaTXRequest.setNbaLob(getNbaDst().getNbaLob());
        nbaTXRequest.setNbaUser(getUserVO());
        NbaTXLife nbaTXLife213 = new NbaTXLife(nbaTXRequest);
        TXLife reqTXLife = nbaTXLife213.getTXLife();
        UserAuthRequestAndTXLifeRequest userReqandTXReq = reqTXLife.getUserAuthRequestAndTXLifeRequest();
        TXLifeRequest nbaTXLife213Request = userReqandTXReq.getTXLifeRequestAt(0);
        //code deleted ALS5661
        nbaTXLife213Request.setMaxRecords(MAX_RECORDS);
        Policy nbApolicy = getNbaTXLife().getPolicy();
        Date startDate = null;
        Date endDate = nbApolicy.getApplicationInfo().getSignedDate();//End date will be same as contract date.
        if (endDate != null) {
            startDate = NbaUtils.calcDayFotFutureDate(endDate, -4); //Calculate start date by deducting 4 months to the application date.
        }
        nbaTXLife213Request.setEndDate(new Date());
        nbaTXLife213Request.setStartDate(startDate);
        if (priorPolicyNumber != null) {
            Holding holding = nbaTXLife213Request.getOLifE().getHoldingAt(0);
            holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
            holding.setCarrierAdminSubSystem(NbaOliConstants.OLI_CARRIER_ADMIN_SUBSYSTEM);
            Policy policy = holding.getPolicy();
            policy.setPolNumber(priorPolicyNumber);
            policy.setCarrierCode(NbaConstants.AXA_CARRIER_CODE);
            policy.deleteProductCode();
            policy.deletePolicyStatus();
            FinancialActivity financialActivity = null;
            FinancialActivityType financialActivityType = null;
            FinancialActivityTypes financialActivityTypes = NbaConfiguration.getInstance().getFinancialActivityTypes();
            if (financialActivityTypes != null) {
                List financialActivityTypeList = financialActivityTypes.getFinancialActivityType();
                int size = financialActivityTypeList.size();
                for (int i = 0; i < size; i++) {
                    financialActivityType = (FinancialActivityType) financialActivityTypeList.get(i);
                    financialActivity = new FinancialActivity();
                    financialActivity.setId(FINANCIAL_ACTIVITY_ID + i + 1);
                    financialActivity.setFinActivityType(financialActivityType.getTc());
                    policy.addFinancialActivity(financialActivity);
                }
            }
        }
        return nbaTXLife213;
    }
    
	//ALS5536 overridden the method to stop Unadmitted Replacement call for informal Application.
	public boolean isCallNeeded() {
		if (getNbaTXLife().isInformalApplication() || NbaUtils.isAdcApplication(getNbaDst())) { //SR494086.6 ADC Retrofit
			return false;
		}
		return true;
	}

}
