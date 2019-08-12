package com.csc.fsg.nba.business.process.evaluation;
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
 * 
 * *******************************************************************************<BR>
 */
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;
/**
 * Class that will do the processing once PredictiveImpairments model is invoked 
 * from NBCTEVAL or NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SR564247</td><td></td><td>Predictive Full Implementation</td></tr>
 * * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 
 * @since New Business Accelerator - Version 4
 */
public class NbaPredictiveImpairmentProcessor extends NbaVpmsModelProcessor {

	protected ArrayList predictiveImpairments = new ArrayList();

	protected ArrayList accepImpairments = null;

	/**
	 * Overridden method, calls the model and * updates the contract with impairments.
	 * 
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		if (nbaTxLife.isSIApplication()) { //APSL2808
			return;
		}
		impSrc = NbaConstants.PREDICTIVEIMPAIRMENTS_SRC;
		if (performingRequirementsEvaluation() || performingPredictiveHoldProcessing()) {
			setPartyID(work);
			if (partyID == null) {
				List parties = NbaUtils.getPartyIds(nbaTxLife.getOLifE(), NbaOliConstants.OLI_REL_INSURED);
				partyID = (String) parties.get(0);
			}
			isSuccess = callPredictiveImpairment();
			if (!isSuccess) {
				throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.PREDICTIVE_IMPAIRMENTS);
			}
			//Do the Impairments Merging
			mergeImpairmentsAndAccep(predictiveImpairments, accepImpairments);
		}
	}

	/**
	 * This function is used to call the PredictiveImpairments model
	 * 
	 * @param
	 * @return boolean : Returns true if the call is successful Else returns false
	 * @throws NbaBaseException
	 */
	
	public boolean callPredictiveImpairment() throws NbaBaseException {
		boolean success = false;
		String xmlString = "";
		VpmsModelResult vpmsModelResult = null;
		NbaVpmsModelResult nbaVpmsModelResult = null;
		NbaVpmsAdaptor vpmsProxy = null;
		ArrayList reqInfoList = new ArrayList();
		RequirementInfo reqInfo = null;
		RequirementInfoExtension reqInfoExtn = null;
		//int requirementScore = 0; commented for APSL4502
		String[] scoreList = null; // APSL4502
		long pprReqstatus=0L; //APSL4502
		try {
			Policy policy = null;
			Map deOink = new HashMap();
			if (txLifeReqResult != null) {
				policy = txLifeReqResult.getPolicy();
			}

			if (policy != null) {
				reqInfoList = policy.getRequirementInfo();
				for (int i = 0; i < reqInfoList.size(); i++) {
					reqInfo = (RequirementInfo) reqInfoList.get(i);
					if (reqInfo != null && reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_PPR) { // APSL4502
						reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
						// APSL4502 BEGIN
						if (reqInfoExtn != null) {
							// requirementScore = reqInfoExtn.getRequirementScore(); // commented for APSL4502
							pprReqstatus = reqInfo.getReqStatus();
							NbaOinkDataAccess accessContract = new NbaOinkDataAccess(txLifeReqResult);
							NbaOinkRequest oinkRequest = new NbaOinkRequest();
							oinkRequest.setVariable("HighestSingleScore");
							scoreList = accessContract.getStringValuesFor(oinkRequest);
							if (scoreList.length > 0 && !NbaUtils.isEmpty(scoreList[0])) {
								deOink.put("A_HighestSingleScore", convertToDefault(scoreList[0]));
							}
							oinkRequest.setVariable("OverallRiskScore");
							accessContract.getStringValuesFor(oinkRequest);
							scoreList = accessContract.getStringValuesFor(oinkRequest);
							if (scoreList.length > 0 && !NbaUtils.isEmpty(scoreList[0])) {
								deOink.put("A_OverallRiskScore", convertToDefault(scoreList[0]));
							}
							deOink.put("A_PprReqStatus", String.valueOf(pprReqstatus));
							// APSL4502 END
							break;
						}

					}
				}
			}
			//deOink.put("A_RequirementScore", String.valueOf(requirementScore)); commented for APSL4502
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess();
			nbaOinkDataAccess.setAcdbSource(new NbaAcdb(), nbaTxLife);
			nbaOinkDataAccess.setLobSource(work.getNbaLob());
			nbaOinkDataAccess.setContractSource(nbaTxLife, work.getNbaLob());
			vpmsProxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsAdaptor.PREDICTIVE_IMPAIRMENTS);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			if (vpmsResultsData == null) {
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.PREDICTIVE_IMPAIRMENTS);
			}
			xmlString = (String) vpmsResultsData.getResultsData().get(0);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("### Results from VPMS Model:" + NbaVpmsAdaptor.PREDICTIVE_IMPAIRMENTS);
				getLogger().logDebug(xmlString);
			}
			nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
			vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
			predictiveImpairments = vpmsModelResult.getImpairmentInfo();
			success = true;
		} catch (RemoteException e) {
			handleRemoteException(e, NbaVpmsAdaptor.PREDICTIVE_IMPAIRMENTS);
		} finally {
			if (vpmsProxy != null) {
				try {
					vpmsProxy.remove();
				} catch (RemoteException e) {
					getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
		}
		return success;

	}
	/**
	 * Converts deOink String values to defaults, if null Or Nan
	 * @param String: input String
	 * @return String: converted String
	 */	
	private String convertToDefault(String str) {
		if (str == null || str.equalsIgnoreCase("null")) {
			return "";
		} else if (str.equalsIgnoreCase("NaN")) {
			return "-1";
		}
		return str;
	}
	

}
