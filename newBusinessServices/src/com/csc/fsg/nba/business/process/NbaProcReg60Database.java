package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;


/**
 * NbaProcReg60Database is the class to process Cases found in N2RG60DB queue. It:
 * - for non pre sale case, set the status of case to Pass status and end processing.
 * - for pre sale case, invoke Reg60 DB interface.
 * - If interface call is successful, set status to Pass else to Fail status.
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL039</td><td>AXA Life Phase 2</td><td>Reg60 Datatbase Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 1
 */
public class NbaProcReg60Database extends com.csc.fsg.nba.business.process.NbaAutomatedProcess {

	protected NbaDst parentCase = null;

	/**
	 * NbaProcReg60Database constructor comment.
	 */
	public NbaProcReg60Database() {
		super();
	}

	/**
	 * @param user the user for whom the work was retrieved.
	 * @param work the AWD case to be processed
	 * @return NbaAutomatedProcessResult the results of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		if (!initialize(user, work)) {
			return getResult();
		}
		if (getResult() == null) {
			doProcess();
		}
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		if (getResult() != null) {
			changeStatus(getResult().getStatus()); //ALII953
			// Update the Work Item with it's new status and update the work item in AWD
			doUpdateWorkItem();
		}
		if (null != parentCase) {
			unlockWork(parentCase);
		}
		// Return the result
		return getResult();
	}
	
	/**
	 * Reg60 Auto Process Logic, Call Reg60DB Websrvice and based on service result set next status.
	 * @throws NbaBaseException
	 */
	public void doProcess() {
		
		//1. If ApplicationInfo.AppType is equal to 1000500005 (pre-sale), continue processing.
		ApplicationInfo appInfo = getNbaTxLife().getPolicy().getApplicationInfo();
		if(appInfo != null && appInfo.getApplicationType()==NbaOliConstants.OLI_APPTYPE_PRESALE){
			//2. If the inbound status is REG60RETRY, AND Holding.Policy.ApplicationInfo.ApplicationInfoExtension.Reg60PSDecision is equal to 2 (authorized), continue processing.
			boolean isStatReg60Retry = false;
			boolean isAuthPSDecision = false;
			NbaLob nbaLob = getWork().getNbaLob();
			if(nbaLob != null){
				String workStatus = nbaLob.getStatus();
				if(workStatus != null && (NbaConstants.A_STATUS_REG60RETRY.equalsIgnoreCase(workStatus) || NbaConstants.A_STATUS_RPLRVWD.equalsIgnoreCase(workStatus) 
						|| NbaConstants.A_STATUS_REQRVWD.equalsIgnoreCase(workStatus) || NbaConstants.A_STATUS_REPLRVWD.equalsIgnoreCase(workStatus) )){ //ALII1372
					isStatReg60Retry = true;
				}
			}
			ApplicationInfoExtension appInfoExt = NbaUtils.getAppInfoExtension(appInfo);
			if(appInfoExt != null && appInfoExt.getReg60PSDecision()==NbaOliConstants.NBA_REG60SPDECISION_AUTH){
				isAuthPSDecision = true;
			}
			if(isStatReg60Retry && isAuthPSDecision){
				invokeReg60Webservice();
			}
		}
    }
	
	/**
	 * To invoke Reg60 Database Webservice. If Failure comes, set Auto process status as Failed.
	 *
	 */
	public void invokeReg60Webservice() {
		try {
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_REG60DB_TRANSMIT, getUser(),
					getNbaTxLife(), null, null);
			webServiceInvoker.execute();
		} catch (NbaBaseException e) {
			getLogger().logException(e);
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Reg60 Database Interface call Failed.", getFailStatus()));
		}
	}

}