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

import java.util.ArrayList;

import com.axa.fsg.nba.vo.AxaProducerVO;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.CriteriaExpression;
import com.csc.fsg.nba.vo.txlife.CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;

/**
 * This class is responsible for creating request for agent search webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.18</td><td>Version 7</td><td>Producer Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSAgentSearchInvoker extends AxaWSInvokerBase {

    private static final String CATEGORY = "AxaProducerInfo";

    private static final String FUNCTIONID = "SearchProducer";

    
    /**
     * @param userVO
     * @param nbaTXLife
     * @param nbaDst
     * @param object
     */
    public AxaWSAgentSearchInvoker(String operation,NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super(operation,userVO, nbaTXLife, nbaDst, object);
        setBackEnd(ADMIN_ID);
        setCategory(CATEGORY);
        setFunctionId(FUNCTIONID);
    }

    /* (non-Javadoc)
     * @see com.csc.fsg.nba.webservice.invoke.AxaWSInvokerBase#createRequest()
     */
    public NbaTXLife createRequest() throws NbaBaseException {
        AxaProducerVO producerVO = (AxaProducerVO) getObject();
        NbaTXLife nbaTXLife = createTXLifeRequest(producerVO.getTransType(),producerVO.getTransSubType(), getUserVO().getUserID());
        TXLifeRequest txLifeRequest = nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
        txLifeRequest.setChangeSubType(null);
        txLifeRequest.setTransMode( NbaOliConstants.TC_MODE_ORIGINAL);
        txLifeRequest.setInquiryLevel( NbaOliConstants.TC_INQLVL_OBJECTALL);
        CriteriaExpression criteriaExp = new CriteriaExpression();
        CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension cexp = new CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension();
        cexp.setCriteriaOperator(producerVO.getOperator());
        cexp.setCriteriaOrCriteriaExpression((ArrayList)producerVO.getCriteriaList());
        criteriaExp.setCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension(cexp);
        txLifeRequest.setCriteriaExpression(criteriaExp);
		
		OLifE olifE = new OLifE();
		olifE.setVersion("2.9.03");
		olifE.setSourceInfo(createSoureInfo());
		nbaTXLife.setOLifE(olifE);
		
        return nbaTXLife;
        
    }

    // NBLXA-2553 Override method for modifying webservice response for Charles Bailey Agent. Reverted from July 2019 release.
//    public void handleResponse() throws NbaBaseException {
//    	handleAgentWebserviceResponse();
//    }
}
