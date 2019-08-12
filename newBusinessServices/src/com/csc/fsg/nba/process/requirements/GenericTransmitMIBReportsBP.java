package com.csc.fsg.nba.process.requirements;

import com.csc.fs.Result;
import com.csc.fs.UserSessionKey;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.accel.process.NbaAutoProcessAccelBP;
import com.csc.fsg.nba.bean.accessors.NbaUnderwriterWorkbenchFacadeBean;
import com.csc.fsg.nba.business.transaction.NbaMIBReportUtils;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUnderwriterWorkbenchVO;
import com.csc.fsg.nba.vo.NbaUserVO;

public class GenericTransmitMIBReportsBP extends NbaAutoProcessAccelBP {
	protected NbaLogger logger = null;

	/*
	 * Calls UnderwriterWorkBenchFacadeBean to transmit MIB Reports
	 * 
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		NbaUnderwriterWorkbenchVO uwVO = (NbaUnderwriterWorkbenchVO) input;
		try {
			NbaUnderwriterWorkbenchFacadeBean uwFacade = new NbaUnderwriterWorkbenchFacadeBean(); // NBA213
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			NbaUserVO userVo = uwVO.getNbaUserVO();
			String dstId = uwVO.getNbaDst().getID();
			NbaDst nbDst = retrieveWorkItem(userVo, retOpt, dstId);
			NbaTXLife txLife = uwVO.getNbATXLife();
			NbaMIBReportUtils mibUtils = new NbaMIBReportUtils(txLife, userVo);
			mibUtils.setNbaDstWithAllTransactions(nbDst);
			mibUtils.processMIBReportsForAContract(nbDst, true);
		} catch (Exception e) {
			getLogger().logException(e);
			addExceptionMessage(result, e);
			result.setErrors(true);
			return result;
		}
		result.addResult(uwVO);
		return result;
	}

	// NBA213 deleted code
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

	protected NbaDst retrieveWorkItem(NbaUserVO nbaUserVO, NbaAwdRetrieveOptionsVO retOpt, String DstId) throws NbaBaseException {
		retOpt.setNbaUserVO(nbaUserVO);
		retOpt.setWorkItem(DstId, true);
		retOpt.requestTransactionAsChild();
		AccelResult accelResult = (AccelResult) callBusinessService("NbaRetrieveWorkBP", retOpt);
		NewBusinessAccelBP.processResult(accelResult);
		return (NbaDst) accelResult.getFirst();
	}

}
