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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;


/**
 * 
 * This class encapsulates checks whenever the Primary Writing Agent is added, deleted on the case, Financial Activity is reversed on the case, ReplacementType is changed, ModalPremAmt is changed.
 * If any of the above change occurs then the class calls ECS update web service.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>ALS4633</td><td>AXA Life Phase 1</td><td>Compensation Interface</td>
 * <tr><td>CR57907 Retrofit</td><td></td><td>Xpress Commission Transaction</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaDeclineExpressCommTransaction extends AxaUpdateExpressCommTransaction {
	protected NbaLogger logger = null;

	protected long[] changeTypes = { 
			// DC_MODAL_AMT_CHG, //CR57907 Retrofit
			DC_FIN_ACT_REVERSE,
			// DC_REPL_TYPE_CHG, //CR57907 Retrofit
			DC_PRIMARY_AGENT_DELETE,
//			DC_AGENT_ELIGIBLE_TO_INELIGIBLE //APSL3655
	};

	/*
	 * (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded() && !nbaTxLife.isSIApplication()) { // APSL2808
			if (hasChangeSubType(DC_AGENT_ELIGIBLE_TO_INELIGIBLE)) {
				AxaWSInvoker webServiceInvoker = null;
				List changeSubTypeList = createChangeSubTypeList();
				Map parametersMap = new HashMap();
				parametersMap.put("changeSubTypeList", changeSubTypeList);
				webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_ECSUPDATE, user, nbaTxLife, nbaDst,
						parametersMap); //SR494086.7 Retrofit
				webServiceInvoker.execute();
			}
		}
		return nbaDst;
	}

	/*
	 * (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#getDataChangeTypes()
	 */
	protected long[] getDataChangeTypes() {
		return changeTypes;
	}

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaDataChangeTransaction#isTransactionAlive()
	 */
	protected boolean isTransactionAlive() {
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.COMPENSATION_INTERFACE_CALL_SWITCH);
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
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
}
