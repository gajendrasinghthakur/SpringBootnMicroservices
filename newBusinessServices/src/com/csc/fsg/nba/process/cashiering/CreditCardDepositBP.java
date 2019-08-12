package com.csc.fsg.nba.process.cashiering;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaCashieringTable;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.vo.NbaCreditCardAccountingExtractVO;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Create Deposit Ticket
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA182</td><td>Version 7</td><td>Cashiering Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class CreditCardDepositBP extends NewBusinessAccelBP {

    public static final BigDecimal ZERO = new BigDecimal("0");

    /**
     * Called to create the deposit ticket.
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            NbaUserVO userVO = (NbaUserVO)input;
            result.addResult(processCreditCardExtracts(userVO));

        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Generates accounting extracts for credit card deposits.
     * Extracts will be created for all credit card payments that have applied and
     * report indicators set to true in the NBA_CREDIT_CARD table.
     * @throws NbaBaseException, NbaDataAccessException
     */
    protected List processCreditCardExtracts(NbaUserVO userVO) throws NbaBaseException, NbaDataAccessException {

        NbaCashieringTable cashTable = new NbaCashieringTable();
        NbaCreditCardData[] extractData = cashTable.getCreditCardExtractData();
        ArrayList depositExtractsList = new ArrayList();
        ArrayList depositExtractVOlist = new ArrayList();

        if (extractData.length > 0) {
            for (int i = 0; i < extractData.length; i++) {
                NbaCreditCardAccountingExtractVO vo = new NbaCreditCardAccountingExtractVO();
                NbaCreditCardData ccData = extractData[i];
                vo.setCompanyCode(ccData.getCompany());
                vo.setCharge_Amt(ccData.getAmount());
                vo.setChargeDate(ccData.getChargeDate());
                vo.setContractNumber(ccData.getContractNumber());
                vo.setCreditCardNumber(ccData.getCreditCardNumber());
                vo.setDepositDate(ccData.getDepositDate());
                vo.setExtractCreate(new java.util.Date());
                vo.setExtractSent(new java.util.Date());
                vo.setTransactionID(ccData.getTransactionId());
                depositExtractVOlist.add(vo);
            }
            depositExtractsList.add(depositExtractVOlist);
            //createAccountingExtracts(session, depositExtractsList, false, false);  //NBA213
        }
        return depositExtractsList;
    }
}
