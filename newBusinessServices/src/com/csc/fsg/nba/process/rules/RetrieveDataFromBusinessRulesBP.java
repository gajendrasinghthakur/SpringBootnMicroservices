package com.csc.fsg.nba.process.rules;

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

import java.util.HashMap;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.business.process.evaluation.NbaACUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaVpmsRequestVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;

/**
 * This business process class is responsible for retrieving data from business rules
 * required for routing a case to next queue.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class RetrieveDataFromBusinessRulesBP extends AccelBP {
	protected static NbaLogger logger = null;

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaVpmsRequestVO requestVO = (NbaVpmsRequestVO) input;
			VpmsComputeResult data = getReferDataFromBusinessRules(requestVO);
			requestVO = retrieveCaseData(data, requestVO);
			result.addResult(requestVO);
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}


    /**
	 * The method makes a call to the passed entry point of the VP/MS model passed as parameter and retruns the VP/MS results.
	 * @param requestVO NbaVpmsRequestVO object containing following: the VP/MS model name, the entry point, Contract's holding
	 * 					inquiry, an instance of	NbaLob, and deOink HashMap
	 * @return VpmsComputeResult result from VP/MS model
	 * @throws NbaBaseException
	 */
    protected VpmsComputeResult getReferDataFromBusinessRules(NbaVpmsRequestVO requestVO) throws NbaBaseException {
		NbaVpmsAdaptor vpmsAdaptor = null;
		String legacyCosignMode="true"; // NBLXA-2085
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
			if (requestVO.getDeOinkMap() == null) {
				requestVO.setDeOinkMap(new HashMap());
			}
			if (requestVO.getNbATXLife() != null) {
				oinkData.setContractSource(requestVO.getNbATXLife());
			}
			if (requestVO.getNbaLob() != null) {
				oinkData.setLobSource(requestVO.getNbaLob());
			}

			// Begin NBLXA-2085
			NbaACUtils.deoinkApproveWithRatingValues(requestVO.getDeOinkMap(), oinkData);
			requestVO.getDeOinkMap().put("A_LegacyCosignMode", legacyCosignMode);
			// End NBLXA-2085

			vpmsAdaptor = new NbaVpmsAdaptor(oinkData, requestVO.getModelName());
			vpmsAdaptor.setVpmsEntryPoint(requestVO.getEntryPoint());
			vpmsAdaptor.setSkipAttributesMap(requestVO.getDeOinkMap());
			return vpmsAdaptor.getResults();
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("Problem retrieving data from " + requestVO.getModelName() + " VP/MS Model", re);
		} finally {
			try {
				if (vpmsAdaptor != null) {
					vpmsAdaptor.remove();
				}
			} catch (Throwable th) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}

	}

    /**
	 * Retrieves the passStatus, action, priority, rule, and level for the case from VP/MS model results
	 * @param data VpmsComputeResult object
	 * @param requestVO NbaVpmsRequestVO object
	 * @return requestVO case data retrieved from model result
	 * @throws NbaBaseException
	 */
    protected NbaVpmsRequestVO retrieveCaseData(VpmsComputeResult data, NbaVpmsRequestVO requestVO) throws NbaBaseException {
		NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(data);
		requestVO.setPassStatus(statusProvider.getPassStatus());
		requestVO.setCaseAction(statusProvider.getCaseAction());
		requestVO.setCasePriority(statusProvider.getCasePriority());
		requestVO.setRule(statusProvider.getRule());
		requestVO.setLevel(statusProvider.getLevel());
		requestVO.setReason(statusProvider.getReason());//ALS5260
		requestVO.setOtherStatus(statusProvider.getOtherStatus()); //ALS5740
		return requestVO;
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

}
