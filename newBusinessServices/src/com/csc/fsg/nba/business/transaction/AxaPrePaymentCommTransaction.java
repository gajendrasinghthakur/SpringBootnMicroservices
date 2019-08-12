/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

package com.csc.fsg.nba.business.transaction;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransResponseVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * 
 * This class encapsulates checks whenever all the system message having MsgRestrictCode=7 are resolved on the case.
 * If above change occurs then the class calls PAL LIFE 70 web service.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr>
 * <tr><td>P2AXAL007</td><td>AXA Life Phase 2</td><td>Producer and Compensation</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaPrePaymentCommTransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;

	protected long[] changeTypes = { DC_SYSMSG_REST_RESOLVED };

	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded() && !(nbaTxLife.isSIApplication()|| nbaTxLife.isInformalApplication())) { // APSL2808 ,APSL3067
			if (hasChangeSubType(DC_SYSMSG_REST_RESOLVED)) {
				NbaTransResponseVO transResponseVO = new NbaTransResponseVO();
				transResponseVO.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS);
				transResponseVO.setTransSubType(NbaOliConstants.TC_SUBTYPE_RTS_HOLDING_TRANSMIT);
				transResponseVO.setOperationName(AxaWSConstants.WS_OP_PAL);
				transResponseVO.setUserLoginName(user.getUserID());
				if (NbaUtils.isAxaWSCallNeeded(nbaTxLife, transResponseVO)) {
					AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_PAL, user, nbaTxLife, null, null).execute();
					nbaTxLife.addTransResponse(transResponseVO);
				}
			}
		}
		return nbaDst;
	}

	protected long[] getDataChangeTypes() {
		return changeTypes;
	}

	protected boolean isTransactionAlive() {
		return NbaUtils.isConfigCallEnabled(NbaUtils.PAL_INTERFACE_CALL_SWITCH);
	}

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
}
