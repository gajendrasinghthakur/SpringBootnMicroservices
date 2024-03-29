package com.csc.fsg.nba.process.extract;

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
import com.csc.fsg.nba.bean.accessors.NbaExtractAccessFacadeBean;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NewBusinessCreateAccountingExtractsRequest;

/**
 * Class Description.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>AXAL3.7.23</td><td>AXA Life phase 1</td><td>Accounting Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class CreateAccountingExtractsBP extends NewBusinessAccelBP implements NbaConstants {
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
        	if(input instanceof NewBusinessCreateAccountingExtractsRequest){
        		NewBusinessCreateAccountingExtractsRequest req = (NewBusinessCreateAccountingExtractsRequest)input;
        		NbaExtractAccessFacadeBean pif = new NbaExtractAccessFacadeBean();
    			if(req.checkPayment){
    				pif.axaCreateAccountingExtractForCheckDeposit(req.paymentsForExtract, req.user, req.correctedDeposit);  //AXAL3.7.23
    			}else {
    				pif.createAccountingExtractForCreditCardDeposit(req.paymentsForExtract);
    			}		        	}
        }catch (NbaBaseException e) {
            addExceptionMessage(result, e);
        }
        catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }
    
}
