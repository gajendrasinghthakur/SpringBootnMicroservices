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


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import com.csc.fs.Result;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaSessionUtils;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.evaluate.GenerateEvaluateWorkItemBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaEvaluateRequest;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.process.evaluate.GenerateEvaluateWorkItemBP;
import com.csc.fs.Result;
/**
 * 
 * This class encapsulates checks whenever following changes are made to the Insured or Owner roles on the policy. - Name. - Address. - Tax
 * Identification. - Tax Identification Type. - Gender/Sex. - Date of Birth and following changes are made on a contract - Policy Status - Plan Change -
 * Agent information
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Auto Underwriting, Data Change Architecture</td>
 * <td>SR494086.5</td><td>Discretionary</td><td>ADC Retrofit</td>
 * <tr><td>P2AXAL053</td><td>AXA Life Phase 2</td><td>R2 Auto Underwriting</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaUpdateReEvalTransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;
	
	
	protected static	long[] changeTypes = { 	DC_FACE_AMT,
												DC_APP_STATE,
												DC_SIGNEDDATE,
												DC_PRODUCTCODE,
												DC_INSURED_DOB, 
												DC_INSURED_GENDER, 
												DC_RIDER_ADDED,
												DC_RIDER_AMT,
												DC_INSURED_FIRSTNAME,
												DC_INSURED_LASTNAME,
												DC_INSURED_MIDDLENAME,
												DC_IMPAIRENT_PERM_AMT_UPDATED,
												DC_IMPAIRENT_TEMP_AMT_UPDATED,
												DC_IMPAIRENT_CREDIT_UPDATED,
												DC_IMPAIRENT_DEBIT_UPDATED,
												DC_IMPAIRENT_DURATION_UPDATED,
												DC_JNT_INSURED_GENDER,
												DC_JNT_INSURED_DOB,
												DC_JNT_INSURED_FIRSTNAME,
												DC_JNT_INSURED_LASTNAME,
												DC_JNT_INSURED_MIDDLENAME,
												DC_RIDER_LTC_ADDED, //APSL4697
												DC_RIDER_LTC_DELETED
											};//ALS3963 //ALS2611 //P2AXAL053
	

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
	
	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		//	Create NBREEVAL work item for contract to run UWRISK, CTEVAL, REQDET, AND AUUND for contract.
		if (isCallNeeded() && nbaDst.isCase() && isCallNeeded(nbaDst,nbaTxLife)) { //SR494086.5 ADC Retrofit  ,APSL3359
			NbaEvaluateRequest req = new NbaEvaluateRequest();
			req.setNbaUserVO(user);
			req.setWork(nbaDst);
			req.setContract(nbaTxLife);
			req.setUserFunction("NBEVAL");
			req.setOverrideContractCommit(true);//AXAL3.7.07
			req.setUnderwritingWB(isImpairementUpdated());//ALS3972
			req.setResetUWWB(true);//ALS3972
			GenerateEvaluateWorkItemBP newReEvalWorkItem = new GenerateEvaluateWorkItemBP();
			Result result = newReEvalWorkItem.process(req);
			if (result.hasErrors()) {
				getLogger().logError("Error creating NBREEVAL work item for contract " + nbaTxLife.getPolicy().getPolNumber() );
			}
		}
		return nbaDst;
	}
	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#getDataChangeTypes()
	 */
	protected long[] getDataChangeTypes() {
		return changeTypes; 
		
	}


	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaDataChangeTransaction#isTransactionAlive()
	 */
	protected boolean isTransactionAlive() {
		// TODO Auto-generated method stub
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ENABLE_DATA_CHANGE_REEVAL_CALL);
	}

	//ALS3972 new method
	protected boolean isImpairementUpdated() {
		return hasChangeSubType(DC_IMPAIRENT_PERM_AMT_UPDATED) || hasChangeSubType(DC_IMPAIRENT_TEMP_AMT_UPDATED)
				|| hasChangeSubType(DC_IMPAIRENT_CREDIT_UPDATED) || hasChangeSubType(DC_IMPAIRENT_DEBIT_UPDATED)
				|| hasChangeSubType(DC_IMPAIRENT_DURATION_UPDATED);
	}	

	//SR494086.5 New Method ADC Retrofit	

	protected boolean isCallNeeded(NbaDst nbaDst, NbaTXLife nbaTxLife) throws NbaBaseException{
		if (nbaDst.isCase() && !NbaUtils.isAdcApplication(nbaDst) && !NbaUtils.isSIApplication(nbaDst) &&  !isCaseReEvaluationNeeded(nbaTxLife)) { // APSL2808,ALII1830,APSL3359
			return true;
		}
		return false;
	}
	
	//New Method - ALII1830
	protected boolean isCaseReEvaluationNeeded(NbaTXLife nbaTxLife) throws NbaBaseException {   //APSL3359
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) { //ALII1865
			ExternalContext extContext = context.getExternalContext();
			NbaDst nbaDst = NbaSessionUtils.getCase((HttpSession) extContext.getSession(false));
			NbaVpmsAdaptor proxy = null;
			Map deOink = new HashMap();
			deOink.put("A_QUEUE", nbaDst.getQueue());
			try {
				NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaTxLife);      //APSL3359
				proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.AUTO_PROCESS_STATUS);
				proxy.setSkipAttributesMap(deOink);
				proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_REEVALNEEDED);
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
				if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
					return NbaConstants.TRUE == Integer.parseInt((String) vpmsResultsData.getResultsData().get(0));
				}
			} catch (RemoteException t) {
				throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
			} catch (NbaBaseException e) {
				throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
			} finally {
				if (proxy != null) {
					try {
						proxy.remove();
					} catch (RemoteException re) {
						NbaLogFactory.getLogger("AxaUpdateReEvalTransaction").logError(re);
					}
				}
			}
		}
		return false;
	}
}
