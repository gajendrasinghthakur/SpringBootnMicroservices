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


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
 * Class that will take care of the processing once ACLabExam model is invoked 
 * from NBCTEVAL and NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACLabExamProcessor extends NbaVpmsModelProcessor {

	protected ArrayList labExamImpairments = new ArrayList();//ACN024
	protected ArrayList accepImpairments = null;	
	
	/**
	 * Overridden method, calls the model and 
	 * updates the contract with impairments.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;	
		impSrc = NbaConstants.LABEXAM_SRC; //ACN016
		if (performingRequirementsEvaluation()) { //SPR2652
			setPartyID(work); //ACN024
			isSuccess = callLabExamModel();
			if (!isSuccess) {
				throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.AC_LAB_EXAM);	//SPR2652
			}
			   //Do the Impairments Merging //ACN024
			  mergeImpairmentsAndAccep(labExamImpairments,accepImpairments); //ACN016							
		}
	}
		
	/**
	 * This function is used to call the ACLabExam model	 
	 * @param 
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */
	//ACP001 New Method	
	public boolean callLabExamModel() throws NbaBaseException {
		VpmsModelResult vpmsModelResult = null;
		boolean success = false;
		ArrayList tempImpairmentList = null;
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess accessContract = new NbaOinkDataAccess(txLifeReqResult); //ACN009
			accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
			accessContract.setLobSource(work.getNbaLob());  //AXAL3.7.07
			vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.AC_LAB_EXAM); //SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);
			Map deOink = getLabExamDeOINKValues();
			Object[] args = getKeys();
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setRequirementIdFilter(reqId);
			oinkRequest.setArgs(args);
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData vpmsResultsData;
			vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			if (vpmsResultsData == null) {
				//SPR3362 code deleted
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_LAB_EXAM); //SPR2652
			} //SPR2652
			String xmlString = (String) vpmsResultsData.getResultsData().get(0);
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug("### Results from VPMS Model:" + NbaVpmsAdaptor.AC_LAB_EXAM);
			    getLogger().logDebug(xmlString);
			} //SPR3290
			NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
			vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
			labExamImpairments = vpmsModelResult.getImpairmentInfo(); //ACN024
			
			if (tempImpairmentList != null && tempImpairmentList.size() != 0) {
				//listImpairmentInfo.addAll(tempImpairmentList); //ACN024
			}
			success = true;
			// SPR2652 Code Deleted
			//SPR3362 code deleted
		// SPR2652 Code Deleted
		} catch (RemoteException e) {	//SPR2652
			handleRemoteException(e, NbaVpmsAdaptor.AC_LAB_EXAM); //SPR2652
		//begin SPR3362
		} finally {
		    if(vpmsProxy != null){
		        try {
                    vpmsProxy.remove();
                } catch (RemoteException e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
		    }
		//end SPR3362
		}
		return success;
	}
	
	/**
	 * This function gets all the deOink variables for AcLabExam model	 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//ACP001 New Method			
	public Map getLabExamDeOINKValues() throws NbaBaseException {
		String[] questionNumberList = null;
		String[] questionTextList = null;
		String[] responseCodeList = null;
		int questionNumberCount = 0;
		HashMap deOink = new HashMap();
		NbaOinkDataAccess dataAccess = new NbaOinkDataAccess(txLifeReqResult); //ACN009
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		//Get the Form Question Number
		oinkRequest.setVariable("FormQuestionNumber");
		oinkRequest.setRequirementIdFilter(reqId);
		questionNumberList = dataAccess.getStringValuesFor(oinkRequest);
		questionNumberCount = questionNumberList.length;
		deOink.put("A_NO_OF_FORMQUESTIONNUMBER", (new Integer(questionNumberCount)).toString());
		for (int i = 0; i < questionNumberCount; i++) {
			if (i == 0) {
				deOink.put("A_FORMQUESTIONNUMBER", questionNumberList[i]);
			} else {
				deOink.put("A_FORMQUESTIONNUMBER[" + i + "]", questionNumberList[i]);
			}
		}
		//Get the Form Question Text
		oinkRequest.setVariable("FORMQUESTIONTEXT");
		oinkRequest.setRequirementIdFilter(reqId);
		questionTextList = dataAccess.getStringValuesFor(oinkRequest);
		for (int i = 0; i < questionTextList.length; i++) {
			if (i == 0) {
				deOink.put("A_FORMQUESTIONTEXT", questionTextList[i]);
			} else {
				deOink.put("A_FORMQUESTIONTEXT[" + i + "]", questionTextList[i]);
			}
		}
		//Get the Form Response Code
		oinkRequest.setVariable("FORMRESPONSECODE");
		oinkRequest.setRequirementIdFilter(reqId);
		responseCodeList = dataAccess.getStringValuesFor(oinkRequest);
		for (int i = 0; i < responseCodeList.length; i++) {
			if (i == 0) {
				deOink.put("A_FORMRESPONSECODE", responseCodeList[i]);
			} else {
				deOink.put("A_FORMRESPONSECODE[" + i + "]", responseCodeList[i]);
			}
		}
		NbaOinkDataAccess accessContract = new NbaOinkDataAccess(nbaTxLife);
		//Get ReqCodeList
		oinkRequest.setVariable("ReqCodeList");
		String[] codeList = accessContract.getStringValuesFor(oinkRequest);
		int count = codeList.length;
		deOink.put("A_no_of_ReqCode", (new Integer(count)).toString());
		if (count == 0) {
			deOink.put("A_ReqCodeList", "");
			deOink.put("A_ReqStatusList", "");
		} else {
			for (int i = 0; i < count; i++) {
				if (i == 0) {
					deOink.put("A_ReqCodeList", codeList[i]);
				} else {
					deOink.put("A_ReqCodeList[" + i + "]", codeList[i]);
				}
			}
			//Get ReqStatusList
			oinkRequest.setVariable("ReqStatusList");
			oinkRequest.setCount(1);
			String[] statusList = accessContract.getStringValuesFor(oinkRequest);
			count = statusList.length;
			for (int i = 0; i < count; i++) {
				if (i == 0) {
					deOink.put("A_ReqStatusList", statusList[i]);
				} else {
					deOink.put("A_ReqStatusList[" + i + "]", statusList[i]);
				}
			}
		}
		// begin AXAL3.7.07
		getPrescriptionLabelDeOINKValues(deOink, oinkRequest, dataAccess);
		getPrescriptionCodeDeOINKValues(deOink, oinkRequest, dataAccess);
		// end AXAL3.7.07

		return deOink;
	}
	/**
	 * This function gets all the deOink the ScriptCheck PrescriptionLabel variables for AcLabExam model	 
	 * @param java.util.Map : The Hash Map containing all the deOink variables
	 * @return void
	 * @throws NbaBaseException
	 */
	//AXAL3.7.07 New Method			
	public void getPrescriptionLabelDeOINKValues(Map deOink, NbaOinkRequest oinkRequest, NbaOinkDataAccess dataAccess) throws NbaBaseException {
		String[] prescriptionLabelList = null;
		int prescriptionLabelCount = 0;
		String prescriptionLabel = "";
	    oinkRequest.setVariable("PrescriptionLabel_INS");
		oinkRequest.setRequirementIdFilter(reqId);
		prescriptionLabelList = dataAccess.getStringValuesFor(oinkRequest);
		prescriptionLabelCount = prescriptionLabelList.length;
		deOink.put("A_no_of_Prescriptions", (new Integer(prescriptionLabelCount)).toString());

		for (int i = 0; i <prescriptionLabelCount; i++) {
			prescriptionLabel = prescriptionLabelList[i];
			if (i == 0) {
				deOink.put("A_PrescriptionLabel", prescriptionLabel);
			} else {
				deOink.put("A_PrescriptionLabel[" + i + "]", prescriptionLabel);
			}
		}  //end for 
	    return;
	}
	/**
	 * This function gets all the deOink the ScriptCheck PrescriptionCode variables for AcLabExam model	 
	 * @param java.util.Map : The Hash Map containing all the deOink variables
	 * @return void
	 * @throws NbaBaseException
	 */
	//AXAL3.7.07 New Method			
	public void getPrescriptionCodeDeOINKValues(Map deOink, NbaOinkRequest oinkRequest, NbaOinkDataAccess dataAccess) throws NbaBaseException {
		String[] prescriptionCodeList = null;
		int prescriptionCodeCount = 0;
		String prescriptionCode = "";
	    oinkRequest.setVariable("PrescriptionCode_INS");
		oinkRequest.setRequirementIdFilter(reqId);
		prescriptionCodeList = dataAccess.getStringValuesFor(oinkRequest);
		prescriptionCodeCount = prescriptionCodeList.length;
		for (int i = 0; i <prescriptionCodeCount; i++) {
			prescriptionCode = prescriptionCodeList[i];
			if (i == 0) {
				deOink.put("A_PrescriptionCode", prescriptionCode);
			} else {
				deOink.put("A_PrescriptionCode[" + i + "]", prescriptionCode);
			}
		}  //end for 
	    return;
	}
}
