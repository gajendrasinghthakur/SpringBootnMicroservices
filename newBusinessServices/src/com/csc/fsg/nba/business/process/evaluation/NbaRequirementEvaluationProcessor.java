package com.csc.fsg.nba.business.process.evaluation;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

import java.util.ArrayList;

import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Class that will take care of the processing once RequirementEvaluation model is invoked 
 * from NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * <tr><td>CR731686</td><td>AXA Life Phase 2</td><td>Preferred Processing</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaRequirementEvaluationProcessor extends NbaVpmsModelProcessor {
	protected ArrayList reqEvalImpairments = new ArrayList();
	protected ArrayList reqEvalAccepImpairments = null;
	/**
	 * This function controls the processing needed to call the RequirementEvaluation
	 * model to determine any impairments for the requirement.	
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		impSrc = NbaConstants.REQUIREMENTEVALUTION_SRC; //ACN016
		setPartyID(work);
		isSuccess = callRequirementEvaluationModel();
		if (!isSuccess) {
			throw new NbaBaseException("Exception in execute method of NbaACLabTvcProcessor");
		}
		mergeImpairmentsAndAccep(reqEvalImpairments, reqEvalAccepImpairments);
	}
	/**
	 * This function is used to call the RequirementEvaluation model. It updates
	 * the reqEvalImpairments object with any impairments returned from the model.
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */
	public boolean callRequirementEvaluationModel() throws NbaBaseException {
		VpmsModelResult vpmsModelResult = null;
		NbaVpmsModelResult nbaVpmsModelResult = null;
		String xmlString = "";
		// SPR3290 code deleted
		NbaVpmsAdaptor vpmsProxy = null;
		boolean success = false;
		try {
			NbaOinkDataAccess accessContract = new NbaOinkDataAccess(txLifeReqResult);
			accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
			accessContract.setPlanSource(nbaTxLife, null);//CR731686
			vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.REQUIREMENTEVALUATION);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);
			Object[] args = getKeys();
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setRequirementIdFilter(txLifeReqResult.getPolicy().getRequirementInfoAt(0).getId());
			oinkRequest.setArgs(args);
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			if (vpmsResultsData == null) {
				success = false;
				throw new NullPointerException("ERROR: NULL RESULTS from VPMS");
			} else {
				xmlString = (String) vpmsResultsData.getResultsData().get(0);
				if(getLogger().isDebugEnabled()) { //SPR3290
				    getLogger().logDebug("### Results from VPMS Model:" + NbaVpmsAdaptor.REQUIREMENTEVALUATION);
				    getLogger().logDebug(xmlString);
				}//SPR3290
				nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
				vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
				reqEvalImpairments = vpmsModelResult.getImpairmentInfo();
				success = true;
			}
			//SPR3362 code removed
		} catch (NbaVpmsException nve) {
			throw new NbaBaseException("NbaVpmsException Exception occured in callRequirementEvaluationModel", nve);
		} catch (Exception e) {
			throw new NbaBaseException("Exception occured in callRequirementEvaluationModel", e);
        //begin SPR3362
        } finally {
            try {
                if (vpmsProxy != null) {
                    vpmsProxy.remove();
                }
            } catch (Throwable th) {
                LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
            }
        }
     //end SPR3362
		return success;
	}
}
