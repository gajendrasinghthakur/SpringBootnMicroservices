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

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.process.NbaAutoProcessProxy;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * The Business Process class responsible for retrieving dst object from database
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA163</td><td>Version 6</td><td>Case History Rewrite</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class RetrieveTXLifeBP extends NewBusinessAccelBP {
    protected NbaLogger logger = null;

    /* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    public Result process(Object input) {
        NbaDst dst = null;
        NbaTXLife nbaTXLife = null;
        AccelResult result = new AccelResult();
        try {
            dst = (NbaDst) input;
            nbaTXLife = doHoldingInquiry(dst.getNbaUserVO(), dst);
        } catch (Exception e) {
            getLogger().logException(e);
            addExceptionMessage(result, e);
            return result;
        }
        result.addResult(nbaTXLife);
        return result;
    }

    /**
     * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * @return com.csc.fsg.nba.foundation.NbaLogger
     */
    protected NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(this.getClass());
            } catch (Exception e) {
                NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }

    /**
     * Create a holding inquiry from a back end system.
     * @param user the AWD user
     * @param aCase the case to be inquired upon
     * @return the <code>NbaTXLife</code> value object that is the request
     * @throws NbaBaseException
     */
    public static NbaTXLife doHoldingInquiry(NbaUserVO user, NbaDst aCase) throws NbaBaseException {
    	NbaAutoProcessProxy apProxy = new NbaAutoProcessProxy(user, aCase, false);
        NbaTXLife holdingInq = apProxy.doHoldingInquiry();
        UserAuthResponseAndTXLifeResponseAndTXLifeNotify allResponses = holdingInq.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
        List responses = allResponses.getTXLifeResponse();
        TXLifeResponse theResponse = (TXLifeResponse) responses.get(0);
        TransResult aTransResult = theResponse.getTransResult();
        long resultCode = aTransResult.getResultCode();
        if (resultCode > 1) {
            String error = (aTransResult.getResultInfoAt(0) != null) ? aTransResult.getResultInfoAt(0).getResultInfoDesc()
                    : "Back end processing failed";
            throw new NbaBaseException(error);
        }
        aCase.updateLobFromNbaTxLife(holdingInq);
        return holdingInq;
    }
}
