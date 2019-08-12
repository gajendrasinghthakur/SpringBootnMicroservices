package com.csc.fsg.nba.process.transaction;

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
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.business.transaction.NbaClientSearchTransaction;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NewBusiness301ClientSearchRequest;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;

/**
 * Class Description.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class Nba301ClientSearchBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			if(input instanceof NewBusiness301ClientSearchRequest){
				NewBusiness301ClientSearchRequest req = (NewBusiness301ClientSearchRequest)input;
				
				
				NbaClientSearchTransaction clientSearch = new NbaClientSearchTransaction();
				NbaTXLife xmlTransaction = clientSearch.createTXLife301(req.cs_Params, req.nbaDst, req.userVO);
				
				if (LogHandler.Factory.isLogging(LogHandler.LOG_DEBUG)) {
					LogHandler.Factory.LogDebug(this, "Outgoing 301 tranx " + xmlTransaction.toXmlString());
				}
				
				String backEndSystem = "";
				if (req.nbaDst.getNbaLob().getBackendSystem() != null) {
					backEndSystem = req.nbaDst.getNbaLob().getBackendSystem();
				} else {
					backEndSystem = "CLIF";
				}
				
				NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(backEndSystem,"Client","ClntSearch");
				
				NbaTXLife txLifeClientSearchResp = 
					txLifeClientSearchResp = service.invokeWebService(xmlTransaction); // SPR2968
				
				result.addResult(txLifeClientSearchResp);
			}
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
		}
		return result;
	}
}
