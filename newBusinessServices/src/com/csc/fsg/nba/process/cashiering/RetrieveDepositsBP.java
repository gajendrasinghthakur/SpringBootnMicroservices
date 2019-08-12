package com.csc.fsg.nba.process.cashiering;

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

import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.configuration.Cashiering;

/**
 * Retrieve deposits tickets
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

public class RetrieveDepositsBP extends NewBusinessAccelBP {

    /**
     * Called to retrieve the list of Deposit Tickets .
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            List deposits = new ArrayList();
            deposits = retrieveDepositTickets();
            result.addResult(deposits);

        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Retrieve Deposit tickets from cashiering database based on input parameters or default values if input is not provided.
     * @param depositDate
     * @param bundleID
     * @param message
     * @return
     * @throws NbaBaseException
     */
    protected List retrieveDepositTickets() throws NbaBaseException {

        Cashiering config = NbaConfiguration.getInstance().getCashiering();
        int days = config.getDepositCorrectionDays();
        NbaCashieringTable cashTable = new NbaCashieringTable();
        List depositTickets = cashTable.getDepositTicketData(days);

        return depositTickets;
    }

}
