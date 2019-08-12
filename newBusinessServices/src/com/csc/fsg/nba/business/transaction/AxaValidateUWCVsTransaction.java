package com.csc.fsg.nba.business.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.business.transaction.AxaDataChangeTransaction;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkHTMLFormatter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.evaluate.GenerateEvaluateWorkItemBP;
import com.csc.fsg.nba.process.rules.RetrieveDataFromBusinessRulesBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaVpmsRequestVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vpms.CopyLobsTaskConstants;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

public class AxaValidateUWCVsTransaction extends AxaDataChangeTransaction implements NbaConstants {
	protected NbaLogger logger = null;

	protected static long[] changeTypes = { DC_UWAPPRVL_SEVERE_CV_RESOLVED };

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

	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		//NBLXA-2582 Start
		ApplicationInfo applicationInfo=null;
		if (nbaTxLife.getPolicy() != null) {
			 applicationInfo = nbaTxLife.getPolicy().getApplicationInfo();
		}
		if (isCallNeeded() && ! NbaConstants.PROC_A2RTODSN.equalsIgnoreCase(nbaTxLife.getBusinessProcess()) && (applicationInfo!=null && applicationInfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_NEW)) {
			changeWorkStatus(nbaTxLife, user, nbaDst);
		}
		//NBLXA-2582 End
		return nbaDst;
	}

	protected void changeWorkStatus(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst work) throws NbaBaseException {
		NbaLob lobs = work.getNbaLob();
		Map deOink = new HashMap();
		deOink.put("A_UndwrtQueueLOB", lobs.getUndwrtQueue());
		deOink.put(NbaVpmsConstants.A_PROCESS_ID, "A2DATACHG");
		NbaVpmsRequestVO vpmsRequestVO = new NbaVpmsRequestVO();
		vpmsRequestVO.setModelName(NbaVpmsConstants.AUTO_PROCESS_STATUS);
		vpmsRequestVO.setEntryPoint(NbaVpmsConstants.EP_WORKITEM_STATUSES);
		vpmsRequestVO.setNbaLob(lobs);
		vpmsRequestVO.setDeOinkMap(deOink);
		vpmsRequestVO.setNbATXLife(nbaTxLife);
		RetrieveDataFromBusinessRulesBP newReEvalWorkItem = new RetrieveDataFromBusinessRulesBP();
		Result result = newReEvalWorkItem.process(vpmsRequestVO);
		if (result.hasErrors()) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS);
		}
		work.setStatus(((NbaVpmsRequestVO) result.getFirst()).getPassStatus());
		work.getNbaLob().setRouteReason("Case moved to UW as all Prevent UW CVs resolved. Case ready for review");//NBLXA-2582
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#getDataChangeTypes()
	 */
	protected long[] getDataChangeTypes() {
		return changeTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaDataChangeTransaction#isTransactionAlive()
	 */
	protected boolean isTransactionAlive() {
		return true;
	}
}
