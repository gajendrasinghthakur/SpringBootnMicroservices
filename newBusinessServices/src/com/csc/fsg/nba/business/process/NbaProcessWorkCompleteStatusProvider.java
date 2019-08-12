package com.csc.fsg.nba.business.process;
/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.ActivityExtension;
import com.csc.fsg.nba.vo.txlife.ContractChangeInfo;
import com.csc.fsg.nba.vo.txlife.ContractChangeOutcome;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/**
 * The NbaProcessWorkCompleteStatusProvider services by calling VPMS to retrieve the 
 * work complete status for a given process.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA136</td><td>Version 6</td><td>In Tray and Search Rewrite</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3611</td><td>Version 8</td><td>The Aggregate process is not setting LOBs on a Case correctly</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaProcessWorkCompleteStatusProvider {
	protected String status = null;
	protected String errorMsg = null;
	protected String priorityAction = null;
	protected String priorityValue = null;
	protected static NbaLogger logger = null;
	private boolean significantRequirement = false;  //NBA136
	protected String reason = null; //ALS5260
	/**
	 * NbaProcessWorkCompleteStatusProvider Constructor
	 * @param userVO the user value object
	 * @param lob the NbaLob object
	 * @param deOink the deOink HashMap. If should have non null value.
	 * @throws NbaBaseException
	 */
	public NbaProcessWorkCompleteStatusProvider(NbaUserVO userVO, NbaLob lob, Map deOink) throws NbaBaseException {
		initializeData(NbaUtils.getBusinessProcessId(userVO), lob, deOink, null);//NBA186
	}

	/**
	 * NbaProcessWorkCompleteStatusProvider Constructor
	 * @param lob the NbaLob object
	 * @param deOink the deOink HashMap. If should have non null value.
	 * @throws NbaBaseException
	 */
	//NBA186 - added new parameter nbaTXLife object
	public NbaProcessWorkCompleteStatusProvider(NbaLob lob, Map deOink, NbaTXLife nbaTXLife) throws NbaBaseException {
		//if no business function, assume '*'
		initializeData("*", lob, deOink, nbaTXLife);//NBA186
	}
	
	//New Method APSL2735
	/**
	 * NbaProcessWorkCompleteStatusProvider Constructor
	 * @param oinkRequest the NbaOinkRequest object
	 * @param lob the NbaLob object
	 * @param deOink the deOink HashMap. If should have non null value.
	 * @param nbaTXLife the NbaTXLife object
	 * @throws NbaBaseException
	 */
	public NbaProcessWorkCompleteStatusProvider(NbaOinkRequest oinkRequest, NbaLob lob, Map deOink, NbaTXLife nbaTXLife) throws NbaBaseException {
		//if no business function, assume '*'
		initializeData(oinkRequest, "*", lob, deOink, nbaTXLife);//NBA186
	}
	
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaProcessWorkCompleteStatusProvider.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaWorkCompleteStatusProvider could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	//New Method APSL2735
	/**
	 * Calls <code>AUTO_PROCESS_STATUS</code> VPMS model to get work complete data. Parse the vpms result and
	 * update class properties.
	 * @param oinkRequest the NbaOinkRequest
	 * @param busFunc the business function name
	 * @param lob the NbaLob object
	 * @param deOink the deOink Map. If should have non null value.
	 * @param nbaTXLife an NbaTXLife object
	 * @throws NbaBaseException
	 */
	protected void initializeData(NbaOinkRequest oinkRequest, String busFunc, NbaLob lob, Map deOink, NbaTXLife nbaTXLife) throws NbaBaseException {
		NbaOinkDataAccess oink = new NbaOinkDataAccess();
		oink.setLobSource(lob);
		if(nbaTXLife != null) {
		    oink.setContractSource(nbaTXLife);
		}
		deOink.put(NbaVpmsConstants.A_PROCESS_ID, busFunc);
		deOink.put(NbaVpmsConstants.A_DELIMITER, NbaVpmsConstants.VPMS_DELIMITER[0]);
		/*APSL5365*/
		if (nbaTXLife != null) {
			ContractChangeInfo activeCCInfo = NbaUtils.getActiveContractChangeInfo(nbaTXLife);
			if (!NbaUtils.isBlankOrNull(activeCCInfo)) {
				deOink.put("A_ContractChangeActive", "1");
				if(NbaUtils.isUnapproveActivityforActiveContractChange(nbaTXLife)){//APSL5376
					deOink.put("A_ContractChangeApproved", "1");
				}
				//BEGIN APSL5382
				if (NbaConstants.A_WT_APPLICATION.equalsIgnoreCase(lob.getWorkType())) {
					updateOutcomeReturnToInitiator(nbaTXLife, deOink);
				}
				//END APSL5382
			}
			/*BEGIN: APSL5370*/
			List<Activity> amicaActivityList = new ArrayList<Activity>();
			amicaActivityList = NbaUtils.getActivityByTypeCode(nbaTXLife.getOLifE().getActivity(), NbaOliConstants.OLI_ACTTYPE_AMICACONTRACTCHANGE);
			if (NbaConstants.A_WT_APPLICATION.equalsIgnoreCase(lob.getWorkType())) { //APSL5410
				for(Activity amicaActivity : amicaActivityList) {
					ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(amicaActivity);
					if (!NbaUtils.isBlankOrNull(activityExtn) &&
							NbaUtils.isBlankOrNull(activityExtn.getRelatedObjectId())) {
						deOink.put("A_DisposeContractChange", "1");
					}
				}
			}
			/*END: APSL5370*/
		}		
		/*APSL5365*/
		NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oink, NbaVpmsConstants.AUTO_PROCESS_STATUS);
		vpmsProxy.setSkipAttributesMap(deOink);
		vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_WORK_COMPLETED);
		if(oinkRequest != null){
			vpmsProxy.setANbaOinkRequest(oinkRequest);
		}
		try {
			VpmsComputeResult result = vpmsProxy.getResults();
			//If a bad code is returned then throw an exception 
			if (result.getReturnCode() != 0 && result.getReturnCode() != 1) {
				throw new NbaVpmsException(result.getMessage());
			}

			parseResult(result);
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException(NbaBaseException.VPMS_GENERIC, re);
		} finally {
			try {
			    if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
			} catch (Exception e) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //Ignoring the exception SPR3362
			}
		}
	}
	
	/**
	 * Calls <code>AUTO_PROCESS_STATUS</code> VPMS model to get work complete data. Parse the vpms result and
	 * update class properties.
	 * @param busFunc the business function name
	 * @param lob the NbaLob object
	 * @param deOink the deOink Map. If should have non null value.
	 * @param nbaTXLife an NbaTXLife object
	 * @throws NbaBaseException
	 */
	//NBA186 added new parameter nbaTXLife,APLS2735 - Modified Code
	protected void initializeData(String busFunc, NbaLob lob, Map deOink, NbaTXLife nbaTXLife) throws NbaBaseException {
		initializeData(null, busFunc, lob, deOink, nbaTXLife);//NBA186
	}
	
	/**
	 * Calls <code>AUTO_PROCESS_STATUS</code> VPMS model to get work complete data. Parse the vpms result and
	 * update class properties.
	 * @param busFunc the business function name
	 * @param lob the NbaLob object
	 * @param deOink the deOink HashMap. If should have non null value.
	 * @throws NbaBaseException
	 */
	protected void initializeData(String busFunc, NbaLob lob, Map deOink) throws NbaBaseException {
		// SPR3290 code deleted
		NbaOinkDataAccess oink = new NbaOinkDataAccess();

		oink.setLobSource(lob);
		deOink.put(NbaVpmsConstants.A_PROCESS_ID, busFunc);
		deOink.put(NbaVpmsConstants.A_DELIMITER, NbaVpmsConstants.VPMS_DELIMITER[0]);
		NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oink, NbaVpmsConstants.AUTO_PROCESS_STATUS);
		vpmsProxy.setSkipAttributesMap(deOink);
		vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_WORK_COMPLETED);
		try {
			VpmsComputeResult result = vpmsProxy.getResults();
			//If a bad code is returned then throw an exception 
			if (result.getReturnCode() != 0 && result.getReturnCode() != 1) {
				throw new NbaVpmsException(result.getMessage());
			}

			parseResult(result);
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException(NbaBaseException.VPMS_GENERIC, re);
		} finally {
			try {
			    if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
			    //SPR3362 code deleted
			} catch (Exception e) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //Ignoring the exception SPR3362
			}
		}
	}

	/**
	 * Parse the result from VPMS and set class properties.
	 * @param vpmsResult the result from VPMS
	 * @throws NbaBaseException if result is null or empty or error message is returned by VPMS.
	 */
	protected void parseResult(VpmsComputeResult vpmsResult) throws NbaBaseException {
		String result = vpmsResult.getResult();
		if (null != result && result.trim().length() > 0) {
			String[] tokens = result.split(NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			switch (tokens.length) {
			//break is not used so it will call all cases below that satisfy switch condition.
			//SPR3611 code deleted
			    //begin ALS5260
				case 5 ://length 5 means set all 
				    if(!NbaVpmsConstants.IGNORE.equals(tokens[4])){ 
				        setReason(tokens[4]); //Fifth contains the reason value 
				    }
				//end ALS5260    
				case 4 : 
					setPriorityValue(tokens[3]); //Fourth contains the priority value 
				case 3 :
					setPriorityAction(tokens[2]); //Third token contains the priority Action
				case 2 :
					//Second token it contains the error message.It contains an error message only when the  status is "VPMSERRD".
					if (NbaConstants.A_STATUS_VPMS_ERROR.equals(tokens[0])) {
						if (tokens[1].trim().length() > 0) {
							//This token contains the error message, if first token is VPMSERRD 
							throw new NbaVpmsException(tokens[1]);	//SPR3611
						}
					}
					setErrorMsg(tokens[1]);
				case 1 :
					setStatus(tokens[0]); //First Token contains the Status returned from the VPMS model.
			}
		} else { //If VPMS returns a null then return an error message.
			throw new NbaVpmsException("Could not get the outgoing status");		//SPR3611
		}
	}

	//APSL5382: New Method
	protected void updateOutcomeReturnToInitiator(NbaTXLife txLife, Map deOink) {
		ContractChangeInfo contractChangeInfo = NbaUtils.getActiveContractChangeInfo(txLife);
		if (!NbaUtils.isBlankOrNull(contractChangeInfo)) {
			List<ContractChangeOutcome> contractChangeOutcomeList = contractChangeInfo.getContractChangeOutcome();
			for (ContractChangeOutcome outcome : contractChangeOutcomeList) {
				if (NbaOliConstants.OLIEXT_LU_CONTRACTCHNGOUTTYPE_RETURNTOINIT == outcome.getOutcomeType() &&
						outcome.getOutcomeProcessed() == false) {
					List<Activity> activityList = new ArrayList<Activity>();
					activityList = NbaUtils.getActivityByTypeCodeAndRelatedObjId(txLife.getOLifE().getActivity(), NbaOliConstants.OLI_ACTTYPE_CONTRACTCHANGE,contractChangeInfo.getId());
					boolean isInitiatorReviewInitiated = false;
					for(Activity activity : activityList) {
						if (NbaOliConstants.OLIEXT_LU_CONTRACTCHNG_ACTKEY_BACKTOINIT == Long.parseLong(activity.getActivityKey())) {
							isInitiatorReviewInitiated = true;
							break;
						}
					}
					if (isInitiatorReviewInitiated) {
						deOink.put("A_ReturnToInitiator", "true");								
					}
				}
			}
		}
	}
	
	/**
	 * Returns error message
	 * @return the error message
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * Returns priority action indicator
	 * @return the priority action indicator
	 */
	public String getPriorityAction() {
		return priorityAction;
	}

	/**
	 * Returns workitem status
	 * @return the workitem status 
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets error message
	 * @param string the error message
	 */
	public void setErrorMsg(String string) {
		errorMsg = string;
	}

	/**
	 * Sets priority action indicator
	 * @param string the priority action indicator
	 */
	public void setPriorityAction(String string) {
		priorityAction = string;
	}

	/**
	 * Sets workitem status
	 * @param string the workitem status
	 */
	public void setStatus(String string) {
		status = string;
	}
	/**
	 * Returns priority value
	 * @return the priority value
	 */
	public String getPriorityValue() {
		return priorityValue;
	}

	/**
	 * Sets priority value
	 * @param string the priority value
	 */
	public void setPriorityValue(String string) {
		priorityValue = string;
	}

	//	SPR3611 code deleted}
	/**
	 * @return Returns the reason.
	 */
	//ALS5260 New Method
	public String getReason() {
		return reason;
	}
	/**
	 * @param reason The reason to set.
	 */
	//ALS5260 New Method
	public void setReason(String reason) {
		this.reason = reason;
	}
}