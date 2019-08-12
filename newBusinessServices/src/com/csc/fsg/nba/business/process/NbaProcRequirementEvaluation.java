package com.csc.fsg.nba.business.process;
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
 * 
 * *******************************************************************************<BR>
 */
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Message;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.process.evaluation.NbaVpmsModelProcessor;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaPollingException;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaEvaluateRequest;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.EvaluationControlModelResults;
import com.csc.fsg.nba.vpms.results.ResultData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;
/**
 * NbaProcRequirementEvaluation is the class that processes nbAccelerator cases found
 * on the AWD contract evaluation queue (NBCTEVAL).
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN008</td><td>Version 4</td><td>Underwriting Workflow Changes</td></tr>
 * <tr><td>ACN010</td><td>Version 4</td><td>Evaluation Control</td></tr>
 * <tr><td>ACP002</td><td>Version 4</td><td>Summary Processing</td></tr>
 * <tr><td>ACP001</td><td>Version 4</td><td>Lab Result Processing</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>Problems and Requirements Merging</td></tr>
 * <tr><td>ACP022</td><td>Version 4</td><td>Foreign Travel</td></tr>
 * <tr><td>ACP013</td><td>Version 4</td><td>Family History</td></tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>MIB 401/402 Migration</td></tr>
 * <tr><td>ACP008</td><td>Version 4</td><td>IU Preferred Processing </td></tr>
 * <tr><td>ACP009</td><td>Version 4</td><td>Non Medical Screening</td></tr>
 * <tr><td>ACP006</td><td>Version 4</td><td>MIB Evaluation</td></tr>
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>SPR2450</td><td>Version 5 </td><td>AC Installation should bypass annuity products</td></tr>
 * <tr><td>NBA122</td><td>Version 5 </td><td>Underwriter Workbench Rewrite</td></tr>
 * <tr><td>SPR2819</td><td>Version 5 </td><td>Auto process APRQEVAL error stops</td></tr>
 * <tr><td>SPR2741</td><td>Version 6</td><td>Re-evaluation is generating insert errors</td></tr>
 * <tr><td>SPR3160</td><td>Version 6</td><td>Requirement Evaluation is expecting Requirement Results attachment to be OLI_LU_BASICATTACHMENTTYP(271)  instead of OLI_LU_BASICATTMNTTY_TEXT (1)</td></tr>
 * <tr><td>SPR2199</td><td>Version 6</td><td>P&R Requirements Merging Logic Needs to Change to Not Discard some Requirements</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * <tr><td>SPR3309</td><td>Version 8</td><td>Duplicate comments are getting added by the Requirement Evaluation automated process</td></tr>
 * <tr><td>ALS4052</td><td>AxaLife Phase 1</td><td> QC # 2874  - AXAL3.7.31 Provider Interface : ExamOne : MVR data values not processing as expected</td></tr>
 * <tr><td>ALS5388</td><td>AXALife Phase 1</td><td>QC#4560-Provider results, CRL, urine rqmt not evaluated</td></tr>
 * <tr><td>PERF-APSL601</td><td>AXA Life Phase 1</td><td>PERF - View/Update Requirement optimization</td></tr>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Full Implementation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 * @see NbaAutomatedProcess
 */
public class NbaProcRequirementEvaluation extends NbaAutomatedProcess {
	protected NbaTXLife txLifeReqResult = null; //ACN009
	protected ArrayList listImpairmentInfo = new ArrayList();
	protected ArrayList listAcceptableImpairments = new ArrayList();
	private NbaAcdb nbaAcdb = new NbaAcdb();
	private String reqId = "";
	private String vpmsResult = "";
	private NbaDst parentWork; //APSL3874

	protected static final String DATA_FOR_SYSTEMATIC_EVALUATION_NOT_PRESENT = "Data Required for systematic evaluation not present"; //SPR3309

	public NbaProcRequirementEvaluation() {
		super();
		setContractAccess(UPDATE);
	}

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		boolean debugLogging = getLogger().isDebugEnabled();
		boolean dataFound = false; //AXAL3.7.07
		NbaEvaluateRequest req = null;//ALS3959
		if (!initialize(user, work)) {
			return getResult();
		}
		if (debugLogging) {
			getLogger().logDebug("Requirement Evaluation: Contract" + getWork().getNbaLob().getPolicyNumber() + "; Requirement Type " + getWork().getNbaLob().getReqType());
		}
		String reqInfoUniqueId = work.getNbaLob().getReqUniqueID();
		getLogger().logDebug("reqInfoUniqueId: " + reqInfoUniqueId);
		// begin NBA122
		boolean uwApplet = NbaConfiguration.getInstance().isUnderwriterWorkbenchApplet();
		RequirementInfo reqInfo = getNbaTxLife().getRequirementInfo(reqInfoUniqueId);
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		// APSL4740 Begin
		boolean hipaaIgoInd = NbaRequirementUtils.retrieveHIPAAIgoInd(getWork(), getUser());
		if (reqInfoExt != null && reqInfoExt.getReviewedInd() && !hipaaIgoInd) {
			reqInfoExt.setReviewedInd(false);
			reqInfoExt.setReviewID(null);
			reqInfoExt.setReviewDate((Date)null);
			reqInfoExt.setActionUpdate();
			doContractUpdate(nbaTxLife);
		}
		// APSL4740 End
		//if this is the original UW applet or the requirement has NOT been reviewed
		if (uwApplet || !(reqInfoExt != null && reqInfoExt.getReviewedInd())) {
		dataFound = retrieveTxLifeReqResult(reqInfo); //ACN009, AXAL3.7.07
			// end NBA122
			if (getResult() != null) {
				changeStatus(getResult().getStatus());
				//SPR3309 code deleted
				//begin PERF-APSL601
				if (!isWorkItemIdPopulated(reqInfoExt)) {
					reqInfoExt.setWorkitemID(getWork().getID());
					reqInfoExt.setActionUpdate();
					doContractUpdate(nbaTxLife);
				}
				//end PERF-APSL601
				doUpdateWorkItem();
				return getResult();
			}
			if (!(work.getNbaLob().getReqStatus().equalsIgnoreCase(Long.toString(NbaOliConstants.OLI_REQSTAT_COMPLETED))) && txLifeReqResult != null) { //ACN010,//SR564247(ALII1673)
		ArrayList al = callEvaluationControl(work);
				EvaluationControlModelResults evalModel = null;
				// SPR3290 code deleted
				for (int i = 0; i < al.size(); i++) {
					evalModel = (EvaluationControlModelResults) al.get(i);
					NbaVpmsModelProcessor processor;
					try {
						processor = (NbaVpmsModelProcessor) NbaUtils.classForName(evalModel.getJavaImplClass()).newInstance();
						processor.initialize(nbaTxLife, user, work, txLifeReqResult); //SPR2741
						processor.execute(); //SPR2741
					} catch (InstantiationException e) {
						throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, NbaPollingException.CLASS_INVALID
								+ NbaBaseException.getExceptionOrigin(e)); // APSL3874
					} catch (IllegalAccessException e) {
						throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, NbaPollingException.CLASS_ILLEGAL_ACCESS
								+ NbaBaseException.getExceptionOrigin(e));// APSL3874
					} catch (ClassNotFoundException e) {
						throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, NbaPollingException.CLASS_NOT_FOUND
								+ NbaBaseException.getExceptionOrigin(e));// APSL3874
						//begin SPR2741
					} catch (NbaBaseException nbe) {
						if (nbe.isFatal()) {
							throw nbe;
						}
						//APSL3874 Code Deleted
						throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, nbe.getExceptionOrigin()); // APSL3874
					}
					//end SPR2741
				}
			} //ACN010
			if (uwApplet) { //NBA122
				//Begin ACN010
				if (this.getRequirementStatus(reqInfo)) {
					getLogger().logDebug("Requirement Evaluation successful for AC installation.");
				} else {
					getLogger().logDebug("Requirement Evaluation Failed for AC installation.");
				}
				//End ACN010
				//begin NBA122
			} else {
				if (reqInfoExt == null) {
					OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
					reqInfo.addOLifEExtension(olifeExt);
					reqInfoExt = olifeExt.getRequirementInfoExtension();
				}
				if (dataFound) { //AXAL3.7.07, only set review indicator when data is present to evaluate.,SR564247(APSL2525),ALII1673 - Removed check for Predictive Cases
					long reqCode = reqInfo.getReqCode(); //NBLXA-1983
					if (reqCode != NbaOliConstants.OLI_REQCODE_BLOOD && reqCode != NbaOliConstants.OLI_REQCODE_URINE
							&& reqCode != NbaOliConstants.OLI_REQCODE_MVRPT && reqCode != NbaOliConstants.OLI_REQCODE_VITALS) { //NBLXA-1983
					reqInfoExt.setReviewedInd(true);
					reqInfoExt.setReviewID(user.getUserID());
					reqInfoExt.setReviewDate(new Date());
					}
					// end AXAL3.7.07
					if (!nbaTxLife.isSIApplication()) { //APSL2808
						//begin ALS3959
						NbaVpmsModelResult modelResult = getDataFromVpms(NbaVpmsAdaptor.EP_GET_REQ_NEED_REVAL);
						if (modelResult.getVpmsModelResult() != null && modelResult.getVpmsModelResult().getResultDataCount() > 0) {
							ResultData resultData = modelResult.getVpmsModelResult().getResultDataAt(0);
							String needRevalWorkItem = resultData.getResultAt(0);
							if (YES_VALUE.equalsIgnoreCase(needRevalWorkItem)) {
								NbaDst parentCase = retrieveParentCaseOnly();
								req = new NbaEvaluateRequest();
								req.setNbaUserVO(user);
								req.setWork(parentCase);
								req.setContract(nbaTxLife);
								req.setOverrideContractCommit(true);
								req.setUserFunction("NBEVAL");
								req.setUnderwritingWB(true);//ALS3972
								req.setResetUWWB(true);//ALS3972
								AccelResult accelResult = (AccelResult) currentBP.callBusinessService("GenerateEvaluateWorkItemBP", req);
								parentWork = req.getWork(); // APSL3874
								if (accelResult.hasErrors()) {
									getLogger().logError("Error creating NBREEVAL work item for contract " + nbaTxLife.getPolicy().getPolNumber());
									//Begin ALS5388
									if (accelResult.getMessagesList().size() > 0) {
										Message msg = (Message) accelResult.getMessagesList().get(0);
										List data = msg.getData();
										if (data != null && data.get(0) != null) {
											String messageStr = data.get(0).toString();
											if (messageStr.equalsIgnoreCase(NbaAWDLockedException.LOCKED_BY_USER)) {
												throw new NbaAWDLockedException("NBREEVAL workitem locked by another user");
											}
										}
									}
									//End ALS5388
									//APSL3874 Code Deleted
									throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_REEVAL); // APSL3874
								}
								req = (NbaEvaluateRequest) accelResult.getFirst();
								update(req.getWork());
							}
						}
						//End ALS3959
					}
				}
				reqInfoExt.setActionUpdate();
			}
			//end NBA122
			//ACN024
			//begin PERF-APSL601
			if (!isWorkItemIdPopulated(reqInfoExt)) {
				reqInfoExt.setWorkitemID(getWork().getID());
			}
			reqInfoExt.setActionUpdate(); //APSL839
			doContractUpdate(nbaTxLife);
		} //NBA122
		changeStatus(getPassStatus());
		result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus());
		doUpdateWorkItem();
		if (req != null) {
			unlockWork(req.getWork()); //ALS3959
		}
		return result;
	}

	//ACP002 new method.
	//ACN009 Renamed method since it will retrieve both 1122 and 401 responses
	//NBA122 Changed the parameter to a RequirementInfo
	private boolean retrieveTxLifeReqResult(RequirementInfo reqInfo) throws NbaBaseException { //AXAL3.7.07
		boolean dataFound = false; //AXAL3.7.07
		//QC14106/APSL3988 Deleted Code
		//APSL3874 Code Deleted
		//BEGIN SPR2819
		if (reqInfo == null) {
			//Begin APSL3874
			RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
			if (!isWorkItemIdPopulated(reqInfoExt)) {
				reqInfoExt.setWorkitemID(getWork().getID());
				reqInfoExt.setActionUpdate();
				doContractUpdate(nbaTxLife);
			}
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_REQINFO);
			//End APSL3874
		}
		//END SPR2819
		// begin NBA122
		//ALS4052 code deleted
		ArrayList attachmentList = reqInfo.getAttachment();
		dataFound = getPCData(attachmentList); //ALS4052
		// end NBA122
		//SR564247(ALII1673)
		//APSL3453
		boolean predictiveJetEligible=NbaUtils.isPredictiveJetEligible(getNbaTxLife()) && isRequirementEligible(work.getNbaLob());
		if (predictiveJetEligible) { //ALII1983
			dataFound = true;
		}
		
		//APSL4968 deleted code for Rx.				
		//QC14106/APSL3988 Deleted Code	
		//Begin QC14106/APSL3988
		boolean noAutoReviewRequirements= isRequirementNotEligibleForAutoReview();
		if(noAutoReviewRequirements){
			dataFound=false;
		}
		if (!dataFound) { //ALS4052
			//Begin ACN010
			if (NbaConfiguration.getInstance().isUnderwriterWorkbenchApplet()) { //NBA122
				work.getNbaLob().setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED));
			}//NBA122
			addComment(getPassStatusComment(AxaStatusDefinitionConstants.VARIANCE_KEY_EVAL, getPassStatus()));// SPR2199 SPR3309 APSL3874
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus())); // SPR3309,APSL3874
							//setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "PCDATA for 1122 is NULL", getHostErrorStatus())); //Code commented for ACN010
			//throw new NullPointerException("PCDATA for 1122 is NULL.");
			//END ACN010
		}
		//ALS4052 code deleted
		//APSL3874 code deleted
		return (dataFound); //AXAL3.7.07
	}

	//	ACP002 new Method.
	private boolean getPCData(ArrayList attachmentList) throws NbaBaseException { //ALS4052
		String pcData = null;
		boolean valid = false; //ALS4052
		NbaTXLife txLifeReqRslt = null;
		for (int j = 0; j < attachmentList.size(); j++) {
			Attachment attach = (Attachment) attachmentList.get(j);
			//begin SPR3160
			long attType = attach.getAttachmentType();
			if (attach.getAttachmentBasicType() == NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT 
					&& (attType == NbaOliConstants.OLI_ATTACH_REQUIRERESULTS //ACN009
							|| attType == NbaOliConstants.OLI_ATTACH_MIB_SERVRESP //ACN009
							|| (!nbaTxLife.isADCApplication() && !nbaTxLife.isInformalApplication() && attType == NbaOliConstants.OLI_ATTACH_MVR))) { //QC5914/APSL839
				//end SPR3160
				AttachmentData attachData = attach.getAttachmentData();
				if (attachData != null) {
					pcData = attachData.getPCDATA();
					try {
						txLifeReqRslt = new NbaTXLife(pcData);
						if (txLifeReqRslt != null) {
							if (AxaUtils.isDataResult(txLifeReqRslt) || attType == NbaOliConstants.OLI_ATTACH_MIB_SERVRESP) { //ALS5062
								valid = true;
								txLifeReqResult = txLifeReqRslt;
								break;
							}
						}
					} catch (Exception e) {
						throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, NbaBaseException.getExceptionOrigin(e)); // APSL3874
					}
				}
			}
		}
		return valid;
	}

	/**
	 * This method is used to call the Evaluation C0ntrol model
	 * 
	 * @param
	 * @return ArrayList
	 * @throws NbaBaseException
	 */

	private ArrayList callEvaluationControl(NbaDst work) throws NbaBaseException {
		ArrayList sortedList = null; // ACP007
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(txLifeReqResult); // SPR2450, AXAL3.7.07
			oinkDataAccess.setLobSource(work.getNbaLob()); // SPR2450
			vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, NbaVpmsAdaptor.EVALUATIONCONTROL); //ACP008,SPR2450
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_CALCXMLOBJECTS); //ACP008
			Map deOink = new HashMap();
			int reqCode = work.getNbaLob().getReqType();
			deOink.put("A_REQCODE_INS", String.valueOf(reqCode));
			deOink.put("A_XMLRESPONSE", "true");
			deOink.put("A_INSTALLATION", getInstallationType());
			deOink.put("A_WORKTYPE_LOB", work.getNbaLob().getWorkType());			
			deOink.put("A_PredictiveInd", String.valueOf(NbaUtils.isPredictiveJetEligible(getNbaTxLife()))); //APSL4451			 
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult vcr = vpmsProxy.getResults();
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vcr);
			ArrayList results = vpmsResultsData.getResultsData();
			results = vpmsResultsData.getResultsData();
			//Resulting string will be the zeroth element.
			NbaVpmsModelResult vpmsOutput = null;
			VpmsModelResult vpmsModelResult = null;
			if (results == null) {
				throw new NullPointerException("ERROR: NULL RESULTS from VPMS");
			} else {
				String result = (String) results.get(0);
				vpmsOutput = new NbaVpmsModelResult(result);
				vpmsModelResult = vpmsOutput.getVpmsModelResult();
			}
			//SPR3362
			ArrayList modelResults = vpmsModelResult.getEvaluationControlModelResults();
			SortedMap map = new TreeMap();
			// Begin ACP007
			// Modified logic to use SortedMap
			for (int i = 0; i < modelResults.size(); i++) {
				EvaluationControlModelResults modelResult = (EvaluationControlModelResults) modelResults.get(i);
				Integer key = new Integer(modelResult.getProcessSequence().toString());
				map.put(key, modelResult);
			}
			// Iterate on treemap and convert to sorted ArrayList
			Set set = map.entrySet();
			Iterator itr = set.iterator();
			sortedList = new ArrayList();
			while (itr.hasNext()) {
				Map.Entry me = (Map.Entry) itr.next();
				sortedList.add(me.getValue());
			}
			// End ACP007
		} catch (NbaVpmsException e) {
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_VPMS, e.getExceptionOrigin()); // APSL3874
		} catch (RemoteException e) {
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, NbaBaseException.getExceptionOrigin(e));// APSL3874
			//begin SPR3362
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (RemoteException re) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		//end SPR3362
		return sortedList;
	}

	//	ACN010 new method
	// NBA122 Changed the parameter to a RequirementInfo
	private boolean getRequirementStatus(RequirementInfo reqInfo) throws NbaBaseException {
		boolean result = false; //NBA122
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(txLifeReqResult);
			oinkData.setLobSource(work.getNbaLob());
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS); //SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_REQUIREMENT_STATUS);
			Map deOink = new HashMap();
			deOink.put("A_INSTALLATION", getInstallationType());
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			ArrayList results = data.resultsData;
			VpmsModelResult vpmsModelResult = null;
			RequirementInfo reqInfoObject = null;
			if (results == null) {
				throw new NullPointerException("ERROR: NULL RESULTS from VPMS in getRequirementStatus");
			} else {
				String xmlString = (String) results.get(0);
				NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
				vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
				ArrayList requirementInfo = new ArrayList();
				requirementInfo = vpmsModelResult.getRequirementInfo();
				reqInfoObject = (RequirementInfo) requirementInfo.get(0);
				long reqStatusFromVPMS = reqInfoObject.getReqStatus();
				// begin NBA122
				if (((reqStatusFromVPMS == NbaOliConstants.OLI_REQSTAT_COMPLETED) 
						&& !(work.getNbaLob().getReqStatus().equalsIgnoreCase(String.valueOf(NbaOliConstants.OLI_REQSTAT_COMPLETED))))
					|| ((reqStatusFromVPMS == NbaOliConstants.OLI_REQSTAT_RECEIVED) 
							&& (NbaUtils.isBlankOrNull(work.getNbaLob()	.getReqReceiptDate())))) {//ALS3110
					work.getNbaLob().setReqReceiptDate(new Date());
					work.getNbaLob().setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
					reqInfo.setReceivedDate(new Date());
					RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					if (requirementInfoExt == null) {
						OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
						requirementInfoExt = olifeExt.getRequirementInfoExtension();
					}
					requirementInfoExt.setReceivedDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
					requirementInfoExt.setActionUpdate();
				}
				work.getNbaLob().setReqStatus(String.valueOf(reqStatusFromVPMS));
				reqInfo.setReqStatus(String.valueOf(reqStatusFromVPMS));
				reqInfo.setActionUpdate();
				result = true;
				// end NBA122
			}
			//SPR3362 code deleted
		} catch (NbaVpmsException nve) {
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_VPMS, nve.getExceptionOrigin()); // APSL3874
		} catch (Exception e) {
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_VPMS, NbaBaseException.getExceptionOrigin(e)); // APSL3874
			//begin SPR3362
		} finally {
			if (vpmsProxy != null) {
				try {
					vpmsProxy.remove();
				} catch (RemoteException re) {
					getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
		}
		//end SPR3362
		return result; //NBA122
	}

	/**
	 * This method creates and initializes an <code>NbaVpmsAdaptor</code> object to call VP/MS to execute the supplied entryPoint.
	 * 
	 * @param entryPoint
	 *                 the entry point to be called in the VP/MS model
	 * @return the results from the VP/MS call in the form of an <code>NbaVpmsResultsData</code> object
	 */
	//ALS3959 new method
	protected NbaVpmsModelResult getDataFromVpms(String entryPoint) throws NbaBaseException, NbaVpmsException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS);
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsModelResult modelResult = new NbaVpmsModelResult(vpmsProxy.getResults().getResult());
			return modelResult;
		} catch (java.rmi.RemoteException re) {
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_VPMS, NbaBaseException.getExceptionOrigin(re)); // APSL3874
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (RemoteException re) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
	}

	//PERF-APSL601 New Method
	protected boolean isWorkItemIdPopulated(RequirementInfoExtension reqInfoExt) {
		if (reqInfoExt == null) {
			return false;
		}
		if (null == reqInfoExt.getWorkitemID() || reqInfoExt.getWorkitemID().trim().length() <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * Determines if the requirement having Medical indicator set.
	 * 
	 * @param currentReq
	 *                 last requirement's nbaLob
	 * @return true if medical indicator is true otherwise false
	 */
	//(SR564247)ALII1673 - New Method-Discontinued
	//ALII1983 New Method
	protected boolean isRequirementEligible(NbaLob nbaLob) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null;
		ArrayList reqList = new ArrayList();
		String reqType = String.valueOf(nbaLob.getReqType());
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.PREDICTIVE_ANALYSIS);
			vpmsProxy.setVpmsEntryPoint(EP_GET_ELIGIBLESYSREV_REQUIREMENT_LIST);
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult compResult = vpmsProxy.getResults();
			NbaStringTokenizer tokens = new NbaStringTokenizer(compResult.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			String aToken;
			while (tokens.hasMoreTokens()) {
				aToken = tokens.nextToken();
				reqList.add(aToken);
			}
		} catch (java.rmi.RemoteException re) {
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_VPMS, NbaBaseException.getExceptionOrigin(re)); // APSL3874
		} catch (NbaBaseException e) {
			getLogger().logDebug(
					"Exception occurred while getting Eligible System Review Requirement Types For Jet Cases From PREDICTIVE_ANALYSIS VPMS Model : "
							+ NbaUtils.getStackTrace(e));
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}

		}
		if (reqList.contains(reqType)) {
			return true;
		}
		return false;
	}
	
	// APSL3874 New Method
	public void handleErrorStatus(){
		try {
			if(this.parentWork != null && this.parentWork.isLocked(user.getUserID())){
				unlockWork(this.parentWork);
			}
		} catch (NbaBaseException e) {
			return;
		}
	}
	
	/**
	 * Determines if the requirement is not eligible for autoReview.
	 * 
	 * @return true if indicator is true otherwise false
	 */
	//QC14106/APSL3988 - New Method
	
	protected boolean isRequirementNotEligibleForAutoReview() throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy=null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS);
			vpmsProxy.setVpmsEntryPoint(EP_CHECK_NO_AUTOREVIEW_REQUIREMENTS);
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult vpmsResult = vpmsProxy.getResults();			
			if (vpmsResult.getReturnCode() != 0 && vpmsResult.getReturnCode() != 1) {	//If a bad code is returned throw an exception 
				throw new NbaVpmsException(vpmsResult.getMessage());
			}
			return Boolean.valueOf(vpmsResult.getResult()).booleanValue();
			
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("VPMS Exception Occured" + NbaVpmsException.VPMS_EXCEPTION, re);
		} catch (NbaBaseException e) {
			getLogger().logDebug(
					"Exception occurred while getting Eligible System Review Requirement Types For Jet Cases From PREDICTIVE_ANALYSIS VPMS Model : "
							+ NbaUtils.getStackTrace(e));
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}

		}
		return false;
	}
}
